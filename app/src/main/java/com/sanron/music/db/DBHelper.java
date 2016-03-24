package com.sanron.music.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/12/20.
 */
public class DBHelper extends SQLiteOpenHelper {


    public static final String DBNAME = "SunMusicDB.db";
    public static final int DBVERSION = 1;


    public static final String ID = "_id";

    public static class Music {
        /**
         * 音乐
         */
        public static final String TABLE = "music_info";
        public static final String DISPLAY = "display_name";
        public static final String PATH = "path";
        public static final String DATE_MODIFIED = "date_modified";
        public static final String TYPE = "type";
        public static final String TITLE = "title";
        public static final String TITLE_KEY = "title_key";
        public static final String ALBUM = "album";
        public static final String ALBUM_KEY = "album_key";
        public static final String ARTIST = "artist";
        public static final String ARTIST_KEY = "artist_key";
        public static final String DURATION = "duration";
        public static final String SONG_ID = "song_id";
        public static final String BITRATE = "bitrate";
        public static final String PIC = "picture";
        public static final String LYRIC = "lyric";

        public static final int TYPE_LOCAL = 1;
        public static final int TYPE_WEB = 2;
    }


    public static class Album {
        /**
         * 专辑
         */
        public static final String TABLE = "album";
        public static final String NAME = "name";
        public static final String ARTIST = "artist";
        public static final String PIC = "picture";
    }

    public static class Artist {
        /**
         * 歌手
         */
        public static final String TABLE = "artist";
        public static final String NAME = "name";
        public static final String PIC = "picture";
    }

    public static class List {
        /**
         * 播放列表
         */
        public static final String TABLE = "playlist";
        public static final String NAME = "name";
        public static final String TYPE = "type";

        public static final int TYPE_USER = 1;
        public static final int TYPE_RECENT = 2;
        public static final int TYPE_FAVORITE = 3;
    }

    public static class ListData {
        /**
         * 播放列表和音乐关系表
         */
        public static final String TABLE = "list_data";
        public static final String MUSIC_ID = "music_id";
        public static final String LIST_ID = "list_id";
    }


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

    public void createTableMusicInfo(SQLiteDatabase db) {
        Map<String, String> columnTypes = new LinkedHashMap<>();
        columnTypes.put(Music.DISPLAY, "text");
        columnTypes.put(Music.TYPE, "integer");
        columnTypes.put(Music.PATH, "text");
        columnTypes.put(Music.DATE_MODIFIED, "integer");
        columnTypes.put(Music.TITLE, "text");
        columnTypes.put(Music.TITLE_KEY, "text");
        columnTypes.put(Music.ALBUM, "text");
        columnTypes.put(Music.ALBUM_KEY, "text");
        columnTypes.put(Music.ARTIST, "text");
        columnTypes.put(Music.ARTIST_KEY, "text");
        columnTypes.put(Music.DURATION, "integer");
        columnTypes.put(Music.SONG_ID, "text");
        columnTypes.put(Music.BITRATE, "integer");
        columnTypes.put(Music.LYRIC, "text");
        columnTypes.put(Music.PIC, "text");
        String sql = buildCreateSql(Music.TABLE, columnTypes);
        db.execSQL(sql);
        String index1 = createIndexSql("path_index", Music.TABLE, Music.PATH);
        String index2 = createIndexSql("type_index", Music.TABLE, Music.TYPE);
        db.execSQL(index1);
        db.execSQL(index2);
    }

    public String createIndexSql(String indexname, String tablename, String column) {
        StringBuffer sb = new StringBuffer();
        return sb.append("create index ").append(indexname)
                .append(" on ").append(tablename)
                .append("(").append(column).append(")")
                .toString();
    }


    public void createTablePlayList(SQLiteDatabase db) {
        Map<String, String> columnTypes = new LinkedHashMap<>();
        columnTypes.put(List.TYPE, "integer");
        columnTypes.put(List.NAME, "text");
        String sql = buildCreateSql(List.TABLE, columnTypes);
        db.execSQL(sql);

        //创建我喜欢，最近播放两个列表
        sql = "insert into " + List.TABLE + "(" + List.TYPE + "," + List.NAME + ") " +
                "values(" + List.TYPE_FAVORITE + ",'我喜欢')";
        db.execSQL(sql);
        sql = "insert into " + List.TABLE + "(" + List.TYPE + "," + List.NAME + ") " +
                "values(" + List.TYPE_RECENT + ",'最近播放')";
        db.execSQL(sql);
    }

    public void createTableListMusic(SQLiteDatabase db) {
        Map<String, String> columnTypes = new LinkedHashMap<>();
        columnTypes.put(ListData.LIST_ID, "integer");
        columnTypes.put(ListData.MUSIC_ID, "integer");
        String sql = buildCreateSql(ListData.TABLE, columnTypes);
        db.execSQL(sql);
    }

    public void createTableArtist(SQLiteDatabase db) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put(Artist.NAME, "integer");
        columns.put(Artist.PIC, "integer");
        String sql = buildCreateSql(Artist.TABLE, columns);
        db.execSQL(sql);
    }

    public void createTableAlbum(SQLiteDatabase db) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put(Album.NAME, "integer");
        columns.put(Album.ARTIST, "integer");
        columns.put(Album.PIC, "integer");
        String sql = buildCreateSql(Album.TABLE, columns);
        db.execSQL(sql);
    }

    //创建触发器
    private void createTrigger(SQLiteDatabase db) {
        String trig1 = "create trigger playlist_cleanup1 after delete on " + List.TABLE
                + " begin"
                + " delete from " + ListData.TABLE + " where " + ListData.LIST_ID + "=old." + DBHelper.ID + ";"
                + " end;";
        db.execSQL(trig1);
    }

    private String buildCreateSql(String table, Map<String, String> columns) {
        StringBuilder sb = new StringBuilder("create table if not exists ").append(table).append("(");
        sb.append(ID).append(" integer primary key autoincrement,");
        for (Map.Entry<String, String> column : columns.entrySet()) {
            sb.append(column.getKey()).append(" ").append(column.getValue()).append(",");
        }
        sb.replace(sb.length() - 1, sb.length(), ")");
        return sb.toString();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
