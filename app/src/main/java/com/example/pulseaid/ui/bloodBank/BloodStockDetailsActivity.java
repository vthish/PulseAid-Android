package com.example.pulseaid.ui.bloodBank;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class BloodStockDetailsActivity extends AppCompatActivity {

    private TextView txtBloodGroupTitle, txtTotalUnits, txtDonateTo, txtReceiveFrom;
    private RecyclerView recyclerDonors;
    private String bloodGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_stock_details);

        bloodGroup = getIntent().getStringExtra("BLOOD_GROUP");
        if (bloodGroup == null) {
            bloodGroup = "A+";
        }
        txtBloodGroupTitle = findViewById(R.id.txtBloodGroupTitle);
        txtTotalUnits = findViewById(R.id.txtTotalUnits);
        txtDonateTo = findViewById(R.id.txtDonateTo);
        txtReceiveFrom = findViewById(R.id.txtReceiveFrom);
        MaterialCardView btnBack = findViewById(R.id.btnBack);
        recyclerDonors = findViewById(R.id.recyclerDonors);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(BloodStockDetailsActivity.this, StockMonitosActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        txtBloodGroupTitle.setText(bloodGroup);
        setupCompatibilityData(bloodGroup);
        setupDummyDonorsList(bloodGroup);
    }

    private void setupCompatibilityData(String bg) {
        String donateTo = "";
        String receiveFrom = "";
        String units = "0";

        switch (bg) {
            case "A+":
                donateTo = "A+, AB+";
                receiveFrom = "A+, A-, O+, O-";
                units = "24 Units Available";
                break;
            case "A-":
                donateTo = "A+, A-, AB+, AB-";
                receiveFrom = "A-, O-";
                units = "05 Units Available";
                break;
            case "B+":
                donateTo = "B+, AB+";
                receiveFrom = "B+, B-, O+, O-";
                units = "18 Units Available";
                break;
            case "B-":
                donateTo = "B+, B-, AB+, AB-";
                receiveFrom = "B-, O-";
                units = "02 Units Available";
                break;
            case "AB+":
                donateTo = "AB+";
                receiveFrom = "Everyone (Universal Recipient)";
                units = "10 Units Available";
                break;
            case "AB-":
                donateTo = "AB+, AB-";
                receiveFrom = "AB-, A-, B-, O-";
                units = "01 Unit Available";
                break;
            case "O+":
                donateTo = "O+, A+, B+, AB+";
                receiveFrom = "O+, O-";
                units = "35 Units Available";
                break;
            case "O-":
                donateTo = "Everyone (Universal Donor)";
                receiveFrom = "O-";
                units = "08 Units Available";
                break;
        }

        txtDonateTo.setText(donateTo);
        txtReceiveFrom.setText(receiveFrom);
        txtTotalUnits.setText(units);
    }

    private void setupDummyDonorsList(String bg) {
        recyclerDonors.setLayoutManager(new LinearLayoutManager(this));

        List<DummyDonor> donorList = new ArrayList<>();
        donorList.add(new DummyDonor("Kamal Perera", "0771234567", bg, "Last Donated: 2 Months Ago"));
        donorList.add(new DummyDonor("Nimal Silva", "0719876543", bg, "Last Donated: 5 Months Ago"));
        donorList.add(new DummyDonor("Kasun Kalhara", "0751122334", bg, "Last Donated: 1 Year Ago"));

        DummyDonorAdapter adapter = new DummyDonorAdapter(donorList);
        recyclerDonors.setAdapter(adapter);
    }

    private static class DummyDonor {
        String name, phone, bloodGroup, lastDonated;

        public DummyDonor(String name, String phone, String bloodGroup, String lastDonated) {
            this.name = name;
            this.phone = phone;
            this.bloodGroup = bloodGroup;
            this.lastDonated = lastDonated;
        }
    }

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