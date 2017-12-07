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

## 1.14 更多任务的信息
了解更多关于task的细节，参考[More About Tasks](https://docs.gradle.org/current/userguide/more_about_tasks.html)


# 2 More about Tasks
上文已经介绍了如果创建简单的任务，现在可以学习如何创建任务之间的依赖,给任务添加其他行为 等等。

Gradle支持 enhanced tasks(增强型任务)，即这些任务拥有自己的属性和方法，这与使用Ant目标完全不同，这些增强型任务是自己编写的或者内置到Gradle中的

## 2.1 任务结果
当Gradle执行任务时，会在控制台UI中通过Tooling API标记任务的不同结果。这些标签是基于任务是否有要执行的动作，是否应执行这些动作，是否执行了这些动作，以及这些动作是否发生了变化。

- **标签：** (no label) or EXECUTED     
	**说明：**任务已经执行完成
	**情况： **
	
	1. 当任务有动作且gradle确定task 是作为构建的一部分
	2. 当任务没有动作或一些依赖，以及任何依赖已经被执行


- **标签：**UP-TO-DATE    
	**说明：**任务输出没有改变
	**情况： **

	 1. 当一个任务有输入有输出并且这些没有改变
	 2. 当一个任务有action，并且task输出没有改变
	 3. 当一个任务没有action，但是有 dpendencies 。并且这些dependencies 已经是UP-TO-DATE,SKIPPED or from CACHE
	 4. 当一个任务没有acton，也没有dependencies


- **标签:**FROM-CACHE 
	**说明：**任务的输出是从之前的执行结果中获得
	**情况:**

	1. 当任务的输出恢复自构建缓存中


- **标签：**SKIPPED 
	**说明：**任务action'被跳过，没有执行其动作
	**情况:**

	1. 当一个任务被明确的从 命令行中排除在外
	2. 当一个任务拥有一个`onlyIf`断言，且返回了false


- **标签：**NO-SOURCE 
	**说明：**任务无需执行其action
	**情况: **
		
	1. 任务有输入有输出，但是没有source ，例如：source files are .java files for JavaCompile


## 2.2 定义任务
已知用 关键词形式定义任务，但是也存在一些关键词形式不起作用的情况，以下提供几种定义任务的形式

示例1：定义任务

	task(hello) {
	    doLast {
	        println "hello"
	    }
	}
	
	task(copy, type: Copy) {
	    from(file('srcDir'))
	    into(buildDir)
	}

示例2：使用字符串作为任务名称

	task('hello') {
	    doLast {
	        println "hello"
	    }
	}
	
	task('copy', type: Copy) {
	    from(file('srcDir'))
	    into(buildDir)
	}

示例3：使用替代的语法去定义任务,在这个方式中，会将任务添加到`tasks`集合,[更多关于TaskContainer的 文档](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/TaskContainer.html)

	tasks.create(name: 'hello') {
	    doLast {
	        println "hello"
	    }
	}
	
	tasks.create(name: 'copy', type: Copy) {
	    from(file('srcDir'))
	    into(buildDir)
	}

## 2.3 查找任务
**每个任务都可以作为项目的属性进行使用，将任务名称作为属性名称使用**

	task hello
	
	println hello.name
	println project.hello.name

任务可以通过`tasks`集合进行使用:

	task hello
	
	println tasks.hello.name
	println tasks['hello'].name

可以通过`tasks.getByPath()`方法在任何项目中访问任务。`getByPath()`方法可以传入 任务名称,相对路径，绝对路径。

	project(':projectA') {
	    task hello
	}
	
	task hello
	
	println tasks.getByPath('hello').path
	println tasks.getByPath(':hello').path
	println tasks.getByPath('projectA:hello').path
	println tasks.getByPath(':projectA:hello').path

	> gradle -q hello
	:hello
	:hello
	:projectA:hello
	:projectA:hello

## 2.4 配置任务
当一个任务既有configuration 又有action时，当使用`doLast`时只是定义了一个action。**任务的配置部分的代码将在构建的配置阶段执行，无论目标是什么任务。**

以下是Gradle提供的几个创建`Copy`类型的任务的示例

**示例1**：普通的配置任务形式1

	task myCopy(type: Copy)

- 这创建了一个没有默认行为的复制任务.该任务可以通过其API进行配置.[详见Copy Docs](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Copy.html)

	注意，可以同时拥有多个同一类型的任务，但是任务名称不同。

**示例2**：普通的配置任务形式2

	Copy myCopy = task(myCopy, type: Copy)
	myCopy.from 'resources'
	myCopy.into 'target'
	myCopy.include('**/*.txt', '**/*.xml', '**/*.properties')

- 这与Java中配置对象的方式类似，必须在每句语句中重复上下文(myCopy)

**示例3**：通过closure去配置任务

	task myCopy(type: Copy)
	
	myCopy {
	   from 'resources'
	   into 'target'
	   include('**/*.txt', '**/*.xml', '**/*.properties')
	}

- **这种形式使用于任何任务。示例中的第三行`myCopy`是`tasks.getByName()`的快捷方式。如果传递一个closure给`getByName()`方法，这个closure会被应用为任务的配置，而不是在任务执行时去使用**

**示例4**：在定义任务时使用一个closure

	task copy(type: Copy) {
	   from 'resources'
	   into 'target'
	   include('**/*.txt', '**/*.xml', '**/*.properties')
	}

## 2.5 替任务添加依赖
在1.5小节中，已经知道简单的相同项目下的依赖添加方式，接下来提供几个更多的 依赖添加方式

**示例1**：给一个任务添加来自其他项目的任务依赖

	project('projectA') {
	    task taskX(dependsOn: ':projectB:taskY') {
	        doLast {
	            println 'taskX'
	        }
	    }
	}
	
	project('projectB') {
	    task taskY {
	        doLast {
	            println 'taskY'
	        }
	    }
	}

	> gradle -q taskX
	taskY
	taskX

**示例2**：使用一个任务的对象来添加依赖
	
	task taskX {
	    doLast {
	        println 'taskX'
	    }
	}
	
	task taskY {
	    doLast {
	        println 'taskY'
	    }
	}
	
	taskX.dependsOn taskY
	
	> gradle -q taskX
	taskY
	taskX



对于更高级的用法，可以使用一个closure定义一个任务的依赖.closure应该返回一个task或一个task的集合，这些task或task集合会被认为是依赖。

**示例3**：通过closure添加依赖

	task taskX {
	    doLast {
	        println 'taskX'
	    }
	}
	
	taskX.dependsOn {
	    tasks.findAll { task -> task.name.startsWith('lib') }
	}
	
	task lib1 {
	    doLast {
	        println 'lib1'
	    }
	}
	
	task lib2 {
	    doLast {
	        println 'lib2'
	    }
	}
	
	task notALib {
	    doLast {
	        println 'notALib'
	    }
	}

	> gradle -q taskX
	lib1
	lib2
	taskX

## 2.6 Ordering tasks
在某些情况下，需要控制2个任务的执行顺序，但是又不能明确的添加依赖关系。**任务排序和任务依赖的主要区别在于，排序规则不影响将执行哪些任务，而仅影响执行顺序**

