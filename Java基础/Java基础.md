# 类之间的关系

- 依赖`uses-a`
- 聚合`has-a`
- 继承`is-a`



# 调用构造器的具体处理步骤：

1. 所有数据域被初始化为默认值
2. 按照在类声明中出现的次序，依次执行所有域初始化语句和初始化块
3. 如果构造器第一行调用了第二个构造器，则执行第二个构造器主体
4. 执行这个构造器的主体


# 1.Lambda 表达式

- lambda表达式 是一个可传递的代码块

## 1.1 lambda表达式的语法
语法：**参数，箭头(->),表达式**

- 如果代码要完成的计算无法放在一个表达式中，那么可以像写方法一样 将这些代码放到`{}`中，并包含显示的return语句，例如:

		(String first,String seconed)->
			{
				return Integer.compare(first.length(),seconed.length());
			}

- 在不需要参数时，参数外的`()`括号是必须提供的

- 如果编译器可以推导出 参数的类型，那么参数类型也可以省略。同样的适用于返回值

## 1.2 函数式接口

对于**只有一个抽象方法的接口，称为函数式接口**。针对这种函数式接口，可以使用lambda表达式进行替换。

- 实际上，在Java se 8开始 接口中可以存在非抽象的方法(default声明的默认方法)

- Java API 在java.util.function包中定义了许多通用的函数式接口,可以将lambda表达式保存在这个类型的变量中。

	- 想要使用保存lambda表达式的函数式接口做某些处理，还需要谨记表达式的用途，**并创建一个特定的函数式接口**

	例如：ArrayList存在一个removeIf方法，需要一个`Predicate`，这个接口就是专门用来传递lambda表达式

			Predicate<String> predicate = (e) -> {
				return e == null;
			};
	
			ArrayList list = new ArrayList();
			list.removeIf(predicate);

## 1.3 方法引用
如果需要将现成的方法传递到其他代码的某个动作

语法：**使用`::`操作符分隔方法名与对象或类名**，主要有三种情况：

- object::instanceMethod
- Class::staticMethod
- Class::instanceMethod

**方法引用所使用参数，取决于上下文！！**。

前俩种情况中，方法引用等价于提供方法参数的lambda表达式。例如：

	System.out::println 等价于 x->System.out.println(x)
	Math::pow 等价于 (x,y)->{Math.pow(x,y);}

针对最后一种情况，第一个参数会成为方法的目标。例如：

	String::compareToIgnoreCase 等同于 (x,y)->{x.compareToIgnoreCase(y);}

- 如果存在多个同名的重载方法，编译器会尝试从上下文中找出指定的那个方法。例如，Math.max 有俩个版本 一个作用于整数，另一个作用于double值，那么选择哪一个版本取决于`Math::max`转换为哪个函数式接口的方法参数。

- 类似lambda表达式，方法引用不能独立存在，总是会转换为函数式接口的实例

- 可以在方法引用中使用`this`参数或`super`参数。例如

		this::equals 等同于 x->this.equals(x)

## 1.4 构造器引用
构造器引用于方法引用相似，只不过方法名为`new`;例如:

	Person::new 

具体使用的构造器取决于上下文

- 可以使用数组类型创建构造器引用，例如

	int[]::new 等价于 x->new int[x]


## 1.5 变量作用域

lambda表达式有三个部分：

1. 一个代码块
2. 参数
3. 自由变量,这是指非参数而且不在代码中定义的变量

- 关于代码块以及自由变量值有一个术语：闭包(Closure).在java中,lambda表达式就是闭包

- lambda表达式可以捕获外围作用域中变量的值，但是要确保锁捕获的值是明确定义的，即只能引用值不会改改变的变量，**无论是lambda表达式内部改变还是在外部改变**

- lambda中声明一个与局部变量同名的 **参数或lambda表达式的局部变量** 是不合法的

- 在lambda表达式中使用`this`关键词，是指创建这个`lambda`表达式的方法的`this`参数


## 1.6 处理lambda表达式

本小节介绍：**编写方法处理lambda表达式**!

 使用lambda表达式的重点是**延迟执行**

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fn9cuql6o3j20o30aegqi.jpg)

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fn9d03nueuj20ot0aon15.jpg)






# 2.泛型
类型参数:`ArrayList<String> list`

一个泛型类就是具有一个或多个类型变量的类

泛型类，泛型方法

通配符 概念

 