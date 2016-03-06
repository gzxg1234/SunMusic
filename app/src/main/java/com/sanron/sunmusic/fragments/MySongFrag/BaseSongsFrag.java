package com.sanron.sunmusic.fragments.MySongFrag;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.DataListAdapter;
import com.sanron.sunmusic.fragments.BaseListFrag;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.service.IMusicPlayer;
import com.sanron.sunmusic.service.PlayerUtils;
import com.sanron.sunmusic.task.GetPlayListsTask;
import com.sanron.sunmusic.window.AddSongToListWindow;
import com.sanron.sunmusic.window.DeleteSongDialogBuilder;

import java.util.List;

/**
 * Created by Administrator on 2016/3/6.
 */
public abstract class BaseSongsFrag extends BaseListFrag<SongInfo> {

    protected IMusicPlayer player = PlayerUtils.getService();

    public BaseSongsFrag(int layout, String[] subscribes) {
        super(layout, subscribes);
    }

    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {
        SongInfo songInfo = mAdapter.getItem(position);
        holder.tvText1.setText(songInfo.getTitle());
        holder.tvText2.setText(songInfo.getArtist());
    }

    @Override
    public boolean onCreateActionMenu(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.song_menu, menu);
        return true;
    }

    @Override
    public void onActionItemSelected(MenuItem item, final List<SongInfo> songInfos) {
        resolveMenuItemClick(item, songInfos);
    }

    @Override
    public void onCreatePopupMenu(Menu menu, MenuInflater inflater, int position) {
        inflater.inflate(R.menu.song_menu, menu);
    }

    @Override
    public void onPopupItemSelected(MenuItem item, int position) {
        final List<SongInfo> songInfos = mAdapter.getData().subList(position, position + 1);
        resolveMenuItemClick(item, songInfos);
    }

    protected boolean resolveMenuItemClick(MenuItem item, final List<SongInfo> songInfos) {
        switch (item.getItemId()) {
            case R.id.menu_add_to_list: {
                //添加到播放列表
                new GetPlayListsTask() {
                    @Override
                    protected void onPostExecute(List<PlayList> playLists) {
                        AddSongToListWindow window = new AddSongToListWindow(getActivity(),
                                playLists, songInfos);
                        window.show();
                    }
                }.execute();
            }
            break;

            case R.id.menu_add_to_quque: {
                //添加到播放队列
                player.enqueue(songInfos);
            }
            break;

            case R.id.menu_delete_song: {
                //删除
                new DeleteSongDialogBuilder(getContext(), songInfos).create().show();
            }
            break;
        }
        return true;
    }

    @Override
    public void onItemClick(View view, int position) {
        player.play(mAdapter.getData(), position);
    }
}
