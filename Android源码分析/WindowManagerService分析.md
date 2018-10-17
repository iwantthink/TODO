# Window分析

[WMS-启动过程](http://gityuan.com/2017/01/08/windowmanger/)

# 1. 简介


# 2. 相关类介绍

- `Surface`

	代表画布

- `WindowManagerService`
	
	添加window的过程,主要功能是添加`Surface`.WMS用来管理所有的`Surface`布局,以及Z轴排序问题

- `SurfaceFinger`

	将`Surface`按次序混合并显示到物理屏幕上

# 3. WMS介绍

- `WMS`继承于`IWindowManager.Stub`.作为Binder本地对象,是具体实现了`WindowManager`功能的类,作为Binder服务端

- `WMS`的成员变量`mSessions`保存着所有的`Session`对象,`Session`继承于`IWindowSession.Stub`, 作为Binder服务端;

- `WMS`的成员变量`mPolicy`:

	实例对象为`PhoneWindowManager`,用于实现各种窗口相关的策略;

- `WMS`的成员变量`mChoreographer`: 

	用于控制窗口动画,屏幕旋转等操作;

- `WMS`的成员变量`mDisplayContents`: 

	记录一组`DisplayContent`对象,这个跟多屏输出相关;

- `WMS`的成员变量`mTokenMap`: 

	保存所有的`WindowToken`对象; 以IBinder为key,可以是IAppWindowToken或者其他Binder的Bp端;

	另一端情况:ActivityRecord.Token extends IApplicationToken.Stub

- `WMS`的成员变量`mWindowMap`: 

	保存所有的`WindowState`对象;以IBinder为key, 是IWindow的Bp端;

	另一端情况: ViewRootImpl.W extends IWindow.Stub

- **一般地,每一个窗口都对应一个`WindowState`对象, 该对象的成员变量`mClient`用于跟应用端交互,成员变量`mToken`用于跟`AMS`交互.**


# 4. 启动过程