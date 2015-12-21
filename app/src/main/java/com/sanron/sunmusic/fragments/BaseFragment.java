package com.sanron.sunmusic.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/12/15.
 */
public abstract class BaseFragment extends Fragment {

    protected View contentView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public <T extends View> T $(int id){
        return (T) contentView.findViewById(id);
    }


}
