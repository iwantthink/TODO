# CallServerInterceptor分析
[okhttp源码分析（五）-CallServerInterceptor过滤器](https://www.jianshu.com/p/4c54e8264971)

# 1. 简介

负责网络通信最后一个步骤：数据交换，即向服务器发送请求数据，从服务器读取响应数据


## 1.1 拦截链

	// ConnectInteceptor
	public Response intercept(RealInterceptorChain chain) {
	    // 当前拦截器处理
	    // 交给下一个拦截器去操作
        return realChain.proceed(request, streamAllocation, httpCodec, connection);
	}

	// StreamAllocation 在 RAFInterceptor中被创建
	// HttpCodec 和RealConnection 在 ConnectInterceptor 中被创建
	public Response proceed(Request request, StreamAllocation streamAllocation, HttpCodec httpCodec,RealConnection connection){
		 // index用来控制具体使用哪个拦截器
        RealInterceptorChain next = new RealInterceptorChain(interceptors, streamAllocation, httpCodec,
                connection, index + 1, request, call, eventListener, connectTimeout, readTimeout,
                writeTimeout);
        Interceptor interceptor = interceptors.get(index);
        Response response = interceptor.intercept(next);
		 return response;
	}
	


# 2. CallServerInterceptor.intercept()


    public Response intercept(Chain chain) throws IOException {
        final RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Call call = realChain.call();
        final HttpCodec httpCodec = realChain.httpStream();
        StreamAllocation streamAllocation = realChain.streamAllocation();
        RealConnection connection = (RealConnection) realChain.connection();
        Request request = realChain.request();

        long sentRequestMillis = System.currentTimeMillis();
		  // 回调-开始写入header
        realChain.eventListener().requestHeadersStart(call);
        // 写入header
        httpCodec.writeRequestHeaders(request);
        // 回调-结束写入header
        realChain.eventListener().requestHeadersEnd(call, request);

        Response.Builder responseBuilder = null;
        // 判断是否允许请求携带body && body非空
        if (HttpMethod.permitsRequestBody(request.method()) && request.body() != null) {
            // 如果请求头中有一个header = Expect, 且其value = 100-continue
            // 那么仅发送请求头信息至服务器，询问服务器是否愿意接受数据
            // 如果服务器发送"HTTP/1.1 100 continue" 才将数据进行传输
            if ("100-continue".equalsIgnoreCase(request.header("Expect"))) {
            	   // 刷新请求
                httpCodec.flushRequest();
                realChain.eventListener().responseHeadersStart(call);
                // 读取响应头，如果可以继续传输，返回的值为空
                responseBuilder = httpCodec.readResponseHeaders(true);
            }
			  // 表示可以继续传输
            if (responseBuilder == null) {
                if (request.body() instanceof DuplexRequestBody) {
                    // Prepare a duplex body so that the application can send a request body later.
                    httpCodec.flushRequest();
                    CountingSink requestBodyOut = new CountingSink(httpCodec.createRequestBody(request, -1L));
                    BufferedSink bufferedRequestBody = Okio.buffer(requestBodyOut);
                    request.body().writeTo(bufferedRequestBody);
                } else {
                    // Write the request body if the "Expect: 100-continue" expectation was met.
                    realChain.eventListener().requestBodyStart(call);
                    long contentLength = request.body().contentLength();
                    CountingSink requestBodyOut =
                            new CountingSink(httpCodec.createRequestBody(request, contentLength));
                    BufferedSink bufferedRequestBody = Okio.buffer(requestBodyOut);

                    request.body().writeTo(bufferedRequestBody);
                    bufferedRequestBody.close();
                    realChain.eventListener().requestBodyEnd(call, requestBodyOut.successfulCount);
                }
            // 服务器不接受
            } else if (!connection.isMultiplexed()) {
                // 如果服务器不接受数据，那么阻止该Connection被重用
                // 否则我们仍有义务去传输请求的body
                streamAllocation.noNewStreams();
            }
        }

        if (!(request.body() instanceof DuplexRequestBody)) {
            httpCodec.finishRequest();
        }

        if (responseBuilder == null) {
            realChain.eventListener().responseHeadersStart(call);
            responseBuilder = httpCodec.readResponseHeaders(false);
        }

        responseBuilder
                .request(request)
                .handshake(streamAllocation.connection().handshake())
                .sentRequestAtMillis(sentRequestMillis)
                .receivedResponseAtMillis(System.currentTimeMillis());
        Internal.instance.initCodec(responseBuilder, httpCodec);
        Response response = responseBuilder.build();

        int code = response.code();
        if (code == 100) {
            // server sent a 100-continue even though we did not request one.
            // try again to read the actual response
            responseBuilder = httpCodec.readResponseHeaders(false);

            responseBuilder
                    .request(request)
                    .handshake(streamAllocation.connection().handshake())
                    .sentRequestAtMillis(sentRequestMillis)
                    .receivedResponseAtMillis(System.currentTimeMillis());
            Internal.instance.initCodec(responseBuilder, httpCodec);
            response = responseBuilder.build();

            code = response.code();
        }

        realChain.eventListener().responseHeadersEnd(call, response);

        if (forWebSocket && code == 101) {
            // Connection is upgrading, but we need to ensure interceptors see a non-null response body.
            response = response.newBuilder()
                    .body(Util.EMPTY_RESPONSE)
                    .build();
        } else {
            response = response.newBuilder()
                    .body(httpCodec.openResponseBody(response))
                    .build();
        }

        if ("close".equalsIgnoreCase(response.request().header("Connection"))
                || "close".equalsIgnoreCase(response.header("Connection"))) {
            streamAllocation.noNewStreams();
        }

        if ((code == 204 || code == 205) && response.body().contentLength() > 0) {
            throw new ProtocolException(
                    "HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
        }

        return response;
    }

