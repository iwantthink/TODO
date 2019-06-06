# Flutter布局类widget

[Flutter快速上车之Widget - 闲鱼技术](https://juejin.im/post/5b8ce76f51882542c0626887)

[在Flutter中构建布局](https://flutterchina.club/tutorials/layout/)

# 重点

这篇笔记并不使用Scaffold，而是直接使用自定义的widget

- **大部分的widget都必须设置`textDirection`!否则可能出现异常!**

	或者通过`Directionality`来进行包装,以提供`textDirction`

**FLutter的架构:**

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3g17wizpkj20zk0ijjrn.jpg)

- flutter是视图描述的基础，其核心思想便是 一切皆widget


# 1 简介

布局类Widget都会包含一个或多个子widget，不同的布局类Widget对子widget排版(layout)方式不同

- **Element树才是最终的绘制树，Element树是通过widget树来创建的（通过Widget.createElement()），widget其实就是Element的配置数据**

Flutter中，根据Widget是否需要包含子节点以及子节点的数量 将Widget分为了三类，分别对应三种Element，如下表：


Widget	|对应的Element |	用途
:---|:---|---
`LeafRenderObjectWidget`|`	LeafRenderObjectElement`|Widget树的叶子节点，**用于没有子节点的widget**，通常基础widget都属于这一类，如Text、Image
`SingleChildRenderObjectWidget`|`SingleChildRenderObjectElement`|**包含一个子Widget**，如：ConstrainedBox、DecoratedBox等
`MultiChildRenderObjectWidget`|`MultiChildRenderObjectElement|**包含多个子Widget**，一般都有一个children参数，接受一个Widget数组。如Row、Column、Stack等


- 注意:

	**Flutter中的很多Widget是直接继承自`StatelessWidget`或`StatefulWidget`，然后在`build()`方法中构建真正的`RenderObjectWidget`**
	
	例如`Text`，它其实是继承自`StatelessWidget`，然后在`build()`方法中通过`RichText`来构建其子树，而RichText才是继承自LeafRenderObjectWidget
	
	因此也可以直接说`Text`属于`LeafRenderObjectWidget`（其它widget也可以这么描述），这才是本质。**那么也就是说其实StatelessWidget和StatefulWidget就是两个用于组合Widget的基类，它们本身并不关联最终的渲染对象（RenderObjectWidget）**

## 1.1 布局类Widget

**布局类Widget就是指直接或间接继承(包含)MultiChildRenderObjectWidget的Widget，它们一般都会有一个children属性用于接收子Widget**

其继承关系 

	Widget > RenderObjectWidget > (Leaf/SingleChild/MultiChild)RenderObjectWidget 
	
- **`RenderObjectWidget`类中定义了一个`RenderObject`对象，并且提供了创建、更新`RenderObject`的方法，子类必须实现这些方法**

- `RenderObject`是最终布局、渲染UI界面的对象，也就是说，对于布局类Widget来说，其布局算法都是通过对应的`RenderObject`对象来实现的

	所以如果对某个布局类Widget原理感兴趣，可以查看其RenderObject的实现
	

# 2 线性布局Row和Column
**所谓线性布局，即指沿水平或垂直方向排布子Widget。Flutter中通过`Row`和`Column`来实现线性布局，Row和Column都继承自`Flex`**


2. 行和列都需要一个子widget列表

2. 子widget本身可以是行、列或其他复杂widget

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

	表示水平方向子widget的布局顺序(是从左往右还是从右往左)，默认为系统当前Locale环境的文本方向(如中文、英语都是从左往右，而阿拉伯语是从右往左)

- `mainAxisSize`：

	**表示Row在主轴(水平)方向占用的空间，默认值是`MainAxisSize.max`，**表示尽可能多的占用水平方向的空间，此时无论子widgets实际占用多少水平空间，Row的宽度始终等于水平方向的最大宽度；
		
	**`MainAxisSize.min`表示尽可能少的占用水平空间，当子widgets没有占满水平剩余空间，则Row的实际宽度等于所有子widgets占用的的水平空间**

- `mainAxisAlignment`：

	**表示子Widgets在所占用的水平空间内对齐方式，但如果父类`Row`的`mainAxisSize`值为`MainAxisSize.min`，那么对于子类的`Column`或`Row`,分别对应的`crossAxisAlignment`或`mainAxisAlignment`无意义，因为此时子widgets的宽度等于Row的宽度**
	
	`MainAxisAlignment.start`表示子widget沿`textDirection`的初始方向对齐，假如`textDirection`取值为`TextDirection.ltr`时，则`MainAxisAlignment.start`表示子widget沿主轴左对齐，`textDirection`取值为`TextDirection.rtl`时表示沿主轴右对齐
	
	`MainAxisAlignment.end`和`MainAxisAlignment.start`正好相反,`MainAxisAlignment.center表`示居中对齐
	
	**可以这么理解：`textDirection`是`mainAxisAlignment`的参考系**

- `verticalDirection`：

	表示Row纵轴（垂直）的对齐方向，默认是`VerticalDirection.down`，表示从上到下

- `crossAxisAlignment`：

	表示子Widgets在纵轴方向的对齐方式，Row的高度等于子Widgets中最高的子元素高度，它的取值和MainAxisAlignment一样(包含start、end、 center三个值)，**不同的是crossAxisAlignment的参考系是verticalDirection**
	
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

默认情况下，`Row`或`Column`沿着其主轴会尽可能占用尽可能多的空间，但如果要将子widget紧密聚集在一起，可以通过设置`mainAxisSize`为`MainAxisSize.min`

- **`mainAxisAlignment`属性的效果会覆盖`mainAxisSize`属性的效果**

## 2.7 嵌套行和列

布局框架允许在`Row`或`Column`内部再嵌套`Row`或`Column`

- **为了最大限度的减少过度嵌套而导致的视觉混淆，可以借助变量或函数去实现UI的各个部分**

# 3 常见的布局widget

widget分为两类：`widgets library`中的标准widget和`Material Components library`中的专用widget

- 任何应用程序都可以使用`widgets library`中的widget，但只有Material应用程序可以使用`Material Components`库

## 3.1 标准widget

### 3.1.1 Container

`Container`支持添加 padding, margins, borders, background color, 或将其他装饰属性添加到widget. 此外`Container`仅支持接收单个子项,但是该子项可以是`Row`,`Column`甚至是widget树的根

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3fut7j65zj208e05wa9x.jpg)

### 3.1.2 GridView

当GridView检测到其内容过长而不适合渲染框时，会自动提供滚动!

- `GridView`提供了四种构造函数

	1. `GridView.builder`
	
	2. `GridView.custom`
	
	3. `GridView.count` : 允许指定列数
	
	4. `GridView.extent`：允许指定子项的最大像素宽度,默认俩列

**注意：**

- **如果直接在`StatelessWidget`的`build()`方法中返回`GridView`,会出现渲染失败的异常,这时需要借助一个`Directionality`!!!**

- 在显示二维列表时，如果希望灵活控制指定行或列的子widget，应该使用`Table`或`DataTable`


示例:

	class TestWidget extends StatelessWidget {
	  @override
	  Widget build(BuildContext context) {
	    return Directionality(
	        textDirection: TextDirection.ltr,
	        child: GridView.count(
	          crossAxisCount: 3,
	          children: List.generate(100, (index) {
	            return Text("hello world $index");
	          }),
	        ));
	  }
	}


### 3.1.3 ListView

`List`是类似列的widget,当检测到其内容过长而不适合渲染框时，会自动提供滚动！

主要特点:

- 用于组织盒子中列表的特殊Column

- 可以水平或垂直放置

- 检测它的内容超过显示框时提供滚动

- 比Column配置少，但更易于使用并支持滚动


示例:

	class TestWidget extends StatelessWidget {
	  @override
	  Widget build(BuildContext context) {
	    return Directionality(
	        textDirection: TextDirection.ltr,
	        child: ListView(
	          children: List.generate(100, (index){
	            return Text("hello world $index");
	          }),
	        ));
	  }
	}


### 3.1.4 Stack

`Stack`用来将一个widget重叠在另一个widget之上，可以完全或者部分重叠底部的widget

主要特点:

- 用于与另一个widget重叠的widget

- 子列表中的第一个widget是基础widget,随后的子widget被覆盖在基础widget的顶部

- Stack的内容不能滚动

- 可以选择剪切超过渲染框的子项


示例:

	class TestWidget extends StatelessWidget {
	  @override
	  Widget build(BuildContext context) {
	    return Stack(
	      alignment: Alignment(0.6, 0.6),
	      children: <Widget>[
	        Image.asset("images/lake.jpg"),
	        Image.asset(
	          "images/lake.jpg",
	          width: 200,
	          height: 200,
	        )
	      ],
	    );
	  }
	}

## 3.2 Material Components

### 3.2.1 Card

`Card`用于将相关内容放到带圆角和投影的盒子中,通常与`ListTile`一起使用

- Card有一个子项， 但它可以是列(`Column`)，行(`Row`)，列表(`ListView`)，网格(`GridView`)或其他小部件

- 默认情况下，Card将其大小缩小为0像素。可以使用`SizedBox`来限制Card的大小

- 在Flutter中，Card具有圆角和阴影，这使它有一个3D效果

	Card的`elevation`属性用来控制投影效果。 例如，将`elevation`设置为24.0，将会使Card从视觉上抬离表面并使阴影变得更加分散。 
	
	**如果指定不支持的值将会完全禁用投影**

主要特点:

- 实现了一个 Material Design card

- 接受单个子项，但该子项可以是Row，Column或其他包含子级列表的widget

- 显示圆角和阴影

- Card内容不能滚动

- Material Components 库的一个widget


示例:

	class TestWidget extends StatelessWidget {
	  @override
	  Widget build(BuildContext context) {
	  
	    return Directionality(
	        textDirection: TextDirection.ltr,
	        child: Card(
	          child: Text("testCard"),
	        ));
	  }
	}

### 3.2.2 ListTile

`ListTile`是一个专门的行级widget,用于创建最多包含3行文字，以及可选的行前和和行尾的图标的行

- 常用在`ListView`或`Card`中,但不仅局限于这俩个

主要特点:

- 包含最多3行文本和可选图标的专用行

- 比起Row不易配置，但更易于使用

- Material Components 库里的widget


示例:

	class TestWidget extends StatelessWidget {
	  @override
	  Widget build(BuildContext context) {
	    var card = new SizedBox(
	      height: 100.0,
	      child: new Card(
	        child: new Column(
	          children: [
	            new ListTile(
	              title: new Text('(408) 555-1212',
	                  style: new TextStyle(fontWeight: FontWeight.w500)),
	              leading: new Icon(
	                Icons.contact_phone,
	                color: Colors.blue[500],
	              ),
	            ),
	            new Divider(),
	            new ListTile(
	              title: new Text('costa@example.com'),
	              leading: new Icon(
	                Icons.contact_mail,
	                color: Colors.blue[500],
	              ),
	              trailing: Icon(
	                Icons.star,
	                color: Colors.blue[500],
	              ),
	            ),
	          ],
	        ),
	      ),
	    );
	
	    return Directionality(
	        textDirection: TextDirection.ltr,
	        child: MediaQuery(data: MediaQueryData(), child: card));
	  }
	}
	








