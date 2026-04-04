package com.example.pulseaid.ui.donor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.data.donor.DonorHistoryModel;

import java.util.List;

public class DonorHistoryAdapter extends RecyclerView.Adapter<DonorHistoryAdapter.DonorHistoryViewHolder> {

    private final List<DonorHistoryModel> historyList;

    public DonorHistoryAdapter(List<DonorHistoryModel> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public DonorHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donor_history_record, parent, false);
        return new DonorHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorHistoryViewHolder holder, int position) {
        DonorHistoryModel item = historyList.get(position);

        holder.tvCenterName.setText(item.getCenterName());
        holder.tvDateTime.setText(item.getDate() + " | " + item.getTime());
        holder.tvStatus.setText(item.getStatus());

        if ("COMPLETED".equalsIgnoreCase(item.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
            if (item.getDonatedUnits() > 0) {
                holder.tvExtraInfo.setVisibility(View.VISIBLE);
                holder.tvExtraInfo.setText("Donated Units: " + item.getDonatedUnits());
            } else {
                holder.tvExtraInfo.setVisibility(View.GONE);
            }
        } else if ("REJECTED".equalsIgnoreCase(item.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
            holder.tvExtraInfo.setVisibility(View.VISIBLE);
            if (item.getRejectReason() != null && !item.getRejectReason().trim().isEmpty()) {
                holder.tvExtraInfo.setText("Reason: " + item.getRejectReason());
            } else {
                holder.tvExtraInfo.setText("Reason: Not specified");
            }
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#666666"));
            holder.tvExtraInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    static class DonorHistoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvCenterName, tvDateTime, tvStatus, tvExtraInfo;

        public DonorHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCenterName = itemView.findViewById(R.id.tv_center_name);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvExtraInfo = itemView.findViewById(R.id.tv_extra_info);
        }
    }
}