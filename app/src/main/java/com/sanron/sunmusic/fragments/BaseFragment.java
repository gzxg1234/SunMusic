package com.sanron.sunmusic.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanron.sunmusic.db.DataProvider;

import java.util.Observer;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/12/15.
 */
public abstract class BaseFragment extends Fragment  implements Observer {

    protected View contentView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        DataProvider.instance().addObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataProvider.instance().deleteObserver(this);
    }

    public <T extends View> T $(int id){
        return (T) contentView.findViewById(id);
    }


}
