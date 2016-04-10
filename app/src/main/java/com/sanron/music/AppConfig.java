package com.sanron.music;

/**
 * Created by sanron on 16-4-10.
 */
public class AppConfig {

    public static final String DATA_PATH = "ddmusic";

    public static final String CACHE_PATH = DATA_PATH + "/cache";

    public static final String DOWNLOAD_PATH = DATA_PATH + "/download";

    public static final String LYRIC_PATH = DATA_PATH + "/lyric";


    /**
     * ImageLoader配置
     */
    public static final String IMG_CACHE_PATH = CACHE_PATH + "/img_cache";

    public static final int IMG_DISK_CACHE_MAX_SIZE = 100 * 1024 * 1024;//磁盘缓存大小

    public static final int IMG_DISK_CACHE_MAX_COUNT = 500;//磁盘缓存文件数量

    public static final int THREAD_POOL_SIZE = 3;

    public static final int MAX_MEMORY_CACHE_SIZE = 16 * 1024 * 1024;//RAM缓存最多16MB

    public static final float MEMORY_CACHE_PERCENTAGE = 0.1f;//默认10%程序最大内存的ram缓存


    /**
     * http配置
     */
    public static final String HTTP_CACHE_PATH = CACHE_PATH + "/http_cache";
    public static final int HTTP_CACHE_MAX_SIZE = 20 * 1024 * 1024;//20MB

    public static final int HTTP_READ_TIMEOUT = 10;
    public static final int HTTP_CONNECT_TIMEOUT = 10;
}
