package com.pulseaid.ui.bloodBank;

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

import com.pulseaid.R;
import com.pulseaid.data.bloodBank.BloodStockDetailsRepository.BloodPacket;
import com.pulseaid.viewmodel.bloodBank.BloodStockDetailsViewModel;
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

        viewModel = new ViewModelProvider(this).get(BloodStockDetailsViewModel.class);
        observeViewModel();
        viewModel.loadDetails(bloodGroup);
    }

    private void initializeViews() {
        txtBloodGroupTitle = findViewById(R.id.txtBloodGroupTitle);
        txtTotalUnits = findViewById(R.id.txtTotalUnits);
        txtDonateTo = findViewById(R.id.txtDonateTo);
        txtReceiveFrom = findViewById(R.id.txtReceiveFrom);
        recyclerDonors = findViewById(R.id.recyclerDonors);

        txtBloodGroupTitle.setText(bloodGroup);
        txtTotalUnits.setText("Loading units...");

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

        viewModel.getPacketList().observe(this, packets -> {
            if (packets != null) {
                int count = packets.size();
                txtTotalUnits.setText(count + " Packets Available");

                if (count <= 5) {
                    txtTotalUnits.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                } else if (count <= 10) {
                    txtTotalUnits.setTextColor(android.graphics.Color.parseColor("#F57C00"));
                } else {
                    txtTotalUnits.setTextColor(android.graphics.Color.parseColor("#388E3C"));
                }

                recyclerDonors.setLayoutManager(new LinearLayoutManager(this));
                BloodPacketAdapter adapter = new BloodPacketAdapter(packets);
                recyclerDonors.setAdapter(adapter);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class BloodPacketAdapter extends RecyclerView.Adapter<BloodPacketAdapter.ViewHolder> {
        private final List<BloodPacket> packets;

        public BloodPacketAdapter(List<BloodPacket> packets) {
            this.packets = packets;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BloodPacket packet = packets.get(position);

            holder.text1.setText("Packet ID: " + packet.packetId + " (" + packet.bloodGroup + ")");
            holder.text1.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
            holder.text1.setTextSize(16f);
            holder.text1.setTypeface(null, android.graphics.Typeface.BOLD);

            holder.text2.setText("Exp: " + packet.expiryDate + " | Status: " + packet.status);
            holder.text2.setTextColor(android.graphics.Color.parseColor("#757575"));
        }

        @Override
        public int getItemCount() {
            return packets.size();
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