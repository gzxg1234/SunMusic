package com.sanron.sunmusic.service;

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
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.sanron.sunmusic.AppContext;
import com.sanron.sunmusic.AppManager;
import com.sanron.sunmusic.R;
import com.sanron.sunmusic.activities.MainActivity;
import com.sanron.sunmusic.model.Music;
import com.sanron.sunmusic.utils.MyLog;
import com.sanron.sunmusic.utils.TUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2015/12/16.
 */
public class MusicService extends Service {

    public static final String TAG = MusicService.class.getSimpleName();
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

    private DisplayImageOptions displayImageOptions;
    private MusicPlayer player;

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
                    if (state == IMusicPlayer.STATE_PAUSE) {
                        player.play();
                    } else if (state == IMusicPlayer.STATE_IDEL) {
                        if (player.getQueue().size() > 0) {
                            player.play(0);
                        }
                    } else if (state == IMusicPlayer.STATE_PLAYING) {
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
        player = new MusicPlayer();
        IntentFilter intentFilter = new IntentFilter(CMD_ACTION);
        registerReceiver(cmdReceiver, intentFilter);

        builder = new NotificationCompat.Builder(this);
        builder.setTicker("");
        builder.setSmallIcon(R.mipmap.default_song_pic);
        builder.setPriority(Notification.PRIORITY_MAX);
        updateNotification();

        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        displayImageOptions = builder.imageScaleType(ImageScaleType.EXACTLY).build();
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

    /**
     * 更新通知
     */
    private void updateNotification() {
        RemoteViews bigContentView = new RemoteViews(getPackageName(), R.layout.notification_big_layout);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_small_layout);
        int state = player.getState();
        switch (state) {
            case IMusicPlayer.STATE_IDEL: {
                bigContentView.setImageViewResource(R.id.iv_picture, R.mipmap.default_song_pic);
                bigContentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_48dp);
                contentView.setImageViewResource(R.id.iv_picture, R.mipmap.default_song_pic);
                contentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_48dp);
            }
            break;
            case IMusicPlayer.STATE_PAUSE: {
                bigContentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_48dp);
                contentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_48dp);
            }
            break;
            case IMusicPlayer.STATE_PLAYING: {
                bigContentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_pause_black_48dp);
                contentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_pause_black_48dp);
            }
            break;
        }

        if (player.getCurrentIndex() != -1) {
            Music music = player.getQueue().get(player.getCurrentIndex());
            String musicInfo = music.getTitle() + "-" + music.getArtist();

            bigContentView.setTextViewText(R.id.tv_music_info, musicInfo);
            contentView.setTextViewText(R.id.tv_music_info, musicInfo);

            Bitmap bitmap = null;
            if (!TextUtils.isEmpty(music.getPicPath())) {
                int size = getResources().getDimensionPixelSize(R.dimen.notification_big_picture_size);
                ImageSize imageSize = new ImageSize(size, size);
                bitmap = imageLoader.loadImageSync("file://" + music.getPicPath(), imageSize, displayImageOptions);
            }
            if (bitmap != null) {
                bigContentView.setImageViewBitmap(R.id.iv_picture, bitmap);
                contentView.setImageViewBitmap(R.id.iv_picture, bitmap);
            }else{
                bigContentView.setImageViewResource(R.id.iv_picture, R.mipmap.default_song_pic);
                contentView.setImageViewResource(R.id.iv_picture, R.mipmap.default_song_pic);
            }

        } else {
            bigContentView.setTextViewText(R.id.tv_music_info, "SunMusic");
            contentView.setTextViewText(R.id.tv_music_info, "SunMusic");
        }

        bigContentView.setOnClickPendingIntent(R.id.ibtn_lrc, cmdIntent(CMD_LYRIC));
        bigContentView.setOnClickPendingIntent(R.id.ibtn_last, cmdIntent(CMD_PREVIOUS));
        bigContentView.setOnClickPendingIntent(R.id.ibtn_play_pause, cmdIntent(CMD_PLAY_PAUSE));
        bigContentView.setOnClickPendingIntent(R.id.ibtn_next, cmdIntent(CMD_NEXT));
        bigContentView.setOnClickPendingIntent(R.id.ibtn_close, cmdIntent(CMD_CLOSE));

        contentView.setOnClickPendingIntent(R.id.ibtn_lrc, cmdIntent(CMD_LYRIC));
        contentView.setOnClickPendingIntent(R.id.ibtn_play_pause, cmdIntent(CMD_PLAY_PAUSE));
        contentView.setOnClickPendingIntent(R.id.ibtn_next, cmdIntent(CMD_NEXT));
        contentView.setOnClickPendingIntent(R.id.ibtn_close, cmdIntent(CMD_CLOSE));

        Notification notification = builder.build();
        notification.contentIntent = PendingIntent.getActivity(this,1,
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

    public class MusicPlayer extends Binder implements IMusicPlayer, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

        private List<Music> quque;//播放队列;
        private List<IMusicPlayer.Callback> callbacks;
        private int mode = MODE_IN_TURN;//模式
        private int currentIndex;//当前位置
        private int state = STATE_IDEL;//播放状态
        private MediaPlayer mediaPlayer;

        public MusicPlayer() {
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
        public List<Music> getQueue() {
            return quque;
        }

        /**
         * 加入播放队列
         */
        @Override
        public void enqueue(List<Music> musics) {
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
        public void play(List<Music> musics, int position) {

            if (musics == null
                    || musics.size() == 0) {
                //替换为空队列，即清空了队列，停止播放
                synchronized (this) {
                    quque.clear();
                    mediaPlayer.reset();
                    currentIndex = -1;
                    changeState(STATE_IDEL);
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
        public void play(int position) {

            if (state == STATE_PREPAREING) {
                //mediaplayer执行了prepareAsync方法,正在异步准备资源中，
                //不能重置mediaplayer，否则会出错
                return;
            }

            synchronized (this) {
                currentIndex = position;
                mediaPlayer.reset();
                Music curSong = quque.get(currentIndex);
                try {
                    mediaPlayer.setDataSource(MusicService.this, Uri.parse(curSong.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.prepareAsync();
                } catch (Exception e) {
                    onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNSUPPORTED, -1);
                    return;
                }
                changeState(STATE_PREPAREING);
            }
        }

        @Override
        public int getCurrentIndex() {
            return currentIndex;
        }

        private void changeState(int newState) {
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
            mediaPlayer.pause();
            changeState(STATE_PAUSE);
            updateNotification();
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
            if (currentIndex > 0) {
                currentIndex--;
            }
            play(currentIndex);
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
            return state == STATE_PLAYING || state == STATE_PAUSE;
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
            TUtils.show(MusicService.this, "播放出错，2s后跳到下一首");
            updateNotification();
            final int errorPosition = getCurrentPosition();
            new Thread() {
                @Override
                public void run() {
                    SystemClock.sleep(2000);
                    if (getCurrentPosition() == errorPosition) {
                        next();
                    }
                }
            }.start();
            return true;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            changeState(STATE_PLAYING);
            updateNotification();
            for (Callback callback : callbacks) {
                callback.onPrepared();
            }
            MyLog.d(TAG, "start play " + quque.get(currentIndex).getDisplayName());
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
            synchronized (this) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                state = STATE_IDEL;
            }
        }
    }

}
