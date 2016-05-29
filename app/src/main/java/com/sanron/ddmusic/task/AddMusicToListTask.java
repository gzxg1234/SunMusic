package com.sanron.ddmusic.task;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.ListMemberHelper;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;

import java.util.List;

/**
 * 添加歌曲至列表
 */
public abstract class AddMusicToListTask extends AsyncTask<Void, Void, Integer> {
    private PlayList mPlayList;
    private List<Music> mAddMusics;
    private Context mContext;

    public AddMusicToListTask(Context context, PlayList playList, List<Music> musics) {
        mPlayList = playList;
        mAddMusics = musics;
        mContext = context.getApplicationContext();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int insertNum = 0;//添加成功数量
        long time = System.currentTimeMillis();
        long listid = mPlayList.getId();
        SQLiteDatabase db = AppDB.get(mContext).getWritableDatabase();
        db.beginTransaction();
        for (int i = 0; i < mAddMusics.size(); i++) {
            Music music = mAddMusics.get(i);
            //检查是否已经存在于列表中
            boolean isExist = ListMemberHelper.isExistByMusicIdAndListId(db, listid, music.getId());
            if (!isExist) {
                long id = ListMemberHelper.addMusicToList(db, music.getId(), listid, time);
                if (id != -1) {
                    insertNum++;
                }
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        if (insertNum > 0) {
            LocalBroadcastManager.getInstance(mContext)
                    .sendBroadcast(new Intent("PlayListUpdate"));
        }
        return insertNum;
    }

}