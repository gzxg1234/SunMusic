package com.sanron.music.fragments.MyMusic;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.sanron.music.R;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.Music;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.service.IPlayer;
import com.sanron.music.task.QueryTask;
import com.sanron.music.utils.TUtils;
import com.sanron.music.view.AddSongToListWindow;

import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

/**
 * Created by sanron on 16-3-28.
 */
public abstract class BaseMusicFrag extends DataFragment implements Observer, CompoundButton.OnCheckedChangeListener {


    protected MusicItemAdapter adapter;
    protected LinearLayout llCheckBar;
    protected RecyclerView lvData;
    protected PopupWindow multiModeWindow;
    protected View actionAddToList;
    protected View actionAddToQueue;
    protected View actionDelete;
    protected TextView tvCheckedNum;
    protected CheckBox cbCheckedAll;

    protected ImageLoader imageLoader;
    protected DisplayImageOptions imageOptions;

    protected IPlayer.Callback callback = new IPlayer.Callback() {
        @Override
        public void onLoadedPicture(Bitmap musicPic) {

        }

        @Override
        public void onStateChange(int state) {
            if (state == IPlayer.STATE_PREPARING) {
                Music currentMusic = player.getCurrentMusic();
                List<Music> listData = adapter.getData();
                if (listData != null && listData.size() > 0) {
                    int playingPos = listData.indexOf(currentMusic);
                    adapter.setPlayingPosition(playingPos);
                }
            } else if (state == IPlayer.STATE_STOP) {
                adapter.setPlayingPosition(-1);
            }
        }

        @Override
        public void onBufferingUpdate(int bufferedPosition) {

        }
    };

    protected RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE: {

                    for (int i = 0; i < lvData.getChildCount(); i++) {
                        View child = lvData.getChildAt(i);
                        MusicItemAdapter.MusicItemHolder holder = (MusicItemAdapter.MusicItemHolder) lvData.getChildViewHolder(child);
                        if (holder.ivPicture.getTag() != null) {
                            Music music = adapter.getItem(holder.getAdapterPosition());
                            if (!TextUtils.isEmpty(music.getPic())) {
                                imageLoader.displayImage("file://" + music.getPic(),
                                        holder.ivPicture, imageOptions);
                                holder.ivPicture.setTag(null);
                            }
                        }
                    }
                }
                break;

                default: {
                    adapter.setFirstBindView(false);
                }
                break;
            }
        }
    };

    /**
     * 删除动作
     */
    protected abstract void onMultiActionDeleteClick(List<Music> checkedMusics);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        imageLoader = ImageLoader.getInstance();
        imageOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnFail(R.mipmap.default_small_song_pic)
                .cacheInMemory(true)
                .build();
        adapter = new MusicItemAdapter(getContext());
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.frag_music, null);
        lvData = $(R.id.lv_data);
        llCheckBar = $(R.id.ll_multi_mode_bar);
        tvCheckedNum = $(R.id.tv_checked_num);
        cbCheckedAll = $(R.id.cb_checked_all);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        cbCheckedAll.setOnCheckedChangeListener(this);
        lvData.setItemAnimator(null);
        lvData.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        lvData.setAdapter(adapter);
        lvData.addOnScrollListener(onScrollListener);
        player.addCallback(callback);
        initWindow();
        callback.onStateChange(IPlayer.STATE_PREPARING);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        player.removeCallback(callback);
        if (adapter.isMultiMode()) {
            //关闭
            endMultiMode();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        DataProvider.instance().deleteObserver(this);
    }


    private void initWindow() {
        multiModeWindow = new PopupWindow(getContext());
        ViewGroup content = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.window_multi_action, null);
        actionDelete = content.findViewById(R.id.btn_delete);
        actionAddToQueue = content.findViewById(R.id.btn_add_to_queue);
        actionAddToList = content.findViewById(R.id.btn_add_to_list);
        multiModeWindow.setContentView(content);
        multiModeWindow.setBackgroundDrawable(new ColorDrawable(0));
        multiModeWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        multiModeWindow.setHeight(getContext().getResources().getDimensionPixelSize(R.dimen.small_player_height));
        multiModeWindow.setFocusable(false);

        actionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMultiActionDeleteClick(getCheckedMusics());
                endMultiMode();
            }
        });

        actionAddToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddToListWindow();
                endMultiMode();
            }
        });

        actionAddToQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Music> checkedMusics = getCheckedMusics();
                player.enqueue(checkedMusics);
                TUtils.show(getContext(), checkedMusics.size() + "首歌曲加入队列");
                endMultiMode();
            }
        });

        multiModeWindow.setAnimationStyle(R.style.MyWindowAnim);
    }


    /**
     * 显示添加歌曲到列表的窗口
     */
    private void showAddToListWindow() {
        final List<Music> checkedMusics = getCheckedMusics();
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
                                .show(contentView);
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
        multiModeWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    protected void endMultiMode() {
        adapter.setMultiMode(false);
        adapter.notifyDataSetChanged();

        multiModeWindow.dismiss();
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
     * 音乐适配
     */
    public static class MusicItemAdapter extends RecyclerView.Adapter<MusicItemAdapter.MusicItemHolder> {

        private List<Music> data;
        private Context context;
        private SparseBooleanArray checkStates;
        private int checkedItemCount;
        private boolean isMultiMode = false;
        private boolean isFirstBindView = true;
        private int playingPosition = -1;//
        private ImageLoader imageLoader;
        private DisplayImageOptions imageOptions;
        private MemoryCache memoryCache;
        private int playingTextColor;//播放中文字颜色
        private int normalTitleTextColor;//正常title颜色
        private int normalArtistTextColor;//正常artist颜色

        public MusicItemAdapter(Context context) {
            this.context = context;
            imageLoader = ImageLoader.getInstance();
            memoryCache = imageLoader.getMemoryCache();
            imageOptions = new DisplayImageOptions.Builder()
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .showImageOnFail(R.mipmap.default_small_song_pic)
                    .cacheInMemory(true)
                    .build();
            Resources resources = context.getResources();
            playingTextColor = resources.getColor(R.color.colorAccent);
            normalTitleTextColor = resources.getColor(R.color.textColorPrimary);
            normalArtistTextColor = resources.getColor(R.color.textColorSecondary);
        }

        public boolean isMultiMode() {
            return isMultiMode;
        }

        public Music getItem(int position) {
            if (data != null) {
                return data.get(position);
            }
            return null;
        }

        public void setMultiMode(boolean multiMode) {
            if (this.isMultiMode == multiMode) {
                return;
            }

            if (multiMode) {
                if (checkStates == null) {
                    checkStates = new SparseBooleanArray();
                } else {
                    checkStates.clear();
                    checkedItemCount = 0;
                }
            }
            isMultiMode = multiMode;
        }

        public void setData(List<Music> data) {
            if (this.data == data) {
                return;
            }

            this.data = data;
            notifyDataSetChanged();
        }

        public int getPlayingPosition() {
            return playingPosition;
        }

        public List<Music> getData() {
            return data;
        }

        public void setFirstBindView(boolean firstBindView) {
            isFirstBindView = firstBindView;
        }

        //查找缓存
        private Bitmap getMemoryBitmap(String uri, ImageView iv) {
            String key = MemoryCacheUtils.generateKey(uri, new ImageSize(iv.getWidth(), iv.getHeight()));
            List<Bitmap> bmps = MemoryCacheUtils.findCachedBitmapsForImageUri(key, memoryCache);
            if (bmps.size() > 0) {
                return bmps.get(0);
            }
            return null;
        }

        public boolean isItemChecked(int position) {
            if (isMultiMode && checkStates != null) {
                return checkStates.get(position);
            }
            return false;
        }

        public void setItemChecked(int position, boolean checked) {
            if (isMultiMode && checkStates != null) {
                boolean oldChecked = checkStates.get(position);
                if (checked != oldChecked) {
                    checkStates.put(position, checked);
                    if (checked) {
                        checkedItemCount++;
                    } else {
                        checkedItemCount--;
                    }
                    if (onItemCheckedListener != null) {
                        onItemCheckedListener.onItemChecked(position, checked);
                    }
                }
            }
        }

        @Override
        public MusicItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_music_item, parent, false);
            return new MusicItemHolder(view);
        }

        public void setPlayingPosition(int position) {
            final int oldPos = playingPosition;
            playingPosition = position;
            if (oldPos != -1) {
                notifyItemChanged(oldPos);
            }
            if (playingPosition != -1) {
                notifyItemChanged(playingPosition);
            }
        }

        @Override
        public void onBindViewHolder(MusicItemHolder holder, final int position) {
            Music music = data.get(position);
            holder.ivPicture.setTag(null);
            String pic = music.getPic();
            if (!TextUtils.isEmpty(pic)) {
                if (isFirstBindView) {
                    //listview第一次加载子view，加载图片
                    imageLoader.displayImage("file://" + music.getPic(),
                            holder.ivPicture,
                            imageOptions);
                } else {
                    //如果内存中缓存了图片，就显示
                    Bitmap cacheBmp = getMemoryBitmap("file://" + pic, holder.ivPicture);
                    if (cacheBmp != null) {
                        holder.ivPicture.setImageBitmap(cacheBmp);
                    } else {
                        holder.ivPicture.setImageResource(R.mipmap.default_small_song_pic);
                        //设置标识已加载图片不
                        holder.ivPicture.setTag(new Object());
                    }
                }
            } else {
                holder.ivPicture.setImageResource(R.mipmap.default_small_song_pic);
            }

            String artist = music.getArtist();
            if ("<unknown>".equals(artist) || TextUtils.isEmpty(artist)) {
                artist = "未知歌手";
            }
            holder.tvTitle.setText(music.getTitle());
            holder.tvArtist.setText(artist);
            if (position == playingPosition) {
                holder.tvTitle.setTextColor(playingTextColor);
                holder.tvArtist.setTextColor(playingTextColor);
            } else {
                holder.tvTitle.setTextColor(normalTitleTextColor);
                holder.tvArtist.setTextColor(normalArtistTextColor);
            }

            if (isMultiMode) {
                holder.ibtnMenu.setVisibility(View.GONE);
                holder.cbSelect.setVisibility(View.VISIBLE);
                holder.cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setItemChecked(position, isChecked);
                    }
                });
                holder.cbSelect.setChecked(isItemChecked(position));
            } else {
                holder.ibtnMenu.setVisibility(View.VISIBLE);
                holder.cbSelect.setVisibility(View.GONE);
            }
        }

        public int getCheckedItemCount() {
            return checkedItemCount;
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        public class MusicItemHolder extends RecyclerView.ViewHolder {
            ImageView ivPicture;
            TextView tvTitle;
            TextView tvArtist;
            ImageButton ibtnMenu;
            CheckBox cbSelect;

            public MusicItemHolder(final View itemView) {
                super(itemView);
                ivPicture = (ImageView) itemView.findViewById(R.id.top_image);
                tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
                tvArtist = (TextView) itemView.findViewById(R.id.tv_artist);
                cbSelect = (CheckBox) itemView.findViewById(R.id.cb_select);
                ibtnMenu = (ImageButton) itemView.findViewById(R.id.ibtn_item_menu);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(itemView, getAdapterPosition());
                        }
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (onItemLongClickListener != null) {
                            return onItemLongClickListener.onItemLongClick(itemView, getAdapterPosition());
                        }
                        return false;
                    }
                });

            }

        }


        private OnItemCheckedListener onItemCheckedListener;
        private OnItemClickListener onItemClickListener;
        private OnItemLongClickListener onItemLongClickListener;

        public void setOnItemCheckedListener(OnItemCheckedListener onItemCheckedListener) {
            this.onItemCheckedListener = onItemCheckedListener;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
            this.onItemLongClickListener = onItemLongClickListener;
        }

        public interface OnItemCheckedListener {
            void onItemChecked(int position, boolean isChecked);
        }

        public interface OnItemLongClickListener {
            boolean onItemLongClick(View itemView, int position);
        }

        public interface OnItemClickListener {
            void onItemClick(View itemView, int position);
        }

    }
}
