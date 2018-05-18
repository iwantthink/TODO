# 特殊的启动Activity方式

[从框架层分析如何启动未注册的 Activity](https://zhuanlan.zhihu.com/p/26455221)


# 1. 简介

![](http://ww1.sinaimg.cn/large/6ab93b35ly1frfleqqv8jj20r20an75t.jpg)

如果启动一个不在Android.manifest中的Activity，会抛出以下的异常信息

	android.content.ActivityNotFoundException: Unable to find explicit activity class {xx.xx.xx.MainActivity}; have you declared this activity in your AndroidManifest.xml?

如何绕过这一检查？

**本文基于Android 27**

# 2 什么地方抛出了这个异常？

通过`[Activity启动流程.md]`可以得知，启动一个Activity 都会走`Instrumentation.execStartActivity()`

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
		···省略代码···
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            int result = ActivityManager.getService()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target != null ? target.mEmbeddedID : null,
                        requestCode, 0, null, options);
            checkStartActivityResult(result, intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
        return null;
    }

- **`checkStartActivityResult()`这个方法中 抛出了这个异常**

	   public static void checkStartActivityResult(int res, Object intent) {
	        if (!ActivityManager.isStartResultFatalError(res)) {
	            return;
	        }
	
	        switch (res) {
	            case ActivityManager.START_INTENT_NOT_RESOLVED:
	            case ActivityManager.START_CLASS_NOT_FOUND:
	                if (intent instanceof Intent && ((Intent)intent).getComponent() != null)
	                    throw new ActivityNotFoundException(
	                            "Unable to find explicit activity class "
	                            + ((Intent)intent).getComponent().toShortString()
	                            + "; have you declared this activity in your AndroidManifest.xml?");
	                throw new ActivityNotFoundException(
	                        "No Activity found to handle " + intent);
			···省略若干代码····
			}

# 3 代表异常的flag是哪里产生的？

代表Activity启动结果的`int result`,是在`ActivityStarter.startActivity()`方法中产生的

    private int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent,
            String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid,
            String callingPackage, int realCallingPid, int realCallingUid, int startFlags,
            ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified,
            ActivityRecord[] outActivity, TaskRecord inTask) {
			··省略代码···
	        int err = ActivityManager.START_SUCCESS;
       		if (err == ActivityManager.START_SUCCESS && aInfo == null) {
            	// We couldn't find the specific class specified in the Intent.
            	// Also the end of the line.
            	err = ActivityManager.START_CLASS_NOT_FOUND;
        	}
            if (err != START_SUCCESS) {
            	if (resultRecord != null) {
               	 resultStack.sendActivityResultLocked(
                        -1, resultRecord, resultWho, requestCode, RESULT_CANCELED, null);}
           		 ActivityOptions.abort(options);
            		return err;
        	}
		··省略代码···
       return startActivity(r, sourceRecord, voiceSession, voiceInteractor, startFlags, true,
                options, inTask, outActivity);
	}

- 该方法是由`ActivityStarter.startActivityLocked()`调用的，注意这里有俩个重载的`startActivity()`方法

- **`aInfo`是一个ActivityInfo类型的对象，记录了`Android.manifest`中的Activity的信息，如果这个信息为空 则会直接返回flag:`START_CLASS_NOT_FOUND`**



    private int startActivity(final ActivityRecord r, ActivityRecord sourceRecord,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask,
            ActivityRecord[] outActivity) {
        int result = START_CANCELED;
        try {
            result = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor,
                    startFlags, doResume, options, inTask, outActivity);
        } finally {
           ···省略代码···
        }

		···省略代码···

        return result;
    }

- 可以看到默认的result值为`START_CANCELED`

- 具体的result值 会交给`startActivityUnchecked()`方法

	     private int startActivityUnchecked(final ActivityRecord r, ActivityRecord sourceRecord,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask,
            ActivityRecord[] outActivity) {
			··省略无数代码···
		    if (mStartActivity.packageName == null) {
		            final ActivityStack sourceStack = mStartActivity.resultTo != null
		                    ? mStartActivity.resultTo.getStack() : null;
		            if (sourceStack != null) {
		                sourceStack.sendActivityResultLocked(-1 /* callingUid */, mStartActivity.resultTo,
		                        mStartActivity.resultWho, mStartActivity.requestCode, RESULT_CANCELED,
		                        null /* data */);
		            }
		            ActivityOptions.abort(mOptions);
	            return START_CLASS_NOT_FOUND;
			}
			···省略代码··
			return START_SUCCESS;
        }

- **`mStartActivity`是一个ActivityRecord对象，它是在启动Activity时供方法共用的一些变量,其中packageName就是待启动的应用所存在的包名.所以这里的判断逻辑即 判断是否存在包名信息，如果不存在就返回`START_CLASS_NOT_FOUND`**
	
	    // Share state variable among methods when starting an activity.
	    private ActivityRecord mStartActivity;


//TODO 查看ActivityInfo是什么地方产生的，这个ActivityInfo记录了AndroidManifest.xml中的信息

    PackageManagerInternal getPackageManagerInternalLocked() {
        if (mPackageManagerInt == null) {
            mPackageManagerInt = LocalServices.getService(PackageManagerInternal.class);
        }
        return mPackageManagerInt;
    }


        ResolveInfo rInfo = mSupervisor.resolveIntent(intent, resolvedType, userId);
