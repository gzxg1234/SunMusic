package com.sanron.sunmusic.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.sanron.sunmusic.R;
import com.sanron.sunmusic.fragments.MySongFrag.MySongFrag;
import com.sanron.sunmusic.fragments.MySongFrag.PlayListFrag;
import com.sanron.sunmusic.fragments.MySongFrag.PlayListSongsFrag;
import com.sanron.sunmusic.model.PlayList;

import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private FragmentManager fm;
    private MaterialMenuDrawable materialMenu;
    private String curFragTag;

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        drawerLayout = $(R.id.drawerlayout);
        toolbar = $(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MySongFrag.TAG.equals(curFragTag)) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }else if(PlayListSongsFrag.TAG.endsWith(curFragTag)){
                    fm.popBackStackImmediate();
                    curFragTag = MySongFrag.TAG;
                    toolbar.setTitle("我的音乐");
                    materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
                }
            }
        });
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        toolbar.setNavigationIcon(materialMenu);

        fm = getSupportFragmentManager();
        setCurrentFragment(MySongFrag.TAG);

    }

    public void onEventMainThread(PlayListFrag.ClickListEvent clickListEvent){
        PlayList playList = clickListEvent.getPlayList();
        fm.beginTransaction()
                .setCustomAnimations(R.anim.frag_slide_in,
                        R.anim.frag_slide_out,
                        R.anim.frag_slide_in,
                        R.anim.frag_slide_out)
                .add(R.id.fragment_container, PlayListSongsFrag.newInstance(playList),PlayListSongsFrag.TAG)
                .addToBackStack("showlistsongs")
                .commit();
        curFragTag = PlayListSongsFrag.TAG;
        toolbar.setTitle(playList.getName());
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(PlayListSongsFrag.TAG.equals(curFragTag)){
            curFragTag = MySongFrag.TAG;
            toolbar.setTitle("我的音乐");
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
        }
    }

    private void setCurrentFragment(String tag) {
        Fragment fragment = null;
        if (MySongFrag.TAG.equals(tag)) {
            fragment = MySongFrag.newInstance();
            toolbar.setTitle("我的音乐");
        }
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment,tag)
                .commit();
        curFragTag = tag;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
