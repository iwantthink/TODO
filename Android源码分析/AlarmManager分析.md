# AlarmManager分析

[理解AlarmManager机制](http://gityuan.com/2017/03/12/alarm_manager_service/)

[定时任务Alarm的深入理解](https://juejin.im/post/5acc2a065188257ddb0fef9e)

[Android官方文档](https://developer.android.com/reference/android/app/AlarmManager)

[SystemServer分析.md]()

# 1. 简介

Android 中的定时任务一般有两种实现方式，一种是`Timer`类，一种是使用Android的`Alarm`机制。这两种方式在多数情况下都能实现类似的效果，但是`Timer`有一个明显的短板，它并不适合用于那些需要长期在后台运行的定时任务。

为了能让电池更加耐用，各种手机都会有自己的休眠策略，Android手机就会在长时间不操作的情况下自动让CPU进入到休眠状态，这就有可能导致Timer的定时任务无法正常运行。而Alarm具有唤醒CPU的功能，它可以保证在大多数情况下需要执行的定时任务的时候CPU都能正常工作。注意唤醒CPU和唤醒屏幕完全不是一个概念。


- 从Android 4.4系统开始，Alarm任务的触发时间将会变得不准确，有可能会延迟一段时间后任务才能得到执行。但这并不是bug，而是系统在耗电性方面进行的优化。系统会自动检测目前有杜少Alarm任务存在，然后将触发时间相近的几个任务放在一起执行，这就可以大幅度的减少cpu被唤醒的次数，从而有效延长电池的使用时间。



# 2. 用法


	PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_JOB_EXPIRED), 0);

	AlarmManager alarmManager=(AlarmManager)getSystemService(Service.ALARM_SERVICE);

	alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), pi);  


# 3. AlarmManager服务

`AlarmManager`作为一个系统服务,在`SystemServer`类中就已经被创建并执行

	private void startOtherServices() {
	  ...
	  mSystemServiceManager.startService(AlarmManagerService.class);
	  ...
	}

- `SystemServiceManager`会将会创建一个`AlarmManagerService`对象,并回调其`onStart()`方法

	具体分析过程查看[SystemServer分析.md]

## 3.1 构造函数

    final AlarmHandler mHandler = new AlarmHandler();

    public AlarmManagerService(Context context) {
        super(context);
        mConstants = new Constants(mHandler);
    }

- `mHandler`作为成员变量,在`AlarmManagerService`对象创建时被创建,该Handler运行在`system_server`进程


## 3.1.1 创建Constants

	private final class Constants extends ContentObserver {
	    public Constants(Handler handler) {
	        super(handler);
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
	    ...
	}

- 当系统处于`idle`状态,`alarm`最小时间间隔为9min. 当系统处于非`idle`状态,最小间隔时间为5s


## 3.2 AlarmManagerService.onStart()


    @Override
    public void onStart() {
        mNativeData = init();
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

        if (mSystemUiUid <= 0) {
            Slog.wtf(TAG, "SysUI package not found!");
        }

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
        
        // now that we have initied the driver schedule the alarm
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
        
        if (mNativeData != 0) {
            AlarmThread waitThread = new AlarmThread();
            waitThread.start();
        } else {
            Slog.w(TAG, "Failed to open alarm driver. Falling back to a handler.");
        }

        try {
            ActivityManager.getService().registerUidObserver(new UidObserver(),
                    ActivityManager.UID_OBSERVER_IDLE, ActivityManager.PROCESS_STATE_UNKNOWN, null);
        } catch (RemoteException e) {
            // ignored; both services live in system_server
        }

		// 发布alarm服务
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

		// 对条件的判断
		............省略代码............
		// currentTimeMillis() 指的是 从纪元开始到现在的时间
		// 获取系统启动到现在的时间
        final long nowElapsed = SystemClock.elapsedRealtime();
		// 转换闹钟触发的时间,仅针对type= RTC| type= RTC_WAKEUP,除此之外直接返回
        final long nominalTrigger = convertToElapsed(triggerAtTime, type);

		// 设置最小触发时间是 当前流逝时间+五秒
        final long minTrigger = nowElapsed + mConstants.MIN_FUTURITY;
		// 保证alarm触发的时间至少是5s之后
        final long triggerElapsed = (nominalTrigger > minTrigger) ? nominalTrigger : minTrigger;

        final long maxElapsed;
        if (windowLength == AlarmManager.WINDOW_EXACT) {
            maxElapsed = triggerElapsed;
        } else if (windowLength < 0) {
            maxElapsed = maxTriggerTime(nowElapsed, triggerElapsed, interval);
            // Fix this window in place, so that as time approaches we don't collapse it.
            windowLength = maxElapsed - triggerElapsed;
        } else {
            maxElapsed = triggerElapsed + windowLength;
        }

        synchronized (mLock) {
            setImplLocked(type, triggerAtTime, triggerElapsed, windowLength, maxElapsed,
                    interval, operation, directReceiver, listenerTag, flags, true, workSource,
                    alarmClock, callingUid, callingPackage);
        }
    }

### 3.2.5 ALMS.setImplLocked()

    private void setImplLocked(int type, long when, long whenElapsed, long windowLength,
            long maxWhen, long interval, PendingIntent operation, IAlarmListener directReceiver,
            String listenerTag, int flags, boolean doValidate, WorkSource workSource,
            AlarmManager.AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
		// 创建Alarm 对象
        Alarm a = new Alarm(type, when, whenElapsed, windowLength, maxWhen, interval,
                operation, directReceiver, listenerTag, workSource, flags, alarmClock,
                callingUid, callingPackage);
        try {
            if (ActivityManager.getService().isAppStartModeDisabled(callingUid, callingPackage)) {
                return;
            }
        } catch (RemoteException e) {
        }
        removeLocked(operation, directReceiver);
        setImplLocked(a, false, doValidate);
    }

### 3.2.6 ALMS.setImplLocked()--2

	private void setImplLocked(Alarm a, boolean rebatching, boolean doValidate) {
	    if ((a.flags&AlarmManager.FLAG_IDLE_UNTIL) != 0) {
	        if (mNextWakeFromIdle != null && a.whenElapsed > mNextWakeFromIdle.whenElapsed) {
	            a.when = a.whenElapsed = a.maxWhenElapsed = mNextWakeFromIdle.whenElapsed;
	        }
	        //增加模糊事件，让alarm比实际预期事件更早的执行
	        final long nowElapsed = SystemClock.elapsedRealtime();
	        final int fuzz = fuzzForDuration(a.whenElapsed-nowElapsed);
	        if (fuzz > 0) {
	            if (mRandom == null) {
	                mRandom = new Random();
	            }
	            //创建随机模糊时间
	            final int delta = mRandom.nextInt(fuzz);
	            a.whenElapsed -= delta;
	            a.when = a.maxWhenElapsed = a.whenElapsed;
	        }
	
	    } else if (mPendingIdleUntil != null) {
	        if ((a.flags&(AlarmManager.FLAG_ALLOW_WHILE_IDLE
	                | AlarmManager.FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED
	                | AlarmManager.FLAG_WAKE_FROM_IDLE))
	                == 0) {
	            mPendingWhileIdleAlarms.add(a);
	            return;
	        }
	    }
	
	    int whichBatch = ((a.flags&AlarmManager.FLAG_STANDALONE) != 0)
	            ? -1 : attemptCoalesceLocked(a.whenElapsed, a.maxWhenElapsed);
	    if (whichBatch < 0) {
	        //TIME_TICK是独立的，不与其他alarm一起批处理
	        Batch batch = new Batch(a);
	        addBatchLocked(mAlarmBatches, batch);
	    } else {
	        ...
	    }
	    ...
	
	    boolean needRebatch = false;
	
	    if ((a.flags&AlarmManager.FLAG_IDLE_UNTIL) != 0) {
	        mPendingIdleUntil = a;
	        mConstants.updateAllowWhileIdleMinTimeLocked();
	        needRebatch = true;
	    } else if ((a.flags&AlarmManager.FLAG_WAKE_FROM_IDLE) != 0) {
	        if (mNextWakeFromIdle == null || mNextWakeFromIdle.whenElapsed > a.whenElapsed) {
	            mNextWakeFromIdle = a;
	            if (mPendingIdleUntil != null) {
	                needRebatch = true;
	            }
	        }
	    }
	
	    if (!rebatching) {
	        if (needRebatch) {
	            //需要对所有alarm重新执行批处理
	            rebatchAllAlarmsLocked(false);
	        }
	
	        rescheduleKernelAlarmsLocked();
	        //重新计算下一个alarm
	        updateNextAlarmClockLocked();
	    }
	}
	
