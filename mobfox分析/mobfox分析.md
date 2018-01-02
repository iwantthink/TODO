# 参考链接


# 简介


# 1.Banner
**继承自RelativeLayout**

**初始化**

拥有四个构造函数，其中三个是在布局中使用，一个用于代码中创建使用。

- 布局中使用： 

        this.context = context;//赋值context
        this.self = this;//赋值self
        this.handler = new Handler(context.getMainLooper());//创建一个主线程的Handeler
        this.getAttrs(attrs);//获取一些参数。。
        this.setUp();//1.7
        this.init();//1.8

- 代码中使用：

        this.context = context;
        this.adspace_width = width;
        this.adspace_height = height;
        this.setLayoutParams(new LayoutParams(LayoutUtils.convertDpToPixel((float)width, context), LayoutUtils.convertDpToPixel((float)height, context)));// 告诉父类，当前Banner控件需要多大
        this.self = this;
        this.handler = new Handler(context.getMainLooper());
        this.setUp();
        this.init();

---
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
			//本质是一个Runnable 通过一些变量进行控制
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
			// 5s之后运行这个timeout ，表示已经超时！ 会回调开发者传入的接口
	        this.handler.postDelayed(this.timeout, 5000L);
			//停止之前一个Repeater
	        if(this.repeater != null) {
	            this.repeater.stop();
	        }
			//判断延迟,>0则执行以下逻辑  刷新广告的逻辑
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

## 1.1 load(String)

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
首先判断控制是否有传入长宽，**如果有且>0则将hasLayout置为true，并获取banner_pos**。如果没有传入，则直接使用`getWidth()/getHeight()`方法获取，如果出异常，则通过ViewTreeObserver方式获取.

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

## 1.4 loadBanner(JsonObject)

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

## 1.7 setUp()
		//创建ExceptionHandler 详见6.1
        MobFoxReport.register(this.context);
		//创建一个WebView 加载的回调
        this.loadAdListener = new MobFoxWebViewLoadAdListener() {

			//加载错误！
            public void onError(MobFoxWebView wv, Exception e) {
				//打印异常信息
                if(e.getMessage() != null) {
                    Log.d("MobFoxBanner", "weblistener error: " + e.getMessage());
                }
				//打印 开始加载到加载出错 花费的时间
                Banner.logTime(Banner.LOAD_START, Banner.this.loadStart);
				//取消Timeout执行 3.1.1
                Banner.this.timeout.cancel();
				//发送异常信息
                MobFoxReport.postException(Banner.this.context, e, (AsyncCallback)null);
				//回调listener，告诉SDK 使用者 加载出错
                if(Banner.this.listener != null) {
                    Banner.this.listener.onBannerError(Banner.this.self, e);
                }
            }
			//加载成功
            public void onAdResponse(MobFoxWebView wv, final JSONObject adResp) {
                //
                Banner.logTime("ad response", Banner.this.loadStart);
				//通过Timeout  去判断加载广告是否超时了
                if(!Banner.this.timeout.isTimeout()) {
					//创建一个HashMap<String,Object>用来存放 参数
					//width , height,demo_age(>0时传入),demo_gender(.length>0时传入),demo_keywords(.length>0时传入)
					//创建一个EventIterator 去遍历 返回的 adResp Json 中的customEvents 部分内容!!
                    Banner.this.iterator = new EventIterator(Banner.this.context, wv, adResp, params);
                    if(Banner.this.iterator.hasNext()) {
                        Banner.this.iterator.callNextEvent(new CustomEventBannerListener() {
                            public void onBannerError(View banner, Exception e) {
								//判断是否超时
								//取消超时控制器
								//如果异常信息为`onAutoRedirect`，调用MobFoxReport.post()，组装一条MobFoxRequest并发送
								//如果异常信息为`onNoAd`,回调BannerListener的onNoFill
								//其余情况下发送异常信息，然后判断EventIterator继续循环是否还可以继续，如果不可以就回调BannerListener的onBannerError()
                            }

                            public void onBannerLoaded(View banner) {
                                Banner.logTime("ad rendered", Banner.this.loadStart);
								//判断是否超时
								//如果没有超时， 取消超时控制器					                                    
								Banner.this.show(banner);//移除容器中的其他View，将当前的Banner 放到容器中
								//回调BannerListener的onBannerLoaded

                            }

                            public void onBannerClosed(View banner) {
                                Log.d("MobFoxBanner", "banner closed");
                                //移除容器内容，并且回调
                            }

                            public void onBannerFinished() {
                                Log.d("MobFoxBanner", "banner finished");
                                if(Banner.this.listener != null) {
                                    Banner.this.listener.onBannerFinished();
                                }
                            }

                            public void onBannerClicked(View banner) {
                                Log.d("MobFoxBanner", "banner clicked");
                                if(Banner.this.listener != null) {
                                    Banner.this.listener.onBannerClicked(banner);
                                }
                            }
                        });
                    }

                }
            }
			//无广告返回
            public void onNoAd(MobFoxWebView wv) {
                Log.d("MobFoxBanner", "on no ad");
				//打印 从开始加载到加载到空广告的耗费时间
                Banner.logTime(Banner.LOAD_START, Banner.this.loadStart);
                Banner.this.timeout.cancel();
                if(Banner.this.listener != null) {
                    Banner.this.listener.onNoFill(Banner.this.self);
                }
            }
        };

## 1.8 init()

    protected void init() {
        warmUp(this.context); //1.9
        this.getAdvId();// 1.10
        this.loadJs();// 1.11
        this.getLayout();// 1.2
        this.getLocation(); // 1.12
		//获取 应用包名
        this.sub_bundle_id = Utils.getBundleId(this.context);
        Utils.postDMP(this.context, this.mobFoxWebView);
		//12.2
        Utils.startMobFoxService(this.context);
    }

## 1.9 warmUp(Context ctx)

   public static void warmUp(Context context) {
        Boolean var1 = warmedUp;
        synchronized(warmedUp) {
            try {
				//启用 http/https 请求缓存
                if(HttpResponseCache.getInstalled() == null) {
                    File httpCacheDir = new File(context.getCacheDir(), "mobfox-http");
                    long httpCacheSize = 10485760L;
                    HttpResponseCache.install(httpCacheDir, httpCacheSize);
                }

                if(warmedUp.booleanValue()) {
                    return;
                }
				//请求了三个地址 ，缓存下来 android.html WebViewJavascriptBridge.js sdk_video.js
                (new MobFoxRequest(MobFoxWebView.getMobfoxUrl())).get((AsyncCallback)null);
                (new MobFoxRequest(MobFoxWebView.getMobfoxUrlVideo())).get((AsyncCallback)null);
                (new MobFoxRequest(MobFoxWebView.getMobfoxUrlBridge())).get((AsyncCallback)null);
                warmedUp = Boolean.valueOf(true);
            } catch (Throwable var6) {
                Log.d("MobFoxBanner", "error init cache", var6);
            }

        }
    }

- HtppResponseCache: 缓存HTTP 和HTTPS 的请求，仅对HTTPURLCONNECTION 起作用

## 1.10 getAdvId()
获取谷歌广告ID

    protected void getAdvId() {
        if(O_ANDADVID.isEmpty()) {
            MobFoxAdIdService advIdService = new MobFoxAdIdService(new Listener() {
                public void onFinish(String adv_id) {
                    if(adv_id == null) {
                        Banner.O_ANDADVID = "";
                    } else {
                        Banner.O_ANDADVID = adv_id;
                    }
                }
            }, this.context);
            advIdService.execute();
        }
    }

## 1.11 loadJs()

    protected void loadJs() {
		//创建了 MobFoxWebView,并传入setUp()中创建的MobFoxWebViewLoadAdListener
        this.mobFoxWebView = new MobFoxWebView(this.context, this.loadAdListener);
		//对当前控件的广告位大小进行判断，然后设置MobFoxWebView的大小！
        if(this.adspace_width > 0 && this.adspace_height > 0) {
            this.mobFoxWebView.setLayoutParams(new LayoutParams(LayoutUtils.convertDpToPixel((float)this.adspace_width, this.context), LayoutUtils.convertDpToPixel((float)this.adspace_height, this.context)));
        } else {
			//如果没有指定大小 ，那就Match_Parent
            this.mobFoxWebView.setLayoutParams(new android.view.ViewGroup.LayoutParams(-1, -1));
        }
    }

## 1.12 getLocation()
获取经纬度的逻辑

    protected void getLocation() {
		//用来判断是否使用 位置服务
        if(loc) {
			//创建一个MobFoxLoactionService 赋值到成员变量
            Location l = MobFoxLocationService.getInstance(this.context).getLocation();
            if(l != null) {
                this.setLocation(l);
            }

        }
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
继承自Runnable，提供了一个condition()方法，一个mobFoxRun()方法。俩个方法都是在Runnable的run()方法中被使用。

- mobFoxRun()方法：具体执行逻辑的地方。被try catch包围，会catch住异常，除了一个指定的异常情况下，会将其余异常发送到服务器。

- condition()方法：用来判断是否执行 mobFoxRun()

### 3.1.1 Timeout
添加了timeout,cancelled字段 默认都为false。

构造函数中传入一个`Callable`(可以获取返回值)。

重写了`mobFoxRun()`方法，传入的Callable会在这里执行。

提供了`reset()/cancel()`对timeout,cancelled字段进行操作。

重写了`condition()`方法，**通过cancelled字段进行控制**,true执行，false不执行

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

## 5.1 BridgeWebView
继承自WebView，定义了一些方法与JS交互。
## 5.2 MobFoxWebView

    public MobFoxWebView(final Context context, MobFoxWebViewLoadAdListener loadAdListener) {
	//将loadAdListener 保存到对象中
	//保存context
	//设置背景颜色为 TRANSPARENT 透明！
	//创建一个在主线程运行的Handler
     this.init(this);
	//获取WebSettings ,开启JS功能，开启缓存setDomStorageEnabled(true).
	//如果SDK>=16 开启
     settings.setAllowFileAccess(true);
     settings.setAllowContentAccess(true);
     settings.setAllowFileAccessFromFileURLs(true);
     settings.setAllowUniversalAccessFromFileURLs(true);
	//设置webview的缓存使用机制 为默认
	//设置当前webView 的触摸反馈为 false，取消默认的触摸反馈
	//设置当前webview 在点击和触摸时 时候拥有声音反馈
	//SDK>21 时 设置允许 webview的安全内容加载不安全来源内容

	//在Native端注册一些 方法 提供给JS调用，并且会回调MobFoxWebViewRenderAdListener的回调方法
	// 设置WebViewCLient,WebChromeClient
	//设置onTouchListener 返回false
	//加载android.html，记录开始加载的时间 ，如果出现异常会将异常发送并 调用回调
	

	}



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

- ready 字段：TODO 说明 应该是JS 调用Native注册的方法 设置true，由H5决定

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
		//调用`setWaterfallsJson`方法
        this.callHandler("setWaterfallsJson", this.waterfalls, new CallBackFunction() {
            public void onCallBack(String data) {
                MobFoxWebView.this.callHandler("loadAd", MobFoxWebView.this.options, (CallBackFunction)null);
            }
        });
    }

- userInteraction:TODO说明


## 5.4 renderAd(JSONObject adResp)

    public void renderAd(final JSONObject adResp) {
		//默认是ready 值为false
		//判断是否 ready ，如果false，则延迟50ms 继续尝试
        if(!this.ready) {
            this.mainHandler.postDelayed(new WebViewRunnable(this.context, this, "renderAdListener") {
                public void mobFoxRun() {
                    MobFoxWebView.this.renderAd(adResp);
                }
            }, 50L);
        } else {
			//ready为true
            this.userInteraction = false;
			//调用JS 中已经注册好了的 `renderAd`函数
            this.callHandler("renderAd", adResp.toString(), new CallBackFunction() {
                public void onCallBack(String data) {
                    try {
                        if(data == null) {
                            data = "";
                        }

                        if(data == "null") {
                            data = "";
                        }

                        if(MobFoxWebView.this.renderAdListener == null) {
                            return;
                        }

                        MobFoxWebView.this.renderAdListener.onRendered(MobFoxWebView.this, data);
                    } catch (Throwable var3) {
                        ;
                    }

                }
            });
        }
    }

## 5.5 init(MobFoxWebView)

    void init(final MobFoxWebView self) {
		//使用 addJSI 方式 提供给js一个Native的调用方法 getCachedVideoURL() 5.6
        this.addJavascriptInterface(this, "MobFoxVideoCache");
		//创建MobFoxWebViewClient 并传入listener
        this.mobFoxWebViewClient = new MobFoxWebViewClient(self, new Listener() {
            public void onClick(final String url) {
                MobFoxWebView.this.mainHandler.post(new WebViewRunnable(MobFoxWebView.this.context, self, "renderAdListener") {
                    public void mobFoxRun() {
                        MobFoxWebView.this.renderAdListener.onAdClick(self, url);
                    }
                });
            }

            public void onError(final Exception e) {
                MobFoxWebView.this.mainHandler.post(new WebViewRunnable(MobFoxWebView.this.context, self, "renderAdListener") {
                    public void mobFoxRun() {
                        MobFoxWebView.this.renderAdListener.onError(self, e);
                    }
                });
            }

            public void onAutoRedirect(WebView view, final String url) {
                MobFoxWebView.this.mainHandler.post(new WebViewRunnable(MobFoxWebView.this.context, self, "renderAdListener") {
                    public void mobFoxRun() {
                        MobFoxWebView.this.renderAdListener.onAutoRedirect(self, url);
                    }
                });
            }
        });
    }

## 5.6 getCachedVideoURL()
创建一个网络代理去使用,使用了一个`com.danikula.videocache`库，貌似是视频播放时 做缓存使用


# 6.MobFoxReport
继承自UncaughtExceptionHandler

## 6.1 register(Context ctx)

    public static synchronized void register(Context c) {
		//默认false，判断是否重复调用
        if(!isRegistered) {
			//创建对象
            MobFoxReport crashCatcher = new MobFoxReport(c);
            UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            if(defaultHandler != null) {
                crashCatcher.setDefaultHandler(defaultHandler);
            }
			//将默认的异常捕获Handler 设置为MobFoxReport
            Thread.setDefaultUncaughtExceptionHandler(crashCatcher);
            isRegistered = true;
        }
    }

## 6.2 postException()
组装一条Json，添加参数。然后创建一个MobFoxRequest 去发送

# 7.EventIterator

## 7.1 构造函数
	//respObj 即 加载ad之后返回的Json
	//params 即在setUp()方法中拼凑的参数
    public EventIterator(Context context, MobFoxWebView mobFoxWebView, JSONObject respObj, Map<String, Object> params) {
        this.context = context;
        this.mainHandler = new Handler(context.getMainLooper());//创建一个主线程的Handler
        this.params = params;//赋值
        this.mobFoxWebView = mobFoxWebView;//赋值
        this.respObj = respObj;//赋值
        JSONArray customEvents = null; 

        try {
			//获取一个customEvents 的数组
            customEvents = respObj.getJSONArray("customEvents");
        } catch (JSONException var12) {
            Log.d("MobFoxBanner", "iterator json exception");
            return;
        }

        this.customDataList = new ArrayList();
        if(customEvents != null && customEvents.length() > 0) {
            for(int i = 0; i < customEvents.length(); ++i) {
                try {
					//解析 Json 放到customDataList中
                    JSONObject customEvent = (JSONObject)customEvents.get(i);
                    CustomEventData customData = CustomEventData.parseJSON(customEvent);
					//用来判断进行迭代的类 是否存在
                    Class.forName("com.mobfox.sdk.customevents." + customData.className);
                    this.customDataList.add(customData);
                } catch (JSONException var9) {
                    Log.d("MobFoxBanner", "iterator json exception");
                } catch (ClassNotFoundException var10) {
                    Log.d("MobFoxBanner", "iterator class not found exception");
                } catch (Throwable var11) {
                    Log.d("MobFoxBanner", "class not found throwable");
                }
            }
        }

    }
## 7.2 hasNext()

    public boolean hasNext() {
        return this.customDataList.size() != 0 || this.respObj.has("ad") || this.respObj.has("vasts");
    }

## 7.3 callNextEvent(CustomEventBannerListener listener)
	
	public void callNextEvent(final CustomEventBannerListener listener) {
			//创建一个单个广告加载的回调
	        CustomEventBannerListener customListener = new CustomEventBannerListener() {
	            public void onBannerLoaded(final View banner) {
	                EventIterator.this.mainHandler.post(new Runnable() {
	                    public void run() {
	                        listener.onBannerLoaded(banner);
	                    }
	                });
					//如果pixel 字段不为空，则创建一个MobFoxRequest 请求并get
	                if(EventIterator.this.pixel != null) {
	                    MobFoxRequest firePixel = new MobFoxRequest(EventIterator.this.pixel);
	                    firePixel.get((AsyncCallback)null);
	                }
	            }
	
	            public void onBannerClosed(final View banner) {
	                EventIterator.this.mainHandler.post(new Runnable() {
	                    public void run() {
	                        listener.onBannerClosed(banner);
	                    }
	                });
	            }
	
	            public void onBannerFinished() {
	                EventIterator.this.mainHandler.post(new Runnable() {
	                    public void run() {
	                        listener.onBannerFinished();
	                    }
	                });
	            }
	
	            public void onBannerClicked(final View banner) {
	                EventIterator.this.mainHandler.post(new Runnable() {
	                    public void run() {
	                        listener.onBannerClicked(banner);
	                    }
	                });
	            }
	
	            public void onBannerError(final View banner, final Exception e) {
	                EventIterator.this.mainHandler.post(new Runnable() {
	                    public void run() {
	                        listener.onBannerError(banner, e);
	                    }
	                });
	            }
	        };
	        if(this.customDataList.size() > 0) {
	            CustomEventData customData = (CustomEventData)this.customDataList.get(0);
	            this.customDataList.remove(0);
	
	            try {
					//获取指定类 并创建
	                Class clazz = Class.forName("com.mobfox.sdk.customevents." + customData.className);
	                Constructor co = clazz.getConstructor(new Class[0]);
	                CustomEventBanner customBanner = (CustomEventBanner)co.newInstance(new Object[0]);
	                this.pixel = customData.pixel;
					//执行loadAd方法
	                customBanner.loadAd(this.context, customListener, customData.networkId, this.params);
	            } catch (ClassNotFoundException var7) {
	                Log.d("MobFoxBanner", "ce ClassNotFoundException");
	            } catch (InvocationTargetException var8) {
	                Log.d("MobFoxBanner", "ce InvocationTargetException");
	            } catch (NoSuchMethodException var9) {
	                Log.d("MobFoxBanner", "ce NoSuchMethodException");
	            } catch (InstantiationException var10) {
	                Log.d("MobFoxBanner", "ce InstantiationException");
	            } catch (IllegalAccessException var11) {
	                Log.d("MobFoxBanner", "ce IllegalAccessException");
	            } catch (Throwable var12) {
	                Log.d("MobFoxBanner", "banner iterator error");
	            }
	
	        } else {
	            this.pixel = null;
	            this.bannerEvent = new BannerEvent(this.mobFoxWebView, this.respObj);
	            this.bannerEvent.loadAd(this.context, customListener, (String)null, (Map)null);
	            if(this.respObj.has("ad")) {
	                this.respObj.remove("ad");
	            } else {
	                if(this.respObj.has("vasts")) {
	                    this.respObj.remove("vasts");
	                }
	
	            }
	        }
	    }

# 8. CustomEventBanner

# 8.1 BannerEvent.loadAd

    public void loadAd(final Context context, @NonNull final CustomEventBannerListener listener, String networkID, Map<String, Object> params) {
		//创建一个MobFoxWebViewRenderAdListener
        this.webView.setRenderAdListener(new MobFoxWebViewRenderAdListener() {
            public void onError(MobFoxWebView wv, Exception e) {
                listener.onBannerError(wv, e);
            }

            public void onAdClick(MobFoxWebView wv, String clickURL) {
				//跳转到指定的 网页
                try {
                    Intent launchBrowser = new Intent("android.intent.action.VIEW");
                    launchBrowser.setData(Uri.parse(clickURL));
                    launchBrowser.setFlags(268435456);
                    context.startActivity(launchBrowser);
                } catch (Exception var4) {
                    Log.d("MobFoxBanner", "launch browser exception");
                    listener.onBannerError(wv, var4);
                    return;
                } catch (Throwable var5) {
                    Log.d("MobFoxBanner", "launch browser exception");
                    listener.onBannerError(wv, new Exception(var5.getMessage()));
                    return;
                }

                listener.onBannerClicked(wv);
            }

            public void onVideoAdFinished(MobFoxWebView wv) {
                listener.onBannerFinished();
            }

            public void onAdClosed(MobFoxWebView wv) {
                listener.onBannerClosed(wv);
            }

            public void onAutoRedirect(MobFoxWebView wv, String url) {
                listener.onBannerError(wv, new Exception("onAutoRedirect"));
            }

            public void onRendered(MobFoxWebView wv, String data) {
                if(data.isEmpty()) {
                    Banner.logTime("rendered!", wv.loadBannerStarted);
                    listener.onBannerLoaded(BannerEvent.this.banner);
                } else {
                    listener.onBannerError(wv, new Exception(data));
                }
            }
        });
        this.banner = this.webView;
        this.webView.renderAd(this.respObj);
		//加载完毕
        listener.onBannerLoaded(this.webView);
    }


# 9.一些回调接口
## 9.1 CustomEventBannerListener
提供给EventIterator使用，在EventIterator 进行循环遍历时，对其遍历过程中 每一个具体的点进行回调.

主要是对 请求返回的 Json中的 `customEvents` 对象 进行处理

	public interface CustomEventBannerListener {
	    void onBannerError(View var1, Exception var2);
	
	    void onBannerLoaded(View var1);
	
	    void onBannerClosed(View var1);
	
	    void onBannerFinished();
	
	    void onBannerClicked(View var1);
	}

## 9.2 BannerListener
由开发者在使用SDK时 创建并传入 SDK，主要作用是将 Banner的一些关键点 告知开发者

public interface BannerListener {
    void onBannerError(View var1, Exception var2);

    void onBannerLoaded(View var1);

    void onBannerClosed(View var1);

    void onBannerFinished();

    void onBannerClicked(View var1);

    void onNoFill(View var1);
}

## 9.3 MobFoxWebViewLoadAdListener
在Banner中setUp()方法中进行初始化，提供给MobFoxWebView使用

	public interface MobFoxWebViewLoadAdListener {
	    void onError(MobFoxWebView var1, Exception var2);
	
	    void onAdResponse(MobFoxWebView var1, JSONObject var2);
	
	    void onNoAd(MobFoxWebView var1);
	}

# 10.MobFoxBaseService

## 10.1 MobFoxAdIdService

获取谷歌广告ID，利用反射获取 `com.google.android.gms.ads.identifier.AdvertisingIdClient`类，然后获取其广告ID并返回，耗时操作利用了AsyncTask实现

# 11.MobFoxLocationService
获取经纬度的逻辑。。。待分析TODO

# 12.Utils

## 12.1 postDMP(Context,WebView)

    public static void postDMP(Context context, WebView view) {
        try {
            String IPAddress = getIPAddress(true);
            String ua = view.getSettings().getUserAgentString();
            DMPManager.updateDMP(context, IPAddress, ua);
            DMPManager.postDMP(context, new DMPCallback() {
                public void onPostCompleted() {
                    Log.d("MobFoxBanner", "dmp post completed");
                }

                public void onPostError() {
                    Log.d("MobFoxBanner", "dmp post failed");
                }
            });
        } catch (Throwable var4) {
            Log.d("MobFoxBanner", "post dmp exception");
        }

    }

## 12.2 startMobFoxService(Context)

    public static void startMobFoxService(Context context) {
        try {
			//判断是否拥有定位权限 Coarse_location 和Fine_location 
			//或者SDK版本低于 24
            if(MobfoxSettings.hasLocation(context) || VERSION.SDK_INT < 24) {
                Intent service = new Intent(context, MobFoxService.class);
                context.startService(service);
            }
        } catch (Exception var2) {
            Log.d("MobFoxBanner", "start uam exception");
        }

    }

# 13.MobFoxService
启动一个服务。。。具体做什么 待分析
