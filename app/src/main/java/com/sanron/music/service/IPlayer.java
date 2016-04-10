package com.sanron.music.service;

import android.graphics.Bitmap;

import com.sanron.music.db.model.Music;

import java.util.List;

/**
 * Created by Administrator on 2016/3/5.
 */
public interface IPlayer {

    int MODE_IN_TURN = 0;//顺讯播放
    int MODE_RANDOM = 1;//随机播放
    int MODE_LOOP = 2;//循环播放

    int STATE_STOP = 0;//停止状态
    int STATE_PREPARING = 1;//准备资源中
    int STATE_PREPARED = 2;//准备完毕
    int STATE_PLAYING = 3;//播放中
    int STATE_PAUSE = 4;//暂停

    List<Music> getQueue();

    void enqueue(List<Music> musics);

    void dequeue(int position);

    void clearQueue();

    void play(int position);

    int getCurrentIndex();

    Music getCurrentMusic();

    Bitmap getCurMusicPic();

    void togglePlayPause();

    void next();

    void previous();

    int getState();

    void setPlayMode(int mode);

    int getPlayMode();

    void addCallback(Callback callback);

    void removeCallback(Callback callback);

    void addOnBufferListener(OnBufferListener onBufferListener);

    void removeBufferListener(OnBufferListener onBufferListener);

    int getProgress();

    int getDuration();

    void seekTo(int position);

    interface Callback {
        void onLoadedPicture(Bitmap musicPic);

        void onStateChange(int state);
    }

    interface OnBufferListener {
        void onBufferingUpdate(int bufferedPosition);

        void onBufferStart();

        void onBufferEnd();
    }
}
