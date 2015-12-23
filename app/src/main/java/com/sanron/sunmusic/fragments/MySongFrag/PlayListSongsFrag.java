package com.sanron.sunmusic.fragments.MySongFrag;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.SongItemAdapter;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.task.GetPlayListSongsTask;
import com.sanron.sunmusic.task.RemoveListSongTask;
import com.sanron.sunmusic.utils.MyLog;
import com.sanron.sunmusic.utils.T;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListSongsFrag extends BaseFragment {

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
        setHasOptionsMenu(true);
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

        new GetPlayListSongsTask() {
            @Override
            protected void onPostData(List<SongInfo> data) {
                mSongItemAdapter.setData(data);
            }
        }.execute(mPlayList.getId());
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
                        new RemoveListSongTask(mPlayList, songInfo) {
                            @Override
                            protected void onPostData(Integer num) {
                                if (num <= 0) {
                                    T.show(getContext(), "移除失败");
                                } else {
                                    mSongItemAdapter.removeData(position);
                                }
                            }
                        }.execute();
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.frag_playlist_songs, null);
        mListPlaySongs = $(R.id.list_playlist_songs);
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
                if(mSongItemAdapter.getItemCount()>0) {
                    new RemoveListSongTask(mPlayList, mSongItemAdapter.getData()) {
                        @Override
                        protected void onPostData(Integer integer) {
                            if(integer>0) {
                                mSongItemAdapter.clearData();
                            }
                        }
                    }.execute();
                }
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
