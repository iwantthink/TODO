# 参考链接
[Android AOP之字节码插栓](http://blog.csdn.net/sbsujjbcy/article/details/50839263)

[Android Gradle和Gradle插件区别](http://blog.csdn.net/jinrall/article/details/53220761)

[如何利用Android studio查看gradle插件源码](https://www.jianshu.com/p/28bb90e565de)

[android插件官方网站](http://tools.android.com/build#TOC-The-following-release-tags-are-availablestudio-3.0studio-2.3studio-2.2studio-2.0studio-1.5studio-1.4...And-for-gradle:gradle_3.0.0gradle_2.3.0gradle_2.2.0gradle_2.0.0gradle_1.5.0...)

[Transform-Api 官方文档](http://tools.android.com/tech-docs/new-build-system/transform-api)

# 重要提示

从`gradle-plugin 3.0.0` 开始，Google 将Android的一些库放到自己的Google()仓库里了。地址如下：

https://dl.google.com/dl/android/maven2/index.html

但是Google()并没有提供文件遍历功能，所以无法直接访问路径去下载。但是实际上源码还是在那个路径下放着，所以只需要输入待下载文件的完整的路径即可下载。

例如：需要下载`gradle-3.0.0-sources.jar` ,只需要将完整的路径输入即可，https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/3.0.0/gradle-3.0.0-sources.jar

**下载完源文件之后放入指定gradle目录下,即可在`Android Studio中`查看对应的源码**

- gradle目录可以通过AS 得知...

# 简介
从`com.android.tools.build:gradle:1.5.0-beta1`开始，gradle插件包含了一个`Transform`接口，允许第三方插件在class文件转成dex文件之前操作编译好的class文件，这个API目标就是简化class文件的自定义操作而不用对Task进行处理

>Starting with 1.5.0-beta1, the Gradle plugin includes a Transform API allowing 3rd party plugins to manipulate compiled class files before they are converted to dex files.
(The API existed in 1.4.0-beta2 but it's been completely revamped in 1.5.0-beta1)

>The goal of this API is to simplify injecting custom class manipulations without having to deal with tasks, and to offer more flexibility on what is manipulated. The internal code processing (jacoco, progard, multi-dex) have all moved to this new mechanism already in 1.5.0-beta1.
Note: this applies only to the javac/dx code path. Jack does not use this API at the moment.

>The API doc is here.

>To insert a transform into a build, you simply create a new class implementing one of the Transform interfaces, and register it with android.registerTransform(theTransform) or android.registerTransform(theTransform, dependencies).

>Important notes:
>The Dex class is gone. You cannot access it anymore through the variant API (the getter is still there for now but will throw an exception)
>Transform can only be registered globally which applies them to all the variants. We'll improve this shortly.
>There's no way to control ordering of the transforms.
>We're looking for feedback on the API. Please file bugs or email us on our adt-dev mailing list.


# 1.Transform
- 该API 是1.4.0-beta2之后出现的，使用前应该修改`com.android.tools.build:gradle:xxxx`和gradle文件夹下的`distributionUrl`地址


**使用方式**：

	AppExtension.registerTransform(Transform transform,Object....dependencies)

    public void registerTransform(@NonNull Transform transform, Object... dependencies) {

- 第二个参数代表手动可以添加的依赖

## 1.1 使用方式

要使用Transform 必须得先添加 依赖`compile 'com.android.tools.build:gradle-api:2.3.3'`。因为当我们通过调用`android.registerTransform()`方法添加`Transform`，所使用的`android`的类型实际上是`AppExtension`,而这相关类存在于`gradle-api-xxx.jar`包中，**另外：这个包就是 gradle-plugin包**。

- 这里有个区别就是 `Com.android.tools.build.gradle Api`和`Com.android.tools.build.gradle`,前者是APIs to customize Android Gradle Builds
， 后者是Gradle plug-in to build Android applications. 前者只添加一个`gradle-api`jar包，后者会添加 很多个jar包例如`dex`,`builder`之类的。**但是要注意后者包含前者！**

**Gradle 的各种版本(Gradle-Plugin 3.0.0开始，改为保存在Google自己提供的`google()`仓库 )：**

1. [gradle 在maven库中的版本](https://mvnrepository.com/artifact/com.android.tools.build/gradle)

2. [gradle-api 在Maven库中的版本](https://mvnrepository.com/artifact/com.android.tools.build/gradle-api)


## 1.2 与旧版本的区别

- [TransformManager](https://android.googlesource.com/platform/tools/base/+/gradle_2.0.0/build-system/gradle-core/src/main/groovy/com/android/build/gradle/internal/pipeline/TransformManager.java)

编译运行一下module，查看`gradle console `可以看到没有了`preDex`Task,多了一些transform开头的Task。

## 1.3 TransformManager介绍
### 1.3.1 getTaskNamePrefix

`gradle plugin`的源码中有一个叫`TransformManager`的类，这个类管理所有的Trasnsform子类，里面有一个方法`getTaskNamePrefix`,在这个方法中可以**获取Task的前缀**，以`transform`开头，之后根据输入类型，即`ContentType`,将输入类型添加到名称中.`ContentType`之间使用`And`连接，拼接完成之后加上`With`，之后紧跟这个Transform的Name,name是在自定义`Transform`的`getName()`方法中重写返回

- `ContentType`代表这个Transform的输入文件类型，类型主要有俩种：1.`Classes`，2.`Resources` 。

源码如下： 

		@NonNull
		    private static String getTaskNamePrefix(@NonNull Transform transform) {
		        StringBuilder sb = new StringBuilder(100);
		        sb.append("transform");
				//遍历所有输入类型
		        Iterator<ContentType> iterator = transform.getInputTypes().iterator();
		        // there's always at least one
				//将类型名称转成 首字母大写的形式添加到 transform名称中
		        sb.append(capitalize(iterator.next().name().toLowerCase(Locale.getDefault())));
				//如果有不止一种类型,那么将所有的类型都添加到transform名称中
		        while (iterator.hasNext()) {
		            sb.append("And").append(capitalize(
		                    iterator.next().name().toLowerCase(Locale.getDefault())));
		        }
				//将 重写的getName()方法 返回的transform名称 添加到transform名称中
		        sb.append("With").append(capitalize(transform.getName())).append("For");
		
		        return sb.toString();
		    }

- `ContentType`是一个接口，有一个默认的枚举类的实现类，**里面定义了俩种文件，一种是Class文件，另一种就是资源文件**

源码如下：

    /**
     * A content type that is requested through the transform API.
     */
    interface ContentType {

        /**
         * Content type name, readable by humans.
         * @return the string content type name
         */
        String name();

        /**
         * A unique value for a content type.
         */
        int getValue();
    }

    /**
     * The type of of the content.
     */
    enum DefaultContentType implements ContentType {
        /**
         * The content is compiled Java code. This can be in a Jar file or in a folder. If
         * in a folder, it is expected to in sub-folders matching package names.
         */
        CLASSES(0x01),

        /** The content is standard Java resources. */
        RESOURCES(0x02);

        private final int value;

        DefaultContentType(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }


- `Scrope`是另一个枚举类，可以翻译为 **作用域**，`Scrope`和`ContentType`一起组成输出产物的目录结构，可以看到`app-build-intermediates-transforms-dex`就是由这俩个值组合产生的。具体`Scrope`的作用可以看注释
	
	    /**
	     * Definition of a scope.
	     */
	    interface ScopeType {
	
	        /**
	         * Scope name, readable by humans.
	         * @return a scope name.
	         */
	        String name();
	
	        /**
	         * A scope binary flag that will be used to encode directory names. Must be unique.
	         * @return a scope binary flag.
	         */
	        int getValue();
	    }
	
	    /**
	     * The scope of the content.
	     *
	     * <p>
	     * This indicates what the content represents, so that Transforms can apply to only part(s)
	     * of the classes or resources that the build manipulates.
	     */
	    enum Scope implements ScopeType {
	        /** Only the project content */
	        PROJECT(0x01),
	        /** Only the sub-projects. */
	        SUB_PROJECTS(0x04),
	        /** Only the external libraries */
	        EXTERNAL_LIBRARIES(0x10),
	        /** Code that is being tested by the current variant, including dependencies */
	        TESTED_CODE(0x20),
	        /** Local or remote dependencies that are provided-only */
	        PROVIDED_ONLY(0x40),
	
	        /**
	         * Only the project's local dependencies (local jars)
	         *
	         * @deprecated local dependencies are now processed as {@link #EXTERNAL_LIBRARIES}
	         */
	        @Deprecated
	        PROJECT_LOCAL_DEPS(0x02),
	        /**
	         * Only the sub-projects's local dependencies (local jars).
	         *
	         * @deprecated local dependencies are now processed as {@link #EXTERNAL_LIBRARIES}
	         */
	        @Deprecated
	        SUB_PROJECTS_LOCAL_DEPS(0x08);
	
	        private final int value;
	
	        Scope(int value) {
	            this.value = value;
	        }
	
	        @Override
	        public int getValue() {
	            return value;
	        }
	    }



### 1.3.2 getName()


    /**
     * Returns the unique name of the transform.
     *
     * <p>This is associated with the type of work that the transform does. It does not have to be
     * unique per variant.
     */
    @NonNull
    public abstract String getName();

#### 1.3.2.1 举例


1. As目录`app/build/intermediates/transforms`，这个目录下 有一个`proguard`目录，是Transform `ProguardTransform`产生的，在源码中可以找到其实现了`getName`方法，返回了`proguard`.这个`getName()`方法返回的值就创建了`proguard`这个目录

		@NonNull
	    @Override
	    public String getName() {
	        return "proguard";
	    }

	- 继承关系：`ProGuardTransform`->`BaseProguardAction`->`ProguardConfigurable`->`Transform`

2. 接着看这个`ProguardTrasnform`的输入文件类型

		@NonNull
	    @Override
	    public Set<ContentType> getInputTypes() {
	        return TransformManager.CONTENT_JARS;
	    }

	- `TransformManger.CONTENT_JARS`，其实就是之前说的`CotnentType`那个枚举类被一个类似set的集合保存。这一段的意思就是：`Proguard`这个Transform存在俩种输入文件，一种是class文件(含jar)，另一种是资源文件，这个Task是做混淆用的，class文件就是`ProguardTransform`依赖的上一个Transform的输出产物，而资源文件可以是混淆时使用的配置文件。

			public static final Set<ContentType> CONTENT_JARS = ImmutableSet.<ContentType>of(CLASSES, RESOURCES);
				
3. 因此根据`getTaskNamePrefix`的生成规则，这个`Transform`最终在控制台显示的名字	

			transformClassesAndResourcesWithProguardForDebug
	
	- For后面跟的是`buildType+productFlavor`，比如QihooDebug，XiaomiRelease，Debug，Release。

### 1.3.3 输出产物的目录生成规则

1. 输出产物的目录指的是`/proguard/release/0.jar`

2. `proguard`上面说了，是`getName()`方法返回的，而`release`则是`buildType`的名字，注意这里不一定是只有`buildType`，如果你的项目中指定了`productFlavor`，那么可能`release`的上一个节点还有`productFlaovor`，就像这样`/proguard/qihoo/release/`。

3. 在`ProGuardTransform`在其父类`ProguardConfigurable`中重写了`getScopes`方法，先忽略`isLibrary`的情况，先分析作为app module不是library的情况。可以看到最终返回的是`TransformManager.SCOPE_FULL_PROJECT`
		
	    @NonNull
	    @Override
	    public Set<? super Scope> getScopes() {
	        if (variantType == VariantType.LIBRARY) {
	            return TransformManager.SCOPE_FULL_LIBRARY_WITH_LOCAL_JARS;
	        }
	
	        return TransformManager.SCOPE_FULL_PROJECT;
	    }

	**TransformManager.SCOPE\_FULL\_PROJECT的值如下**：
	
	    public static final Set<Scope> SCOPE_FULL_PROJECT =
	            Sets.immutableEnumSet(
	                    Scope.PROJECT,
	                    Scope.SUB_PROJECTS,
	                    Scope.EXTERNAL_LIBRARIES);

4. 在`proguard/release/`目录下，还有一个`_content_.json`文件，里面保存有`scopes,types,format,present`等信息。可以在`ProguardTransform`的`doMinification`方法中找到输出代码：

		File outFile =output.getContentLocation(
		                            "combined_res_and_classes", outputTypes, scopes, Format.JAR);

5. 这个`ProguardTransform`的输出产物，会作为下一个依赖它的`Transform`的输入产物

### 1.3.4 输出输入的关系
在没有开启混淆的情况下,`ProguardTransform的`下一个Transform是`DexTransform`。 

下面分俩种情况,debug 模式 不开启混淆，release 开启混淆,然后打印输入和输出文件情况：

	project.afterEvaluate {
	    project.android.applicationVariants.each { variant ->
	        def proguardTask = project.tasks.findByName("transformClassesAndResourcesWithProguardFor${variant.name.capitalize()}")
	        if (proguardTask) {
	            project.logger.error "proguard=>${variant.name.capitalize()}"
	
	            proguardTask.inputs.files.files.each { File file->
	                project.logger.error "file inputs=>${file.absolutePath}"
	            }
	
	            proguardTask.outputs.files.files.each { File file->
	                project.logger.error "file outputs=>${file.absolutePath}"
	            }
	        }
	
	        def dexTask = project.tasks.findByName("transformClassesWithDexFor${variant.name.capitalize()}")
	        if (dexTask) {
	            project.logger.error "dex=>${variant.name.capitalize()}"
	
	            dexTask.inputs.files.files.each { File file->
	                project.logger.error "file inputs=>${file.absolutePath}"
	            }
	
	            dexTask.outputs.files.files.each { File file->
	                project.logger.error "file outputs=>${file.absolutePath}"
	            }
	        }
	    }
	}

输出日志：

	dex=>Debug
	//省略部分输出日志
	******
	proguard=>Release
	//省略部分输出日志
	file outputs=>E:\github\CustomizePluginDemo\app\build\intermediates\transforms\proguard\release
	dex=>Release
	file inputs=>E:\github\CustomizePluginDemo\app\build\intermediates\transforms\proguard\release
	//省略部分输出日志

可以看到proguard的产物`transforms\proguard\release`变成了 dex的输入文件.

结论：**可以向gradle plugin 注册一个Transform ,这个Transform注册之后，需要在编译成字节码之后被执行，执行完之后再去执行混淆的`ProguardTransform`。这样`ProguardTransform`的输入文件就变成自定义的Transform的输入文件，然后自定义的Transform的输出文件就变成了 `ProguardTransform`的输入文件。 **

**开启混淆其实也是类似的做法，只是把`ProguardTransform`换成了`DexTransform`**


### 1.3.5 自定义Transform
[Android-Plugin-DSL-Reference](https://google.github.io/android-gradle-dsl/current/index.html)


- **有一个很重要的规则，在编写groovy文件的时候一定要记得写包名，另外一些包一定记得import,~~~Android studio 如果你只是复制代码进文件，不会自动添加。。。必须重写一遍。。。 可能是需要设置~~~**

1. 在插件的apply方法中注册一个Transform
		
		/**
		* 注册transform接口
		*/
		def isApp = project.plugins.hasPlugin(AppPlugin)
		if (isApp) {
		      def android = project.extensions.getByType(AppExtension)
		      def transform = new MyTransform(project)
		      android.registerTransform(transform)
		}

	- 这里的`extensions.getByType(AppExtension)`是去获取插件对象，就是我们在`build.gradle`中添加的`apply plugin: 'com.android.application'`,**除了`AppExtension`之外，还有`com.android.library projects`对应`LibraryExtension`,`TestExtension`对应`com.android.test projects`**

	- 通过这个`AppExtension`，可以获取许多`Android`的属性

	- 这里指定插件适应于`app`而不是`library`

	- 换一种写法`project.extensions.getByName('android')` 也能实现同样的功能.**`AppExtension` 就是扩展的类型，`android`就是AppExtension的名称**

	- 直接创建匿名类`Transform`，会报NullPointExc。。

2. Transform具体的实现，需要注意的是`Transform`是在`package com.android.build.api`下的.除了实现一些抽象方法之外，还需要去重写一个`transform(TransformInvocation transformInvacation)`方法

		class MyTransform extends Transform{
		
		    @Override
		    String getName() {
		        return null
		    }
		
		    @Override
		    Set<QualifiedContent.ContentType> getInputTypes() {
		        return null
		    }
		
		    @Override
		    Set<? super QualifiedContent.Scope> getScopes() {
		        return null
		    }
		
		    @Override
		    boolean isIncremental() {
		        return false
		    }
		
		    @Override
		    void transform(TransformInvocation transformInvocation) throws com.android.build.api.transform.TransformException, InterruptedException, IOException {
		        super.transform(transformInvocation)
		    }
		}

3. 在`MyTransform的transfrom()`方法中，可以获取该Transform的输入文件，输入文件有俩类 一种是：`directoryInput`,另一种是:`jarInputs`。前者是输入`.class`文件,后者是输入`.jar`文件

        transformInput.directoryInputs.findAll { DirectoryInput directoryInput ->
            File dest = transformInvocation.outputProvider.getContentLocation(
                    directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY
            )
            
            FileUtils.copyDirectory(directoryInput.file, dest);
			//TODO 处理directory下的class 进行字节码注入
        }

		 transformInput.jarInputs.findAll { JarInput jarInput ->
		                String destName = jarInput.name
		                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
		                if (destName.endsWith(".jar")) {
		                    destName = destName.substring(0, destName.length() - 4)
		                }
		                File dest = transformInvocation.outputProvider.getContentLocation(
		                        destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR
		                )
		                FileUtils.copyFile(jarInput.file, dest)
						//TODO 处理JAR进行字节码注入
		            }
		
	- inputJar输入路径如下：

			log: MyTransform jarinput path = C:\Users\renbo\.android\build-cache\11c54790ff4ad70dd516e92b124412031e233308\output\jars\classes.jar
			log: MyTransform jarinput path = C:\Users\renbo\.android\build-cache\cdb003da677181616841cbde22313188da74858c\output\jars\classes.jar
			log: MyTransform jarinput path = C:\Users\renbo\.android\build-cache\6542b9eeb85d9a97bff471a595a332ea1f76cf29\output\jars\classes.jar
			log: MyTransform jarinput path = C:\Users\renbo\.android\build-cache\0837cda07f168f5cf5f1a53128afa242f7df27bb\output\jars\classes.jar
			log: MyTransform jarinput path = C:\Users\renbo\.android\build-cache\c1ce52a9925745448899f2f64fab670f2ddf3bf8\output\jars\classes.jar
			log: MyTransform jarinput path = C:\Users\renbo\.android\build-cache\81379c51ac83ab95be3844a95a8790c7ccd393be\output\jars\classes.jar
			log: MyTransform jarinput path = C:\Users\renbo\.android\build-cache\607e64fb4b0903b7418588a4a6af6ca27ba62ff5\output\jars\classes.jar
			log: MyTransform jarinput path = C:\Users\renbo\.android\build-cache\c0eb07f32bf544d0c7c1fdba1f54b0bf220a8580\output\jars\classes.jar
			log: MyTransform jarinput path = E:\sdk\extras\android\m2repository\com\android\support\support-annotations\25.3.1\support-annotations-25.3.1.jar
			log: MyTransform jarinput path = C:\Users\renbo\.android\build-cache\f48922dbc0641d50d385fd74aa662575552d735c\output\jars\classes.jar
			log: MyTransform jarinput path = C:\Users\renbo\.android\build-cache\1847cb314b40784e3716b3be490b8e694e5e9f42\output\jars\classes.jar

	- directoryInput输入路径如下：

			log: MyTransform directoryInput path = E:\github\CustomizePluginDemo\app\build\intermediates\classes\apple\release

	- 通过调用` transformInput.directoryInputs`会返回输入的文件夹的集合，通常是`.class`文件的文件夹

	- 通过调用`transformInput.jarInputs`会返回输入的Jar文件的集合，通常是依赖

	- **通过`TransformOutputProvider`可以获取到输出文件的输出路径**

	- 自定义Transform的执行顺序是在：.java编译成.class之后，.class转换成.dex之前 。这一点可以从文档简介中看到

	 		to manipulate compiled class files before they are converted to dex files

	- Transform 都是一一对应的，上一个Transform的输出文件 就是下一个Transform的输入文件。所以需要将输出文件输出到指定位置 以供下一个Transform去使用，这个过程应该是TransformManager去控制的。

4. 最后只用在TODO的地方对需要修改的文件 进行ASM操作即可，记得先删除，再保存回`dest`中


### 1.3.6 定义扩展
创建`extensions`，包含`includePkg`,`excludeClass`,`mappingDir`。`includePkg`表示什么包名下的类会被修改，`excludeClass`可以指定哪些类名被排除在外，`oldDir`表示mapping文件的地址，用在混淆操作时(其实是想用这个参数作为指定补丁包的类所在的地址)

定义：  

	public class MyExtension {
	    List<String> includePkg = []
	    List<String> excludeClass = []
	    String oldDir
	
	    MyExtension(Project project) {
	
	    }
	}

    project.extensions.create('RYAN', MyExtension, project)

使用：

	RYAN {
	    excludeClass = ["Test.class"]
	    includePkg = ["com.hmt.analytics.customizeplugin"]
	    oldDir = "$project.buildDir/outputs/ryan"
	}

- TODO这里有一个待验证的行为,就是如果对Application的子类进行字节码修改。。可能会报出来一个`ClassNotFound`..

### 1.3.7 从manifest文件找到application


	def processManifestTask = project.tasks.findByName("process${buildAndFlavor}Manifest")
	def manifestFile = processManifestTask.outputs.files.files[0]
	def applicationName = getApplication(manifestFile)

	public static String getApplication(File manifestFile) {
        def manifest = new XmlParser().parse(manifestFile)
        def androidTag = new groovy.xml.Namespace("http://schemas.android.com/apk/res/android", 'android')
        def applicationName = manifest.application[0].attribute(androidTag.name)

        if (applicationName != null) {
            return applicationName.replace(".", "/") + ".class"
        }
        return null;
    }

### 1.3.8 文件对比-hash值
举个栗子：
例如1.0的版本，已经打包发布，这时候 最好将每个class文件的`hash`值保存下来。 这样进行热修复 打补丁时， 我们在修改完对应 .java文件之后，在编译过程 执行到`MyTransform`的时候，与`1.0`版本保存下来的`hash`值 进行对比。。就可以知道哪些类是需要打进补丁包内的！

### 1.3.9 字节码注入操作
- 在`1.3.5`当中，已经得知`transform()`方法 可以获取到输入文件，只需要在TODO的地方进行操作即可

- groovy提供了及其方便的遍历的 函数`traverse`,具体使用去看groovy文档接口

- `transform`的输入文件，并不全都是需要进行字节码修改的文件，另外还需要根据之前定义的`extensions` 来对文件进行过滤。

	    boolean filterFile(File file) {
	        def needDeal = false
	        mExtension.includePkg.each { String item ->
	            log("includePkg = $item")
	            def replacedPath = file.absolutePath.replace("\\$file.name", "")
	            def replacedItem = item.replace(".", "\\")
	            log("replacedItem = $replacedItem")
	            log("replacedPath = $replacedPath")
	            //指定包名，并且不是以R开头的资源文件，不是BuildConfig.class
	            //TODO 完善判断过滤文件的机制
	            if (replacedPath.endsWith(replacedItem) &&
	                    !file.name.startsWith("R\$") &&
	                    !file.name.startsWith("BuildConfig") &&
	                    !file.name.equals("R.class")) {
	                log("file $file.name need modify")
	                needDeal = true
	                //排除忽略的类文件
	                mExtension.excludeClass.each {
	                    log("excludeClass = $it")
	                    if (it.equals(file.name)) {
	                        log("file in excludeClass")
	                        needDeal = false
	                    }
	                }
	            }
	        }
	        return needDeal
	    }

- 遍历并过滤之后，这里的做法是 将被修改过的文件记录下来，等输入文件输出到`MyTransform`路径下之后，再去`MyTransform`输出路径下去替换这些文件！**在这里只是处理了diretory下的class，其实jar也是差不多的**

		directoryInput.file.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { File classFile ->
	                    log("classFile = $classFile.name")
	                    boolean needModify = filterFile(classFile)
	                    if (needModify) {
	                        try {
	                            File modifiedFile = Inject.injectClass(directoryInput.file, classFile, transformInvocation.context)
	                            modifyMap.put(classFile.absolutePath.replace(directoryInput.file.absolutePath, ""), modifiedFile)
	                        } catch (Exception e) {
	                            log(e.message)
	                        }
	                        log("over")
	                    }
	                }
		 //输出到MyTransform的输出路径下
		 FileUtils.copyDirectory(directoryInput.file, dest);

		 modifyMap.each {
		                 log("key = $it.key")
		                 log("value = $it.value.absolutePath")
		                 File targetFile = new File(dest.absolutePath + "\\$it.key")
		                 if (targetFile.exists()) {
		                        targetFile.delete()
		                    }
		                 FileUtils.copyFile(it.value, targetFile)
		                }

	- 在这一步当中，还可以做文件`hash`值校验，更新(输出新的hash值到文件，以备下次校验)

### 1.3.10 拷贝mapping文件
- mapping文件就是混淆的对应规则。。如果下一次打补丁包，有可能会需要进行混淆,那么就需要应用mapping文件。。 那这个混淆规则 就必须得保持一致！

- mapping文件是混淆完成后输出的,因此需要hook掉混淆的task，在task完成的时候输出文件！

- mapping文件实例部分内容：

		android.support.annotation.Keep -> android.support.annotation.Keep:
		android.support.graphics.drawable.AndroidResources -> android.support.a.a.a:
	    int[] styleable_VectorDrawableTypeArray -> a
	    int[] styleable_VectorDrawableGroup -> b
	    int[] styleable_VectorDrawablePath -> c
	    int[] styleable_VectorDrawableClipPath -> d
	    int[] styleable_AnimatedVectorDrawable -> e
	    int[] styleable_AnimatedVectorDrawableTarget -> f
	    void <clinit>() -> <clinit>

- 代码：

        project.afterEvaluate {
            def flavorsAndTypes = getProductFlavorsBuildTypes(project)
            flavorsAndTypes.each { item ->
                copyMappingFile(project, item)
            }
        }

	    def copyMappingFile(Project project, String flavorsAndTypes) {
	        def changedFlavorsAndTypes = capitalize(flavorsAndTypes)
	        def proguardTask = project.tasks.findByName("transformClassesAndResourcesWithProguardFor${changedFlavorsAndTypes}")
	        if (proguardTask) {
	            proguardTask.doLast {
	                def mapFile = new File("$project.buildDir/outputs/mapping/$flavorsAndTypes/mapping.txt")
	                def mapCopyDir = new File("$project.buildDir/outputs/ryan/$flavorsAndTypes")
	                if (!mapCopyDir.exists()) {
	                    mapCopyDir.mkdirs()
	                }
	                def mapCopyFile = new File("$mapCopyDir.absolutePath/mapping.txt")
	                if (!mapCopyFile.exists()) {
	                    mapCopyFile.createNewFile()
	                }
	                FileUtils.copyFile(mapFile, mapCopyFile)
	                List<File> fileList = getProguardConfigFile(proguardTask)
	                mProguardConfigFile.put(flavorsAndTypes, fileList)
	            }
	
	        }
	    }
		//capitalize 就是将首字母替换成大写的,可以查看gradle console 中输出的 task日志，都是驼峰式的
		// Gstring提供了这个函数。
		String capitalize(String flavorsAndTypes) {
	        String[] arrays = flavorsAndTypes.split("\\\\")
	        def changedFlavorsAndTypes = ""
	        arrays.each {
	            //changedFlavorsAndTypes += it.replace(it.substring(0, 1), it.substring(0, 1).toUpperCase())
				changedFloavrosAndTypes += it.capitalize()
	        }
	        changedFlavorsAndTypes
	    }



### 1.3.11 获取productFlavorsBuildTypes
- `AppExtension`中有一个方法`applicationVariants`

	    project.extensions.getByType(AppExtension).applicationVariants.findAll {
	        System.err.println("application variants = $it.name")
	    }
		//输出内容
		application variants = orangeDebug
		application variants = orangeRelease
		application variants = appleDebug
		application variants = appleRelease



- 也可以通过`AppExtension`中的`productFlavors`和`buildTypes`自己进行组合。这里获取到productFlavors&buildTypes 主要是为了放到路径中去使用，所以自己单独做了处理。没有特殊要求 可以用上面那种

	    List getProductFlavorsBuildTypes(Project project) {
	        def app = project.extensions.getByType(AppExtension)
	        List composedList = []
	        def flavors = app.productFlavors
	        def types = app.getBuildTypes()
	        if (flavors) {
	            flavors.each { productFlavor ->
	                if (types) {
	                    types.each { buildType ->
	                        composedList.add("$productFlavor.name\\$buildType.name")
	                    }
	                } else {
	                    composedList.add("$productFlavor.name")
	                }
	            }
	        } else {
	            types.each { buildType ->
	                composedList.add("$buildType.name")
	
	            }
	        }
	        return composedList
	    }

### 1.3.12 获取proguardConfigFile
- **获取混淆的配置文件，这样在打补丁包的时候 就可以使用**

- 可以在获取mapping的时候 顺便做这个事情。。因为都在获取`proguardTask`..也可以单独的做

- 这里的`proguardTask`就是通过`project.tasks.getByName("transformClassesAndResourcesWithProguardFor${changedFlavorsAndTypes}")` 获取到的一个`task`.可以通过打印该task的 `metaClass`，会发现它的类型是`TransformTask`而不是`Transform`的实现。 这个`TransformTask`是一个包装类，里面包装了`Transform`

- `getAllConfigurationFiles`拿到了所有的配置文件，这些配置文件包括了在build.gradle中定义的混淆配置以及aapt的混淆配置

	    def getProguardConfigFile(TransformTask proguardTask) {
	        //proguardTask 存在一个方法获取transform
	        ProGuardTransform proGuardTransform = proguardTask.getTransform()
	        //获取所有的混淆的配置文件。。 这个方法是ProguardTRansform 的接口中的方法
	        //代码好像不提示。。。 但是 我打出来之后是可以使用
	        proGuardTransform.getAllConfigurationFiles()
	    }

### 1.3.13 获取sdk路径
先从root路径下的local.properties中获取sdk路径.如果没有该文件，就通过系统环境变量来获取sdk的路径

	   def getSdkDir(Project project) {
	        //确定sdkDir的路径
	        def sdkDir
	        Properties properties = new Properties()
	        File localProps = project.rootProject.file('local.properties')
	        if (localProps.exists()) {
	            properties.load(localProps.newDataInputStream())
	            sdkDir = properties.getProperty('sdk.dir')
	        } else {
	            sdkDir = System.getenv('ANDROID_HOME')
	        }
	    }

### 1.3.14 获取存储下来的mapping文件

    def getMappingFile(Project project, String flavorAndType) {
        def TAG = "getMappingFile :"
        def mExtension = project.extensions.findByName('RYAN') as MyExtension
        def mappingDir = new File(mExtension.oldDir)
        if (mappingDir.exists()) {
            logE("$TAG mappingDir exist")
            logE("$TAG $mappingDir.absolutePath/$flavorAndType/mapping.txt")
            def mappingFile = new File("$mappingDir.absolutePath/$flavorAndType/mapping.txt")
            logE("$TAG mappingFile path = $mappingFile.absolutePath")
            mappingFile
        }

    }

### 1.3.15 保存输入文件的路径
- 保存`Transform`的输入文件，因为在打补丁包的时候，需要添加 这些补丁包的 依赖类！！所以建议在`Transform`遍历文件的时候把这些输入文件的路径保存下来！方便 在做dex操作时使用

		proguardLibfiles.add(directoryInput.file)
		proguardLibfiles.add(jarInput.file)
		
		if (proguardLibfiles && proguardLibfiles.size()) {
		            File output = new File(mProject.buildDir.absolutePath + "\\tmp\\libFiles.txt")
		            if (!output.exists()) {
		                output.createNewFile()
		            }
		            output.withDataOutputStream { strem ->
		                proguardLibfiles.findAll { File file ->
		                    strem.write("$file.absolutePath\n".getBytes())
		                    strem.flush()
		                }
		            }
		        }


### 1.3.16 读取保存的输入文件路径

	  def getProguardLibFiles() {
	        List<File> proguardLibFiles = new ArrayList<>()
	        File output = new File(mProject.buildDir.absolutePath + "\\tmp\\libFiles.txt")
	        output.withDataInputStream { input ->
	            input.eachLine { path ->
	                logE("proguardLibFiels = $path")
	                proguardLibFiles.add(new File(path))
	            }
	        }
	        proguardLibFiles
	    }

### 1.3.13 dex操作
- 这一步主要完成的就是将class文件打成dex文件。这里需要知道一点就是之前自定义的`Transform`是在混淆操作之前的！ **所以输出的文件 也是未经混淆的**。如果是将未混淆的文件 打成dex包，下发到app端，进行热修复 肯定是错误的！

- 因此需要判断是否存在混淆的task，如果存在 则需要手动混淆，混淆的时候应用之前记录下来的`configurationFiles`,并且还需要应用上次发版时的`mapping`文件 以保持类名的对应

- 关键的一点就是需要将混淆的代码加入到`configuration.programJars`中去，混淆的依赖代码加入到`configuration.libraryJars`中去，而依赖的代码就是transform的输入文件，需要将这些输入文件一一保存起来，这样混淆的时候才能拿到。我们只需在遍历输入文件的时候加入到一个变量中即可

- 打包准备过程：

		 def prepareDex(Project project) {
		        def TAG = 'method dex:  '
		        List<String> flavorsAndTypes = getProductFlavorsBuildTypes(project)
		        flavorsAndTypes.each { item ->
		            File classDir = new File("$project.buildDir\\tmp\\$item")
					//判断被打包的文件夹是否存在
		            if (classDir.exists() && classDir.listFiles().size() > 0) {
		                logE("$TAG $item classDir has subFile or dir")
						//获取sdk 的地址
		                def sdkDir = getSdkDir(project)
		                logE("$TAG sdkDir = $sdkDir")
		                if (sdkDir) {
		                    def changedFlavorsAndTypes = capitalize(item)
		                    def proguardTask = project.tasks.
		                            findByName("transformClassesAndResourcesWithProguardFor${changedFlavorsAndTypes}")
		                    if (proguardTask) {
		                        logE("$TAG proguardTask can be find")
		                        def mappingFile = getMappingFile(project, item)
		                        //混淆的配置
		                        Configuration configuration = new Configuration()
		                        //使用混合的类名，这样不同的类混淆后将使用同一类名
		                        configuration.useMixedCaseClassNames = false
		                        configuration.programJars = new ClassPath()
		                        configuration.libraryJars = new ClassPath()
		                        //应用mapping文件
		                        configuration.applyMapping = mappingFile
		                        //打开日志
		                        configuration.verbose = true
		                        //输出配置文件
		                        configuration.printConfiguration = new File("$classDir.absolutePath/dump.txt")
		                        //不过滤没有引用的文件....应该是我们打的这些都都没有引用的文件，所以必须不过滤
		                        configuration.shrink = false
		                        //将android.jar和apache库加入依赖
		                        def compileSdkVersion = project.android.compileSdkVersion
		                        logE("compileSdkVersion = $compileSdkVersion")
		                        ClassPathEntry androidEntry = new ClassPathEntry(new File("$sdkDir/platforms/$compileSdkVersion/android.jar"), false)
		                        configuration.libraryJars.add(androidEntry)
		
		                        File apacheFile = new File("$sdkDir/$compileSdkVersion/platforms/optional/org.apache.http.legacy.jar")
		                        //android-23 以下才存在apache包
		                        if (apacheFile.exists()) {
		                            ClassPathEntry apacheEntry = new ClassPathEntry(apacheFile, false)
		                            configuration.libraryJars.add(apacheFile)
		                        }
		                        List<File> proguardLibFiles = getProguardLibFiles()
		                        //将MyTransform的所有输入文件都添加到混淆依赖jar
		                        if (proguardLibFiles) {
		                            ClassPathEntry jarFile
		                            proguardLibFiles.findAll { file ->
		                                jarFile = new ClassPathEntry(file, false)
		                                configuration.libraryJars.add(jarFile)
		                            }
		                        }
		
		                        //设置待dex未混淆的目录
		                        ClassPathEntry classPathEntry = new ClassPathEntry(classDir, false)
		                        configuration.programJars.add(classPathEntry)
		
		                        //定义混淆输出路径
		                        File proguardOutPut = new File("$project.buildDir.absolutePath/tmp/$item/proguard")
		                        //第二个参数表示是输出
		                        ClassPathEntry classPathEntryOut = new ClassPathEntry(proguardOutPut, true)
		                        configuration.programJars.add(classPathEntryOut)
		
		                        //外部定义的混淆文件的获取并应用
		                        def file = mProguardConfigFile.get(item)
		                        file.findAll { proguardFile ->
		                            logE("$TAG proguard外部定义的混淆文件 = $proguardFile.absolutePath")
		                            ConfigurationParser proguardParser = new ConfigurationParser(proguardFile, System.getProperties())
		                            try {
		                                proguardParser.parse(configuration)
		                            } catch (Exception e) {
		                                logE(e.message)
		                            }
		                        }
		
		                        //执行混淆
		                        ProGuard proGuard = new ProGuard(configuration)
		                        proGuard.execute()
								//这里的这个路径 就是做完混淆之后的那些需要dex的类的路径！
		                        classDir = proguardOutPut
		
		                    }
		
		                    dex(project, sdkDir, classDir)
		
		                } else {
		                    logE("$TAG android sdk dir not defined")
		                }
		            }
		        }
		    }


- 实际打包过程

		 def dex(Project project, String sdkDir, File classDir) {
		        def TAG = "dex :　"
		        logE("$TAG dex begining")
		        def cmdExt = Os.isFamily(Os.FAMILY_WINDOWS) ? '.bat' : ''
		        def stdout = new ByteArrayOutputStream()
		        project.exec {
		            commandLine "${sdkDir}/build-tools/${project.android.buildToolsVersion}/dx${cmdExt}",
		                    '--dex',
		                    "--output=${new File(classDir, 'ryan_dex.jar').absolutePath}",
		                    "${classDir.absolutePath}"
		            standardOutput = stdout
		        }
		        def error = stdout.toString().trim()
		        if (error) {
		            logE("$TAG dex error = $error")
		        }
		    }

- 具体的混淆的逻辑。。建议看gradle的源码，因为上面的这些步骤 就是对gradle混淆的源码的复现


# 2. Android DSL和Gradle 类的对应关系

## 2.1 添加插件

在Android  Studio 添加插件时：

	apply plugin: 'com.android.application'

实际上`apply()`方法是属于当前`build.gradle`对应的Project对象(每个`build.gradle`会对应一个Project对象)。默认的Project对象应该是`DefaultProject`类型的,这一点可以通过查看这个类的继承关系得知，`Project`是一个接口，它的继承关系是：

	Project(接口)->ProjectInternal(接口)->DefaultProject（类）

	AbstractPluginAware(抽象类)-> DefaultProject(类)

	PluginAware(接口)->PluginAwareInternal(接口)->AbstractPluginAware(抽象类)

- 实际上`apply()`方法是在`PluginAware`接口中被定义的

- `Project`默认实现是`DefaultProject`

## 2.2 插件

`apply plugin: 'com.android.application'` ,表示添加插件，`com.android.application`实际上就是这个插件的名称，即`resources/META-INF.gradle-plugins/.properties`目录下的`.properties`文件的名称

每一个插件，用代码来表示 实际上就是实现了`org.gradle.api.Plugin`接口的一个类，它有一个`apply()`方法，**注意这个apply方法和Project.apply()是不同的，它们具有不同的作用**

**实际上，`application`对应的插件类就是 `AppPlugin`,这一点可以从代码注释中看到**
	
	/**
	 * Gradle plugin class for 'application' projects.
	 */
	public class AppPlugin extends BasePlugin implements Plugin<Project> {
		。。。
	}

`AppPlugin`的继承关系

插件中的`apply()`会创建`Extension`提供开发者使用

    @NonNull
    @Override
    protected BaseExtension createExtension(
            @NonNull Project project,
            @NonNull ProjectOptions projectOptions,
            @NonNull Instantiator instantiator,
            @NonNull AndroidBuilder androidBuilder,
            @NonNull SdkHandler sdkHandler,
            @NonNull NamedDomainObjectContainer<BuildType> buildTypeContainer,
            @NonNull NamedDomainObjectContainer<ProductFlavor> productFlavorContainer,
            @NonNull NamedDomainObjectContainer<SigningConfig> signingConfigContainer,
            @NonNull NamedDomainObjectContainer<BaseVariantOutput> buildOutputs,
            @NonNull ExtraModelInfo extraModelInfo) {
        return project.getExtensions()
                .create(
                        "android",
                        AppExtension.class,
                        project,
                        projectOptions,
                        instantiator,
                        androidBuilder,
                        sdkHandler,
                        buildTypeContainer,
                        productFlavorContainer,
                        signingConfigContainer,
                        buildOutputs,
                        extraModelInfo);
    }