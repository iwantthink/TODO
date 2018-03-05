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

Python内置了发送邮件的协议(SMTP),可以发送纯文本邮件，HTML邮件以及带附件的邮件

Python提供了`smtplib`和`email`俩个模块支持SMTP，前者负责发送邮件，后者负责构造邮件

构造一个邮件对象就是一个Messag对象，如果构造一个MIMEText对象，就表示一个文本邮件对象，如果构造一个MIMEImage对象，就表示一个作为附件的图片，要把多个对象组合起来，就用MIMEMultipart对象，而MIMEBase可以表示任何对象。它们的继承关系如下：

	Message
	+- MIMEBase
	   +- MIMEMultipart
	   +- MIMENonMultipart
	      +- MIMEMessage
	      +- MIMEText
	      +- MIMEImage


### 18.1.1 纯文本邮件

	from email.mime.text import MIMEText
	msg = MIMEText('hello, send by Python...', 'plain', 'utf-8')

- 构造的MIMEText对象，第一个参数是 邮件正文，第二个参数是MIME的`subtype`，传入`plain`表示纯文本，最终的MIME就是`text/plain`。第三个参数是编码，保证兼容


	# 输入Email地址和口令:
	from_addr = input('From: ')
	password = input('Password: ')
	# 输入收件人地址:
	to_addr = input('To: ')
	# 输入SMTP服务器地址:
	smtp_server = input('SMTP server: ')
	
	import smtplib
	server = smtplib.SMTP(smtp_server, 25) # SMTP协议默认端口是25
	server.set_debuglevel(1)
	server.login(from_addr, password)
	server.sendmail(from_addr, [to_addr], msg.as_string())
	server.quit()

- `set_debuglevel(1)`就可以打印出和SMTP服务器交互的所有信息
- SMTP协议就是简单的文本命令和响应
- `login()`方法用来登录SMTP服务器
- `sendmail()`方法就是发邮件，一次性可以发送给多个人，所以传入一个`list`
- 邮件正文是一个`str`
- `as_string()`就是把MIMEText对象变成str

完成上面的内容之后，已经完成基本的功能，但是还缺少：

- 邮件主题
- 发件人信息
- 收件人信息

**以上内容不是通过SMTP协议发送给MTA，而是包含在发给MTA的文本中，所以需要添加`From`,`To`,`Subject`到`MIMEText`对象中，组成一个完成的邮件**

	from email import encoders
	from email.header import Header
	from email.mime.text import MIMEText
	from email.utils import parseaddr, formataddr
	
	import smtplib
	
	def _format_addr(s):
	    name, addr = parseaddr(s)
	    return formataddr((Header(name, 'utf-8').encode(), addr))
	
	from_addr = input('From: ')
	password = input('Password: ')
	to_addr = input('To: ')
	smtp_server = input('SMTP server: ')
	
	msg = MIMEText('hello, send by Python...', 'plain', 'utf-8')
	msg['From'] = _format_addr('Python爱好者 <%s>' % from_addr)
	msg['To'] = _format_addr('管理员 <%s>' % to_addr)
	msg['Subject'] = Header('来自SMTP的问候……', 'utf-8').encode()
	
	server = smtplib.SMTP(smtp_server, 25)
	server.set_debuglevel(1)
	server.login(from_addr, password)
	server.sendmail(from_addr, [to_addr], msg.as_string())
	server.quit()

- 由于发件人信息和收件人信息可能包含中文，所以需要通过`Header`对象进行编码

- `msg['To']`接收的字符串而不是list,如果有多个接收地址，用`','`分隔

- 看到的收件人的名字很可能不是我们传入的管理员，因为很多邮件服务商在显示邮件时，会把收件人名字自动替换为用户注册的名字，但是其他收件人名字的显示不受影响。


查看Email的原始内容，可以看到经过编码的邮件头：

	From: =?utf-8?b?UHl0aG9u54ix5aW96ICF?= <xxxxxx@163.com>
	To: =?utf-8?b?566h55CG5ZGY?= <xxxxxx@qq.com>
	Subject: =?utf-8?b?5p2l6IeqU01UUOeahOmXruWAmeKApuKApg==?=

- 这就是经过`Header`对象编码的文本，包含`utf-8`编码信息和Base64编码的文本

### 18.1.2 发送HMTL邮件 

只需要在要18.1.1 的基础上，在构造`MIMEText`对象时，把HTML字符串传进去，再把第二个参数由`plain`变成`html`即可

	msg = MIMEText('<html><body><h1>Hello</h1>' +
	    '<p>send by <a href="http://www.python.org">Python</a>...</p>' +
	    '</body></html>', 'html', 'utf-8')

### 18.1.3 发送附件

带附件的邮件可以看做包含若干部分的邮件：文本和各个附件本身，可以通过`MIMEMultipart`对象代表邮件本身，然后往里面加上一个`MIMEText`和`MIMEBase`分别作为 邮件本身正文和附件

	# 邮件对象:
	msg = MIMEMultipart()
	msg['From'] = _format_addr('Python爱好者 <%s>' % from_addr)
	msg['To'] = _format_addr('管理员 <%s>' % to_addr)
	msg['Subject'] = Header('来自SMTP的问候……', 'utf-8').encode()
	
	# 邮件正文是MIMEText:
	msg.attach(MIMEText('send with file...', 'plain', 'utf-8'))
	
	# 添加附件就是加上一个MIMEBase，从本地读取一个图片:
	with open('/Users/michael/Downloads/test.png', 'rb') as f:
	    # 设置附件的MIME和文件名，这里是png类型:
	    mime = MIMEBase('image', 'png', filename='test.png')
	    # 加上必要的头信息:
	    mime.add_header('Content-Disposition', 'attachment', filename='test.png')
	    mime.add_header('Content-ID', '<0>')
	    mime.add_header('X-Attachment-Id', '0')
	    # 把附件的内容读进来:
	    mime.set_payload(f.read())
	    # 用Base64编码:
	    encoders.encode_base64(mime)
	    # 添加到MIMEMultipart:
	    msg.attach(mime)

### 18.1.4 发送图片

如果要把一个图片嵌入到邮件正文，直接在HTML邮件中添加图片地址链接是不行的，因为大部分邮件服务商都会自动屏蔽带外链的图片，因为不知道这些链接是否指向恶意网站

要把图片嵌入邮件正文，只需要按照发送附件的方式，先把图片作为附件添加，然后在HTML中通过`src="cid=0"` 就可以把附件作为图片嵌入。如果有多张图片，一次给它们编号，然后引用不同的`cid = ?`即可

将18.1.3中的代码进行修改，`MIMEMultipart`的`MIMEText`参数由`plain`改成`html`,然后在适当位置引用图片

	msg.attach(MIMEText('<html><body><h1>Hello</h1>' +
	    '<p><img src="cid:0"></p>' +
	    '</body></html>', 'html', 'utf-8'))

### 18.1.5 同时支持HTML和Plain格式

为了防止收件人设备太古老，查看不了HTML邮件。可以在发送HTML的同时再附加一个纯文本。

利用`MIMEMultipart`就可以组合一个HTML和Plain，要注意指定subtype是`alternative`
	
	msg = MIMEMultipart('alternative')
	msg['From'] = ...
	msg['To'] = ...
	msg['Subject'] = ...
	
	msg.attach(MIMEText('hello', 'plain', 'utf-8'))
	msg.attach(MIMEText('<html><body><h1>Hello</h1></body></html>', 'html', 'utf-8'))
	# 正常发送msg对象...

### 18.1.6 加密SMTP

使用标准的25端口连接SMTP服务器时，使用的是明文传输，发送邮件的整个过程可能会被窃听。要更安全地发送邮件，可以加密SMTP会话，实际上就是先创建SSL安全连接，然后再使用SMTP协议发送邮件。

某些邮件服务商，例如Gmail，提供的SMTP服务必须要加密传输。我们来看看如何通过Gmail提供的安全SMTP发送邮件。

	smtp_server = 'smtp.gmail.com'
	smtp_port = 587
	server = smtplib.SMTP(smtp_server, smtp_port)
	server.starttls()
	# 剩下的代码和前面的一模一样:
	server.set_debuglevel(1)
	...

- 只需要在创建`SMTP`对象后，立刻调用`starttls()`方法，就创建了安全连接

## 18.2 POP3收取邮件

收取邮件就是编写一个`MUA`作为客户端，从`MDA`把邮件获取到用户的电脑或者手机上。收取邮件最常用的协议是`POP`协议

Python内置一个`poplib`模块，实现了`POP3`协议，可以直接用来收取邮件

- `POP3`协议收取的不是一个已经可阅读的邮件本身，而是邮件的原始文本，即经过编码后的一段文本

- 将`POP3`协议收取的文本变成可阅读的邮件，需要通过`email`模块提供的各种类进行解析

**收取邮件分俩个步骤：**

1. 用`poplib`把邮件的原始文本下载到本地
2. 用`email`解析原始文本，还原为邮件对象

### 18.2.1 通过POP3下载邮件

	import poplib
	
	# 输入邮件地址, 口令和POP3服务器地址:
	email = input('Email: ')
	password = input('Password: ')
	pop3_server = input('POP3 server: ')
	
	# 连接到POP3服务器:
	server = poplib.POP3(pop3_server)
	# 可以打开或关闭调试信息:
	server.set_debuglevel(1)
	# 可选:打印POP3服务器的欢迎文字:
	print(server.getwelcome().decode('utf-8'))
	
	# 身份认证:
	server.user(email)
	server.pass_(password)
	
	# stat()返回邮件数量和占用空间:
	print('Messages: %s. Size: %s' % server.stat())
	# list()返回所有邮件的编号:
	resp, mails, octets = server.list()
	# 可以查看返回的列表类似[b'1 82923', b'2 2184', ...]
	print(mails)
	
	# 获取最新一封邮件, 注意索引号从1开始:
	index = len(mails)
	resp, lines, octets = server.retr(index)
	
	# lines存储了邮件的原始文本的每一行,
	# 可以获得整个邮件的原始文本:
	msg_content = b'\r\n'.join(lines).decode('utf-8')
	# 稍后解析出邮件:
	msg = Parser().parsestr(msg_content)
	
	# 可以根据邮件索引号直接从服务器删除邮件:
	# server.dele(index)
	# 关闭连接:
	server.quit()

- 用POP3获取邮件其实很简单，要获取所有邮件，只需要循环使用retr()把每一封邮件内容拿到即可。真正麻烦的是把邮件的原始内容解析为可以阅读的邮件对象。


### 18.2.2 解析邮件

解析邮件与构造邮件的逻辑正好相反，首先导入包

	from email.parser import Parser
	from email.header import decode_header
	from email.utils import parseaddr
	
	import poplib

只需要一行代码即可将邮件内容解析为`Message`对象

	msg = Parser().parsestr(msg_content)

这个Message可能是一个`MIMEMultipart`对象，即包含嵌套的其他`MIMEBase`对象，并且嵌套可能不止一层，所以需要递归的打印出`Message`的信息

	# indent用于缩进显示:
	def print_info(msg, indent=0):
	    if indent == 0:
	        for header in ['From', 'To', 'Subject']:
	            value = msg.get(header, '')
	            if value:
	                if header=='Subject':
	                    value = decode_str(value)
	                else:
	                    hdr, addr = parseaddr(value)
	                    name = decode_str(hdr)
	                    value = u'%s <%s>' % (name, addr)
	            print('%s%s: %s' % ('  ' * indent, header, value))
	    if (msg.is_multipart()):
	        parts = msg.get_payload()
	        for n, part in enumerate(parts):
	            print('%spart %s' % ('  ' * indent, n))
	            print('%s--------------------' % ('  ' * indent))
	            print_info(part, indent + 1)
	    else:
	        content_type = msg.get_content_type()
	        if content_type=='text/plain' or content_type=='text/html':
	            content = msg.get_payload(decode=True)
	            charset = guess_charset(msg)
	            if charset:
	                content = content.decode(charset)
	            print('%sText: %s' % ('  ' * indent, content + '...'))
	        else:
	            print('%sAttachment: %s' % ('  ' * indent, content_type))

- 邮件中的`Subject`和`Email`中包含的名称都是经过编码的str，要正常显示，必须decode

		def decode_str(s):
		    value, charset = decode_header(s)[0]
		    if charset:
		        value = value.decode(charset)
		    return value

	- `decode_header()`返回一个List，因为像Cc、Bcc这样的字段可能包含多个邮件地址，所以解析出来的会有多个元素

- 文本邮件的内容也是str，还需要检测编码，否则非`UTF-8`编码的邮件都无法正常显示

		def guess_charset(msg):
		    charset = msg.get_charset()
		    if charset is None:
		        content_type = msg.get('Content-Type', '').lower()
		        pos = content_type.find('charset=')
		        if pos >= 0:
		            charset = content_type[pos + 8:].strip()
		    return charset

# 19 访问数据库

为了便于程序保存和读取数据，而且，能直接通过条件快速查询到指定的数据，就出现了数据库（Database）这种专门用于集中存储和查询的软件。

现在广泛使用的关系数据库是基于20世纪70年代的关系模型诞生的，**在关系数据库中，基于表`(table)`的一对多的关系就是关系数据库的基础**


免费的开源数据库：

- **MySQL，大家都在用，一般错不了；推荐使用**

- PostgreSQL，学术气息有点重，其实挺不错，但知名度没有MySQL高；

- sqlite，嵌入式数据库，适合桌面和移动应用。


## 19.1 使用Sqlite

Sqlite 是一种嵌入式数据库，它的数据库就是一个文件，由于SQLite本身是C写的，而且体积很小，所以经常被集成到各种应用程序

Python内置了SQLite3，所以在Python中可以直接使用

概念：

- 表是数据库中存放关系数据的集合，一个数据库里面通常都包含多个表

- 要操作关系数据库，首先需要连接到数据库，一个数据库的连接被称为`Connection`

- 连接到数据库后，需要打开游标，称之为`Cursor`,通过`Cursor`执行SQL语句，然后获取对应结果

- Python定义了一套操作数据库的API接口，任何数据库要连接到Python，只需要提供符合Python标准的数据库驱动即可

### 19.1.1 创建

	# 导入SQLite驱动:
	>>> import sqlite3
	# 连接到SQLite数据库
	# 数据库文件是test.db
	# 如果文件不存在，会自动在当前目录创建:
	>>> conn = sqlite3.connect('test.db')
	# 创建一个Cursor:
	>>> cursor = conn.cursor()
	# 执行一条SQL语句，创建user表:
	>>> cursor.execute('create table user (id varchar(20) primary key, name varchar(20))')
	<sqlite3.Cursor object at 0x10f8aa260>
	# 继续执行一条SQL语句，插入一条记录:
	>>> cursor.execute('insert into user (id, name) values (\'1\', \'Michael\')')
	<sqlite3.Cursor object at 0x10f8aa260>
	# 通过rowcount获得插入的行数:
	>>> cursor.rowcount
	1
	# 关闭Cursor:
	>>> cursor.close()
	# 提交事务:
	>>> conn.commit()
	# 关闭Connection:
	>>> conn.close()、

### 19.1.2 查询

	>>> conn = sqlite3.connect('test.db')
	>>> cursor = conn.cursor()
	# 执行查询语句:
	>>> cursor.execute('select * from user where id=?', ('1',))
	<sqlite3.Cursor object at 0x10f8aa340>
	# 获得查询结果集:
	>>> values = cursor.fetchall()
	>>> values
	[('1', 'Michael')]
	>>> cursor.close()
	>>> conn.close()

- 使用Python的DB-API时，只要搞清楚Connection和Cursor对象，打开后一定记得关闭，就可以放心地使用。

- 使用Cursor对象执行insert，update，delete语句时，执行结果由rowcount返回影响的行数，就可以拿到执行结果。

- 使用Cursor对象执行select语句时，通过featchall()可以拿到结果集。结果集是一个list，每个元素都是一个tuple，对应一行记录。

- 如果SQL语句带有参数，那么需要把参数按照位置传递给`execute()`方法，有几个`?`占位符就必须对应几个参数

		cursor.execute('select * from user where name=? and pwd=?', ('abc', 'password'))


## 19.2 使用MYSQL

MySQL是Web世界中使用最广泛的数据库服务器。SQLite的特点是轻量级、可嵌入，但不能承受高并发访问，适合桌面和移动应用。而MySQL是为服务器端设计的数据库，能承受高并发访问，同时占用的内存也远远大于SQLite。

MYSql内部支持多种数据库引擎，最常用的是支持数据库事务的InnoDB

如何连接到MYSQL服务器的test数据库 演示：

	# 导入MySQL驱动:
	>>> import mysql.connector
	# 注意把password设为你的root口令:
	>>> conn = mysql.connector.connect(user='root', password='password', database='test')
	>>> cursor = conn.cursor()
	# 创建user表:
	>>> cursor.execute('create table user (id varchar(20) primary key, name varchar(20))')
	# 插入一行记录，注意MySQL的占位符是%s:
	>>> cursor.execute('insert into user (id, name) values (%s, %s)', ['1', 'Michael'])
	>>> cursor.rowcount
	1
	# 提交事务:
	>>> conn.commit()
	>>> cursor.close()
	# 运行查询:
	>>> cursor = conn.cursor()
	>>> cursor.execute('select * from user where id = %s', ('1',))
	>>> values = cursor.fetchall()
	>>> values
	[('1', 'Michael')]
	# 关闭Cursor和Connection:
	>>> cursor.close()
	True
	>>> conn.close()

- 执行INSERT等操作后要调用commit()提交事务；

- MySQL的SQL占位符是%s。

## 19.3 使用SQLAlchemy

数据库表是一个二维表，包含多行多列。把一个表的内容用Python的数据结构表示出来的话，可以用一个list表示多行，list的每一个元素是tuple，表示一行记录，比如，包含id和name的user表

	[
	    ('1', 'Michael'),
	    ('2', 'Bob'),
	    ('3', 'Adam')
	]

- **Python的DB-API返回的数据结构就是这样的**

- 但是tuple表示一行很难看出表的结构，如果把一个tuple用class实例来表示，就可以更容易的看出表的结构

		class User(object):
		    def __init__(self, id, name):
		        self.id = id
		        self.name = name
		
		[
		    User('1', 'Michael'),
		    User('2', 'Bob'),
		    User('3', 'Adam')
		]

**以上就是ORM技术:`Object-Relational Mapping`.关系数据库的表结构可以映射到对象上**

Python中提供了ORM框架来做这个转换，最有名的ORM框架是`sqlalchemy`

### 19.3.1 用法

导入SQLAlchemy,初始化DBSession

	# 导入:
	from sqlalchemy import Column, String, create_engine
	from sqlalchemy.orm import sessionmaker
	from sqlalchemy.ext.declarative import declarative_base
	
	# 创建对象的基类:
	Base = declarative_base()
	
	# 定义User对象:
	class User(Base):
	    # 表的名字:
	    __tablename__ = 'user'
	
	    # 表的结构:
	    id = Column(String(20), primary_key=True)
	    name = Column(String(20))
	
	# 初始化数据库连接:
	engine = create_engine('mysql+mysqlconnector://root:password@localhost:3306/test')
	# 创建DBSession类型:
	DBSession = sessionmaker(bind=engine)

