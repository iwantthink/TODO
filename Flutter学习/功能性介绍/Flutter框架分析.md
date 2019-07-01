# Flutter框架分析
[Flutter运行机制-从启动到显示](https://github.com/flutterchina/flutter-in-action/blob/master/docs/chapter14/flutter_app_startup.md)

[Flutter框架分析（一）-- 总览和Window](https://juejin.im/post/5c7cd2f4e51d4537b05b0974)

# 简介

下图描述了Flutter App 刷新界面，播放动画，响应触摸事件，将页面显示到屏幕上的流程

![](http://ww1.sinaimg.cn/large/6ab93b35ly1g4enlmetx2j20zk0k3dg6.jpg)

**这是Flutter框架中的一个渲染流水线（`Rendering pipline`）**,渲染流水线由垂直同步信号（`Vsync`）驱动，而`Vsync`信号是由系统提供的，如果Flutter app是运行在Android上的话，那Vsync信号就是熟悉的Android的那个Vsync信号。**当Vsync信号到来以后，Flutter 框架会按照图里的顺序执行一系列动作,即动画（Animate）、构建（Build）、布局（Layout）和绘制（Paint），最终生成一个场景（Scene）之后送往底层，由GPU绘制到屏幕上**

- **动画（Animate）阶段:** 因为动画会随每个Vsync信号的到来而改变状态（State），所以动画阶段是流水线的第一个阶段。

- **构建（Build）阶段:** 在这个阶段那些需要被重新构建的Widget会在此时被重新构建。也就是`StatelessWidget.build()`或者`State.build()`被调用的时候

- **布局（Layout）阶段:** 这时会确定各个显示元素的位置，尺寸。此时是`RenderObject.performLayout()`被调用的时候。

- **绘制（Paint）阶段:** 此时是`RenderObject.paint()`被调用的时候

**Flutter app只有在状态发生变化的时候需要触发渲染流水线**,也就是说当app什么都不做的时候是不需要重新渲染页面的。所以，Vsync信号需要Flutter app去调度。比如当某个页面需要调用`State.setState()`，那么Flutter框架会为这个调用最终会发起一个调度`Vsync`信号的请求给底层,然后底层会在`Vsync`信号到来的时候驱动渲染流水线开始运作，最后把新的页面显示到屏幕上

# 1. Flutter整体架构
Flutter整体架构如下图:

![](http://ww1.sinaimg.cn/large/6ab93b35ly1g4enva93bej20rs0eemx8.jpg)

整个Flutter架构是分为两部分的,上层的框架（`Framework`）部分和底层的引擎（`Engine`）部分

- **框架（Framework）部分使用Dart语言写的**

- **引擎（Engine）部分使用C++实现的,为框架提供支撑，同时也是连接框架和宿主系统（Android/iOS）的桥梁**

	触发渲染流水线的`Vsync`信号来自引擎，渲染完成以后的场景也是要送入引擎来显示，`Vsync`信号的调度也是框架通过引擎来通知系统的
	
	
**渲染流程从框架和引擎交互的角度用一个示意图来表示就是下面这个样子**：

![](http://ww1.sinaimg.cn/large/6ab93b35ly1g4eoa0v426j20d50c2wec.jpg)

1. 框架通知引擎（`scheduleFrame()`）需要调度一帧

2. 在系统的Vsync信号到来以后，引擎会首先会回调框架的`_beginFrame()`函数。此时框架的渲染流水线进入动画（Animate）阶段

3. 在动画（Animate）阶段阶段完成以后。引擎会处理完微任务队列，接着再回调框架的`_drawFrame()`函数。渲染流水线继续按序运行构建、布局和绘制

4. 绘制结束以后，框架调用`render()`将绘制完成的场景送入引擎以显示到屏幕上


# 1. Window类
Flutter中有一个窗口`Window`概念，Flutter中的UI都是容纳在窗口中，窗口是框架的根基，页面的绘制，用户输入事件的处理等等都是需要通过窗口进行管理，此外框架和引擎渲染交互流程也是窗口进行管理

- **`Window`是`Flutter Framework`连接宿主操作系统最基础的接口**

Flutter中的`Window`来自库`dart:ui`,源码在`window.dart`文件中,此外Flutter中的`Window`是一个单例：

	/// The most basic interface to the host operating system's user interface.

	class Window {
	    
	  // 当前设备的DPI，即一个逻辑像素显示多少物理像素，数字越大，显示效果就越精细保真。
	  // DPI是设备屏幕的固件属性，如Nexus 6的屏幕DPI为3.5 
	  double get devicePixelRatio => _devicePixelRatio;
	  
	  // Flutter UI绘制区域的大小
	  Size get physicalSize => _physicalSize;
	
	  // 当前系统默认的语言Locale
	  Locale get locale;
	    
	  // 当前系统字体缩放比例。  
	  double get textScaleFactor => _textScaleFactor;  
	    
	  // 当绘制区域大小改变回调
	  VoidCallback get onMetricsChanged => _onMetricsChanged;  
	  // Locale发生变化回调
	  VoidCallback get onLocaleChanged => _onLocaleChanged;
	  // 系统字体缩放变化回调
	  VoidCallback get onTextScaleFactorChanged => _onTextScaleFactorChanged;
	  // 绘制前回调，一般会受显示器的垂直同步信号VSync驱动，当屏幕刷新时就会被调用
	  FrameCallback get onBeginFrame => _onBeginFrame;
	  // 绘制回调  
	  VoidCallback get onDrawFrame => _onDrawFrame;
	  // 点击或指针事件回调
	  PointerDataPacketCallback get onPointerDataPacket => _onPointerDataPacket;
	  // 调度Frame，该方法执行后，onBeginFrame和onDrawFrame将紧接着会在合适时机被调用，
	  // 此方法会直接调用Flutter engine的Window_scheduleFrame方法
	  void scheduleFrame() native 'Window_scheduleFrame';
	  // 更新应用在GPU上的渲染,此方法会直接调用Flutter engine的Window_render方法
	  void render(Scene scene) native 'Window_render';
	
	  // 发送平台消息
	  void sendPlatformMessage(String name,
	                           ByteData data,
	                           PlatformMessageResponseCallback callback) ;
	  // 平台通道消息处理回调  
	  PlatformMessageCallback get onPlatformMessage => _onPlatformMessage;
	  // 获取启动时初始页面的路由
	  String _defaultRouteName() native 'Window_defaultRouteName';
	  ... //其它属性及回调
	   
	}

- `native`关键字表示这个函数是调用到engine层的(与Java的JNI调用类似)

- **基本上`Window`类就是对engine层对上提供的和用户界面相关的接口进行封装**


# 2. Flutter运行机制(显示->启动)

Flutter的入口是`lib/main.dart`文件中的`main()`函数，这是所有Dart应用程序的入口

	void main() {
	  runApp(MyApp());
	}

Flutter会在`main()`函数中调用`runApp()`函数用来启动整个Flutter应用程序:

	// Inflate the given widget and attach it to the screen
	//加载所传入的widget,并跟当前屏幕进行关联
	void runApp(Widget app) {
	  WidgetsFlutterBinding.ensureInitialized()
	    ..attachRootWidget(app)
	    ..scheduleWarmUpFrame();
	}

- 传入的app组件是应用启动后要展示的第一个widget

## 2.1 WidgetsFlutterBinding

**`WidgetsFlutterBinding`是用来绑定`Framework`和`Flutter engine`的胶水,即连接俩者的桥梁**

- `WidgetsFlutterBinding`继承了`BindingBase`,并且`with`了许多`mixin`类型的`Binding`,这些`Binding`也都继承自`BindingBase`

### 2.1.1 BindingBase

	abstract class BindingBase {
	    BindingBase() {
	        ...
	        initInstances();
	        ...
	    }
	    ...
	    ui.Window get window => ui.window;
	}


**关于抽象类`BindingBase`,有俩个重要的点**:

1. 在其构造函数中会调用函数`initInstances()`。这个函数会由其子类，也就是上面说那些各种`mixin`类实现

2. `BindingBase`有一个`getter`，返回的是`window`。这些`Binding`其实就是对window的封装


### 2.1.2 ensureInitialized()

**`WidgetsFlutterBinding.ensureInitialized()`负责初始化一个WidgetsBinding的全局单例,此外还会依次调用`mixin`类中的`initInstances()`方法**

	class WidgetsFlutterBinding extends BindingBase with GestureBinding, ServicesBinding, SchedulerBinding, PaintingBinding, SemanticsBinding, RendererBinding, WidgetsBinding {
	  static WidgetsBinding ensureInitialized() {
	    if (WidgetsBinding.instance == null)
	      WidgetsFlutterBinding();
	    return WidgetsBinding.instance;
	  }
	}
	
	abstract class BindingBase {
	    BindingBase() {
	        ...
	        initInstances();
	        ...
	    }
	    ...
	    ui.Window get window => ui.window;
	}
	
	mixin WidgetsBinding on BindingBase, SchedulerBinding, GestureBinding, RendererBinding, SemanticsBinding {
		void initInstances() {
			super.initInstances();
			_instance = this;
			.......
		}
	}

- 如果`WidgetsBinding.instance `为空，那么会去调用`WidgetsFlutterBinding`的构造函数,而`WidgetsFlutterBinding`的构造函数会去调用`BindingBase`的构造函数，在`BindingBase`的构造函数中会去调用`initInstances()`方法,因为Binding中都有实现这个`initInstances()`方法，那么会按照`with`添加的逆顺序进行调用`mixin`类中的`initInstances()`方法


### 2.1.2 Binding

**`WidgetsFlutterBinding`混入了许多Binding,查看这些Binding的源码，基本都是监听并处理`Window`对象的一些事件，然后将这些事件按照Framework的模型包装、抽象然后分发。这也验证了上述的结论，`WidgetsFlutterBinding`是粘连Flutter engine与上层Framework的"胶水"**

`GestureBinding`：将`window.onPointerDataPacket`绑定到Framework手势子系统，是Framework事件模型与底层事件的绑定入口，主要包含指针事件的生命周期和手势竞技场

	mixin GestureBinding on BindingBase implements HitTestable, HitTestDispatcher, HitTestTarget {
		  @override
		  void initInstances() {
		    super.initInstances();
		    _instance = this;
		    window.onPointerDataPacket = _handlePointerDataPacket;
	
	}


`ServicesBinding`：将`window.onPlatformMessage`绑定到Framework的平台消息(message channel)处理系统，主要处理原生和Flutter之间的通信
	
	  void initInstances() {
	    super.initInstances();
	    _instance = this;
	    window
	      ..onPlatformMessage = BinaryMessages.handlePlatformMessage;
	    initLicenses();
	  }

`SchedulerBinding`：将`window.onBeginFrame`和`window.onDrawFrame`绑定到Framework绘制调度子系统，主要作用是监听刷新事件

	  void initInstances() {
	    super.initInstances();
	    _instance = this;
	    window.onBeginFrame = _handleBeginFrame;
	    window.onDrawFrame = _handleDrawFrame;
	    SystemChannels.lifecycle.setMessageHandler(_handleLifecycleMessage);
	    readInitialLifecycleStateFromNativeWindow();
	  }

`PaintingBinding`：Hook缓存清除逻辑，以清除图片缓存

	  void initInstances() {
	    super.initInstances();
	    _instance = this;
	    _imageCache = createImageCache();
	    if (shaderWarmUp != null) {
	      shaderWarmUp.execute();
	    }
	  }

`SemanticsBinding`：语义化层与Flutter engine的桥梁，主要是辅助功能的底层支持

	  void initInstances() {
	    super.initInstances();
	    _instance = this;
	    _accessibilityFeatures = window.accessibilityFeatures;
	  }


`RendererBinding`: 设置`window.onMetricsChanged `、`window.onTextScaleFactorChanged `等回调。它是渲染树与Flutter engine的桥梁

	  @override
	  void initInstances() {
	    super.initInstances();
	    _instance = this;
	    _pipelineOwner = PipelineOwner(
	      onNeedVisualUpdate: ensureVisualUpdate,
	      onSemanticsOwnerCreated: _handleSemanticsOwnerCreated,
	      onSemanticsOwnerDisposed: _handleSemanticsOwnerDisposed,
	    );
	    window
	      ..onMetricsChanged = handleMetricsChanged
	      ..onTextScaleFactorChanged = handleTextScaleFactorChanged
	      ..onPlatformBrightnessChanged = handlePlatformBrightnessChanged
	      ..onSemanticsEnabledChanged = _handleSemanticsEnabledChanged
	      ..onSemanticsAction = _handleSemanticsAction;
	    initRenderView();
	    _handleSemanticsEnabledChanged();
	    assert(renderView != null);
	    addPersistentFrameCallback(_handlePersistentFrameCallback);
	    _mouseTracker = _createMouseTracker();
	  }

- **这个`Binding`负责管理渲染流程。主要内容是实例化一个`PipelineOwner`类，这个类负责管理驱动渲染流水线。给`window`设置了一系列回调函数，处理屏幕尺寸变化，亮度变化等。调用`initRenderView()`方法。调用`addPersistentFrameCallback()`函数添加回调函数**

	**`addPersistentFrameCallback()`添加的这个回调非常重要，渲染流水线的主要阶段都会在这个回调里启动**
	
	**`initRenderView()`函数会创建一个`RenderView`**,Flutter框架中存在一个渲染树（`render tree`),这个`RenderView`就是渲染树（`render tree`)的根节点，这一点可以通过打开"Flutter Inspector"看到，在"Render Tree"这个Tab下，最根部的红框里就是这个`RenderView`


	![](http://ww1.sinaimg.cn/large/6ab93b35ly1g4ew4drbaxj20yu0isaaq.jpg)


`WidgetsBinding`：提供了`window.onLocaleChanged`、`onBuildScheduled `等回调。它是Flutter Widget层与engine的桥梁

	  void initInstances() {
	    super.initInstances();
	    _instance = this;
	    buildOwner.onBuildScheduled = _handleBuildScheduled;
	    window.onLocaleChanged = handleLocaleChanged;
	    window.onAccessibilityFeaturesChanged = handleAccessibilityFeaturesChanged;
	    SystemChannels.navigation.setMethodCallHandler(_handleNavigationInvocation);
	    SystemChannels.system.setMessageHandler(_handleSystemMessage);
	  }

- 这个绑定的初始化先给`buildOwner`设置了个`onBuildScheduled`回调. 接着给window设置了两个回调，因为和渲染关系不大，就不细说了。最后设置`SystemChannels.navigation`和`SystemChannels.system`的消息处理函数。这两个回调一个是专门处理路由的，另一个是处理一些系统事件，比如剪贴板，震动反馈，系统音效等等


- **注意俩个"Owner",它们将会Flutter框架里的核心类**

	1. `RendererBinding`里初始化的时候实例化了一个`PipelineOwner`

	2. `BuildOwner`在`WidgetsBinding`里实例化的,它主要负责管理Widget的重建



总体上来讲是把window提供的API分别封装到不同的Binding里。需要重点关注的是`SchedulerBinding`，`RendererBinding`和`WidgetsBinding`。这3个是渲染流水线的重要存在



### 2.1.3 attachRootWidget()

**`WidgetsFlutterBinding.attachRootWidget()`负责将根Widget添加到`RenderView`上**

	// rootWidget是自定义的widget内容
	void attachRootWidget(Widget rootWidget) {
		_renderViewElement = RenderObjectToWidgetAdapter<RenderBox>(
		  container: renderView,
		  debugShortDescription: '[root]',
		  child: rootWidget,
		).attachToRenderTree(buildOwner, renderViewElement);
	}

- **`renderView`是一个`RenderObject`，它是渲染树的根，而`_renderViewElement `是`renderView`对应的`Element`对象**

	1. 渲染绑定(`RendererBinding`)通过`pipelineOwner`间接持有`render tree`的根节点`RenderView`
	
	2. 组件绑定(`WidgetsBinding`)持有`element tree`的根节点`RenderObjectToWidgetElement`

	- 这里的`renderView`在`RendererBinding `中被创建,`RenderObject`需要有对应的`Widget`(`RenderObjectToWidgetAdapter `)和`Element`(`RenderObjectToWidgetElement`)

- `RenderObjectToWidgetAdapter`是`RenderObject`关联到`Element`树的桥梁

	`RenderObjectToWidgetAdapter`代码:

		class RenderObjectToWidgetAdapter<T extends RenderObject> extends RenderObjectWidget {
		  /// Creates a bridge from a [RenderObject] to an [Element] tree.
		  ///
		  /// Used by [WidgetsBinding] to attach the root widget to the [RenderView].
		  RenderObjectToWidgetAdapter({
		    this.child,
		    this.container,
		    this.debugShortDescription
		  }) : super(key: GlobalObjectKey(container));
		
		  @override
		  RenderObjectToWidgetElement<T> createElement() => RenderObjectToWidgetElement<T>(this);
		
		  @override
		  RenderObjectWithChildMixin<T> createRenderObject(BuildContext context) => container;
		  ...
		}
	
	- `createElement()`返回的就是`RenderObjectToWidgetElement`
	
	- `createRenderObject()`返回的`container`就是构造函数传入的`RenderView`

- **作为自定义的内容`MyApp`将会作为一个子widget存在于`RenderObjectToWidgetAdapter`之中**

- `attachToRenderTree()`方法中的逻辑就属于渲染流水线的构建（Build）阶段，这时会根据自己的widget生成`element tree`和`render tree`


### 2.1.4 scheduleWarmUpFrame（）

构建（Build）阶段完成以后，就要进入布局（Layout）阶段和绘制（Paint）阶段了

	  void scheduleWarmUpFrame() {
	    ...
	    Timer.run(() {
	      ...
	      handleBeginFrame(null);
	      ...
	    });
	    Timer.run(() {
	      ...
	      handleDrawFrame();
	      ...
	    });
	  }

这个函数中就调用了两个函数，就是window的中的两个回调函数`onBeginFrame()`和`onDrawFrame()`,这里其实就是在具体执行这两个回调。最后渲染出来首帧场景送入engine显示到屏幕

- 这里使用`Timer.run()`来异步运行两个回调(`Timer`会添加一个`Event`)，是为了在它们被调用之前有机会处理完微任务队列（`microtask queue`）

## 2.2 总结
为了节省等待Vsync信号的时间，在Flutter初始化的时候会直接执行渲染流程以获得第一帧图像(即执行`runApp()`),但是接下来 渲染流水线是由Vsync信号驱动的

Flutter框架初始化过程其实主要的点都在几个绑定（binding）的初始化.Flutter框架其实就是围绕渲染流水线和window在做文章:

1. 3个重要绑定：`SchedulerBinding`，`RendererBinding`和`WidgetsBinding`

2. 2个"owner"：`PipelineOwner`和`BuildOwner`

3. 2颗树的根节点：`render tree`根节点`RenderView`；`element tree`根节点`RenderObjectToWidgetElement`
