# Handler分析
[Android Handler源码级分析以及实现](https://blog.csdn.net/liurenyou/article/details/72805916)
[Android 异步消息处理机制 让你深入理解 Looper、Handler、Message三者关系](https://blog.csdn.net/lmj623565791/article/details/38377229/)

[Android异步消息处理机制完全解析，带你从源码的角度彻底理解](https://blog.csdn.net/guolin_blog/article/details/9991569)


[Handler Looper Message 官方源码地址](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/)
# 1. 简介

Handler,Looper,Message 组成了Android的异步消息处理机制，异步消息处理线程启动后会进入一个无限的循环体，每循环一次，就会从其内部的消息队列中取出一个消息，然后回调相应的消息处理函数，执行完一个消息后继续循环，**若消息队列为空，线程则会阻塞并等待**。

对应到Handler,Looper,Message.Looper负责创建一个MessageQueue，然后会开始一个永真循环，不断的从MessageQueue中读取消息，如果队列为空则阻塞并等待，Handler负责添加Message到Looper的MessageQueue中

Handler,Looper,Message 都是在android.jar包下的。

# 2. Looper

## 2.1 prepare()

     /** Initialize the current thread as a looper.
      * This gives you a chance to create handlers that then reference
      * this looper, before actually starting the loop. Be sure to call
      * {@link #loop()} after calling this method, and end it by calling
      * {@link #quit()}.
      */
    public static void prepare() {
        prepare(true);
    }

    private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        sThreadLocal.set(new Looper(quitAllowed));
    }

    /**
     * Initialize the current thread as a looper, marking it as an
     * application's main looper. The main looper for your application
     * is created by the Android environment, so you should never need
     * to call this function yourself.  See also: {@link #prepare()}
     */
    public static void prepareMainLooper() {
        prepare(false);
        synchronized (Looper.class) {
            if (sMainLooper != null) {
                throw new IllegalStateException("The main Looper has already been prepared.");
            }
            sMainLooper = myLooper();
        }
    }

    /**
     * Return the Looper object associated with the current thread.  Returns
     * null if the calling thread is not associated with a Looper.
     */
    public static @Nullable Looper myLooper() {
        return sThreadLocal.get();
    }

- `prepare()`方法会为当前Thread创建一个Looper，然后保存到ThreadLocal中。该方法不能重复调用，否则会抛出异常

- `prepareMainLooper()`方法是由Android environment调用的（具体的代码是 在`ActivityThread.main()`中），同样的创建了Looper并放入了ThreadLocal中

## 2.2 构造函数

    private Looper(boolean quitAllowed) {
        mQueue = new MessageQueue(quitAllowed);
        mThread = Thread.currentThread();
    }

在Looper的构造函数中做了俩件事情：

1. 创建了MessageQueue

	**在最新版本的Android 26 中，创建MessageQueue时需要传入一个布尔值，代表这个MessageQueue是否可以退出。主线程的Looper 是无法退出的，其他是可退出的**

2. 将Looper绑定到当前的线程

	**实际上，通过ThreadLocal 能够保证一个线程只会有一个Looper，同时一个Looper只持有一个MessageQueue**

## 2.3 loop()

    /**
     * Run the message queue in this thread. Be sure to call
     * {@link #quit()} to end the loop.
     */
    public static void loop() {
        final Looper me = myLooper();
        if (me == null) {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
        final MessageQueue queue = me.mQueue;

        // Make sure the identity of this thread is that of the local process,
        // and keep track of what that identity token actually is.
        Binder.clearCallingIdentity();
        final long ident = Binder.clearCallingIdentity();

        for (;;) {
            Message msg = queue.next(); // might block
            if (msg == null) {
                // No message indicates that the message queue is quitting.
                return;
            }

            // This must be in a local variable, in case a UI event sets the logger
            final Printer logging = me.mLogging;
            if (logging != null) {
                logging.println(">>>>> Dispatching to " + msg.target + " " +
                        msg.callback + ": " + msg.what);
            }

            final long slowDispatchThresholdMs = me.mSlowDispatchThresholdMs;

            final long traceTag = me.mTraceTag;
            if (traceTag != 0 && Trace.isTagEnabled(traceTag)) {
                Trace.traceBegin(traceTag, msg.target.getTraceName(msg));
            }
            final long start = (slowDispatchThresholdMs == 0) ? 0 : SystemClock.uptimeMillis();
            final long end;
            try {
                msg.target.dispatchMessage(msg);
                end = (slowDispatchThresholdMs == 0) ? 0 : SystemClock.uptimeMillis();
            } finally {
                if (traceTag != 0) {
                    Trace.traceEnd(traceTag);
                }
            }
            if (slowDispatchThresholdMs > 0) {
                final long time = end - start;
                if (time > slowDispatchThresholdMs) {
                    Slog.w(TAG, "Dispatch took " + time + "ms on "
                            + Thread.currentThread().getName() + ", h=" +
                            msg.target + " cb=" + msg.callback + " msg=" + msg.what);
                }
            }

            if (logging != null) {
                logging.println("<<<<< Finished to " + msg.target + " " + msg.callback);
            }

            // Make sure that during the course of dispatching the
            // identity of the thread wasn't corrupted.
            final long newIdent = Binder.clearCallingIdentity();
            if (ident != newIdent) {
                Log.wtf(TAG, "Thread identity changed from 0x"
                        + Long.toHexString(ident) + " to 0x"
                        + Long.toHexString(newIdent) + " while dispatching to "
                        + msg.target.getClass().getName() + " "
                        + msg.callback + " what=" + msg.what);
            }

            msg.recycleUnchecked();
        }
    }

1. 首先会获取Looper，即从ThreadLocal中获取当前线程的Looper。然后从Looper 中拿到对应的MessageQueue

2. 进入`for(;;)`形式的永真循环，通过MessageQueue的next()方法去获取 `Message`,如果取不到值 会阻塞

3. 调用`msg.target.dispatchMessage(msg);`去执行message,`msg.target`实际上就是Handler

**注意：主线程中在AndroidThread中已经调用了Looper.loop()以及Looper.prepareMainLooper()**

# 3. Hanlder

Handler通常作为Message的发送体出现，会连接MessageQueue 和 Message

**Handler通常有三种形式：**

1. 重写Handler的handleMessage()方法

        Handler handler1 = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

2. 初始化Handler时，传入一个Callback

        Handler handler2 = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        });

3. 普通初始化，在发送消息时传入一个Runnable类型的参数

		  Handler handler = new Handler();
		        handler.post(new Runnable() {
		            @Override
		            public void run() {
		
		            }
		        });

## 3.1 构造函数

    public Handler() {
        this(null, false);
    }
	//使用传入的Callback当做具体的处理message的地方
    public Handler(Callback callback) {
        this(callback, false);
    }
	//使用传入的Looper而不是默认的
    public Handler(Looper looper) {
        this(looper, null, false);
    }

    public Handler(Looper looper, Callback callback) {
        this(looper, callback, false);
    }

    public Handler(boolean async) {
        this(null, async);
    }


    public Handler(Callback callback, boolean async) {
        if (FIND_POTENTIAL_LEAKS) {
            final Class<? extends Handler> klass = getClass();
            if ((klass.isAnonymousClass() || klass.isMemberClass() || klass.isLocalClass()) &&
                    (klass.getModifiers() & Modifier.STATIC) == 0) {
                Log.w(TAG, "The following Handler class should be static or leaks might occur: " +
                    klass.getCanonicalName());
            }
        }

        mLooper = Looper.myLooper();
        if (mLooper == null) {
            throw new RuntimeException(
                "Can't create handler inside thread that has not called Looper.prepare()");
        }
        mQueue = mLooper.mQueue;
        mCallback = callback;
        mAsynchronous = async;
    }

    public Handler(Looper looper, Callback callback, boolean async) {
        mLooper = looper;
        mQueue = looper.mQueue;
        mCallback = callback;
        mAsynchronous = async;
    }

**如果Handler外部有传入Looper的话，会使用这个Looper**，否则会调用`Looper.myLooper()`去ThreadLocal中获取一个**当前线程的`Looper`**，从这里也可以看出必须要在初始化之前调用`Looper.prepare()`,否则会抛出异常。**注意：UI Thread 的Looper.prepare()方法 已经在AndroidThread 入口处被调用**

## 3.2 发送消息

发送消息有俩种形式：

1. post() 形式

	    public final boolean post(Runnable r)
	    {
	       return  sendMessageDelayed(getPostMessage(r), 0);
	    }
	
	    public final boolean postAtTime(Runnable r, long uptimeMillis)
	    {
	        return sendMessageAtTime(getPostMessage(r), uptimeMillis);
	    }
	
	    public final boolean postAtTime(Runnable r, Object token, long uptimeMillis)
	    {
	        return sendMessageAtTime(getPostMessage(r, token), uptimeMillis);
	    }
	
	    public final boolean postDelayed(Runnable r, long delayMillis)
	    {
	        return sendMessageDelayed(getPostMessage(r), delayMillis);
	    }
	
	    public final boolean postAtFrontOfQueue(Runnable r)
	    {
	        return sendMessageAtFrontOfQueue(getPostMessage(r));
	    }


2. sendMessage()

	    public final boolean sendMessage(Message msg)
	    {
	        return sendMessageDelayed(msg, 0);
	    }
	
	    public final boolean sendEmptyMessage(int what)
	    {
	        return sendEmptyMessageDelayed(what, 0);
	    }
	
	    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
	        Message msg = Message.obtain();
	        msg.what = what;
	        return sendMessageDelayed(msg, delayMillis);
	    }
	
	    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
	        Message msg = Message.obtain();
	        msg.what = what;
	        return sendMessageAtTime(msg, uptimeMillis);
	    }
	
	    public final boolean sendMessageDelayed(Message msg, long delayMillis)
	    {
	        if (delayMillis < 0) {
	            delayMillis = 0;
	        }
	        return sendMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis);
	    }
	
	    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
	        MessageQueue queue = mQueue;
	        if (queue == null) {
	            RuntimeException e = new RuntimeException(
	                    this + " sendMessageAtTime() called with no mQueue");
	            Log.w("Looper", e.getMessage(), e);
	            return false;
	        }
	        return enqueueMessage(queue, msg, uptimeMillis);
	    }
	
	    public final boolean sendMessageAtFrontOfQueue(Message msg) {
	        MessageQueue queue = mQueue;
	        if (queue == null) {
	            RuntimeException e = new RuntimeException(
	                this + " sendMessageAtTime() called with no mQueue");
	            Log.w("Looper", e.getMessage(), e);
	            return false;
	        }
	        return enqueueMessage(queue, msg, 0);
	    }

post形式 会将传入的Runnable 组装成一个msg 继续调用

**post形式和sendMessage形式的方法在于最终会调用 sendMessageAtTime(),各个方法只是参数的形式不同**

	
	    private boolean enqueueMessage(MessageQueue queue, Message msg, long uptimeMillis) {
	        msg.target = this;
	        if (mAsynchronous) {
	            msg.setAsynchronous(true);
	        }
	        return queue.enqueueMessage(msg, uptimeMillis);
	    }

**注意：这里的`msg.target = this`，表示将当前Handler 赋值给了msg.target。**最终会调用MessageQueue.enqueueMessage()方法，将message插入到队列中

## 3.3 dispatchMessage()

    
    /**
     * Handle system messages here.
     */
    public void dispatchMessage(Message msg) {
        if (msg.callback != null) {
            handleCallback(msg);
        } else {
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return;
                }
            }
            handleMessage(msg);
        }
    }

该方法是Looper.loop()中，取出message时 会传递给这个函数去执行。

1. 首先判断是否是以post()形式传递的消息，如果是成立 则取出其中的Runnable 去执行

2. 其次判断Handler是否是以 传入Callback的形式初始化的，如果成立，则调用Callback.handleMessage(msg)

3. 如果 Callback 为空，或者 Callback中没有处理该信息，那么会走到 `handleMessage`函数(重写handleMessage函数的形式进行初始化)

# 4. MessageQueue

消息队列，实际存放Message的地方

## 4.1 构造函数

    MessageQueue(boolean quitAllowed) {
        mQuitAllowed = quitAllowed;
        mPtr = nativeInit();
    }

quitAllowed 表示当前消息队列是否可以退出

mPtr表示 本地代码的初始化


## 4.2 enqueueMessage

    boolean enqueueMessage(Message msg, long when) {
        if (msg.target == null) {
            throw new IllegalArgumentException("Message must have a target.");
        }
        if (msg.isInUse()) {
            throw new IllegalStateException(msg + " This message is already in use.");
        }

        synchronized (this) {
            if (mQuitting) {
                IllegalStateException e = new IllegalStateException(
                        msg.target + " sending message to a Handler on a dead thread");
                Log.w(TAG, e.getMessage(), e);
                msg.recycle();
                return false;
            }

            msg.markInUse();
            msg.when = when;
            Message p = mMessages;
            boolean needWake;
            if (p == null || when == 0 || when < p.when) {
                // New head, wake up the event queue if blocked.
                msg.next = p;
                mMessages = msg;
                needWake = mBlocked;
            } else {
                // Inserted within the middle of the queue.  Usually we don't have to wake
                // up the event queue unless there is a barrier at the head of the queue
                // and the message is the earliest asynchronous message in the queue.
                needWake = mBlocked && p.target == null && msg.isAsynchronous();
                Message prev;
                for (;;) {
                    prev = p;
                    p = p.next;
                    if (p == null || when < p.when) {
                        break;
                    }
                    if (needWake && p.isAsynchronous()) {
                        needWake = false;
                    }
                }
                msg.next = p; // invariant: p == prev.next
                prev.next = msg;
            }

            // We can assume mPtr != 0 because mQuitting is false.
            if (needWake) {
                nativeWake(mPtr);
            }
        }
        return true;
    }

MessageQueue 并没有使用一个集合把所有的消息保存起来。它只使用了一个mMessages表示当前待处理的消息，然后进行入队操作，即将所有的消息按时间进行排序(uptimeMillis参数)，然后调用`msg.next()` 去指定每一个消息的下一个消息

如果是通过sendMessageAtFrontOfQueue()方法发送消息，也会代用`enqueueMessage()`来让消息入队，只是uptimeMillis参数为0，这样会插入到队列的头部，然后会赋值`mMessages`参数，用来让下一个Message 进行比较

最终通过 `nativeWake(mPtr);`方法，调用本地代码去唤醒阻塞的线程