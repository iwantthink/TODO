# Dart语法

[Dart中文网站](http://dart.goodev.org/guides/language/language-tour)

# 1. 重要概念

所有能够使用变量引用的东西都是对象， 并且每个对象都是类的实例

- 在 Dart 中 甚至连 数字、方法和 `null` 都是对象

- 所有的对象都继承于 Object 类

**Dart是强类型语言**，但是类型注释并不是强制的，因为Dart可以推断类型

- **特殊类型`dynamic`可以明确的指定一个变量不需要类型**

Dart支持泛型(`generic type`),例如`List<int>`,`List<dynamic>`

Dart 在运行之前会先解析代码。

- 可以通过使用 类型或者编译时常量来帮助 Dart 去捕获异常以及 让代码运行的更高效

Dart 支持顶级方法 (例如 `main()`)，同时还支持在类中定义函数（静态函数和实例函数）

- **还可以在方法中定义方法 （嵌套方法或者局部方法）**

Dart 支持顶级变量，以及在类中定义变量（静态变量和实例变量）

- 实例变量有时候被称之为字段（`Field`）或者属性（`Propertie`)

**Dart与Java不同，没有 `public`,`protected`和`private`关键字**

- 如果一个标识符以 (`_`) 开头，则表示该标识符 在库内是私有的

**标识符可以以字母或者 `_` 下划线开头，后面可以是 其他字符和数字的任意组合**

Dart拥有表达式(` expression `)和语句(`statement`)，前者拥有运行时值，后者没有

- 例如表达式`condition ? expr1 : expr2`拥有`expr1`和`expr2`俩个值，语句`if-else`就没有。

- 语句可以包含一个或多个表达式，但是表达式通常不能直接包含语句

**Dart 工具可能报告两种类型的问题：警告和错误**

- 警告只是说代码可能有问题， 但是并不会阻止代码运行

- 错误可以是**编译时错误**也可以是**运行时错误**。遇到编译时错误时，代码将无法执行；运行时错误将会在运行代码的时候导致一个异常

# 2. 关键字

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g394idn457j20ly0jxjum.jpg)

# 3. 变量

使用`var`对变量进行声明并初始化,Dart会对其进行类型推断.此外直接声明变量类型也是Dart支持的

	var name = 'Ryan';
	
- **变量保存引用**，引用指向了内容为`Ryan`的String对象

- 变量name的类型会被Dart推断为`String`

**如果一个变量意味着可以使用任意类型，可以使用`Object`或`dynamic`,[但是俩者的意义不相同](https://dart.dev/guides/language/effective-dart/design#do-annotate-with-object-instead-of-dynamic-to-indicate-any-object-is-allowed)**

- 使用 Object 时，意味着变量可以接受任意类型,类型系统会保证其类型安全


- 使用 dynamic 则是告诉编译器，不用做类型检测。并且当调用一个不存在的方法时，会执行 `noSuchMethod()`方法，默认情况下（在 Object 里实现）它会抛出 NoSuchMethodError。

**Dart提供了`is`操作符在运行时检测类型**

## 3.1 默认值

没有进行初始化的变量自动获取一个默认值(`null`)

- **数字类型的变量默认的初始值也是`null`,数字类型也是对象**
		
		int lineCount;
		assert(lineCount == null);

## 3.2 可选的类型
声明变量时，可以使用具体类型替代`var`,使得声明一个具体类型的变量

	String name = 'Ryan';

- 添加具体的类型可以提供代码补全等等好处

## 3.3 `final` 和 `const`

`final`和`const`都能表示变量无法被修改,前者表示运行时常量(在程序运行时确定值)，后者表示编译时常量(即在编译时就确定了值)

- **`final`无法和`var`一起使用，但是可以和具体的类型一起使用**

- **实例变量可以被`final`修饰，但是无法被`const`修饰。如果`const`变量在类中，请定义为 `static const`**

- **`final`变量必须在构造函数主体开始执行前被初始化(即变量声明阶段). 通过构造函数参数或构造函数的初始化列表**

- 被`final`声明的变量只能赋值一次 , 顶级的`final`变量 或者类中的`final`变量在第一次被使用时初始化

		final name = 'Bob'; // Or: final String name = 'Bob';


- 被`const`声明的变量是编译时常量(同时其也是隐式的`final`变量)

		const bar = 1000000;       // Unit of pressure (dynes/cm2)
		const atm = 1.01325 * bar; // Standard atmosphere

	- 可以直接定义 `const` 和其常量值，也可以定义一个 `const` 变量使用其他 `const` 变量的值来初始化其值

- **`const`关键字不仅仅只用来定义变量,还可以用来定义值（任何变量都可以有一个不变的值），或者定义一个创建不变值的构造函数**

		var foo = const [];
		final bar = const [];
		const baz = []; // Equivalent to `const []`


	- 定义构造函数为 `const` 类型时，该构造函数创建的对象是不可改变的

	- 非`final`和非`const`的变量可以被改变，但是其常量值是无法被改变的

# 5 内置的类型

Dart 内置如下类型：

- numbers

- strings

- booleans

- lists (也被称之为 arrays)

- sets

- maps

- runes (用于在字符串中表示 Unicode 字符)

- symbols

上面的类型可以直接通过字面量来初始化，例如`this is a string`是一个字符串字面量，`true`是一个布尔字面量

因为Dart中每个变量引用都是一个对象(类的实例)，所以通常使用构造函数来初始化变量.**一些内置的类型具有自己的构造函数**,例如map类型可以使用`Map()`构造函数来实例化


## 5.1 数值

Dart 支持两种类型的数字：

- `int`

	整数值不会大于64位，并且其具体大小取决于平台。在Dart VM上，取值范围就是 `-2^63`至`2^63-1`之间

- `double`

	64-bit (双精度) 浮点数，符合 IEEE 754 标准

`int`和`double`都是`num`的子类

- `num`类型定义了基本的操作符，例如 `+`,` -`,` /`, 和` *`， 还定义了 `abs()`、 `ceil()`、和 `floor()` 等函数

- 位操作符，例如 `>> `定义在 `int` 类中

- 如果在`num`或其子类中找不到的方法，可以从`dart:math`库中查询

**整数就是不带小数点的数字,`double`类型就是带小数点的数字**

- **从Dart2.1开始，整数可以被赋值给double类型变量而不抛出异常**

**字符串和数字之间允许互相转换:**

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

	const msPerSecond = 1000;
	const secondsUntilRetry = 5;
	const msUntilRetry = secondsUntilRetry * msPerSecond;

## 5.2 字符串

Dart提供了`String`类型来表示字符串类型

Dart的字符串是由`UTF_16`编码单元组成的序列,可以使用单引号或者双引号来创建字符串

	var s1 = "string one";
	var s2 = 'string two';

**在字符串中可以以`${expression}`的形式添加插值**。如果`expression`是一个标识符，还可以省略花括号

- 如果表达式的结果是一个对象，那么会调用对象的`toString()`函数来获取一个字符串

操作符`==`用来判断俩个对象是否相等，对于字符串来说，如果俩者包含相同的编码单元序列，则它们相等

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

	// These work in a const string.
	const aConstNum = 0;
	const aConstBool = true;
	const aConstString = 'a constant string';
	
	// These do NOT work in a const string.
	var aNum = 0;
	var aBool = true;
	var aString = 'a string';
	const aConstList = [1, 2, 3];
	
	const validConstString = '$aConstNum $aConstBool $aConstString';
	// const invalidConstString = '$aNum $aBool $aString $aConstList';

## 5.3 布尔值

Dart提供了`bool`类型来表示布尔值，只有`true`和`false`所创建的对象是布尔类型，此外这俩个对象也是编译时常量

- 在Dart中，只有`true`对象被认为是真，所有其他的值都是`false`(JS中1,"None Empty" 等值都被当做`true`)


## 5.4 列表

Dart中提供`List`类型表示列表，列表就代表了数组，列表的表示方式如下:

	var list = [1,2,3];// 推荐使用字面量形式创建
	// var list = <String>[123];
	var list2 = List();
	var list3 = new List();
	
- 列表的下标从0开始

- 列表长度通过其`length()`方法获取

- **列表字面量之前添加 `const` 关键字，可以定义一个不变的列表对象（编译时常量)**

		var constantList = const [1, 2, 3];
		constantList[0]=222; //Cannot modify an unmodifiable list

### 5.4.1 展开操作符

Dart2.3开始加入了展开操作符(`...`)和非空展开操作符`...?`,它们可以用来简洁的将多个元素插入列表

- 非空展开操作符会检查被展开的列表是否为空，防止抛出异常

示例(将列表加入另外一个列表)

	var list = [1, 2, 3];
	var list2 = [0, ...list];
	assert(list2.length == 4);

### 5.4.2 `collection if/for`

Dart2.3开始加入了`collection if`和`collection for`，这使得可以在列表初始化中可以使用条件(`if`)和循环(`for`)

示例(`collection if`)

	var nav = [
	  'Home',
	  'Furniture',
	  'Plants',
	  if (promoActive) 'Outlet'
	];

示例(`collection for`)

	var listOfInts = [1, 2, 3];
	var listOfStrings = [
	  '#0',
	  for (var i in listOfInts) '#$i'
	];
	assert(listOfStrings[1] == '#1');



## 5.5 Set

**Set无序且不重复的集合，Dart通过`Set`类型或字面量来表示`Set`**

	var halogens = {'fluorine', 'chlorine', 'bromine', 'iodine', 'astatine'};
	var h2 = Set();

- Dart2.2开始才支持用字面量表示Set

**因为Set的字面量与Map的字面量相同，都是使用`{}`进行包裹，因此在创建空的Set时，会出现歧义(Dart会认为`{}`是一个`Map<dynamic,dynamic>`)**

- 为了消除歧义,在创建一个空的Set，可以通过泛型+`{}`的形式。 或者将`{}`赋值给`Set`类型的变量

		var names = <String>{};
		// Set<String> names = {}; // This works, too.
		// var names = {}; // Creates a map, not a set.

Set通过`add()`或`addAll()`方法添加元素

	var elements = <String>{};
	elements.add('fluorine');
	elements.addAll(halogens);

Set通过`length`属性获取长度

	assert(names.length == 0);

为了创建一个作为编译时常量的Set，需要在Set字面量前添加一个`const`

	final constantSet = const {
	  'astatine',
	};
	// constantSet.add('helium'); // Uncommenting this causes an error.


Dart2.3开始Set支持扩展操作符(`...`或`...?`),`collection if`,`collection for`

## 5.5 Map

Map是一个拥有键值对的对象，键和值可以是任何类型的对象，每个键仅出现一次，而一个值可以多次出现。**Dart通过`Map`类型来表示Map,或者通过字面量`{key:value}`来表示Map**

	var m = {
		'name':'jack',
		'age':1
	
	};

Map可以通过构造函数创建

	var gifts = new Map();	

	// 如果key不存在，则返回null
	assert gifts[123123] == null;

- Map添加值

		gifts['first'] = 'partridge';
		gifts[1]= 123;

- Map读取值,在读取到不存在的key时，会返回null

		assert(gifts[1]==123);
		
- Map长度(键值对数量)

		assert(gifts.length == 2)	
		

在Map字面量前添加`const`可以创建一个不可变的Map对象(编译时常量)

		var m = const{1:123};
		// m[1] = 'Helium'; // Uncommenting this causes an error.

Dart2.3开始Map支持扩展操作符(`...`或`...?`),`collection if`,`collection for`

## 5.6 Runes

`Runes`表示`UTF-32`码点的字符串

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

**`Symbol`对象代表Dart中声明的操作符或者标识符**

- 混淆之后的代码，标识符名称会被混淆，但是`Symbol`的名称不会改变

**使用`#`字面量来获取标识符的Symbol对象**

	#bar
	#radix

- Symbol 字面量定义是编译时常量

# 6. 方法

Dart是一门面向对象语法，即使是方法也是对象，**Dart使用`Function`类型作为方法的类型**，这意味着方法也可以赋值给变量，或者当做其他方法的参数

- **类的实例只要是方法，那就可以被调用**

定义方法的示例:

	bool isNoble(int atomicNumber) {
	  return _nobleGases[atomicNumber] != null;
	}

- Dart不建议省略方法的返回值类型，但是省略了也可以工作

- **对于只有一个表达式的方法，可以使用缩写语法来定义，即使用胖箭头`=>`来定义**

		bool isNoble(int atomicNumber) => _nobleGases[atomicNumber] != null;

	- `=> expr`语法就是`{return expr;}`的缩写

	- 胖箭头和分号之间只能使用一个表达式，并且不能使用语句。 例如不能使用`if`语句，但是可以使用条件表达式

## 6.1 方法参数

**方法参数分为 必选和可选， 必选参数需要位于参数列表的前面，后面的就是可选参数**

- 可选具名参数也可以使用`@required`注解

### 6.1.1 Optional parameters（可选参数）

可选参数可以是**命名参数**或者**位置参数**，但是这两种参数不能同时当做可选参数

#### 6.1.1.1 Optional named parameters（可选命名参数）
**在定义方法的时候，使用 `{paramTyp1 param1,paramTyp2 param2, …}` 的形式来指定命名参数.那么调用方法的时候，就可以使用`paramName: value`形式来指定命名参数**

	enableFlags({bool bold, bool hidden}) {
	  // ...
	}

	enableFlags(bold: true, hidden: false);

- **注意这里有一个花括号!!!**

- **所有的命名参数都是可选的!!!!**这意味着下面的调用是合法的

		enableFlags()

Dart中任何命名参数都可以使用`@required`注解，它表示这个参数时一个必须的参数

	const Scrollbar({Key key, @required Widget child})

- 当构造`Scrollbar`时，如果缺少了`child`参数，那么编译器会报错

#### 6.1.1.2 Optional positional parameters（可选位置参数）
**把一些方法的参数放到 `[] `中就变成可选位置参数**

	String say(String from, String msg, [String device]) {
	  var result = '$from says $msg';
	  if (device != null) {
	    result = '$result with a $device';
	  }
	  return result;
	}

	assert(say('Bob', 'Howdy') == 'Bob says Howdy');

	assert(say('Bob', 'Howdy', 'smoke signal') =='Bob says Howdy with a smoke signal');

### 6.1.2 Default parameter values（默认参数值）
**定义方法时，在可选参数定义后加上`=value`可以定义可选参数的默认值**

- **默认值可以是list或map,只要其是编译时常量**

- 可选参数如果没有提供默认值，则默认值为`null`

- **只有可选参数能够添加默认值，包括可选位置参数和可选命名参数**

示例(可选命名参数):
	
	void enableFlags({bool bold = false, bool hidden = false}) {
	  // ...
	}
	
	// bold will be true; hidden will be false.
	enableFlags(bold: true);

示例(可选位置参数):

	String say(String from, String msg,
	    [String device = 'carrier pigeon', String mood]) {
	  var result = '$from says $msg';
	  if (device != null) {
	    result = '$result with a $device';
	  }
	  if (mood != null) {
	    result = '$result (in a $mood mood)';
	  }
	  return result;
	}
	
	assert(say('Bob', 'Howdy') ==
	    'Bob says Howdy with a carrier pigeon');


## 6.2 入口函数

**每个Dart应用都需要一个顶级`main()`方法才能执行**

- **`main()`方法的返回值为`void`,并且有一个可选参数`List<String>`**

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
**匿名方法即没有名称的方法，也被称为lambda或者闭包. 匿名方法可以赋值给一个变量，并在之后通过变量使用该匿名方法**

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

	var list = ['apples', 'bananas', 'oranges'];
	list.forEach((item) {
	  print('${list.indexOf(item)}: $item');
	});

**匿名参数也可以使用缩写语法进行表示**

	list.forEach( (i)=> print(list.indexOf(i).toString()+":"+i));


## 6.5 静态作用域(`Lexical scope`)

**Dart是静态作用域语言，即变量的作用域在编写代码的时候就确定了**

- 基本上花括号里面定义的变量就 只能在花括号里面访问，和 Java 作用域 类似

## 6.6 闭包(`Lexical closures`)

**闭包就是一个方法对象，其可以访问其定义所处范围内的变量，即使方法在定义之外被使用，也可以访问**

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

## 6.7 测试方法是否相等
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

## 6.8 返回值

**所有方法都需要返回一个值，如果没有指定返回值，那么默认将语句`return null;`作为方法的最后一个语句进行执行**

	foo() {}
	
	assert(foo() == null);

# 7 操作符

**表达式中必有操作符!**下面的例子都是表达式:

	a++
	a + b
	a = b
	c ? a : b
	a is T

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

- **操作符可以被重写**

## 7.1 算数操作符

操作符	|解释
---|---
`+	`|加号
`–	`|减号
`-expr`	|负号
`*	`|乘号
`/	`|除号
`~/`	|除号，但是返回值为整数
`%`	|取模

## 7.2 递增/递减操作符

Operator	|Meaning
---|---
`++var`	|`var = var + 1 (expression value is var + 1)`
`var++`	|`var = var + 1 (expression value is var)`
`--var`	|`var = var – 1 (expression value is var – 1)`
`var--`	|`var = var – 1 (expression value is var)`


## 7.3 相等和关系操作符

操作符	|解释
---|---
`==	`|相等
`!=	`|不等
`>	`|大于
`<	`|小于
`>=	`|大于等于
`<=`|小于等于

**使用`==`操作符可以判断俩个对象代表的是否为相同内容**(如果要判断俩个对象是否相同则需要使用`identical()`方法)

**`==`操作符的工作原理:**

- 如果俩个操作数俩个都是null， 则返回 true，如果 只有一个是 null 返回 false。

- 操作符实际上是定义在表达式左侧对象上的函数，可以通过重写这些函数实现不同的功能


## 7.4 类型判定操作符

**`as`,`is`和`is! `操作符是在运行时判定对象类型的操作符**

操作符	|解释
:---:|:---:
`as`|类型转换
`is	`|如果对象是指定的类型返回`True`
`is!`|如果对象是指定的类型返回`False`

- 所有的实例对象都继承自`Object`,因此`obje is Object`总是为`true`

- **操作符`as`用来将一个对象转换为特定类型**

	在使用`is`操作符时，如果被判断的对象是一个`null`或者类型不匹配，不会抛出异常。但是使用`as`操作符，如果被转换的对象为空时会抛出一个异常

**`as`操作符可以简化`is`操作符:**

	if (emp is Person) { // Type check
	  emp.firstName = 'Bob';
	}
	
	(emp as Person).firstName = 'Bob';

## 7.5 赋值操作符

**`=`操作符用来赋值，`??=`操作符用来为值为`null`的变量指定值**

	// Assign value to a
	a = value;
	// Assign value to b if b is null; otherwise, b stays the same
	b ??= value;

**复合赋值操作符:**

 | | | | | 
---|---|---|---|---
`=`	|`–=`|`/=`|`%=`|`>>=`|`^=`
`+=	`|`*=	`|`~/=`|`<<=`|`&=`|`|=`

**复合赋值操作符的原理:**

 |复合赋值操作符|对应的表达式
:---:|:---:|:---: 	
对于 操作符 op:|	`a op= b`	|`a = a op b`
示例:	|`a += b`	|`a = a + b`


## 7.6 逻辑操作符

操作符	|解释
:---:|:---:
`!expr`|	对表达式结果取反（true 变为 false ，false 变为 true）
`||`	|逻辑 OR
`&&`	|逻辑 AND

## 7.7 位操作符&移位操作符
**Dart 中可以单独操作数字的某一位。通常会对整数使用下面操作符**

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

## 7.8 条件表达式

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


## 7.9 级联操作符

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

## 7.10 其他操作符

Operator	|Name	|Meaning
---|---|---
`()	`|方法调用操作符	|代表调用一个方法
`[]`	|列表访问操作符	|访问 list 中特定位置的元素
`.`	|成员访问操作符	|访问元素，例如 foo.bar 代表访问 foo 的 bar 成员
`?.`	|条件成员访问操作符	|和`.`类似，但是左边的操作对象不能为 null，例如 `foo?.bar` 如果foo 为 null 则返回 null，否则返回 bar 成员

# 8 流程控制语句

## 8.1 `if- else`判断语句

Dart 支持 `if `语句以及可选的 `else`语句

	if (isRaining()) {
	  you.bringRainCoat();
	} else if (isSnowing()) {
	  you.wearJacket();
	} else {
	  car.putTopDown();
	}

## 8.2 `For`循环语句

Dart 支持标准的`for`循环语句

	var message = new StringBuffer("Dart is fun");
	for (var i = 0; i < 5; i++) {
	  message.write('!');
	}

Dart`for`循环中的闭包可以获取到循环的`index`索引值

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

## 8.3 `while`&`do-while`循环语句

`while` 循环在执行循环之前先判断条件是否满足：

	while (!isDone()) {
	  doSomething();
	}

`do-while` 循环是先执行循环代码再判断条件：

	do {
	  printLine();
	} while (!atEndOfPage());

## 8.4 `break`&`continue`关键字

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

**上述代码如果应用于实现了`Iterable`接口的对象，可以使用如下的形式:**
		
		candidates.where((c) => c.yearsExperience >= 5)
		          .forEach((c) => c.interview());

## 8.5 `switch case`判断语句

Dart 中的Switch语句使用`==`操作符对字符串，整数和编译时常量进行比较

- **被比较的对象必须都是同一个类的实例（并且不是其子类)**

- **类必须没有重写`==`操作符**

**Dart中的Switch语句仅适用于有限的情况，例如在解释器或者扫描器中使用**

**每个非空的`case`语句都必须以`break` 语句结尾**,如果省略了`break`将会抛出异常

- 另外还可以通过`continue`,`throw`,`return`来结束非空`case`语句

**`switch`语句的`default`语句是可选的，当没有`case`语句匹配时，默认执行`default`语句**


**`switch`语句中可以使用`continue`+`label`，用来跳转到指定的标签(`label`)**

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

## 8.6 断言(`assert`)

**`assert`语句可以用来结束语句的执行**，断言中的表达式为true 则通过，为false则抛出异常`AssertionError`

	// 确保text变量非空
	assert(text != null);
	
	// 确保数值小于100
	assert(number < 100);
	
	// 确保字符串以指定字符开头
	assert(urlString.startsWith('https'));

- **断言仅在检查模式下运行有效，其在生产模式中不会执行！！！**

`assert()`方法接收俩个参数，第一个参数用来接收布尔值（可以是任何解析为布尔值的表达式），第二个参数用来设置断言失败时输出的内容


# 9 异常

**Dart支持抛出异常以及捕获异常**。异常用来表示一些错误情况，如果异常没有被捕获，则会导致代码执行终止

- **Dart中的异常都是非检查异常**,方法不一定声明了它们可能抛出的异常，并且也不会要求必须捕获异常

**Dart提供了`Exception`和`Error`类型，以及俩者的子类来表示异常，此外还可以自定义异常**

- **不仅如此，Dart允许将任何非空对象作为异常抛出,其并不需要实现`Exception`或`Error`**

## 9.1 `throw`语句
**Dart使用`throw`语句来抛出异常**

	throw new FormatException('Expected at least 1 section');
	throw 'Out of llamas!';

因为抛出异常是一个表达式，因此可以在任何能使用表达式的地方使用(抛出异常的语句可以结合`=>`使用)

	distanceTo(Point other) =>
	    throw new UnimplementedError();
	    
	    
## 9.2 `catch`语句
**Dart使用`catch`和`on`俩个关键字来对抛出的异常进行处理,俩个关键字可以单独使用也可以组合使用**

- `catch`可以在处理异常时提供异常对象

- `on`可以用来指定处理的异常类型

示例:

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

**`catch`语句可以带有一个或者俩个参数，第一个参数为抛出的异常对象，第二参数为堆栈信息(`StackTrace`对象)**

**通过`rethrow`异常可以将捕获的异常重新抛出**

	final foo = '';
	try {
		foo = "You can't change a final variable's value.";
	} catch (e) {
		print('misbehave() partially handled ${e.runtimeType}.');
		rethrow; // Allow callers to see the exception.
	}

## 9.3 `finally`语句

**Dart提供`finally`语句用来在异常语句中确保无论异常是否抛出，某些代码都必须执行**

- 如果没有捕获异常，那么异常会在`finally`语句执行接收后抛出

- 如果捕获了异常，那么会先执行`catch`语句，再执行`finally`语句



# 10 类

**Dart 是一门面向对象的语言，同时支持基于`mixin`的继承机制**

- 每个对象都是一个类的实例，并且所有类都继承自`Object`

- 基于`mixin`的继承意味着每个类(除了`Object`)都只有一个超类，一个类的代码可以在其他多个类继承中重复使用


**10.1-10.3 展示了如何使用类，其他的章节用来展示如何实现类**

## 10.1 使用类成员
对象的成员由函数和数据(分别是函数变量和实例变量)组成。当调用一个函数时，实际是对一个对象调用方法,该方法可以访问同一个对象内的函数和数据

- Dart提供了(`.`)操作符来引用对象的变量或者方法

- `?.`操作符也可以实现引用对象的变量和方法，此外它还对被调用对象进行了非空判断，避免被调用对象为空时的异常

		var p;
		p?.name;

## 10.2 使用构造函数

**Dart使用构造函数以及可选的`new`关键字来创建对象，构造函数的名称可以是`ClassName`或者`ClassName.identifier`**

	var jsonData = JSON.decode('{"x":1, "y":2}');
	
	// Create a Point using Point().
	var p1 = new Point(2, 2);
	
	// Create a Point using Point.fromJson().
	var p2 = new Point.fromJson(jsonData);

- Dart 2.0开始`new`关键字是可选的

**Dart提供了`const`关键字来定义常量构造函数，常量构造函数可以创建编译时常量**

	var p = const ImmutablePoint(2,2);
	var p2 = const ImmutablePoint(2,2);
	assert(identical(p,p2));// They are the same instance!

**在一个具有常量的上下文中，可以省略掉构造函数或字面量前面的`const`。但是对于没有常量上下文的地方，不可以省略! **.示例：

	// Lots of const keywords here.
	const pointAndLine = const {
	  'point': const [const ImmutablePoint(0, 0)],
	  'line': const [const ImmutablePoint(1, 10), const ImmutablePoint(-2, 11)],
	};

- Dart 2.0 开始在具有常量上下文中字面量前的`const`可以省略

## 10.3 获取对象的类型
**`Object`类中的`runtimeType`属性可以用来判断实例的类型，该属性返回一个`Type`对象**

	var a = 123;
	print('The type of a is ${a.runtimeType}');




## 10.4 实例变量

定义实例变量的示例:

	class Point {
	  num x; // Declare instance variable x, initially null.
	  num y; // Declare y, initially null.
	  num z = 0; // Declare z, initially 0.
	}

- **所有未初始化的变量，其值都是`null`**

- **每个实例变量都会自动生成一个隐式的`getter()`方法,此外每个非`final`的实例变量会自动生成一个隐式的`setter()`方法**

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

- **如果在实例变量定义时对其进行初始化(不是构造函数，或其他方法中对其进行初始化)，那么该值是在类实例创建时初始化，即在构造函数和初始化参数列表执行之前**


## 10.5 构造函数
构造函数就是一个名称和类名称相同的方法,此外还可以接收参数

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

### 10.5.1 默认构造函数

**如果没有定义构造函数，那么Dart会提供一个默认的无参构造函数，并且该构造函数会调用父类的无参构造函数**

### 10.5.2 构造函数无法被继承

**子类不会继承父类的构造函数，如果子类没有定义构造函数，那么只有一个无参构造函数**

### 10.5.3 命名构造函数(` Named constructor`)
**命名构造函数可以为一个类实现多个构造函数，此外还可以更清晰的表明意图**

- **一个类中不能拥有多个普通的构造函数**

示例:

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

## 10.6 调用父类构造函数
**默认情况下，子类的构造函数会自动调用父类的 无名称无参数的默认构造函数**

- 如果父类中没有无参数的构造函数，那么必须手动调用父类的一个构造函数

- 调用父类的构造函数的语法是 在构造函数参数之后，构造函数主体之前添加一个`:superConstructor(param)`

	具体形式如`:super(param)`/`:super.namedConstructor(param)`

**父类的构造函数在子类构造函数的主体的开头被执行，如果提供了一个初始化参数列表(`initializer list`),则初始化参数列表在父类构造函数执行之前被执行**. 具体的执行顺序如下:

1. `initializer list`（初始化参数列表）

2. `superclass’s no-arg constructor`（父类的无参构造函数）

3. `main class’s no-arg constructor`（子类的无参构造函数）


示例：

	class Person {
	  String firstName;
	  Person.fromJson(Map data) {
	    print('in Person');
	  }
	}
	
	class Employee extends Person {
	  // Person没有默认构造函数，所以必须手动调用
	  Employee.fromJson(Map data) : super.fromJson(data) {
	    print('in Employee');
	  }
	}
	
	main() {
	  var emp = new Employee.fromJson({});
	
	  // Prints:
	  // in Person
	  // in Employee
	  if (emp is Person) {
	    // Type check
	    emp.firstName = 'Bob';
	  }
	  (emp as Person).firstName = 'Bob';
	}

**因为父类构造函数的参数是在调用构造函数之前被计算的，所以给父类构造函数的参数可以是一个表达式，比如函数调用**

	class Employee extends Person {
	  Employee() : super.fromJson(getDefaultData());
	  // ···
	}

- 父类构造函数中的参数不能访问`this`,例如只能是静态方法,但是实例方法不行


## 10.7 初始化列表(`Initializer List`)

**构造函数主体执行前除了可以调用父类构造函数之外，还可以初始化实例参数**，初始化示例参数的表达式之间需要使用逗号`,`进行隔开

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

**在开发阶段，可以通过`assert()`方法来对初始化列表进行校验**

	Point.withAssert(this.x, this.y) : assert(x >= 0) {
	  print('In Point.withAssert(): ($x, $y)');
	}

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


## 10.8 重定向构造函数

**同一个类中的构造函数可以通过冒号`:`重定向到另外一个构造函数（包括父类）**

- **被重定向的构造函数主体为空**

示例:

	class Point {
	  num x;
	  num y;
	
	  // The main constructor for this class.
	  Point(this.x, this.y);
	
	  // Delegates to the main constructor.
	  Point.alongXAxis(num x) : this(x, 0);
	}

## 10.9 常量构造函数

**通过使用常量构造函数创建实例，可以提供一个状态不变的对象(即返回编译时常量)**

- **在构造函数前添加`const`关键字,同时声明所有类的变量为`final`**

示例：

	class ImmutablePoint {
	  final num x;
	  final num y;
	  const ImmutablePoint(this.x, this.y);
	  static final ImmutablePoint origin =
	      const ImmutablePoint(0, 0);
	}


## 10.10 工厂构造函数

**使用`factory`关键字标记的构造函数就是工厂方法构造函数，它表示构造函数不会总是返回一个新的对象!!!!**

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
	
- 工厂构造函数就像任何其他构造函数一样去使用

		var logger = Logger('UI');
		logger.log('Button clicked');


# 11 函数
**函数就是为类提供行为的方法**

## 11.1 实例函数
**对象的实例函数可以访问`this`以及实例变量**

示例：

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

## 11.2 `getter()`&`setter()`

**`getter()`和`setter()` 是用来设置和访问对象属性的特殊函数**

- 每个实例变量都隐含的具有一个`getter()`方法， 如果变量没有被`final`修饰，那么还有一个 `setter()`方法

- **使用`get`和`set`关键字定义变量的`getter()`和`setter()`方法
，可以额外的创建属性**.俩者可以单独使用！

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
	

## 11.3 抽象函数

实例函数，`getter`函数,`setter`函数都可以为抽象函数

- **抽象函数就是只定义了函数接口，但是没有提供具体的实现，其实现需要由子类提供**

- 抽象函数只能存在于抽象类中

- **抽象函数的特征就是用分号代替函数体`{...}`**

示例：

	abstract class Doer {
	  // ...Define instance variables and methods...
	
	  void doSomething(); // Define an abstract method.
	}
	
	class EffectiveDoer extends Doer {
	  void doSomething() {
	    // ...Provide an implementation, so the method is not abstract here...
	  }
	}


# 12 抽象类
**Dart使用`abstract`修饰符定义一个抽象类，抽象类无法被初始化**

- 抽象类通常用来定义接口,它可以拥有任意个数的抽象函数或具有具体实现的函数(即一个抽象类中可以全是抽象函数或没有抽象函数)

- **默认抽象类无法被实例化，但是通过定义 工厂构造函数，抽象类也可以实例化**

示例:

	// This class is declared abstract and thus
	// can't be instantiated.
	abstract class AbstractContainer {
	  // Define constructors, fields, methods...
	
	  void updateChildren(); // Abstract method.
	}

# 13 隐式接口

**每个类都隐式的定义了一个包含所有实例成员的接口，并且这个类实现了这个接口**

- 如果想创建类A来支持类B的api，但是不通过继承来实现，那么就可以通过实现这个隐式接口

**Dart通过`implements`关键字来实现一个或者多个接口**

	// A person. The implicit interface contains greet().
	class Person {
	  // 在接口中，但是仅仅在此库中可见
	  final _name;
	
	  // 不在接口中，因为是构造函数
	  Person(this._name);
	
	  // 在接口中
	  String greet(who) => 'Hello, $who. I am $_name.';
	}
	
	// Person接口的实现类
	class Imposter implements Person {
	  // 必须重新定义它
	  final _name = "";
	
	  String greet(who) => 'Hi $who. Do you know who I am?';
	}
	
	greetBob(Person person) => person.greet('bob');
	
	main() {
	  print(greetBob(new Person('kathy')));
	  print(greetBob(new Imposter()));
	}

- 构造函数不会存在隐式接口中

- 所以在隐式接口中的内容都需要被重写




# 14 扩展类
**Dart通过`extends`关键字实现继承，通过`super`关键字实现对父类的引用**

	class Television {
	  void turnOn() {
	    _illuminateDisplay();
	    _activateIrSensor();
	  }
	  // ···
	}
	
	class SmartTelevision extends Television {
	  void turnOn() {
	    super.turnOn();
	    _bootNetworkInterface();
	    _initializeMemory();
	    _upgradeApps();
	  }
	  // ···
	}

## 14.1 重载成员


子类可以重写实例函数,`getter()`函数和`setter()`函数,通过`@override`注解可以标明函数是对父类函数的重写

	class A {
	  // 重写了Object的noSuchMethod方法
	  @override
	  void noSuchMethod(Invocation mirror) {
	    print('You tried to use a non-existent member:' +
	          '${mirror.memberName}');
	  }
	}

要在类型安全的代码中缩小方法参数或实例变量的类型，可以使用`covariant`关键字


## 14.2 可重载的操作符

类中的操作符实际上就是类中的方法，Dart支持重写该方法来实现自定义逻辑

`<` | `+`| | `[]`
:---:|:---:|:---:|:---:
`>` | `/` |`^`|`[]=`
`<=`| `~/` |`&`	|`~`
`>=	`|`*`|`<<`|`==`
`–	`|`%`|`>>`| `|`


- 上面表格中的操作符都支持重写

- **`!=`不是一个可以重载的操作符。 表达式`e1 != e2`只是`!(e1 == e2)`的语法糖**

- 如果重写了`==`操作符，还应该重写对象的`hashCode`属性的`getter()`函数

**Dart通过`operator`关键字对操作符进行重写**

示例（使得`+`操作符支持`Vector`）：

	class Vector {
	  final int x;
	  final int y;
	  const Vector(this.x, this.y);
	
	  // 重写 + 操作符
	  Vector operator +(Vector v) {
	    return new Vector(x + v.x, y + v.y);
	  }
	
	  // 重写 - 操作符
	  Vector operator -(Vector v) {
	    return new Vector(x - v.x, y - v.y);
	  }
	}
	
	main() {
	  final v = new Vector(2, 3);
	  final w = new Vector(2, 2);
	
	  // v == (2, 3)
	  assert(v.x == 2 && v.y == 3);
	
	  // v + w == (4, 5)
	  assert((v + w).x == 4 && (v + w).y == 5);
	
	  // v - w == (0, 1)
	  assert((v - w).x == 0 && (v - w).y == 1);
	}


## 14.3 noSuchMethod()
**Dart通过重载`noSuchMethod()`方法，来获取代码调用不存在的方法或实例变量的行为**

	class A {
	  // Unless you override noSuchMethod, using a
	  // non-existent member results in a NoSuchMethodError.
	  @override
	  void noSuchMethod(Invocation invocation) {
	    print('You tried to use a non-existent member: ' +
	        '${invocation.memberName}');
	  }
	}

无法调用一个未实现的函数，除非以下几种情况:

1. 接收器拥有一个静态类型`dynamic`

2. 接收器有一个定义未实现方法的静态类型（抽象也可以），接收器的动态类型有一个`noSuchMethod()`方法，它与Object类中的实现不同

- [更多内容查看nosuchmethod-forwarding](https://github.com/dart-lang/sdk/blob/master/docs/language/informal/nosuchmethod-forwarding.md)

# 15 枚举类型
**枚举是一种特殊的类，用于表示固定数量的常量值**

## 15.1 使用枚举
**Dart提供了`enum`关键字用来定义枚举类型**

	enum Color { red, green, blue }

**枚举类型中的每个值都有一个被final修饰的`index`属性(即只有`getter()`函数),该`index`属性的值从0开始**

**枚举类拥有一个`values`常量，可以返回所有的枚举值**

	assert(Color.values.length == 3);
	

**枚举十分适合在`switch`语句中使用，因为如果没有处理所有的枚举值，那么会抛出警告!**
	
**枚举类型具有如下的限制：**

- 枚举类型无法被继承,无法使用 mixin,无法实现一个枚举类型

- 无法显示的实例化一个枚举类型

# 16 `mixin`(给类添加功能)
**混入(`Mixin`)指的是在多重类结构中重用代码. Dart通过在`with`关键字后添加一个或多个被混入的类名称来实现混入**

示例(展示如何使用混入类):

	class Musician extends Performer with Musical {
	  // ...
	}
	
	class Maestro extends Person
	    with Musical, Aggressive, Demented {
	  Maestro(String maestroName) {
	    name = maestroName;
	    canConduct = true;
	  }
	}

**要创建一个混入类需要遵循以下规则:**

1. 需要继承自`Object`,并且不允许提供构造函数(否则将被当做普通类来使用)

2. 此外还需要使用`mixin`关键字替代`class`关键字对类进行定义!

- 定义混入类示例:

		mixin Musical {
		  bool canPlayPiano = false;
		  bool canCompose = false;
		  bool canConduct = false;
		
		  void entertainMe() {
		    if (canPlayPiano) {
		      print('Playing piano');
		    } else if (canConduct) {
		      print('Waving hands');
		    } else {
		      print('Humming to self');
		    }
		  }
		}


**混入类可以通过`on`关键字来指定 实现混入类的类必须继承的类型**

	mixin A {
	  getName(){
	    return "aaaaaaa";
	  }
	}
	
	class C{
	}
	
	class B extends C with A{
	}

# 17 类变量和类方法

Dart使用`static`关键字实现类范围的变量和方法

- **类变量和类方法 又称为 静态变量和静态方法**

- 静态变量和静态方法可以直接通过 **类名.方法名** 或 **类名.变量名** 调用

## 17.1 静态变量
**静态变量适用于类范围的状态和常量**

	class Queue {
	  static const initialCapacity = 16;
	  // ···
	}
	
	void main() {
	  assert(Queue.initialCapacity == 16);
	}

- **Dart中静态变量只有在被使用时才初始化**

## 17.2 静态方法

**静态方法不需要通过类实例来访问，因此无法使用`this`**

	class P {
	  static sayHi(){
	  		print('hello ');
	  }
	}
	
	void main() {
	  P.sayHi();
	}

- **对于通用的方法，建议使用顶级方法替代静态方法**


# 18 泛型
**Dart通过`<>`标记法为类型指定泛型（参数化类型），例如文档中的列表为`List<E>`**

- 按照惯例，大多数表示泛型的类型变量是大写的单个字母,如`E T S K`

**使用泛型的好处：**

- 正确的指定泛型类型可以生成更好的代码

- 通过泛型可以减少代码重复,利于静态分析

- 确保类型安全


示例（确保列表只能接受`String`类型）:

	var names = List<String>();
	names.addAll(['Seth', 'Kathy', 'Lars']);
	names.add(42); // Error

示例(减少代码重复):

	abstract class ObjectCache {
	  Object getByKey(String key);
	  void setByKey(String key, Object value);
	}
	
	abstract class Cache<T> {
	  T getByKey(String key);
	  void setByKey(String key, T value);
	}

- **在这段代码中，T是一个占位符**	

- `ObjectCache`形式的类，如果想要修改value的类型，必须重新定义，但是使用泛型就不需要


## 18.1 泛型集合字面量

**`List`,`Set`和`Map`都可以使用泛型，它可以指定集合的key类型或者value类型，只需要在集合定义的花括号之前添加`<T>`即可实现对集合添加泛型**

	// 定义了泛型的列表
	var names = <String>['Seth', 'Kathy', 'Lars'];
	// 定义了泛型的Set
	var uniqueNames = <String>{'Seth', 'Kathy', 'Lars'};
	// 定义了泛型的Map
	var pages = <String, String>{
	  'index.html': 'Homepage',
	  'robots.txt': 'Hints for web robots',
	  'humans.txt': 'We are people, not machines'
	};

## 18.2 在使用构造函数时使用泛型
**构造函数中可以使用一个或多个泛型，只需要将`<T...>`添加到实例化类语句中的类名称之后**

	// 初始化了一个限定了类型的Set实例
	var nameSet = Set<String>.from(names);
	// 初始化了一个限定了类型的Map实例
	var views = Map<int, View>();

## 18.3 运行时使用泛型

**Dart中的泛型被具体化了，这意味着泛型可以在运行时被使用**，如下例子：

	  var names = List<String>();
	  names.addAll(['Seth', 'Kathy', 'Lars']);
	  assert(names is List<String>);
	  assert(names is! List<int>);
	
- Java中的泛型在运行时会被删除，也就是说在Java中无法判断列表的泛型

## 18.4 限制泛型

**Dart支持通过`extends`关键字对泛型进行限制**

	class Foo<T extends SomeBaseClass> {
	  // Implementation goes here...
	  String toString() => "Instance of 'Foo<$T>'";
	}
	
	class Extender extends SomeBaseClass {...}

- 实例化`Foo`时，如果指定任意继承自`SomeBaseClass`的泛型或者不指定泛型都不会报错，但是如果指定了非继承自`SomeBaseClass`的泛型那就会报错
		
		//success
		var someBaseClassFoo = Foo<SomeBaseClass>();
		//success 
		var someBaseClassFoo = Foo();
		// fail,不允许声明指定泛型之外的
		var foo = Foo<Object>();


## 18.5 泛型方法
一开始Dart不支持在方法上使用泛型，这是一项新功能,允许在方法上使用泛型

	T first<T>(List<T> ts) {
	  // Do some initial work or error checking, then...
	  T tmp = ts[0];
	  // Do some additional checking or processing...
	  return tmp;
	}

- 泛型的类型参数`first<T>`可以在以下几个地方使用

	1. 返回值类型

	2. 参数类型

	3. 局部变量


# 19 库和可见性
Dart提供了`import`和`library`指令来创建模块化和可共享的代码库

- 库不仅提供api，还是一个隐私单元，在库中所有以下划线(`_`)开头的标识符都只在库中可见

- **每个Dart程序都是一个库，即使它没有使用`library`指令**

- 库可以使用`package`关键字进行分发

## 19.1 库的使用
**使用`import`可以通过命名空间在一个库中引入另外一个库**

- 例如web应用程序通常使用`dart:html`库，其表示方式如下:

		import 'dart:html';

**导入库需要一个表示库地址的`URI`参数**

1. 对于内置库，可以使用一个特殊的scheme:`dart:`来对其进行表示

2. 对于包管理器中的库，可以使用特殊的scheme:`package:`来对其进行表示

3. 对于其他库，可以使用文件系统路径表示

		import 'package:test/test.dart';

- URI表示统一资源标识符，URL(统一资源定位符)是一种常见的URI

## 19.2 指定库的前缀
**如果导入了俩个标识符冲突的库，可以为一个或俩个库指定前缀**

示例(lib1和lib2都有一个`Element`类)

	import 'package:lib1/lib1.dart';
	import 'package:lib2/lib2.dart' as lib2;
	
	// Uses Element from lib1.
	Element element1 = Element();
	
	// Uses Element from lib2.
	lib2.Element element2 = lib2.Element();

## 19.3 导入部分库
**借助`show`和`hide`关键字，Dart允许选择性的导入库的内容**,示例如下:

	// Import only foo.
	import 'package:lib1/lib1.dart' show foo;
	
	// Import all names EXCEPT foo.
	import 'package:lib2/lib2.dart' hide foo;


## 19.4 延迟加载库
**Dart支持仅在需要时才加载库**，延迟加载库的使用场景如下

1. 减少App的启动时间

2. 执行A/B测试,例如尝试不同算法的不同实现

3. 加载平时很少使用的功能

**通过结合使用`import`和`deferred as`即可实现延迟加载库**

	import 'package:greetings/hello.dart' deferred as hello;

- **当需要使用到库中内容时，使用库的标识符调用`loadLibrary()`方法即可**

		Future greet() async {
		  await hello.loadLibrary();
		  hello.printGreeting();
		}
		
	- `await`代码用于暂停执行直到库加载成功

	- **`loadLibrary()`仅在首次对库进行加载，并不会每次都加载**


延迟加载库需要注意的点：

1. 延迟库的常量不是导入文件中的常量。只有当库加载完毕的时候，库中常量才可以使用

2. 不能在导入库的文件中使用来自延迟库的类型。如果存在延迟库和普通库都使用的类型，将其移动到一个公共库中

3. Dart默认会将`loadLibrary()`添加到延迟库中。`loadLibrary()`方法返回一个`Future`

## 19.5 实现库包并发布

[Create Library Packages](https://dart.dev/guides/libraries/create-library-packages)介绍了如果实现一个库包并发布


# 20 异步(`asynchrony support`)

Dart库中包含许多返回`Future`或`Stream`对象的函数
 
 - **这些函数是异步，并且这些异步函数在设置完耗时操作后（例如`I/O`）直接返回，而不会等待操作完成后返回**

Dart提供了`async`和`await`关键字支持异步编程,它们可以创建同步代码一样的异步代码

## 20.1 处理`Future`
**`Future`表示一个异步任务，如果需要其结果，可以有俩种方式**

1. 使用`async`和`await`

2. 使用`Future`的API

使用了`async`和`await`的代码就是异步的，但是这种代码看起来很像同步代码，例如下面使用`await`来等待一个异步函数执行结束
	
	Future checkVersion() async {
	  var version = await lookUpVersion();
	  // Do something with version
	}
	
- **`await`必须在一个异步方法中被使用(即被`async`标记的方法)**，`main()`方法也适用这条规则！

	异步方法可能会执行耗时操作，但是代码执行时并不会等待这个方法执行完毕。相反，异步方法只会执行`await`关键字后的第一个表达式，然后就会返回一个`Future`,程序会在表达式执行结束后恢复执行
	
- 可以使用`try-catch-finally`来处理`await`可能抛出的异常

`await`可以在异步方法中多次被使用

	var entrypoint = await findEntrypoint();
	var exitCode = await runExecutable(entrypoint, args);
	await flushThenExit(exitCode);


`await`表达式的值通常是一个`Future`,如果不是的话，Dart也会自动将其包装成一个`Future`(这个`Future`对象会返回表达式的值)。这时如果通过`Future`获取这个被包装的对象，会阻塞执行直到对象可用


## 20.2 定义异步方法
**异步方法就是方法体被`async`修饰符修饰的方法**

- 使用`async`修饰方法，会使得方法返回`Future`. 并且如果方法有返回值，那么会被`Future`自动包装

- **如果异步方法没有返回一个有用的值，那么可以指定其返回值为`void`**

示例:

	String lookUpVersion() => '1.0.0';
	//对上述代码进行改造，将其变成一个异步方法
	Future<String> lookUpVersion() async => '1.0.0';
	
	
## 20.2 处理`Stream`

如果需要从`Stream`中获取返回值，有俩种方式:

1. 使用`async`和一个异步`for`循环(`await for`)

	- 使用`await for`时需要确定确实需要等待所有流可能产生的结果，例如不要对`UI`事件回调使用，因为它会发送无穷无尽的事件流

2. 使用`Stream`的APi


### 20.2.1 `await for`
**异步的`for`循环形式如下：**

	await for (varOrType identifier in expression) {
	  // Executes each time the stream emits a value.
	}

- **`expression`的值必须是`Stream`类型**

其执行流程如下：

	1. 等待流发出一个值

	2. 将流的值设置给变量，然后执行循环的主体

	3. 重复上述过程直到流被关闭

**为了停止监听流，可以使用`break`或`continue`语句，它们将跳出循环并取消对流的订阅**

`await for`必须在一个异步方法中使用(即被`async`修饰的方法)


# 21 生成器(`Generator`)

**Dart提供了生成器方法，用来延迟生成一系列值. Dart内置了俩种生成器方法**

1. 同步生成器方法： 返回一个实现了`Iterable`的对象

2. 异步生成器方法： 返回一个`Stream`对象


**要实现一个同步生成器方法，需要使用`sync*`对函数主体进行修饰，然后使用`yield`返回值**

	Iterable<int> naturalsTo(int n) sync* {
	  int k = 0;
	  while (k < n) yield k++;
	}

**要实现一个异步生成器方法，需要使用`async*`对函数主体进行修饰，然后使用`yield`返回值**

	Stream<int> asynchronousNaturalsTo(int n) async* {
	  int k = 0;
	  while (k < n) yield k++;
	}	


**如果生成器方法是递归方法，那么可以通过使用`yield*`来提高性能**

	Iterable<int> naturalsDownFrom(int n) sync* {
	  if (n > 0) {
	    yield n;
	    yield* naturalsDownFrom(n - 1);
	  }
	}

# 22 可被调用的类实例(`Callable class`)

**通过给类添加一个`call()`方法，可以将类实例当做方法一样去调用**
	
	class WannabeFunction {
	  call(String a, String b, String c) => '$a $b $c!';
	}
	
	main() {
	  var wf = new WannabeFunction();
	  var out = wf("Hi","there,","gang");
	  print('$out');
	}


# 23 Isolates

现在的电脑，手机上基本上都是多核CPU，为了充分利用这些，开发者通常使用并发运行的共享内存线程，但是这种方案很容易导致问题发生


Dart的代码有时运行在`isolate`中而不是线程，每个`isolate`都有自己的内存堆，以此来确保其他`isolate`无法访问当前`isolate`

更多内容查看[dart-isolate-library](https://api.dartlang.org/stable/2.3.1/dart-isolate/dart-isolate-library.html)

# 24 Typedef
`typedef`(方法类型别名)为方法类型提供了一个名称，这样就可以在声明字段和返回值类型时使用 。当将一个函数类型对象分配给变量时，`typedef`用来保留类型信息


示例(不使用`typedef`)

	class SortedCollection {
	  Function compare;
	
	  SortedCollection(int f(Object a, Object b)) {
	    compare = f;
	  }
	}
	
	// Initial, broken implementation.
	int sort(Object a, Object b) => 0;
	
	void main() {
	  SortedCollection coll = SortedCollection(sort);
	
	  // All we know is that compare is a function,
	  // but what type of function?
	  assert(coll.compare is Function);
	}

- 这里仅仅知道`compare`变量保存的是一个`Function`类型，但是不知道具体的类型是什么...

- 当`compare = f`执行时，变量`f`的类型信息就丢失了。 **如果对变量使用显示的名称并保留类型信息，那么开发者或工具就可以使用这些信息**

示例(使用`typedef`)

	typedef Compare = int Function(Object a, Object b);
	
	class SortedCollection {
	  Compare compare;
	
	  SortedCollection(this.compare);
	}
	
	// Initial, broken implementation.
	int sort(Object a, Object b) => 0;
	
	void main() {
	  SortedCollection coll = SortedCollection(sort);
	  assert(coll.compare is Function);
	  assert(coll.compare is Compare);
	}
	
- 目前来说，`typeof`仅适用于方法类型


由于`typedef`只是别名，Dart还提供了一种检查任何function类型的方法

	typedef Compare<T> = int Function(T a, T b);
	
	int sort(int a, int b) => a - b;
	
	void main() {
	  assert(sort is Compare<int>); // True!
	}


# 25 元数据(`metadata`)

使用元数据可以给代码提供额外的信息。元数据注解以`@`开头，然后是对编译时常量的引用(例如`deprecated`)或对常量构造函数的调用

**`@deprecated`和`@override`俩个注解对Dart中所有代码都适用**

	class Television {
	  /// _Deprecated: Use [turnOn] instead._
	  @deprecated
	  void activate() {
	    turnOn();
	  }
	
	  /// Turns the TV's power on.
	  void turnOn() {...}
	}

**Dart支持自定义元数据注解**,下面展示一个自定义的`@todo`注解:

	library todo;
	
	class Todo {
	  final String who;
	  final String what;
	
	  const Todo(this.who, this.what);
	}

	// 使用todo注解
	import 'todo.dart';
	
	@Todo('seth', 'make this do something')
	void doSomething() {
	  print('do something');
	}	

**元数据可以出现在库、类、`typedef `、` type parameter, `、构造函数、工厂、函数、字段、参数或变量声明之前，也可以出现在导入或导出指令之前**

- 可以在运行时使用反射检索元数据


# 26 注释

Dart支持单行注释，多行注释，文档注释

- 单行注释以`//`开头，每行注释都会被Dart编译器忽略

- 多行注释以`/*`开头，以`*/`结尾，所以多行注释中的内容都会被Dart编译器忽略

- 文档注释以`///`开头或`/**`开头，`///`连续用在多行上 效果就和多行注释一样

	文档注释中会忽略除了被圆括号包裹外的任何内容，可以在圆括号中指向类，方法，字段，顶级变量，函数，以及参数



示例（文档注释）:

	/// A domesticated South American camelid (Lama glama).
	///
	/// Andean cultures have used llamas as meat and pack
	/// animals since pre-Hispanic times.
	class Llama {
	  String name;
	
	  /// Feeds your llama [Food].
	  ///
	  /// The typical llama eats one bale of hay per week.
	  void feed(Food food) {
	    // ...
	  }
	
	  /// Exercises your llama with an [activity] for
	  /// [timeLimit] minutes.
	  void exercise(Activity activity, int timeLimit) {
	    // ...
	  }
	}

- 在生成的文档中，`[Food]`会成为指向Food类的超链接

[DartDoc](https://github.com/dart-lang/dartdoc#dartdoc)可以用来生成Dart代码的HTML类型文档
















	