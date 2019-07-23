# 1. 屏幕相关
[Android 屏幕适配：最全面的解决方案](https://www.jianshu.com/p/ec5a1a30694b)

# 1.1 概念

**屏幕尺寸**:

- 手机对角线的物理尺寸(单位：英寸 , inch = 2.54cm)

**屏幕分辨率**：

- 手机横向和纵向的像素点数总和(单位:pixel,pixel = 1像素点)

- 分辨率通常描述为 屏幕宽*屏幕高

**屏幕像素密度**：

- 每英寸的像素点数(单位:dpi,dots per inch)

	假设设备内每英寸有160个像素，那么该设备的dpi = 160
	
- 每种Android手机屏幕大小都有一个相应的屏幕像素密度

	密度类型|代表的分辨率（px）|屏幕像素密度（dpi）
	---|---|---
	低密度（ldpi）|240x320|120
	中密度（mdpi）|320x480|160
	高密度（hdpi）|480x800|240
	超高密度（xhdpi）|720x1280|320
	超超高密度（xxhdpi）|1080x1920|480

## 1.2 屏幕尺寸,分辨率和dpi关系

![](https://upload-images.jianshu.io/upload_images/944365-2b5dc928ab334440.png?imageMogr2/auto-orient/)

举一个实际的例子，屏幕分辨率为 1080px*1920px,屏幕尺寸为5英寸，dpi为多少?

![](https://upload-images.jianshu.io/upload_images/944365-5f2509be9276460c.png?imageMogr2/auto-orient/)

## 1.3 密度无关像素(dp/dip)

密度无关像素：

- `density-independent pixel`,与实际物理像素点无关，用来保证在不同屏幕像素密度的设备上拥有相同的显示效果

- 单位:dp

- 例如在俩个屏幕分辨率分别为`480*800` 和`320*480` 的手机上，要画一条长度是屏幕宽一半的线，如果使用px作为计量单位，前者中线的长度应该是240px,后者中线的长度应该是160px.而如果使用dp作为计量单位，统一使用160dp就可以

## 1.4 dp和px进行转换

**在Android中，规定以160dpi（即屏幕分辨率为320x480）为基准：1dp=1px**

密度类型|代表的分辨率（px）|屏幕密度（dpi）|换算（px/dp）|比例
---|---|---|---|---
低密度（ldpi）|240x320|120|1dp=0.75px|3
中密度（mdpi）|320x480|160|1dp=1px|4
高密度（hdpi）|480x800|240|1dp=1.5px|6
超高密度（xhdpi）|720x1280|320|1dp=2px|8
超超高密度（xxhdpi）|1080x1920|480|1dp=3px|12


## 1.5 独立比例像素

独立比例像素:

- scale-independent pixel,简称sp或者sip

- Android推荐使用该单位设置文字大小



# 2. Bitmap
[Android性能优化（五）之细说Bitmap](https://www.jianshu.com/p/e49ec7d053b3)

[Android中Bitmap内存优化](https://www.jianshu.com/p/3f6f6e4f1c88)

## 2.1 如何计算Bitmap占用的内存

不考虑压缩，只是加载一张Bitmap，那么它占用的内存 = width * height * 一个像素所占的内存

说法也对，但是不全对，没有说明场景，同时也忽略了一个影响项：Density


