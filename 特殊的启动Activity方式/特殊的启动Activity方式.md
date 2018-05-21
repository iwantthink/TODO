# 特殊的启动Activity方式

[从框架层分析如何启动未注册的 Activity](https://zhuanlan.zhihu.com/p/26455221)

[Android -- PackageManagerService初始化分析](https://blog.csdn.net/csdn_of_coder/article/details/72991772)


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
			//检查启动结果
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

## 3.1 ActivityStarter.startActivity(参数多) 

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
			//判断错误 START_CLASS_NOT_FOUND
       		if (err == ActivityManager.START_SUCCESS && aInfo == null) {
            	// We couldn't find the specific class specified in the Intent.
            	// Also the end of the line.
            	err = ActivityManager.START_CLASS_NOT_FOUND;
        	}
			//判断错误 START_INTENT_NOT_RESOLVED
	        if (err == ActivityManager.START_SUCCESS && intent.getComponent() == null) {
	            // We couldn't find a class that can handle the given Intent.
	            // That's the end of that!
	            err = ActivityManager.START_INTENT_NOT_RESOLVED;
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

- **`intent.getComponent()`会返回一个`ComponentName`类型的对象，这个对象在初始化Intent被创建，代表这个与这个Intent相关联的类的信息**

	    /**
	     * Retrieve the concrete component associated with the intent.  When receiving
	     * an intent, this is the component that was found to best handle it (that is,
	     * yourself) and will always be non-null; in all other cases it will be
	     * null unless explicitly set.
	     *
	     * @return The name of the application component to handle the intent.
	     *
	     * @see #resolveActivity
	     * @see #setComponent
	     */
	    public @Nullable ComponentName getComponent() {
	        return mComponent;
	    }

### 3.1.1 判断条件-ActivityInfo如何产生的
查看ActivityInfo是什么地方产生的，这个ActivityInfo记录了AndroidManifest.xml中的信息

----

**查看启动流程，可以得知`ActivityInfo`是在`ActivityStarter.startActivityMayWait()`中产生**

    final int startActivityMayWait(IApplicationThread caller, int callingUid,
            String callingPackage, Intent intent, String resolvedType,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            IBinder resultTo, String resultWho, int requestCode, int startFlags,
            ProfilerInfo profilerInfo, WaitResult outResult,
            Configuration globalConfig, Bundle bOptions, boolean ignoreTargetSecurity, int userId,
            TaskRecord inTask, String reason) {
		···省略代码···
        ResolveInfo rInfo = mSupervisor.resolveIntent(intent, resolvedType, userId);
        // Collect information about the target of the Intent.
        ActivityInfo aInfo = mSupervisor.resolveActivity(intent, rInfo, startFlags, profilerInfo);
		···省略代码···
	}

---

**查看`ActivityStackSupervisor.resolveIntent()`**

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId) {
        return resolveIntent(intent, resolvedType, userId, 0);
    }

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags) {
        synchronized (mService) {
            return mService.getPackageManagerInternalLocked().resolveIntent(intent, resolvedType,
                    PackageManager.MATCH_INSTANT | PackageManager.MATCH_DEFAULT_ONLY | flags
                    | ActivityManagerService.STOCK_PM_FLAGS, userId);
        }
    }

    PackageManagerInternal getPackageManagerInternalLocked() {
        if (mPackageManagerInt == null) {
            mPackageManagerInt = LocalServices.getService(PackageManagerInternal.class);
        }
        return mPackageManagerInt;
    }

- **可以看到这里通过`LocalServices`获取了`PackageManagerService`的远程代理Binder.然后通过`PackageManagerService`获取到了最符合的`ResolveInfo`.具体流程查看`[PackageManagerService分析.md]`**

---

**查看`ActivityStackSupervisor.resolveActivity()`**

    ActivityInfo resolveActivity(Intent intent, ResolveInfo rInfo, int startFlags,
            ProfilerInfo profilerInfo) {
        final ActivityInfo aInfo = rInfo != null ? rInfo.activityInfo : null;
      ``省略代码````
        return aInfo;
    }

- 从第一句判断可以得知，ActivityInfo是从rInfo中或获取的，那么查看rInfo如何获取的即可知道aInfo是如何获取的。


## 3.2 ActivityStarter.startActivity(参数少)


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

# 4. 关于抛出的异常的总结

**Activity的启动是借助AMS进行调度，在AMS中会对被启动的Activity进行检查（例如是否存在该Activity，AndroidManifest.xml中是否注册）**

**可以将Activity的启动过程大致的分为俩部分**：

1. 启动Activity的请求 发送至 AMS

2. AMS处理完了之后，通知应用启动Activity

- 第二阶段是在应用层的操作，可以通过反射进行修改。

# 5. 如何绕过检查？

**绕过检查的主要思路**：

- 通过一个合法的Intent 来包裹一个 未注册的Activity启动请求，先通过第一阶段的检查，然后在第二个阶段的某个地方将系统传递过来的Intent给替换掉

## 5.1 在哪里将合法的Intent替换？

启动Activity之后，Intent在经过Instrumentation,AMS等一系列方法调用之后，最终会回到`ActivityThread`，并调用`ApplicationThread.scheduleLaunchActivity()`。然后通过Handler 切换到主线程去执行方法。

       // we use token to identify this activity without having to send the
        // activity itself back to the activity manager. (matters more with ipc)
        @Override
        public final void scheduleLaunchActivity(Intent intent, IBinder token, int ident,
                ActivityInfo info, Configuration curConfig, Configuration overrideConfig,
                CompatibilityInfo compatInfo, String referrer, IVoiceInteractor voiceInteractor,
                int procState, Bundle state, PersistableBundle persistentState,
                List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents,
                boolean notResumed, boolean isForward, ProfilerInfo profilerInfo) {
            ···省略代码···
            ActivityClientRecord r = new ActivityClientRecord();
            r.intent = intent;
			···省略代码···
            sendMessage(H.LAUNCH_ACTIVITY, r);
        }

- **这个方法执行在Binder的线程池中，而不是主线程中**

- **这里的Intent包含了待启动Activity的信息，只需要将这个Intent替换成 包含未注册的Activity信息的Intent即可**

- Handler的`dispatchMessage()`方法表示了具体消息处理逻辑。首先处理`Handler.post(new Runnable())`发送过来的 `Runnable`。其次，判断是否存在mCallback变量(这个变量也是表示处理逻辑)，如果存在则用mCallback去处理msg。最后，会去调用`handleMessage()`去处理msg。

	    /**
	     * Handle system messages here.
	     */
	    public void dispatchMessage(Message msg) {
	        if (msg.callback != null) {
	            handleCallback(msg);
	        } else {
	            if (mCallback != null) {
	                if (mCallback.handleMessage(msg)) {
	                    return;
	                }
	            }
	            handleMessage(msg);
	        }
	    }


**在调用`scheduleLaunchHandler`时 已经通过了AMS对Intent的检查。那么在这里应该就可以去寻找HOOK点，做替换的操作**

**Hook点：**

-  观察这个方法将原始的Intent拼装成一个`ActivityClientRecord` 放入msg中，然后通过Handler 发送msg。 

-  ActivityThread中的`Handler mH`并没有使用`mCallback`去处理msg。

**这里是一个很好的Hook点，我们可以将替换原始Intent的逻辑 通过反射写在mCallback中，这样当我们触发虚假Activity启动时，就可以在mCallback中将 未注册的Intent 替换进去**

## 5.2 如何传递真正的intent？

Intent 在被创建之后，可以传入一些额外数据。

		  //在AndroidManifest.xml中注册过的Activity
          Intent intent = new Intent(TestActivitys.this, TestActivitys.class);
		  //未注册过的Activity
          Intent realIntent = new Intent(TestActivitys.this, MainActivity.class);
          intent.putExtra("TARGET_INTENT", realIntent);
          startActivity(intent);


## 5.3 Hook逻辑

    private void hookHandler() {

        try {
            //获取ActivityThread对象
            Class class_activityThread = Class.forName("android.app.ActivityThread");
            Method method_currentActivityThread = class_activityThread.getMethod("currentActivityThread",
                    new Class[]{});
            method_currentActivityThread.setAccessible(true);
            Object activityThread = method_currentActivityThread.invoke(null, new Object[]{});


            //获取mH 这个Handler
            Field mHField = class_activityThread.getDeclaredField("mH");
            //这一步是为了绕过Java检查，而不是将修饰符修改成public
            mHField.setAccessible(true);
            Object mH = mHField.get(activityThread);


            //获取mH 中的mCallback对象
            Field field_callBack = Handler.class.getDeclaredField("mCallback");
            field_callBack.setAccessible(true);
            // 给mCallback设置回调
            field_callBack.set(mH, mDispatcher);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public Handler.Callback mDispatcher = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 100) {
                Object record = msg.obj;
                try {
                    Field intentField = record.getClass().getDeclaredField("intent");
                    intentField.setAccessible(true);
                    Intent intent = (Intent) intentField.get(record);
                    if (intent.hasExtra("TARGET_INTENT")) {
                        Bundle bundle = intent.getExtras();
                        Intent realIntent = (Intent) bundle.get("TARGET_INTENT");
                        intentField.set(record, realIntent);
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
            return false;
        }
    };