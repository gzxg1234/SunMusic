package com.sanron.music.task;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.Music;

import java.io.File;
import java.util.List;

/**
 * 删除本地歌曲
 * Created by Administrator on 2015/12/24.
 */
public abstract class DelLocalMusicTask extends AsyncTask<Void, Void, Integer> {
    private Context mContext;
    private List<Music> mDeleteSongs;
    private boolean mDeleteFile;

    public DelLocalMusicTask(Context context, List<Music> music, boolean deleteFile) {
        this.mContext = context;
        this.mDeleteSongs = music;
        this.mDeleteFile = deleteFile;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_MUSIC);
        int delNum = 0;
        ContentValues values = new ContentValues(1);
        for (int i = 0; i < mDeleteSongs.size(); i++) {
            Music deleteSong = mDeleteSongs.get(i);
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
            delNum += access.delete(values);
        }

        access.close();
        return delNum;
    }
}
