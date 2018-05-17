# AcitvityRecord分析

[ActivityRecord源码地址](https://android.googlesource.com/platform/frameworks/base/+/master/services/core/java/com/android/server/am/ActivityRecord.java)

[Activity的代表ActivityRecrod-分析](https://blog.csdn.net/u010479969/article/details/48047329)

# 1. 简介

ActivityRecord是Activity的标识，与每个Activity是一一对应的。ActivityRecord是在Activity启动的过程中被创建的。

**本篇基于源码27分析**

# 2. 创建过程

Activity的创建过程是会通过AMS进行调度，AMS会调用`ActivityStarter`,`ActivityStackSupervisor`等类去完成具体的操作

## 2.1 ActivityStarter.startActivity(参数1)

    /** DO NOT call this method directly. Use {@link #startActivityLocked} instead. */
    private int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent,
            String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid,
            String callingPackage, int realCallingPid, int realCallingUid, int startFlags,
            ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified,
            ActivityRecord[] outActivity, TaskRecord inTask) {
		```省略代码```

        ActivityRecord r = new ActivityRecord(mService, callerApp, callingPid, callingUid,
                callingPackage, intent, resolvedType, aInfo, mService.getGlobalConfiguration(),
                resultRecord, resultWho, requestCode, componentSpecified, voiceSession != null,
                mSupervisor, options, sourceRecord);
		//赋值给outActivity
        if (outActivity != null) {
            outActivity[0] = r;
        }

		```省略代码```
        return startActivity(r, sourceRecord, voiceSession, voiceInteractor, startFlags, true,
                options, inTask, outActivity);
    }

- ActivityRecord在此被创建

## 2.2 ActivityRecord构造函数

    ActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromPid,
            int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType,
            ActivityInfo aInfo, Configuration _configuration,
            ActivityRecord _resultTo, String _resultWho, int _reqCode,
            boolean _componentSpecified, boolean _rootVoiceInteraction,
            ActivityStackSupervisor supervisor, ActivityOptions options,
            ActivityRecord sourceRecord) {
		``省略代码``
	}

**传入参数说明：**

- `ProcessRecord _caller` ：调用者进程(即运行在什么进程)，与aInfo一起来决定当前Activity的应用包名

- `int _launchedFromUid` ：启动Activity的Uid 

- `String _launchedFromPackage`:启动Activity的包名  

- `Intent _inten`t:启动的Intent  

- `String _resolvedType`:调用的包名  

- `ActivityInfo aInfo`:Activity的信息  

- `Configuration _configuration`:Activity的配置信息  

- `ActivityRecord _resultTo`:parent Activity的信息  

- `String _resultWho`:parent Activity的包名

- `int _reqCode`:startActivityForResult中的RequestCode

- `boolean _componentSpecified`: boolean componentSpecified = intent.getComponent() != null;

- `ActivityStackSupervisor supervisor`: 

- `ActivityContainer container`:大多数为null，一些特殊的启动方式会有值.

- `ActivityOptions options `: 

- `ActivityRecord sourceRecord`:

**成员变量说明：**

- TaskRecord task //跑在哪个task

- ActivityInfo info // Activity信息

- int mActivityType //Activity类型

	- APPLICATION\_ACTIVITY\_TYPE:普通应用类型

	- HOME\_ACTIVITY\_TYPE：桌面类型

	- RECENTS\_ACTIVITY\_TYPE：最近任务类型

- ActivityState state //Activity状态

	- INITIALIZING

	- RESUMED：已恢复

	- PAUSING

	- PAUSED：已暂停

	- STOPPING

	- STOPPED：已停止

	- FINISHING

	- DESTROYING

	- DESTROYED：已销毁

- ApplicationInfo appInfo //跑在哪个app

- ComponentName realActivity //组件名

- String packageName //包名

- String processName //进程名

- int launchMode //启动模式

- int userId // 该Activity运行在哪个用户id

**时间相关变量说明：**

|时间点|赋值时间|含义|
|:---:|:---:|:---:|
|createTime|	new ActivityRecord	|Activity首次创建时间点
|displayStartTime|	AS.setLaunchTime	|Activity首次启动时间点
|fullyDrawnStartTime|	AS.setLaunchTime	|Activity首次启动时间点
|startTime|	  -	|Activity上次启动的时间点
|lastVisibleTime|	AR.windowsVisibleLocked	|Activity上次成为可见的时间点
|cpuTimeAtResume|	AS.completeResumeLocked	|从Rsume以来的cpu使用时长
|pauseTime|	AS.startPausingLocked	|Activity上次暂停的时间点
|launchTickTime|	AR.startLaunchTickingLocked	|Eng版本才赋值
|lastLaunchTime|	ASS.realStartActivityLocked	|上一次启动时间

- AR指的是ActivityRecord

- AS指的是ActivityStack