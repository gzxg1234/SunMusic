package com.sanron.music.fragments.MyMusic;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.Music;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.service.IPlayer;
import com.sanron.music.task.QueryTask;
import com.sanron.music.view.RemoveListSongDialogBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

/**
 * Created by Administrator on 2015/12/21.
 */

public class ListMusicFrag extends BaseMusicFrag {

    private PlayList playList;

    public static final String TAG = ListMusicFrag.class.getSimpleName();

    public static final String ARG_PLAY_LIST = "play_list";

    @Override
    protected void onMultiActionDeleteClick(List<Music> checkedMusics) {
        new RemoveListSongDialogBuilder(getContext(), playList, checkedMusics)
                .show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            playList = (PlayList) getArguments().get(ARG_PLAY_LIST);
        }
        setObserveTable(DBHelper.ListData.TABLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.addBackPressedHandler(this);
        contentView.setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainActivity.removeBackPressedHandler(this);
    }

    @Override
    public boolean onBackPressed() {
        if (adapter.isMultiMode()) {
            endMultiMode();
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void refreshData() {
        new QueryTask()
                .table(DBHelper.ListData.TABLE)
                .columns(DBHelper.ListData.MUSIC_ID)
                .selection(DBHelper.ListData.LIST_ID + "=?")
                .selectionArgs(String.valueOf(playList.getId()))
                .execute(listDataCallback);
    }

    private QueryTask.QueryCallback musicDataCallback = new QueryTask.QueryCallback() {
        @Override
        public void onPreQuery() {

        }

        @Override
        public void onQueryFinish(Cursor cursor) {
            List<Music> musics = new LinkedList<>();
            while (cursor.moveToNext()) {
                musics.add(Music.fromCursor(cursor));
            }
            adapter.setData(musics);
            callback.onStateChange(IPlayer.STATE_PREPARING);
        }
    };


    private QueryTask.QueryCallback listDataCallback = new QueryTask.QueryCallback() {
        @Override
        public void onPreQuery() {
        }

        @Override
        public void onQueryFinish(Cursor cursor) {
            StringBuilder sb = new StringBuilder();
            while (cursor.moveToNext()) {
                long musicId = cursor.getLong(0);
                sb.append(musicId).append(",");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            new QueryTask()
                    .table(DBHelper.Music.TABLE)
                    .selection(DBHelper.ID + " in (" + sb.toString() + ")")
                    .execute(musicDataCallback);
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_PLAY_LIST, playList);
    }


}
