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
import com.sanron.sunmusic.task.GetPlayListsTask;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/12/21.
 */
public class PlayListFrag extends BaseFragment {

    private RecyclerView mListPlayLists;
    private PlayListAdapter mPlayListsAdapter;
    private List<PlayList> mPlayLists;

    public static final String TAG = "PlayListFrag";

    public static PlayListFrag newInstance() {
        return new PlayListFrag();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GetPlayListsTask(getContext()){
            @Override
            protected void onPostData(List<PlayList> playLists) {
                mPlayLists = playLists;
                if(mPlayListsAdapter!=null) {
                    mPlayListsAdapter.setData(mPlayLists);
                }
            }
        }.execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.frag_playlist, null);
        mListPlayLists = $(R.id.list_playlist);
        mPlayListsAdapter = new PlayListAdapter(getContext(),mPlayLists);
        mListPlayLists.setAdapter(mPlayListsAdapter);
        mListPlayLists.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mPlayListsAdapter.setOnItemClickListener(new PlayListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                List<PlayList> playLists = mPlayListsAdapter.getData();
                ClickListEvent event = new ClickListEvent();
                event.setPlayList(playLists.get(position));
                EventBus.getDefault().post(event);
            }
        });
        return contentView;
    }

    public static class ClickListEvent {
        private PlayList playList;

        public PlayList getPlayList() {
            return playList;
        }

        public void setPlayList(PlayList playList) {
            this.playList = playList;
        }
    }


    public static class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.PlayListHolder> {

        private List<PlayList> mData;
        private Context mContext;
        private OnItemClickListener onItemClickListener;

        public PlayListAdapter(Context context,List<PlayList> data) {
            super();
            mContext = context;
            mData = data;
        }

        public void setData(List<PlayList> data) {
            this.mData = data;
            notifyDataSetChanged();
        }

        public List<PlayList> getData() {
            return mData;
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
                        onItemClickListener.onItemClick(holder.itemView, position);
                    }
                }
            });
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
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

        interface OnItemClickListener {
            void onItemClick(View view, int position);
        }
    }
}
