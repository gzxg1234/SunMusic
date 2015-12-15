package com.sanron.sunmusic.fragment;

import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by Administrator on 2015/12/15.
 */
public class BaseFragment extends Fragment {

    protected View contentView;

    public <T extends View> T $(int id){
        return (T) contentView.findViewById(id);
    }
}
