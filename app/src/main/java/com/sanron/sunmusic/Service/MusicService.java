package com.sanron.sunmusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.utils.MyLog;
import com.sanron.sunmusic.utils.T;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2015/12/16.
 */
public class MusicService extends Service {

    public static final String TAG = MusicService.class.getSimpleName();

    private MusicPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MusicPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return player;
    }

    public class MusicPlayer extends Binder implements IMusicPlayer, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

        private List<SongInfo> quque;//播放队列;
        private List<IMusicPlayer.Callback> callbacks;
        private int mode = MODE_IN_TURN;//模式
        private int currentIndex;//当前位置
        private int state = STATE_STOP;//播放状态
        private MediaPlayer mediaPlayer;

        public MusicPlayer() {
            mediaPlayer = new MediaPlayer();
            quque = new ArrayList<>();
            callbacks = new ArrayList<>();
            currentIndex = -1;
        }


        @Override
        public List<SongInfo> getQuque() {
            return quque;
        }

        /**
         * 加入播放队列
         *
         * @param songInfos
         */
        @Override
        public void enqueue(List<SongInfo> songInfos) {
            quque.addAll(songInfos);
            Log.i(TAG, songInfos.size() + "首歌加入队列");
        }

        /**
         * 替换队列，并播放position位置歌曲
         */
        @Override
        public void play(List<SongInfo> songInfos, int position) {
            quque = songInfos;
            play(position);
        }


        /**
         * 播放队列position位置歌曲
         */
        @Override
        public void play(int position) {
            if (state == STATE_PREPARE) {
                //mediaplayer执行了prepareAsync方法,正在异步准备资源中，
                //不能重置mediaplayer，否则会出错
                return;
            }

            synchronized (this) {
                mediaPlayer.reset();
                currentIndex = position;
                SongInfo curSong = quque.get(currentIndex);
                try {
                    mediaPlayer.setDataSource(MusicService.this, Uri.parse(curSong.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setOnErrorListener(this);
                mediaPlayer.setOnBufferingUpdateListener(this);
                mediaPlayer.prepareAsync();
            }
            changeState(STATE_PREPARE);
        }

        @Override
        public int getCurrentIndex() {
            return currentIndex;
        }

        private void changeState(int newState) {
            if (state != newState) {
                state = newState;
                for (Callback callback : callbacks) {
                    callback.onStateChange(state);
                }
            }
        }

        /**
         * 播放歌曲，如果歌曲是暂停状态，则恢复播放
         */
        @Override
        public void play() {
            if (state == STATE_PAUSE) {
                mediaPlayer.start();
                changeState(STATE_PLAYING);
            } else {
                play(currentIndex);
            }
        }

        @Override
        public void pause() {
            mediaPlayer.pause();
            changeState(STATE_PAUSE);
        }

        @Override
        public void next() {
            if (quque.size() == 0) {
                return;
            }
            play(++currentIndex % quque.size());
        }

        @Override
        public void last() {
            if (quque.size() == 0) {
                return;
            }
            if (currentIndex > 0) {
                currentIndex--;
            }
            play(currentIndex);
        }

        @Override
        public int getState() {
            return state;
        }

        @Override
        public void setPlayMode(int mode) {
            if (this.mode != mode) {
                this.mode = mode;
                for (Callback callback : callbacks) {
                    callback.onModeChange(mode);
                }
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
        public int getCurrentPosition() {
            synchronized (this) {
                if (state == STATE_PREPARE
                        || state == STATE_STOP) {
                    return -1;
                }
                return mediaPlayer.getCurrentPosition();
            }
        }

        @Override
        public int getDuration() {
            synchronized (this) {
                if (state == STATE_PREPARE
                        || state == STATE_STOP) {
                    return -1;
                }
                return mediaPlayer.getDuration();
            }
        }

        @Override
        public void seekTo(int msec) {
            if(state == STATE_PLAYING
                    || state == STATE_PAUSE){
                mediaPlayer.seekTo(msec);
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            MyLog.i(TAG, "play end");
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
                    play(new Random().nextInt(quque.size()));
                }
                break;
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            T.show(MusicService.this, "播放出错，2s后跳到下一首");
            final int errorPosition = getCurrentPosition();
            new Thread(){
                @Override
                public void run() {
                    SystemClock.sleep(2000);
                    if(getCurrentPosition() == errorPosition){
                        next();
                    }
                }
            }.start();
            return true;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            changeState(STATE_PLAYING);
            for (Callback callback : callbacks) {
                callback.onStartPlay(currentIndex);
            }
            MyLog.i(TAG, "start : " + quque.get(currentIndex).getDisplayName());
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {

        }

    }

}
