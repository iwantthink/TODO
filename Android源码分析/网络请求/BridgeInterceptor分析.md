# BridgeInterceptor分析

[okhttp源码分析（三）-CacheInterceptor过滤器](https://www.jianshu.com/p/bfb13eb3a425)

[MIME类型 - MDN](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Basics_of_HTTP/MIME_types)

# 1. 简介

从应用程序代码到网络代码的桥梁,负责把用户构造的请求转换为发送到服务器的请求（配置请求头等）,同时把服务器返回的响应转换为对用户友好的响应信息

- 主要负责设置内容长度，编码方式，还有压缩方式等，主要是添加头部的作用。


## 1.1 拦截器链

在拦截器链中，`RetryAndFollowUpInterceptor`会调用`BridgeInteceptor`

	// RAFInterceptor 
    public Response intercept(RealInterceptorChain chain) {
    	// 当前拦截器处理
       // 交给下一个拦截器去操作
		response = realChain.proceed(request, streamAllocation, null, null);
		// 当前拦截器处理
		return response;
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
	
- `HttpCodec`和`RealConnection`未创建，`StreamAllocation`已经在`RAFInterceptor`中创建	


# 2. BridgeInterceptor.intercept()

	 /**
	  * 主要就是添加请求头，设置内容长度，编码方式，压缩方式等
	  *
	  */
    public Response intercept(Chain chain) throws IOException {
    	 // 获取原始的Request
        Request userRequest = chain.request();
        // 利用当前Request创建一个Builder，该Builder包含了原始Request的所有内容
        Request.Builder requestBuilder = userRequest.newBuilder();

		 // RequestBody 是通过Request.post()方法传入,表示请求体
        RequestBody body = userRequest.body();
        if (body != null) {
        	  // 设置MIME
            MediaType contentType = body.contentType();
            if (contentType != null) {
                requestBuilder.header("Content-Type", contentType.toString());
            }
			  // 设置请求体长度（字节数）
			  // -1 表示未知长度
            long contentLength = body.contentLength();
            if (contentLength != -1) {
                requestBuilder.header("Content-Length", Long.toString(contentLength));
                requestBuilder.removeHeader("Transfer-Encoding");
            } else {
                requestBuilder.header("Transfer-Encoding", "chunked");
                requestBuilder.removeHeader("Content-Length");
            }
        }
		 // 设置host
        if (userRequest.header("Host") == null) {
            requestBuilder.header("Host", hostHeader(userRequest.url(), false));
        }
		 
		 // 设置连接为持久的，使得对同一个服务器的请求可复用连接(在一段时间内对连接进行保留，不是永久的)
        if (userRequest.header("Connection") == null) {
            requestBuilder.header("Connection", "Keep-Alive");
        }

        // 如果添加了 "Accept-Encoding: gzip"消息头，那么就需要负责使用gzip对传输流进行解析
        // 该标记 表示客户端支持gzip
        boolean transparentGzip = false;
        if (userRequest.header("Accept-Encoding") == null && userRequest.header("Range") == null) {
            transparentGzip = true;
            requestBuilder.header("Accept-Encoding", "gzip");
        }
        // 添加Cookie 消息头，值类似“a=b; c=d”
		 // 默认的CookieJar 是一个空实现
        List<Cookie> cookies = cookieJar.loadForRequest(userRequest.url());
        if (!cookies.isEmpty()) {
            requestBuilder.header("Cookie", cookieHeader(cookies));
        }
		  // 添加User-Agent消息头，标识设备
        if (userRequest.header("User-Agent") == null) {
            requestBuilder.header("User-Agent", Version.userAgent());
        }
		  // 将请求交给了下一个拦截器
        Response networkResponse = chain.proceed(requestBuilder.build());
		  // 处理Cookie
        HttpHeaders.receiveHeaders(cookieJar, userRequest.url(), networkResponse.headers());
		  // 构建一个Response.Builder对响应进行处理
		  // 会将处理过后的Request也添加进去
        Response.Builder responseBuilder = networkResponse.newBuilder()
                .request(userRequest);
		  // 客户端支持gzip && 响应使用了gzip && 响应体存在
        if (transparentGzip
                && "gzip".equalsIgnoreCase(networkResponse.header("Content-Encoding"))
                && HttpHeaders.hasBody(networkResponse)) {
            // 包装source
            GzipSource responseBody = new GzipSource(networkResponse.body().source());
            Headers strippedHeaders = networkResponse.headers().newBuilder()
                    .removeAll("Content-Encoding")
                    .removeAll("Content-Length")
                    .build();
            // 更新响应头
            responseBuilder.headers(strippedHeaders);
            // 将包装后的source 塞进一个新的body
            String contentType = networkResponse.header("Content-Type");
            responseBuilder.body(new RealResponseBody(contentType, -1L, Okio.buffer(responseBody)));
        }

        return responseBuilder.build();
    }
    
    
    
## 2.1 HttpHeaders.receiveHeaders()

    public static void receiveHeaders(CookieJar cookieJar, HttpUrl url, Headers headers) {
        if (cookieJar == CookieJar.NO_COOKIES) return;
		 // 解析响应头中的 Cookie内容
        List<Cookie> cookies = Cookie.parseAll(url, headers);
        if (cookies.isEmpty()) return;
		 // 调用处理Cooki的回调
        cookieJar.saveFromResponse(url, cookies);
    } 
    
- 默认的`CookieJar`就是 `CookieJar.NO_COOKIES `类型

- 主要逻辑就是解析响应头中的`Set-Cookie`,然后将`Cookie`传入回调，按照回调的逻辑去存储

### 2.1.1 Cookie.parseAll()

    public static List<Cookie> parseAll(HttpUrl url, Headers headers) {
        List<String> cookieStrings = headers.values("Set-Cookie");
        List<Cookie> cookies = null;

        for (int i = 0, size = cookieStrings.size(); i < size; i++) {
        	  // 解析单个
            Cookie cookie = Cookie.parse(url, cookieStrings.get(i));
            if (cookie == null) continue;
            if (cookies == null) cookies = new ArrayList<>();
            cookies.add(cookie);
        }

        return cookies != null
                ? Collections.unmodifiableList(cookies)
                : Collections.emptyList();
    }
    
- 解析响应中的`Set-Cookie`响应头  
    
    
# 3. Http知识    
    
## 3.1 Content-Type 消息头    
    
用于指示资源的MIME类型 `media type `    
    
在响应中，`Content-Type`响应头告诉客户端实际返回的内容的内容类型。

- 浏览器会在某些情况下进行MIME查找，并不一定遵循此标题的值; 为了防止这种行为，可以将标题 X-Content-Type-Options 设置为 nosniff。

- 在请求中 (如POST 或 PUT)，**客户端告诉服务器实际发送的数据类型**。

- 包含三种指令，使用`';'`分隔

	1. `media-type`:资源或数据的 MIME type

	2. `charset`:字符编码标准

	3. `boundary`: 对于多部分实体，boundary 是必需的，其包括来自一组字符的1到70个字符，已知通过电子邮件网关是非常健壮的，而不是以空白结尾。它用于封装消息的多个部分的边界
    
    
### 3.1.1 MIME

多用途Internet邮件扩展（MIME）类型 是一种标准化的方式来表示文档的性质和格式

- 浏览器通常使用MIME类型（而不是文件扩展名）来确定如何处理文档；**因此服务器设置正确以将正确的MIME类型附加到响应对象的头部是非常重要的**


**语法：**

	type/subtype

- MIME的组成结构非常简单；由类型与子类型两个字符串中间用`'/'`分隔而组成。不允许空格存在。

	`type` 表示可以被分多个子类的独立类别。`subtype`表示细分后的每个类型。

- MIME类型对大小写不敏感，但是传统写法都是小写。


## 3.2 Transfer-Encoding 消息头

`Transfer-Encoding` 消息头 指明了将 entity 安全传递给用户所采用的编码形式。

**语法：**

	Transfer-Encoding: chunked
	Transfer-Encoding: compress
	Transfer-Encoding: deflate
	Transfer-Encoding: gzip
	Transfer-Encoding: identity
	
	// Several values can be listed, separated by a comma
	Transfer-Encoding: gzip, chunked

**`chunked`指令：**

数据以一系列分块的形式进行发送。 Content-Length 首部在这种情况下不被发送。
	
在每一个分块的开头需要添加当前分块的长度，以十六进制的形式表示，后面紧跟着 `'\r\n'` ，之后是分块本身，后面也是`'\r\n'` 。
	
终止块是一个常规的分块，不同之处在于其长度为0。终止块后面是一个挂载（trailer），由一系列（或者为空）的实体消息首部构成

- 示例：

		HTTP/1.1 200 OK 
		Content-Type: text/plain 
		Transfer-Encoding: chunked
		
		7\r\n
		Mozilla\r\n 
		9\r\n
		Developer\r\n
		7\r\n
		Network\r\n
		0\r\n 
		\r\n

- 分块编码主要应用于如下场景，即要传输大量的数据，但是在请求在没有被处理完之前响应的长度是无法获得的。例如，当需要用从数据库中查询获得的数据生成一个大的HTML表格的时候，或者需要传输大量的图片的时候。

## 3.3 Connection 消息头

决定当前的事务完成后，是否会关闭网络连接。如果该值是`“keep-alive”`，网络连接就是持久的，不会关闭，使得对同一个服务器的请求可以继续在该连接上完成。

## 3.4 Accept-Encoding 消息头

明确说明了（接收端）可以接受的内容编码形式（所支持的压缩算法）

## 3.5 User_Agent 消息头

可以用来识别发送请求的浏览器/移动设备。

- 该字符串中包含有用空格间隔的产品标记符及注释的清单。

	产品标记符由产品名称、后面紧跟的 `'/' `以及产品版本号构成，例如 `Firefox/4.0.1` 。用户代理可以随意添加多少产品标记符都可以。
	
	注释是一个用**括号分隔的自由形式的字符串**。显然括号本身不能用在该字符串中。规范没有规定注释的内部格式，不过一些浏览器会把一些标记符放置在里面，不同的标记符之间使用 `';' `分隔。

