package com.example.pulseaid.ui.bloodBank;

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
import com.example.pulseaid.data.bloodBank.ExpiringAlertsRepository.AlertItem;
import com.example.pulseaid.viewmodel.bloodBank.ExpiringAlertsViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ExpiringAlertsActivity extends AppCompatActivity {

    private RecyclerView recyclerExpiringAlerts;
    private ExpiringAlertsViewModel viewModel;
    private AlertAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expiring_alerts);

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerExpiringAlerts = findViewById(R.id.recyclerExpiringAlerts);
        recyclerExpiringAlerts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AlertAdapter(new ArrayList<>());
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

    private static class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {
        private List<AlertItem> items;

        public AlertAdapter(List<AlertItem> items) {
            this.items = items;
        }

        public void updateList(List<AlertItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expiring_packet, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AlertItem item = items.get(position);
            holder.txtPacketId.setText("Packet: " + item.packetId);
            holder.txtBloodGroup.setText("Blood Group: " + item.bloodGroup);
            holder.txtDaysLeft.setText(item.daysLeftText);

            if (item.daysLeftValue <= 3) {
                holder.txtDaysLeft.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
            } else {
                holder.txtDaysLeft.setTextColor(android.graphics.Color.parseColor("#F57C00"));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtPacketId, txtBloodGroup, txtDaysLeft;

            ViewHolder(View itemView) {
                super(itemView);
                txtPacketId = itemView.findViewById(R.id.txtPacketId);
                txtBloodGroup = itemView.findViewById(R.id.txtBloodGroup);
                txtDaysLeft = itemView.findViewById(R.id.txtDaysLeft);
            }
        }
    }
}