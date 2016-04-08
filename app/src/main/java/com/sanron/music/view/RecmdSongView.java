package com.sanron.music.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sanron.music.R;

/**
 * Created by sanron on 16-3-22.
 */
public class RecmdSongView extends LinearLayout {

    private ImageView ivPic;
    private TextView tvTitle;
    private TextView tvReason;

    public RecmdSongView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_recmd_song,this);
        ivPic = (ImageView) findViewById(R.id.iv_recmd_pic);
        tvTitle = (TextView) findViewById(R.id.tv_recmd_name);
        tvReason = (TextView) findViewById(R.id.tv_recmd_reason);
    }

    public ImageView getPicView() {
        return ivPic;
    }

    public TextView getTitleView() {
        return tvTitle;
    }

    public TextView getReasonView() {
        return tvReason;
    }
}
