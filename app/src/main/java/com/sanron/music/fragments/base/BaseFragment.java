package com.sanron.music.fragments.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.sanron.music.R;
import com.sanron.music.activities.MainActivity;

/**
 * Created by Administrator on 2016/3/5.
 */
public abstract class BaseFragment extends Fragment implements MainActivity.BackPressedHandler, MainActivity.PlayerReadyCallback {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeGroup(R.id.alternative_group);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMainActivity().addPlayerReadyCallback(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getMainActivity().removePlayerReadyCallback(this);
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
