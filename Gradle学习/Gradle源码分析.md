# Gradle源码分析
[Gradle 庖丁解牛（构建源头源码浅析）](https://blog.csdn.net/yanbober/article/details/60584621)

[gradlew 源码分析](https://blog.knero.cn/2017/10/15/gradlew-source-analyze.html)
# 简介

高版本的Gradle添加了许多高级功能..代码也就变得十分复杂，如果仅仅是为了查看大概的逻辑还是要分析低版本..这篇源码分析就是分析的`Gradle-3.3`版本


# 0. Shell脚本（`gradlew`）

`gradlew`脚本就是去执行`org.gradle.wrapper.GradleWrapperMain`

# 1 GradleWrapper

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


# 4 GradleMain

## 4.1 main()

    public static void main(String[] args) throws Exception {
        new ProcessBootstrap().run("org.gradle.launcher.Main", args);
    }    
    
- **就是设置一堆`ClassPath`环境，然后调用`org.gradle.launcher.Main`的`run()`方法**


# 5 ProcessBootstrap

## 5.1 run()

    public void run(String mainClassName, String[] args) {
        try {
            runNoExit(mainClassName, args);
            System.exit(0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(1);
        }
    }

## 5.2 runNoExit()

    private void runNoExit(String mainClassName, String[] args) throws Exception {
        ClassPathRegistry classPathRegistry = new DefaultClassPathRegistry(new DefaultClassPathProvider(new DefaultModuleRegistry(CurrentGradleInstallation.get())));
        ClassLoaderFactory classLoaderFactory = new DefaultClassLoaderFactory();
        ClassPath antClasspath = classPathRegistry.getClassPath("ANT");
        ClassPath runtimeClasspath = classPathRegistry.getClassPath("GRADLE_RUNTIME");
        ClassLoader antClassLoader = classLoaderFactory.createIsolatedClassLoader(antClasspath);
        ClassLoader runtimeClassLoader = new VisitableURLClassLoader(antClassLoader, runtimeClasspath);

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(runtimeClassLoader);

        try {
            Class<?> mainClass = runtimeClassLoader.loadClass(mainClassName);
            Object entryPoint = mainClass.newInstance();
            Method mainMethod = mainClass.getMethod("run", String[].class);
            mainMethod.invoke(entryPoint, new Object[]{args});
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);

            ClassLoaderUtils.tryClose(runtimeClassLoader);
            ClassLoaderUtils.tryClose(antClassLoader);
        }
    }
    
- **设置`ClassPath`，最终通过反射调用`org.gradle.launcher.Main`类的`run`方法**    
    
# 6 Main

## 6.1 run()

    public void run(String[] args) {
        RecordingExecutionListener listener = new RecordingExecutionListener();
        try {
            doAction(args, listener);
        } catch (Throwable e) {
            createErrorHandler().execute(e);
            listener.onFailure(e);
        }

        Throwable failure = listener.getFailure();
        ExecutionCompleter completer = createCompleter();
        if (failure == null) {
            completer.complete();
        } else {
            completer.completeWithFailure(failure);
        }
    }

- 该方法位于`Main`类的父类中    
    
- **创建了一个`RecordingExecutionListener`，如果构建出错会将异常设置给它。 通过判断它是否存在`Throwable`可以得知本次执行是否成功！！！**    

- 创建了一个`ProcessCompleter`，用来区分程序是正常退出还是异常退出!

### 6.1.1 RecordingExecutionListener

    private static class RecordingExecutionListener implements ExecutionListener {
        private Throwable failure;

        public void onFailure(Throwable failure) {
            this.failure = failure;
        }

        public Throwable getFailure() {
            return failure;
        }
    }



## 6.2 类主体

	public class Main extends EntryPoint {
	    public static void main(String[] args) {
	        new Main().run(args);
	    }
	
	    protected void doAction(String[] args, ExecutionListener listener) {
	        createActionFactory().convert(Arrays.asList(args)).execute(listener);
	    }
	
	    CommandLineActionFactory createActionFactory() {
	        return new CommandLineActionFactory();
	    }
	}   

- **这里就是Gradle的主命令行入口** ,**其`run()`方法来自`EntryPoint`,最终就是会调用`doAction()`**


## 6.3 CommandLineActionFactory

### 6.3.1 convert()

    public Action<ExecutionListener> convert(List<String> args) {
        ServiceRegistry loggingServices = createLoggingServices();

        LoggingConfiguration loggingConfiguration = new DefaultLoggingConfiguration();

        return new WithLogging(loggingServices,
                args,
                loggingConfiguration,
                new ExceptionReportingAction(
                        new JavaRuntimeValidationAction(
                                new ParseAndBuildAction(loggingServices, args)),
                        new BuildExceptionReporter(loggingServices.get(StyledTextOutputFactory.class), loggingConfiguration, clientMetaData())));
    }
    

- **主要就是将命令行中的参数进行包装，返回一个`CommandLineActionFactory.WithLogging`对象**

	`WithLogging`包含了一个`ExceptionReportingAction`,其自身是一个`Action`,并且它嵌套了多层Action,包括`JavaRuntimeValidationAction`,`ParseAndBuildAction`,`BuildExceptionReporter`
	
	这四个`Action`层层嵌套

# 7. CommandLineActionFactory.WithLogging

`WithLogging`继承自`Action<ExecutionListener>`

## 7.1 构造函数

        private final ServiceRegistry loggingServices;
        private final List<String> args;
        private final LoggingConfiguration loggingConfiguration;
        private final Action<ExecutionListener> action;

        WithLogging(ServiceRegistry loggingServices, List<String> args, LoggingConfiguration loggingConfiguration, Action<ExecutionListener> action) {
            this.loggingServices = loggingServices;
            this.args = args;
            this.loggingConfiguration = loggingConfiguration;
            this.action = action;
        }

- **这里的`action`是在`CommandLineActionFactory.convert()`方法中创建的`ExceptionReportingAction`**

## 7.2 WithLogging.execute()

    public void execute(ExecutionListener executionListener) {
        CommandLineConverter<LoggingConfiguration> loggingConfigurationConverter = new LoggingCommandLineConverter();
        CommandLineConverter<BuildLayoutParameters> buildLayoutConverter = new LayoutCommandLineConverter();
        BuildLayoutParameters buildLayout = new BuildLayoutParameters();
        CommandLineParser parser = new CommandLineParser();
        loggingConfigurationConverter.configure(parser);
        buildLayoutConverter.configure(parser);
        parser.allowUnknownOptions();
        parser.allowMixedSubcommandsAndOptions();
        try {
            ParsedCommandLine parsedCommandLine = parser.parse(args);
            loggingConfigurationConverter.convert(parsedCommandLine, loggingConfiguration);
            buildLayoutConverter.convert(parsedCommandLine, buildLayout);
        } catch (CommandLineArgumentException e) {
            // Ignore, deal with this problem later
        }

        LoggingManagerInternal loggingManager = loggingServices.getFactory(LoggingManagerInternal.class).create();
        loggingManager.setLevelInternal(loggingConfiguration.getLogLevel());
        loggingManager.start();
        try {
            NativeServices.initialize(buildLayout.getGradleUserHomeDir());
            loggingManager.attachProcessConsole(loggingConfiguration.getConsoleOutput());
            action.execute(executionListener);
        } finally {
            loggingManager.stop();
        }
    }

- **最终就是调用了由构造函数中传入的`Action`,也就是是调用了`ExceptionReportingActio`的`execute()`方法**
 
# 8. ExceptionReportingAction

**`ExceptionReportingAction`，`JavaRuntimeValidationAction`,`ParseAndBuildAction`,`BuildExceptionReporter`。这四个`Action`层层嵌套,每一层`Action`都将调用下一层的`Action`,直到最后一层，也就是`BuildExceptionReporter `**

## 8.1 ExceptionReportingAction.execute()

    public void execute(ExecutionListener executionListener) {
        try {
            action.execute(executionListener);
        } catch (ReportedException e) {
            executionListener.onFailure(e.getCause());
        } catch (Throwable t) {
            reporter.execute(t);
            executionListener.onFailure(t);
        }
    }   

- **这里的`action`是在`CommandLineActionFactory `中创建的`JavaRuntimeValidationAction `**

# 8.2 JavaRuntimeValidationAction.execute()

    public void execute(ExecutionListener executionListener) {
        UnsupportedJavaRuntimeException.assertUsingVersion("Gradle", JavaVersion.VERSION_1_7);
        action.execute(executionListener);
    }

- 这里对Java版本进行了校验    
    
- **这里的`action`是在`CommandLineActionFactory `中创建的`ParseAndBuildAction `**
    
    
    
# 9. ParseAndBuildAction
`ParseAndBuildAction `是`CommandLineActionFactory`类中的内部类
## 9.1 构造函数

    private final ServiceRegistry loggingServices;
    private final List<String> args;

    private ParseAndBuildAction(ServiceRegistry loggingServices, List<String> args) {
        this.loggingServices = loggingServices;
        this.args = args;
    }

- **`List<String> args` 就是通过命令行传入的参数，包括任务名称，命令选项等。。。**

## 9.2 ParseAndBuildAction.execute()

    public void execute(ExecutionListener executionListener) {
        List<CommandLineAction> actions = new ArrayList<CommandLineAction>();
        // 添加一个Factory,用来创建 help和version对应的aciton
        actions.add(new BuiltInActions());
        // 添加俩个Factory
        createActionFactories(loggingServices, actions);
        CommandLineParser parser = new CommandLineParser();
        for (CommandLineAction action : actions) {
        	  // 执行Factory的configureCommandLineParser()方法
            action.configureCommandLineParser(parser);
        }

        Action<? super ExecutionListener> action;
        try {
        	  // 对命令的参数进行解析,保存到不同的列表中
            ParsedCommandLine commandLine = parser.parse(args);
            // 使用不同的Factory 去创建Action
            action = createAction(actions, parser, commandLine);
        } catch (CommandLineArgumentException e) {
            action = new CommandLineParseFailureAction(parser, e);
        }
		 // 执行创建出的Action
        action.execute(executionListener);
    }

主要逻辑:

1. **添加三个`Factory`(实际类型是`CommandLineAction`）**

	`help, version` 对应的`BuiltInActions`,`GuiActionsFactory`,`BuildActionsFactory`

2. 根据每个`Factory`特定的逻辑(即`configureCommandLineParser()`方法)，对`CommandLineParser`进行设置

3. 解析命令的参数，生成`ParsedCommandLine`，不同的参数会保存在`ParsedCommandLine`对象中不同的列表里

4. `creatAction()`方法中 使用`Factory`创建出对应的`Action`

5. 执行Action，如果出现异常会传递给`executionListener`

### 9.2.1 BuiltInActions

    private static class BuiltInActions implements CommandLineAction {
        public void configureCommandLineParser(CommandLineParser parser) {
            parser.option(HELP, "?", "help").hasDescription("Shows this help message.");
            parser.option(VERSION, "version").hasDescription("Print version info.");
        }

        public Runnable createAction(CommandLineParser parser, ParsedCommandLine commandLine) {
            if (commandLine.hasOption(HELP)) {
                return new ShowUsageAction(parser);
            }
            if (commandLine.hasOption(VERSION)) {
                return new ShowVersionAction();
            }
            return null;
        }
    }

### 9.2.2 createActionFactories()

    protected void createActionFactories(ServiceRegistry loggingServices, Collection<CommandLineAction> actions) {
        actions.add(new GuiActionsFactory());
        actions.add(new BuildActionsFactory(loggingServices, new ParametersConverter(), new CachingJvmVersionDetector(new DefaultJvmVersionDetector(new DefaultExecActionFactory(new IdentityFileResolver())))));
    }

- **这里需要注意的是第二个参数`ParameterConverter`**,后续会通过这个类对命令的参数进行处理

## 9.3 createAction()

    private Action<? super ExecutionListener> createAction(Iterable<CommandLineAction> factories, CommandLineParser parser, ParsedCommandLine commandLine) {
        for (CommandLineAction factory : factories) {
        		// 根据命令的参数创建包含对应执行逻辑的`Runnable`
            Runnable action = factory.createAction(parser, commandLine);
            if (action != null) {
            		// 就是将Runnable 包装成Action
                return Actions.toAction(action);
            }
        }
        throw new UnsupportedOperationException("No action factory for specified command-line arguments.");
    }
    
- 根据`Factory`结合命令的参数 去创建指定的`Runnable`并包装成Action返回, 实际上就是调用`Factory`的`createAction()`方法


# 10. CommandLineAction

**实际上，每个`Factory`都是继承自`CommandLineAction`**。每个`Factory`都有它自己的功能，只会去处理指定的逻辑


	public interface CommandLineAction {
	    /**
	     * Configures the given parser with the options used by this action.
	     */
	    void configureCommandLineParser(CommandLineParser parser);
	
	    /**
	     * Creates an executable action from the given command-line args. Returns null if this action was not selected by the given
	     * command-line args.
	     */
	    Runnable createAction(CommandLineParser parser, ParsedCommandLine commandLine);
	}

## 10.1 BuiltInActions.creatAction()

**以`help`命令来举例,会对传入的命令参数进行判断，然后返回一个指定的`Runnable`用来执行**

    public Runnable createAction(CommandLineParser parser, ParsedCommandLine commandLine) {
    	  // 判断传入的
        if (commandLine.hasOption(HELP)) {
            return new ShowUsageAction(parser);
        }
        if (commandLine.hasOption(VERSION)) {
            return new ShowVersionAction();
        }
        return null;
    }

- 判断选项中是否存在`HELP`或`VERSION`,然后返回一个包含了具体的实现逻辑的`Runnable`


### 10.1.1 ShowUsageAction()

    private static class ShowUsageAction implements Runnable {
        private final CommandLineParser parser;

        public ShowUsageAction(CommandLineParser parser) {
            this.parser = parser;
        }

        public void run() {
            showUsage(System.out, parser);
        }
    }    
    
### 10.1.2 showUsage()

    private static void showUsage(PrintStream out, CommandLineParser parser) {
        out.println();
        out.print("USAGE: ");
        clientMetaData().describeCommand(out, "[option...]", "[task...]");
        out.println();
        out.println();
        parser.printUsage(out);
        out.println();
    }
 
 - 这里是具体的执行逻辑       


## 10.2 BuildActionsFactory

### 10.2.1 BuildActionsFactory.configureCommandLineParser()


### 10.2.2 createAction()

    public Runnable createAction(CommandLineParser parser, ParsedCommandLine commandLine) {
    	 Parameters parameters = parametersConverter.convert(commandLine, new Parameters());
        。。。省略对参数的各种处理,判断，包装等等操作。。。
        
        if (parameters.getDaemonParameters().isEnabled()) {
            return runBuildWithDaemon(parameters.getStartParameter(), parameters.getDaemonParameters(), loggingServices);
        }
        if (canUseCurrentProcess(parameters.getDaemonParameters())) {
            return runBuildInProcess(parameters.getStartParameter(), parameters.getDaemonParameters(), loggingServices);
        }

        return runBuildInSingleUseDaemon(parameters.getStartParameter(), parameters.getDaemonParameters(), loggingServices);
    }

- 主要


# 11 CommandLineParser

## 11.1 option()

    public CommandLineOption option(String... options) {
        for (String option : options) {
            if (optionsByString.containsKey(option)) {
                throw new IllegalArgumentException(String.format("Option '%s' is already defined.", option));
            }
            if (option.startsWith("-")) {
                throw new IllegalArgumentException(String.format("Cannot add option '%s' as an option cannot start with '-'.", option));
            }
            if (!OPTION_NAME_PATTERN.matcher(option).matches()) {
                throw new IllegalArgumentException(String.format("Cannot add option '%s' as an option can only contain alphanumeric characters or '-' or '_'.", option));
            }
        }
        CommandLineOption option = new CommandLineOption(Arrays.asList(options));
        for (String optionStr : option.getOptions()) {
            optionsByString.put(optionStr, option);
        }
        return option;
    }
- 对传入的`option`进行检查，查看是否已经存在，或者其定义的形式是否正确   ,然后保存在`HashMap optionsByString`中

 
