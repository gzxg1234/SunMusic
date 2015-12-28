package com.sanron.sunmusic.fragments.MySongFrag;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.SongItemAdapter;
import com.sanron.sunmusic.db.SongInfoProvider;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.task.DelLocalSongTask;
import com.sanron.sunmusic.task.GetLocalSongsTask;
import com.sanron.sunmusic.task.GetPlayListsTask;
import com.sanron.sunmusic.task.RefreshLocalSongsTask;
import com.sanron.sunmusic.utils.T;
import com.sanron.sunmusic.window.AddSongToListWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Administrator on 2015/12/21.
 */
public class LocalSongFrag extends BaseFragment implements Observer {


    private RecyclerView mListLocalSongs;
    private SongItemAdapter mSongItemAdapter;
    private ProgressDialog mProgressDialog;
    public static final String TAG = "LocalSongFrag";

    public static LocalSongFrag newInstance() {
        return new LocalSongFrag();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        SongInfoProvider.instance().addObserver(this);
        mSongItemAdapter = new SongItemAdapter(getContext(), null);
        mSongItemAdapter.setOnActionClickListener(new SongItemAdapter.OnActionClickListener() {
            @Override
            public void onActionClick(View view, int actionPosition) {
                SongInfo songInfo = mSongItemAdapter.getData().get(actionPosition);
                showActionMenu(view, songInfo);
            }
        });
        update(null, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SongInfoProvider.instance().deleteObserver(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.frag_localsong, null);
        mListLocalSongs = $(R.id.list_localsong);
        mListLocalSongs.setAdapter(mSongItemAdapter);
        mListLocalSongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setOwnerActivity(getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("正在扫描本地歌曲");
        return contentView;
    }

    public void showActionMenu(final View anchor, final SongInfo songInfo) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.inflate(R.menu.localsong_action);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_add_to_list: {
                        new GetPlayListsTask() {
                            @Override
                            protected void onPostExecute(List<PlayList> playLists) {
                                AddSongToListWindow window = new AddSongToListWindow(getActivity(),
                                        playLists, songInfo);
                                window.show();
                            }
                        }.execute();
                    }
                    break;

                    case R.id.menu_add_to_quque: {

                    }
                    break;

                    case R.id.menu_delete_local: {
                        List<SongInfo> deleteSongs = new ArrayList<>();
                        deleteSongs.add(songInfo);
                        showConfirmDeleteDlg("删除歌曲", deleteSongs);
                    }
                    break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public void update(Observable observable, Object data) {
        new GetLocalSongsTask() {
            @Override
            protected void onPostExecute(List<SongInfo> data) {
                mSongItemAdapter.setData(data);
            }
        }.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.localsongfrag_option_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_refresh_localsong: {
                new RefreshLocalSongsTask(getContext()){
                    @Override
                    protected void onPreExecute() {
                        mProgressDialog.show();
                    }
                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        mProgressDialog.cancel();
                    }
                }.execute();
            }
            break;

            case R.id.option_delete_alllocal: {
                showConfirmDeleteDlg("删除所有歌曲", mSongItemAdapter.getData());
            }
            break;
        }
        return true;
    }

    /**
     * 显示删除确定对话框
     */
    private void showConfirmDeleteDlg(String title, final List<SongInfo> deleteSongs) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setCancelable(false);
        final boolean[] deleteFile = new boolean[]{false};
        builder.setMultiChoiceItems(new String[]{"同时删除音乐文件"},
                deleteFile,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        deleteFile[0] = isChecked;
                    }
                });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                new DelLocalSongTask(getContext(), deleteSongs, deleteFile[0]) {

                    @Override
                    protected void onPostExecute(Integer num) {
                        if (num > 0) {
                            T.show(getContext(), "删除" + num + "首歌曲");
                        } else {
                            T.show(getContext(), "删除失败");
                        }
                    }
                }.execute();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
}


