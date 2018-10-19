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

之前[Window的相关操作.md]()中已经分析过`Window`在应用进程中的创建过程,并与 2.3.1-2.3.2 小节得知,在视图的创建过程中会调用 `WMS.addWindow`

## 6.1 addWindow-1
	
	 public int addWindow(Session session, IWindow client, int seq,
	            WindowManager.LayoutParams attrs, int viewVisibility, int displayId,
	            Rect outContentInsets, Rect outStableInsets, Rect outOutsets,
	            InputChannel outInputChannel) {
	
	        int[] appOp = new int[1];
			//检查权限,如果没有权限会结束方法
	        int res = mPolicy.checkAddPermission(attrs, appOp);//1
	        if (res != WindowManagerGlobal.ADD_OKAY) {
	            return res;
	        }
	        ...
	        synchronized(mWindowMap) {
	            if (!mDisplayReady) {
	                throw new IllegalStateException("Display has not been initialialized");
	            }
	            final DisplayContent displayContent = mRoot.getDisplayContentOrCreate(displayId);//2
	            if (displayContent == null) {
	                Slog.w(TAG_WM, "Attempted to add window to a display that does not exist: "
	                        + displayId + ".  Aborting.");
	                return WindowManagerGlobal.ADD_INVALID_DISPLAY;
	            }
	            ...
	            if (type >= FIRST_SUB_WINDOW && type <= LAST_SUB_WINDOW) {//3
	                parentWindow = windowForClientLocked(null, attrs.token, false);//4
	                if (parentWindow == null) {
	                    Slog.w(TAG_WM, "Attempted to add window with token that is not a window: "
	                          + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN;
	                }
	                if (parentWindow.mAttrs.type >= FIRST_SUB_WINDOW
	                        && parentWindow.mAttrs.type <= LAST_SUB_WINDOW) {
	                    Slog.w(TAG_WM, "Attempted to add window with token that is a sub-window: "
	                            + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN;
	                }
	            }
	           ...
	}
	...
	}

`WMS`的`addWindow`返回的是`addWindow`的各种状态，比如添加`Window`成功，无效的`display`等等，这些状态被定义在`WindowManagerGlobal`中。
 
- 注释1处根据`Window`的属性，调用`WMP`的`checkAddPermission`方法来检查权限，具体的实现在`PhoneWindowManager`的`checkAddPermission`方法中，如果没有权限则不会执行后续的代码逻辑。

- 注释2处通过`displayId`来获得窗口要添加到哪个`DisplayContent`上，如果没有找到`DisplayContent`，则返回`WindowManagerGlobal.ADD_INVALID_DISPLAY`这一状态，其中`DisplayContent`用来描述一块屏幕。

- 注释3处，`type`代表一个窗口的类型，它的数值介于`FIRST_SUB_WINDOW`和`LAST_SUB_WINDOW`之间（1000~1999），这个数值定义在`WindowManager`中，说明这个窗口是一个子窗口。

- 注释4处，`attrs.token`是`IBinder`类型的对象，`windowForClientLocked`方法内部会根据`attrs.token`作为key值从`mWindowMap`中得到该子窗口的父窗口。接着对父窗口进行判断，如果父窗口为null或者type的取值范围不正确则会返回错误的状态。


## 6.2 addWindow-2

	 ...
	            AppWindowToken atoken = null;
	            final boolean hasParent = parentWindow != null;
	            WindowToken token = displayContent.getWindowToken(
	                    hasParent ? parentWindow.mAttrs.token : attrs.token);//1
	            final int rootType = hasParent ? parentWindow.mAttrs.type : type;//2
	            boolean addToastWindowRequiresToken = false;
	
	            if (token == null) {
	                if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
	                    Slog.w(TAG_WM, "Attempted to add application window with unknown token "
	                          + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                }
	                if (rootType == TYPE_INPUT_METHOD) {
	                    Slog.w(TAG_WM, "Attempted to add input method window with unknown token "
	                          + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                }
	                if (rootType == TYPE_VOICE_INTERACTION) {
	                    Slog.w(TAG_WM, "Attempted to add voice interaction window with unknown token "
	                          + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                }
	                if (rootType == TYPE_WALLPAPER) {
	                    Slog.w(TAG_WM, "Attempted to add wallpaper window with unknown token "
	                          + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                }
	                ...
	                if (type == TYPE_TOAST) {
	                    // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
	                    if (doesAddToastWindowRequireToken(attrs.packageName, callingUid,
	                            parentWindow)) {
	                        Slog.w(TAG_WM, "Attempted to add a toast window with unknown token "
	                                + attrs.token + ".  Aborting.");
	                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                    }
	                }
	                final IBinder binder = attrs.token != null ? attrs.token : client.asBinder();
	                token = new WindowToken(this, binder, type, false, displayContent,
	                        session.mCanAddInternalSystemWindow);//3
	            } else if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {//4
	                atoken = token.asAppWindowToken();//5
	                if (atoken == null) {
	                    Slog.w(TAG_WM, "Attempted to add window with non-application token "
	                          + token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_NOT_APP_TOKEN;
	                } else if (atoken.removed) {
	                    Slog.w(TAG_WM, "Attempted to add window with exiting application token "
	                          + token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_APP_EXITING;
	                }
	            } else if (rootType == TYPE_INPUT_METHOD) {
	                if (token.windowType != TYPE_INPUT_METHOD) {
	                    Slog.w(TAG_WM, "Attempted to add input method window with bad token "
	                            + attrs.token + ".  Aborting.");
	                      return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                }
	            }
	      ...      


- 注释1处通过`displayContent`的`getWindowToken`方法来得到`WindowToken`。

- 注释2处，如果有父窗口就将父窗口的`type`值赋值给`rootType`，如果没有将当前窗口的`type`值赋值给`rootType`。

	接下来如果`WindowToken`为null，则根据`rootType`或者`type`的值进行区分判断，如果`rootType`值等于`TYPE_INPUT_METHOD`、`TYPE_WALLPAPER`等值时，则返回状态值`WindowManagerGlobal.ADD_BAD_APP_TOKEN`，说明`rootType`值等于`TYPE_INPUT_METHOD、TYPE_WALLPAPER`等值时是不允许`WindowToken`为null的。

	通过多次的条件判断筛选，最后会在注释3处隐式创建`WindowToken`，**这说明当我们添加窗口时是可以不向WMS提供`WindowToken`的**，前提是`rootType`和`type`的值不为前面条件判断筛选的值。`WindowToken`隐式和显式的创建肯定是要加以区分的

- 注释3处的第4个参数为false就代表这个`WindowToken`是隐式创建的。接下来的代码逻辑就是`WindowToken`不为null的情况，根据`rootType`和type的值进行判断，比如在注释4处判断如果窗口为应用程序窗口，

- 在注释5处会将`WindowToken`转换为专门针对应用程序窗口的`AppWindowToken`，然后根据`AppWindowToken`的值进行后续的判断。

## 6.3 addWindow-3

	   ...
	  final WindowState win = new WindowState(this, session, client, token, parentWindow,
	                    appOp[0], seq, attrs, viewVisibility, session.mUid,
	                    session.mCanAddInternalSystemWindow);//1
	            if (win.mDeathRecipient == null) {//2
	                // Client has apparently died, so there is no reason to
	                // continue.
	                Slog.w(TAG_WM, "Adding window client " + client.asBinder()
	                        + " that is dead, aborting.");
	                return WindowManagerGlobal.ADD_APP_EXITING;
	            }
	
	            if (win.getDisplayContent() == null) {//3
	                Slog.w(TAG_WM, "Adding window to Display that has been removed.");
	                return WindowManagerGlobal.ADD_INVALID_DISPLAY;
	            }
	
	            mPolicy.adjustWindowParamsLw(win.mAttrs);//4
	            win.setShowToOwnerOnlyLocked(mPolicy.checkShowToOwnerOnly(attrs));
	            res = mPolicy.prepareAddWindowLw(win, attrs);//5
	            ...
	            win.attach();
	            mWindowMap.put(client.asBinder(), win);//6
	            if (win.mAppOp != AppOpsManager.OP_NONE) {
	                int startOpResult = mAppOps.startOpNoThrow(win.mAppOp, win.getOwningUid(),
	                        win.getOwningPackage());
	                if ((startOpResult != AppOpsManager.MODE_ALLOWED) &&
	                        (startOpResult != AppOpsManager.MODE_DEFAULT)) {
	                    win.setAppOpVisibilityLw(false);
	                }
	            }
	
	            final AppWindowToken aToken = token.asAppWindowToken();
	            if (type == TYPE_APPLICATION_STARTING && aToken != null) {
	                aToken.startingWindow = win;
	                if (DEBUG_STARTING_WINDOW) Slog.v (TAG_WM, "addWindow: " + aToken
	                        + " startingWindow=" + win);
	            }
	
	            boolean imMayMove = true;
	            win.mToken.addWindow(win);//7
	             if (type == TYPE_INPUT_METHOD) {
	                win.mGivenInsetsPending = true;
	                setInputMethodWindowLocked(win);
	                imMayMove = false;
	            } else if (type == TYPE_INPUT_METHOD_DIALOG) {
	                displayContent.computeImeTarget(true /* updateImeTarget */);
	                imMayMove = false;
	            } else {
	                if (type == TYPE_WALLPAPER) {
	                    displayContent.mWallpaperController.clearLastWallpaperTimeoutTime();
	                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
	                } else if ((attrs.flags&FLAG_SHOW_WALLPAPER) != 0) {
	                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
	                } else if (displayContent.mWallpaperController.isBelowWallpaperTarget(win)) {
	                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
	                }
	            }
	         ...

- 在注释1处创建了`WindowState`，它存有窗口的所有的状态信息，在WMS中它代表一个窗口。从`WindowState`传入的参数，可以发现`WindowState`中包含了WMS、Session、WindowToken、父类的WindowState、LayoutParams等信息。

- 紧接着在注释2和3处分别判断请求添加窗口的客户端是否已经死亡、窗口的`DisplayContent`是否为null，如果是则不会再执行下面的代码逻辑。

- 注释4处调用了WMP的`adjustWindowParamsLw`方法，该方法的实现在`PhoneWindowManager`中，会根据窗口的type对窗口的LayoutParams的一些成员变量进行修改。

- 注释5处调用WMP的`prepareAddWindowLw`方法，用于准备将窗口添加到系统中。 

- 注释6处将`WindowState`添加到`mWindowMap`中。

- 注释7处将`WindowState`添加到该`WindowState`对应的`WindowToken`中(实际是保存在`WindowToken`的父类`WindowContainer`中)，这样`WindowToken`就包含了相同组件的`WindowState`。

## 6.4 addWindow总结

addWindow方法分了3个部分来进行讲解，主要就是做了下面4件事： 

1. 对所要添加的窗口进行检查，如果窗口不满足一些条件，就不会再执行下面的代码逻辑。 

2. `WindowToken`相关的处理，比如有的窗口类型需要提供WindowToken，没有提供的话就不会执行下面的代码逻辑，有的窗口类型则需要由WMS隐式创建`WindowToken`。 

3. `WindowState`的创建和相关处理，将`WindowToken`和`WindowState`相关联。 

4. 创建和配置`DisplayContent`，完成窗口添加到系统前的准备工作。
