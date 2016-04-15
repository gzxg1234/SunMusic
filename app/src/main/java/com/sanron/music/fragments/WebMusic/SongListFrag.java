package com.sanron.music.fragments.WebMusic;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sanron.music.R;
import com.sanron.music.adapter.SongItemAdapter;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.DataProvider;
import com.sanron.music.db.model.Music;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.Song;
import com.sanron.music.net.bean.SongList;
import com.sanron.music.service.IPlayer;
import com.sanron.music.task.AddOnlineSongListTask;
import com.sanron.music.utils.T;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;

/**
 * Created by sanron on 16-4-1.
 */
public class SongListFrag extends PullFrag implements View.OnClickListener, IPlayer.OnPlayStateChangeListener {

    public static final String ARG_LIST_ID = "list_id";

    private String listId;
    private SongList data;
    private boolean isCollected;

    private TextView tvSongListTitle;
    private TextView tvSongListTag;

    private TextView tvSongNum;
    private ImageButton ibtnFavorite;
    private ImageButton ibtnDownload;
    private ImageButton ibtnShare;
    private ImageView ivPicture;

    private SongItemAdapter adapter;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions imageOptions;

    private Handler handler = new Handler(Looper.getMainLooper());


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
        adapter = new SongItemAdapter(getContext(), player);
        imageOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .build();
        Bundle args = getArguments();
        if (args != null) {
            listId = args.getString(ARG_LIST_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_songlist, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvSongNum = $(R.id.tv_song_num);
        ibtnDownload = $(R.id.ibtn_download);
        ibtnFavorite = $(R.id.ibtn_favorite);
        ibtnShare = $(R.id.ibtn_share);
        tvSongListTag = $(R.id.tv_list_tag);
        tvSongListTitle = $(R.id.tv_list_title);
        ivPicture = $(R.id.top_board);

        pullListView.setAdapter(adapter);
        ibtnDownload.setOnClickListener(this);
        ibtnShare.setOnClickListener(this);
        ibtnFavorite.setOnClickListener(this);
        pullListView.setHasMore(false);
        player.addPlayStateChangeListener(this);

        ivPicture.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ivPicture.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width = ivPicture.getWidth();
                int normalHeaderHeight = ivPicture.getHeight() + viewOperator.getHeight();
                pullListView.setMaxHeaderHeight(width + viewOperator.getHeight());
                pullListView.setNormalHeaderHeight(normalHeaderHeight);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        player.removePlayStateChangeListener(this);
    }

    @Override
    protected void onEnterAnimationEnd() {
        Call call = MusicApi.songListInfo(listId, new ApiCallback<SongList>() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showLoadFailedView();
                        }
                    });
                }
            }

            @Override
            public void onSuccess(final SongList data) {
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
        addCall(call);
    }


    private void setData(SongList data, Bitmap image) {
        this.data = data;
        ivPicture.setImageBitmap(image);
        tvSongListTag.setText(data.tag);
        tvSongListTitle.setText(data.title);
        tvSongNum.setText("共" + data.songs.size() + "首歌");
        adapter.setData(data.songs);
        pullListView.onLoadCompleted();
        setTitle(data.title);
        if (isCollected) {
            ibtnFavorite.setImageResource(R.mipmap.ic_favorite_black_24dp);
        }
        hideLoadingView();
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
    public void onPlayStateChange(int state) {
        if (state == IPlayer.STATE_PREPARING) {
            //列表中是否有播放中的歌曲
            Music currentMusic = player.getCurrentMusic();
            List<Song> listData = adapter.getData();
            if (listData != null) {
                for (int i = 0; i < listData.size(); i++) {
                    if (listData.get(i).songId.equals(currentMusic.getSongId())) {
                        adapter.setPlayingPosition(i);
                        break;
                    }
                }
            }
        } else if (state == IPlayer.STATE_STOP) {
            adapter.setPlayingPosition(-1);
        }
    }
}
