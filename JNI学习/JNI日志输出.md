# JNI日志输出
[JNI输出Android日志](https://blog.csdn.net/kgdwbb/article/details/72852628)

# 简介

Android提供了 头文件`android/log.h`,用来在`C/C++`的代码中输出Android日志


	#include<android/log.h>

# 1. 使用方式


	int __android_log_write(int prio, const char *tag, const char *text);
	 
	int __android_log_print(int prio, const char *tag,  const char *fmt, ...)
	 
	int __android_log_vprint(int prio, const char *tag, const char *fmt, va_list ap);


- `prio`参数代表日志的优先级，对应于android_LogPriority结构体

- `tag`参数就是日志的Tag

- `text`参数就是要输出的日志的文件

- `fmt`参数就是要输出的日志的格式

- `...`和`ap`参数就是要输出的日志的参数列表

# 2. 日志级别

	typedef enum android_LogPriority {
	
	    ANDROID_LOG_UNKNOWN = 0,
	
	    ANDROID_LOG_DEFAULT,    /* only for SetMinPriority() */
	
	    ANDROID_LOG_VERBOSE,
	
	    ANDROID_LOG_DEBUG,
	
	    ANDROID_LOG_INFO,
	
	    ANDROID_LOG_WARN,
	
	    ANDROID_LOG_ERROR,
	
	    ANDROID_LOG_FATAL,
	
	    ANDROID_LOG_SILENT,     /* only for SetMinPriority(); must be last */
	
	} android_LogPriority;


常用的NDK日志级别：

- `ANDROID_LOG_VERBOSE`对应Java的`Log.v()`函数

- `ANDROID_LOG_DEBUG` 对应Java的`Log.d()`函数

- `ANDROID_LOG_INFO` 对应Java的`Log.i()`函数

- `ANDROID_LOG_WARN`对应Java的`Log.w()`函数

- `ANDROID_LOG_ERROR` 对应Java的`Log.e()`函数

- `ANDROID_LOG_FATAL` 这个是最严重的致命级别的错误，Java没有对应的日志函数和它对应，这个就相当于增强版的Log.e()函数


# 3. 日志输出宏

使用`#define`预处理指定创建符号常量

	
	const char *TAG = "TEST--->";
	
	#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)

	//调用方式
	LOGE("THIS IS A SAMPLE");

- C++编译器会在编译时,将LOGE替换成`__android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)`