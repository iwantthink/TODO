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

Dart的字符串编码是`UTF_16`,可以使用单引号或者双引号来创建字符串,字符串类型可以使用`String`表示

	var s1 = "string one";
	var s2 = 'string two';

**在字符串中可以以`${expression}`的形式添加插值**。如果表达式是一个引用，则可以省略花括号

- 如果表达式的结果是一个对象，那么会调用对象的`toString()`函数来获取一个字符串

**字符串之间可以通过操作符`+`进行链接,也可以直接将多个字符串放到一起来实现同样的功能**

	var s1 = 'a ' 'b'
	         " c";
	assert(s1 == 'a b'
	             ' c');
	
	var s2 = 'a'
	         + 'b';
	assert(s2 == 'ab');

- 使用三个双引号或者三个单引号包裹的字符串可以实现字符串内换行


**通过在双引号前添加前缀`r`可以创建一个原始字符串(即不解析转义)**

	var s = r"one \n two";

字符串字面量是编译时常量， 如果字符串带有插值，并且插值表达式中引用的为编译时常量，那么该字符串结果也是编译时常量

## 5.3 布尔值

Dart提供了`bool`类型来表示布尔值，只有`true`和`false`所创建的对象是布尔类型，此外这俩个对象也是编译时常量

- 在Dart中，只有`true`对象被认为是真，所有其他的值都是`false`(JS中1,"None Empty" 等值都被当做`true`)


## 5.4 列表

Dart中提供`List`类型表示列表，列表包含了数组，列表的表示方式如下:

	var list = [1,2,3];
	
- 列表的下标从0开始

- 列表长度通过`length()`方法获取

- **列表字面量之前添加 `const` 关键字，可以定义一个不变的列表对象（编译时常量)**

		var constantList = const [1, 2, 3];
		constantList[0]=222; //Cannot modify an unmodifiable list


## 5.5 Map

Map是一个拥有键值对的对象，键和值可以是任何类型的对象，每个键仅出现一次，而一个值可以多次出现。**Dart通过`Map`类型来表示Map,或者通过字面量`{key:value}`来表示Map**

	var m = {
		'name':'jack',
		'age':1
	
	};

Map可以通过构造函数创建

	var gifts = new Map();
	// 添加新值
	gifts['first'] = 'partridge';
	gifts[1]= 123;
	// 获取值
	assert gifts[1]==123;
	// 如果key不存在，则返回null
	assert gifts[123123] == null;
	// 通过length属性获取键值对数量
	assert gifts.length == 2;

- 在Map字面量前添加`const`可以创建一个不可变的Map对象(编译时常量)

		var m = const{1:123};

## 5.6 Runes

`Runes`表示`UTF-32`编码的字符串

- Unicode 为每一个字符,标点符号,表情符号等都定义了一个唯一的数值。 但是因为Dart字符串是 `UTF-16`编码字符序列，所以在字符串中表达 32-bit Unicode 值就需要新的语法

- **Dart中使用`"\uXXXX"`的形式来表示Unicode，`XXXX`是4个16进制的数。**例如心形符号`♥`是`\u2665`

	对于非4个数值的情况，需要将编码值放到花括号中，例如`\u{1f600}`

`String`类型提供了`codeUnitAt`属性和`codeUnit`属性来获取16位的编码值,`runes`属性用来获取字符串的runes信息

	  var clapping = '\u{1f44f}';
	  print(clapping);
	  print(clapping.codeUnits);
	  print(clapping.runes.toList());
	
	  Runes input = new Runes(
	      '\u2665  \u{1f605}  \u{1f60e}  \u{1f47b}  \u{1f596}  \u{1f44d}');
	  print(new String.fromCharCodes(input));


## 5.7 Symbol

`Symbol`对象代表Dart中声明的操作符或者标识符

- 混淆之后的代码，标识符名称会被混淆，但是`Symbol`的名称不会改变

使用`#`字面量来获取标识符的Symbol对象

	#bar
	#radix

- Symbol 字面量定义是编译时常量

# 6. 方法

**Dart中的方法是`Function`类型的对象**，这意味着方法也可以赋值给变量，或者当做其他方法的参数

- **类的实例可以当做方法来调用**

定义方法的示例:

	bool isNoble(int atomicNumber) {
	  return _nobleGases[atomicNumber] != null;
	}

- 方法的返回值类型可以省略

- **对于只有一个表达式的方法，可以使用胖箭头`=>`来缩短语法的定义**

		bool isNoble(int atomicNumber) => _nobleGases[atomicNumber] != null;


	- 胖箭头和分号之间只能使用一个表达式，并且不能使用语句。 例如不能使用`if`语句，但是可以使用条件表达式

## 6.1 方法参数

**方法参数分为 必选和可选， 必选参数需要位于参数列表的前面，后面的就是可选参数**

### 6.1.1 Optional parameters（可选参数）

可选参数可以是**命名参数**或者**基于位置的参数**，但是这两种参数不能同时当做可选参数

### 6.1.2 Optional named parameters（可选命名参数）

调用方法的时候，可以使用`paramName: value`形式来指定命名参数

	enableFlags(bold: true, hidden: false);

在定义方法的时候，使用 `{paramTyp1 param1,paramTyp2 param2, …}` 的形式来指定命名参数：

	enableFlags({bool bold, bool hidden}) {
	  // ...
	}


### 6.1.3 Optional positional parameters（可选位置参数）
把一些方法的参数放到 `[] `中就变成可选位置参数了

	String say(String from, String msg, [String device]) {
	  var result = '$from says $msg';
	  if (device != null) {
	    result = '$result with a $device';
	  }
	  return result;
	}

	assert(say('Bob', 'Howdy') == 'Bob says Howdy');

	assert(say('Bob', 'Howdy', 'smoke signal') =='Bob says Howdy with a smoke signal');

### 6.1.4 Default parameter values（默认参数值）

定义方法时，在可选参数后加上`=value`可以定义可选参数的默认值

- 默认值只能是编译时常量

- 如果没有提供默认值，则默认值为 null

- 可选位置参数也可以添加默认值

示例:

	void enableFlags({bool bold = false, bool hidden = false}) {
	  // ...
	}
	
	// bold will be true; hidden will be false.
	enableFlags(bold: true);


## 6.2 入口函数

**每个Dart应用都需要一个顶级`main()`入口方法才能执行**

- `main()`方法的返回值为`void`,并且有一个可选参数`List<String>`

示例:

	void main() {
	  querySelector("#sample_text_id")
	    ..text = "Click me!"
	    ..onClick.listen(reverseText);
	}

- **`..`表示级联操作符(cascade),它可以在一个对象上执行多个操作**
	

## 6.3 一等方法对象

**方法可以作为另外一个方法的参数**

	printElement(element) {
	  print(element);
	}
	
	var list = [1, 2, 3];
	
	// Pass printElement as a parameter.
	list.forEach(printElement);
	
**方法可以赋值给变量**
	
	var loudify = (msg) => '!!! ${msg.toUpperCase()} !!!';
	assert(loudify('hello') == '!!! HELLO !!!');
	

## 6.4 匿名方法

匿名方法即没有名称的方法，也被称为lambda或者闭包. 匿名方法可以赋值给一个变量，并在之后通过变量使用该匿名方法

匿名方法与命名参数类似：

	([[Type] param1[, …]]) { 
	  codeBlock; 
	};

- 在括号之间可以定义若干个参数(可以不定义)

- 参数类型可以不指定

- 参数之间使用逗号分割

- 参数可以是可选参数 

- 花括号中的代码为函数体

示例（使用匿名函数遍历输出列表）：

	var list = ['apples', 'oranges', 'grapes', 'bananas', 'plums'];
	list.forEach((i) {
	  print(list.indexOf(i).toString() + ': ' + i);
	});

**匿名参数也可以使用胖箭头语法进行表示**

	list.forEach( (i)=> print(list.indexOf(i).toString()+":"+i));


# 7. 静态作用域(`Lexical scope`)

**Dart是静态作用域语言，即变量的作用域在编写代码的时候就确定了**

- 基本上大括号里面定义的变量就 只能在大括号里面访问，和 Java 作用域 类似

# 8. 闭包(`Lexical closures`)

**一个闭包就是一个方法对象，无论该对象在何处被调用，都可以访问其在定义时的作用域内的变量**

	Function makeAdder(num addBy) {
	  return (num i) => addBy + i;
	}
	
	main() {
	  // Create a function that adds 2.
	  var add2 = makeAdder(2);
	
	  // Create a function that adds 4.
	  var add4 = makeAdder(4);
	  // 此时addBy = 2
	  assert(add2(3) == 5);
	  // 此时addBy = 4
	  assert(add4(3) == 7);
	}

# 9. 测试函数是否相等
下面示例比较了顶级方法,静态函数，实例函数

	foo() {}               // A top-level function
	
	class A {
	  static void bar() {} // A static method
	  void baz() {}        // An instance method
	}
	
	main() {
	  var x;
	
	  // Comparing top-level functions.
	  x = foo;
	  assert(foo == x);
	
	  // Comparing static methods.
	  x = A.bar;
	  assert(A.bar == x);
	
	  // Comparing instance methods.
	  var v = new A(); // Instance #1 of A
	  var w = new A(); // Instance #2 of A
	  // y 指向 w 指向的实例
	  var y = w;
	  // x 指向 w 指向实例的方法baz
	  x = w.baz;
	
	  // 它们指向了同样的实例，因此俩个方法相同
	  assert(y.baz == x);
	
	  // These closures refer to different instances,
	  // so they're unequal.
	  assert(v.baz != w.baz);
	}

# 10. 返回值

所有方法都需要返回一个值，如果没有指定返回值，那么默认将语句`return null;`作为方法的最后一个语句进行执行


# 11. 操作符

**使用操作符时就创建了表达式**

描述	|操作符
:---:|:---:
unary postfix |	`expr++ ` `expr--` `()` `[]` `.` `?.`
unary prefix	|`-expr` `!expr` `~expr` `++expr` ` --expr `  
multiplicative|	`*` `/` `%` `~/`
additive|	`+` `-`
shift	|`<<` `>>`
bitwise AND|	`&`
bitwise XOR|	`^`
bitwise OR|	`|`
relational and type test|	`>=` `>` `<=` `<` `as` `is` `is!`
equality|	`==` `!=`   
logical AND|	`&&`
logical OR|	`||`
if null|	`??`
conditional|	`expr1 ? expr2 : expr3`
cascade|	`..`
assignment|	`=` `*=` `/=` `~/=` `%=` `+=` `-=` `<<=` `>>=` `&=` `^=` `|=` `??=`

- **表格中的操作符优先级是从左到右，从上到下**

- **对于有俩个操作数的操作符表达式，左边的操作数决定了操作符的功能**，例如 有一条操作符表达式`Vector+Point`,则使用的是`Vector`实例中定义的操作符`+`


## 11.1 算数操作符

操作符	|解释
---|---
+	|加号
–	|减号
-expr	|负号
*	|乘号
/	|除号
~/	|除号，但是返回值为整数
%	|取模

## 11.2 递增/递减操作符

Operator	|Meaning
---|---
++var	|`var = var + 1 (expression value is var + 1)`
var++	|`var = var + 1 (expression value is var)`
--var	|`var = var – 1 (expression value is var – 1)`
var--	|`var = var – 1 (expression value is var)`


## 11.3 相等操作符

操作符	|解释
---|---
==	|相等
!=	|不等
>	|大于
<	|小于
>=	|大于等于
<=	|小于等于

**使用`==`操作符可以判断俩个对象代表的是否为相同内容**(如果要判断俩个对象是否相同则需要使用`identical()`方法)

**`==`操作符的工作原理:**

- 如果俩个操作数俩个都是null， 则返回 true，如果 只有一个是 null 返回 false。

- 操作符实际上是定义在表达式左侧对象上的函数，可以通过重写这些函数实现不同的功能


## 13.4 类型判定操作符

**`as`,`is`和`is! `操作符是在运行时判定对象类型的操作符**

操作符	|解释
:---:|:---:
`as`|类型转换
`is	`|如果对象是指定的类型返回 True
`is!`|如果对象是指定的类型返回 False

- 所有的实例对象都继承自`Object`,因此`obje is Object`总是为`true`

- 在使用`is`操作符时，如果被判断的对象是一个`null`或者类型不匹配，不会抛出异常。但是使用`as`操作符，如果被转换的对象为空时会抛出一个异常

`as`操作符可以简化`is`操作符:

	if (emp is Person) { // Type check
	  emp.firstName = 'Bob';
	}
	
	(emp as Person).firstName = 'Bob';

## 13.5 赋值操作符

**`=`操作符用来赋值，`??=`操作符用来为值为`null`的变量指定值**

**复合赋值操作符:**

 | | | | | 
---|---|---|---|---
`=`	|`–=`|`/=`|`%=`|`>>=`|`^=`
`+=	`|`*=	`|`~/=`|`<<=`|`&=`|`|=`

**复合赋值操作符的原理:**

 |复合赋值操作符	|相等的表达式
:---:|:---:|:---: 	
对于 操作符 op:|	`a op= b`	|`a = a op b`
示例:	|`a += b`	|`a = a + b`


## 13.6 逻辑操作符

操作符	|解释
:---:|:---:
`!expr`|	对表达式结果取反（true 变为 false ，false 变为 true）
`||`	|逻辑 OR
`&&`	|逻辑 AND

## 13.7 位操作符&移位操作符

**Dart 中可以单独操作数字的某一位， 下面操作符同样应用于整数**


操作符	|解释
:---:|:---:
`&`	|AND
`|`	|OR
`^`	|XOR
`~expr`|	Unary bitwise complement (0s become 1s; 1s become 0s)
`<<	`|Shift left
`>>	`|Shift right

示例：

	final value = 0x22;
	final bitmask = 0x0f;
	
	assert((value & bitmask)  == 0x02);  // AND
	assert((value & ~bitmask) == 0x20);  // AND NOT
	assert((value | bitmask)  == 0x2f);  // OR
	assert((value ^ bitmask)  == 0x2d);  // XOR
	assert((value << 4)       == 0x220); // Shift left
	assert((value >> 4)       == 0x02);  // Shift right

## 13.8 条件表达式

Dart有俩个特殊的操作符可以替换`if-else`语句:

1. `condition?expr1:expr2`

	如果`condition`为`true`，执行`expr1`(并返回执行的结果);否则执行`expr2`并返回其结果

2. `expr1 ?? expr2`

	如果`expr1`是`non-null`，返回其值； 否则执行 expr2 并返回其结果


示例:

	// Slightly longer version uses ?: operator.
	String toString() => msg == null ? super.toString() : msg;
	
	// Very long version uses if-else statement.
	String toString() {
	  if (msg == null) {
	    return super.toString();
	  } else {
	    return msg;
	  }
	}


## 13.9 级联调用

**Dart提供了级联操作符(`..`)对同一个对象进行连续调用(调用可以是方法或成员变量),使用级联操作符可以避免临时变量的创建**

- 严格来说级联语法不是操作符，而是Dart中的特殊语法

示例：

	void main(List<String> args) {
	
	  Person()..setName()..setAge();
	  
	  // 相当于下面的代码
	  var p = new Person();
	  p.setName();
	  p.setAge()
	
	}
	
	class Person{
	
	  var name = "null";
	
	  void setName(){
	    name+="setname";
	  }
	
	  void setAge(){
	    name+="setage";
	  }
	}

**级联调用还可以嵌套使用**

	final addressBook = (new AddressBookBuilder()
	      ..name = 'jenny'
	      ..email = 'jenny@example.com'
	      ..phone = (new PhoneNumberBuilder()
	            ..number = '415-555-0100'
	            ..label = 'home')
	          .build())
	    .build();

**使用级联调用符需要注意返回值，如果对空值使用级联调用，那么就会报错**

## 13.10 其他操作符

Operator	|Name	|Meaning
---|---|---
`()	`|方法调用操作符	|代表调用一个方法
`[]`	|列表访问操作符	|访问 list 中特定位置的元素
`.`	|成员访问操作符	|访问元素，例如 foo.bar 代表访问 foo 的 bar 成员
`?.`	|条件成员访问操作符	|和`.`类似，但是左边的操作对象不能为 null，例如 `foo?.bar` 如果foo 为 null 则返回 null，否则返回 bar 成员

# 14. 流程控制语句

## 14.1 `if- else`判断语句

Dart 支持 `if `语句以及可选的 `else`语句

	if (isRaining()) {
	  you.bringRainCoat();
	} else if (isSnowing()) {
	  you.wearJacket();
	} else {
	  car.putTopDown();
	}

## 14.2 `For`循环语句

Dart 支持标准的`for`循环语句

	var message = new StringBuffer("Dart is fun");
	for (var i = 0; i < 5; i++) {
	  message.write('!');
	}

Dart中`for`循环中的闭包会捕获循环的`index`索引值

	var callbacks = [];
	for (var i = 0; i < 2; i++) {
	  callbacks.add(() => print(i));
	}
	callbacks.forEach((c) => c());

	// Dart输出 0 和 1 
	// Js 输出 2 2

**List和Set等实现了`Iterable`接口的类支持`for-in`形式的遍历**

	var collection = [0, 1, 2];
	for (var x in collection) {
	  print(x);
	}

## 14.3 `while`&`do-while`循环语句

`while` 循环在执行循环之前先判断条件是否满足：

	while (!isDone()) {
	  doSomething();
	}

`do-while` 循环是先执行循环代码再判断条件：

	do {
	  printLine();
	} while (!atEndOfPage());

## 14.4 `break`&`continue`关键字

使用 break 来终止循环：

	while (true) {
	  if (shutDownRequested()) break;
	  processIncomingRequests();
	}

使用 continue 来开始下一次循环：

	for (int i = 0; i < candidates.length; i++) {
	  var candidate = candidates[i];
	  if (candidate.yearsExperience < 5) {
	    continue;
	  }
	  candidate.interview();
	}

- 上述代码如果应用于实现了`Iterable`接口的对象，可以使用如下的形式:
		
		candidates.where((c) => c.yearsExperience >= 5)
		          .forEach((c) => c.interview());

## 14.5 `switch case`判断语句

Dart 中的Switch语句使用`==`操作符与`case`进行比较

- 比较的对象必须都是同一个类的实例（并且不是其子类）

- 类必须没有覆写`==`操作符

- **Dart中的Switch语句仅适用于有限的情况，例如在解释器或者扫描器中使用**


**每个非空的`case`语句都必须有一个 `break` 语句**,如果省略了`break`将会抛出异常

- 另外还可以通过`continue`,`throw`,`return`来结束非空`case`语句

**`switch`语句的`default`语句是可选的，当没有`case`语句匹配时，默认执行`default`语句**


**`switch`语句中可以使用`continue`语句，用来跳转到指定的标签(`label`)**

	var command = 'CLOSED';
	switch (command) {
	  case 'CLOSED':
	    executeClosed();
	    continue nowClosed;
	    // Continues executing at the nowClosed label.
	
	nowClosed:
	  case 'NOW_CLOSED':
	    // Runs for both CLOSED and NOW_CLOSED.
	    executeNowClosed();
	    break;
	}

**每个`case`语句都可以拥有局部变量，并且这个变量仅在这个语句内可见**

# 15 断言(`assert`)

**`assert`语句可以用来结束语句的执行**，断言中的表达式为true 则通过，为false则抛出异常`AssertionError`

	// 确保text变量非空
	assert(text != null);
	
	// 确保数值小于100
	assert(number < 100);
	
	// 确保字符串以指定字符开头
	assert(urlString.startsWith('https'));

- **断言仅在检查模式下运行有效，其在生产模式中不会执行！！！**

# 16 异常

**Dart支持抛出异常以及捕获异常**

- 异常用来表示一些错误情况，如果异常没有被捕获，则会导致代码执行终止

- **Dart中的异常都是非检查异常**,方法不一定声明了它们可能抛出的异常，并且不会要求必须捕获异常

- **Dart提供了`Exception`和`Error`类型，以及俩者的子类来表示异常，此外还可以自定义异常**

	**不仅如此，Dart允许将任何非空对象作为异常抛出,其并不需要实现`Exception`或`Error`**

## 16.1 `throw`语句
**Dart使用`throw`语句来抛出异常**

	throw new FormatException('Expected at least 1 section');
	throw 'Out of llamas!';

抛出异常的语句可以结合`=>`使用,也可以在任何能使用表达式的地方使用

	distanceTo(Point other) =>
	    throw new UnimplementedError();
	    
	    
## 16.2 `catch`语句
**Dart使用`catch`语句或`on .. catch`语句捕获异常**,`on catch`语句可以指定捕获异常类型

	try {
	  throwException();
	  // 指定捕获的异常类型
	} on OutOfLlamasException {
	   ....
	   // 指定捕获的异常类型，并使用变量e接收
	}on OtherException catch(e){
		....
		// 捕获所有类型的异常,并使用变量e接收
	}catch(e){
		....
	}catch(e,s){
		....
	}	
		
- 对于可能抛出多种类型异常的代码，可以指定多个捕获语句。每个语句分别对应一个异常类型， 如果捕获语句没有指定异常类型，则该语句可以捕获任何异常类型

- **`catch`语句可以带有一个或者俩个参数，第一个参数为抛出的异常对象，第二参数为堆栈信息(`StackTrace`对象)**

- **通过`rethrow`异常可以将捕获的异常重新抛出**

		final foo = '';
		try {
			foo = "You can't change a final variable's value.";
		} catch (e) {
			print('misbehave() partially handled ${e.runtimeType}.');
			rethrow; // Allow callers to see the exception.
		}

## 16.3 `finally`语句

**Dart提供`finally`语句用来在异常语句中确保无论异常是否抛出，某些代码都必须执行**

- 如果没有捕获异常，那么异常会在`finally`语句执行接收后抛出

- 如果捕获了异常，那么会先执行`catch`语句，再执行`finally`语句



# 17 类
**Dart 是一门面向对象的语言，同时支持基于`mixin`的继承机制**

- 每个对象都是一个类的实例，并且所有类都继承自`Object`

- 基于`mixin`的继承意味着每个类(除了`Object`)都只有一个超类，一个类的代码可以在其他多个类继承中重复使用

**Dart使用`new`关键字和构造函数来创建对象，构造函数的名称可以是`ClassName`或者`ClassName.identifier`**

	var jsonData = JSON.decode('{"x":1, "y":2}');
	
	// Create a Point using Point().
	var p1 = new Point(2, 2);
	
	// Create a Point using Point.fromJson().
	var p2 = new Point.fromJson(jsonData);


**当调用一个方法时，是调用一个对象的方法，方法可能会访问对象的内容**.Dart提供了(`.`)操作符来引用对象的变量或者方法

- `?.`操作符也可以实现引用对象的变量和方法，此外他还对被调用对象进行了非空判断，避免被调用对象为空时的异常

		var p;
		p?.name;


**Dart提供了`const`关键字来使用常量构造函数，常量构造函数可以创建编译时常量，用法与`new`相似，只需要将`new`语句中的`new`替换掉即可**

	var p = const ImmutablePoint(2,2);
	var p2 = const ImmutablePoint(2,2);
	assert(identical(p,p2));

- 两个一样的编译时常量是同一个对象

**`Object`类中的`runtimeType`属性可以用来判断实例的类型，该属性返回一个`Type`对象**

	var a = 123;
	print('The type of a is ${a.runtimeType}');


## 17.1 定义类中的实例变量

定义实例变量的示例:

	class Point {
	  num x; // Declare instance variable x, initially null.
	  num y; // Declare y, initially null.
	  num z = 0; // Declare z, initially 0.
	}

- **所有未初始化的变量，其值都是`null`**

- **每个实例变量都会自动生成一个`getter()`方法(隐式的)**。**每个非`final`的实例变量会自动生成一个`setter()`方法**

		class Point {
		  num x;
		  num y;
		}
		
		main() {
		  var point = new Point();
		  point.x = 4;          // 使用getter
		  assert(point.x == 4); // 使用getter
		  assert(point.y == null); //默认值都为`null`
		}

- 如果在实例变量定义时对其进行初始化(不是构造函数，或其他方法中对其进行初始化)，那么该值是在类实例创建时初始化，即在构造函数和初始化参数列表执行之前


## 17.2 构造函数

构造函数就是一个名称和类名称相同的方法,此外还可以带有其他可选的标识符

	class Point {
	  num x;
	  num y;
	
	  Point(num x, num y) {
	    // There's a better way to do this, stay tuned.
	    this.x = x;
	    this.y = y;
	  }
	}

- **`this`关键字指代当前的类实例**,Dart建议尽可能忽略`this`,仅在名称冲突时使用


**Dart提供了一个语法糖用来将构造函数的参数赋值给实例变量**

	class Point {
	  num x;
	  num y;
	
	  // Syntactic sugar for setting x and y
	  // before the constructor body runs.
	  Point(this.x, this.y);
	}

### 17.2.1 默认构造函数

**如果没有定义构造函数，那么Dart会提供一个默认的无参无名称的构造函数，并且该构造函数会调用父类的无参构造函数**

**子类不会继承父类的构造函数，如果子类没有定义构造函数，那么只有一个无名称无参数的构造函数**

### 17.2.2 命名构造函数(` Named constructor`)
**命名构造函数可以为一个类实现多个构造函数，使用命名构造函数可以更清晰的表明意图**

	class Point {
	  num x;
	  num y;
	
	  Point(this.x, this.y);
	
	  // Named constructor
	  Point.fromJson(Map json) {
	    x = json['x'];
	    y = json['y'];
	  }
	}

- **父类的命名构造函数同样不会被子类继承**

### 17.2.3 调用父类构造函数

**默认情况下，子类的构造函数会自动调用父类的 无名称无参数的默认构造函数**

**如果父类中没有无名称无参数构造函数，那么需要在子类的构造函数中通过`:`+`super()`/`super.namedConstructor()` 手动调用父类的其他构造函数**(如果父类没有无名称无参数构造函数，并且不手动调用则会报错)

	class A {
	  A(a){
	    print('construct A with one parameter!!');
	  }
	
	  A.namedConstructor(){
	    print('construct A by namedConstructor!!!');
	  }
	}
	
	class B extends A{
	  // 调用父类的命名构造函数
	  B(a):super.namedConstructor(){
	    print('construct B !');
	  }
	}
	
	class C extends A{
	  // 调用父类拥有一个参数的构造函数	
	  C(a):super(a){
	    print('construct C !');
	  }
	}
	
	class D extends A{
		// 默认会调用 父类无参无名称构造函数
		D(){
		// 由于A中没有，所以会报错
		}
	
	}
	
	void main(List<String> args) {
	  var b = new B(123);
	  var c = new C(123);
	}


**父类的构造函数在子类构造函数的主体最前面被执行，如果提供了一个初始化参数列表(`initializer list`),则初始化参数列表在父类构造函数执行之前被执行**. 具体的执行顺序如下:

1. `initializer list`（初始化参数列表）

2. `superclass’s no-arg constructor`（父类的无名构造函数）

3. `main class’s no-arg constructor`（子类的无名构造函数）

示例(无初始化参数列表):

	class E extends A {
	  static initialize(){
	      print('initialize param!');
	      return 'StringFromInitialize!';
	  }
	  E():super(initialize()){
	    print('construct E!');
	  }
	}

- **由于父类构造函数的参数会在子类构造函数之前执行，因此父类构造函数的参数可以是一个表达式或方法调用**

- 如果同时使用初始化列表和`super()`，那么必须将`super()`放在最后

- **调用父类构造函数的参数将无法访问`this`. 例如，参数可以是静态函数，但是不能是实例函数**

### 17.2.4 初始化列表(`Initializer List`)

**构造函数执行前除了可以调用父类构造函数之外，还可以初始化实例参数**，初始化示例参数的表达式之间需要使用逗号`,`进行隔开

	class Point {
	  num x;
	  num y;
	
	  Point(this.x, this.y);
	
	  // Initializer list sets instance variables before
	  // the constructor body runs.
	  Point.fromJson(Map jsonMap)
	      : x = jsonMap['x'],
	        y = jsonMap['y'] {
	    print('In Point.fromJson(): ($x, $y)');
	  }
	}

- **初始化表达式等号右边的部分不能访问`this`**


**初始化列表十分适合设置`final`变量的值**

	import 'dart:math';
	class Point {
	  final num x;
	  final num y;
	  final num distanceFromOrigin;
	  
	  Point(x, y)
	      : x = x,
	        y = y,
	        distanceFromOrigin = sqrt(x * x + y * y);
	}
	
	main() {
	  var p = new Point(2, 3);
	  print(p.distanceFromOrigin);
	}


### 17.2.5 重定向构造函数

**同一个类中的构造函数可以通过冒号`:`来调用另外一个构造函数**

- **被重定向的构造函数不允许拥有代码块**

示例:

	class Point {
	  num x;
	  num y;
	
	  // The main constructor for this class.
	  Point(this.x, this.y);
	
	  // Delegates to the main constructor.
	  Point.alongXAxis(num x) : this(x, 0);
	}

### 17.2.6 常量构造函数

通过使用常量构造函数创建实例，可以提供一个状态不变的对象(即将对象定义为编译时常量)

- **在构造函数前添加`const`关键字,同时声明所有类的变量为`final`**

示例：

	class ImmutablePoint {
	  final num x;
	  final num y;
	  const ImmutablePoint(this.x, this.y);
	  static final ImmutablePoint origin =
	      const ImmutablePoint(0, 0);
	}


### 17.2.7 工厂方法构造函数

**使用`factory`关键字标记的构造函数就是工厂方法构造函数，其表示构造函数并不总是返回一个新的对象!!!!**

- **在构造函数前添加`factory`关键字**

- **工厂构造函数无法访问`this`**

示例:

	class Logger {
	  final String name;
	  bool mute = false;
	
	  // _cache is library-private, thanks to the _ in front of its name
	  static final Map<String, Logger> _cache =
	      <String, Logger>{};
	
	  factory Logger(String name) {
	    if (_cache.containsKey(name)) {
	      return _cache[name];
	    } else {
	      final logger = new Logger._internal(name);
	      _cache[name] = logger;
	      return logger;
	    }
	  }
	
	  Logger._internal(this.name);
	
	  void log(String msg) {
	    if (!mute) {
	      print(msg);
	    }
	  }
	}
	
	var logger = new Logger('UI');
	logger.log('Button clicked');
	
- 使用`new`关键字调用工厂构造函数	


# 18 函数
**函数就是在类中定义的方法，是类对象的行为**

## 18.1 实例函数
**对象的实例函数可以访问`this`**,实例函数示例:

	import 'dart:math';
	
	class Point {
	  num x;
	  num y;
	  Point(this.x, this.y);
	
	  num distanceTo(Point other) {
	    var dx = x - other.x;
	    var dy = y - other.y;
	    return sqrt(dx * dx + dy * dy);
	  }
	}

- `distanceTo()`就是一个实例函数

## 18.2 `getter()`&`setter()`

`getter()`和`setter()` 是用来设置和访问对象属性的特殊函数

- 每个实例变量都隐含的具有一个`getter()`方法， 如果变量没有被`final`修饰，那么还有一个 `setter()`方法

- 使用`get`和`set`关键字可以定义变量的`getter()`和`setter()`方法

示例:

	class Rectangle {
	  num left;
	  num top;
	  num width;
	  num height;
	
	  Rectangle(this.left, this.top, this.width, this.height);
	
	  // Define two calculated properties: right and bottom.
	  // 定义right变量的getter方法
	  num get right             => left + width;
	      set right(num value)  => left = value - width;
	  num get bottom            => top + height;
	      set bottom(num value) => top = value - height;
	}
	
	main() {
	  var rect = new Rectangle(3, 4, 20, 15);
	  assert(rect.left == 3);
	  rect.right = 12;
	  assert(rect.left == -8);
	}
	


	





