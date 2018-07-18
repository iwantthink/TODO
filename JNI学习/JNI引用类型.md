# JNI引用类型

[JNI完全指南(三)——引用类型](https://www.zybuluo.com/cxm-2016/note/566590)

[Android NDK 开发（一）JNI简介及调用流程](https://blog.csdn.net/u011974987/article/details/52602913)


# 1. 引用类型简介

**Java代码的内存是由垃圾回收器来管理，而JNI代码则不受Java的垃圾回收器来管理，所以JNI代码提供了一组函数，来管理通过JNI代码生成的JNI对象，比如jobject，jclass，jstring，jarray等，对于这些对象，不能简单的在JNI代码里面声明一个全局变量，然后把JNI对象赋值给全局变量，需要采用JNI代码提供的专有函数来管理这些全局的JNI对象。**

JNI中的引用类型分为:

- 全局引用

	- 强全局引用

	- 弱全局引用

- 局部引用


# 2. 全局引用

对于JNI对象，绝对不能简单的声明一个全局变量，在JNI接口函数里面给这个全局变量赋值这么简单，**一定要使用JNI代码提供的管理JNI对象的函数**，否则代码可能会出现预想不到的问题。

**JNI对象的全局引用分为两种:**

- **强全局引用**，这种引用会阻止Java的垃圾回收器回收JNI代码引用的Java对象

- **弱全局引用**，这种全局引用则不会阻止垃圾回收器回收JNI代码引用的Java对象。

## 2.1 强全局引用

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


### 2.1.1 创建全局引用

通过以下方法可以将任意引用转换为全局引用。由于全局引用不再受到JVM统一管理，所以需要在不用时手动删除。

	jobject NewGlobalRef(JNIEnv *env, jobject obj);

- `obj`：任意类型的引用。

- `return`：全局引用。如果内存不足返回NULL。


### 2.1.2 删除全局引用

通过以下方法可以删除一个全局引用。

	void DeleteGlobalRef(JNIEnv *env, jobject globalRef);

- `globalRef`：全局引用。


## 2.2 弱全局引用

弱全局引用是一种特殊的全局引用。

跟普通的全局引用不同的是，一个弱全局引用允许Java对象被垃圾回收器回收。

当垃圾回收器运行的时候，如果一个对象仅被弱全局引用所引用，则这个引用将会被回收。

一个被回收了的弱引用指向NULL，开发者可以将其与NULL比较来判定该对象是否可用。



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



### 2.2.1 创建弱全局引用

可以通过如下方法新建一个弱全局引用。

	jweak NewWeakGlobalRef(JNIEnv *env, jobject obj);

- `obj`：任意对象。

- `return`：返回弱全局引用，如果obj为NULL则返回NULL。

### 2.2.2 删除弱全局引用

可以使用如下方法删除一个弱全局引用。

	void DeleteWeakGlobalRef(JNIEnv *env, jweak obj);

- `obj`：弱全局引用。







# 3. 局部引用

在JNI接口函数中引用JNI对象的局部变量,都是对JNI对象的局部引用,一旦JNI函数返回,所有这些JNI对象都会被自动释放.

- 同时也可以采用JNI代码提供的`DeleteLocalRef`函数来删除一个局部JNI对象引用

- 局部引用是JVM负责的引用类型，其被JVM分配管理，并占用JVM的资源。

- 局部引用在native方法返回后被自动回收。

- 局部引用只在创建它们的线程中有效，不能跨线程传递。


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


## 3.1 创建局部引用

通过以下方法可以创建一个局部引用。通常情况下，我们在native方法中创建的引用都是局部引用，并且不需要手动进行释放，当方法返回时，这个引用就会被自动销毁。

	jobject NewLocalRef(JNIEnv *env, jobject ref);

- `ref`：全局或者局部引用

- `return`：局部引用


## 3.2 删除局部引用

通过以下方法可以删除一个局部引用。

	void DeleteLocalRef(JNIEnv *env, jobject localRef);

- `localRef`：局部引用。


## 3.3 确认局部引用容量

虚拟机将确保每个本地方法至少可以创建16个局部引用。但是在如今的场景中，16个局部引用已经远远不能满足开发需求了。为了为了解决这个问题，JNI提供了查询可用引用容量的方法，在创建超出限制的引用时最好先确认是否有足够的空间可以进行创建。

	jint EnsureLocalCapacity(JNIEnv *env, jint capacity);

- `capacity`：给定局部引用的数量。

- `return`：JNI_OK表示当前线程栈可以创建capacity个局部引用。返回其他值表示不可用，并抛出一个OutOfMemoryError异常


**存在异常:**

- `OutOfMemoryError`


## 3.4 局部栈帧的入栈和出栈

**`Push/PopLocalFrame`函数对提供了对局部引用的生命周期更方便的管理。**

- 可以在本地函数的入口处调用`PushLocalFrame`，然后在出口处调用PopLocalFrame，这样的话，在两个函数之间任何位置创建的局部引用都会被释放。而且，这两个函数是非常高效的。

- 如果在函数的入口处调用了PushLocalFrame，那么一定要在函数返回时调用PopLocalFrame。

---

下面的方法用来创建并入栈一个能够保存一定数量数据引用的栈帧：

	jint PushLocalFrame(JNIEnv *env, jint capacity);

- `capacity`：给定局部引用的数量。

- `return`：JNI_OK表示当前线程栈可以创建capacity个局部引用。返回其他值表示不可用，并抛出一个OutOfMemoryError异常


---
可以调用如下方法弹出一个局部引用栈帧，并销毁其中的全部局部引用。

	jobject PopLocalFrame(JNIEnv *env, jobject result);

- `result`：给定保存栈帧的引用，如果不需要前一个栈帧则可以传入NULL。

- `return`：前一个帧的引用。


**存在异常:**

- `OutOfMemoryError`：

