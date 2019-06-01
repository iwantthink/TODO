# Flutter基础widget

[基础widget](https://github.com/flutterchina/flutter-in-action/blob/master/docs/chapter3/flutter_widget_intro.md)


# 1. Widget简介

## 1.1 概念
Flutter中几乎所有的对象都是一个Widget，与原生开发中“控件”不同的是，Flutter中的widget的概念更广泛，它不仅可以表示UI元素，也可以表示一些功能性的组件如：用于手势检测的 GestureDetector widget、用于应用主题数据传递的Theme等等。而原生开发中的控件通常只是指UI元素。

由于Flutter主要就是用于构建用户界面的，所以，在大多数时候，可以认为widget就是一个控件，不必纠结于概念


## 1.2 Widget和Element

**在Flutter中，Widget的功能是“描述一个UI元素的配置数据”**，也就是说，Widget其实并不是表示最终绘制在设备屏幕上的显示元素，而只是显示元素的一个配置数据

**实际上，Flutter中真正代表屏幕上显示元素的类是Element，也就是说Widget只是描述Element的一个配置**

- Widget实际上就是Element的配置数据，Widget树实际上是一个配置树，而真正的UI渲染树是由Element构成；不过，由于Element是通过Widget生成，所以它们之间有对应关系，所以在大多数场景中，可以宽泛地认为Widget树就是指UI控件树或UI渲染树

- 一个Widget对象可以对应多个Element对象。并且根据同一份配置（Widget），可以创建多个实例（Element）

## 1.3 主要接口

Widget类本身是一个抽象类，其中最核心的就是定义了`createElement()`接口，**在Flutter开发中，一般都不用直接继承Widget类来实现Widget，而是会通过继承`StatelessWidget`和`StatefulWidget`来间接继承Widget类来实现**

- `StatelessWidget`和`StatefulWidget`都是直接继承自Widget类，而这两个类也正是Flutter中非常重要的两个抽象类，它们引入了两种Widget模型


Widget定义:

	@immutable
	abstract class Widget extends DiagnosticableTree {
	  const Widget({ this.key });
	  final Key key;
	    
	  @protected
	  Element createElement();
	
	  @override
	  String toStringShort() {
	    return key == null ? '$runtimeType' : '$runtimeType-$key';
	  }
	
	  @override
	  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
	    super.debugFillProperties(properties);
	    properties.defaultDiagnosticsTreeStyle = DiagnosticsTreeStyle.dense;
	  }
	  
	  static bool canUpdate(Widget oldWidget, Widget newWidget) {
	    return oldWidget.runtimeType == newWidget.runtimeType
	        && oldWidget.key == newWidget.key;
	  }
	}


- Widget类继承自`DiagnosticableTree`,即“诊断树”，**主要作用是提供调试信息**

- `Key`属性类似于React/Vue中的key，**主要的作用是决定是否在下一次build时复用旧的widget**，决定的条件在`canUpdate()`方法中

	为Widget显式添加key的话可能（但不一定）会使UI在重新构建时变的高效

- `createElement()`：正如前文所述“一个Widget可以对应多个Element”；**Flutter Framework在构建UI树时，会先调用此方法生成对应节点的Element对象**。此方法是Flutter Framework隐式调用的，不需要开发者手动调用

- `debugFillProperties(...)`是复写父类的方法，**主要是设置诊断树的一些特性**

- `canUpdate(...)`是一个静态方法，它主要用于在Widget树重新build时复用旧的widget，也就是说：**是否用新的Widget对象去更新旧UI树上所对应的Element对象的配置**

	通过其源码可以看到，只要`newWidget`与`oldWidget`的`runtimeType`和`key`同时相等时就会用newWidget去更新Element对象的配置，否则就会创建新的Element


# 2. StatelessWidget
`StatelessWidget`继承自Widget，重写了`createElement() `方法：

	abstract class StatelessWidget extends Widget {
	  /// Initializes [key] for subclasses.
		const StatelessWidget({ Key key }) : super(key: key);
	
		@override
		StatelessElement createElement() => new StatelessElement(this);
		
		@protected
		Widget build(BuildContext context);
	}

- `StatelessElement`间接继承自Element类，StatelessWidget作为其配置数据

`StatelessWidget`适用于不需要维护状态的场景，它通常在`build()`方法中通过嵌套其它Widget来构建UI，在构建过程中会递归的构建其嵌套的Widget

	class Echo extends StatelessWidget {
	  const Echo({
	    Key key,  
	    @required this.text,
	    this.backgroundColor:Colors.grey,
	  }):super(key:key);
	    
	  final String text;
	  final Color backgroundColor;
	
	  @override
	  Widget build(BuildContext context) {
	    return Center(
	      child: Container(
	        color: backgroundColor,
	        child: Text(text),
	      ),
	    );
	  }
	}

- **按照惯例，Widget的构造函数应使用命名参数，命名参数中的必要参数要添加`@required`标注，这样有利于静态代码分析器进行检查**

- **按照惯例,在继承widget时，第一个参数通常应该是Key，如果接受子widget的child参数，那么通常应该将它放在参数列表的最后**

- **按照惯例，widget的属性应被声明为final，防止被意外改变**

# 3. StatefulWidget
`StatefulWidget`继承自widget类，并重写了`createElement() `方法，不同于`StatelessWidget`的是其返回的`Element`对象并不相同。另外`StatefulWidget`类中添加了一个新的接口`createState()`

`StatefulWidget`的类定义：

	abstract class StatefulWidget extends Widget {
	  const StatefulWidget({ Key key }) : super(key: key);
	    
	  @override
	  StatefulElement createElement() => new StatefulElement(this);
	    
	  @protected
	  State createState();
	}

- `StatefulElement`间接继承自`Element`类，`StatefulWidget`作为其配置数据

	`StatefulElement` 中可能会多次调用`createState()`来创建状态(State)对象

- `createState()` 用于创建和`StatefulWidget`相关的状态，它在`StatefulWidget`的生命周期中可能会被多次调用

	例如，当一个StatefulWidget同时插入到widget树的多个位置时，Flutter framework就会调用该方法为每一个位置生成一个独立的State实例，其实，本质上就是一个StatefulElement对应一个State实例
	

## 3.1 State

一个`StatefulWidget`类会对应一个`State`类，State表示与其对应的StatefulWidget要维护的状态

State中的保存的状态信息可以：

1. 在widget被build时可以被同步读取

2. 在widget生命周期中可以被改变，**当State被改变时，可以手动调用其`setState()`方法通知Flutter framework状态发生改变，Flutter framework在收到消息后，会重新调用其`build()`方法重新构建widget树，从而达到更新UI的目的**

State中有两个常用属性：

1. `widget`:

	**它表示与该State实例关联的widget实例，由Flutter framework动态设置**。注意，这种关联并非永久的，因为在应用生命周期中，UI树上的某一个节点的widget实例在重新构建时可能会变化，但State实例只会在第一次插入到树中时被创建，当在重新构建时，如果widget被修改了，Flutter framework会动态设置`State.widget`为新的widget实例

2. `context`:

	**它是BuildContext类的一个实例，表示构建widget的上下文**，是操作widget在树中位置的一个句柄，它包含了一些查找、遍历当前Widget树的一些方法。**每一个widget都有一个自己的context对象**

## 3.2 State生命周期

理解State的生命周期对flutter开发非常重要，在接下来的示例中通过实现一个计数器widget来了解State的生命周期，点击它可以使计数器加1，由于要保存计数器的数值状态，所以应继承StatefulWidget，代码如下：

	class CounterWidget extends StatefulWidget {
	  const CounterWidget({
	    Key key,
	    this.initValue: 0
	  });
	  // 表示计数器的初始值
	  final int initValue;
	
	  @override
	  _CounterWidgetState createState() => new _CounterWidgetState();
	}

	class _CounterWidgetState extends State<CounterWidget> {  
	  int _counter;
	
	  @override
	  void initState() {
	    super.initState();
	    //初始化状态  
	    _counter=widget.initValue;
	    print("initState");
	  }
	
	  @override
	  Widget build(BuildContext context) {
	    print("build");
	    return Scaffold(
	      body: Center(
	        child: FlatButton(
	          child: Text('$_counter'),
	          //点击后计数器自增
	          onPressed:()=>setState(()=> ++_counter,
	          ),
	        ),
	      ),
	    );
	  }
	
	  @override
	  void didUpdateWidget(CounterWidget oldWidget) {
	    super.didUpdateWidget(oldWidget);
	    print("didUpdateWidget");
	  }
	
	  @override
	  void deactivate() {
	    super.deactivate();
	    print("deactive");
	  }
	
	  @override
	  void dispose() {
	    super.dispose();
	    print("dispose");
	  }
	
	  @override
	  void reassemble() {
	    super.reassemble();
	    print("reassemble");
	  }
	
	  @override
	  void didChangeDependencies() {
	    super.didChangeDependencies();
	    print("didChangeDependencies");
	  }
	}

- `CounterWidget`作为新路由被打开时：

		I/flutter ( 5436): initState
		I/flutter ( 5436): didChangeDependencies
		I/flutter ( 5436): build

- 热重载打开时:

		I/flutter ( 5436): reassemble
		I/flutter ( 5436): didUpdateWidget
		I/flutter ( 5436): build

- widget树中移除`CounterWidget`

		Widget build(BuildContext context) {
		  //移除计数器 
		  //return CounterWidget();
		  //随便返回一个Text()
		  return Text("xxx");
		}

	此时进行热重载打开:
	
		I/flutter ( 5436): reassemble
		I/flutter ( 5436): deactive
		I/flutter ( 5436): dispose

### 3.2.1 生命周期回调函数
**注意：在继承`StatefulWidget`重写其方法时，对于包含`@mustCallSuper`标注的父类方法，都要在子类方法中先调用父类方法。**



- `initState`：

	**当Widget第一次插入到Widget树时会被调用，对于每一个State对象，Flutter framework只会调用一次该回调**
	
	因此通常在该回调中做一些一次性的操作，如状态初始化、订阅子树的事件通知等
	
	不能在该回调中调用`BuildContext.inheritFromWidgetOfExactType()`方法（该方法用于在Widget树上获取离当前widget最近的一个父级`InheritFromWidget`），原因是在初始化完成后，Widget树中的`InheritFromWidget`也可能会发生变化，所以正确的做法应该在在`build()`方法或`didChangeDependencies()`中调用它

- `didChangeDependencies()`：

	**当State对象的依赖发生变化时会被调用**，典型的场景是当系统语言Locale或应用主题改变时，Flutter framework会通知widget调用此回调
	
	例如：在`build() `中包含了一个InheritedWidget，然后`build()`中InheritedWidget发生了变化，那么此时`InheritedWidget`和其子widget的`didChangeDependencies()`回调都会被调用。

- `build()`：

	**它主要是用于构建Widget子树的**，会在如下场景被调用：

	1. 在调用`initState()`之后

	2. 在调用`didUpdateWidget()`之后

	3. 在调用`setState()`之后

	4. 在调用`didChangeDependencies()`之后

	5. 在State对象从树中一个位置移除后（会调用deactivate）又重新插入到树的其它位置之后


- `reassemble()`：

	**此回调是专门为了开发调试而提供的，在热重载(hot reload)时会被调用，此回调在Release模式下永远不会被调用**

- `didUpdateWidget()`：

	**在widget重新构建时，Flutter framework会调用`Widget.canUpdate()`方法来检测Widget树中同一位置的新旧节点，然后决定是否需要更新，如果`Widget.canUpdate()`返回true则会调用此回调**。`Widget.canUpdate()`会在新旧widget的key和runtimeType同时相等时会返回true，也就是说在在新旧widget的key和runtimeType同时相等时didUpdateWidget()就会被调用。

- `deactivate()`：

	**当State对象从树中被移除时，会调用此回调**
	
	在一些场景下，Flutter framework会将State对象重新插到树中，如包含此State对象的子树在树的一个位置移动到另一个位置时（可以通过GlobalKey来实现）。如果移除后没有重新插入到树中则紧接着会调用`dispose()`方法。

- `dispose()`：

	**当State对象从树中被永久移除时调用；通常在此回调中释放资源**


# 4. 状态管理

响应式的编程框架中都会有一个永恒的主题——“状态管理”，无论是在React/Vue（两者都是支持响应式编程的web开发框架）还是Flutter

- **这个问题也就是说`StatefulWidget`的状态应该被谁管理？**widget本身？父widget？都会？还是另一个对象？答案是取决于实际情况！以下是管理状态的最常见的方法：

	1. Widget管理自己的state

	2. 父widget管理子widget状态

	3. 混合管理（父widget和子widget都管理状态）

以下原则可以帮助决定如何使用哪种管理方法：

1. 如果状态是用户数据，如复选框的选中状态、滑块的位置，则该状态最好由父widget管理

2. 如果状态是有关界面外观效果的，例如颜色、动画，那么状态最好由widget本身来管理

3. 如果某一个状态是不同widget共享的则最好由它们共同的父widget管理

- **在widget内部管理状态封装性会好一些，而在父widget中管理会比较灵活**

- 有些时候，**如果不确定到底该怎么管理状态，那么推荐的首选是在父widget中管理（灵活会显得更重要一些）**


## 4.1 Widget管理自身状态

创建一个盒子，当点击它时，盒子背景会在绿色与灰色之间切换。状态`_active`确定颜色：绿色为true ，灰色为false

	class TapboxA extends StatefulWidget {
	  TapboxA({Key key}) : super(key: key);
	
	  @override
	  _TapboxAState createState() => new _TapboxAState();
	}
	
	class _TapboxAState extends State<TapboxA> {
	  bool _active = false;
	
	  void _handleTap() {
	    setState(() {
	      _active = !_active;
	    });
	  }
	
	  Widget build(BuildContext context) {
	    return new GestureDetector(
	      onTap: _handleTap,
	      child: new Container(
	        child: new Center(
	          child: new Text(
	            _active ? 'Active' : 'Inactive',
	            style: new TextStyle(fontSize: 32.0, color: Colors.white),
	          ),
	        ),
	        width: 200.0,
	        height: 200.0,
	        decoration: new BoxDecoration(
	          color: _active ? Colors.lightGreen[700] : Colors.grey[600],
	        ),
	      ),
	    );
	  }
	}

## 4.2 父widget管理子widget的state
**通过父widget管理状态并告诉其子widget何时更新通常是比较好的方式**

- 例如，IconButton是一个图片按钮，但它是一个无状态的widget，因此父widget需要知道该按钮是否被点击来采取相应的处理

示例代码:

	// ParentWidget 为 TapboxB 管理状态.
	
	//------------------------ ParentWidget --------------------------------
	
	class ParentWidget extends StatefulWidget {
	  @override
	  _ParentWidgetState createState() => new _ParentWidgetState();
	}
	
	class _ParentWidgetState extends State<ParentWidget> {
	  bool _active = false;
	
	  void _handleTapboxChanged(bool newValue) {
	    setState(() {
	      _active = newValue;
	    });
	  }
	
	  @override
	  Widget build(BuildContext context) {
	    return new Container(
	      child: new TapboxB(
	        active: _active,
	        onChanged: _handleTapboxChanged,
	      ),
	    );
	  }
	}
	
	//------------------------- TapboxB ----------------------------------
	
	class TapboxB extends StatelessWidget {
	  TapboxB({Key key, this.active: false, @required this.onChanged})
	      : super(key: key);
	
	  final bool active;
	  final ValueChanged<bool> onChanged;
	
	  void _handleTap() {
	    onChanged(!active);
	  }
	
	  Widget build(BuildContext context) {
	    return new GestureDetector(
	      onTap: _handleTap,
	      child: new Container(
	        child: new Center(
	          child: new Text(
	            active ? 'Active' : 'Inactive',
	            style: new TextStyle(fontSize: 32.0, color: Colors.white),
	          ),
	        ),
	        width: 200.0,
	        height: 200.0,
	        decoration: new BoxDecoration(
	          color: active ? Colors.lightGreen[700] : Colors.grey[600],
	        ),
	      ),
	    );
	  }
	}

## 4.3 混合管理
**混合管理指的是widget自身管理一些内部状态，而父widget管理一些其他外部状态**

- 在下面TapboxC示例中，按下时，盒子的周围会出现一个深绿色的边框。抬起时，边框消失；点击生效，盒子的颜色改变。 TapboxC将其_active状态导出到其父widget中，但在内部管理其_highlight状态。这个例子有两个状态对象_ParentWidgetState和_TapboxCState。

示例代码:

	//---------------------------- ParentWidget ----------------------------
	
	class ParentWidgetC extends StatefulWidget {
	  @override
	  _ParentWidgetCState createState() => new _ParentWidgetCState();
	}
	
	class _ParentWidgetCState extends State<ParentWidgetC> {
	  bool _active = false;
	
	  void _handleTapboxChanged(bool newValue) {
	    setState(() {
	      _active = newValue;
	    });
	  }
	
	  @override
	  Widget build(BuildContext context) {
	    return new Container(
	      child: new TapboxC(
	        active: _active,
	        onChanged: _handleTapboxChanged,
	      ),
	    );
	  }
	}
	
	//----------------------------- TapboxC ------------------------------
	
	class TapboxC extends StatefulWidget {
	  TapboxC({Key key, this.active: false, @required this.onChanged})
	      : super(key: key);
	
	  final bool active;
	  final ValueChanged<bool> onChanged;
	
	  _TapboxCState createState() => new _TapboxCState();
	}
	
	class _TapboxCState extends State<TapboxC> {
	  bool _highlight = false;
	
	  void _handleTapDown(TapDownDetails details) {
	    setState(() {
	      _highlight = true;
	    });
	  }
	
	  void _handleTapUp(TapUpDetails details) {
	    setState(() {
	      _highlight = false;
	    });
	  }
	
	  void _handleTapCancel() {
	    setState(() {
	      _highlight = false;
	    });
	  }
	
	  void _handleTap() {
	    widget.onChanged(!widget.active);
	  }
	
	  Widget build(BuildContext context) {
	    // 在按下时添加绿色边框，当抬起时，取消高亮  
	    return new GestureDetector(
	      onTapDown: _handleTapDown, // 处理按下事件
	      onTapUp: _handleTapUp, // 处理抬起事件
	      onTap: _handleTap,
	      onTapCancel: _handleTapCancel,
	      child: new Container(
	        child: new Center(
	          child: new Text(widget.active ? 'Active' : 'Inactive',
	              style: new TextStyle(fontSize: 32.0, color: Colors.white)),
	        ),
	        width: 200.0,
	        height: 200.0,
	        decoration: new BoxDecoration(
	          color: widget.active ? Colors.lightGreen[700] : Colors.grey[600],
	          border: _highlight
	              ? new Border.all(
	                  color: Colors.teal[700],
	                  width: 10.0,
	                )
	              : null,
	        ),
	      ),
	    );
	  }
	}


## 4.4 全局状态管理
全局状态管理适用于当应用中包括一些跨widget（甚至跨路由）的状态需要同步

- 例如有一个设置页，里面可以设置应用语言，但是为了让设置实时生效，期望在语言状态发生改变时，APP Widget能够重新build一下，但APP Widget和设置页并不在一起。正确的做法是通过一个全局状态管理器来处理这种“相距较远”的widget之间的通信

目前主要有两种办法：

1. 实现一个全局的事件总线，将语言状态改变对应为一个事件，然后在APP Widget所在的父widgetinitState 方法中订阅语言改变的事件，当用户在设置页切换语言后，触发语言改变事件，然后APP Widget那边就会收到通知，然后重新build一下即可。

2. 使用redux这样的全局状态包，可以在pub上查看其详细信息

# 5. Flutter widget库介绍
Flutter提供了一套丰富、强大的基础widget，在基础widget库之上Flutter又提供了一套Material风格（Android默认的视觉风格）和一套Cupertino风格（iOS视觉风格）的widget库

要使用基础widget库，需要先导入：

	import 'package:flutter/widgets.dart';

## 5.1 基础widget

- `Text`：

	该 widget 可让创建一个带格式的文本

- `Row、 Column`：

	这些具有弹性空间的布局类Widget可在水平（Row）和垂直（Column）方向上创建灵活的布局。其设计是基于web开发中的Flexbox布局模型。

- `Stack`：

	取代线性布局 (和Android中的FrameLayout相似)，Stack允许子 widget 堆叠，可以使用 `Positioned` 来定位子widget相对于Stack的上下左右四条边的位置
	
- `Container`：

	Container 可让创建矩形视觉元素。Container可以使用一个`BoxDecoration`进行装饰, 如 background、一个边框、或者一个阴影。 Container 也可以具有边距（margins）、填充(padding)和应用于其大小的约束(constraints)。另外， Container可以使用矩阵在三维空间中对其进行变换。

## 5.2 Material widget

Material应用程序以MaterialApp widget开始， 该widget在应用程序的根部创建了一些有用的widget，比如一个Theme，它配置了应用的主题。 

其widget包括如：Scaffold、AppBar、FlatButton等

要使用Material widget，需要先引入它：

	import 'package:flutter/material.dart';


**在Material widget库中，有一些widget可以根据实际运行平台来切换表现风格**，比如MaterialPageRoute，在路由切换时，如果是Android系统，它将会使用Android系统默认的页面切换动画(从底向上)，如果是iOS系统时，它会使用iOS系统默认的页面切换动画（从右向左）

## 5.3 Cupertino widget

Flutter也提供了一套丰富的Cupertino风格的widget，尽管目前还没有Material widget那么丰富，但也在不断的完善中。

	import 'package:flutter/cupertino.dart';


# 6. 总结

Flutter提供了丰富的widget，在实际的开发中你可以随意使用它们，不要怕引入过多widget库会让应用安装包变大，这不是web开发，**dart在编译时只会编译使用了的代码**

- **由于Material和Cupertino都是在基础widget库之上的，所以如果应用中引入了这两者之一，则不需要再引入`flutter/widgets.dart`了，因为它们内部已经引入过了**






















	