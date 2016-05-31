package com.sanron.ddmusic.fragments.mymusic;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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

import com.sanron.ddmusic.R;
import com.sanron.ddmusic.adapter.MusicAdapter;
import com.sanron.ddmusic.common.ViewTool;
import com.sanron.ddmusic.db.AppDB;
import com.sanron.ddmusic.db.PlayListHelper;
import com.sanron.ddmusic.db.ResultCallback;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;
import com.sanron.ddmusic.playback.Player;
import com.sanron.ddmusic.service.PlayUtil;
import com.sanron.ddmusic.view.AddSongToListWindow;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sanron on 16-3-28.
 */
public class ListMusicFragment extends BaseDataFragment implements CompoundButton.OnCheckedChangeListener, Player.OnPlayStateChangeListener {


    @BindView(R.id.ll_multi_mode_bar)
    LinearLayout mCheckBar;
    @BindView(R.id.lv_data)
    RecyclerView mLvData;
    @BindView(R.id.tv_checked_num)
    TextView mTvCheckedNum;
    @BindView(R.id.cb_checked_all)
    CheckBox mCbCheckedAll;

    PopupWindow mMusicOperator;
    View mOpAddToList;
    View mOpAddToQueue;
    View mOpDelete;
    protected MusicAdapter mAdapter;
    private PlayList mPlayList;

    public static final String TAG = ListMusicFragment.class.getSimpleName();

    public static final String ARG_PLAY_LIST = "play_list";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra("id", 0);
            if (id == mPlayList.getId()) {
                loadData();
            }
        }
    };

    public static ListMusicFragment newInstance(PlayList list) {
        ListMusicFragment listMusicFragment = new ListMusicFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLAY_LIST, list);
        listMusicFragment.setArguments(args);
        return listMusicFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mPlayList = (PlayList) getArguments().get(ARG_PLAY_LIST);
        }
        mAdapter = new MusicAdapter(getContext());
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mReceiver,
                        new IntentFilter(AppDB.tableChangeAction(PlayListHelper.Columns.TABLE)));
    }

    @Override
    public int getViewResId() {
        return R.layout.layout_list_music;
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        initWindow();
        mAdapter.setFirstBindView(true);
        mAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                if (mAdapter.isMultiMode()) {
                    mAdapter.setItemChecked(position, !mAdapter.isItemChecked(position));
                    mAdapter.notifyItemChanged(position);
                } else {
                    int playingPos = mAdapter.getPlayingPosition();
                    if (playingPos != -1
                            && position == playingPos) {
                        PlayUtil.togglePlayPause();
                        return;
                    }
                    PlayUtil.clearQueue();
                    PlayUtil.enqueue(mAdapter.getData());
                    PlayUtil.play(position);
                }
            }
        });
        mAdapter.setOnItemLongClickListener(new MusicAdapter.OnItemLongClickListener() {
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
        mAdapter.setOnItemCheckedListener(new MusicAdapter.OnItemCheckedListener() {
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
        mAdapter.setOnItemMenuClickListener(new MusicAdapter.OnItemMenuClickListener() {
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
        PlayUtil.addPlayStateChangeListener(this);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getMainActivity().removeBackPressedHandler(this);
        PlayUtil.removePlayStateChangeListener(this);
        if (mAdapter.isMultiMode()) {
            endMultiMode();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mReceiver);
    }

    /**
     * 删除动作
     */
    protected void onDeleteOperator(final List<Music> checkedMusics) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(mPlayList.getTitle());
        if (checkedMusics.size() == 1) {
            builder.setMessage("移除歌曲 \"" + checkedMusics.get(0).getTitle() + "\"?");
        } else {
            builder.setMessage("移除" + checkedMusics.size() + "首歌曲?");
        }
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppDB.get(getContext()).deleteMusicFromPlayList(mPlayList.getId(), checkedMusics,
                        new ResultCallback<Integer>() {
                            @Override
                            public void onResult(Integer result) {
                                ViewTool.show("移除" + result + "首歌曲");
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


    @Override
    public void loadData() {
        AppDB.get(getContext()).getPlayListMusics(mPlayList.getId(), new ResultCallback<List<Music>>() {
            @Override
            public void onResult(List<Music> result) {
                mAdapter.setData(result);
            }
        });
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
        if (state == Player.STATE_PREPARING
                || state == Player.STATE_IDLE) {
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
                        PlayUtil.enqueue(musics);
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
        View content = LayoutInflater.from(getContext()).inflate(R.layout.window_music_operator, null);
        mOpDelete = ButterKnife.findById(content, R.id.btn_delete);
        mOpAddToQueue = ButterKnife.findById(content, R.id.btn_add_to_queue);
        mOpAddToList = ButterKnife.findById(content, R.id.btn_add_to_list);
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
                PlayUtil.enqueue(checkedMusics);
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
        SQLiteDatabase db = AppDB.get(getContext()).getWritableDatabase();
        List<PlayList> playLists = new LinkedList<>();
        playLists.addAll(PlayListHelper.getListByType(db, PlayList.TYPE_USER));
        playLists.addAll(PlayListHelper.getListByType(db, PlayList.TYPE_FAVORITE));
        new AddSongToListWindow(getActivity(),
                playLists,
                musics)
                .show(getView());
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

}
