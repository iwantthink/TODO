# View的绘制流程分析

[Android开发艺术探索]()

[从ViewRootImpl类分析View绘制的流程](https://blog.csdn.net/feiduclear_up/article/details/46772477)

[深入理解Android 卷III 网络版](https://www.kancloud.cn/alex_wsc/android-deep3/416457)

[从源码分析View的绘制流程](http://zhangruifeng.top/2018/06/14/%E4%BB%8E%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90View%E7%9A%84%E7%BB%98%E5%88%B6%E6%B5%81%E7%A8%8B/)

[Android开发之漫漫长途 Ⅴ——Activity的显示之ViewRootImpl的预测量、窗口布局、最终测量、布局、绘制](https://segmentfault.com/a/1190000012018189)


# 1. 简介

从`Window`的相关文章中得知,在`ActivityThread`的`performLaunchActivity()`中,会回调`Activity`的`onCreate()`方法,而`onCreate()`方法正是调用`setContentView()`的地方,但是在这里仅仅只是创建了`DecorView`

真正将`DecorView`添加到`Window`并进行关联的过程是在`ActivityThread.handleResumeActivity()`中

**重点分析`ViewRootImpl`类**

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

- 在方法的最后调用了`view.assignParent()`,即将`ViewRootImpl`设置给了`DecorView`,作为其父类存在

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

- `mChoreographer`是`Choreographer`类型,其内部包含了一个`Handler`,它在`ViewRootImpl`的构造函数中被创建,而`ViewRootImpl`由`WindowManager`创建并初始化

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

            performTraversals();
			....................
        }
    }

## 2.9 ViewRootImpl.performTraversals()

**从这一步开始,View开始绘制,它经过`measure`,`layout`,`draw`三个过程将View绘制出来**.其中`measure`用来测量View的宽高,`layout`用来确定View在父容器中的位置,`draw`负责将View绘制在屏幕上

# 3. ViewRootImpl的绘制过程

`ViewRootImpl.performTraversals()`方法可以划分为五个阶段:

![](http://ww1.sinaimg.cn/large/6ab93b35ly1fx0pu8gaxtj20mb0b3mz3.jpg)


1. 预测量阶段

	这是进入`performTraversals()`方法后的第一阶段,会对控件树进行第一次的测量,测量结果可以通过`mView.getMeasuredWidth()/Height()`获得. 在此阶段中将会计算出控件树为显示其内容所需的尺寸(即期望尺寸).此阶段中,`View`及其子类的`onMeasure()`将会沿着控件树依次被调用

2. 布局窗口阶段

	根据预测量的结果，通过`IWindowSession.relayout()`方法向WMS请求调整窗口的尺寸等属性，这将引发WMS对窗口进行重新布局，并将布局结果返回给ViewRootImpl。

3. 最终测量阶段

	预测量的结果是控件树所期望的窗口尺寸。然而由于在WMS中影响窗口布局的因素很多，WMS不一定会将窗口准确地布局为控件树所要求的尺寸，而迫于WMS作为系统服务的强势地位，控件树不得不接受WMS的布局结果。因此在这一阶段，`performTraversals()`将以窗口的实际尺寸对控件进行最终测量。在这个阶段中，View及其子类的`onMeasure()`方法将会沿着控件树依次被回调。

4. 布局控件树阶段

	完成最终测量之后便可以对控件树进行布局了。测量确定的是控件的尺寸，而布局则是确定控件的位置。在这个阶段中，View及其子类的`onLayout()`方法将会被回调。

5. 绘制阶段

	这是`performTraversals()`的最终阶段。确定了控件的位置与尺寸后，便可以对控件树进行绘制了。在这个阶段中，View及其子类的`onDraw()`方法将会被回调。

## 3.1 ViewRootImpl.performTraversals()

	private void performTraversals() {
		//mView就是DecorView,在WMG中VRI被创建,并传入了DV
		//保存在局部变量中,以提高访问效率
		final View host = mView;
		//非空,是否已经添加
		if (host == null || !mAdded)
	            return;
		//是否正在遍历
		mIsInTraversal = true;
		//是否马上绘制View
		mWillDrawSoon = true;
		//mWindowAttributes 即WindowManager.LayoutParams
		//默认的lp.width/height是  MATCH_PARENT
        WindowManager.LayoutParams lp = mWindowAttributes;	
		//mWinFrame是由WMS赋值的窗口最新大小
		Rect frame = mWinFrame;

		//第一阶段:预测量
		.............
		//顶层视图DecorView所需要窗口的宽度和高度
		int desiredWindowWidth;
		int desiredWindowHeight;
		.......................

		//在构造方法中mFirst已经设置为true，表示是否是第一次绘制DecorView
		//此时窗口刚添加到WMS,并未进行relayout,因此mWinFrame中没有存储有效的窗口尺寸
		Rect frame = mWinFrame;
		//mFirst表示这是第一次遍历,此时窗口刚被添加到WMS,尚未进行`relayout()`,因此mWinFrame并没有存储有效的窗口大小!
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
			//控件树可能拒绝接受新的窗口尺寸,可能需要窗口在布局阶段尝试设置新的窗口尺寸
			windowSizeMayChange = true;
		}

		............................
        // Execute enqueued actions on every traversal in case a detached view enqueued an action
        getRunQueue().executeActions(mAttachInfo.mHandler);


		//layoutRequested为true表示在进行“遍历”之前requestLayout()方法被调用过。
		// requestLayout()方法用于要求ViewRootImpl进行一次“遍历”并对控件树重新进行测量与布局
		boolean layoutRequested = mLayoutRequested && (!mStopped || mReportNextDraw);
		if (layoutRequested) {
			final Resources res = mView.getContext().getResources();
		
			if (mFirst) {
		          ......
				//确认控件树是否需要进入TOUCH_MODE
			} else {
		            ......
				/**
				检查WMS是否单方面改变了一些参数，标记下来，然后作为后文是否进行控件布局的条件之一
				*如果窗口的width或height被指定为WRAP_CONTENT时。表示该窗口为悬浮窗口。此时会对desiredWindowWidth/Height 进行调整

				在前面的代码中，这两个值被设置为窗口的当前尺寸。
				而根据MeasureSpec的要求，测量结果不得大于SPEC_SIZE。
				然而，如果这个悬浮窗口需要更大的尺寸以完整显示其内容时，
				例如为AlertDialog设置了一个更长的消息内容，如此取值将导致无法得到足够大的测量结果，从而导致内容无法完整显示。
				因此，对于此等类型的窗口，ViewRootImpl会调整desiredWindowWidth/Height为此应用可以使用的最大尺寸
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

			//重置 mLayoutRequested标志,这个标志在requestLayout()中被置true
			// 因此只要之后的代码执行过程中,任何一个控件执行了requestLayout(),那么就会重新进行遍历
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
		//记录布局窗口之前的Surface版本号
		final int surfaceGenerationId = mSurface.getGenerationId();


        //开始进行布局准备,进入窗口布局的几个条件
        if (mFirst || windowShouldResize || insetsChanged ||viewVisibilityChanged || params != null) {
            /*******部分代码省略**********/
        	......
			//  记录下在布局窗口之前是否拥有一块有效的Surface
         	boolean hadSurface = mSurface.isValid();
         	......
          	try {
				// 通过 relayoutWindow()方法布局窗口
              	relayoutResult = relayoutWindow(params, viewVisibility, insetsPending);
				
				// 对比布局结果,检查Insets是否发生变化
				// 省略大部分对比的代码
				final boolean visibleInsetsChanged = !mPendingVisibleInsets.equals(
                        mAttachInfo.mVisibleInsets);
				// 如果发生变化,则要更新到mAttachInfo中
                if (contentInsetsChanged) {
                    mAttachInfo.mContentInsets.set(mPendingContentInsets);
                }
				
				..................................

              }catch(...){
			......
			}finally{
              ......
			}
			//第二阶段:窗口布局阶段从这里结束

			//省略代码, 对Surface进行处理

			//保存窗口的位置和尺寸信息
            mAttachInfo.mWindowLeft = frame.left;
            mAttachInfo.mWindowTop = frame.top;			
            if (mWidth != frame.width() || mHeight != frame.height()) {
                mWidth = frame.width();
                mHeight = frame.height();
            }

	
			//第三阶段:最终测量阶段从这里开始
	        if (!mStopped || mReportNextDraw) {
                boolean focusChangedDueToTouchMode = ensureTouchModeLocally(
                        (relayoutResult&WindowManagerGlobal.RELAYOUT_RES_IN_TOUCH_MODE) != 0);

				//进行最终测量的条件: 
				//1. TouchMode 发生变化
				//2. 最新的窗口尺寸不符合预测量结果
				//3. ContentInsets发生变化
                if (focusChangedDueToTouchMode || mWidth != host.getMeasuredWidth()
                        || mHeight != host.getMeasuredHeight() || contentInsetsChanged ||
                        updatedConfiguration) {
					//最终生成MeasureSpec的参数 为窗口的最新尺寸
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
			//执行到这里,说明不符合执行布局窗口的条件,即窗口的尺寸不需要进行调整
			//这种情况下,可能是窗口的位置发生了变化,那么仅需将窗口的最新位置保存到mAttachInfo中即可
			maybeHandleWindowMove(frame);
		}
		//第三阶段:最终测量阶段到这里结束
		
		//第四阶段:控件布局阶段从这里开始
		//⑴ 布局阶段的判断条件
	    final boolean didLayout = layoutRequested && (!mStopped || mReportNextDraw);
	  ......
		if (didLayout) {
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
				// 将透明区域设置到WMS
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
		// 当mView不可见 自然不需要绘制
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


### 3.1.2 View.post(Runnable action)

    public boolean post(Runnable action) {
        final AttachInfo attachInfo = mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mHandler.post(action);
        }

        // Postpone the runnable until we know on which thread it needs to run.
        // Assume that the runnable will be successfully placed after attach.
        getRunQueue().post(action);
        return true;
    }

在进行多线程任务时，开发者可以通过调用`View.post()`或`View.postDelayed()`方法将一个Runnable对象发送到主线程执行。这两个方法的原理是将`Runnable`对象发送到`ViewRootImpl`的`mHandler`去。

当控件已经加入到控件树时，可以通过`AttachInfo`轻易获取这个`Handler`。而当控件没有位于控件树中时，则没有mAttachInfo可用，此时执行`View.post()/PostDelay()`方法，Runnable将会被添加到这个RunQueue队列中。
      
在`performTraversals()`中，`ViewRootImpl`将会把`RunQueue`中的Runnable发送到mHandler中，进而得到执行。所以无论控件是否显示在控件树中，`View.post()/postDelay()`方法都是可用的，除非当前进程中没有任何处于活动状态的`ViewRootImpl`

	getRunQueue().executeActions(mAttachInfo.mHandler);


# 4. performTraversals 阶段分析

## 4.1 预测量阶段(PreMeasure)

这是进入`performTraversals()`的第一个阶段.会对控件进行第一次测量,在此阶段中将会计算出控件树为显示其内容所需的尺寸(即期望窗口尺寸).这个阶段中View的子类的`onMeasure()`方法将会沿着控件树依次被回调

1. **预测量参数的候选(对应第一阶段①②③)**

	预测量也是一次完整的测量过程,与最终测量的区别仅在于参数不同.实际的测量工作是在`View`或其子类的`onMeasure()`中完成,其测量结果受限于其父控件的指示.这个指示的具体表现是`onMeasure()`中的俩个参数:`widthSpec`和`heightSpec`.

	关于`MeasureSpec` 可以参考 [View的绘制原理.md](https://github.com/iwantthink/note-view/blob/master/View_CustomView_base.md)

	**预测量时`SPEC_SIZE`的取值规则**:
	
	- 第一次遍历时,选取最大的可用尺寸作为`SPEC_SIZE`候选

	- 当此窗口为悬浮窗口时,即`LayoutParams.width/height `其中之一被指定为`Wrap_content`,使用最大的可用尺寸作为`SPEC_SIZE`候选

	- 其他情况下,使用窗口最新尺寸作为`SPEC_SIZE`候选

2. **测量协商(对应第一阶段④)**

	调用了`measureHierarchy()`方法,该方法用于测量整个控件树.控件树主要按照`desiredWindowWidth/Height`这俩个参数进行测量,这俩个参数本可以直接作为控件树的测量参考,但是`measureHierarchy()`方法 仍然会对其进行修改,这是针对将`LayoutParams.width/height`设置为了`WRAP_CONTENT`的悬浮窗口而言

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

	- `windowSizeMayChange`为true,表示WMS单方面改变了窗口尺寸而控件树的测量结果与这一尺寸有差异，或当前窗口为悬浮窗口，其控件树的测量结果将决定窗口的新尺寸

	**可选条件**:

	- 测量结果与`ViewRootImpl`中所保存的结果有差异

	- 悬浮窗口的测量结果与窗口的最新尺寸有差异


在预测量阶段,除了对控件树进行预测量之外,还准备为后续阶段准备了所需的参数

1. `viewVisibilityChanged`:即View的可见性是否发生了变化。由于`mView`是窗口的内容，因此`mView`的可见性即是窗口的可见性。当这一属性发生变化时，需要通过通过WMS改变窗口的可见性。

2. `LayoutParams`:预测量阶段需要收集应用到`LayoutParams`的改动，这些改动一方面来自于`WindowManager.updateViewLayout()`,而另一方面则来自于控件树。

	以`SystemUIVisibility`为例，`View.setSystemUIVisibility()`所修改的设置需要反映到`LayoutParams`中，而这些设置确却保存在控件自己的成员变量里。在预测量阶段会通过`ViewRootImpl.collectViewAttributes()`方法遍历控件树中的所有控件以收集这些设置，然后更新`LayoutParams`。


### 4.1.1 ViewRootImpl.measureHierarchy()

    private boolean measureHierarchy(final View host, final WindowManager.LayoutParams lp,
            final Resources res, final int desiredWindowWidth, final int desiredWindowHeight) {
		// MeasureSpec 用于描述宽度 和高度
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
		//表示测量结果是否可能导致窗口的尺寸发生变化
        boolean windowSizeMayChange = false;
		//表示测量的值是否能够使控件树充分显示内容
        boolean goodMeasure = false;
		//测量协商仅发生在 LayoutParams被指定为`WRAP_CONTENT`的情况下
        if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            // On large screens, we don't want to allow dialogs to just
            // stretch to fill the entire width of the screen to display
            // one line of text.  First try doing the layout at a smaller
            // size to see if it will fit.
			//  ⑴ 首先使用它最期望的宽度限制进行测量
            final DisplayMetrics packageMetrics = res.getDisplayMetrics();
			//这一宽度限制定义保存在frameworks/base/core/res/res/values/config.xml找到它的定义
            res.getValue(com.android.internal.R.dimen.config_prefDialogWidth, mTmpValue, true);
			//宽度限制保存在 baseSize中
            int baseSize = 0;
            if (mTmpValue.type == TypedValue.TYPE_DIMENSION) {
                baseSize = (int)mTmpValue.getDimension(packageMetrics);
            }

		    //如果宽度限制不为0并且传入的desiredWindowWidth 大于measureHierarchy期望的限制宽度，
            if (baseSize != 0 && desiredWindowWidth > baseSize) {
				// 组合一个新的MeasureSpec,利用baseSize
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
		//如果俩次测量 均不能让控件树满意,那么直接使用最大宽度(desiredWindowWidth)进行测量.这一次是最终测量,即使不满意 也不会再进行测量,因为已经没有多余控件了
        if (!goodMeasure) {
            childWidthMeasureSpec = getRootMeasureSpec(desiredWindowWidth, lp.width);
            childHeightMeasureSpec = getRootMeasureSpec(desiredWindowHeight, lp.height);
            performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);

			//如果测量得到的宽度或者高度与ViewRootImpl中的窗口不一致，，那么之后可能要改变窗口的尺寸了
            if (mWidth != host.getMeasuredWidth() || mHeight != host.getMeasuredHeight()) {
                windowSizeMayChange = true;
            }
        }
		//返回窗口尺寸是否可能发生变化
        return windowSizeMayChange;
    }

- 主要测量内容在`performMeasure()`方法中

- 对于非悬浮窗口，即当`LayoutParams.width`被设置为`MATCH_PARENT`时，不存在协商过程，直接使用给定的`desiredWindowWidth/Height`进行测量即可。
	
	**而对于悬浮窗口，`measureHierarchy()`可以连续进行两次让步。因而在最不利的情况下，在ViewRootImpl的一次“遍历”中，控件树需要进行三次测量，即控件树中的每一个View.onMeasure()会被连续调用三次之多。所以相对于onLayout()，onMeasure()方法的对性能的影响比较大。**


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
        		
		//省略内容为 :: 针对LAYOUT_MODE_OPTICAL_BOUNDS 这种情况,修改MeasureSpec

        // Suppress sign extension for the low bytes
        long key = (long) widthMeasureSpec << 32 | (long) heightMeasureSpec & 0xffffffffL;
        if (mMeasureCache == null) mMeasureCache = new LongSparseLongArray(2);
		// 判断是否需要强制重新布局
        final boolean forceLayout = (mPrivateFlags & PFLAG_FORCE_LAYOUT) == PFLAG_FORCE_LAYOUT;

        // Optimize layout by avoiding an extra EXACTLY pass when the view is
        // already measured as the correct size. In API 23 and below, this
        // extra pass is required to make LinearLayout re-distribute weight.
        final boolean specChanged = widthMeasureSpec != mOldWidthMeasureSpec
                || heightMeasureSpec != mOldHeightMeasureSpec;
        final boolean isSpecExactly = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY
                && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY;
        final boolean matchesSpecSize = getMeasuredWidth() == MeasureSpec.getSize(widthMeasureSpec)
                && getMeasuredHeight() == MeasureSpec.getSize(heightMeasureSpec);
        final boolean needsLayout = specChanged
                && (sAlwaysRemeasureExactly || !isSpecExactly || !matchesSpecSize);
		//仅当MeasureSpec 发生变化,或者被要求强制重新布局时,才会进行测量
        if (forceLayout || needsLayout) {
            // ⑴ 准备工作
            mPrivateFlags &= ~PFLAG_MEASURED_DIMENSION_SET;

            resolveRtlPropertiesIfNeeded();

            int cacheIndex = forceLayout ? -1 : mMeasureCache.indexOfKey(key);
            if (cacheIndex < 0 || sIgnoreMeasureCache) {
                // measure ourselves, this should set the measured dimension flag back
				//⑵ 对当前控件 进行测量
                onMeasure(widthMeasureSpec, heightMeasureSpec);
                mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
            } else {
                long value = mMeasureCache.valueAt(cacheIndex);
                // Casting a long to int drops the high 32 bits, no mask needed
                setMeasuredDimensionRaw((int) (value >> 32), (int) value);
                mPrivateFlags3 |= PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
            }

            // flag not set, setMeasuredDimension() was not invoked, we raise
            // an exception to warn the developer
			//⑶ 检查onMeasure 的实现是否调用了 setMeasuredDimension()
            if ((mPrivateFlags & PFLAG_MEASURED_DIMENSION_SET) != PFLAG_MEASURED_DIMENSION_SET) {
                throw new IllegalStateException(".....);
            }

            mPrivateFlags |= PFLAG_LAYOUT_REQUIRED;
        }

        mOldWidthMeasureSpec = widthMeasureSpec;
        mOldHeightMeasureSpec = heightMeasureSpec;

        mMeasureCache.put(key, ((long) mMeasuredWidth) << 32 |
                (long) mMeasuredHeight & 0xffffffffL); // suppress sign extension
    }

- 从`View.measure()`方法中可以看出,其并没有实现任何测量算法,它的作用在于引发`onMeasure()`的调用,并对`onMeasure()`的准确性进行检查

	另外,在控件系统看来,一旦控件执行了测量操作,那么随后必须进行布局操作,因此在完成测量之后,将`PFLAG_LAYOUT_REQUIRED`标记加入了`mPrivateFlags`,以便`View.layout()`顺利进行

	**`onMeasure()`方法中获得的测量结果,则通过`setMeasuredDimension()`方法进行保存**,具体分析查看 #### 4.1.4

-  所谓强制重新布局，是指当控件树中的一个子控件的内容发生变化时，需要进行重新的测量和布局的情况

	在这种情况下，这个子控件的父控件（以及其父控件的父控件）所提供的`MeasureSpec`必定与上次测量时的值相同，因而导致从`ViewRootImpl`到这个控件的路径上的父控件的`measure()`方法无法得到执行

    进而导致子控件无法重新测量其尺寸或布局。因此，当子控件因内容发生变化时，从子控件沿着控件树回溯到`ViewRootImpl`，并依次调用沿途父控件的`requestLayout()`方法，在这个方法中，会在`mPrivateFlags`中加入标记`PFLAG_FORCE_LAYOUT`，从而使得这些父控件的`measure()`方法得以顺利执行，进而这个子控件有机会进行重新测量与布局。这便是强制重新布局的意义

- **`onMeasure()`如何将测量结果告知父控件?**

	对于非`ViewGroup`的控件来说其实现相对简单，只要按照`MeasureSpec`的原则如实计算其所需的尺寸即可。而对于ViewGroup类型的控件来说情况则复杂得多，因为它不仅拥有自身需要显示的内容（如背景），它的子控件也是其需要测量的内容。因此它不仅需要计算自身显示内容所需的尺寸，还有考虑其一系列子控件的测量结果。为此它必须为每一个子控件准备`MeasureSpec`，并调用每一个子控件的`measure()`函数。

### 4.1.4 View.setMeasuredDimension()

    protected final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        boolean optical = isLayoutModeOptical(this);
        if (optical != isLayoutModeOptical(mParent)) {
            Insets insets = getOpticalInsets();
            int opticalWidth  = insets.left + insets.right;
            int opticalHeight = insets.top  + insets.bottom;

            measuredWidth  += optical ? opticalWidth  : -opticalWidth;
            measuredHeight += optical ? opticalHeight : -opticalHeight;
        }
        setMeasuredDimensionRaw(measuredWidth, measuredHeight);
    }

    private void setMeasuredDimensionRaw(int measuredWidth, int measuredHeight) {
        mMeasuredWidth = measuredWidth;
        mMeasuredHeight = measuredHeight;

        mPrivateFlags |= PFLAG_MEASURED_DIMENSION_SET;
    }

- 存储测量结果的俩个变量可以通过`getMeasuredWidthAndState()/getMeasuredHeightAndState()`获得

- 此方法虽然简单，但需要注意，**与`MeasureSpec`类似，测量结果不仅仅是一个尺寸，而是一个测量状态与尺寸的复合变量**。

	其0至30位表示了测量结果的尺寸，而31、32位则表示了控件对测量结果是否满意，即父控件给予的MeasureSpec是否可以使得控件完整地显示其内容。当控件对测量结果满意时，直接将尺寸传递给`setMeasuredDimension()`即可，注意要保证31、32位为0。倘若对测量结果不满意，则使用`View.MEASURED_STATE_TOO_SMALL | measuredSize `作为参数传递给`setMeasuredDimension()`以告知父控件对MeasureSpec进行可能的调整。

## 4.2 布局窗口阶段(WindowLayout)

根据**预测量**的结果,通过`IWindowSession.relayout()`方法想WMS请求调整窗口的尺寸等属性,这将引起WMS对窗口重新布局,并将布局结果返回给`ViewRootImpl`.

如果布局结果使得窗口尺寸发生变化,那么最终测量阶段将会被执行. 最终测量阶段使用`performMeasure()`方法完成,其过程与预测量完全一致,区别在于`MeasureSpec`参数的不同

布局窗口会对`Surface`产生影响,这个阶段会出现硬件加速相关的内容.

- 布局窗口能够进行的原因是控件系统有修改窗口属性的需求

	例如第一次'遍历'需要确定窗口的尺寸以及一块`Surface`,预测量结果与窗口当前尺寸不一致,则需要窗口更改尺寸.

	`mView`可见性发生变化,则需要将窗口隐藏或显示等..

### 4.2.1 布局窗口的条件

窗口布局的开销很大,因此必须限制窗口布局阶段的执行. 

另外如果不需要进行窗口布局,则WMS不会在预测量之后修改窗口的尺寸,这种情况下,预测量值是有效的!同时不需要再进行最终测量

	if (mFirst || windowShouldResize || insetsChanged ||
                viewVisibilityChanged || params != null || mForceNextWindowRelayout) {
			//布局窗口与最终测量阶段的代码
	}else{
		//不需要进行布局窗口的情况
	}

1. `mFirst`

	表示这是窗口创建以来的第一次遍历,此时窗口仅仅是添加到了WMS中,但是尚未进行窗口布局,并没有有效的`Surface`进行内容绘制.因此必须进行窗口布局

2. `windowShouldResize` :

	当控件树的测量结果与窗口当前的尺寸有差异,需要通过布局窗口阶段向WMS提出修改窗口大小的请求以满足控件树的要求

3. `insetsChanged`: 

	表示WMS单方面改变了窗口的`ContentInsets`.这种情况一般发生在`SystemUI`的可见性发生了变化或输入法窗口弹出或关闭的情况

	严格来说,这种情况并不需要重新进行窗口布局,只不过`ContentInsets`发生变化时需要执行一段渐变动画使窗口内容过度到新的`ContentInsets`下,而这段动画的启动动作发生在窗口布局阶段

4. `viewVisibilityChanged`:

	`DecorView`是`ViewRootImpl`具体内容的体现,这个条件表示 其可见度发生了变化,那么就需要重新进行布局阶段

5. `params!=null`:

	在`performTraversals()`方法开始,该值被设置为null. 后续窗口使用者可以通过`WindowManager.updateViewLayout()`方法修改窗口的`LayoutParams`,或者在预测量阶段通过`collectViewAttributes()`方法收集到的控件属性使得`LayoutParams`发生改变

	那就需要将新的`LayoutParams`通过窗口布局更新到WMS中,使其对窗口依照新的属性进行重新布局

6. `mForceNextWindowRelayout`:

### 4.2.2 布局窗口

`ViewRootImpl.relayoutWindow()`是`IWindowSession.relayout()`的包装方法,它将窗口的`LayoutParams`,预测量结果以及`mView`的可见度等作为输入,获得`mWindowFrame`,`mPendingContentInsets`,`mSurface`等作为输出

- `ViewRootImpl.relayoutWIndow()`方法并没有直接将预测量结果交给WMS,而是乘上了`appScale`这个系数

	`appScale`用于在兼容模式下显示下一个窗口.当窗口在设备的屏幕尺寸下显示异常时,Android会尝试使用兼容尺寸显示它,此时测量与布局控件树都将以此兼容尺寸为准

- `mPendingCOnfiguration`就是一个`Configuration`类型的实例,其意义是WMS给予窗口的当前配置

	其内容包含了 设备当前的语言,屏幕尺寸,输入方式,UI模式(夜间模式,车载模式),dpi等等信息


### 4.2.3 布局窗口后的处理

更新`mAttachInfo`中的参数

## 4.3 最终测量阶段(EndMeasure)

预测量结果是控件树期待的窗口尺寸,但是在WMS中影响布局的因素很多,WMS不一定会将窗口的尺寸调整为控件树所要求的尺寸,反而是控件树要听从WMS的布局结果(WMS作为系统服务)

在这个阶段中View及其子类的`onMeasure()`方法将会沿着控件树依次被回调。最终测量阶段直接调用`performMeasure()`方法而不是`measureHierarchy()`方法，是因为`measureHierarchy()`有个协商过程，而到了最终测量阶段控件树已经没有了协商的余地，无论控件树是否接收，它只能被迫接受WMS的布局结果

最终测量阶段与预测量阶段最大的区别就是:

- **预测量使用了屏幕的可用空间或窗口的当前尺寸作为候选大小,然后通过`measureHierarchy()`方法以协商的方式确定`MeasureSpec`,其结果体现了控件树所期望的窗口尺寸**.在窗口布局时这一参数会传递给WMS，WMS去处理具体

- **最终测量阶段,控件树只能接受WMS的布局结果,以最新的窗口大小作为`MeasureSpec`进行测量**

## 4.4 布局控件树阶段(Layout)

这一阶段主要的工作内容就是将上一步的最终测量结果作为依据对控件进行布局. 测量是确定控件的尺寸,布局则是确定控件的位置.**在这个阶段中,`View`及其子类的`onLayout()`方法将会被回调**

控件的实际位置与尺寸由`View`的`mLeft,mTop,mRight,mBottom`四个成员变量存储的坐标值进行表示,因此,控件树的布局过程就是根据测量结果为每一个控件设置这个四个值

- `mLeft`等坐标值 是相对于父控件的左上角的距离

总体来说 **布局控件阶段** 做了俩件事情

### 4.4.1 控件树布局

在这个阶段 主要调用了`performLayout()`函数

    private void performLayout(WindowManager.LayoutParams lp, int desiredWindowWidth,
            int desiredWindowHeight) {
        mLayoutRequested = false;
        mScrollMayChange = true;
        mInLayout = true;

        final View host = mView;
        if (host == null) {
            return;
        }
        try {
            host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
			.......省略代码.........
        } finally {
        }
        mInLayout = false;
    }

- `performLayout()`方法中,具体的布局操作一样是调用`View`的`layout()`


---

布局阶段把测量结果转化为控件的实际位置与尺寸,而控件的实际位置与尺寸由`View`的`mLeft`,`mTop`,`mRight`,`mBottom`这四个成员变量存储.

- 也就是说控件树的布局过程就是根据测量结果为每一个控件设置这个四个成员变量的过程,`mLeft、mTop、mRight、mBottom` 是相对于父控件的坐标值。


`View.layout()`代码如下:


    public void layout(int l, int t, int r, int b) {
        if ((mPrivateFlags3 & PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT) != 0) {
            onMeasure(mOldWidthMeasureSpec, mOldHeightMeasureSpec);
            mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
        }

        int oldL = mLeft;
        int oldT = mTop;
        int oldB = mBottom;
        int oldR = mRight;

        boolean changed = isLayoutModeOptical(mParent) ?
                setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);
		// PFLAG_LAYOUT_REQUIRED 是在View.measure()中被设置
        if (changed || (mPrivateFlags & PFLAG_LAYOUT_REQUIRED) == PFLAG_LAYOUT_REQUIRED) {

			//如果该View是ViewGroup类型,则在其onLayout()方法中会依次调用子类的layout()方法
            onLayout(changed, l, t, r, b);
			//清除PFLAG_LAYOUT_REQUIRED 标志
            mPrivateFlags &= ~PFLAG_LAYOUT_REQUIRED;
			//调用监听回调,告诉这些监听者 ,布局发生变化
            ListenerInfo li = mListenerInfo;
            if (li != null && li.mOnLayoutChangeListeners != null) {
                ArrayList<OnLayoutChangeListener> listenersCopy =
                        (ArrayList<OnLayoutChangeListener>)li.mOnLayoutChangeListeners.clone();
                int numListeners = listenersCopy.size();
                for (int i = 0; i < numListeners; ++i) {
                    listenersCopy.get(i).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
                }
            }
        }
			.....省略代码....
    }

- `View`中的`onLayout()`是空实现

- `setOpticalFrame()`最终也是调用的`setFrame()`方法,将传入的`l,t,r,b`设置给`mLeft,mRight...`等


---
**测量阶段和布局阶段的对比:**

- 测量确定的是控件的尺寸,在一定程度上确定了子控件的位置.

	布局则是根据测量结果来实施,并最终确定子控件位置

- 测量结果对布局过程没有约束力. 子控件在`onMeasure()`方法中计算出了自身应有的尺寸,但是由于`layout()`方法是由父控件调用的,因此父控件最终决定控件的位置尺寸,测量结果仅仅是一个参考


- 一般来说,子控件的测量结果影响父控件,因此测量过程是由子控件到根控件.

	布局过程则相反,是由父控件的布局结果影响子控件的布局结果,所以布局过程是由根控件到子控件


### 4.4.2 设置透明区域

布局阶段的另一个工作是计算并设置窗口的透明区域。这一功能主要是为`SurfaceView`服务。



## 4.5 绘制阶段(Draw)

这是`performTraversals()`的最后阶段(`performDraw()`)。确定控件的尺寸和位置后。便进行对控件树的绘制。在这个阶段中View及其子类的`onDraw()`方法将会被回调。


- 在开发Android自定义控件时，往往都需要重写`View.onDraw()`方法以绘制内容到一个给定的`Canvas`中。

	 `Canvas`是一个绘图工具类，其API提供了一系列绘图指定供开发者使用。这些指令可以分为两个部分：

	1. **绘制指令**:这些最常用的指令由一系列名为`drawXXX()`的方法提供。它们用来实现实际的绘制行为，例如绘制点、线、圆以及方块等

	2. **辅助指令**:这些用于提供辅助功能的指令将会影响后续指令的效果。如变换、裁剪区域等。这些辅助指令不如上面的绘制指令那么直观，但是在Android的绘制过程中大量使用了辅助指令。在这些辅助指令中，最常用的莫过于变换指令了。变换指令包括`translate`(平移坐标系),`rotate`(旋转坐标系),`scale`(缩放坐标系)等，这些指令很大的帮助了控件树的绘制。

		其实只要想一想我们在重写onDraw()函数时从未考虑过控件的位置、旋转、缩放等状态。这说明在onDraw()方法执行之前，这些状态都已经以变换的方式设置到Canvas中了。因此onDraw()方法中Canvas使用的是控件自身的坐标系。

- `View.onDraw()`也是空实现,具体的逻辑交给子类去实现

### 4.5.1 ViewRootImpl.performDraw()

    private void performDraw() {

        final boolean fullRedrawNeeded = mFullRedrawNeeded;
        mFullRedrawNeeded = false;
        mIsDrawing = true;

        try {
            draw(fullRedrawNeeded);
        } finally {
            mIsDrawing = false;
        }
		`````````省略代码`````````````
    }

### 4.5.2 ViewRootImpl.draw()


    private void draw(boolean fullRedrawNeeded) {
        Surface surface = mSurface;
		.....
		// 计算mView在垂直方向的滚动量(ScrollY),
        scrollToRectOrFocus(null, false);
		//mScroller 保存了上述代码中的滚动量,ViewRootImpl使用mScroller产生一个动画效果,使滚动不显示那么突兀
		//类似于一个插值器,用于计算本次绘制时间点所需使用的滚动量
        boolean animating = mScroller != null && mScroller.computeScrollOffset();
        final int curScrollY;
        if (animating) {
			//如果mScroller正在执行滚动动画,则采用mScroller所计算的滚动量
            curScrollY = mScroller.getCurrY();
        } else {
			//如果已经结束,则使用上面的scrollToRectOrFocus()所计算出的滚动量
            curScrollY = mScrollY;
        }
		//如果新计算出的滚动量与上次绘制的滚动量不同,则需要进行完整的重绘
		//因为发生滚动时,整个画面都是需要更新的!!!
        if (mCurScrollY != curScrollY) {
            mCurScrollY = curScrollY;
            fullRedrawNeeded = true;
            if (mView instanceof RootViewSurfaceTaker) {
                ((RootViewSurfaceTaker) mView).onRootViewScrollYChanged(mCurScrollY);
            }
        }
		.........省略代码............

		//如果需要进行完整重绘,则修改脏区域为整个窗口
        if (fullRedrawNeeded) {
            mAttachInfo.mIgnoreDirtyState = true;
            dirty.set(0, 0, (int) (mWidth * appScale + 0.5f), (int) (mHeight * appScale + 0.5f));
        }

     	.........省略代码............

        if (!dirty.isEmpty() || mIsAnimating || accessibilityFocusDirty) {
			//当满足下列条件,表示此窗口采用硬件加速的绘制方式
			//硬件加速绘制入口是HardwareRenderer
            if (mAttachInfo.mThreadedRenderer != null && mAttachInfo.mThreadedRenderer.isEnabled()) {
				.........省略代码............
                mAttachInfo.mThreadedRenderer.draw(mView, mAttachInfo, this);
            } else {
				//软件绘制的入口 drawSoftware()
                if (!drawSoftware(surface, mAttachInfo, xOffset, yOffset, scalingRequired, dirty)) {
                    return;
                }
            }
        }
		//如果mScroller 仍在动画过程之中,则立即安排下一次重绘
        if (animating) {
            mFullRedrawNeeded = true;
            scheduleTraversals();
        }
    }

- 在`ViewRootImpl.setView()`方法中,会调用`enableHardwareAcceleration()`方法,该方法中会根据窗口的`LayoutParams`判断是否开启硬件加速,并将结果保存在`mAttachInfo`中


# 5. 绘制流程

## 5.1 软件绘制的原理

    private boolean drawSoftware(Surface surface, AttachInfo attachInfo, int xoff, int yoff,
            boolean scalingRequired, Rect dirty) {

        // 使用软件渲染器绘制,定义绘制所需的canvas
        final Canvas canvas;
        try {
			......省略代码..........
			//通过Surface.lockCanvas()获取一个依次Surface为画布的Canvas
			// 其参数为脏区域
            canvas = mSurface.lockCanvas(dirty);
			......省略代码..........
        } catch (................) {
            return false;
        } 

        try {
			//绘制开始之前,先清空之前所计算的脏区域
			//这样如果在绘制的过程中执行了View.invalidate(),则可以重新计算脏区域
            dirty.setEmpty();
            mIsAnimating = false;
            mView.mPrivateFlags |= View.PFLAG_DRAWN;

            try {
				//使用Canvas进行第一次交换,此次变化的目的是使得坐标系统按照之前所计算的滚动量进行相应的滚动
				//随后绘制的内容都会在滚动后的新坐标下进行
                canvas.translate(-xoff, -yoff);
                if (mTranslator != null) {
                    mTranslator.translateCanvas(canvas);
                }
                canvas.setScreenDensity(scalingRequired ? mNoncompatDensity : 0);
                attachInfo.mSetIgnoreDirtyState = false;
				// 通过mView.draw()在Canvas上绘制整个控件树
                mView.draw(canvas);

                drawAccessibilityFocusedDrawableIfNeeded(canvas);
            } finally {
            }
        } finally {
            try {
				// 最后步骤,通过Surface.unlockCanvasAndPost()方法显示绘制后的内容
                surface.unlockCanvasAndPost(canvas);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }

- 返回结果true,表示绘制成功,false 表示绘制时出现错误

- `drawSoftware()`主要的四步工作:

	1. 通过`Surface.lockCanvas()`获取一个用于绘制的Canvas

	2. 对Canvas进行变化以实现滚动效果

	3. 通过`mView.draw()`将根控件绘制在Canvas上

	4. 通过`Surface.unlockCanvasAndPost()`显示绘制后的内容

## 5.2 View.draw(Canvas)绘制流程

`View.draw()`绘制过程中,可以划分为 简便绘制流程 和 完整绘制流程

## 5.2.1 简便绘制流程

    public void draw(Canvas canvas) {
        final int privateFlags = mPrivateFlags;
		//检查PFLAG_DIRTY_OPAQUEz是否存在于mPrivateFlags中,以确定是否是'实心'控件
        final boolean dirtyOpaque = (privateFlags & PFLAG_DIRTY_MASK) == PFLAG_DIRTY_OPAQUE &&
                (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
        mPrivateFlags = (privateFlags & ~PFLAG_DIRTY_MASK) | PFLAG_DRAWN;

        /*
         * Draw traversal performs several drawing steps which must be executedin the appropriate order:
         *
         *      1. Draw the background
         *      2. If necessary, save the canvas' layers to prepare for fading
         *      3. Draw view's content
         *      4. Draw children
         *      5. If necessary, draw the fading edges and restore layers
         *      6. Draw decorations (scrollbars for instance)
         */

        // Step 1, draw the background, if needed
        int saveCount;
		//为了提高效率,'实心'控件的背景绘制会被跳过
        if (!dirtyOpaque) {
            drawBackground(canvas);
        }

        // skip step 2 & 5 if possible (common case)
		// step2-5 通常情况下会被跳过
        final int viewFlags = mViewFlags;
        boolean horizontalEdges = (viewFlags & FADING_EDGE_HORIZONTAL) != 0;
        boolean verticalEdges = (viewFlags & FADING_EDGE_VERTICAL) != 0;
		//控件不需要绘制渐变边界,那么可以直接进行简便绘制流程
        if (!verticalEdges && !horizontalEdges) {
            // Step 3, draw the content
			// 绘制控件自身内容
            if (!dirtyOpaque) onDraw(canvas);

            // Step 4, draw the children
			//绘制控件的子控件,如果当前控件不是ViewGroup,那么该方法什么都不做
            dispatchDraw(canvas);

            drawAutofilledHighlight(canvas);

            // Overlay is part of the content and draws beneath Foreground
            if (mOverlay != null && !mOverlay.isEmpty()) {
                mOverlay.getOverlayView().dispatchDraw(canvas);
            }

            // Step 6, draw decorations (foreground, scrollbars)
            onDrawForeground(canvas);

            // Step 7, draw the default focus highlight
            drawDefaultFocusHighlight(canvas);

            // we're done...
            return;
        }
		
		...........省略完整绘制流程...............
    }

- `drawBackground()`
	
	    private void drawBackground(Canvas canvas) {
	        final Drawable background = mBackground;
				.........省略代码........
			//将背景绘制到Canvas上
	        final int scrollX = mScrollX;
	        final int scrollY = mScrollY;
	        if ((scrollX | scrollY) == 0) {
	            background.draw(canvas);
	        } else {
				//draw(canvas) 方法是在控件自身的坐标系下调用的
				//就是说Canvas已经根据其`mScrollX/Y`对Canvas进行了变化以实现控件滚动的效果,从而所绘制的背景也会被滚动
				//不过Android 希望仅滚动控件的内容,而保持背景静止
				//因此在绘制背景时,先进行逆变换,以撤销先前实行的滚动变换,完成背景绘制之后再将滚动变换重新应用到Canvas
	            canvas.translate(scrollX, scrollY);
	            background.draw(canvas);
	            canvas.translate(-scrollX, -scrollY);
	        }
	    }

- 简便绘制过程非常简单

	1. 绘制背景(背景不会受滚动影响)

	2. 通过调用`onDraw()`方法绘制控件自身的内容

	3. 通过调用`dispatchDraw()`绘制其子控件

	4. 绘制控件的装饰

### 5.2.1.1 确定子控件绘制的顺序,View.dispatchDraw(Canvas)
此方法是重绘工作得以从根控件`mView`延续到控件树中每一个子控件的重要过程

**`View.dispatchDraw()`默认是空实现,需要到`ViewGroup`中查看**

    protected void dispatchDraw(Canvas canvas) {
        boolean usingRenderNodeProperties = canvas.isRecordingFor(mRenderNode);
        final int childrenCount = mChildrenCount;
        final View[] children = mChildren;
        int flags = mGroupFlags;
		
		..........省略动画相关..............

        int clipSaveCount = 0;
		//① 设置剪裁区域
		//有时候子控件可能部分或者完全位于ViewGroup之外,默认情况下,ViewGroup的下列代码可以通过Canvas.clipRect()方法将子控件的绘制限制在自身区域之内
		//超出自身区域的绘制内容将被裁剪
		//是否需要对越界内容进行裁剪取决于ViewGroup.mGroupFlags中是否包含CLIP_TOP_PADDING_MASK标记,因此开发者可以通过ViewGroup.setClipToPadding()方法修改这一行为,使得子控件在超出范围 仍被绘制

        final boolean clipToPadding = (flags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK;
        if (clipToPadding) {
			//首先保存Canvas的状态,随后可以通过Canvas.restore()方法恢复到这个状态
            clipSaveCount = canvas.save(Canvas.CLIP_SAVE_FLAG);
			//保证给定区域之外的绘制都会被裁剪
			// padding 的值也会被去除
            canvas.clipRect(mScrollX + mPaddingLeft, mScrollY + mPaddingTop,
                    mScrollX + mRight - mLeft - mPaddingRight,
                    mScrollY + mBottom - mTop - mPaddingBottom);
        }

        // We will draw our child's animation, let's reset the flag
        mPrivateFlags &= ~PFLAG_DRAW_ANIMATION;
        mGroupFlags &= ~FLAG_INVALIDATE_REQUIRED;

        boolean more = false;
		//获取当前时间戳,用于子控件计算其动画参数
        final long drawingTime = getDrawingTime();

		.........省略部分看不懂的代码............
        for (int i = 0; i < childrenCount; i++) {
			........动画操作相关,省略代码...........
			// 判断是否自定义了绘制顺序..通过控制 子类的index 从而控制绘制顺序
            final int childIndex = getAndVerifyPreorderedIndex(childrenCount, i, customOrder);
            final View child = getAndVerifyPreorderedView(preorderedList, children, childIndex);
            if ((child.mViewFlags & VISIBILITY_MASK) == VISIBLE || child.getAnimation() != null) {
                more |= drawChild(canvas, child, drawingTime);
            }
        }

		//绘制透明的控件
        while (transientIndex >= 0) {
            // there may be additional transient views after the normal views
            final View transientChild = mTransientViews.get(transientIndex);
            if ((transientChild.mViewFlags & VISIBILITY_MASK) == VISIBLE ||
                    transientChild.getAnimation() != null) {
                more |= drawChild(canvas, transientChild, drawingTime);
            }
            transientIndex++;
            if (transientIndex >= transientCount) {
                break;
            }
        }

		.............省略动画处理相关内容.............

		// 撤销之前所做的裁剪操作
        if (clipToPadding) {
            canvas.restoreToCount(clipSaveCount);
        }
		............省略动画处理相关内容.............
    }

- 本方法中,最重要的内容就是定义重绘顺序,绘制顺序对于子控件来说意义重大,因为当多个控件存在重叠时,后绘制的控件会覆盖先前绘制的控件

	默认情况下,后加入ViewGroup的子控件位于`mChildren`数组的尾部,因此绘制顺序与加入顺序一致

	ViewGroup可以通过`ViewGroup.setChildrenDrawingOrderEnabled()`方法将`FALG_USE_CHILD_DRAWING_ORDER`标记加入`mGroupFlags`,并重写`getChildDrawingOrder()`方法来自定义绘制顺序

- 在确定绘制顺序之后,便通过`ViewGroup.drawChild()`方法绘制子控件

	`drawChild()`仅仅只是调用了子控件的`View.draw(ViewGroup,Canvas,Long)`方法进行子控件的绘制

#### 5.2.1.2 View.draw(ViewGroup,Canvas,Long)
该方法的工作内容 就是为随后调用`View.draw(Canvas)`准备坐标系,此外还包括硬件加速,绘图缓存和动画计算等工作

    /**
     * This method is called by ViewGroup.drawChild() to have each child view draw itself.
     *
     * This is where the View specializes rendering behavior based on layer type,
     * and hardware acceleration.
     */
    boolean draw(Canvas canvas, ViewGroup parent, long drawingTime) {
		//是否开启硬件加速的标志
        final boolean hardwareAcceleratedCanvas = canvas.isHardwareAccelerated();
		//如果控件处于动画中,transformToApply会存储动画在当前时点所计算出的Transformation
        Transformation transformToApply = null;
		// 进行动画的计算,并将结果保存在transformToApply中
		// 这是进行坐标变换的第一个因素
		.............省略代码.................

		// 计算控件内容的滚动量
		// 计算是通过computeScroll()方法完成的,其将结果保存在mScrollX/Y中
		// 这是进行坐标变换的第二个因素
        int sx = 0;
        int sy = 0;
        if (!drawingWithRenderNode) {
			//自定义滑动控件时,通常会重写该方法,并设置mScrollX/Y
            computeScroll();
            sx = mScrollX;
            sy = mScrollY;
        }

        final boolean drawingWithDrawingCache = cache != null && !drawingWithRenderNode;
        final boolean offsetForScroll = cache == null && !drawingWithRenderNode;
		//保存Canvas的当前状态
		// 此时Canvas的坐标系为父控件的坐标系
		// 在随后将Canvas变换到此控件的坐标系并完成绘制后,会通过Canvas.restoreTo()方法将Canvas状态重置到此时
		// 这样Canvas即可继续用来绘制父控件的下一个子控件
        int restoreTo = -1;
        if (!drawingWithRenderNode || transformToApply != null) {
            restoreTo = canvas.save();
        }

		// 第一次变换,对应控件位置和滚动量
		// 最先处理的是子控件的位置mLeft/mTop,以及滚动量
		// 子控件的位置mLeft/mTop 这是进行坐标变换的第三个因素
        if (offsetForScroll) {
            canvas.translate(mLeft - sx, mTop - sy);
        } else {
            if (!drawingWithRenderNode) {
                canvas.translate(mLeft, mTop);
            }
            if (scalingRequired) {
                if (drawingWithRenderNode) {
                    // TODO: Might not need this if we put everything inside the DL
                    restoreTo = canvas.save();
                }
                // mAttachInfo cannot be null, otherwise scalingRequired == false
                final float scale = 1.0f / mAttachInfo.mApplicationScale;
                canvas.scale(scale, scale);
            }
        }

        float alpha = drawingWithRenderNode ? 1 : (getAlpha() * getTransitionAlpha());

		// 如果此控件的动画所计算出的变换存在(即有动画在执行),或者通过View.setScaleX/Y()等方法修改了控件自身的变换,则将它们所产生的变化矩阵应用到Canvas
        if (transformToApply != null
                || alpha < 1
                || !hasIdentityMatrix()
                || (mPrivateFlags3 & PFLAG3_VIEW_IS_ANIMATING_ALPHA) != 0) {
            if (transformToApply != null || !childHasIdentityMatrix) {
                int transX = 0;
                int transY = 0;
				//记录滚动量
                if (offsetForScroll) {
                    transX = -sx;
                    transY = -sy;
                }
				//将动画产生的变化矩阵应用到Canvas
                if (transformToApply != null) {
                    if (concatMatrix) {
						//表示使用硬件加速
                        if (drawingWithRenderNode) {
                            renderNode.setAnimationMatrix(transformToApply.getMatrix());
                        } else {
                            //应用动画产生的矩阵到Canvas
							//首先撤销了滚动量的变化,在动画的变换矩阵应用到Canvas之后,再重新应用滚动量变换
                            canvas.translate(-transX, -transY);
                            canvas.concat(transformToApply.getMatrix());
                            canvas.translate(transX, transY);
                        }
                        parent.mGroupFlags |= ViewGroup.FLAG_CLEAR_TRANSFORMATION;
                    }

                    float transformAlpha = transformToApply.getAlpha();
                    if (transformAlpha < 1) {
                        alpha *= transformAlpha;
                        parent.mGroupFlags |= ViewGroup.FLAG_CLEAR_TRANSFORMATION;
                    }
                }
				// 将控件自身的变换矩阵应用到Canvas中
				//控件自身的变换矩阵是进行坐标系变换的第四个因素
                if (!childHasIdentityMatrix && !drawingWithRenderNode) {
                    canvas.translate(-transX, -transY);
                    canvas.concat(getMatrix());
                    canvas.translate(transX, transY);
                }
            }

			.............省略代吗..............
        } else if ((mPrivateFlags & PFLAG_ALPHA_SET) == PFLAG_ALPHA_SET) {
            onSetAlpha(255);
            mPrivateFlags &= ~PFLAG_ALPHA_SET;
        }

		// 非硬件加速的情况下,直接进行剪裁
		...........省略代码............
	
		//不使用绘图缓存
        if (!drawingWithDrawingCache) {
            if (drawingWithRenderNode) {
                mPrivateFlags &= ~PFLAG_DIRTY_MASK;
				//使用硬件加速的方式绘制控件
                ((DisplayListCanvas) canvas).drawRenderNode(renderNode);
            } else {
                // 使用变换过的Canvas 进行最终绘制
				// 在完成了坐标系的变换之后,Canvas已经位于控件自身的坐标系之下,也就可以通过draw(Canvas)进行控件内容的实际绘制工作,这样一来,绘制流程便回到了 简单绘制流程,而 dispatchDraw()则会继续绘制其子类

				//PFLAG_SKIP_DRAW 会跳过draw()方法,直接调用子类绘制
				//这是一种优化,对于大多数ViewGroup而言,其onDraw()内容为空
                if ((mPrivateFlags & PFLAG_SKIP_DRAW) == PFLAG_SKIP_DRAW) {
                    mPrivateFlags &= ~PFLAG_DIRTY_MASK;
                    dispatchDraw(canvas);
                } else {
                    draw(canvas);
                }
            }
        } else if (cache != null) {
       		.........省略代码,使用绘图缓存绘制控件...................
        }

		//恢复Canvas的状态 在一切开始之前
		// Canvas回到父控件的坐标系,这样便允许 父控件在调用`dispatchDraw()`过程中将Canvas 交给下一个子控件,避免了互相影响
        if (restoreTo >= 0) {
            canvas.restoreToCount(restoreTo);
        }
		............省略代码.............
		//该值来自动画计算,动画若继续,则more 为true
        return more;
    }

- **坐标系变换**,将Canvas从父控件的坐标系变换到子控件的坐标系一次需要变换如下参数

	1. 控件在父控件中的位置,即`mLeft/mTop`.
	
		使用了`Canvas.translate()`方法

	2. 控件动画过程中所产生的矩阵.

		绘制过程中,控件可能正在进行一个或多个动画(ScaleAnimation,RotateAnimation,TranslateAnimation等),这些动画根据当前的时间点计算出`Transformation`,再将其中所包含的变换矩阵通过`Canvas.concact()`方法设置给Canvas,是的坐标系发生变换

	3. 控件自身的变换矩阵

		除了动画可以产生矩阵使得控件发生动画效果之外,View类还提供了`setScaleX/Y()`,`setTranslationX/Y()`等方法使得控件产生上述效果

		这一系列方法所设置的变换信息会被整合在`View.mTransformationInfo`成员变量中,并可以通过`VIew.getMatrix()`方法从这个成员变量中提取一个整合了所有变换信息的矩阵

		这个矩阵会在`View.draw(VIewGroup,Canvas,long)`方法中被应用

	4. 控件内容的滚动量,即`mScrollX/Y`

		虽然滚动量在一开始通过`Canvas.translate()`进行了变换,但是在进行另外俩种矩阵变换时,都会先撤销变换,待完成变换之后,再重新应用滚动量

		这说明滚动量是最后被应用的~

- Canvas针对4个因素进行坐标系变换之后,其坐标系已经是控件自身的坐标系了,接着调用`draw(canvas)`进行控件内容的绘制,通过`onDraw()`绘制自身内容. 然后继续通过`dispatchDraw()`方法遍历子类
		

### 5.2.2 完整绘制流程

    public void draw(Canvas canvas) {
        final int privateFlags = mPrivateFlags;
        final boolean dirtyOpaque = (privateFlags & PFLAG_DIRTY_MASK) == PFLAG_DIRTY_OPAQUE &&
                (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
        mPrivateFlags = (privateFlags & ~PFLAG_DIRTY_MASK) | PFLAG_DRAWN;

        // Step 1, draw the background, if needed
        int saveCount;

        if (!dirtyOpaque) {
            drawBackground(canvas);
        }

		...............省略简便绘制流程....................

        /*
         * Here we do the full fledged routine...
         * (this is an uncommon case where speed matters less,
         * this is why we repeat some of the tests that have been
         * done above)
         */

        boolean drawTop = false;
        boolean drawBottom = false;
        boolean drawLeft = false;
        boolean drawRight = false;

        float topFadeStrength = 0.0f;
        float bottomFadeStrength = 0.0f;
        float leftFadeStrength = 0.0f;
        float rightFadeStrength = 0.0f;

        // Step 2, save the canvas' layers
        int paddingLeft = mPaddingLeft;

        final boolean offsetRequired = isPaddingOffsetRequired();
        if (offsetRequired) {
            paddingLeft += getLeftPaddingOffset();
        }

        int left = mScrollX + paddingLeft;
        int right = left + mRight - mLeft - mPaddingRight - paddingLeft;
        int top = mScrollY + getFadeTop(offsetRequired);
        int bottom = top + getFadeHeight(offsetRequired);

        if (offsetRequired) {
            right += getRightPaddingOffset();
            bottom += getBottomPaddingOffset();
        }

        final ScrollabilityCache scrollabilityCache = mScrollCache;
        final float fadeHeight = scrollabilityCache.fadingEdgeLength;
        int length = (int) fadeHeight;

        // clip the fade length if top and bottom fades overlap
        // overlapping fades produce odd-looking artifacts
        if (verticalEdges && (top + length > bottom - length)) {
            length = (bottom - top) / 2;
        }

        // also clip horizontal fades if necessary
        if (horizontalEdges && (left + length > right - length)) {
            length = (right - left) / 2;
        }

        if (verticalEdges) {
            topFadeStrength = Math.max(0.0f, Math.min(1.0f, getTopFadingEdgeStrength()));
            drawTop = topFadeStrength * fadeHeight > 1.0f;
            bottomFadeStrength = Math.max(0.0f, Math.min(1.0f, getBottomFadingEdgeStrength()));
            drawBottom = bottomFadeStrength * fadeHeight > 1.0f;
        }

        if (horizontalEdges) {
            leftFadeStrength = Math.max(0.0f, Math.min(1.0f, getLeftFadingEdgeStrength()));
            drawLeft = leftFadeStrength * fadeHeight > 1.0f;
            rightFadeStrength = Math.max(0.0f, Math.min(1.0f, getRightFadingEdgeStrength()));
            drawRight = rightFadeStrength * fadeHeight > 1.0f;
        }

        saveCount = canvas.getSaveCount();

        int solidColor = getSolidColor();
        if (solidColor == 0) {
            final int flags = Canvas.HAS_ALPHA_LAYER_SAVE_FLAG;

            if (drawTop) {
                canvas.saveLayer(left, top, right, top + length, null, flags);
            }

            if (drawBottom) {
                canvas.saveLayer(left, bottom - length, right, bottom, null, flags);
            }

            if (drawLeft) {
                canvas.saveLayer(left, top, left + length, bottom, null, flags);
            }

            if (drawRight) {
                canvas.saveLayer(right - length, top, right, bottom, null, flags);
            }
        } else {
            scrollabilityCache.setFadeColor(solidColor);
        }

        // Step 3, draw the content
        if (!dirtyOpaque) onDraw(canvas);

        // Step 4, draw the children
        dispatchDraw(canvas);

        // Step 5, draw the fade effect and restore layers
        final Paint p = scrollabilityCache.paint;
        final Matrix matrix = scrollabilityCache.matrix;
        final Shader fade = scrollabilityCache.shader;

        if (drawTop) {
            matrix.setScale(1, fadeHeight * topFadeStrength);
            matrix.postTranslate(left, top);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(left, top, right, top + length, p);
        }

        if (drawBottom) {
            matrix.setScale(1, fadeHeight * bottomFadeStrength);
            matrix.postRotate(180);
            matrix.postTranslate(left, bottom);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(left, bottom - length, right, bottom, p);
        }

        if (drawLeft) {
            matrix.setScale(1, fadeHeight * leftFadeStrength);
            matrix.postRotate(-90);
            matrix.postTranslate(left, top);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(left, top, left + length, bottom, p);
        }

        if (drawRight) {
            matrix.setScale(1, fadeHeight * rightFadeStrength);
            matrix.postRotate(90);
            matrix.postTranslate(right, top);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(right - length, top, right, bottom, p);
        }

        canvas.restoreToCount(saveCount);

        drawAutofilledHighlight(canvas);

        // Overlay is part of the content and draws beneath Foreground
        if (mOverlay != null && !mOverlay.isEmpty()) {
            mOverlay.getOverlayView().dispatchDraw(canvas);
        }

        // Step 6, draw decorations (foreground, scrollbars)
        onDrawForeground(canvas);

        if (debugDraw()) {
            debugDrawFocus(canvas);
        }
    }


## 5.2 硬件绘制的原理

`ViewRootImpl.draw()`方法中可以看到,如果`mAttachInfo.mHardwareRenderer`存在并且有效,则会使用硬件加速的方式绘制控件树. 相较于软件绘制,硬件加速绘制可以充分利用GPU性能,提高绘制效率

### 5.2.1 硬件加速绘制简介

如果窗口使用硬件加速,`ViewRootImpl`会创建一个`ThreadedRenderer`并保存在`mAttachInfo`中

`ThreadedRenderer`用于硬件加速的渲染器,封装了硬件加速的图形库,并以Android与硬件加速图形库的中间层的身份存在.负责从Android的Surface生成一个`HardwareLayer`,供硬件加速图形库作为绘制的输出目标



### 5.2.2 硬件加速绘制的流程

`ViewRootImpl.draw()`方法中,如果存在硬件加速,则会调用

	mAttachInfo.mThreadedRenderer.draw(mView, mAttachInfo, this);

- `mAttachInfo`: `View.AttachInfo`类

- `mThreadRenderer`: `ThreadedRenderer`类

#### 5.2.2.1 ThreadedRenderer.draw()


    void draw(View view, AttachInfo attachInfo, DrawCallbacks callbacks) {
        attachInfo.mIgnoreDirtyState = true;

        final Choreographer choreographer = attachInfo.mViewRootImpl.mChoreographer;
        choreographer.mFrameInfo.markDrawStart();

        updateRootDisplayList(view, callbacks);

        attachInfo.mIgnoreDirtyState = false;

        // register animating rendernodes which started animating prior to renderer
        // creation, which is typical for animators started prior to first draw
        if (attachInfo.mPendingAnimatingRenderNodes != null) {
            final int count = attachInfo.mPendingAnimatingRenderNodes.size();
            for (int i = 0; i < count; i++) {
                registerAnimatingRenderNode(
                        attachInfo.mPendingAnimatingRenderNodes.get(i));
            }
            attachInfo.mPendingAnimatingRenderNodes.clear();
            // We don't need this anymore as subsequent calls to
            // ViewRootImpl#attachRenderNodeAnimator will go directly to us.
            attachInfo.mPendingAnimatingRenderNodes = null;
        }

        final long[] frameInfo = choreographer.mFrameInfo.mFrameInfo;
        int syncResult = nSyncAndDrawFrame(mNativeProxy, frameInfo, frameInfo.length);
        if ((syncResult & SYNC_LOST_SURFACE_REWARD_IF_FOUND) != 0) {
            setEnabled(false);
            attachInfo.mViewRootImpl.mSurface.release();
            // Invalidate since we failed to draw. This should fetch a Surface
            // if it is still needed or do nothing if we are no longer drawing
            attachInfo.mViewRootImpl.invalidate();
        }
        if ((syncResult & SYNC_INVALIDATE_REQUIRED) != 0) {
            attachInfo.mViewRootImpl.invalidate();
        }
    }

# 6. 使用绘图缓存
