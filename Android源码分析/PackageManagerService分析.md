# PackageManagerService分析

[深入理解Android 卷ii]()

[SystemServer分析.md](https://github.com/iwantthink/TODO/blob/master/Android%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/SystemServer%E5%88%86%E6%9E%90.md)

[Android SystemUI源码](https://android.googlesource.com/platform/frameworks/base/+/master/packages/SystemUI/)


# 1. 简介

`PackageManagerService`作为系统核心服务,由`SystemServer`创建,具体可以查看[SystemServer分析.md]()

`PackageManagerService`的主要创建过程是调用PMKS的静态方法`main()`,然后调用`systemReady()`方法通知系统进入就绪状态

Android 27 


# 2. PMKS.main()

    public static PackageManagerService main(Context context, Installer installer,
            boolean factoryTest, boolean onlyCore) {
        // Self-check for initial settings.
		// 检查一些系统参数
        PackageManagerServiceCompilerMapping.checkProperties();

		// factoryTest 和 onlyCore均为false
        PackageManagerService m = new PackageManagerService(context, installer,
                factoryTest, onlyCore);
        m.enableSystemUserPackages();
		// 向 ServiceManager 注册
        ServiceManager.addService("package", m);
        final PackageManagerNative pmn = m.new PackageManagerNative();
        ServiceManager.addService("package_native", pmn);
        return m;
    }

- 参数`Installer`, 在`SystemServer`中被创建,主要用来和`Native`进程`installd`进行交互. (老版本会在PMKS中创建)

- `main()`函数比较简单,但是执行时间却较长,主要原因是`PKMS`在其构造函数中做了许多 耗时的操作

- PKMS的构造函数的主要功能:

	扫描Android系统中几个目标文件夹中的apk,从而建立合适的数据结构以管理如Package信息,四大组件信息,权限信息等各种信息

	抽象的来看,PKMS像一个加工厂,解析实际的物理文件(APK文件)以生成符合要求的产品. 例如,PKMS将解析APK包中的`AndroidManifest.xml`,并根据其中声明的`Activity`标签来创建与此对应的对象并加以保管

- PKMS构造函数的工作流程:

	1. 扫描目标文件夹之前的准备工作

	2. 扫描目标文件夹

	3. 扫描之后的工作

	- 或者根据系统自带的log进行步骤区分

		1. `BOOT_PROGRESS_PMS_START`
	
		2. `BOOT_PROGRESS_PMS_SYSTEM_SCAN_START`
	
		3. `BOOT_PROGRESS_PMS_DATA_SCAN_START`
	
		4. `BOOT_PROGRESS_PMS_SCAN_END`
	
		5. `BOOT_PROGRESS_PMS_READY`

# 3. 前期准备工作

    public PackageManagerService(Context context, Installer installer,
            boolean factoryTest, boolean onlyCore) {

        LockGuard.installLock(mPackages, LockGuard.INDEX_PACKAGES);

        EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_PMS_START,
                SystemClock.uptimeMillis());

		// mSdkVersion 是PMKS 的成员变量,定义的时候进行赋值,其值取自系统属性 ro.build.version.sdk
		// 如果没有定义该值,则APK无法知道自己运行在Android 哪个版本上
        if (mSdkVersion <= 0) {
            Slog.w(TAG, "**** ro.build.version.sdk not set!");
        }

        mContext = context;

        mPermissionReviewRequired = context.getResources().getBoolean(
                R.bool.config_permissionReviewRequired);
		// 假定false , 运行在非工厂模式下
        mFactoryTest = factoryTest;
		// 假定false , 运行在 普通模式下
        mOnlyCore = onlyCore;
		// 用来存储与显示屏相关的一些属性,例如屏幕的宽/高,分辨率等信息
        mMetrics = new DisplayMetrics();

		// Settings 是Android 的全局管理者,用于协助PMS保存所有的安装包信息
		// 用于存储系统运行过程中的一些设置
        mSettings = new Settings(mPackages);
        mSettings.addSharedUserLPw("android.uid.system", Process.SYSTEM_UID,
                ApplicationInfo.FLAG_SYSTEM, ApplicationInfo.PRIVATE_FLAG_PRIVILEGED);

        mSettings.addSharedUserLPw("android.uid.phone", RADIO_UID,
                ApplicationInfo.FLAG_SYSTEM, ApplicationInfo.PRIVATE_FLAG_PRIVILEGED);

        mSettings.addSharedUserLPw("android.uid.log", LOG_UID,
                ApplicationInfo.FLAG_SYSTEM, ApplicationInfo.PRIVATE_FLAG_PRIVILEGED);

        mSettings.addSharedUserLPw("android.uid.nfc", NFC_UID,
                ApplicationInfo.FLAG_SYSTEM, ApplicationInfo.PRIVATE_FLAG_PRIVILEGED);

        mSettings.addSharedUserLPw("android.uid.bluetooth", BLUETOOTH_UID,
                ApplicationInfo.FLAG_SYSTEM, ApplicationInfo.PRIVATE_FLAG_PRIVILEGED);

        mSettings.addSharedUserLPw("android.uid.shell", SHELL_UID,
                ApplicationInfo.FLAG_SYSTEM, ApplicationInfo.PRIVATE_FLAG_PRIVILEGED);

		********省略代码*********

	}

- `Settings` 的作用是管理Android系统运行中的一些设置信息

- `addSharedUserLPw()`的四个参数:

	1. 字符串,表示`sharedUserId`

	2. 系统进程使用的用户id,值为1000
	
	3. 标志系统Package

	4. 允许使用`PRIVATE_FLAG_PRIVILEGED` 权限


## 3.1 Settings分析

### 3.1.1 UID/GID

- UID为用户ID的缩写,GID为用户组ID的缩写,这俩个概念与Linux系统中进程的权限管理有关. 

- 一般来说每一个进程都会有一个对应的UID(即表示该进程属于哪个用户,不同用户拥有不同的权限). 

- 一个进程也可以分属不同的用户组(每个用户组都有对应的权限)

在Android平台中,系统定义的`UID/GID`保存在`Process.java`文件中

- 可以通过`adb shell`登录手机,使用`busybox`提供的`ps`命令查看进程的uid


### 3.1.2 sharedUserId介绍

[SystemUI源码](https://android.googlesource.com/platform/frameworks/base/+/master/packages/SystemUI/)

查看一下`SystemUI`的`AndroidManifest.xml`

	<manifest xmlns:android="http://schemas.android.com/apk/res/android"
	        xmlns:androidprv="http://schemas.android.com/apk/prv/res/android"
	        package="com.android.systemui"
	        android:sharedUserId="android.uid.systemui"
	        coreApp="true">
		..........
	</manifest>
	
- 声明了一个名为`android:sharedUserId`的属性,其值为`android.uid.systemui`. `sharedUserId`与`UID`有关,作用是:

	1. 俩个或多个声明了同一种`sharedUserId`的apk可共享彼此的数据,并且运行在同一进程中

### 3.1.3 数据结构分析

1. `Settings`类定义了一个`mSharedUsers`成员,它是一个`ArrayMap`,以字符串(`android.uid.system`)为key,对应的value是一个`SharedUserSetting`对象

2. `SharedUserSetting`派生自`GrantedPermission`类,其与权限有关. `SharedUserSetting`定义了一个成员变量`packages`,类型为`ArraySet`,用于保存声明了相同`sharedUserId`的`Package`的权限设置信息

3. 每个`Package`有自己的权限设置. 权限的概念由`PackageSetting`类表达. 该类继承自`PackagesettingBase  -> GrantedPermissions`

4. `Settings`类中还有俩个成员,`mUserIds`和`mOtherUserIds` , 这俩位成员的类型分别是`Arraylist`和`SparseArray`. 其目的是以UID为索引,得到对应的`SharedUserSettings`对象.

	一般情况下,以索引获取数组元素的速度 会比 以key获取`HashMap`中元素的速度要快. **这种就是空间换时间的做法**


### 3.1.4 Settings.addSharedUserLPw函数

    SharedUserSetting addSharedUserLPw(String name, int uid, int pkgFlags, int pkgPrivateFlags) {
		// mSharedUsers 是一个 ArrayMap
        SharedUserSetting s = mSharedUsers.get(name);
        if (s != null) {
			// 重复添加,且值相同
            if (s.userId == uid) {
                return s;
            }
			// 重复添加了指定值,但是前后值不同,保存第一次添加的值
			// 打印log
            PackageManagerService.reportSettingsProblem(Log.ERROR,
                    "Adding duplicate shared user, keeping first: " + name);
            return null;
        }
		// 创建新的SharedUserSetting 用来保存值
        s = new SharedUserSetting(name, pkgFlags, pkgPrivateFlags);
        s.userId = uid;
        if (addUserIdLPw(uid, s, name)) {
            mSharedUsers.put(name, s);
            return s;
        }
        return null;
    }

### 3.1.5 Settings.addUserIdLPw()


    private boolean addUserIdLPw(int uid, Object obj, Object name) {
		// uid不能超出限制范围
		// Android 对uid进行了分类 ,应用APK 所在进程的uid从 10000 开始到19999结束,而系统APk所在进程的uid 小于10000 
        if (uid > Process.LAST_APPLICATION_UID) {
			// 无效的应用uid
            return false;
        }
		// uid 从10000 开始才属于应用的uid
        if (uid >= Process.FIRST_APPLICATION_UID) {
			// 当前集合大小
            int N = mUserIds.size();
			// 计算索引 其值为 uid 与 FIRST_APPLICATION_UID(即 10000) 的差
            final int index = uid - Process.FIRST_APPLICATION_UID;
			// 对mUserIds 进行扩容
            while (index >= N) {
                mUserIds.add(null);
                N++;
            }
			// 非空则报错,重复添加
            if (mUserIds.get(index) != null) {
                return false;
            }
			// 保存应用 Package的 uid
            mUserIds.set(index, obj);
        } else {
			// 剩下的属于系统的uid 添加到 mOtherUserIds

			// 重复添加....
            if (mOtherUserIds.get(uid) != null) {
                return false;
            }
            mOtherUserIds.put(uid, obj);
        }
        return true;
    }

# 4. XML文件扫描

	public PackageManagerService(Context context, Installer installer,
	        boolean factoryTest, boolean onlyCore) {
	
		........省略 3.0 前期准备工作的代码...........

		// 获取 debug.separate_processes属性, 该值和调试有关
		// 如果设置了这个属性,会强制应用组件在自己的进程中运行,一般不会设置
        String separateProcesses = SystemProperties.get("debug.separate_processes");
        if (separateProcesses != null && separateProcesses.length() > 0) {
			// 所有process都设置该属性
            if ("*".equals(separateProcesses)) {
                mDefParseFlags = PackageParser.PARSE_IGNORE_PROCESSES;
                mSeparateProcesses = null;
            } else {
				// 个别
                mDefParseFlags = 0;
                mSeparateProcesses = separateProcesses.split(",");
            }
        } else {
			// 不设置,一般会走这里
            mDefParseFlags = 0;
            mSeparateProcesses = null;
        }
			
		// 用来和Native进程installd交互
        mInstaller = installer;

		// 用于dex优化
        mPackageDexOptimizer = new PackageDexOptimizer(installer, mInstallLock, context,
                "*dexopt*");
        mDexManager = new DexManager(this, mPackageDexOptimizer, installer, mInstallLock);
        mMoveCallbacks = new MoveCallbacks(FgThread.get().getLooper());

        mOnPermissionChangeListeners = new OnPermissionChangeListeners(
                FgThread.get().getLooper());

		// 通过DisplayManager获取默认的显示信息,保存到Metrics
        getDefaultDisplayMetrics(context, mMetrics);

		// 获取系统配置信息
        SystemConfig systemConfig = SystemConfig.getInstance();
        mGlobalGids = systemConfig.getGlobalGids();
        mSystemPermissions = systemConfig.getSystemPermissions();
        mAvailableFeatures = systemConfig.getAvailableFeatures();
        Trace.traceEnd(TRACE_TAG_PACKAGE_MANAGER);

        mProtectedPackages = new ProtectedPackages(mContext);

        synchronized (mInstallLock) {
        // writer
        synchronized (mPackages) {
			// 创建名称为"PackageManager"的handler线程
			// 实际上就是创建了一个开启了Looper 的线程
			// 该线程的工作是: 程序的安装和卸载等
            mHandlerThread = new ServiceThread(TAG,
                    Process.THREAD_PRIORITY_BACKGROUND, true /*allowIo*/);
            mHandlerThread.start();

			// 用于处理外部的安装请求等消息,将线程切换到"PackageManager"去执行
			// 比如adb install ,packageinstaller安装apk时
            mHandler = new PackageHandler(mHandlerThread.getLooper());
            mProcessLoggingHandler = new ProcessLoggingHandler();
			// 监听超时
            Watchdog.getInstance().addThread(mHandler, WATCHDOG_TIMEOUT);

            mDefaultPermissionPolicy = new DefaultPermissionGrantPolicy(this);
            mInstantAppRegistry = new InstantAppRegistry(this);

			// 创建各种目录
            File dataDir = Environment.getDataDirectory();
			// 指向 /data/app/ 
            mAppInstallDir = new File(dataDir, "app");
            mAppLib32InstallDir = new File(dataDir, "app-lib");
            mAsecInternalPath = new File(dataDir, "app-asec").getPath();
            mDrmAppPrivateInstallDir = new File(dataDir, "app-private");

			// 创建用户管理服务
            sUserManager = new UserManagerService(context, this,
                    new UserDataPreparer(mInstaller, mInstallLock, mContext, mOnlyCore), mPackages);

            // Propagate permission configuration in to package manager.
			// 从系统配置中获取到权限列表
            ArrayMap<String, SystemConfig.PermissionEntry> permConfig
                    = systemConfig.getPermissions();
            for (int i=0; i<permConfig.size(); i++) {
                SystemConfig.PermissionEntry perm = permConfig.valueAt(i);
                BasePermission bp = mSettings.mPermissions.get(perm.name);
                if (bp == null) {
                    bp = new BasePermission(perm.name, "android", BasePermission.TYPE_BUILTIN);
                    mSettings.mPermissions.put(perm.name, bp);
                }
                if (perm.gids != null) {
                    bp.setGids(perm.gids, perm.perUser);
                }
            }

			// 获取共享库
            ArrayMap<String, String> libConfig = systemConfig.getSharedLibraries();
            final int builtInLibCount = libConfig.size();
            for (int i = 0; i < builtInLibCount; i++) {
                String name = libConfig.keyAt(i);
                String path = libConfig.valueAt(i);
                addSharedLibraryLPw(path, null, name, SharedLibraryInfo.VERSION_UNDEFINED,
                        SharedLibraryInfo.TYPE_BUILTIN, PLATFORM_PACKAGE_NAME, 0);
            }

            mFoundPolicyFile = SELinuxMMAC.readInstallPolicy();


            mFirstBoot = !mSettings.readLPw(sUserManager.getUsers(false));

            // Clean up orphaned packages for which the code path doesn't exist
            // and they are an update to a system app - caused by bug/32321269
            final int packageSettingCount = mSettings.mPackages.size();
            for (int i = packageSettingCount - 1; i >= 0; i--) {
                PackageSetting ps = mSettings.mPackages.valueAt(i);
                if (!isExternal(ps) && (ps.codePath == null || !ps.codePath.exists())
                        && mSettings.getDisabledSystemPkgLPr(ps.name) != null) {
                    mSettings.mPackages.removeAt(i);
                    mSettings.enableSystemPackageLPw(ps.name);
                }
            }

            if (mFirstBoot) {
                requestCopyPreoptedFiles();
            }

            String customResolverActivity = Resources.getSystem().getString(
                    R.string.config_customResolverActivity);
            if (TextUtils.isEmpty(customResolverActivity)) {
                customResolverActivity = null;
            } else {
                mCustomResolverComponentName = ComponentName.unflattenFromString(
                        customResolverActivity);
            }

            long startTime = SystemClock.uptimeMillis();
		.........................
	}

## 4.1 SystemConfig

### 4.1.1 SystemConfig 构造函数

    SystemConfig() {
        // Read configuration from system
        readPermissions(Environment.buildPath(
                Environment.getRootDirectory(), "etc", "sysconfig"), ALLOW_ALL);
        // Read configuration from the old permissions dir
        readPermissions(Environment.buildPath(
                Environment.getRootDirectory(), "etc", "permissions"), ALLOW_ALL);
		........省略剩下的读取权限信息.......
    }

- `Environment.getOdmDirectory()`,`Environment.getRootDirectory()`,`Environment.getVendorDirectory`,`Environment.getOemDirectory`  四个方法就是获取不同的根目录

- 例如`Environment.buildPath(Environment.getRootDirectory(), "etc", "permissions")`, 就是从`system/etc/permissions`目录中读取

### 4.1.2 SystemConfig.readPermissions()

    void readPermissions(File libraryDir, int permissionFlag) {
        // Read permissions from given directory.
        if (!libraryDir.exists() || !libraryDir.isDirectory()) {
            if (permissionFlag == ALLOW_ALL) {
				// 传入的文件夹 无效
            }
            return;
        }
		// 是否能够读取
        if (!libraryDir.canRead()) {
            return;
        }

        // Iterate over the files in the directory and scan .xml files
        File platformFile = null;
        for (File f : libraryDir.listFiles()) {
            // 最后再处理 platform.xml文件
            if (f.getPath().endsWith("etc/permissions/platform.xml")) {
                platformFile = f;
                continue;
            }

			// 不处理 非 .xml 结尾的文件
            if (!f.getPath().endsWith(".xml")) {
                continue;
            }
			// 判断文件是否可以读取
            if (!f.canRead()) {
                continue;
            }
			// 解析 xml文件
            readPermissionsFromXml(f, permissionFlag);
        }

		// 最后再读取这个文件,所以它的优先级最高
        if (platformFile != null) {
            readPermissionsFromXml(platformFile, permissionFlag);
        }
    }

- 实际上就是调用`readPermissionsFromXml()`方法去解析指定系统目录下的XML文件



### 4.1.3 SystemConfig.readPermissionsFromXml()

代码过长,不展示了,实际的作用就是:将XML文件中的标签以及他们之间的关系转换成代码中的相应数据结构

`platform.xml`的详细内容:
	
	<?xml version="1.0" encoding="utf-8"?>
	<permissions>
		// 建立权限名与gid的映射关系
		// 例如下面BLUETOOTH_ADMIN 权限,对应的用户组是 net_bt_admin
		// 该文件中的permission标签仅对那些需要通过读写设备(蓝牙/camera)/创建socket等进程划分了gid
		// 因为这些权限涉及和Linux内核交互,所以需要在底层权限(由不同的用户组界定)和Android层权限(由不同的字符串界定)之间建立映射关系
	    <permission name="android.permission.BLUETOOTH_ADMIN" >
	        <group gid="net_bt_admin" />
	    </permission>
		.......省略剩下的permission标签......
	
		// 赋予指定uid 相应的权限,就是把它加入到对应的用户组中  
		// 例如,如果uid 为 media,就给予它 android.permission.WAKE_LOCK 权限
	    <assign-permission name="android.permission.WAKE_LOCK" uid="media" />

	
	    <!-- This is a list of all the libraries available for application
	         code to link against. -->
	
	    <library name="android.test.runner"
	            file="/system/framework/android.test.runner.jar" />
	    <library name="javax.obex"
	            file="/system/framework/javax.obex.jar"/>
	
	    <!-- These are the standard packages that are white-listed to always have internet
	         access while in power save mode, even if they aren't in the foreground. -->
	    <allow-in-power-save package="com.android.providers.downloads" />
	</permissions>