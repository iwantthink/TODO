# InstantRun
[Instant Run工作原理及用法](https://www.jianshu.com/p/2e23ba9ff14b)

[instant-run-how-does-it-work](https://medium.com/google-developers/instant-run-how-does-it-work-294a1633367f)

[Instant Run 浅析](http://jiajixin.cn/2015/11/25/instant-run/)

[Instant Run 谈Android替换Application和动态加载机制](http://w4lle.com/2016/05/02/%E4%BB%8EInstant%20run%E8%B0%88Android%E6%9B%BF%E6%8D%A2Application%E5%92%8C%E5%8A%A8%E6%80%81%E5%8A%A0%E8%BD%BD%E6%9C%BA%E5%88%B6/)




# 1.介绍

`InstantRun`是Android Studio 2.0新增的一个运行机制，可以减少开发时对应用的构建和部署的时间。

`InstantRun`在第一次运行时，和往常一样。但是接下来的每次运行，都会大幅度减少消耗的时间


# 2. 构建周期

**完整的构建和部署流程：**

![](https://upload-images.jianshu.io/upload_images/1313748-36d4c846f79411a8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/691)

流程： 构建->部署->安装->app登录->activity创建。

`Instant Run` 目标是 尽可能多的提出不必要的步骤，然后提升必要步骤的速度。 这就需要以下几点：

- 只对代码改变部分做构建和部署

- 不重新安装应用

- 不重启应用

- 不重启Activity


# 3.热插拔，温插拔，冷插拔

![](https://upload-images.jianshu.io/upload_images/1313748-ca1496925395d633.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

**`Instant Run`等同于 增量构建+热 或 温 或 冷插拔**

- 热插拔：代码改变被应用，投射到APP，不需要重启应用，不需要重建当前activity

- 温插拔：activity 需要被重启才能看到所需更改

	场景：典型的情况是 代码修改涉及到了资源文件，即Resources

- 冷插拔：app需要被重启(但是仍然不需要重新安装)

	场景：任何涉及结构性变化的，比如：修改了继承规则，修改了方法签名等

## 3.1 热插拔

![](https://upload-images.jianshu.io/upload_images/1313748-e7c0b89defecdc1e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/417)

Android Studio monitors： 运行着Gradle任务来生成增量.dex文件（这个dex文件是对应着开发中的修改类） Android Studio会提取这些.dex文件发送到`App Server`(socket)，交给自定义的类加载去加载.dex文件

**加载的原理：**

![](https://upload-images.jianshu.io/upload_images/1313748-932358d7cce43515.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/404)

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

![](https://upload-images.jianshu.io/upload_images/1313748-64826a1e2847e169.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/699)

manifest文件合并、打包，和res一起被AAPT合并到APK中，同样项目代码被编译成字节码，然后转换成.dex 文件，也被合并到APK中。


**首次运行Instant Run时 Gradle执行的操作:**

![](https://upload-images.jianshu.io/upload_images/1313748-b77963070354a0b7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/696)

1. 在有Instant Run的环境下：一个新的`App Server`类会被注入到App中，与Bytecode instrumentation协同监控代码的变化

2. 同时会生成一个新的Application类，它注入了一个自定义类加载器,同时确保该Application类会启动我们所需的新注入的`App Server`(长连接，socket)。Manifest会被修改来确保应用能使用这个新的Application类(如果已经存在Application，Instant Run创建的这个Application类会代理已存在的Application)

3. Instant Run 成功运行，下次使用时，会通过决策，合理运用冷温热插拔来协助缩短构建和部署时间


# 5.Instant Run 无法回退

代码更改可以通过热拔插快速部署，但是热拔插会影响应用的初始化，所以我们不得不通过重启应用来响应这些修改。



# 6.Instant Run 实现细节

**动态加载(俩种机制)：**

1. 修改Java代码需要重启应用加载dex，而在Application初始化时替换了Application，新建了一个自定义的DexClassLoader去加载所有的dex文件，称之为**重启更新机制**

2. 修改代码不需要重启，新建一个`ClassLoader`去加载修改部分，称之为**热更新机制**