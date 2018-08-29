
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
			//层级由小至大
            viewTreeList.add((ViewGroup)parent);
            parent = parent.getParent();
        }
		//最顶级View的index
        int endIndex = viewTreeList.size() - 1;
		//获取顶级view
        View rootView = (View)viewTreeList.get(endIndex);
		//初始化WindowHelper
        WindowHelper.init();
		//
        String bannerText = null;
		//
        String inheritableObjInfo = null;
		// 当前View 位于父控件的位置
        int viewPosition = 0;
		// 当前View 位于列表类型的父控件中的位置
        int listPos = -1;
		// flag 表示当前控件父类是否是列表类
        boolean mHasListParent = false;
		// 
        boolean mParentIdSettled = false;
		// 获取顶级View的类型 
        String prefix = WindowHelper.getSubWindowPrefix(rootView);
        String opx = prefix;
        String px = prefix;
        if(!WindowHelper.isDecorView(rootView) && !(rootView.getParent() instanceof View)) {
            opx = prefix + "/" + Util.getSimpleClassName(rootView.getClass());
            px = opx;
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

        Object inheritableObject = rootView.getTag(84159243);
        if(inheritableObject != null && inheritableObject instanceof String) {
            inheritableObjInfo = (String)inheritableObject;
        }

        if(rootView instanceof ViewGroup) {
            ViewGroup parentView = (ViewGroup)rootView;

            for(int i = endIndex - 1; i >= 0; --i) {
                viewPosition = 0;
                View childView = (View)viewTreeList.get(i);
                Object viewName = childView.getTag(84159241);
                if(viewName != null) {
                    opx = "/" + viewName;
                    px = px + "/" + viewName;
                } else {
                    Object viewName = Util.getSimpleClassName(childView.getClass());
                    viewPosition = parentView.indexOfChild(childView);
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
                    } else if(Util.isListView(parentView)) {
                        Object bannerTag = parentView.getTag(84159247);
                        if(bannerTag != null && bannerTag instanceof List && ((List)bannerTag).size() > 0) {
                            viewPosition = Util.calcBannerItemPosition((List)bannerTag, viewPosition);
                            bannerText = Util.truncateViewContent(String.valueOf(((List)bannerTag).get(viewPosition)));
                        }

                        listPos = viewPosition;
                        px = opx + "/" + viewName + "[-]";
                        opx = opx + "/" + viewName + "[" + viewPosition + "]";
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
        viewNode.mViewContent = Util.getViewContent(view, bannerText);
        viewNode.mInheritableGrowingInfo = inheritableObjInfo;
        viewNode.mClickableParentXPath = px;
        viewNode.mBannerText = bannerText;
        return viewNode;
    }