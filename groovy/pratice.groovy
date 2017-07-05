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



//三个引号中的 字符串 支持随意换行
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





