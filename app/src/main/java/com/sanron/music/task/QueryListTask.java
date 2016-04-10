package com.sanron.music.task;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.CursorAdapter;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.PlayList;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sanron on 16-4-8.
 */
public class QueryListTask extends AsyncTask<Void, Void, List<PlayList>> {

    @Override
    protected List<PlayList> doInBackground(Void... params) {
        List<PlayList> playLists = new LinkedList<>();
        DataProvider.Access listAccess = DataProvider.instance().getAccess(DBHelper.List.TABLE);
        DataProvider.Access listMemberAccess = DataProvider.instance().getAccess(DBHelper.ListMember.TABLE);

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
