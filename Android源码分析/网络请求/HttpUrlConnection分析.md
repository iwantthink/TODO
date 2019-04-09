# HttpUrlConnection分析

[HttpHandler 源码](https://android.googlesource.com/platform/external/okhttp/+/refs/heads/oreo-release/android/main/java/com/squareup/okhttp/HttpHandler.java)

[ConnectionSpec 源码](https://android.googlesource.com/platform/external/okhttp/+/refs/heads/oreo-release/okhttp/src/main/java/com/squareup/okhttp/ConnectionSpec.java)

[OkUrlFactory 源码](https://android.googlesource.com/platform/external/okhttp/+/refs/heads/oreo-release/okhttp-urlconnection/src/main/java/com/squareup/okhttp/OkUrlFactory.java)

[ConfigAwareConnectionPool 源码](https://android.googlesource.com/platform/external/okhttp/+/refs/heads/oreo-release/android/main/java/com/squareup/okhttp/ConfigAwareConnectionPool.java)

[HttpURLConnection 源码分析](https://www.jianshu.com/p/35ecbc09c160)


# 1. 简介

`HttpUrlConnection`是对Http协议的实现

## 1.1 URL简介

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g1vdjbn4eoj20jw03v3yd.jpg)

功能| 方法名称 | 值
---|---
获得URL的协议 |`String getProtocol()`|`http`
获得授权机构信息（包括用户信息、主机和端口） |`String getAuthority()`|`user:pass@www.example.com:80`
获得用户信息（用户名和密码） |`String getUserInfo()`|`user:pass`
获取主机地址（域名或ip地址） |`String getHost()`|`www.example.com`
获得端口 |`int getPort()`|`80`
获得文件信息（路径、文件名和查询参数） |`String getFile()`|`index.html?uid=1#fragmentid`
获得路径信息（路径、文件名） |`String getPath()`|`index.html`
获取查询参数信息 |`String getQuery()`|`uid=1`
获得片段信息 |`String getRef()`|`fragmentid`
获得该协议默认端口 |`int getDefaultPort()`|`80`


# 2. HttpUrlConnection的使用方式

    public static void request(String action, String requestUrl) throws IOException {

        URL url;
        HttpURLConnection conn;
        if (NETWORK_GET.equals(action)) {
            url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            //HttpURLConnection默认就是用GET发送请求，所以下面的setRequestMethod可以省略
            conn.setRequestMethod("GET");
            // 表示是否可以读取响应体中的数据，默认为true。
            conn.setDoInput(true);
            //用setRequestProperty方法设置一个自定义的请求头字段
            conn.setRequestProperty("action", NETWORK_GET);
            //禁用网络缓存
            conn.setUseCaches(false);
            //在对各种参数配置完成后，通过调用connect方法建立TCP连接，但是并未真正获取数据
            //conn.connect()方法不必显式调用，当调用conn.getInputStream()方法时内部也会自动调用connect方法
            conn.connect();


            Log.d("NetUtils", "conn.getResponseCode():" + conn.getResponseCode());

            //调用getInputStream方法后，服务端才会收到完整的请求，并阻塞式地接收服务端返回的数据
            InputStream is = conn.getInputStream();
        } else if (NETWORK_POST.equals(action)) {
            //用POST发送键值对数据
            url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            //通过setRequestMethod将conn设置成POST方法
            conn.setRequestMethod("POST");
            //表示是否可以通过请求体发送数据给服务端，默认为false。
            conn.setDoOutput(true);
            //用setRequestProperty方法设置一个自定义的请求头:action
            conn.setRequestProperty("action", NETWORK_POST);
            //获取conn的输出流
            OutputStream os = conn.getOutputStream();
            //获取两个键值对name=孙群和age=27的字节数组，将该字节数组作为请求体
            byte[] requestBody = new String("category_id=56&utm_medium=index-banner-s&utm_source=desktop").getBytes("UTF-8");
            //将请求体写入到conn的输出流中
            os.write(requestBody);
            //记得调用输出流的flush方法
            os.flush();
            //关闭输出流
            os.close();
            //当调用getInputStream方法时才真正将请求体数据上传至服务器
            InputStream is = conn.getInputStream();
        }
    }


# 3. URL的创建

    public URL(String spec) throws MalformedURLException {
        this(null, spec);
    }

    public URL(URL context, String spec) throws MalformedURLException {
        this(context, spec, null);
    }

    public URL(URL context, String spec, URLStreamHandler handler)
        throws MalformedURLException
    {
        String original = spec;
        int i, limit, c;
        int start = 0;
        String newProtocol = null;
        boolean aRef=false;
        boolean isRelative = false;

        // Check for permission to specify a handler
		.......................

        try {
			// 获取请求地址长度
            limit = spec.length();
			// 移除俩边的空格
            while ((limit > 0) && (spec.charAt(limit - 1) <= ' ')) {
                limit--;        //eliminate trailing whitespace
            }
            while ((start < limit) && (spec.charAt(start) <= ' ')) {
                start++;        // eliminate leading whitespace
            }
			// 判断spec头四位字符与"url:"是否相同
            if (spec.regionMatches(true, start, "url:", 0, 4)) {
                start += 4;
            }
			// 判断 地址是否以'#'字符开头,如果是 则就是一个ref
            if (start < spec.length() && spec.charAt(start) == '#') {
                aRef=true;
            }
			// 对地址进行调整
            for (i = start ; !aRef && (i < limit) &&
                     ((c = spec.charAt(i)) != '/') ; i++) {
				// 仅保留 protocol部分
                if (c == ':') {
                    String s = spec.substring(start, i).toLowerCase();
                    if (isValidProtocol(s)) {
                        newProtocol = s;
                        start = i + 1;
                    }
                    break;
                }
            }

            protocol = newProtocol;
			// 如果构造函数中传入了一个URL,就会从这个URL中获取一些信息
            if ((context != null) && ((newProtocol == null) ||
                            newProtocol.equalsIgnoreCase(context.protocol))) {
				...................
            }

            if (protocol == null) {
                throw new MalformedURLException("no protocol: "+original);
            }

            // 获取UrlStreamHandler
            if (handler == null &&
                (handler = getURLStreamHandler(protocol)) == null) {
                throw new MalformedURLException("unknown protocol: "+protocol);
            }

            this.handler = handler;

            i = spec.indexOf('#', start);
            if (i >= 0) {
                ref = spec.substring(i + 1, limit);
                limit = i;
            }


            if (isRelative && start == limit) {
                query = context.query;
                if (ref == null) {
                    ref = context.ref;
                }
            }
			// 解析URl
            handler.parseURL(this, spec, start, limit);

        } catch(MalformedURLException e) {
        } catch(Exception e) {
        }
    }

- 主要做了三件事

	1. 解析出URL字符串中的协议(`protocol`),保存到URL类的成员变量`protocol`

	2. 通过`getURLStreamHandler()`方法获取处理`protocol`协议对应的`URLStreamHandler`

	3. 利用`URLStreamHandler`的`parseURL()`方法解析URL字符串

## 3.1 URL.getURLStreamHandler()

    static URLStreamHandler getURLStreamHandler(String protocol) {
		// 从缓存中获取
        URLStreamHandler handler = handlers.get(protocol);
        if (handler == null) {

            boolean checkedWithFactory = false;

            // 借助工厂类生成 handler 是优先级最高的
            if (factory != null) {
                handler = factory.createURLStreamHandler(protocol);
                checkedWithFactory = true;
            }

			// 尝试使用 java的方式获取protocol handler
            if (handler == null) {
                final String packagePrefixList = System.getProperty(protocolPathProp,"");
                StringTokenizer packagePrefixIter = new StringTokenizer(packagePrefixList, "|");

                while (handler == null &&
                       packagePrefixIter.hasMoreTokens()) {

                    String packagePrefix = packagePrefixIter.nextToken().trim();
                    try {
                        String clsName = packagePrefix + "." + protocol +
                          ".Handler";
                        Class<?> cls = null;
                        try {
                            ClassLoader cl = ClassLoader.getSystemClassLoader();
                            cls = Class.forName(clsName, true, cl);
                        } catch (ClassNotFoundException e) {
                            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
                            if (contextLoader != null) {
                                cls = Class.forName(clsName, true, contextLoader);
                            }
                        }
                        if (cls != null) {
                            handler  =
                              (URLStreamHandler)cls.newInstance();
                        }
                    } catch (ReflectiveOperationException ignored) {
                    }
                }
            }

			// 以Android的方式获取 protocol handler
			// 使okhttp作为默认的 http/https handler
            if (handler == null) {
                try {
				
                    if (protocol.equals("file")) {
                        handler = new sun.net.www.protocol.file.Handler();
                    } else if (protocol.equals("ftp")) {
                        handler = new sun.net.www.protocol.ftp.Handler();
                    } else if (protocol.equals("jar")) {
                        handler = new sun.net.www.protocol.jar.Handler();
                    } else if (protocol.equals("http")) {
                        handler = (URLStreamHandler)Class.
                            forName("com.android.okhttp.HttpHandler").newInstance();
                    } else if (protocol.equals("https")) {
                        handler = (URLStreamHandler)Class.
                            forName("com.android.okhttp.HttpsHandler").newInstance();
                    }
                } catch (Exception e) {
                }
            }

            synchronized (streamHandlerLock) {
                URLStreamHandler handler2 = null;
				// 再次检查,防止其他线程在上次检查之后创建了handler
                handler2 = handlers.get(protocol);
                if (handler2 != null) {
                    return handler2;
                }
				// 再次检查,防止其他线程在上次检查之后传入了factory
                if (!checkedWithFactory && factory != null) {
                    handler2 = factory.createURLStreamHandler(protocol);
                }
                if (handler2 != null) {
					// 工厂类创建的handler 优先级最高!!!
                    handler = handler2;
                }
                // 缓存
                if (handler != null) {
                    handlers.put(protocol, handler);
                }
            }
        }
        return handler;
    }

- 反射的参数中`HttpHandler`的包名是`com.android.okhttp`,**但是源码中的`HttpHandler`的包名是`com.squareup.okhttp`,这是因为android对其名称进行了修改**

		rule com.squareup.** com.android.@1
		rule okio.** com.android.okhttp.okio.@1

- **Android 4.4 开始使用`OKHttp`处理`Http`协议**

## 3.2 URLStreamHandler.parseURL()

将URL字符串的各个部分解析出来,然后用这些值来初始化当前URL对象

# 4. HttpUrlConnection的创建

**具体来说是`HttpURLConnectionImpl`或`HttpsURLConnectionImpl`的创建**

## 4.1 HttpHandler.openConnection()

    @Override 
	protected URLConnection openConnection(URL url) throws IOException {
        return newOkUrlFactory(null /* proxy */).open(url);
    }

    @Override 
	protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        if (url == null || proxy == null) {
            throw new IllegalArgumentException("url == null || proxy == null");
        }
        return newOkUrlFactory(proxy).open(url);
    }

## 4.2 HttpHandler.newOkUrlFactory()

    protected OkUrlFactory newOkUrlFactory(Proxy proxy) {
        OkUrlFactory okUrlFactory = createHttpOkUrlFactory(proxy);
		//对于通过java.net.URL创建的HttpURLConnections，Android使用一个连接池，该连接池在默认网络更改时知道，以便在默认网络更改时不会重新使用池化连接。
        okUrlFactory.client().setConnectionPool(configAwareConnectionPool.get());
        return okUrlFactory;
    }

### 4.2.1 HttpHandler.ConfigAwareConnectionPool

	private final ConfigAwareConnectionPool configAwareConnectionPool =
            ConfigAwareConnectionPool.getInstance();

### 4.2.2 ConfigAwareConnectionPool.get()

	  /**
	   * Returns the current {@link ConnectionPool} to use.
	   */
	  public synchronized ConnectionPool get() {
	    if (connectionPool == null) {
	      if (!networkEventListenerRegistered) {
	        networkEventDispatcher.addListener(new NetworkEventListener() {
	          @Override
	          public void onNetworkConfigurationChanged() {
	            synchronized (ConfigAwareConnectionPool.this) {
				  // 如果网络配置发生改变,那就将connectionPool 置空以确保下次不再重复使用,而是重新生成
	              connectionPool = null;
	            }
	          }
	        });
	        networkEventListenerRegistered = true;
	      }
	      connectionPool = new ConnectionPool(
	          CONNECTION_POOL_MAX_IDLE_CONNECTIONS, CONNECTION_POOL_KEEP_ALIVE_DURATION_MS);
	    }
	    return connectionPool;
	  }

- `CONNECTION_POOL_MAX_IDLE_CONNECTIONS`和`CONNECTION_POOL_KEEP_ALIVE_DURATION_MS`作为默认值会优先从系统配置中取值

### 4.2.3 ConnectionPool的构造函数

	  public ConnectionPool(int maxIdleConnections, long keepAliveDurationMs) {
	    this(maxIdleConnections, keepAliveDurationMs, TimeUnit.MILLISECONDS);
	  }

	  public ConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
	    this.maxIdleConnections = maxIdleConnections;
	    this.keepAliveDurationNs = timeUnit.toNanos(keepAliveDuration);
	    // Put a floor on the keep alive duration, otherwise cleanup will spin loop.
	    if (keepAliveDuration <= 0) {
	      throw new IllegalArgumentException("keepAliveDuration <= 0: " + keepAliveDuration);
	    }
	  }

- `maxIdleConnections`:`ConnectionPool`中空闲TCP连接的最大数量.

- `keepAliveDuration`:`ConnectionPool`中TCP连接最长的空闲时长.


## 4.3 HttpHandler.createHttpOkUrlFactory()

    // Visible for android.net.Network.
    public static OkUrlFactory createHttpOkUrlFactory(Proxy proxy) {
        OkHttpClient client = new OkHttpClient();
        // 明确的设置超时时间为无穷,包含读写以及连接
        client.setConnectTimeout(0, TimeUnit.MILLISECONDS);
        client.setReadTimeout(0, TimeUnit.MILLISECONDS);
        client.setWriteTimeout(0, TimeUnit.MILLISECONDS);
        // Set the default (same protocol) redirect behavior. The default can be overridden for
        // each instance using HttpURLConnection.setInstanceFollowRedirects().
        client.setFollowRedirects(HttpURLConnection.getFollowRedirects());
        // Do not permit http -> https and https -> http redirects.
        client.setFollowSslRedirects(false);

		// 允许明文传输,仅针对http
        client.setConnectionSpecs(CLEARTEXT_ONLY);
        // When we do not set the Proxy explicitly OkHttp picks up a ProxySelector using
        // ProxySelector.getDefault().
        if (proxy != null) {
            client.setProxy(proxy);
        }
        // OkHttp requires that we explicitly set the response cache.
        OkUrlFactory okUrlFactory = new OkUrlFactory(client);
        // 使用已经创建的NetworkSecurityPolicy 哪些请求能够通过http
        okUrlFactory.setUrlFilter(CLEARTEXT_FILTER);
        ResponseCache responseCache = ResponseCache.getDefault();
        if (responseCache != null) {
            AndroidInternal.setResponseCache(okUrlFactory, responseCache);
        }
        return okUrlFactory;
    }

- 创建一个`OkHttpClient`,并将其作为参数创建一个`OkUrlFactory`


### 4.3.1 HttpHandler.CLEARTEXT_ONLY

    private final static List<ConnectionSpec> CLEARTEXT_ONLY =
        Collections.singletonList(ConnectionSpec.CLEARTEXT);


	/** Unencrypted, unauthenticated connections for {@code http:} URLs. */
	public static final ConnectionSpec CLEARTEXT = new Builder(false).build();

- 代表未加密,未经过身份验证的连接.即不需要使用`TLS`,直接明文传输

### 4.3.2 HttpHandler.CLEARTEXT_FILTER

    private static final CleartextURLFilter CLEARTEXT_FILTER = new CleartextURLFilter();

    private static final class CleartextURLFilter implements URLFilter {
        @Override
        public void checkURLPermitted(URL url) throws IOException {
            String host = url.getHost();
            if (!NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(host)) {
                throw new IOException("Cleartext HTTP traffic to " + host + " not permitted");
            }
        }
    }

- `CleartextURLFilter`用来判断是否能够与指定URL的host 进行明文通信

## 4.4 OkUrlFactory.open()

	public HttpURLConnection open(URL url) {
		return open(url, client.getProxy());
	}
  
	HttpURLConnection open(URL url, Proxy proxy) {
		String protocol = url.getProtocol();
    	OkHttpClient copy = client.copyWithDefaults();
    	copy.setProxy(proxy);
    	if (protocol.equals("http")) {
			return new HttpURLConnectionImpl(url, copy, urlFilter);
    	}

		if (protocol.equals("https")) {
			return new HttpsURLConnectionImpl(url, copy, urlFilter);
		}    		

		throw new IllegalArgumentException("Unexpected protocol: " + protocol);
	}

- 根据协议创建`HttpUrlConnectionImpl`或`HttpsUrlConnectionImpl`

# 5. HttpsUrlConnection的创建

`HttpsHandler` 重写了`HttpHandler`的`newOkUrlFactory()`方法


## 5.1 HttpsHandler.newOkUrlFactory()

    @Override
    protected OkUrlFactory newOkUrlFactory(Proxy proxy) {
        OkUrlFactory okUrlFactory = createHttpsOkUrlFactory(proxy);

        okUrlFactory.client().setConnectionPool(configAwareConnectionPool.get());
        return okUrlFactory;
    }

### 5.1.1 HttpsHandler.createHttpsOkUrlFactory()

    public static OkUrlFactory createHttpsOkUrlFactory(Proxy proxy) {

		// 创建一个 进行Https通信的Client,只需要在OkHttpClient的基础上添加一些额外配置
        OkUrlFactory okUrlFactory = HttpHandler.createHttpOkUrlFactory(proxy);
		// 允许所有的HTTPS 请求
        okUrlFactory.setUrlFilter(null);
        OkHttpClient okHttpClient = okUrlFactory.client();

        // Only enable HTTP/1.1 (implies HTTP/1.0). Disable SPDY / HTTP/2.0.
        okHttpClient.setProtocols(HTTP_1_1_ONLY);
        okHttpClient.setConnectionSpecs(Collections.singletonList(TLS_CONNECTION_SPEC));

        // Android support certificate pinning via NetworkSecurityConfig so there is no need to
        // also expose OkHttp's mechanism. The OkHttpClient underlying https HttpsURLConnections
        // in Android should therefore always use the default certificate pinner, whose set of
        // {@code hostNamesToPin} is empty.
        okHttpClient.setCertificatePinner(CertificatePinner.DEFAULT);

        // OkHttp does not automatically honor the system-wide HostnameVerifier set withHttpsURLConnection.setDefaultHostnameVerifier().
        okUrlFactory.client().setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());

        // OkHttp does not automatically honor the system-wide SSLSocketFactory set with
        // HttpsURLConnection.setDefaultSSLSocketFactory().
        // See https://github.com/square/okhttp/issues/184 for details.
        okHttpClient.setSslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
        return okUrlFactory;
    }

- `ConnectionSpecs`:值为`TLS_CONNECTION_SPEC`,代表需要支持TLS,即密文传输

- `Protocols`:表示TLS握手阶段时，通过ALPN协商应用层协议时客户端ClientHello携带的应用层协议列表

- `SSLSocketFactory`