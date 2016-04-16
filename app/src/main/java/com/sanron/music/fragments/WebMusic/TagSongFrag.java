package com.sanron.music.fragments.WebMusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sanron.music.R;
import com.sanron.music.adapter.SongItemAdapter;
import com.sanron.music.db.model.Music;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.Song;
import com.sanron.music.net.bean.TagData;
import com.sanron.music.service.IPlayer;
import com.sanron.music.view.DDPullListView;

import java.util.List;

import okhttp3.Call;

/**
 * 分类歌曲
 * Created by sanron on 16-4-12.
 */
public class TagSongFrag extends PullFrag implements IPlayer.OnPlayStateChangeListener {

    private String tag;
    private TextView tvPlay;
    private ImageButton ibtnDownload;
    private TagData tagData;
    private TextView tvTagName;
    private SongItemAdapter adapter;
    public static final int LOAD_LIMIT = 100;

    public static TagSongFrag newInstance(String tag) {
        Bundle args = new Bundle();
        args.putString("tag", tag);
        TagSongFrag tagSongFrag = new TagSongFrag();
        tagSongFrag.setArguments(args);
        return tagSongFrag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            tag = args.getString("tag");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_frag_tag_song, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvPlay = $(R.id.tv_play);
        ibtnDownload = $(R.id.ibtn_download);
        tvTagName = $(R.id.tv_tag_name);

        setTitle(tag);
        tvTagName.setText(tag);
        player.addPlayStateChangeListener(this);
        adapter = new SongItemAdapter(getContext(), player);
        pullListView.setAdapter(adapter);
        pullListView.setOnLoadListener(new DDPullListView.OnLoadListener() {
            @Override
            public void onLoad() {
                getData();
            }
        });
        topBoard.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                topBoard.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int height = topBoard.getHeight();
                pullListView.setMaxHeaderHeight(height + 200);
                pullListView.setNormalHeaderHeight(height);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        player.removePlayStateChangeListener(this);
    }

    private void getData() {
        Call call = MusicApi.tagInfo(tag, LOAD_LIMIT, adapter == null ? 0 : adapter.getCount(), new ApiCallback<TagData>() {
            @Override
            public void onSuccess(final TagData data) {
                setData(data);
                pullListView.onLoadCompleted();
            }

            @Override
            public void onFailure(Exception e) {
                showLoadFailedView();
            }
        });
        addCall(call);
    }

    @Override
    protected void onEnterAnimationEnd() {
        getData();
    }


    private void setData(TagData tagData) {
        this.tagData = tagData;
        if (tagData != null
                && tagData.taginfo != null) {
            adapter.addData(tagData.taginfo.songs);
            if (tagData.taginfo.havemore == 0) {
                pullListView.setHasMore(false);
            }
        }
        hideLoadingView();
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
