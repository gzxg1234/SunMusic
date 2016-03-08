package com.sanron.sunmusic.fragments.MyMusic;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.DataListAdapter;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.model.Artist;
import com.sanron.sunmusic.task.GetArtistsTask;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class ArtistFrag extends BaseListFrag<Artist> {

    public ArtistFrag(int layout) {
        super(layout,new String[]{DBHelper.TABLE_ARTIST, DBHelper.TABLE_MUSIC,DBHelper.TABLE_ALBUM});
    }

    public static ArtistFrag newInstance() {
        return new ArtistFrag(LAYOUT_STAGGERED);
    }

    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {
        Artist artist = mAdapter.getItem(position);
        holder.tvText1.setText(artist.getName());
        holder.tvText2.setText(artist.getAlbumNum()+"张专辑");
        String picPath = artist.getPicPath();
        if(TextUtils.isEmpty(picPath)){
            holder.ivPicture.setImageResource(R.mipmap.default_artist_album_pic);
        }else{
            File file = new File(picPath);
            if(!file.exists()){
                holder.ivPicture.setImageResource(R.mipmap.default_artist_album_pic);
            }
        }
    }

    @Override
    public void refreshData() {
        new GetArtistsTask() {
            @Override
            protected void onPostExecute(List<Artist> artists) {
                mAdapter.setData(artists);
            }
        }.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.option_menu_artist_frag,menu);
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
