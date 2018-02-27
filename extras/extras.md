- 这里有一个特殊的字符\, 如果想将之转义为普通的反斜线,并不能直接\\, 因为这和正则表达式中定义的新的转义方式冲突了,这种情况下使用\\\\,表示正则表达式中的字面意义的反斜线. 这是一种特殊情况, 记下即可

- android studio 2.2.2 bug ，如果创建创建多个module，在libs下放相同名称的jar包，只会加载第一个重名的jar包，其他项目中的jar包不会被加载！


- android studio 3.0.0 新建一个项目。。。一直卡在 background tasks执行的阶段，看信息 是一直在下载`appcompat-v7 26.0.0-beat1`...**不知道为什么下载不过来！！**。然后我换成26.0.1 26.0.0 都不行。。 但是换成25.3.1是成功的(25.3.1 是我本地有的**)

- android studio 使用 生成混淆SDK的TASK 时，出现了一个`can't find common super class of`错误，通过修改混淆配置`ignorewarning`解决但是打出来的包 会出现`AbstractMethodError:abstract method "void java.lang.Runnable.run()"`异常。

	最终是通过添加`-libraryjars C:\Program Files\Java\jre1.8.0_151\lib\rt.jar `到混淆规则中，解决了这个问题(此外我还额外添加了 Android 默认的混淆规则进去)

	系统自动生成的aar-release(混淆后)是可以正常使用的