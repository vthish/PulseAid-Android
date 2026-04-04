package com.example.pulseaid.ui.hospital;

import android.os.Bundle;
import android.util.Log;
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

public class ActiveRequestsActivity extends AppCompatActivity {

    private RecyclerView rvActiveRequests;
    private ProgressBar pbLoadingActive;
    private ActiveRequestAdapter adapter;
    private HospitalActiveRequestsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_requests);

        // Toolbar Setup
        Toolbar toolbar = findViewById(R.id.toolbarActive);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            Toast.makeText(this, "Toolbar not found", Toast.LENGTH_SHORT).show();
        }

        // Initialize Views
        rvActiveRequests = findViewById(R.id.rvActiveRequests);
        pbLoadingActive = findViewById(R.id.pbLoadingActive);

        if (rvActiveRequests == null) {
            Toast.makeText(this, "Failed to load list view", Toast.LENGTH_SHORT).show();
            return;
        }


        rvActiveRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActiveRequestAdapter();
        rvActiveRequests.setAdapter(adapter);


        viewModel = new ViewModelProvider(this).get(HospitalActiveRequestsViewModel.class);


        viewModel.activeRequests.observe(this, requests -> {
            Log.d("ActiveRequestsActivity", "Observed requests count: " + (requests != null ? requests.size() : 0));
            adapter.setList(requests);
        });

        viewModel.isLoading.observe(this, loading -> {
            if (pbLoadingActive != null) {
                pbLoadingActive.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            }
        });


        viewModel.listenToHospitalRequests();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}