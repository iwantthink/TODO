# Activity生命周期管理

[Android 插件化原理解析——Activity生命周期管理](http://weishu.me/2016/03/21/understand-plugin-framework-activity-management/)

**参考`[动态加载.md]`和`[特殊的启动Activity方式.md]`**

# 1. 简介

在插件化中,如果仅仅是动态加载一个类,那么通过`ClassLoader`(类加载器)即可实现.但是仅仅将类加载进来对四大组件来说是没有用的,**Adnroid的四大组件拥有一个概念:生命周期**.所以在加载完类之后,还需要赋予其生命周期.


# 2. 插件的生命周期
**关于插件类的生命周期 有几种解决方案,参考`[动态加载.md]`**

**这里主要介绍 动态创建Activity方式**

## 2.1 跟生命周期有关的限制

**这一点可以参考`[特殊的启动Activity方式.md]`**

**简而言之,如果启动一个不在`AndroidManifest.xml`中声明过的组件，会抛出异常信息**.例如启动一个Activity:

	android.content.ActivityNotFoundException: Unable to find explicit activity class {xx.xx.xx.MainActivity}; have you declared this activity in your AndroidManifest.xml?

## 2.2 如何绕过这个限制?

**要解决这个限制,就必须了解Activity的启动流程,那么在了解了Activity启动流程之后,需要知道以下几点:**

- 关于Activity,Application 需要知道,它们其实也只是普通的Java对象,也是需要被构造函数创建出来的

- 应用一直在与系统服务进行通信,并且将一些操作交给Android Framewroks实现,例如 Android 将诸如 生命周期管理,Activity堆栈 等 操作都交给AMS去实现. 

	这样实际上是为了简化应用层的开发,不用再去关心这些复杂的事情

**简单的Activity启动过程示意图:**

![](http://7xp3xc.com1.z0.glb.clouddn.com/201601/1458532084072.png)

**解决方案:**

- **通过一个合法的Intent 来包裹一个 未注册的Activity启动请求，先通过AMS的检查，然后在ActivityThread的某个地方将系统传递过来的Intent给替换掉**

## 2.3 具体流程

**统一名称:注册过的Activity 称为`fake A`,未注册的称为 `real A`**

1. 首先在清单文件中 注册 `fake A`,通过它来通过AMS对Activity的检查.然后调用`startActivity()`启动 `real A`

2. Hook掉`startActivity()`方法,在该方法中,创建启动`fake A`de Intent(并将`real A`的Intent 放到`fake A`的Intent中 )

	这样,AMS检查`fake A`,发现其是合法的,就会通知`ActivityThread`去具体的执行启动流程

3. 在通过了AMS检查之后,通过Hook`ActivityThread.mH`将`real A`的Intent 从 `fake A`中取出,替换掉`fake A`的Intent,并继续启动操作.

## 2.4 插件中的Activity为什么能收到回调?

1. 了解Acitvity启动流程,应该知道 App和系统服务 是不处于一个进程的,一个位于App进程,另外一个位于system_server进程.

2. Android采用Binder机制 来进行进程间通信,ActivityThread和AMS之间通信是通过**Binder代理对象**,ActivityThread提供了`IApplicationThread`,AMS提供了`IActivityManager`

	- ActivityThread通过`IActivityManager`调用AMS,AMS通过`IApplicationThread`调用ActivityThread.

	- ActivityThread可以通过ServiceManager获取到AMS的Binder代理

	- AMS获取`IApplicationThread`是通过: ActivityThread在调用AMS时,会将代表自己的Binder当做参数传过去

3. **实际上,AMS是在操作`fake A`.只是在ActivityThread这里,将对应操作的执行对象 改成了`real A`**

	- 这是因为,在`ActivityThread`中有一个代表IBinder和Activity关系的类,叫做`mActivities`.由于Hook了mH替换了Intent,所以在`mActivities`中IBinder对应的是未注册的Activity.

			final ArrayMap<IBinder, ActivityClientRecord> mActivities = new ArrayMap<>();


# 3. 局限

如果一直`startActivity()`而不去`finish()`,同时每启动一个未注册的Activity 就需要一个 已经注册的Activity,那么清单文件就得声明无限个用来替换的Activity. 但是实际上这种情况比较少出现,因为一个app 如果一直重复打开N个 而不关闭 是不正常的.