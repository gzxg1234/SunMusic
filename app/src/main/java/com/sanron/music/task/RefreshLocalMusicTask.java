package com.sanron.music.task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.Music;
import com.sanron.music.utils.AudioTool;

/**
 * 同步媒体库(同步MediaProvider数据)
 * Created by Administrator on 2015/12/21.
 */
public class RefreshLocalMusicTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private DataProvider.Access songInfoAccess;
    private DataProvider.Access artistAccess;
    private DataProvider.Access albumAccess;
    public static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    public static final String[] PROJECTION = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
    };

    public RefreshLocalMusicTask(Context context) {
        songInfoAccess = DataProvider.instance().getAccess(DBHelper.TABLE_MUSIC);
        artistAccess = DataProvider.instance().getAccess(DBHelper.TABLE_ARTIST);
        albumAccess = DataProvider.instance().getAccess(DBHelper.TABLE_ALBUM);
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Cursor cursor = mContext.getContentResolver().query(URI,
                PROJECTION,
                MediaStore.Audio.Media.IS_MUSIC + "=?",
                new String[]{"1"},
                MediaStore.Audio.Media._ID);

        ContentValues values = new ContentValues(2);
        values.put(DBHelper.MUSIC_TYPE, Music.TYPE_LOCAL);
        //删除数据库和MediaProvider不同的数据
        StringBuffer delSql = new StringBuffer("delete from " + DBHelper.TABLE_MUSIC
                + " where " + DBHelper.MUSIC_TYPE + "=" + Music.TYPE_LOCAL + " and (");

        if (cursor.moveToFirst()) {
            do {
                String songid = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                delSql.append(DBHelper.MUSIC_MUSICID).append("!=").append(songid).append(" and ");
                values.put(DBHelper.MUSIC_MUSICID, songid);
                Cursor c2 = songInfoAccess.query(values);
                if (!c2.moveToFirst()) {

                    ContentValues songValues = toContentValues(cursor);

                    //歌手信息
                    String artistName = songValues.getAsString(DBHelper.MUSIC_ARTISTNAME);
                    long artistId = getArtistID(artistName);
                    songValues.put(DBHelper.MUSIC_ARTISTID, artistId);

                    //专辑信息
                    String albumName = songValues.getAsString(DBHelper.MUSIC_ALBUMNAME);
                    long albumId = getAlbumID(artistName, albumName);
                    songValues.put(DBHelper.MUSIC_ALBUMID, albumId);

                    songInfoAccess.insert(songValues);
                }
            } while (cursor.moveToNext());
            delSql.replace(delSql.length() - 5, delSql.length(), ")");
        } else {
            delSql.replace(delSql.length() - 6, delSql.length(), "");
        }
        songInfoAccess.execSQL(delSql.toString());

        songInfoAccess.close();
        albumAccess.close();
        artistAccess.close();
        return null;
    }

    public long getArtistID(String artistName) {
        ContentValues artistValues = new ContentValues();
        artistValues.put(DBHelper.ARTIST_NAME, artistName);
        Cursor cursor = artistAccess.query(artistValues);
        long artistId = -1;
        if (cursor.moveToFirst()) {
            //如果数据库中已有此歌手
            artistId = cursor.getLong(cursor.getColumnIndex(DBHelper.ID));
        } else {
            //没有则插入新歌手信息
            artistId = artistAccess.insert(artistValues);
        }
        return artistId;
    }

    public long getAlbumID(String artistName, String albumName) {
        ContentValues albumValues = new ContentValues();
        albumValues.put(DBHelper.ALBUM_NAME, albumName);
        albumValues.put(DBHelper.ALBUM_ARTIST, artistName);
        Cursor cursor = albumAccess.query(albumValues);
        long albumId;
        if (cursor.moveToFirst()) {
            //如果数据库中已有此专辑
            albumId = cursor.getLong(cursor.getColumnIndex(DBHelper.ID));
        } else {
            //没有则插入新专辑信息
            albumId = albumAccess.insert(albumValues);
        }
        return albumId;
    }


    //转换MediaProvider数据
    private ContentValues toContentValues(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
        String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        int bitrate = AudioTool.readBitrate(path);
        int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
        String letter = "";
        if (title.length() > 0) {
            letter = PinyinHelper.convertToPinyinString(title.substring(0, 1), "", PinyinFormat.WITHOUT_TONE);
            letter = letter.substring(0, 1);
        }

        ContentValues values = new ContentValues();
        values.put(DBHelper.MUSIC_TYPE, Music.TYPE_LOCAL);
        values.put(DBHelper.MUSIC_TITLE, title);
        values.put(DBHelper.MUSIC_MUSICID, id);
        values.put(DBHelper.MUSIC_LETTER, letter);
        values.put(DBHelper.MUSIC_PATH, path);
        values.put(DBHelper.MUSIC_ALBUMNAME, album);
        values.put(DBHelper.MUSIC_DURATION, duration);
        values.put(DBHelper.MUSIC_DISPLAYNAME, displayName);
        values.put(DBHelper.MUSIC_ARTISTNAME, artist);
        values.put(DBHelper.MUSIC_BITRATE, bitrate);
        return values;
    }

}
