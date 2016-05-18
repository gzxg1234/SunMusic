package com.sanron.music.playback;

import android.content.Context;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.sanron.music.AppContext;
import com.sanron.music.api.bean.SongUrlInfo;

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
}
