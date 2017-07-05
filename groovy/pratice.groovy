// 单行注释
/*
  多行注释
  cmd groovyConsole --- open GroovyConsole
  ctrl+w ---clear output window
  ctrl+r --- run groovy coude 
*/
/**
   评论,可以添加 @param @return 之类的
*/

//groovy 可以不以分号结尾
//groovy 支持动态类型，即定义变量的时候可以不指定其类型  定义变量使用def（def 不是必须的！）
//groovy 标识符 可以以 英文 下划线 $开头,不能以数字开头
//  转义字符　＇＼＇
def variable1 = '123'
def int variable2 = 1
def variable3  = "\${}"
//println variable3

//有返回值的 函数  可以不使用def
String function1(arg1,arg2){
}

//无返回值的 函数 必须使用def
def noReturnFunc(){
}


//函数中的返回值 可以不使用return语句来设置。默认会将函数中最后一句代码执行的结果设置成返回值
//如果定义函数时指定了返回类型，那么函数中的返回值必须是与之匹配的类型。使用动态类型的话，就可以返回任何类型了。
def doSomething(){
    "return with str"// 返回String 类型
    1111 // 返回intger类型
}



//单引号中的内容严格对应java中的String，不对$符号进行转义
//双引号 内容与脚本语言相似，如果字符中有$符号，会以 $表达式 先求出值
// 占位符 可以 以前缀$ 或者${}  ,当GString 调用toString() 时 占位符表达式 的值会被计算出来
// 占位符 之内允许声明， 不过其返回值是根据最后一句，需要注意
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




//groovy 函数调用的时候 可以不加上 括号
//println('hello')
//println 'hello'

//$number1.toString()  Groovy会认为你在调用number1 的toString属性
def number1 = 123.456
//解决办法
// println "${number1.toString()}"



// 我们可以在${} 占位符中加入任意表达式 ，但是当占位符 包含一个箭头时，${->} ,该表达式实际是一个闭包表达式，可以将其视为一个前缀为$的闭包
// 闭包比纯粹的表达式有一个优势：lazy evaluation
def eagerGstring = "value = ${number1}"
def lazyGstring = "value = ${->number1}"
assert eagerGstring == "value = 123.456"
assert lazyGstring  == "value = 123.456"
number1 = 2
assert eagerGstring == "value = 123.456"
assert lazyGstring == "value = 2"


//在GString中 使用闭包时 不允许有多个参数
def gString1 = "value = ${arg1,arg2->number1}"



//当我们期望一个String类型的参数时，传入一个GString类型的参数，Groovy会自动调用toString() 
String getString(String msg){
    assert msg instanceof String
    return msg
}
def msg1 = "hello ${number1}"
assert msg1 instanceof GString
assert getString(msg1) instanceof String



// GString 和 String 的hashCODE 不同
//println msg1.hashCode()
//println "hello ${2}".hashCode()
//println 'hello 2'.hashCode()
assert msg1.hashCode() != 'hello 2'.hashCode()
assert "hello ${number1}".hashCode() != 'hello 2'.hashCode()

//三重 单引号 字符串 ,支持多行 其他与 单引号字符串一样
def quoto1 = '''
one 
two
${number1}
'''
//println quoto1

//三重 双引号 字符串  类似 双引号字符串，区别就是多行的,并且在三重双引号中  单引号和双引号 不需要转义
def msg2 = """
 "one
 '' two 
 ${number1}
 three
 "
"""
//println msg2




// 斜线字符串  适用于 定义正则表达式和patterns ,因为不需要转义反斜线
def slashy1 = /.*hello*./
assert slashy1 == '.*hello*.'

// 斜线字符串，允许不转义的带上反斜线
def slashy2 = /\one/
//println //one/  失败
//println slashy2  成功
//println "\one"  失败
//println "/one"  成功

// 斜线字符串支持 多行
def slashy3 = /
\one 
two /
//println slashy3

//斜线字符串 支持 占位符
def slashy4 = /value = $number1/
def slashy5 = 'value = $number1'
//println slashy5
//println slashy4

// 一个空的斜线字符串 不能使用双斜线来表示,因为Groovy会认为这是个注解标志
//assert '' != //



//   $/ /$  在其中的字符串 不需要转义 &  和 斜线, 支持多行， 其他与GString 类似
// 通过$符号进行转义  ， 可以转义 $和 斜杠 
// 正斜杠不需要转义  ， 反斜杠需要转义
// &符号可以做占位符 ，和字符串一起使用时 需要转义
 
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




//三种指定类型的方式
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
//可以使用以下方式定义整数
//primitive types 原始类型
byte byte1 =1
char char1 = 2
short short1 = 3
long long1 = 4
int int1 = 5
//infinite precision  无限精度
BigInteger bi1 = 6

//如果使用def 定义整数，那么变量的类型会适应这个整数值
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

//负数
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




//二进制   0b 前缀
int xInt2 = 0b11
assert xInt2 == 3

//八进制 0前缀 后面跟八进制数字
int xInt8 = 077
assert xInt8 == 63

//十六进制 0x 前缀
int xInt16 = 0x3a
assert xInt16 == 58

//允许在 数字中使用 下划线 用来方便的阅读
long reditCardNumber = 123_456
assert reditCardNumber == 123456

// 可以通过添加后缀 指定数字类型
// BigInteger G or g
// Long L or l
//Integer I or i
//BigDecimal G or g
//Double D or d
//Float F or f
assert 1i.class == Integer
assert 1i.class != Long

