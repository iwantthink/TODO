# 使用Gradle插件

[Using Gradle Plugins](https://docs.gradle.org/current/userguide/plugins.html)



# 1. 简介

Gradle 的核心仅提供了少量的操作逻辑. 所有的实用特性,类似编译java源码的能力, 是由插件提供的. 

- 插件添加了新的任务(如:JavaCompile),域对象(如:SourceSet),约定(如:Java资源位置是`src/main/java`)以及扩展自核心的对象 和 插件提供的对象

接下来将介绍如何使用插件，以及插件的术语和概念


# 2. 插件的作用

**应用插件到项目允许插件来扩展项目的能力**

## 2.1 插件能做如下的事情

- 扩展Gradle模型（如:添加可配置的DSL元素）

- 按照约定配置项目(如:添加新的任务或配置合理的默认值)

- 应用特定的配置（如:增加组织库或执行标准）

## 2.2 应用插件的好处

通过应用插件,而不是向项目构建脚本添加逻辑可以收获很多好处:

- 促进重用,并减少维护多项目类似逻辑的开销

- 允许更高程度的模块化，增强可理解性和组织性

- 封装必要的逻辑，并允许构建脚本尽可能是声明性地

# 3. 插件的类型

**在Gradle中一般有两种类型的插件,脚本插件(`script plugin`)和二进制插件(`binary plugin`)**

- 脚本插件是额外的构建脚本,它会进一步配置构建,通常实现操作构建的生命性方法.尽管可以外部化并且从远程位置访问,它们通常还是会在构建内部中使用

- 二进制插件是实现了`Plugin`接口的类,并且采用编程的方式来操纵构建.二进制插件可以驻留在构建脚本,项目层级内或外部的插件jar


**插件一开始通常是脚本插件(因为它们易于编写)，然后随着代码变得更有价值，它被迁移到一个二进制插件，使得很容易地在多个项目或组织之间进行测试和共享**



# 4. 使用插件

为了使用封装在插件中的构建逻辑，Gradle需要执行两个步骤。首先，它需要解析(`resolve`)插件，然后需要将插件应用(`apply`)于目标，这个目标通常是一个项目(`Project`)

- **解析插件意味着找到包含给定插件的jar的正确版本，并将其添加到脚本的classpath中**

	**一旦插件被解析，它的API就可以在构建脚本中使用**。脚本插件是自解析的，因为脚本插件是根据应用脚本插件时提供的特定文件路径或URL解析的。此外，作为Gradle发行版一部分的核心二进制插件会被自动解析

- 应用(`apply`)插件意味着 在需要使用插件进行增强的项目上执行插件的`Plugin.apply（T）`方法。 

	应用插件是幂等的(`idempotent`),也就是说，可以多次地应用任何插件而不会产生副作用

	英文直接翻译的意思是幂等(denoting an element of a set that is unchanged in value when multiplied or otherwise operated on by itself.),上下中的大意应该是不会受其他因素的影响.


使用插件最常见的用例是解析插件并将其应用于当前项目。由于这是一个非常常见的用例，所以建议构建使用`plugins{}`DSL一次性解析和应用插件


# 5. 脚本插件

## 5.1 应用一个脚本插件

	// build.gradle
	apply from: 'other.gradle'

- 脚本插件是自动解析的，并且可以应用来自 本地文件系统或远程位置的脚本

	文件系统的位置相对于项目目录，而远程脚本位置使用HTTP URL指定。可以将多个脚本插件(任意一种形式)应用于给定的目标



# 6. 二进制插件


可以使用插件ID应用插件，插件的id是一个全局唯一的标识符 或者说名称

核心Gradle插件的特殊之处在于，它们提供了缩写，比如核心`JavaPlugin`对应`"java”`。除核心插件外的所有其他二进制插件都必须使用插件id的完全限定形式(例如`com.github.foo.bar`)，但是由于一些原因，某些遗留的插件仍然可能使用一个简短的非限定形式。


 放置插件ID的位置取决于您使用的是插件DSL(`plugins{}`)还是使用了构建脚本块(`buildscript{}`)


## 6.1 二进制插件的位置

**一个插件就是一个实现了`Plugin`接口的类.Gradle提供的核心插件作为其发行版的一部分，这意味着它们将被自动解析,你需要做的仅仅是应用核心插件.然而,非核心的二进制插件需要在应用之前先解析**

这可以通过以下几种方式实现:

1. 使用`plugins{}`包含来自插件门户的插件 和 自定义存储库中的插件(参见使用插件DSL应用插件)。

2. 将来自外部的Jar定义为构建脚本依赖(参见使用构建脚本块应用插件)

3. 将插件定义为项目中buildSrc目录下的源文件(参见使用buildSrc提取函数逻辑)。

4. 将插件定义为构建脚本中的内联类声明。

自定义插件可以参考[Custom Plugin](https://docs.gradle.org/current/userguide/custom_plugins.html#custom_plugins)


# 7. 使用插件DSL应用插件

`plugins{}`DSL提供了一种简洁方便的方式来声明插件依赖,它需要与Gradle插件门户一起使用，可以轻松访问核心插件和社区插件。 `plugins{}`DSl会配置一个`PluginDependenciesSpec`实例


-  在构建脚本中，`plugins{}`脚本块API属于`PluginDependenciesSpec`。 也就是说，可以在`plugins{}`脚本块的主体中​​使用此API来声明用于脚本的插件

	[PluginDependenciesSpec](https://docs.gradle.org/current/javadoc/org/gradle/plugin/use/PluginDependenciesSpec.html)


## 7.1 应用一个核心插件(使用缩写)

应用一个核心插件时，插件id可以使用其缩写：

	plugins {
	    id 'java'
	}

## 7.2 应用一个来自社区的插件

要从门户网站应用社区插件，必须使用完全限定的插件id

	plugins {
	    id 'com.jfrog.bintray' version '0.4.1'
	}


## 7.3 插件DSL的限制

使用插件DSL向项目添加插件的方式不仅仅是一种更方便的语法。插件DSL的处理方式允许Gradle非常早、非常快地确定正在使用的插件。这使得Gradle可以做一些聪明的事情，比如：

- 优化插件类的加载和重用

- 允许不同的插件使用不同版本的依赖项

- 为编辑提供有关编译帮助中的潜在属性和值的详细信息。

这要求在执行构建脚本的其余部分之前，Gradle可以以一种轻松快速的方式提取指定插件。它还要求插件的定义是静态的


## 7.4 插件DSL与传统apply机制的区别

`plugins{}`块机制和传统的`apply()`方法机制之间有一些关键的区别。还存在一些限制，其中一些是机制尚在发展时的暂时限制，另一些是新方法固有的限制


### 7.4.1 语法限制

**`plugins{}`块不允许支持任意的代码,它是受限制的，为了幂等性(每次执行都产生相同的结果)和无副作用(在任何时间都能安全执行)**

`plugins{}`的形式有俩种：

	plugins {
	    id «plugin id»                                            // (1)
	    id «plugin id» version «plugin version» [apply «false»]   // (2)
	}

- 俩种形式的含义分别是：

	1. **第一种形式用于构建脚本中早已可用的插件或Gradle核心插件**

			// 示例
			plugins{
				id 'java'
			}

	2. **第二种形式用于待解析的二进制插件**

			// 示例
			plugins {
				id 'com.jfrog.bintray' version '0.4.1'
			}

- **还有一些额外的限制**：

	1. 其中`«plugin id»`和`«plugin version»`必须是常量，字面量,字符串之一 

	2. 带布尔值的apply语句 `[apply «false»]`可用于禁用立即解析并应用插件（例如只想在子项目中应用插件时）
	
	3. 不允许其他语句的存在,如果存在则会导致编译错误

	4. `plugins {}`块必须是构建脚本中的顶级语句,它不能嵌套在另一个结构中（例如if语句或for循环）


### 7.4.2 只能在构建脚本中使用插件DSL

`plugins{}`块目前只能在项目的构建脚本中使用。它不能用于脚本插件,`settings.gradle`文件或`init`脚本

- Gradle的未来版本将取消这个限制(文章使用Gradle 5.4)

**如果`plugins {}`块的限制让你无法接受，Gradle建议的方法是使用`buildscript{}`块应用插件**



## 7.5 将插件应用到子项目

如果您有多项目构建，则可能希望将插件应用于构建中的部分或全部子项目，而不是应用于根项目或主项目。

- `plugins{}`块的默认行为是立即解析并应用插件。 但是，可以使用`apply false`语法告诉Gradle不要将插件应用于当前项目，然后在子项目块中使用`apply plugin：«plugin id»`或在子项目的构建脚本中使用`plugins {}`块：

### 7.5.1 在指定项目中应用插件

	//settings.gradle
	include 'helloA'
	include 'helloB'
	include 'goodbyeC'

	//build.gradle
	plugins {
	    id 'org.gradle.sample.hello' version '1.0.0' apply false
	    id 'org.gradle.sample.goodbye' version '1.0.0' apply false
	}
	
	subprojects {
	    if (name.startsWith('hello')) {
	        apply plugin: 'org.gradle.sample.hello'
	    }
	}
	
	goodbyeC/build.gradle
	plugins {
	    id 'org.gradle.sample.goodbye'
	}

	> gradle hello
	:helloA:hello
	:helloB:hello
	Hello!
	Hello!
	
- 这时运行hello任务，可以看到只有`helloA`和`helloB`俩个项目应用了插件hello


# 8. 应用buildSrc目录中的插件

Gradle支持应用保存在在项目`buildSrc `目录中的插件，只要它们有一个已定义的id

## 8.1 定义buildSrc中插件的Id

下面的例子展示了如何绑定一个定在`buildSrc`目录下的插件实现类(`my.MyPlugin`),指定其ID为`my-plugin`

	plugins {
	    id 'java'
	    id 'java-gradle-plugin'
	}
	
	gradlePlugin {
	    plugins {
	        myPlugins {
	            id = 'my-plugin'
	            implementationClass = 'my.MyPlugin'
	        }
	    }
	}
	
	dependencies {
	    compileOnly gradleApi()
	}
	
定义完之后，就可以像普通的插件id一样被用于`plugins{}`

	plugins {
	    id 'my-plugin'
	}

# 9. 插件管理

`pluginManagement {}`块可能只出现在俩个地方

1. 在`settings.gradle`文件中，并且它必须是文件中的第一个块

2. 出现在[初始化脚本](https://docs.gradle.org/current/userguide/init_scripts.html#init_scripts)



## 9.1 为每个项目和全局配置pluginManagement
	
	//settings.gradle
	pluginManagement {
	    resolutionStrategy {
	    }
	    repositories {
	    }
	}

	//init.gradle
	settingsEvaluated { settings ->
	    settings.pluginManagement {
	        resolutionStrategy {
	        }
	        repositories {
	        }
	    }
	}


# 10. 自定义插件库

默认情况下，`plugins{}` DSL解析来自公共Gradle插件门户的插件。 许多构建作者还希望解决来自私有Maven或Ivy存储库的插件，因为插件包含专有的实现细节，或者只是为了更多地控制其构建可用的插件


要指定自定义插件存储库，请使用`pluginManagement{}`块中的`repositories{}`块：

## 10.1 使用自定义插件库

	//settings.gradle
	pluginManagement {
	    repositories {
	        maven {
	            url '../maven-repo'
	        }
	        gradlePluginPortal()
	        ivy {
	            url '../ivy-repo'
	        }
	    }
	}

- 这告诉Gradle在解析插件时首先在`../maven-repo`查看Maven存储库 . 如果在Maven存储库中找不到插件，那么检查Gradle插件门户是否能够找到. 最后，将检查`../ivy-repo`上的Ivy存储库

	如果不想在`Gradle Plugin Portal`中搜索，请省略`gradlePluginPortal（）`行


# 11. 插件解析规则

插件解析规则允许您修改`plugins{}`块中的插件请求，例如 更改请求的版本或显式指定具体实现的构件的坐标

## 11.1 设置插件解析规则

**要添加解析规则，请使用`pluginManagement {}`块内的`resolutionStrategy {}`块**

	//settings.gradle
	pluginManagement {
	    resolutionStrategy {
	        eachPlugin {
	            if (requested.id.namespace == 'org.gradle.sample') {
	                useModule('org.gradle.sample:sample-plugins:1.0.0')
	            }
	        }
	    }
	    repositories {
	        maven {
	            url '../maven-repo'
	        }
	        gradlePluginPortal()
	        ivy {
	            url '../ivy-repo'
	        }
	    }
	}

- 这告诉Gradle使用指定的插件实现构件，而不是使用内置默认的 从插件ID到Maven / Ivy仓库的映射

- 除了实际实现插件的构件之外，自定义Maven和Ivy插件存储库必须包含插件标记构件。 有关将插件发布到自定义存储库的更多信息，请阅读[Gradle Plugin Development Plugin](https://docs.gradle.org/current/userguide/java_gradle_plugin.html#java_gradle_plugin)

- 通过[PluginManagementSpec](https://docs.gradle.org/current/javadoc/org/gradle/plugin/management/PluginManagementSpec.html)可以查看更多的使用方法

# 12. Plugin Marker Artifacts

因为`plugins {}`块仅允许通过全局唯一的插件id和版本属性来声明插件， Gradle需要一种方法来查找插件实现构件(`plugin implementation artifact`)的坐标

- 为此，Gradle将通过使用坐标`plugin.id:plugin.id.gradle.plugin:plugin.version`,这个标记需要依赖于实际的插件实现。发布这些标记是由[java-grade-plugin](https://docs.gradle.org/current/userguide/java_gradle_plugin.html#java_gradle_plugin)自动完成的


## 12.1 完整的插件发布示例

下面是一个示例，存在于`sample-plugins`项目中，显示了如何使用`java-gradle-plugin`,`maven-publish-plugin`和`ivy-publish-plugin`将`org.gradle.sample.hello`插件和`org.gradle.sample.goodbye`插件发布到Ivy和Maven存储库

	//build.gradle
	plugins {
	    id 'java-gradle-plugin'
	    id 'maven-publish'
	    id 'ivy-publish'
	}
	
	group 'org.gradle.sample'
	version '1.0.0'
	
	gradlePlugin {
	    plugins {
	        hello {
	            id = 'org.gradle.sample.hello'
	            implementationClass = 'org.gradle.sample.hello.HelloPlugin'
	        }
	        goodbye {
	            id = 'org.gradle.sample.goodbye'
	            implementationClass = 'org.gradle.sample.goodbye.GoodbyePlugin'
	        }
	    }
	}
	
	publishing {
	    repositories {
	        maven {
	            url '../../consuming/maven-repo'
	        }
	        ivy {
	            url '../../consuming/ivy-repo'
	        }
	    }
	}
	
- [Maven Publish Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven)	
	
- [Ivy Publish Plugin](https://docs.gradle.org/current/userguide/publishing_ivy.html#publishing_ivy)	
	
在项目中执行`gradle publish`命名后，就能得到以下的仓库布局：

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g2i57c9f72j20oc0bhaay.jpg)



# 13. 旧版的插件应用

随着`plugins{}`DSL的引入，用户应该没有理由使用传统的应用插件的方法。如果构建作者由于当前工作方式的限制而不能使用插件DSL，这里将对其进行文档说明


## 13.1 应用二进制插件(使用插件id)

	//build.gradle
	apply plugin: 'java'

- **插件可以通过插件id被应用**，上面的例子中，使用了缩写`java`去应用了[`JavaPlugin`](https://docs.gradle.org/current/javadoc/org/gradle/api/plugins/JavaPlugin.html)


## 13.2 按类型应用二进制插件

**除了使用插件id，还可以通过指定插件的类来应用插件**

	//build.gradle
	apply plugin: JavaPlugin

- 上面的`JavaPlugin`符号指向了`JavaPlugin`类

	由于`org.gradle.api.plugins`包在所有构建脚本中被自动导入，因此不需要严格导入`JavaPlugin`（[请参阅默认导入](https://docs.gradle.org/current/userguide/writing_build_scripts.html#script-default-imports)）


此外，没有必要附加`.class`来标识Groovy中的类，就像在Java中一样。


## 13.3 使用构建脚本块来应用插件

通过将插件添加到构建脚本`classpath`然后应用插件，可以将已发布为外部jar文件的二进制插件添加到项目中

- Gradle提供了`buildscript {}`块用来实现 将外部jar添加到构建脚本`classpath`中，如构建脚本的外部依赖项中所述

### 13.3.1 使用buildscript块应用插件

	buildscript {
	    repositories {
	        jcenter()
	    }
	    dependencies {
	        classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:0.4.1'
	    }
	}
	
	apply plugin: 'com.jfrog.bintray'


# 14. 寻找社区插件

Gradle提供了[plugin portal](https://plugins.gradle.org/)，开发者可能在这上面寻找插件


# 15. 更多关于插件的信息

本章旨在介绍插件和Gradle及其扮演的角色。 有关插件内部工作方式的更多信息，请参阅[自定义插件](https://docs.gradle.org/current/userguide/custom_plugins.html#custom_plugins)