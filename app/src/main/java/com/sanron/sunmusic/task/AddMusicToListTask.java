package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.Music;

import java.util.List;

/**
 * 添加歌曲至列表
 */
public abstract class AddMusicToListTask extends AsyncTask<Void, Void, Integer[]> {
    private PlayList mPlaylist;
    private List<Music> mAddSongs;

    public AddMusicToListTask(PlayList playList, List<Music> musics) {
        this.mPlaylist = playList;
        this.mAddSongs = musics;
    }

    @Override
    protected Integer[] doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_LISTMUSIC);

        int addNum = 0;//添加成功数量
        int existsNum = 0;//已存在数量
        ContentValues values = new ContentValues(2);
        for(int i=0; i<mAddSongs.size(); i++){
            Music music = mAddSongs.get(i);
            values.put(DBHelper.LISTMUSIC_LISTID,mPlaylist.getId());
            values.put(DBHelper.LISTMUSIC_MUSICID, music.getId());
            if(music.getType() == Music.TYPE_LOCAL) {
                //添加本地歌曲
                //检查是否已经存在于列表中
                Cursor cursor = access.query(values);
                if (cursor.moveToFirst()) {
                    existsNum++;
                }else if(access.insert(values) != -1){
                    addNum ++;
                }
            }
        }

        access.close();
        return new Integer[]{addNum,existsNum};
    }

}