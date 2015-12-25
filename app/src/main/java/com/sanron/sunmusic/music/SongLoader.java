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

    public static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public static final String[] PROJECTION = new String[]{
            MediaStore.Audio.Media._ID,
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
                MediaStore.Audio.Media._ID);

        while (cursor.moveToNext()) {
            SongInfo songInfo = toSongInfo(cursor);
            songInfos.add(songInfo);
        }
        cursor.close();
        return songInfos;
    }


    private static SongInfo toSongInfo(Cursor cursor) {
        SongInfo songInfo = new SongInfo();

        String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
        String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
        int index = displayName.lastIndexOf(".");
        if (index != -1) {
            displayName = displayName.substring(0, index);
        }
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
        String letter = "";
        if (displayName.length() > 0) {
            letter = PinyinHelper.convertToPinyinString(displayName.substring(0, 1), "", PinyinFormat.WITHOUT_TONE);
            letter = letter.substring(0,1);
        }

        songInfo.setSongId(id);
        songInfo.setAlbum(album);
        songInfo.setArtist(artist);
        songInfo.setDisplayName(displayName);
        songInfo.setDuration(duration);
        songInfo.setPath(path);
        songInfo.setTitle(title);
        songInfo.setType(SongInfo.TYPE_LOCAL);
        songInfo.setLetter(letter);
        return songInfo;
    }
}
