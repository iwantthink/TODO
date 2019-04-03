# WindowManagerService分析5

[Android窗口管理分析（3）：窗口分组及Z-order的确定](https://www.jianshu.com/p/90ede7b2a64a)

[Activity中的mToken](http://www.momoandy.com/2018/12/20/Activity%E4%B8%AD%E7%9A%84mToken/)

# 1. 简介
[WMS分析4-窗口分组.md]()简单分析了窗口分组中需要使用到的属性,以及其流程. **现在用几个例子来具体分析不同的窗口类型在窗口分组中的流程!**

# 2. 为什么Activity与Dialog是同一组?

在添加到WMS的时候，Dialog的窗口属性是`WindowManager.LayoutParams.TYPE_APPLICATION`，与`Activity`同属于应用窗口

- 因此必须使用Activity的`ActivityRecord.Token`才行，换句话说，必须使用`Activity`对应的`WindowManagerImpl`进行`addView()`才可以。

Dialog和Activity共享同一个`WindowManager`（也就是`WindowManagerImpl`），而`WindowManagerImpl`里面有个`Window`类型的`mParentWindow`变量，这个变量在Activity的`attach()`方法中被赋值,其值为创建`WindowManagerImpl`时传入的为当前Activity的Window(即PhoneWindow)，而`PhoneWindow`里面的`mAppToken`值又是AMS为当前Activity创建的`Token`，所以Activity与Dialog共享了同一个mAppToken值，只是Dialog和Activity的Window对象不同

## 2.1 Dialog的使用

        Dialog dialog = new AlertDialog.Builder(MainActivity.this).
                setTitle("Title").
                setMessage("Message").
                create();
        dialog.show();

### 2.1.1 Dialog 的构造函数

	Dialog(@NonNull Context context, @StyleRes int themeResId, boolean createContextThemeWrapper) {

		// 根据theme封装context
	    if (createContextThemeWrapper) {
	        ...
	        mContext = new ContextThemeWrapper(context, themeResId);
	    } else {
	        mContext = context;
	    }

		// 获取 WindowManagerImpl
	    mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    
		// 创建PhoneWindow
	    final Window w = new PhoneWindow(mContext);
	    mWindow = w;
	    w.setCallback(this);
	    w.setOnWindowDismissedCallback(this);
	    w.setWindowManager(mWindowManager, null, null);
	    w.setGravity(Gravity.CENTER);
	    mListenersHandler = new ListenersHandler(this);
	}

- 根据Theme调整Context, `createContextThemeWrapper`通常是true


- **这里的`Context`十分重要,决定了获取到的`WindowManagerImpl`**

## 2.2 Dialog的`show()`方法

    public void show() {
		...................
        mCanceled = false;

        if (!mCreated) {
            dispatchOnCreate(null);
        } else {
            // Fill the DecorView in on any configuration changes that
            // may have occured while it was removed from the WindowManager.
            final Configuration config = mContext.getResources().getConfiguration();
            mWindow.getDecorView().dispatchConfigurationChanged(config);
        }

        onStart();
		// 这里会创建DecorView... 因为DecorView 没有被创建
        mDecor = mWindow.getDecorView();
		.........
		// 这里获取的是默认的 LayoutParams
        WindowManager.LayoutParams l = mWindow.getAttributes();
		//
        mWindowManager.addView(mDecor, l);

        mShowing = true;
        sendShowMessage();
    }

- 这里获取的`mWindowManager` 实际类型是`WindowManagerImpl`,与`Activity`获取到的是同一个

## 2.2.1 mWindow.getAttributes()

    public final WindowManager.LayoutParams getAttributes() {
        return mWindowAttributes;
    }

- `WindowManager.LayoutParams mWindowAttributes` 做为`Window`的成员变量,仅在对象初始化时被赋值

	    // The current window attributes.
	    private final WindowManager.LayoutParams mWindowAttributes =
	        new WindowManager.LayoutParams();

		// WindowManager.LayoutParams 的构造函数
        public LayoutParams() {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            type = TYPE_APPLICATION;
            format = PixelFormat.OPAQUE;
        }


**从这里可以知道,`Dialog`对应的窗口是应用窗口**


## 2.3 WindowManagerImpl.addView()

    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }

- **已知这里的`WindowManagerImpl`是`Activity`对应的那个,因此这里的`mParentWindow`是`Activity.attach()`中传给`WindowManagerImpl`的`PhoneWindow`**
	
	那么`WindowManagerImpl.addView()`仍然按照处理`Activity`的方式处理，并利用Activity的`PhoneWindow`的 `adjustLayoutParamsForSubWindow()`方法调整参数

	赋值给`WindowManager.LayoutParams token`的值仍然是`Activity`的`ActivityRecord.Token`，那么在WMS端，对应就是`APPWindowToken`，也就是Activity与Dialog属于同一分组


## 2.4 为什么`Dialog`用`Application`作为context不行呢

Dialog的窗口类型属于应用窗口，如果采用Application作为context，那么在通过`context.getSystemService(Context.WINDOW_SERVICE)`获取`WindowManagerImpl`时,其调用的`getSystemService()`,会使用`Application`对应的`ContextImpl`. 这和`Activity`的`ContextImpl`是不一样的. 这就会导致获取到的`WindowManagerImpl`不一致

- `Application`和`Activity`分别对应的`WindowManagerImpl`的区别是前者没有`parentWindow`，因此在`WMG.addView()`方法中的`WindowManagerGlobal.adjustLayoutParamsForSubWindow()`函数不会被调用

	这样就导致`WindowManager.LayoutParams`的`token`就不会被赋值，最终导致`ViewRootImpl`在通过`setView()`向WMS在添加窗口的时候会失败,WMS抛出`WindowManagerGlobal.ADD_BAD_APP_TOKEN`错误给App端,App端接收到信息后抛出异常

		public int addWindow(Session session, IWindow client, XXX )
		        ...
		        // 对于应用窗口 token不可以为null
		        WindowToken token = mTokenMap.get(attrs.token);
		        if (token == null) {
		            if (type >= FIRST_APPLICATION_WINDOW && type <= LAST_APPLICATION_WINDOW) {
		                Slog.w(TAG, "Attempted to add application window with unknown token "
		                      + attrs.token + ".  Aborting.");
		                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
		            }
			...........
		}

		public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
		    synchronized (this) {
		                   ....
		                    case WindowManagerGlobal.ADD_NOT_APP_TOKEN:
		                        throw new WindowManager.BadTokenException(
		                                "Unable to add window -- token " + attrs.token
		                                + " is not for an application");
			....................
		}


**因此,不用使用Application作为Dialog的context的根本原因,是因为其不能为Dialog提供正确的token!!!**


# 3. PopupWindow类子窗口的添加流程及WindowToken分组

## 3.1 PopUpWindow 的使用方式
	// PopupWindow的布局
	View root = LayoutInflater.from(MainActivity.this).
                inflate(R.layout.pop_window, null);
	PopupWindow popupWindow =
                new PopupWindow(root,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true);
	popupWindow.setBackgroundDrawable(new BitmapDrawable());
	popupWindow.showAsDropDown(mTargetView);

- `mTargetView`表示弹出的`popupwindow`的位置相对于这个控件

### 3.1.1 PopupWindow的构造函数

    public PopupWindow(View contentView, int width, int height, boolean focusable) {
        if (contentView != null) {
            mContext = contentView.getContext();
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }

        setContentView(contentView);
        setWidth(width);
        setHeight(height);
        setFocusable(focusable);
    }

- 会去获取布局使用的`Context`以及其对应的`WindowManagerImpl`

## 3.1.2 showAsDropDown()

    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
		.....状态判断.......
		
        TransitionManager.endTransitions(mDecorView);

        attachToAnchor(anchor, xoff, yoff, gravity);

		............

        final WindowManager.LayoutParams p =
                createPopupLayoutParams(anchor.getApplicationWindowToken());

        preparePopup(p);

        final boolean aboveAnchor = findDropDownPosition(anchor, p, xoff, yoff,
                p.width, p.height, gravity, mAllowScrollingAnchorParent);

        updateAboveAnchor(aboveAnchor);
        p.accessibilityIdOfAnchor = (anchor != null) ? anchor.getAccessibilityViewId() : -1;

        invokePopup(p);
    }


- 存在三个重载的方法,但是最终都会调用上面的这个

### 3. View.getApplicationWindowToken()

    public IBinder getApplicationWindowToken() {
        AttachInfo ai = mAttachInfo;
        if (ai != null) {
            IBinder appWindowToken = ai.mPanelParentWindowToken;
            if (appWindowToken == null) {
                appWindowToken = ai.mWindowToken;
            }
            return appWindowToken;
        }
        return null;
    }


### 3. WindowManagerGlobal.addView()

    public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) {

        View panelParentView = null;
        synchronized (mLock) {

            // If this is a panel window, then find the window it is being
            // attached to for future reference.
            if (wparams.type >= WindowManager.LayoutParams.FIRST_SUB_WINDOW &&
                    wparams.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
                final int count = mViews.size();
                for (int i = 0; i < count; i++) {
                    if (mRoots.get(i).mWindow.asBinder() == wparams.token) {
                        panelParentView = mViews.get(i);
                    }
                }
            }

            root = new ViewRootImpl(view.getContext(), display);

			.............
			// do this last because it fires off messages to start doing things
            try {
                root.setView(view, wparams, panelParentView);
            } catch (RuntimeException e) {...}


### 3. ViewRootImpl.setView()

    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {

        synchronized (this) {
            if (mView == null) {

                if (panelParentView != null) {
                    mAttachInfo.mPanelParentWindowToken
                            = panelParentView.getApplicationWindowToken();
                }
		............
		}
	}
