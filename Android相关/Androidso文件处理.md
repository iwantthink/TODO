# so文件处理



# 1. ABI是什么

[ABI 管理](https://developer.android.com/ndk/guides/abis)

ABI 是 Application Binary Interface 的缩写。

不同 Android 手机使用不同的 CPU，因此支持不同的指令集。CPU 与指令集的每种组合都有其自己的应用二进制界面（或 ABI）。 ABI 可以非常精确地定义应用的机器代码在运行时如何与系统交互。 您必须为应用要使用的每个 CPU 架构指定 ABI。




# 2. 如何判断当前是32/64位进程

[判断当前进程运行时环境(32/64位)的方法](https://www.jianshu.com/p/eb51edb1ab81)




# 3. 如何判断so文件是32位的还是64位

通过`file`命令

	ryandeMacBook-Pro:arm64-v8a ryan$ file libnative-lib.so 
	libnative-lib.so: ELF 64-bit LSB pie executable ARM aarch64, version 1 (SYSV), dynamically linked, BuildID[sha1]=a42cb174133d3dff314921967e3fd8cf0c267694, with debug_info, not stripped

# 4. AS项目中so文件的放置位置

有俩种方法可以往项目中添加so文件

1.   拷贝CPU架构对应的so文件文件夹到app/libs目录下，如图：

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3kk5g1s37j20i6091my5.jpg)
	

	并在模块中的`build.gradle`文件中往`android`块中配置`sourceSets`
	
		sourceSets {
	           main {
	               jniLibs.srcDir 'libs'
	           }
	    }

2. 拷贝CPU架构对应的so文件文件夹到`src/main/jniLibs`目录

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1g3kk6zq32yj20hx0c70u0.jpg)



# 5. Android 动态链接库加载原理
[Android 的 so 文件加载机制](https://www.jianshu.com/p/f243117766f1)

[Android 动态链接库加载原理及 HotFix 方案介绍](https://juejin.im/entry/57c8e6b52e958a0068cc0c86)	

[Android中so使用知识和问题总结以及插件开发过程中加载so的方案解析](http://www.520monkey.com/archives/849)

[动态加载so注意事项&案例](https://www.jianshu.com/p/a06e6f0f402a)

[为何 Twitter 区别于微信、淘宝，只使用了 armeabi-v7a?](https://www.diycode.cc/topics/691)

# 6. 如何查看进程是被哪个zygote创建

Android系统从5.0开始支持64bit CPU，于是系统就有了zygote和zygote64两个进程来分别创建32和64位的应用进程

- 简单验证一下：使用`$adb shell ps | grep zygote `能看到两个zygote和zygote64两个进程。再使用`$adb shell ps | grep <package_name>`就能找到应用进程ID和父进程ID


# 7. 常见错误

[如果项目只包含了 armeabi，那么在所有Android设备都可以运行](https://blog.csdn.net/xiaxiayige/article/details/68925669)


[arm64-v8a是可以向下兼容的，但前提是你的项目里面没有arm64-v8a的文件夹，如果你有两个文件夹armeabi和arm64-v8a，两个文件夹，armeabi里面有a.so 和 b.so,arm64-v8a里面只有a.so，那么arm64-v8a的手机在用到b的时候发现有arm64-v8a的文件夹，发现里面没有b.so，就报错了，所以这个时候删掉arm64-v8a文件夹，这个时候手机发现没有适配arm64-v8a，就会直接去找armeabi的so库，所以要么你别加arm64-v8a,要么armeabi里面有的so库，arm64-v8a里面也必须有
](https://blog.csdn.net/zjws23786/article/details/79550231)