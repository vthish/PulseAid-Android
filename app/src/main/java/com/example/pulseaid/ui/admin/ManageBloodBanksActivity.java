package com.example.pulseaid.ui.admin;

import android.content.Intent;
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
import com.example.pulseaid.data.admin.BloodBank;
import com.example.pulseaid.viewmodel.admin.ManageBloodBanksViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ManageBloodBanksActivity extends AppCompatActivity {

    private RecyclerView bloodBanksRecyclerView;
    private BloodBankAdapter adapter;
    private ProgressBar progressBar;
    private ManageBloodBanksViewModel viewModel;
    private ImageView btnBack;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_blood_banks);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        bloodBanksRecyclerView = findViewById(R.id.bloodBanksRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        fabAdd = findViewById(R.id.fabAdd);

        bloodBanksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(ManageBloodBanksViewModel.class);

        progressBar.setVisibility(View.VISIBLE);
        viewModel.getBloodBanks().observe(this, bloodBanks -> {
            progressBar.setVisibility(View.GONE);

            adapter = new BloodBankAdapter(bloodBanks, bank -> showDeleteConfirmationDialog(bank));
            bloodBanksRecyclerView.setAdapter(adapter);
        });

        btnBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ManageBloodBanksActivity.this, AddInstitutionActivity.class);
            intent.putExtra("ROLE_TYPE", "Blood Staff");
            startActivity(intent);
        });
    }

    private void showDeleteConfirmationDialog(BloodBank bank) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Blood Bank")
                .setMessage("Are you sure you want to delete " + bank.getName() + " from the system?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteBloodBank(bank.getId());
                    Toast.makeText(ManageBloodBanksActivity.this, "Blood Bank Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}