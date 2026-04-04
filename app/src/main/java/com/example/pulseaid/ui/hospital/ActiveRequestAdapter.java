package com.example.pulseaid.ui.hospital;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.data.hospital.HospitalActiveRequestModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ActiveRequestAdapter extends RecyclerView.Adapter<ActiveRequestAdapter.ViewHolder> {

    private List<HospitalActiveRequestModel> list = new ArrayList<>();

    public void setList(List<HospitalActiveRequestModel> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HospitalActiveRequestModel model = list.get(position);

        String bloodGroup = model.getRequestedBloodGroup() != null ? model.getRequestedBloodGroup() : "N/A";
        String status = model.getStatus() != null ? model.getStatus() : "Unknown";
        String urgency = model.getUrgency() != null ? model.getUrgency() : "";

        String overallUnits = "0";
        if (model.getTotalUnits() != null) {
            overallUnits = String.valueOf(model.getTotalUnits());
        }

        holder.tvBloodTag.setText(bloodGroup);
        holder.tvUnits.setText(overallUnits + " Units Requested");
        holder.tvStatusValue.setText(status);

        Long requestMillis = model.getRequestDate();
        if (requestMillis != null && requestMillis > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            holder.tvDate.setText("Requested on: " + sdf.format(new Date(requestMillis)));
        } else {
            holder.tvDate.setText("Requested on: N/A");
        }

        holder.tvUrgency.setVisibility("Urgent".equalsIgnoreCase(urgency) ? View.VISIBLE : View.GONE);

        setStatusColor(holder.tvStatusValue, status);

        holder.llBankDetailsContainer.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (holder.llBankDetailsContainer.getVisibility() == View.GONE) {
                holder.llBankDetailsContainer.setVisibility(View.VISIBLE);
                holder.tvTapToView.setText("Tap to collapse");
            } else {
                holder.llBankDetailsContainer.setVisibility(View.GONE);
                holder.tvTapToView.setText("Tap to view bank details");
            }
        });

        holder.llBankDetailsContainer.removeAllViews();

        List<Map<String, Object>> banks = model.getAssignedBanks();
        if (banks != null && !banks.isEmpty()) {
            for (Map<String, Object> bank : banks) {
                addBankRow(holder.llBankDetailsContainer, bank);
            }
        } else {
            TextView tvNoBanks = new TextView(holder.itemView.getContext());
            tvNoBanks.setText("Waiting for Smart Suggestion to assign banks...");
            tvNoBanks.setTextSize(12);
            tvNoBanks.setAlpha(0.6f);
            holder.llBankDetailsContainer.addView(tvNoBanks);
        }
    }

    private void addBankRow(LinearLayout container, Map<String, Object> bankData) {
        String name = String.valueOf(bankData.getOrDefault("bankName", "Unknown Bank"));
        String bStatus = String.valueOf(bankData.getOrDefault("deliveryStatus", "Pending"));

        Object unitsObj = bankData.get("unitsProvided");
        int bUnits = 0;
        if (unitsObj != null) {
            try {
                bUnits = Integer.parseInt(unitsObj.toString());
            } catch (NumberFormatException e) {
                bUnits = 0;
            }
        }

        TextView tvBank = new TextView(container.getContext());
        tvBank.setText("• " + name + ": " + bUnits + " Units : " + bStatus);
        tvBank.setTextSize(13);
        tvBank.setPadding(0, 4, 0, 4);
        tvBank.setTypeface(null, Typeface.BOLD);

        if ("Dispatched".equalsIgnoreCase(bStatus)) tvBank.setTextColor(Color.parseColor("#1976D2"));
        else if ("Accepted".equalsIgnoreCase(bStatus)) tvBank.setTextColor(Color.parseColor("#388E3C"));
        else tvBank.setTextColor(Color.parseColor("#E53935"));

        container.addView(tvBank);
    }

    private void setStatusColor(TextView tv, String status) {
        if ("Pending".equalsIgnoreCase(status)) tv.setTextColor(Color.parseColor("#E53935"));
        else if ("Accepted".equalsIgnoreCase(status)) tv.setTextColor(Color.parseColor("#388E3C"));
        else if ("Dispatched".equalsIgnoreCase(status)) tv.setTextColor(Color.parseColor("#1976D2"));
        else tv.setTextColor(Color.parseColor("#333333"));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBloodTag, tvUnits, tvDate, tvStatusValue, tvUrgency, tvTapToView;
        LinearLayout llBankDetailsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBloodTag = itemView.findViewById(R.id.tvActiveBloodTag);
            tvUnits = itemView.findViewById(R.id.tvActiveUnits);
            tvDate = itemView.findViewById(R.id.tvActiveDate);
            tvStatusValue = itemView.findViewById(R.id.tvActiveStatusValue);
            tvUrgency = itemView.findViewById(R.id.tvUrgencyTag);
            tvTapToView = itemView.findViewById(R.id.tvTapToView);
            llBankDetailsContainer = itemView.findViewById(R.id.llBankDetailsContainer);
        }
    }
}