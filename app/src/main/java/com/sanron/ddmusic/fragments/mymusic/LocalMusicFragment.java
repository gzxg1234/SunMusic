package com.sanron.ddmusic.fragments.mymusic;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;

import com.sanron.ddmusic.R;
import com.sanron.ddmusic.activities.MainActivity;
import com.sanron.ddmusic.activities.ScanActivity;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.ResultCallback;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class LocalMusicFragment extends ListMusicFragment implements MainActivity.BackPressedHandler, CompoundButton.OnCheckedChangeListener {

    public static final int MENU_UPDATE_LOCAL_MUSIC = 1;

    public LocalMusicFragment() {
        PlayList playList = new PlayList();
        playList.setId(PlayList.TYPE_LOCAL_ID);
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
        if (getMainActivity() == null) {
            return;
        }
        if (isVisibleToUser) {
            getMainActivity().addBackPressedHandler(this);
        } else {
            getMainActivity().removeBackPressedHandler(this);
            if (isAdded() && mAdapter.isMultiMode()) {
                endMultiMode();
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(R.id.alternative_group, MENU_UPDATE_LOCAL_MUSIC, Menu.NONE, "扫描歌曲");
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
                    AppDB.get(getContext()).deleteLocalMusic(
                            mDeleteMusics,
                            mIsDeleteFile,
                            new ResultCallback<Integer>() {
                                @Override
                                public void onResult(Integer deleteNum) {
                                    if (deleteNum > 0) {
                                        ViewTool.show("删除" + deleteNum + "首歌曲");
                                    } else {
                                        ViewTool.show("删除失败");
                                    }
                                    mProgressDialog.dismiss();
                                }
                            });
                }
                break;
            }
        }

    }
}


