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


## 3.1  PMS对象获取流程分析

**Android 提供了PackageManager 来管理PMS的Binder对象,所以调用PackageManager即调用到了PMS(具体实现是交给了PMS)**

### 3.1.1 Activity中如何获取PackageManager

	PackageManager packageManager = getPackageManager();
	//ContextWrapper
    @Override
    public PackageManager getPackageManager() {
        return mBase.getPackageManager();
    }

- Activity并没有重写`getPackageManager()`,所以默认的实现是在`ContextWrapper`中

- `ContextWrapper`中的mBase即Activity对应的`ContextImpl`

### 3.1.2 ContextImpl如何实现getPackageManager

	//ContextImpl
	@Override
	public PackageManager getPackageManager() {
		//缓存
		if (mPackageManager != null) {
			return mPackageManager;
		}
		//获取Binder代理对象
		IPackageManager pm = ActivityThread.getPackageManager();
		if (pm != null) {
			// Doesn't matter if we make more than one instance.
			//返回一个管理类
			return (mPackageManager = new ApplicationPackageManager(this, pm));
		}
	
		return null;
	}

- 每个Activity都会对应一个`ContextImpl`,所以不能将`mPackageManager`作为Hook点(如果针对单个Activity 可以考虑).

- PMS的Binder代理对象会通过`ActivityThread.getPackageManager()`获取

- **获取到了Binder代理对象,会传给`ApplicationPackageManager`,然后返回给调用者使用.**

- **注意:这里有一个`mPackageManager`变量,是作为缓存使用,如果存在缓存 会优先使用缓存**.`mPakcageManager`实际上是一个`ApplicationPackageManager`类,是`IPackageManager`的管理类.

### 3.1.3 ActivityThread.getPackageManager()

    public static IPackageManager getPackageManager() {
        if (sPackageManager != null) {
            //Slog.v("PackageManager", "returning cur default = " + sPackageManager);
            return sPackageManager;
        }
		//通过ServiceManager获取原始的IBinder
        IBinder b = ServiceManager.getService("package");
		//进行类型转换,最终得到IPacakgeManager.Stub.Proxy,即Binder代理对象
        sPackageManager = IPackageManager.Stub.asInterface(b);
        return sPackageManager;
    }

- 这里的sPackageManager 是一个静态变量,充当`IPackageManager.Stub.Proxy`的缓存.

### 3.1.4 Activity使用PakcageManager

	int result = packageManager.checkPermission(Manifest.permission.ACCESS_WIFI_STATE,
                getPackageName());

- 已知`packageManager`是`ApplicationPakcageManager`类型.

	    @Override
	    public int checkPermission(String permName, String pkgName) {
	        try {
	            return mPM.checkPermission(permName, pkgName, mContext.getUserId());
	        } catch (RemoteException e) {
	            throw e.rethrowFromSystemServer();
	        }
	    }

- 发现具体的操作 交给了`mPM`变量,这个变量是`IPackageManager`类型.具体的赋值 是在构造函数处.实际上这个`mPM`就是PMS的Binder代理对象

## 3.2 Hook分析

从上述流程可以知道,`ActivityThread.sPackageManager`其本身是一个 静态变量 符合Hook原则,是一个很适合的Hook点

但是在`ContextImpl`中有一个`mPackageManager`成员变量作为缓存,如果只是将 `ActivityThread.sPakcageManager`替换掉 其实并不会起作用.**也是因为这个缓存的存在,使得Hook一个点 就解决所有PMS的方案落空,必须得Hook掉所有`ContextImpl.mPakcageManager`**

**因此,只能考虑修改这个`ContextImpl.mPackageManager`,那么具体如何修改这个成员变量?:**

1. 替换掉`ActivityThread.sPackageManager`,然后将`ContextImpl.mPackageManager`置空.这样`ContextImpl.getPackageManager()`就会再次调用`ActivitThread.getPackageManager()`去获取Binder代理对象,然后组装一个ApplicationPackageManager并返回.

	而`ActivityThread.getPackageManager()`方法会优先去取`sPackageManager`变量中的值(即被动态代理的对象)

2. 获取到`ActivityThread.sPackageManager`,并创建动态代理.然后将动态代理对象 设置到 `ContextImpl.mPackageManager`的`mPM`字段中

- **注意,无论是方案1 或是方案2,俩者都仅针对当前Activity 或Service起作用,不同的Activity或Service需要重新调用Hook方法(因为Activity和Service 与 ContextImpl一一对应..)**


## 3.3 方案2

### 3.3.1 获取ActivityThread.sPackageManager

	//ActivityThread字节码
	Class activityThread_class = Class.forName("android.app.ActivityThread");
	//sPackageManager字段
	Field sPackageManager_field = activityThread_class.getDeclaredField("sPackageManager");
	//绕过检查
	sPackageManager_field.setAccessible(true);
	//获取到Binder代理对象!
	Object sPackageManager = sPackageManager_field.get(null);

### 3.3.2 创建动态代理

	//动态代理
	Object newPackageManager = Proxy.newProxyInstance(sPackageManager.getClass().getClassLoader(),
                    new Class[]{IBinder.class, IInterface.class,
                            Class.forName("android.content.pm.IPackageManager")},
                    new PMSProxy(sPackageManager));
	
	//InvocationHandler,具体修改逻辑的地方
	public class PMSProxy implements InvocationHandler {
	
	    private Object mBase;
	
	    public PMSProxy(Object base) {
	        mBase = base;
	    }
	
	    @Override
	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	        Log.e("PMSProxy", "methdo name = " + method.getName());
	
	
	        return method.invoke(mBase, args);
	    }
	}

### 3.3.3 替换ContextImpl.mPakcageManager

这里有俩个方法获取到mPakcageManager,用反射获取当前Activity的mBase,然后用mBase再去获取这个mPackageManager......**或者直接getPackageManager()即可...**

	PackageManager packageManager = getPackageManager();
	Field mPM_field = packageManager.getClass().getDeclaredField("mPM");
	mPM_field.setAccessible(true);
	mPM_field.set(packageManager, newPackageManager);

## 3.4 总结

由于`ContextImpl`并没有使用 静态变量保存 `PacakgeManager`对象,导致必须对每一个`ContextImpl`进行Hook(即每一个ContextImpl实例就拥有一个PMS代理对象的引用).

**因此,Hook对象 最好是静态的或者是单例的.实例变量作为Hook对象是非常麻烦的!!! **