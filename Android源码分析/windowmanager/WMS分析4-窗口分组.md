# WindowManagerService分析4

[Android窗口管理分析（3）：窗口分组及Z-order的确定](https://www.jianshu.com/p/90ede7b2a64a)

[Activity中的mToken](http://www.momoandy.com/2018/12/20/Activity%E4%B8%AD%E7%9A%84mToken/)

# 1. 简介

**在Android系统中，窗口是有分组概念的**

- 例如，Activity中弹出的所有`PopupWindow`会随着Activity的隐藏而隐藏，可以说这些都附属于Actvity的子窗口分组，对于Dialog也同样如此，只不过Dialog与Activity属于同一个分组。

	窗口类型划分：**应用窗口、子窗口、系统窗口**，`Activity`与`Dialog`都属于应用窗口，而`PopupWindow`属于子窗口，`Toast`、输入法等属于系统窗口。

	**只有应用窗口与系统窗口可以作为父窗口，子窗口不能作为子窗口的父窗口**，也就说Activity与Dialog或者系统窗口中可以弹出PopupWindow，但是PopupWindow不能在自己内部弹出PopupWindow子窗口。

日常开发中，一些常见的问题都同窗口的分组有关系，比如为什么新建Dialog的时候必须要用Activity的Context，而不能用Application的；为什么不能以PopupWindow的View为锚点弹出子PopupWindow？其实这里面就牵扯都Android的窗口组织管理形式

**本文主要包含以下几点内容**：

1. 窗口的分组管理 ：应用窗口组、子窗口组、系统窗口组

2. Activity、Dialg应用窗口及PopWindow子窗口的添加原理跟注意事项

3. 窗口的Z次序管理：窗口的分配序号、次序调整等

4. WMS中窗口次序分配如何影响SurfaceFlinger服务

# 2. 重要参数介绍

在[WMS分析3-具体案例.md]()的窗口添加过程中,依次调用`WindowManagerImpl->WindowManagerGlobal`. 以`WindowManagerGlobal.addView()`方法中的参数为例

    public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) 

1. `View view`:

	表示应用要添加到`WMG`管理的控件

2. `ViewGroup.LayoutParams params`:

	用来描述窗口属性. 实际传入的是`WindowManager.LayoutParams`

3. `Display display`:

	表示输出的显示设备

4. `Window parentWindow`:

	表示其父窗口

在`WindowManager.LayoutParams`中有俩个很重要的参数`type`和`token`

    public static class LayoutParams extends ViewGroup.LayoutParams implements Parcelable {

        public int type;

        /**
         * Identifier for this window.  This will usually be filled in for
         * you.
         */
        public IBinder token = null;

	}

- **`int type`用来描述窗口的类型**

- **`IBinder token`是标志窗口的分组,token相同的窗口属于同一组,在WMS中该值对应一个`WindowToken`**


**[Window属性介绍.md]()中介绍了`type`的取值**

# 3. 窗口的分组原理

如果用一句话概括窗口分组的话：**Android窗口是以token来进行分组的，同一组窗口握着相同的token**

- 什么是token呢？在 Android WMS管理框架中，`token`一个`IBinder`对象，`IBinder`在实体端与代理端会相互转换

	这里只看实体端，它的取值只有两种:

	1. **`ViewRootImpl`中`ViewRootImpl.W`**

		`ViewRootImpl.W`的实体对象在`ViewRootImpl`中实例化

			 static class W extends IWindow.Stub {.....}

	2. **`ActivityRecord`中的`ActivityRecord.Token`**

		`IApplicationToken.Stub`在`ActivityManagerService`端实例化，之后被AMS添加到WMS服务中去，作为Activity应用窗口的键值标识

			static class Token extends IApplicationToken.Stub {.....}

之前说过`Activity`跟`Dialog`属于同一分组，现在就来看一下`Activity`跟`Dialog`的`token`是如何复用的，这里的复用分为APP端及WMS服务端

关于窗口的添加流程之前已经分析过，这里只跟随窗口token来分析窗口的分组，我们知道在WMS端，`WindowState`与窗口的一一对应，而`WindowToken`与窗口分组有关，查看两者的定义：


## 3.1 WindowState


	/** A window in the window manager. */
	class WindowState extends WindowContainer<WindowState> implements WindowManagerPolicy.WindowState {
	
	    final WindowManagerService mService;
	    final WindowManagerPolicy mPolicy;
	    final Context mContext;
	    final Session mSession;
	    final IWindow mClient;
	    final int mAppOp;
	    WindowToken mToken;
	    // The same object as mToken if this is an app window and null for non-app windows.
	    AppWindowToken mAppToken;

		..........
	}

- `AppWindowToken mAppToken`的取值有俩种情况

	1. 如果是一个应用程序窗口(`Application Window`),则`mAppToken`的值和`mToken`是相同的对象

	2. 如果不是一个应用程序窗口(`Application Window`),则`mAppToken`的值为null

- `IWindow mClient`表示当前窗口的Binder代理,用来进行IPC

## 3.2 WindowToken
	/**
	 * Container of a set of related windows in the window manager. Often this is an AppWindowToken,
	 * which is the handle for an Activity that it uses to display windows. For nested windows, there is
	 * a WindowToken created for the parent window to manage its children.
	 */
	class WindowToken extends WindowContainer<WindowState> {

	    // The window manager!
	    protected final WindowManagerService mService;
	
	    // The actual token.
	    final IBinder token;
	
	    // The type of window this token is for, as per WindowManager.LayoutParams.
	    final int windowType;

		........
	}

- `WindowToken`继承自`WindowContainer`,其泛型是`WindowState`说明保存的都是`WindowState`

## 3.3 WindowContainer

	class WindowContainer<E extends WindowContainer> implements Comparable<WindowContainer> {
	
	    /**
	     * The parent of this window container.
	     * For removing or setting new parent {@link #setParent} should be used, because it also
	     * performs configuration updates based on new parent's settings.
	     */
	    private WindowContainer mParent = null;
	
	    // List of children for this window container. List is in z-order as the children appear on
	    // screen with the top-most window container at the tail of the list.
	    protected final WindowList<E> mChildren = new WindowList<E>();
		
		.......
	
	}

- `WindowList<E> mChildren`储存`WindowState`(WS和窗口一一对应)

- `WindowContainer mParent` 表示父窗口信息


![](http://ww1.sinaimg.cn/large/6ab93b35gy1g1ilyqgngaj20rs0m30t2.jpg)


## 3.4 WMS.addWindow()

**WMS的窗口分组逻辑主要是在其`addWindow()`方法中:**


    public int addWindow(Session session, IWindow client, int seq,
            WindowManager.LayoutParams attrs, int viewVisibility, int displayId,
            Rect outContentInsets, Rect outStableInsets, Rect outOutsets,
            InputChannel outInputChannel) {

        WindowState parentWindow = null;
        final int type = attrs.type;
        synchronized(mWindowMap) {
			// 防止重复添加
			// IWindow是一个Binder代理,在WMS端,一个窗口只可能对应一个IWindow代理,这是由Binder通信机制保证的,因此不能重复添加,否则会报错
            if (mWindowMap.containsKey(client.asBinder())) {
                return WindowManagerGlobal.ADD_DUPLICATE_ADD;
            }
			// 如果窗口类型是 子窗口,但是又找不到其父窗口..那么就GG
            if (type >= FIRST_SUB_WINDOW && type <= LAST_SUB_WINDOW) {
				// 找到其父窗口的WindowState
                parentWindow = windowForClientLocked(null, attrs.token, false);
                if (parentWindow == null) {
                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN;
                }
                if (parentWindow.mAttrs.type >= FIRST_SUB_WINDOW
                        && parentWindow.mAttrs.type <= LAST_SUB_WINDOW) {
                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN;
                }
            }
		
            AppWindowToken atoken = null;

			// 如果存在父窗口,那么就选择父窗口的WindowToken,这样就可以对它们使用相同的策略
			// WindowToken 要么通过 DisplayContent.addWindowToken()添加
			// 要么在特殊情况下,WMS会帮助其创建,见下面的逻辑
            WindowToken token = displayContent.getWindowToken(
                    hasParent ? parentWindow.mAttrs.token : attrs.token);
            // 如果这是子窗口,那么希望子窗口和父窗口使用同样的类型检查规则
            final int rootType = hasParent ? parentWindow.mAttrs.type : type;

			.......省略对rootType的检查,当WIndowToken token 为空时,可能会去创建WindowToken.......

			// 这个WindowState 就是表示当前窗口,如果存在父窗口,那么其代表的WindowState 就会在子窗口的构造函数中进行关联
            final WindowState win = new WindowState(this, session, client, token, parentWindow,
                    appOp[0], seq, attrs, viewVisibility, session.mUid,
                    session.mCanAddInternalSystemWindow);

            res = mPolicy.prepareAddWindowLw(win, attrs);
            win.attach();
			// 当前请求添加窗口的进程的IBinder 对应 WindowState
            mWindowMap.put(client.asBinder(), win);
            final AppWindowToken aToken = token.asAppWindowToken();
            if (type == TYPE_APPLICATION_STARTING && aToken != null) {
                aToken.startingWindow = win;
            }
			// 这里的mToken  如果存在父窗口,那就是父窗口的WindowToken
			// 将当前待处理窗口的WindowState 添加到当前的WindowToken中
            win.mToken.addWindow(win);

	}

- **首先明确,一个窗口对应一个`WindowState`,`WindowToken`用来对`WindowState`分组**

- **子窗口的`WS`会被添加到父窗口的`WS`,然后父窗口的`WS`会被添加到代表这一系列窗口的`WindowToken`中**

### 3.4.1 WindowToken.addWIndow()

    void addWindow(final WindowState win) {
		// 判断WS对应的窗口是否是子窗口
        if (win.isChildWindow()) {
			// 子窗口早就被添加到父窗口了.. 所以这里不需要再被处理
			// 在子窗口对应的WindowState被创建时,父窗口就从构造函数中传进去了
            return;
        }

		// 判断WindowToken 是否已经添加了当前WindowState(当前窗口)
        if (!mChildren.contains(win)) {
            addChild(win, mWindowComparator);
            mService.mWindowsChanged = true;
        }
    }

- 返回`mIsChildWindow`的值

	**`WindowState`的构造函数中会判断当前对应的窗口是否是子窗口,并对`mIsChildWindow`进行赋值**

- `mChildren` 包含了在当前`WindowToken`下的一组`WindowState`. **注意:这里的`WindowState`可能还包含子窗口的`WindowState`**


### 3.4.2 WindowContainer.addChild()

    protected void addChild(E child, Comparator<E> comparator) {
		// 说明当前WindowState已经设置了WindowToken
        if (child.getParent() != null) {
            throw new IllegalArgumentException(".....");
        }
		......省略序列代码......
		// 添加到集合中
		mChildren.add(child);
		// 将当前窗口(WindowState)和WindowToken进行关联
        child.setParent(this);
    }


# 4. Activity 对应的Token的介绍

AMS通过`ActivityStarter`为Activity创建`ActivityRecord`的时候(构造函数中)，会新建`Token extends IApplicationToken.Stub appToken`对象

    ActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromPid,
            int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType,
            ActivityInfo aInfo, Configuration _configuration,
            ActivityRecord _resultTo, String _resultWho, int _reqCode,
            boolean _componentSpecified, boolean _rootVoiceInteraction,
            ActivityStackSupervisor supervisor, ActivityOptions options,
            ActivityRecord sourceRecord) {
        appToken = new Token(this);

		.......................
	}

    static class Token extends IApplicationToken.Stub {
		............
	}

## 4.1 Activity对应的Token的传递

AMS经过`ActivityStarter`,`ActivityStackSupervisor`和`ActivityStack`的一系列跳转之后,最终调用`ActivityStackSupervisor.realStartActivityLocked()`,通过`app.thread`(即`IApplicationThread`)调用其`scheduleLaunchActivity()`方法和应用进程进行IPC

- **这里的`app.thread`是`IApplicationThread.Stub`类型,注意是`ActivityThread`类的内部类`ApplicationThread`**


        // we use token to identify this activity without having to send the
        // activity itself back to the activity manager. (matters more with ipc)
        @Override
        public final void scheduleLaunchActivity(Intent intent, IBinder token, int ident,
                ActivityInfo info, Configuration curConfig, Configuration overrideConfig,
                CompatibilityInfo compatInfo, String referrer, IVoiceInteractor voiceInteractor,
                int procState, Bundle state, PersistableBundle persistentState,
                List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents,
                boolean notResumed, boolean isForward, ProfilerInfo profilerInfo) {
			..................
            ActivityClientRecord r = new ActivityClientRecord();

            r.token = token;
			.................
            sendMessage(H.LAUNCH_ACTIVITY, r);
        }

- 参数中的`IBinder token`是AMS中的`ActivityRecord.appToken`, 也就是在`ActivityRecord`构造函数中创建的`Token`(该`Token`类同样继承自`IApplicationToken.Stub`)


在`ActivityThread`类中经过一系列方法调用,最终在`ActivityThread.performLaunchActivity()`方法中通过调用`Activity.attach()`方法将`Token`注入到Activity.

### 4.1.1 过程概述

AMS向App进程发起IPC，`ActivityThread`拿到`Token`在App进程的Binder代理，创建好Activity后在`attach()`方法中注入到Activity中
	

## 4.2 Token对于AMS和App的作用

**AMS端维护一份`ProcessRecord`数据结构，`ProcessRecord`代表一个进程的所有信息，包含了这个进程的`TaskRecord`和`ActivityRecord`和其他系统组件等。**

**App进程则在`ActivityThread`维护一个`key`和`value`为`IBinder`和`ActivityClientRecord`的`ArrayMap mActivities`，这里的`IBinder`就是AMS端的那个`Token`，`ActivityClientRecord`则类似AMS端的`ActivityRecord`，存储着Activity实例和Activity相关信息。**

**这样，无论是`AMS`还是`App`进程的`ActivityThread`均可以使用`Token`这个`Binder`查找到准确的Activity，然后进行相应操作。**

### 4.2.1 应用场景

启动一个新的Activity：App携带当前Activity的`Token`向AMS发起IPC，AMS准备创建并启动新的Activity，在新的Activity可见之前就会携带这个`Token`向App进程通信暂停上一个Activity，App进程收到通信后通过`Token`查找`ActivityThread`存储的列表，找到对应Activity后做相应处理并回调这个Activity的`onPause()`，然后AMS才会让新启动的Activity进入可见状态。


# 5. Activity 对应的Token 绑定到客户端Window的过程

## 5.1 Token绑定到Activity

在`ActivityThread.performLaunchActivity()`方法中通过调用`Activity.attach()`方法将`Token`注入到Activity.

    final void attach(Context context, ActivityThread aThread,
            Instrumentation instr, IBinder token, int ident,
            Application application, Intent intent, ActivityInfo info,
            CharSequence title, Activity parent, String id,
            NonConfigurationInstances lastNonConfigurationInstances,
            Configuration config, String referrer, IVoiceInteractor voiceInteractor,
            Window window, ActivityConfigCallback activityConfigCallback) {

		// 当前Activity 对应一个PhoneWIndow
        mWindow = new PhoneWindow(this, window, activityConfigCallback);
        mWindow.setWindowControllerCallback(this);
        mWindow.setCallback(this);
        mToken = token;
		// PhoneWIndow保存了token
        mWindow.setWindowManager(
                (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
                mToken, mComponent.flattenToString(),
                (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0)
        mWindowManager = mWindow.getWindowManager();
		.........
	}

- 这里参数中的`IBinder token`是AMS中通过`ActivityRecord`生成的`Token`,由于在应用进程中,所以这里应该是BInder代理

- `PhoneWindow`保存了`Token`


## 5.1 PhoneWindow.setWIndowManager()

    public void setWindowManager(WindowManager wm, IBinder appToken, String appName,
            boolean hardwareAccelerated) {
        mAppToken = appToken;
        mAppName = appName;
        mHardwareAccelerated = hardwareAccelerated
                || SystemProperties.getBoolean(PROPERTY_HARDWARE_UI, false);
        if (wm == null) {
            wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        }
        mWindowManager = ((WindowManagerImpl)wm).createLocalWindowManager(this);
    }

## 5.2 Token绑定到Window

1. 在`ActivityThread`的`performResumeActivity()`方法中依次完成Activity的`onRestart()、onStart()、onResume()`回调

2. 在`handleResumeActivity()`方法中将`PhoneWindow`中的`DecorView`视图状态设置为`INVISIBLE`

3. 之后调用`wm.addView(decor, l)`添加视图，其中`wm`是`WindowManagerImpl`

	- 在`attach()`方法中，通过`PhoneWindow`构造了`WindowManagerImpl`对象并赋值给了`Activity`；

4. 调用`r.activity.makeVisible()`设置页面View为可见。



    final void handleResumeActivity(IBinder token,
            boolean clearHide, boolean isForward, boolean reallyResume, int seq, String reason) {
		// 包含了Token 信息
        ActivityClientRecord r = mActivities.get(token);
		// 经过一系列回调,修改了ActivityClientRecord的信息
        r = performResumeActivity(token, clearHide, reason);
        if (r != null) {
            final Activity a = r.activity;

            boolean willBeVisible = !a.mStartedActivity;
            if (!willBeVisible) {
                try {
                    willBeVisible = ActivityManager.getService().willActivityBeVisible(
                            a.getActivityToken());
                } catch (RemoteException e) {
                }
            }
            if (r.window == null && !a.mFinished && willBeVisible) {
                r.window = r.activity.getWindow();
                View decor = r.window.getDecorView();
                decor.setVisibility(View.INVISIBLE);
                ViewManager wm = a.getWindowManager();
				// 窗口参数信息
                WindowManager.LayoutParams l = r.window.getAttributes();
                a.mDecor = decor;
                l.type = WindowManager.LayoutParams.TYPE_BASE_APPLICATION;

                if (a.mVisibleFromClient) {
                    if (!a.mWindowAdded) {
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

            // Tell the activity manager we have resumed.
            if (reallyResume) {
                try {
                    ActivityManager.getService().activityResumed(token);
                } catch (RemoteException ex) {
                    throw ex.rethrowFromSystemServer();
                }
            }

        } 
		.......................
    }

- `wm`是`WindowManagerImpl`,在`Activity.attach()`方法中通过`PhoneWindow`获取

	WMI通过桥接模式 将具体的调用交给了`WindowManagerGlobal`

- 这里和Token绑定最重要的一部分就是`wm.addView(decor, l)`,这里有俩个比较重要的参数

	1. `WindowManager.LayoutParams l`

	2. `DecorView decor`

### 5.2.1 WindowManagerImpl.addView()

    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }

- `mParentWindow`是在`WMI`的构造函数中被传入

	在`Activity.attach()`方法中,会调用`PhoneWindow.setWindowManager()`方法,然后在该方法中 调用了`WindowManagerImpl.createLocalWindowManager()`将`PhoneWindow`传入

### 5.2.1 WindowManagerGlobal.addView()

    public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) {
		......省略参数的检查...................

        final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
		//Activity.attach()中创建的 PhoneWindow
        if (parentWindow != null) {
			// 在这里绑定了token
            parentWindow.adjustLayoutParamsForSubWindow(wparams);
        } else {

            final Context context = view.getContext();
            if (context != null
                    && (context.getApplicationInfo().flags
                            & ApplicationInfo.FLAG_HARDWARE_ACCELERATED) != 0) {
                wparams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            }
        }

        ViewRootImpl root;
        View panelParentView = null;

        synchronized (mLock) {
        	..................

            root = new ViewRootImpl(view.getContext(), display);

            view.setLayoutParams(wparams);

            mViews.add(view);
            mRoots.add(root);
            mParams.add(wparams);

            // do this last because it fires off messages to start doing things
            try {
                root.setView(view, wparams, panelParentView);
            } catch (RuntimeException e) {
            }
        }
    }

- `WindowManagerGlobal.addView()`做了4件事。

	1. 调用`PW.adjustLayoutParamsForSubWindow()`为`WindowManager.LayoutParams`绑定Token；

	2. 构造ViewRootImpl(简称VRI)；

	3. 为DecorView设置`WindowManager.LayoutParams`

	4. 把`DecorView、VRI和WindowManager.LayoutParams`添加到WMG中的List中缓存起来；

	5. 调用`root.setView(view, wparams, panelParentView)`做进一步设置；

### 5.2.2 PhoneWindow.adjustLayoutParamsForSubWindow()




# 6. Activity 对应的WindowToken的创建

1. 在`AMS.startActivityLocked()`方法中会通过调用`ActivityRecord`的`createWindowContainer()`去创建`AppWindowContainerController`

2. 在`AppWindowContainerController`的构造函数中,会去创建`AppWindowToken`

3. `AppWindowToken`的构造函数中会调用其父类`WindowToken`的构造函数. 

4. `WindowToken`的构造函数会调用`onDisplayChanged()`方法,在这里将调用`DisplayContent`的`reParentWindowToken()`

5. 方法`reParentWindowToken`会调用`addWindowToken()`,将应用的Binder 和其对应的`WindowToken`保存到一个`HashMap<IBinder, WindowToken> mTokenMap`.**后面WMS添加Window时会和mTokenMap中的Token进行匹配验证**

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g1jmwjp98gj20y90e9abj.jpg)

随后，通过`Binder`通信将`IApplicationToken`传递给APP端，在通知`ActivityThread`新建`Activity`对象之后，利用`Activity`的`attach()`方法添加到`Activity`中

