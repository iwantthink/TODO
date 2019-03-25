# WindowManagerService分析2

[Android解析WindowManagerService（二）WMS的重要成员和Window的添加过程](http://liuwangshu.cn/framework/wms/2-wms-member.html)


# 1. WMS的重要成员

	
	final WindowManagerPolicy mPolicy;
	final IActivityManager mActivityManager;
	final ActivityManagerInternal mAmInternal;
	final AppOpsManager mAppOps;
	final DisplaySettings mDisplaySettings;
	...
	final ArraySet<Session> mSessions = new ArraySet<>();
	final WindowHashMap mWindowMap = new WindowHashMap();
	final ArrayList<AppWindowToken> mFinishedStarting = new ArrayList<>();
	final ArrayList<AppWindowToken> mFinishedEarlyAnim = new ArrayList<>();
	final ArrayList<AppWindowToken> mWindowReplacementTimeouts = new ArrayList<>();
	final ArrayList<WindowState> mResizingWindows = new ArrayList<>();
	final ArrayList<WindowState> mPendingRemove = new ArrayList<>();
	WindowState[] mPendingRemoveTmp = new WindowState[20];
	final ArrayList<WindowState> mDestroySurface = new ArrayList<>();
	final ArrayList<WindowState> mDestroyPreservedSurface = new ArrayList<>();
	...
	final H mH = new H();
	...
	final WindowAnimator mAnimator;
	...
	 final InputManagerService mInputManager


- `mPolicy`：`WindowManagerPolicy`

	**`WindowManagerPolicy`是窗口管理策略的接口类，用来定义一个窗口策略所要遵循的通用规范，并提供了WindowManager所有的特定的UI行为**

	它的具体实现类为PhoneWindowManager，这个实现类在WMS创建时被创建。WMP允许定制窗口层级和特殊窗口类型以及关键的调度和布局。

mSessions：ArraySet
ArraySet类型的变量，元素类型为Session。在Android解析WindowManager（三）Window的添加过程这篇文章中我提到过Session，它主要用于进程间通信，其他的应用程序进程想要和WMS进程进行通信就需要经过Session，并且每个应用程序进程都会对应一个Session，WMS保存这些Session用来记录所有向WMS提出窗口管理服务的客户端。
mWindowMap：WindowHashMap
WindowHashMap类型的变量，WindowHashMap继承了HashMap，它限制了HashMap的key值的类型为IBinder，value值的类型为WindowState。WindowState用于保存窗口的信息，在WMS中它用来描述一个窗口。综上得出结论，mWindowMap就是用来保存WMS中各种窗口的集合。

mFinishedStarting：ArrayList
ArrayList类型的变量，元素类型为AppWindowToken，它是WindowToken的子类。要想理解mFinishedStarting的含义，需要先了解WindowToken是什么。WindowToken主要有两个作用：

可以理解为窗口令牌，当应用程序想要向WMS申请新创建一个窗口，则需要向WMS出示有效的WindowToken。AppWindowToken作为WindowToken的子类，主要用来描述应用程序的WindowToken结构，
应用程序中每个Activity都对应一个AppWindowToken。
WindowToken会将相同组件（比如Acitivity）的窗口（WindowState）集合在一起，方便管理。
mFinishedStarting就是用于存储已经完成启动的应用程序窗口（比如Acitivity）的AppWindowToken的列表。
除了mFinishedStarting，还有类似的mFinishedEarlyAnim和mWindowReplacementTimeouts，其中mFinishedEarlyAnim存储了已经完成窗口绘制并且不需要展示任何已保存surface的应用程序窗口的AppWindowToken。mWindowReplacementTimeout存储了等待更换的应用程序窗口的AppWindowToken，如果更换不及时，旧窗口就需要被处理。

mResizingWindows：ArrayList
ArrayList类型的变量，元素类型为WindowState。
mResizingWindows是用来存储正在调整大小的窗口的列表。与mResizingWindows类似的还有mPendingRemove、mDestroySurface和mDestroyPreservedSurface等等。其中mPendingRemove是在内存耗尽时设置的，里面存有需要强制删除的窗口。mDestroySurface里面存有需要被Destroy的Surface。mDestroyPreservedSurface里面存有窗口需要保存的等待销毁的Surface，为什么窗口要保存这些Surface？这是因为当窗口经历Surface变化时，窗口需要一直保持旧Surface，直到新Surface的第一帧绘制完成。

mAnimator：WindowAnimator
WindowAnimator类型的变量，用于管理窗口的动画以及特效动画。

mH：H
H类型的变量，系统的Handler类，用于将任务加入到主线程的消息队列中，这样代码逻辑就会在主线程中执行。

mInputManager：InputManagerService
InputManagerService类型的变量，输入系统的管理者。InputManagerService（IMS）会对触摸事件进行处理，它会寻找一个最合适的窗口来处理触摸反馈信息，WMS是窗口的管理者，因此，WMS“理所应当”的成为了输入系统的中转站，WMS包含了IMS的引用不足为怪。