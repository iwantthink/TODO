# Dart语法

[Dart中文网站](http://dart.goodev.org/guides/language/language-tour)

# 1. 重要概念

所有能够使用变量引用的都是对象， 每个对象都是一个类的实例

- 在 Dart 中 甚至连 数字、方法和 null 都是对象

- 所有的对象都继承于 Object 类

使用静态类型可以更清晰的表明意图，并且可以让静态分析工具来分析代码， 但这并不是强制性的

- 在调试代码的时候可能注意到 没有指定类型的变量的类型为 `dynamic`

Dart 在运行之前会先解析代码。

- 可以通过使用 类型或者编译时常量来帮助 Dart 去捕获异常以及 让代码运行的更高效

Dart 支持顶级方法 (例如 `main()`)，同时还支持在类中定义函数（静态函数和实例函数）

- **还可以在方法中定义方法 （嵌套方法或者局部方法）**

Dart 支持顶级变量，以及 在类中定义变量（静态变量和实例变量）

- 实例变量有时候被称之为域（`Field`）或者属性（`Propertie`)

Dart与Java不同，没有 `public`,`protected`和`private`关键字

- 如果一个标识符以 (`_`) 开头，则表示该标识符 在库内是私有的

标识符可以以字母或者 `_` 下划线开头，后面可以是 其他字符和数字的组合

表达式(` expression `)和 语句(`statement`)在特定情况下是有区别的

**Dart 工具可以指出两种问题：警告和错误**

- 警告只是说代码可能有问题， 但是并不会阻止代码执行

- 错误可以是**编译时错误**也可以是**运行时错误**。遇到编译时错误时，代码将 无法执行；运行时错误将会在运行代码的时候导致一个异常

# 2. 关键字

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g394idn457j20ly0jxjum.jpg)

# 3. 变量

使用`var`对变量进行声明

	var name = 'Ryan';
	
- 变量是一个引用，引用指向了内容为`Ryan`的String对象

## 3.1 默认值

没有进行初始化的变量自动获取一个默认值(`null`)

- **数字类型的变量默认的初始值也是`null`,数字类型也是对象**
		
		int lineCount;
		assert(lineCount == null);

## 3.2 可选的类型
声明变量时，可以使用具体类型替代`var`,使得声明一个具体类型的变量

	String name = 'Ryan';

- 添加具体的类型可以提供代码补全等等好处

# 4 `final` 和 `const`

`final`和`const`都能表示变量无法被修改，区别是：

- 被`final`声明的变量只能赋值一次 , 顶级的`final`变量 或者类中的`final`变量在第一次被使用时初始化

		final name = 'Bob'; // Or: final String name = 'Bob';


- 被`const`声明的变量是编译时常量(同时，其也是`final`变量)

		const bar = 1000000;       // Unit of pressure (dynes/cm2)
		const atm = 1.01325 * bar; // Standard atmosphere
		
	- 如果`const`变量在类中，请定义为 `static const`

	- 可以直接定义 `const` 和其值，也可以定义一个 `const` 变量使用其他 `const` 变量的值来初始化其值

- `const`关键字不仅仅只用来定义常量,还可以用来创建不变的值（任何变量都可以有一个不变的值）

	定义构造函数为 `const` 类型时，该构造函数创建的对象是不可改变的。


- **实例变量可以是`final`但是不能是`const`**

# 5 内置的类型

Dart 内置如下类型：

- numbers

- strings

- booleans

- lists (也被称之为 arrays)

- maps

- runes (用于在字符串中表示 Unicode 字符)

- symbols

上面的类型可以直接通过字面量来初始化，例如`this is a string`是一个字符串字面量，`true`是一个布尔字面量

因为Dart中每个变量引用都是一个对象(类的实例)，因此通常使用构造函数来初始化变量.**一些内置的类型具有自己的构造函数**,例如map类型可以使用`Map()`构造函数来实例化


## 5.1 数值

Dart 支持两种类型的数字：

- `int`

	整数值，其取值通常位于 -253 和 253 之间。整数就是不带小数点的数字。

- `double`

	64-bit (双精度) 浮点数，符合 IEEE 754 标准。如果一个数带小数点，那么其就是`double`类型

`int`和`double`都是`num`的子类

- `num`类型定义了基本的操作符，例如 `+`,` -`,` /`, 和` *`， 还定义了 `abs()`、 `ceil()`、和 `floor()` 等函数

- 位操作符，例如 `>> `定义在 `int` 类中

**字符串和数字之间允许进行转换:**

	// String -> int
	var one = int.parse('1');
	assert(one == 1);
	
	// String -> double
	var onePointOne = double.parse('1.1');
	assert(onePointOne == 1.1);
	
	// int -> String
	String oneAsString = 1.toString();
	assert(oneAsString == '1');
	
	// double -> String
	String piAsString = 3.14159.toStringAsFixed(2);
	assert(piAsString == '3.14');

**整数类型支持传统的位移操作符，`<<`, `>>`, `&`和 `|` **

	assert((3 << 1) == 6);  // 0011 << 1 == 0110
	assert((3 >> 1) == 1);  // 0011 >> 1 == 0001
	assert((3 | 4)  == 7);  // 0011 | 0100 == 0111


**数字字面量是编译时常量，在算数表达式中，如果操作数是变量，那么结果也是编译时常量**


## 5.2 字符串

Dart的字符串编码是`UTF_16`,可以使用单引号或者双引号来创建字符串





