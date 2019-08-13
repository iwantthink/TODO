# 去抖动

## 1. debounce 去抖动

定义：

- 调用函数N秒之后，才会去执行函数,**但是如果在这N秒内，又重复调用了该函数，则取消前一次调用，重新以这次调用作为N秒的起始**

### 1.1 debounce示例

    Observable.create<String> { emitter ->
        emitter.onNext("1")
        Thread.sleep(500)
        emitter.onNext("2")
        Thread.sleep(250)
        emitter.onNext("3")
        Thread.sleep(301)
        emitter.onNext("4")
        emitter.onNext("5")
    }
        .debounce(300, TimeUnit.MILLISECONDS)
        .subscribe(
            {
                // 执行在Rx提供的线程池中
                System.out.println("debounce = ${System.currentTimeMillis()}")
                System.out.println("debounce - onNext  it = $it")
            },
            {
                System.out.println("debounce - onError")

            },
            {
                System.out.println("debounce - onComplete")

            })

	I/System.out: debounce = 1563851317663
	I/System.out: debounce - onNext  it = 1
	I/System.out: debounce = 1563851318413
	I/System.out: debounce - onNext  it = 3
	I/System.out: debounce = 1563851318715
	I/System.out: debounce - onNext  it = 5

- 执行流程如下:

	事件1发出，开始计时，等待300ms内是否有新的事件发出(下一个事件是500ms后),没有新的事件，因此执行事件1
	
	然后事件2发出，开始计时，但是250ms后事件3就发出了，因此事件2被取消，**此时以事件3发出的时刻作为起始点，重新开始计时**，由于下一个事件4是301ms后发出，因此执行事件3
	
	事件4和事件5连续发出，然后事件序列结束，则取最后一个事件5执行


## 2. throttle 节流

定义：

- 预先设定一个执行周期，当距离调用函数的时间大于等于执行周期则执行该动作，然后等待下一个函数调用开始并进行下一个新周期

	**需要注意，只有在调用函数事件发生时，才开始计时**

### 2.1 `throttleFirst`示例:

    val ob4 = Observable.intervalRange(
        0,
        10,
        0,
        500,
        TimeUnit.MILLISECONDS
    )

	ob4.throttleFirst(1, TimeUnit.SECONDS)
        .subscribe(
            {
                // 执行在Rx提供的线程池中
                System.out.println("throttleFirst Time = ${System.currentTimeMillis()}")
                System.out.println("throttleFirst - onNext  it = $it")
            },
            {
                System.out.println("throttleFirst - onError")

            },
            {
                System.out.println("throttleFirst - onComplete")

            })
 
	 I/System.out: throttleFirst Time = 1563849434566
	 I/System.out: throttleFirst - onNext  it = 0
	 I/System.out: throttleFirst Time = 1563849436065
	 I/System.out: throttleFirst - onNext  it = 3
	 I/System.out: throttleFirst Time = 1563849437565
	 I/System.out: throttleFirst - onNext  it = 6
	 I/System.out: throttleFirst Time = 1563849439065
	 I/System.out: throttleFirst - onNext  it = 9
	 I/System.out: throttleFirst - onComplete

- 执行流程如下:

	当事件0发出，开始计时，判断耗时是否大于等于执行周期(0s<1s，等待下一事件),事件1发出，判断耗时是否大于等于执行周期(0.5s<1s,等待下一事件),事件2发出，判断耗时是否大于等于执行周期(1s=1s),条件满足，则取该执行周期内事件序列的首个

	然后等待下一个事件的到来，准备进入下一个执行周期。事件3发出，开始计时，判断耗时是否大于等于执行周期(0s<1s)...如此循环直至事件结束

### 2.2 `throttleLast`示例:

    ob4.throttleLast(1, TimeUnit.SECONDS)
        .subscribe(
            {
                // 执行在Rx提供的线程池中
                System.out.println("throttleLast Time = ${System.currentTimeMillis()}")
                System.out.println("throttleLast - onNext  it = $it")
            },
            {
                System.out.println("throttleLast - onError")

            },
            {
                System.out.println("throttleLast - onComplete")

            })

	I/System.out: throttleLast Time = 1563849927246
	I/System.out: throttleLast - onNext  it = 1
	I/System.out: throttleLast Time = 1563849928244
	I/System.out: throttleLast - onNext  it = 3
	I/System.out: throttleLast Time = 1563849929245
	I/System.out: throttleLast - onNext  it = 5
	I/System.out: throttleLast Time = 1563849930244
	I/System.out: throttleLast - onNext  it = 7
	I/System.out: throttleLast - onComplete    
	
- 原理与`throttleFirst`相似，只是它是取执行周期内的事件序列中的最后一个事件	


## 3. sample

定义：

- 获取在指定的时间间隔内发生的事件序列中的最近发生的一个

	**与throttleLast类似**

### 3.1 sample示例

       ob4.sample(1, TimeUnit.SECONDS)
            .subscribe(
                {
                    // 执行在Rx提供的线程池中
                    System.out.println("sample = ${System.currentTimeMillis()}")
                    System.out.println("sample - onNext  it = $it")
                },
                {
                    System.out.println("sample - onError")

                },
                {
                    System.out.println("sample - onComplete")

                })

	I/System.out: sample - onNext  it = 1
	I/System.out: sample - onNext  it = 3
	I/System.out: sample - onNext  it = 5
	I/System.out: sample - onNext  it = 7
	I/System.out: sample - onComplete
	
	
# Flowable

## 1. 定义

**背压是指在异步场景中，被观察者发送事件速度远快于观察者的处理速度的情况下，一种告诉上游的被观察者降低发送速度的策略**

在 `Observable/Observer` 组合的使用中是不支持背压的,`Flowable/Subscriber` 支持背压



# Observable原理

## 1. 创建Observable

	val result: Observable<String> = Observable.
	            create<String> { emitter ->
	        }

    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {

        return RxJavaPlugins.onAssembly(new ObservableCreate<T>(source));
    }

- `ObservableCreate `是`Observable`的一个子类,其提供了`subscribeActual()`方法的具体实现

- `RxJavaPlugins.onAssembly()`暂时不用理会，就当做直接执行了`ObservableCreate`对象

- create方法接收的是`ObservableOnSubscribe`接口的实现类，封装了开发者自定义的逻辑,这个实现类会保存为`source`字段，**实际上所有的`Observable`子类都会有这么一个字段，用来指代上游`Observable`(实际上是`ObservableOnSubscribe`)**

## 2. 调用subscribe

`Observable.create()`方法返回的是`ObservableCreate`,因此这里调用的是`ObservableCreate `的`subscribe`方法

step1:

    public final Disposable subscribe() {
        return subscribe(Functions.emptyConsumer(), Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION, Functions.emptyConsumer());
    }
    
	public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
            Action onComplete, Consumer<? super Disposable> onSubscribe) {

        LambdaObserver<T> ls = new LambdaObserver<T>(onNext, onError, onComplete, onSubscribe);

        subscribe(ls);

        return ls;
    }


- 如果没有传入onNext,onError,onComplete,onSubscribe ,RxJava会构造一个空实现的回调。然后RxJava会将这些回调封装成一个新的组合类`LambdaObserver`

step2:

    public final void subscribe(Observer<? super T> observer) {

        try {
            observer = RxJavaPlugins.onSubscribe(this, observer);

            subscribeActual(observer);
        } catch (NullPointerException e) { // NOPMD
            throw e;
        } catch (Throwable e) {
        }
    }

- `RxJavaPlugins.onSubscribe`暂时忽略，就当做直接返回了`LambdaObserver `即可

- **`subscribeActual`是一个抽象方法，是由`ObservableCreate `提供了具体的实现**

step3:

	// ObservableCreate 类中的方法
    protected void subscribeActual(Observer<? super T> observer) {
        CreateEmitter<T> parent = new CreateEmitter<T>(observer);
        // 触发观察者的onSubscribe()方法
        observer.onSubscribe(parent);

        try {
        	  // 调用开发者编写的逻辑，例如onNext,onError之类的
            source.subscribe(parent);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            parent.onError(ex);
        }
    }

- `CreateEmitter `是对`observer`(即`LambdaObserver`)的封装

- 调用了`LambdaObserver.onSubscribe()`中调用了之前在`create()`时传入的`onSubscribe:Consumer`参数,调用了开发者编写的观察者回调

- `source`即`create()`方法中传入的`ObservableOnSubscribe`回调,这里调用了开发者编写的被观察者回调

# 3. 调用onNext

在`Observable.create()`方法中编写了`ObservableOnSubscribe`的逻辑，其中通过`emitter.onNext("1")`方法发送数据,而这个`emitter`类型就是`CreateEmitter`
	
	// CreateEmitter类
	// t 即传入的数据
	public void onNext(T t) {
	    if (t == null) {
	        onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
	        return;
	    }
	    // 未被处理
	    if (!isDisposed()) {
	    	  // 直接调用观察者编写的onNext方法
	        observer.onNext(t);
	    }
	}

- `CreateEmitter`是在`ObservableCreate.subscribeActual()`方法中被创建

- 代码比较简单，先是非空判断，然后判断是否停止,满足条件后就是去调用`LambdaObserver`(即对开发者编写的回调逻辑的封装)

- **至此,Observable和Observer就被串联起来了!!**

# 4. 调用dispose

在调用`subscribe`之后，会返回一个`Disposable`对象，其实际类型是`LambdaObserver`,该对象可以用来调用`dispose()`方法，用来销毁当前对象

	// LambdaObserver类
    public void dispose() {
        DisposableHelper.dispose(this);
    }

	// DisposableHelper
    public static boolean dispose(AtomicReference<Disposable> field) {
    	 // 默认为空
        Disposable current = field.get();
        Disposable d = DISPOSED;
        if (current != d) {
            current = field.getAndSet(d);
            if (current != d) {
                if (current != null) {
                    current.dispose();
                }
                return true;
            }
        }
        return false;
    }

- `LambdaObserver`继承自`AtomicReference`,在`dispose()`方法中会获取当前的`Disposable`对象(实际上是一个`CreateEmitter`)

	**在`ObservableCreate`类中的`subscribeActual()`方法会调用`LambdaObserver.onSubscribe()`,这里会借助`DisposableHelper.setOnce(this, d)`将`CreateEmitter`对象传给`LambdaObserver`**

	`CreateEmitter`同样继承自`AtomicReference`


- 这里通过`LambdaObserver`获取到的就是`CreateEmitter`,除了自身的值会被设置成`DISPOSED`外，`CreateEmitter`的值也会被设置为`DISPOSED`

# 5. 调用subscribeOn

step1:

    public final Observable<T> subscribeOn(Scheduler scheduler) {

        return RxJavaPlugins.onAssembly(new ObservableSubscribeOn<T>(this, scheduler));
    }

- 这里与`create()`方法的作用相似，就是一个装饰者模式,他会将传入的`Scheduler`和上一个`Observable`进行封装

step2:

**上一步返回了一个新的`Observable`(即`ObservableSubscribeOn`),因此接下来的操作都是基于这个对象进行**

	Observable.subscribe() - > ObservableSubscribeOn.subscribeActual()


step3:

	// ObservableSubscribeOn 类
    public void subscribeActual(final Observer<? super T> observer) {
        final SubscribeOnObserver<T> parent = new SubscribeOnObserver<T>(observer);

		  // 在这里 LambdaObserver 与 SubscribeOnObserver产生关联
		  // 后者的引用会被保存在前者中
        observer.onSubscribe(parent);

 		  // 将SubscribeTask 和SubscribeOnObserver产生关联
 		  // 前者的引用会保存在后者中
        parent.setDisposable(scheduler.scheduleDirect(new SubscribeTask(parent)));
    }

- `observer`类型是`LambdaObserver`,在`subscribeActual()`方法中被封装成`SubscribeOnObserver`


- `observer.onSubscribe(parent)`逻辑之前介绍过，就是将`parent:SubscribeOnObserver`设置给`LambdaObserver`	,此外还会调用开发者设置的`onSubscribe`回调


- `parent:SubscribeOnObserver`首先被`SubscribeTask`封装，传入`Scheduler.scheduleDirect()`方法中处理

	`scheduler:Scheduler`对象是`subscribeOn()`方法传入的，而`Scheduler`本身只是一个抽象类，很多具体的逻辑交给子类来实现

step4:

	// Scheduler类
    public Disposable scheduleDirect(@NonNull Runnable run) {
        return scheduleDirect(run, 0L, TimeUnit.NANOSECONDS);
    }

    public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
        final Worker w = createWorker();

        final Runnable decoratedRun = RxJavaPlugins.onSchedule(run);

        DisposeTask task = new DisposeTask(decoratedRun, w);

        w.schedule(task, delay, unit);

        return task;
    }

- `createWorker()`是一个抽象方法，具体实现是子类，举个例子`IoScheduler`类

		   public Worker createWorker() {
		        return new EventLoopWorker(pool.get());
		    }

- 然后将`SubscribeTask`和`Worker`封装成`DisposeTask`

- 最后通过`Worker.schedule()`去执行task

- **`SubscribeTask`会被返回**

# 多个Observable的情况

## 1.1 创建流程

RxJava中采用了装饰者模式，Observable每进行一次转换，都会创建出一个新的Observable,并保持对前一个`Observable`的引用(使用source字段保存)

    Observable.create<String> { observableEmitter: ObservableEmitter<String> ->
        observableEmitter.onNext("start")
    }.flatMap {
        Observable.just("$it-flatMap")
    }.map {
        "$it-map"
    }.subscribe {
        log("modified data = $it")
    }

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g5x02sqi4bj20vq0rmq70.jpg)

## 1.2 订阅流程

Observable创建完成后,会进行订阅操作,借助source字段,原始的Observer会被各个`Observable`层层封装，最终传递给`ObservableOnSubscribe`(即开发者提供的逻辑,进行onNext等操作)

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g5x17bocbsj20i10iuq5o.jpg)

- 注意:仅在`ObserverCreate`的`subscribeActual()`方法中会调用`Observer.onSubscribe()`

	MergeObserver.onSubscribe()->原始Observer.onSubscribe()
	
- 订阅过程可能会经过N个Observable,但是最后一个肯定是`Observable.create()`创建的`ObservableCreate`

## 1.3 发射流程

[参考文章](https://www.jianshu.com/p/a73619cd8d33)

订阅完成之后,通过各个Observable对原始Observer的层层封装,最终会被封装成一个`CreateEmitter`，并且传递到`SubscribeObserver`的`subscribe()`方法，进行`onNext`等操作

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g5x2o9jbzaj20o00pygoq.jpg)

## 1.4 总结

创建时，Observable一层层封装，并保持前一个的引用

- 举个例子: `A( B( ObservableCreate()))`

订阅时，通过各个`Observable`对上一层`Observable`的引用，依次对传入的原始`Observer`进行封装同时还会保留对上一个`Observer`的引用的保存，会最先使用最外层的Observable

- 举个例子: A 会最先处理原始`Observer`

	`CreateEmitter(B-Observer(A-Observer(原始Observer)))`

发射时,通过各个Observer对上一层`Observer`的引用,依次调用`onNext()`方法

- 举个例子: `CreateEmitter.onNext -> B-Observer.onNext ->A-Observer.onNext -> 原始Observer.onNext`

# Observable线程问题

    Observable.create<String> { observableEmitter: ObservableEmitter<String> ->
        observableEmitter.onNext("Thread = ${Thread.currentThread().name}")
    }
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(Schedulers.io())
        .subscribe {
            log("Observable = $it")
            log("Observer = ${Thread.currentThread().name}")
        }

# 1. 创建过程

1. `Observable.create()`方法接收一个`ObservableOnSubscribe`类型的对象,并保存在`source`字段中(其包含数据发射逻辑)

	创建返回一个**`ObservableCreate`对象**(继承自`Observable`),包含了`source`对象

2. `ObservableCreate.subscribeOn()`方法接收一个`Scheduler`类型的对象(表示被订阅者执行在的线程信息)

	创建并返回一个**`ObservableSubscribeOn`对象**(继承自`AbstractObservableWithUpstream`),封装了当前`Observable`对象(即`ObservableCreate`对象)以及`Scheduler`信息

3. `ObservableSubscribeOn.observeOn()`方法接收一个`Scheduler`类型的对象(表示订阅者执行在的线程信息)

	创建并返回一个**`ObservableObserveOn`对象**(继承自`AbstractObservableWithUpstream`),封装了了当前`Observable`对象(即`ObservableSubscribeOn`)以及`Scheduler`信息

# 2. 订阅过程


1. `Observable.subscribe()`方法有几个参数重载(`Observer`或者是几个`Consumer`,`Action`的组合)，注意`Consumer`和`Action`的组合 会封装成一个`LambdaObserver`并返回，但是传入参数如果是`Observer`则不会返回

	此外，`subscribe()`方法中还会调用`subscribeActual()`这个抽象方法，方便`Observable`的子类去扩展逻辑

	**根据上述的流程，这里的`Observable`实际类型就是`ObservableObserveOn`**

2. `ObservableObserveOn.subscribeActual()`方法接收一个原始的`Observer`对象(包含消息处理逻辑，可能是一个`Observer`/`LambdaObserver`),**下面以`LambdaObserver`为例分析**

	    protected void subscribeActual(Observer<? super T> observer) {
	    	  // 运行在当前线程
	        if (scheduler instanceof TrampolineScheduler) {
	            source.subscribe(observer);
	        } else {
	            Scheduler.Worker w = scheduler.createWorker();
	
	            source.subscribe(new ObserveOnObserver<T>(observer, w, delayError, bufferSize));
	        }
	    }

	- 	参数t的是`LambdaObserver`对象,`source`是在创建过程中(`ObservableSubscribeOn.observeOn`),构建`ObservableObserveOn`对象时

	- 先判断线程信息，除了`TrampolineScheduler`之外，会创建一个`Worker`,然后将原始Observer，Worker等封装到`ObserveOnObserver`中,并将这个`ObserveOnObserver`传递给上一个`Observable`

3. 	`ObservableSubscribeOn.subscribeActual()`方法接收`ObserveOnObserver`,并将其再次封装为`SubscribeOnObserver `

	    public void subscribeActual(final Observer<? super T> observer) {
	        final SubscribeOnObserver<T> parent = new SubscribeOnObserver<T>(observer);
	
	        observer.onSubscribe(parent);
	
	        parent.setDisposable(scheduler.scheduleDirect(new SubscribeTask(parent)));
	    }

	1. 将`SubscribeOnObserver `封装到`SubscribeTask`中(其`run()`方法会调用`ObservableSubscribeOn`的source去执行`subscribe()`方法)

	2. 调用创建`ObservableSubscribeOn`时的`Scheduler`去执行这个Task,例如这里的`Scheduler`就是`HandlerScheduler`

		    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
					.........
		        ScheduledRunnable scheduled = new ScheduledRunnable(handler, run);
		        Message message = Message.obtain(handler, scheduled);
		        if (async) {
		            message.setAsynchronous(true);
		        }
		        handler.sendMessageDelayed(message, unit.toMillis(delay));
		        return scheduled;
		    }

		- 这里非常简单，通过运行在主线程的Handler,进行线程的切换,然后执行上一个Observable的subscribe()方法(即ObservableCreate)

		- 注意，这里返回的是一个`ScheduledRunnable`,其将前面的`SubscribeTask`进行了封装,**实际上`ScheduledRunnable`内部是调用了`SubscribeTask`**,也就是说`ObservableCreate`的`subscribe()`方法，在这里就已经切换到了`ObservableSubscribeOn`指定的`Scheduler`中执行

			**记住，这个`subscribeOn()`仅针对ObservableOnSubscribe生效！！**

4. 最终`ObservableCreate.subscribe()`方法被调用,
	
	    protected void subscribeActual(Observer<? super T> observer) {
	        CreateEmitter<T> parent = new CreateEmitter<T>(observer);
	        observer.onSubscribe(parent);
	
	        try {
	            source.subscribe(parent);
	        } catch (Throwable ex) {
	            Exceptions.throwIfFatal(ex);
	            parent.onError(ex);
	        }
	    }
    
    - 这里的`source`就是`ObservableOnSubscribe`

5. `ObservableOnSubscribe.subscribe()`就是开发者自己编写的被订阅者的逻辑所在，也就是

	注意：调用链中SubscribeOn()之前的Observable.subscribe()方法都执行在其指定的Scheduler中
    
    
# 3. 执行过程
1. `ObservableOnSubscribe`是开发者编写事件发射逻辑

		{ emitter: ObservableEmitter<String> ->
		    emitter.onNext("1")
		}

	- `emitter`实际类型是`CreateEmitter`

2. `CreateEmitter`类的`onNext()`方法对数据t进行了非空检测，然后继续转交给`SubscribeOnObserver`

        public void onNext(T t) {
            if (t == null) {
                onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                return;
            }
            if (!isDisposed()) {
                observer.onNext(t);
            }
        }

3. `SubscribeOnObserver`的`onNext()`没有做额外的操作，直接将数据转交给`MapObserver`的`onNext()`

        public void onNext(T t) {
            downstream.onNext(t);
        }

4. `MapObserver`的`onNext()`中判断了是否还有更多的onXXX(onError,onComplete调用后即没有,done = true),然后调用了`LambdaObserver`的`onNext()`

        public void onNext(T t) {
            if (done) {
                return;
            }

            if (sourceMode != NONE) {
                downstream.onNext(null);
                return;
            }
            
            downstream.onNext(v);
        }

5. `LambdaObserver`的`onNext()`方法中，添加了对当前被观察者是否存活的状态的判断,如果存活，则调用开发者编写的逻辑!!

	    public void onNext(T t) {
	        if (!isDisposed()) {
	            try {
	                onNext.accept(t);
	            } catch (Throwable e) {
	                Exceptions.throwIfFatal(e);
	                get().dispose();
	                onError(e);
	            }
	        }
	    }



