package com.sanron.music.fragments.MyMusic;

import com.sanron.music.adapter.DataListAdapter;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.Artist;
import com.sanron.music.task.GetArtistsTask;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class ArtistFrag extends BaseDataFrag<Artist> {

    public ArtistFrag() {
        super(LAYOUT_GRID,
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
}
