package io.scalac.degree.android.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.flurry.android.FlurryAgent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.List;

import io.scalac.degree.android.fragment.BaseFragment;
import io.scalac.degree.android.fragment.SpeakersFragment_;
import io.scalac.degree.android.fragment.TabsFragment.TabType;
import io.scalac.degree.android.fragment.TabsFragment_;
import io.scalac.degree.android.fragment.TalkFragment;
import io.scalac.degree.android.fragment.TalkFragment_;
import io.scalac.degree.android.fragment.TalksFragment.TalksType;
import io.scalac.degree.android.fragment.TalksFragment_;
import io.scalac.degree.connection.model.SlotApiModel;
import io.scalac.degree.data.manager.AbstractDataManager;
import io.scalac.degree.data.manager.NotificationsManager;
import io.scalac.degree.data.manager.SlotsDataManager;
import io.scalac.degree.utils.Utils;
import io.scalac.degree33.R;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener,
        AbstractDataManager.IDataManagerListener<SlotApiModel> {

    private static final String TAG_CONTENT_FRAGMENT = "content_fragment";
    @Bean
    SlotsDataManager slotsDataManager;
    @StringRes(R.string.devoxx_conference)
    String conferenceCode;
    @ViewById(R.id.toolbarWithSpinner)
    Toolbar toolbar;
    @ViewById(R.id.navigationView)
    NavigationView navigationView;
    @ViewById(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @ViewById(R.id.homeProgressBar)
    ContentLoadingProgressBar progressBar;

    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Spinner toolbarSpinner;
    private int lastClickedMenuItemId;
    private int currentClickedMenuItemId;
    private boolean isColdStart;
    private FragmentManager.OnBackStackChangedListener
            onBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            syncActionBarArrowState();
            final Fragment currentFragment = getCurrentFragment();
            setupToolbarSpinnerVisibility(currentFragment);
            setupToolbarTitle(currentFragment);
        }
    };
    private String incomingSlotId;

    @AfterViews
    protected void afterViews() {
        getSupportFragmentManager().
                addOnBackStackChangedListener(onBackStackChangedListener);

        setupToolbar();
        setupDrawer();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(NotificationsManager.EXTRA_TALK_ID)) {
            incomingSlotId = intent.getStringExtra(
                    NotificationsManager.EXTRA_TALK_ID);
        }
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(onBackStackChangedListener);
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        isColdStart = savedInstanceState == null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, Utils.FLURRY_API_KEY);

        actionBarDrawerToggle.syncState();
        slotsDataManager.fetchTalks(conferenceCode, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        final int menuItemId = menuItem.getItemId();

        switch (menuItemId) {
            case R.id.drawer_menu_feedback:
                navigationView.setCheckedItem(lastClickedMenuItemId);
                handleFeedbackClick();
                break;
            case R.id.drawer_menu_register:
                navigationView.setCheckedItem(lastClickedMenuItemId);
                handleRegisterClick();
                break;
            default:
                lastClickedMenuItemId = menuItemId;
                drawerLayout.closeDrawers();
                break;
        }

        return true;
    }

    private void handleRegisterClick() {
        RegisterUserActivity_.intent(this).start();
    }

    @Override
    public void onBackPressed() {
        if (!closeDrawerIfNeeded()) {
            final boolean shouldBackToMainFragment =
                    lastClickedMenuItemId != R.id.drawer_menu_schedule_by_time &&
                            getSupportFragmentManager().getBackStackEntryCount() == 0;

            if (shouldBackToMainFragment) {
                selectItem(R.id.drawer_menu_schedule_by_time);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        actionBarDrawerToggle.onDrawerSlide(drawerView, slideOffset);
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        actionBarDrawerToggle.onDrawerOpened(drawerView);
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        actionBarDrawerToggle.onDrawerClosed(drawerView);
        selectItem(lastClickedMenuItemId);
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        actionBarDrawerToggle.onDrawerStateChanged(newState);
    }

    public ActionBar getSupportActionBarHelper() {
        return getSupportActionBar();
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void buttonScalacOnClick(View v) {
        FlurryAgent.logEvent("Scalac_clicked");
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("http://scalac.io/"));
        startActivity(i);
    }

    public void replaceFragment(Fragment fragment) {
        replaceFragment(fragment, false, FragmentTransaction.TRANSIT_NONE);
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        replaceFragment(fragment, addToBackStack, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack, int fragmentTransition) {
        setupToolbarSpinnerVisibility(fragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setTransition(fragmentTransition);
        ft.replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT);
        ft.attach(fragment);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    public void invalidateToolbarTitle() {
        setupToolbarTitle(getCurrentFragment());
    }

    public Spinner getToolbarSpinner() {
        return toolbarSpinner;
    }

    public void hideToolbarSpinner() {
        toolbarSpinner.setVisibility(View.GONE);
    }

    public void showToolbarSpinner() {
        toolbarSpinner.setVisibility(View.VISIBLE);
    }

    @SuppressLint("RtlHardcoded")
    private boolean closeDrawerIfNeeded() {
        final boolean shouldBeClosed = drawerLayout.isDrawerVisible(Gravity.LEFT);
        if (shouldBeClosed) {
            drawerLayout.closeDrawers();
        }
        return shouldBeClosed;
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().
                findFragmentByTag(TAG_CONTENT_FRAGMENT);
    }

    private void setupToolbarTitle(Fragment fragment) {
        if (fragment instanceof BaseFragment) {
            final String titleString = ((BaseFragment) fragment).getTitleAsString();
            if (TextUtils.isEmpty(titleString)) {
                final int titleResId = ((BaseFragment) fragment).getTitle();
                if (titleResId != BaseFragment.UNKNOWN_TITLE_RES) {
                    getSupportActionBarHelper().setTitle(titleResId);
                } else {
                    getSupportActionBarHelper().setTitle("");
                }
            } else {
                getSupportActionBarHelper().setTitle(titleString);
            }
        }
    }

    private void syncActionBarArrowState() {
        final int backStackEntryCount =
                getSupportFragmentManager().getBackStackEntryCount();
        final boolean shouldDrawerIndBeEnabled = backStackEntryCount == 0;
        actionBarDrawerToggle.setDrawerIndicatorEnabled(shouldDrawerIndBeEnabled);
        drawerLayout.setDrawerLockMode(shouldDrawerIndBeEnabled
                ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        toolbarSpinner = new Spinner(actionBar.getThemedContext());
        toolbar.addView(toolbarSpinner);
    }

    private void setupToolbarSpinnerVisibility(Fragment fragment) {
        if (fragment instanceof BaseFragment) {
            if (((BaseFragment) fragment).needsToolbarSpinner()) {
                showToolbarSpinner();
            } else {
                hideToolbarSpinner();
            }
        }
    }

    private void selectItem(final int menuItemId) {
        lastClickedMenuItemId = menuItemId;
        navigationView.setCheckedItem(lastClickedMenuItemId);

        if (lastClickedMenuItemId != currentClickedMenuItemId) {
            currentClickedMenuItemId = lastClickedMenuItemId;
        } else {
            return;
        }

        removeFragments();

        switch (menuItemId) {
            case R.id.drawer_menu_schedule_by_time:
                replaceFragment(TabsFragment_.builder().currentDatePosition(
                        slotsDataManager.getInitialDatePosition())
                        .tabTypeEnumName(TabType.TIME.name()).build(), false);
                break;
            case R.id.drawer_menu_schedule_by_rooms:
                replaceFragment(TabsFragment_.builder().currentDatePosition(
                        slotsDataManager.getInitialDatePosition())
                        .tabTypeEnumName(TabType.ROOM.name()).build(), false);
                break;
            case R.id.drawer_menu_talks:
                replaceFragment(TalksFragment_.builder().build(), false);
                break;
            case R.id.drawer_menu_speakers:
                replaceFragment(SpeakersFragment_.builder().build(), false);
                break;
            case R.id.drawer_menu_my_schedule:
                replaceFragment(TalksFragment_.builder().
                        talksTypeEnumName(TalksType.NOTIFICATION.name()).build(), false);
                break;
        }

        syncActionBarArrowState();
    }

    private void removeFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            for (Fragment fragment : fragments) {
                if (fragment != null)
                    ft.detach(fragment).remove(fragment);
            }
            ft.commit();
            fragmentManager.executePendingTransactions();
        }
    }

    private void handleFeedbackClick() {
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.parse(String.format(getString(R.string.feedback_link),
                        getString(R.string.app_name),
                        Utils.getVersionName(this),
                        Utils.getVersionCode(this),
                        Build.VERSION.RELEASE,
                        Build.VERSION.SDK_INT,
                        Build.MANUFACTURER,
                        Build.MODEL)));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), R.string.toast_feedback_activity_not_found, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void setupDrawer() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View view) {
                syncActionBarArrowState();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
            }
        };
        actionBarDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!actionBarDrawerToggle.isDrawerIndicatorEnabled()) {
                    onBackPressed();
                }
            }
        });
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                actionBarDrawerToggle.syncState();
            }
        });
        drawerLayout.setDrawerListener(this);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onDataStartFetching() {
        progressBar.show();
    }

    @Override
    public void onDataAvailable(List<SlotApiModel> items) {
        progressBar.hide();

        if (TextUtils.isEmpty(incomingSlotId)) {
            final Intent intent = getIntent();
            if (intent != null && intent.hasExtra(NotificationsManager.EXTRA_TALK_ID)) {
                incomingSlotId = intent.getStringExtra(
                        NotificationsManager.EXTRA_TALK_ID);
            }
        }

        if (isColdStart && !TextUtils.isEmpty(incomingSlotId)) {
            final Optional<SlotApiModel> optModel = Stream.of(items)
                    .filter(new SlotApiModel.SameModelPredicate(incomingSlotId))
                    .findFirst();

            if (optModel.isPresent()) {
                final FragmentManager fm = getSupportFragmentManager();
                final Fragment talkFragment = fm.findFragmentByTag(TAG_CONTENT_FRAGMENT);
                final SlotApiModel slotApiModel = optModel.get();
                if (talkFragment instanceof TalkFragment) {
                    ((TalkFragment) talkFragment).setupViews(slotApiModel);
                } else {
                    removeFragments();
                    replaceFragment(TalkFragment_.builder().slotModel(slotApiModel).build());
                }

                syncActionBarArrowState();
            } else {
                selectItem(R.id.drawer_menu_schedule_by_time);
            }
        } else {
            selectItem(R.id.drawer_menu_schedule_by_time);
        }
    }

    @Override
    public void onDataAvailable(SlotApiModel item) {
        // Nothing here.
    }

    @Override
    public void onDataError() {
        progressBar.hide();
    }
}
