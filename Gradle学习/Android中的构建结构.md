# Android中的构建结构

[Android-Script Block-DSL](https://developer.android.com/tools/building/plugin-for-gradle.html)

[Android-Dsl-APi](https://google.github.io/android-gradle-dsl/current/index.html)


# 简介

`Gradle Build Init`插件可用于引导创建新Gradle构建的过程。它支持创建不同类型的全新项目，以及将现有构建（例如，Apache Apache Maven构建）转换为Gradle构建。

`Gradle`插件通常需要在使用之前被应用于项目(参考[Using Plugins](https://docs.gradle.org/4.10-rc-2/userguide/plugins.html#sec:using_plugins)),**但是,`Build Init`插件是一个自动应用的插件,意味着不需要显示的应用它,即可使用**

- 要使用这个插件,只需要执行任务名为`init`的任务,同时该任务还会使用`wrapper`任务去为项目创建`Gradle Wrapper`文件


# 1. 初始化项目


首先,需要创建一个项目的目录. 然后进入到该项目的目录中执行`gradle init`

- 如果需要结合Kotlin DSL使用,那么需要在创建命令后面添加`--dsl kotlin`

		gradle init --dsl kotlin 

- 当命令行中显示`BUILD SUCCESSFUL`,同时出现了如下结构的项目目录时,就表示创建成功

		├── build.gradle  // 用于配置当前项目的 Gradle构建脚本
		├── gradle 
		│   └── wrapper
		│       ├── gradle-wrapper.jar  // Gradle Wrapper可执行Jar
		│       └── gradle-wrapper.properties  // Gradle Wrapper 配置文件
		├── gradlew  // 适用于Unix系统的Gradle Wrapper脚本
		├── gradlew.bat   // 适用于Windows系统的 Gradle Wrapper脚本
		└── settings.gradle  // 用于配置Gradle构建的Gradle设置脚本


## 1.1 Gradle Wrapper

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
	
	- **可以改变distributionUrl 来改变gradle版本**

## 1.2 Android 项目结构

 	MyApp
		├── build.gradle
		├── settings.gradle
		├── gradlew
		├── gradlew.bat
		├── gradlew/wrapper/.....
		└── app // 模块名称
			├── build.gradle // module下的构建脚本
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


- **setting.gradle**

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

### 1.2.1 module中的build.gradle介绍

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


### 1.2.2 Reducing apk file
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
	