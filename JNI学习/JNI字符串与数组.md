# JNI字符串与数组

[JNI完全指南(五)——字符串与数组](https://www.zybuluo.com/cxm-2016/note/566597#%E4%BA%94%E5%AD%97%E7%AC%A6%E4%B8%B2%E4%B8%8E%E6%95%B0%E7%BB%84)

# 1. Java字符串

## 1.1 新建Java字符串

在JNI中，如果需要使用一个Java字符串，可以采用如下方式新建String对象。

	jstring NewString(JNIEnv *env, const jchar *unicodeChars, 
	jsize len);

- `unicodeChars`：一个指向Unicode编码的字符数组的指针。

- `len`：unicodeChars的长度

- `return`：Java字符串对象

存在异常

- OutOfMemoryError

## 1.2 获取Java字符串长度

通过以下方法我们能够获取到Java字符串的长度

	jsize GetStringLength(JNIEnv *env, jstring string);

- `string`：Java字符串对象

- `return`：字符串长度


## 1.3 从Java字符串获取字符数组

可以通过以下方法从Java字符串获取字符数组，**当使用完毕后，需要调用`ReleaseStringChars`进行释放。**

	const jchar * GetStringChars(JNIEnv *env, jstring string, 
	jboolean *isCopy);

- `isCopy`：注意，**这个参数很重要**

	这是一个指向Java布尔类型的指针。函数返回之后应当检查这个参数的值

	- 如果值为JNI_TRUE表示返回的字符是Java字符串的拷贝，可以对其中的值进行任意修改。

	- 如果返回值为JNI_FALSE，表示这个字符指针指向原始Java字符串的内存，这时候对字符数组的任何修改都将会原始字符串的内容。
	
	**如果你不关心字符数组的来源，或者说你的操作不会对字符数组进行任何修改，可以传入NULL**。

- `return`：指向字节数组的指针


## 1.4 释放从Java字符串中获取的字符数组

	void ReleaseStringChars(JNIEnv *env, jstring string, 
	const jchar *chars);

- `string`：Java字符串对象。

- `chars`：字符数组。

## 1.5 创建UTF-8编码字符串

	jstring NewStringUTF(JNIEnv *env, const char *bytes);

- `bytes`：UTF-8编码的字节数组。

- `return`：UTF-8编码的Java字符串对象


## 1.6 获取UTF-8字符串的长度

	jsize GetStringUTFLength(JNIEnv *env, jstring string);

## 1.7 获取UTF-8编码的Java字符串

	const char * GetStringUTFChars(JNIEnv *env, jstring string, jboolean *isCopy);

## 1.8 释放从UTF-8字符串中获取的字符数组

	void ReleaseStringUTFChars(JNIEnv *env, jstring string, const char *utf);

## 1.9 从Java字符串中截取一段字符

如果想要从字符串中获取其中的一段内容，可以采用如下方式：

	void GetStringRegion(JNIEnv *env, jstring str, jsize start, jsize len, jchar *buf);

- `str`：Java字符串对象。

- `start`：起始位置。

- `len`：截取长度。

- `buf`：保存截取结果的缓冲区。

**存在异常**

- `StringIndexOutOfBoundsException`


## 1.10 从UTF-8字符串中截取一段字符

	void GetStringUTFRegion(JNIEnv *env, jstring str, jsize start, jsize len, char *buf);

## 1.11 直接字符串指针

**当需要获取字符数组时，使用上面1.3和1.7的方法都有可能或得到原始字符串的拷贝，很明显这对运行效率有些影响**。

如果能够获得原始字符串的直接指针，就可以极大地优化运行效率。于是JNI提供了`Get/ReleaseStringCritical`两个函数来操作原始字符串的直接指针。但是对直接指针的操作有着极其严格的限制。这两个函数之间不能存在任何会让线程阻塞的操作。 

这两个函数原型如下：

	const jchar * GetStringCritical(JNIEnv *env, jstring string, jboolean *isCopy);
	
	void ReleaseStringCritical(JNIEnv *env, jstring string, const jchar *carray);


# 2. Java数组

## 2.1 获取数组长度

	jsize GetArrayLength(JNIEnv *env, jarray array);


示例：

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jobjectArray array) {
	    int len=env->GetArrayLength(array);
	}

## 2.2 新建对象数组

使用如下方法可以创建一个对象数组。

	jobjectArray NewObjectArray(JNIEnv *env, jsize length, 
	jclass elementClass, jobject initialElement);

- `length`：数组的长度。

- `elementClass`：数组中的对象类型。

- `initialElement`：数组中的每个元素都会使用这个值进行初始化，可以为NULL。

- `return`：对象数组，创建失败返回NULL

**存在异常**

- `OutOfMemoryError`


## 2.3 获取对象数组元素

在JNI中获取对象数组元素需要使用下列函数

	jobject GetObjectArrayElement(JNIEnv *env,
		 jobjectArray array, jsize index);

- `array`：对象数组

- `index`：位置索引

**存在异常**

`ArrayIndexOutOfBoundsException`

**示例1**:

	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jobjectArray array) {
	    int len=env->GetArrayLength(array);
	    for(int i=0;i<len;i++)
	    {
	        jobject item=env->GetObjectArrayElement(array,i);
	    }
	}

**示例2**:
	
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jobjectArray array) {
	    int len=env->GetArrayLength(array);
	    for(int i=0;i<len;i++)
	    {
	        jstring item=(jstring)env->GetObjectArrayElement(array,i);
	    }
	}


## 2.4 设置对象数组元素

	void SetObjectArrayElement(JNIEnv *env, jobjectArray array, 
	jsize index, jobject value);

- `array`：对象数组

- `index`：位置索引

- `value`:对应位置的数据

**存在异常**

- `ArrayIndexOutOfBoundsException`

- `ArrayStoreException`：传入的值与数组类型不一致


	
示例3:	 
	
	extern "C"
	JNIEXPORT void JNICALL
	Java_com_ryan_applistbyso_MainActivity_test(JNIEnv* env, jobject thiz,jobjectArray array) {
	    jobject obj;
	    env->SetObjectArrayElement(array,1,obj);
	}

## 2.5 基本数据类型数组

### 2.5.1 新建基本数据类型数组

新建基本数据类型数组的函数与2.2类似，函数原型为

	ArrayType New<PrimitiveType>Array(JNIEnv *env, jsize length);

具体函数名与返回值类型的关系如下表所示：


函数名	|返回数组类型
---|---
NewBooleanArray	|jbooleanArray
NewByteArray	|jbyteArray
NewCharArray	|jcharArray
NewShortArray	|jshortArray
NewIntArray	|jintArray
NewLongArray	|jlongArray
NewFloatArray	|jfloatArray
NewDoubleArray	|jdoubleArray

### 2.5.2 获取基本数据类型数组元素

获取基本数据类型数组元素的函数原型为

	NativeType *Get<PrimitiveType>ArrayElements(JNIEnv *env, 
	ArrayType array, jboolean *isCopy);

- isCopy：参考1.3

具体函数名与返回类型的关系如下表所示：


函数名	|参数数组类型	|返回值类型
---|---|---
GetBooleanArrayElements	|jbooleanArray	|jboolean
GetByteArrayElements	|jbyteArray	|jbyte
GetCharArrayElements	|jcharArray	|jchar
GetShortArrayElements	|jshortArray	|jshort
GetIntArrayElements	|jintArray	|jint
GetLongArrayElements	|jlongArray	|jlong
GetFloatArrayElements	|jfloatArray	|jfloat
GetDoubleArrayElements	|jdoubleArray	|jdouble


### 2.5.3 释放基本数据类型数组

	void Release<PrimitiveType>ArrayElements(JNIEnv *env, 
	ArrayType array, NativeType *elems, jint mode);

- `mode`参数

mode	| 行为
---|---
0	|copy back the content and free the elems buffer
JNI_COMMIT	|copy back the content but do not free the elems buffer
JNI_ABORT	|free the buffer without copying back the possible changes

## 2.6 截取数组

详情参考1.9，函数原型如下：

	void GetArrayRegion(JNIEnv *env, ArrayType array, 
	jsize start, jsize len, NativeType *buf);


函数名	|数组类型	|数据类型
---|---|---
GetBooleanArrayRegion	|jbooleanArray	|jboolean
GetByteArrayRegion	|jbyteArray	|jbyte
GetCharArrayRegion	|jcharArray	|jchar
GetShortArrayRegion	|jshortArray	|jhort
GetIntArrayRegion	|jintArray	|jint
GetLongArrayRegion	|jlongArray	|jlong
GetFloatArrayRegion	|jfloatArray	|jloat
GetDoubleArrayRegion	|jdoubleArray	|jdouble

## 2.7 范围设置数组

可以通过如下方法给数组的部分赋值

	void Set<PrimitiveType>ArrayRegion(JNIEnv *env, ArrayType array, 
	jsize start, jsize len, const NativeType *buf);


函数名	|数据类型	|参数类型
---|---|---
SetBooleanArrayRegion	|jbooleanArray	|jboolean
SetByteArrayRegion	|jbyteArray	|jbyte
SetCharArrayRegion	|jcharArray	|jchar
SetShortArrayRegion	|jshortArray	|jshort
SetIntArrayRegion	|jintArray	|jint
SetLongArrayRegion	|jlongArray	|jlong
SetFloatArrayRegion	|jfloatArray	|jfloat
SetDoubleArrayRegion	|jdoubleArray	|jdouble



## 2.8 操作基本数据类型数组的直接指针


在某些情况下，需要原始数据指针来进行一些操作。

调用`GetPrimitiveArrayCritical`后，可以获得一个指向原始数据的指针，但是在调用`ReleasePrimitiveArrayCritical`函数之前，要保证不能进行任何可能会导致线程阻塞的操作。由于GC的运行会打断线程，所以在此期间任何调用GC的线程都会被阻塞。
	
	void * GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy); 
	void ReleasePrimitiveArrayCritical(JNIEnv *env, jarray array, void *carray, jint mode);


示例:


	  jint len = (*env)->GetArrayLength(env, arr1);
	  jbyte *a1 = (*env)->GetPrimitiveArrayCritical(env, arr1, 0);
	  jbyte *a2 = (*env)->GetPrimitiveArrayCritical(env, arr2, 0);
	  /* We need to check in case the VM tried to make a copy. */
	  if (a1 == NULL || a2 == NULL) {
	    ... /* out of memory exception thrown */
	  }
	  memcpy(a1, a2, len);
	  (*env)->ReleasePrimitiveArrayCritical(env, arr2, a2, 0);
	  (*env)->ReleasePrimitiveArrayCritical(env, arr1, a1, 0);

