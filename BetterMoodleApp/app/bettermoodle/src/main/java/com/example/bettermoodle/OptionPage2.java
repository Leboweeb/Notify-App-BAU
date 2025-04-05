package com.example.bettermoodle;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class OptionPage2 extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    HomeFragment homeFragment = new HomeFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    ScheduleFragment scheduleFragment = new ScheduleFragment();
    NotificationsFragment notificationFragment = new NotificationsFragment();
    private int mSelectedItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.optionpage2);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_fragment);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
                assert currentFragment != null;
                // before reconstructing view, clear cache key for the current fragment
                UtilsKt.clearFragmentCache(currentFragment, getApplicationContext());
                // then refresh fragment frame layout
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().detach(currentFragment).commit();
                fm.beginTransaction().attach(currentFragment).commit();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // the home fragment is the default fragment.
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    navigateToFragment(homeFragment);
                    return true;
                } else if (itemId == R.id.notification) {
                    navigateToFragment(notificationFragment);
                    return true;
                } else if (itemId == R.id.schedule) {
                    navigateToFragment(scheduleFragment);
                    return true;
                } else if (itemId == R.id.settings) {
                    navigateToFragment(settingsFragment);
                    return true;
                }
                mSelectedItem = itemId;
                return false;
            }
        });

    }

    private void navigateToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    @Override
    public void onBackPressed() {
        MenuItem homeItem = bottomNavigationView.getMenu().getItem(0);
        if (mSelectedItem != homeItem.getItemId()) {
            navigateToFragment(homeFragment);
            // Select home item
            bottomNavigationView.setSelectedItemId(homeItem.getItemId());
        } else {
            super.onBackPressed();
        }
    }


}