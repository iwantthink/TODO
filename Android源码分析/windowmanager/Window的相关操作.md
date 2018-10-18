# Window的相关操作

[Android解析WindowManager（三）Window的添加过程](http://liuwangshu.cn/framework/wm/3-add-window.html)

[Android开发艺术探索]()

# 1. 简介

本文基于android-27

`WindowManager`对`Window`进行管理，说到管理那就离不开对`Window`的添加、更新和删除的操作。

对于Window的操作，最终都是交由`WMS`来进行处理。

窗口的操作分为两大部分，

1. 一部分是WindowManager处理部分，

2. 另一部分是WMS处理部分。

已知`Window`分为三大类，分别是：`Application Window`（应用程序窗口）、`Sub Windwow`（子窗口）和`System Window`（系统窗口），对于不同类型的窗口添加过程会有所不同，但是对于`WMS`处理部分，添加的过程基本上是一样的， `WMS`对于这三大类的窗口基本是“一视同仁”的。

![](http://upload-images.jianshu.io/upload_images/1417629-a2307e2c73db270d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 这篇文章仅分析`WindowManager`处理的部分,与WMS相关的内容可以在[WindowManagerService分析.md]()中查看

# 2. Window的添加过程

`Window`的添加过程需要通过`WindowManager`的`addView()`来实现,而`WindowManager`是一个接口,它真正的实现是`WindowManagerImpl`类.

## 2.1 WindowManagerImpl.addView()

    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }

- `applyDefaultToken()`方法与Window的层级有关系

- `WindowManagerImpl`并没有直接实现Window的添加操作,而是全部交给了`WindowManagerGlobal`类来处理

- 这种工作模式是典型的桥接模式,将所有的操作委托给`WindowManagerGlobal`来实现

- 这里的`mContext`实际是`ContextImpl`类型,在`WindowManagerImpl`被创建时传入

- `mParentWindow`是当前Window对象

## 2.2 WindowManagerGlobal.addView()

	public void addView(View view, ViewGroup.LayoutParams params,
	          Display display, Window parentWindow) {
	    ...//参数检查
	      final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
		  // 判断当前添加进来的view是否属于子Window,如果是则需要调整布局参数
	      if (parentWindow != null) {
	          parentWindow.adjustLayoutParamsForSubWindow(wparams);
	      } else {
	      ...
	      }
	
	      ViewRootImpl root;
	      View panelParentView = null;
	       	  //创建ViewRootImpl
	          root = new ViewRootImpl(view.getContext(), display);
			  //添加LayoutParamas
	          view.setLayoutParams(wparams);
	          mViews.add(view);
	          mRoots.add(root);
	          mParams.add(wparams);
	      }
	      try {
	          root.setView(view, wparams, panelParentView);
	      } catch (RuntimeException e) {
	         ...
	      }
	  }

- 在这个方法中,创建了`ViewRootImpl`,并将最终添加View的实现交给`ViewRootImpl`来实现


## 2.2.1 WindowManagerGlobal 重要的列表

	//存储所有Window对应的View
    private final ArrayList<View> mViews = new ArrayList<View>();
	//存储所有Window对应的ViewRootImpl
    private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();
	//存储所有Window对应的布局参数
    private final ArrayList<WindowManager.LayoutParams> mParams =
            new ArrayList<WindowManager.LayoutParams>();
	//存储正在被删除的View对象,或者说是已经调用了removeView方法,
	//但是删除操作还未完成的Window对象
    private final ArraySet<View> mDyingViews = new ArraySet<View>();

## 2.3 ViewRootImpl

ViewRootImpl身负了很多职责：

- View树的根并管理View树

- 触发View的测量、布局和绘制

- 输入事件的中转站

- 管理Surface

- 负责与WMS进行进程间通信

### 2.3.1 ViewRootImpl.setView()
	
	public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
	       synchronized (this) {
	          ...
	               try {
	                   mOrigWindowType = mWindowAttributes.type;
	                   mAttachInfo.mRecomputeGlobalAttributes = true;
	                   collectViewAttributes();
	                   res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
	                           getHostVisibility(), mDisplay.getDisplayId(),
	                           mAttachInfo.mContentInsets, mAttachInfo.mStableInsets,
	                           mAttachInfo.mOutsets, mInputChannel);
	               } 
	               ...
	   }

- `mWindowSession`是`IWindowSession`类型的，它是一个`Binder`对象，用于进行进程间通信，`IWindowSession`是`Client`端的代理，它的`Server`端的实现为`Session`，此前包含`ViewRootImpl`在内的代码逻辑都是运行在本地进程的，而`Session`的`addToDisplay`方法则运行在WMS所在的进程,这里其实就是一次IPC