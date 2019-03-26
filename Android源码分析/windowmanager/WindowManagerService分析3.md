# WindowManagerService分析3

[深入理解Android 卷III ]()


# 1. WindowToken

## 1.1 WindowToken的意义

1. **`WindowToken`将属于同一个应用组件的窗口组织在了一起**。所谓的应用组件可以是Activity、InputMethod、Wallpaper以及Dream。

	在WMS对窗口的管理过程中，用`WindowToken`指代一个应用组件。例如在进行窗口ZOrder排序时，属于同一个`WindowToken`的窗口会被安排在一起，而且在其中定义的一些属性将会影响所有属于此`WindowToken`的窗口。这些都表明了属于同一个WindowToken的窗口之间的紧密联系。

2. **`WindowToken`具有令牌的作用，是对应用组件的行为进行规范管理的一个手段**。

	`WindowToken`由应用组件或其管理者负责向WMS声明并持有。**应用组件在需要新的窗口时，必须提供WindowToken以表明自己的身份，并且窗口的类型必须与所持有的WindowToken的类型一致**。

	**在创建系统类型的窗口时不需要提供一个有效的Token，WMS会隐式地为其声明一个WindowToken**。但是它要求客户端**必须拥有`SYSTEM_ALERT_WINDOW`或`INTERNAL_SYSTEM_WINDOW`权限**才能创建系统类型的窗口,方法`addWindow()`中一开始的`mPolicy.checkAddPermission()`的目的就是如此

## 1.2 如何添加一个WindowToken?

**对于WMS的客户端来说，Token仅仅是一个Binder对象而已。**

    @Override
    public void addWindowToken(IBinder binder, int type, int displayId) {

		// 要求声明Token的调用者拥有MANAGE_APP_TOKENS权限
        if (!checkCallingPermission(MANAGE_APP_TOKENS, "addWindowToken()")) {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        synchronized(mWindowMap) {
            final DisplayContent dc = mRoot.getDisplayContentOrCreate(displayId);
            WindowToken token = dc.getWindowToken(binder);
            if (token != null) {
                return;
            }
            if (type == TYPE_WALLPAPER) {
                new WallpaperWindowToken(this, binder, true, dc,
                        true /* ownerCanManageAppTokens */);
            } else {
                new WindowToken(this, binder, type, true, dc, true /* ownerCanManageAppTokens */);
            }
        }
    }