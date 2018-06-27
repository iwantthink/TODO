# SystemServer分析

[Android系统启动-综述](http://gityuan.com/2016/02/01/android-booting/)

[Android源码分析-GitYuan-目录](http://gityuan.com/android/)

[Android系统启动-Systemserver](http://gityuan.com/2016/02/14/android-system-server/)


# 1. 简介

![](http://gityuan.com/images/process/android-booting.jpg)

Linux系统中 用户空间的第一个进程是`init`进程(Kernel启动后,会创建`init`进程). 然后`init`进程会启动`ServiceManager`(Binder服务管家),`Zygote`进程(Java进程的鼻祖,通过fork它会创建`system_server`进程等各种app进程)


SystemServer进程由`Zygote fork`生成,**进程名为`system_server`**,该进程承载着framework的核心服务

- `Zygote`启动过程中 会调用`startSystemServer()`,该方法即`system_server`的起点


**本文基于Android 27!!**

# 2. 启动流程

![](http://gityuan.com/images/boot/systemServer/system_server.jpg)

暂时不会分析`SystemServer.main()`之前的逻辑

# 3. SystemServer

## 3.1 main()

    /**
     * The main entry point from zygote.
     */
    public static void main(String[] args) {
        new SystemServer().run();
    }

## 3.2 run()

    private void run() {
        try {
            traceBeginAndSlog("InitBeforeStartServices");
            //当系统时间比1970年更早，就设置当前系统时间为1970年
            if (System.currentTimeMillis() < EARLIEST_SUPPORTED_TIME) {
                Slog.w(TAG, "System clock is before 1970; setting to 1970.");
                SystemClock.setCurrentTimeMillis(EARLIEST_SUPPORTED_TIME);
            }

            // 设置默认timezone property 为 GMT
            String timezoneProperty =  SystemProperties.get("persist.sys.timezone");
            if (timezoneProperty == null || timezoneProperty.isEmpty()) {
                Slog.w(TAG, "Timezone not set; setting to GMT.");
                SystemProperties.set("persist.sys.timezone", "GMT");
            }
			``省略代码``

			//清除vm内存增长上限，由于启动过程需要较多的虚拟机内存空间
            VMRuntime.getRuntime().clearGrowthLimit();

            //设置内存的可能有效使用率为0.8
            VMRuntime.getRuntime().setTargetHeapUtilization(0.8f);

            // 针对部分设备依赖于运行时就产生指纹信息，因此需要在开机完成前已经定义
            Build.ensureFingerprintProperty();

            // 访问环境变量前，需要明确地指定用户
            Environment.setUserRequired(true);

            // 确保当前系统进程的binder调用，总是运行在前台优先级
            BinderInternal.disableBackgroundScheduling(true);

            // 设置binder线程的数量
            BinderInternal.setMaxThreads(sMaxBinderThreads);

            // 设置优先级
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
            android.os.Process.setCanSelfBackground(false);
			//开启Looper循环,主线程的Looper就在当前线程运行
            Looper.prepareMainLooper();

            // 加载android_servers.so库，该库包含的源码在frameworks/base/services/目录下
            System.loadLibrary("android_servers");

            //检测上次关机过程是否失败，该方法可能不会返回
            performPendingShutdown();

            // 初始化系统上下文
            createSystemContext();

            // 创建系统服务管理
            mSystemServiceManager = new SystemServiceManager(mSystemContext);
            mSystemServiceManager.setRuntimeRestarted(mRuntimeRestart);

			//将mSystemServiceManager添加到本地服务的成员sLocalServiceObjects
            LocalServices.addService(SystemServiceManager.class, mSystemServiceManager);

            // Prepare the thread pool for init tasks that can be parallelized
            SystemServerInitThreadPool.get();
        } finally {
            traceEnd();  // InitBeforeStartServices
        }

        // Start services.
        try {
            traceBeginAndSlog("StartServices");
			//启动引导服务
            startBootstrapServices();
			//启动核心服务
            startCoreServices();
			//启动其他服务
            startOtherServices();
            SystemServerInitThreadPool.shutdown();
        } catch (Throwable ex) {
            throw ex;
        } finally {
            traceEnd();
        }

        // 用于debug版本，将log事件不断循环地输出到dropbox（用于分析）
        if (StrictMode.conditionallyEnableDebugLogging()) {
            Slog.i(TAG, "Enabled StrictMode for system server main thread.");
        }
  		
        // Loop forever.
        Looper.loop();
        throw new RuntimeException("Main thread loop unexpectedly exited");
    }

- `LocalServices`通过用静态Map变量sLocalServiceObjects，来保存以服务类名为key，以具体服务对象为value的Map结构。

### 3.2.1 createSystemService()
	
    private void createSystemContext() {
        ActivityThread activityThread = ActivityThread.systemMain();
        mSystemContext = activityThread.getSystemContext();
        mSystemContext.setTheme(DEFAULT_SYSTEM_THEME);

        final Context systemUiContext = activityThread.getSystemUiContext();
        systemUiContext.setTheme(DEFAULT_SYSTEM_THEME);
    }

- 通过`ActivityThread`去创建`Context`.**这里有俩个`Context`被创建**

### 3.2.2 startBootstrapServices

    /**
     * Starts the small tangle of critical services that are needed to get
     * the system off the ground.  These services have complex mutual dependencies
     * which is why we initialize them all in one place here.  Unless your service
     * is also entwined in these dependencies, it should be initialized in one of
     * the other functions.
     */
    private void startBootstrapServices() {

		``省略日志打印的代码``
		//阻塞等待与installd建立socket通道
        Installer installer = mSystemServiceManager.startService(Installer.class);

        //启动DeviceIdentifiersPolicyService
        mSystemServiceManager.startService(DeviceIdentifiersPolicyService.class);

		//启动AMS
        mActivityManagerService = mSystemServiceManager.startService(ActivityManagerService.Lifecycle.class).getService();
        mActivityManagerService.setSystemServiceManager(mSystemServiceManager);
        mActivityManagerService.setInstaller(installer);

		//启动PowerManagerService
        mPowerManagerService = mSystemServiceManager.startService(PowerManagerService.class);

		//通过AMS初始化PM
        mActivityManagerService.initPowerManagement();

        // 启动RecoverySystemService
        if (!SystemProperties.getBoolean("config.disable_noncore", false)) {
            traceBeginAndSlog("StartRecoverySystemService");
            mSystemServiceManager.startService(RecoverySystemService.class);
            traceEnd();
        }
        RescueParty.noteBoot(mSystemContext);

        //启动LightsService
        mSystemServiceManager.startService(LightsService.class);

        // 启动DisplayManagerService
        mDisplayManagerService = mSystemServiceManager.startService(DisplayManagerService.class);

        // 在初始化package manager之前，需要默认的显示.
        mSystemServiceManager.startBootPhase(SystemService.PHASE_WAIT_FOR_DEFAULT_DISPLAY);


        // 当设备正在加密时,仅运行`核心`
        String cryptState = SystemProperties.get("vold.decrypt");
        if (ENCRYPTING_STATE.equals(cryptState)) {
            Slog.w(TAG, "Detected encryption in progress - only parsing core apps");
            mOnlyCore = true;
        } else if (ENCRYPTED_STATE.equals(cryptState)) {
            Slog.w(TAG, "Device encrypted - only parsing core apps");
            mOnlyCore = true;
        }

        // 启动PackageManagerService
        if (!mRuntimeRestart) {
            MetricsLogger.histogram(null, "boot_package_manager_init_start",
                    (int) SystemClock.elapsedRealtime());
        }
        mPackageManagerService = PackageManagerService.main(mSystemContext, installer,
                mFactoryTestMode != FactoryTest.FACTORY_TEST_OFF, mOnlyCore);
        mFirstBoot = mPackageManagerService.isFirstBoot();
        mPackageManager = mSystemContext.getPackageManager();

        if (!mRuntimeRestart && !isFirstBootOrUpgrade()) {
            MetricsLogger.histogram(null, "boot_package_manager_init_ready",
                    (int) SystemClock.elapsedRealtime());
        }

		//启动OtaDexOptService
        if (!mOnlyCore) {
            boolean disableOtaDexopt = SystemProperties.getBoolean("config.disable_otadexopt",
                    false);
            if (!disableOtaDexopt) {

                try {
                    OtaDexoptService.main(mSystemContext, mPackageManagerService);
                } catch (Throwable e) {
                    reportWtf("starting OtaDexOptService", e);
                } finally {
                }
            }
        }

		//启动UserManagerService
        mSystemServiceManager.startService(UserManagerService.LifeCycle.class);
        traceEnd();

        // Initialize attribute cache used to cache resources from packages.
        AttributeCache.init(mSystemContext);

		//为system_server进程设置并开启
        mActivityManagerService.setSystemProcess();

		//设置
        mDisplayManagerService.setupSchedulerPolicies();

		//启动OverlayManagerService
        mSystemServiceManager.startService(new OverlayManagerService(mSystemContext, installer));

        //启动传感器服务,其需要pms,app ops service,permissions service 的支持,因此最后才去启动
		//传感器服务在单独的线程中
        mSensorServiceStart = SystemServerInitThreadPool.get().submit(() -> {
            TimingsTraceLog traceLog = new TimingsTraceLog(
                    SYSTEM_SERVER_TIMING_ASYNC_TAG, Trace.TRACE_TAG_SYSTEM_SERVER);
            traceLog.traceBegin(START_SENSOR_SERVICE);
            startSensorService();
            traceLog.traceEnd();
        }, START_SENSOR_SERVICE);
    }

- 总结一下:该方法中启动了许多服务,因为这些服务相互依赖,所以统一在这里初始化

- 上述的代码中移除了日志相关的代码

### 3.2.3 startCoreServices

    private void startCoreServices() {
        // Records errors and logs, for example wtf()
        mSystemServiceManager.startService(DropBoxManagerService.class);

        // 启动BatteryService,用于记录电池电量.需要LightService.
        mSystemServiceManager.startService(BatteryService.class);

        // 启动UsageStatsService,用于统计应用使用情况
        mSystemServiceManager.startService(UsageStatsService.class);
        mActivityManagerService.setUsageStatsManager(
                LocalServices.getService(UsageStatsManagerInternal.class));

        // 启动WebViewUpdateService
        mWebViewUpdateService = mSystemServiceManager.startService(WebViewUpdateService.class);
    }

- 启动一些服务,这些服务不会跟bootstrap进程纠缠

### 3.2.4 startOtherServices

启动各种未被重构和组织的服务
