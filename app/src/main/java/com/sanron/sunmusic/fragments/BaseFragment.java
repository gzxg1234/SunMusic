package com.sanron.sunmusic.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by Administrator on 2016/3/5.
 */
public class BaseFragment extends Fragment{

    protected View contentView;

    public <T extends View> T $(int id) {
        return (T) contentView.findViewById(id);
    }

}
