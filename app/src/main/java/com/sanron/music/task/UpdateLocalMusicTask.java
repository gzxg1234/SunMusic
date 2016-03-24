package com.sanron.music.task;

import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.Music;

import java.io.File;
import java.util.List;

/**
 * 更新本地歌曲
 * Created by sanron on 16-3-27.
 */
public class UpdateLocalMusicTask extends AsyncTask<Void, Void, Void> {


    private List<Music> musics;

    public UpdateLocalMusicTask(List<Music> musics) {
        this.musics = musics;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (musics != null) {
            DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.Music.TABLE);
            access.beginTransaction();
            //删除数据库中无效数据，如不存在的文件
            Cursor cursor = access.query(
                    new String[]{DBHelper.ID, DBHelper.Music.DATE_MODIFIED, DBHelper.Music.PATH},
                    DBHelper.Music.TYPE + "=" + DBHelper.Music.TYPE_LOCAL,
                    null);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                long modifiedDate = cursor.getLong(1);
                String path = cursor.getString(2);
                File file = new File(path);
                //文件不存在或者修改时间对不上，移除
                if (!file.exists()
                        || file.lastModified() / 1000 != modifiedDate) {
                    access.delete(DBHelper.ID + "=" + id);
                }
            }

            //插入数据
            for (Music music : musics) {
                //检查是否已经存在,对比文件路径和修改时间
                Cursor c = access.query(new String[]{DBHelper.Music.DATE_MODIFIED},
                        DBHelper.Music.PATH + "=?",
                        new String[]{music.getPath()});
                if (c.moveToFirst()) {
                    long dateModified = c.getLong(0);
                    if (music.getModifiedDate() == dateModified) {
                        continue;
                    }
                }
                access.insert(null, music.toContentValues());
            }
            access.setTransactionSuccessful();
            access.endTransaction();
            access.close();
        }
        return null;
    }
}
