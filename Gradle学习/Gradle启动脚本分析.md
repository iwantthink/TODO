# Gradle源码分析
[Gradle 庖丁解牛（构建源头源码浅析）](https://blog.csdn.net/yanbober/article/details/60584621)

[gradlew 源码分析](https://blog.knero.cn/2017/10/15/gradlew-source-analyze.html)

[Gradle源代码编译以及源代码分析](https://blog.51cto.com/483181/category5.html)

[【Android 修炼手册】Gradle 篇 -- Gradle 源码分析](https://zhuanlan.zhihu.com/p/67842670)
# 简介

高版本的Gradle添加了许多高级功能..代码也就变得十分复杂，如果仅仅是为了查看大概的逻辑还是要分析低版本..这篇源码分析就是分析的`Gradle-4.1`版本


# 0. Shell脚本（`gradlew`）

`gradlew`脚本就是去执行`project/gradle/wrapper/gradle-wrapper.jar`中的  `org.gradle.wrapper.GradleWrapperMain`类，并将命令的参数传入

# 1 GradleWrapperMain

## 1.1 main()

    public static void main(String[] args) throws Exception {
    	 // --------------STEP  ONE----------------
    	 // 读取gradle-wrapper.jar 文件
        File wrapperJar = wrapperJar();
        // gradle-wrapper.properties
        File propertiesFile = wrapperProperties(wrapperJar);
        // 当前项目根目录
        File rootDir = rootDir(wrapperJar);

		 // --------------STEP  TWO----------------
		 // 用来解析命令行
        CommandLineParser parser = new CommandLineParser();
        parser.allowUnknownOptions();
        // 添加 option [g,q]
        parser.option(GRADLE_USER_HOME_OPTION, GRADLE_USER_HOME_DETAILED_OPTION).hasArgument();
        parser.option(GRADLE_QUIET_OPTION, GRADLE_QUIET_DETAILED_OPTION);

		 // 用来解析系统参数
        SystemPropertiesCommandLineConverter converter = new SystemPropertiesCommandLineConverter();
        converter.configure(parser);
		  // 读取 命令行中的参数 ，例如 gradlew [args] assemble
        ParsedCommandLine options = parser.parse(args);

        Properties systemProperties = System.getProperties();
        systemProperties.putAll(converter.convert(options, new HashMap<String, String>()));
        
        // --------------STEP  THREE----------------
		  // 除了使用-g 指定gradle user home 地址
		  //  不然就使用默认 .gradle 所在的文件夹
        File gradleUserHome = gradleUserHome(options);

        addSystemProperties(gradleUserHome, rootDir);
		  // 判断是否存在 q 选项，即不输出日志
        Logger logger = logger(options);

		  // --------------STEP  FOUR----------------
		  // 解析gradle-wrapper.properties 中的信息....
        WrapperExecutor wrapperExecutor = WrapperExecutor.forWrapperPropertiesFile(propertiesFile);
        wrapperExecutor.execute(
                args,
                new Install(logger, new Download(logger, "gradlew", wrapperVersion()), new PathAssembler(gradleUserHome)),
                new BootstrapMainStarter());
    }

- 参数`String [] args`就是向Shell脚本`gradlew`传递的参数，可能包括任务名称，命令行选项(包含长/短选项)..

- **通过`CommandLineParser `会将传入的参数进行解析,区分出任务名称 和 选项,将其进行分类并放入不同的集合中保存**

**这个类主要做四件事情:**

1. 先根据 GradleWrapperMain 这个类找到`gradle-wrapper.jar`,`gradle-wrapper.properties`,`root dir`

2. 读取系统环境的配置

3. 优先读取命令行中通过`-g`设置的Gradle地址，如果不存在，那么读取系统系统属性中设置的Gradle地址，如果再不存在，那么就使用`~/.gradle`

4. 检测`.gradle`下是的指定版本中是否存在`gradle-launcher-[version].jar`（如果不存在，先下载）, 然后通过反射运行`org.gradle.launcher.GradleMain`类的`main()`方法


# 2. WrapperExecutor

## 2.1 execute()

    public void execute(String[] args, Install install, BootstrapMainStarter bootstrapMainStarter) throws Exception {
    	 // 如果不存在指定版本的gradle，那么就会创建文件并下载
        File gradleHome = install.createDist(config);
        bootstrapMainStarter.start(args, gradleHome);
    }

这一步做了俩件事情

1. 下载gradle wrapper 需要的依赖以及源码。 其版本信息和保存路径等可以在`gradle-wrapper.properties`配置中找到

2. 执行gradle构建流程(`BootstrapMainStarter.start()`)

# 3 BootstrapMainStarter

## 3.1 start()

    public void start(String[] args, File gradleHome) throws Exception {
        File gradleJar = findLauncherJar(gradleHome);
        URLClassLoader contextClassLoader = new URLClassLoader(new URL[]{gradleJar.toURI().toURL()}, ClassLoader.getSystemClassLoader().getParent());
        Thread.currentThread().setContextClassLoader(contextClassLoader);
        Class<?> mainClass = contextClassLoader.loadClass("org.gradle.launcher.GradleMain");
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, new Object[]{args});
        if (contextClassLoader instanceof Closeable) {
            ((Closeable) contextClassLoader).close();
        }
    }
    
- **通过反射获取到`org.gradle.launcher.GradleMain`,并执行其`main()`方法**


`gradlewrapper.jar`的执行到这里为止，接下来就是执行在`.gradle`目录下的`gradle-launcher-[version].jar`中的逻辑

# 4. DefaultGradleLauncher

GradleMain 到DefaultGradleLauncher这段代码我还未调通，但是网络上的教程表示最终会执行到这里

TODO:// 待我走通再分析源码


DefaultGradleLauncher:

1. loadSettings

	回调`BuildListener.buildStarted`接口
	执行`init.gradle`
	查找`settings.gradle`位置
	编译`buildSrc`目录
	解析`gradle.properties`
	编译并执行`settings.gradle`
	创建`Project`和`subProject`(DefaultProject)
	
2. configureBuild

	回调`BuildListener.beforeEvaluate`接口
	设置默认task[init,wrapper]和默认接口
	编译`build.gradle`脚本并执行
	回调`BuildListener.afterEvaluate`
	回调`BuildListener.projectsEvaluated`

3. constructTaskGraph

	处理需要排除的task
	添加默认的task
	计算task 依赖图
	生成task 依赖图

4. runTasks

	处理`--dry-run`
	创建线程，执行task
	回调`TaskExecutionListener.beforeExecute`
	task执行前处理[处理try catch , 判断onlyif ,跳过没有action的task ，跳过update-to-date 的task 等等]
	回调`TaskActionListener.beforeActions`
	执行task的actions
	回调`TaskActionListener.afterActions`
	回调`TaskExecutionListener.afterExecute`

5. finishBuild

	回调`BuildListener.buildFinished`
	
# 5. Gradle脚本如何编译和执行

Gradle会利用Groovy相关api将`buid.gradle`中的内容进行编译，最终会生成一个继承了`ProjectScript `的类,每一个闭包都生成一个继承了`Closure`的类

生成的类中会按照`build.gradle`中DSL声明的顺序，在`run`方法中依次调用

**有一点比较重要的是:`buildScript {}` 会早于其他内容执行**















	