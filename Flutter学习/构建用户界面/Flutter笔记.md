# Flutter笔记


# 1. 为什么`build()`方法在`State`类中，而不是`StatefullWidget`?

[**这主要是为了开发时能够灵活的访问状态**](https://github.com/flutterchina/flutter-in-action/blob/master/docs/chapter2/first_flutter_app.md)


如果将`build()`方法防在`StatefullWidget`中会产生俩个问题

1. 状态访问不方便

2. 继承`StatefulWidget`不方便

