package com.sanron.music.task;

import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.bean.Music;

import java.util.LinkedList;
import java.util.List;

public class QueryListMemberDataTask extends AsyncTask<Void, Void, List<Music>> {

    private long mListId;

    public QueryListMemberDataTask(long listid) {
        this.mListId = listid;
    }

    @Override
    protected List<Music> doInBackground(Void... params) {
        List<Music> musics = new LinkedList<>();
        DataProvider.Access listMemberAccess = DataProvider.get().newAccess(DBHelper.ListMember.TABLE);
        DataProvider.Access musicAccess = DataProvider.get().newAccess(DBHelper.Music.TABLE);
        Cursor c1 = listMemberAccess.query(new String[]{DBHelper.ListMember.MUSIC_ID},
                DBHelper.ListMember.LIST_ID + "=" + mListId, null);
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