package com.sanron.ddmusic.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sanron.ddmusic.db.bean.PlayList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sanron on 16-5-29.
 */
public class PlayListHelper {

    public interface Columns {
        String TABLE = "playlist";
        String TITLE = "name";
        String TYPE = "type";
        String ADD_TIME = "add_time";
        String LIST_ID = "list_id";
        String ICON = "icon_url";
    }

    public static void onCreate(SQLiteDatabase db) {
        Map<String, String> columnTypes = new LinkedHashMap<>();
        columnTypes.put(Columns.TYPE, "integer");
        columnTypes.put(Columns.TITLE, "text");
        columnTypes.put(Columns.ADD_TIME, "integer default 0");
        columnTypes.put(Columns.LIST_ID, "text");
        columnTypes.put(Columns.ICON, "text");
        String sql = BaseHelper.buildCreateSql(Columns.TABLE, columnTypes);

        //创建我喜欢，最近播放两个列表
        String sql1 = "insert into " + Columns.TABLE + "(" + BaseHelper.ID + "," + Columns.TYPE + "," + Columns.TITLE + ") " +
                "values(" + PlayList.TYPE_LOCAL_ID + "," + PlayList.TYPE_LOCAL + ",'本地音乐')";
        String sql2 = "insert into " + Columns.TABLE + "(" + BaseHelper.ID + "," + Columns.TYPE + "," + Columns.TITLE + ") " +
                "values(" + PlayList.TYPE_RECENT_ID + "," + PlayList.TYPE_RECENT + ",'最近播放')";
        String sql3 = "insert into " + Columns.TABLE + "(" + BaseHelper.ID + "," + Columns.TYPE + "," + Columns.TITLE + ") " +
                "values(" + PlayList.TYPE_FAVORITE_ID + "," + PlayList.TYPE_FAVORITE + ",'我喜欢')";
        db.execSQL(sql);
        db.execSQL(sql1);
        db.execSQL(sql2);
        db.execSQL(sql3);

        String trig1 = "create trigger playlist_cleanup1 after delete on " + Columns.TABLE
                + " begin"
                + " delete from " + Columns.TABLE + " where " + ListMemberHelper.Columns.LIST_ID + "=old." + BaseHelper.ID + ";"
                + " end;";
        db.execSQL(trig1);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static List<PlayList> getListByType(SQLiteDatabase db, int type) {
        List<PlayList> playLists = new ArrayList<>();
        Cursor c = db.query(
                Columns.TABLE,
                null,
                Columns.TYPE + "=" + type,
                null, null, null, Columns.ADD_TIME, null);
        while (c.moveToNext()) {
            PlayList playList = PlayList.fromCursor(c);
            playLists.add(playList);
        }
        c.close();
        return playLists;
    }

    /**
     * 检查listid是否存在
     */
    public static boolean isExistByListId(SQLiteDatabase db, String listid) {
        String sql = "select 1 from " + Columns.TABLE
                + " where " + Columns.LIST_ID + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{listid});
        boolean exist = cursor.moveToFirst();
        cursor.close();
        return exist;
    }

    public static int deleteById(SQLiteDatabase db, long id) {
        return db.delete(Columns.TABLE, BaseHelper.ID + "=" + id, null);
    }

    public static boolean isExistByName(SQLiteDatabase db, String name) {
        String sql = "select 1 from " + Columns.TABLE
                + " where " + Columns.TITLE + "=? and " + Columns.TYPE + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{name, PlayList.TYPE_USER + ""});
        boolean exist = cursor.moveToFirst();
        cursor.close();
        return exist;
    }

    public static int updateName(SQLiteDatabase db, long id, String name) {
        ContentValues update = new ContentValues(1);
        update.put(Columns.TITLE, name);
        return db.update(Columns.TABLE, update, BaseHelper.ID + "=" + id, null);
    }

    public static long addPlaylist(SQLiteDatabase db, PlayList list) {
        return db.insert(Columns.TABLE, null, list.toContentValues());
    }


}
