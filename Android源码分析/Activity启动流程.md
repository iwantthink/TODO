# Activity启动流程

[深入理解Activity启动流程](http://www.cloudchou.com/android/post-788.html)

[Launcher源码](https://android.googlesource.com/platform/packages/apps/Launcher/+/master/src/com/android/launcher/Launcher.java)

[Android源码分析-Activity的启动过程-任玉刚](https://blog.csdn.net/singwhatiwanna/article/details/18154335)

# 1. 概述

系统从开机之后就会启动一个`Launcher`程序(即桌面),然后可以通过点击应用图标来启动应用的入口Activity，**在这个过程中(Activity的启动)需要多个进程之间交互**

Android系统中有一个`zygote`进程专用于孵化Android框架层和应用层程序的进程。有一个`system_server`进程，该进程运行了很多提供Binder的service，例如：`ActivityManagerService,PackageManagerService,WindowManagerService`等，这些服务分别运行在不同的线程中

`ActivityManagerService`就是负责管理Activity栈，应用程序，task等

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fqzbxttc06j20ln0bq74f.jpg)

1. 用户在Launcher程序里点击应用图标时，会通知ActivityManagerService启动应用的入口Activity

2. ActivityManagerService发现这个应用还未启动，则会通知Zygote进程孵化出应用进程，然后在这个dalvik/ART应用进程里执行ActivityThread的main方法。

3. 应用进程接下来通知ActivityManagerService应用进程已启动，ActivityManagerService保存应用进程的一个Binder代理对象，这样ActivityManagerService可以通过这个代理对象控制应用进程

5. 然后ActivityManagerService通知应用进程创建入口Activity的实例，并执行它的生命周期方法。

**ActivityManagerService管理Activity时，主要涉及以下几个类:**

1. ActivityManagerService，它是管理activity的入口类，聚合了ProcessRecord对象和ActivityStack对象

2. ProcessRecord，表示应用进程记录，每个应用进程都有对应的ProcessRecord对象

3. ActivityStack，该类主要管理回退栈

4. ActivityRecord，每次启动一个Actvity会有一个对应的ActivityRecord对象，表示Activity的一个记录

5. ActivityInfo，Activity的信息，比如启动模式，taskAffinity，flag信息(这些信息在AndroidManifest.xml里声明Activity时填写)

6. TaskRecord，Task记录信息，一个Task可能有多个ActivityRecord，但是一个ActivityRecord只能属于一个TaskRecord


# 2. Activity启动流程简介

1. Activity调用ActivityManagerService启动应用

2. ActivityManagerService调用Zygote孵化应用进程

3. Zygote进程孵化应用进程

4. 新进程启动ActivityThread

5. 应用进程绑定到ActivityManagerService

6. ActivityThread的Handler处理启动Activity的消息

**分析基于Android 26 源码**

## 2.1 Activity调用AMS启动应用

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fqzevkrkp4j20ph0dh0su.jpg)


点击Launcher应用的桌面图标之后，Launcher程序会调用startActivity启动应用。**最终会走到Instrumentation的execStartActivity来启动应用**

    /**
     * Like {@link #execStartActivity(Context, IBinder, IBinder, Activity, Intent, int, Bundle)},
     * but for starting as a particular user.
     *
     * @param who The Context from which the activity is being started.
     * @param contextThread The main thread of the Context from which the activity
     *                      is being started.
     * @param token Internal token identifying to the system who is starting
     *              the activity; may be null.
     * @param target Which fragment is performing the start (and thus receiving
     *               any result).
     * @param intent The actual Intent to start.
     * @param requestCode Identifier for this request's result; less than zero
     *                    if the caller is not expecting a result.
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
            Context who, IBinder contextThread, IBinder token, String resultWho,
            Intent intent, int requestCode, Bundle options, UserHandle user) {
		//核心功能在这个whoThread中完成，其内部scheduleLaunchActivity方法用于完成activity的打开  
        IApplicationThread whoThread = (IApplicationThread) contextThread;
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
                        if (am.isBlocking()) {
							//当前监视器组织activity启动
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
                .startActivityAsUser(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, resultWho,
                        requestCode, 0, null, options, user.getIdentifier());
			//检查AMS的处理结果，如果无法打开activity 会抛出各种异常
            checkStartActivityResult(result, intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
        return null;
    }



[Instrumentation源码](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/Instrumentation.java)

## 2.2 AMS调用Zygote孵化应用进程

从2.1中可以看出 Instrumentation将具体的开启 交给了AMS来处理