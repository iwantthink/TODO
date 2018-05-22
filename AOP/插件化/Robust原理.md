# Robust原理

[Robust WIKI](https://github.com/Meituan-Dianping/Robust/wiki)

[Android热补丁之Robust原理解析(一)](http://w4lle.com/2017/03/31/robust-0/)

[Android热更新方案Robust](https://tech.meituan.com/android_robust.html)

[美团 Robust热更新使用图解](http://blog.csdn.net/dawN4get/article/details/72861966)

[Android studio dependencies依赖的jar包的存储位置](http://blog.csdn.net/a31081314/article/details/78551536)

# 1. 使用方式

参考[Robust-官方文档](https://github.com/Meituan-Dianping/Robust/blob/master/README-zh.md)即可实现，注意除了需要生成 补丁包 以及添加依赖之外。还需要 手动调用加载补丁包的方法`new PatchExecutor(ctx,patchManipulate,robustCallback).start()`

    public PatchExecutor(Context context, PatchManipulate patchManipulate, RobustCallBack robustCallBack)


# 1. 原理解析

1. 打基础包时插桩，在每个类中插入一段类型为 `ChangeQuickRedirect`静态变量的逻辑

2. 加载补丁时，从补丁包中读取要替换的类以及具体替换的方法实现，新建ClassLoader去加载补丁dex


Robust 主要分为俩部分，分别是 app生成部分 和 补丁生成部分

- app生成部分包含俩部分：插件 和 jar包：
	
		apply plugin: 'robust'
		compile 'com.meituan.robust:robust:0.4.78'
		classpath 'com.meituan.robust:gradle-plugin:0.4.78'

- 补丁生成部分：


# 2. app生成部分

## 2.1 jar包

Jar包的主要作用是：帮助加载补丁

	new PatchExecutor(ctx,patchManipulate,robustCallback).start()

PatchExecutor是一个Thread，调用其`start()`方法，会执行run()中的逻辑

    @Override
    public void run() {
        try {
            //拉取补丁列表
            List<Patch> patches = fetchPatchList();
            //应用补丁列表
            applyPatchList(patches);
        } catch (Throwable t) {
            Log.e("robust", "PatchExecutor run", t);
            robustCallBack.exceptionNotify(t, "class:PatchExecutor,method:run,line:36");
        }
    }

- 主要做了俩件事：拉取补丁列表 和 应用补丁列表

- 拉取补丁是通过 构造函数中传入的PatchManipulate 回调实现的，将具体拉取补丁的工作交给开发者去实现

	    protected List<Patch> fetchPatchList() {
	        return patchManipulate.fetchPatchList(context);
	    }

- 获取到补丁列表之后，会传入`applyPatchList(List<Patch> patches)`去应用补丁

## 2.1.1 fetchPatchList()

    @Override
    protected List<Patch> fetchPatchList(Context context) {
        Log.d(TAG, "fetchPatchList");
        //将app自己的robustApkHash上报给服务端，服务端根据robustApkHash来区分每一次apk build来给app下发补丁
        //apkhash is the unique identifier for  apk,so you cannnot patch wrong apk.
        String robustApkHash = RobustApkHashUtils.readRobustApkHash(context);
        Log.w("robust", "robustApkHash :" + robustApkHash);
        //connect to network to get patch list on servers
        //在这里去联网获取补丁列表
        Patch patch = new Patch();
        patch.setName("123");
        //we recommend LocalPath store the origin patch.jar which may be encrypted,while TempPath is the true runnable jar
        //LocalPath是存储原始的补丁文件，这个文件应该是加密过的，TempPath是加密之后的，TempPath下的补丁加载完毕就删除，保证安全性
        //这里面需要设置一些补丁的信息，主要是联网的获取的补丁信息。重要的如MD5，进行原始补丁文件的简单校验，以及补丁存储的位置，这边推荐把补丁的储存位置放置到应用的私有目录下，保证安全性
        patch.setLocalPath(Environment.getExternalStorageDirectory().getPath() +
                File.separator + "robust" + File.separator + "patch");

        //setPatchesInfoImplClassFullName 设置项各个App可以独立定制，
        // 需要确保的是setPatchesInfoImplClassFullName设置的包名是和xml配置项patchPackname保持一致，
        // 而且类名必须是：PatchesInfoImpl
        //请注意这里的设置,设置 包含需要替换的类的信息的 这个信息类 在补丁包中的完整名称
        patch.setPatchesInfoImplClassFullName("com.ryan.hotfix.patch.PatchesInfoImpl");
        List patches = new ArrayList<Patch>();
        patches.add(patch);
        return patches;
    }


## 2.1.2 applyPatchList(List<Patch> patches)

		protected void applyPatchList(List<Patch> patches) {
				//进行非空判断
		        if (null == patches || patches.isEmpty()) {
		            return;
		        }
		        Log.d("robust", " patchManipulate list size is " + patches.size());
				//遍历所有的 补丁包 去执行应用
		        for (Patch p : patches) {
					//判断是否已经加载过，通过isAppliedSucces标志
		            if (p.isAppliedSuccess()) {
		                Log.d("robust", "p.isAppliedSuccess() skip " + p.getLocalPath());
		                continue;
		            }
					//在这个回调中可以进行对 补丁存在的验证
		            if (patchManipulate.ensurePatchExist(p)) {
		                boolean currentPatchResult = false;
		                try {
							//执行具体的应用逻辑
		                    currentPatchResult = patch(context, p);
		                } catch (Throwable t) {
		                    robustCallBack.exceptionNotify(t, "class:PatchExecutor method:applyPatchList line:69");
		                }
						// 设置标志位
		                if (currentPatchResult) {
		                    //设置patch 状态为成功
		                    p.setAppliedSuccess(true);
		                    //统计PATCH成功率 PATCH成功
		                    robustCallBack.onPatchApplied(true, p);
		
		                } else {
		                    //统计PATCH成功率 PATCH失败
		                    robustCallBack.onPatchApplied(false, p);
		                }
		
		                Log.d("robust", "patch LocalPath:" + p.getLocalPath() + ",apply result " + currentPatchResult);
		
		            }
		        }
		    }


## 2.1.3 patch(Context ctx,Patch patch)

    protected boolean patch(Context context, Patch patch) {
		//验证补丁 ，移动补丁位置
        if (!patchManipulate.verifyPatch(context, patch)) {
            robustCallBack.logNotify("verifyPatch failure, patch info:" + "id = " + patch.getName() + ",md5 = " + patch.getMd5(), "class:PatchExecutor method:patch line:107");
            return false;
        }
		//创建DexClassLoader，指定路径为补丁包位置，用来加载补丁包中的补丁类
        DexClassLoader classLoader = new DexClassLoader(patch.getTempPath(), context.getCacheDir().getAbsolutePath(),
                null, PatchExecutor.class.getClassLoader());
        patch.delete(patch.getTempPath());

        Class patchClass, oldClass;

        Class patchsInfoClass;
        PatchesInfo patchesInfo = null;
        try {
            Log.d("robust", "PatchsInfoImpl name:" + patch.getPatchesInfoImplClassFullName());
			//获取补丁包中 的PatchesInfoImpl类，这个类保存有具体哪些类被修改过，包括这个类的原始名称和被修改过的补丁名称
            patchsInfoClass = classLoader.loadClass(patch.getPatchesInfoImplClassFullName());
            patchesInfo = (PatchesInfo) patchsInfoClass.newInstance();
            Log.d("robust", "PatchsInfoImpl ok");
        } catch (Throwable t) {
            robustCallBack.exceptionNotify(t, "class:PatchExecutor method:patch line:108");
            Log.e("robust", "PatchsInfoImpl failed,cause of" + t.toString());
            t.printStackTrace();
        }

        if (patchesInfo == null) {
            robustCallBack.logNotify("patchesInfo is null, patch info:" + "id = " + patch.getName() + ",md5 = " + patch.getMd5(), "class:PatchExecutor method:patch line:114");
            return false;
        }

        //classes need to patch
        List<PatchedClassInfo> patchedClasses = patchesInfo.getPatchedClassesInfo();
        if (null == patchedClasses || patchedClasses.isEmpty()) {
            robustCallBack.logNotify("patchedClasses is null or empty, patch info:" + "id = " + patch.getName() + ",md5 = " + patch.getMd5(), "class:PatchExecutor method:patch line:122");
            return false;
        }
		//遍历需要进行替换操作的类的信息列表
        for (PatchedClassInfo patchedClassInfo : patchedClasses) {
			//被替换的
            String patchedClassName = patchedClassInfo.patchedClassName;
			//进行替换的，补丁类
            String patchClassName = patchedClassInfo.patchClassName;
            if (TextUtils.isEmpty(patchedClassName) || TextUtils.isEmpty(patchClassName)) {
                robustCallBack.logNotify("patchedClasses or patchClassName is empty, patch info:" + "id = " + patch.getName() + ",md5 = " + patch.getMd5(), "class:PatchExecutor method:patch line:131");
                continue;
            }
            Log.d("robust", "current path:" + patchedClassName);
            try {
				//获取原始类中的 ChangeQuickRedirectField接口，需要将补丁类赋值给它
                oldClass = classLoader.loadClass(patchedClassName.trim());
                Field[] fields = oldClass.getDeclaredFields();
                Log.d("robust", "oldClass :" + oldClass + "     fields " + fields.length);
                Field changeQuickRedirectField = null;
                for (Field field : fields) {
					//判断field 是否是指定字段，判断包含该字段的类 是否是指定类
                    if (TextUtils.equals(field.getType().getCanonicalName(), ChangeQuickRedirect.class.getCanonicalName()) && TextUtils.equals(field.getDeclaringClass().getCanonicalName(), oldClass.getCanonicalName())) {
						//进行赋值
                        changeQuickRedirectField = field;
                        break;
                    }
                }
                if (changeQuickRedirectField == null) {
                    robustCallBack.logNotify("changeQuickRedirectField  is null, patch info:" + "id = " + patch.getName() + ",md5 = " + patch.getMd5(), "class:PatchExecutor method:patch line:147");
                    Log.d("robust", "current path:" + patchedClassName + " something wrong !! can  not find:ChangeQuickRedirect in" + patchClassName);
                    continue;
                }
                Log.d("robust", "current path:" + patchedClassName + " find:ChangeQuickRedirect " + patchClassName);
                try {
					//加载补丁类
                    patchClass = classLoader.loadClass(patchClassName);
                    Object patchObject = patchClass.newInstance();
                    changeQuickRedirectField.setAccessible(true);
					//将补丁类赋值给 原始类中的changeQuickRedirectField字段
                    changeQuickRedirectField.set(null, patchObject);
                    Log.d("robust", "changeQuickRedirectField set sucess " + patchClassName);
                } catch (Throwable t) {
                    Log.e("robust", "patch failed! ");
                    t.printStackTrace();
                    robustCallBack.exceptionNotify(t, "class:PatchExecutor method:patch line:163");
                }
            } catch (Throwable t) {
                Log.e("robust", "patch failed! ");
                t.printStackTrace();
                robustCallBack.exceptionNotify(t, "class:PatchExecutor method:patch line:169");
            }
        }
        Log.d("robust", "patch finished ");
        return true;
    }


# 2.2 Gradle插件-实现基础包插栓

插件是被添加到根目录下的`build.gradle`中的buildScript中，其保存路径是类似于 `C:\Users\renbo\.gradle\caches\modules-2\files-2.1\com.meituan.robust\gradle-plugin\0.4.78\9e12cad745baf47e183cc335a505a3a1760bfbe8`

- 可以参考[Android studio dependencies依赖的jar包的存储位置](http://blog.csdn.net/a31081314/article/details/78551536)

打基础包时，Robust会为每个类新增一个类型`ChangeQuickRedirect`的静态变量，并且在每个方法前，增加判断变量是否为空的逻辑，如果不为空，则走插桩的逻辑，否则走正常逻辑

