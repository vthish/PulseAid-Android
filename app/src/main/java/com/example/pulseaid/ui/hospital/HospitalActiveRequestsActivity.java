package com.example.pulseaid.ui.hospital;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.hospital.HospitalActiveRequestsViewModel;

public class HospitalActiveRequestsActivity extends AppCompatActivity {

    private HospitalActiveRequestsViewModel viewModel;
    private HospitalActiveRequestAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_requests);

        Toolbar toolbar = findViewById(R.id.toolbarActive);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.pbLoadingActive);
        RecyclerView rv = findViewById(R.id.rvActiveRequests);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HospitalActiveRequestAdapter();
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(HospitalActiveRequestsViewModel.class);

        viewModel.isLoading.observe(this, loading -> {
            if (progressBar != null) {
                progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.activeRequests.observe(this, requests -> {
            if (requests != null) {
                adapter.setList(requests);
                if (requests.isEmpty()) {
                    Toast.makeText(this, "No active requests found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.listenToHospitalRequests();
    }
}