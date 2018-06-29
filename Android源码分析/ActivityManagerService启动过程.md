# AMS启动过程
[ActivityManagerService启动过程](http://gityuan.com/2016/02/21/activity-manager-service/)
# 1. 简介

`AMS`在`system_server`进程中被创建启动.具体在`SystemServer`类的`startBootStrapServices(),startCoreServices(),startOtherServices()`


**本文基于Android 27**
# 2. 启动流程

## 2.1 startBootstrapServices()

    private void startBootstrapServices() {

        mActivityManagerService = mSystemServiceManager.startService(ActivityManagerService.Lifecycle.class).getService();
		//设置AMS的系统服务管理器
        mActivityManagerService.setSystemServiceManager(mSystemServiceManager);
		//设置AMS的App安装器
        mActivityManagerService.setInstaller(installer);
        // 设置AMS的电源管理
        mActivityManagerService.initPowerManagement();
        // Set up the Application instance for the system process and get started.
		//设置SystemServer
        mActivityManagerService.setSystemProcess();

	}

## 2.2 启动AMS服务

`SystemServiceManager.startService(ActivityManagerService.Lifecycle.class)` 功能主要：

1. 创建`ActivityManagerService.Lifecycle`对象
2. 在`startService()`中会调用`Lifecycle.onStart()`即调用`AMS.onStart()`

### 2.2.1 ActivityManagerService.Lifecycle

    public static final class Lifecycle extends SystemService {
        private final ActivityManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            mService = new ActivityManagerService(context);
        }

        @Override
        public void onStart() {
            mService.start();
        }

        @Override
        public void onCleanupUser(int userId) {
            mService.mBatteryStatsService.onCleanupUser(userId);
        }

        public ActivityManagerService getService() {
            return mService;
        }
    }

- `Lifecycle`是`ActivityManagerService`的内部类

- `Lifecycle`内部有一个`AMS`的成员变量,会在其构造函数中被创建

### 2.2.2 AMS构造函数

	// Note: This method is invoked on the main thread but may need to attach various
    // handlers to other threads.  So take care to be explicit about the looper.
    public ActivityManagerService(Context systemContext) {
        LockGuard.installLock(this, LockGuard.INDEX_ACTIVITY);
        mInjector = new Injector();
        mContext = systemContext;

        mFactoryTest = FactoryTest.getMode();
        mSystemThread = ActivityThread.currentActivityThread();
        mUiContext = mSystemThread.getSystemUiContext();

        Slog.i(TAG, "Memory class: " + ActivityManager.staticGetMemoryClass());

        mPermissionReviewRequired = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_permissionReviewRequired);

        mHandlerThread = new ServiceThread(TAG,
                THREAD_PRIORITY_FOREGROUND, false /*allowIo*/);
        mHandlerThread.start();
        mHandler = new MainHandler(mHandlerThread.getLooper());
        mUiHandler = mInjector.getUiHandler(this);

        mConstants = new ActivityManagerConstants(this, mHandler);

        /* static; one-time init here */
        if (sKillHandler == null) {
            sKillThread = new ServiceThread(TAG + ":kill",
                    THREAD_PRIORITY_BACKGROUND, true /* allowIo */);
            sKillThread.start();
            sKillHandler = new KillHandler(sKillThread.getLooper());
        }

        mFgBroadcastQueue = new BroadcastQueue(this, mHandler,
                "foreground", BROADCAST_FG_TIMEOUT, false);
        mBgBroadcastQueue = new BroadcastQueue(this, mHandler,
                "background", BROADCAST_BG_TIMEOUT, true);
        mBroadcastQueues[0] = mFgBroadcastQueue;
        mBroadcastQueues[1] = mBgBroadcastQueue;

        mServices = new ActiveServices(this);
        mProviderMap = new ProviderMap(this);
        mAppErrors = new AppErrors(mUiContext, this);

        // TODO: Move creation of battery stats service outside of activity manager service.
        File dataDir = Environment.getDataDirectory();
        File systemDir = new File(dataDir, "system");
        systemDir.mkdirs();
        mBatteryStatsService = new BatteryStatsService(systemContext, systemDir, mHandler);
        mBatteryStatsService.getActiveStatistics().readLocked();
        mBatteryStatsService.scheduleWriteToDisk();
        mOnBattery = DEBUG_POWER ? true
                : mBatteryStatsService.getActiveStatistics().getIsOnBattery();
        mBatteryStatsService.getActiveStatistics().setCallback(this);

        mProcessStats = new ProcessStatsService(this, new File(systemDir, "procstats"));

        mAppOpsService = mInjector.getAppOpsService(new File(systemDir, "appops.xml"), mHandler);
        mAppOpsService.startWatchingMode(AppOpsManager.OP_RUN_IN_BACKGROUND, null,
                new IAppOpsCallback.Stub() {
                    @Override public void opChanged(int op, int uid, String packageName) {
                        if (op == AppOpsManager.OP_RUN_IN_BACKGROUND && packageName != null) {
                            if (mAppOpsService.checkOperation(op, uid, packageName)
                                    != AppOpsManager.MODE_ALLOWED) {
                                runInBackgroundDisabled(uid);
                            }
                        }
                    }
                });

        mGrantFile = new AtomicFile(new File(systemDir, "urigrants.xml"));

        mUserController = new UserController(this);

        mVrController = new VrController(this);

        GL_ES_VERSION = SystemProperties.getInt("ro.opengles.version",
            ConfigurationInfo.GL_ES_VERSION_UNDEFINED);

        if (SystemProperties.getInt("sys.use_fifo_ui", 0) != 0) {
            mUseFifoUiScheduling = true;
        }

        mTrackingAssociations = "1".equals(SystemProperties.get("debug.track-associations"));
        mTempConfig.setToDefaults();
        mTempConfig.setLocales(LocaleList.getDefault());
        mConfigurationSeq = mTempConfig.seq = 1;
        mStackSupervisor = createStackSupervisor();
        mStackSupervisor.onConfigurationChanged(mTempConfig);
        mKeyguardController = mStackSupervisor.mKeyguardController;
        mCompatModePackages = new CompatModePackages(this, systemDir, mHandler);
        mIntentFirewall = new IntentFirewall(new IntentFirewallInterface(), mHandler);
        mTaskChangeNotificationController =
                new TaskChangeNotificationController(this, mStackSupervisor, mHandler);
        mActivityStarter = new ActivityStarter(this, mStackSupervisor);
        mRecentTasks = new RecentTasks(this, mStackSupervisor);

        mProcessCpuThread = new Thread("CpuTracker") {
            @Override
            public void run() {
                synchronized (mProcessCpuTracker) {
                    mProcessCpuInitLatch.countDown();
                    mProcessCpuTracker.init();
                }
                while (true) {
                    try {
                        try {
                            synchronized(this) {
                                final long now = SystemClock.uptimeMillis();
                                long nextCpuDelay = (mLastCpuTime.get()+MONITOR_CPU_MAX_TIME)-now;
                                long nextWriteDelay = (mLastWriteTime+BATTERY_STATS_TIME)-now;
                                //Slog.i(TAG, "Cpu delay=" + nextCpuDelay
                                //        + ", write delay=" + nextWriteDelay);
                                if (nextWriteDelay < nextCpuDelay) {
                                    nextCpuDelay = nextWriteDelay;
                                }
                                if (nextCpuDelay > 0) {
                                    mProcessCpuMutexFree.set(true);
                                    this.wait(nextCpuDelay);
                                }
                            }
                        } catch (InterruptedException e) {
                        }
                        updateCpuStatsNow();
                    } catch (Exception e) {
                        Slog.e(TAG, "Unexpected exception collecting process stats", e);
                    }
                }
            }
        };

        Watchdog.getInstance().addMonitor(this);
        Watchdog.getInstance().addThread(mHandler);
    }