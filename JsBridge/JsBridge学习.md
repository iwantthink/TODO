# 1. WebView的使用

[WebView开发1](http://www.jianshu.com/p/3c94ae673e2a)

[WebView开发2](https://jiandanxinli.github.io/2016-08-31.html)

[WebView开发3-Native与Js交互](https://www.jianshu.com/p/345f4d8a5cfa)

## 1.1 交互
Android 与 JS**通过WebView的交互** 主要分为：

1. **Android调用Js代码**
	
	1. 通过`Webview.loadUrl("javascript:functionName()")`。
	2. 通过`WebView.evaluateJavascript()`

	![](https://upload-images.jianshu.io/upload_images/944365-30f095d4c9e638fd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

2. **Js调用Android代码**

	1. 通过	`WebView.addJavascriptInterface()`进行对象映射。
	2. 通过`WebViewClient.shouldOverrideUrlLoading()`方法回调拦截url。
	3. 通过WebChromeClient的`onJsAlert()`,`onJsConfirm()`,`onJsPrompt()`方法回调拦截Js对话框`alert()`,`confirm()`,`prompt()`的消息

### 1.1.1 Android调用Js代码

	//HTML代码
	<!DOCTYPE html>
	<html>
	    <head>
	        <title>Page Title</title>
	    </head>
	
	    <body>
	
	        <button type="button" onclick="alert('Welcome')">Js->Java 无参数无返回</button>
	
	    </body>
	
	    <script>
	
	        function java2Js1(){
	            alert("Java->Js 无参 无返回");
	        }
	
	        function java2Js2(message){
	            alert("Java->Js 有参 无返回 msg = "+message);
	        }
	
	        function java2Js3(){
	            return "Java->Js 无参 有返回";
	        }
	    </script>
	</html>

	//Android端代码
        switch (v.getId()) {
            case R.id.btn_test1:
                mWebView.loadUrl("file:///android_asset/sample.html");
                break;
            case R.id.btn_test2:
                //无参数 无返回
                mWebView.loadUrl("javascript:java2Js1()");
                break;
            case R.id.btn_test3:
                //有参数 无返回
                mWebView.loadUrl("javascript:java2Js2(\"from native\")");
                break;
            case R.id.btn_test4:
                mWebView.evaluateJavascript("javascript:java2Js3()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Toast.makeText(SeconedActivity.this, value, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }

### 1.1.2 Js调用Android代码

**方式1：**

        mWebView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void hello() {
                Toast.makeText(SeconedActivity.this, "i am called by js", Toast.LENGTH_SHORT).show();
            }

            @JavascriptInterface
            public void hello2(String msg) {
                Toast.makeText(SeconedActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @JavascriptInterface
            public void hello3() {
				//返回值需要Android 端调用Js 来返回
                mWebView.loadUrl("javascript:functionName()");
            }

        }, "test_name");

       <button type="button" onclick="js2java1()">Js->Java 无参数无返回</button>
        <button type="button" onclick="js2java2('Js->Java 有参数 无返回')">Js->Java 有参数无返回</button>
        <button type="button" onclick="js2java3()">Js->Java 无参数有返回</button>

        function js2java1(){
            test_name.hello();
        }

        function js2java2(message){
            test_name.hello2(message);
        }

        function js2java3(){
			test_name.hello3();
        }

**方式2：**

	//步骤1：
    <button type="button" id="button1" onclick="callAndroid()">点击调用Android代码</button>

	<script>
         function callAndroid(){
            /*约定的url协议为：js://webview?arg1=111&arg2=222*/
            document.location = "js://webview?arg1=111&arg2=222";
         }
      </script>

  	mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String str) {

                // 步骤2：根据协议的参数，判断是否是所需要的url
                // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                //假定传入进来的 url = "js://webview?arg1=111&arg2=222"（同时也是约定好的需要拦截的）
                Uri uri = Uri.parse(str);
                Log.d("SeconedActivity", uri.toString());
                // 如果url的协议 = 预先约定的 js 协议
                // 就解析往下解析参数
                if (uri.getScheme().equals("js")) {

                    // 如果 authority  = 预先约定协议里的 webview，即代表都符合约定的协议
                    // 所以拦截url,下面JS开始调用Android需要的方法
                    if (uri.getAuthority().equals("webview")) {

                        //  步骤3：
                        // 执行JS所需要调用的逻辑
                        Toast.makeText(SeconedActivity.this, "js调用了Android的方法", Toast.LENGTH_SHORT).show();
                        // 可以在协议上带有参数并传递到Android上
                        HashMap<String, String> params = new HashMap<>();
                        Set<String> collection = uri.getQueryParameterNames();

                    }

                    return true;
                }
                return super.shouldOverrideUrlLoading(view, str);
            }
        });

**方式3：**

![](https://upload-images.jianshu.io/upload_images/944365-1385f748618af886.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

    <button type="button" id="button1" onclick="clickprompt()">点击调用Android代码</button>

	<script>
        
    	function clickprompt(){
    	// 调用prompt（）
    	var result=prompt("js://demo?arg1=111&arg2=222");
    	alert("demo " + result);
		}

    </script>

    mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
				//具体流程与方式2相似
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
        });

![](https://upload-images.jianshu.io/upload_images/944365-8c91481325a5253e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

# 2.JsBridge
[JSBridge的原理](http://www.cnblogs.com/dailc/p/5931324.html)

[URI-统一资源标识符 wiki](https://zh.wikipedia.org/wiki/%E7%BB%9F%E4%B8%80%E8%B5%84%E6%BA%90%E6%A0%87%E5%BF%97%E7%AC%A6)

[URL-统一资源定位符](https://zh.wikipedia.org/wiki/%E7%BB%9F%E4%B8%80%E8%B5%84%E6%BA%90%E5%AE%9A%E4%BD%8D%E7%AC%A6)
## 2.1 介绍

JSBridge是一座用JavaScript搭建起来的桥，一端是web，一端是native。我们搭建这座桥的目的也很简单，让native可以调用web的js代码，让web可以 “调用” 原生的代码。

**JSBridge是Native代码与JS代码的通信桥梁。目前的一种统一方案是:H5触发 自定义的url ->Native捕获自定义的url ->原生分析,执行->原生调用h5。如下图**

![](https://dailc.github.io/staticResource/blog/hybrid/jsbridge/img_hybrid_base_jsbridgePrinciple_1.png)

## 2.2 自定义URL

统一资源定位符的标准格式如下：

	协议类型:[//服务器地址[:端口号]][/资源层级UNIX文件路径]文件名[?查询][#片段ID]

统一资源定位符的完整格式如下：

	协议类型:[//[访问资源需要的凭证信息@]服务器地址[:端口号]][/资源层级UNIX文件路径]文件名[?查询][#片段ID]

其中【访问凭证信息@；:端口号；?查询；#片段ID】都属于选填项。

在JsBridge中，使用自定义的URL-scheme(即协议类型，URL中`://`前面的就是) 组成一条URL ，这个scheme不需要注册(在Android中scheme需要注册才能使用，实现跳转App)，而是由前端通过某种方式触发，然后Native通过某种方法捕获对应的URL，然后拿到前端触发的URL，根据定义好的协议，分析当前触发了哪种方法，然后根据定义来执行。

## 2.3 实现流程

1. 设计出一个Native与JS交互的全局桥对象
	
	规定JS和Native之间的通信必须通过一个H5全局对象JSBridge来实现

	- 这个对象有如下特点：

		- 名称为`JSBridge`,是H5页面中全局对象window的一个属性
 		`var JSBridge = window.JsBridge||(window.JSBridge={})`

	- 该对象有如下方法

		- `registerHandler( String,Function )`H5调用,** 注册本地JS方法**,注册后Native可通过JSBridge调用。调用后会将方法注册到本地变量messageHandlers 中

		- `callHandler( String,JSON,Function )`H5调用, **调用原生开放的api**,调用后实际上还是本地通过url scheme触发。调用时会将回调id存放到本地变量responseCallbacks中

		- `_handleMessageFromNative( JSON )`Native调用, **原生调用H5页面注册的方法**,**或者通知H5页面执行回调方法**

2. JS如何调用Native

	在第一步中，定义好了全局桥对象，通过`callHandler()`方法来调用Native方法，它内部的流程是：

	1. 判断是否有回调函数,如果有,生成一个回调函数id,并将id和对应回调添加进入回调函数集合responseCallbacks中

	2. 通过特定的参数转换方法,将传入的数据,方法名一起,拼接成一个url scheme

			//url scheme的格式如2.2
			//基本有用信息就是后面的callbackId,handlerName与data
			//原生捕获到这个scheme后会进行分析
			var uri = CUSTOM_PROTOCOL_SCHEME://API_Name:callbackId/handlerName?data
					
	3. 使用内部早就创建好的一个隐藏iframe来触发scheme

			//创建隐藏iframe过程
			var messagingIframe = document.createElement('iframe');
			messagingIframe.style.display = 'none';
			document.documentElement.appendChild(messagingIframe);
			
			//触发scheme
			messagingIframe.src = uri;

		>注意,正常来说是可以通过window.location.href达到发起网络请求的效果的，但是有一个很严重的问题，就是如果我们连续多次修改window.location.href的值，在Native层只能接收到最后一次请求，前面的请求都会被忽略掉。所以JS端发起网络请求的时候，需要使用iframe，这样就可以避免这个问题。

3. Native如何得知api被调用

	在第二步中，已经成功实现在H5页面调用Native方法，那么Native如何知道H5调用了Native并且知道scheme被触发：

	在Android中(WebViewClient里),通过`shouldoverrideurlloading`可以捕获到url scheme的触发

		public boolean shouldOverrideUrlLoading(WebView view, String url){
			//读取到url后自行进行分析处理
			
			//如果返回false，则WebView处理链接url，如果返回true，代表WebView根据程序来执行url
			return true;
		}

	> 另外,Android中也可以不通过iframe.src来触发scheme,android中可以通过window.prompt(uri, "");来触发scheme,然后Native中通过重写WebViewClient的onJsPrompt来获取uri

4. 分析url-参数和回调的格式

	第三步中，Native已经知道了H5传到Native的url scheme,现在需要分析这个url 去了解 H5到底要调用Native的什么方法：

	1. 根据api名,在本地找寻对应的api方法,并且记录该方法执行完后的回调函数id

	2. 根据提取出来的参数,根据定义好的参数进行转化
如果是JSON格式需要手动转换,如果是String格式直接可以使用

	3. 原生本地执行对应的api功能方法

	4. 功能执行完毕后,找到这次api调用对应的回调函数id,然后连同需要传递的参数信息,组装成一个JSON格式的参数
	回调的JSON格式为:{responseId:回调id,responseData:回调数据}

		- responseId String型 H5页面中对应需要执行的回调函数的id,在H5中生成url scheme时就已经产生

		- responseData JSON型 Native需要传递给H5的回调数据,是一个JSON格式: {code:(整型,调用是否成功,1成功,0失败),result:具体需要传递的结果信息,可以为任意类型,msg:一些其它信息,如调用错误时的错误信息}
		
	5. 通过JSBridge通知H5页面回调

5. Native如何调用JS

	到了这一步,就该Native通过JSBridge调用H5的JS方法或者通知H5进行回调了,具体如下

		//将回调信息传给H5
		JSBridge._handleMessageFromNative(messageJSON);	

	如上,实际上是通过JSBridge的_handleMessageFromNative传递数据给H5,其中的messageJSON数据格式根据两种不同的类型,有所区别,如下

	**Native通知H5页面进行回调**
	数据格式为: Native通知H5回调的JSON格式

	**Native主动调用H5方法**
	数据格式是:`{handlerName:api名,data:数据,callbackId:回调id}`

	- handlerName String型 需要调用的,h5中开放的api的名称

	- data JSON型 需要传递的数据,固定为JSON格式(因为我们固定H5中注册的方法接收的第一个参数必须是JSON,第二个是回调函数)

	- callbackId String型 原生生成的回调函数id,h5执行完毕后通过url scheme通知原生api成功执行,并传递参数

	> 注意,这一步中,如果Native调用的api是h5没有注册的,h5页面上会有对应的错误提示。

	另外,H5调用Native时,Native处理完毕后一定要及时通知H5进行回调,要不然这个回调函数不会自动销毁,多了后会引发内存泄漏。

6. H5中api方法的注册以及格式

	前面有提到Native主动调用H5中注册的api方法,那么h5中怎么注册供原生调用的api方法呢？格式又是什么呢?如下

		//注册一个测试函数
		JSBridge.registerHandler('testH5Func',function(data,callback){
			alert('测试函数接收到数据:'+JSON.stringify(data));
			callback&&callback('测试回传数据...');
		});				
			
	如上述代码为注册一个供原生调用的api

	**H5中注册的API格式注意**

	如上代码,注册的api参数是`(data,callback)`

	其中第一个data即原生传过来的数据,第二个callback是内部封装过一次的,执行callback后会触发url scheme,通知原生获取回调信息 