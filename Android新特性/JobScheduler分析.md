# JobScheduler分析

[理解JobScheduler机制-源码分析](http://gityuan.com/2017/03/10/job_scheduler_service/)

[JobScheduler的使用](https://www.jianshu.com/p/aa957774a965)

[Android JobScheduler的使用和原理](https://www.jianshu.com/p/55e16941bfbd)

[Android官方文档-JobScheduler](https://developer.android.com/reference/android/app/job/JobScheduler)

[Android官方文档-JobScheduler 文档](https://github.com/googlesamples/android-JobScheduler)


# 简介

在Android开发中,经常会遇到如下需求,在未来的某个时间点或者满足某中条件的情况下去执行指定的操作

1. 延迟执行,一般利用 闹钟管理器AlarmManager进行定时管理

2. 是否处于联网状态,充电状态,空闲状态,通常通过监听系统的相应广播来实现

	- 网络状态变化需要监听系统广播
	
			android.net.conn.CONNECTIVITY_CHANGE

	- 设备是否充电需要监听系统广播`Intent.ACTION_POWER_CONNECTED`

			android.intent.action.ACTION_POWER_CONNECTED；

	- 设备是否空闲需要监听系统广播`Intent.ACTION_SCREEN_OFF`

			android.intent.action.SCREEN_OFF；
 

在Android L 中,Google提供了JobSCheduler组件来帮助开发者实现.JobScheduler允许开发者创建在后台执行的job，当预置的条件被满足时，这些Job将会在后台被执行。

- `JobScheduler`集成了常见的几种运行条件,是开发者更快捷的实现之前需结合广播等多种操作的功能

`JobScheduler`的宗旨就是将一些不是特别紧急的任务放到更合适的时机进行批量处理,有俩个好处

1. 避免频繁唤醒硬件模块,从而导致电量消耗

2. 避免在不合适的时间执行任务(低电量,弱网络)


# 1. JobScheduler的使用

`JobScheduler`最低支持到`Android L`,目前并没有兼容包

1. `JobScheduler`: 任务调度器

2. `JobInfo`:任务概要信息,用来描述任务的执行时间,条件,策略等行为,使用Builder模式

3. `JobService`:任务服务,描述具体逻辑

	- **`JobService`是运行在应用的主线程中,不能执行耗时的操作**

	- `JobService`继承自`Service`,必须在`AndroidManifest`中声明

## 1.1 创建JobService

具体的业务逻辑写在这里

	public class JobSchedulerService extends JobService{

	  @Overrid
	  public boolean onStartJob(JobParameters params){
	    return false;
	  }
	
	  @Override
	  public boolean onStopJob(JobParameters params){
	    return false;
	  }
	}

- `onStartJob()`

	当系统触发执行Job时,会回调该Job的`onStartJob`方法,这个方法会返回一个布尔型的值

	- 返回值 = true:

		系统认为当前Job需要执行一个耗时的任务,即当`onStartJob()`这个方法结束的时候,耗时任务仍在异步执行. 那么当耗时任务结束时,需要手动调用`jobFinished(JobParameters params, boolean needRescheduled)`

		**`jobFinished()`必须调用,否则系统会阻塞`JobScheduler`的执行队列**

- `onStopJob()`

	当系统受到一个`cancel job`的请求,并且该job仍在执行,这是系统会回调`onStopJob()`方法

	也就是说在系统受到取消请求时，并不会一定会调用onStopJob方法，只有onStartJob返回true的时候，才会调用onStopJob，否则不调用。

	但不论是否调用onStopJob方法，系统受到取消请求时，都会取消该job

- `jobFinished()`

	`jobFinished(JobParameters params, boolean needRescheduled)`中的两个参数

	- 第一个参数`JobParameter`来自于`onStartJob(JobParameters params)`中的params，这也说明了如果我们想要在`onStartJob`中执行异步操作，必须要保存下来这个JobParameter。

## 1.2 创建JobScheduler

	JobScheduler mJobScheduler = (JobScheduler)getSystemService( Context.JOB_SCHEDULER_SERVICE );

	// 添加JobService
	mJobScheduler.schedule()

- `JobScheduler`的`schedule()`方法返回一个整型,失败时会返回一个负数,成功时会返回创建`JobInfo.Builder`时传入的`JobID`

- `JobScheduler`提供了两个方法来取消`Job`

	- `cancel(int jobId)`:该方法取消指定的Job

	- `cancelAll()`:取消所有的Job。



## 1.3 创建 JobInfo

Android 提供了`JobInfo.Builder`来设置`JobScheduler`的触发条件

`JobInfo.Builder`可以设置很多参数,但是有一些参数是互斥的,同时设置了之后可能抛出异常


1. `setMinimumLatency(long minLatencyMillis)`

	**这个方法指定Job至少要多少毫秒之后执行**

	比如`setMinimumLatency(5000)`,就表示这个Job在启动后至少要5秒之后执行，前五秒肯定是不会执行的。

	这个参数和`setPeriodic()`互斥,两个同时设置会抛出异常。

2. `setOverrideDeadline(long maxExecutionDelayMillis)`

	这个方法指定Job在某段时间之后必须执行

	即使设置的其他条件不满足(比如还设置了要求充电，连接wifi等条件)。这是一个严格准时的执行，比如`setOverrideDeadline(5000)`就表明这个Job在第五秒的时候会准时执行，而忽略其他的条件。

	这个方法也和`setPerioidc`互斥。

3. `setPersisted(boolean isPersisted)`

	设置是否在设备重启之后继续schedule该Job 

	此时需要声明权限`RECEIVE_BOOT_COMPLETED`，否则会报错`“java.lang.IllegalArgumentException: Error: requested job be persisted without holding RECEIVE_BOOT_COMPLETED permission.”` 
	
	而且`RECEIVE_BOOT_COMPLETED`需要在安装的时候就要声明，如果一开始没声明，而在升级时才声明，那么依然会报权限不足的错误。

4. `setRequiredNetworkType(int networkType)`

	设置启动指定Job时所需要的网络类型，一共有三个值
	
	1. `JobInfo.NETWORK_TYPE_NONE`:启动指定Job时不需要任何的网络连接；
	2. `JobInfo.NETWORK_TYPE_ANY`:启动指定Job时只要连着网就可以，不要求网络类型。
	3. `JobInfo.NETWORK_TYPE_UNMETERED`:启动指定Job时需要连接Wifi.

5. `setRequiresCharging(boolean requiresCharging)`

	设置执行Job时是否需要连接电源

6. `setRequiresDeviceIdle(boolean requiresDeviceIdle)`

	设置执行Job时是否需要机器处于空闲状态

7. `setPeriodic(long intervalMillis)`

	设置执行的间隔时间,单位毫秒

	该方法不能和`setMinimumLatency()`、`setOverrideDeadline()`这两个同时调用，否则会报错`“java.lang.IllegalArgumentException: Can't call setMinimumLatency() on a periodic job”`或者报错`“java.lang.IllegalArgumentException: Can't call setOverrideDeadline() on a periodic job”`

8. `setBackoffCriteria()`

	设置重试策略和等待时间

- 同时使用`setRequiredNetworkType(int networkType)`, `setRequiresCharging(boolean requireCharging) `和`setRequiresDeviceIdle(boolean requireIdle)`这三个条件，可能导致Job永远都不会执行。

	这个时候需要`setOverrideDeadline(long time)`来确保Job肯定能被执行一次
