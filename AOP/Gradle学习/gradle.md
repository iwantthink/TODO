# Gradle学习

## 1.Gradle基本概念
- build.gradle 又被称作构建脚本

- gradle为我们提供了许多默认的配置以及通常的默认值

- Gradle是一种基于Groovy的动态DSL，而Groovy是一种基于jvm的动态语言

### 1.1 Project和tasks
- 每个项目的编译至少有一个project

- 每个project至少有一个task,task里面又包含了很多action，action就是一个代码块里面包含了需要被执行的代码

- 每一个build.gradle代表一个project

- task在build.gradle中定义


### 1.2 Gradle编译
在编译过程中， Gradle 会根据 build 相关文件，聚合所有的project和task，执行task 中的 action。因为 build.gradle文件中的task非常多，先执行哪个后执行那个需要一种逻辑来保证。这种逻辑就是依赖逻辑，几乎所有的Task 都需要依赖其他 task 来执行，没有被依赖的task 会首先被执行。所以到最后所有的 Task 会构成一个 有向无环图（DAG Directed Acyclic Graph）的数据结构。

编译过程分为三个阶段：

- 初始化阶段：创建 Project 对象，如果有多个build.gradle，也会创建多个project.
- 配置阶段：在这个阶段，会执行所有的编译脚本，同时还会创建project的所有的task，为后一个阶段做准备。
- 执行阶段：在这个阶段，gradle 会根据传入的参数决定如何执行这些task,真正action的执行代码就在这里.


### 1.3 项目结构

 	MyApp
		├── build.gradle
		├── settings.gradle
		└── app
			├── build.gradle
			├── build
			├── libs
			└── src
				└── main
               	├── java
               	│   └── com.package.myapp
               	└── res
                   	├── drawable
                   	├── layout
                   	└── etc.

### 1.4 Gradle Wrapper
Gradle Wrapper 提供了一个batch文件，当使用脚本时，当前的gradle版本会被下载下来 并使用，避免了开发者去下载不同版本的gradle！

	 myapp/
   	├── gradlew
   	├── gradlew.bat
   	└── gradle/wrapper/
       	├── gradle-wrapper.jar
       	└── gradle-wrapper.properties

- bat文件针对window系统，shell脚本针对mac系统，一个jar文件，一个配置文件。配置文件如下：  

		#Sat May 30 17:41:49 CEST 2015
   	distributionBase=GRADLE_USER_HOME
   	distributionPath=wrapper/dists
   	zipStoreBase=GRADLE_USER_HOME
   	zipStorePath=wrapper/dists
   	distributionUrl=https\://services.gradle.org/distributions/
   	gradle-2.4-all.zip
	
	可以改变distributionUrl 来改变gradle版本

### 1.5 基本构建命令

- 获取所有有分组的可运行tasks，可以添加--all参数  来查看所有task
		gradlew tasks

- 创建一个指定版本的apk，
		gradlew assembleXXX



### 1.6 构建脚本的构成

	MyApp
   	├── build.gradle
   	├── settings.gradle
   	└── app
       	└── build.gradle

- setting.gradle 
	这个 setting 文件定义了哪些module 应该被加入到编译过程，对于单个module 的项目可以不用需要这个文件，但是对于 multimodule 的项目我们就需要这个文件，否则gradle 不知道要加载哪些项目。这个文件的代码在初始化阶段就会被执行。

- 根目录的build.gradle
	顶层的build.gradle文件的配置最终会被应用到所有项目中。它典型的配置如下：
		buildscript {
    		repositories {
        		jcenter()
    		}

    		dependencies {
        		classpath 'com.android.tools.build:gradle:1.2.3'
    		}
		}

		allprojects{
    		repositories{
        		jcenter()
    		}
		}

	- buildscript:定义了Adnroid编译工具的类路径.repositories中，jCenter是一个仓库
	- allprojects:定义的属性会被应用到所有的module中，但是为了保证每个项目的独立性，我们一般不会在这里操作太多共有的东西


- 每个项目单独的build.gradle:仅针对每个module的配置,这里的配置优先级最高

		apply plugin: 'com.android.application'
	