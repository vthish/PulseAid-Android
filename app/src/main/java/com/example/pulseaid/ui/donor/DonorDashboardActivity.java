package com.example.pulseaid.ui.donor;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.donor.DonorDashboardViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DonorDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private DonorDashboardViewModel dashboardViewModel;
    private boolean isProfileComplete = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_dashboard);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.parseColor("#FFF5F2"));
        }

        bottomNav = findViewById(R.id.donor_bottom_nav);
        dashboardViewModel = new ViewModelProvider(this).get(DonorDashboardViewModel.class);

        setupNavigation();

        dashboardViewModel.getProfileStatus().observe(this, isComplete -> {
            isProfileComplete = isComplete != null && isComplete;

            if (!isProfileComplete) {
                bottomNav.setVisibility(View.GONE);
                loadRootFragment(new DonorProfileFragment());
            } else {
                bottomNav.setVisibility(View.VISIBLE);

                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.donor_fragment_container);
                if (currentFragment == null || currentFragment instanceof DonorProfileFragment || savedInstanceState == null) {
                    loadRootFragment(new DonorHomeFragment());
                    bottomNav.setSelectedItemId(R.id.nav_home);
                }
            }
        });
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.donor_fragment_container);
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                if (currentFragment instanceof DonorHomeFragment) {
                    return true;
                }
                clearFragmentBackStack();
                loadRootFragment(new DonorHomeFragment());
                return true;
            } else if (id == R.id.nav_history) {
                if (currentFragment instanceof DonarHistoryFragment) {
                    return true;
                }
                clearFragmentBackStack();
                loadRootFragment(new DonarHistoryFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                if (currentFragment instanceof DonorAccountFragment) {
                    return true;
                }
                clearFragmentBackStack();
                loadRootFragment(new DonorAccountFragment());
                return true;
            }

            return false;
        });

        bottomNav.setOnItemReselectedListener(item -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.donor_fragment_container);
            int id = item.getItemId();

            if (id == R.id.nav_home && !(currentFragment instanceof DonorHomeFragment)) {
                clearFragmentBackStack();
                loadRootFragment(new DonorHomeFragment());
            } else if (id == R.id.nav_history && !(currentFragment instanceof DonarHistoryFragment)) {
                clearFragmentBackStack();
                loadRootFragment(new DonarHistoryFragment());
            } else if (id == R.id.nav_profile && !(currentFragment instanceof DonorAccountFragment)) {
                clearFragmentBackStack();
                loadRootFragment(new DonorAccountFragment());
            }
        });
    }

    private void clearFragmentBackStack() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void loadRootFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.donor_fragment_container, fragment)
                .commit();
    }
}