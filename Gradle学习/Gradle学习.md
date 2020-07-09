[Gradlede 的版本库](http://services.gradle.org/distributions/)

[Gradle 官网](https://gradle.org/)

[Gradle DSL 教程](https://docs.gradle.org/current/dsl/)

[深入理解Android之Gradle](http://blog.csdn.net/innost/article/details/48228651)

[Gradle之完整指南](http://www.jianshu.com/p/9df3c3b6067a)

[Gradle-旧版本文档](https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Advanced-Build-Customization)


[Gradle深入与实战（六）Gradle的背后是什么？](http://benweizhu.github.io/blog/2015/03/31/deep-into-gradle-in-action-6/)

[Gradle构建源码浅析](https://blog.csdn.net/yanbober/article/details/60584621)

[【Android 修炼手册】Gradle 篇 -- Gradle 的基本使用](https://zhuanlan.zhihu.com/p/65249493)

[Gradle基础 构建生命周期和Hook技术](https://juejin.im/post/5afec54951882542715001f2)

# Gradle?

	Gradle is an open-source build automation tool focused on flexibility and performance.
	Gradle build scripts are written using a Groovy or Kotlin DSL

Gradle可以从三个方面理解:

1. Gradle 是一个使用Task进行自动化构建的工具

	Gradle通过组织一系列的task来完成自动化构建(task是gradle中最重要的概念).
	
	例如apk的生成流程，需要 资源处理->Javac编译->Dex编译->Aapt打包->签名..每个具体的步骤就对应到Gradle的某个task

	- 注意:**Gradle是一个构建工具，它负责定义流程和规则**,而具体的工作则是通过所使用的插件来完成

2. Gradle 脚本使用Groovy DSL/Kotlin DSL

	DSL 即Domain Specific Language,与之对应的是GPL(General-Purpose Language 例如Java)。DSL使用简单,定义简洁,比起配置文件，DSL又可以实现语言逻辑
	
	以`android{}`为例，这本身是一个函数调用，参数是一个Closure
	
3. Gradle 脚本本质上即类定义，配置项即方法调用,参数是Closure

	Gradle 基于groovy或kotlin编写,因此本质上是面向对象语言,特点就是一切皆对象.
	
	以`rootProject/build.gradle`为例,其对应Project类,其`buildScript`块对应`Project.buildScript()`方法	

- Groovy语言的特点就是其可以较好的支持DSL

	DSL是介于配置文件和编程语言之间的一种以高效的方式在特定领域中描述对象的语言 ,此外DSL是为了跟一部分人交流而不是像GPL是为了和所有人交流。
	
	比如我做后端的, 我可能就不了解CSS这种前端的东西, 这个时候CSS就是一种DSL, 他只是在前端工程师和UI/UE工程师之间用来交流

# Gradle项目的创建?

`Gradle Build Init`插件可用于引导创建新Gradle构建的过程。它支持创建不同类型的全新项目，以及将现有构建（例如，Apache Apache Maven构建）转换为Gradle构建

- `Gradle`插件通常需要在使用之前被应用于项目(参考[Using Plugins](https://docs.gradle.org/4.10-rc-2/userguide/plugins.html#sec:using_plugins)),**但是,`Build Init`插件是一个内置的插件,意味着不需要显示的应用它,即可使用**

- 要使用这个插件,只需要执行任务名为`init`的任务,同时该任务还会使用`wrapper`任务去为项目创建`Gradle Wrapper`文件

创建一个Gradle项目,需要创建并进入到该项目的目录中执行`gradle init`

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

# 构建?

构建，可以叫做build或make. 就是根据输入信息然后干一堆事情，然后得出几个产出物。

Gradle 就是一个构建工具

- 发展历史从`ANT->MAVEN->GRADLE`

学习Gradle需要掌握俩个点：

1. Groovy的语法

2. Gradle的语法


# Gradle参数优先级

[Build Environment Parameter Priority](https://docs.gradle.org/current/userguide/build_environment.html)


1. 命令行标志
2. 系统属性
3. Gradle属性
4. 环境变量 


# 1. 构建脚本结构

Gradle的构建脚本可以由>=0条的语句或脚本块(script block)组成

- 语句可以包括方法调用,属性赋值 和 局部变量定义

- 脚本块就是一种方法调用,其将Closure当做参数

	闭包被当做一种在执行时会配置一些代理对象的配置闭包,

	[顶级脚本块地址-点我](https://docs.gradle.org/current/dsl/)


# 2. 构建脚本中Groovy相关的特性

**由于Gradle构建脚本也是一个Groovy脚本,因此其可以包含许多Groovy脚本的元素,例如 方法定义和类定义等等**

**方法中最后一个参数为Closure时，可以把闭包放在方法调用之后！（这是groovy特性）**
		
		//方法定义，以下三种方法都是一样的效果
		def method(Closure cl){
			cl()
		}
		//调用方式1
		method(){	
			println 'method--1'
		}
		//调用方式2
		method({println 'method--2'})
		//调用方式3
		method{println 'method--3'}


**Groovy支持函数调用传入`map`,会自动转换格式**

	apply plugin:'com.android.library'

- `参数名1：参数值1，参数名2：参数值2` 会被转成map


根据Groovy的语法，一个Property会自动生成get/set方法

局部变量 用def 声明，且只能在被定义的地方可见(Groovy特征)

# 3. Gradle工作流程

在编译过程中， Gradle 会根据 build 相关文件，聚合所有的project和task，执行task 中的 action。因为` build.gradle`文件中的task非常多，先执行哪个后执行那个需要一种逻辑来保证。这种逻辑就是依赖逻辑，几乎所有的Task 都需要依赖其他 task 来执行，没有被依赖的task 会首先被执行。所以到最后所有的 Task 会构成一个 有向无环图（DAG Directed Acyclic Graph）的数据结构。

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fjnug3z25hj20q7065t9d.jpg)

## 3.1 编译过程分为三个阶段

	//settings.gradle文件
	println "this is executed during the initialization phase"
	
	//rootProject/build.gradle文件
	task configured{

		println "this is executed during the Configuration phase"
	
		doLast{
			println "this is executed during the Execution phase"
		}
	
		doFirst{
			println "this is executed during the Execution phase"
		}
	}

1.  **初始化阶段**：Gradle支持单项目和多项目构建.在初始化阶段,Gradle将会确定哪些项目参与构建,并为每个项目创建一个`Project`对象. 

	解析`settings.gradle`文件.**为每个项目配置`Project` 对象**，如果有多个项目，也会创建多个`Project`对象.

	- Hook: `gradle.beforeProject{project->}`

2.  **配置阶段**：在这个阶段，会解析每个`build.gradle`对Project进行配置，同时还会创建待执行的task，并创建一个有向图来描述Task之间的依赖关系(用于解决执行过程中的任务依赖关系) . 此外作为构建一部分的构建脚本会被执行

	- Hook: 
		
			gradle.taskGraph.whenReady{graph->
				println "task 图构建完成时调用"
			}
			gradle.taskGraph.beforeTask{
				println "每个task 执行前会调用"
			}
			gradle.taskGraph.afterTask{
				println "每个task执行完成后会调用"
			}

3.  **执行阶段**：在这个阶段，gradle 会根据传入的参数决定如何执行这些task（在配置阶段被创建和配置的,真正action的执行代码就在这里.

	Gradle会将这个任务链上的所有任务按依赖顺序执行一遍

	- Hook: `gradle.buildFinished{result ->}`

## 3.2 构建监听


Gradle的构建过程中，会调用一些回调接口

	gradle.addBuildListener(new BuildListener() {
	    @Override
	    void buildStarted(Gradle gradle) {
	        println('构建开始')
	        // 这个回调一般不会调用，因为我们注册的时机太晚，注册的时候构建已经开始了，是 gradle 内部使用的
	    }
	
	    @Override
	    void settingsEvaluated(Settings settings) {
	        println('settings 文件解析完成')
	    }
	
	    @Override
	    void projectsLoaded(Gradle gradle) {
	        println('项目加载完成')
	        gradle.rootProject.subprojects.each { pro ->
	            pro.beforeEvaluate {
	                println("${pro.name} 项目配置之前调用")
	            }
	            pro.afterEvaluate{
	                println("${pro.name} 项目配置之后调用")
	            }
	        }
	    }
	
	    @Override
	    void projectsEvaluated(Gradle gradle) {
	        println('项目解析完成')
	    }
	
	    @Override
	    void buildFinished(BuildResult result) {
	        println('构建完成')
	    }
	})
	
	gradle.taskGraph.whenReady {
	    println("task 图构建完成")
	}
	gradle.taskGraph.beforeTask {
	    println("每个 task 执行前会调这个接口")
	}
	gradle.taskGraph.afterTask {
	    println("每个 task 执行完成会调这个接口")
	}
# 4. Gradle编程模型

[Gradle Build Language Reference,介绍Gradle的模型](https://docs.gradle.org/current/dsl/)

`Gradle Script`是配置型脚本,当Gradle脚本在被执行时,根据脚本的类型会配置一个特定类型的对象作为脚本的代理对象(`delegate object`)

例如,当`rootProject/build.gradle`执行时,会配置一个`Project`类型的对象,作为该脚本的委托对象

下图展示了不同类型的Gradle脚本对应的代理类型:

Type of Script | Delegates to instance of
---|---
Build script | Project
Init script | Gradle
Settings script | Settings

- `build.gradle` 又被称作构建脚本

不同类型的Gradle脚本有不同的委托对象,委托对象的属性和方法可以在脚本中使用

每个构建脚本对象都实现了`Script`接口,该接口定义了许多可以在脚本中使用的属性和方法

**Gradle主要有三种对象，这三种对象和三种不同的脚本文件对应，在gradle执行的时候，会将脚本转换成对应的对象：**

- **Gradle对象**：当我们执行gradle xxx或者什么的时候，gradle会根据`init.gradle`构造出一个Gradle对象。**在整个执行过程中，只有这么一个对象**。Gradle对象的数据类型就是Gradle(一般很少去定制这个默认的配置脚本)。

- **Setting对象**：每一个`settings.gradle`都会配置一个[Settings](https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html)委托对象

- **Project对象**：每一个`build.gradle`都会配置一个Project委托对象。

>**对于其他的`.gradle`文件，除非定义了class,否则会转换成一个实现了Script接口的对象(与Groovy类似)**

## 4.1 Gradle对象

Gradle对象是根据`init.gradle`生成的,并且整个Gradle构建执行过程中,只有一个`Gradle`对象,可以在其中做一些整体初始化的操作，例如配置log输出等

`init.gradle`的使用方法如下,如果存在多个文件，那么将按照下面方法的顺序进行执行(在同一目录下的文件会按照文件名称顺序执行)

1. 通过命令行中指定文件地址。命令行参数`--init-script`或`-I` +`init.gradle`文件地址

2. 将文件名为`init.gradle`的文件放在`USER_HOME/.gradle/`目录下

3. 将后缀为`.gradle`的文件放在`USER_HOME/.gradle/init.d`目录下

4. 将后缀为`.gradle`的文件放在`GRADLE_HOME/init.d/`目录下

与普通的Gradle构建脚本一样，其拥有一个Gradle类型的委托对象,并且在脚本中不存在的方法或属性都会被代理到该对象(同时也实现了Script接口)

测试代码:

	//在settings.gradle中，则输出"In settings,gradle id is"  
	println "In settings.gradle, gradle id is " +gradle.hashCode()  
	println "Home Dir:" + gradle.gradleHomeDir.path  
	println "User Home Dir:" + gradle.gradleUserHomeDir.path  
	println "Parent: " + gradle.parent  

- `settings.gradle`和rootProject/module下的`build.gradle`的构建脚本得到的gradle实例对象是一样的(根据hashCode判断)

- **Gradle对象 默认是Settings和Project的成员变量.可以直接通过`gradle`获取 或者`getGradle()`获取**

- `Gradle`对象的有用多种属性,[Gradle文档](https://docs.gradle.org/current/dsl/org.gradle.api.invocation.Gradle.html)

	- `gradle.gradleHomeDir` : `Gradle`可执行程序的路径

	- `gradle.gradleUserHomeDir` : `Gradle`配置的目录,存储了一些配置文件,以及编译过程中的缓存文件，生成的类文件，编译过程中依赖的插件等




## 4.2 Setting对象
`settings.gradle`是负责配置项目的脚本,其对应Settings代理对象

主要方法有:

	include(projectPath)
	includeFlat(projectNames)
	project(projectDir)

## 4.3 Project对象
Project对象是根据`build.gradle`生成的，因此写在`build.gradle`中的dsl,实际上大多是对Project接口中方法的调用(Project实际上是一个接口，其真正实现类是`DefaultProject`)

- `Project`接口作为主要API,用于构建文件与Gradle的交互.通过`Project`,可以访问Gradle的所有功能,

- Project包含若干个task,task里面又包含了若干action，action就是一个代码块，里面包含了需要被执行的代码

	task在build.gradle中被定义

- **`project`和`build.gradle`是一种一对一的关系**

- 根据Gradle的要求，每一个Project在其根目录下都需要有一个`build.gradle`

- 由于`Project`对应具体的工程，所以需要为`Project`加载所需要的插件，比如为Java工程加载Java插件。其实**一个Project包含多少Task往往是插件决定的**。

- `build.gradle`中所有未定义的方法/属性，都会委派给委托对象(即`Project`)去使用

		 defaultTasks('some-task')  // Delegates to Project.defaultTasks()
		 reportsDir = file('reports') // Delegates to Project.file() and the Java Plugin

**通常Project需要执行的内容：**

1. 加载插件

	通过`Project`对象的`apply(key:value)`函数来加载插件，`apply plugin:'com.android.library'`
	
	- 除了加载二进制文件,还可以加载gradle文件

		1. **from**: 被添加的脚本. Accepts any path supported by Project.uri(java.lang.Object).
		
		2. **plugin**: Plugin的Id或者是插件的具体实现类 
	
		3. **to**: The target delegate object or objects. The default is this plugin aware object. Use this to configure objects other than this object.
	
2. 配置插件。例如设置哪里读取源文件。
	
3. 设置属性

	- 如果是单个脚本，则不需要考虑属性的跨脚本使用。但是Gradle往往包含不止一个`build.gradle`文件！例如,`build.gradle`,`settings.gralde` 和自定义的`build.gradle`.**Gradle提供了一种名为`extra property`的方法**

	- **`extra property`是额外属性的意思**，在第一次定义该属性的时候需要通过`ext`前缀来标示它是一个额外的属性。定义好之后，后面的存取就不需要ext前缀了。**ext属性支持Project和Gradle对象即Project和Gradle对象都可以设置ext属性**

	- 属性值可以从`local.properties`中读取

			Properties p = new Properties()
			File pF = new File(rootDir.getAbsolutePath()+'/local.properties')
			properties.load(pF.newDataInputStream())

	- **可以直接获取`ext`前缀，表明操作的是外置属性**.定义属性或设置属性时需要ext前缀。读取时就不需要ext前缀了

			gradle.ext.api = p.getProperty('sdk.api')
			println gradle.api 

	除了`ext.xxx=value`这种定义方式之外，还可以使用`ext{}`这种书写方式。**ext{}不是ext函数传入Closure，但是ext{}中的{}的确是`Closure`**

			ext{
				    getVersionNameAdvanced = this.&getVersionNameAdvanced  
			}

	- **加载`utils.gradle`的Project对象**和**`utils.gradle`对象本身所代表的Script对象**的关系。

		- 当一个Project apply一个gradle文件时，这个gradle文件会转换成一个Script对象

		- Script中有一个delegate对象，这个delegate默认是被设置为 加载Script的Project对象(即调用apply的project)

		- 在apply中有一个to参数，可以将delegate指定为别的对象

		- **delegate作用**：当Script中操作一些不是Script自己定义的变量或函数时，gradle会到Script的delegate对象去找，看看有没有定义这些变量或函数

	- `utils.gradle`对应的project就是加载`utils.gradle`的project

	- utils中的ext 就是对应project的ext。

### 4.3.1 rootProject/build.gradle
该目录下的构建脚本负责项目整体的一些配置

主要方法:

	buildscript // 配置脚本的 classpath
	allprojects // 配置项目及其子项目
	respositories // 配置仓库地址，后面的依赖都会去这里配置的地址查找
	dependencies // 配置项目的依赖

### 4.3.2 module/build.gradle
该目录下的构建脚本负责项目具体的配置，通常需要引入插件表示项目具体的类型

主要方法：

	compileSdkVersion // 指定编译需要的 sdk 版本
	defaultConfig // 指定默认的属性，会运用到所有的 variants 上
	buildTypes // 一些编译属性可以在这里配置，可配置的所有属性在 这里
	productFlavor // 配置项目的 flavor


## 4.4 生命周期

>There is a one-to-one relationship between a Project and a build.gradle file. During build initialisation, Gradle assembles a Project object for each project which is to participate in the build, as follows:

- Create a **Settings instance** for the build.

- Evaluate the settings.gradle script, if present, against the Settings object to configure it.

- Use the configured Settings object to create the hierarchy of Project instances.

- Finally, evaluate each Project by executing its build.gradle file, if present, against the project. The projects are evaluated in breadth-wise order, such that a project is evaluated before its child projects. This order can be overridden by calling Project.evaluationDependsOnChildren() or by adding an explicit evaluation dependency using Project.evaluationDependsOn(java.lang.String).



# 5. Tooling APi

Gradle 可以通过 tooling api 来标记不同的任务结果

- 标签： `no label or EXECUTED`     说明：任务已经执行完成

	情况： 

	1. 当任务有动作且gradle确定task 是作为构建的一部分

- 标签：`UP-TO-DATE`    说明：任务输出没有改变

	情况： 

	1. 当一个任务有输入有输出并且这些没有改变
	2. 当一个任务有action，并且task输出没有改变
	3. 当一个任务没有action，但是又 dpendencies 。并且这些dependencies 已经是UP-TO-DATE,SKIPPED or from CACHE
	4. 当一个任务没有acton，也没有dependencies

- 标签:`FROM-CACHE`  说明：任务的输出是从之前的执行结果中获得

	情况:

	1. 当任务存有构建输出缓存

- 标签：`SKIPPED` 说明：任务action'被跳过

	情况:

	1. 当一个任务被明确的从 命令行中排除在外
	2. 当一个任务 返回了 false

- 标签：`NO-SOURCE` 说明：任务无需执行其action

	情况: 
		
	1. 任务有输入有输出，但是没有source ，例如：source files are .java files for JavaCompile

# 6. Gradle基础构建命令
- gradle projects 

	**查看工程信息**

- gradle tasks

	**获取所有有分组的可运行task**

	- 查看指定Project的任务，`gradlew project-path:tasks`,project-path 是目录名，这是在根目录的情况。如果已经在某个Project的目录下了 ，则不需要指定

			gradlew hmt_sdk:tasks
		
			cd XXXModule
			gradlew tasks

	- `gradlew tasks`会列出每个任务的描述

	- 添加`--all`参数  来查看task的依赖关系

- gradlew task-name

	执行指定名称的任务

	- task 与task之间往往是有依赖关系的


这些都是基本的命令，在实际项目中会根据不同的配置，会对这些task 设置不同的依赖。比如 默认的 assmeble 会依赖 assembleDebug 和assembleRelease，如果直接执行assmeble，最后会编译debug，和release 的所有版本出来。如果我们只需要编译debug 版本，我们可以运行assembleDebug。

很多命令除了会输出结果到命令行，还会在`build`文件夹下下生成运行报告，例如`check`命令会生成lint-result.html在`build/outputs`


# 7. 多项目构建加速

通过以下方式加快gradle的编译

- **开启并行编译：** 在项目根目录下的`gradle.properties`中设置

		org.gradle.parallel = true

- **开启编译守护进程:** 该进程在第一次启动后会一直存在，接下来每次编译都可以重用该进程，也是在项目根目录下的`gradle.properties`中设置

- **加大可用编译内存：** 同样是在项目根目录下的`gradle.properties`中设置

		org.gradle.jvmargs=-Xms256m -Xmx1024m



# 8. 命令参数(指令)

执行`task`的时候可以通过添加`--profile`参数生成一份执行报告在`reports/profile`中

参数`-q`可以抑制gradle日志消息

执行Task时， 添加`--continue` 可以在任务失败之后 继续执行

通过`-P`设置属性，注意大小写
	
	gradle -q taskA -P xxxx

# 9. 多项目构建

Gradle允许在单次执行中构建多个项目,这需要在根目录下提供`settings.gradle`构建脚本，并在其中对子项目进行声明

[参考](https://docs.gradle.org/4.10/dsl/org.gradle.api.initialization.Settings.html#org.gradle.api.initialization.Settings:include(java.lang.String[]))


# 10. 依赖
Gradle 3.4 版本中引入了全新的依赖配置

![](https://pic4.zhimg.com/80/v2-7fb32810290ce55e4d2cd2294a5225ff_1440w.jpg)


# 11 Gradle Wrapper

Gradle Wrapper 提供了一个`batch/shell`文件，当使用脚本时，当前的gradle版本会被下载下来 并使用，避免了开发者去下载不同版本的gradle，解决兼容性！

	 myapp/
   	├── gradlew
   	├── gradlew.bat
   	└── gradle/wrapper/
       	├── gradle-wrapper.jar
       	└── gradle-wrapper.properties

- bat文件针对window系统，shell脚本针对mac系统，是执行Gradle的脚本

- `gradle-wrapper.properties`:就是一个配置文件,可以设置gradle下载地址和版本

		 #Sat May 30 17:41:49 CEST 2015
	   	distributionBase=GRADLE_USER_HOME
	   	distributionPath=wrapper/dists
	   	zipStoreBase=GRADLE_USER_HOME
	   	zipStorePath=wrapper/dists
	   	distributionUrl=https\://services.gradle.org/distributions/
	   	gradle-2.4-all.zip
	
	- **可以改变distributionUrl 来改变gradle版本**

- `gradle-wrapper.jar`:依赖包

- gradle wrapper 一般下载到`GRADLE_CACHE/wrapper/dists `目录下

# 12 插件调试
Gradle调试方法可以参考官方教程[Debugging build logic](https://docs.gradle.org/current/userguide/troubleshooting.html#sec:troubleshooting_build_logic)

总结就是俩点:

1. 创建Remote调试配置
2. 命令行输入开始调试命令

		❯ gradle help -Dorg.gradle.debug=true
	
3. 设置断点,并点击debug按钮

可以在`buildSrc`目录下，添加对AGP的引用，然后将断点打在源码中

注意:`*.gradle`文件无法调试，只能调试源码

# 12. 文件操作

## 10.1 获取File对象
- 使用相对路径

		File file1 = file('hello.txt')
		println "file1 = ${file1.getText()}"

- 使用绝对路径

		File file2 = file(file1.absolutePath)
		println "file2 = ${file2.getText()}"		

- 使用具有相对路径的File对象

		File file3 = file(new File('hello.txt'))
		println "file3 = ${file3.getText()}"

## 10.2 获取FileCollection

- 通过`files()`获取，可以将 集合，迭代 映射 和数组传给此方法。这些将会被展开并转换成实例
		FileCollection collection1 = files('hello.txt',new File('other.gradle'))
		FileCollection collection2 = files('hello.txt')
		collection1.each{
			println "file name =${it.name}"
		}

- 将FileCollection转换成　各种类型
		Set set1 = collection1.files
		Set set2 = collection1 as Set
		List list1 = collection1 as List
		String path1 = collection1.asPath

- 以下俩个方法当FileCollection只存在一个 File时可以使用
		File file4 = collection2.singleFile
		File file5 = collection2 as File



- 添加和减去 FileCollection
		def union1 = collection1 + files('build.gradle')
		union1.each{
			println "union1 file name = ${it.name}"
		}
		def different1 = collection1 - files('hello.txt')
		different1.each{
			println "different1 file name = ${it.name}"
		}

- 通过`listFiles()`方法可以将`dir` 转换成`FileCollection`


## 10.3 文件树 FileTree

- 文件树是按照层次排列文件的集合,由FileTree 表示，其扩展了FileCollection ,`Project.fileTree(Map)`

		FileTree tree1 = fileTree(dir:'src')

- 添加 包含和不包含的规则

	- 添加方式1

			tree1.include '*.txt'
			tree1.exclude '*.gradle'
			tree1.each{
				println "FileTree $it.name"
			}

	- 添加方式2
	
			tree1 = fileTree('src').include('*.gradle')

	- 添加方式3

			tree1 = fileTree('src'){
				include '*.txt'
			}

	- 添加方式4 通过map创建

			tree1 = fileTree(dir:'src',include:'*.txt')
			tree1 = fileTree(dir:'src',include:['*.txt','*.gradle'])
			tree1 = fileTree(dir:'src',include:'*',exclude:'')

- 筛选FileTree

		FileTree filtered1 = tree1.matching{
			include '*.txt'
		}

- 遍历FileTree
		filtered1.each{
			println "file name = $it.name"
		}


- Add trees together

		FileTree sum = filtered1 + fileTree(dir: 'src',include:'*.txt')

- Visit the elements of the tree
		sum.visit {element ->
    		println "$element.relativePath => $element.file"
		}



- 可以将 zip or tar 作为文件树 ，以下俩个方法会返回FileTree
		Project.zipTree(Object)
		Project.tarTree(Object)

		FileTree someTar1 = tarTree(resources.gzip('xxx.ext'))
		someTar1.each{
			println it.name
		}

- 指定一组输入文件
		task compile1(type:JavaCompile){
			source = file('src')
		}
		source= file('src') 接受一个file对象
		source = 'src' 接收路径
		source = ['src','dest']  可以使用集合来指定多个源目录
		source = fileTree(dir:'src').matching{ include '*.gradle'} 可以接收一个fileTree
		source = { file('src').listFiles().findAll{ it.name.endsWith('.zip')}.collect{zipTree(it)}   }
 


## 10.4 Copying file

- 复制文件时，可以过滤内容 ，**需要提供 from   into  ** 

		task copy2(type:Copy){
			from 'src'
			into 'dest'
		}

- from 可以接收一个files()方法作为参数
	- 当参数被解析时，如果是目录，则该目录下的所有内容将被递归复制到目标目录下（注意：不是目录本身）
	- 当参数被解析后，不存在，则会忽略
	- 当参数是task时，即取 task 的输出结果，并且该task会被添加为Copy task的依赖

			task copy3(type:Copy){
 			//目录
 			from 'src'
 			//单个文件
 			from 'src/b.txt'
 			//task的输出
 			// from copyTask
 			//明确指定任务的输出
 			// from copyTaskWithPatterns.outputs
 			//指定zip file
 			// from zipTree('src/xx.zip')
 			// into { getDestDir()}
 			}




- 在copy时，对输入文件进行筛选

		task copy4(type:Copy){
			from 'src'
			into 'copy4'
			include '*.txt'
			//传入闭包的话 会给闭包一个 FileTree 参数
			exclude {
				it.file.name.startsWith 'a'
			}
		}



- 复制文件除了使用 定义一个task 指定Copy 类型外，  Project 也提供了copy 方法

		task copy5<<{
 			copy{
 				from 'src'
 				into 'copy5'
 			}
 		}

		task copy6{
			inputs.file copy2
			outputs.dir 'copy6'
			doLast{
				copy{
					from copy2
					into 'copy6'
				}
			}
		}

- 重命名 file

		task rename1(type:Copy){
			from 'hello.txt'
			into '/'
			rename{
				it.replace('hello','hi')
			}
		}

- 嵌套输出 

		task copy7(type:Copy){
			into 'copy7'
			exclude '*.txt'
			from('src'){
				include '*'
			}

			into('copy7') {
				exclude '*.java'
				from 'src'
			}
		}

## 10.5 Sync
- Sync 是扩展自Copy
- 与copy的区别就是， Sync 会将文件先全部复制到 目标目录下，然后再将不需要的删除

		task copy8(type:Sync){
			from 'src'
			into 'copy8'
		}



## 10.6 zip
- 创建zip，默认 应该是生成在buiild下，命名规则应该是 `projectName-version.type` 
	- version 可以在task中指定。。 貌似在 全局设置version 没用！
	- baseName 可以替换掉 projectName 
	- baseName - appendix - version - classifier .type

			task zip1(type:Zip){
				version = 1.5
				baseName = 'xixixixi'
				appendix = 'appendix'
				classifier = 'classifier'
				from 'src'
			}


# 11. Gradle操作实例
## 11.1  keystore 保护
如果我们将store的密码明文的写在signingConfigs里面，对安全性不好，所以需要构建一个动态加载任务，在编译release源码的时候从本地文件(git忽略名单中的文件)获取keystore信息

	task getReleasePsw << {
   	 def psw = ''
   	 println 'getReleasePsw is running'
    	if (rootProject.file('local.properties').exists()) {
     	   java.util.Properties properties = new Properties();
     	   properties.load(rootProject.file('local.properties').newDataInputStream())
    	    psw = properties.getProperty('release.psw')
  	  } else if (psw.trim().isEmpty()) {
    	    //TODO 在获取不到配置文件时 从控制台输入
   	     psw = java.lang.System.in.each {
   	     }
  	  }	
	}

光创建这个task还不行，需要为task添加依赖 这样执行打包task时 自动执行getReleasePsw任务
	
	tasks.whenTaskAdded{
		if(it.name.equals 'packageRelease'){
			it.dependsOn 'getReleasePsw'
		}
	}


## 11.2 hook Android编译插件 重命名apk

	android.applicationVariants.all{variant->
		variant.outputs.each{output->
			def file = output.outputFile

			output.outputFile = new File(file.parent,
			file.name.replace(".apk","-${variant.versionName}.apk"))
		}
	}

生成类似 `app-debug-1.0.apk`


## 11.3 设置默认值

通过以下设置 可以在 task被添加到project时立刻接收到通知

这可以用来设置一些默认值或行为（task在Build file中可用之前）


	tasks.whenTaskAdded{
		it.ext.srcDir = 'src'
	}

	task task22<<{
		println "task22 srcDir =$srcDir "
	}

## 11.4 配置结束回调
- task 执行图绘制完毕，应该是配置结束

		gradle.taskGraph.whenReady{
			println "taskGraph.whenReady =  $it"
		}

## 11.5 buildTypesScriptBlock

- buildTypes和上面的signingConfigs，当我们在build.gradle中通过{}配置它的时候， 其背后的所代表的对象是NamedDomainObjectContainer< BuildType>和NamedDomainObjectContainer < SigningConfig> 

- 注意，NamedDomainObjectContainer< BuildType/SigningConfig>是一种容器，容器的元素是BuildType或者SigningConfig。

- 我们在debug{}要填充BuildType或者SigningConfig所包的元素，比如storePassword就是SigningConfig类的成员。而proguardFile等是BuildType的成员。 


- 为什么要使用NamedDomainObjectContainer这种数据结构呢？因为往这种容器里添加元素可以采用这样的方法： 
	比如signingConfig为例 
    	signingConfig{//这是一个NamedDomainObjectContainer<SigningConfig> 
       		test1{//新建一个名为test1的SigningConfig元素，然后添加到容器里 
        		 //在这个花括号中设置SigningConfig的成员变量的值 
       		} 
      		test2{//新建一个名为test2的SigningConfig元素，然后添加到容器里 
         		//在这个花括号中设置SigningConfig的成员变量的值 
      		} 
    	} 

- 在buildTypes中，Android默认为这几个NamedDomainObjectContainer添加了debug和release对应的对象。如果我们再添加别的名字的东西，那么gradleassemble的时候也会编译这个名字的apk出来。比如，我添加一个名为test的buildTypes，那么gradle assemble 就会编译一个xxx-test-yy.apk。在此，test就好像debug、release一样。 
