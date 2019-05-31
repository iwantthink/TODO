# so文件处理



# 1. ABI是什么




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


# 6. 如何查看进程是被哪个zygote创建

Android系统从5.0开始支持64bit CPU，于是系统就有了zygote和zygote64两个进程来分别创建32和64位的应用进程

- 简单验证一下：使用`$adb shell ps | grep zygote `能看到两个zygote和zygote64两个进程。再使用`$adb shell ps | grep <package_name>`就能找到应用进程ID和父进程ID

