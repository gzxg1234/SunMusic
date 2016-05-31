package com.sanron.ddmusic.fragments.mymusic;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sanron.ddmusic.R;
import com.sanron.ddmusic.adapter.PlayListAdapter;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.PlayListHelper;
import com.sanron.ddmusic.db.ResultCallback;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;
import com.sanron.ddmusic.service.PlayUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListFragment extends BaseDataFragment implements PlayListAdapter.OnItemClickListener, PlayListAdapter.OnItemMenuClickListener {


    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    public static final int MENU_NEW_LIST = 1;
    private PlayListAdapter mAdapter;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadData();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new PlayListAdapter(getContext());
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mReceiver,
                        new IntentFilter(AppDB.tableChangeAction(PlayListHelper.Columns.TABLE)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mReceiver);
    }

    @Override
    public int getViewResId() {
        return R.layout.layout_recycler_view;
    }

    @Override
    public void initView(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemMenuClickListener(this);
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    public void loadData() {
        AppDB.get(getContext()).getPlayList(new ResultCallback<List<PlayList>>() {
            @Override
            public void onResult(List<PlayList> result) {
                mAdapter.setData(result);
            }
        });
    }

    @Override
    public void onItemClick(View itemView, int position) {
        PlayList playList = (PlayList) mAdapter.getItem(position);
        getMainActivity().showPlayListSongs(playList);
    }

    @Override
    public void onItemMenuClick(View view, final int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        Menu menu = popupMenu.getMenu();
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_playlist, menu);
        int type = ((PlayList) mAdapter.getItem(position)).getType();
        if (type == PlayList.TYPE_FAVORITE) {
            //不是用户创建的列表，不能重命名和删除
            menu.removeItem(R.id.menu_delete_list);
            menu.removeItem(R.id.menu_rename_list);
        } else if (type == PlayList.TYPE_COLLECTION) {
            menu.removeItem(R.id.menu_rename_list);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onPopupItemSelected(item, position);
                return true;
            }
        });
        popupMenu.show();
    }

    public void onPopupItemSelected(MenuItem item, int position) {
        PlayList playList = (PlayList) mAdapter.getItem(position);
        switch (item.getItemId()) {
            case R.id.menu_play_list: {
                AppDB.get(getContext()).getPlayListMusics(playList.getId(), new ResultCallback<List<Music>>() {
                    @Override
                    public void onResult(List<Music> result) {
                        if (result.size() == 0) {
                            ViewTool.show("列表暂时没有歌曲");
                        } else {
                            PlayUtil.clearQueue();
                            PlayUtil.enqueue(result);
                            PlayUtil.play(0);
                        }
                    }
                });
            }
            break;

            case R.id.menu_delete_list: {
                showDeleteConfirmDlg(playList);
            }
            break;

            case R.id.menu_rename_list: {
                showRenameDialog(playList);
            }
            break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(R.id.alternative_group, MENU_NEW_LIST, Menu.NONE, "新建列表");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_NEW_LIST: {
                showNewListDialog();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showDeleteConfirmDlg(final PlayList playList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("删除列表");
        builder.setMessage("确定删除列表\"" + playList.getTitle() + "\"？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppDB.get(getContext()).deletePlayList(playList.getId(), new ResultCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        if (result) {
                            ViewTool.show("删除歌单成功");
                            loadData();
                        } else {
                            ViewTool.show("删除失败");
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void showRenameDialog(final PlayList playList) {
        final ListNameInputDialog dlg = new ListNameInputDialog(getContext());
        dlg.setTitle("重命名");
        dlg.setInput(playList.getTitle());
        dlg.setOnOkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String input = dlg.getInput();
                if (dlg.getInput().trim().equals("")) {
                    dlg.setInputError("输入为空");
                    return;
                }

                if (dlg.getInput().equals(playList.getTitle())) {
                    dlg.dismiss();
                    return;
                }

                AppDB.get(getContext()).updatePlayListName(playList.getId(), input, new ResultCallback<Integer>() {
                    @Override
                    public void onResult(Integer result) {
                        switch (result) {
                            case 0: {
                                ViewTool.show("修改失败");
                                dlg.dismiss();
                            }
                            break;
                            case 1: {
                                ViewTool.show("修改成功");
                                dlg.dismiss();
                            }
                            break;
                            case -1: {

                                dlg.setInputError("列表名已存在");
                            }
                            break;
                        }
                    }
                });
            }
        });
        dlg.show();
    }

    /**
     * 新建列表对话框
     */
    private void showNewListDialog() {
        final ListNameInputDialog dlg = new ListNameInputDialog(getContext());
        dlg.setOnOkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dlg.getInput().trim().equals("")) {
                    dlg.setInputError("输入为空");
                    return;
                }

                AppDB.get(getContext()).addPlayList(dlg.getInput(), new ResultCallback<Integer>() {
                    @Override
                    public void onResult(Integer result) {
                        switch (result) {
                            case 0: {
                                ViewTool.show("新建列表失败");
                                dlg.dismiss();
                            }
                            break;

                            case 1: {
                                ViewTool.show("新建列表成功");
                                dlg.dismiss();
                            }
                            break;

                            case -1: {
                                dlg.setInputError("列表名已存在");
                            }
                            break;
                        }
                    }
                });
            }
        });
        dlg.show();
    }

    public static class ListNameInputDialog extends Dialog {
        @BindView(R.id.et_input)
        EditText etInput;
        @BindView(R.id.btn_ok)
        Button btnOk;
        @BindView(R.id.btn_cancel)
        Button btnCancel;
        @BindView(R.id.tv_dlg_title)
        TextView tvTitle;

        public ListNameInputDialog(Context context) {
            super(context, android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dlg_input_listname);
            ButterKnife.bind(this);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel();
                }
            });
        }

        public void setTitle(String title) {
            tvTitle.setText(title);
        }

        public String getInput() {
            return etInput.getText().toString();
        }

        public void setInputError(String error) {
            etInput.setError(error);
        }

        public void setInput(String input) {
            etInput.setText(input);
        }

        public void setOnOkClickListener(View.OnClickListener onClickListener) {
            btnOk.setOnClickListener(onClickListener);
        }
    }
}
