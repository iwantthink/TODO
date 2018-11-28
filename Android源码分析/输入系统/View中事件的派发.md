# View中事件的派发


# 1. 简介

从[ViewRootImpl中输入事件的派发.md]()中最后一小节可知,在`ViewPostImeInputStage.processPointerEvent()`方法中,事件被传递了`mView`去派发

这里的`PointerEvent`是包含以`MotionEvent.getAction()`进行区分的俩种事件,以是否实际接触到屏幕为区分

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

	因此在实际使用过程中,需要将这俩个信息进行分离.

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

`MotionEvent`的拆分可以通过`MotionEvent.split()`方法完成,可以从当前`MotionEvent`中产生一个新的仅包含特定触控点信息的`MotionEvent`,而这个新的`MotionEvent`则称为子序
列的一部分

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

- **`dispatchTouchEvent()`有`View`和`ViewGroup`俩种实现,`ViewGroup`的实现负责将触摸事件沿着控件树向子控件进行派发,而`View`的实现则主要用于事件接收与处理工作**

## 4.2 View对触摸事件的派发

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

### 4.2.1 View.onFilterTouchEventForSecurity()

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

## 4.3 ViewGroup对触摸事件的派发

`ViewGroup`的主要工作是将触摸事件派发给合适的子控件

事件的派发可以划分为**确定派发目标 和 执行派发俩个部分**

- **确定派发目标**

	对于触摸事件来说,序列不可中断性原则,确定派发目标发生在收到`ACTION_DOWN`或`ACTION_POINTER_DOWN`时刻

	- `ViewGroup`会按照逆绘制顺序依次查找事件坐标所落在的子控件,并将事件发送给子控件的`dispatchTouchEvent()`方法,然后根据返回值确定第一个愿意接受这一序列的子控件,将其确定为后续事件的派发目标

	一旦通过`ACTION_DOWN`或`ACTION_POINTER_DOWN`确定派发目标,`ViewGroup`会将此触控点的ID与目标控件建立绑定关系,属于此触控点的事件序列都会发送给这一目标

	`VIewGroup`通过一个`TouchTarget`类的实例来描述这种绑定关系,这个类保存了一个触控点ID的列表 以及一个 `View`实例,以此实现从触控点ID到目标控件的映射

- **执行派发**

	执行派发主要是将事件传递给目标的`dispatchTouchEvent()`方法. 

	由于多点触控的存在,执行派发时可能需要将`MotionEvent`进行拆分, 因此`ViewGroup`在其派发过程中可能维护着多个`TouchTarget`实例

	`TouchTarget`存在一个`next`成员变量,它其实是一个单向链表. `ViewGroup`将所有的`TouchTarget`存储在一个以`mFirstTouchTarget`为表头的单向链表中


`ViewGroup.dispatchTouchEvent()`中会通过`onInterceptTouchEvent()`尝试对输入事件进行截获

## 4.3 ViewGroup.dispatchPointerEvent()

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
		...........省略代码..............

        boolean handled = false;
		//同样的会对遮盖状态进行检查和过滤
        if (onFilterTouchEventForSecurity(ev)) {
            final int action = ev.getAction();
			//获取实际的动作,不包含触控点的索引号
            final int actionMasked = action & MotionEvent.ACTION_MASK;

            //处理一个初始化的down
            if (actionMasked == MotionEvent.ACTION_DOWN) {
				// ACTION_DOWN表示一条新的事件序列的开始,这是会重置一切状态,包括清空TouchTarget列表
                cancelAndClearTouchTargets(ev);
                resetTouchState();
            }

            // 判断是否有拦截
            final boolean intercepted;
			// 事件动作为DOWN, 或者事件序列已经确定派发目标
            if (actionMasked == MotionEvent.ACTION_DOWN
                    || mFirstTouchTarget != null) {
				// 判断是否禁止拦截
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

            // Update list of touch targets for pointer down, if needed.
            final boolean split = (mGroupFlags & FLAG_SPLIT_MOTION_EVENTS) != 0;
            TouchTarget newTouchTarget = null;
            boolean alreadyDispatchedToNewTouchTarget = false;
            if (!canceled && !intercepted) {

                // If the event is targeting accessiiblity focus we give it to the
                // view that has accessibility focus and if it does not handle it
                // we clear the flag and dispatch the event to all children as usual.
                // We are looking up the accessibility focused host to avoid keeping
                // state since these events are very rare.
                View childWithAccessibilityFocus = ev.isTargetAccessibilityFocus()
                        ? findChildWithAccessibilityFocus() : null;

                if (actionMasked == MotionEvent.ACTION_DOWN
                        || (split && actionMasked == MotionEvent.ACTION_POINTER_DOWN)
                        || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                    final int actionIndex = ev.getActionIndex(); // always 0 for down
                    final int idBitsToAssign = split ? 1 << ev.getPointerId(actionIndex)
                            : TouchTarget.ALL_POINTER_IDS;

                    // Clean up earlier touch targets for this pointer id in case they
                    // have become out of sync.
                    removePointersFromTouchTargets(idBitsToAssign);

                    final int childrenCount = mChildrenCount;
                    if (newTouchTarget == null && childrenCount != 0) {
                        final float x = ev.getX(actionIndex);
                        final float y = ev.getY(actionIndex);
                        // Find a child that can receive the event.
                        // Scan children from front to back.
                        final ArrayList<View> preorderedList = buildTouchDispatchChildList();
                        final boolean customOrder = preorderedList == null
                                && isChildrenDrawingOrderEnabled();
                        final View[] children = mChildren;
                        for (int i = childrenCount - 1; i >= 0; i--) {
                            final int childIndex = getAndVerifyPreorderedIndex(
                                    childrenCount, i, customOrder);
                            final View child = getAndVerifyPreorderedView(
                                    preorderedList, children, childIndex);

                            // If there is a view that has accessibility focus we want it
                            // to get the event first and if not handled we will perform a
                            // normal dispatch. We may do a double iteration but this is
                            // safer given the timeframe.
                            if (childWithAccessibilityFocus != null) {
                                if (childWithAccessibilityFocus != child) {
                                    continue;
                                }
                                childWithAccessibilityFocus = null;
                                i = childrenCount - 1;
                            }

                            if (!canViewReceivePointerEvents(child)
                                    || !isTransformedTouchPointInView(x, y, child, null)) {
                                ev.setTargetAccessibilityFocus(false);
                                continue;
                            }

                            newTouchTarget = getTouchTarget(child);
                            if (newTouchTarget != null) {
                                // Child is already receiving touch within its bounds.
                                // Give it the new pointer in addition to the ones it is handling.
                                newTouchTarget.pointerIdBits |= idBitsToAssign;
                                break;
                            }

                            resetCancelNextUpFlag(child);
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
                                newTouchTarget = addTouchTarget(child, idBitsToAssign);
                                alreadyDispatchedToNewTouchTarget = true;
                                break;
                            }

                            // The accessibility focus didn't handle the event, so clear
                            // the flag and do a normal dispatch to all children.
                            ev.setTargetAccessibilityFocus(false);
                        }
                        if (preorderedList != null) preorderedList.clear();
                    }

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

            // Dispatch to touch targets.
            if (mFirstTouchTarget == null) {
                // No touch targets so treat this as an ordinary view.
                handled = dispatchTransformedTouchEvent(ev, canceled, null,
                        TouchTarget.ALL_POINTER_IDS);
            } else {
                // Dispatch to touch targets, excluding the new touch target if we already
                // dispatched to it.  Cancel touch targets if necessary.
                TouchTarget predecessor = null;
                TouchTarget target = mFirstTouchTarget;
                while (target != null) {
                    final TouchTarget next = target.next;
                    if (alreadyDispatchedToNewTouchTarget && target == newTouchTarget) {
                        handled = true;
                    } else {
                        final boolean cancelChild = resetCancelNextUpFlag(target.child)
                                || intercepted;
                        if (dispatchTransformedTouchEvent(ev, cancelChild,
                                target.child, target.pointerIdBits)) {
                            handled = true;
                        }
                        if (cancelChild) {
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

            // Update list of touch targets for pointer up or cancel, if needed.
            if (canceled
                    || actionMasked == MotionEvent.ACTION_UP
                    || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                resetTouchState();
            } else if (split && actionMasked == MotionEvent.ACTION_POINTER_UP) {
                final int actionIndex = ev.getActionIndex();
                final int idBitsToRemove = 1 << ev.getPointerId(actionIndex);
                removePointersFromTouchTargets(idBitsToRemove);
            }
        }

        if (!handled && mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onUnhandledEvent(ev, 1);
        }
        return handled;
    }