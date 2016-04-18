package com.sanron.music.activities;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.sanron.music.AppContext;
import com.sanron.music.R;
import com.sanron.music.common.ViewTool;
import com.sanron.music.db.bean.PlayList;
import com.sanron.music.service.PlayerUtil;
import com.sanron.music.ui.PagerFragment;
import com.sanron.music.ui.PlayerFrag;
import com.sanron.music.ui.mymusic.AlbumFrag;
import com.sanron.music.ui.mymusic.ArtistFrag;
import com.sanron.music.ui.mymusic.ListMusicFrag;
import com.sanron.music.ui.mymusic.LocalMusicFrag;
import com.sanron.music.ui.mymusic.NavigationHeaderFrag;
import com.sanron.music.ui.mymusic.PlayListFrag;
import com.sanron.music.ui.mymusic.RecentPlayFrag;
import com.sanron.music.ui.web.AllTagFrag;
import com.sanron.music.ui.web.BillboardFrag;
import com.sanron.music.ui.web.GedanFrag;
import com.sanron.music.ui.web.RadioFrag;
import com.sanron.music.ui.web.RecmdFrag;
import com.sanron.music.ui.web.SingerFrag;
import com.sanron.music.ui.web.SingerInfoFrag;
import com.sanron.music.ui.web.SongListFrag;
import com.sanron.music.ui.web.TagSongFrag;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashSet;
import java.util.Set;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ServiceConnection {

    private AppBarLayout mAppBar;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private FragmentManager mFm;
    private MaterialMenuDrawable mMaterialMenu;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private PlayerFrag mPlayerFrag;
    private NavigationView mNavigationView;

    private Set<PlayerReadyCallback> mPlayerReadyCallbacks;
    private Set<BackPressedHandler> mBackPressedHandlers;

    private boolean mIsShowingPlayListSongsFrag = false;//是否在显示列表歌曲fragment

    private boolean mIsConnectedPlayer;

    private int curPagerPosition = -1;

    public static final String[] PAGERS = new String[]{
            "MyMusic", "WebMusic", "DownloadManager"
    };
    public static final String[] PAGER_TITLES = new String[]{
            "我的音乐", "音乐库", "下载管理"
    };


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        mSlidingUpPanelLayout = $(R.id.sliding_panel);
        mNavigationView = $(R.id.navigation_view);
        mDrawerLayout = $(R.id.drawer_layout);
        mToolbar = $(R.id.toolbar);
        mAppBar = $(R.id.app_bar);

        mPlayerReadyCallbacks = new HashSet<>();
        mBackPressedHandlers = new HashSet<>();
        mFm = getSupportFragmentManager();

        setSupportActionBar(mToolbar);
        mMaterialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        mToolbar.setNavigationIcon(mMaterialMenu);

        setUpHeader();
        mNavigationView.setNavigationItemSelectedListener(this);

        ViewTool.setViewFitsStatusBar(mAppBar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsShowingPlayListSongsFrag) {
                    dismissPlayListSongs();
                } else {
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            }
        });

        //正在播放UI
        mPlayerFrag = (PlayerFrag) mFm.findFragmentByTag(PlayerFrag.class.getName());
        if (mPlayerFrag == null) {
            mPlayerFrag = new PlayerFrag();
            mFm.beginTransaction()
                    .add(R.id.player_frag_container, mPlayerFrag, PlayerFrag.class.getName())
                    .commit();
        }

        int pagerPosition = 0;
        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt("curPagerPosition");
            String title = savedInstanceState.getString("title");
            mToolbar.setTitle(title);

            mIsShowingPlayListSongsFrag =
                    savedInstanceState.getBoolean("isShowingPlayListSongsFrag");

            String iconStateName = savedInstanceState.getString("iconState");
            MaterialMenuDrawable.IconState iconState =
                    MaterialMenuDrawable.IconState.valueOf(iconStateName);
            if (iconState != null) {
                mMaterialMenu.setIconState(iconState);
            }
        }
        switchPager(pagerPosition);


        mSlidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0) {
                    mPlayerFrag.setSmallControllerVisibility(View.VISIBLE);
                } else {
                    mPlayerFrag.setSmallControllerVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    mSlidingUpPanelLayout.setTouchEnabled(true);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    mSlidingUpPanelLayout.setTouchEnabled(false);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
        });

        PlayerUtil.bindService(this, this);
    }

    private void setUpHeader() {
        View navigationHeader = mNavigationView.getHeaderView(0);
        ViewTool.setViewFitsStatusBar(navigationHeader);
        NavigationHeaderFrag headerFrag = (NavigationHeaderFrag) mFm.findFragmentByTag(NavigationHeaderFrag.class.getName());
        if (headerFrag == null) {
            headerFrag = new NavigationHeaderFrag();
            mFm.beginTransaction()
                    .add(headerFrag, NavigationHeaderFrag.class.getName())
                    .commit();
        }
        headerFrag.setHeader(navigationHeader);
        addPlayerReadyCallback(headerFrag);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mIsConnectedPlayer = true;
        for (PlayerReadyCallback callback : mPlayerReadyCallbacks) {
            callback.onPlayerReady();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        finish();
    }

    public void collapseSlidingPanel() {
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public void showSongList(String listId) {
        addFragmentToFront(SongListFrag.newInstance(listId));
    }

    public void showTagSong(String tag) {
        addFragmentToFront(TagSongFrag.newInstance(tag));
    }

    public void showAllTag() {
        addFragmentToFront(AllTagFrag.newInstance());
    }

    public void showSinger(String artistId) {
        addFragmentToFront(SingerInfoFrag.newInstance(artistId));
    }

    private void addFragmentToFront(Fragment fragment) {
        mFm.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right,
                        R.anim.slide_out_right,
                        R.anim.slide_in_right,
                        R.anim.slide_out_right)
                .add(R.id.fragment_container_1, fragment,
                        fragment.getClass().getName())
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }

    public void addBackPressedHandler(BackPressedHandler handler) {
        mBackPressedHandlers.add(handler);
    }

    public void removeBackPressedHandler(BackPressedHandler handler) {
        mBackPressedHandlers.remove(handler);
    }

    public void addPlayerReadyCallback(PlayerReadyCallback callback) {
        if (mPlayerReadyCallbacks.add(callback)
                && mIsConnectedPlayer) {
            callback.onPlayerReady();
        }
    }

    public void removePlayerReadyCallback(PlayerReadyCallback callback) {
        mPlayerReadyCallbacks.remove(callback);
    }


    public void switchPager(int pos) {
        if (pos == curPagerPosition) {
            return;
        }

        FragmentTransaction ft = mFm.beginTransaction();
        //隐藏当前显示fragment
        System.out.println(pos);
        System.out.println(curPagerPosition);
        for (int i = 0; i < PAGERS.length; i++) {
            if (i != pos) {
                Fragment fragment = mFm.findFragmentByTag(PAGERS[i]);
                if (fragment != null) {
                    ft.hide(fragment);
                }
            }
        }

        Fragment toFragment = mFm.findFragmentByTag(PAGERS[pos]);
        if (toFragment == null) {
            //未添加,则添加
            switch (pos) {
                case 0: {
                    String[] titles = new String[]{"我的歌单", "最近播放", "本地音乐", "艺术家", "专辑"};
                    String[] fragments = new String[]{PlayListFrag.class.getName(), RecentPlayFrag.class.getName(),
                            LocalMusicFrag.class.getName(), ArtistFrag.class.getName(), AlbumFrag.class.getName()};
                    toFragment = PagerFragment.newInstance(titles, fragments);
                }
                break;

                case 1: {
                    String[] titles = new String[]{"推荐", "排行", "歌手", "歌单", "电台"};
                    String[] fragments = new String[]{RecmdFrag.class.getName(), BillboardFrag.class.getName(),
                            SingerFrag.class.getName(), GedanFrag.class.getName(), RadioFrag.class.getName()};
                    toFragment = PagerFragment.newInstance(titles, fragments);
                }
                break;
            }
            ft.add(R.id.fragment_container_2, toFragment, PAGERS[pos]);
        } else {
            //否则显示
            ft.show(toFragment);
        }
        curPagerPosition = pos;
        mToolbar.setTitle(PAGER_TITLES[curPagerPosition]);
        ft.commit();
    }

    /**
     * 显示列表歌曲
     */
    public void showPlayListSongs(PlayList playList) {
        ListMusicFrag listMusicFrag = ListMusicFrag.newInstance(playList);
        mFm.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_top,
                        R.anim.slide_out_bottom,
                        R.anim.slide_in_top,
                        R.anim.slide_out_bottom)
                .add(R.id.fragment_container_2, listMusicFrag, ListMusicFrag.TAG)
                .addToBackStack(ListMusicFrag.class.getName())
                .commit();
        mToolbar.setTitle(playList.getTitle());
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mMaterialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
        mIsShowingPlayListSongsFrag = true;
    }

    public void dismissPlayListSongs() {
        if (mFm.popBackStackImmediate(ListMusicFrag.class.getName(),
                FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
            mToolbar.setTitle("我的音乐");
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mMaterialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
            mIsShowingPlayListSongsFrag = false;
        }
    }

    @Override
    public void onBackPressed() {
        for (BackPressedHandler handler : mBackPressedHandlers) {
            if (handler.onBackPressed()) {
                return;
            }
        }
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            //收缩播放器面板
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (mIsShowingPlayListSongsFrag) {
            dismissPlayListSongs();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayerReadyCallbacks.clear();
        mBackPressedHandlers.clear();
        PlayerUtil.unbindService(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curPagerPosition", curPagerPosition);
        outState.putString("title", mToolbar.getTitle().toString());
        outState.putString("iconState", mMaterialMenu.getIconState().name());
        outState.putBoolean("mIsShowingPlayListSongsFrag", mIsShowingPlayListSongsFrag);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_my_music: {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                //延迟加载,使关闭菜单动画流畅
                mDrawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switchPager(0);
                    }
                }, 400);
            }
            break;

            case R.id.menu_web_music: {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                mDrawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switchPager(1);
                    }
                }, 400);
            }
            break;

            case R.id.menu_download_manager: {

            }
            break;

            case R.id.menu_setting: {

            }
            break;

            case R.id.menu_close_app: {
                ((AppContext) getApplicationContext()).closeApp();
            }
            break;
        }
        invalidateOptionsMenu();
        return true;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        switch (currentFragmentTag) {
//            case TAG_WEBMUSIC: {
//                getMenuInflater().inflate(R.menu.option_menu_webmusicfrag, menu);
//            }
//            break;
//            case TAG_MYMUSIC: {
//                getMenuInflater().inflate(R.menu.option_menu_mymusicfrag, menu);
//            }
//            break;
//
//        }
//        return true;
//    }


    /**
     * 绑定上服务监听
     */
    public interface PlayerReadyCallback {
        void onPlayerReady();
    }

    /**
     * 返回键监听
     */
    public interface BackPressedHandler {
        boolean onBackPressed();
    }
}
