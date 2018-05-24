# Hook Binder

[Hook机制之Binder Hook](http://weishu.me/2016/02/16/understand-plugin-framework-binder-hook/)

# 1.介绍

Android系统通过Binder机制,将系统服务提供给应用程序使用.例如`ActivityManagerService`,`ClipboardManager`,`AudioManager`等

Hook Binder 本质就是Hook 系统服务的Binder对象,这些系统服务实际上都在`system_server`进程运行

**本文基于android 27源码进行分析**

# 2.系统服务是如何获取的?

通过`Context`对象的`getSystemService()`方法可以获取到对应的系统Service

	ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);


**继承关系：**

- Context->ContextWrapper->ContextThemeWrapper->Activity

**Activity.getSystemService():**

    @Override
    public Object getSystemService(@ServiceName @NonNull String name) {
        if (getBaseContext() == null) {
            throw new IllegalStateException(
                    "System services not available to Activities before onCreate()");
        }

        if (WINDOW_SERVICE.equals(name)) {
            return mWindowManager;
        } else if (SEARCH_SERVICE.equals(name)) {
            ensureSearchManager();
            return mSearchManager;
        }
        return super.getSystemService(name);
    }

- 会判断一下 mBase是否存在,因为最终是会通过mBase去具体操作

- 如果要获取的系统服务是 `WindowManagerService` 或者是`SearchManagerService`,那么优先从缓存中赋予

**ContextThemeWrapper.getSystemService():**

    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mInflater == null) {
                mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
            }
            return mInflater;
        }
        return getBaseContext().getSystemService(name);
    }

- 获取`LayoutInflatreService`

**ContextImpl.getSystemService():**

	   @Override
	    public Object getSystemService(String name) {
	        return SystemServiceRegistry.getSystemService(this, name);
	    }

**SystemServiceRegistry.getSystemService():**

    /**
     * Gets a system service from a given context.
     */
    public static Object getSystemService(ContextImpl ctx, String name) {
        ServiceFetcher<?> fetcher = SYSTEM_SERVICE_FETCHERS.get(name);
        return fetcher != null ? fetcher.getService(ctx) : null;
    }

- **具体的Service获取会从 `SYSTEM_SERVICE_FETCHERS`中获取**


- **`SYSTEM_SERVICE_FETCHERS`是一个 HashMap,key=系统服务名称,value = 对应的ServiceFetcher**

  	  private static final HashMap<String, ServiceFetcher<?>> SYSTEM_SERVICE_FETCHERS =
            new HashMap<String, ServiceFetcher<?>>();



# 3. SystemServiceRegistry如何通过ServiceFetcher获取系统服务?

## 3.1 ServiceFetcher接口

    /**
     * Base interface for classes that fetch services.
     * These objects must only be created during static initialization.
     */
    static abstract interface ServiceFetcher<T> {
        T getService(ContextImpl ctx);
    }

- **`ServiceFetcher`只是一个基础接口,同时必须得在静态代码块中进行初始化**

- `ServiceFetcher`接口有三个抽象实现类,`StaticApplicationContextServiceFetcher`,`CachedServiceFetcher`,`StaticServiceFetcher`.这三个抽象类 都添加了一个抽象方法`createService()`.


### 3.1.1 CachedServiceFetcher

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

- **`createService()`用来创建对应系统服务的管理类(会通过这个管理类去获取真正的代理Binder).**具体创建逻辑由具体的实现类来实现

- `getService()`用来获取对应的service,首先会从`ContextImpl`中获取对应cache,如果不存在 则会调用`createService()`去创建一个,并放到缓存中

- 每次创建一个`CacheServiceFetcher`,`sServiceCacheSize`就会自增,其表示的是缓存的系统服务的数量

### 3.1.2 StaticApplicationContextServiceFetcher

### 3.1.3 StaticServiceFetcher

## 3.2 SystemServiceRegistry-静态代码块

	final class SystemServiceRegistry {
	    static {
			```省略若干注册代码``
	        registerService(Context.ACTIVITY_SERVICE, ActivityManager.class,
	                new CachedServiceFetcher<ActivityManager>() {
	            @Override
	            public ActivityManager createService(ContextImpl ctx) {
	                return new ActivityManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
	            }});
	
	        registerService(Context.CLIPBOARD_SERVICE, ClipboardManager.class,
	                new CachedServiceFetcher<ClipboardManager>() {
	            @Override
	            public ClipboardManager createService(ContextImpl ctx) throws ServiceNotFoundException {
	                return new ClipboardManager(ctx.getOuterContext(),
	                        ctx.mMainThread.getHandler());
	            }});
			
		}
	
	}

- **`createService()`只是创建一个管理类,并不是真正去获取远程代理Binder**

- 通过`registerService()`方法注册系统服务,**实际上只是将 系统服务名称+系统服务的管理类+对应的ServiceFetcher 保存在俩个HashMap,`SYSTEM_SERVICE_NAMES`和S`YSTEM_SERVICE_FETCHERS`**

	    /**
	     * Statically registers a system service with the context.
	     * This method must be called during static initialization only.
	     */
	    private static <T> void registerService(String serviceName, Class<T> serviceClass,
	            ServiceFetcher<T> serviceFetcher) {
	        SYSTEM_SERVICE_NAMES.put(serviceClass, serviceName);
	        SYSTEM_SERVICE_FETCHERS.put(serviceName, serviceFetcher);
	    }

## 3.3 管理类中的Service的获取过程

**在SystemServiceRegistry的静态代码块中,其`ServiceFetcher.createService()`会去创建对应服务的管理类,具体的代理Binder对象都是通过管理类来获取的**,例如ActivityManager,ClipboardManager等...

	@Override
	public ActivityManager createService(ContextImpl ctx) throws ServiceNotFoundException {
		return new ActivityManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
	}});

	@Override
	public ClipboardManager createService(ContextImpl ctx) throws ServiceNotFoundException {
		return new ClipboardManager(ctx.getOuterContext(),
                        ctx.mMainThread.getHandler());
	}});

### 3.3.1 ActivityManager

ActivityManager构造函数如下:

    /*package*/ ActivityManager(Context context, Handler handler) {
        mContext = context;
    }

实际上我们获取Service的方法如下

    /**
     * @hide
     */
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

- 会借助`ServiceManager`来获取真正的系统服务远程代理Binder.

### 3.3.2 ClipboardManager

构造函数:

    /** {@hide} */
    public ClipboardManager(Context context, Handler handler) throws ServiceNotFoundException {
        mContext = context;
        mService = IClipboard.Stub.asInterface(
                ServiceManager.getServiceOrThrow(Context.CLIPBOARD_SERVICE));
    }

- 会借助`ServiceManager`来获取真正的系统服务远程代理Binder.

## 3.4 ServiceManager

**无论是ActivityManager还是ClipboardManager都是通过ServiceManager来获取IBinder对象,此时获取的IBinder对象仅具备和驱动打交道的能力,不能独立工作,需要通过`asInterface()`方法进行类型转换.**


**ServiceManager.getService():**

    /**
     * Returns a reference to a service with the given name.
     * 
     * @param name the name of the service to get
     * @return a reference to the service, or <code>null</code> if the service doesn't exist
     */
    public static IBinder getService(String name) {
        try {
            IBinder service = sCache.get(name);
            if (service != null) {
                return service;
            } else {
                return Binder.allowBlocking(getIServiceManager().getService(name));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "error in getService", e);
        }
        return null;
    }

## 3.5 IBinder.asInterface()

	public static 表示能力的接口 asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof 表示能力的接口))) {
                return ((表示能力的接口) iin);
            }
            return new 表示能力的接口.Stub.Proxy(obj);
	}

- 具体Binder分析 `[Binder分析.md]`

1. 进行非空判断

2. 调用IBinder的queryLocalInterface()判断IBinder是否是本地对象.如果是则直接进行类型强转

3. 如果IBinder不是本地对象,那么转换成远程代理Binder对象


# 4. Hook ClipboardService流程分析

1. 操作剪切板的操作,会先获取一个`ClipboardManager`管理类,然后就可以通过这个管理类与远程系统Service进行通信(管理类将操作转交给Binder):

		ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clipData = clipboardManager.getPrimaryClip();
		ClipData.Item item = clipData.getItemAt(0);
		String clipStr = item.getText().toString();

2. `Activity.getSystemService()`方法,通过前面章节的分析可以知道,会去借助`SystemServiceRegistry`类的`ServiceFetcher`接口来获取对应的系统服务的管理类(准确的来说是其子类,`CachedServiceFetcher`)

3. 通过`ServiceFetcher`能够获取到的系统服务管理类,`ClipboardManager`

	这个类会通过`ServiceManager`类 来获取IBinder对象(**同时这里有一个逻辑,优先从一个HashMap中获取IBinder对象**)

4. 在`ServiceManager`中,由于这时获取到的IBinder对象并不是真正的可调用对象,其仅具有跨进程能力,还不具备方法调用的能力,还需要通过`Binder.Stub.asInterface()`方法进行类型转换.将IBinder转换成 本地Binder对象或者是代理Binder对象.无论是Binder本地对象或者是Binder代理对象 都具备该Binder对应系统服务的能力

		IClipboard mService = IClipboard.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.CLIPBOARD_SERVICE));


**Hook点分析:**

**在应用中调用所有的系统服务都是借助Binder远程代理对象实现的(因为不在一个进程..所以肯定是会转换成binder代理对象),那么只要为这个Binder代理对象创建一个代理对象,然后替换掉这个Binder代理对象,就可以实现Hook**


## 4.1 获取到需要代理的对象

通过反射调用`ServiceManager.getService(String name)`获取

	IBinder b = ServiceManager.getServiceOrThrow(Context.CLIPBOARD_SERVICE)

**反射的话 需要注意,需要先获取到执行invoke执行方法的对象.例外: 静态方法和静态变量 在invoke时不需要传入执行对象.**

**Hook代码:**

	//获取ServiceManager对象,通过其成员变量sServiceManager获取
	Class serviceManager_Class = Class.forName("android.os.ServiceManager");
	Field sServiceManager_Field = serviceManager_Class.getDeclaredField("sServiceManager");
	sServiceManager_Field.setAccessible(true);
	Object sServiceManager = sServiceManager_Field.get(null);

	//获取原始IBinder,通过反射执行ServiceManager.getService(String name)
	Method getService_Method = serviceManager_Class.getDeclaredMethod("getService", String.class);
	getService_Method.setAccessible(true);
	//原始IBinder
	IBinder originalClipIBinder = (IBinder) getService_Method.invoke(sServiceManager, "clipboard");

## 4.2 创建动态代理
注意 4.1 获取到的IBinder对象只是远程Binder代理对象,还需要通过`IClipboard.Stub.asInterface()`方法转换成`IClipboard.Stub.Proxy`之后才能真正被使用.**所以我们调用系统服务的方法 是基于这个转换之后的IBinder对象**
	
	//创建动态代理
	IBinder mBase = Proxy.newProxyInstance(mBase.getClass().getClassLoader(),
	                    new Class[]{IBinder.class,
	                            IInterface.class,
	                            Class.forName("android.content.IClipboard")},
	                    new ProxyClipBinderProxy(mBase));
	//动态代理的 InvocationHandler,即具体修改逻辑的地方
	public class ProxyClipBinderProxy implements InvocationHandler {
	
	    private Object mBase;
		//传入的是 Binder代理对象
	    public ProxyClipBinderProxy(Object base) {
	        mBase = base;
	    }
	
	
	    @Override
	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	        Log.d("ProxyClipBinderProxy", "method name = " + method.getName());
	        if (method.getName().equals("getPrimaryClip")) {
	            ClipData data = (ClipData) method.invoke(mBase, args);
	            ClipData.Item item = data.getItemAt(0);
	            return new ClipData(data.getDescription().getLabel(),
	                    new String[]{}, new ClipData.Item("you are hooked:" + item.getText().toString()));
	        }
	        return method.invoke(mBase, args);
	    }
	}

## 4.3 替换原始对象

已经知道 `ServiceManager.getSystemService()`会去缓存中获取,那么只需要将代理对象 放到缓存中.**但是这里还有一个问题,ClipboardManager构造函数中,在获取到这个原始的IBinder对象之后,还需要进行`asInterface()`的转换,`asInterface()`方法判断的时候,并不会起作用(因为IBinder传入之后,创建了`Stub.Proxy`,具体执行方法的类并不是这个`Stub.Proxy`而是远程的系统服务). **

	public static 表示能力的接口 asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof 表示能力的接口))) {
                return ((表示能力的接口) iin);
            }
            return new 表示能力的接口.Stub.Proxy(obj);
	}

- `queryLocalInterface()`可以作为Hook点,用来将修改后的`IClipboard.Stub.Proxy`返回..

### 4.3.1 Hook原始对象-queryLocalInterface

即使我们已经将动态代理对象 替换到 ServiceManager的缓存中,在通过`asInterface()`方法判断的时候,并不会起作用(因为IBinder传入之后,创建了`Stub.Proxy`,具体执行方法的类并不是这个`Stub.Proxy`而是远程的系统服务).

所以需要 hook掉这个`queryLocalInterface()`这个方法,让其返回代理对象.那么每次从缓存中取出我们的代理的Binder代理对象,并在`asInterface()`中执行`queryLocalInterface()`方法 都会返回我们代理对象.

	//Hook queryLocalInterface,令其返回 被动态代理的Binder代理对象
	IBinder clipProxy = (IBinder) Proxy.newProxyInstance(originalClipIBinder.getClass().getClassLoader(),
                    new Class[]{IBinder.class}, new OriginalClipBinderProxy(originalClipIBinder));

	//具体替换queryLocalInterface的地方
	public class OriginalClipBinderProxy implements InvocationHandler {
	
	    /**
	     * 转换之后的对象
	     */
	    private Object mBase;
	    private Object mOriginalBase;
	
	    /**
	     * @param base 转换之前的对象
	     */
	    public OriginalClipBinderProxy(IBinder base) {
	        mOriginalBase = base;
	        try {
	            //实际上调用的得是Stub.Proxy类型,所以得通过asInterface进行类型转换
	            //获取转换之后的IBinder
	            Class clipboardStub_Class = Class.forName("android.content.IClipboard$Stub");
	            Method asInterface_Method = clipboardStub_Class.getDeclaredMethod("asInterface", IBinder.class);
	            asInterface_Method.setAccessible(true);
	            mBase = asInterface_Method.invoke(null, base);
	
	
	            //hook Stub.Proxy的方法
	            //注意这里有三个接口
	            mBase = Proxy.newProxyInstance(mBase.getClass().getClassLoader(),
	                    new Class[]{IBinder.class,
	                            IInterface.class,
	                            Class.forName("android.content.IClipboard")},
	                    new ProxyClipBinderProxy(mBase));
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	
	    }
	
	
	    @Override
	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			//最关键的一步,Hook掉queryLocalInterface
	        if (method.getName().equals("queryLocalInterface")) {
	            return mBase;
	        }
	        return method.invoke(mOriginalBase, args);
	    }
	}

### 4.3.2 替换对象

	//获取ServiceManager对象的sCache成员变量
	Field sCache_Field = serviceManager_Class.getDeclaredField("sCache");
	sCache_Field.setAccessible(true);
	HashMap<String, IBinder> sCache = (HashMap<String, IBinder>) sCache_Field.get(sServiceManager);

	//将被修改过的Binder存入sCache
	sCache.put(Context.CLIPBOARD_SERVICE, clipProxy);

## 4.4 完整流程

	//获取ServiceManager对象,通过其成员变量sServiceManager获取
	Class serviceManager_Class = Class.forName("android.os.ServiceManager");
	Field sServiceManager_Field = serviceManager_Class.getDeclaredField("sServiceManager");
	sServiceManager_Field.setAccessible(true);
	Object sServiceManager = sServiceManager_Field.get(null);
	//获取原始IBinder,通过反射执行ServiceManager.getService(String name)
	Method getService_Method = serviceManager_Class.getDeclaredMethod("getService", String.class);
	getService_Method.setAccessible(true);
	IBinder originalClipIBinder = (IBinder) getService_Method.invoke(sServiceManager, "clipboard");
	//Hook queryLocalInterface,令其返回 被动态代理的Binder代理对象
	IBinder clipProxy = (IBinder) Proxy.newProxyInstance(originalClipIBinder.getClass().getClassLoader(),
                    new Class[]{IBinder.class}, new OriginalClipBinderProxy(originalClipIBinder));


	//获取ServiceManager对象的sCache成员变量
	Field sCache_Field = serviceManager_Class.getDeclaredField("sCache");
	sCache_Field.setAccessible(true);
	HashMap<String, IBinder> sCache = (HashMap<String, IBinder>) sCache_Field.get(sServiceManager);

	//将被修改过的Binder存入sCache
	sCache.put(Context.CLIPBOARD_SERVICE, clipProxy);



