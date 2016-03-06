package com.sanron.sunmusic.fragments.MySongFrag;

import android.app.ProgressDialog;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.service.IMusicPlayer;
import com.sanron.sunmusic.service.PlayerUtils;
import com.sanron.sunmusic.task.GetLocalSongsTask;
import com.sanron.sunmusic.task.RefreshLocalSongsTask;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class LocalSongsFrag extends BaseSongsFrag {

    private IMusicPlayer player = PlayerUtils.getService();
    public LocalSongsFrag(int layout) {
        super(layout,new String[]{DBHelper.TABLE_SONG,DBHelper.TABLE_ALBUM});
    }

    public static LocalSongsFrag newInstance() {
        return new LocalSongsFrag(LAYOUT_LINEAR);
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
        super.onCreateActionMenu(mode,menu);
        menu.removeItem(R.id.menu_remove_from_list);
        return true;
    }

    @Override
    public void onCreatePopupMenu(Menu menu, MenuInflater inflater,int position) {
        super.onCreatePopupMenu(menu,inflater,position);
        menu.removeItem(R.id.menu_remove_from_list);
    }

}


