package com.sanron.music.task;

import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.Music;

import java.util.LinkedList;
import java.util.List;

public class QueryListMemberDatasTask extends AsyncTask<Void, Void, List<Music>> {

    private long listid;

    public QueryListMemberDatasTask(long listid) {
        this.listid = listid;
    }

    @Override
    protected List<Music> doInBackground(Void... params) {
        List<Music> musics = new LinkedList<>();
        DataProvider.Access listMemberAccess = DataProvider.instance().getAccess(DBHelper.ListMember.TABLE);
        DataProvider.Access musicAccess = DataProvider.instance().getAccess(DBHelper.Music.TABLE);
        Cursor c1 = listMemberAccess.query(new String[]{DBHelper.ListMember.MUSIC_ID},
                DBHelper.ListMember.LIST_ID + "=" + listid, null);
        StringBuilder sb = new StringBuilder();
        while (c1.moveToNext()) {
            long musicId = c1.getLong(0);
            sb.append(musicId).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        Cursor c2 = musicAccess.query(null,
                DBHelper.ID + " in(" + sb.toString() + ")", null, null, null, DBHelper.Music.TITLE_KEY, null);
        while (c2.moveToNext()) {
            musics.add(Music.fromCursor(c2));
        }
        musicAccess.close();
        listMemberAccess.close();
        return musics;
    }
}