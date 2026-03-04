package com.example.pulseaid.ui.donor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.donor.DonorDashboardViewModel;

public class DonorHomeFragment extends Fragment {

    private TextView donorWelcomeText, donorLivesSavedText, donorStatusText;
    private DonorDashboardViewModel dashboardViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donor_home, container, false);


        donorWelcomeText = view.findViewById(R.id.user_welcome_text);
        donorLivesSavedText = view.findViewById(R.id.lives_saved_count);
        donorStatusText = view.findViewById(R.id.eligibility_status_text);


        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DonorDashboardViewModel.class);
        setupDataObservers();

        return view;
    }

    private void setupDataObservers() {

        dashboardViewModel.getDonorName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) donorWelcomeText.setText("Hello, " + name + "!");
        });

        dashboardViewModel.getLivesSaved().observe(getViewLifecycleOwner(), lives -> {
            donorLivesSavedText.setText(lives + " Lives Saved");
        });

        dashboardViewModel.getEligibilityStatus().observe(getViewLifecycleOwner(), status -> {
            donorStatusText.setText(status);
        });
    }
}