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






# 7. 文本以及样式

## 7.1 Text

Text用于显示简单样式文本，它包含一些控制文本显示样式的一些属性

- `textAlign`：文本的对齐方式；可以选择左对齐、右对齐还是居中

- `maxLines`、`overflow`：指定文本显示的最大行数，默认情况下，文本是自动折行的，如果指定此参数，则文本最多不会超过指定的行,超过的文本会以`overflow`指定的形式来显示截断

- `textScaleFactor`：代表文本相对于当前字体大小的缩放因子，相对于去设置文本的样式style属性的fontSize，它是调整字体大小的一个快捷方式



## 7.2 TextStyle

TextStyle用于指定文本显示的样式如颜色、字体、粗细、背景等

- `height`：该属性用于指定行高，但它并不是一个绝对值，而是一个因子，**具体的行高等于`fontSize*height`**

- `fontFamily` ：由于不同平台默认支持的字体集不同，所以在手动指定字体时一定要先在不同平台测试一下

- `fontSize`：该属性和Text的textScaleFactor都用于控制字体大小。但是有两个主要区别：

	1. fontSize可以精确指定字体大小，而textScaleFactor只能通过缩放比例来控制

	2. textScaleFactor主要是用于系统字体大小设置改变时对Flutter应用字体进行全局调整，而fontSize通常用于单个文本，字体大小不会跟随系统字体大小变化


## 7.3 TextSpan

TextSpan，它代表文本的一个“片段”,对一个Text内容的不同部分按照不同的样式显示

	const TextSpan({
	  TextStyle style, 
	  Sting text,
	  List<TextSpan> children,
	  GestureRecognizer recognizer,
	});

- **通过`Text.rich()`方法来使用`TextSpan`**


## 7.4 DefaultTextStyle

**在widget树中，文本的样式默认是可以被继承的**

- 例如如果在widget树的某一个节点处设置一个默认的文本样式，那么该节点的子树中所有文本都会默认使用这个样式，而`DefaultTextStyle`正是用于设置默认文本样式的

## 7.5 使用字体
在Flutter中使用字体分两步完成

1. **首先在`pubspec.yaml`中声明它们，以确保它们会打包到应用程序中** 

		flutter:
		  fonts:
		    - family: Raleway
		      fonts:
		        - asset: assets/fonts/Raleway-Regular.ttf
		        - asset: assets/fonts/Raleway-Medium.ttf
		          weight: 500
		        - asset: assets/fonts/Raleway-SemiBold.ttf
		          weight: 600
		          
2. **通过`TextStyle`属性使用字体**

### 7.5.1 Package中的字体

要使用Package中定义的字体，必须提供`package`参数。例如，假设上面的字体声明位于my_package包中。然后创建TextStyle的过程如下：

	const textStyle = const TextStyle(
	  fontFamily: 'Raleway',
	  package: 'my_package', //指定包名
	);

- **如果在`my_package`包内部使用它自己定义的字体，也应该在创建文本样式时指定package参数**

- **一个包可以只提供字体文件而不需要在`pubspec.yaml`中声明**。 这些文件应该存放在包的lib/文件夹中。字体文件不会自动绑定到应用程序中，应用程序可以在声明字体时有选择地使用这些字体

	假设一个名为`my_package`的包中有一个字体文件：

		lib/fonts/Raleway-Medium.ttf

	应用程序声明字体文件：
	
		 flutter:
		   fonts:
		     - family: Raleway
		       fonts:
		         - asset: assets/fonts/Raleway-Regular.ttf
		         - asset: packages/my_package/fonts/Raleway-Medium.ttf
		           weight: 500	

	- **`lib/`是隐含的，所以它不应该包含在asset路径中**

	在这种情况下，由于应用程序本地定义了字体，所以在创建TextStyle时可以不指定package参数：

		const textStyle = const TextStyle(
		  fontFamily: 'Raleway',
		);

# 8. 按钮

Material widget库中提供了多种按钮Widget如RaisedButton、FlatButton、OutlineButton等，它们都是直接或间接对`RawMaterialButton`的包装定制

所有Material 库中的按钮都有如下相同点：

1. 按下时都会有“水波动画”

2. 有一个onPressed属性来设置点击回调，当按钮按下时会执行该回调，如果不提供该回调则按钮会处于禁用状态，禁用状态不响应用户点击

## 8.1 RaisedButton

RaisedButton 即"漂浮"按钮，它默认带有阴影和灰色背景,按下后，阴影会变大

## 8.2 FlatButton

FlatButton即扁平按钮，默认背景透明并不带阴影,按下后，会有背景色

## 8.3 OutlineButton

OutlineButton默认有一个边框，不带阴影且背景透明。按下后，边框颜色会变亮、同时出现背景和阴影(较弱)

## 8.4 IconButton

IconButton是一个可点击的Icon，不包括文字，默认没有背景，点击后会出现背景：

## 8.5 自定义按钮外观
按钮外观可以通过其属性来定义，不同按钮属性大同小异，以FlatButton为例，介绍一下常见的按钮属性

	const FlatButton({
	  ...  
	  @required this.onPressed, //按钮点击回调
	  this.textColor, //按钮文字颜色
	  this.disabledTextColor, //按钮禁用时的文字颜色
	  this.color, //按钮背景颜色
	  this.disabledColor,//按钮禁用时的背景颜色
	  this.highlightColor, //按钮按下时的背景颜色
	  this.splashColor, //点击时，水波动画中水波的颜色
	  this.colorBrightness,//按钮主题，默认是浅色主题 
	  this.padding, //按钮的填充
	  this.shape, //外形
	  @required this.child, //按钮的内容
	})

- **Flutter 中没有提供去除背景的设置，假若需要去除背景，则可以通过将背景颜色设置为全透明来实现**。对应上面的代码，便是将 `color: Colors.blue `替换为 `color: Color(0x000000)`


# 9. 图片

Flutter中可以通过`Image`类来加载并显示图片，Image的数据源可以是asset、文件、内存以及网络

## 9.1 ImageProvider

**`ImageProvider` 是一个抽象类，主要定义了图片数据获取的接口`load()`，从不同的数据源获取图片需要实现不同的`ImageProvider`**

- 如AssetImage是实现了从Asset中加载图片的ImageProvider，而NetworkImage实现了从网络加载图片的ImageProvider

## 9.2 Image

**`Image` widget有一个必选的`image`参数，它对应一个`ImageProvider`**

- 例如从assets中加载图片，需要使用`AssetImage`.从网络加载图片，需要使用`NetworkImage`

- **除了通过构造函数构造一个`Image`,`Image`类还提供了命名构造函数，快速创建`Image`,例如`Image.asset()`,`Image.netWork()`,`Image.memory()`,`Image.file()`**

### 9.2.1 从asset和网络加载图片

1. 在工程根目录下创建一个images目录，并将图片avatar.png拷贝到该目录

2. 在pubspec.yaml中的flutter部分添加如下内容：

		  assets:
		    - images/avatar.png

	- 注意: 由于 yaml 文件对缩进严格，所以必须严格按照每一层两个空格的方式进行缩进，此处assets前面应有两个空格

3. 加载该图片

		Image(
		  image: AssetImage("images/avatar.png"),
		  width: 100.0
		);

	Image提供了一个命名构造函数`Image.asset`用于从asset中加载、显示图片：
	
		Image.asset("images/avatar.png",
		  width: 100.0,
		)

### 9.2.2 从网络加载图片
由于不需要再本地对文件进行设置，因此仅需要为`Image` widget提供一个`ImageProvider`即可

	Image(
	  image: NetworkImage(
	      "https://avatars2.githubusercontent.com/u/20411648?s=460&v=4"),
	  width: 100.0,
	)

`Image`类提供了一个命名构造函数`Image.network`用于从网络加载,显示图片

	Image.network("http......")

## 9.3 构造函数的参数

	const Image({
	  ...
	  this.width, //图片的宽
	  this.height, //图片高度
	  this.color, //图片的混合色值
	  this.colorBlendMode, //混合模式
	  this.fit,//缩放模式
	  this.alignment = Alignment.center, //对齐方式
	  this.repeat = ImageRepeat.noRepeat, //重复方式
	  ...
	})

- `width、height`：

	用于设置图片的宽、高，**当不指定宽高时，图片会根据当前父容器的限制，尽可能的显示其原始大小**

	如果只设置width、height的其中一个，那么另一个属性默认会按比例缩放，但可以通过fit属性来指定适应规则

- `fit`:

	**该属性用于在图片的显示空间和图片本身大小不同时指定图片的适应模式**。适应模式是在`BoxFit`中定义，它是一个枚举类型，有如下值：

	- `fill`：会拉伸填充满显示空间，图片本身长宽比会发生变化，图片会变形

	- `cover`：会按图片的长宽比放大后居中填满显示空间，图片不会变形，超出显示空间部分会被剪裁

	- `contain`：这是图片的默认适应规则，图片会在保证图片本身长宽比不变的情况下缩放以适应当前显示空间，图片不会变形

	- `fitWidth`：图片的宽度会缩放到显示空间的宽度，高度会按比例缩放，然后居中显示，图片不会变形，超出显示空间部分会被剪裁

	- `fitHeight`：图片的高度会缩放到显示空间的高度，宽度会按比例缩放，然后居中显示，图片不会变形，超出显示空间部分会被剪裁

	- `none`：图片没有适应策略，会在显示空间内显示图片，如果图片比显示空间大，则显示空间只会显示图片中间部分

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3o2qmwwxkj20a00hsta5.jpg)


- `color`和`colorBlendMode`：

	**在图片绘制时可以对每一个像素进行颜色混合处理**，color指定混合色，而colorBlendMode指定混合模式

- `repeat`：

	当图片本身大小小于显示空间时，指定图片的重复规则


# 10 Icon

Flutter中可以使用iconfont，iconfont即“字体图标”，它是将图标做成字体文件，然后在指定不同的字符时显示不同的图片

- **在字体文件中，每一个字符都对应一个位码，而每一个位码对应一个显示字形，不同的字体就是指字形不同，即字符对应的字形是不同的。而在iconfont中，只是将位码对应的字形做成了图标，所以不同的字符最终就会渲染成不同的图标**

IconFont与图片相比有以下优点：

1. 体积小：可以减小安装包大小。

2. 矢量的：iconfont都是矢量图标，放大不会影响其清晰度。

3. 可以应用文本样式：可以像文本一样改变字体图标的颜色、大小对齐等。

4. 可以通过TextSpan和文本混用。


## 10.1 Material Design 字体图标

Flutter默认包含了一套Material Design的字体图标，在pubspec.yaml文件中的配置如下

	flutter:
	  uses-material-design: true

使用字体图标被当做文本一样去使用，但是前提是需要知道图标的码点

	String icons = "";
	// accessible: &#xE914; or 0xE914 or E914
	icons += "\uE914";
	// error: &#xE000; or 0xE000 or E000
	icons += " \uE000";
	// fingerprint: &#xE90D; or 0xE90D or E90D
	icons += " \uE90D";
	
	Text(icons,
	  style: TextStyle(
	      fontFamily: "MaterialIcons",
	      fontSize: 24.0,
	      color: Colors.green
	  ),
	);

- **必须添加`style`,并对字体图标的`fontFamily`进行指定，否则会因为找不到而显示失败，或者找到错误的码点对应的字体图标**


上面的方法不仅需要提前知道图标的码点，而且十分复杂，Flutter封装了一个`IconData`和`Icon`来专门显示字体图标

	Icon(Icons.fingerprint,color: Colors.green,)

- **Icons类中包含了所有Material Design图标的IconData静态变量定义**

## 10.2 使用自定义字体图标
[`iconfont.cn`](https://www.iconfont.cn)上有很多字体图标素材

- [Iconfont 使用教程](https://www.iconfont.cn/help/detail?helptype=code)

要使用自定义字体图标需要以下流程:

1. 将图标生成不同格式的字体文件(Flutter中使用`ttf`格式)

2. 导入并使用

	1. 流程与导入字体文件相同。假设将字体图标文件保存在项目根目录下，路径为`fonts/iconfont.ttf`. 在`pubspec.yaml`文件中对其进行声明
			
			#fonts: 属于flutter 下面的
			flutter:
				fonts:
				  - family: myIcon  #指定一个字体名
				    fonts:
				      - asset: fonts/iconfont.ttf

	2. **除了直接使用码点进行使用之外，还可以通过`IconData`对码点进行封装**

			class MyIcons{
			  // book 图标
			  static const IconData book = const IconData(
			      0xe614, 
			      fontFamily: 'myIcon', 
			      matchTextDirection: true
			  );
			}

	3. 使用方式和普通的IconData一样

			Icon(MyIcons.book,color: Colors.purple)


# 11 单选开关和复选框

Material widgets库中提供了Material风格的单选开关`Switch`和复选框`Checkbox`，它们都是继承自`StatelessWidget`，所以它们本身不会保存当前选择状态，并且一般都是在父widget中管理选中状态

- 当用户点击Switch或Checkbox时，它们会触发onChanged回调，可以在此回调中处理选中状态改变逻辑

示例:

	class SwitchAndCheckBoxTestRoute extends StatefulWidget {
	  @override
	  _SwitchAndCheckBoxTestRouteState createState() => new _SwitchAndCheckBoxTestRouteState();
	}
	
	class _SwitchAndCheckBoxTestRouteState extends State<SwitchAndCheckBoxTestRoute> {
	  bool _switchSelected=true; //维护单选开关状态
	  bool _checkboxSelected=true;//维护复选框状态
	  @override
	  Widget build(BuildContext context) {
	    return Column(
	      children: <Widget>[
	        Switch(
	          value: _switchSelected,//当前状态
	          onChanged:(value){
	            //重新构建页面  
	            setState(() {
	              _switchSelected=value;
	            });
	          },
	        ),
	        Checkbox(
	          value: _checkboxSelected,
	          activeColor: Colors.red, //选中时的颜色
	          onChanged:(value){
	            setState(() {
	              _checkboxSelected=value;
	            });
	          } ,
	        )
	      ],
	    );
	  }
	}

## 11.1 属性和外观

`Switch`和`CheckBox`都有一个`activeColor`属性，用来设置激活状态下widget的颜色

**`CheckBox`长宽都固定，无法自定义**

**`Switch`只能定义宽度，高度固定**

`CheckBox`有一个属性`tristate`,表示是否为三态，默认值为false,如果设置为true，那么`CheckBox`的value会增加一个状态`null`

# 12 输入框
**Material widget库中提供了`TextField`用于文本输入**

## 12.1 参数介绍

	const TextField({
	  ...
	  TextEditingController controller, 
	  FocusNode focusNode,
	  InputDecoration decoration = const InputDecoration(),
	  TextInputType keyboardType,
	  TextInputAction textInputAction,
	  TextStyle style,
	  TextAlign textAlign = TextAlign.start,
	  bool autofocus = false,
	  bool obscureText = false,
	  int maxLines = 1,
	  int maxLength,
	  bool maxLengthEnforced = true,
	  ValueChanged<String> onChanged,
	  VoidCallback onEditingComplete,
	  ValueChanged<String> onSubmitted,
	  List<TextInputFormatter> inputFormatters,
	  bool enabled,
	  this.cursorWidth = 2.0,
	  this.cursorRadius,
	  this.cursorColor,
	  ...
	})

- `controller`：**编辑框的控制器，通过它可以设置/获取编辑框的内容、选择编辑内容、监听编辑文本改变事件**。如果没有提供controller，则TextField内部会自动创建一个

- `focusNode`：用于控制`TextField`是否占有当前键盘的输入焦点。它是和键盘交互的一个handle

- `InputDecoration`：用于控制TextField的外观显示，如提示文本、背景颜色、边框等

- `keyboardType`：用于设置该输入框默认的键盘输入类型，取值如下：

	TextInputType枚举值|	含义
	:---:|:---:
	text	|文本输入键盘
	multiline	|多行文本，需和maxLines配合使用(设为null或大于1)
	number	|数字；会弹出数字键盘
	phone	|优化后的电话号码输入键盘；会弹出数字键盘并显示"* #"
	datetime	|优化后的日期输入键盘；Android上会显示“: -”
	emailAddress	|优化后的电子邮件地址；会显示“@ .”
	url	   |优化后的url输入键盘； 会显示“/ .”

- `textInputAction`：键盘动作按钮图标(即回车键位图标)，它是一个枚举值，有多个可选值

- `style`：正在编辑的文本样式

- `textAlign`: 输入框内编辑文本在水平方向的对齐方式

- `autofocus`: 是否自动获取焦点

- `obscureText`：是否隐藏正在编辑的文本，如用于输入密码的场景等，文本内容会用“•”替换

- `maxLines`：输入框的最大行数，默认为1；如果为null，则无行数限制

- `maxLength`和`maxLengthEnforced` ：maxLength代表输入框文本的最大长度，设置后输入框右下角会显示输入的文本计数。maxLengthEnforced决定当输入文本长度超过maxLength时是否阻止输入，为true时会阻止输入，为false时不会阻止输入但输入框会变红

- `onChange`：**输入框内容改变时的回调函数**；注：内容改变事件也可以通过controller来监听

- `onEditingComplete`和`onSubmitted`：这两个回调都是在输入框输入完成时触发，比如按了键盘的完成键（对号图标）或搜索键（🔍图标）

	不同的是两个回调签名不同，onSubmitted回调是`ValueChanged<String>`类型，它接收当前输入内容做为参数，而`onEditingComplete`不接收参数

- `inputFormatters`：用于指定输入格式；当用户输入内容改变时，会根据指定的格式来校验

- `enable`：如果为false，则输入框会被禁用，禁用状态不接收输入和事件，同时显示禁用态样式（在其decoration中定义）

- `cursorWidth、cursorRadius`和`cursorColor`：这三个属性是用于自定义输入框光标宽度、圆角和颜色的


## 12.2 输入框示例

	TextField(
	            autofocus: true,
	            decoration: InputDecoration(
	                labelText: "用户名",
	                hintText: "用户名或邮箱",
	                prefixIcon: Icon(Icons.person)
	            ),
	          )

## 12.3 获取输入内容
获取输入内容需要借助`TextEditingController `

	//定义一个controller
	TextEditingController _unameController=new TextEditingController();
	
	// 将controller与TextField绑定
	TextField(
	    autofocus: true,
	    controller: _unameController, //设置controller
	    ...
	)
	
	// 使用controller获取文本
	print(_unameController.text)


## 12.4 监听文本变化
监听文本变化有俩种方式

1. 通过设置构造函数中的`onChange`回调

		TextField(
		    autofocus: true,
		    onChanged: (v) {
		      print("onChange: $v");
		    }
		)


2. 通过`TextEditingController`添加监听回调

		@override
		void initState() {
		  //监听输入改变  
		  _unameController.addListener((){
		    print(_unameController.text);
		  });
		}

- `onChanged`是专门用于监听文本变化，而`controller`不仅能监听文本变化，还可以设置默认值、选择文本

## 12.5 控制焦点

焦点可以通过`FocusNode`和`FocusScopeNode`来控制

- **默认情况下，焦点由`FocusScopeNode `来管理，它代表焦点控制范围，可以在这个范围内可以通过`FocusScopeNode`在输入框之间移动焦点、设置默认焦点等**

- **可以通过`FocusScope.of(context)` 来获取widget树中默认的`FocusScopeNode`**

- `FocusScope`需要和对应的widget进行关联(即通过构造函数设置),之后通过`FocusScopeNode`与widget对应的`FocusScope`进行焦点管理

示例：

	TextField(focusNode: focusNode2,//关联focusNode2
	            decoration: InputDecoration(
	                labelText: "input1"
	            ),
	          )

    // 获取FocusScopeNode对focusNode2进行获取焦点操作
    FocusScope.of(context).requestFocus(focusNode2);
	// 放弃自身焦点
	focusNode2.unfocus();

## 12.6 监听焦点状态改变事件

`FocusNode`继承自`ChangeNotifier`，可以向`FocusNode`添加焦点改变的回调事件，如：

	// 创建 focusNode   
	FocusNode focusNode = new FocusNode();
	...
	// focusNode绑定输入框   
	TextField(focusNode: focusNode);
	...
	// 监听焦点变化    
	focusNode.addListener((){
	   print(focusNode.hasFocus);
	});

- 拥有焦点时`focusNode.hasFocus`值为true，失去焦点时为false


## 12.7 自定义样式

**虽然`decoration`属性可以用来定义输入框样式，但是有一些样式如下划线默认颜色及宽度都是不能直接自定义的**

	TextField(
	  ...
	  decoration: InputDecoration(
	  border: UnderlineInputBorder(
	  //下面代码没有效果
	  borderSide: BorderSide(
	  		color: Colors.red,
	 		width: 5.0
	    )),
	  prefixIcon: Icon(Icons.person)
	  ),
	)

由于`TextField`在绘制下划线时使用的颜色是主题色里面的`hintColor`，但提示文本颜色也是用的`hintColor`， **如果直接修改主题中的`hintColor`，那么下划线和提示文本的颜色都会变**

- **`TextField`中的`decoration`中可以设置`hintStyle`，它可以覆盖`hintColor`**，并且主题中可以通过inputDecorationTheme来设置输入框默认的decoration

		Theme(
		  data: Theme.of(context).copyWith(
		      hintColor: Colors.grey[200], //定义下划线颜色
		      inputDecorationTheme: InputDecorationTheme(
		          labelStyle: TextStyle(color: Colors.grey),//定义label字体样式
		          hintStyle: TextStyle(color: Colors.grey, fontSize: 14.0)//定义提示文本样式
		      )
		  ),
		  child: Column(
		    children: <Widget>[
		      TextField(
		        decoration: InputDecoration(
		            labelText: "用户名",
		            hintText: "用户名或邮箱",
		            prefixIcon: Icon(Icons.person)
		        ),
		      ),
		      TextField(
		        decoration: InputDecoration(
		            prefixIcon: Icon(Icons.lock),
		            labelText: "密码",
		            hintText: "您的登录密码",
		            hintStyle: TextStyle(color: Colors.grey, fontSize: 13.0)
		        ),
		        obscureText: true,
		      )
		    ],
		  )
		)

- 通过设置`InputDecoration `的中的`border`为 `InputBorder.none`可以隐藏下划线

**通过widget组合的方式，可以定义背景圆角等。一般来说，优先通过decoration来自定义样式，如果decoration实现不了，再用widget组合的方式**


# 13 表单

Flutter提供了一个`Form `widget，它可以对输入框进行分组，然后进行一些统一操作，如输入内容校验、输入框重置以及输入内容保存

## 13.1 Form
Form继承自StatefulWidget对象，它对应的状态类为`FormState`

	Form({
	  @required Widget child,
	  bool autovalidate = false,
	  WillPopCallback onWillPop,
	  VoidCallback onChanged,
	})

- `autovalidate`：是否自动校验输入内容；当为true时，每一个子FormField内容发生变化时都会自动校验合法性，并直接显示错误信息。否则，需要通过调用`FormState.validate()`来手动校验。

- `onWillPop`：决定Form所在的路由是否可以直接返回（如点击返回按钮），该回调返回一个Future对象，如果Future的最终结果是false，则当前路由不会返回；如果为true，则会返回到上一个路由。此属性通常用于拦截返回按钮。

- `onChanged`：Form的任意一个子FormField内容发生变化时会触发此回调

- `child` :FormField

## 13.2 FormField

Form的子孙元素必须是`FormField`类型，FormField是一个抽象类，定义几个属性，FormState内部通过它们来完成操作

	const FormField({
	  ...
	  FormFieldSetter<T> onSaved, //保存回调
	  FormFieldValidator<T>  validator, //验证回调
	  T initialValue, //初始值
	  bool autovalidate = false, //是否自动校验。
	})

- Flutter提供了一个`TextFormField` widget，它继承自FormField类，也是TextField的一个包装类，所以除了FormField定义的属性之外，它还包括TextField的属性


## 13.3 FormState

**`FormState`为Form的State类，可以通过`Form.of()`或`GlobalKey`获得。通过它可以对Form的子孙`FormField`进行统一操作**

- `FormState.validate()`：调用此方法后，会调用Form子孙FormField的validate回调，如果有一个校验失败，则返回false，所有校验失败项都会返回用户返回的错误提示。

- `FormState.save()`：调用此方法后，会调用Form子孙FormField的save回调，用于保存表单内容

- `FormState.reset()`：调用此方法后，会将子孙FormField的内容清空。


## 13.4 示例

	class FormTestRoute extends StatefulWidget {
	  @override
	  _FormTestRouteState createState() => new _FormTestRouteState();
	}
	
	class _FormTestRouteState extends State<FormTestRoute> {
	  TextEditingController _unameController = new TextEditingController();
	  TextEditingController _pwdController = new TextEditingController();
	  GlobalKey _formKey= new GlobalKey<FormState>();
	
	  @override
	  Widget build(BuildContext context) {
	    return Scaffold(
	      appBar: AppBar(
	        title:Text("Form Test"),
	      ),
	      body: Padding(
	        padding: const EdgeInsets.symmetric(vertical: 16.0, horizontal: 24.0),
	        child: Form(
	          key: _formKey, //设置globalKey，用于后面获取FormState
	          autovalidate: true, //开启自动校验
	          child: Column(
	            children: <Widget>[
	              TextFormField(
	                  autofocus: true,
	                  controller: _unameController,
	                  decoration: InputDecoration(
	                      labelText: "用户名",
	                      hintText: "用户名或邮箱",
	                      icon: Icon(Icons.person)
	                  ),
	                  // 校验用户名
	                  validator: (v) {
	                    return v
	                        .trim()
	                        .length > 0 ? null : "用户名不能为空";
	                  }
	
	              ),
	              TextFormField(
	                  controller: _pwdController,
	                  decoration: InputDecoration(
	                      labelText: "密码",
	                      hintText: "您的登录密码",
	                      icon: Icon(Icons.lock)
	                  ),
	                  obscureText: true,
	                  //校验密码
	                  validator: (v) {
	                    return v
	                        .trim()
	                        .length > 5 ? null : "密码不能少于6位";
	                  }
	              ),
	              // 登录按钮
	              Padding(
	                padding: const EdgeInsets.only(top: 28.0),
	                child: Row(
	                  children: <Widget>[
	                    Expanded(
	                      child: RaisedButton(
	                        padding: EdgeInsets.all(15.0),
	                        child: Text("登录"),
	                        color: Theme
	                            .of(context)
	                            .primaryColor,
	                        textColor: Colors.white,
	                        onPressed: () {
	                          //在这里不能通过此方式获取FormState，context不对
	                          //print(Form.of(context));
	                            
	                          // 通过_formKey.currentState 获取FormState后，
	                          // 调用validate()方法校验用户名密码是否合法，校验
	                          // 通过后再提交数据。 
	                          if((_formKey.currentState as FormState).validate()){
	                            //验证通过提交数据
	                          }
	                        },
	                      ),
	                    ),
	                  ],
	                ),
	              )
	            ],
	          ),
	        ),
	      ),
	    );
	  }
	}

- 注意，登录按钮的`onPressed()`方法中不能通过`Form.of(context)`来获取，原因是此处的context来自`FormTestRoute`，而`Form.of(context)`是根据所指定context向根去查找，而`FormState`是在`FormTestRoute`的子树中，所以不行

	正确的做法是通过Builder来构建登录按钮，Builder会将widget节点的context作为回调参数：

		Expanded(
		 // 通过Builder来获取RaisedButton所在widget树的真正context(Element) 
		  child:Builder(builder: (context){
		    return RaisedButton(
		      ...
		      onPressed: () {
		        //由于本widget也是Form的子widget，所以可以通过下面方式获取FormState  
		        if(Form.of(context).validate()){
		          //验证通过提交数据
		        }
		      },
		    );
		  })
		)


**`context`是操作Widget所对应的`Element`的一个接口，由于Widget树对应的Element都是不同的，所以context也都是不同的**