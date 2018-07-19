# JNI反射

[JNI完全指南(九)——反射](https://www.zybuluo.com/cxm-2016/note/566621)

# 简介

反射能够使开发者在仅知道名称的情况下就能操作方法和属性。JNI提供了一系列在Java反射的核心方法。


# 1. 将一个Method对象转换为方法ID

	jmethodID FromReflectedMethod(JNIEnv *env, jobject method);

- `method`：`java.lang.reflect.Method`或者`java.lang.reflect.Constructor`对象。通过1.3得到。


# 2. 将一个Field对象转换为属性ID


	jfieldID FromReflectedField(JNIEnv *env, jobject field);

- `field`：`java.lang.reflect.Field`对象，通过1.4得到。


# 3. 反射得到Method对象

以下方法将一个来源于cls类的方法ID转换为`java.lang.reflect.Method`或者`java.lang.reflect.Constructor`对象。

	jobject ToReflectedMethod(JNIEnv *env, jclass cls,jmethodID methodID, jboolean isStatic);

- `cls`：方法所在的类。

- `methodID`：方法ID。

- `isStatic`：是否是静态方法。

- `return`：`java.lang.reflect.Method`或者`java.lang.reflect.Constructor`对象。**失败是返回NULL**。


**存在异常**

- `OutOfMemoryError`

# 4. 反射得到Field对象

以下方法将一个来源于cls类的属性ID转换为java.lang.reflect.Field对象。

	jobject ToReflectedField(JNIEnv *env, jclass cls,jfieldID fieldID, jboolean isStatic);

- `cls`：属性所在的类对象。

- `fieldID`：属性ID。

- `isStatic`：是否是静态属性。

**存在异常**

- `OutOfMemoryError`