package com.sanron.music.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sanron.music.AppContext;
import com.sanron.music.AppManager;
import com.sanron.music.R;
import com.sanron.music.common.MyLog;
import com.sanron.music.common.T;
import com.sanron.music.db.bean.Music;
import com.sanron.music.net.JsonCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.LrcPicData;
import com.sanron.music.net.bean.SongUrlInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import okhttp3.Call;

/**
 * Created by Administrator on 2015/12/16.
 */
public class PlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {

    public static final String TAG = PlayerService.class.getSimpleName();
    public static final int FOREGROUND_ID = 0x88;

    public static final String NOTIFY_ACTION = "com.sanron.music.PLAYBACK";
    public static final String EXTRA_CMD = "CMD";
    public static final String CMD_BACK_APP = "back_app";
    public static final String CMD_PREVIOUS = "previous";
    public static final String CMD_PLAY_PAUSE = "play_pause";
    public static final String CMD_NEXT = "next";
    public static final String CMD_LYRIC = "lyric";
    public static final String CMD_CLOSE = "close_app";

    public static final int WHAT_PLAY_ERROR = 1;
    public static final int WHAT_BUFFER_TIMEOUT = 2;


    private PowerManager.WakeLock mWakeLock;

    private NotificationCompat.Builder mNotificationBuilder;


    private AudioManager mAudioManager;

    private Bitmap mCurMusicPic;

    private Player mPlayer;

    private boolean mIsLossAudioFocus;


    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_PLAY_ERROR: {
                    mPlayer.next();
                }
                break;

                case WHAT_BUFFER_TIMEOUT: {
                    mPlayer.next();
                    T.show("缓冲超时,自动播放下一曲");
                }
                break;
            }
        }
    };

    private BroadcastReceiver cmdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra(EXTRA_CMD);
            switch (cmd) {
                case CMD_PREVIOUS: {
                    mPlayer.previous();
                }
                break;

                case CMD_PLAY_PAUSE: {
                    int state = mPlayer.getState();
                    if (state == IPlayer.STATE_PAUSE) {
                        mPlayer.resume();
                    } else if (state == IPlayer.STATE_PLAYING) {
                        mPlayer.pause();
                    } else if (state == IPlayer.STATE_STOP) {
                        if (mPlayer.getQueue().size() > 0) {
                            mPlayer.play(0);
                        }
                    }
                }
                break;

                case CMD_NEXT: {
                    mPlayer.next();
                }
                break;

                case CMD_LYRIC: {

                }
                break;

                case CMD_CLOSE: {
                    ((AppContext) getApplicationContext()).closeApp();
                }
                break;

                case CMD_BACK_APP: {
                    Activity curActivity = AppManager.instance().currentActivity();
                    if (curActivity == null) {
                        Intent in = getPackageManager().getLaunchIntentForPackage(getPackageName());
                        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        startActivity(in);
                    } else {
                        Intent in = new Intent(PlayerService.this, curActivity.getClass());
                        in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        curActivity.startActivity(in);
                    }
                }
                break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.i(TAG, "service create");
        mPlayer = new Player();
        acquireWakeLock();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        IntentFilter intentFilter = new IntentFilter(NOTIFY_ACTION);
        registerReceiver(cmdReceiver, intentFilter);

        mNotificationBuilder = new NotificationCompat.Builder(this);
        mNotificationBuilder.setTicker("");
        mNotificationBuilder.setSmallIcon(R.mipmap.default_song_pic);
        mNotificationBuilder.setPriority(Notification.PRIORITY_MAX);
        updateNotification();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i(TAG, "service destroy");
        stopForeground(true);
        unregisterReceiver(cmdReceiver);
        mAudioManager.abandonAudioFocus(this);
        releaseWakeLock();
        mPlayer.release();
        mPlayer = null;
    }


    /**
     * 申请唤醒锁
     */
    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SunMusic");
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    /**
     * 释放唤醒锁
     */
    private void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    /**
     * 更新通知
     */
    private void updateNotification() {
        RemoteViews bigContentView = new RemoteViews(getPackageName(), R.layout.notification_big_layout);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_small_layout);
        int state = mPlayer.getState();
        switch (state) {
            case IPlayer.STATE_STOP: {
                bigContentView.setImageViewResource(R.id.iv_album_pic, R.mipmap.default_song_pic);
                bigContentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_24dp);
                contentView.setImageViewResource(R.id.iv_album_pic, R.mipmap.default_song_pic);
                contentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_36dp);
            }
            break;
            case IPlayer.STATE_PAUSE: {
                bigContentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_24dp);
                contentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_36dp);
            }
            break;
            case IPlayer.STATE_PLAYING: {
                bigContentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_pause_black_24dp);
                contentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_pause_black_36dp);
            }
            break;
        }

        if (mPlayer.getCurrentIndex() != -1) {
            Music music = mPlayer.getCurrentMusic();
            String artist = music.getArtist();
            artist = artist.equals("<unknown>") ? "未知歌手" : artist;
            String musicInfo = music.getTitle() + "-" + artist;

            bigContentView.setTextViewText(R.id.tv_music_info, musicInfo);
            contentView.setTextViewText(R.id.tv_queue_item_title, music.getTitle());
            contentView.setTextViewText(R.id.tv_text2, artist);

            if (mCurMusicPic != null) {
                bigContentView.setImageViewBitmap(R.id.iv_album_pic, mCurMusicPic);
                contentView.setImageViewBitmap(R.id.iv_album_pic, mCurMusicPic);
            } else {
                bigContentView.setImageViewResource(R.id.iv_album_pic, R.mipmap.default_song_pic);
                contentView.setImageViewResource(R.id.iv_album_pic, R.mipmap.default_song_pic);
            }

        } else {
            bigContentView.setTextViewText(R.id.tv_music_info, getText(R.string.app_name));
            contentView.setTextViewText(R.id.tv_queue_item_title, getText(R.string.app_name));
            contentView.setTextViewText(R.id.tv_text2, "");
        }

        bigContentView.setOnClickPendingIntent(R.id.ibtn_lrc, cmdIntent(CMD_LYRIC));
        bigContentView.setOnClickPendingIntent(R.id.ibtn_rewind, cmdIntent(CMD_PREVIOUS));
        bigContentView.setOnClickPendingIntent(R.id.ibtn_play_pause, cmdIntent(CMD_PLAY_PAUSE));
        bigContentView.setOnClickPendingIntent(R.id.ibtn_forward, cmdIntent(CMD_NEXT));
        bigContentView.setOnClickPendingIntent(R.id.ibtn_close, cmdIntent(CMD_CLOSE));

        contentView.setOnClickPendingIntent(R.id.ibtn_lrc, cmdIntent(CMD_LYRIC));
        contentView.setOnClickPendingIntent(R.id.ibtn_play_pause, cmdIntent(CMD_PLAY_PAUSE));
        contentView.setOnClickPendingIntent(R.id.ibtn_forward, cmdIntent(CMD_NEXT));
        contentView.setOnClickPendingIntent(R.id.ibtn_close, cmdIntent(CMD_CLOSE));

        Notification notification = mNotificationBuilder.build();
        notification.contentIntent = cmdIntent(CMD_BACK_APP);
        notification.bigContentView = bigContentView;
        notification.contentView = contentView;
        startForeground(FOREGROUND_ID, notification);
    }

    private PendingIntent cmdIntent(String cmd) {
        Intent intent = new Intent(NOTIFY_ACTION);
        intent.putExtra(EXTRA_CMD, cmd);
        return PendingIntent.getBroadcast(this, cmd.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyLog.d(TAG, "service onBind");
        return mPlayer;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MyLog.d(TAG, "service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            //暂时失去音频焦点，比如电话

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                if (mPlayer.getState() == IPlayer.STATE_PLAYING) {
                    mPlayer.pause();
                    mIsLossAudioFocus = true;
                }
            }
            break;

            case AudioManager.AUDIOFOCUS_GAIN: {
                if (mPlayer.getState() == IPlayer.STATE_PAUSE
                        && mIsLossAudioFocus) {
                    mPlayer.resume();
                    mIsLossAudioFocus = false;
                }
            }
            break;
        }
    }

    public class Player extends Binder implements IPlayer, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener {

        /**
         * 播放队列
         */
        private List<Music> mQueue;
        /**
         * 播放模式
         */
        private int mMode = MODE_IN_TURN;
        /**
         * 当前播放歌曲位置
         */
        private int mCurrentIndex;
        /**
         * 播放器状态
         */
        private int mState = STATE_STOP;
        /**
         * 音乐文件地址请求
         */
        private Call mFileLinkCall;
        private Call mSearchPicCall;
        private Set<OnLoadedPictureListener> mOnLoadedPictureListeners;
        private Set<OnBufferListener> mOnBufferListeners;
        private Set<OnPlayStateChangeListener> mOnPlayStateChangeListeners;
        private MediaPlayer mMediaPlayer;

        /**
         * 超时时间
         */
        public static final int BUFFER_TIMEOUT = 30 * 1000;

        public Player() {
            mOnBufferListeners = new HashSet<>();
            mOnPlayStateChangeListeners = new HashSet<>();
            mOnLoadedPictureListeners = new HashSet<>();
            mQueue = new ArrayList<>();
            mCurrentIndex = -1;
        }

        private void initMediaPlayer() {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
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
            } else if (position < mCurrentIndex) {
                //更正currentindex
                mCurrentIndex--;
            } else if (position == mCurrentIndex) {
                //当移除的歌曲正在播放时
                if (mCurrentIndex == mQueue.size()) {
                    //刚好播放最后一首歌，又需要移除他,将播放第一首歌曲
                    mCurrentIndex = 0;
                }
                play(mCurrentIndex);
            }
        }

        @Override
        public void clearQueue() {
            if (mMediaPlayer == null) {
                return;
            }

            mQueue.clear();
            mCurrentIndex = -1;
            mMediaPlayer.reset();
            changeState(STATE_STOP);
            setCurMusicPic(null);
            updateNotification();
        }

        /**
         * 播放队列position位置歌曲
         */
        @Override
        public void play(int position) {
            handler.removeMessages(WHAT_PLAY_ERROR);
            handler.removeMessages(WHAT_BUFFER_TIMEOUT);
            if (mFileLinkCall != null) {
                mFileLinkCall.cancel();
            }
            if (mSearchPicCall != null) {
                mSearchPicCall.cancel();
            }
            if (mMediaPlayer == null) {
                initMediaPlayer();
            }
            mMediaPlayer.reset();

            mCurrentIndex = position;
            Music music = mQueue.get(mCurrentIndex);
            changeState(STATE_PREPARING);
            updateNotification();
            String dataPath = music.getData();
            if (TextUtils.isEmpty(dataPath)) {
                //网络歌曲
                playWebMusic(music.getSongId());
            } else {
                //检查文件是否存在
                File file = new File(dataPath);
                if (file.exists()) {
                    prepare(Uri.parse(dataPath));
                } else {
                    handlerPlayError("本地歌曲文件不存在");
                }
            }
        }

        private void prepare(Uri uri) {
            try {
                mMediaPlayer.setDataSource(PlayerService.this, uri);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepareAsync();
            } catch (IllegalStateException e) {
                MyLog.w(TAG, "IllegalState");
            } catch (IOException e) {
                e.printStackTrace();
                handlerPlayError("播放出错");
            }
        }


        private void handlerPlayError(final String errorMsg) {
            mState = STATE_STOP;
            T.show(errorMsg + ",3s后播放下一曲");
            handler.sendEmptyMessageDelayed(WHAT_PLAY_ERROR, 3000);
        }


        private void playWebMusic(final String songid) {
            mFileLinkCall = MusicApi.songLink(songid, new JsonCallback<SongUrlInfo>() {
                @Override
                public void onFailure(Exception e) {
                    handlerPlayError("网络请求失败");
                }

                @Override
                public void onSuccess(SongUrlInfo data) {
                    SongUrlInfo.SongUrl.Url url = null;
                    if (data != null
                            && data.songUrl != null) {
                        url = PlayerUtil.selectFileUrl(getApplicationContext(), data.songUrl.urls);
                    }
                    if (url == null) {
                        handlerPlayError("此歌曲暂无网络资源");
                    } else {
                        prepare(Uri.parse(url.fileLink));
                    }
                }
            });
        }

        @Override
        public int getCurrentIndex() {
            return mCurrentIndex;
        }

        @Override
        public Music getCurrentMusic() {
            if (mCurrentIndex == -1) {
                return null;
            }
            return mQueue.get(mCurrentIndex);
        }

        @Override
        public Bitmap getCurMusicPic() {
            return mCurMusicPic;
        }

        private void changeState(int newState) {
            mState = newState;
            for (OnPlayStateChangeListener onPlayStateChangeListener : mOnPlayStateChangeListeners) {
                onPlayStateChangeListener.onPlayStateChange(mState);
            }
        }

        void resume() {
            mMediaPlayer.start();
            changeState(STATE_PLAYING);
            updateNotification();
        }

        void pause() {
            mMediaPlayer.pause();
            changeState(STATE_PAUSE);
            updateNotification();
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
            play((mCurrentIndex + 1) % mQueue.size());
        }

        @Override
        public void previous() {
            if (mQueue.size() == 0) {
                return;
            }
            int lastIndex = mCurrentIndex;
            if (mCurrentIndex > 0) {
                lastIndex = mCurrentIndex - 1;
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
        public void addOnLoadedPictureListener(OnLoadedPictureListener onLoadedPictureListener) {
            mOnLoadedPictureListeners.add(onLoadedPictureListener);
        }

        @Override
        public void removeOnLoadedPictureListener(OnLoadedPictureListener onLoadedPictureListener) {
            mOnLoadedPictureListeners.remove(onLoadedPictureListener);
        }

        @Override
        public int getProgress() {
            if (mState >= IPlayer.STATE_PREPARED) {
                return mMediaPlayer.getCurrentPosition();
            }
            return -1;
        }

        @Override
        public int getDuration() {
            if (mState >= IPlayer.STATE_PREPARED) {
                return mMediaPlayer.getDuration();
            }
            return -1;
        }

        @Override
        public void seekTo(int msec) {
            if (mState >= STATE_PREPARED) {
                mMediaPlayer.seekTo(msec);
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
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
                    initMediaPlayer();
                    return true;
                }
            }

            switch (extra) {
                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                case MediaPlayer.MEDIA_ERROR_MALFORMED: {
                    MyLog.e(TAG, "不支持的音乐文件");
                    handlerPlayError("无法播放此音乐");
                }
                break;

                case MediaPlayer.MEDIA_ERROR_IO: {
                    MyLog.e(TAG, "读写文件错误");
                    handlerPlayError("打开歌曲文件出错");
                }
                break;

                case MediaPlayer.MEDIA_ERROR_TIMED_OUT: {
                    MyLog.w(TAG, "Some operation takes too long to complete");
                }
                break;
            }
            return true;
        }

        private void setCurMusicPic(Bitmap img) {
            mCurMusicPic = img;
            for (OnLoadedPictureListener listener : mOnLoadedPictureListeners) {
                listener.onLoadedPicture(img);
            }
            updateNotification();
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            changeState(STATE_PREPARED);
            mp.start();
            changeState(STATE_PLAYING);

            //加载音乐图片
            Music music = mQueue.get(mCurrentIndex);
            String artist = music.getArtist();
            mSearchPicCall = MusicApi.searchLrcPic(music.getTitle(),
                    "<unknown>".equals(artist) ? "" : artist,
                    2,
                    new JsonCallback<LrcPicData>() {
                        final int requestIndex = mCurrentIndex;

                        @Override
                        public void onSuccess(LrcPicData data) {
                            List<LrcPicData.LrcPic> lrcPics = data.lrcPics;
                            String pic = null;
                            if (lrcPics != null) {
                                for (LrcPicData.LrcPic lrcPic : lrcPics) {
                                    pic = lrcPic.pic1000x1000;
                                    if (TextUtils.isEmpty(pic)) {
                                        pic = lrcPic.pic500x500;
                                    }
                                    if (!TextUtils.isEmpty(pic)) {
                                        break;
                                    }
                                }
                            }
                            if (!TextUtils.isEmpty(pic)) {
                                ImageLoader.getInstance()
                                        .loadImage(pic, new SimpleImageLoadingListener() {
                                            @Override
                                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                                if (requestIndex == getCurrentIndex()) {
                                                    setCurMusicPic(loadedImage);
                                                }
                                            }

                                            @Override
                                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                                if (requestIndex == getCurrentIndex()) {
                                                    setCurMusicPic(null);
                                                }
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            setCurMusicPic(null);
                        }
                    });
        }


        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            //percent是已缓冲的时间减去已播放的时间 占  未播放的时间 的百分百
            //比如歌曲时长300s,已播放20s,已缓冲50s,则percent=(50-20)/(300-50);
            if (mState >= STATE_PREPARED) {
                int duration = mp.getDuration();
                int currentPosition = mp.getCurrentPosition();
                int remain = duration - currentPosition;
                int buffedPosition = currentPosition + (int) Math.floor(remain * percent / 100f);

                for (OnBufferListener listener : mOnBufferListeners) {
                    listener.onBufferingUpdate(buffedPosition);
                }
            }
        }

        void release() {
            changeState(STATE_STOP);
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                    handler.sendEmptyMessageDelayed(WHAT_BUFFER_TIMEOUT, BUFFER_TIMEOUT);
                    for (OnBufferListener listener : mOnBufferListeners) {
                        listener.onBufferStart();
                    }
                }
                break;

                case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                    handler.removeMessages(WHAT_BUFFER_TIMEOUT);
                    for (OnBufferListener listener : mOnBufferListeners) {
                        listener.onBufferEnd();
                    }
                }
                break;
            }
            return true;
        }
    }

}
