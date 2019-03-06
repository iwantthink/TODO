# HTML学习

[W3School HTML基础教程](http://www.w3school.com.cn/html/html_jianjie.asp)

[HTML简介- 前端九部](https://www.yuque.com/fe9/basic/hw5ara)

[HTML 4.01 快速参考](http://www.w3school.com.cn/html/html_quick.asp)

# 1. HTML简介

## 1.1 什么是HTML?

**HTML 是用来描述网页的一种语言。**

- HTML 指的是超文本标记语言 (`Hyper Text Markup Language`)

- HTML 不是一种编程语言，而是一种**标记语言** (`markup language`)

- 标记语言是一套标记标签 (`markup tag`)

- HTML 使用标记标签来描述网页

## 1.2 HTML标签是什么?

**HTML 标记标签通常被称为 HTML 标签 (HTML tag)。**

- HTML 标签是由尖括号包围的关键词，比如 `<html>`

- HTML 标签通常是成对出现的，比如 `<b>` 和 `</b>`

- 标签对中的第一个标签是开始标签，第二个标签是结束标签

- 开始和结束标签也被称为开放标签和闭合标签

## 1.3 HTML文档是什么?

**HTML 文档 = 网页**

- HTML 文档描述网页

- HTML 文档包含 HTML 标签和纯文本

- HTML 文档也被称为网页

## 1.4 Web浏览器的作用?

Web 浏览器的作用是读取 HTML 文档，并以网页的形式显示出它们。浏览器不会显示 HTML 标签，而是使用标签来解释页面的内容：

**示例:**

	<html>
	<body>
	
	<h1>我的第一个标题</h1>
	
	<p>我的第一个段落。</p>
	
	</body>
	</html>

- `<html> 与 </html>` 之间的文本描述网页

- `<body> 与 </body> `之间的文本是可见的页面内容

- `<h1> 与 </h1> `之间的文本被显示为标题

- `<p> 与 </p> `之间的文本被显示为段落


# 2. HTML元素是什么?

**HTML 元素指的是从开始标签（start tag）到结束标签（end tag）的所有代码。**

开始标签|元素内容|结束标签
---|---|---
`<p>`|This is a Paragraph|`</p>`
`<a href="default.htm" >`|This is a link|</a>
`<br />`|| 


## 2.1 HTML元素语法

1. HTML 元素以开始标签起始

2. HTML 元素以结束标签终止

3. 元素的内容是开始标签与结束标签之间的内容

4. 某些 HTML 元素具有空内容（empty content）

5. 空元素在开始标签中进行关闭（以开始标签的结束而结束）

6. 大多数 HTML 元素可拥有属性


## 2.2 嵌套的HTML元素

**大多数 HTML 元素可以嵌套（可以包含其他 HTML 元素）。**

HTML 文档由嵌套的 HTML 元素构成。

**示例:**

	<html>
		<body>
			<p>This is my first paragraph.</p>
		</body>
	</html>

- `<html>`元素定义了整个HTML文档,其元素内容是另外一个HTML元素(`<body>`元素)

## 2.3 不要忘记结束标签

即使您忘记了使用结束标签，大多数浏览器也会正确地显示 HTML.但不要依赖这种做法。忘记使用结束标签会产生不可预料的结果或错误


	<p>This is a paragraph
	<p>This is a paragraph

## 2.4 空的HTML元素

1. 没有内容的 HTML 元素被称为空元素。**空元素是在开始标签中关闭的**。

2. `<br>` 就是没有关闭标签的空元素（`<br>` 标签定义换行）。

3. 在 XHTML、XML 以及未来版本的 HTML 中，所有元素都必须被关闭。

4. 在开始标签中添加斜杠，比如 `<br />`，是关闭空元素的正确方法，HTML、XHTML 和 XML 都接受这种方式。

5. 即使 `<br>` 在所有浏览器中都是有效的，但使用 `<br />` 其实是更长远的保障。


## 2.5 使用小写标签

1. HTML 标签对大小写不敏感：`<P> `等同于` <p>`。许多网站都使用大写的 HTML 标签。

2. W3School 使用的是小写标签，因为万维网联盟（W3C）在 HTML 4 中推荐使用小写，而在未来 (X)HTML 版本中强制使用小写。


# 3. HTML属性是什么?

**属性为 HTML 元素提供附加信息**

1. HTML 标签可以拥有属性。属性提供了有关 HTML 元素的更多的信息。

2. 属性总是以名称/值对的形式出现，比如：name="value"。

3. 属性总是在 HTML 元素的开始标签中规定。


**示例:**

	<a href="http://www.w3school.com.cn">This is a link</a>

- 链接的地址在 href 属性中被指定

## 3.1 始终为属性值加引号

属性值应该始终被包括在引号内。双引号是最常用的，不过使用单引号也没有问题。

在某些个别的情况下，比如属性值本身就含有双引号，那么必须使用单引号，例如：

	name='Bill "HelloWorld" Gates'

## 3.2 HTML参考手册

[HTML参考手册](http://www.w3school.com.cn/tags/index.asp)


## 3.3 使用较多的属性

属性|值|描述
---|---|---
class	|classname	|规定元素的类名（classname）
id	|id	|规定元素的唯一 id
style	|style_definition	|规定元素的行内样式（inline style）
title	|text	|规定元素的额外信息（可在工具提示中显示）


# 4. HTML常用标签介绍


## 4.1 HTML标题标签

标题（Heading）是通过 `<h1> - <h6>` 等标签进行定义的,属于块级元素!

- `<h1>` 定义最大的标题。`<h6>` 定义最小的标题。

- 浏览器会自动地在标题的前后添加空行

	**默认情况下，HTML 会自动地在块级元素前后添加一个额外的空行，比如段落、标题元素前后**


### 4.1.1 标题的重要性

**请确保将 HTML heading 标签只用于标题。不要仅仅是为了产生粗体或大号的文本而使用标题**。

- 搜索引擎使用标题为您的网页的结构和内容编制索引。

- 因为用户可以通过标题来快速浏览您的网页，所以用标题来呈现文档结构是很重要的。

- 应该将 h1 用作主标题（最重要的），其后是 h2（次重要的），再其次是 h3，以此类推。


## 4.2 HTML 水平线标签

`<hr />` 标签在 HTML 页面中创建水平线。

- **hr 元素可用于分隔内容**

- 使用水平线 (`<hr/>` 标签) 来分隔文章中的小节是一个办法（但并不是唯一的办法）


## 4.3 HTML 段落标签

可以把 HTML 文档分割为若干段落,属于块级元素!

- 段落是通过 `<p>` 标签定义的。

- 浏览器会自动地在段落的前后添加空行。（**`<p>` 是块级元素**）

- 使用空的段落标记 `<p></p> `去插入一个空行是个坏习惯。用 `<br />` 标签代替它！（但是不要用 `<br />` 标签去创建列表)


## 4.4 HTML 折行标签

如果您希望在不产生一个新段落的情况下进行换行（新行），请使用 `<br />` 标签：

	<p>This is<br />a para<br />graph with line breaks</p>

- `<br/>` 也可以被嵌套

- `<br />` 元素是一个空的 HTML 元素。由于关闭标签没有任何意义，因此它没有结束标签。

	**即使` <br> `在所有浏览器中的显示都没有问题，使用` <br />` 也是更长远的保障**



## 4.5. HTML文本格式相关的标签

HTML 可定义很多供格式化输出的元素，比如粗体和斜体字

标签|描述
---|---
`<b>`	|定义粗体文本。
`<big>`|	定义大号字。
`<em>`|	定义着重文字。
`<i>`|	定义斜体字。
`<small>`|	定义小号字。
`<strong>`|	定义加重语气。
`<sub>`|	定义下标字。
`<sup>`|	定义上标字。
`<ins>`|	定义插入字。
`<del>`|	定义删除字。
`<s>`|	不赞成使用。使用 `<del>` 代替。
`<strike>`|	不赞成使用。使用 `<del>` 代替。
`<u>`|	不赞成使用。使用样式（style）代替。


## 4.6 HTML引用相关的标签

标签	|描述
---|---
`<abbr>	`|定义缩写。
`<acronym>`|	定义首字母缩写。
`<address>`|	定义地址。
`<bdo>`|	定义文字方向。
`<blockquote>`|	定义长的引用。
`<q>`|	定义短的引用语。
`<cite>`|	定义引用、引证。
`<dfn>`|	定义一个定义项目。

## 4.7 计算机代码相关的标签

标签|	描述
---|---
`<code>`|	定义计算机代码。
`<kbd>`|	定义键盘码。
`<samp>`|	定义计算机代码样本。
`<tt>`|	定义打字机代码。
`<var>`|	定义变量。
`<pre>`|	定义预格式文本。
`<listing>`|	不赞成使用。使用 `<pre>` 代替。
`<plaintext>`|	不赞成使用。使用 `<pre>` 代替。
`<xmp>`|	不赞成使用。使用 `<pre>` 代替。


## 4.8 HTML 链接标签

HTML 使用超级链接与网络上的另一个文档相连。几乎可以在所有的网页中找到链接。点击链接可以从一张页面跳转到另一张页面


- 超链接可以是一个字，一个词，或者一组词，也可以是一幅图像，您可以点击这些内容来跳转到新的文档或者当前文档中的某个部分。

- 当您把鼠标指针移动到网页中的某个链接上时，箭头会变为一只小手。

- **通过使用 `<a>` 标签在 HTML 中创建链接**。

- 有两种使用 `<a>` 标签的方式：

	1. 通过使用 href 属性 - 创建指向另一个文档的链接

	2. 通过使用 name 属性 - 创建文档内的书签

### 4.8.1 链接语法

	<a href="url">Link text</a>

- href 属性规定链接的目标。

- 开始标签和结束标签之间的文字被作为超级链接来显示。

- **"链接文本" 不必一定是文本。图片或其他 HTML 元素都可以成为链接**


### 4.8.2 target 属性

	//在新窗口打开文档
	<a href="http://www.w3school.com.cn/" target="_blank">Visit W3School!</a>

- 使用 Target 属性，你可以定义被链接的网页文档在何处显示。

	`_blank`表示打开一个新的窗口

### 4.8.3 name 属性


**name 属性规定锚（anchor）的名称**。

- 您可以使用 name 属性创建 HTML 页面中的书签。

- 书签不会以任何特殊方式显示，它对读者是不可见的。

- 当使用命名锚（named anchors）时，我们可以创建直接跳至该命名锚（比如页面中某个小节）的链接，这样使用者就无需不停地滚动页面来寻找他们需要的信息了。

**命名锚的语法:**

	<a name="label">锚（显示在页面上的文本）</a>

- 锚的名称可以是任何你喜欢的名字

- **可以使用 id 属性来替代 name 属性，命名锚同样有效**


**锚的使用示例:**

1. 首先，我们在 HTML 文档中对锚进行命名（创建一个书签）：

		<a name="tips">基本的注意事项 - 有用的提示</a>

2. 然后，我们在同一个文档中创建指向该锚的链接：

		<a href="#tips">有用的提示</a>

	您也可以在其他页面中创建指向该锚的链接：

		<a href="http://www.w3school.com.cn/html/html_links.asp#tips">有用的提示</a>

	- 在上面的代码中，我们将 # 符号和锚名称添加到 URL 的末端，就可以直接链接到 tips 这个命名锚了。


## 4.9 HTML图像标签

在 HTML 中，图像由 `<img>` 标签定义。

- `<img>` 是空标签，意思是说，它只包含属性，**并且没有闭合标签**。

**要在页面上显示图像，你需要使用源属性（src）**。

- src 指 "source"。源属性的值是图像的 URL 地址。

定义图像的语法是：

	<img src="url" />

- URL 指存储图像的位置。如果名为 "boat.gif" 的图像位于 www.w3school.com.cn 的 images 目录中，那么其 URL 为 `http://www.w3school.com.cn/images/boat.gif`


- 浏览器将图像显示在文档中图像标签出现的地方。如果你将图像标签置于两个段落之间，那么浏览器会首先显示第一个段落，然后显示图片，最后显示第二段。


### 4.9.1 替换文本属性(Alt)

alt 属性用来为图像定义一串预备的可替换的文本。替换文本属性的值是用户定义的。

	<img src="boat.gif" alt="Big Boat">

- 在浏览器无法载入图像时，替换文本属性告诉读者她们失去的信息。此时，浏览器将显示这个替代性的文本而不是图像。为页面上的图像都加上替换文本属性是个好习惯，这样有助于更好的显示信息，并且对于那些使用纯文本浏览器的人来说是非常有用的。


## 4.10 HTML表格标签

表格由 `<table>` 标签来定义。

- 每个表格均有若干行（由 `<tr> `标签定义），每行被分割为若干单元格（由 `<td>` 标签定义）。
- 
- 字母 td 指表格数据（table data），即数据单元格的内容。数据单元格可以包含文本、图片、列表、段落、表单、水平线、表格等等。


## 4.11 HTML列表标签

HTML 支持有序、无序和定义列表

### 4.11.1 无序列表

无序列表是一个项目的列表，此列项目使用粗体圆点（典型的小黑圆圈）进行标记。

无序列表始于 `<ul>` 标签。每个列表项始于` <li>`。

	<ul>
		<li>Coffee</li>
		<li>Milk</li>
	</ul>

- 列表项内部可以使用段落、换行符、图片、链接以及其他列表等等。


### 4.11.2 有序列表

有序列表也是一列项目，列表项目使用数字进行标记。

有序列表始于 `<ol>` 标签。每个列表项始于 `<li>` 标签。
	
	<ol>
		<li>Coffee</li>
		<li>Milk</li>
	</ol>

- 列表项内部可以使用段落、换行符、图片、链接以及其他列表等等。


### 4.11.3 定义列表

自定义列表不仅仅是一列项目，而是项目及其注释的组合。

自定义列表以 `<dl>` 标签开始。每个自定义列表项以 `<dt>` 开始。每个自定义列表项的定义以 `<dd>` 开始。

	<dl>
		<dt>Coffee</dt>
		<dd>Black hot drink</dd>
		<dt>Milk</dt>
		<dd>White cold drink</dd>
	</dl>

- 列表项内部可以使用段落、换行符、图片、链接以及其他列表等等。


# 5. HTML 注释

可以将注释插入 HTML 代码中，这样可以提高其可读性，使代码更易被人理解。浏览器会忽略注释，也不会显示它们。

	<!-- 在此处编写注释 -->

- 开始括号之后（左边的括号）需要紧跟一个叹号，结束括号之前（右边的括号）不需要


## 5.1 条件注释

	<!--[if IE 8]>
	    .... some HTML here ....
	<![endif]-->

- 条件注释定义只有 Internet Explorer 执行的 HTML 标签。


# 6. 查看页面源代码

只需要单击右键，然后选择“查看源文件”（IE）或“查看页面源代码”（Firefox），其他浏览器的做法也是类似的。这么做会打开一个包含页面 HTML 代码的窗口

# 7. HTML中的空格

对于 HTML，您无法通过在 HTML 代码中添加额外的空格或换行来改变输出的效果。

当显示页面时，浏览器会移除源代码中多余的空格和空行。所有连续的空格或空行都会被算作一个空格。需要注意的是，HTML 代码中的所有连续的空行（换行）也被显示为一个空格。



# 8. HTML格式

**style 属性用于改变 HTML 元素的样式**


## 8.1 HTML的style属性

style 属性提供了一种改变所有 HTML 元素的样式的通用方法。

样式是 HTML 4 引入的，它是一种新的首选的改变 HTML 元素样式的方式。**通过 HTML 样式，能够通过使用 style 属性直接将样式添加到 HTML 元素，或者间接地在独立的样式表中（CSS 文件）进行定义**。

**背景颜色示例**:

	<html>
		<body style="background-color:yellow">
			<h2 style="background-color:red">This is a heading</h2>
			<p style="background-color:green">This is a paragraph.</p>
		</body>
	</html>

- `background-color` 属性为元素定义了背景颜色


**字体、颜色和尺寸示例:**

	<html>
		<body>
			<h1 style="font-family:verdana">A heading</h1>
			<p style="font-family:arial;color:red;font-size:20px;">A paragraph.</p>
		</body>
	</html>

- font-family、color 以及 font-size 属性分别定义元素中文本的字体系列、颜色和字体尺寸：

**文本对齐示例:**

	<html>
		<body>
			<h1 style="text-align:center">This is a heading</h1>
			<p>The heading above is aligned to the center of this page.</p>
		</body>
	</html>

## 8.2 CSS

**通过使用 HTML4.0，所有的格式化代码均可移出 HTML 文档，然后移入一个独立的样式表**

### 8.2.1 如何使用样式

当浏览器读到一个样式表，它就会按照这个样式表来对文档进行格式化。有以下三种方式来插入样式表：

1. 外部样式表

	**当样式需要被应用到很多页面的时候**，外部样式表将是理想的选择。使用外部样式表，你就可以通过更改一个文件来改变整个站点的外观。

		<head>
			<link rel="stylesheet" type="text/css" href="mystyle.css">
		</head>

2. 内部样式表

	**当单个文件需要特别样式时**，就可以使用内部样式表。你可以在 head 部分通过 `<style>` 标签定义内部样式表。

		<head>
			<style type="text/css">
				body {background-color: red}
				p {margin-left: 20px}
			</style>
		</head>

3. 内联样式

	**当特殊的样式需要应用到个别元素时**，就可以使用内联样式。 使用内联样式的方法是在相关的标签中使用样式属性。样式属性可以包含任何 CSS 属性。以下实例显示出如何改变段落的颜色和左外边距。

		<p style="color: red; margin-left: 20px">
			This is a paragraph
		</p>


# 9. HTML的块级元素和内联元素

大多数 HTML元素 被定义为块级元素或内联元素,可以通过 `<div>` 和 `<span>` 将 HTML 元素组合起来。

- “块级元素”译为 block level element，“内联元素”译为 inline element。


## 9.1 HTML块级元素

**块级元素在浏览器显示时，通常会以新行来开始（和结束）**。

例子：`<h1>, <p>, <ul>, <table>`


## 9.2 HTML内联元素(行内元素)

内联元素在显示时通常不会以新行开始。

例子：`<b>, <td>, <a>, <img>`

## 9.3 HTML `<div>` 元素

**HTML `<div>` 元素是块级元素，它是可用于组合其他 HTML 元素的容器。**

`<div>` 元素没有特定的含义。除此之外，由于它属于块级元素，浏览器会在其前后显示折行。

- 如果与 CSS 一同使用，`<div>` 元素可用于对大的内容块设置样式属性。

- `<div>` 元素的另一个常见的用途是文档布局。它取代了使用表格定义布局的老式方法。使用 `<table>` 元素进行文档布局不是表格的正确用法。`<table>` 元素的作用是显示表格化的数据。


## 9.4 HTML `<span>` 元素

**HTML `<span>` 元素是内联元素，可用作文本的容器。**

- `<span>` 元素也没有特定的含义。

- **当与 CSS 一同使用时，`<span>` 元素可用于为部分文本设置样式属性。**

# 10. HTML 类

对 HTML 进行分类（设置类），使我们能够为元素的类定义 CSS 样式。

为相同的类设置相同的样式，或者为不同的类设置不同的样式。


	<!DOCTYPE html>
	<html>
	  
	  <head>
	    <style>.cities { background-color:black; color:white; margin:20px; padding:20px; }</style></head>
	  
	  <body>
	    <div class="cities">
	      <h2>London</h2>
	      	<p>London is the capital city of England. It is the most populous city in the United Kingdom, with a metropolitan area of over 13 million inhabitants.</p>
	    </div>
	  </body>
	
	</html>

## 10.1 分类块级元素

HTML `<div>` 元素是块级元素,**能够用作其他 HTML 元素的容器**。

设置 `<div>` 元素的类，使我们能够为相同的` <div> `元素设置相同的类：


	<!DOCTYPE html>
	<html>
	  
	  <head>
	    <style>.cities { background-color:black; color:white; margin:20px; padding:20px; }</style></head>
	  
	  <body>
	    <div class="cities">
	      <h2>London</h2>
	      <p>London is the capital city of England. It is the most populous city in the United Kingdom, with a metropolitan area of over 13 million inhabitants.</p>
	    </div>
	    <div class="cities">
	      <h2>Paris</h2>
	      <p>Paris is the capital and most populous city of France.</p>
	    </div>
	    <div class="cities">
	      <h2>Tokyo</h2>
	      <p>Tokyo is the capital of Japan, the center of the Greater Tokyo Area, and the most populous metropolitan area in the world.</p>
	    </div>
	  </body>
	
	</html>

## 10.2 分类行内元素

HTML `<span>` 元素是行内元素，**能够用作文本的容器**。

设置` <span> `元素的类，能够为相同的 `<span>` 元素设置相同的样式。

	<!DOCTYPE html>
	<html>
	  <head>
	    <style>span.red {color:red;}</style>
	  </head>

	  <body>
	    <h1>My
	      <span class="red">Important</span>Heading</h1></body>
	</html>

# 11. HTML布局

网站常常以多列显示内容（就像杂志和报纸）。



## 11.1 使用` <div>` 元素的 HTML 布局

`<div>` 元素常用作布局工具，因为能够轻松地通过 CSS 对其进行定位。

这个例子使用了四个 `<div>` 元素来创建多列布局：


	<!DOCTYPE html>
	<html>
	  
	  <head>
	    <style>#header { background-color:black; color:white; text-align:center; padding:5px; } #nav { line-height:30px; background-color:#eeeeee; height:300px; width:100px; float:left; padding:5px; } #section { width:350px; float:left; padding:10px; } #footer { background-color:black; color:white; clear:both; text-align:center; padding:5px; }</style></head>
	  
	  <body>
	    <div id="header">
	      <h1>City Gallery</h1></div>
	    <div id="nav">London
	      <br>Paris
	      <br>Tokyo
	      <br></div>
	    <div id="section">
	      <h2>London</h2>
	      <p>London is the capital city of England. It is the most populous city in the United Kingdom, with a metropolitan area of over 13 million inhabitants.</p>
	      <p>Standing on the River Thames, London has been a major settlement for two millennia, its history going back to its founding by the Romans, who named it Londinium.</p>
	    </div>
	    <div id="footer">Copyright ? W3Schools.com</div></body>
	
	</html>


## 11.2 使用HTML5的网站布局

HTML5 提供的新语义元素定义了网页的不同部分：

元素|含义
---|---
header|	定义文档或节的页眉
nav	|定义导航链接的容器
section|	定义文档中的节
article|	定义独立的自包含文章
aside|	定义内容之外的内容（比如侧栏）
footer|	定义文档或节的页脚
details|	定义额外的细节
summary|	定义 details 元素的标题


这个例子使用 `<header>, <nav>, <section>`, 以及` <footer> `来创建多列布局：

	<!DOCTYPE html>
	<html>
	  
	  <head>
	    <style>header { background-color:black; color:white; text-align:center; padding:5px; } nav { line-height:30px; background-color:#eeeeee; height:300px; width:100px; float:left; padding:5px; } section { width:350px; float:left; padding:10px; } footer { background-color:black; color:white; clear:both; text-align:center; padding:5px; }</style>
	  </head>
	  
	  <body>
	    <header>
	      <h1>City Gallery</h1></header>
	    <nav>London
	      <br>Paris
	      <br>Tokyo
	      <br></nav>
	    <section>
	      <h1>London</h1>
	      <p>London is the capital city of England. It is the most populous city in the United Kingdom, with a metropolitan area of over 13 million inhabitants.</p>
	      <p>Standing on the River Thames, London has been a major settlement for two millennia, its history going back to its founding by the Romans, who named it Londinium.</p>
	    </section>
	    <footer>Copyright W3Schools.com</footer></body>
	
	</html>

## 11.3 使用表格的 HTML 布局

`<table> `元素不是作为布局工具而设计的。

- `<table>` 元素的作用是显示表格化的数据。

**使用 `<table>` 元素能够取得布局效果，因为能够通过 CSS 设置表格元素的样式**：


	<!DOCTYPE html>
	<html>
	  
	  <head>
	    <style>table.lamp { width:100%; border:1px solid #d4d4d4; } table.lamp th, td { padding:10px; } table.lamp th { width:40px; }</style>
	  </head>
	  
	  <body>
	    <table class="lamp">
	      <tr>
	        <th>
	          <img src="/images/lamp.jpg" alt="Note" style="height:32px;width:32px"></th>
	        <td>The table element was not designed to be a layout tool.</td></tr>
	    </table>
	  </body>
	
	</html>

# 12. 什么是RWD?

RWD 指的是响应式 Web 设计（Responsive Web Design）

- RWD 是一种网页设计的技术做法，该设计可使网站在不同的设备（从桌面计算机显示器到移动电话或其他移动产品设备）上浏览时对应不同分辨率皆有适合的呈现，减少用户进行缩放、平移和滚动等操作行为


## 12.1 创建响应式设计的方式

1. 自己创建

2. 借助BootStrap

	使用现成的CSS框架,Bootstrap 是最流行的开发响应式 web 的 HTML, CSS, 和 JS 框架

# 13. HTML框架

**通过使用框架，你可以在同一个浏览器窗口中显示不止一个页面。每份HTML文档称为一个框架，并且每个框架都独立于其他的框架。**

**使用框架的坏处**：

- 开发人员必须同时跟踪更多的HTML文档

- 很难打印整张页面


## 13.1 框架结构标签（`<frameset>`）

框架结构标签（`<frameset>`）的功能:

1. 定义如何将窗口分割为框架

2. 每个 frameset 定义了一系列行或列

3. `rows/columns` 的值规定了每行或每列占据屏幕的面积

- frameset 标签也被某些文章和书籍译为框架集。

## 13.2 框架标签(`<frame>`)

Frame 标签定义了放置在每个框架中的 HTML 文档。

在下面的这个例子中，我们设置了一个两列的框架集。第一列被设置为占据浏览器窗口的 25%。第二列被设置为占据浏览器窗口的 75%。HTML 文档 "frame_a.htm" 被置于第一个列中，而 HTML 文档 "frame_b.htm" 被置于第二个列中：

	<frameset cols="25%,75%">
	   <frame src="frame_a.htm">
	   <frame src="frame_b.htm">
	</frameset>

## 13.3 使用注意

假如一个框架有可见边框，用户可以拖动边框来改变它的大小。为了避免这种情况发生，可以在 `<frame> `标签中加入：`noresize="noresize"`。

为不支持框架的浏览器添加 `<noframes>` 标签。

重要提示：不能将 `<body></body>` 标签与 `<frameset></frameset>` 标签同时使用！不过，假如你添加包含一段文本的 `<noframes>` 标签，就必须将这段文字嵌套于`<body></body>` 标签内。（在下面的第一个实例中，可以查看它是如何实现的。)

	<html>
	
	<frameset cols="25%,50%,25%">
	  <frame src="/example/html/frame_a.html">
	  <frame src="/example/html/frame_b.html">
	  <frame src="/example/html/frame_c.html">
	
	<noframes>
	<body>您的浏览器无法处理框架！</body>
	</noframes>
	
	</frameset>
	
	</html>

# 14. HTML IFrame

iframe 用于在网页内显示网页。

**语法:**

	<iframe src="URL"></iframe>


- height 和 width 属性用于规定 iframe 的高度和宽度。

	**属性值的默认单位是像素，但也可以用百分比来设定（比如 "80%"）**。

- frameborder 属性规定是否显示 iframe 周围的边框。

	设置属性值为 "0" 就可以移除边框：

- iframe 可用作链接的目标（target）。这样链接就会在iframe中打开

	链接的 target 属性必须引用 iframe 的 name 属性：

# 15 HTML背景

`<body>` 拥有两个配置背景的标签。背景可以是颜色或者图像。

1. 背景颜色属性将背景设置为某种颜色。属性值可以是十六进制数、RGB 值或颜色名。

		<body bgcolor="#000000">
		<body bgcolor="rgb(0,0,0)">
		<body bgcolor="black">

2. 背景属性将背景设置为图像。属性值为图像的URL。如果图像尺寸小于浏览器窗口，那么图像将在整个浏览器窗口进行复制。

		<body background="clouds.gif">
		<body background="http://www.w3school.com.cn/clouds.gif">

- URL可以是相对地址，如第一行代码。也可以使绝对地址，如第二行代码。

## 15.1 重要提示

**`<body>` 标签中的背景颜色（bgcolor）、背景（background）和文本（text）属性在最新的 HTML 标准（HTML4 和 XHTML）中已被废弃。W3C 在他们的推荐标准中已删除这些属性。**

**应该使用层叠样式表（CSS）来定义 HTML 元素的布局和显示属性。**


# 16. HTML脚本

## 16.1 `<script>`标签

`<script>` 标签用于定义客户端脚本，比如 JavaScript。

- **script 元素既可包含脚本语句，也可通过 src 属性指向外部脚本文件。**

- 必需使用 type 属性来规定脚本的 MIME 类型。

- JavaScript 最常用于图片操作、表单验证以及内容动态更新。

下面的脚本会向浏览器输出“Hello World!”：

	<script type="text/javascript">
		document.write("Hello World!")
	</script>

对于那些需要很多JavaScript 代码的页面来说，会导致浏览器在呈现页面时出现明显的延迟，而延迟期间的浏览器窗口中将是一片空白。为了避免这个问题，现代Web应用程序一般都把全部JavaScript引用放在<body>元索中，放在页面的内容后面。如下例所示：

## 16.2 `<noscipt>`标签

**`<noscript>` 标签提供无法使用脚本时的替代内容，比方在浏览器禁用脚本时，或浏览器不支持客户端脚本时**。

- noscript 元素可包含普通 HTML 页面的 body 元素中能够找到的所有元素。

只有在浏览器不支持脚本或者禁用脚本时，才会显示 noscript 元素中的内容：

	<script type="text/javascript">
		document.write("Hello World!")
	</script>
	<noscript>Your browser does not support JavaScript!</noscript>


## 16.3 适配老式浏览器

如果浏览器压根没法识别 `<script> `标签，那么` <script> `标签所包含的内容将以文本方式显示在页面上。为了避免这种情况发生，你应该将脚本隐藏在注释标签当中。那些老的浏览器（无法识别` <script>` 标签的浏览器）将忽略这些注释，所以不会将标签的内容显示到页面上。而那些新的浏览器将读懂这些脚本并执行它们，即使代码被嵌套在注释标签内。

**示例:**

	<script type="text/javascript">
	<!--
	document.write("Hello World!")
	//-->
	</script>

# 17. HTML头部元素

**`<head> `元素是所有头部元素的容器**

- `<head>` 内的元素可包含脚本，指示浏览器在何处可以找到样式表，提供元信息，等等。

- 以下标签都可以添加到 head 部分：

		<title>、<base>、<link>、<meta>、<script> 以及 <style>

## 17.1 `<title>`标签

**`<title>` 标签定义文档的标题**

- title 元素在所有 HTML/XHTML 文档中都是必需的。

**title 元素能够：**

- 定义浏览器工具栏中的标题

- 提供页面被添加到收藏夹时显示的标题

- 显示在搜索引擎结果中的页面标题

## 17.2 `<base>`标签

**`<base>` 标签为页面上的所有链接规定默认地址或默认目标（target）：**

	<head>
		<base href="http://www.w3school.com.cn/images/" />
		<base target="_blank" />
	</head>


## 17.3 `<link>`标签

**`<link>` 标签定义文档与外部资源之间的关系**

`<link>` 标签最常用于连接样式表：

	<head>
		<link rel="stylesheet" type="text/css" href="mystyle.css" />
	</head>

## 17.4 `<style>`标签

**`<style>` 标签用于为 HTML 文档定义样式信息**

可以在 style 元素内规定 HTML 元素在浏览器中呈现的样式：

	<head>
		<style type="text/css">
			body {background-color:yellow}
			p {color:blue}
		</style>
	</head>

## 17.5 `<meta>`标签

元数据（metadata）是关于数据的信息。

- `<meta>` 标签提供关于 HTML 文档的元数据。元数据不会显示在页面上，但是对于机器是可读的。

- 典型的情况是，meta 元素被用于规定页面的描述、关键词、文档的作者、最后修改时间以及其他元数据。

- `<meta>` 标签始终位于 head 元素中。

- 元数据可用于浏览器（如何显示内容或重新加载页面），搜索引擎（关键词），或其他 web 服务。

### 17.5.1 针对搜索引擎的关键词

一些搜索引擎会利用 meta 元素的 name 和 content 属性来索引您的页面。

下面的 meta 元素定义页面的描述：

	<meta name="description" content="Free Web tutorials on HTML, CSS, XML" />

下面的 meta 元素定义页面的关键词：

	<meta name="keywords" content="HTML, CSS, XML" />

- name 和 content 属性的作用是描述页面的内容。


## 17.6 `<script>`


`<script>` 标签用于定义客户端脚本，比如 JavaScript。

# 18. HTML实体

在 HTML 中，某些字符是预留的。[HTML 实体符号参考手册](http://www.w3school.com.cn/tags/html_ref_entities.html)

- 在 HTML 中不能使用小于号（<）和大于号（>），这是因为浏览器会误认为它们是标签。

- 如果希望正确地显示预留字符，我们必须在 HTML 源代码中使用**字符实体（character entities）**。

	字符实体类似这样：

		&entity_name;
		
		&#entity_number;

	- 如需显示小于号，我们必须这样写：`&lt;` 或 `&#60;`

- 使用实体名而不是数字的好处是，名称易于记忆。不过坏处是，浏览器也许并不支持所有实体名称（对实体数字的支持却很好）。

## 18.1 不间断空格（non-breaking space）

HTML 中的常用字符实体是不间断空格(`&nbsp;`)。

- 浏览器总是会截短 HTML 页面中的空格。如果您在文本中写 10 个空格，在显示该页面之前，浏览器会删除它们中的 9 个。如需在页面中增加空格的数量，您需要使用 `&nbsp;` 字符实体。


# 19. HTML 统一资源定位器(URL)

URL 也被称为网址,全称是`Uniform Resource Locator`,中文也译为“统一资源定位符”

- URL 可以由单词组成，比如 “w3school.com.cn”，或者是因特网协议（IP）地址：192.168.1.253。大多数人在网上冲浪时，会键入网址的域名，因为名称比数字容易记忆。


当您点击 HTML 页面中的某个链接时，对应的` <a>` 标签指向万维网上的一个地址。

- 统一资源定位器（URL）用于定位万维网上的文档（或其他数据）。


网址，比如 `http://www.w3school.com.cn/html/index.asp`，遵守以下的语法规则：

	scheme://host.domain:port/path/filename

**语法解释**：

名称|含义
---|---
scheme | 定义因特网服务的类型。最常见的类型是 http
host | 定义域主机（http 的默认主机是 www）
domain | 定义因特网域名，比如 w3school.com.cn
:port | 定义主机上的端口号（http 的默认端口号是 80）
path | 定义服务器上的路径（如果省略，则文档必须位于网站的根目录中）。
filename | 定义文档/资源的名称

**流行的scheme:**

Scheme	|访问	|用于...
---|---|---
http|	超文本传输协议	|以 http:// 开头的普通网页。不加密。
https|	安全超文本传输协议	|安全网页。加密所有信息交换。
ftp|	文件传输协议	|用于将文件下载或上传至网站。
file|	 &nbsp;	|您计算机上的文件。

## 19.1 URL 字符编码

**URL 只能使用 ASCII 字符集来通过因特网进行发送。**但是由于 URL 常常会包含 ASCII 集合之外的字符，所以URL 必须转换为有效的 ASCII 格式。

- URL 编码使用 "%" 其后跟随两位的十六进制数来替换非 ASCII 字符。

- URL 不能包含空格。URL 编码通常使用 + 来替换空格。


# 20. HTML颜色

颜色由红色、绿色、蓝色混合而成。


**颜色值:**

颜色由一个十六进制符号来定义，这个符号由红色、绿色和蓝色的值组成（RGB）。

每种颜色的最小值是0（十六进制：#00）。最大值是255（十六进制：#FF）。

**颜色名:**

大多数的浏览器都支持颜色名集合。

- 提示：仅仅有 16 种颜色名被 W3C 的 HTML4.0 标准所支持。它们是：aqua, black, blue, fuchsia, gray, green, lime, maroon, navy, olive, purple, red, silver, teal, white, yellow。

	如果需要使用其它的颜色，需要使用十六进制的颜色值。


**Web安全色:**

数年以前，当大多数计算机仅支持 256 种颜色的时候，一系列 216 种 Web 安全色作为 Web 标准被建议使用。其中的原因是，微软和 Mac 操作系统使用了 40 种不同的保留的固定系统颜色（双方大约各使用 20 种）。

我们不确定如今这么做的意义有多大，因为越来越多的计算机有能力处理数百万种颜色，不过做选择还是你自己。


# 21. HTML 文档类型

Web 世界中存在许多不同的文档。只有了解文档的类型，浏览器才能正确地显示文档。

- HTML 也有多个不同的版本，**只有完全明白页面中使用的确切 HTML 版本，浏览器才能完全正确地显示出 HTML 页面。这就是 `<!DOCTYPE>` 的用处**。

- `<!DOCTYPE>` 不是 HTML 标签。它为浏览器提供一项信息（声明），即 HTML 是用什么版本编写的。


示例:

	<!DOCTYPE html>
	<html>
	<head>
	<title>Title of the document</title>
	</head>
	
	<body>
	The content of the document......
	</body>
	
	</html>

## 21.1 常用声明

HTML5

	<!DOCTYPE html>

HTML 4.01

	<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
	"http://www.w3.org/TR/html4/loose.dtd">

XHTML 1.0

	<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">