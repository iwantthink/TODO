# 1. 简介

## 1.1 解释器

编写Python代码是保存在一个`.py`扩展名的文本文件。解释器就是用来执行`.py`文件的工具

默认官方提供了一个`CPython`,即用c语言开发的解释器，在命令行中运行`python`就是启动CPython解释器

## 1.2 模式

- 命令行模式：

	在Windows开始菜单选择“命令提示符”，就进入到命令行模式，它的提示符类似`C:\>：`

- Python交互模式：

	在命令行模式下敲命令python，就看到类似如下的一堆文本输出，然后就进入到Python交互模式，它的提示符是`>>>`。

	可以通过`exit()`退出Python交互模式

- Python脚本模式：

	创建`.py`文件，在其中编写代码。使用`python xx.py`运行脚本。

## 1.3 直接运行.py文件

**在window上是不行的，但是在Linux和Mac上是可行的**

方法是在`.py`文件的第一行加上一个特殊的注释（指定解释器的路径）：

	#!/usr/bin/env python3
	
	print('hello, world')

  

## 1.4 输入/输出

**输出**：
- `print()`即可实现输出，可以通过符号逗号`,`隔开多个字符串从而输出一串字符串，逗号相当于一个空格

- print默认输出是换行的，可以在末尾添加`end=""`实现不换行

		>>> print('name','jack')
		>>> name jack
		>>> print('name',end = "")

**输入**：
- `input()`即可实现输入，等待输入时是阻塞的.`input()`可以传入字符串在执行时输出

**输入是Input，输出是Output，因此，我们把输入输出统称为Input/Output，或者简写为IO。**

# 2.基础

注释，解释器会忽略掉注释

- `#`符号开头的语句是单行注释

- 多行注释用三个单引号 或者 三个双引号 将注释包括起来

		''' 
		注释第一行
		注释第二行
		'''

**当语句末尾出现`:`符号时，该行之后出现的缩进的语句视为代码块**

- 按照约定，Pyhton中的缩进使用 **四个空格**
- 缩进相同的一组语句构成一个代码块，称之为代码组

Pyhton大小写敏感

## 2.1 数据类型

**Python中有留个标准数据类型：**
1. Number 数字:int,float,bool,complex(复数)
2. String 字符串:str
3. List 列表
4. Tuple 元组
5. Sets 集合
6. Dictionary 字典


### 2.1.1 Number
- 整数

	`0x`十六进制
	`0o`八进制(前者是数字零，后者是字母o)
	`0b`二进制

	Python对整数没有大小限制，不同于JAVA(32位整数范围在-2147483648-2147483647)

	可以使用`del`关键字 删除数字对象的引用

		>>> var1 = 123
		>>> del var1

-  浮点数

	浮点数即小数(因为科学计数法中一个浮点数的小数点位置是可变的，所以称为浮点数)

	浮点数可以使用数学写法`1.23 3.14 -0.96`,也可以使用科学计数法表示，即使用`e`来替换`10`,那么`1.23 * 10 ^10 `==`1.23 * e ^ 10`

	浮点数和整数在计算机内部存储方式不同，**整数运算永远是精确的,包括除法，浮点数会出现四舍五入**

	- 在Python中存在俩种除法

		1. `/`,计算结果是浮点数，即使是俩个整数恰好整除

				>>> 10/3
				3.3333333333335 

		2. `//`地板除，整数的地板除永远是整数，即只取整数部分。如果是浮点数，那只取浮点数的整数部分，小数部分用0替代

- 布尔值

	Python中布尔型通过`True`/`False`表示,它们是有值的 `True = 1 False = 0`,并且可以和数字相加

	布尔值可以通过 `and`,`or`,`not`进行运算

		>>> True and True
		True

### 2.1.2 字符串

**字符串是以`单引号 或 双引号`括起来的任意文本**

`\`反斜杠是转义字符,可以转义很多字符，`\n`表示换行，字符`\`本身也需要转义

Pyhton允许使用`r''`的语法来表示字符串中的内容默认不转义

Pyhton允许使用`'''...'''`的语法来表示多行内容,还可以和不转义的语法并用

字符串是不可变的

可以通过符号`*`对字符串进行复制，通过符号`+`对字符串进行连接

	>>> s = 'abc' * 2
	'abcabc'
	>>> s = 'a'+'b'
	'ab'

### 2.1.3 空值

空值在Python中是一个特殊的值 ，使用`None`进行表示。`None`不能理解为`0`,因为`0`是有意义的，而`None`是一个特殊的空值

### 2.1.4 变量

变量名必须是大小写英文、数字和`_`的组合，且不能用数字开头

Python中的变量不需要声明类型，但是每个变量在使用之前必须赋值，只有被赋值之后才会被创建

**这种变量本身类型不固定的语言称之为动态语言**，与之对应的是静态语言。静态语言在定义变量时必须指定变量类型，如果赋值的时候类型不匹配，就会报错。

**变量在内存中的表示：**

- 当编写`a = 'ABC'`时，Python解释器做了俩件事：
	
	1. 在内存中创建了一个`ABC`字符串
	2. 在内存中创建了一个名为`a`的变量,并把它指向`ABC`


可以将一个变量赋值给另一个变量，**这个操作实际上是将变量`a`指向变量`b`所指向的数据**

**Python允许同时为多个变量赋值，或者是同时为多个对象指定多个变量**
	
	a = b = c = 1
	a,b,c = 1,'str1',True

### 2.1.5 常量

在Python中通常使用全部大写的变量名表示变量，但是实际上常量就是变量。。也可以被改变，只是规定说不能被改变

## 2.2 字符编码

### 2.2.1 字符编码

计算机只能处理数字，所以要处理文本就必须先转换成数字。计算机采用8个比特(bit)作为一个字节(byte),所以一个字节能表示的最大的整数就是255，要表示更大的整数就必须使用更多的字节


计算机由美国人发明，因此最早只有127个字符被编码到计算机里，这个编码表被称为`ASCII`编码，但是世界上这么多种语言 仅用`ASCII`肯定是不够的，所以开发出了`Unicode`编码表。

`ASCII`编码与`Unicode`编码的区别是：前者是一个字节的，后者**通常是**俩个字节但是遇到特殊字符也可以使用更多字节

`ASCII`编码转成`Unicode`编码 只需要在前面添加0 即可

`Unicode`编码有一个缺点就是：如果书写的文本只是英文，那么占用的的存储空间会比`ASCII`大一倍

为了应付这种情况，出现了**可变长编码**`UTF-8`编码，UTF-8编码把一个Unicode字符根据不同的数字大小编码成1-6个字节，常用的英文字母被编码成1个字节，汉字通常是3个字节，只有很生僻的字符才会被编码成4-6个字节。如果你要传输的文本包含大量英文字符，用UTF-8编码就能节省空间。

`ASCII`编码实际就可以被看做是`UTF-8`编码的一部分

**计算机系统通用的编码工作方式：**

- 在计算机内存中，**统一使用Unicode编码**，当需要保存到硬盘或者需要传输的时候，就转换为UTF-8编码。


### 2.2.2 字符串中的编码

**在Python 3版本中，字符串使用`Unicode`编码，也就是说Python支持多语言**

对于单个字符的编码,Python提供了`ord()`函数获取字符的整数表示，`chr()`函数将编码转换为对应的字符

如果知道字符的整数编码，可以使用十六进制编写字符串

	>>> '\u4e2d\u6587'
	'中文'

由于Python的字符串类型是`str`，在内存中以Unicode表示，一个字符对应若干个字节。如果要在网络上传输，或者保存到磁盘上，就需要把`str`变为以字节为单位的`bytes`。

Python对bytes类型的数据用带b前缀的单引号或双引号表示：
	
	x = b'ABC'

要注意区分'ABC'和b'ABC'，前者是str，后者虽然内容显示得和前者一样，但bytes的每个字符都只占用一个字节。

以`Unicode`表示的str通过`encode()`函数可以编码为指定的`bytes`，例如：

	>>> 'ABC'.encode('ascii')
	b'ABC'

纯英文的str可以用ASCII编码为bytes，内容是一样的，含有中文的str可以用UTF-8编码为bytes。含有中文的str无法用ASCII编码，因为中文编码的范围超过了ASCII编码的范围，Python会报错。

在bytes中，无法显示为ASCII字符的字节，用\x##显示。

从网络或磁盘上读取了字节流，那么读到的数据就是`bytes`。要把bytes变为str，就需要用`decode()`方法：

	>>> b'ABC'.decode('ascii')
	'ABC'

如果`bytes`中包含无法解码的字节，会报错

如果bytes中只有一小部分无效的字节，可以传入errors='ignore'忽略错误的字节：

	>>> b'\xe4\xb8\xad\xff'.decode('utf-8', errors='ignore')
	'中'

- `len()`函数

	**	计算`str`的字符数，计算`bytes`的字节数**

Python源代码是一个文本文件，当源代码包含中文的时候，在包含中文时，必须制定保存为`UTF-8`编码(**这就需要通过设置ide使用的编码来指定**)，另外在Python解释器读取源代码时为了让它按照`UTF-8`编码读取时，可以在文件开头写上如下注释，前者表示在`Linux/Mac`环境下，这是一个可执行文件，win会忽略。 后者表示解释器需要按`UTF-8`编码读取源代码

	#!/usr/bin/env python3
	# -*- coding: utf-8 -*-

### 2.2.3 格式化

Python中的格式化 和c语言一致，使用`%`实现

	>>> 'Hello, %s' % 'world'
	'Hello, world'
	>>> 'Hi, %s, you have $%d.' % ('Michael', 1000000)
	'Hi, Michael, you have $1000000.'

`%`运算符就是用来格式化字符串的。在字符串内部，`%s`表示用字符串替换，`%d`表示用整数替换，`%f`表示浮点数替换,`%x`表示十六进制整数替换。有几个`%?`占位符，后面就跟几个变量或者值，顺序要对应好。如果只有一个`%?`，括号可以省略。

格式化整数和浮点数时还可以指定是否补0和整数与小数的位数

	print('%2d-%02d' % (3, 1))
	print('%.2f' % 3.1415926)

	 3-01 
	3.14 

`%s`可以将任何数据类型转换成字符串

在存在格式化的字符串中想单独使用`%`,需要进行转义`%%`


格式化还有另一种形式，使用`format()`函数，它会将传入的参数依次替换字符串内的占位符`{0},{1}`
	
	>>> 'Hello, {0}, 成绩提升了 {1:.1f}%'.format('小明', 17.125)
	'Hello, 小明, 成绩提升了 17.1%'

### 2.2.4 多行语句

Python通常是一行一条语句，但是如果语句很长，则可以通过 **反斜杠**(`\`)将一条语句拆分成多条

	total = item_one + \
	        item_two + \
	        item_three

在`[],{},()`中不需要使用反斜杠`\`即可换行
	
	total = ['item_one', 'item_two', 'item_three',
	        'item_four', 'item_five']

### 2.2.5 同一行显示多条语句
Python允许同一行中使用多条语句，语句之间使用分号`;`分隔

	import sys; x = 'abc';

## 2.3 list和tuple
### 2.3.1 list
Python内置的一种数据类型是`list`.这是一种有序的集合，可以随时添加和删除其中的元素

	>>> classmates = ['a','b','c']

使用`len()`函数可以获得元素个数，使用索引访问`list`中的每个元素，索引从`0`开始代表第一个，也可以使用负数`-1`代表最后一个,不允许越界.

- list函数
	- `append(obj)`将元素添加到`list`末尾
	- `insert(index,obj)`将元素插入到指定索引位置
	- `pop()`删除末尾元素,`pop(i)`删除指定索引位置元素，可以直接赋值指定索引位置的元素
	- `count(obj)`统计某个元素出现的次数
	- `extend()seq`:在列表末尾一次性追加另一个序列中的多个值（用新列表扩展原来的列表）


`list`中元素的数据类型可以不同，元素可以为另外一个`list`,如果`list`没有元素，就是空`list`,长度为0

`list`中的元素类型可以不相同，支持数字，字符串，列表(即嵌套)

### 2.3.2 tuple
`tuple`与`list`相似，但是一旦初始化就不能修改，即它没有`append()`,`insert()`等方法

	>>> tupp = ('a','b','c')

定义空tuple时，可以使用`t = ()`,但是定义只有一个元素的tuple 如果使用`t = (1)`这种形式，那么定义的不是tuple，而是`1`这个数字！因为`()`即可以表示tuple 也可以表示数学公式中的小括号，Python规定，这种情况按小括号进行计算，**所以当只有1个元素的tuple 必须添加一个`,`来消除歧义**,Python在显示只有一个元素的tuple时也会带上`,`

	>>> t = (1,)

`tuple`所谓的不变 指的是指向不变，如果`tuple`中保存有`list`,那这个`list`是可变的

`tuple`是可以进行连接组合操作的


## 2.4 条件判断

Python 中使用`if else`语句实现条件判断,`elif`(else if可以添加多个)可以添加更细致的判断。**注意`:`是必须的**

	if <条件判断1>:
	    <执行1>
	elif <条件判断2>:
	    <执行2>
	elif <条件判断3>:
	    <执行3>
	else:
	    <执行4>

`if`语句中的判断条件可以缩写，**填入非零数值，非空字符串，非空list等即代表`True`,否则为`False`**

**`input()`函数得到的是字符串类型**,可以通过`int()`函数进行转换，`int()`函数只允许转换合法的数字

**条件判断从上向下匹配，当满足条件时执行对应的块内语句，后续的elif和else都不再执行。**

## 2.5 循环

Python 提供俩种循环
1. 一种是`for...in `，依次将list或tuple中的每个元素迭代出来

		names =['jakc','Ryan','Bob']
		for name in names:
			print(name)

2. 另一种是`while`循环
		
		x = 10
		while(x <100):
			print(x)
			x=x+1

Python 提供了`range()`函数，用于生成一个整数序列，`list()`函数可以将序列转换成list
	
	>>> list(range(3))
	[0,1,2]


**在循环中使用`break`可以实现提前退出循环。`continue`可以实现跳过当前循环，开始下一次循环**

## 2.6 使用dict和set
### 2.6.1 dict
Python内置字典:`dict`。字典是无序的对象集合，在其他语言中也被称为`map`,结构是`key-value`,具有极快的查找顺序.

	dic = {'name':'jack','age':13}
	dic['name']

除了初始化设置值，还可以通过key 放入,**key不存在的话 会抛出异常**

	dic['age'] = 19

通过`in`关键字可以判断key是否存在，或者通过`get()`函数判断key,不存在即返回`None`或者自定义的值

	>>> 'name' in dic
	True
	>>> dic.get('name')
	>>> dic.get('xxx','default value')
	
**注意：返回None的时候Python的交互环境不显示结果。**

`pop(key)`可以删除指定key

**list和dict相比：**

- dict有以下几个特点（**`dict`是以空间换时间**）：

	查找和插入的速度极快，不会随着key的增加而变慢；
	需要占用大量的内存，内存浪费多。

- 而list相反：

	查找和插入的时间随着元素的增加而增加；
	占用空间小，浪费内存很少。



**注意：dict中的key必须是不可变对象,因为dict内部通过计算key的Hash值确定存储位置。所以list作为一个可变对象，不能充当`dict`的key**

### 2.6.2 set

集合`（set）`是一个无序不重复元素的序列。与`dict`一样无法使用可变对象作为key,基本功能是进行成员关系测试和删除重复元素。

可以使用大括号 `{ }` 或者 `set()` 函数创建集合，注意：创建一个空集合必须用 `set() `而不是 `{ }`，因为`{ }` 是用来创建一个空字典。

	>>> {1,2,3}
	>>> set([1,2,3])

通过`add(key)`函数进行添加操作，`remove(key)`函数进行删除操作

`set`可以看成数学意义上的无序和无重复元素的集合，因此，两个`set`可以做数学意义上的交集、并集等操作：

	>>> s1 = set([1, 2, 3])
	>>> s2 = set([2, 3, 4])
	>>> s1 & s2
	{2, 3}
	>>> s1 | s2
	{1, 2, 3, 4}

## 2.7 运算符

**成员运算符:**

- `in`表示能否在指定序列中找到值,返回True/False
- `not in `,与`in`的功能相似，返回值相反

		>>> l = [1,2,3]
		>>> 1 in l
		True


**身份运算符:**

- `is`或`is not`,用于比较俩个对象的存储单元，判断俩个标识符是否引用自一个对象
	
		>>> x,y = 1,1
		>>>x is y
		True
		>>> id(x) == id(y)
		True

	- `id()`函数用于获取对象内存地址

	- `is`和`==`的区别是，前者比较变量引用对象是否为同一个，后者用于判断引用变量的值是否相等

**逻辑运算符：**

- `and`,`x and y`进行布尔与运算，其他情况返回 y的计算值
- `or`，`x or y`进行布尔或运算，其他情况返回 y 的计算值
- `not`,`not x`进行布尔非运算

**位运算符：**

- `&` 与
- `|` 或
- `^` 异或
- `~` 取反
- `<<` 左移运算符
- `>>` 右移运算符

## 2.8 可变对象/不可变对象

在 Python 中，`str, tuple, 和 number` 是不可更改的对象，而 `list,dict` 等则是可以修改的对象。对于可变对象，其内部内容是会发生变化的

- 不可变类型：变量赋值 a=5 后再赋值 a=10，这里实际是新生成一个 int 值对象 10，再让 a 指向它，而 5 被丢弃，不是改变a的值，相当于新生成了a。

- 可变类型：变量赋值 la=[1,2,3,4] 后再赋值 la[2]=5 则是将 list la 的第三个元素值更改，本身la没有动，只是其内部的一部分值被修改了

**Python函数的参数传递：**

- 不可变类型：类似 c++ 的值传递，如 整数、字符串、元组。如fun（a），传递的只是a的值，没有影响a对象本身。比如在 fun（a）内部修改 a 的值，只是修改另一个复制的对象，不会影响 a 本身。

- 可变类型：类似 c++ 的引用传递，如 列表，字典。如 fun（la），则是将 la 真正的传过去，修改后fun外部的la也会受影响


**需要牢记一点，a 只是保存 字符串对象 的地址，而真正的字符串对象是'abc'**。对于任何不可变对象，调用对象自身的任意方法都不会改变对象自身的内容。这些方法会创建新的对象并返回！


# 3. 函数

Python可以自定义函数，同时也内置了许多函数，可以通过`help()`函数对一些变量或函数进行查看

[Python官网-函数文档](https://docs.python.org/3/library/functions.html#abs)

数据类型转换函数: `int(),float(),str(),bool(),tuple(),list(),dict(),set()`

函数名其实就是一个函数对象的引用，可以将函数名赋给一个变量，相当于给这个函数起了一个别名

## 3.1 定义函数

**Python中定义一个函数需要使用`def`关键字，依次写出函数名，括号，括号中的参数和冒号`:`,然后在缩进中编写函数体，函数的返回值用`return`关键字表明。**

	def 函数名（参数列表）:
	    函数体

- 如果没有`return`语句，函数执行完毕后就会返回结果，结果为`None`.`return None`可以简写为`return`

**可以通过`from filename import functionName`来导入指定保存在指定文件中的指定函数**


如果需要定义一个**空函数**,即什么都不做的函数，可以使用`pass`关键词，`pass`可以充当占位符，让函数先运行起来，**缺少了`pass`的话，代码运行会出现语法错误**

**可以使用`isinstance()`函数来进行参数类型检查**

函数可以同时返回多个值，`return x,y`.**实际上Python将多个参数组成一个tuple返回，同时在语法上，tuple可以被对应数量的变量接收，即按照位置赋给对应的值。**

	>>> def move():
			x = 1
			y = 2
			return x,y

	>>> q,r = move()
	>>> w = move()
	(1,2)
	
## 3.2 函数的参数

参数可以分为：**必选参数，默认参数，可变参数 ，关键字参数 和 命名关键字参数**

### 3.2.1 位置参数
即调用函数时，按参数传入时的位置顺序依次赋值给函数定义的参数.这种函数的参数就叫做位置参数


### 3.2.2 默认参数
即当调用者不传入这个参数时，会取函数在定义时设置的默认值

设置默认参数注意：

1. 必选参数在前,默认参数在后，否则解释器会报错
2. 当函数有多个参数时，把变化大的参数放前面，变化小的参数放后面。变化小的参数就可以作为默认参数。

在调用函数时，可以按顺序提供默认参数，例如`func('param1','param2',30)`就代表 第三个参数传入30,第四个参数由于没有传入则使用默认。
也可以乱序提供默认参数，例如`func('param1','param2',c = 11)`,当乱序提供时需要指定参数名称

	#函数定义
	def func(a,b,c = 18, d = 'beijing'):
		pass

- **Python函数在定义的时候，默认参数的值就被计算出来了，默认参数也是一个变量，指向一个对象，每次调用该函数，如果改变了默认参数对应的变量，则下次调用时，默认参数内容就变了。**

	**所以定义默认参数时：默认参数必须指向不变对象**
		#出错的情况
		>>> def addEnd(a = []):
			a.append('END')
			return a
		>>> addEnd()
		['END']
		>>> addEnd()
		['END','END']
		#正确情况
		>>> def addEnd(a = None):
			if a is None:
				a = []
			a.append('END')
			return a

### 3.2.3 可变参数

可变参数即传入的参数是不定的，可以是任意个。

通过在参数前 添加`*`符号，即可将参数变成可变参数.在函数内部，参数接收到的是一个`tuple`

如果已有一个`list`或者`tuple`,可以通过在其前面添加一个`*`符号变成可变参数传入 需要可变参数 的函数

	>>>l = [1,2,3]
	>>>calc(l[0],l[1],l[2])
	>>>calc(*l)
	>>> def calc(*numbers):
    	sum = 0
    	for n in numbers:
        	sum = sum + n * n
    	return sum

### 3.2.4 关键字参数

关键字参数允许传入任意个含参数名的参数，这些关键字参数在函数内部自动组装成一个`dict`

	>>> def person(name, age, **kw):
	    print('name:', name, 'age:', age, 'other:', kw)
	
	>>> person('Michael', 30)
	name: Michael age: 30 other: {}

	>>> person('Bob', 35, city='Beijing')
	name: Bob age: 35 other: {'city': 'Beijing'}
	>>> person('Adam', 45, gender='M', job='Engineer')
	name: Adam age: 45 other: {'gender': 'M', 'job': 'Engineer'}

**可以直接将`dict`传入关键字参数，但是需要在`dict`之前 添加俩个`*`符号 **

**关键字参数仅获取到传入的`dict`的一份拷贝，对这份拷贝的改动不会影响到函数外的`dict`**

### 3.2.5 命名关键字参数

如果需要限制关键字参数，即可使用命名关键字参数。

**命名关键字参数 通过`*,param1,param2` 进行定义，`*`符号后面的参数被视为命名关键字参数**。如果函数定义中已经有了一个可变参数，那么后面跟着的命名关键字参数就不需要添加分隔符号`*`

	>>> def person(name, age, *, city, job):
	    print(name, age, city, job)
	
	>>> person('Jack', 24, city='Beijing', job='Engineer')
	Jack 24 Beijing Engineer

	>>> def person(name, age, *args, city, job):
    print(name, age, args, city, job)

**注意：命名关键字参数在调用时必须带上参数名称，且数量必须正确**，如果不带上参数名称，Python解释器会将传入的参数视为位置参数，但实际上函数需要的并不是位置参数，所以会报错

**命名关键字参数 也可以有默认值,这样在函数调用时如果没传入该参数时就会取默认值**

	>>> def person(name, age, *, city='Beijing', job):
	    print(name, age, city, job)
	>>> person('Jack', 24, job='Engineer')
	Jack 24 Beijing Engineer


### 3.2.6 参数组合

在Python中定义函数，可以用**必选参数、默认参数、可变参数、关键字参数和命名关键字参数，这5种参数都可以组合使用**。

**请注意，参数定义的顺序必须是：必选参数、默认参数、可变参数、命名关键字参数和关键字参数。**


对于任意函数，都可以通过类似`func(*args,**kw)`的形式调用它，无论它的参数是如何定义的

例如如下俩个函数,在函数调用的时候，Python解释器自动按照参数位置和参数名把对应的参数传进去。

	def f1(a, b, c=0, *args, **kw):
	    print('a =', a, 'b =', b, 'c =', c, 'args =', args, 'kw =', kw)
	
	def f2(a, b, c=0, *, d, **kw):
	    print('a =', a, 'b =', b, 'c =', c, 'd =', d, 'kw =', kw)


	>>> f1(1, 2)
	a = 1 b = 2 c = 0 args = () kw = {}
	>>> f1(1, 2, c=3)
	a = 1 b = 2 c = 3 args = () kw = {}
	>>> f1(1, 2, 3, 'a', 'b')
	a = 1 b = 2 c = 3 args = ('a', 'b') kw = {}
	>>> f1(1, 2, 3, 'a', 'b', x=99)
	a = 1 b = 2 c = 3 args = ('a', 'b') kw = {'x': 99}
	>>> f2(1, 2, d=99, ext=None)
	a = 1 b = 2 c = 0 d = 99 kw = {'ext': None}

	#通过 一个list 一个tuple 也可以调用
	>>> args = (1, 2, 3, 4)
	>>> kw = {'d': 99, 'x': '#'}
	>>> f1(*args, **kw)
	a = 1 b = 2 c = 3 args = (4,) kw = {'d': 99, 'x': '#'}
	>>> args = (1, 2, 3)
	>>> kw = {'d': 88, 'x': '#'}
	>>> f2(*args, **kw)
	a = 1 b = 2 c = 3 d = 88 kw = {'x': '#'}

## 3.3 递归函数

在函数内部，可以调用其他函数。如果一个函数在内部调用自身本身，这个函数就是递归函数。

**使用递归函数需要注意防止栈溢出**。在计算机中，函数调用是通过栈（`stack`）这种数据结构实现的，每当进入一个函数调用，栈就会加一层栈帧，每当函数返回，栈就会减一层栈帧。由于栈的大小不是无限的，所以，递归调用的次数过多，会导致栈溢出

- 栈溢出可以通过**尾递归**优化：尾递归指的是，在函数返回的时候，调用自身本身，并且`return`语句不能包含表达式。这样编译器或解释器就可以对尾递归进行优化，使递归本身无论调用多少次都只占用一个帧栈。

		#优化之前
		def fact(n):
		    if n==1:
		        return 1
		    return n * fact(n - 1)

		#优化之后
		def fact(n):
		    return fact_iter(n, 1)
		
		def fact_iter(num, product):
		    if num == 1:
		        return product
		    return fact_iter(num - 1, num * product)

**大多数编程语言都没有针对尾递归做优化，Python解释器也没有，所以任何Python中的函数递归都可能导致栈溢出**


# 4.高级特性
## 4.1 切片
对这种经常取指定索引范围的操作，用循环十分繁琐，因此，Python提供了切片（`Slice`）操作符，能大大简化这种操作。

	# 旧方法
	>>> L = ['Michael', 'Sarah', 'Tracy', 'Bob', 'Jack']
	>>> L[0],L[1],L[2]
	# 取前n个
	>>> r = []
	>>> n = 3
	>>> for i in range(n):
			r.append(L[i])
	
	# 使用切片
	>>> L[0:3]

- `L[0:3]`表示从索引`0`开始，知道索引`3`结束，不包括`3`。如果第一个索引是`0`,默认还可以省略

- Python还支持倒数切片，倒数第一个元素的索引是`-1`,`L[-1:]`取最后一个元素。

- `L[:]`边上不写边界的情况，会默认取所有。例如，`L[:10]`就是取前十.`L[-3:]`就是取后三

- `L[::5]`代表所有数，从首位开始然后每五个取一次

- `L[:]`可以原样复制一个list

- `tuple`也可以使用切片，操作结果仍然为`tuple`

- 字符串也可以看做是一种list，其中每个字符为一个元素，其操作结果仍然是字符串

## 4.2 迭代

给定一个`list`或`tuple`，可以通过`for`循环遍历，这种遍历称为**迭代(Iteration)**

Python中的迭代通过`for....in ...`完成，任何可迭代对象都可以进行迭代，不局限于`list`,`tuple`这种有下标的数据，`dict`这种无下标的也可以迭代。

`dict`默认迭代的是key，如果要迭代value，可以通过`for value in dict.values()`.如果要同时迭代key和value,可以通过`for k,v in d.items()`

字符串也是可迭代对象

**通过collections模块的Iterable类型进行判断是否属于可迭代对象**

	>>> from collections import Iterable
	>>> isinstance('abc', Iterable) # str是否可迭代
	True
	>>> isinstance([1,2,3], Iterable) # list是否可迭代
	True
	>>> isinstance(123, Iterable) # 整数是否可迭代
	False

Python内置的`enumerate`函数，可以将一个`list`转换成 索引-元素 对的形式

	>>> l = ['a','b','c']
	>>> list(enumerate(l))
	[(1,'a'),(2,'b'),(3,'c')]

Python中同时引用俩个变量是很正常的。


## 4.3 列表生成式

`[x * x for x in range(11)]`生成`[0*0,1*1......10*10]`

**for 循环后面还可以添加`if`判断，用来对参数做筛选**

`[x * x for x in range(1, 11) if x % 2 == 0]`

for循环 可以是 多层的，例如

	>>>[m * n for m in 'abc' for n in 'def']
    ['ad','ae','af','bd','be'.......'cf']

	for m in 'abc':
        for n in 'def':
             print(m+n)

`for`循环可以同时使用俩个甚至多个变量


## 4.4 生成器

在Python中，一边循环一边计算的机制称为**生成器generator**。凡是使用了`yield`的函数都被称为生成器.

生成器有俩种定义方式：

1. 只用将列表生成式外层的 中括号 替换成 圆括号，这就是一个生成器

	区别是 最外层的`[]`和`()`,前者代表一个`list`后者代表一个`generator`

2. 如果一个函数定义中包括`yield`关键字，那这个函数就是一个生成器

	generator和函数的执行流程不一样。函数是顺序执行，遇到return语句或者最后一行函数语句就返回。而变成generator的函数，在每次调用next()的时候执行，遇到yield语句返回，再次执行时从上次返回的yield语句处继续执行。

	在对函数形式的生成器进行`for`循环时是拿不到`return`语句的返回值，如果想拿到返回值，必须手动捕获`StopIteration`错误，返回值包含在`StopIteration`的`value`中

`generator`通过`next(g)`函数获取下一个返回值，当没有更多元素时，会抛出`StopIteration`.

	def fib(max):
	    n, a, b = 0, 0, 1
	    while n < max:
	        yield b
	        a, b = b, a + b
	        n = n + 1
	    return 'done'

	>>> g = fib(6)
	>>> while True:
	...     try:
	...         x = next(g)
	...         print('g:', x)
	...     except StopIteration as e:
	...         print('Generator return value:', e.value)
	...         break

`generator`也是一个 可迭代对象，可以使用`for`循环

- 表达式赋值：

        a, b = b, a + b # 等号后面是一个tuple，这一行可以被理解为如下俩行
		a = t[0]
        b = t[1]

## 4.5 迭代器

**凡是可以直接作用于`for`循环的数据有俩种，这些统称为可迭代对象(Iterable)**

- 集合数据类型，如list、tuple、dict、set、str等；

- generator，包括生成器和带`yield`的`generator function`

可以通过`isinstance([],Iterable)`判断是否是可迭代对象

生成器不仅可以作用于`for`循环，还可以被`next()`函数不断调用并返回下一个值，直至抛出`StopIteration`错误。**凡是可以被`next()`函数调用并不断返回下一个值的对象被称为 迭代器`Iterator`**

`Iterator`对象表示一个数据流，可以把这个数据流看做一个有序序列，但是不能提前知道序列的长度，只能不断通过`next()`函数计算下一个数据，**所以`Iterator`的计算是惰性的，只有在需要返回下一个数据时才会计算！**

`Iterator`可以表示一个无穷大的数据流，例如全体自然数，但是`list`是不可能实现的。

一些类型例如list,dict,str虽然是`Iterable`但却不是`Iterator`,**不过可以通过`iter()`函数进行转换**

迭代器有俩个基本方法：`iter()`和`next()`

- 可以通过`iter()`创建迭代器对象

- `next()`用来获取迭代器的下一个元素

# 5.函数式编程

函数式编程就是一种抽象程度很高的编程范式，纯粹的函数式编程语言编写的函数没有变量，因此，任意一个函数，只要输入是确定的，输出就是确定的，这种纯函数我们称之为没有副作用。而允许使用变量的程序设计语言，由于函数内部的变量状态不确定，同样的输入，可能得到不同的输出，因此，这种函数是有副作用的。

函数式编程的一个特点就是，允许把函数本身作为参数传入另一个函数，还允许返回一个函数！

Python对函数式编程提供部分支持。由于Python允许使用变量，因此，Python不是纯函数式编程语言。

## 5.1 高阶函数

1. **变量可以指向函数**

		f = abs
		>>> f(-12)
		12

2. **函数名也是变量**

	函数名其实就是指向函数的变量

3. **传入函数**

	既然变量可以指向函数，函数的参数能接收变量，那么一个函数就可以接收另一个函数作为参数，**这种函数就称之为高阶函数。**

### 5.1.1 map/reduce

Python内置了`map()`和`reduce()`函数,reduce需要`from functools import reduce`

- **`map()`函数接收俩个参数，一个是函数，一个是`Iterable`,`map()`函数会将传入的函数依次作用于`Iterable`,并返回一个`Iterator`**

		>>> def f(x):
		...     return x * x
		...
		>>> r = map(f, [1, 2, 3, 4, 5, 6, 7, 8, 9])
		#`Iterator`是惰性序列，因此需要通过`list()`函数让它将整个序列计算出来并返回一个list
		>>> list(r)
		[1, 4, 9, 16, 25, 36, 49, 64, 81]

- `reduce()`函数把一个 函数作用在一个序列上，这个函数必须接收俩个参数，`reduce()`函数把结果继续和序列的下一个元素做累积计算

		reduce(f, [x1, x2, x3, x4]) = f(f(f(x1, x2), x3), x4)

### 5.1.2 filter

`filter()`函数属于Python内建的函数

`filter()`函数接收一个函数和一个序列，`filter()`函数会根据 传入函数的返回值决定是否保留当前序列中的对应元素，返回一个`Iterator`(也就是一个惰性序列)

### 5.1.3 sorted

Python内置的`sorted()`函数就可以对list进行排序，另外还可以接收一个`key`函数来对元素进行预先操作。`key`函数 会作用于list的每一个元素上，然后根据key函数的返回值组成的心list进行排序

如果需要反向排序，传入第三个参数`reverse = True`即可

## 5.2 返回函数

1. 函数作为返回值

	高阶函数不仅可以接收函数作为参数，还可以返回函数。这个被返回的函数没有立刻执行，而是在被调用时才会执行。并且 每次返回的函数 都互不相关

	相关参数和变量都保存在返回的函数中的函数被称为“闭包（Closure）

2. 闭包

	返回函数不能引用任何循环变量，或者后续会发生变化的变量。否则会对返回函数的执行结果产生影响。

	**如果一定需要引用循环变量，可以通过创建一个函数，用该函数的参数绑定循环变量当前的值，无论该循环变量后续如何更改，已绑定到函数参数的值不变**

		# f1()= 9  f2() = 9 f3() = 9
		def count():
		    fs = []
		    for i in range(1, 4):
		        def f():
		             return i*i
		        fs.append(f)
		    return fs
		
		f1, f2, f3 = count()
		# f1() = 1 f2() = 4 f3() = 9
		def count():
		    def f(j):
		        def g():
		            return j*j
		        return g
		    fs = []
		    for i in range(1, 4):
		        fs.append(f(i)) # f(i)立刻被执行，因此i的当前值被传入f()
		    return fs

## 5.3 匿名函数

关键字`lambda`表示匿名函数,`:`冒号之前是参数，之后是表达式

	lambda [arg1 [,arg2,.....argn]]:expression

	#俩者等价
	def f(x):
	    return x * x
	
	lambda x: x * x

匿名函数有个限制，**只能有一个表达式，不能写`return`，返回值就是表达式的结果**

匿名函数没有名字，所以不用担心函数名冲突

匿名函数也是一个函数对象，可以将匿名函数赋值给一个变量，再利用变量进行调用

匿名函数也是函数，所以可以作为返回值返回

## 5.4 装饰器

**函数也是对象，默认有一个`__name__`属性代表函数的名字**

使用`@+函数名`，添加到被定义的函数之上.即可实现**装饰器Decorator**,装饰器不需要改变原始的函数即可替原始函数添加功能。

	@log
	def now():
	    print('2015-3-25')
	# log 函数如下所示，返回一个wrapper
	def log(func):
	    def wrapper(*args, **kw):
	        print('call %s():' % func.__name__)
	        return func(*args, **kw)
	    return wrapper

- 等同于`now = log(now)`

	由于`log()`是一个decorator，返回一个函数，所以，原来的now()函数仍然存在，只是现在同名的now变量指向了新的函数，于是调用now()将执行新函数，即在log()函数中返回的wrapper()函数。

- `warpper(*args,**kw)`表示函数可以接收任意参数


如果decorator本身需要参数，那就需要编写一个包含`wrapper`的高阶函数，例如

	def log(text):
	    def decorator(func):
	        def wrapper(*args, **kw):
	            print('%s %s():' % (text, func.__name__))
	            return func(*args, **kw)
	        return wrapper
	    return decorator

	@log('execute')
	def now():
	    print('2015-3-25')
	# 效果等同于
	>>> now = log('execute')(now)

- 首先执行`log('execute')`，返回的是`decorator`函数，再调用返回的函数，参数是now函数，返回值最终是wrapper函数。

**被装饰过后的函数，实际上函数的`__name__`参数已经发生了变化，对于有些依赖函数签名的代码来说执行可能会出错**。可以通过Python内置的`functools.wraps`实现类似`wrapper__name__=func.__name__`的功能

	# 无参数
	import functools
	
	def log(func):
	    @functools.wraps(func)
	    def wrapper(*args, **kw):
	        print('call %s():' % func.__name__)
	        return func(*args, **kw)
	    return wrapper
	
	#有参数
	import functools
	
	def log(text):
	    def decorator(func):
	        @functools.wraps(func)
	        def wrapper(*args, **kw):
	            print('%s %s():' % (text, func.__name__))
	            return func(*args, **kw)
	        return wrapper
	    return decorator

	# 兼容俩种情况
	def log(param):
	    def decorator(func):
	        def wrapper(*args, **kw):
	            print('start')
	            return func(*args, **kw)
	        return wrapper
        # param 类型是 str.所以返回函数引用
	    if isinstance(param, str):
	        return decorator
        # param 类型是 函数！ 所以返回调用decorator(param)的返回值 
	    elif not isinstance(param, str):
	        return decorator(param)

## 5.5 偏函数

Python中的`functools`模块提供了许多功能，其中一个就是**偏函数**(`Partial function`).这里的偏函数与数学意义上的不一样

**偏函数可以实现将一个函数所需的某些参数固定(即给定默认值),然后返回一个新的函数(这个函新函数不需要传入已经固定的参数)，**

- `int()`函数可以把字符串转换为整数，默认按十进制，`int()`函数提供了额外的`base`参数，用来设定 进制，默认十进制

		>>> int('123',base= 8)

- 偏函数的作用：
		# 下面函数 in2 将参数base=2 固定
		def int2(x, base=2):
		    return int(x, base)
		# 等同于
		>>> import functools
		>>> int2 = functools.partial(int, base=2)


创建偏函数时，可以传入 函数对象，`*args`,`**kw`这三种参数

	int2 = functools.partial(int, base=2)
	#等价于
	kw = { 'base': 2 }
	int('10010', **kw)

	#当传入 
	max2 = functools.partial(max, 10)
	#等价于
	# 10 会被添加到 *args的左边
	args = (10, 5, 6, 7)
	max(*args)


