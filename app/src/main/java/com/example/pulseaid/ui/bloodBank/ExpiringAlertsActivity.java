package com.example.pulseaid.ui.bloodBank;

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

public class ExpiringAlertsActivity extends AppCompatActivity {

    private RecyclerView recyclerExpiringAlerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expiring_alerts);

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerExpiringAlerts = findViewById(R.id.recyclerExpiringAlerts);
        recyclerExpiringAlerts.setLayoutManager(new LinearLayoutManager(this));

        List<AlertItem> alertList = new ArrayList<>();
        alertList.add(new AlertItem("PKT-1023", "A+", "2 Days"));
        alertList.add(new AlertItem("PKT-5041", "O-", "3 Days"));
        alertList.add(new AlertItem("PKT-2234", "B+", "5 Days"));
        alertList.add(new AlertItem("PKT-8890", "AB-", "7 Days"));

        AlertAdapter adapter = new AlertAdapter(alertList);
        recyclerExpiringAlerts.setAdapter(adapter);
    }

    // Model Class
    private static class AlertItem {
        String packetId, bloodGroup, daysLeft;
        public AlertItem(String packetId, String bloodGroup, String daysLeft) {
            this.packetId = packetId;
            this.bloodGroup = bloodGroup;
            this.daysLeft = daysLeft;
        }
    }

    // Adapter Class
    private static class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {
        private final List<AlertItem> items;

        public AlertAdapter(List<AlertItem> items) {
            this.items = items;
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
            holder.txtDaysLeft.setText(item.daysLeft);

            if(item.daysLeft.equals("2 Days") || item.daysLeft.equals("3 Days")) {
                holder.txtDaysLeft.setTextColor(android.graphics.Color.parseColor("#D32F2F")); // Red for very critical
            } else {
                holder.txtDaysLeft.setTextColor(android.graphics.Color.parseColor("#F57C00")); // Orange for warning
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