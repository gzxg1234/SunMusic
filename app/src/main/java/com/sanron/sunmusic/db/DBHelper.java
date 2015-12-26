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
    public static final String SONG_ALBUMNAME = "album_name";
    public static final String SONG_ALBUMID = "album_id";
    public static final String SONG_ARTISTNAME = "artist_name";
    public static final String SONG_ARTISTID = "artist_id";
    public static final String SONG_DURATION = "duration";
    public static final String SONG_PATH = "path";
    public static final String SONG_SONGID = "songid";
    public static final String SONG_LETTER = "title_letter";
    public static final String SONG_BITRATE = "bitrate";
    public static final String SONG_PIC = "picture";
    public static final String SONG_LYRIC = "lyric";

    /**
     * 专辑
     */
    public static final String TABLE_ALBUM = "album";
    public static final String ALBUM_NAME = "name";
    public static final String ALBUM_ARTIST = "artist";
    public static final String ALBUM_SONGNUM = "song_num";
    public static final String ALBUM_PIC = "picture";

    /**
     * 歌手
     */
    public static final String TABLE_ARTIST = "artist";
    public static final String ARTIST_NAME = "name";
    public static final String ARTIST_ALBUMNUM = "album_num";
    public static final String ARTIST_PIC = "picture";


    /**
     * 播放列表
     */
    public static final String TABLE_PLAYLIST = "playlist";
    public static final String PLAYLIST_NAME = "name";
    public static final String PLAYLIST_TYPE = "type";
    public static final String PLAYLIST_SONGNUM = "song_num";


    /**
     * 播放列表和音乐关系表
     */
    public static final String TABLE_LISTSONGS = "playlist_songs";
    public static final String LISTSONGS_SONGID = "song_id";
    public static final String LISTSONGS_LISTID = "list_id";


    public DBHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableSongInfo(db);
        createTablePlayList(db);
        createTableListSongs(db);
        createTableArtist(db);
        createTableAlbum(db);
        createTrigger(db);
    }

    public void createTableSongInfo(SQLiteDatabase db){
        String[] columns = new String[]{
                SONG_TYPE,
                SONG_DISPLAYNAME,
                SONG_TITLE,
                SONG_ALBUMNAME,
                SONG_ALBUMID,
                SONG_ARTISTNAME,
                SONG_ARTISTID,
                SONG_DURATION,
                SONG_PATH,
                SONG_SONGID,
                SONG_LETTER,
                SONG_BITRATE,
                SONG_LYRIC,
                SONG_PIC
        };
        String[] columnTypes = new String[]{"interger","text","text","text","integer","text","integer",
                "integer","text","text","text","integer","text","text"};
        String sql = buildCreateSql(TABLE_SONG, columns, columnTypes);
        db.execSQL(sql);
    }

    public void createTablePlayList(SQLiteDatabase db){
        String[] columns =  new String[]{
                PLAYLIST_TYPE,
                PLAYLIST_NAME,
                PLAYLIST_SONGNUM,
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

    public void createTableArtist(SQLiteDatabase db){
        String[] columns =  new String[]{
                ARTIST_NAME,
                ARTIST_ALBUMNUM,
                ARTIST_PIC
        };
        String[] columnTypes = new String[]{"text", "integer default 0","text"};
        String sql = buildCreateSql(TABLE_ARTIST, columns, columnTypes);
        db.execSQL(sql);
    }

    public void createTableAlbum(SQLiteDatabase db){
        String[] columns =  new String[]{
                ALBUM_NAME,
                ALBUM_ARTIST,
                ALBUM_SONGNUM,
                ALBUM_PIC
        };
        String[] columnTypes = new String[]{"text", "text","integer default 0","text"};
        String sql = buildCreateSql(TABLE_ALBUM, columns, columnTypes);
        db.execSQL(sql);
    }

    //创建触发器
    private void createTrigger(SQLiteDatabase db){
        String trig1 = "create trigger listsongsInsert after insert on "+DBHelper.TABLE_LISTSONGS
                +" begin"
                +" update "+DBHelper.TABLE_PLAYLIST+" set "+DBHelper.PLAYLIST_SONGNUM +"="+DBHelper.PLAYLIST_SONGNUM +"+1 where "+DBHelper.ID+"=new."+DBHelper.LISTSONGS_LISTID+";"
                +" end;";
        String trig2 = "create trigger listsongsDelete after delete on "+DBHelper.TABLE_LISTSONGS
                +" begin"
                +" update "+DBHelper.TABLE_PLAYLIST+" set "+DBHelper.PLAYLIST_SONGNUM +"="+DBHelper.PLAYLIST_SONGNUM +"-1 where "+DBHelper.ID+"=old."+DBHelper.LISTSONGS_LISTID+";"
                +" end;";
        String trig3 = "create trigger songinfoInsert after insert on "+DBHelper.TABLE_SONG
                +" begin"
                +" update "+DBHelper.TABLE_ALBUM+" set "+DBHelper.ALBUM_SONGNUM+"="+DBHelper.ALBUM_SONGNUM+"+1 where "+DBHelper.ID+"=new."+DBHelper.SONG_ALBUMID+";"
                +" end;";
        String trig4 = "create trigger songinfoDelete after delete on "+DBHelper.TABLE_SONG
                +" begin"
                +" delete from "+DBHelper.TABLE_LISTSONGS+" where "+DBHelper.LISTSONGS_SONGID+"=old."+DBHelper.ID+";"
                +" update "+DBHelper.TABLE_ALBUM+" set "+DBHelper.ALBUM_SONGNUM+"="+DBHelper.ALBUM_SONGNUM+"-1 where "+DBHelper.ID+"=old."+DBHelper.SONG_ALBUMID+";"
                +" end;";
        String trig5 = "create trigger playlistDelete after delete on "+DBHelper.TABLE_PLAYLIST
                +" begin"
                +" delete from "+DBHelper.TABLE_LISTSONGS+" where "+DBHelper.LISTSONGS_LISTID+"=old."+DBHelper.ID+";"
                +" end;";
        String trig6 = "create trigger albumInsert after insert on "+DBHelper.TABLE_ALBUM
                +" begin"
                +" update "+DBHelper.TABLE_ARTIST+" set "+DBHelper.ARTIST_ALBUMNUM+"="+DBHelper.ARTIST_ALBUMNUM+"+1 where "+DBHelper.ARTIST_NAME+"=new."+DBHelper.ALBUM_ARTIST+";"
                +" end;";
        String trig7 = "create trigger albumDelete after delete on "+DBHelper.TABLE_ALBUM
                +" begin"
                +" update "+DBHelper.TABLE_ARTIST+" set "+DBHelper.ARTIST_ALBUMNUM+"="+DBHelper.ARTIST_ALBUMNUM+"-1 where "+DBHelper.ARTIST_NAME+"=new."+DBHelper.ALBUM_ARTIST+";"
                +" delete from "+DBHelper.TABLE_SONG+" where "+DBHelper.SONG_ALBUMID+"=old."+DBHelper.ID+";"
                +" end;";
        db.execSQL(trig1);
        db.execSQL(trig2);
        db.execSQL(trig3);
        db.execSQL(trig4);
        db.execSQL(trig5);
        db.execSQL(trig6);
        db.execSQL(trig7);
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
