# 实例对象与new命令
[实例对象与 new 命令 - 阮一峰](https://wangdoc.com/javascript/oop/new.html)

# 1. 对象是什么?
面向对象编程（`Object Oriented Programming`，缩写为 OOP）是目前主流的编程范式。它将真实世界各种复杂的关系，抽象为一个个对象，然后由对象之间的分工与合作，完成对真实世界的模拟。

每一个对象都是功能中心，具有明确分工，可以完成接受信息、处理数据、发出信息等任务。对象可以复用，通过继承机制还可以定制。因此，面向对象编程具有灵活、代码可复用、高度模块化等特点，容易维护和开发，比起由一系列函数或指令组成的传统的过程式编程（procedural programming），更适合多人合作的大型软件项目。

那么，“对象”（object）到底是什么?可以分为俩个层次:

1. 对象是单个实物的抽象

2. 对象是一个容器,封装了属性(`property`)和方法(`method`)


# 2. 构造函数

面向对象编程的第一步，就是要生成对象

- 那通常就需要一个模板，用来表示某一类实物的共同特征，然后对象根据这个模板生成。

典型的面向对象编程语言（比如 C++ 和 Java），都有“类”（class）这个概念。

- 所谓“类”就是对象的模板，对象就是“类”的实例。但是，**JavaScript 语言的对象体系，不是基于“类”的，而是基于构造函数（`constructor`）和原型链（`prototype`）**。

JavaScript 语言使用构造函数（`constructor`）作为对象的模板。

- 所谓”构造函数”，就是专门用来生成实例对象的函数。

	它就是对象的模板，描述实例对象的基本结构。

	一个构造函数，可以生成多个实例对象，这些实例对象都有相同的结构。

构造函数就是一个普通的函数，但是有自己的特征和用法。

	var Vehicle = function () {
	  this.price = 1000;
	};

- 上面代码中，Vehicle就是构造函数。为了与普通函数区别，构造函数名字的第一个字母通常大写。

**构造函数的特点有两个**。

1. **函数体内部使用了`this`关键字，代表了所要生成的对象实例**。

2. **生成对象的时候，必须使用`new`命令**。


# 3. new命令

## 3.1 基本用法

`new` 命令的作用就是执行构造函数,返回一个实例对象

	var Vehicle = function () {
	  this.price = 1000;
	};
	
	var v = new Vehicle();
	v.price // 1000

- 实例`v`从构造函数`Vehicle()`中得到了属性`price`

	`new`命令执行时，构造函数内部的`this`，就代表了新生成的实例对象，`this.price`表示实例对象有一个`price`属性，值是1000。


- 构造函数允许接收参数

- **`new`命令本身就可以执行构造函数,所以构造函数的后面带不带括号都可以**.但是为了表示是函数调用,推荐使用括号.

		// 俩者等价
		var v = new Vehicle();// 推荐的写法
		var v = new Vehicle;// 不推荐的写法

- 如果忘记搭配`new`命令使用构造函数,那么构造函数就会被当做普通函数使用. **此外,其函数中的`this`就会代表全局对象!**

		var Vehicle = function (){
		  this.price = 1000; // 全局变量
		};
		
		var v = Vehicle();
		v // undefined
		price // 1000 

### 3.1.1 确保`new`命令与构造函数一起使用

有俩种方式可以确保`new`命令与构造函数一起使用:

1. **构造函数内部使用严格模式(即函数内部第一行加上`use strict`)可以保证构造函数必须与`new`命令一起使用**,一旦没有一起使用,就会报错

		function Fubar(foo){
		  'use strict';
		  this._foo = foo;
		}
		
		Fubar()
		// TypeError: Cannot set property '_foo' of undefined

	- 由于严格模式中，函数内部的`this`不能指向全局对象，默认等于`undefined`，导致不加`new`调用会报错（JavaScript 不允许对undefined添加属性）。

2. 构造函数内部判断是否使用`new`命令，如果发现没有使用，则直接返回一个实例对象。

		function Fubar(foo, bar) {
		  if (!(this instanceof Fubar)) {
		    return new Fubar(foo, bar);
		  }
		
		  this._foo = foo;
		  this._bar = bar;
		}
		
		Fubar(1, 2)._foo // 1
		(new Fubar(1, 2))._foo // 1


## 3.2 `new`命令的原理

使用`new`命令时，它后面的函数依次执行下面的步骤。

1. 创建一个空对象，作为将要返回的对象实例。

2. 将这个空对象的原型，指向构造函数的prototype属性。

3. **将这个空对象赋值给函数内部的this关键字**。

	- 构造函数内部，`this`指的是一个新生成的空对象，所有针对`this`的操作，都会发生在这个空对象上。

		构造函数之所以叫“构造函数”，就是说这个函数的目的，就是操作一个空对象（即this对象），将其“构造”为需要的样子。

4. 开始执行构造函数内部的代码。


## 3.3 `return`语句

如果构造函数内部有`return`语句，而且`return`后面跟着一个对象，`new`命令会返回`return`语句指定的对象；否则，就会不管`return`语句，返回`this`对象。
	
	var Vehicle = function () {
	  this.price = 1000;
	  return 1000;
	};
	
	(new Vehicle()) === 1000  // false

- `new`命令就会忽略这个`return`语句，返回“构造”后的`this`对象

- **如果`return`语句返回的是一个跟`this`无关的新对象,`new`命令会返回这个新对象**

- **如果对普通函数使用`new`命令,会返回一个空对象**

		function getMessage() {
		  return 'this is a message';
		}
		
		var msg = new getMessage();
		
		msg // {}
		typeof msg // "object"

	- 之所以返回空对象,是因为**`new`命令总是返回一个对象,要么是实例对象,要么是`return`语句指定的对象** . 本例中,返回的是字符串,所以`new`命令就忽略了该语句(数值,字符串,布尔值 是属于原始类型)

## 3.4 `new`命令简化后的内部创建流程
	
	function _new(/* 构造函数 */ constructor, /* 构造函数参数 */ params) {
	  // 将 arguments 对象转为数组
	  var args = [].slice.call(arguments);
	  // 取出构造函数
	  var constructor = args.shift();
	  // 创建一个空对象，继承构造函数的 prototype 属性
	  var context = Object.create(constructor.prototype);
	  // 执行构造函数
	  var result = constructor.apply(context, args);
	  // 如果返回结果是对象，就直接返回，否则返回 context 对象
	  return (typeof result === 'object' && result != null) ? result : context;
	}
	
	// 实例
	var actor = _new(Person, '张三', 28);


## 3.5 new.target

函数内部可以使用`new.target`属性。

- 如果当前函数是`new`命令调用，`new.target`指向当前函数，否则为undefined。

		function f() {
		  console.log(new.target === f);
		}
		
		f() // false
		new f() // true

- 使用这个属性，可以判断函数调用的时候，是否使用new命令。

		function f() {
		  if (!new.target) {
		    throw new Error('请使用 new 命令调用！');
		  }
		  // ...
		}


# 4. Object.create()

构造函数作为模板，可以生成实例对象。但是，有时拿不到构造函数，只能拿到一个现有的对象。

- 那么就可以以这个现有的对象作为模板，生成新的实例对象，这时就可以使用`Object.create()`方法

		var person1 = {
		  name: '张三',
		  age: 38,
		  greeting: function() {
		    console.log('Hi! I\'m ' + this.name + '.');
		  }
		};
		
		var person2 = Object.create(person1);
		
		person2.name // 张三
		person2.greeting() // Hi! I'm 张三.

		person2.__proto__ === person1