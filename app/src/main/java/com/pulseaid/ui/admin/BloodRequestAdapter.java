package com.pulseaid.ui.admin;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pulseaid.R;
import com.pulseaid.data.admin.BloodRequest;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class BloodRequestAdapter extends RecyclerView.Adapter<BloodRequestAdapter.RequestViewHolder> {
    private List<BloodRequest> requestList;
    private boolean isPendingTab;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener { void onResolve(BloodRequest request); }

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

        // Static Urgency text
        holder.tvDetails.setText("Quantity: " + request.getQuantity() + " Units | Urgency: Emergency");
        holder.tvStatus.setText("Status: " + request.getStatus());

        if ("Resolved".equals(request.getStatus())) holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        else holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));

        if (isPendingTab) {
            holder.actionButtonsLayout.setVisibility(View.VISIBLE);
            holder.btnBroadcast.setOnClickListener(v -> { if (listener != null) listener.onResolve(request); });
        } else holder.actionButtonsLayout.setVisibility(View.GONE);

        // Click for popup details
        holder.itemView.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Emergency Reason")
                    .setMessage(request.getReason() != null ? request.getReason() : "No details provided.")
                    .setPositiveButton("Close", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() { return requestList.size(); }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospitalName, tvDetails, tvBloodGroup, tvStatus;
        LinearLayout actionButtonsLayout;
        MaterialButton btnBroadcast, btnResolve;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            btnBroadcast = itemView.findViewById(R.id.btnBroadcast);
            btnResolve = itemView.findViewById(R.id.btnResolve);
        }
    }
}