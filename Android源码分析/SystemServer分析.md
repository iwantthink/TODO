# SystemServer分析

[Android系统启动-综述](http://gityuan.com/2016/02/01/android-booting/)

[Android源码分析-GitYuan-目录](http://gityuan.com/android/)

[Android系统启动-Systemserver](http://gityuan.com/2016/02/14/android-system-server/)

[Android 8.0系统源码分析--Binder进程间通信（三）](https://blog.csdn.net/sinat_22657459/article/details/78757632)


# 1. 简介

![](http://gityuan.com/images/process/android-booting.jpg)

Linux系统中 用户空间的第一个进程是`init`进程(Kernel启动后,会创建`init`进程). **然后`init`进程会启动`ServiceManager`(Binder服务管家),`Zygote`进程(Java进程的鼻祖,通过fork它会创建`system_server`进程等各种app进程)**


SystemServer进程由`Zygote fork`生成,**进程名为`system_server`**,该进程承载着framework的核心服务

- `Zygote`启动过程中 会调用`startSystemServer()`,该方法即`system_server`的起点

- **`system_server`进程就是Android系统Service运行的进程**

	Android的应用进程没有权限直接访问设备的底层资源,只能通过`SystemServer`中的服务代理访问.

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

            // 创建系统服务管理器
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

- `LocalServices`通过用静态Map变量`sLocalServiceObjects`，来保存以服务类名为key，以具体服务对象为value的Map结构。

- **该方法中创建了`SystemServiceManager`,该类用来启动各种服务**

- `startBootstrapServices(),startCoreServices(),startOtherServices()`等方法 用来创建并开启服务

- `Looper.loop()`方法进入消息循环

### 3.2.1 createSystemContext()
	
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

    private void startOtherServices() {
        ...
        SystemConfig.getInstance();
        mContentResolver = context.getContentResolver(); // resolver
        ...
        mActivityManagerService.installSystemProviders(); //provider
        mSystemServiceManager.startService(AlarmManagerService.class); // alarm
        // watchdog
        watchdog.init(context, mActivityManagerService); 
        inputManager = new InputManagerService(context); // input
        wm = WindowManagerService.main(...); // window
        inputManager.start();  //启动input
        mDisplayManagerService.windowManagerAndInputReady();
        ...
        mSystemServiceManager.startService(MOUNT_SERVICE_CLASS); // mount
        mPackageManagerService.performBootDexOpt();  // dexopt操作
        ActivityManagerNative.getDefault().showBootMessage(...); //显示启动界面
        ...
        statusBar = new StatusBarManagerService(context, wm); //statusBar
        //dropbox
        ServiceManager.addService(Context.DROPBOX_SERVICE,
                    new DropBoxManagerService(context, new File("/data/system/dropbox")));
         mSystemServiceManager.startService(JobSchedulerService.class); //JobScheduler
         lockSettings.systemReady(); //lockSettings

        //phase480 和phase500
        mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
        mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
        ...
        // 准备好window, power, package, display服务
        wm.systemReady();
        mPowerManagerService.systemReady(...);
        mPackageManagerService.systemReady();
        mDisplayManagerService.systemReady(...);
        
        //重点
        // We now tell the activity manager it is okay to run third party
        // code.  It will call back into us once it has gotten to the state
        // where third party code can really run (but before it has actually
        // started launching the initial applications), for us to complete our
        // initialization.
		mActivityManagerService.systemReady(() -> {
  			......
        });
    }

- [AMS的具体创建过程](http://gityuan.com/2016/02/21/activity-manager-service/)

- **这一步执行完之后,`SystemServer`进程的主线程的启动准备工作完成,然后会开启`Looper.loop()`,等待其他线程通过Handler发送消息到主线程进行处理**


# 4. 服务的各个阶段

`SystemServiceManager`的`startBootPhase()`贯穿`system_server`进程的整个启动过程：

- `SystemServiceManager.startBoootPhase()`:

		Starts the specified boot phase for all system services that have been started up to this point.

![](http://gityuan.com/images/boot/systemServer/system_server_boot_process.jpg)

**代码中的位置:**

	public final class SystemServer {
	
	    private void startBootstrapServices() {
	      ...
	      //phase100
	      mSystemServiceManager.startBootPhase(SystemService.PHASE_WAIT_FOR_DEFAULT_DISPLAY);
	      ...
	    }
	
	    private void startCoreServices() {
	      ...
	    }
	
	    private void startOtherServices() {
	      ...
	      //phase480 && 500
	      mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
	      mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
	      
	      ...
	      mActivityManagerService.systemReady(()-> {
	             //phase550
	             mSystemServiceManager.startBootPhase(
	                     SystemService.PHASE_ACTIVITY_MANAGER_READY);
	             ...
	             //phase600
	             mSystemServiceManager.startBootPhase(
	                     SystemService.PHASE_THIRD_PARTY_APPS_CAN_START);
	      }
	    }
	}

## 4.1 PHASE 0

	SystemServiceManager.mCurrentPhase = -1

SystemServer中 启动了许多核心的引导服务

- ActivityManagerService

- PowerManagerService

- LightsService

- DisplayManagerService

- Installer

- DeviceIdentifiersPolicyService

- RecoverySystemService


## 4.2 Phase100

    /*
     * Boot Phases
     */

    public static final int PHASE_WAIT_FOR_DEFAULT_DISPLAY = 100; // maybe should be a dependency?


SystemServiceManager中会回调各个已经被创建的Service的`onBootPhase(100)`方法,然后在`SystemServer`中创建大量的服务

- PackageManagerService
- WindowManagerService
- InputManagerService
- NetworkManagerService
- DropBoxManagerService
- FingerprintService
- LauncherAppsService
- ....省略


## 4.3 Phase480

    /**
     * After receiving this boot phase, services can obtain lock settings data.
     */
	
    public static final int PHASE_LOCK_SETTINGS_READY = 480;


SystemServiceManager中会回调各个已经被创建的Service的`onBootPhase(480)`

该状态紧接着就是下一个Phase500.

## 4.4 Phase500

    /**
     * After receiving this boot phase, services can safely call into core system services
     * such as the PowerManager or PackageManager.
     */

    public static final int PHASE_SYSTEM_SERVICES_READY = 500;


SystemServiceManager中会回调各个已经被创建的Service的`onBootPhase(480)`


**进入该阶段之后,服务就能安全调用核心系统服务.**接下来,在SystemServer中,各个服务执行`SystemReady()`

- WindowManagerService.systemReady():
- PowerManagerService.systemReady():
- PackageManagerService.systemReady():
- DisplayManagerService.systemReady():

## 4.5 Phase550

    /**
     * After receiving this boot phase, services can broadcast Intents.
     */
    public static final int PHASE_ACTIVITY_MANAGER_READY = 550;


SystemServiceManager中会回调各个已经被创建的Service的`onBootPhase(550)`

进入该阶段之后,服务能广播intent,但是`system_server`进程的主线程并未就绪

接下来会执行AMS启动native crash监控, 加载WebView，启动SystemUi等

## 4.6 Phase600

    /**
     * After receiving this boot phase, services can start/bind to third party apps.
     * Apps will be able to make Binder calls into services at this point.
     */
    public static final int PHASE_THIRD_PARTY_APPS_CAN_START = 600;

SystemServiceManager中会回调各个已经被创建的Service的`onBootPhase(600)`

**接下里,各种服务一次执行其 `systemRunning()`方法**


## 4.7 Phase1000

    /**
     * After receiving this boot phase, services can allow user interaction with the device.
     * This phase occurs when boot has completed and the home application has started.
     * System services may prefer to listen to this phase rather than registering a
     * broadcast receiver for ACTION_BOOT_COMPLETED to reduce overall latency.
     */
    public static final int PHASE_BOOT_COMPLETED = 1000;

在经过一系列流程，再调用AMS.finishBooting()时，则进入阶段Phase1000。

当到达这个阶段,服务允许用户与设备进行交互.这个阶段发生在boot已经完成,且Home应用已经开启

到此，系统服务启动阶段完成就绪，`system_server`进程启动完成则进入Looper.loop()状态，随时待命，等待消息队列MessageQueue中的消息到来，则马上进入执行状态。

# 5.服务分类

`system_server`进程，从源码角度划分为引导服务、核心服务、其他服务3类。 

- 引导服务(7个)：

	ActivityManagerService、PowerManagerService、LightsService、DisplayManagerService、PackageManagerService、UserManagerService、SensorService；

- 核心服务(3个)：

	BatteryService、UsageStatsService、WebViewUpdateService；

- 其他服务(70个+)：

	AlarmManagerService、VibratorService等。

# 6.服务注册方式

[Android系统服务的注册方式](http://gityuan.com/2016/10/01/system_service_common/)

启动过程采用俩种不同的方式来注册系统服务:

- `ServiceManager.addService()`

- `SystemServiceManager.startService()`

**俩者核心都是向`ServiceManager`进程注册Binder服务**


## 6.1 ServiceManager

例如`ConnectivityService`的注册过程:

	ConnectivityService connectivity = new ConnectivityService(context, networkManagement, networkStats, networkPolicy);
	ServiceManager.addService(Context.CONNECTIVITY_SERVICE, connectivity);


### 6.1.1 ServiceManager.addService()

    /**
     * Place a new @a service called @a name into the service
     * manager.
     * 
     * @param name the name of the new service
     * @param service the service object
     */
    public static void addService(String name, IBinder service) {
        try {
            getIServiceManager().addService(name, service, false);
        } catch (RemoteException e) {
            Log.e(TAG, "error in addService", e);
        }
    }

- `addService()`一共有俩个重载方法

### 6.1.2 ServiceManager.getIServiceManager

    private static IServiceManager getIServiceManager() {
        if (sServiceManager != null) {
            return sServiceManager;
        }

        // Find the service manager
        sServiceManager = ServiceManagerNative
                .asInterface(Binder.allowBlocking(BinderInternal.getContextObject()));
        return sServiceManager;
    }

- `BinderInternal.getContextObject()`,指向远程进程(目录`/system/bin/servicemanager/`)中的Servicemanager服务

- 通过Binder通信,这里的`sServiceManager`实际上是`ServiceManagerProxy`(即Binder的远程代理对象).

		public abstract class ServiceManagerNative extends Binder implements IServiceManager
		{
		    /**
		     * Cast a Binder object into a service manager interface, generating
		     * a proxy if needed.
		     */
		    static public IServiceManager asInterface(IBinder obj)
		    {
		        if (obj == null) {
		            return null;
		        }
		        IServiceManager in =
		            (IServiceManager)obj.queryLocalInterface(descriptor);
		        if (in != null) {
		            return in;
		        }
		        
		        return new ServiceManagerProxy(obj);
		    }
			````省略代码``
		}

- 这里还是一个单例

- `ServiceManager`是由init进程通过解析`init.rc`文件而创建的

- **这一步主要是为了获取能够跟远程ServiceManager服务进行通信的Binder远程代理,`ServiceManager`可以管理所有Binder服务**

### 6.1.3 ServiceManagerProxy.addService()

    public void addService(String name, IBinder service, boolean allowIsolated)
            throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IServiceManager.descriptor);
        data.writeString(name);
        data.writeStrongBinder(service);
        data.writeInt(allowIsolated ? 1 : 0);
        mRemote.transact(ADD_SERVICE_TRANSACTION, data, reply, 0);
        reply.recycle();
        data.recycle();
    }

- `ServiceManagerProxy`是`ServiceManagerNative`的内部类

-  通过`mRemote`(即Binder远程代理)发送一个`ADD_SERVICE_TRANSACTION`消息到`ServiceManager`真正所在的进程

	具体的在`system_server`中的`ServiceManager`服务如何注册服务参考文章->>>>>[Binder系列5—注册服务(addService)](http://gityuan.com/2015/11/14/binder-add-service/#addservice)


## 6.2 SystemServiceManager

**通过`SystemServiceManager`启动的服务,都有一个特点,就是继承自`SystemService`对象**

例如`AlarmManagerService`的注册过程:

	SystemServiceManager mSystemServiceManager = new SystemServiceManager(mSystemContext); 

	mSystemServiceManager.startService(AlarmManagerService.class);

- 注意:这里需要创建`SystemServiceManager`对象,因为注册方法是对象的方法.


### 6.2.1 SSM 构造函数

	/**
	 * Manages creating, starting, and other lifecycle events of
	 * {@link com.android.server.SystemService system services}.
	 *
	 * {@hide}
	 */
	public class SystemServiceManager {
	
	    // Services that should receive lifecycle events.
	    private final ArrayList<SystemService> mServices = new ArrayList<SystemService>();
	
	    SystemServiceManager(Context context) {
	        mContext = context;
	    }
	
	}

### 6.2.2 SSM.startService

    /**
     * Starts a service by class name.
     *
     * @return The service instance.
     */
    @SuppressWarnings("unchecked")
    public SystemService startService(String className) {
		```省略代码```
	}

    /**
     * Creates and starts a system service. The class must be a subclass of
     * {@link com.android.server.SystemService}.
     *
     * @param serviceClass A Java class that implements the SystemService interface.
     * @return The service instance, never null.
     * @throws RuntimeException if the service fails to start.
     */
    @SuppressWarnings("unchecked")
    public <T extends SystemService> T startService(Class<T> serviceClass) {
		```省略代码```
	}

    public void startService(@NonNull final SystemService service) {
        // Register it.
        mServices.add(service);
        // Start it.
        long time = SystemClock.elapsedRealtime();
        try {
            service.onStart();
        } catch (RuntimeException ex) {
            throw new RuntimeException("Failed to start service " + service.getClass().getName()
                    + ": onStart threw an exception", ex);
        }
        warnIfTooLong(SystemClock.elapsedRealtime() - time, service, "onStart");
    }

- 方法1,传入待启动的服务的类名称

- 方法2,传入`SystemService`及其子类 的字节码.会通过反射生成一个服务类对象

- 方法3,具体的开启服务的地方(**主要有俩步操作**)

	1. Register it
	2. Start it

`startService()`方法主要完成

1. 创建类对象
2. 注册服务,将服务的类对象添加到`mServices`集合
3. 调用服务的类对象的`onStart()`
	
### 6.2.3 SystemService.onStart()

    /**
     * Called when the dependencies listed in the @Service class-annotation are available
     * and after the chosen start phase.
     * When this method returns, the service should be published.
     */
    public abstract void onStart();

### 6.2.4 AlarmManagerService.onStart()

    @Override
    public void onStart() {
		```省略一些准备的逻辑``

        publishBinderService(Context.ALARM_SERVICE, mService);
        publishLocalService(LocalService.class, new LocalService());
    }


    private final IBinder mService = new IAlarmManager.Stub() {
		```具体的实现逻辑```
	}

- `AlarmManagerService`内部创建了一个`IAlarmManager.Stub`对象.该类就是Binder本地对象,真正实现服务逻辑的地方

- 通过`publishBinderService()`方法注册服务

### 6.2.5 SystemService.publishBinderService

    /**
     * Publish the service so it is accessible to other services and apps.
     */
    protected final void publishBinderService(String name, IBinder service) {
        publishBinderService(name, service, false);
    }

    /**
     * Publish the service so it is accessible to other services and apps.
     */
    protected final void publishBinderService(String name, IBinder service,
            boolean allowIsolated) {
        ServiceManager.addService(name, service, allowIsolated);
    }

- 会发现,最终还是会调用`ServiceManager.addService()`

# 7. 俩者区别?

其主要区别就在`SystemServiceManager`的`startBootPhase(int phase)`.已知在`SystemServer`类中,会根据当前系统启动到不同的阶段,通过`startBootPhase()` 回调所有服务的`onBootPhase()`

即系统开启启动过程,当执行到`system_server`进程时,将启动过程划分为几个阶段.

	public static final int PHASE_WAIT_FOR_DEFAULT_DISPLAY = 100; 
	public static final int PHASE_LOCK_SETTINGS_READY = 480;
	public static final int PHASE_SYSTEM_SERVICES_READY = 500;
	public static final int PHASE_ACTIVITY_MANAGER_READY = 550;
	public static final int PHASE_THIRD_PARTY_APPS_CAN_START = 600;
	public static final int PHASE_BOOT_COMPLETED = 1000;

![](http://gityuan.com/images/boot/systemServer/system_server_boot_process.jpg)

- `PHASE_BOOT_COMPLETED=1000`，该阶段是发生在Boot完成和home应用启动完毕, 对于系统服务更倾向于监听该阶段，而非监听广播ACTION_BOOT_COMPLETED


**俩种注册方式的过程都会调用到`ServiceManager.addService()`,对于方式二来说,其多了一个服务对象创建以及 根据不同启动阶段采用不同的动作的过程. 即方式二比方式一功能更丰富**