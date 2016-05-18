package com.sanron.music.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sanron.music.api.JsonCallback;
import com.sanron.music.api.MusicApi;
import com.sanron.music.api.bean.LrcPicData;
import com.sanron.music.common.MyLog;
import com.sanron.music.db.bean.Music;
import com.sanron.music.playback.DDPlayer;
import com.sanron.music.playback.Player;

import java.util.List;

import okhttp3.Call;

/**
 * Created by Administrator on 2015/12/16.
 */
public class PlayerService extends Service implements Player.OnPlayStateChangeListener {

    public static final String TAG = PlayerService.class.getSimpleName();


    private PlayerNotificationManager mNotificationManager;

    private AudioManager mAudioManager;

    private DDPlayer mDDPlayer;


    private Call mSearchPicCall;

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.i(TAG, "service create");
        mDDPlayer = new DDPlayer(this);
        mDDPlayer.addPlayStateChangeListener(this);
        mNotificationManager = new PlayerNotificationManager(this);
        mNotificationManager.startNotification();
    }

    public DDPlayer getDDPlayer() {
        return mDDPlayer;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.i(TAG, "service destroy");
        mNotificationManager.stopNotification();
        mDDPlayer.removePlayStateChangeListener(this);
        mDDPlayer.release();
        mDDPlayer = null;
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
        mNotificationManager.updateImage(null);
        switch (state) {

            case Player.STATE_PREPARING: {
                //获取图片更新通知
                if (mSearchPicCall != null) {
                    mSearchPicCall.cancel();
                }
                Music music = mDDPlayer.getCurrentMusic();
                String artist = music.getArtist();
                mSearchPicCall = MusicApi.searchLrcPic(music.getTitle(),
                        "<unknown>".equals(artist) ? "" : artist,
                        2,
                        new JsonCallback<LrcPicData>() {
                            final int requestIndex = mDDPlayer.getCurrentIndex();

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
                                                    if (requestIndex == mDDPlayer.getCurrentIndex()) {
                                                        mNotificationManager.updateImage(loadedImage);
                                                    }
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                            }
                        });
            }
            break;
        }
    }
}
