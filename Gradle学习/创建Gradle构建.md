# Creating New Gradle Builds

[创建新的Gradle 构建](https://guides.gradle.org/creating-new-gradle-builds/)

[Build Init Plugin ](https://docs.gradle.org/4.10-rc-2/userguide/build_init_plugin.html#sec:build_init_types)

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

# 2. 创建task

Gradle提供了APi,用于创建和配置任务. Project包含Tasks集合,每个task 都能执行一些基本操作

	task copy(type: Copy, group: "Custom", description: "Copies sources to the dest directory") {
	    from "src"
	    into "dest" // 不必创建dest目录,Gradle将自动创建
	}

- `group`,`description` 可以是任意值,甚至可以忽略不写


# 3. 应用插件

Gradle包含一系列的插件,更多可用的插件可以在[Gradle插件门户网站](https://plugins.gradle.org/)

例如,`base`插件中包含一个核心类型`Zip`,利用这个`Zip`去创建一个压缩的task

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