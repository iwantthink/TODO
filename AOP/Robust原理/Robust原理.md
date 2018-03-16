# Robust原理

[Robust WIKI](https://github.com/Meituan-Dianping/Robust/wiki)

[Android热补丁之Robust原理解析(一)](http://w4lle.com/2017/03/31/robust-0/)

[Android热更新方案Robust](https://tech.meituan.com/android_robust.html)

# 1. 原理解析

1. 打基础包时插桩，在每个类中插入一段类型为 `ChangeQuickRedirect`静态变量的逻辑

2. 加载补丁时，从补丁包中读取要替换的类以及具体替换的方法实现，新建ClassLoader去加载补丁dex


## 1.1 基础概念

打基础包时，Robust会为每个类新增一个类型`ChangeQuickRedirect`的静态变量，并且在每个方法前，增加判断变量是否为空的逻辑，如果不为空，则走插桩的逻辑，否则走正常逻辑

