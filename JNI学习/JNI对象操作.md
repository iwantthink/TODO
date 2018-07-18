# JNI对象操作

[JNI完全指南(四)——对象操作](https://www.zybuluo.com/cxm-2016/note/566595)

[Android NDK 开发（一）JNI简介及调用流程](https://blog.csdn.net/u011974987/article/details/52602913)

# 对象操作

# 1. 基本操作

## 1.1 创建默认对象

如果希望通过一个类创建一个对象，**并且没有或不需要调用非默认的构造方法的时候，可以使用如下方式给对象分配空间**。

	jobject AllocObject(JNIEnv *env, jclass clazz);

- `clazz`：类。

- `return`：返回使用clazz类创建的对象，如果clazz没有默认的构造方法，则返回NULL。

**存在异常:**

- `InstantiationException`：对象初始化异常，这个类可能是抽象的也可能是接口，或者传入的参数与调用的构造器不匹配。

- `OutOfMemoryError`

## 1.2 创建对象

这里创建对象和Java类似，**需要指定类信息，并且选择合适的构造器传入参数**。

**以下提供了三种创建对象的方式：**

- 第一种方式 

		jobject NewObject(JNIEnv *env, jclass clazz, 
			jmethodID methodID, ...);

	- `clazz`：类

	- `methodID`：构造器方法ID

	- `...`：可变参数列表

- 第二种方式 
		jobject NewObjectA(JNIEnv *env, jclass clazz, 
			jmethodID methodID, const jvalue *args);

	- `args`：这里需要传入参数数组

- 第三种方式 

		jobject NewObjectV(JNIEnv *env, jclass clazz, 
			jmethodID methodID, va_list args);

	- `args`：指向变参列表的指针



**存在异常**

- `InstantiationException`

- `OutOfMemoryError`


## 1.3 从对象获取类信息

通过如下方法可以从一个Java对象中获取该对象的Java类信息。

	jclass GetObjectClass(JNIEnv *env, jobject obj);

- `obj`:任意对象


## 1.4 获取一个对象的引用类型

通过如下方法可以得到当前对象的引用类型是全局引用、局部引用还是弱全局引用。

	jobjectRefType GetObjectRefType(JNIEnv* env, jobject obj);

- `return`：当前对象的引用类型。这些类型在`jni.h`文件中的定义如下所示。

		typedef enum jobjectRefType {
		    JNIInvalidRefType = 0,//无效引用
		    JNILocalRefType = 1,//局部引用
		    JNIGlobalRefType = 2,//全局引用
		    JNIWeakGlobalRefType = 3//弱全局引用
		} jobjectRefType;


## 1.5 实例运算

在Java中使用`instanceof`来判断一个对象是否是一个类的实例，在JNI中，需要通过如下方法来进行实例运算。

	jboolean IsInstanceOf(JNIEnv *env, jobject obj, 
		jclass clazz);

- `obj`:任意对象

- `clazz`:类类型

## 1.6 判断对象是否相同

在Java中使用==可以判断两个引用是否指向同一个对象，在JNI中使用下列方法可以进行相同的判断，无论是全局引用，局部引用还是弱全局引用。

	jboolean IsSameObject(JNIEnv *env, jobject ref1, 
			jobject ref2);


# 2. 访问实例对象中的属性

在JNI中如果需要操作一个属性，一般需要先获取到该属性在JVM中的唯一标识ID，然后再通过相应的Get和Set方法去操作属性


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


## 2.1 获取属性ID

通过如下方法可以获取到属性的唯一标识符ID

	jfieldID GetFieldID(JNIEnv *env, jclass clazz, 
			const char *name, const char *sig);

- `name`：使用UTF-8编码的属性名

- `sig`：使用UTF-8编码的属性类型签名

- `return`：属性ID

**存在异常**

- `NoSuchFieldError`

- `ExceptionInInitializerError`

- `OutOfMemoryError`


## 2.2 获取属性值

当获取到属性的ID的时候，就可以拿到属性的值了。在JNI中，不同类型的属性有不同的方法获取属性值，如下表所示。


获取属性值得函数名	|返回值类型
---|---
GetObjectField	|jobject
GetBooleanField	|jboolean
GetByteField	|jbyte
GetCharField	|jchar
GetShortField	|jshort
GetIntField	|jint
GetLongField	|jlong
GetFloatField	|jfloat
GetDoubleField	|jdouble

**函数的参数列表:**

- `obj`：一个Java对象(不能为空)

- `fieldID`：属性ID


## 2.3 设置属性值

设置属性值的函数名	|参数类型
---|---
SetObjectField	|jobject
SetBooleanField	|jboolean
SetByteField	|jbyte
SetCharField	|jchar
SetShortField	|jshort
SetIntField	|jint
SetLongField	|jlong
SetFloatField	|jfloat
SetDoubleField	|jdouble


**参数列表**：

- `obj`：Java对象

- `fieldID`：属性ID

- `value`：上表中对应于函数名的参数类型的值。


# 3. 调用实例方法

对于调用实例成员方法，和2.7访问属性的过程类似，需要事先获取这个方法的ID，然后根据这个ID来进行相应的操作。


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

## 3.1 获取实例方法ID

	jmethodID GetMethodID(JNIEnv *env, jclass clazz, 
		const char *name, const char *sig);

- `clazz`：Java类对象

- `name`：UTF-8编码的方法名

- `sig`：UTF-8编码的方法签名

- `return`：实例方法ID。没有此方法时返回NULL。

**存在异常**

- `NoSuchMethodError`

- `ExceptionInInitializerError`

- `OutOfMemoryError`


## 3.2 调用实例方法

在JNI中，根据不同的参数类型和返回值类型需要调用不同的方法。**对于传入的参数类型不同需要在方法名的后面使用不同的后缀来标识**。

	NativeType Call<type>Method(JNIEnv *env, jobject obj, 
	jmethodID methodID, ...);
	
	NativeType Call<type>MethodA(JNIEnv *env, jobject obj, 
	jmethodID methodID, const jvalue *args);
	
	NativeType Call<type>MethodV(JNIEnv *env, jobject obj, 
	jmethodID methodID, va_list args);

- `obj`：Java对象

- `methodID`：实例方法ID

- `...`：变参列表

- `jvalue *args`：参数数组

- `va_list args`：指向变参列表的指针


**所有函数名及其返回值类型如下表所示**：



函数名	|返回值类型
---|---
CallVoidMethod, CallVoidMethodA, CallVoidMethodV	| void
CallObjectMethod,CallObjectMethodA,CallObjectMethodV|	jobject
CallBooleanMethod,CallBooleanMethodA,CallBooleanMethodV|	jboolean
CallByteMethod,CallByteMethodA,CallByteMethodV	|jbyte
CallCharMethod,CallCharMethodA,CallCharMethodV	|jchar
CallShortMethod,CallShortMethodA,CallShortMethodV	|jshort
CallIntMethod,CallIntMethodA,CallIntMethodV	|jint
CallLongMethod,CallLongMethodA,CallLongMethodV	|jlong
CallFloatMethod,CallFloatMethodA,CallFloatMethodV	|jfloat
CallDoubleMethod,CallDoubleMethodA,CallDoubleMethodV	|jdouble



## 3.3 调用非虚实例方法

在C++中，让一个方法具有多态属性需要显式的声明virtual关键字，而**在Java中，所有的方法默认都是virtual的**。

- 如果对这些概念仍有疑问，可以[参考C++：多态公有继承](https://www.zybuluo.com/cxm-2016/note/494027)。那么当需要以非虚方式调用方法时，JNI提供了以下方法。

**方式一： **

	NativeType CallNonvirtual<type>Method(JNIEnv *env, jobject obj, 
	jclass clazz, jmethodID methodID, ...);

**方式二: **

	NativeType CallNonvirtual<type>MethodA(JNIEnv *env, jobject obj, 
	jclass clazz, jmethodID methodID, const jvalue *args);

**方式三: **

	NativeType CallNonvirtual<type>MethodV(JNIEnv *env, jobject obj, 
	jclass clazz, jmethodID methodID, va_list args);

参数列表:

- `obj`：Java对象

- `methodID`：实例方法ID

- `...`：变参列表

- `jvalue *args`：参数数组

- `va_list args`：指向变参列表的指针



**完整的函数名与返回值表**


函数名	|返回值类型
---|---
CallNonvirtualVoidMethod,CallNonvirtualVoidMethodA,CallNonvirtualVoidMethodV	|void
CallNonvirtualObjectMethod,CallNonvirtualObjectMethodA,CallNonvirtualObjectMethodV	|jobject
CallNonvirtualBooleanMethod,CallNonvirtualBooleanMethodA,CallNonvirtualBooleanMethodV	|jboolean
CallNonvirtualByteMethod,CallNonvirtualByteMethodA,CallNonvirtualByteMethodV	|jbyte
CallNonvirtualCharMethod,CallNonvirtualCharMethodA,CallNonvirtualCharMethodV	|jchar
CallNonvirtualShortMethod,CallNonvirtualShortMethodA,CallNonvirtualShortMethodV	|jshort
CallNonvirtualIntMethod,CallNonvirtualIntMethodA,CallNonvirtualIntMethodV	|jint
CallNonvirtualLongMethod,CallNonvirtualLongMethodA,CallNonvirtualLongMethodV	|jlong
CallNonvirtualFloatMethod,CallNonvirtualFloatMethodA,CallNonvirtualFloatMethodV	|jfloat
CallNonvirtualDoubleMethod,CallNonvirtualDoubleMethodA,CallNonvirtualDoubleMethodV	|jdouble




# 4. 访问静态属性

在Java中通过类就能够访问静态属性，那么与访问实例的成员属性相比，差别就是不需要通过一个Java对象就能对静态属性进行访问。



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

## 4.1 获取静态属性ID

通过以下方法能够获取一个静态属性的ID。

	jfieldID GetStaticFieldID(JNIEnv *env, jclass clazz, 
	const char *name, const char *sig);

**产生异常**

- NoSuchFieldError

- ExceptionInInitializerError

- OutOfMemoryError

## 4.2 获取静态属性值

先来看一下获取静态属性的一系列函数的名称。完整名称与返回值的对应关系如后表所示。

	NativeType GetStatic<type>Field(JNIEnv *env, jclass clazz, 
	jfieldID fieldID);


函数名称	|返回值类型
---|---
GetStaticObjectField	|jobject
GetStaticBooleanField	|jboolean
GetStaticByteField	|jbyte
GetStaticCharField	|jchar
GetStaticShortField	|jshort
GetStaticIntField	|jint
GetStaticLongField	|jlong
GetStaticFloatField	|jfloat
GetStaticDoubleField	|jdouble


## 4.3 设置静态属性值

设置静态属性值得函数可表示如下，其函数名称与参数类型的具体关系如后表所示。

	void SetStatic<type>Field(JNIEnv *env, jclass clazz, 
	jfieldID fieldID, NativeType value);


函数名	|参数类型
---|---
SetStaticObjectField	|jobject
SetStaticBooleanField	|jboolean
SetStaticByteField	|jbyte
SetStaticCharField	|jchar
SetStaticShortField	|jshort
SetStaticIntField	|jint
SetStaticLongField	|jlong
SetStaticFloatField	|jfloat
SetStaticDoubleField	|jdouble

# 5. 调用静态方法

## 5.1 获取静态方法ID

通过以下方法获取静态方法的ID

	jmethodID GetStaticMethodID(JNIEnv *env, jclass clazz, 
	const char *name, const char *sig);

- `clazz`：Java类对象

- `name`：UTF-8编码的静态方法名

- `sig`：UTF-8编码的静态方法签名

- `return`：


## 5.2 调用静态方法


调用静态方法的方式有如下三种

**第一种** 

	NativeType CallStatic<type>Method(JNIEnv *env, jclass clazz, 
	jmethodID methodID, ...);

**第二种** 

	NativeType CallStatic<type>MethodA(JNIEnv *env, jclass clazz, 
	jmethodID methodID, jvalue *args);

**第三种** 

	NativeType CallStatic<type>MethodV(JNIEnv *env, jclass clazz, 
	jmethodID methodID, va_list args);

- `...`：变参列表

- `jvalue *args`：Java值数组

- `va_list args`：指向变参列表的指针


函数名与返回值类型的关系如下表所示：



函数名	|返回参数类型
---|---
CallStaticVoidMethod，CallStaticVoidMethodA，CallStaticVoidMethodV	|void
CallStaticObjectMethod，CallStaticObjectMethodA，CallStaticObjectMethodV	|jobject
CallStaticBooleanMethod，CallStaticBooleanMethodA，CallStaticBooleanMethodV	|jboolean
CallStaticByteMethod，CallStaticByteMethodA，CallStaticByteMethodV	|jbyte
CallStaticCharMethod，CallStaticCharMethodA，CallStaticCharMethodV	|jchar
CallStaticShortMethod，CallStaticShortMethodA，CallStaticShortMethodV	|jshort
CallStaticIntMethod，CallStaticIntMethodA，CallStaticIntMethodV	|jint
CallStaticLongMethod，CallStaticLongMethodA，CallStaticLongMethodV	|jlong
CallStaticFloatMethod，CallStaticFloatMethodA，CallStaticFloatMethodV	|jfloat
CallStaticDoubleMethod，CallStaticDoubleMethodA，CallStaticDoubleMethodV	|jdouble



## 4.5 JNI访问Java类方法相关的函数

### 4.5.1 JNI访问Java类的实例方法

`GetObjectClass`函数用来获取Java对象对应的类类型

`GetMethodID`函数用来获取Java类实例方法的方法ID

`Call<Type>Method`函数用来调用Java类实例特定返回值的方法

- 比如CallVoidMethod，调用java没有返回值的方法，CallLongMethod用来调用Java返回值为Long的方法，等等。

 


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




### 4.6.2 JNI访问Java类静态字段

`GetStaticFieldID`函数用来获取Java静态字段的字段ID

`GetStatic<Type>Field`用来获取Java类静态字段的值

- 比如用`GetStaticIntField`函数获取Java 静态int型字段的值

	用`GetStaticLongField`函数获取Java 静态long字段的值

	用`GetStaticObjectField`函数获取Java静态引用类型字段的值

