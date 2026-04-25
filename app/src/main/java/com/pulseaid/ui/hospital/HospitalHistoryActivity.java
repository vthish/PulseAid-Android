package com.pulseaid.ui.hospital;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pulseaid.R;
import com.pulseaid.viewmodel.hospital.HospitalHistoryViewModel;

import java.util.ArrayList;

public class HospitalHistoryActivity extends AppCompatActivity {

    private HospitalHistoryViewModel viewModel;
    private HospitalHistoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainHistory), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnBack = findViewById(R.id.btnBackHistory);
        btnBack.setOnClickListener(v -> finish());

        progressBar = findViewById(R.id.pbHistory);
        tvEmpty = findViewById(R.id.tvHistoryEmpty);

        RecyclerView rv = findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HospitalHistoryAdapter();
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(HospitalHistoryViewModel.class);

        viewModel.isLoading.observe(this, loading ->
                progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
        );

        viewModel.historyRequests.observe(this, requests -> {
            if (requests == null || requests.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                adapter.setList(new ArrayList<>());
            } else {
                tvEmpty.setVisibility(View.GONE);
                adapter.setList(requests);
            }
        });

        viewModel.listenToHistoryRequests();
    }
}