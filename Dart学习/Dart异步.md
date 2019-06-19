# Dart异步
[dart:async - asynchronous programming](http://dart.goodev.org/guides/libraries/library-tour#dartasync---asynchronous-programming)

[Future Api](https://api.dartlang.org/stable/2.4.0/dart-async/Future-class.html)

[Stream Api](https://api.flutter.dev/flutter/dart-async/Stream-class.html)

[Dart Language Asynchrony Support: Phase 1](http://dart.goodev.org/articles/language/await-async)

[关于事件循环](https://dart.dev/articles/archive/event-loop)

[深入理解Flutter多线程](https://www.jianshu.com/p/54da18ed1a9e)

# 1. 简介

**异步编程通常使用回调函数，但是 Dart 提供了另外的选择**:`Future`和 `Stream`对象

- `Future`和JavaScript中的`Promise`类似，**代表在将来某个时刻会返回一个结果**

- **`Stream`是一种用来获取一些列数据的方式，例如事件流**

- **`Future`,`Stream`以及其他异步操作的类在 `dart:async`库中**

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

在Dart库中随处可见`Future`对象，通常异步函数返回的对象就是一个 `Future`

- 当一个`Future`执行完后，它所代表的值就可以使用了



## 3.2 基础使用






