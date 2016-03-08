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

import com.sanron.sunmusic.model.Music;
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

        private List<Music> quque;//播放队列;
        private List<IMusicPlayer.Callback> callbacks;
        private int mode = MODE_IN_TURN;//模式
        private int currentIndex;//当前位置
        private int state = STATE_IDEL;//播放状态
        private MediaPlayer mediaPlayer;

        public MusicPlayer() {
            mediaPlayer = new MediaPlayer();
            quque = new ArrayList<>();
            callbacks = new ArrayList<>();
            currentIndex = -1;

            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
        }


        @Override
        public List<Music> getQuque() {
            return quque;
        }

        /**
         * 加入播放队列
         */
        @Override
        public void enqueue(List<Music> musics) {
            quque.addAll(musics);
            Log.i(TAG, musics.size() + "首歌加入队列");
        }

        /**
         * 移出队列
         */
        public void dequeue(int position){
            quque.remove(position);
            if(quque.size() == 0){
                //移除后，队列空了
                play(null,0);
            }else if(position < currentIndex){
                //更正currentindex
                currentIndex --;
            }else if(position == currentIndex){
                //当移除的歌曲正在播放时
                if(currentIndex == quque.size()){
                    //刚好播放最后一首歌，又需要移除他,将播放第一首歌曲
                    currentIndex = 0;
                }
                play(currentIndex);
            }
        }

        /**
         * 替换队列，并播放position位置歌曲
         */
        @Override
        public void play(List<Music> musics, int position) {

            if(musics == null
                    || musics.size() == 0){
                //替换为空队列，即清空了队列，停止播放
                synchronized (this) {
                    quque.clear();
                    mediaPlayer.reset();
                    changeState(STATE_IDEL);
                    return;
                }
            }

            quque.clear();
            quque.addAll(musics);
            play(position);
        }


        /**
         * 播放队列position位置歌曲
         */
        @Override
        public void play(int position) {

            if (state == STATE_PREPAREING) {
                //mediaplayer执行了prepareAsync方法,正在异步准备资源中，
                //不能重置mediaplayer，否则会出错
                return;
            }

            synchronized (this) {
                currentIndex = position;
                mediaPlayer.reset();
                Music curSong = quque.get(currentIndex);
                try {
                    mediaPlayer.setDataSource(MusicService.this, Uri.parse(curSong.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepareAsync();
                changeState(STATE_PREPAREING);
            }
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
            play((currentIndex + 1) % quque.size());
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
                if (isPrepared()) {
                    return mediaPlayer.getCurrentPosition();
                }
                return -1;
            }
        }

        private boolean isPrepared() {
            return state == STATE_PLAYING || state == STATE_PAUSE;
        }

        @Override
        public int getDuration() {
            synchronized (this) {
                if (isPrepared()) {
                    return mediaPlayer.getDuration();
                }
                return -1;
            }
        }

        @Override
        public void seekTo(int msec) {
            if (isPrepared()) {
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
            new Thread() {
                @Override
                public void run() {
                    SystemClock.sleep(2000);
                    if (getCurrentPosition() == errorPosition) {
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
            for(Callback callback:callbacks){
                callback.onPrepared();
            }
            MyLog.i(TAG, "start : " + quque.get(currentIndex).getDisplayName());
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            //percent是已缓冲的时间减去已播放的时间 占  未播放的时间 的百分百
            //比如歌曲时长300s,已播放20s,已缓冲50s,则percent=(50-20)/(300-50);
            int duration = mp.getDuration();
            int currentPosition = mp.getCurrentPosition();
            int remain = duration - currentPosition;
            int buffedPosition = (int) (currentPosition + (remain * percent / 100f));

            MyLog.i(TAG, "buffered position " + buffedPosition);
            for (Callback callback : callbacks) {
                callback.onBufferingUpdate(buffedPosition);
            }
        }
    }

}
