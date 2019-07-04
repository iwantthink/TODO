# 1. 使用自定义Task进行混淆

在使用自定义Task 进行混淆时, 最好添加`--info` 将warning 都处理掉

不要直接将一个未混淆的Jar 放到另外一个库中一起打包混淆...处理warning会发狂的!!!


# 2. 无法下载库的问题

Android Studio 在下载库时报出异常`org.gradle.api.resources.ResourceException: Could not get resource`

**解决办法:**

Android Studio -> Preferences -> Build,Execution,Deployment -> Gradle -> Android Studio 

打开Enable embedded Maven repository

![](https://picture-pool.oss-cn-beijing.aliyuncs.com/1527781034399981.png)

