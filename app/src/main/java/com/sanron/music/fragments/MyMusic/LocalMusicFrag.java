package com.sanron.music.fragments.MyMusic;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.sanron.music.activities.MainActivity;
import com.sanron.music.activities.ScanActivity;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.Music;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.task.DeleteLocalMusicTask;
import com.sanron.music.utils.T;

import java.util.List;
import java.util.Observer;

/**
 * Created by Administrator on 2015/12/21.
 */
public class LocalMusicFrag extends ListMusicFrag implements MainActivity.BackPressedHandler, Observer, CompoundButton.OnCheckedChangeListener {

    public static final int MENU_UPDATE_LOCAL_MUSIC = 1;

    public static final String EXTRA_EVENT = "event";

    public static final int EVENT_REFRESH = 1;

    public static final int EVENT_DELETE = 2;
    public static final String EXTRA_DELETE_ID = "delete_id";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int event = intent.getIntExtra(EXTRA_EVENT, -1);
            switch (event) {
                case EVENT_REFRESH: {
                    refreshData();
                }
                break;

                case EVENT_DELETE: {
                    long id = intent.getLongExtra(EXTRA_DELETE_ID, -1);
                    List<Music> musics = adapter.getData();
                    for (int i = 0; i < musics.size(); i++) {
                        if (musics.get(i).getId() == id) {
                            adapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
                break;
            }
        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(receiver, new IntentFilter(getClass().getName()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(receiver);
    }

    public LocalMusicFrag() {
        PlayList playList = new PlayList();
        playList.setId(DBHelper.List.TYPE_LOCAL_ID);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_PLAY_LIST, playList);
        setArguments(bundle);
    }

    @Override
    protected void onDeleteOperator(List<Music> checkedMusics) {
        new DeleteLocalSongDialogBuilder(getContext(), checkedMusics)
                .show();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).addBackPressedHandler(this);
            }
        } else {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).removeBackPressedHandler(this);
            }
            if (isAdded() && adapter.isMultiMode()) {
                endMultiMode();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(ALTERNATIVE_GROUP_ID, MENU_UPDATE_LOCAL_MUSIC, Menu.NONE, "扫描歌曲");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case MENU_UPDATE_LOCAL_MUSIC: {
                Intent intent = new Intent(getContext(), ScanActivity.class);
                startActivity(intent);
            }
            break;
        }
        return true;
    }


    public class DeleteLocalSongDialogBuilder extends AlertDialog.Builder implements DialogInterface.OnClickListener {
        private List<Music> mDeleteMusics;
        private boolean mIsDeleteFile;
        private ProgressDialog mProgressDialog;

        public DeleteLocalSongDialogBuilder(Context context, List<Music> deleteMusics) {
            super(context);
            this.mDeleteMusics = deleteMusics;
            this.mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("删除中");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);

            if (deleteMusics.size() == 1) {
                setTitle("删除歌曲 " + deleteMusics.get(0).getTitle());
            } else {
                setTitle("删除" + deleteMusics.size() + "首歌曲");
            }
            setMultiChoiceItems(new String[]{"同时删除音乐文件"},
                    new boolean[]{mIsDeleteFile},
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            mIsDeleteFile = isChecked;
                        }
                    });
            setPositiveButton("确定", this);
            setNegativeButton("取消", this);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE: {
                    dialog.cancel();
                }
                break;

                case DialogInterface.BUTTON_POSITIVE: {
                    new DeleteLocalMusicTask(mDeleteMusics, mIsDeleteFile) {
                        @Override
                        protected void onPreExecute() {
                            mProgressDialog.show();
                        }

                        @Override
                        protected void onPostExecute(Integer deleteNum) {
                            if (deleteNum > 0) {
                                T.show("删除" + deleteNum + "首歌曲");
                            } else {
                                T.show("删除失败");
                            }
                            mProgressDialog.dismiss();
                        }
                    }.execute();
                }
                break;
            }
        }

    }
}


