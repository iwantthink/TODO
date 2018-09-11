# 1. 监听

添加监听的位置在:MessageProcessor->monitorViewTreeChange

1. OnGlobalLayoutListener

2. OnScrollChangedListener

3. OnGlobalFocusChangeListener

Method:

    public void saveNewWindowImpressionDelayed() {
        ThreadUtils.postOnUiThreadDelayed(new Runnable() {
            public void run() {
                MessageProcessor.this.saveAllWindowImpress(true);
            }
        }, 500L);
    }


    private Runnable mSaveAllWindowImpression = new Runnable() {
        public void run() {
            MessageProcessor.this.flushPendingPageEvent();
            MessageProcessor.this.saveAllWindowImpress();
        }
    };


## 1. GrowingIO 注册

	Configuration configuration = new Configuration()
		.useID()
		.trackAllFragments()
		.setChannel("hmt_channel")
		.setDebugMode(true)
		.setTestMode(true);
	GrowingIO.startWithConfiguration(this, configuration);

## 1.2 GrowingIO.startWithConfiguration()

1. 判断是否已经初始化

2. 判断Android版本,不支持4.2以下

3. 必须在主线程中初始化

4. 判断是否处于RN开发模式.通过`application.getResources()`来获取一些配置属性

5. `PermissionUtil`检查权限情况.通过传入的`Configuration`初始化`GConfig`

6. 初始化`APPState`

7. 判断是否是Sample, 去创建不同的`GrowingIO`对象返回

## 1.3 GrowingIO构造函数

1. `MessageProcessor`进行初始化

2. 创建`ActivityLifecycleCallbacksRegistrar`接口,该接口用来注册和解绑`ActivityLifeCallback`接口. 通过`getAppState()`获取到`AppState`对象并注册(AppState本身实现了`ActivityLifecycleCallbacks`)

3. 通过`MessageProcessor.getInstance()`获取`AppState.ActivityStateListener`接口的实现类`MessageProcessor`对象本身,并添加到 `AppState`对象中

3. `GConfig.sCanHook = true;`

4. hook instrumentation

5. 创建`ArgumentChecker`,用来检查数据的有效性


## 1.4 生命周期的转发

`AppState`类实现了`ActivityLifeCallbacks`接口,并被注册到了系统中

`MessageProcessor`类实现了`AppState.ActivityStateListener`,并被添加到了`AppState`对象中的`ArrayList mStateChangeListeners`

    @UiThread
    public boolean addActivityStateChangeListener(AppState.ActivityStateListener listener) {
        if(listener != null && !this.mStateChangeListeners.contains(listener)) {
            this.mStateChangeListeners.add(listener);
            return true;
        } else {
            return false;
        }
    }

在`ActivityLifecycleCallbacks`的回调中,会去调用注册到`AppState`对象中的`ActivityStateListener`

- 跟Activity相关的分别是`onActivityResumed`,`onActivityPaused`,`onActivityStopped`,`onActivityDestroyed`


## 1.5 ActivityLifecycleCallbacks

    public interface ActivityLifecycleCallbacks {
        void onActivityCreated(Activity activity, Bundle savedInstanceState);
        void onActivityStarted(Activity activity);
        void onActivityResumed(Activity activity);
        void onActivityPaused(Activity activity);
        void onActivityStopped(Activity activity);
        void onActivitySaveInstanceState(Activity activity, Bundle outState);
        void onActivityDestroyed(Activity activity);
    }

## 1.6 ActivityStateListener

    public interface ActivityStateListener {
        void onResumed(Activity var1);

        void onResumed(Fragment var1);

        void onResumed(android.support.v4.app.Fragment var1);

        void onResumed(View var1);

        void onPaused(Activity var1);

        void onPaused(Fragment var1);

        void onPaused(android.support.v4.app.Fragment var1);

        void onPaused(View var1);

        void onDestroy(Activity var1);

        void onDeactivated(Activity var1);
    }



# 2. MessageProcessor

## 2.1 生命周期转发

    public void onResumed(Activity activity) {
        PageObserver.post(this.getAppState().getForegroundActivity());
		//判断是否是 包含在另一个Activity中的 Activity
        if(!activity.isChild()) {
			// 获取其HashCode
            this.mCurrentRootWindowsHashCode = activity.getWindow().getDecorView().hashCode();
        }
		
        if(this.mGConfig.isEnabled()) {
            try {
				// 注册网络状态监听的广播
                activity.getApplicationContext().registerReceiver(this.mNetworkReceiver, this.mNetworkFilter);
                this.getAppState().setNetworkListening(true);
            } catch (Exception var3) {
                ;
            }

            this.savePage(activity);
            this.clearActionCalculatorMap();
            this.saveAllWindowImpressionDelayed();
            LogUtil.d("GIO.MessageProcessor", new Object[]{"Activity.onResumed: saveAllWindowImpressionDelayed"});
        }
    }

## 2.2 savePage(Activity activity)

    private void savePage(Activity activity) {
		// 判断当前屏幕是否处于点亮状态
        if(this.isLegalPageEvent()) {
            this.mPTM = System.currentTimeMillis();
            ThreadUtils.cancelTaskOnUiThread(this.mResendPageEventTask);
			// 在 flushPendingPageEvent() 中被用到
            this.mPendingPageEvent = new Pair(new WeakReference(activity), new PageEvent(activity, this.mLastPageName, this.mPTM));
        } else {
            SessionManager.forResumeNoPageBug();
        }

    }

     this.mResendPageEventTask = new Runnable() {
                public void run() {
                    MessageProcessor.this.mFullRefreshingPage = false;
                    MessageProcessor.this.forceRefresh(refreshImpression, refreshPtm);
                }
            };

## 2.3 saveAllWindowImpressionDelayed()

    public void saveAllWindowImpressionDelayed() {
        ThreadUtils.cancelTaskOnUiThread(this.mSaveAllWindowImpression);
        ThreadUtils.postOnUiThreadDelayed(this.mSaveAllWindowImpression, 200L);
    }

    private Runnable mSaveAllWindowImpression = new Runnable() {
        public void run() {
            MessageProcessor.this.flushPendingPageEvent();
            MessageProcessor.this.saveAllWindowImpress();
        }
    };

## 2.4 flushPendingPageEvent()

    private void flushPendingPageEvent() {
        if(this.mPendingPageEvent != null) {
			//判断是否是新进入一个页面
            if(SessionManager.enterNewPage()) {
                this.saveVisit(((PageEvent)this.mPendingPageEvent.second).mPageName);
            }

            this.persistEvent((VPAEvent)this.mPendingPageEvent.second);
            JSONObject pVar = null;
            if(this.mPendingPageEvent.first != null) {
                pVar = (JSONObject)this.mPendingPageVariables.get(((WeakReference)this.mPendingPageEvent.first).get());
            }

            if(pVar != null) {
                this.persistEvent((VPAEvent)(new PageVariableEvent((PageEvent)this.mPendingPageEvent.second, pVar)));
                this.mPendingPageVariables.remove(((WeakReference)this.mPendingPageEvent.first).get());
            }

            this.mLastPageEvent = this.mPendingPageEvent;
            this.mPendingPageEvent = null;
        }

    }

## 2.5 saveAllWindowImpress()

    public void saveAllWindowImpress(boolean onlyNewWindow) {
        GConfig config = GConfig.getInstance();
		//判断是否发送type = imp 的数据
        if(config != null && config.shouldSendImp()) {
			//获取当前最前台的Activity
            Activity activity = this.getAppState().getForegroundActivity();
            if(activity != null) {
				//初始化Window 等字段的信息
                WindowHelper.init();
				//获取WindowManager中的mViews字段,通常得到的是DecorView
                View[] windowRootViews = WindowHelper.getWindowViews();
                ArrayList<ActionCalculator> newWindowCalculators = new ArrayList();
				// 如果当前Window的数量大于1 ,则忽略掉其他的Activity
                boolean skipOtherActivity = ViewHelper.getMainWindowCount(windowRootViews) > 1;
                View[] var7 = windowRootViews;
                int var8 = windowRootViews.length;

                for(int var9 = 0; var9 < var8; ++var9) {
                    View root = var7[var9];
                    if(root != null) {
						// 获取当前rootView的 window类型
                        String prefix = this.getWindowPrefix(root);
                        if(!"/Ignored".equals(prefix) && ViewHelper.isWindowNeedTraverse(root, prefix, skipOtherActivity) && this.findCalculatorByWindow(root) == null) {
                            ActionCalculator actionCalculator = new ActionCalculator(this.getAppState().getPageName(activity), this.mPTM, root, prefix);
                            this.mActionCalculatorMap.put(new WeakReference(root), actionCalculator);
                            newWindowCalculators.add(actionCalculator);
                            this.monitorViewTreeChange(root);
                        }
                    }
                }

                Object calculators;
                if(onlyNewWindow) {
                    calculators = newWindowCalculators;
                } else {
                    calculators = this.mActionCalculatorMap.values();
                }

                Iterator var14 = ((Collection)calculators).iterator();

                while(var14.hasNext()) {
                    ActionCalculator actionCalculator = (ActionCalculator)var14.next();
                    this.saveImpress(actionCalculator);
                }

                if(newWindowCalculators.size() > 0) {
                    CircleManager.getInstance().refreshWebCircleTasks();
                }

                CircleManager.getInstance().updateTagsIfNeeded();
            }
        }
    }

## 2.6 saveImpress(ActionCalculator calculator)

    private void saveImpress(ActionCalculator calculator) {
        if(calculator != null) {
            List<ActionEvent> events = calculator.obtainImpress();
            if(events == null) {
                return;
            }

            Iterator var3 = events.iterator();

            while(var3.hasNext()) {
                ActionEvent event = (ActionEvent)var3.next();
                this.persistEvent((VPAEvent)event);
            }
        }

    }

# 3. PageObserver

## 3.1 post(Activity activity)

    public static void post(Activity activity) {
        mHander.removeCallbacks(callback);
        callback.setActivity(activity);
        mHander.postDelayed(callback, 300L);
    }

- `mHander`在主线程中执行

## 3.2 Callback

	try {
		if(this.mActivity.get() == null) {
			LogUtil.e("GIO.PageObserver", "mActivity == null");
			return;
		}
		//获取到DecorView
		View[] windows = new View[]{((Activity)this.mActivity.get()).getWindow().getDecorView()};
		this.travelViewTree(windows, (Activity)this.mActivity.get());
		this.end();
	} catch (Throwable var2) {
		var2.printStackTrace();
	}

## 3.3 travelViewTree(View[] views, Activity activity)

		void travelViewTree(@NonNull View[] rootView, @NonNull Activity activity) {
            View[] var3 = rootView;
            int var4 = rootView.length;

            label41:
            for(int var5 = 0; var5 < var4; ++var5) {
                View view = var3[var5];
                if(view != null) {
                    Stack<View> stack = new Stack();
                    stack.push(view);

                    while(true) {
                        while(true) {
                            if(stack.isEmpty()) {
                                continue label41;
                            }

                            View current = (View)stack.pop();
							// 非viewGroup
                            if(!(current instanceof ViewGroup)) {
                                this.handView(current, activity);
							// 对ViewGroup进行筛选,返回false 就不继续遍历处理了
                            } else if(!this.handleViewGroup(current, activity, stack)) {
                                ViewGroup viewGroup = (ViewGroup)current;

                                for(int index = 0; index < viewGroup.getChildCount(); ++index) {
                                    stack.push(viewGroup.getChildAt(index));
                                }
                            }
                        }
                    }
                }
            }

        }

## 3.4 handView()

## 3.5 handleViewGroup(View current, Activity activity, Stack<View> stack)

        boolean handleViewGroup(View current, Activity activity, Stack<View> stack) {
			//非空&& 可见
            if(current != null && current.isShown()) {
				// 控件属于ViewPager
				// Viewpager中保存的是Fragment
                if(current instanceof ViewPager && PageHelper.isFragmentViewPager((ViewPager)current)) {
                    return this.fragmentVisitor.handle(activity, current, stack);
				// 仅仅只是ViewPager
                } else if(current instanceof ViewPager) {
                    return this.customeVisitor.handle(activity, current, stack);
				// 判断当前View 是否是 Fragment的rootView
                } else if(AppState.getInstance().isFragmentView(activity, current) && AppState.getInstance().getFragmentByView(activity, current) != null) {
                    return this.fragmentTrackVisitor.handle(activity, AppState.getInstance().getFragmentByView(activity, current), stack);
				// 其他情况
                } else {
					// 设置代理OnFocusChangeListener
                    this.onlickListenerVisitor.handle(activity, current, (Stack)null);
					// 默认返回了false
                    return false;
                }
            } else {
                return true;
            }
        }

- 主要功能就是 , 判断是否是ViewGroup, 如果是特定类型的ViewGroup 例如 ViewPager,Fragment 之类的  就需要取出这些控件的根视图 继续遍历


# 4. ViewVisitor

	public interface ViewVisitor {
	    boolean handle(Activity var1, Object var2, Stack<View> var3);
	
	    boolean end();
	}

## 4.1 ListenerInfoVisitor


    public boolean handle(Activity activity, Object view, Stack<View> stack) {
        View current = (View)view;
        if(current.isClickable() && this.checkEnv(current) && current.getTag(84159250) == null) {
			//获取到当前View中的 mListenerInfo字段
            Object listenerInfo = this.getListenerInfo(current);
			// 如果为空 则创建出一个,并设置给当前控件
            if(listenerInfo == null) {
                listenerInfo = this.getNewListenerInfo(current);
                this.setListenerInfo(current, listenerInfo);
            }
			//创建失败  则直接退出
            if(listenerInfo == null) {
                return false;
            } else {
				//获取mListenerInfo中的 mOnFocusChangeListener ,创建代理 
                this.setOnFocusChangeListener(listenerInfo, new OnFocusChangeListenerProxy(this.getOnFocusChangeListener(listenerInfo)));
                current.setTag(84159250, Boolean.valueOf(true));
                return false;
            }
        } else {
            return false;
        }
    }

- 判断是否可以点击,获取`View`的`mListenerInfo`字段

- `checkEnv()`, 获取`s$ListenerInfo`类字节码,获取其`mOnFocusChangeListener`,`mOnClickListener`字段


## 4.2 FragmentVisitor

    @TargetApi(11)
    public boolean handle(Activity activity, Object viewPager, Stack<View> stack) {
        LogUtil.i("GIO.FragmentVisitor", "handle FragmentVisitor " + activity.toString());
        ViewPager current = (ViewPager)viewPager;
        Object target = PageHelper.getViewPagerCurrentItem(current);
        View temp = null;
        if(ClassExistHelper.instanceOfSupportFragment(target)) {
            temp = ((Fragment)target).getView();
        }

        if(target instanceof android.app.Fragment) {
            temp = ((android.app.Fragment)target).getView();
        }

        if(temp != null) {
            this.list.add(target);
            stack.push(temp);
        }

        return true;
    }

- 获取fragment的rootview,添加到stack中...继续遍历


# 5. WindowHelper

## 5.1 getWindowViews()

    public static View[] getWindowViews() {
        View[] result = new View[0];
        if(sWindowManger == null) {
            Activity current = AppState.getInstance().getForegroundActivity();
            return current != null?new View[]{current.getWindow().getDecorView()}:result;
        } else {
            try {
                View[] views = null;
                if(sArrayListWindowViews) {
                    views = (View[])((ArrayList)viewsField.get(sWindowManger)).toArray(result);
                } else if(sViewArrayWindowViews) {
                    views = (View[])((View[])viewsField.get(sWindowManger));
                }

                if(views != null) {
                    result = views;
                }
            } catch (Exception var2) {
                LogUtil.d(var2);
            }
			//过滤掉空的View
            return stripNullView(result);
        }
    }

- 获取`WindowManager`中的 `mViews`字段

# 6. ActionCalculator 

## 6.1 obtainImpress()

    public List<ActionEvent> obtainImpress() {
        GConfig config = GConfig.getInstance();
        List<ActionEvent> events = null;
        if(config != null && config.shouldSendImp()) {
            this.mNewImpressViews = new ArrayList();
            if(this.mRootView != null && this.mRootView.get() != null) {
                ViewHelper.traverseWindow((View)this.mRootView.get(), this.mWindowPrefix, this.mViewTraveler);
            }

            events = new ArrayList(2);
            ActionEvent impEvents = null;
            if(this.mNewImpressViews.size() > 0) {
                impEvents = ActionEvent.makeImpEvent();
                impEvents.elems = this.mNewImpressViews;
                impEvents.setPageTime(this.mPtm);
                impEvents.mPageName = this.mPage;
                events.add(impEvents);
            }

            if(this.mTodoViewNode.size() > 0) {
                if(impEvents == null) {
                    impEvents = ActionEvent.makeImpEvent();
                    impEvents.setPageTime(this.mPtm);
                    impEvents.mPageName = this.mPage;
                }

                ImplEventAsyncExecutor.getInstance().execute(impEvents, this.mTodoViewNode);
                this.mTodoViewNode = new ArrayList();
                return null;
            }
        }

        return events;
    }


# 7. ViewHelper

## 7.1 traverseWindow(View rootView, String windowPrefix, ViewTraveler callBack)

    public static void traverseWindow(View rootView, String windowPrefix, ViewTraveler callBack) {
        if(rootView != null) {
            int[] offset = new int[2];
            rootView.getLocationOnScreen(offset);
            boolean fullscreen = offset[0] == 0 && offset[1] == 0;
            ViewNode rootNode = new ViewNode(rootView, 0, -1, Util.isListView(rootView), fullscreen, false, false, windowPrefix, windowPrefix, windowPrefix, callBack);
            Object inheritableObject = rootView.getTag(84159243);
            if(inheritableObject != null && inheritableObject instanceof String) {
                rootNode.mInheritableGrowingInfo = (String)inheritableObject;
            }

            if(rootNode.isNeedTrack()) {
                if(!WindowHelper.isDecorView(rootView)) {
                    rootNode.traverseViewsRecur();
                } else {
                    rootNode.traverseChildren();
                }
            }

        }
    }