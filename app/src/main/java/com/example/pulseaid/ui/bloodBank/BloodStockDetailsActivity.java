package com.example.pulseaid.ui.bloodBank;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.data.bloodBank.BloodStockDetailsRepository.DummyDonor;
import com.example.pulseaid.viewmodel.bloodBank.BloodStockDetailsViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class BloodStockDetailsActivity extends AppCompatActivity {

    private TextView txtBloodGroupTitle, txtTotalUnits, txtDonateTo, txtReceiveFrom;
    private RecyclerView recyclerDonors;
    private String bloodGroup;

    private BloodStockDetailsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_stock_details);

        bloodGroup = getIntent().getStringExtra("BLOOD_GROUP");
        if (bloodGroup == null) {
            bloodGroup = "A+";
        }

        initializeViews();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(BloodStockDetailsViewModel.class);

        // Observe Data
        observeViewModel();

        // Fetch Data
        viewModel.loadDetails(bloodGroup);
    }

    private void initializeViews() {
        txtBloodGroupTitle = findViewById(R.id.txtBloodGroupTitle);
        txtTotalUnits = findViewById(R.id.txtTotalUnits);
        txtDonateTo = findViewById(R.id.txtDonateTo);
        txtReceiveFrom = findViewById(R.id.txtReceiveFrom);
        recyclerDonors = findViewById(R.id.recyclerDonors);

        txtBloodGroupTitle.setText(bloodGroup);
        // Assuming units logic is handled elsewhere or passed via Intent, setting dummy for now.
        txtTotalUnits.setText("Check Main Stock for Units");

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(BloodStockDetailsActivity.this, StockMonitosActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void observeViewModel() {
        viewModel.getCompatibilityInfo().observe(this, info -> {
            if (info != null) {
                txtDonateTo.setText(info.get("donateTo"));
                txtReceiveFrom.setText(info.get("receiveFrom"));
            }
        });

        viewModel.getDonorList().observe(this, donors -> {
            if (donors != null) {
                recyclerDonors.setLayoutManager(new LinearLayoutManager(this));
                DummyDonorAdapter adapter = new DummyDonorAdapter(donors);
                recyclerDonors.setAdapter(adapter);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter for RecyclerView
    private static class DummyDonorAdapter extends RecyclerView.Adapter<DummyDonorAdapter.ViewHolder> {
        private final List<DummyDonor> donors;

        public DummyDonorAdapter(List<DummyDonor> donors) {
            this.donors = donors;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DummyDonor donor = donors.get(position);
            holder.text1.setText(donor.name + " (" + donor.bloodGroup + ")");
            holder.text1.setTextColor(android.graphics.Color.parseColor("#212121"));
            holder.text1.setTextSize(16f);
            holder.text1.setTypeface(null, android.graphics.Typeface.BOLD);

            holder.text2.setText(donor.phone + " | " + donor.lastDonated);
            holder.text2.setTextColor(android.graphics.Color.parseColor("#757575"));
        }

        @Override
        public int getItemCount() {
            return donors.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);

                itemView.setPadding(30, 30, 30, 30);
                itemView.setBackgroundColor(android.graphics.Color.WHITE);

                androidx.recyclerview.widget.RecyclerView.LayoutParams params = new androidx.recyclerview.widget.RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 16);
                itemView.setLayoutParams(params);
            }
        }
    }
}