package com.sanron.music.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.common.ViewTool;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.bean.PlayList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter {
    private List<PlayList> mData = new ArrayList<>();
    private List<Object> mItems = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private OnItemMenuClickListener mOnItemMenuClickListener;

    public PlayListAdapter(Context context) {
        this.mContext = context;
    }

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_GROUP_ITEM = 1;

    public void setData(List<PlayList> data) {
        mData.clear();
        mData.addAll(data);
        mItems.clear();
        List<PlayList> selfList = new LinkedList<>();
        List<PlayList> onlineList = new LinkedList<>();
        for (PlayList playList : data) {
            if (playList.getType() == DBHelper.List.TYPE_COLLECTION) {
                onlineList.add(playList);
            } else {
                selfList.add(playList);
            }
        }
        if (selfList.size() > 0) {
            mItems.add("自建歌单");
            mItems.addAll(selfList);
        }
        if (onlineList.size() > 0) {
            mItems.add("收藏歌单");
            mItems.addAll(onlineList);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position) instanceof String) {
            return TYPE_GROUP_ITEM;
        }
        return TYPE_ITEM;
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {

            case TYPE_GROUP_ITEM: {
                TextView tvGroup = new TextView(mContext);
                final int height = ViewTool.dpToPx(40);
                final int paddingLeft = ViewTool.dpToPx(8);
                final int textColor = mContext.getResources().getColor(R.color.colorAccent);
                tvGroup.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        height));
                tvGroup.setPadding(paddingLeft, 0, 0, 0);
                tvGroup.setGravity(Gravity.CENTER_VERTICAL);
                tvGroup.setTextColor(textColor);
                holder = new GroupHolder(tvGroup);
            }
            break;

            case TYPE_ITEM: {
                View view = LayoutInflater.from(mContext).inflate(R.layout.list_common_item, parent, false);
                CommonItemViewHolder commonHolder = new CommonItemViewHolder(view);
                commonHolder.ivMenu.setImageResource(R.mipmap.ic_more_vert_black_24dp);
                holder = commonHolder;
            }
            break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof CommonItemViewHolder) {
            CommonItemViewHolder itemHolder = (CommonItemViewHolder) holder;
            PlayList playList = (PlayList) mItems.get(position);
            if (playList.getType() == DBHelper.List.TYPE_FAVORITE) {
                itemHolder.ivPicture.setImageResource(R.mipmap.ic_favorite_list);
            } else if (playList.getType() == DBHelper.List.TYPE_USER) {
                itemHolder.ivPicture.setImageResource(R.mipmap.icon_normal_list);
            } else {
                ImageLoader.getInstance().displayImage(playList.getIcon(),
                        itemHolder.ivPicture);
            }
            itemHolder.tvText1.setText(playList.getTitle());
            itemHolder.tvText2.setText(playList.getSongNum() + "首");
            itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, position);
                    }
                }
            });
            itemHolder.ivMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemMenuClickListener != null) {
                        mOnItemMenuClickListener.onItemMenuClick(v, position);
                    }
                }
            });
        } else if (holder instanceof GroupHolder) {
            GroupHolder groupHolder = (GroupHolder) holder;
            groupHolder.tvText.setText((String) mItems.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public List<PlayList> getData() {
        return mData;
    }

    class GroupHolder extends RecyclerView.ViewHolder {
        TextView tvText;

        public GroupHolder(View itemView) {
            super(itemView);
            tvText = (TextView) itemView;
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemMenuClickListener(OnItemMenuClickListener onItemMenuClickListener) {
        this.mOnItemMenuClickListener = onItemMenuClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemMenuClickListener {
        void onItemMenuClick(View view, int position);
    }
}