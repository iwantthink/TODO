# AlarmManager分析

[理解AlarmManager机制](http://gityuan.com/2017/03/12/alarm_manager_service/)

[定时任务Alarm的深入理解](https://juejin.im/post/5acc2a065188257ddb0fef9e)

[Android官方文档](https://developer.android.com/reference/android/app/AlarmManager)

[Android之AlarmManagerService(一)](http://www.robinheztto.com/2017/03/10/android-alarm-1/)

[SystemServer分析.md]()

# 1. 简介

Android 中的定时任务一般有两种实现方式，一种是`Timer`类，一种是使用Android的`Alarm`机制。这两种方式在多数情况下都能实现类似的效果，但是`Timer`有一个明显的短板，它并不适合用于那些需要长期在后台运行的定时任务。

为了能让电池更加耐用，各种手机都会有自己的休眠策略，Android手机就会在长时间不操作的情况下自动让CPU进入到休眠状态，这就有可能导致Timer的定时任务无法正常运行。而Alarm具有唤醒CPU的功能，它可以保证在大多数情况下需要执行的定时任务的时候CPU都能正常工作。注意唤醒CPU和唤醒屏幕完全不是一个概念。


- 从Android 4.4系统开始，Alarm任务的触发时间将会变得不准确，有可能会延迟一段时间后任务才能得到执行。但这并不是bug，而是系统在耗电性方面进行的优化。系统会自动检测目前有杜少Alarm任务存在，然后将触发时间相近的几个任务放在一起执行，这就可以大幅度的减少cpu被唤醒的次数，从而有效延长电池的使用时间。


# 2. AlarmManager的使用介绍

	PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_JOB_EXPIRED), 0);

	AlarmManager alarmManager=(AlarmManager)getSystemService(Service.ALARM_SERVICE);

	alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), pi);  

## 2.1 Alarm的类型

- `AlarmManager.RTC_WAKEUP`:

	使用系统绝对时间(当前系统时间，System.currentTimeMillis())，系统休眠状态也将唤醒系统。

- `AlarmManager.RTC`:

	使用系统绝对时间(当前系统时间，System.currentTimeMillis())，系统休眠状态下不可用。

- `AlarmManager.ELAPSED_REALTIME_WAKEUP`:

	使用系统相对时间(相对系统启动时间，SystemClock.elapsedRealtime())，系统休眠状态也将唤醒系统。

- `AlarmManager.ELAPSED_REALTIME`:

	使用系统相对时间(相对系统启动时间，SystemClock.elapsedRealtime())，系统休眠状态下不可用。

- `RTC/RTC_WAKEUP`和`ELAPSED_REALTIME/ELAPSED_REALTIME_WAKEUP`最大的差别就是RTC受time zone/locale的影响，可以通过修改手机时间触发闹钟事件，`ELAPSED_REALTIME/ELAPSED_REALTIME_WAKEUP`要通过真实时间的流逝，即使在休眠状态时间也会被计算。

- `WAKEUP`类型的Alarm会唤醒系统，休眠状态下会增加系统的功耗，所以在使用中应尽量避免使用该种类型的Alarm。

## 2.2 Alarm的Flag

- `FLAG_STANDALONE`:

	指定stand-alone精准alarm，该alarm不会被batch(批量处理)，设置`WINDOW_EXACT`的alarm会指定此flag。

- `FLAG_WAKE_FROM_IDLE`:

	指定alarm即使在idle模式也将唤醒系统，如alarm clock。

- `FLAG_ALLOW_WHILE_IDLE`:

	针对Doze模式，alarm即使在系统idle状态下也会执行，但是不会使系统退出idle mode，只有特殊alarm才需要标记该Flag。

- `FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED`:

	针对Doze模式，alarm即使在系统idle状态下也会执行而且没有时间限制，但是不会使系统退出idle mode，只有特殊alarm才需要标记该Flag。

- `FLAG_IDLE_UNTIL`:

	只有调用`AlarmManager.setIdleUntil()`接口才可能设置该flag，用来使系统进入idle mode直到`marker alarm`被执行，执行`marker alarm`时系统会退出`idle mode`(设置后进入DozeIdle状态让Alarm系统挂起，直到这个Alarm到期)。

## 2.3 Alarm的set方法

1. 非精准Alarm，其window被指定为`WINDOW_HEURISTIC`：

		public void set(int type, long triggerAtMillis, PendingIntent operation) {}
		public void set(int type, long triggerAtMillis, String tag, OnAlarmListener listener,Handler targetHandler) {}
		public void setRepeating(int type, long triggerAtMillis,long intervalMillis, PendingIntent operation) {}
		public void setInexactRepeating(int type, long triggerAtMillis,long intervalMillis, PendingIntent operation) {}
		// Doze模式下
		public void setAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) {}

2. 精准Alarm，其window被标记为`WINDOW_EXACT`：

		public void setWindow(int type, long windowStartMillis, long windowLengthMillis,PendingIntent operation) {}
		public void setWindow(int type, long windowStartMillis, long windowLengthMillis, String tag, OnAlarmListener listener, Handler targetHandler) {}
		public void setExact(int type, long triggerAtMillis, PendingIntent operation) {}   
		public void setExact(int type, long triggerAtMillis, String tag, OnAlarmListener listener, Handler targetHandler) {}  
		public void setAlarmClock(AlarmClockInfo info, PendingIntent operation) {}
		// Doze模式下
		public void setIdleUntil(int type, long triggerAtMillis, String tag, OnAlarmListener listener,Handler targetHandler) {}
		public void setExactAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) {}


# 3. AlarmManagerService的创建和启用

`AlarmManagerService`作为一个系统服务,在`SystemServer`类中就已经被创建并执行

	private void startOtherServices() {
	  ...
	  mSystemServiceManager.startService(AlarmManagerService.class);
	  ...
	}

- `SystemServiceManager`会将会创建一个`AlarmManagerService`对象,并回调其`onStart()`方法

	 在`onStart()`之后,`SYSTEM_SERVICES_READY`时,`onBootPhase()`将被回调

	具体分析过程查看[SystemServer分析.md]

## 3.1 构造函数

    final AlarmHandler mHandler = new AlarmHandler();

    public AlarmManagerService(Context context) {
        super(context);
		// 创建常量类,负责Alarm相关的常量读取以及更新
        mConstants = new Constants(mHandler);
    }

- `mHandler`作为成员变量,在`AlarmManagerService`对象创建时被创建,该Handler运行在`system_server`进程


## 3.1.1 创建Constants

	private final class Constants extends ContentObserver {
	    public Constants(Handler handler) {
	        super(handler);
			// 更新Doze IDLE下flag为 ALLOW_WHILE_IDLE Alarm的执行时间
	        updateAllowWhileIdleMinTimeLocked();
	        updateAllowWhileIdleWhitelistDurationLocked();
	    }
	
	    public void updateAllowWhileIdleMinTimeLocked() {
	        mAllowWhileIdleMinTime = mPendingIdleUntil != null
	                ? ALLOW_WHILE_IDLE_LONG_TIME : ALLOW_WHILE_IDLE_SHORT_TIME;
	    }
	
	    public void updateAllowWhileIdleWhitelistDurationLocked() {
	        if (mLastAllowWhileIdleWhitelistDuration != ALLOW_WHILE_IDLE_WHITELIST_DURATION) {
	            mLastAllowWhileIdleWhitelistDuration = ALLOW_WHILE_IDLE_WHITELIST_DURATION;
	            BroadcastOptions opts = BroadcastOptions.makeBasic();
	            //设置为10s
	            opts.setTemporaryAppWhitelistDuration(ALLOW_WHILE_IDLE_WHITELIST_DURATION);
	            mIdleOptions = opts.toBundle();
	        }
	    }
	    // 注册监听Settings数据库的变化并更新
	    public void start(ContentResolver resolver) {
	        mResolver = resolver;
	        mResolver.registerContentObserver(Settings.Global.getUriFor(
	                Settings.Global.ALARM_MANAGER_CONSTANTS), false, this);
	        updateConstants();
	    }
	    ...
	}

- 当系统处于`idle`状态,`alarm`最小时间间隔为9min. 当系统处于非`idle`状态,最小间隔时间为5s


## 3.2 AlarmManagerService.onStart()


    @Override
    public void onStart() {
		// native层初始化
        mNativeData = init();
		// mNextWakeup - 下一个包含wakeup alarm的batch的start时间,
		// mNextNonWakeup - 下一个非wakeup的batch的start时间
        mNextWakeup = mNextNonWakeup = 0;

		// 由于重启后内核并没有保存时区信息,必须将当前时区设置到内核
		// 时区如果发生变化,会发送相应的广播
        setTimeZoneImpl(SystemProperties.get(TIMEZONE_PROPERTY));

        // Also sure that we're booting with a halfway sensible current time
        if (mNativeData != 0) {
            final long systemBuildTime = Environment.getRootDirectory().lastModified();
            if (System.currentTimeMillis() < systemBuildTime) {
                Slog.i(TAG, "Current time only " + System.currentTimeMillis()
                        + ", advancing to build time " + systemBuildTime);
                setKernelTime(mNativeData, systemBuildTime);
            }
        }

        // Determine SysUI's uid
        final PackageManager packMan = getContext().getPackageManager();
        try {
            PermissionInfo sysUiPerm = packMan.getPermissionInfo(SYSTEM_UI_SELF_PERMISSION, 0);
            ApplicationInfo sysUi = packMan.getApplicationInfo(sysUiPerm.packageName, 0);
			// 判断应用是否拥有特殊权限
            if ((sysUi.privateFlags&ApplicationInfo.PRIVATE_FLAG_PRIVILEGED) != 0) {
                mSystemUiUid = sysUi.uid;
            } else {
                Slog.e(TAG, "SysUI permission " + SYSTEM_UI_SELF_PERMISSION
                        + " defined by non-privileged app " + sysUi.packageName
                        + " - ignoring");
            }
        } catch (NameNotFoundException e) {
        }

		// 初始化wakeLock
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "*alarm*");

		// 获取一个 之后会执行发送广播的 PendingIntent
		// 广播action是  ACTION_TIME_TICK
        mTimeTickSender = PendingIntent.getBroadcastAsUser(getContext(), 0,
                new Intent(Intent.ACTION_TIME_TICK).addFlags(
                        Intent.FLAG_RECEIVER_REGISTERED_ONLY
                        | Intent.FLAG_RECEIVER_FOREGROUND
                        | Intent.FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS), 0,
                        UserHandle.ALL);

        Intent intent = new Intent(Intent.ACTION_DATE_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING
                | Intent.FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS);

		// 获取一个 之后会执行发送广播的 PendingIntent
		// 广播action是  ACTION_DATE_CHANGED
        mDateChangeSender = PendingIntent.getBroadcastAsUser(getContext(), 0, intent,
                Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT, UserHandle.ALL);
        
        // 时间/日期变换广播
		// 监听ACTION_TIME_TICK,ACTION_DATE_CHANGED 的动态注册的广播
        mClockReceiver = new ClockReceiver();
		// 首次调度一次,之后每俩分钟执行一次
        mClockReceiver.scheduleTimeTickEvent();
		// 执行一次当日期改变时的逻辑
        mClockReceiver.scheduleDateChangedEvent();
		// 监听亮屏/灭屏的广播
        mInteractiveStateReceiver = new InteractiveStateReceiver();
		// 监听package移除/重启,sdcard不可用的广播
        mUninstallReceiver = new UninstallReceiver();
        
		// Native初始化成功,运行AlarmThread
        if (mNativeData != 0) {
            AlarmThread waitThread = new AlarmThread();
            waitThread.start();
        } else {
        }

        try {
			// 监听应用退出,用来移除Alarm
            ActivityManager.getService().registerUidObserver(new UidObserver(),
                    ActivityManager.UID_OBSERVER_IDLE, ActivityManager.PROCESS_STATE_UNKNOWN, null);
        } catch (RemoteException e) {
            // ignored; both services live in system_server
        }

		// 发布binderservice 与 localservice ,分别供其他进程与system_server内部服务调用
        publishBinderService(Context.ALARM_SERVICE, mService);
        publishLocalService(LocalService.class, new LocalService());
    }


### 3.2.1 AlarmManagerService.init()

本地方法,创建`Alarm`驱动对象等

### 3.2.2 创建ClockReceiver

    class ClockReceiver extends BroadcastReceiver {
        public ClockReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_DATE_CHANGED);
            getContext().registerReceiver(this, filter);
        }

		..................
	}

- 动态注册了监听 `ACTION_TIME_TICK`和 `ACTION_DATE_CHANGED`的广播

### 3.2.3 ClockReceiver.scheduleTimeTickEvent()

        public void scheduleTimeTickEvent() {
            final long currentTime = System.currentTimeMillis();
			// 忽略当前的秒数
            final long nextTime = 60000 * ((currentTime / 60000) + 1);

            // 距离下一分钟的间隔时间
            final long tickEventDelay = nextTime - currentTime;

            final WorkSource workSource = null; // Let system take blame for time tick events.
            setImpl(ELAPSED_REALTIME, SystemClock.elapsedRealtime() + tickEventDelay, 0,
                    0, mTimeTickSender, null, null, AlarmManager.FLAG_STANDALONE, workSource,
                    null, Process.myUid(), "android");
        }

### 3.2.4 AlarmManagerService.setImpl()

    void setImpl(int type, long triggerAtTime, long windowLength, long interval,
            PendingIntent operation, IAlarmListener directReceiver, String listenerTag,
            int flags, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock,
            int callingUid, String callingPackage) {

	    // PendingIntent与AlarmListener不能同时设置或同时为空
	    if ((operation == null && directReceiver == null)
	            || (operation != null && directReceiver != null)) {
	        Slog.w(TAG, "Alarms must either supply a PendingIntent or an AlarmReceiver");
	        return;
	    }
	
	    // windowLength时间大于INTERVAL_HALF_DAY时，设置为INTERVAL_HOUR
	    if (windowLength > AlarmManager.INTERVAL_HALF_DAY) {
	        Slog.w(TAG, "Window length " + windowLength
	                + "ms suspiciously long; limiting to 1 hour");
	        windowLength = AlarmManager.INTERVAL_HOUR;
	    }
	
	    // Repeating alarm的Repeat周期时间最短为MIN_INTERVAL即1s
	    final long minInterval = mConstants.MIN_INTERVAL;
	    if (interval > 0 && interval < minInterval) {
	        Slog.w(TAG, "Suspiciously short interval " + interval
	                + " millis; expanding to " + (minInterval/1000)
	                + " seconds");
	        interval = minInterval;
	    }
	
	    // 检查定时器类型的合法性
	    if (type < RTC_WAKEUP || type > RTC_POWEROFF_WAKEUP) {
	        throw new IllegalArgumentException("Invalid alarm type " + type);
	    }
	
	    // 检查triggerAtTime触发时间的合法性
	    if (triggerAtTime < 0) {
	        triggerAtTime = 0;
	    }

		// currentTimeMillis() 指的是 从纪元开始到现在的时间
		// 获取系统启动到现在的时间 , 即相对时间
        final long nowElapsed = SystemClock.elapsedRealtime();
		// 转换闹钟触发的时间,仅针对type= RTC| type= RTC_WAKEUP,除此之外直接返回
		// 将触发绝对时间转换成相对时间
        final long nominalTrigger = convertToElapsed(triggerAtTime, type);

		// 防止定时器滥用,设置最小触发时间是 当前流逝时间+五秒
		// 相对时间
        final long minTrigger = nowElapsed + mConstants.MIN_FUTURITY;
		// 计算触发时间，当传入的触发时间大于最小触发时间时，使用传入触发时间，否则使用最小触发时间
        final long triggerElapsed = (nominalTrigger > minTrigger) ? nominalTrigger : minTrigger;

        final long maxElapsed;
        if (windowLength == AlarmManager.WINDOW_EXACT) {
			// 精确alarm，最大触发时间即为上面计算的触发时间
            maxElapsed = triggerElapsed;
        } else if (windowLength < 0) {
	        // 非精准alarm，计算最大触发时间
            maxElapsed = maxTriggerTime(nowElapsed, triggerElapsed, interval);
            // 根据最大触发时间修正alarm的时间窗时间
            windowLength = maxElapsed - triggerElapsed;
        } else {
			// 如果时间窗口>0，最大触发时间设置为触发时间加时间窗口
            maxElapsed = triggerElapsed + windowLength;
        }

        synchronized (mLock) {
            setImplLocked(type, triggerAtTime, triggerElapsed, windowLength, maxElapsed,
                    interval, operation, directReceiver, listenerTag, flags, true, workSource,
                    alarmClock, callingUid, callingPackage);
        }
    }

	// 非精准alarm，计算最大触发时间
	static final long MIN_FUZZABLE_INTERVAL = 10000;
	static long maxTriggerTime(long now, long triggerAtTime, long interval) {
	    long futurity = (interval == 0)
	            ? (triggerAtTime - now)
	            : interval;
	    // 如果下一次触发的时间小于10秒，最大触发时间即为当前触发时间        
	    if (futurity < MIN_FUZZABLE_INTERVAL) {
	        futurity = 0;
	    }
	    // 下一次触发的时间大于10秒，把0.75倍的下次触发时间+当前触发时间作为alarm的最大触发时间maxElapsed
	    return triggerAtTime + (long)(.75 * futurity);
	}

### 3.2.5 ALMS.setImplLocked()

    private void setImplLocked(int type, long when, long whenElapsed, long windowLength,
            long maxWhen, long interval, PendingIntent operation, IAlarmListener directReceiver,
            String listenerTag, int flags, boolean doValidate, WorkSource workSource,
            AlarmManager.AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
		// 创建Alarm 对象,将所有请求参数封装到该对象中
        Alarm a = new Alarm(type, when, whenElapsed, windowLength, maxWhen, interval,
                operation, directReceiver, listenerTag, workSource, flags, alarmClock,
                callingUid, callingPackage);
        try {
            if (ActivityManager.getService().isAppStartModeDisabled(callingUid, callingPackage)) {
                return;
            }
        } catch (RemoteException e) {
        }
		// set Alarm之前,先移除前面设置的alarm
        removeLocked(operation, directReceiver);
		// 继续设置Alarm
        setImplLocked(a, false, doValidate);
    }

### 3.2.6 ALMS.setImplLocked()--2

	private void setImplLocked(Alarm a, boolean rebatching, boolean doValidate) {
		// 该定时器是Doze Idle定时器
	    if ((a.flags&AlarmManager.FLAG_IDLE_UNTIL) != 0) {
			// 如果有设置FLAG_WAKE_FROM_IDLE的Alarm mNextWakeFromIdle(第一次为null)且IDLE_UNTIL触发时间比它要长
			// 设置IDLE_UNTIL Alarm的触发时间为mNextWakeFromIdle触发时间
	        if (mNextWakeFromIdle != null && a.whenElapsed > mNextWakeFromIdle.whenElapsed) {
	            a.when = a.whenElapsed = a.maxWhenElapsed = mNextWakeFromIdle.whenElapsed;
	        }
			
			// 将IDLE_UNTIL的触发时间随机提前一点
	        final long nowElapsed = SystemClock.elapsedRealtime();
			// 计算fuzz , 用来取随机值
	        final int fuzz = fuzzForDuration(a.whenElapsed-nowElapsed);
	        if (fuzz > 0) {
	            if (mRandom == null) {
	                mRandom = new Random();
	            }

	            final int delta = mRandom.nextInt(fuzz);
				// 将delta触发时间提前
	            a.whenElapsed -= delta;
	            a.when = a.maxWhenElapsed = a.whenElapsed;
	        }
	
	    } else if (mPendingIdleUntil != null) {
	        // 当前已经处于Doze Idle状态，除了带FLAG_ALLOW_WHILE_IDLE，FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED
	        // FLAG_WAKE_FROM_IDLE flag的Alarm可以设置外，其他的Alarm被加入到mPendingWhileIdleAlarms后退出

	        if ((a.flags&(AlarmManager.FLAG_ALLOW_WHILE_IDLE
	                | AlarmManager.FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED
	                | AlarmManager.FLAG_WAKE_FROM_IDLE))
	                == 0) {
	            mPendingWhileIdleAlarms.add(a);
	            return;
	        }
	    }
	
		// FLAG_STANDALONE标志放到单独batch中，非FLAG_STANDALONE的在mAlarmBatches中查找合适的batch
	    int whichBatch = ((a.flags&AlarmManager.FLAG_STANDALONE) != 0)
	            ? -1 : attemptCoalesceLocked(a.whenElapsed, a.maxWhenElapsed);

		//　没有找到合适的batch，新建batch并加入到mAlarmBatches     
	    if (whichBatch < 0) {
	        Batch batch = new Batch(a);
	        addBatchLocked(mAlarmBatches, batch);
	    } else {
			// 找到了合适的batch，添加到batch中         
        	Batch batch = mAlarmBatches.get(whichBatch);
        	// 添加alarm，并更新了batch触发时间，需要对mAlarmBatches里面的所有batch进行排序
        	if (batch.add(a)) {
            	mAlarmBatches.remove(whichBatch);
            	addBatchLocked(mAlarmBatches, batch);
        	}
	    }

	    if (a.alarmClock != null) {
	        mNextAlarmClockMayChange = true;
	    }
	
	    boolean needRebatch = false;
	
	    if ((a.flags&AlarmManager.FLAG_IDLE_UNTIL) != 0) {
			// 设置 mPendingIntent , 即代表进入 Doze IDLE
	        mPendingIdleUntil = a;
	        mConstants.updateAllowWhileIdleMinTimeLocked();
			// 需要rebatch 所有的alarm
	        needRebatch = true;
	    } else if ((a.flags&AlarmManager.FLAG_WAKE_FROM_IDLE) != 0) {
			// 更新mNextWakeFromIdle
	        if (mNextWakeFromIdle == null || mNextWakeFromIdle.whenElapsed > a.whenElapsed) {
	            mNextWakeFromIdle = a;
	            if (mPendingIdleUntil != null) {
	                needRebatch = true;
	            }
	        }
	    }
	
		// 当前不在 rebatch
	    if (!rebatching) {
	        if (needRebatch) {
	            //需要对所有alarm重新进行batch操作
	            rebatchAllAlarmsLocked(false);
	        }
			// 重新设置几个核心alarm
	        rescheduleKernelAlarmsLocked();
	        //重新计算下一个alarm的alarmclock
	        updateNextAlarmClockLocked();
	    }
	}

- `setImplLocked()`中首先对`FLAG_IDLE_UNTIL`的`alarm`进行处理，然后根据`mPendingIdleUntil`是否null判断当前是否是`Doze Idle`模式，`Doze Idle`模式下会pending所有非`FLAG_ALLOW_WHILE_IDLE`，`FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED`，`FLAG_WAKE_FROM_IDLE` flag的alarm，然后添加alarm合适的batch中，最后调度设置alarm到kernel中。当涉及到`FLAG_IDLE_UNTIL`进入`Doze Idle`，或`FLAG_WAKE_FROM_IDLE Doze`下更新Alarm时，需重新进行rebatch操作



#### 3.2.6.1 补充

	static int fuzzForDuration(long duration) {
	    if (duration < 15*60*1000) {
	        // 小于15分钟，返回实际时间
	        return (int)duration;
	    } else if (duration < 90*60*1000) {
	        // 小于90分钟，最多提前15分钟
	        return 15*60*1000;
	    } else {
	        // 90分钟以上，最多提前半小时
	        return 30*60*1000;
	    }
	}
	
	// 根据触发时间与最长触发时间从mAlarmBatches中找合适的batch，找到则返回index，否则返回-1
	int attemptCoalesceLocked(long whenElapsed, long maxWhen) {
	    final int N = mAlarmBatches.size();
	    for (int i = 0; i < N; i++) {
	        Batch b = mAlarmBatches.get(i);
	        if ((b.flags&AlarmManager.FLAG_STANDALONE) == 0 && b.canHold(whenElapsed, maxWhen)) {
	            return i;
	        }
	    }
	    return -1;
	}
	
	final class Batch {
	    long start;
	    long end;
	    ......
	    // 根据Alarm的触发时间及最大触发时间，与batch的触发时间与最大触发时间是否有重合，有重合即可以合入该batch
	    boolean canHold(long whenElapsed, long maxWhen) {
	        return (end >= whenElapsed) && (start <= maxWhen);
	    }
	
	    // 添加Alarm，并根据Alarm的触发时间判断是否需要更新batch时间，如果更新了batch时间则返回true，对batch重新排序
	    boolean add(Alarm alarm) {
	        boolean newStart = false;
	        // narrows the batch if necessary; presumes that canHold(alarm) is true
	        int index = Collections.binarySearch(alarms, alarm, sIncreasingTimeOrder);
	        if (index < 0) {
	            index = 0 - index - 1;
	        }
	        alarms.add(index, alarm);
	        // 如果Alarm的触发时间比batch的触发时间要早，则更新整个batch的触发时间
	        if (alarm.whenElapsed > start) {
	            start = alarm.whenElapsed;
	            newStart = true;
	        }
	        if (alarm.maxWhenElapsed < end) {
	            end = alarm.maxWhenElapsed;
	        }
	        flags |= alarm.flags;
	        return newStart;
	    }    
	    ......
	}
	
	//将mAlarmBatches按照升序排序
	static boolean addBatchLocked(ArrayList<Batch> list, Batch newBatch) {
	    int index = Collections.binarySearch(list, newBatch, sBatchOrder);
	    if (index < 0) {
	        index = 0 - index - 1;
	    }
	    list.add(index, newBatch);
	    return (index == 0);
	}

	
## 3.3 AlarmThread 分析

    private class AlarmThread extends Thread
    {
        public AlarmThread()
        {
            super("AlarmManager");
        }
        
        public void run()
        {
            ArrayList<Alarm> triggerList = new ArrayList<Alarm>();

            while (true)
            {
				// 在waitForAlarm中阻塞等待Alarm的触发
                int result = waitForAlarm(mNativeData);
				// 记录触发时间mLastWakeup
                mLastWakeup = SystemClock.elapsedRealtime();
				// 清空列表
                triggerList.clear();
				// 获取当前绝对时间
                final long nowRTC = System.currentTimeMillis();
				// 获取当前相对时间
                final long nowELAPSED = SystemClock.elapsedRealtime();

				// 首先判断是否是时间改变事件
                if ((result & TIME_CHANGED_MASK) != 0) {
                    // The kernel can give us spurious time change notifications due to
                    // small adjustments it makes internally; we want to filter those out.
                    final long lastTimeChangeClockTime;
                    final long expectedClockTime;
                    synchronized (mLock) {
                        lastTimeChangeClockTime = mLastTimeChangeClockTime;
                        expectedClockTime = lastTimeChangeClockTime
                                + (nowELAPSED - mLastTimeChangeRealtime);
                    }

	                // 时间变化至少是 +/- 1000 ms或者是第一次改变才进行处理
                    if (lastTimeChangeClockTime == 0 || nowRTC < (expectedClockTime-1000)
                            || nowRTC > (expectedClockTime+1000)) {

						// 由于时间变化，所以要重新批处理所有的alarm
                        removeImpl(mTimeTickSender);
                        removeImpl(mDateChangeSender);
                        rebatchAllAlarms();
                        mClockReceiver.scheduleTimeTickEvent();
                        mClockReceiver.scheduleDateChangedEvent();
                        synchronized (mLock) {
                            mNumTimeChanged++;
                            mLastTimeChangeClockTime = nowRTC;
                            mLastTimeChangeRealtime = nowELAPSED;
                        }
                        Intent intent = new Intent(Intent.ACTION_TIME_CHANGED);
                        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING
                                | Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT
                                | Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND
                                | Intent.FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS);
                        getContext().sendBroadcastAsUser(intent, UserHandle.ALL);

                        // The world has changed on us, so we need to re-evaluate alarms
                        // regardless of whether the kernel has told us one went off.
                        result |= IS_WAKEUP_MASK;
                    }
                }

                if (result != TIME_CHANGED_MASK) {
                    // If this was anything besides just a time change, then figure what if
                    // anything to do about alarms.
                    synchronized (mLock) {

                        if (WAKEUP_STATS) {
                            if ((result & IS_WAKEUP_MASK) != 0) {
                                long newEarliest = nowRTC - RECENT_WAKEUP_PERIOD;
                                int n = 0;
                                for (WakeupEvent event : mRecentWakeups) {
                                    if (event.when > newEarliest) break;
                                    n++; // number of now-stale entries at the list head
                                }
                                for (int i = 0; i < n; i++) {
                                    mRecentWakeups.remove();
                                }

                                recordWakeupAlarms(mAlarmBatches, nowELAPSED, nowRTC);
                            }
                        }

						// 从mAlarmBatches中获取触发的batch的alarm并加入到triggerList中，返回是否有wakeup的alarm
                        boolean hasWakeup = triggerAlarmsLocked(triggerList, nowELAPSED, nowRTC);
						// 没有wakeup类型的alarm，在灭屏情况下加入到mPendingNonWakeupAlarms延迟执行
                        if (!hasWakeup && checkAllowNonWakeupDelayLocked(nowELAPSED)) {
                           
                            if (mPendingNonWakeupAlarms.size() == 0) {
                                mStartCurrentDelayTime = nowELAPSED;
                                mNextNonWakeupDeliveryTime = nowELAPSED
                                        + ((currentNonWakeupFuzzLocked(nowELAPSED)*3)/2);
                            }
                            mPendingNonWakeupAlarms.addAll(triggerList);
                            mNumDelayedAlarms += triggerList.size();
                            rescheduleKernelAlarmsLocked();
                            updateNextAlarmClockLocked();
                        } else {
                       		// 如果有wakeup类型的alarm，将mPendingNonWakeupAlarms加入到triggerList
                            rescheduleKernelAlarmsLocked();
                            updateNextAlarmClockLocked();
                            if (mPendingNonWakeupAlarms.size() > 0) {
                                calculateDeliveryPriorities(mPendingNonWakeupAlarms);
                                triggerList.addAll(mPendingNonWakeupAlarms);
                                Collections.sort(triggerList, mAlarmDispatchComparator);
                                final long thisDelayTime = nowELAPSED - mStartCurrentDelayTime;
                                mTotalDelayTime += thisDelayTime;
                                if (mMaxDelayTime < thisDelayTime) {
                                    mMaxDelayTime = thisDelayTime;
                                }
                                mPendingNonWakeupAlarms.clear();
                            }
							// 执行alarm
                            deliverAlarmsLocked(triggerList, nowELAPSED);
                        }
                    }

                } else {
                    // Just in case -- even though no wakeup flag was set, make sure
                    // we have updated the kernel to the next alarm time.
                    synchronized (mLock) {
                        rescheduleKernelAlarmsLocked();
                    }
                }
            }
        }
    }

- 在`AlarmThread`中开启一个死循环,不断的等待Alarm的触发

- `AlarmThread`线程通过`waitForAlarm()`阻塞等待定时器触发，如果是时间改变事件，则发送时间改变通知并重新设置定时器。如果不是时间改变，则将`mAlarmBatches`中的首个`batch`的`alarm`加入到`triggerList`中，如果触发的alarm中没有`wakeup`类型，加入到`mPendingNonWakeupAlarms`中等下次执行，如果有`wakeup`类型的`alarm`，则调用`deliverAlarmsLocked`执行。




### 3.3.1 AlarmManagerService.deliverAlarmsLocked()


    void deliverAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED) {
        mLastAlarmDeliveryTime = nowELAPSED;
        for (int i=0; i<triggerList.size(); i++) {
            Alarm alarm = triggerList.get(i);
            final boolean allowWhileIdle = (alarm.flags&AlarmManager.FLAG_ALLOW_WHILE_IDLE) != 0;
            try {
             
				.........................
                mDeliveryTracker.deliverLocked(alarm, nowELAPSED, allowWhileIdle);
            } catch (RuntimeException e) {
            }
        }
    }

### 3.3.2 AlarmManagerService.deliverLocked()

	public void deliverLocked(Alarm alarm, long nowELAPSED, boolean allowWhileIdle) {
	    if (alarm.operation != null) {
	        // operation即传入的PendingIntent 
			// 这里就是执行PendingIntent操作
	        alarm.operation.send(getContext(), 0,
	                mBackgroundIntent.putExtra(
	                    Intent.EXTRA_ALARM_COUNT, alarm.count),
	                    mDeliveryTracker, mHandler, null,
	                    allowWhileIdle ? mIdleOptions : null);
	    } else {
	       
	       alarm.listener.doAlarm(this);
	       // 5s的超时时长
	       mHandler.sendMessageDelayed(
	               mHandler.obtainMessage(AlarmHandler.LISTENER_TIMEOUT,
	                       alarm.listener.asBinder()),
	               mConstants.LISTENER_TIMEOUT);
	    }
	    //alarm正在触发
	    final InFlight inflight = new InFlight(AlarmManagerService.this,
	            alarm.operation, alarm.listener, alarm.workSource, alarm.uid,
	            alarm.packageName, alarm.type, alarm.statsTag, nowELAPSED);
	    mInFlight.add(inflight);
	    mBroadcastRefCount++;
	    qcNsrmExt.addTriggeredUid((alarm.operation != null) ?
	                            alarm.operation.getCreatorUid() :
	                            alarm.uid);
	    ...
	}

### 3.3.3 PendingIntent.send()

    public void send(Context context, int code, @Nullable Intent intent,
            @Nullable OnFinished onFinished, @Nullable Handler handler,
            @Nullable String requiredPermission, @Nullable Bundle options)
            throws CanceledException {
        try {
            String resolvedType = intent != null ?
                    intent.resolveTypeIfNeeded(context.getContentResolver())
                    : null;

			// 跳转到 ActivityManagerService
            int res = ActivityManager.getService().sendIntentSender(
                    mTarget, mWhitelistToken, code, intent, resolvedType,
                    onFinished != null
                            ? new FinishedDispatcher(this, onFinished, handler)
                            : null,
                    requiredPermission, options);
			....................
        } catch (RemoteException e) {
            throw new CanceledException(e);
        }
    }

- 这里的`mTarget`就是`PendingIntentRecord`,可以通过[PendingIntent分析.md]()得知

### 3.3.4 AlarmManagerService.sendIntentSender()

    @Override
    public int sendIntentSender(IIntentSender target, IBinder whitelistToken, int code,
            Intent intent, String resolvedType,
            IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {

        if (target instanceof PendingIntentRecord) {
            return ((PendingIntentRecord)target).sendWithResult(code, intent, resolvedType,
                    whitelistToken, finishedReceiver, requiredPermission, options);
        } else {
            if (intent == null) {
                intent = new Intent(Intent.ACTION_MAIN);
            }
            try {
                target.send(code, intent, resolvedType, whitelistToken, null,
                        requiredPermission, options);
            } catch (RemoteException e) {
            }

            if (finishedReceiver != null) {
                try {
                    finishedReceiver.performReceive(intent, 0,
                            null, null, false, false, UserHandle.getCallingUserId());
                } catch (RemoteException e) {
                }
            }
            return 0;
        }
    }

### 3.3.5 PendingIntentRecord.sendWithResult()


    public int sendWithResult(int code, Intent intent, String resolvedType, IBinder whitelistToken,
            IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        return sendInner(code, intent, resolvedType, whitelistToken, finishedReceiver,
                requiredPermission, null, null, 0, 0, 0, options);
    }

### 3.3.6 PendingIntentRecord.sendInner()

    int sendInner(int code, Intent intent, String resolvedType, IBinder whitelistToken,
            IIntentReceiver finishedReceiver,
            String requiredPermission, IBinder resultTo, String resultWho, int requestCode,
            int flagsMask, int flagsValues, Bundle options) {

		..........省略...........
        synchronized (owner) {
            if (!canceled) {
                sent = true;
				.........省略........

                switch (key.type) {
                    case ActivityManager.INTENT_SENDER_ACTIVITY:
                        if (options == null) {
                            options = key.options;
                        } else if (key.options != null) {
                            Bundle opts = new Bundle(key.options);
                            opts.putAll(options);
                            options = opts;
                        }
                        try {
                            if (key.allIntents != null && key.allIntents.length > 1) {

								.........省略............
								// 核心方法
                                owner.startActivitiesInPackage(uid, key.packageName, allIntents,
                                        allResolvedTypes, resultTo, options, userId);
                            } else {
                                owner.startActivityInPackage(uid, key.packageName, finalIntent,
                                        resolvedType, resultTo, resultWho, requestCode, 0,
                                        options, userId, null, "PendingIntentRecord");
                            }
                        } catch (RuntimeException e) {
                        }
                        break;
                    case ActivityManager.INTENT_SENDER_ACTIVITY_RESULT:
                        final ActivityStack stack = key.activity.getStack();
                        if (stack != null) {
							// 核心方法
                            stack.sendActivityResultLocked(-1, key.activity, key.who,
                                    key.requestCode, code, finalIntent);
                        }
                        break;
                    case ActivityManager.INTENT_SENDER_BROADCAST:
                        try {
							// 核心方法
                            int sent = owner.broadcastIntentInPackage(key.packageName, uid,
                                    finalIntent, resolvedType, finishedReceiver, code, null, null,
                                    requiredPermission, options, (finishedReceiver != null),
                                    false, userId);
                            if (sent == ActivityManager.BROADCAST_SUCCESS) {
                                sendFinish = false;
                            }
                        } catch (RuntimeException e) {
                        }
                        break;
                    case ActivityManager.INTENT_SENDER_SERVICE:
                    case ActivityManager.INTENT_SENDER_FOREGROUND_SERVICE:
                        try {
							// 核心方法
                            owner.startServiceInPackage(uid, finalIntent, resolvedType,
                                    key.type == ActivityManager.INTENT_SENDER_FOREGROUND_SERVICE,
                                    key.packageName, userId);
                        } catch (RuntimeException e) {
                            Slog.w(TAG, "Unable to send startService intent", e);
                        } catch (TransactionTooLargeException e) {
                            res = ActivityManager.START_CANCELED;
                        }
                        break;
                }

				........省略代码.........
                return res;
            }
        }
        return ActivityManager.START_CANCELED;
    }


- `INTENT_SENDER_ACTIVITY`: 则执行startActivitiesInPackage

- `INTENT_SENDER_ACTIVITY_RESULT`: 则执行sendActivityResultLocked

- `INTENT_SENDER_SERVICE`: 则执行startServiceInPackage

- `INTENT_SENDER_BROADCAST`: 则执行broadcastIntentInPackage


## 3.4 AlarmManagerService.onBootPhase()

    @Override
    public void onBootPhase(int phase) {
        if (phase == PHASE_SYSTEM_SERVICES_READY) {
			// 监听设置常量变化
            mConstants.start(getContext().getContentResolver());
			// 权限判断服务,通常是系统应用使用
            mAppOps = (AppOpsManager) getContext().getSystemService(Context.APP_OPS_SERVICE);
			// 本地服务
            mLocalDeviceIdleController
                    = LocalServices.getService(DeviceIdleController.LocalService.class);
        }
    }


# 4. AlarmManager的获取与使用

        registerService(Context.ALARM_SERVICE, AlarmManager.class,
                new CachedServiceFetcher<AlarmManager>() {
            @Override
            public AlarmManager createService(ContextImpl ctx) throws ServiceNotFoundException {
                IBinder b = ServiceManager.getServiceOrThrow(Context.ALARM_SERVICE);
                IAlarmManager service = IAlarmManager.Stub.asInterface(b);
                return new AlarmManager(service, ctx);
            }});

- `AlarmManager`的注册过程在`SystemServiceRegistry`的静态代码块中

- 由此可知,通过`getSystemService(Service.ALARM_SERVICE)`获取的是`AlarmManager`对象

## 4.1 构造函数

    AlarmManager(IAlarmManager service, Context ctx) {
		// 远程AlarmManagerService的Proxy对象
        mService = service;
        mPackageName = ctx.getPackageName();
        mTargetSdkVersion = ctx.getApplicationInfo().targetSdkVersion;
        mAlwaysExact = (mTargetSdkVersion < Build.VERSION_CODES.KITKAT);
        mMainThreadHandler = new Handler(ctx.getMainLooper());
    }

- `mMainThreadHandler`是运行在app进程的主线程


## 4.1 AlarmManager.set()

    public void set(@AlarmType int type, long triggerAtMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, legacyExactLength(), 0, 0, operation, null, null,
                null, null, null);
    }

## 4.2 AlarmManager.setImpl()

    private void setImpl(@AlarmType int type, long triggerAtMillis, long windowMillis,
            long intervalMillis, int flags, PendingIntent operation, final OnAlarmListener listener,
            String listenerTag, Handler targetHandler, WorkSource workSource,
            AlarmClockInfo alarmClock) {

        if (triggerAtMillis < 0) {
            triggerAtMillis = 0;
        }

        ListenerWrapper recipientWrapper = null;
        if (listener != null) {
            synchronized (AlarmManager.class) {
                if (sWrappers == null) {
                    sWrappers = new ArrayMap<OnAlarmListener, ListenerWrapper>();
                }

                recipientWrapper = sWrappers.get(listener);
                // no existing wrapper => build a new one
                if (recipientWrapper == null) {
                    recipientWrapper = new ListenerWrapper(listener);
                    sWrappers.put(listener, recipientWrapper);
                }
            }
			// 如果没有设置handler对象,就采用当前进程的主线程的Handler
            final Handler handler = (targetHandler != null) ? targetHandler : mMainThreadHandler;
            recipientWrapper.setHandler(handler);
        }

        try {
			// 调用AlarmManagerService,通过mService代理
			// 因此,接下来的程序运行到 system_server进程中
            mService.set(mPackageName, type, triggerAtMillis, windowMillis, intervalMillis, flags,
                    operation, recipientWrapper, listenerTag, workSource, alarmClock);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

- `mService`的实现类是在`AlarmManagerService`中

## 4.3 AlarmManagerService.set()

    private final IBinder mService = new IAlarmManager.Stub() {
        @Override
        public void set(String callingPackage,
                int type, long triggerAtTime, long windowLength, long interval, int flags,
                PendingIntent operation, IAlarmListener directReceiver, String listenerTag,
                WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock) {
            final int callingUid = Binder.getCallingUid();

            // make sure the caller is not lying about which package should be blamed for
            // wakelock time spent in alarm delivery
            mAppOps.checkPackage(callingUid, callingPackage);

            // Repeating alarms 必须使用 PendingIntent, 而不是AlarmListener
            if (interval != 0) {
                if (directReceiver != null) {
                    throw new IllegalArgumentException("Repeating alarms cannot use AlarmReceivers");
                }
            }

            if (workSource != null) {
                getContext().enforcePermission(
                        android.Manifest.permission.UPDATE_DEVICE_STATS,
                        Binder.getCallingPid(), callingUid, "AlarmManager.set");
            }

            // 清除caller设置的flag: WAKE_FROM_IDLE或FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED(需根据具体条件设置)
            flags &= ~(AlarmManager.FLAG_WAKE_FROM_IDLE
                    | AlarmManager.FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED);

            // 只有DeviceIdleController可设置FLAG_IDLE_UNTIL,以进入Doze Idle状态
            if (callingUid != Process.SYSTEM_UID) {
                flags &= ~AlarmManager.FLAG_IDLE_UNTIL;
            }

			// 如果是请求设置精确alarm，设置FLAG_STANDALONE，标志不对其进行batch批处理
            if (windowLength == AlarmManager.WINDOW_EXACT) {
                flags |= AlarmManager.FLAG_STANDALONE;
            }

      		// 如果是alarmClock，设置FLAG_WAKE_FROM_IDLE可从idle唤醒，FLAG_STANDALONE不对其进行batch批处理
            if (alarmClock != null) {
                flags |= AlarmManager.FLAG_WAKE_FROM_IDLE | AlarmManager.FLAG_STANDALONE;

      		// 如果caller是系统/用户设置mDeviceIdleUserWhitelist白名单，设置FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED标志
      		// 即在Doze Idle模式下精准时间，不受影响
            } else if (workSource == null && (callingUid < Process.FIRST_APPLICATION_UID
                    || callingUid == mSystemUiUid
                    || Arrays.binarySearch(mDeviceIdleUserWhitelist,
                            UserHandle.getAppId(callingUid)) >= 0)) {
                flags |= AlarmManager.FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED;
                flags &= ~AlarmManager.FLAG_ALLOW_WHILE_IDLE;
            }

            setImpl(type, triggerAtTime, windowLength, interval, operation, directReceiver,
                    listenerTag, flags, workSource, alarmClock, callingUid, callingPackage);
        }

		......省略........
	}

-  对请求的参数进行了初步处理