# HTML表单

[W3School - HTML表单](http://www.w3school.com.cn/html/html_forms.asp)


# 1. 简介

**HTML 表单用于搜集不同类型的用户输入**。


## 1.1 表单的表现方式?

`<form>` 元素定义 HTML 表单, HTML表单包含表单元素

- 表单元素指的是不同类型的`<input>`元素,复选框,单选按钮,提交按钮等等

		<form>
		 .
		form elements
		 .
		</form>


## 1.2 表单相关属性介绍

属性	|描述
---|---
accept-charset|	规定在被提交表单中使用的字符集（默认：页面字符集）。
action|	规定向何处提交表单的地址（URL）（提交页面）。
autocomplete|	规定浏览器应该自动完成表单（默认：开启）。
enctype|	规定被提交数据的编码（默认：url-encoded）。
method|	规定在提交表单时所用的 HTTP 方法（默认：GET）。
name|	规定识别表单的名称（对于 DOM 使用：document.forms.name）。
novalidate|	规定浏览器不验证表单。
target|	规定 action 属性中地址的目标（默认：_self）。


### 1.2.1 Action属性

action 属性定义在提交表单时执行的动作。

向服务器提交表单的通常做法是使用提交按钮。

通常，表单会被提交到 web 服务器上的网页。

在上面的例子中，指定了某个服务器脚本来处理被提交表单：

	<form action="action_page.php">

- **如果省略 action 属性，则 action 会被设置为当前页面**


### 1.2.2 Method属性

method 属性规定在提交表单时所用的 HTTP 方法（GET 或 POST）：

	<form action="action_page.php" method="GET">


#### 1.2.2.1 关于GET

如果表单提交是被动的（比如搜索引擎查询），并且没有敏感信息。

当您使用 GET 时，表单数据在页面地址栏中是可见的：

	action_page.php?firstname=Mickey&lastname=Mouse

- 注释：GET 最适合少量数据的提交。浏览器会设定容量限制。

#### 1.2.2.2 关于POST

如果表单正在更新数据，或者包含敏感信息（例如密码）。

POST 的安全性更加，因为在页面地址栏中被提交的数据是不可见的。


### 1.2.3 Name 属性

**如果要正确地被提交，每个输入字段必须设置一个 name 属性。**

本例只会提交 "Last name" 输入字段：


## 1.3 如何组合表单数据?

`<fieldset>` 元素组合表单中的相关数据

`<legend>` 元素为 `<fieldset>` 元素定义标题。


# 2. 表单元素

即表单中可以填写的元素

## 2.1 HTML表单元素

### 2.1.1 `<input>`元素

**最重要的表单元素是 `<input> `元素**。

- `<input>` 元素根据不同的 type 属性，可以变化为多种形态


类型|	描述
---|---
text	|定义常规文本输入。
radio	|定义单选按钮输入（选择多个选择之一）
submit	|定义提交按钮（提交表单）


### 2.1.2 `<select> `元素（下拉列表）

定义下拉列表

	<select name="cars">
		<option value="volvo">Volvo</option>
		<option value="saab">Saab</option>
		<option value="fiat">Fiat</option>
		<option value="audi">Audi</option>
	</select>


- `<option>` 元素定义待选择的选项。

- 列表通常会把首个选项显示为被选选项。

	您能够通过添加 selected 属性来定义预定义选项。


### 2.1.3 `<textarea>`元素

定义多行输入字段（文本域）

示例:

	<textarea name="message" rows="10" cols="30">
	The cat was playing in the garden.
	</textarea>

### 2.1.4 `<button>`元素

定义可点击的按钮

示例:

	<button type="button" onclick="alert('Hello World!')">Click Me!</button>



## 2.2 HTML5表单元素

HTML5 增加了如下表单元素：

- `<datalist>`

- `<keygen>`

- `<output>`


注释：默认地，浏览器不会显示未知元素。新元素不会破坏您的页面。

### 2.2.1 `<datalist>`元素

`<datalist>` 元素为 `<input>` 元素设置预定义的选项列表。

用户会在他们输入数据时看到预定义选项的下拉列表。

**`<input>` 元素的 list 属性必须引用 `<datalist>` 元素的 id 属性。**

**示例:**

	<form action="action_page.php">
	<input list="browsers">
	<datalist id="browsers">
	   <option value="Internet Explorer">
	   <option value="Firefox">
	   <option value="Chrome">
	   <option value="Opera">
	   <option value="Safari">
	</datalist> 
	</form>


# 3. `<input>`的类型

**通过修改`<input>`的type属性,可以改变其输入类型**


## 3.1 HTML中的输入类型

### 3.1.1 文本输入(type="text")

定义供文本输入的单行输入字段

**示例:**

	<form>
		 First name:<br>
		<input type="text" name="firstname">
		<br>
		 Last name:<br>
		<input type="text" name="lastname">
	</form> 

- **表单本身不可见**

- **文本字段的默认宽度是20个字符**

- **如果要正确地被提交，每个输入字段必须设置一个 name 属性**


### 3.1.2 密码输入(type="password")

定义密码字段

	<form>
	 	User name:<br>
		<input type="text" name="username">
		<br>
		 User password:<br>
		<input type="password" name="psw">
	</form> 


- password 字段中的字符会被做掩码处理（显示为星号或实心圆


### 3.1.3 提交按钮(type="submit")

用于设置向表单处理程序（form-handler）提交表单的按钮

- 表单处理程序通常是包含用来处理输入数据的脚本的服务器页面。

- 表单处理程序在表单的 action 属性中指定：


示例:

	<form action="action_page.php">
		First name:<br>
		<input type="text" name="firstname" value="Mickey">
		<br>
		Last name:<br>
		<input type="text" name="lastname" value="Mouse">
		<br><br>
		<input type="submit" value="Submit">
	</form> 

- 如果您省略了提交按钮的 value 属性，那么该按钮将获得默认文本：





### 3.1.4 单按钮输入(type="radio")

单选按钮允许用户在有限数量的选项中选择其中之一：


**示例:**

	<form>
	<input type="radio" name="sex" value="male" checked>Male
	<br>
	<input type="radio" name="sex" value="female">Female
	</form> 

### 3.1.5 复选框输入(type="checkbox")

定义复选框,允许用户在有限数量的选项中选择零个或多个选项

示例:

	<form>
		<input type="checkbox" name="vehicle" value="Bike">I have a bike
		<br>
		<input type="checkbox" name="vehicle" value="Car">I have a car 
	</form> 



### 3.1.6 按钮(type="button")

定义按钮


示例:

	<input type="button" onclick="alert('Hello World!')" value="Click Me!">


## 3.2 HMTL5 新增的输入类型


### 3.2.1 number类型

用于应该包含数字值的输入字段。

能够对数字做出限制。

根据浏览器支持，限制可应用到输入字段。

	<form>
	  Quantity (between 1 and 5):
	  <input type="number" name="quantity" min="1" max="5">
	</form>


输入限制
---


属性	|描述
---|---
disabled|	规定输入字段应该被禁用。
max|	规定输入字段的最大值。
maxlength|	规定输入字段的最大字符数。
min|	规定输入字段的最小值。
pattern|	规定通过其检查输入值的正则表达式。
readonly|	规定输入字段为只读（无法修改）。
required|	规定输入字段是必需的（必需填写）。
size|	规定输入字段的宽度（以字符计）。
step|	规定输入字段的合法数字间隔。
value|	规定输入字段的默认值。


### 3.2.2 date类型

用于应该包含日期的输入字段。

根据浏览器支持，日期选择器会出现输入字段中。


	<form>
	  Enter a date before 1980-01-01:
	  <input type="date" name="bday" max="1979-12-31"><br>
	  Enter a date after 2000-01-01:
	  <input type="date" name="bday" min="2000-01-02"><br>
	</form>

- 同样的可以添加限制

### 3.2.3 color 类型

用于应该包含颜色的输入字段。

根据浏览器支持，颜色选择器会出现输入字段中。

	
	<form>
	  Select your favorite color:
	  <input type="color" name="favcolor">
	</form>

### 3.2.4 range 类型


用于应该包含一定范围内的值的输入字段。

根据浏览器支持，输入字段能够显示为滑块控件。


	<form>
	  <input type="range" name="points" min="0" max="10">
	</form>

- 能够使用如下属性来规定限制：min、max、step、value


### 3.2.5 month类型

允许用户选择月份和年份。

根据浏览器支持，日期选择器会出现输入字段中。

	<form>
	  Birthday (month and year):
	  <input type="month" name="bdaymonth">
	</form>



### 3.2.6 week类型


允许用户选择周和年。

根据浏览器支持，日期选择器会出现输入字段中

	<form>
	  Select a week:
	  <input type="week" name="week_year">
	</form>

### 3.2.7 time类型

允许用户选择时间（无时区）。

根据浏览器支持，时间选择器会出现输入字段中


	<form>
	  Select a time:
	  <input type="time" name="usr_time">
	</form>

- Firefox 或者 Internet Explorer 11 以及更早版本不支持 type="time"

### 3.2.8 datetime类型

允许用户选择日期和时间（有时区）。

根据浏览器支持，日期选择器会出现输入字段中


	<form>
	  Birthday (date and time):
	  <input type="datetime" name="bdaytime">
	</form>

- Chrome、Firefox 或 Internet Explorer 不支持 type="datetime"


### 3.2.9 datetime-local类型

允许用户选择日期和时间（无时区）。

根据浏览器支持，日期选择器会出现输入字段中


- Firefox 或者 Internet Explorer 不支持 type="datetime-local"


### 3.2.10 email类型

用于应该包含电子邮件地址的输入字段。

根据浏览器支持，能够在被提交时自动对电子邮件地址进行验证。

某些智能手机会识别 email 类型，并在键盘增加 ".com" 以匹配电子邮件输入。

	<form>
	  E-mail:
	  <input type="email" name="email">
	</form>

- IE9 及更早版本不支持 type="email"

### 3.2.11 search类型

用于搜索字段（搜索字段的表现类似常规文本字段）


	<form>
	  Search Google:
	  <input type="search" name="googlesearch">
	</form>

### 3.2.12 tel类型

用于应该包含电话号码的输入字段。

目前只有 Safari 8 支持 tel 类型


	<form>
	  Telephone:
	  <input type="tel" name="usrtel">
	</form>

### 3.2.13 url

用于应该包含 URL 地址的输入字段。

根据浏览器支持，在提交时能够自动验证 url 字段。

某些智能手机识别 url 类型，并向键盘添加 ".com" 以匹配 url 输入

	
	<form>
	  Add your homepage:
	  <input type="url" name="homepage">
	</form>



# 4. `<input>`的属性

## 4.1 HTML的属性

### 4.1.1 value属性

规定输入字段的初始值


	<form action="">
		 First name:<br>
		<input type="text" name="firstname" value="John">
		<br>
		 Last name:<br>
		<input type="text" name="lastname">
	</form> 

### 4.1.2 readonly属性

规定输入字段为只读（不能修改）


	<form action="">
		 First name:<br>
		<input type="text" name="firstname" value="John" readonly>
		<br>
		 Last name:<br>
		<input type="text" name="lastname">
	</form> 


- readonly 属性不需要值。它等同于 `readonly="readonly"`


### 4.1.3 disabled 属性

规定输入字段是禁用的。

- 被禁用的元素是不可用和不可点击的。

- 被禁用的元素不会被提交。


	<form action="">
		 First name:<br>
		<input type="text" name="firstname" value="John" disabled>
		<br>
		 Last name:<br>
		<input type="text" name="lastname">
	</form> 

- disabled 属性不需要值。它等同于 `disabled="disabled"`

### 4.1.4 size 属性

规定输入字段的尺寸（以字符计）

	<form action="">
		 First name:<br>
		<input type="text" name="firstname" value="John" size="40">
		<br>
		 Last name:<br>
		<input type="text" name="lastname">
	</form> 


### 4.1.5 maxlength 属性

规定输入字段允许的最大长度

	<form action="">
		 First name:<br>
		<input type="text" name="firstname" maxlength="10">
		<br>
		 Last name:<br>
		<input type="text" name="lastname">
	</form> 


- 如设置 maxlength 属性，则输入控件不会接受超过所允许数的字符。

	**该属性不会提供任何反馈**。如果需要提醒用户，则必须编写 JavaScript 代码。

	注释：**输入限制并非万无一失。JavaScript 提供了很多方法来增加非法输入。如需安全地限制输入，则接受者（服务器）必须同时对限制进行检查**。

## 4.2 HTML5的属性

HTML5 为`<input>`新增了属性,并为`<form>`也新增了属性


### 4.2.1 autocomplete 属性(`<form>`)

规定表单或输入字段是否应该自动完成。

- 当自动完成开启，浏览器会基于用户之前的输入值自动填写值。

- 提示：您可以把表单的 autocomplete 设置为 on，同时把特定的输入字段设置为 off，反之亦然。


- **autocomplete 属性适用于 `<form>` 以及如下 `<input>` 类型**：

	text、search、url、tel、email、password、datepickers、range 以及 color。


自动完成开启的 HTML 表单（某个输入字段为 off）：


	<form action="action_page.php" autocomplete="on">
	   First name:<input type="text" name="fname"><br>
	   Last name: <input type="text" name="lname"><br>
	   E-mail: <input type="email" name="email" autocomplete="off"><br>
	   <input type="submit">
	</form> 

- 在某些浏览器中，您也许需要手动启用自动完成功能。


### 4.2.2 novalidate 属性(`<form>`)


novalidate 属性属于` <form>` 属性

如果设置，则 novalidate 规定在提交表单时不对表单数据进行验证。

	<form action="action_page.php" novalidate>
	   E-mail: <input type="email" name="user_email">
	   <input type="submit">
	</form> 


### 4.2.3 autofocus 属性

autofocus 属性是布尔属性。

如果设置，则规定当页面加载时` <input> `元素应该自动获得焦点。

示例(使First name 输入在页面加载时自动获取焦点):

	<form action="demo_form.asp">
	  First name: <input type="text" name="fname" autofocus><br>
	  Last name: <input type="text" name="lname"><br>
	  <input type="submit">
	</form>

- Internet Explorer 9 以及更早的版本不支持 input 标签的 autofocus 属性


### 4.2.4 form 属性

form 属性规定 `<input> `元素所属的一个或多个表单。

- 提示：如需引用一个以上的表单，请使用空格分隔的表单 id 列表。

示例(输入字段位于 HTML 表单之外（但仍属表单）):

	<form action="/example/html5/demo_form.asp" method="get" id="form1">
		First name: <input type="text" name="fname" /><br />
		<input type="submit" value="提交" />
		</form>
		
		<p>下面的 "Last name" 字段位于 form 元素之外，但仍然是表单的一部分。</p>

	Last name: <input type="text" name="lname" form="form1" />

### 4.2.5 formaction 属性

规定当提交表单时处理该输入控件的文件的 URL。

- **该属性覆盖 `<form>` 元素的 action 属性**。

- 该属性适用于 type="submit" 以及 type="image"。


	<form action="action_page.php">
	   First name: <input type="text" name="fname"><br>
	   Last name: <input type="text" name="lname"><br>
	   <input type="submit" value="Submit"><br>
	   <input type="submit" formaction="demo_admin.asp"
	   value="Submit as admin">
	</form> 

### 4.2.6 formenctype  属性


规定当把表单数据（form-data）提交至服务器时如何对其进行编码（仅针对 method="post" 的表单）。

- **该属性覆盖` <form> `元素的 enctype 属性**。

- 该属性适用于 type="submit" 以及 type="image"。


	<form action="/example/html5/demo_post_enctype.asp" method="post">
		First name: <input type="text" name="fname" /><br />
		<input type="submit" value="提交" />
		<input type="submit" formenctype="multipart/form-data" value="以 Multipart/form-data 编码提交" />
	</form>


### 4.2.7 formmethod  属性

定义用以向 action URL 发送表单数据（form-data）的 HTTP 方法。

- 该属性覆盖 `<form>` 元素的 method 属性。

- 该属性适用于 type="submit" 以及 type="image"。


	<form action="action_page.php" method="get">
	   First name: <input type="text" name="fname"><br>
	   Last name: <input type="text" name="lname"><br>
	   <input type="submit" value="Submit">
	   <input type="submit" formmethod="post" formaction="demo_post.asp"
	   value="Submit using POST">
	</form> 


### 4.2.8 formnovalidate  属性

如果设置，则规定在提交表单时不对 `<input>` 元素进行验证。

- **该属性覆盖 `<form>` 元素的 novalidate 属性**。

- 该属性可用于 type="submit"。


	<form action="action_page.php">
	   E-mail: <input type="email" name="userid"><br>
	   <input type="submit" value="Submit"><br>
	   <input type="submit" formnovalidate value="Submit without validation">
	</form> 



### 4.2.9 formtarget  属性

规定的名称或关键词指示提交表单后在何处显示接收到的响应。

- **该属性会覆盖 `<form>` 元素的 target 属性**。

- 该属性可与 type="submit" 和 type="image" 使用。


	<form action="action_page.php">
	   First name: <input type="text" name="fname"><br>
	   Last name: <input type="text" name="lname"><br>
	   <input type="submit" value="Submit as normal">
	   <input type="submit" formtarget="_blank"
	   value="Submit to a new window">
	</form> 



### 4.2.10 height 和 width 属性

height 和 width 属性规定 `<input>` 元素的高度和宽度。

- **height 和 width 属性仅用于 `<input type="image">`**。

- 注释：请始终规定图像的尺寸。如果浏览器不清楚图像尺寸，则页面会在图像加载时闪烁。


	<input type="image" src="img_submit.gif" alt="Submit" width="48" height="48">

- 该图片被定义为提交按钮

### 4.2.11 list  属性

list 属性定义了`<input>` 引用哪个 `<datalist>` 元素的预定义选项。


	<input list="browsers">
	
	<datalist id="browsers">
	   <option value="Internet Explorer">
	   <option value="Firefox">
	   <option value="Chrome">
	   <option value="Opera">
	   <option value="Safari">
	</datalist> 

### 4.2.12 min 和 max 属性

规定 `<input> `元素的最小值和最大值。

- min 和 max 属性适用于如需输入类型：

	number、range、date、datetime、datetime-local、month、time 以及 week。


	<form action="/example/html5/demo_form.asp" method="get">
		Points: <input type="number" name="points" min="0" max="10" />
		<input type="submit" />
	</form>


### 4.2.13 multiple  属性

multiple 属性是布尔属性。

规定是否允许用户在 `<input>` 元素中输入一个以上的值。

- multiple 属性适用于以下输入类型：email 和 file。


	<form action="/example/html5/demo_form.asp" method="get">
		选择图片：<input type="file" name="img" multiple="multiple" />

		<input type="submit" />
	</form>

- 注意是在文件选择界面就选择俩个,而不是重复添加

### 4.2.14 pattern  属性

规定用于检查 `<input>` 元素值的正则表达式。

- pattern 属性适用于以下输入类型：

	text、search、url、tel、email、and password。

- 提示：请使用全局的 title 属性对模式进行描述以帮助用户。


	<form action="/example/html5/demo_form.asp" method="get">

		国家代码：<input type="text" name="country_code" pattern="[A-z]{3}"
		title="三个字母的国家代码" />
		<input type="submit" />
	</form>


### 4.2.15 placeholder  属性

**规定用以描述输入字段预期值的提示**（样本值或有关格式的简短描述）。

- 该提示会在用户输入值之前显示在输入字段中。

- placeholder 属性适用于以下输入类型：

	text、search、url、tel、email 以及 password。



	<form action="/example/html5/demo_form.asp" method="get">
		<input type="search" name="user_search" placeholder="Search W3School" />
		<input type="submit" />
	</form>


### 4.2.16 required  属性

required 属性是布尔属性。

- 如果设置，则规定在提交表单之前必须填写输入字段。

- required 属性适用于以下输入类型：

	text、search、url、tel、email、password、date pickers、number、checkbox、radio、and file.


	<form action="/example/html5/demo_form.asp" method="get">
		Name: <input type="text" name="usr_name" required="required" />
		<input type="submit" value="提交" />
	</form>


### 4.2.17 step  属性


规定 `<input>` 元素的合法数字间隔。

- 示例：如果 step="3"，则合法数字应该是 -3、0、3、6、等等。

- 提示：step 属性可与 max 以及 min 属性一同使用，来创建合法值的范围。

- step 属性适用于以下输入类型：

	number、range、date、datetime、datetime-local、month、time 以及 week。



	<form action="/example/html5/demo_form.asp" method="get">
		<input type="number" name="points" step="3" />
		<input type="submit" />
	</form>

