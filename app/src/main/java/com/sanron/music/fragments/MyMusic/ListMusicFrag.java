package com.sanron.music.fragments.MyMusic;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.Music;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.service.IPlayer;
import com.sanron.music.task.QueryListMemberDatasTask;
import com.sanron.music.view.RemoveListSongDialogBuilder;

import java.util.List;
import java.util.Observable;

/**
 * Created by Administrator on 2015/12/21.
 */

public class ListMusicFrag extends BaseMusicFrag {

    private PlayList playList;

    public static final String TAG = ListMusicFrag.class.getSimpleName();

    public static final String ARG_PLAY_LIST = "play_list";

    public static ListMusicFrag newInstance(PlayList list) {
        ListMusicFrag listMusicFrag = new ListMusicFrag();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLAY_LIST, list);
        listMusicFrag.setArguments(args);
        return listMusicFrag;
    }


    @Override
    protected void onDeleteOperator(List<Music> checkedMusics) {
        new RemoveListSongDialogBuilder(getContext(), playList, checkedMusics)
                .show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            playList = (PlayList) getArguments().get(ARG_PLAY_LIST);
        }
        setObserveTable(DBHelper.ListMember.TABLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void update(Observable observable, Object data) {
        super.update(observable, data);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.addBackPressedHandler(this);
        contentView.setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainActivity.removeBackPressedHandler(this);
    }

    @Override
    public boolean onBackPressed() {
        if (adapter.isMultiMode()) {
            endMultiMode();
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void refreshData() {
        new QueryListMemberDatasTask(playList.getId()) {
            @Override
            protected void onPostExecute(List<Music> musics) {
                adapter.setData(musics);
                callback.onStateChange(IPlayer.STATE_PLAYING);
            }
        }.execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_PLAY_LIST, playList);
    }

}
