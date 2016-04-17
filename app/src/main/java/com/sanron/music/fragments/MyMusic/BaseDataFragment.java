package com.sanron.music.fragments.MyMusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sanron.music.R;
import com.sanron.music.db.DataProvider;
import com.sanron.music.fragments.BaseFragment;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by sanron on 16-3-28.
 */
public abstract class BaseDataFragment extends BaseFragment implements Observer {


    public static final int ALTERNATIVE_GROUP_ID = 1;
    private String[] observeTables;
    private boolean hasLoadData;

    protected abstract void refreshData();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        DataProvider.instance().addObserver(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!hasLoadData) {
            refreshData();
            hasLoadData = true;
        }
    }

    protected void setObserveTable(String... table) {
        observeTables = table;
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
                refreshData();
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
        if (observeTables != null && data != null) {
            for (String table : observeTables) {
                if (table.equals(data)) {
                    if (isVisible()) {
                        //可见时刷新
                        refreshData();
                    } else {
                        hasLoadData = false;
                    }
                    break;
                }
            }
        }
    }

}
