
# 1.使用Gradle命令行

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

## 5.1 什么是依赖管理？
依赖管理可以简单的划分成：dependencies 和publications 。