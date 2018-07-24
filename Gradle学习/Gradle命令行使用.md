# Gradle命令行

[Gradle命令行界面-官方文档](https://docs.gradle.org/current/userguide/command_line_interface.html)


# 简介

命令行界面是与Gradle进行交互的主要方式之一,建议使用`Gradle Wrapper`替代直接使用`Gradle`命令


**Gradle命令语法:**

	gradle [taskName...] [--option-name...]

- 使用空格分隔不同的任务

- 可以使用`=`来设置option的默认参数

- 通过添加`--no-` 来停止使用某种`option`

		--build-cache
		--no-build-cache

- 一些较长的格式 通常会有缩写

		--help
		-h

>许多命令行标志可以写在`gradle.properties`文件中,避免每次都输入

- 参考[配置构建环境](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties)


# 1. 命令行的使用

## 1.1 执行多个任务
可以在单个构建中执行多个任务，例如`gradle compile test` 将依次执行`compile`和`test`任务，此外还会执行每个任务的依赖关系。

每个任务只会被执行一次，无论它是在命令行中被指定的还是作为另外一个任务的依赖项。**优先执行依赖项。**

**示例如下：**

![任务依赖关系](https://docs.gradle.org/current/userguide/img/commandLineTutorialTasks.png)

	task compile {
	    doLast {
	        println 'compiling source'
	    }
	}
	
	task compileTest(dependsOn: compile) {
	    doLast {
	        println 'compiling unit tests'
	    }
	}
	
	task test(dependsOn: [compile, compileTest]) {
	    doLast {
	        println 'running unit tests'
	    }
	}
	
	task dist(dependsOn: [compile, test]) {
	    doLast {
	        println 'building the distribution'
	    }
	}
	//输出内容
	> gradle dist test
	:compile
	compiling source
	:compileTest
	compiling unit tests
	:test
	running unit tests
	:dist
	building the distribution

## 1.2 排除任务
可以使用`-x`命令行选项排除指定的任务，需要提供任务名称

**使用1.1中的例子：**

	> gradle dist -x test
	:compile
	compiling source
	:dist
	building the distribution

## 1.3 发生故障时继续构建
默认情况下一旦任务失败，Gradle将终止执行并使构建失败。为了在单个构建执行过程中发现更多的错误，可以使用`--continue`

## 1.4 任务名称缩写
在命令行中指定任务时，不必提供完整的任务名称，只需要满足在所有task中能够作为唯一标识即可

可以缩写驼峰式任务名称中的每个单词，例如`gradle compileTest`==`gradle cT`==`gradle compTest`

使用`-x`选项时也可以使用任务缩写名称

例如`gradle dist`==`gradle d`

## 1.5 选择执行命令的构建文件
运行gradle命令时，会在当前目录中寻找一个构建文件,可以通过`-b`选项指定另外一个构建文件

当使用`-b`选项时，settings.gradle文件就不会被使用

**示例如下：**

	task hello {
	    doLast {
	        println "using build file '$buildFile.name' in '$buildFile.parentFile.name'."
	    }
	}

	> gradle -q -b subdir/myproject.gradle hello
	using build file 'myproject.gradle' in 'subdir'.

可以通过`-p`选项指定构建使用的项目目录，对于多项目构建应该使用`-p`

	> gradle -q -p subdir hello
	using build file 'build.gradle' in 'subdir'.

## 1.6 强制执行任务
Gradle中许多任务都支持**增量构建(incremental builds.)**。这些任务根据自上次运行以来输入或输出是否发生变化来确定是否需要运行。可以用过查看运行时期，输出log 中 task的名称旁是否有`UP-TO-DATE`文本判断当前任务是否处于增量构建。

可以通过`--rerun-tasks`选项强制gradle执行所有任务，忽略`UP-TO-DATE`检查

示例如下:

	> gradle doIt
	:doIt UP-TO-DATE
	
	> gradle --rerun-tasks doIt
	:doIt

## 1.7 获取构建的信息
Gradle提供了内置任务用来显示构建的细节，有助于了解构建的结构和依赖关系

### 1.7.1 列出项目
运行`gradle projects`将提供所选项目的子项目列表，显示其层次结构

	> gradle -q projects
	
	------------------------------------------------------------
	Root project
	------------------------------------------------------------
	
	Root project 'projectReports'
	+--- Project ':api' - The shared API for the application
	\--- Project ':webapp' - The Web application implementation
	
	To see a list of the tasks of a project, run gradle <project-path>:tasks
	For example, try running gradle :api:tasks

通过`description`属性可以为项目提供说明

### 1.7.2 列出任务
运行`gradle tasks`会提供所选项目的主要任务列表。

	> gradle -q tasks
	
	------------------------------------------------------------
	All tasks runnable from root project
	------------------------------------------------------------
	
	Default tasks: dists
	
	Build tasks
	-----------
	clean - Deletes the build directory (build)
	dists - Builds the distribution
	libs - Builds the JAR
	
	Build Setup tasks
	-----------------
	init - Initializes a new Gradle build.
	wrapper - Generates Gradle wrapper files.
	
	Help tasks
	----------
	buildEnvironment - Displays all buildscript dependencies declared in root project 'projectReports'.
	components - Displays the components produced by root project 'projectReports'. [incubating]
	dependencies - Displays all dependencies declared in root project 'projectReports'.
	dependencyInsight - Displays the insight into a specific dependency in root project 'projectReports'.
	dependentComponents - Displays the dependent components of components in root project 'projectReports'. [incubating]
	help - Displays a help message.
	model - Displays the configuration model of root project 'projectReports'. [incubating]
	projects - Displays the sub-projects of root project 'projectReports'.
	properties - Displays the properties of root project 'projectReports'.
	tasks - Displays the tasks runnable from root project 'projectReports' (some of the displayed tasks may belong to subprojects).
	
	To see all tasks and more detail, run gradle tasks --all
	
	To see more detail about a task, run gradle help --task <task>

- 默认情况下，只会显示已经有任务组的任务，关于分组可以通过`group`属性设置

		dists {
		    description = '建立分配' 
		    group = 'build'
		}

可以使用`--all`选项获取全部任务列表，包括未分配任务组分组的任务

	> gradle -q tasks --all
	
	------------------------------------------------------------
	All tasks runnable from root project
	------------------------------------------------------------
	
	Default tasks: dists
	
	Build tasks
	-----------
	clean - Deletes the build directory (build)
	api:clean - Deletes the build directory (build)
	webapp:clean - Deletes the build directory (build)
	dists - Builds the distribution
	api:libs - Builds the JAR
	webapp:libs - Builds the JAR
	
	Build Setup tasks
	-----------------
	init - Initializes a new Gradle build.
	wrapper - Generates Gradle wrapper files.
	
	Help tasks
	----------
	buildEnvironment - Displays all buildscript dependencies declared in root project 'projectReports'.
	api:buildEnvironment - Displays all buildscript dependencies declared in project ':api'.
	webapp:buildEnvironment - Displays all buildscript dependencies declared in project ':webapp'.
	components - Displays the components produced by root project 'projectReports'. [incubating]
	api:components - Displays the components produced by project ':api'. [incubating]
	webapp:components - Displays the components produced by project ':webapp'. [incubating]
	dependencies - Displays all dependencies declared in root project 'projectReports'.
	api:dependencies - Displays all dependencies declared in project ':api'.
	webapp:dependencies - Displays all dependencies declared in project ':webapp'.
	dependencyInsight - Displays the insight into a specific dependency in root project 'projectReports'.
	api:dependencyInsight - Displays the insight into a specific dependency in project ':api'.
	webapp:dependencyInsight - Displays the insight into a specific dependency in project ':webapp'.
	dependentComponents - Displays the dependent components of components in root project 'projectReports'. [incubating]
	api:dependentComponents - Displays the dependent components of components in project ':api'. [incubating]
	webapp:dependentComponents - Displays the dependent components of components in project ':webapp'. [incubating]
	help - Displays a help message.
	api:help - Displays a help message.
	webapp:help - Displays a help message.
	model - Displays the configuration model of root project 'projectReports'. [incubating]
	api:model - Displays the configuration model of project ':api'. [incubating]
	webapp:model - Displays the configuration model of project ':webapp'. [incubating]
	projects - Displays the sub-projects of root project 'projectReports'.
	api:projects - Displays the sub-projects of project ':api'.
	webapp:projects - Displays the sub-projects of project ':webapp'.
	properties - Displays the properties of root project 'projectReports'.
	api:properties - Displays the properties of project ':api'.
	webapp:properties - Displays the properties of project ':webapp'.
	tasks - Displays the tasks runnable from root project 'projectReports' (some of the displayed tasks may belong to subprojects).
	api:tasks - Displays the tasks runnable from project ':api'.
	webapp:tasks - Displays the tasks runnable from project ':webapp'.
	
	Other tasks
	-----------
	api:compile - Compiles the source files
	webapp:compile - Compiles the source files
	docs - Builds the documentation

### 1.7.3 显示任务使用详情
运行`gradle help --task someTask`可以提供特定任务的详细信息

	> gradle -q help --task libs
	Detailed task information for libs
	
	Paths
	     :api:libs
	     :webapp:libs
	
	Type
	     Task (org.gradle.api.Task)
	
	Description
	     Builds the JAR
	
	Group
	     build

### 1.7.4 列出项目依赖关系
运行`gradle dependencies`会提供指定项目的依赖关系

	> gradle -q dependencies api:dependencies webapp:dependencies
	
	------------------------------------------------------------
	Root project
	------------------------------------------------------------
	
	No configurations
	
	------------------------------------------------------------
	Project :api - The shared API for the application
	------------------------------------------------------------
	
	compile
	\--- org.codehaus.groovy:groovy-all:2.4.10
	
	testCompile
	\--- junit:junit:4.12
	     \--- org.hamcrest:hamcrest-core:1.3
	
	------------------------------------------------------------
	Project :webapp - The Web application implementation
	------------------------------------------------------------
	
	compile
	+--- project :api
	|    \--- org.codehaus.groovy:groovy-all:2.4.10
	\--- commons-io:commons-io:1.2
	
	testCompile
	No dependencies

通过`--configuration`显示指定的依赖关系

	> gradle -q api:dependencies --configuration testCompile
	
	------------------------------------------------------------
	Project :api - The shared API for the application
	------------------------------------------------------------
	
	testCompile
	\--- junit:junit:4.12
	     \--- org.hamcrest:hamcrest-core:1.3

### 1.7.5 列出项目的buildscript依赖关系
运行`gradle buildEnvironment`,显示指定构建脚本之间的依赖关系

### 1.7.6 深入了解特定的依赖关系
运行`gradle dependencyInsight`可以深入了解指定依赖项

	> gradle -q webapp:dependencyInsight --dependency groovy --configuration compile
	org.codehaus.groovy:groovy-all:2.4.10
	\--- project :api
	     \--- compile

这个任务可以用来寻找某些依赖关系来自何处并且为什么被选择。[更多细节可以参考DependencyInsightReportTask](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.diagnostics.DependencyInsightReportTask.html)

### 1.7.7 列出项目属性
运行`gradle properties`将提供所选项目的属性列表

	> gradle -q api:properties
	
	------------------------------------------------------------
	Project :api - The shared API for the application
	------------------------------------------------------------
	
	allprojects: [project ':api']
	ant: org.gradle.api.internal.project.DefaultAntBuilder@12345
	antBuilderFactory: org.gradle.api.internal.project.DefaultAntBuilderFactory@12345
	artifacts: org.gradle.api.internal.artifacts.dsl.DefaultArtifactHandler_Decorated@12345
	asDynamicObject: DynamicObject for project ':api'
	baseClassLoaderScope: org.gradle.api.internal.initialization.DefaultClassLoaderScope@12345
	buildDir: /home/user/gradle/samples/userguide/tutorial/projectReports/api/build
	buildFile: /home/user/gradle/samples/userguide/tutorial/projectReports/api/build.gradle

### 1.7.8 构建过程的分析
在命令行中添加`--profile`使得构建运行时记录一些耗时信息，并写入报告`build/reports/profiles`中。

### 1.7.9 Dry run
如果希望了解任务的执行顺序，但不具体的去执行任务，可以使用`-m`选项

例如：`gradle -m clean compile`

# 2 Gradle控制台
[Gradle Console-USER GUIDE](https://docs.gradle.org/current/userguide/console.html)

# 3 Gradle Wrapper

Gradle Wrapper 会为用户下载并安装正确的Gradle版本

## 3.1 使用Wrapper执行构建
Gradle建议使用Wrapper.
- `./gradlew<task>`:在类Unix的平台上，如Linux和Mac OS X
- gradlew <task>:在使用gradlew.bat批处理文件的Windows上

每个wrapper都绑定了一个特定的Gradle版本，所以在使用Wrapper运行一个命令之前，都会先去下载相应的Gradle包，然后使用该Gradle包去构建。

组成Gradle Wrapper文件和目录：

	app/	
	gradlew （Unix Shell脚本）
	gradlew.bat （Windows批处理文件）
	gradle/wrapper/gradle-wrapper.jar （包装JAR）
	gradle/wrapper/gradle-wrapper.properties （包装属性）

Wrapper 下载的Gradle 存储在`$USER_HOME/.gradle/wrapper/dists`

## 3.2 将Wrapper添加到项目
Wrapper是需要添加到版本控制中的。通过使用Wrapper，任何人都可以在没有事先安装Gradle的情况下使用项目。可以保证用户使用指定的Gradle版本。

通过运行`gradle wrapper`任务可以将Wrapper安装到项目中，通过`gradle wrapper --gradle-version 2.4`

如果想切换到新版本的Gradle，不需要重新运行wrapper任务。在`gradle-wrapper.properties`文件中更改相应条目已经足够了，但是如果要利用Gradle包装器中的新功能，则需要重新生成包装文件。

# 4 Gradle 守护进程
[Gradle Daemon Process DOC](https://docs.gradle.org/current/userguide/gradle_daemon.html)

# 5 依赖管理
[Gradle Dependency Management](https://docs.gradle.org/current/userguide/artifact_dependencies_tutorial.html)
## 5.1 什么是依赖管理？
依赖管理可以简单的划分成：dependencies(被添加进项目的文件)和publications(项目输出的文件)。

## 5.2 声明依赖

	apply plugin: 'java'
	
	repositories {
	    mavenCentral()
	}
	
	dependencies {
	    compile group: 'org.hibernate', name: 'hibernate-core', version: '3.6.7.Final'
	    testCompile group: 'junit', name: 'junit', version: '4.+'
	}

这里声明了以下几点内容：
1. Gradle 需要从Maven central repository寻找依赖
2. hibernate-core被项目依赖（在运行时被需要）
3. 项目的测试时需要使用junit,junit大于4.0且最新的版本会被添加


## 5.3 依赖配置
配置是一组已命名的依赖和artifacts

以下是配置的三个目的：

1. **Declaring Dependencies**

	The plugin uses configurations to make it easy for build authors to declare what other subprojects or external artifacts are needed for various purposes during the execution of tasks defined by the plugin.

2. **Resolving Dependencies**

	The plugin uses configurations to find (and possibly download) inputs to the tasks it defines.

3. **Exposing Artifacts for Consumption**

	The plugin uses configurations to define what artifacts it generates for other projects to consume.


## 5.4 外部依赖
依赖声明有很多种，其中一种就是**外部依赖**。就是当前构建对构建之外的某些文件有依赖关系，并且这种文件存储在某种存储库中，例如Maven...

**外部依赖的定义方式：**

	dependencies {
		//完整写法
	    compile group: 'org.hibernate', name: 'hibernate-core', version: '3.6.7.Final'
		//缩写方式
    	compile 'org.hibernate:hibernate-core:3.6.7.Final'

	}

- 外部依赖通过`group,name,version`属性定义。**不同的仓库中，group/version 这俩个属性可能是可选的**，`group:name:version`

## 5.5 库
Gradle通过 repository 找到外部依赖文件 。 repository实际上就是一些文件，根据group,name,version进行组织。

repository有很多种格式，例如Maven 或ivy,以及多种访问方式，例如使用本地文件系统或 Http

默认情况下，Gradle不定义任何repository.所以在使用依赖关系之前，至少得先定义一个repository.

1. 使用Maven central repository

		repositories {
		    mavenCentral()
		}

2. 使用Jcenter 

		repositories {
		    jcenter()
		}

3. 使用远程Maven库

		repositories {
		    maven {
		        url "http://repo.mycompany.com/maven2"
		    }
		}

4. 远程ivy目录

		repositories {
		    ivy {
		        url "http://repo.mycompany.com/repo"
		    }
		}

5. Maven和ivy 库 都可以使用本地文件系统

		repositories {
		    ivy {
		        // URL can refer to a local directory
		        url "../local-repo"
		    }
		}


**一个项目可以有多个repository,Gradle将按照指定顺序在每个repository中查找依赖项，并在包含依赖项的第一个repository中停止**

## 5.6 Publishing artifacts

依赖配置也可以用来发布文件，通常称这些文件为publication artifacts或 artifacts

插件在定义项目的artifacts方面做得很好，因此通常不需要做一些特殊的处理去告诉Gradle什么需要被发布，只用告诉Gradle artifacts需要被发布到哪里。

发布的实现需要通过`uploadArchives`任务。如下是一个发布到ivy库的例子

	uploadArchives {
	    repositories {
	        ivy {
	            credentials {
	                username "username"
	                password "pw"
	            }
	            url "http://repo.mycompany.com"
	        }
	    }
	}

- 当运行`gradle uploadArchives`命令时，Gradle会构建和上传项目Jar包，同时Gradle还会生成和上传一个`ivy.xml`文件

- 发布到Maven库也是可以的，语法和发布到ivy有微小的区别。

		apply plugin: 'maven'
		
		uploadArchives {
		    repositories {
		        mavenDeployer {
		            repository(url: "file://localhost/tmp/myRepo/")
		        }
		    }
		}

	1. 发布到Maven库需要添加Maven插件

	2. 与ivy的`ivy.xml`文件类似，上传到Maven库 会生成和上传一个`pom.xml`

# 6 多项目构建
相互依赖的模块更容易被理解和消化，Gradle通过`multi-project`构建支持这种方案

## 6.1 multi-project构建的结构
这种构建通常都有各自不同的大小和形状，但也有一些共同的特点

1. 一个settings.gradle文件在项目的根目录或主目录下

2. 一个build.gradle文件在根目录或主目录

3. 子文件夹拥有自己的`*.gradle`构建文件（一些多项目构建可能会省略子项目构建脚本）

Gradle通过`settings.gradle`文件知道项目和子项目的结构。通常不需要通过查看这个文件去了解项目结构，而是通过运行命令`gradle projects`

如下是一个Java 多项目构建的输出例子：

	> gradle -q projects
	
	------------------------------------------------------------
	Root project
	------------------------------------------------------------
	
	Root project 'multiproject'
	+--- Project ':api'
	+--- Project ':services'
	|    +--- Project ':services:shared'
	|    \--- Project ':services:webservice'
	\--- Project ':shared'
	
	To see a list of the tasks of a project, run gradle <project-path>:tasks
	For example, try running gradle :api:tasks

## 6.2 执行multi-project构建
从用户的角度看，多项目构建 仍然是一个可运行的任务集合，区别就是可能会需要执行不同的项目的任务，有俩种实现方式：

1. 切换到指定项目目录下，按`gradle <task>`方式正常执行
2. 可以从任何目录下执行，按`gradle:services:webservice:build`

# 7 连续构建
孵化功能。。
[Continuous Build](https://docs.gradle.org/current/userguide/continuous_build.html)

# 8 复合构建
孵化功能。。。
[Composite Build](https://docs.gradle.org/current/userguide/composite_builds.html)

# 9 构建环境

## 9.1 通过gradle.properties配置构建环境
Gradle可以在本地环境中通过GRADLE_OPTS或JAVA_OPTS配置，也可以通过项目中的`gradle.properties`文件进行配置。

**配置按如下顺序去获取(如果多个地方进行了设置，以最后一个为准)**

1. 来自项目构建目录中的`gradle.properties`
2. 来自gradle user home 中的`gradle.properties`
3. 当命令行设置了`-Dsome.property`时，会从system properties中获取

当设置如下属性时，需要注意JDK或JRE版本要高于7:

1. org.gradle.daemon
	
	设置为true则Gradle守护进程时用于运行构建，从Gradle 3.0开始，守护进程默认是启用的，并且建议运行Gradle时使用守护进程。

2. org.gradle.java.home

	指定Gradle构建进程的Java目录。该值可以为一个 jdk或者jre的位置(取决于构建)，如果该值未指定，则使用合理的默认值。

3. org.gradle.jvmargs

	指定用于守护进程的`jvmargs`。该设置对调整内存设置特别有用。目前，默认设置在内存方面相当慷慨。

4. org.gradle.configureondemand

	Enables new incubating mode that makes Gradle selective when configuring projects. Only relevant projects are configured which results in faster builds for large multi-projects

5. org.gradle.parallel

	配置后，Gradle将以孵化并行模式运行。

6. org.gradle.workers.max

	配置后，Gradle将使用给定数量的工作人员的最大数量。

7. org.gradle.logging.level

	设置为quiet, warn, lifecycle, info, or debug，Gradle将使用此日志级别。值不区分大小写。

8. org.gradle.debug

	When set to true, Gradle will run the build with remote debugging enabled, listening on port 5005. Note that this is the equivalent of adding -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 to the JVM command line and will suspend the virtual machine until a debugger is attached.

9. org.gradle.daemon.performance.enable-monitoring

	当设置为false时，Gradle将不会监视正在运行的守护进程的内存使用情况。

10. org.gradle.caching

	设置为true时，Gradle将尝试重新使用以前版本的输出。

11. org.gradle.console

	当设置为plain，auto或rich时，Gradle将使用不同类型的控制台。

## 9.2 Gradle属性和系统属性
通过添加`-D`命令行选项，可以**传递系统属性**到运行Gradle的JVM。实际上，Gradle的`-D`命令行选项和Java上的一样

通过使用属性文件`gradle.properties`可以添加**属性到`project`对象**。`gradle.properties`文件可以放在Gradle用户目录文件夹下(gradle user home由环境变量`GRADLE_USER_HOME`定义， 默认是`USER_HOME/.gradle`)。也可以放在项目文件夹下。对于多项目构建的项目可以将`gradle.properties`放到任何一个子项目文件夹中。

- `gradle.properties`文件中的属性可以被`project`对象 访问到。

- Gradle用户目录中的属性文件优先级别比项目目录中的高

- 可以在`gradle.properties`中设置系统属性，需要给属性名称添加前缀`systemProp.`。例如`systemProp.propName`

- 在多项目构建中`systemProp.`只有在根目录与下的属性文件中设置才有用

- 例子：

		//gradle.properties
		gradlePropertiesProp=gradlePropertiesValue
		sysProp=shouldBeOverWrittenBySysProp
		envProjectProp=shouldBeOverWrittenByEnvProp
		systemProp.system=systemValue
		//build.gradle
		task printProps {
		    doLast {
		        println commandLineProjectProp
		        println gradlePropertiesProp
		        println systemProjectProp
		        println envProjectProp
		        println System.properties['system']
		    }
		}

		> gradle -q -PcommandLineProjectProp=commandLineProjectPropValue -Dorg.gradle.project.systemProjectProp=systemPropertyValue printProps
		commandLineProjectPropValue
		gradlePropertiesValue
		systemPropertyValue
		envPropertyValue
		systemValue


可以通过`-P`命令行选项将**属性直接添加到`project`对象**

## 9.2.1 检查项目属性
构建脚本中的项目属性可以通过其名称直接访问吗，跟使用变量一样.如果这个属性不存在，就会抛出一个异常，同时构建失败。

如果构建脚本中存在逻辑依赖于用户可选的属性(例如存在于gradle.properties文件中)，需要在访问之前检查是否存在，`hasProperty('propertyName')`方法可以实现，返回true/false

## 9.3 通过代理访问网络

# 10 使用Tooling Api 嵌入Gradle

## 10.1 Tooling Api介绍
Gradle提供了名为Tooling Api的编程Api，用来将Gradle嵌入软件中。这个Api允许执行和监视构建，并查询Gradle有关构建的细节

## 10.2 Tooling Api的功能

Tooling Api提供的一些功能：

- 查询构建的详细信息，包括项目层次结构和项目依赖关系，每个项目的外部依赖关系（包括源和Javadoc jars），源目录和任务。

- 执行构建并侦听stdout和stderr日志记录和进度消息（例如，在命令行上运行时显示在“状态栏”中的消息）。

- 执行特定的测试类或测试方法。

- 在构建执行时接收有趣的事件，例如项目配置，任务执行或测试执行。

- 取消正在运行的构建。

- 将多个独立的Gradle构建合并为一个复合构建。

- 工具API可以下载并安装适当的Gradle版本，类似于包装。

- 该实现是轻量级的，只有少量的依赖关系。它也是一个行为良好的库，并且不会对您的类加载器结构或日志记录配置做任何假设。这使得API很容易嵌入到你的应用程序中。

## 10.3 Tooling Api 和 Gradld 构建守护进程
Tooling Api总是使用 Gradle构建守护进程

## 10.4 快速开始
Tooling APi是面向开发者的接口,javadoc 是其主要文档。Gradle在`sample/toolingApi`目录下提供了几个例子

使用Tooling Api 需要添加如下 库 和 依赖：

	//build.gradle
	repositories {
	    maven { url 'https://repo.gradle.org/gradle/libs-releases' }
	}
	
	dependencies {
	    compile "org.gradle:gradle-tooling-api:${toolingApiVersion}"
	    // The tooling API need an SLF4J implementation available at runtime, replace this with any other implementation
	    runtime 'org.slf4j:slf4j-simple:1.7.10'
	}

# 11 构建缓存

[Gradle build cache(与Android的构建缓存不同)](https://docs.gradle.org/current/userguide/build_cache.html)