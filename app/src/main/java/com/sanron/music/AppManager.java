package com.sanron.music;

import android.app.Activity;

import java.util.Stack;

/**
 * Created by Administrator on 2016/3/15.
 */
public class AppManager {
    private Stack<Activity> mActivities;
    private static volatile AppManager mInstance;

    public static AppManager instance() {
        if (mInstance == null) {
            synchronized (AppManager.class) {
                if (mInstance == null) {
                    mInstance = new AppManager();
                }
            }
        }
        return mInstance;
    }

    private AppManager() {
        mActivities = new Stack<>();
    }

    public void addActivity(Activity activity) {
        mActivities.add(activity);
    }

    public void removeActivity(Activity activity) {
        mActivities.remove(activity);
    }

    public void finishActivity(Activity activity) {
        mActivities.remove(activity);
        activity.finish();
    }

    public Activity currentActivity() {
        if (mActivities.size() > 0) {
            return mActivities.peek();
        }
        return null;
    }

    public void finishAllActivity() {
        for (Activity activity : mActivities) {
            activity.finish();
        }
        mActivities.clear();
    }

}
