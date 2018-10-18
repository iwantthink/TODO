# WindowManagerService分析

[WMS-启动过程](http://gityuan.com/2017/01/08/windowmanger/)

[WMS的诞生(1)](https://blog.csdn.net/itachi85/article/details/78186741)

[Android窗口管理服务WindowManagerService的简要介绍和学习计划](https://blog.csdn.net/Luoshengyang/article/details/8462738)

# 1. 简介
**基于Android 27,代码中会移除掉LOG和一些与分析内容不相关的代码**

内容包含WMS的创建和使用

# 2. WMS的创建过程

在分析过`SystemServer.java`之后,可以知道`WindowManagerService`是在此被创建并启动

## 2.1 SystemServer入口

	public static void main(String[] args) {
	       new SystemServer().run();
	}

## 2.2 SystemServer.run()

	  private void run() {
	         try {
				// 加载 libandroid_servers.so 
	            System.loadLibrary("android_servers");
	            ...
				// 创建 SystemServiceManager,对系统的服务进行创建,启动和生周期管理
	            mSystemServiceManager = new SystemServiceManager(mSystemContext);
	            mSystemServiceManager.setRuntimeRestarted(mRuntimeRestart);
	            LocalServices.addService(SystemServiceManager.class, mSystemServiceManager);
	            // Prepare the thread pool for init tasks that can be parallelized
	            SystemServerInitThreadPool.get();
	        } finally {
	            traceEnd();  // InitBeforeStartServices
	        }
	        try {
	            traceBeginAndSlog("StartServices");
	            startBootstrapServices();
	            startCoreServices();
	            startOtherServices();
	            SystemServerInitThreadPool.shutdown();
	        } catch (Throwable ex) {
	        } finally {
	        }
	    ...
	    }

- Android官方将系统服务分为了三个类型,分别是 引导服务,核心服务和其他服务.这三个类型的服务分别在`startBootstrapServices()`,`startCoreServices()`,`startOtherServices()`中被启动

- 其他服务是一些非紧要和不需要立刻启动的服务,`WMS`就属于这种类型


## 2.3 SystemServer.startOtherServices()

	private void startOtherServices() {
	 ...
	            //获取实例并进行初始化
	            final Watchdog watchdog = Watchdog.getInstance();
	            watchdog.init(context, mActivityManagerService);
	            //创建IMS
	            inputManager = new InputManagerService(context);
				//执行WMS的main方法,内部会创建WMS
				//构造函数中有一个IMS,因为WMS是输入事件的中转站,所以会包含IMS
	            wm = WindowManagerService.main(context, inputManager,
	                    mFactoryTestMode != FactoryTest.FACTORY_TEST_LOW_LEVEL,
	                    !mFirstBoot, mOnlyCore, new PhoneWindowManager());
				//将WMS,IMS注册到ServiceManager
	            ServiceManager.addService(Context.WINDOW_SERVICE, wm);
	            ServiceManager.addService(Context.INPUT_SERVICE, inputManager);
	           try {
				//初始化显示信息
	            wm.displayReady();
	               } catch (Throwable e) {
	            reportWtf("making display ready", e);
	              }
	           ...
	           try {
				//通知WMS 系统的初始化已经完成
				//内部调用了 WindwoManagerPolicy的systemReady
	            wm.systemReady();
	               } catch (Throwable e) {
	            reportWtf("making Window Manager Service ready", e);
	              }
	            ...      
	}

- 因为`WMS.main()`是运行在`SystemServer.run()`方法中,`WMS.main()`运行在`system_server`进程

- 客户端需要使用WMS,就要先去`ServiceManager`中查询,根据SM的返回的信息建立通信(即获取Binder代理对象)

## 2.4 WindowManagerService.main()

    public static WindowManagerService main(final Context context, final InputManagerService im,
            final boolean haveInputMethods, final boolean showBootMsgs, final boolean onlyCore,
            WindowManagerPolicy policy) {
		//DisplayThread是一个单例的前台线程,处理需要低延时显示的相关操作,只能由
		//WindowManager,DisplayManager,InputManager实时执行快速操作
        DisplayThread.getHandler().runWithScissors(() ->
				//创建WMS实例
                sInstance = new WindowManagerService(context, im, haveInputMethods, showBootMsgs,
                        onlyCore, policy), 0);
        return sInstance;
    }

- `WMS`实例的创建在一个`Runnable`中,而这个`Runnable`是被`DisplayThread`所属的`Handler`执行.所以可以得出`WMS`的创建过程是运行在`android.display`线程中

- `runWithScissors`方法的第二个参数是0


## 2.5 WindowManagerService.runWithScissors()

    public final boolean runWithScissors(final Runnable r, long timeout) {
        if (r == null) {
            throw new IllegalArgumentException("runnable must not be null");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be non-negative");
        }
		//Looper.myLooper()获取的是执行runWithScissors()方法的线程
		// mLooper 获取的是当前Handler所在的线程
        if (Looper.myLooper() == mLooper) {
            r.run();
            return true;
        }

        BlockingRunnable br = new BlockingRunnable(r);
        return br.postAndWait(this, timeout);
    }

- 根据每个线程只有一个Looper的原理判断当前的线程(`system_server`线程)是否是Handler所指向的线程(`android.display`线程).如果是则直接执行,否则调用`BlockingRunnable.postAndWait()`进行执行


## 2.6 BlockingRunnable.postAndWait()

    private static final class BlockingRunnable implements Runnable {
        private final Runnable mTask;
        private boolean mDone;

        public BlockingRunnable(Runnable task) {
            mTask = task;
        }

        @Override
        public void run() {
			//执行了传入的Runnable,(运行在`android.display`线程)
            try {
                mTask.run();
            } finally {
                synchronized (this) {
                    mDone = true;
                    notifyAll();
                }
            }
        }

        public boolean postAndWait(Handler handler, long timeout) {
			// 将当前Runnable添加到handler队列中
            if (!handler.post(this)) {
				//插入失败 直接当做执行失败
                return false;
            }

            synchronized (this) {
                if (timeout > 0) {
                    final long expirationTime = SystemClock.uptimeMillis() + timeout;
                    while (!mDone) {
                        long delay = expirationTime - SystemClock.uptimeMillis();
                        if (delay <= 0) {
                            return false; // timeout
                        }
                        try {
                            wait(delay);
                        } catch (InterruptedException ex) {
                        }
                    }
                } else {
					//mDone = false 会一直调用wait()方法使当前(system_server线程)进入等待状态
                    while (!mDone) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
            return true;
        }
    }

- `system_server`线程会一直等待`android.display`线程执行完`BlockingRunnable`

# 3. WMS内部创建过程

## 3.1 WMS构造函数

	   private WindowManagerService(Context context, InputManagerService inputManager,
	            boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore) {
	       //保存IMS,这样WMS就持有了IMS
	       mInputManager = inputManager;
	       ...
	        mDisplayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
			//每个显示设备都有一个Display实例
	        mDisplays = mDisplayManager.getDisplays();
	        for (Display display : mDisplays) {
				//将display封装成DisplayContent
	            createDisplayContentLocked(display);
	        }
	        //获得AMS实例,持有AMS实例
	        mActivityManager = ActivityManagerNative.getDefault();
	        //创建 WindowAnimator
	        mAnimator = new WindowAnimator(this);
	        mAllowTheaterModeWakeFromLayout = context.getResources().getBoolean(
	                com.android.internal.R.bool.config_allowTheaterModeWakeFromWindowLayout);
	        LocalServices.addService(WindowManagerInternal.class, new LocalService());
			//初始化了窗口管理策略的接口类WindowManagerPolicy
	        initPolicy();
	        // Add ourself to the Watchdog monitors.
	        Watchdog.getInstance().addMonitor(this);
	     ...
	    }

- `DisplayContent`是用来描述一块屏幕

- `WindwoAnimator`用于管理所有的窗口动画,

- `WindowManagerPolicy` 用来定义一个窗口策略所需要遵循的通用规范

- `Watchdog`用来监控系统的一些关键服务的运行状况（比如传入的WMS的运行状况），这些被监控的服务都会实现`Watchdog.Monitor`接口。`Watchdog`每分钟都会对被监控的系统服务进行检查，如果被监控的系统服务出现了死锁，则会杀死`Watchdog`所在的进程，也就是`system_server`进程。

## 3.2 WMS.initPolicy()

    private void initPolicy() {
        UiThread.getHandler().runWithScissors(new Runnable() {
            @Override
            public void run() {
                WindowManagerPolicyThread.set(Thread.currentThread(), Looper.myLooper());

                mPolicy.init(mContext, WindowManagerService.this, WindowManagerService.this);
            }
        }, 0);
    }

- `initPolicy()`方法与`WMS.main()`方法的实现类似.`runWithScissors()`运行在`android.display`线程中,`WMP.init()`运行在`android.ui`线程中,直到执行结束才会去唤醒`android.display`

# 4. WMS创建时的线程关系图

![](http://upload-images.jianshu.io/upload_images/1417629-f57741f3fe96198d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

# 5. WMS中的重要成员

## 5.1 WMS

    final WindowManagerPolicy mPolicy;
    final IActivityManager mActivityManager;
    final ActivityManagerInternal mAmInternal;
    final AppOpsManager mAppOps;
    final DisplaySettings mDisplaySettings;
    ...
    final ArraySet<Session> mSessions = new ArraySet<>();
    final WindowHashMap mWindowMap = new WindowHashMap();
    final ArrayList<AppWindowToken> mFinishedStarting = new ArrayList<>();
    final ArrayList<AppWindowToken> mFinishedEarlyAnim = new ArrayList<>();
    final ArrayList<AppWindowToken> mWindowReplacementTimeouts = new ArrayList<>();
    final ArrayList<WindowState> mResizingWindows = new ArrayList<>();
    final ArrayList<WindowState> mPendingRemove = new ArrayList<>();
    WindowState[] mPendingRemoveTmp = new WindowState[20];
    final ArrayList<WindowState> mDestroySurface = new ArrayList<>();
    final ArrayList<WindowState> mDestroyPreservedSurface = new ArrayList<>();
    ...
    final H mH = new H();
    ...
    final WindowAnimator mAnimator;
    ...
     final InputManagerService mInputManager

- `mPolicy`： `WindowManagerPolicy`接口。

	`WindowManagerPolicy`是窗口管理策略的接口类，用来定义一个窗口策略所要遵循的通用规范，并提供了`WindowManager`所有的特定的UI行为。**它的具体实现类为`PhoneWindowManager`**，这个实现类在WMS创建时被创建(`SystemServer.startOtherServices()`)。WMP允许定制窗口层级和特殊窗口类型以及关键的调度和布局。

- `mSessions`：ArraySet 

	ArraySet类型的变量，元素类型为`Session`。**`Session`主要用于进程间通信**，其他的应用程序进程想要和`WMS`进程进行通信就需要经过`Session`，并且每个应用程序进程都会对应一个Session，WMS保存这些Session用来记录所有向WMS提出窗口管理服务的客户端。 

- `mWindowMap`：WindowHashMap 

	WindowHashMap继承了HashMap，它限制了`HashMap`的key值的类型为`IBinder`，`value`值的类型为`WindowState`。`WindowState`用于保存窗口的信息，在`WMS`中它用来描述一个窗口。**综上得出结论，`mWindowMap`就是用来保存WMS中各种窗口的集合。**

- `mFinishedStarting`：ArrayList 
	
	元素类型为`AppWindowToken`，它是`WindowToken的`子类。要想理解mFinishedStarting的含义，需要先了解`WindowToken`是什么。

	**WindowToken主要有两个作用**：

	1. 可以理解为窗口令牌，当应用程序想要向`WMS`申请新创建一个窗口，则需要向`WMS`出示有效的`WindowToken`。`AppWindowToken`作为`WindowToken`的子类，主要用来描述应用程序的`WindowToken`结构， 

	应用程序中每个Activity都对应一个AppWindowToken。

	2. `WindowToken`会将相同组件（比如Acitivity）的窗口（`WindowState`）集合在一起，方便管理。

- `mFinishedStarting`就是用于存储已经完成启动的应用程序窗口（比如Acitivity）的AppWindowToken的列表。 
	
	除了mFinishedStarting，还有类似的mFinishedEarlyAnim和mWindowReplacementTimeouts，其中mFinishedEarlyAnim存储了已经完成窗口绘制并且不需要展示任何已保存surface的应用程序窗口的AppWindowToken。mWindowReplacementTimeout存储了等待更换的应用程序窗口的AppWindowToken，如果更换不及时，旧窗口就需要被处理。

- `mResizingWindows`：ArrayList 

	元素类型为`WindowState`。 `WindowState`用于保存窗口的信息，在`WMS`中它用来描述一个窗口。

- `mResizingWindows`是用来存储正在调整大小的窗口的列表。与

	`mResizingWindows`类似的还有`mPendingRemove`、`mDestroySurface`和`mDestroyPreservedSurface`等等。
	
	其中`mPendingRemove`是在内存耗尽时设置的，里面存有需要强制删除的窗口。

	`mDestroySurface`里面存有需要被`Destroy`的`Surface`。

	`mDestroyPreservedSurface`里面存有窗口需要保存的等待销毁的`Surface`

	**为什么窗口要保存这些Surface？这是因为当窗口经历Surface变化时，窗口需要一直保持旧Surface，直到新Surface的第一帧绘制完成。**

- `mAnimator`：WindowAnimator 

	用于管理窗口的动画以及特效动画。

- `mH`：H 

	系统的Handler类，用于将任务加入到主线程的消息队列中，这样代码逻辑就会在主线程中执行。

- `mInputManager`：InputManagerService 

	输入系统的管理者。`InputManagerService`（IMS）会对触摸事件进行处理，它会寻找一个最合适的窗口来处理触摸反馈信息，`WMS`是窗口的管理者，因此，`WMS`“理所应当”的成为了输入系统的中转站，WMS包含了IMS的引用不足为怪。


# 6. Window的添加过程

