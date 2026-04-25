package com.pulseaid.ui.donor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pulseaid.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DonorUrgentAlertsFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration alertListener;

    private LinearLayout alertsContainer;
    private LinearLayout layoutLoading;
    private ScrollView scrollAlerts;
    private TextView tvEmptyAlerts;

    private String donorBloodGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_donor_urgent_alerts, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        alertsContainer = view.findViewById(R.id.alerts_container);
        layoutLoading = view.findViewById(R.id.layout_loading);
        scrollAlerts = view.findViewById(R.id.scroll_alerts);
        tvEmptyAlerts = view.findViewById(R.id.tv_no_alerts);

        showLoadingState();
        loadCurrentDonorBloodGroup();

        return view;
    }

    private void loadCurrentDonorBloodGroup() {
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String fetchedBloodGroup = userDoc.getString("bloodGroup");

                        if (!TextUtils.isEmpty(fetchedBloodGroup)) {
                            donorBloodGroup = fetchedBloodGroup.trim().toUpperCase();
                            listenToResolvedAlerts();
                        } else {
                            showEmptyState();
                        }
                    } else {
                        showEmptyState();
                    }
                })
                .addOnFailureListener(e -> showEmptyState());
    }

    private void listenToResolvedAlerts() {
        alertListener = db.collection("EmergencyRequests")
                .whereEqualTo("status", "Resolved")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        showEmptyState();
                        return;
                    }

                    renderMatchingAlerts(value);
                });
    }

    private void renderMatchingAlerts(QuerySnapshot snapshots) {
        if (getContext() == null || alertsContainer == null) {
            return;
        }

        alertsContainer.removeAllViews();

        List<DocumentSnapshot> docs = new ArrayList<>(snapshots.getDocuments());

        Collections.sort(docs, (doc1, doc2) -> {
            Long t1 = doc1.getLong("timestamp");
            Long t2 = doc2.getLong("timestamp");

            if (t1 == null) t1 = 0L;
            if (t2 == null) t2 = 0L;

            return Long.compare(t2, t1);
        });

        boolean foundAtLeastOne = false;

        for (DocumentSnapshot doc : docs) {
            final String alertId = doc.getId();
            final String rawRequestBloodGroup = doc.getString("bloodGroup");
            final String centerId = doc.getString("centerId");
            final String reason = doc.getString("reason");

            Long unitsLong = doc.getLong("units");
            final int units = unitsLong != null ? unitsLong.intValue() : 0;

            if (TextUtils.isEmpty(rawRequestBloodGroup) || TextUtils.isEmpty(centerId)) {
                continue;
            }

            final String normalizedBloodGroup = rawRequestBloodGroup.trim().toUpperCase();
            final boolean compatible = isDonorCompatibleForRequest(donorBloodGroup, normalizedBloodGroup);

            if (!compatible) {
                continue;
            }

            foundAtLeastOne = true;

            final boolean isDirectMatch = donorBloodGroup.equalsIgnoreCase(normalizedBloodGroup);
            final boolean isSeen = isAlertSeen(alertId);

            db.collection("Users").document(centerId).get()
                    .addOnSuccessListener(centerDoc -> {
                        String centerName = "Blood Center";
                        String district = "";

                        if (centerDoc.exists()) {
                            String fetchedName = centerDoc.getString("name");
                            String fetchedDistrict = centerDoc.getString("district");

                            if (!TextUtils.isEmpty(fetchedName)) {
                                centerName = fetchedName;
                            }

                            if (!TextUtils.isEmpty(fetchedDistrict)) {
                                district = fetchedDistrict;
                            }
                        }

                        View alertCard = createAlertCard(
                                alertId,
                                centerId,
                                centerName,
                                district,
                                normalizedBloodGroup,
                                reason,
                                units,
                                isDirectMatch,
                                isSeen
                        );

                        alertsContainer.addView(alertCard);
                        showAlertsState();
                    })
                    .addOnFailureListener(e -> {
                        View alertCard = createAlertCard(
                                alertId,
                                centerId,
                                "Blood Center",
                                "",
                                normalizedBloodGroup,
                                reason,
                                units,
                                isDirectMatch,
                                isSeen
                        );

                        alertsContainer.addView(alertCard);
                        showAlertsState();
                    });
        }

        if (!foundAtLeastOne) {
            showEmptyState();
        }
    }

    private View createAlertCard(String alertId,
                                 String centerId,
                                 String centerName,
                                 String district,
                                 String requestBloodGroup,
                                 String reason,
                                 int units,
                                 boolean isDirectMatch,
                                 boolean isSeen) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View cardView = inflater.inflate(R.layout.item_donor_urgent_alert, alertsContainer, false);

        MaterialCardView cardRoot = cardView.findViewById(R.id.card_alert_root);
        TextView tvCenterName = cardView.findViewById(R.id.tv_center_name);
        TextView tvBadge = cardView.findViewById(R.id.tv_alert_badge);
        TextView tvBloodGroup = cardView.findViewById(R.id.tv_blood_group);
        TextView tvReason = cardView.findViewById(R.id.tv_reason);
        TextView tvSupportMessage = cardView.findViewById(R.id.tv_support_message);
        TextView tvUnits = cardView.findViewById(R.id.tv_units);
        MaterialButton btnDonateNow = cardView.findViewById(R.id.btn_donate_now);

        tvCenterName.setText(centerName);
        tvBadge.setText("RESOLVED");
        tvBloodGroup.setText(requestBloodGroup);

        if (TextUtils.isEmpty(reason)) {
            tvReason.setText("Reason: Blood request has been confirmed and is now available for donors.");
        } else {
            tvReason.setText("Reason: " + reason);
        }

        if (units > 0) {
            tvUnits.setVisibility(View.VISIBLE);
            tvUnits.setText("Required units: " + units);
        } else {
            tvUnits.setVisibility(View.GONE);
        }

        if (isDirectMatch) {
            tvSupportMessage.setText("Thank you for being a direct match. Your willingness to donate can make an immediate life-saving difference.");
        } else {
            tvSupportMessage.setText("Thank you for stepping forward as a compatible donor. Your support still plays a vital role in helping save lives.");
        }

        applySeenStyle(cardRoot, tvCenterName, tvBloodGroup, tvReason, tvSupportMessage, !isSeen);

        cardView.setOnClickListener(v -> {
            markAlertAsSeen(alertId);
            applySeenStyle(cardRoot, tvCenterName, tvBloodGroup, tvReason, tvSupportMessage, false);
        });

        btnDonateNow.setOnClickListener(v -> {
            markAlertAsSeen(alertId);
            applySeenStyle(cardRoot, tvCenterName, tvBloodGroup, tvReason, tvSupportMessage, false);
            handleDonateNowClick(centerId, district);
        });

        return cardView;
    }

    private void handleDonateNowClick(String centerId, String district) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        Toast.makeText(getContext(), "Unable to load your profile.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Long lastDonationDate = userDoc.getLong("lastDonationDate");
                    int remainingDays = calculateRemainingDays(lastDonationDate);

                    if (remainingDays > 0) {
                        Toast.makeText(getContext(), "You can donate again after " + remainingDays + " more days.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    checkExistingAppointmentAndRedirect(uid, centerId, district);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Unable to verify your donor status right now.", Toast.LENGTH_SHORT).show()
                );
    }

    private void checkExistingAppointmentAndRedirect(String uid, String centerId, String district) {
        db.collection("appointments")
                .whereEqualTo("donorUid", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean hasActiveAppointment = false;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String status = doc.getString("status");

                        if ("PENDING".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status)) {
                            hasActiveAppointment = true;
                            break;
                        }
                    }

                    if (hasActiveAppointment) {
                        Toast.makeText(getContext(), "You already have a pending appointment.", Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = new Intent(requireContext(), DonorBookingActivity.class);
                        intent.putExtra("preselected_center_id", centerId);
                        intent.putExtra("preselected_district", district);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Unable to check your appointments right now.", Toast.LENGTH_SHORT).show()
                );
    }

    private int calculateRemainingDays(Long lastDonationDate) {
        if (lastDonationDate == null || lastDonationDate <= 0) {
            return 0;
        }

        long diffInMs = System.currentTimeMillis() - lastDonationDate;
        long daysPassed = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);
        int remainingDays = (int) (90 - daysPassed);

        return Math.max(remainingDays, 0);
    }

    private void applySeenStyle(MaterialCardView cardRoot,
                                TextView tvCenterName,
                                TextView tvBloodGroup,
                                TextView tvReason,
                                TextView tvSupportMessage,
                                boolean unread) {

        if (unread) {
            tvCenterName.setTypeface(null, Typeface.BOLD);
            tvBloodGroup.setTypeface(null, Typeface.BOLD);
            tvReason.setTypeface(null, Typeface.BOLD);
            tvSupportMessage.setTypeface(null, Typeface.BOLD);
            cardRoot.setStrokeWidth(3);
        } else {
            tvCenterName.setTypeface(null, Typeface.NORMAL);
            tvBloodGroup.setTypeface(null, Typeface.NORMAL);
            tvReason.setTypeface(null, Typeface.NORMAL);
            tvSupportMessage.setTypeface(null, Typeface.NORMAL);
            cardRoot.setStrokeWidth(1);
        }
    }

    private boolean isDonorCompatibleForRequest(String donorGroup, String requestedGroup) {
        if (TextUtils.isEmpty(donorGroup) || TextUtils.isEmpty(requestedGroup)) {
            return false;
        }

        donorGroup = donorGroup.trim().toUpperCase();
        requestedGroup = requestedGroup.trim().toUpperCase();

        switch (requestedGroup) {
            case "O-":
                return donorGroup.equals("O-");
            case "O+":
                return donorGroup.equals("O-") || donorGroup.equals("O+");
            case "A-":
                return donorGroup.equals("O-") || donorGroup.equals("A-");
            case "A+":
                return donorGroup.equals("O-") || donorGroup.equals("O+")
                        || donorGroup.equals("A-") || donorGroup.equals("A+");
            case "B-":
                return donorGroup.equals("O-") || donorGroup.equals("B-");
            case "B+":
                return donorGroup.equals("O-") || donorGroup.equals("O+")
                        || donorGroup.equals("B-") || donorGroup.equals("B+");
            case "AB-":
                return donorGroup.equals("O-") || donorGroup.equals("A-")
                        || donorGroup.equals("B-") || donorGroup.equals("AB-");
            case "AB+":
                return donorGroup.equals("O-") || donorGroup.equals("O+")
                        || donorGroup.equals("A-") || donorGroup.equals("A+")
                        || donorGroup.equals("B-") || donorGroup.equals("B+")
                        || donorGroup.equals("AB-") || donorGroup.equals("AB+");
            default:
                return false;
        }
    }

    private boolean isAlertSeen(String alertId) {
        if (getContext() == null) return false;

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("DonorUrgentAlertsPrefs", Context.MODE_PRIVATE);

        Set<String> seenIds = prefs.getStringSet("seen_alert_ids", new HashSet<>());
        return seenIds != null && seenIds.contains(alertId);
    }

    private void markAlertAsSeen(String alertId) {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("DonorUrgentAlertsPrefs", Context.MODE_PRIVATE);

        Set<String> oldSet = prefs.getStringSet("seen_alert_ids", new HashSet<>());
        Set<String> newSet = new HashSet<>();

        if (oldSet != null) {
            newSet.addAll(oldSet);
        }

        newSet.add(alertId);
        prefs.edit().putStringSet("seen_alert_ids", newSet).apply();
    }

    private void showLoadingState() {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.VISIBLE);
        }

        if (scrollAlerts != null) {
            scrollAlerts.setVisibility(View.GONE);
        }

        if (tvEmptyAlerts != null) {
            tvEmptyAlerts.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.GONE);
        }

        if (scrollAlerts != null) {
            scrollAlerts.setVisibility(View.VISIBLE);
        }

        if (alertsContainer != null) {
            alertsContainer.removeAllViews();
        }

        if (tvEmptyAlerts != null) {
            tvEmptyAlerts.setVisibility(View.VISIBLE);
            tvEmptyAlerts.setText("No confirmed urgent alerts available for your blood type right now.");
        }
    }

    private void showAlertsState() {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.GONE);
        }

        if (scrollAlerts != null) {
            scrollAlerts.setVisibility(View.VISIBLE);
        }

        if (tvEmptyAlerts != null) {
            tvEmptyAlerts.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (alertListener != null) {
            alertListener.remove();
        }
    }
}