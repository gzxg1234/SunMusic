package com.sanron.sunmusic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.model.PlayList;

import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.PlayListHolder> {

    private List<PlayList> mData;
    private Context mContext;
    private OnItemClickListener onItemClickListener;
    private OnActionClickListener onActionClickListener;

    public PlayListAdapter(Context context, List<PlayList> data) {
        super();
        mContext = context;
        mData = data;
    }

    public void setData(List<PlayList> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void addData(PlayList playList, int position) {
        mData.add(position, playList);
        notifyItemInserted(position);
    }

    public void addData(PlayList playList) {
        addData(playList, mData.size());
    }

    public PlayList getData(int position) {
        return mData.get(position);
    }

    public List<PlayList> getData() {
        return mData;
    }

    public void removeData(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public void clearData() {
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public PlayListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_playlist, parent, false);
        return new PlayListHolder(view);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public void onBindViewHolder(final PlayListHolder holder, final int position) {
        PlayList playList = mData.get(position);
        holder.tvListName.setText(playList.getName());
        holder.tvSongnum.setText(playList.getSongNum() + "首歌曲");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
                }
            }
        });
        holder.btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onActionClickListener != null) {
                    onActionClickListener.onActionClick(holder.btnAction, holder.getAdapterPosition());
                }
            }
        });
    }

    public class PlayListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvListName;
        TextView tvSongnum;
        ImageButton btnAction;

        public PlayListHolder(View itemView) {
            super(itemView);
            tvListName = (TextView) itemView.findViewById(R.id.tv_playlist_name);
            tvSongnum = (TextView) itemView.findViewById(R.id.tv_songnum);
            btnAction = (ImageButton) itemView.findViewById(R.id.btn_list_action);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnActionClickListener {
        void onActionClick(View view, int actionPosition);
    }

    public void setOnActionClickListener(OnActionClickListener onActionClickListener) {
        this.onActionClickListener = onActionClickListener;
    }


}