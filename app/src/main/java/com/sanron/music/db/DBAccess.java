package com.sanron.music.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sanron.music.db.model.Music;

import java.util.List;

/**
 * Created by sanron on 16-3-23.
 */
public class DBAccess{

    private DBHelper dbHelper;
    private static DBAccess instance;

    public static DBAccess instance(){
        if(instance == null){
            synchronized (DBAccess.class){
                if(instance == null){
                    instance = new DBAccess();
                }
            }
        }
        return instance;
    }

    public void init(Context context){
        dbHelper = new DBHelper(context);
    }

    public void updateLocalMusic(List<Music> musics){
        SQLiteDatabase db =getDatabse();
        for(Music music:musics){
            String path = music.getPath();
            Cursor c = db.rawQuery("select "+DBHelper.ID+
            " from "+DBHelper.TABLE_MUSIC+
            " where "+DBHelper.MUSIC_PATH+"=?",new String[]{path});
            if(c.moveToFirst()){
                long id = c.getInt(0);
                db.update(DBHelper.TABLE_MUSIC,music.toValues(),
                        DBHelper.ID+"=?",
                        new String[]{""+id});
            }else{
                db.insert(DBHelper.TABLE_MUSIC,null,music.toValues());
            }
            c.close();
        }
        db.close();
    }

    public SQLiteDatabase getDatabse(){
        return dbHelper.getWritableDatabase();
    }
}
