# Flutter手势

[Flutter中的点击、拖动和其它手势](https://flutterchina.club/gestures/)


# 1. 简介

Flutter中的手势系统有两个独立的层

- 第一层有原始指针(`pointer`)事件，它描述了屏幕上指针（例如，触摸，鼠标和触控笔）的位置和移动

- 第二层有手势，描述由一个或多个指针移动组成的语义动作

# 2. Pointers

指针事件(`PointerEvent`)代表用户与设备屏幕交互的原始数据。以下是四种类型的指针事件:

- `PointerDownEvent`: 指针接触到屏幕的特定位置

- `PointerMoveEvent`: 指针从屏幕上的一个位置移动到另一个位置

- `PointerUpEvent`: 指针停止接触屏幕

- `PointerCancelEvent`: 指针的输入事件不再针对此应用（事件取消）


**当发生触摸事件时，会发生以下的流程:**

1. 在触摸发生时，生成`PointerEvent`

2. 框架对应用程序执行命中测试，以此来确定`PointerEvent`对应屏幕位置上存在哪些widget

3. 触摸事件（包括指针的起始事件以及该指针的后续事件）会被分发到由命中测试发现的最内部的widget

4. 从包含`PointerEvent`的一组widget中，这些事件会在widget树中向上冒泡，这些事件会从最内部的widget被分发到到widget根的路径上的所有小部件，没有机制取消或停止冒泡过程


**要直接从widget层监听指针事件，可以使用`Listener` widget,但是通常情况下建议使用手势**

# 3. 手势

手势表示可以识别多个单独的指针事件（甚至可能是多个单独的指针事件）的语义动作（例如，轻敲，拖动和缩放）

完整的一个手势可以分派多个事件，对应于手势的生命周期（例如，拖动开始，拖动更新和拖动结束）：

**从widget层监听手势，可以可以使用`GestureDetector`**

- 如果使用的是`Material`包中的widget,大部分都对触摸进行了响应。例如`IconButton`和`FlatButton`响应了按压，`ListView`响应了华东事件触发滚动

- `InkWell`可以为widget提供涟漪效果，即点击后出现涟漪(需要提供onTap参数)

## 3.1 Tap

- `onTapDown`: 指针已经在特定位置与屏幕接触

- `onTapUp`: 指针停止在特定位置与屏幕接触

- `onTap`: tap事件触发

- `onTapCancel`: 先前指针触发的onTapDown不会在触发tap事件

## 3.2 双击

- `onDoubleTap`: 用户快速连续两次在同一位置轻敲屏幕.
长按

- `onLongPress`: 指针在相同位置长时间保持与屏幕接触

## 3.3 垂直拖动

- `onVerticalDragStart`: 指针已经与屏幕接触并可能开始垂直移动

- `onVerticalDragUpdate`: 指针与屏幕接触并已沿垂直方向移动.

- `onVerticalDragEnd`: 先前与屏幕接触并垂直移动的指针不再与屏幕接触，并且在停止接触屏幕时以特定速度移动

## 3.4 水平拖动

- `onHorizontalDragStart`: 指针已经接触到屏幕并可能开始水平移动

- `onHorizontalDragUpdate`: 指针与屏幕接触并已沿水平方向移动

- `onHorizontalDragEnd`: 先前与屏幕接触并水平移动的指针不再与屏幕接触，并在停止接触屏幕时以特定速度移动


# 4. 手势消歧

在屏幕上的指定位置上，可能同时会存在多个手势检测器。所有这些手势检测器都会有指针事件流经过，并且会去尝试识别特定手势。 `GestureDetector`会根据非空的回调来决定尝试识别哪些手势

当屏幕上的指定位置对应有多个手势检测器时，框架通过让每个检测器加入一个“手势竞争场”来确定用户想要的手势。“手势竞争场”使用以下规则确定哪个手势胜出

- 在任何时候，检测者都可以宣布自身失败并离开“手势竞争场”。如果在“竞争场”中只剩下一个检测器，那么该检测器就是赢家

- 在任何时候，检测者都可以宣布自身胜利，这会导致胜利，并且所有剩下的检测器都会失败


例如，在消除水平拖动和垂直拖动的歧义时，两个检测器在接收到指针向下事件时进入“手势竞争场”，然后检测器观察指针移动事件。 

- 如果用户将指针水平移动超过一定数量的逻辑像素，则水平检测器将声明胜利，并且手势将被解释为水平拖拽。 类似地，如果用户垂直移动超过一定数量的逻辑像素，垂直检测器将宣布胜利。

- 当只有水平（或垂直）拖动识别器时，“手势竞争场”是有益的。在这种情况下，“手势竞争场”将只有一个检测器，并且水平拖动将被立即识别，这意味着水平移动的第一个像素可以被视为拖动，用户不需要等待进一步的手势消歧





