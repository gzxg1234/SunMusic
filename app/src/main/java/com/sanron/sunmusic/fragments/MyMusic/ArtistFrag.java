package com.sanron.sunmusic.fragments.MyMusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.DataListAdapter;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.model.Artist;
import com.sanron.sunmusic.task.GetArtistsTask;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class ArtistFrag extends BaseListFrag<Artist> {

    public ArtistFrag() {
        super(LAYOUT_STAGGERED,
                new String[]{DBHelper.TABLE_ARTIST, DBHelper.TABLE_MUSIC, DBHelper.TABLE_ALBUM});
    }

    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {
        Artist artist = mAdapter.getItem(position);
        holder.tvText1.setText(artist.getName());
        holder.tvText2.setText(artist.getAlbumNum() + "张专辑");
    }

    @Override
    public String onGetPicturePath(Artist data) {
        return data.getPicPath();
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
        inflater.inflate(R.menu.option_menu_artist_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_match_pic: {

            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
