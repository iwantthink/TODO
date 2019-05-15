# DSL学习
[Domain-Specific Languages](http://www.groovy-lang.org/dsls.html)

[Groovy 中文文档](https://groovys.readthedocs.io/zh/latest/GettingStarted/index.html)

[DSL 极客学院](http://wiki.jikexueyuan.com/project/groovy-introduction/domain-specific-languages.html)

# 1 命令链(`Command chains`)
Groovy允许在调用方法时，省略参数周围的圆括号.命令链扩展了这个功能，不仅允许省略括号，还可以省略方法调用链之间的点号(`.`) ,即`a(b).c(d)`实际上可以表示成 `a b c d `。

- 命令链支持 方法参数类型为闭包参数，命名参数，或者方法拥有多个参数

- 命令链也可以用于赋值

示例：

	// equivalent to: turn(left).then(right)
	turn left then right
	
	// equivalent to: take(2.pills).of(chloroquinine).after(6.hours)
	take 2.pills of chloroquinine after 6.hours
	
	// equivalent to: paint(wall).with(red, green).and(yellow)
	paint wall with red, green and yellow
	
	// with named parameters too
	// equivalent to: check(that: margarita).tastes(good)
	check that: margarita tastes good
	
	// with closures as parameters
	// equivalent to: given({}).when({}).then({})
	given { } when { } then { }


- **如果命令链中存在一个方法调用是没有参数的，那么这个方法调用必须使用圆括号**

		// equivalent to: select(all).unique().from(names)
		select all unique() from names

- 如果命令链中的元素是奇数个，那么该链将由若干个 "方法（参数）"组成,并在命令链尾部调用属性访问

		// equivalent to: take(3).cookies
		// and also this: take(3).getCookies()
		take 3 cookies

## 1.1 如何使用命令链创建DSL
使用`map`和闭包创建DSL


	show = { println it }
	square_root = { Math.sqrt(it) }
	
	def please(action) {
	  [the: { what ->
	    		   [of: { n -> action(what(n)) }
	    	       ]
	  	     }
	  ]
	}
	
	// equivalent to: please(show).the(square_root).of(100)
	please show the square_root of 100
	// ==> 10.0

## 1.2 如何简化已经存在的方法调用
如果已经存在一连串的方法调用，可以通过命令链将其简化成DSL

以下是一个Java方法调用链:

	@Grab('com.google.guava:guava:r09')
	import com.google.common.base.*
	def result = Splitter.on(',').trimResults(CharMatcher.is('_' as char)).split("_a ,_b_ ,c__").iterator().toList()

为了将其简化成DSL，先根据上述代码构建一个帮助类(这个帮助方法是基础,必须提供):

	@Grab('com.google.guava:guava:r09')
	import com.google.common.base.*
	def split(string) {
	  [on: { sep ->
	    [trimming: { trimChar ->
	      Splitter.on(sep).trimResults(CharMatcher.is(trimChar as char)).split(string).iterator().toList()
	    }]
	  }]
	}

将帮助方法代码以命令链形式调用:

	split "_a ,_b_ ,c__" on ',' trimming '-\'

# 2 Operator overloading

查看[操作符.md]()的10.2小节

# 3 Script base classes
# 3.1 The Script class
**脚本形式的Groovy文件总是会被编译成一个继承了`groovy.lang.Script`的类，这个类包含了一个抽象方法`run()`,当脚本被编译之后，脚本中定义的方法会被添加到这个生成的类当中，直接在脚本主体中的内容会被添加到`run()`方法中**

通过`Binding`对象可以使得应用程序和其调用的`Groovy`脚本进行数据共享:
	
	// sample.groovy
	def binding = new Binding()             
	def shell = new GroovyShell(binding)    
	binding.setVariable('x',1)              
	binding.setVariable('y',3)
	shell.evaluate 'z=2*x+y'                
	assert binding.getVariable('z') == 5  

- `binding`对象用于Groovy脚本和调用脚本的应用程序之间共享数据

- `GroovyShell`可以用来执行Groovy代码,它可以接受`Binding`作为构造参数


除了使用`Binding`对象之外，还可以使用自定义脚本基类来实现


# 3.2 The @BaseScript annotation
# 3.3 Alternate abstract method
# 4 Adding properties to numbers
# 5 @DelegatesTo
## 5.1. Explaining delegation strategy at compile time
## 5.2. @DelegatesTo
## 5.3. DelegatesTo modes
### 5.3.1. Simple delegation
### 5.3.2. Delegation strategy
### 5.3.3. Delegate to parameter
### 5.3.4. Multiple closures
### 5.3.5. Delegating to a generic type
### 5.3.6. Delegating to an arbitrary type
# 6 Compilation customizers
6.1 Introduction
6.2 Import customizer
6.3 AST transformation customizer
6.4 Secure AST customizer
6.5 Source aware customizer
6.6 Customizer builder
6.6.1 Import customizer
6.6.2 AST transformation customizer
6.6.3 Secure AST customizer
6.6.4 Source aware customizer
6.6.5 Inlining a customizer
6.6.6 Multiple customizers
6.7 Config script flag
6.7.1 Static compilation by default
6.8 AST transformations
7 Custom type checking extensions
8 Builders
8.1 Creating a builder
8.1.1 BuilderSupport
8.1.2 FactoryBuilderSupport
8.2 Existing builders
8.2.1 MarkupBuilder
8.2.2 StreamingMarkupBuilder
8.2.3 SaxBuilder
8.2.4 StaxBuilder
8.2.5 DOMBuilder
8.2.6 NodeBuilder
8.2.7 JsonBuilder
8.2.8 StreamingJsonBuilder
8.2.9 SwingBuilder
8.2.10 AntBuilder
8.2.11 CliBuilder
Using Annotations and an interface
Using Annotations and an instance
Using Annotations and a script
Options with arguments
Specifying a type
Custom parsing of the argument String
Options with multiple arguments
Types and multiple arguments
Setting a default value
Use with TypeChecked
Advanced CLI Usage
Apache Commons CLI
Picocli
8.2.12 ObjectGraphBuilder
8.2.13 JmxBuilder
8.2.14 FileTreeBuilder