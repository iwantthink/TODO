# Gradle的日志输出
[Working with logging](https://docs.gradle.org/current/userguide/logging.html)

# 1. 简介

日志是构建工具的主要UI，如果它太冗长，那么很容易隐藏真正的警告和问题。 另一方面，相关信息可以用来确定问题是否出错

- Gradle定义了6个日志级别，如下面的日志级别所示。 除了通常可以看到的日志级别之外，还有两个特定的Gradle日志级别。 这俩个级别分别是`QUIET`和`LIFECYCLE`,后者是默认值，用于报告构建进度

# 2. 日志级别

日志类型|日志解释
---|---
ERROR	|错误信息
QUIET	|重要消息信息
WARNING	|警告信息
LIFECYCLE	| 进度消息信息
INFO	| 信息消息
DEBUG	| 调试信息

# 3. 选择日志等级

你可以在命令行中选择如[3.1 命令行的日志等级选项]()所示的选项选择不同的日志级别. 此外，Gradle还允许在[`gradle.properties`文件](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties)中指定日志等级

## 3.1 命令行的日志等级选项

选项|输出日志等级
---|---
`no logging options`|	LIFECYCLE及更高
`-q `or` --quiet`|	QUIET及更高
`-i `or` --info`|	INFO及更高
`-d `or` --debug`|	DEBUG及更高(所有日志信息)


## 3.2 堆栈信息选项

选项	|含义
---|---
`No stacktrace options`| 在构建错误时，不会输出栈踪信息到控制台(例如编译错误).仅在内部异常出现时打印堆栈信息.如果选择DEBUG日志等级,总会打印截断的堆栈信息
`-s `or` --stacktrace`	|打印截断的堆栈信息,Gradle不建议输出完整的堆栈信息,Groovy完整的堆栈十分冗长(由于底层的动态调用机制。然而，它们通常不包含代码中出了什么错的相关信息)。这个选项会弃用警告信息
`-S `or` --full-stacktrace`|	打印全部堆栈信息。这个选项会弃用警告信息


# 4. 编写自己的日志信息
在构建文件输出日志的简单方法是将消息写入标准输出. Gradle将写入标准输出的任何内容重定向到QUIET日志级别的日志记录系统

## 4.1 编写log信息输出到stdout

	//build.gradle
	println 'A message which is logged at QUIET level'


## 4.2 在构建脚本中使用logger属性
Gradle为构建脚本提供了一个`logger`属性，这是`Logger`的一个实例.这个接口继承自`SLF4J Logger`接口并且加入了一些Gradle的具体方法


	//build.gradle
	logger.quiet('An info log message which is always logged.')
	logger.error('An error log message.')
	logger.warn('A warning log message.')
	logger.lifecycle('A lifecycle info log message.')
	logger.info('An info log message.')
	logger.debug('A debug log message.')
	logger.trace('A trace log message.')


## 4.3 使用占位符输出日志

使用典型的SLF4J模式将占位符替换为日志消息的实际值

	// build.gradle
	logger.info('A {} log message', 'info')


## 4.4 使用SLF4J编写日志

可以使用构建中的三方类（例如，buildSrc目录中的类）hook掉Gradle的日志记录系统,然后像使用构建脚本中提供的`logger`一样去使用这个三方类即可. 例如使用`SLF4J logger`

	//build.gradle
	import org.slf4j.LoggerFactory
	
	def slf4jLogger = LoggerFactory.getLogger('some-logger')
	slf4jLogger.info('An info log message logged using SLF4j')



# 5. 从外部工具和库记录日志

在内部, Gradle 使用 Ant 和 lvy , 俩者都有自己的日志系统, Gradle重定向他们的日志输出到 Gradle日志系统. 除了Ant/lvy的`TRACE`级别的日志是映射到Gradle的`DEBUG`级别, 其余的都是`1:1`的从 `Ant/lvy` 的日志等级映射到 Gradle 的日志等级. 这意味着默认的 Gradle 日志级别将不会显示任何的 Ant /lvy 的输出, 除非它是一个错误或警告


有许多工具仍然使用标准输出记录日志。默认情况下,Gradle将标准输出重定向到`QUIET`日志级别，将标准错误重定向到`ERROR`级别

- 此行为是可配置的。 项目对象提供了一个`LoggingManager`，它允许在评估构建脚本时更改标准输出或错误重定向到的日志级别

## 5.1 配置标准输出捕获

	//build.gradle
	logging.captureStandardOutput LogLevel.INFO
	println 'A message which is logged at INFO level'
	
## 5.2 为任务配置标准输出捕获	
	
任务同样提供了`LoggingManager`去更改任务执行过程中的标准输出或错误日志级别。

	//build.gradle
	task logInfo {
	    logging.captureStandardOutput LogLevel.INFO
	    doFirst {
	        println 'A task message which is logged at INFO level'
	    }
	}

-  Gradle还提供与Java Util Logging，Jakarta Commons Logging和Log4j日志记录工具包的集成。 构建中的类使用这些日志记录工具包编写的任何日志消息都将重定向到Gradle的日志记录系统



# 6. 改变Gradle的日志

Gradle支持用自定义的日志界面代替Gradle的日志界面。例如，如果您想以某种方式自定义UI(为了记录更多或更少的信息,或者为了更改日志格式). 那么通过使用`Gradle.useLogger(java.lang.Object)`方法替换日志记录。这可以通过构建脚本、init脚本或嵌入API访问

- 注意:这个方法将完全禁用Gradle的默认输出


## 6.1 自定义日志输出内容

下面是一个在初始化脚本中更改如何记录任务执行和构建完成


	// customLogger.init.gradle
	useLogger(new CustomEventLogger())
	
	class CustomEventLogger extends BuildAdapter implements TaskExecutionListener {
	
	    void beforeExecute(Task task) {
	        println "[$task.name]"
	    }
	
	    void afterExecute(Task task, TaskState state) {
	        println()
	    }
	
	    void buildFinished(BuildResult result) {
	        println 'build completed'
	        if (result.failure != null) {
	            result.failure.printStackTrace()
	        }
	    }
	}
	
	// 输出内容
	$ gradle -I customLogger.init.gradle build
	
	> Task :compile
	[compile]
	compiling source
	
	
	> Task :testCompile
	[testCompile]
	compiling test source
	
	
	> Task :test
	[test]
	running unit tests
	
	
	> Task :build
	[build]
	
	build completed
	3 actionable tasks: 3 executed


自定义的日志输出类可以实现下列任何监听器接口. 

- 注意，当注册了一个日志输出类，它仅仅替换掉自定义日志类所实现的接口，其余的仍然会使用Gradle默认的接口实现

自定义输出类能够实现的监听器接口:

- [BuildListener](https://docs.gradle.org/current/javadoc/org/gradle/BuildListener.html)

- [ProjectEvaluationListener](https://docs.gradle.org/current/javadoc/org/gradle/api/ProjectEvaluationListener.html)

- [TaskExecutionGraphListener](https://docs.gradle.org/current/javadoc/org/gradle/api/execution/TaskExecutionGraphListener.html)

- [TaskExecutionListener](https://docs.gradle.org/current/javadoc/org/gradle/api/execution/TaskExecutionListener.html)

- [TaskActionListener](https://docs.gradle.org/current/javadoc/org/gradle/api/execution/TaskActionListener.html)

