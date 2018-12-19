# Android提供的工具类
---
Android 提供了一些工具类，在我们creating custom views 时使用，能够比较方便的获得一些数据。

  
参考链接：  
[Android官方文档-custom-views 链接](https://developer.android.com/training/custom-views/index.html "Android官方文档-custom-views")
## 1.1 Configuration  
>This class describes all device configuration information that can impact the resources the application retrieves. This includes both user-specified configuration options (locale list and scaling) as well as device configurations (such as input modes, screen size and screen orientation).

>You can acquire this object from Resources, using getConfiguration(). Thus, from an activity, you can get it by chaining the request with getResources():


>`Configuration config = getResources().getConfiguration();`

简而言之：获取设备的一些信息，输入模式，屏幕大小，屏幕方向等等！或者是用户的一些配置信息，locale和scaling等等！


## 1.2 ViewConfiguration

>Contains methods to standard constants used in the UI for timeouts, sizes, and distances.

>`ViewConfiguration confg = ViewConfiguration.get(context)`

提供一些自定义控件会用到的标准常量，例如尺寸大小，滑动距离，敏感度等等。。

## 1.3 GestureDetector  
用来在onTouchEvent()处理手势

<pre>

比如将Activity的Touch事件交给GestureDetector处理
@Override  
public boolean onTouchEvent(MotionEvent event) {  
     return mGestureDetector.onTouchEvent(event);  
} 


比如将View的Touch事件交给GestureDetector处理
mButton=(Button) findViewById(R.id.button);  
mButton.setOnTouchListener(new OnTouchListener() {            
   @Override  
   public boolean onTouch(View arg0, MotionEvent event) {  
          return mGestureDetector.onTouchEvent(event);  
      }  
});  
</pre>

## 1.4 VelocityTracker
用来获取速度。。  
速度 = (终点位置-起点位置)/时间段  
速度可以为负的，说明当前是逆着坐标轴正方向滑动  
<pre>
VelocityTracker vt = VelocityTracker.obtain();  
vt.addMovement(motionEvent);  
vt.computeCurrentVelocity(1000);
vt.getXVelocity();
vt.clear();
vt.recycle();
</pre>


## 1.5 Scroller  


scrollBy 最终还是调用了scrollTo  
<pre>
public void scrollBy(int x, int y) {   
       scrollTo(mScrollX + x, mScrollY + y);   
} 
</pre>

**scrollTo 和scrollBy 实际上只是移动了view的内容**  


scrollTo（０,２５），实际上会向上移动25 而不是向下。  

## 1.6 ViewDragHelper
用来处理拖拽 子View ，提供了一些辅助方法和与其相关的状态记录。  

<pre>

public class MyLinearLayout extends LinearLayout {
    private ViewDragHelper mViewDragHelper;

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViewDragHelper();
    }

    //初始化ViewDragHelper
    private void initViewDragHelper() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }

            //处理水平方向的越界
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                int fixedLeft;
                View parent = (View) child.getParent();
                int leftBound = parent.getPaddingLeft();
                int rightBound = parent.getWidth() - child.getWidth() - parent.getPaddingRight();

                if (left < leftBound) {
                    fixedLeft = leftBound;
                } else if (left > rightBound) {
                    fixedLeft = rightBound;
                } else {
                    fixedLeft = left;
                }
                return fixedLeft;
            }

            //处理垂直方向的越界
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                int fixedTop;
                View parent = (View) child.getParent();
                int topBound = getPaddingTop();
                int bottomBound = getHeight() - child.getHeight() - parent.getPaddingBottom();
                if (top < topBound) {
                    fixedTop = topBound;
                } else if (top > bottomBound) {
                    fixedTop = bottomBound;
                } else {
                    fixedTop = top;
                }
                return fixedTop;
            }

            //监听拖动状态的改变
            @Override
            public void onViewDragStateChanged(int state) {
                super.onViewDragStateChanged(state);
                switch (state) {
                    case ViewDragHelper.STATE_DRAGGING:
                        System.out.println("STATE_DRAGGING");
                        break;
                    case ViewDragHelper.STATE_IDLE:
                        System.out.println("STATE_IDLE");
                        break;
                    case ViewDragHelper.STATE_SETTLING:
                        System.out.println("STATE_SETTLING");
                        break;
                }
            }

            //捕获View
            @Override
            public void onViewCaptured(View capturedChild, int activePointerId) {
                super.onViewCaptured(capturedChild, activePointerId);
                System.out.println("ViewCaptured");
            }

            //释放View
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                System.out.println("ViewReleased");
            }
        });
    }

    //将事件拦截交给ViewDragHelper处理
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }


    //将Touch事件交给ViewDragHelper处理
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mViewDragHelper.processTouchEvent(ev);
        return true;
    }
}
</pre>


## 2 角度和弧度
角度是60进制，弧度是10进制。例如30°6角度换算成弧度就是30.1°

- 角度：两条射线从圆心向圆周射出，形成一个夹角和夹角正对的一段弧。当这段弧长正好等于圆周长的360分之一时，两条射线的夹角的大小为1度.


- 弧度：两条射线从圆心向圆周射出，形成一个夹角和夹角正对的一段弧。当这段弧长正好等于圆的半径时，两条射线的夹角大小为1弧度.


### 2.1 角度和弧度的换算关系
360（角度） = 2π(弧度) 
>rad是弧度，deg是角度  
>rad= deg \* π / 180  
>deg = rad \* 180 / π


## 3 颜色

### 3.1 颜色模式：  

1. ARGB8888  四通道高精度(32位)
2. ARGB4444  四通道低精度(16位)
3. RGB565    屏幕默认模式(16位)
4. Alpha8    仅有透明通道(8位)

>其中字母表示通道类型，数值表示该类型用多少位二进制来描述。如ARGB8888则表示有四个通道(ARGB),每个对应的通道均用8位来描述。


## 4 颜色混合模式
因为我们的显示屏是没法透明的，因此最终显示在屏幕上的颜色里可以认为没有Alpha通道。Alpha通道主要在两个图像混合的时候生效。


- 默认情况下，当一个颜色绘制到Canvas上时的混合模式是这样计算的：

	**(RGB通道) 最终颜色 = 绘制的颜色 + (1 - 绘制颜色的透明度) × Canvas上的原有颜色。**

	>1.这里我们一般把每个通道的取值从0(ox00)到255(0xff)映射到0到1的浮点数表示。
	>
	>2.这里等式右边的“绘制的颜色”、“Canvas上的原有颜色” 都是经过预乘了自己的Alpha通道的值。如绘制颜色：0x88ffffff，那么参与运算时的每个颜色通道的值不是1.0，而是(1.0 * 0.5333 = 0.5333)。 (其中0.5333 = 0x88/0xff)

	使用这种方式 就可以生成绘制的内容以半透明的方式叠在上面


