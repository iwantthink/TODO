# 基于动态代理的Hook机制

[设计模式之代理模式](https://juejin.im/entry/589051a60ce4630056dca6ab)

[Android插件化原理解析--Hook机制之动态代理](http://weishu.me/2016/01/28/understand-plugin-framework-proxy-hook/)

# 1. 代理是什么？

**代理就是为其他对象（被代理对象）提供一种控制其操作行为。实际的行为是由被代理对象完成的**

可以查看`[Java静态代理和动态代理.md]`来了解更多

# 2. Hook是什么？

通过代理，可以获得一个功能比原始对象更强大的代理对象，同时这个代理对象可以替换掉原始对象来使用， 做一些参数修改，替换返回值，增加逻辑等操作。**以上的行为可以称之为Hook。**


# 3. Hook流程介绍

1. 寻找Hook点，原则是静态变量或者单例对象，尽量Hook pulic的对象和方法，非public不保证每个版本都一样，需要适配。

2. 选择合适的代理方式，如果是接口可以用动态代理；如果是类可以手动写代理也可以使用cglib。

3. 偷梁换柱——用代理对象替换原始对象

## 3.1 寻找Hook对象

要做Hook操作，必须要先确定被Hook的对象，怎么样的对象比较方便Hook？

- **容易找到的对象，例如 静态变量 和 单例**

	在一个进程中，静态变量 和 单例 是相对不容易发生变化的，因此非常容易定位，而普通对象要么无法标志，要么容易改变


## 3.2 选择合理的代理方式

如果遇到的是接口类型，那么可以使用动态代理

如果遇到的是实体类，那么可以考虑使用静态代理，即手写代理


# 4.实例

**Hook掉`startActivity()`**

- `startActivity()`这个方法俩个地方有，Context和Activity。

**继承关系：**

- Context->ContextWrapper->ContextThemeWrapper->Activity

Context中包含了抽象的startActivity()方法。ContextWrapper实现了抽象方法，并将这个方法转交给mBase(即ContextImpl)去实现。Activity再次重写了这个方法。


## 4.1 Context.startActivity()

**ContextWrapper.startActivity():**

	  @Override
	    public void startActivity(Intent intent) {
	        mBase.startActivity(intent);
	    }

- mBase是一个ContextImpl对象，是在启动Activity流程中，`ActivityThread.performLaunchActivity()`调用`activity.attach()`传入的.

**ContextImpl.startActivity():**

    @Override
    public void startActivity(Intent intent, Bundle options) {
        warnIfCallingFromSystemProcess();

        // Calling start activity from outside an activity without FLAG_ACTIVITY_NEW_TASK is
        // generally not allowed, except if the caller specifies the task id the activity should
        // be launched in.
        if ((intent.getFlags()&Intent.FLAG_ACTIVITY_NEW_TASK) == 0
                && options != null && ActivityOptions.fromBundle(options).getLaunchTaskId() == -1) {
            throw new AndroidRuntimeException(
                    "Calling startActivity() from outside of an Activity "
                    + " context requires the FLAG_ACTIVITY_NEW_TASK flag."
                    + " Is this really what you want?");
        }
        mMainThread.getInstrumentation().execStartActivity(
                getOuterContext(), mMainThread.getApplicationThread(), null,
                (Activity) null, intent, -1, options);
    }

- 检查intent中的Flag是否包含`FLAG_ACTIVITY_NEW_TASK`

- 调用`ActivityThread`中的`Instrumentation`的`execStartActivity()`方法

ActivityThread是程序的入口，其对象保存在ActivityThread的sCurrentActivityThread静态变量中，那么作为其成员变量`mInstrumentation`也是唯一的。**所以可以选择ActivityThread的mInstrumentation作为Hook点，只要Hook掉这里**

## 4.2 Activity.startActivity()

**Activity.startActivity():**

	public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
            @Nullable Bundle options) {
		``省略代码```
        Instrumentation.ActivityResult ar =
                mInstrumentation.execStartActivity(
                    this, mMainThread.getApplicationThread(), mToken, this,
                    intent, requestCode, options);
		```省略代码```
    }

- 这里调用了Activity的mInstrumentation.execStartActivity()。**可以选择这里的mInstrumentation作为Hook点，但是这只能针对单个Activity，即要对每一个Activity都Hook，所以这肯定不合理，继续寻找**


**Instrumentation.execStartActivity()**

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
		``省略代码```
        try {
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

- 通过`ActivityManager.getService()`获取到AMS的远程代理Binder，通过代理Binder去调用`startActivity()`

**ActivityManager.getService():**

    /**
     * @hide
     */
    public static IActivityManager getService() {
        return IActivityManagerSingleton.get();
    }

    private static final Singleton<IActivityManager> IActivityManagerSingleton =
            new Singleton<IActivityManager>() {
                @Override
                protected IActivityManager create() {
                    final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                    final IActivityManager am = IActivityManager.Stub.asInterface(b);
                    return am;
                }
            };

- `Singleton`是一个懒加载的单例类,**可以选择IActivityManagerSingleton的mInstance成员变量作为Hookd对象，所有的Activity.startActivity()最终都会调用这个方法，所以选择这个Hook点是正确的！**