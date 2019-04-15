# ConnectInterceptor分析
[okhttp源码分析（四）-ConnectInterceptor过滤器](https://www.jianshu.com/p/4bf4c796db6f)

[okHttp3源码解析(二)-ConnectInterceptor － 小专栏](https://xiaozhuanlan.com/topic/5208976413)


# 1. 简介

`ConnectInterceptor`拦截器是OKHttp中负责和服务器建立连接的过滤器，与`Volly`,`Android-async-http`等其他网络框架不同的是，`OKHttp`没有利用Google提供的现有的`HttpUrlConnection`等类来直接建立连接，而是专门使用一个拦截器用于建立连接，并在拦截器中对建立连接的过程进行开发和优化(利用OKio)

- `Volly`的底层利用了策略模式，提供了`HttpStack`接口，针对不同的Android版本使用不同的实现，例如>2.3 则使用`HttpUrlConnection`,<2.3则使用`HttpClientStack`


## 1.1 拦截链
在`CacheInterceptor`拦截器中会调用`ConnectInterceptor`拦截器


	public Response CacheInterceptor.intercept(){
		networkResponse = chain.proceed(networkRequest);
	}
	
    public Response proceed(Request request) throws IOException {
    	 // StreamAllocation,httpCodec,connection 使用拦截链中的值
    	 // 除了StreamAllocation 在 RAFInterceptor中被创建，其余仍未被创建
        return proceed(request, streamAllocation, httpCodec, connection);
    }


	public Response proceed(Request request, StreamAllocation streamAllocation, HttpCodec httpCodec,RealConnection connection){
		 // index用来控制具体使用哪个拦截器
        RealInterceptorChain next = new RealInterceptorChain(interceptors, streamAllocation, httpCodec,
                connection, index + 1, request, call, eventListener, connectTimeout, readTimeout,
                writeTimeout);
        Interceptor interceptor = interceptors.get(index);
        Response response = interceptor.intercept(next);
		 return response;
	}
	
## 1.2 ConnectInterceptor构造函数

    public final OkHttpClient client;

    public ConnectInterceptor(OkHttpClient client) {
        this.client = client;
    }
    	
	
# 2. ConnectInterceptor.intercept()

    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        // 获取RAFInterceptor拦截器中创建的 StreamAllocation
        StreamAllocation streamAllocation = realChain.streamAllocation();

        // 这里开始需要使用网络
        // 除了GET类型的请求都需要进行"健康"检查
        boolean doExtensiveHealthChecks = !request.method().equals("GET");
        // 创建HttpCodec
        HttpCodec httpCodec = streamAllocation.newStream(client, chain, doExtensiveHealthChecks);
        // 获取Connection
        RealConnection connection = streamAllocation.connection();
		 // 交给下一个拦截器
        return realChain.proceed(request, streamAllocation, httpCodec, connection);
    }
    
- `HttpCodec`: 一个接口，用来编码Http 请求，解码 Http 响应. 其具体实现类是`Http1Codec`和`Http2Codec`,分别对应`Http`和`Https`

- `RealConnection`:真正建立连接的对象，利用Socket建立连接    
    

## 2.1 StreamAllocation.newStream()

    public HttpCodec newStream(
            OkHttpClient client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
        // 超时时间
        int connectTimeout = chain.connectTimeoutMillis();
        int readTimeout = chain.readTimeoutMillis();
        int writeTimeout = chain.writeTimeoutMillis();
        int pingIntervalMillis = client.pingIntervalMillis();
        // 连接失败是否重试
        boolean connectionRetryEnabled = client.retryOnConnectionFailure();

        try {
        	  // 获取一个“健康”的Connection
            RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout,
                    writeTimeout, pingIntervalMillis, connectionRetryEnabled, doExtensiveHealthChecks);
                    
            // 借助Connection 去创建 HttpCodec
            HttpCodec resultCodec = resultConnection.newCodec(client, chain, this);
		     // 赋值
            synchronized (connectionPool) {
                codec = resultCodec;
                return resultCodec;
            }
        } catch (IOException e) {
            throw new RouteException(e);
        }
    }
    
## 2.2 StreamAllocation.findHealthyConnection()

    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout,
                                                 int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled,
                                                 boolean doExtensiveHealthChecks) throws IOException {
        while (true) {
            RealConnection candidate = findConnection(connectTimeout, readTimeout, writeTimeout,
                    pingIntervalMillis, connectionRetryEnabled);

            // 如果这个Connection是全新的，可以跳过检查，直接返回
            synchronized (connectionPool) {
                if (candidate.successCount == 0) {
                    return candidate;
                }
            }

            // 判断是否连接是否 “健康”
            if (!candidate.isHealthy(doExtensiveHealthChecks)) {
            		// 禁止当前Connection 创建新的流
            		//从连接池中移除，并重新开始下一轮Connection的获取
                noNewStreams();
                continue;
            }

            return candidate;
        }
    }

- 不断循环直至查找到一个“健康”的Connection并返回

    
## 2.3 StreamAllocation.findConnection()

    private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout,
                                          int pingIntervalMillis, boolean connectionRetryEnabled) throws IOException {
                                          		  // 是否早连接池中找到Connection
        boolean foundPooledConnection = false;
        // 保存找到的“健康”的连接
        RealConnection result = null;
        // 保存当前路由
        Route selectedRoute = null;
        // 保存可以释放的连接
        Connection releasedConnection;
        // 对应需要被关闭的连接
        Socket toClose;
        synchronized (connectionPool) {
        	  // 对当前StreamAllocation的状态进行判断
        	  // 以下三种情况下，StreamAllocaiton都不可以再被使用
            if (released) throw new IllegalStateException("released");
            if (codec != null) throw new IllegalStateException("codec != null");
            if (canceled) throw new IOException("Canceled");


            // 尝试复用一个已经分配的连接
            // 需要小心使用，因为这个Connection可能被禁止创建新的流
            releasedConnection = this.connection;
            
            // 对releasedConnection进行检查，如果Connection无法创建新的流，则直接释放掉，并且connection会被置空
            toClose = releaseIfNoNewStreams();
            
            // 当前Connection不为空，则表示可用（因为已经使用了releaseIfNoNewStreams()方法对Connection进行检查)
            if (this.connection != null) {
                result = this.connection;
                releasedConnection = null;
            }
            if (!reportedAcquired) {
                // If the connection was never reported acquired, don't report it as released!
                releasedConnection = null;
            }
            
			  // 当前Connection无法被使用，尝试从连接池中获取一个Connection进行复用
            if (result == null) {
                // 尝试从连接池中获取
                // acquire() 就是借助ConnectionPool进行操作
                Internal.instance.acquire(connectionPool, address, this, null);
                if (connection != null) {
                		// 标记从池中找到了可以复用的Connection
                    foundPooledConnection = true;
                    // 赋值找到的Connection
                    result = connection;
                } else {
                    selectedRoute = route;
                }
            }
        }
        // 如果存在需要关闭的Socket，那么会在该方法中关闭
        closeQuietly(toClose);
		 // 回调
        if (releasedConnection != null) {
            eventListener.connectionReleased(call, releasedConnection);
        }
        // 回调
        if (foundPooledConnection) {
            eventListener.connectionAcquired(call, result);
        }
        
        // 从连接池中找到了可复用的Connection，或者是复用了当前的Connection，那么直接返回
        if (result != null) {
            return result;
        }

        // 切换路由，重新在连接池中查找（如果不存在则创建）
        // 这是一个能导致阻塞的操作
        boolean newRouteSelection = false;
        // 切换路由，继续在连接池中查找
        if (selectedRoute == null && (routeSelection == null || !routeSelection.hasNext())) {
            newRouteSelection = true;
            // 通过路由选择器切换路由
            routeSelection = routeSelector.next();
        }

        synchronized (connectionPool) {
            if (canceled) throw new IOException("Canceled");
			  // 出现了新的路由
            if (newRouteSelection) {
                // Now that we have a set of IP addresses, make another attempt at getting a connection from
                // the pool. This could match due to connection coalescing.
                List<Route> routes = routeSelection.getAll();
                for (int i = 0, size = routes.size(); i < size; i++) {
                    Route route = routes.get(i);
                    // 使用新的路由尝试从连接池中获取
                    Internal.instance.acquire(connectionPool, address, this, route);
                    // 获取到了新的路由
                    if (connection != null) {
                    	 // 标记从池中找到了Connection
                        foundPooledConnection = true;
                        result = connection;
                        // 记录当前使用的Route
                        this.route = route;
                        // 找到了 结束循环
                        break;
                    }
                }
            }
				
			  // 未在连接池中找到Connection
            if (!foundPooledConnection) {
                if (selectedRoute == null) {
                    selectedRoute = routeSelection.next();
                }

                // Create a connection and assign it to this allocation immediately. This makes it possible
                // for an asynchronous cancel() to interrupt the handshake we're about to do.
                //创建一个Connection,并分配资源
                // 这使得异步的cancel()可以取消将要进行的handshake
                route = selectedRoute;
                refusedStreamCount = 0;
                result = new RealConnection(connectionPool, selectedRoute);
                // 通过Connection 创建流
                acquire(result, false);
            }
        }

        // 切换路由之后从连接池中找到，那么需要调用回调并返回
        if (foundPooledConnection) {
            eventListener.connectionAcquired(call, result);
            return result;
        }

		  // Connection是新创建的
        // 进行 TCP + TLS handshakes操作(阻塞的操作)
        result.connect(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis,
                connectionRetryEnabled, call, eventListener);
        // 将该路由从错误缓存记录中移除
        routeDatabase().connected(result.route());

        Socket socket = null;
        synchronized (connectionPool) {
            reportedAcquired = true;

            // 将Connection加入连接池
            Internal.instance.put(connectionPool, result);

            // 去重操作
            // 存在多个Connection连接至相同的Address，则合并
            if (result.isMultiplexed()) {
                socket = Internal.instance.deduplicate(connectionPool, address, this);
                result = connection;
            }
        }
        closeQuietly(socket);

        eventListener.connectionAcquired(call, result);
        return result;
    }

- 该方法主要作用：返回一个Connection用来host a new stream 。 优先复用当前可用的Connection，其次再从连接池中获取Connection，最后再通过创建获取Conneciton


### 2.3.1 StreamAllocation.releaseIfNoNewStreams()


    private Socket releaseIfNoNewStreams() {
        assert (Thread.holdsLock(connectionPool));
        // 当前连接是否为空，并且当前连接无法再创建新的流，则进行回收
        RealConnection allocatedConnection = this.connection;
        if (allocatedConnection != null && allocatedConnection.noNewStreams) {
        	  // 进行回收
            return deallocate(false, false, true);
        }
        return null;
    }
    
- 如果当前持有的Connection被限制去创建新的流，那么回收当前连接，同时返回一个可以被关闭的Socket
	
	由于Http2 多个请求可以共享一个相同的连接，因此可能存在当前连接限制后续的请求
	
	
- 该方法的返回值可以分为以下几种情况:

	1. 当前连接为空(即当前没有连接)，直接返回null

	2. 当前连接不为空，并且仍然可以创建流(即仍然可用),直接返回null

	3. 当前连接不为空，并且无法创建流(即不可用)

- `noNewStreams`是一个布尔值，表示该Connection无法创建新的流，**并且，一旦该值被设置为true，便无法更改**	

- 调用了`deallocate()`方法，其参数`noNewStreams = false,released = false, streamFinished = true`

### 2.3.2 StreamAllocation.deallocate()

    private Socket deallocate(boolean noNewStreams, boolean released, boolean streamFinished) {
        assert (Thread.holdsLock(connectionPool));
		  // 当前Connection无法创建新的流，将HttpCodec 置空
        if (streamFinished) {
            this.codec = null;
        }
        // 标记当前StreamAllocation需要释放
        if (released) {
            this.released = true;
        }
        Socket socket = null;
        // 连接非空，需要进行释放
        if (connection != null) {
        	  // 表示当前Connection无法创建新的流
            if (noNewStreams) {
                connection.noNewStreams = true;
            }
            if (this.codec == null && (this.released || connection.noNewStreams)) {
                // 释放Connection 中保存的StreamAllocation
                release(connection);
                // 当前Connection的流为空
                if (connection.allocations.isEmpty()) {
                    connection.idleAtNanos = System.nanoTime();
                    //  判断该Conneciton是否需要从连接池中被移除（无法创建新的流||不允许存在空闲Connection），如果被移除则需要关闭！！！
                    if (Internal.instance.connectionBecameIdle(connectionPool, connection)) {
                        //不允许在同步代码块中做io的操作，因此返回了一个closable的实现类，等到完成同步代码块之后再通过closable进行关闭操作
                        // 返回的是Connection持有的Socket
                        socket = connection.socket();
                    }
                }
                connection = null;
            }
        }
        return socket;
    }

- 释放此次分配的资源。如果分配了足够的资源，连接会被断开或关闭

	返回一个`closeable`（即Socket）,调用者在完成同步块中的代码之后需要传递给`closeQuietly()`进行关闭，（因为在连接池的同步块中不记性IO操作）
	
### 2.3.3 StreamAllocation.release()

    private void release(RealConnection connection) {
        for (int i = 0, size = connection.allocations.size(); i < size; i++) {
            Reference<StreamAllocation> reference = connection.allocations.get(i);
            if (reference.get() == this) {
                connection.allocations.remove(i);
                return;
            }
        }
        throw new IllegalStateException();
    }
    
- 从当前Connection持有的流的列表中移除指定那个
    
	
### 2.3.4 ConnectionPool.connectionBecameIdle()

    boolean connectionBecameIdle(RealConnection connection) {
        assert (Thread.holdsLock(this));
        // 当前Connection无法创建新的流 || 不允许存在空闲Connection
        if (connection.noNewStreams || maxIdleConnections == 0) {
        	  // 从连接池中移除
            connections.remove(connection);
            return true;
        } else {
            notifyAll(); // Awake the cleanup thread: we may have exceeded the idle connection limit.
            return false;
        }
    }	
    
- 通知连接池当前传入的Connection处于空闲状态，如果返回true 则表示传入的Connection已经被移除并且需要被关闭！


### 2.3.5 Internal.instance.acquire()

    public void acquire(ConnectionPool pool,
                        Address address,
                        StreamAllocation streamAllocation,
                        @Nullable Route route) {
        // 将方法转叫给ConnectionPool实现
        pool.acquire(address, streamAllocation, route);
    }
    
- **注意这里的`route`为空**    


### 2.3.6 ConnectionPool.acquire()

`Internal.instance` 是一个抽象的单例，其具体的实现类在OKHttpClient的静态代码块之中

    void acquire(Address address, StreamAllocation streamAllocation, @Nullable Route route) {
    	  // 断言当前线程已经持有指定锁
        assert (Thread.holdsLock(this));
        // 遍历池中可复用的连接
        for (RealConnection connection : connections) {
      		  // 判断俩者是否匹配 address和 route
            if (connection.isEligible(address, route)) {
            	   // 连接池中存在可以复用的连接
            	   // 那就往这个Connection中添加一个流的引用
                streamAllocation.acquire(connection, true);
                return;
            }
        }
    }

#### 2.3.6.1 RealConnection.isEligible()

    public boolean isEligible(Address address, @Nullable Route route) {
        // 如果当前Connection 最大并发数已经达到上限 || 无法创建新的流 。 则匹配失败
        if (allocations.size() >= allocationLimit || noNewStreams) return false;

        //  如果俩个Address除了host之外的字段没有完全重叠，则匹配失败
        if (!Internal.instance.equalsNonHost(this.route.address(), address)) return false;

        // 如果host 匹配成功，则匹配成功
        if (address.url().host().equals(this.route().address().url().host())) {
            return true; // This connection is a perfect match.
        }

        // 即使host 匹配失败，一下仍然可以通过http2 的特性合并复用
        // See also:
        // https://hpbn.co/optimizing-application-delivery/#eliminate-domain-sharding
        // https://daniel.haxx.se/blog/2016/08/18/http2-connection-coalescing/

        // 1. 连接必须是 HTTP/2.
        if (http2Connection == null) return false;

        // 2. The routes must share an IP address. This requires us to have a DNS address for both
        // hosts, which only happens after route planning. We can't coalesce connections that use a
        // proxy, since proxies don't tell us the origin server's IP address.
        if (route == null) return false;
        // 不允许使用代理
        if (route.proxy().type() != Proxy.Type.DIRECT) return false;
        if (this.route.proxy().type() != Proxy.Type.DIRECT) return false;
        if (!this.route.socketAddress().equals(route.socketAddress())) return false;

        // 3. Connection的服务器证书必须覆盖新的host
        if (route.address().hostnameVerifier() != OkHostnameVerifier.INSTANCE) return false;
        if (!supportsUrl(address.url())) return false;

        // 4. 证书需要匹配host
        try {
            address.certificatePinner().check(address.url().host(), handshake().peerCertificates());
        } catch (SSLPeerUnverifiedException e) {
            return false;
        }

        return true; // The caller's address can be carried by this connection.
    }
    
- 主要分为俩种情况

	1. host相同，直接复用连接

	2. 如果是Http/2 ,通过其特性合并连接复用

#### 2.3.6.2 StreamAllocation.acquire

    public void acquire(RealConnection connection, boolean reportedAcquired) {
        assert (Thread.holdsLock(connectionPool));
        if (this.connection != null) throw new IllegalStateException();
		 // 当前StreamAllocation对应这个 connection
        this.connection = connection;
        // 标记已经获得
        this.reportedAcquired = reportedAcquired;
        // 将StreamAllocation进行包装，然后保存到Connection
        connection.allocations.add(new StreamAllocationReference(this, callStackTrace));
    }

- 创建一个包装引用（弱引用），用来保存当前StreamAllocation. 即往当前Connection中添加一条流



### 2.3.7 RealConnection.connect()

真正建立了连接的地方。。等学习了Http的知识之后 再回来分析看看。。。
//TODO


### 2.3.8 routeDatabase().connected()

	public final class RouteDatabase {
	    private final Set<Route> failedRoutes = new LinkedHashSet<>();
	
	    /**
	     * Records a failure connecting to {@code failedRoute}.
	     */
	    public synchronized void failed(Route failedRoute) {
	        failedRoutes.add(failedRoute);
	    }
	
	    /**
	     * Records success connecting to {@code route}.
	     */
	    public synchronized void connected(Route route) {
	        failedRoutes.remove(route);
	    }
	
	    /**
	     * Returns true if {@code route} has failed recently and should be avoided.
	     */
	    public synchronized boolean shouldPostpone(Route route) {
	        return failedRoutes.contains(route);
	    }
	}

- OKHttp会记录

## 2.4 RealConnection.newCodec()

    public HttpCodec newCodec(OkHttpClient client, Interceptor.Chain chain,
                              StreamAllocation streamAllocation) throws SocketException {
         // 如果连接是Http/2,则返回Http/2 对应的Http2Codec
        if (http2Connection != null) {
            return new Http2Codec(client, chain, streamAllocation, http2Connection);
        } else {
        	  //Http1.1
            socket.setSoTimeout(chain.readTimeoutMillis());
            source.timeout().timeout(chain.readTimeoutMillis(), MILLISECONDS);
            sink.timeout().timeout(chain.writeTimeoutMillis(), MILLISECONDS);
            return new Http1Codec(client, streamAllocation, source, sink);
        }
    }
    
- 根据不同的根据Connection