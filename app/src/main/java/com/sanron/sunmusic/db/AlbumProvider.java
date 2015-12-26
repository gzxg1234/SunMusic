package com.sanron.sunmusic.db;

/**
 * Created by Administrator on 2015/12/23.
 */
public class AlbumProvider extends DataProvider {

    private AlbumProvider() {
        super(DBHelper.TABLE_ALBUM);
    }

    private static volatile AlbumProvider mInstance = null;

    public static AlbumProvider instance() {
        if (mInstance == null) {
            synchronized (AlbumProvider.class) {
                if (mInstance == null) {
                    mInstance = new AlbumProvider();
                }
            }
        }
        return mInstance;
    }

}
