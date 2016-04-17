package com.sanron.music.fragments.MyMusic;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.adapter.MusicItemAdapter;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.Music;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.service.IPlayer;
import com.sanron.music.task.DeleteTask;
import com.sanron.music.task.QueryListMemberDataTask;
import com.sanron.music.task.QueryTask;
import com.sanron.music.utils.T;
import com.sanron.music.view.AddSongToListWindow;

import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

/**
 * Created by sanron on 16-3-28.
 */
public class ListMusicFrag extends BaseDataFragment implements Observer, CompoundButton.OnCheckedChangeListener, IPlayer.OnPlayStateChangeListener {


    protected MusicItemAdapter adapter;
    protected LinearLayout llCheckBar;
    protected RecyclerView lvData;
    protected PopupWindow musicOperator;
    protected View opAddToList;
    protected View opAddToQueue;
    protected View opDelete;
    protected TextView tvCheckedNum;
    protected CheckBox cbCheckedAll;

    protected ImageLoader imageLoader;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            playList = (PlayList) getArguments().get(ARG_PLAY_LIST);
        }
        setObserveTable(DBHelper.ListMember.TABLE);

        imageLoader = ImageLoader.getInstance();
        adapter = new MusicItemAdapter(getContext());
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_list, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.addBackPressedHandler(this);
        lvData = $(R.id.lv_data);
        llCheckBar = $(R.id.ll_multi_mode_bar);
        tvCheckedNum = $(R.id.tv_checked_num);
        cbCheckedAll = $(R.id.cb_checked_all);

        initWindow();
        adapter.setFirstBindView(true);
        adapter.setOnItemClickListener(new MusicItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                if (adapter.isMultiMode()) {
                    adapter.setItemChecked(position, !adapter.isItemChecked(position));
                    adapter.notifyItemChanged(position);
                } else {
                    int playingPos = adapter.getPlayingPosition();
                    if (position == playingPos) {
                        player.togglePlayPause();
                        return;
                    }
                    player.clearQueue();
                    player.enqueue(adapter.getData());
                    player.play(position);
                }
            }
        });
        adapter.setOnItemLongClickListener(new MusicItemAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View itemView, int position) {
                if (adapter.isMultiMode()) {
                    endMultiMode();
                } else {
                    startMultiMode(position);
                }
                return true;
            }
        });
        adapter.setOnItemCheckedListener(new MusicItemAdapter.OnItemCheckedListener() {
            @Override
            public void onItemChecked(int position, boolean isChecked) {
                tvCheckedNum.setText("已选中" + adapter.getCheckedItemCount() + "项");
                if (adapter.getCheckedItemCount() == adapter.getItemCount()) {
                    cbCheckedAll.setChecked(true);
                } else {
                    cbCheckedAll.setChecked(false);
                }
            }
        });
        adapter.setOnItemMenuClickListener(new MusicItemAdapter.OnItemMenuClickListener() {
            @Override
            public void onItemMenuClick(View view, int position) {
                showMusicMenu(view, position);
            }
        });
        cbCheckedAll.setOnCheckedChangeListener(this);
        lvData.setItemAnimator(null);
        lvData.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        lvData.setAdapter(adapter);
        player.addPlayStateChangeListener(this);
        onPlayStateChange(IPlayer.STATE_PREPARING);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainActivity.removeBackPressedHandler(this);
        player.removePlayStateChangeListener(this);
        if (adapter.isMultiMode()) {
            //关闭
            endMultiMode();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * 删除动作
     */
    protected void onDeleteOperator(List<Music> checkedMusics) {

        new RemoveListSongDialogBuilder(getContext(), playList, checkedMusics)
                .show();
    }


    @Override
    public void refreshData() {
        new QueryListMemberDataTask(playList.getId()) {
            @Override
            protected void onPostExecute(List<Music> musics) {
                adapter.setData(musics);
                onPlayStateChange(IPlayer.STATE_PREPARING);
            }
        }.execute();
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
    public void onPlayStateChange(int state) {
        if (state == IPlayer.STATE_PREPARING) {

            //列表中是否有播放中的歌曲
            Music currentMusic = player.getCurrentMusic();
            List<Music> listData = adapter.getData();
            if (listData != null) {
                adapter.setPlayingPosition(listData.indexOf(currentMusic));
            }
        } else if (state == IPlayer.STATE_STOP) {
            adapter.setPlayingPosition(-1);
        }
    }


    private void showMusicMenu(View view, int position) {
        final List<Music> musics = adapter.getData().subList(position, position + 1);
        PopupMenu popupMenu = new PopupMenu(getContext(), view, Gravity.BOTTOM);
        popupMenu.getMenuInflater().inflate(R.menu.song_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_add_to_list: {
                        showAddToListWindow(musics);
                    }
                    break;

                    case R.id.menu_add_to_quque: {
                        player.enqueue(musics);
                        T.show(musics.size() + "首歌曲加入队列");
                    }
                    break;

                    case R.id.menu_delete: {
                        onDeleteOperator(musics);
                    }
                    break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void initWindow() {
        musicOperator = new PopupWindow(getContext());
        ViewGroup content = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.window_music_operator, null);
        opDelete = content.findViewById(R.id.btn_delete);
        opAddToQueue = content.findViewById(R.id.btn_add_to_queue);
        opAddToList = content.findViewById(R.id.btn_add_to_list);
        musicOperator.setContentView(content);
        musicOperator.setBackgroundDrawable(new ColorDrawable(0));
        musicOperator.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        musicOperator.setHeight(getContext().getResources().getDimensionPixelSize(R.dimen.small_player_height));
        musicOperator.setFocusable(false);

        opDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteOperator(getCheckedMusics());
                endMultiMode();
            }
        });

        opAddToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddToListWindow(getCheckedMusics());
                endMultiMode();
            }
        });

        opAddToQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Music> checkedMusics = getCheckedMusics();
                player.enqueue(checkedMusics);
                endMultiMode();
                T.show(checkedMusics.size() + "首歌曲加入队列");
            }
        });

        musicOperator.setAnimationStyle(R.style.MyWindowAnim);
    }


    /**
     * 显示添加歌曲到列表的窗口
     */
    private void showAddToListWindow(List<Music> musics) {
        final List<Music> checkedMusics = musics;
        new QueryTask()
                .table(DBHelper.List.TABLE)
                .selection(DBHelper.List.TYPE + "=? or " + DBHelper.List.TYPE + "=?")
                .selectionArgs(DBHelper.List.TYPE_USER + "",
                        DBHelper.List.TYPE_FAVORITE + "")
                .execute(new QueryTask.QueryCallback() {
                    @Override
                    public void onPreQuery() {
                    }

                    @Override
                    public void onQueryFinish(Cursor cursor) {
                        List<PlayList> playLists = new LinkedList<>();
                        while (cursor.moveToNext()) {
                            PlayList playList = PlayList.fromCursor(cursor);
                            playLists.add(playList);
                        }
                        new AddSongToListWindow(getActivity(),
                                playLists,
                                checkedMusics)
                                .show(getView());
                    }
                });
    }

    /**
     * 获取选中的音乐
     *
     * @return
     */
    private List<Music> getCheckedMusics() {
        List<Music> checkedMusics = new LinkedList<>();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            if (adapter.isItemChecked(i)) {
                checkedMusics.add(adapter.getItem(i));
            }
        }
        return checkedMusics;
    }


    /**
     * 开始多选模式
     */
    protected void startMultiMode(int position) {
        adapter.setMultiMode(true);
        adapter.setItemChecked(position, true);
        adapter.notifyDataSetChanged();

        llCheckBar.setVisibility(View.VISIBLE);
        musicOperator.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    protected void endMultiMode() {
        adapter.setMultiMode(false);
        adapter.notifyDataSetChanged();

        musicOperator.dismiss();
        llCheckBar.setVisibility(View.GONE);
        cbCheckedAll.setChecked(false);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_checked_all: {
                if (adapter.isMultiMode()) {
                    if (isChecked
                            || adapter.getCheckedItemCount() == adapter.getItemCount()) {
                        for (int i = 0; i < adapter.getItemCount(); i++) {
                            adapter.setItemChecked(i, isChecked);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            break;
        }
    }


    /**
     * 移除歌曲对话框
     */
    public class RemoveListSongDialogBuilder extends AlertDialog.Builder implements DialogInterface.OnClickListener, DeleteTask.DeleteCallback {
        private PlayList mPlayList;
        private List<Music> mRemoveMusics;
        private ProgressDialog mProgressDialog;

        public RemoveListSongDialogBuilder(Context context, final PlayList playList, List<Music> removeSongs) {
            super(context);
            this.mPlayList = playList;
            this.mRemoveMusics = removeSongs;
            this.mProgressDialog = new ProgressDialog(context);

            setTitle(playList.getTitle());
            if (mRemoveMusics.size() == 1) {
                setMessage("移除歌曲 \"" + mRemoveMusics.get(0).getTitle() + "\"?");
            } else {
                setMessage("移除" + mRemoveMusics.size() + "首歌曲?");
            }
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
                    String in = createIn();
                    new DeleteTask()
                            .table(DBHelper.ListMember.TABLE)
                            .where(DBHelper.ListMember.LIST_ID + "=" + mPlayList.getId()
                                    + " and " + DBHelper.ListMember.MUSIC_ID + " in(" + in + ")")
                            .execute(this);
                }
                break;
            }
        }

        private String createIn() {
            StringBuilder in = new StringBuilder();
            for (Music music : mRemoveMusics) {
                in.append(music.getId()).append(",");
            }
            if (in.length() > 0) {
                in.deleteCharAt(in.length() - 1);
            }
            return in.toString();
        }

        @Override
        public void onPreDelete() {
            mProgressDialog.show();
        }

        @Override
        public void onDeleteFinish(int deleteCount) {
            if (deleteCount > 0) {
                T.show("移除" + deleteCount + "首歌曲");
            } else {
                T.show("移除失败");
            }
            mProgressDialog.dismiss();
        }
    }
}
