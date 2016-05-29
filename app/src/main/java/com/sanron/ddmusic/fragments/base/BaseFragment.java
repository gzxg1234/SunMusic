package com.sanron.ddmusic.fragments.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanron.ddmusic.R;
import com.sanron.ddmusic.activities.MainActivity;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/3/5.
 */
public abstract class BaseFragment extends Fragment implements MainActivity.BackPressedHandler, MainActivity.PlayerReadyCallback {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public int getViewResId() {
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = null;
        int resId = getViewResId();
        if (resId != 0) {
            root = inflater.inflate(resId, container, false);
            ButterKnife.bind(this, root);
        }
        return root;
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
