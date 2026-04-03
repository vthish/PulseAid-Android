package com.example.pulseaid.ui.bloodBank;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.bloodBank.ExpiringAlertsViewModel;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;

public class ExpiringAlertsActivity extends AppCompatActivity {
    private RecyclerView recyclerExpiringAlerts;
    private ExpiringAlertsViewModel viewModel;
    private ExpiringAlertsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expiring_alerts);

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerExpiringAlerts = findViewById(R.id.recyclerExpiringAlerts);
        recyclerExpiringAlerts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExpiringAlertsAdapter(new ArrayList<>());
        recyclerExpiringAlerts.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ExpiringAlertsViewModel.class);
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getAlertList().observe(this, alerts -> {
            if (alerts != null) {
                adapter.updateList(alerts);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}