package com.sanron.sunmusic.fragments.MySongFrag;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.task.LoadLocalSongTask;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class LocalSongFrag extends BaseFragment {


    RecyclerView listLocalSong;
    LocalSongAdapter listAdapter;
    public static final String TAG = "LocalSongFrag";

    public static LocalSongFrag newInstance() {
        return new LocalSongFrag();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.frag_localsong, null);
        listLocalSong = $(R.id.list_localsong);
        listAdapter = new LocalSongAdapter(getContext(), null);
        listLocalSong.setAdapter(listAdapter);
        listLocalSong.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new LoadLocalSongTask(getContext(),this).execute();
    }

    public void refreshData(List<SongInfo> songInfos) {
        listAdapter.setData(songInfos);
        listAdapter.notifyDataSetChanged();
    }

    public static class LocalSongAdapter extends RecyclerView.Adapter<LocalSongAdapter.LocalSongHolder> {

        private Context mContext;
        private List<SongInfo> mData;

        public LocalSongAdapter(Context context, List<SongInfo> songInfoList) {
            this.mContext = context;
        }

        public void setData(List<SongInfo> songInfos) {
            this.mData = songInfos;
        }

        @Override
        public LocalSongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_localsong, parent,false);
            return new LocalSongHolder(view);
        }

        @Override
        public void onBindViewHolder(LocalSongHolder holder, int position) {
            SongInfo songInfo = mData.get(position);
            holder.tvDisplayName.setText(songInfo.getDisplayName());
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        class LocalSongHolder extends RecyclerView.ViewHolder {
            TextView tvDisplayName;
            ImageButton btnAction;

            public LocalSongHolder(View itemView) {
                super(itemView);
                tvDisplayName = (TextView) itemView.findViewById(R.id.tv_display_name);
                btnAction = (ImageButton) itemView.findViewById(R.id.btn_song_action);
            }
        }
    }
}
