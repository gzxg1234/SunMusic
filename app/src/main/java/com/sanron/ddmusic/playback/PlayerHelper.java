package com.sanron.ddmusic.playback;

import android.content.Context;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.sanron.ddmusic.AppContext;
import com.sanron.ddmusic.api.bean.SongUrlInfo;
import com.sanron.ddmusic.db.bean.Music;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by sanron on 16-5-18.
 */
public class PlayerHelper {

    /**
     * 根据网络选择合适的音质歌曲文件
     *
     * @param context
     * @param data
     * @return
     */
    public static SongUrlInfo.SongUrl.Url selectFileUrl(Context context, SongUrlInfo data) {
        if (data == null
                || data.songUrl == null
                || data.songUrl.urls == null
                || data.songUrl.urls.size() == 0) {
            return null;
        }

        SongUrlInfo.SongUrl.Url result = null;
        SongUrlInfo.SongUrl.Url minBitrateFile = null;
        SongUrlInfo.SongUrl.Url maxBitrateFile = null;
        int minBitrate = Integer.MAX_VALUE;
        int maxBitrate = 0;
        for (SongUrlInfo.SongUrl.Url url : data.songUrl.urls) {
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
        return result;
    }

    public static void savePlayQueue(Context context, List<Music> musics, int playPosition) {

        PlayQueueState playQueueState = new PlayQueueState(musics, playPosition);
        try {
            File saveFile = new File(context.getFilesDir(), "play_queue_state");
            FileOutputStream fos = new FileOutputStream(saveFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(playQueueState);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PlayQueueState loadPlayQueueState(Context context) {
        PlayQueueState playQueueState = null;
        try {
            File saveFile = new File(context.getFilesDir(), "play_queue_state");
            FileInputStream fis = new FileInputStream(saveFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            playQueueState = (PlayQueueState) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return playQueueState;
    }

}
