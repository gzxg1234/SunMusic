package com.sanron.sunmusic.service;

import com.sanron.sunmusic.model.Music;

import java.util.List;

/**
 * Created by Administrator on 2016/3/5.
 */
public interface IMusicPlayer {

    int MODE_IN_TURN = 0;//顺讯播放
    int MODE_RANDOM = 1;//随机播放
    int MODE_LOOP = 2;//循环播放

    int STATE_IDEL = 0;//空闲状态
    int STATE_PLAYING = 1;//播放中
    int STATE_PREPAREING = 2;//准备资源中
    int STATE_PAUSE = 3;//暂停

    List<Music> getQueue();

    void enqueue(List<Music> musics);

    void dequeue(int position);

    void play(List<Music> musics, int position);

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
        void onPrepared();
        void onModeChange(int newMode);
        void onBufferingUpdate(int bufferedPosition);
    }
}
