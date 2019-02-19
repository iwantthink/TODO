# 1. 流程

1. 创建SSLSocket,通过websocket创建wss连接

2. 服务端连接成功之后,会返回一条type为`device_info_request`的json.客户端在收到这条json之后,收集设备信息 并组装成一条type为 `device_info_response`的json 发送给服务端

3. 服务端收到客户端的设备信息,经过处理之后 会下发第一条带配置的 `snapshot_request` json,这条json中包含了 客户端需要采集哪类控件以及具体的属性等等信息

4. 客户端针对配置信息 收集具体的信息,并将完整的控件树上传到服务端

5. 服务端根据客户端上传的控件信息 以及 截图 ,绘制视图 .随后每隔一段时间 都会 下发不带配置文件的`snapshot_request` json

6. 服务端对比 截图的hash值 判断是否需要重绘

	客户端会监听视图树是否发生变化,如果发生变化 那下一次`snapshot_resp` 会重新上传完整的视图树

7. 服务端会下发一条 `event_binding_request`,如果当前服务端保存有事件信息


# 2. 指令类型

## 2.1 服务端指令类型

1. `device_info_request`:要求客户端发送设备信息

		{"type":"device_info_request"}

2. `snapshot_request`:请求客户端截图 以及 视图关系

		{"type":"snapshot_request","payload":{"config":{"classes":[{"name":"android.view.View","properties":[{"name":"importantForAccessibility","get":{"selector":"isImportantForAccessibility","parameters":[],"result":{"type":"java.lang.Boolean"}}},{"name":"clickable","get":{"selector":"isClickable","parameters":[],"result":{"type":"java.lang.Boolean"}}},{"name":"alpha","get":{"selector":"getAlpha","parameters":[],"result":{"type":"java.lang.Float"}},"set":{"selector":"setAlpha","parameters":[{"type":"java.lang.Float"}]}},{"name":"hidden","get":{"selector":"getVisibility","parameters":[],"result":{"type":"java.lang.Integer"}},"set":{"selector":"setVisibility","parameters":[{"type":"java.lang.Integer"}]}},{"name":"background","get":{"selector":"getBackground","parameters":[],"result":{"type":"android.graphics.drawable.Drawable"}},"set":{"selector":"setBackground","parameters":[{"type":"android.graphics.drawable.ColorDrawable"}]}}]},{"name":"android.widget.TextView","properties":[{"name":"importantForAccessibility","get":{"selector":"isImportantForAccessibility","parameters":[],"result":{"type":"java.lang.Boolean"}}},{"name":"clickable","get":{"selector":"isClickable","parameters":[],"result":{"type":"java.lang.Boolean"}}},{"name":"alpha","get":{"selector":"getAlpha","parameters":[],"result":{"type":"java.lang.Float"}},"set":{"selector":"setAlpha","parameters":[{"type":"java.lang.Float"}]}},{"name":"hidden","get":{"selector":"getVisibility","parameters":[],"result":{"type":"java.lang.Integer"}},"set":{"selector":"setVisibility","parameters":[{"type":"java.lang.Integer"}]}},{"name":"text","get":{"selector":"getText","parameters":[],"result":{"type":"java.lang.CharSequence"}},"set":{"selector":"setText","parameters":[{"type":"java.lang.CharSequence"}]}},{"name":"textColor","get":{"selector":"getTextColors","parameters":[],"result":{"type":"android.content.res.ColorStateList"}},"set":{"selector":"setTextColor","parameters":[{"type":"java.lang.Integer"}]}},{"name":"fontSize","get":{"selector":"getTextSize","parameters":[],"result":{"type":"java.lang.Float"}},"set":{"selector":"setTextSize","parameters":[{"type":"java.lang.Integer"},{"type":"java.lang.Float"}]}}]},{"name":"android.widget.ImageView","properties":[{"name":"importantForAccessibility","get":{"selector":"isImportantForAccessibility","parameters":[],"result":{"type":"java.lang.Boolean"}}},{"name":"clickable","get":{"selector":"isClickable","parameters":[],"result":{"type":"java.lang.Boolean"}}},{"name":"alpha","get":{"selector":"getAlpha","parameters":[],"result":{"type":"java.lang.Float"}},"set":{"selector":"setAlpha","parameters":[{"type":"java.lang.Float"}]}},{"name":"hidden","get":{"selector":"getVisibility","parameters":[],"result":{"type":"java.lang.Integer"}},"set":{"selector":"setVisibility","parameters":[{"type":"java.lang.Integer"}]}},{"name":"image","get":{"selector":"getDrawable","parameters":[],"result":{"type":"android.graphics.drawable.Drawable"}},"set":{"selector":"setImageDrawable","parameters":[{"type":"android.graphics.drawable.BitmapDrawable"}]}}]}]}}}

3. change_request:请求客户端更改点击事件信息

4. `event_binding_request` : 让客户端去绑定点击事件

		{"type":"event_binding_request","payload":{"events":[{"id":39216,"path":[{"index":0,"view_class":"android.widget.FrameLayout"},{"index":0,"view_class":"android.widget.LinearLayout"},{"index":0,"view_class":"android.widget.FrameLayout"},{"index":0,"mp_id_name":"decor_content_parent"},{"index":0,"mp_id_name":"action_bar_container"},{"index":0,"mp_id_name":"action_bar"},{"index":0,"view_class":"android.support.v7.widget.ActionMenuView"},{"contentDescription":"更多选项","index":0}],"event_name":"testtitle","event_type":"click","device_type":"Android","os_version":"5.1","app_version":"1.0","manufacturer":"Meizu","brand":"Meizu","model":"tracking.androideventbinding","lib_version":"5.4.1","cid":"c44","screenshot_url":null},{"id":39183,"path":[{"index":0,"prefix":"shortest","id":16908290},{"index":0,"view_class":"android.support.constraint.ConstraintLayout"},{"index":0,"mp_id_name":"btn_toast"}],"event_name":"testtoast","event_type":"click","device_type":"Android","os_version":"5.1","app_version":"1.0","manufacturer":"Meizu","brand":"Meizu","model":"tracking.androideventbinding","lib_version":"5.4.1","cid":"c45","screenshot_url":null}]}}


5. clear_request:请求客户端清除点击事件

6. tweak_request:ABTest

正常流程下来.


## 2.2 客户端指令类型

1. `device_info_response`:

2. `snapshot_response`:

	
# 3. 问题

1. 不同控件需要不同的处理逻辑,目前mixpanel 对一些列表控件的支持并不完善

2. 服务端对信息处理的细节  由于没有服务端代码 不了解
