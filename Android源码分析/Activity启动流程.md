# Activity启动流程

[深入理解Activity启动流程](http://www.cloudchou.com/android/post-788.html)

[Launcher源码](https://android.googlesource.com/platform/packages/apps/Launcher/+/master/src/com/android/launcher/Launcher.java)

[Android源码分析-Activity的启动过程-任玉刚](https://blog.csdn.net/singwhatiwanna/article/details/18154335)

[Android解析ActivityManagerService（一）AMS启动流程和AMS家族](https://blog.csdn.net/itachi85/article/details/76405596)

[Android-Server源码目录 包含AMS](https://android.googlesource.com/platform/frameworks/base/+/master/services/core/java/com/android/server)

[Android 7.0 - 应用程序进程启动过程](http://liuwangshu.cn/framework/applicationprocess/1.html)

[Android 7.0 - 应用程序启动过程](http://liuwangshu.cn/framework/component/1-activity-start-1.html)

[Android 8.0 - 根Activity启动过程](https://blog.csdn.net/itachi85/article/details/78569299)

# 1. 概述

系统从开机之后就会启动一个`Launcher`程序(即桌面),然后可以通过点击应用图标来启动应用的入口Activity，**在这个过程中(Activity的启动)需要多个进程之间交互**

Android系统中有一个`zygote`进程专用于孵化Android框架层和应用层程序的进程。有一个`system_server`进程，该进程运行了很多提供Binder的service，例如：`ActivityManagerService,PackageManagerService,WindowManagerService`等，这些服务分别运行在不同的线程中

`ActivityManagerService`就是负责管理`Activity`栈，应用程序，task等

**启动Activity这个工作，无论被启动的Activity是否处于同一进程，同一应用，都是由AMS管理的**

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fqzbxttc06j20ln0bq74f.jpg)

1. 用户在Launcher程序里点击应用图标时，会通知`ActivityManagerService`启动应用的入口`Activity`

2. `ActivityManagerService`发现这个应用还未启动，则会通知`Zygote`进程孵化出应用进程，然后在这个`dalvik/ART`应用进程里执行`ActivityThread`的`main`方法。

3. 应用进程接下来通知`ActivityManagerService`应用进程已启动，`ActivityManagerService`保存应用进程的一个`Binder`代理对象，这样`ActivityManagerService`可以通过这个代理对象控制应用进程

5. 然后`ActivityManagerService`通知应用进程创建入口Activity的实例，并执行它的生命周期方法。

## 1.1 重要类介绍

**ActivityManagerService管理Activity时，主要涉及以下几个类:**

1. `ActivityManagerService`，它是管理activity的入口类，聚合了ProcessRecord对象和ActivityStack对象

2. `ProcessRecord`，表示应用进程记录，每个应用进程都有对应的ProcessRecord对象

3. `ActivityStack`，该类主要管理回退栈

4. `ActivityRecord`，每次启动一个Actvity会有一个对应的ActivityRecord对象，表示Activity的一个记录.**可以查看[ActivityRecord分析]**

5. `ActivityInfo`，Activity的信息，比如启动模式，taskAffinity，flag信息(这些信息在AndroidManifest.xml里声明Activity时填写)

6. `TaskRecord`，Task记录信息，一个Task可能有多个ActivityRecord，但是一个ActivityRecord只能属于一个TaskRecord


# 2. Activity启动流程简介

1. Activity调用ActivityManagerService启动应用

2. ActivityManagerService调用Zygote孵化应用进程

3. Zygote进程孵化应用进程

4. 新进程启动ActivityThread

5. 应用进程绑定到ActivityManagerService

6. ActivityThread的Handler处理启动Activity的消息

**分析基于Android 26 源码**

## 2.1 Activity调用AMS启动应用

**Activity调用AMS可以分成俩种**：

1. Launcher调用AMS

	    /**
	     * Launches the intent referred by the clicked shortcut.
	     *
	     * @param v The view representing the clicked shortcut.
	     */
	    public void onClick(View v) {
	        Object tag = v.getTag();
	        if (tag instanceof ApplicationInfo) {
	            // Open shortcut
	            final Intent intent = ((ApplicationInfo) tag).intent;
	            startActivitySafely(intent);
	        } else if (tag instanceof FolderInfo) {
	            handleFolderClick((FolderInfo) tag);
	        }
	    }

	    void startActivitySafely(Intent intent) {
			// 注意这里 添加了new_task的 flag
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        try {
	            startActivity(intent);
	        } catch (ActivityNotFoundException e) {
	            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
	        } catch (SecurityException e) {
	            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
	            e(LOG_TAG, "Launcher does not have the permission to launch " + intent +
	                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
	                    "or use the exported attribute for this activity.", e);
	        }
	    }

	- [Launcher源码](https://android.googlesource.com/platform/packages/apps/Launcher/+/master/src/com/android/launcher/Launcher.java)

	- 点击`Launcher`应用的桌面图标之后，`Launcher`程序会调用`startActivity`启动应用,**俩种情况最终都会走到`Instrumentation`的`execStartActivity()`来启动应用**,

2. 普通应用调用AMS

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1fqzevkrkp4j20ph0dh0su.jpg)

	   /**
	     * Execute a startActivity call made by the application.  The default 
	     * implementation takes care of updating any active {@link ActivityMonitor}
	     * objects and dispatches this call to the system activity manager; you can
	     * override this to watch for the application to start an activity, and 
	     * modify what happens when it does. 
	     *
	     * <p>This method returns an {@link ActivityResult} object, which you can 
	     * use when intercepting application calls to avoid performing the start 
	     * activity action but still return the result the application is 
	     * expecting.  To do this, override this method to catch the call to start 
	     * activity so that it returns a new ActivityResult containing the results 
	     * you would like the application to see, and don't call up to the super 
	     * class.  Note that an application is only expecting a result if 
	     * <var>requestCode</var> is &gt;= 0.
	     *
	     * <p>This method throws {@link android.content.ActivityNotFoundException}
	     * if there was no Activity found to run the given Intent.
	     *
	     * @param who The Context from which the activity is being started.
	     * @param contextThread The main thread of the Context from which the activity
	     *                      is being started.
	     * @param token Internal token identifying to the system who is starting 
	     *              the activity; may be null.
	     * @param target Which activity is performing the start (and thus receiving 
	     *               any result); may be null if this call is not being made
	     *               from an activity.
	     * @param intent The actual Intent to start.
	     * @param requestCode Identifier for this request's result; less than zero 
	     *                    if the caller is not expecting a result.
	     * @param options Addition options.
	     *
	     * @return To force the return of a particular result, return an 
	     *         ActivityResult object containing the desired data; otherwise
	     *         return null.  The default implementation always returns null.
	     *
	     * @throws android.content.ActivityNotFoundException
	     *
	     * @see Activity#startActivity(Intent)
	     * @see Activity#startActivityForResult(Intent, int)
	     * @see Activity#startActivityFromChild
	     *
	     * {@hide}
	     */
	    public ActivityResult execStartActivity(
	            Context who, IBinder contextThread, IBinder token, Activity target,
	            Intent intent, int requestCode, Bundle options) {
			//核心功能在这个whoThread中完成，其内部scheduleLaunchActivity方法用于完成activity的打开  
	        IApplicationThread whoThread = (IApplicationThread) contextThread;
	        Uri referrer = target != null ? target.onProvideReferrer() : null;
	        if (referrer != null) {
	            intent.putExtra(Intent.EXTRA_REFERRER, referrer);
	        }
	        if (mActivityMonitors != null) {
	            synchronized (mSync) {
					//先检查一遍是否存在这个activity
	                final int N = mActivityMonitors.size();
	                for (int i=0; i<N; i++) {
	                    final ActivityMonitor am = mActivityMonitors.get(i);
	                    ActivityResult result = null;
						//判断这个监视器是否被用于onStartActivity，拦截所有activity启动
	                    if (am.ignoreMatchingSpecificIntents()) {
	                        result = am.onStartActivity(intent);
	                    }
						// am拦截了
	                    if (result != null) {
	                        am.mHits++;
	                        return result;
						//match方法 使用intentFilter 和类名进行匹配
	                    } else if (am.match(who, null, intent)) {
	                        am.mHits++;
							//当前监视器阻止activity启动
	                        if (am.isBlocking()) {
	                            return requestCode >= 0 ? am.getResult() : null;
	                        }
	                        break;
	                    }
	                }
	            }
	        }
	        try {
	            intent.migrateExtraStreamToClipData();
	            intent.prepareToLeaveProcess(who);
				//委托给AMS去处理具体的开启逻辑
	            int result = ActivityManager.getService()
	                .startActivity(whoThread, who.getBasePackageName(), intent,
	                        intent.resolveTypeIfNeeded(who.getContentResolver()),
	                        token, target != null ? target.mEmbeddedID : null,
	                        requestCode, 0, null, options);
				//检查AMS的处理结果，如果无法打开activity 会抛出各种异常
	            checkStartActivityResult(result, intent);
	        } catch (RemoteException e) {
	            throw new RuntimeException("Failure from system", e);
	        }
	        return null;
	    }

	- **查看源码的时候请注意**:`Instrumentation` 中有俩个`execStartActivity()`方法。俩者所需的参数不同，提供给不同的地方去调用，但是正常的启动activity流程是走的 参数少的那个方法。俩个方法最终调用的AMS方法也不同，一个是调用 startActivityAsUser ，一个是调用startActivity(**实际上这个方法也会调用startActivityUser**)。

	- `ActivityManager.getService()` 通过`SingleTon`这个类获取到了AMS的Binder代理，接着通过这个Binder代理调用了`startActivityAsUser()`方法，那么实际上会调用`IActivityManager.Stub.Proxy.startActivityAsUser`

	而Proxy这个类，会通过Binder通信 去调用`IActivityManager.Stub.startActivityAsUser`.完成一次进程间通信(**具体细节查看Binder分析.md**)

	- [Instrumentation源码](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/Instrumentation.java)

## 2.2 AMS调用Zygote孵化应用进程

[Android深入四大组件（六）Android8.0 根Activity启动过程（前篇）](https://blog.csdn.net/itachi85/article/details/78569299)

**从2.1中可以看出 Instrumentation将具体的开启 交给了AMS来处理,AMS运行在`system_server`进程。**

在Android 27 的源码中，调用关系是这样的：

![](http://upload-images.jianshu.io/upload_images/1417629-b9da48e2ebdaf3d6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 2.2.1 AMS.startActivity 

	  @Override
	    public final int startActivity(IApplicationThread caller, String callingPackage,
	            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
	            int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
	        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo,
	                resultWho, requestCode, startFlags, profilerInfo, bOptions,
	                UserHandle.getCallingUserId());
	    }

- startActivity和startActivityAsUser 不同点就是 后者比前者多了一个`int userID`参数，在前者中是通过`UserHandler.getCallingUserId()`获得调用者的`UserID`。**AMS会根据这个UserID来确定调用者的权限**

### 2.2.2  AMS.startActivityAsUser 
	
	    @Override
	    public final int startActivityAsUser(IApplicationThread caller, String callingPackage,
	            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
	            int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
	        //判断调用者进程是否被隔离，成立则抛出异常    
	        enforceNotIsolatedCaller("startActivity");//1
	        //检查调用者权限,无权限则抛出异常
	        userId = mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(),
	                userId, false, ALLOW_FULL_ONLY, "startActivity", null);//2
	        return mActivityStarter.startActivityMayWait(caller, -1, callingPackage, intent,
	                resolvedType, null, null, resultTo, resultWho, requestCode, startFlags,
	                profilerInfo, null, null, bOptions, false, userId, null, null,
	                "startActivityAsUser");
	    }

- 注释1：判断调用者进程是否被隔离，被隔离则抛出`SecurityException`

- 注释2：检查调用者是否有权限，如果没有权限会抛出`SecurityException`

- **需要注意的是倒数第二个参数类型为TaskRecord，代表启动的Activity所在的栈。最后一个参数`"startActivityAsUser"`代表启动的理由**

### 2.2.3 ActivityStarter.startActivityMayWait 

	final int startActivityMayWait(IApplicationThread caller, int callingUid,
	            String callingPackage, Intent intent, String resolvedType,
	            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
	            IBinder resultTo, String resultWho, int requestCode, int startFlags,
	            ProfilerInfo profilerInfo, WaitResult outResult,
	            Configuration globalConfig, Bundle bOptions, boolean ignoreTargetSecurity, int userId,
	            IActivityContainer iContainer, TaskRecord inTask, String reason) {
	         ...
	        int res = startActivityLocked(caller, intent, ephemeralIntent, resolvedType,
	                    aInfo, rInfo, voiceSession, voiceInteractor,
	                    resultTo, resultWho, requestCode, callingPid,
	                    callingUid, callingPackage, realCallingPid, realCallingUid, startFlags,
	                    options, ignoreTargetSecurity, componentSpecified, outRecord, container,
	                    inTask, reason);
	         ...
	         return res;
	     }
	 }

- 最终调用了`ActivityStarter.startActivityLocked`方法，`startActivityLocked`方法的参数要比`startActivityAsUser`多几个

- **ActivityStarter是Android7.0新加入的类，它是加载Activity的控制类，会收集所有的条件来决定如何将Intent和Flags转换为Activity，并将Activity和Task和Stack相关联。**

### 2.2.4 ActivityStarter.startActivityLocked 

	   int startActivityLocked(IApplicationThread caller, Intent intent, Intent ephemeralIntent,
	            String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo,
	            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
	            IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid,
	            String callingPackage, int realCallingPid, int realCallingUid, int startFlags,
	            ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified,
	            ActivityRecord[] outActivity, ActivityStackSupervisor.ActivityContainer container,
	            TaskRecord inTask, String reason) {
	        //判断启动的理由不为空
	        if (TextUtils.isEmpty(reason)) {//1
	            throw new IllegalArgumentException("Need to specify a reason.");
	        }
	        mLastStartReason = reason;
	        mLastStartActivityTimeMs = System.currentTimeMillis();
	        mLastStartActivityRecord[0] = null;
	        mLastStartActivityResult = startActivity(caller, intent, ephemeralIntent, resolvedType,
	                aInfo, rInfo, voiceSession, voiceInteractor, resultTo, resultWho, requestCode,
	                callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags,
	                options, ignoreTargetSecurity, componentSpecified, mLastStartActivityRecord,
	                container, inTask);
	        if (outActivity != null) {
	            outActivity[0] = mLastStartActivityRecord[0];
	        }
	        return mLastStartActivityResult;
	    }

- 在这个方法中会判断 **启动Activiyt的理由**，如果为空会抛出异常

### 2.2.5 ActivityStarter.startActivity 

		  private int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent,
		            String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo,
		            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
		            IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid,
		            String callingPackage, int realCallingPid, int realCallingUid, int startFlags,
		            ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified,
		            ActivityRecord[] outActivity, ActivityStackSupervisor.ActivityContainer container,
		            TaskRecord inTask) {
		        int err = ActivityManager.START_SUCCESS;
		        final Bundle verificationBundle
		                = options != null ? options.popAppVerificationBundle() : null;
		        ProcessRecord callerApp = null;
		        if (caller != null) {//1
		            //获取Launcher进程
		            callerApp = mService.getRecordForAppLocked(caller);//2
		            if (callerApp != null) {
		              //获取Launcher进程的pid和uid并赋值
		                callingPid = callerApp.pid;
		                callingUid = callerApp.info.uid;
		            } else {
		                Slog.w(TAG, "Unable to find app for caller " + caller
		                        + " (pid=" + callingPid + ") when starting: "
		                        + intent.toString());
		                err = ActivityManager.START_PERMISSION_DENIED;
		            }
		        }
		        ...
		        //创建即将要启动的Activity的描述类ActivityRecord
		        ActivityRecord r = new ActivityRecord(mService, callerApp, callingPid, callingUid,
		                callingPackage, intent, resolvedType, aInfo, mService.getGlobalConfiguration(),
		                resultRecord, resultWho, requestCode, componentSpecified, voiceSession != null,
		                mSupervisor, container, options, sourceRecord); //3  
		        if (outActivity != null) {
		            outActivity[0] = r;//4
		        }
		        ...
		            doPendingActivityLaunchesLocked(false);
		            return startActivity(r, sourceRecord, voiceSession, voiceInteractor, startFlags, true,
		                options, inTask, outActivity);//5
		    }

- 注释1：caller代表 启动Activity的进程的ApplicationThread

- 注释2：此处调用AMS的`getRecordForAppLocked`方法得到的是代表启动Activity的进程的callerApp对象，它是ProcessRecord类型的，ProcessRecord用于描述一个应用程序进程

- 注释3：**ActivityRecord用于描述一个Activity，用来记录一个Activity的所有信息，在此处创建该对象，用来描述即将启动的Activity**

- 注释4-5：将ActivityRecrod 赋值给outActivity数组，并作为参数传递下去

### 2.2.6  ActivityStarter.startActivity 

### 2.2.7 ActivityStarter.startActivityUnchecked 

		  private int startActivityUnchecked(final ActivityRecord r, ActivityRecord sourceRecord,
		            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
		            int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask,
		            ActivityRecord[] outActivity) {
		...
		 if (mStartActivity.resultTo == null && mInTask == null && !mAddingToTask
		                && (mLaunchFlags & FLAG_ACTIVITY_NEW_TASK) != 0) {//1
		            newTask = true;
		            //创建新的TaskRecord
		            result = setTaskFromReuseOrCreateNewTask(
		                    taskToAffiliate, preferredLaunchStackId, topStack);//2
		        } else if (mSourceRecord != null) {
		            result = setTaskFromSourceRecord();
		        } else if (mInTask != null) {
		            result = setTaskFromInTask();
		        } else {
		            setTaskToCurrentTopOrCreateNewTask();
		        }
		       ...
		 if (mDoResume) {
		            final ActivityRecord topTaskActivity =
		                    mStartActivity.getTask().topRunningActivityLocked();
		            if (!mTargetStack.isFocusable()
		                    || (topTaskActivity != null && topTaskActivity.mTaskOverlay
		                    && mStartActivity != topTaskActivity)) {
		               ...
		            } else {
		                if (mTargetStack.isFocusable() && !mSupervisor.isFocusedStack(mTargetStack)) {
		                    mTargetStack.moveToFront("startActivityUnchecked");
		                }
		                mSupervisor.resumeFocusedStackTopActivityLocked(mTargetStack, mStartActivity,
		                        mOptions);//3
		            }
		        } else {
		            mTargetStack.addRecentActivityLocked(mStartActivity);
		        }
		        ...
		
		}

- 主要处理栈管理相关的逻辑
	
- 注释1：启动一个新的app的Activity，Flag会设置成`FLAG_ACTIVITY_NEW_TASK`
	
- 注释2：`setTaskFromReuseOrCreateNewTask()`该方法内部会创建一个新的TaskRecord，TaskRecord用来描述一个Activity任务栈

### 2.2.8 ActivityStackSupervisor.resumeFocusedStackTopActivityLocked 

		boolean resumeFocusedStackTopActivityLocked(
		        ActivityStack targetStack, ActivityRecord target, ActivityOptions targetOptions) {
		    if (targetStack != null && isFocusedStack(targetStack)) {
		        return targetStack.resumeTopActivityUncheckedLocked(target, targetOptions);
		    }
    		//获取要启动的Activity所在栈的栈顶的处于活动状态的ActivityRecord
		    final ActivityRecord r = mFocusedStack.topRunningActivityLocked();//1
		    if (r == null || r.state != RESUMED) {//2
		        mFocusedStack.resumeTopActivityUncheckedLocked(null, null);//3
		    } else if (r.state == RESUMED) {
		        mFocusedStack.executeAppTransition(targetOptions);
		    }
		    return false;
		}
	
- 注释1： 调用ActivityStack的topRunningActivityLocked方法获取待启动的Activity所在栈的栈顶的ActivityRecord(即获得是否处于停止状态)

- 注释2：如果ActivityRecord不为null，或者待启动的Activity的状态不是RESUMED状态，就会调用 注释3

- 注释3：对于待启动的Activity，注释2是肯定成立的

### 2.2.9  ActivityStack.resumeTopActivityUncheckedLocked

		  boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
		        if (mStackSupervisor.inResumeTopActivity) {
		            return false;
		        }
		        boolean result = false;
		        try {
		            mStackSupervisor.inResumeTopActivity = true;
		            result = resumeTopActivityInnerLocked(prev, options);//1
		        } finally {
		            mStackSupervisor.inResumeTopActivity = false;
		        }
		        mStackSupervisor.checkReadyForSleepLocked();
		        return result;
		    }

### 2.2.10 ActivityStack.resumeTopActivityInnerLocked ->

		private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
		      ...
		           mStackSupervisor.startSpecificActivityLocked(next, true, true);
		       }
		        if (DEBUG_STACK) mStackSupervisor.validateTopActivitiesLocked();
		       return true;
		}

- 这一块的代码非常多，但是只用关注调用了ActivityStackSupervisor的startSpecificActivityLocked方法

### 2.2.11 ActivityStackSupervisor.startSpecificActivityLocked

		void startSpecificActivityLocked(ActivityRecord r,
		            boolean andResume, boolean checkConfig) {
		        //获取即将要启动的Activity的所在的应用程序进程
		        ProcessRecord app = mService.getProcessRecordLocked(r.processName,
		                r.info.applicationInfo.uid, true);//1
		        r.getStack().setLaunchTime(r);
		
		        if (app != null && app.thread != null) {//2
		            try {
		                if ((r.info.flags&ActivityInfo.FLAG_MULTIPROCESS) == 0
		                        || !"android".equals(r.info.packageName)) {
		                    app.addPackage(r.info.packageName, r.info.applicationInfo.versionCode,
		                            mService.mProcessStats);
		                }
		                realStartActivityLocked(r, app, andResume, checkConfig);//3
		                return;
		            } catch (RemoteException e) {
		                Slog.w(TAG, "Exception when starting activity "
		                        + r.intent.getComponent().flattenToShortString(), e);
		            }
		        }
		        mService.startProcessLocked(r.processName, r.info.applicationInfo, true, 0,
		                "activity", r.intent.getComponent(), false, false, true);
		    }

- 注释1：获取即将要启动的Activity的所在的应用程序进程

- 注释2：判断待启动的Activity所在的进程是否已经运行，已经运行的话就会调用`realStartActivityLocked`,该方法的第二个参数代表待启动的Activity的所在进程的ProcessRecord

- **如果待启动的Activity所在进程尚未存在，会调用`AMS.startProcessLocked()`方法，该方法会去调用`Process.start()`方法去通过Zygote孵化应用进程 去创建应用进程，创建成功之后会调用`ActivityThread.main()`,在`main()`方法中会调用`ActivityThread.attach()`,然后会走到AMS中去 **

- [具体的Zygote孵化过程](http://liuwangshu.cn/framework/applicationprocess/1.html)

### 2.2.12  ActivityStackSupervisor.realStartActivityLocked 

			final boolean realStartActivityLocked(ActivityRecord r, ProcessRecord app,
			          boolean andResume, boolean checkConfig) throws RemoteException {
			   ...
			          app.thread.scheduleLaunchActivity(new Intent(r.intent), r.appToken,
			                  System.identityHashCode(r), r.info, new Configuration(mService.mConfiguration),
			                  new Configuration(task.mOverrideConfig), r.compat, r.launchedFromPackage,
			                  task.voiceInteractor, app.repProcState, r.icicle, r.persistentState, results,
			                  newIntents, !andResume, mService.isNextTransitionForward(), profilerInfo);
			  ...      
			      return true;
			  }

 - 这里的app.thread指的是IApplicationThread(实际类型为Binder代理对象,IApplicationThread.Stub.Proxy),它的具体实现是ActivityThread的内部类ApplicationThread(继承了IApplication.Stub)

- app指的是待启动Activity所在的应用程序进程，`app.thread.scheduleLaunchActivity()`指的是要在目标进程中启动Activity。

- 当前代码运行在`system_server`进程，通过IApplicationThread来和应用程序进程进行进程间通讯

### 2.2.13   ApplicationThread.scheduleLaunchActivity

到这一步 就是跳转到ActivityThread中去执行。**可以参考`[ActivityThread分析.md]`**