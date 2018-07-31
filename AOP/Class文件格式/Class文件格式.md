# 1.概述
计算机只能识别0和1，所以大家编写的程序都需要经过编译器，转换为由0和1组成的二进制本地机器码(`Native Code`)。随着虚拟机的不断发展，很多程序语言开始选择与操作系统和机器指令集无关的格式作为编译后的存储格式（Class文件），从而实现”Write Once, Run Anywhere”。 Java设计之初，考虑后期能让Java虚拟机运行其他语言，目前有越来越多的其他语言都可以直接需要在Java虚拟机，虚拟机只能识别Class文件，至于是由何种语言编译而来的，虚拟机并不关心，如下图：

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fjgkeieb0mj20lt098gls.jpg)

[Class文件格式实例解析](http://www.acyouzi.com/2016/11/10/jvm-class-file-structure/) 

# 2.Class组成
Class文件是一组以**8位字节为单位的二进制流**，中间没有任何分隔符，非常紧凑。 当需要占用8位以上的数据时，会按照Big-endian顺序，高位在前，低位在后的方式来分割成多个8位字节来存储。

- **任何一个Class文件都对应着唯一的类或接口的定义信息**；

- 类或接口并不一定定义在文件里，也可以通过类加载器直接生成。


Java虚拟机规范规定：Class文件格式采用伪结构来存储数据，伪结构中只有无符号数和表这两种数据类型。

- 无符号数：是基本数据类型，以u1、u2、u4、u8分别代表1个字节、2个字节、4个字节、8个字节的无符号数。无符号数用于描述数字、索引引用、数量值、字符串值。

- 表：是由多个无符号数或者子表作为数据项构成的符合数据类型。用于描述有层次关系的复合结构的数据。整个Class其实就是一张表。

## 2.1 相关概念

- 全限定名

	是指把类全名中的“.”号，用“/”号替换，并且在最后加入一个“；”分号后生成的名称。比如`java.lang.Object`对应的全限定名为`java/lang/Object; `

- 简单名

	这个比较好理解，就是直接的方法名或者字段。比如toString()方法，不需要包名作为前缀了。

- 字段描述符

	用于描述字段的数据类型。

	规则如下：

		基本类型字符---对应类型

		B------------------byte
		C------------------char
		D------------------double
		F------------------float
		I------------------int
		S------------------short
		J------------------long
		Z------------------boolean
		V------------------void
		L+classname +;-----对象类型
		[------------------数组类型

	例如：

	基本类型：int ==> I
	对象类型：String ==> Ljava/lang/String;
	数组类型：long[] ==> [J


- 方法描述符

	用来描述方法的参数列表(数量、类型以及顺序)和返回值。

	格式：(参数描述符列表)返回值描述符。 例如：

		Object m(int i, double d, Thread t) {..} 
		=============> 
		(IDLjava/lang/Thread;)Ljava/lang/Object;


## 2.2 ClassFile结构
一个Class类文件是由一个ClassFile结构组成：

	classFile {
    u4             magic;               //魔数，固定值0xCAFEBABE
    u2             minor_version;       //次版本号
    u2             major_version;       //主版本号
    u2             constant_pool_count; //常量的个数
    cp_info        constant_pool[constant_pool_count-1];  //具体的常量池内容
    u2             access_flags;        //访问标识
    u2             this_class;          //当前类索引
    u2             super_class;         //父类索引
    u2             interfaces_count;    //接口的个数
    u2             interfaces[interfaces_count];          //具体的接口内容
    u2             fields_count;        //字段的个数
    field_info     fields[fields_count];                  //具体的字段内容
    u2             methods_count;       //方法的个数
    method_info    methods[methods_count];                //具体的方法内容
    u2             attributes_count;    //属性的个数
    attribute_info attributes[attributes_count];          //具体的属性内容
	}

- 一个Class文件的大小：26 + cp_info[] + u2[] + field_info[] + method_info[] + attribute_info[]

# 3 ClassFile文件组成
## 3.1 魔数
每个Class文件头4个字节称为魔数(Magic Number),作用是用于确定这个Class文件是否能被虚拟机所接受，魔数固定值`0xCAFEBABE`。这是身份识别，比如jpeg等图片文件头也会有魔数。

## 3.2 版本号
紧跟魔数，也占用4个字节。从第5字节到第8字节存储的分别是 次版本号，主版本号。

## 3.3 常量池
常量池是Class文件空间最大的数据项之一，长度不固定。

1. 常量池长度 用u2类型代表常量池容量计数值，u2紧跟版本号。u2的大小等于常量池的常量个数+1（**计数是从1开始,如果数值是35 则实际常量池有34项常量**）。**对于u2=0的特殊情况，代表没有使用常量池**。

2. 常量池内容,格式如下：

		cp_info {
    		u1 tag;
    		u1 info[];
		}
	
	包括两个类常量，**字面量和符号引用**：

	- 字面量：与Java语言层面的常量概念相近，包含文本字符串、声明为final的常量值等。

	- 符号引用：编译语言层面的概念，包括以下3类：
	类和接口的全限定名
	字段的名称和描述符
	方法的名称和描述符

	- 常量池中每一项常量都是一个表结构，每个表的开始第一位是u1类型的标志位tag, 代表当前这个常量的类型。在JDK 1.7.中共有14种不同的表结构的类型，如下：

	![](http://ww1.sinaimg.cn/large/6ab93b35gy1fjglcxzk5uj20p80cn7aq.jpg)

	Class文件都是二进制格式，可通过Jdk/bin/javap.exe工具，分析Class文件字节码。关于javap用法，可通过javap --help来查看。

- java代码编译时没有连接的概念，而是在虚拟机加载class文件的时候动态的连接，所以class文件中没有保存方法字段的最终内存布局。当虚拟机运行时需要从常量池中获取符号引用然后翻译到具体内存地址中

- `constant_Utf8_info`类型的常量，这个类型能标示的最大长度就是(65535),也就是最长的变量或方法名长度

## 3.4 访问标识
2个字节代表，标示用于识别一些类或者接口层次的访问信息.

	标识名-------------------标识值----------解释
	ACC_PUBLIC------------------- 0x0001----------声明为public;可以从包外部访问
	ACC_FINAL-------------------0x0010----------被声明为final;不允许子类修改
	ACC_SUPER-------------------0x0020----------当被invokespecial指令调用时，将特殊对待父类的方法
	ACC_INTERFACE-------------------0x0200----------接口标识符
	ACC_ABSTRACT-------------------0x0400----------声明为abstract;不能被实例化
	ACC_SYNTHETIC-------------------0x1000----------声明为synthetic;不存在于源代码，由编译器生成
	ACC_ANNOTATION-------------------0x2000----------声明为注释类型
	ACC_ENUM-------------------0x4000----------声明为枚举类型

## 3.5 类/父类索引
当前类索引和父类索引占用大小都为u2类型，由于一个类只能继承一个父类，故父类索引只有一个。除了java.lang.Object对象的父类索引为0，其他所有类都有父类。

## 3.6 接口索引

一个类可以实现多个接口，故利用interfaces_count来记录该类所实现的接口个数，interfaces[interfaces_count]来记录所有实现的接口内容。

## 3.7 字段表
字段表**用于描述类或接口中声明的变量**，格式如下：

	field_info {
    u2             access_flags; //访问标识
    u2             name_index;  //名称索引
    u2             descriptor_index; //描述符索引
    u2             attributes_count; //属性个数
    attribute_info attributes[attributes_count];  //属性表的具体内容
	}

- 字段访问标识如下：(表中加粗项是字段独有的)

		标识名	标识值	解释
		ACC_PUBLIC	0x0001	声明为 public; 可以从包外部访问
		ACC_PRIVATE	0x0002	声明为 private; 只有定义的类可以访问
		ACC_PROTECTED	0x0004	声明为 protected;只有子类和相同package的类可访问
		ACC_STATIC	0x0008	声明为 static；属于类变量
		ACC_FINAL	0x0010	声明为 final; 对象构造后无法直接修改值
		**ACC_VOLATILE**	0x0040	声明为 volatile; 不会被缓存,直接刷新到主屏幕
		**ACC_TRANSIENT**	0x0080	声明为 transient; 不能被序列化
		ACC_SYNTHETIC	0x1000	声明为 synthetic; 不存在于源代码，由编译器生成
		ACC_ENUM	0x4000	声明为enum
		Java语法中，接口中的字段默认包含ACC_PUBLIC, ACC_STATIC, ACC_FINAL标识。ACC_FINAL，ACC_VOLATILE不能同时选择等规则。

	紧跟其后的name_index和descriptor_index是对常量池的引用，分别代表着字段的简单名和方法的描述符。

## 3.8 方法表
方法表用于描述类或接口中声明的方法，格式如下：

	method_info {
    u2             access_flags; //访问标识
    u2             name_index;  //名称索引
    u2             descriptor_index;  //描述符索引
    u2             attributes_count;  //属性个数
    attribute_info attributes[attributes_count]; //属性表的具体内容
	}
	
方法访问标识如下：(表中加粗项是方法独有的)

	标识名	标识值	解释
	ACC_PUBLIC	0x0001	声明为 public; 可以从包外部访问
	ACC_PRIVATE	0x0002	声明为 private; 只有定义的类可以访问
	ACC_PROTECTED	0x0004	声明为 protected;只有子类和相同package的类可访问
	ACC_STATIC	0x0008	声明为 static；属于类变量
	ACC_FINAL	0x0010	声明为 final; 不能被覆写
	**ACC_SYNCHRONIZED**	0x0020	声明为 synchronized; 同步锁包裹
	ACC_BRIDGE	0x0040	桥接方法, 由编译器生成
	**ACC_VARARGS**	0x0080	声明为 接收不定长参数
	**ACC_NATIVE**	0x0100	声明为 native; 由非Java语言来实现
	**ACC_ABSTRACT**	0x0400	声明为 abstract; 没有提供实现
	**ACC_STRICT**	0x0800	声明为 strictfp; 浮点模式是FP-strict
	ACC_SYNTHETIC	0x1000	声明为 synthetic; 不存在于源代码，由编译器生成
	
- 对于方法里的Java代码，进过编译器编译成字节码指令后，存放在方法属性表集合中“code”的属性内。
- 当子类没有覆写父类方法，则方法集合中不会出现父类的方法信息。
- Java语言中重载方法，必须与原方法同名，且特征签名不同。特征签名是指方法中各个参数在常量池的字段符号引用的集合，不包括返回值。当时Class文件格式中，特征签名范围更广，允许方法名和特征签名都相同，但返回值不同的方法，合法地共存子啊同一个Class文件中。

## 3.9 属性表
属性表格式：

	attribute_info {
    u2 attribute_name_index;   //属性名索引
    u4 attribute_length;       //属性长度
    u1 info[attribute_length]; //属性的具体内容
	}

属性表的限制相对宽松，不需要各个属性表有严格的顺序，只要不与已有的属性名重复，任何自定义的编译器都可以向属性表中写入自定义的属性信息，Java虚拟机运行时会忽略掉无法识别的属性。 关于虚拟机规范中预定义的属性，这里不展开讲了，列举几个常用的。

	属性名	使用位置	解释
	Code	方法表	方法体的内容
	ConstantValue	字段表	final关键字定义的常量值
	Deprecated	类、方法表、字段表	声明为deprecated
	InnerClasses	类文件	内部类的列表
	LineNumberTable	Code属性	Java源码的行号与字节码指令的对应关系
	LocalVariableTable	Code属性	方法的局部变量描述
	Signature	类、方法表、字段表	用于支持泛型的方法签名，由于Java的泛型采用擦除法，避免类型信息被擦除后导致签名混乱，Signature记录相关信息

### 3.9.1 Code属性 
Code属性java程序方法体中的代码，经编译后得到的字节码指令存储在Code属性内，Code属性位于方法表的属性集合中。但与native或者abstract的方法则不会存在Code属性中。

Code属性的格式如下：

	Code_attribute {
    u2 attribute_name_index; //常量池中的uft8类型的索引，值固定为”Code“
    u4 attribute_length; //属性值长度，为整个属性表长度-6
    u2 max_stack;   //操作数栈的最大深度值，jvm运行时根据该值佩服栈帧
    u2 max_locals;  //局部变量表最大存储空间，单位是slot
    u4 code_length; // 字节码指令的个数
    u1 code[code_length]; // 具体的字节码指令
    u2 exception_table_length; //异常的个数
    {   u2 start_pc;
        u2 end_pc;
        u2 handler_pc; //当字节码在[start_pc, end_pc)区间出现catch_type或子类，则转到handler_pc行继续处理。
        u2 catch_type; //当catch_type=0，则任意异常都需转到handler_pc处理
    } exception_table[exception_table_length]; //具体的异常内容
    u2 attributes_count;     //属性的个数
    attribute_info attributes[attributes_count]; //具体的属性内容
	}

- slot是虚拟机中局部变量分配内存使用的最小单位。**对于byte/char/float/int/short/boolean/returnAddress等长度不超过32位的局部变量，每个占用1个Slot；对于long和double这两种64位的数据类型则需要2个Slot来存放。**


- 实例方法中有隐藏参数this, 显式异常处理器的参数，方法体定义的局部变量都使用局部变量表来存放。

- max_locals，不是所有局部变量所占Slot之和，因为Slot可以重用，javac编译器会根据变量的作用域来分配Slot给各个变量使用，从而计算出max\_locals大小。

- 虚拟机规范限制严格方法不允许超过65535个字节码，否则拒绝编译。

Code属性是Class文件中最重要的属性，Java程序的Class信息分为代码(方法体中的Java代码)和元数据(包含类、接口、字段、方法定义以及其他信息)两部分。

ConstantValue属性 ConstantValue属性是指被static关键字修饰的变量（也称为类变量）。

- 类变量: 在类构造器方法或者使用ConstantValue属性来赋值
- 实例变量：在实例构造器方法进行赋值


# 4 实例

	public class TestByteCode extends TestClass implements Test {
	    public static int i = 1;
	    public final static int j = 1;
	    public int k = 1;
	
	    class TestChild{
	        public int t_c_i;
	    }
	    static {
	        System.out.println("static");
	    }
	    public int function() throws Exception {
	        try {
	            this.k = 100;
	        }catch (Exception e){
	            System.out.println("error");
	        }finally {
	            System.out.println("finally");
	        }
	        return this.k;
	    }
	    public static int static_function() {
	        return 2;
	    }
	}   
	
	其中 TestClass 与 Test 定义分别为：
	public class TestClass {
	    public int t_c;
	}
	public interface Test {
	    public int t_i = 10;
	}

经过`javap -v `后生成的文件内容

	public class com.acyouzi.reflect.TestByteCode extends com.acyouzi.reflect.TestClass implements com.acyouzi.reflect.Test
	// minor major version
	minor version: 0
	major version: 52
	// 访问标志
	flags: ACC_PUBLIC, ACC_SUPER
	// 常量池，总共62项
	Constant pool:
	// 第一项是实例构造函数，分别引用了常量池中11行和41行的内容，可以看到完整的方法签名构成
	#1 = Methodref          #11.#41        // com/acyouzi/reflect/TestClass."<init>":()V
	#2 = Fieldref           #10.#42        // com/acyouzi/reflect/TestByteCode.k:I
	.
	.
	.
	#24 = Utf8               Code
	#25 = Utf8               LineNumberTable
	#26 = Utf8               LocalVariableTable
	#27 = Utf8               this
	.
	.
	.
	#61 = Utf8               println
	#62 = Utf8               (Ljava/lang/String;)V
	{
	// 可以看到父类，接口的参数不会在class文件中体现出来
	public static int i;
	    descriptor: I
	    flags: ACC_PUBLIC, ACC_STATIC
	
	public static final int j;
	    descriptor: I
	    flags: ACC_PUBLIC, ACC_STATIC, ACC_FINAL
	    // 前面介绍的关于 static 赋初值的两种方式之2，final + static + 基本数据类型或者String 
	    ConstantValue: int 1
	
	public int k;
	    descriptor: I
	    flags: ACC_PUBLIC
	
	// 构造函数
	public com.acyouzi.reflect.TestByteCode();
	    descriptor: ()V
	    flags: ACC_PUBLIC
	    Code:
	    // 栈深度为2，局部变量所需空间1， args_size
	    stack=2, locals=1, args_size=1
	        0: aload_0
	        // 执行父类的构造方法
	        1: invokespecial #1                  // Method com/acyouzi/reflect/TestClass."<init>":()V
	        4: aload_0
	        5: iconst_1
	        // 给非静态变量 k 赋值
	        6: putfield      #2                  // Field k:I
	        9: return
	    // 参数名变量对应关系表
	    LineNumberTable:
	        line 3: 0
	        line 6: 4
	    // 本地变量，可以看到直接持有this指针
	    LocalVariableTable:
	        Start  Length  Slot  Name   Signature
	            0      10     0  this   Lcom/acyouzi/reflect/TestByteCode;
	
	// 这部分注意异常情况
	public int function() throws java.lang.Exception;
	    descriptor: ()I
	    flags: ACC_PUBLIC
	    Code:
	    stack=2, locals=3, args_size=1
	        0: aload_0
	        1: bipush        100
	        3: putfield      #2                  // Field k:I
	        6: getstatic     #3                  // Field java/lang/System.out:Ljava/io/PrintStream;
	        9: ldc           #4                  // String finally
	        11: invokevirtual #5                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
	        // 没有异常就跳到最后的 return 语句
	        14: goto          48   
	        17: astore_1
	        18: getstatic     #3                  // Field java/lang/System.out:Ljava/io/PrintStream;
	        21: ldc           #7                  // String error
	        23: invokevirtual #5                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
	        26: getstatic     #3                  // Field java/lang/System.out:Ljava/io/PrintStream;
	        29: ldc           #4                  // String finally
	        31: invokevirtual #5                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
	        34: goto          48
	        37: astore_2
	        38: getstatic     #3                  // Field java/lang/System.out:Ljava/io/PrintStream;
	        41: ldc           #4                  // String finally
	        43: invokevirtual #5                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
	        46: aload_2
	        47: athrow
	        48: aload_
	        49: getfield      #2                  // Field k:I
	        52: ireturn
	    // 查表判断异常怎么处理，
	    Exception table:
	        from    to  target type
	            // 0-6 行出现 Exception 类型异常要跳到 17 行执行处理逻辑
	            0     6    17   Class java/lang/Exceptio
	            0     6    37   any
	            17    26    37   any
	    // 用于类型检查
	    StackMapTable: number_of_entries = 3
	        frame_type = 81 /* same_locals_1_stack_item */
	        stack = [ class java/lang/Exception ]
	        frame_type = 83 /* same_locals_1_stack_item */
	        stack = [ class java/lang/Throwable ]
	        frame_type = 10 /* same */
	    // 异常属性，列出了可能抛出的异常
	    Exceptions:
	    throws java.lang.Exception
	
	// 静态方法，不持有 this 指针
	public static int static_function();
	    descriptor: ()I
	    flags: ACC_PUBLIC, ACC_STATIC
	    Code:
	    stack=1, locals=0, args_size=0
	        0: iconst_2
	        1: ireturn
	    LineNumberTable:
	        line 25: 0
	
	// 构造代码块 是不是等价于 < clinit >?
	static {};
	    descriptor: ()V
	    flags: ACC_STATIC
	    Code:
	    stack=2, locals=0, args_size=0
	        0: iconst_1
	        // static 变量 i 赋值初值
	        1: putstatic     #8                  // Field i:I
	        4: getstatic     #3                  // Field java/lang/System.out:Ljava/io/PrintStream;
	        7: ldc           #9                  // String static
	        9: invokevirtual #5                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
	        12: return
	    LineNumberTable:
	        line 4: 0
	        line 12: 4
	        line 13: 12
	}
	SourceFile: "TestByteCode.java"
	// InnerClass的内容
	InnerClasses:
	    #14= #13 of #10; //TestChild=class com/acyouzi/reflect/TestByteCode$TestChild of class com/acyouzi/reflect/TestByteCode


- final static 与 static 变量赋初值的位置，虽然在定义的位置就写出赋值，但是可能并不是声明完就立即赋初值，下面的非静态变量也是一样。
- this 指针在非静态方法中被默认添加到本地变量表
- 不管有没有写明，在构造函数第一句都会调用父类的构造方法
- 非静态变量赋初值会被移动到构造方法中，紧跟在父类构造方法调用语句之后
- 异常的处理是通过 exception_table