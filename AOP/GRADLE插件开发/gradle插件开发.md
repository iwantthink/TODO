# 1.插件类型

- Build Script :直接在构建脚本文件（build.gradle）中编写，缺点是无法复用插件代码(在其他项目中使用的话 需要复制gradle文件)

- buildSrc Project:将插件源码放到 `rootProjectDir/buildSrc/src/main/groovy `目录下。Gradle会编译和测试插件，并使其在构建脚本的类路径上可用。另外插件对构建使用的每个构建脚本都可见。但是其他项目没有定义的项目里 依旧无法使用。

- Standalone project:在独立的项目里编写插件,打成Jar包使用 或发布到仓库，之后可以直接引用。

# 2.插件编写
## 2.1 编写简单的插件
- 给出的例子中设置的插件类型是Project类型的，可以在Plugin<>设置更多的类型参数（目前不太可能）

- 以下的例子都是在build.gradle中编写的


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

## 2.2 从构建从获取输入
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
			
			greeting.message = 'hello from gradle'
			greeting.cl = {println 'hello plugin'}
	
			greeting{
				cl {println 'xxxxx'}
				message 'xxxxx'
			}


# 3.Gradle插件开发
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


