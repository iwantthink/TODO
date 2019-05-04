# Gradle命令行

[Gradle命令行界面-官方文档](https://docs.gradle.org/current/userguide/command_line_interface.html)


# 简介

命令行界面是与Gradle进行交互的主要方式之一,建议使用`Gradle Wrapper`替代直接使用`Gradle`（即使用`gradlew`代替`gradle`）


在命令行上执行Gradle需要符合以下结构,在任务名称之前或之后可以使用选项

	gradle [taskName...] [--option-name...]

- 使用空格分隔不同的任务

- 可以使用`=`来设置选项的值(允许不使用`=`直接添加值，但是不建议)

		--console=plain

- 通过添加`--no-` 来停止使用某种选项,以下是作用相反的命令

		// 使用构建缓存
		--build-cache
		// 不使用构建缓存
		--no-build-cache

- 一些较长的格式 通常会有缩写

		--help
		-h

- 许多命令行标志可以写在`gradle.properties`文件中,避免每次都输入

	参考[配置构建环境](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties)


下面的部分描述了Gradle命令行界面的使用，大致按用户目标分组。一些插件还添加了自己的命令行选项，例如 `-- test`用于Java测试过滤


# 1. 执行任务

Gradle支持运行任务及其所有依赖

	$ gradle myTask

- 大多数构建都支持一组称为生命周期任务的通用任务。 这些包括`build`,`assemble`和 `check`任务


## 1.1 在多项目构建中执行任务

在多项目构建中，可以使用全限定名称来执行子项目任务，即使用`:`分隔开的子项目名称和任务名称。 从根项目运行时，以下内容是等效的：

	$ gradle :mySubproject:taskName
	$ gradle mySubproject:taskName

在根目录下仅使用任务名称执任务，将执行所有子项目的任务。 例如，当从根项目目录调用`test`任务，所有子项目将运行`test`任务

	$ gradle test

当从子项目中调用任务，应该省略项目名称：

	$ cd mySubproject
	$ gradle taskName

在子项目中使用`Gradle Wrapper`时，必须相对引用`gradlew`。 例如：`../ gradlew taskName`

## 1.2 执行多个任务
可以在单个构建中执行多个任务，将按照命令行中列出的顺序执行任务，并且还将执行每个任务的依赖

	$ gradle test deploy


- 每个任务只会被执行一次，无论它是在命令行中被指定的还是作为另外一个任务的依赖项。**优先执行依赖项。**


## 1.3 排除任务
可以使用`-x`或`--exclude-task`命令行选项排除指定的任务，选项后面需要提供任务名称

![](http://ww1.sinaimg.cn/large/6ab93b35ly1g2phd4r6b5j20jq02eq2q.jpg)

	$ gradle dist --exclude-task test
	
	> Task :compile
	compiling source
	
	> Task :dist
	building the distribution
	
-  可以看到测试任务未执行，即使它是dist任务的依赖. 此外作为`test`任务的依赖`compileTest `任务也不会执行。但是`compile`作为`dist`的依赖仍旧会执行

	

## 1.4 强制任务执行
使用`--rerun-tasks`命令行选项可以强制Gradle忽略`UP-TO-DATE`，去执行任务

	$ gradle test --rerun-tasks

- 这将强制`test`任务及其所有的依赖去执行

- 这有点像执行了`gradle clean test`,但是没有构建产出被删除

## 1.5 出错时继续执行

默认情况下，Gradle将在任何任务失败后立即中止执行并使构建失败。为了在单个构建执行中发现尽可能多的故障，可以使用`--continue`选项

	$ gradle test --continue

- 当使用`--continue`执行时，Gradle将在任务的所有依赖项都没有失败的情况下执行待执行的每个任务，而不是在遇到第一个失败时立即停止。 每个遇到的故障都将在构建结束时报告

- 如果任务失败，任何依赖于该任务的后续任务都不会执行。例如，如果被测代码编译失败，测试将不会运行;因为测试任务将直接或间接地依赖于编译任务。


## 1.6 任务名称缩写

在命令行中指定任务时，不必提供完整的任务名称，只需要满足在所有task中能够作为唯一标识即可

可以缩写驼峰式任务名称中的每个单词，例如`gradle compileTest`==`gradle cT`==`gradle compTest`

	$ gradle cT
	
	> Task :compile
	compiling source
	
	> Task :compileTest
	compiling unit tests


- 使用`-x`选项时也可以使用任务缩写名称

		gradle dist==gradle d
		
		
		
# 2. 通用任务

以下是内置和大多数主要Gradle插件应用的任务约定


## 2.1 计算所有的输出

在Gradle构建中，`build`任务通常用于指定组装所有输出并运行所有检查

	$ gradle build

## 2.2 运行应用

应用程序通常与`run`任务一起运行，`run`任务负责组装应用程序并执行某些脚本或二进制文件

	$ gradle run


## 2.3 执行所有的检查

所有验证任务(包括测试和lint)都可以使用`check`任务执行

	$ gradle check

## 2.4 清理输出

可以使用`clean`任务删除构建的输出，这样将导致后续任务执行需要大量额外构建时间

	$ gradle clean


# 3. 项目报告

Gradle提供了几个内置任务，可以显示构建的特定细节。 这对于理解构建的结构和依赖关系以及调试问题非常有用

- 可以使用`gradle help`获得有关可用的报告选项的基本帮助


## 3.1 列出项目

运行`gradle projects`可以得到所选项目的子项目列表，以层次结构显示

	$ gradle projects

## 3.2 列出任务

运行`gradle tasks`会提供所选项目的主要任务列表。 此报告显示项目的默认任务（如果有）以及每个任务的描述

	$ gradle tasks

默认情况下，此报告仅显示已分配给任务组的任务。可以使用`——all`选项在任务列表中获得更多信息

	$ gradle tasks --all

如果需要更精确，可以使用`——group`命令行选项显示来自特定组的任务

	$ gradle tasks --group="build setup"


## 3.3 显示任务使用细节

运行`gradle help --task someTask`命令可以获得特定任务的详细信息

	$ gradle -q help --task libs
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

- 此信息包括完整的任务路径、任务类型、可能的命令行选项和任务的描述


# 4. 报告依赖

构建扫描提供了一个完整的、可视化的报告，说明存在于哪些配置之上的依赖、传递依赖和依赖版本选择

	$ gradle myTask --scan

- 执行这个命令将提供一个链接，可以在其中找到依赖信息


## 4.1 列出项目依赖

运行`gradle dependencies`命令将根据配置分解所选项目的依赖项列表。对于每个配置，该配置的直接依赖和传递依赖都显示在一个树中。下面是这个报告的一个例子

	$ gradle dependencies


## 4.2 其他

运行`gradle buildEnvironment`将使得所选项目的构建脚本的依赖可视化

	$ gradle buildEnvironment

	$ gradle dependencyInsight


## 4.3 列出项目属性

运行`gradle properties`命令会列出所选项目的所有属性

	$ gradle -q api:properties
	
	------------------------------------------------------------
	Project :api - The shared API for the application
	------------------------------------------------------------
	
	allprojects: [project ':api']
	ant: org.gradle.api.internal.project.DefaultAntBuilder@12345
	antBuilderFactory: org.gradle.api.internal.project.DefaultAntBuilderFactory@12345
	artifacts: org.gradle.api.internal.artifacts.dsl.DefaultArtifactHandler_Decorated@12345
	asDynamicObject: DynamicObject for project ':api'
	baseClassLoaderScope: org.gradle.api.internal.initialization.DefaultClassLoaderScope@12345

## 4.4 软件模型报告

可以使用`model`任务为软件模型项目获得元素的层次视图

	$ gradle model


# 5. 命令补全

通过单独安装[gradle-completion](https://github.com/gradle/gradle-completion),可以在bash中使用`tab`进行命令补全








