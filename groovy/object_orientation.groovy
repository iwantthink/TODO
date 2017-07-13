class Foo{
    static int i
}

assert Foo.i.class == Integer.class
assert Foo.class.getDeclaredField('i').type ==int.class
assert Foo.i.class !=int.class

//class
//public field 会自动转换成属性，没有过多的get set
// 声明 属性 方法 如果没有指定访问修饰符 默认都是public
// 一个文件可以包含多个类 ，但如果一个.groovy没有任何类，它就会被当做ｓｃｒｉｐｔ

class Person{
    String name
    Integer age
    
    def increaseAge(Integer years){
        this.age+=years
    }
}


//normal class
// 可以在任何其他类 或者脚本下进行实例化



//inner class
//官方文档好像叫封闭类？ 外部类（封闭类）
// 外部类可以使用内部类 ，内部类可以使用外部类的成员 即使是私有的， 除了外部类之外的类不允许访问内部类
// 使用内部类的一些原因
// 通过内部类的形式隐藏在其他类中 增加封装，不需要知道具体的实现。 更加的高内聚 低耦合
// 如果内部类 仅被使用了一次  可以考虑使用匿名内部类
class Outer2{
    private String privateStr = 'some thing'
    
    def startThread(){
        new Thread(new Inner2()).start()
    }
    class Inner2 implements Runnable{
        void run(){
            println "${privateStr}"
        }
    }
}



//abstract class
//抽象类 必须使用abstract 关键字声明
//抽象方法 必须使用abstract 关键字声明  与java类似  可以有 抽象和非抽象的方法共存，
abstract class AbstractClass{
    String name
    
    abstract def absMethod()
}



//interface
//接口中的方法总是公开的， 所以不能使用private protected 进行修饰 
interface Greeter{
    void greet(String name)
}



//构造函数
//位置参数构造函数 》》》即已经声明了构造函数
//使用声明的构造函数有三种方式
class PersonConstruct{
    PersonConstruct(String naem){
    }
}
def p1 = new PersonConstruct('name')
def p2 = ['name'] as PersonConstruct
PersonConstruct p3 = ['name']

//命名参数构造函数,即 没有声明构造函数
//没有声明构造函数，可以使用map(key-value)的形式来传递参数用来创对象，这样可以随意的选取参数组合 并且不用再定义构造函
class PersonWOConstruct{
    String name
    Integer age
}
def pwo1 = new PersonWOConstruct()
def pwo2 = new PersonWOConstruct(name:'jack')
def pwo3 = new PersonWOConstruct(name:'jack',age:22)



//Method 
//一个方法 如果用def 来进行声明  代表可以返回任何类型的值
def func1(){
    println 'hello groovy'
}
def re = func1()
assert re ==null
//一个方法可以接收任何数量的 未经声明类型的参数
// 默认方法 为public

def foo1(Map args){
    println "$args.name     $args.value"
}
foo1(name:'jack',value:123)

// 默认参数
def foo2(param1,param2 = 2,param3){
    println "param1 = $param1"
    println "param2 = $param2"
    println "param3 = $param3"
}
//当参数数量不足时， 会优先将参数 提供给 没有默认值的
foo2(1000,1);


//可变参数
def foo3(Object... args){
    args.length
}

assert foo3(1,2,3) ==3
assert foo3(1) == 1
assert foo3() == 0
// 数组 也可以被认定为 可变参数
def foo4(Object [] args){
    args.length
}
assert foo4() == 0
assert foo4(1) == 1
assert foo4(1, 2) == 2

//如果 同时定义了 可变参数 ， 又定义了 指定数量的参数的 方法 ，会优先以 指定数量的为准
def foo6(Object... args){
        1
}
def foo6(args){
        2
}
assert foo6()==1
assert foo6(1)==2




//field
//访问修饰符 public  protected   private
//static final synchronized
//一个字段可以在被声明时  直接初始化
class Practice{
    private String name = next()
    
    def next(){
            111
    }
}

def cl = new Practice()
println cl.name
// 建议 字段 在声明的时候 指定 类型，虽然说 可以省略掉 这个 类型的声明




//property
//属性是 private  和 get/set 的组合
//属性 不能有 public protected private等修饰符
//可以选择 static final synchronized
//Groovy会适当生成 set/get
class Person2{
    String name //会创建出一个private String name ,和一个 setter 和一个 getter
    String city
}
//被final 修饰时， 不会生成setter  但是会有 getter
class Person3{
    final String name = 'jack'
}
def pp1 = new Person3()
assert pp1.name =='jack'
assert pp1.getName() == 'jack'
class Person4{
    String name
    String age
    
   void name(String name){
       this.name = name
   }
   
   String getName(){
       this.name
   }
}
def pp2 = new Person4()
assert  pp2.properties.keySet().containsAll(['name','age'])
//默认使用类中属性  将隐式调用get /set，但是如果在方法中  是直接读取字段
pp2.name('jack') //读取字段
assert pp2.name =='jack'// 隐式调用 get


//Groovy 会识别属性， 即使没有提供字段 ，但是只要有 遵循 java bean  和  get/set 的方法 ，就可以直接使用属性
//如果是有 set  那就 只写   如果只有get 就 只读
class Example1{

    private String name

    void setName(String name){
        this.name = name
    }
    
//    String getName(){
//        "my name is$name"
//    }
}

def ex1 = new Example1()
ex1.name ='jack'
assert ex1.name=='jack'






//Annotation 注解  
// 使用 @interface 关键字
@interface SomeAnnotation{}
//注解 可以添加 没有具体实现的方法 和  有默认值的 方法
//成员类型 可以是 原始类型，字符串 ，类，一个枚举，另一种注解类型，或者上述任何的数组形式
@interface SomeAnnotation1{
    String value() default 'groovy'
}
//注解位置
//注解可以应用于代码的各种元素
@SomeAnnotation
void func11(){}
@SomeAnnotation
class SomeClass1{}
@SomeAnnotation
String str1
//可以通过 @Target 元注解 来指定注解范围
import java.lang.annotation.ElementType
import java.lang.annotation.Target
@Target([ElementType.METHOD])
@interface SomeAnnotation11{}

//注解值
//在使用注解时，至少得替那些没有默认值的 成员设置值
@Target([ElementType.METHOD])
@interface Page{
    int statusCode() default 100
    String value()
}
@Page(value = 'jack')
def sayHi(){}
//如果只存在一个 需要设置值的 成员时，可以省略= 之前的部分
@Page('jack')
void sayHello(){}



//注解可见性
//通过元注解@Retention 进行设置
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
@Retention(RetentionPolicy.RUNTIME)
@interface SomeAnnotation4{
    String status()
}


//闭包可以作为 注解的参数, 但是 需要你将成员的类型设置成为 Class
// 当前Groovy 是2.4.7  貌似还没有这个特性， 应该是在 这之后 添加的。。 具体的没看  
//class Example3{
//    @SomeAnnotation4({6})
//    void sayHello1(){}
//}




//元注解 meta-annotations
//使用元注解  可以用来修饰 注解， 在编译时 这个注解 会被替换

@interface Service{}
@interface Transactional{}

@Service
@Transactional
@interface Combination{}

@Combination()
class Example5{
}

def annotations = Example5.annotations*.annotationType()
assert !(Service in annotations)

//TODO。。。。。。。。。。还有很多杂七杂八的玩意   暂时用不到 用到再学。。。



