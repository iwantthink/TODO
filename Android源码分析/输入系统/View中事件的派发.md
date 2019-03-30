# View中事件的派发


# 1. 简介

从[ViewRootImpl中输入事件的派发.md]()中最后一小节可知,在`ViewPostImeInputStage.processPointerEvent()`方法中,事件最终通过`DecorView mView`去派发

这里的`PointerEvent`是包含以`MotionEvent.getAction()`进行区分的俩种事件,**以是否实际接触到屏幕为区分**

1. 实际的触摸事件,例如`ACTION_DOWN/MOVE/UP`等实际接触到屏幕所产生的事件

2. 未接触到屏幕的事件,`ACTION_HOVER_ENTER/MOVE/UP`

# 2 MotionEvent

触摸事件被封装在一个继承自`InputEvent`类的`MotionEvent`中,包含了多种用于描述一次触摸的详细信息

## 2.1 基本信息的获取

**`MotionEvent`最基本的俩个信息**

1. 通过`getAction()`方法获取的动作信息

2. 通过`getX()/getY()`方法获取的位置信息

## 2.2 动作的信息

由于多点触摸的存在,`MotionEvent`中获取信息的方法与`KeyEvent`类的`getAction(),getKeyCode()`不同

- **通过`MotionEvent.getAction()`所获得的动作信息是一个复合值**

	其低8位描述了实际的动作,如`ACTION_DOWN,ACTION_UP`等,其9~16位描述了引发此事件的触控点从0开始的索引号

	**因此在实际使用过程中,需要将这俩个信息进行分离**.

	1. 通过`MotionEvent.getActionMasked()`获取实际的动作

	2. 通过`MotionEvent.getActionIndex()`获取此事件所代表的触控点的索引号


另外,虽然**一个`MotionEvent`由一个触控点所引发,但是其包含了所有触控点的位置信息**,以便开发者在收到一个`MotionEvent`时根据所有触控点的信息进行计算和决策.因此`MotionEvent.getX()/getY()`可以接收触控点的**索引号**为参数,从而返回指定触摸点的触摸位置

## 2.3 一般开发流程

**一般情况下,开发者在收到一个`MotionEvent`之后**

1. 需要先通过`MotionEvent.getActionMasked()`获取其实际的动作,

2. 然后通过`MotionEvent.getActionIndex()`获取引发这一事件的触控点的**索引号**

3. 然后再根据索引号获取触控点的坐标信息(`getX(),getY()`) 和 `ID`(`getPointerID()`)

	- 注意**索引号只是获取这些信息的一个临时的工具**

## 2.4 触控点的索引号

`MotionEvent`内部有一个`PointerProperties`类型的数组`gSharedTempPointerProperties`,其每个元素都描述了一个触控点的信息,**所谓索引号就是一个触控点在这个数组中所在的位置**

## 2.5 触控点的ID号

在多点触摸的过程中,伴随着用户手指的抬起和按下,一个触控点在数组中的位置不是一成不变的

- 例如,用户所按下的第二个点B 的索引号为1, 当用户首先抬起其所按下的第一个点A(索引号0)之后, 点A在`gSharedTempPointerProperties`的信息会被删除,从而使得点B的索引号变为0,**因此触控点的索引号并不能用来识别或追踪一个特定触控点**

	**开发者需要通过触控点的ID达到识别或追踪一个特定触控点的目的,触控点的ID存储在`PointerProperties`结构体中,可以通过`MotionEvent.getPointerID()`方法获得,与`getX()/getY()`方法类似,这一方法需要索引号作为参数**

## 2.6 特殊情况

**当`MotionEvent`所携带的动作为`ACTION_MOVE`时,其`getAction()`所获得的动作信息并不包含触控点的索引**

- 因为`ACTION_MOVE`并不会导致增加或减少触控点,**不过它仍然保存了所有触控点的位置/ID等信息**

开发者可以通过`MotionEvent.getPointerCount()`获得此时有多少触控点处于活动状态,并通过for循环遍历每一个触控点的信息

**从这一事实可知,`getAction()`中所包含的触控点索引号 其实是为了通知开发者是否产生了新的触控点(按下),或某个触控点被移除(抬起)**

# 3. 触摸事件的序列

触摸事件的序列是**用户从第一个手指按下开始,到最后一个手指抬起这一过程所产生的`MotionEvent`序列**

- **单点触摸**的事件序列

	从一个`ACTION_DOWN`开始,经历一系列的`ACTION_MOVE`,以一个`ACTION_UP`结束

- **多点触摸**的事件序列

	以一个`ACTION_DOWN`开始,经历一系列的`ACTION_MOVE`, 但是当用户的另一个手指按下时会产生一个`ACTION_POINTER_DOWN`,这之后如果某一个手指抬起时会产生一个`ACTION_POINTER_UP`,当最后一个手指抬起时,以一个`ACTION_UP`结束事件序列

	在这个过程中,开发者通过事件所携带的触控点的ID追踪某一个触控点的始末

**Android在派发触摸事件时,有一个很重要的原则,事件序列不可中断性,即一旦一个控件决定接受一个触控点的`ACTION_DOWN`或`ACTION_POINTER_DOWN`(在事件处理函数中返回true),那么控件将接受事件序列后续的所有事件,即便触控点移动到控件区域之外**

## 3.1 示例

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fx9wqa0rwej20pv0cbaec.jpg)

上面是拥有三个触控点的事件序列,以`ACTION_DOWN`开始,`ACTION_UP`结束,某额外一触控点在中途的按下和抬起由`ACTION_POINTER_DOWN/UP`事件表示,它们在`getAction()`中所携带的索引号指示了这一动作的触控点的索引,并且这一索引伴随着当前处于活动状态的触控点的数量的变化而变化,但是触控点的ID始终不变

## 3.2 事件序列的拆分

多点触摸下的事件序列中,每一个触摸点的信息都足以独立形成一个事件序列

如3.1节中的例子,触控点1的信息组成一条单点序列,触控点2和3 组成一条双点序列. 这俩条序列被称为原始序列的子序列,这一行为就被称为事件序列的拆分(Split)

`MotionEvent`的拆分可以通过`MotionEvent.split()`方法完成,可以从当前`MotionEvent`中产生一个新的仅包含特定触控点信息的`MotionEvent`,而这个新的`MotionEvent`则称为子序列的一部分

- `MotionEvent.split()`是一个被`@hide`修饰的方法

### 3.2.1 拆分的作用
![](http://ww1.sinaimg.cn/large/6ab93b35gy1fx9x8zdr0pj20md061myo.jpg)

当一个`ViewGroup`中包含俩个子控件,用户的俩个手指分别按在控件1 和 控件2上时, 因为俩个触控点都落在`ViewGroup`中,因此`ViewGroup`会收到一条双点的事件序列,那么当`ViewGroup`将事件派发给子控件时,就需要将双点事件序列拆分成俩条单点事件序列,分别派发给子控件


## 3.3 触摸事件的结束标志

触摸事件的序列除`ACTION_UP`之外还有另外一个结束标记`ACTION_CANCEL`,与前者不同的是,后者代表控件需要中断对事件的处理并自己恢复到接收事件序列之前的状态

例如,一个正在接收事件序列的控件从控件树中被移除,或者发生了页面切换等,它将收到`ACTION_CANCEL`


# 4. 控件对触摸事件的接收与处理

`ViewRootImpl`将触摸事件交给了根控件,并调用了其`dispatchPointerEvent()`方法

## 4.1 View.dispatchPointerEvent()

    public final boolean dispatchPointerEvent(MotionEvent event) {
        if (event.isTouchEvent()) {
            return dispatchTouchEvent(event);
        } else {
            return dispatchGenericMotionEvent(event);
        }
    }

- 在该方法中会判断当前事件是否是触摸事件,然后将触摸事件交给`dispatchTouchEvent()`进行处理

- **`dispatchTouchEvent()`有`View`和`ViewGroup`俩种实现**

	1. `ViewGroup`的实现负责将触摸事件沿着控件树向子控件进行派发

	2. `View`的实现则主要用于事件接收与处理工作

# 5. View对触摸事件的派发

View.dispatchTouchEvent()

    /**
     * Pass the touch screen motion event down to the target view, or this
     * view if it is the target.
     *
     * @param event The motion event to be dispatched.
     * @return True if the event was handled by the view, false otherwise.
     */
    public boolean dispatchTouchEvent(MotionEvent event) {
		............省略代码................
		//处理结果
        boolean result = false;
		............省略代码................

		//返回不含触摸事件序列的动作信息
        final int actionMasked = event.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            // Defensive cleanup for new gesture
			// 停止正在进行的嵌套滚动
            stopNestedScroll();
        }
		// 过滤触摸事件
		// 处于对最终用户信息安全角度的考虑,当本窗口位于另外一个非全屏窗口之下时,可能会阻止控件处理触摸事件
        if (onFilterTouchEventForSecurity(event)) {
			//当前控件可用,且事件是scroll bar 拖动 并且被处理成功
            if ((mViewFlags & ENABLED_MASK) == ENABLED && handleScrollBarDragging(event)) {
                result = true;
            }

			// 尝试让此控件的onTouchListener处理触摸事件
            ListenerInfo li = mListenerInfo;
            if (li != null && li.mOnTouchListener != null
                    && (mViewFlags & ENABLED_MASK) == ENABLED
                    && li.mOnTouchListener.onTouch(this, event)) {
                result = true;
            }

			//onTouchListener()方法不处理事件
			//尝试让onTouchEvent()回调处理事件
            if (!result && onTouchEvent(event)) {
                result = true;
            }
        }
		............省略代码................

        // Clean up after nested scrolls if this is the end of a gesture;
        // also cancel it if we tried an ACTION_DOWN but we didn't want the rest
        // of the gesture.
        if (actionMasked == MotionEvent.ACTION_UP ||
                actionMasked == MotionEvent.ACTION_CANCEL ||
                (actionMasked == MotionEvent.ACTION_DOWN && !result)) {
            stopNestedScroll();
        }

        return result;
    }

- 总体流程就是 **先尝试让`onTouchListener()`处理事件,如果没有处理成功,那么就让`onTouchEvent()`回调进行处理.**

- `ENABLED_MASK`: 与`setFlags()`一起使用的,用于判断此`View`是否可用的标记

## 5.1 View.onFilterTouchEventForSecurity()

	//根据安全策略过滤触摸事件
    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        // 判断是否存在遮挡
        if ((mViewFlags & FILTER_TOUCHES_WHEN_OBSCURED) != 0
                && (event.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0) {
            // 窗口被遮挡,丢弃此事件
            return false;
        }
        return true;
    }

- `MotionEvent.FLAG_WINDOW_IS_OBSCURED`: 表明此窗口部分或完整地被另外一个窗口所遮挡,此时用户可能因为无法看到当前窗口的一些敏感信息 或被遮挡窗口的恶意信息所蒙骗而进行一些不安全的操作

	开发者可以通过在执行敏感行为的控件上调用`View.setFilterTouchesWhenObscured()`方法在`mViewFlags`中添加这一标记

# 6. ViewGroup对触摸事件的派发

`ViewGroup`的主要工作是将触摸事件派发给合适的子控件

事件的派发可以划分为**确定派发目标 和 执行派发俩个部分**

- **确定派发目标**

	**对于触摸事件来说,序列不可中断性原则,确定派发目标发生在收到`ACTION_DOWN`或`ACTION_POINTER_DOWN`时刻**

	- `ViewGroup`会按照逆绘制顺序依次查找事件坐标所落在的子控件,并将事件发送给子控件的`dispatchTouchEvent()`方法,然后根据返回值确定第一个愿意接受这一序列的子控件,将其确定为后续事件的派发目标

	一旦通过`ACTION_DOWN`或`ACTION_POINTER_DOWN`确定派发目标,`ViewGroup`会将此触控点的ID与目标控件建立绑定关系,属于此触控点的事件序列都会发送给这一目标

	`ViewGroup`通过一个`TouchTarget`类的实例来描述这种绑定关系,这个类保存了一个触控点ID的列表 以及一个 `View`实例,以此实现从触控点ID到目标控件的映射



- **执行派发**

	执行派发主要是将事件传递给目标的`dispatchTouchEvent()`方法. 

	由于多点触控的存在,执行派发时可能需要将`MotionEvent`进行拆分, 因此`ViewGroup`在其派发过程中可能维护着多个`TouchTarget`实例

	`TouchTarget`存在一个`next`成员变量,**它其实是一个单向链表**. `ViewGroup`将所有的`TouchTarget`存储在一个以`mFirstTouchTarget`为表头的单向链表中

		    // First touch target in the linked list of touch targets.
	    	private TouchTarget mFirstTouchTarget;


**`ViewGroup.dispatchTouchEvent()`中会通过`onInterceptTouchEvent()`尝试对输入事件进行截获**

## 6.1 ViewGroup.dispatchTouchEvent() - 确定派发目标

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
		...........省略代码..............

        boolean handled = false;
		//同样的会对遮盖状态进行检查和过滤
        if (onFilterTouchEventForSecurity(ev)) {
			// 获取动作,包含触控点的索引号
            final int action = ev.getAction();
			//获取实际的动作,不包含触控点的索引号
            final int actionMasked = action & MotionEvent.ACTION_MASK;

            //处理一个初始化的down
            if (actionMasked == MotionEvent.ACTION_DOWN) {
				// ACTION_DOWN表示一条新的事件序列的开始,这是会重置一切状态,包括清空TouchTarget列表,这样ViewGroup 就可以进行新的触摸事件派发
                cancelAndClearTouchTargets(ev);
                resetTouchState();
            }

            // 判断是否有拦截
            final boolean intercepted;
			// 事件动作为DOWN, 或者事件序列已经确定派发目标 才会重新计算是否需要拦截
            if (actionMasked == MotionEvent.ACTION_DOWN
                    || mFirstTouchTarget != null) {
				// 判断是否禁止ViewGroup拦截,即不允许ViewGroup拦截事件
                final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
				// 如果没有禁止拦截
                if (!disallowIntercept) {
					//调用回调 获取是否拦截
                    intercepted = onInterceptTouchEvent(ev);
                    ev.setAction(action); // restore action in case it was changed
                } else {
				// 如果禁止拦截
                    intercepted = false;
                }
            } else {
                // There are no touch targets and this action is not an initial down
                // so this view group continues to intercept touches.
				// 没有 派发目标,并且不是DOWN事件
				// 那么ViewGroup 会拦截该事件序列
                intercepted = true;
            }

            // If intercepted, start normal event dispatch. Also if there is already
            // a view that is handling the gesture, do normal event dispatch.
            if (intercepted || mFirstTouchTarget != null) {
                ev.setTargetAccessibilityFocus(false);
            }

            // canceled表示ViewGroup所收到的这一事件序列是否被取消
			// 例如被移除控件树 或者 Activity切换等 
            final boolean canceled = resetCancelNextUpFlag(this)
                    || actionMasked == MotionEvent.ACTION_CANCEL;

            // split表示此控件树是否启用事件拆分机制
			// 开发者通过setMotionEventSplittingEnabled()方法控制这一机制
			// 这个flag 在 initViewGroup()中被设置(构造函数调用了这个方法),所以默认情况下是存在这个flag的
            final boolean split = (mGroupFlags & FLAG_SPLIT_MOTION_EVENTS) != 0;
			// 如果此次事件产生了新的派发目标,那么会保存在这个局部变量中
			// 派发目标仅在(ACTION_DOWN 或 ACTION_POINTER_DOWN)发生
            TouchTarget newTouchTarget = null;
			// 如果确定派发目标 调用了子控件的dispatchTouchEvent(),代表事件已经完成派发
			// 这种情况下此变量置为true , 以跳过后续的派发过程
            boolean alreadyDispatchedToNewTouchTarget = false;

			// 如果事件序列没有被取消 并且 没有被当前ViewGroup拦截
			// 满足这俩个条件才有进行派发目标查找的必要
            if (!canceled && !intercepted) {

                // If the event is targeting accessiiblity focus we give it to the
                // view that has accessibility focus and if it does not handle it
                // we clear the flag and dispatch the event to all children as usual.
                // We are looking up the accessibility focused host to avoid keeping
                // state since these events are very rare.
                View childWithAccessibilityFocus = ev.isTargetAccessibilityFocus()
                        ? findChildWithAccessibilityFocus() : null;

				// 如果事件的实际动作是ACTION_DOWN 或 ACTION_POINTER_DOWN ,标志一个子序列的开始,此时需要执行派发目标的确定
                if (actionMasked == MotionEvent.ACTION_DOWN
                        || (split && actionMasked == MotionEvent.ACTION_POINTER_DOWN)
                        || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
					// 获取这一按下事件的触控点的索引号
                    final int actionIndex = ev.getActionIndex(); // always 0 for down

					// 通过索引号获取触控点的id
					// 为了能够在一个整型变量中存储一个ID列表,通过将1进行左移若干个位的方式将ID转换为2的ID次方的形式并存储在idBitsToAssign变量中
					// 当找到一个派发目标之后,会将这个idBitsToAssign添加到派发目标所对应的TouchTarget中,从而使得这一触控点被绑定在TouchTarget上
					// 根据int的容量, 可知Android控件系统最多可以支持32个触控点
					
					// 当 split为false及ViewGroup没有启用序列的拆分时,idBitsToAssign被设置为TouchTarget.All_POINTER_IDS,意思是所有触控点的事件都会被派发给后续被确定的目标控件
                    final int idBitsToAssign = split ? 1 << ev.getPointerId(actionIndex): TouchTarget.ALL_POINTER_IDS;

                    // Clean up earlier touch targets for this pointer id in case they
                    // have become out of sync.
                    removePointersFromTouchTargets(idBitsToAssign);

					//开始对子控件按照逆绘制顺序进行遍历,检查哪一个控件对这一新的事件子序列感兴趣
                    final int childrenCount = mChildrenCount;
                    if (newTouchTarget == null && childrenCount != 0) {
						// 触摸事件是基于位置进行派发目标的查找,因此需要先获取事件坐标
						// 通过触控点的索引号获取坐标
                        final float x = ev.getX(actionIndex);
                        final float y = ev.getY(actionIndex);

						// 返回一个预排序的View集合
                        final ArrayList<View> preorderedList = buildTouchDispatchChildList();
						// 是否使用自定义绘制顺序
                        final boolean customOrder = preorderedList == null
                                && isChildrenDrawingOrderEnabled();
                        final View[] children = mChildren;
						// 注意这里是逆绘制顺序
                        for (int i = childrenCount - 1; i >= 0; i--) {
							
                            final int childIndex = getAndVerifyPreorderedIndex(
                                    childrenCount, i, customOrder);
                            final View child = getAndVerifyPreorderedView(
                                    preorderedList, children, childIndex);

   							...........省略一段看不懂的代码,跟childWithAccessibilityFocus有关.................


							// 判断控件是否能处理事件 和 事件坐标是否落在控件之内,如果不符合则跳转到下一次循环
                            if (!canViewReceivePointerEvents(child)
                                    || !isTransformedTouchPointInView(x, y, child, null)) {
                                ev.setTargetAccessibilityFocus(false);
                                continue;
                            }

							// 从mFirstTouchTarget 链表中查找控件所对应的TouchTarget
							// 如果子控件所对应的TouchTarget已经存在,表明此控件已经在接收另外一个事件子序列,ViewGroup会默认此控件对这一条子序列也感兴趣
							// 此时将触控点ID绑定在其上,并终止派发目标的查找,后续的派发工作会据此 将此事件派发给这一控件
                            newTouchTarget = getTouchTarget(child);
                            if (newTouchTarget != null) {
                                // Child is already receiving touch within its bounds.
                                // Give it the new pointer in addition to the ones it is handling.
                                newTouchTarget.pointerIdBits |= idBitsToAssign;
                                break;
                            }

                            resetCancelNextUpFlag(child);

							// 使用dispatchTransformedTouchEvent()方法尝试将事件派发给当前子控件:
							// 1. 根据最后一个参数idBitsToAssign 将其指定的触控点的信息从原始事件ev中分离并产生一个新的MotionEvent的Action
							// 2. 如果有必要,修改新MotionEvent的Action
							// 3. 把事件的坐标转换到子控件的坐标系下
							// 4. 将新的MotionEvent派发给子控件
                            if (dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {
                                // Child wants to receive touch within its bounds.
                                mLastTouchDownTime = ev.getDownTime();
                                if (preorderedList != null) {
                                    // childIndex points into presorted list, find original index
                                    for (int j = 0; j < childrenCount; j++) {
                                        if (children[childIndex] == mChildren[j]) {
                                            mLastTouchDownIndex = j;
                                            break;
                                        }
                                    }
                                } else {
                                    mLastTouchDownIndex = childIndex;
                                }
                                mLastTouchDownX = ev.getX();
                                mLastTouchDownY = ev.getY();
								// 当子控件决定接受这一事件,为其创建一个TouchTarget并保存在mFirstTouchTarget链表中,从此之后,来自此触控点的事件都会派发给这个子控件
                                newTouchTarget = addTouchTarget(child, idBitsToAssign);
								// 表示此事件已经在子控件中得到处理, 后续的事件派发流程将不会再次发送此事件到这一子控件
                                alreadyDispatchedToNewTouchTarget = true;
                                break;
                            }

                            // The accessibility focus didn't handle the event, so clear
                            // the flag and do a normal dispatch to all children.
                            ev.setTargetAccessibilityFocus(false);
                        }
						// 遍历结束,清空数据避免泄露
                        if (preorderedList != null) preorderedList.clear();
                    }

					// 如果上述遍历过程并没有找到能够接受此事件序列的子控件,ViewGroup会将这一事件序列强行交给最近一次接受事件序列的子控件
                    if (newTouchTarget == null && mFirstTouchTarget != null) {
                        // Did not find a child to receive the event.
                        // Assign the pointer to the least recently added target.
                        newTouchTarget = mFirstTouchTarget;
                        while (newTouchTarget.next != null) {
                            newTouchTarget = newTouchTarget.next;
                        }
                        newTouchTarget.pointerIdBits |= idBitsToAssign;
                    }
                }
            }
		..........省略实际执行派发工作的代码..............
    }

- **`ViewGroup.dispatchTouchEvent()`方法前半部分的目的是以更新`mFirstTouchTarget`链表的方式确定一系列的派发目标**.派发目标由`TouchTarget`表示,并在`TouchTarget`中的`pointerIdBits`中保管目标所感兴趣的触控点的列表,这是后续执行派发的关键.

**`ViewGroup`确定派发目标的原则:**

1. 仅当事件的动作为`ACTION_DOWN`或`ACTION_POINTER_DOWN`时才会进行派发目标的查找. 因为这些动作标志着新的事件子序列的开始,`ViewGroup`仅需要为新的序列查找一个派发目标

2. `ViewGroup`会沿着绘制顺序相反的方向查找. 用户肯定是希望点击在能够看得到的东西上.**因此`ZOrder`越大的控件接收事件的优先级越大**

3. `ViewGroup`会将一个控件作为派发目标的先决条件是 控件能够接收事件 并且 事件的坐标位于其边界内.  当事件落入子控件内部并且它接受过另外一条事件序列时,则直接认定它就是此事件序列的派发目标 . 因为当用户将俩根手指按在一个控件上时,很可能是想要对此控件进行多点操作

4. `ViewGroup`会首先通过`dispatchTransformedTouchEvent()`尝试将事件派发给候选控件,倘若控件在其事件处理函数中返回true,则可以确定它就是派发目标,否则继续测试下一个控件

5. 当遍历了所有子控件后都无法找到一个合适的派发目标,`ViewGroup`会强行将接受了上一条事件序列的子控件作为派发目标. 因为`ViewGroup`猜测用户以相邻次序按下的俩根手指应该包含能够共同完成某种任务的期望

	可以看出,`ViewGroup`尽其所能将事件派发给子控件,而不是将事件留给自己处理. 不过当目前没有任何一个子控件正在接收事件序列时(mFirstTouchTarget 为null),`ViewGroup`只能自己处理



### 6.1.1 ViewGroup.cancelAndClearTouchTargets()

    /**
     * Cancels and clears all touch targets.
     */
    private void cancelAndClearTouchTargets(MotionEvent event) {
        if (mFirstTouchTarget != null) {
            boolean syntheticEvent = false;
			//创建一个合成的MotionEvent
            if (event == null) {
                final long now = SystemClock.uptimeMillis();
                event = MotionEvent.obtain(now, now,
                        MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
                event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                syntheticEvent = true;
            }

			// 遍历链表,清空标记
            for (TouchTarget target = mFirstTouchTarget; target != null; target = target.next) {
                resetCancelNextUpFlag(target.child);
                dispatchTransformedTouchEvent(event, true, target.child, target.pointerIdBits);
            }
            clearTouchTargets();

            if (syntheticEvent) {
                event.recycle();
            }
        }
    }

### 6.1.2 ViewGroup.buildTouchDispatchChildList()

    public ArrayList<View> buildTouchDispatchChildList() {
        return buildOrderedChildList();
    }

    /**
     * 用已经预排序过的控件的子类去填充并返回 mPreSortedChildren
     * 这个预排序顺序指的是 优先通过Z坐标,然后按照子绘图顺序
     * mPreSortedChildren 必须在使用后清空,避免泄露
     */
    ArrayList<View> buildOrderedChildList() {
        final int childrenCount = mChildrenCount;
		// 如果子类只有一个 那直接返回,如果子类中存在没有z坐标的 也直接返回 null
        if (childrenCount <= 1 || !hasChildWithZ()) return null;

		.........省略复用或创建ArrayList mPreSortedChildren 的过程.............

		// 判断ViewGroup是否需要使用 getChildDrawingOrder()方法定义的顺序去绘制子类
        final boolean customOrder = isChildrenDrawingOrderEnabled();
        for (int i = 0; i < childrenCount; i++) {
			// 获取下一个被添加到list中的view的索引
            final int childIndex = getAndVerifyPreorderedIndex(childrenCount, i, customOrder);
			// 获取这个view
            final View nextChild = mChildren[childIndex];
			// 获取View的Z坐标
            final float currentZ = nextChild.getZ();

            // 根据 z坐标的大小计算 当前控件在mPreSortedChildren中的位置
			// z坐标越大 位置越靠后
            int insertIndex = i;
			// 跟之前插入list 中的view 进行z坐标的比较,z 坐标越大 insertIndex 越大
            while (insertIndex > 0 && mPreSortedChildren.get(insertIndex - 1).getZ() > currentZ) {
                insertIndex--;
            }
			//按照z坐标升序
            mPreSortedChildren.add(insertIndex, nextChild);
        }
        return mPreSortedChildren;
    }

#### 6.1.2.1 ViewGroup.getChildDrawingOrder()

    /**
     * 返回此次迭代所需要绘制的子类索引
     * 
     */
    protected int getChildDrawingOrder(int childCount, int i) {
        return i;
    }

- 可以通过重写这个方法自定义绘制顺序 , 默认 会直接返回 `i`(`i`表示)

- 如果希望这个方法能够被使用到,必须先调用`setChildrenDrawingOrderEnabled(boolean)`开启子类绘制顺序

#### 6.1.2.2 ViewGroup.getAndVerifyPreorderedIndex()

    private int getAndVerifyPreorderedIndex(int childrenCount, int i, boolean customOrder) {
        final int childIndex;
		// 判断是否使用自定义的索引号
        if (customOrder) {
            final int childIndex1 = getChildDrawingOrder(childrenCount, i);
			// 对自定义返回的绘制顺序进行校验,不能大于子类的总数...
            if (childIndex1 >= childrenCount) {
                throw new IndexOutOfBoundsException(.....);
            }
            childIndex = childIndex1;
        } else {
            childIndex = i;
        }
        return childIndex;
    }

#### 6.1.2.3 ViewGroup.isChildrenDrawingOrderEnabled()

	/**
	 *	判断ViewGroup是否使用getChildDrawingOrder()提供的绘制顺序去绘制子类
	 */
    protected boolean isChildrenDrawingOrderEnabled() {
        return (mGroupFlags & FLAG_USE_CHILD_DRAWING_ORDER) == FLAG_USE_CHILD_DRAWING_ORDER;
    }

#### 6.1.2.4 ViewGroup.getAndVerifyPreorderedView()

    private static View getAndVerifyPreorderedView(ArrayList<View> preorderedList, View[] children,
            int childIndex) {
        final View child;
		// 是否存在按照预排列顺序排列好的list
        if (preorderedList != null) {
			// 从list中取出
            child = preorderedList.get(childIndex);
            if (child == null) {
                throw new RuntimeException("Invalid preorderedList contained null child at index "
                        + childIndex);
            }
        } else {
			// 直接从mChildren中取
            child = children[childIndex];
        }
        return child;
    }

## 6.2 ViewGroup.dispatchTouchEvent() - 执行派发工作

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
		.............

        boolean handled = false;
        if (onFilterTouchEventForSecurity(ev)) {

			...............省略查找派发目标的代码 6.1小节......................

            // Dispatch to touch targets.
            if (mFirstTouchTarget == null) {
                // 当mFirstTouchTarget 为null,表示之前没有任何一个合适的子控件接受事件序列,此时只能将事件序列交给ViewGroup自身处理
				// 同样的使用dispatchTransformedTouchEvent()将事件派发给ViewGroup自身,但是 第三个参数child = null
                handled = dispatchTransformedTouchEvent(ev, canceled, null,
                        TouchTarget.ALL_POINTER_IDS);
            } else {
                // 遍历链表 为每一个TouchTarget派发事件,除了已经派发过的
                TouchTarget predecessor = null;
                TouchTarget target = mFirstTouchTarget;
                while (target != null) {
                    final TouchTarget next = target.next;
					// target是上述 查找派发逻辑中找到的那个 target,说明已经处理了事件,不需要额外派发
                    if (alreadyDispatchedToNewTouchTarget && target == newTouchTarget) {
						// 标记已经处理成功
                        handled = true;
                    } else {
						// 表示因为某种原因需要中断目标控件继续接受事件序列,通常是因为目标控件即将被移出控件树,或者ViewGroup决定截取此事件序列
						// 此时仍然会将事件发送给目标控件,但是其动作会被改成ACTION_CANCEL
                        final boolean cancelChild = resetCancelNextUpFlag(target.child)
                                || intercepted;
						// 将事件派发给目标控件
                        if (dispatchTransformedTouchEvent(ev, cancelChild,
                                target.child, target.pointerIdBits)) {
                            handled = true;
                        }
						
						// 如果已经决定终止目标控件继续接受事件序列,则将其对应的TouchTarget从链表中删除并回收.
						// 下次事件到来时将不会为其派发事件
                        if (cancelChild) {
                            if (predecessor == null) {
								// 将当前target从链表中移除
                                mFirstTouchTarget = next;
                            } else {
                                predecessor.next = next;
                            }
                            target.recycle();
                            target = next;
                            continue;
                        }
                    }
                    predecessor = target;
					// 切换到下一个派发目标
                    target = next;
                }
            }

            // Update list of touch targets for pointer up or cancel, if needed.
			...............
        }

		................
        return handled;
    }

### 6.2.1 ViewGroup.dispatchTransformedTouchEvent()

    /**
     * 将MotionEvent 转换成特定子控件的坐标空间
     * 过滤掉不相关的触控点id,并在必要的时候覆盖其action
     * 如果child==null ,则假定ViewGroup将处理事件序列
     */
    private boolean dispatchTransformedTouchEvent(MotionEvent event, boolean cancel,
            View child, int desiredPointerIdBits) {
        final boolean handled;

		// 首先处理当需要终止子控件对事件序列进行处理的情况
		// 终止事件序列的处理 不需要执行坐标转换和过滤,需要对动作进行处理
		// 此时只需要将事件的动作替换为ACTION_CANCEL 并调用子控件的dispatchTouchEvent()即可
		// 并不需要坐标变换等操作,因为ACTION_CANCEL 是一个要求接受者立刻终止事件处理并恢复到事件处理之前状态的一个记号而已,此时其所携带的除动作之外的信息都是无效的
        final int oldAction = event.getAction();
        if (cancel || oldAction == MotionEvent.ACTION_CANCEL) {
            event.setAction(MotionEvent.ACTION_CANCEL);
            if (child == null) {
                handled = super.dispatchTouchEvent(event);
            } else {
                handled = child.dispatchTouchEvent(event);
            }
            event.setAction(oldAction);
			//事件派发完毕,直接返回
            return handled;
        }

        // 这俩个局部变量是确定是否需要进行事件序列分割的依据
		// oldPointerIdBits 表示了原始事件中所有触控点的列表
		// newPointerIdBits 表示了目标希望接受的触控点的列表 , 是子集
		// desiredPointerIdBits 已经描述了目标希望接受的触控点的列表,仍然需要newPointerIdBits , 因为desiredPointerIdBits 的值有可能是 TouchTarget.All_POINTER_IDS(此时它并不能准确的表示实际需要派发的触控点列表)
        final int oldPointerIdBits = event.getPointerIdBits();
        final int newPointerIdBits = oldPointerIdBits & desiredPointerIdBits;

        // If for some reason we ended up in an inconsistent state where it looks like we
        // might produce a motion event with no pointers in it, then drop the event.
        if (newPointerIdBits == 0) {
            return false;
        }

        // transformedEvent 是一个来自原始MotionEvent 的新的MotionEvent,它只包含了目标所感兴趣的触控点,派发给目标事件对象的是它 而不是原始事件
        final MotionEvent transformedEvent;

		// 如果俩者相等 , 则表示目标对原始事件的所有触控点全盘接受,因此 transformedEvent 仅仅是原始事件的一个复制
        if (newPointerIdBits == oldPointerIdBits) {
			// 如果子类为空 ,说明ViewGroup自身接收事件序列
			// 子类不存在矩阵变换
			// 存在以上俩种情况,直接去处理事件序列
            if (child == null || child.hasIdentityMatrix()) {
                if (child == null) {
					//ViewGroup 自己处理事件序列
                    handled = super.dispatchTouchEvent(event);
                } else {
					// 进行矩阵变换
                    final float offsetX = mScrollX - child.mLeft;
                    final float offsetY = mScrollY - child.mTop;
                    event.offsetLocation(offsetX, offsetY);
					// 将变换后的事件交给子控件去处理
                    handled = child.dispatchTouchEvent(event);

                    event.offsetLocation(-offsetX, -offsetY);
                }
                return handled;
            }
            transformedEvent = MotionEvent.obtain(event);
        } else {
			// 当俩者不相等时
			// transformedEvent 仅仅只是 原始事件的一个子集,使用split()将其分离
            transformedEvent = event.split(newPointerIdBits);
        }

        // 执行必要的转换并转发
        if (child == null) {
			// 当child参数为null , 将事件发送给ViewGroup自身
            handled = super.dispatchTouchEvent(transformedEvent);
        } else {
			// 对transformedEvent 进行坐标系变换,使之位于派发目标的坐标系之中
			// 计算ViewGroup的滚动量 以及目标控件的位置
            final float offsetX = mScrollX - child.mLeft;
            final float offsetY = mScrollY - child.mTop;
            transformedEvent.offsetLocation(offsetX, offsetY);

			// 当目标控件中存在使用 setScaleX()等方法设置的矩阵变换时,将对事件坐标进行变换. 此次变换完成之后 , 事件坐标点便位于目标控件的坐标系
            if (! child.hasIdentityMatrix()) {
                transformedEvent.transform(child.getInverseMatrix());
            }
			// 将执行变换过的事件 发送给目标控件
            handled = child.dispatchTouchEvent(transformedEvent);
        }

        // 销毁
        transformedEvent.recycle();
        return handled;
    }

此方法包含的内容

- 处理`ACTION_CANCEL`

- 处理`transformedEvent`

	1. 根据目标所感兴趣的触控点列表生成`transformedEvent`,`transformedEvent`有可能是原始事件的copy,或者是仅包含部分触控点信息的一个子集

	2. 对`transformedEvent`进行坐标系变换,使坐标位于目标控件坐标系中

	3. 通过`dispatchTouchEvent()`将`transformedEvent`发送给目标控件


- 修改事件的Action, 这部分逻辑体现在`MotionEvent.split()`方法中

#### 6.2.1.1 View.hasIdentityMatrix()

    final boolean hasIdentityMatrix() {
        return mRenderNode.hasIdentityMatrix();
    }

- 判断是否存在单位矩阵,即没有通过`setScaleX()`等方法设置过矩阵变换

### 6.2.2 为什么需要修改Action?

在 3.1 示例 中,事件序列一定以`ACTION_DOWN`开始,经过一系列`ACTION_POINTER_DOWN`,`ACTION_POINTER_UP`,`ACTION_MOVE`之后以一条`ACTION_UP`结束

**特殊情况1:**

- 假设`ViewGroup`收到示例中的事件序列,它的一个子控件仅对触控点3 对应的子序列感兴趣,此时`ViewGroup`通过`MotionEvent.split()`方法将触控点3的信息分离出来并派发给子控件

	对于`ViewGroup`来说,触控点3 是`ACTION_POINTER_UP`->`ACTION_MOVE`->`ACTION_POINTER_UP`,这是合理的 . 但是对于目标子控件来说 这不合理,对于目标子控件来说,它需要接收的事件序列必须是以`ACTION_DOWN`开始,`ACTION_UP`结束

**特殊情况2:**

- `ViewGroup`中的子控件仅对触控点2感兴趣,那么当触控点3的按下事件发生时,其动作为`ACTION_POINTER_UP`,子控件肯定是对这个动作不感兴趣. 但是对其所携带的触控点2的坐标等信息是感兴趣的. 

	因此,当`ViewGroup`为子控件分离触控点2的信息到一个`transformedEvent`时,需要将事件的动作修改为`ACTION_MOVE`

#### 6.2.2.1 如何修改事件动作?

1. 首先,修改事件动作的情况仅发生在原始事件的动作包含`ACTION_POINTER_DOWN`/`ACTION_POINTER_UP`的情况

2. 当传递给`MotionEvent.split()`的触控点ID列表中仅包含一个触控点,并且它是引发`ACTION_POINTER_DOWN/UP`的触控点时,分离出的事件动作将被设置为`ACTION_DOWN/UP`

3. 当传递给`MotionEvent.split()`的触控点ID列表中不包含引发`ACTION_POINTER_DOWN/UP`的触控点时,则表示不关心这一按下或抬起动作,分离出的事件的动作将会被设置为`ACTION_MOVE`

4. 当传递给`MotionEvent.split()`的触控点ID列表中包含多个触控点,并且其中之一是引发`ACTION_POINTER_DOWN/UP`的触控点时,分离出的事件动作将会被保持为`ACTION_POINTER_DOWN/UP`,但是其包含的触控点索引将会根据新事件内部的`mSamplePointerCoords`数组的状况重新计算

## 6.3 移除派发目标

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
	    .............
	
	    boolean handled = false;
	    if (onFilterTouchEventForSecurity(ev)) {
	
	        ...............省略查找派发目标的代码 6.1小节......................
	
			...............省略派发事件的代码 6.2 小节...................
	
	        // Update list of touch targets for pointer up or cancel, if needed.
	                   // Update list of touch targets for pointer up or cancel, if needed.
	            if (canceled
	                    || actionMasked == MotionEvent.ACTION_UP
	                    || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
					//当ViewGroup收到一个 ACTION_UP 或 ACTION_CANCEL 事件时,整个事件序列已经结束,因此删除掉所有TouchTarget
	                resetTouchState();
	            } else if (split && actionMasked == MotionEvent.ACTION_POINTER_UP) {
					// 当事件 ACTION_POINTER_UP , 将其对应的触控点ID 从对应的TouchTarget中移除
					// removePointersFromTouchTargets() 会在TouchTarget的最后一个触控点ID被移除的同时,将这个TouchTarget从mFirstTouchTarget 链表中删除并销毁
	                final int actionIndex = ev.getActionIndex();
	                final int idBitsToRemove = 1 << ev.getPointerId(actionIndex);
	                removePointersFromTouchTargets(idBitsToRemove);
	            }
	    }
	
	    ................
	    return handled;
	}

### 6.3.1 ViewGroup.removePointersFromTouchTargets()

    private void removePointersFromTouchTargets(int pointerIdBits) {
        TouchTarget predecessor = null;
        TouchTarget target = mFirstTouchTarget;
        while (target != null) {
            final TouchTarget next = target.next;
            if ((target.pointerIdBits & pointerIdBits) != 0) {
                target.pointerIdBits &= ~pointerIdBits;
                if (target.pointerIdBits == 0) {
                    if (predecessor == null) {
                        mFirstTouchTarget = next;
                    } else {
                        predecessor.next = next;
                    }
                    target.recycle();
                    target = next;
                    continue;
                }
            }
            predecessor = target;
            target = next;
        }
    }

- 从链表中移除指定的经过处理的触控点id,同时会在`TouchTarget`中的最后一个触控点ID被移除时,将这个`TouchTarget`从`mFirstTouchTarget`链表中删除并销毁


# 7. 触摸事件派发的总结

触摸事件 因为 **多点触摸 和 事件序列拆分机制**的存在 而变得十分复杂,其本质就是**从根控件开始在其子控件中寻找目标子控件并发给目标子控件,然后再从目标子控件中继续刚才的寻找,直到事件得到处理,**

多点触摸与事件序列拆分是围绕`TouchTarget`完成的,**`TouchTarget`是绑定触控点与目标控件的纽带**

- `TouchTarget`主要有俩个功能 
	1. 确定派发目标 
	2. 执行派发

每当一个事件子序列到来,`ViewGroup`都会新建或选择一个现有的`TouchTarget`,并将子序列对应的触控点ID绑定在其上,从而生成`TouchTarget`感兴趣的触控点列表. 随后的事件到来时`ViewGroup`会从其所收到的`MotionEvent`中为每一个`TouchTarget`分离出包含它所感兴趣的触控点的新`MotionEvent`,然后派发给对应的子控件

`TouchTarget`的数量就是 事件序列经过此`ViewGroup`之后被拆分成的子序列的个数


