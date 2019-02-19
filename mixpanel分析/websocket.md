# websocket

[Android WebSocket 编程](https://juejin.im/post/5aaf165b518825556f5537f7)

[WebSocket协议：5分钟从入门到精通](https://www.cnblogs.com/chyingp/p/websocket-deep-in.html)

[WS/WSS](https://www.alibabacloud.com/help/zh/doc-detail/63421.htm)

[SSLSocketFactory的作用](https://stackoverflow.com/questions/9921548/sslsocketfactory-in-java)

# 简介


# 1. SSLSocket



# 2. 什么是WS/WSS？
`WebSocket (WS)`是HTML5一种新的协议。它实现了浏览器与服务器全双工通信，能更好地节省服务器资源和带宽并达到实时通讯。WebSocket建立在TCP之上，同HTTP一样通过TCP来传输数据，但是它和HTTP最大不同是：

- WebSocket是一种双向通信协议，**在建立连接后，WebSocket服务器和Browser/Client Agent都能主动的向对方发送或接收数据**，就像Socket一样；WebSocket需要类似TCP的客户端和服务器端通过握手连接，连接成功后才能相互通信。

`WSS（Web Socket Secure）`是WebSocket的加密版本。


## 2.1 为何使用WS/WSS？

随着互联网的蓬勃发展，各种类型的Web应用层出不穷，很多应用要求服务端有能力进行实时推送能力（比如直播间聊天室），以往很多网站为了实现推送技术，所用的技术都是轮询。轮询是在特定的的时间间隔（如每1秒），由浏览器对服务器发出HTTP请求，然后由服务器返回最新的数据给客户端的浏览器。这种传统的模式带来很明显的缺点，即浏览器需要不断地向服务器发出请求，然而HTTP请求可能包含较长的头部，其中真正有效的数据可能只是很小的一部分，显然这样会浪费很多的带宽资源。

在这种情况下，HTML5定义了WebSocket协议，能更好地节省服务器资源和带宽，并且能够更实时地进行通讯。WebSocket实现了浏览器与服务器全双工(`full-duplex`)通信，允许服务器主动发送信息给客户端。