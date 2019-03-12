# JavaScript基础语法

[JavaScript 的基本语法 - from:阮一峰](https://wangdoc.com/javascript/basic/grammar.html)

[如何存储你需要的信息-变量- from:MDN](https://developer.mozilla.org/zh-CN/docs/Learn/JavaScript/First_steps/Variables)

# 1. JS 语句

JavaScript 程序的执行单位为行（line），也就是一行一行地执行。一般情况下，每一行就是一个语句。

- 语句（statement）是为了完成某种任务而进行的操作，比如下面就是一行赋值语句。

		var a = 1 + 3;

	- `1 + 3`叫做**表达式（`expression`）**:

		指一个为了得到返回值的计算式。

- **语句和表达式的区别在于:**
	1. 前者主要为了进行某种操作，一般情况下不需要返回值；
	2. 后者则是为了得到返回值，**一定会返回一个值**。凡是 JavaScript 语言中预期为值的地方，都可以使用表达式。比如，赋值语句的等号右边，预期是一个值，因此可以放置各种表达式。

## 1.1 分号`;`

语句以分号结尾，一个分号就表示一个语句结束。

- 使用分号的另一用处是在一行中编写多条语句。

- 分号前面可以没有任何内容，JavaScript 引擎将其视为空语句。

- 在 JavaScript 中，**用分号来结束语句是可选的**。

- 表达式不需要分号结尾,避免产生无意义的语句

	一旦在表达式后面添加分号,则JS引擎会将表达式视为语句

		1 + 3 ;

## 1.2 JS 代码执行顺序

JS是轻量级的解释型语言,浏览器会按照编写顺序来执行每条语句。


## 1.3 JS 代码块

JavaScript 使用大括号，将多个相关的语句组合在一起，称为“**区块**”（block）。

对于var命令来说，JavaScript 的区块不构成单独的作用域（scope）。

	{
	  var a = 1;
	}

	a // 1

- 上面代码在区块内部，使用var命令声明并赋值了变量a，然后在区块外部，变量a依然有效，**区块对于var命令不构成单独的作用域**，**与不使用区块的情况没有任何区别**。

- 在 JavaScript 语言中，单独使用区块并不常见，区块往往用来构成其他更复杂的语法结构，比如`for、if、while、function`等。

## 1.4 JS对大小写敏感

**JavaScript 对大小写是敏感的。**

- 当编写 JavaScript 语句时，请留意是否关闭大小写切换键。

- 函数 `getElementById()` 与 `getElementbyID()` 是不同的。

- 同样，变量 myVariable 与 MyVariable 也是不同的。


## 1.5 空格

JavaScript 会忽略多余的空格。您可以向脚本添加空格，来提高其可读性。下面的两行代码是等效的：

	var name="Hello";
	var name = "Hello";


## 1.6 对代码行进行折行

可以在**文本字符串**中使用**反斜杠(`\`)**对代码行进行换行。下面的例子会正确地显示：

	document.write("Hello \
	World!");

不过，不能像这样折行：

	document.write \
	("Hello World!");

# 2. JS 注释

JavaScript 注释可用于提高代码的可读性。

- JavaScript 不会执行注释。


## 2.1 单行注释

单行注释以 `//` 开头。


	// 输出标题：
	document.getElementById("myH1").innerHTML="Welcome to my Homepage";
	// 输出段落：
	document.getElementById("myP").innerHTML="This is my first paragraph.";


## 2.2 多行注释

多行注释以` /*` 开始，以` */` 结尾。

下面的例子使用多行注释来解释代码：

	/*
	下面的这些代码会输出
	一个标题和一个段落
	并将代表主页的开始
	*/
	document.getElementById("myH1").innerHTML="Welcome to my Homepage";
	document.getElementById("myP").innerHTML="This is my first paragraph.";


# 3. JS变量

变量是对“值”的具名引用。

- 变量不是数值本身，它们仅仅是一个用于存储数值的容器

- 变量就是为“值”起名，然后引用这个名字，就等同于引用这个值。

- 变量的名字就是变量名

- JavaScript 是一种动态类型语言，也就是说，变量的类型没有限制，变量可以随时更改类型。


## 3.1 变量概念

	var a = 1;

- 上面的代码先声明变量a，然后在变量a与数值1之间建立引用关系，称为将数值1“赋值”给变量a。以后，引用变量名a就会得到数值1。

- 最前面的var，是变量声明命令。它表示通知解释引擎，要创建一个变量a。

- **变量的声明和赋值,是分开的俩个步骤,实际上的步骤如下:**

		var a;
		a = 1;

## 3.2  undefined

	var a;
	a // undefined

如果只是声明变量而没有赋值，则该变量的值是undefined。

- undefined是一个特殊的值，表示“无定义”。

千万不要把两个概念弄混淆了，“一个变量存在，但是没有数值”和“一个变量并不存在” — 他们完全是两回事 
- 不存在意味着没有可以存放变量的“盒子”。

	没有定义的值意味着有一个“盒子”，但是它里面没有任何值


## 3.3 变量范围

通过`var`定义的变量,其使用范围由其定义的地方决定

- 函数内:局部变量

- 脚本中:全局变量

存在一种特殊情况,如果不使用`var`关键字直接赋值,那么所定义的变量就是全局变量(无论定义在哪里)

	x = 1;

## 3.4 使用var重复定义

如果使用`var`重新声明一个已经存在的变量，是无效的。

	var x = 1;
	var x;
	x // 1

但是如果重复定义时还赋值了,那么会覆盖掉之前的值

	var x = 1;
	var x = 2;
	
	// 等同于
	
	var x = 1;
	var x;
	x = 2;


## 3.5 一条语句,多个变量

可以在一条语句中声明很多变量。

- 该语句以 var 开头，并使用逗号分隔变量即可：

		var name="Gates", age=56, job="CEO";

声明也可以横跨多行

	var name="Gates",
	age=56,
	job="CEO";

## 3.6 变量提升(var hoisting 顶置 )

JavaScript 引擎的工作方式是，先解析代码，获取所有被声明的变量，然后再一行一行地运行。

这造成的结果，就是所有的变量的声明语句，都会被提升到代码的头部，这就叫做变量提升（hoisting）。

	console.log(a);
	var a = 1;

- 上面代码首先使用`console.log`方法，在控制台（console）显示变量a的值。这时变量a还没有声明和赋值，所以这是一种错误的做法，但是实际上不会报错。因为存在变量提升，真正运行的是下面的代码。

		var a;
		console.log(a);
		a = 1;

	- 最后的结果是显示undefined，表示变量a已声明，但还未赋值。


# 4. 标识符

标识符（identifier）指的是用来识别各种值的合法名称。最常见的标识符就是变量名，以及后面要提到的函数名。JavaScript 语言的标识符对大小写敏感，所以a和A是两个不同的标识符。

标识符有一套命名规则，不符合规则的就是非法标识符。JavaScript 引擎遇到非法标识符，就会报错。

简单说，标识符命名规则如下。

- 第一个字符，可以是任意 Unicode 字母（包括英文字母和其他语言的字母），以及美元符号（`$`）和下划线（`_`）。

- 第二个字符及后面的字符，除了 Unicode 字母、美元符号和下划线，还可以用数字0-9。

- 中文名也是合法的标识符,可以用作变量名


# 5. JS 条件语句

条件语句用于基于不同的条件来执行不同的动作。

通常在写代码时，需要为不同的决定来执行不同的动作。可以在代码中使用条件语句来完成该任务。

在 JavaScript 中，可使用以下条件语句：

- `if` 语句 - 只有当指定条件为 true 时，使用该语句来执行代码

- `if...else` 语句 - 当条件为 true 时执行代码，当条件为 false 时执行其他代码

- `if...else if....else `语句 - 使用该语句来选择多个代码块之一来执行

- `switch` 语句 - 使用该语句来选择多个代码块之一来执行

## 5.1 `if`语句

只有当指定条件为 true 时，该语句才会执行代码。

	if (条件)
	{只有当条件为 true 时执行的代码}

- 请使用小写的 if。使用大写字母（IF）会生成 JavaScript 错误！

## 5.2 `if...Else`语句

	if (条件)
	  {当条件为 true 时执行的代码}
	else
	  {当条件不为 true 时执行的代码}

## 5.3 `if...else if...else`语句

	if (条件 1)
	  {当条件 1 为 true 时执行的代码}
	else if (条件 2)
	  {当条件 2 为 true 时执行的代码}
	else
	  {当条件 1 和 条件 2 都不为 true 时执行的代码}

## 5.4 `switch`语句

	switch(n)
	{
		case 1:
		  执行代码块 1;
		  break;
		case 2:
		  执行代码块 2;
		  break;
		default:
		  n 与 case 1 和 case 2 不同时执行的代码;
	}

- `default`: 关键词来规定匹配不存在时做的事情

- `break`  : 关键字用来跳出switch结构


# 6. JS 循环语句

JavaScript 支持不同类型的循环：

- `for` - 循环代码块一定的次数

- `for/in` - 循环遍历对象的属性

- `while` - 当指定的条件为 true 时循环指定的代码块

- `do/while` - 同样当指定的条件为 true 时循环指定的代码块


## 6.1 for 循环

	for (初始化表达式; 条件; 递增表达式) {
	  语句
	}

- 初始化表达式（initialize）：确定循环变量的初始值，只在循环开始时执行一次。

- 条件表达式（test）：每轮循环开始时，都要执行这个条件表达式，只有值为真，才继续进行循环。

- 递增表达式（increment）：每轮循环的最后一个操作，通常用来递增循环变量。

## 6.2 For/In 循环

	var person={fname:"John",lname:"Doe",age:25};
	
	for (x in person)
	  {
	  txt=txt + person[x];
	  }

- 循环遍历对象的属性

## 6.3 While循环

	while (条件)
	{
		需要执行的代码
	}

## 6.4 do/while 循环

	do {
		语句
	} while (条件);

- 不管条件是否为真，`do...while`循环至少运行一次

# 7. 三元运算符`?:`


JavaScript 还有一个三元运算符（即该运算符需要三个运算子）`?:`，也可以用于逻辑判断。

	(条件) ? 表达式1 : 表达式2

- 如果“条件”为true，则返回“表达式1”的值，否则返回“表达式2”的值。

# 8. break语句和continue语句

break 语句用于跳出循环或代码块,跳出后，会继续执行循环或代码块之后的代码（如果有的话）


continue 语句用于立即终止本轮循环,返回循环结构的头部,开始下一轮循环


- **如果存在多重循环，不带参数的break语句和continue语句都只针对最内层循环**。

# 9. label 

JavaScript 语言允许，语句的前面有标签（label），相当于定位符，用于跳转到程序的任意位置，标签的格式如下。

语法:

	label:
	语句

- `label`可以是任意的标识符，但不能是保留字，语句部分可以是任意语句。

通常`label`用来结合`break` 和`continue` 使用,跳出特定的循环:

	break labelname;
	
	continue labelname;


- continue 语句（带有或不带标签引用）只能用在循环中。
	
- break 语句:
	1. 不带标签引用,只能用在循环或 switch 中
	
	2. 通过标签引用，break 语句可用于跳出任何 JavaScript 代码块


## 9.1 break + label 循环示例
	
	top:
	for (var i = 0; i < 3; i++){
		for (var j = 0; j < 3; j++){
			if (i === 1 && j === 1) break top;
			console.log('i=' + i + ', j=' + j);
		}
	}
	// i=0, j=0
	// i=0, j=1
	// i=0, j=2
	// i=1, j=0


- 上面代码为一个双重循环区块，break命令后面加上了top标签（注意，top不用加引号），满足条件时，直接跳出双层循环。如果break语句后面不使用标签，则只能跳出内层循环，进入下一次的外层循环。


## 9.2 break+ label 代码块示例

	foo: {
	  console.log(1);
	  break foo;
	  console.log('本行不会输出');
	}
	console.log(2);
	// 1
	// 2

- 上面代码执行到break foo，就会跳出区块。


## 9.3 continue +label 循环示例

	top:
	for (var i = 0; i < 3; i++){
		for (var j = 0; j < 3; j++){
			if (i === 1 && j === 1) continue top;
			console.log('i=' + i + ', j=' + j);
		}
	}
	// i=0, j=0
	// i=0, j=1
	// i=0, j=2
	// i=1, j=0
	// i=2, j=0
	// i=2, j=1
	// i=2, j=2

- 上面代码中，continue命令后面有一个标签名，满足条件时，会跳过当前循环，直接进入下一轮外层循环。如果continue语句后面不使用标签，则只能进入下一轮的内层循环。


