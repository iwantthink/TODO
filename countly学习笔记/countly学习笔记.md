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
	- `onTimer()`会判断当前是否有活动的`application session`,如果有就会发送一个 `session hearbeat`。	
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
在获取对象之后，需要调用`init()`方法去初始化。注意最后俩个参数。`deviceID`是用来标识独立设备的一个id，`idMode`表示`deviceID`的生成策略。

	   public synchronized Countly init(final Context context, final String serverURL, final String appKey, final String deviceID, DeviceId.Type idMode) {
			//判断context是否有效
			//判断url是否有效
			//removing trailing '/'
			//判断 appKey 和deviceId 是否有效	
			//如果deviceID和idMode都为空,会自动为idMode设置值，优先考虑OPEN_UDID，其次是ADVERTISING_ID
			//判断sdk是否二次初始化，且使用不同的值
	    }