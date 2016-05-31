package com.sanron.ddmusic.fragments.mymusic;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.sanron.ddmusic.fragments.base.LazyLoadFragment;

/**
 * Created by sanron on 16-3-28.
 */
public abstract class BaseDataFragment extends LazyLoadFragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
