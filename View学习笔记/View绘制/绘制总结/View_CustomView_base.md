# View的绘制原理基础

# 1. ViewRootImpl 和DecorView
- `ViewRootImpl`是连接`WindowManager` 和`DecorView`的纽带，View绘制的三大流程都是通过`ViewRootImpl`来完成的。  

- 创建流程:

	`ViewRootImpl`,`DecorView`,`PhoneWindow`等创建过程参考[视图添加过程.md](),[视图创建过程.md]()

- View的绘制流程从`ViewRootImpl`的**`performTraversals`**方法开始,然后会依次调用`performMeasure,performLayout,performDraw` 。  经过measure , layout , draw 三个过程 才能最终将一个view绘制出来 。
  
	例如:`ViewGroup`(performMeasure->measure->onMeasure)->View(measure),在onMeasure中会对所有的子类进行遍历并调用子类的measure

- measure的过程结束后，可以获得测量宽高，getMeasureWidth,几乎所有情况下 测量宽高都等同于 最终宽高。  

- `performDraw()`的遍历过程是在draw方法中通过`dispatchDraw()`来实现  

- Layout过程决定了View的四个顶点的坐标和实际的View的宽高，可以通过`getWidth()`, `getHeight()`方法获取最终宽高

- `android.R.id.content` 是`DecorView`的content 部分的id




# 2. MeasureSpec基础知识

>A MeasureSpec encapsulates the layout requirements passed from parent to child.Each MeasureSpec represents a requirement for either the width or the height.A MeasureSpec is comprised of a size and a mode.

1. **`MeasureSpec`封装了父布局传递给子View的布局要求  **

2. `MeasureSpec`可以表示宽和高  

3. `MeasureSpec`由size和mode组成  

  
## 2.1 MeasureSpec 组成
MeasureSpec 是一个32位的int数据，高2位 代表SpecMdoe即某种测量模式，低30位为SpecSize 代表该模式下的大小信息  

- **获取size:** 

		MeasureSpec.getSize(measureSpec)

- **获取mode:**

		MeasureSpec.getMode(measureSpec)

- **生成specMode**

		MeasureSpec.makeMeasureSpec(size,mode)
  
## 2.2 SpecMode类型

1. `MeasureSpec.EXACTLY `

2. `MeasureSpec.AT_MOST`

3. `MeasureSpec.UNSPECIFIED`

### 2.2.1 MeasureSpec.EXACTLY
>The parent has determined an exact size for the child. The child is going to be given those bounds regardless of how big it wants to be.

父容器已经检测出子类view所需要的精确大小.子控件必须为`SPEC_SIZE`的值 ,即该模式下，View的测量大小即为`SPEC_SIZE`

**当控件的`LayoutParams.width/height`为 确定值或者是`MATCH_PARENT`时,对应的`MeasureSpec.SPEC_MODE`会使用`EXACTLY`**

### 2.2.2 MeasureSpec.AT_MOST

>The child can be as large as it wants up to the specified size.

父容器未检测出子View所需要的精确大小，但是指定了一个可用大小即`SPEC_SIZE`,该模式下，View的测量大小最大不能超过SpecSize  

**当控件的`LayoutParams.width/height`为`WRAP_CONTENT`时,对应的`MeasureSpec.SPEC_MODE`会使用`AT_MOST`**

### 2.2.3 MeasureSpec.UNSPECIFIED
>The parent has not imposed any constraint on the child. It can be whatever size it wants.

控件在进行测量时,父容器不对子View做限制,可以无视`SPEC_SIZE`的值.控件可以是它所期望的任何值
 
MeasureSpec.UNSPECIFIED这种模式一般用作Android系统内部，或者ListView和ScrollView等滑动控件


## 2.3 MeasureSpec 和LayoutParams 的关系 
**`LayoutParams` 需要和父容器一起才能决定`view`的`MeasureSpec`**

- 对于顶级`DecorView`来说，其`MeasureSpec`由窗口的尺寸和自身`LayoutParams` 共同决定.   

	在`ViewRootImpl` 的`measureHierarchy ()`中 有如下一段代码，展示了`DecorView`的`MeasureSpec`的创建过程

		childWidthMeasureSpec = getRootMeasureSpec(desiredWindowWidth, lp.width);
    	childHeightMeasureSpec = getRootMeasureSpec(desiredWindowHeight, lp.height);
   		performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);

	`getRootMeasureSpec()`的逻辑:

		//windowSize 		窗口大小 期待大小
		//rootDImension 	窗口LayoutParams 大小
		private static int getRootMeasureSpec(int windowSize, int rootDimension) {
	        int measureSpec;
	        switch (rootDimension) {
	
	        case ViewGroup.LayoutParams.MATCH_PARENT:
	            // Window can't resize. Force root view to be windowSize.
	            measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.EXACTLY);
	            break;
	        case ViewGroup.LayoutParams.WRAP_CONTENT:
	            // Window can resize. Set max size for root view.
	            measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.AT_MOST);
	            break;
	        default:
	            // Window wants to be an exact size. Force root view to be that size.
	            measureSpec = MeasureSpec.makeMeasureSpec(rootDimension, MeasureSpec.EXACTLY);
	            break;
	        }
	        return measureSpec;
    }


- 对于普通`View`来说，`View`的`measure`过程由`ViewGroup`传递而来，首先可以看下`ViewGroup`的`measureChildWithMargins()`方法,了解非rootView 的`MeasureSpec`是如何进行创建

	    protected void measureChildWithMargins(View child,
        int parentWidthMeasureSpec, int widthUsed,
        int parentHeightMeasureSpec, int heightUsed) {
	        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
	
	        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
	                mPaddingLeft + mPaddingRight + lp.leftMargin + lp.rightMargin
	                        + widthUsed, lp.width);
	        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
	                mPaddingTop + mPaddingBottom + lp.topMargin + lp.bottomMargin
	                        + heightUsed, lp.height);
	
	        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    	}

	该方法在调用子元素的measure之前，会先通过`getChildMeasureSpec()`方法来获得子元素的`MeasureSpec`

	    public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
			// 父类mode
	        int specMode = MeasureSpec.getMode(spec);
			// 父类size
	        int specSize = MeasureSpec.getSize(spec);
			// 父类大小-padding 大小 , 不能小于0
			// 即减去paading之后的大小
	        int size = Math.max(0, specSize - padding);
			// 子类大小
	        int resultSize = 0;
			// 子类模式
	        int resultMode = 0;
	
	        switch (specMode) {
		        // Parent has imposed an exact size on us
		        case MeasureSpec.EXACTLY:
					// 结合子类的LayoutParams进行判断
		            if (childDimension >= 0) {
						// 子类有确切的大小
		                resultSize = childDimension;
		                resultMode = MeasureSpec.EXACTLY;
		            } else if (childDimension == LayoutParams.MATCH_PARENT) {
		                // Child wants to be our size. So be it.
		                resultSize = size;
		                resultMode = MeasureSpec.EXACTLY;
		            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
		                // Child wants to determine its own size. It can't be
		                // bigger than us.
		                resultSize = size;
		                resultMode = MeasureSpec.AT_MOST;
		            }
		            break;
		
		        // Parent has imposed a maximum size on us
		        case MeasureSpec.AT_MOST:
		            if (childDimension >= 0) {
		                // Child wants a specific size... so be it
		                resultSize = childDimension;
		                resultMode = MeasureSpec.EXACTLY;
		            } else if (childDimension == LayoutParams.MATCH_PARENT) {
		                // Child wants to be our size, but our size is not fixed.
		                // Constrain child to not be bigger than us.
		                resultSize = size;
		                resultMode = MeasureSpec.AT_MOST;
		            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
		                // Child wants to determine its own size. It can't be
		                // bigger than us.
		                resultSize = size;
		                resultMode = MeasureSpec.AT_MOST;
		            }
		            break;
		
		        // Parent asked to see how big we want to be
		        case MeasureSpec.UNSPECIFIED:
		            if (childDimension >= 0) {
		                // Child wants a specific size... let him have it
		                resultSize = childDimension;
		                resultMode = MeasureSpec.EXACTLY;
		            } else if (childDimension == LayoutParams.MATCH_PARENT) {
		                // Child wants to be our size... find out how big it should
		                // be
		                resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
		                resultMode = MeasureSpec.UNSPECIFIED;
		            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
		                // Child wants to determine its own size.... find out how
		                // big it should be
		                resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
		                resultMode = MeasureSpec.UNSPECIFIED;
		            }
		            break;
	        }
	        //noinspection ResourceType
	        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
   	    }
	- 可以通过以上代码知道 **子元素的MeasureSpec 主要是根据 父容器的MeasureSpec和子元素本身的LayoutParams来组合而成的**！

	**简单的总结下：  **
		
		1. 当子`View`决定采用固定宽高的时候，子`View`的`MeasureSpec`都是精确模式,并且大小遵循`LayoutParams`的大小。     

		2. 当子`View`的宽高是`match_parent` ,则要根据父容器的`MeasureSpec`来具体区分
			1. 如果父容器的模式是精准模式，那么子`View`也是精准模式,并且其大小是父容器的剩余空间
			2. 如果父容器的模式是最大模式，那么子`View`也是最大模式，并且其大小不会超过父容器的剩余空间   

		3. 当View的宽高是`wrap_content`时，不管父容器是精准还是最大化，View的模式总是最大化，并且大小不可以超过父容器的剩余空间  

		4. **一般情况下不考虑`UNSPECIFIED`模式。。 这个模式主要是系统内部多次measure时用到**
		 

# 3. View的工作流程
## 3.1 measure过程
**measure过程的发起:**

1. 从`ViewRootImpl.performTraversals()`开始,然后执行`ViewRootImpl.performMeasure()`,接着在其代码中调用`mView.measure()`(已知该mView是DecorView)

2. `DecorView`是`FrameLayout`类型,其没有也不能重写`measure()`方法,`measure()`存在于`View`中

3. 在`View.measure()`方法中,调用了`onMeasure()`方法,该方法在`DecorView`中被重写,`DecorView`实现了自己的测量逻辑,并创建了代表自身的`MeasureSpec`

4. 随后 `DecorView`回调了`FrameLayout`的`onMeasure()`,并开始遍历子控件

**measure过程要分情况:**

1. 一种是子控件为`View`，那么父类在调用子控件进行测量时,通过`measure()`方法就完成了其测量过程.

	子控件自身的大小由子控件的`onMeasure()`方法提供

2. 一种是子控件为`ViewGroup`，那么当父类调用子控件进行测量时,除了完成自己的测量过程，还需要去遍历调用所有子元素的`measure()`方法。 

	`ViewGroup`无法重写`View.measure()`方法,所以其实现还是在`View`中

	**遍历子类的逻辑需要继承`ViewGroup`类型的控件自己来实现,具体的操作就是重写`ViewGroup`类型控件的`onMeasure()`方法中,然后在`onMeasure()`方法中遍历子类,获取其`MeasureSpec`并调用其`measure()`方法**


### 3.1.1 View的measure 过程  

`View`类型的控件,只需要测量好自身大小即可,具体的测量逻辑在`onMeasure()`中,`View`类型的控件需要重写这个方法 并提供自己的测量逻辑,**最重要的是一定要在测量结束 调用`setMeasuredDimension()`方法将测量结果进行保存**

以下是默认的`View`类型控件测量过程:

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
	}

- **`widthMeasureSpec`和`heightMeasureSpec`是其父类对子类的要求**.它的来源可以分成俩种:

	1. 由`ViewRootImpl`根据窗口大小和默认的`WindowManager.LayoutParams`生成

		这也是`MeasureSpec`最初的生成规则

	2. 在`ViewGroup`类型的控件中的`onMeasure()`中生成,并传递给子类

---

`getSuggestedMinimumHeight()`逻辑如下

	protected int getSuggestedMinimumHeight() {
        return (mBackground == null) ? mMinHeight : max(mMinHeight, mBackground.getMinimumHeight());

    	}

- `getSuggestedMinimumHeight()`方法的逻辑：

	1. 如果View没有设置背景，那么返回android:minWidth属性设置的值，这个值可以为0。

	2. 如果有设置背景，则返回 minWidth和背景的最小宽度这俩者中的最大值，其实就是View在`UNSPECIFIED`情况下的测量宽高.

---

`getDefaultSize()`代码如下:

	public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    	}

- 参数:
	1. `measureSpec`可以拆分成俩个值,`specMode`代表父类控件对子类控件的要求,`specSize`代表父类提供给子类的最大值

	2. **`size`代表控件自身需要的最小值**


- 从中可以看出默认情况下`AT_MOST`和`EXACTLY` 是同一种处理方式

- 已知,父类在调用子类之前,会先生成`MeasureSpec`,这个值代表父类对子类的要求.

	根据`getChildMeasureSpec()`方法,其生成的`MeasureSpec`值(根据父类MeasureSpec和子类自身的LayoutParams生成)，再结合这里的`getDefaultSize()`处理方式。

	**得出一个结论，如果自定义了一个`View` 却不对其`wrap_content` 进行特殊处理，那么就会得到`match_parent`一样的效果。 **

	- 因为`wrap_content`和`match_parent` 这俩个`LayoutParams` 对长度的要求在生成`MeasureSpec`时,`specMode`都是`AT_MOST`或`EXACTLY`


### 3.1.2 ViewGroup的measure过程  

`ViewGroup`没有重写`onMeasure()`方法,是一个抽象类。
  		  
`ViewGroup`没有具体的定义测量的过程，其测量过程需要各个子类去具体实现  

`ViewGroup`提供了一个`measureChildren()`的方法，在`ViewGroup`进行`measure`时，会对每一个子元素进行`measure`

	protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
		final int size = mChildrenCount;
		final View[] children = mChildren;
	        for (int i = 0; i < size; ++i) {
	            final View child = children[i];
	            if ((child.mViewFlags & VISIBILITY_MASK) != GONE) {
	                measureChild(child, widthMeasureSpec, heightMeasureSpec);
	            }
	        }
    	}

- `measureChild()`主要就是通过`lp.width`(具体数值，`match_parent`,`wrap_content`)和父容器生成的`measureSpec`来生成 子类的`MeasureSpec`,并传给子类`measure()`方法当做参数使用

		protected void measureChild(View child, int parentWidthMeasureSpec,
            int parentHeightMeasureSpec) {
	        final LayoutParams lp = child.getLayoutParams();
	
	        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
	                mPaddingLeft + mPaddingRight, lp.width);
	        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
	                mPaddingTop + mPaddingBottom, lp.height);
	
	        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		}

### 3.1.3 宽高信息注意事项

**Activity的生命周期和View的生命周期是不同步的，因此如果你在activity的生命周期中去获取view的宽高信息，很有可能获得是0！ ** 

**解决办法如下：**  


1. `Activity / View  #onWindowFocusChanged  `

	但是注意这个方法可能会被调用多次，在焦点频繁的获得失去的时候.  

2. `View.post(runnable)  `

	在runnable中获取宽高信息，然后post到消息队列的尾部，等待looper调用此runnable时，View也已经初始化好了  

3. `ViewTreeObserver  `

	View树的状态改变，onGlobalLayout会被多次调用

4. 手动调用`view.measure(measureSpecWidth,measureSpecHeight)`  
	


### 3.2 layout过程

`layout`作用是`Viewgroup`用来确定自身的位置，当`Viewgroup`的位置被确定后，它在`onLayout()`中会遍历所有的子元素并调用其`layout`方法，在`layout`方法中又会调用`onLayout`方法。  
layout方法会确定view本身位置，onLayout方法会确定所有子元素的位置

View的`layout()`方法   
 
	public void layout(int l, int t, int r, int b) {
		if ((mPrivateFlags3 & PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT) != 0) {
	            onMeasure(mOldWidthMeasureSpec, mOldHeightMeasureSpec);
	            mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
		}
	
		int oldL = mLeft;
		int oldT = mTop;
		int oldB = mBottom;
		int oldR = mRight;
	
		boolean changed = isLayoutModeOptical(mParent) ?
	                setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);
	
		if (changed || (mPrivateFlags & PFLAG_LAYOUT_REQUIRED) == PFLAG_LAYOUT_REQUIRED) {
			onLayout(changed, l, t, r, b);
			mPrivateFlags &= ~PFLAG_LAYOUT_REQUIRED;
	
			ListenerInfo li = mListenerInfo;
			if (li != null && li.mOnLayoutChangeListeners != null) {
	                ArrayList<OnLayoutChangeListener> listenersCopy =
	                        (ArrayList<OnLayoutChangeListener>)li.mOnLayoutChangeListeners.clone();
	                int numListeners = listenersCopy.size();
	                for (int i = 0; i < numListeners; ++i) {
	                    listenersCopy.get(i).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
				}
			}
		}
	
		mPrivateFlags &= ~PFLAG_FORCE_LAYOUT;
		mPrivateFlags3 |= PFLAG3_IS_LAID_OUT;
	}

- `LAYOUT_MODE_OPTICAL_BOUNDS`:

	在绘制View的阴影,发光区域等情况下,会影响到其他控件.这个FLAG就指代这种情况

- `View.layout()`最开始是从`ViewRootImpl.performLayout()`开始,其坐标为

		host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());


- **布局流程**：

	首先会通过`setFrame()`来设定View的四个顶点的值(`setOpticalFrame()`最终也是调用`setFrame()`,只不过会将一些特殊区域考虑进来)，即去初始化`mLeft,mRight,mTop,mBottom`。这四个值确定了，View在父容器的位置也就确定了。接着会去调用onLayout方法（这个方法是父容器确定子元素的位置）。 

	**View和ViewGroup均没有真正去实现onLayout方法，因为要根据具体的布局来决定具体的实现。**

- `getWidth()`或 `geHeight()` 方法与 `getMeasuredWidth()`和`getMeasuredHeight()`俩者完全一样，只是前者的值在 layout过程中 生成， 后者的值在measure过程中生成。  

	但是如果手动的重写layout方法，并将left right top bottom 参数进行修改。。 那么会造成 测量宽高和 最终宽高不一致。。。另外某些情况下View需要多次measure才能确定自己的测量宽高，在前几次测量过程中，其测量宽高可能和最终宽高不一致，但是测量完成之后，还是一致的！



### 3.3 draw过程
draw的绘制过程遵循以下几步：  

1. 绘制背景 background.draw(canvas)
2. 绘制自己 onDraw
3. 绘制children  dispatchDraw
4. 绘制装饰  onDrawScrollBars  

View的draw代码：

	  /*
         * Draw traversal performs several drawing steps which must be executed
         * in the appropriate order:
         *
         *      1. Draw the background
         *      2. If necessary, save the canvas' layers to prepare for fading
         *      3. Draw view's content
         *      4. Draw children
         *      5. If necessary, draw the fading edges and restore layers
         *      6. Draw decorations (scrollbars for instance)
         */

        // Step 1, draw the background, if needed
        int saveCount;

        if (!dirtyOpaque) {
            drawBackground(canvas);
        }

        // skip step 2 & 5 if possible (common case)
        final int viewFlags = mViewFlags;
        boolean horizontalEdges = (viewFlags & FADING_EDGE_HORIZONTAL) != 0;
        boolean verticalEdges = (viewFlags & FADING_EDGE_VERTICAL) != 0;
        if (!verticalEdges && !horizontalEdges) {
            // Step 3, draw the content
            if (!dirtyOpaque) onDraw(canvas);

            // Step 4, draw the children
            dispatchDraw(canvas);

            // Overlay is part of the content and draws beneath Foreground
            if (mOverlay != null && !mOverlay.isEmpty()) {
                mOverlay.getOverlayView().dispatchDraw(canvas);
            }

            // Step 6, draw decorations (foreground, scrollbars)
            onDrawForeground(canvas);

            // we're done...
            return;
        }


View绘制过程是通过dispatchDraw来实现的，dispatchDraw会遍历调用所有子元素的draw方法。  

### 3.4 setWillNotDraw

View有一个特殊的方法**setWillNotDraw()**,如果一个view不需要绘制任何内容，那么设置这个标志位true，系统会进行优化！默认情况下，view 没有启用这个标志，viewgroup默认启用这个标志。  
当我们的自定义控件继承自viewGroup并且本身不具有绘制功能时，可以开启这个标志。另外当明确知道一个viewgroup 需要通过onDraw来进行绘制内容时，需要关闭这个标志。。。  

	public void setWillNotDraw(boolean willNotDraw) {
        setFlags(willNotDraw ? WILL_NOT_DRAW : 0, DRAW_MASK);
    }