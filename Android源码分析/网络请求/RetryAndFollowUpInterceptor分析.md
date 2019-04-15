# RetryAndFollowUpInterceptor分析

[okhttp源码分析（二）-RetryAndFollowUpInterceptor过滤器](https://www.jianshu.com/p/3b23521f78b6)

# 1. 简介

根据`RealCall.getResponseWithInterceptorChain()`方法，`RealInterceptorChain `会按照拦截器添加的顺序依次调用拦截器， 拦截器的添加顺序为

1. 用户设置的拦截器

2. RetryAndFollowUpInterceptor——失败和重定向过滤器

3. BridgeInterceptor——封装request和response过滤器

4. CacheInterceptor——缓存相关的过滤器，负责读取缓存直接返回、更新缓存

5. ConnectInterceptor——负责和服务器建立连接，连接池等

6. 用户设置的跟网络相关的拦截器

7. CallServerInterceptor——负责向服务器发送请求数据、从服务器读取响应数据(实际网络请求)

拦截器链(`RealInterceptorChain`)使用责任链模式将开发者设置的拦截器，OKHttp Corem,全部的网络拦截器,网络调用者组合在一起. 

## 1.1 拦截器链的功能

使用下面的伪代码来表示`RealInterceptorChain`的功能:

    Response getResponseWithInterceptorChain() throws IOException {

        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(client.interceptors());
        interceptors.add(retryAndFollowUpInterceptor);
        interceptors.add(new BridgeInterceptor(client.cookieJar()));
        interceptors.add(new CacheInterceptor(client.internalCache()));
        interceptors.add(new ConnectInterceptor(client));
        if (!forWebSocket) {
            interceptors.addAll(client.networkInterceptors());
        }
        interceptors.add(new CallServerInterceptor(forWebSocket));

        Interceptor.Chain chain = new RealInterceptorChain(interceptors, null, null, null, 0,
                originalRequest, this, eventListener, client.connectTimeoutMillis(),
                client.readTimeoutMillis(), client.writeTimeoutMillis());

        return chain.proceed(originalRequest);
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

    public Response intercept(RealInterceptorChain chain) {
    	// 当前拦截器处理
       // 交给下一个拦截器去操作
		response = realChain.proceed(request, streamAllocation, null, null);
		// 当前拦截器处理
		return response;
    }

## 1.2 RetryAndFollowUpInterceptor构造函数

    private final OkHttpClient client;

    public RetryAndFollowUpInterceptor(OkHttpClient client) {
        this.client = client;
    }


# 2. RetryAndFollowUpInterceptor

`RetryAndFollowUpInterceptor`主要作用就是判断当前请求是否需要重试或重定向


## 2.1 RetryAndFollowUpInterceptor的构造函数

    public RetryAndFollowUpInterceptor(OkHttpClient client) {
        this.client = client;
    }
    
-  该拦截器在`RealCall`的构造函数中被创建，其内部持有了一个`OKHttpClient`对象   


# 3. RAFInterceptor.intercept()



    public Response intercept(Chain chain) throws IOException {
    	 //原始的Request请求，未经修改
        Request request = chain.request();
        // 转换类型
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        // 通过OKHttpClient构建出的RealCall
        Call call = realChain.call();
        EventListener eventListener = realChain.eventListener();
		 // 用来创建请求所需要的网络组件，在当前拦截器中没有使用
        StreamAllocation streamAllocation = new StreamAllocation(client.connectionPool(),
                createAddress(request.url()), call, eventListener, callStackTrace);
        // 赋值给了成员变量
        this.streamAllocation = streamAllocation;
		 // 记录重定向次数
        int followUpCount = 0;
        // 记录上一个请求
        Response priorResponse = null;
        while (true) {
        	  // 是否通过RealCall取消了请求
            if (canceled) {
                streamAllocation.release(true);
                throw new IOException("Canceled");
            }

            Response response;
            // 记录是否碰到未知异常
            boolean releaseConnection = true;
            try {
            		// 将请求交给下一个拦截器去操作
                response = realChain.proceed(request, streamAllocation, null, null);
                // 正常情况
                releaseConnection = false;
            } catch (RouteException e) {
                	// 表示请求尝试通过一个路由进行连接失败，请求还未被发出
                	// 通过recover()方法判断请求是否可以重试
                if (!recover(e.getLastConnectException(), streamAllocation, false, request)) {
                    throw e.getFirstConnectException();
                }
                // 已知的异常
                releaseConnection = false;
                // 再次用当前的Request 进行尝试
                continue;
            } catch (IOException e) {
                // 表示尝试与服务端进行通信失败，请求可能已经被发送
                // 判断请求是否已经被发送(判断连接是否被关闭)
                boolean requestSendStarted = !(e instanceof ConnectionShutdownException);
                if (!recover(e, streamAllocation, requestSendStarted, request)) throw e;
                //已知的异常
                releaseConnection = false;
                //再次用当前的Request进行尝试
                continue;
            } finally {
                //遇到了未捕获的异常，直接释放资源
                if (releaseConnection) {
                    streamAllocation.streamFailed(null);
                    streamAllocation.release(true);
                }
            }

            // 如果之前的响应不为空，那么将当前响应附加到前一个响应中
            // 这种响应不应该有body
            if (priorResponse != null) {
                response = response.newBuilder()
                        .priorResponse(priorResponse.newBuilder()
                                .body(null)
                                .build())
                        .build();
            }
			  // 需要重定向的请求
            Request followUp;
            try {
            		//对响应结果进行判断，是否需要进行重定向
                followUp = followUpRequest(response, streamAllocation.route());
            } catch (IOException e) {
                streamAllocation.release(true);
                throw e;
            }

				// 不需要进行重定向，直接返回
            if (followUp == null) {
                streamAllocation.release(true);
                return response;
            }
			  // 需要进行重定向！关闭响应流
            closeQuietly(response.body());
			  // 重定向的次数不能超过最大值，默认为20
            if (++followUpCount > MAX_FOLLOW_UPS) {
                streamAllocation.release(true);
                throw new ProtocolException("Too many follow-up requests: " + followUpCount);
            }
				// UnrepeatableRequestBody 标记请求是不可重复的
            if (followUp.body() instanceof UnrepeatableRequestBody) {
                streamAllocation.release(true);
                throw new HttpRetryException("Cannot retry streamed HTTP body", response.code());
            }
				//判断connection是否相同，否则重新创建streamAllocation
            if (!sameConnection(response, followUp.url())) {
                streamAllocation.release(false);
                streamAllocation = new StreamAllocation(client.connectionPool(),
                        createAddress(followUp.url()), call, eventListener, callStackTrace);
                this.streamAllocation = streamAllocation;
            } else if (streamAllocation.codec() != null) {
                throw new IllegalStateException("Closing the body of " + response
                        + " didn't close its backing stream. Bad interceptor?");
            }
				// 使用重定向或重试的 request再次进行网络请求
            request = followUp;
            priorResponse = response;
        }
    }

- **该拦截器在正常情况下并没有什么逻辑，只有当出现异常，例如重试和重定向时，这个拦截器才起作用**

- `StreamAllocation`在初始化时许多参数都为空，它在之后的链式调用中才会陆续的创建一系列的参数

	其主要的功能是协调`Connections,Streams,Calls`三者之间的关系

- `StreamAllocation`:主要是在ConnectInterceptor中使用,用于创建跟请求相关的网络组件

	1. 用于获取连接服务端的connecttion连接和用于服务端用于数据传输的输入输出流，依次通过拦截器传递给ConnectInterceptor


## 3.1 RAFInterceptor.recover()

	/**
	* 报告并尝试从与服务器通信的失败中恢复,对请求进行重试
	* 如果返回true，表示当前请求是可恢复的，返回false则表示失败是永久性的
	*
	* 带body的请求只有在body被缓存 或者 失败发生在请求被发送之前 才能恢复！
	*/
    private boolean recover(IOException e, StreamAllocation streamAllocation,
                            boolean requestSendStarted, Request userRequest) {
        streamAllocation.streamFailed(e);

        // 用户直接禁止重试或重定向，那么失败就是永久性的
        if (!client.retryOnConnectionFailure()) return false;

        // 请求已经被发送过，并且不支持重复请求，那么失败就是永久性的
        if (requestSendStarted && requestIsUnrepeatable(e, userRequest)) return false;

        // 异常是否严重，如果是非常严重的问题，就不允许重试了
        if (!isRecoverable(e, requestSendStarted)) return false;

        // 判断是否还有未尝试过的路由
        if (!streamAllocation.hasMoreRoutes()) return false;

        // 使用老的路由选择器去进行新的连接
        return true;
    }
    
- `requestSendStarted `表示请求是否已经被发送    
    
    
    
### 3.1.1 RAFInterceptor.requestIsUnrepeatable()

    private boolean requestIsUnrepeatable(IOException e, Request userRequest) {
        return userRequest.body() instanceof UnrepeatableRequestBody
                || e instanceof FileNotFoundException;
    }    
    
- 判断当前请求是否可以重复，有俩种情况    
   
	1. 如果请求的body 的类型是`UnrepeatableRequestBody ` ,表示请求不允许重复请求    
    
	2. 异常为 文件未找到，那么同样也不能重复请求
    
    
### 3.1.2 RAFInterceptor.isRecoverable（）

    private boolean isRecoverable(IOException e, boolean requestSendStarted) {
        //  如果是协议相关问题，无法恢复
        if (e instanceof ProtocolException) {
            return false;
        }
        
        // 中断类型的异常需要区分具体的异常类型，来决定请求是否可恢复
        if (e instanceof InterruptedIOException) {
        		// 如果异常属于Socket超时，并且请求还未发送过 ， 那么是可以恢复的（例如，连接一个路由失败，那么可以尝试下一个）
            return e instanceof SocketTimeoutException && !requestSendStarted;
        }

        // 安全问题
        if (e instanceof SSLHandshakeException) {
            // 只有遇到证书验证失败的情况，不需要进行重试
            if (e.getCause() instanceof CertificateException) {
                return false;
            }
        }
        if (e instanceof SSLPeerUnverifiedException) {
            // e.g. a certificate pinning error.
            return false;
        }

		  // 尝试通过不同的路由 来解决那种已知的不太可能被修复的客户端或协商问题
        // 以上情况都不符,返回true 表示使用一个新的路由去尝试请求
        return true;
    }    
    
- **通过判断异常是否属于可恢复类型，来决定请求是否可以重试**

- `SSLPeerUnverifiedException`:证书校验，请求的域名与证书的域名进行校验 以及 握手 都可能导致这个异常
    

### 3.1.3 StreamAllocation.hasMoreRoutes()

    public boolean hasMoreRoutes() {
        return route != null
                || (routeSelection != null && routeSelection.hasNext())
                || routeSelector.hasNext();
    }
    
- 判断是否还有其他可用的路由

    
## 3.2 Response priorResponse的含义

    if (priorResponse != null) {
        response = response.newBuilder()
                .priorResponse(priorResponse.newBuilder()
                        .body(null)
                        .build())
                .build();
    }    
    
1. 前一个`Response`构建了一个`Response.Builder`，将其信息统统传入，然后利用这个`Builder`将`body`置空，再构建出一个新得`body`为空其他信息不变的`Response`    
    
2. 利用当前`Response`新建了一个`Response.Builder`,并且这个`Builder`中保存了当前`Response`的信息 . 然后将前一个`Response`传入进行保存(会对响应的body进行空判断)  

3. 最终构建出了一个包含了前一个`Response`的`Response`


## 3.3 RAFInterceptor.sameConnection()

    private boolean sameConnection(Response response, HttpUrl followUp) {
        HttpUrl url = response.request().url();
        return url.host().equals(followUp.host())
                && url.port() == followUp.port()
                && url.scheme().equals(followUp.scheme());
    }
    
- 判断重定向的请求是否可以复用当前响应对应的`StreamAllocation`,在`host`,`port`,`scheme`三个方面进行了判断 ，只要这三个条件符合那就符合。


## 3.4 RAFInterceptor.followUpRequest()

	/**
	* 根据响应码，对请求头中添加验证，重定向，处理客户端请求超时。
	*  如果重定向是不需要的，那么会返回null
	*/
    private Request followUpRequest(Response userResponse, Route route) throws IOException {
        if (userResponse == null) throw new IllegalStateException();
        int responseCode = userResponse.code();
		  // 请求的method
        final String method = userResponse.request().method();
        switch (responseCode) {
            case HTTP_PROXY_AUTH:
                Proxy selectedProxy = route != null
                        ? route.proxy()
                        : client.proxy();
                if (selectedProxy.type() != Proxy.Type.HTTP) {
                    throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
                }
                return client.proxyAuthenticator().authenticate(route, userResponse);

            case HTTP_UNAUTHORIZED:
                return client.authenticator().authenticate(route, userResponse);

            case HTTP_PERM_REDIRECT:
            case HTTP_TEMP_REDIRECT:
                // "If the 307 or 308 status code is received in response to a request other than GET
                // or HEAD, the user agent MUST NOT automatically redirect the request"
                if (!method.equals("GET") && !method.equals("HEAD")) {
                    return null;
                }
                // fall-through
            case HTTP_MULT_CHOICE:
            case HTTP_MOVED_PERM:
            case HTTP_MOVED_TEMP:
            case HTTP_SEE_OTHER:
                // Does the client allow redirects?
                if (!client.followRedirects()) return null;

                String location = userResponse.header("Location");
                if (location == null) return null;
                HttpUrl url = userResponse.request().url().resolve(location);

                // Don't follow redirects to unsupported protocols.
                if (url == null) return null;

                // If configured, don't follow redirects between SSL and non-SSL.
                boolean sameScheme = url.scheme().equals(userResponse.request().url().scheme());
                if (!sameScheme && !client.followSslRedirects()) return null;

                // Most redirects don't include a request body.
                Request.Builder requestBuilder = userResponse.request().newBuilder();
                if (HttpMethod.permitsRequestBody(method)) {
                    final boolean maintainBody = HttpMethod.redirectsWithBody(method);
                    if (HttpMethod.redirectsToGet(method)) {
                        requestBuilder.method("GET", null);
                    } else {
                        RequestBody requestBody = maintainBody ? userResponse.request().body() : null;
                        requestBuilder.method(method, requestBody);
                    }
                    if (!maintainBody) {
                        requestBuilder.removeHeader("Transfer-Encoding");
                        requestBuilder.removeHeader("Content-Length");
                        requestBuilder.removeHeader("Content-Type");
                    }
                }

                // When redirecting across hosts, drop all authentication headers. This
                // is potentially annoying to the application layer since they have no
                // way to retain them.
                if (!sameConnection(userResponse, url)) {
                    requestBuilder.removeHeader("Authorization");
                }

                return requestBuilder.url(url).build();

            case HTTP_CLIENT_TIMEOUT:
                // 408's are rare in practice, but some servers like HAProxy use this response code. The
                // spec says that we may repeat the request without modifications. Modern browsers also
                // repeat the request (even non-idempotent ones.)
                if (!client.retryOnConnectionFailure()) {
                    // The application layer has directed us not to retry the request.
                    return null;
                }

                if (userResponse.request().body() instanceof UnrepeatableRequestBody) {
                    return null;
                }

                if (userResponse.priorResponse() != null
                        && userResponse.priorResponse().code() == HTTP_CLIENT_TIMEOUT) {
                    // We attempted to retry and got another timeout. Give up.
                    return null;
                }

                if (retryAfter(userResponse, 0) > 0) {
                    return null;
                }

                return userResponse.request();

            case HTTP_UNAVAILABLE:
                if (userResponse.priorResponse() != null
                        && userResponse.priorResponse().code() == HTTP_UNAVAILABLE) {
                    // We attempted to retry and got another timeout. Give up.
                    return null;
                }

                if (retryAfter(userResponse, Integer.MAX_VALUE) == 0) {
                    // specifically received an instruction to retry without delay
                    return userResponse.request();
                }

                return null;

            default:
                return null;
        }
    }

- **总结一下：根据返回码，决定是否需要重新构建一个`Request`，如果不需要则直接返回null**




# 4. StreamAllocation

这个类协调三个实体（`Conneciton,Stream,Call`）之间的关系

1. `Connection`:连接到远程服务器的物理套接字，可能建立耗时很久，因此可以进行取消当前进行连接的Connection


2. `Stream`:在连接上分层的逻辑Http Request/Response 对 .

	每个连接都有自己的分配限制，该限制定义的是连接可以携带的并发流的数量
	
	Http/1.x 一次可以携带一个，Http/2 通常一次可以携带多个

3. `Call`: 流的逻辑序列，通常是初始请求和后续请求 

	对单个请求的所有流通常都保存在一个Conneciton中，以此获取更便捷的操作行为
	
	
## 4.1 StreamAllocation中重要的成员

`boolean 	noNewStreams`: 表示Connection无法再被新的流使用

`Method streamFinished()`:从此次分配中释放活动的流

`boolean release`:移除请求对Connection的连接。 当流仍然存在时，不会立即移除连接

	
	
## 4.2 StreamAllocation的创建

在`RetryAndFollowUpInterceptor`拦截器中创建了`StreamAllocation`

    StreamAllocation streamAllocation = new StreamAllocation(client.connectionPool(),
            createAddress(request.url()), call, eventListener, callStackTrace);
            
    public StreamAllocation(ConnectionPool connectionPool, Address address, Call call,
                            EventListener eventListener, Object callStackTrace) {
        this.connectionPool = connectionPool;
        this.address = address;
        this.call = call;
        this.eventListener = eventListener;
        this.routeSelector = new RouteSelector(address, routeDatabase(), call, eventListener);
        this.callStackTrace = callStackTrace;
    }
    
    
- `ConnectionPool connectionPool`:连接池

- `Address adress`: 与连接相关的信息，主机名，端口，代理，`SSLSocketFactory`,`HostNameVerifier`,`protocols`等等信息

- `Call call`:对应`RealCall`
            
- `EventListener eventListener`: 默认实现为空

- `RouteSelector`:用来选择一个连接至服务器的路由，每个连接都需要选择代理服务器，IP地址和TLS模式
            
            
### 4.2.1 StreamAllocation.createAddress()    

    private Address createAddress(HttpUrl url) {
    	 // 创建SSlSocket的工厂类
        SSLSocketFactory sslSocketFactory = null;
        //主机名验证
        HostnameVerifier hostnameVerifier = null;
        //证书验证
        CertificatePinner certificatePinner = null;
        // 只有请求是Https 才需要设置
        if (url.isHttps()) {
            sslSocketFactory = client.sslSocketFactory();
            hostnameVerifier = client.hostnameVerifier();
            certificatePinner = client.certificatePinner();
        }

        return new Address(url.host(), url.port(), client.dns(), client.socketFactory(),
                sslSocketFactory, hostnameVerifier, certificatePinner, client.proxyAuthenticator(),
                client.proxy(), client.protocols(), client.connectionSpecs(), client.proxySelector());
    }

- `CertificatePinner `：该类用来约束哪些证书是可信的（固定证书可以防御对证书验证的攻击）

- `SSLSocket`:扩展自Socket，使用SSL或TLS协议的安全套接字

- `DNS`: 解析Host 获取对应的ip地址，返回`InetAddress`列表，(`InetAddress`保存`ip`)

- `Client.proxy`:

### 4.2.2 Address 构造函数

    public Address(String uriHost, int uriPort, Dns dns, SocketFactory socketFactory,
                   @Nullable SSLSocketFactory sslSocketFactory, @Nullable HostnameVerifier hostnameVerifier,
                   @Nullable CertificatePinner certificatePinner, Authenticator proxyAuthenticator,
                   @Nullable Proxy proxy, List<Protocol> protocols, List<ConnectionSpec> connectionSpecs,
                   ProxySelector proxySelector) {
		//对参数进行验证以及赋值

    }

- `Address`:表示连接至源服务器的规格。

	对于简单的连接来说，仅包含主机名和端口 . 如果请求添加了代理，那这里还会包含代理信息。 对于安全连接，还包含`SSLSocketFactory`,`HostNameVerifier`以及`certificate pinner`

### 4.2.3 RouteSelector



