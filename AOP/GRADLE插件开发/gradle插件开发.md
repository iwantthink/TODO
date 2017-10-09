
---
一定要注意Android studio 中gradle的版本,gradle插件和gradle是不同的。前者用来配置环境使得AS支持gradle，后者是用于Gradle开发
---
# 1.自定义任务类
Gradle支持俩种类型的Task：

1. 简单类型的Task，可以通过一些`action Closure` 定义。`action closure`确定了Task的行为

2. 增强型的Task，行为是内置到Task里的，另外Task提供了属性用于配置行为。大多数的Gradle plugin 都是使用的增强型Task，只需要声明任务并使用其属性配置任务

## 1.1 增强型任务
- 增强型任务的属性或行为通过一个类来定义，需要指定Task的type或class

## 1.2 Task class 源码存放位置

- **Build script：**可以直接在构建脚本中定义任务类。有利于任务类自动编译并，并且这个任务类自动的会被包含在构建脚本的类路径中。缺点就是任务类在构建脚本外部不可见，所以就无法在没有定义任务类的地方去使用

- **buildScr Project：**可以将任务类的源码放在 `rootProjectDir/buildSrc/src/main/groovy`目录下。Gradle会编译和测试插件，并使其在构建脚本的类路径上可用。另外任务类对构建使用的每个构建脚本都可见。但是其他项目没有定义的项目里 依旧无法使用。

- **Standalone project：**在独立的项目里编写任务类,打成Jar包使用 或发布到仓库，之后可以直接引用。


## 1.3 编写简单的任务类
- 要实现自定义任务类，需要创建一个类并继承`DefaultTask`类

- 通过`TaskAction`注解 向任务类添加方法，当任务执行时，Gradle会自动调用该action

- 定义的时候可以不使用方法+注解的 方式 替 任务类添加行为。可以在创建任务类对象的时候调用doFirst()或doLast()的cloasure 来添加

- 可以替任务类 添加属性，这样在定义任务类的时候 就可以设置这个属性的值


	class GreetingTask extends DefaultTask{
		String name = 'name from greetingTask'

		@TaskAction
		def greet(){
			println 'hello from greetingTask'
		}
	}

	//use the default greeting
	task hello(type:GreetingTask)

	//customize the greeting
	task greeting(type:GreetingTask){
		name = 'hello from greeting'
	}

## 1.4 独立项目中的任务类
### 1.4.1 定义
- 把任务类放到单独的一个项目中，可以发布出来 让别人引用。

- 项目的build.gradle 写法,需要引入groovy插件，添加Gradle Api为编译时依赖
		apply plugin: 'groovy'

		dependencies {
    		compile gradleApi()
    		compile localGroovy()
		}

- `src/main/groovy/org/gradle/GreetingTask.groovy`下放置任务类

### 1.4.2 引用

		buildscript {
    		repositories {
        		maven {
            		url uri('../repo')
        		}
    		}
    		dependencies {
        		classpath group: 'org.gradle', name: 'customPlugin',
                  version: '1.0-SNAPSHOT'
    		}
		}

		task greeting(type: org.gradle.GreetingTask) {
    		greeting = 'howdy!'
		}





# 2.自定义插件
**插件源码放置位置：**
- Build Script :直接在构建脚本文件（build.gradle）中编写，缺点是无法复用插件代码(在其他项目中使用的话 需要复制gradle文件)

- buildSrc Project:将插件源码放到 `rootProjectDir/buildSrc/src/main/groovy `目录下。Gradle会编译和测试插件，并使其在构建脚本的类路径上可用。另外插件对构建使用的每个构建脚本都可见。但是其他项目没有定义的项目里 依旧无法使用。

- Standalone project:在独立的项目里编写插件,打成Jar包使用 或发布到仓库，之后可以直接引用。



## 2.1 编写简单的插件
以下的例子都是在build.gradle中编写的,有些GradleAPI 在AndroidStudio中 不存在，因为Gradle的版本问题。。一些API 在特定版本之后才出现
---
- 给出的例子中设置的插件类型是Project类型的，可以在Plugin<>泛型中设置更多的类型参数（目前不太可能）

		apply plugin:GreetingPlugin //直接依赖 去使用！
	
		class GreetingPlugin implements Plugin<Project> {
	    	void apply(Project project) {
	        	project.task('hello') {
	            	doLast {
	               		println "Hello from the GreetingPlugin"
	            	}
	        	}
	    	}
		}

## 2.2 从构建中获取输入信息
- 其实就是从apply插件的 build.gradle 中 传递参数给插件！

- 大多数插件需要从构建脚本获取一些配置，可以通过`extension objects`方法实现。具体就是与Gradle Project 相关联的一个 `ExtensionContainer`对象实现参数的传递。

	- 插件中定义 
			class GreetingPlugin implements Plugin<Project> {
    			void apply(Project project) {
        			// 添加扩展对象
        			project.extensions.create("greeting", GreetingPluginExtension)
        			// 添加一个使用配置的任务
        			project.task('hello') {
            			doLast {
                			println project.greeting.message
            			}
        			}
    			}
			}

			class GreetingPluginExtension {
    			def String message = 'Hello from GreetingPlugin'
				
				Closure cl
			}
	- 使用插件
			apply plugin:GreetingPlugin
			//方式1
			greeting.message = 'hello from gradle'
			greeting.cl = {println 'hello plugin'}
			//方式2 通过Closure 配置
			greeting{
				cl {println 'xxxxx'}
				message 'xxxxx'
			}

## 2.3 处理自定义任务类和插件用到 文件的情况
- 当定义任务类和插件时，如果用到了file 最好能够比较灵活的去使用，例如解析文件 越迟越好。 大概意思就是 这种赖加载的方式有助于先设置属性值，等到真正执行时才去读取这个属性值。
		//定义
		class GreetingToFileTask extends DefaultTask {

    		def destination

    		File getDestination() {
        		project.file(destination)
    		}

    		@TaskAction
    		def greet() {
        		def file = getDestination()
        		file.parentFile.mkdirs()
        		file.write 'Hello!'
    		}
		}
		//使用
		task greet(type: GreetingToFileTask) {
    		destination = { project.greetingFile }
		}

		task sayGreeting(dependsOn: greet) {
    		doLast {
        		println file(greetingFile).text
    		}
		}

		ext.greetingFile = "$buildDir/hello.txt"

## 2.4 将extension properties 映射到 task properties
- Gradle API 提供了可变类型，`PropertyState`表示可以在执行时间内进行懒加载。`PropertyState.set(T)`设置值，`Provider.get()`获取值

- 定义
		class GreetingPlugin implements Plugin<Project> {
		    void apply(Project project) {
		        def extension = project.extensions.create('greeting', GreetingPluginExtension, project)
		        project.tasks.create('hello', Greeting) {
		            message = extension.message
		            outputFiles = extension.outputFiles
		        }
		    }
		}
		
		class GreetingPluginExtension {
		    final PropertyState<String> message
		    final ConfigurableFileCollection outputFiles
		
		    GreetingPluginExtension(Project project) {
		        message = project.property(String)
		        message.set('Hello from GreetingPlugin')
		        outputFiles = project.files()
		    }
		
		    void setOutputFiles(FileCollection outputFiles) {
		        this.outputFiles.setFrom(outputFiles)
		    }
		}
		
		class Greeting extends DefaultTask {
		    final PropertyState<String> message = project.property(String)
		    final ConfigurableFileCollection outputFiles = project.files()
		
		    void setOutputFiles(FileCollection outputFiles) {
		        this.outputFiles.setFrom(outputFiles)
		    }
		
		    @TaskAction
		    void printMessage() {
		        outputFiles.each {
		            logger.quiet "Writing message 'Hi from Gradle' to file"
		            it.text = message.get()
		        }
		    }
		}
	

- 使用
		apply plugin: GreetingPlugin
		
		greeting {
		    message = 'Hi from Gradle'
		    outputFiles = files('a.txt', 'b.txt')
		}

## 2.5 独立项目

1. 首先需要在build.gradle中应用groovy插件，并添加Gradle API
		apply plugin: 'groovy'
		
		dependencies {
		    compile gradleApi()
		    compile localGroovy()
		}
2. 提供一个`.properties`文件放到`META-INF/gradle-plugins`文件夹下,文件名就是插件的id(就是在build.gradle时 apply plugin:'文件名',建议与自己设置的gourpId+artifactId相符合)，implementation-class 指向具体的实现类(请填写完整的类名)
		implementation-class=org.gradle.GreetingPlugin

3. 在其他项目中使用自定义的插件
	- 这是发布到maven的情况
	 		apply plugin: 'org.samples.greeting'
	
			buildscript {
			    repositories {
			        maven {
			            url uri('../repo')
			        }
			    }
			    dependencies {
			        classpath group: 'org.gradle', name: 'customPlugin',
			                  version: '1.0-SNAPSHOT'
			    }
			}

	- 还可以将插件发布到[Gradle plugin portal](https://plugins.gradle.org/),就可以使用以下引用方式
			plugins {
			    id 'com.jfrog.bintray' version '0.4.1'
			}


## 2.6 为插件提供DSL
- 通过`extension object` 可以为插件添加项目属性和DSL块

### 2.6.1 嵌套的DSL元素
- 要创建一个嵌套的DSL元素，需要使用`ObjectFactory`对象来生成一个装饰过的对象

- 如下例子中，插件通过构造函数将 项目的`ObjectFactory`传递给`extension object`


		class Person {
		    String name
		}
		
		class GreetingPluginExtension {
		    String message
		    final Person greeter
		
		    @javax.inject.Inject
		    GreetingPluginExtension(ObjectFactory objectFactory) {
		        // Create a Person instance
		        greeter = objectFactory.newInstance(Person)
		    }
		
		    void greeter(Action<? super Person> action) {
		        action.execute(greeter)
		    }
		}
		
		class GreetingPlugin implements Plugin<Project> {
		    void apply(Project project) {
		        // Create the extension, passing in an ObjectFactory for it to use
		        def extension = project.extensions.create('greeting', GreetingPluginExtension, project.objects)
		        project.task('hello') {
		            doLast {
		                println "${extension.message} from ${extension.greeter.name}"
		            }
		        }
		    }
		}
		
		apply plugin: GreetingPlugin
		
		greeting {
		    message = 'Hi'
		    greeter {
		        name = 'Gradle'
		    }
		}


### 2.6.2 配置对象集合
Managing a collection of objects

	class Book {
	    final String name
	    File sourceFile
	
	    Book(String name) {
	        this.name = name
	    }
	}
	
	class DocumentationPlugin implements Plugin<Project> {
	    void apply(Project project) {
	        // Create a container of Book instances
	        def books = project.container(Book)
	        books.all {
	            sourceFile = project.file("src/docs/$name")
	        }
	        // Add the container as an extension object
	        project.extensions.books = books
	    }
	}
	
	apply plugin: DocumentationPlugin
	
	// Configure the container
	books {
	    quickStart {
	        sourceFile = file('src/docs/quick-start')
	    }
	    userGuide {
	
	    }
	    developerGuide {
	
	    }
	}
	
	task books {
	    doLast {
	        books.each { book ->
	            println "$book.name -> $book.sourceFile"
	        }
	    }
	}

- 使用`Project.container()`方法创建了一些`NamedDomainObjectContainer`类的实例,这个类 具有许多方法来管理和配置对象。
- 为了使用任何具有`project.container`方法的类型，它必须将名为`name`的属性公开为对象的唯一名称和常量名称。 容器方法的project.container（Class）变体通过尝试调用具有单个字符串参数的类的构造函数来创建新的实例，该参数是对象的所需名称。 查看允许自定义实例化策略的project.container方法变体的上述链接。

# 3.Gradle插件开发实例
本文基于 Android studio 开发，其实也可以通过idea 开发。

## 3.1 插件开发设置
1. 新建Android项目，选择Library项目
2. 更改项目结构为以下结构
	
		src
		├── main
		|	  └─ groovy
		|	  |		└─ com.pkg
		|	  |		      └─ xxx.groovy
		|	  └─ resources
		|			└─ META-INF
		|					└─ gradle-plugins
		|							└─ xxx.properties
		└── build.gradle


- groovy 下的路径为包名+具体groovy文件,groovy文件中编写具体插件逻辑
		package com.hypers

		import org.gradle.api.Plugin
		import org.gradle.api.Project

		class GreetingPlugin implements Plugin<Project> {

    		@Override
    		void apply(Project project) {
				project.task 'sayHello'<<{
					println 'hello groovy'
				}
			}
		}

- resources/META-INF/gradle-plugins目录下的xxx.properties填写内容
	>implementation-class=包名+插件名

## 3.2 发布到本地仓库
目前4.1版本的gradle 有俩种方式进行发布操作
1. [The Maven Plugin](https://docs.gradle.org/current/userguide/maven_plugin.html#useMavenPlugin)
2. [Maven Publishing](https://docs.gradle.org/current/userguide/publishing_maven.html)

- 发布例子：
		apply plugin:'maven'

		group='cn.edu.zafu.gradle.plugin'
		version='1.0.0'

		uploadArchives {
    		repositories {
        		mavenDeployer {
            		repository(url: uri('../repo'))
        		}
    		}
		}
	- group和version会被作为maven库的坐标的一部分

- 使用例子：
		apply plugin: 'com.hypers.GreetingPlugin'

		buildscript {
    		repositories {
        	maven {
            	url uri('../repo') //插件所在的目录
        		}
    		}

    		dependencies {
        		classpath 'com.hypers:GreetingPlugin:0.1' //添加依赖
    		}
	
		}
	- apply plugin 后面引用的名字就是之前resources下定义的xxx.properties的文件名
	- classpath 就是使用 gradle中定义的group,version以及moduleName


## 3.3 发布到Jcenter仓库


