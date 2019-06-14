# Flutter动画
[Flutter中的动画](https://flutterchina.club/animations/)


# 1. 动画类型

动画分为两类：基于tween或基于物理

以下部分解释了这些术语的含义，并列出了一些相关的资源

- [Github Flutter Gallery Demo](https://github.com/flutter/flutter/tree/master/examples/flutter_gallery)展示了许多动画的示例,在某些情况下，这些示例是动画的最佳实践

## 1.1 补间(Tween)动画
`Tween`是`in-betweening`的简称。在补间动画中，定义了开始点和结束点,时间线以及 转换时间和速度的曲线，然后会通过框架进行计算如何从开始点过渡到结束点

## 1.2 基于物理的动画

在基于物理的动画中，运动被模拟为与真实世界的行为相似。例如，当你掷球时，它在何处落地，取决于抛球速度有多快、球有多重、距离地面有多远。 类似地，将连接在弹簧上的球落下（并弹起）与连接到绳子上的球放下的方式也是不同



# 2. 常见的动画模式

## 2.1 动画列表或网格

这种模式指的是在网格或者列表类型的widget中 添加或删除时应用动画

- [列表添加/删除时应用动画示例](https://flutterchina.club/catalog/samples/animated-list/)


## 2.2 共享元素转换

这种模式指的是，用户从页面中选择一个元素(通常是图像),然后打开所选元素的详情页面，再打开过程中应用动画。例如在Flutter中，可以使用`Hero`(widget)轻松实现路由(页面)之间的共享元素过渡动画

- `Hero`动画

	1. 在改变位置和大小的同时，hero从一页飞到另一页

	2. hero的边界从一个圆形变成一个正方形，同时它从一个页面飞到另一个页面

- `Flutter Gallery`项目中的Studies-Shrine演示了`hero`动画的示例


## 2.3 交错动画

交错动画指的是，动画被分解为较小的动作，其中一些动作被延迟。 较小的动画是可以连续的，或者可以部分或完全重叠


# 3. 动画的概念和类

重点概念

1. `Animation`对象是Flutter动画库中的一个核心类，它生成指导动画的值

2. `Animation`对象知道动画的当前状态（例如，它是开始、停止还是向前或向后移动），但它不知道屏幕上显示的内容

3. `AnimationController`管理`Animation`

4. `CurvedAnimation`将过程抽象为一个非线性曲线

5. `Tween`在正在执行动画的对象所使用的数据范围之间生成值。例如，`Tween`可能会生成从红到蓝之间的色值，或者从0到255

6. 使用`Listeners`和`StatusListeners`可以监听动画状态改变

Flutter中的动画系统是基于`Animation`对象，widget可以在`build()`函数中读取`Animation`对象的当前值，并且可以监听动画的状态改变.此外还可以将可以将当前动画作为更加复杂的动画的基础


## 3.1 Animation

在Flutter中动画系统的主要构建是`Animation`，其表示特定类型的值，并且该值会随着动画生命周期改变而改变(即在一段时间内值发生改变)

- **大多数执行动画的widget都会接收一个`Animation`对象作为参数,监听该`Animation`对象的值变化**

- Animation对象本身和UI渲染没有任何关系

- **Animation是一个抽象类，它拥有其当前值和状态（完成或停止）**

- Animation对象的输出可以是线性的、曲线的、一个步进函数或者任何其他可以设计的映射

- 根据Animation对象的控制方式，动画可以反向运行，甚至可以在中间切换方向

- Animation还可以生成除double之外的其他类型值，如：Animation<Color> 或 Animation<Size>

- `Animation`的值可以通过其`value`属性获取


### 3.1.1 addListener

每当`Animation`的值发生变化时，就会通知所有使用`addListener()`添加的监听器。通常一个监听动画的`State`对象将在监听器的回调中调用自身的`setState()`方法，以通知widget需要使用动画的新值重新构建

当动画改变值时，有俩个widget可以帮助其他widget进行重建:`AnimatedWidget`和`AnimatedBuilder`

- `AnimatedWidget `:

	**适用于无状态的widget**。使用方式：只需要继承它并实现`build()`方法即可

- `AnimatedBuilder `

	**适用于想要将`Animation`包含在更大的构建函数中的widget**。使用方式：构建`AnimatedBuilder`，并在构造函数中传入`builder`函数,`builder`函数会在动画发生变化时而被调用


### 3.1.2 addStatusListener

Flutter的动画系统提供了`AnimationStatus`，它指示动画将如何随时间演变。每当动画的状态发生变化时，动画就会通知所有通过`addStatusListener()`添加的监听器

- 通常情况下，动画以`dismissed`的状态开始，这意味着它们位于范围的开始（例如当动画的值范围是0.0-1.0时，那么当值为0.0时动画就处于`dismissed`状态）。然后动画可以开始执行(从0.0到1.0)，那么动画就处于`forward`状态，同样也可以反向运行(从1.0到0.0)，那么动画就处于`reverse`状态。最后，如果动画达到其范围的末尾(例如1.0)，那么动画就处于`completed`状态


## 3.2 AnimationController

**创建动画的第一步就是要创建一个`AnimationController`，可以用来控制动画的执行**。 例如可以控制动画的`forward`和`stop`操作，或者`fling`操作(使得动画进行物理模拟，例如弹簧效果)

- **`AnimationController`是一个特殊的`Animation`对象，在屏幕刷新的每一帧，就会生成一个新的值。默认情况下，`AnimationController`在给定的时间段内会线性的生成从0.0到1.0的数字**

**创建了动画控制器之后，就可以开始基于它构建其他动画**。例如，可以创建一个反向动画，就是一个原始动画的镜像，运行方向相反(从1.0到0.0)。此外，还可以创建一个通过`curve`调整的`CurvedAnimation`




示例(创建一个`AnimationController`)

	final AnimationController controller = new AnimationController(
	    duration: const Duration(milliseconds: 2000), vsync: this);

- `AnimationController`具有控制动画的方法。例如`forward()`方法可以启动动画。动画值的产生与屏幕刷新率有关，因此每秒钟通常会产生60个数字，在生成动画值之后，每个`Animation`对象都会去通知监听器

- 当创建一个`AnimationController`时，需要传递一个`vsync`参数，存在`vsync`时会防止屏幕外动画（即动画的UI不在当前屏幕时）消耗不必要的资源

	**通过将`SingleTickerProviderStateMixin`添加到类定义中，可以将有状态的widget作为`vsync`的值**

- `vsync`对象会绑定动画的定时器到一个可视的widget，所以当widget不显示时，动画定时器将会暂停，当widget再次显示时，动画定时器重新恢复执行，这样就可以避免动画相关UI不在当前屏幕时消耗资源。 如果要使用自定义的State对象作为vsync时，请包含`TickerProviderStateMixin`

> 某些情况下,动画的当前值可能会超出`AnimationController`的0.0-1.0的范围



### 3.3 CurvedAnimation

`CurvedAnimation`将动画过程定义为一个非线性曲线

	final CurvedAnimation curve = new CurvedAnimation(parent: controller, curve: Curves.easeIn);

- `Curves`类中提供了许多常用的曲线，此外flutter允许使用自定义的曲线

		class ShakeCurve extends Curve {
		  @override
		  double transform(double t) {
		    return math.sin(t * math.PI * 2);
		  }
		}

`CurvedAnimation `继承自`Animation<double>`

- 它包装了正在修改的对象，因此不需要`AnimationController`来实现曲线

# 4. Tween

默认情况下，`AnimationController`对象的范围从0.0到1.0。如果需要不同范围的值，则可以使用`Tween<T>`，它提供了`begin`和`end`属性用来设置开始值和结束值，此外`Tween`的子类提供了许多针对特定类型的插值(即它可以为许多不同类型的数据提供插值)

- 例如`ColorTween`可以为颜色值提供插值，`RectTween`可以为矩形提供插值

- Flutter支持自定义的`Tween`,只需要继承它并重写`lerp`函数


**`Tween`本身仅定义了如何在俩个值之间进行插值，如果要获取动画当前帧的具体值，还需要`Animation`来确定当前的状态**。有俩种方法可以将`Tween`和一个具体的`Animation`进行结合，从而得到一个具体的值


1. 可以使用`Tween`的`evaluate()`方法去获取`Animation`对应`Tween`的值. 这个方法适用于已经监听动画并且在动画状态发生改变时需要重新构建的widget

2. 使用`Tween`的`animate()`方法可以基础一个原始的`Animation`为`Tween`创建新的`Animation`,其会返回一个新的包含`Tween`插值的`Animation`对象. 这种方法适用于当希望将一个新创建的`Animation`提供给一个额外的widget,该widget可以读取`Tween`的当前值，并监听值的变换


以下示例展示了Tween生成-200.0至0.0的值：

	final Tween doubleTween = new Tween<double>(begin: -200.0, end: 0.0);

- Tween是一个无状态(stateless)对象，需要begin和end值

- **Tween的唯一职责就是定义从输入范围到输出范围的映射。输入范围通常为0.0到1.0，但这不是必须的**


- **Tween继承自`Animatable<T>`，而不是继承自`Animation<T>`**

	Animatable与Animation相似，但不是必须输出double值。例如，`ColorTween`指定两种颜色之间的过渡

		final Tween colorTween =  new ColorTween(begin: Colors.transparent, end: Colors.black54);


## 4.1 Tween.animate()方法示例

使用Tween对象的`animate()`方法并传入一个`Animation`

例如，以下代码在500毫秒内生成从0到255的整数值

	final AnimationController controller = new AnimationController(
	    duration: const Duration(milliseconds: 500), vsync: this);
	Animation<int> alpha = new IntTween(begin: 0, end: 255).animate(controller);

- 注意`animate()`返回的是一个Animation，而不是一个`Animatable`


示例(构建控制器，曲线和Tween)

	final AnimationController controller = new AnimationController(
	    duration: const Duration(milliseconds: 500), vsync: this);
	final Animation curve =
	    new CurvedAnimation(parent: controller, curve: Curves.easeOut);
	Animation<int> alpha = new IntTween(begin: 0, end: 255).animate(curve);








