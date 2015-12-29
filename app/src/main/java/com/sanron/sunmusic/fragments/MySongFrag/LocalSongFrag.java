package com.sanron.sunmusic.fragments.MySongFrag;

import android.app.ProgressDialog;
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
import com.sanron.sunmusic.service.MusicService;
import com.sanron.sunmusic.service.PlayerUtils;
import com.sanron.sunmusic.task.GetLocalSongsTask;
import com.sanron.sunmusic.task.GetPlayListsTask;
import com.sanron.sunmusic.task.RefreshLocalSongsTask;
import com.sanron.sunmusic.window.AddSongToListWindow;
import com.sanron.sunmusic.window.DeleteSongDialogBuilder;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class LocalSongFrag extends BaseListFrag<SongInfo> {

    private MusicService.MusicPlayer player = PlayerUtils.getService();
    public LocalSongFrag(int layout) {
        super(layout,new String[]{DBHelper.TABLE_SONG,DBHelper.TABLE_ALBUM});
    }

    public static LocalSongFrag newInstance() {
        return new LocalSongFrag(LAYOUT_LINEAR);
    }


    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {
        SongInfo songInfo = mAdapter.getItem(position);
        holder.tvText1.setText(songInfo.getTitle());
        holder.tvText2.setText(songInfo.getArtist());
    }

    @Override
    public void refreshData() {
        new GetLocalSongsTask() {
            @Override
            protected void onPostExecute(List<SongInfo> data) {
                mAdapter.setData(data);
            }
        }.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.option_menu_localsongfrag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_refresh_localsong: {
                final ProgressDialog mProgressDlg = new ProgressDialog(getContext());
                new RefreshLocalSongsTask(getContext()) {
                    @Override
                    protected void onPreExecute() {
                        mProgressDlg.setMessage("正在扫描本地歌曲");
                        mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        mProgressDlg.setCancelable(false);
                        mProgressDlg.show();
                    }
                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mProgressDlg.cancel();
                    }
                }.execute();
            }
            break;
        }
        return true;
    }

    @Override
    public boolean onCreateActionMenu(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.song_menu,menu);
        menu.removeItem(R.id.menu_remove_from_list);
        return true;
    }

    @Override
    public void onActionItemSelected(MenuItem item, final List<SongInfo> songInfos){
        resolveMenuItemClick(item,songInfos);
    }

    @Override
    public void onCreatePopupMenu(Menu menu, MenuInflater inflater,int position) {
        inflater.inflate(R.menu.song_menu,menu);
        menu.removeItem(R.id.menu_remove_from_list);
    }

    @Override
    public void onPopupItemSelected(MenuItem item, int position) {
        final List<SongInfo> songInfos = mAdapter.getData().subList(position, position + 1);
        resolveMenuItemClick(item,songInfos);
    }

    public boolean resolveMenuItemClick(MenuItem item, final List<SongInfo> songInfos){
        switch(item.getItemId()){
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
        player.play(mAdapter.getData(),position);
        super.onItemClick(view, position);
    }
}


