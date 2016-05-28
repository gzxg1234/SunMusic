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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.sanron.music.AppContext;
import com.sanron.music.R;
import com.sanron.music.common.ViewTool;
import com.sanron.music.db.bean.PlayList;
import com.sanron.music.fragments.NavigationHeaderFrag;
import com.sanron.music.fragments.NowPlayingFragment;
import com.sanron.music.fragments.PagerFragment;
import com.sanron.music.fragments.pagermymusic.AlbumFragment;
import com.sanron.music.fragments.pagermymusic.ArtistFragment;
import com.sanron.music.fragments.pagermymusic.ListMusicFragment;
import com.sanron.music.fragments.pagermymusic.LocalMusicFragment;
import com.sanron.music.fragments.pagermymusic.PlayListFragment;
import com.sanron.music.fragments.pagermymusic.RecentPlayFragment;
import com.sanron.music.fragments.pagerwebmusic.BillboardFragment;
import com.sanron.music.fragments.pagerwebmusic.RecommendFragment;
import com.sanron.music.fragments.pagerwebmusic.SingerFragment;
import com.sanron.music.fragments.pagerwebmusic.SongListFragment;
import com.sanron.music.fragments.web.AlbumSongsFragment;
import com.sanron.music.fragments.web.AllTagFragment;
import com.sanron.music.fragments.web.BillboardSongsFragment;
import com.sanron.music.fragments.web.OfficialSongListInfoFragment;
import com.sanron.music.fragments.web.SingerInfoFragment;
import com.sanron.music.fragments.web.SingerListFragment;
import com.sanron.music.fragments.web.SongListInfoFragment;
import com.sanron.music.fragments.web.TagInfoFragment;
import com.sanron.music.service.PlayerUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ServiceConnection {

    @BindView(R.id.app_bar)
    AppBarLayout mAppBar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.sliding_panel)
    SlidingUpPanelLayout mSlidingUpPanelLayout;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    private FragmentManager mFragmentManager;
    private MaterialMenuDrawable mMaterialMenu;
    private NowPlayingFragment mNowPlayingFragment;

    private List<PlayerReadyCallback> mPlayerReadyCallbacks;
    private List<BackPressedHandler> mBackPressedHandlers;

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
        ButterKnife.bind(this);
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        ViewTool.setViewFitsStatusBar(mAppBar);

        mPlayerReadyCallbacks = new ArrayList<>();
        mBackPressedHandlers = new ArrayList<>();
        mFragmentManager = getSupportFragmentManager();

        setSupportActionBar(mToolbar);
        mMaterialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        mToolbar.setNavigationIcon(mMaterialMenu);

        setUpHeader();
        mNavigationView.setNavigationItemSelectedListener(this);

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


        mNowPlayingFragment = (NowPlayingFragment) mFragmentManager.findFragmentByTag(NowPlayingFragment.class.getName());
        if (mNowPlayingFragment == null) {
            mNowPlayingFragment = new NowPlayingFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.player_frag_container, mNowPlayingFragment, NowPlayingFragment.class.getName())
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
                    mNowPlayingFragment.setSmallControllerVisibility(View.VISIBLE);
                } else {
                    mNowPlayingFragment.setSmallControllerVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    mSlidingUpPanelLayout.setSlideViewClickable(true);
                } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    mSlidingUpPanelLayout.setSlideViewClickable(false);
                }
            }
        });

        PlayerUtil.bindService(this, this);
    }

    private void setUpHeader() {
        View navigationHeader = mNavigationView.getHeaderView(0);
        ViewTool.setViewFitsStatusBar(navigationHeader);
        NavigationHeaderFrag headerFrag = (NavigationHeaderFrag) mFragmentManager.findFragmentByTag(NavigationHeaderFrag.class.getName());
        if (headerFrag == null) {
            headerFrag = new NavigationHeaderFrag();
            mFragmentManager.beginTransaction()
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
        addFragmentToFront(SongListInfoFragment.newInstance(listId));
    }

    public void showOfficialSongList(String code) {
        addFragmentToFront(OfficialSongListInfoFragment.newInstance(code));
    }

    public void showAlbumSongs(String albumId) {
        addFragmentToFront(AlbumSongsFragment.newInstance(albumId));
    }

    public void showBillboardInfo(int type) {
        addFragmentToFront(BillboardSongsFragment.newInstance(type));
    }

    public void showSingerList(String title, int area, int sex) {
        addFragmentToFront(SingerListFragment.newInstance(title, area, sex));
    }

    public void showTagSong(String tag) {
        addFragmentToFront(TagInfoFragment.newInstance(tag));
    }

    public void showAllTag() {
        addFragmentToFront(AllTagFragment.newInstance());
    }

    public void showSingerInfo(String artistId) {
        addFragmentToFront(SingerInfoFragment.newInstance(artistId));
    }

    //切换到歌单页
    public void setPagerItemToSongList() {
        if (curPagerPosition == 1) {
            PagerFragment pagerFragment = (PagerFragment) mFragmentManager.findFragmentByTag(PAGERS[1]);
            if (pagerFragment != null) {
                pagerFragment.setCurrentItem(3);
            }
        }
    }

    private void addFragmentToFront(Fragment fragment) {
        mFragmentManager.beginTransaction()
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

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        for (int i = 0; i < PAGERS.length; i++) {
            if (i != pos) {
                Fragment fragment = mFragmentManager.findFragmentByTag(PAGERS[i]);
                if (fragment != null) {
                    ft.hide(fragment);
                }
            }
        }

        Fragment toFragment = mFragmentManager.findFragmentByTag(PAGERS[pos]);
        if (toFragment == null) {
            //未添加,则添加
            switch (pos) {
                case 0: {
                    String[] titles = new String[]{"我的歌单", "最近播放", "本地音乐", "艺术家", "专辑"};
                    String[] fragments = new String[]{PlayListFragment.class.getName(),
                            RecentPlayFragment.class.getName(),
                            LocalMusicFragment.class.getName(),
                            ArtistFragment.class.getName(),
                            AlbumFragment.class.getName()};
                    toFragment = PagerFragment.newInstance(titles, fragments);
                }
                break;

                case 1: {
                    String[] titles = new String[]{"推荐", "歌手", "排行", "歌单"};
                    String[] fragments = new String[]{RecommendFragment.class.getName(),
                            SingerFragment.class.getName(),
                            BillboardFragment.class.getName(),
                            SongListFragment.class.getName()};
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
        supportInvalidateOptionsMenu();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 显示列表歌曲
     */
    public void showPlayListSongs(PlayList playList) {
        ListMusicFragment listMusicFragment = ListMusicFragment.newInstance(playList);
        mFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_top,
                        R.anim.slide_out_bottom,
                        R.anim.slide_in_top,
                        R.anim.slide_out_bottom)
                .add(R.id.fragment_container_2, listMusicFragment, ListMusicFragment.TAG)
                .addToBackStack(ListMusicFragment.class.getName())
                .commit();
        mToolbar.setTitle(playList.getTitle());
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mMaterialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
        mIsShowingPlayListSongsFrag = true;
    }

    public void dismissPlayListSongs() {
        if (mFragmentManager.popBackStackImmediate(ListMusicFragment.class.getName(),
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
        outState.putBoolean("isShowingPlayListSongsFrag", mIsShowingPlayListSongsFrag);
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


    /**
     * 绑定服务监听
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
