package com.sanron.music;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Stack;

/**
 * Created by Administrator on 2016/3/15.
 */
public class AppManager {
    private Stack<Activity> activities;
    private static AppManager instance;

    public static AppManager instance(){
        if(instance == null){
            synchronized (AppManager.class){
                if(instance == null){
                    instance = new AppManager();
                }
            }
        }
        return instance;
    }

    private AppManager(){
        activities = new Stack<>();
    }

    public void addActivity(Activity activity){
        activities.add(activity);
    }

    public void removeActivity(Activity activity){
        activities.remove(activity);
    }

    public void finishActivity(Activity activity){
        activities.remove(activity);
        activity.finish();
    }

    public Activity currentActivity(){
        if(activities.size() > 0){
            return activities.peek();
        }
        return null;
    }

    public void finishAllActivity(){
        for(Activity activity:activities){
            activity.finish();
        }
        activities.clear();
    }

}
