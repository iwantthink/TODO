# WindowManagerService分析4

[Android窗口管理分析（3）：窗口分组及Z-order的确定](https://www.jianshu.com/p/90ede7b2a64a)

[Activity中的mToken](http://www.momoandy.com/2018/12/20/Activity%E4%B8%AD%E7%9A%84mToken/)

# 1. 窗口概念简介

**在Android系统中，窗口是有分组概念的**

- 例如，Activity中弹出的所有`PopupWindow`会随着Activity的隐藏而隐藏，可以说其附属于Actvity的子窗口分组，对于Dialog也同样如此，只不过Dialog与Activity属于同一个分组。

窗口类型划分：**应用窗口、子窗口、系统窗口**，`Activity`与`Dialog`都属于应用窗口，而`PopupWindow`属于子窗口，`Toast`、输入法等属于系统窗口。

- **只有应用窗口与系统窗口可以作为父窗口，子窗口不能作为子窗口的父窗口**，也就说`Activity`与`Dialog`或者系统窗口中可以弹出`PopupWindow`，但是`PopupWindow`不能在自己内部弹出`PopupWindow`子窗口。

日常开发中，一些常见的问题都同窗口的分组有关系,其实这里面就牵扯都Android的窗口组织管理形式!

1. 比如为什么新建`Dialog`的时候必须要用Activity的Context，而不能用Application的?

2. 为什么不能以`PopupWindow`的View为锚点弹出子`PopupWindow`？

**本文主要包含以下几点内容**：

1. 窗口的分组管理 ：应用窗口组、子窗口组、系统窗口组

2. Activity、Dialg应用窗口及PopWindow子窗口的添加原理跟注意事项

3. 窗口的Z次序管理：窗口的分配序号、次序调整等

4. WMS中窗口次序分配如何影响SurfaceFlinger服务

# 2. 重要参数介绍

根据[WMS分析3-具体案例.md]()的分析结果可知

- **窗口添加过程中,依次会调用`WindowManagerImpl.addView()`,`WindowManagerGlobal.addView()`,`ViewRootImpl.setView()`,  `Session.addToDisplay()`,`WMS.addWindow()`**

## 2.1 `WMG.addView()`参数介绍

以`WindowManagerGlobal.addView()`方法中的参数为例介绍几个重要参数

    public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) 

1. `View view`:

	表示待添加到WMS中的控件

2. `ViewGroup.LayoutParams params`:

	用来描述窗口属性. 实际传入的应该是`WindowManager.LayoutParams`类型

3. `Display display`:

	表示输出的显示设备

4. `Window parentWindow`:

	表示其父窗口,具体的值可能为空!

	在`Activity.attach()`方法中会调用`mWindow.setWIndowManager()`,这个`mWindow`就是`PhoneWindow`

## 2.2 WindowManager.LayoutParams中的参数

在`WM.LayoutParams`中有俩个很重要的参数`type`和`token`

    public static class LayoutParams extends ViewGroup.LayoutParams implements Parcelable {
        public int type;

        //Identifier for this window.  This will usually be filled in for you
        public IBinder token = null;
	}

- `int type`:

	用来描述窗口的类型. 主要分为三类:应用窗口,系统窗口和子窗口

	**[Window属性介绍.md]()中详细介绍了`type`的取值**

- `IBinder token`:

	是窗口进行分组的关键,token相同的窗口属于同一组,在WMS中该值对应一个`WindowToken`


# 3. 窗口的分组原理

如果用一句话概括窗口分组的话：

- **Android窗口是以`token`来进行分组的，同一组窗口握着相同的`token`**

**什么是token呢**？

- 在 Android WMS管理框架中，`token`一个`IBinder`对象，`IBinder`在实体端与代理端会相互转换,它的取值只有两种

	1. **`ViewRootImpl`中`ViewRootImpl.W`**

		`ViewRootImpl.W`的实体对象在`ViewRootImpl`构造函数中初始化

			 static class W extends IWindow.Stub {.....}

	2. **`ActivityRecord`中的`ActivityRecord.Token`**

		`IApplicationToken.Stub`在`ActivityManagerService`端实例化，之后被AMS添加到WMS服务中去，**主要作用是作为Activity应用窗口的键值标识**

			static class Token extends IApplicationToken.Stub {.....}

		- **注意:**`ActivityThread`中也有一个`IApplicationThread.Stub`的实现类`ApplicationThread`,该类提供给`AMS`对应用进程进行IPC

关于窗口的添加流程之前已经分析过，这里只跟随窗口`token`来分析窗口的分组，我们知道在WMS端，`WindowState`与窗口的一一对应，而`WindowToken`与窗口分组有关，查看两者的定义：

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


# 4. 窗口分组逻辑介绍

**WMS的窗口分组逻辑主要是在其`addWindow()`方法中:**

## 4.1 `WMS.addWindow()` - part1

    public int addWindow(Session session, IWindow client, int seq,
            WindowManager.LayoutParams attrs, int viewVisibility, int displayId,
            Rect outContentInsets, Rect outStableInsets, Rect outOutsets,
            InputChannel outInputChannel) {

		// 表示父类WindowState
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

## 4.2 `WindowToken.addWindow()` - part2

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


###  4.2.1  WindowContainer.addChild()

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


# 5. Activity对应的`ActivityRecord.Token`

## 5.1 创建过程

**AMS通过`ActivityStarter`为Activity创建`ActivityRecord`的时候,在`ActivityRecord`的构造函数中会新建`Token extends IApplicationToken.Stub appToken`对象**

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

## 5.2 传递过程概述

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
			//ActivityRecord.Token
            r.token = token;
			.................
            sendMessage(H.LAUNCH_ACTIVITY, r);
        }

- 参数中的`IBinder token`是AMS中的`ActivityRecord.appToken`, 也就是在`ActivityRecord`构造函数中创建的`Token`(该`Token`类同样继承自`IApplicationToken.Stub`)


**在`ActivityThread`类中经过一系列方法调用,最终在`ActivityThread.performLaunchActivity()`方法中通过调用`Activity.attach()`方法将`Token`注入到Activity**.


# 6. Token对于AMS和App的作用

**AMS端维护一份`ProcessRecord`数据结构，`ProcessRecord`代表一个进程的所有信息，包含了这个进程的`TaskRecord`和`ActivityRecord`和其他系统组件等。**

**App进程则在`ActivityThread`维护一个`key`和`value`为`IBinder`和`ActivityClientRecord`的`ArrayMap mActivities`**

- 这里的`IBinder`就是AMS端的那个`Token`，`ActivityClientRecord`则类似AMS端的`ActivityRecord`，存储着Activity实例和Activity相关信息。

**这样，无论是`AMS`还是`App`进程的`ActivityThread`均可以使用`Token`这个`Binder`查找到准确的Activity，然后进行相应操作。**

## 6.1 应用场景

启动一个新的Activity：App携带当前Activity的`Token`向AMS发起IPC，AMS准备创建并启动新的Activity，在新的Activity可见之前就会携带这个`Token`向App进程通信暂停上一个Activity，App进程收到通信后通过`Token`查找`ActivityThread`存储的列表，找到对应Activity后做相应处理并回调这个Activity的`onPause()`，然后AMS才会让新启动的Activity进入可见状态。


# 7. ActivityRecord.Token的传递分析

## 7.1 ActivityRecord.Token绑定到Activity

在`ActivityThread.performLaunchActivity()`方法中通过调用`Activity.attach()`方法将`Token`注入到Activity.

    final void attach(Context context, ActivityThread aThread,
            Instrumentation instr, IBinder token, int ident,
            Application application, Intent intent, ActivityInfo info,
            CharSequence title, Activity parent, String id,
            NonConfigurationInstances lastNonConfigurationInstances,
            Configuration config, String referrer, IVoiceInteractor voiceInteractor,
            Window window, ActivityConfigCallback activityConfigCallback) {

		// 当前Activity 对应一个PhoneWindow
        mWindow = new PhoneWindow(this, window, activityConfigCallback);
        mWindow.setWindowControllerCallback(this);
        mWindow.setCallback(this);
		// Token 被绑定到了 Activity
        mToken = token;
		// 将token保存到PhoneWindow中
        mWindow.setWindowManager(
                (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
                mToken, mComponent.flattenToString(),
                (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0)
        mWindowManager = mWindow.getWindowManager();
		.........
	}

- 这里参数中的`IBinder token`是`AMS`中通过`ActivityRecord`生成的`Token`,由于在应用进程中,所以这里应该是Binder的远程代理对象

- **对`Token`的处理 主要在`PhoneWindow.setWindowManager()`中**

## 7.2 ActivityRecord.Token绑定到Window(PhoneWindow)

### 7.2.1 PhoneWindow.setWindowManager()

    public void setWindowManager(WindowManager wm, IBinder appToken,
	 String appName,boolean hardwareAccelerated) {
		// 注意这个mAppToken 就是AMS创建的Token
        mAppToken = appToken;
        mAppName = appName;
        mHardwareAccelerated = hardwareAccelerated
                || SystemProperties.getBoolean(PROPERTY_HARDWARE_UI, false);
        if (wm == null) {
            wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        }
		// 将当前PhoneWindow 传入 WindowManagerImpl,赋值给它的成员变量`mParentWindow`
        mWindowManager = ((WindowManagerImpl)wm).createLocalWindowManager(this);
    }

- `mAppToken`即`AMS`为当前`Activity`创建的`Token`

## 7.3 ActivityRecord.Token绑定到WindowManagerService

1. 在`ActivityThread`的`performResumeActivity()`方法中依次完成Activity的`onRestart()、onStart()、onResume()`回调

2. 在`handleResumeActivity()`方法中将`PhoneWindow`中的`DecorView`视图状态设置为`INVISIBLE`

3. 之后会调用`wm.addView(decor, l)`添加视图，其中`wm`是`WindowManagerImpl`

	- 在`attach()`方法中，通过`PhoneWindow`构造了`WindowManagerImpl`对象并赋值给了`Activity`；

4. 调用`r.activity.makeVisible()`设置页面View为可见。



    final void handleResumeActivity(IBinder token,
            boolean clearHide, boolean isForward, boolean reallyResume, int seq, String reason) {
		// 通过AMS-Token  获取到保存 Activity相关信息的ActivityClientRecord
        ActivityClientRecord r = mActivities.get(token);
		// 经过一系列回调,修改了ActivityClientRecord的信息
        r = performResumeActivity(token, clearHide, reason);
        if (r != null) {
            final Activity a = r.activity;
			..............
            if (r.window == null && !a.mFinished && willBeVisible) {
				// Activity的Window在其attach()方法中被创建
                r.window = r.activity.getWindow();
				// DecorView 在 Activity.onCreate()中被创建
                View decor = r.window.getDecorView();
                decor.setVisibility(View.INVISIBLE);
                ViewManager wm = a.getWindowManager();
				// 获取窗口参数信息
                WindowManager.LayoutParams l = r.window.getAttributes();
				// 将DecorView 绑定到Activity
                a.mDecor = decor;
				// Activity 的窗口类型是 应用窗口
                l.type = WindowManager.LayoutParams.TYPE_BASE_APPLICATION;

                if (a.mVisibleFromClient) {
                    if (!a.mWindowAdded) {
                        a.mWindowAdded = true;
						// 调用WindowManagerImpl
                        wm.addView(decor, l);
                    } else {
						...........
                    }
                }
			.........
        } 
		.......................
    }

- `wm`是`WindowManagerImpl`,在`Activity.attach()`方法中通过`PhoneWindow`获取

	WMI通过桥接模式 将具体的调用交给了`WindowManagerGlobal`

- 这里和Token绑定最重要的一部分就是`wm.addView(decor, l)`,这里有俩个比较重要的参数

	1. `WindowManager.LayoutParams l`

	2. `DecorView decor`

### 7.3.1 WindowManagerImpl.addView()

    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }

- `mParentWindow`是在`WMI`的构造函数中被传入

	**在`Activity.attach()`方法中,会调用`PhoneWindow.setWindowManager()`方法,然后在该方法中 调用了`WindowManagerImpl.createLocalWindowManager()`将`PhoneWindow`传入**

### 7.3.2 WindowManagerGlobal.addView()

    public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) {
		......省略参数的检查...................

        final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
		
        if (parentWindow != null) {
			// 在这里将ActivityRecord.Token绑定到LayoutParams
            parentWindow.adjustLayoutParamsForSubWindow(wparams);

        } else {
			...................
        }

        ViewRootImpl root;
        View panelParentView = null;

        synchronized (mLock) {
        	..................

            root = new ViewRootImpl(view.getContext(), display);
			// WindowManager.LayoutParams 绑定到 DecorView
            view.setLayoutParams(wparams);
			// 存储数据
            mViews.add(view);
            mRoots.add(root);
            mParams.add(wparams);

            try {
                root.setView(view, wparams, panelParentView);
            } catch (RuntimeException e) {
            }
        }
    }

- `WindowManagerGlobal.addView()`做了4件事。

	1. 调用`PW.adjustLayoutParamsForSubWindow()`为`WindowManager.LayoutParams`的成员变量`token`设置值；

	2. 构造`ViewRootImpl`(简称VRI)；

	3. 为`DecorView`设置`WindowManager.LayoutParams`

	4. 把`DecorView、VRI和WindowManager.LayoutParams`添加到WMG中的List中缓存起来；

	5. 调用`root.setView(view, wparams, panelParentView)`做进一步设置；

### 7.3.3 PhoneWindow.adjustLayoutParamsForSubWindow()

    void adjustLayoutParamsForSubWindow(WindowManager.LayoutParams wp) {
        CharSequence curTitle = wp.getTitle();
		// 类型为 子窗口
        if (wp.type >= WindowManager.LayoutParams.FIRST_SUB_WINDOW &&
                wp.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
            if (wp.token == null) {
				// 返回与PhoneWindow绑定的 DecorView
                View decor = peekDecorView();
                if (decor != null) {
					// 这里获取到的是 IWindow(W extends IWindow.Stub )
                    wp.token = decor.getWindowToken();
                }
            }
 			..........设置title.................
            }
		// 类型为 系统窗口
        } else if (wp.type >= WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW &&
                wp.type <= WindowManager.LayoutParams.LAST_SYSTEM_WINDOW) {
            // We don't set the app token to this system window because the life cycles should be independent
            // If an app creates a system window and then the app goes to the stopped state
            // , the system window should not be affected (can still show and receive input events

            if (curTitle == null || curTitle.length() == 0) {
                final StringBuilder title = new StringBuilder(32);
                title.append("Sys").append(wp.type);
                if (mAppName != null) {
                    title.append(":").append(mAppName);
                }
                wp.setTitle(title);
            }
		// 应用窗口
        } else {
			// 对于Activity来说,wp.token就是AMS端传过来的ActivityRecord.Token
			// mAppToken  setWindowManager方法中传入
            if (wp.token == null) {
                wp.token = mContainer == null ? mAppToken : mContainer.mAppToken;
            }
            if ((curTitle == null || curTitle.length() == 0)
                    && mAppName != null) {
                wp.setTitle(mAppName);
            }
        }
        if (wp.packageName == null) {
            wp.packageName = mContext.getPackageName();
        }
        if (mHardwareAccelerated ||(mWindowAttributes.flags & FLAG_HARDWARE_ACCELERATED) != 0) {
            wp.flags |= FLAG_HARDWARE_ACCELERATED;
        }
    }

根据不同的窗口类型,`WindowManager.LayoutParams`中的成员变量`token`的值也不同

1. **应用窗口的`WindowManager.LayoutParams.token`的值是`AMS`为当前Activity创建的`Token`**

2. **子窗口的`LayoutParams.token`的值是 `IWindow(W extends IWindow.Stub )`,其使用的是父窗口的`Token`**

#### 7.3.3.1 `View.getWindowToken()`

在`ViewRootImpl`类的`performTraversals()`方法中,会调用`dispatchAttachedToWindow()`方法将`View.AttachInfo`保存到`DecorView`中

    private void performTraversals() {
        final View host = mView;
		.......
        if (mFirst) {
			...........
            host.dispatchAttachedToWindow(mAttachInfo, 0);
        } 
		..........
	}



### 7.3.4 ViewRootImpl中对Token的操作

	
	public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
	    synchronized (this) {
	        if (mView == null) {
	            mView = view;
	            ...
	            try {
					...........
	                res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
	                        getHostVisibility(), mDisplay.getDisplayId(), mWinFrame,
	                        mAttachInfo.mContentInsets, mAttachInfo.mStableInsets,
	                        mAttachInfo.mOutsets, mAttachInfo.mDisplayCutout, mInputChannel);
	            }
	            //把VRI绑定到DecorView。
	            view.assignParent(this);
	            ...
	        }
	    }
	}

#### 7.3.4.1 `Session mWindowSession`的创建

在`ViewRootImpl`的构造函数中创建了`mWindowSession`,其具体实现类是`Session`,是一个Binder,运行于`SystemServer`进程中

每一个进程对应一个`Session`代理,`WindowManagerGlobal`维护了一个`Session`的远程Binder代理对象(**单例**)

- **具体可以参考[WMS分析3-具体案例---3.3.md]()**

## 7.4 `Session.addToDisplay()`

    @Override
    public int addToDisplay(IWindow window, int seq, WindowManager.LayoutParams attrs,
            int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets,
            Rect outOutsets, InputChannel outInputChannel) {
        return mService.addWindow(this, window, seq, attrs, viewVisibility, displayId,
                outContentInsets, outStableInsets, outOutsets, outInputChannel);
    }

## 7.5 WindowManagerService.addWindow()

**参考[WMS分析2-WMS的使用.md]()**


## 7.6 总结

Activity通过`PhoneWindow`把`Token`放到`DecorView`的`WindowManager.LayoutParams`中。然后App进程向WMS发起IPC，WMS拿到Token保存到WindowState，最终存储到WMS的mWindowMap中。



# 8. Activity 对应的WindowToken的创建

1. 在`AMS.startActivityLocked()`方法中会通过调用`ActivityRecord`的`createWindowContainer()`去创建`AppWindowContainerController`

2. 在`AppWindowContainerController`的构造函数中,会去创建`AppWindowToken`

3. `AppWindowToken`的构造函数中会调用其父类`WindowToken`的构造函数. 

4. `WindowToken`的构造函数会调用`onDisplayChanged()`方法,在这里将调用`DisplayContent`的`reParentWindowToken()`

5. 方法`reParentWindowToken`会调用`addWindowToken()`,将应用的Binder 和其对应的`WindowToken`保存到一个`HashMap<IBinder, WindowToken> mTokenMap`.**后面WMS添加Window时会和mTokenMap中的Token进行匹配验证**

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g1jmwjp98gj20y90e9abj.jpg)

随后，通过`Binder`通信将`IApplicationToken`传递给APP端，在通知`ActivityThread`新建`Activity`对象之后，利用`Activity`的`attach()`方法添加到`Activity`中

# 9. ActivityRecord.Token对于WMS的作用?

AMS在创建`Token`后，在`ActivityStarter.startActivityLocked()`方法中创建`WindowToken`，然后分别以`Token`和`WindowToken`为key和value存储在`DisplayContent`的`mTokenMap`中.

**当WMS调用addWindow添加Window时候，会使用Activity传过来的Token去mTokenMap查找，来验证Token的合法性**

