# 本地方法

[JNI完全指南(六)——本地方法](https://www.zybuluo.com/cxm-2016/note/566615)

当在一个Java文件中书写一个native的方法的时候，为了让JNI识别方法，就需要采用注册的方式。

# 1. 注册本地方法

通过以下方法可以向JNI环境注册一个本地方法。

	jint RegisterNatives(JNIEnv *env, jclass clazz, 
	const JNINativeMethod *methods, jint nMethods);

- `clazz`: 包含本地方法的Java类。

- `methods`: 本地方法描述数组。

- `nMethods`: 数组长度。

- `return`：成功返回0，否则注册失败

用来描述本地方法的结构体包含一个char类型的指针，指向UTF-8编码的方法名，还包含一个UTF-8编码的方法签名。最后还有一个指向本地方法的指针。

	typedef struct {
	    char *name;
	    char *signature;
	    void *fnPtr;
	} JNINativeMethod;


本地方法指针就是通常在C源文件中与Java中native方法对应的函数，定义如下：

	ReturnType (*fnPtr)(JNIEnv *env, jobject objectOrClass, ...);


# 2. 解除本地方法

当确定不再需要本地方法的时候,可调用这个方法来解除本地方法的注册.这个方法会导致本地库的重新加载和链接

	jint UnregisterNatives(JNIEnv *env, jclass clazz);

- `return`：返回0表示成功，否则为失败。