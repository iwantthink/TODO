# Dart线程模型
[Isolate class - Api](https://api.flutter.dev/flutter/dart-isolate/Isolate-class.html)


[Dart 中的事件循环 译文](https://blog.yuegs.com/2018/08/30/dart-event-loop/)

[Dart 中的事件循环 原文](https://dart.dev/articles/archive/event-loop#event-queue-new-future)

[Flutter Engine线程管理与Dart Isolate机制 - 咸鱼开发团队](https://www.yuque.com/xytech/flutter/kwoww1)

[并发和并行的概念解释!!!](https://laike9m.com/blog/huan-zai-yi-huo-bing-fa-he-bing-xing,61/)

# 1. Dart isolate机制

> An isolated Dart execution context


## 1.1 并发和并行的区别

![并发与并行的区别](https://pic1.zhimg.com/80/4733c03cd0e126b9a500d5912bf9b581_hd.jpg)

- 并发：交替做不同事的能力

- 并行：同时做不同事的能力

## 1.2 isolate定义

`isolate`是Dart对`actor`并发模式的实现

- 运行中的Dart程序由一个或多个`actor`组成，这些`actor`也就是Dart概念里面的`isolate`

**`isolate`是拥有独立内存和运行事件循环的单线程 的实体**,本身的意思是"隔离"，因为`isolate`之间的内存在逻辑上是隔离的

`isolate`中的代码是按顺序执行的，任何Dart程序的并发都是运行多个`isolate`的结果。因为Dart没有共享内存的并发，没有竞争的可能性所以不需要锁，也就不用担心死锁的问题


## 1.3 isolate之间的通信

由于`isolate`之间没有共享内存，所以它们之间的通信唯一方式只能是通过`Port`进行，而且Dart中的消息传递总是异步的


## 1.4 isolate与普通线程的区别

`isolate`与`Thread`相似，但实际上两者有本质的区别

- 操作系统内内的线程之间是可以有共享内存的而isolate没有，这是最为关键的区别

## 1.5 Dart执行模型
当启动任何一个Dart应用(包括Flutter)时，将创建并启动一个新的`isolate`,然后Dart会自动的做如下三件事:

1. 初始化2个FIFO（先进先出）队列（`MicroTask`和`Event`)

2. 并且当该方法执行完成后，执行`main()`方法

3. 启动事件循环

## 1.6 isolate的使用
**使用`Isolate.spawn()`和Flutter的`compute()`函数都可以创建一个`isolate`**


# 2. 基础概念

## 2.1 Event loops/queues

事件循环的作用就是不断的从事件队列中获取条目并处理，直到事件队列中不存在条目

![](https://raw.githubusercontent.com/yuegs/yuegs.github.io/master/images/flutter/dart-event-loop/event-loop.png)

事件队列中的条目可以是 用户输入，文件IO通知，定时器等等

![](https://raw.githubusercontent.com/yuegs/yuegs.github.io/master/images/flutter/dart-event-loop/event-loop-example.png)


# 3. Dart单线程模型

在Java和OC中，如果程序发生异常且没有被捕获，那么程序将会终止，但在Dart或JavaScript中则不会，这和它们的运行机制有关系，Java和OC都是多线程模型的编程语言，任意一个线程触发异常且没被捕获时，整个进程就退出了。但Dart和JavaScript都是单线程模型，运行机制很相似(但有区别)

- **一旦Dart函数执行，它将持续执行直到退出**。也就是说Dart函数在执行期间，无法被其他Dart代码打断


![](https://raw.githubusercontent.com/yuegs/yuegs.github.io/master/images/flutter/dart-event-loop/event-loop-and-main.png)


## 3.1 Dart中的事件循环和事件队列

下面是Dart官方提供的dart大致运行原理图：

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3jiktcng7j20d30e2dg8.jpg)

Dart在单线程中是以消息循环机制来运行的，其中包含一个事件循环和两个任务队列:

- **微任务队列**(`microtask queue`)

	微任务通常来源于Dart内部，并且微任务非常少。其主要用于在当前事件之后，下一个事件之前进行处理

	通过`Future.microtask()`或者顶级函数`scheduleMicrotask()`方法可以添加一个微任务(async库中)

- **事件队列**(`event queue`).并且微任务队列的执行优先级高于事件队列

	**事件队列包含了所有的外部事件以及系统事件，例如I/O、计时器、点击、绘制事件以及Dart isolate中的消息等等**.

	使用`Future`就是往事件队列中添加事件

**Dart线程运行过程：**

- 如上图中所示，入口函数`main()`执行完后，消息循环机制便启动了。首先会按照先进先出的顺序逐个执行微任务队列中的任务，当所有微任务队列执行完后便开始执行事件队列中的任务，事件任务执行完毕后再去执行微任务，如此循环往复

- 之所以如此，是因为微任务队列优先级高，如果微任务太多，执行时间总和就越久，事件队列任务的延迟也就越久，对于GUI应用来说最直观的表现就是比较卡，所以必须得保证微任务队列不会太长

**在事件循环中，当某个任务发生异常并没有被捕获时，程序并不会退出，而是导致当前任务的后续代码不再被执行，也就是说一个任务中的异常是不会影响其它任务执行的**






