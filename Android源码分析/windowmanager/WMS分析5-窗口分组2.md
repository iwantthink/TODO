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

### 3.1.2 showAsDropDown()

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

- **`anchor`是`PopupWindow`弹出时所依赖的那个控件**

- 先给出结论,`anchor.getApplicationWindowToken()`在当前这个例子中,获取到的是在`ViewRootImpl`中创建的那个`W extends IWindow.Stub`

## 3.2 View.getApplicationWindowToken()

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

- 如果`AttachInfo`中存在`mPanelParentWindowToken`那就使用它,否则就使用`mWindowToken`

- `AttachInfo`是在`ViewRootImpl`创建时被创建的,同时会分发给`DecorView`,并且该`DecorView`下所有的子类包括其自身,都使用的这个`AttachInfo`

	因此这里,查看`DecorView`在`WindowManagerGlobal.addView()`中创建`mPanelParentWIndowToken`的过程

- 关于`AttachInfo`的具体分析可以查看[AttachInfo.md]()


### 3.2.1 WindowManagerGlobal.addView()

    public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) {

        final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
        View panelParentView = null;
        synchronized (mLock) {

			// 如果当前控件是子窗口类型,那么就找到它的父窗口,取父窗口的对应的View
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
            view.setLayoutParams(wparams);
			// 保存控件
            mViews.add(view);
			// 保存ViewRootImpl
            mRoots.add(root);
			// 保存窗口属性
            mParams.add(wparams);
			.............
			// do this last because it fires off messages to start doing things
            try {
                root.setView(view, wparams, panelParentView);
            } catch (RuntimeException e) {...}

- 当前讨论的是作为`PopupWindow`的`anchor`控件,它属于`Activity`,所以Activity就是对应的`DecorView`

	**作为Acitivity的`DecorView`,属于应用窗口,因此`mPanelParentWIndowToken`没有值**

- 假如当前的`DecorView`对应的是子窗口类型,那么它就会去寻找其所依赖的父窗口对应的View

### 3.2.2 ViewRootImpl.setView()

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

- `panelParentView`作为 子窗口对应控件的父控件,在子窗口需要获取`mPanelParentWindowToken`时,会调用父窗口的`getApplicationWindowToken()`不断向上遍历(实际上最多就俩层..因为 子窗口不能作为父窗口来使用)

	因此这里子窗口的`mPanelParentWindowToken`就是父窗口的`WindowToken`,即`IWindow`


## 3.3 PopupWindow.createPopupLayoutParams()

    protected final WindowManager.LayoutParams createPopupLayoutParams(IBinder token) {
        final WindowManager.LayoutParams p = new WindowManager.LayoutParams();
        p.gravity = computeGravity();
        p.flags = computeFlags(p.flags);
        p.type = mWindowLayoutType;
        p.token = token;
		...........
        return p;
    }

- 新建了一个`WindowManager.LayoutParams`作为窗口属性,并将之前获取到的`windowToken`赋值给当前窗口属性(`W extends IWindow.Stub`)

	`Activity`和`Dialog`的token是在`ActivityRecord`中的`Token extends IApplication.Stub`


- 窗口的`type`属性的默认值应该是`TYPE_APPLICATION_PANEL`:

		private int mWindowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;

		public static final int TYPE_APPLICATION_PANEL = FIRST_SUB_WINDOW;


## 3.4 PopupWindow.preparePopup()

    private void preparePopup(WindowManager.LayoutParams p) {


		// 判断是否设置了  Drawable mBackground
        if (mBackground != null) {
            mBackgroundView = createBackgroundView(mContentView);
            mBackgroundView.setBackground(mBackground);
        } else {
            mBackgroundView = mContentView;
        }

        mDecorView = createDecorView(mBackgroundView);
        mDecorView.setIsRootNamespace(true);

        // The background owner should be elevated so that it casts a shadow.
        mBackgroundView.setElevation(mElevation);

        // We may wrap that in another view, so we'll need to manually specify
        // the surface insets.
        p.setSurfaceInsets(mBackgroundView, true /*manual*/, true /*preservePrevious*/);

        mPopupViewInitialLayoutDirectionInherited =
                (mContentView.getRawLayoutDirection() == View.LAYOUT_DIRECTION_INHERIT);
    }

### 3.4.1 PopupWindow.createBackgroundView()

    private PopupBackgroundView createBackgroundView(View contentView) {
        final ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
        final int height;
        if (layoutParams != null && layoutParams.height == WRAP_CONTENT) {
            height = WRAP_CONTENT;
        } else {
            height = MATCH_PARENT;
        }

        final PopupBackgroundView backgroundView = new PopupBackgroundView(mContext);
        final PopupBackgroundView.LayoutParams listParams = new PopupBackgroundView.LayoutParams(
                MATCH_PARENT, height);
        backgroundView.addView(contentView, listParams);

        return backgroundView;
    }

- 创建了一个`PopupBackgroundView`,对视图View进行包装

### 3.4.2 PopupWindow.createDecorView()

    private PopupDecorView createDecorView(View contentView) {
		................
        final PopupDecorView decorView = new PopupDecorView(mContext);
        decorView.addView(contentView, MATCH_PARENT, height);
		.............
        return decorView;
    }

- 使用`PopupDecorView`对`PopupBackgroundView`或者原始视图View 进行包装

- 该包装后的View 代表`PopupWindow`的根视图,类似于Activity的`DecorView`


## 3.5 PopupWindow.invokePopup()

    private void invokePopup(WindowManager.LayoutParams p) {
		...........

        final PopupDecorView decorView = mDecorView;
        mWindowManager.addView(decorView, p);

    }

- 主要就是调用了`WindowManagerImpl`去添加视图

- 这里的`WindowManager`是在其构造函数中创建的,获取的是视图View 对应的那个.**可以是Activity,也可以是Application,这对后续添加视图没有影响**

	**因为`PopupWindow`的`token`是显性赋值的，就是是就算用`Application`，也不会有什么问题，对于`PopupWindow`子窗口，关键点是View锚点决定其token，而不是`WindowManagerImpl`对象**


## 3.6 WindowManagerImpl.addView()

	public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
	    applyDefaultToken(params);
	    mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
	}

- 假设这里的是`Activity`对应的`WindowManagerImpl`,那么`mParentWindow`就是`Activity.attach()`方法中`PhoneWindow`


### 3.6.1 WindowManagerService.addView() 

    public int addWindow(Session session, IWindow client, int seq,
            WindowManager.LayoutParams attrs, int viewVisibility, int displayId,
            Rect outContentInsets, Rect outStableInsets, Rect outOutsets,
            InputChannel outInputChannel) {

	        WindowState parentWindow = null;

	       // PopupWindow的type属于子窗口,那就需要找到其对应的父窗口对应的WindowState
			//PopupWindow的type是手动指定的,是父窗口的IWindow
			// 获取到父窗口的WindowState
	       WindowState attachedWindow = null;
            if (type >= FIRST_SUB_WINDOW && type <= LAST_SUB_WINDOW) {
                parentWindow = windowForClientLocked(null, attrs.token, false);
				...........非空判断...
            }
	        // 如果Activity第一次添加子窗口 ，子窗口分组对应的WindowToken一定是null
            WindowToken token = displayContent.getWindowToken(
                    hasParent ? parentWindow.mAttrs.token : attrs.token);
	        AppWindowToken atoken = null;
	        if (token == null) {
	        ...
	            token = new WindowToken(this, attrs.token, -1, false);
	            addToken = true;
	        }           

	       // 新建窗口WindowState对象 注意这里的parentWindow 是父窗口的WindowState
           WindowState win = new WindowState(this, session, client, token, parentWindow,
                    appOp[0], seq, attrs, viewVisibility, session.mUid,
                    session.mCanAddInternalSystemWindow);
	       ...
			//添加更新全部map
			mWindowMap.put(client.asBinder(), win);
			win.mToken.addWindow(win);

	
	}

- `PopupWindow`属于子窗口,其对应的父类的`WindowState`是存在的

- 这里获取到的`PopupWindow`对应的`WindowToken`是其父窗口的,`PopupWindow`属于父窗口那组

# 4. 窗口的Z次序管理:窗口的分配序号,次序调整等