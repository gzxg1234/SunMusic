package com.sanron.music.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sanron.music.R;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.Playable;
import com.sanron.music.utils.TUtils;
import com.sanron.music.view.ShowQueueMusicWindow;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 播放界面
 * Created by Administrator on 2016/3/5.
 */
public class PlayerFrag extends BaseFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {


    private ViewGroup smallPlayer;
    private ProgressBar splayProgress;
    private ImageView sivSongPicture;
    private TextView stvTitle;
    private TextView stvArtist;
    private ImageButton sibtnPlayPause;
    private ImageButton sibtnNext;

    private ViewGroup bigPlayer;
    private SeekBar playProgress;
    private ImageView ivSongPicture;
    private TextView tvTitle;
    private TextView tvArtist;
    private TextView tvPlayPostion;
    private TextView tvDuration;
    private ImageButton ibtnBack;
    private ImageButton ibtnChangeMode;
    private ImageButton ibtnLast;
    private ImageButton ibtnPlayPause;
    private ImageButton ibtnNext;
    private ImageButton ibtnPlayQuque;

    private ImageLoader imageLoader = ImageLoader.getInstance();


    //刷新播放进度进程
    private UpdateProgressThread updateProgressThread;

    //点击左上角回退键事件
    public static final int EVENT_CLICK_BACK = 1;


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        updateProgressThread.pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        player.seekTo(seekBar.getProgress());
        updateProgressThread.running();
    }

    private class UpdateProgressThread extends Thread {

        private Object lock = new Object();
        private boolean pause = false;
        private boolean running = true;

        public void pause() {
            pause = true;
        }

        public void running() {
            if (pause) {
                synchronized (lock) {
                    lock.notify();
                    pause = false;
                }
            }
        }

        public void end() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                if (pause) {
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                int position = player.getCurrentPosition();
                final int pos = (position == -1 ? 0 : position);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPlayProgress(pos);
                    }
                });
                SystemClock.sleep(100);
            }
        }
    }

    private IPlayer.Callback callback = new IPlayer.Callback() {
        @Override
        public void onStateChange(int state) {
            switch (state) {
                case IPlayer.STATE_IDEL: {
                    updateProgressThread.pause();
                    setSongPic("");
                    setTitleText(getContext().getString(R.string.app_name));
                    setArtistText("");
                    setSongDuration(0);
                    setPlayProgress(0);
                    sibtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
                    ibtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_black_48dp);
                }
                break;

                case IPlayer.STATE_PAUSE: {
                    updateProgressThread.pause();
                    sibtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
                    ibtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_black_48dp);
                }
                break;

                case IPlayer.STATE_PLAYING: {
                    updateProgressThread.running();
                    sibtnPlayPause.setImageResource(R.mipmap.ic_pause_black_36dp);
                    ibtnPlayPause.setImageResource(R.mipmap.ic_pause_black_48dp);
                }
                break;
            }
        }

        @Override
        public void onPrepared() {
            Playable playable = player.getQueue().get(player.getCurrentIndex());
            setTitleText(playable.title());
            setArtistText(playable.artist());
            setSongPic(playable.pic());

            setSongDuration(player.getDuration());
            setPlayProgress(0);
            int type = playable.type();
            if (type == Playable.TYPE_FILE) {
                playProgress.setSecondaryProgress(player.getDuration());
            } else if(type == Playable.TYPE_HTTP){
                playProgress.setSecondaryProgress(0);
            }
            updateProgressThread.running();
        }

        @Override
        public void onModeChange(int newMode) {
            int iconId = 0;
            switch (newMode) {
                case IPlayer.MODE_IN_TURN:
                    iconId = R.mipmap.ic_repeat_black_24dp;
                    break;
                case IPlayer.MODE_LOOP:
                    iconId = R.mipmap.ic_repeat_one_black_24dp;
                    break;
                case IPlayer.MODE_RANDOM:
                    iconId = R.mipmap.ic_shuffle_black_24dp;
                    break;
            }
            ibtnChangeMode.setImageResource(iconId);
        }

        @Override
        public void onBufferingUpdate(int bufferedPosition) {
            if (bufferedPosition > playProgress.getSecondaryProgress()) {
                playProgress.setSecondaryProgress(bufferedPosition);
            }
        }
    };

    public void setSongPic(String picPath){
        DisplayImageOptions options = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY).build();
        if(!TextUtils.isEmpty(picPath)){
            File file = new File(picPath);
            if(file.exists()){
                imageLoader.displayImage("file://" + picPath, ivSongPicture, options);
                imageLoader.displayImage("file://" + picPath, sivSongPicture, options);
                return;
            }
        }

        ivSongPicture.setImageResource(R.mipmap.default_big_song_pic);
        sivSongPicture.setImageResource(R.mipmap.default_song_pic);
        //尝试从网络获取

    }

    private void setTitleText(String title) {
        stvTitle.setText(title);
        tvTitle.setText(title);
    }

    private void setArtistText(String artist) {
        stvArtist.setText(artist);
        tvArtist.setText(artist);
    }

    private void setPlayProgress(int position) {
        playProgress.setProgress(position);
        splayProgress.setProgress(position);
        tvPlayPostion.setText(formatTime(position));
    }

    private void setSongDuration(int duration) {
        playProgress.setMax(duration);
        splayProgress.setMax(duration);
        tvDuration.setText(formatTime(duration));
    }

    private String formatTime(int millis){
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        return sdf.format(new Date(millis));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.frag_player, null);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        smallPlayer = $(R.id.small_player);
        splayProgress = $(R.id.s_play_progress);
        sivSongPicture = $(R.id.s_iv_song_pic);
        stvTitle = $(R.id.s_tv_title);
        stvArtist = $(R.id.s_tv_artist);
        sibtnPlayPause = $(R.id.s_ibtn_play_pause);
        sibtnNext = $(R.id.s_ibtn_next);
        sibtnPlayPause.setOnClickListener(this);
        sibtnNext.setOnClickListener(this);

        bigPlayer = $(R.id.big_player);
        ivSongPicture = $(R.id.iv_song_picture);
        tvTitle = $(R.id.tv_title);
        tvArtist = $(R.id.tv_artist);
        tvDuration = $(R.id.tv_song_duration);
        tvPlayPostion = $(R.id.tv_play_position);
        ibtnBack = $(R.id.ibtn_back);
        ibtnChangeMode = $(R.id.ibtn_play_mode);
        ibtnLast = $(R.id.ibtn_last);
        ibtnPlayPause = $(R.id.ibtn_play_pause);
        ibtnNext = $(R.id.ibtn_next);
        ibtnPlayQuque = $(R.id.ibtn_play_quque);
        playProgress = $(R.id.play_progress);

        ibtnChangeMode.setOnClickListener(this);
        ibtnLast.setOnClickListener(this);
        ibtnPlayPause.setOnClickListener(this);
        ibtnNext.setOnClickListener(this);
        ibtnPlayQuque.setOnClickListener(this);
        ibtnBack.setOnClickListener(this);
        playProgress.setOnSeekBarChangeListener(this);

        if(savedInstanceState != null){
            int smallVisibility = savedInstanceState.getInt("smallPlayerVisibility",View.VISIBLE);
            smallPlayer.setVisibility(smallVisibility);
        }

        updateProgressThread = new UpdateProgressThread();
        updateProgressThread.start();

        player.addCallback(callback);

        callback.onModeChange(player.getState());
        callback.onStateChange(player.getPlayMode());
        if (player.getCurrentPosition() != -1)
            callback.onPrepared();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        player.removeCallback(callback);
        updateProgressThread.end();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ibtn_play_mode: {
                player.setPlayMode((player.getPlayMode() + 1) % 3);
            }
            break;

            case R.id.ibtn_last: {
                player.last();
            }
            break;

            case R.id.s_ibtn_play_pause:
            case R.id.ibtn_play_pause: {
                int state = player.getState();
                if (state == IPlayer.STATE_PAUSE) {
                    player.play();
                }else if(state == IPlayer.STATE_IDEL){
                    if(player.getQueue().size() > 0){
                        player.play(0);
                    }else{
                        TUtils.show(getContext(), "播放列表为空");
                    }
                }else if (state == IPlayer.STATE_PLAYING) {
                    player.pause();
                }
            }
            break;

            case R.id.s_ibtn_next:
            case R.id.ibtn_next: {
                player.next();
            }
            break;

            case R.id.ibtn_play_quque: {
                new ShowQueueMusicWindow(getActivity(),player).show();
            }
            break;

            case R.id.ibtn_back: {
                Intent intent = new Intent(PlayerFrag.class.getName());
                intent.putExtra("event", EVENT_CLICK_BACK);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
            }
            break;
        }
    }

    //主界面底部的小控制面板可见性
    public void setSmallControllerVisibility(int visibility) {
        smallPlayer.setVisibility(visibility);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("smallPlayerVisibility",smallPlayer.getVisibility());
    }

}
