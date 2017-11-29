# Groovy介绍
- Groovy是一种动态语言，运行于JVM。定义：Groovy是在java平台上的，具有像Python,Ruby和smalltalk 语言特性的灵活动态语言

- Groovy类似于脚本(shell)的存在，执行Groovy脚本时，Groovy会先将其编译为java类字节码，然后通过JVM 执行这个java类

- Groovy 除了使用JDK之外，还可以使用[GDK-API](http://www.groovy-lang.org/api.html)

- 参考链接：

	[Groovy官方文档](http://www.groovy-lang.org/documentation.html)

# 1.与Java的差异

## 1.1 default imports
默认导入如下包和类：

- java.io.*

- java.lang.*

- java.math.BigDecimal

- java.math.BigInteger

- java.net.*

- java.util.*

- groovy.lang.*

- groovy.util.*

## 1.2 Multi-methods
在Groovy中，在运行时才决定哪个方法被执行，这被称为Multi-methods 或 runtime dispatch，这意味着方法将在运行时根据参数类型被选择。在Java中，方法在编译时期根据声明的类型进行选择。

**举个栗子**，如下代码可以在java或groovy中执行，但是结果会不同：

	int method(String arg) {
	    return 1;
	}
	int method(Object arg) {
	    return 2;
	}
	Object o = "Object";
	int result = method(o);

- 在Java环境下`assertEquals(2, result);`,这是因为Java使用静态信息类型，对象o 被声明为Object，实际调用方法的类型是Object.

- 在Groovy环境下 `assertEquals(1, result);`，这是因为Groovy中，当方法被调用时会在运行时期决定，实际调用方法的参数是String类型。

## 1.3 Array initializers
在Groovy中,`{.....}`被保留用作closures

以下的语法无法用来创建数组：

	int [] arrary = {1,2,3,4}

实际上需要使用如下语法：

	int [] array = [1,2,3,4]

## 1.4 Package scope visibility
在Java中，字段上省略修饰符会将该字段修饰成包私有字段

	class Person {
	    String name
	}

在Groovy中，上述语法行为会创建一个属性，也就是说被声明为一个私有字段，并且会提供关联的getter和setter方法

可以通过`@packageScope`创建包专用字段

	class Person {
	    @PackageScope String name
	}

## 1.5 ARM blocks
Java 中的ARM(Automatic Resource Management)block 在Groovy中并不支持。Groovy中用依赖于closures的方法实现同样的效果.

	Path file = Paths.get("/path/to/file");
	Charset charset = Charset.forName("UTF-8");
	try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
	    String line;
	    while ((line = reader.readLine()) != null) {
	        System.out.println(line);
	    }
	
	} catch (IOException e) {
	    e.printStackTrace();
	}

Groovy实现方式：

	new File('/path/to/file').eachLine('UTF-8') {
	   println it
	}

或者使用更接近Java的形式：

	new File('/path/to/file').withReader('UTF-8') { reader ->
	   reader.eachLine {
	       println it
	   }
	}

## 1.6 Inner classes
匿名内部类和嵌套类遵循Java规则，but you should not take out the Java Language Spec and keep shaking the head about things that are different. The implementation done looks much like what we do for groovy.lang.Closure, with some benefits and some differences. Accessing private fields and methods for example can become a problem, but on the other hand local variables don’t have to be final.

### 1.6.1 Static inner classes
静态内部类的例子：

	class A {
	    static class B {}
	}
	
	new A.B()

- 静态内部类的使用是最支持的，如果必须要使用内部类，请使用静态内部类。

### 1.6.2 Anonymous Inner Classes

	import java.util.concurrent.CountDownLatch
	import java.util.concurrent.TimeUnit
	
	CountDownLatch called = new CountDownLatch(1)
	
	Timer timer = new Timer()
	timer.schedule(new TimerTask() {
	    void run() {
	        called.countDown()
	    }
	}, 0)
	
	assert called.await(10, TimeUnit.SECONDS)

### 1.6.3 Creating Instances of Non-Static Inner Classes
Java实现方式：

	public class Y {
	    public class X {}
	    public X foo() {
	        return new X();
	    }
	    public static X createX(Y y) {
	        return y.new X();
	    }
	}

Groovy不支持`y.new X()`这种语法，作为替代，必须使用`new X(y)`：

	public class Y {
	    public class X {}
	    public X foo() {
	        return new X()
	    }
	    public static X createX(Y y) {
	        return new X(y)
	    }
	}

- Caution though, Groovy supports calling methods with one parameter without giving an argument. The parameter will then have the value null. Basically the same rules apply to calling a constructor. There is a danger that you will write new X() instead of new X(this) for example. Since this might also be the regular way we have not yet found a good way to prevent this problem.

## 1.7 Lambdas
Java 8 支持 lambdas和方法引用

	Runnable run = () -> System.out.println("Run");
	list.forEach(System.out::println);

Groovy不支持这种语法，但是提供了closures:

	Runnable run = { println 'run' }
	list.each { println it } // or list.each(this.&println)

## 1.8 GStrings
被双引号包括的字符串会被认为是GString类型，如果GString带有$符号，可以会造成编译错误或生成不同的值。

正常情况下，如果一个API声明了参数类型为GString和String，俩者会被自动转换。注意Java API中接收的对象类型。

## 1.9 String and Character literals
Groovy中被单引号包括的字符用来表示String,被双引号包括的字符可以表示String或GString(具体取决于插值)

	assert 'c'.getClass()==String
	assert "c".getClass()==String
	assert "c${1}".getClass() in GString

Groovy中当字段被声明为`char`类型，会自动转换单个字符的String类型字符串为`char`类型。

	char a='a'

当调用一个参数为`char`类型的方法时，需要明确地转换类型 或 确保参数已经提前被转换了类型。

	assert Character.digit(a, 16)==10 : 'But Groovy does boxing'
	assert Character.digit((char) 'a', 16)==10
	
	try {
	  assert Character.digit('a', 16)==10
	  assert false: 'Need explicit cast'
	} catch(MissingMethodException e) {
	}

Groovy支持俩种转换风格(直接添加`(char)`或添加 `as char`),俩种风格在转换多个字符时会有差异

	// for single char strings, both are the same
	assert ((char) "c").class==Character
	assert ("c" as char).class==Character
	
	// for multi char strings they are not
	try {
	  ((char) 'cx') == 'c'
	  assert false: 'will fail - not castable'
	} catch(GroovyCastException e) {
	}
	assert ('cx' as char) == 'c'
	assert 'cx'.asType(char) == 'c'

## 1.10 Primitives and wrappers
Groovy使用对象处理一切事物，会自动包装原始类型。Because of this, it does not follow Java’s behavior of widening taking priority over boxing. Here’s an example using int

	int i
	m(i)
	//1
	void m(long l) {           
	  println "in m(long)"
	}
	//2
	void m(Integer i) {        
	  println "in m(Integer)"
	}

- m方法1，是java会调用的，since widening has precedence over unboxing.

- m方法2，是Groovy会调用的,因为原始类型引用使用了其包装类

## 1.11 Behaviour of ==
在Java中`==`意味着对象的引用类型相同，`equals`判断字符串的值是否相同。 在Groovy中`==`翻译为`a.compareTo(b)==0`,相当于`equals`方法

如果要确定对象的引用是否相同，可以使用 `is`。[Groovy==和equals](http://blog.csdn.net/hivon/article/details/2291559)

## 1.12 Conversions

## 1.13 Extra keywords

- as  
- def  
- in  
- trait   

# 2 The Groovy Delelopment Kit
例如:File,List,Map,
[GDK使用说明](http://www.groovy-lang.org/groovy-dev-kit.html)

# 3.基础知识

- **建议使用API 之前先去查看文档**

- groovyConsole(open GroovyConsole),ctrl+w(clear output window),ctrl+r(run groovy code)

- Groovy注解标记和Java一样,支持`//` 单行注释,`/*content*/`多行注释 和GroovyDoc评论`/**content*/`(GroovyDoc评论可以添加@param,@return等)

- shebang line:UNIX系统支持的一种特殊单元注释，用于指明脚本的运行环境，这样就可以直接在终端中使用`xxx.groovy`运行，而不用像`groovy xxx.groovy`
		
		#!/usr/bin/env groovy
		println "Hello from the shebang line"

- Groovy标识符可以以字母，下划线，$符号开头，但是不能以数字开头

- 转义字符:`\` 反斜杠

	- 对于键盘上不存在的符号，可以使用unicode转义序列:`\+u+4个十六进制数字`

- Groovy可以不用分号`;` 结尾

- Groovy**支持动态类型**,即定义变量的时候可以不指定其具体类型（也可以指定具体类型)

- Groovy中定义变量可以使用关键词 **def**.但是实际上def 也不是必须的！只是为了代码清晰，建议还是加上**def**


- 函数定义时,参数的类型也可以不指定，例如:
		String func(arg1,arg2){
			....etc
		}

- Groovy中函数的返回值也可以是无类型的，**但是无类型的函数必须用def声明,或者用viod声明**,例如：
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
	    
- getName()如果不添加括号,Groovy会认为getName是一个变量

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

- 引用标识符，Groovy在dotted expression 后面可以使用引号标识符，例如map.a 可以表示为map.'a'或map."a".同时引号中可以包含Java中不支持的空格，减号`-`。
		
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

## 3.1 字符串
### 3.1.1 单引号`'content'`
内容严格对应Java中的String，不对`$`符号进行转义

	def name = 'ryan'
	def str = 'i am $name'
	assert str == 'i am $name'

单引号的字符串可以通过+运算符连接

	assert 'ab' == 'a'+'b'

### 3.1.2 双引号`"content"`

- 双引号字符串如果没有插值表达式，那么其类型是String,否则类型是GString。GString调用toString()方法之后 其类型就是String

- 如果字符串中有`$`或`${}`占位符，会对 $表达式 先求值，当Gstring调用`toString()`时，占位符表达式的值会被计算出来。

		def name = 'ryan'
		def str = "i am $name"
		assert str == 'i am ryan'
		def str2 = "i am ${1>2?'boy':'girl'}"

- 占位符`${ }`之内允许添加任意表达式，其返回值根据最后一句

- 当占位符中包含一个箭头时`${->}`,该表达式实际是一个闭包表达式，可以将其视为一个前缀为`$`的闭包,另外`${->}`比纯粹的`${}`有一个优势，就是lazy evalution

		def number1 = 123.456	
		def eagerGstring = "value = ${number1}"
		def lazyGstring = "value = ${->number1}"
		assert eagerGstring == "value = 123.456"
		assert lazyGstring  == "value = 123.456"
		number1 = 2
		assert eagerGstring == "value = 123.456"
		assert lazyGstring == "value = 2"

- Gstring中，使用闭包表达式时，不允许有多个参数

- 期望一个String类型的参数时，传入一个Gstring类型的参数，Groovy会自动调用toString()
		def number = 1
		def msg = "hello ${number1}"
		assert msg instanceof GString
		assert getString(msg1) instanceof String

- GString 和String的hashCode()不同，所以尽量避免使用GString作为Map的key

		def param = 'abc'
		assert "hello $param".hashCode()!="hello abc".hashCode()
		def msg = "hello $param"
		assert msg.hashCode()!="hello abc".hashCode()

### 1.1.3 三重引号 ` ```content ```  `
内容支持随意换行,类似于双引号字符串，不支持插值，区别是支持多行，并且在三重双引号中，**单引号和双引号 不需要转义**

	def multieLine = ``` begin  
	line1  
	line2  
	end ```

### 1.1.4 斜线字符串
- 适用于定义正则表达式和patterns,不需要转义反斜线（允许不转义的带上反斜线）

	def slashy = /\.*hello*./
	def str = "\\"

- 斜线字符串支持多行形式

- 斜线字符串支持占位符`$`

- 一个内容为空的斜线字符串，Groovy会认为这是一个注解标志

### 1.1.5 `$//$`格式的字符串
- 在其中的字符串不需要转义 `$`和斜线`/`,支持多行，与GString类似

- 通过 `$`符号进行转义，可以转义 `$`和 斜线`/`

- 正斜杠`/`不需要转义，反斜杠需要转义`\`

- `$`符号可以做占位符，和字符串一起使用时需转义


	def name = "Guillaume"
	def date = "April, 1st"

	def dollarSlashy = $/
    
    Hello $name,
    today we're ${date}.
    /
    $ dollar sign
    $$ escaped dollar sign
    \ backslash
    / forward slash
    $/ escaped forward slash
    $$$/ escaped opening dollar slashy
    $/$$ escaped closing dollar slashy
	/$
	println dollarSlashy


## 1.2 数据类型
- java中的基本类型
- Groovy中的容器类
- 闭包

### 1.2.1 基本数据类型
- 作为动态语言，Groovy世界中的所有事物都是对象。所以，int，boolean这些Java中的基本数据类型，在Groovy代码中其实对应的是它们的包装数据类型。比如int对应为Integer，boolean对应为Boolean。

		def int x = 1
		println x.getClass().getCanonicalName()// java.lang.Integer

- 原始类型： byte,char,short,int,long 无限精度：java.lang.BigInteger

- 使用`def`定义整数，变量的类型会适应这个整数值
		
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

- 定义二进制，八进制，十六进制

		//二进制   0b 前缀
		int xInt2 = 0b11
		assert xInt2 == 3

		//八进制 0前缀 后面跟八进制数字
		int xInt8 = 077
		assert xInt8 == 63

		//十六进制 0x 前缀
		int xInt16 = 0x3a
		assert xInt16 == 58

- 允许在数字中使用下划线`_` 增加数字可阅读性
	long reditCardNumber = 123456_789
	assert reditCardNumber == 123456789

- 可以通过添加后缀，指定数字的类型
		// 可以通过添加后缀 指定数字类型
		// BigInteger G or g
		// Long L or l
		//Integer I or i
		//BigDecimal G or g
		//Double D or d
		//Float F or f
		assert 1i.class == Integer
		assert 1i.class != Long

- byte char short 和int 进行计算，结果是int类型。byte char short int 和long进行计算，结果是long类型...更多的去查官方文档

		byte var1 = 127
		char var2 = 'A'
		short var3 = 456
		int var4 = 1
		long var5 = 123

		assert (var1 + var4).class == Integer
		assert (var2 + var4).class == Integer
		assert (var3 + var4).class == Integer


- **Groovy不提供专用的整除运算符号**！只能通过`intdiv()`函数
		
		assert 6.intdiv(5)==1
			
- 如果 在除法中 存在一个 float 或 double类型的数据，那么结果就是Double类型 。否则结果都是BigDecimal类型

		assert (4/3).class == BigDecimal
		assert (4d/3).class == Double
		assert (4f/3).class == Double
		assert (4l/3).class == BigDecimal

- 次方运算符号是`**` ，表达式： `基数**指数`

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


- 布尔值:true 或false




### 1.2.2 容器类
Groovy中容器类有三种:

- List:链表,其底层对应Java中的List接口，一般用ArrayList作为真正的实现类.除非使用as指定了类型
- Map:键-值表，底层对应java中的LinkedHashMap
- Range:范围，是List的一种拓展
- Arrays:数组，必须得指定类型

使用介绍：  

1. List由`[]`定义，其元素可以是任何对象。变量存取可以直接通过索引存取，而且不用担心索引越界，如果索引超过当前链表长度，List会自动往该索引添加元素
		
		def aList = [1,'2',true]
		assert aList[1]=='2'
		assert aList[5]==null
		aList[11]= 11
		assert aList[11] ==11
		assert aList.size == 11

	- 可以通过`<<`leftshift 操作符往List末尾添加一个数据
			def aList = [1,2,3]
			aList<<4
			assert aList.size() == 4
	- List还可以包含另外一个List
			def multi = [[0,1],[2,3]]
			assert multi[1][1]==3	 

2. Map由`[:]`定义，冒号左边是key，右边是value。key建议是字符串，value可以是任何对象。另外key可以用单引号或双引号包裹，也可以不包裹
  
		def aMap = ['key1':'value1','key2':'value2']
		aMap.keyName //取值方式1
		aMap.['keyName']//取值方式2
		assert aMap.yellow == null//取不存在的值会返回null
		aMap.anotherkey = 'i am map'//添加新元素，anotherkey是key的名称

	- 可以使用String或int 作为key，但是key类型为int时，取值不能直接用`.key`，而必须使用`map[key]`。另外添加新的key时，也可以是数字 但是需要用`''`包裹

			aMap.'3' = 2
			assert aMap.containsKey('3')

	- 如果使用一个变量的name作为key，那么会把这个name当做key，而不是这个name对应的内容.可以通过添加`()`括号来使用其对应内容当做key。
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
			
3. Range类型的变量，由`begin值+俩个点+end值` 组合表示,如果不想包含最后一个值，可以在`end值`前添加一个`<`符号
		def aRange = 1..5//包含1,2,3,4,5
		def aRangeWithOutEnd = 1..<5 //包含1,2,3,4
		aRange.from
		aRange.to

4. Arrays类型,可以使用多重数组
		def arrays1 = [1,2,3]
		Integer [] arrays2 = [1,2,3]
		assert arrays1 instanceof ArrayList
		assert arrays2 instanceof Integer[]

		def mult = new Integer[2][3]

### 1.2.3 闭包Closure
- 闭包,是一种数据类型，是一段可执行的代码

- 语法：`{ [closureParameters->] statements   }`,中括号可选填，但是当有参数时  -> 是必须的

- 当Closure作为一个对象时,闭包 是groovy.lang.Closure 类的一个实例，所以可以作为任何其他变量被分配

		def listener  ={println it}
		assert listener instanceof Closure

- 如果不使用def 定义 Closure的话 ， 可以使用Closure  

		Closure listener2 = {println "hi $it"}
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
		func(1){println 'param ...'}

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
        		def nestedClosure = {
            		def cl = {owner}
            		cl()
        		}
       		assert nestedClosure()==nestedClosure
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

## 1.3 Groovy的表现形式
Groovy支持class形式和script形式

- Groovy可以像java那样填写package 然后写类
- 可以通过import 添加其他包下的类
- Groovy默认的类以及变量 默认都是public的

#### 1.3.1.1 什么是脚本？ 
- groovy文件只要不是和java一样的去定义class，那就是一个脚本

- 可以通过`grooyc -d classes test.groovy`将groovy文件转换成class文件,`-d path`是设置class文件的存储位置

- groovy脚本会被转换成了一个类，它从script派生。

- **每一个脚本都会生成一个static main函数**。这样，当`groovy test.groovy`的时候，其实就是用java去执行这个main函数

- 脚本中的所有代码都会放到run函数中。比如，println 'Groovy world'，这句代码实际上是包含在run函数里的。

- 如果脚本中定义了函数，则函数会被定义在类中。

#### 1.3.1.2 脚本中的变量和作用域
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

#### 1.3.1.3 表现形式
- script 形式1 
		//Main2.Groovy Script的一种形式 ，无需声明它
		println "hello groovy "

- Script 的另一种表现形式
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

### 1.3.2 class形式
	class Main{
    	static void main(String... args){
        	println 'hello groovy'
    	}
	}


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

## 1.6 groovy中包的使用

- groovy需要在类定义之前指定包，否则使用默认包
		package com.pkg

- import 手动导入包
		import groovy.xml.MarkupBuilder

- Groovy会默认导入一些包
		import java.lang.*
		import java.util.*
		import java.io.*
		import java.net.*
		import groovy.lang.*
		import groovy.util.*
		import java.math.BigInteger
		import java.math.BigDecimal	

- 通过`*`通配符导入,表示导入包中所有的类
		import groovy.xml.*

- Groovy 允许静态导入，相当于把方法当做自己类中的静态方法使用。
		import static Boolean.FALSE
		assert !FALSE


- Groovy 的静态导入与java相似 ，但是更加的动态，Groovy允许你的类中定义和 静态导入的方法拥有同样的名字，只需要俩者有不同的参数要求。这在java中是不被允许的 ，但是 groovy是允许的

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

- 可以使用as对包名设置别名
		import static Calendar.getInstance as now
		assert now().class == Calendar.getInstance().class

		import java.util.Date as jud
		assert new jud() instanceof java.util.Date

# 2 Groovy的功能

## 2.1 遍历文件夹
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