# Gradle-Plugin 学习


# 1. 简介

Android Studio 中，Gradle-Plugin 和Gradle 是不同的概念

- Gradle是一个基于Apache Ant和Apache Maven概念的项目自动化建构工具，它使用一种基于Groovy的特定领域语言来声明项目设置

- Gradle-Plugin 是Google使用Gradle开发出的一个Android插件，该插件为Android Studio提供了项目的管理，例如，添加项目依赖，打包，签名等流程的定义

	

# 2. 源码下载方法

从`gradle-plugin 3.0.0` 开始，Google 将Android的一些库放到自己的`Google()`仓库里了。地址如下：

	https://dl.google.com/dl/android/maven2/index.html

但是`Google()`并没有提供文件遍历功能，所以无法直接访问路径去下载。但是实际上源码还是在那个路径下放着，所以只需要输入待下载文件的完整的路径即可下载。

例如：需要下载`gradle-3.0.0-sources.jar` ,只需要将完整的路径输入即可

	https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/3.0.0/gradle-3.0.0-sources.jar

**下载完源文件之后放入指定gradle目录下,即可在`Android Studio中`查看对应的源码**

- gradle目录可以通过AS 得知...

- 示例

		/Users/ryan/.gradle/caches/modules-2/files-2.1/com.android.tools.build/gradle/3.3.2/
		
