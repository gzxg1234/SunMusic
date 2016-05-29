package com.sanron.ddmusic.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sanron on 16-4-24.
 */
public class RankSongAdapter extends SongAdapter {


    public RankSongAdapter(Context context) {
        super(context);
        setShowPicture(true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        return view;
    }

}
