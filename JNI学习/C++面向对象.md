# 面向对象

[C++教程-面向对象](https://wizardforcel.gitbooks.io/w3school-cpp/content/Text/49.html)


# 简介

`c++`在c语言的基础上增加了面向对象编程,支持面向对象程序设计.


概念|描述
---|---
类成员函数|	类的成员函数是指那些把定义和原型写在类定义内部的函数，就像类定义中的其他变量一样。
类访问修饰符|	类成员可以被定义为 public、private 或 protected。默认情况下是定义为 private。
构造函数 & 析构函数|	类的构造函数是一种特殊的函数，在创建一个新的对象时调用。类的析构函数也是一种特殊的函数，在删除所创建的对象时调用。
C++ 拷贝构造函数|	拷贝构造函数，是一种特殊的构造函数，它在创建对象时，是使用同一类中之前创建的对象来初始化新创建的对象。
C++ 友元函数|	友元函数可以访问类的 private 和 protected 成员。
C++ 内联函数|	通过内联函数，编译器试图在调用函数的地方扩展函数体中的代码。
C++ 中的 this 指针|	每个对象都有一个特殊的指针 this，它指向对象本身。
C++ 中指向类的指针|	指向类的指针方式如同指向结构的指针。实际上，类可以看成是一个带有函数的结构。
C++ 类的静态成员|	类的数据成员和函数成员都可以被声明为静态的。

# 1. c++类&对象

类是c++的核心特性,通常被称为用户定义的类型

类用于指定对象的形式,包含了数据表示法和用于处理数据的方法.

类中的数据和方法 称为类的成员


## 1.1 c++类定义

类定义是以关键字 `class` 开头，后跟类的名称。类的主体是包含在一对花括号中。类定义后必须跟着一个分号或一个声明列表

	class Box {
		public :
			double length;
			double breadth;
			double height;
	}

- 关键字`public`确定了类成员的访问属性

- 在类对象作用域内,公共成员在类的外部是可访问的

- `private`,`protected`也是可以使用的修饰符


## 1.2 c++对象

声明类的对象,就像声明基本类型的变量一样


	Box box1;
	Box box2;

- 俩个对象都有各自的数据成员

## 1.3 访问数据成员

类的对象的公共数据成员可以使用直接成员访问符`(.)`来访问

	cout << " box length :" << box1.length; 

- 私有的成员和受保护的成员不能使用 直接成员访问运算符`(.)`来直接访问

## 1.4 c++ 类成员函数

类的成员函数指那些把定义和原型写在类定义内部的函数,与类定义的其他变量一样,类成员函数是类的一个成员,可以操作类的任意对象,可以访问对象中的所有成员

	class Box
	{
	   public:
	      double length;         // 长度
	      double breadth;        // 宽度
	      double height;         // 高度
	      double getVolume(void);// 返回体积
	};


成员函数可以定义在类定义内部，**或者单独使用范围解析运算符` :: `来定义**。

- 在类定义中定义的成员函数把函数声明为内联的，即便没有使用 `inline `标识符。所以可以按照如下方式定义` Volume()` 函数：

		class Box
		{
		   public:
		      double length;      // 长度
		      double breadth;     // 宽度
		      double height;      // 高度
		
		      double getVolume(void)
		      {
		         return length * breadth * height;
		      }
		};

- 在类的外部使用**范围解析运算符`::`**定义该函数

		double Box::getVolume(void)
		{
		    return length * breadth * height;
		}

## 1.5 c++ 类访问修饰符

**数据隐藏**是面向对象编程的一个重要特点,它防止函数直接访问类类型的内部成员

类成员的访问限制是通过在类主体内部对各个区域标记 `public,private,protected`来指定的

- `public,private,protected`被称为**访问说明符**

- **每个标记区域在下一个标记区域开始之前或者在遇到类主体结束右括号之前都是有效的。**

- **成员和类的默认访问修饰符是 `private`**
		
		class Base {
		
		   public:
		
		  // public members go here
		
		   protected:
		
		  // protected members go here
		
		   private:
		
		  // private members go here
		
		};

### 1.5.1 公有成员(public)

**公有成员在程序中类的外部是可访问的**。可以不借助任何成员函数来直接设置和获取公有变量的值

	cout << " box length = " << box.length;


### 1.5.2 私有成员(private)

**私有成员变量或函数在类的外部是不可访问的**，甚至是不可查看的。**只有类和友元函数可以访问私有成员。**

**默认情况下,类的所有成员都是私有的**

### 1.5.3 保护成员(protected)

保护成员变量或函数与私有成员十分相似，但有一点不同，**保护成员在派生类（即子类）中是可访问的。**

	class Box
	{
	   protected:
	      double width;
	};
	
	class SmallBox:Box // SmallBox 是派生类
	{
	   public:
	      void setSmallWidth( double wid );
	      double getSmallWidth( void );
	};

## 1.6 c++类构造函数& 析构函数

### 1.6.1 类的构造函数

类的构造函数是类的一种特殊的成员函数，**它会在每次创建类的新对象时执行**。

- **构造函数的名称与类的名称是完全相同的**，**并且不会返回任何类型**，**也不会返回 void**。

- 构造函数可用于为某些成员变量设置初始值。

		class Line
		{
		   public:
		      void setLength( double len );
		      double getLength( void );
		      Line();  // 这是构造函数
		
		   private:
		      double length;
		};
		
		// 成员函数定义，包括构造函数
		Line::Line(void)
		{
		    cout << "Object is being created" << endl;
		}

### 1.6.2 带参数的构造函数

默认的构造函数没有任何参数,自定义的构造函数可以选择带或不带参数

	class Line
	{
	   public:
	      void setLength( double len );
	      double getLength( void );
	      Line(double len);  // 这是构造函数
	
	   private:
	      double length;
	};
	
	// 成员函数定义，包括构造函数
	Line::Line( double len)
	{
	    cout << "Object is being created, length = " << len << endl;
	    length = len;
	}

	int main(){
		Line line(123);
	}

### 1.6.3 使用初始化列表来初始化字段

**语法定义:**
	
	C::C( double a, double b, double c): X(a), Y(b), Z(c)
	{
	  ....
	}
	

示例:

	Line::Line( double len): length(len)
	{
	    cout << "Object is being created, length = " << len << endl;
	}
	//上面的语法还可以用下面这种表示
	Line::Line( double len)
	{
	    cout << "Object is being created, length = " << len << endl;
	    length = len;
	}

### 1.6.4 类的析构函数

类的析构函数是类的一种特殊的成员函数，**它会在每次删除所创建的对象时执行**。

析构函数的名称与类的名称是完全相同的，只是在前面加了个波浪号`（~）`作为前缀，它不会返回任何值，也不能带有任何参数。析构函数有助于在跳出程序（比如关闭文件、释放内存等）前释放资源。

	class Line
	{
	   public:
	      void setLength( double len );
	      double getLength( void );
	      Line();   // 这是构造函数声明
	      ~Line();  // 这是析构函数声明
	
	   private:
	      double length;
	};

	Line::~Line(void)
	{
	    cout << "Object is being deleted" << endl;
	}

## 1.7 c++ 拷贝构造函数

拷贝构造函数是一种特殊的构造函数，它在创建对象时，**是使用同一类中之前创建的对象来初始化新创建的对象**。

拷贝构造函数通常用于：

- 通过使用另一个同类型的对象来初始化新创建的对象。

- 复制对象把它作为参数传递给函数。

- 复制对象，并从函数返回这个对象。

**如果在类中没有定义拷贝构造函数，编译器会自行定义一个。如果类带有指针变量，并有动态内存分配，则它必须有一个拷贝构造函数。**


拷贝构造函数的最常见形式如下：

	classname (const classname &obj) {
	   // 构造函数的主体
	}

- obj 是一个对象引用,该对象用于初始化另一个对象


示例:

	class Line
	{
	   public:
	      int getLength( void );
	      Line( int len );             // 简单的构造函数
	      Line( const Line &obj);  // 拷贝构造函数
	      ~Line();                     // 析构函数
	
	   private:
	      int *ptr;
	};

	Line::Line(const Line &obj)
	{
	    cout << "Copy constructor allocating ptr." << endl;
	    ptr = new int;
	   *ptr = *obj.ptr; // copy the value
	}

	int main(){
		Line line1(11);//调用了拷贝构造函数
		Line line2 = line1;//调用了拷贝构造函数
	}

- 对象创建时,拷贝构造函数默认会调用一次

## 1.8 c++ 友元函数

**类的友元函数是定义在类外部**，但有权访问类的所有私有（private）成员和保护（protected）成员。

- **尽管友元函数的原型有在类的定义中出现过，但是友元函数并不是成员函数。**

- 友元可以是一个函数,该函数被称为友元函数;

- 友元也可以是一个类，该类被称为友元类，在这种情况下，整个类及其所有成员都是友元。

- 通过在类定义中该函数的原型前使用关键字`friend`,来声明一个函数为一个类的友元

		class Box
		{
		   double width;
		public:
		   double length;
		   friend void printWidth( Box box );
		   void setWidth( double wid );
		};

- 通过在声明类的前面添加关键字`friend`,可以使得整个类的成员函数都称为友元

		friend  class  ClassTwo;


## 1.9 c++ 内联函数

C++ 内联函数是通常与类一起使用。**如果一个函数是内联的，那么在编译时，编译器会把该函数的代码副本放置在每个调用该函数的地方**。

对内联函数进行任何修改，都需要重新编译函数的所有客户端，因为编译器需要重新更换一次所有的代码，否则将会继续使用旧的函数。

如果想把一个函数定义为内联函数，则需要在函数名前面放置关键字 `inline`，在调用函数之前需要对函数进行定义。如果已定义的函数多于一行，编译器会忽略` inline `限定符。的情况下定义的函数多了一行。

**在类定义中的定义的函数都是内联函数，即使没有使用 `inline `说明符。**


	inline int Max(int x, int y)
	{
	   return (x > y)? x : y;
	}
	
	// 程序的主函数
	int main( )
	{
	
	   cout << "Max (20,10): " << Max(20,10) << endl;
	   cout << "Max (0,200): " << Max(0,200) << endl;
	   cout << "Max (100,1010): " << Max(100,1010) << endl;
	   return 0;
	}

	>>>>>>>>>>>>>>>>>>>>>>>

	Max (20,10): 20
	Max (0,200): 200
	Max (100,1010): 1010


## 1.10 c++ this指针

在 C++ 中，每一个对象都能通过 `this` 指针来访问自己的地址。

**`this `指针是所有成员函数的隐含参数**。因此，在成员函数内部，它可以用来指向调用对象。

友元函数没有 `this` 指针，因为友元不是类的成员。**只有成员函数才有 `this` 指针。**


	class Box
	{
	   public:
	      // 构造函数定义
	      Box(double l=2.0, double b=2.0, double h=2.0)
	      {
	         cout <<"Constructor called." << endl;
	         length = l;
	         breadth = b;
	         height = h;
	      }
	      double Volume()
	      {
	         return length * breadth * height;
	      }
	      int compare(Box box)
	      {
	         return this->Volume() > box.Volume();
	      }
	   private:
	      double length;     // Length of a box
	      double breadth;    // Breadth of a box
	      double height;     // Height of a box
	};
	
	int main(void)
	{
	   Box Box1(3.3, 1.2, 1.5);    // Declare box1
	   Box Box2(8.5, 6.0, 2.0);    // Declare box2
	
	   if(Box1.compare(Box2))
	   {
	      cout << "Box2 is smaller than Box1" <<endl;
	   }
	   else
	   {
	      cout << "Box2 is equal to or larger than Box1" <<endl;
	   }
	   return 0;
	}

	>>>>>>>>>>>>>>>>>>>>>

	Constructor called.
	Constructor called.
	Box2 is equal to or larger than Box1

## 1.11 c++ 指向类的指针

一个指向 `C++` 类的指针与指向结构的指针类似，**访问指向类的指针的成员，需要使用成员访问运算符` ->`**，就像访问指向结构的指针一样。

与所有的指针一样，您必须在使用指针之前，对指针进行初始化。


	class Box
	{
	   public:
	      // 构造函数定义
	      Box(double l=2.0, double b=2.0, double h=2.0)
	      {
	         cout <<"Constructor called." << endl;
	         length = l;
	         breadth = b;
	         height = h;
	      }
	      double Volume()
	      {
	         return length * breadth * height;
	      }
	   private:
	      double length;     // Length of a box
	      double breadth;    // Breadth of a box
	      double height;     // Height of a box
	};
	
	int main(void)
	{
	   Box Box1(3.3, 1.2, 1.5);    // Declare box1
	   Box Box2(8.5, 6.0, 2.0);    // Declare box2
	   Box *ptrBox;                // Declare pointer to a class.
	
	   // 保存第一个对象的地址
	   ptrBox = &Box1;
	
	   // 现在尝试使用成员访问运算符来访问成员
	   cout << "Volume of Box1: " << ptrBox->Volume() << endl;
	
	   // 保存第二个对象的地址
	   ptrBox = &Box2;
	
	   // 现在尝试使用成员访问运算符来访问成员
	   cout << "Volume of Box2: " << ptrBox->Volume() << endl;
	
	   return 0;
	}

	>>>>>>>>>>>>>>>>>>>>>>>

	Constructor called.
	Constructor called.
	Volume of Box1: 5.94
	Volume of Box2: 102


## 1.12 类的静态成员

可以使用 `static` 关键字来把类成员定义为静态的。

- 当声明类的成员为静态时，这意味着无论创建多少个类的对象，静态成员都只有一个副本。

**静态成员在类的所有对象中是共享的**。如果不存在其他的初始化语句，在创建第一个对象时，所有的静态数据都会被初始化为零。

**不能把静态成员放置在类的定义中**，**但是可以在类的外部通过使用范围解析运算符 ` :: `来重新声明静态变量从而对它进行初始化**，如下面的实例所示。


	class Box
	{
	   public:
	      static int objectCount;
	      // 构造函数定义
	      Box(double l=2.0, double b=2.0, double h=2.0)
	      {
	         cout <<"Constructor called." << endl;
	         length = l;
	         breadth = b;
	         height = h;
	         // 每次创建对象时增加 1
	         objectCount++;
	      }
	      double Volume()
	      {
	         return length * breadth * height;
	      }
	   private:
	      double length;     // 长度
	      double breadth;    // 宽度
	      double height;     // 高度
	};
	
	// 初始化类 Box 的静态成员
	int Box::objectCount = 0;
	
	int main(void)
	{
	   Box Box1(3.3, 1.2, 1.5);    // 声明 box1
	   Box Box2(8.5, 6.0, 2.0);    // 声明 box2
	
	   // 输出对象的总数
	   cout << "Total objects: " << Box::objectCount << endl;
	
	   return 0;
	}

- 只有静态成员变量 可以在类的外部被定义


### 1.12.1 静态函数成员

如果把函数成员声明为静态的，就可以把函数与类的任何特定对象独立开来。

- **静态成员函数即使在类对象不存在的情况下也能被调用**

- 静态函数只要使用类名加范围解析运算符 `:: `就可以访问。

- **静态成员函数只能访问静态数据成员，不能访问其他静态成员函数和类外部的其他函数。**

- 静态成员函数有一个类范围，他们不能访问类的` this `指针。

- 可以使用静态成员函数来判断类的某些对象是否已被创建。


示例:


	class Box
	{
	   public:
	      static int objectCount;
	      // 构造函数定义
	      Box(double l=2.0, double b=2.0, double h=2.0)
	      {
	         cout <<"Constructor called." << endl;
	         length = l;
	         breadth = b;
	         height = h;
	         // 每次创建对象时增加 1
	         objectCount++;
	      }
	      double Volume()
	      {
	         return length * breadth * height;
	      }
	      static int getCount()
	      {
	         return objectCount;
	      }
	   private:
	      double length;     // 长度
	      double breadth;    // 宽度
	      double height;     // 高度
	};
	
	// 初始化类 Box 的静态成员
	int Box::objectCount = 0;
	
	int main(void)
	{
	
	   // 在创建对象之前输出对象的总数
	   cout << "Inital Stage Count: " << Box::getCount() << endl;
	
	   Box Box1(3.3, 1.2, 1.5);    // 声明 box1
	   Box Box2(8.5, 6.0, 2.0);    // 声明 box2
	
	   // 在创建对象之后输出对象的总数
	   cout << "Final Stage Count: " << Box::getCount() << endl;
	
	   return 0;
	}

	>>>>>>>>>>>>>>>>>>>>>>>

	Inital Stage Count: 0
	Constructor called.
	Constructor called.
	Final Stage Count: 2

# 2. C++继承

面向对象程序设计中最重要的一个概念是继承。继承允许依据另一个类来定义一个类，这使得创建和维护一个应用程序变得更容易。这样做，也达到了重用代码功能和提高执行时间的效果。

当创建一个类时，不需要重新编写新的数据成员和成员函数，只需指定新建的类继承了一个已有的类的成员即可。**这个已有的类称为基类，新建的类称为派生类。**

继承代表`is a`关系

## 2.1 基类&派生类

一个类可以派生自多个类，这意味着，它可以从多个基类继承数据和函数。

定义一个派生类，使用一个类派生列表来指定基类。类派生列表以一个或多个基类命名，形式如下：

	class derived-class: access-specifier base-class

- `access-specifier`是访问修饰符,即`public,protected,private`其中一个

- `base-class`是之前定义过的某个类的名称

- 如果没有使用访问修饰符,默认的访问修饰符为 `private`


	// 基类
	class Shape 
	{
	   public:
	      void setWidth(int w){
	         width = w;
	      }
	      void setHeight(int h){
	         height = h;
	      }
	   protected:
	      int width;
	      int height;
	};
	
	// 派生类
	class Rectangle: public Shape
	{
	   public:
	      int getArea()
	      { 
	         return (width * height); 
	      }
	};

## 2.2 访问控制和继承

**派生类可以访问基类中所有的非私有成员**。

- 因此基类成员如果不想被派生类的成员函数访问，则应在基类中声明为 `private`



访问	|public	|protected|	private
---|---|---
同一个类	|yes	|yes|	yes
派生类	|yes	|yes|	no
外部的类	|yes	|no|	no

**一个派生类继承了所有的基类方法，但下列情况除外：**

- 基类的构造函数、析构函数和拷贝构造函数。

- 基类的重载运算符。

- 基类的友元函数。



## 2.3 继承类型

当一个类派生自基类，该基类可以被继承为 `public、protected` 或 `private` 几种类型。

继承类型是通过上面讲解的访问修饰符 `access-specifier` 来指定的。

几乎不使用 protected 或 private 继承，通常使用 public 继承。当使用不同类型的继承时，遵循以下几个规则：

- 公有继承（`public`）：当一个类派生自公有基类时，基类的公有成员也是派生类的公有成员，基类的保护成员也是派生类的保护成员，基类的私有成员不能直接被派生类访问，但是可以通过调用基类的公有和保护成员来访问。

- 保护继承（`protected`）： 当一个类派生自保护基类时，基类的公有和保护成员将成为派生类的保护成员。

- 私有继承（`private`）：当一个类派生自私有基类时，基类的公有和保护成员将成为派生类的私有成员。

## 2.4 多重继承

C++ 类可以从多个类继承成员，语法如下：

	class derived-class: access baseA, access baseB....

其中，访问修饰符 `access` 是 `public、protected 或 private` 其中的一个，用来修饰每个基类，各个基类之间用逗号分隔，如上所示。

示例:


	// 基类 Shape
	class Shape 
	{
	   public:
	      void setWidth(int w)
	      {
	         width = w;
	      }
	      void setHeight(int h)
	      {
	         height = h;
	      }
	   protected:
	      int width;
	      int height;
	};
	
	// 基类 PaintCost
	class PaintCost 
	{
	   public:
	      int getCost(int area)
	      {
	         return area * 70;
	      }
	};
	
	// 派生类
	class Rectangle: public Shape, public PaintCost
	{
	   public:
	      int getArea()
	      { 
	         return (width * height); 
	      }
	};

# 3. c++ 多态

多态按字面的意思就是多种形态。当类之间存在层次结构，并且类之间是通过继承关联时，就会用到多态。

**C++ 多态意味着调用成员函数时，会根据调用函数的对象的类型来执行不同的函数。**

	class Shape {
	   protected:
	      int width, height;
	   public:
	      Shape( int a=0, int b=0)
	      {
	         width = a;
	         height = b;
	      }
	      int area()
	      {
	         cout << "Parent class area :" <<endl;
	         return 0;
	      }
	};
	class Rectangle: public Shape{
	   public:
	      Rectangle( int a=0, int b=0):Shape(a, b) { }
	      int area ()
	      { 
	         cout << "Rectangle class area :" <<endl;
	         return (width * height); 
	      }
	};
	class Triangle: public Shape{
	   public:
	      Triangle( int a=0, int b=0):Shape(a, b) { }
	      int area ()
	      { 
	         cout << "Triangle class area :" <<endl;
	         return (width * height / 2); 
	      }
	};
	// 程序的主函数
	int main( )
	{
	   Shape *shape;
	   Rectangle rec(10,7);
	   Triangle  tri(10,5);
	
	   // 存储矩形的地址
	   shape = &rec;
	   // 调用矩形的求面积函数 area
	   shape->area();
	
	   // 存储三角形的地址
	   shape = &tri;
	   // 调用三角形的求面积函数 area
	   shape->area();
	
	   return 0;
	}

	>>>>>>>>>>>>>>>>>

	Parent class area
	Parent class area

- **导致错误输出的原因是**，调用函数 `area()` 被编译器设置为基类中的版本，这就是所谓的**静态多态**，或**静态链接 **

- 函数调用在程序执行前就准备好了。**有时候这也被称为早绑定，因为 area() 函数在程序编译期间就已经设置好了。**


**解决这个的方法就是 在`area()`的声明前放置关键字`virtual`**

- 此时编译器看的是指针的内容,而不是它的类型.因此，由于 tri 和 rec 类的对象的地址存储在 *shape 中，所以会调用各自的 area() 函数。


## 3.1 虚函数

**虚函数 是在基类中使用关键字` virtual` 声明的函数**。

- 在派生类中重新定义基类中定义的虚函数时，会告诉编译器不要静态链接到该函数。

**想要在程序中任意点可以根据所调用的对象类型来选择调用的函数，这种操作被称为动态链接，或后期绑定。**

## 3.2 纯虚函数

如果想要在基类中定义虚函数，以便在派生类中重新定义该函数更好地适用于对象，但是在基类中又不能对虚函数给出有意义的实现，这个时候就会用到纯虚函数。

	
	class Shape {
	   protected:
	      int width, height;
	   public:
	      Shape( int a=0, int b=0)
	      {
	         width = a;
	         height = b;
	      }
	      // pure virtual function
	      virtual int area() = 0;
	};

- `=0` 即告诉编译器,函数没有主体,为纯虚函数

# 4. c++ 数据抽象

数据抽象是指，只向外界提供关键信息，并隐藏其后台的实现细节，即只表现必要的信息而不呈现细节。

**数据抽象是一种依赖于接口和实现分离的编程（设计）技术。**

在 C++ 中，使用类来定义自己的抽象数据类型（ADT）。可以使用类 `ostream `的 `cout` 对象来输出数据到标准输出，如下所示：

	
	#include <iostream>
	using namespace std;
	
	int main( )
	{
	   cout << "Hello C++" <<endl;
	   return 0;
	}

- 在这个过程中,不需要理解`cout`具体是如何在用户的屏幕上显示文本,只需要知道公共接口


## 4.1 访问标签强制抽象

在 C++ 中，使用**访问标签**来定义类的抽象接口。一个类可以包含零个或多个访问标签：

- 使用公共标签定义的成员都可以访问该程序的所有部分。一个类型的数据抽象视图是由它的公共成员来定义的。

- 使用私有标签定义的成员无法访问到使用类的代码。私有部分对使用类型的代码隐藏了实现细节。

访问标签出现的频率没有限制。每个访问标签指定了紧随其后的成员定义的访问级别。指定的访问级别会一直有效，直到遇到下一个访问标签或者遇到类主体的关闭右括号为止。

## 4.2 数据抽象的实例

C++ 程序中，**任何带有公有和私有成员的类都可以作为数据抽象的实例**。请看下面的实例：

	class Adder{
	   public:
	      // 构造函数
	      Adder(int i = 0)
	      {
	        total = i;
	      }
	      // 对外的接口
	      void addNum(int number)
	      {
	          total += number;
	      }
	      // 对外的接口
	      int getTotal()
	      {
	          return total;
	      };
	   private:
	      // 对外隐藏的数据
	      int total;
	};
	int main( )
	{
	   Adder a;
	
	   a.addNum(10);
	   a.addNum(20);
	   a.addNum(30);
	
	   cout << "Total " << a.getTotal() <<endl;
	   return 0;
	}

- 上面的类把数字相加，并返回总和。公有成员 addNum 和 getTotal 是对外的接口，用户需要知道它们以便使用类。私有成员 total 是用户不需要了解的，但又是类能正常工作所必需的。

## 4.3 设计策略

抽象把代码分离为接口和实现。所以在设计组件时，必须保持接口独立于实现，这样，如果改变底层实现，接口也将保持不变。

在这种情况下，不管任何程序使用接口，接口都不会受到影响，只需要将最新的实现重新编译即可。

# 5. C++数据封装

所有的 C++ 程序都有以下两个基本要素：

- **程序语句（代码）**：这是程序中执行动作的部分，它们被称为函数。

- **程序数据**：数据是程序的信息，会受到程序函数的影响。

封装是面向对象编程中的把数据和操作数据的函数绑定在一起的一个概念，这样能避免受到外界的干扰和误用，从而确保了安全。**数据封装引申出了另一个重要的 OOP 概念，即数据隐藏。**

数据封装是一种把数据和操作数据的函数捆绑在一起的机制，数据抽象是一种仅向用户暴露接口而把具体的实现细节隐藏起来的机制。


## 5.1 设计策略

通常情况下，我们都会设置类成员状态为私有`（private）`，除非真的需要将其暴露，这样才能保证良好的封装性。

这通常应用于数据成员，但它同样适用于所有成员，包括虚函数。


# 6. C++接口(抽象类)

接口描述了类的行为和功能,不需要完成类的特定实现

**C++ 接口是使用抽象类来实现的**，抽象类与数据抽象互不混淆，数据抽象是一个把实现细节与相关的数据分离开的概念。

**如果类中至少有一个函数被声明为纯虚函数，则这个类就是抽象类。纯虚函数是通过在声明中使用 "= 0" 来指定的**，如下所示：

	class Box
	{
	   public:
	      // 纯虚函数
	      virtual double getVolume() = 0;
	   private:
	      double length;      // 长度
	      double breadth;     // 宽度
	      double height;      // 高度
	};

设计抽象类（通常称为 ABC）的目的，是为了给其他类提供一个可以继承的适当的基类。**抽象类不能被用于实例化对象**，**它只能作为接口使用**。如果试图实例化一个抽象类的对象，会导致编译错误。

因此，如果一个 ABC 的子类需要被实例化，则必须实现每个虚函数，这也意味着 C++ 支持使用 ABC 声明接口。如果没有在派生类中重载纯虚函数，就尝试实例化该类的对象，会导致编译错误。
可用于实例化对象的类被称为具体类。

## 6.1 设计策略

面向对象的系统可能会使用一个抽象基类为所有的外部应用程序提供一个适当的、通用的、标准化的接口。然后，派生类通过继承抽象基类，就把所有类似的操作都继承下来。

外部应用程序提供的功能（即公有函数）在抽象基类中是以纯虚函数的形式存在的。这些纯虚函数在相应的派生类中被实现。

这个架构也使得新的应用程序可以很容易地被添加到系统中，即使是在系统被定义之后依然可以如此。

