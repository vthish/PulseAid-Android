package com.example.pulseaid.ui.bloodBank;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.bloodBank.HospitalRequestViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class HospitleRequestHandlingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private HospitalRequestViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospitle_request_handling);

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerHospitalRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with empty list
        adapter = new RequestAdapter(new ArrayList<>(), new RequestAdapter.OnRequestActionListener() {
            @Override
            public void onIssue(String requestId) {
                viewModel.issueBlood(requestId);
                Toast.makeText(HospitleRequestHandlingActivity.this, "Issuing blood...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReject(String requestId) {
                viewModel.rejectRequest(requestId);
                Toast.makeText(HospitleRequestHandlingActivity.this, "Request rejected.", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HospitalRequestViewModel.class);

        // Observe Data
        observeViewModel();

        // Load data
        viewModel.loadRequests();
    }

    private void observeViewModel() {
        viewModel.getRequestList().observe(this, requests -> {
            if (requests != null) {
                adapter.updateData(requests);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            // Optional: Show/hide a progress bar here
        });
    }
}