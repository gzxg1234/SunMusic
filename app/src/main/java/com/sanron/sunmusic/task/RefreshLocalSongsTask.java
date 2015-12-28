package com.sanron.sunmusic.task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.sanron.sunmusic.db.AlbumProvider;
import com.sanron.sunmusic.db.ArtistProvider;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.db.SongInfoProvider;
import com.sanron.sunmusic.model.Artist;
import com.sanron.sunmusic.model.SongInfo;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

/**
 * 刷新本地歌曲(同步MediaProvider数据)
 * Created by Administrator on 2015/12/21.
 */
public class RefreshLocalSongsTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;
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

    public RefreshLocalSongsTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        SongInfoProvider songInfoProvider = SongInfoProvider.instance();
        Cursor cursor = mContext.getContentResolver().query(URI,
                PROJECTION,
                MediaStore.Audio.Media.IS_MUSIC + "=?",
                new String[]{"1"},
                MediaStore.Audio.Media._ID);

        ContentValues values = new ContentValues(2);
        values.put(DBHelper.SONG_TYPE, SongInfo.TYPE_LOCAL);
        //删除数据库和MediaProvider不同的数据
        StringBuffer delSql = new StringBuffer("delete from " + DBHelper.TABLE_SONG
                + " where " + DBHelper.SONG_TYPE + "=" + SongInfo.TYPE_LOCAL+" and (");

        if(cursor.moveToFirst()){
            do{
                String songid = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                delSql.append(DBHelper.SONG_SONGID).append("!=").append(songid).append(" and ");
                values.put(DBHelper.SONG_SONGID, songid);
                Cursor c2 = songInfoProvider.query(values);
                if (!c2.moveToFirst()) {

                    ContentValues songValues = toContentValues(cursor);

                    //歌手信息
                    String artistName = songValues.getAsString(DBHelper.SONG_ARTISTNAME);
                    long artistId = getArtistID(artistName);
                    songValues.put(DBHelper.SONG_ARTISTID,artistId);

                    //专辑信息
                    String albumName = songValues.getAsString(DBHelper.SONG_ALBUMNAME);
                    long albumId = getAlbumID(artistName,albumName);
                    songValues.put(DBHelper.SONG_ALBUMID,albumId);

                    songInfoProvider.insert(songValues);
                }

                c2.close();
            }while (cursor.moveToNext());
            delSql.replace(delSql.length() - 5, delSql.length(), ")");
        }else{
            delSql.replace(delSql.length() - 6, delSql.length(), "");
        }
        cursor.close();
        songInfoProvider.execSQL(delSql.toString());

        songInfoProvider.notifyDataChanged();
        PlayListProvider.instance().notifyDataChanged();
        ArtistProvider.instance().notifyDataChanged();
        AlbumProvider.instance().notifyDataChanged();
        return null;
    }

    public long getArtistID(String artistName){
        ContentValues artistValues = new ContentValues();
        artistValues.put(DBHelper.ARTIST_NAME,artistName);
        Cursor cursor = ArtistProvider.instance().query(artistValues);
        long artistId = -1;
        if(cursor.moveToFirst()){
            //如果数据库中已有此歌手
            artistId = cursor.getLong(cursor.getColumnIndex(DBHelper.ID));
        }else{
            //没有则插入新歌手信息
            artistId = ArtistProvider.instance().insert(artistValues);
        }
        cursor.close();
        return artistId;
    }

    public long getAlbumID(String artistName,String albumName){
        ContentValues albumValues = new ContentValues();
        albumValues.put(DBHelper.ALBUM_NAME,albumName);
        albumValues.put(DBHelper.ALBUM_ARTIST,artistName);
        Cursor cursor = AlbumProvider.instance().query(albumValues);
        long albumId = -1;
        if(cursor.moveToFirst()){
            //如果数据库中已有此专辑
            albumId = cursor.getLong(cursor.getColumnIndex(DBHelper.ID));
        }else{
            //没有则插入新专辑信息
            albumId = AlbumProvider.instance().insert(albumValues);
        }
        cursor.close();
        return albumId;
    }

    //读比特率
    private int readBitrate(String path) {
        File file = new File(path);
        if (file.exists()) {
            try {
                AudioFile audioFile = AudioFileIO.read(file);
                int bitrate = (int) audioFile.getAudioHeader().getBitRateAsNumber();
                return bitrate;
            } catch (CannotReadException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TagException e) {
                e.printStackTrace();
            } catch (ReadOnlyFileException e) {
                e.printStackTrace();
            } catch (InvalidAudioFrameException e) {
                e.printStackTrace();
            }

        }
        return 0;
    }

    //转换MediaProvider数据
    private ContentValues toContentValues(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
        String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        int bitrate = readBitrate(path);
        int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
        String letter = "";
        if (title.length() > 0) {
            letter = PinyinHelper.convertToPinyinString(title.substring(0, 1), "", PinyinFormat.WITHOUT_TONE);
            letter = letter.substring(0, 1);
        }

        ContentValues values = new ContentValues();
        values.put(DBHelper.SONG_TYPE, SongInfo.TYPE_LOCAL);
        values.put(DBHelper.SONG_TITLE, title);
        values.put(DBHelper.SONG_SONGID, id);
        values.put(DBHelper.SONG_LETTER, letter);
        values.put(DBHelper.SONG_PATH, path);
        values.put(DBHelper.SONG_ALBUMNAME, album);
        values.put(DBHelper.SONG_DURATION, duration);
        values.put(DBHelper.SONG_DISPLAYNAME, displayName);
        values.put(DBHelper.SONG_ARTISTNAME, artist);
        values.put(DBHelper.SONG_BITRATE, bitrate);
        return values;
    }

}
