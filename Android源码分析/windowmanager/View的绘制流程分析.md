# View的绘制流程分析

[Android开发艺术探索]()

[从ViewRootImpl类分析View绘制的流程](https://blog.csdn.net/feiduclear_up/article/details/46772477)

[深入理解Android 卷III 网络版](https://www.kancloud.cn/alex_wsc/android-deep3/416457)

[从源码分析View的绘制流程](http://zhangruifeng.top/2018/06/14/%E4%BB%8E%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90View%E7%9A%84%E7%BB%98%E5%88%B6%E6%B5%81%E7%A8%8B/)

[Android开发之漫漫长途 Ⅴ——Activity的显示之ViewRootImpl的预测量、窗口布局、最终测量、布局、绘制](https://segmentfault.com/a/1190000012018189)


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

- 创建`ViewRootImpl`,并开始对`PhoneWindow`中的`DecorView`进行操作.将`DecorView`设置给`ViewRootImp`,通过`ViewRootImpl`来更新界面并完成`Window`的添加过程

- 将`Window`所对应的`View,ViewRootImpl,LayoutParams`分别添加到`WindowManager`的集合中


## 2.4 ViewRootImpl.setView()

	 public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
	        synchronized (this) {
         	if (mView == null) {
                //ViewRootImpl成员变量view进行复制，以后操作的都是mView。
                mView = view;
                /*******部分代码省略**********/
                // Schedule the first layout -before- adding to the window
                // manager, to make sure we do the relayout before receiving
                // any other events from the system.
                requestLayout();
                if ((mWindowAttributes.inputFeatures
                        & WindowManager.LayoutParams.INPUT_FEATURE_NO_INPUT_CHANNEL) == 0) {
                    mInputChannel = new InputChannel();
                }
                try {
                    mOrigWindowType = mWindowAttributes.type;
                    mAttachInfo.mRecomputeGlobalAttributes = true;
                    collectViewAttributes();
                    //将该Window添加到屏幕。
                    //mWindowSession实现了IWindowSession接口，它是Session的客户端Binder对象.
                    //addToDisplay是一次AIDL的跨进程通信，通知WindowManagerService添加IWindow
                    res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
                            getHostVisibility(), mDisplay.getDisplayId(),
                            mAttachInfo.mContentInsets, mAttachInfo.mStableInsets, mInputChannel);
                } catch (RemoteException e) {
                    mAdded = false;
                    mView = null;
                    mAttachInfo.mRootView = null;
                    mInputChannel = null;
                    mFallbackEventHandler.setView(null);
                    unscheduleTraversals();
                    setAccessibilityFocus(null, null);
                    throw new RuntimeException("Adding window failed", e);
                } finally {
                    if (restore) {
                        attrs.restore();
                    }
                }
                /*******部分代码省略**********/
                //设置当前View的mParent
                view.assignParent(this);
                /*******部分代码省略**********/
            }
	      }
	 }

## 2.5 ViewRootImpl.requestLayout()

    @Override
    public void requestLayout() {
        if (!mHandlingLayoutInLayoutRequest) {
			//校验线程
            checkThread();
            mLayoutRequested = true;
            scheduleTraversals();
        }
    }

- 请求对界面进行布局

- `checkThread()`,`mThread`是创建`ViewRootImpl`时被赋值的,即代表的是初始化`ViewRootImpl`所在的线程(主线程)

		void checkThread() {
	        if (mThread != Thread.currentThread()) {
	            throw new CalledFromWrongThreadException(
	                    "Only the original thread that created a view hierarchy can touch its views.");
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
		//非空,是否已经添加
		if (host == null || !mAdded)
	            return;
		//是否正在遍历
		mIsInTraversal = true;
		//是否马上绘制View
		mWillDrawSoon = true;
	
		//第一阶段:预测量
		.............
		//顶层视图DecorView所需要窗口的宽度和高度
		int desiredWindowWidth;
		int desiredWindowHeight;
		.......................

		//在构造方法中mFirst已经设置为true，表示是否是第一次绘制DecorView
		//此时窗口刚添加到WMS,并未进行relayout,因此mWinFrame中没有存储有效的窗口尺寸
		Rect frame = mWinFrame;
		if (mFirst) {
			mFullRedrawNeeded = true;
			mLayoutRequested = true;
	
			final Configuration config = mContext.getResources().getConfiguration();

			if (shouldUseDisplaySize(lp)) {
				//为状态栏设置desiredWindowWidth/Height,其取值是屏幕尺寸
				Point size = new Point();
	            mDisplay.getRealSize(size);
	            desiredWindowWidth = size.x;
	            desiredWindowHeight = size.y;
			} else {
				// ⑴ 第一次遍历,应采用可以使用的最大尺寸作为SPEC_SIZE
				desiredWindowWidth = dipToPx(config.screenWidthDp);
				desiredWindowHeight = dipToPx(config.screenHeightDp);
			}
				
			//第一次遍历,控件树即将第一次显示到窗口,填充了一个mAttachInfo中的字段,然后通过mView 发起dispatchAttachedToWindow
			//之后每一个位于控件树中的控件被添加之后都会回调onAttachedToWindow
			host.dispatchAttachedToWindow(mAttachInfo, 0);
			mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(true);
	            ........
	
		} else {
			// ⑵ 非第一次遍历,采用窗口的最新尺寸作为SPEC_SIZE
			desiredWindowWidth = frame.width();
			desiredWindowHeight = frame.height();
			//窗口的最新尺寸与ViewRootImpl中的现有尺寸不同,说明WMS单方面改变了窗口的尺寸,将引起以下三个结果
			if (desiredWindowWidth != mWidth || desiredWindowHeight != mHeight) {
			//需要完整的重绘以适应新的窗口尺寸
			mFullRedrawNeeded = true;
			//需要对控件树重新布局
			mLayoutRequested = true;
			//控件树可能拒绝接受新的窗口尺寸,可能需要窗口在布局阶段尝试设置新的窗口尺寸,(仅尝试)
			windowSizeMayChange = true;
		}

		boolean layoutRequested = mLayoutRequested && (!mStopped || mReportNextDraw);
		if (layoutRequested) {
			final Resources res = mView.getContext().getResources();
		
			if (mFirst) {
		          ......
			} else {
		            ......
				/**
				*检查WMS是否单方面改变了一些参数，标记下来，然后作为后文是否进行控件布局的条件之一
				*如果窗口的width或height被指定为WRAP_CONTENT时。表示该窗口为悬浮窗口。
				*/
				if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT
		                    || lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
					//悬浮窗口的尺寸取决于测量结果。因此有可能向WMS申请改变窗口的尺寸
					windowSizeMayChange = true;
		
					if (shouldUseDisplaySize(lp)) {
		                   //一样的设置状态栏的desiredWindowWidth/height
		                    Point size = new Point();
		                    mDisplay.getRealSize(size);
		                    desiredWindowWidth = size.x;
		                    desiredWindowHeight = size.y;
					} else {
		                // ⑶ 设置悬浮窗口的SPEC_SIZE的候选为应用可以使用的最大尺寸
		                    Configuration config = res.getConfiguration();
		                    desiredWindowWidth = dipToPx(config.screenWidthDp);
		                    desiredWindowHeight = dipToPx(config.screenHeightDp);
					}	
				}
			}
			// ⑷ 进行测量
			windowSizeMayChange |= measureHierarchy(host, lp, res,desiredWindowWidth, desiredWindowHeight);
		}


		if (layoutRequested) {
			// Clear this now, so that if anything requests a layout in the
			// rest of this function we will catch it and re-run a full
			// layout pass.
			mLayoutRequested = false;
		}
		......
		// ⑸ 判断窗口是否需要改变尺寸
        boolean windowShouldResize = layoutRequested && windowSizeMayChange
            && ((mWidth != host.getMeasuredWidth() || mHeight != host.getMeasuredHeight())
                || (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT &&
                        frame.width() < desiredWindowWidth && frame.width() != mWidth)
                || (lp.height == ViewGroup.LayoutParams.WRAP_CONTENT &&
                        frame.height() < desiredWindowHeight && frame.height() != mHeight));
        windowShouldResize |= mDragResizing && mResizeMode == RESIZE_MODE_FREEFORM;
		.............

		//第一阶段: 预测量到这里结束

		.............
		
		//第二阶段:窗口布局阶段从这里开始
        final boolean isViewVisible = viewVisibility == View.VISIBLE;
        final boolean windowRelayoutWasForced = mForceNextWindowRelayout;
        //开始进行布局准备,进入窗口布局的几个条件
        if (mFirst || windowShouldResize || insetsChanged ||viewVisibilityChanged || params != null) {
            /*******部分代码省略**********/
        	......
         	boolean hadSurface = mSurface.isValid();
         	......
          	try {
              	relayoutResult = relayoutWindow(params, viewVisibility, insetsPending);
              }catch(...){
			......
			}finally{
              ......
			}
			//第二阶段:窗口布局阶段从这里结束
	
			//第三阶段:最终测量阶段从这里开始
	        if (!mStopped || mReportNextDraw) {
	            ......
	                int childWidthMeasureSpec = getRootMeasureSpec(mWidth, lp.width);
	                int childHeightMeasureSpec = getRootMeasureSpec(mHeight, lp.height);
	                //⑴ 可以看到测量中调用的performMeasure
	                performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
	             
	                int width = host.getMeasuredWidth();
	                int height = host.getMeasuredHeight();
	                boolean measureAgain = false;
	                //⑵ 判断LayoutParams.horizontalWeight和lp.verticalWeight ，以作为是否再次测量的依据
	                if (lp.horizontalWeight > 0.0f) {
	                    width += (int) ((mWidth - width) * lp.horizontalWeight);
	                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
	                            MeasureSpec.EXACTLY);
	                    measureAgain = true;
	                }
	                if (lp.verticalWeight > 0.0f) {
	                    height += (int) ((mHeight - height) * lp.verticalWeight);
	                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
	                            MeasureSpec.EXACTLY);
	                    measureAgain = true;
	                }
					//再次测量
	                if (measureAgain) {
	                    performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
	                }
	
	                layoutRequested = true;
	            }
	        }
		}else{
			maybeHandleWindowMove(frame);
		}
		//第三阶段:最终测量阶段到这里结束
		
		//第四阶段:控件布局阶段从这里开始
		//⑴ 布局阶段的判断条件
	    final boolean didLayout = layoutRequested && (!mStopped || mReportNextDraw);
	  ......
		if (didLayout) {
	        ......
	        //⑵ 通过performLayout对控件进行布局
	        performLayout(lp, mWidth, mHeight);
	
	       ......
	       //⑶ 如果有必要，计算窗口的透明区域并把该区域设置给WMS
	        if ((host.mPrivateFlags & View.PFLAG_REQUEST_TRANSPARENT_REGIONS) != 0) {
	            
	            host.getLocationInWindow(mTmpLocation);
	            mTransparentRegion.set(mTmpLocation[0], mTmpLocation[1],
	                    mTmpLocation[0] + host.mRight - host.mLeft,
	                    mTmpLocation[1] + host.mBottom - host.mTop);
	
	            host.gatherTransparentRegion(mTransparentRegion);
	            if (mTranslator != null) {
	                mTranslator.translateRegionInWindowToScreen(mTransparentRegion);
	            }
	
	            if (!mTransparentRegion.equals(mPreviousTransparentRegion)) {
	                mPreviousTransparentRegion.set(mTransparentRegion);
	                mFullRedrawNeeded = true;
	                
	                try {
	                    mWindowSession.setTransparentRegion(mWindow, mTransparentRegion);
	                } catch (RemoteException e) {
	                }
	            }
			}
		}
		//第四阶段:控件布局阶段到这里结束

		//第五阶段:绘制阶段从这里开始
	    ......
	    boolean cancelDraw = mAttachInfo.mTreeObserver.dispatchOnPreDraw() || !isViewVisible;
	
	    if (!cancelDraw && !newSurface) {
	        if (mPendingTransitions != null && mPendingTransitions.size() > 0) {
	            for (int i = 0; i < mPendingTransitions.size(); ++i) {
	                mPendingTransitions.get(i).startChangingAnimations();
	            }
	            mPendingTransitions.clear();
	        }
	
	        performDraw();
	    } else {
	        if (isViewVisible) {
	            // Try again
	            scheduleTraversals();
	        } else if (mPendingTransitions != null && mPendingTransitions.size() > 0) {
	            for (int i = 0; i < mPendingTransitions.size(); ++i) {
	                mPendingTransitions.get(i).endChangingAnimations();
	            }
	            mPendingTransitions.clear();
	        }
	    }
	
	    mIsInTraversal = false;
	
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

# 4. performTraversals 阶段分析

## 4.1 预测量阶段(PreMeasure)

这是进入`performTraversals()`的第一个阶段.会对控件进行第一次测量,在此阶段中将会计算出控件树为显示其内容所需的尺寸(即期望窗口尺寸).这个阶段中View的子类的`onMeasure()`方法将会沿着控件树依次被回调

1. **预测量参数的候选(对应第一阶段①②③)**

	预测量也是一次完整的测量过程,与最终测量的区别仅在于参数不同.实际的测量工作是在`View`或其子类的`onMeasure()`中完成,其测量结果受限于其父控件的指示.这个指示的具体表现是`onMeasure()`中的俩个参数:`widthSpec`和`heightSpec`.

	关于`MeasureSpec` 可以参考 [View的绘制原理.md](https://github.com/iwantthink/note-view/blob/master/View_CustomView_base.md)
	
	- 第一次遍历时,选取最大的可用尺寸作为`SPEC_SIZE`候选

	- 当此窗口为悬浮窗口时,即LayoutParams.width/height 其中之一被指定为`Wrap_content`,使用最大的可用尺寸作为`SPEC_SIZE`候选

	- 其他情况下,使用窗口最新尺寸作为`SPEC_SIZE`候选

2. **测量协商(对应第一阶段④)**

	调用了`measureHierarchy()`方法,该方法用于测量整个控件树.控件树主要按照`desiredWindowWidth/Height`这俩个参数进行测量,另外在这个方法中会将窗口尽可能设计的优雅(通过与控件树的协商,但是协商仅发生在LayoutParams.width/height被指定为`WRAP_CONTENT`时,如果为`MATCH_PARENT`或固定数值 则协商过程不会发生)

	该方法会监测如果使用当前`desiredWindowWidth/Height`是否可能导致窗口尺寸变化

	**`measureHierarchy()`代码查看 #4.1.1**

3. **确定是否需要改变窗口尺寸(对应第一阶段⑤)**

	`windowSizeMayChanged`在前面的代码中多次被置为true,但是该值仅仅是窗口是否需要改变尺寸的条件之一

		boolean windowShouldResize = layoutRequested && windowSizeMayChange
	        && (
			(mWidth != host.getMeasuredWidth() || mHeight != host.getMeasuredHeight() )
	            || 
			(lp.width == ViewGroup.LayoutParams.WRAP_CONTENT &&
	         frame.width() < desiredWindowWidth && frame.width() != mWidth)
	            || 
			(lp.height == ViewGroup.LayoutParams.WRAP_CONTENT &&
	         frame.height() < desiredWindowHeight && frame.height() != mHeight));

	**必要条件**:

	- `layoutRequested`为true,表示`ViewRootImpl`的`requestLayout()`方法被调用过

	`View`类中也有`requestLayout()`方法,当控件内容发生变化从而需要调整其尺寸时,会调用自身的`requestLayout()`,并且此方法会沿着控件树向根部回溯,最终调用到`ViewRootImpl`的`requestLayout()`方法,从而引发再一次的`perfromTraversals()`

	之所以这是一个必备条件是因为`performTraversals()`还有可能因为**重绘**被调用,当控件仅仅需要重绘而不需要重新布局时(例如背景颜色发生变化),会通过`invalidate()`方法回溯到`ViewRootImpl`.此时不会通过`requestLayout()`触发`performTraversals()`,而是通过`scheduleTraversals()`方法进行触发,**这种情况下不需要进行布局窗口阶段**

	- `windowSizeMayChange`为true,表示在预测量阶段 最大宽度也不满足控件树展示的大小,那么可能就需要改变窗口的尺寸

	**可选条件**:

	- 测量结果与`ViewRootImpl`中所保存的结果有差异

	- 悬浮窗口的测量结果与窗口的最新尺寸有差异

### 4.1.1 ViewRootImpl.measureHierarchy()

    private boolean measureHierarchy(final View host, final WindowManager.LayoutParams lp,
            final Resources res, final int desiredWindowWidth, final int desiredWindowHeight) {
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
		//表示是否可能导致窗口的尺寸变化
        boolean windowSizeMayChange = false;
		//表示测量的值是否能够使控件树充分显示内容
        boolean goodMeasure = false;
        if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            // On large screens, we don't want to allow dialogs to just
            // stretch to fill the entire width of the screen to display
            // one line of text.  First try doing the layout at a smaller
            // size to see if it will fit.
			//  ⑴ 首先使用它期望的宽度限制进行测量
            final DisplayMetrics packageMetrics = res.getDisplayMetrics();
            res.getValue(com.android.internal.R.dimen.config_prefDialogWidth, mTmpValue, true);
            int baseSize = 0;
			//宽度限制保存在 baseSize中
            if (mTmpValue.type == TypedValue.TYPE_DIMENSION) {
                baseSize = (int)mTmpValue.getDimension(packageMetrics);
            }

		    //如果宽度限制不为0并且传入的desiredWindowWidth 大于measureHierarchy期望的限制宽度，
            if (baseSize != 0 && desiredWindowWidth > baseSize) {
                childWidthMeasureSpec = getRootMeasureSpec(baseSize, lp.width);
                childHeightMeasureSpec = getRootMeasureSpec(desiredWindowHeight, lp.height);
				// ⑵ 第一次测量,使用measureHierarchy期望的限制宽度 并得到状态
                performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
				// 判断状态
                if ((host.getMeasuredWidthAndState()&View.MEASURED_STATE_TOO_SMALL) == 0) {
					// 控件树对测量结果表示满意!
                    goodMeasure = true;
                } else {
                    // Didn't fit in that size... try expanding a bit.
					// ⑶ 控件树对测量结果不满意,进行第二次协商,这次将限制宽度改为 期望宽度(baseSize)和最大宽度(desiredWindowWidth)俩者和的一半
                    baseSize = (baseSize+desiredWindowWidth)/2;
                    childWidthMeasureSpec = getRootMeasureSpec(baseSize, lp.width);
					// ⑷ 第二次测量
                    performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                    // 判断结果是否满意
                    if ((host.getMeasuredWidthAndState()&View.MEASURED_STATE_TOO_SMALL) == 0) {
                        goodMeasure = true;
                    }
                }
            }
        }
		//如果俩次测量 均不能让控件树满意,那么直接使用最大宽度(desiredWindowWidth)进行测量
        if (!goodMeasure) {
            childWidthMeasureSpec = getRootMeasureSpec(desiredWindowWidth, lp.width);
            childHeightMeasureSpec = getRootMeasureSpec(desiredWindowHeight, lp.height);
            performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);

			//如果测量得到的宽度或者高度与ViewRootImpl中的窗口不一致，，那么之后可能要改变窗口的尺寸了
            if (mWidth != host.getMeasuredWidth() || mHeight != host.getMeasuredHeight()) {
                windowSizeMayChange = true;
            }
        }
        return windowSizeMayChange;
    }

- 主要测量内容在`performMeasure()`方法中

### 4.1.2 ViewRootImpl.performMeasure()


    private void performMeasure(int childWidthMeasureSpec, int childHeightMeasureSpec) {
        if (mView == null) {
            return;
        }
        try {
            mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } finally {
        }
    }

- 直接将测量转交给了 `View`,其就是`ViewRootImpl`初始化时传入的`DecorView`(`DecorView`是一个`FrameLayout`)

	`measure()`方法是final的,所以只需要在`View`中查看即可

### 4.1.3 View.measure()

    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
		
		//省略初始化操作

        if (forceLayout || needsLayout) {
            // first clears the measured dimension flag
			// ⑴ 准备工作
            mPrivateFlags &= ~PFLAG_MEASURED_DIMENSION_SET;
			....................
			// ⑵ 对当前控件 进行测量
			onMeasure(widthMeasureSpec, heightMeasureSpec);

            // ⑶ 检查onMeasure 的实现是否调用了 setMeasuredDimension()
            if ((mPrivateFlags & PFLAG_MEASURED_DIMENSION_SET) != PFLAG_MEASURED_DIMENSION_SET) {
				throw new IllegalStateException()
            }

            mPrivateFlags |= PFLAG_LAYOUT_REQUIRED;
        }

        mOldWidthMeasureSpec = widthMeasureSpec;
        mOldHeightMeasureSpec = heightMeasureSpec;
		//缓存
        mMeasureCache.put(key, ((long) mMeasuredWidth) << 32 |
                (long) mMeasuredHeight & 0xffffffffL); // suppress sign extension
    }
