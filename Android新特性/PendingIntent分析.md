# PendingIntent分析

[Android官方文档-PendingIntent](https://developer.android.com/reference/android/app/PendingIntent)

[官方文档翻译](http://antiless.com/pending-intent/)

[Stack Overflow - PendingIntent的解释](https://stackoverflow.com/questions/2808796/what-is-an-android-pendingintent)

[Android Intent和PendingIntent的区别](http://www.appblog.cn/2018/03/11/Android%20Intent%E5%92%8CPendingIntent%E7%9A%84%E5%8C%BA%E5%88%AB/)

[彻底认识PendingIntent](http://blog.51cto.com/13616847/2120149)

# 1. 简介

> A description of an Intent and target action to perform with it. Instances of this class are created with `getActivity(Context, int, Intent, int)`, `getActivities(Context, int, Intent[], int)`, `getBroadcast(Context, int, Intent, int)`, and `getService(Context, int, Intent, int)`; the returned object can be handed to other applications so that they can perform the action you described on your behalf at a later time.

- **`PendingIntent`是`Intent`行为的描述类,类似文件句柄用来指向对应的文件. 通过`getActivity()`,`getActivities()`,`getBroadcast()`,`getService()`四个方法创建而来,返回的对象可以被传递给其他应用,这样其他应用就可以在之后执行预先描述的行为**

- 对比理解:

	1. `FileDescriptor`: 描述文件
	2. `Socket`: 描述网络
	3. `PendingIntent`:描述行为,即`IntentDescriptor`行为描述符


> By giving a PendingIntent to another application, you are granting it the right to perform the operation you have specified as if the other application was yourself (with the same permissions and identity). As such, you should be careful about how you build the PendingIntent: almost always, for example, the base Intent you supply should have the component name explicitly set to one of your own components, to ensure it is ultimately sent there and nowhere else.

- 通过将`PendingIntent`传递给其他应用,就相当于授权该应用执行你指定的操作的权利,就像是该应用就是创建`PendingIntent`的应用一样(具体一点就是说,拥有相同的权限和标识). 

	因此,在创建`PendingIntent`的时候需要小心.例如,在创建所提供的基础`Intent`时,需要加上限制用的包名,类名. 以确保`PendingIntent`只被指定的对象利用


> A PendingIntent itself is simply a reference to a token maintained by the system describing the original data used to retrieve it. 
> This means that, even if its owning application's process is killed, the PendingIntent itself will remain usable from other processes that have been given it. 
> If the creating application later re-retrieves the same kind of PendingIntent (same operation, same Intent action, data, categories, and components, and same flags), it will receive a PendingIntent representing the same token if that is still valid, and can thus call cancel() to remove it.

- `PendingIntent`本身只是一个指向系统维护的令牌的引用,该令牌描述了如何获取`PendingIntent`的原始数据 . 这意味着即使创建其的进程被杀死,`PendingIntent`仍然能够在已经持有其的进程中使用. 如果原始的应用在之后重新通过完全相同的参数去获取到相同的`PendingIntent`,它将能够获得一个表示指向相同`PendingIntent`的令牌,只要它仍然有效,就可以调用`cancel()`去移除


> Because of this behavior, it is important to know when two Intents are considered to be the same for purposes of retrieving a PendingIntent. A common mistake people make is to create multiple PendingIntent objects with Intents that only vary in their "extra" contents, expecting to get a different PendingIntent each time. This does not happen. The parts of the Intent that are used for matching are the same ones defined by Intent.filterEquals. If you use two Intent objects that are equivalent as per Intent.filterEquals, then you will get the same PendingIntent for both of them.

- 由于上述的操作,所以需要知道在获取相同`PendingIntent`时,怎么样的俩个`Intent`是被认为相同 . 改变`Intent`的`extra`内容 并不能获取到不同的`PendingIntent`对象

	可以通过`Intent.filterEquals()`方法去判断俩个`Intent`是否相同


> There are two typical ways to deal with this.

> If you truly need multiple distinct PendingIntent objects active at the same time (such as to use as two notifications that are both shown at the same time), then you will need to ensure there is something that is different about them to associate them with different PendingIntents. This may be any of the Intent attributes considered by Intent.filterEquals, or different request code integers supplied to getActivity(Context, int, Intent, int), getActivities(Context, int, Intent[], int), getBroadcast(Context, int, Intent, int), or getService(Context, int, Intent, int).

> If you only need one PendingIntent active at a time for any of the Intents you will use, then you can alternatively use the flags FLAG_CANCEL_CURRENT or FLAG_UPDATE_CURRENT to either cancel or modify whatever current PendingIntent is associated with the Intent you are supplying.

- 可以通过俩种方式可以获取到不同的`PendingIntent`

	1. 改变`Intent.filterEquals()`中任意一个字段

	2. 在获取`PendingIntent`时,使用不同的`request code`

- 如果在同一时间拥有多个`Intent`,但是只想拥有一个`PendingIntent`, 那么可以通过使用`FLAG_CANCEL_CURRENT`或`FLAG_UPDATE_CURRENT` 标志去取消或更新与`PendingIntent`相关联的`Intent`


# 2. Intent与PendingIntent的区别


1. `Intent`即时启动，其随着所在`Activity`的消失而消失
	
	`PendingIntent`看做是对`Intent`的包装,可以等到事件发生后触发，`PendingIntent`可以`cancel`.

	由于`PendingIntent`中 保存有当前App的`Context`，这赋予了外部App一种能力，可以如同当前App一样的执行`PendingIntent`里的 `Intent`， 就算在执行时当前App已经不存在了，也能通过存在PendingIntent里的Context照样执行Intent。另外还可以处理Intent执行后的操作。常和AlarmManager 和NotificationManager一起使用。

2. `Intent`在程序结束后即终止，而`PendingIntent`在程序结束后依然有效

3. `PendingIntent`自带`Context`，而`Intent`需要在某个`Context`内运行

4. `Intent`在原`task`中运行，`PendingIntent`在新的`task`中运行


实例:

- A组件 创建了一个 PendingIntent 的对象然后传给 B组件，B 在执行这个 PendingIntent 的 send 时候，它里面的 Intent 会被发送出去，而接受到这个 Intent 的 C 组件会认为是 A 发的。

	B 以 A 的权限和身份发送了这个 Intent。

- 比如，我们的 Activity 如果设置了 exported = false，其他应用如果使用 Intent 就访问不到这个 Activity，但是使用 PendingIntent 是可以的。



# 3. 常见的PendingIntent静态方法分析

	PendingIntent.getActivity()
	PendingIntent.getService()
	PendingIntent.getBroadcast()

- 以上三个方法最终都会调用到`AlarmManagerService.getIntentSender()`


## 3.1 PendingIntent.getActivity()

    public static PendingIntent getActivity(Context context, int requestCode,
            @NonNull Intent intent, @Flags int flags, @Nullable Bundle options) {
		// 包名
        String packageName = context.getPackageName();
        String resolvedType = intent != null ? intent.resolveTypeIfNeeded(
                context.getContentResolver()) : null;
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(context);
            IIntentSender target =
                ActivityManager.getService().getIntentSender(
                    ActivityManager.INTENT_SENDER_ACTIVITY, packageName,
                    null, null, requestCode, new Intent[] { intent },
                    resolvedType != null ? new String[] { resolvedType } : null,
                    flags, options, UserHandle.myUserId());
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

## 3.2 PendingIntent.getService()

    public static PendingIntent getService(Context context, int requestCode,
            @NonNull Intent intent, @Flags int flags) {
        return buildServicePendingIntent(context, requestCode, intent, flags,
                ActivityManager.INTENT_SENDER_SERVICE);
    }

    private static PendingIntent buildServicePendingIntent(Context context, int requestCode,
            Intent intent, int flags, int serviceKind) {
        String packageName = context.getPackageName();
        String resolvedType = intent != null ? intent.resolveTypeIfNeeded(
                context.getContentResolver()) : null;
        try {
            intent.prepareToLeaveProcess(context);
            IIntentSender target =
                ActivityManager.getService().getIntentSender(
                    serviceKind, packageName,
                    null, null, requestCode, new Intent[] { intent },
                    resolvedType != null ? new String[] { resolvedType } : null,
                    flags, null, UserHandle.myUserId());
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

## 3.3 PendingIntent.getBroadcast()

    public static PendingIntent getBroadcastAsUser(Context context, int requestCode,
            Intent intent, int flags, UserHandle userHandle) {
        String packageName = context.getPackageName();
        String resolvedType = intent != null ? intent.resolveTypeIfNeeded(
                context.getContentResolver()) : null;
        try {
            intent.prepareToLeaveProcess(context);
            IIntentSender target =
                ActivityManager.getService().getIntentSender(
                    ActivityManager.INTENT_SENDER_BROADCAST, packageName,
                    null, null, requestCode, new Intent[] { intent },
                    resolvedType != null ? new String[] { resolvedType } : null,
                    flags, null, userHandle.getIdentifier());
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }


## 3.4 AMS.getIntentSender()

    public static IActivityManager getService() {
        return IActivityManagerSingleton.get();
    }

    private static final Singleton<IActivityManager> IActivityManagerSingleton =
            new Singleton<IActivityManager>() {
                @Override
                protected IActivityManager create() {
                    final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
					// 转换成Proxy
                    final IActivityManager am = IActivityManager.Stub.asInterface(b);
                    return am;
                }
            };

- 在`SystemServer`类中,`AMS`被注册到`ServiceManager`中


	public IIntentSender getIntentSender(int type, String packageName, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle options, int userId) {
	    //重新拷贝一次intent对象内容
	    if (intents != null) {
	        for (int i=0; i<intents.length; i++) {
	            Intent intent = intents[i];
	            if (intent != null) {
	                intents[i] = new Intent(intent);
	            }
	        }
	    }
	    ...
	
	    synchronized(this) {
	        int callingUid = Binder.getCallingUid();
	        int origUserId = userId;
	        userId = handleIncomingUser(Binder.getCallingPid(), callingUid, userId,
	                type == ActivityManager.INTENT_SENDER_BROADCAST,
	                ALLOW_NON_FULL, "getIntentSender", null);
	        if (origUserId == UserHandle.USER_CURRENT) {
	            userId = UserHandle.USER_CURRENT;
	        }

	        return getIntentSenderLocked(type, packageName, callingUid, userId,
	              token, resultWho, requestCode, intents, resolvedTypes, flags, options);
	    }
	}

- 此处的`packageName`为创建`PendingIntent`所在进程的包名,后续会把该信息保存到`PendingIntentRecord.Key`


## 3.5 AMS.getIntentSenderLocked()

	IIntentSender getIntentSenderLocked(int type, String packageName, int callingUid, int userId, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle options) {
	    ActivityRecord activity = null;
	    ...
	    //创建Key对象
	    PendingIntentRecord.Key key = new PendingIntentRecord.Key(
	            type, packageName, activity, resultWho,
	            requestCode, intents, resolvedTypes, flags, options, userId);
	    WeakReference<PendingIntentRecord> ref;
	    ref = mIntentSenderRecords.get(key);
	    PendingIntentRecord rec = ref != null ? ref.get() : null;
	    if (rec != null) {
	        if (!cancelCurrent) {
	            if (updateCurrent) {
	                if (rec.key.requestIntent != null) {
	                    rec.key.requestIntent.replaceExtras(intents != null ?
	                            intents[intents.length - 1] : null);
	                }
	                if (intents != null) {
	                    intents[intents.length-1] = rec.key.requestIntent;
	                    rec.key.allIntents = intents;
	                    rec.key.allResolvedTypes = resolvedTypes;
	                } else {
	                    rec.key.allIntents = null;
	                    rec.key.allResolvedTypes = null;
	                }
	            }
	            return rec;
	        }
	        rec.canceled = true;
	        mIntentSenderRecords.remove(key);
	    }
	    if (noCreate) {
	        return rec;
	    }
	    //创建PendingIntentRecord对象
	    rec = new PendingIntentRecord(this, key, callingUid);
	    mIntentSenderRecords.put(key, rec.ref);
	    ...
	    return rec;
	}