# AMS启动过程
[ActivityManagerService启动过程](http://gityuan.com/2016/02/21/activity-manager-service/)
# 1. 简介

`AMS`在`system_server`进程中被创建启动.

具体位于`SystemServer`类的`startBootStrapServices(),startCoreServices(),startOtherServices()`三个方法中


**本文基于Android 27**
# 2. 启动流程-startBootstrapServices

`SystemServer.startBootstrapServices()`

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

## 2.1 SystemServiceManager.startService()

`SystemServiceManager.startService(ActivityManagerService.Lifecycle.class)` 功能主要：

1. 创建`ActivityManagerService.Lifecycle`对象

2. **在`startService()`中会调用`Lifecycle.onStart()`即调用`AMS.onStart()`**

### 2.1.1 ActivityManagerService.Lifecycle

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

- `SystemServiceManager`的创建过程可以参考[SystemServer分析.md](https://github.com/iwantthink/TODO/blob/master/Android%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/ActivityManagerService%E5%90%AF%E5%8A%A8%E8%BF%87%E7%A8%8B.md)

### 2.1.2 AMS构造函数

	// Note: This method is invoked on the main thread but may need to attach various
    // handlers to other threads.  So take care to be explicit about the looper.
    public ActivityManagerService(Context systemContext) {
        LockGuard.installLock(this, LockGuard.INDEX_ACTIVITY);
        mInjector = new Injector();
        mContext = systemContext;

        mFactoryTest = FactoryTest.getMode();//默认是FACTORY_TEST_OFF
		//获取当前进程的主线程
        mSystemThread = ActivityThread.currentActivityThread();
        mUiContext = mSystemThread.getSystemUiContext();

        mPermissionReviewRequired = mContext.getResources().getBoolean(com.android.internal.R.bool.config_permissionReviewRequired);
		//创建一个HandlerThread,并开启循环
        mHandlerThread = new ServiceThread(TAG,
                THREAD_PRIORITY_FOREGROUND, false /*allowIo*/);
        mHandlerThread.start();
		//创建一个工作Handler
        mHandler = new MainHandler(mHandlerThread.getLooper());
		//创建一个Handler用于与UI交互
        mUiHandler = mInjector.getUiHandler(this);

        mConstants = new ActivityManagerConstants(this, mHandler);

        /* static; one-time init here */
        if (sKillHandler == null) {
            sKillThread = new ServiceThread(TAG + ":kill",
                    THREAD_PRIORITY_BACKGROUND, true /* allowIo */);
            sKillThread.start();
            sKillHandler = new KillHandler(sKillThread.getLooper());
        }
		//前台广播接收者.在运行超过10s后将放弃执行
        mFgBroadcastQueue = new BroadcastQueue(this, mHandler,
                "foreground", BROADCAST_FG_TIMEOUT, false);
		//后台广播接收者,在运行超过60s后将放弃执行
        mBgBroadcastQueue = new BroadcastQueue(this, mHandler,
                "background", BROADCAST_BG_TIMEOUT, true);
        mBroadcastQueues[0] = mFgBroadcastQueue;
        mBroadcastQueues[1] = mBgBroadcastQueue;
		//创建ActivityService
        mServices = new ActiveServices(this);
        mProviderMap = new ProviderMap(this);
        mAppErrors = new AppErrors(mUiContext, this);

        // TODO: Move creation of battery stats service outside of activity manager service.
		//创建目录 /data/system
        File dataDir = Environment.getDataDirectory();
        File systemDir = new File(dataDir, "system");
        systemDir.mkdirs();

		//创建BatteryStatsService服务
        mBatteryStatsService = new BatteryStatsService(systemContext, systemDir, mHandler);
        mBatteryStatsService.getActiveStatistics().readLocked();
        mBatteryStatsService.scheduleWriteToDisk();
        mOnBattery = DEBUG_POWER ? true
                : mBatteryStatsService.getActiveStatistics().getIsOnBattery();
        mBatteryStatsService.getActiveStatistics().setCallback(this);

		//创建进程统计服务,信息保存在目录 /data/system/procstats
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

		//创建名为 CPUTracker的线程
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
						//更新CPU状态
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

- `HandlerThread`是一个创建了Looper的Thread,允许多个任务串行执行

- 创建了三个Thread

	1. 直接创建的`CpuTracker`

	2. 通过`ServiceThread`创建的`ActivityManager`线程

	3. 通过`UiThread`类创建的`android.ui`线程

### 2.1.3 AMS.start()

    private void start() {
		//移除所有的进程租
        removeAllProcessGroups();
		//启动CpuTracker线程
        mProcessCpuThread.start();
		//启动电池统计服务
        mBatteryStatsService.publish();
        mAppOpsService.publish(mContext);
		//创建LocalService 并添加到LocalServices
		//LocalService即ActivityManagerInternal的子类
        LocalServices.addService(ActivityManagerInternal.class, new LocalService());
        // Wait for the synchronized block started in mProcessCpuThread,
        // so that any other acccess to mProcessCpuTracker from main thread
        // will be blocked during mProcessCpuTracker initialization.
        try {
            mProcessCpuInitLatch.await();
        } catch (InterruptedException e) {
            Slog.wtf(TAG, "Interrupted wait during start", e);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted wait during start");
        }
    }

## 2.2 AMS.setSystemProcess

    public void setSystemProcess() {
        try {
			//注册一系列服务到ServbiceManager中
            ServiceManager.addService(Context.ACTIVITY_SERVICE, this, true);
            ServiceManager.addService(ProcessStats.SERVICE_NAME, mProcessStats);
            ServiceManager.addService("meminfo", new MemBinder(this));
            ServiceManager.addService("gfxinfo", new GraphicsBinder(this));
            ServiceManager.addService("dbinfo", new DbBinder(this));
            if (MONITOR_CPU_USAGE) {
                ServiceManager.addService("cpuinfo", new CpuBinder(this));
            }
            ServiceManager.addService("permission", new PermissionController(this));
            ServiceManager.addService("processinfo", new ProcessInfoService(this));
			//获取ApplicationInfo
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(
                    "android", STOCK_PM_FLAGS | MATCH_SYSTEM_ONLY);	
			//查看2.2.1
            mSystemThread.installSystemApplicationInfo(info, getClass().getClassLoader());

            synchronized (this) {
				//创建ProcessRecord对象
                ProcessRecord app = newProcessRecordLocked(info, info.processName, false, 0);
				//设置为persistent进程
                app.persistent = true;
                app.pid = MY_PID;
                app.maxAdj = ProcessList.SYSTEM_ADJ;
                app.makeActive(mSystemThread.getApplicationThread(), mProcessStats);
                synchronized (mPidsSelfLocked) {
                    mPidsSelfLocked.put(app.pid, app);
                }
				//维护进程lru
                updateLruProcessLocked(app, false, null);
				//更新adj
                updateOomAdjLocked();
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(
                    "Unable to find android system package", e);
        }
    }

- 该方法主要注册各种服务


### 2.2.1 ActivityThread.installSystemApplicationInfo

    public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        synchronized (this) {
            getSystemContext().installSystemApplicationInfo(info, classLoader);
            getSystemUiContext().installSystemApplicationInfo(info, classLoader);

            // give ourselves a default profiler
            mProfiler = new Profiler();
        }
    }

- `getSystemContext()`返回了一个`ContextImpl`对象

		//创建ContextImpl
	    public ContextImpl getSystemContext() {
	        synchronized (this) {
	            if (mSystemContext == null) {
	                mSystemContext = ContextImpl.createSystemContext(this);
	            }
	            return mSystemContext;
	        }
	    }
	
		//创建LoadedApk,并为ContextImpl 设置信息
	    static ContextImpl createSystemContext(ActivityThread mainThread) {
	        LoadedApk packageInfo = new LoadedApk(mainThread);
	        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0,
	                null);
	        context.setResources(packageInfo.getResources());
	        context.mResources.updateConfiguration(context.mResourcesManager.getConfiguration(),
	                context.mResourcesManager.getDisplayMetrics());
	        return context;
	    }

- `getSystemUiContext()`返回的也是`ContextImpl`

	    /**
	     * System Context to be used for UI. This Context has resources that can be themed.
	     * Make sure that the created system UI context shares the same LoadedApk as the system context.
	     */
	    static ContextImpl createSystemUiContext(ContextImpl systemContext) {
	        final LoadedApk packageInfo = systemContext.mPackageInfo;
	        ContextImpl context = new ContextImpl(null, systemContext.mMainThread, packageInfo, null,
	                null, null, 0, null);
	        context.setResources(createResources(null, packageInfo, null, Display.DEFAULT_DISPLAY, null,
	                packageInfo.getCompatibilityInfo()));
	        return context;
	    }



- 调用`ContextImpl`的`installSystemApplicationInfo()`方法.实际上就是调用`LoadedApk`的方法

	    void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
	        mPackageInfo.installSystemApplicationInfo(info, classLoader);
	    }

### 2.2.2 LoadedApk.installSystemApplicationInfo

    /**
     * Sets application info about the system package.
     */
    void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        assert info.packageName.equals("android");
        mApplicationInfo = info;
        mClassLoader = classLoader;
    }

- `ApplicationInfo`在`AMS.setSystemProcess()`中获取.

		mContext.getPackageManager().getApplicationInfo("android", STOCK_PM_FLAGS | MATCH_SYSTEM_ONLY);

- 将包名为`android`的应用信息保存到`mApplicationInfo`

- 保存`classLoader`


# 3. 启动流程-startOtherServices()

    private void startOtherServices() {
        ...
        mActivityManagerService.installSystemProviders(); //provider
        ```省略代码```

        //phase480 和phase500
        mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
        mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
        ...
        
        //重点
        // We now tell the activity manager it is okay to run third party
        // code.  It will call back into us once it has gotten to the state
        // where third party code can really run (but before it has actually
        // started launching the initial applications), for us to complete our
        // initialization.
		//查看 3.2
		mActivityManagerService.systemReady(() -> {
	         //phase550
	         mSystemServiceManager.startBootPhase(
	                 SystemService.PHASE_ACTIVITY_MANAGER_READY);
	         ...
	         //phase600
	         mSystemServiceManager.startBootPhase(
	                 SystemService.PHASE_THIRD_PARTY_APPS_CAN_START);
	         ...
        });
    }

## 3.1 AMS.installSystemProviders()

    public final void installSystemProviders() {
        List<ProviderInfo> providers;
        synchronized (this) {
            ProcessRecord app = mProcessNames.get("system", SYSTEM_UID);
            providers = generateApplicationProvidersLocked(app);
            if (providers != null) {
                for (int i=providers.size()-1; i>=0; i--) {
                    ProviderInfo pi = (ProviderInfo)providers.get(i);
					//移除非系统的provider
                    if ((pi.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM) == 0) {
                        Slog.w(TAG, "Not installing system proc provider " + pi.name
                                + ": not system .apk");
                        providers.remove(i);
                    }
                }
            }
        }
        if (providers != null) {
			//安装所有的系统Providers
            mSystemThread.installSystemProviders(providers);
        }

        mConstants.start(mContext.getContentResolver());
        mCoreSettingsObserver = new CoreSettingsObserver(this);
        mFontScaleSettingObserver = new FontScaleSettingObserver();

        // Now that the settings provider is published we can consider sending
        // in a rescue party.
        RescueParty.onSettingsProviderPublished(mContext);

        //mUsageStatsService.monitorPackages();
    }

## 3.2 AMS.systemReady()

代码过长,简单划分为三部分

    public void systemReady(final Runnable goingCallback, TimingsTraceLog traceLog) {
	    before goingCallback;
	    goingCallback.run();
	    after goingCallback;
	}

    public void systemReady(final Runnable goingCallback, TimingsTraceLog traceLog) {
        traceLog.traceBegin("PhaseActivityManagerReady");
        synchronized(this) {
			//第一次应该为false
            if (mSystemReady) {
                // If we're done calling all the receivers, run the next "boot phase" passed in
                // by the SystemServer
                if (goingCallback != null) {
                    goingCallback.run();
                }
                return;
            }

            mLocalDeviceIdleController
                    = LocalServices.getService(DeviceIdleController.LocalService.class);
            mAssistUtils = new AssistUtils(mContext);
            mVrController.onSystemReady();
            // Make sure we have the current profile info, since it is needed for security checks.
            mUserController.onSystemReady();
            mRecentTasks.onSystemReadyLocked();
            mAppOpsService.systemReady();
            mSystemReady = true;
        }

        try {
            sTheRealBuildSerial = IDeviceIdentifiersPolicyService.Stub.asInterface(
                    ServiceManager.getService(Context.DEVICE_IDENTIFIERS_SERVICE))
                    .getSerial();
        } catch (RemoteException e) {}

        ArrayList<ProcessRecord> procsToKill = null;
        synchronized(mPidsSelfLocked) {
            for (int i=mPidsSelfLocked.size()-1; i>=0; i--) {
                ProcessRecord proc = mPidsSelfLocked.valueAt(i);
                if (!isAllowedWhileBooting(proc.info)){
                    if (procsToKill == null) {
                        procsToKill = new ArrayList<ProcessRecord>();
                    }
                    procsToKill.add(proc);
                }
            }
        }

        synchronized(this) {
            if (procsToKill != null) {
                for (int i=procsToKill.size()-1; i>=0; i--) {
                    ProcessRecord proc = procsToKill.get(i);
                    Slog.i(TAG, "Removing system update proc: " + proc);
                    removeProcessLocked(proc, true, false, "system update done");
                }
            }

            // Now that we have cleaned up any update processes, we
            // are ready to start launching real processes and know that
            // we won't trample on them any more.
            mProcessesReady = true;
        }

        Slog.i(TAG, "System now ready");
        EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_AMS_READY,
            SystemClock.uptimeMillis());

        synchronized(this) {
            // Make sure we have no pre-ready processes sitting around.

            if (mFactoryTest == FactoryTest.FACTORY_TEST_LOW_LEVEL) {
                ResolveInfo ri = mContext.getPackageManager()
                        .resolveActivity(new Intent(Intent.ACTION_FACTORY_TEST),
                                STOCK_PM_FLAGS);
                CharSequence errorMsg = null;
                if (ri != null) {
                    ActivityInfo ai = ri.activityInfo;
                    ApplicationInfo app = ai.applicationInfo;
                    if ((app.flags&ApplicationInfo.FLAG_SYSTEM) != 0) {
                        mTopAction = Intent.ACTION_FACTORY_TEST;
                        mTopData = null;
                        mTopComponent = new ComponentName(app.packageName,
                                ai.name);
                    } else {
                        errorMsg = mContext.getResources().getText(
                                com.android.internal.R.string.factorytest_not_system);
                    }
                } else {
                    errorMsg = mContext.getResources().getText(
                            com.android.internal.R.string.factorytest_no_action);
                }
                if (errorMsg != null) {
                    mTopAction = null;
                    mTopData = null;
                    mTopComponent = null;
                    Message msg = Message.obtain();
                    msg.what = SHOW_FACTORY_ERROR_UI_MSG;
                    msg.getData().putCharSequence("msg", errorMsg);
                    mUiHandler.sendMessage(msg);
                }
            }
        }

        retrieveSettings();
        final int currentUserId;
        synchronized (this) {
            currentUserId = mUserController.getCurrentUserIdLocked();
            readGrantedUriPermissionsLocked();
        }

        if (goingCallback != null) goingCallback.run();
        traceLog.traceBegin("ActivityManagerStartApps");
        mBatteryStatsService.noteEvent(BatteryStats.HistoryItem.EVENT_USER_RUNNING_START,
                Integer.toString(currentUserId), currentUserId);
        mBatteryStatsService.noteEvent(BatteryStats.HistoryItem.EVENT_USER_FOREGROUND_START,
                Integer.toString(currentUserId), currentUserId);
        mSystemServiceManager.startUser(currentUserId);

        synchronized (this) {
            // Only start up encryption-aware persistent apps; once user is
            // unlocked we'll come back around and start unaware apps
            startPersistentApps(PackageManager.MATCH_DIRECT_BOOT_AWARE);

            // Start up initial activity.
            mBooting = true;
            // Enable home activity for system user, so that the system can always boot. We don't
            // do this when the system user is not setup since the setup wizard should be the one
            // to handle home activity in this case.
            if (UserManager.isSplitSystemUser() &&
                    Settings.Secure.getInt(mContext.getContentResolver(),
                         Settings.Secure.USER_SETUP_COMPLETE, 0) != 0) {
                ComponentName cName = new ComponentName(mContext, SystemUserHomeActivity.class);
                try {
                    AppGlobals.getPackageManager().setComponentEnabledSetting(cName,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0,
                            UserHandle.USER_SYSTEM);
                } catch (RemoteException e) {
                    throw e.rethrowAsRuntimeException();
                }
            }
            startHomeActivityLocked(currentUserId, "systemReady");

            try {
                if (AppGlobals.getPackageManager().hasSystemUidErrors()) {
                    Slog.e(TAG, "UIDs on the system are inconsistent, you need to wipe your"
                            + " data partition or your device will be unstable.");
                    mUiHandler.obtainMessage(SHOW_UID_ERROR_UI_MSG).sendToTarget();
                }
            } catch (RemoteException e) {
            }

            if (!Build.isBuildConsistent()) {
                Slog.e(TAG, "Build fingerprint is not consistent, warning user");
                mUiHandler.obtainMessage(SHOW_FINGERPRINT_ERROR_UI_MSG).sendToTarget();
            }

            long ident = Binder.clearCallingIdentity();
            try {
                Intent intent = new Intent(Intent.ACTION_USER_STARTED);
                intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY
                        | Intent.FLAG_RECEIVER_FOREGROUND);
                intent.putExtra(Intent.EXTRA_USER_HANDLE, currentUserId);
                broadcastIntentLocked(null, null, intent,
                        null, null, 0, null, null, null, AppOpsManager.OP_NONE,
                        null, false, false, MY_PID, SYSTEM_UID,
                        currentUserId);
                intent = new Intent(Intent.ACTION_USER_STARTING);
                intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
                intent.putExtra(Intent.EXTRA_USER_HANDLE, currentUserId);
                broadcastIntentLocked(null, null, intent,
                        null, new IIntentReceiver.Stub() {
                            @Override
                            public void performReceive(Intent intent, int resultCode, String data,
                                    Bundle extras, boolean ordered, boolean sticky, int sendingUser)
                                    throws RemoteException {
                            }
                        }, 0, null, null,
                        new String[] {INTERACT_ACROSS_USERS}, AppOpsManager.OP_NONE,
                        null, true, false, MY_PID, SYSTEM_UID, UserHandle.USER_ALL);
            } catch (Throwable t) {
                Slog.wtf(TAG, "Failed sending first user broadcasts", t);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
            mStackSupervisor.resumeFocusedStackTopActivityLocked();
            mUserController.sendUserSwitchBroadcastsLocked(-1, currentUserId);
            traceLog.traceEnd(); // ActivityManagerStartApps
            traceLog.traceEnd(); // PhaseActivityManagerReady
        }
    }

### 3.2.1 before goingCallBack

该阶段的主要功能

- 向`PRE_BOOT_COMPLETED`的接收者发送广播

- 杀掉`procsToKill`中的进程,杀掉进程且不允许重启

- 此时,系统和进程都处于ready状态


