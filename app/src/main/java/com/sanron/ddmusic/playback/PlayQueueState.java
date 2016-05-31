package com.sanron.ddmusic.playback;

import com.sanron.ddmusic.db.bean.Music;

import java.io.Serializable;
import java.util.List;

public class PlayQueueState implements Serializable {
    private List<Music> musics;
    private int position;

    public PlayQueueState() {
    }

    public PlayQueueState(List<Music> musics, int position) {
        this.musics = musics;
        this.position = position;
    }

    public List<Music> getMusics() {
        return musics;
    }

    public void setMusics(List<Music> musics) {
        this.musics = musics;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}