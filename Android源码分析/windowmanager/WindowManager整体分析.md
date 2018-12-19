# WindwoManager整体分析

[Android解析WindowManager（一）WindowManager体系](http://liuwangshu.cn/framework/wm/1-windowmanager.html)

[Android开发艺术探索]()

[深入理解Android 卷III]()

# 1. 简介

# 2. Window,WindowManager和WMS

![](http://upload-images.jianshu.io/upload_images/1417629-4e2047a49e2572f3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 2.1 Window

`Window`表示一个窗口的概念,它是一个抽象类,具体实现是`PhoneWindow`

Android中所有的视图都是通过`Window`来呈现的,不管是`Activity`,`Dialog`,`Toast`,它们的视图都是附加在`Window`上,**因此`Window`是`View`的直接管理者**

`Window`并不是真实存在的,实体是`View`

## 2.2 WindowManager

`WindowManager`是外界访问`Window`的入口,作为一个接口类,继承自接口`ViewManager`,是用来管理`Window`的(可以通过WM对`Window`进行添加和删除的操作).

它的具体实现类是`WindowManagerImpl`

## 2.3 WindowManagerService

WMS与WM通过Binder进行IPC,WMS作为系统级别的服务,并不会将全部API暴露给WM,这一点与`ActivityManager`和`ActivityManagerService`相似

主要功能包括`Window`管理和输入系统

# 3. WindowManager介绍

`WindowManager`是一个接口类，继承自接口`ViewManager`.WM在继承VM的同时,又加入了很多功能,包括Window类型,层级相关的常量,内部类以及一些方法

其中存在俩个方法是根据`Window`特性加入

	public Display getDefaultDisplay();
	public void removeViewImmediate(View view);

- `getDefaultDisplay()`方法会得知这个`WindowManager`实例将`Window`添加到哪个屏幕上了，换句话说，就是得到`WindowManager`所管理的屏幕（`Display`）。

- `removeViewImmediate()`方法则规定在这个方法返回前要立即执行`View.onDetachedFromWindow()`，来完成传入的View相关的销毁工作


## 3.1 ViewManager

	public interface ViewManager
	{
	    /**
	     * Assign the passed LayoutParams to the passed View and add the view to the window.
	     * 
	     * @param view The view to be added to this window.
	     * @param params The LayoutParams to assign to view.
	     */
	    public void addView(View view, ViewGroup.LayoutParams params);
	    public void updateViewLayout(View view, ViewGroup.LayoutParams params);
	    public void removeView(View view);
	}

- `ViewManager`定义了三个方法,分别用来添加,更新和删除view

	另外,**这三个方法都是直接对`View`进行操作,说明WM具体管理的是以`View`形式存在的`Window`**


# 4. 整体关系结构图

![](http://upload-images.jianshu.io/upload_images/1417629-d398194cb0b50bae.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

`PhoneWindow`继承自`Window`，`Window`通过`setWindowManager`方法与`WindowManager`发生关联。`WindowManager`继承自接口`ViewManager`，`WindowManagerImpl`是`WindowManager`接口的实现类，但是具体的功能都会委托给`WindowManagerGlobal`来实现