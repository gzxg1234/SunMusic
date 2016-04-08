package com.sanron.music.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sanron.music.R;

/**
 * Created by sanron on 16-3-21.
 */
public class HotSongListView extends CardView {

    private ImageView ivPic;
    private TextView tvTitle;

    public HotSongListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_hot_songlist, this);
        ivPic = (ImageView) findViewById(R.id.iv_songlist_pic);
        tvTitle = (TextView) findViewById(R.id.tv_songlist_title);
    }

    public ImageView getImageView() {
        return ivPic;
    }

    public TextView getTitleView() {
        return tvTitle;
    }
}
