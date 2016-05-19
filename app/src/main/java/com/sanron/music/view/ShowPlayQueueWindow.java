package com.sanron.music.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.db.bean.Music;
import com.sanron.music.playback.Player;
import com.sanron.music.service.PlayerUtil;

import java.util.List;

/**
 * 播放队列窗口
 */
public class ShowPlayQueueWindow extends ScrimPopupWindow implements Player.OnPlayStateChangeListener, View.OnClickListener {

    private List<Music> mQueue;
    private Context context;
    private View mContentView;
    private TextView mTvTitle;
    private ImageButton mIbtnRemoveAll;
    private RecyclerView mLvQueue;
    private QueueItemAdapter mAdapter;

    public ShowPlayQueueWindow(Activity activity) {
        super(activity);
        this.context = activity;
        this.mContentView = LayoutInflater.from(activity).inflate(R.layout.window_queue_music, null);
        this.mQueue = PlayerUtil.getQueue();
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        setFocusable(true);
        setTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(screenHeight / 2);
        setAnimationStyle(R.style.MyWindowAnim);
        setContentView(mContentView);

        mTvTitle = (TextView) mContentView.findViewById(R.id.tv_title);
        mIbtnRemoveAll = (ImageButton) mContentView.findViewById(R.id.ibtn_remove_all);
        mLvQueue = (RecyclerView) mContentView.findViewById(R.id.lv_queue_music);

        mTvTitle.setText("播放队列(" + mQueue.size() + ")");

        mAdapter = new QueueItemAdapter();
        mLvQueue.setLayoutManager(new LinearLayoutManager(context));
        mLvQueue.setAdapter(mAdapter);
        mLvQueue.post(new Runnable() {
            @Override
            public void run() {
                mLvQueue.scrollToPosition(PlayerUtil.getCurrentIndex());
            }
        });

        mIbtnRemoveAll.setOnClickListener(this);
        PlayerUtil.addPlayStateChangeListener(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        PlayerUtil.removePlayStateChangeListener(this);
    }


    @Override
    public void onPlayStateChange(int state) {
        if (state == Player.STATE_PREPARING) {
            mAdapter.notifyDataSetChanged();
            mLvQueue.scrollToPosition(PlayerUtil.getCurrentIndex());
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_remove_all: {
                PlayerUtil.clearQueue();
                mQueue.clear();
                mTvTitle.setText("播放队列(0)");
                mAdapter.notifyDataSetChanged();
            }
            break;
        }
    }


    public class QueueItemAdapter extends RecyclerView.Adapter<QueueItemAdapter.QueueItemHolder> {


        final int DEFAULT_TITLE_COLOR = context.getResources().getColor(R.color.textColorPrimary);
        final int DEFAULT_ARTIST_COLOR = context.getResources().getColor(R.color.textColorSecondary);
        final int PLAY_TEXT_COLOR = context.getResources().getColor(R.color.colorAccent);

        @Override
        public QueueItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_queue_item, parent, false);
            return new QueueItemHolder(view);
        }

        public Music getItem(int position) {
            return mQueue.get(position);
        }

        @Override
        public void onBindViewHolder(QueueItemHolder holder, final int position) {
            Music music = getItem(position);

            if (position == PlayerUtil.getCurrentIndex()) {
                holder.tvArtist.setTextColor(PLAY_TEXT_COLOR);
                holder.tvTitle.setTextColor(PLAY_TEXT_COLOR);
                holder.sign.setVisibility(View.VISIBLE);
            } else {
                holder.tvTitle.setTextColor(DEFAULT_TITLE_COLOR);
                holder.tvArtist.setTextColor(DEFAULT_ARTIST_COLOR);
                holder.sign.setVisibility(View.INVISIBLE);
            }

            holder.tvTitle.setText(music.getTitle());
            String artist = music.getArtist();
            artist = artist == null || artist.equals("<unknown>") ? "未知歌手" : artist;
            holder.tvArtist.setText(artist);
            holder.ibtnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayerUtil.dequeue(position);
                    mQueue.remove(position);
                    mTvTitle.setText("播放队列(" + mQueue.size() + ")");
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mQueue == null ? 0 : mQueue.size();
        }

        class QueueItemHolder extends RecyclerView.ViewHolder {
            View sign;
            TextView tvTitle;
            TextView tvArtist;
            ImageButton ibtnRemove;

            public QueueItemHolder(View itemView) {
                super(itemView);
                sign = itemView.findViewById(R.id.playing_sign);
                tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
                tvArtist = (TextView) itemView.findViewById(R.id.tv_queue_item_artist);
                ibtnRemove = (ImageButton) itemView.findViewById(R.id.ibtn_remove);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int adapterPos = getAdapterPosition();
                        if (PlayerUtil.getCurrentIndex() == adapterPos) {
                            PlayerUtil.togglePlayPause();
                            return;
                        }
                        PlayerUtil.play(adapterPos);
                    }
                });
            }
        }
    }

}
