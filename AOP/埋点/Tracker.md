# Tracker

[](https://www.jianshu.com/p/8459a75ce5ca)

# 1. Feature

## 1.1 自动获取当前项目包名

解析`AndroidManifest.xml`

    private static String getAppPackageName() {
        String packageName
        try {
            def manifestFile = android.sourceSets.main.manifest.srcFile
            packageName = new XmlParser().parse(manifestFile).attribute('package')
        } catch (Exception e) {
        }
        return packageName
    }

- 主要是`XmlParser` 的使用

## 1.2 支持Jar包的进行AOP操作


## 1.3 android.jar包的位置

[打通Android Gradle编译过程的任督二脉](http://www.10tiao.com/html/223/201605/2651231835/1.html)

通过`AppExtension`的`getBootClassPath()`获取到


## 1.4 筛选规则

以包名为主要过滤规则, 类路径中包含包名的  即符合要求

可以单独传入 类名,用来 `exclude`


## 1.5 ViewPath生成规则

`ViewPath`: 类名+`ResourceID`+所属父组件的Index 等特征信息组成

`ViewPath` 需要体现View 在控件树中的全路径,其形式可以如下:

	parent1[index]#id/parent2[index]#id/.../view[index]#id

- `parent1`:表示父类控件的类名

- `[index]`:表示子类控件在其直接父类控件中的序列,**暂时可以将index固定为0,因为只要有`#id`就可以定位控件**

- `#id`:表示布局文件中`android:id`属性值

![](http://nos.netease.com/knowledge/eceb7098-e3d9-48fb-9599-18cb5e38a7b4)




## 1.6 ViewID的生成规则

通过AOP操作实现的布码,在收集数据时 需要赋予View一个独立的ID,以此来区分其他的View

- 这个ViewID 需要具有一致性,即同一个View 无论界面布局如何动态变化,或者多次进入,此ViewID 保持不变


`View`控件的特征信息:

1. `Resource ID Name`: 开发者手动为控件设置的唯一标识.一般在布局文件中通过指定`android:id`属性来设置

2. `Resource ID`: 开发者在为控件设置了唯一标识后,系统会自动生成`包名.R`文件,`Resource ID`就是对应的`Resource ID name`.开发者通过`View.getID()`会获取到这个值,可以借助R文件 找到对应的`Resource ID Name`.这个值可能是会变化的,建议使用`Resource ID Name`作为标识符

	    public static final int btn_test=0x7f070025;

3. `ClassName`:`View`控件的类型


**对于存在ID的View来说,`ResourceId`已经能够作为标识符,但是有一些View并不存在`ResoucceID`,那么可以考虑使用ViewPath 来作为唯一标识**

	md5(PageName+ViewPath)


## 1.7 添加Transform的消耗时间



# 2. Develop

1. 实现AOP埋点

	控件的埋点

	Fragment的埋点

2. 实现埋点代码的具体逻辑

	获取viewpath

3. 实现在线下发配置,通过配置控制收集的页面信息