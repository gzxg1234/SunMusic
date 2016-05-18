package com.sanron.music.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

import com.sanron.music.AppContext;
import com.sanron.music.AppManager;
import com.sanron.music.R;
import com.sanron.music.db.bean.Music;
import com.sanron.music.playback.DDPlayer;
import com.sanron.music.playback.Player;

/**
 * Created by sanron on 16-5-18.
 */
public class PlayerNotificationManager extends BroadcastReceiver {

    private PlayerService mService;
    private DDPlayer mDDPlayer;
    private NotificationManagerCompat mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private Bitmap mImage;
    public static final int FOREGROUND_ID = 0x666;

    public static final String NOTIFY_ACTION = "com.sanron.music.PLAYBACK";
    public static final String EXTRA_CMD = "CMD";
    public static final String CMD_BACK_APP = "back_app";
    public static final String CMD_PREVIOUS = "previous";
    public static final String CMD_PLAY_PAUSE = "play_pause";
    public static final String CMD_NEXT = "next";
    public static final String CMD_LYRIC = "lyric";
    public static final String CMD_CLOSE = "close_app";

    public PlayerNotificationManager(PlayerService service) {
        mService = service;
        mDDPlayer = mService.getDDPlayer();
        mNotificationManager = NotificationManagerCompat.from(service);

        mNotificationBuilder = new NotificationCompat.Builder(service);
        mNotificationBuilder.setTicker("");
        mNotificationBuilder.setSmallIcon(R.mipmap.default_song_pic);
        mNotificationBuilder.setPriority(Notification.PRIORITY_MAX);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String cmd = intent.getStringExtra(EXTRA_CMD);
        switch (cmd) {
            case CMD_PREVIOUS: {
                mDDPlayer.previous();
            }
            break;

            case CMD_PLAY_PAUSE: {
                int state = mDDPlayer.getState();
                if (state == Player.STATE_PAUSE) {
                    mDDPlayer.resume();
                } else if (state == Player.STATE_PLAYING) {
                    mDDPlayer.pause();
                } else if (state == Player.STATE_STOP) {
                    if (mDDPlayer.getQueue().size() > 0) {
                        mDDPlayer.play(0);
                    }
                }
            }
            break;

            case CMD_NEXT: {
                mDDPlayer.next();
            }
            break;

            case CMD_LYRIC: {

            }
            break;

            case CMD_CLOSE: {
                ((AppContext) mService.getApplicationContext()).closeApp();
            }
            break;

            case CMD_BACK_APP: {
                Activity curActivity = AppManager.instance().currentActivity();
                if (curActivity == null) {
                    Intent in = mService.getPackageManager().getLaunchIntentForPackage(mService.getPackageName());
                    in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    mService.startActivity(in);
                } else {
                    Intent in = new Intent(mService, curActivity.getClass());
                    in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    curActivity.startActivity(in);
                }
            }
            break;
        }
    }

    private PendingIntent cmdIntent(String cmd) {
        Intent intent = new Intent(NOTIFY_ACTION);
        intent.putExtra(EXTRA_CMD, cmd);
        return PendingIntent.getBroadcast(mService, cmd.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void startNotification() {
        IntentFilter intentFilter = new IntentFilter(NOTIFY_ACTION);
        mService.registerReceiver(this, intentFilter);
        mService.startForeground(FOREGROUND_ID, createNotification());
    }

    public void stopNotification() {
        mService.unregisterReceiver(this);
        mService.stopForeground(true);
    }

    /**
     * 更新通知
     */
    public void updateNotification() {
        mNotificationManager.notify(FOREGROUND_ID, createNotification());
    }

    public void updateImage(Bitmap bitmap) {
        this.mImage = bitmap;
        updateNotification();
    }

    private Notification createNotification() {
        RemoteViews bigContentView = new RemoteViews(mService.getPackageName(), R.layout.notification_big_layout);
        RemoteViews contentView = new RemoteViews(mService.getPackageName(), R.layout.notification_small_layout);
        int state = mDDPlayer.getState();
        switch (state) {
            case Player.STATE_STOP: {
                bigContentView.setImageViewResource(R.id.iv_album_pic, R.mipmap.default_song_pic);
                bigContentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_24dp);
                contentView.setImageViewResource(R.id.iv_album_pic, R.mipmap.default_song_pic);
                contentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_36dp);
            }
            break;
            case Player.STATE_PAUSE: {
                bigContentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_24dp);
                contentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_play_arrow_black_36dp);
            }
            break;
            case Player.STATE_PLAYING: {
                bigContentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_pause_black_24dp);
                contentView.setImageViewResource(R.id.ibtn_play_pause, R.mipmap.ic_pause_black_36dp);
            }
            break;
        }

        if (mDDPlayer.getCurrentIndex() != -1) {
            Music music = mDDPlayer.getCurrentMusic();
            String artist = music.getArtist();
            artist = artist.equals("<unknown>") ? "未知歌手" : artist;
            String musicInfo = music.getTitle() + "-" + artist;

            bigContentView.setTextViewText(R.id.tv_music_info, musicInfo);
            contentView.setTextViewText(R.id.tv_queue_item_title, music.getTitle());
            contentView.setTextViewText(R.id.tv_text2, artist);

            if (mImage != null) {
                bigContentView.setImageViewBitmap(R.id.iv_album_pic, mImage);
                contentView.setImageViewBitmap(R.id.iv_album_pic, mImage);
            } else {
                bigContentView.setImageViewResource(R.id.iv_album_pic, R.mipmap.default_song_pic);
                contentView.setImageViewResource(R.id.iv_album_pic, R.mipmap.default_song_pic);
            }

        } else {
            bigContentView.setTextViewText(R.id.tv_music_info, mService.getText(R.string.app_name));
            contentView.setTextViewText(R.id.tv_title, mService.getText(R.string.app_name));
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

        Notification notification = mNotificationBuilder.build();
        notification.contentIntent = cmdIntent(CMD_BACK_APP);
        notification.bigContentView = bigContentView;
        notification.contentView = contentView;
        return notification;
    }
}
