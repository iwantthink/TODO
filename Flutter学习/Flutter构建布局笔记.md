# Flutter笔记

[Flutter快速上车之Widget - 闲鱼技术](https://juejin.im/post/5b8ce76f51882542c0626887)


# 重点

这篇笔记并不使用Scaffold，而是直接使用自定义的widget

- **大部分的widget都必须设置`textDirection`!否则可能出现异常!**

	或者通过`Directionality`来进行包装,以提供`textDirction`



**FLutter的架构:**

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3g17wizpkj20zk0ijjrn.jpg)

- flutter是视图描述的基础，其核心思想便是 一切皆widget





# 1 布局widget

## 1.1 重点

1. 即使应用程序本身也是一个 widget

2. 创建一个widget并将其添加到布局widget中是很简单的

3. 要在设备上显示widget，请将布局widget添加到 app widget中

4. 使用Scaffold是最容易的，它是 Material Components库中的一个widget，它提供了一个默认banner，背景颜色，并且具有添加drawer，snack bar和底部sheet的API。

5. 可以构建仅使用标准widget库中的widget来构建您的应用程序


## 1.2 流程

1. 选择一个widget来保存文本，图标或图像

2. 创建一个widget来容纳可见对象

	例如Text保存文本，Image保存图片，Icon保存图标等等

3. 将可见widget添加到布局widget

	布局widget可以约束子widget，其特性会传递到所包含的widget
	
	所有布局widget都有一个`child`属性(例如Center,Container等),或一个`children`属性(例如Row,Column,ListView,Stack等). 前者接收一个子widget，后者接收一组子widget

4. 将布局widget添加到页面

	Flutter应用本身就是一个Widget，大部分widget都有一个`build()`方法，该方法用来接收一个显示在设备上的widget
	
	对于Material风格的应用程序，它已经定义好了Appbar，标题或背景颜色等。但是非Material风格的应用程序需要开发者自己去定义
	
	
# 2 垂直和水平widget

## 2.1 重点

1. 行widget和列widget是两种最常用的布局widget,其允许最大程度的自定义

2. 行和列都需要一个子widget列表

2. 子widget本身可以是行、列或其他复杂widget

3. 可以指定行或列如何在垂直或水平方向上对齐其子项

4. 可以拉伸或限制特定的子widget

5. 可以指定子widget如何使用行或列的可用空间


## 2.2 对齐widget

通过使用`mainAxisAlignment`和`crossAxisAlignment`属性可以对齐子项,**`MainAxisAlignment`以及`CrossAxisAlignment`类提供了许多控制对齐的常量**

- 对于`Row`来说，主轴是水平方向，横轴是垂直方向

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3ftn5uxbrj208004kt8r.jpg)

- 对于`Column`来说，主轴是垂直方向，横轴是水平方向

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3ftnhfzpzj204308u3yk.jpg)


## 2.3 调整widget

`Expanded`可以用来实现widget的占比，即某个widget占总体布局的几分之几。例如将`Row`中的子项用`Expanded`进行包装，就可以控制沿主轴方向的widget大小

- `Expanded`具有一个`flex`属性，它是一个整数类型的属性，用于确定widget的弹性系数，默认的弹性系数为1

## 2.4 聚集widget

默认情况下，`Row`或`Column`沿着其主轴会尽可能占用尽可能多的空间，但如果要将子widget紧密聚集在一起，可以通过设置`mainAxisSize`为`MainAxisSize.min`

- **`mainAxisAlignment`属性的效果会覆盖`mainAxisSize`属性的效果**

## 2.5 嵌套行和列

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
	








