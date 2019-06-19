# 自定义widget
[自定义widget](https://github.com/flutterchina/flutter-in-action/blob/master/docs/chapter13/index.md)

# 1. 简介

当Flutter提供的现有Widget无法满足需求，或者为了共享代码需要封装一些通用Widget，这时就需要自定义Widget

在Flutter中自定义Widget有三种方式：**通过组合Widget、自绘和实现`RenderObject`**

# 2. 组合widget

这种方式是通过拼装其它低级别的Widget来组合成一个高级别的Widget，例如`Container`就是一个组合Widget，它是由`DecoratedBox`、`ConstrainedBox`、`Transform`、`Padding`、`Align`等组成

在Flutter中，组合的思想非常重要，**Flutter提供了非常多的基础Widget，而界面开发都是按照需要组合这些Widget来实现各种不同的布局**

## 2.1 示例(支持渐变背景的按钮)

参考Flutter中`RaisedButton`的实现，它从构造函数中接收一个widget以及控件的绘制信息，在其`build`方法中借助`InkWell`,`Material`,`DecoratedBox`等实现效果

	class CustomButton extends StatelessWidget {
	  var child;
	  var colors = [];
	  var onPress;
	  var height;
	  var width;
	
	  CustomButton(
	      {Key key,
	      this.child,
	      this.colors,
	      this.onPress,
	      this.width = 200.0,
	      this.height = 100.0})
	      : assert(colors != null),
	        assert(child != null),
	        super(key: key);
	
	  @override
	  Widget build(BuildContext context) {
	    Object widget = InkWell(
	      splashColor: colors.last,
	      highlightColor: Colors.transparent,
	      onTap: onPress,
	      child: Container(
	        width: width,
	        height: height,
	        child: child,
	      ),
	    );
	
	    widget = Material(
	      child: widget,
	      type: MaterialType.transparency,
	    );
	    var decoratedWidget = DecoratedBox(
	      child: widget,
	      decoration: BoxDecoration(gradient: LinearGradient(colors: colors)),
	    );
	    return decoratedWidget;
	  }
	}

- 实际上Button比这个复杂的多，具体的实现可以参考`RaisedButton`	
- **通过组合的方式定义Widget和普通的写界面并无差异，不过在抽离出单独的Widget时需要考虑代码规范性，如必要参数要用`@required `标注，对于可选参数在特定场景需要判空或设置默认值等**

	这是由于使用者大多时候可能不了解Widget的内部细节，所以为了保证代码健壮性，需要在用户错误地使用Widget时能够兼容或报错提示（使用assert断言函数）

## 2.2 示例(附加动画的旋转组件)
自定义一个`CustomRotated`组件，使用者传入旋转角度等信息，widget产生角度变换并附带一个动画效果


	class CustomRotated extends StatefulWidget {
	  var turn;
	  var duration;
	  var child;
	
	  CustomRotated({this.duration, turn, this.child})
	      : turn = turn / 360 * (pi * 2);
	
	  @override
	  State<StatefulWidget> createState() {
	    return CustomRotatedState();
	  }
	}

	class CustomRotatedState extends State<CustomRotated>
	    with SingleTickerProviderStateMixin {
	  double currentRotation = 0;
	  AnimationController controller;
	
	  @override
	  void initState() {
	    super.initState();
	    print("init state");
	    controller = AnimationController(
	      vsync: this,
	      duration: Duration(milliseconds: widget.duration),
	      lowerBound: 0,
	      upperBound: double.infinity,
	    );
	    controller.value = currentRotation;
	    currentRotation += widget.turn;
	  }
	
	  @override
	  void didUpdateWidget(CustomRotated oldWidget) {
	    super.didUpdateWidget(oldWidget);
	    if (controller.isCompleted || controller.isDismissed) {
	      currentRotation += widget.turn;
	      controller.animateTo(currentRotation);
	    }
	  }
	
	  @override
	  Widget build(BuildContext context) {
	    return AnimatedBuilder(
	        child: widget.child,
	        animation: controller,
	        builder: (ctx, child) {
	          return Container(
	            color: Colors.yellow,
	            alignment: Alignment.center,
	            width: 200,
	            height: 100,
	            child: Transform.rotate(
	              angle: controller.value,
	              child: GestureDetector(
	                child: child,
	              ),
	            ),
	          );
	        });
	  }
	
	  @override
	  void dispose() {
	    super.dispose();
	    controller.dispose();
	  }
	}

- **`AnimationController`可以通过设置`lowerBound`和`upperBound`来指定产生值的最小值和最大值**

- **通过`AnimationController.animateTo()`可以使动画的当前值不断增加到一个target值**


# 3. 自绘

对于一些复杂或不规则的UI，可能无法使用现有Widget组合的方式来实现，比如需要一个正六边形、一个渐变的圆形进度条、一个棋盘等，当然有时候可以使用图片来实现，但在一些需要动态交互的场景静态图片是实现不了的，比如要实现一个手写输入面板。这时，就需要来自己绘制UI外观

- **Flutter中提供了`CustomPaint`和`Canvas`进行自绘UI外观**

几乎所有的UI系统都会提供一个自绘UI的接口，这个接口通常会提供一块2D画布`Canvas`，`Canvas`内部封装了一些基本绘制的API，开发者可以通过`Canvas`绘制各种自定义图形。在Flutter中，提供了一个`CustomPaint`组件，它可以结合一个画笔`CustomPainter`来实现绘制自定义图形


## 3.1 CustomPaint
**`CustomPaint`组件主要用来提供`Canvas`**,当进行绘制时，`CustomPaint`先是用它的`painter`在当前画布上进行绘制，去绘制其子类。然后在绘制完子类之后，会使用它的`foregroundPainter`去绘制

- 画布的坐标系统和`CustomPaint`对象的坐标系统相同，`Painter`会在从原点开始的矩形内绘画，并包含给定大小的区域。（**如果`Painter`在这些界限之外绘画，可能没有足够的内存分配来栅格化绘画命令，结果的行为是未定义的，这时可以使用`SizeBox`或`FittedBox`限制绘制范围**）

构造函数:

	const CustomPaint({
	  Key key,
	  this.painter, 
	  this.foregroundPainter,
	  this.size = Size.zero, 
	  this.isComplex = false, 
	  this.willChange = false, 
	  Widget child, //子节点，可以为空
	})

- `painter`: 背景画笔，会显示在子节点后面

- `foregroundPainter`: 前景画笔，会显示在子节点前面

	**画笔需要继承`CustomPainter`类，画笔类中提供了真正实现的绘制逻辑**

- `size`：**当child为null时，代表默认绘制区域大小，如果有child则忽略此参数，画布尺寸则为child尺寸**

	如果有child但是想指定画布为特定大小，可以使用`SizeBox`包裹`CustomPaint`实现

- `isComplex`：是否复杂的绘制，如果是，Flutter会应用一些缓存策略来减少重复渲染的开销

- `willChange`：和`isComplex`配合使用，当启用缓存时，该属性代表在下一帧中绘制是否会改变

**如果`CustomPaint`有子节点，为了避免子节点不必要的重绘并提高性能，通常情况下都会将子节点包裹在`RepaintBoundary` 组件中**，这样会在绘制时创建一个新的绘制层（Layer），其子Widget将在新的Layer上绘制，而父Widget将在原来Layer上绘制，也就是说`RepaintBoundary` 子Widget的绘制将独立于父Widget的绘制，`RepaintBoundary`会隔离其子节点和`CustomPaint`本身的绘制边界

示例如下：

	CustomPaint(
	  size: Size(300, 300), //指定画布大小
	  painter: MyPainter(),
	  child: RepaintBoundary(child:...)), 
	)

## 3.2 CustomPainter
**实现一个自定义`Painter`有俩种方式**

1. 继承`Painter`,必须重写`paint()`或`shouldRepaint()`方法

2. 实现此`Painter`接口以定义自定义绘制委托


- 每当需要重新绘制自定义对象时，`paint`方法就会被调用,这个方法也是具体绘制的地方

- 当提供一个新的实例时，`shouldRepaint()`方法会被调用,以检查新的实例是否真正的表示不同的信息


### 3.2.1 CustomPaint.paint()

`CustomPainter`中提定义了一个虚函数`paint()`：

	void paint(Canvas canvas, Size size);

- `Canvas`：一个画布，使用画笔进行绘制

	API名称	|功能
	---|---
	drawLine	|画线
	drawPoint	|画点
	drawPath	|画路径
	drawImage	|画图像
	drawRect	|画矩形
	drawCircle	|画圆
	drawOval	|画椭圆
	drawArc	|画圆弧

- `Size`：当前绘制区域大小。


### 3.2.2 Paint
**`Paint`用于描述在画布上绘画时使用的样式**


`Paint`的构造函数为无参构造函数，主要的设置是通过调用其方法

	var paint = Paint() //创建一个画笔并配置其属性
	  ..isAntiAlias = true //是否抗锯齿
	  ..style = PaintingStyle.fill //画笔样式：填充
	  ..color=Color(0x77cdb175);//画笔颜色

## 3.3 示例

### 3.3.1 绘制一个圆形

自定义绘制时的基本流程:

	class CustomGomoku extends StatelessWidget {
	  @override
	  Widget build(BuildContext context) {
	    return Center(
	      child: CustomPaint(
	        size: Size(50, 50),
	        painter: Gomoku(),
	      ),
	    );
	  }
	}
	
	class Gomoku extends CustomPainter {
	  Paint mPaint;
	
	  Gomoku() {
	    mPaint = new Paint()
	      ..isAntiAlias = true
	      ..color = Colors.orange;
	  }
	
	  @override
	  void paint(Canvas canvas, Size size) {
	    canvas.drawCircle(Offset(25, 25), 25, mPaint);
	  }
	
	  @override
	  bool shouldRepaint(CustomPainter oldDelegate) {
	    return false;
	  }
	}

## 3.3.2 示例2（自定义进度条）

如果遇到无法通过系统提供的现有Widget实现的UI时，例如需要一个渐变圆形进度条，而Flutter提供的`CircularProgressIndicator`并不支持在显示精确进度时对进度条应用渐变色（其`valueColor `属性只支持执行旋转动画时变化`Indicator`的颜色），这时最好的方法就是通过自定义Widget绘制逻辑来画出期望的外观

[自定义进度条](https://github.com/flutterchina/flutter-in-action/blob/master/docs/chapter13/gradient_circular_progress_demo.md)


## 3.4 性能

**Flutter中绘制是比较消耗资源的操作，在实现自绘控件时应该考虑到性能开销，下面是两条关于性能优化的建议：**

- **尽可能的利用好`shouldRepaint()`返回值，在UI树重新build时，控件在绘制前都会先调用该方法以确定是否有必要重绘**

	假如绘制的UI不依赖外部状态，那么就应该始终返回false，因为外部状态改变导致重新build时不会影响UI外观；如果绘制依赖外部状态，那么就应该在`shouldRepaint()`中判断依赖的状态是否改变，如果已改变则应返回true来重绘，反之则应返回false不需要重绘

- **绘制时尽可能多的分层**
	
	针对那些经常发生变化的内容和不会发生变化的内容，将它们分别放到不同的widget中进行绘制，然后将俩者进行结合

	在上面五子棋的示例中，将棋盘和棋子的绘制放在了一起，这样会有一个问题：由于棋盘始终是不变的，用户每次落子时变的只是棋子，但是如果按照上面的代码来实现，每次绘制棋子时都要重新绘制一次棋盘，这是没必要的。优化的方法就是将棋盘单独抽为一个Widget，并设置其shouldRepaint回调值为false，然后将棋盘Widget作为背景。然后将棋子的绘制放到另一个Widget中，这样落子时只需要绘制棋子

## 3.5 总结

自绘控件非常强大，理论上可以实现任何2D图形外观，实际上Flutter提供的所有Widget最终都是调用Canvas绘制出来的，只不过绘制的逻辑被封装起来了

- 可以查看具有外观样式的Widget的源码，找到其对应的`RenderObject`对象，如`Text`组件最终会通过`RenderParagraph`对象来通过`Canvas`实现文本绘制逻辑




# 4. 实现RenderObject

Flutter提供的任何具有UI外观的Widget，如文本Text、Image都是通过相应的`RenderObject`渲染出来的，如`Text`组件是由`RenderParagraph`渲染，而`Image`是由`RenderImage`渲染

`RenderObject`是一个抽象类，它定义了一个抽象方法`paint(...)`:

	void paint(PaintingContext context, Offset offset)

- `PaintingContext`代表Widget的绘制上下文，**通过`PaintingContext.canvas`可以获得`Canvas`，绘制逻辑主要是通过Canvas API来实现**. 

	**子类需要实现此方法以实现自身的绘制逻辑，如`RenderParagraph`需要实现文本绘制逻辑，而`RenderImage`需要实现图片绘制逻辑**


`RenderObject`中最终也是通过`Canvas`来绘制的，那么通过实现`RenderObject`的方式和上面介绍的通过`CustomPaint`和`Canvas`进行自绘的方式有什么区别？

- `CustomPaint`只是为了方便开发者封装的一个代理类，它直接继承自`SingleChildRenderObjectWidget`，通过`RenderCustomPaint`的`paint`方法将`Canvas`和画笔`Painter`(需要开发者实现)连接起来实现了最终的绘制（绘制逻辑在`Painter`中）


# 5. 总结

组合是自定义组件最简单的方法，**在任何需要自定义的场景下，都应该优先考虑是否能够通过组合来实现**。而自绘和通过实现`RenderObject`的方法本质上是一样的，都需要开发者调用Canvas API手动去绘制UI，缺点是必须了解Canvas API，并且得自己去实现绘制逻辑，而优点是强大灵活，理论上可以实现任何外观的UI



























