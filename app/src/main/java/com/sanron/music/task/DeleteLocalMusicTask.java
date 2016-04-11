package com.sanron.music.task;

import android.os.AsyncTask;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.Music;

import java.util.List;

/**
 * Created by sanron on 16-4-11.
 */
public class DeleteLocalMusicTask extends AsyncTask<Void, Void, Void> {

    private List<Music> deleteMusics;
    private boolean isDeleteFile;

    public DeleteLocalMusicTask(List<Music> deleteMusics, boolean deleteFile) {
        this.deleteMusics = deleteMusics;
        this.isDeleteFile = deleteFile;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DataProvider.Access listMemberAccess = DataProvider.instance().getAccess(DBHelper.ListMember.TABLE);
        DataProvider.Access musicAccess = DataProvider.instance().getAccess(DBHelper.Music.TABLE);


        return null;
    }
}
