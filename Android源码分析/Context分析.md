# Context分析

[完全剖析Android上下文环境Context](https://xujiaojie.github.io/2017/09/18/%E5%AE%8C%E5%85%A8%E5%89%96%E6%9E%90Android%E4%B8%8A%E4%B8%8B%E6%96%87%E7%8E%AF%E5%A2%83Context/)

[Android 官方文档](https://developer.android.com/reference/android/content/Context)
# 1. 简介

1. 应用程序环境信息的接口，表示上下文的意思。

2. `Context`是一个由Android系统提供实现类的抽象类(这个实现类即`ContextImpl`,这里用到了一个代理模式，通过`ContextWrapper`代理了`ContextImpl`)

3. 通过`Context`类可以允许访问特定应用程序的资源和类，以及对应用级操作的调用（比如启动活动，广播和接受意图等等）。

## 1.1 Context继承关系

![Context继承关系](http://ww1.sinaimg.cn/large/6ab93b35ly1frao9xdnfoj20bj08jaac.jpg)

![](http://otpesi023.bkt.clouddn.com/Context1.jpg)

**`Context`的实际实现类是`ContextImpl`，是一个注解了的内部保护文件。是Context api的通用实现，提供了基本的Context给Activity和Application组件使用**

- `ContextWrapper`类中存在一个变量`mBase`是一个`ContextImpl`对象，指向真正的实现类`ContextImpl`.而`ContextImpl`类中的变量`mOuterContext`指向相对应的`Activity,Service,Application`

	可以查看`ContextWrapper`的源码：

	    /**
	     * Set the base context for this ContextWrapper.  All calls will then be
	     * delegated to the base context.  Throws
	     * IllegalStateException if a base context has already been set.
	     * 
	     * @param base The new base context for this wrapper.
	     */
	    protected void attachBaseContext(Context base) {
	        if (mBase != null) {
	            throw new IllegalStateException("Base context already set");
	        }
	        mBase = base;
	    }

	Activity被创建的时候，会被调用其`attach()`方法，在`attach()`方法中 就会调用`attachBaseContext()`方法 来将创建出来的`ContextImpl`传给`ContextWrapper`




## 1.2 应用程序中Context的数量

总Context数量 = Activity个数+Service个数+Application


# 2. Context 创建过程

## 2.1 Application Context创建过程

当应用程序被启动的过程中，`Application`通过`LoadedApk.makeApplication()`被创建：

1. Android 系统Framework框架里的ActivityManagerService 会调度Activity的创建，在这个流程中，其中一步是调用`ActivityTherad.scheduleLaunchActivity()`

2. `ActivityThread.scheduleLaunchActivity()`方法会通过Handler 将线程切换到主线程，最终会在`ActivityThread.handleBindApplication()`方法中执行




