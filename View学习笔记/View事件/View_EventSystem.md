# View的事件分发机制

[View中事件的派发.md]()

[Android开发艺术探索]()

# 1. dispatchTouchEvent()

用来进行事件的分发。如果事件能够传递给当前view，那么此方法就一定会调用（所以我们可能在这里处理一些一定能执行到的逻辑）

- 返回结果受到当前view的`onTouchEvent()`和下级view的dispatchTouchEvent方法的影响，表示是否消耗当前事件。

- `View`和`ViewGroup`中都有各自的实现

	1. `View`主要负责事件接收与处理

	2. `ViewGroup`主要负责将触摸事件沿着控件树向子控件进行派发

# 2. onInterceptTouchEvent()

在`dispatchTouchEvent()`方法中被调用，用来判断是否拦截某个事件，如果当前View拦截了某个事件，那么在同一个事件序列中，当前方法不会再次被调用，返回结果表示是否拦截当前事件。

- **该方法仅存在于`ViewGroup`**

# 3. onTouchEvent()

在`dispatchTouchEvent()`方法中调用，用来处理点击事件，返回结果表示是否消耗当前事件，如果不消耗，则在同一个事件序列中，当前view无法再次接收到事件。

下面用一段伪代码表示的逻辑(ViewGroup的逻辑)：

	public boolean dispatchTouchEvent(MotionEvent ev){

		boolean consume = false;

		if(onInterceptTouchEvent(ev))

			consume = onTouchEvent(ev);

		else

			consume = child.dispatchTouchEvent(ev);

		return consume;
	}
	

# 4. View中Listener的优先级

当一个View需要处理事件时，如果它设置了`onTouchListener()`，那么`onTouchListener()`中的onTouch方法会被回调。这时事件如何处理还要看onTouch的返回值，如果返回false，那么当前view的onTouchEvent方法会被调用；如果返回true，那么onTouchEvent将不会再被调用！这样做的好处是方便在外界处理点击事件。


	if (li != null && li.mOnTouchListener != null
                    && (mViewFlags & ENABLED_MASK) == ENABLED
                    && li.mOnTouchListener.onTouch(this, event)) {
                result = true;
            }
	if (!result && onTouchEvent(event)) {
                result = true;
            }

- 在onTouchEvent方法中，如果当前设置有`onClickListener()`，那么它的onClick方法会被调用。



# 5. View中的事件传递顺序

当一个点击事件产生之后，它的传递过程遵循如下顺序:Activity->Window->View，即事件总是先传递给Activity，Activity再传递给Window，最后Window再传递给顶级View。顶级View接收到事件后，就会按照事件分发机制去分发事件。

顶级View通常是一个Viewgroup，如果ViewGroup的onInterceptTouchEvent返回true，则事件由ViewGroup处理，这时如果viewGroup的mOnTouchListener被设置，则onTouch会被调用，否则的话onTouchEvent会被调用。也就是说onTouch会屏蔽掉onTouchEvent。另外在onTouchEvent中，如果设置了mOnClickListener，则onClick会被调用，如果顶级viewGroup不拦截事件，事件就会传递给它所在的点击事件链上的子view，子view的dispatchTouchEvent会被调用


如果一个View的onTouchEvent返回false，那么它的父容器的onTouchEvent将会被调用，以此类推。如果所有的元素都不处理这个事件，那么这个事件将会最终传递给Activity处理，即Activity的onTouchEvent方法会被调用。

# 6. 事件传递的一些结论：

## 6.1 第一条

同一个事件序列是指从手指接触屏幕开始，到手指离开屏幕那一刻结束，在这个过程当中所产生的一系列事件，这个事件序列以down事件开始，中间含有数量不定的move事件，最终以up事件结束。

## 6.2 第二条

正常情况下，一个事件序列只能被一个View拦截且消耗，这一条可以参考（3），因为一旦一个元素拦截了某个事件，那么同一个事件序列中的所有事件都会直接交给它处理，因此同一个事件序列中的事件不能分别由俩个View同时处理，但是通过特殊的手段也是可以实现的，例如一个view将本该自己处理的事件通过onTouchEvent强行传递给其他view进行处理。

## 6.3 第三条

某个View（得是Viewgroup，参考第七条）一旦决定拦截，那个这一个事件序列都会交给它来处理（如果事件序列能够传递给它），并且它的onTnterceptTouchEvent不会再被调用。这条也很好理解，就是说当一个view决定拦截一个事件后，那么系统会把同一个事件序列内的其他方法都直接交给它处理，因此就不用在调用这个view的onInterceptTouchEvent去询问它是否要拦截了。

如下ViewGroup中的源码：
  
ViewGroup会在俩种情况下去判断是否需要拦截当前事件

	1.MotionEvent.ACTION_DOWN
	
	2.mFirstTouchTarget!=null

当事件由viewGroup的子元素成功处理时，mFirstTouchTarget会被赋值并指向子元素。反之一旦事件由当前viewGroup拦截时，mFirstTouchTarget就不成立了。所以一旦事件被viewGroup拦截，跟在DOWN事件后面的MOVE 和UP 事件 到来时，不会再去判断onInterceptTouchEvent。

		 // Check for interception.
            final boolean intercepted;
            if (actionMasked == MotionEvent.ACTION_DOWN
                    || mFirstTouchTarget != null) {
                final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
                if (!disallowIntercept) {
                    intercepted = onInterceptTouchEvent(ev);
                    ev.setAction(action); // restore action in case it was changed
                } else {
                    intercepted = false;
                }
            } else {
                // There are no touch targets and this action is not an initial down
                // so this view group continues to intercept touches.
                intercepted = true;
            }


这里有一种特殊情况，FLAG\_DISALLOW\_INTERCEPT标志位，通过requestDisallowInterceptTouchEvent方法设置后，ViewGroup无法拦截除了ACTION\_DOWN 之外的事件，因为如果是ACTION\_DOWN这个事件会重置FLAG\_DISALLOW\_INTERCEPT这个标志位，将导致View中设置的这个标记位无效。因此当面对ACTION\_DOWN时，ViewGroup总是会调用自己的onInterceptTouchEvent方法来询问自己是否要拦截事件。
	     

	 // Handle an initial down.
            if (actionMasked == MotionEvent.ACTION_DOWN) {
                // Throw away all previous state when starting a new touch gesture.
                // The framework may have dropped the up or cancel event for the previous gesture
                // due to an app switch, ANR, or some other state change.
                cancelAndClearTouchTargets(ev);
                resetTouchState();
            }


## 6.4 第四条

某个view一旦开始处理事件，如果它不消耗`ACTION_DOWN`事件（onTouchEvent返回了false），那么同一个事件序列中的其他事件都不会再交给它来处理，并且事件将重新交由它的父元素去处理，即父元素的onTouchEvent会被调用。意思就是说，如果事件交给了一个view来处理，那么它就一定要消耗掉，否则接下来同一个事件序列中剩下的事件就不再交给它处理了。

已经知道mFirstTouchTarget在ViewGroup中的子元素成功处理之后会被赋值，那么子元素没有或者子元素都不处理。这俩种情况下，ViewGroup会自己处理点击事件。注意到dispatchTransformedTouchEvent的第三个参数为null，那么会调用父元素的事件分发。

	  // Dispatch to touch targets.
            if (mFirstTouchTarget == null) {
                // No touch targets so treat this as an ordinary view.
                handled = dispatchTransformedTouchEvent(ev, canceled, null,
                        TouchTarget.ALL_POINTER_IDS);
            }


	 	if (child == null) {
                handled = super.dispatchTouchEvent(event);
            } else {
                handled = child.dispatchTouchEvent(event);
            }


## 6.5 第五条

如果view不消耗除ACTION_DOWN以外的其他事件，那么这个点击事件会消失，此时父元素的onTouchEvent并不会被调用，并且当前view可以持续收到后续的事件，最终这些消失的点击事件会传递给Activity处理。

## 6.6 第六条

ViewGroup默认不拦截任何事件！Android源码中ViewGroup的onInterceptTouchEvent方法默认返回的是false。

下面是ViewGroup中的源码：

	    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

## 6.7 第七条

View没有onInterceptTouchEvent方法，一旦有点击事件传递给它，那么它的onTouchEvent方法就会被调用。

## 6.8 第八条

View的onTouchEvent默认都会消耗事件（返回true），除非它是不可点击的（clickable和longclickable同时都为false）。View的longClickable属性默认都为false，clickable属性要分情况，比如button的clickable默认为true，而textview的clickable属性默认为false。



## 6.9 第九条

View的enable属性不影响onTouchEvent的默认返回值。哪怕一个view是disable状态的，只要它的clickable和longclickable 有一个为true，那么它的onTouchEvent就返回true。

## 6.10 第十条

onClick会发生的前提是当前的view可点击，并且它收到了down和up的事件。

## 6.11 第十一条

事件传递过程是由外向内的，即事件总是先传递给父元素，然后再由父元素分发给子view，通过requestDisallowInterceptTouchEvent方法可以在子元素中干预父元素的事件分发过程，但是ACTION_DOWN事件除外。



# 7. 滑动冲突

解决方法:

1. 场景1：外部滑动和内部滑动方向不一致

2. 场景2：外部滑动和内部滑动方向一致

3. 场景3：以上俩种情况的嵌套。

## 7.1 滑动冲突的处理规则：

1. 根据滑动的方向去判断由谁来拦截

2. 根据业务逻辑去判断

## 7.2 解决方式：

1. 外部拦截法（推荐使用），只通过父控件去控制。

	 	private int mLastX;
		private int mLastY;
	
		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
	
	    boolean intercept = false;
	
	    int x = (int) ev.getX();
	
	    int y = (int) ev.getY();
	
	    switch (ev.getAction()) {
	
	        case MotionEvent.ACTION_DOWN:
	
	            intercept = true;
	
	            break;
	
	        case MotionEvent.ACTION_MOVE:
	
	            if (parentNeedEvent) {
	                intercept = true;
	            } else {
	
	                intercept = false;
	            }
	
	            break;
	
	        case MotionEvent.ACTION_UP:
	
	            intercept = false;
	            break;
	
	        default:
	            break;
	
	    }
	
	    mLastX = x;
	
	    mLastY = y;
	
	    return intercept;
		}

2. 内部拦截法父控件默认拦截任何事件，通过子控件的 `requestDisallowInterceptTouchEvent()`这个方法控制父控件不要去拦截）

	子元素中：

	 	private int mLastX;
		private int mLastY;
	
		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
	
	    int x = (int) ev.getX();
	
	    int y = (int) ev.getY();
	
	    switch (ev.getAction()) {
	
	        case MotionEvent.ACTION_DOWN:
	
	            parent.requestDisallowInterceptTouchEvent();
	
	            break;
	
	        case MotionEvent.ACTION_MOVE:
	
	            int deltaX = x - mLastX;
	            int deltaY = y - mLastY;
	
	            if(parentNeed){
	               parent.requestDiallowInterceptTouchEvent();
	               }
	
	            break;
	
	        case MotionEvent.ACTION_UP:
	
	            break;
	
	        default:
	            break;
	
	    }
	
	    mLastX = x;
	
	    mLastY = y;
	
	    return super.dispatchTouchEvent(event);
		}

	父元素中:

	     @Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
	    
	    int action = ev.getAction();
	    
	    if (action == MotionEvent.ACTION_DOWN)
	    {
	        
	        return false;
	    }else {
	        
	        return true;
	    }
	    
		}
