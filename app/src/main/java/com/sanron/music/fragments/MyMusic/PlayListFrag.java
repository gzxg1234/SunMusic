package com.sanron.music.fragments.MyMusic;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.adapter.DataListAdapter;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.task.AddPlayListTask;
import com.sanron.music.task.DelPlayListTask;
import com.sanron.music.task.GetPlayListTask;
import com.sanron.music.task.UpdatePlayListNameTask;
import com.sanron.music.utils.TUtils;

import java.util.List;


/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListFrag extends BaseDataFrag<PlayList> {


    public static final String TAG = "PlayListFrag";
    public static final int EVENT_CLICK_LIST = 1;
    public static final String EXTRA_PLAYLIST = "playlist";
    public static final int MENU_NEW_LIST = 1;

    public PlayListFrag() {
        super(LAYOUT_LINEAR,new String[]{DBHelper.TABLE_PLAYLIST,DBHelper.TABLE_LISTMUSIC,DBHelper.TABLE_MUSIC});
        setShowItemPicture(false);
    }

    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {
        PlayList playList = mAdapter.getItem(position);
        holder.tvText1.setText(playList.getName());
    }

    @Override
    public void refreshData() {
        new GetPlayListTask() {
            @Override
            protected void onPostExecute(List<PlayList> playLists) {
                mAdapter.setData(playLists);
            }
        }.execute();
    }

    @Override
    public void onItemClick(View view, int position) {
        PlayList playList = mAdapter.getData().get(position);
        Intent intent = new Intent(PlayListFrag.class.getName());
        intent.putExtra("event",EVENT_CLICK_LIST);
        intent.putExtra(EXTRA_PLAYLIST,playList);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }


    @Override
    public void onCreatePopupMenu(Menu menu, MenuInflater inflater,int position) {
        inflater.inflate(R.menu.popup_menu_playlist,menu);
        PlayList playList = mAdapter.getItem(position);
        if (playList.getType() != PlayList.TYPE_USER) {
            //不是用户创建的列表，不能重命名和删除
            menu.removeItem(R.id.menu_delete_list);
            menu.removeItem(R.id.menu_rename_list);
        }
    }

    @Override
    public void onPopupItemSelected(MenuItem item, int position) {
        final PlayList playList = mAdapter.getItem(position);
        switch (item.getItemId()) {

            case R.id.menu_delete_list: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("删除列表");
                builder.setMessage("确定删除列表？");
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        new DelPlayListTask() {
                            @Override
                            protected void onPostExecute(Integer integer) {
                                if (integer <= 0) {
                                    TUtils.show(getContext(), "删除失败");
                                }
                                dialog.dismiss();
                            }
                        }.execute(playList.getId());
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
            break;

            case R.id.menu_rename_list: {
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

                        PlayList update = new PlayList();
                        update.setName(input);
                        update.setId(playList.getId());
                        new UpdatePlayListNameTask(update) {
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
            break;

            case R.id.menu_play_list: {

            }
            break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        menu.add(ALTERNATIVE_GROUP_ID, MENU_NEW_LIST,Menu.NONE,"新建列表");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_NEW_LIST: {
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
                            protected void onPostExecute(Integer num) {
                                if (num > 0) {
                                    TUtils.show(getContext(), "新建列表成功");
                                    dlg.cancel();
                                } else if (num == 0) {
                                    TUtils.show(getContext(), "新建列表失败");
                                    dlg.cancel();
                                } else if (num == -1) {
                                    dlg.setInputError("列表名已存在");
                                }
                            }
                        }.execute(dlg.getInput());
                    }
                });
                dlg.show();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class ListNameInputDialog extends Dialog {
        private EditText etInput;
        private Button btnOk;
        private Button btnCancel;
        private TextView tvTitle;

        public ListNameInputDialog(Context context) {
            super(context);
            setCancelable(false);
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
