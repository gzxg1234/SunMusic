package com.sanron.sunmusic.fragments.MySongFrag;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.DataListAdapter;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.task.GetPlayListSongsTask;
import com.sanron.sunmusic.task.GetPlayListsTask;
import com.sanron.sunmusic.utils.T;
import com.sanron.sunmusic.window.AddSongToListWindow;
import com.sanron.sunmusic.window.DeleteSongDialogBuilder;
import com.sanron.sunmusic.window.RemoveListSongDialogBuilder;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListSongsFrag extends BaseListFrag<SongInfo> {

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
        contentView.setBackgroundColor(Color.WHITE);
    }


    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {
        SongInfo songInfo = mAdapter.getItem(position);
        holder.tvText1.setText(songInfo.getTitle());
        holder.tvText2.setText(songInfo.getArtist());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.option_menu_playlistsongsfrag, menu);
    }


    @Override
    public boolean onCreateActionMenu(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.song_menu,menu);
        return true;
    }

    @Override
    public void onActionItemSelected(MenuItem item, List<SongInfo> songInfos) {
        resolveMenuItemClick(item,songInfos);
    }

    @Override
    public void onCreatePopupMenu(Menu menu, MenuInflater inflater,int position) {
        inflater.inflate(R.menu.song_menu,menu);
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

    public boolean resolveMenuItemClick(MenuItem item, final List<SongInfo> songInfos){
        switch (item.getItemId()) {
            case R.id.menu_add_to_list: {
                new GetPlayListsTask() {
                    @Override
                    protected void onPostExecute(List<PlayList> playLists) {
                        AddSongToListWindow window = new AddSongToListWindow(getActivity(),
                                playLists, songInfos);
                        window.show();
                    }
                }.execute();
            }break;

            case R.id.menu_add_to_quque: {
                //添加到播放队列

            }
            break;

            case R.id.menu_remove_from_list:{
                new RemoveListSongDialogBuilder(getContext(), mPlayList, songInfos)
                        .create().show();
            }break;

            case R.id.menu_delete_song: {
                //删除
                new DeleteSongDialogBuilder(getContext(), songInfos).create().show();
            }
            break;
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
