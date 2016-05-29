package com.sanron.ddmusic.task;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.PlayListHelper;
import com.sanron.ddmusic.db.bean.PlayList;

/**
 * 新建列表
 * Created by Administrator on 2015/12/24.
 */
public abstract class AddPlayListTask extends AsyncTask<Void, Void, Integer> {

    public static final int EXISTS = -1;
    public static final int FAILED = 0;
    public static final int SUCCESS = 1;

    private Context mContext;
    private String mName;

    public AddPlayListTask(Context context, String name) {
        mContext = context.getApplicationContext();
        mName = name;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int result = FAILED;
        SQLiteDatabase db = AppDB.get(mContext).getWritableDatabase();
        //检查是否重名
        boolean isExists = PlayListHelper.isExistByName(db, mName);
        if (isExists) {
            result = EXISTS;
        } else {
            PlayList playList = new PlayList();
            playList.setType(PlayList.TYPE_USER);
            playList.setTitle(mName);
            playList.setAddTime(System.currentTimeMillis());
            long id = PlayListHelper.addPlaylist(db, playList);
            if (id != -1) {
                result = SUCCESS;
                LocalBroadcastManager.getInstance(mContext)
                        .sendBroadcast(new Intent("PlayListUpdate"));
            }
        }
        return result;
    }

}
