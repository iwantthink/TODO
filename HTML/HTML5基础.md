# HTML5基础

[HTML5 简介](http://www.w3school.com.cn/html/html5_intro.asp)


# 1. 简介

## 1.1 什么是HTML5?

HTML5 是最新的 HTML 标准。

HTML5 是专门为承载丰富的 web 内容而设计的，并且无需额外插件。

HTML5 拥有新的语义、图形以及多媒体元素。

HTML5 提供的新元素和新的 API 简化了 web 应用程序的搭建。

HTML5 是跨平台的，被设计为在不同类型的硬件（PC、平板、手机、电视机等等）之上运行。


## 1.2 新特性

- 新的语义元素，比如 `<header>, <footer>, <article>, and <section>`。

- 新的表单控件，比如数字、日期、时间、日历和滑块。

- 强大的图像支持（借由 `<canvas>` 和 `<svg>`）

- 强大的多媒体支持（借由 `<video>` 和 `<audio>`）

- 强大的新 API，比如用本地存储取代 cookie。

## 1.3 被删除的内容

以下 HTML 4.01 元素已从 HTML5 中删除：

- `<acronym>`
- `<applet>`
- `<basefont>`
- `<big>`
- `<center>`
- `<dir>`
- `<font>`
- `<frame>`
- `<frameset>`
- `<noframes>`
- `<strike>`
- `<tt>`


# 2. HTML5 中的新内容

## 2.1 文档声明

	<!DOCTYPE html>

- HTML5 中默认的字符编码是 UTF-8


## 2.2 新的属性语法

HTML5 标准允许 4 中不同的属性语法。


本例演示在` <input>` 标签中使用的不同语法：


类型	|示例
---|---
Empty	|`<input type="text" value="John Doe" disabled>`
Unquoted|	`<input type="text" value=John Doe>`
Double-quoted|	`<input type="text" value="John Doe">`
Single-quoted|	`<input type="text" value='John Doe'>`


- 在 HTML5 标准中，根据对属性的需求，可能会用到所有 4 种语法。


# 3 HTML5 浏览器支持

所有现代浏览器都支持 HTML5。

此外，所有浏览器，不论新旧，都会自动把未识别元素当做**行内元素**来处理。

正因如此，您可以帮助老式浏览器处理”未知的“ HTML 元素。


## 3.1 把HTML5 元素定义为块级元素

HTML5 定义了八个新的语义 HTML 元素,**所有都是块级元素**。

- 您可以把 CSS **display** 属性设置为 **block**，以确保老式浏览器中正确的行为：


	header, section, footer, aside, nav, main, article, figure {
	    display: block; 
	}


## 3.2 向HTML添加新元素

可以通过浏览器 trick 向 HTML 添加任何新元素：

本例向 HTML 添加了一个名为 <myHero> 的新元素，并为其定义 display 样式：
	
	<html>
	
		<head>
		  <title>Styling the article element</title>
		  <script>document.createElement("myHero")</script>
		  <style>
		      myHero {
		        display:block;
		        background-color:#ddd;
		        padding: 50px;
		        font-size: 30px;
		      }  
		  </style>
		</head>
	
		<body>
		
			<h1>My First Heading</h1>
			
			<p>My first paragraph.</p>
		
			<myHero>My First Hero</myHero>
		
		</body>
	</html>


- 已添加的 JavaScript 语句 `document.createElement("myHero")`，仅适用于 IE。



### 3.2.1 Internet Explorer 的问题


上述方案可用于所有新的 HTML5 元素，但是Internet Explorer 8 以及更早的版本，不允许对未知元素添加样式。

幸运的是，Sjoerd Visscher 创造了 "HTML5 Enabling JavaScript", "the shiv"：

	<!--[if lt IE 9]>
	  <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
	<![endif]-->

- 以上代码是一段注释，但是 IE9 的早期版本会读取它（并理解它）。


	<!DOCTYPE html>
	<html>
	
		<head>
		  <title>Styling HTML5</title>
		  <!--[if lt IE 9]>
		  <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js">
		  </script>
		  <![endif]-->
		</head>
	
		<body>
		
			<h1>My First Article</h1>
			
			<article>
				London is the capital city of England. It is the most populous city in the United Kingdom, with a metropolitan area of over 13 million inhabitants.
			</article>
		
		</body>
	</html>

- 引用 shiv 代码的链接必须位于 `<head>` 元素中，因为 Internet Explorer 需要在读取之前认识所有新元素。


# 4. HTML5 新元素

## 4.1 新的语义/结构元素


标签	|描述
---|---
`<article>`|	定义文档内的文章。
`<aside>`|	定义页面内容之外的内容。
`<bdi>`|	定义与其他文本不同的文本方向。
`<details>`|	定义用户可查看或隐藏的额外细节。
`<dialog>`|	定义对话框或窗口。
`<figcaption>`|	定义 `<figure>` 元素的标题。
`<figure>`|	定义自包含内容，比如图示、图表、照片、代码清单等等。
`<footer>`|	定义文档或节的页脚。
`<header>`|	定义文档或节的页眉。
`<main>`|	定义文档的主内容。
`<mark>`|	定义重要或强调的内容。
`<menuitem>`|	定义用户能够从弹出菜单调用的命令/菜单项目。
`<meter>`|	定义已知范围（尺度）内的标量测量。
`<nav>`|	定义文档内的导航链接。
`<progress>`|	定义任务进度。
`<rp>`|	定义在不支持 ruby 注释的浏览器中显示什么。
`<rt>`|	定义关于字符的解释/发音（用于东亚字体）。
`<ruby>`|	定义 ruby 注释（用于东亚字体）。
`<section>`|	定义文档中的节。
`<summary>`|	定义 `<details>` 元素的可见标题。
`<time>`|	定义日期/时间。
`<wbr>`|定义可能的折行（line-break）。



## 4.2 新的表单元素

标签|	描述
---|---
`<datalist>`|	定义输入控件的预定义选项。
`<keygen>`|	定义键对生成器字段（用于表单）。
`<output>`|	定义计算结果。


## 4.3 新的输入类型

新的输入类型	|新的输入属性
---|---
color<br/>date<br/>datetime<br/>datetime-local<br/>email<br/>month<br/>number<br/>range<br/>search<br/>tel<br/>time<br/>url<br/>week|autocomplete<br/>autofocus<br/>form<br/>formaction<br/>formenctype<br/>formmethod<br/>formnovalidate<br/>formtarget<br/>height 和 width<br/>list<br/>min 和 max<br/>multiple<br/>pattern (regexp)<br/>placeholder<br/>required<br/>step



## 4.4 图像


标签	|描述
---|---
`<canvas>`|	定义使用 JavaScript 的图像绘制。
`<svg>`|	定义使用 SVG 的图像绘制。


## 4.5 新的媒介元素

标签	|描述
---|---
`<audio>`|	定义声音或音乐内容。
`<embed>`|	定义外部应用程序的容器（比如插件）。
`<source>`|	定义 `<video>` 和` <audio>` 的来源。
`<track>`|	定义 `<video>` 和` <audio>`的轨道。
`<video>`|	定义视频或影片内容。



# 5. 语义元素

**语义元素是拥有语义的元素**

- 语义学（源自古希腊）可定义为对语言意义的研究。


## 5.1 什么是语义元素?

语义元素清楚地向浏览器和开发者描述其意义。

- 非语义元素的例子：`<div>` 和 `<span>` - 无法提供关于其内容的信息。

- 语义元素的例子：`<form>`、`<table>` 以及 `<img>` - 清晰地定义其内容。

## 5.2 浏览器支持

所有现代浏览器均支持 HTML5 语义元素。

此外，您可以“帮助”老式浏览器处理“未知元素”


## 5.3 为何使用 HTML5 元素？

如果使用 HTML4 的话，开发者会使用他们喜爱的属性名来设置页面元素的样式：

	
- header, top, bottom, footer, menu, navigation, main, container, content, article, sidebar, topnav, ...

如此，浏览器便无法识别正确的网页内容。

而通过 HTML5 元素，比如：

- `<header> <footer> <nav> <section> <article>`，此问题迎刃而解。

根据 W3C，语义网：

- “允许跨应用程序、企业和团体对数据进行分享和重用。”

## 5.4 嵌套语义元素 


在 HTML5 标准中，`<article>` 元素定义完整的相关元素自包含块。

`<section>` 元素被定义为相关元素块。

我们能够使用该定义来决定如何嵌套元素吗？不，我们不能！

- 在因特网上，您会发现 `<section>` 元素包含 `<article>` 元素的 HTML 页面，还有 `<article>` 元素包含 `<sections>` 元素的页面。

- 您还会发现 `<section>` 元素包含 `<section>` 元素，同时 `<article>` 元素包含 `<article>` 元素。



## 5.5 HTML5语义元素介绍

### 5.5.1 `<section>`元素

定义文档中的节。

根据 W3C 的 HTML 文献：

- 节（section）是有主题的内容组，通常具有标题。

可以将网站首页划分为简介、内容、联系信息等节。


	<section>
	   <h1>WWF</h1>
	   <p>The World Wide Fund for Nature (WWF) is....</p>
	</section> 

### 5.5.2 `<article>`元素

定独立的自包含内容。

- 文档有其自身的意义，并且可以独立于网站其他内容进行阅读。

`<article>` 元素的应用场景：

- 论坛

- 博客

- 新闻


	<article>
	   <h1>What Does WWF Do?</h1>
	   <p>WWF's mission is to stop the degradation of our planet's natural environment,
	  and build a future in which humans live in harmony with nature.</p>
	</article> 

### 5.5.3 `<header>`元素


`<header>` 元素为文档或节规定页眉。

`<header>` 元素应该被用作介绍性内容的容器。

一个文档中可以有多个 `<header>` 元素。


实例(定义了页眉)

	<article>
	   <header>
	     <h1>What Does WWF Do?</h1>
	     <p>WWF's mission:</p>
	   </header>
	   <p>WWF's mission is to stop the degradation of our planet's natural environment,
	  and build a future in which humans live in harmony with nature.</p>
	</article> 


### 5.5.4 `<footer>`元素

`<footer>` 元素为文档或节规定页脚。

`<footer>` 元素应该提供有关其包含元素的信息。

页脚通常包含文档的作者、版权信息、使用条款链接、联系信息等等。

您可以在一个文档中使用多个` <footer> `元素。

实例:


	<footer>
	   <p>Posted by: Hege Refsnes</p>
	   <p>Contact information: <a href="mailto:someone@example.com">
	  someone@example.com</a>.</p>
	</footer> 

### 5.5.5 `<nav> `元素

`<nav>` 元素定义导航链接集合。

`<nav>` 元素旨在定义大型的导航链接块。不过，并非文档中所有链接都应该位于 `<nav>` 元素中！

实例:


	<nav>
		<a href="/html/">HTML</a> |
		<a href="/css/">CSS</a> |
		<a href="/js/">JavaScript</a> |
		<a href="/jquery/">jQuery</a>
	</nav> 


### 5.5.6 `<aside>` 元素

`<aside>` 元素页面主内容之外的某些内容（比如侧栏）。

aside 内容应该与周围内容相关。

	<p>My family and I visited The Epcot center this summer.</p>
	
	<aside>
	   <h4>Epcot Center</h4>
	   <p>The Epcot Center is a theme park in Disney World, Florida.</p>
	</aside> 


### 5.5.7 `<figure> & <figcaption> `元素


在书籍和报纸中，与图片搭配的标题很常见。

标题（caption）的作用是为图片添加可见的解释。

通过 HTML5，图片和标题能够被组合在 `<figure>` 元素中：

实例

	<figure>
	   <img src="pic_mountain.jpg" alt="The Pulpit Rock" width="304" height="228">
	   <figcaption>Fig1. - The Pulpit Pock, Norway.</figcaption>
	</figure> 

- `<img>` 元素定义图像，`<figcaption>` 元素定义标题。



# 6. HTML4 迁移至HTML5

[W3School 迁移指南](http://www.w3school.com.cn/html/html5_migration.asp)