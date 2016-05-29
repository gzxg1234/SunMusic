package com.sanron.ddmusic.fragments.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * 懒加载fragment,用在viewpager中
 * Created by sanron on 16-4-18.
 */
public abstract class LazyLoadFragment extends BaseFragment {

    private boolean mIsLoaded;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view, savedInstanceState);
        if (!mIsLoaded
                && getUserVisibleHint()) {
            loadData();
            mIsLoaded = true;
        }
    }

    protected void initView(View view, Bundle savedInstanceState) {
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser
                && getView() != null
                && !mIsLoaded) {
            loadData();
            mIsLoaded = true;
        }
    }

    protected abstract void loadData();
}
