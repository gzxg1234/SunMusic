package com.sanron.sunmusic.fragments.MySongFrag;

import android.content.Context;
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
import android.widget.Toast;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.PlayListAdapter;
import com.sanron.sunmusic.db.PlayListProvider;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.task.AddPlayListTask;
import com.sanron.sunmusic.task.DeletePlayListTask;
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
public class PlayListFrag extends BaseFragment implements Observer {

    private RecyclerView mListPlayLists;
    private PlayListAdapter mPlayListsAdapter;
    private List<PlayList> mPlayLists;

    public static final String TAG = "PlayListFrag";

    public static PlayListFrag newInstance() {
        return new PlayListFrag();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        PlayListProvider.instance().addObserver(this);
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
                new PlayListMenu(getContext(),view,playList).show();
            }
        });
        update(null, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PlayListProvider.instance().deleteObserver(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.frag_playlist, null);
        mListPlayLists = $(R.id.list_playlist);
        mListPlayLists.setAdapter(mPlayListsAdapter);
        mListPlayLists.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return contentView;
    }

    @Override
    public void update(Observable observable, Object data) {
        new GetPlayListsTask() {
            @Override
            protected void onPostData(List<PlayList> playLists) {
                mPlayListsAdapter.setData(playLists);
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

    public static class PlayListMenu extends PopupMenu implements PopupMenu.OnMenuItemClickListener {
        private PlayList mPlayList;
        private Context mContext;
        public PlayListMenu(Context context, View anchor,PlayList playList) {
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
                    new DeletePlayListTask() {
                        @Override
                        protected void onPostData(Integer integer) {
                            if (integer <= 0) {
                                T.show(mContext,"删除失败");
                            }
                        }
                    }.execute(mPlayList.getId());
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
                            if(dlg.getInput().trim().equals("")){
                                dlg.setInputError("输入为空");
                                return;
                            }

                            PlayList update = new PlayList();
                            update.setName(input);
                            update.setId(mPlayList.getId());
                            new UpdatePlayListNameTask() {
                                @Override
                                protected void onPostData(Integer num) {
                                    if(num == -1){
                                        dlg.setInputError("列表名已存在");
                                    }else if(num == 0){
                                        T.show(mContext,"修改失败");
                                        dlg.dismiss();
                                    }else{
                                        T.show(mContext,"修改成功");
                                        dlg.dismiss();
                                    }
                                }
                            }.execute(update);
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
                        if(dlg.getInput().trim().equals("")){
                            dlg.setInputError("输入为空");
                            return;
                        }

                        new AddPlayListTask() {
                            @Override
                            protected void onPostData(Integer num) {
                                if(num == 0){
                                    Toast.makeText(getContext(),"添加失败",Toast.LENGTH_SHORT).show();
                                }else if(num == -1){
                                    Toast.makeText(getContext(),"列表名已存在",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }.execute(dlg.getInput());
                        dlg.dismiss();
                    }
                });
                dlg.show();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

}
