package com.mb.android.ui.mobile.homescreen;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mb.android.MainApplication;
import com.mb.android.activities.BaseMbMobileActivity;
import com.mb.android.activities.mobile.NewsActivity;
import com.mb.android.playbackmediator.widgets.MiniController;
import com.mb.android.R;
import com.mb.android.ui.main.SettingsActivity;
import com.mb.android.fragments.NavigationMenuFragment;
import com.mb.android.logging.AppLogger;

/**
 * Created by Mark on 12/12/13.
 *
 * The HomeScreen activity represent the main activity that the user will see when navigating the UI
 */
public class HomescreenActivity extends BaseMbMobileActivity {

    private ActionBarDrawerToggle mDrawerToggle;
    private ViewPager mViewPager;
    private boolean isFresh = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_homescreen);
        mViewPager = (ViewPager) findViewById(R.id.vpEhsViewPager);
        AppLogger.getLogger().Info("HomeScreen Activity: onCreate");

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawer.setFocusableInTouchMode(false);

        NavigationMenuFragment fragment = (NavigationMenuFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer);
        if (fragment != null && fragment.isInLayout()) {
            fragment.setDrawerLayout(drawer);
        }

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawer,
                R.string.abc_action_bar_home_description,
                R.string.abc_action_bar_up_description) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
//                getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                getActionBar().setTitle(mDrawerTitle);
            }

        };

        drawer.setDrawerListener(mDrawerToggle);

        mMini = (MiniController) findViewById(R.id.miniController1);
        mCastManager.addMiniController(mMini);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(getResources().getString(R.string.settings_action_bar_button)).setIcon(R.drawable.settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(getResources().getString(R.string.news_action_bar_button)).setIcon(R.drawable.ic_action_chat).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getTitle().equals(getResources().getString(R.string.settings_action_bar_button))) {
            Intent intent = new Intent(HomescreenActivity.this, SettingsActivity.class);

            startActivity(intent);
        } else if (item.getTitle().equals(getResources().getString(R.string.news_action_bar_button))) {
            Intent intent = new Intent(HomescreenActivity.this, NewsActivity.class);

            startActivity(intent);
        } else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFresh) {
            buildUI();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        AppLogger.getLogger().Info("HomeScreen Activity: onPause");
//        mMini.removeOnMiniControllerChangedListener(mCastManager);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLogger.getLogger().Info("HomeScreen Activity: onDestroy");
        MainApplication.getAudioService().Terminate();
    }

    @Override
    protected void onConnectionRestored() {
        buildUI();
    }

    private void buildUI() {
        if (isFresh) {
            HomeScreenPagerAdapter mHomeScreenPagerAdapter = new HomeScreenPagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mHomeScreenPagerAdapter);

            try {
                String tabIndex = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_home_default_tab", "1");
                mViewPager.setCurrentItem(Integer.valueOf(tabIndex), true);
            } catch (Exception e) {
                AppLogger.getLogger().ErrorException("Error setting view pager index", e);
            }

            mViewPager.requestFocus();
            isFresh = false;
        }
    }

    public class HomeScreenPagerAdapter extends FragmentStatePagerAdapter {

        SparseArray<Fragment> fragments;

        public HomeScreenPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new SparseArray<>(getCount());
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            fragments.remove(position);
        }

        @Override
        public Fragment getItem(int i) {

            Fragment fragment = fragments.get(i);
            if (fragment != null) {
                return fragment;
            }

            if (i == 0) {
                fragments.put(i, new FavoritesFragment());

            } else if (i == 1) {
                fragments.put(i, new NewItemsFragment());
            } else if (i == 2) {
                fragments.put(i, new UpNextFragment());
            } else if (i == 3) {
                fragments.put(i, new ResumableFragment());
            } else {
                fragments.put(i, new CollectionsFragment());
            }
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            if (position == 0)
                return getResources().getString(R.string.favorites_header);
            else if (position == 1)
                return getResources().getString(R.string.new_header);
            else if (position == 2)
                return getResources().getString(R.string.next_up_header);
            else if (position == 3)
                return getResources().getString(R.string.resume_header);

            return getResources().getString(R.string.library_header);
        }
    }
}