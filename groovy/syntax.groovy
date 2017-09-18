// ����ע��
/*
  ����ע��
  cmd groovyConsole --- open GroovyConsole
  ctrl+w ---clear output window
  ctrl+r --- run groovy coude 
*/
/**
   ����,������� @param @return ֮���
*/

//groovy ���Բ��ԷֺŽ�β
//groovy ֧�ֶ�̬���ͣ������������ʱ����Բ�ָ��������  �������ʹ��def��def ���Ǳ���ģ���
//groovy ��ʶ�� ������ Ӣ�� �»��� $��ͷ,���������ֿ�ͷ
//  ת���ַ������ܣ�
def variable1 = '123'
def int variable2 = 1
def variable3  = "\${}"
//println variable3

//�з���ֵ�� ����  ���Բ�ʹ��def
String function1(arg1,arg2){
}

//�޷���ֵ�� ���� ����ʹ��def ����ʹ�� void
def noReturnFunc(){
}

void noReturnFunc1(){}


//�����еķ���ֵ ���Բ�ʹ��return��������á�Ĭ�ϻὫ���������һ�����ִ�еĽ�����óɷ���ֵ
//������庯��ʱָ���˷������ͣ���ô�����еķ���ֵ��������֮ƥ������͡�ʹ�ö�̬���͵Ļ����Ϳ��Է����κ������ˡ�
def doSomething(){
    "return with str"// ����String ����
    1111 // ����intger����
}



//�������е������ϸ��Ӧjava�е�String������$���Ž���ת��
//˫���� ������ű��������ƣ�����ַ�����$���ţ����� $���ʽ �����ֵ
// ռλ�� ���� ��ǰ׺$ ����${}  ,��GString ����toString() ʱ ռλ�����ʽ ��ֵ�ᱻ�������
// ռλ�� ֮������������ �����䷵��ֵ�Ǹ������һ�䣬��Ҫע��
def str3 = 'not'
def str1 = 'i am $str3 student'
//println str1
def str2 = "i am $str3 student"
//println str2
assert str2.toString() == str2
/*
i am $a student
i am not student
*/




//groovy �������õ�ʱ�� ���Բ����� ����
//println('hello')
//println 'hello'

//$number1.toString()  Groovy����Ϊ���ڵ���number1 ��toString����
def number1 = 123.456
//����취
// println "${number1.toString()}"

// ���ǿ�����${} ռλ���м���������ʽ �����ǵ�ռλ�� ����һ����ͷʱ��${->} ,�ñ��ʽʵ����һ���հ����ʽ�����Խ�����Ϊһ��ǰ׺Ϊ$�ıհ�
// �հ��ȴ���ı��ʽ��һ�����ƣ�lazy evaluation
def eagerGstring = "value = ${number1}"
def lazyGstring = "value = ${->number1}"
assert eagerGstring == "value = 123.456"
assert lazyGstring  == "value = 123.456"
number1 = 2
assert eagerGstring == "value = 123.456"
assert lazyGstring == "value = 2"


//��GString�� ʹ�ñհ�ʱ �������ж������
def gString1 = "value = ${arg1,arg2->number1}"

//����������һ��String���͵Ĳ���ʱ������һ��GString���͵Ĳ�����Groovy���Զ�����toString() 
String getString(String msg){
    assert msg instanceof String
    return msg
}
def msg1 = "hello ${number1}"
assert msg1 instanceof GString
assert getString(msg1) instanceof String



// GString �� String ��hashCODE ��ͬ
//println msg1.hashCode()
//println "hello ${2}".hashCode()
//println 'hello 2'.hashCode()
assert msg1.hashCode() != 'hello 2'.hashCode()
assert "hello ${number1}".hashCode() != 'hello 2'.hashCode()

//���� ������ �ַ��� ,֧�ֶ��� ������ �������ַ���һ��
def quoto1 = '''
one 
two
${number1}
'''
//println quoto1

//���� ˫���� �ַ���  ���� ˫�����ַ�����������Ƕ��е�,����������˫������  �����ź�˫���� ����Ҫת��
def msg2 = """
 "one
 '' two 
 ${number1}
 three
 "
"""
//println msg2




// б���ַ���  ������ ����������ʽ��patterns ,��Ϊ����Ҫת�巴б��
def slashy1 = /.*hello*./
assert slashy1 == '.*hello*.'

// б���ַ���������ת��Ĵ��Ϸ�б��
def slashy2 = /\one/
//println //one/  ʧ��
//println slashy2  �ɹ�
//println "\one"  ʧ��
//println "/one"  �ɹ�

// б���ַ���֧�� ����
def slashy3 = /
\one 
two /
//println slashy3

//б���ַ��� ֧�� ռλ��
def slashy4 = /value = $number1/
def slashy5 = 'value = $number1'
//println slashy5
//println slashy4

// һ���յ�б���ַ��� ����ʹ��˫б������ʾ,��ΪGroovy����Ϊ���Ǹ�ע���־
//assert '' != //



//   $/ /$  �����е��ַ��� ����Ҫת�� &  �� б��, ֧�ֶ��У� ������GString ����
// ͨ��$���Ž���ת��  �� ����ת�� $�� б�� 
// ��б�ܲ���Ҫת��  �� ��б����Ҫת��
// &���ſ�����ռλ�� �����ַ���һ��ʹ��ʱ ��Ҫת��
 
def name = "Guillaume"
def date = "April, 1st"

def dollarSlashy = $/
    
    Hello $name,
    today we're ${date}.
    /
    $ dollar sign
    $$ escaped dollar sign
    \ backslash
    / forward slash
    $/ escaped forward slash
    $$$/ escaped opening dollar slashy
    $/$$ escaped closing dollar slashy
/$
//println dollarSlashy




//����ָ�����͵ķ�ʽ
char c1 = 'A'
def c2 = 'A' as char
def c3 = (char)'A'
assert c1 instanceof Character
assert c2 instanceof Character
assert c3 instanceof Character



//integral literals
/*
byte char short int long java.lang.BigInteger
*/
//����ʹ�����·�ʽ��������
//primitive types ԭʼ����
byte byte1 =1
char char1 = 2
short short1 = 3
long long1 = 4
int int1 = 5
//infinite precision  ���޾���
BigInteger bi1 = 6

//���ʹ��def ������������ô���������ͻ���Ӧ�������ֵ
def a = 1
assert a instanceof Integer

// Integer.MAX_VALUE
def b = 2147483647
assert b instanceof Integer

// Integer.MAX_VALUE + 1
def c = 2147483648
assert c instanceof Long

// Long.MAX_VALUE
def d = 9223372036854775807
assert d instanceof Long

// Long.MAX_VALUE + 1
def e = 9223372036854775808
assert e instanceof BigInteger

//����
def na = -1
assert na instanceof Integer

// Integer.MIN_VALUE
def nb = -2147483648
assert nb instanceof Integer

// Integer.MIN_VALUE - 1
def nc = -2147483649
assert nc instanceof Long

// Long.MIN_VALUE
def nd = -9223372036854775808
assert nd instanceof Long

// Long.MIN_VALUE - 1
def ne = -9223372036854775809
assert ne instanceof BigInteger




//������   0b ǰ׺
int xInt2 = 0b11
assert xInt2 == 3

//�˽��� 0ǰ׺ ������˽�������
int xInt8 = 077
assert xInt8 == 63

//ʮ������ 0x ǰ׺
int xInt16 = 0x3a
assert xInt16 == 58

//������ ������ʹ�� �»��� ����������Ķ�
long reditCardNumber = 123_456
assert reditCardNumber == 123456

// ����ͨ����Ӻ�׺ ָ����������
// BigInteger G or g
// Long L or l
//Integer I or i
//BigDecimal G or g
//Double D or d
//Float F or f
assert 1i.class == Integer
assert 1i.class != Long



byte var1 = 127
char var2 = 'A'
short var3 = 456
int var4 = 1
long var5 = 123

// byte char short �� int ���м���  ����� int ����
assert (var1 + var4).class == Integer
assert (var2 + var4).class == Integer
assert (var3 + var4).class == Integer

//long �� byte char short int  ���м���  �����long ����
// ̫���ˡ��� ƽ���ò���   ������ȥ���ٷ��ĵ��ɡ�����



//Groovy ���ṩר�õ������������  ֻ��ͨ��intdiv ����
//println 4.intdiv(5)

//��� �ڳ����� ����һ�� float �� double���͵����ݣ���ô�������Double���� ������������BigDecimal����
assert (4/3).class == BigDecimal
assert (4d/3).class == Double
assert (4f/3).class == Double
assert (4l/3).class == BigDecimal



//�η�����  ����:**   ���� ** ָ��
// ���ָ����С�� ��������Է��� integer �Ǿͷ���integer  �� ���Է���Long �ͷ���Long �� ����Ļ�ͳһ����Double
assert 2**0.1 instanceof Double
assert 2**-0.1 instanceof Double
assert 1**-0.3f instanceof Integer
assert 9.9**1.9 instanceof Double

//ǰ�᣺ָ��������
//��� ������ ����ô�ð��������Ƿ���������  ����Integer Long or Double
assert 10**-1 instanceof Double
assert 1**-1 instanceof Integer
//��� �������������㣬 ��ô���� ����������
//��� ������ BigDecimal  ��ô���� BigDecimal
//��� ������BigInteger ��ô����BigInteger
//��� ������Integer ��ô����Integer �������ݷŲ���ʱ  �ͷ��� BigInteger
//��� ������Long ����ô����Long �� �����ݷŲ���ʱ  �ͷ���BigInteger
assert new BigDecimal(10) ** 0 instanceof BigDecimal
assert new BigInteger(10) ** 1 instanceof BigInteger
assert 10i ** 1 instanceof Integer
assert 10i ** 10 instanceof BigInteger
assert 10l ** 10 instanceof Long
assert 10l ** 100 instanceof BigInteger



// ����ֵ  true false
def bool1 = true
assert bool1 instanceof Boolean



// Lists  ����
//list ������ [] ���� �����ҿ���ֱ��ͨ���������д�ȡ���������������ǰ�����ȣ�list���Զ������������Ԫ��
// һ��List �� ArrayList��Ϊ������ʵ��
def numbers2 = [1,2,3] as LinkedList
assert numbers2 instanceof LinkedList

def numbers1 = [1,2,3]
assert numbers1 instanceof List
assert numbers1.size ==3
assert numbers1 instanceof ArrayList
assert numbers1[1]==2
//����ͨ�� << leftShift ������ ����Listĩβ ���һ�� ����
numbers1<< 4
assert numbers1[-1] ==4
assert numbers1[0,-1] == [1,4]
assert numbers1[0..-1] == [1,2,3,4]
//list �����԰�������һ��list
def multi = [[0,1],[2,3]]
assert multi[1][1]==3



// Arrays  ����
//List��� ʹ��def ���� Ĭ����Arraylist ���� ,����ʹ���� as ָ������
int[] arry1 = [1,2,3]
Integer[] arry2 = [1,2,3]
def arry3 = [1,2,3]
def arry4 = [1,2,3] as int[]
assert arry4 instanceof int[]
assert arry3 instanceof ArrayList
assert arry1 instanceof int[]
assert arry2 instanceof Integer[]
//Arrays ȡ��С ����ʹ��  size... ������ size() 
assert arry1.size() ==3
//println arry1.class
//println arry2.class
// Arrays Ҳ���� ʹ�ö�������
def multi2 = new Integer[2][2]
assert multi2.size() == 2
assert multi2[0][0]==null

Integer[][] p1 
p1 = [[1],[2]]
assert p1 instanceof Integer[][]




//Maps �ֵ� ��ֵ��
//map ��[key:value]���ڣ� key ������ ������ ��˫���� ���߲������� ����
def maps1 = [red:'#FF0000',green:'#00FF00',blue:'#0000ff']
assert maps1 instanceof Map
assert maps1 instanceof HashMap
assert maps1 instanceof LinkedHashMap

//ȡֵ
assert maps1.red == '#FF0000'
assert maps1['red'] == '#FF0000'

// ȥȡһ�������ڵ�key  �᷵��null
assert maps1.yellow == null

//����ʹ�� String  int ��Ϊkey
//������int ��Ϊkey�Ļ� ȡֵ��ʱ�� ����ֱ�� �� .key ��ȡֵ  ����ʹ��  map[key]��ȡֵ
def maps2 = [1:2,3:4]
assert maps2[1]==2

//���ʹ��һ��������name ��Ϊkey  ��ô������ name����key
def key1  = 'hello'
def maps3 = [key1:'world']
assert maps3.containsKey('key1')
assert !maps3.containsKey('hello')

//�������Ҫʹ��name ��ͬʱ����ȡvalue����key
def maps4 = [(key1):'world']
assert maps4.containsKey('hello')
assert !maps4.containsKey('key1')

//ֱ�����key
maps4.anotherKey = "i am map"
println maps4