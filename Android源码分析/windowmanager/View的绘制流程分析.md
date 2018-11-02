# View的绘制流程分析

[Android开发艺术探索]()

[从ViewRootImpl类分析View绘制的流程](https://blog.csdn.net/feiduclear_up/article/details/46772477)


# 1. 简介

从`Window`的相关文章中得知,在`ActivityThread`的`performLaunchActivity()`中,会回调`Activity`的`onCreate()`方法,而`onCreate()`方法正是调用`setContentView()`的地方,但是在这里仅仅只是创建了`DecorView`

真正将`DecorView`添加到`Window`并进行关联的过程是在`ActivityThread.handleResumeActivity()`中


# 2. 顶层DecorView添加到窗口的过程

## 2.1 ActivityThread.handleResumeActivity()


    final void handleResumeActivity(IBinder token,
            boolean clearHide, boolean isForward, boolean reallyResume, int seq, String reason) {
		//获取到当前Activity的数据
        ActivityClientRecord r = mActivities.get(token);


        // TODO Push resumeArgs into the activity for consideration
        r = performResumeActivity(token, clearHide, reason);

        if (r != null) {
            final Activity a = r.activity;

            if (r.window == null && !a.mFinished && willBeVisible) {
				//获取当前Activity的PhoneWindow
                r.window = r.activity.getWindow();
				//获取PhoneWindow对应的DecorView
                View decor = r.window.getDecorView();
				//设置DecorView的可见度
                decor.setVisibility(View.INVISIBLE);
                ViewManager wm = a.getWindowManager();
                WindowManager.LayoutParams l = r.window.getAttributes();
				//将PhonewWindow中的DV与Activity产生关联
                a.mDecor = decor;
                l.type = WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
                l.softInputMode |= forwardBit;
				.......
                if (a.mVisibleFromClient) {
                    if (!a.mWindowAdded) {
						//标记根布局DV已经添加到窗口
                        a.mWindowAdded = true;
						//将根布局DV添加到当前Activity的窗口
                        wm.addView(decor, l);
                    } else {
                        a.onWindowAttributesChanged(l);
                    }
                }
			......
		}}}

- 这里`r.activity.getWindow()`即获取到了`PhonewWindow`,PW是在`Activity.attach()`中被创建的

- `r.window.getDecorView()`获取到的是在`Activity.onCreate()`中调用`setContentView()`时创建的`DecorView`,其被创建之后会被保存在`PhoneWindow`


## 2.2 WindowManager.addView()
已知`WindowManager`真正的实现类是`WindowManagerImpl`,并且`WindowManagerImpl`仅仅是将添加View的操作转交给`WindowManagerGlobal`

## 2.3 WindowManagerGlobal.addView()

    public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) {
		..........
			//创建ViewRootImpl
            root = new ViewRootImpl(view.getContext(), display);
            view.setLayoutParams(wparams);

            mViews.add(view);
            mRoots.add(root);
            mParams.add(wparams);
            // do this last because it fires off messages to start doing things
            try {
                root.setView(view, wparams, panelParentView);
            } catch (RuntimeException e) {
				..............
            }
	}

## 2.4 ViewRootImpl.setView()

	 public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
	        synchronized (this) {
	            if (mView == null) {
	                //将顶层视图DecorView赋值给全局的mView
	                mView = view;
	            .............
	            //标记已添加DecorView
	             mAdded = true;
	            .............
	            //请求布局
	            requestLayout();
	
	            .............     
	        }
	 }

## 2.5 ViewRootImpl.requestLayout()

    @Override
    public void requestLayout() {
        if (!mHandlingLayoutInLayoutRequest) {
            checkThread();
            mLayoutRequested = true;
            scheduleTraversals();
        }
    }

## 2.6 ViewRootImpl.scheduleTraversals()

    void scheduleTraversals() {
        if (!mTraversalScheduled) {
            mTraversalScheduled = true;
            mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();
            mChoreographer.postCallback(
                    Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
            if (!mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
        }
    }

## 2.7 ViewRootImpl.TraversalRunnable

    final class TraversalRunnable implements Runnable {
        @Override
        public void run() {
            doTraversal();
        }
    }

## 2.8 ViewRootImpl.doTraversal()

    void doTraversal() {
        if (mTraversalScheduled) {
            mTraversalScheduled = false;
            mHandler.getLooper().getQueue().removeSyncBarrier(mTraversalBarrier);

            if (mProfile) {
                Debug.startMethodTracing("ViewAncestor");
            }

            performTraversals();

            if (mProfile) {
                Debug.stopMethodTracing();
                mProfile = false;
            }
        }
    }

## 2.9 ViewRootImpl.performTraversals()

**从这一步开始,View开始绘制,它经过`measure`,`layout`,`draw`三个过程将View绘制出来**.其中`measure`用来测量View的宽高,`layout`用来确定View在父容器中的位置,`draw`负责将View绘制在屏幕上

# 3. View的绘制过程

## 3.1 ViewRootImpl.performTraversals()

	private void performTraversals() {
	        // cache mView since it is used so much below...
	        //mView就是DecorView,在WMG中VRI被创建,并传入了DV
	        final View host = mView;
	        //在2.3小节,成员变量mAdded赋值为true，因此条件不成立
	        if (host == null || !mAdded)
	            return;
	        //是否正在遍历
	        mIsInTraversal = true;
	        //是否马上绘制View
	        mWillDrawSoon = true;
	
	        .............
	        //顶层视图DecorView所需要窗口的宽度和高度
	        int desiredWindowWidth;
	        int desiredWindowHeight;
	
	        .....................
	        //在构造方法中mFirst已经设置为true，表示是否是第一次绘制DecorView
	        if (mFirst) {
	            mFullRedrawNeeded = true;
	            mLayoutRequested = true;
	            //如果窗口的类型是有状态栏的，那么顶层视图DecorView所需要窗口的宽度和高度就是除了状态栏
	            if (lp.type == TYPE_STATUS_BAR_PANEL
                		|| lp.type == TYPE_INPUT_METHOD
                		|| lp.type == TYPE_VOLUME_OVERLAY) {
	                Point size = new Point();
	                mDisplay.getRealSize(size);
	                desiredWindowWidth = size.x;
	                desiredWindowHeight = size.y;
	            } else {//除了上面的情况,其他的顶层视图DecorView所需要窗口的宽度和高度就是整个屏幕的宽高
	                DisplayMetrics packageMetrics =
	                    mView.getContext().getResources().getDisplayMetrics();
	                desiredWindowWidth = packageMetrics.widthPixels;
	                desiredWindowHeight = packageMetrics.heightPixels;
	            }
	    }
	............
	//获得view宽高的测量规格，mWidth和mHeight表示窗口的宽高，lp.widthhe和lp.height表示DecorView根布局宽和高
	 int childWidthMeasureSpec = getRootMeasureSpec(mWidth, lp.width);
	 int childHeightMeasureSpec = getRootMeasureSpec(mHeight, lp.height);
	
	  // Ask host how big it wants to be
	  //执行测量操作
	  performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
	
	........................
	//执行布局操作
	 performLayout(lp, desiredWindowWidth, desiredWindowHeight);
	
	.......................
	//执行绘制操作
	performDraw();
	
	}

### 3.1.1 Display.getRealSize(Point size)

通过`ContextImpl`获取到`Display`

    @Override
    public Display getDisplay() {
        if (mDisplay == null) {
            return mResourcesManager.getAdjustedDisplay(Display.DEFAULT_DISPLAY,
                    mResources);
        }

        return mDisplay;
    }

