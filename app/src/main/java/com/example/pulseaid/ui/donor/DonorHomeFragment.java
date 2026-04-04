package com.example.pulseaid.ui.donor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.donor.DonorHomeViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.Set;

public class DonorHomeFragment extends Fragment {

    private TextView tvWelcome, tvLivesSaved, tvEligibilityStatus, tvEligibilityDays, tvUpcomingLabel, tvBookingDetails, tvBookingTime;
    private ProgressBar pbEligibility;
    private MaterialCardView cardAppointments, cardEmergencyRequests, cardUpcomingBooking;
    private ImageView ivProfile;
    private View viewUrgentDot;
    private DonorHomeViewModel viewModel;

    private boolean hasPendingAppointment = false;
    private boolean isEligible = true;
    private int remainingDays = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donor_home, container, false);

        initViews(view);
        viewModel = new ViewModelProvider(this).get(DonorHomeViewModel.class);

        setupObservers();
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel.loadDashboardData(userId, getSeenAlertIds());
    }

    private void initViews(View v) {
        tvWelcome = v.findViewById(R.id.tv_home_welcome);
        tvLivesSaved = v.findViewById(R.id.tv_lives_saved_count);
        tvEligibilityStatus = v.findViewById(R.id.tv_eligibility_status);
        tvEligibilityDays = v.findViewById(R.id.tv_eligibility_days);
        pbEligibility = v.findViewById(R.id.pb_eligibility);
        ivProfile = v.findViewById(R.id.iv_home_profile);
        viewUrgentDot = v.findViewById(R.id.view_urgent_dot);

        tvUpcomingLabel = v.findViewById(R.id.tv_upcoming_label);
        tvBookingDetails = v.findViewById(R.id.tv_booking_details);
        tvBookingTime = v.findViewById(R.id.tv_booking_time);

        cardAppointments = v.findViewById(R.id.card_appointments);
        cardEmergencyRequests = v.findViewById(R.id.card_emergency_requests);
        cardUpcomingBooking = v.findViewById(R.id.card_upcoming_booking);
    }

    private void setupObservers() {
        viewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            if (name != null && !name.trim().isEmpty()) {
                tvWelcome.setText("Hello, " + name + "!");
            }
        });

        viewModel.getLivesSaved().observe(getViewLifecycleOwner(), count -> {
            int safeCount = count != null ? count : 0;
            tvLivesSaved.setText(String.format("%02d Lives Saved", safeCount));
        });

        viewModel.getIsEligible().observe(getViewLifecycleOwner(), eligible -> {
            isEligible = eligible != null && eligible;
        });

        viewModel.getRemainingDays().observe(getViewLifecycleOwner(), days -> {
            remainingDays = days != null ? days : 0;

            if (remainingDays <= 0) {
                tvEligibilityStatus.setText("You are eligible to donate!");
                tvEligibilityDays.setText("Ready");
                pbEligibility.setProgress(90);
            } else {
                tvEligibilityStatus.setText("Eligible in " + remainingDays + " Days");
                tvEligibilityDays.setText(remainingDays + " Days left");
                pbEligibility.setProgress(Math.max(90 - remainingDays, 0));
            }
        });

        viewModel.getHasPendingAppointment().observe(getViewLifecycleOwner(), pending -> {
            hasPendingAppointment = pending != null && pending;

            if (hasPendingAppointment) {
                tvUpcomingLabel.setText("PENDING");
                tvUpcomingLabel.setTextColor(getResources().getColor(R.color.pulse_red));
            } else {
                tvUpcomingLabel.setText("Upcoming Appointment");
                tvUpcomingLabel.setTextColor(getResources().getColor(R.color.pulse_red));
                tvBookingDetails.setText("No Active Appointment");
                tvBookingTime.setText("Book your slot today");
            }
        });

        viewModel.getUpcomingBookingDetails().observe(getViewLifecycleOwner(), details -> {
            if (details != null && hasPendingAppointment) {
                tvBookingDetails.setText(details.getCenterName());
                tvBookingTime.setText(details.getDateTime());
            } else if (!hasPendingAppointment) {
                tvBookingDetails.setText("No Active Appointment");
                tvBookingTime.setText("Book your slot today");
            }
        });

        viewModel.getHasUnreadUrgentAlerts().observe(getViewLifecycleOwner(), hasUnread -> {
            if (viewUrgentDot != null) {
                viewUrgentDot.setVisibility(Boolean.TRUE.equals(hasUnread) ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupClickListeners() {
        ivProfile.setOnClickListener(v -> loadFragment(new DonorAccountFragment()));

        cardAppointments.setOnClickListener(v -> handleAppointmentCardClick());

        cardUpcomingBooking.setOnClickListener(v -> {
            if (hasPendingAppointment) {
                loadFragment(new DonorUpcomingAppointmentFragment());
            } else {
                showToast("You don't have any upcoming appointment at the moment.");
            }
        });

        cardEmergencyRequests.setOnClickListener(v -> loadFragment(new DonorUrgentAlertsFragment()));
    }

    private void handleAppointmentCardClick() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("appointments")
                .whereEqualTo("donorUid", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean hasPending = false;
                    boolean hasCompleted = false;
                    long latestCompletedTimestamp = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String status = doc.getString("status");
                        Long timestamp = doc.getLong("timestamp");
                        long safeTimestamp = timestamp != null ? timestamp : 0;

                        if ("PENDING".equalsIgnoreCase(status)) {
                            hasPending = true;
                        } else if ("COMPLETED".equalsIgnoreCase(status)) {
                            if (safeTimestamp > latestCompletedTimestamp) {
                                latestCompletedTimestamp = safeTimestamp;
                            }
                            hasCompleted = true;
                        }
                    }

                    if (hasPending) {
                        showToast("You already have a upcoming appointment. Cancel it first if you need to book a new one.");
                        return;
                    }

                    if (hasCompleted && remainingDays > 0) {
                        showToast("You can book your next donation after " + remainingDays + " more days.");
                        return;
                    }

                    startActivity(new Intent(getActivity(), DonorBookingActivity.class));
                })
                .addOnFailureListener(e -> {
                    if (!isEligible && remainingDays > 0) {
                        showToast("You can book your next donation after " + remainingDays + " more days.");
                    } else {
                        startActivity(new Intent(getActivity(), DonorBookingActivity.class));
                    }
                });
    }

    private Set<String> getSeenAlertIds() {
        if (getContext() == null) {
            return new HashSet<>();
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("DonorUrgentAlertsPrefs", Context.MODE_PRIVATE);
        Set<String> seenIds = prefs.getStringSet("seen_alert_ids", new HashSet<>());

        if (seenIds == null) {
            return new HashSet<>();
        }

        return new HashSet<>(seenIds);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
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