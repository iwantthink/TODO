# JNI类与异常
[JNI完全指南(二)——类与异常](https://www.zybuluo.com/cxm-2016/note/564038)

[Android NDK 开发（一）JNI简介及调用流程](https://blog.csdn.net/u011974987/article/details/52602913)


# 1. 类

## 1.1 加载类

JNI提供了以下方法通过类定义去加载一个类

	jclass DefineClass(JNIEnv *env, const char *name, jobject loader, 
						const jbyte *buf, jsize bufLen);

- `name`：类的全名，必须是被UTF-8编码过的字符串。

- `loader`：类加载器。

- `buf`：包含类数据的缓冲区。这个缓冲区包含一个原始类数据，并且要求这个类在调用此方法前没有被JVM所引用。

- `bufLen`：缓冲区长度。

- `return`：java类对象。发生错误时返回NULL。


**加载类可能抛出的异常**

- `ClassFormatError`：类格式错误。

- `ClassCircularityError`：类循环错误。如果一个类或接口继承了自己。

- `OutOfMemoryError`：内存溢出。

- `SecurityException`：安全性错误。如果调用者试图在Java包结构上定义一个类。



## 1.2 查找类

JNI提供了以下方法可以通过类名查找到一个类

	jclass FindClass(JNIEnv *env, const char *name);

- `name`：类的全名。使用UTF-8编码，其中分隔符使用/表示。

- `return`：java类对象。发生错误时返回NULL。


**查找类可能会抛出如下异常**：

- `ClassFormatError`:类格式错误

- `ClassCircularityError`:类循环错误

- `NoClassDefFoundError`：没有找到相应的类对象。

- `OutOfMemoryError`:内存溢出

## 1.3 获取父类

JNI提供了以下方法获取到一个类的父类的类对象

	jclass GetSuperclass(JNIEnv *env, jclass clazz);

- `clazz`：需要查询的类对象。

- `return`：clazz的父类类对象。如果clazz是Object类对象或者clazz是接口的类信息，返回NULL。

## 1.4 类型判断

JNI提供了以下方法去判断这两种类型是否可以互相转换。

	jboolean IsAssignableFrom(JNIEnv *env, jclass clazz1, 
								jclass clazz2);

- `clazz1`：原始类型。

- `class2`：目标类型。

- `return`：当前类型转换是否安全。



# 2. 异常

C++中可以使用其内部的异常机制，但是这套机制抛出的异常并不会传递给JVM，所以为了弥补这个缺点，JNI实现了一套可以和JVM进行交流的异常机制。

**当JNI函数调用的Java方法出现异常的时候，并不会影响JNI方法的执行**，但是并不推荐JNI函数忽略Java方法出现的异常继续执行，这样可能会带来更多的问题。

- **推荐的方法是，当JNI函数调用的Java方法出现异常的时候，JNI函数应该合理的停止执行代码。**

## 2.1 抛出已有的异常对象

	jint Throw(JNIEnv *env, jthrowable obj);

- `obj`：一个java.lang.Throwable对象。

- `return`：异常抛出结果。0表示异常正常抛出到JVM，否则异常抛出失败。


## 2.2 抛出一个新异常对象

	jint ThrowNew(JNIEnv *env, jclass clazz, const char *message);

- `clazz`：待抛出的异常类。

- `message`：异常消息。要求UTF-8编码。

- `return`：异常抛出结果。0表示异常正常抛出到JVM，否则异常抛出失败。

示例:

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(
	        JNIEnv *env,
	        jobject thiz) {
	
	    jclass clazz = env->FindClass("java/lang/NullPointerException");
	
	    env->ThrowNew(clazz,"there is something wrong!!!!!");
	}



## 2.3 获取JVM抛出的异常

如果需要知道之前的操作是否存在JVM抛出的异常时,可以调用如下方法获取异常

	jthrowable ExceptionOccurred(JNIEnv *env);

- `return`：当前发生的异常对象。如果没有异常抛出则返回NULL。

**注意:**

- 即使获取到了异常对象，这个异常仍然在JVM中存在，直到调用`ExceptionClear`方法清空异常


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




## 2.4 输出错误日志

当拦截到一个异常，可以使用如下方法打印错误栈中的内容。就像Java中的printStackTrace：

	void ExceptionDescribe(JNIEnv *env);


## 2.5 清空异常信息

调用以下方法可以清空当前产生的全部异常信息

	void ExceptionClear(JNIEnv *env);

## 2.6 产生一个严重的错误

对于普通异常，可以产生也可以拦截。但是如果发生了一些错误导致程序无法再正常运行下去了，则可以发送一个错误信息给JVM，此时程序将被终止。

	void FatalError(JNIEnv *env, const char *msg);

- `msg`：错误信息。UTF-8。

## 2.7 检查是否存在异常信息

通过以下方法查看当前有没有异常产生。

	jboolean ExceptionCheck(JNIEnv *env);

- `return`：是否存在异常信息。

