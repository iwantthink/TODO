# JavaScript对象原型概念

[对象原型 - MDN](https://developer.mozilla.org/zh-CN/docs/Learn/JavaScript/Objects/Object_prototypes)

[Javascript继承机制的设计思想 - 阮一峰](http://www.ruanyifeng.com/blog/2011/06/designing_ideas_of_inheritance_mechanism_in_javascript.html)

[js中\_\_proto\_\_和prototype的区别和关系？](https://www.zhihu.com/question/34183746)

# 1. 简介

JavaScript 常被描述为一种基于原型的语言 (`prototype-based language`)

- 每个对象拥有一个原型对象(通过`__proto__`指向构造它的构造函数的原型对象)，对象以其原型为模板、从原型继承方法和属性。

- 原型对象也可能拥有原型对象(因为原型对象也是对象,也能有`__proto__`属性)，并从中继承方法和属性，一层一层、以此类推。

	**这种关系常被称为原型链 (prototype chain)，它解释了为何一个对象会拥有定义在其他对象中的属性和方法**。

- 准确地说，**这些属性和方法定义在Object的构造器函数(`constructor functions`)之上的`prototype`属性上，而非对象实例本身**。


在传统的 OOP 中，首先定义“类”，此后创建对象实例时，类中定义的所有属性和方法都被复制到实例中。

- 在 JavaScript 中并不如此复制——**而是在对象实例和它的构造器之间建立一个链接**（它是`__proto__`属性，是从构造函数的`prototype`属性派生的），之后通过上溯原型链，在构造器中找到这些属性和方法

-  理解**对象的原型**（可以通过`Object.getPrototypeOf(obj)`或者已被弃用的`__proto__`属性获得）与**构造函数的`prototype`属性**之间的区别是很重要的。

	前者是每个实例上都有的属性，后者是构造函数的属性。也就是说，Object.getPrototypeOf(new Foobar())和Foobar.prototype指向着同一个对象


# 2. JavaScript中的原型介绍

在javascript中，函数可以有属性。**此外,每个函数都有一个特殊的属性叫作原型（prototype）** 


## 2.1 示例

	function doSomething(){}
	console.log( doSomething.prototype );

	>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	{
	    constructor: ƒ doSomething(),
	    __proto__: {
	        constructor: ƒ Object(),
	        hasOwnProperty: ƒ hasOwnProperty(),
	        isPrototypeOf: ƒ isPrototypeOf(),
	        propertyIsEnumerable: ƒ propertyIsEnumerable(),
	        toLocaleString: ƒ toLocaleString(),
	        toString: ƒ toString(),
	        valueOf: ƒ valueOf()
	    }
	}

- `doSomething`函数有一个默认的原型属性


给`doSomething`的原型添加一些属性:

	doSomething.prototype.foo = "bar";// add a property onto the prototype
	console.log( doSomething.prototype );

	>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	{
	    foo: "bar",
	    constructor: ƒ doSomething(),
	    __proto__: {
	        constructor: ƒ Object(),
	        hasOwnProperty: ƒ hasOwnProperty(),
	        isPrototypeOf: ƒ isPrototypeOf(),
	        propertyIsEnumerable: ƒ propertyIsEnumerable(),
	        toLocaleString: ƒ toLocaleString(),
	        toString: ƒ toString(),
	        valueOf: ƒ valueOf()
	    }
	}

通过new运算符在这个原型的基础上创建实例,并给这个实例添加一些属性

	var instance = new doSomething();
	instance.prop = "some value"; // add a property onto the object
	console.log( instance );

	>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	
	{
	    prop: "some value",
	    __proto__: { // instance.prototype
	        foo: "bar",
	        constructor: ƒ doSomething(),
	        __proto__: {
	            constructor: ƒ Object(),
	            hasOwnProperty: ƒ hasOwnProperty(),
	            isPrototypeOf: ƒ isPrototypeOf(),
	            propertyIsEnumerable: ƒ propertyIsEnumerable(),
	            toLocaleString: ƒ toLocaleString(),
	            toString: ƒ toString(),
	            valueOf: ƒ valueOf()
	        }
	    }
	}

- `instance` 的 `__proto__` 属性就是`doSomething.prototype`

1. 当访问`instance`的属性,浏览器首先查找 instance 对象是否有这个属性. 

	如果没有这个属性,那么浏览器会在 instance的`__proto__`中查找(即`doSomething.prototype`)

2. 如果 `instance` 的 `__proto__` 有这个属性, 那么 `instance` 的 `__proto__` 上的这个属性就会被使用. 

	否则, 如果 `instance` 的 `__proto__` 没有这个属性, 浏览器就会去查找 `instance` 的 `__proto__` 中的 `__proto__` ，看它是否有这个属性

3. 默认情况下, 所有函数的原型属性的 `__proto__` 就是 `window.Object.prototype`. 所以 `instance` 的 `__proto__` 的 `__proto__` (也就是 `doSomething.prototype` 的 `__proto__` (就是 `Object.prototype`)) 会被查找是否有这个属性. 

4. 如果没有在它里面找到这个属性, 然后就会在 `instance` 的 `__proto__` 的 `__proto__` 的 `__proto__` 里面查找. 然而这有一个问题: `instance` 的 `__proto__` 的 `__proto__` 的 `__proto__` 不存在. 最后, 原型链上面的所有的 `__proto__` 都被找完了, 浏览器所有已经声明了的 `__proto__` 上都不存在这个属性，然后就得出结论，这个属性是 `undefined`.


# 3. 理解原型链

	function Person(first, last, age, gender, interests) {
	  this.name = {
	    first,
	    last
	  };
	  this.age = age;
	  this.gender = gender;
	  this.interests = interests;
	  this.bio = function() {
	    alert(this.name.first + ' ' + this.name.last + ' is ' + this.age + ' years old. He likes ' + this.interests[0] + ' and ' + this.interests[1] + '.');
	  };
	  this.greeting = function() {
	    alert('Hi! I\'m ' + this.name.first + '.');
	  };
	};

	var p1 = new Person('ryan','Ma',32,'male',['math','english'])

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g1162gi6i9j206k0azwef.jpg)

- 在浏览器的控制台中输入`p1.`,浏览器会弹出自动补全的提示.在这个列表中可以看到如下内容:

	1. 定义在p1的原型对象,即`Person()`构造函数中的成员
	2. 定义在Person构造函数中的原型对象 ,即`Object`中的成员

原型链的运作机制如下:

![原型链的运作机制](http://ww1.sinaimg.cn/large/6ab93b35gy1g11614gfx6j20jh04dwem.jpg)

- 原型链中的方法和属性没有被复制到其他对象中,它们被访问是通过"原型链"的方式

- 没有官方的方法用于直接访问一个对象的原型对象 . 原型链中的“连接”被定义在一个内部属性中，在 JavaScript 语言标准中用 [[`prototype`]] 表示（参见 ECMAScript）。

	然而，大多数现代浏览器还是提供了一个名为 `__proto__` （前后各有2个下划线）的属性，其包含了对象的原型。

	可以尝试输入 `person1.__proto__` 和 `person1.__proto__.__proto__`，看看代码中的原型链是什么样的！

# 4. `__proto__`和`prototype`的区别和关系

明确几个概念:

1. 在JS中,万物皆对象.方法(Function)是对象.方法的原型(Function.prototype)是对象. 因此它们都会具有对象共有的特点.

	**即:对象具有属性`__proto__`(可称为隐式原型)**,**一个对象的隐式原型指向构造该对象的构造函数的原型(`prototype`)**!!!! 这保证了实例能够访问在构造函数原型中定义的属性和方法

		p1.__proto__ == Person.prototype // true

2. 方法这个特殊的对象,除了和其他对象一样拥有上述`__proto__`属性之外,**还有自己特有的属性`prototype`(称为原型属性)**

	- 这个原型属性(`prototype`)是一个指针,指向一个对象(这个对象的作用就是包含所有实例共享的属性和方法,**也被称为 原型对象**)

	- **原型对象也有一个属性,叫做`constructor`,这个属性包含了一个指针,指回原来的构造函数**

			Person.prototype.constructor == Person // true

## 4.1 概念分析

![](http://ww1.sinaimg.cn/large/6ab93b35gy1g117p19lszj20eg0gawi1.jpg)


1. 构造函数`Foo()`

	已知构造函数的原型属性(`Foo.prototype`)指向了原型对象,在原型对象里有所有实例共有的方法和属性,所有构造函数声明的实例(`f1,f2`)都可以共享这些属性和方法.

2. 原型对象`Foo.prototype`

	`Foo.prototype`保存着实例共享的属性和方法,并且有一个属性`constructor`,其包含了一个指针,指向了构造函数

3. 实例`f1,f2`

	`f1,f2`是`Foo`构造函数的俩个实例,这俩个对象也有属性`__proto__`,指向了其构造函数的原型对象(通过这种形式,就可以访问原型对象的方法和属性)

4. 构造函数`Foo()`作为对象

	构造函数`Foo()`作为对象,也有`__proto__`属性,其指向了构造该对象的构造函数的原型(`prototype`).

	函数的构造函数是`Function()`,所以这里的`__proto__`属性就是指向了`Function.prototype`

5. 原型对象的`__proto__`

	`Foo.prototype`和`Function.prototype`这俩个原型对象作为对象也拥有`__proto__`,其同样指向了构造该对象的构造函数的原型(`prototype`)

	原型对象的构造函数是`Object()`,所以这里的`__proto__`属性就是指向了`Object.prototype`

6. **`Object.prototype`的`__proto__`属性指向null**!!


## 4.2 实例分析


![](http://ww1.sinaimg.cn/large/6ab93b35gy1g1190tbeeoj20lv0fmmxh.jpg)

- `prototype`属性包含了一个指针,这个指针指向了原型对象(其包含了所有实例共有的方法和属性). 

	具体点的例子就是 `Person.prototype` 指向的原型对象就是 上面输出的内容

## 4.3 总结

1. 对象都拥有`__proto__`属性,该属性指向构造该对象的构造函数的原型对象

2. 方法除了有属性`__proto__`,还拥有`prototype`属性,该属性指向了该方法的原型对象



# 5. 属性和方法介绍

## 5.1 create()

	var person2 = Object.create(person1);

- `create()` 实际做的是从指定原型对象创建一个新的对象。这里以 person1 为原型对象创建了 person2 对象
	
		person2.__proto__ // 返回p1!!

## 5.2 原型对象的constructor属性

原型对象的`constructor`属性指向了用于构造 指向该原型对象的 实例的构造函数

1. 可以借助这个`constructor`属性来创建另一个实例.

		var p3 = new p1.constructor('Jack','chen',22,['a']);

	- 通常你不会去用这种方法创建新的实例；但如果你刚好因为某些原因没有原始构造器的引用,就可以用这种方法

2. 借助`constructor`属性,可以获取到构造函数的名称

		p1.constructor.name // Person


# 6. 原型对象(prototype)的应用

一种极其常见的对象定义模式是，**在构造器（函数体）中定义属性、在 prototype 属性上定义方法**。如此，构造器只包含属性定义，而方法则分装在不同的代码块，代码更具可读性

	// 构造器及其属性定义
	
	function Test(a,b,c,d) {
	  // 属性定义
	};
	
	// 定义第一个方法
	
	Test.prototype.x = function () { ... }
	
	// 定义第二个方法
	
	Test.prototype.y = function () { ... }