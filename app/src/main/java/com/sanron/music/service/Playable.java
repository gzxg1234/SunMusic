package com.sanron.music.service;

import android.net.Uri;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by sanron on 16-3-21.
 */
public abstract class Playable implements Serializable {

    public static final int TYPE_FILE = 1;
    public static final int TYPE_HTTP = 2;

    public abstract String url();

    public abstract String title();

    public abstract String album();

    public abstract String artist();

    public abstract String pic();

    public int type(){
        String url = url();
        if(!TextUtils.isEmpty(url)){
            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            if ("file".equals(scheme)) {
                return TYPE_FILE;
            } else if("http".equals(scheme)){
                return TYPE_HTTP;
            }
        }
        return 0;
    }
}
