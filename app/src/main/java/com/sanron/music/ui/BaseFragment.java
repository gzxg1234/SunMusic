package com.sanron.music.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.sanron.music.activities.MainActivity;

/**
 * Created by Administrator on 2016/3/5.
 */
public class BaseFragment extends Fragment implements MainActivity.BackPressedHandler, MainActivity.PlayerReadyCallback {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).removePlayerReadyCallback(this);
    }

    public <T extends View> T $(int id) {
        return (T) getView().findViewById(id);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onPlayerReady() {
    }
}
