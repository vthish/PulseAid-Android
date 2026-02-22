package com.example.pulseaid.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.data.admin.Hospital;
import com.example.pulseaid.viewmodel.admin.ManageHospitalsViewModel;

public class ManageHospitalsActivity extends AppCompatActivity {

    private RecyclerView hospitalsRecyclerView;
    private HospitalAdapter adapter;
    private ProgressBar progressBar;
    private ManageHospitalsViewModel viewModel;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_hospitals);

        // Handle Window Insets
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        hospitalsRecyclerView = findViewById(R.id.hospitalsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        hospitalsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(ManageHospitalsViewModel.class);

        progressBar.setVisibility(View.VISIBLE);
        viewModel.getHospitals().observe(this, hospitals -> {
            progressBar.setVisibility(View.GONE);

            adapter = new HospitalAdapter(hospitals, hospital -> showDeleteConfirmationDialog(hospital));
            hospitalsRecyclerView.setAdapter(adapter);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void showDeleteConfirmationDialog(Hospital hospital) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hospital")
                .setMessage("Are you sure you want to delete " + hospital.getName() + " from the system?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteHospital(hospital.getId());
                    Toast.makeText(ManageHospitalsActivity.this, "Hospital Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}