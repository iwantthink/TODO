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


//�з���ֵ�� ����  ���Բ�ʹ��def
String function1(arg1,arg2){
}

//�޷���ֵ�� ���� ����ʹ��def
def noReturnFunc(){
}


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
def a = 'not'
def str1 = 'i am $a student'
//println str1
def str2 = "i am $a student"
//println str2
assert str2.toString() == str2
/*
i am $a student
i am not student
*/



//���������е� �ַ��� ֧�����⻻��
def str3 = '''one
two
three
 '''
//println str3
/*
one
two
three
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





