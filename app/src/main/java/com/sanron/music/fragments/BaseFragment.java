package com.sanron.music.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanron.music.AppContext;
import com.sanron.music.activities.MainActivity;
import com.sanron.music.service.IPlayer;

/**
 * Created by Administrator on 2016/3/5.
 */
public class BaseFragment extends Fragment implements MainActivity.BackPressedHandler {

    protected IPlayer player;
    protected AppContext appContext;
    protected MainActivity mainActivity;

    public static final String ACTION_FRAG_EVENT = "com.sanron.FragmentEvent";
    public static final String EXTRA_FROM = "from";
    public static final String EXTRA_EVENT = "event";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = (AppContext) (getContext().getApplicationContext());
        player = appContext.getMusicPlayer();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MainActivity) {
            mainActivity = (MainActivity) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    public <T extends View> T $(int id) {
        return (T) getView().findViewById(id);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
