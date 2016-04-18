package com.sanron.music.ui.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.sanron.music.R;
import com.sanron.music.ui.BaseSlideWebFrag;

/**
 * Created by sanron on 16-4-19.
 */
public class SingerListFrag extends BaseSlideWebFrag {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_singer_list, container, false);
    }

    @Override
    protected void loadData() {
    }
}
