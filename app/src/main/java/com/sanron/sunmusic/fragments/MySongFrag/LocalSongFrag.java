package com.sanron.sunmusic.fragments.MySongFrag;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.sanron.sunmusic.task.GetLocalSongsTask;
import com.sanron.sunmusic.task.GetPlayListsTask;
import com.sanron.sunmusic.task.UpdateLocalSongsTask;
import com.sanron.sunmusic.window.AddSongToListWindow;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Administrator on 2015/12/21.
 */
public class LocalSongFrag extends BaseFragment implements Observer {


    private RecyclerView mListLocalSongs;
    private SongItemAdapter mSongItemAdapter;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.frag_localsong, null);
        mListLocalSongs = $(R.id.list_localsong);
        mListLocalSongs.setAdapter(mSongItemAdapter);
        mListLocalSongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
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
                            protected void onPostData(List<PlayList> playLists) {
                                AddSongToListWindow window = new AddSongToListWindow(getActivity(), playLists, songInfo);
                                window.show();
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

    @Override
    public void update(Observable observable, Object data) {
        new GetLocalSongsTask() {
            @Override
            protected void onPostData(List<SongInfo> data) {
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
                new UpdateLocalSongsTask(getContext()) {
                    @Override
                    protected void onPostData(List<SongInfo> data) {
                        mSongItemAdapter.setData(data);
                    }
                }.execute();
            }
            break;

            case R.id.option_delete_alllocal: {

            }
            break;
        }
        return true;
    }


}


