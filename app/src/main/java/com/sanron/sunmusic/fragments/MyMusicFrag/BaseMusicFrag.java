package com.sanron.sunmusic.fragments.MyMusicFrag;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.DataListAdapter;
import com.sanron.sunmusic.fragments.BaseListFrag;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.Music;
import com.sanron.sunmusic.service.IMusicPlayer;
import com.sanron.sunmusic.service.PlayerUtils;
import com.sanron.sunmusic.task.GetPlayListTask;
import com.sanron.sunmusic.utils.T;
import com.sanron.sunmusic.window.AddSongToListWindow;
import com.sanron.sunmusic.window.DeleteSongDialogBuilder;

import java.util.List;

/**
 *
 * 可播放音乐基础fragment
 * Created by Administrator on 2016/3/6.
 */
public abstract class BaseMusicFrag extends BaseListFrag<Music> {



    public BaseMusicFrag(int layout, String[] subscribes) {
        super(layout, subscribes);
    }

    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {
        Music music = mAdapter.getItem(position);
        holder.tvText1.setText(music.getTitle());
        holder.tvText2.setText(music.getArtist());
    }

    @Override
    public boolean onCreateActionMenu(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.song_menu, menu);
        return true;
    }

    @Override
    public void onActionItemSelected(MenuItem item, final List<Music> musics) {
        resolveMenuItemClick(item, musics);
    }

    @Override
    public void onCreatePopupMenu(Menu menu, MenuInflater inflater, int position) {
        inflater.inflate(R.menu.song_menu, menu);
    }

    @Override
    public void onPopupItemSelected(MenuItem item, int position) {
        final List<Music> musics = mAdapter.getData().subList(position, position + 1);
        resolveMenuItemClick(item, musics);
    }

    protected boolean resolveMenuItemClick(MenuItem item, final List<Music> musics) {
        switch (item.getItemId()) {
            case R.id.menu_add_to_list: {
                //添加到播放列表
                new GetPlayListTask() {
                    @Override
                    protected void onPostExecute(List<PlayList> playLists) {
                        AddSongToListWindow window = new AddSongToListWindow(getActivity(),
                                playLists, musics);
                        window.show();
                    }
                }.execute();
            }
            break;

            case R.id.menu_add_to_quque: {
                //添加到播放队列
                player.enqueue(musics);
                T.show(getContext(),musics.size()+"首歌曲添加到队列");
            }
            break;

            case R.id.menu_delete_song: {
                //删除
                new DeleteSongDialogBuilder(getContext(), musics).create().show();
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
