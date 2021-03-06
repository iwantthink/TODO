# 1. Kotlinx 失效

场景:加入了firebase之后，某个页面的kotlinx 引用飘红...kotlin-android 和kotlin-android-extensions正常导入

解决方法：Android Studio -> File -> Invalidate Caches/Restart

# 2. Kotlin如何帮助避免内存泄露

[【译】Kotlin如何帮助避免内存泄漏](https://www.jianshu.com/p/ee29403bc68d)

主要原因是因为Lambda并不是创建了匿名内部类,因此如果在Java8中使用了lambda 那么同样不会引起由匿名内部类隐式持有外部类引用所导致的内存泄漏.

- 但是如果在lambda中使用了外部类的字段，那么仍然可能因持有对外部类的引用而导致内存泄露


# 3. Lambda

[](https://medium.com/tompee/idiomatic-kotlin-lambdas-and-sam-constructors-fe2075965bfb)

编译器能够在大部分情况下能够帮助你在Kotlin中使用Lambda时将其自动转换成实现了指定函数接口的匿名类，但是仍然有一些情况是需要你手动进行转换的！！(Kotlin支持使用SAM结构进行手动转换) 

Kotlin中的Lambda只可以转换成具有SAM结构的函数Java接口!!(SAM结构是指一个接口只有一个方法)。但是需要注意Kotlin不支持将lambda转换成Kotlin接口

	FunctionalInterfaceName { lambda_function}
	
通常使用情况:

	// 分配给变量时
	val runnable : Runnable = Runnable { print("I am a runnable")}
	// 返回指定类型的函数接口
	fun createOnClickListener() : View.OnClickListener {
	return View.OnClickListener { v -> print("I am clicked") }
	}
	
# 4. Kotlin依赖镜像地址

	maven{ url 'http://maven.aliyun.com/nexus/content/groups/public/'}
	

# 5. Aar依赖问题
[相关文章](https://www.geekpeer.com/Development/Android/8193.html)

**Gradle低版本中**,使用`maven publish`插件上传module至仓库，其pom文件中不会包含当前项目的依赖，可以通过手动编写pom文件内容，添加`dependencies`和`dependency` 为aar添加依赖声明

高版本中会带有

    pom.withXml {
        def dependenciesNode = asNode().appendNode('dependencies')

        configurations.api.allDependencies.each {
            if (!it.name.equals("unspecified")) {
                def dependencyNode = dependenciesNode.appendNode('dependency')
                dependencyNode.appendNode('groupId', it.group)
                dependencyNode.appendNode('artifactId', it.name)
                dependencyNode.appendNode('version', it.version)
            }
        }
    }

- 据说使用`maven`插件不会有该问题，但是Gradle官方现在是推荐使用`maven-publish`插件


当项目添加某个依赖，并且该依赖 有其自己的依赖， 默认并不会传递依赖,需要使用设置依赖的transition值为true 才能传递。

	projectB <<<< OkHttp
	
	projectA <<<< projectB
	
	projectA xxxxxxx OkHttp

示例:
	
    implementation() {
        transitive(true)
    }
