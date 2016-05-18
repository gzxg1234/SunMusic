package com.sanron.music.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.sanron.music.db.bean.Music;
import com.sanron.music.playback.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sanron on 16-4-18.
 */
public class PlayerUtil {
    private static Player sPlayer;
    private static Map<Context, ServiceBinder> sBinders = new HashMap<>();

    public static boolean bindService(Context context, ServiceConnection callback) {
        Intent intent = new Intent(context, PlayerService.class);
        context.startService(intent);
        ServiceBinder binder = new ServiceBinder(callback);
        boolean isSuccess = context.bindService(intent,
                binder, Context.BIND_AUTO_CREATE);
        if (isSuccess) {
            sBinders.put(context, binder);
        }
        return isSuccess;
    }

    public static void unbindService(Context context) {
        ServiceBinder binder = sBinders.remove(context);
        if (binder != null) {
            context.unbindService(binder);
        }
        if (sBinders.size() == 0) {
            sPlayer = null;
        }
    }

    public static class ServiceBinder implements ServiceConnection {
        private ServiceConnection callback;

        public ServiceBinder(ServiceConnection callback) {
            this.callback = callback;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (sPlayer == null) {
                sPlayer = (Player) service;
            }
            if (callback != null) {
                callback.onServiceConnected(name, service);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Context context = null;
            for (Map.Entry<Context, ServiceBinder> entry : sBinders.entrySet()) {
                if (this == entry.getValue()) {
                    context = entry.getKey();
                    break;
                }
            }
            sBinders.remove(context);
            if (sBinders.size() == 0) {
                sPlayer = null;
            }
        }
    }


    public static List<Music> getQueue() {
        if (sPlayer != null) {
            return sPlayer.getQueue();
        }
        return null;
    }

    public static void enqueue(List<Music> musics) {
        if (sPlayer != null) {
            sPlayer.enqueue(musics);
        }
    }

    public static void dequeue(int position) {
        if (sPlayer != null) {
            sPlayer.dequeue(position);
        }
    }

    public static void clearQueue() {
        if (sPlayer != null) {
            sPlayer.clearQueue();
        }
    }

    public static void play(int position) {
        if (sPlayer != null) {
            sPlayer.play(position);
        }
    }

    public static int getCurrentIndex() {
        if (sPlayer != null) {
            return sPlayer.getCurrentIndex();
        }
        return -1;
    }

    public static Music getCurrentMusic() {
        if (sPlayer != null) {
            return sPlayer.getCurrentMusic();
        }
        return null;
    }

    public static void togglePlayPause() {
        if (sPlayer != null) {
            sPlayer.togglePlayPause();
        }
    }

    public static void next() {
        if (sPlayer != null) {
            sPlayer.next();
        }
    }

    public static void previous() {
        if (sPlayer != null) {
            sPlayer.previous();
        }
    }

    public static int getState() {
        if (sPlayer != null) {
            return sPlayer.getState();
        }
        return Player.STATE_STOP;
    }

    public static void setPlayMode(int mode) {
        if (sPlayer != null) {
            sPlayer.setPlayMode(mode);
        }
    }

    public static int getPlayMode() {
        if (sPlayer != null) {
            return sPlayer.getPlayMode();
        }
        return Player.MODE_IN_TURN;
    }

    public static void addPlayStateChangeListener(Player.OnPlayStateChangeListener listener) {
        if (sPlayer != null) {
            sPlayer.addPlayStateChangeListener(listener);
        }
    }

    public static void removePlayStateChangeListener(Player.OnPlayStateChangeListener listener) {
        if (sPlayer != null) {
            sPlayer.removePlayStateChangeListener(listener);
        }
    }

    public static void addOnBufferListener(Player.OnBufferListener listener) {
        if (sPlayer != null) {
            sPlayer.addOnBufferListener(listener);
        }
    }

    public static void addOnCompletedListener(Player.OnCompletedListener listener) {
        if (sPlayer != null) {
            sPlayer.addOnCompletedListener(listener);
        }
    }

    public static void removeOnCompletedListener(Player.OnCompletedListener listener) {
        if (sPlayer != null) {
            sPlayer.removeOnCompletedListener(listener);
        }
    }


    public static void removeBufferListener(Player.OnBufferListener listener) {
        if (sPlayer != null) {
            sPlayer.removeBufferListener(listener);
        }
    }

    public static boolean isPlaying() {
        if (sPlayer != null) {
            return sPlayer.isPlaying();
        }
        return false;
    }

    public static int getProgress() {
        if (sPlayer != null) {
            return sPlayer.getProgress();
        }
        return 0;
    }

    public static int getDuration() {
        if (sPlayer != null) {
            return sPlayer.getDuration();
        }
        return 0;
    }

    public static void seekTo(int position) {
        if (sPlayer != null) {
            sPlayer.seekTo(position);
        }
    }


}
