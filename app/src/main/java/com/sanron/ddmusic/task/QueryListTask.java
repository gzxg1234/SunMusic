package com.sanron.ddmusic.task;

import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.ddmusic.db.DBHelper;
import com.sanron.ddmusic.db.DataProvider;
import com.sanron.ddmusic.db.bean.PlayList;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sanron on 16-4-8.
 */
public class QueryListTask extends AsyncTask<Void, Void, List<PlayList>> {

    @Override
    protected List<PlayList> doInBackground(Void... params) {
        List<PlayList> playLists = new LinkedList<>();
        DataProvider.Access listAccess = DataProvider.get().newAccess(DBHelper.List.TABLE);
        DataProvider.Access listMemberAccess = DataProvider.get().newAccess(DBHelper.ListMember.TABLE);

        Cursor c1 = listAccess.query(null,
                DBHelper.List.TYPE + "=" + DBHelper.List.TYPE_FAVORITE
                        + " or " + DBHelper.List.TYPE + "=" + DBHelper.List.TYPE_USER
                        + " or " + DBHelper.List.TYPE + "=" + DBHelper.List.TYPE_COLLECTION,
                null, null, null, DBHelper.List.ADD_TIME, null);

        while (c1.moveToNext()) {
            PlayList playList = PlayList.fromCursor(c1);
            Cursor c2 = listMemberAccess.rawQuery("select count(1) from " + DBHelper.ListMember.TABLE
                    + " where " + DBHelper.ListMember.LIST_ID + "=" + playList.getId());
            if (c2.moveToFirst()) {
                playList.setSongNum(c2.getInt(0));
            }
            playLists.add(playList);
        }
        listAccess.close();
        listMemberAccess.close();
        return playLists;
    }
}
