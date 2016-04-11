package com.sanron.music.fragments.MyMusic;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.adapter.ListItemAdapter;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.Music;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.task.AddPlayListTask;
import com.sanron.music.task.DeleteTask;
import com.sanron.music.task.QueryListMemberDatasTask;
import com.sanron.music.task.QueryListTask;
import com.sanron.music.task.UpdateListNameTask;
import com.sanron.music.utils.T;

import java.util.List;


/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListFrag extends DataFragment implements ListItemAdapter.OnItemClickListener, ListItemAdapter.OnItemMenuClickListener {


    public static final String TAG = "PlayListFrag";
    public static final int EVENT_CLICK_LIST = 1;
    public static final String EXTRA_PLAYLIST = "playlist";
    public static final int MENU_NEW_LIST = 1;

    private RecyclerView lvPlayList;
    private ListItemAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        adapter = new ListItemAdapter(getContext());
        setObserveTable(DBHelper.List.TABLE, DBHelper.ListMember.TABLE);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_playlist, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lvPlayList = $(R.id.recycler_view);
        lvPlayList.setLayoutManager(new LinearLayoutManager(getContext()));
        lvPlayList.setItemAnimator(null);
        lvPlayList.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemMenuClickListener(this);
    }

    @Override
    public void refreshData() {
        new QueryListTask() {
            @Override
            protected void onPostExecute(List<PlayList> playLists) {
                adapter.setData(playLists);
            }
        }.execute();
    }

    @Override
    public void onItemClick(View itemView, int position) {
        PlayList playList = adapter.getItem(position);
        Intent intent = new Intent(ACTION_FRAG_EVENT);
        intent.putExtra(EXTRA_FROM, getClass().getName());
        intent.putExtra(EXTRA_EVENT, EVENT_CLICK_LIST);
        intent.putExtra(EXTRA_PLAYLIST, playList);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    @Override
    public void onItemMenuClick(View view, final int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        Menu menu = popupMenu.getMenu();
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_playlist, menu);
        int type = adapter.getItem(position).getType();
        if (type == DBHelper.List.TYPE_FAVORITE) {
            //不是用户创建的列表，不能重命名和删除
            menu.removeItem(R.id.menu_delete_list);
            menu.removeItem(R.id.menu_rename_list);
        } else if (type == DBHelper.List.TYPE_COLLECTION) {
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
        PlayList playList = adapter.getItem(position);
        switch (item.getItemId()) {
            case R.id.menu_play_list: {
                playListMusics(playList);
            }
            break;

            case R.id.menu_delete_list: {
                new DeleteConfirmDialog(getContext(), playList).show();
            }
            break;

            case R.id.menu_rename_list: {
                showRenameDialog(playList);
            }
            break;
        }
    }

    /**
     * 播放列表歌曲
     */
    private void playListMusics(final PlayList playList) {
        new QueryListMemberDatasTask(playList.getId()) {
            @Override
            protected void onPostExecute(List<Music> musics) {
                if (musics.size() == 0) {
                    T.show(getContext(), "列表暂时没有歌曲");
                } else {
                    player.clearQueue();
                    player.enqueue(musics);
                    player.play(0);
                }
            }
        }.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(ALTERNATIVE_GROUP_ID, MENU_NEW_LIST, Menu.NONE, "新建列表");
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

    /**
     * 重命名对话框
     */
    private void showRenameDialog(final PlayList playList) {
        final ListNameInputDialog dlg = new ListNameInputDialog(getContext());
        dlg.setTitle("重命名");
        dlg.setInput(playList.getTitle());
        dlg.setOnOkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = dlg.getInput();
                if (dlg.getInput().trim().equals("")) {
                    dlg.setInputError("输入为空");
                    return;
                }

                if (dlg.getInput().equals(playList.getTitle())) {
                    dlg.dismiss();
                    return;
                }

                PlayList update = new PlayList();
                update.setTitle(input);
                update.setId(playList.getId());
                new UpdateListNameTask(update) {
                    @Override
                    protected void onPostExecute(Integer result) {
                        switch (result) {
                            case FAILED: {
                                T.show(getContext(), "修改失败");
                                dlg.dismiss();
                            }
                            break;
                            case SUCCESS: {
                                T.show(getContext(), "修改成功");
                                dlg.dismiss();

                            }
                            break;
                            case EXISTS: {

                                dlg.setInputError("列表名已存在");
                            }
                            break;
                        }
                    }
                }.execute();
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

                new AddPlayListTask() {
                    @Override
                    protected void onPostExecute(Integer result) {

                        switch (result) {
                            case FAILED: {
                                T.show(getContext(), "新建列表失败");
                                dlg.dismiss();
                            }
                            break;

                            case SUCCESS: {
                                T.show(getContext(), "新建列表成功");
                                dlg.dismiss();
                            }
                            break;

                            case EXISTS: {
                                dlg.setInputError("列表名已存在");
                            }
                            break;
                        }
                    }
                }.execute(dlg.getInput());
            }
        });
        dlg.show();
    }

    /**
     * 删除确认对话框
     */
    public class DeleteConfirmDialog extends AlertDialog.Builder implements DialogInterface.OnClickListener, DeleteTask.DeleteCallback {
        private PlayList playList;

        public DeleteConfirmDialog(Context context, PlayList playList) {
            super(context);
            setTitle("删除列表");
            setMessage("确定删除列表\"" + playList.getTitle() + "\"？");
            setPositiveButton("确定", this);
            setNegativeButton("取消", this);
            this.playList = playList;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE: {
                    dialog.dismiss();
                }
                break;
                case DialogInterface.BUTTON_POSITIVE: {
                    new DeleteTask()
                            .table(DBHelper.List.TABLE)
                            .where(DBHelper.ID + "=" + playList.getId())
                            .execute(this);
                    dialog.dismiss();
                }
                break;
            }
        }

        @Override
        public void onPreDelete() {
        }

        @Override
        public void onDeleteFinish(int deleteCount) {
            if (deleteCount == 0) {
                T.show(getContext(), "删除失败");
            } else {
                T.show(getContext(), "删除歌单成功");
            }
        }
    }

    public static class ListNameInputDialog extends Dialog {
        private EditText etInput;
        private Button btnOk;
        private Button btnCancel;
        private TextView tvTitle;

        public ListNameInputDialog(Context context) {
            super(context, android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dlg_input_listname);
            tvTitle = (TextView) findViewById(R.id.tv_dlg_title);
            etInput = (EditText) findViewById(R.id.et_input);
            btnOk = (Button) findViewById(R.id.btn_ok);
            btnCancel = (Button) findViewById(R.id.btn_cancel);
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
