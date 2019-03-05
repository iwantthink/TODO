# JavaScript简介

[W3School -Js教程](http://www.w3school.com.cn/js/js_intro.asp)

[JavaScript教程-阮一峰](https://wangdoc.com/javascript/index.html)

# 1. 简介 

**JavaScript 是脚本语言,官方名称是`ECMA-262 `**

- JS由 Brendan Eich 发明。它于 1995 年出现在 Netscape 中（该浏览器已停止更新），并于 1997 年被 ECMA（一个标准协会）采纳。

JavaScript 是一种轻量级的编程语言。

JavaScript 是可插入 HTML 页面的编程代码。

JavaScript 插入 HTML 页面后，可由所有的现代浏览器执行。

JavaScript 与 Java 是两种完全不同的语言，无论在概念还是设计上。

- Java（由 Sun 发明）是更复杂的编程语言。

**浏览器会在读取代码时，逐行地执行脚本代码。而对于传统编程来说，会在执行前对所有代码进行编译。**

# 2. JavaScript 实现

HTML 中的脚本必须位于` <script>` 与` </script>` 标签之间。

脚本可被放置在 HTML 页面的 `<body>` 和 `<head>` 部分中。

## 2.1 `<script>`标签

如需在 HTML 页面中插入 JavaScript，请使用 `<script>` 标签。

- `<script>` 和 `</script>` 会告诉 JavaScript 在何处开始和结束。

- `<script>`的元素内容包含了 JavaScript

		<script>
			alert("My First JavaScript");
		</script>

	- 浏览器会解释并执行位于 `<script>` 和 `</script>` 之间的 JavaScript代码

	- 那些老旧的实例可能会在 `<script>` 标签中使用 `type="text/javascript"`。现在已经不必这样做了。JavaScript 是所有现代浏览器以及 HTML5 中的默认脚本语言。



### 2.1.1 `<head>`或`<body>`中的JS

可以在 HTML 文档中放入不限数量的脚本。

脚本可位于 HTML 的` <body>` 或 `<head>` 部分中，或者同时存在于两个部分中。

通常的做法是把函数放入 `<head>` 部分中，或者放在页面底部。这样就可以把它们安置到同一处位置，不会干扰页面的内容。


### 2.1.2 `<body>`中的JavaScript

	<!DOCTYPE html>
	<html>
		<body>
			.
			.
			<script>
			document.write("<h1>This is a heading</h1>");
			document.write("<p>This is a paragraph</p>");
			</script>
			.
			.
		</body>
	</html>

- 本例中,JS会在页面加载时向HTML的`<body>`写入文本


## 2.2 JavaScript 函数和事件

上面例子中的 JavaScript 语句，会在页面加载时执行。

但是通常情况下，我们需要在某个事件发生时执行代码，比如当用户点击按钮时。

如果我们把 JavaScript 代码放入函数中，就可以在事件发生时调用该函数。


### 2.2.1 `<head>`中的JS函数

	<!DOCTYPE html>
	<html>
		<head>
			<script>
				function myFunction()
				{
				document.getElementById("demo").innerHTML="My First JavaScript Function";
				}
			</script>
		</head>
		
		<body>
			<h1>My Web Page</h1>
			<p id="demo">A Paragraph</p>
			<button type="button" onclick="myFunction()">Try it</button>
		</body>
	</html>

### 2.2.2 `<body>`中的JS函数

	<!DOCTYPE html>
	<html>
		<body>
			<h1>My Web Page</h1>
			<p id="demo">A Paragraph</p>
			<button type="button" onclick="myFunction()">Try it</button>
		
			<script>
				function myFunction()
				{
					document.getElementById("demo").innerHTML="My First JavaScript Function";
				}
			</script>
		</body>
	</html>

- 把 JavaScript 放到了页面代码的底部，这样就可以确保在 `<p>` 元素创建之后再执行脚本

## 2.3 外部的JS

可以把脚本保存到外部文件中,外部文件通常包含被多个网页使用的代码。

- 外部 JavaScript 文件的文件扩展名是 `.js`。

- 如需使用外部文件，请在 `<script>` 标签的 "src" 属性中设置该` .js `文件：

实例

	<!DOCTYPE html>
	<html>
		<body>
			<script src="myScript.js"></script>
		</body>
	</html>


- 在 `<head>` 或 `<body>` 中引用脚本文件都是可以的。实际运行效果与您在 `<script>` 标签中编写脚本完全一致。

- 外部脚本中不能包含`<script>`标签



# 3. JS输出

JavaScript 通常用于操作 HTML 元素。

## 3.1 操作HTML元素

如需从 JavaScript 访问某个 HTML 元素，您可以使用 `document.getElementById(id)` 方法。

- 请使用 "id" 属性来标识 HTML 元素


	<!DOCTYPE html>
	<html>
		<body>
		     <h1>我的第一张网页</h1>
		     <p id="demo">我的第一个段落</p>
		     <script>
		          document.getElementById("demo").innerHTML = "我的第一段 JavaScript";
		     </script>
		</body>
	</html>

- JS由Web浏览器执行

## 3.2 写到文档输出

	<!DOCTYPE html>
	<html>
		<body>
			<h1>我的第一张网页</h1>
			
			<script>
				document.write("<p>我的第一段 JavaScript</p>");
			</script>
		</body>
	</html>

- 将`<p>`元素写到HTML文档输出中


- 请使用 `document.write()` 仅仅向文档输出内容。

	**如果在文档已完成加载后执行 document.write，整个 HTML 页面将被覆盖**


# 4. JS 语句

JavaScript 程序的执行单位为行（line），也就是一行一行地执行。一般情况下，每一行就是一个语句。

语句（statement）是为了完成某种任务而进行的操作，比如下面就是一行赋值语句。

	var a = 1 + 3;

- `1 + 3`叫做表达式（expression），指一个为了得到返回值的计算式。

	**语句和表达式的区别在于:**
	1. 前者主要为了进行某种操作，一般情况下不需要返回值；
	2. 后者则是为了得到返回值，一定会返回一个值。凡是 JavaScript 语言中预期为值的地方，都可以使用表达式。比如，赋值语句的等号右边，预期是一个值，因此可以放置各种表达式。

## 4.1 分号`;`

语句以分号结尾，一个分号就表示一个语句结束。

- 使用分号的另一用处是在一行中编写多条语句。

- 分号前面可以没有任何内容，JavaScript 引擎将其视为空语句。

- 在 JavaScript 中，**用分号来结束语句是可选的**。

- 表达式不需要分号结尾,避免产生无意义的语句

## 4.2 JS代码

JavaScript 代码（或者只有 JavaScript）是 JavaScript 语句的序列。

- 浏览器会按照编写顺序来执行每条语句。


实例

	// 单条JS代码
	document.getElementById("demo").innerHTML="Hello World";
	// 单条JS代码
	document.getElementById("myDIV").innerHTML="How are you?";

## 4.3 JS代码块

**JavaScript 语句通过代码块的形式进行组合。**

- 块由左花括号开始，由右花括号结束。

- 块的作用是使语句序列一起执行。

- JavaScript 函数是将语句组合在块中的典型例子。

下面的例子将运行可操作两个 HTML 元素的函数：

实例
	
	function myFunction()
	{
		document.getElementById("demo").innerHTML="Hello World";
		document.getElementById("myDIV").innerHTML="How are you?";
	}

## 4.4 JS对大小写敏感

**JavaScript 对大小写是敏感的。**

- 当编写 JavaScript 语句时，请留意是否关闭大小写切换键。

- 函数 `getElementById` 与 `getElementbyID` 是不同的。

- 同样，变量 myVariable 与 MyVariable 也是不同的。


## 4.5 空格

JavaScript 会忽略多余的空格。您可以向脚本添加空格，来提高其可读性。下面的两行代码是等效的：

	var name="Hello";
	var name = "Hello";


## 4.6 对代码行进行折行

可以在文本字符串中使用**反斜杠(`\`)**对代码行进行换行。下面的例子会正确地显示：

	document.write("Hello \
	World!");

不过，您不能像这样折行：

	document.write \
	("Hello World!");

# 5. JS 注释

JavaScript 注释可用于提高代码的可读性。

- JavaScript 不会执行注释。


## 5.1 单行注释

单行注释以 `//` 开头。


	// 输出标题：
	document.getElementById("myH1").innerHTML="Welcome to my Homepage";
	// 输出段落：
	document.getElementById("myP").innerHTML="This is my first paragraph.";


## 5.2 多行注释

多行注释以` /*` 开始，以` */` 结尾。

下面的例子使用多行注释来解释代码：

	/*
	下面的这些代码会输出
	一个标题和一个段落
	并将代表主页的开始
	*/
	document.getElementById("myH1").innerHTML="Welcome to my Homepage";
	document.getElementById("myP").innerHTML="This is my first paragraph.";

## 5.3 使用注释来阻止执行

在下面的例子中，注释用于阻止其中一条代码行的执行（可用于调试）：

	//document.getElementById("myH1").innerHTML="Welcome to my Homepage";
	document.getElementById("myP").innerHTML="This is my first paragraph.";


在下面的例子中，注释用于阻止代码块的执行（可用于调试）：

	/*
	document.getElementById("myH1").innerHTML="Welcome to my Homepage";
	document.getElementById("myP").innerHTML="This is my first paragraph.";
	*/


## 5.4 在行末使用注释

	var x=5;    // 声明 x 并把 5 赋值给它
	var y=x+2;  // 声明 y 并把 x+2 赋值给它


# 6. JS变量

变量是对“值”的具名引用。

- 变量就是为“值”起名，然后引用这个名字，就等同于引用这个值。

- 变量的名字就是变量名

## 6.1 变量命名

变量可以使用短名称（比如 x 和 y），也可以使用描述性更好的名称（比如 age, sum, totalvolume）。

- 变量必须以字母开头

- 变量也能以 $ 和 _ 符号开头（不过我们不推荐这么做）

- 变量名称对大小写敏感（y 和 Y 是不同的变量）

- **JavaScript 语句和 JavaScript 变量都对大小写敏感**。


## 6.2 数据类型

JavaScript 变量还能保存其他数据类型，比如文本值 (name="Bill Gates")。

- 在 JavaScript 中，类似 "Bill Gates" 这样一条文本被称为字符串。

JavaScript 变量有很多种类型，不仅仅是**数字和字符串**。

- 当向变量分配文本值时，应该用双引号或单引号包围这个值。

- **当向变量赋的值是数值时，不要使用引号。如果您用引号包围数值，该值会被作为文本来处理。**


## 6.3 创建JS变量

在 JavaScript 中创建变量通常称为“声明”变量。

- **使用 `var` 关键词来声明变量**

	var carname;
	
- 变量声明之后，该变量是空的（它没有值）。

如需向变量赋值，请使用等号：

	carname="Volvo";

也可以在声明变量时对其赋值：

	var carname="Volvo";

例子

在下面的例子中，我们创建了名为 carname 的变量，并向其赋值 "Volvo"，然后把它放入 id="demo" 的 HTML 段落中：

	<p id="demo"></p>
	var carname="Volvo";
	document.getElementById("demo").innerHTML=carname;



## 6.4 一条语句,多个变量

可以在一条语句中声明很多变量。

- 该语句以 var 开头，并使用逗号分隔变量即可：

		var name="Gates", age=56, job="CEO";

声明也可以横跨多行

	var name="Gates",
	age=56,
	job="CEO";


## 6.5 Value = undefined

在计算机程序中，经常会声明无值的变量。未使用值来声明的变量，其值实际上是 **`undefined`**。

在执行过以下语句后，变量 carname 的值将是 undefined：

	var carname;

## 6.6 重新声明 JavaScript 变量

如果重新声明 JavaScript 变量，该变量的值不会丢失：

在以下两条语句执行后，变量 carname 的值依然是 "Volvo"：

	var carname="Volvo";
	var carname;

# 7. JS数据类型

字符串、数字、布尔、数组、对象、Null、Undefined


## 7.1 JS拥有动态类型

JavaScript 拥有动态类型。这意味着相同的变量可用作不同的类型：

	var x                // x 为 undefined
	var x = 6;           // x 为数字
	var x = "Bill";      // x 为字符串

## 7.2 JS 字符串

字符串是存储字符（比如 "Bill Gates"）的变量。

字符串可以是引号中的任意文本。您可以使用单引号或双引号：

实例

	var carname="Bill Gates";
	var carname='Bill Gates';


可以在字符串中使用引号，只要不匹配包围字符串的引号即可：

实例

	var answer="Nice to meet you!";
	var answer="He is called 'Bill'";
	var answer='He is called "Bill"';


## 7.3 JS 数字

JavaScript 只有一种数字类型。数字可以带小数点，也可以不带：

实例

	var x1=34.00;      //使用小数点来写
	var x2=34;         //不使用小数点来写

极大或极小的数字可以通过科学（指数）计数法来书写：

实例

	var y=123e5;      // 12300000
	var z=123e-5;     // 0.00123


## 7.4 JS 布尔


布尔（逻辑）只能有两个值：true 或 false。

	var x=true
	var y=false

- 布尔常用在条件测试中。

## 7.5 JS 数组

下面的代码创建名为 cars 的数组：

	var cars=new Array();
	cars[0]="Audi";
	cars[1]="BMW";
	cars[2]="Volvo";

或者 (condensed array):

	var cars=new Array("Audi","BMW","Volvo");

或者 (literal array):

	var cars=["Audi","BMW","Volvo"];


**数组下标是基于零的，所以第一个项目是 [0]，第二个是 [1]，以此类推**。


## 7.6 JS对象

对象由花括号分隔。在括号内部，对象的属性以名称和值对的形式 (name : value) 来定义。属性由逗号分隔：

	var person={firstname:"Bill", lastname:"Gates", id:5566};

- 上面例子中的对象 (person) 有三个属性：firstname、lastname 以及 id。

- 空格和折行无关紧要。声明可横跨多行：

		var person={
		firstname : "Bill",
		lastname  : "Gates",
		id        :  5566
		};

**对象属性有两种寻址方式**：

	name=person.lastname;
	name=person["lastname"];


## 7.7 Undefined 和 Null

Undefined 这个值表示变量不含有值。

可以通过将变量的值设置为 null 来清空变量。

实例

	cars=null;
	person=null;

## 7.8 声明变量类型

当您声明新变量时，可以使用关键词 "new" 来声明其类型：

	var carname=new String;
	var x=      new Number;
	var y=      new Boolean;
	var cars=   new Array;
	var person= new Object;

- JavaScript 变量均为对象。当您声明一个变量时，就创建了一个新的对象。


# 8. JS 对象

JavaScript 中的所有事物都是对象：字符串、数字、数组、日期，等等。

在 JavaScript 中，对象是拥有属性和方法的数据。

## 8.1 属性和方法

属性是与对象相关的值。

方法是能够在对象上执行的动作。

举例：汽车就是现实生活中的对象。

汽车的属性：

	car.name=Fiat
	
	car.model=500
	
	car.weight=850kg
	
	car.color=white 

汽车的方法：

	car.start()
	
	car.drive()
	
	car.brake()

汽车的属性包括名称、型号、重量、颜色等。

所有汽车都有这些属性，但是每款车的属性都不尽相同。

汽车的方法可以是启动、驾驶、刹车等。

所有汽车都拥有这些方法，但是它们被执行的时间都不尽相同。


## 8.2 JS中的对象

在 JavaScript 中，对象是数据（变量），拥有属性和方法。

当声明一个 JavaScript 变量时：

	var txt = "Hello";

- **实际上已经创建了一个 JavaScript 字符串对象**。

	字符串对象拥有内建的属性 length。对于上面的字符串来说，length 的值是 5。

	字符串对象同时拥有若干个内建的方法。

属性：

	txt.length=5

方法：

	txt.indexOf()
	
	txt.replace()
	
	txt.search()

- 提示：在面向对象的语言中，属性和方法常被称为对象的成员。


## 8.3 创建JS对象

JS中可以创建自己的对象。

本例创建名为 "person" 的对象，并为其添加了四个属性：

实例

	person=new Object();
	person.firstname="Bill";
	person.lastname="Gates";
	person.age=56;
	person.eyecolor="blue";

## 8.4 访问对象的属性

访问对象属性的语法是：

	objectName.propertyName

示例(使用String 对象的 length 属性来查找字符串的长度):

	var message="Hello World!";
	var x=message.length;

	------output-------
	12


## 8.5 访问对象的方法

访问对象方法的语法是：

	objectName.methodName()

示例(使用 String 对象的 `toUpperCase()` 方法来把文本转换为大写):

	var message="Hello world!";
	var x=message.toUpperCase();

	-----output------
	HELLO WORLD!

# 9. JS函数

函数是由事件驱动的或者当它被调用时执行的可重复使用的代码块。

## 9.1 JS 函数语法

**函数就是包裹在花括号中的代码块，通过使用关键词 `function`定义函数**

	function functionname()
	{
		这里是要执行的代码
	}

- 当调用该函数时，会执行函数内的代码。

	可以在某事件发生时直接调用函数（比如当用户点击按钮时），并且可由 JavaScript 在任何位置进行调用。

- 关键词 function 必须是小写的，并且必须以与函数名称相同的大小写来调用函数。


## 9.2 调用带参数的函数

在调用函数时，可以向其传递值，这些值被称为参数。

- 参数可以在函数中使用。

- 可以发送任意多的参数，由逗号 (`,`) 分隔：

		myFunction(argument1,argument2)

当声明函数时，请把参数作为变量来声明：

	function myFunction(var1,var2)
	{
	这里是要执行的代码
	}

- 变量和参数必须以一致的顺序出现。第一个变量就是第一个被传递的参数的给定的值，以此类推。


## 9.3 带有返回值的函数

通过使用 `return` 语句就可以实现将值返回给函数调用处

- 在使用 `return` 语句时，函数会停止执行，并返回指定的值。

		function myFunction()
		{
			var x=5;
			return x;
		}

	- 整个 JavaScript 并不会停止执行，仅仅是函数。JavaScript 将从调用函数的地方继续执行代码，


## 9.4 退出函数执行

仅仅希望退出函数时 ，也可使用` return `语句。返回值是可选的：

## 9.5 局部JS变量

在 JavaScript 函数内部声明的变量（使用 var）是局部变量，所以只能在函数内部访问它。（该变量的作用域是局部的）。

- 可以在不同的函数中使用名称相同的局部变量，因为只有声明过该变量的函数才能识别出该变量。

- 只要函数运行完毕，本地变量就会被删除。

## 9.6 全局JS变量

在函数外声明的变量是全局变量，网页上的所有脚本和函数都能访问它。


## 9.7 JS变量的生存期

JavaScript 变量的生命期从它们被声明的时间开始。

- 局部变量会在函数运行以后被删除。

- 全局变量会在页面关闭后被删除。

## 9.8 向未声明的JS变量分配值

**如果把值赋给尚未声明的变量，该变量将被自动作为全局变量声明,即使其在函数内执行!**

这条语句：

	carname="Volvo";

- 将声明一个全局变量 carname


# 10. 运算符

运算符 = 用于赋值。

运算符 + 用于加值。


## 10.1 JavaScript 算术运算符


运算符	|描述	|例子	|结果
---|---|---|---
+	|加	|x=y+2	|x=7
-	|减	|x=y-2	|x=3
*	|乘	|x=y*2	|x=10
/	|除	|x=y/2	|x=2.5
%	|求余数 (保留整数)	|x=y%2	|x=1
++	|累加	|x=++y	|x=6
--	|递减	|x=--y	|x=4


## 10.2 用于字符串的 + 运算符

+ 运算符用于把文本值或字符串变量加起来（连接起来）。

如需把两个或多个字符串变量连接起来，请使用 + 运算符。

	txt1="What a very";
	txt2="nice day";
	txt3=txt1+txt2;

**如果把数字与字符串相加，结果将成为字符串**。


# 11. JS 比较

## 11.1 比较运算符

比较运算符在逻辑语句中使用，以测定变量或值是否相等。


运算符	|描述	|例子
---|---|---
==	|等于	|x==8 为 false
===	|全等（值和类型）	|x===5 为 true；x==="5" 为 false
!=	|不等于	|x!=8 为 true
>	|大于	|x>8 为 false
<	|小于	|x<8 为 true
>=	|大于或等于	|x>=8 为 false
<=	|小于或等于	|x<=8 为 true


- 可以在条件语句中使用比较运算符对值进行比较，然后根据结果来采取行动

		if (age<18) document.write("Too young");

## 11.2 逻辑运算符

逻辑运算符用于测定变量或值之间的逻辑。


运算符	|描述	|例子
---|---|---
&&	|and|	(x < 10 && y > 1) 为 true
||	|or|	(x==5 || y==5) 为 false
!	|not|	!(x==y) 为 true


## 11.3 条件运算符(?:)

JavaScript 还包含了基于某些条件对变量进行赋值的条件运算符。(三元运算符)


语法:

	variablename=(condition)?value1:value2 

示例:

	greeting=(visitor=="PRES")?"Dear President ":"Dear ";



# 12. 条件语句

条件语句用于基于不同的条件来执行不同的动作。

通常在写代码时，需要为不同的决定来执行不同的动作。可以在代码中使用条件语句来完成该任务。

在 JavaScript 中，可使用以下条件语句：

- `if` 语句 - 只有当指定条件为 true 时，使用该语句来执行代码

- `if...else` 语句 - 当条件为 true 时执行代码，当条件为 false 时执行其他代码

- `if...else if....else `语句 - 使用该语句来选择多个代码块之一来执行

- `switch` 语句 - 使用该语句来选择多个代码块之一来执行


## 12.1 `If`语句

只有当指定条件为 true 时，该语句才会执行代码。


	if (条件)
	{
	  只有当条件为 true 时执行的代码
	}

- 请使用小写的 if。使用大写字母（IF）会生成 JavaScript 错误！


## 12.2 `If...Else`语句

	if (条件)
	  {
	  当条件为 true 时执行的代码
	  }
	else
	  {
	  当条件不为 true 时执行的代码
	  }

## 12.3 `If...else if...else`语句

	if (条件 1)
	  {
	  当条件 1 为 true 时执行的代码
	  }
	else if (条件 2)
	  {
	  当条件 2 为 true 时执行的代码
	  }
	else
	  {
	  当条件 1 和 条件 2 都不为 true 时执行的代码
	  }

## 12.4 `Switch`语句

	switch(n)
	{
		case 1:
		  执行代码块 1
		  break;
		case 2:
		  执行代码块 2
		  break;
		default:
		  n 与 case 1 和 case 2 不同时执行的代码
	}

- default 关键词来规定匹配不存在时做的事情


# 13. JS循环

JavaScript 支持不同类型的循环：

- `for` - 循环代码块一定的次数

- `for/in` - 循环遍历对象的属性

- `while` - 当指定的条件为 true 时循环指定的代码块

- `do/while` - 同样当指定的条件为 true 时循环指定的代码块


## 13.1 For 循环

	for (语句 1; 语句 2; 语句 3)
	  {
	  被执行的代码块
	  }

## 13.2 For/In 循环

	var person={fname:"John",lname:"Doe",age:25};
	
	for (x in person)
	  {
	  txt=txt + person[x];
	  }

- 循环遍历对象的属性

## 13.3 While循环

	while (条件)
	  {
	  需要执行的代码
	  }

## 13.4 do/while 循环

	do
	  {
	  需要执行的代码
	  }
	while (条件);


# 14. 关键词

## 14.1 Break 和Continue 

break 语句用于跳出循环,跳出循环后，会继续执行该循环之后的代码（如果有的话）

continue 用于跳过循环中的一个迭代。


## 14.2 label 

JavaScript 语言允许，语句的前面有标签（label），相当于定位符，用于跳转到程序的任意位置，标签的格式如下。

语法:

	label:
	语句

- `label`可以是任意的标识符，但不能是保留字，语句部分可以是任意语句。

结合Break 和Continue 使用,跳出特定的循环:

	break labelname;
	
	continue labelname;


- continue 语句（带有或不带标签引用）只能用在循环中。
	
- break 语句（不带标签引用），只能用在循环或 switch 中。
	
	通过标签引用，break 语句可用于跳出任何 JavaScript 代码块：


# 15. JS错误

## 15.1 Try...Catch语句

try 语句捕捉代码块的错误。

- 允许我们定义在执行时进行错误测试的代码块

catch 语句处理错误。

- 允许我们定义当 try 代码块发生错误时，所执行的代码块

-  **try 和 catch 是成对出现的**


	try
	  {
	  //在这里运行代码
	  }
	catch(err)
	  {
	  //在这里处理错误
	  }

## 15.2 Throw语句

throw 语句创建或抛出异常（exception）

- 异常可以是 JavaScript 字符串、数字、逻辑值或对象


	try
	  {
		  var x=document.getElementById("demo").value;
		  if(x=="")    throw "empty";
		  if(isNaN(x)) throw "not a number";
		  if(x>10)     throw "too high";
		  if(x<5)      throw "too low";
	  }
	catch(err)
	  {
		  var y=document.getElementById("mess");
		  y.innerHTML="Error: " + err + ".";
	  }


# 16. JS 表单验证

JavaScript 可用来在数据被送往服务器前对 HTML 表单中的这些输入数据进行验证。

被 JavaScript 验证的这些典型的表单数据有：

- 用户是否已填写表单中的必填项目？

- 用户输入的邮件地址是否合法？

- 用户是否已输入合法的日期？

- 用户是否在数据域 (numeric field) 中输入了文本？
