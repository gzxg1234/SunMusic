package com.sanron.music.service;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by Administrator on 2016/3/5.
 */
public interface IPlayer {

    int MODE_IN_TURN = 0;//顺讯播放
    int MODE_RANDOM = 1;//随机播放
    int MODE_LOOP = 2;//循环播放

    int STATE_STOP = 0;//停止状态
    int STATE_PREPAREING = 1;//准备资源中
    int STATE_PREPARED = 2;//准备资源完成
    int STATE_PLAYING = 3;//播放中
    int STATE_PAUSE = 4;//暂停

    List<? extends Playable> getQueue();

    void enqueue(List<? extends Playable> musics);

    void dequeue(int position);

    void play(List<? extends Playable> musics, int position);

    void play(int position);

    int getCurrentIndex();

    Bitmap getCurMusicPic();

    void play();

    void pause();

    void next();

    void last();

    int getState();

    void setPlayMode(int mode);

    int getPlayMode();

    void addCallback(Callback callback);

    void removeCallback(Callback callback);

    int getCurrentPosition();

    int getDuration();

    void seekTo(int position);

    interface Callback {
        void onLoadedPicture(Bitmap musicPic);

        void onStateChange(int state);

        void onModeChange(int newMode);

        void onBufferingUpdate(int bufferedPosition);
    }
}
