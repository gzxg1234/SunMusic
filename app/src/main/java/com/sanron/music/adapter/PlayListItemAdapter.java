package com.sanron.music.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanron.music.R;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.PlayList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PlayListItemAdapter extends RecyclerView.Adapter {
    private List<PlayList> data;
    private List<Object> items;
    private Context context;
    private ImageLoader imageLoader;
    private OnItemClickListener onItemClickListener;
    private OnItemMenuClickListener onItemMenuClickListener;

    public PlayListItemAdapter(Context context) {
        this.context = context;
        imageLoader = ImageLoader.getInstance();
    }

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_GROUP_ITEM = 1;

    public void setData(List<PlayList> data) {
        if (this.data == data) {
            return;
        }

        this.data = data;
        if (data != null) {
            items = new ArrayList<>();
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
                items.add("自建歌单");
                items.addAll(selfList);
            }
            if (onlineList.size() > 0) {
                items.add("收藏歌单");
                items.addAll(onlineList);
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_GROUP_ITEM;
        }
        return TYPE_ITEM;
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

            case TYPE_GROUP_ITEM: {
                View view = LayoutInflater.from(context).inflate(R.layout.layout_playlist_type_header, parent, false);
                return new GroupHolder(view);
            }

            default: {
                View view = LayoutInflater.from(context).inflate(R.layout.list_playlist_item, parent, false);
                return new ListItemHolder(view);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int holderType = getItemViewType(position);
        if (holderType == TYPE_ITEM) {
            ListItemHolder itemHolder = (ListItemHolder) holder;
            PlayList playList = (PlayList) items.get(position);
            if (playList.getType() == DBHelper.List.TYPE_FAVORITE) {
                itemHolder.ivPicture.setImageResource(R.mipmap.ic_favorite_list);
            } else if (playList.getType() == DBHelper.List.TYPE_USER) {
                itemHolder.ivPicture.setImageResource(R.mipmap.icon_normal_list);
            } else {
                imageLoader.displayImage(playList.getIcon(),
                        itemHolder.ivPicture);
            }
            itemHolder.tvName.setText(playList.getTitle());
            itemHolder.tvMusicNum.setText(playList.getSongNum() + "首");
        } else if (holderType == TYPE_GROUP_ITEM) {
            GroupHolder groupHolder = (GroupHolder) holder;
            groupHolder.tvText.setText((String) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public List<PlayList> getData() {
        return data;
    }

    class GroupHolder extends RecyclerView.ViewHolder {
        TextView tvText;

        public GroupHolder(View itemView) {
            super(itemView);
            tvText = (TextView) itemView.findViewById(R.id.tv_type);
        }
    }

    class ListItemHolder extends RecyclerView.ViewHolder {
        ImageView ivPicture;
        TextView tvName;
        TextView tvMusicNum;
        ImageButton ibtnMenu;

        public ListItemHolder(final View itemView) {
            super(itemView);
            ivPicture = (ImageView) itemView.findViewById(R.id.iv_picture);
            tvMusicNum = (TextView) itemView.findViewById(R.id.tv_music_num);
            tvName = (TextView) itemView.findViewById(R.id.tv_list_name);
            ibtnMenu = (ImageButton) itemView.findViewById(R.id.ibtn_item_menu);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });
            ibtnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemMenuClickListener != null) {
                        onItemMenuClickListener.onItemMenuClick(ibtnMenu, getAdapterPosition());
                    }
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemMenuClickListener(OnItemMenuClickListener onItemMenuClickListener) {
        this.onItemMenuClickListener = onItemMenuClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemMenuClickListener {
        void onItemMenuClick(View view, int position);
    }
}