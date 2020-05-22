# 1. 遇到依赖下载超时
 Flutter 编译Android项目时,会从`https://storage.googleapis.com`地址下载依赖,但是这个地址被墙了，导致超时。
 
 解决方法就是往build.gradle中添加一个新的地址
 
      maven {
         url 'http://download.flutter.io'
     }
     
 - [参考资料](https://github.com/flutter/flutter/issues/47452)