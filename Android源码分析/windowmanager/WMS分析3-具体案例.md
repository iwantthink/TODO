# WindowManagerService分析3

[深入理解Android 卷III ]()

[Android窗口管理分析（2）：WindowManagerService窗口管理之Window添加流程 - 看书的小蜗牛](https://www.jianshu.com/p/40776c123adb)

[Android窗口管理分析（1）：View如何绘制到屏幕上的主观理解 - 看书的小蜗牛](https://www.jianshu.com/p/e4b19fc36a0e)

# 1. 简介 

窗口管理可以说是Android系统中最复杂的一部分，它涉及的模块比较多，虽然笼统的说是窗口管理，其实，除了WindowManagerService还包括SurfaceFlinger服务、Linux的共享内存及tmpfs文件系统、Binder通信、InputManagerService、动画、VSYNC同步技术等.

- **这里主要介绍WMS在窗口管理中的作用!**


## 1.1 WMS介绍

**WindowManagerService是负责Android的窗口管理.并且它只负责管理，比如窗口的添加、移除、调整顺序等，至于图像的绘制与合成之类的都不是WMS管理的范畴，WMS更像在更高的层面对于Android窗口的一个抽象，真正完成图像绘制的是APP端，而完成图层合成的是SurfaceFlinger服务 **

- **此外,WMS还为所有窗口分配Surface,管理Surface的显示顺序(Z-order)以及位置尺寸,控制窗口动画,是输入系统的一个重要中转站**


## 1.2 示例


    TextView mview=new TextView(context);
    ...<!--设置颜色 样式-->
    WindowManager mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
    wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
    wmParams.format = PixelFormat.RGBA_8888;
    wmParams.width = 800;
    wmParams.height = 800;
    mWindowManager.addView(mview, wmParams);

- 以上代码可以在主屏幕上添加一个TextView并展示，**并且这个TextView独占一个窗口**。

	在利用`WindowManager.addView()`添加窗口之前，TextView的onDraw不会被调用，也就说View必须被添加到窗口中，才会被绘制，或者可以这样理解，**只有申请了依附窗口，View才会有可以绘制的目标内存**。

## 1.3 WMS在窗口管理中的职责

当APP通过`WindowManagerService`的代理向其添加窗口的时候，WindowManagerService除了自己进行登记整理，还需要向SurfaceFlinger服务申请一块Surface画布，其实主要是画布背后所对应的一块内存，只有这一块内存申请成功之后，APP端才有绘图的目标，并且这块内存是APP端同SurfaceFlinger服务端共享的，这就省去了绘图资源的拷贝

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g1h7ebaw0qj20rs0lr3z3.jpg)

- App端可以直接通过`unLockCanvasAndPost`直接同`SurfaceFlinger`通信进行重绘的，就是说图形的绘制同WMS没有关系，WMS只是负责窗口的管理，并不负责窗口的绘制

这一点其实也可以从IWindowSession的binder通信接口看出来：

	interface IWindowSession {
	
	    int add(IWindow window, int seq, in WindowManager.LayoutParams attrs,
	            in int viewVisibility, out Rect outContentInsets,
	            out InputChannel outInputChannel);
	            
	    int addToDisplay(IWindow window, int seq, in WindowManager.LayoutParams attrs,
	            in int viewVisibility, in int layerStackId, out Rect outContentInsets,
	            out InputChannel outInputChannel);
	            
	    int relayout(IWindow window, int seq, in WindowManager.LayoutParams attrs,
	            int requestedWidth, int requestedHeight, int viewVisibility,
	            int flags, out Rect outFrame, out Rect outOverscanInsets,
	            out Rect outContentInsets, out Rect outVisibleInsets,
	            out Configuration outConfig, out Surface outSurface);
	            
	    void remove(IWindow window);
	...
	}

- 从参数上看,App和WMS通信时没有任何View相关的信息,基本上是以`IWindow`为单位,并且设计的操作也都是针对窗口的(添加,移除,大小调整,分组等等)

	**单单从窗口显示来看，WMS的作用确实很明确，就是在服务端登记当前存活窗口，后面还会看到，这会影响SurfaceFlinger的图层混合，可以说是为SurfaceFlinger服务的**'


## 1.4 View的绘制与数据传递

每个View都有自己的`onDraw()`回调，开发者可以在`onDraw()`里绘制自己想要绘制的图像,这里View的绘制是在APP端而不是在WMS端

- 直观上理解，View的绘制也不会交给服务端，不然也太不独立，可是View绘制的内存是什么时候分配的呢？是谁分配的呢？

	我们知道每个Activity可以看做是一个图层，其对应一块绘图表面其实就是`Surface`，`Surface`绘图表面对应的内存其实是由`SurfaceFlinger`申请的，并且，内存是APP与SurfaceFlinger间进程共享的。实现机制是基于Linux的共享内存，其实就是`MAP+tmpfs`文件系统，你可以理解成SF为APP申请一块内存，然后通过binder将这块内存相关的信息传递APP端，APP端往这块内存中绘制内容，绘制完毕，通知SF图层混排，之后，SF再将数据渲染到屏幕。其实这样做也很合理，因为图像内存比较大，普通的binder与socket都无法满足需求，内存共享的示意图如下

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1g1hb4ofmdqj20rs0iwq3a.jpg)


## 1.5 窗口管理总结

整个Android窗口管理简化的话可以分为以下三部分:

1. WindowManagerService：

	**WMS控制着Surface画布的添加与次序，动画还有触摸事件**

2. SurfaceFlinger：

	**SF负责图层的混合，并且将结果传输给硬件显示**

3. APP端：

	**每个APP负责相应图层的绘制**

4. APP与SurfaceFlinger通信：

	**APP与SF图层之间数据的共享是通过匿名内存来实现的**

# 2. WMS如何管理窗口?

在窗口的添加流程过程中,App端,WMS端,SurfaceFlinger端都需要参与.下面介绍这个流程中重要的几个点

1. 窗口的分类,`Activity、Dialog、PopupWindow、Toast`等对应窗口的区别

2. `Window、IWindow 、WindowState、WindowToken、AppToken`等之间的关系

3. 窗口的添加以及Surface的申请与Binder传递

## 2.1 窗口的分类

参考[Window属性介绍.md](),可以知道**Android大致将窗口分为三类:应用窗口,子窗口和系统窗口**

- `Activity,Dialog`属于应用窗口,`Dialog`比较的特殊,其从表现上偏向于子窗口(**需要依附Activity**),但是从性质上来说,其拥有自己的`WindowToken`,属于应用窗口.

- `PopupWindow`属于子窗口

- `Toast`属于系统窗口

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g1hbhtm2k3j20rs0lot96.jpg)


# 3. 窗口的添加实例

在[WMS分析2.md]()中,分析了WMS的`addView()`方法的使用. 下面结合具体的例子分析窗口的添加流程,使用Activity去分析窗口添加流程十分麻烦,会涉及AMS等相关知识,这里仅使用一个悬浮View来展示和分析窗口的添加


	private void addTextViewWindow(Context context){
	
	    TextView mview=new TextView(context);
	    ...<!--设置颜色 样式-->
	    <!--关键点1-->
	    WindowManager mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
	    WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
	    <!--关键点2-->
	    wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
	    wmParams.format = PixelFormat.RGBA_8888;
	    wmParams.width = 800;
	    wmParams.height = 800;
	    <!--关键点3-->
	    mWindowManager.addView(mview, wmParams);
	}

- **关键点1:**

	获取`WindowManagerService`的代理对象,通过[Context分析.md](),可以知道对于`Application`来说,这里得到的是一个被封装过的代理对象(`WindowManagerImpl`实例)

	需要注意的是,`WMI`有俩个构造函数,在这里是一个参数的构造函数,**所以`mParentWindow`为空**

		public WindowManagerImpl(Display display) {
		    this(display, null);
		}
		
		private WindowManagerImpl(Display display, Window parentWindow) {
		    mDisplay = display;
		    mParentWindow = parentWindow;
		}

- **关键点2:**

	`WindowManager.LayoutParams`中主要看一个type参数，这个参数决定了窗口的类型，这里我们定义成一个Toast窗口，属于系统窗口，不需要处理父窗口、子窗口之类的事，更容易分析

- **关键点3:**

	这里调用了`WMI`的`addView()`方法,实际上是交给了`WindowManagerGlobal`来处理

## 3.1 WindowManagerImpl的`addView()`方法

	    @Override
	    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
			// 这里添加了默认的token
	        applyDefaultToken(params);
	        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
	    }

- 在方法`applyDefaultToken()`中,如果判断当前窗口没有父类,就会将默认的token添加到LayoutParams

## 3.2 WindowManagerGlobal的`addView()`方法

`mGlobal`是`WindowManagerGlobal`类型,其`addView()`方法大概的逻辑如下(不包含窗口关系判断):

	public void addView(View view, ViewGroup.LayoutParams params,
	        Display display, Window parentWindow) {
	
	    final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
	        <!--关键点1-->
	        root = new ViewRootImpl(view.getContext(), display);
	        view.setLayoutParams(wparams);
	        mViews.add(view);
	        mRoots.add(root);
	        mParams.add(wparams);
	    }
	   <!--关键点2-->
	    try {
	        root.setView(view, wparams, panelParentView);
	    }           
	 ...  
	}

- **关键点1:**

	在向WMS添加View的时候，`WindowManagerGlobal`首先为View新建了一个`ViewRootImpl`，**`ViewRootImpl`可以看做也是Window和View之间的通信的纽带**，比如将View添加到WMS、处理WMS传入的触摸事件、通知WMS更新窗口大小等、同时`ViewRootImpl`也封装了View的绘制与更新方法等


## 3.3 ViewRootImpl的`setView()`方法

	 public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
	        synchronized (this) {
	            if (mView == null) {
	                mView = view;
	                  ...
	                    <!--关键点1 -->
	                // Schedule the first layout -before- adding to the window
	                // manager, to make sure we do the relayout before receiving
	                // any other events from the system.
	                requestLayout();
	                if ((mWindowAttributes.inputFeatures
	                        & WindowManager.LayoutParams.INPUT_FEATURE_NO_INPUT_CHANNEL) == 0) {
	                    mInputChannel = new InputChannel();
	                }
	                try {
	                    <!--关键点2 -->
	                    res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
	                            getHostVisibility(), mDisplay.getDisplayId(),
	                            mAttachInfo.mContentInsets, mAttachInfo.mStableInsets,
	                            mAttachInfo.mOutsets, mInputChannel);
	                } catch (RemoteException e) {
	               ...
				}
			}
	}

- **关键点1:**
	
	这里是先为relayout占一个位置，其实是依靠Handler先发送一个Message，排在所有WMS发送过来的消息之前，先布局绘制一次，之后才会处理WMS传来的各种事件，比如触摸事件等，毕竟要首先将各个View的布局、位置处理好，才能准确的处理WMS传来的事件。

- **关键点2**:

	这里才是真正添加窗口的地方，虽然关键点1执行在前，但是用的是Handler发消息的方式来处理，其Runable一定是在关键点2之后执行

	这里有俩个比较重要的对象`IWindowSession.Stub mWindowSession`与`IWindow.Stub mWindow`，两者都是在ViewRootImpl在实例化的时候创建的

		public ViewRootImpl(Context context, Display display) {
		    mContext = context;
		    mWindowSession = WindowManagerGlobal.getWindowSession();
		    mWindow = new W(this);
			............
		}

	- `mWindowSession`它是通过`WindowManagerGlobal.getWindowSession`获得的一个Binder服务代理(`IWindowSession.Stub`)，是App端向WMS端发送消息的通道。

	- `mWindow`是一个`W extends IWindow.Stub`类型的Binder对象，其主要作用是传递给WMS端，作为WMS向APP端发送消息的通道

### 3.3.1 IWindowSession的获取

`IWindowSession`获取的具体操作是：首先通过`getWindowManagerService()` 获取WMS的代理，之后通过WMS的代理在服务端open一个Session，并在APP端获取该Session的代理


    public static IWindowSession getWindowSession() {
        synchronized (WindowManagerGlobal.class) {
            if (sWindowSession == null) {
                try {
                    InputMethodManager imm = InputMethodManager.getInstance();
					<!--关键点1-->
                    IWindowManager windowManager = getWindowManagerService();
					<!--关键点2-->
                    sWindowSession = windowManager.openSession(
                            new IWindowSessionCallback.Stub() {
                                @Override
                                public void onAnimatorScaleChanged(float scale) {
                                    ValueAnimator.setDurationScale(scale);
                                }
                            },
                            imm.getClient(), imm.getInputContext());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            return sWindowSession;
        }
    }

- **关键点1:**

	方法`getWindowManagerService()`在这里 是真正的获取了 WMS的远程代理Binder

		IWindowManager.Stub.asInterface(ServiceManager.getService("window"))

- **关键点2:**

	它通过WMS的binder代理，通知WMS调用`openSession()`方法，创建一个Session返回给APP端，而`Session extends IWindowSession.Stub` ，很明显也是一个Binder通信的Stub端，封装了每一个Session会话的操作。

	    @Override
	    public IWindowSession openSession(IWindowSessionCallback callback, IInputMethodClient client,
	            IInputContext inputContext) {
	        if (client == null) throw new IllegalArgumentException("null client");
	        if (inputContext == null) throw new IllegalArgumentException("null inputContext");
	        Session session = new Session(this, callback, client, inputContext);
	        return session;
	    }

## 3.4 Session的`addToDisplay()`方法

    @Override
    public int addToDisplay(IWindow window, int seq, WindowManager.LayoutParams attrs,
            int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets,
            Rect outOutsets, InputChannel outInputChannel) {
        return mService.addWindow(this, window, seq, attrs, viewVisibility, displayId,
                outContentInsets, outStableInsets, outOutsets, outInputChannel);
    }

- 这里的`mService`就是WMS对象,在其构造函数中传入.所以这里又去调用了WMS的`addWindow()`方法

	
- WMS的`addVIew()`可以去参考[WMS分析2.md]()

	**这一部分内容主要是针对WMS的窗口管理,以及`SurfaceFlinger`链接的获取**

# 4. `Surface`分配

第三小节主要针对WMS的窗口管理，接下来第四小节主要围绕Surface的分配来进行分析

**ViewRootImpl在setView时候分了两步:**

1. 调用了`requestLayout()`

	方法`requestLayout()`调用里面使用了Hanlder的一个小手段，那就是利用方法`postSyncBarrier()`添加了一个`Barrier`（挡板）

	- **这个挡板的作用是阻塞普通的同步消息的执行,在挡板被撤销之前，只会执行异步消息**
	
		方法`requestLayout()`先添加了一个挡板Barrier，之后自己插入了一个异步任务`mTraversalRunnable`，其主要作用就是保证`mTraversalRunnable`在所有同步Message之前被执行，保证View绘制的最高优先级

2. 通过`IWindowSession`调用其`addToDisplay()`方法


虽然先调用`requestLayout()`，但是由于其内部利用Handler发送消息延迟执行的，所以可以看做方法`requestLayout()`是在方法`addWindow()`之后执行的


那么这里就看添加窗口之后，如何分配`Surface`的


## 4.1 ViewRootImpl.scheduleTraversals()

    void scheduleTraversals() {
        if (!mTraversalScheduled) {
            mTraversalScheduled = true;

			// 注释1 
			// 添加挡板
            mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();
			// 注释2
			// 添加异步消息
            mChoreographer.postCallback(
                    Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
            if (!mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
        }
    }

- `mTraversalRunnable`任务的主要作用是：

	如果Surface未分配，则请求分配Surface，并测量、布局、绘图，其执行主体其实是`performTraversals()`函数

### 4.1.1 ViewRootImpl.performTraversals()

该函数包含了APP端View绘制大部分的逻辑, performTraversals函数很长，这里只简要看几个点:
	
	private void performTraversals() {
		final View host = mView;
		...
		if (mFirst || windowShouldResize || insetsChanged ||viewVisibilityChanged || params != null) {

			// 注释1
			// 申请Surface或者重新设置参数
			relayoutResult = relayoutWindow(params, viewVisibility, insetsPending);
	        
		    // 注释2
			// 测量
			performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
		}        
	    // 注释3 
		// 布局
        final boolean didLayout = layoutRequested && (!mStopped || mReportNextDraw);
        boolean triggerGlobalLayoutListener = didLayout
                || mAttachInfo.mRecomputeGlobalAttributes;
        if (didLayout) {
			performLayout(lp, desiredWindowWidth, desiredWindowHeight);
			...............
		}
		.............
		// 注释4 
		// 更新window
	    mWindowSession.setInsets(mWindow, insets.mTouchableInsets,
	                        contentInsets, visibleInsets, touchableRegion);
	    // 注释5
		// 绘制
		performDraw();
		...........
	}

- 注释1:

	方法`relayoutWindow()`主要是通过`mWindowSession.relayout()`向WMS申请或者更新`Surface`如下，这里只关心一个重要的参数mSurface，在Binder通信中mSurface是一个out类型的参数，也就是Surface内部的内容需要WMS端负责填充，并回传给APP端

	   private int relayoutWindow(WindowManager.LayoutParams params, int viewVisibility,
	            boolean insetsPending) throws RemoteException {
	       ...
	        int relayoutResult = mWindowSession.relayout(
	                mWindow, mSeq, params, ...  mSurface);
	        ...
	        return relayoutResult;
	    }