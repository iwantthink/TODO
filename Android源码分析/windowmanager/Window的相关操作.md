# Window的相关操作

[Android解析WindowManager（三）Window的添加过程](http://liuwangshu.cn/framework/wm/3-add-window.html)

[Android 源码解析 之 setContentView](https://blog.csdn.net/lmj623565791/article/details/41894125)

[Android开发艺术探索]()

# 1. 简介

本文基于android-27

`WindowManager`对`Window`进行管理，说到管理那就离不开对`Window`的添加、更新和删除的操作。

对于Window的操作，最终都是交由`WMS`来进行处理。

窗口的操作分为两大部分，

1. 一部分是WindowManager处理部分，

2. 另一部分是WMS处理部分。

已知`Window`分为三大类，分别是：`Application Window`（应用程序窗口）、`Sub Windwow`（子窗口）和`System Window`（系统窗口），对于不同类型的窗口添加过程会有所不同，但是对于`WMS`处理部分，添加的过程基本上是一样的， `WMS`对于这三大类的窗口基本是“一视同仁”的。

![](http://upload-images.jianshu.io/upload_images/1417629-a2307e2c73db270d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 这篇文章仅分析`WindowManager`处理的部分,与WMS相关的内容可以在[WindowManagerService分析.md]()中查看

# 2. Window的添加操作

`Window`的添加过程需要通过`WindowManager`的`addView()`来实现,而`WindowManager`是一个接口,它真正的实现是`WindowManagerImpl`类.

## 2.1 WindowManagerImpl.addView()

    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }

- `applyDefaultToken()`方法与Window的层级有关系

- `WindowManagerImpl`并没有直接实现Window的添加操作,而是全部交给了`WindowManagerGlobal`类来处理

- 这种工作模式是典型的桥接模式,将所有的操作委托给`WindowManagerGlobal`来实现

- 这里的`mContext`实际是`ContextImpl`类型,在`WindowManagerImpl`被创建时传入

- `mParentWindow`是当前Window对象

## 2.2 WindowManagerGlobal.addView()

	public void addView(View view, ViewGroup.LayoutParams params,
	          Display display, Window parentWindow) {
	    ...//参数检查
	      final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
		  // 判断当前添加进来的view是否属于子Window,如果是则需要调整布局参数
	      if (parentWindow != null) {
	          parentWindow.adjustLayoutParamsForSubWindow(wparams);
	      } else {
	      ...
	      }
	
	      ViewRootImpl root;
	      View panelParentView = null;
	       	  //创建ViewRootImpl
	          root = new ViewRootImpl(view.getContext(), display);
			  //添加LayoutParamas
	          view.setLayoutParams(wparams);
	          mViews.add(view);
	          mRoots.add(root);
	          mParams.add(wparams);
	      }
	      try {
	          root.setView(view, wparams, panelParentView);
	      } catch (RuntimeException e) {
	         ...
	      }
	  }

- 在这个方法中,创建了`ViewRootImpl`,并将最终添加View的实现交给`ViewRootImpl`来实现


## 2.2.1 WindowManagerGlobal 重要的列表

	//存储所有Window对应的View
    private final ArrayList<View> mViews = new ArrayList<View>();
	//存储所有Window对应的ViewRootImpl
    private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();
	//存储所有Window对应的布局参数
    private final ArrayList<WindowManager.LayoutParams> mParams =
            new ArrayList<WindowManager.LayoutParams>();
	//存储正在被删除的View对象,或者说是已经调用了removeView方法,
	//但是删除操作还未完成的Window对象
    private final ArraySet<View> mDyingViews = new ArraySet<View>();

## 2.3 ViewRootImpl

ViewRootImpl身负了很多职责：

- View树的根并管理View树

- 触发View的测量、布局和绘制

- 输入事件的中转站

- 管理Surface

- 负责与WMS进行进程间通信

### 2.3.1 ViewRootImpl.setView()
	
	public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
	       synchronized (this) {
	          ...
	               try {
	                   mOrigWindowType = mWindowAttributes.type;
	                   mAttachInfo.mRecomputeGlobalAttributes = true;
	                   collectViewAttributes();
	                   res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
	                           getHostVisibility(), mDisplay.getDisplayId(),
	                           mAttachInfo.mContentInsets, mAttachInfo.mStableInsets,
	                           mAttachInfo.mOutsets, mInputChannel);
	               } 
	               ...
	   }

- `mWindowSession`是`IWindowSession`类型的，它是一个`Binder`代理对象，用于进行进程间通信，`IWindowSession`是`Client`端的代理，它的`Server`端的实现为`Session`，此前包含`ViewRootImpl`在内的代码逻辑都是运行在本地进程的，而`Session`的`addToDisplay`方法则运行在`WMS`所在的进程,这里其实就是一次IPC


### 2.3.2 Session.addToDisplay()
	
	@Override
	 public int addToDisplay(IWindow window, int seq, WindowManager.LayoutParams attrs,
	         int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets,
	         Rect outOutsets, InputChannel outInputChannel) {
	     return mService.addWindow(this, window, seq, attrs, viewVisibility, displayId,
	             outContentInsets, outStableInsets, outOutsets, outInputChannel);
	 }

- `addToDisplay`方法中会调用了`WMS`的`addWindow`方法，并将自身也就是`Session`，作为参数传了进去，每个应用程序进程都会对应一个`Session`，`WMS`会用`ArrayList`来保存这些`Session`。这样剩下的工作就交给WMS来处理，在WMS中会为这个添加的窗口分配`Surface`，并确定窗口显示次序，可见负责显示界面的是画布`Surface`，而不是窗口本身。`WMS`会将它所管理的`Surface`交由`SurfaceFlinger`处理，`SurfaceFlinger`会将这些`Surface`混合并绘制到屏幕上。


# 3. 视图的创建过程
 
仅分析Window创建之前的调用逻辑,具体的Window创建过程在[WindowManager整体分析-第四节.md]()

## 3.1 Activity的Window的创建过程

1. `Activity`的启动过程中,与`Window`的创建相关的逻辑在调用到`Activity.attach()`方法中

2. 在`Activity.attach()`方法中,系统会创建`Activity`所属的`Window`对象并为其设置回调接口,`Window`对象的创建是直接创建一个`PhoneWindow`

	由于`Activity`实现了`Window`的`Callback`接口,因此当`Window`接收到外接的状态改变时就会回调`Activity`的方法

## 3.2 Activity视图关联到Window的过程

1. `Activity`的启动过程中,在执行完`activity.attach()`之后,会通过`Instrumentation.callActivityOnCreate()`调用`Activity`的`onCreate()`. 

	**`Activity`的视图是由`setContentView()`方法提供,并且它是在`onCreate()`方法中被调用**

2. 查看`Activity`的`setContentView()`

	    public void setContentView(@LayoutRes int layoutResID) {
	        getWindow().setContentView(layoutResID);
	        initWindowDecorActionBar();
	    }

	- 通过代码可以知道,`Activity`将具体实现交给`Window`来实现

	- `getWindow()`方法就是获取在`attach()`方法中创建的那个`PhoneWindow`

### 3.2.1 PhoneWindow.setContentView()

	   @Override
	    public void setContentView(int layoutResID) {
	        //
	        if (mContentParent == null) {
	            installDecor();
	        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
				//如果没有5.0转场动画,则remove掉之前所有添加的view
	            mContentParent.removeAllViews();
	        }
			// 5.0 转场动画
	        if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
	            final Scene newScene = Scene.getSceneForLayout(mContentParent, layoutResID,
	                    getContext());
	            transitionTo(newScene);
	        } else {
	            mLayoutInflater.inflate(layoutResID, mContentParent);
	        }
			// 通知Activity 视图发生改变
	        mContentParent.requestApplyInsets();
	        final Callback cb = getCallback();
	        if (cb != null && !isDestroyed()) {
	            cb.onContentChanged();
	        }
	        mContentParentExplicitlySet = true;
	    }

- `mContentParent`就是id为`ID_ANDROID_CONTENT`的FrameLayout,实际上`setContentView`就是为`mContentParent`添加一个子类.

- `installDecor()`方法主要完成了俩个事情

	1. 创建`DecorView`,通过配置给`DecorView`进行设置

	2. 给`mContentParent`赋值

- `mLayoutInflator.inflate()`主要的作用是:

	**给mContentParent添加子view.换而言之就是将 `layoutResID` 解析成view 并添加到 mContentParent**

- 在创建好视图之后,会回调`Activity`的`onContentChanged()`方法通知`Activity`视图已经发生了变化

- **一个Window对象对应着一个View(DecorView),`ViewRootImpl`就是对这个`DecorView`进行操作**


### 3.2.1.1 PhoneWindow.installDecor()

    private void installDecor() {
        mForceDecorInstall = false;
        if (mDecor == null) {
			//生成DecorView,就是new 了一个DecorView(FrameLayout的子类)
            mDecor = generateDecor(-1);
			.......
        } else {
			// 将DecorView和Window进行关联
            mDecor.setWindow(this);
        }
       if (mContentParent == null) {
			// 根据DecorView 生成 mContentParent
            mContentParent = generateLayout(mDecor);
			..............
		}

### 3.2.1.2 PHoneWindow.generateLayout()

    protected ViewGroup generateLayout(DecorView decor) {
        // Apply data from current theme.
		//获得Window的样式
        TypedArray a = getWindowStyle();
		//省略代码
		//通过WindowStyle中设置的各种属性,对Window
		//进行requestFeature或者setFlags

        // Inflate the window decor.
		//
        int layoutResource;
        int features = getLocalFeatures();
		//主要功能就是根据不同的配置 给DecorView添加不同的布局文件
		//即 给DecorView添加不同的子节点
        // System.out.println("Features: 0x" + Integer.toHexString(features));
        if ((features & (1 << FEATURE_SWIPE_TO_DISMISS)) != 0) {
            layoutResource = R.layout.screen_swipe_dismiss;
            setCloseOnSwipeEnabled(true);
        } else if ((features & ((1 << FEATURE_LEFT_ICON) | (1 << FEATURE_RIGHT_ICON))) != 0) {
            if (mIsFloating) {
                TypedValue res = new TypedValue();
                getContext().getTheme().resolveAttribute(
                        R.attr.dialogTitleIconsDecorLayout, res, true);
                layoutResource = res.resourceId;
            } else {
                layoutResource = R.layout.screen_title_icons;
            }
            // XXX Remove this once action bar supports these features.
            removeFeature(FEATURE_ACTION_BAR);
            // System.out.println("Title Icons!");
        } 
		// 省略添加的代码
        mDecor.startChanging();
		//下面的方法是将找到的不同的布局文件,添加给DecorView
		//这里也说明了，我们经常写的requestWindowFeature(Window.FEATURE_NO_TITLE)代码为什么一定放在setContentView之前。
		//因为系统会根据配置找不同的布局文件，而一旦添加了布局文件，就没有办法再移除title了。因此会抛出异常
        mDecor.onResourcesLoaded(mLayoutInflater, layoutResource);
		//找到 contentParent,其实findViewById内部调用的是DecorView.findViewByID
        ViewGroup contentParent = (ViewGroup)findViewById(ID_ANDROID_CONTENT);
      	//省略代码
        mDecor.finishChanging();
        return contentParent;
    }

- `generateLayout()`主要功能就是:

	1. **通过`findViewByID()`方法找到`DecorView`中id为`ID_ANDROID_CONTENT`的`view`.并赋值给`mContentParent`**

	2. 根据不同的配置给`DecorView`添加不同的`layoutResource`布局文件

## 3.3 Activity视图真正可见的时候

**截止到回调为止,DecorView创建并初始化完毕,Activity的布局文件也被添加到了`DecorView`的子布局`ID_ANDROID_CONTENT`中(即mContentParent所代表的view),但是这个时候,`DecorView`仍未被`WindowManager`正式添加到`Window`中**

`Window`更多表示的是一种抽象的功能集合,虽然已经在`Activity.attach()`方法中被创建,但是这个时候由于`DecorView`并没有被`WindowManager`识别,所以这个时候的`Window`无法提供具体的功能,**因为它还无法接收外接的输入信息**

在`ActivityThread`的`handleResumeActivity()`方法中,首先会调用`Activity`的`onResume()`方法,然后会调用`Activity`的`makeVisible()`,正是这个方法中,`DecorView`真正完成了添加和显示这俩个过程,直到这时候`Activity`的视图才能被用户看到

