package com.sanron.sunmusic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

public class SongItemAdapter extends RecyclerView.Adapter<SongItemAdapter.LocalSongHolder> {

    private Context mContext;
    private List<SongInfo> mData;
    private OnItemClickListener onItemClickListener;
    private OnActionClickListener onActionClickListener;

    public SongItemAdapter(Context context, List<SongInfo> data) {
        super();
        mContext = context;
        mData = data;
        if (mData == null) {
            mData = new ArrayList<>();
        }
    }

    public void setData(List<SongInfo> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void addData(SongInfo songInfo, int position) {
        mData.add(position, songInfo);
        notifyItemInserted(position);
    }

    public void addData(SongInfo songInfo) {
        addData(songInfo, mData.size());
    }

    public SongInfo getData(int position) {
        return mData.get(position);
    }

    public void removeData(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public void clearData(){
        mData.clear();
        notifyDataSetChanged();
    }

    public List<SongInfo> getData() {
        return mData;
    }

    @Override
    public LocalSongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_song, parent, false);
        return new LocalSongHolder(view);
    }

    @Override
    public void onBindViewHolder(final LocalSongHolder holder, int position) {
        final SongInfo songInfo = mData.get(position);
        holder.tvSongName.setText(songInfo.getTitle());
        holder.tvSongTitle.setText(songInfo.getArtist());
        holder.btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onActionClickListener != null) {
                    onActionClickListener.onActionClick(holder.btnAction, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class LocalSongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivIcon;
        ImageButton btnAction;
        TextView tvSongName;
        TextView tvSongTitle;

        public LocalSongHolder(View itemView) {
            super(itemView);
            ivIcon = (ImageView) itemView.findViewById(R.id.song_icon);
            tvSongName = (TextView) itemView.findViewById(R.id.tv_song_title);
            btnAction = (ImageButton) itemView.findViewById(R.id.btn_song_action);
            tvSongTitle = (TextView) itemView.findViewById(R.id.tv_song_artist);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public interface OnActionClickListener {
        void onActionClick(View view, int actionPosition);
    }


    public void setOnActionClickListener(OnActionClickListener onActionClickListener) {
        this.onActionClickListener = onActionClickListener;
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}