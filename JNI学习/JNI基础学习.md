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

- **所有的Java类对象在JNI函数里面都使用`jobject`来表示**


# 4. JNI函数详解

## 4.1 JNI字符串相关的函数

### 4.1.1 C/C++字符串转JNI字符串

`NewString`函数用来生成`Unicode` JNI字符串

`NewStringUTF`函数用来生成`UTF-8` JNI字符串


### 4.1.2 JNI字符串转C/C++字符串

`GetStringChars`函数用来从jstring获取Unicode C/C++字符串

`GetStringUTFChars`函数用来从jstring获取UTF-8 C/C++字符串

**示例:**

    const char *str=env->GetStringUTFChars(jstr,NULL);
    const jchar *jchar2=env->GetStringChars(jstr,NULL);

### 4.1.3 释放JNI字符串

`ReleaseStringChars`函数用来释放Unicode C/C++字符串

`ReleaseStringUTFChars`函数用来释放UTF-8 C/C++字符串

### 4.1.4 JNI字符串截取

`GetStringRegion`函数用来截取Unicode JNI字符串

`GetStringUTFRegion`函数用来截取UTF-8 JNI字符串

### 4.1.5 获取JNI字符串的长度

`GetStringLength`用来获取Unicode JNI字符串的长度

`GetStringUTFLength`函数用来获取UTF-8 JNI字符串的长度

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
方法类型|**（参数类型）返回值 类型** 


- **方法类型说明**:

	Java方法`void hello(String msg,String msg2)`对应的签名就是`(Ljava/lang/String; Ljava/lang/String;)V` 

	Java方法`String getNewName(String name)`对应的签名是`（Ljava/lang/String;) Ljava/lang/String` 

	Java方法`long add(int a,int b)`对应的签名是`(II)J`

	Java方法`void delete()`对应的签名是`()V`

- **注意,通过JNI获取Java方法或字段时,基本类型不需要添加`;`符号**


## 4.5 JNI访问Java类方法相关的函数

### 4.5.1 JNI访问Java类的实例方法

`GetObjectClass`函数用来获取Java对象对应的类类型

`GetMethodID`函数用来获取Java类实例方法的方法ID

`Call<Type>Method`函数用来调用Java类实例特定返回值的方法

- 比如CallVoidMethod，调用java没有返回值的方法，CallLongMethod用来调用Java返回值为Long的方法，等等。

 
示例:
	
	//Java代码
    public native void test();
	//被调用的普通方法
    public void fuck(String msg) {
        Log.e("MainActivity", "ryan = " + msg);
    }
	 
	//JNI代码：
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(
	        JNIEnv *env,
	        jobject thiz) {
	
	    jclass clazz = env->GetObjectClass(thiz);

	    jmethodID fuck_jmethodID = env->GetMethodID(clazz,
	                                                "fuck",
                                         "(Ljava/lang/String;)V");

	    jstring jmsg = env->NewStringUTF("i am called from jni");

	    env->CallVoidMethod(thiz, fuck_jmethodID, jmsg);
	    
	}

### 4.5.2 JNI访问Java类的静态方法

`GetObjectClass`函数用来获取Java对象对应的类类型

`GetStaticMethodID`函数用来获取Java类静态方法的方法ID

`CallStatic<Type>Method`函数用来调用Java类特定返回值的静态方法，比如CallStaticVoidMethod，调用java没有返回值的静态方法，CallStaticLongMethod用来调用Java返回值为Long的静态方法，等等。


示例:

	//Java代码
    public native void test();
	//被调用的静态方法
    public static void fuck_static(String msg) {
        Log.e("MainActivity", "ryan :" + msg);
    }
	 
	//JNI代码：
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(
	        JNIEnv *env,
	        jobject thiz) {
	
	    jclass clazz = env->GetObjectClass(thiz);
	
	    jmethodID fuck_static_jmethodID = env->GetStaticMethodID(clazz,
	                                                             "fuck_static",
	                                                             "(Ljava/lang/String;)V");
	    jstring  jsmg2 = env->NewStringUTF("Saturday is an important day");
	
	    env->CallStaticVoidMethod(clazz,fuck_static_jmethodID,jsmg2);
	}


## 4.6 JNI访问Java类字段相关的函数

### 4.6.1 JNI访问Java类实例字段

`GetFieldID`函数用来获取Java字段的字段ID

`Get<Type>Field`用来获取Java类字段的值

- 比如用`GetIntField`函数获取`Java int`型字段的值

	用`GetLongField`函数获取`Java long`字段的值

	用`GetObjectField`函数获取**Java引用类型字段的值**


示例:

	//Java代码
	public native void test();
	public String name = "ryan";

	//JNI代码
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(
	        JNIEnv *env,
	        jobject thiz) {
	
	    jclass clazz = env->GetObjectClass(thiz);
	
	    jfieldID name_jfieldID = env->GetFieldID(clazz, "name", "Ljava/lang/String;");
	
	    jstring name = (jstring) env->GetObjectField(thiz, name_jfieldID);
	
	}


### 4.6.2 JNI访问Java类静态字段

`GetStaticFieldID`函数用来获取Java静态字段的字段ID

`GetStatic<Type>Field`用来获取Java类静态字段的值

- 比如用`GetStaticIntField`函数获取Java 静态int型字段的值

	用`GetStaticLongField`函数获取Java 静态long字段的值

	用`GetStaticObjectField`函数获取Java静态引用类型字段的值



示例:

	//Java代码
	public static int age = 12;

	//JNI代码
	#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
	
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(
	        JNIEnv *env,
	        jobject thiz) {
	
	    jclass clazz = env->GetObjectClass(thiz);
	
	    jfieldID name_static_jfieldID = env->GetStaticFieldID(clazz,
	                                                          "age",
	                                                          "I");
	
	    jint age = env->GetStaticIntField(clazz,
	                                      name_static_jfieldID);
	
	
	   LOGE("---------jint = %d",age);
	}


## 4.7 JNI线程同步相关的函数

**JNI可以使用Java对象进行线程同步**

- `MonitorEnter`函数用来锁定Java对象

- `MonitorExit`函数用来释放Java对象锁


示例:

	//Java代码
	public native void test(new Object());

	//JNI代码
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jobject obj) {
	    env->MonitorEnter(obj);
	    //do something
	    env->MonitorExit(obj);
	}


## 4.8 JNI 异常相关的函数

### 4.8.1 JNI处理Java异常

**当JNI函数调用的Java方法出现异常的时候，并不会影响JNI方法的执行**，但是并不推荐JNI函数忽略Java方法出现的异常继续执行，这样可能会带来更多的问题。

- **推荐的方法是，当JNI函数调用的Java方法出现异常的时候，JNI函数应该合理的停止执行代码。**

- `ExceptionOccurred`函数用来判断JNI函数调用的Java方法是否出现异常

- `ExceptionClear`函数用来清除JNI函数调用的Java方法出现的异常


示例:

	//Java代码
    public void fuck(String msg) {
        //抛出异常
        int i = 1/0;
        Log.e("MainActivity", "ryan = " + msg);
    }

	//JNI代码
	#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
	
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(
	        JNIEnv *env,
	        jobject thiz) {
	
	    jclass clazz = env->GetObjectClass(thiz);
	
	    jmethodID fuck_methodID = env->GetMethodID(clazz,
	                                               "fuck",
	                                               "(Ljava/lang/String;)V");
	    jstring msg = env->NewStringUTF("test exception");
	    env->CallVoidMethod(thiz, fuck_methodID, msg);
	
	    if (env->ExceptionOccurred() != NULL) {
	        env->ExceptionClear();
	        LOGE("there was a exception");
	        return;
	    }
	
	    LOGE("program end normally");
	    
	}


### 4.8.2 JNI抛出Java类型的异常

JNI通过`ThrowNew`函数抛出Java类型的异常

示例:

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(
	        JNIEnv *env,
	        jobject thiz) {
	
	    jclass clazz = env->FindClass("java/lang/NullPointerException");
	
	    env->ThrowNew(clazz,"there is something wrong!!!!!");
	}


## 4.9 JNI对象的全局引用和局部引用

**Java代码的内存是由垃圾回收器来管理，而JNI代码则不受Java的垃圾回收器来管理，所以JNI代码提供了一组函数，来管理通过JNI代码生成的JNI对象，比如jobject，jclass，jstring，jarray等，对于这些对象，不能简单的在JNI代码里面声明一个全局变量，然后把JNI对象赋值给全局变量，需要采用JNI代码提供的专有函数来管理这些全局的JNI对象。**

### 4.9.1 JNI对象的局部引用

在JNI接口函数中引用JNI对象的局部变量,都是对JNI对象的局部引用,一旦JNI函数返回,所有这些JNI对象都会被自动释放.

- 同时也可以采用JNI代码提供的`DeleteLocalRef`函数来删除一个局部JNI对象引用

示例:

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(
	        JNIEnv *env,
	        jobject thiz) {
	
	    jclass clazz = env->GetObjectClass(thiz);
	
	    jmethodID fuck = env->GetMethodID(clazz, "fuck", "(Ljava/lang/String;)V");
	
	    jstring msg = env->NewStringUTF("test deletelocalref");
	
	    env->CallVoidMethod(thiz, fuck, msg);
	
	    env->DeleteLocalRef(clazz);
	
	}

### 4.9.2 JNI对象的全局引用

对于JNI对象，绝对不能简单的声明一个全局变量，在JNI接口函数里面给这个全局变量赋值这么简单，**一定要使用JNI代码提供的管理JNI对象的函数**，否则代码可能会出现预想不到的问题。

**JNI对象的全局引用分为两种:**

- **强全局引用**，这种引用会阻止Java的垃圾回收器回收JNI代码引用的Java对象

- **弱全局引用**，这种全局引用则不会阻止垃圾回收器回收JNI代码引用的Java对象。


#### 4.9.2.1 强全局引用

`NewGlobalRef`用来创建强全局引用的JNI对象

`DeleteGlobalRef`用来删除强全局引用的JNI对象

示例:

	//强全局引用
	jobject gThiz;
	
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(
	        JNIEnv *env,
	        jobject thiz) {
	    //错误!不能直接给全局JNI对象赋值
	    //gThiz = thiz;
	
	    //生成全局的JNI对象应用,这样生成的全局的JNI对象才可以在其它函数中使用
	    gThiz = env->NewGlobalRef(thiz);
	
	    //不需要这个全局引用时,可以删除
	
	    env->DeleteGlobalRef(gThiz);
	}


#### 4.9.2.2 弱全局引用

`NewWeakGlobalRef`用来创建弱全局引用的JNI对象

`DeleteWeakGlobalRef`用来删除弱全局引用的JNI对象

`IsSameObject`用来判断两个JNI对象是否相同


示例:

	jobject gThiz;
	
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(
	        JNIEnv *env,
	        jobject thiz) {
	    //错误!不能直接给全局JNI对象赋值
	    //gThiz = thiz;
	
	    //生成全局的JNI对象应用,这样生成的全局的JNI对象才可以在其它函数中使用
	    gThiz = env->NewWeakGlobalRef(thiz);
	
	    // 判断弱全局引用是否已经被Java的垃圾回收器回收
	    if (env->IsSameObject(gThiz, NULL)) {
	        LOGE("object gThiz is GC");
	    }
	
	    //不需要这个全局引用时,可以删除
	    env->DeleteWeakGlobalRef(gThiz);
	}



# 5. Java代码和JNI代码通信


## 5.1 Java通过JNI接口调用`C/C++`方法

1. 需要在Java代码中声明`Native`方法原型

		public native void callJNI(String msg);

2. 在`C/C++`代码中声明JNI方法原型

		extern "C"
		JNIEXPORT void JNICALL
		Java_com_ryan_applistbyso_MainActivity_test(
		        JNIEnv *env,
		        jobject thiz) {
			//TODO
		}


## 5.2 JNI函数的原型

	[extern "C"]
	JNIEXPORT 函数返回值 JNICALL 
	完整的函数声明(JNIENV *env, jobject thiz, …){
		//函数主体
	}

---

**`extern "C"` 是用来表明这段JNI函数声明代码是通过`C++`编写的**,如果源代码是C语言,则可以不添加这个声明

**`JNIEXPORT`关键字 是用来说明这个函数是一个可导出函数!**
	
- 在`C/C++`库里面的函数 有些可以直接被外部调用,有些不可以,原因就是每一个`C/C++`库都有一个导出函数列表,只有在这个列表里面的函数才可以被外部直接调用,类似Java中Public,Private的区别

**`JNICALL` 说明这个函数是一个JNI函数,用来和普通的`C/C++`函数进行区分**

- 实际上不添加这个关键字,Java也可以调用这个JNI函数

**`Void`说明这个函数的返回值是void,如果需要返回值,将其替换成对应的类型即可**


**JNI函数名的组成分为三部分:**

- `Java_` : 固定字符串

- JNI方法位于Java中的某个类的完整类名(类名中的`.`需要替换成`_`)

- 真实的JNI方法名 :这个方法名需要和Java代码里面声明的JNI方法名一致


**JNI函数的参数**:

- JNI函数必须添加俩个默认参数

		JNIEnv* env,jobject thiz

	- `env`:**代表一个指向`JNIEnv`函数表的指针**,原生代码通过`JNIEnv`接口指针提供的各种函数来使用虚拟机的功能

		`JNIEnv`是一个指向线程-局部数据的指针,而线程-局部数据中包含指向线程表的指针

	- `thiz`:代表的是声明这个JNI方法的Java类的引用

- JNI函数的普通参数:即Java方法中传入的参数


# 6. 静态JNI方法和实例JNI方法区别

	Java代码：
	
	public native void test();
	public native static void call();
	C++代码：
	
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz) {
	    //do something
	}
	
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_call(JNIEnv* env, jclass thiz) {
	    //do something
	}

**区别:**

- **普通JNI方法对应的JNI函数的第二个参数是`jobject`类型**

- **静态的JNI方法对应的JNI函数的第二个参数是`jclass`类型**


