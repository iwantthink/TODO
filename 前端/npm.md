# NPX 使用
[npx 使用教程](https://www.ruanyifeng.com/blog/2019/02/npx.html)


一般来说，想要调用通过npm安装的模块 ，只能在项目脚本和 `package.json` 的scripts字段里面， 如果想在命令行下调用，必须像下面这样。

	# 项目的根目录下执行mocha 模块
	$ node-modules/.bin/mocha --version

npx就是为了解决上述问题而诞生的，让使用项目内部安装的模块更加方便

	npx mocha --version
	
- 原理：**运行时会去`node_modules/.bin`路径和环境变量`$PATH`里面，检查命令是否存在**	

- 由于npx 会检查环境变量`$PATH`,所以系统命令也可以调用

		# 等同于ls
		$ npx ls