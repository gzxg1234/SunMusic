package com.sanron.music.fragments.pagermymusic;

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

import com.sanron.music.R;
import com.sanron.music.adapter.MusicItemAdapter;
import com.sanron.music.common.ViewTool;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.bean.Music;
import com.sanron.music.db.bean.PlayList;
import com.sanron.music.service.IPlayer;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.task.DeleteTask;
import com.sanron.music.task.QueryListMemberDataTask;
import com.sanron.music.task.QueryTask;
import com.sanron.music.view.AddSongToListWindow;

import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

/**
 * Created by sanron on 16-3-28.
 */
public class ListMusicFragment extends BaseDataFragment implements Observer, CompoundButton.OnCheckedChangeListener, IPlayer.OnPlayStateChangeListener {


    protected MusicItemAdapter mAdapter;
    protected LinearLayout mCheckBar;
    protected RecyclerView mLvData;
    protected PopupWindow mMusicOperator;
    protected View mOpAddToList;
    protected View mOpAddToQueue;
    protected View mOpDelete;
    protected TextView mTvCheckedNum;
    protected CheckBox mCbCheckedAll;
    private PlayList mPlayList;

    public static final String TAG = ListMusicFragment.class.getSimpleName();

    public static final String ARG_PLAY_LIST = "play_list";

    public static ListMusicFragment newInstance(PlayList list) {
        ListMusicFragment listMusicFragment = new ListMusicFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLAY_LIST, list);
        listMusicFragment.setArguments(args);
        return listMusicFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mPlayList = (PlayList) getArguments().get(ARG_PLAY_LIST);
        }
        setObserveTable(DBHelper.ListMember.TABLE);

        mAdapter = new MusicItemAdapter(getContext());
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_list_music, container, false);
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        mLvData = $(R.id.lv_data);
        mCheckBar = $(R.id.ll_multi_mode_bar);
        mTvCheckedNum = $(R.id.tv_checked_num);
        mCbCheckedAll = $(R.id.cb_checked_all);

        initWindow();
        mAdapter.setFirstBindView(true);
        mAdapter.setOnItemClickListener(new MusicItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                if (mAdapter.isMultiMode()) {
                    mAdapter.setItemChecked(position, !mAdapter.isItemChecked(position));
                    mAdapter.notifyItemChanged(position);
                } else {
                    int playingPos = mAdapter.getPlayingPosition();
                    if (playingPos != -1
                            && position == playingPos) {
                        PlayerUtil.togglePlayPause();
                        return;
                    }
                    PlayerUtil.clearQueue();
                    PlayerUtil.enqueue(mAdapter.getData());
                    PlayerUtil.play(position);
                }
            }
        });
        mAdapter.setOnItemLongClickListener(new MusicItemAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View itemView, int position) {
                if (mAdapter.isMultiMode()) {
                    endMultiMode();
                } else {
                    startMultiMode(position);
                }
                return true;
            }
        });
        mAdapter.setOnItemCheckedListener(new MusicItemAdapter.OnItemCheckedListener() {
            @Override
            public void onItemChecked(int position, boolean isChecked) {
                mTvCheckedNum.setText("已选中" + mAdapter.getCheckedItemCount() + "项");
                if (mAdapter.getCheckedItemCount() == mAdapter.getItemCount()) {
                    mCbCheckedAll.setChecked(true);
                } else {
                    mCbCheckedAll.setChecked(false);
                }
            }
        });
        mAdapter.setOnItemMenuClickListener(new MusicItemAdapter.OnItemMenuClickListener() {
            @Override
            public void onItemMenuClick(View view, int position) {
                showMusicMenu(view, position);
            }
        });

        mCbCheckedAll.setOnCheckedChangeListener(this);

        mLvData.setItemAnimator(null);
        mLvData.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        mLvData.setAdapter(mAdapter);

        getMainActivity().addBackPressedHandler(this);
    }

    @Override
    public void onPlayerReady() {
        PlayerUtil.addPlayStateChangeListener(this);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getMainActivity().removeBackPressedHandler(this);
        PlayerUtil.removePlayStateChangeListener(this);
        if (mAdapter.isMultiMode()) {
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

        new RemoveListSongDialogBuilder(getContext(), mPlayList, checkedMusics)
                .show();
    }


    @Override
    public void loadData() {
        new QueryListMemberDataTask(mPlayList.getId()) {
            @Override
            protected void onPostExecute(List<Music> musics) {
                mAdapter.setData(musics);
                onPlayStateChange(IPlayer.STATE_PREPARING);
            }
        }.execute();
    }

    @Override
    public boolean onBackPressed() {
        if (mAdapter.isMultiMode()) {
            endMultiMode();
            return true;
        }
        return super.onBackPressed();
    }


    @Override
    public void onPlayStateChange(int state) {
        if (state == IPlayer.STATE_PREPARING
                || state == IPlayer.STATE_STOP) {
            mAdapter.notifyDataSetChanged();
        }
    }


    private void showMusicMenu(View view, int position) {
        final List<Music> musics = mAdapter.getData().subList(position, position + 1);
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
                        PlayerUtil.enqueue(musics);
                        ViewTool.show(musics.size() + "首歌曲加入队列");
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
        mMusicOperator = new PopupWindow(getContext());
        ViewGroup content = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.window_music_operator, null);
        mOpDelete = content.findViewById(R.id.btn_delete);
        mOpAddToQueue = content.findViewById(R.id.btn_add_to_queue);
        mOpAddToList = content.findViewById(R.id.btn_add_to_list);
        mMusicOperator.setContentView(content);
        mMusicOperator.setBackgroundDrawable(new ColorDrawable(0));
        mMusicOperator.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mMusicOperator.setHeight(getContext().getResources().getDimensionPixelSize(R.dimen.small_player_height));
        mMusicOperator.setFocusable(false);

        mOpDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteOperator(getCheckedMusics());
                endMultiMode();
            }
        });

        mOpAddToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddToListWindow(getCheckedMusics());
                endMultiMode();
            }
        });

        mOpAddToQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Music> checkedMusics = getCheckedMusics();
                PlayerUtil.enqueue(checkedMusics);
                endMultiMode();
                ViewTool.show(checkedMusics.size() + "首歌曲加入队列");
            }
        });

        mMusicOperator.setAnimationStyle(R.style.MyWindowAnim);
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
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            if (mAdapter.isItemChecked(i)) {
                checkedMusics.add(mAdapter.getItem(i));
            }
        }
        return checkedMusics;
    }


    /**
     * 开始多选模式
     */
    protected void startMultiMode(int position) {
        mAdapter.setMultiMode(true);
        mAdapter.setItemChecked(position, true);
        mAdapter.notifyDataSetChanged();

        mCheckBar.setVisibility(View.VISIBLE);
        mMusicOperator.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    protected void endMultiMode() {
        mAdapter.setMultiMode(false);
        mAdapter.notifyDataSetChanged();

        mMusicOperator.dismiss();
        mCheckBar.setVisibility(View.GONE);
        mCbCheckedAll.setChecked(false);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_checked_all: {
                if (mAdapter.isMultiMode()) {
                    if (isChecked
                            || mAdapter.getCheckedItemCount() == mAdapter.getItemCount()) {
                        for (int i = 0; i < mAdapter.getItemCount(); i++) {
                            mAdapter.setItemChecked(i, isChecked);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
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
                ViewTool.show("移除" + deleteCount + "首歌曲");
            } else {
                ViewTool.show("移除失败");
            }
            mProgressDialog.dismiss();
        }
    }
}
