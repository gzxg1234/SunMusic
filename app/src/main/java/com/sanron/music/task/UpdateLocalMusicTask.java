package com.sanron.music.task;

import android.content.ContentValues;
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
            DataProvider.Access musicAccess = DataProvider.instance().getAccess(DBHelper.Music.TABLE);
            DataProvider.Access listAccess = DataProvider.instance().getAccess(DBHelper.List.TABLE);
            DataProvider.Access listMemberAccess = DataProvider.instance().getAccess(DBHelper.ListMember.TABLE);
            DataProvider.instance().beginTransaction();

            //插入数据
            for (Music music : musics) {
                //检查是否已经存在,对比文件路径和修改时间
                Cursor c = musicAccess.query(new String[]{DBHelper.Music.DATE_MODIFIED},
                        DBHelper.Music.DATA + "=?",
                        new String[]{music.getData()});
                if (c.moveToFirst()) {
                    long dateModified = c.getLong(0);
                    if (music.getModifiedDate() == dateModified) {
                        continue;
                    }
                }
                musicAccess.insert(null, music.toContentValues());
            }


            listMemberAccess.delete(DBHelper.ListMember.LIST_ID + "=?",
                    String.valueOf(DBHelper.List.TYPE_LOCAL_ID));

            Cursor c2 = musicAccess.query(new String[]{DBHelper.ID + "", DBHelper.Music.DATA + ""},
                    DBHelper.Music.DATA + "!=\"\"", null);
            ContentValues values = new ContentValues(2);
            while (c2.moveToNext()) {
                long musicid = c2.getLong(0);
                String dataPath = c2.getString(1);
                File file = new File(dataPath);
                if (file.exists()) {
                    values.put(DBHelper.ListMember.LIST_ID, DBHelper.List.TYPE_LOCAL_ID);
                    values.put(DBHelper.ListMember.MUSIC_ID, musicid);
                    listMemberAccess.insert(null, values);
                }
            }
            DataProvider.instance().setTransactionSuccessful();
            DataProvider.instance().endTransaction();
            musicAccess.close();
            listAccess.close();
            listMemberAccess.close();
        }
        return null;
    }

}
