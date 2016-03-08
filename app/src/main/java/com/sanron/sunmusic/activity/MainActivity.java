package com.sanron.sunmusic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.sanron.sunmusic.R;
import com.sanron.sunmusic.fragments.MyMusicFrag.ListMusicFrag;
import com.sanron.sunmusic.fragments.MyMusicFrag.MyMusicFrag;
import com.sanron.sunmusic.fragments.MyMusicFrag.PlayListFrag;
import com.sanron.sunmusic.fragments.PlayerFrag;
import com.sanron.sunmusic.model.PlayList;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public class MainActivity extends BaseActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private FragmentManager fm;
    private MaterialMenuDrawable materialMenu;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private PlayerFrag playerFrag;
    private String curFragTag;
    private boolean isShowingPlayListSongsFrag = false;//是否在显示列表歌曲fragment

    public static final String TAG = MainActivity.class.getName();

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int event = intent.getIntExtra("event", -1);
            if (PlayListFrag.class.getName().equals(action)) {
                //
                switch (event) {
                    case PlayListFrag.EVENT_CLICK_LIST: {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        PlayList playList = (PlayList) intent.getSerializableExtra(PlayListFrag.EXTRA_PLAYLIST);
                        showPlayListSongs(playList);
                    }
                    break;
                }

            } else if (PlayerFrag.class.getName().equals(action)) {
                //
                switch (event) {
                    case PlayerFrag.EVENT_CLICK_BACK: {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    break;
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTranslucentStatusBar();
        setContentView(R.layout.activity_main);
        initView();
    }

    public void setTranslucentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    private void initView() {

        slidingUpPanelLayout = $(R.id.sliding_panel);
        drawerLayout = $(R.id.drawerlayout);
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);

        toolbar = $(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(materialMenu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowingPlayListSongsFrag) {
                    dismissPlayListSongs();
                } else {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            }
        });

        fm = getSupportFragmentManager();
        playerFrag = new PlayerFrag();
        fm.beginTransaction()
                .add(R.id.player_contanier, playerFrag)
                .commit();
        setCurrentFragment(MyMusicFrag.TAG);

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0) {
                    playerFrag.setSmallControllerVisibility(View.VISIBLE);
                } else {
                    playerFrag.setSmallControllerVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    slidingUpPanelLayout.setTouchEnabled(true);
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    slidingUpPanelLayout.setTouchEnabled(false);
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(PlayerFrag.class.getName()));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(PlayListFrag.class.getName()));
    }

    /**
     * 显示列表歌曲
     */
    public void showPlayListSongs(PlayList playList) {
        fm.beginTransaction()
                .setCustomAnimations(R.anim.frag_slide_in,
                        R.anim.frag_slide_out,
                        R.anim.frag_slide_in,
                        R.anim.frag_slide_out)
                .add(R.id.fragment_container, ListMusicFrag.newInstance(playList), ListMusicFrag.TAG)
                .addToBackStack(ListMusicFrag.class.getSimpleName())
                .commit();
        toolbar.setTitle(playList.getName());
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
        isShowingPlayListSongsFrag = true;
    }

    public void dismissPlayListSongs() {
        if (fm.popBackStackImmediate(ListMusicFrag.class.getSimpleName(),
                FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
            toolbar.setTitle("我的音乐");
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
            isShowingPlayListSongsFrag = false;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            //收缩播放器面板
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (isShowingPlayListSongsFrag) {
            dismissPlayListSongs();
        } else {
            super.onBackPressed();
        }
    }

    private void setCurrentFragment(String tag) {
        Fragment fragment = null;
        if (MyMusicFrag.TAG.equals(tag)) {
            fragment = MyMusicFrag.newInstance();
            toolbar.setTitle("我的音乐");
        }
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
        curFragTag = tag;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
