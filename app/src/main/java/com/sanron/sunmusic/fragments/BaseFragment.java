package com.sanron.sunmusic.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.sanron.sunmusic.AppContext;
import com.sanron.sunmusic.service.IMusicPlayer;

/**
 * Created by Administrator on 2016/3/5.
 */
public class BaseFragment extends Fragment {

    protected View contentView;
    protected IMusicPlayer player;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        player = ((AppContext)(getContext().getApplicationContext())).getMusicPlayer();
    }

    public <T extends View> T $(int id) {
        return (T) contentView.findViewById(id);
    }

}
