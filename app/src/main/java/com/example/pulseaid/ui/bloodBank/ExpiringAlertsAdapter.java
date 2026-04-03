package com.example.pulseaid.ui.bloodBank;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import com.example.pulseaid.data.bloodBank.ExpiringAlertsRepository.AlertItem;
import java.util.List;

public class ExpiringAlertsAdapter extends RecyclerView.Adapter<ExpiringAlertsAdapter.ViewHolder> {
    private List<AlertItem> items;

    public ExpiringAlertsAdapter(List<AlertItem> items) {
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

        if (item.daysLeftValue < 1) {
            holder.txtDaysLeft.setTextColor(Color.parseColor("#B71C1C")); // Dark Red for hours
        } else if (item.daysLeftValue <= 3) {
            holder.txtDaysLeft.setTextColor(Color.parseColor("#D32F2F")); // Red
        } else {
            holder.txtDaysLeft.setTextColor(Color.parseColor("#F57C00")); // Orange
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

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