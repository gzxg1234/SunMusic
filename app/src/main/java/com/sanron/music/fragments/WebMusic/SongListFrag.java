package com.sanron.music.fragments.WebMusic;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sanron.music.R;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.Music;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.Song;
import com.sanron.music.net.bean.SongList;
import com.sanron.music.task.AddOnlineSongListTask;
import com.sanron.music.utils.T;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-1.
 */
public class SongListFrag extends PullFrag implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String ARG_LIST_ID = "list_id";

    private Call requestCall;
    private String listId;
    private SongList data;
    private boolean isCollected;

    private TextView tvSongListTitle;
    private TextView tvSongListTag;

    private TextView tvSongNum;
    private ImageButton ibtnFavorite;
    private ImageButton ibtnDownload;
    private ImageButton ibtnShare;

    private SongListItemAdapter adapter;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions imageOptions;


    public static SongListFrag newInstance(String songListId) {
        SongListFrag songListFrag = new SongListFrag();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_LIST_ID, songListId);
        songListFrag.setArguments(bundle);
        return songListFrag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SongListItemAdapter(getContext());
        imageOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .build();
        Bundle args = getArguments();
        if (args != null) {
            listId = args.getString(ARG_LIST_ID);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LayoutInflater.from(getContext())
                .inflate(R.layout.layout_songlist_operator, operatorContainer, true);
        LayoutInflater.from(getContext())
                .inflate(R.layout.layout_songlist_info, infoContainer, true);
        tvSongNum = (TextView) operatorContainer.findViewById(R.id.tv_song_num);
        ibtnDownload = (ImageButton) operatorContainer.findViewById(R.id.ibtn_download);
        ibtnFavorite = (ImageButton) operatorContainer.findViewById(R.id.ibtn_favorite);
        ibtnShare = (ImageButton) operatorContainer.findViewById(R.id.ibtn_share);
        tvSongListTag = (TextView) infoContainer.findViewById(R.id.tv_list_tag);
        tvSongListTitle = (TextView) infoContainer.findViewById(R.id.tv_list_title);

        pullListView.setAdapter(adapter);
        ibtnDownload.setOnClickListener(this);
        ibtnShare.setOnClickListener(this);
        ibtnFavorite.setOnClickListener(this);
        pullListView.setOnItemClickListener(this);
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (requestCall != null) {
            requestCall.cancel();
        }
    }

    private void loadData() {
        requestCall = MusicApi.songListInfo(listId, new ApiCallback<SongList>() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    showLoadFailedView();
                }
            }

            @Override
            public void onSuccess(Call call, final SongList data) {
                String pic = data.pic700;
                if (TextUtils.isEmpty(pic)) {
                    pic = data.pic500;
                    if (TextUtils.isEmpty(pic)) {
                        pic = data.pic500;
                    }
                }
                //加载图片
                final Bitmap image = imageLoader.loadImageSync(pic, imageOptions);
                //检查是否已收藏
                isCollected = checkIsCollected(listId);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setData(data, image);
                    }
                });
            }
        });
    }

    private void setData(SongList data, Bitmap image) {
        this.data = data;
        isLoaded = true;
        hideLoadingView();
        tvSongListTag.setText(data.tag);
        tvSongListTitle.setText(data.title);
        tvSongNum.setText("共" + data.songs.size() + "首歌");
        adapter.setData(data.songs);
        setTitle(data.title);
        setHeaderImage(image);
        if (isCollected) {
            ibtnFavorite.setImageResource(R.mipmap.ic_favorite_black_24dp);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_download: {

            }
            break;

            case R.id.ibtn_favorite: {
                if (isCollected) {
                    T.show(getContext(), "已收藏过此歌单");
                } else {
                    new AddOnlineSongListTask(data) {
                        @Override
                        protected void onPostExecute(Integer result) {
                            switch (result) {

                                case SUCCESS: {
                                    T.show(getContext(), "收藏成功");
                                    isCollected = true;
                                    ibtnFavorite.setImageResource(R.mipmap.ic_favorite_black_24dp);
                                }
                                break;

                                case FAILED: {
                                    T.show(getContext(), "收藏歌单失败");
                                }
                                break;
                            }
                        }
                    }.execute();
                }
            }
            break;

            case R.id.ibtn_share: {

            }
            break;
        }
    }

    private boolean checkIsCollected(String listid) {
        DataProvider.Access access = DataProvider.instance().getAccess(DBHelper.List.TABLE);
        Cursor c = access.query(new String[]{DBHelper.ID},
                DBHelper.List.LIST_ID + "=?",
                new String[]{listid});
        boolean result = c.moveToFirst();
        access.close();
        return result;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.pull_list_view: {
                List<Song> songs = adapter.getData();
                List<Music> musics = new LinkedList<>();
                for (Song song : songs) {
                    musics.add(song.toMusic());
                }
                player.clearQueue();
                player.enqueue(musics);
                player.play(position - 1);
            }
            break;
        }
    }


    public static class SongListItemAdapter extends BaseAdapter {

        private Context context;
        private List<Song> data;

        public SongListItemAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        public void setData(List<Song> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        public List<Song> getData() {
            return data;
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Song song = data.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_list_song_item, parent, false);
            }
            TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            TextView tvArtist = (TextView) convertView.findViewById(R.id.tv_artist);
            ImageView ivMv = (ImageView) convertView.findViewById(R.id.iv_mv);
            tvTitle.setText(song.title);
            tvArtist.setText(song.author);
            if (song.hasMv == 1) {
                ivMv.setVisibility(View.VISIBLE);
            } else {
                ivMv.setVisibility(View.GONE);
            }
            return convertView;
        }

    }
}
