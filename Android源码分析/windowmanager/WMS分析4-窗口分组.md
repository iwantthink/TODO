# WindowManagerService分析4

[Android窗口管理分析（3）：窗口分组及Z-order的确定](https://www.jianshu.com/p/90ede7b2a64a)

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

# 4. Activity 对应的token,WindowToken的添加

