# 1. Giffun分析


# 2. GifFunApplication

初始化 GifFun类，该类是全局APi接口

- 持有ApplicationContext

- 创建位于主线程的Handler

- 获取用户的登录状态(从Sp中)

初始化LitePal数据库

设置Dalvik可执行文件分包支持

- [64K引用限制](https://developer.android.com/studio/build/multidex.html?hl=zh-CN)


# 3. 基类

## 3.1 BaseActivity

1. 通过onCreate-onDestroy对每个Activity引用进行持有和移除，并注册和解绑EventBus

2. 通过onResume-onPause获取当前是否位于前台的状态

3. 重写了setCOntentView()并调用setupViews()，在该方法中会寻找loading控件

4. 定义了设置toolbar的方法，设置状态栏透明方法，显示和隐藏软键盘

	展示和隐藏表示 服务器内容错误，网络失败，无内容 等控件的方法
	
	展示和隐藏显示进度的方法(dialog)
	
5. 	提供了默认的EventBus消息处理逻辑,如果遇到ForceToLoginEvent 则会将页面全部关闭并返回登录页面

6. 提供检查运行时权限的方法,保存权限申请回调,重写了权限检查回调，并检查权限是否申请成功，并回调底层页面提供的权限回调

	抽象出permissionsGranted()方法
	
7. 重写了onOptionsItemSelected()方法，定义了R.id.home按钮的逻辑,默认逻辑是关闭当前的页面

8. 实现了RequestLifecycle接口（定义了进行网络请求所需要经历的声明周期函数），具体实现了代表网络请求中三个步骤的方法

	1. 开始网络加载: 显示loading控件，隐藏其他
	
	2. 结束网络加载: 隐藏loading
	
	3. 网络加载失败: 隐藏loading

## 3.2 AuthActivity

该类是登录和注册Activity的基类，继承自BaseActivity,并且为抽象类

1. 提供登录类型以及登录类型对应的名称 等常量

2. 提供存储用户身份信息的方法(将id,token等信息保存到SP,同时刷新内存中的登录状态)

3. 从服务端获取用户的基本信息,昵称,头像等等信息

	TODO 待分析具体

4. 定义了抽象的跳转至 MainActivity的方法	

## 3.3 BaseFragment

1. 提供了onCreateView方法，用来查找布局中的loading控件

2. 具体实现了RequestLifecycle接口，提供了进行网络请求所需要的生命周期函数的具体实现

3. 权限处理逻辑的封装

4. 页面无内容，网络异常导致加载失败，服务器异常导致加载失败等等等布局的展示函数



# 4. SPlashActivity

SplashActivity重写了onCreate()方法，并执行了延迟跳转

- 延迟跳转会根据是否处于登录状态，决定跳转至MainActivity或LoginActivity

	此外，对于跳转至LoginActivity,还会通过检查当前页面是否处于前台来判断是否执行转场动画

	- 跳转方法位于被打开的Activity中，这里的Intent 使用的是Action 进行跳转

- OpenSourceSplashActivity重写了onCreate()方法，并提供了布局文件 

## 4.1 转场动画
SplashActivity 中的logoView控件会在5.0 以上的机器执行Transition动画 跳转至LoginActivity

[Transition 动画官方文档](https://developer.android.com/training/transitions/start-activity)

[Transition 动画](https://www.jianshu.com/p/69d48f313dc4)

[Transition 动画2](https://www.jianshu.com/p/01280c2e3443)

[Github-Sample](https://github.com/lgvalle/Material-Animations)

[Android 官方文档- 样式和主题](https://developer.android.com/guide/topics/ui/themes.html?hl=zh-cn)

# 5. LoginActivity

继承自AuthActivity,进一步扩展了登录界面的逻辑，作为登录的基类，是一个抽象类

1. 屏蔽返回键,保证Transition动画完整播放

2. 实现了forwardToMainActivity()方法，提供了跳转至MainActivity的具体逻辑

3. 重写了基类中EventBus的处理逻辑，判断消息类型&& 消息中携带的页面信息，仅关闭当前页面

4. 提供伴生对象，对象中提供了打开当前LoginActivity的方法(有俩个重载，一个带Transition动画)

5. 定义了表示当前是否处于Transition状态的属性

## 5.1 OpenSourceLoginActivity

提供了具体的页面


1. **页面使用了一个自定义的LinearLayout布局，重写了回调Layout()方法，用于在键盘弹出，布局发生变化时，动态的修改某个控件的weight ,使得页面可以完全展示**

	- [View.post()可能的问题](https://www.cnblogs.com/plokmju/p/7481727.html)

	- 这里有一个知识点:android:visibility 是 真实的可见度，tools:visibility 是预览的可见度 .具体分析可以查看[android中xml tools属性详解](https://blog.csdn.net/u012792686/article/details/79218208)


# 6. MainActivity 

1. 数据库使用的是[LitePal](https://github.com/LitePalFramework/LitePal)

2. CoordinatorLayout + AppBarLayout (Toolbar + TabLayout) + ViewPager

	[CoordinatorLayout介绍](https://blog.csdn.net/huachao1001/article/details/51554608)

	[CoordinatorLayout 子 View 之间的依赖管理机制 —— 有向无环图](https://blog.csdn.net/a153614131/article/details/53750329)
	
	[AppbarLayout最详细使用说明](https://www.jianshu.com/p/94ceeb8bbf87)
	
	[AppBarLayout scroolFlags属性详解](https://zhuanlan.zhihu.com/p/47014848)
	
	[Material Design 组件集合!](https://juejin.im/post/5ad84f38f265da50412ebc75)
	
3. 重写了onOptionsItemSelected()方法，将home按钮修改为打开抽屉,并添加了search 按钮

	search按钮添加了一个Transition动画 连接至SearchActivity


# 7. SearchActivity

1. 设置SearchView

2. 设置RecyclerView

3. 设置Transition动画

4. 兼容SearchView <6.0时



# AlbumFragment

1. 重写onCreateView 提供布局

2. 重写onActivityCreated 加载图片

3. 提供了加载图片的方法，通过contentProvider 获取 外部存储内容

4. 提供了加载完毕的回调逻辑,用来根据加载图片的结果来显示不同的内容(loadComplete)

5. 提供了一个根据屏幕宽度以及RecyclerView列数量,计算图片应该展示的大小


## GifAlbumFragment

1. onActivityCreated时 通知关联的Activity,当前展示的Fragment就是自己, 并改变toolbar提示内容 

	设置contentProvider展示的内容类型
	
	创建 RecyclerView的adapter

2. 重写加载完毕的页面，额外添加首次打开时的加载完毕提示
