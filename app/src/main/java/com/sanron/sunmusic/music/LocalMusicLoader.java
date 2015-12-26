package com.sanron.sunmusic.music;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/19.
 */
public class LocalMusicLoader {

    public static List<File> load() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalDir = Environment.getExternalStorageDirectory();
            return load(externalDir);
        }
        return null;
    }

    public static List<File> load(File dir){
        List<File> files = new ArrayList<>();
        searchMusic(files, dir);
        return files;
    }

    private static void searchMusic(final List<File> data, File dir) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory() && file.getPath().indexOf("/.") == -1) {
                searchMusic(data, file);
            } else {
                String path = file.getAbsolutePath();
                if (path.endsWith(".mp3")
                        || path.endsWith(".aac")
                        || path.endsWith(".flac")) {
                    data.add(file);
                }
            }
        }
    }


}
