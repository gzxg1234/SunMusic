package com.sanron.ddmusic.adapter;

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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.sanron.ddmusic.R;
import com.sanron.ddmusic.api.MusicApi;
import com.sanron.ddmusic.api.bean.LrcPicData;
import com.sanron.ddmusic.api.callback.JsonCallback;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.service.PlayUtil;
import com.sanron.ddmusic.view.Indexable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * 音乐适配
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicItemHolder> implements Indexable{

    private List<Music> mData = new ArrayList<>();
    private Context mContext;

    private SparseBooleanArray mCheckStates;
    private int mCheckedItemCount;
    private boolean mIsMultiMode = false;

    private int mPlayingPosition = -1;
    private boolean mIsFirstBindView = true;
    private MemoryCache mMemoryCache;

    private int mPlayingTextColor;//播放中文字颜色
    private int mNormalTitleTextColor;//正常title颜色
    private int mNormalArtistTextColor;//正常artist颜色

    private HashMap<Long, String> mAvatarUrlCache;//缓存头像url

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE: {
                    //停止时，遍历子View获得ViewHolder开始执行加载头像
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

    public MusicAdapter(Context context) {
        this.mContext = context;
        mMemoryCache = ImageLoader.getInstance()
                .getMemoryCache();
        Resources resources = context.getResources();
        mPlayingTextColor = resources.getColor(R.color.colorAccent);
        mNormalTitleTextColor = resources.getColor(R.color.textColorPrimary);
        mNormalArtistTextColor = resources.getColor(R.color.textColorSecondary);
    }


    public boolean isMultiMode() {
        return mIsMultiMode;
    }

    public Music getItem(int position) {
        if (mData != null) {
            return mData.get(position);
        }
        return null;
    }

    public void setMultiMode(boolean multiMode) {
        if (this.mIsMultiMode == multiMode) {
            return;
        }

        if (multiMode) {
            if (mCheckStates == null) {
                mCheckStates = new SparseBooleanArray();
            } else {
                mCheckStates.clear();
                mCheckedItemCount = 0;
            }
        }
        mIsMultiMode = multiMode;
    }

    public void setData(List<Music> data) {
        mData.clear();
        mData.addAll(data);
        if (mAvatarUrlCache == null) {
            mAvatarUrlCache = new HashMap<>(data.size());
        }
        setFirstBindView(true);
        notifyDataSetChanged();
    }


    public List<Music> getData() {
        return mData;
    }

    public void setFirstBindView(boolean firstBindView) {
        mIsFirstBindView = firstBindView;
    }

    //查找缓存
    private Bitmap getMemoryBitmap(String uri) {
        List<String> keys = MemoryCacheUtils.findCacheKeysForImageUri(uri, mMemoryCache);
        for (String key : keys) {
            List<Bitmap> bmps = MemoryCacheUtils.findCachedBitmapsForImageUri(key, mMemoryCache);
            if (bmps.size() > 0) {
                return bmps.get(0);
            }
        }
        return null;
    }

    public boolean isItemChecked(int position) {
        if (mIsMultiMode && mCheckStates != null) {
            return mCheckStates.get(position);
        }
        return false;
    }


    public void setItemChecked(int position, boolean checked) {
        if (mIsMultiMode && mCheckStates != null) {
            boolean oldChecked = mCheckStates.get(position);
            if (checked != oldChecked) {
                mCheckStates.put(position, checked);
                if (checked) {
                    mCheckedItemCount++;
                } else {
                    mCheckedItemCount--;
                }
                if (onItemCheckedListener != null) {
                    onItemCheckedListener.onItemChecked(position, checked);
                }
            }
        }
    }

    @Override
    public MusicItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_music_item, parent, false);
        return new MusicItemHolder(view);
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

    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    @Override
    public void onBindViewHolder(final MusicItemHolder holder, int position) {
        final Music music = mData.get(position);
        String artist = music.getArtist();
        if ("<unknown>".equals(artist)) {
            artist = "未知歌手";
        }
        holder.tvTitle.setText(music.getTitle());
        holder.tvArtist.setText(artist);
        Music curMusic = PlayUtil.getCurrentMusic();
        if (curMusic != null
                && (curMusic.getId() == music.getId())) {
            holder.tvTitle.setTextColor(mPlayingTextColor);
            holder.tvArtist.setTextColor(mPlayingTextColor);
            mPlayingPosition = position;
        } else {
            holder.tvTitle.setTextColor(mNormalTitleTextColor);
            holder.tvArtist.setTextColor(mNormalArtistTextColor);
        }

        if (mIsMultiMode) {
            holder.ibtnMenu.setVisibility(View.GONE);
            holder.cbSelect.setVisibility(View.VISIBLE);
            holder.cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setItemChecked(holder.getAdapterPosition(), isChecked);
                }
            });
            holder.cbSelect.setChecked(isItemChecked(position));
        } else {
            holder.ibtnMenu.setVisibility(View.VISIBLE);
            holder.cbSelect.setVisibility(View.GONE);
        }

        //加载图片
        holder.ivPicture.setImageResource(R.mipmap.default_small_song_pic);
        holder.ivPicture.setTag(null);
        if (mIsFirstBindView) {
            displayAvatar(holder, position);
        } else {
            String avatar = mAvatarUrlCache.get(music.getId());
            if (!TextUtils.isEmpty(avatar)) {
                //加载ram缓存图片
                Bitmap cacheBmp = getMemoryBitmap(avatar);
                if (cacheBmp != null) {
                    holder.ivPicture.setImageBitmap(cacheBmp);
                    return;
                }
            }
            holder.ivPicture.setTag(new Object());
        }
    }

    @Override
    public void onViewRecycled(MusicItemHolder holder) {
        super.onViewRecycled(holder);
        if (holder.call != null) {
            holder.call.cancel();
        }
        ImageLoader.getInstance()
                .cancelDisplayTask(holder.ivPicture);
    }


    private void displayAvatar(final MusicItemHolder holder, int position) {
        final Music music = mData.get(position);
        String artist = music.getArtist();
        String avatar = mAvatarUrlCache.get(music.getId());
        if (avatar == null) {
            //搜索头像
            holder.call = MusicApi.searchLrcPic(music.getTitle(),
                    "<unknown>".equals(artist) ? "" : artist,
                    2,
                    new JsonCallback<LrcPicData>() {
                        @Override
                        public void onSuccess(LrcPicData data) {
                            List<LrcPicData.LrcPic> lrcPics = data.lrcPics;
                            String avatar = "";
                            if (lrcPics == null) {
                                mAvatarUrlCache.put(music.getId(), avatar);
                                return;
                            }

                            for (LrcPicData.LrcPic lrcPic : lrcPics) {
                                avatar = lrcPic.avatar180x180;
                                if (avatar == null) {
                                    avatar = lrcPic.avatar500x500;
                                }
                                if (avatar != null) {
                                    break;
                                }
                            }
                            mAvatarUrlCache.put(music.getId(), avatar);
                            if (!TextUtils.isEmpty(avatar)) {
                                ImageLoader.getInstance()
                                        .displayImage(avatar, holder.ivPicture);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                        }
                    });
        } else if (!avatar.isEmpty()) {
            ImageLoader.getInstance()
                    .displayImage(avatar, holder.ivPicture);
        }
    }

    public int getCheckedItemCount() {
        return mCheckedItemCount;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public char getIndexForPosition(int position) {
        String str = mData.get(position).getTitleKey();
        if (TextUtils.isEmpty(str)) {
            return '#';
        } else {
            char letter = str.toUpperCase().charAt(0);
            if (letter < 'A' || letter > 'Z') {
                return '#';
            }
            return letter;
        }
    }

    @Override
    public int getCount() {
        return mData==null?0:mData.size();
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
            ivPicture = (ImageView) itemView.findViewById(R.id.iv_music_pic);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_music_title);
            tvArtist = (TextView) itemView.findViewById(R.id.tv_music_artist);
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