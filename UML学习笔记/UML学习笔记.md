# 1.UML学习
[UML入门](http://www.jianshu.com/p/1256e2643923)  
[PlantUML-可以集成在markdown中的UML实现工具](http://www.plantuml.com/plantuml/uml/SyfFKj2rKt3CoKnELR1Io4ZDoSa70000)  
[markDown中使用UML](http://blog.csdn.net/Zhangxichao100/article/details/77774752)  

* [1.6 类图](#1.6类图)

* [2.类之间的关系-实例](#2 类之间的关系实例)

## 1.1 时序图
通过描述对象之间发送消息的时间顺序显示多个对象之间的动态协作。
时序图包括的建模元素主要有：对象（Actor）、生命线（Lifeline）、控制焦点（Focus of control）、消息（Message）等等。
[时序图介绍](http://smartlife.blog.51cto.com/1146871/284874)

![Alt text](http://g.gravizo.com/g?
    a --> b: how are you;
	note right: greeting;
	a -> a: i am thinking;
	b -> a: fine;
)

## 1.2 用例图
参与者与用例的交互。

![](http://ww1.sinaimg.cn/large/6ab93b35gy1flbor7hieij207t06zdfx.jpg)


## 1.3 活动图
可以当做流程图来使用。。
![](http://ww1.sinaimg.cn/large/6ab93b35gy1flbsm2g51aj206w07s749.jpg)

## 1.4 组件图
表示组件是如何互相组织以构建更大的组件或是软件系统。下图是Web项目的组件图。

![](http://ww1.sinaimg.cn/large/6ab93b35gy1flbsn0c4glj208w04uwed.jpg)

## 1.5 状态图
描述一个对象在其生存期间的动态行为。下图是线程的状态图。


![](http://ww1.sinaimg.cn/large/6ab93b35gy1flbsnqs2rej20ed05hjrf.jpg)

## 1.6 类图
用来描述类与类之间的关系。

### 1.6.1 访问权限控制

	- private 
	# protected 
	~ package 
	+ public 

![](http://www.plantuml.com/plantuml/png/Iyv9B2vMS2dDpQrKgERILIWeoYnBB4bLICjCpKanv5862kINf2QNfAP0X8ouwXGA4fEp4zDJ5N9JIpBoKmmrDBcq5GfAat8oaw52Ha2XMW00)

### 1.6.1 类与类之间的关系

1. 继承关系(泛化关系generalization)
	- 类的继承结构表现在UML中为：泛化(generalize)与实现(realize)：
	- 继承关系为is-a 的关系；俩个对象之间如果可以用is-a表示，就是继承关系(...是..)。eg:自行车是车，狗是动物
	- 继承关系用一条带**空心箭头**的**实线**表示,如下图:son 继承自Father
	- **注：最终代码中，泛化关系表现为继承非抽象类；**

	![](http://www.plantuml.com/plantuml/png/SqiioKWjKh2fqTLL2CxF0m00)
2. 实现
	- 实现关系用一条**带空心箭头**的**虚线**表示；
	- eg:List是一个抽象概念(理解为接口)，AbstractList实现了List这个接口(实现了接口自身也可能是抽象的)
	- **注：最终代码中，实现关系表现为继承抽象类；**
	
	![](http://www.plantuml.com/plantuml/png/IqmgBYbAJ2vHICv9B2vMS8HoVJABIxWoyqfIYz8IarCLm5mGeM1JewU7eWe0)
3. 依赖
	- 依赖关系是用一条**带箭头**的**虚线**表示的；如下图表示Human依赖于Cigaretee；
	- 用来描述一个对象在运行期间会用到另一个对象的关系；
	- 与关联关系不同的是，它是一种临时性的关系，通常在运行期间产生，并且随着运行时的变化； 依赖关系也可能发生变化；
	- 显然，依赖也有方向，双向依赖是一种非常糟糕的结构，总是应该保持单向依赖，杜绝双向依赖的产生；
	- **注：在最终代码中，依赖关系体现为类构造方法及类方法的传入参数，箭头的指向为调用关系；依赖关系除了临时知道对方外，还是“使用”对方的方法和属性；**

	![](http://www.plantuml.com/plantuml/png/yoZDJSnJqDEpKt3EJ4yiIYqfIGK0)
4. 关联
	- 关联关系是用**一条带箭头**的**直线**表示的；
	- 描述不同类的对象之间的结构关系；是一种静态关系， 通常与运行状态无关，一般由常识等因素决定的；一般用来定义对象之间静态的、天然的结构； 所以，关联关系是一种“强关联”的关系；
	- 比如，人和水之间就是一种关联关系；学生和学校就是一种关联关系；
	- 关联关系默认不强调方向，表示对象间相互知道；如果特别强调方向，如下图，表示Human知道water，但water不知道Human；
	- **注：在最终代码中，关联对象通常是以成员变量的形式实现的；**

	![](http://www.plantuml.com/plantuml/png/Iyv9B2vM24yiIItYIWQpFKfp4_EumAI2hguTH0u0)
5. 聚合
	- 聚合关系用一条**带空心菱形箭头**的**实线**表示
	- 如下图表示Human聚合到Company上，或者说Company由Human组成；
	- 聚合关系用于表示实体对象之间的关系，表示整体由部分构成的语义；例如一个公司由多个员工组成；
	- 与组合关系不同的是，整体和部分不是强依赖的，即使整体不存在了，部分仍然存在；例如， 部门撤销了，人员不会消失，他们依然存在；

	![](http://www.plantuml.com/plantuml/png/SyxFBKZCgrJ8rzLLy2ZDJSm30000)
6. 组合
	- 组合关系用一条**带实心菱形箭头**的**直线**表示
	- 如下图表示Brain组成Human，或者Human由Brain组成；
	- 与聚合关系一样，组合关系同样表示整体由部分构成的语义；比如人类由一个脑子组成；
	- 但组合关系是一种强依赖的特殊聚合关系，如果整体不存在了，则部分也不存在了；例如， 人不存在了，脑子也将不存在了；

	![](http://www.plantuml.com/plantuml/png/yoZDJSnJqDBLLN0gIipC0m00)

# 2 类之间的关系实例

![](http://ww1.sinaimg.cn/large/6ab93b35gy1flbtqe0iblj20ns0bj0t8.jpg)

- 车的类图结构为`<<abstract>>`，表示车是一个抽象类；

- 它有两个继承类：小汽车和自行车；它们之间的关系为实现关系，使用带空心箭头的虚线表示；

- 小汽车为与SUV之间也是继承关系，它们之间的关系为继承关系，使用带空心箭头的实线表示；

- 小汽车与发动机之间是组合关系，使用带实心菱形箭头的实线表示；

- 学生与班级之间是聚合关系，使用带空心菱形箭头的实线表示；

- 学生与身份证之间为关联关系，使用带箭头的实线表示；**(图中应该是有错误)**

- 学生上学需要用到自行车，与自行车是一种依赖关系，使用带箭头的虚线表示；
