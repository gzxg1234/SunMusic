package com.sanron.music.fragments.pagermymusic;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.sanron.music.db.DataProvider;
import com.sanron.music.fragments.base.LazyLoadFragment;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by sanron on 16-3-28.
 */
public abstract class BaseDataFragment extends LazyLoadFragment implements Observer {


    private String[] mObserveTables;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        DataProvider.get().addObserver(this);
    }

    protected void setObserveTable(String... table) {
        mObserveTables = table;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataProvider.get().deleteObserver(this);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (mObserveTables != null && data != null) {
            for (String table : mObserveTables) {
                if (table.equals(data)) {
                    loadData();
                    break;
                }
            }
        }
    }

}
