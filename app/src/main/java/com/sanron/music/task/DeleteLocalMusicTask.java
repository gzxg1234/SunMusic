package com.sanron.music.task;

import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.bean.Music;

import java.io.File;
import java.util.List;

/**
 * Created by sanron on 16-4-11.
 */
public class DeleteLocalMusicTask extends AsyncTask<Void, Void, Integer> {

    private List<Music> mDeleteMusics;
    private boolean mIsDeleteFile;

    public DeleteLocalMusicTask(List<Music> deleteMusics, boolean deleteFile) {
        this.mDeleteMusics = deleteMusics;
        this.mIsDeleteFile = deleteFile;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        DataProvider.Access listMemberAccess = DataProvider.instance().getAccess(DBHelper.ListMember.TABLE);
        DataProvider.Access musicAccess = DataProvider.instance().getAccess(DBHelper.Music.TABLE);
        DataProvider.instance().beginTransaction();
        int deleteNum = 0;
        for (Music music : mDeleteMusics) {
            //查找是否有播放列表中引用本地歌曲
            Cursor c = listMemberAccess.rawQuery("select 1 from " + DBHelper.ListMember.TABLE
                    + " where " + DBHelper.ListMember.MUSIC_ID + "=" + music.getId()
                    + " and " + DBHelper.ListMember.LIST_ID + "!=" + DBHelper.List.TYPE_LOCAL_ID);
            if (!c.moveToFirst()) {
                //没有引用，删除歌曲信息
                musicAccess.delete(DBHelper.ID + "=" + music.getId());
            }
            System.out.println(mIsDeleteFile);
            if (mIsDeleteFile
                    && !TextUtils.isEmpty(music.getData())) {
                File file = new File(music.getData());
                file.delete();
            }
            deleteNum += listMemberAccess.delete(DBHelper.ListMember.MUSIC_ID + "=" + music.getId()
                    + " and " + DBHelper.ListMember.LIST_ID + "=" + DBHelper.List.TYPE_LOCAL_ID);
        }
        DataProvider.instance().setTransactionSuccessful();
        DataProvider.instance().endTransaction();
        listMemberAccess.close();
        musicAccess.close();
        return deleteNum;
    }
}
