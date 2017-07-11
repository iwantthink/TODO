//Closure
//�﷨�� { [closureParameters->] statements   },�����ſ�ѡ����ǵ��в���ʱ  -> �Ǳ����

//��Closure��Ϊһ������ʱ
//�հ� ��groovy.lang.Closure ���һ��ʵ�������Կ�����Ϊ�κ���������������
def listener  ={println it}
assert listener instanceof Closure
//�����ʹ��def ���� Closure�Ļ� �� ����ʹ��Closure
Closure listener2 = {->println "hi $it"}
Closure<Boolean> listener3 = {File file->
file.name.endWith('.txt')
}



//���ñհ�
def code = {123}
//�������ô������ Closure  ����� ͨ������+()  ������
//���� ͨ�� ����.call() ������
// �뷽����ͬ����  Closure ���ǻ᷵��һ��ֵ
assert code()==123
assert code.call()==123


//Closure�Ĳ���
// 1.��ѡ�� -����  2.���� 3.��ѡ�� -Ĭ��ֵ
def closure1 = {int i=1->
     "the value of i is $i"
}
assert closure1()=='the value of i is 1'


//��������
//��һ���հ�û����ȷ���� ����ʱ�� ����һ�������Ĳ���   it 
def closure2 = {"hello $it" }
assert closure2('groovy') == 'hello groovy'


//������ȷ����Ҫ����ʱ ��ô��
def closure3 ={->
    "hi groovy"
}


//�հ�Ҳ����ʹ�ÿɱ����
def vargsFunc1 = {String ... args -> args.join('')}
assert vargsFunc1('1','2','3')=='123'
//ʹ������Ļ� Ҳ����ʵ����ͬ�Ĺ���
def vargsFunc2 = {String [] args -> args.join('') }
assert vargsFunc2('1','2','3')=='123'

// ������� �ɱ������ ��Ҫ�в����� ��ô�ɱ������Ҫ�ŵ����
def vargsFunc3 = {int i,String... args
->
println "i=$i ,args = $args"
}
 
vargsFunc3(1,'1','2')


//owner delegate and  this
//���Ǵ��ڱհ��еĸ���
// this ����˼
//����getThisObject �����ض���closure���࣬�൱����ʽ ʹ��this
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


//����հ��� �ڲ����ж��壬 ��ô�᷵���ڲ��� �������ⲿ��
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

//��Ƕ�ױհ�������� �� ������ �ⲿ��  �����Ǳհ�
class NestedClosure{
    void run(){
        def nestedClosures = {
            def cl = {this}
            cl()
        }
        
        assert nestedClosures()==this
    }
}



//owner����˼
//owner �᷵��һ�� ֱ�ӱպ� ����owner�հ� �Ķ����������� class ����closure
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
//����ʹ��delegate ���� getDelegate �����ʱհ��Ĵ���
// Ĭ������� ��������Ϊ owner
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
        //����Ӧ�ÿ����жϳ� delegate ��ʱ�� owner
        assert enclosed()==enclosed
        assert ownerMethod() ==ownerMethod
        
    }
}
def e4 = new Enclosing3()
e4.run()


//�հ��� ��������ǿ��Ա����õ�
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

//�ڱհ��У�������ȷ����delegate ����ʹ��delegate
class Ryan{
    String name
}
def r = new Ryan(name:'Ryan')
def r2 = {name.toUpperCase()}
r2.delegate = r
assert r2() == 'RYAN'


//delegate�� ί�в���
//Closure.OWNER_FIRST  owner ���� delegate���  ������Ĭ�ϵĲ���!!

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

//Closure.DELEGATE_FIRST delegate���� owner ���
ryan2.upper.resolveStrategy = Closure.DELEGATE_FIRST
assert ryan2.upper()=='RYAN1'

//Closure.OWNER_ONLY ����� owner
//Closure.DELEGATE_ONLY  ����� delegate




//��Gstring ��ʹ��Closure
// GString ֻ���� ������ʱ�� ȥ��ֵ
//${x}���ܴ���һ�� �հ� 
def x =1
def gs = "x=${x}"
assert gs == 'x=1'
x = 2
assert gs !='x=2'

//�����Ҫ�� GString �� ʵ�������ıհ�������Ա��������ӳٹ�ֵ ����ʹ�� ${->x}
def y = 1
def gss = "y=${->y}"
assert gss =='y=1'
y=2
assert gss == 'y=2'



//curry(left)
//curry �Ĺ����� ���� �����Ĳ�����������һ�������˲���֮��� �հ�
def curry1 = {int a,int b->a+b}
def curry2 = curry1.curry(1)
assert curry2(1)==2
assert curry2(1)==curry1(1,1)


//Right curry
//�������� ���Ҳ�Ĳ�����������һ�������˲���֮��ıհ�
def curry3 = {int a,String b-> "$b has $a kids"}
def curry4 = curry3.rcurry('lucy')
assert curry4(1)=='lucy has 1 kids'



//index based curry
//����հ����ն������������� ����ʹ�� ncurry(),ȥ�趨 ָ������λ�õĲ���ֵ
def curry5 = {a,b,c->a+b+c}
def curry6 = curry5.ncurry(1,1)
assert curry6(2,3)==6



// memolize
def fib
fib = { long n -> n<2?n:fib(n-1)+fib(n-2) }.memoize()
println fib(111)

//assert fib(15) == 610 // slow!



//Composition
//��һ�� �հ��Ľ�� ��Ϊ����һ���հ��� ����
def plus2 = {it+2}
def times3 = {it * 3}
def timesInPlus = plus2<<times3
assert timesInPlus(1)==5
assert timesInPlus(1)== plus2(times3(1))

def plusInTimes = plus2>>times3
assert plusInTimes(1)==9
assert plusInTimes(1)== times3(plus2(1))



//Trampoline
//�ݹ��㷨ͨ�������Ѹ߶����ƣ����������һ���ݹ�����̫��ķ��������ջ��յ�һ�� StackOverflowException
def factorial
factorial = { int n, def accu = 1G ->
    if (n < 2) return accu
    factorial.trampoline(n - 1, n * accu)
}
factorial = factorial.trampoline()

assert factorial(1)    == 1
assert factorial(3)    == 1 * 2 * 3
assert factorial(1000) // == 402387260.. plus another 2560 digits

