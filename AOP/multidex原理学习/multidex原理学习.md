# Android Multidex实现原理解析
[实现原理解析-源码分析](http://allenfeng.com/2016/11/17/principle-analysis-on-multidex/)

[配置方法数超过64k的应用-官方](https://developer.android.com/studio/build/multidex.html?hl=zh-cn)

[Dex分包方案](http://www.jianshu.com/p/a67a560903fa)

[Android关于Dex拆分(MultiDex)技术详解](http://www.wjdiankong.cn/android%E5%85%B3%E4%BA%8Edex%E6%8B%86%E5%88%86multidex%E6%8A%80%E6%9C%AF%E8%AF%A6%E8%A7%A3/)
# 1 介绍
当应用及其引用的库达到特定大小时，会遇到构建错误，指明应用已达到 Android 应用构建架构的极限。

早期版本的构建系统按如下方式报告这一错误：

	Conversion to Dalvik format failed:
	Unable to execute dex: method ID not in [0, 0xffff]: 65536

较新版本的 Android 构建系统虽然显示的错误不同，但指示的是同一问题：
	
	trouble writing output:
	Too many field references: 131000; max is 65536.
	You may try using --multi-dex option.

## 1.1 关于64K引用限制
Android 应用 (APK) 文件包含 `Dalvik Executable (DEX)` 文件形式的可执行字节码文件，其中包含用来运行应用的已编译代码。`Dalvik Executable `规范将可在单个 DEX 文件内可引用的方法总数限制在 65,536，其中包括 Android 框架方法、库方法以及自己代码中的方法。在计算机科学领域内，术语千（简称 K）表示 1024（或 2^10）。由于 65,536 等于 64 X 1024，因此这一限制也称为“64K 引用限制”。

## 1.2  Android 5.0 之前版本的Dalvik可执行文件分包支持
Android5.0(API 21)之前的平台版本使用Dalvik运行时 来执行应用代码。默认情况下，Dalvik限制应用的每个APK只能使用单个classes.dex字节码文件，可以使用**Dalvik可执行文件分包支持库**来实现。

## 1.3 Android 5.0 及更高版本的 Dalvik 可执行文件分包支持
Android 5.0（API 级别 21）及更高版本使用名为 ART 的运行时，原生支持从 APK 文件加载多个 DEX 文件。ART 在应用安装时执行预编译，扫描 classesN.dex 文件，并将它们编译成单个 .oat 文件，供 Android 设备执行。因此，如果 minSdkVersion 为 21 或更高值，则不需要 Dalvik 可执行文件分包支持库。

## 1.4 配置应用进行Dalvik可执行文件分包

- 如果miniSdkVersion >=21,只需要在module内的build.gradle将multiDexEnabled设置为true即可(该属性在defaultConfig内)

- 如果miniSdkVersion <21
	- 首先在module内的build.gradle 将multiDexEnabled设置为true，另外在依赖script block 中添加 `  compile 'com.android.support:multidex:1.0.1'`
	- 根据是否要替换Application	
		- 默认没有使用Application,则只用在清单文件中将标签application的name设置成`android.support.multidex.MultiDexApplication`即可
		- 如果使用了Application，可以根据是否可以替换基本类
			- 如果可以替换，直接将原本继承Application 更改为继承自MultiDexApplication
			- 如果不可以替换，可以改为替换`attachBaseContext()`方法，并调用`MultiDex.install(this)`启动Dalvik可执行文件分包

					public class MyApplication extends SomeOtherApplication {
					  @Override
					  protected void attachBaseContext(Context base) {
					     super.attachBaseContext(context);
					     Multidex.install(this);
					  }
					}

## 1.5 其他说明
**Dalvik可执行文件分包支持库有其局限性，具体请查看官方文档**

# 2 实现原理分析

无论使用哪种方式实现MultiDex，使用MultiDexApplication 或者是 重写`attachBaseContext`,最终都会调用`MultiDex.install(this)`

	
	    public static void install(Context context) {
	        Log.i("MultiDex", "Installing application");
	        if(IS_VM_MULTIDEX_CAPABLE) {
	            Log.i("MultiDex", "VM has multidex support, MultiDex support library is disabled.");
	        } else if(VERSION.SDK_INT < 4) {
	            throw new RuntimeException("MultiDex installation failed. SDK " + VERSION.SDK_INT + " is unsupported. Min SDK version is " + 4 + ".");
	        } else {
	            try {
	                ApplicationInfo applicationInfo = getApplicationInfo(context);
	                if(applicationInfo == null) {
	                    Log.i("MultiDex", "No ApplicationInfo available, i.e. running on a test Context: MultiDex support library is disabled.");
	                    return;
	                }
	
	                doInstallation(context, new File(applicationInfo.sourceDir), new File(applicationInfo.dataDir), "secondary-dexes", "");
	            } catch (Exception var2) {
	                Log.e("MultiDex", "MultiDex installation failure", var2);
	                throw new RuntimeException("MultiDex installation failed (" + var2.getMessage() + ").");
	            }
	
	            Log.i("MultiDex", "install done");
	        }
	    }

- `IS_VM_MULTIDEX_CAPABLE` 字段是通过`isVMMultidexCapable(System.getProperty("java.vm.version"))`方法进行判断当前vm 是否支持 MULTIDEX。应该是2.2.x以上以上的vm支持MultiDex。

	   static boolean isVMMultidexCapable(String versionString) {
	        boolean isMultidexCapable = false;
	        if(versionString != null) {
	            Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
	            if(matcher.matches()) {
	                try {
	                    int major = Integer.parseInt(matcher.group(1));
	                    int minor = Integer.parseInt(matcher.group(2));
	                    isMultidexCapable = major > 2 || major == 2 && minor >= 1;
	                } catch (NumberFormatException var5) {
	                    ;
	                }
	            }
	        }
	
	        Log.i("MultiDex", "VM with version " + versionString + (isMultidexCapable?" has multidex support":" does not have multidex support"));
	        return isMultidexCapable;
	    }

- Multidex 最低支持到Android 1.6 (API 4).低于这个版本 会抛出RunTimeException

取出ApplicationInfo 之后会去 调用 `doInstallation(context, new File(applicationInfo.sourceDir), new File(applicationInfo.dataDir), "secondary-dexes", "");`方法。

- applicationInfo.sourceDir。基本APK的完整路径 `/data/app/com.hmt.analytics.customizeplugin-1/base.apk`

- applicationInfo.dataDir 。持久数据的本地存储路径 `/data/data/com.hmt.analytics.customizeplugin`

## 2.1 doInstallation()

	 private static void doInstallation(Context mainContext, File sourceApk, File dataDir, String secondaryFolderName, String prefsKeyPrefix) throws IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
	        Set var5 = installedApk;
	        synchronized(installedApk) {
	            if(!installedApk.contains(sourceApk)) {
	                installedApk.add(sourceApk);
	                if(VERSION.SDK_INT > 20) {
	                    Log.w("MultiDex", "MultiDex is not guaranteed to work in SDK version " + VERSION.SDK_INT + ": SDK version higher than " + 20 + " should be backed by " + "runtime with built-in multidex capabilty but it's not the " + "case here: java.vm.version=\"" + System.getProperty("java.vm.version") + "\"");
	                }
	
	                ClassLoader loader;
	                try {
	                    loader = mainContext.getClassLoader();
	                } catch (RuntimeException var11) {
	                    Log.w("MultiDex", "Failure while trying to obtain Context class loader. Must be running in test mode. Skip patching.", var11);
	                    return;
	                }
	
	                if(loader == null) {
	                    Log.e("MultiDex", "Context class loader is null. Must be running in test mode. Skip patching.");
	                } else {
	                    try {
	                        clearOldDexDir(mainContext);
	                    } catch (Throwable var10) {
	                        Log.w("MultiDex", "Something went wrong when trying to clear old MultiDex extraction, continuing without cleaning.", var10);
	                    }
	
	                    File dexDir = getDexDir(mainContext, dataDir, secondaryFolderName);
	                    List<? extends File> files = MultiDexExtractor.load(mainContext, sourceApk, dexDir, prefsKeyPrefix, false);
	                    installSecondaryDexes(loader, dexDir, files);
	                }
	            }
	        }
	    }

- 如果sdk version 大于Android 4.4(APi 20) 不保证能使用。。。

- 获取一个`ClassLoader`,待Patch的class loader应该是BaseDexClassLoader的子类，Multidex主要通过修改pathList字段来添加更多的dex。

- MultiDex的二级dex文件将存放在`/data/data/<package-name>/secondary-dexes`

- `clearOldDexDir(mainContext)`方法 会去删除 context.getFilesDir()路径下的 dex文件。。。`/data/data/<package name>/files/secondary-dexes`

- `getDexDir(mainContext, dataDir, secondaryFolderName);`方法会去获取dexDir的File.
	- 先去创建`/data/data/com.hmt.analytics.customizeplugin/code_cache`这个目录，如果创建失败 会使用 `/data/data/com.hmt.analytics.customizeplugin/files/code_cache`作为目录。
	- 最终 在 目录下创建 `secondary-dexes`文件夹.`/data/data/com.hmt.analytics.customizeplugin/code_cache/secondary-dexes`。

- 调用`MultiDexExtractor.load(mainContext, sourceApk, dexDir, prefsKeyPrefix, false)`获取apk中可用的二级dex列表

- 调用`installSecondaryDexes(loader, dexDir, files);`开始安装dex.

### 2.1.1 MultiDexExtractor.load(mainContext, sourceApk, dexDir, prefsKeyPrefix, false)

	    static List<? extends File> load(Context context, File sourceApk, File dexDir, String prefsKeyPrefix, boolean forceReload) throws IOException {
	        Log.i("MultiDex", "MultiDexExtractor.load(" + sourceApk.getPath() + ", " + forceReload + ", " + prefsKeyPrefix + ")");
	        long currentCrc = getZipCrc(sourceApk);
	        File lockFile = new File(dexDir, "MultiDex.lock");
	        RandomAccessFile lockRaf = new RandomAccessFile(lockFile, "rw");
	        FileChannel lockChannel = null;
	        FileLock cacheLock = null;
	        IOException releaseLockException = null;
	
	        List files;
	        try {
	            lockChannel = lockRaf.getChannel();
	            Log.i("MultiDex", "Blocking on lock " + lockFile.getPath());
	            cacheLock = lockChannel.lock();
	            Log.i("MultiDex", lockFile.getPath() + " locked");
	            if(!forceReload && !isModified(context, sourceApk, currentCrc, prefsKeyPrefix)) {
	                try {
	                    files = loadExistingExtractions(context, sourceApk, dexDir, prefsKeyPrefix);
	                } catch (IOException var21) {
	                    Log.w("MultiDex", "Failed to reload existing extracted secondary dex files, falling back to fresh extraction", var21);
	                    files = performExtractions(sourceApk, dexDir);
	                    putStoredApkInfo(context, prefsKeyPrefix, getTimeStamp(sourceApk), currentCrc, files);
	                }
	            } else {
	                Log.i("MultiDex", "Detected that extraction must be performed.");
	                files = performExtractions(sourceApk, dexDir);
	                putStoredApkInfo(context, prefsKeyPrefix, getTimeStamp(sourceApk), currentCrc, files);
	            }
	        } finally {
	            if(cacheLock != null) {
	                try {
	                    cacheLock.release();
	                } catch (IOException var20) {
	                    Log.e("MultiDex", "Failed to release lock on " + lockFile.getPath());
	                    releaseLockException = var20;
	                }
	            }
	
	            if(lockChannel != null) {
	                closeQuietly(lockChannel);
	            }
	
	            closeQuietly(lockRaf);
	        }
	
	        if(releaseLockException != null) {
	            throw releaseLockException;
	        } else {
	            Log.i("MultiDex", "load found " + files.size() + " secondary dex files");
	            return files;
	        }
	    }

- 获取base.APK(zip文件)的 CRC 标识 

- FileLock是独占锁，控制不同程序(JVM)对同一文件的并发访问。

- 会判断上次保存的apk（zip文件）的CRC校验码和last modify日期与dex的总数量是否与当前apk相同，如果不同则需要重新解压去获取dex。此外，forceReload也会决定是否需要重新解压。

	- 如果apk被修改过，会通过 `performExtractions()`方法获取dex.并调用`putStoredApkInfo`将解压出来的信息进行保存,等待下一次使用时做验证.

	- 如果apk未被修改,会通过`loadExistingExtractions()`直接加载上次解压出来的文件.

### 2.1.2 installSecondaryDexes(loader, dexDir, files)

    private static void installSecondaryDexes(ClassLoader loader, File dexDir, List<? extends File> files) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IOException {
        if(!files.isEmpty()) {
            if(VERSION.SDK_INT >= 19) {
                MultiDex.V19.install(loader, files, dexDir);
            } else if(VERSION.SDK_INT >= 14) {
                MultiDex.V14.install(loader, files, dexDir);
            } else {
                MultiDex.V4.install(loader, files);
            }
        }
    }

- 根据不同的sdk版本，需要采取不同的实现方式去将**二级dex添加到classLoader中**,调用不同的静态内部类的install方法去实现。大体实现类似相似，**以下分析`v14`这种情况**

- loader:通过context.getClassLoader获取的默认类加载器

- files:二级dex文件解压后的路径

- dexDir:对应`/data/data/<pkg-name>/code_cache/secondary-dexes/`

- MultiDex.V14.install()

        private static void install(ClassLoader loader, List<? extends File> additionalClassPathEntries, File optimizedDirectory) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            Field pathListField = MultiDex.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            MultiDex.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new ArrayList(additionalClassPathEntries), optimizedDirectory));
        }

- 首先调用MultiDex.findField()通过反射去获得ClassLoader对象的pathList字段（类型为DexPathList）,并获取其值.然后调用`MultiDex.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new ArrayList(additionalClassPathEntries), optimizedDirectory))`

	 - 三个参数 dexPathList(通过反射获取的),"dexElements"(fieldName),Object[]. 其中第三个参数是通过`makeDexElements(dexPathList, new ArrayList(additionalClassPathEntries), optimizedDirectory)`方法获取的
	 - `makeDexElements()` 通过反射获取makeDexElements方法，然后执行,会返回一个`Element[]`
	 - `makeDexElements()`方法主要是将获得的二级dex文件列表封装成Element[]

---
	private static void expandFieldArray(Object instance, String fieldName, Object[] extraElements) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
	        Field jlrField = findField(instance, fieldName);
	        Object[] original = (Object[])((Object[])jlrField.get(instance));
	        Object[] combined = (Object[])((Object[])Array.newInstance(original.getClass().getComponentType(), original.length + extraElements.length));
	        System.arraycopy(original, 0, combined, 0, original.length);
	        System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
	        jlrField.set(instance, combined);
	}

- `expandFieldArray()`方法中，通过反射 获取`pathList`上的`dexElements`字段，并获取其值。

- 创建一个`dexElements`类型的数组，作为中介。

- 将`dexElements`数组复制到中介数组，将Element[]也复制到中介数组。

- 将中介数组的值设置到`DexPathList`对象的`dexElements`字段上.


- /libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java
		
		public class BaseDexClassLoader extends ClassLoader {
		    ...
		    /** structured lists of path elements */
		    private final DexPathList pathList;
		    ...
		    public BaseDexClassLoader(String dexPath, File optimizedDirectory, String libraryPath, ClassLoader parent) {
		        super(parent);
		        this.originalPath = dexPath;
		        this.originalLibraryPath = libraryPath;
		        this.pathList =
		            new DexPathList(this, dexPath, libraryPath, optimizedDirectory);
		    }
		}

- /libcore/dalvik/src/main/java/dalvik/system/DexPathList.java

		/*package*/ final class DexPathList {
		    ....
		    /**
		     * Makes an array of dex/resource path elements, one per element of
		     * the given array.
		     */
		    private static Element[] makeDexElements(ArrayList<File> files,
		            File optimizedDirectory) {
		        ArrayList<Element> elements = new ArrayList<Element>();
		        /*
		         * Open all files and load the (direct or contained) dex files
		         * up front.
		         */
		        for (File file : files) {
		            File zip = null;
		            DexFile dex = null;
		            String name = file.getName();
		            if (name.endsWith(DEX_SUFFIX)) {
		                // Raw dex file (not inside a zip/jar).
		                try {
		                    dex = loadDexFile(file, optimizedDirectory);
		                } catch (IOException ex) {
		                    System.logE("Unable to load dex file: " + file, ex);
		                }
		            } else if (name.endsWith(APK_SUFFIX) || name.endsWith(JAR_SUFFIX)
		                    || name.endsWith(ZIP_SUFFIX)) {
		                zip = file;
		                try {
		                    dex = loadDexFile(file, optimizedDirectory);
		                } catch (IOException ignored) {
		                    /*
		                     * IOException might get thrown "legitimately" by
		                     * the DexFile constructor if the zip file turns
		                     * out to be resource-only (that is, no
		                     * classes.dex file in it). Safe to just ignore
		                     * the exception here, and let dex == null.
		                     */
		                }
		            } else {
		                System.logW("Unknown file type for: " + file);
		            }
		            if ((zip != null) || (dex != null)) {
		                elements.add(new Element(file, zip, dex));
		            }
		        }
		        return elements.toArray(new Element[elements.size()]);
		    }
		    ...
		}

# 3 总结
- apk在Applicaion实例化之后，会检查系统版本是否支持MultiDex，判断二级dex是否需要安装；

- 如果需要安装则会从apk中解压出classes2.dex并将其拷贝到应用的`/data/data//code_cache/secondary-dexes/`目录下；

- 通过反射将classes2.dex等注入到当前的ClassLoader的pathList中，完成整体安装流程。

# 4 MultiDex之Dex拆分

**dex拆分步骤为：**

1. 自动扫描整个工程代码得到main-dex-list；

2. 根据main-dex-list对整个工程编译后的所有class进行拆分，将主、从dex的class文件分开；

3. 用dx工具对主、从dex的class文件分别打包成 .dex文件，并放在apk的合适目录。
怎么自动生成 main-dex-list？ Android SDK 从 build tools 21 开始提供了 mainDexClasses 脚本来生成主 dex 的文件列表。查看这个脚本的源码，可以看到它主要做了下面两件事情：

	- 调用 proguard 的 shrink 操作来生成一个临时 jar 包；

	- 将生成的临时 jar 包和输入的文件集合作为参数，然后调用com.android.multidex.MainDexListBuilder 来生成主 dex 文件列表。
