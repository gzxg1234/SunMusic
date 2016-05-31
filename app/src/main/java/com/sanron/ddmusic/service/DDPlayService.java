package com.sanron.ddmusic.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sanron.ddmusic.api.LrcPicProvider;
import com.sanron.ddmusic.common.MyLog;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.playback.DDPlayer;
import com.sanron.ddmusic.playback.Player;

/**
 * Created by Administrator on 2015/12/16.
 */
public class DDPlayService extends Service implements Player.OnPlayStateChangeListener, LrcPicProvider.OnLrcPicChangeCallback {

    public static final String TAG = DDPlayService.class.getSimpleName();


    private PlayNotificationManager mNotificationManager;

    private DDPlayer mDDPlayer;

    private NonViewAware mNonViewAware;

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.i(TAG, "service create");
        mDDPlayer = new DDPlayer(this);
        mNotificationManager = new PlayNotificationManager(this);
        mNotificationManager.startNotification();
        mDDPlayer.addPlayStateChangeListener(this);
        mDDPlayer.loadLastState();
        LrcPicProvider.get().addOnLrcPicChangeCallback(this);
    }

    public DDPlayer getDDPlayer() {
        return mDDPlayer;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i(TAG, "service destroy");
        LrcPicProvider.get().removeOnLrcPicChangeCallback(this);
        mDDPlayer.removePlayStateChangeListener(this);
        mDDPlayer.release();
        mDDPlayer = null;
        mNotificationManager.stopNotification();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyLog.d(TAG, "service onBind");
        return mDDPlayer;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MyLog.d(TAG, "service onUnbind");
        return super.onUnbind(intent);
    }


    @Override
    public void onPlayStateChange(int state) {
        switch (state) {
            case Player.STATE_PLAYING:
            case Player.STATE_PAUSE:
            case Player.STATE_IDLE: {
                mNotificationManager.updateNotification();
            }
            break;

            case Player.STATE_PREPARING: {
                mNotificationManager.updateImage(null);
                Music music = mDDPlayer.getCurrentMusic();
                String artist = music.getArtist();

                LrcPicProvider.get().search(music.getTitle(),
                        "<unknown>".equals(artist) ? "" : artist);
            }
            break;
        }
    }

    @Override
    public void onLrcPicChange() {
        if (mNonViewAware != null) {
            ImageLoader.getInstance().cancelDisplayTask(mNonViewAware);
        }
        String songPicture = LrcPicProvider.get().getSongPictureLink();
        if (!TextUtils.isEmpty(songPicture)) {
            mNonViewAware = new NonViewAware(
                    new ImageSize(ViewTool.dpToPx(100), ViewTool.dpToPx(100)), ViewScaleType.CROP);
            ImageLoader.getInstance()
                    .displayImage(songPicture, mNonViewAware, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            mNotificationManager.updateImage(loadedImage);
                        }
                    });
        }
    }
}
