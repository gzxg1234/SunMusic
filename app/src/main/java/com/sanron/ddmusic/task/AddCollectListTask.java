package com.sanron.ddmusic.task;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.sanron.ddmusic.api.bean.Song;
import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.ListMemberHelper;
import com.sanron.ddmusic.db.MusicHelper;
import com.sanron.ddmusic.db.PlayListHelper;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;

import java.util.List;

/**
 * 添加收藏歌单
 * Created by sanron on 16-4-8.
 */
public class AddCollectListTask extends AsyncTask<Void, Void, Integer> {

    public static final int FAILED = 0;
    public static final int SUCCESS = 1;

    private List<Song> mSongs;
    private String mTitle;
    private String mListId;
    private String mPic;
    private Context mContext;

    public AddCollectListTask(Context context, List<Song> songs, String title, String listId, String pic) {
        mSongs = songs;
        mTitle = title;
        mListId = listId;
        mPic = pic;
        mContext = context.getApplicationContext();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        SQLiteDatabase db = AppDB.get(mContext).getWritableDatabase();
        db.beginTransaction();
        long time = System.currentTimeMillis();
        try {
            PlayList playList = new PlayList();
            playList.setIcon(mPic);
            playList.setTitle(mTitle);
            playList.setListId(mListId);
            playList.setType(PlayList.TYPE_COLLECTION);
            playList.setAddTime(System.currentTimeMillis());

            //插入歌单信息
            long listid = PlayListHelper.addPlaylist(db, playList);
            if (listid == -1) {
                return FAILED;
            }

            //插入歌单歌曲信息
            for (Song song : mSongs) {
                String songid = song.songId;

                long musicid;
                //是否存有歌曲信息
                Music m = MusicHelper.getMusicBySongId(db, songid);
                if (m == null) {
                    Music m2 = song.toMusic();
                    String titleKey = (song.title == null ?
                            null : PinyinHelper.convertToPinyinString(song.title, "", PinyinFormat.WITHOUT_TONE));
                    m2.setTitleKey(titleKey);
                    musicid = MusicHelper.addMusic(db, m2);
                } else {
                    musicid = m.getId();
                }
                if (musicid == -1) {
                    return FAILED;
                }

                long insertId = ListMemberHelper.addMusicToList(db, musicid, listid, time);
                if (insertId == -1) {
                    return FAILED;
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            return FAILED;
        } finally {
            db.endTransaction();
        }

        LocalBroadcastManager.getInstance(mContext)
                .sendBroadcast(new Intent("PlayListUpdate"));
        return SUCCESS;
    }
}
