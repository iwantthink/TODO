# Dart异步
[dart:async - asynchronous programming](http://dart.goodev.org/guides/libraries/library-tour#dartasync---asynchronous-programming)

[Future Api](https://api.dartlang.org/stable/2.4.0/dart-async/Future-class.html)

[Stream Api](https://api.flutter.dev/flutter/dart-async/Stream-class.html)

[Dart Language Asynchrony Support: Phase 1](http://dart.goodev.org/articles/language/await-async)

[关于事件循环](https://dart.dev/articles/archive/event-loop)

[深入理解Flutter多线程](https://www.jianshu.com/p/54da18ed1a9e)

[Dart|什么是Stream?](https://juejin.im/post/5baa4b90e51d450e6d00f12e)

# 1. 简介

**异步编程通常使用回调函数，但是 Dart 提供了另外的选择**:`Future`和 `Stream`对象

- `Future`和JavaScript中的`Promise`类似，**代表在将来某个时刻会返回一个结果**

- **`Stream`是一种用来获取一些列数据的方式，例如事件流**

**`Future`,`Stream`以及其他异步操作的类在 `dart:async`库中**

	import 'dart:async';


但是并不是只能使用`Future`或`Stream`来进行异步编程，Dart拥有一些异步功能的关键字，例如`async`和`await`可以用来简化异步编程

# 2. 异步支持

**Dart拥有一些支持异步编程的语言特性,例如`async`和`await`关键字支持异步编程**

- **其主要是搭配`Future`或`Stream`一起使用**

- `async`用来修饰方法，`await`配合表达式使用

## 2.1 关键字vs`Future`和`Stream`的APi

**在直接使用`Future`和`Stream`api 之前，可以考虑先使用`await`+`async`对`Future`或`Stream`进行操作，这比直接使用`Future`和`Stream`api的代码要更加容易理解**

例如下面的代码，使用`Future.then()`函数执行三个异步方法,每个方法执行完后才继续执行后一个方法

	runUsingFuture() {
	  //...
	  findEntrypoint().then((entrypoint) {
	    return runExecutable(entrypoint, args);
	  }).then(flushThenExit);
	}

使用`await`表达式对上述代码进行实现

	runUsingAsyncAwait() async {
	  // findEntrypint()返回一个 Future
	  // await 会等待这个Future执行完毕再往下执行
	  var entrypoint = await findEntrypoint();
	  var exitCode = await runExecutable(entrypoint, args);
	  await flushThenExit(exitCode);
	}

- **在`runUsingAsyncAwait()`这个异步方法中遇到`await`之后，会暂停当前方法继续向下执行，直到当前方法执行完毕，才会在遇到`await`的地方继续开始执行**

	**此外，在遇到`await`之后，调用`runUsingAsyncAwait()`方法的地方会向下执行，而不是等待`runUsingAsyncAwait()`方法执行完毕**

- 使用`await`表达式之后，可以将`Future`中的错误当做异常来处理

			attached() async {
			  super.attached();
			  try {
			    await appObject.start();
			  } catch (e) {
			    //...handle the error...
			  }
			}

**异步方法（带有关键字`async`的方法）会返回`Future`,如果不想让方法返回`Future`，则不要使用`async`关键字**

- 例如可以在自己的同步方法里面调用一个其他的异步方法

## 2.2 声明异步方法(async的使用)

**一个异步方法就是方法体被`async`修饰的方法，即修饰符`async`需要在`=>`之前或方法的花括号之前**

- 异步方法在被执行时，并不是意味着方法体内所有的内容已经是异步执行了，只有在异步方法内遇到`await`后,那么会停止`async`方法内部的执行，从而继续执行异步方法外的代码，当`await`返回后，会继续`await`的位置执行，所以`await`的操作不会影响后面的代码的操作

	如果没有遇到`await`,那么异步方法中的内容还是同步执行的

	如果异步方法中第一个遇到的是`await sleep()`还是会同步执行...并不适用于上述的逻辑

**修饰符只是函数的实现细节，而不是签名的一部分，从调用者的角度来说，调用异步函数和调用普通函数没有区别**

- 出于上述的原因,`async`修饰符对函数的返回类型没有影响，但是它确实改变了实际返回的对象类型

- **异步方法会返回`Future`，如果手动指定方法的返回值类型为非`Future`，编译器会报告静态错误**


`return`语句的操作在异步函数和在常规函数中不同。**`return`会在异步函数调用时返回给调用者一个`Future`，`return`表达式的值会被封装到这个`Future`中去,当`return`语句的表达式值返回时，`Future`就执行完毕了**

- 如果在异步函数中抛出(或重新抛出)异常，则抛出的对象将用于实现一个带有错误的`Future`

- 如果`return`语句的返回类型为T,那么该函数就需要具有返回类型`Future<T>`.否则编译器会报告静态警告(不声明返回类型时，会返回`Future<dynamic>`)

- 如果`return`语句返回的是`Future<T>`,那么方法的返回值类型是`Future<T>`而不是`Future<Future<T>>`
	
- 对于已经完成另外一个`Future`的`Future`,除了等待就没有其他可做的事情了，所以`async`库消除了`Future`层，`type discipline`就是为了识别这种情况而设计的

	> There is not much you can do with a Future that has completed to another Future except wait some more, so layers of Futures are eliminated by the async library. The type discipline is designed to recognize that fact.

- ~~~Dart中的一步函数始终是异步的，这与其他语言中的异步函数不同，在其他语言中，函数可能在某些情况下是完全同步的。**在Dart中，异步函数的每个部分都在被调用的调用返回给调用者后执行**~~~
	
- **如果异步方法没有返回一个有用的值，那么可以指定其返回值为`void`**


示例:
	
	// 使用async
	foo() async => 42;
	// 使用Future可以得到同样的效果
	foo() => new Future(() => 42);

- **`async`修饰符节省了许多样板代码，但是其真正的作用是允许在函数内部使用`await expression`**


## 2.3 使用await表达式

### 2.3.1 为什么使用await表达式?
**`await expression`允许像编写同步代码一样去编写异步代码**

假设有一个变量`myFile`指向一个文件，然后需要复制到一个新的地址

	String newPath = '/some/where/out/there';
	myFile.copy(newPath).path == newPath;

- 以上代码不能成功执行，因为Dart的`i/o`操作是异步的，复制操作会返回一个`Future`,因此不能这样调用`path`属性

为了成功执行，需要为`copy()`方法返回的`Future`设置一个回调，这个回调需要接收一个参数，即`Future`的返回值

	myFile.copy(newPath).then((f) => f.path == newPath);

- 这样编写代码有一些冗长，并且如果逻辑更加复杂，代码也会更复杂

实际上，**上述代码真正想要实现的逻辑就是等待异步代码执行完毕，获取到结果，再恢复执行**，那么`await expression`可以用来实现这样的逻辑

	(await myFile.copy(newPath)).path == newPath;

- 当`await expression`表达式执行时，`myFile.copy()`被调用，得到一个`Future`.然后执行就被暂停了，直到`Future`完成之后才恢复执行

	**`await expression`的值就是其返回的`Future`返回的值**，即复制之后的文件对象，这时就可以获取文件对象的`path`与`newPath`进行比较

### 2.3.2 await的表现形式

通常`await`表达式的形式如下:

	await expression

- **`expression`是一个一元表达式，通常情况下，`expression`是异步计算并且预计会返回一个`Future`**

- **`await `表达式会计算`expression`的值，同时暂停当前方法直到其结果返回，也就是说等待`Future`的执行完毕**

- **`await expression`的执行结果就是`Future`的返回值**

- 暂停之后，执行会在事件循环的下一个周期中恢复,[关于事件循环](https://dart.dev/articles/archive/event-loop)

- **与直接使用返回`Future`的`expression`不同，`await expression`会阻塞，直到需要的对象返回为止,而前者会直接返回一个`Future`并不会阻塞代码的执行**

- **在一个异步方法内可以使用多次 await 表达式**

		var entrypoint = await findEntrypoint();
		var exitCode = await runExecutable(entrypoint, args);
		await flushThenExit(exitCode);


### 2.3.3 await遇到异常时

在使用`await expression`时，如果`Future`以一个错误结束，那`await expression`会在执行恢复时将这个错误抛出(这精简了异步代码处理异常的)

如果抛出异常的是`expression`而不是`Future`,那么抛出的异常会被封装到一个`Future`中并且执行暂停，当恢复执行后该异常会被抛出


- **可以使用`try-catch-finally`来处理`await`可能抛出的异常**


### 2.3.4 expression不返回Future怎么办?
`await expression`在遇到`expression`不返回`Future`时，会一直等待(从技术上将,会将`expression`的结果封装在`Future`中，并在事件循环周期中等待其完成)

- 这是Dart和其他语言中类似特性的区别之一, 在Dart中，`await`会一直等待，这使得行为更加的可预测

	特别是如果循环中有一个无条件的`await`,那么可以确保每一次迭代都会暂停

### 2.3.5 await 使用限制

**`await expression`只能在一个异步方法中被使用(即方法体被`async`修饰的方法)**

- 如果尝试在一个普通方法中使用`await`，会发生一个编译时错误

- 暂停了一个普通函数之后，它就不再是同步的

## 2.4 示例
假设当前正在运行一个需要每帧都更新屏幕的动画

如果不使用`async+await`:

	import "dart:html"
	
	main() {
	  var context = querySelector("canvas").context2D;
	  var running = true;    // Set false to stop.
	
	  tick(time) {
	    context.clearRect(0, 0, 500, 500);
	    context.fillRect(time % 450, 20, 50, 50);
	
	    if (running) window.animationFrame.then(tick);
	  }
	
	  window.animationFrame.then(tick);
	}



## 2.5 在循环中使用异步
**异步的`for`循环形式如下：**

	await for (varOrType identifier in expression) {
	  // Executes each time the stream emits a value.
	}

- **`expression`的值必须是`Stream`类型**

其执行流程如下：

	1. 等待`Stream`返回一个值

	2. 使用`Stream`返回的参数执行for循环代码主体

	3. 重复上述过程直到`Stream`数据返回完毕

**为了停止监听流，可以使用`break`或`continue`语句，它们将跳出循环并取消对`Stream`的订阅**

**`await for`必须在一个异步方法中使用(即被`async`修饰的方法)**

## 2.5 处理`Future`
**`Future`表示一个异步任务，Flutter提供了俩种方式对其进行使用:**

1. 使用`async`方法和`await`表达式

2. 使用`Future`的API

使用了`async`方法和`await`表达式的代码就是异步的，只是这种代码风格看起来很像同步代码，例如下面使用`await`来等待一个异步函数执行结束
	
	Future checkVersion() async {
	  // lookupVersion()方法耗时
	  var version = await lookUpVersion();
	  // Do something with version
	}

- 等待`lookUpVersion()`方法执行结束
	
## 20.5 处理`Stream`

**`Stream`表示一个异步流，Flutter提供了俩种方式对其进行使用:**

1. 使用`async`方法和一个异步`for`循环(`await for`)

	- 使用`await for`时需要确定确实需要等待所有流可能产生的结果，例如不要对`UI`事件回调使用，因为它会发送无穷无尽的事件流

2. 使用`Stream`的APi





# 3. Future

`Future`被用来表示一个潜在的值或者错误，它们会在未来的某个时刻可用。`Future`可以注册一个回调，在值或错误可用时`Future`会通过回调进行处理

- `Future`表示不是立刻完成的计算，普通的方法会返回一个结果，但是异步方法会返回一个`Future`,其最终包含结果,并且`Future`会告诉你什么时候结果准备好了

`Future`可能会以俩种方式结束执行,开发者可以分别对俩种方式设置监听回调

1. 执行成功,获取到了值

2. 执行失败，获取到了一个错误

## 3.1 基础使用

**`Future.then()`方法可以用来设置一个回调，该回调会在`Future`执行结束的时候调用**

	Future<R> then<R>(FutureOr<R> onValue(T value), {Function onError});

例如`HttpRequest.getString() `返回一个`Future`，由于 HTTP 请求是一个耗时操作。使用 `then()`可以在 Future 完成的时候来解析返回的数据：

	HttpRequest.getString(url).then((String result) {
	  print(result);
	});
	// Should handle errors here.

**`Future.catchError()`方法可以用来处理`Future`对象可能抛出的各种异常和错误**

	Future<T> catchError(Function onError, {bool test(Object error)});

示例:

	HttpRequest.getString(url).then((String result) {
	  print(result);
	}).catchError((e) {
	  // Handle or ignore the error.
	});


- **`then().catchError()` 模式就是异步版本的 try-catch**


需要确保是在`Future.then()`返回的`Future`上调用`catchError()`， 而不是在原来的 `Future`对象上调用。否则的话，`Future.catchError()`就只能处理原来`Future`对象抛出的异常而无法处理`then()`代码里面的异常



## 3.2 链接多个异步方法

`Future.then()`函数返回值为`Future`，借助这个可以把多个异步调用给串联起来

- **如果`then()`函数注册的回调函数返回一个`Future`，那么`then()`函数也会返回一个同样的`Future`**

	如果回调函数返回的是一个其他类型的值， 则`Future.then()`会创建一个新的Future对象并完成这个`future`

示例:

	Future result = costlyQuery();
	
	return result.then((value) => expensiveWork())
	             .then((value) => lengthyComputation())
	             .then((value) => print('done!'))
	             .catchError((exception) => print('DOH!'));

- 代码是按照如下顺序执行的：

	1. `costlyQuery()`

	2. `expensiveWork()`

	3. `lengthyComputation()`

## 3.3 等待多个Future

**使用`Future.wait()`这个静态函数可以管理多个`Future`并等待所有Future执行完成(多个`Future`会同时开始执行)**

	Future deleteDone = deleteLotsOfFiles();
	Future copyDone = copyLotsOfFiles();
	Future checksumDone = checksumLotsOfOtherFiles();
	
	Future.wait([deleteDone, copyDone, checksumDone])
	    .then((List values) {
	      print('Done with all the long steps');
	    });

- 这三个方法是同时开始执行的!

# 4. Stream

**`Stream`就是一系列异步事件,它就像是一个异步的`Iterable-where`,此外`Stream`会在事件准备就绪时通知你有一个事件，而不是我们手动去获取下一个事件**

- [Stream-Api](https://api.dartlang.org/stable/2.4.0/dart-async/Stream-class.html)

- 数据序列包含用户生成事件和从文件读取的数据

- **`Stream`Api 和`await for`都可以用来处理`Stream`**

- `Stream`提供了一种响应错误的方法

- `Stream`提供了一种方法去接收事件序列，事件序列可以是数据事件（也称为`Stream`的元素）或者错误事件(即提示有错误发生的通知),此外当流已发出其所有事件时，最后还会发送一个"完成"事件通知监听器`Strean`已经结束


## 4.1 Stream的种类

**`Stream`有俩种类型:**

1. `Single-Subscription Streams`:单订阅流

2. `Broadcast Streams`:广播流

### 4.1.1 Single-Subscription Streams

**一个单订阅流仅允许单个listener在其生命周期内对其进行监听**。它会在拥有一个listener之后才开始生成事件，并且在listener取消订阅之后停止发送事件(即使事件的源仍然在提供事件,比如仍然在调用`Sink.add()`添加事件)
	
- 单订阅流通常用于流式传输大型的连续数据块，其包含一系列作为较大整体一部分的事件，事件会以正确的顺序传递，并且确保不会遗漏任何事件（通常这种流来自文件I/O等）,在开始监听时，数据将会以块的形式提供
	
重复监听同一个单订阅流式不被允许的,即使是第一个订阅者已经取消了
	
- 重复监听可能意味着错误了初始事件，这样会导致后续的部分没有意义

### 4.1.2 Broadcast Streams

**广播流允许任意数量的listener，并且当广播流会在其准备就绪时就发送事件，无论其是否存在listener,因此中途被添加的listener无法收到被添加之前的事件**

广播流用于独立事件或者观察者，一次会处理一个消息，例如浏览器的鼠标事件
	
广播流可以随时被监听，并且可以在被取消之后再次进行监听
	
默认情况下`isBroadcast()`返回false，任何一个广播流在继承自`Stream`之后都要重写`isBroadcast()`并返回true
	
**如果存在多个listener想要监听一个单订阅流，可以使用`asBroadcatStream()`方法基于单订阅流创建一个广播流**

在任何一种流上，流转换的操作(如`where`和`skip`)都返回与方法调用时相同的流类型，除非另有说明

当触发事件时，监听器就会接收到。但是如果在触发时间的同时将listener添加到广播流，则该listener不会接收当前正在触发的事件。

如果listener被取消，那么它将立刻停止接收事件

可以将侦听广播流视为侦听新流，该新流仅包含发生侦听时尚未发出的事件


**当"结束"事件发送时，订阅者会在收到事件之前被取消订阅，当该事件发送时，`Stream`已经没有了订阅者**

- 在这之后向广播流添加新订阅者是可以的，但是这些订阅者将会尽快收到一个新的"结束"事件



## 4.2 获取Stream
获取`Stream`的方法:

1. 构造函数+命名构造函数

	使用普通的构造函数创建新的`Stream`,只需要继承`Stream`并实现其`listen()`方法即可

2. 使用`StreamController`

3. IO Stream

### 4.2.1 Stream的构造函数

`Stream`提供了三个命名构造函数:

1. `Stream.fromFuture`:

	从Future创建新的单订阅流,当Future完成时将触发一个data或者error，然后使用Down事件关闭这个流

		factory Stream.fromFuture(Future<T> future)

2. `Stream.fromFutures`:

	从一组Future创建一个单订阅流，每个future都有自己的data或者error事件，当所有`Future`完成后，流将会关闭。如果`Future`集合为空，流将会立刻关闭

		factory Stream.fromFutures(Iterable<Future<T>> futures)

3. `Stream.fromIterable`:

	创建从一个集合中获取其数据的单订阅流

		factory Stream.fromIterable(Iterable<T> elements)


### 4.2.2 StreamController

`StreamController`是一个控制`Stream`的控制器，它允许在其流上发送数据、错误和已完成事件

`StreamController`可以用来创建一个其他人可以监听的简单流，并将事件推送到该流

- 此外，还可以检查该流是否暂停，是否有订阅者，并且在这些这些状态发生改变时获得回调


## 4.3 接收`Stream`事件

流可以以多种方式创建，但是它们都可以以相同的方式使用

- 当`Stream`中没有更多的事件时，`Stream`就结束了，并且接收事件的代码会收到通知(结束通知)

- 当使用`await for`循环读取事件时，当流完成时循环停止,否则会一直等待

### 4.3.1 `await for`

异步for循环(通常称为`await for`)在流的事件上迭代，就像for循环在Iterable上迭代一样

- 异步for循环(`await for`)在某些情况下可以用来替代Stream API


示例：

	// 接收所有数据事件，计算总和
	import 'dart:async';
	
	Future<int> sumStream(Stream<int> stream) async {
	  var sum = 0;
	  // 当循环体结束时，函数暂停，直到下一个事件或Stream结束
	  await for (var value in stream) {
	    sum += value;
	  }
	  return sum;
	}
	
	Stream<int> countStream(int to) async* {
	  for (int i = 1; i <= to; i++) {
	    print("time = ${DateTime.now().toString()}");
	    yield i;
	  }
	}
	
	main() async {
	  var stream = countStream(10);
	  var sum = await sumStream(stream);
	  print(sum); // 55
	}

- `await for`必须在异步方法中使用

### 4.3.2 Stream.listen()

**`Stream.listen()`方法是最常见的定义`Stream`事件的方法，当有事件发出时，`Stream`会调用回调**

	StreamSubscription<T> listen(void onData(T event),
	      {Function onError, void onDone(), bool cancelOnError});

1. `onData`(必填)：收到数据时触发的回调

2. `onError`：收到Error时触发的回调

3. `onDone`：结束时触发的回调

4. `unsubscribeOnError`：遇到第一个Error时是否取消订阅，默认为false


### 4.3.3 处理异常与执行完成

使用异步 for 循环 (await for) 和使用 Stream API 的 异常处理情况是有 区别的。

1. **如果使用异步for循环，则可以使用`try-catch`来处理异常, `Stream`完成后执行的代码需要位于异步for循环之后**

		readFileAwaitFor() async {
		  var config = new File('config.txt');
		  Stream<List<int>> inputStream = config.openRead();
		
		  var lines = inputStream
		      .transform(UTF8.decoder)
		      .transform(new LineSplitter());
		  try {
		    await for (var line in lines) {
		      print('Got ${line.length} characters from stream');
		    }
		    print('file is now closed');
		  } catch (e) {
		    print(e);
		  }
		}

2. **如果使用`Stream`API，则可以使用`Stream.onError`函数来处理异常。`Stream`完成后执行的代码需要在`Stream.onDone()`函数中执行**

		var config = new File('config.txt');
		Stream<List<int>> inputStream = config.openRead();

		inputStream
		    .transform(UTF8.decoder)
		    .transform(new LineSplitter())
		    .listen((String line) {
		      print('Got ${line.length} characters from stream');
		    }, onDone: () {
		      print('file is now closed');
		    }, onError: (e) {
		      print(e);
		    });

## 4.4 处理`Stream`的方法

除了`drain()`和`pipe()`之外，所有这些函数都对应与Iterable上的类似函数。通过使用带有`await for`循环的异步函数(或者只使用其他方法之一)，可以轻松地编写每个方法

	Future<T> get first;
	Future<bool> get isEmpty;
	Future<T> get last;
	Future<int> get length;
	Future<T> get single;
	Future<bool> any(bool Function(T element) test);
	Future<bool> contains(Object needle);
	Future<E> drain<E>([E futureValue]);
	Future<T> elementAt(int index);
	Future<bool> every(bool Function(T element) test);
	Future<T> firstWhere(bool Function(T element) test, {T Function() orElse});
	Future<S> fold<S>(S initialValue, S Function(S previous, T element) combine);
	Future forEach(void Function(T element) action);
	Future<String> join([String separator = ""]);
	Future<T> lastWhere(bool Function(T element) test, {T Function() orElse});
	Future pipe(StreamConsumer<T> streamConsumer);
	Future<T> reduce(T Function(T previous, T element) combine);
	Future<T> singleWhere(bool Function(T element) test, {T Function() orElse});
	Future<List<T>> toList();
	Future<Set<T>> toSet();


示例：

	Future<bool> contains(Object needle) async {
	  await for (var event in this) {
	    if (event == needle) return true;
	  }
	  return false;
	}
	
	Future forEach(void Function(T element) action) async {
	  await for (var event in this) {
	    action(event);
	  }
	}
	
	Future<List<T>> toList() async {
	  final result = <T>[];
	  await this.forEach(result.add);
	  return result;
	}
	
	Future<String> join([String separator = ""]) async =>
	    (await this.toList()).join(separator);



## 4.4 修改Stream的方法
**以下方法能够基于原始`Stream`返回一个新的`Stream`,并且监听需要重新进行设置**

下面方法对应`Iterable`上都存在类似函数

	Stream<R> cast<R>();
	Stream<S> expand<S>(Iterable<S> Function(T element) convert);
	Stream<S> map<S>(S Function(T event) convert);
	Stream<R> retype<R>();
	Stream<T> skip(int count);
	Stream<T> skipWhile(bool Function(T element) test);
	// 限制事件接收次数,count表示最多能经过take函数的事件次数，超过时Stream会关闭
	Stream<T> take(int count);
	Stream<T> takeWhile(bool Function(T element) test);
	// 指定接收的事件
	Stream<T> where(bool Function(T event) test);


`asyncExpand()`和`asyncMap()`函数类似于`expand()`和`map()`，但是允许它们的函数参数是异步函数。`distinct()`函数在Iterable上不存在.

	Stream<E> asyncExpand<E>(Stream<E> Function(T event) convert);
	Stream<E> asyncMap<E>(FutureOr<E> Function(T event) convert);
	Stream<T> distinct([bool Function(T previous, T next) equals]);


`handleError()`能够处理`await for`无法处理的错误，当`await for`遇到第一个错误时，会终止循环以及`Stream`上的监听，并且无法恢复，而使用`handleError()`删除`Stream`中的错误之后，然后再用于`await for`就不会有这种问题

	Stream<T> handleError(Function onError, {bool test(error)});
	Stream<T> timeout(Duration timeLimit,
	    {void Function(EventSink<T> sink) onTimeout});
	Stream<S> transform<S>(StreamTransformer<T, S> streamTransformer);

### 4.4.1 Stream.transform()

使用`Stream.transform()`函数可以生产另外一个数据类型的Stream 对象：

	import 'dart:convert';
	import 'dart:io';
	
	Future<void> main(List<String> args) async {
	  var file = File(args[0]);
	  var lines = file
	      .openRead()
	      .transform(utf8.decoder)
	      .transform(LineSplitter());
	  await for (var line in lines) {
	    if (!line.startsWith('#')) print(line);
	  }
	}

- 上面的代码使用两种转换器（transformer）。第一个使用 `UTF8.decoder`来把整数类型的数据流转换为字符串类型的数据流。然后使用 `LineSplitter `把字符串类型数据流转换为按行分割的数据流。 

	这些转换器都来至于`dart:convert`库


示例：

	StreamController<int> controller = StreamController<int>();
	
	final transformer = StreamTransformer<int,String>.fromHandlers(
	    handleData:(value, sink){
	  	if(value==100){
	      sink.add("你猜对了");
	    }
		else{ sink.addError('还没猜中，再试一次吧');
	    }
	  });
	  
	controller.stream
	        .transform(transformer)
	        .listen(
	            (data) => print(data),
	            onError:(err) => print(err));
	    
	controller.sink.add(23);

- `StreamTransformer<S,T>`负责接收`Stream`，进行处理之后返回一条新的`Stream`

	- `S`表示原始流的输入类型

	- `T`表示转换后的流的输入类型

- `handleData`接收一个函数，这个函数有俩个参数，第一个是原始`Stream`的值，第二个是新`Stream`的入口

