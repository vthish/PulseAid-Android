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
import com.pulseaid.viewmodel.hospital.HospitalDeliveryViewModel;

import java.util.ArrayList;

public class ConfirmDeliveriesActivity extends AppCompatActivity {

    private HospitalDeliveryViewModel viewModel;
    private DeliveryAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private ImageView btnBack;
    private RecyclerView rvConfirmDeliveries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_deliveries);

        setupInsets();
        initViews();
        setupRecyclerView();
        setupViewModel();
        setupObservers();
        setupActions();

        viewModel.listenToDispatchedRequests();
    }

    private void setupInsets() {
        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.pbLoadingDelivery);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvConfirmDeliveries = findViewById(R.id.rvConfirmDeliveries);
    }

    private void setupRecyclerView() {
        if (rvConfirmDeliveries != null) {
            rvConfirmDeliveries.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HospitalDeliveryViewModel.class);
        adapter = new DeliveryAdapter(viewModel);

        if (rvConfirmDeliveries != null) {
            rvConfirmDeliveries.setAdapter(adapter);
        }
    }

    private void setupObservers() {
        viewModel.isLoading.observe(this, loading -> {
            if (progressBar != null) {
                progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.dispatchedRequests.observe(this, requests -> {
            if (adapter == null || tvEmptyState == null) {
                return;
            }

            if (requests == null || requests.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                adapter.setList(new ArrayList<>());
            } else {
                tvEmptyState.setVisibility(View.GONE);
                adapter.setList(requests);
            }
        });
    }

    private void setupActions() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}