package com.sanron.music.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.db.model.Music;
import com.sanron.music.service.IPlayer;

import java.util.List;

/**
 * 播放队列窗口
 */
public class ShowQueueMusicWindow extends PopupWindow implements IPlayer.Callback, View.OnClickListener {

    private Activity mActivity;
    private float mOldAlpha;
    private IPlayer player;
    private List<Music> queue;

    private View mContentView;
    private TextView tvTitle;
    private ImageButton ibtnRemoveAll;
    private RecyclerView lvQueue;
    private QueueItemAdapter adapter;

    public ShowQueueMusicWindow(final Activity activity, final IPlayer player) {
        super(activity);
        this.player = player;
        this.mActivity = activity;
        this.mContentView = LayoutInflater.from(activity).inflate(R.layout.window_queue_music, null);
        this.queue = player.getQueue();
        player.addCallback(this);
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        setFocusable(true);
        setTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(screenHeight / 2);
        setAnimationStyle(R.style.MyWindowAnim);
        setContentView(mContentView);

        tvTitle = (TextView) mContentView.findViewById(R.id.tv_title);
        ibtnRemoveAll = (ImageButton) mContentView.findViewById(R.id.ibtn_remove_all);
        lvQueue = (RecyclerView) mContentView.findViewById(R.id.lv_queue_music);

        tvTitle.setText("播放队列(" + queue.size() + ")");

        adapter = new QueueItemAdapter();
        lvQueue.setLayoutManager(new LinearLayoutManager(mActivity));
        lvQueue.setAdapter(adapter);
        lvQueue.post(new Runnable() {
            @Override
            public void run() {
                lvQueue.scrollToPosition(player.getCurrentIndex());
            }
        });

        ibtnRemoveAll.setOnClickListener(this);
    }

    public void show() {
        showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        animateShow();
    }

    //activity背景恢复动画
    private void animateDismiss() {
        final WindowManager.LayoutParams attr = mActivity.getWindow().getAttributes();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.7f, mOldAlpha);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                attr.alpha = (float) animation.getAnimatedValue();
                mActivity.getWindow().setAttributes(attr);
            }
        });
        valueAnimator.start();
    }

    //activity背景变暗动画
    private void animateShow() {
        final WindowManager.LayoutParams attr = mActivity.getWindow().getAttributes();
        mOldAlpha = attr.alpha;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(attr.alpha, 0.7f);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                attr.alpha = (float) animation.getAnimatedValue();
                mActivity.getWindow().setAttributes(attr);
            }
        });
        valueAnimator.start();
    }

    @Override
    public void dismiss() {
        animateDismiss();
        super.dismiss();
        player.removeCallback(this);
    }

    @Override
    public void onLoadedPicture(Bitmap musicPic) {

    }

    @Override
    public void onStateChange(int state) {
        if (state == IPlayer.STATE_PREPARING) {
            adapter.notifyDataSetChanged();
            lvQueue.scrollToPosition(player.getCurrentIndex());
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_remove_all: {
                player.clearQueue();
                queue.clear();
                tvTitle.setText("播放队列(0)");
                adapter.notifyDataSetChanged();
            }
            break;
        }
    }


    public class QueueItemAdapter extends RecyclerView.Adapter<QueueItemAdapter.QueueItemHolder> {


        final int DEFAULT_TITLE_COLOR = mActivity.getResources().getColor(R.color.textColorPrimary);
        final int DEFAULT_ARTIST_COLOR = mActivity.getResources().getColor(R.color.textColorSecondary);
        final int PLAY_TEXT_COLOR = mActivity.getResources().getColor(R.color.colorAccent);

        @Override
        public QueueItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mActivity).inflate(R.layout.list_queue_item, parent, false);
            return new QueueItemHolder(view);
        }

        public Music getItem(int position) {
            return queue.get(position);
        }

        @Override
        public void onBindViewHolder(QueueItemHolder holder, final int position) {
            Music music = getItem(position);

            if (position == player.getCurrentIndex()) {
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
                    player.dequeue(position);
                    queue.remove(position);
                    tvTitle.setText("播放队列(" + queue.size() + ")");
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return queue == null ? 0 : queue.size();
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
                tvArtist = (TextView) itemView.findViewById(R.id.tv_artist);
                ibtnRemove = (ImageButton) itemView.findViewById(R.id.ibtn_remove);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int adapterPos = getAdapterPosition();
                        if (player.getCurrentIndex() == adapterPos) {
                            player.togglePlayPause();
                            return;
                        }
                        player.play(adapterPos);
                    }
                });
            }
        }
    }

}
