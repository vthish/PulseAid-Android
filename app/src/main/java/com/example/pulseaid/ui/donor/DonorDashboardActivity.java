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

        if (savedInstanceState == null) {
            loadFragment(new DonorHomeFragment());
        }

        dashboardViewModel.getProfileStatus().observe(this, isComplete -> {
            if (isComplete != null && !isComplete) {
                loadFragment(new DonorProfileFragment());
                bottomNav.setSelectedItemId(R.id.nav_profile);
            }
        });
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new DonorHomeFragment();
            } else if (id == R.id.nav_history) {
                selectedFragment = new DonarHistoryFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new DonorAccountFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.donor_fragment_container, fragment)
                .commit();
    }
}