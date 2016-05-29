package com.sanron.ddmusic.task;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.ListMemberHelper;
import com.sanron.ddmusic.db.MusicHelper;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;

import java.io.File;
import java.util.List;

/**
 * Created by sanron on 16-4-11.
 */
public class DeleteLocalMusicTask extends AsyncTask<Void, Void, Integer> {

    private Context mContext;
    private List<Music> mDeleteMusics;
    private boolean mIsDeleteFile;

    public DeleteLocalMusicTask(Context context, List<Music> deleteMusics, boolean deleteFile) {
        mContext = context.getApplicationContext();
        mDeleteMusics = deleteMusics;
        mIsDeleteFile = deleteFile;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        SQLiteDatabase db = AppDB.get(mContext).getWritableDatabase();
        db.beginTransaction();
        int deleteNum = 0;
        for (Music music : mDeleteMusics) {
            //查找是否有播放列表中引用本地歌曲
            boolean hasRef = ListMemberHelper.isExistByMusicIdAndListId(db, PlayList.TYPE_LOCAL_ID, music.getId());
            if (!hasRef) {
                //没有引用，删除歌曲信息
                MusicHelper.deleteById(db, music.getId());
            }

            if (mIsDeleteFile
                    && !TextUtils.isEmpty(music.getData())) {
                File file = new File(music.getData());
                file.delete();
            }

            deleteNum += ListMemberHelper.delete(db, PlayList.TYPE_LOCAL_ID, music.getId());
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        if (deleteNum > 0) {
            LocalBroadcastManager.getInstance(mContext)
                    .sendBroadcast(new Intent("LocalMusicUpdate"));
        }
        return deleteNum;
    }
}
