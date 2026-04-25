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
        cardDonors.setTranslationY(80f);
        cardDonors.setAlpha(0f);
        cardInstitutes.setTranslationY(80f);
        cardInstitutes.setAlpha(0f);
        cardChart.setTranslationY(120f);
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
        pieChartBloodGroups.setExtraOffsets(5, 5, 5, 15);

        pieChartBloodGroups.setBackgroundColor(Color.WHITE);

        pieChartBloodGroups.setDragDecelerationFrictionCoef(0.95f);

        pieChartBloodGroups.setDrawHoleEnabled(true);
        pieChartBloodGroups.setHoleColor(Color.WHITE);
        pieChartBloodGroups.setTransparentCircleColor(Color.WHITE);
        pieChartBloodGroups.setTransparentCircleAlpha(110);
        pieChartBloodGroups.setHoleRadius(52f);
        pieChartBloodGroups.setTransparentCircleRadius(57f);

        pieChartBloodGroups.setDrawCenterText(true);
        pieChartBloodGroups.setCenterText("Blood\nGroups");
        pieChartBloodGroups.setCenterTextSize(18f);
        pieChartBloodGroups.setCenterTextColor(Color.parseColor("#334155"));

        pieChartBloodGroups.setRotationAngle(0);
        pieChartBloodGroups.setRotationEnabled(true);
        pieChartBloodGroups.setHighlightPerTapEnabled(true);

        Legend l = pieChartBloodGroups.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setWordWrapEnabled(true);
        l.setDrawInside(false);
        l.setXEntrySpace(12f);
        l.setYEntrySpace(8f);
        l.setYOffset(10f);
        l.setTextSize(12f);
        l.setTextColor(Color.parseColor("#64748B"));
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

                        // NPE Warning fixed here
                        if (bloodGroup != null && !bloodGroup.isEmpty()) {
                            Integer currentCount = bloodGroupCounts.get(bloodGroup);
                            bloodGroupCounts.put(bloodGroup, (currentCount == null ? 0 : currentCount) + 1);
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
        dataSet.setSelectionShift(7f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#F87171")); // Light Coral
        colors.add(Color.parseColor("#60A5FA")); // Light Blue
        colors.add(Color.parseColor("#34D399")); // Emerald
        colors.add(Color.parseColor("#FBBF24")); // Amber
        colors.add(Color.parseColor("#A78BFA")); // Violet
        colors.add(Color.parseColor("#22D3EE")); // Cyan
        colors.add(Color.parseColor("#F472B6")); // Pink
        colors.add(Color.parseColor("#A3E635")); // Lime

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartBloodGroups));
        data.setValueTextSize(13f);
        data.setValueTextColor(Color.WHITE);

        data.setValueTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        pieChartBloodGroups.setData(data);
        pieChartBloodGroups.highlightValues(null);
        pieChartBloodGroups.invalidate();

        pieChartBloodGroups.animateY(1500, Easing.EaseInOutQuart);
    }
}