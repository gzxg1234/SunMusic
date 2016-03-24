package com.sanron.music.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.db.model.Music;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.Playable;

import java.util.List;

/**
 * 播放队列窗口
 */
public class ShowQueueMusicWindow extends PopupWindow implements IPlayer.Callback, AdapterView.OnItemClickListener, View.OnClickListener {

    private Activity mActivity;
    private float mOldAlpha;
    private IPlayer player;
    private List<? extends Playable> queue;

    private View mContentView;
    private TextView tvTitle;
    private ImageButton ibtnRemoveAll;
    private ListView lvQueue;
    private MusicAdapter adapter;

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
        setHeight(screenHeight/2);
        setAnimationStyle(R.style.MyWindow);
        setContentView(mContentView);

        tvTitle = (TextView) mContentView.findViewById(R.id.tv_title);
        ibtnRemoveAll = (ImageButton) mContentView.findViewById(R.id.ibtn_remove_all);
        lvQueue = (ListView) mContentView.findViewById(R.id.lv_queue_music);

        tvTitle.setText("播放队列("+queue.size()+")");

        adapter = new MusicAdapter();
        lvQueue.setAdapter(adapter);
        lvQueue.post(new Runnable() {
            @Override
            public void run() {
                lvQueue.setSelection(player.getCurrentIndex());
            }
        });

        lvQueue.setOnItemClickListener(this);
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
        valueAnimator.setDuration(400);
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
        valueAnimator.setDuration(400);
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
        if(state == IPlayer.STATE_PREPARED){
            adapter.notifyDataSetChanged();
            lvQueue.setSelection(player.getCurrentIndex());
        }
    }

    @Override
    public void onModeChange(int newMode) {
    }

    @Override
    public void onBufferingUpdate(int bufferedPosition) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.lv_queue_music:{
                player.play(position);
            }break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibtn_remove_all:{
                player.play(null,0);
                tvTitle.setText("播放队列(0)");
                adapter.notifyDataSetChanged();
            }break;
        }
    }

    public class MusicAdapter extends BaseAdapter{

        final int DEFAULT_TITLE_COLOR = mActivity.getResources().getColor(R.color.textColorPrimary);
        final int DEFAULT_ARTIST_COLOR = mActivity.getResources().getColor(R.color.textColorSecondary);
        final int PLAY_TEXT_COLOR = mActivity.getResources().getColor(R.color.colorAccent);

        class ViewHolder{
            View sign;
            ImageView ivPic;
            TextView tvTitle;
            TextView tvArtist;
            ImageButton ibtnRemove;
        }

        @Override
        public int getCount() {
            return queue.size();
        }

        @Override
        public Object getItem(int position) {
            return queue.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Music music = (Music) getItem(position);
            ViewHolder holder = null;
            if(convertView == null){
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.list_item_queue,null);
                holder = new ViewHolder();
                holder.sign =  convertView.findViewById(R.id.playing_sign);
                holder.ivPic = (ImageView) convertView.findViewById(R.id.iv_picture);
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
                holder.tvArtist = (TextView) convertView.findViewById(R.id.tv_artist);
                holder.ibtnRemove = (ImageButton) convertView.findViewById(R.id.ibtn_remove);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            if(position == player.getCurrentIndex()){
                holder.tvArtist.setTextColor(PLAY_TEXT_COLOR);
                holder.tvTitle.setTextColor(PLAY_TEXT_COLOR);
                holder.sign.setVisibility(View.VISIBLE);
            }else{
                holder.tvTitle.setTextColor(DEFAULT_TITLE_COLOR);
                holder.tvArtist.setTextColor(DEFAULT_ARTIST_COLOR);
                holder.sign.setVisibility(View.INVISIBLE);
            }

            holder.tvTitle.setText(music.getTitle());
            holder.tvArtist.setText(music.getArtist());
            holder.ibtnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    player.dequeue(position);
                    tvTitle.setText("播放队列("+queue.size()+")");
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }
}
