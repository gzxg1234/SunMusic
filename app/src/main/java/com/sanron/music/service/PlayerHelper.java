package com.sanron.music.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.sanron.music.net.bean.SongUrlInfo;
import com.sanron.music.utils.NetTool;

import java.util.List;

/**
 * Created by sanron on 16-4-7.
 */
public class PlayerHelper {

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

            int netType = NetTool.checkNet(context);
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
