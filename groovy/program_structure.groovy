// groovy 必须在类定义之前指定包 ，否则使用默认包
package com.package

//import 
//例如 MarkupBuilder 这个类 如果我不 手动 import ， 则 下面的assert不成立
import groovy.xml.MarkupBuilder
def xml = new MarkupBuilder()
assert xml !=null


// Groovy 会为我们默认 导入一些包 减少我们的代码
//import java.lang.*
//import java.util.*
//import java.io.*
//import java.net.*
//import groovy.lang.*
//import groovy.util.*
//import java.math.BigInteger
//import java.math.BigDecimal



// * 导入， Groovy 提供了像java 一样的一种方式， 使用 符号* 表示导入包中所有的类
// 例如  MarkupBuilder  和  StreamingMarkupBuilder  都是在 groovy.xml 下面
import groovy.xml.* 
assert new StreamingMarkupBuilder()!=null



// Groovy 允许 静态导入 ， 相当于把方法当做自己类中的静态方法一样使用
import static Boolean.FALSE
assert !FALSE
//Groovy 的静态导入 与java相似 ，但是更加的动态，Groovy允许你的类中定义和 静态导入的方法  同样的名字，只需要俩者有不同的参数要求
//这在java中是不被允许的  。但是 groovy是允许的
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



//静态导入别名
import static Calendar.getInstance as now
assert now().class == Calendar.getInstance().class




//导入别名
import java.util.Date as jud
assert new jud() instanceof java.util.Date




// Groovy 支持 class 形式 和 脚本形式
//class 形式
//Main.Groovy
class Main{
    static void main(String... args){
        println 'hello groovy'
    }
}

//script 形式 
//Main2.Groovy Script的一种形式 ，无需声明它
println "hello groovy "

//Script 的另一种表现形式
//需要提供一个 run 方法 
import org.codehaus.groovy.runtime.InvokerHelper
class Main2 extends Script{
       def run(){
           println 'hello groovy srcipt'
       }
       static void main(String[] args){
           InvokerHelper.runScript(Main2,args)
       }
}
//另外 如果抛出异常， 会使用 转换之前的 行号 而不是生成的代码的行号

// Script 的第一种形式 ，定义变量
//int x = 1   会在生成的代码中  被当做 局部变量
//def x = 1  会被当做全局变量

