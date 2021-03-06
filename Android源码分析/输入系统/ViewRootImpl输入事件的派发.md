# ViewRootImpl中输入事件的派发

[深入理解Android 卷iii PDF版]()

# 1. 简介

控件树中的输入事件派发是由`ViewRootImpl`为起点,沿着控件树逐层传递给目标控件,最终再回到`ViewRootImpl`的一个环形过程

事件派发的过程发生在创建`ViewRootImpl`的主线程中,但却独立于`ViewRootImpl.performTraversals()`之外,即输入事件的派发并不依赖于`ViewRootImpl`的`心跳`作为动力.

-  **它的动力来源于`InputEventReceiver`的`Looper`,当一个输入事件被派发给`ViewRootImpl`所在的窗口时,`Looper`会被唤醒并触发`InputEventReceiver.onInputEvent()`回调,控件树的输入事件派发起始于这一回调.** 


# 2. 触摸模式和焦点

Android同时支持**按键与触摸**俩种操作方式,并且可以在俩者之间自由切换

可以获取焦点的控件分为俩类:

1. 任何情况下都可以获取焦点的控件 ,如文本输入框

2. 仅在键盘操作时可以获取焦点的控件,如菜单项,按钮等

## 2.1 触摸模式介绍

**触摸模式(TouchMode)**是为了管理俩者的差异而引入的概念,Android通过进入或退出触摸模式实现二者无缝切换 . 

- 在非触摸模式下:

	文本框,按钮,菜单项等都可以获取焦点,并且可以通过方向键使得焦点在这些控件之间切换

- 在触摸模式下:

	某些控件如菜单项,按钮将不再可以保持或获取焦点,而文本框则仍然可以保持或获取焦点


**触摸模式**是一个系统级的概念,就是说会对所有窗口产生影响.系统是否处于触摸模式取决WMS中的一个成员变量`mInTouchMode`

    /**
     * Whether the UI is currently running in touch mode (not showing
     * navigational focus because the user is directly pressing the screen).
     */
    boolean mInTouchMode;

- 退出触摸模式的操作有  

	1. 按下方向键
	2. 通过键盘按下字母键
	3. 执行了`View.requestFocusFromTouch()`

- 进入触摸模式的操作:

	1. 用户在窗口上进行了点击操作 . 窗口的`ViewRootImpl`会识别上述操作,然后通过WMS的接口`setInTouchMode()`设置`mInTouchMode`的值

	**只有拥有`ViewRootImpl`的窗口才能影响触摸模式,或对触摸模式产生响应**

系统进入或退出触摸模式对控件系统的最主要的影响是 其对焦点的选择策略



# 3 控件焦点

控件的焦点影响按键事件的派发,还影响了控件的表现形式(拥有焦点的控件往往会高亮显示以区分)

## 3.1 获取焦点的条件

控件获取焦点的方式很多,例如从控件树中按照一定策略查找到某个控件并使其获得焦点,或用户通过方向键选择某个控件使其获得焦点 .**而最基本的方式是通过`View.requestFocus()`**


`View.requestFocus()`的实现有俩种,即`View`和`ViewGroup`是不同的

- 当调用的控件为View时,表示期望此View能够获取焦点

- 当调用的控件为ViewGroup时,则会根据一定的焦点选择策略选择一个子控件或者ViewGroup本身作为焦点持有者

### 3.1.1 初始焦点获取

**当控件树被添加到`ViewRootImpl`之后,会调用`ViewRootImpl.requestFocus()`设置初始的焦点**

## 3.2 View的requestFocus()

    public final boolean requestFocus() {
        return requestFocus(View.FOCUS_DOWN);
    }

- `View.FOCUS_DOWN`表示焦点的寻找方向. 当**本控件是一个`ViewGroup`时将会从自身的`View[] mChildren`成员变量中按照顺序去查找**,由于当前分析的是控件为`View`的情况,所以该参数无效

	    public final boolean requestFocus(int direction) {
	        return requestFocus(direction, null);
	    }

- `Rect`参数表示上一个焦点控件的区域 . 表示从哪个位置开始沿着`direction`所指定的方向查找焦点控件.仅当控件是`ViewGroup`时有意义

	    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
	        return requestFocusNoSearch(direction, previouslyFocusedRect);
	    }

- **这个俩个参数的重载方法便是`View`和`ViewGroup`对焦点控件查找的分界点**

	在`View`类型的控件中,直接调用了`View.requestFocusNoSearch()`,代表的含义就是无需查找,直接使本控件获取焦点

	    private boolean requestFocusNoSearch(int direction, Rect previouslyFocusedRect) {
	        // 首先必须是Focusable,另外不可见的控件也不能获取焦点
			// 可以通过View.setFocusable()进行设置
	        if ((mViewFlags & FOCUSABLE) != FOCUSABLE
	                || (mViewFlags & VISIBILITY_MASK) != VISIBLE) {
	            return false;
	        }
	
	        // 如果系统目前处于触摸模式,则要求此控件必须可以在触摸模式下可以拥有焦点
	        if (isInTouchMode() &&
	            (FOCUSABLE_IN_TOUCH_MODE != (mViewFlags & FOCUSABLE_IN_TOUCH_MODE))) {
	               return false;
	        }
	
	        // need to not have any parents blocking us
			// 判断是否存在任意父控件的DescendantFocusability取值为FOCUS_BLOCK_DESCENDANTS,意义为父类阻止此控件获取焦点
	        if (hasAncestorThatBlocksDescendantFocus()) {
	            return false;
	        }
	
			//通过此方法使得控件获取焦点
	        handleFocusGainInternal(direction, previouslyFocusedRect);
	        return true;
	    }

**控件能否获取焦点有一下俩个要求:**

1. 并不是所有的控件都能够获取焦点

	控件系统通过`View.setFocusable()`设置控件能否获取焦点. 该方法会将`NOT_FOCUSABLE`或`FOCUSABLE`标记加入`View.mViewFlags`成员中

	**但是当控件持有`FOCUSABLE`标记时,也不一定能够获取焦点**. 控件系统通过`View.setFocusableInTouchMode()`区分这类控件,该方法会将`FOCUSABLE_IN_TOUCH_MODE`标记加入`View.mViewFlags`中

	**当控件处于触摸模式时,仅当拥有`FOCUSABLE_IN_TOUCH_MODE`标记的控件才能获取焦点**

2. 控件能否获取焦点还取决于其父控件的特性`DescendantFocusability`,这一特性描述了子控件与父控件之间的焦点获取策略
 
	`DescendantFocusability`存在三种取值,例如在`FOCUS_BLOCK_DESCENDANTS`时 父控件会阻止子控件获取焦点


**因此控件是否能够获取焦点的策略如下:**

1. 当`View.mViewFlags`存在`NOT_FOCUSABLE`标记时,无法获取焦点

2. 当其父控件的`DescendantFocusability`取值为`FOCUS_BLOCK_DESCENDANTS`时,无法获取焦点

3. 当`View.mViewFlags`存在`FOCUSABLE`标记时,有俩种情况

	- 位于非触摸模式,控件可以获取焦点
	- 位于触摸模式,控件的`View.mViewFlags`必须持有`FOCUSABLE_IN_TOUCH_MODE`标记才可以获取焦点


### 3.2.1 获取焦点!

`View.handleFocusGainInternal()`

    void handleFocusGainInternal(@FocusRealDirection int direction, Rect previouslyFocusedRect) {

		//判断是否已经拥有焦点
        if ((mPrivateFlags & PFLAG_FOCUSED) == 0) {
			//将PFLAG_FOCUSED加入mPrivateFlags中
			// 这便表示此控件已经拥有焦点!
            mPrivateFlags |= PFLAG_FOCUSED;

            View oldFocus = (mAttachInfo != null) ? getRootView().findFocus() : null;

			//将焦点变换通知其父控件
			// 这一操作主要是为了保证控件树中仅有一个控件拥有焦点!!!
			// 并且在ViewRootImpl中触发一次遍历从而进行重绘
            if (mParent != null) {
                mParent.requestChildFocus(this, this);
                updateFocusedInCluster(oldFocus, direction);
            }

			
            if (mAttachInfo != null) {
                mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(oldFocus, this);
            }
			//通知对此控件焦点变换感兴趣的监听者
            onFocusChanged(true, direction, previouslyFocusedRect);
			//更新控件的Drawable状态.使得控件在绘制中出现高亮显示
            refreshDrawableState();
        }
    }

- **`PFLAG_FOCUSED`是一个控件是否拥有焦点的最直接体现**,但这一标记仅体现了焦点在个体级别上的特性,而`mParent.requestChildFocus()`则体现了焦点在控件树级别的特性

### 3.2.2 控件树中的焦点体系

`mParent.requestChildFocus()`方法是定义在`ViewParent`接口中的方法,其实现者为`ViewGroup`和`ViewRootImpl`.

- **`ViewGroup`实现的目的之一就是将焦点从上一个焦点控件手中夺走**, 即将`PFLAG_FOCUSED`标记从控件的`mPrivateFlags`中移除

- 另外一个目的是 将这一操作继续向控件树的根部进行回溯,直到`ViewRootImpl`,`ViewRootImpl`的`requestChildFocus()`方法会将焦点控件保存起来备用,并引发一次遍历

`[ViewGroup.requestChildFocus()]`

    @Override
    public void requestChildFocus(View child, View focused) {
		// 父类阻止子类获取焦点
        if (getDescendantFocusability() == FOCUS_BLOCK_DESCENDANTS) {
            return;
        }

        // 如果上一个焦点控件就是这个ViewGroup,则通过`View.unFocus()`将`PFLAG_FOCUSED`标记移除,以释放焦点
        super.unFocus(focused);

		// mFocused代表目前拥有焦点控件的父控件(child代表将要拥有焦点的控件,俩者肯定在不同时才需要进行焦点获取)
		// child 在View调用requestChildFocus()时 为 View本身
		//  	 在ViewGroup调用requestChildFocus()时为 ViewGroup本身
		// 第一次调用时 mFocused 为空,肯定不等于View,那么mFocused直接被赋值为child
        if (mFocused != child) {
	        // 如果上一个焦点控件 是这个控件树中的子控件,即mFocused 不为空
			// 则调用 该子控件的unFocus()
            if (mFocused != null) {
                mFocused.unFocus(focused);
            }
			//新焦点体系的建立
			// 设置mFocused为child
			// child参数有时并不是实际拥有焦点的控件,而是实际拥有焦点的控件的父控件
            mFocused = child;
        }
			
		// 将这一操作继续向控件树的根部回溯
		// 此时 child参数是 此ViewGroup,而不是实际拥有焦点的focused
        if (mParent != null) {
            mParent.requestChildFocus(this, focused);
        }
    }

- **`ViewGroup.requestChildFocus()`方法包含了新的焦点体系的建立过程,以及旧的焦点体系的销毁过程**

	新的焦点体系建立通过`ViewGroup.requestChildFocus()`方法的回溯过程中进行`mFocused=child`完成,这实际上是建立了一个单向链表,通过`mFocused`可以从根控件开始沿着这一单向链表找到实际拥有焦点的控件

	旧的焦点体系摧毁是通过在回溯过程调用`mFocused.unFocus`完成

- `ViewGroup.unFocus()`

		@Override
	    void unFocus(View focused) {
			// 如果mFocused为空,则表示此ViewGroup位于mFocused单向链表的尾端
			// 即此ViewGroup是焦点的实际拥有者,因此调用View.unFocus()使此ViewGroup放弃焦点
	        if (mFocused == null) {
				//调用View.unFocus()
	            super.unFocus(focused);
	        } else {
				//传递给链表的下一个控件
	            mFocused.unFocus(focused);
				//同时将当前的mFocused 置空
	            mFocused = null;
	        }
	    }

- `View.unFocus()`方法会调用`clearFocusInternal()`方法

	    void clearFocusInternal(View focused, boolean propagate, boolean refocus) {
	        if ((mPrivateFlags & PFLAG_FOCUSED) != 0) {
	            mPrivateFlags &= ~PFLAG_FOCUSED;
	
	            if (propagate && mParent != null) {
	                mParent.clearChildFocus(this);
	            }
	
	            onFocusChanged(false, 0, null);
	            refreshDrawableState();
	
	            if (propagate && (!refocus || !rootViewRequestFocus())) {
	                notifyGlobalFocusCleared(this);
	            }
	        }
	    }

### 3.2.3 View类型控件获取焦点流程(View.requestChildFocus())

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fx7ga3a05oj20n60bhwh3.jpg)

1. 当`View2-1-1`通过`View.requestFocus()`尝试获取焦点时,首先会将`PFLAG_FOCUSED`标记加入其成员`mPrivateFlags`中,以声明其拥有焦点

2. 然后调用`ViewGrou2-1`的`requestChildFocus()`,此时`2-1`会通过`unFocus()`销毁旧的焦点体系,但是由于其`mFocused`为null,它无法进行销毁旧的焦点体系,之后它将`mFocused`设置为`View2-1-1`.最后继续回溯,去调用`ViewGroup2`的`requestChildFocus()`

3. `ViewGroup2`的`mFocused`指向了`View2-2`,于是调用了`ViewGroup2-2`的`unFocus()`进行旧的焦点体系的销毁. 

4. `ViewGroup2-2`的`unFocus()`将此操作传递给`View2-2-2`的`unFocus()`以移除`View2-2-2`的`PFLAG_FOCUSED`标记,并将自身的`mFocused`置空

5. `ViewGroup2-2`销毁了旧的焦点体系之后,回到`ViewGroup2`将`mFocused`置为`View2-1`


- **`View`类有俩个查询控件焦点状态的方法**:

	1. `isFocused()`:是否拥有`PFLAG_FOCUSED`,控件直接拥有焦点

	2. `hasFocuse()`:是否拥有`PFLAG_FOCUSED`或者`mFocused`不为空,即理解为 焦点被自身拥有或者被其子类拥有


## 3.3 ViewGroup的requestFocus()

已知`ViewGroup`会重写`View.requestFocus(int,Rect)`,拥有其自己的获取焦点的逻辑

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
	
		// 获取ViewGroup的 DescendantFocusability 特性的取值
        int descendantFocusability = getDescendantFocusability();

        switch (descendantFocusability) {
            case FOCUS_BLOCK_DESCENDANTS:
				// ViewGroup 将会阻止所有子控件获取焦点,同时调用View.requestFocus()尝试自己获取焦点
                return super.requestFocus(direction, previouslyFocusedRect);
            case FOCUS_BEFORE_DESCENDANTS: {
				//ViewGroup 将有优先于子控件获取焦点的权利
				// 首先调用View.requestFocus()尝试自己获取焦点,如果自己不满足获取焦点的条件,那么会调用onRequestFocusInDescendants()将获取焦点的请求转发给子控件
                final boolean took = super.requestFocus(direction, previouslyFocusedRect);
                return took ? took : onRequestFocusInDescendants(direction, previouslyFocusedRect);
            }
            case FOCUS_AFTER_DESCENDANTS: {
				// 子控件有优于ViewGroup获取焦点的权利
				// 首先调用onRequestFocusInDescendants()尝试让子控件获取焦点.倘若子控件无法获取焦点,则ViewGroup再尝试自己获取焦点
                final boolean took = onRequestFocusInDescendants(direction, previouslyFocusedRect);
                return took ? took : super.requestFocus(direction, previouslyFocusedRect);
            }
            default:
                throw new IllegalStateException(../...);
        }
    }

- `ViewGroup.requestFocus()`方法是会根据`DescendantFocusability`特性来决定焦点的获取逻辑

	开发者可以通过`ViewGroup.setDescendantFocusability()`方法修改该值

### 3.3.1 ViewGroup.onRequestFocusInDescendants()

    protected boolean onRequestFocusInDescendants(int direction,
            Rect previouslyFocusedRect) {
        int index;
        int increment;
        int end;
        int count = mChildrenCount;
		// 判断方向,决定初始值
        if ((direction & FOCUS_FORWARD) != 0) {
            index = 0;
            increment = 1;
            end = count;
        } else {
            index = count - 1;
            increment = -1;
            end = -1;
        }
		
        final View[] children = mChildren;
        for (int i = index; i != end; i += increment) {
            View child = children[i];
			//子控件必须是可见的
            if ((child.mViewFlags & VISIBILITY_MASK) == VISIBLE) {
				// 调用子控件去获取焦点,如果子控件可以获取到焦点,那么直接停止查找
                if (child.requestFocus(direction, previouslyFocusedRect)) {
                    return true;
                }
            }
        }
        return false;
    }

- 此方法的目的是按照`direction`参数所描述的方向,在子控件列表中依次尝试使其获取焦点.

	**这里的`direction`并不是指代控件在屏幕上的位置,而是它们在`mChildren`列表中的位置,因此`direction`仅有按照索引递增或递减俩种方向**


### 3.4 下一个焦点控件的查找

主要是对于 键盘操作,即非触摸模式情况下的分析


# 4. 输入事件派发的综述

**已知Android输入系统派发的终点是`InputEventReceiver`**(根据深入理解Android第五章得出),那么作为控件系统最高级别的管理者`ViewRootImpl`,其便是`InputEventReceiver`的一个用户,它从`InputEventReceiver`中获取事件,然后将它们按照一定流程派发给所有感兴趣的对象,包括`View,PhoneWindow,Activity,Dialog`等,因此从`InputEventReceiver.onInputEvent()`开始讨论


在`ViewRootImpl.setView()`方法中,会利用WMS分配的`InputChannel`以及当前线程的`Looper`创建`InputEventReceiver`的子类`WindowInputEventReceiver`的一个实例,并保存在`ViewRootImpl.mInputEventReceiver`中.

- **这标志着从设备驱动到本窗口的输入事件通道的正式建立,每当有输入事件到来,`ViewRootImpl`都可以通过`WindowInputEventReceiver.onInputEvent()`回调获得该事件并对其进行处理**

## 4.1 ViewRootImpl.setView()


    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
        synchronized (this) {
            if (mView == null) {

				..............省略不相关代码...............
				//创建过程
                if (mInputChannel != null) {
                    if (mInputQueueCallback != null) {
                        mInputQueue = new InputQueue();
                        mInputQueueCallback.onInputQueueCreated(mInputQueue);
                    }
                    mInputEventReceiver = new WindowInputEventReceiver(mInputChannel,
                            Looper.myLooper());
                }
				............省略代码..............

                // 设置输入管道
                CharSequence counterSuffix = attrs.getTitle();
                mSyntheticInputStage = new SyntheticInputStage();
                InputStage viewPostImeStage = new ViewPostImeInputStage(mSyntheticInputStage);
                InputStage nativePostImeStage = new NativePostImeInputStage(viewPostImeStage,
                        "aq:native-post-ime:" + counterSuffix);
                InputStage earlyPostImeStage = new EarlyPostImeInputStage(nativePostImeStage);
                InputStage imeStage = new ImeInputStage(earlyPostImeStage,
                        "aq:ime:" + counterSuffix);
                InputStage viewPreImeStage = new ViewPreImeInputStage(imeStage);
                InputStage nativePreImeStage = new NativePreImeInputStage(viewPreImeStage,
                        "aq:native-pre-ime:" + counterSuffix);
				//NativePreImeInputStage
                mFirstInputStage = nativePreImeStage;
				//EarlyPostImeInputStage
                mFirstPostImeInputStage = earlyPostImeStage;
                mPendingInputEventQueueLengthCounterName = "aq:pending:" + counterSuffix;
            }
        }
    }

- `InputStage`的构造函数中,会传入另外一个`InputStage`,这个Stage会被保存在成员变量`mNext`中,作为事件转发时的下一个目标来使用

各个`InputStage`的作用

1. `NativePreImeInputStage`:

	Delivers pre-ime input events to a native activity.
	Does not support pointer events.

2. `ViewPreImeInputStage`:

	 Delivers pre-ime input events to the view hierarchy.
	 Does not support pointer events.

3. `ImeInputStage`:

	 Delivers input events to the ime.
	 Does not support pointer events.

4. `EarlyPostImeInputStage`:

	Performs early processing of post-ime input events.

5. `NativePostImeInputStage`:

	Delivers post-ime input events to a native activity.

6. `ViewPostImeInputStage`:

	Delivers post-ime input events to the view hierarchy.

7. `SyntheticInputStage` :

	Performs synthesis of new input events from unhandled input events.


## 4.1.1 ViewRootImpl.WindowInputEventReceiver 类

    final class WindowInputEventReceiver extends InputEventReceiver {
        public WindowInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        @Override
        public void onInputEvent(InputEvent event, int displayId) {
			// 将输入事件入队
			// 注意第四个参数为true
            enqueueInputEvent(event, this, 0, true);
        }

        @Override
        public void onBatchedInputEventPending() {
            if (mUnbufferedInputDispatch) {
                super.onBatchedInputEventPending();
            } else {
                scheduleConsumeBatchedInput();
            }
        }

        @Override
        public void dispose() {
            unscheduleConsumeBatchedInput();
            super.dispose();
        }
    }

### 4.1.2 ViewRootImpl.enqueueInputEvent()

    void enqueueInputEvent(InputEvent event,
            InputEventReceiver receiver, int flags, boolean processImmediately) {
        adjustInputEventForCompatibility(event);

		//将InputEvent 和对应的InputEventReceiver 封装为一个QueuedInputEvent
        QueuedInputEvent q = obtainQueuedInputEvent(event, receiver, flags);

     	//将新建的QueuedInputEvent 追加到mPendingInputEventTail所表示的单向链表尾部
		// ViewRootImpl 将会沿着链表从头到尾的逐个处理输入事件
        QueuedInputEvent last = mPendingInputEventTail;
		//待处理事件尾部为空,说明这是第一次处理这个事件
        if (last == null) {
			//待处理事件的头部 赋值
            mPendingInputEventHead = q;
			//待处理事件的尾部 赋值
            mPendingInputEventTail = q;
        } else {
			// 建立链表关系
            last.mNext = q;
			//更新待处理事件尾部
            mPendingInputEventTail = q;
        }
		//待处理事件数量+1
        mPendingInputEventCount += 1;

        if (processImmediately) {
			//直接在当前线程中对输入事件进行处理
            doProcessInputEvents();
        } else {
			//转到主线程中进行处理
            scheduleProcessInputEvents();
        }
    }

- `QueueInputEvent` 是输入事件在`ViewRootImpl`中的存在形式

- 对于正常事件来说`processImmediately`通常为true

#### 4.1.2.1 ViewRootImpl.obtainQueuedInputEvent()

    private QueuedInputEvent obtainQueuedInputEvent(InputEvent event,
            InputEventReceiver receiver, int flags) {
		// 复用对象
        QueuedInputEvent q = mQueuedInputEventPool;
        if (q != null) {
			// 输入事件池中的数量
            mQueuedInputEventPoolSize -= 1;
			// 置为链表中的下一个输入事件
            mQueuedInputEventPool = q.mNext;
            q.mNext = null;
        } else {
            q = new QueuedInputEvent();
        }

        q.mEvent = event;
        q.mReceiver = receiver;
        q.mFlags = flags;
        return q;
    }

- 将`InputEvent` 和对应的`InputEventReceiver` 封装为一个`QueuedInputEvent`.如果存在可复用的对象就会复用

### 4.1.3 ViewRootImpl.doProcessInputEvents()

    void doProcessInputEvents() {
        // 处理输入事件队列中的事件
        while (mPendingInputEventHead != null) {
			........省略代码............
			
			QueuedInputEvent q = mPendingInputEventHead;
			mPendingInputEventHead = q.mNext;
			//该方法会完成单个事件的整个处理流程
			deliverInputEvent(q);
        }

		........省略代码............
    }

- 该方法中,会将所有的输入事件都处理完毕再退出,换言之就是在处理完所有输入事件之前 会一直占用主线程

	这种行为主要是为了节省 因为输入事件而导致的`requestLayout()`或`invalidate()`操作, 处理完所有事件之后 由一次`performTraversals()`统一完成


## 4.2 事件处理逻辑的分歧(ViewRootImpl)

    private void deliverInputEvent(QueuedInputEvent q) {
		// 用于输出一致性测试日志
        if (mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onInputEvent(q.mEvent, 0);
        }

        InputStage stage;
		// 判断QueuedInputEvent 中的mFlags中是否存在 FLAG_UNHANDLED 标记
		// 第一次进来应该是不存在的
        if (q.shouldSendToSynthesizer()) {
			// SyntheticInputStage
            stage = mSyntheticInputStage;
        } else {
			// mFirstPostImeInputStage   = EarlyPostImeInputStage
			// mFirstInputStage  =   NativePreImeInputStage
            stage = q.shouldSkipIme() ? mFirstPostImeInputStage : mFirstInputStage;
        }
		// 执行事件分发
        if (stage != null) {
            stage.deliver(q);
        } else {
			//结束事件分发
            finishInputEvent(q);
        }
    }

### 4.2.1 QueuedInputEvent.shouldSendToSynthesizer()

        public boolean shouldSendToSynthesizer() {
            if ((mFlags & FLAG_UNHANDLED) != 0) {
                return true;
            }

            return false;
        }

- 判断`mFlags`中是否存在`FLAG_UNHANDLED`标记

### 4.2.2 QueuedInputEvent.shouldSkipTme()

        public boolean shouldSkipIme() {
            if ((mFlags & FLAG_DELIVER_POST_IME) != 0) {
                return true;
            }
			//判断类型是否为 MotionEvent
			//并且事件的来源是 与显示器相关联的指示器设备 或 SOURCE_ROTARY_ENCODER
            return mEvent instanceof MotionEvent
                    && (mEvent.isFromSource(InputDevice.SOURCE_CLASS_POINTER)
                        || mEvent.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER));
        }

- `mEvent`在`ViewRootImpl.obtainQueuedInputEvent()`中被赋值.其代表的就是一个原始的`InputEvent`


## 4.3 事件处理逻辑的终点(ViewRootImpl)

无论事件处理逻辑有多少个处理逻辑,应输入系统事件发送循环的要求,最终都会调用`ViewRootImpl.finishInputEvent()`, 这个方法会向`InputDispatcher`发送输入事件处理完毕的反馈,同时也标志着一条输入事件的处理流程的结束

    private void finishInputEvent(QueuedInputEvent q) {
		// 回收输入事件,并向InputDispatcher发送反馈
        if (q.mReceiver != null) {
			// 如果mReceiver 不为空,表示这是一个来自InputEventReceiver 的事件,需要向InputEventReceiver 发送反馈
			// 事件实例的回收由InputEventReceiver 完成
            boolean handled = (q.mFlags & QueuedInputEvent.FLAG_FINISHED_HANDLED) != 0;
            q.mReceiver.finishInputEvent(q.mEvent, handled);
        } else {
			//mReceiver为空,说明这是ViewRootImpl自行创建的事件!
			// 此时只需要将事件实例回收即可
            q.mEvent.recycleIfNeededAfterDispatch();
        }
		// 回收不再有效的QueuedInputEvent实例
		// 被回收的实例会组成一个 mQueuedInputEventPool 为头部的单向链表
		// 方便下次obtainQueuedInputEvent()时进行复用
        recycleQueuedInputEvent(q);
    }


### 4.3.1 QueuedInputEvent.recycleQueuedInputEvent()

**`QueuedInputEvent`类是`ViewRootImpl`类的内部类**

    private void recycleQueuedInputEvent(QueuedInputEvent q) {
        q.mEvent = null;
        q.mReceiver = null;

        if (mQueuedInputEventPoolSize < MAX_QUEUED_INPUT_EVENT_POOL_SIZE) {
            mQueuedInputEventPoolSize += 1;
            q.mNext = mQueuedInputEventPool;
            mQueuedInputEventPool = q;
        }
    }

# 5. 事件的派发分析

**从4.2节得知事件的处理会调用`InputStage`的`deliver()`.**

- `InputStage`是一个抽象类,其有`EarlyPostImeInputStage`,`SyntheticInputStage`,`NativePreImeInputStage`等子类

- `ViewRootImpl`并不需要将触摸事件派发给输入法,因为`InputDispatcher`会将点击到输入法的窗口的事件直接派发给它,而不需要通过`ViewRootImpl`

## 5.1 InputStage.deliver()

        public final void deliver(QueuedInputEvent q) {
			// 判断事件是否已经结束
            if ((q.mFlags & QueuedInputEvent.FLAG_FINISHED) != 0) {
				// 已经结束,将事件转发给下一个
                forward(q);
			//判断事件是否需要被丢弃
			// 主要会对视图是否已经添加,焦点是否正常获取进行检查
            } else if (shouldDropInputEvent(q)) {
                finish(q, false);
            } else {
				// onProcess()处理事件,并将结果和事件传递给apply()方法
                apply(q, onProcess(q));
            }
        }

- 子类没有重写过`deliver()`的逻辑

- `deliver()`处理逻辑

	1. 首先会判断事件是否已经结束,如果已经结束 则会将事件转发给下一个`Stage`进行处理

	2. 判断事件是否需要被丢弃,即对视图的状态,焦点状态等进行检查

	3. 调用`onProcess()`去处理事件,并将结果交给`apply()`去处理.(`onProcess()`方法在子类中被重写)

	

## 5.2 InputStage.forward()

	/**
	* 转发事件到下一阶段
	*/
	protected void forward(QueuedInputEvent q) {
		onDeliverToNext(q);
	}

### 5.2.1 InputStage.onDeliverToNext()

	/**
	* Called when an event is being delivered to the next stage.
	*/
	protected void onDeliverToNext(QueuedInputEvent q) {
		// mNext 也是一个InputStage
		// 简单的判断是否存在下一个InputStage ,如果存在就继续转发
		if (mNext != null) {
			mNext.deliver(q);
		} else {
			//不存在 就去回收事件
			finishInputEvent(q);
		}
	}

## 5.3 InputStage.finish()

	/**
	* 给输入事件加上`FLAG_FINISHED`标记,并转发它到下一个Stage
	*/
	protected void finish(QueuedInputEvent q, boolean handled) {
		q.mFlags |= QueuedInputEvent.FLAG_FINISHED;
		if (handled) {
			// 表示处理成功
			q.mFlags |= QueuedInputEvent.FLAG_FINISHED_HANDLED;
		}
		//去转发
		forward(q);
	}

## 5.4 InputStage.onProcess()

	/**
	* Called when an event is ready to be processed.
	* @return A result code indicating how the event was handled.
	*/
	protected int onProcess(QueuedInputEvent q) {
		return FORWARD;
	}

- 具体的逻辑都在其子类中被实现,默认的状态就是`FORWARD`,代表转发给下一个`Stage`去处理

- 在`ViewRootImpl.setView()`中创建了几个`InputStage`,并进行了关联,不同类型的`InputStage`会去处理不同类型的事件

### 5.4.1 ViewPostImeInputStage.onProcess()

        @Override
        protected int onProcess(QueuedInputEvent q) {
			// 处理按键事件
            if (q.mEvent instanceof KeyEvent) {
                return processKeyEvent(q);
            } else {
                final int source = q.mEvent.getSource();
				//处理触摸事件
                if ((source & InputDevice.SOURCE_CLASS_POINTER) != 0) {
                    return processPointerEvent(q);
	
				//处理轨迹球事件
                } else if ((source & InputDevice.SOURCE_CLASS_TRACKBALL) != 0) {
                    return processTrackballEvent(q);

				// 处理其他Motion事件,如悬浮(Hover),游戏手柄等
                } else {
                    return processGenericMotionEvent(q);
                }
            }
        }

- 处理触摸事件的逻辑在`processPointerEvent()`之中

### 5.4.2 ViewPostImeInputStage.processPointerEvent()

	private int processPointerEvent(QueuedInputEvent q) {
		
		final MotionEvent event = (MotionEvent)q.mEvent;

		mAttachInfo.mUnbufferedDispatchRequested = false;
		mAttachInfo.mHandlingPointerEvent = true;
		// 这一步将事件交给了View去派发
		// mView 可知是 DecorView
		boolean handled = mView.dispatchPointerEvent(event);
		maybeUpdatePointerIcon(event);
		maybeUpdateTooltip(event);
		mAttachInfo.mHandlingPointerEvent = false;
		if (mAttachInfo.mUnbufferedDispatchRequested && !mUnbufferedInputDispatch) {
			mUnbufferedInputDispatch = true;
			if (mConsumeBatchedInputScheduled) {
                    scheduleConsumeBatchedInputImmediately();
			}
		}
		//根据事件的处理结果 返回不同的结果
		return handled ? FINISH_HANDLED : FORWARD;
	}

- `MotionEvent`是`InputEvent`的子类,用来表示移动事件(鼠标,笔,手指,轨迹球)

### 5.4.3 View.dispatchPointerEvent()

    public final boolean dispatchPointerEvent(MotionEvent event) {
        if (event.isTouchEvent()) {
            return dispatchTouchEvent(event);
        } else {
            return dispatchGenericMotionEvent(event);
        }
    }

- 根据是否是触摸事件 进行分发