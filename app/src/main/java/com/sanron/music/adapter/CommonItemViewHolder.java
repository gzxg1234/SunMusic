package com.sanron.music.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.sanron.music.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sanron on 16-5-28.
 */
public class CommonItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_text1)
    public TextView tvText1;
    @BindView(R.id.tv_text2)
    public TextView tvText2;
    @BindView(R.id.iv_picture)
    public  RoundedImageView ivPicture;
    @BindView(R.id.iv_menu)
    public ImageView ivMenu;

    public CommonItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
