package com.sanron.music.playback;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.SongUrlInfo;
import com.sanron.music.common.MyLog;
import com.sanron.music.common.ViewTool;
import com.sanron.music.db.bean.Music;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;

public class DDPlayer extends Binder implements Player, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener, AudioManager.OnAudioFocusChangeListener {


    public static final String TAG = DDPlayer.class.getSimpleName();

    private Context mContext;
    private List<Music> mQueue;
    private int mMode = MODE_IN_TURN;
    private int mCurrentPosition;
    private int mState = STATE_STOP;
    private boolean mPlayWhenReady = true;
    private boolean mLossFocusWhenPlaying;
    private Call mFileLinkCall;
    private List<OnCompletedListener> mOnCompletedListeners;
    private List<OnBufferListener> mOnBufferListeners;
    private List<OnPlayStateChangeListener> mOnPlayStateChangeListeners;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private PowerManager.WakeLock mWakeLock;

    private int mAudioFocus = NO_FOCUS;

    public static final float DUCK_VOLUME = 0.1f;

    public static final int HAS_FOCUS = 1;//有焦点
    public static final int NO_FOCUS = 2;//没焦点
    public static final int NO_FOCUS_CAN_DUCK = 3;//没焦点但是可以降低声音

    public static final int WHAT_PLAY_ERROR = 1;
    public static final int WHAT_BUFFER_TIMEOUT = 2;
    public static final int BUFFER_TIMEOUT = 30 * 1000;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_PLAY_ERROR: {
                    next();
                }
                break;

                case WHAT_BUFFER_TIMEOUT: {
                    next();
                    ViewTool.show("缓冲超时,自动播放下一曲");
                }
                break;
            }
        }
    };

    public DDPlayer(Context context) {
        mContext = context;
        mOnBufferListeners = new ArrayList<>();
        mOnCompletedListeners = new ArrayList<>();
        mOnPlayStateChangeListeners = new ArrayList<>();
        mQueue = new ArrayList<>();
        mCurrentPosition = -1;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DDMusic");
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mWakeLock.acquire();
    }

    @Override
    public List<Music> getQueue() {
        return new ArrayList<>(mQueue);
    }

    /**
     * 加入播放队列
     */
    @Override
    public void enqueue(List<Music> musics) {
        mQueue.addAll(musics);
        Log.d(TAG, "enqueue " + musics.size() + " songs");
    }

    /**
     * 移出队列
     */
    public void dequeue(int position) {
        mQueue.remove(position);
        if (mQueue.size() == 0) {
            //移除后，队列空了
            clearQueue();
        } else if (position < mCurrentPosition) {
            //更正currentindex
            mCurrentPosition--;
        } else if (position == mCurrentPosition) {
            //当移除的歌曲正在播放时
            if (mCurrentPosition == mQueue.size()) {
                //刚好播放最后一首歌，又需要移除他,将播放第一首歌曲
                mCurrentPosition = 0;
            }
            play(mCurrentPosition);
        }
    }

    @Override
    public void clearQueue() {
        if (mMediaPlayer == null) {
            return;
        }

        mQueue.clear();
        mCurrentPosition = -1;
        mMediaPlayer.reset();
        changeState(STATE_STOP);
    }

    /**
     * 播放队列position位置歌曲
     */
    @Override
    public void play(int position) {
        getAudioFocus();
        if (mMediaPlayer == null) {
            initMediaPlayer();
        }
        if (mFileLinkCall != null) {
            mFileLinkCall.cancel();
        }
        mHandler.removeMessages(WHAT_PLAY_ERROR);
        sendBufferingEnd();

        mCurrentPosition = position;
        Music music = mQueue.get(mCurrentPosition);
        changeState(STATE_PREPARING);
        String dataPath = music.getData();
        if (TextUtils.isEmpty(dataPath)) {
            //网络歌曲
            sendBufferingStart();
            playWebMusic(music.getSongId());
        } else {
            //检查文件是否存在
            File file = new File(dataPath);
            if (file.exists()) {
                prepare(Uri.parse(dataPath));
            } else {
                sendPlayError("本地歌曲文件不存在");
            }
        }
    }

    private void prepare(Uri uri) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mContext, uri);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            MyLog.w(TAG, "IllegalState");
        } catch (IOException e) {
            e.printStackTrace();
            sendPlayError("播放出错");
        }
    }

    private void sendPlayError(final String errorMsg) {
        changeState(STATE_ERROR);
        ViewTool.show(errorMsg + ",3s后播放下一曲");
        mHandler.sendEmptyMessageDelayed(WHAT_PLAY_ERROR, 3000);
    }

    private void playWebMusic(final String songid) {
        final String title = getCurrentMusic().getTitle();
        mFileLinkCall = MusicApi.songLink(songid, new JsonCallback<SongUrlInfo>() {

            @Override
            public void onFailure(Exception e) {
                sendPlayError("网络请求失败");
            }

            @Override
            public void onSuccess(SongUrlInfo data) {
                SongUrlInfo.SongUrl.Url url = PlayerHelper.selectFileUrl(mContext, data);
                if (url == null) {
                    sendPlayError("此歌曲暂无网络资源");
                } else {
                    System.out.println("----:" + title);
                    System.out.println("----:" + url.fileLink);
                    prepare(Uri.parse(url.fileLink));
                }
            }
        });
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    @Override
    public Music getCurrentMusic() {
        if (mCurrentPosition == -1) {
            return null;
        }
        return mQueue.get(mCurrentPosition);
    }

    private void changeState(int newState) {
        mState = newState;
        for (OnPlayStateChangeListener onPlayStateChangeListener : mOnPlayStateChangeListeners) {
            onPlayStateChangeListener.onPlayStateChange(mState);
        }
    }

    public void resume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
        changeState(STATE_PLAYING);
    }


    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
        changeState(STATE_PAUSE);
    }

    @Override
    public void togglePlayPause() {
        if (mState == STATE_PAUSE) {
            resume();
        } else if (mState == STATE_PLAYING) {
            pause();
        }
    }


    @Override
    public void next() {
        if (mQueue.size() == 0) {
            return;
        }
        play((mCurrentPosition + 1) % mQueue.size());
    }

    @Override
    public void previous() {
        if (mQueue.size() == 0) {
            return;
        }
        int lastIndex = mCurrentPosition;
        if (mCurrentPosition > 0) {
            lastIndex = mCurrentPosition - 1;
        }
        play(lastIndex);
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public void setPlayMode(int mode) {
        if (this.mMode != mode) {
            this.mMode = mode;
        }
    }

    @Override
    public int getPlayMode() {
        return mMode;
    }

    @Override
    public void addPlayStateChangeListener(OnPlayStateChangeListener onPlayStateChangeListener) {
        mOnPlayStateChangeListeners.add(onPlayStateChangeListener);
    }

    @Override
    public void removePlayStateChangeListener(OnPlayStateChangeListener onPlayStateChangeListener) {
        mOnPlayStateChangeListeners.remove(onPlayStateChangeListener);
    }

    @Override
    public void addOnBufferListener(OnBufferListener onBufferListener) {
        mOnBufferListeners.add(onBufferListener);
    }

    @Override
    public void removeBufferListener(OnBufferListener onBufferListener) {
        mOnBufferListeners.remove(onBufferListener);
    }

    @Override
    public void addOnCompletedListener(OnCompletedListener onCompletedListener) {
        mOnCompletedListeners.add(onCompletedListener);
    }

    @Override
    public void removeOnCompletedListener(OnCompletedListener onCompletedListener) {
        mOnCompletedListeners.remove(onCompletedListener);
    }


    @Override
    public int getProgress() {
        if (isPrepared()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public int getDuration() {
        if (isPrepared()) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public boolean isPrepared() {
        return mMediaPlayer != null
                && (mState == STATE_PLAYING || mState == STATE_PAUSE || mState == STATE_PREPARED);
    }

    @Override
    public void seekTo(int msec) {
        if (mMediaPlayer != null && isPrepared()) {
            mMediaPlayer.seekTo(msec);
        }
    }

    private void sendOnCompleted() {
        for (OnCompletedListener listener : mOnCompletedListeners) {
            listener.onCompleted();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        sendOnCompleted();
        switch (mMode) {
            case MODE_IN_TURN: {
                next();
            }
            break;

            case MODE_LOOP: {
                mMediaPlayer.start();
            }
            break;

            case MODE_RANDOM: {
                play(new Random().nextInt(mQueue.size()));
            }
            break;
        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        MyLog.e(TAG, "error (" + what + "," + extra + ")");
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED: {
                //media服务失效，初始化meidaplayer
                MyLog.e(TAG, "MediaPlayer died");
                release();
            }
        }

        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
            case MediaPlayer.MEDIA_ERROR_MALFORMED: {
                MyLog.e(TAG, "不支持的音乐文件");
                sendPlayError("无法播放此音乐");
            }
            break;

            case MediaPlayer.MEDIA_ERROR_IO: {
                MyLog.e(TAG, "读写文件错误");
                sendPlayError("打开歌曲文件出错");
            }
            break;

            case MediaPlayer.MEDIA_ERROR_TIMED_OUT: {
                MyLog.w(TAG, "Some operation takes too long to complete");
            }
            break;
        }
        return true;
    }

    public boolean isPlayWhenReady() {
        return mPlayWhenReady;
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        mPlayWhenReady = playWhenReady;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        changeState(STATE_PREPARED);
        if (mPlayWhenReady && mState != STATE_PAUSE && mAudioFocus != NO_FOCUS) {
            mp.start();
            changeState(STATE_PLAYING);
        }
        sendBufferingEnd();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //percent是已缓冲的时间减去已播放的时间 占  未播放的时间 的百分百
        //比如歌曲时长300s,已播放20s,已缓冲50s,则percent=(50-20)/(300-50);
        if (isPrepared()) {
            int duration = mp.getDuration();
            int currentPosition = mp.getCurrentPosition();
            int remain = duration - currentPosition;
            int buffedPosition = currentPosition + (int) Math.floor(remain * percent / 100f);

            for (OnBufferListener listener : mOnBufferListeners) {
                listener.onBufferingUpdate(buffedPosition);
            }
        }
    }

    private void sendBufferingStart() {
        //缓冲开始，发送一个延时消息，超时则到下一曲
        mHandler.sendEmptyMessageDelayed(WHAT_BUFFER_TIMEOUT, BUFFER_TIMEOUT);
        for (OnBufferListener listener : mOnBufferListeners) {
            listener.onBufferStart();
        }
    }

    private void sendBufferingEnd() {
        if (mHandler.hasMessages(WHAT_BUFFER_TIMEOUT)) {
            mHandler.removeMessages(WHAT_BUFFER_TIMEOUT);
            for (OnBufferListener listener : mOnBufferListeners) {
                listener.onBufferEnd();
            }
        }
    }

    public void release() {
        changeState(STATE_STOP);
        abandonAudioFocus();
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                sendBufferingStart();
            }
            break;

            case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                sendBufferingEnd();
            }
            break;
        }
        return true;
    }

    private void getAudioFocus() {
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocus = HAS_FOCUS;
        }
    }

    private void abandonAudioFocus() {
        int result = mAudioManager.abandonAudioFocus(this);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocus = NO_FOCUS;
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            //暂时失去音频焦点，比如电话

            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                if (mState == Player.STATE_PLAYING) {
                    mLossFocusWhenPlaying = true;
                    pause();
                }
                mAudioFocus = NO_FOCUS;
            }
            break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
                }
                mAudioFocus = NO_FOCUS_CAN_DUCK;
            }
            break;

            case AudioManager.AUDIOFOCUS_GAIN: {
                if (mState == Player.STATE_PAUSE
                        && mLossFocusWhenPlaying) {
                    resume();
                    mLossFocusWhenPlaying = false;
                }
                if (mMediaPlayer != null) {
                    mMediaPlayer.setVolume(1f, 1f);
                }
                mAudioFocus = HAS_FOCUS;
            }
            break;
        }
    }
}
