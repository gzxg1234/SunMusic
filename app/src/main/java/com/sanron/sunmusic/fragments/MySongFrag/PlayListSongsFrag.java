package com.sanron.sunmusic.fragments.MySongFrag;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.PlayList;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.task.GetPlayListSongsTask;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListSongsFrag extends BaseFragment {

    private PlayList mPlayList;
    private RecyclerView mListPlaySongs;
    private ListSongsAdapter mPlaySongsAdapter;
    private List<SongInfo> mPlaySongs;

    public static final String TAG = "PlayListSonsFrag";

    public static PlayListSongsFrag newInstance(PlayList playList) {
        return new PlayListSongsFrag(playList);
    }

    private PlayListSongsFrag(PlayList mPlayList) {
        this.mPlayList = mPlayList;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GetPlayListSongsTask(getContext()) {
            @Override
            protected void onPostData(List<SongInfo> data) {
                mPlaySongs = data;
                if (mPlaySongsAdapter != null) {
                    mPlaySongsAdapter.setmData(mPlaySongs);
                }
            }
        }.execute(mPlayList.getId());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.frag_playlist_songs, null);
        mListPlaySongs = $(R.id.list_playlist_songs);
        mPlaySongsAdapter = new ListSongsAdapter(getContext(), mPlaySongs);
        mListPlaySongs.setAdapter(mPlaySongsAdapter);
        mListPlaySongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return contentView;
    }

    public static class ListSongsAdapter extends RecyclerView.Adapter<ListSongsAdapter.ListSongsHolder> {

        private Context mContext;
        private List<SongInfo> mData;

        public ListSongsAdapter(Context context, List<SongInfo> data) {
            super();
            this.mContext = context;
            this.mData = data;
        }

        @Override
        public ListSongsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_playlist_song, parent, false);
            return new ListSongsHolder(view);
        }

        @Override
        public void onBindViewHolder(ListSongsHolder holder, int position) {
            SongInfo songInfo = mData.get(position);
            holder.tvName.setText(songInfo.getDisplayName());
            holder.tvTitle.setText(songInfo.getTitle());
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public void setmData(List<SongInfo> mData) {
            this.mData = mData;
            notifyDataSetChanged();
        }

        public List<SongInfo> getmData() {
            return mData;
        }

        public static class ListSongsHolder extends RecyclerView.ViewHolder {
            ImageButton btnAddQueue;
            ImageButton btnAction;
            TextView tvName;
            TextView tvTitle;

            public ListSongsHolder(View itemView) {
                super(itemView);
                btnAddQueue = (ImageButton) itemView.findViewById(R.id.btn_add_queue);
                btnAction = (ImageButton) itemView.findViewById(R.id.btn_song_action);
                tvName = (TextView) itemView.findViewById(R.id.tv_song_name);
                tvTitle = (TextView) itemView.findViewById(R.id.tv_song_title);
            }
        }
    }

}
