# Kotlin学习


# 1. 同一性??
**当需要可空引用时，像数字、字符会被装箱。装箱操作不会保留同一性**

        val b: Int = 123
        lll(b === b) // 输出true
        val b2: Int? = b
        val b22: Int? = b
        lll(b2 === b22) // 输出true

        val a: Int = 10000
        lll(a === a) // 输出“true”
        val boxedA: Int? = a
        val anotherBoxedA: Int? = a
        lll(boxedA === anotherBoxedA) // 输出false
        
# 2. 浮点数比较

当其中的操作数a与b都是静态已知的Float或Double或者它们对应的可空类型（声明为该类型，或者推断为该类型，或者智能类型转换的结果是该类型），两数字所形成的操作或者区间遵循 IEEE754 浮点运算标准。

然而，为了支持泛型场景并提供全序支持，当这些操作数并非并非静态类型为浮点数（例如是Any、Comparable<......>、类型参数）时，这些操作使用为Float与Double实现的不符合标准的equals与compareTo，这会出现：

- 认为NaN与其自身相等

- 认为NaN比包括正无穷大（POSITIVE_INFINITY）在内的任何其他元素都大

- 认为-0.0小于0.0


# 3. Unicode转义语法

`\uFF00`


# For循环

for 可以循环遍历任何提供了迭代器的对象,需要满足如下条件

- 有一个成员函数或者扩展函数 `iterator()`

- 成员函数或者扩展函数的返回类型中需要满足如下条件:

	- 有一个成员函数或者扩展函数 `next()`

	- 有一个成员函数或者扩展函数`hasNext()`返回 Boolean

**这三个函数都需要被标记为`operator`**

# 4. 继承

如果派生类有一个主构造函数，其基类型可以（并且必须）用基类的主构造函数参数就地初始化


## 4.1 覆盖规则
在 Kotlin 中，实现继承由下述规则规定：**如果一个类从它的直接超类继承相同成员的多个实现，它必须覆盖这个成员并提供其自己的实现（也许用继承来的其中之一）**。为了表示采用从哪个超类型继承的实现，我们使用由尖括号中超类型名限定的super，如`super<Base>`


# 5. 对象表达式和对象声明的差异

1. 对象表达式在被使用的地方立即执行(初始化)

2. 对象声明是在第一次被访问到时延迟初始化

3. **伴生对象的初始化是在相应的类被加载时(解析),与Java静态初始化块的语义相匹配**

# 6. 声明属性的完整语法

	var <propertyName>[: <PropertyType>] [= <property_initializer>]    
		[<getter>]    
		[<setter>]
		
## 6.1 幕后字段
[Kotlin 什么是幕后字段?](https://juejin.im/post/5b95321ae51d450e6475b7c6)

**在Kotlin中, 如果属性至少一个访问器使用默认实现，那么Kotlin会自动提供幕后字段，用关键字field表示，幕后字段主要用于自定义getter和setter中，并且只能在getter 和setter中访问**

有幕后字段的属性转换成Java代码一定有一个对应的Java变量


## 6.2 幕后属性

一个属性如果对外表现为只读，对内表现为可读可写，我们将这个属性成为幕后属性

	private var _table: Map<String, Int>? = null
	public val table: Map<String, Int>
	    get() {
	        if (_table == null) {
	            _table = HashMap() // 类型参数已推断出
	        }
	        return _table ?: throw AssertionError("Set to null by another thread")
	    }

- `_table`就是幕后属性


# 7. 修饰符

	public internal protected private

## 7.1 internal

可见性修饰符`internal`意味着该成员只在相同模块中可见

- 一个模块就是编译在一起的一套Kotlin文件

	1. 一个Intellij IDEA模块

	2. 一个Maven项目

	3. 一个Gradle源集(例外是`test`源集可以访问`main`源集)

	4. 一次`<kotlinc>` Ant任务执行所编译的一套文件

# 8. 扩展

扩展函数是静态分发的，即它们不是根据接收者类型的虚方法。 这意味着调用的扩展函数是由函数调用所在的表达式的类型来决定的， 而不是由表达式运行时求值结果决定的。例如：

	open class C
	
	class D: C()
	
	fun C.foo() = "c"
	
	fun D.foo() = "d"
	
	fun printFoo(c: C) {
	    println(c.foo())
	}
	
	printFoo(D())
	// 输出 c


## 8.1 扩展接收者的类型

声明为成员的扩展可以声明为 open 并在子类中覆盖。这意味着这些函数的分发对于分发接收者类型是虚拟的，但对于扩展接收者类型是静态的。

## 8.2 接收者概念

扩展接收者

- 就是调用扩展方法的对象,当扩展接收者和分发接收者

分发接收者

- 当一个扩展方法位于另外一个类中时，分发接收者就是指的这个类，可以通过`this@Outer`进行访问



# 9. 密封类

[Kotlin——中级篇（六）：数据类（data）、密封类(sealed)详解- 掘金](https://juejin.im/post/5a37e4b45188253aea1f7219)

所谓受限的类继承结构，即当类中的一个值只能是有限的几种类型，而不能是其他的任何类型


# 10. 泛型
[Kotlin中的泛型](https://juejin.im/post/5be40974f265da615a414f54)

[【码上开学】Kotlin 的泛型](https://juejin.im/post/5d6c6636f265da03c8153a03)

使用泛型的时候加上的类型参数，会在编译器在编译的时候去掉，这个过程就称为类型擦除

- Java提供了**泛型通配符(`? extends`和`? super`)来解决这个问题**


## 10.1 Java类型通配符`?`

	Number num = new Integer(1);  
	ArrayList<Number> list = new ArrayList<Integer>(); //type mismatch

**`Interger`是`Number`的子类，这是Java多态的特性，但是`ArrayList<Integer>`并不是`ArrayList<Number>`的子类，这时就需要一个引入通配符，来表示一个引用 既可以是当前类 又可以是其父类**

- Java中的通配符用`?`表示

	通配符`?`可以认为是任意类型的父类,它是一个具体的类型，是泛型实参，与泛型形参(`T`,`E`等)不同
	
- 引入通配符，不仅解决了泛型实参之间的逻辑关系，还对泛型引入了边界的概念

- 在Java中，通配符`?`单独使用时，其实就是`<? extends Object>`的缩写


### 10.1.1 Java 通配符`?`的上界
	List<? extends Number> list = new ArrayList<Number>();

- **表示类或者方法接收T或者T的子类型**

- **定义通配符`?`的上界又可以称为协变**

- 除此之外，`? extends`有俩层意思

	1. `?`是一个通配符，表示类的泛型类型是未知类型

	2. `extends`限制了这个未知类型，为其定义了上界(包括上界本身)

### 10.1.2 Java 通配符`?`的下界

	 List<? super Integer> list = new ArrayList<Number>();

- 表示类或者方法接收T或者T的父类型

- **定义通配符`?`的下界，又称为逆变**

- `? super`有俩层意思

	1. 通配符`?`表示类的泛型类型是未知类型

	2. `super`限制了这个未知类型，为其定义了下界(包括下界本身)

### 10.1.3 小结

小结:**Java 的泛型本身是不支持协变和逆变的**

Java中可以使用泛型通配符 `? extends` 来使泛型支持协变，但是「只能读取不能修改」，这里的修改仅指对泛型集合添加元素，如果是 `remove(int index)` 以及 clear 当然是可以的

	List<? extends TextView> textViews = new ArrayList<TextView>(); // 👈 本身
	List<? extends TextView> textViews = new ArrayList<Button>(); // 👈 直接子类
	List<? extends TextView> textViews = new ArrayList<RadioButton>(); // 👈 间接子类

	List<? extends TextView> textViews = new ArrayList<Button>();
	// 具体解释查看下面的注解
	TextView textView = textViews.get(0); // 👈 get 可以
	textViews.add(textView);
	// 👆 add 会报错，no suitable method found for add(TextView)

- `List<? extends TextView>`的泛型类型是一个未知类型,编译器也不知道是什么类型，只知道它有一个上界

	在get操作时，由于它必须满足`? extends TextView`这个限制条件，因此get操作得到的对象必定是`TextView`的子类型,根据Java多态，这个对象可以被赋值给`TextView`(**但是变量类型不可以是`TextView`子类，变量类型必须是其上界**)

	在add操作时,编译器无法确定泛型类型到底是什么？(它可以是`List<TextView>`或者`List<Button>`,只要满足限制条件即可)正因为无法确定具体的泛型类型，所以无法将特定的类赋值给特定的泛型(例如`List<? extends TextView>`的实际类型可能是`ArrayList<Button>`,那么将`TextView`赋值给它就会出错   )！！

- **正是因为add操作的限制,协变的泛型只能向外提供数据被消费，因此被称为生产者**

Java中可以使用泛型通配符 `? super` 来使泛型支持逆变，但是「只能修改不能读取」，这里说的不能读取是指不能按照泛型类型读取，你如果按照 Object 读出来再强转当然也是可以的

		List<? super Button> buttons = new ArrayList<Button>(); // 👈 本身
		List<? super Button> buttons = new ArrayList<TextView>(); // 👈 直接父类
		List<? super Button> buttons = new ArrayList<Object>(); // 👈 间接父类

		List<? super Button> buttons = new ArrayList<TextView>();
		Object object = buttons.get(0); // 👈 get 出来的是 Object 类型
		Button button = ...
		buttons.add(button); // 👈 add 操作是可以的

- `List<? super Button>`的泛型类型是一个未知类型！编译器不知道其具体类型，只知道它有一个下界

	在add操作时，由于它必须满足`? super Button`这个限制条件，因此无论具体类型是`ArrayList<Button>`或`ArrayList<TextView>`,它们都是Button的父类，因此`Button`肯定可以被添加(**但是`Button`的父类就无法被添加，被添加的类的类型必须是下界**)
	
	在get操作时,编译器无法确定泛型类型到底是什么?(它可以是`List<TextView>`或`List<Button>`,只要满足限制条件即可)，正因为无法确定具体泛型类型，所以无法根据泛型确定返回值类型(例如,`List<? super Button>`的实际类型可能是`ArrayList<Button>`或`ArrayList<TextView>`,那么在get操作时，返回的可能是`Button`或`TextView`,因此只能用俩者共同的父类Object)

## 10.2 Java 什么时候使用协变和逆变

在《Effective Java》中给出了一个PECS原则：

	PECS：Producer extends,Customer super

- **当使用泛型类作为生产者，需要从泛型类中取数据时，使用`? extends`，此时泛型类是协变的**

	**当使用泛型类作为消费者，需要往泛型类中写数据时，使用`? suepr`，此时泛型类是逆变的**

一个经典的案例就是Collections中的copy方法

    public static <T> void copy(List<? super T> dest, List<? extends T> src) {
        int srcSize = src.size();
        if (srcSize > dest.size())
            throw new IndexOutOfBoundsException("Source does not fit in dest");

        if (srcSize < COPY_THRESHOLD ||
            (src instanceof RandomAccess && dest instanceof RandomAccess)) {
            for (int i=0; i<srcSize; i++)
                dest.set(i, src.get(i));
        } else {
            ListIterator<? super T> di=dest.listIterator();
            ListIterator<? extends T> si=src.listIterator();
            for (int i=0; i<srcSize; i++) {
                di.next();
                di.set(si.next());
            }
        }
    }


## 10.3 Kotlin中的协变和逆变
Kotlin的泛型本身也是不可变的，但是借助`out`和`in`可以实现协变和逆变(统称型变)

- 关键字`out`表示协变(等同于Java中的上界通配符`? extends T`),即该类型仅从类中返回(生产)，并不被消费(**只能被读取，不能被修改**)

		class Producer<T> {
		    fun produce(): T {
		        ...
		    }
		}
		
		val producer: Producer<out TextView> = Producer<Button>()
		val textView: TextView = producer.produce() // 👈 相当于 'List' 的 `get`


- 关键字`in`表示逆变(等同于Java中的下界通配符`? super T`).即该类型仅从外部传入类中,并不生产（**只能被修改，不能被读取**）

		class Consumer<T> {
		    fun consume(t: T) {
		        ...
		    }
		}
		
		val consumer: Consumer<in Button> = Consumer<TextView>()
		consumer.consume(Button(context)) // 👈 相当于 'List' 的 'add'


小结:

- **上面介绍的是Kotlin中协变和逆变在使用处的型变(即类型投影)**


### 10.3.1 声明处型变

10.3中介绍了在声明变量和方法时使用关键字`out`或`in`对泛型进行修饰(声明为协变或逆变),但是这种形式太过麻烦，每个变量和方法声明的地方都需要使用

- 因此，Kotlin允许对类的泛型直接使用`in`或`out`进行修饰，这被称为声明处型变

- **当泛型作为函数的返回值时，称为协变点，当泛型作为函数参数时，称为逆变点**

以List的泛型E为例，这里使用了协变，即List作为生产者(E类型只能被读取，不能被修改)

	public interface List<out E> : Collection<E> {
	  
	    // E 作为协变点
	    public operator fun get(index: Int): E
	
	    // E 作为逆变点
	    public fun indexOf(element: @UnsafeVariance E): Int
	  }

- 这里的List是只读的List，使用out关键字修饰泛型，这里将泛型E作为协变来使用，也就是当做函数的返回值。但是源码中也将E作为函数的参数使用，即当做逆变来使用，由于函数（比如indexOf）并不会修改List，所以加注解@UnsafeVariance来修饰

以`Comparable`的泛型T为例，这里使用了逆变，即`Comparable`作为消费者

	public interface Comparable<in T> {
		// T 作为逆变点
	    public operator fun compareTo(other: T): Int
	}

最后总结一下，**泛型既可以作为函数的返回值，也可以作为函数的参数。当作为函数的返回值时，泛型是协变的，使用out修饰；当作为函数的参数时，泛型是逆变的，使用in修饰**

1. 在泛型形参前面加上out关键字，表示泛型的协变，作为返回值，为只读类型，泛型参数的继承关系与类的继承关系保持一致，比如List和List

2. 在泛型参数前面加上in表示逆变，表示泛型的逆变，作为函数的参数，为只写类型，泛型参数的继承关系与类的继承关系相反，比如Comparable和Comparable


**协变和逆变 主要是用于有泛型的类型进行赋值**，普通情况下不会发生型变，而协变和逆变确保了赋值能够正常进行

## 10.3.2 使用处型变

## 10.3.3 示例

原始版本：

	fun copy<T>(dest:Array<T>,src:Array<T>){
		if(dest.size < src.size){
			throw ...
		}
		src.forEachedIndexed{index,value->
			dest[index] = src[index]
		}
	}
 
 out版本:
 
	fun copyOut<T>(dest:Array<T>,src:Array<out T>){
		if(dest.size < src.size){
			throw ...
		}
		src.forEachedIndexed{index,value->
			dest[index] = src[index]
		}
	}

- 表示 src的泛型必须是dest的泛型的子类, 则src是dest的子类


in版本:

	fun copyIn<T>(dest:Array<in T>,src:Array<T>){
		if(dest.size < src.size){
			throw ...
		}
		src.forEachedIndexed{index,value->
			dest[index] = src[index]
		}
	}

- 表示 dest的泛型必须是src的泛型的父类, 则src是dest的子类

使用:

	val src = arrayOf<Int>(1,2,3)
	val dest = arrayOfNulls<Number>(3)
	
	copyOut(dest,src) // ok 这时T是Number ,所以src可以接收所有泛型继承自Number的数组
	copyIn(dest,src) // ok 这时T是Int ,所以dest 可以接受所有泛型是Int父类的数组


## 10.4 小节

![Snipaste_2020-03-08_22-30-53.png](http://ww1.sinaimg.cn/large/6ab93b35ly1gcmw9ksfltj21tu0e4gwp.jpg)

## 10.7 星投影

**`*`就是泛型通配符，实际上只是一种语法糖!**

- **Java中单个`?`也能当做泛型通配符来使用，相当于`? extends Object`,Kotlin通过符号`*`支持这种行为(相当于`out Any?`)**


情况1:
对于类的泛型不变时:

	interface Fruit<T>{
		fun getT()
		fun setT(t:T)
	}
	
	val  fruit:Fruit<*> = object:Fruit<String>{....}
	fruit.getT() // 返回值只能用Any?接收
	fruit.setT("str") // 错误setT()方法的参数类型在这里是Nothing 

- 因为T默认是Any?类型的,这里的`<*>`= `<out Any?>`

情况2:
对于类的泛型协变时:


情况3:
对于类的泛型逆变时:


	


## 10.7 泛型问题???

    // a4 类型为 Array<String>
    val a4 = arrayOf("2", "3", "4")
    // 不型变 导致错误!
    copy(a4, a2) // 编译器报错
    // 但是这里 类型为什么是Array<Any>????
    copy(arrayOf("2", "#", "4"), a2)

	fun copy(from: Array<Any>, to: Array<Any>) {
	    assert(from.size == to.size)
	    for (i in from.indices)
	        to[i] = from[i]
	}

## 10.8 泛型上界?????

	fun <T> copyWhenGreater(list: List<T>, threshold: T): List<String> where T : CharSequence,
	T : Comparable<T> {
	return list.filter { it > threshold }.map { it.toString() }
	}

# 11. 内联类


内联类必须有一个主构造函数，并且在主构造函数里必须有且只有一个 val 属性，除此之外，不能再拥有其他的字段。（var 属性目前还没做好所以不能用）

	// 内联类
	fun test() {
	   val duck = Duck("ywwuyi")
	   println(duck.name)
	   println(duck.i)
	   duck.i = 6655
	   duck.talk()
	}
	
	// 经过编译器处理后的代码
	// 内联类看起来就像是一个“零开销”的 wrapper
	fun test() {
	   val duck = "ywwuyi"
	   println(duck) // 输出 1551
	   println(Duck$Erased.getI(duck))
	   Duck$Erased.setI(duck, 6655)
	   Duck$Erased.talk(duck);
	}


内敛类既可以表示为基础类型，也可以表示为包装器，引用相等对于内联类来说没有意义，因此禁止对内联类进行比较


## 11.1 名称修饰
由于内联类被编译为基础类型，因此会导致各种模糊的错误

	inline class UInt(val x: Int)
	
	// 在 JVM 平台上被表示为'public final void compute(int x)'
	fun compute(x: Int) { }
	
	// 同理，在 JVM 平台上也被表示为'public final void compute(int x)'！
	fun compute(x: UInt) { }

**为了解决这种问题，通常会在函数名后拼接一些稳定的哈希值来重命名函数**
	
	 // 下面提供了该方法的解决办法
	 fun compute(x: UInt) { }
	 public final void compute-<hashcode>(int x)



# 12 委托

## 12.1 委托属性

语法是： val/var <属性名>: <类型> by <表达式>

- 属性对应的 `get()`与 `set()`会被委托给它的 `getValue()` 与 `setValue()` 方法


## 12.2 属性委托要求



**对于一个只读属性（即 val 声明的），委托必须提供一个名为 `getValue()`的函数，该函数接受以下参数**：

- `thisRef` —— 必须与 属性所有者 类型（对于扩展属性——指被扩展的类型）相同或者是它的超类型；

- `property` —— 必须是类型 `KProperty<*>` 或其超类型

- 这个函数必须返回与属性相同的类型（或其子类型）

- **参考`ReadOnlyProperty `接口**


**对于一个可变属性（即 var 声明的），委托必须额外提供一个名为 `setValue()`的函数，该函数接受以下参数**：

- `thisRef` —— 同 `getValue()`

- `property` —— 同 `getValue()`

- `new value` —— 必须与属性同类型或者是它的超类型

- **参考`ReadWriteProperty `接口**


**`getValue()` 或 `setValue()` 函数可以通过委托类的成员函数提供或者由扩展函数提供**

- 当需要委托属性到原本未提供的这些函数的对象时后者会更便利

- 两个函数都需要用`operator `关键字来进行标记


## 12.3 翻译规则

	class C {
		var prop: Type by MyDelegate()
	}
	// 这段是由编译器生成的相应代码: 
	class C {
		private val prop$delegate = MyDelegate() var prop: Type
	}


# 13. 函数

## 13.1 中缀表示法

中缀表示法 就是说忽略调用函数时的 点和圆括号 进行调用

- 使用`infix`关键字进行声明

中缀表示法的要求

1. 它们必须是成员函数或扩展函数

2. 它们必须只有一个参数

3. 其参数不得接受可变数量的参数且不能有默认值

## 13.2 函数类型

函数类型具有与函数签名相对应的特殊表示法，即它们的参数和返回值：

1. 所有函数类型都有一个圆括号括起来的参数类型列表以及一个返回类型：`(A, B) -> C` 表示接受类型分别为 A 与 B 两个参数并返回一个 C 类型值的函数类型

	- 参数类型列表可以为空，如 `() -> A`

	- Unit 返回类型不可省略

2. 函数类型可以有一个额外的接收者类型，它在表示法中的点之前指定： 类型 `A.(B) -> C `表示可以在 A 的接收者对象上以一个 B 类型参数来调用并返回一个 C 类型值的函数

	- 带有接收者的函数字面值通常与这些类型一起使用

3. 挂起函数属于特殊种类的函数类型，它的表示法中有一个 `suspend `修饰符 ，例如 `suspend () -> Unit `或者 `suspend A.(B) -> C`

## 13.2 函数类型实例化

**以下是获取函数类型实例的几种方式:**

- 使用函数字面值的代码块，采用以下形式之一：

	1. lambda 表达式: 
	
			{ a, b -> a + b }
	
	2. 匿名函数: 
	
			fun(s: String): Int { return s.toIntOrNull() ?: 0 }

	带有接收者的函数字面值可用作带有接收者的函数类型的值。

- 使用已有声明的可调用引用：

	- 顶层、局部、成员、扩展函数：::isOdd、 String::toInt，

	- 顶层、成员、扩展属性：List<Int>::size，

	- 构造函数：::Regex

	这包括指向特定实例成员的绑定的可调用引用：foo::toString。

- 使用实现函数类型接口的自定义类的实例：

		class IntTransformer: (Int) -> Int {
		    override operator fun invoke(x: Int): Int = TODO()
		}
	
		val intFunction: (Int) -> Int = IntTransformer()

## 13.3 函数类型实例调用

函数类型的值可以通过其 `invoke(……)` 操作符调用：

	f.invoke(x) 
	f(x)


**调用带有接收者的函数类型有俩种形式**:

1. **如果该值具有接收者类型，那么应该将接收者对象作为第一个参数传递**

2. 另外一种形式是在函数类型的引用前面加上接收者对象,**就好比该值是一个扩展函数**：

		1.foo(2)，


## 13.4 Lambda 表达式与匿名函数

**lambda 表达式与匿名函数是“函数字面值”，即未声明的函数,但可以做为表达式传递**

### 13.4.1 Lambda 表达式语法

	val sum = { x: Int, y: Int -> x + y }
	
**lambda 表达式总是括在花括号中， 完整语法形式的参数声明放在花括号内，并有可选的类型标注， 函数体跟在一个 `->` 符号之后**

- 如果推断出的该 lambda 的返回类型不是 Unit，那么该 lambda 主体中的最后一个（或可能是单个）表达式会视为返回值


# 作用域函数

作用域函数的主要作用就是基于一个对象执行一段代码,主要不同是 这个对象是作为`this`或者`it`传入，以及代码块执行的结果是对象自身还是执行结果


## 函数选择

Function	|Object reference	|Return value|Is extension function
:---:|:---:|:---:|:---:
let|	it|	Lambda result|	Yes
run	|this|	Lambda result	|Yes
run	|-|	Lambda result	|No: called without the context object
with|this|	Lambda result|	No: takes the context object as an argument.
apply|	this|	Context object|	Yes
also|	it|	Context object|	Yes


# Kotlin与Java交互

# 在Kotlin中使用JNI

要声明一个在本地（C 或 C++）代码中实现的函数，需要使用 `external` 修饰符来标记它：

	external fun foo(x: Int): Double



# 协程

[Kotlin协程使用手册](https://juejin.im/post/5a90e3836fb9a063592bebe2)

[Kotlin 协程入门这一篇就够了](https://juejin.im/post/5d0afe0bf265da1b7152fb00)




# 标签

# 内联函数(inline)

[Kotlin内联函数参考文章](https://www.jianshu.com/p/4f29c9724b33)

[Kotlin内联函数参考文章](https://juejin.im/entry/5bfccb625188256b0f5837fd)

## 内联函数是什么？

在程序编译时能将程序中内联函数的调用表达式直接替换成内联函数的函数体

- 关键字：inline

## 为什么使用内联函数?

1. 减少方法压栈和出栈

	调用一个方法其实就是一个方法压栈和出栈的过程，调用方法时将栈帧压入方法栈，然后执行方法体，方法结束时将栈帧出栈，这个压栈和出栈的过程是一个耗费资源的过程，这个过程中传递形参也会耗费资源

## 如何使用noinline

如果不希望指定参数(lambda类型或函数类型)进行内联操作，可以添加noinline

## 什么是crossinline

**crossinline 的作用是让被标记的lambda表达式不允许非局部返回**

- 非局部返回指的是在lambda表达式中，退出包含它的函数(直接使用return是不允许的，只能通过return@label实现退出当前lambda)

	但是内联函数允许在lambda中直接使用return退出包含lambda的函数！！

## 什么是reified?

**reified就是具体化泛型,使得开发者能够直接使用泛型的类型(注意：是泛型的类型！！)**

- java中是不能使用泛型作为类型来使用，但是kotlin中通过reified支持了这种行为

举个实际的例子：
	
	// Function
	private fun <T : Activity> Activity.startActivity(context: Context, clazz: Class<T>) {
	    startActivity(Intent(context, clazz))
	}
	
	// Caller
	startActivity(context, NewActivity::class.java)

	// 使用reified改写之后
	
	// Function
	inline fun <reified T : Activity> Activity.startActivity(context: Context) {
	    startActivity(Intent(context, T::class.java))
	}
	
	// Caller
	startActivity<NewActivity>(context)
	


再举个例子:

	inline fun <reified T> membersOf() = T::class.members


