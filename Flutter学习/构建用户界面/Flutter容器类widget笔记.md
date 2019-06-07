# Flutter容器类widget

[容器类widget](https://github.com/flutterchina/flutter-in-action/blob/master/docs/chapter5/index.md)

Flutter官方没有对widget进行区分，这里以功能来对widge进行区分，是为了方便学习

# 1. 容器类widget

容器类Widget和布局类Widget都作用于其子Widget，不同的是：

- 布局类Widget一般都需要接收一个widget数组（children），他们直接或间接继承自（或包含）`MultiChildRenderObjectWidget`.而容器类Widget一般只需要接收一个子Widget（child），他们直接或间接继承自（或包含）`SingleChildRenderObjectWidget`

- **布局类Widget是按照一定的排列方式来对其子Widget进行排列；而容器类Widget一般只是包装其子Widget，对其添加一些修饰（补白或背景色等）、变换(旋转或剪裁等)、或限制(大小等)**


# 2. Padding

**Padding可以用来给子widget填充(添加间距)**

	Padding({
	  ...
	  EdgeInsetsGeometry padding,
	  Widget child,
	})

- **`padding`指的是相对于当前widget边界到内容的间距**

	`EdgeInsetsGeometry`是一个抽象类，开发中，一般都使用`EdgeInsets`，它是`EdgeInsetsGeometry`的一个子类，定义了一些设置间距的便捷方法

## 2.1 EdgeInsets

`EdgeInsets`提供了如下便捷的方法:

- `fromLTRB(double left, double top, double right, double bottom) `：

	分别指定四个方向的间距

- `all(double value)` : 

	所有方向均使用相同数值的间距

- `only({left, top, right ,bottom })`：

	可以设置具体某个方向的间距(可以同时指定多个方向)

- `symmetric({ vertical, horizontal })`：

	用于设置对称方向的间距，vertical指top和bottom，horizontal指left和right


# 3. ConstrainedBox

`ConstrainedBox `是通过`RenderConstrainedBox `来渲染的，用于对子widget添加额外的约束

	ConstrainedBox({
	    Key key,
	    @required this.constraints,
	    Widget child,
	  })

- `constraints`表示约束条件，其类型为`BoxConstraints`

	例如，如果想让子widget的最小高度是80像素，你可以使用`const BoxConstraints(minHeight: 80.0)`作为子widget的约束

## 3.1 BoxConstraints
**BoxConstraints用于设置限制条件**

	const BoxConstraints({
	  this.minWidth = 0.0, //最小宽度
	  this.maxWidth = double.infinity, //最大宽度
	  this.minHeight = 0.0, //最小高度
	  this.maxHeight = double.infinity //最大高度
	})

- `BoxConstraints`还定义了一些便捷的命名构造函数，用于快速生成特定限制规则的BoxConstraints

	例如`BoxConstraints.tight(Size size)`，它可以生成给定大小的限制. `BoxConstraints.expand()`可以生成一个尽可能大的用以填充另一个容器的`BoxConstraints`


# 4. SizedBox

**`SizedBox`也是通过`RenderConstrainedBox `来渲染，其只是`ConstrainedBox`的一个定制版widget**

SizedBox用于给子widget指定固定的宽高

	SizedBox(
	  width: 80.0,
	  height: 80.0,
	  child: redBox
	)

	//等价于下面的代码
		
	ConstrainedBox(
	  constraints: BoxConstraints.tightFor(width: 80.0,height: 80.0),
	  child: redBox, 
	)

- `BoxConstraints.tightFor()`等价于下面的代码

		BoxConstraints(minHeight: 80.0,maxHeight: 80.0,minWidth: 80.0,maxWidth: 80.0)

- 实际上`ConstrainedBox`和`SizedBox`都是通过`RenderConstrainedBox`来渲染的,可以看到`ConstrainedBox`和`SizedBox`的`createRenderObject()`方法都返回的是一个`RenderConstrainedBox`对象


## 4.1 多重限制

如果某个widget存在多个父`ConstrainedBox`限制,`maxHeight`/`maxWidth`和`minHeight`/`minWidth`也有所不同

1. 对于`minHeight`/`minWidth`来说，会取整个嵌套限制中 较大的数值


2. 对于`maxHeight`/`maxWidth`来说，会取整个嵌套限制中 较小的数值

# 5. UnconstrainedBox

`UnconstrainedBox`表示不会对子Widget产生任何限制，它允许其子Widget按照其本身大小绘制

- 一般情况下很少直接使用此widget，但在"去除"多重限制的时候也许会有帮助

示例(去除多重限制)

	ConstrainedBox(
	    constraints: BoxConstraints(minWidth: 60.0, minHeight: 100.0),  //父
	    child: UnconstrainedBox( //“去除”父级限制
	      child: ConstrainedBox(
	        constraints: BoxConstraints(minWidth: 90.0, minHeight: 20.0),//子
	        child: redBox,
	      ),
	    )
	)

- 如果没有去除父级的限制，那么会得到一个`90*100`的红色框，在去除了限制之后，得到了一个`90*20`的红色框

- **但是`UnconstrainedBox`对父限制的“去除”并非是真正的去除，上面例子中虽然红色区域大小是`90×20`，但上方仍然有80的空白空间**。也就是说父限制的`minHeight(100.0)`仍然是生效的，只不过它不影响最终子元素`redBox`的大小，但仍然还是占有相应的空间，**可以认为此时的父ConstrainedBox是作用于子UnconstrainedBox上，而redBox只受子ConstrainedBox限制**

- 并且Flutter中没有什么方法可以完全去除父`BoxConstraints`的限制


## 5.1 约束失效情况
**有时使用`ConstraintedBox`或`SizedBox`来给子widget指定宽高并没有效果，这是因为已经有一个父widget设置了限制**

例如在导航栏的右侧菜单中，使用了`SizedBox`指定了`loading`按钮的大小

	 AppBar(
	   title: Text(title),
	   actions: <Widget>[
	         SizedBox(
	             width: 20, 
	             height: 20,
	             child: CircularProgressIndicator(
	                 strokeWidth: 3,
	                 valueColor: AlwaysStoppedAnimation(Colors.white70),
	             ),
	         )
	   ],
	)

- **实际上这里的`SizedBox`并不会生效，因为`AppBar`已经指定了`actions`按钮的限制条件**,这时可以通过`UnconstrainedBox`来去除父widget的限制

		AppBar(
		  title: Text(title),
		  actions: <Widget>[
		      UnconstrainedBox(
		            child: SizedBox(
		              width: 20,
		              height: 20,
		              child: CircularProgressIndicator(
		                strokeWidth: 3,
		                valueColor: AlwaysStoppedAnimation(Colors.white70),
		              ),
		          ),
		      )
		  ],
		)


# 6. DecoratedBox

**`DecoratedBox`可以在其子widget绘制前(或后)绘制一个装饰Decoration（如背景、边框、渐变等）**

DecoratedBox定义如下：

	const DecoratedBox({
	  Decoration decoration,
	  DecorationPosition position = DecorationPosition.background,
	  Widget child
	})

- `decoration`：

	**代表将要绘制的装饰**，它类型为Decoration，Decoration是一个抽象类，它定义了一个接口 `createBoxPainter()`，子类的主要职责是需要通过实现它来创建一个画笔，该画笔用于绘制装饰
	
	通常使用`BoxDecoration`

- `position`：

	**此属性决定在哪里绘制Decoration**，它接收DecorationPosition的枚举类型，该枚举类两个值：

	1. `background`：在子widget之后绘制，即背景装饰

	2. `foreground`：在子widget之上绘制，即前景

## 6.1 BoxDecoration
**`BoxDecoration `是`Decoration`的子类，实现了常用的装饰元素绘制逻辑**

	BoxDecoration({
	  Color color, //颜色
	  DecorationImage image,//图片
	  BoxBorder border, //边框
	  BorderRadiusGeometry borderRadius, //圆角
	  List<BoxShadow> boxShadow, //阴影,可以指定多个
	  Gradient gradient, //渐变
	  BlendMode backgroundBlendMode, //背景混合模式
	  BoxShape shape = BoxShape.rectangle, //形状
	})

- `border`需要`BoxBorder `参数，通常使用`Border`提供的命名构造函数来创建，例如`Border.all()`

# 7. Transform

`Transform`可以在其子Widget绘制时对其应用一个矩阵变换（transformation），Matrix4是一个4D矩阵，通过它可以实现各种矩阵操作

- **Flutter中默认的坐标原点位于左上角，往右和往下是正方向**

示例:

	Container(
	  color: Colors.black,
	  child: new Transform(
	    alignment: Alignment.topRight, //相对于坐标系原点的对齐方式
	    transform: new Matrix4.skewY(0.3), //沿Y轴倾斜0.3弧度
	    child: new Container(
	      padding: const EdgeInsets.all(8.0),
	      color: Colors.deepOrange,
	      child: const Text('Apartment for rent!'),
	    ),
	  ),
	);

## 7.1 平移
Flutter提供了一个命名构造函数`Transform.translate`,其接收一个`Offset`参数，可以指定子widget沿x/y轴平移指定距离

	DecoratedBox(
	  decoration:BoxDecoration(color: Colors.red),
	  //默认原点为左上角，左移20像素，向上平移5像素  
	  child: Transform.translate(
	    offset: Offset(-20.0, -5.0),
	    child: Text("Hello world"),
	  ),
	)

## 7.2 旋转
Flutter提供了一个命名构造函数`Transform.rotate`,其可以对子widget进行旋转的

	DecoratedBox(
	  decoration:BoxDecoration(color: Colors.red),
	  child: Transform.rotate(
	    //旋转90度
	    angle:math.pi/2 ,
	    child: Text("Hello world"),
	  ),
	)；

## 7.3 缩放

Flutter提供了一个命名构造函数`Transform.scale`,其可以对子widget进行放大或缩小

	DecoratedBox(
	  decoration:BoxDecoration(color: Colors.red),
	  child: Transform.scale(
	      scale: 1.5, //放大到1.5倍
	      child: Text("Hello world")
	  )
	);

## 7.4 注意事项

**`Transform`的变换是应用在绘制阶段，而并不是应用在布局(layout)阶段，所以对于通过使用`Transform`获取widget作为容器类子类的情况，其`Transform`所得的内容和 容器类是分隔开的，容器类占用空间的大小和在屏幕上的位置都是固定不变的，因为这些是在布局阶段就确定**


- **由于矩阵变化只会作用在绘制阶段，所以在某些场景下，在UI需要变化时，可以直接通过矩阵变化来达到视觉上的UI改变，而不需要去重新触发build流程**，这样会节省layout的开销，所以性能会比较好。如之前介绍的Flow widget，它内部就是用矩阵变换来更新UI，除此之外，Flutter的动画widget中也大量使用了Transform以提高性能

示例：

	Row(
	    mainAxisAlignment: MainAxisAlignment.center,
	    children: <Widget>[
	      Container(
	        color: Colors.amber,
	        //将Transform.rotate换成RotatedBox
	        child: RotatedBox(
	          quarterTurns: 1, //旋转90度(1/4圈)
	          child: Text("Hello world"),
	        ),
	      ),
	      Container(
	        color: Colors.blue,
	        child: Transform.rotate(
	          angle: pi / 2,
	          child: Container(
	            color: Colors.teal,
	            child: Text("测试代码"),
	          ),
	        ),
	      ),
	      Text(
	        "你好",
	        style: TextStyle(color: Colors.green, fontSize: 18.0),
	      )
	    ]
	)

## 7.4 RotatedBox

`RotatedBox`和`Transform.rotate`功能相似，都可以对子widget进行旋转变换，但是有一点不同：**RotatedBox的变换是在layout阶段，会影响容器类widget的位置和大小**

	Row(
	  mainAxisAlignment: MainAxisAlignment.center,
	  children: <Widget>[
	    DecoratedBox(
	      decoration: BoxDecoration(color: Colors.red),
	      //将Transform.rotate换成RotatedBox  
	      child: RotatedBox(
	        quarterTurns: 1, //旋转90度(1/4圈)
	        child: Text("Hello world"),
	      ),
	    ),
	    Text("你好", style: TextStyle(color: Colors.green, fontSize: 18.0),)
	  ],
	)

- **由于`RotatedBox`是作用于layout阶段，所以widget会旋转90度（而不只是绘制的内容），`decoration`会作用到widget所占用的实际空间上**



# 8. Container

`Container`本身不对应具体的`RenderObject`，它是`DecoratedBox`、`ConstrainedBox`、`Transform`、`Padding`、`Align`等widget的一个组合widget。所以只需通过一个`Container`可以实现同时需要装饰、变换、限制的场景

	Container({
	  this.alignment,
	  this.padding, //容器内补白，属于decoration的装饰范围
	  Color color, // 背景色
	  Decoration decoration, // 背景装饰
	  Decoration foregroundDecoration, //前景装饰
	  double width,//容器的宽度
	  double height, //容器的高度
	  BoxConstraints constraints, //容器大小的限制条件
	  this.margin,//容器外补白，不属于decoration的装饰范围
	  this.transform, //变换
	  this.child,
	})

- **容器的大小可以通过`width`、`height`属性来指定，也可以通过`constraints`来指定，如果同时存在时，width、height优先**

	实际上Container内部会根据width、height来生成一个constraints

- **`color`和`decoration`是互斥的**，实际上，当指定color时，Container内会自动创建一个` new BoxDecoration(color: color)`并赋值给`decoration`属性

## 8.1 Padding和Margin

**在实现效果上，`margin`的间距是在容器外部，而`padding`的间距是在容器内部，但是事实上，`Container`内`margin`和`padding`都是通过`Padding`组件来实现的**


	Container(
	  margin: EdgeInsets.all(20.0), //容器外补白
	  color: Colors.orange,
	  child: Text("Hello world!"),
	),
	Container(
	  padding: EdgeInsets.all(20.0), //容器内补白
	  color: Colors.orange,
	  child: Text("Hello world!"),
	)

- 上面代码等价于如下:

		Padding(
		  padding: EdgeInsets.all(20.0),
		  child: DecoratedBox(
		    decoration: BoxDecoration(color: Colors.orange),
		    child: Text("Hello world!"),
		  ),
		),
		DecoratedBox(
		  decoration: BoxDecoration(color: Colors.orange),
		  child: Padding(
		    padding: const EdgeInsets.all(20.0),
		    child: Text("Hello world!"),
		  ),
		)	

# 9. Scaffold

**大多数路由都会包含一个导航栏，有些路由可能会有抽屉菜单(Drawer)以及底部Tab导航菜单等,因为这是非常通用的需求，所以Flutter Material库提供了一个`Scaffold`组件，它是一个路由的骨架，可以非常容易的拼装出一个完整的页面**

构造函数:

	  const Scaffold({
	    Key key,
	    this.appBar,
	    this.body,
	    this.floatingActionButton,
	    this.floatingActionButtonLocation,
	    this.floatingActionButtonAnimator,
	    this.persistentFooterButtons,
	    this.drawer,
	    this.endDrawer,
	    this.bottomNavigationBar,
	    this.bottomSheet,
	    this.backgroundColor,
	    this.resizeToAvoidBottomPadding,
	    this.resizeToAvoidBottomInset,
	    this.primary = true,
	    this.drawerDragStartBehavior = DragStartBehavior.start,
	    this.extendBody = false,
	    this.drawerScrimColor,
	  })

- `appBar`: 导航栏,通常为一个`AppBar`

- `drawer`/`endDrawer`:页面左侧抽屉或页面右侧抽屉,通常为`Drawer`

- `bottomNavigationBar`:底部导航栏，通常为`BottomNavigationBar`配合`BottomNavigationBarItem`

- `floatingActionButton `:悬浮按钮,通常是一个`FloatingActionButton`

## 9.1 AppBar

**AppBar是一个Material风格的导航栏，它可以设置标题、导航栏菜单、底部Tab等**

	AppBar({
	  Key key,
	  this.leading, //导航栏最左侧Widget，常见为抽屉菜单按钮或返回按钮。
	  this.automaticallyImplyLeading = true, //如果leading为null，是否自动实现默认的leading按钮
	  this.title,// 页面标题
	  this.actions, // 导航栏右侧菜单
	  this.bottom, // 导航栏底部菜单，通常为Tab按钮组
	  this.elevation = 4.0, // 导航栏阴影
	  this.centerTitle, //标题是否居中 
	  this.backgroundColor,
	  ...   //其它属性见源码注释
	})

- 如果`Scaffold`添加了抽屉，那么默认情况下`AppBar`的`leading`会被设置为打开抽屉的按钮。 `leading`可以被改变，但是其响应事件需要额外添加(点击打开抽屉)


- **触发打开抽屉的逻辑有俩种方式：**

	1. 通过`Scaffold.of(context)`可以获取父级最近的`Scaffold`组件的State对象，然后通过该对象打开或关闭抽屉

	        leading: Builder(builder: (ctx) {
	          return IconButton(
	              icon: Icon(Icons.event),
	              onPressed: () {
	                ScaffoldState state = Scaffold.of(ctx);
	                print("isDrawerOpen = ${state.isDrawerOpen}");
	                if (!state.isDrawerOpen) {
	                  state.openDrawer();
	                } else {
	                  state.openEndDrawer();
	                }
	              });
	        })

	2. **Flutter还有一种通用的获取StatefulWidget对象State的方法：通过GlobalKey来获取！** 步骤有两步：
	
		1. 给目标`StatefulWidget`添加`GlobalKey`

				//定义一个globalKey, 由于GlobalKey要保持全局唯一性，我们使用静态变量存储
				static GlobalKey<ScaffoldState> _globalKey= new GlobalKey();
				...
				Scaffold(
				    key: _globalKey , //设置key
				    ...  
				)

		2. 通过GlobalKey来获取State对象

				_globalKey.currentState.openDrawer()

	
### 9.1.1 TabBar

**`AppBar`有一个`bottom`属性，用来添加一个导航栏底部的tab按钮组**

Material组件库中提供了一个`TabBar`组件，它可以快速生成Tab菜单

- Material库为`TabBar`提供了`Tab`作为子widget,此外还可以使用自定义的widget

		Tab({
		  Key key,
		  this.text, // 菜单文本
		  this.icon, // 菜单图标
		  this.child, // 自定义Widget
		})

- `TabBar`需要一个`TabController`,用于控制/监听Tab菜单切换

### 9.1.2 TabBarView

Material库提供了一个`TabBarView`组件，它被用来配合`TabBar`来实现同步切换和滑动状态同步(仅靠`TabBar`只能生成一个静态的菜单)

示例:

	Scaffold(
	  appBar: AppBar(
	    ... //省略无关代码
	    bottom: TabBar(
	      controller: _tabController,
	      tabs: tabs.map((e) => Tab(text: e)).toList()),
	  ),
	  drawer: new MyDrawer(),
	  body: TabBarView(
	    controller: _tabController,
	    children: tabs.map((e) { //创建3个Tab页
	      return Container(
	        alignment: Alignment.center,
	        child: Text(e, textScaleFactor: 5),
	      );
	    }).toList(),
	  ),
	  ... // 省略无关代码  
	)    

- Flutter会通过同一个`TabController`来连接`appBar`和`body`

**除了`TabBarView`之外，Flutter还提供了一个`PageView `组件,其功能和`TabBarView`相似**


## 9.2 Drawer

Flutter提供了`Drawer`组件作为抽屉菜单，当用户手指从屏幕左/右向里滑动时便可打开抽屉菜单


抽屉菜单通常将Drawer作为根节点，它实现了Material风格的菜单面板，`MediaQuery.removePadding`组件可以用来移除抽Drawer内的一些指定空白

- 抽屉菜单页通常顶部由用户头像和昵称组成，底部是一个菜单列表，用ListView实现

## 9.3 FloatingActionButton

`FloatingActionButton`是Material设计规范中的一种特殊Button，通常悬浮在页面的某一个位置作为某种常用动作的快捷入口

- 可以通过`Scaffold`的`floatingActionButton`属性来设置一个`FloatingActionButton`，同时通过`floatingActionButtonLocation`属性来指定其在页面中悬浮的位置


## 9.4 底部导航

通过`Scaffold`的`bottomNavigationBar`属性可以来设置底部导航

- 通过Material组件库提供的`BottomNavigationBar`和`BottomNavigationBarItem`两个组件来实现Material风格的底部导航栏


除此之外，Flutter还提供了一个`BottomAppBar `组件，可以配合`FloatingActionButton `一起实现一种"打洞"效果的底部导航

	bottomNavigationBar: BottomAppBar(
	  color: Colors.white,
	  shape: CircularNotchedRectangle(), // 底部导航栏打一个圆形的洞
	  child: Row(
	    children: [
	      IconButton(icon: Icon(Icons.home)),
	      SizedBox(), //中间位置空出
	      IconButton(icon: Icon(Icons.business)),
	    ],
	    mainAxisAlignment: MainAxisAlignment.spaceAround, //均分底部导航栏横向空间
	  ),
	)

- 光上面的设置是不够的，还需要配合设置`FloatingActionButton `的位置,才能够实现嵌入的效果

		floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,

- `BottomAppBar`的`shape`属性决定洞的外形，`CircularNotchedRectangle`实现了一个圆形的外形，Flutter同样支持自定义外形



![](http://ww1.sinaimg.cn/large/6ab93b35ly1g3sp2ktpwyj20k008ajrj.jpg)



