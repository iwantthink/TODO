# OKHttp分析

[okhttp源码分析（一）——基本流程（超详细）](https://www.jianshu.com/p/37e26f4ea57b)

[Github- okhttp 库](https://github.com/square/okhttp)


# 1. 简介

适用于Android 和Java 应用程序的`Http`和`Http/2` 客户端

# 2. 使用方式

	OkHttpClient client = new OkHttpClient.Builder().build();
	Request request = new Request.Builder().url(url).build();
	Call call = client.newCall(request);

## 2.1 同步请求

	try {
		Response response = call.execute();
		ResponseBody body = response.body();
		Log.d("NetUtils", body.string());
		body.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
- 在当前方法所在的线程中执行	

## 2.2 异步请求

	call.enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Log.d("NetUtils", response.body().string());
        }
    });
    
- 通过线程池创建子线程并运行


# 3. OkHttpClinet的创建过程

`OkHttpClient`有俩种创建方式，一是直接通过构造函数创建，二是通过`OKHttpClient`的静态内部类`Builder`进行创建

- 区别是：前者使用默认的配置，后者可以对配置进行修改

## 3.1 通过构造函数创建

	OkHttpClient client = new OkHttpClient();

	public OkHttpClient() {
		this(new Builder());
	}
	  
 - 使用默认的`Builder`对`OKHttp`进行初始化
  
## 3.2 通过`OKHttp$Builder`创建  

    OkHttpClient client = new OkHttpClient.Builder().build();
    
    
## 3.3 `OKHttp$Builder`的构造函数

    public Builder() {
    	// 调度器
      dispatcher = new Dispatcher();
      // 应用层支持的协议 http1.1  http2
      protocols = DEFAULT_PROTOCOLS;
      // 传输层版本和连接协议
      connectionSpecs = DEFAULT_CONNECTION_SPECS;
      // 定义一个工厂，返回了一个实现为空的EventListener
      eventListenerFactory = EventListener.factory(EventListener.NONE);
      // 代理选择器
      proxySelector = ProxySelector.getDefault();
      if (proxySelector == null) {
        proxySelector = new NullProxySelector();
      }
      // cookie
      cookieJar = CookieJar.NO_COOKIES;
      socketFactory = SocketFactory.getDefault();
      // 主机名验证
      hostnameVerifier = OkHostnameVerifier.INSTANCE;
      // 证书链
      certificatePinner = CertificatePinner.DEFAULT;
      // 代理身份验证
      proxyAuthenticator = Authenticator.NONE;
      // 本地身份验证
      authenticator = Authenticator.NONE;
      // 链接池
      connectionPool = new ConnectionPool();
      // 域名
      dns = Dns.SYSTEM;
      // 是否支持SSL重定向
      followSslRedirects = true;
      // 是否支持重定向
      followRedirects = true;
      // 是否支持连接失败重试
      retryOnConnectionFailure = true;
      // 超时时间
      callTimeout = 0;
      // 连接超时
      connectTimeout = 10_000;
      // 读取超时
      readTimeout = 10_000;
      // 写入超时
      writeTimeout = 10_000;
      pingInterval = 0;
    }    
    
# 4. Request的创建

通过建造者模式创建

    Request request = new Request.Builder().url(url).build();
    
## 4.1 `Request$Builder`

    public Builder() {
      this.method = "GET";
      this.headers = new Headers.Builder();
    }

- 默认的`method`设置为`GET` , 并创建了一个`Header`的构造器
    
# 5. Call的创建

	Call call = client.newCall(request)

- 一个`Call`准备好执行的请求，可以对其进行例如取消等操作。由于该对象表示一对请求/响应，因此无法重复执行！

## 5.1 OKHttpClient.newCall()

	  @Override public Call newCall(Request request) {
	    return RealCall.newRealCall(this, request, false /* for web socket */);
	  }

## 5.2 RealCall.newRealCall()

	  static RealCall newRealCall(OkHttpClient client, Request originalRequest, boolean forWebSocket) {
	    // Safely publish the Call instance to the EventListener.
	    RealCall call = new RealCall(client, originalRequest, forWebSocket);
	    call.eventListener = client.eventListenerFactory().create(call);
	    return call;
	  }
	  
- 从这里可以知道`Call`真正的实现类是`RealCall`  

### 5.2.1 RealCall的构造函数

	  private RealCall(OkHttpClient client, Request originalRequest, boolean forWebSocket) {
	    this.client = client;
	    this.originalRequest = originalRequest;
	    this.forWebSocket = forWebSocket;
	    // 创建重试和重定向拦截器
	    this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(client);
	    this.timeout = new AsyncTimeout() {
	      @Override protected void timedOut() {
	        cancel();
	      }
	    };
	    this.timeout.timeout(client.callTimeoutMillis(), MILLISECONDS);
	  }
	  
- `AsyncTimeout `使用后台线程对超时操作进行记录  ，这里重写了超时之后的逻辑。然传入了在`OKHttpClient$Builder`中的初始化的属性`callTimeOut`(默认值为0)

### 5.2.2 EventListener的创建

获取了`OKHttpClient`中的工厂，并调用了`create()`,查看该工厂类，可以直接返回的是一个默认的空实现的`EventListener`

		// OKHttpClient$Builder的构造函数
	  eventListenerFactory = EventListener.factory(EventListener.NONE)
	  
	  public static final EventListener NONE = new EventListener() {
	  };

	  static EventListener.Factory factory(EventListener listener) {
	    return call -> listener;
	  }

# 6. 异步请求

## 6.1 RealCall.enqueue()

    @Override
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        // 为retryAndFollowUpInterceptor 设置callStackTrace
        captureCallStackTrace();
        // 默认是空实现
        eventListener.callStart(this);
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }
    
- 检查是否重复执行 

- `AsyncCall`封装了传入的回调函数，

## 6.2 Dispatcher.enqueue()





# 7.

    

    