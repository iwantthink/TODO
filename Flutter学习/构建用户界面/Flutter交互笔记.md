# Flutter交互

[为您的Flutter应用程序添加交互](https://flutterchina.club/tutorials/interactive/)

# 1. Stateful和Stateless Widget

主要内容:


- widget分为有状态的和无状态的

	- 如果用户与widget交互，widget会发生变化，那么它就是有状态的

- widget的状态（state）是一些可以更改的值, 如一个slider滑动条的当前值或checkbox是否被选中

- widget的状态保存在一个State对象中, 它和widget的布局显示分离

- 当widget状态改变时, State 对象调用`setState()`, 告诉框架去重绘widget

无状态widget没有内部状态，例如`Icon`,`IconButton`,`Text`等都是无状态widget，并且它们都是`StatelessWidget`的子类

有状态widget是动态的，用户可以与其进行交互(例如输入表单，移动silder滑块),或者可以随时间改变(例如数据变化导致UI更新)。例如`Checkbox`,`Radio`,`Slider`,`InkWell`,`Form`和`TextField`等都是有状态的widget，并且它们都是`StatefulWidget`

## 1.1 创建Stateful Widget

**重点:**

- 要创建一个自定义有状态widget，需创建两个类：`StatefulWidget`和`State`

- 状态对象包含widget的状态和`build()` 方法

- 当widget的状态改变时，状态对象调用`setState()`，告诉框架重绘widget

主要步骤:

1. 决定哪个对象管理widget的状态

2. 创建`StatefulWidget`子类

3. 创建`State`子类

	自定义`State`类可以用来存储可变信息，可以在widget的生命周期内改变逻辑和内部状态

4. 将Stateful Widget插入widget树中

## 1.2 管理状态
**重点:**

- Flutter中有多种方法可以管理状态

- 选择使用何种管理方法

- 如果不是很清楚时, 建议在父widget中管理状态


**具体由哪个widget(父widget，widget本身) 管理状态取决于实际情况**,以下是几种常见的管理状态的方法

1. widget管理自己的state

2. 父widget管理widget状态

3. 混合管理（父widget和子widget都管理状态）

**决定使用哪种管理状态的方法可以遵循以下原则：**

1. 如果状态依靠用户数据，例如复选框的选中状态、滑块的位置，则该状态最好由父widget管理

2. 如果所讨论的状态是有关界面外观效果的，例如动画，那么状态最好由widget本身来管理.

3. 如果不能够确定，那么首选在父widget中管理状态

### 1.2.1 widget管理自身状态
当前widget的状态只有它自身需要时，那么由它自身来对状态进行管理是最好的。例如在ListView的内容超过渲染框时，需要进行滚动。大部分开发者都不希望对其滚动行为进行管理，因此`ListView`自身管理其滚动偏移量

示例（CallWidget自身对其状态进行修改）

	class _CallState extends State<CallWidget> {
	  var _textState = false;
	
	  @override
	  Widget build(BuildContext context) {
	    return GestureDetector(
	      onTap: () {
	        setState(() {
	          _textState = !_textState;
	        });
	      },
	      child: Container(
	        child: Text(
	          "管理自身状态",
	          style: TextStyle(
	            color: _textState ? Colors.blue : Colors.red,
	            fontSize: 30,
	          ),
	        ),
	      ),
	    );
	  }
	}
	
	class CallWidget extends StatefulWidget {
	  @override
	  State<StatefulWidget> createState() {
	    return _CallState();
	  }
	}


### 1.2.2 父widget管理widget状态
在父widget需要知道子widget的状态时，最好通过父widget对widget的状态进行管理，例如当布局需要知道一个Button的状态从而决定另外一个Text的显示内容时

示例(父widget对子widget状态进行控制)	
	
	class ParentWidget extends StatefulWidget {
	  @override
	  State<StatefulWidget> createState() {
	    return _ParentState();
	  }
	}
	
	class _ParentState extends State<ParentWidget> {
	  var _textState = false;
	
	  void _handStateChanged() {
	    setState(() {
	      _textState = !_textState;
	    });
	  }
	
	  @override
	  Widget build(BuildContext context) {
	    return ChildWidget(
	      handStateChanged: _handStateChanged,
	      textState: _textState,
	    );
	  }
	}
	
	class ChildWidget extends StatelessWidget {
	  var handStateChanged;
	
	  var textState;
	
	  ChildWidget({Key key,@required this.handStateChanged, this.textState = false})
	      : super(key: key);
	
	  @override
	  Widget build(BuildContext context) {
	    return GestureDetector(
	      onTap: handStateChanged,
	      child: Text(
	        "父widget对状态进行管理",
	        style: TextStyle(
	          fontSize: 40,
	          color: textState ? Colors.red : Colors.blue,
	        ),
	      ),
	    );
	  }
	}



### 1.2.3 混合管理

上面俩种情况的结合情况，将需要父类控制的状态交给父类控制，将需要子类自身控制的状态交给子类自身


## 1.3 其他交互式widget

Flutter提供了各种按钮和类似拥有交互的widget，这些内置的widget通常实现了`Material Design`设计要求

- 除此之外，**使用`GestureDetector`可以为任意widget添加交互**

## 1.4 部分预置widget的列表：

### 1.4.1 标准 widgets:

- Form

- FormField

### 1.4.2 Material Components:

- Checkbox

- DropdownButton

- FlatButton

- FloatingActionButton

- IconButton

- Radio

- RaisedButton

- Slider

- Switch

- TextField

