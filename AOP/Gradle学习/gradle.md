# Gradle学习
- 构建，可以叫做build或make. 就是根据输入信息然后干一堆事情，然后得出几个产出物。

- Gradle 就是一个构建工具，ANT->MAVEN->GRADLE

- Gradle 因为采用的Groovy语言，所以具备Groovy的特点DSL(Domain Specific Language)

- 学习Gradle需要掌握俩个点：1是Groovy的语法，2是Gradle的使用方法


## 1.Gradle基本概念
- build.gradle 又被称作构建脚本

- gradle为我们提供了许多默认的配置以及通常的默认值

- Gradle是一种基于Groovy的动态DSL，而Groovy是一种基于jvm的动态语言

### 1.1 Project和tasks
- 每个项目的编译至少有一个project

- 每个project至少有一个task,task里面又包含了很多action，action就是一个代码块里面包含了需要被执行的代码

- 每一个build.gradle代表一个project

- task在build.gradle中定义


### 1.2 Gradle编译
在编译过程中， Gradle 会根据 build 相关文件，聚合所有的project和task，执行task 中的 action。因为 build.gradle文件中的task非常多，先执行哪个后执行那个需要一种逻辑来保证。这种逻辑就是依赖逻辑，几乎所有的Task 都需要依赖其他 task 来执行，没有被依赖的task 会首先被执行。所以到最后所有的 Task 会构成一个 有向无环图（DAG Directed Acyclic Graph）的数据结构。

编译过程分为三个阶段：

- **初始化阶段**：创建 Project 对象，如果有多个build.gradle，也会创建多个project.
- **配置阶段**：在这个阶段，会执行所有的编译脚本，同时还会创建project的所有的task，为后一个阶段做准备。
- **执行阶段**：在这个阶段，gradle 会根据传入的参数决定如何执行这些task,真正action的执行代码就在这里.


### 1.3 项目结构

 	MyApp
		├── build.gradle
		├── settings.gradle
		└── app
			├── build.gradle
			├── build
			├── libs
			└── src
				└── main
               	├── java
               	│   └── com.package.myapp
               	└── res
                   	├── drawable
                   	├── layout
                   	└── etc.

### 1.4 Gradle Wrapper
Gradle Wrapper 提供了一个batch文件，当使用脚本时，当前的gradle版本会被下载下来 并使用，避免了开发者去下载不同版本的gradle，解决兼容性！

	 myapp/
   	├── gradlew
   	├── gradlew.bat
   	└── gradle/wrapper/
       	├── gradle-wrapper.jar
       	└── gradle-wrapper.properties

- bat文件针对window系统，shell脚本针对mac系统，一个jar文件，一个配置文件。配置文件如下：  

		#Sat May 30 17:41:49 CEST 2015
   	distributionBase=GRADLE_USER_HOME
   	distributionPath=wrapper/dists
   	zipStoreBase=GRADLE_USER_HOME
   	zipStorePath=wrapper/dists
   	distributionUrl=https\://services.gradle.org/distributions/
   	gradle-2.4-all.zip
	
	可以改变distributionUrl 来改变gradle版本

### 1.5 基本构建命令

- 获取所有有分组的可运行tasks，可以添加--all参数  来查看task的依赖关系
		gradlew tasks

- 创建一个指定buildType的apk
		gradlew assembleXXX

- 移除所有的编译输出文件
		gradlew clean

- 同时执行assemble和check命令
		gradlew build 

- 执行lint监测编译
		gradlew check

这些都是基本的命令，在实际项目中会根据不同的配置，会对这些task 设置不同的依赖。比如 默认的 assmeble 会依赖 assembleDebug 和assembleRelease，如果直接执行assmeble，最后会编译debug，和release 的所有版本出来。如果我们只需要编译debug 版本，我们可以运行assembleDebug。

很多命令除了会输出结果到命令行，还会在`build`文件夹下下生成运行报告，例如`check`命令会生成lint-result.html在`build/outputs`


### 1.6 构建脚本的构成

	MyApp
   	├── build.gradle
   	├── settings.gradle
   	└── app
       	└── build.gradle

- **setting.gradle **
	这个 setting 文件定义了哪些module 应该被加入到编译过程，对于单个module 的项目可以不用需要这个文件，但是对于 multimodule 的项目我们就需要这个文件，否则gradle 不知道要加载哪些项目。这个文件的代码在初始化阶段就会被执行。

- **根目录的build.gradle**
	顶层的build.gradle文件的配置最终会被应用到所有项目中。它典型的配置如下：
		buildscript {
    		repositories {
        		jcenter()
    		}

    		dependencies {
        		classpath 'com.android.tools.build:gradle:1.2.3'
    		}
		}

		allprojects{
    		repositories{
        		jcenter()
    		}
		}

	- **buildscript**:定义了Adnroid编译工具的类路径.repositories中，jCenter是一个仓库
	- **allprojects**:定义的属性会被应用到所有的module中，但是为了保证每个项目的独立性，我们一般不会在这里操作太多共有的东西


- **每个项目单独的build.gradle**:仅针对每个module的配置,这里的配置优先级最高

#### 1.6.1 module中的build.gradle介绍

		apply plugin: 'com.android.application'
	
		android {
    		compileSdkVersion 25
    		buildToolsVersion "25.0.3"

    		defaultConfig {
        		applicationId "com.hmt.analytics.customizeplugin"
        		minSdkVersion 16
        		targetSdkVersion 25
        		versionCode 1
        		versionName "1.0"
        		testInstrumentationRunner 	"android.support.test.runner.AndroidJUnitRunner"
    			}

    		buildTypes {
        		release {
            		minifyEnabled false
            		proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        				}
    			}
		}

		dependencies {
    		compile fileTree(dir: 'libs', include: ['*.jar'])
    		compile 'com.android.support:appcompat-v7:25.3.1'
		}



- **apply plugin:**添加了Android程序的gradle插件,plugin提供了Android编译，测试，打包等等task


- **android：** 编译文件中最大的代码块，关于android的所有特殊配置都在这里，这里就是前面plugin所提供的

- **defaultConfig**：程序的默认配置,如何和AndroidMainfest.xml定义了重复的属性，会以这里为主


- **applicationId**:在我们曾经定义的AndroidManifest.xml中，那里定义的包名有两个用途：一个是作为程序的唯一识别ID,防止在同一手机装两个一样的程序；另一个就是作为我们R资源类的包名。在以前我们修改这个ID会导致所有用引用R资源类的地方都要修改。但是现在我们如果修改applicationId只会修改当前程序的ID,而不会去修改源码中资源文件的引用。

- **buildTypes:**定义了编译类型,针对每个类型可以有不同的编译配置,不同的编译配置对应不同的编译命令。默认debug,release


- **dependencies:**属于gradle的依赖配置。定义当前module需要依赖的三方库
	- 引用库时,每个库名称包含三个元素:`组名`：`库名称`：`版本号`
	- 可以通过添加通配符来保证依赖库处于最新状态，但是建议不要这么做，因为这样每次编译都要去请求网络判断是否有最新版本
	- 通过`files()`方法可以添加文件依赖，如果有很多文件，可以通过`fileTree()`方法
- **native libraries**
	配置本地.so库。在配置文件中做如下配置，然后在对应位置建立对应文件夹，并加入对应平台的.so文件即可
		android{
			sourceSets.main{
				jniLibs.srcDir  'src/main/jniLibs'
			}
		}

- **BuildConfig**
	这个类是根据gradle配置文件生成的，其中的参数例如BuildConfig.DEBUG 可以用来判断当前版本是否是debug版本。

	我们可以在defaultConfig中 或buildTypes中具体的类型中 定义一些key-value对，这些key-value对在不同的编译类型的apk下的值不同，例如我们可以为debug,release设置不同的请求地址
		buildTypes{
			debug{
				buildConfigField "String","API_URL","www.google.com"
			}

			release{
				buildConfigField "String","API_URL","www.irs01.com"
			}
		}

	此外还可以为不同编译类型设置不同的资源文件
		buildTypes{
			debug{
				resValue "String","app_name","example_demo"
			}

			release{
				resValue "String","app_name","demo"
			}
		}
		

- **repositories**
	Repositories 就是代码仓库,平时的添加的一些 dependency 就是从这里下载的，Gradle 支持三种类型的仓库：Maven,Ivy和一些静态文件或者文件夹。在编译的执行阶段，gradle 将会从仓库中取出对应需要的依赖文件，当然，gradle 本地也会有自己的缓存，不会每次都去取这些依赖。

	gradle支持多种Maven仓库，一般为公共的Jcenter，可以通过手动添加一些私人库并添加账号密码
		repositories{
			maven{
				url "http://repo.xxx.xx/maven"
				creadentials{
					username 'user'
					password 'password'
				}
			}
		}

	也可以使用相对路径配置本地仓库，可以通过配置项目中存在的静态文件夹作为本地仓库
		repositories{
			flatDir{
				dirs 'aars'
			}
		}


- **library projects**
	需要写一个library项目给其他项目引用，那么apply plugin 就需要改成 'com.android.library',另外还需要在setting.gradle 中include。 默认生成的话as会做好这些

	如果不方便直接引用module ，可以将module打包成aar形式，然后通过文件的形式引用。这种情况需要在项目下新建aars文件夹，并在根目录下的build.gradle配置**本地仓库**，然后在dependencies中添加`compile name:'libraryname',ext:'aar'`

- **build variants-build type**
	在编译的时候动态根据当前的编译类型输出不同样式的apk文件等情况时就需要用到了buiildtypes

		buildTypes{
			staging.initWith buildTypes.debug
			staging{
				applicationIdSuffix '.staging'
				versionNameSuffix '-staging'
				debuggable = false
			}
		}
- **Source sets**
	每当新建一个build type时，gradle默认会创建一个新的source set。可以建立与`main`同级的文件夹，这样在编译时 会根据不同的编译类型 选择某些对应文件夹下的源码。不止文件夹可以替换，资源也可以替换

	另外dependencies中也可以通过 `buildType+compile` 来指定 编译类型去添加指定三方框架


- **product flavors**
	如果我们需要针对同一份代码编译不同的程序(包名不同)，就需要`product flavors`
	- 注意product flavors和build type是不一样的，而且他们的属性也不一样。所有的product flavor和defaultConfig共享属性

	像Build type 一样，product flavor 也可以有自己的source set文件夹。除此之外，product flavor 和 build type 可以结合，他们的文件夹里面的文件优先级甚至高于 单独的built type 和product flavor 文件夹的优先级。如果你想对于 blue类型的release 版本有不同的图标，我们可以建立一个文件夹叫blueRelease，注意，这个顺序不能错，一定是 flavor+buildType 的形式。

	更复杂的情况下，我们可能需要多个product 的维度进行组合，比如我想要 color 和 price 两个维度去构建程序。这时候我们就需要使用flavorDimensions：

			android{
				flavorDimensions 'color','price'

				productFlavors{
					red{
						flavorDimension 'color'
					}
					blue{
						flavorDimension 'color'
					}
					free{
						flavorDimension 'price'
					}
					paid{
						flavorDimension 'price'
					}
				}
			}
	这样gradle会自动进行组合，得出类似blue+free+debug blue+paid+release red+free+debug red+paid+release

	BuildType中定义资源优先级最大，Library中定义的资源优先级最低

- **signing configurations**
	首先我们需要在android{}中配置
		android{
			signingConfigs{
				storeFile file("release.jks")
				storePassword "password"
				keyAlias 'rellease-jks'
				keyPassword "123456"
			}
		}

	配置之后需要在build type中使用
		buildTypes{
			release{
				signingConfig signingConfigs.release
			}
		}
	

#### 1.6.2 Speeding up multimodule build
通过以下方式加快gradle的编译

- **开启并行编译：** 在项目根目录下的`gradle.properties`中设置
		org.gradle.parallel = true

- **开启编译守护进程:** 该进程在第一次启动后会一直存在，接下来每次编译都可以重用该进程，也是在项目根目录下的`gradle.properties`中设置

- **加大可用编译内存： ** 同样是在项目根目录下的`gradle.properties`中设置
		org.gradle.jvmargs=-Xms256m -Xmx1024m



#### 1.6.3 Reducing apk file
在编译的时候，有许多的资源并没有用到，可以通过`shrinkResources`来优化资源文件，除去不必要的资源。

	android{
		buildTypes{
			release{
				minifyEnabled = true //只有当俩者都为true 才会真正的删除无效代码和无银用资源
				shrinkResources = true//
			}
		}
	}

在某些情况下，一些资源是通过动态加载的方式载入的，这时候需要像Progard一样对资源进程**keep**操作。操作方式就是：在`res/raw/`下创建一个`keep.xml`文件，使用如下方式keep资源

	<?xml version="1.0" encoding="utf-8"?>  
	<resources xmlns:tools="http://schemas.android.com/tools"  tools:keep="@layout/activity_four,@drawable/no_reference_but_keep"/>  

	

对于一些尺寸文件，我们可以这么做去防止被去除：

	android{
		defaultConfig{
			resConfigs "hdpi","xhdpi","xxhdpi"
		}
	}

#### 1.6.4 指令
执行`task`的时候可以通过添加`--profile`参数生成一份执行报告在`reports/profile`中


## 2. 实例

### 2.1 keystore 保护
如果我们将store的密码明文的写在signingConfigs里面，对安全性不好，所以需要构建一个动态加载任务，在编译release源码的时候从本地文件(git忽略名单中的文件)获取keystore信息

	task getReleasePsw << {
   	 def psw = ''
   	 println 'getReleasePsw is running'
    	if (rootProject.file('local.properties').exists()) {
     	   java.util.Properties properties = new Properties();
     	   properties.load(rootProject.file('local.properties').newDataInputStream())
    	    psw = properties.getProperty('release.psw')
  	  } else if (psw.trim().isEmpty()) {
    	    //TODO 在获取不到配置文件时 从控制台输入
   	     psw = java.lang.System.in.each {
   	     }
  	  }	
	}

光创建这个task还不行，需要为task添加依赖 这样执行打包task时 自动执行getReleasePsw任务
	
	tasks.whenTaskAdded{
		if(it.name.equals 'packageRelease'){
			it.dependsOn 'getReleasePsw'
		}
	}


### 2.2 hook Android编译插件 重命名apk

	android.applicationVariants.all{variant->
		variant.outputs.each{output->
			def file = output.outputFile

			output.outputFile = new File(file.parent,
			file.name.replace(".apk","-${variant.versionName}.apk"))
		}
	}

生成类似 `app-debug-1.0.apk`


## 3 引用说明
[深入理解Android之Gradle](http://blog.csdn.net/innost/article/details/48228651)

[Gradle之完整指南](http://www.jianshu.com/p/9df3c3b6067a)