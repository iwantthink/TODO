# 使用文件
[Working With Files](https://docs.gradle.org/current/userguide/working_with_files.html)
# 1 查找文件
可以使用`Project.file(java.lang.Object)`方法去找到相对项目目录下的文件

**示例1：**查找文件

	// Using a relative path
	File configFile = file('src/config.xml')
	
	// Using an absolute path
	configFile = file(configFile.absolutePath)
	
	// Using a File object with a relative path
	configFile = file(new File('src/config.xml'))
	
	// Using a java.nio.file.Path object with a relative path
	configFile = file(Paths.get('src', 'config.xml'))
	
	// Using an absolute java.nio.file.Path object
	configFile = file(Paths.get(System.getProperty('user.home')).resolve('global-config.xml'))

- 可以将任何对象传递给`file()`方法，它会尝试将所有这些对象转换为一个File对象。通常，会传递 String,File或Path Instance(如果这个Path instance是绝对路径，则用它来构建一个File对象。否则会利用项目目录构建一个File对象)。

- `file()`方法还支持解析Url ，如`file:/some/path.xml`

除了使用`Project.file(java.lang.Object)`这种方式将用户提供的值转换为File,还可以使用`new File(somePath)`这种方式获取File

# 2 文件集合
一个文件集合就是一组文件。由`FileCollection`接口表示。Gradle API 中的许多对象都实现了这个接口。例如，[dependency configurations](https://docs.gradle.org/current/userguide/dependency_management.html#sub:configurations) 实现了`FileCollection`

获取`FileCollection`对象的一个方式是使用`Project.files(java.lang.Object[])`方法。可以给这个方法传递任意书连接杆的