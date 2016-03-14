package com.sanron.sunmusic.fragments.MyMusic;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.model.Music;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.task.GetListMusicTask;
import com.sanron.sunmusic.utils.T;
import com.sanron.sunmusic.window.RemoveListSongDialogBuilder;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class ListMusicFrag extends BaseMusicFrag {

    private PlayList mPlayList;

    public static final String TAG = "PlayListSonsFrag";


    public ListMusicFrag() {
        super(LAYOUT_LINEAR, new String[]{DBHelper.TABLE_LISTMUSIC, DBHelper.TABLE_MUSIC});

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mPlayList = (PlayList) getArguments().get("playList");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contentView.setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.option_menu_playlistsongsfrag, menu);
    }

    @Override
    public void onPopupItemSelected(MenuItem item, int position) {
        final List<Music> musics = mAdapter.getData().subList(position, position + 1);
        resolveMenuItemClick(item, musics);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_clear_list_songs: {

                if (mAdapter.getItemCount() > 0) {
                    //弹出确认对话框
                    new RemoveListSongDialogBuilder(getContext(), mPlayList, mAdapter.getData())
                            .create()
                            .show();
                } else {
                    T.show(getContext(), "当前列表为空");
                }
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean resolveMenuItemClick(MenuItem item, final List<Music> musics) {
        switch (item.getItemId()) {
            case R.id.menu_remove_from_list: {
                new RemoveListSongDialogBuilder(getContext(), mPlayList, musics)
                        .create().show();
            }
            break;

            default:
                super.resolveMenuItemClick(item, musics);
        }
        return true;
    }

    @Override
    public void refreshData() {
        new GetListMusicTask(mPlayList) {
            @Override
            protected void onPostExecute(List<Music> data) {
                mAdapter.setData(data);
            }
        }.execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("playList", mPlayList);
    }
}
