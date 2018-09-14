
    public static ViewNode getViewNode(View view, ViewTraveler viewTraveler) {
		//保存View的视图
        ArrayList<View> viewTreeList = new ArrayList(8);
		//获取当前View控件的父类
        ViewParent parent = view.getParent();
        viewTreeList.add(view);
		//取当前View的父类,直到为空或类型不为ViewGroup
        while(parent != null && parent instanceof ViewGroup) {
            if(Util.isIgnoredView((View)parent)) {
                return null;
            }
			// index[0] = 传入的View
			// index[max] = 顶层View
            viewTreeList.add((ViewGroup)parent);
            parent = parent.getParent();
        }
		//index[max]
        int endIndex = viewTreeList.size() - 1;
		//获取顶级view
        View rootView = (View)viewTreeList.get(endIndex);
		//初始化WindowHelper
        WindowHelper.init();
		//
        String bannerText = null;
		// TAG的值
        String inheritableObjInfo = null;
		// 当前View 位于父控件的位置
        int viewPosition = 0;
		// 当前View 位于列表类型的父控件中的位置
        int listPos = -1;
		// flag 表示当前控件父类是否是列表类
        boolean mHasListParent = false;
		// 是否仅使用TAG 作为Name
        boolean mParentIdSettled = false;
		// 获取顶级View的类型 
        String prefix = WindowHelper.getSubWindowPrefix(rootView);
		// 路径
        String opx = prefix;
		// 路径
        String px = prefix;
		// 1. 不是DecorView 
		// 2. 顶层控件的父类 不是View
        if(!WindowHelper.isDecorView(rootView) && !(rootView.getParent() instanceof View)) {
			// 获取ROOTView的 类名, 修改opx
            opx = prefix + "/" + Util.getSimpleClassName(rootView.getClass());
            px = opx;
			//是否添加rouseID名称,判断View中是否存在TAG
            if(GConfig.USE_ID) {
                String id = Util.getIdName(rootView, mParentIdSettled);
                if(id != null) {
                    if(rootView.getTag(84159242) != null) {
                        mParentIdSettled = true;
                    }

                    opx = opx + "#" + id;
                    px = px + "#" + id;
                }
            }
        }
		//获取RootView的TAG,并转换成String
        Object inheritableObject = rootView.getTag(84159243);
        if(inheritableObject != null && inheritableObject instanceof String) {
            inheritableObjInfo = (String)inheritableObject;
        }

		//计算ViewPath
        if(rootView instanceof ViewGroup) {
			//顶层View
            ViewGroup parentView = (ViewGroup)rootView;
			// 从第二高的层级开始往下进行遍历  max->min
            for(int i = endIndex - 1; i >= 0; --i) {
				// 当前View位于 父类View控件中的位置
                viewPosition = 0;
				// 获取index对应的View
                View childView = (View)viewTreeList.get(i);
				//获取对应的TAG,当做View的自定义名称
                Object viewName = childView.getTag(84159241);
				//如果存在TAG,直接使用TAG当做控件名称
                if(viewName != null) {
                    opx = "/" + viewName;
                    px = px + "/" + viewName;
                } else {
					//获取类名
                    Object viewName = Util.getSimpleClassName(childView.getClass());
					//获取当前控件位于父控件中的位置
                    viewPosition = parentView.indexOfChild(childView);
					// 判断是否是ViewPager,AdapterView,RecyclerView....如果出现这三个类型的父类控件 position需要重新计算
					// 计算得到的position 会在下面 被使用
                    if(ClassExistHelper.instanceOfViewPager(parentView)) {
                        viewPosition = ((ViewPager)parentView).getCurrentItem();
                        mHasListParent = true;
                    } else if(parentView instanceof AdapterView) {
                        AdapterView listView = (AdapterView)parentView;
                        viewPosition += listView.getFirstVisiblePosition();
                        mHasListParent = true;
                    } else if(ClassExistHelper.instanceOfRecyclerView(parentView)) {
                        int adapterPosition = getChildAdapterPositionInRecyclerView(childView, parentView);
                        if(adapterPosition >= 0) {
                            mHasListParent = true;
                            viewPosition = adapterPosition;
                        }
                    }
					//如果是 ExpandableListView  那么需要重新计算position
                    if(parentView instanceof ExpandableListView) {
                        ExpandableListView listParent = (ExpandableListView)parentView;
                        long elp = listParent.getExpandableListPosition(viewPosition);
                        int footerIndex;
                        if(ExpandableListView.getPackedPositionType(elp) == 2) {
                            if(viewPosition < listParent.getHeaderViewsCount()) {
                                opx = opx + "/ELH[" + viewPosition + "]/" + viewName + "[0]";
                                px = px + "/ELH[" + viewPosition + "]/" + viewName + "[0]";
                            } else {
                                footerIndex = viewPosition - (listParent.getCount() - listParent.getFooterViewsCount());
                                opx = opx + "/ELF[" + footerIndex + "]/" + viewName + "[0]";
                                px = px + "/ELF[" + footerIndex + "]/" + viewName + "[0]";
                            }
                        } else {
                            footerIndex = ExpandableListView.getPackedPositionGroup(elp);
                            int childIdx = ExpandableListView.getPackedPositionChild(elp);
                            if(childIdx != -1) {
                                listPos = childIdx;
                                px = opx + "/ELVG[" + footerIndex + "]/ELVC[-]/" + viewName + "[0]";
                                opx = opx + "/ELVG[" + footerIndex + "]/ELVC[" + childIdx + "]/" + viewName + "[0]";
                            } else {
                                listPos = footerIndex;
                                px = opx + "/ELVG[-]/" + viewName + "[0]";
                                opx = opx + "/ELVG[" + footerIndex + "]/" + viewName + "[0]";
                            }
                        }
					// 父类控件如果是 AdapterView,RecyclerView,ViewPager
					// 计算Banner的position
					// 给listPos 赋值,该值代表控件位于列表的位置
                    } else if(Util.isListView(parentView)) {
						//获取TAG2,一个List
                        Object bannerTag = parentView.getTag(84159247);
                        if(bannerTag != null && bannerTag instanceof List && ((List)bannerTag).size() > 0) {
                            viewPosition = Util.calcBannerItemPosition((List)bannerTag, viewPosition);
                            bannerText = Util.truncateViewContent(String.valueOf(((List)bannerTag).get(viewPosition)));
                        }
						// 设置 listPos 
                        listPos = viewPosition;
                        px = opx + "/" + viewName + "[-]";
                        opx = opx + "/" + viewName + "[" + viewPosition + "]";
					// 父类是 srfl
                    } else if(ClassExistHelper.instanceOfSwipeRefreshLayout(parentView)) {
                        opx = opx + "/" + viewName + "[0]";
                        px = px + "/" + viewName + "[0]";
                    } else {
                        opx = opx + "/" + viewName + "[" + viewPosition + "]";
                        px = px + "/" + viewName + "[" + viewPosition + "]";
                    }

                    if(GConfig.USE_ID) {
                        String id = Util.getIdName(childView, mParentIdSettled);
                        if(id != null) {
                            if(childView.getTag(84159242) != null) {
                                mParentIdSettled = true;
                            }

                            opx = opx + "#" + id;
                            px = px + "#" + id;
                        }
                    }
                }

                inheritableObject = childView.getTag(84159243);
                if(childView instanceof RadioGroup) {
                    RadioGroup radioGroup = (RadioGroup)childView;
                    View theView = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
                    if(childView != null) {
                        String childInheritableGrowingInfo = (String)theView.getTag(84159243);
                        if(!TextUtils.isEmpty(childInheritableGrowingInfo)) {
                            inheritableObject = childInheritableGrowingInfo;
                        }
                    }
                }

                if(inheritableObject != null && inheritableObject instanceof String) {
                    inheritableObjInfo = (String)inheritableObject;
                }

                if(!(childView instanceof ViewGroup)) {
                    break;
                }

                parentView = (ViewGroup)childView;
            }
        }

        inheritableObject = view.getTag(84159243);
        if(inheritableObject != null && inheritableObject instanceof String) {
            inheritableObjInfo = (String)inheritableObject;
        }

        ViewNode viewNode = new ViewNode(view, viewPosition, listPos, mHasListParent, prefix.equals(WindowHelper.getMainWindowPrefix()), true, mParentIdSettled, opx, px, prefix, viewTraveler);
		// 查找当前View的Text
        viewNode.mViewContent = Util.getViewContent(view, bannerText);
        viewNode.mInheritableGrowingInfo = inheritableObjInfo;
        viewNode.mClickableParentXPath = px;
        viewNode.mBannerText = bannerText;
        return viewNode;
    }