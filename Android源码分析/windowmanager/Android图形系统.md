# Android图形系统.md

[Android图形系统概述](http://gityuan.com/2017/02/05/graphic_arch/)

[官方-系统级图形架构](https://source.android.com/devices/graphics/architecture.html)

# 1. 简介

Android系统的图形系统包括`WindowManager`,`SurfaceFlinger`,`Open GL`,`GPU`等模块.其中`SurfaceFlinger`作为负责绘制应用UI的核心，从名字可以看出其功能是将所有Surface合成工作。 不论使用什么渲染API, 所有的东西最终都是渲染到`surface`. `surface`代表`BufferQueue`的生产者端, 并且 由`SurfaceFlinger`所消费, 这便是基本的生产者-消费者模式. Android平台所创建的`Window`都由`surface`所支持, 所有可见的`surface`渲染到显示设备都是通过`SurfaceFlinger`来完成的.

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