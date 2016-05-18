package com.sanron.music.playback;

import com.sanron.music.db.bean.Music;

import java.util.List;

/**
 * Created by Administrator on 2016/3/5.
 */
public interface Player {

    int MODE_IN_TURN = 0;//顺讯播放
    int MODE_RANDOM = 1;//随机播放
    int MODE_LOOP = 2;//循环播放

    int STATE_STOP = 0;//停止状态
    int STATE_PREPARING = 1;//准备资源中
    int STATE_PREPARED = 3;//准备资源完成
    int STATE_PLAYING = 4;//播放中
    int STATE_PAUSE = 5;//暂停
    int STATE_ERROR = 6;//错误

    List<Music> getQueue();

    void enqueue(List<Music> musics);

    void dequeue(int position);

    void clearQueue();

    void play(int position);

    int getCurrentIndex();

    Music getCurrentMusic();

    void togglePlayPause();

    void next();

    boolean isPlaying();

    void previous();

    int getState();

    void setPlayMode(int mode);

    int getPlayMode();

    void addPlayStateChangeListener(OnPlayStateChangeListener onPlayStateChangeListener);

    void removePlayStateChangeListener(OnPlayStateChangeListener onPlayStateChangeListener);

    void addOnBufferListener(OnBufferListener onBufferListener);

    void removeBufferListener(OnBufferListener onBufferListener);

    void addOnCompletedListener(OnCompletedListener onCompletedListener);

    void removeOnCompletedListener(OnCompletedListener onCompletedListener);

    int getProgress();

    int getDuration();

    void seekTo(int position);

    interface OnPlayStateChangeListener {
        void onPlayStateChange(int state);
    }

    interface OnCompletedListener {
        void onCompleted();
    }

    interface OnBufferListener {
        void onBufferingUpdate(int bufferedPosition);

        void onBufferStart();

        void onBufferEnd();
    }
}
