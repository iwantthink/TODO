# 深入了解Tasks
[more about tasks](https://docs.gradle.org/current/userguide/more_about_tasks.html)

[深入了解Tasks](http://wiki.jikexueyuan.com/project/GradleUserGuide-Wiki/more_about_tasks/README.html)

# 1. 简介

Gradle 可以创建更为强大复杂的任务. 这些任务可以有它们自己的属性和方法. 这一点正是和 Ant targets 不一样的地方. 这些强大的任务既可以由你自己创建也可以使用 Gradle 内建好的


# 2. 任务输出


当Gradle执行任务时，会通过Tooling API在在控制台UI中使用标签标记任务的不同结果。这些标签是基于任务是否有要执行的动作，是否应执行这些动作，是否执行了这些动作，以及这些动作是否发生了变化。

- **标签：** (no label) or EXECUTED    
 
	**说明：**任务已经执行完成
	
	**情况：**
	
	1. 当任务有动作且gradle确定task 是作为构建的一部分
	2. 当任务没有动作或一些依赖，以及任何依赖已经被执行


- **标签：**UP-TO-DATE    

	**说明：**任务输出没有改变
	
	**情况：**

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

	**情况:**
		
	1. 任务有输入有输出，但是没有source ，例如：source files are .java files for JavaCompile



# 3. 定义 tasks

**除了构建脚本基础中定义任务的形式 (keyword 形式). 还有一些变化的定义形式来适应某些特殊的情况**.


## 3.1 方式1

比如下面的例子中任务名被括号括起来了. 这是因为之前定义简单任务的形式 (keyword 形式) 在表达式里是不起作用的.

例子 15.1. Defining tasks using a DSL specific syntax
	
	build.gradle
	
	task(hello) << {
	    println "hello"
	}
	
	task(copy, type: Copy) {
	    from(file('srcDir'))
	    into(buildDir)
	}
	
## 3.2 方式2	
	
你也可以使用 strings 来定义任务的名字：

例子 15.2. 使用字符串来定义任务的名字

	build.gradle
	
	task('hello') <<
	{
	    println "hello"
	}
	
	task('copy', type: Copy) {
	    from(file('srcDir'))
	    into(buildDir)
	}
	
## 3.3 方式3	
	
还有另外一种语法形式来定义任务, 更加直观:

例子 15.3. 使用tasks定义任务
	
	build.gradle
	
	tasks.create(name: 'hello') << {
	    println "hello"
	}
	
	tasks.create(name: 'copy', type: Copy) {
	    from(file('srcDir'))
	    into(buildDir)
	}
	
- 这里实际上我们把任务加入到 tasks collection 中. 可以看一看 [TaskContainer](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/TaskContainer.html) 来深入了解下.


# 4. 定位任务

经常需要在构建文件里找到定义的任务, 举个例子, 为了配置它们或者使用它们作为依赖. 

有许多种方式都可以来实现定位.

## 4.1 方式1

每一个任务都是一个 project 的有效属性, 该属性使用任务名来作为属性名:

例子 15.4. 通过属性获取 tasks
	
	build.gradle
	
	task hello
	
	println hello.name
	println project.hello.name
	
## 4.2 方式2
		
例子 15.5. 通过 tasks collection 获取 tasks
	
	build.gradle
	
	task hello
	
	println tasks.hello.name
	println tasks['hello'].name
	
## 4.3 方式3	
	
可以使用 `tasks.getByPath()` 方法通过任务的路径来使用任何 project 里的任务

-  `getByPath()` 方法的输入可以使用 任务的名字, 任务的相对路径或者绝对路径作为

例子 15.6. 通过路径获取 tasks
	
	build.gradle
	
	project(':projectA') {
	    task hello
	}
	
	task hello
	
	println tasks.getByPath('hello').path
	println tasks.getByPath(':hello').path
	println tasks.getByPath('projectA:hello').path
	println tasks.getByPath(':projectA:hello').path
gradle -q hello 的输出

	> gradle -q hello
	:hello
	:hello
	:projectA:hello
	:projectA:hello
	
- 参考 [TaskContainer](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/TaskContainer.html) 可以知道跟多关于定位 tasks 的选项


# 5. 配置任务

举一个例子, 创建一个自定义的Copy任务，并对其进行配置:

例子 15.7. 创建一个 copy task
	
	build.gradle
	
	task myCopy(type: Copy)
	
- 它创建了一个没有默认行为的 copy 任务. 这个任务可以通过它的 API 来配置(参考 Copy). 

- 补充说明一下, 这个 task 的名字是 “myCopy”, 但是它是 “Copy” 类(type). 

	**Gradle可以有许多具有同样type，但是名字不同的任务. 这个在实现特定类型的所有任务的 cross-cutting concerns 时特别有用**

## 5.1 方式1

例子 15.8. 使用API配置一个任务
	
	build.gradle
	
	Copy myCopy = task(myCopy, type: Copy)
	myCopy.from 'resources'
	myCopy.into 'target'
	myCopy.include('**/*.txt', '**/*.xml', '**/*.properties')
	
- 这与通过 Java 配置对象是一样的形式. 但是你每次都必须在语句里重复上下文 (myCopy). 这种方式可能读起来并不是那么的漂亮.

## 5.2 方式2

通过闭包配置任务是公认的最具可读性的方式

例子 15.9. 通过闭包 closure配置一个任务
	
	build.gradle
	
	task myCopy(type: Copy)
	
	myCopy {
	   from 'resources'
	   into 'target'
	   include('**/*.txt', '**/*.xml', '**/*.properties')
	}
	
- `tasks.getByName()` 方法的一种简洁的写法. 特别要注意的是, 如果你通过闭包的形式来实现 `getByName()` 方法, 这个闭包会在 task 配置的时候执行而不是在 task 运行的时候执行.


## 5.3 方式3
在定义 task 时使用闭包配置任务

例子 15.10. 通过定义一个任务
	
	build.gradle
	
	task copy(type: Copy) {
	   from 'resources'
	   into 'target'
	   include('**/*.txt', '**/*.xml', '**/*.properties')
	}
	
- 请不要忘了构建的各个阶段.

一个任务有配置和动作. 当使用 << 时, 你只是简单的使用捷径定义了动作. 定义在配置区域的代码只会在构建的配置阶段执行, 而且不论执行哪个任务. 可以参考第 55 章, The Build Lifecycle for more details about the build lifecycle.