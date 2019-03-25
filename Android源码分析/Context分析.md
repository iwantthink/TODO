# Context分析

[完全剖析Android上下文环境Context](https://xujiaojie.github.io/2017/09/18/%E5%AE%8C%E5%85%A8%E5%89%96%E6%9E%90Android%E4%B8%8A%E4%B8%8B%E6%96%87%E7%8E%AF%E5%A2%83Context/)

[Android 官方文档](https://developer.android.com/reference/android/content/Context)
# 1. 简介

1. 应用程序环境信息的接口，表示上下文的意思。

2. `Context`是一个由Android系统提供实现类的抽象类(这个实现类即`ContextImpl`,这里用到了一个代理模式，通过`ContextWrapper`代理了`ContextImpl`)

3. 通过`Context`类可以允许访问特定应用程序的资源和类，以及对应用级操作的调用（比如启动活动，广播和接受意图等等）。

## 1.1 Context继承关系

![Context继承关系](http://ww1.sinaimg.cn/large/6ab93b35ly1frao9xdnfoj20bj08jaac.jpg)

![](http://otpesi023.bkt.clouddn.com/Context1.jpg)

**`Context`的实际实现类是`ContextImpl`，是一个注解了的内部保护文件。是Context api的通用实现，提供了基本的Context给Activity和Application组件使用**

- **`ContextWrapper`类中存在一个变量`mBase`是一个`ContextImpl`对象，指向真正的实现类`ContextImpl`.而`ContextImpl`类中的变量`mOuterContext`指向相对应的`Activity,Service,Application`**

	可以查看`ContextWrapper`的源码：

	    /**
	     * Set the base context for this ContextWrapper.  All calls will then be
	     * delegated to the base context.  Throws
	     * IllegalStateException if a base context has already been set.
	     * 
	     * @param base The new base context for this wrapper.
	     */
	    protected void attachBaseContext(Context base) {
	        if (mBase != null) {
	            throw new IllegalStateException("Base context already set");
	        }
	        mBase = base;
	    }

	Activity被创建的时候，会被调用其`attach()`方法，在`attach()`方法中 就会调用`attachBaseContext()`方法 来将创建出来的`ContextImpl`传给`ContextWrapper`




## 1.2 应用程序中Context的数量

总Context数量 = Activity个数+Service个数+Application


# 2. Context 创建过程

## 2.1 Application Context创建过程

**ApplicationContext是在应用程序被启动的过程中被创建，应用程序被启动的过程是由AMS进行管理**

### 2.1.1 AMS.attachApplicationLocked()

Android 系统Framework框架里的`ActivityManagerService` 会调度Activity的创建，在这个流程中，其中一步是调用`ActivityTherad.handleBindApplication()`完成与`ActivityThread` 的绑定

### 2.1.2 Activity.handleBindApplication()

`ActivityThread.scheduleLaunchActivity()`方法会通过Handler 将线程切换到主线程，最终会在`ActivityThread.handleBindApplication()`方法中执行.

	Application app = data.loadedApk.makeApplication(data.restrictedBackupMode, null);

### 2.1.3 LoadedApk.makeApplication()

	    public Application makeApplication(boolean forceDefaultAppClass,
	            Instrumentation instrumentation) {
	        if (mApplication != null) {
	            return mApplication;
	        }
			``省略代码```
	        Application app = null;
	
			``省略代码```
	        try {
	            java.lang.ClassLoader cl = getClassLoader();
			``省略代码```
				//创建出Application对应的ContextImpl
	            ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);
	            app = mActivityThread.mInstrumentation.newApplication(
	                    cl, appClass, appContext);
				//设置ContextImpl的mOuterContext变量，代表该ContextImpl是哪一个对象的具体实现类
	            appContext.setOuterContext(app);
	        } catch (Exception e) {
	            ``省略代码```
	        }
	        mActivityThread.mAllApplications.add(app);
	        mApplication = app;
	
	        if (instrumentation != null) {
	            try {
	                instrumentation.callApplicationOnCreate(app);
	            } catch (Exception e) {
	                ``省略代码```
	            }
	        }
			``省略代码```
	
	        return app;
	    }

- 变量`mApplication`是`LoadedApk`类中的成员变量，是Application类型。如果已经加载会直接返回

- **会通过调用`ContextImpl.setOuterContext(app)`将Application绑定到`ContextImpl`**

- **具体的Application的创建是委托给Instrumentation创建的**

### 2.1.4 Instrumentation.newApplication()

    static public Application newApplication(Class<?> clazz, Context context)
            throws InstantiationException, IllegalAccessException, 
            ClassNotFoundException {
        Application app = (Application)clazz.newInstance();
        app.attach(context);
        return app;
    }

- 通过反射加载Application

- **调用Application的`attach()`,会将传入的`ContextImpl`绑定到`ContextWrapper`**（即ContextWrapper的mBase变量,mBase代表具体的Context实现类）

	    /* package */ final void attach(Context context) {
	        attachBaseContext(context);
	        mLoadedApk = ContextImpl.getImpl(context).mPackageInfo;
	    }


## 2.2 Activity Context的创建过程

### 2.2.1 ActivityThread.scheduleLaunchActivity()

在应用创建的过程中，AMS在做完任务栈进程创建等一些工作后，会将具体的Activity启动交给ActivityThread去操作，依次调用如下操作

1. ActivityThread.scheduleLaunchActivity()

2. ActivityThread.handleLaunchActivity()

3. ActivityThread.performLaunchActivity()

### 2.2.2 ActivityThread.performLaunchActivity()

    private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {

		```省略代码```
        ContextImpl appContext = createBaseContextForActivity(r);
        Activity activity = null;
        try {
            java.lang.ClassLoader cl = appContext.getClassLoader();
            activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);
            ```省略代码```
        } catch (Exception e) {
            ```省略代码```
        }

        try {
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);

            ```省略代码```

            if (activity != null) {
                ```省略代码```
                Window window = null;
                if (r.mPendingRemoveWindow != null && r.mPreserveWindow) {
                    window = r.mPendingRemoveWindow;
                    r.mPendingRemoveWindow = null;
                    r.mPendingRemoveWindowManager = null;
                }
				//将Activity绑定到ComtextImpl
                appContext.setOuterContext(activity);
                activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window, r.configCallback);

               ```省略代码```

            mActivities.put(r.token, r);

        } catch (SuperNotCalledException e) {
            throw e;

        } catch (Exception e) {
			```省略代码```
        }

        return activity;
    }

- 调用`createBaseContextForActivity()`方法创建了Activity的具体Context实现类


### 2.2.3 ActivityThread.createBaseContextForActivity()

    private ContextImpl createBaseContextForActivity(ActivityClientRecord r) {
        final int displayId;
        try {
            displayId = ActivityManager.getService().getActivityDisplayId(r.token);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }

        ContextImpl appContext = ContextImpl.createActivityContext(
                this, r.packageInfo, r.activityInfo, r.token, displayId, r.overrideConfig);

        ```省略代码```
        return appContext;
    }


### 2.2.4 Activity.attach()

	 final void attach(Context context, ActivityThread aThread,
	            Instrumentation instr, IBinder token, int ident,
	            Application application, Intent intent, ActivityInfo info,
	            CharSequence title, Activity parent, String id,
	            NonConfigurationInstances lastNonConfigurationInstances,
	            Configuration config, String referrer, IVoiceInteractor voiceInteractor,
	            Window window, ActivityConfigCallback activityConfigCallback) {
	        attachBaseContext(context);
		```省略大量代码``
	}

- **将`Activity`的`ContextImpl` 绑定到`Activity-ContextWrapper`**