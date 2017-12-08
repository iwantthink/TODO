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

获取`FileCollection`对象的一个方式是使用`Project.files(java.lang.Object[])`方法。可以给这个方法传递任意数量的参数，都会被转换成一组File对象。**通常会传 `collections,iterables,maps and arrays`给files()方法这些类型参数**

	FileCollection collection = files('src/file1.txt',
	                                  new File('src/file2.txt'),
	                                  ['src/file3.txt', 'src/file4.txt'],
	                                  Paths.get('src', 'file5.txt'))


**`files()`方法的参数还可以是一个closure或一个`Callable`实例。**这俩个对象将在集合内容被查询时被调用，并且它们会返回一组转换过的File实例。返回值可以是任何`files()`方法支持的任何类型

**示例1**：通过Closure或`Callable`实例

	task list {
	    doLast {
	        File srcDir
	
	        // Create a file collection using a closure
	        collection = files { srcDir.listFiles() }
	
	        srcDir = file('src')
	        println "Contents of $srcDir.name"
	        collection.collect { relativePath(it) }.sort().each { println it }
	
	        srcDir = file('src2')
	        println "Contents of $srcDir.name"
	        collection.collect { relativePath(it) }.sort().each { println it }
	    }
	}
	
	> gradle -q list
	Contents of src
	src/dir1
	src/file1.txt
	Contents of src2
	src2/dir1
	src2/dir2

**还有另外一些类型可以当做`files()`参数**：

- **FileCollection**:文件集合本身
- **Task**:任务的输出内容是文件集合
- **TaskOutputs**:TaskOutputs的输出内容是文件集合

**注意：`FileCollection`是懒加载的，例如创建一个文件集合代表将来哪些文件会被创建**

文件集合是 可迭代的，并且可以通过`as`操作符将其类型转换为其他多个类型。 可以通过`+`操作符将俩个文件集合添加到一起。可以通过`-`操作符将一个文件集合从另外一个文件集合中减去

	// Iterate over the files in the collection
	collection.each { File file ->
	    println file.name
	}
	
	// Convert the collection to various types
	Set set = collection.files
	Set set2 = collection as Set
	List list = collection as List
	String path = collection.asPath
	File file = collection.singleFile
	File file2 = collection as File
	
	// Add and subtract collections
	def union = collection + files('src/file3.txt')
	def different = collection - files('src/file3.txt')

# 3 文件树
文件树是按照阶级层次排列的集合。例如，文件树可以代表目录树或者zip文件的内容。文件树通过`FileTree`接口表示，`FileTree`接口扩展了`FileCollection`接口，所以`FileTree`可以被当做`FileCollection`对待。

Gradle中有`source sets`等实现了`FileTree`接口。

通过`Project.fileTree(java.util.Map)`方法获得一个`FileTree`对象，这将获得一个具有基础目录的文件树，可以使用`Ant-style`添加`include`或`exclude`模式

	// Create a file tree with a base directory
	FileTree tree = fileTree(dir: 'src/main')
	
	// Add include and exclude patterns to the tree
	tree.include '**/*.java'
	tree.exclude '**/Abstract*'
	
	// Create a tree using path
	tree = fileTree('src').include('**/*.java')
	
	// Create a tree using closure
	tree = fileTree('src') {
	    include '**/*.java'
	}
	
	// Create a tree using a map
	tree = fileTree(dir: 'src', include: '**/*.java')
	tree = fileTree(dir: 'src', includes: ['**/*.java', '**/*.xml'])
	tree = fileTree(dir: 'src', include: '**/*.java', exclude: '**/*test*/**')

`FileTree`可以像`FileCollection`一样使用，另外还可以访问文件树的内容并通过`Ant-style`去选择文件树

	// Iterate over the contents of a tree
	tree.each {File file ->
	    println file
	}
	
	// Filter a tree
	FileTree filtered = tree.matching {
	    include 'org/gradle/api/**'
	}
	
	// Add trees together
	FileTree sum = tree + fileTree(dir: 'src/test')
	
	// Visit the elements of the tree
	tree.visit {element ->
	    println "$element.relativePath => $element.file"
	}

- 注意：默认情况下，`fileTree()`方法返回的`FileTree`实例会默认应用一些`Ant-style`的`exclude`模式，[参考Default Excludes](http://ant.apache.org/manual/dirtasks.html#defaultexcludes)

# 4 Using the contents of an archive as a file tree
可以使用`archive`的内容，例如`ZIP`或`TAR`文件，Gradle提供了`Project.zipTree(java.lang.Object)`和`Project.tarTree(java.lang.Object)`方法。这些方法将会返回一个`FileTree`对象，可以当做文件树或文件集合使用。

**示例1**：Using an archive as a file tree

	// Create a ZIP file tree using path
	FileTree zip = zipTree('someFile.zip')
	
	// Create a TAR file tree using path
	FileTree tar = tarTree('someFile.tar')
	
	//tar tree attempts to guess the compression based on the file extension
	//however if you must specify the compression explicitly you can:
	FileTree someTar = tarTree(resources.gzip('someTar.ext'))

# 5 指定一组输入文件
**Gradle中的许多接收一组输入文件的属性**。例如，`JavaCompile`任务有一个`source`属性(该属性定义了待编译的源文件，可以使用和`files()`方法同样的参数给该属性设置(例如`File String collection FileCollection closure`)

	task compile(type: JavaCompile)
	
	// Use a File object to specify the source directory
	compile {
	    source = file('src/main/java')
	}
	
	// Use a String path to specify the source directory
	compile {
	    source = 'src/main/java'
	}
	
	// Use a collection to specify multiple source directories
	compile {
	    source = ['src/main/java', '../shared/java']
	}
	
	// Use a FileCollection (or FileTree in this case) to specify the source files
	compile {
	    source = fileTree(dir: 'src/main/java').matching { include 'org/gradle/api/**' }
	}
	
	// Using a closure to specify the source files.
	compile {
	    source = {
	        // Use the contents of each zip file in the src dir
	        file('src').listFiles().findAll {it.name.endsWith('.zip')}.collect { zipTree(it) }
	    }
	}

	compile {
	    // Add some source directories use String paths
	    source 'src/main/java', 'src/main/groovy'
	
	    // Add a source directory using a File object
	    source file('../shared/java')
	
	    // Add some source directories using a closure
	    source { file('src/test/').listFiles() }
	}

# 6 复制文件
Gradle 提供了`Copy`类型去复制文件，复制任务允许在复制任务时过滤文件的内容，并映射到文件名

要使用`Copy`任务，必须提供一组待复制的文件和一个复制的目标文件夹。可以使用`copy spec`在复制过程时指定如何转换文件，`copy spec`代表着一个`CopySpec`接口，`Copy`任务就实现了这个接口。

通过`CopySpec.from(java.lang.Object[])`指定输入的源文件，通过`CopySpec.into(java.lang.Object[])`指定输出的目录

**示例1**：通过Copy任务复制文件
	
	task copyTask(type: Copy) {
	    from 'src/main/webapp'
	    into 'build/explodedWar'
	}

`from`方法接收的参数和`files()`方法一样。当一个参数为目录时，当前目录下所有的内容将会递归的赋值到目标目录下(不包括本身)。当一个参数不存在时，将被忽略。当一个参数为task时，该任务的输出文件将用来复制且自动添加`Copy`依赖。

`into`方法接收的参数和`file()`方法一样。

**示例2**：指定复制任务的源文件和目标目录

	task anotherCopyTask(type: Copy) {
	    // Copy everything under src/main/webapp
	    from 'src/main/webapp'
	    // Copy a single file
	    from 'src/staging/index.html'
	    // Copy the output of a task
	    from copyTask
	    // Copy the output of a task using Task outputs explicitly.
	    from copyTaskWithPatterns.outputs
	    // Copy the contents of a Zip file
	    from zipTree('src/main/assets.zip')
	    // Determine the destination directory later
	    into { getDestDir() }
	}

可以通过`Ant-style`的`include`或`exclude`模式去选择文件

**示例3**：筛选待复制的文件

	task copyTaskWithPatterns(type: Copy) {
	    from 'src/main/webapp'
	    into 'build/explodedWar'
	    include '**/*.html'
	    include '**/*.jsp'
	    exclude { details -> details.file.name.endsWith('.html') &&
	                         details.file.text.contains('staging') }
	}

另外还可以通过`Project.copy(org.gradle.api.Action)`方法去复制文件，这与使用Task的方式类似但是有一些主要限制，首先，`copy()`方法不支持增量更新。其次，当一个任务被作为复制的源文件时(例如作为from()的参数),`copy()`方法不能遵守任务依赖关系，因为他是一个方法而不是任务，**所以必须显示的声明所有的输入和输出.**

**示例4**:通过`copy()`方法复制任务，不支持增量更新

	task copyMethod {
	    doLast {
	        copy {
	            from 'src/main/webapp'
	            into 'build/explodedWar'
	            include '**/*.html'
	            include '**/*.jsp'
	        }
	    }
	}

## 6.1 重命名文件

**示例1**：复制时重命名文件

	task rename(type: Copy) {
	    from 'src/main/webapp'
	    into 'build/explodedWar'
	    // Use a closure to map the file name
	    rename { String fileName ->
	        fileName.replace('-staging-', '')
	    }
	    // Use a regular expression to map the file name使用正则表达式来映射文件名
	    rename '(.+)-staging-(.+)', '$1$2'
	    rename(/(.+)-staging-(.+)/, '$1$2')
	}

## 6.2 筛选文件

**示例1**：复制时筛选文件

	import org.apache.tools.ant.filters.FixCrLfFilter
	import org.apache.tools.ant.filters.ReplaceTokens
	
	task filter(type: Copy) {
	    from 'src/main/webapp'
	    into 'build/explodedWar'
	    // Substitute property tokens in files
	    expand(copyright: '2009', version: '2.3.1')
	    expand(project.properties)
	    // Use some of the filters provided by Ant
	    filter(FixCrLfFilter)
	    filter(ReplaceTokens, tokens: [copyright: '2009', version: '2.3.1'])
	    // Use a closure to filter each line
	    filter { String line ->
	        "[$line]"
	    }
	    // Use a closure to remove lines
	    filter { String line ->
	        line.startsWith('-') ? null : line
	    }
	    filteringCharset = 'UTF-8'
	}

- 当使用`ReplaceTokens`类的`filter`操作符，结果是一个模板，用一组给定的值替换有`@tokenName`形式的标记。`expand`操作符有同样的作用，只是它将源文件当做Groovy模板，且标记以`${tokenName}`形式出现，当使用这种方式时可能需要转义部分源文件，例如包含了`$`或`<%`字符串

- 通过`filteringCharset`指定编码规则，这在读写文件时是一个好习惯，如果未指定的话，默认会使用JVM默认的编码

## 6.3 使用`CopySpec`类
复制规范形成一个层次结构。`copy spec`继承其目标路径，`include`模式，`exclude`模式，复制action，名称映射和过滤器

**示例1：**Nested copy specs

	task nestedSpecs(type: Copy) {
	    into 'build/explodedWar'
	    exclude '**/*staging*'
	    from('src/dist') {
	        include '**/*.html'
	    }
	    into('libs') {
	        from configurations.runtime
	    }
	}

# 7 使用Sync任务
`Sync`任务继承自`Copy`任务。它会将源文件复制到目标路径，然后删除它没有复制的文件。这可以用于安装应用等操作

**示例1：**

	task libs(type: Sync) {
	    from configurations.runtime
	    into "$buildDir/libs"
	}

# 8 创建archives
一个项目可以拥有尽可能多的`Jar`archives.同样也可以添加`WAR`,`ZIP`,`TAR`到项目中。

`archives`可以利用`Zip`,`Tar`,`Jar`,`War`,`Ear`等任务进行创建。他们都以同样的方式工作，所以这里只展示一个

**示例1：**创建一个ZIP形式的压缩档

	apply plugin: 'java'
	
	task zip(type: Zip) {
	    from 'src/dist'
	    into('libs') {
	        from configurations.runtime
	    }
	}

- 这里之所以使用java 插件，是因为它会为`archives`任务添加默认的一些值.当然不使用java插件也是支持的。

- `Sync`任务和`Copy`任务的工作方式完全相同，都实现了`CopySpec`接口。


## 8.1 存档命名
默认存档名称使用格式`projectName-version.type`生成

**示例1**创建zip压缩文件

	apply plugin: 'java'
	
	version = 1.0
	
	task myZip(type: Zip) {
	    from 'somedir'
	}
	
	println myZip.archiveName
	println relativePath(myZip.destinationDir)
	println relativePath(myZip.archivePath)
	
	> gradle -q myZip
	zipProject-1.0.zip
	build/distributions
	build/distributions/zipProject-1.0.zip

- 默认生成的zip压缩文件可以使用`archivesBaseName`属性修改生成的压缩文件名称

**示例2**：custom archive name

	apply plugin: 'java'
	version = 1.0
	
	task myZip(type: Zip) {
	    from 'somedir'
	    baseName = 'customName'
	}
	
	println myZip.archiveName

	> gradle -q myZip
	customName-1.0.zip

**示例3**：add appendix & classifier

	apply plugin: 'java'
	archivesBaseName = 'gradle'
	version = 1.0
	
	task myZip(type: Zip) {
	    appendix = 'wrapper'
	    classifier = 'src'
	    from 'somedir'
	}
	
	println myZip.archiveName

	> gradle -q myZip
	gradle-wrapper-1.0-src.zip


[更多的存档命名查看Table 20.1](https://docs.gradle.org/current/userguide/working_with_files.html)

## 8.2 在多个archive之间共享内容
通过使用`Project.copySpec(org.gradle.api.Action)`方法

## 8.3 Reproducible archives

	tasks.withType(AbstractArchiveTask) {
	    preserveFileTimestamps = false
	    reproducibleFileOrder = true
	}

# 9 属性文件
属性文件在Java开发过程中经常使用。Gradle将其作为构建的一部分使得创建属性文件更加简单。可以使用`WriteProperties`任务去创建属性文件

`WriteProperties`任务修复了