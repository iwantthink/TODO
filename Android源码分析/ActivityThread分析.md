# ActivityThread分析
[Android主线程(ActivityThread)源代码分析](https://blog.csdn.net/shifuhetudi/article/details/52089562)

[ActivityThread源码](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app)

[深入理解Activity启动流程](http://ju.outofmemory.cn/entry/169880)

# 1. 简介

android应用程序作为控制类程序，跟Java程序类似，都有一个入口，Java程序的入口是main()函数，**而Adnroid程序的入口是ActivityThread 的main()方法**

ActivityThread主要的作用是 根据AMS(ActivityManagerService)的要求，通过IApplicationThread的接口来负责调用和执行activities,broadcasts和其他操作。

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

## 2.1 ActivityThread 成员变量

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

## 2.2 ActivityClientRecord

	//存储的Activity的表示对象ActivityClientRecord
	 static final class ActivityClientRecord {
	         //唯一表示
	        IBinder token;
	        //这里存储了真正的Activity对象
	        Activity activity;
	        //省略代码
	    }

ActivityClientRecord是ActivityThread的一个内部类，这个ActivityClientRecord 是传入AMS的一个标志，里面携带了很多信息，代码中的有一个Activity对象，就是真正的Activity实例。通过它可以知道用户去往哪些页面

## 2.3 ApplicationThread 

    private class ApplicationThread extends IApplicationThread.Stub {

	}


- ApplicationThread是一个Binder对象

- ApplicationThread是提供给AMS的，用来控制Activity去执行对应方法

## 2.4 H

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


## 2.5 attach(boolean system)

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

## 2.6 mgr.attachApplication(mAppThread)

`mAppThread`是ApplicationThread类型，该类型继承自IApplicationThread.Stub类型(即一个Binder对象)

IActivityManager是一个`IInterface`，代表`ActivityManagerService `具备什么能力(即有哪些接口可供调用)。

`attach()`方法中通过`ActivityManager.getService()`获取到了ASM的Binder代理对象，然后通过这个对象调用 ActivityManagerService的`attachApplication(mAppThread)`，mAppThread传递给ActivityManagerService 提供给AMS去调用四大组件的方法(实际上这个AMS接收到的mAppThread是一个ApplicationThreadProxy,即Binder的代理对象)

- 通过IActivityManager


## 2.7 AMS.attachApplication()

[启动Activity的工作过程](https://blog.csdn.net/qian520ao/article/details/78156214#bindapplication)

1. `ActivityManagerService.attachApplication` ->

2. `ActivityManagerService.attachApplicationLocked` ->