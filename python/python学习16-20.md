# 16 图形界面

Python支持多种图形界面的第三方库：TK,wxWidgets,Qt,GTK

Python自带的库支持TK的Tkinter,无需安装包

我们编写的Python代码会调用内置的Tkinter，Tkinter封装了访问Tk的接口；

Tk是一个图形库，支持多个操作系统，使用Tcl语言开发；

Tk会调用操作系统提供的本地GUI接口，完成最终的GUI。

所以，我们的代码只需要调用Tkinter提供的接口就可以了。

Python内置的Tkinter可以满足基本的GUI程序的要求，如果是非常复杂的GUI程序，建议用操作系统原生支持的语言和库来编写。



# 17 网络编程

网络通信就是不同计算机上的俩个不同进程之间在通信

## 17.1 TCP/IP

计算机为了联网，就必须规定通信协议，这一系列协议统称为**互联网协议簇(Internet Protocaol Suite)**，也就是全球通用的协议标准。[参考链接-互联网协议簇](http://www.ruanyifeng.com/blog/2012/05/internet_protocol_suite_part_i.html)

互联网协议包含上百种协议标准，但是最重要的俩个协议是**TCP协议和IP协议**，所以通常将互联网的协议简称为**TCP/IP协议**

- IP协议：

   互联网上每个计算机的唯一标识就是IP地址，类似`123.123.123.123`.如果一台计算机同时接入到多个网络，比如路由器，他就会有多个IP地址，所以IP地址对应的实际上是计算机的网络接口，通常是网卡
   
   IP协议实际上是一个32位整数(称为IPV4)，以字符串表示的IP地址实际上是把32位整数分成四份(每八位一份)然后按分组后的数字表示，目的是便于阅读。IPv6地址实际上就是一个128位整数，是IPv4的升级版，字符串表示类似于`2001:0db8:85a3:0042:1000:8a2e:0370:7334`
   
   IP协议负责把数据从一台计算机通过网络发送到另一台计算机。数据被分割成一小块一小块，然后通过IP包发送出去。由于互联网链路复杂，两台计算机之间经常有多条线路，因此，路由器就负责决定如何把一个IP包转发出去。IP包的特点是按块发送，途径多个路由，但不保证能到达，也不保证顺序到达。

- TCP协议：

	建立在**IP协议**之上，负责在俩台计算机之间建立可靠连接，保证数据包按顺序到达。TCP协议会通过握手建立连接，然后对每个IP包编号，确保对方按顺序收到，如果包丢掉了，就会自动重发

	许多常用的更高级的协议都是建立在TCP/IP协议基础上的，例如浏览器的HTTP协议，发送邮件的SMTP协议等

	一个TCP报文除了包含要传输的数据，还包含源IP地址和目标IP地址，源端口和目标端口

	- 端口：
	
		俩台计算机通信时，只发IP地址是不够的，因为同一台计算机上可以跑多个网络程序。一个TCP报文来了之后需要通过端口来区分到底发给 QQ还是发给 浏览器

		**一个进程可能同时与多个计算机建立连接，因此它会申请多个端口**

## 17.2 TCP编程

Socket是网络编程的一个抽象概念，通常用一个Socket表示**打开了一个网络链接**，而打开链接需要知道目标计算机的IP地址 和端口号，再指定协议类型即可

创建TCP连接时，主动发起连接的叫客户端，被动响应连接的叫服务器

### 17.2.1 客户端

- 创建一个基于TCP的Socket

		# 导入socket库:
		import socket
		
		# 创建一个socket:
		s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		# 建立连接:
		s.connect(('www.sina.com.cn', 80))
		# 发送数据:
		s.send(b'GET / HTTP/1.1\r\nHost: www.sina.com.cn\r\nConnection: close\r\n\r\n')

	- `AF_INET`是指定IPv4协议，`AF_INET6`即指定ipv6
	- `SOCK_STREAM`指定使用面向流的TCP协议
	
	- 客户端要主动发起TCP连接，必须知道服务器的IP地址和端口号。新浪网站的IP地址可以用域名www.sina.com.cn自动转换到IP地址，但是怎么知道新浪服务器的端口号呢？

		- 答案是作为服务器，提供什么样的服务，端口号就必须固定下来。由于我们想要访问网页，因此新浪提供网页服务的服务器必须把端口号固定在80端口，因为80端口是Web服务的标准端口。其他服务都有对应的标准端口号，例如SMTP服务是25端口，FTP服务是21端口，等等。端口号小于1024的是Internet标准服务的端口，端口号大于1024的，可以任意使用。
	
	- connect参数是一个tuple,包含端口号和地址

	- TCP连接创建的是双向通道，双方都可以同时给对方发数据。但是具体的谁先发，怎么协调，需要根据具体的协议来决定。例如HTTP协议规定客户端必须先发请求给服务器，服务器收到后才发数据给客户端

	- 发送的格式必须符合HTTP标准
	

- 接收返回数据

		# 接收数据:
		buffer = []
		while True:
		    # 每次最多接收1k字节:
		    d = s.recv(1024)
		    if d:
		        buffer.append(d)
		    else:
		        break
		data = b''.join(buffer)
		# 关闭连接:
		s.close()

	- 通过`recv(max)`方法接收数据，max指定一次性最多接收多少字节数，因此需要在while循环中反复接收，直到`recv()`返回空数据，表示接收完毕

	- 接收完毕之后 需要调用`close()`关闭Socket

- 处理返回数据

		header, html = data.split(b'\r\n\r\n', 1)
		print(header.decode('utf-8'))
		# 把接收的数据写入文件:
		with open('sina.html', 'wb') as f:
		    f.write(html)

	- 接收到的数据包括HTTP头和网页本身，只需要把HTTP头和网页分离

### 17.2.2 服务器

服务器进程需要绑定一个端口并监听来自其他客户端的连接。如果某个客户端连接过来了，服务器就与该客户端建立Socket连接，随后的通信就靠这个Socket连接了

服务器通常会打开固定端口(例如80)进行监听，每来一个客户端连接，就创建该Socket连接。

- 由于服务器会有大量来自客户端的连接，所以服务器需要区分哪一个Socket连接和哪个客户端绑定。**一个Socket依赖四项内容确定一个socket：服务器地址，服务器端口，客户端地址，客户端端口**

- 另外由于服务器需要同时响应多个客户端的请求，所以每个连接都需要一个新的进程或者线程来处理，否则服务器一次只能服务一个客户端

## 17.2.3 sample
服务器功能：接收客户端连接，把客户端发过来的字符串加工再返回

1. 创建一个基于IPV4和TCP协议的Socket

		s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

2. 然后需要绑定监听的地址和端口。由于服务器可能有多个网卡，可以绑定到某一块网卡的IP地址上，也可以用`0.0.0.0`绑定到所有的网络地址，还可以用`127.0.0.1`绑定到本机地址,`127.0.0.1`是一个特殊的IP地址，表示本机地址，如果绑定到这个地址，客户端必须同时在本机运行才行，也就是说外部计算机无法连接进来

	端口号需要预先指定。因为我们写的这个服务不是标准服务，所以用9999这个端口号。**请注意，小于1024的端口号必须要有管理员权限才能绑定：**

		# 监听端口:
		s.bind(('127.0.0.1', 9999))

3. 开始监听端口，通过`listen()`，传入的参数指定等待连接的最大数量

		s.listen(5)
		print('Waiting for connection...')

4. 服务器程序需要通过一个永真循环来接收来自客户端的连接，`accept()`会阻塞并返回一个客户端的连接

	每个连接都必须创建新线程(或进程)来处理，因为单线程的服务器程序一次只能处理一个客户端连接

		while True:
		    # 接受一个新连接:
		    sock, addr = s.accept()
		    # 创建新线程来处理TCP连接:
		    t = threading.Thread(target=tcplink, args=(sock, addr))
		    t.start()

		def tcplink(sock, addr):
		    print('Accept new connection from %s:%s...' % addr)
		    sock.send(b'Welcome!')
		    while True:
		        data = sock.recv(1024)
		        time.sleep(1)
		        if not data or data.decode('utf-8') == 'exit':
		            break
		        sock.send(('Hello, %s!' % data.decode('utf-8')).encode('utf-8'))
		    sock.close()
		    print('Connection from %s:%s closed.' % addr)

5. 之前的是服务器代码，现在需要编写客户端代码

		s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		# 建立连接:
		s.connect(('127.0.0.1', 9999))
		# 接收欢迎消息:
		print(s.recv(1024).decode('utf-8'))
		for data in [b'Michael', b'Tracy', b'Sarah']:
		    # 发送数据:
		    s.send(data)
		    print(s.recv(1024).decode('utf-8'))
		s.send(b'exit')
		s.close()

## 17.3 UDP编程

TCP是建立可靠连接，并且通信双方都可以以流的形式发送数据。**相对TCP，UDP则是面向无连接的协议**

- 使用UDP协议时，不需要建立连接。只需要知道IP地址和端口号就可以发送数据，但是不能保证到达

- UDP比TCP数据传输更快，对于不要求可靠到达的数据可以使用UDP

与TCP相似，UDP的通信双方也分为客户端和服务端

1. 服务端需要绑定端口和ip

		s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
		# 绑定端口:
		s.bind(('127.0.0.1', 9999))

	- `SOCK_DGRAM`指定了Socket的类型是UDP

2. UDP不需要使用`listen()`等待服务器连接，而是直接接收数据

		print('Bind UDP on 9999...')
		while True:
		    # 接收数据:
		    data, addr = s.recvfrom(1024)
		    print('Received from %s:%s.' % addr)
		    s.sendto(b'Hello, %s!' % data, addr)

	- `recvfrom()`方法返回数据和客户端的地址与端口。这样服务器收到数据之后，可以直接调用`sendto()`就可以把数据用UDP发给客户端

3. 客户端使用

		s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
		for data in [b'Michael', b'Tracy', b'Sarah']:
		    # 发送数据:
		    s.sendto(data, ('127.0.0.1', 9999))
		    # 接收数据:
		    print(s.recv(1024).decode('utf-8'))
		s.close()

	- 不需要调用`connect()`创建连接，而是直接通过`sendto()`给服务器发送数据

	- 从服务器接收数据仍然调用recv()方法。

**服务器绑定UDP端口和TCP端口互不冲突，也就是说，UDP的9999端口与TCP的9999端口可以各自绑定。**


# 18 电子邮件

一封电子邮件的旅程：

	发件人 -> MUA -> MTA -> MTA -> 若干个MTA -> MDA <- MUA <- 收件人

- MUA:Mail User Agent 邮件用户代理 即电子邮件软件(qq邮箱)
- MTA：Mail Transfer Agent 邮件传输代理 发送Email的服务提供商
- MDA:Mail Delivery Agent 邮件投递代理 接收Email的服务提供商

邮件发送和接收的本质就是：

- 编写MUA把邮件发到MTA

- 编写MUA从MDA上收邮件

协议：

- 发送邮件时，MUA到MTA使用的协议是`SMTP:Simple Mail Transfer Protocol `。MTA到另外一个MTA也是使用SMTP

- 接收邮件时，MUA和MDA使用的协议有俩种：
	- `POP :Post Ofiice Protocol`,目前版本是3 所以称为POP3。
	- `IMAP: Internet Message Access Protocol`，目前版本是4，**优点是不但能取邮件，还可以直接操作MDA上存储的邮件，比如将邮件从收件箱移到垃圾箱**


邮件客户端软件在发邮件时，会让你先配置SMTP服务器，也就是你要发到哪个MTA上。假设你正在使用163的邮箱，你就不能直接发到新浪的MTA上，因为它只服务新浪的用户，所以，你得填163提供的SMTP服务器地址：smtp.163.com，为了证明你是163的用户，SMTP服务器还要求你填写邮箱地址和邮箱口令，这样，MUA才能正常地把Email通过SMTP协议发送到MTA。

类似的，从MDA收邮件时，MDA服务器也要求验证你的邮箱口令，确保不会有人冒充你收取你的邮件，所以，Outlook之类的邮件客户端会要求你填写POP3或IMAP服务器地址、邮箱地址和口令，这样，MUA才能顺利地通过POP或IMAP协议从MDA取到邮件。

在使用Python收发邮件前，请先准备好至少两个电子邮件，如xxx@163.com，xxx@sina.com，xxx@qq.com等，注意两个邮箱不要用同一家邮件服务商。

最后特别注意，目前大多数邮件服务商都需要手动打开SMTP发信和POP收信的功能，否则只允许在网页登录：

![](https://cdn.liaoxuefeng.com/cdn/files/attachments/00144780905835577e7b77a053849829f4cf034d1fc48c3000/l)

## 18.1 SMTP发送邮件

