package com.sanron.music.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.db.model.Music;
import com.sanron.music.net.bean.Song;
import com.sanron.music.service.IPlayer;

import java.util.LinkedList;
import java.util.List;

public class SongItemAdapter extends BaseAdapter {

    private Context context;
    private IPlayer player;
    private List<Song> data;
    private int playingPosition = -1;//
    private int playingTextColor;//播放中文字颜色
    private int normalTitleTextColor;//正常title颜色
    private int normalArtistTextColor;//正常artist颜色

    public SongItemAdapter(Context context, IPlayer player) {
        this.context = context;
        this.player = player;
        Resources resources = context.getResources();
        playingTextColor = resources.getColor(R.color.colorAccent);
        normalTitleTextColor = resources.getColor(R.color.textColorPrimary);
        normalArtistTextColor = resources.getColor(R.color.textColorSecondary);
    }

    public void setPlayingPosition(int position) {
        playingPosition = position;
        notifyDataSetChanged();
    }



    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    public void setData(List<Song> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public List<Song> getData() {
        return data;
    }

    public void addData(List<Song> songs) {
        if (data == null) {
            data = songs;
        } else {
            data.addAll(songs);
        }
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Song song = data.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_list_song_item, parent, false);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder == null) {
            holder = new ViewHolder();
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tvArtist = (TextView) convertView.findViewById(R.id.tv_artist);
            holder.ivMv = (ImageView) convertView.findViewById(R.id.iv_mv);
        }


        if (position == playingPosition) {
            holder.tvTitle.setTextColor(playingTextColor);
            holder.tvArtist.setTextColor(playingTextColor);
        } else {
            holder.tvTitle.setTextColor(normalTitleTextColor);
            holder.tvArtist.setTextColor(normalArtistTextColor);
        }
        holder.tvTitle.setText(song.title);
        holder.tvArtist.setText(song.author);
        if (song.hasMv == 1) {
            holder.ivMv.setVisibility(View.VISIBLE);
        } else {
            holder.ivMv.setVisibility(View.GONE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Song> songs = data;
                List<Music> musics = new LinkedList<>();
                for (Song song : songs) {
                    musics.add(song.toMusic());
                }
                player.clearQueue();
                player.enqueue(musics);
                player.play(position);
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView tvTitle;
        TextView tvArtist;
        ImageView ivMv;
    }
}