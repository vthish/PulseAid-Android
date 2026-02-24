package com.example.pulseaid.ui.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.data.admin.BloodRequest;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class BloodRequestAdapter extends RecyclerView.Adapter<BloodRequestAdapter.RequestViewHolder> {

    private List<BloodRequest> requestList;
    private boolean isPendingTab;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onApprove(BloodRequest request);
        void onReject(BloodRequest request);
    }

    public BloodRequestAdapter(List<BloodRequest> requestList, boolean isPendingTab, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.isPendingTab = isPendingTab;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        BloodRequest request = requestList.get(position);

        holder.tvHospitalName.setText(request.getHospitalName());
        holder.tvBloodGroup.setText(request.getBloodGroup());

        // Fixed the terms back to English
        String details = "Quantity: " + request.getQuantity() + " Units | Urgency: " + request.getUrgency();
        holder.tvDetails.setText(details);

        holder.tvStatus.setText("Status: " + request.getStatus());

        if ("Approved".equals(request.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if ("Rejected".equals(request.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")); // Red
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
        }

        if (isPendingTab) {
            holder.actionButtonsLayout.setVisibility(View.VISIBLE);
        } else {
            holder.actionButtonsLayout.setVisibility(View.GONE);
        }

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(request);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(request);
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospitalName, tvDetails, tvBloodGroup, tvStatus;
        LinearLayout actionButtonsLayout;
        MaterialButton btnApprove, btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}