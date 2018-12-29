# ClassLoader
[热修复入门：Andoird中的ClassLoader第一篇](https://jaeger.itscoder.com/android/2016/08/27/android-classloader.html)

[深度分析ClassLoader(源码级别)](http://www.hollischuang.com/archives/199)

[深入分析Java ClassLoader原理](http://blog.csdn.net/xyang81/article/details/7292380)

[oracle-docs-ClassLoader-loadClass](https://docs.oracle.com/javase/7/docs/api/java/lang/ClassLoader.html#loadClass(java.lang.String)

[超详细java中的classloader详解](http://blog.csdn.net/briblue/article/details/54973413)

[Android BaseDexClassLoader 5.0 源码](https://android.googlesource.com/platform/libcore/+/lollipop-release/dalvik/src/main/java/dalvik/system/)

[Android解析ClassLoader（一）Java中的ClassLoader](http://blog.csdn.net/itachi85/article/details/78088701)

[Android类加载器分析](http://tech.lede.com/2017/02/22/rd/android/android_classLoader/) 

[类的加载机制](https://blog.csdn.net/u012152619/article/details/46964775)

[【深入Java虚拟机】之四：类加载机制](https://blog.csdn.net/ns_code/article/details/17881581)

**强烈建议安装chrome插件 Android SDK Search!!!**
**使用方式：地址栏输入ad 然后按TAB**

# 1.ClassLoader简介
ClassLoader就是类加载器，具体作用就是将class文件加载到jvm虚拟机中，在jvm启动的时候，并不会一次性加载所有的class文件，而是根据需要去**动态加载**到内存。

简单来说ClassLoader就是加载class文件，提供给程序运行时使用。

## 1.1 默认的ClassLoader

Java提供俩种类型的类加载器(以JVM的角度)：启动类加载器(使用c++实现,是虚拟机的一部分)和所有其他的类加载器(全部由Java实现,独立于JVM,全部继承自`java.lang.ClassLoader`,且都是在启动类加载器之后加载的)。

**如果以Java开发人员的角度,Java类加载器**可以分为三种ClassLoader类型:

### 1.1.1 BootStrap ClassLoader

**BootStrap ClassLoader：**称为 引导类加载器，是java类加载层次中最顶层的类加载器，负责加载JDK中的核心类库，如：rt.jar,resources.jar,charsets.jar等。默认加载路径在`/JAVA_HOME/jre/lib` 目录下，也可以通过启动Java虚拟机时指定`-Xbootclasspath/a:path`选项，来改变Bootstrap ClassLoader的加载目录。

~~**`BootStrap ClassLoader`存在于Launcher中**~~

Java虚拟机的启动就是通过 `Bootstrap ClassLoader`创建一个初始类来完成的。**由于Bootstrap ClassLoader是使用C/C++语言实现的， 所以该加载器不能被Java代码访问到**。

**需要注意的是Bootstrap ClassLoader并不继承java.lang.ClassLoader**。 

可以通过`System.getProperty("sun.boot.class.path")`方法来获取BootStrap ClassLoader所加载的目录(JVM中运行才有效),打印结果如下：

	C:\Program Files\Java\jdk1.8.0_102\jre\lib\resources.jar;
	C:\Program Files\Java\jdk1.8.0_102\jre\lib\rt.jar;
	C:\Program Files\Java\jdk1.8.0_102\jre\lib\sunrsasign.jar;
	C:\Program Files\Java\jdk1.8.0_102\jre\lib\jsse.jar;
	C:\Program Files\Java\jdk1.8.0_102\jre\lib\jce.jar;
	C:\Program Files\Java\jdk1.8.0_102\jre\lib\charsets.jar;
	C:\Program Files\Java\jdk1.8.0_102\jre\lib\jfr.jar;
	C:\Program Files\Java\jdk1.8.0_102\jre\classes

### 1.1.2 Extension ClassLoader
**Extension ClassLoader**:称为扩展类加载器，负责加载java的扩展类库，默认加载`JAVA_HOME/JRE/LIB/EXT`目录下的所有jar.可以通过`-Djava.ext.dirs`选项添加和修改Extensions ClassLoader加载的路径。 存在于Launcher中。

可以通过`System.out.println(System.getProperty("java.ext.dirs"))`方法来获取Extensions ClassLoader所加载的目录（JVM中运行才有效），打印结果如下：

	C:\Program Files\Java\jre1.8.0_151\lib\ext;
	C:\WINDOWS\Sun\Java\lib\ext


### 1.1.3 App ClassLoader

**App ClassLoader**:称为系统类加载器，**负责加载应用程序classPath目录下的所有jar和Class文件**。

可以加载通过`-Djava.class.path`选项所指定的目录下的jar和Class文件

**一般情况下,这个就是程序中默认的类加载器**



**注意：**
> 除了Java默认提供的三个ClassLoader之外，用户还可以根据需要定义自已的ClassLoader，而这些自定义的ClassLoader都必须继承自java.lang.ClassLoader类，也包括Java提供的另外二个ClassLoader（Extension ClassLoader和App ClassLoader）在内，但是Bootstrap ClassLoader不继承自ClassLoader，因为它不是一个普通的Java类，底层由C++编写，已嵌入到了JVM内核当中，当JVM启动后，Bootstrap ClassLoader也随着启动，负责加载完核心类库后，并构造Extension ClassLoader和App ClassLoader类加载器。

### 1.2 Java ClassLoader的继承关系

![](http://upload-images.jianshu.io/upload_images/1417629-bff51289538f3222.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 默认系统提供的ClassLoader有三种类型，但是系统提供的ClassLoader相关类不止三个

- ClassLoader是一个抽象类，其中定义了ClassLoader的主要功能。

- SecureClassLoader继承了抽象类ClassLoader，但SecureClassLoader并不是ClassLoader的实现类，而是拓展了ClassLoader类加入了权限方面的功能，加强了ClassLoader的安全性。

- URLClassLoader继承自SecureClassLoader，用来通过URl路径从jar文件和文件夹中加载类和资源。

- ExtClassLoader和AppClassLoader都继承自URLClassLoader，**它们都是Launcher 的内部类，Launcher 是Java虚拟机的入口应用，ExtClassLoader和AppClassLoader都是在Launcher中进行初始化的。**

## 1.3 双亲委托模型(Parent Delegation Model)

### 1.3.1 原理介绍

`ClassLoader`使用的双亲委托模式来搜索类，每个`ClassLoader `实例都有一个父类加载的引用(不是继承关系，是一个包含关系),虚拟机内置的类加载器(Bootstrap ClassLoader)本身没有父类加载器，但是可以作为其它ClassLoader实例的父类加载器。

    protected ClassLoader(ClassLoader parent) {
        this(checkCreateClassLoader(), parent);
    }

    protected ClassLoader() {
        this(checkCreateClassLoader(), getSystemClassLoader());
    }

	private ClassLoader(Void unused, ClassLoader parent) {
		this.parent = parent;
	}


- 当类加载器收到加载类或资源的请求时，通常都是先委托给父类加载器，也就是说只有当父类加载器找不到指定类或资源时，自身才会执行实际的类加载过程，具体的加载过程如下：

	1. 源ClassLoader先判断该Class 是否已经加载，如果已经加载，则直接返回Class，如果没有则委托给父类加载器。
	
	2. 父类加载器判断是否加载过该Class，如果已经加载，则直接返回Class，如果没有则委托给祖父类加载器
	
	3. 以此类推，直到始祖类加载器(引导类加载器)
	
	4. 始祖类加载器判断是否加载过该Class，如果已经加载，则直接返回Class。如果没有则尝试从其对应的类路径下寻找Class字节码文件并载入。如果载入成功，则直接返回Class，如果载入失败，则委托给始祖类加载器的子类加载器
	
	5. 以此类推，一级一级往下直到源ClassLoader
	
	6. 源ClassLoader尝试从其对应的类路径下寻找Class字节码文件并载入。如果载入成功，则直接返回Class，**如果载入失败，源ClassLoader不会再委托其子类加载器**，而是抛出异常(ClassNotFoundException)。

![](http://hi.csdn.net/attachment/201202/25/0_13301699801ybH.gif)

### 1.3.2 为什么使用双亲委托模型？
- 可以避免重复加载， 当父类已经加载了该类的时候，就没有必要子ClassLoader再加载一次

- 更加安全。防止自定义的类替代掉系统的类.

	**举个栗子**：如果不使用双亲委托模型，完全可以自定义一个类加载器去加载自定义的String类来动态替换掉Java核心api中定义的String类。

### 1.3.3 JVM如何判定俩个class是相同的？

**JVM在判定俩个class是否相同时，不仅会判断俩个类的类名是否相同，而且会判断是否由同一个类加载器实例加载的。只有俩者都符合时，JVM才认为这俩个class是相同的**

**举个栗子**：就算两个class是同一份class字节码，如果被两个不同的ClassLoader实例所加载，JVM也会认为它们是两个不同class。比如网络上的一个Java类`org.classloader.simple.NetClassLoaderSimple`，javac编译之后生成字节码文件NetClassLoaderSimple.class，ClassLoaderA和ClassLoaderB这两个类加载器并读取了NetClassLoaderSimple.class文件，并分别定义出了java.lang.Class实例来表示这个类，对于JVM来说，它们是两个不同的实例对象，但它们确实是同一份字节码文件，如果试图将这个Class实例生成具体的对象进行转换时，就会抛运行时异常java.lang.ClassCaseException，提示这是两个不同的类型。

### 1.3.4 Java ClassLoader-loadClass()
**类加载步骤在JDK8源码中的体现：**

	 protected Class<?> loadClass(String var1, boolean var2) throws ClassNotFoundException {
	        synchronized(this.getClassLoadingLock(var1)) {
				// 查找是否存在已经加载的类
	            Class var4 = this.findLoadedClass(var1);//1
				// 如果已经存在 已经加载的类,那么就会直接返回了
	            if(var4 == null) {
	                long var5 = System.nanoTime();
	
	                try {
						// 通过父类加载器 去查找
	                    if(this.parent != null) {
	                        var4 = this.parent.loadClass(var1, false);//2
	                    } else {
							// 直接通过 BootStrapClassLoader加载
	                        var4 = this.findBootstrapClassOrNull(var1);//3
	                    }
	                } catch (ClassNotFoundException var10) {
	                    ;
	                }

					// 一直回溯到BootStrap ClassLoader 都未能找到	
	                if(var4 == null) {
	                    long var7 = System.nanoTime();
					
	                    var4 = this.findClass(var1);//4
	                    PerfCounter.getParentDelegationTime().addTime(var7 - var5);
	                    PerfCounter.getFindClassTime().addElapsedTimeFrom(var7);
	                    PerfCounter.getFindClasses().increment();
	                }
	            }
	
	            if(var2) {
	                this.resolveClass(var4);
	            }
	
	            return var4;
	        }
	    }

- 注释1处会检查类是否已经加载,如果已经加载则后面的代码不会执行，最后会返回该类。没有加载则会继续执行

- 注释2处会判断如果父类加载器不为null，则调用父类加载器的loadClass()方法。

- 如果父类加载器null，会调用注释3处的 `findBootstrapClassOrNull()`方法。这个方法内部调用了Native方法`findLoadedClass0()`,其最终会用`Bootstrap ClassLoader `来查找类。如果`Bootstrap ClassLoader `也没有找到该类，则会调用注释4处的`findClass()`方法继续逐级向下进行查找。

## 1.4 类的加载过程

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

- 该方法需要交给ClassLoader的实现类去实现

# 2 自定义ClassLoader

**JVM已经提供了默认的类加载器，为什么还需要自定义类加载器？**
- 因为java中提供的默认ClassLoader，只加载指定目录下的jar和class，如果想加载其他位置的类或jar时，就需要用到自定义类加载器。
- 举个栗子：加载网络上的一个class文件，通过动态加载到内存之后，要调用这个类中的方法。

## 2.1 自定义类加载器的步骤 

1. 继承java.lang.ClassLoader

2. 重写父类的findClass()方法。
	- 为何仅重写findClass？
	
		因为JDK在loadClass方法中已经实现了ClassLoader搜索类的算法，当loadClass中搜索不到类时，loadClass就会调用findClass方法来搜索类。

## 2.2 loadClass方法

	protected Class<?> loadClass(String name,
	                 boolean resolve)
	                      throws ClassNotFoundException{
		.............
	}

- 使用指定的** 二进制名称 **加载时，此方法默认实现按以下顺序搜索类

	1. 调用`findLoadedClass(String)`检查类是否已经加载,如果已经加载,则直接返回

	2. 调用父类加载器上的`loadClass()`方法。如果父类加载器为空，则使用jvm内置的类加载器(Bootstrap ClassLoader)

	3. 已经回溯到根类加载器仍未找到,那么调用`findClass(String)`来执行具体的加载类的逻辑

- 二进制名称示例：

	   "java.lang.String"
	   "javax.swing.JSpinner$DefaultEditor"
	   "java.security.KeyStore$Builder$FileBuilder$1"
	   "java.net.URLClassLoader$3$1"

## 2.3 自定义的ClassLoader例子

	package classloader;  
	  
	import java.io.ByteArrayOutputStream;  
	import java.io.InputStream;  
	import java.net.URL;  
	  
	/** 
	 * 加载网络class的ClassLoader 
	 */  
	public class NetworkClassLoader extends ClassLoader {  
	      
	    private String rootUrl;  
	  
	    public NetworkClassLoader(String rootUrl) {  
	        this.rootUrl = rootUrl;  
	    }  
	  
	    @Override  
	    protected Class<?> findClass(String name) throws ClassNotFoundException {  
	        Class clazz = null;
			// this.findLoadedClass(name); 父类已加载   
			//检查该类是否已被加载过    
	        if (clazz == null) {  
				//根据类的二进制名称,获得该class文件的字节码数组 
	            byte[] classData = getClassData(name);   
	            if (classData == null) {  
	                throw new ClassNotFoundException();  
	            }  
				//将class的字节码数组转换成Class类的实例  
	            clazz = defineClass(name, classData, 0, classData.length);  
	        }   
	        return clazz;  
	    }  
	  
	    private byte[] getClassData(String name) {  
	        InputStream is = null;  
	        try {  
	            String path = classNameToPath(name);  
	            URL url = new URL(path);  
	            byte[] buff = new byte[1024*4];  
	            int len = -1;  
	            is = url.openStream();  
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	            while((len = is.read(buff)) != -1) {  
	                baos.write(buff,0,len);  
	            }  
	            return baos.toByteArray();  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        } finally {  
	            if (is != null) {  
	               try {  
	                  is.close();  
	               } catch(IOException e) {  
	                  e.printStackTrace();  
	               }  
	            }  
	        }  
	        return null;  
	    }  
	  
	    private String classNameToPath(String name) {  
	        return rootUrl + "/" + name.replace(".", "/") + ".class";  
	    }  
	  
	}  

# 3 Android ClassLoader

Android 的`Dalvik/ART `虚拟机和标准 JVM 一样，也是需要加载class文件到内存中使用，**但是在ClassLoader的加载细节上会有些差别**

- 无论是DVM还是ART 加载的都不是Class文件而是dex文件。

- Android中的ClassLoader分为俩种类型，系统ClassLoader和自定义ClassLoader

	- 系统ClassLoader包括：

	1. `BootClassLoader`(定义在`android.jar` 中的ClassLoader中)

	2. `PathClassLoader`

	3. `DexClassLoader`


[`Android-ClassLoader`](https://developer.android.com/reference/java/lang/ClassLoader.html)是一个抽象类，它有俩个实现类`BaseDexClassLoader`和`SecureClassLoader`。其中一个实现类`BaseDexClassLoader`是由谷歌提供的，在oracle的文档中是没有的。

- **`SecureClassLoader`**:其子类只有一个URLClassLoader，用来加载jar文件，这在Android的Davlvik/ART上是没法使用的

- **`BaseDexClassLoader`**:其直接子类 有 **`DexClassLoader`**,`InMemoryDexClassLoader`(API>26),**`PathClassLoader`**。

- [源码地址](https://android.googlesource.com/platform/libcore/+/refs/heads/master/dalvik/src/main/java/dalvik/system/)


## 3.1 Android 系统ClassLoader

[PathClassLoader/DexClassLoader 源码地址](https://android.googlesource.com/platform/libcore/+/master/dalvik/src/main/java/dalvik/system)

[DexClassLoader源码地址](https://android.googlesource.com/platform/libcore/+/master/dalvik/src/main/java/dalvik/system/DexClassLoader.java)

### 3.1.1 BootClassLoader

与Java中的BootstrapClassLoader不同，它是由Java实现的而不是由C/C++实现

**BootClassLoader是ClassLoader的内部类**,并继承自ClassLoader。另外BootClassLoader是一个单例类，**其类访问修饰符是默认的，意思是同一包内访问，因此在应用中是无法调用的**。

[ClassLoader-线上源码](https://android.googlesource.com/platform/libcore/+/refs/heads/master/ojluni/src/main/java/java/lang/ClassLoader.java)


	class BootClassLoader extends ClassLoader {
	
	    private static BootClassLoader instance;
	    @FindBugsSuppressWarnings("DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED")
	    public static synchronized BootClassLoader getInstance() {
	        if (instance == null) {
	            instance = new BootClassLoader();
	        }
	        return instance;
	    }
	    public BootClassLoader() {
	        super(null);
	    }
		.............
	}

### 3.1.2 PathClassLoader

Android系统**使用PathClassLoader来加载系统类和应用程序的类**，如果是加载应用程序类，则会加载`data/app/`目录下的dex文件以及包含dex的apk文件或jar文件，不管是加载哪种类型的文件，最终都是加载dex文件，**在这里为了方便理解，我们将dex文件以及包含dex的apk文件或jar文件统称为dex相关文件**。

`PathClassLoader`不建议开发者直接使用。来查看它的代码： 

	public class PathClassLoader extends BaseDexClassLoader {
	    public PathClassLoader(String dexPath, ClassLoader parent) {
	        super(dexPath, null, null, parent);
	    }
	    public PathClassLoader(String dexPath, String librarySearchPath, ClassLoader parent) {
	        super(dexPath, null, librarySearchPath, parent);
	    }
	}

- **String dexPath**:

	dex文件以及包含dex的jar文件或apk文件的路径集合，多个文件路径之间以文件分隔符分隔，默认分隔符是":"

	**dexPath一般是已经安装应用的apk文件路径，PathClassLoader在应用启动时创建，从`/data/app/.`安装目录下加载apk文件。**

- **String libraryPath**:

	包含native libraries(c/c++库)的文件夹路径集合

	多个文件之间以文件分隔符分隔，默认":",可能为null

- **ClassLoader parent**:当前ClassLoader的父类加载器

**PathClassLoader代码中只有俩个构造方法，具体实现都在BaseDexClassLoader.**

- `PathClassLoader` 在源码中寻找其构造方法的调用地方

	- `ZygoteInit`中的调用是用来启动相关的系统服务

	- `ApplicationLoaders`中用来加载系统安装过的apk，用来加载apk内的class，其调用是在LoadApk类中getClassLoader()方法，得到的就是PathClassLoader

- `PathClassLoader`并没有重写`BaseDexClassLoader`中的方法，类似`findClass()` 之类的逻辑都是在`BaseDexClassLoader`中被实现，这点`PathClassLoader`和`DexClassLoader`都一样

### 3.1.3 DexClassLoader

> A class loader that loads classes from .jar and .apk files containing a classes.dex entry. This can be used to execute code not installed as part of an application.

对比`PathClassLoader`只能加载已安装应用的dex或apk文件，`DexClassLoader`没有此限制，可以从SD卡上加载包含`classes.dex`的`.jar`和`.apk`文件

- **这也是插件化和热修复的基础，在不需要安装应用的情况下，完成需要使用的dex加载。**

**DexClassLoader只有一个构造方法，其具体实现在BaseDexClassLoader**

	public class DexClassLoader extends BaseDexClassLoader {
	 public DexClassLoader(String dexPath, String optimizedDirectory,
	            String libraryPath, ClassLoader parent) {
	        super(dexPath, new File(optimizedDirectory), libraryPath, parent);
	    }
	}

- **String dexPath**:

	dex文件以及包含dex的jar文件或apk文件的路径集合，多个文件路径之间以文件分隔符分隔，默认分隔符是":"。

- **String optimizedDirectory**:

	~~API 26开始被废弃,表示优化过的 dex 文件(即ODEX)的缓存路径，即从 apk 或 jar 文件中提取出来的 dex 文件。PathClassLoader已经默认了这个参数为`/data/dalvik-cache/`。该路径不可以为空，且应该是应用私有的，有读写权限的路径（实际上也可以使用外部存储空间，但是这样的话就存在代码注入的风险），可以通过以下方式来创建一个这样的路径：~~

		File dexOutputDir = context.getCodeCacheDir();//API>21

- **String libraryPath**:

	存储 C/C++ 库文件的路径集

- **Classloader parent**:

	父类加载器

### 3.1.4 Android ClassLoader的继承关系

**在Android中，App 安装到手机后，apk里面的`classes.dex`中的class都是通过`PathClassLoader`加载的。**

- 举个栗子，可以使用如下方式 打印出使用的ClassLoader:

		ClassLoader loader = MainActivity.class.getClassLoader();
	        while (loader != null) {
	            System.err.println(loader.toString());
	            loader = loader.getParent();
	    }
	
		W/System.err: dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.hmt.analytics.customizeplugin-1/base.apk"],nativeLibraryDirectories=[/data/app/com.hmt.analytics.customizeplugin-1/lib/arm64, /vendor/lib64, /system/lib64]]]

		W/System.err: java.lang.BootClassLoader@1c3e199e

	- 可以看到有俩种`ClassLoader` : `PathClassLoader`和`BootClassLoader`

	- `DexPathList`中包含了许多apk的路径，其中`/data/app/com.hmt.analytics.customizeplugin-1/base.apk`就是应用apk在手机中的位置。

- Android中的ClassLoader继承关系如下：

	![](http://upload-images.jianshu.io/upload_images/1417629-00545613a9db8f6f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

	- `ClassLoader`是一个抽象类，其中定义了`ClassLoader`的主要功能。`BootClassLoader`是它的内部类。

	- `SecureClassLoader`类和JDK8中的`SecureClassLoader`类的代码是一样的，它继承了抽象类`ClassLoader`。`SecureClassLoader`并不是`ClassLoader`的实现类，而是拓展了`ClassLoader`类加入了权限方面的功能，加强了`ClassLoader`的安全性。

	- `URLClassLoader`类和JDK8中的`URLClassLoader`类的代码是一样的，它继承自`SecureClassLoader`，用来通过URl路径从jar文件和文件夹中加载类和资源。

	- `BaseDexClassLoader`继承自`ClassLoader`，是抽象类`ClassLoader`的具体实现类，`PathClassLoader`和`DexClassLoader`都继承它。

### 3.1.5 Android ClassLoader的创建过程

//TODO

## 3.2 Android 的dex文件

Android应用打包成apk文件时，class文件会被dx.jar工具打包成 dex文件。

将apk文件解压缩之后，里面存在classes.dex文件，可能有多个 (因为Android 应用及引用库中方法达到特定数量时 会引起崩溃,所以谷歌提供了Multidex方案 来解决这个问题).

当 Android 系统安装一个应用的时候，会针对不同平台对 Dex 进行优化，这个过程由一个专门的工具来处理，叫 DexOpt 。DexOpt 是在第一次加载 Dex 文件的时候执行的，该过程会生成一个 ODEX 文件，即 Optimised Dex。执行 ODEX 的效率会比直接执行 Dex 文件的效率要高很多，加快 App 的启动和响应。

优化后的ODEX文件存储在`/data/dalvik-cache`下，**后缀名还是 `.dex` **。具体可以查看 [相关文章链接一](http://www.mywiki.cn/hovercool/index.php/ART%E5%92%8CDalvik)。

**Android中的Dalvik/ART 无法像JVM那样直接加载class文件和jar文件中的class**，而是需要通过dx工具来转换成 Dalvik byte code才行，**只能通过dex或者包含dex的jar、apk文件来加载**。（注意odex文件后缀可能是.dex或是.odex ，同属于dex文件），**因此Android中的ClassLoader就交给了BaseDexClassLoader。**

ODEX相关文章：

- [ART 和 Dalvik](http://www.mywiki.cn/hovercool/index.php/ART%E5%92%8CDalvik)

- [ODEX格式及生成过程](http://www.jianshu.com/p/242abfb7eb7f)

- [What are ODEX files in Android](https://stackoverflow.com/questions/9593527/what-are-odex-files-in-android)


## 3.3 BaseDexClassLoader源码分析

### 3.3.1 BaseDexClassLoader 结构

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fqgnvp1vj6j20rh0aydh0.jpg)


### 3.3.2 BaseDexClassLoader 构造函数

	public class BaseDexClassLoader extends ClassLoader {
		...
		public BaseDexClassLoader(String dexPath, File optimizedDirectory, String libraryPath, ClassLoader parent){
			super(parent);
			this.pathList = new DexPathList(this, dexPath, libraryPath, optimizedDirectory);
		}
		...
	}

- `dexPath`:
	
	待加载的程序文件(一般是dex文件，也可以是包含dex的jar/apk/zip文件)所在目录

- `optimizedDirectory`：

	dex文件的输出目录（因为在加载jar/apk/zip等压缩格式的程序文件时会解压出其中的dex文件，该目录就是专门用于存放这些被解压出来的dex文件的）。

- `libraryPath`：

	加载程序文件时需要用到的库路径。

- `parent`：

	父加载器

- `private final DexPathList pathList`这个字段非常重要，`BaseDexClassLoader`继承自`ClassLoader`实现了许多方法都是基于这个字段

	例如:`findClass()`,`findResource()`,`findResources()`,`findLibrary()`。

	    @Override
	    protected Class<?> findClass(String name) throws ClassNotFoundException {
	        List<Throwable> suppressedExceptions = new ArrayList<Throwable>();
	        Class c = pathList.findClass(name, suppressedExceptions);
	        ...
	        return c;
	    }
	    @Override
	    protected URL findResource(String name) {
	        return pathList.findResource(name);
	    }
	    @Override
	    protected Enumeration<URL> findResources(String name) {
	        return pathList.findResources(name);
	    }
	    @Override
	    public String findLibrary(String name) {
	        return pathList.findLibrary(name);
	    }


### 3.3.3 DexPathList 构造函数
[`DexPathList`](https://android.googlesource.com/platform/libcore-snapshot/+/ics-mr1/dalvik/src/main/java/dalvik/system/DexPathList.java)构造方法比较简单，接收了dexPath,libraryPath 和optimizedDirectory。

- 并且调用`makeDexElements()`方法生成了一个`Element[] dexElements`数组

- Element是DexPathList的一个嵌套类。

	
	//DexPathList构造方法 8.0 的代码
	DexPathList(ClassLoader definingContext, 
		String dexPath,
		String librarySearchPath, 
		File optimizedDirectory, 
		boolean isTrusted) {
			// 省略一些非空判断的代码
			// 创建一个Element数组
			this.dexElements =
	            makeDexElements(
					splitDexPath(dexPath),
				 	optimizedDirectory,
					suppressedExceptions, 
					definingContext, 
					isTrusted));
	        this.nativeLibraryDirectories = splitLibraryPath(libraryPath);
	}

#### 3.3.3.1 Element类

	//Element类
	//Element of the dex/resource path
	static class Element {

		//A file denoting a zip file (in case of a resource jar or a dex jar), or a directory (only when dexFile is null).
		public final File file;
		public final ZipFile zipFile;
		public final DexFile dexFile;
		public Element(File file, ZipFile zipFile, DexFile dexFile) {
			this.file = file;
			this.zipFile = zipFile;
			this.dexFile = dexFile;
		}

		public URL findResource(String name) {
			if ((zipFile == null) || (zipFile.getEntry(name) == null)) {
				return null;
			}

	        try {
				return new URL("jar:" + file.toURL() + "!/" + name);
			} catch (MalformedURLException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

#### 3.3.3.2 DexPathList-makeDexElements()

    private static Element[] makeDexElements(List<File> files, File optimizedDirectory,
            List<IOException> suppressedExceptions, ClassLoader loader, boolean isTrusted) {
		// files 代表 dexPath 转换来的文件对象
		Element[] elements = new Element[files.size()];
		int elementsPos = 0;
		/*
		* Open all files and load the (direct or contained) dex files up front.
		*/
		for (File file : files) {
			if (file.isDirectory()) {
				// We support directories for looking up resources. Looking up resources in
				// directories is useful for running libcore tests.
				elements[elementsPos++] = new Element(file);
			} else if (file.isFile()) {

				String name = file.getName();
				DexFile dex = null;
				if (name.endsWith(DEX_SUFFIX)) {
					// Raw dex file (not inside a zip/jar).
					try {
						dex = loadDexFile(file, optimizedDirectory, loader, elements);
						if (dex != null) {
							elements[elementsPos++] = new Element(dex, null);
						}
					} catch (IOException suppressed) {
						System.logE("Unable to load dex file: " + file, suppressed);
						suppressedExceptions.add(suppressed);
					}
				} else {

					try {
						dex = loadDexFile(file, optimizedDirectory, loader, elements);
					} catch (IOException suppressed) {
                      /*
                       * IOException might get thrown "legitimately" by the DexFile constructor if
                       * the zip file turns out to be resource-only (that is, no classes.dex file
                       * in it).
                       * Let dex == null and hang on to the exception to add to the tea-leaves for
                       * when findClass returns null.
                       */
                      suppressedExceptions.add(suppressed);
					}

					if (dex == null) {
						elements[elementsPos++] = new Element(file);
					} else {
						elements[elementsPos++] = new Element(dex, file);
					}
				}

				if (dex != null && isTrusted) {
					dex.setTrusted();
				}
	
			} else {
              System.logW("ClassLoader referenced unknown path: " + file);
			}
		}

		if (elementsPos != elements.length) {
			elements = Arrays.copyOf(elements, elementsPos);
		}

		return elements;
	}

- **参数List<File> files**： 由 dexPath经过处理获取，进行拆分并封装成File

- **参数File optimizedDirectory** ：dex文件的输出目录

- **参数ArrayList<IOException> suppressedExceptions**:集合 用来收集IOException

- **参数ClassLoader loader**: 类加载器

- **参数boolean isTrusted:**

- **总的功能就是将 dexPath路径中的 含有dex的文件 组装成一个Element[] 数组！**

#### 3.3.3.3 DexPathList-findClass()

    public Class<?> findClass(String name, List<Throwable> suppressed) {
        for (Element element : dexElements) {
            Class<?> clazz = element.findClass(name, definingContext, suppressed);
            if (clazz != null) {
                return clazz;
            }
        }
        if (dexElementsSuppressedExceptions != null) {
            suppressed.addAll(Arrays.asList(dexElementsSuppressedExceptions));
        }
        return null;
    }

- 根据传入的完整的类名来加载对应的class

- 这里有关于热修复的一个点，就是将补丁dex文件放到`DexPathList类的dexElements成员变量`中(dexElements是一个Element 数组，需要放到这个数组的最前面),这样在findClass之中 优先找到补丁dex文件，加载到之后就会停止寻找，达到了修复的目的。

### 3.3.4 BaseDexClassLoader寻找class的线路

1. 当传入一个完整的类名，调用 `BaseDexClassLader` 的 `findClass(String name) `方法

2. `BaseDexClassLader` 的 findClass 方法会交给` DexPathList `的 `findClass(String name, List<Throwable> suppressed)` 方法处理

3. 在 `DexPathList` 方法的内部，会遍历 dexFile ，通过 DexFile 的 `dex.loadClassBinaryName(name, definingContext, suppressed) `来完成类的加载

**注意：**

- 实际在项目中使用`BaseDexClassLoader`或`DexClassLoader`去加载某个dex或者apk的class时，是无法调用`findClass()`因为它是被protected修饰的。实际上需要去调用`loadClass(String className)`方法，该方法在ClassLoader中具体实现。


# 4. 利用DexClassLoader实现加载本地 dex文件实例

1. 创建俩个类，`luck.ryan.ISayHello.java`和`luck.ryan.HelloJava.java`

		package luck.ryan;
		
		public interface ISayHello {
		    String say();
		}
		
		package luck.ryan;
		
		public class HelloJava implements ISayHello {
		    @Override
		    public String say() {
		        return "i am hello  from dex file";
		    }
		}

2. 编写task 将这俩个类的.class文件打成jar包

		task makeJar(type: org.gradle.api.tasks.bundling.Jar) {
		
		    baseName "hellojava"
		
		    from "build/intermediates/classes/debug/"
		
		    into "/"
		
		    exclude {
		        it.path.contains('hmtdemo')||it.path.contains('android')
		    }
		}

	- exclude 只是为了排除其他类

	- 利用gradlew makeJar 命令即可 在build/libs 下面找到这个jar

3. 利用dx.bat 文件对jar包进行处理，生成dex文件

		dx --dex --output=sayhello_dex.jar sayhello.jar

	- dx.bat 文件处于 `E:\sdk\build-tools`下的具体版本中

	- 执行完命令之后会生成新的jar包，这个jar包包含dex文件

4. 将包含dex文件的jar包放入手机存储空间

5. 在一个app中编写如下代码

	    public void testHotFix() {
	
	        // 获取到包含 class.dex 的 jar 包文件
	        final File jarFile =
	                new File(Environment.
	                        getExternalStorageDirectory().getPath()
	                        + File.separator + "HMT_TEST" + File.separator + "hello.jar");
	        if (!jarFile.exists()) {
	            return;
	        }
	
	        // 如果没有读权限,确定你在 AndroidManifest 中是否声明了读写权限
	
	        // getCodeCacheDir() 方法在 API 21 才能使用,实际测试替换成 getExternalCacheDir() 等也是可以的
	        // 只要有读写权限的路径均可
	        DexClassLoader dexClassLoader =
	                new DexClassLoader(jarFile.getAbsolutePath(), getExternalCacheDir().getAbsolutePath(), null, getClassLoader());
	
	        Log.d(TAG, "testHotFix cacheDir = " + getExternalCacheDir().getAbsolutePath());
	        try {
	            // 加载 HelloJava 类
	            Class clazz = dexClassLoader.loadClass("luck.ryan.HelloJava");
	            // 强转成 ISayHello, 注意 ISayHello 的包名需要和 jar 包中的一致
	            ISayHello iSayHello = (ISayHello) clazz.newInstance();
	            Log.d(TAG, "testHotFix = " + iSayHello.say());
	        } catch (ClassNotFoundException e) {
	            e.printStackTrace();
	        } catch (InstantiationException e) {
	            e.printStackTrace();
	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	        }
	
	    }

6. run!!!!!!