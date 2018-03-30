# 简介
- ASM是一款基于java字节码层面的代码分析和修改工具，ASM的目标是生成,转换和分析已编译的java class文件。

- ASM有两套API，一套基于访问者模式、一套基于树的数据结构。
	
	- 基于访问者模式的api，类中的每个数据结构都是一个Event，类的生成同样基于这样的Event。
	- 基于树的数据结构的api是面向对象的一种设计，类被表示为一个对象。这两套api的区别类似xml解析中的SAX和DOM。两套api都是对同一个class操作的，如果用户需要修改相关联的类需要自己手动管理。

- 参考链接：

	[ASM库的原理和使用方法 1](http://www.apmbe.com/java-asm%E5%BA%93%E7%9A%84%E5%8E%9F%E7%90%86%E4%B8%8E%E4%BD%BF%E7%94%A8%E6%96%B9%E6%B3%95%EF%BC%88%E4%B8%80%EF%BC%89/)
	
	[ASM简介](http://www.jianshu.com/p/85502e42bbb6)
	
	[ASM-API文档](http://asm.ow2.org/asm50/javadoc/user/index.html)
	
	[ASM-PDF 使用介绍](http://download.forge.objectweb.org/asm/asm4-guide.pdf)

	[java 虚拟机的堆栈](https://www.zhihu.com/question/29833675)

	[ASM-Maven地址](https://mvnrepository.com/artifact/org.ow2.asm/asm)

#  CoreAPI-Classes部分

## 1.1 结构
- 编译过的类在源码中保留着结构性的信息以及几乎所有的符号,类包含以下几部分：

	- 类信息。包括描述类的 修饰符(例如`public`,`private`),名称，父类，接口，注解（modifiers, the name, the type and the annotations）

	- 在此类中声明的每一个字段的信息。包含描述字段的修饰符，名称，类型，注解 （modifiers, the name, the type and the annotations）

	- 在此类中声明的每一个方法和构造方法。包含描述方法的修饰符，名称，返回值和参数的类型，注解（the modifiers, the name, the return and parameter types,and the annotations）。 当然还有以java字节码指令序列形式存在的方法具体实现代码(就是被方法体内的代码，然后是被编译过的)。

- 源码和字节码的区别
	- 类的字节码(编译过后的类)只能包含一个类，源码可以包含多个类（内部类）
	- 类的字节码不包含注解，但是可以包含 类，字段，方法和 用于将附加信息和这些元素进行关联的属性(class, field, method and code attributes)。 在Java 5 开始，annotations 可以实现相同的目的，所以属性变得越来越无关紧要
	- 类的字节码中不包含`package`和`import`部分，所以所有的类型名称都必须使用全名
	- 类的字节码存在一个常量池(constant pool),这个常量池包含出现在类中的数字，字符和类型常量。这些常量只被定义一次，并在类的字节码中的不同地方引用其索引。
	
	![](http://ww1.sinaimg.cn/large/6ab93b35ly1fkowhyp68uj20me0ewjt4.jpg)
	
	- Java 类型 在 源码和 类的字节码中 是不同的表现形式..这点可以在 **Class文件格式.md** 中查看

### 1.1.1 internal names(内部名称)
- 大部分情况，类型被限制为类或接口类型。例如 类的超类，类实现的接口或者方法抛出的异常 这些都不能是基本类型或数组类型，基本都是类和接口类型 。  

- 这些类型用内部名称在类的字节码中表示。

- 类的内部名称只是这个类的完全限定名，其中的点用斜杠替换。例如，字符串的内部名称为java /lang/ String。

### 1.1.2 Type descriptors(类型描述符)
- 内部名称仅用于被限制为类或接口类型的类型

- 在所有其他情况下，例如字段类型，Java 类型 在 类的字节码中 都是通过类型描述符(type descriptiors)来表现的。

![](http://ww1.sinaimg.cn/large/6ab93b35ly1fkox3jxph3j20ff0ctgmi.jpg)

### 1.1.3 Method descriptors 

- 方法描述符是用于描述 方法的参数类型和返回值类型 的类型描述符列表

- 方法描述符 由一个 左括号开始，接着是每个参数的类型描述符，然后再添加 对应的右括号。紧接着的是返回值的类型描述符，如果返回值为void，则添加V（**方法描述符不包含方法的名称或参数名称**）

	![](http://ww1.sinaimg.cn/large/6ab93b35ly1fkox4nioawj20nw060t9i.jpg)

## 1.2 接口和组件
- **Class的生成和转换是基于`ClassVisitor`这个抽象类**。该类每个方法都对应class的一个结构。

- class中简单的结构 可以通过一个 **参数为描述其的内容，返回为void** 的方法来访问

- class中复杂的结构（其内容可以是任意长度和复杂度的部分） 通过一个有初始化方法调用  并返回辅助的visitor class 的方法 进行访问。例如:`visitAnnotation`,`visitField`,`visitMethod`需要返回`AnnotationVisitor`,`FieldVisitor`,`MethodVisitor`

- `ClassVisitor`方法调用必定按照如下顺序:`visit visitSource? visitOuterClass? ( visitAnnotation | visitAttribute )*
( visitInnerClass | visitField | visitMethod )*
visitEnd`

- `ClassReader`用来解析编译过后的类的字节码数组.通过`accept`方法与`ClassVisitor`相关联，并在解析过程中会调用`ClassVisitor`相应的visitXXX方法。**可以被看做一个事件生产者**.

- `ClassWriter`,是`ClassVisitor`这个抽象类的子类，以二进制形式直接构建编译过的类(字节码)。它可以生成一个类的字节码数组,通过`toByteArray`方法输出字节码数组。**可以被看做是一个事件消费者**。

- `ClassVisitor`，cv将其所有收到的方法委托给另外一个cv实例。**可以被看做一个事件过滤器**


### 1.2.1 解析字节码
- 解析字节码的必须的组件是`ClassReader`

- `ClassReader`可以通过 类的全名，字节数组或`InputStream`.

- 可以通过`ClassLoader`的`getResourceAsStream `获取内容的输入流

		cl.getResourceAsStream(classname.replace(’.’, ’/’) + ".class");


**例子：**
实现一个类似`javap`工具的打印类内容的功能
1. step1 编写`ClassVisitor`,打印它访问的类的信息
	
		public class ClassPrinter extends ClassVisitor {
		    public ClassPrinter() {
		        super(ASM4);
		    }
		
		    public void visit(int version, int access, String name,
		                      String signature, String superName, String[] interfaces) {
		        System.out.println(name + " extends " + superName + " {");
		    }
		
		    public void visitSource(String source, String debug) {
		    }
		
		    public void visitOuterClass(String owner, String name, String desc) {
		    }
		
		    public AnnotationVisitor visitAnnotation(String desc,
		                                             boolean visible) {
		        return null;
		    }
		
		    public void visitAttribute(Attribute attr) {
		    }
		
		    public void visitInnerClass(String name, String outerName,
		                                String innerName, int access) {
		    }
		
		    public FieldVisitor visitField(int access, String name, String desc,
		                                   String signature, Object value) {
		        System.out.println(" " + desc + " " + name);
		        return null;
		    }
		
		    public MethodVisitor visitMethod(int access, String name,
		                                     String desc, String signature, String[] exceptions) {
		        System.out.println(" " + name + desc);
		        return null;
		    }
		
		    public void visitEnd() {
		        System.out.println("}");
		    }
		}

2. step2 将`ClassPrinter`和`ClassReader` 通过`accept`进行关联。这样cr生成的事件就会被cp消费.

		ClassPrinter cp = new ClassPrinter();
		ClassReader cr = new ClassReader("java.lang.Runnable");
		cr.accept(cp, 0);

	输出结果：
	
		java/lang/Runnable extends java/lang/Object {
		run()V
		}

### 1.2.2 生成字节码
- 生成字节码必须的组件是`ClassWriter`

**例子:**

1. 如下是一个接口

		package pkg;
		
		public interface Comparable extends Mesurable {
		    int LESS = -1;
		    int EQUAL = 0;
		    int GREATER = 1;
		
		    int compareTo(Object o);
		}

2. 可以通过调用`ClassWriter`的方法生成

        ClassWriter cw=new ClassWriter(0);

        cw.visit(V1_5,ACC_PUBLIC+ACC_ABSTRACT+ACC_INTERFACE,"pkg/Comparable",null,"java/lang/Object",new String[]{"pkg/Mesurable"});

        cw.visitField(ACC_PUBLIC+ACC_FINAL+ACC_STATIC,"LESS","I",null,new Integer(-1)).visitEnd();

        cw.visitField(ACC_PUBLIC+ACC_FINAL+ACC_STATIC,"EQUAL","I",null,new Integer(0)).visitEnd();

        cw.visitField(ACC_PUBLIC+ACC_FINAL+ACC_STATIC,"GREATER","I",null,new Integer(1)).visitEnd();

        cw.visitMethod(ACC_PUBLIC+ACC_ABSTRACT,"compareTo","(Ljava/lang/Object;)I",null,null).visitEnd();

        cw.visitEnd();

        byte[]b=cw.toByteArray();

	- `visit`方法定义了类的头。其中`V1_5`是一个常量，它指定了类的版本。`ACC_xxxx`是一个常量，是与java修饰符对应的标志（在例子中，指定了该类是一个接口，并且是public和abstract的） 。 参数`pkg/Comparable`指定了类名(类的字节码中不包含包或导入包，所有的类名都是全限定名) 。下一个参数`null`对应于泛型。`"pkg/Comparable"`参数是指的超类(接口类隐式继承Object)。`new String[]{"pkg/Mesurable"}`参数是拓展的接口数组。

	- `visitField`方法用于定义字段。第一个`ACC_PUBLIC+ACC_FINAL+ACC_STATIC`参数定义了Java中的修饰符public static fianl(接口中字段默认的修饰符).第二个参数是字段的名称.第三个参数`I`是类型类型描述符形式的字段类型。 第四个参数`null`,是泛型。 第五个参数`new Integer(-1)` 是字段的常量值，只能用于真正的常量字段，即最终静态字段，对于其他字段，它必须为null。 
		- 由于这里没有任何注释,所以立即调用`visitEnd`会返回`FieldVisitor`.并且不会再调用这个字段的`visitAnnotation`或`visitAtribute`方法

	- `visitMethod`方法用于定义方法(例子中的`compareTo`).第一个参数`ACC_PUBLIC+ACC_ABSTRACT`是Java访问修饰符。 第二个参数`compareTo`代表方法名称。 第三个参数`(Ljava/lang/Object;)I`是方法的描述符(参数类型和返回值类型)。 第四个参数`null` 对应泛型 。 第五个参数`null`,是由方法引起的异常数组，由内部名称(包含包名的类名)指定。
		- `visitMethod`方法返回一个`MethodVisitor`,可以用来定义方法的`annotations `,`annotations `,以及方法的具体实现代码。最终还需要调用`MethodVisitor`的`visitEnd`来结束

	- `visitEnd`,在类,方法和字段的最后 都需要调用`visitEnd`.用来表示结束。

	- `toByteArray`用来获得字节码的字节数组.

#### 1.2.2.1 使用生成的字节码

- 生成了的字节码通常会存放到 一个`.class`文件中。可以通过`ClassLoader`动态加载.

- 方式1：就是定义定义一个继承自`ClassLoader`,实现其`defineClass`方法。

		class MyClassLoader extends ClassLoader {
			public Class defineClass(String name, byte[] b) {
				return defineClass(name, b, 0, b.length);
			}
		}
	
	字节码通过以下方式加载

		Class c = myClassLoader.defineClass("pkg.Comparable", b);

- 方式2：定义一个继承自`ClassLoader`的方法，并重写其`findClass`方法。这样就可以实现加载之后 直接进行改写并返回。

		class StubClassLoader extends ClassLoader {
		    @Override
		    protected Class findClass(String name)
		            throws ClassNotFoundException {
		        if (name.endsWith("_Stub")) {
		            ClassWriter cw = new ClassWriter(0);
		            ...
		            byte[] b = cw.toByteArray();
		            return defineClass(name, b, 0, b.length);
		        }
		        return super.findClass(name);
		    }
		}

- 具体生成类的方式取决去当时的上下文。

### 1.2.3 改造字节码
- 第一步就是将`ClassReader`通过`accept`方法与`ClassWriter`进行关联.

		byte[] b1 = ...;
		ClassWriter cw = new ClassWriter(0);
		ClassReader cr = new ClassReader(b1);
		cr.accept(cw, 0);
		byte[] b2 = cw.toByteArray(); // b2 represents the same class as b1

- 第二步需要将 `ClassVisitor`加入使用，组成一个结构**`Reader`->`Adapter`->`Writer`**;

		byte[] b1 = ...;
		ClassWriter cw = new ClassWriter(0);
		****************
		// cv forwards all events to cw
		ClassVisitor cv = new ClassVisitor(ASM4, cw) { };
		****************
		ClassReader cr = new ClassReader(b1);
		cr.accept(cv, 0);
		byte[] b2 = cw.toByteArray(); // b2 represents the same class as b1

	- 目前字节码的字节数组并没有被过滤，因为`ClassVisitor`中并没有重写cv中的代码并添加改造的逻辑代码。 

- 第三步，重写`ClassVisitor`中的方法,加入改造的逻辑代码。

		public class ChangeVersionAdapter extends ClassVisitor {
		    public ChangeVersionAdapter(ClassVisitor cv) {
		        super(ASM4, cv);
		    }
		    @Override
		    public void visit(int version, int access, String name,
		                      String signature, String superName, String[] interfaces) {
		        cv.visit(V1_5, access, name, signature, superName, interfaces);
		    }
		}

	- 上述代码中的cv 其实就是传入 adapter的cw
	- 实现了改写类的版本号的功能，改成java 1.5

- 通过修改`visit`方法中的其他参数，可以实现其他转换功能。例如可以将接口添加到以实现的接口列表中，可以改变类的名称(实际上，类的名称出现在编译的类中的许多不同的地方，需要将所有的这些地方都修改才能真正的重命名类！)

#### 1.2.3.1 优化

- 上述的例子仅仅修改了 java的版本号，改动才不到4个字节，却需要解析整个字节码的字节数组，不是很效率。 


- 可以通过直接拷贝`b1`中不需要改造的部分到`b2`中去来提高效率。

- ASM自动为方法完成了以上优化：
	- 如果`ClassReader`检测到`ClassVisitor`返回了由`ClassWriter`返回的`MethodVisitor`，并且这个`MethodVisitor`被传入cr的`accpet`方法中。这意味着这个方法不用改造。

	        byte[] b1 = ...
	        ClassReader cr = new ClassReader(b1);
	        ClassWriter cw = new ClassWriter(cr, 0);
	        ChangeVersionAdapter ca = new ChangeVersionAdapter(cw);
	        cr.accept(ca, 0);
	        byte[] b2 = cw.toByteArray();

- 这种方式有一种缺点就是，会拷贝源类文件中所有的已定义的常量 到被改造的字节码中，与不采用这种优化方式的写法相比，会导致类文件增大。 所以这种方式 只适合在 需要添加方法，字段 等添加性的改造时使用。

### 1.2.3.1 适用改造字节码的场景
- 改造后的字节码可以被保存在磁盘中，通过`ClassLoader`加载并使用。

- 在`ClassLoader`中改造字节码的话，只能改造被其载入的类，如果想要改造所有的类，需要使用到`ClassFileTransformer`(在`java.lang.instrument`包中被定义)

- 实例如下：
	
		  public static void premain(String agentArgs, Instrumentation inst) {
		        inst.addTransformer(new ClassFileTransformer() {
		            public byte[] transform(ClassLoader l, String name, Class c,
		                                    ProtectionDomain d, byte[] b)
		                    throws IllegalClassFormatException {
		                ClassReader cr = new ClassReader(b);
		                ClassWriter cw = new ClassWriter(cr, 0);
		                ClassVisitor cv = new ChangeVersionAdapter(cw);
		                cr.accept(cv, 0);
		                return cw.toByteArray();
		            }
		        });
		    }

### 1.2.4 移除类中成员
- 之前小节中的改造类的java 版本的功能，也可以被应用到`ClassVisitor`中的其他方法中。例如可以通过修改`visitField`和`visitMethod`方法的`access`和`name` 来改变字段或方法的名称和修饰符。 **更进一步的功能是，可以不调用此方法（visitField/visitMethod等)来移除指定元素。**

#### 1.2.4.1 移除内部类，外部类，源文件
- 实例：

		public class RemoveDebugAdapter extends ClassVisitor {
		    public RemoveDebugAdapter(ClassVisitor cv) {
		        super(ASM4, cv);
		    }
		
		    @Override
		    public void visitSource(String source, String debug) {
				//super.visitSource(source, debug);
		    }
		
		    @Override
		    public void visitOuterClass(String owner, String name, String desc) {
				//super.visitOuterClass(owner, name, desc);
		    }
		
		    @Override
		    public void visitInnerClass(String name, String outerName,String innerName, int access) {
				 //super.visitInnerClass(name, outerName, innerName, access);

		    }
		}

	- 以上例子中移除了`visitInnerClass`,`visitOuterClass`,`visitSource`中调用父类的方法，实现了删除外部类和内部类，以及被编译过的类的源文件。这都是通过不在指定的`visit`方法中转发任何`visit`方法实现的.

#### 1.2.4.2 移除方法和字段
- 1.6.1 小节的策略对字段和方法不起作用，因为`visitField`和`visitMethod`必须返回一个结果。

- 针对方法和字段的移除，首先必须不转发方法的调用(visitMehtod,visitField),其次需要返回一个null。

- 如下实例通过指定 **方法名和描述符(入参和返回值类型)** 来移除特定方法(关指定方法名是不够的,因为方法会有重载,即方法名相同 参数不同)

		public class RemoveMethodAdapter extends ClassVisitor {
		    private String mName;
		    private String mDesc;
		
		    public RemoveMethodAdapter(
		            ClassVisitor cv, String mName, String mDesc) {
		        super(ASM4, cv);
		        this.mName = mName;
		        this.mDesc = mDesc;
		    }
		
		    @Override
		    public MethodVisitor visitMethod(int access, String name,
		                                     String desc, String signature, String[] exceptions) {
		        if (name.equals(mName) && desc.equals(mDesc)) {
		// do not delegate to next visitor -> this removes the method
		            return null;
		        }
		        return cv.visitMethod(access, name, desc, signature, exceptions);
		    }
		}

### 1.2.5 添加类中成员
- 可以通过添加更多的`visitXXX`，来添加类的成员。添加的`visitXXX`方法可以被插入到类的原来的`visitXXX`之间，前提是命令在指定`visitXxx`方法中被调用。

- 例如想添加字段到类中，就需要在原来的`visitField`方法调用之间插入新的.另外必须把这个新的方法调用放到class adapter 中。

- 在`visit`,`visitSource`,`visitOuterClass`,`visitAnnotation`或`visitAttribute` 插入是没有意义的！只有在`visitInnerClass`,`visitField`,`visitMethod`或`visitEnd`是有效的。

- 如果在`visitEnd`中添加新的方法调用()，那这个字段会被添加到任何调用过`visitEnd`的地方(除非添加了明确的条件)，实际上`visitEnd`总是被调用！

- 如果在`visitField`或`visitMethod`中添加新的方法调用(即visitField,visitMethod),那将多种字段：1.类的成员变量 2.方法中的字段.这主要取决于需求是什么，例如，可以在类中添加一个单独的计数器字段用来计算对象被调用的次数 ，或者在单个方法中添加一个计数器字段用来计算方法被调用的次数。

- **注意:**唯一正确的添加成员的时机是在`visitEnd`中进行！因为一个类不能添加重复的成员，而唯一的确定被添加成员是否唯一的方法是跟原来的所有的成员进行对比，并且只有在所有成员都被访问过之后才能确定(也就是在`visitEnd`中)！ 当然也可以使用一些特别奇葩的名称(`_counter$`) 来确唯一性，这样就可以不用在`visitEnd`中使用了

- **注意：** Tree API 没有这个限制！


- 实例：
	
		public class AddFieldAdapter extends ClassVisitor {
		    private int fAcc;
		    private String fName;
		    private String fDesc;
		    private boolean isFieldPresent;
		
		    public AddFieldAdapter(ClassVisitor cv, int fAcc, String fName,
		                           String fDesc) {
		        super(ASM4, cv);
		        this.fAcc = fAcc;
		        this.fName = fName;
		        this.fDesc = fDesc;
		    }
		
		    @Override
		    public FieldVisitor visitField(int access, String name, String desc,
		                                   String signature, Object value) {
		        if (name.equals(fName)) {
		            isFieldPresent = true;
		        }
		        return cv.visitField(access, name, desc, signature, value);
		    }
		
		    @Override
		    public void visitEnd() {
		        if (!isFieldPresent) {
		            FieldVisitor fv = cv.visitField(fAcc, fName, fDesc, null, null);
		            if (fv != null) {
		                fv.visitEnd();
		            }
		        }
		        cv.visitEnd();
		    }
		}  

	- 字段在`visitEnd`方法中被添加，在`visitMethod`中仅做了检测字段是否已经被添加的操作。
	- 注意：`fv!=null`在`fv.visitEnd`之前，因为`class visitor`可能会返回null

### 1.2.6 转换链(Transformation chains)
- 目前只看到由`ClassReader`,`class adapter`,`ClassWriter`组成的**转换链**，当然也可以组成更复杂的**转换链**，通过添加多个`class adapter`.不同的`class adapter`可以实现独立的类改造，组合之后以实现复杂的改造

- **注意： ** 转换链并不是一定需要被设计成线性的。可以编写一个`ClassVisitor`，在它的`visit`方法中，在同一时间调用多个`ClassVisitor`

		public class MultiClassAdapter extends ClassVisitor {
		    protected ClassVisitor[] cvs;
		    public MultiClassAdapter(ClassVisitor[] cvs) {
		        super(Opcodes.ASM5);
		        this.cvs = cvs;
		    }
		    @Override public void visit(int version, int access, String name,
		                                String signature, String superName, String[] interfaces) {
		        for (ClassVisitor cv : cvs) {
		            cv.visit(version, access, name, signature, superName, interfaces);
		        }
		    }
		    //...
		}

- 对称的几个类适配器可以被委托给同一个`ClassVisitor`,这需要一些预防措施作为前提，例如`visit`和`visitEnd`方法在`ClassVisitor`中仅被调用一次

## 1.3 工具
- ASM 在`org.objectweb.asm.utils`包中提供了几个可以在开发类生成器或类适配器时使用的工具(同时在runtime时又不需要的)。另外ASM 也提供了一个在运行时操作`internal names`,`type descriptors`,`method descriptors`的类。

### 1.3.1 Type
- ASM API暴露出在编译类中的Java类型，例如 `interal names`和`type descriptor`。如果能将Java类型暴露出来就像它在源码中那样，可读性会更强。但这需要在`ClassReader`和`ClassWriter`俩个代表中进行转换，这将降低其性能。这也就是为什么ASM不直接将内部名称和类型描述符转换等效的源代码形式。**但是ASM提供了`Type`类，用于手动进行操作**


- `Type`类对象可以用来表示Java Type,可以通过`type descriptor`或`Class object`构造而来。`Type`类 还包含了用来表示基本类型的静态变量，例如 `Type.INF_TYPE`代表`int`

- `Type`类的`getInternalName`能够返回一个类型的内部名称。例如,`Type.getType(String.class).getInternalName()`返回一个`String.class`的`internalName`(java/lang/String).**这个方法只能被用在 class(类) 或 interface types(接口类型)**

- `Type`类的`getDescriptor`方法返回一个类型的描述符。例如，在代码中可以使用`Type.getType(String.class).getDescriptor().`来替代`Ljava/lang/String`。或者 用`Type.INT_TYPE.getDescriptor()`来替代`I`

- `Type`类对象 也可以用来表示 `method type`（方法类型）。可以通过`method descriptor`或`Mehtod`对象来构造。`getDescriptor`方法返回对应类型对象的类型描述符 。此外`getArgumentTypes and getReturnType `可以用来获取方法的 参数类型和返回值类型 相对应的类型对象。例如，`Type.getArgumentTypes("(I)V")`返回一个包含单一元素类型`Type.INT_TYPE`的数组。 再比如，`Type.getReturnType("(I)V") `返回`Type.VOID_TYPE`

### 1.3.2 TraceClassVisitor

- 为了检查生成或转换的类是否符合预期，`ClassWriter`返回的字节数组是不可读的。如果能用文本来表示，会更可读，而这就是`TraceClassVisitor`提供的功能。这个类扩展了`ClassWriter`,构建被访问类的文本表示，所以可以用`TraceClassVisitor`来替代`ClassWriter` 生成可读的生成的痕迹。或者可以同时使用这俩个类。除了`TraceClassVisitor`默认的行为，还可以将其所有的方法调用委托给另外一个`Visitor`,例如`ClassWriter`

    ClassWriter cw = new ClassWriter(0);
    TraceClassVisitor tcv = new TraceClassVisitor(cw, printWriter);
    tcv.visit(...);
    ...
    tcv.visitEnd();
    byte b[] = cw.toByteArray();

	- 这段代码创建了一个`Tcv`,它将所有调用都委托给了cw（打印类,PrintWriter），并打印这些调用的文本表示：
	
			// class version 49.0 (49)
			// access flags 1537
			public abstract interface pkg/Comparable implements pkg/Mesurable {
			// access flags 25
			public final static I LESS = -1
			// access flags 25
			public final static I EQUAL = 0
			// access flags 25
			public final static I GREATER = 1
			// access flags 1025
			public abstract compareTo(Ljava/lang/Object;)I
			}

- **注意：**可以在生成或转换链的任意点使用`TCV`以便查看该点发生了什么,不仅仅是在cw之前。

- **注意：**这个适配器生成的类的文本表示 可以使用`String.equals()`方法进行比较

### 1.3.3 CheckClassAdapter
- `ClassWriter`不会检查被调用的方法是否具有适当的顺序 以及参数的有效。所以可能生成被Java虚拟机验证器拒绝的无效类。为了尽快检测出这些错误，可以使用`CheckClassAdapter`类，和`TraceClassVisitor`相似，这个类拓展了`ClassVisitor`类，并将所有的方法调用委托给另外一个`ClassVisitor`（例如tcv或cw）. CCA 会先检查方法是否以适当的顺序调用，使用有效的参数，然后再委托给下一个访问者。在出现错误的情况下，会抛出`IllegalStateException or IllegalArgumentException`

	    ClassWriter cw = new ClassWriter(0);
	    TraceClassVisitor tcv = new TraceClassVisitor(cw, printWriter);
	    CheckClassAdapter cv = new CheckClassAdapter(tcv);
	    cv.visit(...);
	    ...
	    cv.visitEnd();
	    byte b[] = cw.toByteArray();

- **注意：** 如果以不同的顺序将这些 class visitor 添加到一条链上，那么它们的执行顺序会不同。例如如下代码，检查将在跟踪之后

		ClassWriter cw = new ClassWriter(0);
		CheckClassAdapter cca = new CheckClassAdapter(cw);
		TraceClassVisitor cv = new TraceClassVisitor(cca, printWriter);

- 与TCV相似，CCA可以在转换链中任意地方被添加而不是仅仅在cw之前去检查类。

### 1.3.4 ASMifier
- 这个类为TCV工具 提供了另外一个后端(默认TCV使用 一个Textifier，产生了之前TCV的输出)。这个后端使的TCV类的每个方法都打印Java code 。例如，调用`visitEnd`会打印`cv.visitEnd()`.带一个带有`ASMifier`后台的TCV访问一个类时，将打印出 ASM生成这个类的源代码。

- ASMifier可以用来访问已存在的类，例如，不知道如何使用ASM生成编译类，那么就编写相应的源代码，然后用javac 编译，并使用ASMifier 访问已编译的类，然后就可以得到 ASM生成这个类的代码。

		java -classpath asm.jar:asm-util.jar \org.objectweb.asm.util.ASMifier \java.lang.Runnable

	生成如下代码：

	    package asm.java.lang;
	    import org.objectweb.asm.*;
	    public class RunnableDump implements Opcodes {
	        public static byte[] dump() throws Exception {
	            ClassWriter cw = new ClassWriter(0);
	            FieldVisitor fv;
	            MethodVisitor mv;
	            AnnotationVisitor av0;
	            cw.visit(V1_5, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
	                    "java/lang/Runnable", null, "java/lang/Object", null);
	            {
	                mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "run", "()V",
	                        null, null);
	                mv.visitEnd();
	            }
	            cw.visitEnd();
	            return cw.toByteArray();
	        }
	    }

# 2 Method
本章介绍了如何通过ASM API 生成 和转换编译过的method。先是介绍了编译过的方法的，然后介绍了生成和转换相应的ASM接口，组件和工具，并提供示例。
## 2.1 Structure 
在编译过的类中，方法代码以一系列字节码指令形式存储。为了生成和转换类，需要了解这些指令以及其工作原理。本节提供了足以开始编写简单的类生成器和转换器指令的概览。如果想了解更完整的定义，应该去阅读java虚拟机规范。
### 3.1.1. Execution model 
- **堆是堆（heap），栈是栈（stack）,堆栈就是栈** 
- **Frame(又称StackFrame/帧栈，方法栈)**
- **operand stack 操作数栈**
- **local variable 局部变量**
- **slot 一种特殊空间**

- 在介绍字节码指令前，需要先介绍java虚拟机执行模型。 

- Java代码在线程中执行，每个线程都有自己的由`frames`（帧栈）组成执行堆栈，每个`frame`表示一个方法调用：即每次调用方法时，一个新的`frame`被push到当前线程的执行堆栈（**thread's execution stack**）。当方法返回时，无论是正常还是异常的情况，这个`frame`从执行堆栈中`pop`，并且在调用方法中继续执行（其`frame`位于栈顶）

- 每个`frame`都包含俩个部分：一个局部变量部分 和 一个**`operand stack`**(操作数栈)部分.局部变量部分包含可以按其索引随意访问的变量。操作数栈部分是被字节码框架当做操作数使用的`stack of value`(栈值)，这意味着栈中的值只能按先进后出的形式来访问。
 
- 不要混淆`operand stack `和`thread's execution stack`,在执行栈中的每一帧都包含它自己的操作数栈。

- 本地变量和操作数栈的大小取决于方法的代码。它在编译时计算，然后和字节码指令一起存储在编译过后的类中。 因此，所有与调用的方法相同frame都有相同的大小，但是对于不同的方法来说 它们的本地变量和操作数栈的大小可能不同

	![An execution stack with 3 frames](http://ww1.sinaimg.cn/large/6ab93b35gy1fks0seayynj209g02xt8l.jpg)

	- 如图显示了带有3`frame`的一个执行栈。 第一个frame包含三个局部变量，**它的操作数栈最大为4个操作数，当前它只包含了2个操作数** 。 第二个frame包含了俩个局部变量，操作数栈中拥有俩个操作数。第三个frame 包含四个局部变量，2个操作数
	- 当它被创建出来时，frame初始化时带有一个空堆栈(empty stack)，局部变量带有目标对象(非静态方法)和方法的参数。 例如，在调用`a.equals(b)`方法的时候，创建了一个空堆栈的frame,局部变量为 a 和 b (其他局部变量尚未初始化)
	- 本地变量和操作数栈中的每个`slot`都可以保存除了`long和double`以外的java值(long 和double 需要俩个slot)。这使得本地变量的管理变得复杂，例如 i的th次方中的参数不一定需要存储在 局部变量i中。 举个栗子：调用`Math.max(1L,2L)`,创建了一个存放在前俩个slot的本地变量1L 和 一个存放在后俩个slot 的本地变量

### 3.1.2. Bytecode instructions (字节码指令)
字节码指令是由 可识别该指令的操作码和固定数量参数 组成。

- 操作码是一个无符号的字节值，因此字节码的名称是由`mnemonic symbol`助词符号识别。 例如，值为0的opcode是通过符号 `NOP`设计的，它对应的指令是 什么都不做。
- 参数指的是定义明确指令行为的静态值，是在opcode 之后赋值的。例如`GOTO`标签指令，它的opcode值为167，作为参数标签它指定下一个被执行的指令。**指令参数不能与指令操作数混淆，参数值是静态的并存储在已编译的代码中，而操作数的值来自操作数栈并只有在运行时才知道**


字节码指令可以分为俩类：一组指令用于将值从本地变量传递给操作数栈，或从操作数栈到本地变量。另一组指令值仅作用于操作数栈，将一些值从栈中pop，然后根据这些弹出的值进行计算，最后将结果push回栈中。

- 第一组：

	`ILOAD, LLOAD, FLOAD, DLOAD, and ALOAD`指令用于读取局部变量并将其值push到操作数栈上。这些指令必须以要读取的本地变量的索引作为参数。`ILOAD`用于加载` boolean, byte, char, short, or int `类型的局部变量。`LLOAD,FLOAD,DLOAD`分别用于加载一个`long, float or double `类型的值(LLOAD,DLOAD实际上加载了俩个slot).`ALOAD`用于加载非原始类型的数据，例如对象和数组引用。对称的，还存在`ISTORE,LSTORE,FSTORE,DSTORE,ASTORE`指令从操作数栈pop出一个值，并存储在其索引对应的局部变量中。

	实际上`XLOAD,XSTORE`都是指定了类型的(几乎所有的指令都是类型化的)。这用于确保不发生非法转换。实际上，用不同的类型去读取存储在局部变量中的值是非法的。但是，将当前值以不同的类型存储到局部变量中是合法的。这意味着局部变化的值可以在方法执行过程中改变

- 第二组：可以分类成如下几类

	- Stack（栈操作相关） 主要用于操作操作栈上的数据：POP,DUP（push复制栈顶的数据）SWAP(交换栈顶的两个元素)

	- Constants（常量相关） 将一个常量push到栈顶ACONST_NULL（pushes null）, ICONST_0(push 0) FCONST_0(push 0f) DCONST_0 BIPUSH b(push byte 类型的 b)SIPUSH s(push short 类型 s) LDC(push 任意类型 int float long double String 或者是class 常量等)

	- Arithmetic and logic(逻辑运算相关) 弹出栈顶几个元素进行运算将结果push到栈顶。xADD xSUB xDIV xREM 分别代表 + - * / % 运算。x可以是‘I’ ‘L’ 'F' 'D' 类似还有和<< >> >>> | ^ & 等相对应的指令。

	- Casts(类型转换相关) 这些指令将栈顶元素弹出，转换类型后入栈。它和java中类型转换相对应。I2F F2D L2D 等 CHECKCAST t将一个引用类型的对象转换为t类型

	- Objects（对象相关）这些指令用于创建对象，锁定对象，检查类型等 NEW type 会将一个type类型的对象入栈。

	- Fields(取值赋值相关) GETFIELD owner name desc 弹出对象引用，将其name对应的变量的值入栈。PUTFIELD将对象弹栈将其值存储在name对应的存储区。这两个指令中弹出的对象必须是owner类型，field的类型必须是desc类型。GETSTATIC和PUTSTATIC是类似的。不过其用于操作静态变量。

	- Methods(方法相关) 这些指令可以调用一个函数或者构造函数。他们会弹出函数参数的个数加上1（调用者对象）个对象，将函数结果入栈。INVOKEVIRTUAL owner name desc 会调用定义在owner类中函数签名为desc名称为name的函数。INVOKESTATIC 用于调用静态方法，INVOKESPECIAL 用于private及构造函数。INVOKEINTERFACE 用于调用定义在接口中的方法。对于java7而言INVOKEDYNAMIC 是用于动态方法调用。

	- Arrays(数组相关) 这些指令主要用于读写数组。xALOAD指令会弹出索引和数组，并且将数组中索引对应的值入栈。xSTORE会弹出一个值，索引和数组，将值存储在数组的索引位置。x可以是I L F D A B C S
	- Jumps（跳转指令） 这些指令会在指定的条件为true或者false的时候跳转到任意指定的指令接着执行。他们对应于高级语言的if for do while break continue 等流程控制语句。IFEQ label会弹出int值，如果该值为0就跳转到label指定的指令。其他的跳转指令也类似：IFNE IFGE ... TABLESWITCH LOOKUPSWITCH 对应于java语言中switch语句。

	- Return（返回）xRETURN 和 RETURN指令被用于终止函数的执行，返回相关值给函数调用者。RETURN对应于 return void, xRETURN用于返回其他值。

### 3.1.3. Examples 
查看一下基本例子，了解字节码具体工作流程，以下是一个bean类：

    package pkg;
    public class Bean {
        private int f;
        public int getF() {
            return this.f;
        }
        public void setF(int f) {
            this.f = f;
        }
    }

get方法对应的字节码指令是:

	ALOAD 0
	GETFIELD pkg/Bean f I
	IRETURN
- 第一个指令是读取局部变量位置0的值，这个值会在这个方法调用frame 创建过程中被初始化，并将值push到操作数栈。
- 第二个指令从栈中弹出第一个指令的值也就是`this`，并push这个对象的f字段也就是`this.f`。
- 第三个指令是从栈中弹出值，并返回给调用者

set方法对应的字节码指令是：

	ALOAD 0
	ILOAD 1
	PUTFIELD pkg/Bean f I
	RETURN

- 第一个指令将`this`push到操作数栈
- 第二个指令将位置1的局部变量push到操作数栈，其值在该方法调用frame创建过程中被初始化并赋值
- 第三个指令弹出 前俩个指令入栈的值，并将int值类型的f字段存储在引用对象中也就是`this.f`
- 第四个指令,隐含在源代码中，但是在字节码中是强制的，它会破坏当前执行frame，并返回给它的调用者。

这个bean类有一个默认的公共构造函数，由编译器生成。这个默认生成的构造函数`Bean() { super(); }`。他对应的字节码指令是：

	ALOAD 0
	INVOKESPECIAL java/lang/Object <init> ()V
	RETURN
- 第一个指令用于加载`this`到操作数栈
- 第二个指令弹出`this`这个值，并调用对象类中定义的`<init>`方法，也就是`super()`(即调用父类的构造函数) 。在源码和字节码中，构造函数的命名方式不同，在字节码中，总是被命名为`<init>`,而在源码中，它们有定义它们类的名称。
- 第三条指令用于返回给调用者，


---
下面来看一个复杂的例子：

    public void checkAndSetF(int f) {
        if (f >= 0) {
            this.f = f;
        } else {
            throw new IllegalArgumentException();
        }
    }

其对应的字节码指令：

		ILOAD 1
		IFLT label
		ALOAD 0
		ILOAD 1
		PUTFIELD pkg/Bean f I
		GOTO end
	label:
		NEW java/lang/IllegalArgumentException
		DUP
		INVOKESPECIAL java/lang/IllegalArgumentException <init> ()V
		ATHROW
	end:
		RETURN

- 第一个指令用于加载局部变量位置1的值到操作数栈上,即`f`
- 第二个指令`IFLT`将`f`弹出,并与0进行比较，如果小于0 则跳转到名字为`label`的标签处，否则就继续执行下一个指令
- 第三到第五个指令 与set方法的指令相同
- 第六个指令`GOTO`，表示无条件的跳转到`end`标签 处的指令,`end`标签处的是返回指令
- `label`标签处的指令
	- 第一条，`NEW`指令创建了一个异常对象，并将其push到了操作数栈
	- 第二条,`DUP`指令在操作数栈上复制了这个值
	- 第三条，`INVOKESPECIAL`指令pop出了 操作数栈中的 俩个 异常值
	- 第四条，调用了构造函数
	- 第五条，`ATHROW`指令，pop出了在操作数栈中剩余的异常值，将其作为一个异常抛出(因此执行流程将不会继续执行指令)

### 3.1.4. Exception handlers 

并没有catch对应的字节码指令，不过函数会和一系列exception handler(异常处理代码)相关联。当抛出指定的异常时对应的handler代码就会执行。因此exception handler就和try catch代码块类似。

    public static void sleep(long d) {
        try {
            Thread.sleep(d);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

编译过后的指令：

	TRYCATCHBLOCK try catch catch java/lang/InterruptedException
	try:
		LLOAD 0
		INVOKESTATIC java/lang/Thread sleep (J)V
		RETURN
	catch:
		INVOKEVIRTUAL java/lang/InterruptedException printStackTrace ()V
		RETURN

- `try `和 `catch` 标签之间的代码对应try 代码块，`catch`标签之后的代码对应catch代码块。`TRYCATCHBLOCK `指定了一个异常处理器 覆盖范围从 try标签到catch标签.在`catch`中处理`InterruptedException`及其子类的异常，这意味着，如果在try catch 之间任何地方抛出这个异常，都会将异常push到空栈上，并执行catch标签

### 3.1.5. Frames（帧）
Java6及以上版本编译的class文件，包含一系列stack map frames来加速jvm对class的校验。它们甚至在运行前就能告知jvm某个frame的符号表及操作栈的详细信息。为此，可以为frame中的每一个指令创建一个frame来查看其运行时的状态

	//运行前的state frame     对应的指令
	[pkg/Bean] []           ALOAD 0
	[pkg/Bean] [pkg/Bean]   GETFIELD
	[pkg/Bean] [I]          IRETURN
	
	//对于 throw new IllegalArgumentException的代码：
	[pkg/Bean I] []                                             NEW
	[pkg/Bean I] [Uninitialized(label)]                         DUP
	[pkg/Bean I] [Uninitialized(label) Uninitialized(label)]    INVOKESPECIAL
	[pkg/Bean I] [java/lang/IllegalArgumentException]           ATHROW


上述的`Uninitialized`只存在于`stack map frame`中，代表内存分配完毕但是构造函数还没调用。`UNINITIALIZED_THIS` 代表被初始化为0 TOP代表未定义类型 NULL代表null

对于编译后的class为了节省空间，实际上并不是每一个指令都对应一个state frame,而是只有跳转指令和异常处理handler 和无条件跳转后面的第一个指令包含state frame. 而其他指令可以从已有的state frames推断出来。为了进一步节省空间，每一个frame只有在和上一个frame不同的时候才会被存储。初始帧由于可以很容易从函数参数中推断，因此不会存储，而后续的帧如果和初始帧相同只需存储 F_SAME即可

	ILOAD 1
	IFLT label
	ALOAD 0
	ILOAD 1
	PUTFIELD pkg/Bean f I
	GOTO end
	label:
	F_SAME
	NEW java/lang/IllegalArgumentException
	        DUP
	INVOKESPECIAL java/lang/IllegalArgumentException <init> ()V
	        ATHROW
	end:
	F_SAME
	        RETURN

## 3.2. Interfaces and components 
### 3.2.1. Presentation 

ASM中的 生成和转换 方法字节码的API 是基于一个 抽象类`MethodVisitor`，它是通过`ClassVisitor`的`visitMethod`方法返回的。

    abstract class MethodVisitor { // public accessors ommited
        MethodVisitor(int api);
        MethodVisitor(int api, MethodVisitor mv);

        AnnotationVisitor visitAnnotationDefault();
        AnnotationVisitor visitAnnotation(String desc, boolean visible);
        AnnotationVisitor visitParameterAnnotation(int parameter,String desc, boolean visible);
        void visitAttribute(Attribute attr);

        void visitCode();
        void visitFrame(int type, int nLocal, Object[] local, int nStack,Object[] stack);
        void visitInsn(int opcode);
        void visitIntInsn(int opcode, int operand);
        void visitVarInsn(int opcode, int var);
        void visitTypeInsn(int opcode, String desc);
        void visitFieldInsn(int opc, String owner, String name, String desc);
        void visitMethodInsn(int opc, String owner, String name, String desc);
        void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
                                    Object... bsmArgs);
        void visitJumpInsn(int opcode, Label label);
        void visitLabel(Label label);
        void visitLdcInsn(Object cst);
        void visitIincInsn(int var, int increment);
        void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels);
        void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels);
        void visitMultiANewArrayInsn(String desc, int dims);
        void visitTryCatchBlock(Label start, Label end, Label handler,
                                String type);
        void visitLocalVariable(String name, String desc, String signature,
                                Label start, Label end, int index);
        void visitLineNumber(int line, Label start);
        void visitMaxs(int maxStack, int maxLocals);
        void visitEnd();
    }

- 如果存在 annotations 或attributes 的字节码那必须先被生成，其次是method的字节码(非抽象的方法)。对于`visitCode`和`visitMaxs`可以被看做函数体的开始和结束，最后需要`visitEnd`代表事件结束。

	    visitAnnotationDefault?
	            ( visitAnnotation | visitParameterAnnotation | visitAttribute )*
	            ( visitCode
	            ( visitTryCatchBlock | visitLabel | visitFrame | visitXxxInsn |
	                    visitLocalVariable | visitLineNumber )*
	    visitMaxs )?
	    visitEnd

---

`ClassVisitor`和`MethodVisitor`可以一起使用以生成一个完整的类

	ClassVisitor cv = ...;
	cv.visit(...);
	MethodVisitor mv1 = cv.visitMethod(..., "m1", ...);
	mv1.visitCode();
	mv1.visitInsn(...);
	...
	mv1.visitMaxs(...);
	mv1.visitEnd();
	MethodVisitor mv2 = cv.visitMethod(..., "m2", ...);
	mv2.visitCode();
	mv2.visitInsn(...);
	...
	mv2.visitMaxs(...);
	mv2.visitEnd();
	cv.visitEnd();

- **注意：**并不是一个`MethodVisitor`结束了 才能开另外一个`MethodVisitor`的操作.

		ClassVisitor cv = ...;
		cv.visit(...);
		MethodVisitor mv1 = cv.visitMethod(..., "m1", ...);
		mv1.visitCode();
		mv1.visitInsn(...);
		...
		MethodVisitor mv2 = cv.visitMethod(..., "m2", ...);
		mv2.visitCode();
		mv2.visitInsn(...);
		...
		mv1.visitMaxs(...);
		mv1.visitEnd();
		...
		mv2.visitMaxs(...);
		mv2.visitEnd();
		cv.visitEnd();

---
ASM提供了三个基于`MethodVisitor`的核心组件用于生成和转换`method`
- `ClassReader`类用来解析编译过的类中的`method`内容，并调用通过`accept`方法接收的CV对象返回的`MethodVisitor`中的指定方法
- `ClassWriter`的`visitMethod`方法返回一个`MethodVisitor`接口的实现类，该接口以二进制形式直接构建字节码形式的方法。
- `MethodVisitor`将收到的方法调用都委托给另外一个`MethodVisitor`实例。可以被看做事件过滤器.

---
- **ClassWriter 选项**

	计算stack map frames 比较困难，必须先计算出所有的frames，然后再找到对应跳转的目标frame或无条件跳转的frame，最后压缩这些剩余frames。计算一个方法的 局部变量和操作数栈的大小也是不容易的，**所以如果希望ASM 自动计算，就需要在创建`ClassWriter`时指定要自动计算的内容**
	
	使用下面的选项是方便的，但是会损耗性能。

	- 对于`new ClassWriter(0)`，没有东西是自动计算的，必须手动计算frames，局部变量，操作数栈的大小。
	
	- 对于`new ClassWriter(ClassWriter.COMPUTE_MAXS)`,会自动计算本地变量和操作数栈的大小。必须手动调用`visitMaxs`(可以使用任意参数，参数会被忽略并重新计算)。在`COMPUTE_MAXS`这种情况下，还必须手动计算frames
	
	- 对于`new ClassWriter(ClassWriter.COMPUTE_FRAMES)`,会自动计算所有的值。可以不必调用`visitFrame`,但是仍然需要调用`visitMaxs`(参数将被忽略和重新计算)。  
	
- **注意：**如果手动计算frames，可以让CW 类执行压缩步骤
- **注意：**为了自动计算frames，有时还需要手动计算俩个类的共同父类。默认情况下CW在`getCommonSuperClass`方法计算，通过反射API和将俩个类加载到jvm。** 如果生成几个互相引用的类，那么被引用的类可能还未生成，所以就需要重写`getCommonSuperClass`方法去解决这个问题。
**

### 3.2.2. Generating methods 

例子：3.1.3中的getF方法，mv是MethodVisitor

	mv.visitCode();
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, "pkg/Bean", "f", "I");
	mv.visitInsn(IRETURN);
	mv.visitMaxs(1, 1);
	mv.visitEnd();

	ALOAD 0
	GETFIELD pkg/Bean f I
	IRETURN

- `visitCode`，调用启动了字节码生成
- `visitVarInsn`,`visitFieldInsn`,`visitInsn`,生成了这个方法的三个指令
- `visitMaxs`，这个调用必须在所有指令完成之后，它被用于定义该方法的执行框架中局部变量和操作数栈的大小
- `visitEnd`,这个调用用来结束方法的生成

---

例子： 

	//源码
	public void checkAndSetF(int f) {
		if (f >= 0) {
			this.f = f;
		} else {
			throw new IllegalArgumentException();
		}
	}

	//指令
		ILOAD 1
	    IFLT label
	    ALOAD 0
	    ILOAD 1
	    PUTFIELD pkg/Bean f I
	    GOTO end
	label:
	    NEW java/lang/IllegalArgumentException
	    DUP
	    INVOKESPECIAL java/lang/IllegalArgumentException <init> ()V
	    ATHROW
	end:
	    RETURN

	//ASM的代码
	mv.visitCode();
	mv.visitVarInsn(ILOAD, 1);
	Label label = new Label();
	mv.visitJumpInsn(IFLT, label);
	mv.visitVarInsn(ALOAD, 0);
	mv.visitVarInsn(ILOAD, 1);
	mv.visitFieldInsn(PUTFIELD, "pkg/Bean", "f", "I");
	Label end = new Label();
	mv.visitJumpInsn(GOTO, end);
	mv.visitLabel(label);
	mv.visitFrame(F_SAME, 0, null, 0, null);
	mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
	mv.visitInsn(DUP);
	mv.visitMethodInsn(INVOKESPECIAL,
	"java/lang/IllegalArgumentException", "<init>", "()V");
	mv.visitInsn(ATHROW);
	mv.visitLabel(end);
	mv.visitFrame(F_SAME, 0, null, 0, null);
	mv.visitInsn(RETURN);
	mv.visitMaxs(2, 2);
	mv.visitEnd();

可以看到visitCode和visitEnd之间，每个指令和ASM的方法调用都是一一对应的(除了标签的声明和构造)

### 3.2.3. Transforming methods
通过修改method adapter 中的方法调用。`MethodVisitor`类提供了基本实现，除了转发它接收到的所有方法调用之外不会做任何事。


举例：删除方法中的NOP命令
	//Method adapter
	public class RemoveNopAdapter extends MethodVisitor {
		public RemoveNopAdapter(MethodVisitor mv) {
			super(ASM4, mv);
		}
	
		@Override
		public void visitInsn(int opcode) {
			if (opcode != NOP) {
				mv.visitInsn(opcode);
			}
		}
	}

	//method adapter 可以被用在如下地方

	public class RemoveNopClassAdapter extends ClassVisitor {
		public RemoveNopClassAdapter(ClassVisitor cv) {
			super(ASM4, cv);
		}
	
		@Override
		public MethodVisitor visitMethod(int access, String name,String desc, String signature, String[] exceptions) {
			MethodVisitor mv;
			mv = cv.visitMethod(access, name, desc, signature, exceptions);
			if (mv != null) {
				mv = new RemoveNopAdapter(mv);
			}
			return mv;
		}
	}

class adapter 只是封装了一个method adapter

---
构建一个method adapter chain 可以和class adapter chain 不同，每个方法都可以有不同的method adapter chain。 例如class adapter 可以只在 方法中删除 NOPs 而不是在构造函数中。

	...
	mv = cv.visitMethod(access, name, desc, signature, exceptions);
	if (mv != null && !name.equals("<init>")) {
		mv = new RemoveNopAdapter(mv);
	}
	...

---
class adapter chain 可以是线性的同时，method adapter chain可以是有分支的

	public MethodVisitor visitMethod(int access, String name,String desc, String signature, String[] exceptions) {
		MethodVisitor mv1, mv2;
		mv1 = cv.visitMethod(access, name, desc, signature, exceptions);
		mv2 = cv.visitMethod(access, "_" + name, desc, signature, exceptions);
		return new MultiMethodAdapter(mv1, mv2);
	}


### 3.2.4. Stateless transformations（无状态转换）
- 假如要测量一个程序中所有类耗费的时间。首先需要为每个类添加一个static timer字段，然后需要将每个类的每个方法的执行时间都添加到timer字段。
源码的实现形式如下：
	
		//变换前
		public class C {
			public void m() throws Exception {
				Thread.sleep(100);
			}
		}
		//变换后
		public class C {
			public static long timer;
		
			public void m() throws Exception {
				timer -= System.currentTimeMillis();
				Thread.sleep(100);
				timer += System.currentTimeMillis();
			}
		}

- 为了了解ASM如何实现这一功能，可以编译这俩个类，并使用`TraceClassVisitor`输出这俩个版本的类(TCV可以使用默认Texifier后端 或 使用ASMifier)。选择默认后端，输出内容中 粗体部分为不同点

	**GETSTATIC C.timer : J
	INVOKESTATIC java/lang/System.currentTimeMillis()J
	LSUB
	PUTSTATIC C.timer : J**
	LDC 100
	**INVOKESTATIC java/lang/Thread.sleep(J)V
	GETSTATIC C.timer : J
	INVOKESTATIC java/lang/System.currentTimeMillis()J
	LADD
	PUTSTATIC C.timer : J**
	RETURN
	MAXSTACK = 4
	MAXLOCALS = 1


---

**可以看到，必须在返回指令之前 往方法开始位置添加 4条指令，往返回指令之前添加4条指令 。另外还需要更新操作数栈的大小。
**
- 方法开始的代码是通过`visitCode`，因此可以在method adapter 中重写这个方法以添加前四个指令。

		public void visitCode() {
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System","currentTimeMillis", "()J");
			mv.visitInsn(LSUB);
			mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
		}

- owner必须在被转换时设置类名称。接下来要实现在`RETURN`或者`xRETURN`或`ATHROW` 会终止方法执行的指令 之前添加剩余的四条指令。 这些指令没有任何参数..所以通过`visitInsn`方法访问。 所以可以重写这个方法以添加剩余的四条命令

		public void visitInsn(int opcode) {
			if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
				mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/System","currentTimeMillis", "()J");
				mv.visitInsn(LADD);
				mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
			}
			mv.visitInsn(opcode);
		}

- 最后，需要更新操作数栈的最大值。之前被添加的指令中有俩个`long`类型的值，因此在操作数栈上需要添加4个slot。 在方法开始时添加四个指令，因为操作数栈在初始化时为空，所以可以知道 操作数栈添加四个指令需要大小为4的栈。然后插入的代码 并不会改变栈的状态(栈pop 多少 就会push多少)。因此，如果原始代码需要大小为 s，则转换方式需要的最大栈大小为max(4,s)。
- 但是在`RETURN`指令之前，并不知道操作栈大小，只知道它小于或者等于s，所以在返回指令之前添加代码 可能需要一个大小为4+s的操作数栈。所以必须重写`visitMaxs`

		public void visitMaxs(int maxStack, int maxLocals) {
			mv.visitMaxs(maxStack + 4, maxLocals);
		}
- 使用`COMPUTE_MAXS`也可以实现，但是手动更新maxStack也很简单。

- 在这次转换中`stack map frames`需不需要进行改变。最初的代码和转换后的都没有包含frames。
- 有没有情况是frames 必须更新的？答案是没有，因为 1)插入的代码没有改变操作栈 2)插入的代码不包含`jump`指令，3) 原始代码的流程控制图没有被修改

---

- 将所有的元素放在相关的`ClassVisitor`和`MethodVisitor`子类中：

		public class AddTimerAdapter extends ClassVisitor {
		    private String owner;
		    private boolean isInterface;
		
		    public AddTimerAdapter(ClassVisitor cv) {
		        super(ASM4, cv);
		    }
		
		    @Override
		    public void visit(int version, int access, String name,
		                      String signature, String superName, String[] interfaces) {
		        cv.visit(version, access, name, signature, superName, interfaces);
		        owner = name;
		        isInterface = (access & ACC_INTERFACE) != 0;
		    }
		
		    @Override
		    public MethodVisitor visitMethod(int access, String name,
		                                     String desc, String signature, String[] exceptions) {
		        MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
		                exceptions);
		        if (!isInterface && mv != null && !name.equals("<init>")) {
		            mv = new AddTimerMethodAdapter(mv);
		        }
		        return mv;
		    }
		
		    @Override
		    public void visitEnd() {
		        if (!isInterface) {
		            FieldVisitor fv = cv.visitField(ACC_PUBLIC + ACC_STATIC, "timer",
		                    "J", null, null);
		            if (fv != null) {
		                fv.visitEnd();
		            }
		        }
		        cv.visitEnd();
		    }
		
		    class AddTimerMethodAdapter extends MethodVisitor {
		        public AddTimerMethodAdapter(MethodVisitor mv) {
		            super(ASM4, mv);
		        }
		
		        @Override
		        public void visitCode() {
		            mv.visitCode();
		            mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
		            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
		                    "currentTimeMillis", "()J");
		            mv.visitInsn(LSUB);
		            mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
		        }
		
		        @Override
		        public void visitInsn(int opcode) {
		            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
		                mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
		                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
		                        "currentTimeMillis", "()J");
		                mv.visitInsn(LADD);
		                mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
		            }
		            mv.visitInsn(opcode);
		        }
		
		        @Override
		        public void visitMaxs(int maxStack, int maxLocals) {
		            mv.visitMaxs(maxStack + 4, maxLocals);
		        }
		    }
		}

	- class adapter 用来实例化method adapter(除了构造函数),添加timer 字段，并存储了类名称。

### 3.2.5. Statefull transformations 

- 更复杂的转换需要记住之前的指令的状态。考虑一个转换情况，删除 `ICONST_0 IADD `序列，其空作用是添加0 。只有当最后一次访问的指令是 ` ICONST_0 `时，才需要删除`IADD`指令。这就需要在method adapter中存储状态，因此这种转换被称为 完全状态转换。

- 当访问到一条指令为 `ICONST_0`时，只有当下一个指令是`IADD`时，它才会被删除。问题是如何知道下一条指令？解决方案就是将这个决定推迟到 下一个指令，如果俩个指令符合条件，就删除俩个指令， 否则就分别发出俩个指令。

- 为了完成删除或替换某些指令，可以添加一个`MethodVisitor`的子类，其`visitXXX`方法调用一个公共的`VisitInsn`

	public abstract class PatternMethodAdapter extends MethodVisitor {
	    protected final static int SEEN_NOTHING = 0;
	    protected int state;
	
	    public PatternMethodAdapter(int api, MethodVisitor mv) {
	        super(api, mv);
	    }
	
	    @Overrid
	    public void visitInsn(int opcode) {
	        visitInsn();
	        mv.visitInsn(opcode);
	    }
	
	    @Override
	    public void visitIntInsn(int opcode, int operand) {
	        visitInsn();
	        mv.visitIntInsn(opcode, operand);
	    }
	
	    ...
	
	    protected abstract void visitInsn();
	}

	上面的抽象类，可以按如下实现方式去实现：

		public class RemoveAddZeroAdapter extends PatternMethodAdapter {
		    private static int SEEN_ICONST_0 = 1;
		
		    public RemoveAddZeroAdapter(MethodVisitor mv) {
		        super(ASM4, mv);
		    }
		
		    @Override
		    public void visitInsn(int opcode) {
		        if (state == SEEN_ICONST_0) {
		            if (opcode == IADD) {
		                state = SEEN_NOTHING;
		                return;
		            }
		        }
		        visitInsn();
		        if (opcode == ICONST_0) {
		            state = SEEN_ICONST_0;
		            return;
		        }
		        mv.visitInsn(opcode);
		    }
		
		    @Override
		    protected void visitInsn() {
		        if (state == SEEN_ICONST_0) {
		            mv.visitInsn(ICONST_0);
		        }
		        state = SEEN_NOTHING;
		    }
		}

	- `visitInsn(int opcode)`首先测试序列是否被检测到。在这种情况下，会重新初始化state的值并返回，这就产生了删除序列的效果。
	- 然后会判断如果这次命令不是`IADD`，而上次时`ICONST_0`,则会将`ICONST_0`指令发出
	- 如果当前指令是`ICONST_0` 会记住这个状态 并返回，以推迟这个指令的决定
	- 其他情况下，指令会被转发给 传入的mv

### 3.2.6 Labels and frames

##3.3. Tools 
###3.3.1. Basic tools 
###3.3.2. AnalyzerAdapter 
###3.3.3. LocalVariablesSorter
###3.3.4. AdviceAdapter 
