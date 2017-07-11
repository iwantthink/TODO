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


