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
        public static final String DATA = "data";
        public static final String DATE_MODIFIED = "date_modified";
        public static final String TITLE = "title";
        public static final String TITLE_KEY = "title_key";
        public static final String ALBUM = "album";
        public static final String ARTIST = "artist";
        public static final String DURATION = "duration";
        public static final String SONG_ID = "song_id";
        public static final String BITRATE = "bitrate";
        public static final String LYRIC = "lyric";
    }


    public static class Album {
        /**
         * 专辑
         */
        public static final String TABLE = "album";
        public static final String NAME = "name";
        public static final String ARTIST = "artist";
    }

    public static class Artist {
        /**
         * 歌手
         */
        public static final String TABLE = "artist";
        public static final String NAME = "name";
    }

    public static class List {
        /**
         * 播放列表
         */
        public static final String TABLE = "playlist";
        public static final String TITLE = "name";
        public static final String TYPE = "type";
        public static final String ADD_TIME = "add_time";
        public static final String LIST_ID = "list_id";
        public static final String ICON = "icon_url";

        public static final int TYPE_LOCAL = 1;//本地音乐
        public static final int TYPE_RECENT = 2;//最近播放
        public static final int TYPE_FAVORITE = 3;//我喜欢
        public static final int TYPE_USER = 4;//用户自建
        public static final int TYPE_COLLECTION = 5;//收藏歌单

        public static final int TYPE_LOCAL_ID = 1;
        public static final int TYPE_RECENT_ID = 2;
    }

    public static class ListMember {
        /**
         * 播放列表和音乐关系表
         */
        public static final String TABLE = "list_member_datas";
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
        columnTypes.put(Music.DATA, "text");
        columnTypes.put(Music.DATE_MODIFIED, "integer");
        columnTypes.put(Music.TITLE, "text");
        columnTypes.put(Music.TITLE_KEY, "text");
        columnTypes.put(Music.ALBUM, "text");
        columnTypes.put(Music.ARTIST, "text default <unknown>");
        columnTypes.put(Music.DURATION, "integer");
        columnTypes.put(Music.SONG_ID, "text");
        columnTypes.put(Music.BITRATE, "integer");
        columnTypes.put(Music.LYRIC, "text");
        String sql = buildCreateSql(Music.TABLE, columnTypes);
        db.execSQL(sql);
        String index1 = createIndexSql("path_index", Music.TABLE, Music.DATA);
        db.execSQL(index1);
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
        columnTypes.put(List.TITLE, "text");
        columnTypes.put(List.ADD_TIME, "integer default 0");
        columnTypes.put(List.LIST_ID, "text");
        columnTypes.put(List.ICON, "text");
        String sql = buildCreateSql(List.TABLE, columnTypes);

        //创建我喜欢，最近播放两个列表
        String sql1 = "insert into " + List.TABLE + "(" + DBHelper.ID + "," + List.TYPE + "," + List.TITLE + ") " +
                "values(" + List.TYPE_LOCAL_ID + "," + List.TYPE_LOCAL + ",'本地音乐')";
        String sql2 = "insert into " + List.TABLE + "(" + DBHelper.ID + "," + List.TYPE + "," + List.TITLE + ") " +
                "values(" + List.TYPE_RECENT_ID + "," + List.TYPE_RECENT + ",'最近播放')";
        String sql3 = "insert into " + List.TABLE + "(" + List.TYPE + "," + List.TITLE + ") " +
                "values(" + List.TYPE_FAVORITE + ",'我喜欢')";
        db.execSQL(sql);
        db.execSQL(sql1);
        db.execSQL(sql2);
        db.execSQL(sql3);
    }

    public void createTableListMusic(SQLiteDatabase db) {
        Map<String, String> columnTypes = new LinkedHashMap<>();
        columnTypes.put(ListMember.LIST_ID, "integer");
        columnTypes.put(ListMember.MUSIC_ID, "integer");
        String sql = buildCreateSql(ListMember.TABLE, columnTypes);
        db.execSQL(sql);
    }

    public void createTableArtist(SQLiteDatabase db) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put(Artist.NAME, "integer");
        String sql = buildCreateSql(Artist.TABLE, columns);
        db.execSQL(sql);
    }

    public void createTableAlbum(SQLiteDatabase db) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put(Album.NAME, "integer");
        columns.put(Album.ARTIST, "integer");
        String sql = buildCreateSql(Album.TABLE, columns);
        db.execSQL(sql);
    }

    //创建触发器
    private void createTrigger(SQLiteDatabase db) {
        String trig1 = "create trigger playlist_cleanup1 after delete on " + List.TABLE
                + " begin"
                + " delete from " + ListMember.TABLE + " where " + ListMember.LIST_ID + "=old." + DBHelper.ID + ";"
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
