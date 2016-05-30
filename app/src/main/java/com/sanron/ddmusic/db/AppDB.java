package com.sanron.ddmusic.db;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.sanron.ddmusic.api.bean.Song;
import com.sanron.ddmusic.db.bean.Music;
import com.sanron.ddmusic.db.bean.PlayList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/20.
 */
public class AppDB extends SQLiteOpenHelper {


    public static final String DB_NAME = "SunMusicDB.db";
    public static final int DB_VERSION = 1;
    private static volatile AppDB sInstance;

    public static AppDB get(Context context) {
        if (sInstance == null) {
            synchronized (AppDB.class) {
                if (sInstance == null) {
                    sInstance = new AppDB(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private final Context mContext;

    public AppDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        MusicHelper.onCreate(db);
        PlayListHelper.onCreate(db);
        ArtistHelper.onCreate(db);
        AlbumHelper.onCreate(db);
        ListMemberHelper.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MusicHelper.onUpgrade(db, oldVersion, newVersion);
        PlayListHelper.onUpgrade(db, oldVersion, newVersion);
        ArtistHelper.onUpgrade(db, oldVersion, newVersion);
        AlbumHelper.onUpgrade(db, oldVersion, newVersion);
        ListMemberHelper.onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * 新增播放列表
     */
    public void addPlayList(final String name, ResultCallback<Integer> callback) {
        new DBAsyncTask<Integer>(callback) {
            @Override
            protected Integer run() {
                int result = 0;
                SQLiteDatabase db = getWritableDatabase();
                //检查是否重名
                boolean isExists = PlayListHelper.isExistByName(db, name);
                if (isExists) {
                    result = -1;
                } else {
                    PlayList playList = new PlayList();
                    playList.setType(PlayList.TYPE_USER);
                    playList.setTitle(name);
                    playList.setAddTime(System.currentTimeMillis());
                    long id = PlayListHelper.addPlaylist(db, playList);
                    if (id != -1) {
                        result = 1;
                        sendTableUpdate(PlayListHelper.Columns.TABLE, 0);
                    }
                }
                return result;
            }
        }.execute();
    }

    /**
     * 删除本地歌曲
     */
    public void deleteLocalMusic(final List<Music> musics, final boolean isDeleteFile, ResultCallback<Integer> callback) {
        new DBAsyncTask<Integer>(callback) {
            @Override
            protected Integer run() {
                SQLiteDatabase db = getWritableDatabase();
                db.beginTransaction();
                int deleteNum = 0;
                for (Music music : musics) {
                    //查找是否有播放列表中引用本地歌曲
                    boolean hasRef = ListMemberHelper.isExistByMusicIdAndListId(db, PlayList.TYPE_LOCAL_ID, music.getId());
                    if (!hasRef) {
                        //没有引用，删除歌曲信息
                        MusicHelper.deleteById(db, music.getId());
                    }

                    if (isDeleteFile
                            && !TextUtils.isEmpty(music.getData())) {
                        File file = new File(music.getData());
                        file.delete();
                    }

                    deleteNum += ListMemberHelper.delete(db, PlayList.TYPE_LOCAL_ID, music.getId());
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                if (deleteNum > 0) {
                    sendTableUpdate(PlayListHelper.Columns.TABLE, PlayList.TYPE_LOCAL_ID);
                }
                return deleteNum;
            }
        }.execute();
    }

    /**
     * 从播放列表中移除歌曲
     */
    public void deleteMusicFromPlayList(final long listid, final List<Music> musics, ResultCallback<Integer> callback) {
        new DBAsyncTask<Integer>(callback) {
            @Override
            protected Integer run() {
                int deleteNum = 0;
                SQLiteDatabase db = getWritableDatabase();
                db.beginTransaction();
                try {
                    for (Music music : musics) {
                        long id = music.getId();
                        if (id == 0) {
                            //查找数据库中songid对应的数据
                            Music m = MusicHelper.getMusicBySongId(db, music.getSongId());
                            if (m != null) {
                                id = m.getId();
                            }
                        }
                        deleteNum += ListMemberHelper.delete(db, listid, id);
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    deleteNum = 0;
                } finally {
                    db.endTransaction();
                }

                if (deleteNum > 0) {
                    sendTableUpdate(PlayListHelper.Columns.TABLE, listid);
                }
                return deleteNum;
            }
        }.execute();
    }

    /**
     * 删除列表
     *
     * @param listid
     * @param callback
     */
    public void deletePlayList(final long listid, ResultCallback<Boolean> callback) {
        new DBAsyncTask<Boolean>(callback) {

            @Override
            protected Boolean run() {
                SQLiteDatabase db = getWritableDatabase();
                boolean deleted = PlayListHelper.deleteById(db, listid) > 0;
                if (deleted) {
                    sendTableUpdate(PlayListHelper.Columns.TABLE, listid);
                }
                return deleted;
            }
        }.execute();
    }

    /**
     * 添加收藏歌单
     */
    public void addCollectList(final List<Song> songs, final String title, final String listId, final String pic,
                               ResultCallback<Integer> callback) {
        new DBAsyncTask<Integer>(callback) {
            @Override
            protected Integer run() {
                SQLiteDatabase db = AppDB.get(mContext).getWritableDatabase();
                db.beginTransaction();
                long time = System.currentTimeMillis();
                try {
                    PlayList playList = new PlayList();
                    playList.setIcon(pic);
                    playList.setTitle(title);
                    playList.setListId(listId);
                    playList.setType(PlayList.TYPE_COLLECTION);
                    playList.setAddTime(System.currentTimeMillis());

                    //插入歌单信息
                    long listid = PlayListHelper.addPlaylist(db, playList);
                    if (listid == -1) {
                        return 0;
                    }

                    //插入歌单歌曲信息
                    for (Song song : songs) {
                        String songid = song.songId;

                        long musicid;
                        //是否存有歌曲信息
                        Music m = MusicHelper.getMusicBySongId(db, songid);
                        if (m == null) {
                            musicid = MusicHelper.addMusic(db, song.toMusic());
                        } else {
                            musicid = m.getId();
                        }
                        if (musicid == -1) {
                            return 0;
                        }

                        long insertId = ListMemberHelper.addMusicToList(db, musicid, listid, time);
                        if (insertId == -1) {
                            return 0;
                        }
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    return 0;
                } finally {
                    db.endTransaction();
                }

                sendTableUpdate(PlayListHelper.Columns.TABLE, 0);
                return 1;
            }
        }.execute();
    }


    /**
     * 添加歌曲至列表
     */
    public void addMusicToPlaylist(final PlayList playList, final List<Music> musics, ResultCallback<Integer> callback) {
        new DBAsyncTask<Integer>(callback) {
            @Override
            protected Integer run() {
                int insertNum = 0;//添加成功数量
                long time = System.currentTimeMillis();
                long listid = playList.getId();
                SQLiteDatabase db = getWritableDatabase();
                db.beginTransaction();
                for (int i = 0; i < musics.size(); i++) {
                    Music music = musics.get(i);
                    //检查是否已经存在于列表中
                    boolean isExist = ListMemberHelper.isExistByMusicIdAndListId(db, listid, music.getId());
                    if (!isExist) {
                        long id = ListMemberHelper.addMusicToList(db, music.getId(), listid, time);
                        if (id != -1) {
                            insertNum++;
                        }
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                if (insertNum > 0) {
                    sendTableUpdate(PlayListHelper.Columns.TABLE, 0);
                }
                return insertNum;
            }
        }.execute();
    }

    /**
     * 是否在我喜欢列表中的歌曲
     */
    public void isFavoriteMusic(final Music music, ResultCallback<Boolean> callback) {
        new DBAsyncTask<Boolean>(callback) {
            @Override
            protected Boolean run() {
                Boolean result = false;
                SQLiteDatabase db = getWritableDatabase();
                long id = music.getId();
                if (id == 0) {
                    String songid = music.getSongId();
                    Music music = MusicHelper.getMusicBySongId(db, songid);
                    if (music != null) {
                        id = music.getId();
                    }
                }
                if (music.getType() == Music.TYPE_WEB) {
                    //网络歌曲,先查在数据库中的id
                }
                if (id != 0) {
                    result = ListMemberHelper.isExistByMusicIdAndListId(db, PlayList.TYPE_FAVORITE_ID, id);
                }
                return result;
            }
        }.execute();
    }

    /**
     * 添加歌曲到我喜欢
     */
    public void addMusicToFavorite(final Music music, ResultCallback<Boolean> callback) {
        new DBAsyncTask<Boolean>(callback) {
            @Override
            protected Boolean run() {
                boolean success = false;
                SQLiteDatabase db = getWritableDatabase();
                db.beginTransaction();
                try {
                    long id = music.getId();
                    if (id == 0) {
                        Music m = MusicHelper.getMusicBySongId(db, music.getSongId());
                        if (m != null) {
                            id = m.getId();
                        }
                    }
                    if (id == 0) {
                        id = MusicHelper.addMusic(db, music);
                    }
                    if (id > 0) {
                        long time = System.currentTimeMillis();
                        long insertId = ListMemberHelper.addMusicToList(db, id, PlayList.TYPE_FAVORITE_ID, time);
                        if (insertId != -1) {
                            success = true;
                        }
                        db.setTransactionSuccessful();
                    }
                } catch (Exception e) {

                } finally {
                    db.endTransaction();
                }
                if (success) {
                    sendTableUpdate(PlayListHelper.Columns.TABLE, PlayList.TYPE_FAVORITE_ID);
                }
                return success;
            }
        }.execute();
    }

    /**
     * 修改播放列表名
     */
    public void updatePlayListName(final long listid, final String newName, ResultCallback<Integer> callback) {
        new DBAsyncTask<Integer>(callback) {

            @Override
            protected Integer run() {
                int result = 0;
                SQLiteDatabase db = AppDB.get(mContext).getWritableDatabase();
                //检查列表名是否已存在
                boolean isExistName = PlayListHelper.isExistByName(db, newName);
                if (isExistName) {
                    //列表名已存在
                    result = -1;
                } else {
                    int num = PlayListHelper.updateName(db, listid, newName);
                    if (num > 0) {
                        result = 1;
                        sendTableUpdate(PlayListHelper.Columns.TABLE, 0);
                    }
                }
                return result;
            }
        }.execute();
    }

    /**
     * 更新本地歌曲
     */
    public void updateLocalMusic(final List<Music> musics, final boolean updateAll, ResultCallback<Void> callback) {
        new DBAsyncTask<Void>(callback) {

            @Override
            protected Void run() {
                long time = System.currentTimeMillis();
                SQLiteDatabase db = getWritableDatabase();
                db.beginTransaction();

                //插入数据
                List<Long> ids = new LinkedList<>();
                for (Music music : musics) {
                    //检查是否已经存在,对比文件路径和修改时间
                    Cursor c = db.query(
                            MusicHelper.Columns.TABLE,
                            new String[]{BaseHelper.ID, MusicHelper.Columns.DATE_MODIFIED},
                            MusicHelper.Columns.DATA + "=?",
                            new String[]{music.getData()}, null, null, null);
                    if (c.moveToFirst()) {
                        long id = c.getLong(0);
                        long dateModified = c.getLong(1);
                        if (music.getModifiedDate() == dateModified) {
                            ids.add(id);
                            continue;
                        }
                    }
                    c.close();
                    long id = MusicHelper.addMusic(db, music);
                    ids.add(id);
                }

                if (updateAll) {
                    //全盘扫描，更新全部
                    ListMemberHelper.deleteByListId(db, PlayList.TYPE_LOCAL_ID);

                    List<Music> locals = MusicHelper.getMusicByType(db, Music.TYPE_LOCAL);
                    for (Music localMusic : locals) {
                        long musicid = localMusic.getId();
                        String dataPath = localMusic.getData();
                        File file = new File(dataPath);
                        if (file.exists()) {
                            ListMemberHelper.addMusicToList(db, musicid, PlayList.TYPE_LOCAL_ID, time);
                        }
                    }
                } else {
                    for (Long id : ids) {
                        if (ListMemberHelper.isExistByMusicIdAndListId(db, PlayList.TYPE_LOCAL_ID, id)) {
                            continue;
                        }
                        ListMemberHelper.addMusicToList(db, id, PlayList.TYPE_LOCAL_ID, time);
                    }
                }

                db.setTransactionSuccessful();
                db.endTransaction();
                sendTableUpdate(PlayListHelper.Columns.TABLE, PlayList.TYPE_LOCAL_ID);
                return null;
            }
        }.execute();
    }

    /**
     * 获取用户需要看到的播放列表
     */
    public void getPlayList(ResultCallback<List<PlayList>> callback) {
        new DBAsyncTask<List<PlayList>>(callback) {

            @Override
            protected List<PlayList> run() {
                SQLiteDatabase db = getWritableDatabase();
                List<PlayList> playLists = new ArrayList<>();
                playLists.addAll(PlayListHelper.getListByType(db, PlayList.TYPE_COLLECTION));
                playLists.addAll(PlayListHelper.getListByType(db, PlayList.TYPE_FAVORITE));
                playLists.addAll(PlayListHelper.getListByType(db, PlayList.TYPE_USER));
                for (PlayList playList : playLists) {
                    playList.setSongNum(ListMemberHelper.getMusicCountByListid(db, playList.getId()));
                }
                return playLists;
            }
        }.execute();
    }

    /**
     * 获取列表音乐
     */
    public void getPlayListMusics(final long listid, ResultCallback<List<Music>> callback) {
        new DBAsyncTask<List<Music>>(callback) {
            @Override
            protected List<Music> run() {
                SQLiteDatabase db = getWritableDatabase();
                List<Music> musics = ListMemberHelper.getMusicsByListId(db, listid);
                Collections.sort(musics, new Comparator<Music>() {
                    @Override
                    public int compare(Music lhs, Music rhs) {
                        return lhs.getTitleKey().compareTo(rhs.getTitleKey());
                    }
                });
                return musics;
            }
        }.execute();
    }

    public static String tableChangeAction(String table) {
        return "com.database." + table + ".update";
    }

    private void sendTableUpdate(String table, long id) {
        Intent intent = new Intent(tableChangeAction(table));
        intent.putExtra("id", id);
        LocalBroadcastManager.getInstance(mContext)
                .sendBroadcast(intent);
    }
}
