package com.sanron.sunmusic.activities;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.sanron.sunmusic.R;
import com.sanron.sunmusic.fragment.BaseFragment;
import com.sanron.sunmusic.fragment.MySongFrag;
import com.sanron.sunmusic.music.LocalMusicLoader;

import java.util.List;

public class MainActivity extends BaseActivity {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    FragmentManager fm;

    public static final String TAG_MYSONG = "MyMusic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){

        drawerLayout = $(R.id.drawerlayout);

        toolbar = $(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                }else{
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        fm = getSupportFragmentManager();
        setCurrentFragment(TAG_MYSONG);
    }


    private void setCurrentFragment(String tag){
        Fragment baseFragment = null;

        if(TAG_MYSONG.equals(tag)){
            baseFragment = MySongFrag.newInstance();
            toolbar.setTitle("我的音乐");
        }
        fm.beginTransaction()
                .replace(R.id.fragment_container,baseFragment,tag)
                .commit();
    }
}
