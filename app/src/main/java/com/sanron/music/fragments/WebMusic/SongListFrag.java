package com.sanron.music.fragments.WebMusic;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sanron.music.R;
import com.sanron.music.net.ApiCallback;
import com.sanron.music.net.MusicApi;
import com.sanron.music.net.bean.SongList;

/**
 * Created by sanron on 16-4-1.
 */
public class SongListFrag extends PullFrag {

    public static final String ARG_LIST_ID = "list_id";

    private String listId;
    private SongList data;
    private ViewGroup songListInfo;
    private TextView tvSongListTitle;
    private TextView tvSongListTag;
    private ImageButton ibtnPlay;

    private Handler handler = new Handler(Looper.getMainLooper());
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
        String[] items = new String[100];
        for (int i = 0; i < items.length; i++) {
            items[i] = "item" + i;
        }
        pullListView.setAdapter(new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, items));
        MusicApi.songListInfo(listId, new ApiCallback<SongList>() {
            @Override
            public void onFailure() {

            }

            @Override
            public void onSuccess(final SongList data) {
                String pic = data.getPic700();
                if (TextUtils.isEmpty(pic)) {
                    pic = data.getPic500();
                    if (TextUtils.isEmpty(pic)) {
                        pic = data.getPic300();
                    }
                }
                final Bitmap image = imageLoader.loadImageSync(pic, imageOptions);
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
        tvSongListTag.setText(data.getTag());
        tvSongListTitle.setText(data.getTitle());
        setTopImage(image);
    }

    @Override
    protected View createViewInfo() {
        songListInfo = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.layout_songlist_info, null);
        tvSongListTag = (TextView) songListInfo.findViewById(R.id.tv_list_tag);
        tvSongListTitle = (TextView) songListInfo.findViewById(R.id.tv_list_title);
        ibtnPlay = (ImageButton) songListInfo.findViewById(R.id.ibtn_play);
        return songListInfo;
    }
}
