# 1. 流程

1. 创建SSLSocket,通过websocket创建wss连接

2. 服务端连接成功之后,会返回一条type为`device_info_request`的json.客户端在收到这条json之后,收集设备信息 并组装成一条type为 `device_info_response`的json 发送给服务端

3. 服务端收到客户端的设备信息,经过处理之后 会下发第一条带配置的 `snapshot_request` json,这条json中包含了 客户端需要采集哪类控件以及具体的属性等等信息

4. 客户端针对配置信息 收集具体的信息,并将完整的控件树上传到服务端

5. 服务端根据客户端上传的控件信息 以及 截图 ,绘制视图 .随后每隔一段时间 都会 下发不带配置文件的`snapshot_request` json

6. 服务端对比 截图的hash值 判断是否需要重绘


# 2. 指令

**服务端指令**:

1. device_info_request:请求客户端信息

2. snapshot_request:请求客户端截图 以及 视图关系

3. change_request:请求客户端更改点击事件信息

4. event_binding_request : 请求客户端绑定点击事件

5. clear_request:请求客户端清除点击事件

6. tweak_request:ABTest

	
# 3. 问题

1. 不同控件需要不同的处理逻辑,目前mixpanel 对一些列表控件的支持并不完善

2. 服务端对信息处理的细节  由于没有服务端代码 不了解
