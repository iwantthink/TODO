# OKHttp分析

[okhttp源码分析（一）——基本流程（超详细）](https://www.jianshu.com/p/37e26f4ea57b)

[Github- okhttp 库](https://github.com/square/okhttp)


# 1. 简介

适用于Android 和Java 应用程序的`Http`和`Http/2` 客户端

文章基于`OKHttp - 3.13.0`

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

- `AsyncCall`封装了传入的回调函数，并且存在一些与`RealCall`(表示当前请求)相关的操作

## 6.2 Dispatcher.enqueue()

    void enqueue(AsyncCall call) {
        synchronized (this) {
        	  // 往异步等待队列中添加
            readyAsyncCalls.add(call);
            // 通过host寻找当前正在运行或等待列表中的AsyncCall
            // 然后更新当次请求对host的请求次数          
            if (!call.get().forWebSocket) {
                AsyncCall existingCall = findExistingCallWithHost(call.host());
                if (existingCall != null) call.reuseCallsPerHostFrom(existingCall);
            }
        }
        promoteAndExecute();
    }
 
- `Dispatcher`中有三个重要的队列

	1. `Deque<AsyncCall> readyAsyncCalls`: 异步等待队列
    
    2. `Deque<AsyncCall> runningAsyncCalls`：正在进行的异步队列

    3. `Deque<RealCall> runningSyncCalls`：正在进行的同步队列

### 6.2.1 Dispatcher. findExistingCallWithHost()

    private AsyncCall findExistingCallWithHost(String host) {
        for (AsyncCall existingCall : runningAsyncCalls) {
            if (existingCall.host().equals(host)) return existingCall;
        }
        for (AsyncCall existingCall : readyAsyncCalls) {
            if (existingCall.host().equals(host)) return existingCall;
        }
        return null;
    }
    
- 根据当次请求的`host`优先从正在进行的异步相关的队列中取出对应的`AsyncCall`


## 6.3 Dispatcher. promoteAndExecute()

    private boolean promoteAndExecute() {
        assert (!Thread.holdsLock(this));
		 //保存可以执行的请求
        List<AsyncCall> executableCalls = new ArrayList<>();
        boolean isRunning;
        synchronized (this) {
            for (Iterator<AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
                AsyncCall asyncCall = i.next();
					// 判断当前运行的请求是否超过最大数量（64),超过则结束处理，等待下次
                if (runningAsyncCalls.size() >= maxRequests) break; // Max capacity.
                // 判断对同一个host的请求次数是否超过最大数量(5)
                // 超过则去处理下一个
                if (asyncCall.callsPerHost().get() >= maxRequestsPerHost)
                    continue; // Host max capacity.
					// 从readyAsyncCalls中移除
                i.remove();
                // 对当前host的请求次数自增
                asyncCall.callsPerHost().incrementAndGet();
                executableCalls.add(asyncCall);
                runningAsyncCalls.add(asyncCall);
            }
            isRunning = runningCallsCount() > 0;
        }

        for (int i = 0, size = executableCalls.size(); i < size; i++) {
            AsyncCall asyncCall = executableCalls.get(i);
            asyncCall.executeOn(executorService());
        }

        return isRunning;
    }
    
- 将合格的请求从`readyAsyncCalls`提升到`runningAsyncCalls`中，并在线程池中执行    

### 6.3.1 Dispatcher.executorService()

    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<>(), Util.threadFactory("OkHttp Dispatcher", false));
        }
        return executorService;
    }
    
- 每当一个请求进来，都会创建一个线程去单独执行，空闲的线程等待超过60s自动死亡

## 6.4 `RealCall$AsyncCall`.executeOn()

    void executeOn(ExecutorService executorService) {
        assert (!Thread.holdsLock(client.dispatcher()));
        boolean success = false;
        try {
        		//执行
            executorService.execute(this);
            //标记执行成功
            success = true;
        } catch (RejectedExecutionException e) {
        		//创建线程池拒绝异常，并抛给回调
            InterruptedIOException ioException = new InterruptedIOException("executor rejected");
            ioException.initCause(e);
            //SDK
            eventListener.callFailed(RealCall.this, ioException);
            //用户
            responseCallback.onFailure(RealCall.this, ioException);
        } finally {
        		//出现异常，则需要额外处理
            if (!success) {
                client.dispatcher().finished(this); // This call is no longer running!
            }
        }
    }  
    
- 尝试通过线程池去执行请求,当call执行失败，会去关闭线程池，这时可能抛出`RejectedExecutionException`，那就需要调用回调以及关闭一些资源

### 6.4.1 Dispatcher.finished()

    void finished(AsyncCall call) {
    		//当次请求对host的请求失败，不能算一次成功的请求
        call.callsPerHost().decrementAndGet();
        finished(runningAsyncCalls, call);
    }
    
    
    private <T> void finished(Deque<T> calls, T call) {
    	  //当runningAsyncCalls为0时的回调
        Runnable idleCallback;
        synchronized (this) {
            if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
            idleCallback = this.idleCallback;
        }
			//再次执行以确保当前没有正在运行的请求，确保readyAsyncCalls队列也没有可运行的
        boolean isRunning = promoteAndExecute();

        if (!isRunning && idleCallback != null) {
            idleCallback.run();
        }
    }
    
- 从`runningAsyncCalls`队列中移除当前请求。 在当前运行的请求为空时，调用Dispatcher空闲回调  

### 6.4.2 NamedRunnable

	public abstract class NamedRunnable implements Runnable {
	    protected final String name;
	
	    public NamedRunnable(String format, Object... args) {
	        this.name = Util.format(format, args);
	    }
	
	    @Override
	    public final void run() {
	        String oldName = Thread.currentThread().getName();
	        Thread.currentThread().setName(name);
	        try {
	            execute();
	        } finally {
	            Thread.currentThread().setName(oldName);
	        }
	    }
	
	    protected abstract void execute();
	}
	
- 在执行时，修改线程名称！	

## 6.5 `RealCall$AsyncCall`.execute()

    @Override
    protected void execute() {
    		//标记是否已经调用回调
        boolean signalledCallback = false;
        timeout.enter();
        try {
        		// 获取通过了拦截器的响应
            Response response = getResponseWithInterceptorChain();
            // 判断请求是否被取消
            if (retryAndFollowUpInterceptor.isCanceled()) {
                signalledCallback = true;
                // 调用请求失败的回调，并传递原因 canceled
                responseCallback.onFailure(RealCall.this, new IOException("Canceled"));
            } else {
            		// 调用请求成功的回调，并返回响应结果
                signalledCallback = true;
                responseCallback.onResponse(RealCall.this, response);
            }
        } catch (IOException e) {
            e = timeoutExit(e);
            if (signalledCallback) {
                // Do not signal the callback twice!
                Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
            } else {
                eventListener.callFailed(RealCall.this, e);
                responseCallback.onFailure(RealCall.this, e);
            }
        } finally {
        		// 移除请求，Dispatcher空闲回调
            client.dispatcher().finished(this);
        }
    }
    
- 在调用`RealCall`对请求进行取消，会将状态传递给`RetryAndFollowUpInterceptor`    
	
	    public void cancel() {
	        retryAndFollowUpInterceptor.cancel();
	    }

## 6.6 RealCall. getResponseWithInterceptorChain()

    Response getResponseWithInterceptorChain() throws IOException {
        // 保存拦截器
        List<Interceptor> interceptors = new ArrayList<>();
        // 首先添加用户设置的拦截器
        interceptors.addAll(client.interceptors());
        //失败和重定向拦截器
        interceptors.add(retryAndFollowUpInterceptor);
        //封装request和response的拦截器
        interceptors.add(new BridgeInterceptor(client.cookieJar()));
        //缓存相关的拦截器
        interceptors.add(new CacheInterceptor(client.internalCache()));
        //连接相关的拦截器
        interceptors.add(new ConnectInterceptor(client));
        if (!forWebSocket) {
        		//添加用户设置的与网络相关的拦截器
        		//返回观察单个网络请求和响应的不可变拦截器列表
            interceptors.addAll(client.networkInterceptors());
        }
        // 具体执行请求，获取响应的拦截器
        interceptors.add(new CallServerInterceptor(forWebSocket));
		  // 利用递归的思想，对拦截器进行处理
        Interceptor.Chain chain = new RealInterceptorChain(interceptors, null, null, null, 0,
                originalRequest, this, eventListener, client.connectTimeoutMillis(),
                client.readTimeoutMillis(), client.writeTimeoutMillis());

        return chain.proceed(originalRequest);
    }
    
    
- `BridgeInterceptor`:负责把用户构造的请求转换为发送到服务器的请求（配置请求头等）,同时把服务器返回的响应转换为对用户友好的响应信息

	从应用程序代码到网络代码的桥梁。首先，它根据用户请求构建网络请求。然后它继续呼叫网络。最后，它根据网络响应构建用户响应。    
	
- `CacheInterceptor`:处理缓存,根据条件(存在响应缓存并被设置为不变的或者响应在有效期内)返回缓存响应
	
	设置请求头(If-None-Match、If-Modified-Since等) 服务器可能返回304(未修改)
   
   可配置用户自己设置的缓存拦截器	
   
- `ConnectInterceptor `:负责真正的请求网络，和服务器进行连接

- `CallServerInterceptor `:执行流操作(写请求体、获得响应数据) 负责向服务器发送请求数据、从服务器读取响应数据

	进行http请求报文的封装与请求报文的解析
	
- `RealInterceptorChain `:责任链模式,每个拦截器只负责自身的任务，然后将不属于自身的任务交给其他拦截器去处理，简化了责任和逻辑	

	`RealInterceptorChain `的构造函数仅仅是初始化了数据，重点在其`proceed()`方法\
	
### 6.6.1 	RealInterceptorChain的构造函数

	Interceptor.Chain chain = new RealInterceptorChain(
		interceptors, 
		null, null, null, 0,
		originalRequest, this, eventListener, 
		client.connectTimeoutMillis(),
		client.readTimeoutMillis(), 
		client.writeTimeoutMillis());


    public RealInterceptorChain(List<Interceptor> interceptors, StreamAllocation streamAllocation,
                                HttpCodec httpCodec, RealConnection connection, int index, Request request, Call call,
                                EventListener eventListener, int connectTimeout, int readTimeout, int writeTimeout) {
			。。。。。简单的赋值
    }
    
- **这里的`StreamAllocation streamAllocation`,`HttpCodec httpCodec`,`RealConnection connection` 都为空， `int index`  为0**


## 6.7 RealInterceptorChain.proceed()

    @Override
    public Response proceed(Request request) throws IOException {
        return proceed(request, streamAllocation, httpCodec, connection);
    }
    
- `streamAllocation`,`httpCodec`,`connection`都是用的是成员变量，是`RealInterceptorChain`被构造时传入的，值都为null 
    
## 6.8 RealInterceptorChain.proceed()
    
    public Response proceed(Request request, StreamAllocation streamAllocation, HttpCodec httpCodec,
                            RealConnection connection) throws IOException {
        // index 不能超过拦截器的数量
        if (index >= interceptors.size()) throw new AssertionError();

        calls++;

        // If we already have a stream, confirm that the incoming request will use it.
        if (this.httpCodec != null && !this.connection.supportsUrl(request.url())) {
            throw new IllegalStateException("network interceptor " + interceptors.get(index - 1)
                    + " must retain the same host and port");
        }

        // If we already have a stream, confirm that this is the only call to chain.proceed().
        if (this.httpCodec != null && calls > 1) {
            throw new IllegalStateException("network interceptor " + interceptors.get(index - 1)
                    + " must call proceed() exactly once");
        }

        //重点在这里，index 自增，其他参数原封不动，
        RealInterceptorChain next = new RealInterceptorChain(interceptors, streamAllocation, httpCodec,
                connection, index + 1, request, call, eventListener, connectTimeout, readTimeout,
                writeTimeout);
    	  // 获取当前index 的拦截器
        Interceptor interceptor = interceptors.get(index);
        // 调用拦截器对RealInterceptorChain进行处理,拦截器内部会再次调用RealInterceptorChain的proceed（）方法
        Response response = interceptor.intercept(next);

        // Confirm that the next interceptor made its required call to chain.proceed().
        if (httpCodec != null && index + 1 < interceptors.size() && next.calls != 1) {
            throw new IllegalStateException("network interceptor " + interceptor
                    + " must call proceed() exactly once");
        }

        // Confirm that the intercepted response isn't null.
        if (response == null) {
            throw new NullPointerException("interceptor " + interceptor + " returned null");
        }

        if (response.body() == null) {
            throw new IllegalStateException(
                    "interceptor " + interceptor + " returned a response with no body");
        }

        return response;
    }
    
- `HttpCodec`:负责对请求和响应的编码和解码    

- `RealInterceptorChain.proceed()`方法会获取当前index的拦截器，然后构建一个新的`RealInterceptorChain`,将其作为参数传给下当前拦截器,当前拦截器内部会再次调用`RealInterceptorChain`的`proceed()`方法 直至最后一个拦截器执行完毕
    
    
# 7. 同步请求

## 7.1 RealCall.execute()

    public Response execute() throws IOException {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        captureCallStackTrace();
        timeout.enter();
        eventListener.callStart(this);
        try {
        	  // 将当前请求添加到 runningAsyncCalls列表中
            client.dispatcher().executed(this);
            // 直接通过拦截链去获取响应
            Response result = getResponseWithInterceptorChain();
            if (result == null) throw new IOException("Canceled");
            return result;
        } catch (IOException e) {
            e = timeoutExit(e);
            eventListener.callFailed(this, e);
            throw e;
        } finally {
            client.dispatcher().finished(this);
        }
    }
    
# 8. 整体流程图


![](http://ww1.sinaimg.cn/large/6ab93b35gy1g1yspd9jpsj20rs18gjs3.jpg)
    

    

   RealInterceptorChainR	 