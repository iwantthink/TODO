# WindowManagerService分析2

[Android解析WindowManagerService（二）WMS的重要成员和Window的添加过程](http://liuwangshu.cn/framework/wms/2-wms-member.html)


# 1. WMS中的重要成员

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

- `mPolicy`： `WindowManagerPolicy`接口。

	`WindowManagerPolicy`是窗口管理策略的接口类，用**来定义一个窗口策略所要遵循的通用规范**，并提供了`WindowManager`所有的特定的UI行为。

	**它的具体实现类为`PhoneWindowManager`**，这个实现类在WMS创建时被创建(`SystemServer.startOtherServices()`)。**WMP允许定制窗口层级和特殊窗口类型以及关键的调度和布局**。

- `mSessions`：ArraySet 

	ArraySet类型的变量，元素类型为`Session`。**`Session`主要用于进程间通信**，其他的应用程序进程想要和`WMS`进程进行通信就需要经过`Session`，并且每个应用程序进程都会对应一个Session，WMS保存这些Session用来记录所有向WMS提出窗口管理服务的客户端。 

- `mWindowMap`：WindowHashMap 

	WindowHashMap继承了HashMap，它限制了`HashMap`的key值的类型为`IBinder`，`value`值的类型为`WindowState`。`WindowState`用于保存窗口的信息，在`WMS`中它用来描述一个窗口。**综上得出结论，`mWindowMap`就是用来保存WMS中各种窗口的集合。**

- `mFinishedStarting`：ArrayList 
	
	元素类型为`AppWindowToken`，它是`WindowToken的`子类。要想理解mFinishedStarting的含义，需要先了解`WindowToken`是什么。

	**WindowToken主要有两个作用**：

	1. 可以理解为窗口令牌，当应用程序想要向`WMS`申请新创建一个窗口，则需要向`WMS`出示有效的`WindowToken`。`AppWindowToken`作为`WindowToken`的子类，主要用来描述应用程序的`WindowToken`结构， 

	应用程序中每个Activity都对应一个AppWindowToken。

	2. `WindowToken`会将相同组件（比如Acitivity）的窗口（`WindowState`）集合在一起，方便管理。

- `mFinishedStarting`就是用于存储已经完成启动的应用程序窗口（比如Acitivity）的AppWindowToken的列表。 
	
	除了mFinishedStarting，还有类似的mFinishedEarlyAnim和mWindowReplacementTimeouts，其中mFinishedEarlyAnim存储了已经完成窗口绘制并且不需要展示任何已保存surface的应用程序窗口的AppWindowToken。mWindowReplacementTimeout存储了等待更换的应用程序窗口的AppWindowToken，如果更换不及时，旧窗口就需要被处理。

- `mResizingWindows`：ArrayList 

	元素类型为`WindowState`。 `WindowState`用于保存窗口的信息，在`WMS`中它用来描述一个窗口。

- `mResizingWindows`是用来存储正在调整大小的窗口的列表。与

	`mResizingWindows`类似的还有`mPendingRemove`、`mDestroySurface`和`mDestroyPreservedSurface`等等。
	
	其中`mPendingRemove`是在内存耗尽时设置的，里面存有需要强制删除的窗口。

	`mDestroySurface`里面存有需要被`Destroy`的`Surface`。

	`mDestroyPreservedSurface`里面存有窗口需要保存的等待销毁的`Surface`

	**为什么窗口要保存这些Surface？这是因为当窗口经历Surface变化时，窗口需要一直保持旧Surface，直到新Surface的第一帧绘制完成。**

- `mAnimator`：WindowAnimator 

	用于管理窗口的动画以及特效动画。

- `mH`：H 

	系统的Handler类，用于将任务加入到主线程的消息队列中，这样代码逻辑就会在主线程中执行。

- `mInputManager`：InputManagerService 

	输入系统的管理者。`InputManagerService`（IMS）会对触摸事件进行处理，它会寻找一个最合适的窗口来处理触摸反馈信息，`WMS`是窗口的管理者，因此，`WMS`“理所应当”的成为了输入系统的中转站，WMS包含了IMS的引用不足为怪。


# 2. Window的添加过程

之前[Window的相关操作.md]()中已经分析过`Window`在应用进程中的创建过程,并与 2.3.1-2.3.2 小节得知,在视图的创建过程中会通过IPC调用WMS的`addWindow`方法

## 2.1 addWindow-1
	
	 public int addWindow(Session session, IWindow client, int seq,
	            WindowManager.LayoutParams attrs, int viewVisibility, int displayId,
	            Rect outContentInsets, Rect outStableInsets, Rect outOutsets,
	            InputChannel outInputChannel) {
	
	        int[] appOp = new int[1];
			//检查权限,如果没有权限会结束方法
	        int res = mPolicy.checkAddPermission(attrs, appOp);//1
	        if (res != WindowManagerGlobal.ADD_OKAY) {
	            return res;
	        }
	        ...
	        synchronized(mWindowMap) {
	            if (!mDisplayReady) {
	                throw new IllegalStateException("Display has not been initialialized");
	            }
				// 获取窗口要添加到的DisplayContent
	            final DisplayContent displayContent = mRoot.getDisplayContentOrCreate(displayId);//2
	            if (displayContent == null) {
	                Slog.w(TAG_WM, "Attempted to add window to a display that does not exist: "
	                        + displayId + ".  Aborting.");
	                return WindowManagerGlobal.ADD_INVALID_DISPLAY;
	            }
	            ...
				// 如果被添加的窗口是一个子窗口,就要求父窗口必须存在!
	            if (type >= FIRST_SUB_WINDOW && type <= LAST_SUB_WINDOW) {//3
					// 取出父窗口
	                parentWindow = windowForClientLocked(null, attrs.token, false);//4
	                if (parentWindow == null) {
	                    Slog.w(TAG_WM, "Attempted to add window with token that is not a window: "
	                          + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN;
	                }
					// 父窗口不能是别的窗口的子窗口!!!
	                if (parentWindow.mAttrs.type >= FIRST_SUB_WINDOW
	                        && parentWindow.mAttrs.type <= LAST_SUB_WINDOW) {// 5 
	                    Slog.w(TAG_WM, "Attempted to add window with token that is a sub-window: "
	                            + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN;
	                }
	            }
	           ...
	}
	...
	}

`WMS`的`addWindow`返回的是`addWindow`的各种状态，比如添加`Window`成功，无效的`display`等等，这些状态被定义在`WindowManagerGlobal`中。
 
- 注释1处根据`Window`的属性，调用`WMP`的`checkAddPermission`方法来检查权限，具体的实现在`PhoneWindowManager`的`checkAddPermission`方法中，如果没有权限则不会执行后续的代码逻辑。

- 注释2处通过`displayId`来获得窗口要添加到哪个`DisplayContent`上，如果没有找到`DisplayContent`，则返回`WindowManagerGlobal.ADD_INVALID_DISPLAY`这一状态，其中`DisplayContent`用来描述一块屏幕。

- 注释3处，`type`代表一个窗口的类型，它的数值介于`FIRST_SUB_WINDOW`和`LAST_SUB_WINDOW`之间（1000~1999），这个数值定义在`WindowManager`中，说明这个窗口是一个子窗口。

- 注释4处，`attrs.token`表示窗口所隶属的对象,是`IBinder`类型的对象.

	方法`windowForClientLocked()`内部会根据`attrs.token`作为key值从`mWindowMap`中得到该子窗口的父窗口。接着对父窗口进行判断，如果父窗口为null或者type的取值范围不正确则会返回错误的状态。

- 注释5处: WMS要求窗口的层级关系最多为俩层!!!


## 2.2 addWindow-2

	 ...
	            AppWindowToken atoken = null;
	            final boolean hasParent = parentWindow != null;
				// 取出WindowToken
	            WindowToken token = displayContent.getWindowToken(
	                    hasParent ? parentWindow.mAttrs.token : attrs.token);//1
				// 如果存在父窗口,就赋值为父窗口的type,否则就是当前窗口
	            final int rootType = hasParent ? parentWindow.mAttrs.type : type;//2
	            boolean addToastWindowRequiresToken = false;
				// WindowToken为空的情况下
	            if (token == null) {
	                if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
	                    Slog.w(TAG_WM, "Attempted to add application window with unknown token "
	                          + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                }
	                if (rootType == TYPE_INPUT_METHOD) {
	                    Slog.w(TAG_WM, "Attempted to add input method window with unknown token "
	                          + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                }
	                if (rootType == TYPE_VOICE_INTERACTION) {
	                    Slog.w(TAG_WM, "Attempted to add voice interaction window with unknown token "
	                          + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                }
	                if (rootType == TYPE_WALLPAPER) {
	                    Slog.w(TAG_WM, "Attempted to add wallpaper window with unknown token "
	                          + attrs.token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                }
	                ...
	                if (type == TYPE_TOAST) {
	                    // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
	                    if (doesAddToastWindowRequireToken(attrs.packageName, callingUid,
	                            parentWindow)) {
	                        Slog.w(TAG_WM, "Attempted to add a toast window with unknown token "
	                                + attrs.token + ".  Aborting.");
	                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                    }
	                }
	                final IBinder binder = attrs.token != null ? attrs.token : client.asBinder();
					// 隐式创建WindowToken
	                token = new WindowToken(this, binder, type, false, displayContent,
	                        session.mCanAddInternalSystemWindow);//3
	
				// WindowToken 非空的情况下
				// 判断窗口类型是否是应用窗口
	            } else if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {//4
	                atoken = token.asAppWindowToken();//5
	                if (atoken == null) {
	                    Slog.w(TAG_WM, "Attempted to add window with non-application token "
	                          + token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_NOT_APP_TOKEN;
	                } else if (atoken.removed) {
	                    Slog.w(TAG_WM, "Attempted to add window with exiting application token "
	                          + token + ".  Aborting.");
	                    return WindowManagerGlobal.ADD_APP_EXITING;
	                }
	            } else if (rootType == TYPE_INPUT_METHOD) {
	                if (token.windowType != TYPE_INPUT_METHOD) {
	                    Slog.w(TAG_WM, "Attempted to add input method window with bad token "
	                            + attrs.token + ".  Aborting.");
	                      return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
	                }
	            }
	      ...      


- 注释1处通过`displayContent`的`getWindowToken`方法来得到`WindowToken`。

- 注释2处，如果有父窗口就将父窗口的`type`值赋值给`rootType`，如果没有将当前窗口的`type`值赋值给`rootType`。

	接下来如果`WindowToken`为null，则根据`rootType`或者`type`的值进行区分判断，如果`rootType`值等于`TYPE_INPUT_METHOD`、`TYPE_WALLPAPER`等值时，则返回状态值`WindowManagerGlobal.ADD_BAD_APP_TOKEN`，说明`rootType`值等于`TYPE_INPUT_METHOD、TYPE_WALLPAPER`等值时是不允许`WindowToken`为null的。

	通过多次的条件判断筛选，最后会在注释3处隐式创建`WindowToken`，**这说明当我们添加窗口时是可以不向WMS提供`WindowToken`的**
	
	- 前提是`rootType`或`type`的值不为前面条件判断筛选的值(`TYPE_TOAST`,`TYPE_WALLPAPER`等等)。`WindowToken`隐式和显式的创建肯定是要加以区分的


- 注释3处的第4个参数为false就代表这个`WindowToken`是隐式创建的。接下来的代码逻辑就是`WindowToken`不为null的情况，根据`rootType`和type的值进行判断，比如在注释4处判断如果窗口为应用程序窗口，

- 在注释5处会将`WindowToken`转换为专门针对应用程序窗口的`AppWindowToken`，然后根据`AppWindowToken`的值进行后续的判断。

## 2.3 addWindow-3

	   ...
	  // WMS为待添加的窗口创建了一个WindowState独享
	  // 该对象维护了一个窗口的所有状态信息
	  final WindowState win = new WindowState(this, session, client, token, parentWindow,
	                    appOp[0], seq, attrs, viewVisibility, session.mUid,
	                    session.mCanAddInternalSystemWindow);//1

				// 对窗口存活状态进行判断
	            if (win.mDeathRecipient == null) {//2
	                // Client has apparently died, so there is no reason to
	                // continue.
	                Slog.w(TAG_WM, "Adding window client " + client.asBinder()
	                        + " that is dead, aborting.");
	                return WindowManagerGlobal.ADD_APP_EXITING;
	            }
				
	            if (win.getDisplayContent() == null) {//3
	                Slog.w(TAG_WM, "Adding window to Display that has been removed.");
	                return WindowManagerGlobal.ADD_INVALID_DISPLAY;
	            }
	
	            mPolicy.adjustWindowParamsLw(win.mAttrs);//4
	            win.setShowToOwnerOnlyLocked(mPolicy.checkShowToOwnerOnly(attrs));
	            res = mPolicy.prepareAddWindowLw(win, attrs);//5
	            ...
	            win.attach();
	            mWindowMap.put(client.asBinder(), win);//6
	            if (win.mAppOp != AppOpsManager.OP_NONE) {
	                int startOpResult = mAppOps.startOpNoThrow(win.mAppOp, win.getOwningUid(),
	                        win.getOwningPackage());
	                if ((startOpResult != AppOpsManager.MODE_ALLOWED) &&
	                        (startOpResult != AppOpsManager.MODE_DEFAULT)) {
	                    win.setAppOpVisibilityLw(false);
	                }
	            }
	
	            final AppWindowToken aToken = token.asAppWindowToken();
	            if (type == TYPE_APPLICATION_STARTING && aToken != null) {
	                aToken.startingWindow = win;
	                if (DEBUG_STARTING_WINDOW) Slog.v (TAG_WM, "addWindow: " + aToken
	                        + " startingWindow=" + win);
	            }
	
	            boolean imMayMove = true;
	            win.mToken.addWindow(win);//7
	             if (type == TYPE_INPUT_METHOD) {
	                win.mGivenInsetsPending = true;
	                setInputMethodWindowLocked(win);
	                imMayMove = false;
	            } else if (type == TYPE_INPUT_METHOD_DIALOG) {
	                displayContent.computeImeTarget(true /* updateImeTarget */);
	                imMayMove = false;
	            } else {
	                if (type == TYPE_WALLPAPER) {
	                    displayContent.mWallpaperController.clearLastWallpaperTimeoutTime();
	                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
	                } else if ((attrs.flags&FLAG_SHOW_WALLPAPER) != 0) {
	                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
	                } else if (displayContent.mWallpaperController.isBelowWallpaperTarget(win)) {
	                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
	                }
	            }
	         ...

- 在注释1处创建了`WindowState`，**它存有窗口的所有的状态信息**，在WMS中它代表一个窗口。从`WindowState`传入的参数，可以发现`WindowState`中包含了`WMS、Session、WindowToken、父类的WindowState、LayoutParams`等信息。

- 紧接着在注释2和3处分别判断请求添加窗口的客户端是否已经死亡、窗口的`DisplayContent`是否为null，如果是则不会再执行下面的代码逻辑。

- 注释4处调用了WMP的`adjustWindowParamsLw`方法，该方法的实现在`PhoneWindowManager`中，会根据窗口的type对窗口的LayoutParams的一些成员变量进行修改。

- 注释5处调用WMP的`prepareAddWindowLw`方法，用于准备将窗口添加到系统中。 

- 注释6处将`WindowState`添加到`mWindowMap`中。

- 注释7处将`WindowState`添加到该`WindowState`对应的`WindowToken`中(实际是保存在`WindowToken`的父类`WindowContainer`中)，这样`WindowToken`就包含了相同组件的`WindowState`。

## 2.4 addWindow总结

addWindow方法分了3个部分来进行讲解，主要就是做了下面4件事： 

1. 对所要添加的窗口进行检查，如果窗口不满足一些条件，就不会再执行下面的代码逻辑。 

2. `WindowToken`相关的处理，比如有的窗口类型需要提供WindowToken，没有提供的话就不会执行下面的代码逻辑，有的窗口类型则需要由WMS隐式创建`WindowToken`。 

3. `WindowState`的创建和相关处理，将`WindowToken`和`WindowState`相关联。 

4. 创建和配置`DisplayContent`，完成窗口添加到系统前的准备工作。
