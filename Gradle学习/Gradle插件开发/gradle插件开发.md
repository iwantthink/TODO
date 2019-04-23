[拥抱 Android Studio 之五：Gradle 插件开发](http://blog.bugtags.com/2016/03/28/embrace-android-studio-gradle-plugin/)

[官方文档-自定义插件](https://docs.gradle.org/current/userguide/custom_plugins.html)

[使用buildSRC形式创建插件](https://www.jianshu.com/p/d53399cd507b)

[使用BuildSrc形式创建插件-2](http://www.heqiangfly.com/2016/03/15/development-tool-gradle-customized-plugin/)

[Gradle 使用指南 -- Plugin DSL 扩展](http://www.heqiangfly.com/2016/04/16/development-tool-gradle-customized-plugin-dsl-extension/)

[Gradle低于4.2的版本如何实现嵌套DSL](https://stackoverflow.com/questions/28999106/define-nested-extension-containers-in-gradle?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa)

[**Gradle编写自定义任务-4.10版本-官方文档**](https://docs.gradle.org/4.10/userguide/custom_tasks.html)

[**Gradle编写自定义插件-4.10版本-官方文档**](https://docs.gradle.org/4.10/userguide/custom_plugins.html)


[**Gradle编写自定义插件-5.1.1版本-官方文档**](https://guides.gradle.org/implementing-gradle-plugins/)

[**设计Gradle脚本-5.1.1版本-官方文档**](https://guides.gradle.org/designing-gradle-plugins/)

# 注意事项

**Android studio 中gradle的版本,gradle插件和gradle是不同的。**

- 前者用来配置环境使得AS支持gradle，后者是用于Gradle开发


# 1. 自定义任务

## 1.1 自定义任务的类型

Gradle支持俩种类型的Task：

1. 简单类型的Task，可以通过一些`action closure` 定义,`action closure`确定了Task的行为. 此类任务适用于在构建脚本中实现一次性任务.

2. 增强型的Task，行为是内置到任务中，并且Task提供了属性用于配置行为。大多数的Gradle plugin 都是使用的增强型Task，只需要声明任务并使用其属性配置任务,通过这种方式,增强型任务允许在许多不同的位置重复使用某种行为,并且允许在不同的构建中使用

	增强型任务的属性或行为通过任务的类来定义,声明增强任务时,可以指定任务的类型或类

## 1.2 任务类源码的存放位置

- **Build script：**可以直接在构建脚本中定义任务类。有利于任务类自动编译，并且这个任务类自动的会被包含在构建脚本的类路径中。缺点就是任务类在构建脚本外部不可见，所以就无法在定义任务类之外的地方去使用

- **buildScr Project：**可以将任务类的源码放在 `rootProjectDir/buildSrc/src/main/groovy`目录下（即和app文件夹在同一级下）。Gradle会编译和测试任务类，并使其在构建脚本的类路径上可用。另外这种位置的任务类对构建中的每个构建脚本都可见。但是其他项目没有定义的项目里 依旧无法使用。

- **Standalone project：**在独立的项目里编写任务类,打成Jar包使用 或发布到仓库，之后可以直接引用。通常,这种Jar包会包含一些自定义插件,或者将多个相关任务类捆绑到单个库中


### 1.2.1 构建脚本/buildSrc中使用任务

**要实现自定义任务类，需要创建一个类并继承`DefaultTask`类**

- 通过添加`@TaskAction`注解,标记任务类中的某个方法为action，当任务执行时，Gradle会自动调用该action

- 定义的时候可以不使用方法+注解的形式替任务类添加行为。可以在创建任务类对象的时候在配置closure中通过添加 `doFirst()`或`doLast()`方法来添加行为

- 可以替任务类 添加属性，这样在定义任务类的时候 就可以设置这个属性的值

		public class GreetingTask extends DefaultTask{
			// 定义了一个属性,使用默认值
			public String name = 'name from greetingTask'
	
			@TaskAction
			def greet(){
				println 'hello from greetingTask'
			}
		}

		//use the default greeting
		task hello(type:GreetingTask)
	
		//customize the greeting
		task greeting(type:GreetingTask){
			// 配置时,修改name属性的值
			name = 'hello from greeting'
		}

- **慎用name这个属性名...会导致任务执行错误(找不到该任务,实际上是存在的)**

### 1.2.2 独立项目中的任务类

把任务类放到插件中，可以发布出来 让别人引用。

示例:

1. 项目的`build.gradle`写法,需要引入groovy插件，添加Gradle Api为编译时依赖. [俩种不同的添加依赖的方式](https://stackoverflow.com/questions/32352816/what-the-difference-in-applying-gradle-plugin)

		apply plugin: 'groovy'
		
		//plugins{
		//	id 'groovy'
		//}
	
		dependencies {
			compile gradleApi()
			compile localGroovy()
		}

2. 创建Task类型

	在目录`src/main/groovy/org/gradle/GreetingTask.groovy`下放置任务类
	
		package org.gradle
		
		import org.gradle.api.DefaultTask
		import org.gradle.api.tasks.TaskAction
		
		class GreetingTask extends DefaultTask {
		    String greeting = 'hello from GreetingTask'
		
		    @TaskAction
		    def greet() {
		        println greeting
		    }
		}

3. 引用Task

		buildscript {
    		repositories {
        		maven {
					// 仓库地址为..本地
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

- 将任务类型暴露出去给其他项目使用,是需要依托插件的


## 1.3 增量任务

Gradle使用声明的输入和输出来确定该任务是否处于`up-to-date`状态,并且是否需要执行工作. 如果没有任何输入或输出发生更改,那么Gradle可跳过该任务.**Gradle将这种机制称之为增量构建支持**,这种机制的优势是可以提高构建的性能


[Up-to-date checks (AKA Incremental Build)](https://docs.gradle.org/5.0/userguide/more_about_tasks.html#sec:up_to_date_checks)

# 2.自定义插件

## 2.1 插件源码的位置

- `Build Script` :直接在构建脚本文件（`build.gradle`）中编写，缺点是无法复用插件代码(在其他项目中使用的话 需要复制gradle文件)

- `buildSrc Project`:将插件源码放到 `rootProjectDir/buildSrc/src/main/groovy `目录下。Gradle会编译和测试插件，并使其在构建脚本的类路径上可用。另外插件对构建使用的每个构建脚本都可见。但是其他项目没有定义的项目里 依旧无法使用。

- `Standalone project`:在独立的项目里编写插件,打成Jar包使用 或发布到仓库，之后可以直接引用。


### 2.1.1 构建脚本中编写插件

**以下的例子都是在`build.gradle`中编写的,有些GradleAPI 在AndroidStudio中 不存在，因为Gradle的版本问题。。一些API 在特定版本之后才出现**

要创建Gradle插件,需要编写一个类去实现`Plugin`接口.**当插件应用于项目时,Gradle会创建插件类的实例,并调用实例的`Plugin.apply()`方法**, 项目对应的`Project`对象会作为参数传递给`apply()`方法,插件可以使用它来对项目进行设置



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

- 给出的例子中设置的插件类型是`Project`类型的，可以在`Plugin<>`泛型中设置更多的类型参数（现在已经支持 Gradle/Setting/Project等）

	如果设置为`Project`,那么该插件可以应用于`build.gradle`

	如果设置为`Settings`,那么该插件可以应用于`settings.gradle`

	如果设置为`Gradle`,那么插件可以应用于`init.gradle`

- `build script`形式的插件，需要直接在编写插件的`build.gradle`中去apply插件，然后在项目中apply这个文件


### 2.1.2 独立项目中编写插件

将插件一到一个独立的项目中并发布就可以与其他人共享

示例:使用一个`Groovy`项目,将插件生成一个Jar包

1. 首先需要在`build.gradle`中应用groovy插件，并添加Gradle API

		apply plugin: 'groovy'
		
		dependencies {
		    compile gradleApi()
		    compile localGroovy()
		}
2. 提供一个`.properties`属性文件到`src/main/resources/META-INF/gradle-plugins/org.samples.greeting.properties`文件夹下

	**文件名就是插件的id**(就是在`build.gradle`时 `apply plugin:'文件名'`,建议与自己设置的`gourpId+artifactId`相符合)，`implementation-class` 指向具体的实现类(请填写完整的类名)

		implementation-class=org.gradle.GreetingPlugin

	- 属性文件名称要与插件ID匹配,其会被用作添加插件时的插件名称

		该文件是需要放在`resources`目录下

		需要使用`implementation-class`标识插件的实现类

	- `gradle-plugins`，可以指定多个properties文件，定义多个插件

	- properties文件名 

3. 在其他项目中使用自定义的插件

	- 发布到`maven`的情况

	 		apply plugin: 'org.samples.greeting'
	
			buildscript {
			    repositories {
			        maven {
			            url uri('../repo')
			            url 'E:/github/HOT_FIX/repo/'

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


### 2.1.3 buildSrc形式的插件

1. 为项目创建一个特殊目录 `rootProjectDir/buildSrc/src/main/groovy`

	此外，还需要在`buildSrc`根目录下创建`build.gradle`

	该构建脚本用来添加依赖，仓库，应用插件等操作

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1fputk0t5xyj20cl0ai74g.jpg)

2. 编写插件类

		package org.example.greeting;
		
		import org.gradle.api.Plugin;
		import org.gradle.api.Project;
		
		public class GreetingPlugin implements Plugin<Project> {
		    public void apply(Project project) {
		        project.getTasks().create("hello", Greeting.class, (task) -> { 
		            task.setMessage("Hello");
		            task.setRecipient("World");                                
		        });
		    }
		}

3. 应用插件

		apply plugin: org.example.greeting.GreetingPlugin

	- **成功编译之后，在当前目录 即可通过 `apply plugin:com.ryan.log.LOG`的形式进行添加插件，注意：不要使用字符串形式 **

	Gradle会编译和测试插件，并使其在构建脚本的类路径上可用。另外插件对构建使用的每个构建脚本都可见。但是其他项目没有定义的项目里 依旧无法使用。

声明插件标识符(可选)

大多数情况下,使用Id来应用插件会比使用类的全限定名称更容易

1. 创建如下目录`buildSrc/src/main/resources/META-INF/gradle-plugins/`

	- 必须按照规则创建目录,因为Gradle会尝试从特定位置解析属性文件

2. 创建`org.example.greeting.properties`

	- **注意: 该文件的名称中除了`.properties`文件扩展名之外的部分将成为插件的标识符**

3. 编写内容

	implementation-class=org.example.greeting.GreetingPlugin

	- Gradle使用此文件确定当前插件使用哪一个类作为插件的实现者

4. 应用插件

		plugins {
		  id 'org.example.greeting'
		}

## 2.2 使用Java Gradle插件开发插件(孵化中)

[使用Plugin Development 插件开发插件](https://docs.gradle.org/5.0/userguide/java_gradle_plugin.html),该插件可以消除构建脚本中的一些样板声明,并提供对插件元数据的验证等功能

该插件会自动应用`Java`插件,添加`gradleApi()`到依赖中,并在`jar`任务执行期间执行插件元数据的验证,最后还会在Jar的`META_INF`目录下生成插件描述符

**对于被开发的插件来说,需要添加`gradlePlugin{}`脚本块来声明插件信息,该脚本块定义了由项目构建的插件,包括插件id 和 implementationClass**

	gradlePlugin {
	    plugins {
			// 插件项目
	        simplePlugin {
	            id = 'org.gradle.sample.simple-plugin'
	            implementationClass = 'org.gradle.sample.SimplePlugin'
	        }
	    }
	}

- 根据`gradlePlugin{}`脚本块,Gradle会自动生成

	1. jar文件中`META-INF`目录下的插件描述符



### 2.2.1 示例

	plugins {
	    id 'java-gradle-plugin'
	}

- 添加到插件的项目中

	gradlePlugin {
	    plugins {
	        simplePlugin {
	            id = 'org.samples.greeting'
	            implementationClass = 'org.gradle.GreetingPlugin'
	        }
	    }
	}


## 2.3 测试自定义插件

使用`ProjectBuilder`类提供测试插件类所需要的`Project`实例

在此目录下编写测试类`src/test/groovy/org/gradle/GreetingPluginTest.groovy`

	class GreetingPluginTest {
	    @Test
	    public void greeterPluginAddsGreetingTaskToProject() {
	        Project project = ProjectBuilder.builder().build()
	        project.pluginManager.apply 'org.samples.greeting'
	
	        assertTrue(project.tasks.hello instanceof GreetingTask)
	    }
	}

## 2.4 从另外一个插件中应用插件


MyBasePlugin:

	import org.gradle.api.Plugin;
	import org.gradle.api.Project;
	
	public class MyBasePlugin implements Plugin<Project> {
	    public void apply(Project project) {
	        // define capabilities
	    }
	}


MyPlugin:

	
	import org.gradle.api.Plugin;
	import org.gradle.api.Project;
	
	public class MyPlugin implements Plugin<Project> {
	    public void apply(Project project) {
	        project.getPlugins().apply(MyBasePlugin.class);
	
	        // define conventions
	    }
	}



## 2.5 捕获用户输入信息来配置插件运行行为

大多数插件需要从构建脚本中获取一些配置，其中一种实现方式是通过`extension objects`方法实现。`Gradle Project`与`ExtensionContainer`对象相关联,该对象包含已应用于项目的插件的所有设置和属性. **可以通过此容器添加扩展对象来为插件提供配置**

- 其实就是从apply插件的 `build.gradle`文件中传递参数给插件！

### 2.5.1 示例一(通过Property类使用扩展属性)

如下插件,使用名为`binaryRepo`的扩展来获取服务器地址

	apply plugin: BinaryRepositoryVersionPlugin
	
	// 使用闭包对 binaryRepo扩展对象进行配置
	binaryRepo {
	    serverUrl = 'http://my.company.com/maven2'
	}

大多数的案例中,暴露出的扩展属性直接映射到任务属性,在执行时直接使用值 . **但是为了避免Gradle的评估顺序对一些值产生影响,建议是借助[`Property API`](https://docs.gradle.org/5.0/userguide/lazy_configuration.html#lazy_properties)来定义扩展属性**

	import org.gradle.api.Action;
	import org.gradle.api.Plugin;
	import org.gradle.api.Project;
	
	public class BinaryRepositoryVersionPlugin implements Plugin<Project> {
	    public void apply(Project project) {
			// 创建扩展
	        BinaryRepositoryExtension extension = project.getExtensions().create("binaryRepo", BinaryRepositoryExtension.class, project);
	
			// LatestArtifactVersion 是Task
			// 添加了Action
	        project.getTasks().register("latestArtifactVersion", LatestArtifactVersion.class, new Action<LatestArtifactVersion>() {
	            public void execute(LatestArtifactVersion latestArtifactVersion) {
	                latestArtifactVersion.getServerUrl().set(extension.getServerUrl());
	            }
	        });
	    }
	}

- `project.extensions.create(String name,Class<T> type,Object...constructionArguments)`: 

	将使用给予的`constructionArguments`参数 创建`type`类型的对象. 注意`type`类型的类中的构造函数需要接收参数

扩展类:


	import org.gradle.api.Project;
	import org.gradle.api.provider.Property;
	
	public class BinaryRepositoryExtension {
	    private final Property<String> serverUrl;
	
	    public BinaryRepositoryExtension(Project project) {
			// 创建泛型String的Property 对象
	        serverUrl = project.getObjects().property(String.class);
	    }
		
		// 必须提供getter方法,这样才能够使Gradle 自动生成setter方法
	    public Property<String> getServerUrl() {
	        return serverUrl;
	    }
	}

- 在该例子中,扩展属性使用了`Property<String>`而不是`String`类型,

- 该属性在构造函数中被初始化,并且通过暴露出的`getter`方法可以直接获取`serverUrl`实例

- **Gradle类加载器会替`Property`类型的字段自动插入`setter`方法**,只要其满足如下条件

	1. 该`Property`类型的字段拥有`getter`方法,并且该`getter`方法拥有返回值类型

	这将允许开发者在`Groovy DSL`中使用"obj.prop = "foo"形式替代掉`obj.prop.set "foo"`形式去设置属性值

任务类:

	import org.gradle.api.DefaultTask;
	import org.gradle.api.provider.Property;
	import org.gradle.api.tasks.Input;
	import org.gradle.api.tasks.TaskAction;
	
	public class LatestArtifactVersion extends DefaultTask {
	    private final Property<String> serverUrl;
	
	    public LatestArtifactVersion() {
	        serverUrl = getProject().getObjects().property(String.class);
	    }
	
	    @Input
	    public Property<String> getServerUrl() {
	        return serverUrl;
	    }
	
	    @TaskAction
	    public void resolveLatestVersion() {
	        // Access the raw value during the execution phase of the build lifecycle
	        System.out.println("Retrieving latest artifact version from URL " + serverUrl.get());
	
	        // do additional work
	    }
	}


### 2.5.2 示例二(直接使用扩展属性)

插件定义:
		
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


使用插件
	
	// 引用插件
	apply plugin:GreetingPlugin
	
	//方式1
	greeting.message = 'hello from gradle'
	greeting.cl = {println 'hello plugin'}

	//方式2 通过Closure 配置
	greeting{
		cl {println 'xxxxx'}
		message 'xxxxx'
	}

- 方式一,直接操作`greeting`对象.`greeting`对象 就是在插件中所创建的那个

- 方式二,就是通过闭包来对`greeting`扩展对象进行配置


### 2.5.1 将扩展属性映射到任务属性

通过扩展捕获用户来自构建脚本中的输入,并将其映射到自定义任务的输入/输出属性.用户仅与由扩展定义并暴露出的DSL进行交互,具体的逻辑隐藏在插件的实现中.

构建脚本中的扩展声明以及扩展属性和自定义任务属性之间的映射发生在构建生命周期的Gradle**配置阶段**.为了避免评估顺序问题,必须在**执行阶段**解析映射属性的实际值


Gradle API 提供了可变类型，`PropertyState`表示可以在执行时间内进行懒加载。`PropertyState.set(T)`设置值，`Provider.get()`获取值

示例(将扩展属性映射到任务属性):
 
	class GreetingPlugin implements Plugin<Project> {
		void apply(Project project) {
			// 创建扩展DSL
			def extension = project.extensions.create('greeting', GreetingPluginExtension, project)

			// 创建任务,使用扩展属性
			project.tasks.create('hello', Greeting) {
				// 直接将hello任务中的属性指向扩展
				message = extension.message
				outputFiles = extension.outputFiles
			}
		}
	}

- `project.extensions.create(String name,Class<T> type,Object...constructionArguments)`: 将使用给予的`constructionArguments`参数 创建`type`类型的对象. 注意`type`类型的类中的构造函数需要接收参数
		

	class GreetingPluginExtension {
		final PropertyState<String> message
		final ConfigurableFileCollection outputFiles
		
		// 结合 插件中的扩展创建方法使用
		GreetingPluginExtension(Project project) {
		
			// objects 就是用来创建各种模型对象
			message = project.objects.property(String)
			// 为上面创建的message属性设置值
			message.set('Hello from GreetingPlugin')
			outputFiles = project.layout.configurableFiles()
		}
		
		// 提供setter方法
		void setOutputFiles(FileCollection outputFiles) {
			this.outputFiles.setFrom(outputFiles)
		}
	}

- `project.layout`:Provides access to several important locations for a project

- `project.objects`:A factory for creating various kinds of model objects.

		
	class Greeting extends DefaultTask {
		// 创建属性
	 	final Property<String> message = project.objects.property(String)
	    final ConfigurableFileCollection outputFiles = project.layout.configurableFiles()
	
		// 提供setter方法
	    void setOutputFiles(FileCollection outputFiles) {
	        this.outputFiles.setFrom(outputFiles)
	    }
	
	    @TaskAction
	    void printMessage() {
			// 处理每一个输出文件
	        outputFiles.each {
	            logger.quiet "Writing message 'Hi from Gradle' to file"
	            it.text = message.get()
	        }
	    }
	}
	

	apply plugin: GreetingPlugin
		
	greeting {
		message = 'Hi from Gradle'
		outputFiles = files('a.txt', 'b.txt')
	}


## 2.6 嵌套的DSL元素

普通结构的DSL:

	site {
	    outputDir = file('build/mysite')
	    websiteUrl = 'https://gradle.org'
	    vcsUrl = 'https://github.com/gradle-guides/gradle-site-plugin'
	}

嵌套结构的DSL:

	apply plugin: SitePlugin
	
	site {
	    outputDir = file('build/mysite')
	
	    customData {
	        websiteUrl = 'http://gradle.org'
	        vcsUrl = 'https://github.com/gradle-guides/gradle-site-plugin'
	    }
	}

### 2.6.1 实现方式一

1. 实现这种`backing objects`十分容易,首先需要引入一个新的数据对象来管理属性`websiteUrl`,`vcsUrl`

		public class CustomData {
		    private String websiteUrl;
		    private String vcsUrl;
		
		    public void setWebsiteUrl(String websiteUrl) {
		        this.websiteUrl = websiteUrl;
		    }
		
		    public String getWebsiteUrl() {
		        return websiteUrl;
		    }
		
		    public void setVcsUrl(String vcsUrl) {
		        this.vcsUrl = vcsUrl;
		    }
		
		    public String getVcsUrl() {
		        return vcsUrl;
		    }
		}

2. 扩展类的实现

		import java.io.File;
		import org.gradle.api.Action;
		
		public class SiteExtension {
		    private File outputDir;
		    private final CustomData customData = new CustomData();
		
		    public void setOutputDir(File outputDir) {
		        this.outputDir = outputDir;
		    }
		
		    public File getOutputDir() {
		        return outputDir;
		    }
		
		    public CustomData getCustomData() {
		        return customData;
		    }
		
		    public void customData(Action<? super CustomData> action) {
		        action.execute(customData);
		    }
		}

	- 在扩展类中
		1. 需要创建`CustomData`实例

		2. **提供一个方法将捕获到的数据委托到之前创建的`CustomData`实例**

			**该方法就是用来配置基础数据对象,参数需要定义为`Action`!**


### 2.6.2 实现方式二

	class Person {
	    String name
	
	}
	
	class GreetingPluginExtension {
	    String message
	    final Person greeter
	
	    @javax.inject.Inject
	    GreetingPluginExtension(ObjectFactory objectFactory) {
	        // 通过ObjectFactory 创建对象
	        greeter = objectFactory.newInstance(Person)
	    }
	
	    void greeter(Action<? super Person> action) {
	        action.execute(greeter)
	    }
	}
	
	class GreetingPlugin implements Plugin<Project> {
	    void apply(Project project) {
	        // 创建扩展对象`greeting`,并将 ObjectFactory当做参数传入
	        def extension = project.extensions.create('greeting', GreetingPluginExtension, project.objects)

	        project.task('hello') {
	            doLast {
	                println "${extension.message} from ${extension.greeter.name}"
	            }
	        }
	    }
	}

- `project.objects: ObjectFactory`:A factory for creating various kinds of model objects.

	可以将 `ObjectFactory`对象通过构造函数直接注入,或者借助`javax.inject.Inject`注解将`ObjectFactory`对象注入到指定方法

	
	apply plugin: GreetingPlugin
	
	greeting {
	    message = 'Hi'
		// 这里实际上调用的是 扩展对象的greeter方法
	    greeter {
	        name = 'Gradle'
	    }
	}

## 2.7 声明DSL配置容器

Gradle支持用同一种类型定义多个,命名数据对象

### 2.7.1 示例

考虑如下构建块:

	apply plugin: ServerEnvironmentPlugin
	
	// 容器
	environments {
		// ServerEnvironment 对象
	    dev {
	        url = 'http://localhost:8080'
	    }
	
	    staging {
	        url = 'http://staging.enterprise.com'
	    }
	
	    production {
	        url = 'http://prod.enterprise.com'
	    }
	}

- 插件提供的`DSL`,提供了容纳一组`Book`扩展对象的容器


扩展对象:

	import org.gradle.api.provider.Property;
	import org.gradle.api.model.ObjectFactory;
	
	import javax.inject.Inject;
	
	public class ServerEnvironment {
	    private final String name;
	    private Property<String> url;
	
	    public ServerEnvironment(String name, ObjectFactory objectFactory) {
	        this.name = name;
	        this.url = objectFactory.property(String.class);
	    }
	
	    public void setUrl(String url) {
	        this.url.set(url);
	    }
	
	    public String getName() {
	        return name;
	    }
	
	    public Property<String> getUrl() {
	        return url;
	    }
	}


插件类:
	
	import org.gradle.api.*;

	public class ServerEnvironmentPlugin implements Plugin<Project> {
	    @Override
	    public void apply(final Project project) {
			// 创建容器
	        NamedDomainObjectContainer<ServerEnvironment> serverEnvironmentContainer = project.container(ServerEnvironment.class, new NamedDomainObjectFactory<ServerEnvironment>() {
	            public ServerEnvironment create(String name) {
	                return new ServerEnvironment(name, project.getObjects());
	            }
	        });

			// 注册容器
	        project.getExtensions().add("environments", serverEnvironmentContainer);
	
			// 对容器中所有的对象进行设置
	        serverEnvironmentContainer.all(new Action<ServerEnvironment>() {
	            public void execute(ServerEnvironment serverEnvironment) {
	                String env = serverEnvironment.getName();
	                String capitalizedServerEnv = env.substring(0, 1).toUpperCase() + env.substring(1);
	                String taskName = "deployTo" + capitalizedServerEnv;
	                project.getTasks().register(taskName, Deploy.class, new Action<Deploy>() {
	                    public void execute(Deploy task) {
	                        task.getUrl().set(serverEnvironment.getUrl());
	                    }
	                });
	            }
	        });
	    }
	}


- 使用`Project.container()`方法可以创建用于管理制定类型对象的容器(`NamedDomainObjectContainer`).该类型的类必须具有公共构造函数,该构造函数将名称作为String参数. 

	任何能够使用`project.container`方法创建容器的类型，都必须将名为`name`的属性公开为对象的唯一名称和常量名称。 

	`project.container（Class）`通过尝试调用具有单个字符串参数的类的构造函数来创建新的实例，这个参数就是对象的所需名称。 查看允许自定义实例化策略的project.container方法变体的上述链接。


## 2.8 自定义任务类和插件中使用文件

当定义任务类和插件时，如果用到了文件对象,最好能够比较灵活的去使用,例如,尽量迟的生成文件对象. 

- 这时,就可以使用`Project.file(java.lang.Object)`方法尽可能迟的解析文件地址

- 尽可能迟的解析文件。大概意思是指这种赖加载的方式有助于先设置属性值，等到真正执行时才去读取这个属性值。

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

- 上述例子中,声明`greetingFile`属性在指定其用在`greet`任务之后

## 2.9 与插件进行交互

在插件中可以手动应用另外一个插件,或者为插件添加一些运行时的行为,这需要使用到`Convention`

- 例如,当前项目应用了`Java`插件,那么就可以重新配置其标准源码目录

		public class InhouseConventionJavaPlugin implements Plugin<Project> {
		    public void apply(Project project) {
				// 手动应用Java插件到项目
		        project.getPlugins().apply(JavaPlugin.class);
		        JavaPluginConvention javaConvention =
		            project.getConvention().getPlugin(JavaPluginConvention.class);
		        SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		        main.getJava().setSrcDirs(Arrays.asList("src"));
		    }
		}


上述的例子中,手动应用了java插件,可以改写代码,仅针对那种项目中添加了`java`插件的情况下进行操作


	import java.util.Arrays;
	
	import org.gradle.api.Action;
	import org.gradle.api.Plugin;
	import org.gradle.api.Project;
	import org.gradle.api.plugins.JavaPlugin;
	import org.gradle.api.plugins.JavaPluginConvention;
	import org.gradle.api.tasks.SourceSet;
	
	public class InhouseConventionJavaPlugin implements Plugin<Project> {
	    public void apply(Project project) {
	        project.getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
	            public void execute(JavaPlugin javaPlugin) {
	                JavaPluginConvention javaConvention =
	                    project.getConvention().getPlugin(JavaPluginConvention.class);
	                SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
	                main.getJava().setSrcDirs(Arrays.asList("src"));
	            }
	        });
	    }
	}

同样的道理,可以针对当项目中存在指定类型任务的情况

	import org.gradle.api.Action;
	import org.gradle.api.Plugin;
	import org.gradle.api.Project;
	import org.gradle.api.tasks.bundling.War;
	
	public class InhouseConventionWarPlugin implements Plugin<Project> {
	    public void apply(Project project) {
	        project.getTasks().withType(War.class).configureEach(new Action<War>() {
	            public void execute(War war) {
	                war.setWebXml(project.file("src/someWeb.xml"));
	            }
	        });
	    }
	}

## 2.10 为插件提供默认依赖项

插件的实现有时需要使用外部依赖,Gradle允许插件直接添加依赖

	import org.gradle.api.Action;
	import org.gradle.api.Plugin;
	import org.gradle.api.Project;
	import org.gradle.api.artifacts.Configuration;
	import org.gradle.api.artifacts.DependencySet;
	
	public class DataProcessingPlugin implements Plugin<Project> {
	    public void apply(Project project) {
			
			// 在ConfigurationContainer 中添加了名为`dataFiles`的item
	        final Configuration config = project.getConfigurations().create("dataFiles")
	            .setVisible(false)
	            .setDescription("The data artifacts to be processed for this plugin.");
	
			// 给依赖对象添加 Action(将该Action保存到集合中)
			// 在构建的执行过程,会执行该 Action
	        config.defaultDependencies(new Action<DependencySet>() {
	            public void execute(DependencySet dependencies) {
	                dependencies.add(project.getDependencies().create("com.company:data:1.4.6"));
	            }
	        });
	
			// 为任务添加了属性
	        project.getTasks().withType(DataProcessing.class).configureEach(new Action<DataProcessing>() {
	            public void execute(DataProcessing dataProcessing) {
	                dataProcessing.setDataFiles(config);
	            }
	        });
	    }
	}

- Configuration 会参与依赖解析,并在执行阶段调用传入的Action

任务类:

	import org.gradle.api.DefaultTask;
	import org.gradle.api.file.ConfigurableFileCollection;
	import org.gradle.api.file.FileCollection;
	import org.gradle.api.tasks.InputFiles;
	import org.gradle.api.tasks.TaskAction;
	
	public class DataProcessing extends DefaultTask {
	    private final ConfigurableFileCollection dataFiles;
	
	    public DataProcessing() {
	        dataFiles = getProject().files();
	    }
	
	    @InputFiles
	    public FileCollection getDataFiles() {
	        return dataFiles;
	    }
	
	    public void setDataFiles(FileCollection dataFiles) {
	        this.dataFiles.setFrom(dataFiles);
	    }
	
	    @TaskAction
	    public void process() {
	        System.out.println(getDataFiles().getFiles());
	    }
	}

- **这种在插件中添加的依赖是可以被覆盖的**

使用插件:

	apply plugin: DataProcessingPlugin
	
	dependencies {
	    dataFiles 'com.company:more-data:2.6'
	}


## 2.12 Extensions vs Conventions

一些Gradle核心插件通过所谓的[`Convention`](https://docs.gradle.org/5.0/javadoc/org/gradle/api/plugins/Convention.html)的帮助下,将配置暴露出来

`Convention`是`extension`之前的概念,其提供相似功能

俩个概念的主要区别在于,`Convention`不允许定义命名空间来模拟类似DSL的APi,这使得很难与Gradle核心DSL区分开. 因此,请避免在编写新插件时使用`Convention`. 在未来,将会移除掉`Convention`这个概念

某些情况下,需要同使用`Convention`定义的Gradle核心插件进行交互. 可以通过调用`Project.getConvention()`方法来获取注册的`Convention`对象. 通过`Convention.getPlugin(Class)`可以检索已经注册的插件

- [convention-api-usage-example](https://guides.gradle.org/implementing-gradle-plugins/#convention-api-usage-example)

# 3. Gradle插件开发实例

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


- groovy目录下的路径通常为 **包名+具体groovy文件**

- `xxx.groovy`文件中编写具体插件逻辑

		package com.hypers

		import org.gradle.api.Plugin
		import org.gradle.api.Project

		class GreetingPlugin implements Plugin<Project> {

    		@Override
    		void apply(Project project) {
				project.task 'sayHello',{
					doLast{
						println 'hello groovy'
					}
				}
			}
		}

- `resources/META-INF/gradle-plugins`目录下的`xxx.properties`填写内容

		implementation-class=具体实现Plugin接口的类

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
//TODO

# 4. Plugin DSL简介

[Gradle-官方文档-Implementing a dsl](https://docs.gradle.org/current/userguide/custom_plugins.html#sec:implementing_a_dsl)

在进行Gradle配置时，很多配置是在`build.gradle`中进行设置的，插件可以在构建过程中获取这些配置。

- 构建脚本中的扩展声明以及扩展属性和自定义任务属性之间的映射发生Gradle生命周期的 配置阶段

通过查看`build.gradle`中的DSL的注释可以看到如下例子：

	android {
		....
	}

- `android`是这个DSL 创建时的名称

- **实际上`android`类型是`ExtensionContainer`，它是一个对象，意味着它也可以调用方法**

- 每个`build.gradle`会对应一个`Project`,`android`这个对象是通过`Project`对象的` ExtensionContainer getExtensions();`方法获取到的

- 上面的代码，实际上是在调用如下方法

		com.android.build.gradle.AppExtension android(Closure configuration)

## 4.2 DSL创建的原理

创建DSL 主要是调用一下方法：

	project.extensions.create('myplugin', MyExtension.class)

`create`方法有三个重载方法：

    @Incubating
    <T> T create(Class<T> var1, String var2, Class<? extends T> var3, Object... var4);

    @Incubating
    <T> T create(TypeOf<T> var1, String var2, Class<? extends T> var3, Object... var4);

    <T> T create(String name, Class<T> type, Object... constructionArguments);

创建DSL时，通常使用第三个重载方法，其参数组成如下：

1. `String name`:被创建的extension的名称，即在`build.gradle`中可以配置的代码块方法名称

2. `Class<T> type`:被创建的extension的类型，即关联的扩展实体类

3. `Object... constructionArguments`:构造extension实例时传入的参数

		//如果需要把在apply方法中的project传入
		project.extensions.create('myplugin',MyExtension.class),project
		//对应的扩展实体类需要添加一个待有Project类的构造函数
		class MyExtension {
		    ......
		    public MyExtension(Project project) {
		    }
		    ......
		}

## 4.3 Plugin中DSL的创建

通过`project.extensions` 可以为插件添加项目属性和DSL块

### 4.3.1 DSL基本实现

1. 创建关联的扩展实体类

		package com.ryan.log
		
		class Person {
		    String mName;
		}

2. 在Plugin的apply方法中创建

		class Log implements Plugin<Project> {
		
		    @Override
		    void apply(Project project) {
		        project.extensions.create("Person", Person.class)
		        project.task('printPerson').doLast {
		            println project.Person.mName
		        }
		    }
		}

3. 集成插件之后使用

		apply plugin:'com.ryan.log'
		
		Person{
		    mName 'Ryan'
		}

### 4.3.2 DSL中的成员变量为扩展实体类时

1. 创建关联的扩展实体类，成员变量也是扩展实体类

		package com.ryan.log
		
		class Company {
		    Person mPerson;
		}

2. 在Plugin的apply方法中创建

        project.extensions.create("Company", Company.class)
        project.task('printCompany').doLast {
            println project.Company.mPerson.mName
        }

3. 集成插件后使用

		Company{
		    mPerson Person{
		        mName 'Jack'
		    }
		}

### 4.3.3 通过增强型任务实现DSL


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
	    final Property<String> message
	    final ConfigurableFileCollection outputFiles
	
	    GreetingPluginExtension(Project project) {
	        message = project.objects.property(String)
	        message.set('Hello from GreetingPlugin')
	        outputFiles = project.files()
	    }
	
	    void setOutputFiles(FileCollection outputFiles) {
	        this.outputFiles.setFrom(outputFiles)
	    }
	}
	
	class Greeting extends DefaultTask {
	    final Property<String> message = project.objects.property(String)
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
	
	apply plugin: GreetingPlugin
	
	greeting {
	    message = 'Hi from Gradle'
	    outputFiles = files('a.txt', 'b.txt')
	}



## 4.4 嵌套DSL

在Gradle4.2之前，官方文档未明确指出嵌套DSL的创建方法

### 4.4.1 Gradle>=4.2
嵌套DSL类型如下：

	android {
	    compileSdkVersion 23
	    buildToolsVersion "23.0.1"
	    defaultConfig {
	        applicationId "com.example.heqiang.testsomething"
	        minSdkVersion 23
	        targetSdkVersion 23
	    }
	}


同时通过`@javax.inject.Inject`声明一个带有`org.gradle.api.model.ObjectFactory`参数的构造函数，并在这个构造函数中通过`ObjectFactory`创建嵌套DSL实例

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

### 4.4.2 Gradle < 4.2


1. 创建关联的扩展实体类，需要在构造函数创建被嵌套的DSL扩展实体类

		package com.ryan.log
		
		class Company {
		    String mCompany
		
		    public Company() {
		        this.extensions.create("Person", Person.class)
		    }
		}

2. 集成插件后使用

		Company{
		
		    mCompany 'Hypers'
		
		    Person{
		        mName  'Jack'
		    }
		}




## 4.5 DSL-对象集合

通过`Project.container(Java.lang.Class)`方法创建`NamedDomainObjectContainer`实例，这个类NDOC提供了很多管理和配置对象的方法。

为了能够使用`project.container（Class）`方法，**传入的实例必须暴露一个唯一的常量，名称为'name'。**

1. 创建class，必须包含`name`字段，且这个字段必须从构造函数中传入

		package com.ryan.log
		
		class Person {
		    String name
		    String lastName
		    Integer age
		
		    Person(String name) {
		        this.name = name
		    }
		}

2. 在Plugin中进行声明

		class Log implements Plugin<Project> {
		
		    @Override
		    void apply(Project project) {
				//创建一个Person实例的container
		        def persons = project.container(Person.class)
				//将container 对应成 扩展对象
		        project.extensions.persons = persons
		
		        project.task("printPersons") << {
		            persons.all {
		                println "person name = ${it.lastName}"
		                println "person age = ${it.age}"
		            }
		        }
		    }
		}

3. 集成插件后使用

		persons{
		    ryan{
		        lastName = 'ma'
		        age = 18
		    }
		
		    jack{
		        lastName = 'duan'
		        age = 17
		    }
		
		}


赋值必须使用`=`等号。。。。