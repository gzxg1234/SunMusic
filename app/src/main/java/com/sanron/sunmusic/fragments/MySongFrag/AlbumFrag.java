package com.sanron.sunmusic.fragments.MySongFrag;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.GridAdapter;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.Album;
import com.sanron.sunmusic.model.Artist;
import com.sanron.sunmusic.task.GetAlbumsTask;
import com.sanron.sunmusic.task.GetArtistsTask;
import com.sanron.sunmusic.utils.DensityUtil;

import java.io.File;
import java.util.List;
import java.util.Observable;

/**
 * Created by Administrator on 2015/12/21.
 */
public class AlbumFrag extends BaseFragment {

    private RecyclerView rvAlbum;
    private GridAdapter mAlbumAdapter;
    public static AlbumFrag newInstance() {
        return new AlbumFrag();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlbumAdapter = new GridAdapter<Album>(getContext(), null) {
            @Override
            public void onBindViewHolder(GridItemHolder holder, int position) {
                Album album = mData.get(position);
                holder.tvText1.setText(album.getName());
                holder.tvText2.setText(album.getSongNum()+"首音乐");
                String picPath = album.getPicPath();
                if(TextUtils.isEmpty(picPath)){
                    holder.ivPicture.setImageResource(R.mipmap.default_pic);
                }else{
                    File file = new File(picPath);
                    if(!file.exists()){
                        holder.ivPicture.setImageResource(R.mipmap.default_pic);
                    }
                }
            }
        };
        update(null, DBHelper.TABLE_ALBUM);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.frag_recycler_layout, null);
        int padding = DensityUtil.dip2px(getContext(),4);
        contentView.setPadding(padding,padding,padding,padding);
        rvAlbum = $(R.id.recycler_view);
        rvAlbum.setAdapter(mAlbumAdapter);
        rvAlbum.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        return contentView;
    }

    @Override
    public void update(Observable observable, Object data) {
        if(DBHelper.TABLE_ALBUM.equals(data)) {
            new GetAlbumsTask() {
                @Override
                protected void onPostExecute(List<Album> albums) {
                    mAlbumAdapter.setData(albums);
                }
            }.execute();
        }
    }

}
