package com.sanron.sunmusic.fragments.MySongFrag;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.PlayListAdapter;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.task.AddPlayListTask;
import com.sanron.sunmusic.task.DelPlayListTask;
import com.sanron.sunmusic.task.GetPlayListsTask;
import com.sanron.sunmusic.task.UpdatePlayListNameTask;
import com.sanron.sunmusic.utils.T;
import com.sanron.sunmusic.window.ListNameInputDialog;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListFrag extends BaseFragment{

    private RecyclerView mListPlayLists;
    private PlayListAdapter mPlayListsAdapter;

    public static final String TAG = "PlayListFrag";

    public static PlayListFrag newInstance() {
        return new PlayListFrag();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayListsAdapter = new PlayListAdapter(getContext(), null);
        mPlayListsAdapter.setOnItemClickListener(new PlayListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                List<PlayList> playLists = mPlayListsAdapter.getData();
                ClickListEvent event = new ClickListEvent();
                event.setPlayList(playLists.get(position));
                EventBus.getDefault().post(event);
            }
        });
        mPlayListsAdapter.setOnActionClickListener(new PlayListAdapter.OnActionClickListener() {
            @Override
            public void onActionClick(View view, int actionPosition) {
                PlayList playList = mPlayListsAdapter.getData(actionPosition);
                new PlayListMenu(getContext(), view, playList).show();
            }
        });
        update(null, DBHelper.TABLE_PLAYLIST);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.frag_recycler_layout, null);
        mListPlayLists = $(R.id.recycler_view);
        mListPlayLists.setAdapter(mPlayListsAdapter);
        mListPlayLists.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return contentView;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (DBHelper.TABLE_PLAYLIST.equals(data)) {
            new GetPlayListsTask() {
                @Override
                protected void onPostExecute(List<PlayList> playLists) {
                    mPlayListsAdapter.setData(playLists);
                }
            }.execute();
        }
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

    public static class PlayListMenu extends PopupMenu implements PopupMenu.OnMenuItemClickListener {
        private PlayList mPlayList;
        private Context mContext;

        public PlayListMenu(Context context, View anchor, PlayList playList) {
            super(context, anchor);
            this.mPlayList = playList;
            this.mContext = context;
            inflate(R.menu.playlist_action);
            if (playList.getType() != PlayList.TYPE_USER) {
                //不是用户创建的列表，不能重命名和删除
                getMenu().removeItem(R.id.menu_delete_list);
                getMenu().removeItem(R.id.menu_rename_list);
            }
            setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {

                case R.id.menu_delete_list: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
                                        T.show(mContext, "删除失败");
                                    }
                                    dialog.dismiss();
                                }
                            }.execute(mPlayList.getId());
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
                    final ListNameInputDialog dlg = new ListNameInputDialog(mContext);
                    dlg.setTitle("重命名");
                    dlg.setInput(mPlayList.getName());
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
                            update.setId(mPlayList.getId());
                            new UpdatePlayListNameTask(update) {
                                @Override
                                protected void onPostExecute(Integer num) {
                                    if (num > 0) {
                                        T.show(mContext, "修改成功");
                                        dlg.dismiss();
                                    } else if (num == 0) {
                                        T.show(mContext, "修改失败");
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
            return true;
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.playlistfrag_option_menu, menu);
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

}
