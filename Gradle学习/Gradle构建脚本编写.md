[Tutorial Using Tasks](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html)

# 1.1 项目和任务
Gradle中所有的内容都是基于俩个基本概念：**项目和任务**

项目具体代表什么取决于Gradle正在做什么。例如，一个项目可能代表着一个Jar库 或一个web应用。

每个项目由一个或者多个任务组成，一个任务代表一个构建执行的一些原子工作。这些工作可能是编译一些类，创建一个Jar，生成Javadoc，或者发布archives到repository

## 1.2 Hello World
当使用gradle命令运行Gradle构建时，该gradle命令会在当前目录下寻找`build.gradle`文件。**这个`build.gradle`文件被称为build script(构建脚本)**,尽管严格来讲它是一个configuration script(配置脚本)

构建脚本 定义了一个project和它的task

**build script 示例：**

	//build.gradle
	task hello {
	    doLast {
	        println 'Hello world!'
	    }
	}

- 在命令行shell中，移动了包含该构建脚本的目录，并使用`gradle -q hello`执行此构建脚本

- 这个构建脚本定义了一个任务，任务名称是`hello`,并且添加了一个action.当执行`gradle hello`时，会去执行提供了的action，这个action只是一个包含一些待执行的groovy代码的闭包

## 1.3 任务定义的快捷方式
例如1.2中的 hello 任务可以用如下更简洁的方式定义：

	task hello << {
	    println 'Hello world!'
	}

- **该快捷方式将在Gradle 5.0 中删除！！**

- `<<`符号只是一个doLast的别名

## 1.4 构建脚本即编程
Gradle的构建脚本提供了Groovy的全部功能。

在Gradle的Task中使用Groovy的示例：

	//示例1
	task upper {
	    doLast {
	        String someString = 'mY_nAmE'
	        println "Original: " + someString
	        println "Upper case: " + someString.toUpperCase()
	    }
	}

	> gradle -q upper
	Original: mY_nAmE
	Upper case: MY_NAME

	//示例2
	task count {
	    doLast {
	        4.times { print "$it " }
	    }
	}

	> gradle -q count
	0 1 2 3 

## 1.5 任务依赖关系
可以声明一个任务依赖于另外一个任务

	task hello {
	    doLast {
	        println 'Hello world!'
	    }
	}
	task intro(dependsOn: hello) {
	    doLast {
	        println "I'm Gradle"
	    }
	}
	
	> gradle -q intro
	Hello world!
	I'm Gradle

添加一个依赖时，被依赖的任务可以不需要存在

	task taskX(dependsOn: 'taskY') {
	    doLast {
	        println 'taskX'
	    }
	}
	task taskY {
	    doLast {
	        println 'taskY'
	    }
	}
	
	> gradle -q taskX
	taskY
	taskX

- 在这个例子中，taskY 在被定义出来之前 就已经被添加为 taskX的依赖。**更多详细内容请参考1.8！** [添加对任务的依赖关系](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:adding_dependencies_to_tasks)

- **在任务未被定义时，不能使用快捷符号**,参考[快捷符号](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html#sec:shortcut_notations)

## 1.6 动态任务
Groovy不仅可以用来定义任务功能，还可以动态的创建任务

	4.times { counter ->
	    task "task$counter" {
	        doLast {
	            println "I'm task number $counter"
	        }
	    }
	}
	
	> gradle -q task1
	I'm task number 1

## 1.7 操作现有的任务
一旦任务被创建出来，就可以通过API进行访问。例如，可以在运行时给任务动态添加依赖

	4.times { counter ->
	    task "task$counter" {
	        doLast {
	            println "I'm task number $counter"
	        }
	    }
	}
	task0.dependsOn task2, task3
	
	> gradle -q task0
	I'm task number 2
	I'm task number 3
	I'm task number 0

或者可以给一些已经存在任务添加行为

	task hello {
	    doLast {
	        println 'Hello Earth'
	    }
	}
	hello.doFirst {
	    println 'Hello Venus'
	}
	hello.doLast {
	    println 'Hello Mars'
	}
	hello {
	    doLast {
	        println 'Hello Jupiter'
	    }
	}
	
	> gradle -q hello
	Hello Venus
	Hello Earth
	Hello Mars
	Hello Jupiter

- doFirst和doLast 可以多次调用。它们分别添加一个action到任务的开始以及结束。当任务执行时，action列表中的aciton会依次执行

## 1.8 快捷符号
Gradle提供了一个便捷的符号去访问一个已存在的任务！即每个任务都可以作为一个构建脚本的属性。

通过构建脚本的属性形式访问任务：
	
	task hello {
	    doLast {
	        println 'Hello world!'
	    }
	}
	hello.doLast {
	    println "Greetings from the $hello.name task."
	}
	
	> gradle -q hello
	Hello world!
	Greetings from the hello task.

## 1.9 额外的任务属性
可以给任务添加自定义属性。通过在任务中添加`ext.propertyName = 'xx'`即可添加自定义属性.在定义之后，就可以像预定义的任务属性那样去读取和设置

	task myTask {
	    ext.myProperty = "myValue"
	}
	
	task printTaskProperties {
	    doLast {
	        println myTask.myProperty
	    }
	}
	
	> gradle -q printTaskProperties
	myValue

- 自定义属性不局限于任务可以使用。详情可以参考：[Extra properties](https://docs.gradle.org/current/userguide/writing_build_scripts.html#sec:extra_properties)

## 1.10 使用Ant任务
	
	task loadfile {
	    doLast {
	        def files = file('../antLoadfileResources').listFiles().sort()
	        files.each { File file ->
	            if (file.isFile()) {
	                ant.loadfile(srcFile: file, property: file.name)
	                println " *** $file.name ***"
	                println "${ant.properties[file.name]}"
	            }
	        }
	    }
	}

	> gradle -q loadfile
	 *** agile.manifesto.txt ***
	Individuals and interactions over processes and tools
	Working software over comprehensive documentation
	Customer collaboration  over contract negotiation
	Responding to change over following a plan
	 *** gradle.manifesto.txt ***
	Make the impossible possible, make the possible easy and make the easy elegant.
	(inspired by Moshe Feldenkrais)

- 具体的请参考[Using Ant From Gradle](https://docs.gradle.org/current/userguide/ant.html)

## 1.11 Using Mehtods
Gradle扩展了如何组织构建逻辑。为上文的例子组织构建逻辑的第一个层次是提取一个方法

	task checksum {
	    doLast {
	        fileList('../antLoadfileResources').each { File file ->
	            ant.checksum(file: file, property: "cs_$file.name")
	            println "$file.name Checksum: ${ant.properties["cs_$file.name"]}"
	        }
	    }
	}
	
	task loadfile {
	    doLast {
	        fileList('../antLoadfileResources').each { File file ->
	            ant.loadfile(srcFile: file, property: file.name)
	            println "I'm fond of $file.name"
	        }
	    }
	}
	
	File[] fileList(String dir) {
	    file(dir).listFiles({file -> file.isFile() } as FileFilter).sort()
	}

	> gradle -q loadfile
	I'm fond of agile.manifesto.txt
	I'm fond of gradle.manifesto.txt

- 这样抽取出来的方法可以在多项目构建中的子项目之间可以共享。详情见[Organizing Build Logic](https://docs.gradle.org/current/userguide/organizing_build_logic.html)

## 1.12 默认任务
Gradle允许定义一个或多个默认执行的任务，在没有执行特定任务时
	
	defaultTasks 'clean', 'run'
	
	task clean {
	    doLast {
	        println 'Default Cleaning!'
	    }
	}
	
	task run {
	    doLast {
	        println 'Default Running!'
	    }
	}
	
	task other {
	    doLast {
	        println "I'm not a default task!"
	    }
	}
	
	> gradle -q
	Default Cleaning!
	Default Running!

- 在多项目构建中，每个项目都可以有自己特定的默认任务，如果子项目不指定默认任务，那么会使用父项目的默认任务(如果已定义则不会执行父项目的默认任务)

- 有问题。我在Gradle4.0.1上 如果子项目没有指定默认任务，并不会默认执行父项目的默认任务

## 1.13 由DAG进行配置
如[The Build Lifecycle](https://docs.gradle.org/current/userguide/build_lifecycle.html)所示。Gradle有一个**配置阶段和一个执行阶段**。在配置阶段之后，Gradle已经知道所有应该执行的任务，Gradle提供了一个hook去使用这些信息。

一个用例就是：在任务被执行之前，会去检查release任务是否会被执行，然后可以分配不同的值给不同的变量。

如下示例中执行distribution和release任务会导致version变量值不同：

	task distribution {
	    doLast {
	        println "We build the zip with version=$version"
	    }
	}
	
	task release(dependsOn: 'distribution') {
	    doLast {
	        println 'We release now'
	    }
	}
	
	gradle.taskGraph.whenReady {taskGraph ->
	    if (taskGraph.hasTask(release)) {
	        version = '1.0'
	    } else {
	        version = '1.0-SNAPSHOT'
	    }
	}

	> gradle -q distribution
	We build the zip with version=1.0-SNAPSHOT
	
	> gradle -q release
	We build the zip with version=1.0
	We release now

- 最重要的部分就是 release任务执行之前被whenReady影响。即使release任务不是主要任务(即gradle命令执行的任务)也会受到影响.