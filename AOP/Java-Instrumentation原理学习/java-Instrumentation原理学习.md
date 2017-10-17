# 简介
- [Java-Instrumentation 之premain](http://fengfu.io/2016/04/24/Java-Instrumentation%E7%A0%94%E7%A9%B6-1/)
- [Java-Instrumentation 之动态植入](http://fengfu.io/2016/04/25/Java-Instrumentation%E7%A0%94%E7%A9%B6-2/)
- [ASM-MVNREPOSITORY](https://mvnrepository.com/artifact/org.ow2.asm/asm-all/5.2)
- [Attach API](http://docs.oracle.com/javase/7/docs/jdk/api/attach/spec/index.html)

- `Instrumentation` 是Java SE5的新特性,它把Java的instrument功能从本地代码中解放出来，使之可以用Java代码的方式解决问题。使用`Instrumentation`，开发者可以构建一个独立于应用程序的代理程序（Agent），**用来监测和协助运行在JVM上的程序，甚至能够替换和修改某些类的定义**。有了这样的功能，就可以实现更为灵活的运行时虚拟机监控和Java类操作了，这样的特性实际上提供了一种虚拟机级别支持的AOP实现方式，使得无需对原有代码做任何升级和改动，就可以实现某些 AOP 的功能了。

- 在`Java SE6` 中 ,`Instrumentation`包被赋予了更强大的功能：**启动后的instrument、本地代码（native code）instrument，以及动态改变classpath等**。这些改变意味着Java具有了更强的动态控制、解释能力，使得Java语言变得更加灵活多变。


- 在`Java SE6`里面，**最大的改变是运行时的Instrumentation成为可能**。在Java SE5中，Instrument要求在运行前利用命令行参数或者系统参数来设置代理类，在实际的运行之中，虚拟机在初始化之时（在绝大多数的 Java 类库被载入之前），`instrumentation`的设置已经启动，并在虚拟机中设置了回调函数，检测特定类的加载情况，并完成实际工作。但是在实际的很多的情况下，我们没有办法在虚拟机启动之时就为其设定代理，这样实际上限制了`instrument`的应用。而`Java SE 6`的新特性改变了这种情况，通过Java Tool API中的attach方式，可以很方便地在运行过程中动态地设置加载代理类，以达到 instrumentation的目的。


# 1 原理

- `“java.lang.instrument”`包的具体实现，依赖于`JVMTI`。`JVMTI（Java Virtual Machine Tool Interface）`是一套由 Java虚拟机提供的，为JVM相关的工具提供的本地编程接口集合。`JVMTI`是从Java SE5开始引入，整合和取代了以前使用的 `Java Virtual Machine Profiler Interface (JVMPI) `和 `the Java Virtual Machine Debug Interface (JVMDI)`，而在Java SE6中，JVMPI和JVMDI已经消失了。

- `JVMTI`提供了一套“代理”程序机制，可以支持第三方工具程序以代理的方式连接和访问JVM，并利用JVMTI提供的丰富的编程接口，完成很多跟JVM相关的功能。事实上，**`java.lang.instrument`包的实现，也就是基于这种机制的**：在Instrumentation的实现当中，存在一个JVMTI的代理程序，通过调用JVMTI当中Java类相关的函数来完成Java类的动态操作。除开Instrumentation功能外，JVMTI还在虚拟机内存管理，线程控制，方法和变量操作等等方面提供了大量有价值的函数，具体可以参考[JVMTI官方文档](http://docs.oracle.com/javase/7/docs/platform/jvmti/jvmti.html)

# 2 Instrumentation的实现
- Jave SE5,premain **以命令行方式在VM启动前指定代理jar**
- Jave SE6,agentmain，**动态添加agent.jar，在VM启动后**

## 2.1 Java SE5
- 在Java SE5时代，Instrumentation只提供了`premain(命令行)`一种方式，即在真正的应用程序(包含main方法的程序)main方法启动之前启动一个代理程序。

- 在Java SE5中，**`Instrumentation`要求在运行前利用命令行参数或者系统参数来设置代理类.**

### 2.1.1 具体实现步骤
1. 编写`Agent`实现类，实现`ClassFileTransformer`接口。`ClassFileTransformer`声明了一个方法。通过这个方法，代理可以得到虚拟机载入的类的字节码(通过`classFileBuffer`参数)。**代理的各种功能一般都是操作这一串字节码得以实现，可以通过判断className来确定需要修改的类，然后修改classfileBuffer并返回修改过后的字节码以达到目的**

	    byte[] transform(ClassLoader loader, String className, Class<?> cBR, ProtectionDomain pD, byte[] classfileBuffer) throws IllegalClassFormatException;

	**注意这里的className 是`java/lang/Void`形式的，而且包含所有依赖的类!**

2. 编写`Agent`入口类，实现`premain`方法：

		public static void premain(String agentArgs, Instrumentation inst)

3. 打包`Agent`：将上述步骤1中声明的java类打包成一个jar文件,并在`META-INF/MANIFEST/MF`文件中加入`Premain-Class`来指定此Java类(此处需要完整类名，即包含包名)

		Manifest-Version: 1.0
		Premain-Class: com.hypers.My

4. 执行命令：

		java -javaagent:apmagent.jar TestMain

	**如果`Agent`类引入别的包，那么需要使用`-cp`参数指定包的路径，否则在执行java命令时，会报找不到类的错误。例如使用了ASM，必须`-cp xx/xx/xx/asm.jar;`，千万不要忘记+`;`**


- **这里有一个问题就是：我实现了一个例子，在transform中对空参数的构造方法添加了打印当前时间戳的语句。但是在执行`java -javaagent.....`指令之后，并没有打印出 时间戳。但是如果 将打印时间戳的代码加入到`main`方法中是可以打印的。这里是有疑惑的地方！！！**


## 2.2 Java SE6
- 在Jave SE6时代，Instrumentation包含了俩种Instrumentation方式,`premain(命令行)`和`agentmain(运行时)`

- 在实际的运行之中，**虚拟机在初始化之时（在绝大多数的 Java 类库被载入之前）**，`instrumentation`的设置已经启动，并在虚拟机中设置了回调函数，检测特定类的加载情况，并完成实际工作。也就是说，在`Java SE5`中，只能使用premain的方式实现Instrumentation。但是在实际的很多的情况下，没有办法在虚拟机启动之时就为其设定代理，这样实际上限制了instrument的应用

- `Java SE6`提供了`agentmain`和`Java Tool API`中的`attach`方式，可以在运行过程中动态的设置加载代理类，达到`Intrumentation`目的

### 2.2.1 关于`agentmain`
- 在`Jave SE6`的`Intrumentation`中，存在一个跟`premain`相似的方法，不同的是`agentmain`可以在一个应用程序的`main`函数开始运行之后再运行

- 与`premain`一样，需要编写一个含有`agentmain`函数的java类

		public static void agentmain(String agentArgs, Instrumentation inst);          [1] 
		public static void agentmain(String agentArgs);              [2]

	**注意:[1]比[2]优先级高，会被优先执行**

- `agentmain`和`premain`中的参数是相同的，用法也相同，都在当前方法中添加`ClassFileTransform`实现代码转换

- 与`premain`一样，必须在`manifest`中设置`"Agent-Class"`来指定包含`agentmain`函数的类

- 跟`premain`不同的是，`agentmain`需要在`main`函数开始运行后才启动，这样的时机应该如何确定呢?可以在`Java SE6`的新特性里面提供的`Attach API`找到`agentmain`具体使用的例子

### 2.2.2 关于Agent-API
- [Agent API](http://docs.oracle.com/javase/7/docs/jdk/api/attach/spec/index.html)**不是java标准API**，而是sun公司提供的一套扩展API，用来向目标**JVM“附着”（Attach）代理工具程序**。

- [Attach API Maven库地址](https://mvnrepository.com/artifact/com.sun/tools/1.5.0)

- `Attach API`很简单，只有2个主要的类，都在`com.sun.tools.attach`包里面：`VirtualMachine`代表一个Java虚拟机，也就是程序需要监控的目标虚拟机，提供了JVM枚举，Attach动作和Detach动作（Attach 动作的相反行为，从JVM 上面解除一个代理）等等 ; `VirtualMachineDescriptor`则是一个描述虚拟机的容器类，配合VirtualMachine类完成各种功能。

### 2.2.3 agentmain实现步骤
- 与`premain`相似，`agentmain`方式同样需要提供一个`agent.jar`，并且这个jar包需要满足:
	- 在`manifest`中指定`Agent-Class`属性，值为代理类全路径
	- 代理类需要提供`public static void agentmain(String args,Instrumentation inst)`或`public static void agentmain(String args)`.俩者同时存在时，前者优先

- `Agent API`中的`VirtualMachine`代理一个运行中的VM，其提供了`loadAgent()`方法，可以在运行时动态加载一个代理jar，这样就实现了`premain`的效果

- `premain`是通过命令行参数 或 系统参数 指定的agent.jar.`agentmain`是通过`Attach API`指定的jar

### 2.2.4 agentmain 实例（命令行）

1. 编写包含`agentmain`方法的agent.jar.与包含`premain`方法的agent.jar相似。需要提供MANIFEST.MF,一个包含`agentmain`的类,自定义的`ClassFileTransformer`.并打成jar包(**注意：要将三方jar包字节码也加入jar包，还需要指定MANIFEST.MF**)。

	    public static void agentmain(String args, Instrumentation inst) {
			inst.addTransformer(new MyClassTransformer(), true);
		}
		//提供一个打包的task..出错了不管~
		task makeJar(type: Jar) {
		    manifest{
		        from('/src/main/resources/META-INF/MANIFEST.MF')
		    }
		    baseName = 'apmagent'
		    from zipTree('/libs/asm-all-5.2.jar')
		    from '/build/classes/java/main'
		    into '/'
		}

2. 编写一个**被修改的类**，提供一个`test()`方法即可,step1中的自定义`ClassFileTransformer`也只是对test方法进行修改，插入输出时间戳的字节码。

		public class ModifedClass {
		    public void test() {
		        System.out.println("i am in modifedClass");
		    }
		}

3. 编写一个长时间运行的类，里面会创建并调用`被修改的类的test方法`。可以在这个类中输出当前`jvm的pid`,也可以通过`jps`指令在cmd中查看!(JPS这个命令是和javac 同一个目录下的)

4. 编写一个`Attach API`的使用类，用作连接step3 中的类并`loadAgent`

		***省略若干代码***
		 System.setProperty("java.library.path",
		                            "C:\\Program Files\\Java\\jdk1.8.0_112\\jre\\bin");
		                    Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		                    fieldSysPath.setAccessible(true);
		                    fieldSysPath.set(null, null);
			
		                    String pid = input.split("-")[1];
		                    int indexOf = pid.indexOf('@');
		                    if (indexOf > 0) {
		                        pid = pid.substring(0, indexOf);
		                    }
		                    System.out.println("pid = " + pid);
		
		                    // attach to target VM
		                    VirtualMachine vm = VirtualMachine.attach(pid);
		
		                    // get system properties in target VM
		                    Properties props = vm.getSystemProperties();
		
		                    // construct path to management agent
		                    String agent = "C:\\Users\\Administrator\\Documents\\CustomizePluginDemo\\agenttest\\libs\\apmagent.jar";
		
		                    // load agent into target VM
		                    vm.loadAgent(agent);
		
		                    // detach
		                    vm.detach();

	- 代码前四句是必须的！否则会报如下错误

			java.util.ServiceConfigurationError: com.sun.tools.attach.spi.AttachProvider: Provider sun.tools.attach.WindowsAttachProvider could not be instantiated: java.lang.UnsatisfiedLinkError: no attach in java.library.path
			Exception in thread "main" com.sun.tools.attach.AttachNotSupportedException: no providers installed
				at com.sun.tools.attach.VirtualMachine.attach(VirtualMachine.java:208)
				at client.Client.main(Client.java:29)

5. 分别编译运行俩个类即可。

- **被动态修改的类**，不能跟**长时间运行的类**放在一起,否则会报错

		java.lang.reflect.InvocationTargetException
		at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
		at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
		at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
		at java.lang.reflect.Method.invoke(Unknown Source)
		at sun.instrument.InstrumentationImpl.loadClassAndStartAgent(Unknown Source)
		at sun.instrument.InstrumentationImpl.loadClassAndCallAgentmain(Unknown Source)
		Caused by: java.lang.UnsupportedOperationException: class redefinition failed: attempted to add a method
		at sun.instrument.InstrumentationImpl.retransformClasses0(Native Method)
		at sun.instrument.InstrumentationImpl.retransformClasses(Unknown Source)
		at io.fengfu.learning.instrument.DynamicAgent.agentmain(DynamicAgent.java:14)
		... 6 more

### 2.3.5 MANIFEST.MF中的属性

- Premain-Class: 当在VM启动时，在命令行中指定代理jar时，必须在manifest中设置Premain-Class属性，值为代理类全类名，并且该代理类必须提供premain方法。否则JVM会异常终止。

- Agent-Class: 当在VM启动之后，动态添加代理jar包时，代理jar包中manifest必须设置Agent-Class属性，值为代理类全类名，并且该代理类必须提供agentmain方法，否则无法启动该代理。

- Boot-Class-Path: Bootstrap class loader加载类时的搜索路径，可选。

- Can-Redefine-Classes: true/false；标示代理类是否能够重定义类。可选。

- Can-Retransform-Classes: true/false；标示代理类是否能够转换类定义。可选。

- Can-Set-Native-Prefix::true/false；标示代理类是否需要本地方法前缀，可选。