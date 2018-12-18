# HTTP学习

[HTTP必知必会的那些](https://mp.weixin.qq.com/s/Fazx13maQfPJItfkOqk9FQ)

# 1. 简介

# 2. HTTP报文

HTTP报文可以分为请求报文和响应报文，格式大同小异。主要分为三个部分：

1. 起始行

2. 首部

3. 主体

## 2.1 请求报文格式
	
	<method> <request-url> <version>
	<headers>
	
	<entity-body>

	//实例
	GET http://s1.mini.wpscdn.cn/config/wps/tray/boot.json?_t=1545127200 HTTP/1.1
	User-Agent: Mozilla/5.0
	Host: s1.mini.wpscdn.cn
	Pragma: no-cache



## 2.2 响应报文格式

	<version> <status> <reason-phrase>
	<headers>
	
	<entity-body>

	//实例
	POST https://servicecut.meizu.com/interface/locate HTTP/1.1
	X-SDK-VERSION: 2.2.1
	X-SN: 91QEBPF5SF29
	X-IMEI: 862937039710025
	X-MAC: 
	User-Agent: Dalvik/2.1.0 (Linux; U; Android 5.1; m3 note Build/LMY47I)
	Host: servicecut.meizu.com
	Connection: Keep-Alive
	Accept-Encoding: gzip
	Content-Type: application/x-www-form-urlencoded
	Content-Length: 48
	
	name=uxip.meizu.com&sim_card_sp=wifi&version=2.0

## 2.3 标签含义

- `<method>` 指请求方法，常用的主要是Get、 Post、Head

- `<version>` 指协议版本，现在通常都是Http/1.1了

- `<request-url>` 请求地址

- `<headers>` 请求头

- `<status>` 指响应状态码， 我们熟悉的200、404等等

- `<reason-phrase>` 原因短语，200 OK 、404 Not Found 这种后面的描述就是原因短语，通常不必太关注。


### 2.3.1 method标签

请求方法常用的有`GET` 和`POST`俩种,俩者的区别是:

1. **传输形式有区别**:

	**`GET`方法发起的请求,会将请求参数拼接在`<request-url>`尾部,格式是`url?param=xxx&param2=xxx&[.....]`**

	这种传输形式使得参数都暴露在地址栏中,并且`url`是`ASCII`编码的,如果参数中出现`Unicode`编码的字符,例如汉字等, 都会经过编码后再传输 

	另外`HTTP`协议本身并没有对url长度做限制,但是一些浏览器和服务器可能会有限制,所以通过`GET`方法发起的请求中参数不能够太长

	**`POST`方法发起的请求是将参数放在请求体中**,不会存在参数过长的问题

2. **方法本身的语意有区别:**

	GET方法通常是指从服务器获取某个URL资源，其行为可以看作是一个读操作，对同一个URL进行多次GET并不会对服务器产生什么影响。

	而POST方法通常是对某个URL进行添加、修改，例如一个表单提交，通常会往服务器插入一条记录。多次POST请求可能导致服务器的数据库中添加了多条记录。所以从语义上来讲，两者也是不能混为一谈的


### 2.3.2 status标签

常见的状态码主要有  

- 200 OK  请求成功，实体包含请求的资源  

- 301 Moved Permanent 请求的URL被移除了，通常会在Location首部中包含新的URL用于重定向。  

- 304 Not Modified    条件请求进行再验证，资源未改变。  

- 404 Not Found       资源不存在  

- 206 Partial Content 成功执行一个部分请求。这个在用于断点续传时会涉及到。

### 2.3.3 header标签

在请求报文和响应报文中都可以携带一些信息，通过与其他部分配合，能够实现各种强大的功能。这些信息位于起始行之下与请求实体之间，以键值对的形式，称之为首部。

每条首部以回车换行符结尾，最后一个首部额外多一个换行，与实体分隔开。

**比较重要的首部标签:**

	Date  
	Cache-Control  
	Last-Modified  
	Etag  
	Expires  
	If-Modified-Since   
	If-None-Match  
	If-Unmodified-Since  
	If-Range  
	If-Match

## 2.4 主体

请求发送的资源,或者是说返回的资源

# 3. HTTP缓存

发起一个HTTP请求后，服务器返回所请求的资源，这时客户端可以将该资源的副本存储在本地，这样当再次对该url资源发起请求时，客户端能快速的从本地存储设备中获取到该url资源，这就是所谓的缓存。

- 缓存既可以节约不必要的网络带宽，又能迅速对http请求做出响应。


## 3.1 HTTP缓存中的概念

1. 新鲜度检测

2. 再验证

3. 再验证命中


网络请求中,有些url所对应的资源并不是一成不变的，服务器中该url的资源可能在一定时间之后会被修改,这时本地缓存中的资源将与服务器一侧的资源有差异。

既然在一定时间之后可能资源会改变，那么在某个时间之前我们可以认为这个资源没有改变，从而放心大胆的使用缓存资源，当请求时间超过来该时间，则认为这个缓存资源可能不再与服务器端一致了。**所以当发起一个请求时，需要先对缓存的资源进行判断，看看究竟我们是否可以直接使用该缓存资源，这个就叫做新鲜度检测**。即每个资源就像一个食品一样，拥有一个过期时间，吃之前需要先看看有没有过期。

如果发现该缓存资源已经超过了一定的时间，**那么再次发起请求时不会直接将缓存资源返回，而是先去服务器查看该资源是否已经改变，这个就叫做再验证**。

如果服务器发现对应的url资源并没有发生变化，则会**返回`304 Not Modified`，并且不再返回对应的实体,这称之为再验证命中**。相反如果再验证未命中，则返回`200 OK`，并将改变后的url资源返回，此时缓存可以更新以待之后请求。


## 3.2 缓存具体实现方式

1. **新鲜度检测**  

	需要通过检测资源是否超过一定的时间，来判断缓存资源是否新鲜可用。那么这个一定的时间怎么决定呢？

	其实是由服务器通过在响应报文中增加`Cache-Control:max-age`，或是`Expire`这两个首部来实现的。

	值得注意的是`Cache-Control`是`http1.1`的协议规范，通常是接相对的时间，即多少秒以后，需要结合`last-modified`这个首部计算出绝对时间。而`Expire`是`http1.0`的规范，后面接一个绝对时间。

2. 再验证  

	如果通过新鲜度检测发现需要请求服务器进行再验证，那么至少需要告诉服务器，已经缓存了一个什么样的资源了，然后服务器来判断这个缓存资源到底是不是与当前的资源一致。逻辑是这样没错。那怎么告诉服务器当前已经有一个备用的缓存资源了呢？

	可以采用一种称之为**条件请求的方式**实现再验证。

	- **Http定义了5个首部用于条件请求**:  
	
			If-Modified-Since   
			If-None-Match  
			If-Unmodified-Since  
			If-Range  
			If-Match

	`If-Modified-Since` 可以结合`Last-Modified`这个服务器返回的响应首部使用，当发起条件请求时，将`Last-Modified`首部的值作为`If-Modified-Since`首部的值传递到服务器，意思是查询服务器的资源自从上一次缓存之后是否有修改。

	`If-None-Match` 需要结合另一个`Etag`的服务器返回的响应首部使用。`Etag`首部实际上可以认为是服务器对文档资源定义的一个版本号。有时候一个文档被修改了，可能所做的修改极为微小，并不需要所有的缓存都重新下载数据。或者说某一个文档的修改周期极为频繁，以至于以秒为时间粒度的判断已经无法满足需求。这个时候可能就需要`Etag`这个首部来表明这个文档的版号了。发起条件请求时可将缓存时保存下来的`Etag`的值作为`If-None-Match`首部的值发送至服务器，如果服务器的资源的`Etag`与当前条件请求的`Etag`一致，表明这次再验证命中。  

## 3.3 OkHttp的缓存机制介绍



# 4. OAuth

`OAuth`是一个用于**授权第三方获取相应资源的协议**。与以往的授权方式不同的是，`OAuth`的授权能避免用户暴露自己的用户密码给第三方，从而更加的安全。

`OAuth`协议通过设置一个授权层，以区分用户和第三方应用。用户本身可以通过用户密码登陆服务提供商，获取到账户所有的资源。而第三方应用只能通过向用户请求授权，获取到一个`Access Token`，用以登陆授权层，从而在指定时间内获取到用户授权访问的部分资源。


## 4.1 OAuth定义的角色

