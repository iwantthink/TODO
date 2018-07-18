# JNI数据类型

[JNI完全指南(一)——数据类型](https://www.zybuluo.com/cxm-2016/note/563686)

[Android NDK 开发（一）JNI简介及调用流程](https://blog.csdn.net/u011974987/article/details/52602913)

# 简介

# 1. 基本数据类型

**Java的基本类型可以直接与`C/C++`的基本类型映射**,因此Java的基本类型对开发人员是透明的

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






# 3. Java和JNI类型对照表

## 3.1 Java和JNI基本类型对照表


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
