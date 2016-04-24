package com.sanron.music.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.api.bean.BillSongList;

/**
 * Created by sanron on 16-4-24.
 */
public class RankSongItemAdapter extends SongItemAdapter {


    public RankSongItemAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = super.getView(position, convertView, parent);
        SongItemHolder holder = (SongItemHolder) view.getTag();
        holder.ivPicture.setVisibility(View.VISIBLE);
        final BillSongList.RankSong rankSong = (BillSongList.RankSong) getItem(position);
        holder.ivPicture.setImageBitmap(null);
        ImageLoader.getInstance()
                .cancelDisplayTask(holder.ivPicture);
        String pic = rankSong.picBig;
        if (TextUtils.isEmpty(pic)) {
            pic = rankSong.picSmall;
        }
        ImageLoader.getInstance()
                .displayImage(pic, holder.ivPicture);
        return view;
    }

}
