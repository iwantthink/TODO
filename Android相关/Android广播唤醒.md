# Android广播唤醒


# 1. 限制

在魅族上,当app被杀死的之后,广播无法被唤醒.

- 需要在设置中打开app的自启动权限

- app不处于STOPPED状态(该状态可以在`设置-应用`中查看)

在小米上,当app被杀死之后,广播无法被唤醒

- 添加 `Intent.FLAG_INCLUDE_STOPPED_PACKAGES` FLAG 到intent中可以解决问题

- 通过设置component 也可以解决问题

- app不处于STOPPED状态



在pixel1上,暂未发现限制