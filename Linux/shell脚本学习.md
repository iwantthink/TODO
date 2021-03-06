# Shell脚本学习

[学习shell](http://billie66.github.io/TLCL/book/index.html)

[Shell-菜鸟教程](http://www.runoob.com/linux/linux-shell-variable.html)

# 1. 简介

Shell 是一个用 C 语言编写的程序，它是用户使用 Linux 的桥梁。

- 其既是一种命令语言，又是一种程序设计语言。

Shell 是指一种应用程序（即命令行），这个应用程序提供了一个界面，用户通过这个界面访问操作系统内核的服务（接受从键盘输入的命令，然后把命令传递给操作系统去执行）

- 几乎所有的 Linux 发行版都提供一个名为 bash 的 来自 GNU 项目的 shell 程序。

- “bash” 是 “Bourne Again SHell” 的首字母缩写， 所指的是这样一个事实，bash 是最初 Unix 上由 Steve Bourne 写成 shell 程序 sh 的增强版。


- 当使用图形用户界面时，我们需要另一个和 shell 交互的叫做终端仿真器的程序。 如果我们浏览一下桌面菜单，可能会找到一个。虽然在菜单里它可能都 被简单地称为 “terminal”，此外还有其他一些终端仿真器可供 Linux 使用，**但基本上，它们都完成同样的事情，即使我们能访问shell**



# 2. 编写Shell脚本

## 2.1 什么是Shell脚本？

最简单的解释，一个 shell 脚本就是一个包含一系列命令的文件。shell 读取这个文件，然后执行 文件中的所有命令，就好像这些命令已经直接被输入到了命令行中一样。

- Shell 有些独特，因为它不仅是一个功能强大的命令行接口,也是一个脚本语言解释器。

- 大多数能够在命令行中完成的任务也能够用脚本来实现，同样地，大多数能用脚本实现的操作也能够 在命令行中完成。


## 2.2 如何编写一个Shell脚本

1. 编写一个脚本

	Shell 脚本就是普通的文本文件

2. 使脚本文件可执行

	需要设置脚本文件的权限来允许其可执行

3. 把脚本放置到 shell 能够找到的地方

	当没有指定可执行文件明确的路径名时，shell 会自动地搜索某些目录， 来查找此可执行文件。为了最大程度的方便，我们会把脚本放到这些目录当中。
	
	
### 2.2.1 脚本文件格式

**每一个Shell脚本都需要添加一行固定代码到内容的第一行，以此告诉操作系统将使用什么解释器去执行该脚本**

	#!/bin/bash

- `#!`字符序列表示的是`shebang`

- 有一种特殊情况可以不用添加解释器信息在内容中，那就是直接在命令行中运行解释器，并将脚本名称作为参数
	
### 2.2.2 可执行权限

需要给与当前文件可执行的权限

	chmod u=x targetfile
	
### 2.2.3 脚本文件位置

当输入可执行命令之后，Shell会通过查找一个目录列表来获取命令的执行程序. **如果没有给出可执行程序的明确路径名，那么系统每次都会 搜索一系列的目录，来查找此可执行程序**

- 这个目录列表就保存在名为`PATH`的环境变量中，使用冒号进行分隔

	`/bin` 目录就是其中一个系统会自动搜索的目录。

- **按照通常的规则，添加目录到你的 `PATH` 变量或者是定义额外的环境变量，要把这些更改放置到 `.bash_profile` 文件中. 对于其他的更改，需要放到`.bashrc`文件中**

	- export 命令告诉 shell 让这个 shell 的子进程可以使用 PATH 变量的内容

	- source 命令可以让shell重新读取指定文件

	
正常情况下，如果编写了一个脚本，并希望系统中的每个用户都可以使用它， 那么这个脚本的传统位置是 `/usr/local/bin`。系统管理员使用的脚本经常放到 `/usr/local/sbin` 目录下。 大多数情况下，本地支持的软件，不管是脚本还是编译过的程序，都应该放到 `/usr/local `目录下， 而不是在 /bin 或 /usr/bin 目录下
	
	
## 2.3 长/短选项

许多命令都有长短俩种选项名称作为特征，例如,ls命令许多选项可以使用短形式也可以使用长形式

	ls -ad
	
	ls --all --directory
	
- 短选项书写方便，长选项易于读取。 他们实现的功能并没有区别！


## 2.4 缩进和行继续符

使用长命令的时候，通过把命令在几个文本行中展开，可以提高命令的可读性

- 通过使用行继续符（反斜杠-回车符序列）和缩进，就可以实现这个功能，更加清晰的展示命令。 

- 脚本和 命令行的一个区别是，脚本可能使用 tab 字符来实现缩进，然而命令行却不能，因为 tab 字符被用来 激活自动补全功能。

## 2.5 vim的特殊设置

vim文本编辑器有许多配置可以帮助脚本的编写（**通过把这些命令（没有开头的冒号字符）添加到你的 `~/.vimrc` 文件中，这些改动会永久生效**）

- `:syntax:on`: 打开语法高亮，不同的shell语法元素会以不同的颜色显示 (`:set syntax=sh`备用)

- `:set hlsearch`:打开查找结果高亮

- `:set tabstop=4`:设置一个tab字符所占的列数，默认是8

- `:set autoindent`:打开 “auto indent” 功能。这将导致 vim 能对新的文本行缩进与刚输入的文本行相同的列数。 对于许多编程结构来说，这就加速了输入。停止缩进，输入 Ctrl-d。


# 3. Shell的基础知识

## 3.1 带引号的字符串允许包含换行符

	#!/bin/bash
	# Program to output a system information page
	echo "<HTML>
	    <HEAD>
	          <TITLE>Page Title</TITLE>
	    </HEAD>
	    <BODY>
	          Page body.
	    </BODY>
	</HTML>"

- 一个带引号的字符串可能包含换行符，因此可以包含多个文本行，Shell 会持续读取文本直到它遇到 右引号

	**它在命令行中也是这样工作**
	
	- 需要注意的一点，每当我们在 shell 中键入多行语句的时候， `>`提示符会在每一行开头出现

## 3.2 注释

以`#`开头的一行就是注释，会被解释器忽略
	
	
# 4. 变量

Shell中把所有的变量都当做字符串来处理

- 通过使用带有`-i` 选项的 declare 命令，可以强制 shell 把 赋值限制为整数,但是几乎不会这么做

- 赋值过程中，变量名称，等号和变量值之间不允许出现空格

- 变量名可能使用花括号 `{}` 进行包围，用来帮助解释器来识别变量的边界

		// 通过mv命令，修改文件名称
		fileName='names'
		// 报错，Shell会认为变量名为fileName1
		mv $fileName $fileName1
		mv $fileName ${fileName}1

当 shell直接碰到一个未创建的变量时，它会自动地创建它

- 当使用的变量并没有提前被初始化，那么其默认被赋值为空

**变量可以通过`$`符号来使用**


## 4.1 变量名的规则

1. 变量名可由字母，数字，字符（字母和数字）和下划线字符组成。

2. 变量名的第一个字符必须是一个字母或一个下划线。

3. 变量名中不允许出现空格和标点符号

4. 不能使用被bash保留的关键字，可以通过help命令进行查看

5. Shell 并不能区分常量和变量，大多数情况下只能通过命名习惯来区分，一个常用惯例是指定大写字母来表示常量，小写字母表示真正的变量
	
	- 实际上，shell 确实提供了一种方法，通过使用带有`-r`（只读）选项的内部命令 declare， 来强制常量的不变性

			declare -r TITLE=”Page Title”
			
## 4.2 只读变量

使用 `readonly` 命令可以将变量定义为只读变量，只读变量的值不能被改变。

	myUrl="http://www.google.com"
	readonly myUrl
	myUrl="http://www.runoob.com"
	
## 4.3 删除变量
使用 `unset` 命令可以删除变量。

	unset variable_name

- 变量被删除后不能再次使用

- unset 命令不能删除只读变量

## 4.4 变量类型

运行Shell时，会同时存在三种类型的变量

1.  局部变量 

	局部变量在脚本或命令中定义，仅在当前shell实例中有效，其他shell启动的程序不能访问局部变量

2. 环境变量 

	所有的程序，包括shell启动的程序，都能访问环境变量，有些程序需要环境变量来保证其正常运行。必要的时候shell脚本也可以定义环境变量
	
3. shell变量 

	shell变量是由shell程序设置的特殊变量。shell变量中有一部分是环境变量，有一部分是局部变量，这些变量保证了shell的正常运行

	
# 5 Shell字符串

字符串是shell编程中最常用最有用的数据类型（除了数字和字符串，也没啥其它类型好用了）

- **字符串可以用单引号，也可以用双引号，也可以不用引号**


符号|能否引用变量  |  能否引用转义符  |  能否引用文本格式符(如：换行符、制表符)
---|:---:|:---:|:---:
单引号  |否|否|否
双引号  |能|能|能
无引号  |能|能|否     

## 5.1 单引号形式

1. 单引号里的任何字符都会原样输出，单引号字符串中的变量是无效的

2. 单引号字串中不能出现单独一个的单引号（对单引号使用转义符后也不行），但可成对出现，作为字符串拼接使用。

## 5.2 双引号形式

	your_name='runoob'
	str="Hello, I know you are \"$your_name\"! \n"
	echo -e $str	
	
双引号的优点：

1. 双引号里可以有变量

2. 双引号里可以出现转义字符
	
	
## 5.3 拼接字符串

1. 使用双引号拼接

		your_name="runoob"
		greeting="hello, "$your_name" !"
		greeting_1="hello, ${your_name} !"
		echo $greeting  $greeting_1

2. 使用单引号拼接

		your_name="runoob"
		greeting_2='hello, '$your_name' !'
		greeting_3='hello, ${your_name} !'
		echo $greeting_2  $greeting_3	

## 5.4 获取字符串长度

	string='abc'
	echo ${#string}
	> 3
	
## 5.5 提取字符串

	string="runoob is a great site"
	echo ${string:1:4} 
	> unoo	
	
- 从字符串的第2个字符串开始，截取4个字符


## 5.6 查找子字符串
查找字符i或者o的位置（哪个字母先出现就计算哪个字母的位置）

	string="runoob is a great site"
	echo `expr index "$string" io`  # 输出 4
	
- **注意：是反引号(`)，而不是单引号(')**

# 6. Shell数组

bash支持一维数组（不支持多维数组），并且没有限定数组的大小。

- 类似于 C 语言，数组元素的下标由 0 开始编号

- 获取数组中的元素要利用下标，下标可以是整数或算术表达式，其值应大于或等于 0。

## 6.1 定义数组

在 Shell 中，用括号来表示数组，数组元素用"空格"符号分割开。

定义数组的一般形式为：

	数组名=(值1 值2 ... 值n)

或者：

	数组名=(
	值1
	值2
	值3
	)
	
还可以单独定义数组的各个值

	数组名[0]=值1
	数组名[n]=值n
	
## 6.2 读取数组

读取数组元素值的一般格式是：

	${数组名[下标]}

- 使用`@`符号可以获取数组中的所有元素

		echo ${array_name[@]}

## 6.3 获取数组的长度

获取数组长度的方法与获取字符串长度的方法相同，例如：

	# 取得数组元素的个数
	length=${#array_name[@]}
	# 或者
	length=${#array_name[*]}
	# 取得数组单个元素的长度
	lengthn=${#array_name[n]}
	
	
# 7. Shell传递参数

在执行Shell脚本时，可以向脚本传递参数，脚本内获取参数的格式为：`$n`

- 序列0指的是脚本的文件名

- n 代表一个数字，数字1 为执行脚本的第一个参数，2 为执行脚本的第二个参数，以此类推……	

示例：

	Test.sh
	#!/bin/bash
	echo "执行的文件名：$0";
	echo "第一个参数为：$1";
	echo "第二个参数为：$2";
	echo "第三个参数为：$3";

	Test a b c
	输出内容：
	执行的文件名：./Test.sh
	第一个参数为：a
	第二个参数为：b
	第三个参数为：c
	
## 7.2 处理参数时的特殊字符

参数处理	|说明
---|---
`$#	`|传递到脚本的参数个数
`$*`|以一个单字符串显示所有向脚本传递的参数。如`"$*"`用`"`括起来的情况、以`"$1 $2 … $n"`的形式输出所有参数。
`$$`	|脚本运行的当前进程ID号
`$!`	|后台运行的最后一个进程的ID号
`$@`	|与`$*`相同，但是使用时加引号，并在引号中返回每个参数。如`"$@"`用`"`括起来的情况、以`"$1" "$2" … "$n"` 的形式输出所有参数。
`$-	`|显示Shell使用的当前选项，与set命令功能相同。
`$?`	|显示最后命令的退出状态。0表示没有错误，其他任何值表明有错误。	
	
	
- `$*` 与 `$@` 区别：

	相同点：都是引用所有参数。

	不同点：只有在双引号中体现出来。假设在脚本运行时写了三个参数 1、2、3，，则 `" * "` 等价于 `"1 2 3"`（传递了一个参数），而 `"@"` 等价于 `"1" "2" "3"`（传递了三个参数）。	
	
- **在为shell脚本传递的参数中如果包含空格，应该使用单引号或者双引号将该参数括起来，以便于脚本将这个参数作为整体来接收**

	
# 8. Shell基本运算符

原生bash不支持简单的数学运算，但是可以通过其他命令来实现，例如 `awk` 和 `expr`(常用)

expr 是一款表达式计算工具，使用它能完成表达式的求值操作。
	
	val=`expr 2 + 2`
	echo "两数之和为 : $val"	
	
- 表达式和运算符之间要有空格，例如 `2+2` 是不对的，必须写成 `2 + 2`，这与我们熟悉的大多数编程语言不一样。

- 完整的表达式要被 反引号\` 包含，注意这个字符不是常用的单引号，在 Esc 键下边。	


## 8.1 算数运算符

运算符	|说明	|举例
---|---|---
`+	`|加法	|`expr $a + $b` 结果为 30。
`-	`|减法	|`expr $a - $b` 结果为 -10。
`*	`|乘法	|`expr $a \* $b` 结果为  200。
`/	`|除法	|`expr $b / $a` 结果为 2。
`%	`|取余	|`expr $b % $a` 结果为 0。
`=	`|赋值	|`a=$b` 将把变量 b 的值赋给 a。
`==	`|相等 |用于比较两个数字，相同则返回 true。	`[ $a == $b ]` 返回 false。
`!=`	|不相等|用于比较两个数字，不相同则返回 true。	`[ $a != $b ]` 返回 true。

- **条件表达式要放在方括号之间，并且要有空格**

	例如: `[$a==$b] `是错误的，必须写成 `[ $a == $b ]`
	
		if [ $a == $b ]
		then
		   echo "a 等于 b"
		fi
	
- 乘号(`*`)前边必须加反斜杠(`\`)才能实现乘法运算；

- 在 MAC 中 shell 的 `expr` 语法是：

	`$((表达式))`，此处表达式中的 `*` 不需要转义符号 `\` 
	
	
## 8.2 关系运算符

**关系运算符只支持数字，不支持字符串，除非字符串的值是数字**

假定变量 a 为 10，变量 b 为 20
	
	
运算符	|说明	|举例
---|---|---
`-eq`|	检测两个数是否相等，相等返回 true	|`[ $a -eq $b ]` 返回 false。
`-ne`|	检测两个数是否不相等，不相等返回 true|`	[ $a -ne $b ]` 返回 true。
`-gt`|	检测左边的数是否大于右边的，如果是，则返回 true|`	[ $a -gt $b ]` 返回 false。
`-lt`|	检测左边的数是否小于右边的，如果是，则返回 true|	`[ $a -lt $b ] `返回 true。
`-ge`|	检测左边的数是否大于等于右边的，如果是，则返回 true|	`[ $a -ge $b ]` 返回 false。
`-le`|	检测左边的数是否小于等于右边的，如果是，则返回 true|	`[ $a -le $b ]` 返回 true。	
	
示例：
	
	if [ $a -eq $b ]
	then
	   echo "$a -eq $b : a 等于 b"
	else
	   echo "$a -eq $b: a 不等于 b"
	fi	
	
## 8.3  布尔运算符


运算符	|说明	|举例
---|---|---
`!	`|非运算，表达式为 true 则返回 false，否则返回 true|	`[ ! false ]` 返回 true。
`-o`|	或运算，有一个表达式为 true 则返回 true|	`[ $a -lt 20 -o $b -gt 100 ]` 返回 true。
`-a`|	与运算，两个表达式都为 true 才返回 true|`	[ $a -lt 20 -a $b -gt 100 ]` 返回 false。


示例：

	if [ $a != $b ]
	then
	   echo "$a != $b : a 不等于 b"
	else
	   echo "$a != $b: a 等于 b"
	fi	
	
## 8.4 逻辑运算符


运算符	|说明	|举例
---|---|---
`&&`|	逻辑的 AND	|`[[ $a -lt 100 && $b -gt 100 ]]` 返回 false
`||`|	逻辑的 OR	|`[[ $a -lt 100 || $b -gt 100 ]]` 返回 true	

示例：

	if [[ $a -lt 100 && $b -gt 100 ]]
	then
	   echo "返回 true"
	else
	   echo "返回 false"
	fi
	
	
## 8.5 字符串运算符

假定变量 a 为 "abc"，变量 b 为 "efg"

运算符	|说明	|举例
---|---|---
`=`	|检测两个字符串是否相等，相等返回 true|	`[ $a = $b ]` 返回 false
`!=`	|检测两个字符串是否相等，不相等返回 true|	`[ $a != $b ]` 返回 true
`-z`	|检测字符串长度是否为0，为0返回 true|	`[ -z $a ]` 返回 false
`-n`	|检测字符串长度是否为0，不为0返回 true|	`[ -n "$a" ]` 返回 true
`$`	|检测字符串是否为空，不为空返回 true|	`[ $a ]` 返回 true。	
示例：

	a="abc"
	b="efg"
	
	if [ $a = $b ]
	then
	   echo "$a = $b : a 等于 b"
	else
	   echo "$a = $b: a 不等于 b"
	fi
	
	
	
## 8.6 文件测试运算符

文件测试运算符用于检测 Unix 文件的各种属性


操作符	|说明	|举例
---|---|---
`-b` file |检测文件是否是块设备文件，如果是，则返回 true|	[ -b $file ] 返回 false。
`-c` file|	检测文件是否是字符设备文件，如果是，则返回 true|	`[ -c $file ]` 返回 false。
`-d` file|	检测文件是否是目录，如果是，则返回 true|	`[ -d $file ]` 返回 false。
`-f` file|	检测文件是否是普通文件（既不是目录，也不是设备文件），如果是，则返回 true|	`[ -f $file ]` 返回 true。
`-g` file|	检测文件是否设置了 SGID 位，如果是，则返回 true|	`[ -g $file ]` 返回 false。
`-k` file|	检测文件是否设置了粘着位(Sticky Bit)，如果是，则返回 true|`	[ -k $file ]` 返回 false。
`-p` file|	检测文件是否是有名管道，如果是，则返回 true|	`[ -p $file ]` 返回 false。
`-u` file|	检测文件是否设置了 SUID 位，如果是，则返回 true|	`[ -u $file ]` 返回 false。
`-r` file|	检测文件是否可读，如果是，则返回 true |	`[ -r $file ]` 返回 true。
`-w` file|	检测文件是否可写，如果是，则返回 true|	`[ -w $file ]` 返回 true。
`-x` file|	检测文件是否可执行，如果是，则返回 true|	`[ -x $file ]` 返回 true。
`-s` file|	检测文件是否为空（文件大小是否大于0），不为空返回 true|	`[ -s $file ]` 返回 true。
`-e` file|	检测文件（包括目录）是否存在，如果是，则返回 true|	`[ -e $file ]` 返回 true。

变量 file 表示文件"/var/www/runoob/test.sh"，它的大小为100字节，具有 rwx 权限。


	file="/var/www/runoob/test.sh"
	if [ -r $file ]
	then
	   echo "文件可读"
	else
	   echo "文件不可读"
	fi


# 9. echo命令

Shell的`echo`命令用于字符串的输出

	echo string
	
## 9.1 显示普通字符串

	echo "It is a test!"
	> It is a test!
	
- **双引号可以进行省略**	

## 9.2 显示转义字符

	echo "\"It is a test!\""
	> "It is a test!"
	
## 9.3 显示变量

	read name
	echo "hello $name"

- `read` 命令一个一个词组地接收输入的参数,并把输入行的每个字段的值指定给 shell 变量,每个词组需要使用空格进行分隔

	如果输入的词组个数大于需要的参数个数，则多出的词组将被作为整体为最后一个参数接收。

	- 选项说明

	 - `-p` 输入提示文字
	 - `-n` 输入字符长度限制(达到6位，自动结束)
	 - `-t` 输入限时
	 - `-s` 隐藏输入内容
	
## 9.4 显示换行

	echo -e "hello ? \n ryan!"
	
- `-e`用于开启转义，默认不转义	

- `\n`是换行符


## 9.5 显示不换行

	echo -e "OK! \c"
	echo "ryan"
	> OK! ryan
	
- `\c`表示不换行


## 9.6 输出重定向

	echo "hello ?" > fileName
	
- `>` 重定向符

## 9.7 原样输出内容（不转义，取变量）

	echo '$name \n'
	
- 单引号里的任何字符都会原样输出

## 9.8 显示命令执行结果

	echo `date`

- **这里使用的是反引号`,不是单引号**

	
# 10. printf命令

printf 命令模仿 C 程序库（library）里的 printf() 程序

	printf  format-string  [arguments...]

- 参数说明:

	1. `format-string`: 格式控制字符串

	2. `arguments`: 参数列表

- printf 由 POSIX 标准所定义，因此使用 printf 命令比使用 echo 命令在移植性方面更好

- printf 使用引用文本或空格分隔的参数，外面可以在 printf 中使用格式化字符串，还可以制定字符串的宽度、左右对齐方式等

- 默认 printf 不会像 echo 自动添加换行符，需要手动添加 `\n`


		printf "Hello, Shell\n"

实例：

	printf "%-10s %-8s %-4s\n" 姓名 性别 体重kg  
	printf "%-10s %-8s %-4.2f\n" 郭靖 男 66.1234 
	printf "%-10s %-8s %-4.2f\n" 杨过 男 48.6543

	>
	姓名     性别   体重kg
	郭靖     男      66.12
	杨过     男      48.65
	
- `%s %c %d %f`都是格式替代符

	1. `d`: Decimal 十进制整数 -- 对应位置参数必须是十进制整数，否则报错！
	
	2. `s`: String 字符串 -- 对应位置参数必须是字符串或者字符型，否则报错！
	
	3. `c`: Char 字符 -- 对应位置参数必须是字符串或者字符型，否则报错！(如果是长度大于1的字符串，自动截取字符串的第一个字符作为结果输出)
	
	4. `f`: Float 浮点 -- 对应位置参数必须是数字型，否则报错！

- `%-10s` 指一个宽度为10个字符（-表示左对齐，没有则表示右对齐），任何字符都会被显示在10个字符宽的字符内，如果不足则自动以空格填充，超过也会将内容全部显示出来。

- `%-4.2f` 指格式化为小数，其中`.2`指保留2位小数。

- `format-string`为双引号或者单引号对输出没有影响

		printf "%d %s\n" 1 "abc"
		printf '%d %s\n' 1 "abc" 

- 不使用引号也可以输出,**但是转义字符就无法使用了**

		printf %s abcdef

- 如果格式只指定了一个参数，但多出的参数仍然会按照该格式输出，format-string 被重用

		printf %s abc def

- 如果没有 arguments，那么 `%s` 用NULL代替，`%d` 用 0 代替


## 10.1 printf的转义序列

序列	|说明
---|---
`\a`	|警告字符，通常为ASCII的BEL字符
`\b`	|后退
`\c	`|抑制（不显示）输出结果中任何结尾的换行字符（只在%b格式指示符控制下的参数字符串中有效），而且，任何留在参数里的字符、任何接下来的参数以及任何留在格式字符串中的字符，都被忽略
`\f	`|换页（formfeed）
`\n	`|换行
`\r	`|回车（Carriage return）
`\t	`|水平制表符
`\v	`|垂直制表符
`\\	`|一个字面上的反斜杠字符
`\ddd`	|表示1到3位数八进制值的字符。仅在格式字符串中有效
`\0ddd`	|表示1到3位的八进制值字符

# 11. test命令
	
Shell中的 test 命令用于检查某个条件是否成立，它可以进行数值、字符和文件三个方面的测试


## 11.1 数值测试

参数	|说明|缩写
---|---|---
`-eq`	|等于则为真|equal的缩写
`-ne`	|不等于则为真|not equal的缩写
`-gt`	|大于则为真|greater than的缩写
`-ge`	|大于等于则为真|greater&equal的缩写
`-lt`	|小于则为真|lower than的缩写
`-le`	|小于等于则为真|lower&equal的缩写

示例：
	
	num1=100
	num2=100
	if test $[num1] -eq $[num2]
	then
	    echo '两个数相等！'
	else
	    echo '两个数不相等！'
	fi
	
- `[]`中可以执行基本的算数运算

		a=5
		b=6
		result=$[a+b] # 注意等号两边不能有空格
		# 下面等式与上面相同
		# result=`expr $a + $b `

	
## 11.2 字符串测试

参数	|说明
---|---
`=	`|等于则为真
`!=	`|不相等则为真
`-z` 字符串	|字符串的长度为零则为真
`-n` 字符串	|字符串的长度不为零则为真

示例：

	num1="ru1noob"
	num2="runoob"
	if test $num1 = $num2
	then
	    echo '两个字符串相等!'
	else
	    echo '两个字符串不相等!'
	fi

## 11.3 文件测试

参数	|说明
---|---
`-e` 文件名|	如果文件存在则为真
`-r` 文件名|	如果文件存在且可读则为真
`-w` 文件名|	如果文件存在且可写则为真
`-x` 文件名|	如果文件存在且可执行则为真
`-s` 文件名|	如果文件存在且至少有一个字符则为真
`-d` 文件名|	如果文件存在且为目录则为真
`-f` 文件名|	如果文件存在且为普通文件则为真
`-c` 文件名|	如果文件存在且为字符型特殊文件则为真
`-b` 文件名|	如果文件存在且为块特殊文件则为真

示例：

	cd /bin
	if test -e ./bash
	then
	    echo '文件已存在!'
	else
	    echo '文件不存在!'
	fi


另外，Shell还提供了与( `-a` )、或( `-o` )、非( `!` )三个逻辑操作符用于将测试条件连接起来，其优先级为：`"!"`最高，`"-a"`次之，`"-o"`最低

	cd /bin
	if test -e ./notFile -o -e ./bash
	then
	    echo '至少有一个文件存在!'
	else
	    echo '两个文件都不存在'
	fi


# 12. Shell 流程控制
	
Shell中的流程控制不能为空，即在`sh/bash`中如果else分支没有语句执行，就不要写这个else

- if语句经常与test命令结合使用

## 12.1 if语句

### 12.1.1 单个if

	if condition
	then
	    command1 
	    command2
	    ...
	    commandN 
	fi

示例(命令行中)：

	if [ $(ps -ef | grep -c "ssh") -gt 1 ]; then echo "true"; fi

- if语句也可以写成一行

### 12.1.2 if+else

	if condition
	then
	    command1 
	    command2
	    ...
	    commandN
	else
	    command
	fi
	
	
### 12.1.3 if+elif+else

	if condition1
	then
	    command1
	elif condition2 
	then 
	    command2
	else
	    commandN
	fi	
	
示例：

	a=10
	b=20
	if [ $a == $b ]
	then
	   echo "a 等于 b"
	elif [ $a -gt $b ]
	then
	   echo "a 大于 b"
	elif [ $a -lt $b ]
	then
	   echo "a 小于 b"
	else
	   echo "没有符合的条件"
	fi	
	
	
## 12.2 for循环

	for var in item1 item2 ... itemN
	do
	    command1
	    command2
	    ...
	    commandN
	done	
	
单行：

	for var in item1 item2 ... itemN; do command1; command2… ;done;

- 当变量值在列表里，for循环即执行一次所有命令，使用变量名获取列表中的当前取值。命令可为任何有效的shell命令和语句

- in列表可以包含替换、字符串和文件名

- in列表是可选的，如果不用它，for循环会使用命令行中传递给当前脚本的位置参数

		i=0
		for loop 
		do 
			echo $i
			i=i+1
		done

示例：

	for loop in 1 2 3 4 5
	do
	    echo "The value is: $loop"
	done


## 12.3 while 语句

while循环用于不断执行一系列命令，也用于从输入文件中读取数据；命令通常为测试条件

	while condition
	do
	    command
	done


示例：

	int=1
	while(( $int<=5 ))
	do
	    echo $int
	    let "int++"
	done
	
-  Bash let 命令，它用于执行一个或多个表达式，变量计算中不需要加上 $ 来表示变量

	[Linux let命令](http://www.runoob.com/linux/linux-comm-let.html)	
	
示例(循环读取键盘输入):	

	echo '按下 <CTRL-D> 退出'
	echo -n '输入你最喜欢的网站名: '
	while read FILM
	do
	    echo "是的！$FILM 是一个好网站"
	done
	
### 12.3.1 无限循环
三种形式可以表示无限循环：

形式 1：

	while :
	do
	    command
	done
	
形式 2：
	
	while true
	do
	    command
	done

形式 3：
	
	for (( ; ; ))


## 12.4 until 循环

until 循环执行一系列命令直至条件为 true 时停止

until 循环与 while 循环在处理方式上刚好相反

一般 while 循环优于 until 循环，但在某些时候—也只是极少数情况下，until 循环更加有用

	
	until condition
	do
	    command
	done
	
- condition 一般为条件表达式，如果返回值为 false，则继续执行循环体内的语句，否则跳出循环。


示例：

	a=0
	
	until [ ! $a -lt 10 ]
	do
	   echo $a
	   a=`expr $a + 1`
	done	

## 12.5 case

Shell case语句为多选择语句。可以用case语句匹配一个值与一个模式，如果匹配成功，执行相匹配的命令。

case语句格式如下：

	case 值 in
	模式1)
	    command1
	    command2
	    ...
	    commandN
	    ;;
	模式2）
	    command1
	    command2
	    ...
	    commandN
	    ;;
	esac

- 取值后面必须为单词in，每一模式必须以右括号结束

- 取值可以为变量或常数

- 匹配发现取值符合某一模式后，其间所有命令开始执行直至 `;;`

- 取值将检测匹配的每一个模式。一旦模式匹配，则执行完匹配模式相应命令后不再继续其他模式。如果无一匹配模式，使用星号 * 捕获该值，再执行后面的命令。


示例:

	echo '输入 1 到 4 之间的数字:'
	echo '你输入的数字为:'
	read aNum
	case $aNum in
	    1)  echo '你选择了 1'
	    ;;
	    2)  echo '你选择了 2'
	    ;;
	    3)  echo '你选择了 3'
	    ;;
	    4)  echo '你选择了 4'
	    ;;
	    *)  echo '你没有输入 1 到 4 之间的数字'
	    ;;
	esac
	
### 12.5.1 esac

case的语法和C family语言差别很大，它需要一个esac（就是case反过来）作为结束标记，每个case分支用右圆括号，用两个分号表示break。

	

## 12.6 跳出循环

在循环过程中，有时候需要在未达到循环结束条件时强制跳出循环，Shell使用两个命令来实现该功能：break和continue。

### 12.6.1 break命令

break命令允许跳出所有循环（终止执行后面的所有循环）


	while :
	do
	    echo -n "输入 1 到 5 之间的数字:"
	    read aNum
	    case $aNum in
	        1|2|3|4|5) echo "你输入的数字为 $aNum!"
	        ;;
	        *) echo "你输入的数字不是 1 到 5 之间的! 游戏结束"
	            break
	        ;;
	    esac
	done

### 12.6.2 continue命令

continue命令与break命令类似，只有一点差别，它不会跳出所有循环，仅仅跳出当前循环

	while :
	do
	    echo -n "输入 1 到 5 之间的数字: "
	    read aNum
	    case $aNum in
	        1|2|3|4|5) echo "你输入的数字为 $aNum!"
	        ;;
	        *) echo "你输入的数字不是 1 到 5 之间的!"
	            continue
	            echo "游戏结束"
	        ;;
	    esac
	done




# 13. Shell 函数

shell中函数的定义格式如下：
	
	[ function ] funname [()]
	
	{
	
	    action;
	
	    [return int;]
	
	}

- 可以带`function 函数名()` 定义，也可以直接`函数名()` 定义,不带任何参数。

- 参数返回，可以显示加：`return` 返回，如果不加，将以最后一条命令运行结果，作为返回值。 

	return后跟数值n(0-255
	
- **函数返回值在调用该函数后通过 `$?` 来获得**

- **所有函数在使用前必须定义,这意味着必须将函数放在脚本开始部分，直至shell解释器首次发现它时，才可以使用**

- **调用函数仅使用其函数名即可**


示例：

	demoFun(){
	    echo "这是我的第一个 shell 函数!"
	}
	echo "-----函数开始执行-----"
	demoFun
	echo "-----函数执行完毕-----"

示例(带return)

	funWithReturn(){
	    echo "这个函数会对输入的两个数字进行相加运算..."
	    echo "输入第一个数字: "
	    read aNum
	    echo "输入第二个数字: "
	    read anotherNum
	    echo "两个数字分别为 $aNum 和 $anotherNum !"
	    return $(($aNum+$anotherNum))
	}
	funWithReturn
	echo "输入的两个数字之和为 $? !"
	
	
## 13.1 函数参数

在Shell中，调用函数时可以向其传递参数。在函数体内部，通过 `$n` 的形式来获取参数的值

例如，$1表示第一个参数，$2表示第二个参数...

- **注意:`$10` 不能获取第十个参数，获取第十个参数需要`${10}`。当`n>=10`时，需要使用`${n}`来获取参数**



示例：
	
	funWithParam(){
	    echo "第一个参数为 $1 !"
	    echo "第二个参数为 $2 !"
	    echo "第十个参数为 $10 !"
	    echo "第十个参数为 ${10} !"
	    echo "第十一个参数为 ${11} !"
	    echo "参数总数有 $# 个!"
	    echo "作为一个字符串输出所有参数 $* !"
	}
	funWithParam 1 2 3 4 5 6 7 8 9 34 73	


## 13.2 参数中的特殊字符


参数处理	|说明
---|---
`$#	`|传递到脚本的参数个数
`$*	`|以一个单字符串显示所有向脚本传递的参数
`$$	`|脚本运行的当前进程ID号
`$!	`|后台运行的最后一个进程的ID号
`$@	`|与$*相同，但是使用时加引号，并在引号中返回每个参数。
`$-	`|显示Shell使用的当前选项，与set命令功能相同。
`$?`|	显示最后命令的退出状态。0表示没有错误，其他任何值表明有错误。


# 14. Shell 输入/输出重定向

大多数 UNIX 系统命令从终端接受输入并将所产生的输出发送回​​到终端

- 一个命令通常从一个叫**标准输入**的地方读取输入，默认情况下，这恰好是你的终端。

- 同样，一个命令通常将其输出写入到**标准输出**，默认情况下，这也是你的终端。


## 14.1 重定向命令列表

命令	|说明
---|---
`command > file`	|将输出重定向到 file。
`command < file`	|将输入重定向到 file。
`command >> file`	|将输出以追加的方式重定向到 file。
`n > file`	|将文件描述符为 n 的文件重定向到 file。
`n >> file`	|将文件描述符为 n 的文件以追加的方式重定向到 file。
`n >& m`	|将输出文件 m 和 n 合并。
`n <& m`	|将输入文件 m 和 n 合并。
`<< tag`	|将开始标记 tag 和结束标记 tag 之间的内容作为输入。


- 需要注意的是文件描述符 0 通常是标准输入（STDIN），1 是标准输出（STDOUT），2 是标准错误输出（STDERR）。

## 14.2 输出重定向

**重定向一般通过在命令间插入特定的符号来实现**

这些符号的语法如下所示:

	command1 > file1

- 上面这个命令执行command1然后将输出的内容存入file1。

- 注意:使用`>`会将任何file1内的已经存在的内容将被新内容替代。如果要将新内容添加在文件末尾，请使用`>>`操作符。


示例：

	who > users

- 上面的who命令的执行结果重定向到了`users`文件中

## 14.3 输入重定向

**和输出重定向一样，Unix 命令也可以从文件获取输入**

语法为：

	command1 < file1

- **本来需要从键盘获取输入的命令会转移到文件读取内容**

- 注意：输出重定向是大于号(>)，输入重定向是小于号(<)。


示例：

	wc -l users
	# 可以将输入重定向到`users`文件,这样就能够从文件中获取内容
	wc -l < users

- 俩个例子输出结果不同，前者会输出行数+文件名，后者仅输出行数，因为它只知道从标准输入读取内容


## 14.4 重定向深入

一般情况下，每个 Unix/Linux 命令运行时都会打开三个文件：

1. 标准输入文件(stdin)：stdin的文件描述符为0，Unix程序默认从stdin读取数据。

2. 标准输出文件(stdout)：stdout 的文件描述符为1，Unix程序默认向stdout输出数据。

3. 标准错误文件(stderr)：stderr的文件描述符为2，Unix程序会向stderr流中写入错误信息。

- 默认情况下，`command > file` 将 stdout 重定向到 file，`command < file` 将stdin 重定向到 file。

### 14.4.1 重定向stderr

stderr 重定向到 file

	command 2 > file
	
stderr 追加到 file 文件末尾	
	
	command 2 >> file
	
- 2 表示标准错误文件(stderr)	

如果希望将 stdout 和 stderr 合并后重定向到 file，可以这样写：

	command > file 2>&1
	
	或者
	
	command >> file 2>&1


### 14.4.2 同时重定向stdout和stderr

**Shell允许同时替换输入和输出：**

	command1 < infile > outfile

- command 命令将 stdin 重定向到 infile，将 stdout 重定向到 outfile


## 14.5 Here Document

Here Document 是 Shell 中的一种特殊的重定向方式，用来将输入重定向到一个交互式 Shell 脚本或程序。

	command << delimiter
	    document
	delimiter

- 它的作用是将两个 delimiter 之间的内容(document) 作为输入传递给 command。

- 结尾的delimiter 一定要顶格写，前面不能有任何字符，后面也不能有任何字符，包括空格和 tab 缩进

- 开始的delimiter前后的空格会被忽略掉


示例：

	wc -l << EOF
	    欢迎来到
	    菜鸟教程
	    www.runoob.com
	EOF
	3          # 输出结果为 3 行
	
Here Document 也可以在脚本中被使用：

	#!/bin/bash
	
	cat << EOF
	line one
	line two
	line three
	EOF	
	
## 14.6 `/dev/null`文件	

如果希望执行某个命令，但又不希望在屏幕上显示输出结果，那么可以将输出重定向到 `/dev/null`

	command > /dev/null
	
- `/dev/null` 是一个特殊的文件，写入到它的内容都会被丢弃；

- 如果尝试从该文件读取内容，那么什么也读不到。但是 `/dev/null` 文件非常有用，将命令的输出重定向到它，会起到"禁止输出"的效果。

如果希望屏蔽 stdout 和 stderr，可以这样写：

	command > /dev/null 2>&1	


# 15. Shell文件包含

Shell 可以包含外部脚本

- 这样可以很方便的封装一些公用的代码作为一个独立的文件

**Shell 文件包含的语法格式如下：**

	. filename   # 注意点号(.)和文件名中间有一空格

或

	source filename


## 15.1 示例

脚本1(`test1.sh`)

	#!/bin/bash
	
	sayhi(){
		echo "hello shell!"
	}

脚本2(`test2.sh`)

	#!/bin/bash
	
	#使用 . 号来引用test1.sh 文件
	. ./test1.sh
	
	# 或者使用以下包含文件代码
	# source ./test1.sh
	
	sayhi