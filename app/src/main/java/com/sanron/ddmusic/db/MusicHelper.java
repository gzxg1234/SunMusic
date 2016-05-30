package com.sanron.ddmusic.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.sanron.ddmusic.db.bean.Music;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sanron on 16-5-29.
 */
public class MusicHelper {

    public interface Columns {
        String TABLE = "music_info";
        String DISPLAY = "display_name";
        String DATA = "data";
        String DATE_MODIFIED = "date_modified";
        String TITLE = "title";
        String TITLE_KEY = "title_key";
        String ALBUM = "album";
        String ARTIST = "artist";
        String DURATION = "duration";
        String SONG_ID = "song_id";
        String BITRATE = "bitrate";
        String TYPE = "type";
    }

    public static void onCreate(SQLiteDatabase db) {
        Map<String, String> columnTypes = new LinkedHashMap<>();
        columnTypes.put(Columns.DISPLAY, "text");
        columnTypes.put(Columns.DATA, "text");
        columnTypes.put(Columns.DATE_MODIFIED, "integer");
        columnTypes.put(Columns.TITLE, "text");
        columnTypes.put(Columns.TITLE_KEY, "text");
        columnTypes.put(Columns.ALBUM, "text");
        columnTypes.put(Columns.ARTIST, "text");
        columnTypes.put(Columns.DURATION, "integer");
        columnTypes.put(Columns.SONG_ID, "text");
        columnTypes.put(Columns.BITRATE, "integer");
        columnTypes.put(Columns.TYPE, "integer default 0");
        String sql = BaseHelper.buildCreateSql(Columns.TABLE, columnTypes);
        db.execSQL(sql);
        String index1 = BaseHelper.createIndexSql("path_index", Columns.TABLE, Columns.DATA);
        db.execSQL(index1);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static Music getMusicById(SQLiteDatabase db, long id) {
        Music music = null;
        Cursor cursor = db.query(Columns.TABLE, null, BaseHelper.ID + "=" + id, null, null, null, null);
        if (cursor.moveToFirst()) {
            music = Music.fromCursor(cursor);
        }
        cursor.close();
        return music;
    }

    public static List<Music> getMusicByType(SQLiteDatabase db, int type) {
        List<Music> musics = new LinkedList<>();
        Cursor cursor = db.query(Columns.TABLE, null, Columns.TYPE + "=" + type, null, null, null, null);
        while (cursor.moveToNext()) {
            musics.add(Music.fromCursor(cursor));
        }
        cursor.close();
        return musics;
    }

    public static int deleteById(SQLiteDatabase db, long id) {
        return db.delete(Columns.TABLE, BaseHelper.ID + "=" + id, null);
    }

    public static Music getMusicBySongId(SQLiteDatabase db, String songid) {
        Music music = null;
        Cursor cursor = db.query(Columns.TABLE, null,
                Columns.SONG_ID + "=?",
                new String[]{songid}, null, null, null);
        if (cursor.moveToFirst()) {
            music = Music.fromCursor(cursor);
        }
        cursor.close();
        return music;
    }

    public static long addMusic(SQLiteDatabase db, Music music) {
        if (music.getTitleKey() == null) {
            String title = music.getTitle();
            String titleKey = (title == null ?
                    null : PinyinHelper.convertToPinyinString(title, "", PinyinFormat.WITHOUT_TONE));
            music.setTitleKey(titleKey);
        }
        return db.insert(Columns.TABLE, null, music.toContentValues());
    }

}
