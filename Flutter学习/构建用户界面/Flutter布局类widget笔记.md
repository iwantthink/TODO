# Flutter布局类widget

[Flutter快速上车之Widget - 闲鱼技术](https://juejin.im/post/5b8ce76f51882542c0626887)

[在Flutter中构建布局](https://flutterchina.club/tutorials/layout/)

# 重点

这篇笔记并不使用Scaffold，而是直接使用自定义的widget

- **大部分的widget都必须设置`textDirection`!否则可能出现异常!**

	或者通过`Directionality`来进行包装,以提供`textDirction`

**Flutter的架构:**

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3g17wizpkj20zk0ijjrn.jpg)

- flutter是视图描述的基础，其核心思想便是 一切皆widget


# 1 简介

布局类Widget都会包含一个或多个子widget，不同的布局类Widget对子widget排版(layout)方式不同

- **Element树才是最终的绘制树，Element树是通过widget树来创建的（通过`Widget.createElement()`），widget其实就是Element的配置数据**

Flutter中，根据Widget是否需要包含子节点以及子节点的数量 将Widget分为了三类，分别对应三种Element，如下表：


Widget	|对应的Element |	用途
:---|:---|---
`LeafRenderObjectWidget`|`	LeafRenderObjectElement`|Widget树的叶子节点，**用于没有子节点的widget**，通常基础widget都属于这一类，如Text、Image
`SingleChildRenderObjectWidget`|`SingleChildRenderObjectElement`|**包含一个子Widget**，如：ConstrainedBox、DecoratedBox等
`MultiChildRenderObjectWidget`|`MultiChildRenderObjectElement|**包含多个子Widget**，一般都有一个children参数，接受一个Widget数组。如Row、Column、Stack等


- 注意:

	**Flutter中的很多Widget是直接继承自`StatelessWidget`或`StatefulWidget`，然后在`build()`方法中构建真正的`RenderObjectWidget`**
	
	例如`Text`，它其实是继承自`StatelessWidget`，然后在`build()`方法中通过`RichText`来构建其子树，而`RichText`继承自`LeafRenderObjectWidget`
	
	因此也可以直接说`Text`属于`LeafRenderObjectWidget`（其它widget也可以这么描述），这才是本质。**那么也就是说其实`StatelessWidget`和`StatefulWidget`就是两个用于组合Widget的基类，它们本身并不关联最终的渲染对象（RenderObjectWidget）**

## 1.1 布局类Widget

**布局类Widget就是指直接或间接继承(包含)`MultiChildRenderObjectWidget`的Widget，它们一般都会有一个children属性用于接收子Widget**

其继承关系 

	Widget > RenderObjectWidget > (Leaf/SingleChild/MultiChild)RenderObjectWidget 
	
- **`RenderObjectWidget`类中定义了一个`RenderObject`对象，并且提供了创建、更新`RenderObject`的方法，子类必须实现这些方法**

- `RenderObject`是最终布局、渲染UI界面的对象，也就是说，对于布局类Widget来说，其布局算法都是通过对应的`RenderObject`对象来实现的

	所以如果对某个布局类Widget原理感兴趣，可以查看其`RenderObject`的实现
	

# 2 线性布局Row和Column
**所谓线性布局，即指沿水平或垂直方向排布子Widget。Flutter中通过`Row`和`Column`来实现线性布局，Row和Column都继承自`Flex`**


1. 行和列都需要一个子widget列表

2. `Row/Column`的子widget可以是行、列或其他复杂widget

3. 可以指定行或列如何在垂直或水平方向上对齐其子项

4. 可以拉伸或限制特定的子widget

5. 可以指定子widget如何使用行或列的可用空间


## 2.1 Row构造函数

	Row({
	  ...  
	  TextDirection textDirection,    
	  MainAxisSize mainAxisSize = MainAxisSize.max,    
	  MainAxisAlignment mainAxisAlignment = MainAxisAlignment.start,
	  VerticalDirection verticalDirection = VerticalDirection.down,  
	  CrossAxisAlignment crossAxisAlignment = CrossAxisAlignment.center,
	  List<Widget> children = const <Widget>[],
	})

- `textDirection`：

	表示水平方向子widget的布局顺序(是从左往右还是从右往左)，默认为系统当前`Locale`环境的文本方向(如中文、英语都是从左往右，而阿拉伯语是从右往左)

- `mainAxisSize`：

	**表示Row在主轴(水平)方向占用的空间，默认值是`MainAxisSize.max`，**表示尽可能多的占用水平方向的空间，此时无论子widgets实际占用多少水平空间，Row的宽度始终等于水平方向的最大宽度；
		
	**`MainAxisSize.min`表示尽可能少的占用水平空间，当子widgets没有占满水平剩余空间，则Row的实际宽度等于所有子widgets占用的的水平空间**

- `mainAxisAlignment`：

	**表示子Widgets在所占用的水平空间内对齐方式，但如果父类`Row`的`mainAxisSize`值为`MainAxisSize.min`，那么对于子类的`Column`或`Row`,分别对应的`crossAxisAlignment`或`mainAxisAlignment`无意义，因为此时子widget的宽度等于Row的宽度**
	
	`MainAxisAlignment.start`表示子widget沿`textDirection`的初始方向对齐，假如`textDirection`取值为`TextDirection.ltr`时，则`MainAxisAlignment.start`表示子widget沿主轴左对齐，`textDirection`取值为`TextDirection.rtl`时表示沿主轴右对齐
	
	`MainAxisAlignment.end`和`MainAxisAlignment.start`正好相反,`MainAxisAlignment.center表`示居中对齐
	
	**可以这么理解：`textDirection`是`mainAxisAlignment`的参考系**

- `verticalDirection`：

	表示Row纵轴（垂直）的对齐方向，默认是`VerticalDirection.down`，表示从上到下

- `crossAxisAlignment`：

	表示子Widgets在纵轴方向的对齐方式，**Row的高度等于子Widgets中最高的子元素高度**，它的取值和MainAxisAlignment一样(包含start、end、 center三个值)，**不同的是crossAxisAlignment的参考系是verticalDirection**
	
	即`verticalDirection`值为`VerticalDirection.down`时`crossAxisAlignment.start`指顶部对齐，`verticalDirection`值为`VerticalDirection.up`时，`crossAxisAlignment.start`指底部对齐；而`crossAxisAlignment.end`和`crossAxisAlignment.start`正好相反

- `children` ：

	子Widgets数组



## 2.2 Column构造函数

**参数与`Row`类似，只是主轴和纵轴的反向相反**

## 2.3 Column嵌套Column 或 Row嵌套Row

**如果Row里面嵌套Row，或者Column里面再嵌套Column，那么只有对最外面的Row或Column会占用尽可能大的空间，里面Row或Column所占用的空间为实际大小**，下面以Column为例说明：

	Container(
	  color: Colors.green,
	  child: Padding(
	    padding: const EdgeInsets.all(16.0),
	    child: Column(
	      crossAxisAlignment: CrossAxisAlignment.start,
	      mainAxisSize: MainAxisSize.max, //有效，外层Colum高度为整个屏幕
	      children: <Widget>[
	        Container(
	          color: Colors.red,
	          child: Column(
	            mainAxisSize: MainAxisSize.max,//无效，内层Colum高度为实际高度  
	            children: <Widget>[
	              Text("hello world "),
	              Text("I am Jack "),
	            ],
	          ),
	        )
	      ],
	    ),
	  ),
	);

如果要让里面的`Column`占满外部`Column`，可以使用`Expanded`组件：

	Expanded( 
	  child: Container(
	    color: Colors.red,
	    child: Column(
	      mainAxisAlignment: MainAxisAlignment.center, //垂直方向居中对齐
	      children: <Widget>[
	        Text("hello world "),
	        Text("I am Jack "),
	      ],
	    ),
	  ),
	)	

- `Expanded`必须作为`Flex`类型widget的直接子类	

## 2.4 对齐widget

**对于线性布局，有主轴和纵轴之分，如果布局是沿水平方向，那么主轴就是指水平方向，而纵轴即垂直方向；如果布局沿垂直方向，那么主轴就是指垂直方向，而纵轴就是水平方向**

- 在线性布局中，有两个定义对齐方式的枚举类`MainAxisAlignment`和`CrossAxisAlignment`，分别代表主轴对齐和纵轴对齐


通过使用`mainAxisAlignment`和`crossAxisAlignment`属性可以对齐子项,**`MainAxisAlignment`以及`CrossAxisAlignment`类提供了许多控制对齐的常量**

- 对于`Row`来说，主轴是水平方向，横轴是垂直方向

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3ftn5uxbrj208004kt8r.jpg)

- 对于`Column`来说，主轴是垂直方向，横轴是水平方向

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3ftnhfzpzj204308u3yk.jpg)


## 2.5 调整widget

`Expanded`可以用来实现widget的占比，即某个widget占总体布局的几分之几。例如将`Row`中的子项用`Expanded`进行包装，就可以控制沿主轴方向的widget大小

- `Expanded`具有一个`flex`属性，它是一个整数类型的属性，用于确定widget的弹性系数，默认的弹性系数为1

## 2.6 聚集widget

默认情况下，`Row`或`Column`沿着其主轴会尽可能占用尽可能多的空间(即`mainAxisSize = MainAxisSize.max`)，但如果要将子widget紧密聚集在一起，可以通过设置`mainAxisSize`为`MainAxisSize.min`

- **`mainAxisAlignment`属性的效果会覆盖`mainAxisSize`属性的效果**

## 2.7 嵌套行和列

布局框架允许在`Row`或`Column`内部再嵌套`Row`或`Column`

- **为了最大限度的减少过度嵌套而导致的视觉混淆，可以借助变量或函数去实现UI的各个部分**

# 3. 弹性布局

**弹性布局允许子widget按照一定比例来分配父容器空间，Flutter中的弹性布局主要通过`Flex`和`Expanded`来配合实现**


## 3.1 Flex 

`Flex`可以沿着水平或垂直方向排列子widget

- 如果知道主轴方向，使用Row或Column会方便一些，因为Row和Column都继承自Flex，参数基本相同，**所以能使用Flex的地方一定可以使用Row或Column**

- Flex本身功能是很强大的，它也可以和Expanded配合实现弹性布局


### 3.1.1 构造函数

	Flex({
	  ...省略部分参数...
	  @required this.direction, //弹性布局的方向, Row默认为水平方向，Column默认为垂直方向
	  List<Widget> children = const <Widget>[],
	})

- 大多数参数在`Row`和`Column`中介绍过了,因此省略了

- `direction`:

	弹性布局的方向, Row默认为水平方向，Column默认为垂直方向


`Flex`继承自`MultiChildRenderObjectWidget`，对应的`RenderObject`为`RenderFlex`，`RenderFlex`中实现了其布局算法



## 3.2 Expanded

`Expanded`可以按比例"拉长"`Row`、`Column`和`Flex`的子widget所占用的空间

	const Expanded({
	  int flex = 1, 
	  @required Widget child,
	})

- `flex`为弹性系数，**如果为0或null，则child是没有弹性的，即不会被"拉长"占用的空间，只会占用固定的大小**

	所有的`flex>0`的Expanded都会按照其flex的比例来分割主轴的全部空闲空间


示例：

	Flex(
	    direction: Axis.vertical,
	    children: <Widget>[
	      Expanded(
	        flex: 1,
	        child: Container(
	          color: Colors.red,
	        ),
	      ),
	      Expanded(
	        flex: 1,
	        child: Container(
	          color: Colors.yellow,
	        ),
	      ),
	      Spacer(
	        flex: 3,
	      )
	    ],
  	)

- `Spacer`只是一个`Expanded`的包装类，主要作用就是占用指定比例的空间


# 4. 流式布局
**Flutter把超出屏幕显示范围会自动折行的布局称为流式布局**

- Flutter提供了`Wrap`和`Flow`用来支持流式布局


## 4.1 Wrap

	Wrap({
	  ...
	  this.direction = Axis.horizontal,
	  this.alignment = WrapAlignment.start,
	  this.spacing = 0.0,
	  this.runAlignment = WrapAlignment.start,
	  this.runSpacing = 0.0,
	  this.crossAxisAlignment = WrapCrossAlignment.start,
	  this.textDirection,
	  this.verticalDirection = VerticalDirection.down,
	  List<Widget> children = const <Widget>[],
	})

- **`Wrap`除了超出显示范围后会折行外，其大部分行为和`Row`,`Flex`,`Column`相同**

`Wrap`存在几个特有的属性:

- `spacing`：主轴方向子widget的间距

- `runSpacing`：纵轴方向的间距

- `runAlignment`：纵轴方向的对齐方式


示例：

	Wrap(
      direction: Axis.horizontal,
      spacing: 8.0, // 主轴(水平)方向间距
      runSpacing: 4.0, // 纵轴（垂直）方向间距
      alignment: WrapAlignment.center, //沿主轴方向居中
      children: <Widget>[
        Container(
          width: 200,
          height: 50,
          color: Colors.yellow,
        ),
        Container(
          width: 200,
          height: 50,
          color: Colors.red,
        ),
        Container(
          width: 200,
          height: 50,
          color: Colors.blue,
        ),
      ],
    )

- `Wrap`子Widget的长宽如果超出屏幕范围，不会报错

- `Wrap`需要在`MaterialApp`中

## 4.2 Flow

**Flow主要用于一些需要自定义布局策略或性能要求较高(如动画中)的场景**

其具有如下优点：

1. 性能好；Flow是一个对child尺寸以及位置调整非常高效的布局类widget，Flow用转换矩阵（`transformation matrices`）在对child进行位置调整的时候进行了优化：在Flow定位过后，如果child的尺寸或者位置发生了变化，在`FlowDelegate`中的`paintChildren()`方法中调用`context.paintChild` 进行重绘，而`context.paintChild`在重绘时使用了转换矩阵（`transformation matrices`），并没有实际调整Widget位置

2. 灵活: 由于需要自己实现`FlowDelegate`的`paintChildren()`方法，所以需要自己计算每一个widget的位置，因此，可以自定义布局策略

- 但是一般不建议使用Flow，因为其过于复杂，许多场景优先考虑使用`Wrap`

缺点：

1. 使用复杂

2. **不能自适应子widget大小，必须通过指定父容器大小或实现`TestFlowDelegate`的`getSize()`返回固定大小**

	
示例


	Flow(
	  delegate: TestFlowDelegate(margin: EdgeInsets.all(10.0)),
	  children: <Widget>[
	    new Container(width: 80.0, height:80.0, color: Colors.red,),
	    new Container(width: 80.0, height:80.0, color: Colors.green,),
	    new Container(width: 80.0, height:80.0, color: Colors.blue,),
	    new Container(width: 80.0, height:80.0,  color: Colors.yellow,),
	    new Container(width: 80.0, height:80.0, color: Colors.brown,),
	    new Container(width: 80.0, height:80.0,  color: Colors.purple,),
	  ],
	)
	
	
	class TestFlowDelegate extends FlowDelegate {
	  EdgeInsets margin = EdgeInsets.zero;
	  TestFlowDelegate({this.margin});
	  @override
	  void paintChildren(FlowPaintingContext context) {
	    var x = margin.left;
	    var y = margin.top;
	    //计算每一个子widget的位置  
	    for (int i = 0; i < context.childCount; i++) {
	      var w = context.getChildSize(i).width + x + margin.right;
	      if (w < context.size.width) {
	        context.paintChild(i,
	            transform: new Matrix4.translationValues(
	                x, y, 0.0));
	        x = w + margin.left;
	      } else {
	        x = margin.left;
	        y += context.getChildSize(i).height + margin.top + margin.bottom;
	        //绘制子widget(有优化)  
	        context.paintChild(i,
	            transform: new Matrix4.translationValues(
	                x, y, 0.0));
	         x += context.getChildSize(i).width + margin.left + margin.right;
	      }
	    }
	  }
	
	  @override
	  getSize(BoxConstraints constraints){
	    //指定Flow的大小  
	    return Size(double.infinity,200.0);
	  }
	
	  @override
	  bool shouldRepaint(FlowDelegate oldDelegate) {
	    return oldDelegate != this;
	  }
	}


- 主要就是去实现`paintChildren()`方法的逻辑，它是确定每个widget的位置

- 由于`Flow`不能自适应子widget，因此需要在`getSize()`方法中返回一个固定大小来指定`Flow`大小


# 5. 层叠布局
Flutter中使用`Stack`和`Positioned`来实现绝对定位，`Stack`允许子widget堆叠，而`Positioned`可以给子widget定位（根据Stack的四个角）

- 层叠布局和Android中的Frame布局是相似的，子widget可以根据到父容器四个角的位置来确定本身的位置

- `Positioned`允许子widget堆叠（按照代码中声明的顺序）

## 5.1 Stack

	Stack({
	  this.alignment = AlignmentDirectional.topStart,
	  this.textDirection,
	  this.fit = StackFit.loose,
	  this.overflow = Overflow.clip,
	  List<Widget> children = const <Widget>[],
	})

- `alignment`：

	**此参数决定如何去对齐没有定位（没有使用Positioned）或部分定位的子widget**
	
	所谓部分定位，在这里**特指没有在某一个轴上定位：**left、right为横轴，top、bottom为纵轴，只要包含某个轴上的一个定位属性就算在该轴上有定位

- `textDirection`：

	**和Row、Wrap的`textDirection`功能一样，都用于决定`alignment`对齐的参考系**
	
	即`textDirection`的值为`TextDirection.ltr`时，则alignment的start代表左，end代表右，即从左往右的顺序
	
	`textDirection`的值为`TextDirection.rtl`，则alignment的start代表右，end代表左，即从右往左的顺序

- `fit`：

	**此参数用于决定没有定位的子widget如何去适应Stack的大小**
	
	`StackFit.loose`表示使用子widget的大小，`StackFit.expand`表示扩伸到Stack的大小

- `overflow`：

	**此属性决定如何显示超出Stack显示空间的子widget**，值为`Overflow.clip`时，超出部分会被剪裁（隐藏），值为`Overflow.visible `时则不会



## 5.2 Positioned

	const Positioned({
	  Key key,
	  this.left, 
	  this.top,
	  this.right,
	  this.bottom,
	  this.width,
	  this.height,
	  @required Widget child,
	})

- `left`、`top` 、`right`、 `bottom`分别代表离Stack左、上、右、底四边的距离

- `width`和`height`用于指定定位元素的宽度和高度，注意，此处的`width`、`height`和其它地方的意义稍微有点区别，此处用于配合left、top 、right、 bottom来定位widget(如果已经指定了子控件的width,可以只使用left,right,top,bottom之一)

	举个例子，在水平方向时，你只能指定left、right、width三个属性中的两个，如指定left和width后，right会自动算出(left+width)，**如果同时指定三个属性则会报错**，垂直方向同理

	此外`Positioned`的`width`和`height`优先级高于子widget的`width`和`height`

- **`Positioned`必须和`Stack`搭配使用**

- 在`Stack`中只有没有使用`Positioned`进行包裹的widget 才会使用`Stack`的`alignment`属性


