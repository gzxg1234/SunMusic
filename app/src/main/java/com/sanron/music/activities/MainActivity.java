package com.sanron.music.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
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

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.sanron.music.R;
import com.sanron.music.db.model.PlayList;
import com.sanron.music.fragments.BaseFragment;
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
import com.sanron.music.fragments.WebMusic.SongListFrag;
import com.sanron.music.utils.MyLog;
import com.sanron.music.view.NavigationHeader;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarLayout appBar;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private FragmentManager fm;
    private MaterialMenuDrawable materialMenu;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private PlayerFrag playerFrag;
    private String currentFragmentTag;//当前显示的fragment;

    private List<BackPressedHandler> backPressedHandlers;

    private NavigationView navigationView;

    private boolean isShowingPlayListSongsFrag = false;//是否在显示列表歌曲fragment

    public static final String TAG = MainActivity.class.getName();

    public static final String TAG_MYMUSIC = "MyMusic";
    public static final String TAG_WEBMUSIC = "WebMusic";
    private Map<String, String> titles;



    private BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra(BaseFragment.EXTRA_FROM);
            int event = intent.getIntExtra(BaseFragment.EXTRA_EVENT, -1);
            if (PlayListFrag.class.getName().equals(from)) {
                //
                switch (event) {
                    case PlayListFrag.EVENT_CLICK_LIST: {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        PlayList playList = (PlayList) intent.getSerializableExtra(PlayListFrag.EXTRA_PLAYLIST);
                        showPlayListSongs(playList);
                    }
                    break;
                }

            } else if (PlayerFrag.class.getName().equals(from)) {
                //
                switch (event) {
                    case PlayerFrag.EVENT_CLICK_BACK: {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    break;
                }
            } else if (RecmdFrag.class.getName().equals(from)) {
                switch (event) {
                    case RecmdFrag.EVENT_CLICK_SONGLIST: {
                        String listId = intent.getStringExtra(RecmdFrag.EXTRA_SONGLIST_ID);
                        fm.beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_right,
                                        R.anim.slide_out_right,
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_right)
                                .add(R.id.fragment_container_1, SongListFrag.newInstance(listId),
                                        SongListFrag.class.getName())
                                .addToBackStack(SongListFrag.class.getName())
                                .commit();
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    public void addBackPressedHandler(BackPressedHandler handler) {
        if (backPressedHandlers != null) {
            backPressedHandlers.add(handler);
        }
    }

    public void removeBackPressedHandler(BackPressedHandler handler) {
        if (backPressedHandlers != null) {
            backPressedHandlers.remove(handler);
        }
    }

    private void initView(Bundle savedInstanceState) {
        titles = new HashMap<>();
        backPressedHandlers = new ArrayList<>();
        NavigationHeader navigationHeader = new NavigationHeader(this, appContext.getMusicPlayer());
        fm = getSupportFragmentManager();
        slidingUpPanelLayout = $(R.id.sliding_panel);
        navigationView = $(R.id.navigation_view);
        drawerLayout = $(R.id.drawer_layout);
        toolbar = $(R.id.toolbar);
        appBar = $(R.id.app_bar);
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        navigationView.addHeaderView(navigationHeader);
        setSupportActionBar(toolbar);

        titles.put(TAG_MYMUSIC, "我的音乐");
        titles.put(TAG_WEBMUSIC, "音乐库");
        navigationView.setNavigationItemSelectedListener(this);

        appContext.setViewFitsStatusBar(appBar);
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
                    .add(R.id.player_frag_container, playerFrag, PlayerFrag.class.getName())
                    .commit();
            changeToFragment(TAG_MYMUSIC);
        } else {
            SaveState saveState = savedInstanceState.getParcelable("savestate");
            currentFragmentTag = saveState.currentFragTag;
            playerFrag = (PlayerFrag) fm.findFragmentByTag(PlayerFrag.class.getName());
            isShowingPlayListSongsFrag = saveState.isShowingPlayListSongsFrag;
            MaterialMenuDrawable.IconState iconState = saveState.menuState;
            if (iconState != null) {
                materialMenu.setIconState(iconState);
            }
            toolbar.setTitle(saveState.titleText);
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

        LocalBroadcastManager.getInstance(this).registerReceiver(eventReceiver,
                new IntentFilter(BaseFragment.ACTION_FRAG_EVENT));
    }

    /**
     * 显示列表歌曲
     */
    public void showPlayListSongs(PlayList playList) {
        ListMusicFrag listMusicFrag = ListMusicFrag.newInstance(playList);
        fm.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_top,
                        R.anim.slide_out_bottom,
                        R.anim.slide_in_top,
                        R.anim.slide_out_bottom)
                .add(R.id.fragment_container_2, listMusicFrag, ListMusicFrag.TAG)
                .addToBackStack(ListMusicFrag.class.getName())
                .commit();
        toolbar.setTitle(playList.getTitle());
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
        isShowingPlayListSongsFrag = true;
    }

    public void dismissPlayListSongs() {
        if (fm.popBackStackImmediate(ListMusicFrag.class.getName(),
                FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
            toolbar.setTitle("我的音乐");
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
            isShowingPlayListSongsFrag = false;
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressedHandlers.size() > 0) {
            for (int i = 0; i < backPressedHandlers.size(); i++) {
                if (backPressedHandlers.get(i).onBackPressed()) {
                    return;
                }
            }
        }
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
        fm.beginTransaction().replace(R.id.fragment_container_2, fragment, tag)
                .commit();
        toolbar.setTitle(titles.get(tag));
        currentFragmentTag = tag;
    }

    private Fragment instanceFragment(String tag) {
        Fragment fragment = null;
        switch (tag) {
            case TAG_MYMUSIC: {
                String[] titles = new String[]{"我的歌单", "最近播放", "本地音乐", "艺术家", "专辑"};
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(eventReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SaveState saveState = new SaveState(currentFragmentTag,
                isShowingPlayListSongsFrag,
                toolbar != null ? toolbar.getTitle().toString() : null,
                materialMenu != null ? materialMenu.getIconState() : null);
        outState.putParcelable("savestate", saveState);
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

            case R.id.menu_download_manager: {

            }
            break;

            case R.id.menu_setting: {

            }
            break;

            case R.id.menu_close_app: {
                appContext.closeApp();
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

    static class SaveState implements Parcelable {

        boolean isShowingPlayListSongsFrag;
        String titleText;
        MaterialMenuDrawable.IconState menuState;
        String currentFragTag;

        public static final Creator<SaveState> CREATOR = new Creator<SaveState>() {
            @Override
            public SaveState createFromParcel(Parcel in) {
                return new SaveState(in);
            }

            @Override
            public SaveState[] newArray(int size) {
                return new SaveState[size];
            }
        };

        public SaveState(String currentFragTag, boolean isShowingPlayListSongsFrag, String titleText, MaterialMenuDrawable.IconState menuState) {
            this.currentFragTag = currentFragTag;
            this.isShowingPlayListSongsFrag = isShowingPlayListSongsFrag;
            this.titleText = titleText;
            this.menuState = menuState;
        }

        protected SaveState(Parcel in) {
            isShowingPlayListSongsFrag = in.readByte() == 1;
            titleText = in.readString();
            currentFragTag = in.readString();
            String stateString = in.readString();
            try {
                menuState = stateString != null ? Enum.valueOf(MaterialMenuDrawable.IconState.class,
                        stateString) : MaterialMenuDrawable.IconState.BURGER;
            } catch (IllegalArgumentException e) {
                menuState = MaterialMenuDrawable.IconState.BURGER;
            }
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (isShowingPlayListSongsFrag ? 1 : 0));
            dest.writeString(titleText);
            dest.writeString(currentFragTag);
            if (menuState != null) {
                dest.writeString(menuState.toString());
            }
        }
    }

    public interface BackPressedHandler {
        boolean onBackPressed();
    }
}
