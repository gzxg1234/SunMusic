package com.sanron.music.fragments.MyMusic;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import com.sanron.music.task.QueryTask;
import com.sanron.music.task.UpdateListNameTask;
import com.sanron.music.utils.TUtils;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListFrag extends DataFragment implements QueryTask.QueryCallback, ListItemAdapter.OnItemClickListener, ListItemAdapter.OnItemMenuClickListener {


    public static final String TAG = "PlayListFrag";
    public static final int EVENT_CLICK_LIST = 1;
    public static final String EXTRA_PLAYLIST = "playlist";
    public static final int MENU_NEW_LIST = 1;

    private RecyclerView lvPlayList;
    private ListItemAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        adapter = new ListItemAdapter(getContext());
        setObserveTable(DBHelper.List.TABLE, DBHelper.ListData.TABLE);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.layout_list, null);
        lvPlayList = $(R.id.recycler_view);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lvPlayList.setLayoutManager(new LinearLayoutManager(getContext()));
        lvPlayList.setItemAnimator(null);
        lvPlayList.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemMenuClickListener(this);
    }

    @Override
    public void refreshData() {
        new QueryTask()
                .table(DBHelper.List.TABLE)
                .selection(DBHelper.List.TYPE + "=" + DBHelper.List.TYPE_FAVORITE
                        + " or " + DBHelper.List.TYPE + "=" + DBHelper.List.TYPE_USER)
                .execute(this);
    }

    @Override
    public void onPreQuery() {
    }

    @Override
    public void onQueryFinish(Cursor cursor) {
        List<PlayList> playLists = new LinkedList<>();
        while (cursor.moveToNext()) {
            final PlayList playList = PlayList.fromCursor(cursor);
            final int posInAdapter = playLists.size();
            playLists.add(playList);
            //查listdata表得到列表包含的歌曲id
            new QueryTask()
                    .table(DBHelper.ListData.TABLE)
                    .columns(DBHelper.ListData.MUSIC_ID)
                    .selection(DBHelper.ListData.LIST_ID + "=?")
                    .selectionArgs(String.valueOf(playList.getId()))
                    .execute(new QueryTask.QueryCallback() {
                        @Override
                        public void onPreQuery() {
                        }

                        @Override
                        public void onQueryFinish(Cursor cursor) {
                            List<Long> musicIds = new LinkedList<>();
                            while (cursor.moveToNext()) {
                                musicIds.add(cursor.getLong(0));
                            }
                            playList.setMusicIds(musicIds);
                            adapter.notifyItemChanged(posInAdapter);
                        }
                    });
        }
        adapter.setData(playLists);
    }

    @Override
    public void onItemClick(View itemView, int position) {
        PlayList playList = adapter.getData().get(position);
        Intent intent = new Intent(PlayListFrag.class.getName());
        intent.putExtra("event", EVENT_CLICK_LIST);
        intent.putExtra(EXTRA_PLAYLIST, playList);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    @Override
    public void onItemMenuClick(View view, final int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        Menu menu = popupMenu.getMenu();
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_playlist, menu);
        if (adapter.getData().get(position).getType() != DBHelper.List.TYPE_USER) {
            //不是用户创建的列表，不能重命名和删除
            menu.removeItem(R.id.menu_delete_list);
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
        PlayList playList = adapter.getData().get(position);
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
        StringBuilder in = new StringBuilder();
        List<Long> musicIds = playList.getMusicIds();
        if (musicIds == null
                || musicIds.size() == 0) {
            TUtils.show(getContext(), "列表暂时没有歌曲");
        } else {
            for (Long musicId : musicIds) {
                in.append(musicId).append(",");
            }
            in.deleteCharAt(in.length() - 1);
            new QueryTask()
                    .table(DBHelper.Music.TABLE)
                    .selection(DBHelper.ID + " in(" + in + ")")
                    .execute(new QueryTask.QueryCallback() {
                        @Override
                        public void onPreQuery() {

                        }

                        @Override
                        public void onQueryFinish(Cursor cursor) {
                            List<Music> musics = new LinkedList<>();
                            while (cursor.moveToNext()) {
                                musics.add(Music.fromCursor(cursor));
                            }
                            player.clearQueue();
                            player.enqueue(musics);
                            player.play(0);
                        }
                    });
        }
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
        dlg.setInput(playList.getName());
        dlg.setOnOkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = dlg.getInput();
                if (dlg.getInput().trim().equals("")) {
                    dlg.setInputError("输入为空");
                    return;
                }

                if (dlg.getInput().equals(playList.getName())) {
                    dlg.dismiss();
                    return;
                }

                PlayList update = new PlayList();
                update.setName(input);
                update.setId(playList.getId());
                new UpdateListNameTask(update) {
                    @Override
                    protected void onPostExecute(Integer num) {
                        if (num > 0) {
                            TUtils.show(getContext(), "修改成功");
                            dlg.dismiss();
                        } else if (num == 0) {
                            TUtils.show(getContext(), "修改失败");
                            dlg.dismiss();
                        } else if (num == -1) {
                            dlg.setInputError("列表名已存在");
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
                    protected void onPostExecute(Integer insertCount) {
                        if (insertCount > 0) {
                            TUtils.show(getContext(), "新建列表成功");
                            dlg.dismiss();
                        } else if (insertCount == 0) {
                            TUtils.show(getContext(), "新建列表失败");
                            dlg.dismiss();
                        } else if (insertCount == -1) {
                            dlg.setInputError("列表名已存在");
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
            setMessage("确定删除列表\"" + playList.getName() + "\"？");
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
                            .callback(this)
                            .execute();
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
                TUtils.show(getContext(), "删除失败");
            } else {
                TUtils.show(getContext(), "删除歌单成功");
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
