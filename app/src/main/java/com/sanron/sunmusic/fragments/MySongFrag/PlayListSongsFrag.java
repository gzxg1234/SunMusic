package com.sanron.sunmusic.fragments.MySongFrag;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
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
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.task.DelListSongTask;
import com.sanron.sunmusic.task.GetPlayListSongsTask;
import com.sanron.sunmusic.utils.T;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListSongsFrag extends BaseFragment{

    private PlayList mPlayList;
    private RecyclerView mListPlaySongs;
    private SongItemAdapter mSongItemAdapter;

    public static final String TAG = "PlayListSonsFrag";

    public static PlayListSongsFrag newInstance(PlayList playList) {
        return new PlayListSongsFrag(playList);
    }

    private PlayListSongsFrag(PlayList mPlayList) {
        this.mPlayList = mPlayList;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSongItemAdapter = new SongItemAdapter(getContext(), null);

        mSongItemAdapter.setOnItemClickListener(new SongItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });

        mSongItemAdapter.setOnActionClickListener(new SongItemAdapter.OnActionClickListener() {
            @Override
            public void onActionClick(View view, int actionPosition) {
                showActionMenu(view, actionPosition);
            }
        });
        update(null,DBHelper.TABLE_LISTSONGS);
    }

    public void showActionMenu(final View anchor, final int position) {
        final SongInfo songInfo = mSongItemAdapter.getData(position);
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.inflate(R.menu.playlistsong_action);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_remove: {
                        List<SongInfo> delSongs = new ArrayList<>();
                        delSongs.add(songInfo);
                        showConfirmRemoveDlg("删除歌曲",
                                "确定删除歌曲\"" + songInfo.getDisplayName() + "\"",
                                delSongs);
                    }
                    break;

                    case R.id.menu_add_to_quque: {

                    }
                    break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.frag_recycler_layout, null);
        contentView.setBackgroundColor(Color.WHITE);
        mListPlaySongs = $(R.id.recycler_view);
        mListPlaySongs.setAdapter(mSongItemAdapter);
        mListPlaySongs.setItemAnimator(new DefaultItemAnimator());
        mListPlaySongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return contentView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.playlistsongsfrag_option_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_clear_list_songs: {

                if (mSongItemAdapter.getItemCount() > 0) {
                    //有歌曲弹出确定对话框
                    showConfirmRemoveDlg("删除歌曲", "确定移除所有歌曲", mSongItemAdapter.getData());
                } else {
                    T.show(getContext(), "当前列表为空");
                }
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmRemoveDlg(String title, String msg, List<SongInfo> songInfos) {
        //有歌曲弹出确定对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                new DelListSongTask(mPlayList, mSongItemAdapter.getData()) {
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

    @Override
    public void update(Observable observable, Object data) {
        if(DBHelper.TABLE_LISTSONGS.equals(data)) {
            new GetPlayListSongsTask(mPlayList) {
                @Override
                protected void onPostExecute(List<SongInfo> data) {
                    mSongItemAdapter.setData(data);
                }
            }.execute();
        }
    }
}
