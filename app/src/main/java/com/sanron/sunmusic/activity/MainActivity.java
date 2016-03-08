package com.sanron.sunmusic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.sanron.sunmusic.R;
import com.sanron.sunmusic.fragments.MyMusic.AlbumFrag;
import com.sanron.sunmusic.fragments.MyMusic.ArtistFrag;
import com.sanron.sunmusic.fragments.MyMusic.ListMusicFrag;
import com.sanron.sunmusic.fragments.MyMusic.LocalMusicFrag;
import com.sanron.sunmusic.fragments.MyMusic.PlayListFrag;
import com.sanron.sunmusic.fragments.MyMusic.RecentPlayFrag;
import com.sanron.sunmusic.fragments.PagerFragment;
import com.sanron.sunmusic.fragments.PlayerFrag;
import com.sanron.sunmusic.model.PlayList;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, SlidingUpPanelLayout.PanelSlideListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private FragmentManager fm;
    private MaterialMenuDrawable materialMenu;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private NavigationView navigationView;
    private PlayerFrag playerFrag;

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
        fm = getSupportFragmentManager();

        navigationView = $(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.menu_my_music);

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

        slidingUpPanelLayout = $(R.id.sliding_panel);
        slidingUpPanelLayout.addPanelSlideListener(this);

        playerFrag = new PlayerFrag();
        fm.beginTransaction()
                .add(R.id.player_contanier, playerFrag,PlayerFrag.class.getName())
                .commit();

        toMyMusic();

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
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
        isShowingPlayListSongsFrag = true;
    }

    public void dismissPlayListSongs() {
        if (fm.popBackStackImmediate(ListMusicFrag.class.getSimpleName(),
                FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
            toolbar.setTitle("我的音乐");
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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

    private void toMyMusic(){
        toolbar.setTitle("我的音乐");
        String[] titles = new String[]{"播放列表","最近播放","本地音乐","艺术家","专辑"};
        Class[] fragments = new Class[]{PlayListFrag.class, RecentPlayFrag.class,
                LocalMusicFrag.class, ArtistFrag.class, AlbumFrag.class};
        fm.beginTransaction()
                .replace(R.id.fragment_container, new PagerFragment(fragments,titles),"MyMusic")
                .commit();
    }

    private void toWebMusic(){
//        toolbar.setTitle("音乐库");
//        fm.beginTransaction()
//                .replace(R.id.fragment_container, new PagerFragment(fragments,titles),"MyMusic")
//                .commit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_my_music:{
                toMyMusic();
                navigationView.setCheckedItem(R.id.menu_my_music);
            }break;

            case R.id.menu_web_music:{

            }break;

            case R.id.menu_setting:{

            }break;

        }
        return false;
    }

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
}
