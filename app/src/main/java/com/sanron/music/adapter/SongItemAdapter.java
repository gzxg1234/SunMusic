package com.sanron.music.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.db.bean.Music;
import com.sanron.music.net.bean.Song;
import com.sanron.music.service.PlayerUtil;

import java.util.LinkedList;
import java.util.List;

public class SongItemAdapter extends BaseAdapter {

    private Context mContext;
    private List<Song> mData;
    private int mPlayingPosition = -1;//
    private int mPlayingTextColor;//播放中文字颜色
    private int mNormalTitleTextColor;//正常title颜色
    private int mNormalArtistTextColor;//正常artist颜色

    public SongItemAdapter(Context context) {
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
        return mData == null ? 0 : mData.size();
    }

    public void setData(List<Song> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public List<Song> getData() {
        return mData;
    }

    public void addData(List<Song> songs) {
        if (mData == null) {
            mData = songs;
        } else {
            mData.addAll(songs);
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Song song = mData.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_song_item, parent, false);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder == null) {
            holder = new ViewHolder();
            holder.tvText1 = (TextView) convertView.findViewById(R.id.tv_text1);
            holder.tvText2 = (TextView) convertView.findViewById(R.id.tv_text2);
            holder.ivMv = (ImageView) convertView.findViewById(R.id.iv_mv);
        }

        Music playingMusic = PlayerUtil.getCurrentMusic();
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
            holder.ivMv.setVisibility(View.VISIBLE);
        } else {
            holder.ivMv.setVisibility(View.GONE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayingPosition == position) {
                    PlayerUtil.togglePlayPause();
                    return;
                }

                List<Song> songs = mData;
                List<Music> musics = new LinkedList<>();
                for (Song song : songs) {
                    musics.add(song.toMusic());
                }
                PlayerUtil.clearQueue();
                PlayerUtil.enqueue(musics);
                PlayerUtil.play(position);
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView tvText1;
        TextView tvText2;
        ImageView ivMv;
    }
}