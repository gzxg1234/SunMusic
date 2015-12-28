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
import com.sanron.sunmusic.db.DataProvider;
import com.sanron.sunmusic.fragments.BaseFragment;
import com.sanron.sunmusic.model.Artist;
import com.sanron.sunmusic.task.GetArtistsTask;
import com.sanron.sunmusic.utils.DensityUtil;

import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Administrator on 2015/12/21.
 */
public class ArtistFrag extends BaseFragment {

    private RecyclerView rvArtists;
    private GridAdapter mArtistAdapter;

    public static ArtistFrag newInstance() {
        return new ArtistFrag();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArtistAdapter = new GridAdapter<Artist>(getContext(), null) {
            @Override
            public void onBindViewHolder(GridItemHolder holder, int position) {
                Artist artist = mData.get(position);
                holder.tvText1.setText(artist.getName());
                holder.tvText2.setText(artist.getAlbumNum()+"张专辑");
                String picPath = artist.getPicPath();
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
        update(null, DBHelper.TABLE_ARTIST);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.frag_recycler_layout, null);
        int padding = DensityUtil.dip2px(getContext(),4);
        contentView.setPadding(padding,padding,padding,padding);
        rvArtists = $(R.id.recycler_view);
        rvArtists.setAdapter(mArtistAdapter);
        rvArtists.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        return contentView;
    }

    @Override
    public void update(Observable observable, Object data) {
        if(DBHelper.TABLE_ARTIST.equals(data)) {
            new GetArtistsTask() {
                @Override
                protected void onPostExecute(List<Artist> artists) {
                    mArtistAdapter.setData(artists);
                }
            }.execute();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.artistfrag_option_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.option_match_pic:{

            }break;
        }
        return super.onOptionsItemSelected(item);
    }
}
