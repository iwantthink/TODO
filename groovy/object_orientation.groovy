class Foo{
    static int i
}

assert Foo.i.class == Integer.class
assert Foo.class.getDeclaredField('i').type ==int.class
assert Foo.i.class !=int.class

//class
//public field ���Զ�ת�������ԣ�û�й����get set
// ���� ���� ���� ���û��ָ���������η� Ĭ�϶���public
// һ���ļ����԰�������� �������һ��.groovyû���κ��࣬���ͻᱻ�����������

class Person{
    String name
    Integer age
    
    def increaseAge(Integer years){
        this.age+=years
    }
}


//normal class
// �������κ������� ���߽ű��½���ʵ����



//inner class
//�ٷ��ĵ�����з���ࣿ �ⲿ�ࣨ����ࣩ
// �ⲿ�����ʹ���ڲ��� ���ڲ������ʹ���ⲿ��ĳ�Ա ��ʹ��˽�еģ� �����ⲿ��֮����಻��������ڲ���
// ʹ���ڲ����һЩԭ��
// ͨ���ڲ������ʽ�������������� ���ӷ�װ������Ҫ֪�������ʵ�֡� ���ӵĸ��ھ� �����
// ����ڲ��� ����ʹ����һ��  ���Կ���ʹ�������ڲ���
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
//������ ����ʹ��abstract �ؼ�������
//���󷽷� ����ʹ��abstract �ؼ�������  ��java����  ������ ����ͷǳ���ķ������棬
abstract class AbstractClass{
    String name
    
    abstract def absMethod()
}



//interface
//�ӿ��еķ������ǹ����ģ� ���Բ���ʹ��private protected �������� 
interface Greeter{
    void greet(String name)
}



//���캯��
//λ�ò������캯�� ���������Ѿ������˹��캯��
//ʹ�������Ĺ��캯�������ַ�ʽ
class PersonConstruct{
    PersonConstruct(String naem){
    }
}
def p1 = new PersonConstruct('name')
def p2 = ['name'] as PersonConstruct
PersonConstruct p3 = ['name']

//�����������캯��,�� û���������캯��
//û���������캯��������ʹ��map(key-value)����ʽ�����ݲ��������������������������ѡȡ������� ���Ҳ����ٶ��幹�캯
class PersonWOConstruct{
    String name
    Integer age
}
def pwo1 = new PersonWOConstruct()
def pwo2 = new PersonWOConstruct(name:'jack')
def pwo3 = new PersonWOConstruct(name:'jack',age:22)



//Method 
//һ������ �����def ����������  ������Է����κ����͵�ֵ
def func1(){
    println 'hello groovy'
}
def re = func1()
assert re ==null
//һ���������Խ����κ������� δ���������͵Ĳ���
// Ĭ�Ϸ��� Ϊpublic

def foo1(Map args){
    println "$args.name     $args.value"
}
foo1(name:'jack',value:123)

// Ĭ�ϲ���
def foo2(param1,param2 = 2,param3){
    println "param1 = $param1"
    println "param2 = $param2"
    println "param3 = $param3"
}
//��������������ʱ�� �����Ƚ����� �ṩ�� û��Ĭ��ֵ��
foo2(1000,1);


//�ɱ����
def foo3(Object... args){
    args.length
}

assert foo3(1,2,3) ==3
assert foo3(1) == 1
assert foo3() == 0
// ���� Ҳ���Ա��϶�Ϊ �ɱ����
def foo4(Object [] args){
    args.length
}
assert foo4() == 0
assert foo4(1) == 1
assert foo4(1, 2) == 2

//��� ͬʱ������ �ɱ���� �� �ֶ����� ָ�������Ĳ����� ���� ���������� ָ��������Ϊ׼
def foo6(Object... args){
        1
}
def foo6(args){
        2
}
assert foo6()==1
assert foo6(1)==2




//field
//�������η� public  protected   private
//static final synchronized
//һ���ֶο����ڱ�����ʱ  ֱ�ӳ�ʼ��
class Practice{
    private String name = next()
    
    def next(){
            111
    }
}

def cl = new Practice()
println cl.name
// ���� �ֶ� ��������ʱ�� ָ�� ���ͣ���Ȼ˵ ����ʡ�Ե� ��� ���͵�����




//property
//������ private  �� get/set �����
//���� ������ public protected private�����η�
//����ѡ�� static final synchronized
//Groovy���ʵ����� set/get
class Person2{
    String name //�ᴴ����һ��private String name ,��һ�� setter ��һ�� getter
    String city
}
//��final ����ʱ�� ��������setter  ���ǻ��� getter
class Person3{
    final String name = 'jack'
}
def pp1 = new Person3()
assert pp1.name =='jack'
assert pp1.getName() == 'jack'
class Person4{
    String name
    String age
}
def pp2 = new Person4()
assert  pp2.properties.keySet().containsAll(['name','age'])











