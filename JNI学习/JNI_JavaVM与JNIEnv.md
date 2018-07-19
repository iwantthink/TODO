# JNI_JavaVM与JNIEnv

[JNI完全指南(十)——JavaVM与JNIEnv](https://www.zybuluo.com/cxm-2016/note/566623)

# 简介


# 1. JNIEnv

**JNIEnv类型是一个指向全部`JNI`方法的指针。该指针只在创建它的线程有效，不能跨线程传递。其声明如下：**

	struct _JNIEnv;
	struct _JavaVM;
	typedef const struct JNINativeInterface* C_JNIEnv;
	#if defined(__cplusplus)
	typedef _JNIEnv JNIEnv;
	typedef _JavaVM JavaVM;
	#else
	typedef const struct JNINativeInterface* JNIEnv;
	typedef const struct JNIInvokeInterface* JavaVM;
	#endif

- `JNIEnv`的具体实现,在C语言环境和C++语言环境中是不同的.也就是说在C语言和C++语言中对于JNI方法的调用是有区别的


**在C语言环境下,JNIEnv的声明方式为:**

	struct JNINativeInterface {
	    ...
	    jint        (*GetVersion)(JNIEnv *);
	    ...
	};

- 调用方式为:

	    jint version = (*env)->GetVersion(env);



**在C++语言环境下,对其进行了封装,其声明方式为:**

	struct _JNIEnv {
	    const struct JNINativeInterface* functions;
	#if defined(__cplusplus)
	    jint GetVersion()
	    { return functions->GetVersion(this); }
	    ...
	#endif /*__cplusplus*/
	};

- 调用方式为:

		jint version = env->GetVersion();


**大部分的方法,在C和C++不同环境中的差别基本如此**


## 1.1 获取当前JNI版本

	jint GetVersion(JNIEnv *env);

- 返回值是宏定义的常量
		
		#define JNI_VERSION_1_1 0x00010001
		#define JNI_VERSION_1_2 0x00010002
		#define JNI_VERSION_1_4 0x00010004
		#define JNI_VERSION_1_6 0x00010006

# 2. JavaVM

JavaVM是虚拟机在JNI中的表示,一个JVM中只有一个JavaVM对象,这个对象是线程共享的


通过JNIEnv我们可以获取一个Java虚拟机对象，其函数如下：

	jint GetJavaVM(JNIEnv *env, JavaVM **vm);

- `vm`：用来存放获得的虚拟机的指针的指针。

- `return`：成功返回0，失败返回其他。


**JNI中JVM的声明:**

	/*
	 * JNI invocation interface.
	 */
	struct JNIInvokeInterface {
	    void*       reserved0;
	    void*       reserved1;
	    void*       reserved2;
	    jint        (*DestroyJavaVM)(JavaVM*);
	    jint        (*AttachCurrentThread)(JavaVM*, JNIEnv**, void*);
	    jint        (*DetachCurrentThread)(JavaVM*);
	    jint        (*GetEnv)(JavaVM*, void**, jint);
	    jint        (*AttachCurrentThreadAsDaemon)(JavaVM*, JNIEnv**, void*);
	};



**JNI中操作JVM的声明如下:**
	
	jint JNI_GetDefaultJavaVMInitArgs(void*);
	jint JNI_CreateJavaVM(JavaVM**, JNIEnv**, void*);
	jint JNI_GetCreatedJavaVMs(JavaVM**, jsize, jsize*);
	JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved);
	JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved);


## 2.1 创建JVM

一般而言，调用`JNI_CreateJavaVM`创建JVM的线程被称为主线程。理论上来说，此方法不允许用户调用。

	jint JNI_CreateJavaVM(JavaVM **p_vm, void **p_env, void *vm_args);

- `p_vm`：保存创建的虚拟机的指针。

- `p_env`：保存获得到的JNIEnv对象的指针。

- `vm_args`：一个JavaVMInitArgs类型的指针，用来设置初始化参数。

- `return`：创建成功返回JNI_OK，失败返回其他。

其中JavaVMInitArgs是存放虚拟机参数的结构体，定义如下：

	typedef struct JavaVMOption {
	    const char* optionString;
	    void*       extraInfo;
	} JavaVMOption;
	typedef struct JavaVMInitArgs {
	    jint        version;
	    jint        nOptions;
	    JavaVMOption* options;
	    jboolean    ignoreUnrecognized;
	} JavaVMInitArgs;


举例说明具体的创建过程如下:

    JavaVMInitArgs vm_args;
    JavaVMOption options[4];
    options[0].optionString = "-Djava.compiler=NONE";           /* disable JIT */
    options[1].optionString = "-Djava.class.path=c:\myclasses"; /* user classes */
    options[2].optionString = "-Djava.library.path=c:\mylibs";  /* set native library path */
    options[3].optionString = "-verbose:jni";                   /* print JNI-related messages */
    vm_args.version = JNI_VERSION_1_2;
    vm_args.options = options;
    vm_args.nOptions = 4;
    vm_args.ignoreUnrecognized = TRUE;
    /* Note that in the JDK/JRE, there is no longer any need to call
     * JNI_GetDefaultJavaVMInitArgs.
     */
    res = JNI_CreateJavaVM(&vm, (void **)&env, &vm_args);
    if (res < 0) ...


## 2.2 链接到虚拟机

**`JNIEnv`指针仅在创建它的线程有效**。

- 如果我们需要在其他线程访问JVM，那么必须先调用`AttachCurrentThread`将当前线程与JVM进行关联，然后才能获得JNIEnv对象。当然，我们在必要时需要调用`DetachCurrentThread`来解除链接。


	jint AttachCurrentThread(JavaVM* vm , JNIEnv** env , JavaVMAttachArgs* args);

- `vm`：虚拟机对象指针。

- `env`：用来保存得到的JNIEnv的指针。

- `args`：链接参数，参数结构体如下所示。

- `return`：链接成功返回0，连接失败返回其他。
	
	struct JavaVMAttachArgs {
	    jint        version;    /* must be >= JNI_VERSION_1_2 */
	    const char* name;       /* NULL or name of thread as modified UTF-8 str */
	    jobject     group;      /* global ref of a ThreadGroup object, or NULL */
	};

## 2.3 解除与虚拟机的连接

下列函数用来解除当前线程与虚拟机之间的链接：

	jint DetachCurrentThread(JavaVM* vm);



## 2.4 卸载虚拟机

调用JNI_DestroyJavaVM函数将会卸载当前使用的虚拟机。

	jint DestroyJavaVM(JavaVM* vm);


## 2.5 动态加载本地方法

在JNI中有一组特殊的函数：

	JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved);
	JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved);

- **这一组函数的作用就是负责Java方法和本地C函数的链接**



例如，在Java代码中声明了这样一段本地代码：

	package com.github.cccxm;
	class NativeLib{
	    public static native String getName(int number);
	}

一般情况下，需要在本地源文件中声明如下：

	JNIEXPORT jstring JNICALL Java_com_github_cccxm_NativeLib_getName(JNIEnv *env,jobject thiz,jint number);

- 在这种情况下，Java方法和本地函数之间的映射关系已经通过编译器实现。

---

如果在某些场景下，**需要动态地加载本地方法**。例如，现在仍使用`NativeLib`类，但是在本地代码中，**声明了一个没有按照JNI规范命名的本地函数**：


	JNIEXPORT jstring JNICALL getName(JNIEnv *env, jclass clazz);


那么就必须使用动态关联的方式实现Java方法与本地函数的映射，代码如下：

	extern "C"
	JNIEXPORT jstring JNICALL getName(JNIEnv *env, jobject thiz, int number) {
	    ALOGE("number is %d",number);
	    return env->NewStringUTF("hello world");
	}
	static const char *CLASS_NAME = "com/github/cccxm/NativeLib";//类名
	static JNINativeMethod method = {//本地方法描述
	        "getName",//Java方法名
	        "(I)Ljava/lang/String;",//方法签名
	        (void *) getName //绑定本地函数
	};
	static bool
	bindNative(JNIEnv *env) {
	    jclass clazz;
	    clazz = env->FindClass(CLASS_NAME);
	    if (clazz == NULL) {
	        return false;
	    }
	    return env->RegisterNatives(clazz, &method, 1) == 0;
	}
	JNIEXPORT jint JNICALL
	JNI_OnLoad(JavaVM *vm, void *reserved) {
	    JNIEnv *env = NULL;
	    jint result = -1;
	    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
	        return result;
	    }
	    bool res = bindNative(env);
	    ALOGE("bind result is %s",res?"ok":"error");
	    // 返回jni的版本
	    return JNI_VERSION_1_6;
	}


- `JNI_Onload`方法在每一个`.so`库中只能存在一个


## 2.6 卸载本地方法

在上面的例子中了解了如何动态加载一个本地方法，那么有加载就有卸载，接下来，看一下如何卸载一个本地方法。

- `JNI_OnLoad`方法是在动态库被加载时调用，而`JNI_OnUnload`则是在本地库被卸载时调用。所以**这两个函数就是一个本地库最重要的两个生命周期方法**。如果没有显式的卸载一个本地库则不会看到此方法被调用。


以下举例说明用法：

	...
	static bool
	unBindNative(JNIEnv *env) {
	    jclass clazz;
	    clazz = env->FindClass(CLASS_NAME);
	    if (clazz == NULL) {
	        return false;
	    }
	    return env->UnregisterNatives(clazz) == 0;
	}
	JNIEXPORT void JNICALL
	JNI_OnUnload(JavaVM *vm, void *reserved) {
	    JNIEnv *env = NULL;
	    jint result = -1;
	    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
	        return;
	    }
	    bool res = unBindNative(env);
	    ALOGE("unbind result is %s", res ? "ok" : "error");
	}


## 2.7 获取默认虚拟机初始化参数


通过以下函数能够获取到默认的虚拟机初始化参数：

	jint JNI_GetDefaultJavaVMInitArgs(void *vm_args);

- `vm_args`：JavaVMInitArgs类型的参数，该结构体声明在2.1

- `return`：获取成功返回JNI_OK，失败返回其他。


## 2.8 获取Java虚拟机

通过以下方法可以获取到已经被创建的Java虚拟机对象。

	jint JNI_GetCreatedJavaVMs(JavaVM **vmBuf, jsize bufLen, jsize *nVMs);

- `vmBuf`：用来保存Java虚拟机的缓冲区

- `bufLen`：缓冲区长度。

- `nVms`：实际获得到的Java虚拟机个数。

- `return`：获取成功返回JNI_OK，失败返回其他。