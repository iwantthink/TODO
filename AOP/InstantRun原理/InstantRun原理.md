# InstantRun
[Instant Run工作原理及用法](https://www.jianshu.com/p/2e23ba9ff14b)

[instant-run-how-does-it-work](https://medium.com/google-developers/instant-run-how-does-it-work-294a1633367f)



[Instant Run 谈Android替换Application和动态加载机制](http://w4lle.com/2016/05/02/%E4%BB%8EInstant%20run%E8%B0%88Android%E6%9B%BF%E6%8D%A2Application%E5%92%8C%E5%8A%A8%E6%80%81%E5%8A%A0%E8%BD%BD%E6%9C%BA%E5%88%B6/)

[Instant Run 原理以及源码分析 旧版本](https://www.jianshu.com/p/780eb85260b3)

[Instant Run 浅析 旧版本](http://jiajixin.cn/2015/11/25/instant-run/)

[Instant Run 原理以及源码分析 新版本](https://www.jianshu.com/p/5947855e3362)

[Instant Run源码地址](https://android.googlesource.com/platform/tools/base/+/gradle_3.0.0/instant-run/)

# 1.介绍

`InstantRun`是Android Studio 2.0新增的一个运行机制，可以减少开发时对应用的构建和部署的时间。

`InstantRun`在第一次运行时，和往常一样。但是接下来的每次运行，都会大幅度减少消耗的时间


# 2. 构建周期

**完整的构建和部署流程：**


流程： 构建->部署->安装->app登录->activity创建。

`Instant Run` 目标是 尽可能多的提出不必要的步骤，然后提升必要步骤的速度。 这就需要以下几点：

- 只对代码改变部分做构建和部署

- 不重新安装应用

- 不重启应用

- 不重启Activity


# 3.热插拔，温插拔，冷插拔

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fynmxyh7n4j20l708kwgu.jpg)

**`Instant Run`等同于 增量构建+热 或 温 或 冷插拔**

- 热插拔：代码改变被应用，投射到APP，不需要重启应用，不需要重建当前activity

- 温插拔：activity 需要被重启才能看到所需更改

	场景：典型的情况是 代码修改涉及到了资源文件，即Resources

- 冷插拔：app需要被重启(但是仍然不需要重新安装)

	场景：任何涉及结构性变化的，比如：修改了继承规则，修改了方法签名等

## 3.1 热插拔


Android Studio monitors： 运行着Gradle任务来生成增量`.dex`文件（这个dex文件是对应着开发中的修改类） Android Studio会提取这些`.dex`文件发送到`App Server`(通过socket)，交给自定义的类加载去加载.dex文件

**加载的原理：**

`App Server`会不断监听是否需要重写类文件，如果需要，任务会被立马执行。新的更改便能立即被响应。可以通过打断点调试来发现它确实是这么做


## 3.2 温插拔

温拔插需要重启Activity，因为资源文件是在Activity创建时加载，所以必须重启Activity来重载资源文件。
目前来说，任何资源文件的修改都会导致重新打包再发送到APP。但是，google的开发团队正在致力于开发一个增量包，这个增量包只会包装修改过的资源文件并能部署到当前APP上。

> 注意：温拔插涉及到的资源文件修改，在manifest上是无效的（这里的无效是指不会启动Instant Run），因为，manifest的值是在APK安装的时候被读取，所以想要manifest下资源的修改生效，还需要触发一个完整的应用构建和部署。总结起来：如果你修改了manifest相关的资源文件，还是需要面临和以前一样的龟速构建。

**所以温拔插实际上只能应对少数的情况，它并不能应付应用在架构、结构上的变化。例如：annotations，fields的增删改、父类文件的修改、static修饰的类、方法、常量等的修改都只能依靠冷拔插。**


## 3.3 冷插拔

应用部署的时候，会把工程拆分成十个部分，每部分都拥有自己的.dex文件，然后所有的类会根据包名被分配给相应的.dex文件。当冷拔插开启时，修改过的类所对应的.dex文件，会重组生成新的.dex文件，然后再部署到设备上。
之所以能这么做，是依赖于Android的ART模式，它能允许加载多个.dex文件。ART模式在android4.4（API-19）中加入，但是Dalvik依然是首选，到了android5.0（API-21），ART模式才成为系统默认首选，所以Instant Run只能运行在API-21及其以上版本，至于低版本的话，会重新构建整个应用（下文会提及低版本解决思路）


# 4. Instant Run运行原理
**Android普通构建流程：**

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fynn7fy4t9j20jx0cigmi.jpg)

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fynn2ogmcfj20lc09vwgd.jpg)

manifest文件合并、打包，和res一起被AAPT合并到APK中，同样项目代码被编译成字节码，然后转换成.dex 文件，也被合并到APK中。


**Instant Run编译和部署流程:**

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fynmxyh7n4j20l708kwgu.jpg)

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fynn8q42wzj20k60cxwg4.jpg)

1. 在有Instant Run的环境下：一个新的`App Server`类会被注入到App中，与Bytecode instrumentation协同监控代码的变化

2. ~~同时会生成一个新的Application类，它注入了一个自定义类加载器,同时确保该Application类会启动我们所需的新注入的`App Server`(长连接，socket)。Manifest会被修改来确保应用能使用这个新的Application类(如果已经存在Application，Instant Run创建的这个Application类会代理已存在的Application)~~

3. Instant Run 成功运行，下次使用时，会通过决策，合理运用冷温热插拔来协助缩短构建和部署时间


# 5.Instant Run 实现细节

Instant Run 由一个插件和一个库文件组成(gradle plugin + instant-run.jar)

## 5.1 低版本

- 参考[Instant Run 浅析 旧版本 ](http://jiajixin.cn/2015/11/25/instant-run/),[Instant Run 原理以及源码分析 旧版本](https://www.jianshu.com/p/780eb85260b3)

## 5.2 高版本 3.0.0

### 5.2.1 程序如何运行：

`Instant Run`将app 拆分成俩部分，分离了 业务代码。另外`instant run`在安装时，通过查看AS的**RUN窗口**，可以发现安装命令变成如下：
	
	$ adb install-multiple -r -t -p com.luck.ryan.hotfix E:\github\HOT_FIX\app\build\intermediates\split-apk\debug\slices\slice_1.apk E:\github\HOT_FIX\app\build\outputs\apk\debug\app-debug.apk 
	
同时，通过root过后的手机，进入`data/app/com.luck.ryan/`目录下，会发现存在多个.apk文件

- `slice_1.apk`包含的是业务代码(即需要`instant run`的代码)。`app-debug.apk`包含的是支持Instant Run 的代码和一些资源文件清单文件等。

- `slice_1.apk`可以在`app\build\intermediates\split-apk\debug\slices`路径下找到，其中的代码可以在`app\build\intermediates\transforms\instantRun\debug\0`中找到，代码会被修改成如下形式：

	   public MainActivity() {
	        IncrementalChange var1 = $change;
	        if(var1 != null) {
	            Object[] var10001 = (Object[])var1.access$dispatch("init$args.([Lcom/ryan/hotfix/MainActivity;[Ljava/lang/Object;)Ljava/lang/Object;", new Object[]{null, new Object[0]});
	            Object[] var2 = (Object[])var10001[0];
	            this(var10001, (InstantReloadException)null);
	            var2[0] = this;
	            var1.access$dispatch("init$body.(Lcom/ryan/hotfix/MainActivity;[Ljava/lang/Object;)V", var2);
	        } else {
	            super();
	        }
	    }
	
	    public void onCreate(Bundle savedInstanceState) {
	        IncrementalChange var2 = $change;
	        if(var2 != null) {
	            var2.access$dispatch("onCreate.(Landroid/os/Bundle;)V", new Object[]{this, savedInstanceState});
	        } else {
	            super.onCreate(savedInstanceState);
	            this.setContentView(2131296283);
	            Log.d("MainActivity", "msg = " + (new HelloJava()).say());
	            this.getName("abcdeg");
	        }
	    }
	
	    public void getName(String name) {
	        IncrementalChange var2 = $change;
	        if(var2 != null) {
	            var2.access$dispatch("getName.(Ljava/lang/String;)V", new Object[]{this, name});
	        } else {
	            Log.d("MainActivity", name);
	        }
	    }


### 5.2.2 程序和AS如何通信

（[Instant Run源码地址](https://android.googlesource.com/platform/tools/base/+/gradle_3.0.0/instant-run/)）：

俩者通过`Server` 和ServiceCommunicator俩个类进行通信。应用安装后先启动ContentProvider(`InstantRunContentProvider`),然后在Provider中创建Server实例，并启动，开启了socket等待AS连接。接着协商协议版本，读取消息头等后续操作

> Content Provider that abuses a quirk of early Android initialization to start the instant run service very early, before Application.onCreate(): content providers get initialized before Application.onCreate() is called.
> 之所以使用ContentProvider 是为了解决了使用额外Service可能导致的ANR

- 客户端(AS)

	修改业务代码时，通过点击AS的Instant Run 按键， 会生成一个Dex文件，包含 被修改的类和一个`AppPatchesLoaderImpl`

	- 被修改的类，即补丁类：

			public class MainActivity$override implements IncrementalChange {
			    public MainActivity$override() {
			    }
			
			    public static Object init$args(MainActivity[] var0, Object[] var1) {
			        Object[] var2 = new Object[]{new Object[]{var0, new Object[0]}, "android/app/Activity.()V"};
			        return var2;
			    }
			
			    public static void init$body(MainActivity $this, Object[] var1) {
			    }
			
			    public static void onCreate(MainActivity $this, Bundle savedInstanceState) {
			        Object[] var2 = new Object[]{savedInstanceState};
			        MainActivity.access$super($this, "onCreate.(Landroid/os/Bundle;)V", var2);
			        $this.setContentView(2131296283);
			        Log.d("MainActivity", "msg = " + (new HelloJava()).say());
			        $this.getName("abcdeg");
			    }
			
			    public static void getName(MainActivity $this, String name) {
			        Log.d("MainActivity", name);
			    }
			
			    public Object access$dispatch(String var1, Object... var2) {
			        switch(var1.hashCode()) {
			        case -1443662126:
			            getName((MainActivity)var2[0], (String)var2[1]);
			            return null;
			        case -1359307449:
			            init$body((MainActivity)var2[0], (Object[])var2[1]);
			            return null;
			        case -641568046:
			            onCreate((MainActivity)var2[0], (Bundle)var2[1]);
			            return null;
			        case -639852501:
			            return init$args((MainActivity[])var2[0], (Object[])var2[1]);
			        default:
			            throw new InstantReloadException(String.format("String switch could not find '%s' with hashcode %s in %s", new Object[]{var1, Integer.valueOf(var1.hashCode()), "com/ryan/hotfix/MainActivity"}));
			        }
			    }
			}

	- `AppPatchesLoaderImpl`类中包含被修改类的信息

			public class AppPatchesLoaderImpl extends AbstractPatchesLoaderImpl {
			    public static final long BUILD_ID = 1520927787779L;
			
			    public AppPatchesLoaderImpl() {
			    }
			
			    public String[] getPatchedClasses() {
			        return new String[]{"com.ryan.hotfix.MainActivity"};
			    }
			}

- 服务端(APP)

	AS通过SOCKET告知APP，然后APP的Server类进行处理。AS会向APP 发送信息 标志APP 需要加载新的PATCHES，源码中的**`case MESSAGE_PATCHES`**，会调用`handleHotSwapPatch()`方法进行处理

	`handleHotSwapPatch()`方法主要 会将AS生成的dex文件通过DexClassLoader加载进来，然后通过反射创建`AppPatchesLoaderImpl`实例，调用其`getPatchedClasses()`方法，获取哪些代码发生了变化的列表，然后会调用其`load()`方法，循环所读取到的发生了变换的列表，并做如下事情：

	1. 通过反射创建被修改的类(dex中的类，不是原始的那个类)

	2. 再用 `ClassLoader` 把 修改过的类 load 进来，由于在最开始 gradle 编译 app-debug.apk 时，就使用 asm 等字节码操作工具给每个类都生成了一个 $change 静态成员，同时在每个方法的开头都插入了逻辑，判断 $change 是否为空，为空则走正常逻辑，否则走修复后的逻辑；这里通过反射直接把 MainActivity$override 实例赋值给了 $change 成员；同时，如果以前已经热部署过了一次或者多次，会把 $change 成员的 $change 字段置为 true，表明之前的过期了。

	接着会调用`restart()`方法，然后最终调用到`Restarter.restartActivityOnUiThread()`方法

	`restartActivityOnUiThread（）`方法最终会去调用`activity.restart()`方法。**到这里 类会重新加载，并且此时`$change`成员不为空，且指向了被修复的类。**

	