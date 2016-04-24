package com.sanron.music.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.api.bean.Singer;

/**
 * Created by sanron on 16-4-19.
 */
public class SingerDetailDialog extends Dialog {

    private ImageView mIvPicture;
    private TextView mTvName;
    private TextView mTvArea;
    private TextView mTvSex;
    private TextView mTvBirthday;
    private TextView mTvConstellation;
    private TextView mTvWeight;
    private TextView mTvStature;
    private TextView mTvIntro;
    private View mClose;

    public SingerDetailDialog(Context context, Singer singer) {
        super(context);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_singer_detail);
        mIvPicture = $(R.id.iv_singer_pic);
        mTvName = $(R.id.tv_name);
        mTvArea = $(R.id.tv_area);
        mTvSex = $(R.id.tv_sex);
        mTvBirthday = $(R.id.tv_birthday);
        mTvConstellation = $(R.id.tv_constellation);
        mTvWeight = $(R.id.tv_weight);
        mTvStature = $(R.id.tv_stature);
        mTvIntro = $(R.id.tv_intro);
        mClose = $(R.id.close);

        mTvName.setText(singer.name);
        mTvArea.setText("地区:  " + singer.country);
        String sex = null;
        if ("0".equals(singer.gender)) {
            sex = "男";
        } else if ("1".equals(singer.gender)) {
            sex = "女";
        } else if ("2".equals(singer.gender)) {
            sex = "组合";
        }
        mTvSex.setText("性别:  " + sex);
        if (TextUtils.isEmpty(singer.birth)
                || "0000-00-00".equals(singer.birth)) {
            mTvBirthday.setVisibility(View.GONE);
        } else {
            mTvBirthday.setText("生日:  " + singer.birth);
        }
        mTvConstellation.setText("星座:  " + singer.constellation);
        if (TextUtils.isEmpty(singer.weight)
                || "0.00".equals(singer.weight)) {
            mTvWeight.setVisibility(View.GONE);
        } else {
            mTvWeight.setText("体重:  " + singer.weight + "kg");
        }
        if (TextUtils.isEmpty(singer.stature)
                || "0.00".equals(singer.stature)) {
            mTvStature.setVisibility(View.GONE);
        } else {
            mTvStature.setText("身高:  " + singer.stature + "cm");
        }
        mTvIntro.setText(singer.intro);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    public void setAvatar(Bitmap bitmap) {
        mIvPicture.setImageBitmap(bitmap);
    }

    public <T extends View> T $(int id) {
        return (T) findViewById(id);
    }
}
