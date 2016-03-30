package com.sanron.music.fragments.WebMusic;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sanron.music.R;
import com.sanron.music.fragments.BaseFragment;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.SongList;
import com.sanron.music.view.DDPullListView;

/**
 * Created by sanron on 16-3-30.
 */
public class SongListFrag extends BaseFragment {

    private DDPullListView lvSongs;
    private DDPullListView.DDPullHeader pullHeader;
    private View topbar;
    private View viewOperator;
    private TextView tvSongListName;
    private TextView tvSongListTag;
    private ImageButton ibtnPlay;
    private String listId;

    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions imageOptions;

    public static final String ARG_LIST_ID = "list_id";

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
        contentView = inflater.inflate(R.layout.frag_songlist, null);
        viewOperator = inflater.inflate(R.layout.layout_songlist_operator,null);
        topbar = $(R.id.top_bar);
        tvSongListName = $(R.id.tv_songlist_name);
        tvSongListTag = $(R.id.tv_songlist_tag);
        lvSongs = $(R.id.lv_songlist_songs);
        pullHeader = lvSongs.getPullHeader();
        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appContext.setViewFitsStatuBar(topbar);
        pullHeader.setOperatorView(viewOperator);
        lvSongs.setMinHeightScale(0.6f);
        String[] item = new String[100];
        for (int i = 0; i < item.length; i++) {
            item[i] = "item" + i;
        }
        lvSongs.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1,
                item));
        MusicApi.songListInfo(listId, new ApiCallback<SongList>() {
            @Override
            public void onFailure() {

            }

            @Override
            public void onSuccess(SongList data) {
                String pic = data.getPic700();
                if (TextUtils.isEmpty(pic)) {
                    pic = data.getPic300();
                    if (TextUtils.isEmpty(pic)) {
                        pic = data.getPic300();
                    }
                }
                final Bitmap bmp = imageLoader.loadImageSync(pic);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lvSongs.getPullHeader().setImageBitmap(bmp);
                    }
                });
            }
        });
    }

}
