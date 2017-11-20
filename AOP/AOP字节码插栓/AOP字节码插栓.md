# 1.Android AOP 字节码插栓介绍

# 1.1 简介
**面向切向编程**（Aspect Oriented Programming），相对于**面向对象编程**（Object Oriented Programming）而言。      

OOP是将功能模块化或者问题模块化,AOP就是将涉及到众多模块的某一类问题进行统一管理


## 1.1 方式介绍

### 1.1.1 AOP 按实现原理可以分为 运行时AOP和编译时AOP
- Android运行时AOP：主要实现通过hook某些关键方法
- Android编译时AOP：主要是在apk打包过程中对class文件的字节码进行识别并更改


### 1.1.2 主流的Anrdoid AOP 框架有：  

1. Dexposed，Xposed等（运行时）

2. [aspactJ（编译时）](http://blog.csdn.net/innost/article/details/49387395)

还有一些非框架的但能实现 AOP的工具类库：

1. java的动态代理机制(对java接口有效)
2. ASM,javassit等字节码操作类库
3. (偏方)DexMaker:Dalvik 虚拟机上，在编译期或者运行时生成代码的 Java API。
4. (偏方)ASMDEX(一个类似 ASM 的字节码操作库，运行在Android平台，操作Dex字节码)

### 1.1.3 各种方式的选择
- Dexposed只针对部分系统版本有效
- Xposed需要机器拥有root权限
- aspactJ没有上述缺点，但是其作为一个AOP框架太大了，不仅方法多，还有一堆引用需要添加


**AOP方法可以确定为俩点：  **
1. 采用编译时的字节码操作的做法
2. hook Android编译打包流程并借助ASM库对项目字节码文件进行统一扫描，过滤以及修改


# 2.AOP实现概述
![AOP大概流程](http://ww1.sinaimg.cn/large/6ab93b35gy1fjhx4ub6grj20o004dt8v.jpg)

关键点在如下俩点：  
1. 字节码插栓入口(step1,step3)
	Android程序从java源代码到可执行的apk,中间包括（不止有）俩个环节,javac和dex ,所以想对字节码进行修改，只需要在javac之后，dex之前对class文件进行字节码扫描，并按照一定规则进行过滤和修改，这样修改过后的字节码就会继续打包流程，并生成apk。
	- javac:将源文件编译成class格式的文件
	- dex：将class格式的文件汇总到dex格式的文件中   
	
2. bytecode mainpulate(step2)
	这个环节主要做俩点：1. 字节码扫描,并按照一定规则进行过滤出哪些class文件需要进行字节码修改。2. 对筛选出来的类进行字节码修改  。 


## 2.1 插栓入口
### 2.1.1 Android打包流程说明
![Android打包流程](http://ww1.sinaimg.cn/large/6ab93b35gy1fjhxdoz9mlj20f00nuwgw.jpg)

- 图中"dex"节点，表示将class文件打包到dex文件的过程，输入的class文件包括：1. 项目java源文件经过javac后编译生成的class文件 2. 第三方依赖包的class文件


- 图中"dex"节点，通过一个dx.jar的jar包进行，存在于Android SDK 的 **sdk/build-tools/22.0.1/lib/dx.jar 目录下**，通过如下命令，进行将class文件打包为dex文件。  
		java dx.jar com.android.dx.command.Main --dex --num-threads=4 —-output output.jar input.jar

- dex任务是启动一个java进程,执行dx.jar中com.android.dx.command.Main类(对于multidex的项目入口可能不是这个类)的main方法进行dex相关逻辑，具体完成class到dex转换的是如下方法：  
		private static boolean processClass(String name,byte[] bytes) {
      	//内容省略
		}
	方法processClass的第二个参数是一个byte[],这就是class文件的二进制数据(class文件是一种紧凑的8位字节的二进制文件，各个数据项按顺序紧密的从前向后排列，相领的项[包括字节码指令]之间没有间隙)，我们通过对这个二进制数据进行扫描，按照一定规则过滤以及字节码修改达到AOP效果

### 2.1.2 如何获取插栓入口

- 入口1:tranform api
	对于Android Gradle Plugin版本在1.5.0 及以上的情况，Google提供了transformapi用作字节码插栓的入口。此处的Android Gradle Plugin 版本指的是build.gradle dependencies的配置
		compile 'com.android.tools.build:gradle:1.5.0'
	关于transform api: 1.[tranform api介绍](https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/transform-api)   2.[Gradle Plugin1.5Nuwa插件-使用tranform api 实现](http://blog.csdn.net/sbsujjbcy/article/details/50839263)


- 入口2：hook dx.jar
	入口1是针对Android Build Gradle Plugin >1.5.0的情况
	该方法是不依赖transform api 获得插栓入口
	>提示：具体使用可以考虑综合这两种方式，首先检查build环境是否支持transform api（反射检查类com.android.build.gradle.BaseExtension是否有registerTransform这个方法即可）然后决定使用哪种方式的插桩入口。


### 2.1.3 hook dx.jar 获取插栓入口
具体实现就是将dx.jar 中的com.android.Main.processClass 字节码进行修改，修改成如下：  
	
	private static boolean processClass(String name,byte[] bytes) {

 	 	bytes＝扫描并修改（bytes）；// Hook点

  		//原有逻辑省略

	}

可以参考 [APM之原理篇](http://blog.csdn.net/sgwhp/article/details/50239747)


dex任务是启动一个java进程,执行dx.jar中com.android.dx.command.Main类(对于multidex的项目入口可能不是这个类)的main方法进行dex相关逻辑.那么问题是如何在一个标准的java进程中对特定方法进行字节码插栓呢？？？ **这里需要使用java 1.5 引入的instrumentation机制**

#### 2.1.3.1 Java Instrumentation介绍
Java Instrumentation指的是可以独立于应用程序之外的代理(agent)程序来监测和协助运行在JVM上的应用程序。这种监测和协助包括但不限于获取JVM运行时状态，替换和修改类定义等

- instrumentation最大作用就是类定义的动态改变和操作

#### 2.1.3.2 Java Instrumentation 使用方式

- 方式一(java 1.5+):
	开发者可以在一个普通java程序(带有main函数的java类)运行时，通过-javaagent参数指定一个特定的jar文件(agent.jar)(包含instrumentation代理)来启动Instrumentation的代理程序。例如：  
		 java -javaagent agent.jar  dex.jar  com.android.dx.command.Main  --dex …........
	这样，在目标main函数执行之前，执行agent.jar包指定类的premain 方法：  
		premain(String args, Instrumentation inst)


- 方式二(java 1.6+):  
		VirtualMachine.loadAgent(agent.jar)
		VirtualMachine vm = VirtualMachine.attach(pid);
		vm.loadAgent(jarFilePath, args);
	此时将会执行agent.jar包指定类的agentmain方法：  
		agentmain(String args, Instrumentation inst)

	- 关于agent.jar
		这里的agent.jar就是一个包含一些指定信息的jar包，就像OSGI的插件jar包一样，在jar包的META-INF/MANIFEST.MF中添加如下信息
			Manifest-Version: 1.0
			Agent-Class: XXXXX
			Premain-Class: XXXXX
			Can-Redefine-Classes: true
			Can-Retransform-Classes: true
		这个jar包就成了agent.jar包，其中Agent-Class指向具有agentmain(String args,Instrumentation inst)方法的类，Premain-Class 指向具有premain(String args,Instrumentation inst)的类
	- 关于premain(String args,Instrumentation inst)
		第二个参数,Instrumentation类有个方法
			addTransformer(ClassFileTransformer transformer,boolean canRetransform)
		而一旦为Instrumentation inst添加了ClassFileTransformer:
			ClassFileTransformer c=new ClassFileTransformer()
			inst.addTransformer(c,true);
		那么以后这个jvm进程中再有**任何类的加载定义**，都会触发此ClassFileTransformer方法
			byte[] transform(  ClassLoader loader,String className,Class classBeingRedefined,ProtectionDomain protectionDomain,byte[] classfileBuffer)throwsIllegalClassFormatException;
		其中,参数byte[] classFileBuffer 是类的class文件数据，对它进行修改就可以实现 在一个标准的java进程中对特定方法进行字节码插栓的目的
		


### 2.1.3.3 hook dx.jar获得插栓入口的完整流程
![](http://ww1.sinaimg.cn/large/6ab93b35gy1fji3pd5ochj20wd0mwt9v.jpg)

- apply plugin: 'bytecodeplugin',作用就是引入我们用于字节码插栓的gradle插件

通过任意方式(AS界面内点击/命令gradle build等)都会启动如图所描述的build流程

通过Java Instrumentation机制，为获得插栓入口，对于apk build过程进行了俩处插栓(即hook)

图中左侧build进程使用Instrumentation的方式时之前叙述过 VirtualMachine.loadAgent方式（即我们之前说的方式二），dex进程中的方式则是 -javaagent agent.jar方式(即方式一)

图中红色部分：  

- 在build过程中
	对ProcessBuilder.start()方法进行插栓，**ProcessBuilder类是J2SE 1.5 在java.lang 中新添加的一个新类，此类用于创建操作系统进程，它提供了一种启动和管理进程的方法，start方法就是开始创建一个进程**，对它进行插栓，使得通过下面方式启动dx.jar进程执行dex任务时:
		java  dex.jar  com.android.dx.command.Main  --dex …........
	增加参数-javaagent agent.jar ,使得dex进程也可以使用Java Instrumentation机制进行字节码插栓


- 在dex过程
	对我们的目标方法com.android.dx.command.Main.processClasses进行字节码插入，从而实现打入apk的每一个项目中的类都按照我们制定的规则进行过滤及字节码修改。



# 3 参考链接
[Android aop之字节码插栓](http://www.jianshu.com/p/c202853059b4)
