package com.sanron.sunmusic.fragments.MySongFrag;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.DataListAdapter;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.task.AddPlayListTask;
import com.sanron.sunmusic.task.DelPlayListTask;
import com.sanron.sunmusic.task.GetPlayListsTask;
import com.sanron.sunmusic.task.UpdatePlayListNameTask;
import com.sanron.sunmusic.utils.T;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListFrag extends BaseListFrag<PlayList> {


    public static final String TAG = "PlayListFrag";

    public PlayListFrag(int layout) {
        super(layout,new String[]{DBHelper.TABLE_PLAYLIST,DBHelper.TABLE_LISTSONGS,DBHelper.TABLE_SONG});
    }

    public static PlayListFrag newInstance() {
        return new PlayListFrag(LAYOUT_LINEAR);
    }

    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {
        PlayList playList = mAdapter.getItem(position);
        holder.tvText1.setText(playList.getName());
        holder.tvText2.setText(playList.getSongNum() + "首歌曲");
        holder.ivPicture.setVisibility(View.GONE);
    }

    @Override
    public void refreshData() {
        new GetPlayListsTask() {
            @Override
            protected void onPostExecute(List<PlayList> playLists) {
                mAdapter.setData(playLists);
            }
        }.execute();
    }

    public static class ClickListEvent {
        private PlayList playList;

        public PlayList getPlayList() {
            return playList;
        }

        public void setPlayList(PlayList playList) {
            this.playList = playList;
        }
    }



    @Override
    public void onItemClick(View view, int position) {
        List<PlayList> playLists = mAdapter.getData();
        ClickListEvent event = new ClickListEvent();
        event.setPlayList(playLists.get(position));
        EventBus.getDefault().post(event);
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
                                    T.show(getContext(), "删除失败");
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
                                    T.show(getContext(), "修改成功");
                                    dlg.dismiss();
                                } else if (num == 0) {
                                    T.show(getContext(), "修改失败");
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
        inflater.inflate(R.menu.option_menu_playlistfrag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_new_playlist: {
                final ListNameInputDialog dlg = new ListNameInputDialog(getContext());
                dlg.setOnOkClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.setTitle("新建播放列表");
                        if (dlg.getInput().trim().equals("")) {
                            dlg.setInputError("输入为空");
                            return;
                        }

                        new AddPlayListTask() {
                            @Override
                            protected void onPostExecute(Integer num) {
                                if (num > 0) {
                                    T.show(getContext(), "新建列表成功");
                                    dlg.cancel();
                                } else if (num == 0) {
                                    T.show(getContext(), "新建列表失败");
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
