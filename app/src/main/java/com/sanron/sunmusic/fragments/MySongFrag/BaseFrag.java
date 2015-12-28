package com.sanron.sunmusic.fragments.MySongFrag;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sanron.sunmusic.db.DBHelper;
import com.sanron.sunmusic.db.DataProvider;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Administrator on 2015/12/29.
 */
public class BaseFrag extends Fragment implements Observer {

    protected View contentView;
    private int mFragmentId;
    private int mType;
    private String mBindTable;
    private RecyclerView.Adapter mAdapter;

    public static final int FRAG_PLAYLIST = 1;
    public static final int FRAG_RECENT_PLAY = 2;
    public static final int FRAG_LOCAL_SONG = 3;
    public static final int FRAG_ARTIST = 4;
    public static final int FRAG_ALBUM = 5;

    public static final int LAYOUT_LIST = 1;//列表
    public static final int LAYOUT_STAGGERED = 2;//瀑布流

    public static Fragment newInstance(int fragmentid,int type){
        BaseFrag fragment = new BaseFrag();
        fragment.mFragmentId = fragmentid;
        fragment.mType = type;
        switch (fragmentid){
            case FRAG_PLAYLIST:{
                fragment.mBindTable = DBHelper.TABLE_PLAYLIST;
            }break;
            case FRAG_LOCAL_SONG:{
                fragment.mBindTable = DBHelper.TABLE_SONG;
            }break;
            case FRAG_ARTIST:{
                fragment.mBindTable = DBHelper.TABLE_ARTIST;
            }break;
            case FRAG_ALBUM:{
                fragment.mBindTable = DBHelper.TABLE_ALBUM;
            }break;
            case FRAG_RECENT_PLAY:{
            }break;
        }
        return fragment;
    }

    private BaseFrag(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        DataProvider.instance().addObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataProvider.instance().deleteObserver(this);
    }

    public <T extends View> T $(int id){
        return (T) contentView.findViewById(id);
    }

    @Override
    public void update(Observable observable, Object data) {
        if(mBindTable.equals(data)){

        }
    }
}