package com.sanron.sunmusic.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sanron.sunmusic.model.PlayList;

/**
 * Created by Administrator on 2015/12/20.
 */
public class DBHelper extends SQLiteOpenHelper {


    public static final String DBNAME = "SunMusicDB.db";
    public static final int DBVERSION = 1;


    public static final String ID = "_id";
    /**
     * 音乐
     */
    public static final String TABLE_SONG = "songinfo";
    public static final String SONG_TYPE = "type";
    public static final String SONG_DISPLAYNAME = "display_name";
    public static final String SONG_TITLE = "title";
    public static final String SONG_ALBUM = "album";
    public static final String SONG_ARTIST = "artist";
    public static final String SONG_DURATION = "duration";
    public static final String SONG_PATH = "path";
    public static final String SONG_SONGID = "songid";
    public static final String SONG_LETTER = "title_letter";
    public static final String SONG_BITRATE = "bitrate";

    /**
     * 歌词
     */
    public static final String TABLE_LYRIC = "lyric";
    public static final String LYRIC_TITLE = "title";
    public static final String LYRIC_ARTIST = "artist";
    public static final String LYRIC_PATH = "path";

    /**
     * 播放列表
     */
    public static final String TABLE_PLAYLIST = "playlist";
    public static final String PLAYLIST_NAME = "name";
    public static final String PLAYLIST_TYPE = "type";
    public static final String PLAYLIST_NUM = "song_num";


    /**
     * 播放列表和音乐关系表
     */
    public static final String TABLE_LISTSONGS = "playlist_songs";
    public static final String LISTSONGS_SONGID = "song_id";
    public static final String LISTSONGS_LISTID = "list_id";
    public static final String LISTSONGS_ADDTIME = "add_time";


    public DBHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableSongInfo(db);
        createTablePlayList(db);
        createTableLyric(db);
        createTableListSongs(db);
        createTrigger(db);
    }

    public void createTableSongInfo(SQLiteDatabase db){
        String[] columns = new String[]{
                SONG_TYPE,
                SONG_DISPLAYNAME,
                SONG_TITLE,
                SONG_ALBUM,
                SONG_ARTIST,
                SONG_DURATION,
                SONG_PATH,
                SONG_SONGID,
                SONG_LETTER,
                SONG_BITRATE
        };
        String[] columnTypes = new String[]{"integer", "text", "text", "text", "text", "integer",
                "text", "text", "text","integer"};
        String sql = buildCreateSql(TABLE_SONG, columns, columnTypes);
        db.execSQL(sql);
    }

    public void createTablePlayList(SQLiteDatabase db){
        String[] columns =  new String[]{
                PLAYLIST_TYPE,
                PLAYLIST_NAME,
                PLAYLIST_NUM,
        };
        String[] columnTypes = new String[]{"integer", "text", "integer default 0"};
        String sql = buildCreateSql(TABLE_PLAYLIST, columns, columnTypes);
        db.execSQL(sql);

        //创建我喜欢，最近播放两个列表
        sql = "insert into "+TABLE_PLAYLIST+"("+PLAYLIST_TYPE+","+PLAYLIST_NAME+") " +
                "values("+PlayList.TYPE_FAVORITE+",'我喜欢')";
        db.execSQL(sql);
        sql = "insert into "+TABLE_PLAYLIST+"("+PLAYLIST_TYPE+","+PLAYLIST_NAME+") " +
                "values("+PlayList.TYPE_RECENT+",'最近播放')";
        db.execSQL(sql);
    }

    public void createTableListSongs(SQLiteDatabase db){
        String[] columns =  new String[]{
                LISTSONGS_LISTID,
                LISTSONGS_SONGID,
        };
        String[] columnTypes = new String[]{"integer", "integer"};
        String sql = buildCreateSql(TABLE_LISTSONGS, columns, columnTypes);
        db.execSQL(sql);

    }

    public void createTableLyric(SQLiteDatabase db){
        String[] columns =  new String[]{
                LYRIC_ARTIST,
                LYRIC_TITLE,
                LYRIC_PATH,
        };
        String[] columnTypes = new String[]{"text", "text", "text", "text"};
        String sql = buildCreateSql(TABLE_LYRIC, columns, columnTypes);
        db.execSQL(sql);
    }

    private void createTrigger(SQLiteDatabase db){
        String trig1 = "create trigger listsongsInsert after insert on "+DBHelper.TABLE_LISTSONGS
                +" begin"
                +" update "+DBHelper.TABLE_PLAYLIST+" set "+DBHelper.PLAYLIST_NUM+"="+DBHelper.PLAYLIST_NUM+"+1 where "+DBHelper.ID+"=new."+DBHelper.LISTSONGS_LISTID+";"
                +" end;";
        String trig2 = "create trigger listsongsDelete after delete on "+DBHelper.TABLE_LISTSONGS
                +" begin"
                +" update "+DBHelper.TABLE_PLAYLIST+" set "+DBHelper.PLAYLIST_NUM+"="+DBHelper.PLAYLIST_NUM+"-1 where "+DBHelper.ID+"=old."+DBHelper.LISTSONGS_LISTID+";"
                +" end;";
        String trig3 = "create trigger songinfoDelete after delete on "+DBHelper.TABLE_SONG
                +" begin"
                +" delete from "+DBHelper.TABLE_LISTSONGS+" where "+DBHelper.LISTSONGS_SONGID+"=old."+DBHelper.ID+";"
                +" end;";
        String trig4 = "create trigger playlistDelete after delete on "+DBHelper.TABLE_PLAYLIST
                +" begin"
                +" delete from "+DBHelper.TABLE_LISTSONGS+" where "+DBHelper.LISTSONGS_LISTID+"=old."+DBHelper.ID+";"
                +" end;";
        db.execSQL(trig1);
        db.execSQL(trig2);
        db.execSQL(trig3);
        db.execSQL(trig4);
    }

    private String buildCreateSql(String table, String[] columnNames, String[] type) {
        StringBuilder sb = new StringBuilder("create table if not exists ").append(table).append("(");
        sb.append(ID).append(" integer primary key autoincrement,");
        for (int i = 0; i < columnNames.length; i++) {
            sb.append(columnNames[i]).append(" ").append(type[i]).append(",");
        }
        sb.replace(sb.length() - 1, sb.length(), ")");
        return sb.toString();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
