package com.sanron.music.fragments.MyMusic;

import android.content.ContentValues;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sanron.music.R;
import com.sanron.music.adapter.DataListAdapter;
import com.sanron.music.db.DBHelper;
import com.sanron.music.db.model.Album;
import com.sanron.music.db.model.Music;
import com.sanron.music.task.GetAlbumsTask;
import com.sanron.music.task.QueryMusicTask;
import com.sanron.music.utils.TUtils;

import java.util.List;

/**
 * Created by Administrator on 2015/12/21.
 */
public class AlbumFrag extends BaseDataFrag<Album> {

    public AlbumFrag() {
        super(LAYOUT_GRID,
                new String[]{DBHelper.TABLE_ALBUM,DBHelper.TABLE_MUSIC,DBHelper.TABLE_PLAYLIST});
    }

    @Override
    protected void bindViewHolder(DataListAdapter.ItemHolder holder, int position) {
        Album album = mAdapter.getItem(position);
        holder.tvText1.setText(album.getName());
        holder.tvText2.setText(album.getSongNum()+"首音乐");
    }

    @Override
    public String onGetPicturePath(Album data) {
        return data.getPicPath();
    }

    @Override
    public void onCreatePopupMenu(Menu menu, MenuInflater inflater, int position) {
        inflater.inflate(R.menu.album_menu,menu);
    }

    @Override
    public void onPopupItemSelected(MenuItem item, int position) {
        switch (item.getItemId()){
            case R.id.menu_add_to_quque:{
                ContentValues query = new ContentValues();
                query.put(DBHelper.MUSIC_ALBUM_KEY,mAdapter.getItem(position).getId());
                new QueryMusicTask(query){
                    @Override
                    protected void onPostExecute(List<Music> musics) {
                        player.enqueue(musics);
                        TUtils.show(getContext(),musics.size()+"首歌曲添加到队列");
                    }
                }.execute();
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
