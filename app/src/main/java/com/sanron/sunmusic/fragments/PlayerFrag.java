package com.sanron.sunmusic.fragments;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.service.IMusicPlayer;
import com.sanron.sunmusic.service.PlayerUtils;
import com.sanron.sunmusic.utils.T;

import java.io.File;

/**
 * Created by Administrator on 2016/3/5.
 */
public class PlayerFrag extends BaseFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {


    private ViewGroup smallPlayer;
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
    private ImageButton ibtnChangeMode;
    private ImageButton ibtnLast;
    private ImageButton ibtnPlayPause;
    private ImageButton ibtnNext;
    private ImageButton ibtnPlayQuque;

    private boolean isTouchingTracking = false;//是否在滑动进度条

    private IMusicPlayer player = PlayerUtils.getService();

    private UpdateProgressThread updateProgressThread;

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
            if(pause) {
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
                playProgress.setProgress(position == -1 ? 0 : position);
                SystemClock.sleep(1000);
            }
        }
    }

    private IMusicPlayer.Callback callback = new IMusicPlayer.Callback() {
        @Override
        public void onStateChange(int state) {
            switch (state) {
                case IMusicPlayer.STATE_STOP:
                case IMusicPlayer.STATE_PAUSE: {
                    setStateIcon(R.mipmap.ic_play_arrow_black_36dp);
                    updateProgressThread.pause();
                }
                break;

                case IMusicPlayer.STATE_PLAYING: {
                    setStateIcon(R.mipmap.ic_pause_black_36dp);
                    updateProgressThread.running();
                }
                break;
            }
        }

        @Override
        public void onStartPlay(int position) {
            if (position > 0) {
                SongInfo curSong = player.getQuque().get(position);
                setTitleText(curSong.getTitle());
                setArtistText(curSong.getArtist());

                String picPath = curSong.getPicPath();
                if (TextUtils.isEmpty(picPath)) {
                    ivSongPicture.setImageResource(R.mipmap.default_big_song_pic);
                    sivSongPicture.setImageResource(R.mipmap.default_song_pic);
                } else {
                    File file = new File(picPath);
                    if (!file.exists()) {
                        ivSongPicture.setImageResource(R.mipmap.default_big_song_pic);
                        sivSongPicture.setImageResource(R.mipmap.default_song_pic);
                    }
                }
                int duration = player.getDuration();
                playProgress.setMax(duration == -1 ? 0 : duration);
                playProgress.setProgress(0);
            } else {
                ivSongPicture.setImageResource(R.mipmap.default_big_song_pic);
                sivSongPicture.setImageResource(R.mipmap.default_song_pic);
                setTitleText("Sun Music");
                setArtistText("");
            }
        }

        @Override
        public void onModeChange(int newMode) {
            int iconId = 0;
            switch (newMode) {
                case IMusicPlayer.MODE_IN_TURN:
                    iconId = R.mipmap.ic_repeat_black_24dp;
                    break;
                case IMusicPlayer.MODE_LOOP:
                    iconId = R.mipmap.ic_repeat_one_black_24dp;
                    break;
                case IMusicPlayer.MODE_RANDOM:
                    iconId = R.mipmap.ic_shuffle_black_24dp;
                    break;
            }
            ibtnChangeMode.setImageResource(iconId);
        }
    };

    public void setTitleText(String title) {
        stvTitle.setText(title);
        tvTitle.setText(title);
    }

    public void setArtistText(String artist) {
        stvArtist.setText(artist);
        tvArtist.setText(artist);
    }

    public void setStateIcon(int resid) {
        sibtnPlayPause.setImageResource(resid);
        ibtnPlayPause.setImageResource(resid);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        player.addCallback(callback);
        updateProgressThread = new UpdateProgressThread();
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
        playProgress.setOnSeekBarChangeListener(this);

        int state = player.getState();
        int mode = player.getPlayMode();
        callback.onModeChange(mode);
        callback.onStateChange(state);
        callback.onStartPlay(player.getCurrentIndex());

        updateProgressThread.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        player.removeCallback(callback);
        updateProgressThread.end();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.removeCallback(callback);
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
                if (state == IMusicPlayer.STATE_PAUSE
                        || state == IMusicPlayer.STATE_STOP) {
                    if (player.getQuque().size() == 0) {
                        T.show(getContext(), "播放列表为空");
                        return;
                    }
                    player.play();
                } else if (state == IMusicPlayer.STATE_PLAYING) {
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

            }
            break;
        }
    }


    public void setSmallControllerVisibility(int visibility) {
        smallPlayer.setVisibility(visibility);
    }
}
