# Window属性介绍

[Android解析WindowManager（二）Window的属性](http://liuwangshu.cn/framework/wm/2-window-property.html)

[Android开发艺术探索]()

# 1. 简介

`WMS`是`Window的`的管理者，为了方便管理还定义了一些“协议”，这些“协议”就是Window的属性，被定义在`WindowManager`的内部类`LayoutParams`中，了解Window的属性能够更好的理解WMS的内部原理。

**`Window`的属性有很多种,跟开发最密切的有三种**

1. **Type(Window的类型)**

2. **Flag(Window的标志)**

3. **SoftInputMode(软键盘相关模式)**

# 2. Window的类型和显示次序

`Window`的类型有很多种，比如应用程序窗口、系统错误窗口、输入法窗口、PopupWindow、Toast、Dialog等等。

**总来来说分为三大类分别是：`Application Window`（应用程序窗口）、`Sub Windwow`（子窗口）、`System Window`（系统窗口），每个大类又包含了很多种类型**

- **它们都定义在`WindowManager`的静态内部类`LayoutParams`中**

## 2.1 Application Window(应用窗口)

Activity就是一个典型的应用程序窗口，应用程序窗口包含的类型如下所示。

	//应用程序窗口类型初始值
	public static final int FIRST_APPLICATION_WINDOW = 1;
	//窗口的基础值，其他的窗口值要大于这个值
	public static final int TYPE_BASE_APPLICATION   = 1;
	//普通的应用程序窗口类型
	public static final int TYPE_APPLICATION        = 2;
	//应用程序启动窗口类型，用于系统在应用程序窗口启动前显示的窗口。
	public static final int TYPE_APPLICATION_STARTING = 3;
	public static final int TYPE_DRAWN_APPLICATION = 4;
	//应用程序窗口类型结束值
	public static final int LAST_APPLICATION_WINDOW = 99;

- 应用窗口的`Type`值范围是1-99,这个数值涉及到窗口的层级


窗口TYPE值|窗口类型
:---:|:---:
`FIRST_APPLICATION_WINDOW = 1`|应用程序窗口的初始值
`TYPE_BASE_APPLICATION=1`|所有程序窗口的base窗口，其他应用程序窗口都显示在它上面
`TYPE_APPLICATION    =2`|普通应用程序窗口，对应的`token`必须设置为Activity的token
`TYPE_APPLICATION_STARTING =3`|应用程序启动时所显示的窗口
`LAST_APPLICATION_WINDOW = 99`|结束应用程序窗口

- 一般Activity都是`TYPE_BASE_APPLICATION`类型的，而`TYPE_APPLICATION`主要是用于Dialo


## 2.2 Sub Window(子窗口)

子窗口**,不能独立的存在**，需要附着在其他窗口才可以，`PopupWindow`就属于子窗口。子窗口的类型定义如下所示：

	//子窗口类型初始值
	public static final int FIRST_SUB_WINDOW = 1000;
	public static final int TYPE_APPLICATION_PANEL = FIRST_SUB_WINDOW;
	public static final int TYPE_APPLICATION_MEDIA = FIRST_SUB_WINDOW + 1;
	public static final int TYPE_APPLICATION_SUB_PANEL = FIRST_SUB_WINDOW + 2;
	public static final int TYPE_APPLICATION_ATTACHED_DIALOG = FIRST_SUB_WINDOW + 3;
	public static final int TYPE_APPLICATION_MEDIA_OVERLAY  = FIRST_SUB_WINDOW + 4; 
	public static final int TYPE_APPLICATION_ABOVE_SUB_PANEL = FIRST_SUB_WINDOW + 5;
	//子窗口类型结束值
	public static final int LAST_SUB_WINDOW = 1999;

- 子窗口的`Type`值范围为1000到1999。


窗口TYPE值|窗口类型
:---:|:---:
`FIRST_SUB_WINDOW = 1000`|SubWindows子窗口，子窗口的Z序和坐标空间都依赖于他们的宿主窗口
`TYPE_APPLICATION_PANEL =1000`|面板窗口，显示于宿主窗口的上层
`TYPE_APPLICATION_MEDIA    =1001`|媒体窗口（例如视频），显示于宿主窗口下层
`TYPE_APPLICATION_SUB_PANEL =1002`|应用程序窗口的子面板，显示于所有面板窗口的上层
`TYPE_APPLICATION_ATTACHED_DIALOG = 1003`|对话框，类似于面板窗口，绘制类似于顶层窗口，而不是宿主的子窗口
`TYPE_APPLICATION_MEDIA_OVERLAY =1004`|媒体信息，显示在媒体层和程序窗口之间，需要实现半透明效果
`LAST_SUB_WINDOW=1999`|结束子窗口


## 2.3 System Window(系统窗口)

Toast、输入法窗口、系统音量条窗口、系统错误窗口都属于系统窗口。系统窗口的类型定义如下所示：
	
	//系统窗口类型初始值
	public static final int FIRST_SYSTEM_WINDOW     = 2000;
	//系统状态栏窗口
	public static final int TYPE_STATUS_BAR         = FIRST_SYSTEM_WINDOW;
	//搜索条窗口
	public static final int TYPE_SEARCH_BAR         = FIRST_SYSTEM_WINDOW+1;
	//通话窗口
	public static final int TYPE_PHONE              = FIRST_SYSTEM_WINDOW+2;
	//系统ALERT窗口
	public static final int TYPE_SYSTEM_ALERT       = FIRST_SYSTEM_WINDOW+3;
	//锁屏窗口
	public static final int TYPE_KEYGUARD           = FIRST_SYSTEM_WINDOW+4;
	//TOAST窗口
	public static final int TYPE_TOAST              = FIRST_SYSTEM_WINDOW+5;
	...
	//系统窗口类型结束值
	public static final int LAST_SYSTEM_WINDOW      = 2999;

- 系统窗口的类型值有接近40个，这里只列出了一小部分， 系统窗口的`Type`值范围为2000到2999。


窗口TYPE值|窗口类型
:---:|:---:
`FIRST_SYSTEM_WINDOW = 2000`|系统窗口
`TYPE_STATUS_BAR     = FIRST_SYSTEM_WINDOW`|状态栏
`TYPE_SYSTEM_ALERT   = FIRST_SYSTEM_WINDOW+3`|系统提示，出现在应用程序窗口之上
`TYPE_TOAST          = FIRST_SYSTEM_WINDOW+5`|显示Toast


## 2.4 窗口显示次序

当一个进程向`WMS`申请一个窗口时，`WMS`会为窗口确定显示次序。

- 为了方便窗口显示次序的管理，手机屏幕可以虚拟的用X、Y、Z轴来表示，其中Z轴垂直于屏幕，从屏幕内指向屏幕外，这样确定窗口显示次序也就是确定窗口在Z轴上的次序，这个次序称为Z-Oder。

	Type值是Z-Oder排序的依据，我们知道应用程序窗口的Type值范围为1到99，子窗口1000到1999 ，系统窗口 2000到2999. **一般情况下，Type值越大则Z-Oder排序越靠前，就越靠近用户。当然窗口显示次序的逻辑不会这么简单，情况会比较多**

	举个常见的情况：当多个窗口的Type值都是`TYPE_APPLICATION`，这时WMS会结合各种情况给出最终的Z-Oder，这个逻辑不在本文的讨论范围，这里我们只需要知道窗口显示次序的基本规则就好。

# 3. Window的标志

`Window`的标志也就是Flag，用于控制`Window`的显示，同样被定义在`WindowManager`的内部类`LayoutParams`中，一共有20多个,这里仅展示常用的几个

Flag|描述
:---:|:---:
`FLAG_ALLOW_LOCK_WHILE_SCREEN_ON` |	只要窗口可见，就允许在开启状态的屏幕上锁屏
`FLAG_NOT_FOCUSABLE `|	窗口不能获得输入焦点，设置该标志的同时，`FLAG_NOT_TOUCH_MODAL`也会被设置
`FLAG_NOT_TOUCHABLE` |	窗口不接收任何触摸事件
`FLAG_NOT_TOUCH_MODAL` |	在该窗口区域外的触摸事件传递给其他的Window,而自己只会处理窗口区域内的触摸事件
`FLAG_KEEP_SCREEN_ON` |	只要窗口可见，屏幕就会一直亮着
`FLAG_LAYOUT_NO_LIMITS `|	允许窗口超过屏幕之外
`FLAG_FULLSCREEN `|	隐藏所有的屏幕装饰窗口，比如在游戏、播放器中的全屏显示
`FLAG_SHOW_WHEN_LOCKED `|	窗口可以在锁屏的窗口之上显示
`FLAG_IGNORE_CHEEK_PRESSES` |	当用户的脸贴近屏幕时（比如打电话），不会去响应此事件
`FLAG_TURN_SCREEN_ON` |	窗口显示时将屏幕点亮

## 3.1 设置Window的Flag

设置Window的Flag有三种方法

1. 通过`Window`的`addFlags`方法：

		Window mWindow =getWindow(); 
		mWindow.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

2. 通过`Window`的`setFlags`方法

		Window mWindow =getWindow();            
		mWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
		,WindowManager.LayoutParams.FLAG_FULLSCREEN);

	- **其实`Window`的`addFlags`方法内部会调用`setFlags`方法，因此这两种方法区别不大。**

3. 给LayoutParams设置Flag，并通过WindowManager的addView方法进行添加，如下所示。

		WindowManager.LayoutParams mWindowLayoutParams =
		              new WindowManager.LayoutParams();
		      mWindowLayoutParams.flags=WindowManager.LayoutParams.FLAG_FULLSCREEN;
		      WindowManager mWindowManager =(WindowManager) getSystemService(Context.WINDOW_SERVICE);  
		      TextView mTextView=new TextView(this);
		      mWindowManager.addView(mTextView,mWindowLayoutParams);

# 4. 软键盘相关模式

窗口和窗口的叠加是非常常见的场景，但如果其中的窗口是软键盘窗口，可能就会出现一些问题，比如典型的用户登录界面，默认的情况弹出的软键盘窗口可能会盖住输入框下方的按钮，这样用户体验会非常糟糕。

为了使得软键盘窗口能够按照期望来显示，`WindowManager`的静态内部类`LayoutParams`中定义了软键盘相关模式，这里给出常用的几个：

SoftInputMode	|描述
:---:|:---:
`SOFT_INPUT_STATE_UNSPECIFIED`|	没有指定状态,系统会选择一个合适的状态或依赖于主题的设置
`SOFT_INPUT_STATE_UNCHANGED`|	不会改变软键盘状态
`SOFT_INPUT_STATE_HIDDEN`|	当用户进入该窗口时，软键盘默认隐藏
`SOFT_INPUT_STATE_ALWAYS_HIDDEN`|	当窗口获取焦点时，软键盘总是被隐藏
`SOFT_INPUT_ADJUST_RESIZE`|	当软键盘弹出时，窗口会调整大小
`SOFT_INPUT_ADJUST_PAN`|	当软键盘弹出时，窗口不需要调整大小，要确保输入焦点是可见的

从上面给出的`SoftInputMode`可以发现，它们与`AndroidManifest`中`Activity`的属性`android:windowSoftInputMode`是对应的。

因此，除了在`AndroidMainfest`中为Activity设置`android:windowSoftInputMode`以外还可以在Java代码中为`Window`设置`SoftInputMode`：

	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
