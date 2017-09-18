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

//无返回值的 函数 必须使用def 或者使用 void
def noReturnFunc(){
}

void noReturnFunc1(){}


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



byte var1 = 127
char var2 = 'A'
short var3 = 456
int var4 = 1
long var5 = 123

// byte char short 和 int 进行计算  结果是 int 类型
assert (var1 + var4).class == Integer
assert (var2 + var4).class == Integer
assert (var3 + var4).class == Integer

//long 和 byte char short int  进行计算  结果是long 类型
// 太多了。。 平常用不到   看到了去看官方文档吧。。。



//Groovy 不提供专用的整除运算符号  只能通过intdiv 函数
//println 4.intdiv(5)

//如果 在除法中 存在一个 float 或 double类型的数据，那么结果就是Double类型 。否则结果都是BigDecimal类型
assert (4/3).class == BigDecimal
assert (4d/3).class == Double
assert (4f/3).class == Double
assert (4l/3).class == BigDecimal



//次方运算  符号:**   基数 ** 指数
// 如果指数是小数 ，如果可以返回 integer 那就返回integer  ， 可以返回Long 就返回Long ， 否则的话统一返回Double
assert 2**0.1 instanceof Double
assert 2**-0.1 instanceof Double
assert 1**-0.3f instanceof Integer
assert 9.9**1.9 instanceof Double

//前提：指数是整数
//如果 负整数 ，那么久按照数据是否满足条件  返回Integer Long or Double
assert 10**-1 instanceof Double
assert 1**-1 instanceof Integer
//如果 是正整数或者零， 那么根据 基数来分类
//如果 基数是 BigDecimal  那么返回 BigDecimal
//如果 基数是BigInteger 那么返回BigInteger
//如果 基数是Integer 那么返回Integer ，当数据放不下时  就返回 BigInteger
//如果 基数是Long ，那么返回Long ， 当数据放不下时  就返回BigInteger
assert new BigDecimal(10) ** 0 instanceof BigDecimal
assert new BigInteger(10) ** 1 instanceof BigInteger
assert 10i ** 1 instanceof Integer
assert 10i ** 10 instanceof BigInteger
assert 10l ** 10 instanceof Long
assert 10l ** 100 instanceof BigInteger



// 布尔值  true false
def bool1 = true
assert bool1 instanceof Boolean



// Lists  集合
//list 变量由 [] 定义 ，并且可以直接通过索引进行存取，如果索引超过当前链表长度，list会自动往该索引添加元素
// 一般List 用 ArrayList作为真正的实现
def numbers2 = [1,2,3] as LinkedList
assert numbers2 instanceof LinkedList

def numbers1 = [1,2,3]
assert numbers1 instanceof List
assert numbers1.size ==3
assert numbers1 instanceof ArrayList
assert numbers1[1]==2
//可以通过 << leftShift 操作符 来往List末尾 添加一个 数据
numbers1<< 4
assert numbers1[-1] ==4
assert numbers1[0,-1] == [1,4]
assert numbers1[0..-1] == [1,2,3,4]
//list 还可以包含另外一个list
def multi = [[0,1],[2,3]]
assert multi[1][1]==3



// Arrays  数组
//List如果 使用def 定义 默认是Arraylist 类型 ,除非使用了 as 指定类型
int[] arry1 = [1,2,3]
Integer[] arry2 = [1,2,3]
def arry3 = [1,2,3]
def arry4 = [1,2,3] as int[]
assert arry4 instanceof int[]
assert arry3 instanceof ArrayList
assert arry1 instanceof int[]
assert arry2 instanceof Integer[]
//Arrays 取大小 不能使用  size... 而得用 size() 
assert arry1.size() ==3
//println arry1.class
//println arry2.class
// Arrays 也可以 使用多重数组
def multi2 = new Integer[2][2]
assert multi2.size() == 2
assert multi2[0][0]==null

Integer[][] p1 
p1 = [[1],[2]]
assert p1 instanceof Integer[][]




//Maps 字典 键值对
//map 以[key:value]存在， key 可以用 单引号 ，双引号 或者不用引号 包裹
def maps1 = [red:'#FF0000',green:'#00FF00',blue:'#0000ff']
assert maps1 instanceof Map
assert maps1 instanceof HashMap
assert maps1 instanceof LinkedHashMap

//取值
assert maps1.red == '#FF0000'
assert maps1['red'] == '#FF0000'

// 去取一个不存在的key  会返回null
assert maps1.yellow == null

//可以使用 String  int 作为key
//但是用int 作为key的话 取值的时候 不能直接 用 .key 来取值  必须使用  map[key]来取值
def maps2 = [1:2,3:4]
assert maps2[1]==2

//如果使用一个变量的name 作为key  那么会把这个 name当成key
def key1  = 'hello'
def maps3 = [key1:'world']
assert maps3.containsKey('key1')
assert !maps3.containsKey('hello')

//如果就是要使用name ，同时让他取value当做key
def maps4 = [(key1):'world']
assert maps4.containsKey('hello')
assert !maps4.containsKey('key1')

//直接添加key
maps4.anotherKey = "i am map"
println maps4