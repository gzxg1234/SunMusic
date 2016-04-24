package com.sanron.music.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.text.TextUtils;

import com.sanron.music.AppContext;
import com.sanron.music.db.bean.Music;
import com.sanron.music.api.bean.SongUrlInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sanron on 16-4-18.
 */
public class PlayerUtil {
    private static IPlayer sPlayer;
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
                sPlayer = (IPlayer) service;
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

    public static Bitmap getCurMusicPic() {
        if (sPlayer != null) {
            return sPlayer.getCurMusicPic();
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
        return IPlayer.STATE_STOP;
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
        return IPlayer.MODE_IN_TURN;
    }

    public static void addPlayStateChangeListener(IPlayer.OnPlayStateChangeListener listener) {
        if (sPlayer != null) {
            sPlayer.addPlayStateChangeListener(listener);
        }
    }

    public static void removePlayStateChangeListener(IPlayer.OnPlayStateChangeListener listener) {
        if (sPlayer != null) {
            sPlayer.removePlayStateChangeListener(listener);
        }
    }

    public static void addOnBufferListener(IPlayer.OnBufferListener listener) {
        if (sPlayer != null) {
            sPlayer.addOnBufferListener(listener);
        }
    }

    public static void removeBufferListener(IPlayer.OnBufferListener listener) {
        if (sPlayer != null) {
            sPlayer.removeBufferListener(listener);
        }
    }

    public static void addOnLoadedPictureListener(IPlayer.OnLoadedPictureListener listener) {
        if (sPlayer != null) {
            sPlayer.addOnLoadedPictureListener(listener);
        }
    }

    public static void removeOnLoadedPictureListener(IPlayer.OnLoadedPictureListener listener) {
        if (sPlayer != null) {
            sPlayer.removeOnLoadedPictureListener(listener);
        }
    }

    public static int getProgress() {
        if (sPlayer != null) {
            return sPlayer.getProgress();
        }
        return -1;
    }

    public static int getDuration() {
        if (sPlayer != null) {
            return sPlayer.getDuration();
        }
        return -1;
    }

    public static void seekTo(int position) {
        if (sPlayer != null) {
            sPlayer.seekTo(position);
        }
    }

    /**
     * 根据网络选择合适的音质歌曲文件
     *
     * @param context
     * @param files
     * @return
     */
    public static SongUrlInfo.SongUrl.Url selectFileUrl(Context context, List<SongUrlInfo.SongUrl.Url> files) {
        SongUrlInfo.SongUrl.Url result = null;
        if (files != null
                && files.size() > 0) {
            SongUrlInfo.SongUrl.Url minBitrateFile = null;
            SongUrlInfo.SongUrl.Url maxBitrateFile = null;
            int minBitrate = Integer.MAX_VALUE;
            int maxBitrate = 0;
            for (SongUrlInfo.SongUrl.Url url : files) {
                String fileUrl = url.fileLink;
                int fileBitrate = url.fileBitrate;
                if (TextUtils.isEmpty(fileUrl)) {
                    continue;
                }

                if (fileBitrate < minBitrate) {
                    minBitrateFile = url;
                    minBitrate = fileBitrate;
                }

                if (fileBitrate > maxBitrate) {
                    maxBitrateFile = url;
                    maxBitrate = fileBitrate;
                }
            }

            int netType = ((AppContext) context.getApplicationContext()).checkNet();
            if (netType == ConnectivityManager.TYPE_MOBILE) {
                //移动网络选择最低音质
                result = minBitrateFile;
            } else if (netType == ConnectivityManager.TYPE_WIFI) {
                //WIFI网络选择最高音质
                result = maxBitrateFile;
            }
        }
        return result;
    }

}
