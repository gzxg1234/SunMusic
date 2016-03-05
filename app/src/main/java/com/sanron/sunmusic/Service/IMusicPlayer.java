package com.sanron.sunmusic.service;

import com.sanron.sunmusic.model.SongInfo;

import java.util.List;

/**
 * Created by Administrator on 2016/3/5.
 */
public interface IMusicPlayer {

    int MODE_IN_TURN = 0;//顺讯播放
    int MODE_RANDOM = 1;//随机播放
    int MODE_LOOP = 2;//循环播放

    int STATE_STOP = 0;//停止
    int STATE_PLAYING = 1;//播放中
    int STATE_PAUSE = 2;//暂停
    int STATE_PREPARE = 3;//准备资源中

    List<SongInfo> getQuque();

    void enqueue(List<SongInfo> songInfos);

    void play(List<SongInfo> songInfos, int position);

    void play(int position);

    int getCurrentIndex();

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
        void onStateChange(int state);
        void onStartPlay(int position);
        void onModeChange(int newMode);
    }
}
