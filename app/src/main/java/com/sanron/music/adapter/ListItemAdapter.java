package com.sanron.music.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.PlayList;

import java.util.List;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ListItemHolder> {
    private List<PlayList> data;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private OnItemMenuClickListener onItemMenuClickListener;


    public ListItemAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<PlayList> data) {
        if (this.data == data) {
            return;
        }

        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public ListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_playlist_item, parent, false);
        return new ListItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ListItemHolder holder, int position) {
        PlayList playList = data.get(position);
        if (playList.getType() == DBHelper.List.TYPE_FAVORITE) {
            holder.ivType.setImageResource(R.mipmap.icon_favorite_list);
        } else {
            holder.ivType.setImageResource(R.mipmap.icon_normal_list);
        }
        holder.tvName.setText(playList.getName());
        List<Long> musicIds = playList.getMusicIds();
        if (musicIds == null) {
            holder.tvMusicNum.setText(0 + "首");
        } else {
            holder.tvMusicNum.setText(musicIds.size() + "首");
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public List<PlayList> getData() {
        return data;
    }

    class ListItemHolder extends RecyclerView.ViewHolder {
        ImageView ivType;
        TextView tvName;
        TextView tvMusicNum;
        ImageButton ibtnMenu;

        public ListItemHolder(final View itemView) {
            super(itemView);
            ivType = (ImageView) itemView.findViewById(R.id.top_image);
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