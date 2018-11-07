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

            // !!FIXME!! This next section handles the case where we did not get the
            // window size we asked for. We should avoid this by getting a maximum size from
            // the window session beforehand.
            if (mWidth != frame.width() || mHeight != frame.height()) {
                mWidth = frame.width();
                mHeight = frame.height();
            }

	
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

根据**预测量**的结果,通过`IWindowSession.relayout()`方法想WMS请求调整窗口的尺寸等属性,这将引起WMS对窗口重新布局,并将布局结果返回给`ViewRootImpl`

- 布局窗口能够进行的原因是控件系统有修改窗口属性的需求
	例如第一次'遍历'需要确定窗口的尺寸以及一块`Surface`,预测量结果与窗口当前尺寸不一致,则需要窗口更改尺寸.

	`mView`可见性发生变化,则需要将窗口隐藏或显示等..


## 4.3 最终测量阶段(EndMeasure)

预测量结果是控件树期待的窗口尺寸,但是在WMS中影响布局的因素很多,WMS不一定会将窗口的尺寸调整为控件树所要求的尺寸,反而是控件树要听从WMS的布局结果(WMS作为系统服务)

在这个阶段中View及其子类的`onMeasure()`方法将会沿着控件树依次被回调。最终测量阶段直接调用`performMeasure()`方法而不是`measureHierarchy()`方法，是因为`measureHierarchy()`有个协商过程，而到了最终测量阶段控件树已经没有了协商的余地，无论控件树是否接收，它只能被迫接受WMS的布局结果


## 4.4 布局控件树阶段(Layout)

这一阶段主要的工作内容就是将上一步的最终测量结果作为依据对控件进行布局. 测量是确定控件的尺寸,布局则是确定控件的位置.**在这个阶段中,`View`及其子类的`onLayout()`方法将会被回调**

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

也就是说控件树的布局过程就是根据测量结果为每一个控件设置这个四个成员变量的过程,`mLeft、mTop、mRight、mBottom` 是相对于父控件的坐标值。


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