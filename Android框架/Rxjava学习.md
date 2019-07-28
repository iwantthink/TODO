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

# 1. 创建过程

1. `Observable.create()`方法接收一个`ObservableOnSubscribe`类型的对象`source`(其包含数据发射逻辑)

	创建并返回了一个**`ObservableCreate`对象**(继承自`Observable`),包含了`source`对象

2. `Observable.subscribeOn()`方法接收一个`Scheduler`类型的对象`scheduler`(包含被订阅者所执行的线程信息)

	创建并返回了一个**`ObservableSubscribeOn`对象**(继承自`Observable`),包含了`ObservableCreate`对象(`source`)以及`Scheduler`信息

3. `Observable.map()`方法接收一个`Function`类型的对象`mapper`(包含对源数据的变换逻辑)

	创建并返回了一个**`ObservableMap`对象**(继承自`Observable`),包含了`ObservableSubscribeOn`对象(`source`)和`mapper`

至此，`Observable`的链式已经结束看一下现有的链式结构

	ObservableMap -> ObservableSubscribeOn -> ObservableCreate
					----source---
	ObservableSubscribeOn-> ObservableCreate -> ObservableOnSubscribe

# 2. 订阅过程


1. `Observable.subscribe()`方法有几个参数重载(`Observer`或者是几个`Consumer`,`Action`的组合)，注意`Consumer`和`Action`的组合 会封装成一个`LambdaObserver`并返回，但是传入参数如果是`Observer`则不会返回

	此外，`subscribe()`方法中还会调用`subscribeActual()`这个抽象方法，方便`Observable`的子类去扩展逻辑

	**根据上述的流程，这里的`Observable`实际类型就是`ObservableMap`**

2. `ObservableMap.subscribeActual()`方法接收一个`Observer`参数(包含消息处理逻辑，可能是一个`Observer`/`LambdaObserver`),**下面以`LambdaObserver`为例分析**

	1. 参数t的类型是`Observer`，即`LambdaObserver`类型,`source`为`ObservableSubscribeOn`对象

			// ObservableMap类
		    public void subscribeActual(Observer<? super U> t) {
		    	 // ObservableSubscribeOn.subscribe()
		        source.subscribe(new MapObserver<T, U>(t, function));
		    }

		- **创建了一个`MapObserver`对象，封装了`LambdaObserver`和`Function`(包含对源数据的转换逻辑)**
		
		**`LambdaObserver`类型的参数对象`t`会保存到`MapObserver`的父类中的`downstream`成员变量中**
		
		然后将这个对象**传入`ObservableSubscribeOn`的`subscribe()`方法**

3. `ObservableSubscribeOn`类并没有重写`subscribe()`方法，所以`subscribe()`方法还是参考`Observable`类中的逻辑，**因此只用看`subscribeActual()`方法的逻辑**

	1. 参数`observer`的类型是`MapObserver`

			// ObservableSubscribeOn 类
		    public void subscribeActual(final Observer<? super T> observer) {
		        final SubscribeOnObserver<T> parent = new SubscribeOnObserver<T>(observer);
		        // MapObserver.onSubscribe
		        observer.onSubscribe(parent);
		
		        parent.setDisposable(scheduler.scheduleDirect(new SubscribeTask(parent)));
		    }

		- **创建了一个`SubscribeOnObserver`对象，封装了`MapObserver`**，其内部用`downstream`变量保存了`MapObserver`对象，并创建了一个`AtomicReference`类型的`upstream`

		- `MapObserver`没有实现`onSubscribe()`,具体的逻辑在其父类`BasicFuseableObserver`.这里的`downstream`是构造函数中传入的`LambdaObserver`
		
		    	public final void onSubscribe(Disposable d) {
			        if (DisposableHelper.validate(this.upstream, d)) {
			
			            this.upstream = d;
			            if (d instanceof QueueDisposable) {
			                this.qd = (QueueDisposable<T>)d;
			            }
			
			            if (beforeDownstream()) {
								// downstream = LambdaObserver
								// 回调了开发者编写的`onSubscribe()`
			                downstream.onSubscribe(this);
			
			                afterDownstream();
			            }
			
			        }
			    }
	    
	    - 创建了一个`SubscribeTask`对象(`Runnable`类型),封装了`SubscribeOnObserver`对象

			    final class SubscribeTask implements Runnable {
			        private final SubscribeOnObserver<T> parent;
					
			        SubscribeTask(SubscribeOnObserver<T> parent) {
			            this.parent = parent;
			        }
			
			        @Override
			        public void run() {
			            source.subscribe(parent);
			        }
			    }
      
       - `Scheduler.scheduleDirect()`方法中，会根据`Scheduler`类型的不同，创建不同的线程去执行`SubscribeTask`,以`IoScheduler`为例,就会创建一个线程池去执行

       **此外内部还会将`SubscribeTask`再次封装，得到一个`DisposeTask`对象,执行并返回**
	    			
	    		 // run-> SubscribeTask  
			    public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
			    	  
			        final Worker w = createWorker();
			
			        final Runnable decoratedRun = RxJavaPlugins.onSchedule(run);
			
			        DisposeTask task = new DisposeTask(decoratedRun, w);
			
			        w.schedule(task, delay, unit);
			
			        return task;
			    }
	    
	    - `createWorker()`是抽象方法由具体子类实现,主要就是创建具体执行任务的线程

		- **`SubscribeOnObserver`类型是`AtomicReference`,调用其`setDisposable()`实际上会借助`DisposableHelper`将`Scheduler.scheduleDirect()`返回的对象添加到自身**


4. 在插入到线程池中后，`DisposeTask`会被执行,在其内部调用`SubscribeTask`的`run()`方法

		 // SubscribeTask
        public void run() {
            source.subscribe(parent);
        }

	- 这时，`SubscribeTask`就运行在`subscribeOn()`所指定的线程中

	- `source`指的是`ObservableCreate`,`parent`指的是`SubscribeOnObserver`

5. `ObservableCreate`中创建了`CreateEmitter`对象，封装了`SubscribeOnObserver`,然后将`CreateEmitter`和`SubscribeOnObserver`之间创建联系
	
	    protected void subscribeActual(Observer<? super T> observer) {
	        CreateEmitter<T> parent = new CreateEmitter<T>(observer);
	        
	        //SubscribeOnObserver 与 CreateEmitter 创建联系,前者保存后者的引用
	        observer.onSubscribe(parent);
	
	        try {
	            source.subscribe(parent);
	        } catch (Throwable ex) {
	            Exceptions.throwIfFatal(ex);
	            parent.onError(ex);
	        }
	    }
	
	- `source`是`ObservableOnSubscribe`,是开发者编写的发射事件逻辑


![](http://ww1.sinaimg.cn/large/6ab93b35ly1g5c61y0kmoj21kc16iwre.jpg)


在订阅过程中，将观察者`Observer`一步步的进行封装

1. 最原始的观察者是开发者编写的发射逻辑,其被封装成`LambdaObserver`

2. 在指定数据的转换逻辑时，将`LambdaObserver`和源数据的转换逻辑 封装成`MapObserver`

3. 在指定被观察者所在的线程时,`MapObserver`被封装成了`SubscribeOnObserver`

	**在这一步，还将`SubscribeOnObserver`做额外的俩次封装,先是封装`SubscribeOnObserver`成`SubscribeTask`,再将`SubscribeTask`封装成`DisposeTask`,更重要的是，在这里将`DisposeTask`放入了`subscribeOn()`传入的的`Scheduler`中执行，因此在后续的开发者通过`CreateEmitter`调用的逻辑都将执行在`Scheduler`中**

4. 最后为了提供给开发者使用，还会再次进行封装，将`SubscribeOnObserver`封装成`CreateEmitter`

	举个例子,`CreateEmitter`调用`onNext()`发送数据,会一层一层的往回调用,先是调用`SubscribeOnObserver`的`onNext()`,再调用`MapObserver`,再调用`LambdaObserver`,最后调用开发者编写的`Consumer`函数
	

**如果存在`observeOn()`方法呢?**

- 假设我们在订阅前添加了`observeOn()`,那么在创建过程中`ObservableMap`会和订阅者执行的线程信息一起被封装到`ObservableObserveOn`

	那么在订阅过程中，**`ObservableObserveOn`的`subscribeAcutal()`方法会最先被调用**，在方法中会创建一个`Worker`包含一个运行在主线程的`Handler`,然后将`LambdaObserver`封装成`ObserveOnObserver`对象，并交给`ObservableMap`去处理(即封装成`MapObserver`),接下来的流程与上面相似....
	
	在执行过程中，调用`CreateEmitter`的`onNext()`方法,最终会回到`ObserveOnObserver`的`onNext()`,而`ObserveOnObserver `会执行在之前创建好的`worker`中(即通过Handler切换到主线程中)
	
	然后`ObservableObserveOn`的`run()`方法会在主线程中回调,并回调`LambdaObserver`......



# 3. 执行过程

从这里开始，就已经执行在了`subscribeOn()`方法所指定的线程当中,


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



