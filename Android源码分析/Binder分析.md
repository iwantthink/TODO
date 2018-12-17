# Binder
[为什么 Android 要采用 Binder 作为 IPC 机制？](https://www.zhihu.com/question/39440766)

[Binder学习指南-weishu](http://weishu.me/2016/01/12/binder-index-for-newer/)

[Binder 设计与实现](https://blog.csdn.net/universus/article/details/6211589)

[Binder系列-开篇-GitYUan](http://gityuan.com/2015/10/31/binder-prepare/)

[Android跨进程通信：图文详解 Binder机制 原理](https://blog.csdn.net/carson_ho/article/details/73560642)

**学习Binder路线：**

1. 先学会熟练使用AIDL进行跨进程通信（简单来说就是远程Service）
2. 看完本文
3. 看Android文档，Parcel, IBinder, Binder等涉及到跨进程通信的类
4. 不依赖AIDL工具，手写远程Service完成跨进程通信
5. 看《Binder设计与实现》
6. 看老罗的博客或者书（书结构更清晰）
7. 再看《Binder设计与实现》
8. 学习Linux系统相关知识；自己看源码。


# 1. AIDL实现过程

1. 首先在`src/main/aidl/包名`目录下创建`.aidl`文件，并添加如下代码

		// ITest.aidl
		package hmtdemo.hmt.com.hmtdemo.hmt;
		
		// Declare any non-default types here with import statements
		
		interface ITest {
		     int add(int a, int b);
		}

2. 其次，通过编译工具编译，会得到一个与之前创建的`.aidl`文件对应的`.java`文件，文件生成在`build/generated/source/aidl`目录下

		/*
		 * This file is auto-generated.  DO NOT MODIFY.
		 * Original file: C:\\Users\\renbo\\Documents\\HMT_DEMO_ANDROID\\hmt\\HMT_AUTO_DEMO\\app\\src\\main\\aidl\\hmtdemo\\hmt\\com\\hmtdemo\\hmt\\ITest.aidl
		 */
		package hmtdemo.hmt.com.hmtdemo.hmt;
		// Declare any non-default types here with import statements
		
		public interface ITest extends android.os.IInterface {
		    /**
		     * Local-side IPC implementation stub class.
		     */
		    public static abstract class Stub extends android.os.Binder implements hmtdemo.hmt.com.hmtdemo.hmt.ITest {
		        private static final java.lang.String DESCRIPTOR = "hmtdemo.hmt.com.hmtdemo.hmt.ITest";
		
		        /**
		         * Construct the stub at attach it to the interface.
		         */
		        public Stub() {
		            this.attachInterface(this, DESCRIPTOR);
		        }
		
		        /**
		         * Cast an IBinder object into an hmtdemo.hmt.com.hmtdemo.hmt.ITest interface,
		         * generating a proxy if needed.
		         */
		        public static hmtdemo.hmt.com.hmtdemo.hmt.ITest asInterface(android.os.IBinder obj) {
		            if ((obj == null)) {
		                return null;
		            }
		            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
		            if (((iin != null) && (iin instanceof hmtdemo.hmt.com.hmtdemo.hmt.ITest))) {
		                return ((hmtdemo.hmt.com.hmtdemo.hmt.ITest) iin);
		            }
		            return new hmtdemo.hmt.com.hmtdemo.hmt.ITest.Stub.Proxy(obj);
		        }
		
		        @Override
		        public android.os.IBinder asBinder() {
		            return this;
		        }
		
		        @Override
		        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
		            switch (code) {
		                case INTERFACE_TRANSACTION: {
		                    reply.writeString(DESCRIPTOR);
		                    return true;
		                }
		                case TRANSACTION_add: {
		                    data.enforceInterface(DESCRIPTOR);
		                    int _arg0;
		                    _arg0 = data.readInt();
		                    int _arg1;
		                    _arg1 = data.readInt();
		                    int _result = this.add(_arg0, _arg1);
		                    reply.writeNoException();
		                    reply.writeInt(_result);
		                    return true;
		                }
		            }
		            return super.onTransact(code, data, reply, flags);
		        }
		
		        private static class Proxy implements hmtdemo.hmt.com.hmtdemo.hmt.ITest {
		            private android.os.IBinder mRemote;
		
		            Proxy(android.os.IBinder remote) {
		                mRemote = remote;
		            }
		
		            @Override
		            public android.os.IBinder asBinder() {
		                return mRemote;
		            }
		
		            public java.lang.String getInterfaceDescriptor() {
		                return DESCRIPTOR;
		            }
		
		            @Override
		            public int add(int a, int b) throws android.os.RemoteException {
		                android.os.Parcel _data = android.os.Parcel.obtain();
		                android.os.Parcel _reply = android.os.Parcel.obtain();
		                int _result;
		                try {
		                    _data.writeInterfaceToken(DESCRIPTOR);
		                    _data.writeInt(a);
		                    _data.writeInt(b);
		                    mRemote.transact(Stub.TRANSACTION_add, _data, _reply, 0);
		                    _reply.readException();
		                    _result = _reply.readInt();
		                } finally {
		                    _reply.recycle();
		                    _data.recycle();
		                }
		                return _result;
		            }
		        }
		
		        static final int TRANSACTION_add = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
		    }
		
		    public int add(int a, int b) throws android.os.RemoteException;
		}

3. 系统在生成这个文件之后，开发者只需要继承`ITest.Stub`这个静态抽象类，实现其在IInterface中的方法，然后在`Service`的onBind方法中返回，即实现了AIDL

		public class RemoteService extends Service {
		    @Override
		    public void onCreate() {
		        super.onCreate();
		    }
		
		    @Override
		    public IBinder onBind(Intent intent) {
		        // Return the interface
		        return mBinder;
		    }
		
		    private final ITest.Stub mBinder = new ITest.Stub() {
		        public int add(int a,int b){
		            return a+b;
		        }
		    };
		}

4. 客户端进程在绑定了另外一个进程的Service之后，会在`onServiceConnection`回调里返回一个`IBinder`对象，然后会通过Stub的`asInterface()`方法去得到一个远程的AIDL对象用来调用。

		ITest mITest;
		private ServiceConnection mConnection = new ServiceConnection() {

		    public void onServiceConnected(ComponentName className, IBinder service) {
		       mITest = ITest.Stub.asInterface(service);
		    }
		
		    public void onServiceDisconnected(ComponentName className) {
		        Log.e(TAG, "Service has unexpectedly disconnected");
		        mIRemoteService = null;
		    }
		};


- Stub类继承自Binder，说明Stub是一个Binder本地对象。实现了`ITest`(即IInterface接口) ，说明Stub携带某种客户端需要的能力(即方法add)。另外Stub类有一个内部类Proxy，这个Proxy表示的是Binder代理对象

- `onServiceConnection()`方法中，传递给了我们一个`IBinder`类型的参数，这个对象是驱动给的。它可以是Binder本地对象(即Binder类型)，也可以是Binder代理对象(即BinderProxy类型)

- `asInterface`方法中，会通过`onServiceConnection`返回的那个`IBinder`类型的对象 去判断当前是否存在本地对象：

	如果是，说明client和server在同一个进程，那么这个IBinder就是本地对象。会将这个对象强制类型转换为Stub并返回。

	如果找不到，说明client和server不在同一个进程，那么这个IBinder就是代理对象。那么会创建一个代理对象Stub.Proxy并返回

        /**
         * Cast an IBinder object into an hmtdemo.hmt.com.hmtdemo.hmt.ITest interface,
         * generating a proxy if needed.
         */
        public static hmtdemo.hmt.com.hmtdemo.hmt.ITest asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof hmtdemo.hmt.com.hmtdemo.hmt.ITest))) {
                return ((hmtdemo.hmt.com.hmtdemo.hmt.ITest) iin);
            }
            return new hmtdemo.hmt.com.hmtdemo.hmt.ITest.Stub.Proxy(obj);
        }

## 1.1 AIDL方法调用

通过`asInterface()`方法可以获得 Stub类型或者Stub.Proxy类型的对象。

- 对于Binder本地对象(转换为Stub)，在调用方法时 是直接调用在Service中Stub的实现类的方法

- 对于Binder代理对象(转换为Stub.Proxy)，实现如下：

		@Override
		public int add(int a, int b) throws android.os.RemoteException {
			android.os.Parcel _data = android.os.Parcel.obtain();
			android.os.Parcel _reply = android.os.Parcel.obtain();
			int _result;
			try {
				_data.writeInterfaceToken(DESCRIPTOR);
				_data.writeInt(a);
				_data.writeInt(b);
				mRemote.transact(Stub.TRANSACTION_add, _data, _reply, 0);
				_reply.readException();
				_result = _reply.readInt();
			} finally {
				_reply.recycle();
				_data.recycle();
			}
			return _result;
		}

	首先通过`Parcel`将数据序列化，其次调用`mRemote.transact`方法。mRemote是就是`onServiceConnection`中IBinder参数，同时如果执行到这里，说明这个之前在`asInterface()`方法中 认为IBinder就是BinderProxy(即Binder类的内部类)
	
	    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
	        Binder.checkParcel(this, code, data, "Unreasonably large binder buffer");
	
	        if (mWarnOnBlocking && ((flags & FLAG_ONEWAY) == 0)) {
	            // For now, avoid spamming the log by disabling after we've logged
	            // about this interface at least once
	            mWarnOnBlocking = false;
	            Log.w(Binder.TAG, "Outgoing transactions from this process must be FLAG_ONEWAY",
	                    new Throwable());
	        }
	
	        final boolean tracingEnabled = Binder.isTracingEnabled();
	        if (tracingEnabled) {
	            final Throwable tr = new Throwable();
	            Binder.getTransactionTracker().addTrace(tr);
	            StackTraceElement stackTraceElement = tr.getStackTrace()[1];
	            Trace.traceBegin(Trace.TRACE_TAG_ALWAYS,
	                    stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName());
	        }
	        try {
	            return transactNative(code, data, reply, flags);
	        } finally {
	            if (tracingEnabled) {
	                Trace.traceEnd(Trace.TRACE_TAG_ALWAYS);
	            }
	        }
	    }

	可以看到最终返回的时候会调用`transactNative()`这个Native方法(frameworks/base/core/jni/android_util_Binder.cpp)。经过一系列调用之后这个Native方法最终会调用到`talkWithDriver`函数，那么这时通信就会交给驱动去完成。最终该函数通过`ioctl`系统调用，Client进程陷入内核态，Client调用add方法的线程挂起等待返回。驱动完成一系列操作之后唤醒Server进程，调用Server进程本地对象的`onTransact()`函数(这一步由Server端线程池完成)。**那么就看Binder本地对象的`onTransact()`方法(即Stub中的此方法)**

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_add: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    int _arg1;
                    _arg1 = data.readInt();
					//调用具体的实现
                    int _result = this.add(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

	**在Server进程里面，onTransact根据调用号（每个AIDL函数都有一个编号，在跨进程的时候，不会传递函数，而是传递编号指明调用哪个函数）调用相关函数**.在调用了Binder本地对象的add方法之后,这个方法将结果返回给驱动，驱动唤醒挂起的Client进程里面的线程并将结果返回，一次跨进程调用就完成了。

## 1.2 AIDL总结

AIDL这种通信方式是有一种固定的模式：

1. 一个需要跨进程传递的对象一定继承自IBinder

2. 如果是本地Binder对象，那么一定继承Binder实现IInterface

3. 如果是代理Binder对象，那么就需要实现IInterface并持有IBinder引用

**Proxy类和Stub类不一样。**

- 俩者都既是Binder又是IInterface。

- 不同点是对于Binder类的处理，Stub采用的是继承的方式(is关系)，Proxy采用的是组合的方式(has关系)。

- 俩者都实现了所有的IInterface函数

- 不同点是Stub使用策略模式调用的是虚函数(待子类实现)，而Proxy使用组合模式

	之所以俩者采用不同的模式实现，因为Stub本身就代表一个能跨进程传输的对象，它得继承IBinder并实现transact这个函数从而得到跨进程能力(这个能力由驱动赋予)，所以Stub通过继承Binder从而实现以上所说的能力

	Proxy类使用组合方式是因为它不需要知道自己具体是什么，它本身也不需要具备进程跨进程传输(即不需要继承Binder)，它只需要拥有这个能力(即保留IBinder的引用)

	Proxy类和Stub类 都提供了返回IBinder的方式，就是asBinder(),前者返回所持有的IBinder的引用，后者直接返回自身this。


## 1.3 AIDL在ActivityManagerService的体现

再去翻阅系统的ActivityManagerServer的源码，就知道哪一个类是什么角色了：

- IActivityManager是一个IInterface，它代表远程Service具有什么能力

- ActivityManagerNative指的是Binder本地对象（类似AIDL工具生成的Stub类），这个类是抽象类，它的实现是ActivityManagerService；因此对于AMS的最终操作都会进入ActivityManagerService这个真正实现；

- 同时如果仔细观察，ActivityManagerNative.java里面有一个非公开类ActivityManagerProxy, 它代表的就是Binder代理对象；是不是跟AIDL模型一模一样呢？

- 那么ActivityManager是什么？他不过是一个管理类而已，可以看到真正的操作都是转发给ActivityManagerNative进而交给他的实现ActivityManagerService 完成的。



# 2. IPC介绍以及Binder介绍

Android系统基于Linux内核，存在**进程隔离**，即对每个进程来说，并不知道其他进程的存在，**因此一个进程需要通过某种机制和另外一个进程进行进程通信**

- 进程隔离是为保护操作系统中进程互不干扰而设计的一组不同硬件和软件的技术。这个技术是为了避免进程A写入进程B的情况发生。 进程的隔离实现，使用了虚拟地址空间。进程A的虚拟地址和进程B的虚拟地址不同，这样就防止进程A将数据信息写入进程B

**Linux用户空间/内核空间之间的访问**

- `Linux Kernel`是操作系统的核心，独立于普通的应用程序，可以访问受保护的内存空间，也有访问硬件设备的所有权限

	对于Kernel存在一个保护机制，这个保护机制用来区分Kernel和上层的应用程序(称之为Kernel Space/User Space),即区分资源访问的权限，只能访问被许可的资源

	虽然从逻辑上抽离出**用户空间 和 内核空间**。但是还是存在一些用户空间会需要访问内核的资源(例如：应用程序访问文件，网络)

		Kernel space can be accessed by user processes only through the use of system calls.

	对此，Linux提供了**系统调用**这种方式，来使得用户空间访问内核空间。通过这个统一的入口接口，所有的资源访问都是在内核的控制下执行，以免用户对系统资源的越权访问，从而保障安全和稳定。

	当一个任务（进程）执行系统调用而陷入内核代码中执行时，我们就称进程处于**内核运行态（或简称为内核态）此时处理器处于特权级最高的（0级）内核代码中执行**。当进程在执行用户自己的代码时，则称其处于**用户运行态（用户态）**。**即此时处理器在特权级最低的（3级）用户代码中运行**。处理器在特权等级高的时候才能执行那些特权CPU指令。

**用户空间之间的访问：**

- 通过系统调用，用户空间可以访问内核空间。那么一个用户空间想要访问另外一个用户空间 可以让操作系统内核添加支持。传统的Linux通信机制，例如Socket，管道等都是内核支持的。

	Binder并不是Linux内核的一部分，它是通过**Linux的动态可加载内核模块(Loadable Kernel Module,LKM)**机制解决的通信。该模块是具有独立功能的程序，可以被单独编译但是不能单独运行。它在运行时被链接到内核作为内核的一部分在内核空间运行。

	也就是说：**Android系统通过添加一个内核模块运行在内核空间(该内核模块实现了Binder底层工作机制)，而用户进程通过这个模块作为桥梁 来完成通信。在Adnroid系统中，这个运行在内核空间，负责各个用户进程通信的内核模块叫做Binder驱动**

	- 驱动程序一般指的是设备驱动程序（Device Driver），是一种可以使计算机和设备通信的特殊程序。相当于硬件的接口，操作系统只有通过这个接口，才能控制硬件设备的工作；

		**驱动就是操作硬件的接口，为了支持Bindler通信过程，Binder使用了一种“硬件”，因此这个模块被称之为驱动**

## 2.1 为什么使用Binder？

Android使用的Linux内核拥有着非常多的跨进程通信机制，比如管道，System V，Socket等；为什么还需要单独搞一个Binder出来呢？**主要有两点，性能和安全**。在移动设备上，广泛地使用跨进程通信肯定对通信机制本身提出了严格的要求；Binder相对出传统的Socket方式，更加高效；另外，传统的进程通信方式对于通信双方的身份并没有做出严格的验证，只有在上层协议上进行架设；比如Socket通信ip地址是客户端手动填入的，都可以进行伪造；而Binder机制从协议本身就支持对通信双方做身份校检，因而大大提升了安全性。这个也是Android权限模型的基础。

- 从IPC角度来说：Binder是Android中的一种跨进程通信方式，该通信方式在linux中没有，是Android独有；

- 从Android Driver层：Binder还可以理解为一种虚拟的物理设备，它的设备驱动是/dev/binder；

- 从Android Native层：Binder是创建Service Manager以及BpBinder/BBinder模型，搭建与binder驱动的桥梁；

- 从Android Framework层：Binder是各种Manager（ActivityManager、WindowManager等）和相应xxxManagerService的桥梁；

- 从Android APP层：Binder是客户端和服务端进行通信的媒介，当bindService的时候，服务端会返回一个包含了服务端业务调用的 Binder对象，通过这个Binder对象，客户端就可以获取服务端提供的服务或者数据，这里的服务包括普通服务和基于AIDL的服务。

## 2.2 Binder通信模型

跨进程通信通常分为CS，即Server进程和Client进程，由于进程隔离的存在，它们之间无法直接通信

**日常生活中通信：**

- 假设A.B进行通信(A是Client,B是Server)，通信的媒介是打电话,A要打电话给B，那么必须知道B的电话，电话从通信录中得知

	必须先查阅通信录，拿到B的电话，A才能和B进行通信。另外这里还需要基站的支持

**Bindler通信：**

- 俩个运行在用户空间的进程要进行通信，需要借助内核的帮助(实际上是需要**动态可加载内核模块**)，这个运行在内核中的程序叫做**Binder驱动**，它的功能类似基站，另外打电话通信中的通信录对应Binder通信中的`ServiceManager`.

	![weishu的图...挂了](http://7xp3xc.com1.z0.glb.clouddn.com/binder-model.png)

**整体流程如下：**

1. ServiceManager建立(即 通信录建立)，首先有一个进程向驱动提出申请为SM。驱动同意之后，SM进程负责管理Service(这里管理的是Service而不是Server，因为CS是可以反过来的，Server也有可能会向Client进行通信。只不过这个时候 代表Client的Service并没有在SM中注册)

2. 各个Server向SM注册：每个Server端进程启动之后，通过SM进行注册，例如我是AMS,我的地址是0x123，依次类推。这样SM就能建立一个对应关系，对应着Server的名字和地址

3. Clinet想要和Server通信，首先通过这个SM拿到Server的地址，然后根据这个地址进行通信

- 在这个流程中，**无论是client和Server的通信 还是client和sm的通信 都会通过 Binder驱动**

- 以上就是整个Binder通信的基本模型，需要注意的是SM整个系统中只有一份，驱动也只有一个

## 2.3 Binder机制跨进程原理

Binder通信中的四个角色：client,server,sm,driver。那么具体client如何与server完成通信的？

**普通的进程间通信的原理：**

- 内核可以访问A和B的所有数据；所以，最简单的方式是通过内核做中转；假设进程A要给进程B发送数据，那么就先把A的数据copy到内核空间，然后把内核空间对应的数据copy到B就完成了；用户空间要操作内核空间，需要通过系统调用；刚好，这里就有两个系统调用：`copy_from_user, copy_to_user`。

**Bindler机制：**

Binder机制并不是通过以上方式实现的，**通信是一个广泛的概念，只要一个进程能调用另外一个进程里某个对象的方法，那么具体要完成什么通信内容就很容易**

![图挂了](http://7sbqce.com1.z0.glb.clouddn.com/2016binder-procedure.png)

1. Server进程向SM注册，告诉SM自己的名字，有什么能力。即Server告诉SM自己叫ASM，自己有个object对象，可以执行add操作。于是SM建立了一张表，然后保存了这些信息(简化了SM注册的流程，见下文)

2. client向SM查询：我需要联系 名字=ASM 的进程中的object对象。**这时候，进程之间通信的数据都会经过运行在内核空间里面的Binder驱动，Binder驱动在数据流过的时候做了一点处理，它并不会直接给client返回一个真正的object对象，而是返回一个object的代理对象(objectProxy),这个objectProxy也有一个add方法，这个add方法所做的事情只是将参数进行包装然后交给binder驱动**

3. client并不知道返回的对象不是真正的对象，其实它也没有必要知道，因为它只需要一个结果。那么client拿到了这个代理对象并调用对应的方法之后，这个方法包装了参数 并交给驱动

4. 驱动在收到包装后的数据，知道是objectProxy 传回来的数据，然后这个objectProxy对应的是object。于是binder驱动通知server进程，调用server进程的object的add方法，在server进程处理完之后，返回处理结果给binder驱动，驱动再把这个结果返回给client。至此 整个流程结束。

- **由于驱动返回的objectProxy与Server进程中的十分相似，给人的感觉像是Server进程中的object对象传递到了Client进程。因此 称Binder对象是可以进行跨进程传递的对象**

	但是实际上Binder跨进程传输时并不是真的把一个对象传输到了另外一个进程

- **对于Binder的访问，如果是在同一个进程(不需要跨进程)，那么直接返回原始的Binder实体。如果在不同的进程，那么会返回一个代理对象**。这一点可以在AIDL的生成代码中看到

- SM和Server通常不在一个进程中，所以Server进程向SM注册的过程也是跨进程通信，驱动也会对这个过程进行一些处理：**Server注册到SM中的对象实际上也是代理对象**，当client向SM查询的时候，驱动会返回给client另外一个代理对象。实际上Server进程的本地对象仅有一个，其他进程拥有的都是它的代理。

**Client进程只不过是持有了Server端的代理；代理对象协助驱动完成了跨进程通信。**

## 2.4 Binder具体的体现

Binder的设计采用了面向对象的思想，在Binder通信模型的四个角色中(client,server,sm,driver)都代表"Binder"。这样对于Binder通信的使用者来说，Server里的Binder或者Client里的Binder没有区别，只要一个Binder对象能够返回结果就行，甚至SM，Binder驱动都不用关心，这就是抽象的体现


**各类Binder对象：**

- **通常情况下,Binder指的是一种通信机制**。使用AIDL进行Binder通信，这时的Binder就是指Binder这种IPC机制

- 对于Server进程来说，**Binder指的是Binder本地对象**

- 对于Clinet进程来说，**Binder指的是Binder代理对象**，其只是Binder本地对象的一个远程代理，对于这个代理Binder对象的操作，会通过驱动最终转发到Binder本地对象上去完成。(对于拥有Binder的使用者来说，并不会关心其拥有的是代理Binder还是本地Binder)

- 对于传输过程来说，Binder是可以进行跨进程传递的对象。Binder驱动会对具有跨进程传递能力的对象做特殊处理，自动完成代理对象和本地对象的转换。(即将代理对象收到的参数 传给 本地对象， 再将本地对象返回的结果 传给 代理对象)

**通过面向对象思想，将进程间的通信转化为对某个Binder对象的引用的调用**。实际上Binder对象是一个可以跨进程引用的对象，它的实体(本地对象)位于一个进程中，而其代理对象可以在系统的任意一个进程中。**同时，这个引用和Java里的引用一样，既可以是强类型也可以是弱类型，而且可以从一个进程传递给另外的进程，让所有进程都能访问同一个Server，这样就像将一个对象或引用 赋值给了另外一个引用**。Binder模糊了进程边界，淡化了进程间通信过程，整个系统仿佛运行于同一个面向对象的程序之中。形形色色的Binder对象以及星罗棋布的引用仿佛粘接各个应用程序的胶水，这也是Binder在英文里的原意。

### 2.4.1 驱动里的Binder

已知Server进程里的Binder对象是Binder本地对象，Client进程里的Binder对象是Binder代理对象，且在Binder对象进行跨进程传递的时候(**由于驱动返回的objectProxy与Server进程中的十分相似，给人的感觉像是Server进程中的object对象传递到了Client进程。因此 称Binder对象是可以进行跨进程传递的对象**)，Binder驱动会自动完成本地对象和代理对象的转换。

因此，Binder驱动必然保存了每一个跨进程的Binder对象的相关信息。在驱动中，**Binder本地对象 体现为 一个叫做`binder_node`的数据结构,Binder代理对象 体现为 一个叫做`binder_ref`的数据结构**。有的地方将Binder本地对象称为Binder实体，将Binder代理对象称为Binder引用(句柄)。其实就是指的Binder对象在驱动力的表现形式


### 2.4.2 Java层的Binder

[Android-AIDL官方文档](https://developer.android.com/guide/components/aidl?hl=zh-cn#CreateAidl)

AIDL中存在以下几个类 :

1. `IBinder`:IBinder是一个接口，代表了**跨进程传输的能力**。只要实现了该接口，就能将这个对象进行跨进程传递，这是来自驱动底层的支持。在跨进程数据流经驱动时，驱动会识别`IBinder`类型的数据，从而自动完成不同进程Binder对象的转换(本地对象->代理对象)

2. `IInterface`: IInterface代表远程Server对象具有什么能力，具体来说就是aidl文件中的提供的接口，也可以理解为 client和server的调用契约

3. `Binder`:**Java层的Binder类，代表的是Binder本地对象**。继承自IBinder，因此具有跨进程能力。**跨进程时，Binder驱动会自动完成Binder和BinderProxy的转换**

4. `BinderProxy`:**BinderProxy是Binder类的一个内部类，代表远程进程的Binder对象的本地代理**。继承自IBinder，因此具有跨进程能力

5. `Stub`: 使用AIDL时，编译工具会生成一个**Stub的静态内部类**，这个类**继承自Binder**，说明它是Binder本地对象，同时该Stub类还实现了IInterface接口，表明它具有远程Server承诺给Client的能力。Stub是一个抽象类，具体的IInterface的相关实现需要手动完成(策略模式的体现)