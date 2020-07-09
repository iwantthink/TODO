# Android Fragment

# 1. 可见性状态

## 1.1 onHiddenChanged

源码:

    /**
     * Return true if the fragment has been hidden.  By default fragments
     * are shown.  You can find out about changes to this state with
     * {@link #onHiddenChanged}.  Note that the hidden state is orthogonal
     * to other states -- that is, to be visible to the user, a fragment
     * must be both started and not hidden.
     */
    final public boolean isHidden() {
        return mHidden;
    }

    /**
     * Called when the hidden state (as returned by {@link #isHidden()} of
     * the fragment has changed.  Fragments start out not hidden; this will
     * be called whenever the fragment changes state from that.
     * @param hidden True if the fragment is now hidden, false otherwise.
     */
    public void onHiddenChanged(boolean hidden) {
    }

1. 如果Fragment是可见的，那么fragment必须满足已经启动并且hidden值为false

2. 当Fragment的hidden状态发生变化时，会回调该onHiddenChanged


注意：

1. 当使用FragmentTransaction调用Fragment的`hide()`和`show()`方法时，`onHiddenChanged`方法就会被调用

2. **在使用`ViewPager+PagerAdapter+Fragment`时该方法不会被回调,只有在自己管理Fragment时(例如TabHost+Fragment,hiden&show),该方法才有效,`replace()`方法也不会回调该方法**

3. `show()/hiden()`被调用时，只有当前Fragment的`onHiddenChanged`有效,其子类的无效


## 1.2 setUserVisibleHint

    /**
     * Set a hint to the system about whether this fragment's UI is currently visible
     * to the user. This hint defaults to true and is persistent across fragment instance
     * state save and restore.
     *
     * <p>An app may set this to false to indicate that the fragment's UI is
     * scrolled out of visibility or is otherwise not directly visible to the user.
     * This may be used by the system to prioritize operations such as fragment lifecycle updates
     * or loader ordering behavior.</p>
     *
     * <p><strong>Note:</strong> This method may be called outside of the fragment lifecycle.
     * and thus has no ordering guarantees with regard to fragment lifecycle method calls.</p>
     *
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     *                        false if it is not.
     *
     * @deprecated Use {@link FragmentTransaction#setMaxLifecycle(Fragment, Lifecycle.State)}
     * instead.
     */
    @Deprecated
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!mUserVisibleHint && isVisibleToUser && mState < STARTED
                && mFragmentManager != null && isAdded() && mIsCreated) {
            mFragmentManager.performPendingDeferredStart(this);
        }
        mUserVisibleHint = isVisibleToUser;
        mDeferStart = mState < STARTED && !isVisibleToUser;
        if (mSavedFragmentState != null) {
            // Ensure that if the user visible hint is set before the Fragment has
            // restored its state that we don't lose the new value
            mSavedUserVisibleHint = isVisibleToUser;
        }
    }

1.  该方法的调用可能和Fragment的生命周期无关,因此对fragment生命周期的调用没有顺序保证

注意：

1. 该方法在使用`ViewPager+PagerAdapter+Fragment`时会被调用


## 1.3 生命周期相关

1. 当使用hide&&show时， `onHiddenChanged()`方法会被调用,`setUserVisibleHint()`方法不会被调用,`onResume/onPause`仅在被添加时被调用一次.

2. 当使用replace时, `onHiddenChanged()`方法不会被调用,`setUserVisibleHint()`方法不会被调用,`onResume/onPause`会被调用

3. 当使用ViewPager时,`onHiddenChanged()`方法不会被调用,`setUserVisibleHint()`方法会被调用,`onResume/onPause`会被调用(并且setUserVisibleHint优先于onResume被调用)

4. 当使用FragmentTabHost+Fragment时, `onHiddenChanged()`方法不会被调用,`setUserVisibleHint()`方法不会被调用,`onResume/onPause`会被调用

使用Home键时或Back键时:

1. ViewPager, 仅`onResume/onPause`会被调用

2. hide&show, 仅`onResume/onPause`会被调用

3. replace时,仅`onResume/onPause`会被调用

4. FragmentTabHost+Fragment,仅`onResume/onPause`会被调用


检测:

1. hide&show :

	resume/pause/ onHiddenChanged 中getUserVisibleHint = true  不会发生变化.(只用判断hidden即可)
	
	onHiddenChanged被调用时,需要判断resume状态 和hidden 状态
	
	
	
2. replace

	resume/pause ,俩个值不会变化一直是 hidden = false, getUserVisibleHint = true 

3. viewpager:

	**isHidden 恒为 false!!!**

	resume/pause中需要加入判断getUserVisibleHint, Resume+Visible 表示进入,Pause+Visible 表示退出
	
	当setUserVisibleHint被调用时，需要判断resume状态,(setUserVisibleHint通常早于Resume被调用,但是由于 ViewPager有预加载的功能，因此可能会出现onResume 早于setUserVisibleHint被调用)
	
	ViewPager 可能会提前加载Fragment,导致Fragment的onResume先执行了，但是setUserVisibleHint 并没有执行，当真正切换到该Fragment时，仅调用起setUserVisibleHint()方法
	
4. FragmentTabHost+Fragment,

	resume/pause, 俩个值不会变化一直是 hidden = false, getUserVisibleHint = true 


	
	