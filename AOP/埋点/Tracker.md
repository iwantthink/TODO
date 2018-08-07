# Tracker

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
