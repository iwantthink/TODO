# Flutter核心原理
[Flutter 核心原理](https://github.com/flutterchina/flutter-in-action/blob/master/docs/chapter14/index.md)


# 1. Flutter UI系统

这里的UI系统特指：基于一个平台，在此平台上实现GUI的一个系统，这里的平台特指操作系统，如Android、iOS或者Windows、macOS

- 各个平台UI系统的原理是相通的，也就是说无论是Android还是iOS，它们将一个用户界面展示到屏幕的流程是相似的，所以，在介绍Flutter UI系统之前，先了解UI系统的基本原理，增加对操作系统和系统底层UI逻辑的了解，可以更好的了解Flutter 的UI系统

## 1.1 UI系统原理

### 1.1.1 硬件绘图基本原理

屏幕显示图像的基本原理:

- 显示器（屏幕）是由一个个物理显示单元组成，每一个单元可以称之为一个物理像素点，而每一个像素点可以发出多种颜色，显示器成相的原理就是在不同的物理像素点上显示不同的颜色，最终构成完整的图像

一个像素点能发出的所有颜色总数是显示器的一个重要指标，比如所谓的1600万色的屏幕就是指一个像素点可以显示出1600万种颜色，而显示器颜色是有RGB三基色组成，所以1600万即2的24次方，即每个基本色（R、G、B）深度扩展至8 bit(位)，颜色深度越深，所能显示的色彩更加丰富靓丽

为了更新显示画面，显示器是以固定的频率刷新（从GPU取数据），比如有一部手机屏幕的刷新频率是 60Hz。当一帧图像绘制完毕后准备绘制下一帧时，显示器会发出一个垂直同步信号（如`VSync`）， 60Hz的屏幕就会一秒内发出 60次这样的信号。而这个信号主要是用于同步CPU、GPU和显示器的。一般地来说，计算机系统中，CPU、GPU和显示器以一种特定的方式协作：**CPU将计算好的显示内容提交给 GPU，GPU渲染后放入帧缓冲区，然后视频控制器按照同步信号从帧缓冲区取帧数据传递给显示器显示**

CPU和GPU的任务是各有偏重的，CPU主要用于基本数学和逻辑计算，而GPU主要执行和图形处理相关的复杂的数学，如矩阵变化和几何计算，GPU的主要作用就是确定最终输送给显示器的各个像素点的色值

### 1.1.2 操作系统绘制API的封装

由于最终的图形计算和绘制都是由相应的硬件来完成，而直接操作硬件的指令通常都会有操作系统屏蔽，应用开发者通常不会直接面对硬件，操作系统屏蔽了这些底层硬件操作后会提供一些封装后的API供操作系统之上的应用调用，但是对于应用开发者来说，直接调用这些操作系统提供的API是比较复杂和低效的，因为操作系统提供的API往往比较基础，直接调用需要了解API的很多细节

正是因为这个原因，**几乎所有用于开发GUI程序的编程语言都会在操作系统之上再封装一层，将操作系统原生API封装在一个编程框架和模型中，然后定义一种简单的开发规则来开发GUI应用程序，而这一层抽象，正是所谓的"UI"系统**

- Android SDK正是封装了Android操作系统API，提供了一个"UI描述文件XML+Java操作DOM"的UI系统,iOS的UIKit 对View的抽象也是一样的，**它们都将操作系统API抽象成一个基础对象（比如用于2D图形绘制的Canvas），然后再定义一套规则来描述UI，如UI树结构，UI操作的单线程原则等**


## 1.2 Flutter UI系统

**无论是Android SDK还是iOS的UIKit 的职责都是相同的，它们只是语言载体和底层的系统不同而已**

那么可不可以实现这么一个UI系统：使用同一种编程语言开发，然后针对不同操作系统API抽象一个对上接口一致，对下适配不同操作系统的的中间层，然后在打包编译时再使用相应的中间层代码？如果可以做到，那么我们就可以使用同一套代码编写跨平台的应用了

- **Flutter的原理正是如此，它提供了一套Dart API，然后在底层通过`OpenGL`这种跨平台的绘制库（内部会调用操作系统API）实现了一套代码跨多端.由于Dart API也是调用操作系统API，所以它的性能接近原生**

- 注意，虽然Dart是先调用了`OpenGL`，`OpenGL`才会调用操作系统API，但是这仍然是原生渲染，因为`OpenGL`只是操作系统API的一个封装库，它并不像WebView渲染那样需要JavaScript运行环境和CSS渲染器，所以不会有性能损失

至此，已经介绍了Flutter UI系统和操作系统交互的这一部分原理，现在介绍Flutter UI对应用开发者定义的开发标准:

- 这个标准 简单概括就是：**组合和响应式**。**使用Flutter开发一个UI界面，需要通过组合其它Widget来实现，Flutter中，一切都是Widget，当UI要发生变化时，不是直接去修改DOM，而是通过更新状态，让Flutter UI系统来根据新的状态来重新构建UI**

Flutter UI系统和Flutter Framework的概念是差不多的，之所以用"UI系统"，是因为其他平台中可能不这么叫，为了概念统一，便于描述


# 2. Element

**Flutter中真正代表屏幕上显示元素的类是`Element`，也就是说`Widget`只是描述`Element`的一个配置**

因此,Flutter中最底层的UI树实际上是由一个个独立的`Element`节点构成,`Widget`最终的`Layout`、渲染都是通过`RenderObject`来完成的

- Flutter中，根据Widget是否需要包含子节点以及子节点的数量 将Widget分为了三类，分别对应三种Element，如下表：


	Widget	|对应的Element |	用途
	:---|:---|---
	`LeafRenderObjectWidget`|`	LeafRenderObjectElement`|Widget树的叶子节点，**用于没有子节点的widget**，通常基础widget都属于这一类，如Text、Image
	`SingleChildRenderObjectWidget`|`SingleChildRenderObjectElement`|**包含一个子Widget**，如：ConstrainedBox、DecoratedBox等
	`MultiChildRenderObjectWidget`|`MultiChildRenderObjectElement`|**包含多个子Widget**，一般都有一个children参数，接受一个Widget数组。如Row、Column、Stack等


**从创建到渲染的大体流程是**

- 根据`Widget`生成`Element`，然后创建相应的`RenderObject`并关联到`Element.renderObject`属性上，最后再通过`RenderObject`来完成布局排列和绘制

	`Element`就是`Widget`在UI树具体位置的一个实例化对象，大多数`Element`只有唯一的`renderObject`，但还有一些`Element`会有多个子节点，如继承自`RenderObjectElement`的一些类以及`MultiChildRenderObjectElement`
	
	最终所有`Element的RenderObject`构成一棵树，称之为渲染树，即render tree

## 2.1 Element的生命周期

1. Framework 调用`Widget.createElement()`创建一个Element实例，记为`element`

2. Framework调用 `element.mount(parentElement,newSlot)` ，`mount()`方法中首先调用`element`所对应`Widget`(通常在其`build()`方法中)的`createRenderObject()`方法创建与`element`相关联的`RenderObject`对象，然后调用`element.attachRenderObject()`方法将`element.renderObject`添加到渲染树中插槽指定的位置（这一步不是必须的，一般发生在Element树结构发生变化时才需要重新attach）。插入到渲染树后的`element`就处于"active"状态，处于"active"状态后就可以显示在屏幕上了（可以隐藏)

3. 当`element`父Widget的配置数据改变时，为了进行Element复用，Framework在决定重新创建Element前会先尝试复用相同位置旧的`element`：

	即调用Element对应Widget的`canUpdate()`方法，如果返回true，则复用旧Element，旧的Element会使用新的Widget配置数据更新，反之则会创建一个新的Element，不会复用
	
	`Widget.canUpdate()`主要是判断`newWidget`与`oldWidget`的`runtimeType`和`key`是否同时相等，如果同时相等就返回true，否则就会返回false。根据这个原理，**当需要强制更新一个Widget时，可以通过指定不同的Key来禁止复用**

		  static bool canUpdate(Widget oldWidget, Widget newWidget) {
		    return oldWidget.runtimeType == newWidget.runtimeType
		        && oldWidget.key == newWidget.key;
		  }

4. 当有父Widget的配置数据改变时，同时其`State.build()`方法返回的Widget结构与之前不同，此时就需要重新构建对应的`Element`树

	为了进行`Element`复用，在`Element`重新构建前会先尝试是否可以复用旧树上相同位置的element，element节点在更新前都会调用其对应Widget的`canUpdate()`方法，如果返回true，则复用旧Element，旧的Element会使用新Widget配置数据更新，反之则会创建一个新的Element。`Widget.canUpdate`主要是判断newWidget与oldWidget的runtimeType和key是否同时相等，如果同时相等就返回true，否则就会返回false。根据这个原理，当我们需要强制更新一个Widget时，可以通过指定不同的Key来避免复用

5. 当有祖先`Element`决定要移除`element`时（如Widget树结构发生了变化，导致element对应的Widget被移除），这时该祖先Element就会调用`deactivateChild()`方法来移除它，移除后`element.renderObject`也会被从渲染树中移除，然后Framework会调用`element.deactivate()`方法，这时element状态变为"inactive"状态

6. "inactive"状态的element将不会再显示到屏幕。为了避免在一次动画执行过程中反复创建、移除某个特定element，"inactive"状态的element在当前动画最后一帧结束前都会保留，如果在动画执行结束后它还未能重新变成"active"状态，Framework就会调用其`unmount()`方法将其彻底移除，这时element的状态为"defunct",它将永远不会再被插入到树中

7. 如果element要重新插入到Element树的其它位置，如element或element的祖先拥有一个`GlobalKey`（用于全局复用元素），那么Framework会先将element从现有位置移除，然后再调用其`activate()`方法，并将其`renderObject`重新attach到渲染树


**对于开发者来说，大多数情况下只需要关注Widget树就行（不需要直接操作Element树），Flutter框架已经将对Widget树的操作映射到了Element树上，这可以极大的降低复杂度，提高开发效率**

- 了解Element对理解整个Flutter UI框架是至关重要的，Flutter正是通过`Element`这个纽带将`Widget`和`RenderObject`关联起来，了解Element层会对Flutter UI框架有个清晰的认识，同时也会提高自己的抽象能力和设计能力

在一些特定情况下（例如获取主题`Theme`数据），必须得直接使用Element对象来完成一些操作

# 3. BuildContext

无论是`StatelessWidget`和`StatefulWidget`的`build()`方法都会传一个`BuildContext`对象：

	Widget build(BuildContext context) {}

Flutter中的许多操作都会需要这个`BuildContext`,例如:

	Theme.of(context) //获取主题
	Navigator.push(context, route) //入栈新路由
	Localizations.of(context, type) //获取Local
	context.size //获取上下文大小
	context.findRenderObject() //查找当前或最近的一个祖先RenderObject

`BuildContext`是一个抽象接口类:

	// A handle to the location of a widget in the widget tree
	// 组件树中组件位置的句柄
	abstract class BuildContext {
	    ...
	}

## 3.1 BuildContext的实现类

`StatelessWidget`和`StatefulWidget`的`build()`方法传入的context对象是哪个实现了`BuildContext`的类?这可以通过查看`build()`方法的调用地方来进行查看

- **`build()`方法的调用发生在`StatelessWidget`和`StatefulWidget`对应的`StatelessElement`和`StatefulElement`的`build()`方法中**

以`StatelessElement`为例：

	class StatelessElement extends ComponentElement {

	  StatelessElement(StatelessWidget widget) : super(widget);
	
	  @override
	  Widget build() => widget.build(this);

	  ...
	}

- `StatelessElement`直接将当前对象传给`Widget.build()`，查看`Element`的定义

		abstract class Element extends DiagnosticableTree implements BuildContext{
			....
		}

**得出结论,`StatelessWidget`和`StatefulWidget`的`build()`方法中使用的`BuildContext`就是`Widget`对应的`Element`类**

- 因此可以通过`context`在`StatelessWidget`和`StatefulWidget`的`build()`方法中直接访问`Element`对象

- 获取主题数据的代码`Theme.of(context)`内部正是调用了`Element`的`inheritFromWidgetOfExactType()`方法

- 之所以不直接定义成`Element`而是定义成`BuildContext`是因为[可以得到面向接口编程的好处](https://juejin.im/post/5baaecd8e51d451a3f4c16d1)

# 4. 进阶

**`Element`是Flutter UI框架内部连接`Widget`和`RenderObject`的纽带**，大多数时候开发者只需要关注`Widget`层即可，但是`Widget`层有时候并不能完全屏蔽`Element`细节，所以Framework在`StatelessWidget`和`StatefulWidget`中通过`build()`方法参数将`Element`对象也传递给了开发者，这样便可以在需要时直接操作`Element`对象

俩个思考:

1. 如果没有Widget层，单靠Element层是否可以搭建起一个可用的UI框架？如果可以应该是什么样子？
	
	可以.因为`Widget`树只是`Element`树的映射，完全可以直接通过`Element`来搭建一个UI框架

2. Flutter UI框架能不做成响应式吗？

	可以。Flutter engine提供的dart API是原始且独立的，这个与操作系统提供的API类似，上层UI框架设计成什么样完全取决于设计者，完全可以将UI框架设计成Android风格或iOS风格，但这些事Google不会再去做


## 4.1 仅使用Element来模拟StatefulWidget

假设有一个页面，该页面有一个按钮，按钮的文本是1-9 9个数，点击一次按钮，则对9个数随机排一次序


第一步：实现一个`Element`

	class HomeView extends ComponentElement{
	  HomeView(Widget widget) : super(widget);
	  String text = "123456789";
	
	  @override
	  Widget build() {
	    Color primary=Theme.of(this).primaryColor; //1
	    return GestureDetector(
	      child: Center(
	        child: FlatButton(
	          child: Text(text, style: TextStyle(color: primary),),
	          onPressed: () {
	            var t = text.split("")..shuffle();
	            text = t.join();
	            markNeedsBuild(); //点击后将该Element标记为dirty，Element将会rebuild
	          },
	        ),
	      ),
	    );
	  }
	}

- 上面`build()`方法不接收参数，因为当前对象本身就是`Element`,在代码中需要用到`BuildContext`的地方直接用this代替即可

	例如代码注释1处`Theme.of(this)`参数直接传this即可

- 当text发生改变时，需要调用`markNeedsBuild()`方法将当前Element标记为dirty，标记为dirty的`Element`会在下一帧中重建

	**实际上，`State.setState()`在内部也是调用的`markNeedsBuild()`方法**

- 上面代码中`build()`方法返回的仍然是一个Widget，这是由于Flutter框架中已经有了Widget这一层，并且组件库都已经是以Widget的形式提供了，如果在Flutter框架中所有组件都像示例的HomeView一样以Element形式提供，那么就可以用纯Element来构建UI了，HomeView的build方法返回值类型就可以是Element了


第二步：如果需要将上面代码在现有Flutter框架中跑起来，那么还是得提供一个Widget作为适配器将`HomeView`结合到现有框架中

	class CustomHome extends Widget {
	  @override
	  Element createElement() {
	    return HomeView(this);
	  }
	}

第三步:直接使用`CustomHome`即可











