package com.sanron.sunmusic.fragments.MySongFrag;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.task.GetPlayListSongsTask;
import com.sanron.sunmusic.utils.T;
import com.sanron.sunmusic.window.RemoveListSongDialogBuilder;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListSongsFrag extends BaseSongsFrag {

    private PlayList mPlayList;

    public static final String TAG = "PlayListSonsFrag";

    public static PlayListSongsFrag newInstance(PlayList playList) {
        return new PlayListSongsFrag(playList);
    }

    private PlayListSongsFrag(PlayList mPlayList) {
        super(LAYOUT_LINEAR,new String[]{DBHelper.TABLE_LISTSONGS,DBHelper.TABLE_SONG});
        this.mPlayList = mPlayList;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        contentView.setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.option_menu_playlistsongsfrag, menu);
    }

    @Override
    public void onPopupItemSelected(MenuItem item, int position) {
        final List<SongInfo> songInfos = mAdapter.getData().subList(position, position + 1);
        resolveMenuItemClick(item,songInfos);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_clear_list_songs: {

                if (mAdapter.getItemCount() > 0) {
                    //有歌曲弹出确定对话框
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

    protected boolean resolveMenuItemClick(MenuItem item, final List<SongInfo> songInfos){
        switch (item.getItemId()) {
            case R.id.menu_remove_from_list:{
                new RemoveListSongDialogBuilder(getContext(), mPlayList, songInfos)
                        .create().show();
            }break;

            default:
                super.resolveMenuItemClick(item,songInfos);
        }
        return true;
    }

    @Override
    public void refreshData() {
        new GetPlayListSongsTask(mPlayList) {
            @Override
            protected void onPostExecute(List<SongInfo> data) {
                mAdapter.setData(data);
            }
        }.execute();
    }
}
