# Gradle 源码分析

# 简介

从[Gradle启动脚本分析.md]()可知，`gradlew`和`gradle-wrapper.jar`俩者最终会去执行`.gradle`目录下的`gradle-launcher-[version].jar`中的逻辑

- 实际上就是调用`GradleMain.main()`方法

# 1 GradleMain

## 1.1 main()

    public static void main(String[] args) throws Exception {
        new ProcessBootstrap().run("org.gradle.launcher.Main", args);
    }    
    
- **就是设置一堆`ClassPath`环境，然后调用`org.gradle.launcher.Main`的`run()`方法**


# 2 ProcessBootstrap

## 2.1 run()

    public void run(String mainClassName, String[] args) {
        try {
            runNoExit(mainClassName, args);
            System.exit(0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(1);
        }
    }

## 2.2 runNoExit()

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
    
# 3 Main

## 3.1 Main类

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

## 3.2 EntryPoint.run()

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

### 3.2.1 RecordingExecutionListener

    private static class RecordingExecutionListener implements ExecutionListener {
        private Throwable failure;

        public void onFailure(Throwable failure) {
            this.failure = failure;
        }

        public Throwable getFailure() {
            return failure;
        }
    }


## 3.3 CommandLineActionFactory

### 3.3.1 convert()

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

	`WithLogging`,`ExceptionReportingAction `,`JavaRuntimeValidationAction`,`ParseAndBuildAction`,这四个类都是实现了`Action<ExecutionListener>`接口，并且层层嵌套，最终的逻辑在`ParseAndBuildAction`


- **`createLoggingServices()`会创建一个`LoggingManagerInternal`并返回，用来向输出日志**


# 4. CommandLineActionFactory.WithLogging

`WithLogging`继承自`Action<ExecutionListener>`

## 4.1 构造函数

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

## 4.2 WithLogging.execute()

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
 
# 5. ExceptionReportingAction

**`ExceptionReportingAction`，`JavaRuntimeValidationAction`,`ParseAndBuildAction`。这三个`Action`层层嵌套,每一层`Action`都将调用下一层的`Action`,直到最后一层，也就是`ParseAndBuildAction `**

## 5.1 ExceptionReportingAction.execute()

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

# 5.2 JavaRuntimeValidationAction.execute()

    public void execute(ExecutionListener executionListener) {
        UnsupportedJavaRuntimeException.assertUsingVersion("Gradle", JavaVersion.VERSION_1_7);
        action.execute(executionListener);
    }

- 这里对Java版本进行了校验    
    
- **这里的`action`是在`CommandLineActionFactory `中创建的`ParseAndBuildAction `**
    
    
    
# 6. ParseAndBuildAction

**`ParseAndBuildAction `是`CommandLineActionFactory`类中的内部类**
## 6.1 构造函数

    private final ServiceRegistry loggingServices;
    private final List<String> args;

    private ParseAndBuildAction(ServiceRegistry loggingServices, List<String> args) {
        this.loggingServices = loggingServices;
        this.args = args;
    }

- **`List<String> args` 就是通过命令行传入的参数，包括任务名称，命令选项等。。。**



## 6.2 ParseAndBuildAction.execute()

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

	- `BuiltInActions`表示 `gradle -help, gradle -version`命令 

	- `GuiActionsFactory`表示 `gradle -gui`命令

	- `BuildActionsFactory`表示其他的命令，例如`gradle assemble`等等

2. 根据每个`Factory`特定的逻辑(即`configureCommandLineParser()`方法)，对`CommandLineParser`进行设置

3. 解析命令的参数，生成`ParsedCommandLine`，不同的参数会保存在`ParsedCommandLine`对象中不同的列表里

4. `creatAction()`方法中 使用`Factory`创建出对应的`Action`

5. 执行Action，如果出现异常会传递给`executionListener`


- **构建任务对应的`Action`是通过`BuildActionsFactory`来实现的**


### 6.2.1 createActionFactories()

    protected void createActionFactories(ServiceRegistry loggingServices, Collection<CommandLineAction> actions) {
        actions.add(new GuiActionsFactory());
        actions.add(new BuildActionsFactory(loggingServices, new ParametersConverter(), new CachingJvmVersionDetector(new DefaultJvmVersionDetector(new DefaultExecActionFactory(new IdentityFileResolver())))));
    }

- 构建除了俩个`factory`用来处理`action`，重点是`BuildActionsFactory`

- **这里需要注意的是第二个参数`ParameterConverter`**,后续会通过这个类对命令的参数进行处理

## 6.3 createAction()

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

### 6.3.1 BuiltInActions

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

- 最简单的例子，也就是`gradle -help`或`gradle -version`命令,这俩个命令是内置的

# 7 CommandLineAction

**实际上，每个`Factory`都是继承自`CommandLineAction`**。每个`Factory`都有它自己的功能，只会去处理指定的逻辑,**用来处理任务的就是 `BuildActionsFactory `这个factory**


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

## 7.1 BuiltInActions.creatAction()

**以`gradle -help`命令来举例,会对传入的命令参数进行判断，然后返回一个指定的`Runnable`表示执行了命令之后的逻辑**

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


### 7.1.1 ShowUsageAction()

    private static class ShowUsageAction implements Runnable {
        private final CommandLineParser parser;

        public ShowUsageAction(CommandLineParser parser) {
            this.parser = parser;
        }

        public void run() {
            showUsage(System.out, parser);
        }
    }    
    
### 7.1.2 showUsage()

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


## 7.2 BuildActionsFactory

**这个`factory`用来处理任务**

### 7.2.1 BuildActionsFactory.configureCommandLineParser()


### 7.2.2 createAction()

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
    
- 编译种类主要分为两种：

	1. **在本进程中编译**

	2. **在守护进程中编译**    

- 选择逻辑是：

	1. 如果有守护进程在运行，那么连接守护进程然后在守护进程编译

	2. 如果可以在当前进程编译，那么就在当前进程编译

	3. 如果都不满足，那就启动守护进程，然后进行编译

- **runXXX系列方法实质都调用了`runBuildAndCloseServices`方法，只是参数不同而已**
	
### 7.3 BuildActionsFactory.runBuildInProcess()
**这里选择在当前进程中编译的逻辑进行分析:**

    private Runnable runBuildInProcess(StartParameter startParameter, DaemonParameters daemonParameters, ServiceRegistry loggingServices) {
        ServiceRegistry globalServices = ServiceRegistryBuilder.builder()
                .displayName("Global services")
                .parent(loggingServices)
                .parent(NativeServices.getInstance())
                .provider(new GlobalScopeServices(startParameter.isContinuous()))
                .build();

        return runBuildAndCloseServices(startParameter, daemonParameters, globalServices.get(BuildExecuter.class), globalServices);
    }

- 通过`ServiceRegistryBuilder`构建一个`ServiceRegistry`,其默认的实现是`DefaultServiceRegistry`，其添加了俩个`parent`和一个`provider`

	- `loggingServices`实际类型是`LoggingManagerInternal`,其用来输出日志
	
	- `NativeServices`用来提供各种原生平台集成服务	
	- `GlobalScopeServices`定义给定进程中所有服务共享的全局服务。 这包括Gradle CLI, daemon and tooling API provider


- **`ServiceRegistryBuilder`的`build()`方法会将`parent`通过构造函数传入`DefaultServiceRegistry`,将`provider`通过`addProvider()`方法传入`DefaultServiceRegistry`**

- **注意`runBuildAndCloseServices `方法的第三个参数**:`globalServices.get(BuildExecuter.class)`，其实际上调用的是`DefaultServiceRegistry`的`get(BuildExecuter.class)`方法

### 7.3.1 DefaultServiceRegistry

`DefaultServiceRegistry`主要的功能

1. 通过`add(xxx)`方法接受服务对象

2. 通过`addProvider(xxx)`方法接受service provider bean对象

	- `service provider bean`可以有3种类型的方法

		1. factory
		2. decorator
		3. configure

### 7.3.2 DefaultServiceRegistry.addProvider()

    public DefaultServiceRegistry addProvider(Object provider) {
        assertMutable();
        findProviderMethods(provider);
        return this;
    }

### 7.3.3 DefaultServiceRegistry.findProviderMethods()

    private void findProviderMethods(Object target) {
        Class<?> type = target.getClass();
        RelevantMethods methods = getMethods(type);
        for (Method method : methods.decorators) {
            if (parentServices == null) {
                throw new ServiceLookupException(String.format("Cannot use decorator method %s.%s() when no parent registry is provided.", type.getSimpleName(), method.getName()));
            }
            //把target自身和所有父类中 decorator类型的方法 通过new DecoratorMethodService(target, method)包装并加入到ownServices列表。
            ownServices.add(new DecoratorMethodService(target, method));
        }
         //把target自身和所有父类中factory类型的方法 通过new FactoryMethodService(target, method)包装并加入到ownServices列表。
        for (Method method : methods.factories) {
            ownServices.add(new FactoryMethodService(target, method));
        }
         //把target自身和所有父类中叫cofigure的方法都通过反射调用
        for (Method method : methods.configurers) {
            applyConfigureMethod(method, target);
        }
    }

- 在这个类中，将`target`的所有方法分成三类，`factory`,`decorator`,`configure`

	1. `factory`类型:

		方法名称以`create`开头，并且返回值不能为空,例如`protected SomeService createSomeService() { ....}`.参数会通过此`registry`或其父类注入. 注意：`factory`类型的方法，如果符合`decorator`类型的特征，那么就会被在多`decorator`类型来处理

	2. `decorator`类型:

		方法名称以`decorate`开头，接收一个参数，并且这个参数的类型就是返回值的类型 。 在调用该类型方法之前，参数会被保存在父`registry`中，然后传递给它
		
	3. `configure`类型:

		方法名称必须为`configure`,接收`ServiceRegistration`为参数,并且没有返回值.参数会通过此`registry`或其父类注入

- **简而言之，在这个类中，会将所有`factory`和`decorator`类型的方法添加到`ownServices `中,然后会调用所有`ServiceRegistry`的`config()`方法**

### 7.3.4 GlobalScopeServices

根据7.3.3可知，会调用`provider`的`configure()`方法

    void configure(ServiceRegistration registration, ClassLoaderRegistry classLoaderRegistry) {
        final List<PluginServiceRegistry> pluginServiceFactories = new DefaultServiceLocator(classLoaderRegistry.getRuntimeClassLoader(), classLoaderRegistry.getPluginsClassLoader()).getAll(PluginServiceRegistry.class);
        for (PluginServiceRegistry pluginServiceRegistry : pluginServiceFactories) {
            registration.add(PluginServiceRegistry.class, pluginServiceRegistry);
            if (pluginServiceRegistry instanceof GradleUserHomeScopePluginServices) {
                registration.add(GradleUserHomeScopePluginServices.class, (GradleUserHomeScopePluginServices) pluginServiceRegistry);
            }
            pluginServiceRegistry.registerGlobalServices(registration);
        }
    }

- `pluginServiceRegistry`实际上是`LauncherServices `类型

#### 7.3.4.1 LauncherServices.registerGlobalServices()

	public class LauncherServices implements PluginServiceRegistry {
	    public void registerGlobalServices(ServiceRegistration registration) {
	        registration.addProvider(new ToolingGlobalScopeServices());
	    }
		
		static class ToolingGlobalScopeServices {
	        BuildExecuter createBuildExecuter(GradleLauncherFactory gradleLauncherFactory, ServiceRegistry globalServices, ListenerManager listenerManager, FileWatcherFactory fileWatcherFactory, ExecutorFactory executorFactory, StyledTextOutputFactory styledTextOutputFactory) {
	            List<BuildActionRunner> buildActionRunners = globalServices.getAll(BuildActionRunner.class);
	            BuildActionExecuter<BuildActionParameters> delegate = new InProcessBuildActionExecuter(gradleLauncherFactory, new ChainingBuildActionRunner(buildActionRunners));
	            return new ContinuousBuildActionExecuter(delegate, fileWatcherFactory, listenerManager, styledTextOutputFactory, executorFactory);
	        }
	
	        ExecuteBuildActionRunner createExecuteBuildActionRunner() {
	            return new ExecuteBuildActionRunner();
	        }
	
	        ClassLoaderCache createClassLoaderCache() {
	            return new ClassLoaderCache();
	        }
	    }
	}	

- **这里向`DefaultServiceRegistry`添加了一个新的`provider`,即`ToolingGlobalScopeServices`**



### 7.3.4 globalServices.get(BuildExecuter.class)

实际上就是调用`DefaultServiceRegistry `的`get()`方法,中间的逻辑十分复杂...建议直接调试查看。

**这里直接给出结果，这个方法主要就是返回`InProcessBuildActionExecuter`对象!!!!**

### 7.3.5 runBuildAndCloseServices()

    private Runnable runBuildAndCloseServices(StartParameter startParameter, DaemonParameters daemonParameters, BuildActionExecuter<BuildActionParameters> executer, ServiceRegistry sharedServices, Object... stopBeforeSharedServices) {
        BuildActionParameters parameters = createBuildActionParamters(startParameter, daemonParameters);
        Stoppable stoppable = new CompositeStoppable().add(stopBeforeSharedServices).add(sharedServices);
        return new RunBuildAction(executer, startParameter, clientMetaData(), getBuildStartTime(), parameters, sharedServices, stoppable);
    }
 
 
 - **这个方法主要是构建了一个`RunBuildAction`,其包装了`executer`,也就是``**
 
 
# 8 RunBuildAction

`RunBuildAction `实际上是一个`Runnable`,但是会被转换成一个`Action`.实际上就是会调用其`run()`方法


## 8.1 run()方法

    public void run() {
        try {
            executer.execute(
                    new ExecuteBuildAction(startParameter),
                    new DefaultBuildRequestContext(new DefaultBuildRequestMetaData(clientMetaData, startTime), new DefaultBuildCancellationToken(), new NoOpBuildEventConsumer()),
                    buildActionParameters,
                    sharedServices);
        } finally {
            if (stoppable != null) {
                stoppable.stop();
            }
        }
    }

- `executre`实际上就是`InProcessBuildActionExecuter`!!!

	`InProcessBuildActionExecuter`会被`ContinuousBuildActionExecuter`进行装饰，这一点从7.3.4-7.3.5 可知

## 8.2 InProcessBuildActionExecuter.execute()

    public Object execute(BuildAction action, BuildRequestContext buildRequestContext, BuildActionParameters actionParameters, ServiceRegistry contextServices) {
        GradleLauncher gradleLauncher = gradleLauncherFactory.newInstance(action.getStartParameter(), buildRequestContext, contextServices);
        try {
            gradleLauncher.addStandardOutputListener(buildRequestContext.getOutputListener());
            gradleLauncher.addStandardErrorListener(buildRequestContext.getErrorListener());
            GradleBuildController buildController = new GradleBuildController(gradleLauncher);
            buildActionRunner.run(action, buildController);
            return buildController.getResult();
        } finally {
            gradleLauncher.stop();
        }
    }

- `gradleLauncherFactory` : `DefaultGradleLauncherFactory`
 
- `gradleLauncher `: `DefaultGradleLauncher`

- `GradleBuildController `对`gradleLauncher `进行了包装,

### 8.3.1 DefaultGradleLauncherFactory.newInstance()

    @Override
    public GradleLauncher newInstance(StartParameter startParameter, BuildRequestContext requestContext, ServiceRegistry parentRegistry) {
        // This should only be used for top-level builds
        if (rootBuild != null) {
            throw new IllegalStateException("Cannot have a current build");
        }

        if (!(parentRegistry instanceof BuildSessionScopeServices)) {
            throw new IllegalArgumentException("Service registry must be of build session scope");
        }
        BuildSessionScopeServices sessionScopeServices = (BuildSessionScopeServices) parentRegistry;

        DefaultGradleLauncher launcher = doNewInstance(startParameter, null, requestContext.getCancellationToken(), requestContext, requestContext.getEventConsumer(), sessionScopeServices, ImmutableList.of(new Stoppable() {
            @Override
            public void stop() {
                rootBuild = null;
            }
        }));
        rootBuild = launcher;
        DeploymentRegistry deploymentRegistry = parentRegistry.get(DeploymentRegistry.class);
        deploymentRegistry.onNewBuild(launcher.getGradle());
        return launcher;
    }
    
- 这里返回的`launcher`是`DefaultGradleLauncher `    

### 8.4 GradleBuildController.run()

    public GradleInternal run() {
        try {
            return (GradleInternal) getLauncher().run().getGradle();
        } finally {
            state = State.Completed;
        }
    }

- **实际上也就是调用`DefaultGradleLauncher.run()`方法** 


# 9. DefaultGradleLauncher

**前面的一些逻辑，仅仅是为了做一些准备工作。从这里开始，正式的进入了`Gradle`的编译流程，即`Load->Configure->Build`**

- Load: 9.1 - 9.xxxx

- Configure:

- Build:

## 9.1 run()

    public BuildResult run() {
        return doBuild(Stage.Build);
    }

## 9.2 doBuild()

    private BuildResult doBuild(final Stage upTo) {
        Throwable failure = null;
        try {
        	  // 注释1
            buildListener.buildStarted(gradle);
            // 注释2
            doBuildStages(upTo);
            flushPendingCacheOperations();
        } catch (Throwable t) {
            failure = exceptionAnalyser.transform(t);
        }
        BuildResult buildResult = new BuildResult(upTo.name(), gradle, failure);
        buildListener.buildFinished(buildResult);
        if (failure != null) {
            throw new ReportedException(failure);
        }

        return buildResult;
    }

- 注释1，通知gradle构建已经开始，那么`BuildListener buildListener`就是接收者，它具体是什么类型?查看9.2.1

- 注释2, 在`doBuildStages()`这个方法中已经可以清晰的看到 构建的三个步骤`Load->Configure->Build`


### 9.2.1 buildListener的创建

在`DefaultGradleLauncher `的构造函数中，`BuildListener buildListener`被传入。 而`DefaultGradleLauncher `是在`DefaultGradleLauncherFactory`类中被创建

	private final BuildListener buildListener;
    public DefaultGradleLauncher(.......
    					BuildListener buildListener
    					............) {
        ..........
        this.buildListener = buildListener;
		 ........
    }


`DefaultGradleLauncherFactory `调用`newInstance()`创建实例，最终在`doNewInstance()`方法中可以找到`DefaultGradleLauncher `的初始化过程

    private DefaultGradleLauncher doNewInstance(....){
    
            DefaultGradleLauncher gradleLauncher = new DefaultGradleLauncher(
			  ........省略........
            gradle.getBuildListenerBroadcaster(),
            .........省略.......
        );
    }

- `gradle`是`DefaultGradle`类型

`DefaultGradle`的构造函数中创建了这个接收者

    public DefaultGradle(GradleInternal parent, StartParameter startParameter, ServiceRegistryFactory parentRegistry) {
    
        buildListenerBroadcast = getListenerManager().createAnonymousBroadcaster(BuildListener.class);
        projectEvaluationListenerBroadcast = getListenerManager().createAnonymousBroadcaster(ProjectEvaluationListener.class);
        buildListenerBroadcast.add(new BuildAdapter() {
            @Override
            public void projectsLoaded(Gradle gradle) {
                rootProjectActions.execute(rootProject);
                rootProjectActions = null;
            }
        });
    }

## 9.3 doBuildStages(Stage.Build)

	// upTo表示执行到哪一步结束
    private void doBuildStages(Stage upTo) {
       
        if (stage == Stage.Build) {
            throw new IllegalStateException("Cannot build with GradleLauncher multiple times");
        }
		  // stage 默认为空
        if (stage == null) {
            // Evaluate init scripts
            initScriptHandler.executeScripts(gradle);

            // Build `buildSrc`, load settings.gradle, and construct composite (if appropriate)
            settings = settingsLoader.findAndLoadSettings(gradle);

            stage = Stage.Load;
        }

        if (upTo == Stage.Load) {
            return;
        }

        if (stage == Stage.Load) {
            // Configure build
            buildOperationExecutor.run("Configure build", new Action<BuildOperationContext>() {
                @Override
                public void execute(BuildOperationContext buildOperationContext) {
                    buildConfigurer.configure(gradle);

                    if (!gradle.getStartParameter().isConfigureOnDemand()) {
                        buildListener.projectsEvaluated(gradle);
                    }

                    modelConfigurationListener.onConfigure(gradle);
                }
            });

            stage = Stage.Configure;
        }

        if (upTo == Stage.Configure) {
            return;
        }

        // After this point, the GradleLauncher cannot be reused
        stage = Stage.Build;

        // Populate task graph
        buildOperationExecutor.run("Calculate task graph", new Action<BuildOperationContext>() {
            @Override
            public void execute(BuildOperationContext buildOperationContext) {
                buildConfigurationActionExecuter.select(gradle);
                if (gradle.getStartParameter().isConfigureOnDemand()) {
                    buildListener.projectsEvaluated(gradle);
                }
            }
        });

        // Execute build
        buildOperationExecutor.run("Run tasks", new Action<BuildOperationContext>() {
            @Override
            public void execute(BuildOperationContext buildOperationContext) {
                buildExecuter.execute(gradle);
            }
        });
    }

- 这里可以很清晰的看到构建的三个步骤,下面会拆分开来进行分析

	1. 配置文件加载
	
	2. 加载`.gradle`文件以及相应的插件中的类
	
	3. 选择待执行的任务
	
	4. 执行任务

	
# 10 Load步骤

	  // Stage stage 默认为空
    if (stage == null) {
        // 找出一些需要初始化的脚本，并对其进行执行
        initScriptHandler.executeScripts(gradle);

        // Build `buildSrc`, load settings.gradle, and construct composite (if appropriate)
        // 构建`buildSrc`模块，加载`settings.gradle`文件，以及`gradle.properties`文件,并处理项目结构
        settings = settingsLoader.findAndLoadSettings(gradle);
		 // 标记状态为Load
        stage = Stage.Load;
    }

- 计算`init.gradle`内容,并加载`setting.gradle`内容

## 10.1 InitScriptHandler.executeScripts()

    public void executeScripts(final GradleInternal gradle) {
        // 找到需要在构建之前执行的.gradle文件
        final List<File> initScripts = gradle.getStartParameter().getAllInitScripts();
        if (initScripts.isEmpty()) {
            return;
        }
		 // 用来保存构建的一些基本信息,例如进程名称
        BuildOperationDetails operationDetails = BuildOperationDetails.displayName("Run init scripts").progressDisplayName("init scripts").build();
        // 处理脚本文件
        buildOperationExecutor.run(operationDetails, new Action<BuildOperationContext>() {
            @Override
            public void execute(BuildOperationContext buildOperationContext) {
                for (File script : initScripts) {
                    processor.process(new UriScriptSource("initialization script", script), gradle);
                }
            }
        });
    }

- **这个类主要包含俩个作用**

	1. **找到当前构建中所有的初始化脚本**

	2. **执行找到的初始化脚本**

### 10.1.1 StartParameter.getAllInitScripts()

    public List<File> getAllInitScripts() {
        CompositeInitScriptFinder initScriptFinder = new CompositeInitScriptFinder(
            new UserHomeInitScriptFinder(getGradleUserHomeDir()), new DistributionInitScriptFinder(gradleHomeDir)
        );

        List<File> scripts = new ArrayList<File>(getInitScripts());
        initScriptFinder.findScripts(scripts);
        return Collections.unmodifiableList(scripts);
    }
    
- `CompositeInitScriptFinder`只是一个装饰类，它会调用`UserHomeInitScriptFinder `和`DistributionInitScriptFinder `去具体的实现

- 主要就是三个作用:

	1. 尝试找到环境变量`GRADLE_USER_HOME`目录下的`init.gradle`文件，并加入列表

		如果没有配置环境变量`GRADLE_USER_HOME`，那么目录就是`~/.gradle`

	2. 找到环境变量`GRADLE_USER_HOME`下面`init.d`目录下的 `*.gradle`配置文件，并加入列表。

	3. 找到`GRADLE_HOME`目录下`init.d`目录的`*.gradle`配置文件，并加入列表

- `GRADLE_USER_HOME`：默认情况下是`~/.gradle/`,如果通过命令指定了值，那么就是用手动设置的值 . 实际上就是保存gradle信息的地方，例如wrapper 或者缓存.

- `GRADLE_HOME`: 根据`gradle-wrapper.properties`文件中的信息(Gradle版本)，选择的指定文件夹，例如`~/.gradle/wrapper/dists/gradle-4.10.1-all/455itskqi2qtf0v2sja68alqd/gradle-4.10.1`


- `init.d` 目录下可以用来添加`.gradle`文件，每一个都会在构建之前执行

		// init.d 下的readme.txt
		You can add .gradle init scripts to this directory. Each one is executed at the start of the build.


### 10.1.2 UserHomeInitScriptFinder.findScripts()

    public void findScripts(Collection<File> scripts) {
        File userInitScript = new File(userHomeDir, "init.gradle");
        if (userInitScript.isFile()) {
            scripts.add(userInitScript);
        }
        findScriptsInDir(new File(userHomeDir, "init.d"), scripts);
    }

- 将`GRADLE_USER_HOME`下的`init.gradle`文件添加到集合中

- 将`GRADLE_USER_HOME`下`init.d`文件夹中所有扩展名为`.gradle`文件添加到集合中

    
### 10.1.3 DistributionInitScriptFinder.findScripts()

    public void findScripts(Collection<File> scripts) {
        if (gradleHome == null) {
            return;
        }
        findScriptsInDir(new File(gradleHome, "init.d"), scripts);
    }

- 将`GRADLE_HOME`下的`init.d`文件夹中所有扩展名`.gradle`的文件添加到集合    


### 10.1.4 执行逻辑
**简而言之就是将之前获取到的脚本文件进行解析，并读取其内容中的属性**


    for (File script : initScripts) {
        processor.process(new UriScriptSource("initialization script", script), gradle);
    }

- `initScirpts`就是需要执行的脚本集合,`UriScriptSource `用来解析传入的脚本文件

- `processor`是`DefaultInitScriptProcessor `类型


`DefaultInitScriptProcessor.process()`

	public class DefaultInitScriptProcessor implements InitScriptProcessor {
	    ...
		
	    public void process(final ScriptSource initScript, GradleInternal gradle) {
	        ....
	        configurer.apply(gradle);
	    }
	}

- `configurer`是`DefaultScriptPluginFactory`类的内部类`ScriptPluginImpl`

**`ScriptPluginImpl`的`apply()`方法主要有俩个步骤：**

1. extract plugin requests and plugin repositories and execute buildscript {}, ignoring (i.e. not even compiling) anything else


2. compile everything except buildscript {}, pluginRepositories{}, and plugin requests, then run


## 10.2 NotifyingSettingsLoader.findAndLoadSettings(gradle)

	 // Build `buildSrc`, load settings.gradle, and construct composite (if appropriate)
	settings = settingsLoader.findAndLoadSettings(gradle);
	
- `SettingsLoader settingsLoader`	在`DefaultGradleLauncherFactory`中被创建

### 10.2.1 SettingsLoader的创建

`SettingsLoader`的创建过程在`DefaultGradleLauncherFactory.doNewInstance()`方法中:

    SettingsLoaderFactory settingsLoaderFactory = serviceRegistry.get(SettingsLoaderFactory.class);
    SettingsLoader settingsLoader = parent != null ? settingsLoaderFactory.forNestedBuild() : settingsLoaderFactory.forTopLevelBuild();
    
- 这里的`parent`为空！,因此会调用`settingsLoaderFactory.forTopLevelBuild ()`

### 10.2.2 settingsLoaderFactory.forTopLevelBuild()

    public SettingsLoader forTopLevelBuild() {
        return new NotifyingSettingsLoader(
            new CompositeBuildSettingsLoader(
                new DefaultSettingsLoader(
                    settingsFinder,
                    settingsProcessor,
                    buildSourceBuilder
                ),
                buildServices
            ),
            buildLoader);
    }

- 一层层的调用.. `NotifyingSettingsLoader -> CompositeBuildSettingsLoader -> DefaultSettingsLoader `

- **`DefaultSettingsLoader`主要处理项目目录下的`setting.gradle`文件，同时处理`buildSrc`模块**

#### 10.2.2.1 DefaultSettingsLoader.findAndLoadSettings()


    public SettingsInternal findAndLoadSettings(GradleInternal gradle) {
        StartParameter startParameter = gradle.getStartParameter();
        // 注释1
        SettingsInternal settings = findSettingsAndLoadIfAppropriate(gradle, startParameter);

        ProjectSpec spec = ProjectSpecs.forStartParameter(startParameter, settings);

        if (spec.containsProject(settings.getProjectRegistry())) {
            setDefaultProject(spec, settings);
            return settings;
        }

        // Try again with empty settings
        StartParameter noSearchParameter = startParameter.newInstance();
        noSearchParameter.useEmptySettings();
        settings = findSettingsAndLoadIfAppropriate(gradle, noSearchParameter);

        // Set explicit build file, if required
        if (noSearchParameter.getBuildFile() != null) {
            ProjectDescriptor rootProject = settings.getRootProject();
            rootProject.setBuildFileName(noSearchParameter.getBuildFile().getName());
        }
        setDefaultProject(spec, settings);

        return settings;
    }

- 注释1：

	`findSettingsAndLoadIfAppropriate()`方法实现俩个功能:
	
	1. 获取俩个`gradle.properties`,并读取其中的属性，例如`org.gradle.jvmargs=-Xmx1536m` 。然后通过`addSystemProperties()`将这些属性添加到集合中
	
		1. 项目路径下的`gradle.properties`
	
		2. `gradle_user_home`路径下的`gradle.properties`

	2. 获取项目目录下`settings.gradle`属性

#### 10.2.2.2 NotifyingSettingsLoader

    public SettingsInternal findAndLoadSettings(GradleInternal gradle) {
        SettingsInternal settings = settingsLoader.findAndLoadSettings(gradle);
        // 通知settings配置文件处理完毕
        gradle.getBuildListenerBroadcaster().settingsEvaluated(settings);
        buildLoader.load(settings.getRootProject(), settings.getDefaultProject(), gradle, settings.getRootClassLoaderScope());
        gradle.getBuildListenerBroadcaster().projectsLoaded(gradle);
        return settings;
    }	
    
- `buildLoader.load()`方法中配置了`gradle project`的层级关系，例如项目根目录，以及各个模块




# 11. Configure步骤

	private void doBuildStages(Stage upTo) {
	        ....
	        // 如果只允许构建到Load阶段
			 if (upTo == Stage.Load) {
            	return;
        	 }
	
	        if (stage == Stage.Load) {
	            // Configure build
	            buildOperationExecutor.run("Configure build", new Runnable() {
	                @Override
	                public void run() {
	                    buildConfigurer.configure(gradle);
	
	                    if (!gradle.getStartParameter().isConfigureOnDemand()) {
	                        buildListener.projectsEvaluated(gradle);
	                    }
	
	                    modelConfigurationListener.onConfigure(gradle);
	                }
	            });
	
	            stage = Stage.Configure;
	        }
	        ...
	}

- 这个阶段主要就是加载了配置文件中的各种`Plugin`,它的调用方法是通过加载生成的class文件进行的，这样不同的配置文件可以用不同的类来执行加载动作，而代码保持一致

		D:\gradle_jar_cache\caches\3.1-snapshot-1\scripts-remapped\quality_cjfs2g3ij3bqjjsmf9bhahspf\2n69on6v0v04xd0c8c445muqy\dsld7eae713beda1bd9e69f8461da734880\classes

	

## 11.1 DefaultBuildConfigurer.configure()


    public void configure(GradleInternal gradle) {
        maybeInformAboutIncubatingMode(gradle);
        if (gradle.getStartParameter().isConfigureOnDemand()) {
            projectConfigurer.configure(gradle.getRootProject());
        } else {
            projectConfigurer.configureHierarchy(gradle.getRootProject());
        }
    }

- 前提条件是运行在当前进程中，所以走的是`projectConfigurer.configureHierarchy()`方法

### 11.1.1 TaskPathProjectEvaluator. configureHierarchy()

    public void configureHierarchy(ProjectInternal project) {
        configure(project);
        for (Project sub : project.getSubprojects()) {
            configure((ProjectInternal) sub);
        }
    }
    
	public void configure(ProjectInternal project) {
        if (cancellationToken.isCancellationRequested()) {
            throw new BuildCancelledException();
        }
        project.evaluate();
    }

- 首先执行根项目的`evaluate()`,然后遍历所有的子项目 对其执行`evaluate()`

### 11.1.2 DefaultProject.evaluate()

    public DefaultProject evaluate() {
        getProjectEvaluator().evaluate(this, state);
        return this;
    }

- `getProjectEvaluator()`方法返回一个`LifecycleProjectEvaluator`

### 11.1.3 LifecycleProjectEvaluator.evaluate()

	public void evaluate(final ProjectInternal project, final ProjectStateInternal state) {
        if (state.getExecuted() || state.getExecuting()) {
            return;
        }

        String displayName = "project " + project.getIdentityPath().toString();
        buildOperationExecutor.run(BuildOperationDetails.displayName("Configure " + displayName).name(StringUtils.capitalize(displayName)).build(), new Action<BuildOperationContext>() {
            @Override
            public void execute(BuildOperationContext buildOperationContext) {
                doConfigure(project, state);
                state.rethrowFailure();
            }
        });
    }


    private void doConfigure(ProjectInternal project, ProjectStateInternal state) {
        ProjectEvaluationListener listener = project.getProjectEvaluationBroadcaster();
        try {
            listener.beforeEvaluate(project);
        } catch (Exception e) {
            addConfigurationFailure(project, state, e);
            return;
        }

        state.setExecuting(true);
        try {
            delegate.evaluate(project, state);
        } catch (Exception e) {
            addConfigurationFailure(project, state, e);
        } finally {
            state.setExecuting(false);
            state.executed();
            notifyAfterEvaluate(listener, project, state);
        }
    }

- `delegate`就是`ConfigureActionsProjectEvaluator `    


### 11.1.4 ConfigureActionsProjectEvaluator.evaluate()

    public void evaluate(ProjectInternal project, ProjectStateInternal state) {
        for (ProjectConfigureAction configureAction : configureActions) {
            configureAction.execute(project);
        }
    }

### 11.1.5 BuildScriptProcessor.evaluate()

    public void execute(ProjectInternal project) {
        LOGGER.info("Evaluating {} using {}.", project, project.getBuildScriptSource().getDisplayName());
        final Timer clock = Timers.startTimer();
        try {
            ScriptPlugin configurer = configurerFactory.create(project.getBuildScriptSource(), project.getBuildscript(), project.getClassLoaderScope(), project.getBaseClassLoaderScope(), true);
            configurer.apply(project);
        } finally {
            LOGGER.debug("Timing: Running the build script took {}", clock.getElapsed());
        }
    } 

- `ScriptPlugin`的实际类是`ScriptPluginImpl`,这个类位于`DefaultScriptPluginFactory`类中

- 解析配置文件，加载配置的plugin插件

### 11.1.6 调用堆栈

	at org.gradle.api.internal.plugins.ClassloaderBackedPluginDescriptorLocator.findPluginDescriptor(ClassloaderBackedPluginDescriptorLocator.java:31)
	at org.gradle.api.internal.plugins.DefaultPluginRegistry$1.load(DefaultPluginRegistry.java:59)
	at org.gradle.api.internal.plugins.DefaultPluginRegistry$1.load(DefaultPluginRegistry.java:51)
	at com.google.common.cache.LocalCache$LoadingValueReference.loadFuture(LocalCache.java:3524)
	at com.google.common.cache.LocalCache$Segment.loadSync(LocalCache.java:2317)
	at com.google.common.cache.LocalCache$Segment.lockedGetOrLoad(LocalCache.java:2280)
	at com.google.common.cache.LocalCache$Segment.get(LocalCache.java:2195)
	at com.google.common.cache.LocalCache.get(LocalCache.java:3934)
	at com.google.common.cache.LocalCache.getOrLoad(LocalCache.java:3938)
	at com.google.common.cache.LocalCache$LocalLoadingCache.get(LocalCache.java:4821)
	at org.gradle.api.internal.plugins.DefaultPluginRegistry.uncheckedGet(DefaultPluginRegistry.java:149)
	at org.gradle.api.internal.plugins.DefaultPluginRegistry.lookup(DefaultPluginRegistry.java:138)
	at org.gradle.api.internal.plugins.DefaultPluginRegistry.lookup(DefaultPluginRegistry.java:127)
	at org.gradle.api.internal.plugins.DefaultPluginRegistry.lookup(DefaultPluginRegistry.java:121)
	at org.gradle.api.internal.plugins.DefaultPluginRegistry.lookup(DefaultPluginRegistry.java:121)
	at org.gradle.api.internal.plugins.DefaultPluginRegistry.lookup(DefaultPluginRegistry.java:121)
	at org.gradle.api.internal.plugins.DefaultPluginManager.apply(DefaultPluginManager.java:108)
	at org.gradle.api.internal.plugins.DefaultObjectConfigurationAction.applyType(DefaultObjectConfigurationAction.java:113)
	at org.gradle.api.internal.plugins.DefaultObjectConfigurationAction.access$200(DefaultObjectConfigurationAction.java:36)
	at org.gradle.api.internal.plugins.DefaultObjectConfigurationAction$3.run(DefaultObjectConfigurationAction.java:80)
	at org.gradle.api.internal.plugins.DefaultObjectConfigurationAction.execute(DefaultObjectConfigurationAction.java:136)
	at org.gradle.groovy.scripts.DefaultScript.apply(DefaultScript.java:114)
	at org.gradle.api.Script$apply$0.callCurrent(Unknown Source)
	at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallCurrent(CallSiteArray.java:52)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:154)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:166)
	at quality_cjfs2g3ij3bqjjsmf9bhahspf.run(E:\work_space\Android-Prototype\config\quality.gradle:2)
	
	at org.gradle.groovy.scripts.internal.DefaultScriptRunnerFactory$ScriptRunnerImpl.run(DefaultScriptRunnerFactory.java:90)
	at org.gradle.configuration.DefaultScriptPluginFactory$ScriptPluginImpl$2.run(DefaultScriptPluginFactory.java:176)
	at org.gradle.configuration.DefaultScriptTarget.addConfiguration(DefaultScriptTarget.java:74)
	at org.gradle.configuration.DefaultScriptPluginFactory$ScriptPluginImpl.apply(DefaultScriptPluginFactory.java:181)
	at org.gradle.api.internal.plugins.DefaultObjectConfigurationAction.applyScript(DefaultObjectConfigurationAction.java:102)
	at org.gradle.api.internal.plugins.DefaultObjectConfigurationAction.access$000(DefaultObjectConfigurationAction.java:36)
	at org.gradle.api.internal.plugins.DefaultObjectConfigurationAction$1.run(DefaultObjectConfigurationAction.java:62)
	at org.gradle.api.internal.plugins.DefaultObjectConfigurationAction.execute(DefaultObjectConfigurationAction.java:136)
	at org.gradle.api.internal.project.AbstractPluginAware.apply(AbstractPluginAware.java:44)
	at org.gradle.api.internal.project.ProjectScript.apply(ProjectScript.java:34)
	at org.gradle.api.Script$apply$0.callCurrent(Unknown Source)
	at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallCurrent(CallSiteArray.java:52)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:154)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:166)
	at build_840r8chxz90tc75jy1mlavsji.run(E:\work_space\Android-Prototype\app\build.gradle:3)
	at org.gradle.groovy.scripts.internal.DefaultScriptRunnerFactory$ScriptRunnerImpl.run(DefaultScriptRunnerFactory.java:90)
	at org.gradle.configuration.DefaultScriptPluginFactory$ScriptPluginImpl$2.run(DefaultScriptPluginFactory.java:176)
	at org.gradle.configuration.ProjectScriptTarget.addConfiguration(ProjectScriptTarget.java:77)
	at org.gradle.configuration.DefaultScriptPluginFactory$ScriptPluginImpl.apply(DefaultScriptPluginFactory.java:181)
	at org.gradle.configuration.project.BuildScriptProcessor.execute(BuildScriptProcessor.java:38)
	at org.gradle.configuration.project.BuildScriptProcessor.execute(BuildScriptProcessor.java:25)
	at org.gradle.configuration.project.ConfigureActionsProjectEvaluator.evaluate(ConfigureActionsProjectEvaluator.java:34)
	at org.gradle.configuration.project.LifecycleProjectEvaluator.evaluate(LifecycleProjectEvaluator.java:55)
	at org.gradle.api.internal.project.DefaultProject.evaluate(DefaultProject.java:573)
	at org.gradle.api.internal.project.DefaultProject.evaluate(DefaultProject.java:125)
	at org.gradle.execution.TaskPathProjectEvaluator.configureHierarchy(TaskPathProjectEvaluator.java:47)
	at org.gradle.configuration.DefaultBuildConfigurer.configure(DefaultBuildConfigurer.java:38)
	at org.gradle.initialization.DefaultGradleLauncher$2.run(DefaultGradleLauncher.java:151)
	at org.gradle.internal.Factories$1.create(Factories.java:22)
	at org.gradle.internal.progress.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:91)
	at org.gradle.internal.progress.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:53)
	at org.gradle.initialization.DefaultGradleLauncher.doBuildStages(DefaultGradleLauncher.java:148)
	at org.gradle.initialization.DefaultGradleLauncher.access$200(DefaultGradleLauncher.java:33)
	at org.gradle.initialization.DefaultGradleLauncher$1.create(DefaultGradleLauncher.java:112)
	at org.gradle.initialization.DefaultGradleLauncher$1.create(DefaultGradleLauncher.java:106)
	at org.gradle.internal.progress.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:91)
	at org.gradle.internal.progress.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:63)
	at org.gradle.initialization.DefaultGradleLauncher.doBuild(DefaultGradleLauncher.java:106)
	at org.gradle.initialization.DefaultGradleLauncher.run(DefaultGradleLauncher.java:92)
	at org.gradle.launcher.exec.GradleBuildController.run(GradleBuildController.java:67)
	at org.gradle.tooling.internal.provider.ExecuteBuildActionRunner.run(ExecuteBuildActionRunner.java:31)
	at org.gradle.launcher.exec.ChainingBuildActionRunner.run(ChainingBuildActionRunner.java:43)
	at org.gradle.launcher.exec.InProcessBuildActionExecuter.execute(InProcessBuildActionExecuter.java:42)
	at org.gradle.launcher.exec.InProcessBuildActionExecuter.execute(InProcessBuildActionExecuter.java:26)
	at org.gradle.tooling.internal.provider.ContinuousBuildActionExecuter.execute(ContinuousBuildActionExecuter.java:79)
	at org.gradle.tooling.internal.provider.ContinuousBuildActionExecuter.execute(ContinuousBuildActionExecuter.java:51)
	at org.gradle.launcher.cli.RunBuildAction.run(RunBuildAction.java:54)
	at org.gradle.internal.Actions$RunnableActionAdapter.execute(Actions.java:173)
	at org.gradle.launcher.cli.CommandLineActionFactory$ParseAndBuildAction.execute(CommandLineActionFactory.java:250)
	at org.gradle.launcher.cli.CommandLineActionFactory$ParseAndBuildAction.execute(CommandLineActionFactory.java:217)
	at org.gradle.launcher.cli.JavaRuntimeValidationAction.execute(JavaRuntimeValidationAction.java:33)
	at org.gradle.launcher.cli.JavaRuntimeValidationAction.execute(JavaRuntimeValidationAction.java:24)
	at org.gradle.launcher.cli.ExceptionReportingAction.execute(ExceptionReportingAction.java:33)
	at org.gradle.launcher.cli.ExceptionReportingAction.execute(ExceptionReportingAction.java:22)
	at org.gradle.launcher.cli.CommandLineActionFactory$WithLogging.execute(CommandLineActionFactory.java:210)
	at org.gradle.launcher.cli.CommandLineActionFactory$WithLogging.execute(CommandLineActionFactory.java:174)
	at org.gradle.launcher.Main.doAction(Main.java:33)
	at org.gradle.launcher.bootstrap.EntryPoint.run(EntryPoint.java:45)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:483)
	at org.gradle.launcher.bootstrap.ProcessBootstrap.runNoExit(ProcessBootstrap.java:60)
	at org.gradle.launcher.bootstrap.ProcessBootstrap.run(ProcessBootstrap.java:37)
	at org.gradle.launcher.GradleMain.main(GradleMain.java:24)

### 11.1.7 ClassloaderBackedPluginDescriptorLocator. findPluginDescriptor()

    public PluginDescriptor findPluginDescriptor(String pluginId) {
        URL resource = classLoader.getResource("META-INF/gradle-plugins/" + pluginId + ".properties");
        if (resource == null) {
            return null;
        } else {
            return new PluginDescriptor(resource);
        }
    }

- 这里会去找`META-INF/gradle-plugins/'pluginid'.properties`这个文件    

	以`pmd`插件为例，其配置文件路径就是`subprojects\code-quality\src\main\resources\META-INF\gradle-plugins\org.gradle.pmd.properties`
	
	内容是 `implementation-class=org.gradle.api.plugins.quality.PmdPlugin ` 它表示pmd插件的描述类是`PmdPlugin.java`，需要去加载这个类


# 12. Build步骤 - 查找任务

	private void doBuildStages(Stage upTo) {
		 ..........................
        // After this point, the GradleLauncher cannot be reused
        stage = Stage.Build;
	
        // Populate task graph
        buildOperationExecutor.run("Calculate task graph", new Action<BuildOperationContext>() {
            @Override
            public void execute(BuildOperationContext buildOperationContext) {
                buildConfigurationActionExecuter.select(gradle);
                if (gradle.getStartParameter().isConfigureOnDemand()) {
                    buildListener.projectsEvaluated(gradle);
                }
            }
        });
		......................
	}
	
- 在这个阶段，gradle会计算task入口，选择逻辑如下:

	1. 如果命令中输入了任务名称，比如这样的指令`gradle pmd`，那么就执行pmd这个任务

	2. 如果命令中没有输入任务名称，比如直接输入`gradle`，那么会去查看是否有默认任务

	3. 如果命令中没有输入任务名称，并且没有默认任务，那就执行`help`这个任务


## 12.1 DefaultBuildConfigurationActionExecuter.select()

    public void select(GradleInternal gradle) {
        List<BuildConfigurationAction> processingBuildActions = CollectionUtils.flattenCollections(BuildConfigurationAction.class, configurationActions, taskSelectors);
        configure(processingBuildActions, gradle, 0);
    }
    
    private void configure(final List<BuildConfigurationAction> processingConfigurationActions, final GradleInternal gradle, final int index) {
        if (index >= processingConfigurationActions.size()) {
            return;
        }
        processingConfigurationActions.get(index).configure(new BuildExecutionContext() {
            public GradleInternal getGradle() {
                return gradle;
            }

            public void proceed() {
                configure(processingConfigurationActions, gradle, index + 1);
            }

        });
    }

- `processingConfigurationActions`集合里有三个成员,这三个成员分别处理不同的逻辑

	1. org.gradle.execution.ExcludedTaskFilteringBuildConfigurationAction@3ee68eb2 index: 0
	2. org.gradle.execution.DefaultTasksBuildExecutionAction@49cd08f9 index: 1
	3. org.gradle.execution.TaskNameResolvingBuildConfigurationAction@4eace42b index: 2


### 12.1.1 ExcludedTaskFilteringBuildConfigurationAction.configure()

    public void configure(BuildExecutionContext context) {
        GradleInternal gradle = context.getGradle();
        Set<String> excludedTaskNames = gradle.getStartParameter().getExcludedTaskNames();
        if (!excludedTaskNames.isEmpty()) {
            final Set<Spec<Task>> filters = new HashSet<Spec<Task>>();
            for (String taskName : excludedTaskNames) {
                filters.add(taskSelector.getFilter(taskName));
            }
            gradle.getTaskGraph().useFilter(Specs.intersect(filters));
        }

        context.proceed();
    }

- 如果用户设置了过滤指定任务，那么就会过滤掉不执行的任务

### 12.1.2 DefaultTasksBuildExecutionAction.configure()

    public void configure(BuildExecutionContext context) {
        StartParameter startParameter = context.getGradle().getStartParameter();
		 // 判断是否有输入任务名称
        for (TaskExecutionRequest request : startParameter.getTaskRequests()) {
            if (!request.getArgs().isEmpty()) {
                // 执行输入的任务
                context.proceed();
                return;
            }
        }

        // Gather the default tasks from this first group project
        ProjectInternal project = context.getGradle().getDefaultProject();

        //so that we don't miss out default tasks
        projectConfigurer.configure(project);
		 // 查询是否存在默认任务
        List<String> defaultTasks = project.getDefaultTasks();
        if (defaultTasks.size() == 0) {
        // 不存在默认任务，执行help任务
            defaultTasks = Collections.singletonList(ProjectInternal.HELP_TASK);
            LOGGER.info("No tasks specified. Using default task {}", GUtil.toString(defaultTasks));
        } else {
            LOGGER.info("No tasks specified. Using project default tasks {}", GUtil.toString(defaultTasks));
        }

        startParameter.setTaskNames(defaultTasks);
        context.proceed();
    }

- 逻辑如下:

	1. 指定了待执行的任务，执行该任务

	2. 未指定任务，使用默认任务，

	3. 不存在默认任务，执行help任务

### 12.1.3 TaskNameResolvingBuildConfigurationAction.configure()

    public void configure(BuildExecutionContext context) {
        GradleInternal gradle = context.getGradle();
        TaskGraphExecuter executer = gradle.getTaskGraph();

        List<TaskExecutionRequest> taskParameters = gradle.getStartParameter().getTaskRequests();
        for (TaskExecutionRequest taskParameter : taskParameters) {
            List<TaskSelector.TaskSelection> taskSelections = commandLineTaskParser.parseTasks(taskParameter);
            for (TaskSelector.TaskSelection taskSelection : taskSelections) {
                LOGGER.info("Selected primary task '{}' from project {}", taskSelection.getTaskName(), taskSelection.getProjectPath());
                executer.addTasks(taskSelection.getTasks());
            }
        }

        context.proceed();
    }

- 把任务添加到`TaskGraphExecuter `中,例如命令中指定的任务，默认任务，help任务...


# 13. Build步骤 - 执行任务

	private void doBuildStages(Stage upTo) {
			....................
        // Execute build
        buildOperationExecutor.run("Run tasks", new Action<BuildOperationContext>() {
            @Override
            public void execute(BuildOperationContext buildOperationContext) {
                buildExecuter.execute(gradle);
            }
        });
    }


## 13.1 DefaultBuildExecuter.execute()

    public void execute(GradleInternal gradle) {
        execute(gradle, 0);
    }

    private void execute(final GradleInternal gradle, final int index) {
        if (index >= executionActions.size()) {
            return;
        }
        executionActions.get(index).execute(new BuildExecutionContext() {
            public GradleInternal getGradle() {
                return gradle;
            }

            public void proceed() {
                execute(gradle, index + 1);
            }

        });
    }	

- 递归执行集合中的`Action`

- executionActions包含两个action

	1. org.gradle.execution.DryRunBuildExecutionAction@1e4d93f7 index: 0

	2. org.gradle.execution.SelectedTaskExecutionAction@76673ed index: 1    

### 13.1.1 DryRunBuildExecutionAction.execute()

	public void execute(BuildExecutionContext context) {
        GradleInternal gradle = context.getGradle();
        if (gradle.getStartParameter().isDryRun()) {
            for (Task task : gradle.getTaskGraph().getAllTasks()) {
                task.setEnabled(false);
            }
        }
        context.proceed();
    }

- 通过`setEnabled(false)`禁止所有的任务执行,即跳过任务执行

- 命令选项中添加`--dry-run`就会跳过任务执行过程

### 13.1.2 SelectedTaskExecutionAction.execute()

    public void execute(BuildExecutionContext context) {
        GradleInternal gradle = context.getGradle();
        TaskGraphExecuter taskGraph = gradle.getTaskGraph();
        if (gradle.getStartParameter().isContinueOnFailure()) {
            taskGraph.useFailureHandler(new ContinueOnFailureHandler());
        }

        taskGraph.addTaskExecutionGraphListener(new BindAllReferencesOfProjectsToExecuteListener());
        taskGraph.execute();
    }

#### 13.1.2.1 DefaultTaskGraphExecuter.execute()

    public void execute() {
        Timer clock = Timers.startTimer();
        ensurePopulated();

        graphListeners.getSource().graphPopulated(this);
        try {
            taskPlanExecutor.process(taskExecutionPlan, new EventFiringTaskWorker(taskExecuter.create(), buildOperationExecutor.getCurrentOperation()));
            LOGGER.debug("Timing: Executing the DAG took " + clock.getElapsed());
        } finally {
            taskExecutionPlan.clear();
        }
    }

#### 13.1.2.2 DefaultTaskPlanExecutor.process()

    public void process(TaskExecutionPlan taskExecutionPlan, Action<? super TaskInternal> taskWorker) {
        taskWorker(taskExecutionPlan, taskWorker, buildOperationWorkerRegistry).run();
        taskExecutionPlan.awaitCompletion();
    }

-  `taskWorker `是`AbstractTaskPlanExecutor`中的内部类

#### 13.1.2.3 AbstractTaskPlanExecutor.TaskExecutorWorker.run()

	private static class TaskExecutorWorker implements Runnable {
	    ...
	
	    public void run() {
	        ...
	        while ((task = taskExecutionPlan.getTaskToExecute()) != null) {
	            BuildOperationWorkerRegistry.Completion completion = buildOperationWorkerRegistry.operationStart();
	            try {
	                ...
	                processTask(task);
	                ...
	            } finally {
	                completion.operationFinish();
	            }
	        }
	        ...
	    }
	
	    protected void processTask(TaskInfo taskInfo) {
	        ...
	        taskWorker.execute(taskInfo.getTask());
	        ...
	    }
	}

#### 13.1.2.4 DefaultTaskGraphExecuter.EventFiringTaskWorker.execute()

    public void execute(final TaskInternal task) {
        TaskOperationDescriptor taskOperation = new TaskOperationDescriptor(task);
        BuildOperationDetails buildOperationDetails = BuildOperationDetails.displayName("Task " + task.getIdentityPath()).name(task.getIdentityPath().toString()).parent(parentOperation).operationDescriptor(taskOperation).build();
        buildOperationExecutor.run(buildOperationDetails, new Action<BuildOperationContext>() {
            @Override
            public void execute(BuildOperationContext buildOperationContext) {
                // These events are used by build scans
                TaskOperationInternal legacyOperation = new TaskOperationInternal(task, buildOperationExecutor.getCurrentOperation().getId());
                internalTaskListener.beforeExecute(legacyOperation, new OperationStartEvent(0));
                TaskStateInternal state = task.getState();
                taskListeners.getSource().beforeExecute(task);
                taskExecuter.execute(task, state, new DefaultTaskExecutionContext());
                taskListeners.getSource().afterExecute(task, state);
                buildOperationContext.failed(state.getFailure());
                internalTaskListener.afterExecute(legacyOperation, new OperationResult(0, 0, state.getFailure()));
            }
        });
    }

- 这里的`taskExecuter.execute()`又使用了装饰者模式。。嵌套了一大堆。。。。

## 13.2 TaskExecutionServices

    TaskExecuter createTaskExecuter(TaskArtifactStateRepository repository, TaskOutputPacker packer, StartParameter startParameter, ListenerManager listenerManager, GradleInternal gradle, TaskOutputOriginFactory taskOutputOriginFactory) {
        // TODO - need a more comprehensible way to only collect inputs for the outer build
        //      - we are trying to ignore buildSrc here, but also avoid weirdness with use of GradleBuild tasks
        boolean isOuterBuild = gradle.getParent() == null;
        TaskInputsListener taskInputsListener = isOuterBuild
            ? listenerManager.getBroadcaster(TaskInputsListener.class)
            : TaskInputsListener.NOOP;

        TaskOutputsGenerationListener taskOutputsGenerationListener = listenerManager.getBroadcaster(TaskOutputsGenerationListener.class);
        return new CatchExceptionTaskExecuter(
            new ExecuteAtMostOnceTaskExecuter(
                new SkipOnlyIfTaskExecuter(
                    new SkipTaskWithNoActionsExecuter(
                        new ResolveTaskArtifactStateTaskExecuter(
                            repository,
                            new SkipEmptySourceFilesTaskExecuter(
                                taskInputsListener,
                                new ValidatingTaskExecuter(
                                    new SkipUpToDateTaskExecuter(
                                        createSkipCachedExecuterIfNecessary(
                                            startParameter,
                                            gradle.getBuildCache(),
                                            packer,
                                            taskOutputsGenerationListener,
                                            taskOutputOriginFactory,
                                            createVerifyNoInputChangesExecuterIfNecessary(
                                                startParameter,
                                                repository,
                                                new ExecuteActionsTaskExecuter(
                                                    taskOutputsGenerationListener,
                                                    listenerManager.getBroadcaster(TaskActionListener.class)
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
    }
    

最后会调用到ExecuteActionsTaskExecuter里面。

- ExecuteAtMostOnceTaskExecuter：检查是否已经执行过

- SkipOnlyIfTaskExecuter:检查是否是skip(这个估计是个属性配置，暂时还没有找到在哪里配)

- SkipTaskWithNoActionsExecuter：检查是否有action,没有则返回

- SkipEmptySourceFilesTaskExecuter：检查是否有source file    


## 13.3 ExecuteActionsTaskExecuter.execute()

    public void execute(TaskInternal task, TaskStateInternal state, TaskExecutionContext context) {
        listener.beforeActions(task);
        if (!task.getTaskActions().isEmpty()) {
            outputsGenerationListener.beforeTaskOutputsGenerated();
        }
        state.setExecuting(true);
        try {
            GradleException failure = executeActions(task, state, context);
            if (failure != null) {
                state.setOutcome(failure);
            } else {
                state.setOutcome(
                    state.getDidWork() ? TaskExecutionOutcome.EXECUTED : TaskExecutionOutcome.UP_TO_DATE
                );
            }
        } finally {
            state.setExecuting(false);
            listener.afterActions(task);
        }
    }

    private GradleException executeActions(TaskInternal task, TaskStateInternal state, TaskExecutionContext context) {
        LOGGER.debug("Executing actions for {}.", task);
        final List<ContextAwareTaskAction> actions = new ArrayList<ContextAwareTaskAction>(task.getTaskActions());
        for (ContextAwareTaskAction action : actions) {
            state.setDidWork(true);
            task.getStandardOutputCapture().start();
            try {
                executeAction(task, action, context);
            } catch (StopActionException e) {
                // Ignore
                LOGGER.debug("Action stopped by some action with message: {}", e.getMessage());
            } catch (StopExecutionException e) {
                LOGGER.info("Execution stopped by some action with message: {}", e.getMessage());
                break;
            } catch (Throwable t) {
                return new TaskExecutionException(task, t);
            } finally {
                task.getStandardOutputCapture().stop();
            }
        }
        return null;
    }

    private void executeAction(TaskInternal task, ContextAwareTaskAction action, TaskExecutionContext context) {
        action.contextualise(context);
        try {
            action.execute(task);
        } finally {
            action.contextualise(null);
        }
    }

- 执行任务对应的action



    
