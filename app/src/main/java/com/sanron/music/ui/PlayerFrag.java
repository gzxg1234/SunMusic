package com.sanron.music.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.Gravity;
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
import com.sanron.music.activities.MainActivity;
import com.sanron.music.common.T;
import com.sanron.music.common.ViewTool;
import com.sanron.music.db.bean.Music;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.view.ShowPlayQueueWindow;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 播放界面
 * Created by Administrator on 2016/3/5.
 */
public class PlayerFrag extends BaseFragment implements View.OnClickListener, View.OnTouchListener, IPlayer.OnPlayStateChangeListener, IPlayer.OnBufferListener, IPlayer.OnLoadedPictureListener {

    private ViewGroup mSmallPlayer;
    private ProgressBar mSplayProgress;
    private ImageView mSivSongPicture;
    private TextView mStvTitle;
    private TextView mStvArtist;
    private ImageButton mSibtnPlayPause;
    private ImageButton mSibtnNext;

    private ViewGroup mBigPlayer;
    private ViewGroup mPlayerTopBar;
    private LinearLayout mLl1;
    private ObjectAnimator mColorAnim1;
    private LinearLayout mLl2;
    private ObjectAnimator mColorAnim2;
    private ProgressBar mPlayProgress;
    private ImageView mIvSongPicture;
    private TextView mTvTitle;
    private TextView mTvArtist;
    private TextView mTvPlayPosition;
    private TextView mTvDuration;
    private ImageButton mIbtnBack;
    private ImageButton mIbtnChangeMode;
    private ImageButton mIbtnRewind;
    private ImageButton mIbtnPlayPause;
    private ImageButton mIbtnForward;
    private ImageButton mIbtnPlayQuque;
    private ShowPlayQueueWindow mShowPlayQueueWindow;

    /**
     * 提示播放模式
     */
    private Toast mModeToast;
    private FastLocateThread mThreadRewind;
    private FastLocateThread mThreadForward;


    //刷新播放进度进程
    private UpdateProgressThread mUpdateProgressThread;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_player, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mSmallPlayer = $(R.id.small_player);
        mSplayProgress = $(R.id.s_play_progress);
        mSivSongPicture = $(R.id.s_iv_song_pic);
        mStvTitle = $(R.id.s_tv_title);
        mStvArtist = $(R.id.s_tv_artist);
        mSibtnPlayPause = $(R.id.s_ibtn_play_pause);
        mSibtnNext = $(R.id.s_ibtn_next);
        mLl1 = $(R.id.ll_1);
        mLl2 = $(R.id.ll_2);
        mBigPlayer = $(R.id.big_player);
        mPlayerTopBar = $(R.id.top_bar);
        mIvSongPicture = $(R.id.iv_music_picture);
        mTvTitle = $(R.id.tv_queue_item_title);
        mTvArtist = $(R.id.tv_text2);
        mTvDuration = $(R.id.tv_music_duration);
        mTvPlayPosition = $(R.id.tv_music_progress);
        mIbtnBack = $(R.id.view_back);
        mIbtnChangeMode = $(R.id.ibtn_play_mode);
        mIbtnRewind = $(R.id.ibtn_rewind);
        mIbtnPlayPause = $(R.id.ibtn_play_pause);
        mIbtnForward = $(R.id.ibtn_forward);
        mIbtnPlayQuque = $(R.id.ibtn_play_quque);
        mPlayProgress = $(R.id.progress_play);

        mSibtnPlayPause.setOnClickListener(this);
        mSibtnNext.setOnClickListener(this);
        mIbtnChangeMode.setOnClickListener(this);
        mIbtnRewind.setOnClickListener(this);
        mIbtnPlayPause.setOnClickListener(this);
        mIbtnForward.setOnClickListener(this);
        mIbtnPlayQuque.setOnClickListener(this);
        mIbtnBack.setOnClickListener(this);

        mUpdateProgressThread = new UpdateProgressThread();
        ViewTool.setViewFitsStatusBar(mPlayerTopBar);

        if (savedInstanceState != null) {
            mSmallPlayer.setVisibility(savedInstanceState.getInt("smallPlayerVisibility", View.VISIBLE));
            mBigPlayer.setVisibility(savedInstanceState.getInt("bigPlayerVisibility", View.VISIBLE));
        }

        getMainActivity().addPlayerReadyCallback(this);
    }

    @Override
    public void onPlayerReady() {
        setUpPlayState();
    }

    /**
     * 设置监听和设置界面状态
     */
    private void setUpPlayState() {
        PlayerUtil.addPlayStateChangeListener(this);
        PlayerUtil.addOnBufferListener(this);
        PlayerUtil.addOnLoadedPictureListener(this);

        mUpdateProgressThread.start();
        Music music = PlayerUtil.getCurrentMusic();
        int state = PlayerUtil.getState();
        if (music != null) {
            setTitleText(music.getTitle());
            setArtistText(music.getArtist());
        }

        if (state >= IPlayer.STATE_PREPARED) {
            setSongDuration(PlayerUtil.getDuration());
        }

        if (state == IPlayer.STATE_PLAYING) {
            mSibtnPlayPause.setImageResource(R.mipmap.ic_pause_black_36dp);
            mIbtnPlayPause.setImageResource(R.mipmap.ic_pause_white_24dp);
        } else {
            mUpdateProgressThread.pause();
        }

        setModeIcon(PlayerUtil.getPlayMode());
        onLoadedPicture(PlayerUtil.getCurMusicPic());
        mIbtnRewind.setOnTouchListener(this);
        mIbtnForward.setOnTouchListener(this);
    }

    @Override
    public void onPlayStateChange(int state) {
        switch (state) {
            case IPlayer.STATE_STOP: {
                mUpdateProgressThread.pause();
                setTitleText(getContext().getString(R.string.app_name));
                setArtistText("");
                setSongDuration(0);
                setPlayProgress(0);
                mSibtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
                mIbtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
            }
            break;

            case IPlayer.STATE_PAUSE: {
                mUpdateProgressThread.pause();
                mSibtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
                mIbtnPlayPause.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
            }
            break;

            case IPlayer.STATE_PLAYING: {
                mUpdateProgressThread.running();
                mSibtnPlayPause.setImageResource(R.mipmap.ic_pause_black_36dp);
                mIbtnPlayPause.setImageResource(R.mipmap.ic_pause_white_24dp);
            }
            break;

            case IPlayer.STATE_PREPARING: {
                Music music = PlayerUtil.getCurrentMusic();
                setTitleText(music.getTitle());
                setArtistText(music.getArtist());
                setSongDuration(0);
                setPlayProgress(0);
                mPlayProgress.setSecondaryProgress(0);
            }
            break;

            case IPlayer.STATE_PREPARED: {
                Music music = PlayerUtil.getCurrentMusic();
                setSongDuration(PlayerUtil.getDuration());
                setPlayProgress(PlayerUtil.getProgress());
                if (TextUtils.isEmpty(music.getData())) {
                    mPlayProgress.setSecondaryProgress(0);
                } else {
                    mPlayProgress.setSecondaryProgress(PlayerUtil.getDuration());
                }
            }
            break;
        }
    }

    @Override
    public void onBufferingUpdate(int bufferedPosition) {

        if (bufferedPosition > mPlayProgress.getSecondaryProgress()) {
            mPlayProgress.setSecondaryProgress(bufferedPosition);
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
            mSivSongPicture.setImageResource(R.mipmap.default_song_pic);
            mIvSongPicture.setImageResource(R.mipmap.default_song_pic);
        } else {
            mSivSongPicture.setImageBitmap(img);
            mIvSongPicture.setImageBitmap(img);
        }
        Bitmap bmp = ((BitmapDrawable) mIvSongPicture.getDrawable()).getBitmap();
        Palette.generateAsync(bmp, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int oldColor1 = ((ColorDrawable) mLl1.getBackground()).getColor();
                int oldColor2 = ((ColorDrawable) mLl2.getBackground()).getColor();
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
                        mThreadRewind = new FastLocateThread(FastLocateThread.REWIND);
                        mThreadRewind.start();
                    }
                    break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        mThreadRewind.stopRun();
                        if (mThreadRewind.isLocating()) {
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
                        mThreadForward = new FastLocateThread(FastLocateThread.FORWARD);
                        mThreadForward.start();
                    }
                    break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        mThreadForward.stopRun();
                        if (mThreadForward.isLocating()) {
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
        getMainActivity().removePlayerReadyCallback(this);
        mUpdateProgressThread.running();
        mUpdateProgressThread.end();
        PlayerUtil.removePlayStateChangeListener(this);
        PlayerUtil.removeBufferListener(this);
        PlayerUtil.removeOnLoadedPictureListener(this);
        if (mShowPlayQueueWindow != null && mShowPlayQueueWindow.isShowing()) {
            mShowPlayQueueWindow.dismiss();
        }
    }


    private void animBackgroundColor(int oldColor1, int newColor1, int oldColor2, int newColor2) {
        if (mColorAnim1 != null
                && mColorAnim1.isRunning()) {
            mColorAnim1.cancel();
        }
        if (mColorAnim2 != null
                && mColorAnim2.isRunning()) {
            mColorAnim2.cancel();
        }
        mColorAnim1 = ObjectAnimator.ofObject(mLl1,
                "backgroundColor",
                new ArgbEvaluator(),
                oldColor1,
                newColor1);
        mColorAnim2 = ObjectAnimator.ofObject(mLl2,
                "backgroundColor",
                new ArgbEvaluator(),
                oldColor2,
                newColor2);
        mColorAnim1.setDuration(1000);
        mColorAnim2.setDuration(1000);
        mColorAnim1.start();
        mColorAnim2.start();
    }

    private void setTitleText(String title) {
        mStvTitle.setText(title);
        mTvTitle.setText(title);
    }

    private void setArtistText(String artist) {
        if (artist == null
                || artist.equals("<unknown>")) {
            artist = "未知歌手";
        }
        mStvArtist.setText(artist);
        mTvArtist.setText(artist);
    }

    private void setPlayProgress(int position) {
        position = Math.max(0, position);
        mPlayProgress.setProgress(position);
        mSplayProgress.setProgress(position);
        mTvPlayPosition.setText(formatTime(position));
    }

    private void setSongDuration(int duration) {
        mPlayProgress.setMax(duration);
        mSplayProgress.setMax(duration);
        mTvDuration.setText("/" + formatTime(duration));
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
        mIbtnChangeMode.setImageResource(iconId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ibtn_play_mode: {
                int newMode = (PlayerUtil.getPlayMode() + 1) % 3;
                PlayerUtil.setPlayMode(newMode);
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
                if (mModeToast != null) {
                    mModeToast.cancel();
                }
                mModeToast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
                mModeToast.setText(msg);
                mModeToast.show();
            }
            break;

            case R.id.ibtn_rewind: {
                PlayerUtil.previous();
            }
            break;

            case R.id.s_ibtn_play_pause:
            case R.id.ibtn_play_pause: {
                if (PlayerUtil.getState() == IPlayer.STATE_STOP) {
                    if (PlayerUtil.getQueue().size() > 0) {
                        PlayerUtil.play(0);
                    } else {
                        T.show("播放列表为空");
                    }
                    return;
                }
                PlayerUtil.togglePlayPause();
            }
            break;

            case R.id.s_ibtn_next:
            case R.id.ibtn_forward: {
                PlayerUtil.next();
            }
            break;

            case R.id.ibtn_play_quque: {
                mShowPlayQueueWindow = new ShowPlayQueueWindow(getActivity());
                mShowPlayQueueWindow.showAtLocation(getView(), Gravity.BOTTOM, 0, 0);
            }
            break;

            case R.id.view_back: {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).collapseSlidingPanel();
                }
            }
            break;
        }
    }

    //主界面底部的小控制面板可见性
    public void setSmallControllerVisibility(int visibility) {
        mSmallPlayer.setVisibility(visibility);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("smallPlayerVisibility", mSmallPlayer.getVisibility());
        outState.putInt("bigPlayerVisibility", mBigPlayer.getVisibility());
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

                final int position = PlayerUtil.getProgress();
                mPlayProgress.post(new Runnable() {
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
            mUpdateProgressThread.pause();
            while (running) {
                int pos = PlayerUtil.getProgress() + speed;
                pos = Math.min(mPlayProgress.getSecondaryProgress(), Math.max(0, pos));
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
                PlayerUtil.seekTo(pos);
                SystemClock.sleep(500);
            }
            mUpdateProgressThread.running();
        }
    }

}
