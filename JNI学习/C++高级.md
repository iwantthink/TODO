# C++高级

[C++高级](https://wizardforcel.gitbooks.io/w3school-cpp/content/Text/77.html)

# 1. C++文件和流

# 2. C++异常处理

异常是指程序在执行期间产生的问题

- **C++异常是指在程序运行时发生的特殊情况,**例如尝试除以零的操作

异常提供了一种转移程序控制权的方式。**C++ 异常处理涉及到三个关键字：`try、catch、throw`。**

- `throw`: 当问题出现时，程序会抛出一个异常。这是通过使用 `throw` 关键字来完成的。

- `catch`: 在您想要处理问题的地方，通过异常处理程序捕获异常。`catch` 关键字用于捕获异常。

- `try`: `try` 块中的代码标识将被激活的特定异常。它后面通常跟着一个或多个 `catch` 块。


如果有一个块抛出一个异常，捕获异常的方法会使用 `try` 和` catch `关键字。`try `块中放置可能抛出异常的代码，`try `块中的代码被称为保护代码。

使用` try/catch `语句的语法如下所示：

	try
	{
	   // 保护代码
	}catch( ExceptionName e1 )
	{
	   // catch 块
	}catch( ExceptionName e2 )
	{
	   // catch 块
	}catch( ExceptionName eN )
	{
	   // catch 块
	}

- 如果 try 块在不同的情境下会抛出不同的异常，这个时候可以尝试罗列多个 catch 语句，用于捕获不同类型的异常。


## 2.1 抛出异常

可以使用 `throw` 语句在代码块中的任何地方抛出异常。

`throw` 语句的操作数可以是任意的表达式，表达式的结果的类型决定了抛出的异常的类型。

	double division(int a, int b)
	{
	   if( b == 0 )
	   {
	      throw "Division by zero condition!";
	   }
	   return (a/b);
	}

## 2.2 捕获异常

catch 块跟在 try 块后面，用于捕获异常。

可以指定想要捕捉的异常类型，这是由 catch 关键字后的括号内的异常声明决定的。

	try
	{
	   // 保护代码
	}catch( ExceptionName e )
	{
	  // 处理 ExceptionName 异常的代码
	}

- 上述代码会捕获一个类型为`ExceptionName`的异常,**如果想让`catch`块能够处理`try`块中抛出的任意类型的异常,则必须在异常声明的括号内使用省略号**
		
		try
		{
		   // 保护代码
		}catch(...)
		{
		  // 能处理任何异常的代码
		}

## 2.3 C++标准的异常

C++ 提供了一系列标准的异常，定义在 `<exception>` 中，可以在程序中使用这些标准的异常。它们是以父子类层次结构组织起来的，如下所示：


![](https://wizardforcel.gitbooks.io/w3school-cpp/content/image/cpp_exceptions.jpg)


异常	|描述
---|---
std::exception|	该异常是所有标准 C++ 异常的父类。
std::bad_alloc|	该异常可以通过 new 抛出。
std::bad_cast|	该异常可以通过 dynamic_cast 抛出。
std::bad_exception|	这在处理 C++ 程序中无法预期的异常时非常有用。
std::bad_typeid|	该异常可以通过 typeid 抛出。
std::logic_error|	理论上可以通过读取代码来检测到的异常。
std::domain_error|	当使用了一个无效的数学域时，会抛出该异常。
std::invalid_argument|	当使用了无效的参数时，会抛出该异常。
std::length_error|	当创建了太长的 std::string 时，会抛出该异常。
std::out_of_range|	该异常可以通过方法抛出，例如 std::vector 和 std::bitset<>::operator。
std::runtime_error|	理论上不可以通过读取代码来检测到的异常。
std::overflow_error|	当发生数学上溢时，会抛出该异常。
std::range_error|	当尝试存储超出范围的值时，会抛出该异常。
std::underflow_error|	当发生数学下溢时，会抛出该异常。

## 2.4 定义新的异常

您可以通过继承和重载 `exception` 类来定义新的异常。

下面的实例演示了如何使用 std::exception 类来实现自己的异常：


	struct MyException : public exception
	{
	  const char * what () const throw ()
	  {
	    return "C++ Exception";
	  }
	};
	
	int main()
	{
	  try
	  {
	    throw MyException();
	  }
	  catch(MyException& e)
	  {
	    std::cout << "MyException caught" << std::endl;
	    std::cout << e.what() << std::endl;
	  }
	  catch(std::exception& e)
	  {
	    //其他的错误
	  }
	}
	>>>>>>>>>>>>>>>>>>>>>>>>>>>
	MyException caught
	C++ Exception

- 在这里，`what()` 是异常类提供的一个公共方法，它已被所有子异常类重载。这将返回异常产生的原因。


# 3. C++动态内存

**C++ 程序中的内存分为两个部分：**

- 栈: 在函数内部声明的所有变量都将占用栈内存

- 堆: 这是程序中未使用的内存,在程序运行时可用于动态分配内存

	很多时候，无法提前预知需要多少内存来存储某个定义变量中的特定信息，所需内存的大小需要在运行时才能确定。

	**在 C++ 中，可以使用特殊的运算符为给定类型的变量在运行时分配堆内的内存，这会返回所分配的空间地址。这种运算符即 `new` 运算**符。

	**如果不再需要动态分配的内存，可以使用 `delete `运算符，删除之前由 `new` 运算符分配的内存。**

## 3.1 new和delete运算符

`new`运算符 为任意的数据类型动态分配内存的通用语法:

	new data-type;

- `data-type`可以是包括数组在内的任意内置的数据类型,也可以是包括类或结构在内的用户自定义的任何数据类型

定义一个指向 double 类型的指针，然后请求内存，该内存在执行时被分配。可以按照下面的语句使用 new 运算符来完成这点
	
	double* pvalue  = NULL; // 初始化为 null 的指针
	pvalue  = new double;   // 为变量请求内存


如果自由存储区已被用完，可能无法成功分配内存。所以建议检查 `new` 运算符是否返回 `NULL `指针，并采取以下适当的操作：

	double* pvalue  = NULL;
	if( !(pvalue  = new double ))
	{
	   cout << "Error: out of memory." <<endl;
	   exit(1);
	
	}


`malloc()` 函数在 C 语言中就出现了，在 C++ 中仍然存在，但建议尽量不要使用 `malloc()` 函数。`new` 与` malloc() `函数相比，其主要的优点是，**`new` 不只是分配了内存，它还创建了对象。**

---

**在任何时候，当觉得某个已经动态分配内存的变量不再需要使用时，可以使用 delete 操作符释放它所占用的内存，如下所示：**

	delete pvalue;        // 释放 pvalue 所指向的内存

示例:

	int main ()
	{
	   double* pvalue  = NULL; // 初始化为 null 的指针
	   pvalue  = new double;   // 为变量请求内存
	
	   *pvalue = 29494.99;     // 在分配的地址存储值
	   cout << "Value of pvalue : " << *pvalue << endl;
	
	   delete pvalue;         // 释放内存
	
	   return 0;
	}

## 3.2 数组的动态内存分配

假设要为一个字符数组（一个有 20 个字符的字符串）分配内存，可以使用上面实例中的语法来为数组动态地分配内存，如下所示
	
	char* pvalue  = NULL;   // 初始化为 null 的指针
	pvalue  = new char[20]; // 为变量请求内存

删除语句如下:

	delete [] pvalue;        // 删除 pvalue 所指向的数组



---

下面是 new 操作符的通用语法，可以为多维数组分配内存，如下所示：

	double** pvalue  = NULL;     // 初始化为 null 的指针
	pvalue  = new double [3][4]; // 为一个 3x4 数组分配内存

释放多维数组内存的语法与二维数组一样：

	delete [] pvalue;        // 删除 pvalue 所指向的数组


## 3.3 对象的动态内存分配

**对象与简单的数据类型没有什么不同**。

示例:

	class Box
	{
	   public:
	      Box() { 
	         cout << "调用构造函数！" <<endl; 
	      }
	      ~Box() { 
	         cout << "调用析构函数！" <<endl; 
	      }
	};
	
	int main( )
	{
	   Box* myBoxArray = new Box[4];
	
	   delete [] myBoxArray; // Delete array
	
	   return 0;
	}

	>>>>>>>>>>>>>>>>>>>>>

	调用构造函数！
	调用构造函数！
	调用构造函数！
	调用构造函数！
	调用析构函数！
	调用析构函数！
	调用析构函数！
	调用析构函数！

- **如果要为一个包含四个 Box 对象的数组分配内存，构造函数将被调用 4 次，同样地，当删除这些对象时，析构函数也将被调用相同的次数（4次）**

# 4. C++命名空间

命名空间,可作为附加信息来区分不同库中相同名称的函数,类,变量等

- **使用了命名空间即定义了上下文,本质上,命名空间就是定义了一个范围**

## 4.1 定义命名空间

命名空间的定义使用关键字`namespace`,后跟命名空间的名称:
	
	namespace namespace_name {
	   // 代码声明
	}

- `namespace_name` 即命名空间的名称

调用指定命名空间的函数或变量,需要在其前面加上命名空间的名称

	name::code;  // code 可以是变量或函数


示例: 
	
	#include <iostream>
	using namespace std;
	
	// 第一个命名空间
	namespace first_space{
	   void func(){
	      cout << "Inside first_space" << endl;
	   }
	}
	// 第二个命名空间
	namespace second_space{
	   void func(){
	      cout << "Inside second_space" << endl;
	   }
	}
	int main ()
	{
	
	   // 调用第一个命名空间中的函数
	   first_space::func();
	
	   // 调用第二个命名空间中的函数
	   second_space::func(); 
	
	   return 0;
	}

	>>>>>>>>>>>>>>>>>>>>>

	Inside first_space 
	Inside second_space

## 4.2 using指令

可以使用` using namespace `指令，**这样在使用命名空间时就可以不用在前面加上命名空间的名称**。

- **这个指令会告诉编译器，后续的代码将使用指定的命名空间**中的名称。

示例:

	#include <iostream>
	using namespace std;
	
	// 第一个命名空间
	namespace first_space{
	   void func(){
	      cout << "Inside first_space" << endl;
	   }
	}
	// 第二个命名空间
	namespace second_space{
	   void func(){
	      cout << "Inside second_space" << endl;
	   }
	}
	using namespace first_space;
	int main ()
	{
	
	   // 调用第一个命名空间中的函数
	   func();
	
	   return 0;
	}
	

**`using` 指令也可以用来指定命名空间中的特定项目**。例如，如果只打算使用 `std` 命名空间中的`cout` 部分，可以使用如下的语句：

	using std::cout;

- 随后的代码中，在使用 `cout `时就可以不用加上命名空间名称作为前缀，**但是 std 命名空间中的其他项目仍然需要加上命名空间名称作为前缀**


`using` 指令引入的名称遵循正常的范围规则。名称从使用` using `指令开始是可见的，直到该范围结束。此时，在范围以外定义的同名实体是隐藏的。


## 4.3 不连续的命名空间

**命名空间可以定义在几个不同的部分中**，因此命名空间是可以由几个单独定义的部分组成的。

一个命名空间的各个组成部分可以分散在多个文件中。

如果命名空间中的某个组成部分需要请求定义在另一个文件中的名称，则仍然需要声明该名称。

下面的命名空间定义可以是定义一个新的命名空间，也可以是为已有的命名空间增加新的元素：

	namespace namespace_name {
	   // 代码声明
	}

## 4.4 嵌套的命名空间

命名空间可以嵌套，可以在一个命名空间中定义另一个命名空间，如下所示：

	namespace namespace_name1 {
	   // 代码声明
	   namespace namespace_name2 {
	      // 代码声明
	   }
	}

**可以通过使用` :: `运算符来访问嵌套的命名空间中的成员**

	// 访问 namespace_name2 中的成员
	using namespace namespace_name1::namespace_name2;
	
	// 访问 namespace:name1 中的成员
	using namespace namespace_name1;

- 上面的语句中，如果使用的是 `namespace_name1`，那么在该范围内` namespace_name2` 中的元素也是可用的

		#include <iostream>
		using namespace std;
		
		// 第一个命名空间
		namespace first_space{
		   void func(){
		      cout << "Inside first_space" << endl;
		   }
		   // 第二个命名空间
		   namespace second_space{
		      void func(){
		         cout << "Inside second_space" << endl;
		      }
		   }
		}
		using namespace first_space::second_space;
		int main ()
		{
		
		   // 调用第二个命名空间中的函数
		   func();
		
		   return 0;
		}

		>>>>>>>>>>>>>>>>>

		Inside second_space


# 5. C++模板

**模板是泛型编程的基础，泛型编程即以一种独立于任何特定类型的方式编写代码。**

模板是创建泛型类或函数的蓝图或公式。

- 库容器，比如迭代器和算法，都是泛型编程的例子，它们都使用了模板的概念。

- 每个容器都有一个单一的定义，比如 向量，我们可以定义许多不同类型的向量，比如` vector <int> `或 `vector <string>`。


可以使用模板来定义函数和类

## 5.1 函数模板

模板函数定义的一般形式如下所示：

	template <class type> ret-type func-name(parameter list)
	{
	   // 函数的主体
	}

- `type` 是函数所使用的数据类型的占位符名称.这个名称可以在函数定义中使用

示例:


	#include <iostream>
	#include <string>
	
	using namespace std;
	
	template <typename T>
	inline T const& Max (T const& a, T const& b) 
	{ 
	    return a < b ? b:a; 
	} 

	int main ()
	{
	
	    int i = 39;
	    int j = 20;
	    cout << "Max(i, j): " << Max(i, j) << endl; 
	
	    double f1 = 13.5; 
	    double f2 = 20.7; 
	    cout << "Max(f1, f2): " << Max(f1, f2) << endl; 
	
	    string s1 = "Hello"; 
	    string s2 = "World"; 
	    cout << "Max(s1, s2): " << Max(s1, s2) << endl; 
	
	   return 0;
	}

	>>>>>>>>>>>>>

	Max(i, j): 39
	Max(f1, f2): 20.7
	Max(s1, s2): World

## 5.2 类模板

泛型类声明的一般形式如下所示

	template <class type> class class-name {
	.
	.
	.
	}

- `type` 是占位符类型名称，可以在类被实例化的时候进行指定。

- 可以使用一个逗号分隔的列表来定义多个泛型数据类型

# 6. C++预处理器

**预处理器是一些指令，指示编译器在实际编译之前所需完成的预处理。**

所有的预处理器指令都是以井号`（#）`开头，**只有空格字符可以出现在预处理指令之前**。

- 预处理指令不是 C++ 语句，所以它们不会以分号（;）结尾。

我们已经看到，之前所有的实例中都有 `#include `指令。

- 这**个宏用于把头文件包含到源文件中。**

C++ 还支持很多预处理指令，比如 `#include、#define、#if、#else、#line `等


## 6.1 #define 预处理

`#define` **预处理指令用于创建符号常量**。

- **该符号常量通常称为宏**

指令的一般形式是：

	#define macro-name replacement-text

- 当这一行代码出现在一个文件中时，在该文件中后续出现的所有宏都将会在程序编译之前被替换为 replacement-text。例如：


	using namespace std;
	
	#define PI 3.14159
	
	int main ()
	{
	
	    cout << "Value of PI :" << PI << endl;
	
	    return 0;
	}



### 6.2.1 函数宏

可以使用 `#defin`e 来定义一个带有参数的宏，如下所示：

	#include <iostream>
	using namespace std;
	
	#define MIN(a,b) (((a)<(b)) ? a : b)
	
	int main ()
	{
	   int i, j;
	   i = 100;
	   j = 30;
	   cout <<"The minimum is " << MIN(i, j) << endl;
	
	    return 0;
	}

	>>>>>>>>>>>>>

	The minimum is  30

## 6.2 条件编译

有几个指令可以**用来有选择地对部分程序源代码进行编译**。

- 这个过程被称为**条件编译**。

条件预处理器的结构与 `if` 选择结构很像。请看下面这段预处理器的代码：

	#ifndef NULL
	   #define NULL 0
	#endif



可以只在调试时进行编译，调试开关可以使用一个宏来实现，如下所示：

	#ifdef DEBUG
	   cerr <<"Variable x = " << x << endl;
	#endif

- 如果在指令 `#ifdef DEBUG` 之前已经定义了符号常量 `DEBUG`，则会对程序中的 `cerr` 语句进行编译。

- 可以使用 `#if 0 `语句注释掉程序的一部分，如下所示：

		#if 0
		   不进行编译的代码
		#endif

示例:

	#include <iostream>
	using namespace std;

	#define DEBUG
	
	#define MIN(a,b) (((a)<(b)) ? a : b)
	
	int main ()
	{
	   int i, j;
	   i = 100;
	   j = 30;
	#ifdef DEBUG
	   cerr <<"Trace: Inside main function" << endl;
	#endif
	
	#if 0
	   /* 这是注释部分 */
	   cout << MKSTR(HELLO C++) << endl;
	#endif
	
	   cout <<"The minimum is " << MIN(i, j) << endl;
	
	#ifdef DEBUG
	   cerr <<"Trace: Coming out of main function" << endl;
	#endif
	    return 0;
	}

	>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	Trace: Inside main function
	The minimum is 30
	Trace: Coming out of main function


## 6.3 # 和 ## 运算符

`#` 和 `##` 预处理运算符在 C++ 和 ANSI/ISO C 中都是可用的。

- `# `运算符会把 `replacement-text` 令牌转换为用引号引起来的字符串。

示例: 

	#include <iostream>
	using namespace std;
	
	#define MKSTR( x ) #x
	
	int main ()
	{
	    cout << MKSTR(HELLO C++) << endl;
	
	    return 0;
	}

	>>>>>>>>>>>>

	HELLO C++

- C++ 预处理器 将 `MKSTR(HELLO C++)` 替换成了 `"HELLO C++"`

		cout << MKSTR(HELLO C++)  << endl;
	
		cout <<  "HELLO C++"  << endl;

----


`##` 运算符用于连接两个令牌。下面是一个实例：

	#define CONCAT( x, y ) x ## y

- 当` CONCAT `出现在程序中时，它的参数会被连接起来，并用来取代宏。

例如，程序中 `CONCAT(HELLO, C++)` 会被替换为 `"HELLO C++"`，如下面实例所示。

	#include <iostream>
	using namespace std;
	
	#define concat(a, b) a ## b
	int main()
	{
	   int xy = 100;
	
	   cout << concat(x, y);
	   return 0;
	}

	>>>>>>>>>>

	100

- 处理方式:
		
		cout << concat(x, y);
		
		cout << xy;



## 6.4 C++中的预处理宏

宏	|描述
---|---
`__LINE__`|	这会在程序编译时包含当前行号。
`__FILE__`|	这会在程序编译时包含当前文件名。
`__DATE__`|	这会包含一个形式为 month/day/year 的字符串，它表示把源文件转换为目标代码的日期。
`__TIME__`|	这会包含一个形式为 hour:minute:second 的字符串，它表示程序被编译的时间。


示例:

	#include <iostream>
	using namespace std;
	
	int main ()
	{
	    cout << "Value of __LINE__ : " << __LINE__ << endl;
	    cout << "Value of __FILE__ : " << __FILE__ << endl;
	    cout << "Value of __DATE__ : " << __DATE__ << endl;
	    cout << "Value of __TIME__ : " << __TIME__ << endl;
	
	    return 0;
	}

	>>>>>>>>>>>>>>>>>>>>>>

	Value of __LINE__ : 6
	Value of __FILE__ : test.cpp
	Value of __DATE__ : Feb 28 2011
	Value of __TIME__ : 18:52:48


# 7. C++信号处理

信号是由操作系统传给进程的中断，会提早终止一个程序。在 UNIX、LINUX、Mac OS X 或 Windows 系统上，可以通过按 Ctrl+C 产生中断。

有些信号不能被程序捕获，但是下表所列信号可以在程序中捕获，并可以基于信号采取适当的动作。**这些信号是定义在 C++ 头文件 <csignal> 中**。


信号|描述
---|---
SIGABRT|	程序的异常终止，如调用 abort。
SIGFPE|	错误的算术运算，比如除以零或导致溢出的操作。
SIGILL|	检测非法指令。
SIGINT|	接收到交互注意信号。
SIGSEGV|	非法访问内存。
SIGTERM|	发送到程序的终止请求。


# 8. C++多线程

多线程是多任务处理的一种特殊形式，多任务处理允许让电脑同时运行两个或两个以上的程序。一般情况下，两种类型的多任务处理：基于进程和基于线程。

- 基于进程的多任务处理是程序的并发执行。

- 基于线程的多任务处理是同一程序的片段的并发执行。

多线程程序包含可以同时运行的两个或多个部分。这样的程序中的每个部分称为一个线程，每个线程定义了一个单独的执行路径。

当前文章基于`Linux`操作系统,使用`POSIX`编写多线程

- `POSIX Threads` 或 Pthreads 提供的 API 可在多种类 Unix POSIX 系统上可用，比如 FreeBSD、NetBSD、GNU/Linux、Mac OS X 和 Solaris。


## 8.1 创建线程

	#include <pthread.h>
	pthread_create (thread, attr, start_routine, arg) 


参数|	描述
---|---
thread|	指向线程标识符指针。
attr|	一个不透明的属性对象，可以被用来设置线程属性。您可以指定线程属性对象，也可以使用默认值 NULL。
start_routine|	线程运行函数起始地址，一旦线程被创建就会执行。
arg|	运行函数的参数。它必须通过把引用作为指针强制转换为 void 类型进行传递。如果没有传递参数，则使用 NULL。