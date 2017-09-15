# Groovy介绍
- Groovy是一种动态语言，运行于JVM。定义：Groovy是在java平台上的，具有像Python,Ruby和smalltalk 语言特性的灵活动态语言

- Groovy类似于脚本(shell)的存在，执行Groovy脚本时，Groovy会先将其编译为java类字节码，然后通过JVM 执行这个java类

- Groovy 除了使用JDK之外，还可以使用[GDK-API](http://www.groovy-lang.org/api.html)


# 1.基础知识

- Groovy注解标记和Java一样,支持// 和 /\*\*/ 和/\*\*\*/

- Groovy不用以 分号 结尾

- Groovy支持动态类型,即定义变量的时候可以不指定其具体类型（也可以指定具体类型）

- Groovy中定义变量可以使用关键词 **def**.但是实际上def 也不是必须的！只是为了代码清晰，建议还是加上**def**


- 函数定义时,参数的类型也可以不指定，例如:
		String func(arg1,arg2){
			....etc
		}

- Groovy中函数的返回值也可以是无类型的，**但是无类型的函数必须用def声明**,例如：
		def nonReturnTypeFunc(){
			"last code" //最后一句代码,返回类型为String
		}

- Groovy的函数里,可以不使用return 来设置函数返回值.如果不使用return语句，函数里最后一句代码的执行结果被设置成返回值 . 当然如果指定了返回值类型，就必须返回指定的类型

- 如果指定了函数的返回类型,则可以不必加def关键词来定义函数
		String getName(){
			return 'jack'
		}

- Groovy中的函数在调用的时候，可以不添加括号。虽然可以不添加括号，但是Groovy经常会将 属性 和函数调用混淆
		println('test')<==>println 'test'
	getName()如果不添加括号,Groovy会认为getName是一个变量
		def getName(){'ryan'}
	

## 1.1 字符串
- 单引号`'content'`：内容严格对应Java中的String，不对`$`符号进行转义
		def name = 'ryan'
		def str = 'i am $name'
		assert str == 'i am $name'

- 双引号`"content"`:如果字符串中有`$`符号，会对 $表达式 先求值
		def name = 'ryan'
		def str = "i am $name"
		assert str == 'i am ryan'

- 三个引号 ` ```content ```  `:内容支持随意换行
		def multieLine = ``` begin  
		line1  
		line2  
		end ```

## 1.2 数据类型
- java中的基本类型
- Groovy中的容器类
- 闭包

### 1.2.1 基本数据类型
作为动态语言，Groovy世界中的所有事物都是对象。所以，int，boolean这些Java中的基本数据类型，在Groovy代码中其实对应的是它们的包装数据类型。比如int对应为Integer，boolean对应为Boolean。

	def int x = 1
	println x.getClass().getCanonicalName()// java.lang.Integer


### 1.2.2 容器类
Groovy中容器类有三种:
- List:链表
- Map:
- Range: