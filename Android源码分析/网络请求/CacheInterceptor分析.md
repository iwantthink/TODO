# CacheInterceptor分析

[okhttp源码分析（三）-CacheInterceptor过滤器](https://www.jianshu.com/p/bfb13eb3a425)

# 1. 简介

主要作用就是对缓存的操作，例如生成缓存，更新缓存等

## 1.1 拦截器链
在拦截器链中，`BridgeInteceptor `会调用`CacheInterceptor`

	// BridgeInteceptor
	public Response intercept(RealInterceptorChain chain) {
	    // 当前拦截器处理
	    // 交给下一个拦截器去操作
	    Response networkResponse = chain.proceed(requestBuilder.build());
	    // 当前拦截器处理
	    return response;
	}

    public Response proceed(Request request) throws IOException {
    	 // 之类的 streamAllocation,httpCodec,connection 使用拦截链中的值
    	 // 除了streamAllocation 在 RAFInterceptor中被创建，其余仍未被创建
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
	
# 2. CacheInterceptor.intercept()

	 /**
	  * 提供来自缓存的请求并将响应写入缓存
	  *
	  */
    public Response intercept(Chain chain) throws IOException {
        // Cache来自用户设置,即通过OKHttpClient设置
        // 默认Cache为null，不为空时尝试通过request获取缓存中的Response
        Response cacheCandidate = cache != null
                ? cache.get(chain.request())
                : null;

        long now = System.currentTimeMillis();
		  // 生成一个 缓存策略类，用于判断如何使用缓存，存在四种情况
		  // cacheCandidate 如果为空，那么其对应的cacheResponse必定为空
        CacheStrategy strategy = new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
        // 需要网络去执行的请求，如果为null则表示当前请求不需要使用网络
        Request networkRequest = strategy.networkRequest;
        // 用来校验或者返回的缓存响应，如果为null则表示当前请求不使用缓存
        Response cacheResponse = strategy.cacheResponse;

		  // 
        if (cache != null) {
            cache.trackResponse(strategy);
        }

		  // 缓存的响应不为空 && 策略类表示不支持使用缓存
		  // 说明缓存无效，关闭资源
        if (cacheCandidate != null && cacheResponse == null) {
            closeQuietly(cacheCandidate.body()); 
        }

        // 禁止使用网络 && 不使用缓存 ， 构建一个表示失败的Response
        if (networkRequest == null && cacheResponse == null) {
            return new Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(504)
                    .message("Unsatisfiable Request (only-if-cached)")
                    .body(Util.EMPTY_RESPONSE)
                    .sentRequestAtMillis(-1L)
                    .receivedResponseAtMillis(System.currentTimeMillis())
                    .build();
        }

        // 不使用网络，那么直接返回缓存
        if (networkRequest == null) {
            return cacheResponse.newBuilder()
                    .cacheResponse(stripBody(cacheResponse))
                    .build();
        }
		
		  // 交给下一个拦截器去处理
        Response networkResponse = null;
        try {
              networkResponse = chain.proceed(networkRequest);
        } finally {
            // 碰到了I/O问题，关闭缓存响应的body
            if (networkResponse == null && cacheCandidate != null) {
                closeQuietly(cacheCandidate.body());
            }
        }

        // 如果获取到了最新的Response，同时缓存的Response也存在
        // 那么就根据响应码,处理缓存的Response
        if (cacheResponse != null) {
        	  // 响应码为 304 ，表示原来的内容还能使用
            if (networkResponse.code() == HTTP_NOT_MODIFIED) {
                Response response = cacheResponse.newBuilder()
                        .headers(combine(cacheResponse.headers(), networkResponse.headers()))
                        .sentRequestAtMillis(networkResponse.sentRequestAtMillis())
                        .receivedResponseAtMillis(networkResponse.receivedResponseAtMillis())
                        .cacheResponse(stripBody(cacheResponse))
                        .networkResponse(stripBody(networkResponse))
                        .build();
                networkResponse.body().close();

                cache.trackConditionalCacheHit();
                cache.update(cacheResponse, response);
                return response;
            } else {
                closeQuietly(cacheResponse.body());
            }
        }
		 // 不存在缓存响应，那么就去使用网络发起请求（前面已经过滤了不使用网络请求的情况，走到这里肯定是支持使用网络）
        Response response = networkResponse.newBuilder()
                .cacheResponse(stripBody(cacheResponse))
                .networkResponse(stripBody(networkResponse))
                .build();

		  // 缓存不为空（默认创建的OKHttpClient为空）
        if (cache != null) {
        	  // 当前响应存在body && 当前响应能够被缓存
            if (HttpHeaders.hasBody(response) && CacheStrategy.isCacheable(response, networkRequest)) {
                // 缓存Header
                CacheRequest cacheRequest = cache.put(response);
                // 缓存Body
                return cacheWritingResponse(cacheRequest, response);
            }
			  // 几种method 无法支持缓存，所以移除
			  // 目前仅支持get
            if (HttpMethod.invalidatesCache(networkRequest.method())) {
                try {
                    cache.remove(networkRequest);
                } catch (IOException ignored) {
                    // The cache cannot be written.
                }
            }
        }

        return response;
    }
    
- `InternalCache`的具体实现类就是 `CacheAdapter`    

	在`CacheInterceptor`拦截器被创建时，会将`OKHttpClient`的`Cache`传递进来
	
	
- `Request strategy.networkRequest`:表示发送给网络的请求，如果为null则表示当前请求不需要使用网络

- `Request strategy.cacheResponse `:用来校验或者返回的缓存响应，如果为null则表示当前请求不使用缓存


- `CacheStrategy`根据`networkRequest`和`cacheResponse`可以分为三种情况

	1.  `networkRequest`为空,`cacheResponse`为空 : 请求失败

	2. `networkRequest`不为空,`cacheResponse`不为空 : 使用网络，并对缓存进行更新等操作

	3. `networkRequest`为空,`cacheResponse`不为空 : 仅使用缓存

	4. `networkRequest`不为空,`cacheResponse`为空 : 使用网络，不使用缓存
        
## 2.1 InternalCache的赋值流程    
       
`CacheInterceptor`拦截器在构建时通过`OKHttpClient`获取了`InternalCache`并传入了拦截器中	

	Response getResponseWithInterceptorChain(){
	
        interceptors.add(new CacheInterceptor(client.internalCache()));
	}
        
### 2.1.1 OKHttpClient.internalCache()

    InternalCache internalCache() {
        return cache != null ? cache.internalCache : internalCache;
    }

## 2.2 `CacheStrategy$Factory`

    CacheStrategy strategy = new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
    

- `Request chain.request`: 经过`BridgeInterceptor`修改的`Request`,被添加了各种请求头

- `Response cacheCandidate`:缓存的响应，可能为空

**构造函数：**

    public Factory(long nowMillis, Request request, Response cacheResponse) {
        this.nowMillis = nowMillis;
        this.request = request;
        this.cacheResponse = cacheResponse;

        if (cacheResponse != null) {
            this.sentRequestMillis = cacheResponse.sentRequestAtMillis();
            this.receivedResponseMillis = cacheResponse.receivedResponseAtMillis();
            Headers headers = cacheResponse.headers();
            for (int i = 0, size = headers.size(); i < size; i++) {
                String fieldName = headers.name(i);
                String value = headers.value(i);
                // Date 表示报文创建的时间
                if ("Date".equalsIgnoreCase(fieldName)) {
                    servedDate = HttpDate.parse(value);
                    servedDateString = value;
                // Expires 表示响应过期的时间
                } else if ("Expires".equalsIgnoreCase(fieldName)) {
                    expires = HttpDate.parse(value);
                // Last-Modified 用来判断资源是否一致
                } else if ("Last-Modified".equalsIgnoreCase(fieldName)) {
                    lastModified = HttpDate.parse(value);
                    lastModifiedString = value;
                // ETag 表示资源的特定版本的标识符
                } else if ("ETag".equalsIgnoreCase(fieldName)) {
                    etag = value;
                // Age 表示消息对象从原始服务器中形成后，在缓存代理中存储的时长，单位 seconed
                } else if ("Age".equalsIgnoreCase(fieldName)) {
                    ageSeconds = HttpHeaders.parseSeconds(value, -1);
                }
            }
        }
    }
    
- 如果存在缓存的响应，那么就去获取缓存响应的响应头中的内容  

### 2.2.1 `CacheStrategy$Factory`.get()
    
    public CacheStrategy get() {
    	 // 根据多种条件去生成一个 CacheStrategy
        CacheStrategy candidate = getCandidate();
		  // 当前需要进行网络请求，但是请求头禁止使用网络
        if (candidate.networkRequest != null && request.cacheControl().onlyIfCached()) {
			  // 不支持网络请求，也不支持使用缓存
            return new CacheStrategy(null, null);
        }

        return candidate;
    }
        
-  返回一个`Strategy`,其包含了符合Request要求的缓存Response(可能为空)

### 2.2.2 `CacheStrategy$Factory`.getCandidate()

根据是否有缓存的响应，请求是否支持缓存，请求是否支持缓存，响应头中缓存过期信息等一系列判断，组装出一个`CacheStrategy`返回

后续就通过该`CacheStrategy`去处理


        private CacheStrategy getCandidate() {
			  // 不存在缓存
            if (cacheResponse == null) {
                return new CacheStrategy(request, null);
            }
            
			  // 如果请求是https,并且没有握手信息(handshake)
            if (request.isHttps() && cacheResponse.handshake() == null) {
                return new CacheStrategy(request, null);
            }

            // 判断该缓存的响应能否存储，正常情况下该检查是冗余的，除非持久化存储或者规则发生改变
            if (!isCacheable(cacheResponse, request)) {
                return new CacheStrategy(request, null);
            }
	
			  // 获取请求头中的对缓存处理方式，永远不为空
            CacheControl requestCaching = request.cacheControl();
            // 请求中禁止缓存 || 当请求对响应的资源有要求时（if-modified-since）
            if (requestCaching.noCache() || hasConditions(request)) {
                return new CacheStrategy(request, null);
            }
			  // 获取缓存的响应头中对缓存的处理方式.不为空
            CacheControl responseCaching = cacheResponse.cacheControl();

			  。。。。。省略时间的计算。。。。。。
			  // 响应支持缓存 && 响应没有过期
            if (!responseCaching.noCache() && ageMillis + minFreshMillis < freshMillis + maxStaleMillis) {
                Response.Builder builder = cacheResponse.newBuilder();
                if (ageMillis + minFreshMillis >= freshMillis) {
                    builder.addHeader("Warning", "110 HttpURLConnection \"Response is stale\"");
                }
                long oneDayMillis = 24 * 60 * 60 * 1000L;
                if (ageMillis > oneDayMillis && isFreshnessLifetimeHeuristic()) {
                    builder.addHeader("Warning", "113 HttpURLConnection \"Heuristic expiration\"");
                }
                return new CacheStrategy(null, builder.build());
            }

            // 判断是否有需要添加到请求头中的条件
            String conditionName;
            String conditionValue;
            if (etag != null) {
                conditionName = "If-None-Match";
                conditionValue = etag;
            } else if (lastModified != null) {
                conditionName = "If-Modified-Since";
                conditionValue = lastModifiedString;
            } else if (servedDate != null) {
                conditionName = "If-Modified-Since";
                conditionValue = servedDateString;
            } else {
            		//没有条件，返回默认的即可
                return new CacheStrategy(request, null); 
            }
			  // 这里的逻辑就是 调用 conditionalRequestHeaders.addLenient()
			  // 将conditionName和conditionValue 添加到 namesAndValues中
            Headers.Builder conditionalRequestHeaders = request.headers().newBuilder();
            Internal.instance.addLenient(conditionalRequestHeaders, conditionName, conditionValue);
			  // 新建了一个Request，传入了修改过的header
            Request conditionalRequest = request.newBuilder()
                    .headers(conditionalRequestHeaders.build())
                    .build();
            return new CacheStrategy(conditionalRequest, cacheResponse);
        }
        
- `Internal.instance`在`OkHttpClient`的静态代码块中被重新赋值。其主要用来预先定义一些操作规则，方便扩展

- 该方法主要的功能就是获取`CacheStrategy`,第一个`Request`表示当前是否需要网络请求(为null则表示不需要),第二个`Request`表示当前是否使用缓存响应(为null则表示不使用)

## 2.3 CacheInterceptor.stripBody()

    private static Response stripBody(Response response) {
        return response != null && response.body() != null
                ? response.newBuilder().body(null).build()
                : response;
    }

- 根据`resp.body`是否为空可以分为俩种情况

	1. `resp`不为空，且`resp.body`不为空 就将其body置空并返回

	2. 否则原样返回

# 3. 缓存介绍

`OKHttp`中有俩个跟缓存相关的类，`Cache`,`InternalCache`

## 3.1 InternalCache

`InternalCache `是一个接口，用来表示`OkHttp`内部使用的缓存标准. 其存在2个实现类

1. `CacheAdapter`

2. `Cache`中的成员变量`internalCache`,在`Cache`类被创建时，这个`InternalCache`也会被初始化

## 3.2 Cache 

缓存Http或Https的响应 到文件系统中，重用这些响应以节省时间和带宽


# 4. Cache

## 4.1 Cache的构造函数

    public Cache(File directory, long maxSize) {
        this(directory, maxSize, FileSystem.SYSTEM);
    }

    Cache(File directory, long maxSize, FileSystem fileSystem) {
        this.cache = DiskLruCache.create(fileSystem, directory, VERSION, ENTRY_COUNT, maxSize);
    }
       
    
- 指定缓存保存的文件地址，缓存大小，以及OKHttp自定义的`FileSystem`

	- 自定义的`FileSystem`与Java 7 中添加的`java.nio.file.FileSystem`相比，前者缺少文件监视，元数据，权限和磁盘空间信息等功能，但是前者比后者更加的易于实现，且适用于各种版本的Java和Android

- 创建了一个`DiskLruCache`进行缓存操作
	
### 4.1.1 FileSystem

用来访问分层数据存储器上的读写文件。正常情况下要使用`FileSystem`提供的默认实现类`SYSTEM`。可以实现其接口，用于注入错误(用于测试)或者转换存储的数据(例如加密)

### 4.1.2 `FileSystem$SYSTEM`


    FileSystem SYSTEM = new FileSystem() {
        @Override
        public Source source(File file) throws FileNotFoundException {
        	  // 将file转换成FileInputStream,并用Source类进行包装
        	  // 用于读取file至内存
            return Okio.source(file);
        }

        @Override
        public Sink sink(File file) throws FileNotFoundException {
            try {
            	   // 将file转换成FileOutputStream,并用Sink类进行包装
            	   // 用于输出内容至file
                return Okio.sink(file);
            } catch (FileNotFoundException e) {
                // Maybe the parent directory doesn't exist? Try creating it first.
                file.getParentFile().mkdirs();
                return Okio.sink(file);
            }
        }

        @Override
        public Sink appendingSink(File file) throws FileNotFoundException {
            try {
            	   // 返回一个添加内容到file尾部的 Sink
                return Okio.appendingSink(file);
            } catch (FileNotFoundException e) {
                // Maybe the parent directory doesn't exist? Try creating it first.
                file.getParentFile().mkdirs();
                return Okio.appendingSink(file);
            }
        }

        @Override
        public void delete(File file) throws IOException {
			  // 删除文件
			  // 如果删除失败，确保是因为文件不存在
            if (!file.delete() && file.exists()) {
                throw new IOException("failed to delete " + file);
            }
        }

        @Override
        public boolean exists(File file) {
        	  // 判断文件是否存在
            return file.exists();
        }

        @Override
        public long size(File file) {
        	  // 返回文件大小
            return file.length();
        }

        @Override
        public void rename(File from, File to) throws IOException {
           // 对文件进行重命名
            delete(to);
            if (!from.renameTo(to)) {
                throw new IOException("failed to rename " + from + " to " + to);
            }
        }

        @Override
        public void deleteContents(File directory) throws IOException {
        	  // 删除文件夹内容，保留文件夹
            File[] files = directory.listFiles();
            if (files == null) {
                throw new IOException("not a readable directory: " + directory);
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteContents(file);
                }
                if (!file.delete()) {
                    throw new IOException("failed to delete " + file);
                }
            }
        }
    };


### 4.1.3 DiskLruCache

`DiskLruCache`在文件系统上使用有限的空间进行缓存操作，每个缓存的条目都存在一个字符串key和固定数量的值，每个密钥必须与正则表达式<strong> [a-z0-9 _-] {1,64} </ strong>匹配

值如果是字节序列，那么可以作为流或文件来访问，其长度必须介于`0-Integet.MAX_VALUE`


## 4.2 Cache.get()

    Response get(Request request) {
    	 // 利用url生成key
        String key = key(request.url());
        DiskLruCache.Snapshot snapshot;
        Entry entry;
        try {
        	  // 通过DisLruCache 获取缓存
            snapshot = cache.get(key);
            // 没有缓存，直接返回null
            if (snapshot == null) {
                return null;
            }
        } catch (IOException e) {
            // Give up because the cache cannot be read.
            return null;
        }

        try {
        	  //创建一个Entry,这里其实传入的是CleanFiles数组的第一个（ENTRY_METADATA = 0）得到是头信息,也就是key.0
        	  //利用缓存中的Source 构建一个Entry
            entry = new Entry(snapshot.getSource(ENTRY_METADATA));
        } catch (IOException e) {
            Util.closeQuietly(snapshot);
            return null;
        }
		  // 利用缓存的Header 信息 构建一个 Response
        Response response = entry.response(snapshot);
		 // 判断缓存中的Response与当前Request是否匹配
        if (!entry.matches(request, response)) {
            Util.closeQuietly(response.body());
            return null;
        }

        return response;
    }
    
- `DiskLruCache`相关的分析查看[DiskLruCache分析.md]()
    
### 4.2.1 DisLruCache.get()

    public synchronized Snapshot get(String key) throws IOException {
    	  // 总结来说就是对journalFile文件的操作，有则删除无用冗余的信息
    	  // 构建新文件，没有则创建一个新的
        initialize();
		  //判断是否关闭，如果缓存损坏了，会被关闭
        checkNotClosed();
        //检查key是否满足格式要求，正则表达式
        validateKey(key);
        //获取key对应的entry
        Entry entry = lruEntries.get(key);
        if (entry == null || !entry.readable) return null;
		  //获取entry里面的snapshot的值
        Snapshot snapshot = entry.snapshot();
        if (snapshot == null) return null;
		  //snapshot存在，则计数器+1
        redundantOpCount++;
        // 将内容写入文档中
        journalWriter.writeUtf8(READ).writeByte(' ').writeUtf8(key).writeByte('\n');
        //是否达到清理的条件
        if (journalRebuildRequired()) {
            executor.execute(cleanupRunnable);
        }

        return snapshot;
    }
    

## 4.3 Cache.put()

    CacheRequest put(Response response) {
        String requestMethod = response.request().method();
		  // OKHttp只能缓存GET请求，从个别Method中判断
        if (HttpMethod.invalidatesCache(response.request().method())) {
            try {
                remove(response.request());
            } catch (IOException ignored) {
            }
            return null;
        }
        // 仅支持GET请求进行缓存
        // OKHttp认为其余对其他类型的请求进行缓存性价比不高
        // 删除这里，可以对其余类型的请求进行缓存（其实还有另外一处）
        if (!requestMethod.equals("GET")) {
            return null;
        }
		  // 如果 响应头中含有 asterisk。则不支持缓存
        if (HttpHeaders.hasVaryAll(response)) {
            return null;
        }

        Entry entry = new Entry(response);
        DiskLruCache.Editor editor = null;
        try {
        	  // DiskLruCache 返回一个Editor 进行操作
            editor = cache.edit(key(response.request().url()));
            if (editor == null) {
                return null;
            }
            // 在这里写入header相关内容
            entry.writeTo(editor);
            // 在这里写入body相关内容
            return new CacheRequestImpl(editor);
        } catch (IOException e) {
            abortQuietly(editor);
            return null;
        }
    }
    
    
### 4.3.1 Entry.writeTo()

        public void writeTo(DiskLruCache.Editor editor) throws IOException {
        	  // 获取一个输出的Sink，用BufferedSink进行包装
        	  // 这里写入的是Header信息，所以使用ENTRY_METADATA
            BufferedSink sink = Okio.buffer(editor.newSink(ENTRY_METADATA));

            sink.writeUtf8(url)
                    .writeByte('\n');
            sink.writeUtf8(requestMethod)
                    .writeByte('\n');
            sink.writeDecimalLong(varyHeaders.size())
                    .writeByte('\n');
            for (int i = 0, size = varyHeaders.size(); i < size; i++) {
                sink.writeUtf8(varyHeaders.name(i))
                        .writeUtf8(": ")
                        .writeUtf8(varyHeaders.value(i))
                        .writeByte('\n');
            }

            sink.writeUtf8(new StatusLine(protocol, code, message).toString())
                    .writeByte('\n');
            sink.writeDecimalLong(responseHeaders.size() + 2)
                    .writeByte('\n');
            for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                sink.writeUtf8(responseHeaders.name(i))
                        .writeUtf8(": ")
                        .writeUtf8(responseHeaders.value(i))
                        .writeByte('\n');
            }
            sink.writeUtf8(SENT_MILLIS)
                    .writeUtf8(": ")
                    .writeDecimalLong(sentRequestMillis)
                    .writeByte('\n');
            sink.writeUtf8(RECEIVED_MILLIS)
                    .writeUtf8(": ")
                    .writeDecimalLong(receivedResponseMillis)
                    .writeByte('\n');

            if (isHttps()) {
                sink.writeByte('\n');
                sink.writeUtf8(handshake.cipherSuite().javaName())
                        .writeByte('\n');
                writeCertList(sink, handshake.peerCertificates());
                writeCertList(sink, handshake.localCertificates());
                sink.writeUtf8(handshake.tlsVersion().javaName()).writeByte('\n');
            }
            sink.close();
        }    

# 5. InternalCache

## 5.1 Cache的成员变量

    final InternalCache internalCache = new InternalCache() {
        @Override
        public @Nullable
        Response get(Request request) throws IOException {
            return Cache.this.get(request);
        }

        @Override
        public @Nullable
        CacheRequest put(Response response) throws IOException {
            return Cache.this.put(response);
        }

        @Override
        public void remove(Request request) throws IOException {
            Cache.this.remove(request);
        }

        @Override
        public void update(Response cached, Response network) {
            Cache.this.update(cached, network);
        }

        @Override
        public void trackConditionalCacheHit() {
            Cache.this.trackConditionalCacheHit();
        }

        @Override
        public void trackResponse(CacheStrategy cacheStrategy) {
            Cache.this.trackResponse(cacheStrategy);
        }
    };
    
    
## 5.2 CacheAdapter

详见[CacheAdapter分析.md]()


    
# 6. OkHttp中InternalCache的使用

`CacheInterceptor`有一个成员变量`InternalCache cache`,其在`RealCall.getResponseWithInterceptorChain()`方法中通过`CacheInterceptor`的构造函数传入，默认情况下为空

	Response getResponseWithInterceptorChain(){
		interceptors.add(new CacheInterceptor(client.internalCache()));
	}

    InternalCache internalCache() {
        return cache != null ? cache.internalCache : internalCache;
    }
    
- `cache`和`internalCache` 通过`OKHttpClient$Builder`进行构建，俩者相互排斥， 
    
        void setInternalCache(@Nullable InternalCache internalCache) {
            this.internalCache = internalCache;
            this.cache = null;
        }

        public Builder cache(@Nullable Cache cache) {
            this.cache = cache;
            this.internalCache = null;
            return this;
        }

# 7. Http相关

## 7.1 If-Modified-Since 消息头


是一个条件式请求首部，**服务器只在所请求的资源在给定的日期时间之后对内容进行过修改的情况下才会将资源返回**，状态码为 200  。如果请求的资源从那时起未经修改，那么返回一个不带有消息主体的  304  响应，而在 `Last-Modified `首部中会带有上次修改时间。 不同于  `If-Unmodified-Since`,` If-Modified-Since `只可以用在 GET 或 HEAD 请求中。

当与` If-None-Match `一同出现时，它（`If-Modified-Since`）会被忽略掉，除非服务器不支持 `If-None-Match`。

## 7.2 If-None-Match 消息头

`If-None-Match` 是一个条件式请求首部。对于 GET 和 HEAD 请求方法来说，当且仅当服务器上没有任何资源的 ETag 属性值与这个首部中列出的相匹配的时候，服务器端会才返回所请求的资源，响应码为  200  。对于其他方法来说，当且仅当最终确认没有已存在的资源的  ETag 属性值与这个首部中所列出的相匹配的时候，才会对请求进行相应的处理。

	If-None-Match: <etag_value>
	If-None-Match: <etag_value>, <etag_value>, …
	If-None-Match: *

## 7.3 Expires 响应头

包含一个日期/时间，代表响应过期的时间

如果在Cache-Control响应头设置了 "max-age" 或者 "s-max-age" 指令，那么 Expires 头会被忽略。

## 7.4 Cache-Control 消息头

用于在http 请求和响应中，通过指定指令来实现缓存机制。缓存指令是单向的, 这意味着在请求设置的指令，在响应中不一定包含相同的指令



