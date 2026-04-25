package com.pulseaid.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.pulseaid.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView tvTotalDonors, tvTotalInstitutions;
    private PieChart pieChartBloodGroups;
    private ProgressBar progressBar;
    private ImageView btnBack;

    private MaterialCardView cardDonors, cardInstitutes, cardChart;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analytics);

        db = FirebaseFirestore.getInstance();

        tvTotalDonors = findViewById(R.id.tvTotalDonors);
        tvTotalInstitutions = findViewById(R.id.tvTotalInstitutions);
        pieChartBloodGroups = findViewById(R.id.pieChartBloodGroups);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        cardDonors = findViewById(R.id.cardDonors);
        cardInstitutes = findViewById(R.id.cardInstitutes);
        cardChart = findViewById(R.id.cardChart);

        btnBack.setOnClickListener(v -> finish());

        prepareAnimations();
        animateIn();

        setupPieChart();
        fetchAnalyticsData();
    }

    private void prepareAnimations() {
        cardDonors.setTranslationY(100f);
        cardDonors.setAlpha(0f);
        cardInstitutes.setTranslationY(100f);
        cardInstitutes.setAlpha(0f);
        cardChart.setTranslationY(150f);
        cardChart.setAlpha(0f);
    }

    private void animateIn() {
        long duration = 600;

        cardDonors.animate()
                .translationY(0f).alpha(1f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(duration)
                .setStartDelay(100)
                .start();

        cardInstitutes.animate()
                .translationY(0f).alpha(1f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(duration)
                .setStartDelay(200)
                .start();

        cardChart.animate()
                .translationY(0f).alpha(1f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(duration)
                .setStartDelay(300)
                .start();
    }

    private void setupPieChart() {
        pieChartBloodGroups.setUsePercentValues(true);
        pieChartBloodGroups.getDescription().setEnabled(false);
        pieChartBloodGroups.setExtraOffsets(5, 10, 5, 20);

        pieChartBloodGroups.setBackgroundColor(Color.WHITE);

        pieChartBloodGroups.setDragDecelerationFrictionCoef(0.95f);

        pieChartBloodGroups.setDrawHoleEnabled(true);
        pieChartBloodGroups.setHoleColor(Color.WHITE);
        pieChartBloodGroups.setTransparentCircleColor(Color.WHITE);
        pieChartBloodGroups.setTransparentCircleAlpha(110);
        pieChartBloodGroups.setHoleRadius(55f);
        pieChartBloodGroups.setTransparentCircleRadius(60f);

        pieChartBloodGroups.setDrawCenterText(true);
        pieChartBloodGroups.setCenterText("Blood\nGroups");
        pieChartBloodGroups.setCenterTextSize(18f);
        pieChartBloodGroups.setCenterTextColor(Color.parseColor("#1E293B"));

        pieChartBloodGroups.setRotationAngle(0);
        pieChartBloodGroups.setRotationEnabled(true);
        pieChartBloodGroups.setHighlightPerTapEnabled(true);

        Legend l = pieChartBloodGroups.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setWordWrapEnabled(true);
        l.setDrawInside(false);
        l.setXEntrySpace(15f);
        l.setYEntrySpace(10f);
        l.setYOffset(10f);
        l.setTextSize(13f);
        l.setTextColor(Color.parseColor("#475569"));
    }

    private void fetchAnalyticsData() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("Users").get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                int totalDonors = 0;
                int totalInstitutions = 0;
                Map<String, Integer> bloodGroupCounts = new HashMap<>();

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String role = doc.getString("role");

                    if ("Donor".equals(role)) {
                        totalDonors++;
                        String bloodGroup = doc.getString("bloodGroup");
                        if (bloodGroup != null && !bloodGroup.isEmpty()) {
                            bloodGroupCounts.put(bloodGroup, bloodGroupCounts.getOrDefault(bloodGroup, 0) + 1);
                        }
                    } else if ("Hospital".equals(role) || "Blood Bank".equals(role) || "Blood Banks".equals(role)) {
                        totalInstitutions++;
                    }
                }

                tvTotalDonors.setText(String.valueOf(totalDonors));
                tvTotalInstitutions.setText(String.valueOf(totalInstitutions));

                loadPieChartData(bloodGroupCounts);

            } else {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPieChartData(Map<String, Integer> bloodGroupCounts) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : bloodGroupCounts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            pieChartBloodGroups.clear();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(4f);
        dataSet.setSelectionShift(6f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#EF4444"));
        colors.add(Color.parseColor("#3B82F6"));
        colors.add(Color.parseColor("#10B981"));
        colors.add(Color.parseColor("#F59E0B"));
        colors.add(Color.parseColor("#8B5CF6"));
        colors.add(Color.parseColor("#06B6D4"));
        colors.add(Color.parseColor("#EC4899"));
        colors.add(Color.parseColor("#84CC16"));

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartBloodGroups));
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        pieChartBloodGroups.setData(data);
        pieChartBloodGroups.highlightValues(null);
        pieChartBloodGroups.invalidate();

        pieChartBloodGroups.animateY(1500, Easing.EaseInOutQuart);
    }
}