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
- **Class的生成和转换是基于`ClassVisitor`这个抽象类**。该类每个方法都对应class的一个结构。

- class中简单的结构 可以通过一个 **参数为描述其的内容，返回为void** 的方法来访问

- class中复杂的结构（其内容可以是任意长度和复杂度的部分） 通过一个有初始化方法调用  并返回辅助的visitor class 的方法 进行访问。例如:`visitAnnotation`,`visitField`,`visitMethod`需要返回`AnnotationVisitor`,`FieldVisitor`,`MethodVisitor`

- `ClassVisitor`方法调用必定按照如下顺序:`visit visitSource? visitOuterClass? ( visitAnnotation | visitAttribute )*
( visitInnerClass | visitField | visitMethod )*
visitEnd`

- `ClassReader`用来解析编译过后的类的字节码数组.通过`accept`方法与`ClassVisitor`相关联，并在解析过程中会调用`ClassVisitor`相应的visitXXX方法。**可以被看做一个事件生产者**.

- `ClassWriter`,是`ClassVisitor`这个抽象类的子类，以二进制形式直接构建编译过的类(字节码)。它可以生成一个类的字节码数组,通过`toByteArray`方法输出字节码数组。**可以被看做是一个事件消费者**。

- `ClassVisitor`，cv将其所有收到的方法委托给另外一个cv实例。**可以被看做一个事件过滤器**


## 1.3 解析字节码
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

## 1.4 生成字节码
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

### 1.4.1 使用生成的字节码

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

## 1.5 改造字节码
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

### 1.5.1 优化

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

### 1.5.2 适用改造字节码的场景
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

## 1.6 移除类中成员
- 之前小节中的改造类的java 版本的功能，也可以被应用到`ClassVisitor`中的其他方法中。例如可以通过修改`visitField`和`visitMethod`方法的`access`和`name` 来改变字段或方法的名称和修饰符。 **更进一步的功能是，可以不调用此方法（visitField/visitMethod等)来移除指定元素。**

### 1.6.1 移除内部类，外部类，源文件
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

### 1.6.2 移除方法和字段
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

## 1.7 添加类中成员
