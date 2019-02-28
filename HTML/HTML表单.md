# HTML表单

[W3School - HTML表单](http://www.w3school.com.cn/html/html_forms.asp)


# 1. 简介

HTML 表单用于搜集不同类型的用户输入。


## 1.1 表单的表现方式?

`<form>` 元素定义 HTML 表单, HTML表单包含表单元素

- 表单元素指的是不同类型的`<input>`元素,复选框,单选按钮,提交按钮等等

		<form>
		 .
		form elements
		 .
		</form>


## 1.3 相关属性介绍

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


### 1.3.1 Action属性

action 属性定义在提交表单时执行的动作。

向服务器提交表单的通常做法是使用提交按钮。

通常，表单会被提交到 web 服务器上的网页。

在上面的例子中，指定了某个服务器脚本来处理被提交表单：

	<form action="action_page.php">

- **如果省略 action 属性，则 action 会被设置为当前页面**


### 1.3.2 Method属性

method 属性规定在提交表单时所用的 HTTP 方法（GET 或 POST）：

	<form action="action_page.php" method="GET">


#### 1.3.2.1 关于GET

如果表单提交是被动的（比如搜索引擎查询），并且没有敏感信息。

当您使用 GET 时，表单数据在页面地址栏中是可见的：

	action_page.php?firstname=Mickey&lastname=Mouse

- 注释：GET 最适合少量数据的提交。浏览器会设定容量限制。

#### 1.3.2.2 关于POST

如果表单正在更新数据，或者包含敏感信息（例如密码）。

POST 的安全性更加，因为在页面地址栏中被提交的数据是不可见的。


### 1.3.3 Name 属性

**如果要正确地被提交，每个输入字段必须设置一个 name 属性。**

本例只会提交 "Last name" 输入字段：


## 1.5 如何组合表单数据?

`<fieldset>` 元素组合表单中的相关数据

`<legend>` 元素为 `<fieldset>` 元素定义标题。


# 2. 表单元素

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

通过修改`<input>`的type属性,可以改变其输入类型


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


### 3.2.1 number

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


### 3.2.2 date

用于应该包含日期的输入字段。

根据浏览器支持，日期选择器会出现输入字段中。


	<form>
	  Enter a date before 1980-01-01:
	  <input type="date" name="bday" max="1979-12-31"><br>
	  Enter a date after 2000-01-01:
	  <input type="date" name="bday" min="2000-01-02"><br>
	</form>

- 同样的可以添加限制

### 3.2.3 color 

用于应该包含颜色的输入字段。

根据浏览器支持，颜色选择器会出现输入字段中。

	
	<form>
	  Select your favorite color:
	  <input type="color" name="favcolor">
	</form>

### 3.2.4 range 


用于应该包含一定范围内的值的输入字段。

根据浏览器支持，输入字段能够显示为滑块控件。


	<form>
	  <input type="range" name="points" min="0" max="10">
	</form>

- 能够使用如下属性来规定限制：min、max、step、value


### 3.2.5 month

允许用户选择月份和年份。

根据浏览器支持，日期选择器会出现输入字段中。

	<form>
	  Birthday (month and year):
	  <input type="month" name="bdaymonth">
	</form>
















