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
import com.example.pulseaid.data.admin.Donor;
import com.example.pulseaid.viewmodel.admin.ManageDonorsViewModel;

public class ManageDonorsActivity extends AppCompatActivity {

    private RecyclerView donorsRecyclerView;
    private DonorAdapter adapter;
    private ProgressBar progressBar;
    private ManageDonorsViewModel viewModel;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_donors);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        donorsRecyclerView = findViewById(R.id.donorsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        donorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(ManageDonorsViewModel.class);

        progressBar.setVisibility(View.VISIBLE);
        viewModel.getDonors().observe(this, donors -> {
            progressBar.setVisibility(View.GONE);

            adapter = new DonorAdapter(donors, donor -> showDeleteConfirmationDialog(donor));
            donorsRecyclerView.setAdapter(adapter);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void showDeleteConfirmationDialog(Donor donor) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Donor")
                .setMessage("Are you sure you want to delete donor " + donor.getName() + " from the system?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteDonor(donor.getId());
                    Toast.makeText(ManageDonorsActivity.this, "Donor Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}