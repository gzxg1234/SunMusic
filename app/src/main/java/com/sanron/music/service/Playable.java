package com.sanron.music.service;

import android.net.Uri;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by sanron on 16-3-21.
 */
public abstract class Playable implements Serializable {

    public static final int TYPE_FILE = 1;
    public static final int TYPE_WEB = 2;

    public abstract String uri();

    public abstract String title();

    public abstract String album();

    public abstract String artist();

    public abstract String pic();

    public abstract int type();
}
