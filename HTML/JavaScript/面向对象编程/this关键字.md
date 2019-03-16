# this 关键字

[this 关键字- 阮一峰](https://wangdoc.com/javascript/oop/this.html)

[函数的this关键字 - MDN](https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Operators/this)

[Function.prototype.call()  - MDN](https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/Function/call)

# 1. this关键字的含义

`this`有一个特点：**它总是返回一个对象**

- 简单的说:`this`就是属性或方法"当前"所属的对象

   this.property // this 就代表了property 属性当前所在的对象

**JavaScript 语言之中，一切皆对象，运行环境也是对象，所以函数都是在某个对象之中运行**，**`this`就是函数运行时所在的对象（环境）**。

- **因为JavaScript 支持运行环境动态切换，也就是说，this的指向是动态的，没有办法事先确定到底指向哪个对象.所以导致`this`容易混淆**

   ```
   Function.prototype.test = function (){
   	    console.log(this)
   	}
   	var f = function(){
   		console.log(this)
   	}
   	f.test() // 调用对象是 f
   	f() // 调用对象是 Window
   ```

   




## 1.1 `this`就是属性或方法当前所属的对象

	var person = {
	  name: '张三',
	  describe: function () {
	    return '姓名：'+ this.name;
	  }
	};
	
	person.describe() // 姓名: 张三

- `this.name`中的`this`表示 属性`describe` 所在的对象。

	由于`this.name`是在`describe`方法中调用，而`describe`方法所在的当前对象是`person`，因此`this`指向`person`，`this.name`就是`person.name`

## 1.2 `this`的指向是可变的

	var A = {
	  name: '张三',
	  describe: function () {
	    return '姓名：'+ this.name;
	  }
	};
	
	var B = {
	  name: '李四'
	};
	
	B.describe = A.describe;
	B.describe()
	// "姓名：李四"

- 这里由于对象的属性被赋值给了另外一个对象,所以`this`由原先的指向A 变成指向了B


重构上面的代码会更加的清晰:

	function f() {
	  return '姓名：'+ this.name;
	}
	
	var A = {
	  name: '张三',
	  describe: f
	};
	
	var B = {
	  name: '李四',
	  describe: f
	};
	
	A.describe() // "姓名：张三"
	B.describe() // "姓名：李四"

- 函数`f`内部使用了`this`关键字,会随着`f`所在的对象不同 而获得不同的属性


## 1.3 只要函数被赋值给另一个变量,`this`就会改变

	var A = {
	  name: '张三',
	  describe: function () {
	    return '姓名：'+ this.name;
	  }
	};
	
	var name = '李四';
	var f = A.describe;
	f() // "姓名：李四"

- `this`指向了全局对象


## 1.4 网页编程实例

	<input type="text" name="age" size=3 onChange="validate(this, 18, 99);">
	
	<script>
	function validate(obj, lowval, hival){
	  if ((obj.value < lowval) || (obj.value > hival))
	    console.log('Invalid Value!');
	}
	</script>

- `this`代表了当前对象(即文本框)

# 2. `this`的实质

JavaScript 语言之所以有 `this` 的设计，**跟内存里面的数据结构有关系**。


## 2.1 属性在内存中的数据结构

	var obj = { foo:  5 };

- 将对象赋值给变量obj的流程是:

	1. JS引擎在内存中生成一个对象`{foo:5}`
	2. 把这个对象的内存地址赋值给变量`obj`. 也就是说`obj`保存了一个地址(`reference`)

- 读取`obj.foo`的流程:

	1. JS引擎从`obj`拿到对象的内存地址
	2. 然后从地址读出**原始的对象**,返回`foo`属性

**原始的对象**以字典结构保存,每一个属性名都对应一个属性描述对象,对应上面的`obj.foo`属性:

	{
	  foo: {
	    [[value]]: 5
	    [[writable]]: true
	    [[enumerable]]: true
	    [[configurable]]: true
	  }
	}

- `foo`属性的值保存在属性描述对象的`value`属性中

## 2.2 方法在内存中的数据结构

**方法的与属性相似,但是JS引擎会将函数单独保存在内存中,并提供一个地址指向它!**

	var obj = { foo: function () {} };

- JS引擎会将函数单独保存在内存中,然后将函数的地址赋值给`foo`属性的`value`属性

   ```
   {
   	  foo: {
   	    [[value]]: 函数的地址
   	    ...
   	  }
   	}
   ```

   

- **由于函数是一个单独的值,所以它可以在不同的环境(上下文)执行**

   ```
   var f = function () {}; // f 保存了函数的地址
   var obj = { f: f }; 
   
   f()	// 单独执行
   obj.f()	// obj 环境执行
   ```

   

- **JS允许在函数体内部,引用当前环境的其他变量**

   ```
   var f = function () {
   	  console.log(x); // x 变量由运行环境提供
   	};
   ```

   

**由于函数可以在不同的环境中运行,所以`this`就被设计出来用作在"函数体内部指代函数当前的运行环境(context)"**

	var f = function () {
	  console.log(this.x); 
	}
	f() // 这时 函数内部的this 就指代 window(全局环境)
	
	var obj = {
	  f: f,
	  x: 2,
	};
	obj.f() // 这时 函数内部的this 就指代 obj


# 3. `this`的使用场合

`this`的值受到三个因素影响:

1. 是否处于严格模式

2. 执行环境

3. 函数的调用方式


## 3.1 全局环境

	this === window // true
	
	function f() {
	  console.log(this === window);
	}
	f() // true

- 通过全局对象调用了函数`f()`

- **无论是否在严格模式下,全局环境中使用`this`(在任何函数体外部),指代的就是全局对象`window`**

## 3.2 函数(运行内)环境

当在函数内部使用`this`时,`this`的取值取决于函数被调用的方式 和 是否处于严格模式

### 3.2.1 简单调用时的`this`

**非严格模式:**

	function f1(){
	  return this;
	}
	f1() === window;   //在浏览器中，全局对象是window

- `f1()`在全局环境中直接调用,因此函数内部的`this`指向全局对象

	- **直接调用并不是仅仅指在全局作用域下进行调用,而是说在任何作用域下通过`funName()`这种方式调用**



**严格模式:**

	function f2(){
	  "use strict"; // 严格模式
	  return this;
	}
	
	f2() === undefined; // true

- 只要`f2()`方法被直接调用,而不是作为对象的属性或方法代用(如`window.f2()`),那么就应该是`undefined`

	**在严格模式下,如果`this`没有被执行环境(execution context)定义,那么会保持`undefined`**

### 3.2.2 箭头函数里的`this`

在箭头函数中，`this`与封闭词法环境的`this`保持一致。在全局代码中，它将被设置为全局对象：
	
	var globalObject = this;
	var foo = (() => this);
	console.log(foo() === globalObject); // true


### 3.2.3 对象的方法里的`this`

**当函数作为对象里的方法被调用时,函数内部的`this`就会指向调用该函数的对象!**

	var obj ={
	  // 定义方法
	  foo: function () {
	    console.log(this);
	  }
	};
	
	obj.foo() // obj

- 如果对象的方法里面包含`this`，`this`的指向就是方法运行时所在的对象。

	**即使函数定义方式或位置发生改变,也不会影响这种行为!**例如,先定义函数,再将其附属到对象上

		var o = {prop: 37};
		function independent() {
		  return this.prop;
		}
		o.f = independent;
		console.log(o.f()); // logs 37


**示例**

	var a = {
	  p: 'Hello',
	  b: {
		// 定义方法
	    m: function() {
	      console.log(this.p);
	    }
	  }
	};
	
	a.b.m() // undefined

- 上述代码中,`m`方法在`b`对象中,因此该方法内部的`this`指向的是`a.b`而不是`a`,其实际执行的逻辑如下:

   ```
   var b = {
   	  m: function() {
   	   console.log(this.p);
   	  }
   	};
   	
   
   var a = {
     p: 'Hello',
     b: b
   };
   
   (a.b).m() // 等同于 b.m()
   ```

   

- **如果`this`所在的方法不在对象的第一层，这时`this`只是指向当前一层的对象，而不会指向更上面的层**

  想要解决这种情况,那就需要将`this`的指向外层对象`a`,或者**将属性`p`放到内层对象`b`中**

  	var a = {
  	  b: {
  		// 方法里的`this`指向的是 b
  	    m: function() {
  	      console.log(this.p);
  	    },
  	    p: 'Hello'
  	  }
  	};

- 如果将嵌套对象内部的方法赋值给一个变量,`this`会指向全局对象(**因为此时调用方法的是全局对象!**)

   ```
   var hello  = a.b.m;
   	hello()  // undefined
   ```

   

   - 将`m`所在的对象`b`赋值给变量`hello`,这样调用时,`this`的指向就不会改变了(**因此此时调用方法的是`a.b`对象**)

      ```
      var hello = a.b;
      hello.m() // Hello
      ```

      


#### 3.2.3.1 方法中的`this`的指向会发生改变

以下几种情况都会改变方法内部的`this`的指向:

	// 情况一
	(obj.foo = obj.foo)() // window
	// 情况二
	(false || obj.foo)() // window
	// 情况三
	(1, obj.foo)() // window

- 上述情况中,**`obj.foo`就是一个值,这个值真正调用的时候,运行环境已经不是`obj`了,而是全局环境,所以`this`不再指向`obj`**

可以这样理解，JavaScript 引擎内部，`obj`和`obj.foo`储存在两个内存地址，称为地址一和地址二

- `obj.foo()`这样调用时，是从地址一调用地址二，因此地址二的运行环境是地址一，`this`指向obj。

- 但是，上面三种情况，都是直接取出地址二进行调用，这样的话，运行环境就是全局环境，因此`this`指向全局环境。

上面三种情况等同于下面的代码。

	// 情况一
	(obj.foo = function () {
	  console.log(this);
	})()
	// 等同于
	(function () {
	  console.log(this);
	})()
	
	// 情况二
	(false || function () {
	  console.log(this);
	})()
	
	// 情况三
	(1, function () {
	  console.log(this);
	})()

### 3.2.4 原型链中的`this`

**对于在对象原型链上的某处定义的方法,`this`指向的是调用这个方法的对象，就像该方法在对象上一样**

	Function.prototype.test = function (){
	    return this;
	}
	function ƒ (){
	    return this;
	}
	
	f.test() 
	// 输出内容
	ƒ (){
	    return this;
	}

- 函数`test()`内部的`this` 指向函数`f()`(**因为函数`f()`也是对象,根据函数内部的`this`会指向调用函数的对象得知,函数`test()`内部的`this`会指向函数`f()`**)


	f() 
	// 输出内容
	window

- 函数`f()`在全局环境中被调用,因此调用对象是window(**因为js中全员皆对象,环境也是对象**)


### 3.2.5 构造函数

**构造函数中的`this`，指的是实例对象**

	var Obj = function (p) {
	  this.p = p; // Obj.p = p
	};
	
	var o = new Obj('Hello World!');
	o.p // "Hello World!"

- 构造函数内的`this` 指向实例对象 `Obj` , 其赋值语句相当于`Obj.p = p` 



# 4. `this`的易错点

## 4.1 多层`this`指代对象不一致

**由于`this`的指代是不确定的,因此尽量避免在函数中包含多层的`this`. 如需要使用,必须明确指定`this`指向的对象!**

	var o = {
	  f1: function () {
	    console.log(this);
	    var f2 = function () {
	      console.log(this);
	    }(); // 立即执行的函数
	  }
	}
	
	o.f1()
	// Object
	// Window

- 第一层`this`指向对象`o`,**因为函数内的`this`指向调用其的对象**

- 第二层`this`指向全局对象,**因为在任何作用域下以`funName()`这种方式调用函数,其内部的`this`就指向全局对象**

	上述代码也可以以下面的表现形式:

		var temp = function () {
		  console.log(this); 
		};
		
		var o = {
		  f1: function () {
		    console.log(this);
		    var f2 = temp(); // this 指向全局对象
		  }
		}

### 4.1.1 解决办法1(使用中间变量固定`this')

	var o = {
	  f1: function() {
	    console.log(this);
	    var that = this;
	    var f2 = function() {
	      console.log(that);
	    }();
	  }
	}
	
	o.f1()
	// Object
	// Object

- 上述代码,**使用了一个变量固定`this`的值,然后在内存函数中调用,这是非常常见的用法!!**

### 4.1.2 解决办法2

	var counter = {
	  count: 0
	};
	counter.inc = function () {
	  'use strict';
	  this.count++
	};
	counter.inc() //  count = 1
	var f = counter.inc;
	f()  // TypeError: Cannot read property 'count' of undefined

- **JS提供了严格模式,可以硬性避免这种问题,在这种模式下,如果函数内部的`this`指向了全局对象 就会报错!**

## 4.2 数组处理方法中的`this`指向全局对象

**数组的`map()`和`foreach()`方法,允许提供一个函数作为参数,但是在这个函数中的`this`会指向全局对象**

	var o = {
	  v: 'hello',
	  p: [ 'a1', 'a2' ],
	  f: function f() {
	    this.p.forEach(function (item) {
	      console.log(this.v + ' ' + item);
	    });
	  }
	}
	
	o.f()
	// undefined a1
	// undefined a2

- 这里导致 undefined 的原因也是`this`指代对象不同导致的
	1. 外层的`this`指向对象`o`
	2. 内层的`this`指向全局对象,**函数只要是独立被调用的，那就是指向window！**

### 4.2.1 解决办法1(使用中间变量固定`this`)

	var o = {
	  v: 'hello',
	  p: [ 'a1', 'a2' ],
	  f: function f() {
	    var that = this;
	    this.p.forEach(function (item) {
	      console.log(that.v+' '+item);
	    });
	  }
	}
	
	o.f()
	// hello a1
	// hello a2

### 4.2.2 解决办法2(固定运行环境)

**`foreach`方法的第二个参数接收一个对象作为其`this`，这样就可以固定它的运行环境**

	var o = {
	  v: 'hello',
	  p: [ 'a1', 'a2' ],
	  f: function f() {
	    this.p.forEach(function (item) {
	      console.log(this.v + ' ' + item);
	    }, this);
	  }
	}
	
	o.f()
	// hello a1
	// hello a2

## 4.3 回调函数被调用环境不同导致`this`指向不同

回调函数中的`this`往往会改变指向，最好避免使用。

	var o = new Object();
	o.f = function () {
	  console.log(this === o);
	}
	
	// jQuery 的写法
	$('#button').on('click', o.f);

- 此时`this`指向了按钮的`DOM`对象,而不是对象`o`

# 5. 绑定this

`this`的动态切换为 JavaScript 创造了巨大的灵活性，但也使得编程变得困难和模糊。有时，需要把`this`固定下来，避免出现意想不到的情况(例如上述的情况)。

- JavaScript 提供了`call、apply、bind`这三个方法，来切换/固定`this`的指向


## 5.1 Function.prototype.call()

**函数实例对象可以使用`Function.prototype.call()`方法(原型链),这个方法可以指定函数内部`this`的指向(即指定函数执行时所在的作用域),然后在所指定的作用域中调用该函数**

	var obj = {};
	
	var f = function () {
	  return this;
	};
	
	f() === window // true
	f.call(obj) === obj // true

- 全局环境运行函数`f`,函数中的`this`指向全局对象(浏览器为`window`).

	`call()`方法可以改变`this`的指向,将`this`指向对象`obj`

**使用call()方法有以下几个注意事项:**

- **`call()`方法的参数如果为空,`null`或`undefined`,则默认传入全局对象!**

		var n = 123;
		var obj = { n: 456 };
		
		function a() {
		  console.log(this.n);
		}
		
		a.call() // 123
		a.call(null) // 123
		a.call(undefined) // 123
		a.call(window) // 123
		a.call(obj) // 456

- **`call()`方法的参数如果是一个原始类型,会被自动转成对应的包装对象!**

		var f = function () {
		  return this;
		};
		
		f.call(5)
		// Number {[[PrimitiveValue]]: 5}

- **`call()`方法可以接受多个参数**

	**第一个参数为`this`所需要指向的对象,后面的参数就是函数调用所需的参数(数量必须对应)**

		function add(a, b) {
		  return a + b;
		}
		
		add.call(this, 1, 2) // 3

### 5.1.1 `call`方法的应用

**`call()`方法的可以用来调用对象的原生方法:**

	var obj = {};
	obj.hasOwnProperty('toString') // false
	
	// 覆盖掉继承的 hasOwnProperty 方法
	obj.hasOwnProperty = function () {
	  return true;
	};
	obj.hasOwnProperty('toString') // true
	
	Object.prototype.hasOwnProperty.call(obj, 'toString') // false

- `hasOwnProperty()`是obj对象继承的方法，如果这个方法一旦被覆盖，就不会得到正确结果

	call方法可以解决这个问题，它将`hasOwnProperty()`方法的原始定义放到obj对象上执行，这样无论obj上有没有同名方法，都不会影响结果

### 5.1.2 `call()`方法调用父构造函数

在一个子构造函数中,可以通过调用父构造函数的`call()`方法实现继承,类似Java的写法

	function Product(name, price) {
	  this.name = name;
	  this.price = price;
	}
	
	function Food(name, price) {
	  Product.call(this, name, price);
	  this.category = 'food';
	}
	
	function Toy(name, price) {
	  Product.call(this, name, price);// this 为new 出来的对象
	  this.category = 'toy';
	}
	
	var cheese = new Food('feta', 5);
	var fun = new Toy('robot', 40);

- 在`new`构造函数时,`this`会指向实例对象,就相当于给实例对象添加了 `name`和`price`属性


## 5.2 Function.prototype.apply()

**`apply()`方法的作用与`call()`方法类似，也是改变`this`指向，然后再调用该函数**。

- 唯一的区别就是，**它接收一个数组作为函数执行时的参数**


	func.apply(thisValue, [arg1, arg2, ...])

- `apply()`方法的第一个参数也是`this`所要指向的那个对象，如果不传,传`null`或传`undefined`，则等同于指定全局对象。

	第二个参数则是一个数组，该数组的所有成员依次作为参数，传入原函数。原函数的参数，在call方法中必须一个个添加，但是在apply方法中，必须以数组形式添加。

### 5.2.1 `apply()`方法的应用

1. **找出数组的最大元素**

	JS不提供直接找出数组最大元素的函数.`Math.max()`方法需要将数组的元素一个个传入. 那么结合`apply()`方法就可以直接将数组传入从而获得最大元素

		var  a = [1,5,2,7];
		// Math.max(1,5,2,7); // 7
		Math.max.apply(null,a); // 7 

2. **将数组的空元素变成`undefined`**

	利用`Array`构造函数将数组的空元素变成`undefined`

		Array.apply(null, ['a', ,'b'])
		// [ 'a', undefined, 'b' ]

	- 空元素与`undefined`的差别在于，数组的`forEach`方法会跳过空元素，但是不会跳过`undefined`。因此，遍历内部元素的时候，会得到不同的结果。

3. **转换类似数组的对象**

	利用数组对象的slice方法，可以将一个类似数组的对象（比如arguments对象）转为真正的数组。(前提是被处理的对象必须拥有`length`属性,并且有相对应的键值对)
	
		Array.prototype.slice.apply({0: 1, length: 1}) // [1]
		Array.prototype.slice.apply({0: 1}) // []
		Array.prototype.slice.apply({0: 1, length: 2}) // [1, undefined]
		Array.prototype.slice.apply({length: 1}) // [undefined]

4. **绑定回调函数的对象**

		var o = new Object();
		o.f = function () { // 添加方法
		  console.log(this === o);
		}
		var f = function (){
		  o.f.apply(o);  // 将o.f 的this 指向 o
		  // 或者 o.f.call(o);
		};
		// jQuery 的写法
		$('#button').on('click', f); // 执行输出 true

- 由于apply方法（或者call方法）不仅绑定函数执行时所在的对象，还会立即执行函数，因此不得不把绑定语句写在一个函数体内

## 5.3 Function.prototype.bind()

**`bind`方法用于将函数体内的`this`绑定到某个对象(没有限制)，然后返回一个新函数。**

	var d = new Date();
	d.getTime() // 1481869925657
	
	var print = d.getTime;
	print() // Uncaught TypeError: this is not a Date object.

- 报错的原因是 `getTime()`方法内部的`this`被赋值给变量`print`之后被改变了,指向了全局对象

- 可以通过`bind()`方法解决这个问题

		var print = d.getTime.bind(d); //  参数就是`this`需要指向的对象
		print() // 1481869925657

**示例:**

	var counter = {
	  count: 0,
	  inc: function () {
	    this.count++;
	  }
	};
	var func2 = counter.inc; // 
	func2() // 无效!
	var func = counter.inc.bind(counter);
	func(); 
	counter.count // 1

- `counter.inc`被赋值给了变量`func`,如果不调用`bind()`方法,其内部`this`会指向全局对象

**`bind()`方法可以将`this`绑定到其他对象上**

	var counter = {
	  count: 0,
	  inc: function () {
	    this.count++;
	  }
	};
	
	var obj = {
	  count: 100
	};
	var func = counter.inc.bind(obj);
	func();
	obj.count // 101

**`bind()`方法可以接受多个参数,将这些参数绑定原函数的参数,返回一个新的函数**

	var add = function (x, y) {
	  return x * this.m + y * this.n;
	}
	
	var obj = {
	  m: 2,
	  n: 2
	};
	
	var newAdd = add.bind(obj, 5);
	newAdd(5) // 20

**`bind()`方法的一个参数如果为空,传`null`或传`undefined`,等于将`this`绑定到全局对象**

### 5.3.1 `bind()`方法使用注意

1. **每次调用都会返回一个新函数!**

	这会在设置/解除监听事件时出现问题(无法取消绑定),其根本原因是对象不同!

		element.addEventListener('click', o.m.bind(o));
		element.removeEventListener('click', o.m.bind(o)); // 无效

	- `click`事件绑定的是`bind()`方法生成的一个匿名函数,而解绑时用的是另外一个匿名函数

	- 解决办法就是保存这个匿名函数,绑定和解绑使用同一个匿名函数

2. **结合回调函数使用**

	回调函数是JS常用的模式之一,但是存在一个常见的错误就是将包含`this`的方法直接当做回调函数(`this`指向改变,导致程序出错)

		var counter = {
		  count: 0,
		  inc: function () {
		    'use strict';
		    this.count++;
		  }
		};
		
		function callIt(callback) {
		  callback();
		}
		
		callIt(counter.inc) // 报错,无法找到count!
		callIt(counter.inc.bind(counter)); // this 指向 counter
		counter.count // 1

	另外一个例子就是某些数组方法接收函数作为参数,这些函数内部的`this`指向容易出错

		var obj = {
		  name: '张三',
		  times: [1, 2, 3],
		  print: function () {
		    this.times.forEach(function (n) {
		      console.log(this.name); // this 指向全局对象
		    });
		  }
		};
		
		obj.print()
		// 没有任何输出
	
	- 换一种写法就能清晰的看出 `forEach()`方法参数(即函数)内部`this`的指向:

			obj.print = function () {
			  this.times.forEach(function (n) {
			    console.log(this === window);
			  });
			};
			
			obj.print()
			// true
			// true
			// true

3. 结合`call()`方法使用

	利用`bind()`方法，可以改写一些 JavaScript 原生方法的使用形式，以数组的`slice()`方法为例。

		[1, 2, 3].slice(0, 1) // [1]
		// 等同于
		Array.prototype.slice.call([1, 2, 3], 0, 1) // [1]

	- 这样做的本质是在`[1, 2, 3]`上面调用`Array.prototype.slice`方法，因此可以用`call`方法表达这个过程，得到同样的结果

	---
	`call()`方法实质上是调用`Function.prototype.call()`方法将其`this`指向进行修改(修改成需要执行的对象),因此可以将上述代码用`bind()`方法改写
			
			var slice = Function.prototype.call.bind(Array.prototype.slice);
			slice([0,1,2],0,1) // [0]

	- **上面代码的含义就是，将`Array.prototype.slice`变成`Function.prototype.call`方法的`this`所指向的对象(`this`总是指向属性或方法当前所在的对象)，调用时就变成了`Array.prototype.slice.call`**

	---
	类似的写法还有:

			var push = Function.prototype.call.bind(Array.prototype.push);
			var pop = Function.prototype.call.bind(Array.prototype.pop);
			
			var a = [1 ,2 ,3];
			push(a, 4)
			a // [1, 2, 3, 4]
			
			pop(a)
			a // [1, 2, 3]

	再进一步.将`Function.prototype.call`方法内的`this`指向`Function.prototype.bind`对象，就意味着`bind()`方法的调用形式也可以被改写

			function f() {
			  console.log(this.v);
			}
			
			var o = { v: 123 };
			var bind = Function.prototype.call.bind(Function.prototype.bind);
			bind(f, o)() // 123

	- 上面代码的含义就是，将`Function.prototype.call`的`this`指向`Function.prototype.bind`对象，所以`bind()`方法就可以直接使用，不需要在函数实例上使用。

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1g12i13htu9j21980zon7n.jpg)





# 6.  `this` 用法的总结

## 6.1 `this`的4种指向

1. 独立调用：`func()`，函数独立调用，this指向window，;

2. 方法调用：`obj.func()`，函数作为obj的一个方法（属性）调用，this指向obj；

3. 构造函数调用：`new Func()`，如果在一个函数前面带上 new 关键字来调用， 那么背地里将会创建一个连接到该函数的 prototype 的新对象，this指向这个新的对象；

4. call、apply、bind调用：

   ```
   func.call(obj,value1,value2);  func.apply(obj,[value1,value2])； func.bind(obj,value1,value2)();  func.bind(obj)(value1,value2); 动态改变this的指向obj；
   ```



## 6.2 独立调用和方法调用

1. 全局环境中，this默认指向到window；
2. 函数独立调用（不论这个函数在哪调用），this默认指向到window；
3. 当函数被作为对象的方法（对象的一个属性）调用时，this指向该对象；
4. 函数只有调用了之后才有this的概念，不然它只是代码而已，我们经常把函数传给一个变量，如果后面没加括号，就只是传了个函数，而不是执行结果；

