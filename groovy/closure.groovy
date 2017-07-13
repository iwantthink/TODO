//Closure
//语法： { [closureParameters->] statements   },中括号可选填，但是当有参数时  -> 是必须的

//当Closure作为一个对象时
//闭包 是groovy.lang.Closure 类的一个实例，所以可以作为任何其他变量被分配
def listener  ={println it}
assert listener instanceof Closure
//如果不使用def 定义 Closure的话 ， 可以使用Closure
Closure listener2 = {->println "hi $it"}
Closure<Boolean> listener3 = {File file->
file.name.endWith('.txt')
}



//调用闭包
def code = {123}
//如果你这么定义了 Closure  你可以 通过名字+()  来调用
//或者 通过 名字.call() 来调用
// 与方法不同的是  Closure 总是会返回一个值
assert code()==123
assert code.call()==123


//Closure的参数
// 1.可选的 -类型  2.名字 3.可选的 -默认值
def closure1 = {int i=1->
     "the value of i is $i"
}
assert closure1()=='the value of i is 1'


//隐含参数
//当一个闭包没有明确定义 参数时， 会有一个隐含的参数   it 
def closure2 = {"hello $it" }
assert closure2('groovy') == 'hello groovy'


//当你明确不需要参数时 这么做
def closure3 ={->
    "hi groovy"
}



////函数的最后一个参数是闭包的时候，可以省略圆括号
//def getName(int a,int b,Closure cl){
//        cl(a,b)
//}
//
//def getAge(a,b){ println "aaaaa"}
//getAge 1,2
//getName(1,2,{a,b->println "a=$a,b=$b "})
//getName 1,2,{a,b->println "222  a=$a,b=$b"}
//

//闭包也可以使用可变参数
def vargsFunc1 = {String ... args -> args.join('')}
assert vargsFunc1('1','2','3')=='123'
//使用数组的话 也可以实现相同的功能
def vargsFunc2 = {String [] args -> args.join('') }
assert vargsFunc2('1','2','3')=='123'

// 如果除了 可变参数外 还要有参数， 那么可变参数需要放到最后
def vargsFunc3 = {int i,String... args
->
println "i=$i ,args = $args"
}
 
vargsFunc3(1,'1','2')


//owner delegate and  this
//这是存在闭包中的概念
// this 的意思
//调用getThisObject 将返回定义closure的类，相当于显式 使用this
class Enclosing{
    void run1(){
        def getObject = { getThisObject()}
        assert getObject()==this
        def getObject2 = {this}
        assert getObject2() ==this
    }
}
def  enclosing1 = new Enclosing()
enclosing1.run1()


//如果闭包在 内部类中定义， 那么会返回内部类 而不是外部类
class EnclosedInInnerClass{
    class Inner{
        Closure cl = {this}
    }
    void run(){
        def inner = new Inner()
        assert inner.cl() == inner
        assert inner.cl() != this
    }
}
def eiic = new EnclosedInInnerClass()
eiic.run()

//在嵌套闭包的情况下 ， 将返回 外部类  而不是闭包
class NestedClosure{
    void run(){
        def nestedClosures = {
            def cl = {this}
            cl()
        }
        
        assert nestedClosures()==this
    }
}



//owner的意思
//owner 会返回一个 直接闭合 含有owner闭包 的对象，无论它是 class 或者closure
class EnclosingOwner{
    void run(){
        def getOwnerMethod = { owner }
        assert getOwnerMethod()==this
        def getOwnerMethod1 = { getOwner()}
        assert getOwnerMethod1()==this
    }
}
def e1 = new EnclosingOwner()
e1.run()


class InnerOwnerClass{
    class Inner{
        Closure cl = {owner}
    }
    void run(){
        def inner = new Inner()
        assert inner.cl()==inner
    }
}
def e2 = new InnerOwnerClass()
e2.run()

class NestedClosure2{
    void run(){
        def nestedMethod = {
            def cl = {owner}
            cl()
        }
       assert nestedMethod()==nestedMethod
    }
}
def e3 = new NestedClosure2()
e3.run()



//delegate
//可以使用delegate 或者 getDelegate 来访问闭包的代理
// 默认情况下 代理设置为 owner
class Enclosing3{
    void run(){
        def func0 = {owner}
        assert func0()==this
        def func1 = {delegate}
        assert func1()==this
        def func2 = {getDelegate()}
        assert func2()==this
        
        def enclosed = {
            {-> delegate}.call()
        }
       
        def ownerMethod = {
            {->owner}.call()
        }
        //这里应该可以判断出 delegate 此时是 owner
        assert enclosed()==enclosed
        assert ownerMethod() ==ownerMethod
        
    }
}
def e4 = new Enclosing3()
e4.run()


//闭包的 代理对象是可以被设置的
class Jack{
    String name
}

class Lucy{
    String name
}
def jack = new Jack(name:'jack')
def lucy = new Lucy(name:'lucy')
def delegateClosure = { delegate.name.toUpperCase() }
delegateClosure.delegate = jack
assert delegateClosure()== 'JACK'
delegateClosure.delegate = lucy
assert delegateClosure()=='LUCY'

//在闭包中，无需明确设置delegate 即可使用delegate
class Ryan{
    String name
}
def r = new Ryan(name:'Ryan')
def r2 = {name.toUpperCase()}
r2.delegate = r
assert r2() == 'RYAN'


//delegate的 委托策略
//Closure.OWNER_FIRST  owner 优先 delegate其次  。这是默认的策略!!

class Ryan1{
    String name
}
class Ryan2{
    String name = 'Ryan2'
    def upper = {name.toUpperCase()}
//    void run(){
//        def t = {owner}
//        assert t().name==this.name
//        def r = { delegate.name.toUpperCase()}
//        r.resolveStrategy = Closure.OWNER_FIRST
//        assert r()=='RYAN2'
//        def ryan1 = new Ryan1(name:'Ryan1')
//        r.delegate = ryan1
//        assert r()=='RYAN1'
//    }
}
def ryan1 = new Ryan1(name:'Ryan1')
def ryan2 = new Ryan2()
ryan2.upper.delegate = ryan1
assert ryan2.upper()=='RYAN2'

//Closure.DELEGATE_FIRST delegate优先 owner 其次
ryan2.upper.resolveStrategy = Closure.DELEGATE_FIRST
assert ryan2.upper()=='RYAN1'

//Closure.OWNER_ONLY 仅针对 owner
//Closure.DELEGATE_ONLY  仅针对 delegate




//在Gstring 中使用Closure
// GString 只会在 创建的时候 去估值
//${x}不能代表一个 闭包 
def x =1
def gs = "x=${x}"
assert gs == 'x=1'
x = 2
assert gs !='x=2'

//如果想要在 GString 中 实现真正的闭包，例如对变量进行延迟估值 ，请使用 ${->x}
def y = 1
def gss = "y=${->y}"
assert gss =='y=1'
y=2
assert gss == 'y=2'



//curry(left)
//curry 的功能是 设置 最左侧的参数，并返回一个设置了参数之后的 闭包
def curry1 = {int a,int b->a+b}
def curry2 = curry1.curry(1)
assert curry2(1)==2
assert curry2(1)==curry1(1,1)


//Right curry
//可以设置 最右侧的参数，并返回一个设置了参数之后的闭包
def curry3 = {int a,String b-> "$b has $a kids"}
def curry4 = curry3.rcurry('lucy')
assert curry4(1)=='lucy has 1 kids'



//index based curry
//如果闭包接收多于俩个参数， 可以使用 ncurry(),去设定 指定索引位置的参数值
def curry5 = {a,b,c->a+b+c}
def curry6 = curry5.ncurry(1,1)
assert curry6(2,3)==6



// memolize
def fib
fib = { long n -> n<2?n:fib(n-1)+fib(n-2) }.memoize()
println fib(111)

//assert fib(15) == 610 // slow!



//Composition
//将一个 闭包的结果 作为另外一个闭包的 参数
def plus2 = {it+2}
def times3 = {it * 3}
def timesInPlus = plus2<<times3
assert timesInPlus(1)==5
assert timesInPlus(1)== plus2(times3(1))

def plusInTimes = plus2>>times3
assert plusInTimes(1)==9
assert plusInTimes(1)== times3(plus2(1))



//Trampoline
//递归算法通常受最大堆高度限制，例如你调用一个递归自身太多的方法，最终会收到一个 StackOverflowException
def factorial
factorial = { int n, def accu = 1G ->
    if (n < 2) return accu
    factorial.trampoline(n - 1, n * accu)
}
factorial = factorial.trampoline()

assert factorial(1)    == 1
assert factorial(3)    == 1 * 2 * 3
assert factorial(1000) // == 402387260.. plus another 2560 digits
