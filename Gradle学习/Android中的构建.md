# Android中的构建结构

[Android-Script Block-DSL](https://developer.android.com/tools/building/plugin-for-gradle.html)

[Android-Dsl-APi](https://google.github.io/android-gradle-dsl/current/index.html)


[详解Android Gradle生成字节码流程](https://zhuanlan.zhihu.com/p/98909010)

[【Android 修炼手册】Android Gradle Plugin 插件主要流程](https://zhuanlan.zhihu.com/p/66052867)


# 1. Android 项目结构

 	MyApp
		├── build.gradle // 根目录下的构建脚本
		├── settings.gradle
		├── gradlew
		├── gradlew.bat
		├── gradlew/wrapper/.....
		└── app // 子模块名称
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

# 2. module中的build.gradle介绍

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


# 3. Reducing apk file
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

# 4. Android Gradle Plugin
[Gradle提供的通用DSL文档](https://docs.gradle.org/current/dsl/org.gradle.api.Project.html#org.gradle.api.Project:buildscript(groovy.lang.Closure))

[AGP和KGP字节码生成流程分析](https://zhuanlan.zhihu.com/p/98909010)

对于Android项目,Google提供了专门的android gradle插件，其包含了Appcation/Library的构建流程(构建相关的task),例如插件`com.android.application`,`com.android.library`等

- 根据Gradle插件开发流程，可以在`META-INF/gradle-plugins`目录下找到其对应的实现类

- 对于Kotlin,Google还提供了kotlin gradle plugin,其提供了插件`kotlin-android`,`kotlin-android-extensions`等

可以在`rootProject/build.gradle`的`buildScript`找到该插件


	// Top-level build file where you can add configuration options common to all sub-projects/modules.
	buildscript {
	    repositories {
	        google()
	        jcenter()
	    }
	    dependencies {
			  // Android Gradle Plugin
	        classpath "com.android.tools.build:gradle:4.0.0"
	        
	        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
	
	        // NOTE: Do not place your application dependencies here; they belong
	        // in the individual module build.gradle files
	    }
	}

## 4.1 预备知识
在项目中添加Android Gradle Plugin 依赖，方便查看

android gradle plugin 3.x 之后，每个flavor必须对应一个dimension(理解为flavor的分组,不同分组的flavor会进行组合)

	flavorDimensions "size", "color"
	
	productFlavors {
	    big {
	        dimension "size"
	    }
	    small {
	        dimension "size"
	    }
	    blue {
	        dimension "color"
	    }
	    red {
	        dimension "color"
	    }
	}

- 上面的配置会生成`bigBlue`,`bigRed`,`smallBlue`,`smallRed`，然后会去跟variant进行组合(即buildType),生成`bigBlueDebug`,`bigBlueRelease`等等...


## 4.2 AppPlugin流程

1. 准备工作

	检查插件版本
	检查module是否重名
	初始化插件信息

2. configureProject 配置项目

	检查Gradle版本和GradleAndroidPlugin插件版本是否匹配
	创建AndroidBuilder和DataBindingBuilder,引入JavaPlugin和JacocoPlugin
	设置构建完成之后的缓存清理工作(添加BuildListener监听
	在buildFinished回调中做清理工作)

3. configureExtension配置扩展

	创建`AppExtension`(即build.gradle中的`android{}`DSL)
	创建依赖管理,ndk管理,任务管理,variant管理
	注册新增配置的回调函数,包括`signingConfig`,`buildType`,`productFlavor`
	创建默认的debug签名，创建debug和release俩个buildType

4. createTasksBeforeEvalue 创建不依赖Flavor的task

	这一步在项目配置之前,即`plugin apply`时就执行。这些task可以在自定义构建逻辑时被引用

	主要逻辑在`TaskManager.createTasksBeforeEvaluate()`中,包含几个重要的公共task，例如`uninstallAll，deviceCheck，connectedCheck，preBuild，extractProguardFiles，sourceSets，assembleAndroidTest，compileLint，lint，lintChecks，cleanBuildCacheresolveConfigAttr，consumeConfigAttr`

5. createAndroidTasks 创建构建task

	在`project.afterEvaluate()`中创建构建task(这时所有模块配置已经完成,即可以获取flavor等配置).

	先通过`populateVariantDataList()`生成flavor相关的数据结构(`ApplicationVariantData extends BaseVariant`包含许多任务定义，但是未提供具体实现)，然后调用`createTasksForVariantData`创建flavor对应的task(实现具体的task)

## 4.3 总结流程

1. 可以在`META-INF/gradle-plugins`下找到插件名称和具体实现类的关系
2. com.android.application入口类时AppPlugin,但是大部分工作在BasePlugin
3. `build.gradle`中的`android{}`DSL是`BasePlugin.configureExtension()`中创建的
4. 主要task在`BasePlugin.createAndroidTasks()`里生成
5. transform会转化成TransformTask


# 5. 构建任务分析
以下是谷歌提供的Android应用模块的构建流程

![](https://developer.android.com/images/tools/studio/build-process_2x.png)

这些构建流程可以划分为一个个Task, 执行一下assembleDebug任务,Gradle会根据依赖关系依次执行相关task

	> Task :app:preBuild UP-TO-DATE
	> Task :app:preDebugBuild UP-TO-DATE
	> Task :app:generateDebugBuildConfig
	> Task :app:generateDebugResValues
	> Task :app:compileDebugRenderscript NO-SOURCE
	> Task :app:compileDebugAidl NO-SOURCE
	> Task :app:generateDebugResources
	> Task :app:createDebugCompatibleScreenManifests
	> Task :app:extractDeepLinksDebug
	> Task :app:processDebugManifest
	> Task :app:mergeDebugResources
	> Task :app:processDebugResources
	> Task :app:compileDebugKotlin
	> Task :app:mergeDebugShaders
	> Task :app:compileDebugShaders NO-SOURCE
	> Task :app:generateDebugAssets UP-TO-DATE
	> Task :app:mergeDebugAssets
	> Task :app:javaPreCompileDebug
	> Task :app:compileDebugJavaWithJavac
	> Task :app:compileDebugSources
	> Task :app:processDebugJavaRes NO-SOURCE
	> Task :app:dexBuilderDebug
	> Task :app:checkDebugDuplicateClasses
	> Task :app:mergeLibDexDebug
	> Task :app:mergeDebugJniLibFolders
	> Task :app:validateSigningDebug
	> Task :app:mergeProjectDexDebug
	> Task :app:mergeDebugJavaResource
	> Task :app:mergeDebugNativeLibs
	> Task :app:stripDebugDebugSymbols NO-SOURCE
	> Task :app:mergeExtDexDebug
	> Task :app:packageDebug
	> Task :app:assembleDebug

关于Task的具体实现，可以在`TaskManager`中找到,主要是俩个方法,`TaskManager.createTasksBeforeEvaluate()`和`ApplicationTaskManager.createTasksForVariantScope()`

- 注意有的任务并没有具体的执行内容，而是被作为锚点来使用

![](https://pic3.zhimg.com/80/v2-7be59c23a324174c78ffc8cd1d56b59a_1440w.jpg)

![](https://pic4.zhimg.com/80/v2-eab8b2bc05b76b820e1ad797e2c85023_1440w.jpg)

## 5.1 如何阅读Task
Gradle Plugin中的Task主要有三种类型:

1. 普通Task
	
	通常继承自`DefaultTask`,并且`@TaskAction`注解会指出其执行逻辑所在的方法

		subClass extends BaseTask extends DefaultAndroidTask extends DefaultTask

2. 增量Task

	通常继承自`IncreamentalTask`,并且其`isIncreamental()`方法返回true 。
	
	Task会根据是否需要增量更新调用不同的方法,`doFullTaskAction()`方法中包含全量执行的逻辑,`doIncreamentalTaskAction()`即增量执行逻辑

		subClass extends IncrementalTask extends BaseTask extends DefaultAndroidTask extends DefaultTask

3. Transform

	继承自Transform,其实现在`transform()`方法中
	Transform在被添加到TransformManager之后，会被处理成`TransformTask`

## 5.2 generateDebugBuildConfig任务

实现类方法:

	ApplicationTaskManager.createTasksForVariantScope -> TaskManager.createBuildConfigTask()
	
其配置保存在:

	GenerateBuildConfig 任务类
	GenerateBuildConfig.ConfigAction 类中
	
具体实现方法:

    @TaskAction
    void generate() throws IOException {
	  // 为防止包名变化 导致生成俩套代码，必须先清除
        File destinationDir = getSourceOutputDir();
        FileUtils.cleanOutputDir(destinationDir);

        BuildConfigGenerator generator = new BuildConfigGenerator(
                getSourceOutputDir(),
                getBuildConfigPackageName());

	  // 添加默认属性,包含DEBUG,APPLICATION_ID等等
        generator
                .addField(
                        "boolean",
                        "DEBUG",
                        isDebuggable() ? "Boolean.parseBoolean(\"true\")" : "false")
                .addField("String", "APPLICATION_ID", '"' + appPackageName.get() + '"')
                .addField("String", "BUILD_TYPE", '"' + getBuildTypeName() + '"')
                .addField("String", "FLAVOR", '"' + getFlavorName() + '"')
                .addField("int", "VERSION_CODE", Integer.toString(getVersionCode()))
                .addField(
                        "String", "VERSION_NAME", '"' + Strings.nullToEmpty(getVersionName()) + '"')
                        // 添加自定义属性!
                .addItems(getItems());

        List<String> flavors = getFlavorNamesWithDimensionNames();
        int count = flavors.size();
        if (count > 1) {
            for (int i = 0; i < count; i += 2) {
                generator.addField(
                        "String", "FLAVOR_" + flavors.get(i + 1), '"' + flavors.get(i) + '"');
            }
        }
	  // 调用JavaWriter生成java文件
        generator.generate();
    }
    
## 5.3 mergeDebugResources任务

实现类方法:

	ApplicationTaskManager.createTasksForVariantScope -> TaskManager.createMergeResourcesTask

其配置保存在:

	MergeResources 增量任务类
	MergeResources.ConfigAction

具体实现方法:

	TODO
	
## 5.4 transformClassesWithDexBuilderForDebug任务
实现类方法:

	TaskManager.createPostCompilationTasks -> 



## 5.5 自定义Transform执行

实现类方法:

	ApplicationTaskManager.createTasksForVariantScope -> ApplicationTaskManager.addCompileTask -> TaskManager.createPostCompilationTasks -> 
	
具体实现方法:

	public void createPostCompilationTasks(
	        @NonNull TaskFactory tasks,
	        @NonNull final VariantScope variantScope) {
	        ..........
	         AndroidConfig extension = variantScope.getGlobalScope().getExtension();

	        // ----- External Transforms -----
	        // apply all the external transforms.
	        List<Transform> customTransforms = extension.getTransforms();
	        List<List<Object>> customTransformsDependencies = extension.getTransformsDependencies();

	        for (int i = 0, count = customTransforms.size(); i < count; i++) {
	            Transform transform = customTransforms.get(i);
	
	            List<Object> deps = customTransformsDependencies.get(i);
	            transformManager
	                    .addTransform(tasks, variantScope, transform)
	                    .ifPresent(t -> {
	                        if (!deps.isEmpty()) {
	                            t.dependsOn(tasks, deps);
	                        }
	
	                        // if the task is a no-op then we make assemble task depend on it.
	                        if (transform.getScopes().isEmpty()) {
	                            variantScope.getAssembleTask().dependsOn(tasks, t);
	                        }
	                    });
	        }
	        
	}



    