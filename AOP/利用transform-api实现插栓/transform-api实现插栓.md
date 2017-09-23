# 参考链接
[Android AOP之字节码插栓](http://blog.csdn.net/sbsujjbcy/article/details/50839263)


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
- 该API 是1.5.0-beta1之后出现的，使用前应该修改`com.android.tools.build:gradle:xxxx`和gradle文件夹下的`distributionUrl`地址


**使用方式**：

	android.registerTransform(theTransform) 
	
	android.registerTransform(theTransform, dependencies)


## 1.1 使用方式
- 要使用Transform 必须得先添加 依赖`compile 'com.android.tools.build:gradle-api:2.3.3'`或者`compile 'com.android.tools.build:gradle:2.3.0'`

- 这里有个区别就是 `Com.android.tools.build.gradle Api`和`Com.android.tools.build.gradle`,前者是APIs to customize Android Gradle Builds
， 后者是Gradle plug-in to build Android applications. 前者只添加一个`gradle-api`jar包，后者会添加 很多个jar包例如`dex`,`builder`之类的。**但是要注意后者包含前者！**


- [gradle 在maven库中的版本](https://mvnrepository.com/artifact/com.android.tools.build/gradle)
[gradle-api 在Maven库中的版本](https://mvnrepository.com/artifact/com.android.tools.build/gradle-api)



## 1.2 与旧版本的区别

- [TransformManager](https://android.googlesource.com/platform/tools/base/+/gradle_2.0.0/build-system/gradle-core/src/main/groovy/com/android/build/gradle/internal/pipeline/TransformManager.java)

- 编译运行一下module，查看gradle console 可以看到没有了`preDex`Task,多了一些transform开头的Task。

## 1.3 TransformManager介绍
### 1.3.1 getTaskNamePrefix
- gradle plugin的源码中有一个叫`TransformManager`的类，这个类管理所有的Trasnsform子类，里面有一个方法`getTaskNamePrefix`,在这个方法中可以获取Task的前缀，以transform开头，之后凭借`ContentType`(这个ContentType代表这个Transform的输入文件类型，类型主要有俩种：1.Classes，2.Resources 。ContentType之间使用And连接，拼接完成之后加上With，之后紧跟这个Transform的Name,name在getName()方法中重写返回即可)

源码如下： 

		@NonNull
		    private static String getTaskNamePrefix(@NonNull Transform transform) {
		        StringBuilder sb = new StringBuilder(100);
		        sb.append("transform");
		
		        Iterator<ContentType> iterator = transform.getInputTypes().iterator();
		        // there's always at least one
		        sb.append(capitalize(iterator.next().name().toLowerCase(Locale.getDefault())));
		        while (iterator.hasNext()) {
		            sb.append("And").append(capitalize(
		                    iterator.next().name().toLowerCase(Locale.getDefault())));
		        }
		
		        sb.append("With").append(capitalize(transform.getName())).append("For");
		
		        return sb.toString();
		    }

- ContentType是一个接口，有一个默认的枚举类的实现类，里面定义了俩种文件，一种是Class文件，另一种就是资源文件

源码如下：

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
	
	        /**
	         * The content is standard Java resources.
	         */
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


- `Scrope`是另一个枚举类，可以翻译为 作用域，`Scrope`和`ContentType`一起组成输出产物的目录结构，可以看到`app-build-intermediates-transforms-dex`就是由这俩个值组合产生的。具体`Scrope`的作用可以看注释

		enum Scope {
		        /** Only the project content */
		        PROJECT(0x01),
		        /** Only the project's local dependencies (local jars) */
		        PROJECT_LOCAL_DEPS(0x02),
		        /** Only the sub-projects. */
		        SUB_PROJECTS(0x04),
		        /** Only the sub-projects's local dependencies (local jars). */
		        SUB_PROJECTS_LOCAL_DEPS(0x08),
		        /** Only the external libraries */
		        EXTERNAL_LIBRARIES(0x10),
		        /** Code that is being tested by the current variant, including dependencies */
		        TESTED_CODE(0x20),
		        /** Local or remote dependencies that are provided-only */
		        PROVIDED_ONLY(0x40);
		
		        private final int value;
		
		        Scope(int value) {
		            this.value = value;
		        }
		
		        public int getValue() {
		            return value;
		        }
		    }



### 1.3.2 getName()
- `app-build-intermediates-transforms`，这个目录下 有一个`proguard`目录，是Transform `ProguardTransform`产生的，在源码中可以找到其实现了`getName`方法，返回了`proguard`.这个`getName()`方法返回的值就创建了`proguard`这个目录

		@NonNull
	    @Override
	    public String getName() {
	        return "proguard";
	    }

- 接着看这个`ProguardTrasnform`的输入文件类型

		@NonNull
	    @Override
	    public Set<ContentType> getInputTypes() {
	        return TransformManager.CONTENT_JARS;
	    }

	- TransformManger.CONTENT_JARS，其实就是之前说的`CotnentType`那个枚举类被一个类似set的集合保存。这一段的意思就是：`Proguard`这个Transform可以接受俩种输入文件，一种是class文件(含jar)，另一种是资源文件，这个Task是做混淆用的，class文件就是`ProguardTransform`依赖的上一个Transform的输出产物，而资源文件可以是混淆时使用的配置文件。

			public static final Set<ContentType> CONTENT_JARS = ImmutableSet.<ContentType>of(CLASSES, RESOURCES);
				
- 因此根据`getTaskNamePrefix`的生成规则，这个`Transform`最终在控制台显示的名字	

			transformClassesAndResourcesWithProguardForDebug
	
	- For后面跟的是buildType+productFlavor，比如QihooDebug，XiaomiRelease，Debug，Release。

### 1.3.3 输出产物的目录生成规则
- 输出产物的目录指的是`/proguard/release/jars/3/1f/main.jar`

- `proguard`上面说了，是`getName()`方法返回的，而`release`则是`buildType`的名字，注意这里不一定是只有`buildType`，如果你的项目中指定了`productFlavor`，那么可能`release`的上一个节点还有`productFlaovor`，就像这样`/proguard/qihoo/release/`。可以看到`ProGuardTransform`中重写了`getScopes`方法，我们先忽略`isLibrary`的情况，因为我们的app module不是library，是一个app。可以看到最终返回的是`TransformManager.SCOPE_FULL_PROJECT`
		
		public Set<Scope> getScopes() {
		  if (isLibrary) {
		      return Sets.immutableEnumSet(Scope.PROJECT, Scope.PROJECT_LOCAL_DEPS);
		  }
		
		  return TransformManager.SCOPE_FULL_PROJECT;
		}





