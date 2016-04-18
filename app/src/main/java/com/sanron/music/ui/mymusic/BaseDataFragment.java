package com.sanron.music.ui.mymusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sanron.music.R;
import com.sanron.music.db.DataProvider;
import com.sanron.music.ui.LazyLoadFragment;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by sanron on 16-3-28.
 */
public abstract class BaseDataFragment extends LazyLoadFragment implements Observer {


    public static final int ALTERNATIVE_GROUP_ID = 1;
    private String[] mObserveTables;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        DataProvider.instance().addObserver(this);
    }


    protected void setObserveTable(String... table) {
        mObserveTables = table;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataProvider.instance().deleteObserver(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_refresh: {
                loadData();
            }
            break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeGroup(ALTERNATIVE_GROUP_ID);
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
