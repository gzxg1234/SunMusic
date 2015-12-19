package com.sanron.sunmusic.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.StringBuilderPrinter;

/**
 * Created by Administrator on 2015/12/20.
 */
public class DBHelper extends SQLiteOpenHelper {


    public static final String DBNAME = "SunMusicDB";
    public static final int DBVERSION = 1;


    public static final String ID = "_id";
    /**
     * 音乐
     */
    public static final String SONG_TABLE = "songinfo";
    public static final String SONG_TYPE = "type";
    public static final String SONG_DISPLAYNAME = "display_name";
    public static final String SONG_TITLE = "title";
    public static final String SONG_ALBUM = "album";
    public static final String SONG_ARTIST = "artist";
    public static final String SONG_DURATION = "duration";
    public static final String SONG_PATH = "path";
    public static final String SONG_SONGID = "songid";
    public static final String SONG_PINYIN = "pinyin";


    /**
     * 播放列表
     */
    public static final String PLAYLIST_TABLE = "playlist";
    public static final String PLAYLIST_NAME = "name";
    public static final String PLAYLIST_TYPE = "type";
    public static final String PLAYLIST_COUNT = "song_num";
    public static final String PLAYLIST_CREATE_TIME = "create_time";


    /**
     * 播放列表和音乐关系表
     */
    public static final String LISTSONGS_TABLE = "playlist_songs";
    public static final String LISTSONGS_SONGID = "song_id";
    public static final String LISTSONGS_LISTID = "list_id";
    public static final String LISTSONGS_ADDTIME = "add_time";


    public DBHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String songInfoSql;
        String playListSql;
        String listSongsSql;

        String[] columns;
        String[] columnTypes;
        columns = new String[]{
                SONG_TYPE,
                SONG_DISPLAYNAME,
                SONG_TITLE,
                SONG_ALBUM,
                SONG_ARTIST,
                SONG_DURATION,
                SONG_PATH,
                SONG_SONGID,
                SONG_PINYIN
        };
        columnTypes = new String[]{"INTEGER", "TEXT", "TEXT", "TEXT", "TEXT", "INTEGER",
                "TEXT", "INTEGER", "TEXT"};
        songInfoSql = buildCreateSql(SONG_TABLE, columns, columnTypes);


        columns = new String[]{
                PLAYLIST_TYPE,
                PLAYLIST_NAME,
                PLAYLIST_COUNT,
                PLAYLIST_CREATE_TIME,
        };
        columnTypes = new String[]{"INTEGER", "TEXT", "INTEGER", "INTEGER"};
        playListSql = buildCreateSql(PLAYLIST_TABLE, columns, columnTypes);


        columns = new String[]{
                LISTSONGS_ADDTIME,
                LISTSONGS_LISTID,
                LISTSONGS_SONGID,
        };
        columnTypes = new String[]{"INTEGER", "INTEGER", "INTEGER", "INTEGER"};
        listSongsSql = buildCreateSql(LISTSONGS_TABLE, columns, columnTypes);


        db.execSQL(songInfoSql);
        db.execSQL(playListSql);
        db.execSQL(listSongsSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String buildCreateSql(String table, String[] columnNames, String[] type) {
        StringBuilder sb = new StringBuilder("create table if not exists ").append(table).append("(");
        sb.append("_id integer primary key autoincrement,");
        for (int i = 0; i < columnNames.length; i++) {
            sb.append(columnNames[i]).append(" ").append(type[i]).append(",");
        }
        sb.replace(sb.length() - 1, sb.length(), ")");
        return sb.toString();
    }
}
