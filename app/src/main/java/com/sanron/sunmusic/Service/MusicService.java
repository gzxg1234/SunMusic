package com.sanron.sunmusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.utils.MyLog;
import com.sanron.sunmusic.utils.T;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2015/12/16.
 */
public class MusicService extends Service {

    public static final String TAG = MusicService.class.getSimpleName();


    public static final int STATE_PAUSE = 0;
    public static final int STATE_PLAYING = 1;

    private int state = STATE_PLAYING;//播放状态
    private MusicPlayer player;
    private MediaPlayer mediaPlayer;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return player;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MusicPlayer();
        mediaPlayer = new MediaPlayer();
    }

    public class MusicPlayer extends Binder implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

        public static final int MODE_IN_TURN = 1;//顺讯播放
        public static final int MODE_RANDOM = 2;//随机播放
        public static final int MODE_LOOP = 3;//循环播放

        private List<SongInfo> quque;//播放队列;
        private int mode = MODE_IN_TURN;
        private int currentIndex;//当前位置

        public List<SongInfo> getQuque(){
            return quque;
        }

        /**
         * 加入播放队列
         *
         * @param songInfos
         */
        public void enqueue(List<SongInfo> songInfos) {
            quque.addAll(songInfos);
            Log.i(TAG, songInfos.size() + "首歌加入队列");
        }

        /**
         * 替换队列，并播放position位置歌曲
         */
        public void play(List<SongInfo> songInfos, int position) {
            quque = songInfos;
            currentIndex = position;
            play();
        }

        public void play(int posiiton){
            currentIndex = posiiton;
            state = STATE_PLAYING;
            play();
        }


        public int play() {

            if (state == STATE_PAUSE) {
                mediaPlayer.start();
            } else if (state == STATE_PLAYING) {
                mediaPlayer.reset();
                final SongInfo curSong = quque.get(currentIndex);
                MyLog.i(TAG, "start : " + curSong.getDisplayName());
                try {
                    mediaPlayer.setDataSource(MusicService.this, Uri.parse(curSong.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setOnErrorListener(this);
                mediaPlayer.setOnBufferingUpdateListener(this);

                mediaPlayer.prepareAsync();
            }

            return 0;
        }

        public void pause() {
            mediaPlayer.pause();
            state = STATE_PAUSE;
        }

        public void next() {
            currentIndex = ++currentIndex % quque.size();
            play();
        }

        public void last() {
            if(currentIndex > 0){
                currentIndex -- ;
            }
            play();
        }

        public int getState() {
            return state;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            switch (mode) {
                case MODE_IN_TURN: {
                    currentIndex = ++currentIndex % quque.size();
                }
                break;

                case MODE_LOOP: {
                }
                break;

                case MODE_RANDOM: {
                    currentIndex = new Random().nextInt(quque.size());
                }
                break;
            }
            play();
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            T.show(MusicService.this,"播放出错，跳到下一首");
            next();
            return true;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {

        }
    }

}
