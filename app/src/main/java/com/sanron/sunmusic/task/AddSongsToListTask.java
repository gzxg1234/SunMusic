package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;

import java.util.List;

/**
 * 添加歌曲至列表
 */
public abstract class AddSongsToListTask extends AsyncTask<Void, Void, Integer[]> {
    private PlayList mPlaylist;
    private List<SongInfo> mAddSongs;

    public AddSongsToListTask(PlayList playList,List<SongInfo> songInfos) {
        this.mPlaylist = playList;
        this.mAddSongs = songInfos;
    }

    @Override
    protected Integer[] doInBackground(Void... params) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.TABLE_LISTSONGS);

        int addNum = 0;//添加成功数量
        int existsNum = 0;//已存在数量
        ContentValues values = new ContentValues(2);
        for(int i=0; i<mAddSongs.size(); i++){
            SongInfo songInfo = mAddSongs.get(i);
            values.put(DBHelper.LISTSONGS_LISTID,mPlaylist.getId());
            values.put(DBHelper.LISTSONGS_SONGID,songInfo.getId());
            if(songInfo.getType() == SongInfo.TYPE_LOCAL) {
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