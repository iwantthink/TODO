# 简介
ASM是一款基于java字节码层面的代码分析和修改工具，ASM的目标是生成,转换和分析已编译的java class文件。

[ASM库的原理和使用方法 1](http://www.apmbe.com/java-asm%E5%BA%93%E7%9A%84%E5%8E%9F%E7%90%86%E4%B8%8E%E4%BD%BF%E7%94%A8%E6%96%B9%E6%B3%95%EF%BC%88%E4%B8%80%EF%BC%89/)

[ASM简介](http://www.jianshu.com/p/85502e42bbb6)

[ASM-API文档](http://asm.ow2.org/asm50/javadoc/user/index.html)

[ASM-PDF 使用介绍](http://download.forge.objectweb.org/asm/asm4-guide.pdf)

# 1 API介绍

- ASM有两套API，一套基于访问者模式、一套基于树的数据结构。
	
	- 基于访问者模式的api，类中的每个数据结构都是一个Event，类的生成同样基于这样的Event。
	- 基于树的数据结构的api是面向对象的一种设计，类被表示为一个对象。这两套api的区别类似xml解析中的SAX和DOM。两套api都是对同一个class操作的，如果用户需要修改相关联的类需要自己手动管理。

## 1.1 类源码与字节码的区别
- 每个class文件只包含一个类，没有注释,没有package和import部分，所有的类型必须使用全名。

- 编译后的字节码有一个常量存放区，里面存放类中出现的所有的数字,字符串，类型常量。这些常量只被定义一次，被类的其他部分通过索引引用.使用ASM的话，常量定义区是透明的。

- 源码文件和class文件中对类型的引用方式不同

## 1.2 接口和组件
class的生成和转换是基于`ClassVisitor`这个抽象类。该类每个方法都对应class的一个结构。

- class中简单的结构 可以通过一个 **参数为描述其的内容，返回为void** 的方法来访问

- class中复杂的结构（其内容可以是任意长度和复杂度的部分） 通过一个有初始化方法调用  并返回辅助的visitor class 的方法 进行访问。例如:`visitAnnotation`,`visitField`,`visitMethod`需要返回`AnnotationVisitor`,`FieldVisitor`,`MethodVisitor`

- `ClassVisitor`方法调用必定按照如下顺序:`visit visitSource? visitOuterClass? ( visitAnnotation | visitAttribute )*
( visitInnerClass | visitField | visitMethod )*
visitEnd`

- `ClassReader`用来解析编译过后的类的字节码数组.通过`accept`方法与`ClassVisitor`相关联，并在解析过程中会调用`ClassVisitor`相应的visitXXX方法。可以被看做一个事件生产者.

- `ClassWriter`来生成二进制格式的class，

- `ClassVisitor`可以被看做一个事件过滤器