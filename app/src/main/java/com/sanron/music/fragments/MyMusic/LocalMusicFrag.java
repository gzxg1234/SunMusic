package com.sanron.music.fragments.MyMusic;

import android.content.ContentValues;
import android.content.Intent;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sanron.music.R;
import com.sanron.music.activities.ScanActivity;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.Music;
import com.sanron.music.task.QueryMusicTask;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class LocalMusicFrag extends BaseMusicFrag {

    public static final int MENU_UPDATE_LOCAL_MUSIC = 1;

    public LocalMusicFrag() {
        super(LAYOUT_LINEAR,new String[]{DBHelper.TABLE_MUSIC,DBHelper.TABLE_ALBUM});
    }

    @Override
    public void refreshData() {
        ContentValues query = new ContentValues(1);
        query.put(DBHelper.MUSIC_TYPE, Music.TYPE_LOCAL);
        new QueryMusicTask(query) {
            @Override
            protected void onPostExecute(List<Music> data) {
                mAdapter.setData(data);
            }
        }.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        menu.add(ALTERNATIVE_GROUP_ID, MENU_UPDATE_LOCAL_MUSIC,Menu.NONE,"扫描歌曲");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    public boolean onCreateActionMenu(ActionMode mode, Menu menu) {
        super.onCreateActionMenu(mode,menu);
        menu.removeItem(R.id.menu_remove_from_list);
        return true;
    }

    @Override
    public void onCreatePopupMenu(Menu menu, MenuInflater inflater,int position) {
        super.onCreatePopupMenu(menu,inflater,position);
        menu.removeItem(R.id.menu_remove_from_list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}


