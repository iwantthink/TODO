# 深入了解Tasks
[more about tasks](https://docs.gradle.org/current/userguide/more_about_tasks.html)

[深入了解Tasks](http://wiki.jikexueyuan.com/project/GradleUserGuide-Wiki/more_about_tasks/README.html)

# 1. 简介

Gradle 可以创建更为强大复杂的任务. 这些任务可以有它们自己的属性和方法. 这一点正是和 Ant targets 不一样的地方. 既可以使用自己创建的增强任务，也可以使用Gradle内置的增强任务


# 2. 任务输出

当Gradle执行任务时，会通过Tooling API在在控制台UI中使用标签标记任务的不同结果。这些标签是基于任务是否有要执行的动作，是否应执行这些动作，是否执行了这些动作，以及这些动作是否发生了变化

- **标签：** `(no label) or EXECUTED  `  
 
	**说明：**任务的动作已经执行完成
	
	**情况：**
	
	1. 当任务有动作，并且gradle确定task是作为构建的一部分
	2. 当任务没有动作或一些依赖，以及任何依赖已经被执行


- **标签：** `UP-TO-DATE `

	**说明：**任务输出没有改变
	
	**情况：**

	 1. 当一个任务有输入有输出并且这些没有改变
	 2. 当一个任务有action，并且task输出没有改变
	 3. 当一个任务没有action，但是有 dpendencies 。并且这些dependencies 已经是UP-TO-DATE,SKIPPED or from CACHE
	 4. 当一个任务没有acton，也没有dependencies

- **标签:**` FROM-CACHE `

	**说明：**任务的输出是从之前的执行结果中获得

	**情况:**

	1. 当任务的输出恢复自构建缓存中


- **标签：** `SKIPPED `

	**说明：**任务action被跳过，没有执行其动作

	**情况:**

	1. 当一个任务被明确的从 命令行中排除在外
	2. 当一个任务拥有一个`onlyIf`断言，且返回了false


- **标签：** `NO-SOURCE `

	**说明：**任务不需要执行其action

	**情况:**
		
	1. 任务有输入有输出，但是没有source ，例如：source files are .java files for JavaCompile



# 3. 定义 tasks

**除了[构建脚本基础]()中定义任务的形式 (keyword 形式). 还有一些变化的定义形式来适应某些特殊的情况**.


## 3.1 方式1(使用字符串作为任务名称)
	
	// build.gradle
	
	task('hello') {
		doLast{
			println "hello"
		}
	}
	
	task('copy', type: Copy) {
	    from(file('srcDir'))
	    into(buildDir)
	}	
	
## 3.2 方式2(使用tasks容器定义任务)

	
	// build.gradle
	
	tasks.create('hello') {
	    doLast {
	        println "hello"
	    }
	}
	
	tasks.create('copy', Copy) {
	    from(file('srcDir'))
	    into(buildDir)
	}
	
- 这里实际上我们把任务加入到 `tasks`中,`tasks`就是一个集合，类型为[TaskContainer](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/TaskContainer.html)


## 3.3 方式3(Defining tasks using a DSL specific syntax)

Groovy DSL提供了特定的语法用于定义任务
	
	// build.gradle
	// Using Groovy dynamic keywords
	task(hello) {
		doLast{
			println "hello"
		}
	}
	
	task(copy, type: Copy) {
	    from(file('srcDir'))
	    into(buildDir)
	}

- 这里使用了Groovy动态关键字

# 4. 定位任务

为了配置任务或者使用任务作为依赖,经常需要在构建文件里找到已经定义的任务


## 4.1 方式1(Accessing tasks using a DSL specific syntax)
Groovy DSL 提供了特定的语法用于定位任务

	task hello
	task copy(type: Copy)
	
	// Access tasks using Groovy dynamic properties on Project
	
	println hello.name
	println project.hello.name
	
	println copy.destinationDir
	println project.copy.destinationDir
	
-  **在Project上使用Groovy动态属性访问任务**
	
## 4.2 方式2(使用tasks容器定位任务)
			
	task hello
	task copy(type: Copy)
	
	println tasks.hello.name
	println tasks.named('hello').get().name
	
	println tasks.copy.destinationDir
	println tasks.named('copy').get().destinationDir
	
## 4.3 方式3	(使用路径定位任务)

通过`tasks.getByPath()`方法可以在任意项目中访问任务

- 该方法可以传递任务名称，相对路径或者绝对路径

示例：
	
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
	
	
- 参考 [TaskContainer](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/TaskContainer.html) 可以知道跟多关于定位 tasks 的选项


# 5. 配置任务

举一个例子, 创建一个自定义的Copy任务，并对其进行配置:

	task myCopy(type: Copy)
	
- 上面创建了一个没有默认行为的`Copy`类型任务. 这个任务可以通过`Copy`API 来配置

- **Gradle可以有许多具有同样type，但是名字不同的任务. 这个在实现特定类型的所有任务的 cross-cutting concerns 时特别有用**

以下展示了多种方式来实现同一个配置

## 5.1 方式1（使用API配置任务）
		
	Copy myCopy = task(myCopy, type: Copy)
	myCopy.from 'resources'
	myCopy.into 'target'
	myCopy.include('**/*.txt', '**/*.xml', '**/*.properties')
	
- 这跟在Java中配置对象是一样的形式. 每次都必须在语句里重复上下文 (`myCopy`). 这种方式写起来有点多余

## 5.2 方式2(Configuring a task using a DSL specific syntax)
	
	
	// Configure task using Groovy dynamic task configuration block
	myCopy {
	   from 'resources'
	   into 'target'
	}
	myCopy.include('**/*.txt', '**/*.xml', '**/*.properties')
	
- 使用Groovy的动态任务配置块对任务进行配置	
	
- 这种方式适用于任何任务.这里访问任务的方式只是`tasks.getByName()`方法的快捷方式

- **特别注意:这里使用的闭包会在 task 配置的时候执行而不是在 task 运行的时候执行**


## 5.3 方式3（定义task时使用闭包配置）
		
	task copy(type: Copy) {
	   from 'resources'
	   into 'target'
	   include('**/*.txt', '**/*.xml', '**/*.properties')
	}
	
- **通过闭包配置任务是公认的最具可读性的方式**	
	
- **请不要忘了构建的各个阶段**

	一个任务有配置和动作. 当使用doLast时, 只是简单的使用快捷方式定义了动作. **定义在配置区域的代码只会在构建的配置阶段执行, 而且不论执行哪个任务**
	
	
# 6. 传递参数给任务构造函数	
不仅可以在任务创建之后再给任务设置属性，任务允许通过任务类的构造函数向其传递参数。 为了将值传递给Task构造函数，必须使用注解`@javax.inject.Inject`对相关构造函数进行注解


## 6.1 方式1(使用`@Inject`注释构造函数)

	class CustomTask extends DefaultTask {
	    final String message
	    final int number
	
	    @Inject
	    CustomTask(String message, int number) {
	        this.message = message
	        this.number = number
	    }
	}
	
在定义完类之后，就可以在创建时向其传递参数：	

1. 使用`TaskContainer`创建带参数的任务,将构造函数所需的参数附加在参数列表的最后

		tasks.create('myTask', CustomTask, 'hello', 42)

2. 使用Map创建带构造函数的任务

		task myTask(type: CustomTask, constructorArgs: ['hello', 42])
		
- **推荐使用`TaskContainer`去创建任务**		

- 在任何情况下，参数都必须非空，否则Gradle会抛出一个`NullPointException`

	
# 7.  向任务添加依赖

Gradle提供了许多种定义任务依赖的方法. [构建脚本基础-第四小节]()已经介绍了**如何使用任务的名称定义依赖**

- 任务名称可以用来指向同一个项目里的任务, 或者其他项目里的任务. **为了指向其他项目, 必须在任务的名称前加入项目的路径**

## 7.1 方式1(将任务依赖于另一个项目的任务)

下面的例子中，任务`projectA:taskX `依赖于任务` projectB:taskY `

	project('projectA') {
	    task taskX {
	        dependsOn ':projectB:taskY'
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
	
gradle -q taskX 的输出
	
	> gradle -q taskX
	taskY
	taskX
	

## 7.2 方式2(使用任务对象添加依赖)

**除了使用任务名称, 还可以使用任务对象来定义依赖**
	
	//build.gradle
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
	
gradle -q taskX 的输出

	> gradle -q taskX
	taskY
	taskX
	
## 7.3 方式3(通过闭包(lazy block)设置依赖)	
当计算时，`lazy block` 会被传递给当前正在被计算其依赖关系的任务，然后将`lazy block`返回的一个或者一组Task对象作为依赖

- `lazy block`使用闭包作为其表现形式


接下来的例子给任务`taskX `加入了一个复杂的依赖, 所有任务名称以`lib`开头的任务都将在`taskX` 之前执行:
	
	task taskX {
	    doLast {
	        println 'taskX'
	    }
	}
	
	// Using a Groovy Closure
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
	
gradle -q taskX 的输出

	> gradle -q taskX
	lib1
	lib2
	taskX
	


# 8. Ordering tasks

在某些情况下，需要控制任务的执行顺序，但是又不能明确的添加依赖关系

- **任务排序和任务依赖的主要区别在于，排序规则不影响将执行哪些任务，而仅影响执行顺序(任务排序不表示任务之间的任何执行依赖)**

**任务排序使用于如下功能:**

1. 强制指定执行任务的顺序：例如`build`任务永远在`clean`任务之后执行

2. 在构建的早期进行构建验证：例如在开始release构建之前，先验证是否有正确的凭据

3. 在一个耗时长的验证任务之前运行一个短耗时的验证任务，以获得更快的反馈：例如单元测试在继承测试之前

4. 一个任务用来汇总特定类型的所有任务的结果：例如测试报告任务组合所有执行的测试任务的结果

## 8.1 排序规则

**排序规则有俩个：**`must run after` and `should run after`

- 当使用`must run after`去指定taskB必须在taskA之后运行，那么当taskA,taskB同时运行时，taskB必须在taskA之后执行，这个表达式为`taskB.mustRunAfter(taskA)`

- `should run After`与`must run after`相似，但是有俩种情况会被忽略。**建议`should run after`用在不是特别严格要求的但是有帮助的地方**


要指定俩个任务之间的`should run after`和`must run after`,Gradle提供了`Task.mustRunAfter(java.lang.Object[])`和`Task.shouldRunAfter(java.lang.Object[])`方法

- 这俩个方法可以接收 任务实例，任务名称或者任何其他`Task.dependsOn[java.lang.Object[]]`接收的参数。


## 8.2 失效情况

排序规则仅在被声明顺序的俩个任务同时执行时才起作用

当使用命令行指令`--continue`时，是有可能在任务A执行失败之后继续执行任务B

1. 在`ordering cycle`中对任务排序，`shouldRunAfter`将会失效,`mustRunAfter`会报错

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

2. 当使用并行执行，并且除了`shouldRunAfter`的任务之外的所有任务的依赖都已经执行，那么任务就会忽略`shouldRunAfter`的那个任务是否执行过


3. 当声明了任务顺序，但仅执行一个任务，这时任务顺序将失效，**因为任务排序并不意味着任务执行**

	> gradle -q taskY
	taskY
	
	

# 9. 添加任务描述
可以添加任务描述到任务，当执行`gradle tasks`时会显示此描述

	task copy(type: Copy) {
	   description 'Copies the resource directory to the target directory.'
	   from 'resources'
	   into 'target'
	   include('**/*.txt', '**/*.xml', '**/*.properties')
	}

# 10. 替换任务
自定义的任务通过`override`属性，可以替换原先已经存在的任务。例如，想要用一个不同类型的任务替换一个Java　plugin中的任务

示例：

	task copy(type: Copy)
	
	task copy(overwrite: true) {
	    doLast {
	        println('I am the new one.')
	    }
	}

	> gradle -q copy
	I am the new one.

- 用自定义的任务去替换了一个使用相同名称的`Copy`类型任务，则必须将新任务的`overwrite`属性设置为true，否则Gradle会抛出一个异常提示任务名称已经存在

# 11. 跳过任务

Gradle提供了多种方式来跳过任务执行

- 如果使用Gradle提供的任务，这个特性将非常有用。因为它允许向任务的内置action添加条件


## 11.1 使用断言(predicate)

可以通过`onlyIf()`方法给任务添加一个断言(predicate),那么任务将仅在断言为true时才执行

- 可以将closure作为一个predicate实现。

	这个closure将被作为一个参数传递给任务，并且需要返回true/false,分别代表当前任务是否需要执行

- **predicate将在任务执行之前被计算**。

**示例**：

	task hello {
	    doLast {
	        println 'hello world'
	    }
	}
	
	hello.onlyIf { !project.hasProperty('skipHello') }

	> gradle hello -PskipHello
	:hello SKIPPED
	
## 11.2 使用StopExecutionException

如果跳过任务的逻辑无法用predicate表示，那么可以使用一个`StopExecutionException`

- 如果这个异常是在action中抛出，那么这个action之后的逻辑以及这个任务的下一个action都会被跳过，但是构建会继续执行下一个任务	

**示例：**

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


## 11.3 启用或禁用任务

每个任务都有一个`enabled`的标志，默认值是true。将其值设置为false会阻止该任务的任何action去执行

- 一个被禁用的任务会被`SKIPPED`标签标记

**示例：**

	task disableMe {
	    doLast {
	        println 'This should not be printed if the task is disabled.'
	    }
	}
	disableMe.enabled = false
	
	> gradle disableMe
	:disableMe SKIPPED
		
	
## 11.4 设置任务超时

每个任务都有一个`timeout`属性，该属性可以用来限制任务的执行时间。当一个任务超时后，执行该任务的线程会被中断，该任务会被认定为执行失败	

- 终结任务(`Finalizer task`)将在超时之后继续被执行

- 如果使用`--continue`选项，其他任务将在当前超时的任务之后接着执行

- 如果任务对中断不响应，那么超时无效，Gradle所有内置任务都能及时响应超时


示例：

	task hangingTask() {
	    doLast {
	        Thread.sleep(100000)
	    }
	    timeout = Duration.ofMillis(500)
	}

	

# 12 Up-to-date checks(增量构建)

任何构建工具的一个重要组成部分是能够避免执行已完成的工作

- 考虑一下编译的过程，一旦源文件编译完成，那么除非一些改动影响了输出 否则是不需要重新编译源文件的,因为编译可能会消耗大量时间，所以在不需要时跳过这一步可以节省很多时间。

Gradle通过一个**Increamental Build 增量构建**的功能来支持这种行为。输出内容中任务名称旁的`UP-TO-DATE`标签就是这种增量构建的表现 ，可以查看2.1小节。

接下来介绍增量构建如何工作，如何在自己的任务中去使用

## 12.1 任务的输入和输出
在大多数的情况下，一个任务需要一些输入并产生一些输出。比如前面一个编译示例，可以看到输入是源文件，并且在Java情况下，输出是一些类文件，其他的一些输入信息包含是否包含调试信息。

![](https://docs.gradle.org/current/userguide/img/taskInputsOutputs.png)

- 正如图中所示,输入的一个重要特征是它会影响一个或多个输出。根据不同的源文件内容和运行代码的Java runtime 最低版本，会生成不同的字节码。

	作为增量构建的一部分，Gradle会测试输入或输出从上次构建以来是否有变化，如果没有变化，则Gradle认为这个任务是最新的，因此跳过该任务的action。**注意增量构建只对那些起码有一个输出的任务起作用，通常任务起码会有一个输出**

以上所说对构建作者的意义是：需要告诉Gradle哪些任务属性是输入，哪些任务属性是输出。如果任务属性影响了输出，请确保将这个任务属性设置为输入，否则任务将被认为已经是最新（实际上不是）。相反的，不要将那些不影响输出的 任务属性设置为输入，否则任务会在不需要执行时执行。还要小心那些不确定的任务，这些任务可能以相同的输入产生不同的输出，这种任务不应该被配置为增量构建.

### 12.1.1 如何将任务属性设置为 输入和输出

如果实现一个自定义类作为任务，那么只用俩步就可以实现增量更新：

1. 为每个任务的输入和输出创建类型化属性(通过getter方法)

2. 为这些属性添加适当的`annotation`(注解)

	注解必须放在getter或Groovy的属性上。放在setter或没有对应添加了注解的getter的java字段上则会被忽略
	
	
**Gradle 支持三种输入输出的主要类型：**
	
1. Simple values

	比如String,numbers等。通常来说，simple values 可以是任何实现了`Serializable`接口的类型

2. Filesystem types

	标准的`File`类，并且来自派生自Gradle的`FileCollection`
	
	或者是任何可以作为方法`Project.file(java.lang.Object)`和`Project.files(Java.lang.Object[])`参数的对象

3. Nested values
			
	与另外俩个类型不同的自定义类型，有自己的输入和输出属性。实际上，任务的输入和输出被嵌套在这些自定义类型中


### 12.1.2 示例

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

- `templateEngine`: 

	代表处理源模板使用哪个引擎(例如,FreeMarker,Velocity)。可以将其实现为字符串，在当前例子中，使用自定义枚举来实现了。由于枚举类型自动实现了Serializable,所以可以将其视为一个Simple value 并使用`@input`注解

- `sourceFiles`:

	任务将要处理的源模板。单个文件或文件集合需要各自不同的注解。在当前例子中处理的是文件集合，所以使用`@InputFiles`注解

- `templateData`:

	表示模型数据.在当前例子中，使用了一个自定义类来表示模型数据，但是其并没有实现`Serializable`所以不能使用`@Input`.使用了`@Nested`来让Gradle知道这是一个嵌套输入属性的值

- `outputDir`:

	生成文件所在的目录.和输入文件一样，有几个不同的注解用来表示输出文件或目录。单个目录就使用`@OutputDirectory`

这些注解的意思是，如果Gradle执行任务时，如果发现这些被注解修饰没有发生变化，Gradle将跳过该任务。

**增量构建相关的注解，以及更多内容去查看[Incremental Tasks](https://docs.gradle.org/current/userguide/custom_tasks.html#incremental_tasks)**

## 12.2 Runtime Api

自定义任务类是使用增量构建的一个简单方式，但如果不能使用自定义任务类这种方式，Gradle还提供了可用于任何任务的替代Api

**更多内容去查看[More about Tasks 19.10.1](https://docs.gradle.org/current/userguide/more_about_tasks.html)**


## 12.3 增量构建如何工作？

当一个任务是首次执行时, Gradle 会取一个输入的快照 (snapshot). 该快照包含组输入文件和每个文件的内容的散列. 然后当 Gradle 执行任务时, 如果任务成功完成，Gradle 会获得一个输出的快照. 该快照包含输出文件和每个文件的内容的散列. Gradle 会保留这两个快照用来在该任务的下一次执行时进行判断.

之后, 每次在任务执行之前, Gradle 都会为输入和输出取一个新的快照, 如果这个快照和之前的快照一样, Gradle 就会假定这个任务已经是最新的 (up-to-date) 并且跳过任务, 反之亦然.

需要注意的是, 如果一个任务有指定的输出目录, 自从该任务上次执行以来被加入到该目录的任务文件都会被忽略, 并且不会引起任务过时 (out of date). 这是因为不相关任务也许会共用同一个输出目录




# 13. Task rules

如果一个任务的生成，需要依托于已经定义好的取值范围或者特定规则，可以通过`Task rules`


## 13.1 生成任务

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

- `addRule`传入的字符串参数的作用是对规则的描述，会在`gradle tasks`时显示

- **闭包内是其具体的规则，这个规则会在命令行执行时生效**,当命令中的执行的任务名称未定义时，会将其作为参数传递给闭包


## 13.2 依赖

**规则并不只是在通过命令行调用任务的时使用. 还可以根据`dependsOn `关系创建基于规则的任务**

**示例2：** 基于规则的任务依赖

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

# 14. 终结任务(`Finalizer tasks`)

当计划运行终结任务时，终结任务将会被添加到任务图(`task graph`)中

- `Finalizer task`指的是无论运行结果如何，最后都会执行的任务

**要指定一个Finalizer task 可以使用`Task.finalizedBy(Java.lang.Object[])`**方法。这个方法可以接收 任务实例，任务名称，或者`Task.dependsOn(Obj)`方法可以接收的参数

## 14.1 添加一个终结任务

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

- 即使最终任务失败，`Finalizer tasks`也会被执行

## 14.2 为失败的任务添加终结任务

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

	FAILURE: Build failed with an exception.


- 如何终结任务没有任何逻辑时并不会执行，举个栗子，如果终结任务被认为是`up to date`或者 终结任务所依赖的任务失败,那么终结任务不会执行

- 终结任务适用于 无论构建成功或失败都必须清理资源的情况

# 15. 生命周期任务

**生命周期任务是不能自行完成的任务,它们通常没有任何action**

生命周期任务可以表示几个概念：

- 工作流程步骤(通过`check`进行检查)

- 可构建的东西

- 可以执行许多相同逻辑任务的任务（通过`compileAll`执行所有编译任务）

Gradle基础插件定义了几个标准的生命周期任务，例如`build,assemble,check`等。所有语言的插件都会继承该基础插件，从而获得同样的一套生命周期任务

- [Java Plugin](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks)

- [Base Plugin](https://docs.gradle.org/current/userguide/base_plugin.html#sec:base_tasks)

除非生命周期任务有自己的action，否则结果由其依赖决定，如果有任何任务的依赖被执行，生命周期任务将被认为`EXECUTED `。如果所有的任务依赖是最新的,被忽略的,或来自缓存的,那么生命周期任务也将被认为已经是`UP-TO-DATE`
	