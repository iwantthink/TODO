# Android输入系统
[深入理解Android 卷iii PDF版]()

# 1. 简介

Android输入系统的工作原理概括来说,就是监控`/dev/input/`下的所有设备节点,当某个节点有数据可读时,将数据读取出来并进行一系列的翻译加工,然后在所有的窗口中寻找合适的事件接收者,并派发

# 2. getevent与sendevent工具

Android系统提供了`getevent`和`sendevent`俩个工具供开发者从设备节点中直接读取输入事件或写入事件

## 2.1 getevent

`getevent`监听输入设备节点的内容,当输入事件被写入节点时,`getevent`会将其读出并打印在屏幕上,其不会对事件数据做任何加工,直接输出内核提供的最原始的事件

	adb shell getevent[-选项] [device_path]

- `device_path`:可选参数

	指明需要监听的设备节点路径.如省略此参数,则监听所有设备节点的事件 

电源键按下和抬起实例:

	[369.462809] /dev/input/event1: 0001 0074 00000001
	[369.646259] /dev/input/event1: 0001 0074 00000000

- **输出内容是十六进制的**

- 每条数据有5项信息:

	1. 产生事件的时间戳,`[369.462809]`
	2. 产生事件的设备节点,`/dev/input/event1`
	3. 事件类型,`0001`
	4. 事件代码,`0074`
	5. 事件的值,`00000001`

- 时间戳,类型,代码,值 是原始事件的4项基本元素,除时间戳外,其他元素的实际意义依照设备类型以及厂商的不同而有所不同

- 这俩条原始数据会被输入系统包装成俩个`KeyEvent`对象,作为俩个按键事件派发给`Framework`中感兴趣的模块或应用程序

## 2.2 sendevent

输入设备的节点不仅在用户空间可读,而且是可写的,因此可以将原始事件写入节点中,从而实现模拟用户输入的功能

	adb shell sendevent [节点路径] [类型] [代码] [值]


- 注意将参数转成十进制使用

## 2.3 模拟按键

send event无法使用，可以用`adb shell input keyevent  xxx`代替

其中xxx 代表Key code，参考：`/frameworks/base/core/java/android/view/KeyEvent.java`

下面是`input keyevent`几个比较常用的用法：

	input keyevent 3    // Home
	
	input keyevent 4    // Back
	
	input keyevent 19  //Up
	
	input keyevent 20  //Down
	
	input keyevent 21  //Left
	
	input keyevent 22  //Right
	
	input keyevent 23  //Select/Ok
	
	input keyevent 24  //Volume+
	
	input keyevent 25  // Volume-
	
	input keyevent 82  // Menu 菜单
	
	input keyevent 7 // 数字0
	
	input keyevent 8  // 数字1
	
	input keyevent 26   // Power key
	
	input keyevent 27   // Camera




# 3. 输入系统简介

输入事件的源头位于`/dev/input/`下的设备节点,而输入系统的终点是由WMS管理的某个窗口

**最初的输入事件为内核生成的原始事件**,而最终交给窗口的是`KeyEvent`或`MotionEvent`对象.因此Android系统的输入系统主要工作是读取设备节点中的原始事件,将其加工封装,然后派发给一个特定的窗口以及窗口中的控件. 这个过程由`InputManagerService`系统服务为核心的多个参与者共同完成

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fx6ac76aqnj20p10730uv.jpg)

- 简单来说,Linux内核将原始事件写入设备节点中,`InputReader`不断通过`EventHub`将原始事件取出并翻译加工成Android输入事件,然后交给`InputDispatcher`.`InputDispatcher`根据WMS提供的窗口信息将事件交给合适的窗口.窗口的`ViewRootImpl`对象再沿着控件树将事件派发给感兴趣的控件. 控件对其接收到的事件作出响应,更新自己的画面,执行特定的动作