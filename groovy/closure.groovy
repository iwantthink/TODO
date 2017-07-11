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


