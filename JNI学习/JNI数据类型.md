# JNI数据类型

[JNI完全指南(一)——数据类型](https://www.zybuluo.com/cxm-2016/note/563686)

[Android NDK 开发（一）JNI简介及调用流程](https://blog.csdn.net/u011974987/article/details/52602913)

# 简介

# 1. 基本数据类型

**Java的基本类型可以直接与`C/C++`的基本类型映射**,因此Java的基本类型对开发人员是透明的

**Java和JNI基本类型对照表如下**:

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



# 2. 引用类型

**Java类的内部数据结构并不直接向原生代码公开,也就是说原生`C/C++`代码并不能直接访问Java代码的字段和方法**

**Java和JNI引用类型对照表如下:**

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



如果使用的语言是C，那么在jni.h中，所有的JNI引用类型都使用如下方式声明：

	typedef jobject jclass;

如果使用的语言是C++，那么在jni.h中，所有的JNI引用类型都使用如下类方式声明：

	class _jobject {};
	class _jclass : public _jobject {};
	// ...
	typedef _jobject *jobject;
	typedef _jclass *jclass;


# 3. 属性和方法的ID

方法和属性的ID 被声明为C指针类型


	struct _jfieldID;                       /* opaque structure */
	typedef struct _jfieldID* jfieldID;     /* field IDs */
	
	struct _jmethodID;                      /* opaque structure */
	typedef struct _jmethodID* jmethodID;   /* method IDs */


# 4. 值类型

`jvalue`共用体类型可以保存多种类型的数据，通常用于数组，其定义如下：

	typedef union jvalue {
	    jboolean    z;
	    jbyte       b;
	    jchar       c;
	    jshort      s;
	    jint        i;
	    jlong       j;
	    jfloat      f;
	    jdouble     d;
	    jobject     l;
	} jvalue;


# 5. 类型签名

Java类型签名映射表

- **JNI获取Java类的方法ID和字段ID，都需要一个很重要的参数，就是Java类的方法和字段的签名，这个签名需要通过下面的表来获取**

Java类型|类型签名
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
任何Java类|L+Java类的完整类名;
type[]| `[type`这个就是Java数组的签名，比如Java `int[]`的签名是`[I`，Java `long[]`的签名就是`[J`，Java `String[]`的签名是 `[Ljava/lang/String`;
方法类型|**（参数类型签名...）返回值类型签名** 

- **任何Java类对应的类型签名示例:**

	比如Java `java.lang.String`类对应的签名是`Ljava/lang/String`;


- **方法类型示例**:

	Java方法`void hello(String msg,String msg2)`对应的签名就是`(Ljava/lang/String; Ljava/lang/String;)V` 

	Java方法`String getNewName(String name)`对应的签名是`（Ljava/lang/String;) Ljava/lang/String` 

	Java方法`long add(int a,int b)`对应的签名是`(II)J`

	Java方法`void delete()`对应的签名是`()V`

- **注意,通过JNI获取Java方法或字段时,基本类型不需要添加`;`符号**


# 6. 使用UTF-8字符串

