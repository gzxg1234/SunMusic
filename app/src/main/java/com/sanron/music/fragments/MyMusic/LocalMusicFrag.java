package com.sanron.music.fragments.MyMusic;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;

import com.sanron.music.activities.MainActivity;
import com.sanron.music.activities.ScanActivity;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.Music;
import com.sanron.music.service.IPlayer;
import com.sanron.music.task.QueryTask;
import com.sanron.music.view.DeleteSongDialogBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

/**
 * Created by Administrator on 2015/12/21.
 */
public class LocalMusicFrag extends BaseMusicFrag implements MainActivity.BackPressedHandler, QueryTask.QueryCallback, Observer, CompoundButton.OnCheckedChangeListener {

    public static final int MENU_UPDATE_LOCAL_MUSIC = 1;

    @Override
    protected void refreshData() {
        new QueryTask()
                .table(DBHelper.Music.TABLE)
                .selection(DBHelper.Music.TYPE + "=" + DBHelper.Music.TYPE_LOCAL)
                .orderBy(DBHelper.Music.TITLE_KEY)
                .execute(this);
    }

    @Override
    protected void onMultiActionDeleteClick(List<Music> checkedMusics) {
        new DeleteSongDialogBuilder(getContext(), checkedMusics)
                .show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setObserveTable(DBHelper.Music.TABLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mainActivity != null) {
                mainActivity.addBackPressedHandler(this);
            }
        } else {
            if (mainActivity != null) {
                mainActivity.removeBackPressedHandler(this);
            }
            if (isAdded() && adapter.isMultiMode()) {
                endMultiMode();
            }
        }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(ALTERNATIVE_GROUP_ID, MENU_UPDATE_LOCAL_MUSIC, Menu.NONE, "扫描歌曲");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case MENU_UPDATE_LOCAL_MUSIC: {
                Intent intent = new Intent(getContext(), ScanActivity.class);
                startActivity(intent);
            }
            break;
        }
        return true;
    }


    @Override
    public void onPreQuery() {
    }

    @Override
    public void onQueryFinish(Cursor cursor) {
        List<Music> musics = new LinkedList<>();
        while (cursor.moveToNext()) {
            Music music = Music.fromCursor(cursor);
            musics.add(music);
        }
        adapter.setData(musics);
        callback.onStateChange(IPlayer.STATE_PREPARING);
    }


}


