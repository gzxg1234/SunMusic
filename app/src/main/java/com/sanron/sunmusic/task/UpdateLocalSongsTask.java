package com.sanron.sunmusic.task;

import android.content.Context;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.SongInfoDao;
import com.sanron.sunmusic.model.SongInfo;
import com.sanron.sunmusic.music.SongLoader;

import java.util.List;

/**
 * 刷新本地歌曲(重新读取MediaProvider数据)
 * Created by Administrator on 2015/12/21.
 */
public abstract class UpdateLocalSongsTask extends DBAccessTask<Void, Void, List<SongInfo>> {

    public UpdateLocalSongsTask(Context context) {
        super(context);
    }

    @Override
    protected List<SongInfo> doInBackground(Void... params) {
        SongInfoDao songInfoDao = new SongInfoDao(mContextRef.get());
        List<SongInfo> newData = SongLoader.load(mContextRef.get());

        for (int i = 0; i < newData.size(); i++) {
            SongInfo songInfo = newData.get(i);
            //查找是否已添加到数据库的本地歌曲
            List<SongInfo> result = songInfoDao.query(DBHelper.SONG_SONGID + "=? and " + DBHelper.SONG_TYPE + "=?",
                    new String[]{songInfo.getSongId(), "" + SongInfo.TYPE_LOCAL});
            if (result.size() != 0) {
                //数据库中已有，保留数据库中的数据
                newData.set(i, result.get(0));
            }
        }

        songInfoDao.deleteLocal();
        songInfoDao.add(newData);
        songInfoDao.closeDatabase();
        return newData;
    }


}
