# 1.hook dx.jar实现插栓
---
[APM原理链接](http://blog.csdn.net/sgwhp/article/details/50239747)
[APM实现链接](http://blog.csdn.net/sgwhp/article/details/50438666)
[APM源码地址](https://github.com/sgwhp/openapm)
# 1.1 dex和processClass方法
- `APM(性能监控)`的`AOP字节码插栓` 插件会在class编译成dex文件的时候注入相关的代码。关键点就在于编译dex文件的时候注入代码，这个编译的过程是由**dx(dx.bat)**执行的，具体的类和方法是`com.android.dx.command.dexer.Main#processClass`.此方法的第二个参数就是class的byte数组，所以只需要在进入`processClass`方法的时候，利用`ASM`工具对class的byte数组进行改造即可实现插入相关代码。

	  private static boolean processClass(String name, byte[] bytes)
	  {
		**********省略代码*************
	  }


# 1.2 Instrumentation和VirtualMachine
- 要让`JVM`在执行`processClass`之前先执行指定代码，就需要对`com.android.dx.command.dexer.Main`这个类进行改造。**java提供了Instrumentation和VirtualMachine来实现这个功能**

- `VirtualMachine`提供了`loadAgent`方法，其指定的`agent`会在`main`方法之前执行,并调用`agent`的`agentMain`方法，`agentMain`的第二个参数是`Instrumentation`.可以通过给`Instrumentation`设置`ClassFileTransformer`来实现对`dexer.Main`这个类的改造，同样也可以通过`ASM`来实现。

- 一般来说,hook dx.jar这种方式实现AOP字节码插栓，需要三个部分:**plugin,agent,具体的业务jar包**。
	- `agent`就是由`VirtualMachine`启动的代理
	- `plugin`就是一个gradle插件，所要做的事情就是调用`loadAgent`方法 。


- plugin开发过程注意：
	- **gradle插件需要添加`tools.jar(在jdk的lib目录下Java\jdk1.8.0_112\lib)`和`agent.jar(自定义agent)`为依赖**。
	- artifacs配置把源码和META-INF加上，但不能加tools.jar和agent.jar。 

- agent开发过程注意:
	- 必须提供`agentmain(String args,Instrumentation inst)`方法，并给`Instrumentation`设置`ClassFileTransformer`，然后在`transformer`中改造`dexer.Main`.当JVM成功执行到`transformer`的时候，会发现传进来的class没有`dexer.Main`.这是因为dx.bat执行dexer.Main,但是和plugin不在一个进程中！

 
# 1.3 ProcessBuilder
- dx.bat其实是由`ProcessBuilder`的start方法启动的，**`ProcessBuilder`有一个`command`成员，保存的是启动目标进程携带的参数**，只要给`dx.bat`带上`-javaagent`参数就能给dx.bat所在进程指定`agent`。 所以需要在执行`start`方法之前调用`command`方法以获取`command`，并往`command`中插入`-javaagent`参数。 参数值就是`agent.jar`所在的路径，可以使用`agent.jar`其中一个class实例`getProtectionDomain().getCodeSource().getLocation().toURI().getPath()`获得

- 这一步还是无法正确改造class,如果把改造类的代码单独放到一个类中，然后用ASM生成字节码调用这个类的方法来对command参数进行修改，就会发现抛出了ClassDefNotFoundError错误。这里涉及到了ClassLoader的知识

# 1.4 ClassLoader和InvocationHandler
- `ProcessBuilder`类是由`Bootstrap ClassLoader`加载的，而自定义的类则是由`AppClassLoader`加载的。

- `Bootstrap ClassLoader`处于`AppClassLoader`的上层，**上层类加载器所加载的类是无法直接引用下层类加载器所加载的类的**。但**如果下层类加载器加载的类实现或继承了上层类加载器加载的类或接口**，则上层类加载器加载的类获取到下层类加载的类的实例时就可以将其强制转型为父类，并调用父类的方法。

- 这个上层类加载器加载的接口，可以使用`InvocationHandler`。

- 还有一个问题，`ProcessBuilder`怎么才能获取到`InvocationHandler`子类的实例？有一个比较巧妙的做法，在`agent`启动的时候，创建`InvocationHandler`实例，并把它赋值给`Logger`的`treeLock`成员。`treeLock`是一个`Object`对象，并且只是用来加锁的，没有别的用途。但`treeLock`是一个`final`成员，所以记得要修改其修饰，去掉`final`。`Logger`同样也是由`Bootstrap ClassLoader`加载，这样`ProcessBuilder`就能通过反射的方式来获取`InvocationHandler`实例了。

# 2. 实例编写
## 2.1 业务逻辑
找到类当中的`test`方法，增加一句输出时间戳的代码！

## 2.2 agent 编写
- agent最终目的是要实现改写`com.android.dx.command.dexer.Main`,在它执行`processClasss`方法内的代码之前通过ASM工具修改其 第二个参数(也就是源class文件的字节码数组)

- `dexer.Main`和`plugin`不在同一个进程中，所以要实现改写`dexer.Main`之前还需要先改写`ProcessBuilder`的`command`成员变量，往其中插入`-javaagent 参数`.同样还是通过ASM工具，当访问到`ProcessBuilder`的`start`方法时，如果`start`的目标是`java`或`dx`,则加入`-javaagent`或`Jjavaagent`

