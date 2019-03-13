# JavaScript教程

[阮一峰的JavaScript教程](https://wangdoc.com/javascript/index.html)

[W3School -Js教程](http://www.w3school.com.cn/js/js_intro.asp)

[MDN Web教程-JavaScript](https://developer.mozilla.org/zh-CN/docs/Learn/JavaScript/First_steps/What_is_JavaScript)

# 1. 什么是JavaScript语言?
JavaScript 是一种轻量级的脚本语言(动态编程语言),应用于HTML文档时,为网站提供动态交互特性.

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

## 1.7 如何向页面添加JavaScript

将元素`<script>`添加到 `<head>`或`<body>`标签中即可实现向页面添加JavaScript

元素`<script>`有几种不同的表现形式

### 1.7.1 内部JavaScript

直接在元素`<script>`的内容中编码JS代码

	<script>
	
	  // 在此编写 JavaScript 代码
	
	</script>


### 1.7.2 外部JavaScript

将JS代码编写到单独的文件中,在元素`<script>`中引用即可

	<script src="script.js" async></script>

### 1.7.3 内联JavaScript处理器

这种方式并不被建议,因为这将使JS污染到HTML,而且效率低下,对于每个需要应用JS的按钮,都得手动添加属性.

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


## 1.8 脚本调用策略

让脚本调用的时机符合预期,会遇到一系列的问题

例如: 

HTML 元素是按其在页面中出现的次序调用的(解释型语言)，如果用 JavaScript 来管理页面上的元素（更精确的说法是使用 文档对象模型 DOM），若 JavaScript 加载于欲操作的 HTML 元素之前，则代码将出错。

这个方法会有几种解决办法:


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
