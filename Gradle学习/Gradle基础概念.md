# 引用

[Understanding Gradle configuration](http://makble.com/understanding-gradle-configuration)

[Gradle中文版](https://legacy.gitbook.com/book/dongchuan/gradle-user-guide-/details)

[**依赖管理的术语介绍-官方文档**](https://docs.gradle.org/current/userguide/dependency_management_terminology.html#sub:terminology_configuration)

[Gradle学习系列之七——依赖管理](https://www.cnblogs.com/davenkin/p/gradle-learning-7.html)

# 1. Configuration 

Gradle将对依赖进行分组，比如编译Java时使用的是这组依赖，运行Java时又可以使用另一组依赖。每一组依赖称为一个Configuration，在声明依赖时，我们实际上是在设置不同的Configuration。值得一提的是，将依赖称为Configuration并不是一个好的名字，更好的应该叫作诸如“DependencyGroup”之类的。但是，习惯了就好的。

要定义一个Configuration，我们可以通过以下方式完成：

	configurations {
	   myDependency
	}

- 为了验证`myDependency`就是一个`Configuration`,可以打印出其类型

		println configurations.myDependency.class

		class org.gradle.api.internal.artifacts.configurations.DefaultConfiguration_Decorated
 

以上只是定义了一个名为`myDependency`的`Configuration`，我们并未向其中加入依赖。我们可以通过`dependencies()`方法向myDependency中加入实际的依赖项：

	dependencies {
	   myDependency 'org.apache.commons:commons-lang3:3.0'
	}
 

以上，我们将Apache的commons加入了myDependency中。之后，如果有Task需要将Apache commons加入到classpath中，我们可以通过以下方式进行获取：

	task showMyDependency << {
	   println configurations.myDependency.asPath
	}

## 1.1 示例

下面，我们来看一个Java项目，该项目依赖于SLF4J，而在测试时依赖于Junit。在声明依赖时，我们可以通过以下方式进行设置：

	dependencies {
	   compile 'org.slf4j:slf4j-log4j12:1.7.2'
	   testCompile 'junit:junit:4.8.2'
	}

我们并没有定义名为compile和testCompile的Configuration，这是这么回事呢？

- 原因在于，`java Plugin`会自动定义compile和testCompile，分别用于编译Java源文件和编译Java测试源文件。　　

	另外，`java Plugin`还定义了runtime和testRuntime这两个Configuration，分别用于在程序运行和测试运行时加入所配置的依赖。


再举个来自Gradle官网的例子：在Gradle中调用Ant，首先我们通过Configuration声明一组依赖，然后在Ant定义中将该Configuration所表示的classpath传给Ant：

	configurations {
	   pmd
	}

	dependencies {
	   pmd group: 'pmd', name: 'pmd', version: '4.2.5'
	}

	task check {
		doLast{
			ant.taskdef(name: 'pmd', classname: 'net.sourceforge.pmd.ant.PMDTask', classpath: configurations.pmd.asPath)

			ant.pmd(shortFilenames: 'true', failonruleviolation: 'true', rulesetfiles: file('pmd-rules.xml').toURI().toString()) {
				formatter(type: 'text', toConsole: 'true')
				fileset(dir: 'src')
	   		}
		}
	}


如果存在依赖冲突，在默认情况下，Gradle会选择最新版本，这和Maven是不同的，Maven会选择离依赖树最近的版本。当然，我们可以通过设置Configuration的resolutionStrategy来重新设置依赖冲突的处理规则，对此本文将不予讲解。


除了可以加入Maven和Ivy的Repository中的依赖之外，Gradle还允许我们声明对其他Project或者文件系统的依赖。比如，如果ProjectA的compileJava依赖于ProjectB，那么可以在ProjectA中声明如下：

	dependencies {
	   compile project(':ProjectB')
	}


## 1.2 获取Configuration

### 1.2.1 在项目中访问Configuration

项目的`Configuration`被包装在`ConfigurationContainer`中,可以通过`project.configurations`获取

	println project.configurations.getClass();
	 
	/*-----------------------
	class org.gradle.api.internal.artifacts.configurations.DefaultConfigurationContainer_Decorated
	*/

### 1.2.2 获取所有的configuration
	
	allConfigurations = project.configurations.getAll()

	println 'allConfigurations size:' + allConfigurations.size()

	configurations.all.each{
    	println "it  = $it"
	}

### 1.2.3 通过名称获取configuration

	println project.configurations.compile.getClass()
	/*------------------------
	class org.gradle.api.internal.artifacts.configurations.DefaultConfiguration_Decorated
	*/

- 相当于使用方法`getByName(String)`获取



# 3. publication and dependencies

粗略的讲, 依赖管理由两部分组成. 首先, Gradle 需要了解你的项目需要构建或运行的东西, 以便找到它们. 我们称这些传入的文件为项目的 `dependencies`(依赖项). 其次, Gradle 需要构建并上传你的项目产生的东西. 我们称这些传出的项目文件为 `publications`(发布项).

## 3.1 dependencies

大多数项目都不是完全独立的. 它们需要其它项目进行编译或测试等等. 举个例子, 为了在项目中使用 Hibernate, 在编译的时候需要在 classpath 中添加一些 Hibernate 的 jar 路径. 要运行测试的时候, 需要在 test classpath 中包含一些额外的 jar, 比如特定的 JDBC 驱动或者 Ehcache jars.


这些传入的文件构成上述项目的依赖. Gradle 允许你告诉它项目的依赖关系, 以便找到这些依赖关系, 并在你的构建中维护它们. 依赖关系可能需要从远程的 Maven 或者 Ivy 仓库中下载, 也可能是在本地文件系统中, 或者是通过多项目构建另一个构建. 我们称这个过程为 `dependency resolution`(依赖解析).

通常, 一个项目本身会具有依赖性. 举个例子, 运行 Hibernate 的核心需要其他几个类库在 classpath 中. 因此, Gradle 在为你的项目运行测试的时候, 它会找到这些依赖关系, 并使其可用. 我们称之为`transitive dependencies`(依赖传递).

## 3.2 publication

大部分项目的主要目的是要建立一些文件, 在项目之外使用. 比如, 你的项目产生一个 Java 库,你需要构建一个jar, 可能是一个 jar 和一些文档, 并将它们发布在某处.

这些传出的文件构成了项目的发布. Gradle 当然会为你负责这个重要的工作. 你声明项目的发布, Gradle 会构建并发布在某处. 究竟什么是"发布"取决于你想做什么. 可能你希望将文件复制到本地目录, 或者将它们上传到一个远程 Maven 或者 Ivy 库.或者你可以使用这些文件在多项目构建中应用在其它的项目中. 我们称这个过程为 publication(发布)