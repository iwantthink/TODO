// groovy �������ඨ��֮ǰָ���� ������ʹ��Ĭ�ϰ�
package com.package

//import 
//���� MarkupBuilder ����� ����Ҳ� �ֶ� import �� �� �����assert������
import groovy.xml.MarkupBuilder
def xml = new MarkupBuilder()
assert xml !=null


// Groovy ��Ϊ����Ĭ�� ����һЩ�� �������ǵĴ���
//import java.lang.*
//import java.util.*
//import java.io.*
//import java.net.*
//import groovy.lang.*
//import groovy.util.*
//import java.math.BigInteger
//import java.math.BigDecimal



// * ���룬 Groovy �ṩ����java һ����һ�ַ�ʽ�� ʹ�� ����* ��ʾ����������е���
// ����  MarkupBuilder  ��  StreamingMarkupBuilder  ������ groovy.xml ����
import groovy.xml.* 
assert new StreamingMarkupBuilder()!=null



// Groovy ���� ��̬���� �� �൱�ڰѷ��������Լ����еľ�̬����һ��ʹ��
import static Boolean.FALSE
assert !FALSE
//Groovy �ľ�̬���� ��java���� �����Ǹ��ӵĶ�̬��Groovy����������ж���� ��̬����ķ���  ͬ�������֣�ֻ��Ҫ�����в�ͬ�Ĳ���Ҫ��
//����java���ǲ��������  ������ groovy�������
import static java.lang.String.format

class SomeClass{
    String format(Integer i){
        i.toString
    }
    static void main(String[] args){
        assert format('String')=='String'
        assert new SomeClass().format(new Integer(1))=='1'
    }
}



//��̬�������
import static Calendar.getInstance as now
assert now().class == Calendar.getInstance().class




//�������
import java.util.Date as jud
assert new jud() instanceof java.util.Date




// Groovy ֧�� class ��ʽ �� �ű���ʽ
//class ��ʽ
//Main.Groovy
class Main{
    static void main(String... args){
        println 'hello groovy'
    }
}

//script ��ʽ 
//Main2.Groovy Script��һ����ʽ ������������
println "hello groovy "

//Script ����һ�ֱ�����ʽ
//��Ҫ�ṩһ�� run ���� 
import org.codehaus.groovy.runtime.InvokerHelper
class Main2 extends Script{
       def run(){
           println 'hello groovy srcipt'
       }
       static void main(String[] args){
           InvokerHelper.runScript(Main2,args)
       }
}
//���� ����׳��쳣�� ��ʹ�� ת��֮ǰ�� �к� ���������ɵĴ�����к�

// Script �ĵ�һ����ʽ ���������
//int x = 1   �������ɵĴ�����  ������ �ֲ�����
//def x = 1  �ᱻ����ȫ�ֱ���

