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

- `RxJavaPlugins.onAssembly()`暂时不用理会，就当做直接执行了`ObservableCreate`对象

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
        observer.onSubscribe(parent);

        try {
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

在`create()`方法中编写了`ObservableOnSubscribe`的逻辑，其中通过`emitter.onNext("1")`方法发送数据,而这个`emitter`类型就是`CreateEmitter`

	// CreateEmitter类
	   public void onNext(T t) {
	        if (t == null) {
	            onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
	            return;
	        }
	        if (!isDisposed()) {
	            observer.onNext(t);
	        }
	    }

- 代码比较简单，先是非空判断，然后判断是否停止,满足条件后就是去调用`LambdaObserver`(即对开发者编写的回调逻辑的封装)

# 4. 调用dispose

在调用`subscribe`之后，会返回一个`Disposable`对象，其实际类型是`LambdaObserver`,该对象可以用来调用`dispose()`方法，用来销毁当前对象

	// LambdaObserver类
    public void dispose() {
        DisposableHelper.dispose(this);
    }

	// DisposableHelper
    public static boolean dispose(AtomicReference<Disposable> field) {
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


# Observable线程问题

1. `Observable.create()`方法接收一个`ObservableOnSubscribe`类型的对象`source`(其包含数据发射逻辑)

	创建并返回了一个**`ObservableCreate`对象**(继承自`Observable`),包含了`source`对象

2. `Observable.subscribeOn()`方法接收一个`Scheduler`类型的对象`scheduler`(包含执行线程信息)

	创建并返回了一个**`ObservableSubscribeOn`对象**(继承自`Observable`),包含了`ObservableCreate`对象以及`Scheduler`信息

3. `Observable.map()`方法创建并返回了一个**`ObservableMap`对象**(继承自`Observable`),包含了`ObservableSubscribeOn`对象和对源数据的变换逻辑

4. `Observable.subscribe()`方法创建并返回了一个**`LambdaObserver`对象**(继承自`Observer`),包含了`onNext,onError,onComplete,onSubscribe`等消息处理逻辑

	此外，这个方法中还会调用`subscribeActual()`这个抽象方法，方便`Observable`的子类去扩展

至此，`Observable`的链式已经结束看一下现有的链式结构

	ObservableMap -> ObservableSubscribeOn -> ObservableCreate


1. `ObservableMap.subscribeActual()`方法接收一个`LambdaObserver`参数(包含消息处理逻辑，是一个`Observer`), 该参数会和一个`Function`参数(包含了对源数据的变换逻辑) 一起被封装到一个**`MapObserver`对象**中

	source








