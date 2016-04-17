package com.sanron.music.utils;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListAdapter;

/**
 * Created by sanron on 16-4-17.
 */
public class ListViewUtils {
    public static int computeTotalHeight(AbsListView absListView) {
        ListAdapter adapter = absListView.getAdapter();
        if (adapter == null) {
            return 0;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, absListView);
            view.measure(0, 0);
            totalHeight += view.getMeasuredHeight();
        }
        return totalHeight;
    }
}
