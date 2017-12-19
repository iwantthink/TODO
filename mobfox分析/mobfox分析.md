
# 1.Banner
继承自RelativeLayout..

使用步骤：

1. `setInventoryHash()`，调用此方法设置广告ID(Hash值),封装了一个MobFoxRequest请求(`https://sdk.starbolt.io/waterfalls.json`)

		 public void setInventoryHash(String invh) {
		        this.s = invh;
				//查看1.6
		        this.getWaterfalls(this.s);
		    }
		

2. `load()`,检查是否设置了inventory hash.创建了一个自定义Runnable类`TimeOut`，传入一个Callable,检查listener是否存在 分别返回true/false，通过`Handler.postDelayed()`延迟5秒发送(**作用：判断请求是否超时**)。通过Repeater设置了一个请求重试.

	    public void load() {
	        this.loadStart = System.currentTimeMillis();
	        if(this.self.s == null || this.self.s.isEmpty()) {
	            Log.d("MobFoxBanner", "please set inventory hash before load()");
	            if(this.self.listener == null) {
	                return;
	            }
	
	            this.self.listener.onBannerError(this.self, new Exception("please set inventory hash before load()"));
	        }
	
	        this.timeout.cancel();
	        this.timeout = new Timeout(this.context, new Callable() {
	            public Object call() throws Exception {
	                if(Banner.this.listener == null) {
	                    return Boolean.valueOf(false);
	                } else {
	                    Banner.this.listener.onBannerError(Banner.this.self, new Exception("timeout"));
	                    return Boolean.valueOf(true);
	                }
	            }
	        });
	        this.handler.postDelayed(this.timeout, 5000L);
			//停止之前一个Repeater
	        if(this.repeater != null) {
	            this.repeater.stop();
	        }
			//判断延迟,>0则执行以下逻辑
	        if(this.refresh > 0) {
	            this.repeater = new Repeater(this.context, this.handler, (long)this.refresh, new Callable() {
	                public Object call() throws Exception {
						//执行load()方法。。
	                    Banner.this.load();
	                    return Boolean.valueOf(true);
	                }
	            });
	            this.repeater.start();
	        }
			//查看 1.1
	        this.load(this.self.s);
	    }

	- refresh 字段：延迟时间，多长时间之后重试  单位：毫秒

## 1.1 load()

    protected void load(String invh) {
		//hasLayout 通过getLayout()方法赋值
		//当hasLayout为false时 执行
        if(!this.hasLayout) {
			// 延迟200毫秒后重试，直到 hasLayout为true！！！
            this.handler.postDelayed(new Runnable() {
                public void run() {
                    if(!Banner.this.hasLayout) {
                        Banner.this.handler.postDelayed(this, 200L);
                    } else {
                        Banner.this.load(Banner.this.self.s);
                    }

                }
            }, 200L);
        } else {
			//判断是否是smart模式，smart模式会自动调整调整空间的长宽
            if(this.smart) {
                if(this.adspace_height == 50) {
                    this.adspace_width = 320;
                } else {
                    if(this.adspace_height != 90) {
                        Log.d("MobFoxBanner", "smart banner supports 50, 90 heights");
                        if(this.listener == null) {
                            return;
                        }
						//取消 timeout
                        this.timeout.cancel();
						//回调
                        this.listener.onBannerError(this.self, new Exception("smart banner supports 50, 90 heights"));
                        return;
                    }
					// w=728 h=90
                    this.adspace_width = 728;
                }

                LayoutUtils.setSmartDimensions(this.context, this.self, this.adspace_height);
            }
			//组装JsonObj
            JSONObject params = this.makeParams();
            if(params != null) {
				//查看1.4
                this.loadBanner(params);
            }
        }
    }

## 1.2 getLayout()
首先判断控制是否有传入长宽，如果有且>0则将hasLayout置为true，并获取banner_pos。如果没有传入，则直接使用`getWidth()/getHeight()`方法获取，如果出异常，则通过ViewTreeObserver方式获取.

    protected void getLayout() {
		//adspace_width/adspace_height 即控件长宽俩者皆>0则hasLayout置为true
        if(this.adspace_width > 0 && this.adspace_height > 0) {
            this.hasLayout = true;
			//查看1.3 获取banner_pos的值
            this.getBannerPosition();
        } else {
            final DisplayMetrics displayMetrics = this.context.getResources().getDisplayMetrics();

            try {
				//获取 长宽  单位：dp
                int width = (int)((float)this.self.getWidth() / displayMetrics.density);
                int height = (int)((float)this.self.getHeight() / displayMetrics.density);
				//如果长宽都>0,赋值成员变量，并调用getBannerPosition()
                if(width > 0 && height > 0) {
                    this.self.adspace_width = width;
                    this.self.adspace_height = height;
                    Log.d("MobFoxBanner", "adspace_width: " + this.self.adspace_width + "\nadspace_height: " + this.self.adspace_height);
                    this.hasLayout = true;
                    this.getBannerPosition();
                    return;
                }
            } catch (Exception var4) {
                Log.d("MobFoxBanner", "get layout error");
            } catch (Throwable var5) {
                Log.d("MobFoxBanner", "get layout error");
            }
			//如果上述方法出错，则通过ViewTreeObserver回调去获取控件长宽
            ViewTreeObserver treeObserver = this.self.getViewTreeObserver();
            treeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if(VERSION.SDK_INT >= 16) {
                        Banner.this.self.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }

                    Banner.this.self.adspace_width = (int)Math.ceil((double)((float)Banner.this.self.getWidth() / displayMetrics.density));
                    Banner.this.self.adspace_height = (int)Math.ceil((double)((float)Banner.this.self.getHeight() / displayMetrics.density));
                    if(Banner.this.self.adspace_height > 45 && Banner.this.adspace_height < 55) {
                        Banner.this.self.adspace_height = 50;
                    }

                    if(Banner.this.self.adspace_height > 85 && Banner.this.adspace_height < 95) {
                        Banner.this.self.adspace_height = 90;
                    }

                    if(Banner.this.self.adspace_height > 245 && Banner.this.adspace_height < 255) {
                        Banner.this.self.adspace_height = 250;
                    }

                    Banner.this.self.post(new Runnable() {
                        public void run() {
                            Banner.this.hasLayout = true;
                        }
                    });
                    Banner.this.getBannerPosition();
                }
            });
        }
    }

## 1.3 getBannerPosition()

    protected void getBannerPosition() {
		//长宽为480*320或320*480即可，判断是否是interstitial类型
        if(this.isInterstitial()) {
            this.banner_pos = 7;
        } else {
            try {
				//判断View的位置 是否展示在屏幕中 ，view的左上角y坐标>0&<mScreenHeight
                if(LayoutUtils.aboveTheFold(this.context, this.self)) {
                    this.banner_pos = 1;
                    return;
                }

                this.banner_pos = 3;
            } catch (Exception var2) {
                Log.d("MobFoxBanner", "above the fold exception");
            } catch (Throwable var3) {
                Log.d("MobFoxBanner", "above the fold exception");
            }

        }
    }

## 1.4 loadBanner(JsonObj)

    protected void loadBanner(JSONObject params) {
        try {
			//调用webView加载
            this.mobFoxWebView.loadAd(params.toString());
        } catch (Exception var3) {
            Log.d("MobFoxBanner", "webView loadBanner error");
            if(this.listener == null) {
                return;
            }

            this.listener.onBannerError(this.self, var3);
        } catch (Throwable var4) {
            Log.d("MobFoxBanner", "webView loadBanner error");
            if(this.listener == null) {
                return;
            }

            this.listener.onBannerError(this.self, new Exception(var4.getMessage()));
        }

    }

## 1.6 getWaterfalls(String invh)

	    protected void getWaterfalls(String invh) {
	        MobFoxRequest request = new MobFoxRequest("https://sdk.starbolt.io/waterfalls.json");
	        request.setTimeout(1000);
	        request.setParam("p", invh);
			//使用get请求
	        request.get(new AsyncCallback() {
				//请求成功 会调用此接口，传入 状态码,响应体，响应头
	            public void onComplete(int code, Object response, Map<String, List<String>> headers) {
					//响应体的内容 赋值给waterfalls...
	                Banner.this.waterfalls = response.toString();
					//赋值给 MobFoxWebView的 waterfalls变量
	                Banner.this.mobFoxWebView.setWaterfalls(Banner.this.waterfalls.replace("\n", "").replace("\t", "").replace("\r", ""));
	
	                try {
						// 解析响应体内容，如果存在debug 则当前为测试模式
	                    JSONObject pub = new JSONObject(Banner.this.waterfalls);
	                    if(pub.has("debug")) {
	                        Banner.DEBUG_MODE = pub.getBoolean("debug");
	                    } else {
	                        Banner.DEBUG_MODE = false;
	                    }
	                } catch (JSONException var5) {
	                    ;
	                }
	
	            }
	
	            public void onError(Exception e) {
					//将MobFoxWebView的waterfalls内容置空
	                Banner.this.mobFoxWebView.setWaterfalls("");
	                if(e.getMessage() != null) {
	                    Log.d("MobFoxBanner", "on waterfalls fetch " + e.getMessage());
	                    if(!e.getMessage().contains("failed to connect to sdk.starbolt.io")) {
							//将异常信息组装成一个Request 发送出去
	                        MobFoxReport.postException(Banner.this.context, e, (AsyncCallback)null);
	                    }
	                } else {
	                    Log.d("MobFoxBanner", "on waterfalls fetch error");
	                }
	            }
	        });
	    }

**返回的Json内容：**

	{
	    "waterfalls": {
	        "banner": [
	            {
	                "name": "banner",
	                "prob": 1
	            }
	        ],
	        "interstitial": [
	            {
	                "name": "banner",
	                "prob": 1
	            }
	        ]
	    },
	    "debug": true
	}


# 2.MobFoxRequest

## 2.1 构造函数
传入请求的url地址，新建俩个HashMap，一个存放参数  一个存放请求头

    public MobFoxRequest(String url) {
        this.url = url;
        this.parameters = new HashMap();
        this.headers = new HashMap();
    }

## 2.2 get(AsyncCallback cb)
即表示请求方式为**get**

	_call(this.url, "GET", this.parameters, this.headers, this.timeout, this.testMode, new MobFoxRequest.DefaultResponseFormatter(null), cb);


## 2.3 post(AsyncCallback cb)
即表示请求方式为**post**

	_call(this.url, "POST", this.data, this.headers, this.timeout, this.testMode, new MobFoxRequest.DefaultResponseFormatter(null), cb);

## 2.4 getBitmap(AsyncCallback cb)

    _call(this.url, "GET", this.parameters, this.headers, this.timeout, this.testMode, new MobFoxRequest.ResponseFormatter() {
            public Object format(InputStream response) {
                return BitmapFactory.decodeStream(response);
            }
        }, new AsyncCallback() {
            public void onComplete(int code, Object response, Map<String, List<String>> headers) {
                cb.onComplete(code, (Bitmap)response, headers);
            }

            public void onError(Exception e) {
                cb.onError(e);
            }
        });

## 2.5 getDrawable(AsyncCallback cb)

	_call(this.url, "GET", this.parameters, this.headers, this.timeout, this.testMode, new MobFoxRequest.ResponseFormatter() {
            public Object format(InputStream response) {
                return Drawable.createFromStream(response, (String)null);
            }
        }, new AsyncCallback() {
            public void onComplete(int code, Object response, Map<String, List<String>> headers) {
                cb.onComplete(code, (Drawable)response, headers);
            }

            public void onError(Exception e) {
                cb.onError(e);
            }
        });

## 2.4  _call(....) 
具体执行请求操作的地方

### 2.4.1 接收的参数：

	String url //请求地址
	final String method //请求方式 get/post
	final Object data // 可能是HashMap(即参数)，或者自定义的一个Object
	final Map<String, String> reqHeaders//请求头参数
	final int timeout //超时时间
	boolean testMode//是否是测试模式
	final MobFoxRequest.ResponseFormatter formatter//将流转换的地方
	final AsyncCallback cb//请求回调

### 2.4.2 具体逻辑
整块代码都被try-catch住，如果出现异常会调用请求回调

    private static void _call(String url, final String method, final Object data, final Map<String, String> reqHeaders, final int timeout, boolean testMode, final MobFoxRequest.ResponseFormatter formatter, final AsyncCallback cb) {
        try {
            String query = "";
			//判断如果是 `Get`请求方式，并且参数为HashMap，就组装参数 用来放到url后面
            if(method.equals("GET") && data instanceof HashMap) {
                query = MobfoxRequestParams.toQuery((HashMap)data);
            }
			//定义一个变量 用来存放完整的url（即 请求地址+参数）
            final String fullURL = url;
            if(query.length() > 0) {
				//判断当前是否已经添加了'?',如果已经添加 则只用添加参数即可，如果没有添加 则需要添加'?'
                if(url.indexOf("?") > 0) {
                    fullURL = url + "&" + query;
                } else {
                    fullURL = url + "?" + query;
                }
            }
			//
            AsyncTask<String, Void, String> t = new AsyncTask<String, Void, String>() {
                int status;
                Object response;
                Map<String, List<String>> responseHeaders;
                Exception err;

                protected String doInBackground(String... params) {
                    HttpURLConnection con = null;
					//将字符写入文本输出流，拥有一个缓冲区，避免字符一写入直接转换
                    BufferedWriter writer = null;

                    Iterator var5;
                    try {
                        URL u = new URL(fullURL);
                        con = (HttpURLConnection)u.openConnection();
                        con.setUseCaches(true);
						//设置请求头中的参数
                        var5 = reqHeaders.keySet().iterator();
                        while(var5.hasNext()) {
                            String h = (String)var5.next();
                            con.setRequestProperty(h, (String)reqHeaders.get(h));
                        }

                        con.setRequestMethod(method);
						//即告诉服务器，请求是来自什么工具
                        con.setRequestProperty("User-Agent", System.getProperty("http.agent"));
						//设置超时时间
                        if(timeout > 0) {
                            con.setConnectTimeout(timeout);
                            con.setReadTimeout(timeout);
                        }
						//如果是Post请求方式，需要手动设置一些值
                        if(method.equals("POST")) {
							//设置允许输入和输出
                            con.setDoInput(true);
                            con.setDoOutput(true);	
							//向外写请求体
                            OutputStream os = con.getOutputStream();
                            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(data.toString());
                            writer.flush();
                            writer.close();
                            os.close();
                        }

                        this.status = con.getResponseCode();
						//将服务器返回的输入流 转换成指定格式
                        this.response = formatter.format(con.getInputStream());
						//获取响应头
                        this.responseHeaders = con.getHeaderFields();
                        String var21 = "success";
                        return var21;
                    } catch (Exception var17) {
                        this.err = var17;
                        var5 = null;
                    } catch (Throwable var18) {
                        if(var18.getMessage() == null) {
                            this.err = new Exception("Error in sending request to server");
                        } else {
                            this.err = new Exception(var18.getMessage());
                        }

                        var5 = null;
                        return var5;
                    } finally {
                        try {
                            if(con != null) {
                                con.disconnect();
                            }
                        } catch (Exception var16) {
                            ;
                        }

                    }

                    return var5;
                }

                protected void onPostExecute(String code) {
					//根据不同状态码 调用不同的接口
                    if(cb != null) {
                        if(code == null) {
                            cb.onError(this.err);
                        } else {
                            cb.onComplete(this.status, this.response, this.responseHeaders);
                        }

                    }
                }
            };
			//如果是测试模式  则串行执行
            if(testMode) {
                t.execute(new String[0]);
            } else {
				//如果不是测试模式，则并行执行
                t.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[0]);
            }
        } catch (Throwable var12) {
            Log.d("MobFoxNetwork", "Unable to complete request!", var12);
            if(cb != null) {
                cb.onError(new Exception(var12.toString()));
            }
        }

    }

- AsyncTask:[AsyncTask-介绍](http://blog.csdn.net/iispring/article/details/50639090)

# 3.Runnable

Timeout 继承自 MobFoxRunnable 继承自 Runnable

## 3.1 MobFoxRunnable
继承自Runnable，提供了一个condition()方法，一个mobFoxRun()方法。

- mobFoxRun()方法：具体执行逻辑的地方。被try catch包围，会catch住异常，除了一个指定的异常情况下，会将其余异常发送到服务器。

- condition()方法：用来判断是否执行 mobFoxRun()

### 3.1.1 Timeout
添加了timeout,cancelled字段 默认都为false。

构造函数中传入一个`Callable`(可以获取返回值)。

重写了`mobFoxRun()`方法，传入的Callable会在这里执行。

提供了`reset()/cancel()`对timeout,cancelled字段进行操作。

重写了`condition()`方法，通过cancelled字段进行控制,true执行，false不执行

# 4.Repeater
**猜测是一个 重试机制！**

构造函数：

    public Repeater(Context context, Handler poster, long delay, Callable action) {
        this.context = context;
        this.poster = poster;//Handler用来发送Runnable
        this.delay = delay;//延迟时间
        this.action = action;//具体执行逻辑的地方
    }

start()方法：创建了一个MobfoxRunnable类，重写了`mobFoxRun()`方法，在这里调用构造函数传入的`Callable`。

stop()方法： 设置stop字段，该字段可以控制Callable的执行

setAction()方法：设置Callable


# 5.MobFoxWebView
**MobFoxWebView 继承自 BridgeWebView 继承自WebView**

## 5.1 BridgeView
一个便捷的JS与Java交互的桥梁。。
[github - JsBridge](https://github.com/lzyzsd/JsBridge/tree/master/library/src/main)

## 5.2 MobFoxWebView

## 5.2.1 loadAd(String options)

    public void loadAd(final String options) {
		//记录开始时间
        this.loadBannerStarted = System.currentTimeMillis();
		// waterfalls 在设置InventoryHash已经去获取(并且是必须做的,不然不会往下执行)
		// ready 在初始化的时候 已经赋值,应该是代表是否 已经初始化完成,可以被改变
        if(this.ready && this.waterfalls != null) {
            this._loadAd(options);
        } else {
			// 重试机制。。。延迟50ms重试
            final Handler h = new Handler();
            h.postDelayed(new Runnable() {
                public void run() {
                    if(MobFoxWebView.this.ready && MobFoxWebView.this.waterfalls != null) {
                        MobFoxWebView.this._loadAd(options);
                    } else {
                        h.postDelayed(this, 50L);
                    }

                }
            }, 50L);
        }
    }

- ready 字段：TODO 说明

## 5.3 _loadAd(String options)

    private void _loadAd(String options) {
		//赋值
        this.options = options;
        this.userInteraction = false;
		//主线程的Handler
        this.mainHandler.post(new Runnable() {
            public void run() {
				//移除所有的视图
                MobFoxWebView.this.removeAllViews();
            }
        });
        this.callHandler("setWaterfallsJson", this.waterfalls, new CallBackFunction() {
            public void onCallBack(String data) {
                MobFoxWebView.this.callHandler("loadAd", MobFoxWebView.this.options, (CallBackFunction)null);
            }
        });
    }

- userInteraction:TODO说明

## 5.4 callHandler(...)

    public void callHandler(String handlerName, String data, CallBackFunction callBack) {
        this.doSend(handlerName, data, callBack);
    }

## 5.5 doSend(...)
TODO:待分析。。调用的JSBridge中的内容

    private void doSend(String handlerName, String data, CallBackFunction responseCallback) {
        try {
            Message m = new Message();
            if(!TextUtils.isEmpty(data)) {
                m.setData(data);
            }

            if(responseCallback != null) {
                String callbackStr = String.format("JAVA_CB_%s", new Object[]{++this.uniqueId + "_" + SystemClock.currentThreadTimeMillis()});
                this.responseCallbacks.put(callbackStr, responseCallback);
                m.setCallbackId(callbackStr);
            }

            if(!TextUtils.isEmpty(handlerName)) {
                m.setHandlerName(handlerName);
            }

            this.queueMessage(m);
        } catch (Throwable var6) {
            Log.d("MobFoxWebView", "error on bridge send message", var6);
        }

    }

# 6.MobFoxReport
继承自UncaughtExceptionHandler