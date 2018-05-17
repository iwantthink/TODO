# ActivityThread分析
[Android主线程(ActivityThread)源代码分析](https://blog.csdn.net/shifuhetudi/article/details/52089562)

[ActivityThread源码](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app)

[Instrumentation源码](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/Instrumentation.java)

[LoadedApk源码](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/LoadedApk.java)

[ActivityManagerService源码](https://android.googlesource.com/platform/frameworks/base/+/master/services/core/java/com/android/server/am/ActivityManagerService.java)

[ProcessRecord源码](https://android.googlesource.com/platform/frameworks/base/+/master/services/core/java/com/android/server/am/ProcessRecord.java)

[深入理解Activity启动流程](http://ju.outofmemory.cn/entry/169880)

# 1. 简介

android应用程序作为控制类程序，跟Java程序类似，都有一个入口，Java程序的入口是main()函数，**而Adnroid程序的入口是ActivityThread 的main()方法**

ActivityThread主要的作用是 根据AMS(ActivityManagerService)的要求，通过IApplicationThread的接口来负责调度和执行activities,broadcasts和其他操作。

在Android系统中，四大组件默认都是运行在主线程中

# 2. ActivityThread main(String [] args)

    public static void main(String[] args) {
        Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "ActivityThreadMain");
        // CloseGuard defaults to true and can be quite spammy.  We
        // disable it here, but selectively enable it later (via
        // StrictMode) on debug builds, but using DropBox, not logs.
        CloseGuard.setEnabled(false);
        Environment.initForCurrentUser();
        // Set the reporter for event logging in libcore
        EventLogger.setReporter(new EventLoggingReporter());
        // Make sure TrustedCertificateStore looks in the right place for CA certificates
        final File configDir = Environment.getUserConfigDirectory(UserHandle.myUserId());
        TrustedCertificateStore.setDefaultUserDirectory(configDir);
        Process.setArgV0("<pre-initialized>");
		//初始化主线程的Looper
        Looper.prepareMainLooper();
		//创建ActivityThread ,并绑定到AMS
        ActivityThread thread = new ActivityThread();
		// 一般的应用程序都不是系统应用，因此设置为false，在这里执行的逻辑包含绑定到AMS
        thread.attach(false);
        if (sMainThreadHandler == null) {
            sMainThreadHandler = thread.getHandler();
        }
        if (false) {
            Looper.myLooper().setMessageLogging(new
                    LogPrinter(Log.DEBUG, "ActivityThread"));
        }
        // End of event ActivityThreadMain.
        Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
		//开启循环,无法退出 一旦退出就会抛出异常
        Looper.loop();
        throw new RuntimeException("Main thread loop unexpectedly exited");
    }

**这里可以看到俩点：**

- 在Activity中可以直接去创建Handler 并使用 而不用调用`Looper.prepare()`和`Looper.loop()` 是因为在ActivityThread 的入口处 已经做了这个操作

- **主线程的Looper是不能在程序中调用退出的**，如果调用的话，就会抛出异常，**退出主线程的循环是框架层在调用退出应用程序的时候才调用的**

## 2.1 ActivityThread 成员变量介绍

	//IBinder对象,AMS持有此对象的代理对象，从而通知ActivityThread管理其他事情
	final ApplicationThread mAppThread = new ApplicationThread();
	//主线程的Looper
	final Looper mLooper = Looper.myLooper();
	//主线程的 Handler，用来处理系统消息
	final H mH = new H();
	//存储了所有的Activity,以IBinder作为key,IBinder是Activity在框架层的唯一表示
	final ArrayMap<IBinder, ActivityClientRecord> mActivities = new ArrayMap<>();
	//存储了所有的Service
	final ArrayMap<IBinder, Service> mServices = new ArrayMap<>();
	//ActivityThread对象，拿到这个对象，可以反射调用这个类的需要的方法
	private static ActivityThread sCurrentActivityThread;

**App中的页面都会保存在mActivities字段中，拿到这个字段就可以知道当前APP有哪些activity，而这些activity都是用户停留过的**

### 2.1.1 ActivityClientRecord

	//存储的Activity的表示对象ActivityClientRecord
	 static final class ActivityClientRecord {
	         //唯一表示
	        IBinder token;
	        //这里存储了真正的Activity对象
	        Activity activity;
	        //省略代码
	    }

ActivityClientRecord是ActivityThread的一个内部类，这个ActivityClientRecord 是传入AMS的一个标志，里面携带了很多信息，代码中的有一个Activity对象，就是真正的Activity实例。通过它可以知道用户去往哪些页面

### 2.1.2 ApplicationThread 

    private class ApplicationThread extends IApplicationThread.Stub {

	}


- ApplicationThread是一个Binder对象

- ApplicationThread是提供给AMS的，用来控制Activity去执行对应方法

### 2.1.3 H

	private class H extends Handler {
		//启动Activity
        public static final int LAUNCH_ACTIVITY         = 100;
		//暂停Activity
        public static final int PAUSE_ACTIVITY          = 101;
		//省略若干代码

		  public void handleMessage(Message msg) {
            if (DEBUG_MESSAGES) Slog.v(TAG, ">>> handling: " + codeToString(msg.what));
            switch (msg.what) {
                case LAUNCH_ACTIVITY: {
                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
                    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;
                    r.loadedApk = getLoadedApkNoCheck(
                            r.activityInfo.applicationInfo, r.compatInfo);
                    handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                } break;
			//省略若干代码
			}
		//省略若干代码
		}

- Android系统也是通过消息分发机制来实现系统的运转


## 2.2 ActivityThread.attach(boolean system)

    private void attach(boolean system) {
		//赋值当前的activityThread
        sCurrentActivityThread = this;
        mSystemThread = system;
		//非系统应用
        if (!system) {
            ViewRootImpl.addFirstDrawHandler(new Runnable() {
                @Override
                public void run() {
                    ensureJitEnabled();
                }
            });
            android.ddm.DdmHandleAppName.setAppName("<pre-initialized>",
                                                    UserHandle.myUserId());
            RuntimeInit.setApplicationObject(mAppThread.asBinder());
            final IActivityManager mgr = ActivityManager.getService();
            try {
                mgr.attachApplication(mAppThread);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
            // Watch for getting close to heap limit.
            BinderInternal.addGcWatcher(new Runnable() {
                @Override public void run() {
                    if (!mSomeActivitiesChanged) {
                        return;
                    }
                    Runtime runtime = Runtime.getRuntime();
                    long dalvikMax = runtime.maxMemory();
                    long dalvikUsed = runtime.totalMemory() - runtime.freeMemory();
                    if (dalvikUsed > ((3*dalvikMax)/4)) {
                        if (DEBUG_MEMORY_TRIM) Slog.d(TAG, "Dalvik max=" + (dalvikMax/1024)
                                + " total=" + (runtime.totalMemory()/1024)
                                + " used=" + (dalvikUsed/1024));
                        mSomeActivitiesChanged = false;
                        try {
                            mgr.releaseSomeActivities(mAppThread);
                        } catch (RemoteException e) {
                            throw e.rethrowFromSystemServer();
                        }
                    }
                }
            });
        } 
        //省略代码
    }

- 通过ActivityManager.getService()获取到一个代理Binder对象(IBinder),然后通过IActivityManager.Stub 进行转换(获取Stub类或者Stub内部类Proxy)。

	**可以通过源代码看到，getService()方法 借助`Singleton`类 实现了单例的懒加载**

		public static IActivityManager getService() {
	        return IActivityManagerSingleton.get();
	    }
	    private static final Singleton<IActivityManager> IActivityManagerSingleton =
	            new Singleton<IActivityManager>() {
	                @Override
	                protected IActivityManager create() {
	                    final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
	                    final IActivityManager am = IActivityManager.Stub.asInterface(b);
	                    return am;
	                }
	            };

	[Android-util-Singleton源码](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/util/Singleton.java)

	**通过IActivityManager 可以用来调用ActivityServiceManager的方法**

## 2.3 AMS.attachApplication(mAppThread)

`mAppThread`是ApplicationThread类型，该类型继承自IApplicationThread.Stub类型(即一个Binder对象)

IActivityManager是一个`IInterface`，代表`ActivityManagerService `具备什么能力(即有哪些接口可供调用)。

`attach()`方法中通过`ActivityManager.getService()`获取到了ASM的Binder代理对象，然后通过这个对象调用 ActivityManagerService的`attachApplication(mAppThread)`，mAppThread传递给ActivityManagerService 提供给AMS去调用四大组件的方法(实际上这个AMS接收到的mAppThread是一个ApplicationThreadProxy,即Binder的代理对象)




# 3. AMS.attachApplication()

**这段逻辑都执行在`system_server`进程**

[启动Activity的工作过程](https://blog.csdn.net/qian520ao/article/details/78156214#bindapplication)

## 3.1 ActivityManagerService.attachApplication

	    @Override
	    public final void attachApplication(IApplicationThread thread) {
	        synchronized (this) {
	            //获取applicationThread的进程id
	            int callingPid = Binder.getCallingPid();
	            final long origId = Binder.clearCallingIdentity();
	            attachApplicationLocked(thread, callingPid);
	            Binder.restoreCallingIdentity(origId);
	        }
	    }

- AMS通过传入的`IApplicationThread`类型的这个对象去通知`ActivityThread`去创建/关联和启动Activity

- `Binder.getCallingPid()`返回的是发起这个跨进程请求的进程的信息。即通过Binder获取Proxy(ApplicationThread.Stub.Proxy)方的进程id

## 3.2 ActivityManagerService.attachApplicationLocked

	    private final boolean attachApplicationLocked(IApplicationThread thread,
	            int pid) {
	
	        // Find the application record that is being attached...  either via
	        // the pid if we are running in multiple processes, or just pull the
	        // next app record if we are emulating process with anonymous threads.
	        ProcessRecord app;
	        if (pid != MY_PID && pid >= 0) {
	            synchronized (mPidsSelfLocked) {
	                app = mPidsSelfLocked.get(pid);
	            }
	        } else {
	            app = null;
	        }
	
	        //因为进程由AMS启动，所以在AMS中一定会有ProcessRecord（进程记录）
	        //如果没有ProcessRecord，则需要杀死该进程并退出
	        if (app == null) {
	            ``````
	            return false;
	        }
	
	        // If this application record is still attached to a previous
	        // process, clean it up now.
	        if (app.thread != null) {
	            //如果从ProcessRecord中获取的IApplicationThread不为空，则需要处理该IApplicationThread
	            //因为有可能此Pid为复用，旧应用进程刚释放，内部IApplicationThread尚未清空，
	            //同时新进程又刚好使用了此Pid
	            handleAppDiedLocked(app, true, true);
	        }
	
	        //创建死亡代理（进程kill后通知AMS）
	        AppDeathRecipient adr = new AppDeathRecipient(app, pid, thread);
			thread.asBinder().linkToDeath(adr, 0);
            app.deathRecipient = adr;
	
	        //进程注册成功，移除超时通知
	        mHandler.removeMessages(PROC_START_TIMEOUT_MSG, app);
	
	        ``````
	        try {
	            //******绑定Application******
				//通过IApplicationThread
	            thread.bindApplication(processName, appInfo, providers, app.instrumentationClass,
	                    profilerInfo, app.instrumentationArguments, app.instrumentationWatcher,
	                    app.instrumentationUiAutomationConnection, testMode,
	                    mBinderTransactionTrackingEnabled, enableTrackAllocation,
	                    isRestrictedBackupMode || !normalMode, app.persistent,
	                    new Configuration(mConfiguration), app.compat,
	                    getCommonServicesLocked(app.isolated),
	                    mCoreSettingsObserver.getCoreSettingsLocked());
	
	            updateLruProcessLocked(app, false, null);
	        } catch (Exception e) {
	
	            ``````
	            //bindApplication失败后，重启进程
	            startProcessLocked(app, "bind fail", processName);
	            return false;
	        }
	
	        try {
	            //******启动Activity(启动MainActivity)******
	            if (mStackSupervisor.attachApplicationLocked(app)) {
	                didSomething = true;//didSomething表示是否有启动四大组件
	            }
	        } catch (Exception e) {
	            badApp = true;
	        }
	
	        ``````
	        //绑定service和Broadcast的Application
	
	        if (badApp) {
	            //如果以上组件启动出错，则需要杀死进程并移除记录
	            app.kill("error during init", true);
	            handleAppDiedLocked(app, false, true);
	            return false;
	        }
	
	        //如果以上没有启动任何组件，那么didSomething为false
	        if (!didSomething) {
	            //调整进程的oom_adj值， oom_adj相当于一种优先级
	            //如果应用进程没有运行任何组件，那么当内存出现不足时，该进程是最先被系统“杀死”
	            updateOomAdjLocked();
	        }
	        return true;
	    }

俩个重要方法函数：
1. `thread.bindApplication(...)`:绑定`Application`到`ActivityThread`。**查看3.3**
	
2.  `mStackSupervisor.attachApplicationLocked(app)`:启动Activity(7.0之前的代码是:`mMainStack.realStartActivityLocked()`)。**查看3.4**

### 3.3.1 ActivityThread.ApplicationThread.bindApplication()

		//ActivityThread内部类ApplicationThread
	    private class ApplicationThread extends ApplicationThreadNative {
	
	        public final void bindApplication(...参数...) {
	            AppBindData data = new AppBindData();
	            //给data设置参数...
	            ``````
	            sendMessage(H.BIND_APPLICATION, data);
	        }
	    }
	
	    private void sendMessage(int what, Object obj, int arg1, int arg2, boolean async) {
	        Message msg = Message.obtain();
	        //给msg设置参数
	        ``````
	        mH.sendMessage(msg);
	    }

- 通过Handler发送消息有俩个优点：

	1. 便于集中管理，方便打印LOG日志等

	2. 通过Handler 来将线程切换到主线程中去做一些事情

- 这个`mH`中的`handleMessage(Message msg)`方法

	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	                ``````  
	                //绑定application
	                case BIND_APPLICATION:
	                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "bindApplication");
	                    AppBindData data = (AppBindData)msg.obj;
	                    handleBindApplication(data);
	                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
	                    break;
	            }

### 3.3.2 ActivityThread.handleBindApplication(AppBindData data)

        private void handleBindApplication(AppBindData data) {

           ``````
          //根据传递过来的ApplicationInfo创建一个对应的LoadedApk对象
          data.loadedApk  = getPackageInfoNoCheck(data.appInfo, data.compatInfo);//获取LoadedApk

          /**
          * For apps targetting Honeycomb or later, we don't allow network usage
          * on the main event loop / UI thread. This is what ultimately throws
          * {@link NetworkOnMainThreadException}.
          */
          //禁止在主线程使用网络操作
          if (data.appInfo.targetSdkVersion >= Build.VERSION_CODES.HONEYCOMB) {
              StrictMode.enableDeathOnNetwork();
          }
          /**
           * For apps targetting N or later, we don't allow file:// Uri exposure.
           * This is what ultimately throws {@link FileUriExposedException}.
           */
           //7.0引入Fileprovide
          if (data.appInfo.targetSdkVersion >= Build.VERSION_CODES.N) {
              StrictMode.enableDeathOnFileUriExposure();
          }

          ``````    

		  // Instrumentation info affects the class loader, so load it before
	      // setting up the app context.
	        final InstrumentationInfo ii;
	      if (data.instrumentationName != null) {
	            try {
	                ii = new ApplicationPackageManager(null, getPackageManager())
	                        .getInstrumentationInfo(data.instrumentationName, 0);
	            } catch (PackageManager.NameNotFoundException e) {
	                throw new RuntimeException(
	                        "Unable to find instrumentation info for: " + data.instrumentationName);
	            }
	
	            mInstrumentationPackageName = ii.packageName;
	            mInstrumentationAppDir = ii.sourceDir;
	            mInstrumentationSplitAppDirs = ii.splitSourceDirs;
	            mInstrumentationLibDir = getInstrumentationLibrary(data.appInfo, ii);
	            mInstrumentedAppDir = data.loadedApk.getAppDir();
	            mInstrumentedSplitAppDirs = data.loadedApk.getSplitAppDirs();
	            mInstrumentedLibDir = data.loadedApk.getLibDir();
	        } else {
	            ii = null;
	      }

          //创建进程对应的Android运行环境ContextImpl
          final ContextImpl appContext = ContextImpl.createAppContext(this, data.info);

		  // Continue loading instrumentation.
          if (ii != null) {
               ``````
          } else {
               //注意Activity的所有生命周期方法都会被Instrumentation对象所监控，
               //也就说执行Activity的生命周期方法前后一定会调用Instrumentation对象的相关方法
               mInstrumentation = new Instrumentation();
          }
			```````
		  //全局唯一的Application对象
		  Application app;
          try {
             // If the app is being launched for full backup or restore, bring it up in
             // a restricted environment with the base application class.
			 //通过LoadedApk创建application
             app = data.loadedApk.makeApplication(data.restrictedBackupMode, null);

             mInitialApplication = app;
            
             // don't bring up providers in restricted mode; they may depend on the
             // app's custom Application class
             if (!data.restrictedBackupMode) {
                if (!ArrayUtils.isEmpty(data.providers)) {
				    //加载进程对应Package中携带的ContentProvider
                    installContentProviders(app, data.providers);
                    // For process that contains content providers, we want to
                    // ensure that the JIT is enabled "at some point".
                    mH.sendEmptyMessageDelayed(H.ENABLE_JIT, 10*1000);
                }
             }

             // Do this after providers, since instrumentation tests generally start their
             // test thread at this point, and we don't want that racing.
             try {
                mInstrumentation.onCreate(data.instrumentationArgs);
             }    

             try {
                  //这里会调用Application的onCreate方法
                  //故此Applcation对象的onCreate方法会比ActivityThread的main方法后调用
                  //但是会比这个应用的所有activity先调用
			       mInstrumentation.callApplicationOnCreate(app);
              } catch (Exception e) {
                  ``````
              }
            } finally {
                StrictMode.setThreadPolicy(savedPolicy);
            }
        }

- `handleBindApplication`的目的是让一个Java进程融入到Android体系中，该函数主要做以下工作去使得新进程融入Android体系：
	1. 按照Android的要求，完成对进程基本参数的设置，包括设置进程名,时区,资源以及兼容性配置 。**同时也添加一些限制，例如主线程不能访问网络**、

	2. 创建进程对应的`ContextImpl,LoadedApk,Application`等对象，同时加载`Application`中的`ContentProvider`,并初始化`Application`

	3. 使用`Instramentation`监控Activity的生命周期(一个进程对应一个`Instrumentation`实例)

### 3.4.1 ActivityStackSupervisor.attachApplicationLocked(ProcessRecord app)

    boolean attachApplicationLocked(ProcessRecord app) throws RemoteException {
        final String processName = app.processName;
        boolean didSomething = false;

        //ActivityStackSupervisor维护着终端中所有ActivityStack
        //此处通过轮询，找出前台栈顶端的待启动Activity
        for (int displayNdx = mActivityDisplays.size() - 1; displayNdx >= 0; --displayNdx) {
            ArrayList<ActivityStack> stacks = mActivityDisplays.valueAt(displayNdx).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; --stackNdx) {
                final ActivityStack stack = stacks.get(stackNdx);
                if (!isFocusedStack(stack)) {
                    continue;
                }

                ActivityRecord hr = stack.topRunningActivityLocked();
                if (hr != null) {
                    //前台待启动的Activity与当前新建的进程一致时，启动这个Activity
                    if (hr.app == null && app.uid == hr.info.applicationInfo.uid
                            && processName.equals(hr.processName)) {
                        try {

                            //realStartActivityLocked进行实际的启动工作
                            if (realStartActivityLocked(hr, app, true, true)) {
                                didSomething = true;
                            }
                        } catch (RemoteException e) {

                        }
                    }
                }
            }
        }

        return didSomething;
    }

- `ActivityStack`:该类主要管理回退栈

- `ActivityStackSupervisor`:维护终端中所有的`ActivityStack`

- `realStartActivityLocked()`方法进行实际的启动工作。。**查看3.5**

## 3.5 ActivityStackSupervisor.realStartActivityLocked()

	```省略代码```
	app.thread.scheduleLaunchActivity(new Intent(r.intent), r.appToken,
                    System.identityHashCode(r), r.info,
                    // TODO: Have this take the merged configuration instead of separate global and
                    // override configs.
                    mergedConfiguration.getGlobalConfiguration(),
                    mergedConfiguration.getOverrideConfiguration(), r.compat,
                    r.launchedFromPackage, task.voiceInteractor, app.repProcState, r.icicle,
                    r.persistentState, results, newIntents, !andResume,
                    mService.isNextTransitionForward(), profilerInfo);
	```省略代码``
- **在`realStartActivityLocked（）`方法中通过`ApplicationThread`的binder代理对象调用`ApplicationThread`中的方法，然后通过`Handler`切换到`ActivityThread`所在线程去执行具体逻辑**

# 4.ApplicationThread.scheduleLaunchActivity

## 4.1 scheduleLaunchActivity

    //ActivityThread内部类ApplicationThread
    private class ApplicationThread extends ApplicationThreadNative {
        @Override
        public final void scheduleLaunchActivity(Intent intent, IBinder token, int ident,
                ActivityInfo info, Configuration curConfig, Configuration overrideConfig,
                CompatibilityInfo compatInfo, String referrer, IVoiceInteractor voiceInteractor,
                int procState, Bundle state, PersistableBundle persistentState,
                List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents,
                boolean notResumed, boolean isForward, ProfilerInfo profilerInfo) {

            updateProcessState(procState, false);

            ActivityClientRecord r = new ActivityClientRecord();
            //设置参数
            ``````

            //从LAUNCH_ACTIVITY这个标识我们就可以知道，它就是用来启动Activity
            sendMessage(H.LAUNCH_ACTIVITY, r);
        }
    }

    private class H extends Handler {
        ``````

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LAUNCH_ACTIVITY: {
                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
                    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;
					//利用ApplicationInfo等信息得到对应的LoadedApk，保存到ActivityClientRecord
                    //ActivityClientRecord包含Activity相关的信息
                    r.loadedApk = getLoadedApkNoCheck(
                            r.activityInfo.applicationInfo, r.compatInfo);
                    handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);

                ``````  
            }
        }
    }

- `ActivityClientRecord`:包含Activity相关的信息,是ActivityThread的内部类

## 4.2 handleLaunchActivity(..)

	private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent) {
	    ``````
	
	    Activity a = performLaunchActivity(r, customIntent);
	    if (a != null) {
	        ``````
	        handleResumeActivity(r.token, false, r.isForward,
	                !r.activity.mFinished && !r.startsNotResumed, r.lastProcessedSeq, reason);
	
	        ``````
	    }else{
		// If there was an error, for any reason, tell the activity manager to stop us.
            try {
                ActivityManager.getService()
                    .finishActivity(r.token, Activity.RESULT_CANCELED, null,
                            Activity.DONT_FINISH_TASK_WITH_ACTIVITY);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
		}    

	    ``````
	}

- `performLaunchActivity`:会创建并调用Activity的`onCreate,onStart,onRestoreInstanceState`方法.**具体查看4.3**

- `handleResumeActivity`:会调用Acitivity的`onResume`方法.**具体查看4.4**

## 4.3 performLaunchActivity

### 4.3.1 从ActivityClientRecord中获取待启动的Activity的信息

        ActivityInfo aInfo = r.activityInfo;
        if (r.packageInfo == null) {
            r.packageInfo = getPackageInfo(aInfo.applicationInfo, r.compatInfo,
                    Context.CONTEXT_INCLUDE_CODE);
        }

        ComponentName component = r.intent.getComponent();
        if (component == null) {
            component = r.intent.resolveActivity(
                mInitialApplication.getPackageManager());
            r.intent.setComponent(component);
        }

        if (r.activityInfo.targetActivity != null) {
            component = new ComponentName(r.activityInfo.packageName,
                    r.activityInfo.targetActivity);
        }

### 4.3.2 创建Context

    ContextImpl appContext = createBaseContextForActivity(r);

- **`ContextImpl`是一个非常重要的数据结构，它是`Context`的具体实现，`Context`中的大部分逻辑都是由`ContextImpl`来完成的。**

- **`ContextImpl`通过`Activity.attach()`方法与`Activity`建立关联**

- **Activity.attach()方法中还会完成`Window`的创建并建立与`Window`的关联，这样当`Window`接收到外部事件之后就可以将事件传递给`Activity`去处理**

### 4.3.3 通过Instrumentation创建Activity

        ContextImpl appContext = createBaseContextForActivity(r);
		Activity activity = null;
	        try {
	            java.lang.ClassLoader cl = appContext.getClassLoader();
	            activity = mInstrumentation.newActivity(
	                    cl, component.getClassName(), r.intent);
	            ···省略代码···
	        } catch (Exception e) {
	            if (!mInstrumentation.onException(activity, e)) {
	                throw new RuntimeException(
	                    "Unable to instantiate activity " + component
	                    + ": " + e.toString(), e);
	            }
	        }

- `Instrumentation.newActivity`:

	    /**
	     * Perform instantiation of the process's {@link Activity} object.  The
	     * default implementation provides the normal system behavior.
	     * 
	     * @param cl The ClassLoader with which to instantiate the object.
	     * @param className The name of the class implementing the Activity
	     *                  object.
	     * @param intent The Intent object that specified the activity class being
	     *               instantiated.
	     * 
	     * @return The newly instantiated Activity object.
	     */
	    public Activity newActivity(ClassLoader cl, String className,
	            Intent intent)
	            throws InstantiationException, IllegalAccessException,
	            ClassNotFoundException {
	        return (Activity)cl.loadClass(className).newInstance();
	    }

	- `newActivity()`这个方法实际上就是通过类加载器将`Activity`这个类创建出来

### 4.3.4 通过LoadedApk创建Application

    Application app = r.loadedApk.makeApplication(false, mInstrumentation);

- 这里的`r`指的是`ActivityClientRecord`.使用`ActivityClientRecord`保存的`LoadedApk`去创建`Application`

- **实际上，在ActivityThread.handleBindApplication()中已经通过`LoadedApk`创建了Application,然后通过`Instrumentation`调用其`onCreate()`方法**。所以这里实际上是检查`Application`是否已经被创建

	    public Application makeApplication(boolean forceDefaultAppClass,
	            Instrumentation instrumentation) {
	        if (mApplication != null) {
	            return mApplication;
	        }
			···省略代码···
			//实际上也是通过ClassLoader创建的
		} 

### 4.3.5 调用Activity.attach完成一些数据的初始化

	activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window, r.configCallback);

- `attach()`方法会将`app,appContext`对象绑定到新创建的Activity

- **Activity.attach()方法中还会完成`Window`的创建并建立与`Window`的关联，这样当`Window`接收到外部事件之后就可以将事件传递给`Activity`去处理**


### 4.3.6 调用Instrumentation.callActivityOnCreate

	if (r.isPersistable()) {
	                    mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
	                } else {
	                    mInstrumentation.callActivityOnCreate(activity, r.state);
	                }

- **`callActivityOnCreate()`方法会调用`Activity`的`performCreate`.在`performCreate`中会调用`Activity`的`onCreate`方法**

### 4.3.7 调用Activity.performStart()

    if (!r.activity.mFinished) {
                    activity.performStart();
                    r.stopped = false;
                }

- 这里没有通过`Instrumentation`直接去调用了`Activity.performStart`

### 4.3.8 调用Instrumentation.callActivityOnRestoreInstanceState()

    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        activity.performRestoreInstanceState(savedInstanceState);
    }

- **保存下来的信息除了在`onCreate`中接收到，还会专门的在`Activity.onRestoreInstanceState()`方法中收到**

## 4.4 handleResumeActivity

### 4.4.1 handleResumeActivity
	
	 final void handleResumeActivity(IBinder token,
	            boolean clearHide, boolean isForward, boolean reallyResume, int seq, String reason) {
			//ActivityClinetRecord保存了Activity的信息
	        ActivityClientRecord r = mActivities.get(token);		
			···省略代码···
	        // 最终会去调用Activity的onResume方法
	        r = performResumeActivity(token, clearHide, reason);
	
	        if (r != null) {
				//获得Activity对象
	            final Activity a = r.activity;
				···省略代码···
				//显示界面
	            if (r.window == null && !a.mFinished && willBeVisible) {
					//获取Activity对应的PhoneWindow对象
	                r.window = r.activity.getWindow();
					//获取顶层布局DecorView对象
	                View decor = r.window.getDecorView();
					//设置为不可见
	                decor.setVisibility(View.INVISIBLE);
					//获取WindowManager
	                ViewManager wm = a.getWindowManager();
	                WindowManager.LayoutParams l = r.window.getAttributes();
	                a.mDecor = decor;
	                l.type = WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
	                l.softInputMode |= forwardBit;
	                if (r.mPreserveWindow) {
	                    a.mWindowAdded = true;
	                    r.mPreserveWindow = false;
	                    // Normally the ViewRoot sets up callbacks with the Activity
	                    // in addView->ViewRootImpl#setView. If we are instead reusing
	                    // the decor view we have to notify the view root that the
	                    // callbacks may have changed.
	                    ViewRootImpl impl = decor.getViewRootImpl();
	                    if (impl != null) {
	                        impl.notifyChildRebuilt();
	                    }
	                }
	                if (a.mVisibleFromClient) {
	                    if (!a.mWindowAdded) {
							//添加DecorView到窗口，并改变状态FLAG
	                        a.mWindowAdded = true;	
	                        wm.addView(decor, l);
	                    } else {
	                        // The activity will get a callback for this {@link LayoutParams} change
	                        // earlier. However, at that time the decor will not be set (this is set
	                        // in this method), so no action will be taken. This call ensures the
	                        // callback occurs with the decor set.
	                        a.onWindowAttributesChanged(l);
	                    }
	                }
	
	            // If the window has already been added, but during resume
	            // we started another activity, then don't yet make the
	            // window visible.
	            } else if (!willBeVisible) {
	                if (localLOGV) Slog.v(
	                    TAG, "Launch " + r + " mStartedActivity set");
	                r.hideForNow = true;
	            }
			    ···省略代码···

				// The window is now visible if it has been added, we are not
	            // simply finishing, and we are not starting another activity.
	            if (!r.activity.mFinished && willBeVisible
	                    && r.activity.mDecor != null && !r.hideForNow) {
	                if (r.newConfig != null) {
	                    performConfigurationChangedForActivity(r, r.newConfig);
	                    if (DEBUG_CONFIGURATION) Slog.v(TAG, "Resuming activity "
	                            + r.activityInfo.name + " with newConfig " + r.activity.mCurrentConfig);
	                    r.newConfig = null;
	                }
	                if (localLOGV) Slog.v(TAG, "Resuming " + r + " with isForward="
	                        + isForward);
	                WindowManager.LayoutParams l = r.window.getAttributes();
	                if ((l.softInputMode
	                        & WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION)
	                        != forwardBit) {
	                    l.softInputMode = (l.softInputMode
	                            & (~WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION))
	                            | forwardBit;
	                    if (r.activity.mVisibleFromClient) {
	                        ViewManager wm = a.getWindowManager();
	                        View decor = r.window.getDecorView();
	                        wm.updateViewLayout(decor, l);
	                    }
	                }
	
	                r.activity.mVisibleFromServer = true;
	                mNumVisibleActivities++;
	                if (r.activity.mVisibleFromClient) {
						//设置顶层的DecorView可见
	                    r.activity.makeVisible();
	                }
	            }
	            // 同时AMS 当前activity已经进入resume状态
	            if (reallyResume) {
	                try {
	                    ActivityManager.getService().activityResumed(token);
	                } catch (RemoteException ex) {
	                    throw ex.rethrowFromSystemServer();
	                }
	            }
	
	        } else {
	           //出了问题。。要通知ASM去结束Activity
	        }
	    }

### 4.4.2 performResumeActivity

	···省略代码···
    r.activity.performResume();
	···省略代码···

- 调用`Activity.performResume()`,该方法中会调用`onResume`,表示当前页面已经可见

- **注意**

![](https://img-blog.csdn.net/20171010211537259?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcWlhbjUyMGFv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)