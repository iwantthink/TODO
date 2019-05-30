# Flutter杂项

[Flutter实战](https://github.com/flutterchina/flutter-in-action/blob/master/docs/SUMMARY.md)


# 1. 路由管理

**路由(Route)在移动开发中通常指页面（Page)**,在Android中通常指一个`Activity`,在iOS中指一个`ViewController`

路由管理，就是管理页面之间如何跳转，通常也可被称为导航管理


## 1.1 MaterialPageRoute

MaterialPageRoute继承自PageRoute类，PageRoute类是一个抽象类，表示占有整个屏幕空间的一个模态路由页面，它还定义了路由构建及切换时过渡动画的相关接口及属性

MaterialPageRoute 是Material组件库的一个Widget，它可以针对不同平台，实现与平台页面切换动画风格一致的路由切换动画

- 如果需要自定义路由切换的动画，那么可以继承PageRoute来实现


## 1.2 Navigator

Navigator是一个路由管理的widget，它通过一个栈来管理一个路由widget集合

- 通常当前屏幕显示的页面就是栈顶的路由

Navigator提供了一系列方法来管理路由栈，这里介绍其最常用的两个方法：

- `Future push(BuildContext context, Route route)`

	将给定的路由入栈（即打开新的页面），返回值是一个`Future`对象，用以接收新路由出栈（即关闭）时的返回数据

- `bool pop(BuildContext context, [ result ])`

	将栈顶路由出栈，result为页面关闭时返回给上一个页面的数据
	

**Navigator类中第一个参数为context的静态方法都对应一个Navigator的实例方法**

- 比如`Navigator.push(BuildContext context, Route route)` 等价于`Navigator.of(context).push(Route route) route) `

- 命名路由相关的方法也是一样


## 1.3 命名路由

所谓命名路由（`Named Route`）即给路由起一个名字，然后可以通过路由名字直接打开新的路由

### 1.3.1 路由表

要想使用命名路由，必须先提供并注册一个路由表（routing table），这样应用程序才知道哪个名称与哪个路由Widget对应

路由表的定义如下：

	Map<String, WidgetBuilder> routes；

- key 为路由的名称，是个字符串；value是个builder回调函数，用于生成相应的路由Widget。

- **在通过路由名称入栈新路由时，应用会根据路由名称在路由表中找到对应的`WidgetBuilder`回调函数，然后调用该回调函数生成路由widget并返回**


### 1.3.2 注册路由表

要使用路由表，必须先注册，例如在`MaterialApp`中使用`routes`属性注册路由表

	MaterialApp(
	  title: 'Flutter Demo',
	  theme: new ThemeData(
	    primarySwatch: Colors.blue,
	  ),
	  //注册路由表
	  routes:{
	   "new_page":(context)=>NewRoute(),
	  } ,
	  home: new MyHomePage(title: 'Flutter Demo Home Page'),
	);

### 1.3.3 通过路由名称打开新的路由

要通过路由名称来打开新路由，可以使用`Navigator.pushNamed()`函数：

	Future pushNamed(BuildContext context, String routeName,{Object arguments})

### 1.3.4 命名路由参数

Flutter最初不支持命名路由传递参数，后续版本才支持

1. 注册一个路由表

2. 在路由中通过`RouteSetting`对象获取路由参数

		var args = ModalRoute.of(context).settings.arguments;

3. 在跳转路由时传递参数

		Navigator.of(context).pushNamed("new_page", arguments: "hi");



# 2. 包管理

Flutter默认使用配置文件`pubspec.yaml`（位于项目根目录）来管理第三方依赖包

- YAML语法简单易解析的文件格式，常用于配置文件

示例:
	
	name: flutter_in_action
	description: First Flutter application.
	
	version: 1.0.0+1
	
	dependencies:
	  flutter:
	    sdk: flutter
	  cupertino_icons: ^0.1.2
	
	dev_dependencies:
	  flutter_test:
	    sdk: flutter
	    
	flutter:
	  uses-material-design: true

- name：应用或包名称

- description: 应用或包的描述、简介

- version：应用或包的版本号

- dependencies：应用或包依赖的其它包或插件

- dev_dependencies：开发环境依赖的工具包（而不是flutter应用本身依赖的包）

- flutter：flutter相关的配置选项

## 2.1 Pub仓库

[Pub](https://pub.dartlang.org/)是Google官方的Dart Packages仓库，可以在上面查找需要的包和插件，也可以向pub发布包和插件

### 2.1.1 示例(引入三方包)
添加一个`english_words`的开源软件包

1. 在pub上找到该包，确认其最新版本以及是否支持Flutter

2. 添加到`dependencies`中

		dependencies:
		  flutter:
		    sdk: flutter
		
		  cupertino_icons: ^0.1.0
		  # 新添加的依赖
		  english_words: ^3.1.3

3. 点击ide的`Pacakges get`按钮或执行命令`flutter packages get`

4. 在使用的地方导入包，然后进行使用

		import 'package:english_words/english_words.dart';


## 2.2 其他依赖方式

Flutter还支持依赖本地包和git仓库

### 2.2.1 依赖本地包
如果存在一个本地包，包名为`pkg1`,那么通过以下语法:

	dependencies:
		pkg1:
			path: ../../code/pkg1

- 路径可以是相对的，也可以是绝对的

### 2.2.2 依赖Git
如果存在一个包存在Git仓库中，并位于仓库根目录中,那么通过以下语法:

	dependencies:
	  pkg1:
	    git:
	      url: git://github.com/flutter/packages.git
	      path: packages/pkg1

- 如果不是位于仓库根目录中，那么可以使用path参数指定相对位置

## 2.3 更多内容

[参考Dart的依赖介绍](https://www.dartlang.org/tools/pub/dependencies)


# 3. 资源管理

Flutter应用程序可以包含代码和 assets（有时称为资源）

- **assets是会打包到程序安装包中的，可在运行时访问**

- 常见类型的assets包括静态数据（例如JSON文件）、配置文件、图标和图片（JPEG，WebP，GIF，动画WebP / GIF，PNG，BMP和WBMP）等

## 3.1 指定assets

Flutter使用`pubspec.yaml`文件来管理应用所需的资源

	flutter:
	  assets:
	    - assets/my_icon.png
	    - assets/background.png

- 每个asset都通过相对于`pubspec.yaml`文件所在位置的显式路径进行标识。assets的声明顺序是无关紧要的。asset的实际目录可以是任意文件夹（在本示例中是assets）

**在构建期间，Flutter将assets放置到称为`asset bundle `的特殊存档中，应用程序可以在运行时读取它们（但不能修改）**


## 3.2 Asset变体(variatn)

Flutter构建过程支持variant的概念：不同版本的asset可能会显示在不同的上下文中

- **在`pubspec.yaml`的assets部分中指定asset路径，在构建过程中，会在相邻子目录中查找具有相同名称的任何文件。这些文件随后会与指定的asset一起被包含在asset bundle中**

示例：

应用目录中存在以下文件

	…/pubspec.yaml
	…/graphics/my_icon.png
	…/graphics/background.png
	…/graphics/dark/background.png
	…etc.

`pubspec.yaml`文件中只需要包含:

	flutter:
	  assets:
	    - graphics/background.png

- `/graphics/dark/background.png`和`/graphics/background.png`都会被包含在`asset bundle`中,前者被认为是一种variant，后者被认为是`_main asset_`(主资源)

在选择匹配当前设备分辨率的图片时，Flutter会使用到asset变体


## 3.3 加载assets

应用可以通过`AssetsBundle`对象访问asset.有两种方法允许从`Asset bundle`中加载字符串或图片(二进制)文件

### 3.3.1 加载文本

1. 通过`rootBundle`对象加载：

	每个Flutter应用程序都有一个rootBundle对象， 通过它可以轻松访问主资源包，直接使用`package:flutter/services.dart`中全局静态的`rootBundle`对象来加载asset即可

2. 通过`DefaultAssetBundle`加载：

	建议使用`DefaultAssetBundle`来获取当前`BuildContext`的`AssetBundle`。 这种方法不是使用应用程序构建的默认asset bundle，而是使父级widget在运行时动态替换的不同的AssetBundle，这对于本地化或测试场景很有用


**通常，可以使用`DefaultAssetBundle.of()`在应用运行时来间接加载asset（例如JSON文件），而在widget上下文之外，或其它AssetBundle句柄不可用时，可以使用rootBundle直接加载这些asset**，例如：

	import 'dart:async' show Future;
	import 'package:flutter/services.dart' show rootBundle;
	
	Future<String> loadAsset() async {
	  return await rootBundle.loadString('assets/config.json');
	}

### 3.3.2 加载图片

**Flutter提供了`AssetImage`类型去加载图片**

	Widget build(BuildContext context) {
	  return new DecoratedBox(
	    decoration: new BoxDecoration(
	      image: new DecorationImage(
	        image: new AssetImage('graphics/background.png'),
	      ),
	    ),
	  );
	}

- `AssetImage`并不是一个widget,而是一个`ImageProvider`,所以需要`DecorationImage `进行包装

- **如果希望直接获得显示图片的widget,可以使用`Image.asset()`等方法**


**Flutter在加载资源时，默认的就会为当前设备加载适合其分辨率的图像。**开发者可以通过使用更加底层的类，如`ImageStream`或`ImageCache`来控制相关加载


### 3.3.3 加载依赖包中的资源图片

**通过给AssetImage提供package参数 ，可以加载依赖包中的图像**

假设应用依赖于一个名为`my_icons`的包，并且它具有如下目录结构:

	…/pubspec.yaml
	…/icons/heart.png
	…/icons/1.5x/heart.png
	…/icons/2.0x/heart.png
	…etc.

那么在当前应用中，可以通过如下方式对其资源图片进行加载

	new AssetImage('icons/heart.png', package: 'my_icons')

	new Image.asset('icons/heart.png', package: 'my_icons')


**包在使用本身的资源时也应该加上package参数来获取**

## 3.4 在assets中声明分辨率相关的图片(适配图片)

`AssetImage`可以将对asset的请求逻辑映射到最接近当前设备像素比例(dpi)的asset。为了使这种映射起作用，必须根据特定的目录结构来保存asset(**`pubspec.yaml`和实际目录都需要进行处理**)：

	…/image.png
	…/Mx/image.png
	…/Nx/image.png
	…etc.

- M和N是数字标识符，其值是设备像素比率，对应于文件夹中包含的图像的分辨率，也就是说，它们指定不同设备像素比例的图片

	[设备像素比介绍](https://www.jianshu.com/p/af6dad66e49a)

- 主资源默认对应于1.0倍的分辨率图片

示例:

	…/my_icon.png
	…/2.0x/my_icon.png
	…/3.0x/my_icon.png

- 在设备像素比率为1.8 的设备上，就会选择`2.0x`目录下的图片

- 如果未在`Image`(widget)上指定渲染图像的宽度和高度，那么`Image`将占用与主资源相同的屏幕空间大小。 也就是说，如果`.../my_icon.png`是72px乘72px，那么`.../3.0x/my_icon.png`应该是216px乘216px; 但如果未指定宽度和高度，它们都将渲染为72像素×72像素（以逻辑像素为单位）

`pubspec.yaml`的asset部分中的每一项都应与实际文件相对应，但主资源项除外。当主资源缺少某个资源时，会按分辨率从低到高的顺序去选择 ，也就是说1x中没有的话会在2x中找，2x中还没有的话就在3x中找


## 3.5 打包包中的assets

如果在`pubspec.yaml`文件中声明了期望的资源，它将会打包到相应的package中。特别是，包本身使用的资源必须在`pubspec.yaml`中指定

包也可以选择在其`lib`文件夹中包含未在其`pubspec.yaml`文件中声明的资源

- 在这种情况下，对于要打包的图片，应用程序必须在`pubspec.yaml`中指定包含哪些图像

例如，一个名为“fancy_backgrounds”的包，可能包含以下文件：

	…/lib/backgrounds/background1.png
	…/lib/backgrounds/background2.png
	…/lib/backgrounds/background3.png

要包含第一张图像，必须在pubspec.yaml的assets部分中声明它：

	flutter:
	  assets:
	    - packages/fancy_backgrounds/backgrounds/background1.png

lib/是隐含的，所以它不应该包含在资产路径中。


## 3.6 特定平台的assets

上面所讲的都是Flutter应用中的资源使用，这些资源只有在Flutter框架运行之后才能使用

如果要给应用设置App图标或者添加启动图，需要根据平台来决定assets

### 3.6.1 设置App图标

Android

- 在Flutter的Android项目中找到`.../android/app/src/main/res`目录，其中有不同分辨率的`mipmap-xxx`文件夹，默认使用`ic_launcher.png`作为图标，对其进行替换即可

iOS

- 在Flutter的iOS项目中找到`.../ios/Runner/Assets.xcassets/AppIcon.appiconset`目录，该目录中已经包含占位符图片



### 3.6.2 启动页

Flutter框架加载时，Flutter会使用本地平台机制绘制启动页

- 此启动页将持续到Flutter渲染应用程序的第一帧时

	这意味着如果不在应用程序的`main()`方法中调用`runApp()`函数 （或者更具体地说，如果不调用`window.render`去响应`window.onDrawFrame()`）的话， 启动屏幕将永远持续显示


Android

- 目录`.../android/app/src/main/res/drawable`下有一个`launch_background.xml`文件，其决定了启动界面

iOS

- 目录`.../ios/Runner/Assets.xcassets/LaunchImage.imageset`下的`Contents.json`文件,其决定了启动页面(修改该文件内容，即可实现更换启动页)


# 4. 调试Flutter应用

在运行应用程序前，可以运行`flutter analyze`测试代码。这个工具（它是dartanalyzer工具的一个包装）将分析代码并发现可能的错误

- Dart分析器大量使用了代码中的类型注释来帮助追踪问题


[调试Flutter应用 - 重点内容](https://github.com/flutterchina/flutter-in-action/blob/master/docs/chapter2/flutter_app_debug.md)

# 5. Dart线程模型

## 5.1 Dart单线程模型

在Java和OC中，如果程序发生异常且没有被捕获，那么程序将会终止，但在Dart或JavaScript中则不会，这和它们的运行机制有关系，Java和OC都是多线程模型的编程语言，任意一个线程触发异常且没被捕获时，整个进程就退出了。但Dart和JavaScript都是单线程模型，运行机制很相似(但有区别)


下面是Dart官方提供的dart大致运行原理图：

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3jiktcng7j20d30e2dg8.jpg)

Dart 在单线程中是以消息循环机制来运行的，其中包含两个任务队列:

- **微任务队列**(`microtask queue`)

- **事件队列**(`event queue`).并且微任务队列的执行优先级高于事件队列

**Dart线程运行过程：**

- 如上图中所示，入口函数`main()`执行完后，消息循环机制便启动了。首先会按照先进先出的顺序逐个执行微任务队列中的任务，当所有微任务队列执行完后便开始执行事件队列中的任务，事件任务执行完毕后再去执行微任务，如此循环往复

**在Dart中，所有的外部事件任务都在事件队列中，如IO、计时器、点击、以及绘制事件等，而微任务通常来源于Dart内部，并且微任务非常少**.

- 之所以如此，是因为微任务队列优先级高，如果微任务太多，执行时间总和就越久，事件队列任务的延迟也就越久，对于GUI应用来说最直观的表现就是比较卡，所以必须得保证微任务队列不会太长

- Flutter提供了`Future.microtask(…)`方法向微任务队列插入一个任务

- **在事件循环中，当某个任务发生异常并没有被捕获时，程序并不会退出，而是导致当前任务的后续代码不再被执行，也就是说一个任务中的异常是不会影响其它任务执行的**

# 6 Flutter异常捕获与日志收集

**Dart中可以通过`try/catch/finally`来捕获代码块异常**

- Dart不强制要求捕获异常

## 6.1 Flutter框架异常捕获

Flutter 框架默认为很多关键的方法进行了异常捕获，例如当出现布局越界或不符合规范的情况时,Flutter会自动弹出一个错误界面,这是因为Flutter已经在执行`build()`方法时对异常进行了捕获，然后进行了处理

## 6.2 自定义处理Flutter框架异常捕获流程

异常捕获的源码:

	@override
	void performRebuild() {
	 ...
	  try {
	    //执行build方法  
	    built = build();
	  } catch (e, stack) {
	    // 有异常时则弹出错误提示  
	    built = ErrorWidget.builder(_debugReportException('building $this', e, stack));
	  } 
	  ...
	}      

- 发生异常时，Flutter的默认处理方式就是弹出一个`ErrorWidget`

**这里直接给出结论，如果要自定义逻辑来处理Flutter捕获的那些异常，可以提供一个自定义的错误处理回调即可**

	void main() {
	  FlutterError.onError = (FlutterErrorDetails details) {
	    reportError(details);
	  };
	 ...
	}

## 6.2 自定义处理其他异常捕获

在Flutter中，还有一些Flutter没有捕获的异常，如调用空对象方法异常、Future中的异常

- **在Dart中，异常可以分两类：同步异常和异步异常，同步异常可以通过try/catch捕获，而异步异常则比较麻烦，如下面的代码是捕获不了Future的异常的：**
		
		try{
		    Future.delayed(Duration(seconds: 1)).then((e) => Future.error("xxx"));
		}catch (e){
		    print(e)
		}

D**art中有一个`runZoned(...)`方法，可以给执行对象指定一个Zone**

- Zone表示一个代码执行的环境范围，可以将Zone类比为一个代码执行沙箱，不同沙箱的之间是隔离的，沙箱可以捕获、拦截或修改一些代码行为，如Zone中可以捕获日志输出、Timer创建、微任务调度的行为，同时Zone也可以捕获所有未处理的异常

- `runZoned(...)`方法定义：

		R runZoned<R>(R body(), {
		    Map zoneValues, 
		    ZoneSpecification zoneSpecification,
		    Function onError,
		}) 

	- `Map zoneValues`: Zone 的私有数据，可以通过实例`zone[key]`获取，可以理解为每个“沙盒”的私有数据。

	- `ZoneSpecification  zoneSpecification`：Zone的一些配置，可以自定义一些代码行为，比如拦截日志输出行为等

		举个例子，拦截应用中所有调用`print()`输出日志的行为
	
			main() {
			  runZoned(() => runApp(MyApp()), zoneSpecification: new ZoneSpecification(
			      print: (Zone self, ZoneDelegate parent, Zone zone, String line) {
			        parent.print(zone, "Intercepted: $line");
			      }),
			  );
			}

		通过这种方式，可以在应用中记录日志，等到应用触发未捕获的异常时，将异常信息和日志统一上传


	- `Function onError`:Zone中未捕获异常处理回调，如果开发者提供了onError回调或者通过`ZoneSpecification.handleUncaughtError`指定了错误处理回调，那么这个zone将会变成一个error-zone，该error-zone中发生未捕获异常(无论同步还是异步)时都会调用开发者提供的回调

			runZoned(() {
			    runApp(MyApp());
			}, onError: (Object obj, StackTrace stack) {
			    var details=makeDetails(obj,stack);
			    reportError(details);
			});
			
		- `error-zone`内部发生的错误是不会跨越当前error-zone的边界的，如果想跨越error-zone边界去捕获异常，可以通过共同的“源”zone来捕获

				var future = new Future.value(499);
				runZoned(() {
					var future2 = future.then((_) { throw "error in first error-zone"; });
					runZoned(() {
						var future3 = future2.catchError((e) { print("Never reached!"); });
					}, onError: (e) { print("unused error handler"); });
				}, onError: (e) { print("catches error of first error-zone."); });
				
				
## 6.3 总结

异常捕获+日志收集+代码上传的逻辑如下所示:

	void collectLog(String line){
	    ... //收集日志
	}
	void reportErrorAndLog(FlutterErrorDetails details){
	    ... //上报错误和日志逻辑
	}
	
	FlutterErrorDetails makeDetails(Object obj, StackTrace stack){
	    ...// 构建错误信息
	}
	
	void main() {
	  FlutterError.onError = (FlutterErrorDetails details) {
	    reportErrorAndLog(details);
	  };
	  runZoned(
	    () => runApp(MyApp()),
	    zoneSpecification: ZoneSpecification(
	      print: (Zone self, ZoneDelegate parent, Zone zone, String line) {
	        collectLog(line); // 收集日志
	      },
	    ),
	    onError: (Object obj, StackTrace stack) {
	      var details = makeDetails(obj, stack);
	      reportErrorAndLog(details);
	    },
	  );
	}	