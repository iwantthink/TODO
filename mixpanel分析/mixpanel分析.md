# 1.  事件

## 1.1 获取事件

`GET https://decide.mixpanel.com/decide?version=1&lib=android&token=efecc1169050774fbff2ce156010d8c1&distinct_id=8b956ef9-8488-4b11-84a2-d64234a78f82&properties=%7B%22%24android_lib_version%22%3A%225.4.1%22%2C%22%24android_app_version%22%3A%221.0%22%2C%22%24android_version%22%3A%225.1%22%2C%22%24android_app_release%22%3A1%2C%22%24android_device_model%22%3A%22m3+note%22%7D HTTP/1.1
`

响应内容:

	[
	    {
	        "event_name":"test",
	        "path":[
	            {
	                "index":0,
	                "prefix":"shortest",
	                "id":16908290
	            },
	            {
	                "index":0,
	                "view_class":"android.support.constraint.ConstraintLayout"
	            },
	            {
	                "index":0,
	                "mp_id_name":"btn_test"
	            }
	        ],
	        "target_activity":null,
	        "event_type":"click"
	    }
	]



## 1.2 上报事件


### 1.2.1 上报内容

{"event":"test","properties":{"mp_lib":"android","$lib_version":"5.4.1","$os":"Android","$os_version":"5.1","$manufacturer":"Meizu","$brand":"Meizu","$model":"m3 note","$google_play_services":"invalid","$screen_dpi":480,"$screen_height":1920,"$screen_width":1080,"$app_version":"1.0","$app_version_string":"1.0","$app_release":1,"$app_build_number":1,"$has_nfc":false,"$has_telephone":true,"$carrier":"","$wifi":true,"$bluetooth_enabled":false,"$bluetooth_version":"ble","token":"efecc1169050774fbff2ce156010d8c1","time":1538202380,"distinct_id":"8b956ef9-8488-4b11-84a2-d64234a78f82","$text":"test","$from_binding":true},"$mp_metadata":{"$mp_event_id":"b9a03ee52b6a448c","$mp_session_id":"e9513b3975e9fa96","$mp_session_seq_id":2,"$mp_session_start_sec":1538202305}}

## 1.3 事件如何绑定?
> 在线的事件配置 如何与实际控件发生关联,在控件被点击时判断是否设置有事件?

# 2. 设置事件

1. MixpanelAPI

	mUpdatesFromMixpanel.startUpdates()

2. ViewCrawler

	applyPersistedUpdates();

