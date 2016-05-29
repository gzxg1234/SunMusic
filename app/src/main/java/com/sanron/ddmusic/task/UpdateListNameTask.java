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
 * 修改播放列表名
 * Created by Administrator on 2015/12/24.
 */
public abstract class UpdateListNameTask extends AsyncTask<Void, Void, Integer> {
    public static final int EXISTS = -1;
    public static final int FAILED = 0;
    public static final int SUCCESS = 1;

    private Context mContext;
    private PlayList updatePlayList;

    public UpdateListNameTask(Context context, PlayList playList) {
        mContext = context.getApplicationContext();
        updatePlayList = playList;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int result = FAILED;
        SQLiteDatabase db = AppDB.get(mContext).getWritableDatabase();
        //检查列表名是否已存在
        boolean isExistName = PlayListHelper.isExistByName(db, updatePlayList.getTitle());
        if (isExistName) {
            //列表名已存在
            result = EXISTS;
        } else {
            int num = PlayListHelper.updateName(db, updatePlayList.getId(), updatePlayList.getTitle());
            if (num > 0) {
                result = SUCCESS;
                LocalBroadcastManager.getInstance(mContext)
                        .sendBroadcast(new Intent("PlayListUpdate"));
            }
        }

        return result;
    }
}
