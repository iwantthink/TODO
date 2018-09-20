# onClick
`onClick()`的会针对俩个点击事件进行埋点

1.  `public static void onClick(Object object, View view)`

2.  ` public static void onClick(Object object, DialogInterface dialogInterface, int which)`


    public static void clickOn(View view) {
        if(GConfig.sCanHook) {
            try {
                if(persistClickEventRunnable.havePendingEvent()) {
                    return;
                }
				//创建ViewNode
                ViewNode viewNode = ViewHelper.getClickViewNode(view);
                if(viewNode == null) {
                    return;
                }
				//针对ImageView控件
                if(GConfig.getInstance().isImageViewCollectionEnable() && TextUtils.isEmpty(viewNode.mViewContent) && view instanceof ImageView) {
                    ActionEvent actionEvent = ViewHelper.getClickActionEvent(viewNode);
                    ClickEventAsyncExecutor.getInstance().execute(new WeakReference(view), viewNode, actionEvent.clone());
                } else {
                    persistClickEventRunnable.resetData(viewNode);
                    handleClickResult(Boolean.valueOf(true));
                }
            } catch (Throwable var3) {
                LogUtil.d(var3);
            }
        }

    }


# 1. ViewHelper

## 1.1 getClickViewNode(View v)

    public static ViewNode getClickViewNode(View view) {
        AppState state = AppState.getInstance();
        if(state != null && GConfig.getInstance().isEnabled()) {
			// 获取前台Activity
            Activity activity = state.getForegroundActivity();
			// 前台Activity非空,且不在忽略名单中
            if(activity != null && !Util.isIgnoredView(view)) {
				// 获取ViewNode
                ViewNode viewNode = getViewNode(view, sClickTraveler);
                if(viewNode == null) {
                    return null;
                } else {
					//清空保存ActionStruct的列表
                    sClickTraveler.resetActionStructList();
					//生成当前ViewNode对应的ActionStruct
                    sClickTraveler.traverseCallBack(viewNode);
					// 意义不明
                    viewNode.traverseChildren();
                    return viewNode;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

## 1.2 getViewNode(View view, ViewTraveler viewTraveler)

参考[ViewNode.md]()

## 1.3 getClickActionEvent(ViewNode viewNode) 

    public static ActionEvent getClickActionEvent(ViewNode viewNode) {
        if(viewNode == null) {
            return null;
        } else {
            AppState state = AppState.getInstance();
            if(state != null && GConfig.getInstance().isEnabled()) {
                Activity activity = state.getForegroundActivity();
                if(activity != null && !Util.isIgnoredView(viewNode.mView)) {
					//创建clck类型的 ActionEvent
                    ActionEvent click = ActionEvent.makeClickEvent();
					//保存页面名称
                    click.mPageName = state.getPageName(activity);
					//保存ActionStruct
                    click.elems = sClickTraveler.actionStructList;
					// 保存时间
                    click.setPageTime(MessageProcessor.getInstance().getPTM());
                    return click;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

## 1.4 persistClickEvent(ActionEvent click, ViewNode viewNode)

	public static void persistClickEvent(ActionEvent click, ViewNode viewNode) {
	        MessageProcessor.getInstance().persistEvent(click);
	        CircleManager manager = CircleManager.getInstance();
	        if(manager.isProjection()) {
	            manager.sendClickEventWith(viewNode);
	        }
	
	    }


#  2. ViewTraveler

	public abstract class ViewTraveler {
	    public ViewTraveler() {
	    }
		//判断是否需要进行遍历
	    public boolean needTraverse(ViewNode viewNode) {
	        return viewNode.isNeedTrack();
	    }
		//遍历之后的回调
	    public abstract void traverseCallBack(ViewNode var1);
	}

## 2.1 ViewNodeTraveler

    private static class ViewNodeTraveler extends ViewTraveler {
        private long currentTime;
        private ArrayList<ActionStruct> actionStructList;

        private ViewNodeTraveler() {
            this.actionStructList = new ArrayList();
        }

        public void resetActionStructList() {
            this.currentTime = System.currentTimeMillis();
            this.actionStructList.clear();
        }

		//读取ViewNode的信息,组装成ActionStruct进行发送
        public void traverseCallBack(ViewNode viewNode) {
            if(this.actionStructList != null) {
                ActionStruct struct = new ActionStruct();
                struct.xpath = viewNode.mParentXPath;
                struct.content = viewNode.mViewContent;
                struct.index = viewNode.mLastListPos;
                struct.time = this.currentTime;
                struct.obj = viewNode.mInheritableGrowingInfo;
                this.actionStructList.add(struct);
            }

        }
    }


# 3. VdsAgent

## 3.1 PersistClickEventRunnable

    private static class PersistClickEventRunnable implements Runnable {
        private ViewNode viewNode;
        private ActionEvent actionEvent;

        private PersistClickEventRunnable() {
        }

        public void resetData(ViewNode viewNode) {
            this.viewNode = viewNode;
            if(viewNode != null) {
                this.actionEvent = ViewHelper.getClickActionEvent(viewNode);
            }

        }

        public boolean havePendingEvent() {
            return this.viewNode != null;
        }

        public void run() {
            try {
                ViewHelper.persistClickEvent(this.actionEvent, this.viewNode);
            } catch (Throwable var2) {
                LogUtil.d(var2);
            }

            this.viewNode = null;
        }
    }


## 3.2 handleClickResult(boolean returnValueObject )

    public static void handleClickResult(Object returnValueObject) {
        boolean result = handleBooleanResult(returnValueObject);
		//根据returnValueObject 和 是否有ViewNode进行判断
        if(result && persistClickEventRunnable.havePendingEvent()) {
			//去除之前的 persisitClickEventRunnable
            ThreadUtils.cancelTaskOnUiThread(persistClickEventRunnable);
			// 执行persistClickEventRunnable
            ThreadUtils.postOnUiThread(persistClickEventRunnable);
        } else {
            persistClickEventRunnable.resetData((ViewNode)null);
        }

    }

## 3.3 handleBooleanResult

	//如果传入的值是Boolean类型,那么就返回其的值
    private static boolean handleBooleanResult(Object returnValueObject) {
        boolean result = false;
        if(returnValueObject instanceof Boolean) {
            result = ((Boolean)returnValueObject).booleanValue();
        }

        