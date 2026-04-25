package com.pulseaid.ui.donor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pulseaid.R;
import com.pulseaid.data.donor.DonorHistoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DonarHistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private TextView tvTotalDonations, tvLastDonation, tvNoHistory;
    private LinearLayout layoutHistoryLoading;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<DonorHistoryModel> historyList;
    private DonorHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_donar_history, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        tvTotalDonations = view.findViewById(R.id.tv_total_donations);
        tvLastDonation = view.findViewById(R.id.tv_last_donation);
        tvNoHistory = view.findViewById(R.id.tv_no_history);
        layoutHistoryLoading = view.findViewById(R.id.layout_history_loading);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        historyList = new ArrayList<>();
        adapter = new DonorHistoryAdapter(historyList);
        rvHistory.setAdapter(adapter);

        showLoadingState();
        loadHistory();

        return view;
    }

    private void loadHistory() {
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("appointments")
                .whereEqualTo("donorUid", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    historyList.clear();

                    final int[] completedCount = {0};
                    final long[] latestTimestamp = {0};
                    final String[] latestDate = {"-"};

                    List<DonorHistoryModel> tempList = new ArrayList<>();

                    int totalDocs = querySnapshot.size();

                    if (totalDocs == 0) {
                        tvTotalDonations.setText("0");
                        tvLastDonation.setText("-");
                        adapter.notifyDataSetChanged();
                        showEmptyState();
                        return;
                    }

                    final int[] processedCount = {0};

                    querySnapshot.getDocuments().forEach(doc -> {

                        String statusTemp = doc.getString("status");
                        if (TextUtils.isEmpty(statusTemp)) {
                            processedCount[0]++;
                            checkAndPublish(processedCount[0], totalDocs, tempList, completedCount[0], latestDate[0]);
                            return;
                        }

                        final String status = statusTemp;

                        if (!status.equalsIgnoreCase("COMPLETED") &&
                                !status.equalsIgnoreCase("REJECTED")) {
                            processedCount[0]++;
                            checkAndPublish(processedCount[0], totalDocs, tempList, completedCount[0], latestDate[0]);
                            return;
                        }

                        final String centerId = doc.getString("centerId");
                        final String date = doc.getString("date");
                        final String time = doc.getString("timeSlot");

                        Long timestampLong = doc.getLong("timestamp");
                        final long timestamp = timestampLong != null ? timestampLong : 0;

                        Long unitsLong = doc.getLong("donatedUnits");
                        final int donatedUnits = unitsLong != null ? unitsLong.intValue() : 0;

                        String rejectReasonTemp = doc.getString("rejectReason");
                        final String rejectReason = rejectReasonTemp != null ? rejectReasonTemp : "";

                        if (status.equalsIgnoreCase("COMPLETED")) {
                            completedCount[0]++;

                            if (timestamp > latestTimestamp[0]) {
                                latestTimestamp[0] = timestamp;
                                latestDate[0] = formatDisplayDate(date);
                            }
                        }

                        db.collection("Users").document(centerId)
                                .get()
                                .addOnSuccessListener(centerDoc -> {

                                    String centerName = "Blood Center";

                                    if (centerDoc.exists()) {
                                        String name = centerDoc.getString("name");
                                        if (!TextUtils.isEmpty(name)) {
                                            centerName = name;
                                        }
                                    }

                                    DonorHistoryModel model = new DonorHistoryModel(
                                            centerName,
                                            date,
                                            time,
                                            status,
                                            donatedUnits,
                                            rejectReason
                                    );

                                    tempList.add(model);

                                    processedCount[0]++;
                                    checkAndPublish(processedCount[0], totalDocs, tempList, completedCount[0], latestDate[0]);
                                })
                                .addOnFailureListener(e -> {
                                    processedCount[0]++;
                                    checkAndPublish(processedCount[0], totalDocs, tempList, completedCount[0], latestDate[0]);
                                });
                    });
                })
                .addOnFailureListener(e -> showEmptyState());
    }

    private void checkAndPublish(int processedCount, int totalDocs, List<DonorHistoryModel> tempList, int completedCount, String latestDate) {
        if (processedCount == totalDocs) {
            Collections.sort(tempList, (a, b) -> b.getDate().compareTo(a.getDate()));

            historyList.clear();
            historyList.addAll(tempList);
            adapter.notifyDataSetChanged();

            tvTotalDonations.setText(String.valueOf(completedCount));
            tvLastDonation.setText(latestDate);

            if (historyList.isEmpty()) {
                showEmptyState();
            } else {
                showContentState();
            }
        }
    }

    private void showLoadingState() {
        layoutHistoryLoading.setVisibility(View.VISIBLE);
        rvHistory.setVisibility(View.GONE);
        tvNoHistory.setVisibility(View.GONE);
    }

    private void showContentState() {
        layoutHistoryLoading.setVisibility(View.GONE);
        rvHistory.setVisibility(View.VISIBLE);
        tvNoHistory.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        layoutHistoryLoading.setVisibility(View.GONE);
        rvHistory.setVisibility(View.GONE);
        tvNoHistory.setVisibility(View.VISIBLE);
    }

    private String formatDisplayDate(String rawDate) {
        if (TextUtils.isEmpty(rawDate)) {
            return "-";
        }

        List<SimpleDateFormat> inputFormats = new ArrayList<>();
        inputFormats.add(new SimpleDateFormat("yyyy-M-d", Locale.getDefault()));
        inputFormats.add(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()));

        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        for (SimpleDateFormat inputFormat : inputFormats) {
            try {
                inputFormat.setLenient(false);
                return outputFormat.format(inputFormat.parse(rawDate));
            } catch (Exception ignored) {
            }
        }

        return rawDate;
    }
}