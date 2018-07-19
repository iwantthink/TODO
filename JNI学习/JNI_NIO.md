# JNI_NIO

[JNI完全指南(八)——NIO](https://www.zybuluo.com/cxm-2016/note/566620)

# 简介

JDK/JRE 1.4时提供了新的IO访问方式，取代了之前效率不高的BIO。

- **NIO的特色之一便是直接地址访问，这种绕过JVM直接操作内存的方式极大地提高了程序的运行效率。**



# 1. 新建直接字节缓冲区

通过下列方法可以将一个内存区域作为直接内存缓冲区，为了能够成功创建缓冲区对象，应该保证传入有效的地址。

	jobject NewDirectByteBuffer(JNIEnv* env, void* address, jlong capacity);

- `address`：缓冲区指针

- `capacity`：缓冲区容量

- `return`：java.nio.ByteBuffer对象的局部引用，当发生异常时返回NULL


# 2. 获取直接缓冲区地址

	void* GetDirectBufferAddress(JNIEnv* env, jobject buf);

- `buf`：java.nio.ByteBuffer对象

- `return`：直接缓冲区的地址指针，发生异常时返回NULL

# 3. 获取直接缓冲区容量

	jlong GetDirectBufferCapacity(JNIEnv* env, jobject buf);

- `buf`：java.nio.ByteBuffer对象

- return`：缓冲区容量，发生异常时返回-1