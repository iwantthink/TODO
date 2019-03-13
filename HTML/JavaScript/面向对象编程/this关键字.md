# this 关键字

[this 关键字- 阮一峰](https://wangdoc.com/javascript/oop/this.html)


# 1. this关键字的含义

`this`可以用在构造函数之中，表示实例对象。除此之外，`this`还可以用在别的场合。但不管是什么场合，`this`都有一个共同点：**它总是返回一个对象**。

- 简单的说:`this`就是属性或方法"当前"所属的对象

		this.property // this 就代表了property 属性当前所在的对象

**JavaScript 语言之中，一切皆对象，运行环境也是对象，所以函数都是在某个对象之中运行**，**`this`就是函数运行时所在的对象（环境）**。

- **JavaScript 支持运行环境动态切换，也就是说，this的指向是动态的，没有办法事先确定到底指向哪个对象**




## 1.1 `this`就是属性或方法当前所属的对象

	var person = {
	  name: '张三',
	  describe: function () {
	    return '姓名：'+ this.name;
	  }
	};
	
	person.describe() // 姓名: 张三

- `this.name`表示`name`属性所在的那个对象。

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

- `this`指向了顶层对象


## 1.4 网页变成实例

	<input type="text" name="age" size=3 onChange="validate(this, 18, 99);">
	
	<script>
	function validate(obj, lowval, hival){
	  if ((obj.value < lowval) || (obj.value > hival))
	    console.log('Invalid Value!');
	}
	</script>

- `this`代表了当前对象(即文本框)

# 2. `this`的实质