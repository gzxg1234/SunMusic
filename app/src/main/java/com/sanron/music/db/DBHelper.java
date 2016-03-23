package com.sanron.music.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

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
    public static final String TABLE_MUSIC = "music_info";
    public static final String MUSIC_DISPLAY = "display_name";
    public static final String MUSIC_TYPE = "type";
    public static final String MUSIC_TITLE = "title";
    public static final String MUSIC_TITLE_KEY = "title_key";
    public static final String MUSIC_PATH = "path";
    public static final String MUSIC_ALBUM = "album";
    public static final String MUSIC_ALBUM_KEY = "album_key";
    public static final String MUSIC_ARTIST = "artist";
    public static final String MUSIC_ARTIST_KEY = "artist_key";
    public static final String MUSIC_DURATION = "duration";
    public static final String MUSIC_SONGID = "song_id";
    public static final String MUSIC_BITRATE = "bitrate";
    public static final String MUSIC_PIC = "picture";
    public static final String MUSIC_LYRIC = "lyric";

    /**
     * 专辑
     */
    public static final String TABLE_ALBUM = "album";
    public static final String ALBUM_NAME = "name";
    public static final String ALBUM_ARTIST = "artist";
    public static final String ALBUM_PIC = "picture";

    /**
     * 歌手
     */
    public static final String TABLE_ARTIST = "artist";
    public static final String ARTIST_NAME = "name";
    public static final String ARTIST_PIC = "picture";


    /**
     * 播放列表
     */
    public static final String TABLE_PLAYLIST = "playlist";
    public static final String PLAYLIST_NAME = "name";
    public static final String PLAYLIST_TYPE = "type";

    /**
     * 播放列表和音乐关系表
     */
    public static final String TABLE_LISTMUSIC = "list_music_map";
    public static final String LISTMUSIC_MUSICID = "music_id";
    public static final String LISTMUSIC_LISTID = "list_id";


    public DBHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableMusicInfo(db);
        createTablePlayList(db);
        createTableListMusic(db);
        createTableArtist(db);
        createTableAlbum(db);
        createTrigger(db);
    }

    public void createTableMusicInfo(SQLiteDatabase db){
        Map<String,String>  columnTypes = new HashMap<>();
        columnTypes.put(MUSIC_TYPE,"integer");
        columnTypes.put(MUSIC_DISPLAY,"text");
        columnTypes.put(MUSIC_TITLE,"text");
        columnTypes.put(MUSIC_TITLE_KEY,"text");
        columnTypes.put(MUSIC_ALBUM,"text");
        columnTypes.put(MUSIC_ALBUM_KEY,"text");
        columnTypes.put(MUSIC_ARTIST,"text");
        columnTypes.put(MUSIC_ARTIST_KEY,"text");
        columnTypes.put(MUSIC_PATH,"text");
        columnTypes.put(MUSIC_DURATION,"integer");
        columnTypes.put(MUSIC_SONGID,"text");
        columnTypes.put(MUSIC_BITRATE,"integer");
        columnTypes.put(MUSIC_LYRIC,"text");
        columnTypes.put(MUSIC_PIC,"text");
        String sql = buildCreateSql(TABLE_MUSIC, columnTypes);
        String index1 = createIndexSql("path_index",TABLE_MUSIC,MUSIC_PATH);
        String index2 = createIndexSql("title_key_index",TABLE_MUSIC,MUSIC_TITLE_KEY);
        String index3 = createIndexSql("album_key_index",TABLE_MUSIC,MUSIC_ALBUM_KEY);
        String index4 = createIndexSql("artist_key_index",TABLE_MUSIC,MUSIC_ARTIST_KEY);
        String index5 = createIndexSql("type_index",TABLE_MUSIC,MUSIC_TYPE);
        db.execSQL(sql);
        db.execSQL(index1);
        db.execSQL(index2);
        db.execSQL(index3);
        db.execSQL(index4);
        db.execSQL(index5);
    }

    public String createIndexSql(String indexname,String tablename,String column){
        StringBuffer sb = new StringBuffer();
        return sb.append("create index ").append(indexname)
                .append(" on ").append(tablename)
                .append("(").append(column).append(")")
                .toString();
    }


    public void createTablePlayList(SQLiteDatabase db){
        Map<String,String>  columnTypes = new HashMap<>();
        columnTypes.put(PLAYLIST_TYPE,"integer");
        columnTypes.put(PLAYLIST_NAME,"text");
        String sql = buildCreateSql(TABLE_PLAYLIST, columnTypes);
        db.execSQL(sql);

        //创建我喜欢，最近播放两个列表
        sql = "insert into "+TABLE_PLAYLIST+"("+PLAYLIST_TYPE+","+PLAYLIST_NAME+") " +
                "values("+PlayList.TYPE_FAVORITE+",'我喜欢')";
        db.execSQL(sql);
        sql = "insert into "+TABLE_PLAYLIST+"("+PLAYLIST_TYPE+","+PLAYLIST_NAME+") " +
                "values("+PlayList.TYPE_RECENT+",'最近播放')";
        db.execSQL(sql);
    }

    public void createTableListMusic(SQLiteDatabase db){
        Map<String,String>  columnTypes = new HashMap<>();
        columnTypes.put(LISTMUSIC_LISTID,"integer");
        columnTypes.put(LISTMUSIC_MUSICID,"integer");
        String sql = buildCreateSql(TABLE_LISTMUSIC, columnTypes);
        db.execSQL(sql);

    }

    public void createTableArtist(SQLiteDatabase db){
        Map<String,String>  columns = new HashMap<>();
        columns.put(ARTIST_NAME,"integer");
        columns.put(ARTIST_PIC,"integer");
        String sql = buildCreateSql(TABLE_ARTIST, columns);
        db.execSQL(sql);
    }

    public void createTableAlbum(SQLiteDatabase db){
        Map<String,String>  columns = new HashMap<>();
        columns.put(ALBUM_NAME,"integer");
        columns.put(ALBUM_ARTIST,"integer");
        columns.put(ALBUM_PIC,"integer");
        String sql = buildCreateSql(TABLE_ALBUM, columns);
        db.execSQL(sql);
    }

    //创建触发器
    private void createTrigger(SQLiteDatabase db){
        String trig1 = "create trigger playlist_cleanup1 after delete on "+DBHelper.TABLE_PLAYLIST
                +" begin"
                +" delete from "+DBHelper.TABLE_LISTMUSIC +" where "+DBHelper.LISTMUSIC_LISTID +"=old."+DBHelper.ID+";"
                +" end;";
        String trig2 = "create trigger playlist_cleanup2 after delete on "+DBHelper.TABLE_MUSIC
                +" begin"
                +" delete from "+DBHelper.TABLE_LISTMUSIC +" where "+DBHelper.LISTMUSIC_MUSICID +"=old."+DBHelper.ID+";"
                +" end;";
        db.execSQL(trig1);
        db.execSQL(trig2);
    }

    private String buildCreateSql(String table, Map<String,String> columns) {
        StringBuilder sb = new StringBuilder("create table if not exists ").append(table).append("(");
        sb.append(ID).append(" integer primary key autoincrement,");
        for(Map.Entry<String,String> column:columns.entrySet()){
            sb.append(column.getKey()).append(" ").append(column.getValue()).append(",");
        }
        sb.replace(sb.length() - 1, sb.length(), ")");
        return sb.toString();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
