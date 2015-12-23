package com.sanron.sunmusic.task;

import android.content.Context;
import android.widget.ListView;

import com.sanron.sunmusic.db.SongInfoProvider;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.music.SongLoader;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 刷新本地歌曲(重新读取MediaProvider数据)
 * Created by Administrator on 2015/12/21.
 */
public abstract class UpdateLocalSongsTask extends DBAccessTask<Void, Void, List<SongInfo>> {

    private WeakReference<Context> mContextRef;
    public UpdateLocalSongsTask(Context context) {
        this.mContextRef = new WeakReference<Context>(context);
    }

    @Override
    protected List<SongInfo> doInBackground(Void... params) {
        SongInfoProvider songInfoProvider = SongInfoProvider.instance();
        List<SongInfo> newData = SongLoader.load(mContextRef.get());
        SongInfo query = new SongInfo();
        for (int i = 0; i < newData.size(); i++) {
            SongInfo songInfo = newData.get(i);
            //查找本地歌曲是否已添加到数据库的
            query.setSongId(songInfo.getSongId());
            List<SongInfo> result = songInfoProvider.query(query);
            if (result.size() > 0) {
                //数据库中已添加，保留数据库中的数据
                newData.set(i, result.get(0));
            }
        }

        query = new SongInfo();
        query.setType(SongInfo.TYPE_LOCAL);
        songInfoProvider.delete(query);
        songInfoProvider.insert(newData);
        return newData;
    }


}
