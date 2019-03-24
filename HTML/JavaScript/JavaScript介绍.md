# JavaScript教程

[阮一峰的JavaScript教程](https://wangdoc.com/javascript/index.html)

[W3School -Js教程](http://www.w3school.com.cn/js/js_intro.asp)

[MDN Web教程-JavaScript](https://developer.mozilla.org/zh-CN/docs/Learn/JavaScript/First_steps/What_is_JavaScript)

[浏览器环境概述-阮一峰](https://wangdoc.com/javascript/bom/engine.html)

# 1. 什么是JavaScript语言?

JavaScript 是一种**轻量级的脚本语言(动态编程语言)**,应用于HTML文档时,为网站提供动态交互特性.

- 浏览器内置了Javascript作为内置脚本语言

- 所谓“脚本语言”（script language），指的是它不具备开发操作系统的能力，而是只用来编写控制其他大型应用程序（比如浏览器）的“脚本”。

JavaScript 也是一种嵌入式（embedded）语言。

- 它本身提供的核心语法不算很多，只能用来做一些数学和逻辑运算。JavaScript 本身不提供任何与 I/O（输入/输出）相关的 API，都要靠宿主环境（host）提供，所以 JavaScript 只合适嵌入更大型的应用程序环境，去调用宿主环境提供的底层 API。

JavaScript 的核心语法部分相当精简，只包括两个部分：

1. 基本的语法构造（比如操作符、控制结构、语句）
2. 标准库（就是一系列具有各种功能的对象比如Array、Date、Math等）。
3. 除此之外，各种宿主环境提供额外的 API（即只能在该环境使用的接口），以便 JavaScript 调用。

以浏览器为例，它提供的额外 API 可以分成三大类。

1. 浏览器控制类：操作浏览器

2. DOM 类：操作网页的各种元素

3. Web 类：实现互联网的各种功能

## 1.1 JS在页面上做了什么?

浏览器在读取一个网页时，代码（HTML, CSS 和 JavaScript）将在一个运行环境（浏览器标签）中得到执行。就像一间工厂，将原材料（代码）加工为一件产品（网页）

在 HTML 和 CSS 集合组装成一个网页后，浏览器的 JavaScript 引擎将执行 JavaScript 代码。这保证了当 JavaScript 开始运行之前，网页的结构和样式已经就位。

- 因为通过 DOM API动态修改 HTML 和 CSS 来更新 UI 正是 JavaScript 最普遍的用处所在。如果 JavaScript 在 HTML 和 CSS 就位之前加载运行，就会引发错误


## 1.2 浏览器安全

每个**浏览器标签页**就是其自身用来运行代码的独立容器（这些容器用专业术语称为“运行环境”）。

- 大多数情况下，每个标签页中的代码完全独立运行，而且一个标签页中的代码不能直接影响另一个标签页（或者另一个网站）中的代码。这是一个好的安全措施，如果不这样，黑客就可以从其他网站盗取信息，等等。

- 可以用安全的方式在不同网站/标签页中传送代码和数据

## 1.3 JS运行次序

当浏览器执行到一段 JavaScript 代码时，通常会按从上往下的顺序执行这段代码。这意味着需要注意编写代码的顺序。

示例:

	const para = document.querySelector('p');
	
	para.addEventListener('click', updateName);
	
	function updateName() {
	  let name = prompt('输入一个新的名字：');
	  para.textContent = '玩家1：' + name;
	}

- 如果将第一行和第三行的代码交换位置,浏览器会报错(`TypeError:para is undefined`)

	这是一个常见的错误,在引用对象之前必须确保该对象已经存在


## 1.4 解释代码vs 编译代码

- 解释型语言中，代码自上而下运行，且实时返回运行结果。代码在执行前，无需由浏览器将其转化为其他形式。

- 编译型语言代码能够运行之前需要先转化（编译）成另一种形式。比如 C/C++ 先被编译成汇编语言，然后才能由计算机运行。

**JavaScript 是轻量级解释型语言。**


## 1.5 服务器端代码vs客户端代码

- 客户端代码是在用户的电脑上运行的代码，在浏览一个网页时，它的客户端代码就会被下载，然后由浏览器来运行并展示。这就是客户端 JavaScript。

- 服务器端代码在服务器上运行，浏览器将结果下载并展示出来。

	流行的服务器端 web 语言包括：PHP、Python、Ruby、ASP.NET 以及 JavaScript！JavaScript 也可用作服务器端语言，比如现在流行的 Node.js 环境


## 1.6 动态代码vs静态代码

- “动态”一词既能描述客户端 JavaScript，又能描述服务器端语言。

	是指通过按需生成新内容来更新 web 页面 / 应用，使得不同环境下显示不同内容。服务器端代码会在服务器上动态生成新内容，例如从数据库中提取信息。而客户端 JavaScript 则在用户端浏览器中动态生成新内容，比如说创建一个新的 HTML 表格，用从服务器请求到的数据填充，然后在网页中向用户展示这个表格。两种情况的意义略有不同，但又有所关联，且两者（服务器端和客户端）经常协同作战。

- 没有动态更新内容的网页叫做“静态”页面，所显示的内容不会改变

# 2. JavaScript与ECMAScript的关系

ECMAScript 和 JavaScript 的关系是，前者是后者的规格，后者是前者的一种实现。在日常场合，这两个词是可以互换的。

ECMAScript 只用来标准化 JavaScript 这种语言的基本语法结构，与部署环境相关的标准都由其他标准规定，比如 DOM 的标准就是由 W3C组织（World Wide Web Consortium）制定的。

- [JS和ECMAScript之间关系的参考文章](https://www.oschina.net/translate/whats-the-difference-between-javascript-and-ecmascript)

# 3. 如何查找并解决JS代码的错误

一般来说，JS代码错误主要分为两种：

- **语法错误**：代码中存在拼写错误，将导致程序完全或部分不能运行，通常你会收到一些出错信息.只要熟悉语言并了解出错信息的含义，你就能够顺利修复它们。

- **逻辑错误**：有些代码语法虽正确，但执行结果和预期相悖，这里便存在着逻辑错误。这意味着程序虽能运行，但会给出错误的结果。由于一般你不会收到来自这些错误的提示，它们通常比语法错误更难修复。


## 3.1 修复语法错误

通过查看 [浏览器开发者工具](https://developer.mozilla.org/zh-CN/docs/Learn/Discover_browser_developer_tools)中的控制台,可以看到语法错误



# 4.  如何向页面添加JavaScript

网页中嵌入 JavaScript 代码，主要有三种方法。

- `<script>`元素直接嵌入代码。

- `<script>`标签加载外部脚本

- 事件属性

- URL 协议

### 4.1 script元素内部嵌入代码

直接在元素`<script>`的内容中编码JS代码

	<script>
	
	  // 在此编写 JavaScript 代码
	
	</script>

- `<script>`标签有一个type属性，用来指定脚本类型。对 JavaScript 脚本来说，type属性可以设为两种值。

	1. `text/javascript`：这是默认值，也是历史上一贯设定的值。**如果你省略type属性，默认就是这个值**。对于老式浏览器，设为这个值比较好。

	2. `application/javascript`：对于较新的浏览器，建议设为这个值。

- **如果type属性的值，浏览器不认识，那么它不会执行其中的代码**

		<script id="mydata" type="x-custom-data">
		  console.log('Hello World');
		</script>

	- 上面的代码，浏览器不会执行，也不会显示它的内容，因为不认识它的type属性。但是，这个`<script>`节点依然存在于 DOM 之中，可以使用`<script>`节点的text属性读出它的内容。


## 4.2 script元素加载外部代码

将JS代码编写到单独的文件中,在元素`<script>`中引用即可

	<script src="script.js" async></script>

- **如果脚本文件使用了非英语字符，还应该注明字符的编码**

		<script charset="utf-8" src="https://www.example.com/script.js"></script>

- **加载外部脚本和直接添加代码块,不能混用**

- script标签允许设置一个integrity属性，写入该外部脚本的 Hash 签名，用来验证脚本的一致性

## 4.3 内联JavaScript处理器

这种方式并不被建议,因为这将使JS污染到HTML,而且效率低下,对于每个需要应用JS的网页元素,都得手动添加属性(比如onclick 和 onmouseover 等)

	<script>
		function createParagraph() {
		  const para = document.createElement('p');
		  para.textContent = '你点击了这个按钮！';
		  document.body.appendChild(para);
		}
	</script>

	<button onclick="createParagraph()">点我呀</button>

**建议使用纯 JavaScript 结构来通过一个指令选取所有按钮**

	const buttons = document.querySelectorAll('button');
	
	for(let i = 0; i < buttons.length ; i++) {
	  buttons[i].addEventListener('click', createParagraph);
	}

## 4.4 URL协议

URL 支持`javascript:`协议

- **即在 URL 的位置写入代码，使用这个 URL 的时候就会执行 JavaScript 代码**

		<a href="javascript:console.log('Hello')">点击</a>

- 浏览器的地址栏也可以执行`javascript:`协议。将`javascript:console.log('Hello')`放入地址栏，按回车键也会执行这段代码。

- 如果 JavaScript 代码返回一个字符串，浏览器就会新建一个文档，展示这个字符串的内容，原有文档的内容都会消失。

		<a href="javascript: new Date().toLocaleTimeString();">点击</a>

- 如果返回的不是字符串，那么浏览器不会新建文档，也不会跳转。

- **为了防止书签替换掉当前文档，可以在脚本前加上void，或者在脚本最后加上void 0**

		<a href="javascript: void new Date().toLocaleTimeString();">点击</a>
		<a href="javascript: new Date().toLocaleTimeString();void 0;">点击</a>

# 5. 脚本调用策略

## 5.1 script元素工作原理

浏览器加载 JavaScript 脚本，主要通过`<script>`元素完成。正常的网页加载流程是这样的。

1. 浏览器一边下载 HTML 网页，一边开始解析。也就是说，不等到下载完，就开始解析。

2. 解析过程中，浏览器发现`<script>`元素，就暂停解析，把网页渲染的控制权转交给 JavaScript 引擎。

3. 如果`<script>`元素引用了外部脚本，就下载该脚本再执行，否则就直接执行代码。

4. JavaScript 引擎执行完毕，控制权交还渲染引擎，恢复往下解析 HTML 网页。

**加载外部脚本时，浏览器会暂停页面渲染，等待脚本下载并执行完成后，再继续渲染**

- 原因是 JavaScript 代码可以修改 DOM，所以必须把控制权让给它，否则会导致复杂的线程竞赛的问题。

	如果外部脚本加载时间很长（一直无法完成下载），那么浏览器就会一直等待脚本下载完成，造成网页长时间失去响应，浏览器就会呈现“假死”状态，这被称为“阻塞效应”。

	**为了避免这种情况，较好的做法是将`<script>`标签都放在页面底部，而不是头部**

	- 这样即使遇到脚本失去响应，网页主体的渲染也已经完成了，用户至少可以看到内容，而不是面对一张空白的页面。如果某些脚本代码非常重要，一定要放在页面头部的话，最好直接将代码写入页面，而不是连接外部脚本文件，这样能缩短加载时间

	脚本文件都放在网页尾部加载，还有一个好处。因为在 DOM 结构生成之前就调用 DOM 节点，JavaScript 会报错，如果脚本都在网页尾部加载，就不存在这个问题，因为这时 DOM 肯定已经生成了。

**多个script标签会按照代码出现的顺序进行执行(但是会同时并行下载):**

	<script src="a.js"></script>
	<script src="b.js"></script>

- **浏览器会同时并行下载`a.js`和`b.js`，但是，执行时会保证先执行a.js，然后再执行b.js，即使后者先下载完成，也是如此**。

	也就是说，**脚本的执行顺序由它们在页面中的出现顺序决定，这是为了保证脚本之间的依赖关系不受到破坏**。当然，加载这两个脚本都会产生“阻塞效应”，必须等到它们都加载完成，浏览器才会继续页面渲染。


解析和执行 CSS，也会产生阻塞。Firefox 浏览器会等到脚本前面的所有样式表，都下载并解析完，再执行脚本；Webkit则是一旦发现脚本引用了样式，就会暂停执行脚本，等到样式表下载并解析完，再恢复执行。

此外，对于来自同一个域名的资源，比如脚本文件、样式表文件、图片文件等，浏览器一般有限制，同时最多下载6～20个资源，即最多同时打开的 TCP 连接有限制，这是为了防止对服务器造成太大压力。如果是来自不同域名的资源，就没有这个限制。所以，通常把静态文件放在不同的域名之下，以加快下载速度。


### 1.8.1 事件监听器解决
通过使用事件监听器,监听浏览器的 "DOMContentLoaded" 事件，即 HTML 文档体加载、解释完毕事件。可以解决问题

	document.addEventListener("DOMContentLoaded", function() {
	   // 事件发生时的回调逻辑
	});


### 1.8.2 async解决

通过使用JavaScript 的一项现代技术（async “异步”属性）也可以解决该问题,它告知浏览器在遇到 `<script>` 元素时不要中断后续 HTML 内容的加载。

	<script src="script.js" async></script>

- 上述情况下，脚本和 HTML 将一并加载，代码将顺利运行。

- async 只能用于外部脚本

#### 1.8.2.1 async 和 defer介绍

浏览器遇到 `async` 脚本时不会阻塞页面渲染，而是直接下载然后运行。

- 这样脚本的运行次序就无法控制，只是脚本不会阻止剩余页面的显示。

	当页面的脚本之间彼此独立，且不依赖于本页面的其它任何脚本时，async 是最理想的选择。

比如，如果你的页面要加载以下三个脚本：

	<script async src="js/vendor/jquery.js"></script>
	
	<script async src="js/script2.js"></script>
	
	<script async src="js/script3.js"></script>

- 三者的调用顺序是不确定的。`jquery.js` 可能在 `script2.js` 和 `script3.js` 之前或之后调用，如果这样，后两个脚本中依赖 jquery 的函数将产生错误，因为脚本运行时 jquery 尚未加载。

解决这一问题可使用 `defer` 属性，脚本将按照在页面中出现的顺序加载和运行：

	<script defer src="js/vendor/jquery.js"></script>
	
	<script defer src="js/script2.js"></script>
	
	<script defer src="js/script3.js"></script>

- 添加 `defer` 属性的脚本将按照在页面中出现的顺序加载，因此第二个示例可确保 `jquery.js` 必定加载于 `script2.js` 和 `script3.js` 之前，同时 `script2.js` 必定加载于 `script3.js` 之前。


**总结:**

1. 如果脚本无需等待页面解析，且无依赖独立运行，那么应使用 async。

2. 如果脚本需要等待解析，且依赖于其它脚本，调用这些脚本时应使用 defer，将关联的脚本按所需顺序置于 HTML 中。

### 1.8.3 ~~旧方法~~

把脚本元素放在文档体的底端（`</body> `标签之前，与之相邻），这样脚本就可以在 HTML 解析完毕后加载了。

- 此方案（以及上述的 DOMContentLoaded 方案）的问题是：只有在所有 HTML DOM 加载完成后才开始脚本的加载/解析过程。对于有大量 JavaScript 代码的大型网站，可能会带来显著的性能损耗。**这也是 async 属性诞生的初衷**