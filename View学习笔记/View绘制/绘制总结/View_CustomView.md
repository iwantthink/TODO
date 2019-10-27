# 自定义View
---
# 1.自定义view的分类

1. 继承View重写onDraw方法

2. 继承ViewGroup派生特殊的Layout

3. 继承特定的View(比如TextView)

4. 继承特定的ViewGroup(比如LinearLayout)

# 2.自定义Vie注意事项

## 2.1 让View支持wrap_content

`wrap_content`根据`getChildMeasuredSpec()`方法 生成的是`AT_MOST`模式的`measureSpec`,默认的View的measure过程，`AT_MOST` 和`EXACTLY` 是同样的处理方式.

## 2.2 让View支持padding

控件在进行测量时，控件需要将它的Padding尺寸计算在内，因为Padding是其尺寸的一部分

直接继承View的控件，如果不在draw方法中处理padding 那么padding是无法起作用的。 

## 2.3 让ViewGroup支持margin

ViewGroup在进行测量时，需要将子控件的Margin尺寸计算在内。因为子控件的Margin尺寸是父控件尺寸的一部分。

## 2.4 尽量不要再View中使用handler，没必要
View内部已经提供了post系列的方法了，`post()`方法也是借助的`View`内部的`Handler`去实现,所以不需要重复创建

## 2.5 View中如果有线程或者动画，需要及时停止！
停止的时机.. onDetachedFromWindow 。  当包含此View 的Activity退出或者当前View被remove时，会被调用。与此方法对应的是onAttachedToWindow,当包含这个View的Activity启动 ，这个方法会被启动。 当View变得不可见时 我们也需要停止线程 和动画！

## 2.6 View带有滑动嵌套时，需要处理好滑动冲突！

## 2.7 MeasureSpec的生成注意事项

`ViewGroup`为子控件准备`MeasureSpec`时，`SPEC_MODE`应取决于子控件的`LayoutParams.width/height`的取值。

取值为`MATCH_PARENT`或一个确定的尺寸时应为`EXACTLY`，`WRAP_CONTENT`时应为`AT_MOST`。

至于`SPEC_SIZE`，应理解为`ViewGroup`对子控件尺寸的限制，即`ViewGroup`按照其实现意图所允许子控件获得的最大尺寸。并且需要扣除子控件的Margin尺寸。

## 2.8 测量与位置的关系注意事项

虽然说测量的目的在于确定尺寸，与位置无关。但是子控件的位置是`ViewGroup`进行测量时必须要首先考虑的。

因为子控件的位置即决定了子控件可用的剩余尺寸，也决定了父控件的尺寸（当父控件的`LayoutParams.width/height`为`WRAP_CONTENT`时）。


当子控件的测量结果中包含`MEASURED_STATE_TOO_SMALL`标记时，只要有可能，父控件就应当调整给予子控件的`MeasureSpec`，并进行重新测量。倘若没有调整的余地，父控件也应当将`MEASURED_STATE_TOO_SMALL`加入到自己的测量结果中，让它的父控件尝试进行调整。

## 2.9  调用子类测量时必须调用measure()方法

`ViewGroup`在测量子控件时必须调用子控件的`measure()`方法，而不能直接调用其`onMeasure()`方法。直接调用`onMeasure()`方法的最严重后果是子控件的`PFLAG_LAYOUT_REQUIRED`标识无法加入到`mPrivateFlag`中，从而导致子控件无法进行布局。


## 2.10 添加标志位

在测量结果中添加`MEASURED_STATE_TOO_SMALL`需要做到实事求是。当一个方向上的空间不足以显示其内容时应考虑利用另一个方向上的空间，例如对文字进行换行处理，因为添加这个标记有可能导致父控件对其进行重新测量从而降低效率。


# 3.自定义属性的使用
1. 在values目录下面 创建自定义属性的XML文件，例如`attrs.xml`,DictView 是这个自定义属性集合的名字，然后这个集合里面可以有很多的自定义属性。 自定义属性的包括名字和格式
	    
		<declare-styleable name="DictView">
        	<attr name="sbName" format="string"></attr>
        	<attr name="sbAge" format="integer"></attr>
    	</declare-styleable>

2. 需要在自定义view的 构造方法中，解析自定义属性

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DictView);
        String str = a.getString(R.styleable.DictView_sbName);
        
3. 直接在布局文件中使用,需要注意：为了使用自定义属性，必须得在布局文件中添加schemas声明xmlns:dict="http://schemas.android.com/apk/res-auto"




# 4 绘制流程

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fikjvk8monj20fc0heaay.jpg)

**生命周期:**

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

# 5 重要函数介绍
[官方API文档---VIEW](https://developer.android.com/reference/android/view/View.html)
## 5.1 构造函数
	//在代码中创建时使用
	public void SampleView(Context context) {}
	//在xml中使用时使用,关于属性会通过attrs传入
	public void SampleView(Context context, AttributeSet attrs) {}  
	public void SampleView(Context context, AttributeSet attrs, int defStyleAttr) {}  
	public void SampleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {}   


## 5.2 onSizeChanged
在视图大小发生改变时调用

## 5.3 setPivotX()---setPivotY()
设置View旋转或缩放的中心坐标