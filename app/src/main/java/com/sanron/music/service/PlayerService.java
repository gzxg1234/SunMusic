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
import android.os.PowerManager;
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
import com.sanron.music.db.model.Music;
import com.sanron.music.utils.MyLog;
import com.sanron.music.utils.TUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2015/12/16.
 */
public class PlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {

    public static final String TAG = PlayerService.class.getSimpleName();
    public static final int FORGROUND_ID = 0x88;

    public static final String CMD_ACTION = "com.sanron.music.PLAYBACK";
    public static final String EXTRA_CMD = "CMD";
    public static final String CMD_BACK_APP = "back_app";
    public static final String CMD_PREVIOUS = "previous";
    public static final String CMD_PLAY_PAUSE = "play_pause";
    public static final String CMD_NEXT = "next";
    public static final String CMD_LYRIC = "lyric";
    public static final String CMD_CLOSE = "close_app";

    private AppContext appContext;

    private PowerManager.WakeLock wakeLock;

    private NotificationCompat.Builder builder;

    private ImageLoader imageLoader = ImageLoader.getInstance();

    private DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheInMemory(true)
            .build();

    private AudioManager audioManager;

    private Bitmap curMusicPic;

    private Player player;

    private BroadcastReceiver cmdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra(EXTRA_CMD);
            MyLog.d(TAG, "playback cmd : " + cmd);
            switch (cmd) {
                case CMD_PREVIOUS: {
                    player.previous();
                }
                break;

                case CMD_PLAY_PAUSE: {
                    int state = player.getState();
                    if (state == IPlayer.STATE_PAUSE) {
                        player.resume();
                    } else if (state == IPlayer.STATE_PLAYING) {
                        player.pause();
                    } else if (state == IPlayer.STATE_STOP) {
                        if (player.getQueue().size() > 0) {
                            player.play(0);
                        }
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
                    appContext.closeApp();
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
        appContext = (AppContext) getApplicationContext();
        player = new Player();
        acquireWakeLock();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

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
        stopForeground(true);
        unregisterReceiver(cmdReceiver);
        audioManager.abandonAudioFocus(this);
        releaseWakeLock();
        player.release();
        player = null;
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
            case IPlayer.STATE_STOP: {
                bigContentView.setImageViewResource(R.id.top_image, R.mipmap.default_song_pic);
                bigContentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_24dp);
                contentView.setImageViewResource(R.id.top_image, R.mipmap.default_song_pic);
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
            Music music = player.getCurrentMusic();
            String musicInfo = music.getTitle() + "-" + music.getArtist();

            bigContentView.setTextViewText(R.id.tv_music_info, musicInfo);
            contentView.setTextViewText(R.id.tv_music_info, musicInfo);

            if (curMusicPic != null) {
                bigContentView.setImageViewBitmap(R.id.top_image, curMusicPic);
                contentView.setImageViewBitmap(R.id.top_image, curMusicPic);
            } else {
                bigContentView.setImageViewResource(R.id.top_image, R.mipmap.default_song_pic);
                contentView.setImageViewResource(R.id.top_image, R.mipmap.default_song_pic);
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
        notification.contentIntent = cmdIntent(CMD_BACK_APP);
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

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            //暂时失去音频焦点，比如电话

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                player.pause();
            }
            break;

            case AudioManager.AUDIOFOCUS_GAIN: {
                if (player.getState() == IPlayer.STATE_PAUSE) {
                    player.resume();
                }
            }
            break;
        }
    }

    public class Player extends Binder implements IPlayer, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

        private List<Music> queue;//播放队列;
        private List<IPlayer.Callback> callbacks;
        private int mode = MODE_IN_TURN;//模式
        private int currentIndex;//当前位置
        private int state = STATE_STOP;//播放状态
        private MediaPlayer mediaPlayer;

        public Player() {
            mediaPlayer = new MediaPlayer();
            queue = new ArrayList<>();
            callbacks = new ArrayList<>();
            currentIndex = -1;

            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
        }

        @Override
        public List<Music> getQueue() {
            return new ArrayList<>(queue);
        }

        /**
         * 加入播放队列
         */
        @Override
        public void enqueue(List<Music> musics) {
            queue.addAll(musics);
            Log.d(TAG, musics.size() + "首歌加入队列");
        }

        /**
         * 移出队列
         */
        public void dequeue(int position) {
            queue.remove(position);
            if (queue.size() == 0) {
                //移除后，队列空了
                clearQueue();
            } else if (position < currentIndex) {
                //更正currentindex
                currentIndex--;
            } else if (position == currentIndex) {
                //当移除的歌曲正在播放时
                if (currentIndex == queue.size()) {
                    //刚好播放最后一首歌，又需要移除他,将播放第一首歌曲
                    currentIndex = 0;
                }
                play(currentIndex);
            }
        }

        @Override
        public void clearQueue() {
            synchronized (this) {
                queue.clear();
                currentIndex = -1;
                mediaPlayer.reset();
                changeState(STATE_STOP);
                setCurMusicPic(null);
                updateNotification();
                return;
            }
        }

        /**
         * 播放队列position位置歌曲
         */
        @Override
        public void play(int position) {

            if (state == STATE_PREPARING) {
                //mediaplayer执行了prepareAsync方法,正在异步准备资源中，
                //不能重置mediaplayer，否则会出错
                return;
            }

            synchronized (this) {
                mediaPlayer.reset();
                currentIndex = position;
                Music playable = queue.get(currentIndex);
                try {
                    changeState(STATE_PREPARING);
                    mediaPlayer.setDataSource(PlayerService.this, Uri.parse(playable.getPath()));
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
        public Music getCurrentMusic() {
            if (currentIndex == -1) {
                return null;
            }
            return queue.get(currentIndex);
        }

        @Override
        public Bitmap getCurMusicPic() {
            return curMusicPic;
        }

        private void changeState(int newState) {
            if (state != newState) {
                state = newState;
                for (Callback callback : callbacks) {
                    callback.onStateChange(state);
                }
            }
        }

        void resume() {
            mediaPlayer.start();
            changeState(STATE_PLAYING);
            updateNotification();
        }

        void pause() {
            mediaPlayer.pause();
            changeState(STATE_PAUSE);
            updateNotification();
        }

        @Override
        public void togglePlayPause() {
            if (state == STATE_PAUSE) {
                resume();
            } else if (state == STATE_PLAYING) {
                pause();
            }
        }


        @Override
        public void next() {
            if (queue.size() == 0) {
                return;
            }
            play((currentIndex + 1) % queue.size());
        }

        @Override
        public void previous() {
            if (queue.size() == 0) {
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
        public int getProgress() {
            synchronized (this) {
                if (state >= IPlayer.STATE_PREPARED) {
                    return mediaPlayer.getCurrentPosition();
                }
                return -1;
            }
        }

        @Override
        public int getDuration() {
            synchronized (this) {
                if (state >= IPlayer.STATE_PREPARED) {
                    return mediaPlayer.getDuration();
                }
                return -1;
            }
        }

        @Override
        public void seekTo(int msec) {
            if (state >= STATE_PREPARED) {
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
                    play(new Random().nextInt(queue.size()));
                }
                break;
            }
        }


        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            System.out.println(state);
            state = STATE_STOP;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    TUtils.show(PlayerService.this, "播放出错，2s后跳到下一首");
                }
            });
            handler.postDelayed(new Runnable() {
                final int errorPos = currentIndex;

                @Override
                public void run() {
                    if (getCurrentIndex() == errorPos) {
                        next();
                    }
                }
            }, 2000);
            return true;
        }

        private void setCurMusicPic(Bitmap bmp) {
            curMusicPic = bmp;
            for (Callback callback : callbacks) {
                callback.onLoadedPicture(bmp);
            }
            updateNotification();
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            boolean isPause = (state == STATE_PAUSE);
            changeState(STATE_PREPARED);
            if (!isPause) {
                mp.start();
                changeState(STATE_PLAYING);
            }
            //加载音乐图片
            Music music = queue.get(currentIndex);
            if (!TextUtils.isEmpty(music.getPic())) {
                ImageSize imageSize = new ImageSize(500, 500);
                imageLoader.loadImage("file://" + music.getPic(),
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

        void release() {
            changeState(STATE_STOP);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
