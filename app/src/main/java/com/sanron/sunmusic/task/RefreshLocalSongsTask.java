package com.sanron.sunmusic.task;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.MediaSync;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ListView;

import com.sanron.sunmusic.db.SongInfoProvider;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.music.SongLoader;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.audio.mp4.atom.Mp4HdlrBox;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 刷新本地歌曲(同步MediaProvider数据)
 * Created by Administrator on 2015/12/21.
 */
public class RefreshLocalSongsTask extends AsyncTask<Void, Void, List<SongInfo>> {

    private Context mContext;
    private MediaScannerConnection.MediaScannerConnectionClient scannerClient;
    private MediaScannerConnection scannerConnection;
    public RefreshLocalSongsTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected List<SongInfo> doInBackground(Void... params) {

        SongInfoProvider songInfoProvider = SongInfoProvider.instance();
        List<SongInfo> newData = SongLoader.load(mContext);
        SongInfo query = new SongInfo();
        for (int i = 0; i < newData.size(); i++) {
            SongInfo songInfo = newData.get(i);
            //查找本地歌曲是否已添加到数据库的
            query.setSongId(songInfo.getSongId());
            List<SongInfo> result = songInfoProvider.query(query);
            if (result.size() > 0) {
                //数据库中已添加，保留数据库中的数据
                newData.set(i, result.get(0));
            }else {
                //扫描音乐比特率
                setBitrate(songInfo);
            }
        }

        query = new SongInfo();
        query.setType(SongInfo.TYPE_LOCAL);
        songInfoProvider.delete(query);
        songInfoProvider.insert(newData);
        songInfoProvider.notifyObservers();
        return newData;
    }

    private void setBitrate(SongInfo songInfo){
        File file = new File(songInfo.getPath());
        if(file.exists()){
            try {
                AudioFile audioFile = AudioFileIO.read(file);
                int bitrate = (int) audioFile.getAudioHeader().getBitRateAsNumber();
                songInfo.setBitrate(bitrate);
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
    };

}
