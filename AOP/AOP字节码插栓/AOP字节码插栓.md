# 1.Android AOP

# 1.1 简介
面向切向编程（Aspect Oriented Programming），相对于面向对象编程（ObjectOriented Programming）而言。  

OOP是将功能模块化或者问题模块化,AOP就是将涉及到众多模块的某一类问题进行统一管理


## 1.1 方式介绍
AOP 按实现原理可以分为**运行时AOP和编译时AOP**
- Android运行时AOP：主要实现通过hook某些关键方法
- Android编译时AOP：主要是在apk打包过程中对class文件的字节码进行识别并更改


主流的Anrdoid AOP 框架有：  
1. Dexposed，Xposed等（运行时）
2. aspactJ（编译时）


还有一些非框架的但能实现 AOP的工具类库：

1. java的动态代理机制(对java接口有效)
2. ASM,javassit等字节码操作类库
3. (偏方)DexMaker:Dalvik 虚拟机上，在编译期或者运行时生成代码的 Java API。
4. (偏方)ASMDEX(一个类似 ASM 的字节码操作库，运行在Android平台，操作Dex字节码)
