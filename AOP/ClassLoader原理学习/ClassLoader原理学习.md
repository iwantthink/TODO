# ClassLoader
[Andoird中的ClassLoader](https://jaeger.itscoder.com/android/2016/08/27/android-classloader.html)

[深度分析ClassLoader(源码级别)](http://www.hollischuang.com/archives/199)

[深入分析Java ClassLoader原理](http://blog.csdn.net/xyang81/article/details/7292380)

[oracle-docs-ClassLoader-loadClass](https://docs.oracle.com/javase/7/docs/api/java/lang/ClassLoader.html#loadClass(java.lang.String)

[超详细java中的classloader详解](http://blog.csdn.net/briblue/article/details/54973413)

[Android BaseDexClassLoader 5.0 源码](https://android.googlesource.com/platform/libcore/+/lollipop-release/dalvik/src/main/java/dalvik/system/)

[Android解析ClassLoader（一）Java中的ClassLoader](http://blog.csdn.net/itachi85/article/details/78088701)

# 1.ClassLoader简介
ClassLoader就是类加载器，具体作用就是将class文件加载到jvm虚拟机中，在jvm启动的时候，并不会一次性加载所有的class文件，而是根据需要去**动态加载**到内存。

简单来说ClassLoader就是加载class文件，提供给程序运行时使用。

![](http://ac-qygvx1cc.clouddn.com/78e71017bdd24420.jpeg)

## 1.1 默认的ClassLoader
Java提供俩种类型的类加载器：系统类加载器和自定义类加载器。**Java系统类加载器**默认分为三种ClassLoader类型:

- **BootStrap ClassLoader**:称为 引导类加载器，是java类加载层次中最顶层的类加载器，负责加载JDK中的核心类库，如：rt.jar,resources.jar,charsets,jar等。默认在`/JAVA_HOME/jre/lib` 目录下，也可以通过启动Java虚拟机时指定`-Xbootclasspath`选项，来改变Bootstrap ClassLoader的加载目录。  存在于Launcher中。

	Java虚拟机的启动就是通过 Bootstrap ClassLoader创建一个初始类来完成的。**由于Bootstrap ClassLoader是使用C/C++语言实现的， 所以该加载器不能被Java代码访问到**。

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

- **Extension ClassLoader**:称为扩展类加载器，负责加载java的扩展类库，默认加载`JAVA_HOME/JRE/LIB/EXT`目录下的所有jar.可以通过`-Djava.ext.dirs`选项添加和修改Extensions ClassLoader加载的路径。 存在于Launcher中。

	可以通过`System.out.println(System.getProperty("java.ext.dirs"))`方法来获取Extensions ClassLoader所加载的目录（JVM中运行才有效），打印结果如下：

		C:\Program Files\Java\jre1.8.0_151\lib\ext;
		C:\WINDOWS\Sun\Java\lib\ext

- **App ClassLoader**:称为系统类加载器，**负责加载应用程序classPath目录下的所有jar和Class文件**。

	可以加载通过`-Djava.class.path`选项所指定的目录下的jar和Class文件



 **注意：**
>除了Java默认提供的三个ClassLoader之外，用户还可以根据需要定义自已的ClassLoader，而这些自定义的ClassLoader都必须继承自java.lang.ClassLoader类，也包括Java提供的另外二个ClassLoader（Extension ClassLoader和App ClassLoader）在内，但是Bootstrap ClassLoader不继承自ClassLoader，因为它不是一个普通的Java类，底层由C++编写，已嵌入到了JVM内核当中，当JVM启动后，Bootstrap ClassLoader也随着启动，负责加载完核心类库后，并构造Extension ClassLoader和App ClassLoader类加载器。

### 1.2 Java ClassLoader的继承关系

![](http://upload-images.jianshu.io/upload_images/1417629-bff51289538f3222.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 默认系统提供的ClassLoader有三种类型，但是系统提供的ClassLoader相关类不止三个

- ClassLoader是一个抽象类，其中定义了ClassLoader的主要功能。

- SecureClassLoader继承了抽象类ClassLoader，但SecureClassLoader并不是ClassLoader的实现类，而是拓展了ClassLoader类加入了权限方面的功能，加强了ClassLoader的安全性。

- URLClassLoader继承自SecureClassLoader，用来通过URl路径从jar文件和文件夹中加载类和资源。

- ExtClassLoader和AppClassLoader都继承自URLClassLoader，**它们都是Launcher 的内部类，Launcher 是Java虚拟机的入口应用，ExtClassLoader和AppClassLoader都是在Launcher中进行初始化的。**

## 1.3 双亲委托模型(Parent Delegation Model)

### 1.3.1 原理介绍
ClassLoader使用的双亲委托模式来搜索类，每个ClassLoader 实例都有一个父类加载的引用(不是继承关系，是一个包含关系),虚拟机内置的类加载器(Bootstrap ClassLoader)本身没有父类加载器，但是可以作为其它ClassLoader实例的父类加载器。

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
	            Class var4 = this.findLoadedClass(var1);//1
	            if(var4 == null) {
	                long var5 = System.nanoTime();
	
	                try {
	                    if(this.parent != null) {
	                        var4 = this.parent.loadClass(var1, false);//2
	                    } else {
	                        var4 = this.findBootstrapClassOrNull(var1);//3
	                    }
	                } catch (ClassNotFoundException var10) {
	                    ;
	                }
	
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

- 注释2处会判断如果父类加载器不为null，则调用父类加载器的loadClass()方法。如果父类加载器null，会调用注释3处的 `findBootstrapClassOrNull()`方法。这个方法内部调用了Native方法`findLoadedClass0()`,其最终会用Bootstrap ClassLoader 来查找类。如果Bootstrap ClassLoader 也没有找到该类，则会调用注释4处的`findClass()`方法继续逐级向下进行查找。

#2 自定义ClassLoader

**JVM已经提供了默认的类加载器，为什么还需要自定义类加载器？**
- 因为java中提供的默认ClassLoader，只加载指定目录下的jar和class，如果想加载其他位置的类或jar时，就需要用到自定义类加载器。
- 举个栗子：加载网络上的一个class文件，通过动态加载到内存之后，要调用这个类中的方法。

## 2.1 自定义类加载器的步骤 
1. 继承java.lang.ClassLoader
2. 重写父类的findClass()方法。
	- 为何仅重写findClass？因为JDK在loadClass方法中已经实现了ClassLoader搜索类的算法，当loadClass中搜索不到类时，loadClass就会调用findClass方法来搜索类。

## 2.2 loadClass方法

	protected Class<?> loadClass(String name,
	                 boolean resolve)
	                      throws ClassNotFoundException

- 使用指定的** 二进制名称 **加载时，此方法默认实现按以下顺序搜索类
	1. 调用findLoadedClass(String)检查类是否已经加载
	2. 调用loadClass父类加载器上的方法。如果父类加载器为空，则使用jvm内置的类加载器(Bootstrap ClassLoader)
	3. 调用findClass(String)来查找类

- 二进制名称例子：

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
	        Class clazz = null;//this.findLoadedClass(name); // 父类已加载     
	        //if (clazz == null) {  //检查该类是否已被加载过  
	            byte[] classData = getClassData(name);  //根据类的二进制名称,获得该class文件的字节码数组  
	            if (classData == null) {  
	                throw new ClassNotFoundException();  
	            }  
	            clazz = defineClass(name, classData, 0, classData.length);  //将class的字节码数组转换成Class类的实例  
	        //}   
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

# 3 Android中的ClassLoader

Android 的Dalvik/ART 虚拟机和标准 JVM 一样，也是需要加载class文件到内存中使用，**但是在ClassLoader的加载细节上会有些差别**

- 无论是DVM还是ART 加载的都不是Class文件而是dex文件。

- Android中的ClassLoader分为俩种类型，系统ClassLoader和自定义ClassLoader

	- 系统ClassLoader包括：BootClassLoader(定义在android.jar 中的ClassLoader中),PathClassLoader和DexClassLoader

## 3.1 Android 系统ClassLoader

### 3.1.1 BootClassLoader
与Java中的BootstrapClassLoader不同，它是由Java实现的

## 3.2 Android 的dex文件

Android应用打包成apk文件时，class文件会被dx.jar工具打包成 dex文件。

将apk文件解压缩之后，里面存在classes.dex文件，可能有多个 (因为Android 应用及引用库中方法达到特定数量时 会引起崩溃,所以谷歌提供了Multidex方案 来解决这个问题).

当 Android 系统安装一个应用的时候，会针对不同平台对 Dex 进行优化，这个过程由一个专门的工具来处理，叫 DexOpt 。DexOpt 是在第一次加载 Dex 文件的时候执行的，该过程会生成一个 ODEX 文件，即 Optimised Dex。执行 ODEX 的效率会比直接执行 Dex 文件的效率要高很多，加快 App 的启动和响应。

优化后的ODEX文件存储在`/data/dalvik-cache`下，后缀名还是 .dex 。具体可以查看 相关文章链接一。

Android中的Dalvik/ART 无法像JVM那样直接加载class文件和jar文件中的class，需要通过dx工具来转换成 Dalvik byte code才行，**只能通过dex或者包含dex的jar、apk文件来加载**。（注意odex文件后缀可能是.dex或是.odex ，同属于dex文件），**因此Android中的ClassLoader就交给了BaseDexClassLoader。**

ODEX相关文章：
- [ART 和 Dalvik](http://www.mywiki.cn/hovercool/index.php/ART%E5%92%8CDalvik)
- [ODEX格式及生成过程](http://www.jianshu.com/p/242abfb7eb7f)
- [What are ODEX files in Android](https://stackoverflow.com/questions/9593527/what-are-odex-files-in-android)

## 3.2 BaseDexClassLoader

[`ClassLoader`](https://developer.android.com/reference/java/lang/ClassLoader.html)是一个抽象类，它有俩个实现类BaseDexClassLoader和SecureClassLoader。其中一个实现类`BaseDexClassLoader`是由谷歌提供的，在oracle的文档中是没有的。

- **SecureClassLoader**:其子类只有一个URLClassLoader，用来加载jar文件，这在Android的Davlvik/ART上是没法使用的

- **BaseDexClassLoader**:其直接子类 有 **DexClassLoader**,InMemoryDexClassLoader(API>26),**PathClassLoader**。

### 3.2.1 PathClassLoader

有俩个构造函数：

    public PathClassLoader(String dexPath, ClassLoader parent) {
        super(dexPath, null, null, parent);
    }

    public PathClassLoader(String dexPath, String libraryPath,
            ClassLoader parent) {
        super(dexPath, null, libraryPath, parent);
    }
- String dexPath:包含classes和resources的jar文件或apk文件的路径，多个文件之间以文件分隔符分隔，默认是":"。
- String libraryPath:包含native libraries(c/c++库)的文件夹路径集，多个文件之间以文件分隔符分隔，默认":",可能为null

PathClassLoader在应用启动时创建，从`/data/app/.`安装目录下加载apk文件。

**PathClassLoader代码中只有俩个构造方法，具体实现都在BaseDexClassLoader.**

dexPath一般是已经安装应用的apk文件路径

在Android中，App 安装到手机后，apk里面的classes.dex中的class都是通过PathClassLoader加载的。

	ClassLoader loader = MainActivity.class.getClassLoader();
        while (loader != null) {
            System.err.println(loader.toString());
            loader = loader.getParent();
    }

	W/System.err: dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.hmt.analytics.customizeplugin-1/base.apk"],nativeLibraryDirectories=[/data/app/com.hmt.analytics.customizeplugin-1/lib/arm64, /vendor/lib64, /system/lib64]]]
	W/System.err: java.lang.BootClassLoader@1c3e199e


BootClassLoader 是PathClassLoader的父加载器，在系统启动时创建，在App启动时会将该对象传进来，具体的调用在`com.android.internal.os.ZygoteInit`的`main()`方法中调用`preload()`,然后调用`preloadClasses()`方法，在该方法内部调用了`Class的forName()`方法

	public static Class<?> forName(String className, boolean shouldInitialize,
	        ClassLoader classLoader) throws ClassNotFoundException {
	    if (classLoader == null) {
	        classLoader = BootClassLoader.getInstance();
	    }
	    // Catch an Exception thrown by the underlying native code. It wraps
	    // up everything inside a ClassNotFoundException, even if e.g. an
	    // Error occurred during initialization. This as a workaround for
	    // an ExceptionInInitializerError that's also wrapped. It is actually
	    // expected to be thrown. Maybe the same goes for other errors.
	    // Not wrapping up all the errors will break android though.
	    Class<?> result;
	    try {
	        result = classForName(className, shouldInitialize, classLoader);
	    } catch (ClassNotFoundException e) {
	        Throwable cause = e.getCause();
	        if (cause instanceof LinkageError) {
	            throw (LinkageError) cause;
	        }
	        throw e;
	    }
	    return result;
	}

- PathClassLoader 在源码中寻找其构造方法的调用地方
	- ZygoteInit中的调用是用来启动相关的系统服务
	- ApplicationLoaders中用来加载系统安装过的apk，用来加载apk内的class，其调用是在LoadApk类中getClassLoader()方法，得到的就是PathClassLoader

### 3.2.2 DexClassLoader

> A class loader that loads classes from .jar and .apk files containing a classes.dex entry. This can be used to execute code not installed as part of an application.

对比PathClassLoader只能加载已安装应用的dex或apk文件，DexClassLoader没有此限制，可以从SD卡上加载classes.dex的.jar和.apk文件，**这也是插件化和热修复的基础，在不需要安装应用的情况下，完成需要使用的dex加载。**

**DexClassLoader只有一个构造方法，其具体实现在BaseDexClassLoader**

DexClassLoader 的源码里面只有一个构造方法，这里也是遵从双亲委托模型：

	public DexClassLoader(String dexPath, String optimizedDirectory,
	        String libraryPath, ClassLoader parent) {
	    super(dexPath, new File(optimizedDirectory), libraryPath, parent);
	}

- String dexPath:包含classes和resources的jar文件或apk文件的路径，多个文件之间以文件分隔符分隔，默认是":"
- String optimizedDirectory:~~已经被废弃,用来缓存优化的 dex 文件的路径，即从 apk 或 jar 文件中提取出来的 dex 文件。该路径不可以为空，且应该是应用私有的，有读写权限的路径（实际上也可以使用外部存储空间，但是这样的话就存在代码注入的风险），可以通过以下方式来创建一个这样的路径：~~
		File dexOutputDir = context.getCodeCacheDir();//API>21

- String libraryPath:存储 C/C++ 库文件的路径集

- Classloader parent:父类加载器


### 3.2.3 BaseClassLoader源码分析


	/**
	 * Base class for common functionality between various dex-based
	 * {@link ClassLoader} implementations.
	 */
	public class BaseDexClassLoader extends ClassLoader {
	    private final DexPathList pathList;
	  
	    public BaseDexClassLoader(String dexPath, File optimizedDirectory,
	            String libraryPath, ClassLoader parent) {
	        super(parent);
	        this.pathList = new DexPathList(this, dexPath, libraryPath, optimizedDirectory);
	    }

	    @Override
	    protected Class<?> findClass(String name) throws ClassNotFoundException {
	        List<Throwable> suppressedExceptions = new ArrayList<Throwable>();
	        Class c = pathList.findClass(name, suppressedExceptions);
	        if (c == null) {
	            ClassNotFoundException cnfe = new ClassNotFoundException("Didn't find class \"" + name + "\" on path: " + pathList);
	            for (Throwable t : suppressedExceptions) {
	                cnfe.addSuppressed(t);
	            }
	            throw cnfe;
	        }
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
	   
	    @Override
	    protected synchronized Package getPackage(String name) {
	        if (name != null && !name.isEmpty()) {
	            Package pack = super.getPackage(name);
	            if (pack == null) {
	                pack = definePackage(name, "Unknown", "0.0", "Unknown",
	                        "Unknown", "0.0", "Unknown", null);
	            }
	            return pack;
	        }
	        return null;
	    }
	    /**
	     * @hide
	     */
	    public String getLdLibraryPath() {
	        StringBuilder result = new StringBuilder();
	        for (File directory : pathList.getNativeLibraryDirectories()) {
	            if (result.length() > 0) {
	                result.append(':');
	            }
	            result.append(directory);
	        }
	        return result.toString();
	    }
	    @Override public String toString() {
	        return getClass().getName() + "[" + pathList + "]";
	    }
	}