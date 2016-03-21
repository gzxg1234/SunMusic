package com.sanron.music.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.sanron.music.AppManager;
import com.sanron.music.R;
import com.sanron.music.fragments.MyMusic.AlbumFrag;
import com.sanron.music.fragments.MyMusic.ArtistFrag;
import com.sanron.music.fragments.MyMusic.ListMusicFrag;
import com.sanron.music.fragments.MyMusic.LocalMusicFrag;
import com.sanron.music.fragments.MyMusic.PlayListFrag;
import com.sanron.music.fragments.MyMusic.RecentPlayFrag;
import com.sanron.music.fragments.PagerFragment;
import com.sanron.music.fragments.PlayerFrag;
import com.sanron.music.fragments.WebMusic.BillboardFrag;
import com.sanron.music.fragments.WebMusic.GedanFrag;
import com.sanron.music.fragments.WebMusic.RadioFrag;
import com.sanron.music.fragments.WebMusic.RecmdFrag;
import com.sanron.music.fragments.WebMusic.SingerFrag;
import com.sanron.music.db.PlayList;
import com.sanron.music.utils.MyLog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private FragmentManager fm;
    private MaterialMenuDrawable materialMenu;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private NavigationView navigationView;
    private PlayerFrag playerFrag;
    private String currentFragmentTag;//当前显示的fragment;

    private boolean isShowingPlayListSongsFrag = false;//是否在显示列表歌曲fragment

    public static final String TAG = MainActivity.class.getName();

    public static final String TAG_MYMUSIC = "MyMusic";
    public static final String TAG_WEBMUSIC = "WebMusic";
    private Map<String, String> titles = new HashMap<>();

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
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTranslucentStatusBar();
        setContentView(R.layout.activity_main);
        ServiceConnection callback = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                initView(savedInstanceState);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                MyLog.e(TAG, "service disconnected!");
                finish();
            }
        };

        if (!appContext.bindService(callback)) {
            MyLog.e(TAG, "can't bind service!");
            finish();
        }
    }


    private void setTranslucentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    private void initView(Bundle savedInstanceState) {
        titles.put(TAG_MYMUSIC, "我的音乐");
        titles.put(TAG_WEBMUSIC, "音乐库");

        fm = getSupportFragmentManager();
        slidingUpPanelLayout = $(R.id.sliding_panel);
        navigationView = $(R.id.navigation_view);
        drawerLayout = $(R.id.drawerlayout);
        toolbar = $(R.id.toolbar);
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);

        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);

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

        if (savedInstanceState == null) {
            playerFrag = new PlayerFrag();
            fm.beginTransaction()
                    .add(R.id.player_contanier, playerFrag, PlayerFrag.class.getName())
                    .commit();
            changeToFragment(TAG_MYMUSIC);
        } else {
            currentFragmentTag = savedInstanceState.getString("currentFragmentTag");

            playerFrag = (PlayerFrag) fm.findFragmentByTag(PlayerFrag.class.getName());
            isShowingPlayListSongsFrag = savedInstanceState.getBoolean("isShowingPlayListSongsFrag");
            MaterialMenuDrawable.IconState iconState = (MaterialMenuDrawable.IconState) savedInstanceState.getSerializable("materialMenuState");
            if (iconState != null) {
                materialMenu.setIconState(iconState);
            }
            toolbar.setTitle(savedInstanceState.getCharSequence("title"));
        }

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
        ListMusicFrag listMusicFrag = new ListMusicFrag();
        Bundle bundle = new Bundle();
        bundle.putSerializable("playList", playList);
        listMusicFrag.setArguments(bundle);
        fm.beginTransaction()
                .setCustomAnimations(R.anim.frag_slide_in,
                        R.anim.frag_slide_out,
                        R.anim.frag_slide_in,
                        R.anim.frag_slide_out)
                .add(R.id.fragment_container, listMusicFrag, ListMusicFrag.TAG)
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

    private void changeToFragment(String tag) {
        if (tag.equals(currentFragmentTag)) {
            return;
        }

        Fragment fragment = instanceFragment(tag);
        fm.beginTransaction().replace(R.id.fragment_container, fragment, tag)
                .commit();
        toolbar.setTitle(titles.get(tag));
        currentFragmentTag = tag;
    }

//    private void changeToFragment(String tag) {
//        FragmentTransaction ft = fm.beginTransaction();
//        Fragment fragment = fm.findFragmentByTag(tag);
//        if (fragment == null) {
//            fragment = instanceFragment(tag);
//            ft.add(R.id.fragment_container, fragment, tag);
//        } else {
//            ft.show(fragment);
//        }
//        if(currentFragment != null){
//            ft.hide(currentFragment);
//        }
//        ft.commit();
//        currentFragment = fragment;
//        toolbar.setTitle(titles.get(tag));
//    }

    private Fragment instanceFragment(String tag) {
        Fragment fragment = null;
        switch (tag) {
            case TAG_MYMUSIC: {
                String[] titles = new String[]{"播放列表", "最近播放", "本地音乐", "艺术家", "专辑"};
                String[] fragments = new String[]{PlayListFrag.class.getName(), RecentPlayFrag.class.getName(),
                        LocalMusicFrag.class.getName(), ArtistFrag.class.getName(), AlbumFrag.class.getName()};
                fragment = PagerFragment.newInstance(titles, fragments);
            }
            break;

            case TAG_WEBMUSIC: {
                String[] titles = new String[]{"推荐", "排行", "歌手", "歌单", "电台"};
                String[] fragments = new String[]{RecmdFrag.class.getName(), BillboardFrag.class.getName(),
                        SingerFrag.class.getName(), GedanFrag.class.getName(), RadioFrag.class.getName()};
                fragment = PagerFragment.newInstance(titles, fragments);
            }
            break;
        }
        return fragment;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isShowingPlayListSongsFrag", isShowingPlayListSongsFrag);
        outState.putSerializable("materialMenuState", materialMenu.getIconState());
        outState.putCharSequence("title", toolbar.getTitle());
        outState.putString("currentFragmentTag", currentFragmentTag);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_my_music: {
                changeToFragment(TAG_MYMUSIC);
                navigationView.setCheckedItem(item.getItemId());
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            break;

            case R.id.menu_web_music: {
                changeToFragment(TAG_WEBMUSIC);
                navigationView.setCheckedItem(item.getItemId());
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            break;

            case R.id.menu_setting: {
            }
            break;

            case R.id.menu_close_app: {
                AppManager.instance().finishAllActivity();
                appContext.tryToStopService();
            }
            break;
        }
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (currentFragmentTag) {
            case TAG_WEBMUSIC: {
                getMenuInflater().inflate(R.menu.option_menu_webmusicfrag, menu);
            }
            break;
            case TAG_MYMUSIC: {
                getMenuInflater().inflate(R.menu.option_menu_mymusicfrag, menu);
            }
            break;
        }
        return true;
    }
}
