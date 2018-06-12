# Android 仓库

[如何上传不开源的jar包至maven仓库](https://zhuanlan.zhihu.com/p/35373395)

[如何使用Android Studio把自己的Android library分享到jCenter和Maven Central](http://www.jcodecraeer.com/plus/view.php?aid=3097)

[使用Gradle上传aar到mavenCentral详细教程](http://blog.liangruijun.com/2015/06/05/%E4%BD%BF%E7%94%A8Gradle%E4%B8%8A%E4%BC%A0aar%E5%88%B0mavenCentral%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/)

[如何上传 Library 到 Maven 仓库](http://git.bookislife.com/post/2015/how-to-upload-library-to-maven-central/)

# 1. 简介

Gradle支持`google`,`jcenter`,`mavenCentral`等仓库.AS3.0+在项目创建时会默认添加`google`和`jcenter`仓库支持.除了**标准的服务器之外,还可以自定义Maven仓库服务器**

`Android Studio`在项目根目录下的`build.gradle`文件中声明仓库,然后会去对应的仓库地址下载library

**注：不管是Jcenter还是Maven Central ，两者都是Maven仓库.但是维护在不同的服务器上,由不同的人提供内容,俩者之间没有联系**

# 2. jcenter

Jcenter是由JFrog公司提供的Bintray中的Java仓库

仓库的地址是`https://jcenter.bintray.com/`

# 3. mavenCentral

MavenCentral是由Sonatype公司维护的,是Apache Maven,SBT和其他构件系统的默认仓库.

仓库名称是`MavenRepo`,仓库的地址是`https://repo1.maven.org/maven2/` .`mavenCentral`已经将内容浏览功能禁止,可以在`http://search.maven.org/`查询Library的相关信息


# 4. 使用MavneCentral 上传jar

`MavenCentral` 提供了一种方式 可以不上传`sources.jar`和`javadoc.jar`

>If, for some reason (for example, license issue or it's a Scala project), you can not provide -sources.jar or -javadoc.jar , please make fake -sources.jar or -javadoc.jar with simple README inside to pass the checking. We do not want to disable the rules because some people tend to skip it if they have an option and we want to keep the quality of the user experience as high as possible.

- **如果因为证书问题或项目为Scala,可以不提供`source.jar`和`javadoc.jar`.但是需要生成一个假的包含README的源码包或文档包,用来通过检查**

- [Supply Javadoc and Sources](https://central.sonatype.org/pages/requirements.html)


**大概流程是:**

- 生成Jar包并签名后 和其他资源一起打成一个`bundle.jar`,然后上传这个`bundle.jar`来发布

- [这篇文档详细说明了如何上传不开源的jar](https://central.sonatype.org/pages/manual-staging-bundle-creation-and-deployment.html)

# 4.1 具体操作流程

