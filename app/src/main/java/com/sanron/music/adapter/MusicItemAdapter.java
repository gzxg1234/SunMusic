package com.sanron.music.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.sanron.music.R;
import com.sanron.music.db.model.Music;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.LrcPicResult;
import com.sanron.music.utils.MyLog;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * 音乐适配
 */
public class MusicItemAdapter extends RecyclerView.Adapter<MusicItemAdapter.MusicItemHolder> {

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
    private HashMap<Long, String> avatarUrls;

    public MusicItemAdapter(Context context) {
        this.context = context;
        imageLoader = ImageLoader.getInstance();
        memoryCache = imageLoader.getMemoryCache();
        imageOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnFail(R.mipmap.default_small_song_pic)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        Resources resources = context.getResources();
        playingTextColor = resources.getColor(R.color.colorAccent);
        normalTitleTextColor = resources.getColor(R.color.textColorPrimary);
        normalArtistTextColor = resources.getColor(R.color.textColorSecondary);
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE: {

                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
                        View child = recyclerView.getChildAt(i);
                        MusicItemHolder holder = (MusicItemHolder) recyclerView.getChildViewHolder(child);
                        if (holder.ivPicture.getTag() != null) {
                            displayAvatar(holder, holder.getAdapterPosition());
                        }
                    }
                }
                break;

                default: {
                    setFirstBindView(false);
                }
                break;
            }
        }
    };

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
        if (data != null) {
            avatarUrls = new HashMap<>(data.size());
        }
        setFirstBindView(true);
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
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(onScrollListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView.removeOnScrollListener(onScrollListener);
    }

    @Override
    public void onBindViewHolder(final MusicItemHolder holder, final int position) {
        final Music music = data.get(position);
        String artist = music.getArtist();
        if ("<unknown>".equals(artist)) {
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

        //加载图片
        holder.ivPicture.setTag(null);
        if (isFirstBindView) {
            displayAvatar(holder, position);
        } else {
            String avatar = avatarUrls.get(music.getId());
            if (!TextUtils.isEmpty(avatar)) {
                //加载ram缓存图片
                Bitmap cacheBmp = getMemoryBitmap(avatar, holder.ivPicture);
                if (cacheBmp != null) {
                    holder.ivPicture.setImageBitmap(cacheBmp);
                    return;
                }
            }
            holder.ivPicture.setTag(new Object());
            holder.ivPicture.setImageResource(R.mipmap.default_small_song_pic);
        }
    }

    @Override
    public void onViewRecycled(MusicItemHolder holder) {
        if (holder.call != null) {
            holder.call.cancel();
        }
        super.onViewRecycled(holder);
    }

    private void displayAvatar(final MusicItemHolder holder, int position) {
        final Music music = data.get(position);
        String artist = music.getArtist();
        String avatar = avatarUrls.get(music.getId());
        if (avatar == null) {
            //搜索头像
            MyLog.d("LazyLoad", music.getTitle() + " search pic");
            holder.call = MusicApi.searchLrcPic(music.getTitle(),
                    "<unknown>".equals(artist) ? "" : artist,
                    2,
                    new ApiCallback<LrcPicResult>() {
                        @Override
                        public void onSuccess(Call call, LrcPicResult data) {
                            List<LrcPicResult.LrcPic> lrcPics = data.getLrcPics();
                            String avatar = "";
                            if (lrcPics == null) {
                                avatarUrls.put(music.getId(), avatar);
                                return;
                            }

                            for (LrcPicResult.LrcPic lrcPic : lrcPics) {
                                avatar = lrcPic.getAvatar180x180();
                                if (avatar == null) {
                                    avatar = lrcPic.getAvatar500x500();
                                }
                                if (avatar != null) {
                                    break;
                                }
                            }
                            avatarUrls.put(music.getId(), avatar);
                            if (!TextUtils.isEmpty(avatar)) {
                                final String finalAvatar = avatar;
                                holder.ivPicture.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageLoader.displayImage(finalAvatar, holder.ivPicture, imageOptions);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            holder.ivPicture.post(new Runnable() {
                                @Override
                                public void run() {
                                    holder.ivPicture.setImageResource(R.mipmap.default_small_song_pic);
                                }
                            });
                        }
                    });

        } else if (!avatar.isEmpty()) {
            imageLoader.displayImage(avatar, holder.ivPicture, imageOptions);
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
        public ImageView ivPicture;
        public TextView tvTitle;
        public TextView tvArtist;
        public ImageButton ibtnMenu;
        public CheckBox cbSelect;
        //搜索图片请求
        public Call call;

        public MusicItemHolder(final View itemView) {
            super(itemView);
            ivPicture = (ImageView) itemView.findViewById(R.id.top_board);
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

            ibtnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemMenuClickListener != null) {
                        onItemMenuClickListener.onItemMenuClick(v, getAdapterPosition());
                    }
                }
            });
        }

    }


    private OnItemCheckedListener onItemCheckedListener;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemMenuClickListener onItemMenuClickListener;

    public void setOnItemMenuClickListener(OnItemMenuClickListener onItemMenuClickListener) {
        this.onItemMenuClickListener = onItemMenuClickListener;
    }

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

    public interface OnItemMenuClickListener {
        void onItemMenuClick(View menuView, int position);
    }
}