package com.sanron.ddmusic.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2015/12/20.
 */
public class AppDB extends SQLiteOpenHelper {


    public static final String DB_NAME = "SunMusicDB.db";
    public static final int DB_VERSION = 1;
    private static volatile AppDB sInstance;

    public static AppDB get(Context context) {
        if (sInstance == null) {
            synchronized (AppDB.class) {
                if (sInstance == null) {
                    sInstance = new AppDB(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public AppDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        MusicHelper.onCreate(db);
        PlayListHelper.onCreate(db);
        ArtistHelper.onCreate(db);
        AlbumHelper.onCreate(db);
        ListMemberHelper.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MusicHelper.onUpgrade(db, oldVersion, newVersion);
        PlayListHelper.onUpgrade(db, oldVersion, newVersion);
        ArtistHelper.onUpgrade(db, oldVersion, newVersion);
        AlbumHelper.onUpgrade(db, oldVersion, newVersion);
        ListMemberHelper.onUpgrade(db, oldVersion, newVersion);
    }

}
