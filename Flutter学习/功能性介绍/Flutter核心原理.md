# Flutter核心原理
[Flutter 核心原理](https://github.com/flutterchina/flutter-in-action/blob/master/docs/chapter14/index.md)

[Flutter框架分析（三）-- Widget，Element和RenderObject](https://juejin.im/post/5c80efde5188251b8a53b306#heading-1)

# 1. Flutter UI系统

这里的UI系统特指：基于一个平台，在此平台上实现GUI的一个系统，这里的平台特指操作系统，如Android、iOS或者Windows、macOS

- 各个平台UI系统的原理是相通的，也就是说无论是Android还是iOS，它们将一个用户界面展示到屏幕的流程是相似的，所以了解UI系统的基本原理，增加对操作系统和系统底层UI逻辑的了解，可以更好的了解Flutter 的UI系统

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


## 1.3 Flutter UI系统的结构
为了更加清晰的查看Widget树，使用层级最少的Widget

	void main() {
	  runApp(MyWidget());
	}
	
	class MyWidget extends StatelessWidget {
	  final String _message = "Flutter框架分析";
	  @override
	  Widget build(BuildContext context) => ErrorWidget(_message);
	}

使用`Dart DevTools`中的`Flutter Inspector`(注意要Android studio 中的`Flutter Inspector`并不会展示root)查看Widget树结构:

![](http://ww1.sinaimg.cn/large/6ab93b35ly1g4j9eureekj20j208wq3u.jpg)

- 这里的`root`指的是`RenderObjectToWidgetAdapter`

- 图中的Widget树结构为:

		RenderObjectToWidgetAdapter->MyWidget->ErrorWidget

- 图中的Element树结构为:

		RenderObjectToWidgetElement->StatelessElement->LeafRenderObjectElement

- 图中的Render树结构为:

		RenderView->RenderErrorBox
		
	- `RenderView`是Render树的根节点，在`MyWidget`对应的`StatelessElement`中并没有包含`RenderObject`(主要是因为`StatelessWidget`无法生成`RenderObject`)，只有最下面的`ErrorWidget`对应的`LeafRenderObjectElement`才持有第二个`RenderObject`

	![](http://ww1.sinaimg.cn/large/6ab93b35ly1g4j9r10vagj20it0d20ss.jpg)

## 1.4 Widget,Element和RenderObject

**`Widget`是对`Element`的配置或描述**

- Flutter app开发者主要的工作都是在和Widget打交道

- 开发者不需要关心树的维护更新，只需要专注于对`Widget状态`的维护

**`Element`负责维护`element tree`**

- `Element`不会去管具体的颜色，字体大小，显示内容等等这些UI的配置或描述，也不会去管布局，绘制这些事，它只管自己的那棵树

- `Element`的主要工作都处于渲染流水线的构建（build）阶段

**`RenderObject`负责具体布局，绘制这些事情**

- 也就是渲染流水线的布局（layout）和 绘制（paint）阶段


# 2. Widget

基类`Widget`是一个抽象类，其定义如下:

	/// Describes the configuration for an [Element]
	@immutable
	abstract class Widget extends DiagnosticableTree {
	  /// Initializes [key] for subclasses.
	  const Widget({ this.key });
	
	  /// Controls how one widget replaces another widget in the tree
	  final Key key;
	
	  /// Inflates this configuration to a concrete instance.
	  @protected
	  Element createElement();
	
	  /// A short, textual description of this widget.
	  @override
	  String toStringShort() {
	    return key == null ? '$runtimeType' : '$runtimeType-$key';
	  }
	
	  @override
	  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
	    super.debugFillProperties(properties);
	    properties.defaultDiagnosticsTreeStyle = DiagnosticsTreeStyle.dense;
	  }
	
	
	  /// Whether the `newWidget` can be used to update an [Element] that currently
	  /// has the `oldWidget` as its configuration.
	  static bool canUpdate(Widget oldWidget, Widget newWidget) {
	    return oldWidget.runtimeType == newWidget.runtimeType
	        && oldWidget.key == newWidget.key;
	  }
	}

- **其`createElement()`方法负责根据配置去实例化对应的`Element`,其中`StatelessWidget`,`StatefulWidget`,`InheritedWidget`和`RenderObjectWidget`是比较重要的子类**

## 2.1 StatelessWidget

	/// A widget that does not require mutable state
	abstract class StatelessWidget extends Widget {
	  /// Initializes [key] for subclasses.
	  const StatelessWidget({ Key key }) : super(key: key);
	  
	  ///Creates a [StatelessElement] to manage this widget's location in the tree
	  /// It is uncommon for subclasses to override this method
	  @override
	  StatelessElement createElement() => StatelessElement(this);
	
	  /// Describes the part of the user interface represented by this widget.
	  @protected
	  Widget build(BuildContext context);
	}

- **`StatelessWidget`没有生成`RenderObject`的方法,所以`StatelessWidget`只是个中间层，它需要实现`build()`方法来返回子Widget**

## 2.2 StatefulWidget

	/// A widget that has mutable state.
	abstract class StatefulWidget extends Widget {

	  /// Initializes [key] for subclasses.
	  const StatefulWidget({ Key key }) : super(key: key);

	  /// Creates a [StatefulElement] to manage this widget's location in the tree.
  	  /// It is uncommon for subclasses to override this method.
	  @override
	  StatefulElement createElement() => StatefulElement(this);
	
	  /// Creates the mutable state for this widget at a given location in the tree
	  @protected
	  State createState();
	}

- `createElement()`方法返回的是一个`StatefulElement`实例,而方法`createState()`构建对应于这个`StatefulWidget`的`State`

- `StatefulWidget`没有生成`RenderObject`的方法,所以`StatefulWidget`也只是个中间层，它需要对应的`State`实现`build`方法来返回子Widget

### 2.2.1 State

	/// The logic and internal state for a [StatefulWidget]
	abstract class State<T extends StatefulWidget> extends Diagnosticable {
	  /// The current configuration
	  T get widget => _widget;
	  T _widget;
	  
	  /// The current stage in the lifecycle for this state object
	  _StateLifecycle _debugLifecycleState = _StateLifecycle.created;
	  
	  /// Verifies that the [State] that was created is one that expects to be
  	  /// created for that particular [Widget].
	  bool _debugTypesAreRight(Widget widget) => widget is T;
	  
	  /// The location in the tree where this widget builds
	  BuildContext get context => _element;
	  StatefulElement _element;
	
	  /// Whether this [State] object is currently in a tree
	  bool get mounted => _element != null;
	
	  /// Called when this object is inserted into the tree
	  void initState() { }
	
	  /// Called whenever the widget configuration changes
	  void didUpdateWidget(covariant T oldWidget) { }
	
	  /// Notify the framework that the internal state of this object has changed
	  void setState(VoidCallback fn) {
	    final dynamic result = fn() as dynamic;
	    _element.markNeedsBuild();
	  }
	
	  /// Called when this object is removed from the tree
	  void deactivate() { }
	  
	  /// Called when this object is removed from the tree permanently
	  void dispose() { }
	
	  /// Describes the part of the user interface represented by this widget
	  Widget build(BuildContext context);
	  
	  /// Called when a dependency of this [State] object changes
	  void didChangeDependencies() { }
	}

- **`State`持有对应的`Widget`和`Element`**

- 函数`build()`的参数`BuildContex`其实就是Element

- 属性`mounted`是用来判断这个`State`是不是关联到`element tree`中的某个`Element`

	**如果当前State不是在mounted == true的状态，调用`setState()`是会crash的**

- 函数`initState()`会在当前`State`被插入到树时被调用,通常在该方法中进行`State`的初始化工作

- 函数`didUpdateWidget(covariant T oldWidget)`会在配置发生变化时被调用，即`State`换了个新的`Widget`以后会被调用到(`State`对应的`Widget`实例只要是相同类型的是可以被替换的)

- 函数`setState()`方法只是简单执行传入的回调然后调用`_element.markNeedsBuild()`

	**建议在调用`setState()`之前用mounted判断一下,因为这类的`_element`可能为空**
	
	另外要注意的一点是，**这个函数也是触发渲染流水线的一个点**

- 函数`deactivate()`在`State`对应的`Element`被从树中移除后调用，这个移除可能是暂时移除

- 函数`dispose()`在`State`对应的`Element`被从树中永久移除后调用

- 函数`build(BuildContext context)`，描述Widget所代表的用户界面部分

- 函数`didChangeDependencies()`，State的依赖发生变化的时候被调用


## 2.3 InheritedWidget

**`InheritedWidget`的作用是向下传递数据**。在`InheritedWidget`之下的子节点都可以通过调用`BuildContext.inheritFromWidgetOfExactType()`来获取这个`InheritedWidget`

	abstract class InheritedWidget extends ProxyWidget {
	  const InheritedWidget({ Key key, Widget child })
	    : super(key: key, child: child);
	
	  @override
	  InheritedElement createElement() => InheritedElement(this);
	
	  /// Whether the framework should notify widgets that inherit from this widget
	  @protected
	  bool updateShouldNotify(covariant InheritedWidget oldWidget);
	}

- `createElement()`函数返回的是一个`InheritedElement`


## 2.4 RenderObjectWidget
`RenderObjectWidget`用来配置`RenderObject`,当配置发生变化需要应用到现有的`RenderObject`上的时候，Flutter框架会调用方法`updateRenderObject()`来把新的配置设置给相应的`RenderObject`

	abstract class RenderObjectWidget extends Widget {
	
	  const RenderObjectWidget({ Key key }) : super(key: key);
	
	  @override
	  RenderObjectElement createElement();
	
	  /// Creates an instance of the [RenderObject] class that this
	  /// [RenderObjectWidget] represents, using the configuration described by this
	  /// [RenderObjectWidget]
	  @protected
	  RenderObject createRenderObject(BuildContext context);

	  /// Copies the configuration described by this [RenderObjectWidget] to the
	  /// given [RenderObject], which will be of the same type as returned by this
	  /// object's [createRenderObject]
	  @protected
	  void updateRenderObject(BuildContext context, covariant RenderObject renderObject) { }
	
	  @protected
	  void didUnmountRenderObject(covariant RenderObject renderObject) { }
	}


- 函数`createElement()`返回值是`RenderObjectElement`,具体逻辑由其子类实现

- 函数`createRenderObject()`是用来实例化`RenderObject`

- **`RenderObjectWidget`有三个比较重要的子类**：

	1. `LeafRenderObjectWidget`:处于Widget树的最底层，没有子节点,对应`LeafRenderObjectElement`

	2. `SingleChildRenderObjectWidget`:只含有一个子节点,对应`SingleChildRenderObjectElement`

	3. `MultiChildRenderObjectWidget`:含有多个子节点,对应`MultiChildRenderObjectElement`

# 3. Element

**`Widget`只是描述`Element`的一个配置,`Element`类主要来维护`element`树**

- Flutter中最底层的UI树实际上是由一个个独立的`Element`节点构成,`Widget`最终的`Layout`、渲染都是通过`RenderObject`来完成的

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

## 3.1 Element类
	
	abstract class Element extends DiagnosticableTree implements BuildContext {
	    Element _parent;
	    Widget _widget;
	    BuildOwner _owner;
	    dynamic _slot;
	    
	    void visitChildren(ElementVisitor visitor) { }
	    
	    Element updateChild(Element child, Widget newWidget, dynamic newSlot) {
	        
	    }
	    
	    void mount(Element parent, dynamic newSlot) {
	        
	    }
	    
	    void unmount() {
	         
	    }
	    
	    void update(covariant Widget newWidget) {
	        
	    }
	    
	    @protected
	    Element inflateWidget(Widget newWidget, dynamic newSlot) {
	    ...
	      final Element newChild = newWidget.createElement();
	      newChild.mount(this, newSlot);
	      return newChild;
	    }
	  
	    void markNeedsBuild() {
	      if (dirty)
	        return;
	      _dirty = true;
	      owner.scheduleBuildFor(this);
	    }
	    
	    void rebuild() {
	      if (!_active || !_dirty)
	        return;
	      performRebuild();
	    }
	  
	    @protected
	    void performRebuild();
	}

- `Element`拥有一个当前的`Widget _widget`和一个`BuildOwner _owner`(`BuildOwner`是在`WidgetsBinding`里实例化的)

	`Element`是树状结构，它会持有父节点`_parent`
	
- `_slot`由父`Element`设置，目的是告诉当前`Element`在父节点中的位置
	
- **函数`visitChildren()`定义了`Element`可以遍历子节点，但是具体的遍历行为是由子类实现**

- **函数`updateChild()`用来更新一个孩子节点,有四种情况**：

	1. 新Widget为空，老Widget也为空。则啥也不做

	2. 新Widget为空，老Widget不为空。这个Element被移除

	3. 新Widget不为空，老Widget为空。则调用`inflateWidget()`以这个Wiget为配置实例化一个Element

	4. 新Widget不为空，老Widget不为空。调用`update()`函数更新子`Element`(`update()`函数由子类实现)

- 函数`mount()`会在新`Element`被实例化以后调用，用来把自己加入`element tree`

	`Element`被移除的时候会调用`unmount()`,用来退出`element tree`

- 函数`markNeedsBuild()`用来标记`Element`为"脏"(dirty)状态,**表明渲染下一帧的时候这个Element需要被重建**

- 函数`rebuild()`在渲染流水线的构建（build）阶段被调用。具体的重建在函数`performRebuild()`中，由Element子类实现

## 3.2 Element的生命周期

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

## 3.3 BuildContext

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

### 3.3.1 BuildContext的实现类

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

## 3.4 ComponentElement
`ComponentElement`表示当前这个`Element`是用来组合其他`Element`的,其实一个抽象类，继承自`Element`

	abstract class ComponentElement extends Element {
	  ComponentElement(Widget widget) : super(widget);
	
	  Element _child;
	
	  @override
	  void performRebuild() {
	    Widget built;
	    built = build();
	    _child = updateChild(_child, built, slot);
	  }
	
	  Widget build();
	}

- 属性`_child`是其子节点

- 函数`performRebuild()`中会调用`build()`来实例化一个Widget(`build()`函数由其子类实现)


## 3.5 StatelessElement

`StatelessElement`对应的Widget是`StatelessWidget`

	class StatelessElement extends ComponentElement {
	
	  @override
	  Widget build() => widget.build(this);
	
	  @override
	  void update(StatelessWidget newWidget) {
	    super.update(newWidget);
	    _dirty = true;
	    rebuild();
	  }
	}

- **函数`build()`直接调用的就是`StatelessWidget.build()`**

	**`build()`函数的入参是`this`,所以`StatelessWidget.build()`的参数`BuildContext`其实就是这个`StatelessElement`**

## 3.6 StatefullElement

`StatefulElement`对应的Widget是`StatefulWidget`

	class StatefulElement extends ComponentElement {
	  /// Creates an element that uses the given widget as its configuration.
	  StatefulElement(StatefulWidget widget)
	      : _state = widget.createState(),
	        super(widget) {
	    _state._element = this;
	    _state._widget = widget;
	  }
	
	  @override
	  Widget build() => state.build(this);
	  
	   @override
	  void _firstBuild() {
	    final dynamic debugCheckForReturnedFuture = _state.initState() 
	    _state.didChangeDependencies();
	    super._firstBuild();
	  }
	
	  @override
	  void deactivate() {
	    _state.deactivate();
	    super.deactivate();
	  }
	
	  @override
	  void unmount() {
	    super.unmount();
	    _state.dispose();
	    _state._element = null;
	    _state = null;
	  }
	
	  @override
	  void didChangeDependencies() {
	    super.didChangeDependencies();
	    _state.didChangeDependencies();
	  }
	}

- 在`StatefulElement`的构造函数中会调用对应`StatefulWidget`的`createState()`函数

	也就是说`State`是在实例化`StatefulElement`的时候被实例化的,并且`State`实例会被这个`StatefulElement`实例持有。从这里也可以看出为什么`StatefulWidget`的状态要由单独的`State`管理，每次刷新的时候可能会有一个新的StatefulWidget被创建，但是State实例是不变的

- 函数`build()`函数中调用了`State.build(this)`

	函数`build()`的入参是this,所以`StatefullWidget.build()`的参数`BuildContext`其实就是这个`StatefullElement`

- **`State`持有状态，当状态改变时对应的回调函数会被调用。实际上这些回调函数其实都是在`StatefulElement`里被调用的**

- 函数`_firstBuild()`中会调用`State.initState()`和`State.didChangeDependencies()`

- 函数`deactivate()`中会调用`State.deactivate()`

- 函数`unmount()`中会调用`State.dispose()`

- 函数`didChangeDependencies()`中会调用`State.didChangeDependencies()`

## 3.7 InheritedElement
`InheritedElement`对应的Widget是`InheritedWidget`

- **其内部实现主要是在维护对其有依赖的子Element的Map，以及在需要的时候调用子`Element`对应的`didChangeDependencies()`回调**

## 3.8 RenderObjectElement

`RenderObjectElement`对应的Widget是`RenderObjectWidget`

	
	abstract class RenderObjectElement extends Element {
	  RenderObject _renderObject;
	  
	  @override
	  void mount(Element parent, dynamic newSlot) {
	    super.mount(parent, newSlot);
	    _renderObject = widget.createRenderObject(this);
	    attachRenderObject(newSlot);
	    _dirty = false;
	  }
	  
	  @override
	  void unmount() {
	    super.unmount();
	    widget.didUnmountRenderObject(renderObject);
	  }
	  
	  @override
	  void update(covariant RenderObjectWidget newWidget) {
	    super.update(newWidget);
	    widget.updateRenderObject(this, renderObject);
	    _dirty = false;
	  }
	  
	  @override
	  void performRebuild() {
	    widget.updateRenderObject(this, renderObject);
	    _dirty = false;
	  }
	  
	  @protected
	  void insertChildRenderObject(covariant RenderObject child, covariant dynamic slot);
	
	  @protected
	  void moveChildRenderObject(covariant RenderObject child, covariant dynamic slot);
	
	  @protected
	  void removeChildRenderObject(covariant RenderObject child);
	
	}


- 函数`mount()`被调用的时候会调用`RenderObjectWidget.createRenderObject()`来实例化`RenderObject`

- 函数`update()`和`performRebuild()`被调用的时候会调用`RenderObjectWidget.updateRenderObject()`

- 函数`unmount()`被调用的时候会调用`RenderObjectWidget.didUnmountRenderObject()`



# 4. Element进阶

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

# 5. RenderObject和RenderBox

**`RenderObject`的主要职责是渲染流水线的Layout和绘制，同时还会维护由`RenderObject`组成一棵渲染树`Render Tree`**

- 每个`Element`都对应一个`RenderObject`，可以通过`Element.renderObject` 来获取

`RenderObject`就是渲染树中的一个对象，它拥有一个`parent`和一个`parentData`插槽（slot），所谓插槽，就是指预留的一个接口或位置，这个接口和位置是由其它对象来接入或占据的，这个接口或位置在软件中通常用预留变量来表示，而`parentData`正是一个预留变量，它正是由`parent` 来赋值的，`parent`通常会通过子`RenderObject`的`parentData`存储一些和子元素相关的数据

- 例如在`Stack`布局中，`RenderStack`就会将子元素的偏移数据存储在子元素的`parentData`中（具体可以查看Positioned实现）

`RenderObject`类本身实现了一套基础的layout和绘制协议，但是并没有定义子节点模型（如一个节点可以有几个子节点，没有子节点？一个？两个？或者更多？）。 它也没有定义坐标系统（如子节点定位是在笛卡尔坐标中还是极坐标？）和具体的布局协议（是通过宽高还是通过constraint和size?，或者是否由父节点在子节点布局之前或之后设置子节点的大小和位置等）

为此，**Flutter提供了一个抽象类`RenderBox`，它继承自`RenderObject`，布局坐标系统采用笛卡尔坐标系，这和Android和iOS原生坐标系是一致的，都是屏幕的top、left是原点，然后分宽高两个轴，大多数情况下，直接使用`RenderBox`就可以了，除非遇到要自定义布局模型或坐标系统的情况**


## 5.1 布局过程

在 `RenderBox` 中，有个`size`属性用来保存控件的宽和高

- `RenderBox`的layout是通过在组件树中从上往下传递`BoxConstraints`对象的实现的

	`BoxConstraints`对象可以限制子节点的最大和最小宽高，子节点必须遵守父节点给定的限制条件

在布局阶段，父节点会调用子节点的`layout()`方法，下面看看`RenderObject`中`layout()`方法的大致实现（删掉了一些无关代码和异常捕获）:

	void layout(Constraints constraints, { bool parentUsesSize = false }) {
	   ...
	   RenderObject relayoutBoundary; 
	    if (!parentUsesSize || sizedByParent || constraints.isTight 
	    	|| parent is! RenderObject) {
	      relayoutBoundary = this;
	    } else {
	      final RenderObject parent = this.parent;
	      relayoutBoundary = parent._relayoutBoundary;
	    }
	    ...
	    if (sizedByParent) {
	        performResize();
	    }
	    performLayout();
	    ...
	}
`layout()`方法需要传入两个参数

- **第一个参数是 `constraints`**:即父节点对子节点大小的限制，该值根据父节点的布局逻辑确定

- **第二个参数是 `parentUsesSize`**:该值用于确定 `relayoutBoundary`，该参数表示子节点布局变化是否影响父节点，如果为true，当子节点布局发生变化时父节点都会标记为需要重新布局，如果为false，则子节点布局发生变化后不会影响父节点

### 5.1.1 relayoutBoundary

**`RenderObject`源码中定义了一个`RenderObject`类型的`_relayoutBoundary`实例变量**

`Element`通过`markNeedsBuild()`方法来标记`Element`为dirty的,当一个`Element`被标记为 `dirty` 时便会重新build，这时 `RenderObject`便会重新布局

- 在 `RenderObject`中有一个类似的`markNeedsLayout()`方法，它会将 `RenderObject` 的布局状态标记为 dirty，这样在下一个frame中便会重新layout

`RenderObject`的`markNeedsLayout()`的源码：
	
	  void markNeedsLayout() {
	    assert(_debugCanPerformMutations);
	    if (_needsLayout) {
	      assert(_debugSubtreeRelayoutRootAlreadyMarkedNeedsLayout());
	      return;
	    }
	    assert(_relayoutBoundary != null);
	    if (_relayoutBoundary != this) {
	      // 调用父类的markNeedsLayout()
	      markParentNeedsLayout();
	    } else {
	      // 需要进行重新布局
	      _needsLayout = true;
	      if (owner != null) {
	        assert(() {
	          if (debugPrintMarkNeedsLayoutStacks)
	            debugPrintStack(label: 'markNeedsLayout() called for $this');
	          return true;
	        }());
	        owner._nodesNeedingLayout.add(this);
	        owner.requestVisualUpdate();
	      }
	    }
	  }

- 代码大致逻辑是先判断自身是不是 `_relayoutBoundary`实例，如果不是就继续向`parent`查找，一直向上查找到是 `_relayoutBoundary` 的`RenderObject`为止,将其标记为 dirty

	换句话说就是如果一个控件的大小被改变时可能会影响到它的 `parent`，那么`parent` 也需要被重新布局，直到其父类`RenderObject` 是 `_relayoutBoundary`，就表示该`RenderObject`的大小变化不会再影响到 parent 的大小了，于是 parent 也就不用重新布局了

### 5.1.2 performResize 和 performLayout

`RenderBox`实际的测量和布局逻辑是在`performResize()` 和 `performLayout()`两个方法中,`RenderBox`子类需要实现这两个方法来定制自身的布局逻辑

	  @override
	  void performResize() {
	    // default behavior for subclasses that have sizedByParent = true
	    size = constraints.smallest;
	    assert(size.isFinite);
	  }
	
	  @override
	  void performLayout() {
	    assert(() {
	      if (!sizedByParent) {
	        throw FlutterError(
	          '$runtimeType did not implement performLayout().\n'
	          'RenderBox subclasses need to either override performLayout() to '
	          'set a size and lay out any children, or, set sizedByParent to true '
	          'so that performResize() sizes the render object.'
	        );
	      }
	      return true;
	    }());
	  }



**查看`layout()` 源码,`performResize()`方法只有在`sizedByParent`为 true时才会执行，而 `performLayout() `是每次布局都会被调用的**

- **`sizedByParent` 意为该节点的大小是否仅通过 parent 传给它的 `constraints`就可以确定了，即该节点的大小与它自身的属性和其子节点无关**

	例如如果一个控件永远充满 parent 的大小，那么` sizedByParent` 就应该返回 true，此时其大小在 `performResize() `中就确定了，在后面的 `performLayout()` 方法中将不会再被修改了，这种情况下 `performLayout()` 只负责布局子节点

**在 `performLayout()` 方法中除了完成自身布局，也必须完成子节点的布局，这是因为只有父子节点全部完成后布局流程才算真正完成。所以最终的调用栈将会变成：**

	layout() > performResize()/performLayout() > child.layout() > ... 
	
- 如此递归完成整个UI的布局

`RenderBox`子类要定制布局算法不应该重写`layout()`方法，因为对于任何`RenderBox`的子类来说，它的layout流程基本是相同的，不同之处只在具体的布局算法，而具体的布局算法子类应该通过重写`performResize()` 和 `performLayout()`两个方法来实现，这俩个方法会在`layout()`中被调用

### 5.1.3 ParentData

当layout结束后，每个节点的位置（相对于父节点的偏移）就已经确定了，`RenderObject`就可以根据位置信息来进行最终的绘制,但是还需要保存子节点的位置信息

那么在layout过程中，节点的位置信息怎么保存？

- 对于大多数`RenderBox`子类来说如果子类只有一个子节点，那么子节点偏移一般都是`Offset.zero` ,如果有多个子节点，则每个子节点的偏移就可能不同。**而子节点在父节点的偏移数据正是通过`RenderObject`的`parentData`属性来保存的**

	在`RenderBox`中，其`parentData`属性默认是一个`BoxParentData`对象，该属性只能通过父节点的`setupParentData()`方法来设置：

		abstract class RenderBox extends RenderObject {
		  @override
		  void setupParentData(covariant RenderObject child) {
		    if (child.parentData is! BoxParentData)
		      child.parentData = BoxParentData();
		  }
		  ...
		}

### 5.1.4 BoxParentData

	/// Parentdata 会被RenderBox和它的子类使用.
	class BoxParentData extends ParentData {
	  /// offset表示在子节点在父节点坐标系中的绘制偏移  
	  Offset offset = Offset.zero;
	
	  @override
	  String toString() => 'offset=$offset';
	}

- **`RenderObject`的`parentData` 只能通过父元素设置**

`ParentData`并不仅仅可以用来存储偏移信息，通常所有和子节点特定的数据都可以存储到子节点的`ParentData`中

- 例如`ContainerBox`的`ParentData`就保存了指向兄弟节点的`previousSibling`和`nextSibling`，`Element.visitChildren()`方法也正是通过它们来实现对子节点的遍历。再比如`KeepAlive` 组件，它使用`KeepAliveParentDataMixin`（继承自ParentData） 来保存子节的keepAlive状态


## 5.2 绘制过程

**`RenderObject`可以通过`paint()`方法来完成具体绘制逻辑，流程和布局流程相似，子类可以实现`paint()`方法来完成自身的绘制逻辑**

	void paint(PaintingContext context, Offset offset) { }

- 通过`context.canvas`可以取到Canvas对象，而`Canvas`提供了API来实现具体的绘制逻辑

**如果节点有子节点，它除了完成自身绘制逻辑之外，还要调用子节点的绘制方法** . 以`Flex`组件对应的`RenderFlex`对象的`pain()`方法为例:

	@override
	void paint(PaintingContext context, Offset offset) {
	
	  // 如果子元素未超出当前边界，则绘制子元素  
	  if (_overflow <= 0.0) {
	    defaultPaint(context, offset);
	    return;
	  }
	
	  // 如果size为空，则无需绘制
	  if (size.isEmpty)
	    return;
	
	  // 剪裁掉溢出边界的部分
	  context.pushClipRect(needsCompositing, offset, Offset.zero & size, defaultPaint);
	
	  assert(() {
	    final String debugOverflowHints = '...'; //溢出提示内容，省略
	    // 绘制溢出部分的错误提示样式
	    Rect overflowChildRect;
	    switch (_direction) {
	      case Axis.horizontal:
	        overflowChildRect = Rect.fromLTWH(0.0, 0.0, size.width + _overflow, 0.0);
	        break;
	      case Axis.vertical:
	        overflowChildRect = Rect.fromLTWH(0.0, 0.0, 0.0, size.height + _overflow);
	        break;
	    }  
	    paintOverflowIndicator(context, offset, Offset.zero & size,
	                           overflowChildRect, overflowHints: debugOverflowHints);
	    return true;
	  }());
	}

- 首先判断有无溢出，如果没有则调用`defaultPaint(context, offset)`来完成绘制

		void defaultPaint(PaintingContext context, Offset offset) {
		  ChildType child = firstChild;
		  while (child != null) {
		    final ParentDataType childParentData = child.parentData;
		    //绘制子节点
		    context.paintChild(child, childParentData.offset + offset);
		    child = childParentData.nextSibling;
		  }
		}

	- 由于`Flex`本身没有需要绘制的东西，所以直接遍历其子节点，然后调用`paintChild()`来绘制子节点，同时将子节点ParentData中在layout阶段保存的offset加上自身偏移作为第二个参数传递给`paintChild()`。而如果子节点还有子节点时，`paintChild()`方法还会调用子节点的`paint()`方法，如此递归完成整个节点树的绘制，最终调用栈为： 

			paint() > paintChild() > paint() ... 。

- 当需要绘制的内容大小溢出当前空间时，将会执行`paintOverflowIndicator()` 来绘制溢出部分提示，这个就是经常看到的溢出提示

	![](http://ww1.sinaimg.cn/large/6ab93b35ly1g4eegefpgkj207u02sjrd.jpg)


## 5.3 RepaintBoundary

与`RelayoutBoundary`相似，`RepaintBoundary`是用于在确定重绘边界的，但是与`RelayoutBoundary`不同的是，这个绘制边界需要由开发者通过`RepaintBoundary`组件自己指定

	CustomPaint(
	  size: Size(300, 300), //指定画布大小
	  painter: MyPainter(),
	  child: RepaintBoundary(
	    child: Container(...),
	  ),
	)

### 5.3.1 `RepaintBoundary`的原理

`RenderObject`有一个`isRepaintBoundary `属性，该属性决定这个`RenderObject`重绘时是否独立于其父元素，如果该属性值为true ，则独立绘制，反之则一起绘制

那独立绘制是怎么实现的呢？ 答案就在`paintChild()`源码中：

	void paintChild(RenderObject child, Offset offset) {
	  ...
	  if (child.isRepaintBoundary) {
	    stopRecordingIfNeeded();
	    _compositeChild(child, offset);
	  } else {
	    child._paintWithContext(this, offset);
	  }
	  ...
	}

在绘制子节点时，如果`child.isRepaintBoundary` 为 true则会调用`_compositeChild()`方法

	void _compositeChild(RenderObject child, Offset offset) {
	  // 给子节点创建一个layer ，然后再上面绘制子节点 
	  if (child._needsPaint) {
	    repaintCompositedChild(child, debugAlsoPaintedParent: true);
	  } else {
	    ...
	  }
	  assert(child._layer != null);
	  child._layer.offset = offset;
	  appendLayer(child._layer);
	}

- 独立绘制是通过在不同的layer（层）上绘制的。所以正确的使用`isRepaintBoundary`属性可以提高绘制效率，避免不必要的重绘

具体原理是：和触发重新build和layout类似，`RenderObject`也提供了一个`markNeedsPaint()`方法:

	void markNeedsPaint() {
	 ...
	  //如果RenderObject.isRepaintBoundary 为true,则该RenderObject拥有layer，直接绘制  
	  if (isRepaintBoundary) {
	    ...
	    if (owner != null) {
	      //找到最近的layer，绘制  
	      owner._nodesNeedingPaint.add(this);
	      owner.requestVisualUpdate();
	    }
	  } else if (parent is RenderObject) {
	    // 没有自己的layer, 会和一个祖先节点共用一个layer  
	    assert(_layer == null);
	    final RenderObject parent = this.parent;
	    // 向父级递归查找  
	    parent.markNeedsPaint();
	    assert(parent == this.parent);
	  } else {
	    // 如果直到根节点也没找到一个Layer，那么便需要绘制自身，因为没有其它节点可以绘制根节点。  
	    if (owner != null)
	      owner.requestVisualUpdate();
	  }
	}

- 当调用`markNeedsPaint()`方法时，会从当前`RenderObject`开始一直向父节点查找，直到找到 一个`isRepaintBoundary`为 true的`RenderObject`时，才会触发重绘，这样便可以实现局部重绘。当出现`RenderObject`绘制的很频繁或很复杂时，可以通过`RepaintBoundary`组件来指定`isRepaintBoundary`为 true，这样在绘制时仅会重绘自身而无需重绘它的 parent，如此便可提高性能

通过`RepaintBoundary`组件是如何设置`isRepaintBoundary`属性呢？

- 其实如果使用了`RepaintBoundary`组件，其对应的`RenderRepaintBoundary`会自动将`isRepaintBoundary`设为true的

		class RenderRepaintBoundary extends RenderProxyBox {
		  /// Creates a repaint boundary around [child].
		  RenderRepaintBoundary({ RenderBox child }) : super(child);
		
		  @override
		  bool get isRepaintBoundary => true;
		}


# 6. 命中测试

**一个对象是否可以响应事件，取决于其对命中测试的返回，当发生用户事件时，会从根节点（`RenderView`）开始进行命中测试,即调用`RednerView`的`hitTest()`方法**

**`hitTest()`方法用来判断该`RenderObject`是否在被点击的范围内，同时负责将被点击的`RenderBox`添加到 `HitTestResult`列表中**，参数`position`为事件触发的坐标（如果有的话)

- `hitTest()`方法的返回值为`true`则表示有`RenderBox`通过了命中测试，需要响应事件，反之则认为当前`RenderBox`没有命中

**在继承`RenderBox`时，可以直接重写`hitTest()`方法，也可以重写 `hitTestSelf()` 或 `hitTestChildren()`, 唯一不同的是 `hitTest()`中需要将通过命中测试的节点信息添加到命中测试结果列表中，而`hitTestSelf() `和` hitTestChildren()`则只需要简单的返回true或false**

下面是`RenderView`的`hitTest()`源码：

	bool hitTest(HitTestResult result, { Offset position }) {
	  if (child != null)
	    child.hitTest(result, position: position); //递归子RenderBox进行命中测试
	  result.add(HitTestEntry(this)); //将测试结果添加到result中
	  return true;
	}

- `RenderView.child`类型是`RenderBox`,所以在递归子类进行命中测试时就是调用的`RenderBox.hitTest()`:

		bool hitTest(HitTestResult result, { @required Offset position }) {
		  ...  
		  if (_size.contains(position)) {
		    if (hitTestChildren(result, position: position) || hitTestSelf(position)) {
		      result.add(BoxHitTestEntry(this, position));
		      return true;
		    }
		  }
		  return false;
		}

`RenderBox.hitTest()`方法默认的实现里调用了`hitTestSelf()`和`hitTestChildren()`两个方法，这两个方法默认实现如下：

	@protected
	bool hitTestSelf(Offset position) => false;
	 
	@protected
	bool hitTestChildren(HitTestResult result, { Offset position }) => false;


# 7. 语义化

语义化即`Semantics`，主要是提供给读屏软件的接口，也是实现辅助功能的基础，通过语义化接口可以让机器理解页面上的内容，对于有视力障碍用户可以使用读屏软件来理解UI内容。如果一个`RenderObject`要支持语义化接口，可以实现 `describeApproximatePaintClip`和 `visitChildrenForSemantics`方法和`semanticsAnnotator getter`。更多关于语义化的信息可以查看API文档。


# 8. `RenderObject`总结

如果要从头到尾实现一个`RenderObject`是比较麻烦的，必须去实现layout、绘制和命中测试逻辑，但是大多数时候可以直接在Widget层通过组合或者CustomPaint完成自定义UI

如果遇到只能定义一个新`RenderObject`的场景时（如要实现一个新的layout算法的布局容器），可以直接继承自`RenderBox`，这样可以减少一部分工作