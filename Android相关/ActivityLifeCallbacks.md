# ActivityLifeCallbacks.md
[官方文档](https://developer.android.com/reference/android/app/Application.ActivityLifecycleCallbacks)

# 1. 简介

Android从 API-14开始引入`Application. registerActivityLifecycleCallbacks` 方法，用来监听所有Activity的生命周期回调。

# 2. 使用


# 3. 调用时机

在`ActivityThread`的`performLaunchActivity()`方法中会去创建`Activity`,并回调Activity的相关生命周期回调


## 3.1 ActivityThread.performLaunchActivity()

    private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        Activity activity = null;
        try {
            java.lang.ClassLoader cl = appContext.getClassLoader();
            activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);
			..............
        }catch(....)

        try {
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);

            if (activity != null) {

               if (r.isPersistable()) {
				...............
                    mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
                } else {
                    mInstrumentation.callActivityOnCreate(activity, r.state);
                }
			}
		}catch(....)

	}

## 3.2 Instrumentation.callActivityOnCreate()

    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        activity.performCreate(icicle);
    }


## 3.3 Activity.performCreate()

    final void performCreate(Bundle icicle, PersistableBundle persistentState) {
        mCanEnterPictureInPicture = true;
        restoreHasCurrentPermissionRequest(icicle);
        if (persistentState != null) {
            onCreate(icicle, persistentState);
        } else {
            onCreate(icicle);
        }
		...........
        mFragments.dispatchActivityCreated();
		..........
    }

## 3.4 Activity.onCreate()

    protected void onCreate(@Nullable Bundle savedInstanceState) {
		..................
		// 重点...
        getApplication().dispatchActivityCreated(this, savedInstanceState);

    }

- 从这里可以知道,在继承`Activity`并重写`onCreate()`方法之后,`ActivityLifeCallbacks`会早于`super.onCreate()`之后的代码被执行


## 3.5 `Application.disPatchActivityCreated()`

	void dispatchActivityCreated(Activity activity, Bundle savedInstanceState) {
		Object[] callbacks = collectActivityLifecycleCallbacks();
		
		if (callbacks != null) {
			for (int i=0; i<callbacks.length; i++) {
				((ActivityLifecycleCallbacks)callbacks[i]).onActivityCreated(activity,savedInstanceState);
			}
		}
	}