# Creating New Gradle Builds

[创建新的Gradle 构建](https://guides.gradle.org/creating-new-gradle-builds/)

[Build Init Plugin ](https://docs.gradle.org/4.10-rc-2/userguide/build_init_plugin.html#sec:build_init_types)

[Android-Script Block-DSL](https://developer.android.com/tools/building/plugin-for-gradle.html)

[Android-Dsl-APi](https://google.github.io/android-gradle-dsl/current/index.html)

[Gradle创建task的教程](https://guides.gradle.org/writing-gradle-tasks/)

# 简介

`Gradle Build Init`插件可用于引导创建新Gradle构建的过程。它支持创建不同类型的全新项目，以及将现有构建（例如，Apache Apache Maven构建）转换为Gradle构建。

`Gradle`插件通常需要在使用之前被应用于项目(参考[Using Plugins](https://docs.gradle.org/4.10-rc-2/userguide/plugins.html#sec:using_plugins)),**但是,`Build Init`插件是一个自动应用的插件,意味着不需要显示的应用它,即可使用**

- 要使用这个插件,只需要执行任务名为`init`的任务,同时该任务还会使用`wrapper`任务去为项目创建`Gradle Wrapper`文件


# 1. 初始化项目


首先,需要创建一个项目的目录. 然后进入到该项目的目录中执行`gradle init`

- 如果需要结合Kotlin DSL使用,那么需要在创建命令后面添加`--dsl kotlin`

		gradle init --dsl kotlin 

- 当命令行中显示`BUILD SUCCESSFUL`,同时出现了如下结构的项目目录时,就表示创建成功

		├── build.gradle  // 用于配置当前项目的 Gradle构建脚本
		├── gradle 
		│   └── wrapper
		│       ├── gradle-wrapper.jar  // Gradle Wrapper可执行Jar
		│       └── gradle-wrapper.properties  // Gradle Wrapper 配置文件
		├── gradlew  // 适用于Unix系统的Gradle Wrapper脚本
		├── gradlew.bat   // 适用于Windows系统的 Gradle Wrapper脚本
		└── settings.gradle  // 用于配置Gradle构建的Gradle设置脚本


## 1.1 Gradle Wrapper

Gradle Wrapper 提供了一个batch文件，当使用脚本时，当前的gradle版本会被下载下来 并使用，避免了开发者去下载不同版本的gradle，解决兼容性！

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
	
	- **可以改变distributionUrl 来改变gradle版本**

## 1.2 Android 项目结构

 	MyApp
		├── build.gradle
		├── settings.gradle
		├── gradlew
		├── gradlew.bat
		├── gradlew/wrapper/.....
		└── app // 模块名称
			├── build.gradle // module下的构建脚本
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


- **setting.gradle**

	这个 setting 文件定义了哪些module 应该被加入到编译过程，对于单个module 的项目可以不用需要这个文件，但是对于 multimodule 的项目我们就需要这个文件，否则gradle 不知道要加载哪些项目。这个文件的代码在初始化阶段就会被执行。

- **根目录的build.gradle**

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

	- **buildscript**:定义了Adnroid编译工具的类路径.repositories中，jCenter是一个仓库

	- **allprojects**:定义的属性会被应用到所有的module中，但是为了保证每个项目的独立性，我们一般不会在这里操作太多共有的东西


- **每个项目单独的build.gradle**:仅针对每个module的配置,这里的配置优先级最高

### 1.2.1 module中的build.gradle介绍

		apply plugin: 'com.android.application'
	
		android {
    		compileSdkVersion 25
    		buildToolsVersion "25.0.3"

    		defaultConfig {
        		applicationId "com.hmt.analytics.customizeplugin"
        		minSdkVersion 16
        		targetSdkVersion 25
        		versionCode 1
        		versionName "1.0"
        		testInstrumentationRunner 	"android.support.test.runner.AndroidJUnitRunner"
    			}

    		buildTypes {
        		release {
            		minifyEnabled false
            		proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        				}
    			}
		}

		dependencies {
    		compile fileTree(dir: 'libs', include: ['*.jar'])
    		compile 'com.android.support:appcompat-v7:25.3.1'
		}

- **apply plugin:**添加了Android程序的gradle插件,plugin提供了Android编译，测试，打包等等task


- **android：** 编译文件中最大的代码块，关于android的所有特殊配置都在这里，这里就是前面plugin所提供的

- **defaultConfig**：程序的默认配置,如何和AndroidMainfest.xml定义了重复的属性，会以这里为主


- **applicationId**:在我们曾经定义的AndroidManifest.xml中，那里定义的包名有两个用途：一个是作为程序的唯一识别ID,防止在同一手机装两个一样的程序；另一个就是作为我们R资源类的包名。在以前我们修改这个ID会导致所有用引用R资源类的地方都要修改。但是现在我们如果修改applicationId只会修改当前程序的ID,而不会去修改源码中资源文件的引用。

- **buildTypes:**定义了编译类型,针对每个类型可以有不同的编译配置,不同的编译配置对应不同的编译命令。默认debug,release


- **dependencies:**属于gradle的依赖配置。定义当前module需要依赖的三方库
	- 引用库时,每个库名称包含三个元素:`组名`：`库名称`：`版本号`
	- 可以通过添加通配符来保证依赖库处于最新状态，但是建议不要这么做，因为这样每次编译都要去请求网络判断是否有最新版本
	- 通过`files()`方法可以添加文件依赖，如果有很多文件，可以通过`fileTree()`方法
- **native libraries**
	配置本地.so库。在配置文件中做如下配置，然后在对应位置建立对应文件夹，并加入对应平台的.so文件即可
		android{
			sourceSets.main{
				jniLibs.srcDir  'src/main/jniLibs'
			}
		}

- **BuildConfig**
	这个类是根据gradle配置文件生成的，其中的参数例如BuildConfig.DEBUG 可以用来判断当前版本是否是debug版本。

	我们可以在defaultConfig中 或buildTypes中具体的类型中 定义一些key-value对，这些key-value对在不同的编译类型的apk下的值不同，例如我们可以为debug,release设置不同的请求地址
		buildTypes{
			debug{
				buildConfigField "String","API_URL","www.google.com"
			}

			release{
				buildConfigField "String","API_URL","www.irs01.com"
			}
		}

	此外还可以为不同编译类型设置不同的资源文件
		buildTypes{
			debug{
				resValue "String","app_name","example_demo"
			}

			release{
				resValue "String","app_name","demo"
			}
		}
		

- **repositories**

	Repositories 就是代码仓库,平时的添加的一些 dependency 就是从这里下载的，Gradle 支持三种类型的仓库：Maven,Ivy和一些静态文件或者文件夹。在编译的执行阶段，gradle 将会从仓库中取出对应需要的依赖文件，当然，gradle 本地也会有自己的缓存，不会每次都去取这些依赖。

	gradle支持多种Maven仓库，一般为公共的Jcenter，可以通过手动添加一些私人库并添加账号密码
		repositories{
			maven{
				url "http://repo.xxx.xx/maven"
				creadentials{
					username 'user'
					password 'password'
				}
			}
		}

	也可以使用相对路径配置本地仓库，可以通过配置项目中存在的静态文件夹作为本地仓库
		repositories{
			flatDir{
				dirs 'aars'
			}
		}


- **library projects**
	需要写一个library项目给其他项目引用，那么apply plugin 就需要改成 'com.android.library',另外还需要在setting.gradle 中include。 默认生成的话as会做好这些

	如果不方便直接引用module ，可以将module打包成aar形式，然后通过文件的形式引用。这种情况需要在项目下新建aars文件夹，并在根目录下的build.gradle配置**本地仓库**，然后在dependencies中添加`compile name:'libraryname',ext:'aar'`

- **build variants-build type**
	在编译的时候动态根据当前的编译类型输出不同样式的apk文件等情况时就需要用到了buiildtypes

		buildTypes{
			staging.initWith buildTypes.debug
			staging{
				applicationIdSuffix '.staging'
				versionNameSuffix '-staging'
				debuggable = false
			}
		}
- **Source sets**
	每当新建一个build type时，gradle默认会创建一个新的source set。可以建立与`main`同级的文件夹，这样在编译时 会根据不同的编译类型 选择某些对应文件夹下的源码。不止文件夹可以替换，资源也可以替换

	另外dependencies中也可以通过 `buildType+compile` 来指定 编译类型去添加指定三方框架


- **product flavors**
	如果我们需要针对同一份代码编译不同的程序(包名不同)，就需要`product flavors`
	- 注意product flavors和build type是不一样的，而且他们的属性也不一样。所有的product flavor和defaultConfig共享属性

	像Build type 一样，product flavor 也可以有自己的source set文件夹。除此之外，product flavor 和 build type 可以结合，他们的文件夹里面的文件优先级甚至高于 单独的built type 和product flavor 文件夹的优先级。如果你想对于 blue类型的release 版本有不同的图标，我们可以建立一个文件夹叫blueRelease，注意，这个顺序不能错，一定是 flavor+buildType 的形式。

	更复杂的情况下，我们可能需要多个product 的维度进行组合，比如我想要 color 和 price 两个维度去构建程序。这时候我们就需要使用flavorDimensions：

			android{
				flavorDimensions 'color','price'

				productFlavors{
					red{
						flavorDimension 'color'
					}
					blue{
						flavorDimension 'color'
					}
					free{
						flavorDimension 'price'
					}
					paid{
						flavorDimension 'price'
					}
				}
			}
	这样gradle会自动进行组合，得出类似blue+free+debug blue+paid+release red+free+debug red+paid+release

	BuildType中定义资源优先级最大，Library中定义的资源优先级最低

- **signing configurations**
	首先我们需要在android{}中配置
		android{
			signingConfigs{
				storeFile file("release.jks")
				storePassword "password"
				keyAlias 'rellease-jks'
				keyPassword "123456"
			}
		}

	配置之后需要在build type中使用
		buildTypes{
			release{
				signingConfig signingConfigs.release
			}
		}


### 1.2.2 Reducing apk file
在编译的时候，有许多的资源并没有用到，可以通过`shrinkResources`来优化资源文件，除去不必要的资源。

	android{
		buildTypes{
			release{
				minifyEnabled = true //只有当俩者都为true 才会真正的删除无效代码和无银用资源
				shrinkResources = true//
			}
		}
	}

在某些情况下，一些资源是通过动态加载的方式载入的，这时候需要像Progard一样对资源进程**keep**操作。操作方式就是：在`res/raw/`下创建一个`keep.xml`文件，使用如下方式keep资源

	<?xml version="1.0" encoding="utf-8"?>  
	<resources xmlns:tools="http://schemas.android.com/tools"  tools:keep="@layout/activity_four,@drawable/no_reference_but_keep"/>  

	

对于一些尺寸文件，我们可以这么做去防止被去除：

	android{
		defaultConfig{
			resConfigs "hdpi","xhdpi","xxhdpi"
		}
	}
	

# 2. 关于Task

项目本质上是Task对象的集合,每个任务执行一些基本工作,例如编译类,运行单元测试等.

Gradle提供了API,用于创建和配置任务. 

- 通过`TaskContainer`这个task管理类,可以实现创建和查找任务. 例如`TaskContainer.create(java.lang.String)`,`TaskContainer.getByName(java.lang.String)`

- `TaskContainer`负责管理Task实例.通过`Project.getTasks()`或构建脚本中的`tasks`属性,可以获取到`TaskContainer`实例.

每个任务都有一个名称,可以任务所在的项目中被用来引用. 

任务路径由 **项目的路径 + ":"+ 任务名称** 组成

## 2.1 Task介绍

>Task 是Gradle中的一种数据类型，代表了一些要执行或todo的工作。不同插件可以添加不同的Task。
>每一个Task都需要和一个Project关联

- 一个Task包含若干Action.所以Task提供了`doFirst`和`doLast`俩个函数 方便开发者使用，这俩个函数分别是用于最先执行的和最后执行的action。

- Task创建的时候可以指定Type，通过`type:typeName`表达。作用就是告诉Gradle，该Task是从哪个基类Task 派生。 则新建的Task也具有基类Task的功能。例如：`task mTask(type:Copy)`，mTask也是一个Copy类型的task

-  **`task mTask{configure closure}`。花括号代表一个Closure，会在Gradle创建这个Task之后返回给用户之前，先执行这个Closure的内容,这个Closure可以被当做一个配置项的存在，去做一些配置，例如设置分组，添加描述等**


- 通过`group`设置分组，通过`description `设置描述

		task taskB{
			group = 'test'
			description = 'desc'
		}
	
		taskA.group = 'test'
		taskA.description = 'desc'


## 2.2 定义Task

Task是和Project关联的，Project中提供了`task()`函数在构建文件中创建一个Task  

`Project`的task函数

返回值|函数名(参数)|功能描述
---|---|---
Task	|task​(String name)	|Creates a Task with the given name and adds it to this project.
Task	|task​(String name, Closure configureClosure)	|Creates a Task with the given name and adds it to this project.
Task	|task​(String name, Action<? super Task> configureAction)	|Creates a Task with the given name and adds it to this project.
Task	|task​(Map<String,​?> args, String name)	|Creates a Task with the given name and adds it to this project.
Task	|task​(Map<String,​?> args, String name, Closure configureClosure)|Creates a Task with the given name and adds it to this project.
		
- `Map<String,?> args`参数是任务创建时的参数,用来控制任务的一些属性


选项	|描述	|默认值
---|---|---
"type"	|The class of the task to create.	|DefaultTask
"overwrite"	|Replace an existing task?	|false
"dependsOn"	|A task name or set of task names which this task depends on|	[]
"action"	|A closure or Action to add to the task.|	null
"description"	|A description of the task.	|null
"group"	|A task group which this task belongs to.	|null



在创建task时，通常可以传入一个`Closure`,**这个Closure是用来配置task的，会在task返回之前执行。**

	task myTask  // myTask是新建Task的名字  
		
	task myTask { configure closure } //closure用来设置配置
		
	task myType << { task action } // 注意，<<符号是doLast的缩写 ，用来添加action
		
	task myTask(type: ParentType)  // ParentType 是当前任务的父类型
		
	task myTask(type: ParentType) { configure closure }

	task('task1')<< { println 'task1 is created'}

	task(task2,type:Copy){
		from 'xxxx'
		into 'yyyy'
	}

	task task3<<{println 'task3 is created'}

**Task另外一种创建方式,通过`TaskContainer`实现**

		tasks.create(name:'task4'){
			group 'test'
			description 'i am task4'
			doLast{
				println 'task4 is created'
			}	
		}
		
		tasks.create(name:'task5',type:Copy){
			group 'test'
			from 'xxx'
			into 'yyy'
		}
	
		// 实际上是通过TaskContainer创建
		TaskContainer getTasks();


## 2.3 任务的Actions

一个任务是由一连串的`Action`对象组成,当任务被执行时,每一个`Action`对象都会按顺序执行(调用其`execute(T)`方法)

可以通过`Task.doFirst(org.gradle.api.Action)`或者`Task.doLast(org.gradle.api.Action)`

- `Groovy`中的`Closure`可以用来被当做一个任务的Action被添加至任务中. 通过`Task.doFirst(groovy.lang.Closure) `或者`Task.doLast(groovy.lang.Closure)`方法

	`task mTask<<{xxx}`,意思是把`closure`作为一个Action添加到Task的Action队列，并且最后才去执行它(`<<`符号是doLast的代表,已经在Gradle5.0 中被移除)

- doLast的快捷键`<<`,会在Gradle5.0中遗弃

## 2.4 终止任务而不报错

通过抛出`StopActionException`可以实现结束当前Action的执行并开始下一个Action的执行

通过抛出`StopExecutionException`可以实现结束当前Task,并开始下一个Task的执行


	taskA.doFirst{
		throw new StopExcutionException()
	}


## 2.5 Task依赖和Task顺序

一个任务可能依赖于其他的任务,或者被安排为总是在另外一个任务之后执行,Gradle会确保在执行任务时遵守所有任务依赖性和排序规则,以便在所有依赖项和`must run after`任务执行之后再执行

任务被设置成依赖于另外一个任务.有俩种方式: 

1. 通过 `Task.dependsOn(java.lang.Object[])`或者 `Task.setDependsOn(java.lang.Iterable)`

2. 通过创建任务时,传入指定参数`dependsOn`

		task funcX()

		task funcY(dependsOn:funcX)		

任务可以被指定执行顺序

1. 通过 `Task.mustRunAfter(java.lang.Object[])`, `Task.setMustRunAfter(java.lang.Iterable)`, `Task.shouldRunAfter(java.lang.Object[]) `and `Task.setShouldRunAfter(java.lang.Iterable)`



依赖有一个特殊用法,**`Lazy DependsOn`,即task1依赖task2时,可以在task2定义之前**

	task funcX(dependsOn:funcY)<<{
	}

	task funcY()<<{
	}

Gradle可以动态创建 Task

	4.times{
		task "task$it"{
			doLast{
				println "i am task $it"
			}
		}
	}

创建任务之后,可以在运行时动态添加依赖关系

	task0.dependsOn(task1,task2,task3)

可以为task添加来自其他project的依赖
	
	project(':moduleA'){
		task task1(dependsOn:':moduleB:tasks2')<<{
			println 'moduleA task1 is run'
		}
	}

	project(':moduleB'){
		task task2<<{
			println 'moduleB task2 is run'
		}
	}

依赖可以使用一个闭包来返回Task

	task6.dependsOn {
		tasks.findAll{
			it.name.startWidth('task')
		}
	}


## 2.6 设置默认Task

Gradle可以通过 `defaultTasks 'tasks1','tasks2'`来设置默认执行的task(当没有其他task明确被执行时),例如:`gradle -q`时，会去执行task `clean`
		
	defaultTasks 'clean'

	task clean<<{
		println 'default cleaning'
	}

## 2.7 Task额外属性
通过`ext.xxxx`来替task设置额外属性

	task func{
		ext.nameProperty = 'ryan'
	}

	task func1<<{
		println "hello my name is $func.nameProperty"
	}

## 2.8 Task在构建文件中的使用

通过`tasks.getByPath()`方法 来获取任务.可以使用任务名称,相对路径,绝对路径调用该方法

	project(':moduleA'){
		task taskA
	}

	println tasks.getByPath('tasksA').path
	println tasks.getByPath(':script:tasksA').path

可以将Task作为属性来使用

	println task1.name
	println project.task1.name

可以通过TaskContainer来访问Task
		
	println tasks.tasks1.name
	println tasks['tasks1'].name

### 2.8.1 动态属性

任务拥有属性,可以通过属性名称或者调用`Task.property(java.lang.String)`方法进行获取. 另外可以通过`Task.setProperty(java.lang.String,java.lang.Object)`方法进行修改.


### 2.8.2 动态方法

插件会通过它的`Convention`对象给Task添加方法


### 2.8.3 平行执行

默认情况下,任务不允许平行执行,但也有一些特殊情况. 可以通过添加`--parallel`标志开启平行执行


## 2.9 配置Task

- 方式1:

		Copy copy1 = task(task10,type:Copy)
		copy1.from '/'
		copy1.into 'task10'
		copy1.include('**.txt')

- 方式2:
		task task11(type:Copy)

		task11{
			from '/'
			into 'task11'
			include '**.txt'
		}

- 方式3:
		task (task12,type:Copy){
			from '/'
			into 'task12'
			include '**.gradle'
		}



## 2.10 覆盖任务

可以通过`overwrite`覆盖任务，如果任务已经存在且不添加`overwrite`,会抛出一个异常，表示任务已经存在

	task taskA<<{
		println 'hello'
	}

	task taskA(overwrite:true)<<{
		println 'overwrite hello'
	}

## 2.11 设置任务执行条件

	task taskA<<{
		println 'hello gradle'
	}

	tasksA.onlyIf{
		!project.hasProperty('xxxx')
	}

## 2.12 定义任务类型

	class Greeting extends DefaultTask {  
	    String message 
	    String recipient
	
	    @TaskAction 
	    void sayGreeting() {
	        println "${message}, ${recipient}!" 
	    }
	}
	
	tasks.register("hello", Greeting) { 
	    group = 'Welcome'
	    description = 'Produces a world greeting'
	    message = 'Hello' 
	    recipient = 'World'
	}


# 3. 应用插件

Gradle包含一系列的插件,更多可用的插件可以在[Gradle插件门户网站](https://plugins.gradle.org/)

例如,`base`插件中包含一个核心类型`Zip`,利用这个`Zip`类型去创建一个压缩的task

1. 首先需要使项目应用`base`插件

		plugins {
		    id "base"
		}

2. 编写task

		task zip(type: Zip, group: "Archive", description: "Archives sources in a zip file") {
		    from "src"
		    setArchiveName "basic-demo-1.0.zip"
		}

- 将`src`目录下的文件,压缩生成一个档案文件,放在`build/distributions`文件夹下