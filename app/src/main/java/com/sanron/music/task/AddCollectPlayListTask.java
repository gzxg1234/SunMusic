package com.sanron.music.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.bean.Music;
import com.sanron.music.db.bean.PlayList;
import com.sanron.music.net.bean.Song;

import java.util.List;

/**
 * 添加收藏歌单
 * Created by sanron on 16-4-8.
 */
public class AddCollectPlayListTask extends AsyncTask<Void, Void, Integer> {

    public static final int FAILED = 0;
    public static final int SUCCESS = 1;

    private List<Song> mSongs;
    private String mTitle;
    private String mListId;
    private String mPic;

    public AddCollectPlayListTask(List<Song> songs, String title, String listId, String pic) {
        mSongs = songs;
        mTitle = title;
        mListId = listId;
        mPic = pic;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        DataProvider.Access listAccess = DataProvider.instance().getAccess(DBHelper.List.TABLE);
        DataProvider.Access listMemberAccess = DataProvider.instance().getAccess(DBHelper.ListMember.TABLE);
        DataProvider.Access musicAccess = DataProvider.instance().getAccess(DBHelper.Music.TABLE);
        DataProvider.instance().beginTransaction();


        try {
            PlayList playList = new PlayList();
            playList.setIcon(mPic);
            playList.setTitle(mTitle);
            playList.setListId(mListId);
            playList.setType(DBHelper.List.TYPE_COLLECTION);
            playList.setAddTime(System.currentTimeMillis());

            //插入歌单信息
            long listid = listAccess.insert(null, playList.toContentValues());
            if (listid == -1) {
                return FAILED;
            }

            //插入歌单歌曲信息
            for (Song song : mSongs) {
                Music music = song.toMusic();
                String songid = song.songId;

                long musicid;
                //是否存有歌曲信息
                Cursor c = musicAccess.query(new String[]{DBHelper.ID},
                        DBHelper.Music.SONG_ID + "=?",
                        new String[]{String.valueOf(songid)});
                if (c.moveToFirst()) {
                    musicid = c.getLong(0);
                } else {
                    musicid = musicAccess.insert(null, music.toContentValues());
                }
                if (musicid == -1) {
                    return FAILED;
                }

                ContentValues values = new ContentValues(2);
                values.put(DBHelper.ListMember.LIST_ID, listid);
                values.put(DBHelper.ListMember.MUSIC_ID, musicid);
                long insertId = listMemberAccess.insert(null, values);
                if (insertId == -1) {
                    return FAILED;
                }
            }
            DataProvider.instance().setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            listAccess.close();
            listMemberAccess.close();
            musicAccess.close();
            DataProvider.instance().endTransaction();
        }
        return SUCCESS;
    }
}
