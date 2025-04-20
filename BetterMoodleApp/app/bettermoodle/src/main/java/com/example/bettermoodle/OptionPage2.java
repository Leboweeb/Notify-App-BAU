package com.example.bettermoodle;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;


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
        // handle redirect intent from widget
        Intent intent = getIntent();
        String value = intent.getStringExtra("destination"); // Retrieve the extra data
        if (value != null) {
            navigateToFragment(scheduleFragment);
            // once fragment actually renders, select schedule icon from bottom nav bar menu
            getWindow().getDecorView().post(
                    () -> {
                        MenuItem scheduleItem = bottomNavigationView.getMenu().getItem(2);
                        bottomNavigationView.setSelectedItemId(scheduleItem.getItemId());
                    }
            );
        } else {
            // no redirect, go to home fragment as default
            getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
        }
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
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