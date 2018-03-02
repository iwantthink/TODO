
# 11 进程和线程

CPU执行代码是顺序执行的，当单核CPU执行多任务时或者 任务数大于CPU的核心数量时，操作系统会轮流让各个任务交替执行，由于CPU执行速度快，故而会感觉所有任务同时执行

**线程是最小的执行单元，而进程由至少一个线程组成。如何调度进程和线程，完全由操作系统决定，程序自己不能决定什么时候执行，执行多长时间。**

**多进程和多线程的程序涉及到同步、数据共享的问题，编写起来更复杂。**

多线程的执行方式和多进程是一样的，也是由操作系统在多个线程之间快速切换，让每个线程都短暂地交替运行，看起来就像同时执行一样。当然，真正地同时执行多线程需要多核CPU才可能实现。

Python同时执行多个任务：

- 多进程模式

- 多线程模式

- 多进程+多线程模式


## 11.1 多进程

`Unix/Linux`操作系统提供了一个`fork()`系统调用。**`fork()`与普通函数调用不一样，其调用一次，返回俩次。因为操作系统自动把当前进程(父进程)复制了一份(子进程)，然后分别在父进程和子进程内返回**

子进程永远返回`0`,而父进程返回子进程的ID。因为一个父进程可以fork出很多个子进程，所以父进程需要记下每个子进程的ID，而子进程只需要调用`getppid()`即可拿到父进程ID

Python的`os`模块封装了常见系统调用，包括`fork`

**Unix/Linux：**

	import os
	
	print('Process (%s) start...' % os.getpid())
	# Only works on Unix/Linux/Mac:
	pid = os.fork()
	if pid == 0:
	    print('I am child process (%s) and my parent is %s.' % (os.getpid(), os.getppid()))
	else:
	    print('I (%s) just created a child process (%s).' % (os.getpid(), pid))

- Windows没有`fork`调用，所以上述代码无法运行。Mac系统是基于`BSD(Unix的一种)`内核，所以Mac可以运行

- `Unix/Linux`可以使用`fork()`实现多进程

**Windows:**

	from multiprocessing import Process
	import os
	
	# 子进程要执行的代码
	def run_proc(name):
	    print('Run child process %s (%s)...' % (name, os.getpid()))
	
	if __name__=='__main__':
	    print('Parent process %s.' % os.getpid())
	    p = Process(target=run_proc, args=('test',))
	    print('Child process will start.')
	    p.start()
	    p.join()
	    print('Child process end.')
	# log输出
	Parent process 928.
	Process will start.
	Run child process test (929)...
	Process end.

- Python是垮平台的，所以也提供了一个跨平台的多进程支持.**`multiprocessing`模块就是跨平台版本的多进程模块**

- `multiprocessing`模块提供了一个`Process`类来代表一个进程对象

- 创建子进程时，只需要传入一个执行函数和函数的参数，`start()`函数用来启动

- `join()`方法可以等待子进程结束后在继续往下运行，通常用于进程间同步


### 11.1.1 进程池

启动大量进程时，可以通过进程池的方式批量创建子进程

	from multiprocessing import Pool
	import os, time, random
	
	def long_time_task(name):
	    print('Run task %s (%s)...' % (name, os.getpid()))
	    start = time.time()
	    time.sleep(random.random() * 3)
	    end = time.time()
	    print('Task %s runs %0.2f seconds.' % (name, (end - start)))
	
	if __name__=='__main__':
	    print('Parent process %s.' % os.getpid())
	    p = Pool(4)
	    for i in range(5):
	        p.apply_async(long_time_task, args=(i,))
	    print('Waiting for all subprocesses done...')
	    p.close()
	    p.join()
	    print('All subprocesses done.')

	# 执行结果
	Parent process 669.
	Waiting for all subprocesses done...
	Run task 0 (671)...
	Run task 1 (672)...
	Run task 2 (673)...
	Run task 3 (674)...
	Task 2 runs 0.14 seconds.
	Run task 4 (673)...
	Task 1 runs 0.27 seconds.
	Task 3 runs 0.86 seconds.
	Task 0 runs 1.41 seconds.
	Task 4 runs 1.91 seconds.
	All subprocesses done.

- `Pool`对象调用`join()`方法会等待所有子进程执行完毕，调用`join()`之前必须先调用`close()`,调用`close()`之后就不能继续添加新的`Process`

### 11.1.2 外部子进程输入和输出

很多时候，子进程并不是自身，而是一个外部进程。当创建了子进程后，还需要控制子进程的输入和输出时，**可以通过`subprocess`模块**

	import subprocess
	
	print('$ nslookup www.python.org')
	r = subprocess.call(['nslookup', 'www.python.org'])
	print('Exit code:', r)
	# log输出
	$ nslookup www.python.org
	Server:        192.168.19.4
	Address:    192.168.19.4#53
	
	Non-authoritative answer:
	www.python.org    canonical name = python.map.fastly.net.
	Name:    python.map.fastly.net
	Address: 199.27.79.223
	
	Exit code: 0

- 代码模拟了如何在Python中运行命令`nslookup www.python.org`,这和直接运行命令效果是一样的


	import subprocess
	
	print('$ nslookup')
	p = subprocess.Popen(['nslookup'], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	output, err = p.communicate(b'set q=mx\npython.org\nexit\n')
	print(output.decode('utf-8'))
	print('Exit code:', p.returncode)

- 子进程需要输入，通过`communicate()`函数

- 上述代码相当于在命令行执行命令`nslookup`,然后手动输入

		set q=mx
		python.org
		exit

### 11.1.3 进程间通讯

Python的`multiprocessing`模块包装了底层的机制，提供了`Queue`,`Pipes`等多种方式来交换数据

	from multiprocessing import Process, Queue
	import os, time, random
	
	# 写数据进程执行的代码:
	def write(q):
	    print('Process to write: %s' % os.getpid())
	    for value in ['A', 'B', 'C']:
	        print('Put %s to queue...' % value)
	        q.put(value)
	        time.sleep(random.random())
	
	# 读数据进程执行的代码:
	def read(q):
	    print('Process to read: %s' % os.getpid())
	    while True:
	        value = q.get(True)
	        print('Get %s from queue.' % value)
	
	if __name__=='__main__':
	    # 父进程创建Queue，并传给各个子进程：
	    q = Queue()
	    pw = Process(target=write, args=(q,))
	    pr = Process(target=read, args=(q,))
	    # 启动子进程pw，写入:
	    pw.start()
	    # 启动子进程pr，读取:
	    pr.start()
	    # 等待pw结束:
	    pw.join()
	    # pr进程里是死循环，无法等待其结束，只能强行终止:
	    pr.terminate()
	
	Process to write: 50563
	Put A to queue...
	Process to read: 50564
	Get A from queue.
	Put B to queue...
	Get B from queue.
	Put C to queue...
	Get C from queue.


Windows没有fork调用，因此，multiprocessing需要“模拟”出fork的效果，父进程所有Python对象都必须通过pickle序列化再传到子进程去，所有，如果multiprocessing在Windows下调用失败了，要先考虑是不是pickle失败了。

## 11.2 多线程

**每个线程都拥有自己的一组CPU寄存器，称为线程的上下文，该上下文反映了线程上次运行该线程的CPU寄存器状态。**指令指针和堆栈指针寄存器是线程上下文中最重要的俩个寄存器。

线程可以分为：

- 内核线程：由操作系统内核创建和撤销
- 用户线程：不需要内核支持而在用户程序中实现的线程


线程是操作系统直接支持的执行单元，Python也内置多线程的支持，并且Python的线程是真正的`Posix Thread`，而不是模拟出来的线程

**Python的标准库提供了俩个模块:**

- `_thread`，已经被废弃，为了兼容性，Python3中还是提供了。

- `threading`。`_thread`是低级模块，`threading`是高级模块，对`_thread`进行了封装。大多数情况下只需要用到`threading`高级模块
	**`threading`除了包含`_thread`模块的所有方法外，还有：**

	`threading.currentThread():` 返回当前的线程变量。
	`threading.enumerate():` 返回一个包含正在运行的线程的list。正在运行指线程启动后、结束前，不包括启动前和终止后的线程。
	`threading.activeCount():` 返回正在运行的线程数量，与len(threading.enumerate())有相同的结果。
	除了使用方法外，线程模块同样提供了Thread类来处理线程，Thread类提供了以下方法:
	
	`run():` 用以表示线程活动的方法。
	`start():`启动线程活动。
	`join([time]):` 等待至线程中止。这阻塞调用线程直至线程的join() 方法被调用中止-正常退出或者抛出未处理的异常-或者是可选的超时发生。
	`isAlive():` 返回线程是否活动的。
	`getName():` 返回线程名。
	`setName():` 设置线程名。

任意进程默认会启动一个线程，该线程被称为主线程，主线程可以启动新的线程。

Python的`threading`模块有一个`current_thread()`函数，永远返回当前线程的实例，主线程实例的名字为`MainThread`,子线程名称可以在创建时指定，如果不指定Python会自动给线程命名

### 11.2.1 线程调用

Python中线程有俩种使用方式：**函数或者用类来包装线程的对象**

- `_thread`模块：调用`_thread.start_new_thread(function,args,[kwargs])`
	- function:执行的函数
	- args:传递给执行函数的参数，必须是tuple
	- kwargs:可选参数

			import _thread
			import time
			
			# 为线程定义一个函数
			def print_time( threadName, delay):
			   count = 0
			   while count < 5:
			      time.sleep(delay)
			      count += 1
			      print ("%s: %s" % ( threadName, time.ctime(time.time()) ))
			
			# 创建两个线程
			try:
			   _thread.start_new_thread( print_time, ("Thread-1", 2, ) )
			   _thread.start_new_thread( print_time, ("Thread-2", 4, ) )
			except:
			   print ("Error: 无法启动线程")

- `threading`模块使用：

	- 函数式：
	
			threading.Thread(target = func)

	- 继承类：

			class mThread(threading.Thread):
				def run(self):
					pass

				

### 11.2.2 Lock

多进程中，同一个变量各自有一份拷贝在各个进程，互不影响。多线程中，所有变量都由所有线程共享，所以一个变量可以被任意线程修改，因此线程之间共享数据最大的危险在于共享数据的同步

多线程共享数据出现问题最大的原因是：**高级语言的一条语句在CPU执行时是若干条语句，即使一个简单的计算**

	balance = balance + n

- 上面的操作分为俩步：

	1. 计算`balance+n`,存入临时变量中
	2. 将临时变量的值赋给`balance`

- 由于执行赋值的操作需要多条语句，而执行这几条语句时，当前线程可能会中断，例如当前线程计算完临时变量，这时其他线程进行赋值，从而导致多个线程把同一个对象的内容改错

		初始值 balance = 0
		
		t1: x1 = balance + 5  # x1 = 0 + 5 = 5
		
		t2: x2 = balance + 8  # x2 = 0 + 8 = 8
		t2: balance = x2      # balance = 8
		
		t1: balance = x1      # balance = 5
		t1: x1 = balance - 5  # x1 = 5 - 5 = 0
		t1: balance = x1      # balance = 0
		
		t2: x2 = balance - 8  # x2 = 0 - 8 = -8
		t2: balance = x2   # balance = -8
		
		结果 balance = -8

**Python提供了`Lock`机制，通过`threading`模块 **

	balance = 0
	lock = threading.Lock()
	
	def run_thread(n):
	    for i in range(100000):
	        # 先要获取锁:
	        lock.acquire()
	        try:
	            # 放心地改吧:
	            change_it(n)
	        finally:
	            # 改完了一定要释放锁:
	            lock.release()

- `lock.acquire()`在多线程执行时，只有一个线程能够获得锁，其他线程只能等待锁，**所以获得锁的线程使用完后一定要释放锁，使用`try...finally...`**

包含锁的代码实际上是单线程模式执行

### 11.2.3 线程优先级队列(Queue)

**Python的`Queue`模块提供了同步的，线程安全的队列类，包括FIFO(先入先出)队列Queue,LIFO(后入先出)队列`LifoQueue`,和优先级队列`PriorityQueue`**

### 11.2.4 多核CPU

Python的线程是真正的线程，但是解释器在执行代码时，有一个`GIL`锁：`Global Interpreter Lock`，任何Python线程执行前，必须先获得`GIL`锁，然后每执行100条字节码，解释器就自动释放`GIL`锁，让别的线程有机会执行。这个`GIL`全局锁实际上把所有线程的执行代码都给上了锁，所以多线程在Python中只能交替执行，即使100个线程泡在100核CPU上，也只能用到1个核


GIL是Python解释器设计的历史遗留问题，通常我们用的解释器是官方实现的CPython，要真正利用多核，除非重写一个不带GIL的解释器。

所以，在Python中，可以使用多线程，但不要指望能有效利用多核。如果一定要通过多线程利用多核，那只能通过C扩展来实现，不过这样就失去了Python简单易用的特点。

**Python虽然不能利用多线程实现多核任务，但可以通过多进程实现多核任务。多个Python进程有各自独立的GIL锁，互不影响。**

## 11.3 ThreadLocal

`ThreadLocal`解决参数在一个线程各个函数之间互相传递的问题

`threading`模块提供了`local()`函数用来创建`ThreadLocal`实例

	import threading
	
	# 创建全局ThreadLocal对象:
	local_school = threading.local()
	
	def process_student():
	    # 获取当前线程关联的student:
	    std = local_school.student
	    print('Hello, %s (in %s)' % (std, threading.current_thread().name))
	
	def process_thread(name):
	    # 绑定ThreadLocal的student:
	    local_school.student = name
	    process_student()
	
	t1 = threading.Thread(target= process_thread, args=('Alice',), name='Thread-A')
	t2 = threading.Thread(target= process_thread, args=('Bob',), name='Thread-B')
	t1.start()
	t2.start()
	t1.join()
	t2.join()

- `ThreadLocal`虽然是一个全局变量，但是每个线程都只能读写自己线程的副本，互不干扰


## 11.4 进程vs线程

实现多任务，通常会涉及`Mater-Worker`模式，Master负责分配任务，Worker负责执行任务，多任务环境下，通常是一个Master,多个Worker

- 用多进程实现Master-Worker，主进程就是Master，其他进程就是Worker。

	多进程的优点是稳定性高，因为一个子进程崩溃了，不会影响主进程和其他子进程。（当然主进程挂了所有进程就全挂了，但是Master进程只负责分配任务，挂掉的概率低）著名的Apache最早就是采用多进程模式

	多进程模式的缺点是创建进程的代价大，在Unix/Linux系统下，用fork调用还行，在Windows下创建进程开销巨大。另外，操作系统能同时运行的进程数也是有限的，在内存和CPU的限制下，如果有几千个进程同时运行，操作系统连调度都会成问题。



- 用多线程实现Master-Worker，主线程就是Master，其他线程就是Worker。

	多线程模式通常比多进程快一点，但是也快不到哪去，而且，多线程模式致命的缺点就是任何一个线程挂掉都可能直接造成整个进程崩溃，因为所有线程共享进程的内存。在Windows上，如果一个线程执行的代码出了问题，你经常可以看到这样的提示：“该程序执行了非法操作，即将关闭”，其实往往是某个线程出了问题，但是操作系统会强制结束整个进程。

	在Windows下，多线程的效率比多进程要高，所以微软的IIS服务器默认采用多线程模式。由于多线程存在稳定性的问题，IIS的稳定性就不如Apache。为了缓解这个问题，IIS和Apache现在又有多进程+多线程的混合模式，真是把问题越搞越复杂。


### 11.4.1 线程切换

无论是多进程还是多线程，只要数量一多，效率肯定上不去。因为切换是需要代价的，如果有几千个任务同时进行，操作系统可能就主要忙着切换任务，而没有时间去执行任务

### 11.4.2 任务类型

是否采用多任务需要考虑任务的类型：**计算密集型和IO密集型**

- **计算密集型任务**的特点是要进行大量的计算，消耗CPU资源，比如计算圆周率、对视频进行高清解码等等，全靠CPU的运算能力。这种计算密集型任务虽然也可以用多任务完成，但是任务越多，花在任务切换的时间就越多，CPU执行任务的效率就越低，所以，要最高效地利用CPU，计算密集型任务同时进行的数量应当等于CPU的核心数。

	计算密集型任务由于主要消耗CPU资源，因此，代码运行效率至关重要。Python这样的脚本语言运行效率很低，完全不适合计算密集型任务。对于计算密集型任务，最好用C语言编写。


- **IO密集型**，涉及到网络、磁盘IO的任务都是IO密集型任务，这类任务的特点是CPU消耗很少，任务的大部分时间都在等待IO操作完成（因为IO的速度远远低于CPU和内存的速度）。对于IO密集型任务，任务越多，CPU效率越高，但也有一个限度。常见的大部分任务都是IO密集型任务，比如Web应用。

	IO密集型任务执行期间，99%的时间都花在IO上，花在CPU上的时间很少，因此，用运行速度极快的C语言替换用Python这样运行速度极低的脚本语言，完全无法提升运行效率。对于IO密集型任务，最合适的语言就是开发效率最高（代码量最少）的语言，脚本语言是首选，C语言最差。

### 11.4.3 异步IO

考虑到CPU和IO之间巨大的速度差异，一个任务在执行的过程中大部分时间都在等待IO操作，单进程单线程模型会导致别的任务无法并行执行，因此才需要多进程模型或者多线程模型来支持多任务并发执行。

现代操作系统对IO操作已经做了巨大的改进，最大的特点就是支持异步IO。如果充分利用操作系统提供的异步IO支持，就可以用单进程单线程模型来执行多任务，这种全新的模型称为事件驱动模型，Nginx就是支持异步IO的Web服务器，它在单核CPU上采用单进程模型就可以高效地支持多任务。在多核CPU上，可以运行多个进程（数量与CPU核心数相同），充分利用多核CPU。由于系统总的进程数量十分有限，因此操作系统调度非常高效。用异步IO编程模型来实现多任务是一个主要的趋势。

**Python语言中，单线程的异步编程被称为协程，有了协程的支持，就可以基于事件驱动编写高效的多任务程序**


## 11.5 分布式进程

在Thread和Process中，优先选择Process,因为Process更稳定，而且可以分布到多台机器上，而THread 只能分布到同一台机器的多个CPU

Python的`multiprocessing`模块不但支持多进程，其中`managers`子模块还支持把多进程分布到多台机器上。一个服务进程可以作为调度者，将任务分布到其他多个进程中，依靠网络通信。由于`managers`模块封装很好，不必了解网络通信的细节，就可以很容易地编写分布式多进程程序。


# 12 正则表达式

正则表达式是一个特殊的字符序列，能够检查一个字符串是否与某种模式匹配

正则表达式中，`\d`可以匹配一个数字,`\D`匹配一个非数字字符,`\w`可以匹配数字或字母，`.`可以匹配任意字符，**如果直接给字符，那就是精确匹配！**

要匹配可变长度的字符，在正则表达式中，用`*`表示任意个字符(包括0个)，用`+`表示至少一个字符，用`?`表示0个或1个字符，用`{n}`表示n个字符，用`{n,m}`表示n-m个字符

`\s`表示匹配空格

**正则匹配默认是贪婪匹配，也就是尽可能多的匹配字符**
	
	>>> re.match(r'^(\d+)(0*)$', '102300').groups()
	('102300', '')

- 由于`\d+`采用贪婪匹配，会把`0`都匹配了，所以`0*`只能匹配空字符串

- 可以通过添加`?`表示 采用非贪婪模式，`\d+?`

## 12.1 正则表达式进阶

可以使用`[]`表示范围，例如：

- `[0-9a-zA-Z\_]`可以匹配一个数字、字母或者下划线；

- `[0-9a-zA-Z\_]+`可以匹配至少由一个数字、字母或者下划线组成的字符串，比如'a100'，'0_Z'，'Py3000'等等；

- `[a-zA-Z\_][0-9a-zA-Z\_]*`可以匹配由字母或下划线开头，后接任意个由一个数字、字母或者下划线组成的字符串，也就是Python合法的变量；

- `[a-zA-Z\_][0-9a-zA-Z\_]{0, 19}`更精确地限制了变量的长度是1-20个字符（前面1个字符+后面最多19个字符）。

`A|B`表示或，可以匹配A或者匹配B

- 例如：`(p|P)ython`可以匹配`Python`或者`python`

`^`表示行的开头

- 例如：`^\d`表示必须以数字开头

`$`表示行的结束，`\d$`表示必须以数字结束

**正则表达式`py`也可以匹配字符串`python`,为了限制这种情况。可以修改为`^py$`整行，就只能匹配`py`**

## 12.2 re模块

Python为正则表达式提供了`re`模块，包含所有正则表达式的功能。**由于Python的字符串本身也用反斜杠`\`进行转义，所以需要注意字符串中的正则表达式的`\`符号需要俩个**

	s = 'ABC\\-001' # Python的字符串
	# 对应的正则表达式字符串变成：
	# 'ABC\-001'

- 建议使用`r''`语法(表示字符串默认不转义)来描述正则表达式

		s = r'ABC\-001' # Python的字符串
		# 对应的正则表达式字符串不变：
		# 'ABC\-001'

**正则表达式对象：**

- `re.RegexObject`:`re.compile()`返回
- `re.MatchObject`:`re.match/re.search`返回，可以通过`group()`返回被RE匹配的字符串

### 12.2.1 re.match和re.search函数

**`re.match(pattern, string, flags=0)`函数尝试从字符串的起始位置匹配一个模式，如果不是起始位置匹配成功的话，返回`None`。若成功则返回一个 匹配的对象**

- `pattern`:匹配的正则表达式
- `string`:进行匹配的字符串
- `flags`:标志位，用于控制正则表达式的匹配方式，如：是否区分大小写，多行匹配等等
 

正则表达式拥有提取子串的功能，用`()`表示的就是要提取的分组（Group）。

- 例如，`^(\d{3})-(\d{3,8})$`分别定义了俩个组。那么可以通过`group(num = 0)/groups()`函数去拿对应的组。

		>>> m = re.match(r'^(\d{3})-(\d{3,8})$', '010-12345')
		>>> m
		<_sre.SRE_Match object; span=(0, 9), match='010-12345'>
		>>> m.group(0)
		'010-12345'
		>>> m.group(1)
		'010'
		>>> m.group(2)
		'12345'

	- `group(0)`永远是原始字符串,从`num=1`开始才是第一个组


**`re.search(pattern,string,flags=0)`扫描整个字符串并返回第一个成功的匹配。匹配成功则返回一个匹配的对象，否则返回`None`**


**`re.match`和`re.search`的区别是**，前者只匹配字符串的开始，如果字符串开始就不符合正则表达式，则失败，返回`None`。而后者匹配整个字符串，直到找到一个匹配

### 12.2.2 检索和替换

Python的`re`模块提供了`re.sub(pattern, repl, string, count=0)`函数用于替换字符串中的匹配项

- `pattern`:正则表达式
- `repl`:替换的字符串，也可以为函数
- `string`:要被查找替换的原始字符串
- `count`:模式匹配后替换的最大次数，默认0表示替换所有的匹配

		#!/usr/bin/python3
		import re
		 
		phone = "2004-959-559 # 这是一个电话号码"
		 
		# 删除注释
		num = re.sub(r'#.*$', "", phone)
		print ("电话号码 : ", num)
		 
		# 移除非数字的内容
		num = re.sub(r'\D', "", phone)
		print ("电话号码 : ", num)

当`re.sub`函数中的`repl`参数为函数时：

	import re
	 
	# 将匹配的数字乘于 2
	def double(matched):
	    value = int(matched.group('value'))
	    return str(value * 2)
	 
	s = 'A23G4HFD567'
	print(re.sub('(?P<value>\d+)', double, s))

### 12.2.3 compile 函数

Python中使用正则表达式时，`re`模块内部会做俩件事：

- 编译正则表达式，如果正则表达式的字符串本身不合法，报错

- 用编译后的正则表达式去匹配字符串

如果一个正则表达式需要重复使用几千次，出于效率的考虑，可以通过`compile`函数预编译正则表达式，编译后生成`Regular Expression`对象

`compile` 函数用于编译正则表达式，生成一个正则表达式`（ Pattern ）`对象，供 `match() `和 `search()` 这两个函数使用。

`re.compile(pattern[, flags])`

- `pattern`:一个字符串形式的正则表达式
- `flags`:可选，表示匹配模式，比如忽略大小写等

	re.I 忽略大小写
	re.L 表示特殊字符集 \w, \W, \b, \B, \s, \S 依赖于当前环境
	re.M 多行模式
	re.S 即为' . '并且包括换行符在内的任意字符（' . '不包括换行符）
	re.U 表示特殊字符集 \w, \W, \b, \B, \d, \D, \s, \S 依赖于 Unicode 字符属性数据库
	re.X 为了增加可读性，忽略空格和' # '后面的注释


	>>>import re
	>>> pattern = re.compile(r'\d+')                    # 用于匹配至少一个数字
	>>> m = pattern.match('one12twothree34four')        # 查找头部，没有匹配
	>>> print m
	None
	>>> m = pattern.match('one12twothree34four', 2, 10) # 从'e'的位置开始匹配，没有匹配
	>>> print m
	None
	>>> m = pattern.match('one12twothree34four', 3, 10) # 从'1'的位置开始匹配，正好匹配
	>>> print m                                         # 返回一个 Match 对象
	<_sre.SRE_Match object at 0x10a42aac0>
	>>> m.group(0)   # 可省略 0
	'12'
	>>> m.start(0)   # 可省略 0
	3
	>>> m.end(0)     # 可省略 0
	5
	>>> m.span(0)    # 可省略 0
	(3, 5)

- `group()`用来获取分组
- `start()`用于获取分组匹配的子串在整个字符串中的起始位置(子串第一个字符的索引)
- `end()`用于获取分组匹配的子串在整个字符串中的结束位置(子串最后一个字符的索引+1)
- `span()`,返回`(start(group), end(group))`


	>>>import re
	>>> pattern = re.compile(r'([a-z]+) ([a-z]+)', re.I)   # re.I 表示忽略大小写
	>>> m = pattern.match('Hello World Wide Web')
	>>> print m                               # 匹配成功，返回一个 Match 对象
	<_sre.SRE_Match object at 0x10bea83e8>
	>>> m.group(0)                            # 返回匹配成功的整个子串
	'Hello World'
	>>> m.span(0)                             # 返回匹配成功的整个子串的索引
	(0, 11)
	>>> m.group(1)                            # 返回第一个分组匹配成功的子串
	'Hello'
	>>> m.span(1)                             # 返回第一个分组匹配成功的子串的索引
	(0, 5)
	>>> m.group(2)                            # 返回第二个分组匹配成功的子串
	'World'
	>>> m.span(2)                             # 返回第二个分组匹配成功的子串
	(6, 11)
	>>> m.groups()                            # 等价于 (m.group(1), m.group(2), ...)
	('Hello', 'World')
	>>> m.group(3)                            # 不存在第三个分组
	Traceback (most recent call last):
	  File "<stdin>", line 1, in <module>
	IndexError: no such group

### 12.2.4 re.findall

`findall(string[, pos[, endpos]])`函数表示在字符串中找到正则表达式锁匹配的所有子串，并返回一个列表，如果没有找到匹配的，则返回空列表

- `string`:待匹配的字符串
- `pos`:可选参数，指定字符串的起始位置，默认为0
- `endpos`:可选参数，指定字符串的结束位置，默认为字符串的长度

**`match/search`是匹配一次(匹配到了就结束)，`findall`是匹配所有**

	import re
	 
	pattern = re.compile(r'\d+')   # 查找数字
	result1 = pattern.findall('runoob 123 google 456')
	result2 = pattern.findall('run88oob123google456', 0, 10)
	 
	print(result1)
	print(result2)
	
	['123', '456']
	['88', '12']

### 12.2.5 re.finditer

与`findall`类似，在字符串中找到正则表达式锁匹配的所有子串，并把他们作为一个迭代器返回

`re.finditer(pattern, string, flags=0)`

- `pattern`:匹配的正则表达式
- `string`:匹配的字符串
- `flags`:标志位


	it = re.finditer(r"\d+","12a32bc43jf3") 
	for match in it: 
	    print (match.group() )

### 12.2.6 re.split

`re.split(pattern, string[, maxsplit=0, flags=0])`函数按照能够匹配的子串将字符串分割后返回列表

- `pattern`:匹配的正则表达式
- `string`:待匹配的字符串
- `maxsplit`:分隔次数,`maxsplit=1`分隔一次，默认为0，不限制次数
- `flags`:标志位

		>>>import re
		>>> re.split('\W+', 'runoob, runoob, runoob.')
		['runoob', 'runoob', 'runoob', '']
		>>> re.split('(\W+)', ' runoob, runoob, runoob.') 
		['', ' ', 'runoob', ', ', 'runoob', ', ', 'runoob', '.', '']
		>>> re.split('\W+', ' runoob, runoob, runoob.', 1) 
		['', 'runoob, runoob, runoob.']
		 
		>>> re.split('a*', 'hello world')   # 对于一个找不到匹配的字符串而言，split 不会对其作出分割
		['hello world']



# 13 常用内建模块

Python之所以自称`“batteries included”`，就是因为内置了许多非常有用的模块，无需额外安装和配置，即可直接使用。

## 13.1 datetime

Python处理日期和时间的标准库

支持日期和时间算法的同时，实现的重点在于更有效的处理和格式化输出

该模块还支持时区处理

	>>> from datetime import datetime
	>>> now = datetime.now() # 获取当前datetime
	>>> print(now)
	2015-05-18 16:28:07.198690
	>>> print(type(now))
	<class 'datetime.datetime'>

- `datetime`是模块，模块中还有一个和模块名称相同的`datetime`类！

- `datetime.now()`是调用类返回当前当前日期和时间,返回值类型为`datetime`

### 13.1.1 datetime和timestamp

在计算机中，时间实际上是用数字表示的。我们把1970年1月1日 00:00:00 UTC+00:00时区的时刻称为`epoch time`，记为0（1970年以前的时间`timestamp`为负数），**当前时间就是相对于`epoch time`的秒数，称为`timestamp`。**

**timestamp 与时区无关，任意时区的timestamp是一样的，只是时间不一样！**
	# UTC时间
	timestamp = 0 = 1970-1-1 00:00:00 UTC+0:00
	# 北京时间
	timestamp = 0 = 1970-1-1 08:00:00 UTC+8:00


`datetime`类拥有一个`timestamp()`函数可以转换成`timestamp`,`timestamp`是一个浮点数，如果有小数位，则小数位表示毫秒数。Java中的timestamp用毫秒数表示

`datetime`类拥有一个`@classmethod`函数`fromtimestamp()`可以将timstamp转换为datetime

- `fromtimestamp()`是转换为当地时间，`utcfromtimestamp()`是转换为UTC标准时区的时间

### 13.1.2 字符型时间和datetime

**str转换为datetime**

`datetime.strptime()`的类方法可以实现

		>>> from datetime import datetime
		>>> cday = datetime.strptime('2015-6-1 18:19:59', '%Y-%m-%d %H:%M:%S')
		>>> print(cday)
		2015-06-01 18:19:59

`strptime()`第二个参数规定了日期和时间部分的格式，转换后的`datetime`是没有时区信息的


**datetime转换为str**

将`datetime`对象格式化为str，可以通过`strftime()`方法实现

	>>> from datetime import datetime
	>>> now = datetime.now()
	>>> print(now.strftime('%a, %b %d %H:%M'))
	Mon, May 05 16:28

### 13.1.3 datetime 加减

`datetime`模块提供了`timedelta`类，支持对日期和时间进行加减(实际上就是把datetime往后或往前计算)得到新的datetime。加减可以直接使用`+/-`这俩个符号，但是需要借助`timedelta`类

	>>> from datetime import datetime, timedelta
	>>> now = datetime.now()
	>>> now
	datetime.datetime(2015, 5, 18, 16, 57, 3, 540997)
	>>> now + timedelta(hours=10)
	datetime.datetime(2015, 5, 19, 2, 57, 3, 540997)
	>>> now - timedelta(days=1)
	datetime.datetime(2015, 5, 17, 16, 57, 3, 540997)
	>>> now + timedelta(days=2, hours=12)
	datetime.datetime(2015, 5, 21, 4, 57, 3, 540997)

### 13.1.4 本地时间转换为UTC时间

本地时间是指系统设定时区的时间(例如北京市`UTC+8:00`时区).UTC时间指`UTC+0:00`时区的时间

`datetime`类有一个时区属性`tzinfo`,默认为`None`，所以无法区分`datetime`是哪个时区

	>>> from datetime import datetime, timedelta, timezone
	>>> tz_utc_8 = timezone(timedelta(hours=8)) # 创建时区UTC+8:00
	>>> now = datetime.now()
	>>> now
	datetime.datetime(2015, 5, 18, 17, 2, 10, 871012)
	>>> dt = now.replace(tzinfo=tz_utc_8) # 强制设置为UTC+8:00
	>>> dt
	datetime.datetime(2015, 5, 18, 17, 2, 10, 871012, tzinfo=datetime.timezone(datetime.timedelta(0, 28800)))

### 13.1.5 时区转换

`datetime`类拥有一个`utcnow()`方法可以拿到当前的UTC时间对象，通过这个对象可以进行转换

	# 拿到UTC时间，并强制设置时区为UTC+0:00:
	>>> utc_dt = datetime.utcnow().replace(tzinfo=timezone.utc)
	>>> print(utc_dt)
	2015-05-18 09:05:12.377316+00:00
	# astimezone()将转换时区为北京时间:
	>>> bj_dt = utc_dt.astimezone(timezone(timedelta(hours=8)))
	>>> print(bj_dt)
	2015-05-18 17:05:12.377316+08:00
	# astimezone()将转换时区为东京时间:
	>>> tokyo_dt = utc_dt.astimezone(timezone(timedelta(hours=9)))
	>>> print(tokyo_dt)
	2015-05-18 18:05:12.377316+09:00
	# astimezone()将bj_dt转换时区为东京时间:
	>>> tokyo_dt2 = bj_dt.astimezone(timezone(timedelta(hours=9)))
	>>> print(tokyo_dt2)
	2015-05-18 18:05:12.377316+09:00

## 13.2 collections

### 13.2.1 namedtuple

	>>> from collections import namedtuple
	>>> Point = namedtuple('Point', ['x', 'y'])
	>>> p = Point(1, 2)
	>>> p.x
	1
	>>> p.y
	2

- `namedtuple`函数，用来创建一个自定义的`tuple`对象，规定了元素的个数，并且可以通过属性而不是索引来引用`tuple`对象的某个元素

- `Point`就是`tuple`的子类

		>>> isinstance(p, Point)
		True
		>>> isinstance(p, tuple)
		True

### 13.2.2 deque

`deque`类，是高效实现了插入和删除操作的双向列表，适合用于队列和栈，一般`list`按索引访问元素很快，但是插入和删除元素很慢

	>>> from collections import deque
	>>> q = deque(['a', 'b', 'c'])
	>>> q.append('x')
	>>> q.appendleft('y')
	>>> q
	deque(['y', 'a', 'b', 'c', 'x'])

- `deque`除了实现list的append()和pop()外，还支持`appendleft()`和`popleft()`，这样就可以非常高效地往头部添加或删除元素

### 13.2.3 defaultdict

`defaultdict`类，默认的`dict`在引用的key不存在时 会抛出`KeyError`。`defaultdict`扩展了`dict`，使得在key不存在时，返回一个默认值，这个默认值通过创建时传入的参数决定

	>>> from collections import defaultdict
	>>> dd = defaultdict(lambda: 'N/A')
	>>> dd['key1'] = 'abc'
	>>> dd['key1'] # key1存在
	'abc'
	>>> dd['key2'] # key2不存在，返回默认值
	'N/A'

- 除了key不存在时返回默认值，其他行为和`dict`都一样

- 默认值是调用函数返回的，函数在创建时传入

### 13.2.4 OrdereDict

`OrdereDict`类,默认的`dict`的key是乱序的，是根据hash排序的,`OrdereDict`保持了key的顺序（**会按照插入的顺序排列**）

	>>> from collections import OrderedDict
	>>> d = dict([('a', 1), ('b', 2), ('c', 3)])
	>>> d # dict的Key是无序的
	{'a': 1, 'c': 3, 'b': 2}
	>>> od = OrderedDict([('a', 1), ('b', 2), ('c', 3)])
	>>> od # OrderedDict的Key是有序的
	OrderedDict([('a', 1), ('b', 2), ('c', 3)])

`OrdereDict`可以实现了一个FIFO(先进先出)的`dict`，当容量超出限制时，会先删除最早添加的key

	from collections import OrderedDict
	
	class LastUpdatedOrderedDict(OrderedDict):
	
	    def __init__(self, capacity):
	        super(LastUpdatedOrderedDict, self).__init__()
	        self._capacity = capacity
	
	    def __setitem__(self, key, value):
	        containsKey = 1 if key in self else 0
	        if len(self) - containsKey >= self._capacity:
	            last = self.popitem(last=False)
	            print('remove:', last)
	        if containsKey:
	            del self[key]
	            print('set:', (key, value))
	        else:
	            print('add:', (key, value))
	        OrderedDict.__setitem__(self, key, value)

### 13.2.5 Counter

`Counter`是一个计数器，例如，统计字符出现的次数.实际上是`dict`的子类，对dict进行了一些改造，默认`Counter`对不存在的key 也不会抛出`KeyError`,会返回一个默认值 0

	>>> from collections import Counter
	>>> c = Counter()
	>>> for ch in 'programming':
	...     c[ch] = c[ch] + 1
	...
	>>> c
	Counter({'g': 2, 'm': 2, 'r': 2, 'a': 1, 'i': 1, 'o': 1, 'n': 1, 'p': 1})
	
## 13.3 base64

Base64是一种用64个字符来表示任意二进制数据的方法

- 用记事本打开exe、jpg、pdf这些文件时，我们都会看到一大堆乱码，因为二进制文件包含很多无法显示和打印的字符，所以，如果要让记事本这样的文本处理软件能处理二进制数据，就需要一个二进制到字符串的转换方法。Base64是一种最常见的二进制编码方法。

**Base64原理：**

1. 准备一个包含64个字符的数组

		['A', 'B', 'C', ... 'a', 'b', 'c', ... '0', '1', ... '+', '/']

2. 对二进制数据进行处理，每三个字节一组，一共是`3*8=24`bit,分为4组，每组对应6个bit。

	![](https://cdn.liaoxuefeng.com/cdn/files/attachments/001399415038305edba53df7d784a7fa76c6b7f6526873b000)

3. 得到4个数字作为索引，然后查表，获得相应的4个字符，就是编码后的字符串

- Base64编码会把三字节的二进制数据编码为四字节的文本数据，长度增加33%，好处是编码后的文本数据可以在邮件正文，网页等直接显示

- 末尾不足三个字节的情况下，Base64会用`\x00`字节在末尾补足，然后还会在编码的末尾添加1个或2个`=`号(`=`号表示补足了多少字节，解码时候会去除)


### 13.3.1 base64模块

Python内置`base64`模块，提供了`b64encode()`和`b64decode()`函数进行base64转换，**俩个函数所需参数都是二进制数据！**

	>>> import base64
	>>> base64.b64encode(b'binary\x00string')
	b'YmluYXJ5AHN0cmluZw=='
	>>> base64.b64decode(b'YmluYXJ5AHN0cmluZw==')
	b'binary\x00string'

由于标准的Base64编码后可能出现字符`+`和`/`,在URL中就不能直接作为参数，所有`base64`模块提供了一种`url safe`的base64编码(其实就是把编码后的b64字符串中的`+`和`/`变成`-`和`_`)。`base64`模块提供了`urlsafe_b64encode()`和`urlsafe_b64decode()`俩个函数

	>>> base64.b64encode(b'i\xb7\x1d\xfb\xef\xff')
	b'abcd++//'
	>>> base64.urlsafe_b64encode(b'i\xb7\x1d\xfb\xef\xff')
	b'abcd--__'
	>>> base64.urlsafe_b64decode('abcd--__')
	b'i\xb7\x1d\xfb\xef\xff'

可以通过自定义64个字符的排列顺序来自定义Base64编码

Base64是一种通过查表的编码方法，不能用于加密，即使使用自定义的编码表也不行。Base64适用于小段内容的编码，比如数字证书签名，Cookie的内容等

由于`=`字符在URL,Cookie中会造成歧义，所以很多Base64编码会去除`=`字符。**这时，需要记住一点，Base64是将3个字节变成4个字节来处理，所以Base64编码的长度永远是4的倍数，因此在反编码的时候需要判断编码长度，然后在末尾添加`=`字符**


## 13.4 struct

Python提供了`struct`模块用来解决`bytes`和其他二进制数据类型的转换

- Python中没有专门处理字节的数据类型，`b'str'`使用二进制str表示字节，所以Python中 ` 字节数组 = 二进制str`

在Python中，如果要把一个32位无符号整数变成字节，也就是4个长度的`bytes`,需要配合运算符这么写：

	>>> n = 10240099
	>>> b1 = (n & 0xff000000) >> 24
	>>> b2 = (n & 0xff0000) >> 16
	>>> b3 = (n & 0xff00) >> 8
	>>> b4 = n & 0xff
	>>> bs = bytes([b1, b2, b3, b4])
	>>> bs
	b'\x00\x9c@c'

- 非常麻烦，并如果是浮点数的话就不能用这种方式进行转换

**`struct`模块提供了`pack`函数和`unpack`函数用来在任意数据类型和`bytes`之间进行转换**

	>>> import struct
	>>> struct.pack('>I', 10240099)
	b'\x00\x9c@c'

- 第一个参数是处理指令，`>I`字符 前者`'>'`表示字节顺序为`big-endian`.后者`'I'`表示4字节无符号整数


	>>> struct.unpack('>IH', b'\xf0\xf0\xf0\xf0\x80\x80')
	(4042322160, 32896)

- 根据`>IH`的说明，后面的bytes依次变为I（4字节无符号整数）和H（2字节无符号整数）。

`struct`模块定义的数据类型可以参考:[官方文档链接](https://docs.python.org/3/library/struct.html#format-characters)


## 13.5 hashlib

摘要算法又称哈希算法、散列算法。它通过一个函数，把任意长度的数据转换为一个长度固定的数据串（通常用16进制的字符串表示）。摘要算法就是通过摘要函数对任意长度的数据`data`计算出固定长度的摘要`digest`,目的就是为了发现原始数据是否被人篡改(摘要函数是一个单向函数，计算`f(data)`很容易，但是反推很困难)

Python的`hashlib`模块，提供了常见的摘要算法,例如MD5,SHA1等


	import hashlib
	
	md5 = hashlib.md5()
	md5.update('how to use md5 in '.encode('utf-8'))
	md5.update('python hashlib?'.encode('utf-8'))
	print(md5.hexdigest())

- 如果需要计算的字符串很长，可以分多条多次调用`update()`,最后的计算结果跟一次性调用是一样的

- MD5是最常见的摘要算法，速度很快，生成结果是固定的128位字节，通常用一个32位的16进制字符串表示


	import hashlib
	
	sha1 = hashlib.sha1()
	sha1.update('how to use sha1 in '.encode('utf-8'))
	sha1.update('python hashlib?'.encode('utf-8'))
	print(sha1.hexdigest())

- SHA1的结果是160位字节，通常用一个40位的16进制字符串表示

- 比SHA1更安全的算法是SHA256和SHA512，不过越安全的算法越慢，而且摘要长度更长

**不同数据可能通过某个摘要算法得到相同的结果，因为任何摘要算法都是把无限多的数据集合映射到一个有限的集合中。这种情况称为碰撞。**

摘要算法可以应用在账号密码的存储。常用的MD5口令很容易被反推，可以通过加盐来增加安全性。



## 13.6 hmac

Hmac算法：`Keyed-Hashing for Message Authentication`,它通过一个标准算法，在计算Hash的过程中，把`key`(也就是盐)混入计算过程中。采用Hmac替代自己的md5+salt算法，更标准化也更安全。**Hmac算法针对所有哈希算法都通用**


	>>> import hmac
	>>> message = b'Hello, world!'
	>>> key = b'secret'
	>>> h = hmac.new(key, message, digestmod='MD5')
	>>> # 如果消息很长，可以多次调用h.update(msg)
	>>> h.hexdigest()
	'fa4ee7d173f2d97ee79022d1a7355bcf'

- 需要注意 传入的key和message都是`bytes`类，所以需要先进行编码

## 13.7 itertools

Python的内建模块`itertools`提供了用于操作迭代对象的函数

`count(start=0,step=1)`函数创建一个无限的迭代器

	>>> import itertools
	>>> natuals = itertools.count(1)
	>>> for n in natuals:
	...     print(n)

`cycle(iterable)`会把传入的一个序列无限重复下去

	>>> import itertools
	>>> cs = itertools.cycle('ABC') # 注意字符串也是序列的一种
	>>> for c in cs:
	...     print(c)
	'A'  'B'  'C'  'A'....


`repeat(object[, times])`会把传入的元素无限重复下去，第二个参数可以限定重复次数

	>>> ns = itertools.repeat('A', 3)
	>>> for n in ns:
	...     print(n)
	...
	A
	A
	A

`takewhile(predicate,iterable)`可以对无限序列添加条件，截取出一个有限的序列

	>>> natuals = itertools.count(1)
	>>> ns = itertools.takewhile(lambda x: x <= 10, natuals)
	>>> list(ns)
	[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

`chain(*iterables)`可以把一组迭代对象串联起来，形成一个更大的迭代器

	>>> for c in itertools.chain('ABC', 'XYZ'):
	...     print(c)
	# 迭代效果：'A' 'B' 'C' 'X' 'Y' 'Z'

`groupby(iterable, key=None)`把迭代器中相领的重复的元素挑出来放到一块

	>>> for key, group in itertools.groupby('AAABBBCCAAA'):
	...     print(key, list(group))
	...
	A ['A', 'A', 'A']
	B ['B', 'B', 'B']
	C ['C', 'C']
	A ['A', 'A', 'A']


**`itertools`模块提供的全部是处理迭代功能的函数，返回的都是`Iterator`**

## 13.7 contextlib
[with语句和上下文管理器](http://www.cnblogs.com/nnnkkk/p/4309275.html)

### 13.7.1 with语句

在Python中任何正确实现了上下文管理，就可以被用于`with`语句
	
`with`语句可以使用`try...finally..`的功能

	f = open("test.txt")
	try:
	    for line in f.readlines():
	        print(line)
	finally:
	    f.close()
	# 上下俩者相同
	with open("text.txt") as f:
	    for line in f.readlines()
	　　　　print(line)


**`with`语句的基本语法结构：**

	with expression [as variable]:
	　　　 with-block

- `expression` 是上下文管理器，其含有`__enter__()`和`__exit__()`函数

- `[as variable]` ，会返回上下文管理器expression调用`__enter__`函数返回的对象。

- `with-block` 是执行语句，执行完毕，就进行资源清理，其实就是调用`__exit__()`函数

**`with`语句不仅可以管理文件，还可以管理锁，连接等等**

	#管理锁
	import  threading
	lock = threading.lock()
	with lock:
	    #执行一些操作
	    pass

### 13.7.2 上下文管理器

**`with`语句主要依赖于 上下文管理器，所谓上下文管理器就是实现了上下文协议的类，这个上下文协议就是指一个类 实现`__enter__()`和`__exit__()`方法**


- `__enter__(self):` 执行一些环境准备工作，同时返回一资源对象

- `__exit__(self,type,value,traceback):` 参数分别是 异常类型，异常信息和堆栈(如果执行体语句没有引发异常，这三个参数都是None)。返回值`True/False`表示异常有没有被处理，如果返回False，引发的异常将会被传递出上下文。`__exit__()`函数内部引发的异常会覆盖执行体中的异常


### 13.7.3 contextlib模块

自定义上下文管理器，除了通过实现`__enter__()`和`__exit__()`方法之外，还可以通过`contextlib`模块中包含的装饰器`@contextmanager`和一些辅助函数来实现

装饰器`@contextmanager`只需要写一个生成器函数就可以代替自定义的上下文管理器

  	  @contextmanager
        def some_generator(<arguments>):
            <setup>
            try:
                yield <value>
            finally:
                <cleanup>

`with`语句用法如下：

	with some_generator(<arguments>) as <variable>:
	            <body>

- `some_generator`函数在在yield之前的代码等同于上下文管理器中的`__enter__`函数。

- `yield`的返回值等同于`__enter__`函数的返回值，即如果with语句声明了`as <variable>`，则`yield`的值会赋给`variable`

- 然后执行`<cleanup>`代码块，等同于上下文管理器的`__exit__`函数。此时发生的任何异常都会再次通过yield函数返回。


**`contextlib`模块提供`nested()`函数用来嵌套处理多个上下文管理器，但是`with`语句本身已经支持多个上下文管理器的使用，所以意义不大**

	  @contextmanager
	  def my_context(name):
	      print("enter")
	      try:
	          yield name
	     finally:
	         print("exit")
	 
	 #使用nested函数来调用多个管理器
	 print("---------使用nested函数调用多个管理器-----------")
	 with nested(my_context("管理器一"), my_context("管理器二"),my_context("管理器三")) as (m1,m2,m3):
	     print(m1)
	     print(m2)
	     print(m3)
	 
	 #直接使用with来调用调用多个管理器
	 print("---------使用with调用多个管理器-----------")
	 with my_context("管理器一") as m1, my_context("管理器二") as m2, my_context("管理器三") as m3:
	     print(m1)
	     print(m2)
	     print(m3)

**`contextlib`模块提供`closing()`函数帮助具有`close()`方法的资源对象生成上下文管理器**

	import urllib, sys
	from contextlib import closing
	
	with closing(urllib.urlopen('http://www.yahoo.com')) as f:
	    for line in f:
	        sys.stdout.write(line)

## 13.8 urllib

Python的`urllib`包下有`request,response`等模块，提供了一系列操作URL的功能

**Get：**

`urlib`包下的`reuquest`模块可以抓取URL内容，返回HTTP响应

	from urllib import request
	
	with request.urlopen('https://api.douban.com/v2/book/2129650') as f:
	    data = f.read()
	    print('Status:', f.status, f.reason)
	    for k, v in f.getheaders():
	        print('%s: %s' % (k, v))
	    print('Data:', data.decode('utf-8'))
	# 输出Log
	Status: 200 OK
	Server: nginx
	Date: Tue, 26 May 2015 10:02:27 GMT
	Content-Type: application/json; charset=utf-8
	Content-Length: 2049
	Connection: close
	Expires: Sun, 1 Jan 2006 01:00:00 GMT
	Pragma: no-cache
	Cache-Control: must-revalidate, no-cache, private
	X-DAE-Node: pidl1
	Data: {"rating":{"max":10,"numRaters":16,"average":"7.4","min":0},"subtitle":"","author":["廖雪峰编著"],"pubdate":"2007-6",...}

- 返回的是字节，所以需要进行反编码

`request`模块里的`Request`类，可以通过这个类添加 请求头

	req = request.Request('http://www.douban.com/')
	req.add_header('User-Agent', 'Mozilla/6.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/8.0 Mobile/10A5376e Safari/8536.25')
	with request.urlopen(req) as f:
	    print('Status:', f.status, f.reason)
	    for k, v in f.getheaders():
	        print('%s: %s' % (k, v))
	    print('Data:', f.read().decode('utf-8'))

**POST:**

要以POST发送一个请求，只需要在调用`urlopen()`函数时，传入`data = xxx`参数即可(参数是字节)

	with request.urlopen(req, data='post body'.encode('utf-8')) as f:
	    print('Status:', f.status, f.reason)
	    for k, v in f.getheaders():
	        print('%s: %s' % (k, v))
	    print('Data:', f.read().decode('utf-8'))


**Handler:**

`request`模块里的`ProxyHandler`类可以实现通过Proxy访问

	proxy_handler = urllib.request.ProxyHandler({'http': 'http://www.example.com:3128/'})
	proxy_auth_handler = urllib.request.ProxyBasicAuthHandler()
	proxy_auth_handler.add_password('realm', 'host', 'username', 'password')
	opener = urllib.request.build_opener(proxy_handler, proxy_auth_handler)
	with opener.open('http://www.example.com/login.html') as f:
	    pass


## 13.9 XML

操作XML有俩种方式：DOM和SAX。 DOM 会将整个XML读入内存，解析为树，因此占用内存大，解析慢，优点是可以任意遍历树的节点。SAX是流模式，边读边解析，占用内存小，解析快，缺点是需要自己处理事件


Python中使用SAX解析XML，只需要`start_element`,`end_element`,`char_data`三个函数即可开始解析xml

例如：当SAX解析器读到一个节点`<a href="/">python</a>`会产生三个事件：

1. `start_element`事件，在读取`<a href="/">`时；

2. `char_data`事件，在读取`python`时；

3. `end_element`事件，在读取`</a>`时。


	from xml.parsers.expat import ParserCreate
	
	class DefaultSaxHandler(object):
	    def start_element(self, name, attrs):
	        print('sax:start_element: %s, attrs: %s' % (name, str(attrs)))
	
	    def end_element(self, name):
	        print('sax:end_element: %s' % name)
	
	    def char_data(self, text):
	        print('sax:char_data: %s' % text)
	
	xml = r'''<?xml version="1.0"?>
	<ol>
	    <li><a href="/python">Python</a></li>
	    <li><a href="/ruby">Ruby</a></li>
	</ol>
	'''
	
	handler = DefaultSaxHandler()
	parser = ParserCreate()
	parser.StartElementHandler = handler.start_element
	parser.EndElementHandler = handler.end_element
	parser.CharacterDataHandler = handler.char_data
	parser.Parse(xml)

- 需要注意的是读取一大段字符串时，CharacterDataHandler可能被多次调用，所以需要自己保存起来，在EndElementHandler里面再合并。

生成XML：

	L = []
	L.append(r'<?xml version="1.0"?>')
	L.append(r'<root>')
	L.append(encode('some & data'))
	L.append(r'</root>')
	return ''.join(L)

## 13.10 HMTLParser

Python提供了 HTMLParser来解析HTML，由于HTML本质是XML的子集，但是其语法没有XML那么严格，所以不能用标准的DOM或SAX来解析HTML

	from html.parser import HTMLParser
	from html.entities import name2codepoint
	
	class MyHTMLParser(HTMLParser):
	
	    def handle_starttag(self, tag, attrs):
	        print('<%s>' % tag)
	
	    def handle_endtag(self, tag):
	        print('</%s>' % tag)
	
	    def handle_startendtag(self, tag, attrs):
	        print('<%s/>' % tag)
	
	    def handle_data(self, data):
	        print(data)
	
	    def handle_comment(self, data):
	        print('<!--', data, '-->')
	
	    def handle_entityref(self, name):
	        print('&%s;' % name)
	
	    def handle_charref(self, name):
	        print('&#%s;' % name)
	
	parser = MyHTMLParser()
	parser.feed('''<html>
	<head></head>
	<body>
	<!-- test html parser -->
	    <p>Some <a href=\"#\">html</a> HTML&nbsp;tutorial...<br>END</p>
	</body></html>''')

# 14 常用第三方模块

所有的第三方模块基本都会在[PyPI](https://pypi.python.org/pypi)上注册，只要找到对应模块名称即可用pip安装

## 14.1 Pillow

PIL:Python Imaging Library ,PIL仅支持到2.7，现在的`Pillow`是其兼容版本，支持最新的3.x 且加入很多新特性

安装Pillow可以通过pip `pip install pillow`,或者通过Anaconda

更多具体操作查看[官网API文档](https://pillow.readthedocs.io/en/latest/)

## 14.2 requests

`requests`模块是比Python内置的`urllib`模块更加高级的模块

安装`requests`:`pip install requests`

	>>> r = requests.get('https://api.github.com/user', auth=('user', 'pass'))
	>>> r.status_code
	200
	>>> r.headers['content-type']
	'application/json; charset=utf8'
	>>> r.encoding
	'utf-8'
	>>> r.text
	u'{"type":"User"...'
	>>> r.json()
	{u'private_gists': 419, u'total_private_repos': 77, ...}

- 对于带参数的URL,可以传入一个`dict`作为`params`参数

- 无论响应内容是文本还是二进制内容，都可以使用`content`属性来获得`bytes`对象

- requests获取HTTP响应头信息只用通过`headers`属性即可返回一个包含所有响应头信息的dict

- **`requests`模块对于特定类型的响应，如JSON 可以直接获取 只用调用`json()`函数即可**

- 当需要传入Http Header时，可以在构造函数中将一个dict传入`headers`参数

- 当需要`POST`请求时，只需要将`get()`函数改成`post()`函数，然后传入`data`参数作为Post请求的数据

		>>> r = requests.post('https://accounts.douban.com/login', data={'form_email': 'abc@example.com', 'form_password': '123456'})

	- requesets默认使用`application/x-www-form-urlencoded`对POST数据编码。

	- 如果POST的数据是JSON，可以直接使用`json`参数

			params = {'key': 'value'}
			r = requests.post(url, json=params) # 内部自动序列化为JSON

上传文件时需要更复杂的编码格式，但是通过requests简化成`files`参数

	>>> upload_files = {'file': open('report.xls', 'rb')}
	>>> r = requests.post(url, files=upload_files)

- 读取文件时，必须使用`rb`即二进制模式读取，这样获取的`bytes`长度才是文件的长度

将`post()`方法替换成`put()`,`delete()`等，就可以以不同的请求方式请求资源

### 14.2.1 Cookie

requests对Cookie 做了特殊处理，使得不必解析Cookie即可获取指定Cookie

	>>> r.cookies['ts']
	'example_cookie_12345'

要在请求中传入Cookie，只需要传入`cookies`参数

	>>> cs = {'token': '12345', 'status': 'working')
	>>> r = requests.get(url, cookies=cs)

### 14.2.2 超时

指定超时时间，使用`timeout`参数即可，单位是秒

	>>> r = requests.get(url, timeout=2.5) # 2.5秒后超时


## 14.3 chardet

`chardet`模块提供了检测编码的功能，在Python中提供了Unicode表示的`str`和`bytes`俩种类型，并且可以通过`encode()`和`decode()`进行转换，但是这无法在不知道编码的情况下进行
	
	>>> chardet.detect(b'Hello, world!')
	{'encoding': 'ascii', 'confidence': 1.0, 'language': ''}
	
[官方文档](https://chardet.readthedocs.io/en/latest/supported-encodings.html)

## 14.4 psutil

`psutil = process and system utilities`模块可以获取系统信息，实现系统监控，而且是跨平台的。

[官方文档](https://github.com/giampaolo/psutil)

获取CPU信息

	>>> import psutil
	>>> psutil.cpu_count() # CPU逻辑数量
	4
	>>> psutil.cpu_count(logical=False) # CPU物理核心
	2
	# 2说明是双核超线程, 4则是4核非超线程

# 15 virtualenv

在开发Python应用程序的时候，系统安装的Python3只有一个版本：3.4。所有第三方的包都会被pip安装到Python3的site-packages目录下。

如果我们要同时开发多个应用程序，那这些应用程序都会共用一个Python，就是安装在系统的Python 3。如果应用A需要jinja 2.7，而应用B需要jinja 2.6怎么办？

- virtualenv为每个应用创建一套`隔离`的Python运行环境，解决了不同应用间多版本冲突的问题。


[参考文档](https://www.cnblogs.com/chaosimple/p/4475958.html)