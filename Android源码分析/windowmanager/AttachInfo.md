# AttachInfo


# 1. AttachInfo的创建

在`WindowManagerGlobal.addView()`中会创建`ViewRootImpl`,`AttachInfo`正是在这里被创建

## 1.1 ViewRootImpl构造函数

    public ViewRootImpl(Context context, Display display) {
		.............
        mWindowSession = WindowManagerGlobal.getWindowSession();
        mWindow = new W(this);
        mAttachInfo = new View.AttachInfo(mWindowSession, mWindow, display, this, mHandler, this,
                context);
		.........
	}

- `mWindowSession`是通过`WindowManagerGlobal.getWindowSession`获得的一个Binder服务代理(`IWindowSession.Stub`)，是App端向WMS端发送消息的通道. 实际上`Session`只是将请求转交给了`WMS`

	**`WindowSession`具体创建过程可以查看[WMS分析3-具体案例.md]()的`3.3.1`小节**

- `mWindow`是一个`W extends IWindow.Stub`类型的Binder对象，其主要作用是传递给WMS端，作为WMS向APP端发送消息的通道


## 1.2 AttachInfo 构造函数

        AttachInfo(IWindowSession session, IWindow window, Display display,
                ViewRootImpl viewRootImpl, Handler handler, Callbacks effectPlayer,
                Context context) {
            mSession = session;
            mWindow = window;
            mWindowToken = window.asBinder();
            mDisplay = display;
            mViewRootImpl = viewRootImpl;
            mHandler = handler;
            mRootCallbacks = effectPlayer;
            mTreeObserver = new ViewTreeObserver(context);
        }

# 2. AttachInfo在控件中的分发

**在`ViewRootImpl`类的`performTraversals()`方法中,会调用`DecorView`的`dispatchAttachedToWindow()`方法将`View.AttachInfo`传递到控件系统中!**


方法`dispatchAttachedToWindow()`在`ViewGroup`中对`View`中的实现进行了重写,遍历子类分发`AttachInfo`

`DecorView`继承自`FrameLayout`,所以在`ViewRootImpl`中调用的是`ViewGroup`的`dispatchAttachedToWindow()`

- `FrameLayout`并没有重写...


## 2.1 ViewRootImpl.performTraversals()

    private void performTraversals() {
        final View host = mView;
		.......
        if (mFirst) {
			...........
            host.dispatchAttachedToWindow(mAttachInfo, 0);
        } 
		..........
	}

- `visibility`传的是一个固定的值,表示`VISIBLE`

## 2.1 View.dispatchAttachedToWindow()

    void dispatchAttachedToWindow(AttachInfo info, int visibility) {
        mAttachInfo = info;
 		............
    }

## 2.2 ViewGroup.dispatchAttachedToWindow()

    void dispatchAttachedToWindow(AttachInfo info, int visibility) {

		// 将AttacInfo赋值给自己
        super.dispatchAttachedToWindow(info, visibility);
		// 遍历子类,传递AttachInfo
        final int count = mChildrenCount;
        final View[] children = mChildren;
        for (int i = 0; i < count; i++) {
            final View child = children[i];
            child.dispatchAttachedToWindow(info,
                    combineVisibility(visibility, child.getVisibility()));
        }
        final int transientCount = mTransientIndices == null ? 0 : mTransientIndices.size();
        for (int i = 0; i < transientCount; ++i) {
            View view = mTransientViews.get(i);
            view.dispatchAttachedToWindow(info,
                    combineVisibility(visibility, view.getVisibility()));
        }
    }

-  主要功能就是 将`AttachInfo`分别分发给自己和子类

