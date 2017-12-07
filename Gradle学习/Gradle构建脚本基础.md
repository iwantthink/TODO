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

**任务排序使用于如下功能:**

- 强制指定执行任务的顺序：例如`build`任务永远在`clean`任务之后执行

- 在构建的早起进行构建验证：例如在开始release构建之前，先验证是否有正确的凭据

- 在一个耗时长的验证任务之前运行一个耗时短的验证任务，以获得更快的反馈：例如单元测试在继承测试之前

- 一个任务用来汇总特定类型的所有任务的结果：例如测试报告任务组合所有执行的测试任务的结果


排序规则有俩个可用：`must run after` and `should run after`

- 当使用`must run after`去指定taskB必须在taskA之后运行，那么taskA运行时 taskB也一定会运行，这个表达式`taskB.mustRunAfter(taskA)`。

- `should run After`与`must run after`相似，但是有俩种情况会被忽略。**建议`should run after`用在不是特别严格要求的但是有帮助的地方**

	- 第一种，如果使用任务排序 引入了一个排序循环(或者说依赖循环？)

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
			task taskZ {
			    doLast {
			        println 'taskZ'
			    }
			}
			taskX.dependsOn taskY
			taskY.dependsOn taskZ
			taskZ.shouldRunAfter taskX

			> gradle -q taskX
			taskZ
			taskY
			taskX

	- 第二种，当使用并行执行且除了`should run after`之前任务的所有依赖都已经确定，那么任务就会忽略`should run after`的那个任务是否执行过。


- 在这些规则之下，仍然有可能执行了taskA而没执行taskB

示例1:添加`must run after`任务顺序

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
	taskY.mustRunAfter taskX

	> gradle -q taskY taskX
	taskX
	taskY

示例2：添加`should run after`任务顺序

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
	taskY.shouldRunAfter taskX
	
	> gradle -q taskY taskX
	taskX
	taskY

在上面的示例中，仍然有可能执行了taskY而没有执行taskX,**因为任务排序并不意味着任务执行**，例如:

	> gradle -q taskY
	taskY

要指定俩个任务之间的`should run after`和`must run after`,通过`Task.mustRunAfter(java.lang.Object[])`和`Task.shouldRunAfter(java.lang.Object[])`方法指定。这俩个方法可以接收 任务实例，任务名称或者任何其他`Task.dependsOn[java.lang.Object[]]`接收的参数。

需要注意`B.mustRunAfter(A)`或`B.shouldRunAfter(A)`并不表示任务之间的任何执行依赖

- 可以单独的执行任务A 或 任务B，排序规则仅在俩个任务同时执行时才起作用

- 当使用命令行指令`--continue`时，是有可能在任务A执行失败之后继续执行任务B

## 2.7 添加任务描述
可以添加任务描述到任务，当执行`gradle tasks`时会显示此描述

	task copy(type: Copy) {
	   description 'Copies the resource directory to the target directory.'
	   from 'resources'
	   into 'target'
	   include('**/*.txt', '**/*.xml', '**/*.properties')
	}

## 2.8 替换任务
任务可以被替换。例如，想要用一个不同类型的任务替换一个Java　plugin中的任务

示例1：

	task copy(type: Copy)
	
	task copy(overwrite: true) {
	    doLast {
	        println('I am the new one.')
	    }
	}

	> gradle -q copy
	I am the new one.

- 示例1表示用自定义的任务去替换了一个`Copy`类型的任务，因为它们使用相同的名称。另外必须将新任务的`override`属性设置为true，否则Gradle会抛出一个异常提示任务名称已经存在。

## 2.9 跳过任务
Gradle提供了多种方式来跳过任务执行

### 2.9.1 Using a predicate
可以通过`onlyIf()`方法给任务添加一个predicate,那么任务仅在predicate为true时才执行。

可以将predicate作为一个closure实现。这个closure将被作为一个参数传递给任务，如果任务需要执行则返回true，不需要则返回false。

predicate将在任务执行之前被计算。

**示例1**：通过predicate跳过任务

	task hello {
	    doLast {
	        println 'hello world'
	    }
	}
	
	hello.onlyIf { !project.hasProperty('skipHello') }

	> gradle hello -PskipHello
	:hello SKIPPED
	
	BUILD SUCCESSFUL in 0s

### 2.9.2 使用StopExecutionException
如果跳过任务的逻辑无法用predicate表示，那么可以使用一个`StopExecutionException`.如果这个异常是由一个action抛出，那么这个action进一步的执行以及这个任务的下一个action都会被跳过，但是构建会继续执行下一个任务

**示例1：**通过StopExecutionException跳过任务

	task compile {
	    doLast {
	        println 'We are doing the compile.'
	    }
	}
	
	compile.doFirst {
	    // Here you would put arbitrary conditions in real life.
	    // But this is used in an integration test so we want defined behavior.
	    if (true) { throw new StopExecutionException() }
	}
	task myTask(dependsOn: 'compile') {
	    doLast {
	        println 'I am not affected'
	    }
	}
	
	> gradle -q myTask
	I am not affected

- 这个功能对Gradle内置的任务非常有用，它允许开发者添加带条件的build-in action


### 2.9.3 启用或禁用任务
每个任务都有一个`enabled`的标志，默认值是true。将其值设置为false会阻止执行任何该任务的action.

一个被禁用的任务会被`SKIPPED`标签标记

**示例1：**启用和禁用任务

	task disableMe {
	    doLast {
	        println 'This should not be printed if the task is disabled.'
	    }
	}
	disableMe.enabled = false
	
	> gradle disableMe
	:disableMe SKIPPED
	
	BUILD SUCCESSFUL in 0s


## 2.10 Up-to-date checks(AKA Incremental Build)
任何构建工具的一个重要组成部分是能够避免执行已完成的工作。考虑一下编译的过程，一旦源文件编译完成，那么除非一些改动影响了输出 否则是不需要重新编译源文件的。例如源文件被修改了，输出文件被移除了。编译可能会消耗大量时间，所以在不需要时跳过这一步可以节省很多时间。

Gradle通过一个**Increamental Build 增量构建**的功能来支持这种行为。输出内容中任务名称旁的`UP-TO-DATE`标签就是这种增量构建的表现 ，可以查看2.1小节。

接下来介绍增量构建如何工作，如何在自己的任务中去使用
### 2.10.1 任务的输入和输出
在大多数的情况下，一个任务需要一些输入并产生一些输出。比如前面举的一个编译示例，可以看到输入是源文件，并且在Java情况下，输出是一些类文件，其他的一些输入信息包含是否包含调试信息。

![](https://docs.gradle.org/current/userguide/img/taskInputsOutputs.png)

输入的一个重要特征是它会影响一个或多个输出，正如图中所示。根据不同的源文件内容和运行代码的Java runtime 最低版本，会生成不同的字节码。

作为增量构建的一部分，Gradle会测试输入或输出从上次构建以来是否有变化，如果没有变化，则Gradle认为这个任务是最新的，因此跳过该任务的action。**注意增量构建只对那些起码有一个输出的任务起作用，通常任务起码会有一个输出**

以上所说对构建作者的意义是：需要告诉Gradle哪些任务属性是输入，哪些任务属性是输出。如果任务属性影响了输出，请确保将这个任务属性设置为输入，否则任务将被认为已经是最新（实际上不是）。相反的，不要将那些不影响输出的 任务属性设置为输入，否则任务会在不需要执行时执行。还要小心那些不确定的任务，这些任务可能以相同的输入产生不同的输出，这种任务不应该被配置为增量构建.

**关于如何将任务属性设置为 输入和输出：**

- 自定义任务类型

	如果将一个自定义任务作为一个类来实现，那么只用俩步就可以实现增量更新：

	1. 为每个任务的输入和输出创建类型化属性(通过getter方法)

	2. 为这些属性添加适当的`annotation`(注解),注解必须放在getter或Groovy的属性上。放在setter或没有对应的getter的java 字段上会被忽略
	
	- Gradle 支持三种输入输出的主要类型：
	
		1. Simple values

			比如String,numbers等。通常来说，simple values 可以是任何实现了`Serializable`接口的类型

		2. Filesystem types

			标准的文件类，Gradle的`FileCollection`的派生类，以及任何可以作为方法`Project.file(java.lang.Object)`和`Project.files(Java.lang.Object[])`参数的对象

		3. Nested values
			
			与另外俩个类型不同的自定义类型，有自己的输入和输出属性。实际上，任务的输入和输出被嵌套在这些自定义类型中

	举个栗子：一个任务需要处理不同类型的模板(Free Marker,Velocity,Moustache等)。这个任务需要将模块源文件与模型数据相结合以生成模板文件的填充版本
	
	这个任务有三个输入和一个输出：模块源文件，模型数据，模板引擎 以及作为输出的 输出文件输出地址

	当自定义任务时可以很方便的通过**注解**设置属性为输入和输出。如下是一个有输入和输出的任务结构

			//buildSrc/src/main/java/org/example/ProcessTemplates.java
			
			package org.example;
			
			import java.io.File;
			import java.util.HashMap;
			import org.gradle.api.*;
			import org.gradle.api.file.*;
			import org.gradle.api.tasks.*;
			
			public class ProcessTemplates extends DefaultTask {
			    private TemplateEngineType templateEngine;
			    private FileCollection sourceFiles;
			    private TemplateData templateData;
			    private File outputDir;
			
			    @Input
			    public TemplateEngineType getTemplateEngine() {
			        return this.templateEngine;
			    }
			
			    @InputFiles
			    public FileCollection getSourceFiles() {
			        return this.sourceFiles;
			    }
			
			    @Nested
			    public TemplateData getTemplateData() {
			        return this.templateData;
			    }
			
			    @OutputDirectory
			    public File getOutputDir() { return this.outputDir; }
			
			    // + setter methods for the above - assume we’ve defined them
			
			    @TaskAction
			    public void processTemplates() {
			        // ...
			    }
			}	

			package org.example;
			
			import java.util.HashMap;
			import java.util.Map;
			import org.gradle.api.tasks.Input;
			
			public class TemplateData {
			    private String name;
			    private Map<String, String> variables;
			
			    public TemplateData(String name, Map<String, String> variables) {
			        this.name = name;
			        this.variables = new HashMap<>(variables);
			    }
			
			    @Input
			    public String getName() { return this.name; }
			
			    @Input
			    public Map<String, String> getVariables() {
			        return this.variables;
			    }
			}
			//*************输出结果***********************
			> gradle processTemplates
			:processTemplates
			
			BUILD SUCCESSFUL in 0s
			1 actionable task: 1 executed
			
			> gradle processTemplates
			:processTemplates UP-TO-DATE
			
			BUILD SUCCESSFUL in 0s
			1 actionable task: 1 up-to-date

	- templateEngine: 代表处理源模板使用哪个引擎(例如,FreeMarker,Velocity)。可以将其实现为字符串，在当前例子中，使用自定义枚举来实现了。由于枚举类型自动实现了Serializable,所以可以将其视为一个Simple value 并使用`@input`注解
	- sourceFiles:任务将要处理的源模板。单个文件或文件集合需要各自不同的注解。在当前例子中处理的是文件集合，所以使用`@InputFiles`注解
	- templateData:表示模型数据.在当前例子中，使用了一个自定义类来表示模型数据，但是其并没有实现`Serializable`所以不能使用`@Input`.使用了`@Nested`来让Gradle知道这是一个嵌套输入属性的值
	- outputDir:生成文件所在的目录.和输入文件一样，有几个不同的注解用来表示输出文件或目录。单个目录就使用`@OutputDirectory`

	这些注解的意思是，如果Gradle执行任务时，如果发现这些被注解修饰没有发生变化，Gradle将跳过该任务。

	**更多内容去查看[More about Tasks 19.10.1](https://docs.gradle.org/current/userguide/more_about_tasks.html)**

- Runtime Api

	自定义任务类是使用增量构建的一个简单方式，但如果不能使用自定义任务类这种方式，Gradle还提供了可用于任何任务的可替代的Api

	**更多内容去查看[More about Tasks 19.10.1](https://docs.gradle.org/current/userguide/more_about_tasks.html)**

### 2.10.2 增量构建如何工作？

### 2.10.3 Advanced techniques

### 2.10.4 Stale task outputs

## 2.11 Task rules
有时需要一个任务的行为依赖于一个用哦与极大或无穷的数值范围的参数。一个很好的方式就是使用task rules

**示例1**：Task rule

	tasks.addRule("Pattern: ping<ID>") { String taskName ->
	    if (taskName.startsWith("ping")) {
	        task(taskName) {
	            doLast {
	                println "Pinging: " + (taskName - 'ping')
	            }
	        }
	    }
	}
	
	> gradle -q pingServer1
	Pinging: Server1

- `addRule`传入的String 参数作用是规则的描述，会在`gradle tasks`时显示

**规则不仅能用在命令行调用任务时，还能创建任务依赖关系的规则**

**示例2：** Dependency on rule based tasks

	tasks.addRule("Pattern: ping<ID>") { String taskName ->
	    if (taskName.startsWith("ping")) {
	        task(taskName) {
	            doLast {
	                println "Pinging: " + (taskName - 'ping')
	            }
	        }
	    }
	}
	
	task groupPing {
	    dependsOn pingServer1, pingServer2
	}
	
	> gradle -q groupPing
	Pinging: Server1
	Pinging: Server2


## 2.12 Finalizer tasks
在最终任务执行之后，Finalizer task会被添加到task graph中.

**要指定一个Finalizer task 可以使用`Task.finalizedBy(Java.lang.Object[])`**方法。这个方法可以接收 任务实例，任务名称，或者`Task.dependsOn(Obj)`方法可以接收的参数

**示例1：** Finalizer tasks

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
	
	taskX.finalizedBy taskY

	> gradle -q taskX
	taskX
	taskY

- 即使最终任务失败，Finalizer tasks也会被执行

**示例2：**

	task taskX {
	    doLast {
	        println 'taskX'
	        throw new RuntimeException()
	    }
	}
	task taskY {
	    doLast {
	        println 'taskY'
	    }
	}
	
	taskX.finalizedBy taskY
	
	> gradle -q taskX
	taskX
	taskY

- Finalizer task 在最终的任务没有做任何事时不会执行，举个栗子，如果最终任务被认为是up to date 或者 依赖的任务失败

- Finalizer task 在无论构建成功或失败都必须清理资源的情况下特别适合

## 2.13 生命周期任务
生命周期任务自身是不做什么事情的。通常没有任何任务action。生命周期任务可以表示几个概念：

- 一个工作流程步骤()

- 一个可构建的东西

- 一个可以执行许多相同逻辑任务的任务

许多Gradle插件定义了自己的生命周期任务，以便于做特定的事情。当开发自己的插件时，应该考虑提供自己的生命周期任务 或者 使用Gradle已经提供的一些生命周期任务。[可以参考 Java Plugin](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks)

除非生命周期任务有自己的action，否则结果由其依赖项决定，如果有任何任务的依赖项被执行，生命周期任务将被认为执行过了。如果所有的任务依赖项是up-to-date,skipped,or from cache 生命周期任务将被认为已经是up-to-date