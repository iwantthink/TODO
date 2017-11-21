# ClassLoader
[Andoird中的ClassLoader](https://jaeger.itscoder.com/android/2016/08/27/android-classloader.html)

[深度分析ClassLoader(源码级别)](http://www.hollischuang.com/archives/199)

[深入分析Java ClassLoader原理](http://blog.csdn.net/xyang81/article/details/7292380)

# 1.ClassLoader简介
ClassLoader就是类加载器，具体作用就是将class文件加载到jvm虚拟机中，在jvm启动的时候，并不会一次性加载所有的class文件，而是根据需要去**动态加载**到内存。

简单来说ClassLoader就是加载class文件，提供给程序运行时使用。

![](http://ac-qygvx1cc.clouddn.com/78e71017bdd24420.jpeg)

## 1.1 默认的ClassLoader
Java默认提供三个ClassLoader

- **BootStrap ClassLoader**:称为 启动类加载器，是java类加载层次中最顶层的类加载器，负责加载JDK中的核心类库，如：rt.jar,resources.jar,charsets,jar等

- **Extension ClassLoader**:称为扩展类加载器，负责加载java的扩展类库，默认加载`JAVA_HOME/JRE/LIB/EXT`目录下的所有jar

- **App ClassLoader**:称为系统类加载器，负责加载应用程序classPath目录下的所有jar和class文件。


 > 除了Java默认提供的三个ClassLoader之外，用户还可以根据需要定义自已的ClassLoader，而这些自定义的ClassLoader都必须继承自java.lang.ClassLoader类，也包括Java提供的另外二个ClassLoader（Extension ClassLoader和App ClassLoader）在内，但是Bootstrap ClassLoader不继承自ClassLoader，因为它不是一个普通的Java类，底层由C++编写，已嵌入到了JVM内核当中，当JVM启动后，Bootstrap ClassLoader也随着启动，负责加载完核心类库后，并构造Extension ClassLoader和App ClassLoader类加载器。

## 1.2 双亲委托模型(Parent Delegation Model)
### 1.2.1 原理介绍
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

3. 以此类推，知道始祖类加载器(启动类加载器)

4. 始祖类加载器判断是否加载过该Class，如果已经加载，则直接返回Class。如果没有则尝试从其对应的类路径下寻找Class字节码文件并载入。如果载入成功，则直接返回Class，如果载入失败，则委托给始祖类加载器的子类加载器

5. 以此类推，一级一级往下直到源ClassLoader

6. 源ClassLoader尝试从其对应的类路径下寻找Class字节码文件并载入。如果载入成功，则直接返回Class，如果载入失败，源ClassLoader不会再委托其子类加载器，而是抛出异常(ClassNotFoundException)。

### 1.2.2 为什么使用双亲委托模型？
因为这样可以避免重复加载， 当父类已经加载了该类的时候，就没有必要子ClassLoader再加载一次

