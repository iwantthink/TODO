#Android提供的工具类
---
Android 提供了一些工具类，在我们creating custom views 时使用，能够比较方便的获得一些数据。

  
参考链接：  
[Android官方文档-custom-views 链接](https://developer.android.com/training/custom-views/index.html "Android官方文档-custom-views")
##1.1 Configuration  
>This class describes all device configuration information that can impact the resources the application retrieves. This includes both user-specified configuration options (locale list and scaling) as well as device configurations (such as input modes, screen size and screen orientation).

>You can acquire this object from Resources, using getConfiguration(). Thus, from an activity, you can get it by chaining the request with getResources():


>`Configuration config = getResources().getConfiguration();`

简而言之：获取设备的一些信息，输入模式，屏幕大小，屏幕方向等等！或者是用户的一些配置信息，locale和scaling等等！


##1.2ViewConfiguration

>Contains methods to standard constants used in the UI for timeouts, sizes, and distances.

>`ViewConfiguration confg = ViewConfiguration.get(context)`