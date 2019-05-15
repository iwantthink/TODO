# Groovy与Java的差异
[Differences with Java](http://www.groovy-lang.org/differences.html)


# 1. 默认导入
默认导入如下包和类(不再需要手动使用`import`)：

- `java.io.*`

- `java.lang.*`

- `java.math.BigDecimal`

- `java.math.BigInteger`

- `java.net.*`

- `java.util.*`

- `groovy.lang.*`

- `groovy.util.*`

# 2. Multi-methods

**在Groovy中，在运行时才决定哪个方法被执行，这被称为`Multi-methods` 或 运行时路由(`runtime dispatch`)，这意味着将在运行时根据参数类型选择具体执行的方法**

- **在Java中，方法在编译时期根据声明的类型进行选择**。


## 2.1 示例

如下代码可以在java或groovy中执行，但是结果会不同：

	int method(String arg) {
	    return 1;
	}
	int method(Object arg) {
	    return 2;
	}
	Object o = "Object";
	int result = method(o);

- 在Java环境下`assertEquals(2, result);`,这是因为Java使用静态信息类型，对象`o` 被声明为Object，实际传递给方法的参数时`Object`类型

- 在Groovy环境下 `assertEquals(1, result);`，这是因为Groovy中，当方法被调用时会在运行时期决定，实际传递给方法的参数是`String`类型

# 3. 数组初始化

**在Groovy中,`{.....}`被保留用作定义闭包**

这意味着，以下的语法无法用来创建数组：

	int [] arrary = {1,2,3,4}

实际上需要使用如下语法：

	int [] array = [1,2,3,4]

# 4. Package scope visibility

在Java中，字段上省略修饰符会导致该字段成为**包私有字段**

	class Person {
	    String name
	}

在Groovy中，上述语法行为会创建一个属性，也就是说一个附带关联的`getter`和`setter`方法
的私有字段

**可以通过`@packageScope`创建包专用字段**

	class Person {
	    @PackageScope String name
	}

# 5. ARM blocks

Groovy并不支持从Java 7开始的ARM(`Automatic Resource Management`)块.但是Groovy提供了依赖于闭包的方法实现同样的效果


**Java实现方式:**

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

- 在这个例子中,数据流会在try块执行完毕后被自动关闭,**前提是:这些可关闭的资源必须实现`java.lang.AutoCloseable`接口**

- ARM是从`Java 7 build 105 `版本开始,Java 7 的编译器和运行环境支持新的`try-with-resources`语句,称为ARM block(Automatic Resource Management),自动化资源管理 

**Groovy实现方式：**

	new File('/path/to/file').eachLine('UTF-8') {
	   println it
	}

或者使用更接近Java的形式：

	new File('/path/to/file').withReader('UTF-8') { reader ->
	   reader.eachLine {
	       println it
	   }
	}

# 6. 内部类

**匿名内部类和嵌套类的实现遵循Java模式**. 这种实现与`groovy.lang.Closure`的实现相似,有好处也有一些区别, 例如闭包访问私有的字段和方法会存在问题,但是好处是局部变量不必一定被`final`修饰

## 6.1 静态内部类

静态内部类的例子：

	class A {
	    static class B {}
	}
	
	new A.B()

- 如果必须要使用内部类，请使用静态内部类，因为Groovy对静态内部类的支持最好

## 6.2 匿名内部类

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

## 6.3 创建非静态内部类的实例

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

- **警告**: **Groovy支持在调用一个参数的方法时,不给出参数,该参数会默认设置为`null`. 这种规则在调用构造函数时同样适用**

	There is a danger that you will write new X() instead of new X(this) for example. Since this might also be the regular way we have not yet found a good way to prevent this problem.

# 7. Lambdas
Java 8 支持 lambdas和方法引用

	// lambdas
	Runnable run = () -> System.out.println("Run");
	// 方法引用
	list.forEach(System.out::println);

Groovy不支持这种语法，但是提供了闭包:

	Runnable run = { println 'run' }
	list.each { println it } // or list.each(this.&println)

# 8. GStrings

被双引号包括的字符串会被认为是`GString`类型，如果`GString`带有`$`符号，可以会造成编译错误或生成不同的值

通常，如果接口上明确定义了参数类型，Groovy会自动转换`GString`和`String`

- 注意Java API中接收`Object`类型作为参数的方法,检查其实际类型

# 9. String and Character literals
Groovy中被单引号包括的字符用来表示`String`,被双引号包括的字符可以表示`String`或`GString`(具体取决于表达式中是否存在插值)

	assert 'c'.getClass()==String
	assert "c".getClass()==String
	assert "c${1}".getClass() in GString

Groovy只有在为`char`类型的变量赋值时才会自动将单个字符串转换为`char`

- **当调用一个参数为`char`类型的方法时，需要明确地转换类型 或 确保参数已经提前被转换了类型**

		char a ='a'
		assert Character.digit(a, 16)==10 : 'But Groovy does boxing'
		assert Character.digit((char) 'a', 16)==10
		
		try {
		 // 报错
		  assert Character.digit('a', 16)==10
		  assert false: 'Need explicit cast'
		} catch(MissingMethodException e) {
		}

**Groovy支持俩种转换风格(直接添加`(char)`或添加 `as char`)，这俩种风格在转换`String`(拥有多个字符)至`char`时会有差异.**

- `Groovy`风格的转换会使用多个字符的字符串的第一个字符

		// for single char strings, both are the same
		assert ((char) "c").class==Character
		assert ("c" as char).class==Character
	
- `C`风格的转换在转换拥有多个字符的字符串到`char`时会直接报错	
	
		// for multi char strings they are not
		try {
		  ((char) 'cx') == 'c'
		  assert false: 'will fail - not castable'
		} catch(GroovyCastException e) {
		}
		assert ('cx' as char) == 'c'
		assert 'cx'.asType(char) == 'c'


- `(char)`形式进行强转,会抛出异常


# 10. Primitives and wrappers

**Groovy使用对象处理一切事物**，**原始类型会被自动包装为引用**。**因此，Groovy不遵循Java的扩展(`widening `)优先于拆箱(`boxing`)的行为:**

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

- 方法1，是java会调用的，因为扩展优先于拆箱

- 方法2，是Groovy会调用的,因为所有的原始引用都使用了它们自身的包装类

# 11. `==`的行为

**在Java中`==`意味着原始类型相等或对象地址相同**

**在Groovy中,如果`==`俩边是可比较的对象，那么`==`翻译为`a.compareTo(b)==0`,否则被翻译为`a.equals(b)`**

**如果要确定对象的引用是否相同，可以使用 `is`。[Groovy==和equals](http://blog.csdn.net/hivon/article/details/2291559)**

# 12. Conversions

## 12.1 Java 转换

Converts from|boolean|byte|short|char|int|long|float|double
---|---|---|---|---|---|---|---|---|---|---
boolean|-||N|N|N|N|N|N|N
byte|N|-|Y|C|Y|Y|Y|Y
short|N|C|-|C|Y|Y|Y|Y
char|N|C|C|-|Y|Y|Y|Y
int|N|C|C|C|-|Y|T|Y
long|N|C|C|C|C|-|T|T
float|N|C|C|C|C|C|-|Y
double|N|C|C|C|C|C|C|-

- `Y`表示Java自动转换，`C`表示需要指定明确的类型转换，`T`表示数转换过程中有数据丢失，`N`表示Java不能进行转换


## 12.2 Groovy转换

...太长了..从原网页查看吧!

# 13. Groovy中额外保留的关键字

- as  

- def  

- in  

- trait   
