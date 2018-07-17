[Android NDK 开发（一）JNI简介及调用流程](https://blog.csdn.net/u011974987/article/details/52602913)

[Android：JNI 与 NDK到底是什么?](https://blog.csdn.net/carson_ho/article/details/73250163)

[Android NDK 开发：CMake 使用](http://cfanr.cn/2017/08/26/Android-NDK-dev-CMake-s-usage/)


# 1. JNI&NDK介绍

## 1.1 JNI简介

**定义**: 

- `Java Native Interface`,**即`Java`和`C/C++`相互通信的接口**

**作用**: 

- 使得Java与本地其他类型语言(C,C++)交互

	即在 Java代码 里调用 `C、C++`等语言的代码 或 `C、C++`代码调用 Java 代码

**注意事项**:

1. `JNI`是Java调用Native语言的一种特性

2. `JNI`是属于Java的,与Android无直接关系

## 1.2 为什么有JNI?

实际开发中,Java需要与**本地代码**进行交互

- 因为Java具备 跨平台 的特点,所以Java与本地代码交互的能力非常弱


## 1.3. NDK简介

**定义**:

- `Native Development Kit`,是`Android`的一个开发工具包

	NDK是属于 Android 的，与Java并无直接关系

**作用**:

- 快速开发`C,C++`的动态库


## 1.4 JNI头文件

JNI开发的前提是需要引入`jni.h`,这个文件在Android NDK目录下

引入示例:

	#include<jni.h>

## 1.5 加载so库

Android 提供了三个函数用来在Java代码中加载JNI库

1. `System.loadLibrary(String libName)`

2. `Runtime.getRuntime().loadLibrary(String libName)`

3. `Runtime.getRuntime().load(String libFilePath)`


当使用 方式1和 方式2 去加载so库时,不需要指定so库的路径,Android会默认从系统的共享库目录里面去查找.

- Android的共享库目录就是`vendor/lib`和`system/lib`

- 如果能在共享库路径里面找到指定名称的so库,那么会立即加载这个so库,**所以在给自定义so库起名时需要避免和Android共享库里的so库重名**

- 如果不能在共享库路径中找到,那么就会在**APP的安装目录**里面查找APP的私有so库,找到之后直接加载


使用方式三 加载so库时,需要指定完整的so库路径,加载速度快

	static {
	    System.loadLibrary("native-lib");
	    //用这种方式加载so库和System.loadLibrary函数加载so库的效果是一样的
	    //Runtime.getRuntime().loadLibrary("native-lib");
	    //String soLibFilePath;
	    //用这种方式加载so库需要指定完整的so库路径
	    //Runtime.getRuntime().load(soLibFilePath);
	}


# 2. Android Studio 的so库配置

`Android Studio`通过`CMakeLists.txt`文件配置需要生成的so库

AS通过`cmake`命令生成so库

## 2.1 CMakeLists.txt文件配置

### 2.1.1 add_library

`add_library`函数用来配置要生成的so库的基本信息，比如库的名字，要生成的so库是静态的还是共享的，so库的C/C++源文件列表

**示例如下**：

	add_library( native-lib
	             SHARED
	             src/main/cpp/native-lib.cpp
	             src/main/cpp/native-lib2.cpp
	             src/main/cpp/native-lib3.cpp)

- 第一个参数是so库的名字

- 第二个参数是要生成的so库的类型，静态so库是`STATIC`,共享so库是`SHARED`

- 第三个参数是C/C++源文件，可以包括多个源文件

### 2.1.2 find_library

`find_library`函数用来从NDK目录下面查找特定的so库

**示例如下**：

	find_library( log-lib
	              log )


- 第一个参数是我们给要查找的so库起的名字，名字可以随便写

- 第二个参数是要查找的so库的名字，这个名字是从真实的so库的名字去掉前缀和后缀后的名字，比如liblog.so这个so库的名字就是log

### 2.1.3 target_link_libraries

`target_link_libraries`函数用来把要生成的so库和依赖的其它so库进行链接，生成我们需要的so库文件

**示例如下**：

	target_link_libraries( native-lib
	                       ${log-lib} )

- 第一个参数是我们要生成的so库的名字去掉前缀和后缀后的名字，在这个例子中，要生成的真实的so库的名字是libnative-lib.so

- 第二个参数是链接我们用`find_library`函数定义的查找的依赖库的名字log-lib，**语法就是${依赖的库的名字}**

# 3. Java和JNI类型对照表

## 3.1 Java和JNI基本类型对照表

Java的基本类型可以直接与`C/C++`的基本类型映射,因此Java的基本类型对开发人员是透明的

Java类型|JNI类型|C/C++类型|大小
---|---|---|---
Boolean|jboolean|unsigned char|无符号8位
Byte|jbyte|char|有符号8位
Char|jchar|unsigned short|无符号16位
Short|jshort|short|有符号16位
Integer|jint|int|有符号32位
Long|jlong|long long|有符号64位
Float|jfloat|float|32位浮点值
Double|jdouble|double|64位双精度浮点值

## 3.2 Java和JNI引用类型对照表

与Java的基本类型不同,引用类型对开发人员不透明.

**Java类的内部数据结构并不直接向原生代码公开,也就是说原生`C/C++`代码并不能直接访问Java代码的字段和方法**

Java类型|C/C++类型
---|---
java.lang.Class|jclass
java.lang.Throwable|jthrowable
java.lang.String|jstring
java.lang.Object|jobject
java.util.Objects|jobjects
java.lang.Object[]|jobjectArray
Boolean[]|jbooleanArray
Byte[]|jbyteArray
Char[]|jcharArray
Short[]|jshortArray
int[]|jintArray
long[]|jlongArray
float[]|jfloatArray
double[]|jdoubleArray
通用数组|jarray

- **任何Java数组在JNI里面都可以使用`jarray`表示**

	例如Java的`int[]`数组,用JNI可以表示为`jintArray`或者`jarray`


# 4. JNI函数详解

## 4.1 JNI字符串相关的函数

### 4.1.1 C/C++字符串转JNI字符串

`NewString`函数用来生成`Unicode` JNI字符串

`NewStringUTF`函数用来生成`UTF-8` JNI字符串


### 4.1.2 JNI字符串转C/C++字符串

GetStringChars函数用来从jstring获取Unicode C/C++字符串

GetStringUTFChars函数用来从jstring获取UTF-8 C/C++字符串

### 4.1.3 释放JNI字符串

ReleaseStringChars函数用来释放Unicode C/C++字符串

ReleaseStringUTFChars函数用来释放UTF-8 C/C++字符串

### 4.1.4 JNI字符串截取

GetStringRegion函数用来截取Unicode JNI字符串

GetStringUTFRegion函数用来截取UTF-8 JNI字符串

### 4.1.5 获取JNI字符串的长度

GetStringLength用来获取Unicode JNI字符串的长度

GetStringUTFLength函数用来获取UTF-8 JNI字符串的长度

**示例**:

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_stringFromJNI(JNIEnv* env, jobject thiz,jstring jstr) {
	    char *str="helloboy";
	    jstring jstr2=env->NewStringUTF(str);
	
	    const jchar *jchar2=env->GetStringChars(jstr,NULL);
	    size_t len=env->GetStringLength(jstr);
	    jstring jstr3=env->NewString(jchar2,len);
	}

## 4.2 JNI数组相关的函数

### 4.2.1 获取JNI基本类型数组元素

**`Get<Type>ArrayElements`函数用来获取基本类型`JNI`数组的元素 **

- 这里面的`<Type>`需要被替换成实际的类型，比如`GetIntArrayElements，GetLongArrayElements`等

示例:

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jintArray array) {
	    jint *intArray=env->GetIntArrayElements(array,NULL);
	    int len=env->GetArrayLength(array);
	    for(int i=0;i<len;i++){
	        jint item=intArray[i];
	    }
	}



### 4.2.2 获取JNI基本类型数组的子数组

**`Get<Type>ArrayRegion`函数用来获取JNI数组的子数组**

- 这里面的`<Type>`需要被替换成实际的类型，比如`GetIntArrayRegion，GetLongArrayRegion`等

示例:

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jintArray array) {
	    jint *subArray=new jint;
	    env->GetIntArrayRegion(array,0,3,subArray);
	}


### 4.2.3  设置JNI基本类型数组的子数组

`Set<Type>ArrayRegion`函数用来获取JNI基本类型数组的子数组

- 这里面的`<Type>`需要被替换成实际的类型，比如`SetIntArrayRegion，SetLongArrayRegion`等

示例:

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jintArray array) {
	    jint *subArray=new jint;
	    env->GetIntArrayRegion(array,0,3,subArray);
	    env->SetIntArrayRegion(array,0,3,subArray);
	}


### 4.2.4 JNI对象数组

`GetObjectArrayElement`函数用来获取JNI对象数组元素

`SetObjectArrayElement`函数用来设置JNI对象数组元素


示例1:

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jobjectArray array) {
	    int len=env->GetArrayLength(array);
	    for(int i=0;i<len;i++)
	    {
	        jobject item=env->GetObjectArrayElement(array,i);
	    }
	}

示例2:
	
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jobjectArray array) {
	    int len=env->GetArrayLength(array);
	    for(int i=0;i<len;i++)
	    {
	        jstring item=(jstring)env->GetObjectArrayElement(array,i);
	    }
	}
	
示例3:	 
	
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jobjectArray array) {
	    jobject obj;
	    env->SetObjectArrayElement(array,1,obj);
	}


### 4.2.5 获取JNI数组的长度

`GetArrayLength`用来获取数组的长度

示例：

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jobjectArray array) {
	    int len=env->GetArrayLength(array);
	}
	 
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jintArray array) {
	    int len=env->GetArrayLength(array);
	}
	

## 4.3 JNI NIO缓冲区相关的函数

使用NIO缓冲区可以在Java和JNI代码中共享大数据，性能比传递数组要快很多，当Java和JNI需要传递大数据时，**推荐使用NIO缓冲区的方式来传递**。

- `NewDirectByteBuffer`函数用来创建NIO缓冲区

- `GetDirectBufferAddress`函数用来获取NIO缓冲区的内容

- `GetDirectBufferCapacity`函数用来获取NIO缓冲区的大小


示例:

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz) {
	    const char *data="hello world";
	    int len=strlen(data);
	    jobject obj=env->NewDirectByteBuffer((void*)data,len);
	    long capicity=env->GetDirectBufferCapacity(obj);
	    char *data2=(char*)env->GetDirectBufferAddress(obj);
	}

## 4.4 JNI访问Java类的方法和字段

Java类型签名映射表

- **JNI获取Java类的方法ID和字段ID，都需要一个很重要的参数，就是Java类的方法和字段的签名，这个签名需要通过下面的表来获取**

Java类型|签名
---|---
Boolean|Z
Byte|B
Char|C
Short|S
Integer|I
Long|J
Float|F
Double|D
Void|V
任何Java类的全名|L任何Java类的全名;比如Java `java.lang.String`类对应的签名是`Ljava/lang/String`;
type[]| `[type`这个就是Java数组的签名，比如Java `int[]`的签名是`[I`，Java `long[]`的签名就是`[J`，Java `String[]`的签名是 `[Ljava/lang/String`;
方法类型|**（参数类型）返回值 类型** 比如Java方法`void hello(String msg,String msg2)`对应的签名就是`(Ljava/lang/String; Ljava/lang/String;)V` .....再比如Java方法`String getNewName(String name)`对应的签名是`（Ljava/lang/String;) Ljava/lang/String` 再比如Java方法`long add(int a,int b)`对应的签名是`(II)J`


## 4.5 JNI访问Java类方法相关的函数

### 4.5.1 JNI访问Java类的实例方法

`GetObjectClass`函数用来获取Java对象对应的类类型

`GetMethodID`函数用来获取Java类实例方法的方法ID

`Call<Type>Method`函数用来调用Java类实例特定返回值的方法

- 比如CallVoidMethod，调用java没有返回值的方法，CallLongMethod用来调用Java返回值为Long的方法，等等。

 
示例:

    public native void test();

    public void fuck(String msg) {
        Log.d("MainActivity", "ryan = " + msg);
    }

	 
	//JNI代码：
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(
	        JNIEnv *env,
	        jobject thiz) {
	
	    jclass clazz = env->GetObjectClass(thiz);
	
	    if (clazz == NULL) {
	        return;
	    }
	
	    jmethodID fuck_jmethodID = env->GetMethodID(clazz,
	                                                "fuck",
	                                                "(Ljava/lang/String;)V");
	    if (fuck_jmethodID == NULL) {
	        return;
	    }
	    jstring jmsg = env->NewStringUTF("i am called from jni");
	    env->CallVoidMethod(thiz, fuck_jmethodID, jmsg);
	    
	}

### 4.5.2 JNI访问Java类的静态方法

`GetObjectClass`函数用来获取Java对象对应的类类型

`GetStaticMethodID`函数用来获取Java类静态方法的方法ID

`CallStatic<Type>Method`函数用来调用Java类特定返回值的静态方法，比如CallStaticVoidMethod，调用java没有返回值的静态方法，CallStaticLongMethod用来调用Java返回值为Long的静态方法，等等。


示例:

