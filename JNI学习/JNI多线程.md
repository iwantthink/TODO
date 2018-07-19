# JNI多线程

[JNI完全指南(七)——多线程](https://www.zybuluo.com/cxm-2016/note/566619)

# 1. 多线程编程

本章不讲述如何使用C语言开启新线程，而是**讲述如何在本地代码中使用线程同步**。由于从Java中访问native方法并没有线程限制，所以我们的本地代码并不一定只会运行在main线程中，一旦本地方法被多个线程调用，那么我们就不得不考虑如何保证数据的安全访问问题了。为了解决这个问题，JNI提供了监视器机制，与Java中的synchronize相同，用来对临界区进行保护性访问。

## 1.1 进入临界区

Java中最简单的同步锁的例子：

	synchronized (this) {
	    //临界区
	}


- 临界区的特点就是在同一时刻仅能有一个线程运行在临界区中。其他线程会被阻塞在临界区外，直到临界区没有任何线程运行。


在JNI中，MonitorEnter函数用来进入一个临界区，当执行完需要同步的代码后，必须调用MonitorExit函数来退出临界区，否则程序将会发生死锁，函数原型如下：

	jint MonitorEnter(JNIEnv *env, jobject obj);

- `obj`：用来充当锁的Java对象

- `return`：进入临界区成功则返回JNI_OK。


## 1.2 退出临界区

退出临界区的函数原型如下

	jint MonitorExit(JNIEnv *env, jobject obj);

- `return`：退出临界区成功则返回JNI_OK。


	示例代码：
	
	if ((*env)->MonitorEnter(env, obj) != JNI_OK) {
	     //进入临界区成功
	}
	//临界区
	if ((*env)->MonitorExit(env, obj) != JNI_OK) {
	    //退出临界区成功
	}


## 1.3 线程的等待唤醒

这里主要是使用的Java的唤醒机制，如果查阅过Object的源码的话会发现，Java中仍是用了native方法来实现，所以这里的效率损失仍是相当大的，最好的实现是直接使用C或C++相应的线程库。



	static jmethodID THREAD_WAIT;
	static jmethodID THREAD_NOTIFY;
	static jmethodID THREAD_NOTYFY_ALL;
	void
	initThread(JNIEnv *env, jobject lock) {
	    jclass cls = env->GetObjectClass(lock);
	    THREAD_WAIT = env->GetMethodID(cls, "wait", "(J)V");
	    THREAD_NOTIFY = env->GetMethodID(cls, "notify", "(V)V");
	    THREAD_NOTYFY_ALL = env->GetMethodID(cls, "notifyAll", "(V)V");
	}
	void
	wait(JNIEnv *env, jobject lock, jlong timeout) {
	    env->CallVoidMethod(lock, THREAD_WAIT, timeout);
	}
	void
	notify(JNIEnv *env, jobject lock) {
	    env->CallVoidMethod(lock, THREAD_NOTIFY);
	}
	void
	notifyAll(JNIEnv *env, jobject lock) {
	    env->CallVoidMethod(lock, THREAD_NOTYFY_ALL);
	}


