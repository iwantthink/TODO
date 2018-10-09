# 1.  事件

1. 考虑如果控件没有设置ID,是否可以绑定事件??

## 1.1 获取事件

`GET https://decide.mixpanel.com/decide?version=1&lib=android&token=efecc1169050774fbff2ce156010d8c1&distinct_id=8b956ef9-8488-4b11-84a2-d64234a78f82&properties=%7B%22%24android_lib_version%22%3A%225.4.1%22%2C%22%24android_app_version%22%3A%221.0%22%2C%22%24android_version%22%3A%225.1%22%2C%22%24android_app_release%22%3A1%2C%22%24android_device_model%22%3A%22m3+note%22%7D HTTP/1.1
`

响应内容:

	[
	    {
	        "event_name":"test",
	        "path":[
	            {
	                "index":0,
	                "prefix":"shortest",
	                "id":16908290
	            },
	            {
	                "index":0,
	                "view_class":"android.support.constraint.ConstraintLayout"
	            },
	            {
	                "index":0,
	                "mp_id_name":"btn_test"
	            }
	        ],
	        "target_activity":null,
	        "event_type":"click"
	    }
	]

### 1.1.1 获取事件的时机


## 1.2 上报事件


### 1.2.1 上报内容

	{
	    "event":"test",
	    "properties":{
	        "mp_lib":"android",
	        "$lib_version":"5.4.1",
	        "$os":"Android",
	        "$os_version":"5.1",
	        "$manufacturer":"Meizu",
	        "$brand":"Meizu",
	        "$model":"m3 note",
	        "$google_play_services":"invalid",
	        "$screen_dpi":480,
	        "$screen_height":1920,
	        "$screen_width":1080,
	        "$app_version":"1.0",
	        "$app_version_string":"1.0",
	        "$app_release":1,
	        "$app_build_number":1,
	        "$has_nfc":false,
	        "$has_telephone":true,
	        "$carrier":"",
	        "$wifi":true,
	        "$bluetooth_enabled":false,
	        "$bluetooth_version":"ble",
	        "token":"efecc1169050774fbff2ce156010d8c1",
	        "time":1538202380,
	        "distinct_id":"8b956ef9-8488-4b11-84a2-d64234a78f82",
	        "$text":"test",
	        "$from_binding":true
	    },
	    "$mp_metadata":{
	        "$mp_event_id":"b9a03ee52b6a448c",
	        "$mp_session_id":"e9513b3975e9fa96",
	        "$mp_session_seq_id":2,
	        "$mp_session_start_sec":1538202305
	    }
	}

### 1.2.1 上报事件的时机

## 1.3 事件如何绑定?
> 在线的事件配置 如何与实际控件发生关联,在控件被点击时判断是否设置有事件?

# 2. 设置AccessibilityDelegate

1. MixpanelAPI 构造函数 --- mUpdatesFromMixpanel.startUpdates()

2. ViewCrawler --- startUpdates();

3. ViewCrawler --- applyPersistedUpdates()

4. ViewCrawler.ViewCrawlerHandler --- loadKnownChanges()

5. ViewCrawler --- applyVariantsAndEventBindings()

            final Map<String, List<ViewVisitor>> editMap = new HashMap<String, List<ViewVisitor>>();
            final int totalEdits = newVisitors.size();
            for (int i = 0; i < totalEdits; i++) {
                final MPPair<String, ViewVisitor> next = newVisitors.get(i);
                final List<ViewVisitor> mapElement;
                if (editMap.containsKey(next.first)) {
                    mapElement = editMap.get(next.first);
                } else {
                    mapElement = new ArrayList<ViewVisitor>();
                    editMap.put(next.first, mapElement);
                }
                mapElement.add(next.second);
            }
            //开启循环
            mEditState.setEdits(editMap);

6. EditState --- setEdits(Map<String, List<ViewVisitor>> newEdits)

7. EditState --- applyEditsOnUiThread()

8. EditState --- applyIntendedEdits()

        for (final Activity activity : getAll()) {
            final String activityName = activity.getClass().getCanonicalName();
            //decorView的RootView
            final View rootView = activity.getWindow().getDecorView().getRootView();

            final List<ViewVisitor> specificChanges;
            final List<ViewVisitor> wildcardChanges;
            synchronized (mIntendedEdits) {
                specificChanges = mIntendedEdits.get(activityName);
                wildcardChanges = mIntendedEdits.get(null);
            }

            if (null != specificChanges) {
                applyChangesFromList(rootView, specificChanges);
            }

            if (null != wildcardChanges) {
                applyChangesFromList(rootView, wildcardChanges);
            }
        }

9. EditState --- applyChangesFromList(View rootView,List<ViewVisitor> changes)

            final int size = changes.size();
            for (int i = 0; i < size; i++) {
                final ViewVisitor visitor = changes.get(i);
                final EditBinding binding = new EditBinding(rootView, visitor, mUiThreadHandler);
                mCurrentEdits.add(binding);
            }

10. EditBinding --- 构造函数

        public EditBinding(View viewRoot, ViewVisitor edit, Handler uiThreadHandler) {
            mEdit = edit;
            mViewRoot = new WeakReference<View>(viewRoot);
            mHandler = uiThreadHandler;
            mAlive = true;
            mDying = false;

            final ViewTreeObserver observer = viewRoot.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.addOnGlobalLayoutListener(this);
            }
            run();
        }

11. EditBinding ---  run()

            if (!mAlive) {
                return;
            }

            final View viewRoot = mViewRoot.get();
            if (null == viewRoot || mDying) {
                cleanUp();
                return;
            }
            // ELSE View is alive and we are alive
			// mEdit 实际类型是 AddAccessibilityEventVisitor
            mEdit.visit(viewRoot);
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, 1000);

12. ViewVisitor --- visit()

		mPathfinder.findTargetsInRoot(rootView, mPath, this);

13. PathFinder --- findTargetsInRoot(View givenRootView, List<PathElement> path, Accumulator accumulator)

        if (path.isEmpty()) {
            return;
        }

        if (mIndexStack.full()) {
            MPLog.w(LOGTAG, "There appears to be a concurrency issue in the pathfinding code. Path will not be matched.");
            return; // No memory to perform the find.
        }

        final PathElement rootPathElement = path.get(0);
        final List<PathElement> childPath = path.subList(1, path.size());

        final int indexKey = mIndexStack.alloc();
		// 2.1 !!
        final View rootView = findPrefixedMatch(rootPathElement, givenRootView, indexKey);
        mIndexStack.free();

        if (null != rootView) {
            findTargetsInMatchedView(rootView, childPath, accumulator);
        }
	


## 2.1 findPreFixedMatch(PathElement findElement, View subject, int indexKey)

        final int currentIndex = mIndexStack.read(indexKey);
        if (matches(findElement, subject)) {
            mIndexStack.increment(indexKey);
            if (findElement.index == -1 || findElement.index == currentIndex) {
                return subject;
            }
        }

        if (findElement.prefix == PathElement.SHORTEST_PREFIX && subject instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) subject;
            final int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = group.getChildAt(i);
                final View result = findPrefixedMatch(findElement, child, indexKey);
                if (null != result) {
                    return result;
                }
            }
        }

        return null;

## 2.2 matches(PathElement matchElement, View subject)

    private boolean matches(PathElement matchElement, View subject) {
        // 过滤条件的viewClassName不为空
        // 目标控件的className 与 过滤条件的className 不相同
        if (null != matchElement.viewClassName &&
                !hasClassName(subject, matchElement.viewClassName)) {
            return false;
        }
        //过滤条件的viewID不能为-1
        //目标控件的viewId 与 过滤条件的VeiwID 不同
        if (-1 != matchElement.viewId && subject.getId() != matchElement.viewId) {
            return false;
        }

        //过滤条件的 contentDescription 不能为空
        //目标控件的 contentDescription 和 过滤条件的 contentDescription 不相等
        if (null != matchElement.contentDescription &&
                !matchElement.contentDescription.equals(subject.getContentDescription())) {
            return false;
        }

        //取出过滤条件中的 tag
        final String matchTag = matchElement.tag;
        // 过滤条件有tag时 才去进行匹配
        if (null != matchElement.tag) {
            //取出目标控件的tag
            final Object subjectTag = subject.getTag();
            //目标控件TAG 为空  或者  过滤条件和目标控件的TAG 不匹配
            if (null == subjectTag || !matchTag.equals(subject.getTag().toString())) {
                return false;
            }
        }

        return true;
    }



# 2. 可视化埋点

## 2.1 如何连接?

通过加速度传感器,摇晃手机 


## 2.2 连接之后做了什么事情?

## 2.3 如何下发事件?

## 2.4 如何绑定事件?