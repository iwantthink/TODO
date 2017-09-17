# Groovy介绍
- Groovy是一种动态语言，运行于JVM。定义：Groovy是在java平台上的，具有像Python,Ruby和smalltalk 语言特性的灵活动态语言

- Groovy类似于脚本(shell)的存在，执行Groovy脚本时，Groovy会先将其编译为java类字节码，然后通过JVM 执行这个java类

- Groovy 除了使用JDK之外，还可以使用[GDK-API](http://www.groovy-lang.org/api.html)


# 1.基础知识

- **建议使用API 之前先去查看文档**

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
	
- 根据Groovy的原则，如果一个类中有名为xxyyzz这样的属性（其实就是成员变量），Groovy会自动为它添加getXxyyzz和setXxyyzz两个函数，用于获取和设置xxyyzz属性值。  
注意，get和set后第一个字母是大写的  
所以，当你看到Range中有getFrom和getTo这两个函数时候，就得知道潜规则下，Range有from和to这两个属性。当然，由于它们不可以被外界设置，所以没有公开setFrom和setTo函数。

- 在Gstring

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

- List:链表,其底层对应Java中的List接口，一般用ArrayList作为真正的实现类
- Map:键-值表，底层对应java中的LinkedHashMap
- Range:范围，是List的一种拓展
  

使用介绍：  

1. List由`[]`定义，其元素可以是任何对象。变量存取可以直接通过索引存取，而且不用担心索引越界，如果索引超过当前链表长度，List会自动往该索引添加元素
		
		def aList = [1,'2',true]
		assert aList[1]=='2'
		assert aList[5]==null
		aList[11]= 11
		assert aList[11] ==11
		assert aList.size == 11
2. Map由`[:]`定义，冒号左边是key，右边是value。key必须是字符串，value可以是任何对象。另外key可以用单引号或双引号包裹，也可以不包裹  
		def aMap = ['key1':'value1','key2':'value2']
		def key1 = 'abc'
		def aNewMap = [key1:'value1']//key1 没加引号，但是默认被处理成字符串。如果需要转移的话需要在key1外边添加括号 `(key1)`
		aMap.keyName //使用方式1
		aMap.['keyName']//使用方式2
		aMap.anotherkey = 'i am map'//添加新元素
3. Range类型的变量，由`begin值+俩个点+end值` 组合表示,如果不想包含最后一个值，可以在`end值`前添加一个`<`符号
		def aRange = 1..5//包含1,2,3,4,5
		def aRangeWithOutEnd = 1..<5 //包含1,2,3,4
		aRange.from
		aRange.to

### 1.2.3 闭包Closure
- 闭包,是一种数据类型，是一段可执行的代码

- 语法：`{ [closureParameters->] statements   }`,中括号可选填，但是当有参数时  -> 是必须的

- 当Closure作为一个对象时,闭包 是groovy.lang.Closure 类的一个实例，所以可以作为任何其他变量被分配

		def listener  ={println it}
		assert listener instanceof Closure

- 如果不使用def 定义 Closure的话 ， 可以使用Closure  

		Closure listener2 = {->println "hi $it"}
		Closure<Boolean> listener3 = {File file->
			file.name.endWith('.txt')
		}

- Closure的调用，通过 `闭包对象.call(参数)`或者`闭包对象(参数)`

- Closure的参数：类型(可选)+名字(必须)+默认值(可选)
		
- 当一个Closure没有明确定义 参数时， 会有一个隐含的参数it  

		def closure = {"hello $it" }
		assert closure2('groovy') == 'hello groovy'
- Closure明确不需要参数时,需要以下形式:

		def closure ={->
    		"hi groovy"
		}
- Closure可以使用可变参数

		def vargsFunc1 = {String ... args -> args.join('')}
		assert vargsFunc1('1','2','3')=='123'
- Closure中参数使用数组的话也可以实现可变参数的功能

		def vargsFunc2 = {String [] args -> args.join('') }
		assert vargsFunc2('1','2','3')=='123'

- Closure中如果除了 可变参数外 还要有参数，那么可变参数需要放到最后
		def vargsFunc3 = {int i,String... args
			->
			println "i=$i ,args = $args"
		}
 
		vargsFunc3(1,'1','2')


- 函数中的最后一个参数如果是闭包，则可以省略函数调用的那个括号`()`
	def func(int i,Closure c){
    	c.call(i)
	}

	func 1,{println "param is $it"}

- 在Gstring中使用Closure  

	Gstring只会在创建的时候去估值,**${x}不能代表一个Closure**

		def x = 1
		def gs = "x=${x}"
		assert gs == 'x=1'
		x = 2
		assert gs != 'x=2'
	如果要在Gstring中使用真正的闭包。例如对变量进行延迟估值，请使用`${->x}`

		def y = 1
		def gss = "y=${->y}"
		assert gss =='y=1'
		y=2
		assert gss == 'y=2'


- curry(lef),功能是设置最左侧的参数，并返回一个设置了参数的闭包

		def curry1 = {int a,int b->a+b}
		def curry2 = curry1.curry(1)
		assert curry2(1)==2
		assert curry2(1)==curry1(1,1)

- right curry,设置最右侧的参数，并返回一个设置了参数之后的闭包

		def curry3 = {int a,String b-> "$b has $a kids"}
		def curry4 = curry3.rcurry('lucy')
		assert curry4(1)=='lucy has 1 kids'

- index based curry,如果Closure接收大于俩个参数，可以使用ncurry()去设定指定索引位置的参数值

		def curry5 = {a,b,c->a+b+c}
		def curry6 = curry5.ncurry(1,1)
		assert curry6(2,3)==6

- Composition，将一个 闭包的结果 作为另外一个闭包的 参数
		def plus2 = {it+2}
		def times3 = {it * 3}
		def timesInPlus = plus2<<times3
		assert timesInPlus(1)==5
		assert timesInPlus(1)== plus2(times3(1))

		def plusInTimes = plus2>>times3
		assert plusInTimes(1)==9
		assert plusInTimes(1)== times3(plus2(1))


- memolize

		def fib
		fib = { long n -> n<2?n:fib(n-1)+fib(n-2) }.memoize()
		println fib(111)
		//assert fib(15) == 610 // slow!

- Trampoline，递归算法通常受最大堆高度限制，例如你调用一个递归自身太多的方法，最终会收到一个 StackOverflowException

		def factorial
		factorial = { int n, def accu = 1G ->
    		if (n < 2) return accu
    		factorial.trampoline(n - 1, n * accu)
		}
		factorial = factorial.trampoline()

		assert factorial(1)    == 1
		assert factorial(3)    == 1 * 2 * 3
		assert factorial(1000) // == 402387260.. plus another 2560 digits





- 闭包的使用跟它的上下文有很大的关系，在使用之前尽量去查询API文档了解上下文语义！




形式如下：  

	def aClosure = {//闭包是一段代码，所以需要使用花括号
		String param,int param2->//箭头前是参数定义，后是代码
		println 'something'//这里是代码,最后一句是返回值
		//也可以使用return 进行返回
	}

形式2：
	def aClosure = {
		//无参数，纯code
		//其实有一个隐含的参数  it
	}

形式3：
	def aClosure = {
	-> doSomething...//这种写法表示不能给closure传参数
	}


#### 1.2.3.1 Closure中的this

- 这些都是仅存在于Closure中的概念

- this 相当于调用了getThisObject,将返回定义Closure的类
	
		class Enclosing{
    		void run1(){
        		def getObject = { getThisObject()}
        		assert getObject()==this
        		def getObject2 = {this}
        		assert getObject2() ==this
    		}
		}	
		def  enclosing = new Enclosing()
		enclosing.run1()

- 如果闭包在内部类中被定义，那么会返回内部类，而不是外部类

		class EnclosedInInnerClass{
    		class Inner{
      	  	Closure cl = {this}
    		}

    		void run(){
       		def inner = new Inner()
        	assert inner.cl() == inner
        	assert inner.cl() != this
    		}
		}
		def eiic = new EnclosedInInnerClass()
		eiic.run()

- 在嵌套Closure的情况下，将会返回外部类，而不是闭包

		class NestedClosure{
    		void run(){
        		def nestedClosures = {
            		def cl = {this}
            		cl()
        		}
        
        		assert nestedClosures()==this
    		}
		}


#### 1.2.3.2 Closure中的owner
- owner会返回一个 直接闭合 含有owner闭包 的对象，无论它是Closure或Class

		class EnclosingOwner{
    		void run(){
        		def getOwnerMethod = { owner }
        		assert getOwnerMethod()==this
        		def getOwnerMethod1 = { getOwner()}
        		assert getOwnerMethod1()==this
    		}
		}
		def e1 = new EnclosingOwner()		
		e1.run()


		class InnerOwnerClass{
   	 		class Inner{
        		Closure cl = {owner}
    		}
    		void run(){
        		def inner = new Inner()
        		assert inner.cl()==inner
    		}
		}
		def e2 = new InnerOwnerClass()
		e2.run()

		class NestedClosure2{
    		void run(){
        		def nestedMethod = {
            		def cl = {owner}
            		cl()
        		}
       		assert nestedMethod()==nestedMethod
			}
		}
		def e3 = new NestedClosure2()
		e3.run()

#### 1.2.3.3 Closure中的delegate
- 默认情况下，代理被设置为owner

		class Enclosing3{
    		void run(){
        		def func0 = {owner}
        		assert func0()==this
        		def func1 = {delegate}
        		assert func1()==this
        		def func2 = {getDelegate()}
        		assert func2()==this
        
        		def enclosed = {
            		{-> delegate}.call()
        		}
       
        		def ownerMethod = {
            		{->owner}.call()
        		}
        		//这里应该可以判断出 delegate 此时是 owner
        		assert enclosed()==enclosed
        		assert ownerMethod() ==ownerMethod
        
    		}
		}
		def e4 = new Enclosing3()
		e4.run()


- Closure的代理对象是可以设置的e

		class Jack{
    		String name
		}

		class Lucy{
    		String name
		}
		def jack = new Jack(name:'jack')
		def lucy = new Lucy(name:'lucy')
		def delegateClosure = { delegate.name.toUpperCase() }
		delegateClosure.delegate = jack
		assert delegateClosure()== 'JACK'
		delegateClosure.delegate = lucy
		assert delegateClosure()=='LUCY'

- Closure中，无需明确设置delegate，即可使用delegate

		class Ryan{
    		String name
		}
		def r = new Ryan(name:'Ryan')
		def r2 = {name.toUpperCase()}
		r2.delegate = r
		assert r2() == 'RYAN'

- delegate的委托策略是：Closure.OWNER_FIRST owner优先，delegate其次。这是默认策略

		class Ryan1{
    		String name
		}
		class Ryan2{
    		String name = 'Ryan2'
    		def upper = {name.toUpperCase()}
    		void run(){
        		def t = {owner}
        		assert t().name==this.name
        		def r = { delegate.name.toUpperCase()}
        		r.resolveStrategy = Closure.OWNER_FIRST
        		assert r()=='RYAN2'
        		def ryan1 = new Ryan1(name:'Ryan1')
        		r.delegate = ryan1
        		assert r()=='RYAN1'
    		}
		}
		def ryan1 = new Ryan1(name:'Ryan1')
		def ryan2 = new Ryan2()
		ryan2.upper.delegate = ryan1
		assert ryan2.upper()=='RYAN2'

- Closure.DELEGATE_FIRST delegate优先 owner 其次
		ryan2.upper.resolveStrategy = Closure.DELEGATE_FIRST
		assert ryan2.upper()=='RYAN1'

- Closure.OWNER_ONLY 仅针对 owner，Closure.DELEGATE_ONLY  仅针对 delegate


## 1.3 脚本类
### 1.3.1 脚本类
- Groovy可以像java那样填写package 然后写类
- 可以通过import 添加其他包下的类
- Groovy默认的类以及变量 默认都是public的

### 1.3.2 什么是脚本？ 
- groovy文件只要不是和java一样的去定义class，那就是一个脚本

- 可以通过`grooyc -d classes test.groovy`将groovy文件转换成class文件,`-d path`是设置class文件的存储位置

- groovy脚本会被转换成了一个类，它从script派生。

- **每一个脚本都会生成一个static main函数**。这样，当`groovy test.groovy`的时候，其实就是用java去执行这个main函数

- 脚本中的所有代码都会放到run函数中。比如，println 'Groovy world'，这句代码实际上是包含在run函数里的。

- 如果脚本中定义了函数，则函数会被定义在类中。、

### 1.3.3 脚本中的变量和作用域
例如：  

	def x = "hello groovy xxx"	
	def y = 'hello groovy yyy'
	def z = 1234565
	def printx(){
   	 println x
    	println y
    	println z
	}
	printx()

反编译过来的话如下：  

	public Object run()
  	{
   		CallSite[] arrayOfCallSite = $getCallSiteArray(); 	
		Object x = "hello groovy xxx";
    	Object y = "hello groovy yyy";
    	Object z = Integer.valueOf(1234565);

   	 	if ((__$stMC) || (BytecodeInterface8.disabledStandardMetaClass())) return arrayOfCallSite[1].callCurrent(this); else return printx(); return null;
  	}

  	public Object printx()
 	 {
    	CallSite[] arrayOfCallSite = $getCallSiteArray(); arrayOfCallSite[2].callCurrent(this, arrayOfCallSite[3].callGroovyObjectGetProperty(this));
    	arrayOfCallSite[4].callCurrent(this, arrayOfCallSite[5].callGroovyObjectGetProperty(this));
    	return arrayOfCallSite[6].callCurrent(this, arrayOfCallSite[7].callGroovyObjectGetProperty(this)); return null;
  	}

上面的代码中，`printx()`被定义成test类的成员函数，`def x = 1`实际上是在run()当中去定义的，所以`printx()`访问不到变量`x`。解决办法就是：**定义的时候不要添加类型和def**  
如下代码就是 不添加类型和def 反编译类中的run()方法中的代码：  

	    CallSite[] arrayOfCallSite = $getCallSiteArray(); 
		String str = "hello groovy xxx"; 
		ScriptBytecodeAdapter.setGroovyObjectProperty(str, test.class, this, (String)"x");

但是这种方式仍然不是将变量x定义成成员变量,虽然可以令pintx()访问到变量x！解决办法就是：**在x前面加上@Field标注，这样，x就彻彻底底是test的成员变量了**

	import groovy.transform.Field;   //必须要先import  
	@Field x = 1


## 1.4 文件I/O
- java.io.File: [File API](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/io/File.html)
- java.io.InputStream: [InputStream API](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/io/InputStream.html)      
- java.io.OutputStream: [OutputStream API](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/io/OutputStream.html)
- java.io.Reader: [Reader API](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/io/Reader.html)
- java.io.Writer: [Writer API](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/io/Writer.html)
- java.nio.file.Path: [Path API](http://docs.groovy-lang.org/latest/html/groovy-jdk/java/nio/file/Path.html)

例子：  

	def srcFile = new File(源文件名)  
	def targetFile = new File(目标文件名)  
	targetFile.withOutputStream{ os->  
  	srcFile.withInputStream{ ins->  
      	os << ins   //利用OutputStream的<<操作符重载，完成	从inputstream到OutputStream的输出 
		// <<是操作符重载 leftShift 
   		}  
	}  

## 1.5 XML操作
查看文档。。。
