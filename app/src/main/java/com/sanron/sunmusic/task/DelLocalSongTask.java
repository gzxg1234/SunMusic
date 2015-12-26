package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.ListSongsProvider;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.db.SongInfoProvider;
import com.sanron.sunmusic.fragments.MySongFrag.PlayListSongsFrag;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

import java.io.File;
import java.util.List;

/**
 * 删除本地歌曲
 * Created by Administrator on 2015/12/24.
 */
public abstract class DelLocalSongTask extends AsyncTask<Void, Void, Integer> {
    private Context mContext;
    private List<SongInfo> mDeleteSongs;
    private boolean mDeleteFile;

    public DelLocalSongTask(Context context, List<SongInfo> songInfo, boolean deleteFile) {
        this.mContext = context;
        this.mDeleteSongs = songInfo;
        this.mDeleteFile = deleteFile;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        SongInfoProvider songInfoProvider = SongInfoProvider.instance();

        int delNum = 0;
        ContentValues values = new ContentValues(1);
        for (int i = 0; i < mDeleteSongs.size(); i++) {
            SongInfo deleteSong = mDeleteSongs.get(i);
            if (mDeleteFile) {
                //同步删除MediaProvider数据
                int num = mContext.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Audio.Media._ID + "=?",
                        new String[]{deleteSong.getSongId()});
                if (num > 0) {
                    //删除本地文件
                    File file = new File(deleteSong.getPath());
                    if (file.exists()) {
                         file.delete();
                    }
                }
            }
            values.put(DBHelper.ID,deleteSong.getId());
            delNum += songInfoProvider.delete(values);
        }

        if(delNum > 0) {
            PlayListProvider.instance().notifyDataChanged();
        }
        songInfoProvider.notifyObservers();
        return delNum;
    }
}
