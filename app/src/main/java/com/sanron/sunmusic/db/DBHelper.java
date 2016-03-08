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
    public static final String TABLE_MUSIC = "music_info";
    public static final String MUSIC_TYPE = "type";
    public static final String MUSIC_DISPLAYNAME = "display_name";
    public static final String MUSIC_TITLE = "title";
    public static final String MUSIC_ALBUMNAME = "album_name";
    public static final String MUSIC_ALBUMID = "album_id";
    public static final String MUSIC_ARTISTNAME = "artist_name";
    public static final String MUSIC_ARTISTID = "artist_id";
    public static final String MUSIC_DURATION = "duration";
    public static final String MUSIC_PATH = "path";
    public static final String MUSIC_MUSICID = "musicid";
    public static final String MUSIC_LETTER = "title_letter";
    public static final String MUSIC_BITRATE = "bitrate";
    public static final String MUSIC_PIC = "picture";
    public static final String MUSIC_LYRIC = "lyric";

    /**
     * 专辑
     */
    public static final String TABLE_ALBUM = "album";
    public static final String ALBUM_NAME = "name";
    public static final String ALBUM_ARTIST = "artist";
    public static final String ALBUM_MUSICNUM = "music_num";
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
    public static final String PLAYLIST_MUSICNUM = "music_num";


    /**
     * 播放列表和音乐关系表
     */
    public static final String TABLE_LISTMUSIC = "playlistmusic";
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
        String[] columns = new String[]{
                MUSIC_TYPE,
                MUSIC_DISPLAYNAME,
                MUSIC_TITLE,
                MUSIC_ALBUMNAME,
                MUSIC_ALBUMID,
                MUSIC_ARTISTNAME,
                MUSIC_ARTISTID,
                MUSIC_DURATION,
                MUSIC_PATH,
                MUSIC_MUSICID,
                MUSIC_LETTER,
                MUSIC_BITRATE,
                MUSIC_LYRIC,
                MUSIC_PIC
        };
        String[] columnTypes = new String[]{"interger","text","text","text","integer","text","integer",
                "integer","text","text","text","integer","text","text"};
        String sql = buildCreateSql(TABLE_MUSIC, columns, columnTypes);
        db.execSQL(sql);
    }

    public void createTablePlayList(SQLiteDatabase db){
        String[] columns =  new String[]{
                PLAYLIST_TYPE,
                PLAYLIST_NAME,
                PLAYLIST_MUSICNUM,
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

    public void createTableListMusic(SQLiteDatabase db){
        String[] columns =  new String[]{
                LISTMUSIC_LISTID,
                LISTMUSIC_MUSICID,
        };
        String[] columnTypes = new String[]{"integer", "integer"};
        String sql = buildCreateSql(TABLE_LISTMUSIC, columns, columnTypes);
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
                ALBUM_MUSICNUM,
                ALBUM_PIC
        };
        String[] columnTypes = new String[]{"text", "text","integer default 0","text"};
        String sql = buildCreateSql(TABLE_ALBUM, columns, columnTypes);
        db.execSQL(sql);
    }

    //创建触发器
    private void createTrigger(SQLiteDatabase db){
        String trig1 = "create trigger listmusicInsert after insert on "+DBHelper.TABLE_LISTMUSIC
                +" begin"
                +" update "+DBHelper.TABLE_PLAYLIST+" set "+DBHelper.PLAYLIST_MUSICNUM +"="+DBHelper.PLAYLIST_MUSICNUM +"+1 where "+DBHelper.ID+"=new."+DBHelper.LISTMUSIC_LISTID +";"
                +" end;";
        String trig2 = "create trigger listmusicDelete after delete on "+DBHelper.TABLE_LISTMUSIC
                +" begin"
                +" update "+DBHelper.TABLE_PLAYLIST+" set "+DBHelper.PLAYLIST_MUSICNUM +"="+DBHelper.PLAYLIST_MUSICNUM +"-1 where "+DBHelper.ID+"=old."+DBHelper.LISTMUSIC_LISTID +";"
                +" end;";
        String trig3 = "create trigger musicinfoInsert after insert on "+DBHelper.TABLE_MUSIC
                +" begin"
                +" update "+DBHelper.TABLE_ALBUM+" set "+DBHelper.ALBUM_MUSICNUM +"="+DBHelper.ALBUM_MUSICNUM +"+1 where "+DBHelper.ID+"=new."+DBHelper.MUSIC_ALBUMID +";"
                +" end;";
        String trig4 = "create trigger musicinfoDelete after delete on "+DBHelper.TABLE_MUSIC
                +" begin"
                +" delete from "+DBHelper.TABLE_LISTMUSIC +" where "+DBHelper.LISTMUSIC_MUSICID +"=old."+DBHelper.ID+";"
                +" update "+DBHelper.TABLE_ALBUM+" set "+DBHelper.ALBUM_MUSICNUM +"="+DBHelper.ALBUM_MUSICNUM +"-1 where "+DBHelper.ID+"=old."+DBHelper.MUSIC_ALBUMID +";"
                +" end;";
        String trig5 = "create trigger playlistDelete after delete on "+DBHelper.TABLE_PLAYLIST
                +" begin"
                +" delete from "+DBHelper.TABLE_LISTMUSIC +" where "+DBHelper.LISTMUSIC_LISTID +"=old."+DBHelper.ID+";"
                +" end;";
        String trig6 = "create trigger albumInsert after insert on "+DBHelper.TABLE_ALBUM
                +" begin"
                +" update "+DBHelper.TABLE_ARTIST+" set "+DBHelper.ARTIST_ALBUMNUM+"="+DBHelper.ARTIST_ALBUMNUM+"+1 where "+DBHelper.ARTIST_NAME+"=new."+DBHelper.ALBUM_ARTIST+";"
                +" end;";
        String trig7 = "create trigger albumDelete after delete on "+DBHelper.TABLE_ALBUM
                +" begin"
                +" update "+DBHelper.TABLE_ARTIST+" set "+DBHelper.ARTIST_ALBUMNUM+"="+DBHelper.ARTIST_ALBUMNUM+"-1 where "+DBHelper.ARTIST_NAME+"=old."+DBHelper.ALBUM_ARTIST+";"
                +" delete from "+DBHelper.TABLE_MUSIC +" where "+DBHelper.MUSIC_ALBUMID +"=old."+DBHelper.ID+";"
                +" end;";
        String trig8 = "create trigger albumUpdate after update of "+DBHelper.ALBUM_MUSICNUM +" on "+DBHelper.TABLE_ALBUM+" when new."+DBHelper.ALBUM_MUSICNUM +"=0"
                +" begin"
                +" delete from "+DBHelper.TABLE_ALBUM+" where "+DBHelper.ID+"=new."+DBHelper.ID+";"
                +" end;";
        String trig9 = "create trigger artistUpdate after update of "+DBHelper.ARTIST_ALBUMNUM+" on "+DBHelper.TABLE_ARTIST+" when new."+DBHelper.ARTIST_ALBUMNUM+"=0"
                +" begin"
                +" delete from "+DBHelper.TABLE_ARTIST+" where "+DBHelper.ID+"=new."+DBHelper.ID+";"
                +" end;";
        db.execSQL(trig1);
        db.execSQL(trig2);
        db.execSQL(trig3);
        db.execSQL(trig4);
        db.execSQL(trig5);
        db.execSQL(trig6);
        db.execSQL(trig7);
        db.execSQL(trig8);
        db.execSQL(trig9);
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
