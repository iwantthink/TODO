# Java静态代理和动态代理

[Java静态代理和动态代理](https://blog.csdn.net/giserstone/article/details/17199755)
# 1. 简介

**代理的概念：**
- 为某个对象提供一个代理，以控制这个对象的访问。代理类和委托类有共同的父类或父接口，这样任何使用委托类对象的地方都可以被替换成代理对象。代理类负责请求的预处理，过滤，将请求分派给委托类处理，以及委托类执行完请求后的后续处理。


![](http://dl.iteye.com/upload/attachment/562226/cd337e41-6ee8-3619-a1c4-a2c096fb711c.jpg)

**根据代理类的生成时间不同可以将代理分为 静态代理 和 动态代理**

- 静态代理是在运行之前就已经存在的，动态代理是运行之后才出现的

**代理模式一般涉及到的角色有：**

- 抽象角色：声明真实对象和代理对象的共同接口，对应代理接口（Subject）；

- 真实角色：代理角色所代表的真实对象，是我们最终要引用的对象，对应委托类（RealSubject）；

- 代理角色：代理对象角色内部含有对真实对象的引用，从而可以操作真实对象，同时代理对象提供与真实对象相同的接口以便在任何时刻都能代替真实对象。同时，代理对象可以在执行真实对象操作时，附加其他的操作，相当于对真实对象进行封装，对应代理类（ProxySubject）

# 2. 静态代理

**所谓的静态代理，也就是在程序运行之前就已经存在代理类的字节码文件，代理类和委托类的关系在运行之前就已经确定**

## 2.1 代理接口

	/**  
	 * 代理接口。处理给定名字的任务。 
	 */  
	public interface Subject {  
	  /** 
	   * 执行给定名字的任务。 
	    * @param taskName 任务名 
	   */  
	   public void dealTask(String taskName);   
	}  

## 2.2 委托类,具体处理业务的类

	/** 
	 * 真正执行任务的类，实现了代理接口。 
	 */  
	public class RealSubject implements Subject {  
	  
	 /** 
	  * 执行给定名字的任务。这里打印出任务名，并休眠500ms模拟任务执行了很长时间 
	  * @param taskName  
	  */  
	   @Override  
	   public void dealTask(String taskName) {  
	      System.out.println("正在执行任务："+taskName);  
	      try {  
	         Thread.sleep(500);  
	      } catch (InterruptedException e) {  
	         e.printStackTrace();  
	      }  
	   }  
	}

## 2.3 静态代理类

	/** 
	 *　代理类，实现了代理接口。 
	 */  
	public class ProxySubject implements Subject {  
	 //代理类持有一个委托类的对象引用  
	 private Subject delegate;  
	   
	 public ProxySubject(Subject delegate) {  
	  this.delegate = delegate;  
	 }  
	  
	 /** 
	  * 将请求分派给委托类执行，记录任务执行前后的时间，时间差即为任务的处理时间 
	  *  
	  * @param taskName 
	  */  
	 @Override  
	 public void dealTask(String taskName) {  
	  long stime = System.currentTimeMillis();   
	  //将请求分派给委托类处理  
	  delegate.dealTask(taskName);  
	  long ftime = System.currentTimeMillis();   
	  System.out.println("执行任务耗时"+(ftime - stime)+"毫秒");  
	    
	 }  
	}  

## 2.4 客户类,调用业务的类

	public class Client{
	
		public static void main(String [] args){
			RealSubject realSub = new RealSubject();
			ProxySubject proxy = new ProxySubject(realSub);
			proxy.dealTask("TASK-A")
		}
	}

## 2.5 静态代理总结

优点：

- 业务类只需要关注业务逻辑本身，保证业务类的重用性

缺点：

- 代理对象的一个接口只服务于一种类型的对象，如果代理的方法很多，就需要创建多个代理对象，那么在程序规模大的时候就会造成类的数量大量增多

- 如果代理接口 增加了一个方法，除了所有实现类需要实现这个方法，所有的代理类也需要实现此方法，增加了代码维护的复杂度

- 必须事先知道 委托类是什么，才能创建代理类


# 3. 动态代理

**动态代理类的源码是程序运行期间由JVM根据反射等机制动态的生成，所以不存在代理类的字节码文件。代理类和委托类的关系是在程序运行时确定。**

## 3.1 java.lang.reflect.Proxy
**Proxy中的静态公开方法：**


	// 方法 1: 该方法用于获取指定代理对象所关联的调用处理器  
	static InvocationHandler getInvocationHandler(Object proxy)   
	  
	// 方法 2：该方法用于获取关联于指定类装载器和一组接口的动态代理类的类对象  
	static Class getProxyClass(ClassLoader loader, Class[] interfaces)   
	  
	// 方法 3：该方法用于判断指定类对象是否是一个动态代理类  
	static boolean isProxyClass(Class cl)   
	  
	// 方法 4：该方法用于为指定类装载器、一组接口及调用处理器生成动态代理类实例  
	static Object newProxyInstance(ClassLoader loader, Class[] interfaces, InvocationHandler h)  


## 3.2 java.lang.reflect.InvocationHandler

这是调用处理器接口，有一个invoke方法，用于集中处理在动态代理类对象上的方法调用，通常在该方法中实现对委托类的代理访问，每次生成动态代理类对象时都要指定一个对应的调用处理器对象

	public interface InvocationHandler {
		// 该方法负责集中处理动态代理类上的所有方法调用。第一个参数既是代理类实例，第二个参数是被调用的方法对象  
		// 第三个方法是调用参数。调用处理器根据这三个参数进行预处理或分派到委托类实例上反射执行  
	    public Object invoke(Object proxy, Method method, Object[] args)
	        throws Throwable;
	}

## 3.3 java.lang.ClassLoader

类加载器类，负责将类的字节码加载到JVM中并为其定义类对象，然后该类才能被使用。Proxy静态方法生成动态代理类同样需要通过类加载器来进行加载才能使用，它与普通类的唯一区别是其字节码是由JVM在运行时动态生成的而非预存在.class文件中。

每次生成动态代理类对象都需要指定一个类加载器对象

## 3.4 动态代理具体步骤

1. 实现InvocationHandler接口创建自己的调用处理器

2. 给Proxy类提供ClassLoader和代理接口类型数组创建动态代理类

3. 以调用处理器类型为参数，利用反射机制得到动态代理类的构造函数

4. 以调用处理器对象为参数，利用动态代理类的构造函数创建动态代理类对象

**分步骤实现动态代理：**

	// InvocationHandlerImpl 实现了 InvocationHandler 接口，并能实现方法调用从代理类到委托类的分派转发  
	// 其内部通常包含指向委托类实例的引用，用于真正执行分派转发过来的方法调用  
	InvocationHandler handler = new InvocationHandlerImpl(..);   
	  
	// 通过 Proxy 为包括 Interface 接口在内的一组接口动态创建代理类的类对象  
	Class clazz = Proxy.getProxyClass(classLoader, new Class[] { Interface.class, ... });   
	  
	// 通过反射从生成的类对象获得构造函数对象  
	Constructor constructor = clazz.getConstructor(new Class[] { InvocationHandler.class });   
	  
	// 通过构造函数对象创建动态代理类实例  
	Interface Proxy = (Interface)constructor.newInstance(new Object[] { handler });  

**简化的动态代理实现：**

	// InvocationHandlerImpl 实现了 InvocationHandler 接口，并能实现方法调用从代理类到委托类的分派转发  
	InvocationHandler handler = new InvocationHandlerImpl(..);   
	  
	// 通过 Proxy 直接创建动态代理类实例  
	Interface proxy = (Interface)Proxy.newProxyInstance( classLoader,   
	     new Class[] { Interface.class },  handler );   

### 3.4.1 创建调用处理器

	/** 
	 * 动态代理类对应的调用处理程序类 
	 */  
	public class SubjectInvocationHandler implements InvocationHandler {  
	   
		 //代理类持有一个委托类的对象引用  
		 private Object delegate;  
		   
		 public SubjectInvocationHandler(Object delegate) {  
		  this.delegate = delegate;  
		 }  
	   
		 @Override  
		 public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {  
		  long stime = System.currentTimeMillis();   
		  //利用反射机制将请求分派给委托类处理。Method的invoke返回Object对象作为方法执行结果。  
		  //因为示例程序没有返回值，所以这里忽略了返回值处理  
		  method.invoke(delegate, args);  
		  long ftime = System.currentTimeMillis();   
		  System.out.println("执行任务耗时"+(ftime - stime)+"毫秒");  
		    
		  return null;  
		 }  
	}  

### 3.4.2 动态代理客户类

	 Subject delegate = new RealSubject();  
	 InvocationHandler handler = new SubjectInvocationHandler(delegate);  
	 Subject proxy = null;  
	 proxy = (Subject)Proxy.newProxyInstance(  
	    delegate.getClass().getClassLoader(),   
	    delegate.getClass().getInterfaces(),   
	    handler);  

## 3.5 动态代理总结

动态代理类是 public,final,非抽象类型的

动态代理类继承了 java.lang,reflect.Proxy类

动态代理类的名字以 `$Proxy`开头

动态代理类实现`getProxyClass()`和`newProxyInstance()`方法的参数interfaces指定的所有接口

动态代理类都具有一个public 类型的构造方法，该构造方法有一个InvocationHandler 类型的参数。

动态代理无法摆脱仅支持Interface代理的桎梏

