# Android图形系统.md

[Android图形系统概述](http://gityuan.com/2017/02/05/graphic_arch/)

# 1. 简介

Android系统的图形系统包括`WindowManager`,`SurfaceFlinger`,`Open GL`,`GPU`等模块.其中`SurfaceFlinger`作为负责绘制应用UI的核心，从名字可以看出其功能是将所有Surface合成工作。 不论使用什么渲染API, 所有的东西最终都是渲染到`surface`. `surface`代表`BufferQueue`的生产者端, 并且 由`SurfaceFlinger`所消费, 这便是基本的生产者-消费者模式. Android平台所创建的`Window`都由`surface`所支持, 所有可见的`surface`渲染到显示设备都是通过`SurfaceFlinger`来完成的.