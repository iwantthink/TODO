# Hook-AMS&PMS

[Android 插件化原理解析——Hook机制之AMS&PMS](http://weishu.me/2016/03/07/understand-plugin-framework-ams-pms-hook/)

# 1. 简介

AMS = ActivityManagerService

PMS = PackageManagerService

**提醒:静态变量和单例 都是比较好的Hook点**

**本文基于android 27 **


# 2. Hook AMS

**一些内容,例如`Context.startActivity()`和`Activity.startActivity()`在 `[基于动态代理的Hook机制.md]`已经被提到,可以去查看**

四大组件都有和AMS进行交互,是Framework层重要的一个类:

1. Activity的启动

2. 服务的绑定和开启

3. 动态广播的注册和接收(静态广播在PMS中完成)

4. `getContentResolver`是通过AMS进行获取

## 2.1 AMS对象获取流程分析

**通过分析`startActivity()`的过程了解AMS获取的过程,已知`startActivity()`有俩种形式:**

1. `Context.startActivity()`,这种方式具体实现是通过`ContextImpl`去实现.这种方式启动的Activity并没有任务栈,所以需要手动添加`FLAG:FLAG_ACTIVITY_NEW_TASK`

	    @Override
	    public void startActivity(Intent intent, Bundle options) {
	        warnIfCallingFromSystemProcess();
	
	        // Calling start activity from outside an activity without FLAG_ACTIVITY_NEW_TASK is
	        // generally not allowed, except if the caller specifies the task id the activity should
	        // be launched in.
	        if ((intent.getFlags()&Intent.FLAG_ACTIVITY_NEW_TASK) == 0
	                && options != null && ActivityOptions.fromBundle(options).getLaunchTaskId() == -1) {
	            throw new AndroidRuntimeException(
	                    "Calling startActivity() from outside of an Activity "
	                    + " context requires the FLAG_ACTIVITY_NEW_TASK flag."
	                    + " Is this really what you want?");
	        }
	        mMainThread.getInstrumentation().execStartActivity(
	                getOuterContext(), mMainThread.getApplicationThread(), null,
	                (Activity) null, intent, -1, options);
	    }

2. `Activity.startActivity()`,这是方式就是通过`Activity`实现的

		public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
	            @Nullable Bundle options) {
			``省略代码```
	        Instrumentation.ActivityResult ar =
	                mInstrumentation.execStartActivity(
	                    this, mMainThread.getApplicationThread(), mToken, this,
	                    intent, requestCode, options);
			```省略代码```
	    }

**以上俩种方式启动Activity,最终都是交给Instrumentation来实现**

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
		``省略代码``
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            int result = ActivityManager.getService()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target != null ? target.mEmbeddedID : null,
                        requestCode, 0, null, options);
            checkStartActivityResult(result, intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
        return null;
    }

- `ActivityManager.getService()`方法 是通过一个`SingleTon`类去实现了懒加载单例.

	    public static IActivityManager getService() {
	        return IActivityManagerSingleton.get();
	    }
	
	    private static final Singleton<IActivityManager> IActivityManagerSingleton =
	            new Singleton<IActivityManager>() {
	                @Override
	                protected IActivityManager create() {
	                    final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
	                    final IActivityManager am = IActivityManager.Stub.asInterface(b);
	                    return am;
	                }
	            };

		public abstract class Singleton<T> {
		    private T mInstance;
		
		    protected abstract T create();
		
		    public final T get() {
		        synchronized (this) {
		            if (mInstance == null) {
		                mInstance = create();
		            }
		            return mInstance;
		        }
		    }
		}

## 2.2 Hook点分析
AMS的Hook点有俩个:

1. **根据`[HookBinder.md]`知道,能够通过Hook掉IBinder的`queryLocalInterface()`方法 来使其返回 修改后的 Binder代理对象,达到Hook效果.** 

2. 根据ASM的获取流程得知,**Android 将AMS对象通过`Singleton`类做了缓存,便于使用.根据寻找单例或静态变量的Hook原则,这里也是进行Hook的一个非常适合的地方.**

## 2.3 版本差异
**注意:之前的分析都是基于Android27,但是实际上Android26开始使用`ActivityManager.getService()`去获取远程Binder代理对象.Android26 以下是通过`ActivityManagerNative.getDefault()`**

**Android 25 获取AMS逻辑:**

	//Instrumentation
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
		``省略代码```
		int result = ActivityManagerNative.getDefault()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target != null ? target.mEmbeddedID : null,
                        requestCode, 0, null, options);
        return null;
    }
    //ActivityManagerNative
    static public IActivityManager getDefault() {
        return gDefault.get();
    }

    private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
        protected IActivityManager create() {
            IBinder b = ServiceManager.getService("activity");
            IActivityManager am = asInterface(b);
            return am;
        }
    };

## 2.3 具体Hook过程

### 2.3.1 android 26之前

    private void hookAMSBEFORE26() {

        try {
            Class acitivtyManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            Field gDefaultField = acitivtyManagerNativeClass.
                    getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            Object gDefault = gDefaultField.get(null);

            Class singleTonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singleTonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);

            //获取到了IActivityManager.即Binder.Stub.Proxy
            Object mInstance = mInstanceField.get(gDefault);

            Object newInstance = Proxy.newProxyInstance(mInstance.getClass().getClassLoader(),
                    new Class[]{IBinder.class,
                            IInterface.class,
                            Class.forName("android.app.IActivityManager")},
                    new ActivityProxy(mInstance));

            mInstanceField.set(gDefault, newInstance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

### 2.3.2 android 25之后


    private void hookAMSAfter25() {
        try {
            Class activityManagerClass = ActivityManager.class;
            Field IActivityManagerSingletonField = activityManagerClass.
                    getDeclaredField("IActivityManagerSingleton");
            IActivityManagerSingletonField.setAccessible(true);
            Object IActivityManagerSingleton = IActivityManagerSingletonField.get(null);

            Class singleTonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singleTonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            //获取到了IActivityManager.即Binder.Stub.Proxy
            Object mInstance = mInstanceField.get(IActivityManagerSingleton);

            Object newInstance = Proxy.newProxyInstance(mInstance.getClass().getClassLoader(),
                    new Class[]{IBinder.class,
                            IInterface.class,
                            Class.forName("android.app.IActivityManager")},
                    new ActivityProxy(mInstance));

            mInstanceField.set(IActivityManagerSingleton, newInstance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

# 3. Hook PMS

PMS具备一些如下的功能,列举几个:

1. 权限校验(checkPermission等)

2. APK meta信息获取(getApplicationInfo等)

3. 四大组件信息获取

