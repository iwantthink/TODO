# Android 仓库

[如何上传不开源的jar包至maven仓库](https://zhuanlan.zhihu.com/p/35373395)

[如何使用Android Studio把自己的Android library分享到jCenter和Maven Central](http://www.jcodecraeer.com/plus/view.php?aid=3097)

[使用Gradle上传aar到mavenCentral详细教程](http://blog.liangruijun.com/2015/06/05/%E4%BD%BF%E7%94%A8Gradle%E4%B8%8A%E4%BC%A0aar%E5%88%B0mavenCentral%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B/)

[如何上传 Library 到 Maven 仓库](http://git.bookislife.com/post/2015/how-to-upload-library-to-maven-central/)

[MavenCentral官方文档-如何发布realse版本](https://central.sonatype.org/pages/releasing-the-deployment.html)

# 1. 简介

Gradle支持`google`,`jcenter`,`mavenCentral`等仓库.AS3.0+在项目创建时会默认添加`google`和`jcenter`仓库支持.除了**标准的服务器之外,还可以自定义Maven仓库服务器**

`Android Studio`在项目根目录下的`build.gradle`文件中声明仓库,然后会去对应的仓库地址下载library

**注：不管是Jcenter还是Maven Central ，两者都是Maven仓库.但是维护在不同的服务器上,由不同的人提供内容,俩者之间没有联系**

# 2 仓库介绍
## 2.1 jcenter

Jcenter是由JFrog公司提供的Bintray中的Java仓库

仓库的地址是`https://jcenter.bintray.com/`

## 2.2 mavenCentral

MavenCentral是由Sonatype公司维护的,是Apache Maven,SBT和其他构件系统的默认仓库.

仓库名称是`MavenRepo`,仓库的地址是`https://repo1.maven.org/maven2/` .`mavenCentral`已经将内容浏览功能禁止,可以在`http://search.maven.org/`查询Library的相关信息


### 2.2.1. 使用MavneCentral 上传jar

`MavenCentral` 提供了一种方式 可以上传伪造的`sources.jar`和`javadoc.jar`,避免了开源.

>If, for some reason (for example, license issue or it's a Scala project), you can not provide -sources.jar or -javadoc.jar , please make fake -sources.jar or -javadoc.jar with simple README inside to pass the checking. We do not want to disable the rules because some people tend to skip it if they have an option and we want to keep the quality of the user experience as high as possible.

- **如果因为证书问题或项目为Scala,可以不提供`source.jar`和`javadoc.jar`.但是需要生成一个假的包含README的源码包或文档包,用来通过检查**

- [Supply Javadoc and Sources](https://central.sonatype.org/pages/requirements.html)


**大概流程是:**

- 生成Jar包并签名后 和其他资源一起打成一个`bundle.jar`,然后上传这个`bundle.jar`来发布

- [官方文档-详细说明了如何上传不开源的jar/aar](https://central.sonatype.org/pages/manual-staging-bundle-creation-and-deployment.html)

# 3 上传Library具体步骤

## 3.1 创建一个Issue

因为Sonatype 使用JIRA去管理请求..所以这里叫做Issue(可以理解成一个发包请求~)

在进入`Create Issue`窗口后,首先需要选择`Project:Community Support - Open Source Project Repository Hosting (OSSRH)`(还有一些其他的选项 不太清楚具体的用途)

**比较重要的是`Group ID`,`Project URL`,`SCM URL`**

- `Group ID`就是组件的`groupid`,在引用时需要使用到.该值通常为项目对应的域名,为了防止被恶意注册等原因,Sonatype会验证这个`groupID`是否跟你有关联..

- `Already Synced to Central`选项表示 当前内容是否可以同步到线上仓库..

创建完`Issue`,如果没有遇到`GroupID`相关的问题:

	Configuration has been prepared, now you can:
	
	Deploy snapshot artifacts into repository https://oss.sonatype.org/content/repositories/snapshots
	Deploy release artifacts into the staging repository https://oss.sonatype.org/service/local/staging/deploy/maven2
	Promote staged artifacts into repository 'Releases'
	Download snapshot and release artifacts from group https://oss.sonatype.org/content/groups/public
	Download snapshot, release and staged artifacts from staging group https://oss.sonatype.org/content/groups/staging
	please comment on this ticket when you promoted your first release, thanks

如果遇到了如下的问题,修改掉`GroupID`或者证明该域名所有权即可

	Do you own the domain ryan.com? If not, please read:
	http://central.sonatype.org/pages/choosing-your-coordinates.html
	You may also choose a groupId that reflects your project hosting, in this case, something like io.github.iwantthink or com.github.iwantthink

## 3.2 创建JAR包

[Manual Staging Bundle Creation and Deployment](https://central.sonatype.org/pages/manual-staging-bundle-creation-and-deployment.html)

1. 首先需要准备四个文件 

	`artifactID-version.pom`:`test-1.0.0.pom`
	`artifactID-version.jar`:`test-1.0.0.jar`
	`artifactID-version-javadoc.jar`:`test-1.0.0-javadoc.jar`
	`artifactID-version.-sources.jar`:`test-1.0.0-sources.jar`

2. 使用`GPG`对这四个文件进行签名.`gpg -ab test-1.0.0.pom`.经过这一步之后会得到一个`.asc`的文件

		test-1.0.0.pom
		test-1.0.0.pom.asc
		test-1.0.0.jar
		test-1.0.0.jar.asc
		test-1.0.0-javadoc.jar
		test-1.0.0-javadoc.jar.asc
		test-1.0.0-sources.jar
		test-1.0.0-sources.jar.asc

3. 使用`jar`命令将之前的所有文件都打包

		jar -cvf bundle.jar test-1.0.0*

## 3.3 上传至OSSRH

登录`https://oss.sonatype.org/`.选择左侧面板中的`Build Promotion`选项下的`Staging Upload`.记得选择`Upload Mode`为`Artifact Bundle`
	
	Artifact(s) with a POM: GAV will be defined from a POM file.
	Artifact(s) with GAV: GAV needs to be manually defined.
	Artifact Bundle: A bundle file produced by Maven Repository Plugin, which should contain the POM.

在上传完`Bundle.jar`之后,选择左侧面板中的`Staging Repositories`查看刚才上传的信息.

- Status会经历三步`open`,`closed`,`release`

- 处于`open`状态下,通常是有一些文件信息的错误,修改后重新上传即可

- 处于`closed`状态 表示已经可以去发布(不过这一步还需要查看之前Issue中的状态)

- 点击`Release`之后 即可发布.**接着需要回到之前的`Issue`,然后在comment中留言表示已发布`Release`请求同步到线上**.成功之后会收到如下信息

	>Central sync is activated for com.github.iwantthink. After you successfully release, your component will be published to Central, typically within 10 minutes, though updates to search.maven.org can take up to two hours.

## 3.4 查看项目

在左侧面板中的`Staging Profiles`中即可查看已经release的Library

接着就需要耐心的等候..通常需要等2个小时以上..我等了四个小时 才在`http://search.maven.org`上搜索到我的Library

