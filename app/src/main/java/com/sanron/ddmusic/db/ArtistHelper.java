package com.sanron.ddmusic.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sanron on 16-5-29.
 */
public class ArtistHelper {

    public interface Columns {
        String TABLE = "artist";
        String NAME = "name";
    }
    public static void onCreate(SQLiteDatabase db) {
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put(Columns.NAME, "integer");
        String sql = BaseHelper.buildCreateSql(Columns.TABLE, columns);
        db.execSQL(sql);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
