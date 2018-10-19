# WindwoManager整体分析

[Android解析WindowManager（一）WindowManager体系](http://liuwangshu.cn/framework/wm/1-windowmanager.html)

[Android开发艺术探索]()

# 1. 简介

# 2. Window,WindowManager和WMS

![](http://upload-images.jianshu.io/upload_images/1417629-4e2047a49e2572f3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 2.1 Window

`Window`表示一个窗口的概念,它是一个抽象类,具体实现是`PhoneWindow`

Android中所有的视图都是通过`Window`来呈现的,不管是`Activity`,`Dialog`,`Toast`,它们的视图都是附加在`Window`上,**因此`Window`是`View`的直接管理者**

`Window`并不是真实存在的,实体是`View`

## 2.2 WindowManager

`WindowManager`是外接访问`Window`的入口,作为一个接口类,继承自接口`ViewManager`,是用来管理`Window`的(可以通过WM对`Window`进行添加和删除的操作).

它的具体实现类是`WindowManagerImpl`

## 2.3 WindowManagerService

WMS与WM通过Binder进行IPC,WMS作为系统级别的服务,并不会将全部API暴露给WM,这一点与`ActivityManager`和`ActivityManagerService`相似

主要功能包括`Window`管理和输入系统

# 3. WindowManager介绍

`WindowManager`是一个接口类，继承自接口`ViewManager`.WM在继承VM的同时,又加入了很多功能,包括Window类型,层级相关的常量,内部类以及一些方法

其中存在俩个方法是根据`Window`特性加入

	public Display getDefaultDisplay();
	public void removeViewImmediate(View view);

- `getDefaultDisplay()`方法会得知这个`WindowManager`实例将`Window`添加到哪个屏幕上了，换句话说，就是得到`WindowManager`所管理的屏幕（`Display`）。

- `removeViewImmediate()`方法则规定在这个方法返回前要立即执行`View.onDetachedFromWindow()`，来完成传入的View相关的销毁工作


## 3.1 ViewManager

	public interface ViewManager
	{
	    /**
	     * Assign the passed LayoutParams to the passed View and add the view to the window.
	     * 
	     * @param view The view to be added to this window.
	     * @param params The LayoutParams to assign to view.
	     */
	    public void addView(View view, ViewGroup.LayoutParams params);
	    public void updateViewLayout(View view, ViewGroup.LayoutParams params);
	    public void removeView(View view);
	}

- `ViewManager`定义了三个方法,分别用来添加,更新和删除view

	另外,**这三个方法都是直接对`View`进行操作,说明WM具体管理的是以`View`形式存在的`Window`**


# 4. Window创建过程

`Window`是一个抽象类,具体实现类为`PhoneWindow`.`ActivityThread`的`performLaunchActivity()`方法在创建`Activity`时,会调用`Activity`的`attach()`,`Window`就在这时候被创建


## 4.1 Activity.attach()

	final void attach(Context context, ActivityThread aThread,
	            Instrumentation instr, IBinder token, int ident,
	            Application application, Intent intent, ActivityInfo info,
	            CharSequence title, Activity parent, String id,
	            NonConfigurationInstances lastNonConfigurationInstances,
	            Configuration config, String referrer, IVoiceInteractor voiceInteractor,
	            Window window) {
	        attachBaseContext(context);
	        mFragments.attachHost(null /*parent*/);
			//创建了Window
	        mWindow = new PhoneWindow(this, window);
	        ...
	        //具体的实现在PhoneWindow的父类,Window中
	        mWindow.setWindowManager(
	                (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
	                mToken, mComponent.flattenToString(),
	                (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);
	      ...

- `Context.WINDOW_SERVICE` = `"window"`;


## 4.2 Window.setWindowManager()

	public void setWindowManager(WindowManager wm, IBinder appToken, String appName,
	        boolean hardwareAccelerated) {
	    mAppToken = appToken;
	    mAppName = appName;
	    mHardwareAccelerated = hardwareAccelerated
	            || SystemProperties.getBoolean(PROPERTY_HARDWARE_UI, false);
	    if (wm == null) {
	        wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
	    }
	    mWindowManager = ((WindowManagerImpl)wm).createLocalWindowManager(this);
	}

- 如果传入的`WindowManager`为空,则会通过`ContextImpl`去获取.实际上在`attach()`方法中,也是同样的获取.**实际上获取到的WM是`WindowManagerImpl`**,这一点通过继续阅读代码(4.2.1-4.2.4) 可以得出

### 4.2.1 ContextImpl.getSystemService()
	
	@Override
	public Object getSystemService(String name) {
	    return SystemServiceRegistry.getSystemService(this, name);
	}

### 4.2.2 SystemServiceRegistry.getSystemService()

    public static Object getSystemService(ContextImpl ctx, String name) {
        ServiceFetcher<?> fetcher = SYSTEM_SERVICE_FETCHERS.get(name);
        return fetcher != null ? fetcher.getService(ctx) : null;
    }

- `SystemServiceRegistry`会在其静态代码块中调用`registerService()`方法,将一些Manager的关系添加到`SYSTEM_SERVICE_NAMES`和`SYSTEM_SERVICE_FETCHERS`

- `SYSTEM_SERVICE_FETCHERS`

	`SYSTEM_SERVICE_FETCHERS`是一个HashMap类型的数据，key-value关系是 `serviceName-serviceFetcher`

- **已知此处的`ServiceFetcher`是`CachedServiceFetcher`类型**

### 4.2.3 SystemServiceRegistry 静态代码块

	final class SystemServiceRegistry {
	...
	 private SystemServiceRegistry() { }
	 static {
	 ...
	   registerService(Context.WINDOW_SERVICE, WindowManager.class,
	                new CachedServiceFetcher<WindowManager>() {
	            @Override
	            public WindowManager createService(ContextImpl ctx) {
	                return new WindowManagerImpl(ctx);
	            }});
	...
	 }
	}

    private static <T> void registerService(String serviceName, Class<T> serviceClass,
            ServiceFetcher<T> serviceFetcher) {
        SYSTEM_SERVICE_NAMES.put(serviceClass, serviceName);
        SYSTEM_SERVICE_FETCHERS.put(serviceName, serviceFetcher);
    }

- 

### 4.2.4 CachedServiceFetcher.getService(ctx)

    static abstract class CachedServiceFetcher<T> implements ServiceFetcher<T> {
        private final int mCacheIndex;

        public CachedServiceFetcher() {
            mCacheIndex = sServiceCacheSize++;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final T getService(ContextImpl ctx) {
            final Object[] cache = ctx.mServiceCache;
            synchronized (cache) {
                // Fetch or create the service.
                Object service = cache[mCacheIndex];
                if (service == null) {
                    try {
                        service = createService(ctx);
                        cache[mCacheIndex] = service;
                    } catch (ServiceNotFoundException e) {
                        onServiceNotFound(e);
                    }
                }
                return (T)service;
            }
        }

        public abstract T createService(ContextImpl ctx) throws ServiceNotFoundException;
    }

- 看到这里,可以得出一个结论

	**`mContext.getSystemService(Context.WINDOW_SERVICE)`方法获取到的`WindowManager`实际类型就是`WindowManagerImpl`**

## 4.3 WindowManagerImpl.createLocalWindowManager()

	public WindowManagerImpl createLocalWindowManager(Window parentWindow) {
	       return new WindowManagerImpl(mContext, parentWindow);
	   }

- `createLocalWindowManagerImpl()`方法同样是创建了`WindowManagerImpl`,但是这一次同时将`Window`作为参数传入,**这样`WindowManagerImpl`就持有了`Window`,就可以对`Window`进行操作**.例如添加View


# 5. WindowManagerImpl

## 5.1 WindowManagerImpl.addView()

	@Override
	 public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
	     applyDefaultToken(params);
		//此处的mParentWindow就是当前Window
	     mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
	 }

- 可以看到`WindowManagerImpl`虽然是`WindowManager`的实现类,但是其仅仅是将功能委托给了`WindowManagerGlobal`,这里使用了桥接模式

## 5.1 WindowManagerImpl

	public final class WindowManagerImpl implements WindowManager {
	    private final WindowManagerGlobal mGlobal = WindowManagerGlobal.getInstance();
	    private final Context mContext;
	    private final Window mParentWindow;
	...
	  private WindowManagerImpl(Context context, Window parentWindow) {
	        mContext = context;
	        mParentWindow = parentWindow;
	    }
	 ...   
	}

- `WindowManagerGlobal`是一个单例，说明在一个进程中只有一个`WindowManagerGlobal`实例。

- `mParentWindow`作为成员变量,说明`WindowManagerImpl`可能会实现多个`Window`,只要创建多个`WMIPL`，也就是说在一个进程中`WindowManagerImpl`可能会有多个实例。

# 6. 整体关系结构图

![](http://upload-images.jianshu.io/upload_images/1417629-d398194cb0b50bae.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

`PhoneWindow`继承自`Window`，`Window`通过`setWindowManager`方法与`WindowManager`发生关联。`WindowManager`继承自接口`ViewManager`，`WindowManagerImpl`是`WindowManager`接口的实现类，但是具体的功能都会委托给`WindowManagerGlobal`来实现