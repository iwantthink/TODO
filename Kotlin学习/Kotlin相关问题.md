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
	
	