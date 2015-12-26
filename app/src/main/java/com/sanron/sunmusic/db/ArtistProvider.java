package com.sanron.sunmusic.db;

/**
 * Created by Administrator on 2015/12/23.
 */
public class ArtistProvider extends DataProvider {

    private ArtistProvider() {
        super(DBHelper.TABLE_ARTIST);
    }

    private static volatile ArtistProvider mInstance = null;

    public static ArtistProvider instance() {
        if (mInstance == null) {
            synchronized (ArtistProvider.class) {
                if (mInstance == null) {
                    mInstance = new ArtistProvider();
                }
            }
        }
        return mInstance;
    }

}
