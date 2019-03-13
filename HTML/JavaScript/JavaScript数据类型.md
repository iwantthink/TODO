# JavaScript数据类型

[JavaScript数据类型-阮一峰](https://wangdoc.com/javascript/types/index.html)

# 1. JS 数据类型

JavaScript 语言的每一个值，都属于某一种数据类型。JavaScript 的数据类型，共有六种。（ES6 又新增了第七种 Symbol 类型的值）

- 数值（number）：整数和小数（比如1和3.14）

- 字符串（string）：文本（比如Hello World）。

- 布尔值（boolean）：表示真伪的两个特殊值，即true（真）和false（假）

- undefined：表示“未定义”或不存在，即由于目前没有定义，所以此处暂时没有任何值

- null：表示空值，即此处的值为空。

- 对象（object）：各种值组成的集合。


**JavaScript 拥有动态类型。这意味着相同的变量可用作不同的类型**：

	var x                // x 为 undefined
	var x = 6;           // x 为数字
	var x = "Bill";      // x 为字符串

## 1.1 数据类型分类

1. 通常，数值、字符串、布尔值这三种类型，合称为**原始类型**（primitive type）的值，即它们是最基本的数据类型，不能再细分了。

2. 对象则称为**合成类型**（complex type）的值，因为一个对象往往是多个原始类型的值的合成，可以看作是一个存放各种值的容器。

	**对象是最复杂的数据类型，又可以分成三个子类型**。

	- 狭义的对象（object）

	- 数组（array）

	- 函数（function）

		**函数其实是处理数据的方法，JavaScript 把它当成一种数据类型，可以赋值给变量，这为编程带来了很大的灵活性，也为 JavaScript 的“函数式编程”奠定了基础**

3. 至于undefined和null，一般将它们看成两个特殊值

## 1.2 `typeof` 运算符

JavaScript 有三种方法，可以确定一个值到底是什么类型。

- typeof 运算符

	typeof运算符可以返回一个值的数据类型。

- instanceof 运算符

- Object.prototype.toString方法

示例(原始类型)：

	typeof 123 // "number"
	typeof '123' // "string"
	typeof false // "boolean"

	function f() {}
	typeof f // "function"

`typeof`在检查一个没有声明的变量时不会报错:

	typeof undefined // "undefined"

- **利用这点,`typeof`可以用来检查一个没有声明的变量,而不报错!**

		v // ReferenceError: v is not defined
		typeof v // "undefined"

	- 上面代码中，变量v没有用var命令声明，直接使用就会报错。但是，放在typeof后面，就不报错了，而是返回undefined

示例(对象):

	typeof window // "object"
	typeof {} // "object"
	typeof [] // "object"

- **空数组（`[]`）的类型也是object，这表示在 JavaScript 内部，数组本质上只是一种特殊的对象。**

	**这里顺便提一下，instanceof运算符可以区分数组和对象**

		var o = {};
		var a = [];
		
		o instanceof Array // false
		a instanceof Array // true

---

	typeof null // "object"

- null的类型是object，这是由于历史原因造成的。1995年的 JavaScript 语言第一版，只设计了五种数据类型（对象、整数、浮点数、字符串和布尔值），没考虑null，只把它当作object的一种特殊值。后来null独立出来，作为一种单独的数据类型，为了兼容以前的代码，typeof null返回object就没法改变了


# 3. null,undefined和布尔值

## 3.1 null和undefined介绍

null与undefined都可以表示“没有”，含义非常相似。将一个变量赋值为undefined或null，语法效果几乎没区别


	if (!undefined) {
	  console.log('undefined is false');
	}
	// undefined is false
	
	if (!null) {
	  console.log('null is false');
	}
	// null is false
	
	undefined == null
	// true

-  在if语句中,俩者都会被自动转成false,相等运算符报告俩者相等

- 之所以存在这俩者,是因为历史原因.**俩者的差异是** :

	1. null是一个表示“空”的对象，转为数值时为0；
	2. undefined是一个表示"此处无定义"的原始值，转为数值时为NaN

## 3.2 null和undefined的用法和含义

1. null表示空值，即该处的值现在为空。调用函数时，某个参数未设置任何值，这时就可以传入null，表示该参数为空。

	- 比如，某个函数接受引擎抛出的错误作为参数，如果运行过程中未出错，那么这个参数就会传入null，表示未发生错误。

2. undefined表示“未定义”，下面是返回undefined的典型场景。

		// 变量声明了，但没有赋值
		var i;
		i // undefined
		
		// 调用函数时，应该提供的参数没有提供，该参数等于 undefined
		function f(x) {
		  return x;
		}
		f() // undefined
		
		// 对象没有赋值的属性
		var  o = new Object();
		o.p // undefined
		
		// 函数没有返回值时，默认返回 undefined
		function f() {}
		f() // undefined


# 4. 布尔值

布尔值代表“真”和“假”两个状态。“真”用关键字true表示，“假”用关键字false表示。布尔值只有这两个值。

下列运算符会返回布尔值：

- 前置逻辑运算符： ! (Not)

- 相等运算符：===，!==，==，!=

- 比较运算符：>，>=，<，<=

**如果 JavaScript 预期某个位置应该是布尔值，会将该位置上现有的值自动转为布尔值**。转换规则是除了下面六个值被转为false，其他值都视为true。

1. `undefined`
2. `null`
3. `false`
4. `0`
5. `NaN`
6. `""`或`''`（空字符串）

- 注意，空数组（`[]`）和空对象（`{}`）对应的布尔值，都是true


# 5. 数值

JavaScript 内部，所有数字都是以64位浮点数形式储存，即使整数也是如此(**也就是说,JS语言的底层根本没有整数,所有数字都是小数(64位浮点数)**)。

- 所以，1与1.0是相同的，是同一个数

		1 === 1.0 // true

- 但是**某些运算只有整数才能完成，此时 JavaScript 会自动把64位浮点数，转成32位整数，然后再进行运算**


**由于浮点数不是精确的值，所以涉及小数的比较和运算要特别小心**

	0.1 + 0.2 === 0.3  // false
	
	0.3 / 0.1 // 2.9999999999999996
	
	(0.3 - 0.2) === (0.2 - 0.1)  // false


## 5.1 数值精度

	(-1)^符号位 * 1.xx...xx * 2^指数部分

- 上面公式是正常情况下（指数部分在0到2047之间），一个数在 JavaScript 内部实际的表示形式。

精度最多只能到53个二进制位，这意味着，绝对值小于2的53次方的整数，即-2^53到2^53，都可以精确表示。

- 简单的法则就是，JavaScript 对15位的十进制数都可以精确处理。

## 5.2 数值范围

 JavaScript 能够表示的数值范围为2^1024到2^-1023（开区间），超出这个范围的数无法表示。

如果一个数大于等于2的1024次方，那么就会发生“正向溢出”，即 JavaScript 无法表示这么大的数，这时就会返回Infinity。

	Math.pow(2, 1024) // Infinity

如果一个数小于等于2的-1075次方（指数部分最小值-1023，再加上小数部分的52位），那么就会发生为“负向溢出”，即 JavaScript 无法表示这么小的数，这时会直接返回0。

	Math.pow(2, -1075) // 0


- JavaScript 提供Number对象的`MAX_VALUE`和`MIN_VALUE`属性，返回可以表示的具体的最大值和最小值。



## 5.3 数值的表示法

JavaScript 的数值有多种表示方法，可以用字面形式直接表示，比如35（十进制）和0xFF（十六进制）。

数值也可以采用科学计数法表示，下面是几个科学计数法的例子。

	123e3 // 123000
	123e-3 // 0.123
	-3.1E+12
	.1e-23

- 科学计数法允许字母e或E的后面，跟着一个整数，表示这个数值的指数部分。


以下两种情况，JavaScript 会自动将数值转为科学计数法表示，其他情况都采用字面形式直接表示。

1. 小数点前的数字多于21位
2. 小数点后的零多于5个


## 5.4 数值的进制

使用字面量（literal）直接表示一个数值时，JavaScript 对整数提供四种进制的表示方法：

1. 十进制：没有前导0的数值。
2. 八进制：有前缀0o或0O的数值，或者有前导0、且只用到0-7的八个阿拉伯数字的数值。
3. 十六进制：有前缀0x或0X的数值。
4. 二进制：有前缀0b或0B的数值。

# 6. 特殊数值

## 6.1 正零和负零

JavaScript 的64位浮点数之中，有一个二进制位是符号位。这意味着，**任何一个数都有一个对应的负值，就连0也不例外**。

- 一个是+0，一个是-0，区别就是64位浮点数表示法的符号位不同。它们是等价的。

		-0 === +0 // true
		0 === -0 // true
		0 === +0 // true

- 几乎所有场合俩者都被当做正常的0,唯一有区别的是,+0或-0 当做分母时,返回的值是不相等的

		(1 / +0) === (1 / -0) // false

	- 因为除以正零得到`+Infinity`，除以负零得到`-Infinity`，这两者是不相等的

## 6.2 NaN

NaN是 JavaScript 的特殊值，表示“非数字”（Not a Number）

- 主要出现在将字符串解析成数字出错的场合。

		5 - 'x' // NaN
		Math.acos(2) // NaN
		Math.log(-1) // NaN
		Math.sqrt(-1) // NaN
		0 / 0 // NaN

- NaN不是独立的数据类型，而是一个特殊数值，**它的数据类型依然属于Number**，使用typeof运算符可以看得很清楚

		typeof NaN // 'number'

**运算规则:**


1. NaN不等于任何值，包括它本身。

		NaN === NaN // false

2. 数组的indexOf方法内部使用的是严格相等运算符，所以该方法对NaN不成立。

		[NaN].indexOf(NaN) // -1

3. NaN在布尔运算时被当作false。

		Boolean(NaN) // false

4. NaN与任何数（包括它自己）的运算，得到的都是NaN。

		NaN + 32 // NaN
		NaN - 32 // NaN
		NaN * 32 // NaN
		NaN / 32 // NaN


## 6.3 Infinity

	// 场景一
	Math.pow(2, 1024)
	// Infinity
	
	// 场景二
	0 / 0 // NaN
	1 / 0 // Infinity

Infinity表示“无穷”，用来表示两种场景。

1. 一种是一个正的数值太大，或一个负的数值太小，无法表示；
2. 另一种是非0数值除以0，得到Infinity。


	Infinity === -Infinity // false
	
	1 / -0 // -Infinity
	-1 / -0 // Infinity


- Infinity有正负之分，Infinity表示正的无穷，-Infinity表示负的无穷


由于数值正向溢出（overflow）、负向溢出（underflow）和被0除，JavaScript 都不报错，所以单纯的数学运算几乎没有可能抛出错误。

- Infinity大于一切数值（除了NaN），-Infinity小于一切数值（除了NaN）。

		Infinity > 1000 // true
		-Infinity < -1000 // true
		Infinity与NaN比较，总是返回false。
		
		Infinity > NaN // false
		-Infinity > NaN // false
		
		Infinity < NaN // false
		-Infinity < NaN // false


**运算规则:**

Infinity的四则运算，符合无穷的数学计算规则。

	5 * Infinity // Infinity
	5 - Infinity // -Infinity
	Infinity / 5 // Infinity
	5 / Infinity // 0

0乘以Infinity，返回NaN；0除以Infinity，返回0；Infinity除以0，返回Infinity。

	0 * Infinity // NaN
	0 / Infinity // 0
	Infinity / 0 // Infinity

Infinity加上或乘以Infinity，返回的还是Infinity。

	Infinity + Infinity // Infinity
	Infinity * Infinity // Infinity

Infinity减去或除以Infinity，得到NaN。

	Infinity - Infinity // NaN
	Infinity / Infinity // NaN

Infinity与null计算时，null会转成0，等同于与0的计算。

	null * Infinity // NaN
	null / Infinity // 0
	Infinity / null // Infinity

Infinity与undefined计算，返回的都是NaN。

	undefined + Infinity // NaN
	undefined - Infinity // NaN
	undefined * Infinity // NaN
	undefined / Infinity // NaN
	Infinity / undefined // NaN


# 7. 与数值相关的全局方法

## 7.1 parseInt()

用于将字符串转为整数

- 如果字符串头部有空格，空格会被自动去除。
- 如果parseInt的参数不是字符串，则会先转为字符串再转换。
- 字符串转为整数的时候，是一个个字符依次转换，如果遇到不能转为数字的字符，就不再进行下去，返回已经转好的部分。
- 如果字符串的第一个字符不能转化为数字（后面跟着数字的正负号除外），返回NaN。
- parseInt的返回值只有两种可能，要么是一个十进制整数，要么是NaN
- 如果字符串以0x或0X开头，parseInt会将其按照十六进制数解析。
- 对于那些会自动转为科学计数法的数字，parseInt会将科学计数法的表示方法视为字符串，因此导致一些奇怪的结果。


**进制转换:**

parseInt方法还可以接受第二个参数（2到36之间），表示被解析的值的进制，返回该值对应的十进制数。

- 默认情况下，parseInt的第二个参数为10，即默认是十进制转十进制

- 如果第二个参数不是数值，会被自动转为一个整数。这个整数只有在2到36之间，才能得到有意义的结果，超出这个范围，则返回NaN。如果第二个参数是0、undefined和null，则直接忽略。


## 7.2 parseFloat()

用于将一个字符串转为浮点数。

- 如果字符串符合科学计数法，则会进行相应的转换。
- 如果字符串包含不能转为浮点数的字符，则不再进行往后转换，返回已经转好的部分。
- parseFloat方法会自动过滤字符串前导的空格。
- 如果参数不是字符串，或者字符串的第一个字符不能转化为浮点数，则返回NaN。

这些特点使得parseFloat的转换结果不同于Number函数


## 7.3 isNaN()

用来判断一个值是否为NaN

- isNaN只对数值有效，如果传入其他值，会被先转成数值。

	比如，传入字符串的时候，字符串会被先转成NaN，所以最后返回true，这一点要特别引起注意。也就是说，isNaN为true的值，有可能不是NaN，而是一个字符串。

	- 对于对象和数组，isNaN也返回true 
	- 但是，对于空数组和只有一个数值成员的数组，isNaN返回false

**判断NaN更可靠的方法是，利用NaN为唯一不等于自身的值的这个特点，进行判断。**

	function myIsNaN(value) {
	  return value !== value;
	}

## 7.4 isFinite()

isFinite方法返回一个布尔值，表示某个值是否为正常的数值。

- **除了Infinity、-Infinity、NaN和undefined这几个值会返回false**，isFinite对于其他的数值都会返回true。


# 8. 字符串

字符串就是零个或多个排在一起的字符，放在单引号或双引号之中。

- 单引号字符串的内部，可以使用双引号。双引号字符串的内部，可以使用单引号。

- 如果要在单引号字符串的内部，使用单引号，就必须在内部的单引号前面加上反斜杠(`\`)，用来转义。双引号字符串内部使用双引号，也是如此

- 由于 HTML 语言的属性值使用双引号，所以很多项目约定 JavaScript 语言的字符串只使用单引号

- 字符串默认只能写在一行内，分成多行将会报错。

	- 如果长字符串必须分成多行，可以在每一行的尾部使用反斜杠(`\`)。但是输出的内容还是单行,效果与写在同一行一样

		反斜杠后面必须是换行符,不能有其他字符(比如空格)

- 连接运算符（+）可以连接多个单行字符串，将长字符串拆成多行书写，输出的时候也是单行。

## 8.1 转义

反斜杠（`\`）在字符串内有特殊含义，用来表示一些特殊字符，所以又称为转义符。

- 如果在非特殊字符前面使用反斜杠，则反斜杠会被省略。

	- `\0` ：null（\u0000）
	- `\b` ：后退键（\u0008）
	- `\f` ：换页符（\u000C）
	- `\n` ：换行符（\u000A）
	- `\r` ：回车键（\u000D）
	- `\t` ：制表符（\u0009）
	- `\v` ：垂直制表符（\u000B）
	- `\'` ：单引号（\u0027）
	- `\"` ：双引号（\u0022）
	- `\\` ：反斜杠（\u005C）

**反斜杠还有三种特殊用法。**

1. `\HHH`

	反斜杠后面紧跟三个八进制数（000到377），代表一个字符。HHH对应该字符的 Unicode 码点，比如\251表示版权符号。显然，这种方法只能输出256种字符。

2. `\xHH`

	\x后面紧跟两个十六进制数（00到FF），代表一个字符。HH对应该字符的 Unicode 码点，比如\xA9表示版权符号。这种方法也只能输出256种字符。

3. `\uXXXX`

	\u后面紧跟四个十六进制数（0000到FFFF），代表一个字符。XXXX对应该字符的 Unicode 码点，比如\u00A9表示版权符号。


## 8.2 字符串与数组

字符串可以被视为字符数组，因此可以使用数组的方括号运算符，用来返回某个位置的字符（位置编号从0开始）。

	var s = 'hello';
	s[0] // "h"
	s[1] // "e"
	s[4] // "o"
	
	// 直接对字符串使用方括号运算符
	'hello'[1] // "e"

- 如果方括号中的数字超过字符串的长度，或者方括号中根本不是数字，则返回undefined。

- 实际上，无法改变字符串之中的单个字符

## 8.3 leng属性

length属性返回字符串的长度，该属性也是无法改变的。

## 8.4 字符集

JavaScript 使用 Unicode 字符集。**JavaScript 引擎内部，所有字符都用 Unicode 表示**。

- JavaScript 不仅以 Unicode 储存字符，还允许直接在程序中使用 Unicode 码点表示字符，即将字符写成`\uxxxx`的形式，其中xxxx代表该字符的 Unicode 码点

- 解析代码的时候，JavaScript 会自动识别一个字符是字面形式表示，还是 Unicode 形式表示。输出给用户的时候，所有字符都会转成字面形式。

		var f\u006F\u006F = 'abc';
		foo // "abc"

	- 第一行变量名是Unicode形式
	- 第二行是字面形式表示


每个字符在 JavaScript 内部都是以16位（即2个字节）的 UTF-16 格式储存。也就是说，JavaScript 的单位字符长度固定为16位长度，即2个字节

- 但是，UTF-16 有两种长度：

	1. 对于码点在U+0000到U+FFFF之间的字符，长度为16位（即2个字节）；
	2. 对于码点在U+10000到U+10FFFF之间的字符，长度为32位（即4个字节），而且前两个字节在0xD800到0xDBFF之间，后两个字节在0xDC00到0xDFFF之间。举例来说，码点U+1D306对应的字符为𝌆，它写成 UTF-16 就是0xD834 0xDF06。

由于历史原因，只支持两字节的字符，不支持四字节的字符，那么对于码点在U+10000到U+10FFFF之间的字符，JavaScript 总是认为它们是两个字符（length属性为2）。**所以处理的时候，必须把这一点考虑在内，也就是说，JavaScript 返回的字符串长度可能是不正确的**。

## 8.5 Base64转码

有时，文本里面包含一些不可打印的符号，比如 ASCII 码0到31的符号都无法打印出来，这时可以使用 Base64 编码，将它们转成可以打印的字符。

- 另一个场景是，有时需要以文本格式传递二进制数据，那么也可以使用 Base64 编码

- 所谓 Base64 就是一种编码方法，可以将任意值转成 0～9、A～Z、a-z、+和`/`这64个字符组成的可打印字符。使用它的主要目的，不是为了加密，而是为了不出现特殊字符，简化程序的处理。


JavaScript 原生提供两个 Base64 相关的方法。

1. `btoa()`：任意值转为 Base64 编码

2. `atob()`：Base64 编码转为原来的值

- **这俩个方法不适合非ASCII码的字符**,所以如果要将非ASCII的字符转换为Base64编码,需要一个转码过程


	function b64Encode(str) {
	  return btoa(encodeURIComponent(str));
	}
	
	function b64Decode(str) {
	  return decodeURIComponent(atob(str));
	}
	
	b64Encode('你好') // "JUU0JUJEJUEwJUU1JUE1JUJE"
	b64Decode('JUU0JUJEJUEwJUU1JUE1JUJE') // "你好"



# 9. 对象

对象（object）是 JavaScript 语言的核心概念，也是最重要的数据类型。

**什么是对象**？

- 简单说，对象就是一组“键值对”（key-value）的集合，是一种无序的复合数据集合。或者说是一个包含相关数据和方法的集合（通常由一些变量和函数组成，我们称之为对象里面的属性和方法）

## 9.1 定义对象

### 9.1.1 使用对象字面量定义对象

		var obj = {
			foo: 'Hello',// 对象的属性
			bar: 'World',// 对象的属性
			print: function(){
				console.log('print log!') // 对象的方法
			}
		};

- 上面代码中，大括号就定义了一个对象，它被赋值给变量obj，所以变量obj就指向一个对象。

	该对象内部包含两个键值对（又称为两个“成员”），第一个键值对是foo: 'Hello'，其中foo是“键名”（成员的名称），字符串Hello是“键值”（成员的值）。

- **键名与键值之间用冒号分隔**。第二个键值对是bar: 'World'，bar是键名，World是键值。两个键值对之间用逗号分隔。

### 9.1.2 使用构造函数定义对象

JavaScript 用一种称为构建函数的特殊函数来定义对象和它们的特征。

- 构建函数非常有用，因为很多情况下不知道实际需要多少个对象（实例）。

- 构建函数提供了创建所需对象（实例）的有效方法，将对象的数据和特征函数按需联结至相应对象。


示例(构建函数):

	function Person(name) {
	  this.name = name;
	  this.greeting = function() {
	    alert('Hi! I\'m ' + this.name + '.');
	  };
	}

	var person1 = new Person('Bob');

- 这个构建函数就是JS版本的类,**构建函数名称的首字通常是大写的,便于区分构建函数和普通函数**

- 这里使用了`this`关键词，即无论是该对象的哪个实例被这个构建函数创建，它的 name 属性就是传递到构建函数形参name的值

- 当新的对象被创建,变量person1有效地包含了以下值:

		{
		  name : 'Bob',
		  greeting : function() {
		    alert('Hi! I\'m ' + this.name + '.');
		  }
		}

	- 实际的方法仍然是定义在类里面的,而不是在对象实例里面

### 9.1.3 使用`Object()`构造函数定义对象

使用`Object()`构造函数能够创建一个空对象,将空对象赋值给变量之后,可以向这个空对象添加属性和方法

	// 创建空对象
	var person1 = new Object();
	// 添加属性和方法
	person1.name = 'Chris';
	person1['age'] = 38;
	person1.greeting = function() {
	  alert('Hi! I\'m ' + this.name + '.');
	}

还可以将对象文本传递给`Object()`构造函数作为参数,以便用属性/方法填充它


	var person1 = new Object({
	  name : 'Chris',
	  age : 38,
	  greeting : function() {
	    alert('Hi! I\'m ' + this.name + '.');
	  }
	});

### 9.1.4 使用`create()`方法创建对象

JavaScript有个内嵌的方法`create()`, 它允许您基于现有对象创建新的对象实例。

	var person2 = Object.create(person1);

	person2.name // Chirs
	person2.greeting() // HI!!.....

- person2 是基于 person1 创建的,它们具有相同的属性和方法

- 这十分灵活,因为它允许不通过构造函数创建新的对象实例. 但是缺点是比起构造函数浏览器在比较新的版本上才支持`create()`方法


## 9.2 键名

**对象的所有键名都是字符串**（ES6 又引入了 Symbol 值也可以作为键名），所以加不加引号都可以。上面的代码也可以写成下面这样

	var obj = {
	  'foo': 'Hello',
	  'bar': 'World'
	};

- 对象的属性之间用逗号分隔，最后一个属性后面可以加逗号（trailing comma），也可以不加。

- 如果键名是数值，会被自动转为字符串。

- 如果键名不符合标识名的条件（比如第一个字符为数字，或者含有空格或运算符），且也不是数字，则必须加上引号，否则会报错


**对象的每一个键名又称为“属性”（property）**，它的“键值”可以是任何数据类型。

- **如果一个属性的值为函数，通常把这个属性称为“方法”**，它可以像函数那样调用

		var obj = {
		  p: function (x) {
		    return 2 * x;
		  }
		};

- 如果属性的值还是一个对象，就形成了链式引用。

		var o1 = {};
		var o2 = { bar: 'hello' };
		
		o1.foo = o2;
		o1.foo.bar // "hello"

- 属性可以动态创建，不必在对象声明时就指定。

## 9.3 对象的引用

如果不同的变量名指向同一个对象，那么它们都是这个对象的引用，也就是说指向同一个内存地址。修改其中一个变量，会影响到其他所有变量

- 但是，**这种引用只局限于对象**，如果两个变量指向同一个原始类型的值。那么，变量这时都是值的拷贝。


## 9.4 表达式还是语句?

对象采用花括号表示，这导致了一个问题：如果行首是一个花括号，它到底是表达式还是语句？

	{ foo: 123 }

- JavaScript 引擎读到上面这行代码，会发现可能有两种含义。第一种可能是，这是一个表达式，表示一个包含foo属性的对象；第二种可能是，这是一个语句，表示一个代码区块，里面有一个标签foo，指向表达式123

	为了避免这种歧义，JavaScript 引擎的做法是，**如果遇到这种情况，无法确定是对象还是代码块，一律解释为代码块**。


- **如果要解释为对象，最好在花括号前加上圆括号**。因为圆括号的里面，只能是表达式，所以确保花括号只能解释为对象

- 这种差异在eval语句（作用是对字符串求值）中反映得最明显。

		eval('{foo: 123}') // 123
		eval('({foo: 123})') // {foo: 123}

	- 如果没有圆括号，eval将其理解为一个代码块；加上圆括号以后，就理解成一个对象。



## 9.5 属性操作

### 9.5.1 属性的读取

	var obj = {
	  p: 'Hello World'
	};
	
	obj.p // p 被当做字符串
	obj['p'] // 传入'p'作为键名
	obj[p] // p被当做变量 error

读取对象的属性，有两种表示方法:

1. 一种是使用点表示法(`object.key`)

	- 数值键名不能使用点表示法（因为会被当成小数点），只能使用方括号表示法

	- 可以用一个对象来作为另外一个对象成员的值(这样实际上是创建了一个子命名空间)

2. 一种是使用方括号表示法(`object[key]`)

	- 如果使用方括号表示法，键名必须放在引号里面，否则会被当作变量处理
	- 方括号表示法的括号内部还可以使用表达式。
	- 数字键可以不加引号，因为会自动转成字符串。
	- **从根本上来说,这跟访问数组的元素是一样的原理. 使用关联了值的名字而不是索引去选择元素**. 对象有时也被称之为**关联数组(associative array)**,因为对象做了字符串到值的映射,而数组做的是数字到值的映射

### 9.5.2 属性的赋值

	var obj = {};
	
	obj.foo = 'Hello';
	obj['bar'] = 'World';

点运算符和方括号运算符，不仅可以用来读取值，还可以用来赋值。

- **JavaScript 允许属性的“后绑定”，也就是说，你可以在任意时刻新增属性，没必要在定义对象的时候，就定义好属性**。

### 9.5.3 属性的查看

	var obj = {
	  key1: 1,
	  key2: 2
	};

	Object.keys(obj);

查看一个对象本身的所有属性，可以使用`Object.keys`方法

### 9.5.4 属性的删除:delete命令

	var obj = { p: 1 };
	Object.keys(obj) // ["p"]
	
	delete obj.p // true
	obj.p // undefined
	Object.keys(obj) // []

delete命令用于删除对象的属性，删除成功后返回true。

- delete命令只能删除对象本身的属性，无法删除继承的属性

- 删除一个不存在的属性，delete不报错，而且返回true (**因此不能根据delete的结果来判断是否存在一个属性**)

只有一种情况，delete命令会返回false，那就是该属性存在，且不得删除。

	var obj = Object.defineProperty({}, 'p', {
	  value: 123,
	  configurable: false
	});
	
	obj.p // 123
	delete obj.p // false


### 9.5.5 属性是否存在:in 运算符

	var obj = { p: 1 };
	'p' in obj // true
	'toString' in obj // true


in运算符用于检查对象是否包含某个属性（注意，检查的是键名，不是键值），如果包含就返回true，否则返回false。它的左边是一个字符串，表示属性名，右边是一个对象。

- in运算符的一个问题是，它不能识别哪些属性是对象自身的，哪些属性是继承的。就像上面代码中，对象obj本身并没有toString属性，但是in运算符会返回true，因为这个属性是继承的。

	- 可以使用对象的hasOwnProperty方法判断一下，是否为对象自身的属性

			var obj = {};
			if ('toString' in obj) {
			  console.log(obj.hasOwnProperty('toString')) // false
			}

### 9.5.6 属性的遍历:for...in循环

	var obj = {a: 1, b: 2, c: 3};
	
	for (var i in obj) {
	  console.log('键名：', i);
	  console.log('键值：', obj[i]);
	}

for...in循环用来遍历一个对象的全部属性。

有两个使用注意点。

1. 它遍历的是对象所有可遍历（enumerable）的属性，会跳过不可遍历的属性。

2. 它不仅遍历对象自身的属性，还遍历继承的属性。


## 9.6 with语句

	with (对象) {
	  语句;
	}

- 它的作用是操作同一个对象的多个属性时,提供一些书写的方便


	// 例一
	var obj = {
	  p1: 1,
	  p2: 2,
	};
	with (obj) {
	  p1 = 4;
	  p2 = 5;
	}

	// 例二
	with (document.links[0]){
	  console.log(href);
	  console.log(title);
	  console.log(style);
	}

**如果with区块内部有变量的赋值操作，必须是当前对象已经存在的属性，否则会创造一个当前作用域的全局变量**

	var obj = {};
	with (obj) {
	  p1 = 4; // p1当前obj对象不存在
	  p2 = 5; // p2当前obj对象不存在
	}
	
	obj.p1 // undefined
	p1 // 4

- 上面代码中，对象obj并没有p1属性，对p1赋值等于创造了一个全局变量p1。正确的写法应该是，先定义对象obj的属性p1，然后在with区块内操作它。

	这是因为with区块没有改变作用域，它的内部依然是当前作用域。这造成了with语句的一个很大的弊病，就是绑定对象不明确。

# 10. 函数

函数是一段可以反复调用的代码块。函数还能接受输入的参数，不同的参数会返回不同的值。

JavaScript拥有许多的内置函数,事实上,许多浏览器内置函数的实现代码并不是使用JavaScript实现的(大多数浏览器后台的函数代码是使用C++这样更低级的系统语言编写的,而不是**JS这样的Web编程语言**)

- 这些浏览器内置函数并不是核心JavaScript语言的一部分,而是被定义为**浏览器API的一部分**

- 严格说来，内置浏览器函数并不是函数(**它们是方法**),俩者在很大的程度上是可以互换的

	**俩者的区别在于:方法是对象内定义的函数**.浏览器内置函数(方法)和变量(属性)存储在结构化对象中


## 10.1 函数的声明

1. **function命令**

	function命令声明的代码区块，就是一个函数。function命令后面是函数名，函数名后面是一对圆括号，里面是传入函数的参数。函数体放在大括号里面。
		
		function print(s) {
		  console.log(s);
		}

	- 这叫做函数的声明（Function Declaration）


2. **函数表达式**

	除了function命令,还可以采用变量赋值的写法

		var print = function(s) {
		  console.log(s);
		};
	
	- 这种形式又被称为匿名函数,它被分配为变量的值

	- 函数的表达式需要在语句的结尾加上分号，表示语句结束

	- 这种写法将一个**匿名函数**赋值给变量,**这个匿名函数又称函数表达式（Function Expression），因为赋值语句的等号右侧只能放表达式**。
	
		采用函数表达式声明函数时，function命令后面不带有函数名。如果加上函数名，该函数名只在函数体内部有效，在函数体外部无效。

			var print = function x(){
			  console.log(typeof x);
			};
			x   // ReferenceError: x is not defined
			print()  // function

		- 这种写法的用处有两个，一是可以在函数体内部调用自身，二是方便除错（除错工具显示函数调用栈时，将显示函数名，而不再显示这里是一个匿名函数）

	- **函数表达式和函数声明有一些区别**:

		**函数声明会进行声明提升(declaration hoisting),而函数表达式不会**

3. **Function构造函数**

		var add = new Function(
		  'x',
		  'y',
		  'return x + y'
		);
		
		// 等同于
		function add(x, y) {
		  return x + y;
		}
	
	- 上面代码中，Function构造函数接受三个参数，除了最后一个参数是add函数的“函数体”，其他参数都是add函数的参数。

	- 可以传递任意数量的参数给Function构造函数，只有最后一个参数会被当做函数体，如果只有一个参数，该参数就是函数体

	- Function构造函数可以不使用new命令，返回结果完全一样。

	- **这种方式几乎没人使用**

## 10.2 函数的重复声明

	function f() {
	  console.log(1);
	}
	f() // 2
	
	function f() {
	  console.log(2);
	}
	f() // 2

**如果同一个函数被多次声明，后面的声明就会覆盖前面的声明。**

## 10.3 圆括号运算符，return 语句和递归

### 10.3.1 圆括号运算符
调用函数时，要使用圆括号运算符。圆括号之中，可以加入函数的参数。

### 10.3.2 return语句

函数体内部的return语句，表示返回。

- JavaScript 引擎遇到return语句，就直接返回return后面的那个表达式的值，后面即使还有语句，也不会得到执行。

	也就是说，return语句所带的那个表达式，就是函数的返回值。

	**return语句不是必需的，如果没有的话，该函数就不返回任何值，或者说返回undefined**。

### 10.3.3 递归

函数可以调用自身，这就是递归（recursion）。

## 10.4 第一等公民

	function add(x, y) {
	  return x + y;
	}
	
	// 将函数赋值给一个变量
	var operator = add;
	
	// 将函数作为参数和返回值
	function a(op){
	  return op;
	}
	a(add)(1, 1)    // 2

**JavaScript 语言将函数看作一种值，与其它值（数值、字符串、布尔值等等）地位相同。**

- 凡是可以使用值的地方，就能使用函数。

	比如，可以把函数赋值给变量和对象的属性，也可以当作参数传入其他函数，或者作为函数的结果返回。函数只是一个可以执行的值，此外并无特殊之处。

- 由于函数与其他数据类型地位平等，所以在 JavaScript 语言中又称函数为第一等公民。

## 10.5 函数名的提升

**JavaScript 引擎将函数名视同变量名**，**所以采用function命令声明函数时，整个函数会像变量声明一样，被提升到代码头部**。所以，下面的代码不会报错。
	
	f();
	
	function f() {}

- 由于“变量提升”，函数f被提升到了代码头部，也就是在调用之前已经声明了

- **仅针对 function命令声明函数 这种形式,如果采用函数表达式定义函数就会出错**


函数表达式定义函数:

	f();
	var f = function (){};
	// TypeError: undefined is not a function

	----------等同如下形式-----------
	var f;
	f();
	f = function () {};

- 因此，如果同时采用function命令和赋值语句声明同一个函数，最后总是采用赋值语句的定义。

		var f = function () {
		  console.log('1');
		}
		
		function f() {
		  console.log('2');
		}
		
		f() // 1


## 10.6 函数的属性和方法
### 10.6.1 属性

**函数的name属性返回函数的名字**。

- 如果是通过变量赋值定义的函数,那么name属性在变量的值是一个匿名函数时返回变量名。如果变量的值是一个具名函数，那么name属性返回function关键字之后的那个函数名

- 实际应用中,在函数内部可以通过name属性 获取到作为参数传入的函数的名称	

**函数的length属性返回函数预期传入的参数个数，即函数定义之中的参数个数。**

- length属性提供了一种机制，判断定义时和调用时参数的差异，以便实现面向对象编程的“方法重载”（overload）。

### 10.6.2 方法

**函数的toString方法返回一个字符串，内容是函数的源码。**

## 10.7 函数作用域

作用域（scope）指的是变量存在的范围。

在 ES5 的规范中，JavaScript 只有两种作用域(ES6 又新增了块级作用域)：

1. 一种是全局作用域，变量在整个程序中一直存在，所有地方都可以读取；

	- 对于顶层函数来说，函数外部声明的变量就是全局变量（global variable），它可以在函数内部读取。

2. 另一种是函数作用域，变量只在函数内部存在。

	- 在函数内部定义的变量，外部无法读取，称为“局部变量”（local variable）。
	
		- 函数内部定义的变量，会在该作用域内覆盖同名全局变量。
	
		- 对于var命令来说，局部变量只能在函数内部声明，在其他区块中声明，一律都是全局变量。


### 10.7.1 函数内部的变量提升

与全局作用域一样，函数作用域内部也会产生“变量提升”现象。**var命令声明的变量，不管在什么位置，变量声明都会被提升到函数体的头部**。

	function foo(x) {
	  if (x > 100) {
	    var tmp = x - 100;
	  }
	}
	
	// 等同于
	function foo(x) {
	  var tmp;
	  if (x > 100) {
	    tmp = x - 100;
	  };
	}


### 10.7.2 函数本身的作用域

函数本身也是一个值，也有自己的作用域。它的作用域与变量一样，就是其声明时所在的作用域，与其运行时所在的作用域无关。


	var a = 1;
	var x = function () {
	  console.log(a);
	};
	
	function f() {
	  var a = 2;
	  x();
	}
	x() // 1
	f() // 1

- 上面代码中，函数`x()`是在函数`f()`的外部声明的，所以它的作用域绑定外层，内部变量a不会到函数`f()`体内取值，所以输出1，而不是2。

- 总之，**函数执行时所在的作用域，是定义时的作用域，而不是调用时所在的作用域**。

很容易犯错的一点是，如果函数A调用函数B，却没考虑到函数B不会引用函数A的内部变量。

	var x = function () {
	  console.log(a);
	};
	
	function y(f) {
	  var a = 2;
	  f();
	}
	
	y(x)
	// ReferenceError: a is not defined

- 上面代码将函数x作为参数，传入函数y。但是，函数x是在函数y体外声明的，作用域绑定外层，因此找不到函数y的内部变量a，导致报错。


## 10.8 参数

函数运行的时候，有时需要提供外部数据，不同的外部数据会得到不同的结果，这种外部数据就叫参数。

### 10.8.1 参数的省略

函数参数不是必需的，JavaScript 允许省略参数。

	function f(a, b) {
	  return a;
	}
	
	f(1, 2, 3) // 1
	f(1) // 1
	f() // undefined
	
	f.length // 2

- 上面代码的函数f定义了两个参数，但是**运行时无论提供多少个参数（或者不提供参数），JavaScript 都不会报错**。

	省略的参数的值就变为undefined。

- 需要注意的是，函数的length属性与实际传入的参数个数无关，只反映函数预期传入的参数个数。

- 但是，没有办法只省略靠前的参数，而保留靠后的参数。如果一定要省略靠前的参数，只有显式传入undefined

### 10.8.2 传递方式

1. 函数参数如果是原始类型的值（数值、字符串、布尔值），传递方式是**传值传递（passes by value）**。这意味着，在函数体内修改参数值，不会影响到函数外部。

2. 函数参数是复合类型的值（数组、对象、其他函数），传递方式是**传址传递（pass by reference）**。也就是说，传入函数的原始值的地址，因此在函数内部修改参数，将会影响到原始值。

	- 如果函数内部修改的，不是参数对象的某个属性，而是替换掉整个参数，这时不会影响到原始值

			var obj = [1, 2, 3];
			
			function f(o) {
			  o = [2, 3, 4];
			}
			f(obj);
			
			obj // [1, 2, 3]

		- 这是因为，形式参数（o）的值实际是参数obj的地址，重新对o赋值导致o指向另一个地址，保存在原地址上的值当然不受影响


### 10.8.3 同名参数

	function f(a, a) {
	  console.log(a);
	}
	
	f(1) // undefined

如果有同名的参数，**则取最后出现的那个值**。

- **即使后面的a没有值或被省略**

- 这时，如果要获得第一个a的值，可以使用`arguments`对象。

		function f(a, a) {
		  console.log(arguments[0]);
		}
		
		f(1) // 1

### 10.8.4 arguments对象

由于 JavaScript 允许函数有不定数目的参数，所以需要一种机制，可以在函数体内部读取所有参数。这就是arguments对象的由来。

- arguments对象包含了函数运行时的所有参数，`arguments[0]`就是第一个参数，`arguments[1]`就是第二个参数，以此类推。这个对象只有在函数体内部，才可以使用。

		var f = function (one) {
		  console.log(arguments[0]);
		  console.log(arguments[1]);
		  console.log(arguments[2]);
		}
		
		f(1, 2, 3)
		// 1
		// 2
		// 3

- 正常模式下，**`arguments`对象可以在运行时修改**。

		var f = function(a, b) {
		  arguments[0] = 3;
		  arguments[1] = 2;
		  return a + b;
		}
		
		f(1, 1) // 5

- 严格模式下，arguments对象与函数参数不具有联动关系。也就是说，修改arguments对象不会影响到实际的函数参数。

		var f = function(a, b) {
		  'use strict'; // 开启严格模式
		  arguments[0] = 3;
		  arguments[1] = 2;
		  return a + b;
		}
		
		f(1, 1) // 2

需要注意的是，**虽然`arguments`很像数组，但它是一个对象**。数组专有的方法（比如slice和forEach），不能在arguments对象上直接使用。

- 如果要让arguments对象使用数组方法，真正的解决方法是将arguments转为真正的数组。下面是两种常用的转换方法：slice方法和逐一填入新数组。

		var args = Array.prototype.slice.call(arguments);
		
		// 或者
		var args = [];
		for (var i = 0; i < arguments.length; i++) {
		  args.push(arguments[i]);
		}

arguments对象带有一个callee属性，返回它所对应的原函数。

	var f = function () {
	  console.log(arguments.callee === f);
	}
	
	f() // true

- 可以通过`arguments.callee`，达到调用函数自身的目的。这个属性在严格模式里面是禁用的，因此不建议使用。


## 10.9 函数其他的知识点
### 10.9.1 闭包

理解闭包，首先必须理解变量作用域。前面提到，JavaScript 有两种作用域：全局作用域和函数作用域。函数内部可以直接读取全局变量。

如果出于种种原因，需要得到函数内的局部变量。正常情况下，这是办不到的，只有通过变通方法才能实现。那就是在函数的内部，再定义一个函数。

	function f1() {
	  var n = 999;
	  function f2() {
	　　console.log(n); // 999
	  }
	}

- 根据JavaScript 语言特有的"链式作用域"结构（chain scope），**子对象会一级一级地向上寻找所有父对象的变量。因此父对象的所有变量，对子对象都是可见的，反之则不成立**

	**那么这只要把f2作为返回值,就可以在f1外部读取f1内部变量**

- 闭包就是函数f2，**即能够读取其他函数内部变量的函数**。

	由于在 JavaScript 语言中，只有函数内部的子函数才能读取内部变量，因此可以把闭包简单理解成“**定义在一个函数内部的函数**”。

	闭包最大的特点，就是它可以“记住”诞生的环境，比如f2记住了它诞生的环境f1，所以从f2可以得到f1的内部变量。在本质上，闭包就是将函数内部和函数外部连接起来的一座桥梁。


闭包的最大用处有两个:

1. **一个是可以读取函数内部的变量**
2. **一个就是让这些变量始终保持在内存中，即闭包可以使得它诞生环境一直存在**。


**作用一的示例(封装对象的私有属性和私有方法)**

	function Person(name) {
	  var _age;
	  function setAge(n) {
	    _age = n;
	  }
	  function getAge() {
	    return _age;
	  }
	  // 返回一个对象
	  return {
	    name: name, // 属性
	    getAge: getAge, // 方法
	    setAge: setAge // 方法
	  };
	}
	
	var p1 = Person('张三');
	p1.setAge(25);
	p1.getAge() // 25

- 外层函数每次运行，都会生成一个新的闭包，而这个闭包又会保留外层函数的内部变量，所以内存消耗很大。因此不能滥用闭包，否则会造成网页的性能问题


**作用二的示例(闭包使得内部变量记住上一次调用时的运算结果):**

	function createIncrementor(start) {
	  return function () {
	    return start++;
	  };
	}
	
	var inc = createIncrementor(5);
	
	inc() // 5
	inc() // 6
	inc() // 7

- 上面代码中，start是函数createIncrementor的内部变量。通过闭包，start的状态被保留了，每一次调用都是在上一次调用的基础上进行计算。从中可以看到，闭包inc使得函数createIncrementor的内部环境，一直存在。所以，**闭包可以看作是函数内部作用域的一个接口**。

- 这种现象的原因就在于inc始终在内存中，而inc的存在依赖于createIncrementor，因此也始终在内存中，不会在调用结束后，被垃圾回收机制回收


### 10.9.2 立即调用的函数表达式(`IIFE`)

有时，我们需要在定义函数之后，立即调用该函数。这时，你不能在函数的定义之后加上圆括号，这会产生语法错误。
	
	function(){ /* code */ }();
	// SyntaxError: Unexpected token (

- 产生这个错误的原因是，function这个关键字即可以当作语句，也可以当作表达式。

	为了避免解析上的歧义，**JavaScript 引擎规定，如果function关键字出现在行首，一律解释成语句**。因此，JavaScript 引擎看到行首是function关键字之后，认为这一段都是函数的定义，不应该以圆括号结尾，所以就报错了

- **解决方法就是不要让function出现在行首，让引擎将其理解成一个表达式。最简单的处理，就是将其放在一个圆括号里面**。

		(function(){ /* code */ }());
		// 或者
		(function(){ /* code */ })();

	- **这就叫做“立即调用的函数表达式”（Immediately-Invoked Function Expression），简称 IIFE**

	- 上面两种写法最后的分号都是必须的。如果省略分号，遇到连着两个 IIFE，可能就会报错。

	- 实际上,只要让解释器以表达式来处理函数定义的方法, 就能够产生同样的效果

			var i = function(){ return 10; }();
			true && function(){ /* code */ }();
			0, function(){ /* code */ }();

	- 通常情况下，只对匿名函数使用这种“立即执行的函数表达式”。
	
		它的目的有两个：
	
		1. 一是不必为函数命名，避免了污染全局变量；
		2. 二是 IIFE 内部形成了一个单独的作用域，可以封装一些外部无法读取的私有变量。

				// 写法一
				var tmp = newData;
				processData(tmp);
				storeData(tmp);
				
				// 写法二
				(function () {
				  var tmp = newData;
				  processData(tmp);
				  storeData(tmp);
				}());

## 10.10 eval命令

eval命令接受一个字符串作为参数，并将这个字符串当作语句执行。

	eval('var a = 1;');
	a // 1

- 上面代码将字符串当作语句运行，生成了变量a。

- 如果参数字符串无法当作语句运行，那么就会报错。

- 放在eval中的字符串，应该有独自存在的意义，不能用来与eval以外的命令配合使用。举例来说，下面的代码将会报错
	
		eval('return;'); // Uncaught SyntaxError: Illegal return statement

- 如果eval的参数不是字符串，那么会原样返回。

- eval没有自己的作用域，都在当前作用域内执行，因此可能会修改当前作用域的变量的值，造成安全问题。

		var a = 1;
		eval('a = 2');
		
		a // 2

	- 如果使用严格模式，eval**内部声明的变量**，不会影响到外部作用域。
	
		- **不过，即使在严格模式下，eval依然可以读写当前作用域的变量**


eval的本质是在当前作用域之中，注入代码。由于安全风险和不利于 JavaScript 引擎优化执行速度，所以一般不推荐使用。通常情况下，eval最常见的场合是解析 JSON 数据的字符串，不过正确的做法应该是使用原生的JSON.parse方法

### 10.10.1 eval的别名调用

前面说过eval不利于引擎优化执行速度。更麻烦的是，还有下面这种情况，引擎在静态代码分析的阶段，根本无法分辨执行的是eval。

	var m = eval;
	m('var x = 1');
	x // 1
	
- 为了保证eval的别名不影响代码优化，JavaScript 的标准规定，凡是使用别名执行eval，eval内部一律是全局作用域。

eval的别名调用的形式五花八门，只要不是直接调用，都属于别名调用，因为引擎只能分辨eval()这一种形式是直接调用。

	eval.call(null, '...')
	window.eval('...')
	(1, eval)('...')
	(eval, eval)('...')

- 上面这些形式都是eval的别名调用，作用域都是全局作用域

# 11. 数组

	var sequence = [1, 1, 2, 3, 5, 8, 13];
	var random = ['tree', 795, [0, 1, 2]];

数组（array）是按次序排列的一组值。

- 每个值的位置都有编号（从0开始），**整个数组用方括号表示**。

- 除了在定义时赋值，数组也可以先定义后赋值。

- 任何类型的数据，都可以放入数组。

- 如果数组的元素还是数组，就形成了多维数组。

## 11.1 数组的本质

**本质上，数组属于一种特殊的对象**。

- typeof 运算符会返回数组的类型是object。

- **数组的特殊性体现在，它的键名是按次序排列的一组整数（0，1，2...) ,会被转换成字符串**

		var arr = ['a', 'b', 'c'];
		
		Object.keys(arr)
		// ["0", "1", "2"]

	- JavaScript 语言规定，对象的键名一律为字符串，所以，数组的键名其实也是字符串。之所以可以用数值读取，是因为非字符串的键名会被转为字符串

	- 这点在赋值时也成立。一个值总是先转成字符串，再被当做index进行赋值。

	- 对于数值的键名，不能使用点结构.因为单独的数值不能作为标识符（identifier）


## 11.2 length属性

数组的length属性，返回数组的成员数量。

	['a', 'b', 'c'].length // 3

- JavaScript 使用一个32位整数，保存数组的元素个数。这意味着，数组成员最多只有 4294967295 个（232 - 1）个，也就是说length属性的最大值就是 4294967295。

- 只要是数组，就一定有length属性。**该属性是一个动态的值，等于键名中的最大整数加上1**

- **清空数组的一个有效方法，就是将length属性设为0**。

- 如果人为设置length大于当前元素个数，则数组的成员数量会增加到这个值，新增的位置都是空位。

		var a = ['a'];
		
		a.length = 3;
		a[1] // undefined

- 由于数组本质上是一种对象，**所以可以为数组添加属性**，但是这不影响length属性的值。

		var a = [];
		
		a['p'] = 'abc';
		a.length // 0
		
		a[2.1] = 'abc';
		a.length // 0

- 如果数组的键名是添加超出范围的数值，该键名会自动转为字符串。

		var arr = [];
		arr[-1] = 'a';
		arr[Math.pow(2, 32)] = 'b';
		
		arr.length // 0
		arr[-1] // "a"
		arr[4294967296] // "b"


## 11.3 in运算符

**检查某个键名是否存在的运算符in，适用于对象，也适用于数组。**

	var arr = [ 'a', 'b', 'c' ];
	2 in arr  // true
	'2' in arr // true
	4 in arr // false

- **由于键名都是字符串**，所以数值2会自动转成字符串。

- 如果数组的某个位置是空位，in运算符返回false
		
		var arr = [];
		arr[100] = 'a';
		
		100 in arr // true
		1 in arr // false

## 11.4 for...in 循环和数组的遍历

**`for...in`循环不仅可以遍历对象，也可以遍历数组，毕竟数组只是一种特殊对象。**

	var a = [1, 2, 3];
	
	for (var i in a) {
	  console.log(a[i]);
	}

- for...in不仅会遍历数组所有的数字键，还会遍历非数字键。(**不推荐使用for...in遍历数组**)

		var a = [1, 2, 3];
		a.foo = true;
		
		for (var key in a) {
		  console.log(key);
		}
		// 0
		// 1
		// 2
		// foo


数组的forEach方法，也可以用来遍历数组

	var colors = ['red', 'green', 'blue'];
	colors.forEach(function (color) {
	  console.log(color);
	});
	// red
	// green
	// blue


## 11.5 数组的空位

当数组的某个位置是空元素，即两个逗号之间没有任何值，我们称该数组存在空位（hole）。

	var a = [1, , 1];
	a.length // 3

- 数组的空位不影响length属性

- 如果最后一个元素后面有逗号，并不会产生空位。也就是说，有没有这个逗号，结果都是一样的

- 数组的空位是可以读取的，返回undefined

- 使用delete命令删除一个数组成员，会形成空位，并且不会影响length属性。

		var a = [1, 2, 3];
		delete a[1];
		
		a[1] // undefined
		a.length // 3

### 11.5.1 空位和undefined的区别

**数组的某个位置是空位，与某个位置是undefined，是不一样的。如果是空位，使用数组的forEach方法、for...in结构、以及Object.keys方法进行遍历，空位都会被跳过。**

- 如果某个位置是undefined，遍历的时候就不会被跳过。

这就是说，空位就是数组没有这个元素，所以不会被遍历到，而undefined则表示数组有这个元素，值是undefined，所以遍历不会跳过。

## 11.6 类似数组的对象

如果一个对象的所有键名都是正整数或零，并且有length属性，那么这个对象就很像数组，语法上称为“类似数组的对象”（array-like object）。

	var obj = {
	  0: 'a',
	  1: 'b',
	  2: 'c',
	  length: 3
	};
	
	obj[0] // 'a'
	obj[1] // 'b'
	obj.length // 3
	obj.push('d') // TypeError: obj.push is not a function

- **“类似数组的对象”并不是数组，因为它们不具备数组特有的方法**。对象obj没有数组的push方法，使用该方法就会报错

- 这种length属性不是动态值，不会随着成员的变化而变化

- 典型的“类似数组的对象”是函数的`arguments`对象，以及大多数 DOM 元素集，还有字符串

		// arguments对象
		function args() { return arguments }
		var arrayLike = args('a', 'b');
		
		arrayLike[0] // 'a'
		arrayLike.length // 2
		arrayLike instanceof Array // false
		
		// DOM元素集
		var elts = document.getElementsByTagName('h3');
		elts.length // 3
		elts instanceof Array // false
		
		// 字符串
		'abc'[1] // 'b'
		'abc'.length // 3
		'abc' instanceof Array // false

- 数组的slice方法可以将“类似数组的对象”变成真正的数组。

		var arr = Array.prototype.slice.call(arrayLike);

	- 除了转为真正的数组，“类似数组的对象”还有一个办法可以使用数组的方法，就是通过`call()`把数组的方法放到对象上面。

			function print(value, index) {
			  console.log(index + ' : ' + value);
			}
			
			Array.prototype.forEach.call(arrayLike, print);

		- 上面代码中，arrayLike代表一个类似数组的对象，本来是不可以使用数组的forEach()方法的，但是通过call()，可以把forEach()嫁接到arrayLike上面调用。

		- 注意，**这种方法比直接使用数组原生的forEach要慢，所以最好还是先将“类似数组的对象”转为真正的数组，然后再直接调用数组的forEach方法**。

## 11.7 字符串和数组之间的转换

	var myData = 'Manchester,London,Liverpool,Birmingham,Leeds,Carlisle';

- 通过`split(',')`方法可以将字符串转换成数组

		var array = myData.split(',');
		// ["Manchester", "London", "Liverpool", "Birmingham", "Leeds", "Carlisle"]

- 通过`join(',')`方法可以将数组转换成字符串

		array.join(',') == myData; // true

- 通过`toString()`方法可以将数组转换为字符串

		array.toString()

## 11.8 添加和删除数组项

	var myArray = ['Manchester', 'London', 'Liverpool', 'Birmingham', 'Leeds', 'Carlisle'];

- 使用`push()`方法可以添加一个或多个元素到数组末尾

		myArray.push('Cardiff');
		myArray.push('Bradford', 'Brighton');

	- 方法调用完成之后会返回数组的新长度

- 使用`pop()`方法可以删除数组的最后一个元素

		myArray.pop();

	- 方法调用完成之后会返回被删除的元素


- `unshift()`和`shift()`从功能上与`push()`和`pop()`一致,只是前者是作用于数组的头部,后者是作用于数组的尾部