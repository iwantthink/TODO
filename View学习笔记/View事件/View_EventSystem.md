# View的事件分发机制

[View中事件的派发.md]()

[视图创建过程.md]()

[Android开发艺术探索]()

[ViewRootImpl输入事件的派发.md]()

# 1. 简介

点击事件的事件分发,其实就是对`MotionEvent`事件的分发过程,即当一个`MotionEvent`产生了之后,系统需要把这个事件传递给一个具体的View,而这个传递过程就是分发过程

点击事件的分发过程由三个非常重要的方法共同完成,`dispatchTouchEvent()`,`onInterceptTouchEvent()`,`onTouchEvent()`

- `[ViewRootImpl输入事件的派发.md]()`分析了一个点击事件在`ViewRootImpl`中的分发过程

- `[View中事件的派发.md]()`分析了点击事件在`View`中的分发过程

## 1.1 dispatchTouchEvent()

用来进行事件的分发。如果事件能够传递给当前view，那么此方法就一定会调用（所以我们可能在这里处理一些一定能执行到的逻辑）

- 返回结果受到当前View的`onTouchEvent()`和下级View的`dispatchTouchEvent()`方法的影响，表示是否消耗当前事件。

- `View`和`ViewGroup`中都有各自的实现

	1. `View`主要负责事件接收与处理

	2. `ViewGroup`主要负责将触摸事件沿着控件树向子控件进行派发

## 1.2 onInterceptTouchEvent()

在`dispatchTouchEvent()`方法中被调用，用来判断是否拦截某个事件，如果当前View拦截了某个事件，那么在同一个事件序列中，当前方法不会再次被调用，返回结果表示是否拦截当前事件。

- **该方法仅存在于`ViewGroup`**

## 1.3 onTouchEvent()

在`dispatchTouchEvent()`方法中调用，用来处理点击事件，返回结果表示是否消耗当前事件，如果不消耗，则在同一个事件序列中，当前view无法再次接收到事件。

- 该方法仅存在于`View`中

## 1.4 表示逻辑的伪代码

下面用一段伪代码表示的逻辑(ViewGroup的逻辑)：

	public boolean dispatchTouchEvent(MotionEvent ev){

		boolean consume = false;

		if(onInterceptTouchEvent(ev))

			consume = onTouchEvent(ev);

		else

			consume = child.dispatchTouchEvent(ev);

		return consume;
	}

1. 对于一个根`ViewGroup`来说,点击事件产生后,首先就会传递给他,这时它的`dispatchTouchEvent()`就会被调用

2. 如果这个`ViewGroup`的`onInterceptTouchEvent()`方法返回true,就表示它要拦截当前事件,那么事件就会被交给这个`ViewGroup`来处理(即它的`onTouchEvent()`被调用,这发生在`View.dispatchTouchEvent()`中)

	`onInterceptTouchEvent()`返回true,那么`dispatchTouchEvent()`的查找派发目标部分就执行不到,导致`mFirstTouchTarget`为空,直接将事件交给`ViewGroup`自身去处理

3. 如果这个`ViewGroup`的`onInterceptTouchEvent()`返回false,表示它不拦截当前事件,这时当前事件就会继续往其子类传递,即子类的`dispatchTouchEvent()`被调用,如此反复直至事件被处理


- **从这里可以看出,实际上`onInterceptTouchEvent()`是优先于 回调事件的!一旦`ViewGroup`决定拦截事件,那么 回调事件就都不会被执行到**
	

# 2. View中Listener的优先级

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



# 3 View中的事件传递顺序

从[ViewRootImpl输入事件的派发.md]()中可以知道,会事件传递给`DecorView`进行处理


## 3.1 DecorView中的逻辑

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
		
        final Window.Callback cb = mWindow.getCallback();
		// mFeatureID<0 表示DecorView是一个顶级窗口的控件树的根控件
        return cb != null && !mWindow.isDestroyed() && mFeatureId < 0
                ? cb.dispatchTouchEvent(ev) : super.dispatchTouchEvent(ev);
    }

- 在`DecorView`中,会将事件优先转交给 实现了`Callback`接口的对象

- `mWindow`实际类型是`PhoneWindow`,其是`Window`的实现类

	在`Activity.attach()`方法中,调用了`Window.setCallback()` 将`Activity`作为`Window.Callback`的实现类传入了其中

	**因此,事件经过DecorView之后,会优先交给`Activity`去进行处理**

## 3.2 Activity中的逻辑

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			// 开发者重写此方法,获取`ACTION_DOWN`事件提醒
            onUserInteraction();
        }
		//判断`Window`是否会将事件消耗,实际上是交给了`DecorView`
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
		// 如果DecorView不处理事件,那么这时才会调用Activity自身的onTouchEvent()
        return onTouchEvent(ev);
    }

- `getWindow()`获取到的是`PhoneWindow`,**所以这里又将事件转交给了`Window`去处理**

## 3.3 PhoneWindow中的逻辑

    @Override
    public boolean superDispatchTouchEvent(MotionEvent event) {
        return mDecor.superDispatchTouchEvent(event);
    }

- `mDecor`即`DecorView`

## 3.4 DecorView中的逻辑

    public boolean superDispatchTouchEvent(MotionEvent event) {
		// 按照常规的事件处理方式去处理
        return super.dispatchTouchEvent(event);
    }

- 到这里发现最终事件又交回给了`DecorView`去处理


## 3.5 总结

当一个点击事件产生之后，它的传递过程遵循如下顺序:DecorView->Activity->Window->DecorView

- 事件总是先传递给`DecorView`,但是`DecorView`不处理，其直接将事件转交给`Activity`去处理

- `Activity`中,将事件再传递给`Window`，而`Window`也不会去真正处理,而是再传递给顶级View(DecorView)。`DecorView`接收到事件后，就会按照普通的事件分发机制去分发事件。

顶级View通常是一个Viewgroup，如果ViewGroup的`onInterceptTouchEvent()`返回true，则事件由ViewGroup处理，这时如果viewGroup的mOnTouchListener被设置，则onTouch会被调用，否则的话onTouchEvent会被调用。也就是说onTouch会屏蔽掉`onTouchEvent()`。另外在`onTouchEvent()`中，如果设置了`mOnClickListener`，则`onClick()`会被调用，如果顶级ViewGroup不拦截事件，事件就会传递给它所在的点击事件链上的子view，子view的dispatchTouchEvent会被调用


如果一个View的`onTouchEvent()`返回false，那么它的父容器的`onTouchEvent()`将会被调用，以此类推。如果所有的元素都不处理这个事件，那么这个事件将会最终传递给Activity处理，即Activity的onTouchEvent方法会被调用。

# 4. 事件传递的一些结论：

## 4.1 第一条

同一个事件序列是指从手指接触屏幕开始，到手指离开屏幕那一刻结束，在这个过程当中所产生的一系列事件，这个事件序列以`ACTION_DOWN`事件开始，中间含有数量不定的`ACTION_MOVE`事件，最终以`ACTION_UP`事件结束。

## 4.2 第二条

正常情况下，一个事件序列只能被一个View拦截且消耗，这一条可以参考（3），因为一旦一个元素拦截了某个事件，那么同一个事件序列中的所有事件都会直接交给它处理，因此同一个事件序列中的事件不能分别由俩个View同时处理，但是通过特殊的手段也是可以实现的，例如一个View将本该自己处理的事件通过onTouchEvent强行传递给其他view进行处理。

- `TouchTarget`仅在`ACTION_DOWN`,`ACTION_POINTER_DOWN`等情况时被创建(即一个事件序列的开始时)

- 一旦一个`View`决定拦截,并消耗.  那么便会创建一个`TouchTarget` 去保存事件和View的关系,那么一个事件序列接下来的事件都会交给该`View`处理

## 4.3 第三条

某个View（得是Viewgroup，参考第七条）一旦决定拦截，那个这一个事件序列都会交给它来处理（如果事件序列能够传递给它），并且它的onTnterceptTouchEvent不会再被调用。这条也很好理解，就是说当一个view决定拦截一个事件后，那么系统会把同一个事件序列内的其他方法都直接交给它处理，因此就不用在调用这个view的onInterceptTouchEvent去询问它是否要拦截了。

如下ViewGroup中的源码：
  
ViewGroup会在俩种情况下去判断是否需要拦截当前事件

	1. MotionEvent.ACTION_DOWN
	
	2. mFirstTouchTarget!=null

当事件由ViewGroup的子元素成功处理时，`mFirstTouchTarget`会被赋值并指向子元素。反之一旦事件由当前viewGroup拦截时，`mFirstTouchTarget`就不成立了。所以一旦事件被ViewGroup拦截，跟在DOWN事件后面的MOVE 和UP 事件 到来时，不会再去判断onInterceptTouchEvent。

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


这里有一种特殊情况，`FLAG_DISALLOW_INTERCEPT`标志位，通过`requestDisallowInterceptTouchEvent()`方法设置后，ViewGroup无法拦截除了`ACTION_DOWN` 之外的事件，因为如果是`ACTION_DOWN`这个事件会重置`FLAG_DISALLOW_INTERCEPT`这个标志位，将导致View中设置的这个标记位无效。因此当面对`ACTION_DOWN`时，ViewGroup总是会调用自己的`onInterceptTouchEvent()`方法来询问自己是否要拦截事件。
	     
	ViewGroup.dispatchTouchEvent()
	 // Handle an initial down.
	if (actionMasked == MotionEvent.ACTION_DOWN) {
		// Throw away all previous state when starting a new touch gesture.
		// The framework may have dropped the up or cancel event for the previous gesture
		// due to an app switch, ANR, or some other state change.
		cancelAndClearTouchTargets(ev);
		resetTouchState();
	}


## 4.4 第四条

某个view一旦开始处理事件，如果它不消耗`ACTION_DOWN`事件（onTouchEvent返回了false），那么同一个事件序列中的其他事件都不会再交给它来处理，并且事件将重新交由它的父元素去处理，即父元素的onTouchEvent会被调用。意思就是说，如果事件交给了一个view来处理，那么它就一定要消耗掉，否则接下来同一个事件序列中剩下的事件就不再交给它处理了。

已经知道`mFirstTouchTarget`在ViewGroup中的子元素成功处理之后会被赋值，那么子元素没有或者子元素都不处理。这俩种情况下，ViewGroup会自己处理点击事件。注意到`dispatchTransformedTouchEvent()`的第三个参数为null，那么会调用父元素的事件分发。

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


## 4.5 第五条

如果View不消耗除ACTION_DOWN以外的其他事件，那么这个点击事件会消失，此时父元素的onTouchEvent并不会被调用，并且当前View可以持续收到后续的事件，最终这些消失的点击事件会传递给Activity处理。

## 4.6 第六条

ViewGroup默认不拦截任何事件！Android源码中ViewGroup的`onInterceptTouchEvent()`方法默认返回的是false。

下面是ViewGroup中的源码：

	public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

- 高版本中 条件有所变换,但是与之类似,正常情况下都是返回false

## 4.7 第七条

View没有`onInterceptTouchEvent()`方法，一旦有点击事件传递给它，那么它的`onTouchEvent()`方法就会被调用。

## 4.8 第八条

View的`onTouchEvent()`默认都会消耗事件（返回true），除非它是不可点击的（clickable和longclickable同时都为false）。View的longClickable属性默认都为false，clickable属性要分情况，比如button的clickable默认为true，而textview的clickable属性默认为false。


## 4.9 第九条

View的enable属性不影响onTouchEvent的默认返回值。哪怕一个view是disable状态的，只要它的clickable和longclickable 有一个为true，那么它的onTouchEvent就返回true。

## 4.10 第十条

onClick会发生的前提是当前的view可点击，并且它收到了down和up的事件。

## 4.11 第十一条

事件传递过程是由外向内的，即事件总是先传递给父元素，然后再由父元素分发给子view，通过requestDisallowInterceptTouchEvent方法可以在子元素中干预父元素的事件分发过程，但是ACTION_DOWN事件除外。



# 5. 滑动冲突

**滑动冲突的场景**:

1. 场景1：

	**外部滑动和内部滑动方向不一致**

2. 场景2：

	**外部滑动和内部滑动方向一致**

3. 场景3：

	**以上俩种情况的嵌套**。

## 5.1 滑动冲突的处理规则：

1. **根据滑动的方向去判断由谁来拦截**

2. **根据具体的业务逻辑去判断**

## 5.2 解决方式：

### 5.2.1 外部拦截法
外部拦截法（推荐使用），只通过父控件去控制

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
					boolean parentNeedEvent = checkParentNeedEvent(ev)
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

- `checkParentNeedEvent()`是待实现的逻辑,去判断父类是否需要拦截事件

-  在`onInterceptTouchEvent()`方法中,`ACTION_DOWN`事件下发时,父容器必须返回false,因为这时如果父容器拦截了`ACTION_DOWN`,则这一事件序列都会交给父容器去处理,那么就没办法再传递给子元素了

- 其次是`ACTION_MOVE`事件,这个事件可以根据需求来决定是否拦截 

- 最后是`ACTION_UP`事件,这里必须返回false,因为`ACTION_UP`本身没有什么意义,但是对于子元素来说可能会有用处

	**特殊情况:**
	
	 - 假设事件交给子元素处理,但父容器在`ACTION_UP`返回了true,这就会导致子元素无法收到`ACTION_UP`事件.这个时候子元素中的`onClick()`事件就无法触发

		但是父容器比较特殊,一旦它开始拦截任何一个事件,那么后续的事件都会交给它来处理,即使`onInterceptTouchEvent()`方法在`ACTION_UP`时返回false
	


### 5.2.2 内部拦截法

内部拦截法父控件默认拦截任何事件，通过子控件的 `requestDisallowInterceptTouchEvent()`这个方法控制父控件不要去拦截）

- 子元素中：

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


- 父元素中:

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
