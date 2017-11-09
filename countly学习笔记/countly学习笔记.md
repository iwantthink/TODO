# Countly 介绍
Countly 是一款实时统计分析系统

[Countly-github-address](https://github.com/Countly/countly-sdk-android)
[Countly-源码解析](http://www.jianshu.com/p/be283cf3c9f4)

# 1. 概要
Android sdk 主要处理**Event,Crash和会话流(Session)**三种数据记录请求。其中**Crash 和 Session**自动记录,并作为**Connection**持久存储到**ConnectionQueue**等待提交到服务器。**Event**由开发者调用，并配有一个**EventQueue**,但是在在上传到服务器时还是通过加入**ConnectionQueue**实现，也就是说所有的请求最终都是**Connection**.

**ConnectionQueue**和**EventQueue**并不是平常意义的FIFO队列，而是本地存储队列。包装了基于**SharePreference**实现的持久层Store，每个请求都会被字符串化并加上分隔符，添加到对应的SP键值后.

# 2. 流程介绍
## 2.1 初始化
### 2.1.1 对象定义
- Countly 使用单例模式处理了其对象，保证线程安全.**这种单例模式优点是不需要使用任何锁，并且是懒加载的(直到有人调用才会去加载)**[Stackoverflow中对这种模式的介绍](http://stackoverflow.com/questions/7048198/thread-safe-singletons-in-java)

		//使用方式
		Countly.sharedInstance()
		//定义方式
		class Countly{
			Countly(){}

			public static Countly sharedInstance() {
			    return SingletonHolder.instance;
			}

		    private static class SingletonHolder {
		        static final Countly instance = new Countly();
		    }
		}

### 2.1.2 构造函数
在Countly的构造函数中， 创建了一个`ConnectionQueue`，初始化了一个`session timer`。

	 private ConnectionQueue connectionQueue_;
     private ScheduledExecutorService timerService_;

	 Countly() {
	        connectionQueue_ = new ConnectionQueue();
	        Countly.userData = new UserData(connectionQueue_);
	        timerService_ = Executors.newSingleThreadScheduledExecutor();
	        timerService_.scheduleWithFixedDelay(new Runnable() {
	            @Override
	            public void run() {
	                onTimer();
	            }
	        }, TIMER_DELAY_IN_SECONDS, TIMER_DELAY_IN_SECONDS, TimeUnit.SECONDS);
	    }



- `session timer`
	- Java 线程池中的定时任务线程池，创建了一个单线程执行程序，可以用来处理延迟或定义任务.[参考链接](http://www.cnblogs.com/yangzhilong/p/4789031.html)
	- Countly在这里创建了一个每隔60秒执行一次`onTimer()`的定时器。
	- `onTimer()`会判断当前是否有活动的`application`,如果有就会发送一个 `session hearbeat`。	
	- 注意这里的`activityCount_`，这个参数是在Countly.onStart()和Countly.onStop()中改变的
		
		    synchronized void onTimer() {
		        final boolean hasActiveSession = activityCount_ > 0;
		        if (hasActiveSession) {
		            if (!disableUpdateSessionRequests_) {
		                connectionQueue_.updateSession(roundedSecondsSinceLastSessionDurationUpdate());
		            }
		            if (eventQueue_.size() > 0) {
		                connectionQueue_.recordEvents(eventQueue_.events());
		            }
		        }
		    }

		- `roundedSecondsSinceLastSessionDurationUpdate()`方法用来计算session多久未发送(Countly的逻辑：负数和零会在很多方法中被判断为无效值)。 [nano和currentTimeMillis的区别](http://blog.csdn.net/dliyuedong/article/details/8806868)
	
			    int roundedSecondsSinceLastSessionDurationUpdate() {
			        final long currentTimestampInNanoseconds = System.nanoTime();
			        final long unsentSessionLengthInNanoseconds = currentTimestampInNanoseconds - prevSessionDurationStartTime_;
			        prevSessionDurationStartTime_ = currentTimestampInNanoseconds;
			        return (int) Math.round(unsentSessionLengthInNanoseconds / 1000000000.0d);
			    }

- `ConnectionQueue`
	- `ConnectionQueue`会排列`session`和`event`数据，并在一个后台线程定期的发送这些数据.这个类中的方法没有使用同步锁，因为该类是通过单例类`Countly`访问。

		- `timer()`方法中的`updateSession`方法会记录一个`session` 并发送到服务器.

			    void updateSession(final int duration) {
			        checkInternalState();
			        if (duration > 0) {
			            final String data = "app_key=" + appKey_
			                    + "&timestamp=" + Countly.currentTimestampMs()
			                    + "&hour=" + Countly.currentHour()
			                    + "&dow=" + Countly.currentDayOfWeek()
			                    + "&session_duration=" + duration
			                    + "&location=" + getCountlyStore().getAndRemoveLocation()
			                    + "&sdk_version=" + Countly.COUNTLY_SDK_VERSION_STRING
			                    + "&sdk_name=" + Countly.COUNTLY_SDK_NAME;
			
			            store_.addConnection(data);
			
			            tick();
			        }
			    }

			- `CountlyStore.addConnection(String data)`会将传入的String 存储到 `SharedPreference`中。有一个限制，SP中的数据不能超过1000条。

				    public synchronized void addConnection(final String str) {
				        if (str != null && str.length() > 0) {
				            final List<String> connections = new ArrayList<>(Arrays.asList(connections()));
				            if (connections.size() < MAX_REQUESTS) {
				                connections.add(str);
				                preferences_.edit().putString(CONNECTIONS_PREFERENCE, join(connections, DELIMITER)).commit();
				            }
				        }
				    }
		
		- `tick`方法，开启一个运行在开后台的`ConnectionProcessor`对象去处理本地的数据。[Future的使用介绍](https://lidong1665.github.io/2017/02/27/Java%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B%EF%BC%9ACallable%E3%80%81Future%E5%92%8CFutureTask/)

    			private Future<?> connectionProcessorFuture_;

			    void tick() {
			        if (!store_.isEmptyConnections() && (connectionProcessorFuture_ == null || connectionProcessorFuture_.isDone())) {
			            ensureExecutor();
			            connectionProcessorFuture_ = executor_.submit(new ConnectionProcessor(serverURL_, store_, deviceId_, sslContext_));
			        }
			    }
			
			    void ensureExecutor() {
			        if (executor_ == null) {
			            executor_ = Executors.newSingleThreadExecutor();
			        }
			    }
			
- `EventQueue`,在`timer()`方法中还会调用`ConnectionQueue.recordEvents(String data)`方法.先是会去判断本地的event数量，然后获取这些值传入`ConnectionQueue`进行发送。`recordEvents()`方法与`updateSession()`方法相似。

	   if (eventQueue_.size() > 0) {
	      connectionQueue_.recordEvents(eventQueue_.events());
	   }

	- `eventQueue_.events()`方法移除当前本地的event，并以URL-encoded Json 形式的字符串返回。

		 	String events() {
		        String result;
		
		        final List<Event> events = countlyStore_.eventsList();
		
		        final JSONArray eventArray = new JSONArray();
		        for (Event e : events) {
		            eventArray.put(e.toJSON());
		        }
		
		        result = eventArray.toString();
		
		        countlyStore_.removeEvents(events);
		
		        try {
		            result = java.net.URLEncoder.encode(result, "UTF-8");
		        } catch (UnsupportedEncodingException e) {
		            // should never happen because Android guarantees UTF-8 support
		        }
		
		        return result;
		    }

		- `countlyStore_.eventsList()`


### 2.1.3 init()方法
- 在获取对象之后，需要调用`init()`方法去初始化。注意最后俩个参数。`deviceID`是用来标识独立设备的一个id，`idMode`表示`deviceID`的生成策略。

	   public synchronized Countly init(final Context context, final String serverURL, final String appKey, final String deviceID, DeviceId.Type idMode) {
			//判断context是否有效
			//判断url是否有效
			//removing trailing '/'
			//判断 appKey 和deviceId 是否有效	
			//如果deviceID和idMode都为空,会自动为idMode设置值，优先考虑OPEN_UDID，其次是ADVERTISING_ID
			//判断sdk是否二次初始化且使用不同的值
			//检查是否能做CountlyMessaging相关的操作---这一块待分析
			//设置start级别。。。待分析
			//对app 应用名称进行检查是否是爬虫
			checkIfDeviceIsAppCrawler() 
			//判断是否首次初始化
			if (eventQueue_ == null) {
	            final CountlyStore countlyStore = new CountlyStore(context);
	
	            DeviceId deviceIdInstance;
	            if (deviceID != null) {
	                deviceIdInstance = new DeviceId(countlyStore, deviceID);
	            } else {
	                deviceIdInstance = new DeviceId(countlyStore, idMode);
	            }
	
	            deviceIdInstance.init(context, countlyStore, true);
	
	            connectionQueue_.setServerURL(serverURL);
	            connectionQueue_.setAppKey(appKey);
	            connectionQueue_.setCountlyStore(countlyStore);
	            connectionQueue_.setDeviceId(deviceIdInstance);
	
	            eventQueue_ = new EventQueue(countlyStore);
	
	            //do star rating related things
	            CountlyStarRating.registerAppSession(context, starRatingCallback_);
	        }
			//赋值`Countly`中的context_
			//赋值`ConnectionQueue`中的context
	    }

- `checkIfDeviceIsAppCrawler()`，会去判断deviceName是否是`Calypso AppCrawler`，好像是一个脚本的名称 然后在类`ConnectionProcessor`中发送数据时可以选择屏蔽这个deviceName的机器。[App crawler 参考](https://testerhome.com/wiki/appcrawler)

- 在首次初始化时 创建了`ConnectionQueue`和`EventQueue`,并传入一些参数。
	- 创建`CountlyStore`，这个类封装了sp用来处理`EventQueue`和`ConnectionQueue`中的数据。
	- 创建了`DeviceId`
		- 构造方法需要传入 开发者传入的deviceId或deviceId Type.会优先使用本地存储的deivceID和deivceId Type ...
		- `init`方法中如果检测到本地已经存在deviceId Type，会优先使用本地的。然后会根据deviceID Type 去生成对应的 deviceID。
	
- `OpenUDID`的生成,通过一个`OpenUDIDAdapter`类来管理`OpenUDID_manager`和`OpenUDID_service`。这个类使用反射来执行`OpenUDID_manager/OpenUDID_service`。(TODO待分析为什么采用这种方式).在调用`sync`方法之后，会去获取所有存在的`OPENUDID_service` 然后遍历这些service 去获取对应的`openudid`并存储到TreeMap中.重复的openudid会使得openudid值自增1，用来做大小的判断。 

	- `getMostFrequentOpenUDID()`会获取使用最多的openudid

- `EventQueue`会在`Countly.init()`方法中被创建

- `setServerURL`

## 2.2 触发点

### 2.2.1 Countly.onCreate(Activity activity)
获取了启动当前activity的intent，然后获取其data(Uri),保存到`DeviceInfo.deepLink`

	 Intent intent = activity.getIntent();
	        if (intent != null) {
	            Uri data = intent.getData();
	            if (data != null) {
	                if (sharedInstance().isLoggingEnabled()) {
	                    Log.d(Countly.TAG, "Data in activity created intent: " + data + " (appLaunchDeepLink " + sharedInstance().appLaunchDeepLink + ") ");
	                }
	                if (sharedInstance().appLaunchDeepLink) {
	                    DeviceInfo.deepLink = data.toString();
	                }
	            }
	        }

### 2.2.2 Countly.onStart()
标识Activity开始被记录

	 public synchronized void onStart(Activity activity) {
	        appLaunchDeepLink = false;
	        if (eventQueue_ == null) {
	            throw new IllegalStateException("init must be called before onStart");
	        }
	
	        ++activityCount_;
	        if (activityCount_ == 1) {
	            onStartHelper();
	        }
	
	        //check if there is an install referrer data
	        String referrer = ReferrerReceiver.getReferrer(context_);
	        if (Countly.sharedInstance().isLoggingEnabled()) {
	            Log.d(Countly.TAG, "Checking referrer: " + referrer);
	        }
	        if (referrer != null) {
	            connectionQueue_.sendReferrerData(referrer);
	            ReferrerReceiver.deleteReferrer(context_);
	        }
	
	        CrashDetails.inForeground();
	
	        if (autoViewTracker) {
	            recordView(activity.getClass().getName());
	        }
	
	        calledAtLeastOnceOnStart = true;
	    }

- `appLaunchDeepLink`,默认是true,执行onStart()之后就变成false.

- 通过判断`eventQueue_`是否为空 确定是否已经调用init()

- 用一个`int activityCount`记录当前activity的数量, 如果当前只有一个activity,会执行`onStartHelper()`

	- `onStartHelper()` 发送 `begin session event`,然后初始化`ConnectionQueue`的session tracking（根据判断条onTimer()中的判断条件来看，应该是activityCount控制的） 。`prevSessionDurationStartTime_`会被记录，表示session的开始时间。
	
			void onStartHelper() {
		        prevSessionDurationStartTime_ = System.nanoTime();
		        connectionQueue_.beginSession();
		    }

	- `beginSession`记录**session start event**,并发送。主要做的内容是 组装一条data，添加到`CountlyStore`，然后执行`tick()`
	
- `ReferrerReceiver`是一个广播类，会接收广播信息 并保存到本地..`Countly` 会收集这些缓存的信息并组装成一条data 发送出去同时删除缓存。

- `CrashDetails`是一个 记录手机状态的类。例如ram等等

- 判断是否需要自动trackview,会执行一个`recordView`方法，这个方法手动的记录一个view，然后会发送俩条数据，一条是activity开始(必发)，一条是上一个activity的持续时间(如果存在上一个activity,`reportViewDuration()`).这个方法可以提供给fragment ，Message Box or transparent activity 使用。

- `calledAtLeastOnceOnStart`字段用来判断 起码调用了一次`onStart（）`

### 2.2.3 Countly.onStop()
标识Activity停止

    public synchronized void onStop() {
        if (eventQueue_ == null) {
            throw new IllegalStateException("init must be called before onStop");
        }
        if (activityCount_ == 0) {
            throw new IllegalStateException("must call onStart before onStop");
        }

        --activityCount_;
        if (activityCount_ == 0) {
            onStopHelper();
        }

        CrashDetails.inBackground();

        //report current view duration
        reportViewDuration();
    }

- `activityCount_` 自减，当前activity为0时 会触发 `onStopHelper()`.
	- `onStopHelper()`中会调用`connectionQueue_.endSession`组装一条data 然后发送。然后重置`prevSessionDurationStartTime_`为0. 另外会判断`eventQueue_`中是否有缓存的的数据，如果有会取出放入`connectionQueue`去发送。

			void onStopHelper() {
		        connectionQueue_.endSession(roundedSecondsSinceLastSessionDurationUpdate());
		        prevSessionDurationStartTime_ = 0;
		
		        if (eventQueue_.size() > 0) {
		            connectionQueue_.recordEvents(eventQueue_.events());
		        }
		    }
	- `CrashDetails.inBackground()`记录当前没有activity，在后台

- `reportViewDuration`,记录上一个view到现在的持续时间.通过`lastView和lastViewStart`进行判断。


## 2.3 数据发送逻辑
`Countly`的数据发送逻辑在`ConnectionProcessor`中，它是一个runnable，被线程池管理。

	public void run(){
		while(true){
		//取出countlyStore中的 数据，进行判断是否存在未发送的，为空则停止
		//判断deviceId 非空
		//判断 待发送的 数据是否有 override_id 或 device_id　字段． 正常情况下都不会存在
		//判断当前设备是否是 App-Crawler 同时  是否需要忽略 App-Crawler这种情况。如果需要忽略，直接将这条数据从本地删除
		//如果当前设备不是爬虫，创建 URLConnection 并 connect，接着判断 返回码，只要是2xx 形式的 都是成功
		//如果连接成功，将当前这条数据从本地删除。。还会判断是否有device_id 会执行
		deviceId_.changeToDeveloperId(store_, newId);
		//如果返回码 是4xx的 ，同样会将数据删除
		//
		}
	}

### 2.3.1 数据中的device_id 和override_id
默认在`ConnectionProcessor`中才会将device_id这个字段添加到json中。但是又如下俩种例外情况

- device\_id 
	会在`Countly.changeDeviceId(deviceId)->connectionQueue_.changeDeviceId(deviceId,duration)`中被改变. 如果在cp 之前已经添加了这个字段，会判断这个值和本地的值是否相同，如果相同则发送的数据无改变，如果不同，会用old\_device\_id取代device\_id 组装一条新的json 并发送.并在发送成功之后改变本地device\_id的值。

- override\_id
	会在`Countly.changeDeviceId(type,deviceId)->connectionQueue_.endSession(duration,deviceIdOverride) `中被改变。 如果存在该字段，会将override\_id替换成device\_id

### 2.3.2 URLConnection对象的获取
在`ConnectionProcessor`中的`urlConnectionForEventData`方法中创建`URLConnection`

1. 为serverURL 添加`/i?`
2. 判断传入的 数据中是否含有`&crash=`且数据大小小于2048，直接添加 数据到url之后，同时计算数据的 `sha1`值 并添加到url 。 如果不含有`&crash=`或数据大小大于2048，仅添加数据的`sha1`值到url

		 if(!eventData.contains("&crash=") && eventData.length() < 2048) {
		            urlStr += eventData;
		            urlStr += "&checksum=" + sha1Hash(eventData + salt);
		        } else {
		            urlStr += "checksum=" + sha1Hash(eventData + salt);
		        }
3.  创建HttpUrlConnection,根据是否存在`publicKeyPinCertificates`和`certificatePinCertificates`进行判定。大概作用应该是 判断开发者是否传入了这俩块信息，即开发者使用了自定义的证书，那么在android 这里就需要跳过这些证书的验证。//TODO待分析 这一块的作用

        final URL url = new URL(urlStr);
        final HttpURLConnection conn;
        if (Countly.publicKeyPinCertificates == null && Countly.certificatePinCertificates == null) {
            conn = (HttpURLConnection)url.openConnection();
        } else {
            HttpsURLConnection c = (HttpsURLConnection)url.openConnection();
            c.setSSLSocketFactory(sslContext_.getSocketFactory());
            conn = c;
        }

4. 解析url获取一个`picturePath`参数

		String picturePath = UserData.getPicturePathFromQuery(url);

5. 判断`picturePath`是否非空，如果非空 则需要使用urlConneciton 去上传文件 。将数据写入请求体。[URLConnection高级用法](https://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests)

6. 如果`picturePath`为空，则去判断 待上传的数据 **是否包含crash信息**或**数据大小大于2048**或**需要强制使用post**。只要满足一个条件那就使用 post 请求，将数据写入请求体。否则 使用 get 去上传数据(不在`urlConnectionForEventData`方法中)

# 3.单元测试
gg...需要google play 在机子上