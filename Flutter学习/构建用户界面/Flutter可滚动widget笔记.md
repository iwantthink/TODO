# 可滚动widget

[可滚动widget](https://github.com/flutterchina/flutter-in-action/blob/master/docs/chapter6/index.md)

# 1. 简介

当内容超过显示视口(`ViewPort`)时，如果没有特殊处理，Flutter则会提示`Overflow`错误。为此，Flutter提供了多种可滚动widget（`Scrollable`组件）用于显示列表和长布局

- `The viewport is the user's visible area of a web page`

**可滚动Widget都直接或间接包含一个`Scrollable` 组件，因此可滚动的widget包括一些共同的属性**

	Scrollable({
	  ...
	  this.axisDirection = AxisDirection.down,
	  this.controller,
	  this.physics,
	  @required this.viewportBuilder, //后面介绍
	})

- `axisDirection`：滚动方向

- `physics`：**此属性接受一个`ScrollPhysics`对象，它决定可滚动Widget如何响应用户操作**

	比如用户滑动完抬起手指后，继续执行动画；或者滑动到边界时，如何显示。**默认情况下，Flutter会根据具体平台分别使用不同的`ScrollPhysics`对象**，应用不同的显示效果，如当滑动到边界时，继续拖动的话，在iOS上会出现弹性效果，而在Android上会出现微光效果
	
	**如果想在所有平台下使用同一种效果，可以显式指定，Flutter SDK中包含了两个`ScrollPhysics`的子类可以直接使用**：

	- `ClampingScrollPhysics`：Android下微光效果

	- `BouncingScrollPhysics`：iOS下弹性效果

- `controller`：此属性接受一个`ScrollController`对象。**`ScrollController`的主要作用是控制滚动位置和监听滚动事件**

	默认情况下，widget树中会有一个默认的`PrimaryScrollController`，如果子树中的可滚动widget没有显式的指定controller并且primary属性值为true时（默认就为true），可滚动widget会使用这个默认的`PrimaryScrollController`，这种机制带来的好处是父widget可以控制子树中可滚动widget的滚动，例如，Scaffold使用这种机制在iOS中实现了"回到顶部"的手势
	
# 2. Scrollbar

**`Scrollbar`是一个Material风格的滚动指示器（滚动条）**

- 如果要给可滚动widget添加滚动条，只需将`Scrollbar`作为可滚动widget的父widget即可

**`Scrollbar`和`CupertinoScrollbar`都是通过`ScrollController`来监听滚动事件来确定滚动条位置**

## 2.1 CupertinoScrollbar

`CupertinoScrollbar`是iOS风格的滚动条，如果使用的是`Scrollbar`，那么在iOS平台它会自动切换为`CupertinoScrollbar`

# 3. ViewPort视口

**在Flutter中，术语`ViewPort`（视口），如无特别说明，则是指一个Widget的实际显示区域**

- 例如，一个ListView的显示区域高度是800像素，虽然其列表项总高度可能远远超过800像素，但是其`ViewPort`仍然是800像素

# 4. 主轴和纵轴

**在可滚动widget的坐标描述中，通常将滚动方向称为主轴，非滚动方向称为纵轴**

- 由于可滚动widget的默认方向一般都是沿垂直方向，所以默认情况下主轴就是指垂直方向，水平方向同理

# 5. SingleChildScrollView

`SingleChildScrollView`类似于Android中的`ScrollView`，它只能接收一个子Widget

	SingleChildScrollView({
	  this.scrollDirection = Axis.vertical, //滚动方向，默认是垂直方向
	  this.reverse = false, 
	  this.padding, 
	  bool primary, 
	  this.physics, // 决定响应操作
	  this.controller, //控制滚动位置和监听滚动事件
	  this.child,
	})

- `reverse`：该属性API文档解释是：**是否按照阅读方向相反的方向滑动**

	例如当`scrollDirection`值为`Axis.horizontal`，如果阅读方向是从左到右(取决于语言环境，阿拉伯语就是从右到左)，reverse为true时，那么滑动方向就是从右往左(即从整个内容的最右边开始向左滑动)。**其实此属性本质上是决定可滚动widget的初始滚动位置是在“头”还是“尾”，取false时，初始滚动位置在“头”，反之则在“尾”**

- `primary`：**指是否使用widget树中默认的`PrimaryScrollController`**

	当滑动方向为垂直方向（scrollDirection值为`Axis.vertical`）并且controller没有指定时，primary默认为true

示例:

	String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	Scrollbar(
      child: SingleChildScrollView(
        padding: EdgeInsets.all(16.0),
        child: Center(
          child: Column( 
            //动态创建一个List<Widget>  
            children: str.split("") 
                //每一个字母都用一个Text显示,字体为原来的两倍
                .map((c) => Text(c, textScaleFactor: 2.0,)) 
                .toList(),
          ),
        ),
      ),
    );

**通常`SingleChildScrollView`只应该在内容适合屏幕的情况下使用，它无法使用基于`Sliver`的延迟实例化，因此如果预计`ViewPort`包含超出屏幕尺寸的内容，那么`SingleChildScrollView`的开销会非常大**


# 6. ListView

ListView是最常用的可滚动widget，它可以沿一个方向线性排布所有子widget

	ListView({
	  ...  
	  //可滚动widget公共参数
	  Axis scrollDirection = Axis.vertical,
	  bool reverse = false,
	  ScrollController controller,
	  bool primary,
	  ScrollPhysics physics,
	  EdgeInsetsGeometry padding,
	  
	  //ListView拥有多个构造函数
	  //ListView各个构造函数的共同参数  
	  double itemExtent,
	  bool shrinkWrap = false,
	  bool addAutomaticKeepAlives = true,
	  bool addRepaintBoundaries = true,
	  double cacheExtent,
	    
	  //子widget列表
	  List<Widget> children = const <Widget>[],
	})

- `itemExtent`：

	该参数如果不为null，则会强制children的"长度"为itemExtent的值；**这里的"长度"是指滚动方向上子widget的长度，即如果滚动方向是垂直方向，则itemExtent代表子widget的高度，如果滚动方向为水平方向，则itemExtent代表子widget的长度**
	
	在ListView中，**指定itemExtent比让子widget自己决定自身长度会更高效**，这是因为指定itemExtent后，滚动系统可以提前知道列表的长度，而不是总是动态去计算，尤其是在滚动位置频繁变化时（滚动系统需要频繁去计算列表高度)

	- 注意里面的内容并不会自适应！
	
- `shrinkWrap`：

	**该属性表示是否根据子widget的总长度来设置ListView的长度，默认值为false**
	
	**默认情况下，ListView的会在滚动方向尽可能多的占用空间**
	
	- 当ListView在一个无边界(滚动方向上)的容器中时，shrinkWrap必须为true

- `addAutomaticKeepAlives`：

	**该属性表示是否将列表项（子widget）包裹在`AutomaticKeepAlive`组件中**
	
	典型地，在一个懒加载列表中，如果将列表项包裹在`AutomaticKeepAlive`中，在该列表项滑出`ViewPort`时该列表项不会被GC，它会使用`KeepAliveNotification`来保存其状态。如果列表项自己维护其KeepAlive状态，那么此参数必须置为false

- `addRepaintBoundaries`：

	**该属性表示是否将列表项（子widget）包裹在`RepaintBoundary`组件中**
	
	当可滚动widget滚动时，**将列表项包裹在`RepaintBoundary`中可以避免列表项重绘**，但是当列表项重绘的开销非常小（如一个颜色块，或者一个较短的文本）时，不添加`RepaintBoundary`反而会更高效
	
	和`addAutomaticKeepAlive`一样，如果列表项自己维护其KeepAlive状态，那么此参数必须置为false


## 6.1 默认构造函数

默认构造函数有一个children参数，它接受一个Widget列表（List）

- **这种方式适合只有少量的子widget的情况，因为这种方式需要将所有children都提前创建好**（这需要做大量工作），而不是等到子widget真正显示的时候再创建

- **实际上通过此方式创建的`ListView`和使用`SingleChildScrollView+Column`的方式没有本质的区别**

**注意**：

- **可滚动widget通过一个List来作为其children属性时，只适用于子widget较少的情况，这是一个通用规律，并非ListView自己的特性，像GridView也是如此**



## 6.2 ListView.builder

**`ListView.builder`适合列表项比较多（或者无限）的情况，因为该构造函数设置了只有当子Widget真正显示的时候才会被创建**

	ListView.builder({
	  // ListView公共参数已省略  
	  ...
	  @required IndexedWidgetBuilder itemBuilder,
	  int itemCount,
	  ...
	})

- `itemBuilder`：列表项的构建器，类型为`IndexedWidgetBuilder`，返回值为一个widget。**当列表滚动到具体的index位置时，会调用该构建器构建列表项**

- `itemCount`：**列表项的数量**，如果为null，则为无限列表

## 6.3 ListView.separated

**`ListView.separated`可以生成列表项之间的分割器**，它除了比`ListView.builder`多了一个`separatorBuilder`参数，该参数是一个分割器生成器

    //下划线widget预定义以供复用。  
    Widget divider1=Divider(color: Colors.blue,);
    Widget divider2=Divider(color: Colors.green);
	ListView.separated(
        itemCount: 100,
        //列表项构造器
        itemBuilder: (BuildContext context, int index) {
          return ListTile(title: Text("$index"));
        },
        //分割器构造器
        separatorBuilder: (BuildContext context, int index) {
          return index%2==0?divider1:divider2;
        },
    )


## 6.4 示例 无限加载列表

	class InfiniteListView extends StatefulWidget {
	  @override
	  _InfiniteListViewState createState() => new _InfiniteListViewState();
	}
	
	class _InfiniteListViewState extends State<InfiniteListView> {
	  static const loadingTag = "##loading##"; //表尾标记
	  var _words = <String>[loadingTag];
	
	  @override
	  void initState() {
	    super.initState();
	    _retrieveData();
	  }
	
	  @override
	  Widget build(BuildContext context) {
	    return ListView.separated(
	      itemCount: _words.length,
	      itemBuilder: (context, index) {
	        //如果到了表尾
	        if (_words[index] == loadingTag) {
	          //不足100条，继续获取数据
	          if (_words.length - 1 < 100) {
	            //获取数据
	            _retrieveData();
	            //加载时显示loading
	            return Container(
	              padding: const EdgeInsets.all(16.0),
	              alignment: Alignment.center,
	              child: SizedBox(
	                  width: 24.0,
	                  height: 24.0,
	                  child: CircularProgressIndicator(strokeWidth: 2.0)
	              ),
	            );
	          } else {
	            //已经加载了100条数据，不再获取数据。
	            return Container(
	                alignment: Alignment.center,
	                padding: EdgeInsets.all(16.0),
	                child: Text("没有更多了", style: TextStyle(color: Colors.grey),)
	            );
	          }
	        }
	        //显示单词列表项
	        return ListTile(title: Text(_words[index]));
	      },
	      separatorBuilder: (context, index) => Divider(height: .0),
	    );
	  }
	
	  void _retrieveData() {
	    Future.delayed(Duration(seconds: 2)).then((e) {
	      _words.insertAll(_words.length - 1,
	          //每次生成20个单词
	          generateWordPairs().take(20).map((e) => e.asPascalCase).toList()
	      );
	      setState(() {
	        //重新构建列表
	      });
	    });
	  }
	}

- 通过`itemBuilder`获取widget，会判断当前加载的widget是否位于队尾，如果已经达到队尾，那么会判断是否需要新增加数据(是否达到上限),如果没有达到上限会去继续拉取数据，如果达到了上限，那么就返回一个表示没有更多内容了的widget,否则会去获取数据，并返回一个加载中的widget，因为加载数据是耗时操作，所以会先显示出那个 加载中的widget，然后再数据加载完之后，调用视图重新构建，这时重新走了`itemBuilder`方法

## 6.5 添加固定表头

考虑使用`Column`里面包含 表示Title的widget 和 `ListView`

1. 但是直接使用，会抛出异常

		Error caught by rendering library, thrown during performResize()。
		Vertical viewport was given unbounded height ...


2. 为了解决这个问题，可以给`ListView`添加一个`ConstrainedBox`或`SizedBox`

3. 但是当手机屏幕高度要大于400时，底部会有一些空白，那为了实现列表铺满除过表头以外的屏幕空间,直观的方法是动态计算，用屏幕高度减去状态栏、导航栏、表头的高度即为剩余屏幕高度，代码如下：

		SizedBox(
		  //Material设计规范中状态栏、导航栏、ListTile高度分别为24、56、56 
		  height: MediaQuery.of(context).size.height-24-56-56,
		  child: ListView.builder(itemBuilder: (BuildContext context, int index) {
		    return ListTile(title: Text("$index"));
		  }),
		)

4. 但是这种方法并不优雅，如果页面布局发生变化，如表头布局调整导致表头高度改变，那么剩余空间的高度就得重新计算.

	为了实现自动拉升ListView以填充屏幕剩余空间.可以使用`Flex+Expanded`布局，因为Column是继承自Flex的，所以可以使用Expanded自动拉伸组件大小的Widget
	
		Column(children: <Widget>[
		    ListTile(title:Text("商品列表")),
		    Expanded(
		      child: ListView.builder(itemBuilder: (BuildContext context, int index) {
		        return ListTile(title: Text("$index"));
		      }),
		    ),
		  ])


## 6.6 总结
不同的构造函数对应了不同的列表项生成模型，**如果需要自定义列表项生成模型，可以通过`ListView.custom`来自定义**，它需要实现一个`SliverChildDelegate`用来给ListView生成列表项widget，更多详情请参考API文档

# 7. GridView

**GridView可以构建一个二维网格列表**

	GridView({
	  Axis scrollDirection = Axis.vertical,
	  bool reverse = false,
	  ScrollController controller,
	  bool primary,
	  ScrollPhysics physics,
	  bool shrinkWrap = false,
	  EdgeInsetsGeometry padding,
	  @required SliverGridDelegate gridDelegate, //控制子widget layout的委托
	  bool addAutomaticKeepAlives = true,
	  bool addRepaintBoundaries = true,
	  double cacheExtent,
	  List<Widget> children = const <Widget>[],
	})

- `gridDelegate`:

	类型是`SliverGridDelegate`，**它的作用是控制GridView子widget如何排列(layout)**
	
	`SliverGridDelegate`是一个抽象类，定义了`GridView` Layout相关接口，子类需要通过实现它们来实现具体的布局算法
	
	Flutter中提供了两个`SliverGridDelegate`的子类`SliverGridDelegateWithFixedCrossAxisCount`和`SliverGridDelegateWithMaxCrossAxisExtent`	

## 7.1 SliverGridDelegateWithFixedCrossAxisCount

**该类实现了一个横轴为固定数量子元素的layout算法(纵轴子元素数量可以自定义)**

	SliverGridDelegateWithFixedCrossAxisCount({
	  @required double crossAxisCount, 
	  double mainAxisSpacing = 0.0,
	  double crossAxisSpacing = 0.0,
	  double childAspectRatio = 1.0,
	})

- `crossAxisCount`：

	**横轴子元素的数量**。此属性值确定后子元素在横轴的长度就确定了,即`ViewPort横轴长度/crossAxisCount`

- `mainAxisSpacing`：

	**主轴方向的间距**

- `crossAxisSpacing`：

	**横轴方向子元素的间距**

- `childAspectRatio`：

	**子元素在横轴长度和主轴长度的比例**。由于`crossAxisCount`指定后子元素横轴长度就确定了，然后通过此参数值就可以确定子元素在主轴的长度


**子元素的大小是通过`crossAxisCount`和`childAspectRatio`两个参数共同决定的**。注意，这里的子元素大小指的是子widget的最大显示空间，注意确保子widget的实际大小不要超出子元素的空间

### 7.1.1 GridView.count

**`GridView.count`构造函数内部使用了`SliverGridDelegateWithFixedCrossAxisCount`，通过它可以快速的创建横轴固定数量子元素的GridView**


## 7.2 SliverGridDelegateWithMaxCrossAxisExtent

**该类实现了一个横轴子元素为固定最大长度的layout算法**

	SliverGridDelegateWithMaxCrossAxisExtent({
	  double maxCrossAxisExtent,
	  double mainAxisSpacing = 0.0,
	  double crossAxisSpacing = 0.0,
	  double childAspectRatio = 1.0,
	})

- `maxCrossAxisExtent`:

	**指定子元素在横轴上的最大长度，之所以是“最大”长度**，是因为横轴方向每个子元素的长度仍然是等分的
	
	举个例子，如果`ViewPort`的横轴长度是450，那么当`maxCrossAxisExtent`的值在区间[450/4，450/3)内的话，子元素最终实际长度都为112.5
	
- **`childAspectRatio`所指的子元素横轴和主轴的长度比为最终的长度比**

### 7.2.1 GridView.extent

**`GridView.extent`构造函数内部使用了`SliverGridDelegateWithMaxCrossAxisExtent`，通过它可以快速的创建纵轴子元素为固定最大长度的的GridView**

## 7.3 GridView.builder

**上面介绍的GridView构造函数都需要一个Widget数组作为其子元素，这些方式都会提前将所有子widget都构建好，所以只适用于子Widget数量比较少时**

- 当子widget比较多时，可以通过`GridView.builder`来动态创建子Widget

`GridView.builder` 必须指定的参数有两个：

	GridView.builder(
	 ...
	 @required SliverGridDelegate gridDelegate, 
	 @required IndexedWidgetBuilder itemBuilder,
	)

- `itemBuilder` :作为子widget构建器

示例:

	GridView.builder(
	        gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
	            crossAxisCount: 3, //每行三列
	            childAspectRatio: 1.0 //显示区域宽高相等
	        ),
	        itemCount: _icons.length,
	        itemBuilder: (context, index) {
	          //如果显示到最后一个并且Icon总数小于200时继续获取数据
	          if (index == _icons.length - 1 && _icons.length < 200) {
	            _retrieveIcons();
	          }
	          return Icon(_icons[index]);
	        }
	    )

- 在itemBuilder中，如果显示到最后一个时，判断是否需要继续获取数据，然后返回一个Icon

## 7.4 子元素大小不一的情况

Flutter的GridView默认子元素显示空间是相等的，但在实际开发中，可能会遇到子元素大小不等的情况

- Pub上有一个包“`flutter_staggered_grid_view`” ，它实现了一个交错GridView的布局模型，可以很轻松的实现这种布局


# 8. CustomScrollView

**`CustomScrollView`是可以使用`sliver`来自定义滚动模型（效果）的widget**

- 它可以包含多种滚动模型，举个例子，假设有一个页面，顶部需要一个`GridView`，底部需要一个`ListView`，而要求整个页面的滑动效果是统一的，即它们看起来是一个整体，**如果使用`GridView+ListView`来实现的话，就不能保证一致的滑动效果，因为它们的滚动效果是分离的，所以这时就需要一个"胶水"，把这些彼此独立的可滚动widget（Sliver）"粘"起来，而`CustomScrollView`的功能就相当于“胶水”**

## 8.1 Sliver

`Sliver`有细片、小片之意，在Flutter中，**`Sliver`通常指具有特定滚动效果的可滚动块**

- 可滚动widget，如ListView、GridView等都有对应的Sliver实现如`SliverList`、`SliverGrid`等

	**对于大多数Sliver来说，它们和可滚动Widget最主要的区别是`Sliver`不会包含`Scrollable` 组件，也就是说Sliver本身不包含滚动交互模型** ，正因如此，CustomScrollView才可以将多个Sliver"粘"在一起，这些Sliver共用`CustomScrollView`的Scrollable，最终实现统一的滑动效果
	

- `Sliver`系列Widget比较多，只需记住它的特点，需要时再去查看文档即可

	“大多数“Sliver都和可滚动Widget对应，但是还是有一些如`SliverPadding`、`SliverAppBar`等是和可滚动Widget无关的，它们主要是为了结合CustomScrollView一起使用，这是因为CustomScrollView的子widget必须都是Sliver


[示例可以查看](https://github.com/flutterchina/flutter-in-action/blob/master/docs/chapter6/custom_scrollview.md)

# 9. 滚动控制

## 9.1 ScrollController

构造函数：

	ScrollController({
	  double initialScrollOffset = 0.0, //初始滚动位置
	  this.keepScrollOffset = true,//是否保存滚动位置
	  ...
	})

- `offset`：**可滚动Widget当前滚动的位置**

- `jumpTo(double offset)`,`animateTo(double offset,...)`：这两个方法用于**跳转到指定的位置**，它们不同之处在于，后者在跳转时会执行一个动画，而前者不会


### 9.1.1 滚动监听

`ScrollController`间接继承自`Listenable`，可以根据`ScrollController`来监听滚动事件

	controller.addListener(()=>print(controller.offset))

- 通过`ScrollController`可以获得当前滚动位置等等信息

## 9.2 滚动位置恢复

**`PageStorage`是一个用于保存页面(路由)相关数据的Widget**，它并不会影响子树的UI外观

- 其实，`PageStorage`是一个功能型Widget，它拥有一个存储桶（`bucket`），子树中的Widget可以通过指定不同的`PageStorageKey`来存储各自的数据或状态


**每次滚动结束，`Scrollable` 组件都会将滚动位置`offset`存储到`PageStorage`中，当`Scrollable` 组件重新创建时再恢复**

- **有一个BUG,`ScrollController`在创建时，必须手动指定`keepScrollOffset`和`initialScrollOffset`的值，否则无法生效...**

- 如果`ScrollController.keepScrollOffset`为false，则滚动位置将不会被存储，`Scrollable`组件重新创建时会使用`ScrollController.initialScrollOffset`

	`ScrollController.keepScrollOffset`为true时，`Scrollable` 组件在第一次创建时，会滚动到`initialScrollOffset`处，因为这时还没有存储过滚动位置。在接下来的滚动中就会存储、恢复滚动位置，而initialScrollOffset会被忽略

当一个路由中包含多个`Scrollable`组件时，如果进行一些跳转或切换操作后，滚动位置将不能正确恢复，这时可以通过显式指定`PageStorageKey`来分别跟踪不同`Scrollable`组件的位置，如：
	
	ListView(key: PageStorageKey(1), ... );
	...
	ListView(key: PageStorageKey(2), ... );

- 不同的`PageStorageKey`，需要不同的值，这样才可以区分为不同`Scrollable` 组件保存的滚动位置。



**实际上一个路由中包含多个`Scrollable` 组件时，如果要分别跟踪它们的滚动位置，并非一定就得给他们分别提供PageStorageKey**

- 这是因为`Scrollable`本身是一个`StatefulWidget`，它的状态中也会保存当前滚动位置，所以，只要`Scrollable` 组件本身没有被从树上detach掉，那么其State就不会销毁(dispose)，滚动位置就不会丢失。只有当Widget发生结构变化，导致`Scrollable` 组件的`State`销毁或重新构建时才会丢失状态，这种情况就需要显式指定`PageStorageKey`

	通过`PageStorage`来存储滚动位置，一个典型的场景是在使用TabBarView时，在Tab发生切换时，Tab页中的`Scrollable` 组件的`State`就会销毁，这时如果想恢复滚动位置就需要指定`PageStorageKey`

## 9.3 ScrollPosition

**一个`ScrollController`可以同时被多个`Scrollable` 组件使用，`ScrollController`会为每一个`Scrollable` 组件创建一个`ScrollPosition`对象**，这些`ScrollPosition`保存在`ScrollController`的positions属性中（List）

- **`ScrollPosition`是真正保存滑动位置信息的对象，offset只是一个便捷属性**

		double get offset => position.pixels;



**一个`ScrollController`虽然可以对应多个`Scrollable` 组件，但是有一些操作，如读取滚动位置offset，则需要一对一，但是仍然可以在一对多的情况下，通过其它方法读取滚动位置**

- 举个例子，假设一个`ScrollController`同时被两个`Scrollable` 组件使用，那么通过如下方式分别读取他们的滚动位置：

		controller.positions.elementAt(0).pixels
		controller.positions.elementAt(1).pixels


通过`controller.positions.length`可以确定controller被几个`Scrollable` 组件使用


### 9.3.1 方法
`ScrollPosition`有两个常用方法：`animateTo()` 和 `jumpTo()`，它们是真正来控制跳转滚动位置的方法

- **`ScrollController`的这两个同名方法，内部最终都会调用`ScrollPosition`的同名方法**

## 9.4 ScrollController控制原理

	ScrollPosition createScrollPosition(
	    ScrollPhysics physics,
	    ScrollContext context,
	    ScrollPosition oldPosition);
	void attach(ScrollPosition position) ;
	void detach(ScrollPosition position) ;

1. 当`ScrollController`和`Scrollable`组件关联时，`Scrollable` 组件首先会调用`ScrollController`的`createScrollPosition()`方法来创建一个`ScrollPosition`来存储滚动位置信息，

2. 接着,`Scrollable` 组件会调用`attach()`方法，将创建的`ScrollPosition`添加到`ScrollController`的`positions`属性中，这一步称为“注册位置”，只有注册后`animateTo()` 和 `jumpTo()`才可以被调用。

3. 当`Scrollable` 组件被销毁时，会调用`ScrollController`的`detach()`方法，将其`ScrollPosition`对象从`ScrollController`的`positions`属性中移除，这一步称为“注销位置”，注销后`animateTo()` 和 `jumpTo() `将不能再被调用。

- 需要注意的是，**`ScrollController`的`animateTo() `和 `jumpTo()`内部会调用所有`ScrollPosition`的`animateTo()` 和 `jumpTo()`，以实现所有和该`ScrollController`关联的`Scrollable`组件都滚动到指定的位置**

# 10. 滚动监听

**Flutter Widget树中子Widget可以通过发送通知（`Notification`）与父(包括祖先)Widget通信**。父Widget可以通过`NotificationListener` 组件来监听自己关注的通知，这种通信方式类似于Web开发中浏览器的事件冒泡，在Flutter中沿用“冒泡”这个术语

- `Scrollable`组件在滚动时会发送`ScrollNotification`类型的通知，`ScrollBar`正是通过监听滚动通知来实现的

**通过`NotificationListener`监听滚动事件和通过`ScrollController`有两个主要的不同**：

1. 通过`NotificationListener`可以在从`Scrollable` 组件到Widget树根之间任意位置都能监听,而`ScrollController`只能和具体的`Scrollable`组件关联后才可以

2. 收到滚动事件后获得的信息不同,`NotificationListener`在收到滚动事件时，通知中会携带当前滚动位置和`ViewPort`的一些信息，而`ScrollController`只能获取当前滚动位置。

## 10.1 NotificationListener

`NotificationListener`是一个Widget，模板参数T是想监听的通知类型，如果省略，则所有类型通知都会被监听，如果指定特定类型，则只有该类型的通知会被监听

`NotificationListener`需要一个`onNotification`回调函数，用于实现监听处理逻辑，该回调可以返回一个布尔值，代表是否阻止该事件继续向上冒泡，如果为true时，则冒泡终止，事件停止向上传播，如果不返回或者返回值为false 时，则冒泡继续

例如一个泛型为`ScrollNotification`的`NotificationListener`

- 在接收到滚动事件时，参数类型为`ScrollNotification`，它包括一个`metrics`属性，它的类型是`ScrollMetrics`，该属性包含当前ViewPort及滚动位置等信息

	- `pixels`：当前滚动位置

	- `maxScrollExtent`：最大可滚动长度

	- `extentBefore`：滑出ViewPort顶部的长度；相当于顶部滑出屏幕上方的列表长度

	- `extentInside`：ViewPort内部长度；相当于屏幕显示的列表部分的长度
	
	- `extentAfter`：列表中未滑入ViewPort部分的长度；相当于列表底部未显示到屏幕范围部分的长度

	- `atEdge`：是否滑到了`Scrollable` 组件的边界（相当于列表顶或底部）
