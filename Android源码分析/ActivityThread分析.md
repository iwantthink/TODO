# ActivityThread分析
[Android主线程(ActivityThread)源代码分析](https://blog.csdn.net/shifuhetudi/article/details/52089562)

[ActivityThread源码](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app)

# 1. 简介

android应用程序作为控制类程序，跟Java程序类似，都有一个入口，Java程序的入口是main()函数，**而Adnroid程序的入口是ActivityThread 的main()方法**

ActivityThread主要的作用是 根据AMS(ActivityManagerService)的要求，通过IApplicationThread的接口来负责调用和执行activities,broadcasts和其他操作。

在Android系统中，四大组件默认都是运行在主线程中