package com.pulseaid.ui.admin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdminUserPagerAdapter extends FragmentStateAdapter {

    public AdminUserPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AdminDonorsFragment();
            case 1:
                return new AdminHospitalsFragment();
            case 2:
                return new AdminBloodBanksFragment();
            default:
                return new AdminDonorsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}