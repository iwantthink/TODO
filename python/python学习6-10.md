# 6. 模块

在Python中，一个`.py`文件就被称为一个模块(module)

使用模块可以避免函数名和变量名冲突，相同名字的函数和变量可以分别存在不同模块中。

**编写模块时，尽量不要与内置函数名冲突**

Python中存在**包**的概念，只要顶层的包名不与别人冲突，那所有的模块都不会冲突。例如加入包之后，`abc.py`的模块名 就会从`abc`变成`package.abc`

- **每一个包目录下必须存在一个`__init__.py`文件，只有存在 Python才会将这个目录当做包，而不是普通目录**。`__init__.py`文件可以为空，也可以存在Python代码，其模块名就是包名

**自定义模块时，模块名不允许和Python自带的模块名冲突，否则无法导入系统自带的模块**

**查看系统是否已存在该模块，检查方法是在Python交互环境执行import abc，若成功则说明系统存在此模块。**


## 6.1 使用模块

**Python模块的标准文件模板：**

	#!/usr/bin/env python3
	# -*- coding: utf-8 -*-
	
	`a test module`
	
	__author__ = 'ryan'

- 第1行注释可以让这个hello.py文件直接在Unix/Linux/Mac上运行，第2行注释表示.py文件本身使用标准UTF-8编码

- 任何模块**代码**的第一行字符串都会被视为模块的文档注释

- 可以不添加文件模板

**使用模块步骤:**

1. 导入模块，通过`import`关键字

2. 导入之后 **即可通过模块名调用模块内部功能**

	例如：`import sys`，就有了指向sys模块的`sys`变量，**`sys`模块有一个`argv`变量，用list存储了命令行的所有参数**。`argv`至少有一个元素，因为第一个元素永远是当前`.py`文件的名称

**在使用命令行运行模块文件时，Python解释器会将一个特殊变量`__name__`置为`__main__`,而在其他地方导入该模块时不会改变**。所以可以利用这一点写一些测试的代码

	if __name__=='__main__':
	    test()

	def test:
    	print('argv = %s'%module.argv[0])


## 6.2 作用域

Python中，**通过在函数和变量前添加`_`前缀来控制作用域**

1. 正常的函数和变量名是公开的，可以直接被引用

2. 类似`__xx__`这样前后各有俩个`_`的变量是特殊变量，可以被直接引用，但是有特殊用途，例如`__author`,`__name__`,`__doc__`。自定义变量时尽量不冲突

3. 类似`_xxx`和`__xxx`这类的函数和变量名就是非公开的，不应该被直接引用，**（但是实际上Python并没有一种方法可以完全限制访问变量和函数）**

**总结：外部不需要引用的函数全部定义成private，只有外部需要引用的函数才定义为public。**

Python的作用域一共四种：

1. `L （Local）` 局部作用域
2. `E （Enclosing）` 闭包函数外的函数中
3. `G （Global）` 全局作用域
4. `B （Built-in）` 内建作用域

- 以L->E->G->B的规则查找

Python中只有模块(module),类(class)以及函数(def,lambda)才会引入新的作用域。

**当内部作用域想修改外部作用域的变量时，需要使用`global`和`nonlocal`关键字**

- `global`,使用情况：

		num = 1
		def fun1():
		    global num  # 需要使用 global 关键字声明
		    print(num) 
		    num = 123
		    print(num)
		fun1()

- `nonlocal`,如果要修改嵌套作用域（enclosing 作用域，外层非全局作用域）中的变量则需要 nonlocal 关键字了，如下实例：



		def outer():
		    num = 10
		    def inner():
		        nonlocal num   # nonlocal关键字声明
		        num = 100
		        print(num)
		    inner()
		    print(num)
		outer()


## 6.3 import

在 python 用 `import` 或者 `from...import` 来导入相应的模块。

将整个模块(somemodule)导入，格式为： `import somemodule`

从某个模块中导入某个函数,格式为： `from somemodule import somefunction`

从某个模块中导入多个函数,格式为： `from somemodule import firstfunc, secondfunc, thirdfunc`

将某个模块中的全部函数导入，格式为： `from somemodule import *`

**使用`import ..`导入模块之后，通过模块名+函数名 进行函数调用。使用`from ... import ... `导入指定模块的指定函数之后，可以直接通过 函数名进行函数调用**

## 6.4 安装第三方模块

在Python中，安装第三方模块，是通过包管理工具`pip`完成的。

一般来说，第三方库都会在Python官方的`pypi.python.org`网站注册，要安装一个第三方库，必须先知道该库的名称，可以在官网或者pypi上搜索，比如Pillow的名称叫Pillow，因此，安装Pillow的命令就是：

	pip install Pillow

在使用Python时，我们经常需要用到很多第三方库，例如，上面提到的Pillow，以及MySQL驱动程序，Web框架Flask，科学计算Numpy等。用pip一个一个安装费时费力，还需要考虑兼容性。**推荐直接使用`Anaconda`**，这是一个基于Python的数据处理和科学计算平台，它已经内置了许多非常有用的第三方库，装上`Anaconda`，就相当于把数十个第三方模块自动安装好了，非常简单易用。

**模块搜索路径：**

当我们添加一个模块时，默认情况下Python解释器会搜索**当前目录、所有已安装的内置模块和第三方模块**，搜索路径存放在sys模块的path变量中：

	['', 'E:\\python\\python35.zip', 'E:\\python\\DLLs', 'E:\\python\\lib', 'E:\\python', 'E:\\python\\lib\\site-packages']

**如果要添加自己的搜索目录，有俩种方式：**

1. 直接修改`sys.path`。这种方式是在运行时修改，运行结束之后失效!

	sys.path.append('E:.....')

2. 设置环境变量`PYTHONPATH`,该环境变量的内容会被自动添加到模块搜索路径中，设置方式与PATH环境变量相似

# 7 面向对象编程
`Object Oriented Programming`，简称OOP，是一种程序设计思想

面向过程的程序设计把计算机程序视为一系列的命令集合，即一组函数的顺序执行。为了简化程序设计，面向过程把函数继续切分为子函数，即把大块函数通过切割成小块函数来降低系统的复杂度。

而面向对象的程序设计把计算机程序视为一组对象的集合，而每个对象都可以接收其他对象发过来的消息，并处理这些消息，计算机程序的执行就是一系列消息在各个对象之间传递。

在Python中，所有数据类型都可以视为对象，也可以自定义对象。自定义的对象数据类型就是面向对象中的类(Class)的概念

面向对象的设计思想是抽象出Class，根据Class创建Instance。

面向对象的抽象程度又比函数要高，因为一个Class既包含数据，又包含操作数据的方法。

## 7.1 类和实例

Python中 定义类是通过`class`关键字，后面紧接着是类名(类名通常是大写开头的单词)，紧接着是`(object)`(表示类的父类，通常如果没有合适的父类，就会使用`object`,这是所有类最终都会继承的类)

**创建实例是通过`类名+()`的形式**

由于类可以起到模板的作用，因此，可以在创建实例的时候，把一些我们认为必须绑定的属性强制填写进去。通过定义一个特殊的`__init__`方法，在创建实例的时候，就把一些属性绑上去：

	class Student(object):
	
	    def __init__(self, name, score):
	        self.name = name
	        self.score = score

	jack = Student('jack',100)

- `__init__`方法的第一个参数永远是`self`,表示创建的实例本身,因此可以在`__init__`方法内部把各种属性绑定到`self`上

- 拥有了`__init__`方法之后，在创建实例时就需要把`__init__`方法中除了`self`之外的参数传入，`self`Python解释器会把实例变量传进去

- 构造方法和普通方法在Python中只有一点不同，就是第一个参数永远是实例变量`self`,并且调用此构造方法时不需要传递该参数

### 7.1.1 数据封装

实例本身拥有一些属性数据，要访问这些数据，没有必要通过外面的函数去访问，可以直接在类的内部定义访问属性数据的函数，**这些封装数据的函数是和类本身关联的，称之为类的方法**

**Python中定义一个方法，其第一个参数必须要是`self`,其他和普通函数一样**，要调用一个方法只需要传入除`self`外的参数即可。简而言之，方法就是与实例绑定的函数，和普通函数不同的是，方法可以直接访问实例的数据

封装是好处是不用知道具体的实现细节，另外一个好处是可以给类添加新的方法

**与静态语言不同，Python允许对实例变量绑定任何数据，也就是说，对于俩个实例变量，虽然它们都是同一个类，但拥有的变量名称都可能不同**

## 7.2 访问限制

**在Python中如果需要让内部属性不被外部访问，可以将属性的名称前加上俩个下划线`__`,这样属性就会变成私有的`private`,只有内部可以访问，外部无法访问**

**在Python中，有一类特殊变量，形式如同`__xxx__`,以俩个下划线开头并且以俩个下划线结尾。这种特殊变量是可以直接访问的，不是私有的**

有时候遇到一个下划线开头的实例变量名，例如`_name`.这种不是私有变量，外部也是可以访问的，但是按照约定俗称的规定，即使可以访问，也需要当做私有变量对待。

实际上双下划线开头的实例变量也可以从外部访问，Python解释器会将`__属性名`变量名称改成`_类名__属性名`，所以可以通过`_类名__属性名访问`。**不同版本的解释器可能会改变属性名**

**有一种错误写法需要避免：**

	>>> bart = Student('Bart Simpson', 59)
	>>> bart.get_name()
	'Bart Simpson'
	>>> bart.__name = 'New Name' # 设置__name变量！
	>>> bart.__name
	'New Name'

- 这种写法实际上是给类新增加了一个`__name`属性，其内部的`__name`属性已经被解释器改变成了`_Student__name`

## 7.3 继承和多态

OOP程序设计中，定义一个类可以继承自某个现有的class

继承的好处是子类可以获得父类的全部功能，另外还可以重写这些父类的方法

在继承关系中，某个类的子类，其数据类型也可以被视为父类，反之不行！

**多态：允许将子类类型的指针赋值给父类类型的指针**。

对于一个变量，只需要知道其父类，无需知道确切的子类型就可以调用方法，至于具体的方法内容由运行该对象的确切类型决定。**即调用方只管调用，不管细节**

### 7.3.1 静态语言vs 动态语言

对于静态语言来说(如java),如果需要传入指定类型，那么传入的对象必须是这个类型或是这个类型的子类，否则无法使用

对于Python来说这种动态语言来说，并不严格的要求继承体系，一个对象只要“看起来像鸭子，走起路来像鸭子，那它就可以被当做鸭子”。**即只要一个类 拥有了指定父类 中的指定方法，那么这个类可以不是 指定父类的继承体系中的一部分！**

动态语言的鸭子类型特点决定了继承不想静态语言那样必须

### 7.3.2 super()

**`super()`函数用于获取传入type的父类并返回该父类实例**

- `super()` 是用来解决多重继承问题的，直接用类名调用父类方法在使用单继承的时候没问题，但是如果使用多继承，会涉及到查找顺序（MRO）、重复调用（钻石继承）等种种问题。

	MRO 就是类的方法解析顺序表, 其实也就是继承父类方法时的顺序表。

**语法：** `super(type,[object-or-type])`

- `type`: 类，获取该类的父类

- `object-or-type`: 类，一般是self


## 7.4 获取对象信息

通过`type()`方法 获取对象类型，即对应的Class类型
	# 判断基本数据类型可以直接写`int,str,float`等
	>>> type(123)==int
	>>> type(1.0) == float
	>>> type('str') == str

可以通过`types`模块中定义的常量 来判断对象是否是函数

	>>> import types
	>>> def fn():
	...     pass
	...
	>>> type(fn)==types.FunctionType
	True
	>>> type(abs)==types.BuiltinFunctionType
	True
	>>> type(lambda x: x)==types.LambdaType
	True
	>>> type((x for x in range(10)))==types.GeneratorType
	True


使用`isinstance()`可以判断一个对象是否是该类型本身，或者位于该类型的父继承链上。另外还可以判断一个变量是否是某些类型中的一种

	>>> isinstance([1,2],(tuple,list))
	True

**优先使用`isinstance()`判断类型**


通过`dir()`函数可以获取一个对象所有属性和方法，它返回一个包含字符串的list

	>>> dir('a')
	['__add__', '__class__', '__contains__', '__delattr__', '__dir__', '__doc__', '__eq__', '__format__', '__ge__', '__getattribute__', '__getitem__', '__getnewargs__', '__gt__', '__hash__', '__init__', '__iter__', '__le__', '__len__', '__lt__', '__mod__', '__mul__', '__ne__', '__new__', '__reduce__', '__reduce_ex__', '__repr__', '__rmod__', '__rmul__', '__setattr__', '__sizeof__', '__str__', '__subclasshook__', 'capitalize', 'casefold', 'center', 'count', 'encode', 'endswith', 'expandtabs', 'find', 'format', 'format_map', 'index', 'isalnum', 'isalpha', 'isdecimal', 'isdigit', 'isidentifier', 'islower', 'isnumeric', 'isprintable', 'isspace', 'istitle', 'isupper', 'join', 'ljust', 'lower', 'lstrip', 'maketrans', 'partition', 'replace', 'rfind', 'rindex', 'rjust', 'rpartition', 'rsplit', 'rstrip', 'split', 'splitlines', 'startswith', 'strip', 'swapcase', 'title', 'translate', 'upper', 'zfill']

- 类似`__xxx__`的属性和方法在Python是有特殊用途的，例如`__len__`方法返回长度，`len()`函数获取一个对象的长度，实际上函数内部是通过调用对象的`__len__()`方法获取的

	    # 俩者是相同的
		>>>len('ABC')=='ABC'.__len__()


**通过`getattr(),setattr(),hasattr()`**可以直接操作一个对象的状态(属性和方法)

	>>> hasattr(obj, 'x') # 有属性'x'吗？
	True
	>>> obj.x
	9
	>>> hasattr(obj, 'y') # 有属性'y'吗？
	False
	>>> setattr(obj, 'y', 19) # 设置一个属性'y'
	>>> hasattr(obj, 'y') # 有属性'y'吗？
	True
	>>> getattr(obj, 'y') # 获取属性'y'
	19
	>>> obj.y # 获取属性'y'
	19

- 如果获取不存在的属性，会抛出`AttributeError`错误

- 可以传入一个默认值，当属性不存在时，返回默认值

		>>> getattr(obj, 'z', 404) # 获取属性'z'，如果不存在，返回默认值404
		404


用法实例：

	def readImage(fp):
	    if hasattr(fp, 'read'):
	        return readData(fp)
	    return None

- 假设我们希望从文件流fp中读取图像，首先要判断该fp对象是否存在read方法，如果存在，则该对象是一个流，如果不存在，则无法读取。hasattr()就派上了用场。

## 7.5实例属性和类属性

**Python是动态语言，根据类创建的实例可以任意绑定属性**。给实例绑定属性的方法是通过实例变量，或者通过self变量：

	class Student(object):
	    def __init__(self, name):
	        self.name = name
	
	s = Student('Bob')
	s.score = 90

在class中直接定义的属性被称之为 类属性，这个属性归类所有，但是所有的实例都可以访问到

	class Student(objcet):
    	name = 'Student'

实例属性优先级别类属性高，所以如果同时存在时，会优先取实例属性

	>>>s = Student()
	>>>print(s.name) # 实例并没有name属性，所以会查找class中的属性
	>>>s.name = 'jack' # 给实例绑定name属性
	>>>s.name =='jack' # 实例属性 和 类属性同时存在
	>>>print(Student.name) # 类属性不会被实例属性覆盖
	>>>del s.name # 如果删除了 实例属性，就会使用类属性

# 8 面对对象高级编程

## 8.1 使用`__slots__`
Python作为动态语言，可以使用动态绑定，即正常情况下，定义了一个class之后，可以程序运行过程中给实例绑定任何属性和方法。

	# 定义类
	class Student(object):
	    pass
	# 绑定属性
	>>> s = Student()
	>>> s.name = 'Michael' # 动态给实例绑定一个属性
	>>> print(s.name)
	Michael
	# 绑定方法
	>>> def set_age(self, age): # 定义一个函数作为实例方法
	...     self.age = age
	...
	>>> from types import MethodType
	>>> s.set_age = MethodType(set_age, s) # 给实例绑定一个方法
	>>> s.set_age(25) # 调用实例方法
	>>> s.age # 测试结果
	25

- 对一个实例绑定的属性和方法，在另外一个实例是不起作用的。**为了能给所有实例都绑定属性或方法，可以给class绑定**

		>>> def set_score(self, score):
		...     self.score = score
		...
		>>> Student.set_score = set_score


**定义class时，可以通过使用`__slots__`变量限制该class实例能够添加的属性**
	
	class Student(object):
	    __slots__ = ('name', 'age') # 用tuple定义允许绑定的属性名称

- **`__slots__`仅对当前类的实例起作用，除非在子类中也定义`__slots__`，否则对继承的子类是不起作用的**。

- **当子类和父类同时存在`__slots__`时，子类实例就会被子类和父类中的`__slots__`共同作用！**

## 8.2 使用`@property`

Python内置的`@property`装饰器可以实现把一个get方法当做属性来调用。`@property`本身会创建另外一个装饰器`方法名.setter`,把一个set方法变成属性赋值

	class Student(object):
	
	    @property
	    def score(self):
	        return self._score
	
	    @score.setter
	    def score(self, value):
	        if not isinstance(value, int):
	            raise ValueError('score must be an integer!')
	        if value < 0 or value > 100:
	            raise ValueError('score must between 0 ~ 100!')
	        self._score = value


## 8.3 多重继承

Python支持多重继承，只需要在定义时的`()`中填写多个父类即可，这样子类可以同时获得多个父类的所有功能


在设计类的继承关系时，通常都是单一继承下来的，但是如果需要加入额外的功能，通过多重继承即可实现。**这种设计通常称之为MixIn**

	class Dog(Mammal, RunnableMixIn, CarnivorousMixIn):
	    pass

## 8.4 定制类

形如`__xxx__`的变量或函数名可以帮助定制类，例如`__slots__`变量用来限制实例添加变量，`__len__()`函数让class能够被`len()`函数作用


`__str__()`方法是返回用户看到的字符串，`__repr__()`是返回开发者看到的字符串，即为调试服务。所以可以重写这俩个方法，以实现自定义输出内容

	# 重写 __str__
	>>> class Student(object):
	...     def __init__(self, name):
	...         self.name = name
	...     def __str__(self):
	...         return 'Student object (name: %s)' % self.name
	...
	>>> print(Student('Michael'))
	Student object (name: Michael)
	# 直接输出 s，会调用__repr__()
	>>> s = Student('Michael')
	>>> s
	<__main__.Student object at 0x109afb310>
	# 重写 __repr__
	class Student(object):
	    def __init__(self, name):
	        self.name = name
	    def __str__(self):
	        return 'Student object (name=%s)' % self.name
	    __repr__ = __str__

**一个类如果想被`for...in ..`循环，就必须实现`__iter__(),__next__`方法**

- `__iter__()`方法返回一个迭代对象，然后Python会调用其`__next__`方法获取循环的下一个值，直到遇到`StopIteration`错误时退出循环

		class Fib(object):
		    def __init__(self):
		        self.a, self.b = 0, 1 # 初始化两个计数器a，b
		
		    def __iter__(self):
		        return self # 实例本身就是迭代对象，故返回自己
		
		    def __next__(self):
		        self.a, self.b = self.b, self.a + self.b # 计算下一个值
		        if self.a > 100000: # 退出循环的条件
		            raise StopIteration()
		        return self.a # 返回下一个值


**`__iter__()`方法只能让类可以作用于for循环，但是无法当做list来使用，实现`__getitem__()`方法之后允许类按下标取元素**

	class Fib(object):
	    def __getitem__(self, n):
	        a, b = 1, 1
	        for x in range(n):
	            a, b = b, a + b
	        return a

- 要让类实现切片，就需要对`__getitem__()`方法参数进行判断，传入的参数是索引或切片，然后做对应的处理
		
		class Fib(object):
		    def __getitem__(self, n):
		        if isinstance(n, int): # n是索引
		            a, b = 1, 1
		            for x in range(n):
		                a, b = b, a + b
		            return a
		        if isinstance(n, slice): # n是切片
		            start = n.start
		            stop = n.stop
		            if start is None:
		                start = 0
		            a, b = 1, 1
		            L = []
		            for x in range(stop):
		                if x >= start:
		                    L.append(a)
		                a, b = b, a + b
		            return L

- 要让类实现step(`l[:10:2]`)，必须在`__getitem__()`处理。要正确实现一个`__getitem__()`需要很多的工作去做。**归功于动态语言的`鸭子类型`，不需要强制继承某个接口，完全可以将自定义类表现的像Python自带的list,tuple,dic一样**

- `__setitem__()`用来实现赋值，`__delitem__()`用来实现删除元素


实现`__getattr__()`方法，可以避免在调用不能存在的类的方法或属性时 出现错误

- 当调用不存在的属性时，Python解释器会试图调用`__getattr__(self,属性名)`来尝试获取属性

		>>> s = Student()
		>>> s.name
		'Michael'
		>>> s.score #score属性不存在
		99

- `__getattr__()`方法中，返回函数也是可以的

		class Student(object):
		
		    def __getattr__(self, attr):
		        if attr=='age':
		            return lambda: 25
		# 调用方式 也要从 变量 改成 函数
		>>> s.age()
		25

- 实现了`__getattr__()`方法之后，即使出现 未在方法中的 属性或方法，也不会报错，只会返回`None`。如果要约束class仅响应个别属性，就需要抛出`AttributeError`错误

		class Student(object):
		
		    def __getattr__(self, attr):
		        if attr=='age':
		            return lambda: 25
		        raise AttributeError('\'Student\' object has no attribute \'%s\'' % attr)


`__call__()`方法可以实现 直接对实例进行调用

	>>>s = Stu()
	>>>s
	__main__.Stu2 object at 0x0000020F2CCC2978>
	
	class Student(object):
	    def __init__(self, name):
	        self.name = name
	
	    def __call__(self):
	        print('My name is %s.' % self.name)
	
	>>> s()
	My name is Michael.

- `__call__()`方法还可以定义参数，对实例进行直接调用跟函数一样

- **判断对象是否是一个可被调用的对象，通过判断类型是否为`Callable`实现。Python提供了`callable()`函数来判断**

## 8.5 使用枚举类

**Python提供`Enum`类,其可以把一组相关联的常量定义在一个class中，且class不可变，同时成员可以直接比较**
	
	from enum import Enum
	
	Month = Enum('Month', ('Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'))
	
	
	for name, member in Month.__members__.items():
	    print(name, '=>', member, ',', member.value)

- value属性是自动赋给成员的`int`常量，默认从`1`开始

如果需要更精确的控制枚举类型，可以从`Enum`派生自定义类

	from enum import Enum, unique
	
	@unique
	class Weekday(Enum):
	    Sun = 0 # Sun的value被设定为0
	    Mon = 1
	    Tue = 2
	    Wed = 3
	    Thu = 4
	    Fri = 5
	    Sat = 6

- `@unique`装饰器可以保证没有重复值


	>>> day1 = Weekday.Mon
	>>> print(day1)
	Weekday.Mon
	>>> print(Weekday.Tue)
	Weekday.Tue
	>>> print(Weekday['Tue'])
	Weekday.Tue
	>>> print(Weekday.Tue.value)
	2
	>>> print(day1 == Weekday.Mon)
	True
	>>> print(day1 == Weekday.Tue)
	False
	>>> print(Weekday(1))
	Weekday.Mon
	>>> print(day1 == Weekday(1))
	True
	>>> Weekday(7)
	Traceback (most recent call last):
	  ...
	ValueError: 7 is not a valid Weekday
	>>> for name, member in Weekday.__members__.items():
	...     print(name, '=>', member)
	...
	Sun => Weekday.Sun
	Mon => Weekday.Mon
	Tue => Weekday.Tue
	Wed => Weekday.Wed
	Thu => Weekday.Thu
	Fri => Weekday.Fri
	Sat => Weekday.Sat

## 8.6 使用元类

动态语言和静态语言最大的不同是函数和类的定义不是在编译时定义的，而是运行时动态创建的

**例如**：定义一个`Hello`的class，将其放到`hello.py`模块
	
	class Hello(object):
	    def hello(self, name='world'):
	        print('Hello, %s.' % name)

**当Python解释器载入`hello`模块时，会依次执行该模块的所有语句，执行结果就是动态创建一个`Hello`的class对象**


	>>> from hello import Hello
	>>> h = Hello()
	>>> h.hello()
	Hello, world.
	>>> print(type(Hello))
	<class 'type'>
	>>> print(type(h))
	<class 'hello.Hello'>

- `type()`函数可以查看一个类或变量的类型，**`Hello`是一个class，它的类型就是`type`**.而`h`是一个实例，它的类型就是class`Hello`

**动态语言函数和类的定义是运行时动态创建的，其创建class的方法就是`type()`函数，`type()`函数既可以返回一个对象的类型，也可以创建出新的类**

举例通过`type()`函数创建指定的类：

		>>> def fn(self, name='world'): # 先定义函数
		...     print('Hello, %s.' % name)
		...
		>>> Hello = type('Hello', (object,), dict(hello=fn)) # 创建Hello class
		>>> h = Hello()
		>>> h.hello()
		Hello, world.
		>>> print(type(Hello))
		<class 'type'>
		>>> print(type(h))
		<class '__main__.Hello'>

- 创建一个class对象，`type()`函数依次传入三个参数

	1. class的名称
	2. 继承的父类集合(Python支持多重继承，集合使用tuple,需要注意tuple只存在一个元素时的写法)
	3. class的方法名称与对应函数绑定

- 通过`type()`函数创建的类和直接写class是完全一样的，因为Python解释器在遇到class定义时，就是扫描一下class定义的语法，然后调用`type()`函数创建出class

- **动态语言本身支持运行期动态创建类**

### 8.6.1 metaclass
要控制类的创建行为，除了使用`type()`动态创建类之外，还可以使用`metaclass`

**`metaclass`译为元类。定义`metaclass`，可以创建类，然后根据创建好的类，可以创建实例。换句话就是说：类可以看成是由`metaclass`创建出来的实例**

`metaclass`允许创建类或修改类。

举例：利用`metaclass`给自定义类添加方法

	# metaclass是类的模板，所以必须从`type`类型派生：
	class ListMetaclass(type):
	    def __new__(cls, name, bases, attrs):
	        attrs['add'] = lambda self, value: self.append(value)
	        return type.__new__(cls, name, bases, attrs)
	# MyList是被修改的类
	class MyList(list, metaclass=ListMetaclass):
	    pass

- Python解释器在扫描到`metaclass`关键字之后，会在通过`__new__()`去创建类

- `__new__()`方法接收到的参数依次是：

	1. 当前准备创建的类的对象
	2. 类的名字
	3. 类继承的父类集合
	4. 类的方法或属性的集合


大多数情况下，直接在类中添加方法 比 通过`metaclass`添加方法 方便的多，但是也有特殊情况，例如`ORM(Object Relational Mapping)`，即对象-关系映射，就是把关系数据库的一行映射为一个对象，也就是一个类对应一个表，这样写代码更方便，不用直接操作SQL语句


#9 错误,调试和测试

## 9.1 错误处理

Python有俩种错误：语法错误和异常

- 语法错误：

		>>> while True print('Hello world')
		  File "<stdin>", line 1, in ?
		    while True print('Hello world')
		                   ^
		SyntaxError: invalid syntax

- 异常：

	即运行期监测到的错误，大多数异常都不会被程序处理，都以错误信息的形式展现

		>>> 10 * (1/0)
		Traceback (most recent call last):
		  File "<stdin>", line 1, in ?
		ZeroDivisionError: division by zero

	**异常处理：Python内置`try...except...finally...`错误处理机制**
		
		>>> while True:
		        try:
		            x = int(input("Please enter a number: "))
		            break
		        except ValueError:
		            print("Oops!  That was no valid number.  Try again   ")

	- try语句中出现了异常，会去except中去匹配，若匹配则执行，若不匹配，这个异常会传递给上层的try

	- 一个try语句可以包含多个except匹配，分别处理不同的异常

	- 一个except可以同时处理多个异常，这些异常需要被放在一个tuple中

		  except (RuntimeError, TypeError, NameError):

	- 最后一个except语句 可以忽略异常的名称，将会被当做通配符使用，可以在这里打印信息，或再次抛出异常
	
			except:
			    print("Unexpected error:", sys.exc_info()[0])
			    raise

	**try except语句还有一个可选的`else`语句，其必须被放在所有except语句之后，这个语句将会在没有发生任何异常的时候执行**

			for arg in sys.argv[1:]:
			    try:
			        f = open(arg, 'r')
			    except IOError as i:
			        print('cannot open', arg)
			    else:
			        print(arg, 'has', len(f.readlines()), 'lines')
			        f.close()

	**异常处理不仅仅可以处理直接发生在try语句中的异常，还能处理调用函数里抛出的异常（间接调用的也行）**

	**Python中所有的错误类型都是继承自`BaseException`**

	**Python内置`logging`模块可以快速打印错误信息**

			def main():
			    try:
			        bar('0')
			    except Exception as e:
			        logging.exception(e)

### 9.1.1 抛出异常

Python使用`raise`语句抛出一个指定的异常

	>>> raise NameError('HiThere')
	Traceback (most recent call last):
	  File "<stdin>", line 1, in ?
	NameError: HiThere

`raise`唯一的一个参数指定了要被抛出的异常。它必须是一个异常的实例或者是异常的类（也就是 Exception 的子类）。

**`raise`语句在不带参数时，会将当前错误原样抛出**

	>>> try:
	        raise NameError('HiThere')
	    except NameError:
	        print('An exception flew by!')
	        raise
   
### 9.1.2 自定义异常

异常类需要直接或间接的继承自`Exception`类

	>>> class MyError(Exception):
	        def __init__(self, value):
	            self.value = value
	        def __str__(self):
	            return repr(self.value)
	   
**大多数异常的名字都以`Error`结尾，这与标准的异常命名一样**

当创建一个模块有可能抛出多种不同的异常时，一种通常的做法是为这个包建立一个基础异常类，然后基于这个基础类为不同的错误情况创建不同的子类

### 9.1.3 定义清理行为

异常处理机制有一个可选的语句`finally`,它定义了无论在任何情况下都会执行的清理行为

	>>> try:
	...     raise KeyboardInterrupt
	... finally:
	...     print('Goodbye, world!')

- 无论`try`语句中有没有发生异常，`finally`语句都会执行

- 如果一个异常在`try`语句(或者是`except语句和else语句`)被抛出，又没有任何`except`与之匹配，那么这个异常会在`finally`语句执行之后再次被抛出

### 9.1.4 预定义的清理行为

关键词`with`可以保证诸如文件之类的对象在使用之后一定会正确的执行清理方法。

	with open("myfile.txt") as f:
	    for line in f:
	        print(line, end="")

- 无论代码是否出问题，文件f总会被关闭

- 更多查看`contextlib`13.7节

## 9.2 调试

调试可以通过`print()`不断打印信息

### 9.2.1 assert
调试通过`assert`断言可以进行调试,断言失败会抛出`AssertionError`

	def foo(s):
	    n = int(s)
	    assert n != 0, 'n is zero!'
	    return 10 / n
	
	def main():
	    foo('0')

Python可以使用 `-O`参数关闭`assert`,关闭之后`assert`语句可以当做`pass`处理

### 9.2.2 logging

通过`logging`模块，不过抛出错误，而且可以输出到文件

	import logging
	
	logging.basicConfig(level = logging.INFO)
	s = '0'
	n = int(s)
	logging.info('n = %d' % n)
	print(10 / n)

- `logging`允许指定输出大于等指定级别的信息，有`debug,info,warning,error`等级别，例如level = info ,就会忽略debug级别的信息

### 9.2.3 pdb

Python提供了调试器`pdb`,让程序以单步方式运行。

- 命令行中使用：

	通过命名行中添加参数`-m pdb`启动调试模式
	
	在调试模式中，输入命令`l`来查看代码，输入命令`n`进行单步执行，任何时候都可以输入命令`p 变量名`来查看变量的值，输入命令`q`结束调试

- 代码中使用：

	导入`pdb`模块，通过`pdb.set_trace()`设置断点，然后运行代码，程序会在断点处自动进入pdb调试模式

	通过命令`c`可以继续运行

		$ python err.py 
		> /Users/michael/Github/learn-python3/samples/debug/err.py(7)<module>()
		-> print(10 / n)
		(Pdb) p n
		0
		(Pdb) c
		Traceback (most recent call last):
		  File "err.py", line 7, in <module>
		    print(10 / n)
		ZeroDivisionError: division by zero

### 9.2.4 IDE

使用带有调试功能的IDE即可

[Visual Studio Code](https://code.visualstudio.com/)，需要安装Python插件。

[PyCharm](http://www.jetbrains.com/pycharm/)

另外，Eclipse加上pydev插件也可以调试Python程序。

## 9.3 单元测试

Python自带了`unittest`模块

编写单元测试时，需要新创建一个测试类，从`unittest.TestCase`继承，对于每一类测试都需要编写一个`test_xxx()`方法。由于`unittest.TestCase`提供了许多内置条件判断，调用这些方法即可断言输出期望值。

最常用的断言有`assertEqual()`判断值是否相等.`assertRaises`用来判断是否抛出指定异常

	self.assertEqual(abs(-1), 1) # 断言函数返回的结果与1相等
	
	with self.assertRaises(KeyError):
	    value = d['empty']

**运行单元测试：**

- 通过添加测试执行代码,然后直接运行测试脚本即可

		if __name__ == '__main__':
		    unittest.main()

- 在命令行添加参数`-m unittest`直接运行单元测试(推荐)

		$ python -m unittest mydict_test
		.....
		----------------------------------------------------------------------
		Ran 5 tests in 0.000s
		
		OK


`unittest`模块提供了`setUp()`,`tearDown()`俩个方法，会在每一个测试方法的执行前后被执行

## 9.4 文档测试

示例代码可以写在注释中，由一些工具来自动生成文档。这些代码代码本身就可以粘贴复制出来直接运行，也可以自动执行。。

	def abs(n):
	    '''
	    Function to get absolute value of number.
	
	    Example:
	
	    >>> abs(1)
	    1
	    >>> abs(-1)
	    1
	    >>> abs(0)
	    0
		>>> abs('a')
		Traceback (most recent call last):
			...
		TypeError
	    '''
	    return n if n >= 0 else (-n)

	if __name__=='__main__':
	    import doctest
	    doctest.testmod()

- Python内置的文档测试`doctest`模块可以直接提出注释中的代码并执行测试。`doctest`严格按照Python交互式命令行的输入和输出来判断测试结果是否正确。只有在测试异常的时候，可以用`...`表示中间一大段输出

		if __name__=='__main__':
	    import doctest
	    doctest.testmod()

# 10 IO编程

IO在计算机中指Input/Output,即输入和输出，由于程序和运行时数据是在内存中驻留，通过CPU执行，所以涉及到数据交换的地方，通常是磁盘，网络等，就需要IO接口

由于CPU和内存的速度远远高于外设的速度，所以，在IO编程中，就存在速度严重不匹配的问题。举个例子来说，比如要把100M的数据写入磁盘，CPU输出100M的数据只需要0.01秒，可是磁盘要接收这100M数据可能需要10秒。有两种办法：

- CPU等待，程序暂停执行后续代码，等待磁盘接收结束，再继续执行。**这种被称为同步IO**

- CPU不等待，在磁盘接收的同时 去执行后续代码。**这种模式被称为异步IO**

操作IO的能力都是由操作系统提供的，每一种编程语言都会把操作系统提供的低级C接口封装起来使用。

## 10.1 文件读写

Python内置了文件读写的函数，用法和C兼容

在磁盘上读写文件的功能都是由操作系统提供的，现代操作系统不允许普通的程序直接操作磁盘，所以，读写文件就是请求操作系统打开一个文件对象（通常称为文件描述符），然后，通过操作系统提供的接口从这个文件对象中读取数据（读文件），或者把数据写入这个文件对象（写文件）。

**读文件：**

Python内置`open()`函数，传入文件名和标识符，会获取到文件对象。

	>>> f = open('/Users/michael/test.txt', 'r')

- 如果文件不存在，会抛出一个`IOError`错误

- 标识符有 `r`,`w`等..具体查看官方文档即可

- 文件打开成功之后，可以调用`read()`方法 一次性读取文件的全部内容(`read()`函数有一个重载，可以传入读取的字节数量)，Python将内容读到内存，用一个`str`对象表示

- 文件使用完之后，需要调用`close()`函数关闭，因为文件对象会占用操作系统的资源，并且操作系统同一时间能打开的文件数量也是有限的

- 除了`read()`函数外，还有`readline()`可以每次读取一行内容，`readlines()`一次性读取所有内容并按行返回`list`


**`file-like Object`，只要存在`read()`方法的对象，在Python中统称为 `file-like Object`,除了File之外，还可以是内存的字节流，网络流，自定义流等等。这其实就是动态语言的鸭子类型所决定的**

`StringIO`就是在内存中创建的file-like Object，常用作临时缓冲。

**二进制文件**

- 要读取二进制文件，例如图片，视频等，需要使用标识符`b`模式打开文件

		>>> f = open('/Users/michael/test.jpg', 'rb')
		>>> f.read()
		b'\xff\xd8\xff\xe1\x00\x18Exif\x00\x00...' # 十六进制表示的字节

**字符编码**

- 默认读取文本文件都是以`UTF-8`编码，如果需要更改编码，可以给`open()`函数传入`encoding`参数

		>>> f = open('/Users/michael/gbk.txt', 'r', encoding='gbk')
		>>> f.read()
		'测试'

- 一些编码不规范的文件，可以会导致`UnicodeDecodeError`错误，因为其在文本文件中夹杂了一些非法编码字符。这种情况下，`open()`函数还可以传入`errors`参数，表示遇到编码错误如何处理

		>>> f = open('/Users/michael/gbk.txt', 'r', encoding='gbk', errors='ignore')

**写文件**

- 使用`open()`函数时传入`w`标识符，即进入了写文件模式，然后使用`write()`函数即可。默认读取文本文件，如果要读取二进制文件 则需要添加`b`标识符

		>>> f = open('/Users/michael/test.txt', 'w')
		>>> f.write('Hello, world!')
		>>> f.close()

- 当写文件时，操作系统不一定会立刻把数据写入磁盘，而是放到内存缓存起来，空闲时再写入。只有在调用了`close()`方法是，操作系统才保证把没有写入的数据全部写入磁盘

- `w`模式写文件时，如果文件已经存在，会直接覆盖(相当于删除后新创建一个文件)。可以通过`a`标识符 切换到 `append`追加模式。


## 10.2 StringIO 和BytesIO

`StringIO`就是在内存中读写str

- 要把str写入`StringIO`,需要先创建一个`StringIO`，然后像文件一样写入即可

		>>> from io import StringIO
		>>> f = StringIO()
		>>> f.write('hello')
		5
		>>> f.write(' ')
		1
		>>> f.write('world!')
		6
		>>> print(f.getvalue())
		hello world!
		
	- `getvalue()`方法用于获得写入后的str

- 要读取`StringIO`,可以用一个str初始化`StringIO`,然后像文件一样读取即可

		>>> from io import StringIO
		>>> f = StringIO('Hello!\nHi!\nGoodbye!')
		>>> while True:
		...     s = f.readline()
		...     if s == '':
		...         break
		...     print(s.strip())
		...
		Hello!
		Hi!
		Goodbye!


`BytesIO`就是在内存中操作二进制数据

- 要写入`BytesIO`,需要先创建一个`BytesIO`,然后写入`bytes`

		>>> from io import BytesIO
		>>> f = BytesIO()
		>>> f.write('中文'.encode('utf-8'))
		6
		>>> print(f.getvalue())
		b'\xe4\xb8\xad\xe6\x96\x87'

	- 写入的是经过`utf-8`编码的字节

- 要读取`BytesIO`，可以用`bytes`初始化`BytesIO`,然后像文件一样读取

		>>> from io import BytesIO
		>>> f = BytesIO(b'\xe4\xb8\xad\xe6\x96\x87')
		>>> f.read()
		b'\xe4\xb8\xad\xe6\x96\x87'

## 10.3 文件和目录

Python内置了`os`模块可以直接调用操作系统提供的接口函数，实现一些类似`dir`,`cp`的命令

	>>> import os
	>>> os.name # 操作系统类型
	'nt'

- 输出`posix`,说明系统是`Linux`,`Unix`,`Mac OS X`. 如果输出`nt`,表示系统为Windows

- **`os`模块的某些函数是跟操作系统有关的**


### 10.3.1 环境变量

在操作系统中定义的环境变量，全部保存在os.environ这个变量中，可以直接查看：

	>>> os.environ
	environ({'VERSIONER_PYTHON_PREFER_32_BIT': 'no', 'TERM_PROGRAM_VERSION': '326', 'LOGNAME': 'michael', 'USER': 'michael', 'PATH': '/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/opt/X11/bin:/usr/local/mysql/bin', ...})

- 要获取某个环境变量的值，可以调用`os.environ.get('key')`:

### 10.3.1 操作文件和目录

**操作文件和目录的函数一部分在`os`模块，一部分在`os.path`模块中**

	# 查看当前目录的绝对路径:
	>>> os.path.abspath('.')
	'/Users/michael'
	# 在某个目录下创建一个新目录，首先把新目录的完整路径表示出来:
	>>> os.path.join('/Users/michael', 'testdir')
	'/Users/michael/testdir'
	# 然后创建一个目录:
	>>> os.mkdir('/Users/michael/testdir')
	# 删掉一个目录:
	>>> os.rmdir('/Users/michael/testdir')


由于不同操作系统拥有不同的路径分隔符，所以在把两个路径合成一个时，不要直接拼字符串，而要通过`os.path.join()`函数

- 在`Linux/Unix/Mac`下，`os.path.join()`返回这样的字符串：

		part-1/part-2

- 而Windows下会返回这样的字符串：

		part-1\part-2

同样的道理 拆分路径时也不要直接拆字符串，而是通过`os.path.split()`函数。**这些合并，拆分路径的函数不要求目录和文件真实存在，它们只对字符串进行操作**

- 获取路径和文件名

		>>> os.path.split('/Users/michael/testdir/file.txt')
		('/Users/michael/testdir', 'file.txt')

- 直接获取文件扩展名

		>>> os.path.splitext('/path/to/file.txt')
		('/path/to/file', '.txt')

- 重命名和删除文件

		# 对文件重命名:
		>>> os.rename('test.txt', 'test.py')
		# 删掉文件:
		>>> os.remove('test.py')


**复制文件的函数在`os`模块中并不存在，因为复制文件并不是由操作系统提供的系统调用。**理论上可以通过读写文件完成文件复制，但是`shutil`模块已经提供了`copyfile()`函数，这个模块可以看做是`os`模块的补充


## 10.4 序列化

在程序运行的过程中，所有变量都是保存在内存中，一旦程序结束，变量所占用的内存就被操作系统全部回收，那么下次重新运行程序，变量会恢复初始值

**变量从内存中变成可存储或传输的过程称之为序列化，Python中叫做`pickling`,其他语言中也被称之为serialization，marshalling，flattening等等。反之，将变量内容从序列化的对象重新读到内存里称之为反序列化，即`unpickling`**

**Python提供了`pickle`模块来实现序列化**，序列化之后，可以将序列化之后的内容写入磁盘，或通过网络传输

- 序列化

		>>> import pickle
		>>> d = dict(name='Bob', age=20, score=88)
		>>> pickle.dumps(d)
		b'\x80\x03}q\x00(X\x03\x00\x00\x00ageq\x01K\x14X\x05\x00\x00\x00scoreq\x02KXX\x04\x00\x00\x00nameq\x03X\x03\x00\x00\x00Bobq\x04u.'
		# 写入文件
		>>> f = open('dump.txt', 'wb')
		>>> pickle.dump(d, f)
		>>> f.close()

	- `pickle.dumps()`函数可以将任意对象序列化成`bytes`,`pickle.dump()`函数可以直接把对象序列化之后下乳一个`file-like Object`

- 反序列化

		>>> f = open('dump.txt', 'rb')
		>>> d = pickle.load(f)
		>>> f.close()
		>>> d
		{'age': 20, 'score': 88, 'name': 'Bob'}

	- `pickle.loads()`函数可以反序列化出对象，`pickle.load()`函数可以从一个`file-like Object`对象中直接反序列化出对象

**序列化和反序列化之后的对象不再是同一个对象！它们只是内容相同！**

`pickle`模块的问题和所有其他编程语言特有的序列化问题一样，就是其只能作用于`Python`!而且不同版本的Python彼此都不兼容。

### 10.4.1 JSON

如果要在不同的编程语言之间传递对象，就必须把对象序列化为标准格式，比如XML，但更好的方法是序列化为JSON，因为JSON表示出来就是一个字符串，可以被所有语言读取，也可以方便地存储到磁盘或者通过网络传输。JSON不仅是标准格式，并且比XML更快，而且可以直接在Web页面中读取，非常方便。

`Json`表示的对象就是标准的`JavaScript`语言的对象，Json和Python内置的数据类型对应如下：

1. Json类型:`{}` Python类型：`dict`
2. Json类型:`[]` Python类型：`list`
3. Json类型:`string` Python类型：`str`
4. Json类型:`1234.56` Python类型：`int`or`float`
5. Json类型:`true/false` Python类型：`True/False`
6. Json类型:`null` Python类型：`None`

**Python内置`json`模块，提供了Python对象到`Json`格式的转换.Json标准规定Json编码是`UTF-8`**

- Python->Json

		>>> import json
		>>> d = dict(name='Bob', age=20, score=88)
		>>> json.dumps(d)
		'{"age": 20, "score": 88, "name": "Bob"}'

	- `json.dumps()`函数返回一个`str`,内容是标准的Json，`json.dump()`可以直接把Json 写入到一个`file-like Object`

- Json->Python

		>>> json_str = '{"age": 20, "score": 88, "name": "Bob"}'
		>>> json.loads(json_str)
		{'age': 20, 'score': 88, 'name': 'Bob'}

	- `json.loads()`函数可以将Json反序列化为Python对象。`json.load()`可以从`file-like Object`中读取字符串并反序列化

### 10.4.2 Json进阶

Python的`dict`对象可以直接序列化为Json的`{}`,但是有的时候，需要使用`class`表示对象，然后序列化。但是直接进行Json的序列化是不行的，因为默认情况下，`dumps()`函数不知道如何将`class`实例转换成一个Json的`{}`对象

**`dumps()`函数拥有一个可选参数`default`，用来指定转换函数。这个转换函数作用就是将 对象变成一个可序列化为Json的对象**

		def student2dict(std):
		    return {
		        'name': std.name,
		        'age': std.age,
		        'score': std.score
		    }
		
		>>> print(json.dumps(s, default=student2dict))
		{"age": 20, "name": "Bob", "score": 88}

- `Stu`类的实例会先通过`student2dict()`函数转换为`dict`,然后转换成Json

**如果不需要那么高的定制化，可以直接使用`__dict__`属性来实现转换函数，该属性通常`class`的实例都有，它就是一个用来存储实例变量的`dict`。当然也有例外，就是定义了`__slots__`的`class`**

	print(json.dumps(s, default=lambda obj: obj.__dict__))

**`class`实例的反序列化 也是同样的道理，需要编写一个`Json`->`Python`对象的转换函数**

	def dict2student(d):
	    return Student(d['name'], d['age'], d['score'])
	
	>>> json_str = '{"age": 20, "score": 88, "name": "Bob"}'
	>>> print(json.loads(json_str, object_hook=dict2student))
	<__main__.Student object at 0x10cd3c190>


