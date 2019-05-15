# 集成Groovy到应用程序

[Integrating Groovy into applicaitons](http://www.groovy-lang.org/integrating.html)




# 1 Groovy集成机制

**Groovy 语言提供了几种在运行时将自身集成至应用程序(可能是Java或Groovy自身等等)中的方法，功能从最基本的简单代码执行到使用缓存和编译器定制**

本章节的实例在Groovy中集成Groovy脚本(就是Groovy代码)，其同样集成机制也适用于Java

## 1.1 Eval
**使用`groovy.util.Eval`类是实现运行时动态执行`Groovy`脚本的最简单方式。`Eval`类中的方法可以接收参数，并且会将表达式的结果返回**

	import groovy.util.Eval
	
	assert Eval.me('33*3') == 99
	assert Eval.me('"foo".toUpperCase()') == 'FOO'

`Eval`类提供了多个方法，用来接收参数并进行执行:
	
	assert Eval.x(4, '2*x') == 8                
	assert Eval.me('k', 4, '2*k') == 8          
	assert Eval.xy(4, 5, 'x*y') == 20           
	assert Eval.xyz(4, 5, 6, 'x*y+z') == 26  

- `Eval.x()`支持传入一个名称固定为`x`的参数，并且允许在Groovy脚本中使用该参数

- `Eval.xy()`支持传入俩个名称固定为`x`和`y`的参数，并且允许在Groovy脚本中使用该参数

- `Eval.xyz()`支持传入三个名称分别固定为`x`,`y`,`z`的参数，并且允许在Groovy脚本中使用该参数

- `Eval.me()`支持传入一个自定义名称的参数,并且允许在Groovy脚本中使用该参数


**由于`Eval`无法缓存 ，因此仅支持执行简单脚本，但是这不代表`Eval`无法支持执行多行代码!!**

	Eval.me('println "line one"; println "line two"; println "line three"')

## 1.2 GroovyShell
### 1.2.1 Multiple sources
使用`groovy.lang.GroovyShell`类是集成Groovy脚本的首选方法，它能够缓存执行脚本所生成的对象

	def shell = new GroovyShell()                           
	def result = shell.evaluate '3*5'                       
	def result2 = shell.evaluate(new StringReader('3*5'))   
	assert result == result2
	def script = shell.parse '3*5'                          
	assert script instanceof groovy.lang.Script
	assert script.run() == 15  

- `GroovyShell.evaluate()`方法可以像`Eval.me()`方法一样执行Groovy脚本，并获取返回值

- `GroovyShell.evaluate()`方法可以从数据源读取数据(例如,`String`,`Reader`,`File`,`InputStream`)

- `GroovyShell.parse()`通过返回一个`Script`类实例 来延迟Groovy脚本的执行时机


### 1.2.2 Sharing data between a script and the application
通过`groovy.lang.Binding`类可以在Groovy脚本和应用之间共享数据

示例(在应用中往`Binding`中写入数据,Groovy脚本读取写入的数据)

	def sharedData = new Binding()                          
	def shell = new GroovyShell(sharedData)                 
	def now = new Date()
	sharedData.setProperty('text', 'I am shared data!')     
	sharedData.setProperty('date', now)                     
	
	String result = shell.evaluate('"At $date, $text"')     
	
	assert result == "At $now, I am shared data!"

- Groovy脚本也能往`Binding`中写数据

		def sharedData = new Binding()                          
		def shell = new GroovyShell(sharedData)                 
		// 将一个未指定类型的变量保存到Binding中
		shell.evaluate('foo=123')                               
		// 应用获取该值
		assert sharedData.getProperty('foo') == 123  

	- **请注意，如果想在Groovy脚本中往`Binding`写入数据，则变量必须不使用具体类型进行声明(包括`def`)**

		**因为如果显示的指定变量类型或使用`def`,会导致局部变量的生成**

			def sharedData = new Binding()
			def shell = new GroovyShell(sharedData)
			
			shell.evaluate('int foo=123')
			
			try {
			    assert sharedData.getProperty('foo')
			} catch (MissingPropertyException e) {
			    println "foo is defined as a local variable"
			}

**可以利用`parse()`方法返回的`Script`实例，来共享`Binding`的实例**

	def shell = new GroovyShell()
	
	def b1 = new Binding(x:3)                       
	def b2 = new Binding(x:4)                       
	def script = shell.parse('x = 2*x')
	script.binding = b1
	script.run()
	script.binding = b2
	script.run()
	assert b1.getProperty('x') == 6
	assert b2.getProperty('x') == 8
	assert b1 != b2
	
- 使用了俩个`Binding`对象，但是`Script`对象仍然是一个，所以仍然会有线程安全问题	

**`Binding`实例传递给`GroovyShell`是非线程安全的，它会在所有脚本中共享**,因此在不同的线程中需要分别创建各自的`Script`

	def shell = new GroovyShell()
	
	def b1 = new Binding(x:3)
	def b2 = new Binding(x:4)
	def script1 = shell.parse('x = 2*x')            
	def script2 = shell.parse('x = 2*x')            
	assert script1 != script2
	script1.binding = b1                            
	script2.binding = b2                            
	def t1 = Thread.start { script1.run() }         
	def t2 = Thread.start { script2.run() }         
	[t1,t2]*.join()                                 
	assert b1.getProperty('x') == 6
	assert b2.getProperty('x') == 8
	assert b1 != b2

### 1.2.3 Custom script class

使用`Binding`进行数据共享很方便，但是有时这种方法效率不高，因此`Groovy`允许设置基本脚本类，该基本脚本类必须继承`groovy.lang.Script`,并且是一个SAM类型

示例：

	abstract class MyScript extends Script {
	    String name
	
	    String greet() {
	        "Hello, $name!"
	    }
	}

	import org.codehaus.groovy.control.CompilerConfiguration
	
	def config = new CompilerConfiguration()                                    
	config.scriptBaseClass = 'MyScript'                                         
	
	def shell = new GroovyShell(this.class.classLoader, new Binding(), config) 
	// 往Groovy脚本中添加了一段代码 
	def script = shell.parse('greet()')                                         
	assert script instanceof MyScript
	script.setName('Michel')
	// 执行了Groovy脚本，得到了greet()的输出
	assert script.run() == 'Hello, Michel!'

- 并不限于使用唯一的 `scriptBaseClass `配置。可以使用任意多个编译器配置，甚至自定义编译器


## 1.3 GroovyClassLoader
**`GroovyShell` 是用来执行Groovy脚本的一个简单工具,在其内部使用`groovy.lang.GroovyClassLoader`在运行时编译并加载`Script`类**

- **使用`GroovyClassLoader`可以加载其他的类而不仅仅是`Script`类**

		import groovy.lang.GroovyClassLoader
		
		def gcl = new GroovyClassLoader()                                           
		def clazz = gcl.parseClass('class Foo { void doIt() { println "ok" } }')    
		assert clazz.name == 'Foo'                                                  
		def o = clazz.newInstance()                                                 
		o.doIt()                                                                    

`GroovyClassLoader`维护了所有其所创建的类的引用，所以这很容易造成内存泄露。例如像下面这样去执行同样的Groovy脚本俩次，会得到俩个独立的类

	import groovy.lang.GroovyClassLoader
	
	def gcl = new GroovyClassLoader()
	// 动态创建类
	def clazz1 = gcl.parseClass('class Foo { }') 
	// 动态创建类                               
	def clazz2 = gcl.parseClass('class Foo { }')                                
	assert clazz1.name == 'Foo'                                                 
	assert clazz2.name == 'Foo'
	assert clazz1 != clazz2        

- **之所以会得到俩个独立的类是因为`GroovyClassLoader`并不会保存解析的文本，如果需要多次创建出的实例为同一个，那么必须保证`parseClass()`传入的参数为`File`类型**

		def gcl = new GroovyClassLoader()
		def clazz1 = gcl.parseClass(file)                                           
		def clazz2 = gcl.parseClass(new File(file.absolutePath))                    
		assert clazz1.name == 'Foo'                                                 
		assert clazz2.name == 'Foo'
		assert clazz1 == clazz2      

	- 使用`File`作为输入，`GroovyClassLoader`能够缓存生成的类文件，这避免了在运行时为同一个源创建多个类

## 1.4 GroovyScriptEngine
`groovy.util.GroovyScriptEngine`类通过Groovy脚本的重载和依赖,为应用的灵活扩展提供了基础

- `GroovyShell`关注独立的Groovy脚本, `GroovyClassLoader` 用于动态编译及加载 `Groovy `类，` GroovyScriptEngine` 在 `GroovyClassLoader` 之上建立一层用于处理Groovy脚本的依赖及重载


为了说明上述逻辑，需要创建一个脚本引擎，并在一个死循环中执行代码

1. 在一个文件夹中创建以下脚本

		// ReloadingTest.groovy
		class Greeter {
		    String sayHello() {
		        def greet = "Hello, world!"
		        greet
		    }
		}
		
		new Greeter()

2. 之后，就可以在外部通过`GroovyScriptEngine`执行`ReloadingTest.groovy`中的代码

		def binding = new Binding()
		def engine = new GroovyScriptEngine([tmpDir.toURI().toURL()] as URL[])          
		
		while (true) {
			 // 执行代码会返回一个`Greeter`实例
		    def greeter = engine.run('ReloadingTest.groovy', binding)                   
		    println greeter.sayHello()                                                  
		    Thread.sleep(1000)
		}

	- 代码会不断输出以下内容

			Hello, world!
			Hello, world!
			...

3. 在不中断程序执行的前提下，修改`ReloadingTest`文件的代码

		class Greeter {
		    String sayHello() {
		        def greet = "Hello, Groovy!"
		        greet
		    }
		}
		
		new Greeter()

	- 代码会改变输出的内容

			Hello, world!
			...
			Hello, Groovy!
			Hello, Groovy!
			...

## 1.5 CompilationUnit
通过直接依赖于`org. codehauss .groovy.control. CompilationUnit`，可以在编译期间执行更多的操作。该类负责确定编译的各个步骤，并允许引入新的步骤，甚至在各个阶段停止编译。例如，对于联合编译器，这就是存根生成的方式。

- **但是并不推荐重写`CompilationUnit`，只有在其他办法都无法解决的情况下，再使用这种方法**


# 2. Bean Scripting Framework

`Bean Scripting Framework `用于创建 Java 调用脚本语言的 API。 `BSF` 已经有很长时间没有更新，并且在 `JSR-223` 中已经废弃。


Groovy 中的 `BSF` 引擎通过 `org.codehaus.groovy.bsf.GroovyEngine `实现。事实上， BSF APIs 已经将其隐藏。通过 BSF API 使用Groovy跟使用其他脚本语言没有区别

由于 Groovy 原生支持 `Java` ，因此只需要关心`BSF`如何调用其他语言，例如 : JRuby

## 2.1. Getting started

将 `Groovy `和 `BSF` 的jar包路径加入到`classpath`,就可以在Java代码中调用 Groovy脚本：

	String myScript = "println('Hello World')\n  return [1, 2, 3]";
	BSFManager manager = new BSFManager();
	List answer = (List) manager.eval("groovy", "myScript.groovy", 0, 0, myScript);
	assertEquals(3, answer.size());

## 2.2. Passing in variables
`BSF`允许 Java代码向脚本语言代码传递bean。可以注册/注销bean, 之后可以在 BSF 方法中调用。注册的内容可以直接在脚本中使用。 例如：

	BSFManager manager = new BSFManager();
	manager.declareBean("xyz", 4, Integer.class);
	Object answer = manager.eval("groovy", "test.groovy", 0, 0, "xyz + 1");
	assertEquals(5, answer);

## 2.3. Other calling options

BSF 中有多种方法可以使用，详细可以查看 [BSF 文档 ](http://commons.apache.org/proper/commons-bsf/manual.html)。

这里介绍另一个方法 `apple()`，其允许在脚本语言定义匿名函数并使用传入的参数。对于Groovy来说，可以使用闭包支持这种函数实现:

	BSFManager manager = new BSFManager();
	Vector<String> ignoreParamNames = null;
	Vector<Integer> args = new Vector<Integer>();
	args.add(2);
	args.add(5);
	args.add(1);
	Integer actual = (Integer) manager.apply("groovy", "applyTest", 0, 0,
	        "def summer = { a, b, c -> a * 100 + b * 10 + c }", ignoreParamNames, args);
	assertEquals(251, actual.intValue());

## 2.4. Access to the scripting engine

BSF 中提供勾子，用于直接获取脚本引擎。这个引擎的功能之一就是调用一个对象上的一个方法:

	BSFManager manager = new BSFManager();
	BSFEngine bsfEngine = manager.loadScriptingEngine("groovy");
	manager.declareBean("myvar", "hello", String.class);
	Object myvar = manager.lookupBean("myvar");
	String result = (String) bsfEngine.call(myvar, "reverse", new Object[0]);
	assertEquals("olleh", result);


# 3. JSR 223 javax.script API

JSR-223 是 Java 中调用脚本语言框架的标准接口。从 Java 6 开始，其目标是为了提供一套通用框架来调用脚本语言。 Groovy 提供了丰富的集成机制，建议使用 Groovy 集成机制替代 JSR-223 API

	import javax.script.ScriptEngine;
	import javax.script.ScriptEngineManager;
	import javax.script.ScriptException;
	...
	ScriptEngineManager factory = new ScriptEngineManager();
	ScriptEngine engine = factory.getEngineByName("groovy");

然后就可以通过`engine`对象轻松的调用Groovy代码
	
	Integer sum = (Integer) engine.eval("(1..10).sum()");
	assertEquals(new Integer(55), sum);

共享变量

	engine.put("first", "HELLO");
	engine.put("second", "world");
	String result = (String) engine.eval("first.toLowerCase() + ' ' + second.toUpperCase()");
	assertEquals("hello WORLD", result);

调用方法：

	import javax.script.Invocable;
	...
	ScriptEngineManager factory = new ScriptEngineManager();
	ScriptEngine engine = factory.getEngineByName("groovy");
	String fact = "def factorial(n) { n == 1 ? 1 : n * factorial(n - 1) }";
	engine.eval(fact);
	Invocable inv = (Invocable) engine;
	Object[] params = {5};
	Object result = inv.invokeFunction("factorial", params);
	assertEquals(new Integer(120), result);
	
- `ScriptEngine`默认保留对脚本函数的强引用	