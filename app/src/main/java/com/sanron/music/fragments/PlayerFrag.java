package com.sanron.music.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.sanron.music.R;
import com.sanron.music.db.model.Music;
import com.sanron.music.service.IPlayer;
import com.sanron.music.utils.T;
import com.sanron.music.view.ShowQueueMusicWindow;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 播放界面
 * Created by Administrator on 2016/3/5.
 */
public class PlayerFrag extends BaseFragment implements View.OnClickListener, View.OnTouchListener, IPlayer.OnPlayStateChangeListener, IPlayer.OnBufferListener, IPlayer.OnLoadedPictureListener {

    private ViewGroup smallPlayer;
    private ProgressBar splayProgress;
    private ImageView sivSongPicture;
    private TextView stvTitle;
    private TextView stvArtist;
    private ImageButton sibtnPlayPause;
    private ImageButton sibtnNext;

    private ViewGroup bigPlayer;
    private ViewGroup playerTopBar;
    private LinearLayout ll1;
    private ObjectAnimator colorAnim1;
    private LinearLayout ll2;
    private ObjectAnimator colorAnim2;
    private ProgressBar playProgress;
    private ImageView ivSongPicture;
    private TextView tvTitle;
    private TextView tvArtist;
    private TextView tvPlayPostion;
    private TextView tvDuration;
    private ImageButton ibtnBack;
    private ImageButton ibtnChangeMode;
    private ImageButton ibtnRewind;
    private ImageButton ibtnPlayPause;
    private ImageButton ibtnForward;
    private ImageButton ibtnPlayQuque;
    private ShowQueueMusicWindow showQueueMusicWindow;
    private Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 提示播放模式
     */
    private Toast modeToast;
    private FastLocateThread threadRewind;
    private FastLocateThread threadForward;


    //刷新播放进度进程
    private UpdateProgressThread updateProgressThread;

    //点击左上角回退键事件
    public static final int EVENT_CLICK_BACK = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_player, null);
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
        ll1 = $(R.id.ll_1);
        ll2 = $(R.id.ll_2);
        bigPlayer = $(R.id.big_player);
        playerTopBar = $(R.id.top_bar);
        ivSongPicture = $(R.id.iv_music_picture);
        tvTitle = $(R.id.tv_title);
        tvArtist = $(R.id.tv_artist);
        tvDuration = $(R.id.tv_music_duration);
        tvPlayPostion = $(R.id.tv_music_progress);
        ibtnBack = $(R.id.view_back);
        ibtnChangeMode = $(R.id.ibtn_play_mode);
        ibtnRewind = $(R.id.ibtn_rewind);
        ibtnPlayPause = $(R.id.ibtn_play_pause);
        ibtnForward = $(R.id.ibtn_forward);
        ibtnPlayQuque = $(R.id.ibtn_play_quque);
        playProgress = $(R.id.progress_play);

        sibtnPlayPause.setOnClickListener(this);
        sibtnNext.setOnClickListener(this);
        ibtnChangeMode.setOnClickListener(this);
        ibtnRewind.setOnClickListener(this);
        ibtnPlayPause.setOnClickListener(this);
        ibtnForward.setOnClickListener(this);
        ibtnPlayQuque.setOnClickListener(this);
        ibtnBack.setOnClickListener(this);

        appContext.setViewFitsStatusBar(playerTopBar);

        if (savedInstanceState != null) {
            smallPlayer.setVisibility(savedInstanceState.getInt("smallPlayerVisibility", View.VISIBLE));
            bigPlayer.setVisibility(savedInstanceState.getInt("bigPlayerVisibility", View.VISIBLE));
        }

        updateProgressThread = new UpdateProgressThread();
        updateProgressThread.start();

        player.addPlayStateChangeListener(this);
        player.addOnBufferListener(this);
        player.addOnLoadedPictureListener(this);

        Music music = player.getCurrentMusic();
        int state = player.getState();
        if (state >= IPlayer.STATE_PREPARING) {
            setTitleText(music.getTitle());
            setArtistText(music.getArtist());
        }

        if (state >= IPlayer.STATE_PREPARED) {
            setSongDuration(player.getDuration());
        }

        if (state == IPlayer.STATE_PLAYING) {
            sibtnPlayPause.setImageResource(R.mipmap.ic_pause_black_36dp);
            ibtnPlayPause.setImageResource(R.mipmap.ic_pause_white_24dp);
        } else {
            updateProgressThread.pause();
        }

        setModeIcon(player.getPlayMode());
        onLoadedPicture(player.getCurMusicPic());

        ibtnRewind.setOnTouchListener(this);
        ibtnForward.setOnTouchListener(this);
    }


    @Override
    public void onPlayStateChange(int state) {
        switch (state) {
            case IPlayer.STATE_STOP: {
                updateProgressThread.pause();
                setTitleText(getContext().getString(R.string.app_name));
                setArtistText("");
                setSongDuration(0);
                setPlayProgress(0);
                sibtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
                ibtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
            }
            break;

            case IPlayer.STATE_PAUSE: {
                updateProgressThread.pause();
                sibtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
                ibtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
            }
            break;

            case IPlayer.STATE_PLAYING: {
                updateProgressThread.running();
                sibtnPlayPause.setImageResource(R.mipmap.ic_pause_black_36dp);
                ibtnPlayPause.setImageResource(R.mipmap.ic_pause_white_24dp);
            }
            break;

            case IPlayer.STATE_PREPARING: {
                Music music = player.getCurrentMusic();
                setTitleText(music.getTitle());
                setArtistText(music.getArtist());
                setSongDuration(0);
                setPlayProgress(0);
                playProgress.setSecondaryProgress(0);
            }
            break;

            case IPlayer.STATE_PREPARED: {
                Music music = player.getCurrentMusic();
                setSongDuration(player.getDuration());
                setPlayProgress(player.getProgress());
                if (TextUtils.isEmpty(music.getData())) {
                    playProgress.setSecondaryProgress(0);
                } else {
                    playProgress.setSecondaryProgress(player.getDuration());
                }
            }
            break;
        }
    }

    @Override
    public void onBufferingUpdate(int bufferedPosition) {

        if (bufferedPosition > playProgress.getSecondaryProgress()) {
            playProgress.setSecondaryProgress(bufferedPosition);
        }
    }

    @Override
    public void onBufferStart() {
        System.out.println("buffer start");
    }

    @Override
    public void onBufferEnd() {
        System.out.println("buffer end");
    }

    @Override
    public void onLoadedPicture(Bitmap img) {
        if (img == null) {
            sivSongPicture.setImageResource(R.mipmap.default_song_pic);
            ivSongPicture.setImageResource(R.mipmap.default_song_pic);
        } else {
            sivSongPicture.setImageBitmap(img);
            ivSongPicture.setImageBitmap(img);
        }
        Bitmap bmp = ((BitmapDrawable) ivSongPicture.getDrawable()).getBitmap();
        Palette.generateAsync(bmp, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int oldColor1 = ((ColorDrawable) ll1.getBackground()).getColor();
                int oldColor2 = ((ColorDrawable) ll2.getBackground()).getColor();
                int newColor1 = palette.getDarkVibrantColor(0xFF000000);
                int newColor2 = palette.getDarkMutedColor(0xFF000000);
                animBackgroundColor(oldColor1, newColor1, oldColor2, newColor2);
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean handled = false;
        switch (v.getId()) {
            case R.id.ibtn_rewind: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        threadRewind = new FastLocateThread(FastLocateThread.REWIND);
                        threadRewind.start();
                    }
                    break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        threadRewind.stopRun();
                        if (threadRewind.isLocating()) {
                            handled = true;
                            v.setPressed(false);
                        }
                    }
                    break;
                }
            }
            break;

            case R.id.ibtn_forward: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        threadForward = new FastLocateThread(FastLocateThread.FORWARD);
                        threadForward.start();
                    }
                    break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        threadForward.stopRun();
                        if (threadForward.isLocating()) {
                            handled = true;
                            v.setPressed(false);
                        }
                    }
                    break;
                }
            }
            break;
        }
        return handled;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        updateProgressThread.end();
        player.removePlayStateChangeListener(this);
        player.removeBufferListener(this);
        player.removeOnLoadedPictureListener(this);
        if (showQueueMusicWindow != null && showQueueMusicWindow.isShowing()) {
            showQueueMusicWindow.dismiss();
        }
    }


    private void animBackgroundColor(int oldColor1, int newColor1, int oldColor2, int newColor2) {
        if (colorAnim1 != null
                && colorAnim1.isRunning()) {
            colorAnim1.cancel();
        }
        if (colorAnim2 != null
                && colorAnim2.isRunning()) {
            colorAnim2.cancel();
        }
        colorAnim1 = ObjectAnimator.ofObject(ll1,
                "backgroundColor",
                new ArgbEvaluator(),
                oldColor1,
                newColor1);
        colorAnim2 = ObjectAnimator.ofObject(ll2,
                "backgroundColor",
                new ArgbEvaluator(),
                oldColor2,
                newColor2);
        colorAnim1.setDuration(1000);
        colorAnim2.setDuration(1000);
        colorAnim1.start();
        colorAnim2.start();
    }

    private void setTitleText(String title) {
        stvTitle.setText(title);
        tvTitle.setText(title);
    }

    private void setArtistText(String artist) {
        if (artist == null
                || artist.equals("<unknown>")) {
            artist = "未知歌手";
        }
        stvArtist.setText(artist);
        tvArtist.setText(artist);
    }

    private void setPlayProgress(int position) {
        position = Math.max(0, position);
        playProgress.setProgress(position);
        splayProgress.setProgress(position);
        tvPlayPostion.setText(formatTime(position));
    }

    private void setSongDuration(int duration) {
        playProgress.setMax(duration);
        splayProgress.setMax(duration);
        tvDuration.setText("/" + formatTime(duration));
    }

    private String formatTime(int millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        return sdf.format(new Date(millis));
    }


    private void setModeIcon(int mode) {
        int iconId = 0;
        switch (mode) {
            case IPlayer.MODE_IN_TURN:
                iconId = R.mipmap.ic_repeat_white_24dp;
                break;
            case IPlayer.MODE_LOOP:
                iconId = R.mipmap.ic_repeat_one_white_24dp;
                break;
            case IPlayer.MODE_RANDOM:
                iconId = R.mipmap.ic_shuffle_white_24dp;
                break;
        }
        ibtnChangeMode.setImageResource(iconId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ibtn_play_mode: {
                int newMode = (player.getPlayMode() + 1) % 3;
                player.setPlayMode(newMode);
                String msg = "";
                setModeIcon(newMode);
                switch (newMode) {
                    case IPlayer.MODE_IN_TURN:
                        msg = "顺序播放";
                        break;
                    case IPlayer.MODE_LOOP:
                        msg = "单曲循环";
                        break;
                    case IPlayer.MODE_RANDOM:
                        msg = "随机播放";
                        break;
                }
                if (modeToast != null) {
                    modeToast.cancel();
                }
                modeToast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
                modeToast.setText(msg);
                modeToast.show();
            }
            break;

            case R.id.ibtn_rewind: {
                player.previous();
            }
            break;

            case R.id.s_ibtn_play_pause:
            case R.id.ibtn_play_pause: {
                if (player.getState() == IPlayer.STATE_STOP) {
                    if (player.getQueue().size() > 0) {
                        player.play(0);
                    } else {
                        T.show(getContext(), "播放列表为空");
                    }
                    return;
                }
                player.togglePlayPause();
            }
            break;

            case R.id.s_ibtn_next:
            case R.id.ibtn_forward: {
                player.next();
            }
            break;

            case R.id.ibtn_play_quque: {
                showQueueMusicWindow = new ShowQueueMusicWindow(getActivity(), player);
                showQueueMusicWindow.show();
            }
            break;

            case R.id.view_back: {
                Intent intent = new Intent(ACTION_FRAG_EVENT);
                intent.putExtra(EXTRA_FROM, getClass().getName());
                intent.putExtra(EXTRA_EVENT, EVENT_CLICK_BACK);
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
        outState.putInt("smallPlayerVisibility", smallPlayer.getVisibility());
        outState.putInt("bigPlayerVisibility", bigPlayer.getVisibility());
    }

    private class UpdateProgressThread extends Thread {

        private int period = 100;
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

                final int position = player.getProgress();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setPlayProgress(position);
                    }
                });
                SystemClock.sleep(period);
            }
        }
    }

    public class FastLocateThread extends Thread {

        public static final int MIN_TIME = 1000;
        private volatile boolean running = true;
        private boolean isLocating = false;
        private int speed;
        public static final int REWIND = 1;//快退
        public static final int FORWARD = 2;//快进

        public FastLocateThread(int type) {
            if (type == REWIND) {
                speed = -4000;
            } else if (type == FORWARD) {
                speed = 4000;
            }
        }

        public void stopRun() {
            running = false;
        }

        public boolean isLocating() {
            return isLocating;
        }

        @Override
        public void run() {
            SystemClock.sleep(MIN_TIME);
            isLocating = true;
            if (!running) {
                return;
            }
            updateProgressThread.pause();
            while (running) {
                int pos = player.getProgress() + speed;
                pos = Math.min(playProgress.getSecondaryProgress(), Math.max(0, pos));
                final int postPos = pos;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPlayProgress(postPos);
                    }
                });
                if (speed < 0) {
                    speed -= 1000;
                } else {
                    speed += 1000;
                }
                player.seekTo(pos);
                SystemClock.sleep(500);
            }
            updateProgressThread.running();
        }
    }

}
