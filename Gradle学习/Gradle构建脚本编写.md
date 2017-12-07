#1 构建脚本的细节内容
[Writing Build Script](https://docs.gradle.org/current/userguide/writing_build_scripts.html)

## 1.1 Gradle构建语言
Gradle提供了用于描述构建的DSL(domain specific language).这种构建语言是基于Groovy的，并且增加了一些内容以便于描述构建

一个构建脚本可以添加任何Groovy的元素！Gradle认为每个构建脚本都是使用UTF-8编码。

构建脚本中的Groovy代码只是去使用Gradle APi，Project 接口是Gradle Api中访问一切的开头，因此如果想知道构建脚本中有哪些`标签`可以用，可以从Project接口文档查看

## 1.2 The Project API
在Gradle中，添加插件使用的是`apply()`方法，那么这个方法属于哪个对象？(属于构建脚本对应的Project对象)

- 一个构建脚本在Gradle中定义一个项目。对于构建中的每一个项目，Gradle都会创建一个类型为**`Project`**的对象，并将这个**`Project`**对象和构建脚本相关联

- **当构建脚本执行时，其对应的**`Project`**类型的对象：**

	- 在构建脚本中调用任何未定义的方法会被委托给构建脚本对应的**`Project`**对象
	- 在构建脚本中访问任何未定义的属性会被委托给构建脚本对应的**`Project`**对象

- 举个栗子，访问**`Project`**类型的对象的name属性

		println name
		println project.name
		
		> gradle -q check
		projectApi
		projectApi

	- 俩个println 语句输出相同的内容，第一个将未定义的属性自动委托给构建脚本的Project类型对象。第二个使用了在任何构建脚本中都能使用的属性`project`(注意是小写的，大写是的类型),这个属性会返回一个与构建脚本相关联的**`Project`**类型的对象。**只有当你定义了一个方法或属性与**`Project`**类型的对象中的成员同名时，才需要明确的使用`project`属性去访问**`Project`**类型的对象。**

### 1.2.1 项目标准属性
**`Project`**类型的对象提供了一些标准的属性，这些属性可以在构建脚本中使用

- **Name**:project **Type**:Project **Default Value**:Project实例

- **Name**:name **Type**:String **Default Value**:项目目录的名称

- **Name**:path **Type**:String **Default Value**:项目的绝对路径

- **Name**:description **Type**:String **Default Value**:项目描述信息

- **Name**:projectDir **Type**:String **Default Value**:包含构建脚本的目录，即构建脚本所在目录

- **Name**:buildDir **Type**:String **Default Value**:projectDir/build

- **Name**:group **Type**:Object **Default Value**:

- **Name**:version **Type**: Object**Default Value**:

- **Name**:ant **Type**:AntBuilder **Default Value**:AntBuilder实例


## 1.3 The Script Api

当Gradle执行脚本时，会将脚本编译成一个实现了`Script`接口的类，这意味着`Script`接口中的方法和属性都可以在脚本中使用

## 1.4 声明变量
有俩种可以在构建脚本中声明的变量：局部变量和额外属性

### 1.4.1 局部变量
局部变量用`def` 关键词声明，只能在声明的范围内可见。局部变量是Groovy语法的一个特性（具体的去查看Groovy学习.md中的4.2章节）

	def dest = "dest"
	
	task copy(type: Copy) {
	    from "source"
	    into dest
	}

### 1.4.2 额外的属性

`Gradle's domain model `中的所有增强对象都可以包含额外的用户定义的属性。这个特性包括且不限于`project,tasks,and source sets`.

额外的属性可以通过持有的对象的`ext`属性进行添加，读取和设置，或者可以通过`ext block`一次性添加多个额外属性

**举个栗子：**

	apply plugin: "java"
	
	ext {
	    springVersion = "3.1.0.RELEASE"
	    emailNotification = "build@master.org"
	}
	
	sourceSets.all { ext.purpose = null }
	
	sourceSets {
	    main {
	        purpose = "production"
	    }
	    test {
	        purpose = "test"
	    }
	    plugin {
	        purpose = "production"
	    }
	}
	
	task printProperties {
	    doLast {
	        println springVersion
	        println emailNotification
	        sourceSets.matching { it.purpose == "production" }.each { println it.name }
	    }
	}

	> gradle -q printProperties
	3.1.0.RELEASE
	build@master.org
	main
	plugin

- 在这个例子中，一个`ext block`为project对象添加了俩个额外的属性。此外，名称为`purpose`的属性通过设置`ext.purpose = null`(null是一个允许的值)被添加到每个 source set中。一旦添加了属性，就可以像预定义的属性那样去读取和设置。

通过特殊的语法添加一个属性，当Gradle试图对一个 拼写错误或不存在的 属性进行设置时 会立刻失败

持有额外属性的对象能被访问的地方，其额外属性也能被访问，额外属性拥有比局部变量更大的访问范围。

一个项目的额外属性 对其子项目也是可见的

## 1.5 配置任意对象

可以使用以下更可读的形式配置对象：

	task configure {
	    doLast {
	        def pos = configure(new java.text.FieldPosition(10)) {
	            beginIndex = 1
	            endIndex = 5
	        }
	        println pos.beginIndex
	        println pos.endIndex
	    }
	}
	
	> gradle -q configure
	1
	5

## 1.6 使用外部脚本配置任意对象

**使用外部脚本配置任意对象示例：**

	task configure {
	    doLast {
	        def pos = new java.text.FieldPosition(10)
	        // Apply the script
	        apply from: 'other.gradle', to: pos
	        println pos.beginIndex
	        println pos.endIndex
	    }
	}
	
	// Set properties.
	beginIndex = 1
	endIndex = 5

## 1.7 一些Groovy基础知识
Groovy语言为创建DSL提供了大量特性，了解Groovy语言的工作原理对编写构建脚本有非常大的帮助，特别是在开始编写自定义插件和任务时。

### 1.7.1 Groovy JDK
Groovy为标准的Java 类增加了许多有用的方法。[查看更多细节 》》Groovy GDK Doc](http://groovy-lang.org/gdk.html)

例如，为`Iterable`提供了一个`each`方法
	
	// Iterable gets an each() method
	configurations.runtime.each { File f -> println f }

### 1.7.2 Property accessors
Groovy 自动将对属性的引用转为适当的 `getter/setter`方法


	// Using a getter method
	println project.buildDir
	println getProject().getBuildDir()
	
	// Using a setter method
	project.buildDir = 'target'
	getProject().setBuildDir('target')

### 1.7.3 Optional parentheses on method calls
方法调用时的圆括号是可选的

	test.systemProperty 'some.prop', 'value'
	test.systemProperty('some.prop', 'value')

### 1.7.4 List and map literals
Groovy为定义List和Map实例提供了一些快捷方式。这俩种字面量是直接的，但是map的字面量有一些不同。

例如，方法`apply`(用来添加插件的方法)实际上需要一个map参数。但是在实际的代码中 `apply plugin:'java'`,并没有真正的去使用map字面量，而是使用的`named parameters 即命名参数`(其具有和map字面量几乎完全相同的语法，只是没有wrapping brackets)。**该命名参数列表不以map的形式表示，但是会在方法被调用时转换成map**
	
	// List literal
	test.includes = ['org/gradle/api/**', 'org/gradle/internal/**']
	
	List<String> list = new ArrayList<String>()
	list.add('org/gradle/api/**')
	list.add('org/gradle/internal/**')
	test.includes = list
	
	// Map literal.
	Map<String, String> map = [key1:'value1', key2: 'value2']
	
	// Groovy will coerce named arguments
	// into a single map argument
	apply plugin: 'java'

### 1.7.5 Closures as the last parameter in a method
Gradle DSL 在很多地方使用closure.**当方法的最后一个参数是closure时，可以在方法调用后放置closure**

	repositories {
	    println "in a closure"
	}
	repositories() { println "in a closure" }
	repositories({ println "in a closure" })

### 1.7.6 Closure delegate
每个closure都有一个delegate对象，Groovy使用该对象去查找变量和方法引用(这俩个不是closure的局部变量或参数)。 Gradle将此特性用在`configuration closure`(配置闭包),其中delegate被设置为待配置的对象

	dependencies {
	    assert delegate == project.dependencies
	    testCompile('junit:junit:4.12')
	    delegate.testCompile('junit:junit:4.12')
	}

### 1.7.7 default import
为了使得构建脚本更加简洁，Gradle自动添加了一组导入语句在Gradle脚本中。这意味着 可以不用使用完整包名去引用类，例如`throw new org.gradle.api.tasks.StopExecutionException() `可以用`throw new StopExecutionException()`来替换。

	Unresolved directive in <stdin> - include::../../../build/generated-resources/main/default-imports.txt[]
