package com.sanron.ddmusic.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.ddmusic.R;
import com.sanron.ddmusic.api.bean.Song;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.service.PlayUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SongAdapter extends BaseAdapter {

    private Context mContext;
    private List<Song> mData = new ArrayList<>();
    private boolean mIsShowPicture;
    private int mPlayingPosition = -1;//
    private int mPlayingTextColor;//播放中文字颜色
    private int mNormalTitleTextColor;//正常title颜色
    private int mNormalArtistTextColor;//正常artist颜色

    public SongAdapter(Context context) {
        this.mContext = context;
        Resources resources = context.getResources();
        mPlayingTextColor = resources.getColor(R.color.colorAccent);
        mNormalTitleTextColor = resources.getColor(R.color.textColorPrimary);
        mNormalArtistTextColor = resources.getColor(R.color.textColorSecondary);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
    }

    public void setPlayingPosition(int position) {
        mPlayingPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public void setData(List<? extends Song> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public List<Song> getData() {
        return mData;
    }

    public void addData(List<? extends Song> data) {
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    protected String onBindText1(Song song) {
        return song.title;
    }

    protected String onBindText2(Song song) {
        return song.author;
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setShowPicture(boolean showPicture) {
        mIsShowPicture = showPicture;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Song song = mData.get(position);
        CommonItemViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_common_item, parent, false);
        } else {
            holder = (CommonItemViewHolder) convertView.getTag();
        }

        if (holder == null) {
            holder = new CommonItemViewHolder(convertView);
            holder.ivMenu.setImageResource(R.mipmap.ic_more_vert_black_24dp);
            convertView.setTag(holder);
        }

        if (mIsShowPicture) {
            holder.ivPicture.setVisibility(View.VISIBLE);
            holder.ivPicture.setImageBitmap(null);
            ImageLoader.getInstance()
                    .cancelDisplayTask(holder.ivPicture);
            String pic = song.picBig;
            if (TextUtils.isEmpty(pic)) {
                pic = song.picSmall;
            }
            ImageLoader.getInstance()
                    .displayImage(pic, holder.ivPicture);
        } else {
            holder.ivPicture.setVisibility(View.GONE);
        }

        Music playingMusic = PlayUtil.getCurrentMusic();
        if (playingMusic != null
                && song.songId.equals(playingMusic.getSongId())) {
            holder.tvText1.setTextColor(mPlayingTextColor);
            holder.tvText2.setTextColor(mPlayingTextColor);
            mPlayingPosition = position;
        } else {
            holder.tvText1.setTextColor(mNormalTitleTextColor);
            holder.tvText2.setTextColor(mNormalArtistTextColor);
        }
        holder.tvText1.setText(onBindText1(song));
        holder.tvText2.setText(onBindText2(song));
        if (song.hasMv == 1) {
            SpannableStringBuilder ssb = new SpannableStringBuilder("  ");
            ssb.setSpan(new ImageSpan(mContext, R.mipmap.ic_mv, ImageSpan.ALIGN_BASELINE),
                    1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvText1.append(ssb);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayingPosition == position) {
                    PlayUtil.togglePlayPause();
                    return;
                }

                List<Song> songs = mData;
                List<Music> musics = new LinkedList<>();
                for (Song song : songs) {
                    musics.add(song.toMusic());
                }
                PlayUtil.clearQueue();
                PlayUtil.enqueue(musics);
                PlayUtil.play(position);
            }
        });
        return convertView;
    }

}