package com.sanron.sunmusic.fragments.MySongFrag;

import android.content.ContentProvider;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.db.PlayListDao;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.task.GetLocalSongsTask;
import com.sanron.sunmusic.task.UpdateLocalSongsTask;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class LocalSongFrag extends BaseFragment {


    private RecyclerView mListLocalSongs;
    private LocalSongAdapter mLocalSongsAdapter;
    private List<SongInfo> mLocalSongs;

    public static final String TAG = "LocalSongFrag";

    public static LocalSongFrag newInstance() {
        return new LocalSongFrag();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        new GetLocalSongsTask(getContext()) {
            @Override
            protected void onPostData(List<SongInfo> data) {
                mLocalSongs = data;
                if(mLocalSongsAdapter!=null) {
                    mLocalSongsAdapter.setData(mLocalSongs);
                }
            }
        }.execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.frag_localsong, null);
        mListLocalSongs = $(R.id.list_localsong);
        mLocalSongsAdapter = new LocalSongAdapter(getContext(), mLocalSongs);
        mLocalSongsAdapter.setData(mLocalSongs);
        mListLocalSongs.setAdapter(mLocalSongsAdapter);
        mListLocalSongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return contentView;
    }

    public static class LocalSongAdapter extends RecyclerView.Adapter<LocalSongAdapter.LocalSongHolder> {

        private Context mContext;
        private List<SongInfo> mData;
        private OnItemClickListener onItemClickListener;

        public LocalSongAdapter(Context context, List<SongInfo> data) {
            super();
            mContext = context;
            mData = data;
        }

        public void setData(List<SongInfo> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public LocalSongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_localsong, parent, false);
            return new LocalSongHolder(view);
        }

        @Override
        public void onBindViewHolder(LocalSongHolder holder, final int position) {
            final SongInfo songInfo = mData.get(position);
            holder.tvDisplayName.setText(songInfo.getDisplayName());
            holder.tvTitle.setText(songInfo.getTitle());

            holder.btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(mContext,v);
                    popupMenu.inflate(R.menu.local_song_action);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.menu_add_to_list:{
                                    new PlayListDao(mContext).addToList(songInfo.getId(),1);
                                }break;

                                case R.id.menu_add_to_quque:{

                                }break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }


        /**
         *
         */
        public class LocalSongHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView tvDisplayName;
            TextView tvTitle;
            ImageButton btnAction;
            public LocalSongHolder(View itemView) {
                super(itemView);
                tvDisplayName = (TextView) itemView.findViewById(R.id.tv_song_name);
                btnAction = (ImageButton) itemView.findViewById(R.id.btn_song_action);
                tvTitle = (TextView) itemView.findViewById(R.id.tv_song_title);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(v,getAdapterPosition());
                }
            }
        }


        public OnItemClickListener getOnItemClickListener() {
            return onItemClickListener;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        interface OnItemClickListener{
            void onItemClick(View view, int position);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.localsongfrag_option_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh: {
                new UpdateLocalSongsTask(getContext()) {
                    @Override
                    protected void onPostData(List<SongInfo> data) {
                        mLocalSongs = data;
                        mLocalSongsAdapter.setData(mLocalSongs);
                    }
                }.execute();
            }
            break;

            case R.id.action_removeall: {

            }
            break;
        }
        return true;
    }


}


