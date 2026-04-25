package com.pulseaid.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pulseaid.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
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

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analytics);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();

        tvTotalDonors = findViewById(R.id.tvTotalDonors);
        tvTotalInstitutions = findViewById(R.id.tvTotalInstitutions);
        pieChartBloodGroups = findViewById(R.id.pieChartBloodGroups);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        setupPieChart();
        fetchAnalyticsData();
    }

    private void setupPieChart() {
        pieChartBloodGroups.setDrawHoleEnabled(true);
        pieChartBloodGroups.setHoleColor(Color.WHITE);
        pieChartBloodGroups.setTransparentCircleRadius(61f);
        pieChartBloodGroups.setCenterText("Blood Groups");
        pieChartBloodGroups.setCenterTextSize(16f);
        pieChartBloodGroups.getDescription().setEnabled(false);

        Legend l = pieChartBloodGroups.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
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
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        pieChartBloodGroups.setData(data);
        pieChartBloodGroups.invalidate(); // Refresh the chart
    }
}