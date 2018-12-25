# View事件,硬件加速

view是什么呢？它是Android中所有控件的基类，可以理解为所有控件的抽象，同时它有一个子类ViewGroup，代表一组view，个人觉得可以理解为布局，那么就代表了控件可以是单个的也可以是一组的。


# view基础知识

1. View的位置参数

2. MotionEvent

3. TouchSlop

4. VelocityTracker

5. GestureDetector

6. View的滑动

7. View的生命周期

# 1.View的位置参数
想要学习 view，首先就得了解Android的坐标体系，android中的坐标原点通常是屏幕的左上角，然后x轴正方向水平向右，Y轴正方向水平向下。如下图：

View在屏幕的位置 可以用`left top right bottom`来表示，看一下view源码中对left的定义，其他的都类似。

- **需要注意的是这里的坐标都是相对坐标，它是当前view相对于它的父容器的坐标。**

	**当前View的左边缘距离它的父容器的左边缘的距离**

  

	/**
     * The distance in pixels from the left edge of this view's parent
     * to the left edge of this view.
     * {@hide}
     */
    @ViewDebug.ExportedProperty(category = "layout")
    protected int mLeft;


View的宽高就是通过这四个参数计算出来的

	width = right - left;
	height = bottom - top

另外在3.0开始，View增加了`x,y,translationX,translationY`
x和y分别代表View左上角的坐标，translationX/Y代表各自方向上的位移（默认为0）

	x = left + translationX;
	
	y = top +translationY;


需要注意的是在view平移的过程中，left/top是不会改变的，改变的是x,y,translationX,translationY，在属性动画中改变的其实也是translationX/Y这俩个值。

# 2.MotionEvent

android将事件信息封装成这个类，然后提供给开发者使用，开发者可以通过这个类来得到坐标信息和触摸的动作

## 2.1 主要的事件类型有:

1. `ACTION_DOWN`: 表示用户开始触摸.

2. `ACTION_MOVE`: 表示用户在移动(手指或者其他)

3. `ACTION_U`P:表示用户抬起了手指

4. `ACTION_CANCEL`:表示手势被取消了

	一些关于这个事件类型的讨论见:  
	[what-causes-a-motionevent-action-cancel-in-android](http://stackoverflow.com/questions/11960861/what-causes-a-motionevent-action-cancel-in-android)

	>**触发条件**:上层 View 是一个 RecyclerView，它收到了一个 ACTION_DOWN 事件，由于这个可能是点击事件，所以它先传递给对应 ItemView，询问 ItemView 是否需要这个事件，然而接下来又传递过来了一个 ACTION\_MOVE 事件，且移动的方向和 RecyclerView 的可滑动方向一致，所以 RecyclerView 判断这个事件是滚动事件，于是要收回事件处理权，这时候对应的 ItemView 会收到一个 ACTION\_CANCEL ，并且不会再收到后续事件。

5.  `ACTION_OUTSIDE`: 表示用户触碰超出了正常的UI边界.(这个不常见)
	
	[参考:OUTSIDE_ACTION](https://stackoverflow.com/questions/8384067/how-to-dismiss-the-dialog-with-click-on-outside-of-the-dialog)  

	>设置视图的 WindowManager 布局参数的 flags为FLAG\_WATCH\_OUTSIDE\_TOUCH，这样点击事件发生在这个视图之外时，该视图就可以接收到一个 ACTION\_OUTSIDE 事件。

6. `ACTION_POINTER_DOWN`:有一个非主要的手指按下了.

7. `ACTION_POINTER_UP`:一个非主要的手指抬起来了

## 2.2 通过getX,getY，getRawX,getRawY来获取坐标

- 前者获取的是**相对于当前View的左上角的坐标**

- 后者获取的是**相对于手机屏幕左上角的坐标**。

也可以理解为一个获取手指落在View上的坐标，一个获取手机落在屏幕上的坐标。

## 2.3 多点触控 
 
- `getActionMasked()`:	与 `getAction()` 类似，多点触控必须使用这个方法获取事件类型。 

- `getActionIndex()`:	获取该事件是哪个指针(手指)产生的。  

- `getPointerCount()`:	获取在屏幕上手指的个数。  

- `getPointerId(int pointerIndex)`:	获取一个指针(手指)的唯一标识符ID，在手指按下和抬起之间ID始终不变。  

- `findPointerIndex(int pointerId)`:	通过PointerId获取到当前状态下PointIndex，之后通过PointIndex获取其他内容。  

- `getX(int pointerIndex)`:	获取某一个指针(手指)的X坐标  

- `getY(int pointerIndex)`:	获取某一个指针(手指)的Y坐标  



### 2.3.1 getAction 和getActionMasked  

多点触控必须使用`getActionMasked()`方法获取事件类型  

单点触控`getAction()`和`getActionMasked()`都可以  

- 使用`getActionIndex()`可以获取到这个index数值，但是这个方法只在DOWN和UP时有效，MOVE时无效 。   

- **追踪事件流，使用`PointId`！**


### 2.3.2 历史数据

`getHistorySize()`:	获取历史事件集合大小  

`getHistoricalX(int pos)`:	获取第pos个历史事件x坐标  
(pos < getHistorySize())  

`getHistoricalY(int pos)`:	获取第pos个历史事件y坐标
(pos < getHistorySize())

`getHistoricalX (int pin, int pos)`:	获取第pin个手指的第pos个历史事件x坐标
(pin < getPointerCount(), pos < getHistorySize() )

`getHistoricalY (int pin, int pos)`:	获取第pin个手指的第pos个历史事件y坐标
(pin < getPointerCount(), pos < getHistorySize() )


**历史数据只有ACTION_MOVE事件**

**历史数据 单点触控 多点触控都可以用**


### 2.3.3 获取事件发生的时间  

- `getDownTime()`:	获取手指按下时的时间。 单位:毫秒

- `getEventTime()`:	获取当前事件发生的时间。

- `getHistoricalEventTime(int pos)`:	获取历史事件发生的时间。


### 2.3.4 获取压力(接触面积大小)  

- `getSize ()`:	获取第1个手指与屏幕接触面积的大小

- `getSize (int pin)`:	获取第pin个手指与屏幕接触面积的大小

- `getHistoricalSize (int pos)`:	获取历史数据中第1个手指在第pos次事件中的接触面积

- `getHistoricalSize (int pin, int pos)`:	获取历史数据中第pin个手指在第pos次事件中的接触面积

- `getPressure ()`:	获取第一个手指的压力大小

- `getPressure (int pin)`:	获取第pin个手指的压力大小

- `getHistoricalPressure (int pos)`:	获取历史数据中第1个手指在第pos次事件中的压力大小

- `getHistoricalPressure (int pin, int pos)`:	获取历史数据中第pin个手指在第pos次事件中的压力大小

获取接触面积大小和获取压力大小是需要硬件支持的    

大部分设备所使用的电容屏不支持压力检测，但能够大致检测接触面积 
 
`getPressure()` 是使用接触面积来模拟的  


### 2.3.5 鼠标事件  

- `ACTION_HOVER_ENTER`:	指针移入到窗口或者View区域，但没有按下。

- `ACTION_HOVER_MOVE`:	指针在窗口或者View区域移动，但没有按下。

- `ACTION_HOVER_EXIT`:	指针移出到窗口或者View区域，但没有按下。

- `ACTION_SCROLL`:	滚轮滚动，可以触发水平滚动(AXIS_HSCROLL)或者垂直滚动(AXIS_VSCROLL)

`android4.0 API-14 `才添加的

通过`getActionMasked()`获取  

`onGenericMotionEvent()` 获取，而不是`onTouchEvent()`

**输入设备类型判断：  **

- `TOOL_TYPE_ERASER`:	橡皮擦

- `TOOL_TYPE_FINGER`:	手指

- `TOOL_TYPE_MOUSE`:	鼠标

- `TOOL_TYPE_STYLUS`:	手写笔

- `TOOL_TYPE_UNKNOWN`:	未知类型


- **使用 `getToolType(int pointerIndex)` 来获取对应的输入设备类型，`pointIndex`可以为0，但必须小于 `getPointerCount()`**。


## 3.TouchSlop

`TouchSlop`的定义是：

- **系统所能识别出的被认为是滑动的最小距离**。

可以在代码中通过 `ViewConfiguration.get(getContext()).getScaledTouchSlop()`获取

源码中的位置：`frameworks/base/core/res/res/values/config.xml `

		/**
	     * Distance a touch can wander before we think the user is scrolling in dips.
	     * Note that this value defined here is only used as a fallback by legacy/misbehaving
	     * applications that do not provide a Context for determining density/configuration-dependent
	     * values.
	     *
	     * To alter this value, see the configuration resource config_viewConfigurationTouchSlop
	     * in frameworks/base/core/res/res/values/config.xml or the appropriate device resource overlay.
	     * It may be appropriate to tweak this on a device-specific basis in an overlay based on
	     * the characteristics of the touch panel and firmware.
     	 */
 	   private static final int TOUCH_SLOP = 8;


## 4.VelocityTracker

	Helper for tracking the velocity of touch events, for implementing
	flinging and other such gestures.

用来计算速度的一个帮助类，注意在获取到速度之前：


1. 必须先调用computeCurrentVelocity方法（传入的参数的时间单位是ms毫秒）

2. 这里速度的定义是 一段时间手指滑动过的像素数，注意速度是可以为负数的（配合android的坐标系），

	速度获取的公式:
	
	- **速度 = (终点位置 - 起点位置) /时间段**

3. 最后在使用完这个类之后，一定要记得使用

		velocityTracker.clear();
	
		velocityTracker.recycle();

附上代码： 
	

    private VelocityTracker mVelocityTracker = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        mVelocityTracker = VelocityTracker.obtain();

        mVelocityTracker.addMovement(event);

        mVelocityTracker.computeCurrentVelocity(1000);

        int xVelocity = (int) mVelocityTracker.getXVelocity();

        int yVelocity = (int) mVelocityTracker.getYVelocity();

        Log.d("VelocityTrackerView", "xVelocity:" + xVelocity);

        Log.d("VelocityTrackerView", "yVelocity:" + yVelocity);

        return true;
    }


## 5.GestureDetector

	Creates a GestureDetector with the supplied listener.
	
	You may only use this constructor from a UI thread (this is the usual situation).

手势检测，**只能在UI线程中创建**，用来辅助检测用户的单击，滑动，长按，双击等行为。通过这个类消耗了事件之后需要将结果返回。


**手势方法介绍**:

- `setIsLongpressEnabled`:	通过布尔值设置是否允许触发长按事件，true 表示允许，false 表示不允许。  

- `	isLongpressEnabled`:	判断当前是否允许触发长按事件，true 表示允许，false 表示不允许。  

- `	onTouchEvent`:	这个是其中一个重要的方法，在最开始已经演示过使用方式了。  

- `	onGenericMotionEvent`:	这个是在 API 23 之后才添加的内容，主要是为 OnContextClickListener 服务的，暂时不用关注。  

- `	setContextClickListener`:	设置 ContextClickListener 。  

- `	setOnDoubleTapListener`:	设置 OnDoubleTapListener 。  


**使用方法**：

- 创建`GestureDetector()`,并且实现`OnGestureListener`接口

	注意有个小bug的解决,在View的`onTouchEvent()`中，将事件传递给`GestureDetector` 来进行处理，但是如果我在`onSingleTapUp`中想要消耗这个事件，返回true这个值 没有起到效果，看一下log

	    @Override
	    public boolean onTouchEvent(MotionEvent event) {
	
	        boolean resume = mGestureDetector.onTouchEvent(event);
	
	        Log.d("VelocityTrackerView", "resume:" + resume);
	
	        return resume;
	    }

		>08-14 10:32:01.109    9925-9925/com.www.example D/VelocityTrackerView﹕ resume:false
		
		可以看到这时候返回的resume并不是预期中的true.然后我们将onTouchEvent中的值手动设置为true之后，看一下log的值
		>08-14 10:37:18.019  12347-12347/com.www.example D/VelocityTrackerView﹕ resume:false
	
		>08-14 10:37:18.109  12347-12347/com.www.example D/VelocityTrackerView﹕ onSingleTapUp
	
		>08-14 10:37:18.109  12347-12347/com.www.example D/VelocityTrackerView﹕ resume:true

	我的猜测是：onSingleTapUp只是消耗`ACTION_UP`这个事件，但是一个事件肯定是从`ACTION_DOWN`开始，然后有零个或者多个ACTION_MOVE，最后是`ACTION_UP`结束。

	然而在`ACTION_DOWN`的时候，我们并没有消耗它，所以返回了false，那么接下来同一个事件序列中的事件就都不会给到当前view了。我的解决办法是，如果你需要用到`onSingleTapUp`这一类的方法，那么就将`onDown（）`这个回调中返回true，或者你明确所有的事件都要view来解决 可以在`onTouchEvent()`中直接返回true。

## 5.1 示例

	private GestureDetector mGestureDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mGestureDetector = new GestureDetector(this);
        //解决长按屏幕后无法拖动的现象
        mGestureDetector.setIsLongpressEnabled(false);

        boolean resume = mGestureDetector.onTouchEvent(event);

        return resume;
    }

	 /**
     * 手指轻轻触摸屏幕的一瞬间，由一个ACTION_DOWN触发
     */
	@Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

 	/**
     * 手指轻轻触摸屏幕，尚未松开或者拖动，由一个ACTION_DOWN触发
     * 注意和onDown（）的区别，它强调没有松开或者拖动的状态
     */
    @Override
    public void onShowPress(MotionEvent e) {

    }

	/**
     * 手指（轻轻触摸屏幕后）松开,伴随一个MotionEvent.ACTION_UP而触发，这是单击行为
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

	/**
     *  手指按下屏幕并拖动,由一个ACTION_DOWN,多个ACTION_MOVE触发，这是拖动行为
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

	/**
     * 用户长久按着屏幕不放，即长按动作
     */
    @Override
    public void onLongPress(MotionEvent e) {

    }

 	 /**
     * 用户按下触摸屏,快速滑动后松开,由一个ACTION_DOWN,多个ACTION_MOVE和一个ACTION_UP触发，这是快速滑动行为
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }



上面已经可以实现了一些基础的方法，如果要使用双击这个回调还得去实现`OnDoubleTapListener()`接口中的方法

	mGestureDetector.setOnDoubleTapListener(this);


    /**
     * 严格的单击行为
     *  注意它和onSingleTapUp的区别，如果触发了onSingleTapConfirmed,那么后面
     *  不可能再紧跟另一个单击行为，即这只可能是单击，而不可能是双击中的一次单击
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

   	/**
     * 双击,由2次连续的组成，它不可能和onSingleTapConfirmed共存
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    /**
     * 表示发生了双击行为，在双击的期间，ACTION_DOWN,ACTION_MOVE，ACTION_UP都会触发此回调
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
 



**建议**：

- 如果只是监听滑动相关的，建议自己在`onTouchEvent()`中实现，如果要监听双击这种行为的话，就使用GestureDetector


## 5.2 构造函数

	GestureDetector(Context context, GestureDetector.OnGestureListener listener)  
	GestureDetector(Context context, GestureDetector.OnGestureListener listener, Handler handler)

- 第二种构造方法，需要一个Handler  主要是需要一个`looper`。

	默认的`GestureDetector` 是要在主线程中创建的，它内部创建的Handler会自动取获取主线程的looper。 重点在于Looper，也可以手动调用Looper.prepare()


## 5.3 手势监听器回调

- `OnContextClickListener()`:	

	这个很容易让人联想到ContextMenu，然而它和ContextMenu并没有什么关系，它是在Android6.0(API 23)才添加的一个选项，是用于检测外部设备上的按钮是否按下的，例如蓝牙触控笔上的按钮，一般情况下，忽略即可。  

- `OnDoubleTapListener`:	

	双击事件，有三个回调类型：双击(DoubleTap)、单击确认(SingleTapConfirmed) 和 双击事件回调(DoubleTapEvent)   

- `OnGestureListener`:	

	手势检测，主要有以下类型事件：按下(Down)、 一扔(Fling)、长按(LongPress)、滚动(Scroll)、触摸反馈(ShowPress) 和 单击抬起(SingleTapUp)   

- `SimpleOnGestureListener`:	

	这个是上述三个接口的空实现，一般情况下使用这个比较多，也比较方便。

### 5.3.1 onContextClickListener

`OnContextClickListener()` 主要是用于检测外部设备按钮的

	gestureDetector.setContextClickListener(OnContextClickListener onContextClickListener)

- 关于它需要注意一点，如果侦听 `onContextClick(MotionEvent)`，则必须在 View 的 `onGenericMotionEvent(MotionEvent)`中调用 `GestureDetector` 的 `OnGenericMotionEvent(MotionEvent)`。

### 5.3.2 onDoubleTapListener

用于检测双击事件，有三个回调接口  `onDoubleTap,onDoubleTapEvent,onSingleTapConfirmed`

	gestureDetector.setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) 

- `onClickListener()` 理论上也可以实现监听单击事件，但是要解决俩个问题：

	1. 在事件传递中，如果`onTouchListener()` 被设置，那么就不会去执行`onTouchEvent()`(onClickListener也在这里)。 

	2. 如果需要同时监听 单击和双击 ，`onClickListener()` 实现起来非常麻烦。

- `onSingleTapConfirmed()` 回调函数会在单击事件发生300s后触发,因为需要确认后续没有其他事件发生

- `onDoubleTapEvent()` 用于在双击事件确定发生时，对第二次按下产生的MotionEvent 信息进行回调，`onDoubleTapEvent()`是在第二次down事件触发时 实时回调的。  

	如果我们要双击事件在 第二次up事件触发时，再回调，就需要在onDoubleTapEvent中处理

		final GestureDetector detector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
    	@Override public boolean onDoubleTap(MotionEvent e) {
        	Log.e("第二次按下时触发");
        	return super.onDoubleTap(e);
    	}

    	@Override public boolean onDoubleTapEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                Log.e("第二次抬起时触发");
                break;
        }
        	return super.onDoubleTapEvent(e);
    	}
		});



### 5.3.3 onGestureListener

主要检测以下类型事件：按下(Down)、 一扔(Fling)、长按(LongPress)、滚动(Scroll)、触摸反馈(ShowPress) 和 单击抬起(SingleTapUp)。

#### 5.3.3.1 onDown()  
为了确保View消费了事件,也就是能接收事件序列(down-move-up)，需要消费掉down事件以确保view能接收到后续事件:  
1.让view可点击，因为可点击状态会默认消费down事件     
2.手动消费掉down事件，返回true

  
#### 5.3.3.2 onFling()

常见的场景就是在 ListView 或者 RecyclerView 上快速滑动时手指抬起后它还会滚动一段时间才会停止

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float
        velocityY) {
    	return super.onFling(e1, e2, velocityX, velocityY);
	}


- e1	手指按下时的 Event。  

- e2	手指抬起时的 Event。  

- velocityX	在 X 轴上的运动速度(像素／秒)。  

- velocityY	在 Y 轴上的运动速度(像素／秒)。  


#### 5.2.3.3 onLongPress()

监测长按事件..即手指按下后不抬起，在一段时间后会触发该事件。

#### 5.3.3.4 onScroll()
监听滚动事件

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float 
        distanceY) {
	    return super.onScroll(e1, e2, distanceX, distanceY);
	}


>e1	手指按下时的Event  
>e2	手指抬起时的Event  
>distanceX	在 X 轴上划过的距离  
>distanceY	在 Y 轴上划过的距离  


#### 5.3.3.5 onShowPress()
它是用户按下时的一种回调，主要作用是给用户提供一种视觉反馈，可以在监听到这种事件时可以让控件换一种颜色，或者产生一些变化，告诉用户他的动作已经被识别。

不过这个消息和 onSingleTapConfirmed 类似，也是一种延时回调，延迟时间是 180 ms，假如用户手指按下后立即抬起或者事件立即被拦截，时间没有超过 180 ms的话，这条消息会被 remove 掉，也就不会触发这个回调。


#### 5.3.3.6 onSingleTapUp
- onSingleTapUp 单击事件抬起   
	onClick  单击事件  
	onSingleTapConfirmed 单击事件确认(300ms延迟)

### 5.4 SimpleOnGestureListener
方便使用！




# 6.View的滑动

有以下几种方式可以实现

## 6.1 View.scrollTo View.scrollBy

`View.scrollBy()`:

	/**
     * Move the scrolled position of your view. This will cause a call to
     * {@link #onScrollChanged(int, int, int, int)} and the view will be
     * invalidated.
     * @param x the amount of pixels to scroll by horizontally
     * @param y the amount of pixels to scroll by vertically
     */
    public void scrollBy(int x, int y) {
        scrollTo(mScrollX + x, mScrollY + y);
    }

`View.scrollTo()`:

	 /**
     * Set the scrolled position of your view. This will cause a call to
     * {@link #onScrollChanged(int, int, int, int)} and the view will be
     * invalidated.
     * @param x the x position to scroll to
     * @param y the y position to scroll to
     */
    public void scrollTo(int x, int y) {
        if (mScrollX != x || mScrollY != y) {
            int oldX = mScrollX;
            int oldY = mScrollY;
            mScrollX = x;
            mScrollY = y;
            invalidateParentCaches();
            onScrollChanged(mScrollX, mScrollY, oldX, oldY);
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        }
    }


- 可以看到`scrollBy()`其实也是调用的`scrollTo()`

- 在滑动过程中`mScrollX,mScrollY`这俩个值，分别等于**view的左边缘到View内容左边缘的水平方向距离**，**View的上边缘到view内容上边缘的垂直方向距离**。

- **使用`scrollTo()`和`scrollBy()`这俩个方法只能改变`View`的内容的位置而不能改变View在布局中的位置。  **

View的左边缘如果在内容左边缘的 右边，mScrollX 为正，所以scrollTo(正数)其实是往左移动。。

## 6.2 使用动画

使用View动画，属性动画俩种。

1. 使用view动画的话需要注意，不会真正改变View的位置，也就是说View动画是对View的影像做操作，View的位置参数是不会改变的，并且如果我们希望动画完成后的状态保留，还必须将`fillAfter`属性设置为true，否则在动画结束后，动画的结果会消失。

2. 使用属性动画，属性动画通过改变`translationX,translationY`来改变View的位置。从3.0开始属性动画增加到了android中，3.0之前的版本可以使用动画兼容库nineoldandroid来实现属性动画，不过其本质还是View动画。


## 6.3 改变布局参数

即改变`LayoutParams`,如果要左移一个View，只需要将这个View左边的`marginLeft`值增加相应的值即可完成移动。另外也可以通过在当前view的左边添加一个空的view，然后增加这个空view的width。

	    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
        
        params.width+=100;
        
        requestLayout();
        //或者setLayoutParams(params);

## 6.4 弹性滑动

### 6.4.1 Scroller

弹性滑动对象,用于实现View的弹性滑动.当使用View的`ScrollBy`,`ScrollTo`来进行滑动的时候，是瞬间完成的，使用`Scroller`就不一样了。

Scroller使用的代码是固定的。

- **注意这里的滑动 指的是View内容的滑动而不是view本身的位置改变。**



	private Scroller mScroller = new Scroller(mContext);

    private void smoothScrollTo(int destX, int destY) {

        int scrollX = getScrollX();

        int delta = destX - scrollX;
        //1000ms内平滑的滑向destX,
        mScroller.startScroll(scrollX, 0, delta, 0, 1000);

        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            
            postInvalidate();
        }
    }


**实现流程**：

- `invalidate（）`会使得View重绘，在View的`draw()`方法中会去调用`computeScroll()`方法，而`computeScroll()`又会去向`Scroller`获取当前的`scrollX`和`scrollY`然后通过`scrollTo()`实现滑动，最后会调用`postInvalidate()`方法进行第二次重绘，如此反复直到View通过`ScrollTo()`滑动到了新的位置。

**工作原理**：

- `Scroller`本身是不能实现View的滑动，需要配合View的`computeScroll()`方法才能完成弹性滑动的效果，它不断地让View重绘，而每一次重绘滑动距滑动的起始时间都有一个时间间隔，通过这个时间间隔可以得出View当前的滑动位置，知道了滑动位置可以通过`scrollTo()`方法完成View的滑动，就这样View的每次重绘都会使得View进行小幅度的滑动，许多的小幅度滑动就组成了弹性滑动。

### 6.4.2 通过动画

可以通过属性动画设置一个值到另外一个值 指定时间段内变化。

        ValueAnimator animator = ValueAnimator.ofInt(0, 1).setDuration(1000);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float fraction = animation.getAnimatedFraction();

                Log.d("VelocityTrackerActivity", "fraction:" + fraction);
                
                mView.scrollTo(startX+(int)(deltaX * fraction) ,0);

            }
        });

        animator.start();

### 6.4.3 使用延时策略

1. Handler+postInvalidate

2. Thread+sleep

# 7 View的生命周期

	public class LifeCycleView extends View {

	    private static final String TAG = LifeCycleView.class.getSimpleName();
	
	    public LifeCycleView(Context context) {
	        super(context);
	
	        Log.d(TAG, "construction with one parameter");
	    }
	
	    public LifeCycleView(Context context, AttributeSet attrs) {
	        super(context, attrs);
	
	        Log.d(TAG, "construction with two parameter");
	    }
	
	    //xml布局被view完全解析了之后会调用
	    //也就是说 如果从代码中创建view 不会回调该接口
	    @Override
	    protected void onFinishInflate() {
	        super.onFinishInflate();
	
	        Log.d(TAG, "onFinishInflate");
	    }
	
	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	
	        Log.d(TAG, "onMeasure");
	    }
	
	    @Override
	    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
	        super.onLayout(changed, left, top, right, bottom);
	
	        Log.d(TAG, "onLayout");
	    }
	
	    @Override
	    protected void onDraw(Canvas canvas) {
	        super.onDraw(canvas);
	
	        Log.d(TAG, "onDraw");
	    }
	
	    @Override
	    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	        super.onSizeChanged(w, h, oldw, oldh);
	
	        Log.d(TAG, "onSizeChanged");
	    }
	
	    @Override
	    protected void onAttachedToWindow() {
	        super.onAttachedToWindow();
	
	        Log.d(TAG, "onAttachedToWindow");
	    }
	
	    @Override
	    protected void onDetachedFromWindow() {
	        super.onDetachedFromWindow();
	
	        Log.d(TAG, "onDetachedFromWindow");
	    }
	
	    @Override
	    protected void onWindowVisibilityChanged(int visibility) {
	        super.onWindowVisibilityChanged(visibility);
	
	        Log.d(TAG, "onWindowVisibilityChanged");
	    }
	}

看一下log：

	08-14 17:45:58.279  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ construction with two parameter
	08-14 17:45:58.279  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ onFinishInflate
	08-14 17:45:58.329  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ onAttachedToWindow
	08-14 17:45:58.329  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ onWindowVisibilityChanged
	08-14 17:45:58.329  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ onMeasure
	08-14 17:45:58.329  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ onMeasure
	08-14 17:45:58.469  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ onSizeChanged
	08-14 17:45:58.469  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ onLayout
	08-14 17:45:58.629  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ onMeasure
	08-14 17:45:58.629  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ onMeasure
	08-14 17:45:58.629  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ onLayout
	08-14 17:45:58.629  12523-12523/com.marenbo.www.example D/LifeCycleView﹕ onDraw



# 8 特殊控件的时间处理方案  

## 8.1 特殊形状的点击区域判断  

通过Region进行特殊形状的点击区域判断

- Region 有`setPath(Path p , Region r)`, 可以将Path 转成Region，然后通过contains方法判断 坐标是否落在region内。

## 8.2 画布转换后坐标系不同的问题  

获取 位置信息时的坐标是 默认的左上角，当我们对画布进行一些 translate,scale,skew,rotate之后，画布坐标系 就和 原始位置坐标系不同了,那么这时如何需要计算一个位于原始位置坐标系中 (x,y)点的位置??

- **解决办法**：  

	既然获取到的位置是 根据位置坐标系得到的，那么 我们可以将位置 通过 一定的变化变成 相对 画布坐标系的。  

	例如：画布坐标系 平移了(transX,transY) ,位置坐标系的（x,y）点,我们可使用Matrix 完成坐标映射+数值转换。  

	通过canvas.getMatrix()获取到 canvas 经过变换的 Matrix ,然后将该Matrix 经过invert()操作，得到逆矩阵！然后再通过mapPoints()方法，将(x,y)点经过Matrix变化之后的点求出  

		canvas.save();
    	canvas.translate(mWidth / 2, mHeight / 2);
    	Matrix matrix = new Matrix();
    	canvas.getMatrix().invert(matrix);
   		float[] arrary = new float[]{mX, mY};
    	matrix.mapPoints(arrary);//这一步就是将原先的坐标 转成 相对画布的坐标
    	canvas.drawCircle(arrary[0], arrary[1], 10, mPaint);
    	canvas.restore();



# 9 硬件加速

- 硬件加速在4.0以上默认开启(API14,API11 开始加入了GPU加速支持但是未默认开启)

- [android-官方 硬件加速说明](https://developer.android.com/guide/topics/graphics/hardware-accel.html)

**硬件加速分为 全局(Application),Activity,Window,View四个层级  **

1. `AndroidManifest.xml`文件 `application`标签添加 
	
		android:hardwareAccelerated =true 属性

2. `AndroidManifest.xml`文件 Activity 标签下使用 `hardwareAcceletared` 

3. Window层级开启硬件加速(不支持关闭)

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED，WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

4. View级别关闭硬件 

		setLayerType(View.LAYER_TYPE_SOFTWARE,null)

	或者在布局文件中，给View 添加属性

		 android:layerType = "software"

## 9.1 原理和存在的问题

硬件加速干了一件非常精明的事情，把所有画布坐标系都设置为屏幕(物理)坐标系，之后在 View 绘制区域设置一个遮罩，保证绘制内容不会超过 View 自身的大小，这样就直接跳过坐标转换过程，可以节省坐标系之间数值转换耗费的时间。因此导致了以下问题：

1. 开启硬件加速情况下 event.getX() 和 不开启情况下 event.getRawX() 等价，获取到的是屏幕(物理)坐标   

2. 开启硬件加速情况下 `event.getRawX()` 数值是一个错误数值，因为本身就是全局的坐标又叠加了一次 View 的偏移量，所以肯定是不正确的  

3. 从 Canvas 获取到的 Matrix 是全局的，默认情况下 x,y 偏移量始终为0，因此你不能从这里拿到当前 View 的偏移量  

4. 由于其使用的是遮罩来控制绘制区域，所以如果重绘 path 时，如果 path 区域变大，但没有执行单步操作会导致 path 绘制不完整或者看起来比较奇怪


# 10 多点触控进阶

Android 2.0 开始引入

## 10.1 index和pointID规则

1.  从0开始，自动增长  

	>第1个手指按下	ACTION_DOWN (0x00000000)  
	>第2个手指按下	ACTION_POINTER_DOWN (0x00000105)  
	>第3个手指按下	ACTION_POINTER_DOWN (0x00000205)  
	>第4个手指按下	ACTION_POINTER_DOWN (0x00000305)  

2. 如果之前落下的手指抬起,后面手指的index会随之减小  

	>第1个手指按下	ACTION_DOWN (0x00000000)  
	>第2个手指按下	ACTION_POINTER_DOWN (0x00000105)  
	>第3个手指按下	ACTION_POINTER_DOWN (0x00000205)   
	>第2个手指抬起	ACTION_POINTER_UP (0x00000106)  
	>第3个手指抬起	ACTION_POINTER_UP (0x00000106)  

3.  index变化趋向于第一次落下的数值（落下手指时，前面有空缺的 会优先填补空缺）

	>第1个手指按下	ACTION_DOWN (0x0000 **00**00)  
	>第2个手指按下	ACTION_POINTER_DOWN (0x0000 **01**05)    

	>第3个手指按下	ACTION_POINTER_DOWN (0x0000 **02**05)    
	>第2个手指抬起	ACTION_POINTER_UP (0x0000 **01**06)  
	>
	>第4个手指按下	ACTION_POINTER_DOWN (0x0000 **01**05)  
	>第3个手指抬起	ACTION_POINTER_UP (0x0000 **02**06)  
	 
	即手指抬起时的 Index 会趋向于和按下时相同，虽然在手指数量不足时，Index 会变小，但是当手指变多时，Index 会趋向于保持和按下时一样。

4. 对move事件无效

	取得的ACTION_MOVE事件  始终为0x0000 0002,也就是说，在move时 你无论移动哪个手指，getActionIndex()获取到的始终是数值0  

	区分move事件是哪个手指触发的，需要使用pointId, pointID 和index **最大的区别就是 pointId 是不变的**，始终为第一次落下时的数值，不会受到其他手指抬起和落下的影响  

## 10.2 pointerId和index的异同

- 相同点： 

	1. 从 0 开始，自动增长。  

	2. 落下手指时优先填补空缺(填补之前抬起手指的编号)。

- 不同点：

	1. Index 会变化，pointerId 始终不变。


## 10.3 Move相关事件  
### 10.3.1 actionIndex 和 pointerIndex

在move中无法取得`actionIndex`，需要使用`pointerIndex  `

	event.getX(int pointerIndex)
	
	event.getY(int pointerIndex)

- `actionIndex`和`pointerIndex`区别不大，俩者**数值是相同的**，可以将`pointerIndex`认为是为move事件准备的`actionIndex`

### 10.2.2 pointerIndex 和pointerId  

通常情况下,pinterIndex和pointerId是相同的，但是也可能会因某些手指的抬起而变得不同

- `pointerIndex`	用于获取具体事件，可能会随着其他手指的抬起和落下而变化  

- `pointerId`	用于识别手指，手指按下时产生，手指抬起时回收，期间始终不变

**`pointerIndex`和`pointerId` 互相转换的方法**

- `getPointerId(int pointerIndex)`:	获取一个指针(手指)的唯一标识符ID，在手指按下和抬起之间ID始终不变。  

- `findPointerIndex(int pointerId)`:	通过 pointerId 获取到当前状态下 pointIndex，之后通过 pointIndex 获取其他内容。


### 10.2.3 遍历多点触控

通过遍历pointerCount获取到所有的pointerIndex，同时通过pointerIndex来获取pointerId，可以通过不同手指抬起和按下来观察pointerIndex和pointerId的变化

	switch (event.getActionMasked()) {
    case MotionEvent.ACTION_MOVE:
        for (int i = 0; i < event.getPointerCount(); i++) {
            Log.i("TAG", "pointerIndex="+i+", pointerId="+event.getPointerId(i));
          	// TODO
        }
	}

### 10.2.4 在多点触控中追踪单个手指

	// 用于判断第2个手指是否存在
    boolean haveSecondPoint = false;

    // 记录第2个手指第位置
    PointF point = new PointF(0, 0);

	@Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                // 判断是否是第2个手指按下
                if (event.getPointerId(index) == 1){
                    haveSecondPoint = true;
                    point.set(event.getY(), event.getX());
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // 判断抬起的手指是否是第2个
                if (event.getPointerId(index) == 1){
                    haveSecondPoint = false;
                    point.set(0, 0);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (haveSecondPoint) {
                    // 通过 pointerId 来获取 pointerIndex
                    int pointerIndex = event.findPointerIndex(1);
                    // 通过 pointerIndex 来取出对应的坐标
                    point.set(event.getX(pointerIndex), event.getY(pointerIndex));
                }
                break;
        }

        invalidate();   // 刷新

        return true;
    }

