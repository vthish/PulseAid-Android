package com.example.pulseaid.ui.donor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.donor.DonorHomeViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class DonorHomeFragment extends Fragment {

    private TextView tvWelcome, tvLivesSaved, tvEligibilityStatus, tvEligibilityPercent;
    private ProgressBar pbEligibility;
    private MaterialCardView cardFindBank, cardEmergencyRequests, cardUpcomingBooking;
    private ImageView ivProfile;
    private DonorHomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donor_home, container, false);

        initViews(view);

        viewModel = new ViewModelProvider(this).get(DonorHomeViewModel.class);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupObservers();
        setupClickListeners();

        viewModel.loadDashboardData(userId);

        return view;
    }

    private void initViews(View v) {
        tvWelcome = v.findViewById(R.id.tv_home_welcome);
        tvLivesSaved = v.findViewById(R.id.tv_lives_saved_count);
        tvEligibilityStatus = v.findViewById(R.id.tv_eligibility_status);
        tvEligibilityPercent = v.findViewById(R.id.tv_eligibility_percent);
        pbEligibility = v.findViewById(R.id.pb_eligibility);
        ivProfile = v.findViewById(R.id.iv_home_profile);

        cardFindBank = v.findViewById(R.id.card_find_bank);
        cardEmergencyRequests = v.findViewById(R.id.card_emergency_requests);
        cardUpcomingBooking = v.findViewById(R.id.card_upcoming_booking);
    }

    private void setupObservers() {
        viewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) tvWelcome.setText("Hello, " + name + "!");
        });

        viewModel.getLivesSaved().observe(getViewLifecycleOwner(), count -> {
            tvLivesSaved.setText(String.format("%02d Lives Saved", count));
        });

        viewModel.getEligibilityStatus().observe(getViewLifecycleOwner(), status -> {
            tvEligibilityStatus.setText(status);
        });

        viewModel.getEligibilityProgress().observe(getViewLifecycleOwner(), progress -> {
            pbEligibility.setProgress(progress);
            tvEligibilityPercent.setText(progress + "%");
        });
    }

    private void setupClickListeners() {
        cardFindBank.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DonorBookingActivity.class);
            startActivity(intent);
        });

        cardEmergencyRequests.setOnClickListener(v -> {
            loadFragment(new DonorUrgentAlertsFragment());
        });

        cardUpcomingBooking.setOnClickListener(v -> {
            loadFragment(new DonorUpcomingAppointmentFragment());
        });

    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.donor_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}