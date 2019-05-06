# Groovy学习
[Groovy核心类源码讲解(上)](https://www.imooc.com/article/44004)

[Groovy官方文档](http://www.groovy-lang.org/documentation.html)

[GDK API 示例(具体例子)](http://www.groovy-lang.org/groovy-dev-kit.html)

# 简介

Groovy是一种动态语言，运行于JVM。定义：Groovy是在java平台上的，具有像Python,Ruby和smalltalk 语言特性的灵活动态语言

Groovy类似于脚本(shell)的存在，执行Groovy脚本时，Groovy会先将其编译为java类字节码，然后通过JVM 执行这个java类

Groovy 除了使用JDK之外，还可以使用[GDK-API](http://www.groovy-lang.org/api.html)


# 3.基础知识

- **建议使用API 之前先去查看文档**

- `groovyConsole`(打开Groovy控制台)
	- `ctrl+w`(clear output window)
	- `ctrl+r`(run groovy code)

- Groovy注解标记和Java一样,支持`//` 单行注释,`/*content*/`多行注释 和GroovyDoc评论`/**content*/`(GroovyDoc评论可以添加@param,@return等)

- `shebang line`:UNIX系统支持的一种特殊单元注释，用于指明脚本的运行环境，这样就可以直接在终端中使用`xxx.groovy`运行，而不用像`groovy xxx.groovy`
		
		#!/usr/bin/env groovy
		println "Hello from the shebang line"

	- `#`符号必须作为首字符

- Groovy标识符可以以字母，下划线，$符号开头，但是不能以数字开头

- 转义字符:`\` 反斜杠

	- 对于键盘上不存在的符号，可以使用unicode转义序列:`\+u+4个十六进制数字`

- **Groovy可以不用分号`;` 结尾**

- Groovy**支持动态类型**,即定义变量的时候可以不指定其具体类型（当然也可以指定具体类型)

- Groovy中定义变量可以使用关键词 **def**.但是实际上def 也不是必须的！只是为了代码清晰，建议还是加上**def**


- **函数定义时,参数的类型也可以不指定**，例如:

		String func(arg1,arg2){
			....etc
		}

- Groovy中函数的返回值也可以是无类型的，**但是无类型的函数必须用def声明,或者用void声明**,例如：

		def nonReturnTypeFunc(){
			"last code" //最后一句代码,返回类型为String
		}

- **Groovy的函数里,可以不使用`return `来设置函数返回值**.如果不使用return语句，函数里最后一句代码的执行结果被设置成返回值 . 当然如果指定了返回值类型，就必须返回指定的类型

- 如果指定了函数的返回类型,则可以不必加def关键词来定义函数

		String getName(){
			return 'jack'
		}

- **Groovy中的函数在调用的时候，可以不添加括号**。虽然可以不添加括号，但是Groovy经常会将 属性 和函数调用混淆

		println('test')<==>println 'test'
	    
- `getName()`如果不添加括号,Groovy会认为`getName`是一个变量

		def getName(){'ryan'}
	
- 根据Groovy的原则，如果一个类中有名为xxyyzz这样的属性（其实就是成员变量），Groovy会自动为它添加getXxyyzz和setXxyyzz两个函数，用于获取和设置xxyyzz属性值。  

	注意，get和set后第一个字母是大写的  

	所以，当你看到Range中有getFrom和getTo这两个函数时候，就得知道潜规则下，Range有from和to这两个属性。当然，由于它们不可以被外界设置，所以没有公开setFrom和setTo函数。

- 指定类型的方式：   

		char c1 = 'A'
		def c2 = 'A' as char
		def c3 = (char)'A'
	
		assert c1 instanceof Character
		assert c2 instanceof Character
		assert c3 instanceof Character	

- 如果抛出异常，会使用脚本被转换之前的 行号 而不是生成的代码的行号

- 引用标识符，Groovy在`dotted expression` 后面可以使用引号标识符，例如`map.a` 可以表示为`map.'a'`或`map."a"`.同时引号中可以包含Java中不支持的空格，减号`-`。
		
		def map = [:]
		
		map."an identifier with a space and double quotes" = "ALLOWED"
		map.'with-dash-signs-and-single-quotes' = "ALLOWED"
		
		assert map."an identifier with a space and double quotes" == "ALLOWED"
		assert map.'with-dash-signs-and-single-quotes' == "ALLOWED"

	- Groovy支持多种字符串的字面量表达式,GString也是支持的

			map.'single quote'
			map."double quote"
			map.'''triple single quote'''
			map."""triple double quote"""
			map./slashy string/
			map.$/dollar slashy string/$

			def name = 'abc'
			map."${name}"


- **Groovy默认的类以及变量 默认都是`public`的**

## 3.1 字符串

## 3.2 数据类型

- java中的基本类型

- Groovy中的容器类

- 闭包

### 3.2.1 Characters
Groovy并没有明确的字符类型值，但是可以通过如下方式指定类型为字符：

	char c1 = 'A' 
	assert c1 instanceof Character
	
	def c2 = 'B' as char 
	assert c2 instanceof Character
	
	def c3 = (char)'C' 
	assert c3 instanceof Character

### 3.2.2 Numbers

**允许在数字中使用下划线`_` 增加数字可阅读性**

	long reditCardNumber = 123456_789
	assert reditCardNumber == 123456789

作为动态语言，**Groovy世界中的所有事物都是对象**。所以，int，boolean这些Java中的基本数据类型，在Groovy代码中其实对应的是它们的包装数据类型。比如int对应为Integer，boolean对应为Boolean。

	def int x = 1
	println x.getClass().getCanonicalName()//java.lang.Integer

可以通过添加后缀，指定数字的类型

数字类型|后缀
---|---
BigInteger|`G` or `g`
Long | `L` or `l`
Integer | `I` or `i`
BigDecimal | `G` or `g`
Double | `D` or `d`
Float| `F` or `f`

	// 可以通过添加后缀 指定数字类型
	assert 1i.class == Integer
	assert 1i.class != Long

#### 3.2.2.1 整数

整数类型： `byte,char,short,int,long` (这五种为原始类型)，`java.lang.BigInteger`(无限精度)

	// primitive types
	byte  b = 1
	char  c = 2
	short s = 3
	int   i = 4
	long  l = 5
		
	// infinite precision
	BigInteger bi =  6

使用`def`定义整数，变量的类型会根据类型的容量去适应这个整数值
		
	def a = 1
	assert a instanceof Integer

	// Integer.MAX_VALUE
	def b = 2147483647
	assert b instanceof Integer

	// Integer.MAX_VALUE + 1
	def c = 2147483648
	assert c instanceof Long

	// Long.MAX_VALUE
	def d = 9223372036854775807
	assert d instanceof Long

	// Long.MAX_VALUE + 1
	def e = 9223372036854775808
	assert e instanceof BigInteger

	//负数
	def na = -1
	assert na instanceof Integer

	// Integer.MIN_VALUE
	def nb = -2147483648
	assert nb instanceof Integer

	// Integer.MIN_VALUE - 1
	def nc = -2147483649
	assert nc instanceof Long

	// Long.MIN_VALUE
	def nd = -9223372036854775808
	assert nd instanceof Long

	// Long.MIN_VALUE - 1
	def ne = -9223372036854775809
	assert ne instanceof BigInteger

定义二进制，八进制，十六进制

	//二进制   0b 前缀
	int xInt2 = 0b11
	assert xInt2 == 3

	//八进制 0前缀 后面跟八进制数字
	int xInt8 = 077
	assert xInt8 == 63

	//十六进制 0x 前缀
	int xInt16 = 0x3a
	assert xInt16 == 58

#### 3.2.2.2 小数

**小数类型有：`float,double,Java.lang.BigDecimal`**

	// primitive types
	float  f = 1.234
	double d = 2.345
	
	// infinite precision
	BigDecimal bd =  3.456

小数可以使用exponents(指数)形式表达，通过`e`或`E`来表示

	assert 1e3  ==  1_000.0
	assert 2E4  == 20_000.0
	assert 3e+1 ==     30.0
	assert 4E-2 ==      0.04
	assert 5e-1 ==      0.5

**小数无法使用 二进制，八进制或十六进制表示**

#### 3.2.2.3 数学运算

**`byte, char, short` 和 `int` 互相进行二元运算，结果都是 `int`**

`long` 和 `(byte, char, short and int)` 进行二元运算，结果都是long

`BigInteger` 和任何整数类型进行计算，结果都是 `BigInteger`

`BigDecimal` 和 `(byte, char, short, int and BigInteger )`进行计算，结果是`BigDecimal`

`float, double ` 和 `BigDecimal`互相进行二元运算，结果是`double`

俩个BigDecimal计算结果还是 BigDecimal

**规则总结如图：**

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fm03wh0vrsj20n80f83yq.jpg)

**因为Groovy的运算符重载，所以运算符可以直接作用于BigInteger和BigDecimal类型的数据,而不需要通过特定的方法去操作**

##### 3.2.2.3.1 除法运算

`/`或`/=` 这俩个运算符，只要算式中存在一个float,double那么结果就是Double类型，否则都是BigDecimal类型

	 assert (4/3).class == BigDecimal
	 assert (4d/3).class == Double
	 assert (4f/3).class == Double
	 assert (4l/3).class == BigDecimal

**Groovy不提供专用的整除运算符号**！只能通过`intdiv()`函数
		
	assert 6.intdiv(5)==1

##### 3.2.2.3.2 次方运算			

次方运算符号是`**` ，表达式： `基数**指数`

- **如果指数是小数** ，如果可以返回 integer 那就返回integer  ， 可以返回Long 就返回Long ， 否则的话统一返回Double
		
		assert 2**0.1 instanceof Double
		assert 2**-0.1 instanceof Double
		assert 1**-0.3f instanceof Integer
		assert 9.9**1.9 instanceof Double

- **如果指数是整数**
	- 负整数:按照数据是否满足条件，返回Integer,Long 或 Double
			assert 10**-1 instanceof Double
			assert 1**-1 instanceof Integer
	- 正整数或零:根据基数分类
			//如果 是正整数或者零， 那么根据 基数来分类
			//如果 基数是 BigDecimal  那么返回 BigDecimal
			//如果 基数是BigInteger 那么返回BigInteger
			//如果 基数是Integer 那么返回Integer ，当数据放不下时  就返回 BigInteger
			//如果 基数是Long ，那么返回Long ， 当数据放不下时  就返回BigInteger
			assert new BigDecimal(10) ** 0 instanceof BigDecimal
			assert new BigInteger(10) ** 1 instanceof BigInteger
			assert 10i ** 1 instanceof Integer
			assert 10i ** 10 instanceof BigInteger
			assert 10l ** 10 instanceof Long
			assert 10l ** 100 instanceof BigInteger

##### 3.2.3 布尔型

`true/false`,可以存储到变量中。更复杂的布尔表达式可以通过逻辑运算符表示。

	def myBooleanVariable = true
	boolean untypedBooleanVar = false
	booleanField = true

### 3.2.4 容器类

Groovy中容器类有三种:

- `List`:链表,其底层对应Java中的List接口，一般用ArrayList作为真正的实现类.**除非使用as操作符转换类型，或者显示的声明其变量类型**

- `Map`:键-值表，**底层对应java中的LinkedHashMap**

- `Range`:范围，是List的一种拓展

- `Arrays`:数组，必须得指定类型

**使用介绍：  **

#### 3.2.4.1 List

- `List`由中括号`[]`定义，其元素可以是任何对象。变量存取可以直接通过索引存取，而且不用担心索引越界，如果索引超过当前链表长度，List会自动往该索引添加元素
		
		//这里包含数字,字符串和布尔值
		def aList = [1,'2',true]
		assert aList[1]=='2'
		assert aList[5]==null
		aList[11]= 11
		assert aList[11] ==11
		assert aList.size == 11

- 通过`list.[下标]`可以访问对应位置.列表的第一个元素下标为`0`,末尾元素的下标可以表示为`list.size()-1`或`-1`

- 支持同时访问多个数据

		//访问 第二个和第四个元素
		assert letters[1, 3] == ['b', 'd']
		//访问 第三个到第五个元素         
		assert letters[2..4] == ['C', 'd', 'e']

- 可以通过`<<`leftshift 操作符往List末尾添加一个数据

		def aList = [1,2,3]
		aList<<4
		assert aList.size() == 4

- List还可以包含其他List，构建成一个多维列表

		def multi = [[0,1],[2,3]]
		assert multi[1][1]==3	 
 
- 通过`size()`方法获取列表大小

- 通过以下方式可以转换`List`默认的实现

			def arrayList = [1, 2, 3]
			assert arrayList instanceof java.util.ArrayList
			
			def linkedList = [2, 3, 4] as LinkedList    
			assert linkedList instanceof java.util.LinkedList
			
			LinkedList otherLinked = [3, 4, 5]          
			assert otherLinked instanceof java.util.LinkedList

#### 3.2.4.2 Map

- Map由`[:]`定义，冒号左边是key，右边是value。key建议是字符串，value可以是任何对象。另外key可以用单引号或双引号包裹，也可以不包裹
  
		def aMap = ['key1':'value1','key2':'value2']
		aMap.key1 //取值方式1 
		aMap['key1']//取值方式2

- 获取不存在的key时 会返回`null`

		assert aMap.yellow == null//取不存在的值会返回null
		aMap.anotherkey = 'i am map'//添加新元素，anotherkey是key的名称

- Groovy创建的Map 实际上是`java.util.LinkedHashMap`

- 可以使用String或int 作为key，但是key类型为int时，取值不能直接用`.key`去获取值，而必须使用`map[key]`方式进行获取。另外添加新的key时，也可以是数字 但是需要用`''`包裹

		aMap.'3' = 2
		assert aMap.containsKey('3')

- 如果使用一个变量的name作为key，那么会把这个name当做key，而不是这个name对应的内容.**可以通过添加`()`括号来使用其对应内容当做key**。

		def key  = 'hello'
		def maps = [key:'world']
		assert maps.containsKey('key')
		assert !maps.containsKey('hello')

		def key = 'hello'
		def maps = [(key):'world']
		assert maps.containsKey('hello')
		assert !maps.containsKey('key1')

- 通过`anotherKey= 'value'`直接添加key-value

		def maps = ['a':1]
		maps.anotherKey = 'b'
		assert map.containsKey('anotherKey')

#### 3.2.4.3 Range
			
Range类型的变量，由`begin值+俩个点+end值` 组合表示,如果不想包含最后一个值，可以在`end值`前添加一个`<`符号

	def aRange = 1..5//包含1,2,3,4,5
	def aRangeWithOutEnd = 1..<5 //包含1,2,3,4
	aRange.from
	aRange.to

	def r = 1..5
	println r.class
	>>>>>>>>>>>>
	class groovy.lang.IntRange

#### 3.2.4.4 Arrays

Groovy重用了List的表示符用来表示数组，此外**为了创建数组还必须声明其类型**

	String[] arrStr = ['Ananas', 'Banana', 'Kiwi']  
		
	assert arrStr instanceof String[]    
	assert !(arrStr instanceof List)

- 通过`as`运算符创建一个数组
		
		def numArr = [1, 2, 3] as int[]      
		
		assert numArr instanceof int[]       
		assert numArr.size() == 3

- 可以创建多维数组
	
			def matrix3 = new Integer[3][3]         
			assert matrix3.size() == 3

- 可以声明一个数组同时不去指定其边界
			
			Integer[][] matrix2                     
			matrix2 = [[1, 2], [3, 4]]
			assert matrix2 instanceof Integer[][]

- 对数组的访问与List相同

- **Groovy 不支持Java中的数组初始化符号`String [] arr = new String[]{1,2,3}`,因为这样会被Groovy认为是闭包**

### 3.2.5 闭包Closure

闭包,是一种数据类型，是一段匿名的可执行的代码,可以接收参数并返回值给一个变量，同时Closure可以使用外部定义的变量

语法：`{ [closureParameters->] statements   }`,中括号可选填，但是当有参数时  `->` 是必须的

- **闭包中的`return`关键字相当于`continue`,不会跳出遍历,而是终止当前循环,开始下次循环**

- 当Closure作为一个对象时,闭包 是`groovy.lang.Closure `类的一个实例，所以可以作为任何其他变量被分配

		def listener  ={println it}
		assert listener instanceof Closure

- 如果不使用def 定义 Closure的话 ， 可以使用Closure  

		Closure listener2 = {println "hi $it"}
		Closure<Boolean> listener3 = {File file->
			file.name.endWith('.txt')
		}

- Closure的调用，通过 `闭包对象.call(参数)`或者`闭包对象(参数)`

- Closure的参数：类型(可选)+名字(必须)+默认值(可选)
		
- 当一个Closure没有明确定义参数且没有添加`->`时， 会有一个隐含的参数it  

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

- Closure中如果除了 可变参数外 还要有参数，**那么可变参数需要放到最后**

		def vargsFunc3 = {int i,String... args
			->
			println "i=$i ,args = $args"
		}
 
		vargsFunc3(1,'1','2')


- **函数中的最后一个参数如果是闭包，则可以省略函数调用的那个括号`()`**

		def func(int i,Closure c){
	    	c.call(i)
		}
	
		func 1,{println "param is $it"}
		func(1){println 'param ...'}

- **在Gstring中使用Closure  **

	Gstring只会在创建的时候去估值,**`${x}`不能代表一个Closure**

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

- `index based curry`,如果Closure接收大于俩个参数，可以使用`ncurry()`去设定指定索引位置的参数值

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
		//解决办法
		factorial = factorial.trampoline()

		assert factorial(1)    == 1
		assert factorial(3)    == 1 * 2 * 3
		assert factorial(1000) // == 402387260.. plus another 2560 digits

- **闭包的使用跟它的上下文有很大的关系，在使用之前尽量去查询API文档了解上下文语义！**



- Closure的形式

	形式1：  
	
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

- `Owner,delegate and this`

	- `this`:

		对应于定义Closure的闭合类

	- `owner`:

		对应于定义Closure的闭合对象，这个闭合对象可以是 class也可以是Closure

	- `delegate`:

		对应于第三方对象，在没有定义消息接收者时，方法会通过第三方对象调用

#### 3.2.5.1 Closure中的this

**这些都是仅存在于Closure中的概念**

`this`对应的是当前`Closure`所在的闭合类

**this 相当于调用了getThisObject,将返回定义Closure的类**
	
- `Closure`定义在脚本.`this`返回的是`groovyc`编译生成的类

		def cl = {
		    println this
		}
		
		cl()


- `Closure`定义在类中.`this`返回该类

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

- `Closure`在内部类中被定义，那么会返回内部类，而不是外部类

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

- **在嵌套Closure的情况下，将会返回外部类，而不是闭包**

		class NestedClosure{
    		void run(){
        		def nestedClosures = {
            		def cl = {this}
            		cl()
        		}
        
        		assert nestedClosures()==this
    		}
		}


#### 3.2.5.2 Closure中的owner
`owner`与`this`类似,**区别是`owner`会返回一个直接包含当前闭包的对象，包括`Closure`或`Class` (但是`this`仅会返回类对象)**

- `Closure`位于类中,`owner`就指向该类对象

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

- `Closure`位于内部类,`owner`就指向该内部类对象

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

- `Closure`位于嵌套`Closure`,`owner`指向包裹该`Closure`的外层`Closure`

		class NestedClosure2{
    		void run(){
        		def nestedClosure = {
            		def cl = {owner}
            		cl()
        		}
       		assert nestedClosure()==nestedClosure
			}
		}
		def e3 = new NestedClosure2()
		e3.run()

#### 3.2.5.3 Closure中的delegate

`Closure`的代理可以通过`delegate`或`getDelegate()`获取.这个概念对于创建Groovy中的DSL十分重要

- **`delegate`默认值是`owner`的值**

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
					// 返回的是外层closure
            		{-> delegate}.call()
        		}
       
        		def ownerMethod = {
					// 返回的是外层closure
            		{->owner}.call()
        		}
        		//这里应该可以判断出 delegate 此时是 owner
        		assert enclosed()==enclosed
        		assert ownerMethod() ==ownerMethod
        
    		}
		}
		def e4 = new Enclosing3()
		e4.run()


- Closure的代理对象是可以设置的

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

- Closure中，**如果没有明确使用的属性是哪个对象**.默认既是`delegate`

		class Ryan{
    		String name
		}
		def r = new Ryan(name:'Ryan')
		def r2 = {name.toUpperCase()}
		r2.delegate = r
		assert r2() == 'RYAN'

---
**delegate的委托策略有**：

`Closure.OWNER_FIRST`,`Closure.DELEGATE_FIRST`,`Closure.OWNER_ONLY`,`Closure.DELEGATE_ONLY`,`Closure.TO_SELF`.可以通过`resolveStrategy`属性设置

- `Closure.OWNER_FIRST`:默认策略,优先从`owner`中获取`property/method`,如果`owner`不存在则会从`delegate`中获取

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

- `Closure.DELEGATE_FIRST`: delegate优先 owner 其次

		ryan2.upper.resolveStrategy = Closure.DELEGATE_FIRST
		assert ryan2.upper()=='RYAN1'

- `Closure.OWNER_ONLY` 仅针对 owner..`Closure.DELEGATE_ONLY  `仅针对 delegate

- `Closure.TO_SELF`:

# 4 Groovy的 Scripts 与 classes

Groovy支持`classes`形式和`scripts`形式

**`classes`形式:**

	// 定义类,名称是任意的
	class Main {        
		//  作为类的入口方法                           
	    static void main(String... args) {          
	        println 'Groovy world!'                 
	    }
	}

- 这是常用的Java形式的代码,业务代码将会被嵌入到class中,然后被执行

**`scripts`形式**

	println 'Groovy world!'


**可以通过`groovyc`命令将`.groovy`文件编译成`.class`文件**

## 4.1 Script class

Groovy 编译器会将一个`script`编译生成`.class`文件,`Script`中的实体部分会被移动到`run()`方法中

	import groovy.lang.Binding;
	import groovy.lang.Script;
	import org.codehaus.groovy.runtime.InvokerHelper;
	import org.codehaus.groovy.runtime.callsite.CallSite;
	
	public class Sample extends Script{

		public Sample() {
			CallSite[] var1 = $getCallSiteArray();
		}
	  
		public Sample(Binding context){
			CallSite[] var2 = $getCallSiteArray();
	    	super(context);
		}
	  
	  public static void main(String... args)
	  {
	    CallSite[] arrayOfCallSite = $getCallSiteArray();
	    arrayOfCallSite[0].call(InvokerHelper.class, Sample.class, args);
	  }
	  
	  public Object run()
	  {
	    CallSite[] arrayOfCallSite = $getCallSiteArray(); 
		return var1[1].callCurrent(this, "Groovy world!");
	  }
	}


- 如果一个脚本是保存在一个文件中,那么这个文件的文件名将被用来作为脚本被编译之后所生成的类名称

	例如`Main.groovy`这个文件包含脚本,那么其编译之后的类名称是`Main`

- groovy文件只要不是和java一样的去定义class，那就是一个脚本

- 可以通过`grooyc -d classes test.groovy`将groovy文件转换成class文件,`-d path`是设置class文件的存储位置

- groovy脚本会被转换成了一个类，它从script派生。

- **每一个脚本都会生成一个static main函数**。这样，当`groovy test.groovy`的时候，其实就是用java去执行这个main函数



### 4.1.1 Methods within Scripts

Groovy 允许在Scripts中定义方法,如下所示:

	int fib(int n) {
	    n < 2 ? 1 : fib(n-1) + fib(n-2)
	}
	assert fib(10)==89

另外,还可以将方法和代码混合使用,那么所编译所生成的`script`类会将所有的方法都移动到`script`类中,并在`run()`方法中组装

	println 'Hello'                                 
	
	int power(int n) { 2**n }                       
	
	println "2^6==${power(6)}"   

转换后的形式:

	import org.codehaus.groovy.runtime.InvokerHelper
	class Main extends Script {
	    int power(int n) { 2** n}                   
	    def run() {
	        println 'Hello'                         
	        println "2^6==${power(6)}"              
	    }
	    static void main(String[] args) {
	        InvokerHelper.runScript(Main, args)
	    }
	}

- 脚本被转换成字节码之后,其原始代码的行号会被保留. 这样是为了方便异常被抛出时查找异常所在的代码

- 脚本中的所有代码都会放到run函数中。比如，println 'Groovy world'，这句代码实际上是包含在run函数里的。

- 如果脚本中定义了函数，则函数会被定义在类中。


## 4.2 脚本中的变量和作用域

[Integrating Groovy in a Java application](http://docs.groovy-lang.org/latest/html/documentation/guide-integrating.html#_integrating_groovy_in_a_java_application)

在脚本中的变量可以用俩种形式来表示:

	//形式1
	int x = 1
	int y = 2
	assert x+y == 3
	//形式2
	x = 1
	y = 2
	assert x+y == 3

形式1 和形式2有一些语法上的不同:

1. 如果以形式1 声明变量,那么这些变量是**局部变量**`local variable`.编译成`.class`之后,会被放到`run()`方法之中.所以同一个脚本的其他方法都访问不到

2. 如果以形式2 声明变量,这些变量会进入`script binding`.这些变量对其他的方法可见

**如果希望变量称为成员变量,且不通过`Binding`这种方式,可以通过添加`@Field`注释来实现**

### 4.2.1 实例

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

## 4.3 script类型的表现形式
- script 形式1 

		//Main2.Groovy Script的一种形式 ，无需声明它
		println "hello groovy "

- Script 的另一种表现形式(`.class`文件)

		//需要提供一个 run 方法 
		import org.codehaus.groovy.runtime.InvokerHelper
		class Main2 extends Script{
       		def run(){
           		println 'hello groovy srcipt'
       		}
       		static void main(String[] args){
           		InvokerHelper.runScript(Main2,args)
       		}
		}

## 4.4 class类型的表现形式

	class Main{
    	static void main(String... args){
        	println 'hello groovy'
    	}
	}


# 5 文件I/O
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

# 6 XML操作
查看文档。。。

# 7 groovy中程序结构

## 7.1 包名
Groovy中包名和Java中一致，用来分离代码避免产生冲突,Groovy需要在类定义之前指定包，否则使用默认包。

	package com.pkg

例如使用不在同一个包中的某个类，需要使用类的全限定名(即包名+类名).或者通过`import`关键字 导入指定包中的类

## 7.2 导包
使用`import` 手动导入包

	//MarkupBuilder 这个类位于 groovy.xml目录下
	import groovy.xml.MarkupBuilder

### 7.2.1 默认导入

Groovy会默认导入一些包

	import java.lang.*
	import java.util.*
	import java.io.*
	import java.net.*
	import groovy.lang.*
	import groovy.util.*
	import java.math.BigInteger
	import java.math.BigDecimal	

### 7.2.2 star import
**与Java一样,Groovy提供了通过`*`通配符导入,表示导入包中所有的类**
		
	import groovy.xml.*

### 7.2.3 静态导入
Groovy 允许静态导入，相当于包中的静态方法或字段当做自己类中的静态方法/字段使用。

	import static Boolean.FALSE
	assert !FALSE


Groovy 的静态导入与java相似 ，但是更加的动态，Groovy允许你的类中定义和 静态导入的方法拥有同样的名字，只需要俩者有不同的参数要求。这在java中是不被允许的 ，但是 groovy是允许的

	import static java.lang.String.format

	class SomeClass{
    	String format(Integer i){
        	i.toString
    	}
    static void main(String[] args){
        assert format('String')=='String'
       	assert new SomeClass().format(new Integer(1))=='1'
    	}
	}

- 如果具有相同的类型,那么导入的类优先!

### 7.2.4  Static import aliasing

静态导入搭配关键字`as`一起使用,可以优雅的解决命名空间的问题

假设需要获取到`Calendar`实例,通过使用其静态方法`getInstance()`. 那么这时可以通过静态导入该方法,并通过`getInstance()`调用即可

但是除了每次都调用`getInstance()`之外,还有一种更简洁的方式就是通过使用`as`关键字给该方法设置一个别名,之后调用别名即可

	import static Calendar.getInstance as now
	
	assert now().class == Calendar.getInstance().class


### 7.2.5 static star import
static star import 类似于常规star import ,**将从给定的类中导入所有的静态方法**

	import static java.lang.Math.*
	
	assert sin(0) == 0.0
	assert cos(0) == 1.0

### 7.2.6 导入别名

通过使用类型别名,可以使用自定义的名称去引用类.这需要通过`as`关键字完成

	import java.util.Date
	import java.sql.Date as SQLDate
	
	Date utilDate = new Date(1000L)
	SQLDate sqlDate = new SQLDate(1000L)
	
	assert utilDate instanceof java.util.Date
	assert sqlDate instanceof java.sql.Date

静态导入和普通导入都可以使用`as`设置别名.

		import static Calendar.getInstance as now
		assert now().class == Calendar.getInstance().class

		import java.util.Date as jud
		assert new jud() instanceof java.util.Date

# 8 Groovy的功能
[Groovyapi示例](http://www.groovy-lang.org/groovy-dev-kit.html)
## 8.1 遍历文件夹

`file.traverse{}`, 在GDK 文档中File 下的方法

>**public void traverse(Map options, Closure closure)
**

Map 可以设置一些过滤条件，例如type,nameFilter等等

- 如下例子

		def totalSize = 0
		def count = 0
		def sortByTypeThenName = { a, b ->
		    a.isFile() != b.isFile() ? a.isFile() <=> b.isFile() : a.name <=> b.name
		}
		rootDir.traverse(
		        type         : FILES,
		        nameFilter   : ~/.*\.groovy/,
		        preDir       : { if (it.name == '.svn') return SKIP_SUBTREE },
		        postDir      : { println "Found $count files in $it.name totalling $totalSize bytes"
		                        totalSize = 0; count = 0 },
		        postRoot     : true
		        sort         : sortByTypeThenName
		) {it -> totalSize += it.size(); count++ }