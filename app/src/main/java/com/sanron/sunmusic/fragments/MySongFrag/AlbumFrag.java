package com.sanron.sunmusic.fragments.MySongFrag;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.adapter.DataListAdapter;
import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.model.Album;
import com.sanron.sunmusic.task.GetAlbumsTask;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class AlbumFrag extends BaseListFrag<Album> {

    public AlbumFrag(int layout) {
        super(layout,new String[]{DBHelper.TABLE_ALBUM,DBHelper.TABLE_SONG,DBHelper.TABLE_PLAYLIST});
    }

    public static AlbumFrag newInstance() {
        return new AlbumFrag(LAYOUT_STAGGERED);
    }

    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {
        Album album = mAdapter.getItem(position);
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

    @Override
    public void onCreatePopupMenu(Menu menu, MenuInflater inflater, int position) {
        inflater.inflate(R.menu.album_menu,menu);
    }

    @Override
    public void onPopupItemSelected(MenuItem item, int position) {
        List<Album> selectedAlbums = mAdapter.getData().subList(position,position+1);
        switch (item.getItemId()){
            case R.id.menu_add_to_quque:{

            }break;

            case R.id.menu_match_pic:{

            }break;
        }
    }

    @Override
    public void refreshData() {
        new GetAlbumsTask() {
            @Override
            protected void onPostExecute(List<Album> albums) {
                mAdapter.setData(albums);
            }
        }.execute();
    }
}
