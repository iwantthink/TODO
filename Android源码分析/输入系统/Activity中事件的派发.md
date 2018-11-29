# Activity中事件的派发

[Android开发艺术探索]()

[深入理解Android 卷III]()

# 1. 简介

[View中事件的派发.md]()仅讨论了控件系统的范畴中对事件的派发逻辑,并没有讨论在`Activity`,`Dialog`等因素存在时的影响

`Activity`,`Dialog`作为控件系统的一个用户同样遵守相同的派发原理,只不过因为其控件树的根控件`DecorView`的特殊性,使得输入事件的派发存在一些新的内容

# 2. Activity与控件系统

窗口以及控件树的讨论主要在`WindowManager`,`ViewRootImpl`和控件树所组成的体系之下进行,但是在大多数情况下,存在一些更高级的概念,例如`Activity`,`Dialog`

# 3. 关于PhoneWindow




## 3.1 选择窗口外观与设置显示内容

### 3.1.1 Activity.requestWindowFeature()

### 3.1.2 Activity.setContentView()

## 3.2 DecorView的特点

# 4. Activity窗口的创建与显示

