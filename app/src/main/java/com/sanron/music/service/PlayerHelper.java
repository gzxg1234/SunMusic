package com.sanron.music.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.sanron.music.net.bean.DetailSongInfo;
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
    public static DetailSongInfo.SongUrl.FileInfo selectFileUrl(Context context, List<DetailSongInfo.SongUrl.FileInfo> files) {
        DetailSongInfo.SongUrl.FileInfo result = null;
        if (files != null
                && files.size() > 0) {
            DetailSongInfo.SongUrl.FileInfo minBitrateFile = null;
            DetailSongInfo.SongUrl.FileInfo maxBitrateFile = null;
            int minBitrate = Integer.MAX_VALUE;
            int maxBitrate = 0;
            for (DetailSongInfo.SongUrl.FileInfo fileInfo : files) {
                String fileUrl = fileInfo.getFileLink();
                int fileBitrate = fileInfo.getFileBitrate();
                if (TextUtils.isEmpty(fileUrl)) {
                    continue;
                }

                if (fileBitrate < minBitrate) {
                    minBitrateFile = fileInfo;
                    minBitrate = fileBitrate;
                }

                if (fileBitrate > maxBitrate) {
                    maxBitrateFile = fileInfo;
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
