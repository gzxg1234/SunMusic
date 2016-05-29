package com.sanron.ddmusic.common;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2015/12/29.
 */
public class AudioTool {

    //读比特率
    public static int readBitrate(String path) {
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

    public static void writeAudioInfo(String path,String title,String album,String artist) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException, CannotWriteException {
        File file = new File(path);
        AudioFile audioFile = AudioFileIO.read(file);
        Tag tag = audioFile.getTagOrCreateDefault();
        tag.setField(FieldKey.TITLE,title);
        tag.setField(FieldKey.ALBUM,album);
        tag.setField(FieldKey.ARTIST,artist);
        AudioFileIO.write(audioFile);
    }
}
