package com.sanron.music.service;

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
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sanron.music.AppContext;
import com.sanron.music.AppManager;
import com.sanron.music.R;
import com.sanron.music.activities.MainActivity;
import com.sanron.music.utils.MyLog;
import com.sanron.music.utils.TUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2015/12/16.
 */
public class PlayerService extends Service {

    public static final String TAG = PlayerService.class.getSimpleName();
    public static final int FORGROUND_ID = 0x88;

    public static final String CMD_ACTION = "com.sanron.music.PLAYBACK";
    public static final String EXTRA_CMD = "CMD";
    public static final String CMD_PREVIOUS = "previous";
    public static final String CMD_PLAY_PAUSE = "play_pause";
    public static final String CMD_NEXT = "next";
    public static final String CMD_LYRIC = "lyric";
    public static final String CMD_CLOSE = "close_app";

    private AppContext appContext;
    private PowerManager.WakeLock wakeLock;
    private NotificationCompat.Builder builder;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    private Player player;

    private BroadcastReceiver cmdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra(EXTRA_CMD);
            MyLog.d(TAG, "playback cmd : " + cmd);
            switch (cmd) {
                case CMD_PREVIOUS: {
                    player.last();
                }
                break;

                case CMD_PLAY_PAUSE: {
                    int state = player.getState();
                    if (state == IPlayer.STATE_PAUSE) {
                        player.play();
                    } else if (state == IPlayer.STATE_STOP) {
                        if (player.getQueue().size() > 0) {
                            player.play(0);
                        }
                    } else if (state == IPlayer.STATE_PLAYING) {
                        player.pause();
                    }
                }
                break;

                case CMD_NEXT: {
                    player.next();
                }
                break;

                case CMD_LYRIC: {

                }
                break;

                case CMD_CLOSE: {
                    AppManager.instance().finishAllActivity();
                    appContext.tryToStopService();
                }
                break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.i(TAG, "service create");
        acquireWakeLock();
        appContext = (AppContext) getApplicationContext();
        player = new Player();
        IntentFilter intentFilter = new IntentFilter(CMD_ACTION);
        registerReceiver(cmdReceiver, intentFilter);

        builder = new NotificationCompat.Builder(this);
        builder.setTicker("");
        builder.setSmallIcon(R.mipmap.default_song_pic);
        builder.setPriority(Notification.PRIORITY_MAX);
        updateNotification();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i(TAG, "service destroy");
        player.release();
        player = null;
        releaseWakeLock();
        unregisterReceiver(cmdReceiver);
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    /**
     * 申请唤醒锁
     */
    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SunMusic");
        if (wakeLock != null) {
            wakeLock.acquire();
        }
    }

    /**
     * 释放唤醒锁
     */
    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
        }
    }

    private Bitmap curMusicPic;

    /**
     * 更新通知
     */
    private void updateNotification() {
        RemoteViews bigContentView = new RemoteViews(getPackageName(), R.layout.notification_big_layout);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_small_layout);
        int state = player.getState();
        switch (state) {
            case IPlayer.STATE_STOP: {
                bigContentView.setImageViewResource(R.id.iv_picture, R.mipmap.default_song_pic);
                bigContentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_24dp);
                contentView.setImageViewResource(R.id.iv_picture, R.mipmap.default_song_pic);
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

        if (player.getCurrentIndex() != -1) {
            Playable playable = player.getQueue().get(player.getCurrentIndex());
            String musicInfo = playable.title() + "-" + playable.artist();

            bigContentView.setTextViewText(R.id.tv_music_info, musicInfo);
            contentView.setTextViewText(R.id.tv_music_info, musicInfo);

            if (curMusicPic != null) {
                bigContentView.setImageViewBitmap(R.id.iv_picture, curMusicPic);
                contentView.setImageViewBitmap(R.id.iv_picture, curMusicPic);
            } else {
                bigContentView.setImageViewResource(R.id.iv_picture, R.mipmap.default_song_pic);
                contentView.setImageViewResource(R.id.iv_picture, R.mipmap.default_song_pic);
            }

        } else {
            bigContentView.setTextViewText(R.id.tv_music_info, getText(R.string.app_name));
            contentView.setTextViewText(R.id.tv_music_info, getText(R.string.app_name));
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

        Notification notification = builder.build();
        notification.contentIntent = PendingIntent.getActivity(this, 1,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        notification.bigContentView = bigContentView;
        notification.contentView = contentView;
        startForeground(FORGROUND_ID, notification);
    }

    private PendingIntent cmdIntent(String cmd) {
        Intent intent = new Intent(CMD_ACTION);
        intent.putExtra(EXTRA_CMD, cmd);
        return PendingIntent.getBroadcast(this, cmd.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyLog.d(TAG, "service onBind");
        return player;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MyLog.d(TAG, "service onUnbind");
        return super.onUnbind(intent);
    }

    public class Player extends Binder implements IPlayer, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

        private List<Playable> quque;//播放队列;
        private List<IPlayer.Callback> callbacks;
        private int mode = MODE_IN_TURN;//模式
        private int currentIndex;//当前位置
        private int state = STATE_STOP;//播放状态
        private MediaPlayer mediaPlayer;

        public Player() {
            mediaPlayer = new MediaPlayer();
            quque = new ArrayList<>();
            callbacks = new ArrayList<>();
            currentIndex = -1;

            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
        }

        @Override
        public List<? extends Playable> getQueue() {
            return quque;
        }

        /**
         * 加入播放队列
         */
        @Override
        public void enqueue(List<? extends Playable> musics) {
            quque.addAll(musics);
            Log.d(TAG, musics.size() + "首歌加入队列");
        }

        /**
         * 移出队列
         */
        public void dequeue(int position) {
            quque.remove(position);
            if (quque.size() == 0) {
                //移除后，队列空了
                play(null, 0);
            } else if (position < currentIndex) {
                //更正currentindex
                currentIndex--;
            } else if (position == currentIndex) {
                //当移除的歌曲正在播放时
                if (currentIndex == quque.size()) {
                    //刚好播放最后一首歌，又需要移除他,将播放第一首歌曲
                    currentIndex = 0;
                }
                play(currentIndex);
            }
        }

        /**
         * 替换队列，并播放position位置歌曲
         */
        @Override
        public void play(List<? extends Playable> musics, int position) {
            if (musics == null
                    || musics.size() == 0) {
                //替换为空队列，即清空了队列，停止播放
                synchronized (this) {
                    quque.clear();
                    mediaPlayer.reset();
                    currentIndex = -1;
                    changeState(STATE_STOP);
                    setCurMusicPic(null);
                    updateNotification();
                    return;
                }
            }

            quque.clear();
            quque.addAll(musics);
            play(position);
        }


        /**
         * 播放队列position位置歌曲
         */
        @Override
        public synchronized void play(int position) {

            if (state == STATE_PREPAREING) {
                //mediaplayer执行了prepareAsync方法,正在异步准备资源中，
                //不能重置mediaplayer，否则会出错
                return;
            }

            synchronized (this) {
                mediaPlayer.reset();
                currentIndex = position;
                changeState(STATE_PREPAREING);
                Playable playable = quque.get(currentIndex);
                try {
                    mediaPlayer.setDataSource(PlayerService.this, Uri.parse(playable.uri()));
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepareAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                    onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, -1);
                    return;
                }
            }
        }

        @Override
        public int getCurrentIndex() {
            return currentIndex;
        }

        @Override
        public Bitmap getCurMusicPic() {
            return curMusicPic;
        }

        private synchronized void changeState(int newState) {
            if (state != newState) {
                state = newState;
                for (Callback callback : callbacks) {
                    callback.onStateChange(state);
                }
            }
        }

        /**
         * 播放歌曲，如果歌曲是暂停状态，则恢复播放
         */
        @Override
        public void play() {
            if (state == STATE_PAUSE) {
                mediaPlayer.start();
                changeState(STATE_PLAYING);
                updateNotification();
            } else {
                play(currentIndex);
            }
        }

        @Override
        public void pause() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                changeState(STATE_PAUSE);
                updateNotification();
            }
        }

        @Override
        public void next() {
            if (quque.size() == 0) {
                return;
            }
            play((currentIndex + 1) % quque.size());
        }

        @Override
        public void last() {
            if (quque.size() == 0) {
                return;
            }
            int lastIndex = currentIndex;
            if (currentIndex > 0) {
                lastIndex = currentIndex - 1;
            }
            play(lastIndex);
        }

        @Override
        public int getState() {
            return state;
        }

        @Override
        public void setPlayMode(int mode) {
            if (this.mode != mode) {
                this.mode = mode;
                for (Callback callback : callbacks) {
                    callback.onModeChange(mode);
                }
            }
        }

        @Override
        public int getPlayMode() {
            return mode;
        }

        @Override
        public void addCallback(Callback callback) {
            callbacks.add(callback);
        }

        @Override
        public void removeCallback(Callback callback) {
            callbacks.remove(callback);
        }

        @Override
        public int getCurrentPosition() {
            synchronized (this) {
                if (isPrepared()) {
                    return mediaPlayer.getCurrentPosition();
                }
                return -1;
            }
        }

        private boolean isPrepared() {
            return state == STATE_PLAYING
                    || state == STATE_PAUSE;
        }

        @Override
        public int getDuration() {
            synchronized (this) {
                if (isPrepared()) {
                    return mediaPlayer.getDuration();
                }
                return -1;
            }
        }

        @Override
        public void seekTo(int msec) {
            if (isPrepared()) {
                mediaPlayer.seekTo(msec);
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            switch (mode) {
                case MODE_IN_TURN: {
                    next();
                }
                break;

                case MODE_LOOP: {
                    play(currentIndex);
                }
                break;

                case MODE_RANDOM: {
                    play(new Random().nextInt(quque.size()));
                }
                break;
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            state = STATE_STOP;
            TUtils.show(PlayerService.this, "播放出错，2s后跳到下一首");
            new Thread() {
                final int errorPos = currentIndex;

                @Override
                public void run() {
                    SystemClock.sleep(2000);
                    if (getCurrentIndex() == errorPos) {
                        next();
                    }
                }
            }.start();
            return true;
        }

        private void setCurMusicPic(Bitmap bmp) {
            if (curMusicPic != null) {
                curMusicPic.recycle();
            }
            curMusicPic = bmp;
            for (Callback callback : callbacks) {
                callback.onLoadedPicture(bmp);
            }
            updateNotification();
        }

        private DisplayImageOptions imageOptions = new DisplayImageOptions
                .Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        @Override
        public void onPrepared(MediaPlayer mp) {
            changeState(STATE_PREPARED);
            mp.start();
            changeState(STATE_PLAYING);
            //加载音乐图片
            Playable playable = quque.get(currentIndex);
            if (!TextUtils.isEmpty(playable.pic())) {
                ImageSize imageSize = new ImageSize(500, 500);
                imageLoader.loadImage("file://" + playable.pic(),
                        imageSize,
                        imageOptions,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                setCurMusicPic(loadedImage);
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                setCurMusicPic(null);
                            }
                        });
            } else {
                setCurMusicPic(null);
            }
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            //percent是已缓冲的时间减去已播放的时间 占  未播放的时间 的百分百
            //比如歌曲时长300s,已播放20s,已缓冲50s,则percent=(50-20)/(300-50);
            int duration = mp.getDuration();
            int currentPosition = mp.getCurrentPosition();
            int remain = duration - currentPosition;
            int buffedPosition = (int) (currentPosition + (remain * percent / 100f));

            MyLog.d(TAG, "buffered position " + buffedPosition);
            for (Callback callback : callbacks) {
                callback.onBufferingUpdate(buffedPosition);
            }
        }

        public void release() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            state = STATE_STOP;
        }
    }

}
