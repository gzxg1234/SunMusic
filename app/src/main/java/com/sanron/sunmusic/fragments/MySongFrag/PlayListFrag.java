package com.sanron.sunmusic.fragments.MySongFrag;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.fragments.BaseFragment;

/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListFrag extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.frag_playlist,null);
        return contentView;
    }

    public static PlayListFrag newInstance(){
        return new PlayListFrag();
    }
}
