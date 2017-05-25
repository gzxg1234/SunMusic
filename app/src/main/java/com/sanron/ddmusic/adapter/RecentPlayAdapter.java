package com.sanron.ddmusic.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanron.ddmusic.R;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.service.PlayUtil;

import java.util.ArrayList;
import java.util.List;

public class RecentPlayAdapter extends RecyclerView.Adapter {


    private int mPlayingTextColor;//播放中文字颜色
    private int mNormalTitleTextColor;//正常title颜色
    private int mNormalArtistTextColor;//正常artist颜色
    private int mPlayingPosition = -1;
    public List<Music> mData = new ArrayList<>();
    private Context mContext;

    public RecentPlayAdapter(Context context) {
        mContext = context;
        Resources resources = context.getResources();
        mPlayingTextColor = resources.getColor(R.color.colorAccent);
        mNormalTitleTextColor = resources.getColor(R.color.textColorPrimary);
        mNormalArtistTextColor = resources.getColor(R.color.textColorSecondary);
    }

    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    public void setPlayingPosition(int playingPosition) {
        mPlayingPosition = playingPosition;
        notifyItemChanged(playingPosition);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if (viewType == 0) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.recent_play_header, parent, false);
            viewHolder = new HeaderHolder(view);
        } else {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.list_common_item, parent, false);
            CommonItemViewHolder holder = new CommonItemViewHolder(view);
            holder.ivPicture.setVisibility(View.GONE);
            holder.ivMenu.setVisibility(View.GONE);
            viewHolder = holder;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof HeaderHolder) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mData.isEmpty()) {
                        ViewTool.show("无播放记录");
                        return;
                    }
                    PlayUtil.clearQueue();
                    PlayUtil.enqueue(mData);
                    PlayUtil.play(0);
                }
            });
        } else if (holder instanceof CommonItemViewHolder) {
            final Music music = mData.get(position - 1);
            String artist = music.getArtist();
            if ("<unknown>".equals(artist)) {
                artist = "未知歌手";
            }
            CommonItemViewHolder commonItemViewHolder = (CommonItemViewHolder) holder;
            commonItemViewHolder.tvText1.setText(music.getTitle());
            commonItemViewHolder.tvText2.setText(artist);
            Music curMusic = PlayUtil.getCurrentMusic();
            if (curMusic != null
                    && (curMusic.getId() == music.getId())) {
                mPlayingPosition = position;
                commonItemViewHolder.tvText1.setTextColor(mPlayingTextColor);
                commonItemViewHolder.tvText2.setTextColor(mPlayingTextColor);
            } else {
                commonItemViewHolder.tvText1.setTextColor(mNormalTitleTextColor);
                commonItemViewHolder.tvText2.setTextColor(mNormalArtistTextColor);
            }

            commonItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayUtil.clearQueue();
                    PlayUtil.enqueue(mData);
                    PlayUtil.play(position - 1);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    public void setData(List<Music> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    class HeaderHolder extends RecyclerView.ViewHolder {
        public HeaderHolder(View itemView) {
            super(itemView);
        }
    }
}