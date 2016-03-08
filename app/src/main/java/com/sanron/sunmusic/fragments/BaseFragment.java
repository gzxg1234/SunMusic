package com.sanron.sunmusic.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import com.sanron.sunmusic.service.IMusicPlayer;
import com.sanron.sunmusic.service.PlayerUtils;

/**
 * Created by Administrator on 2016/3/5.
 */
public class BaseFragment extends Fragment{

    protected View contentView;
    protected IMusicPlayer player = PlayerUtils.getService();

    public <T extends View> T $(int id) {
        return (T) contentView.findViewById(id);
    }

}
