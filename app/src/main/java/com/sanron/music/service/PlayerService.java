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
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sanron.music.AppContext;
import com.sanron.music.AppManager;
import com.sanron.music.R;
import com.sanron.music.db.model.Music;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.DetailSongInfo;
import com.sanron.music.net.bean.LrcPicResult;
import com.sanron.music.utils.MyLog;
import com.sanron.music.utils.T;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;

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

    public static final int WHAT_PLAY_ERROR = 1;
    public static final int WHAT_BUFFER_TIMEOUT = 2;

    private AppContext appContext;

    private PowerManager.WakeLock wakeLock;

    private NotificationCompat.Builder builder;


    private AudioManager audioManager;

    private Bitmap curMusicPic;

    private Player player;

    private boolean isLossAudioFocus;

    private ImageLoader imageLoader = ImageLoader.getInstance();

    private DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true)
            .build();

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_PLAY_ERROR: {
                    player.next();
                }
                break;

                case WHAT_BUFFER_TIMEOUT: {
                    player.next();
                    T.show(getApplicationContext(), "缓冲超时,自动播放下一曲");
                }
                break;
            }
        }
    };

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
            String artist = music.getArtist();
            artist = artist.equals("<unknown>") ? "未知歌手" : artist;
            String musicInfo = music.getTitle() + "-" + artist;

            bigContentView.setTextViewText(R.id.tv_music_info, musicInfo);
            contentView.setTextViewText(R.id.tv_title, music.getTitle());
            contentView.setTextViewText(R.id.tv_artist, artist);

            if (curMusicPic != null) {
                bigContentView.setImageViewBitmap(R.id.top_image, curMusicPic);
                contentView.setImageViewBitmap(R.id.top_image, curMusicPic);
            } else {
                bigContentView.setImageViewResource(R.id.top_image, R.mipmap.default_song_pic);
                contentView.setImageViewResource(R.id.top_image, R.mipmap.default_song_pic);
            }

        } else {
            bigContentView.setTextViewText(R.id.tv_music_info, getText(R.string.app_name));
            contentView.setTextViewText(R.id.tv_title, getText(R.string.app_name));
            contentView.setTextViewText(R.id.tv_artist, "");
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
                if (player.getState() == IPlayer.STATE_PLAYING) {
                    player.pause();
                    isLossAudioFocus = true;
                }
            }
            break;

            case AudioManager.AUDIOFOCUS_GAIN: {
                if (player.getState() == IPlayer.STATE_PAUSE
                        && isLossAudioFocus) {
                    player.resume();
                    isLossAudioFocus = false;
                }
            }
            break;
        }
    }

    public class Player extends Binder implements IPlayer, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener {

        /**
         * 播放队列
         */
        private List<Music> queue;
        /**
         * 播放模式
         */
        private int mode = MODE_IN_TURN;
        /**
         * 当前播放歌曲位置
         */
        private int currentIndex;
        /**
         * 播放器状态
         */
        private int state = STATE_STOP;
        /**
         * 音乐文件地址请求
         */
        private Call fileLinkCall;
        private Call searchPicCall;
        private List<OnBufferListener> onBufferListeners;
        private List<IPlayer.Callback> callbacks;
        private MediaPlayer mediaPlayer;

        /**
         * 超时时间
         */
        public static final int BUFFER_TIMEOUT = 30 * 1000;

        public Player() {
            onBufferListeners = new ArrayList<>();
            queue = new ArrayList<>();
            callbacks = new ArrayList<>();
            currentIndex = -1;
        }

        private void initMediaPlayer() {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
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
            Log.d(TAG, "enqueue " + musics.size() + " songs");
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
            if (mediaPlayer == null) {
                return;
            }
            synchronized (this) {
                queue.clear();
                currentIndex = -1;
                mediaPlayer.reset();
                changeState(STATE_STOP);
                setCurMusicPic(null);
                updateNotification();
            }
        }

        /**
         * 播放队列position位置歌曲
         */
        @Override
        public void play(int position) {
            handler.removeMessages(WHAT_PLAY_ERROR);
            handler.removeMessages(WHAT_BUFFER_TIMEOUT);
            if (fileLinkCall != null) {
                fileLinkCall.cancel();
            }
            if (searchPicCall != null) {
                searchPicCall.cancel();
            }


            if (mediaPlayer != null) {
                mediaPlayer.reset();
            }
            currentIndex = position;
            Music music = queue.get(currentIndex);
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
            synchronized (this) {
                if (mediaPlayer == null) {
                    initMediaPlayer();
                }
                try {
                    mediaPlayer.setDataSource(PlayerService.this, uri);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepareAsync();
                } catch (IllegalStateException e) {
                    MyLog.e(TAG, "IllegalState");
                } catch (IOException e) {
                    e.printStackTrace();
                    handlerPlayError("播放出错");
                }
            }
        }


        private void handlerPlayError(final String errorMsg) {
            state = STATE_STOP;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    T.show(PlayerService.this, errorMsg + ",3s后播放下一曲");
                }
            });
            handler.sendEmptyMessageDelayed(WHAT_PLAY_ERROR, 3000);
        }


        private void playWebMusic(final String songid) {
            fileLinkCall = MusicApi.songLink(songid, new ApiCallback<DetailSongInfo>() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    if (!call.isCanceled()) {
                        handlerPlayError("网络请求失败");
                    }
                }

                @Override
                public void onSuccess(Call call, DetailSongInfo data) {
                    DetailSongInfo.SongUrl.FileInfo playFileInfo = null;
                    DetailSongInfo.SongUrl songUrl = data.getSongUrl();
                    if (songUrl != null) {
                        List<DetailSongInfo.SongUrl.FileInfo> files = songUrl.getFileInfos();
                        playFileInfo = PlayerHelper.selectFileUrl(getApplicationContext(), files);
                    }
                    if (playFileInfo == null) {
                        handlerPlayError("此歌曲暂无网络资源");
                    } else {
                        prepare(Uri.parse(playFileInfo.getFileLink()));
                    }
                }
            });
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
            state = newState;
            for (Callback callback : callbacks) {
                callback.onStateChange(state);
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
        public void addOnBufferListener(OnBufferListener onBufferListener) {
            onBufferListeners.add(onBufferListener);
        }

        @Override
        public void removeBufferListener(OnBufferListener onBufferListener) {
            onBufferListeners.remove(onBufferListener);
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
                    mediaPlayer.start();
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
            String artist = music.getArtist();
            searchPicCall = MusicApi.searchLrcPic(music.getTitle(),
                    "<unknown>".equals(artist) ? "" : artist,
                    2,
                    new ApiCallback<LrcPicResult>() {
                        final int requestIndex = currentIndex;

                        @Override
                        public void onSuccess(Call call, LrcPicResult data) {
                            List<LrcPicResult.LrcPic> lrcPics = data.getLrcPics();
                            Bitmap loadedImage = null;
                            String pic = null;
                            if (lrcPics != null) {
                                for (LrcPicResult.LrcPic lrcPic : lrcPics) {
                                    pic = lrcPic.getPic1000x1000();
                                    if (TextUtils.isEmpty(pic)) {
                                        pic = lrcPic.getPic500x500();
                                    }
                                    if (!TextUtils.isEmpty(pic)) {
                                        break;
                                    }
                                }
                            }
                            if (!TextUtils.isEmpty(pic)) {
                                loadedImage = imageLoader.loadImageSync(pic, imageOptions);
                            }
                            if (requestIndex == getCurrentIndex()) {
                                final Bitmap finalLoadedImage = loadedImage;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        setCurMusicPic(finalLoadedImage);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            setCurMusicPic(null);
                        }
                    });
        }


        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            //percent是已缓冲的时间减去已播放的时间 占  未播放的时间 的百分百
            //比如歌曲时长300s,已播放20s,已缓冲50s,则percent=(50-20)/(300-50);
            if (state >= STATE_PREPARED) {
                int duration = mp.getDuration();
                int currentPosition = mp.getCurrentPosition();
                int remain = duration - currentPosition;
                int buffedPosition = currentPosition + (int) Math.floor(remain * percent / 100f);

                for (OnBufferListener listener : onBufferListeners) {
                    listener.onBufferingUpdate(buffedPosition);
                }
            }
        }

        void release() {
            changeState(STATE_STOP);
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                    handler.sendEmptyMessageDelayed(WHAT_BUFFER_TIMEOUT, BUFFER_TIMEOUT);
                    for (OnBufferListener listener : onBufferListeners) {
                        listener.onBufferStart();
                    }
                }
                break;

                case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                    handler.removeMessages(WHAT_BUFFER_TIMEOUT);
                    for (OnBufferListener listener : onBufferListeners) {
                        listener.onBufferEnd();
                    }
                }
                break;
            }
            return true;
        }
    }

}
