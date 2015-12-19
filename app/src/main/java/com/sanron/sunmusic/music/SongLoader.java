package com.sanron.sunmusic.music;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.sanron.sunmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/19.
 */
public class SongLoader {

   /* public static List<File> load() {
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
    }*/

    public static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public static final String[] PROJECTION = new String[]{
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
    };

    public static List<SongInfo> load(Context context) {
        List<SongInfo> songInfos = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(URI,
                PROJECTION,
                MediaStore.Audio.Media.IS_MUSIC + "=?",
                new String[]{"1"},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        while (cursor.moveToNext()) {
            SongInfo songInfo = toSongInfo(cursor);
            songInfos.add(songInfo);
        }

        return songInfos;
    }


    private static SongInfo toSongInfo(Cursor cursor) {
        SongInfo songInfo = new SongInfo();

        String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
        String pinyin = PinyinHelper.convertToPinyinString(displayName, "", PinyinFormat.WITHOUT_TONE);

        songInfo.setAlbum(album);
        songInfo.setArtist(artist);
        songInfo.setDisplayName(displayName);
        songInfo.setDuration(duration);
        songInfo.setPath(path);
        songInfo.setTitle(title);
        songInfo.setType(SongInfo.TYPE_LOCAL);
        songInfo.setPinyin(pinyin);
        return songInfo;
    }
}
