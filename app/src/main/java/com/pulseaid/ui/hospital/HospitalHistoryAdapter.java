package com.pulseaid.ui.hospital;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pulseaid.R;
import com.pulseaid.data.hospital.HospitalActiveRequestModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HospitalHistoryAdapter extends RecyclerView.Adapter<HospitalHistoryAdapter.ViewHolder> {

    private List<HospitalActiveRequestModel> list = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public void setList(List<HospitalActiveRequestModel> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hospital_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= list.size()) {
            return;
        }

        HospitalActiveRequestModel model = list.get(position);
        if (model == null) {
            return;
        }

        holder.tvBloodTag.setText(safeString(model.getRequestedBloodGroup(), "--"));
        holder.tvUnits.setText(model.getTotalUnits() + " Units Received");

        long completedMillis = model.getCompletedDate();
        if (completedMillis > 0) {
            holder.tvMainDate.setText("Completed on: " + sdf.format(new Date(completedMillis)));
        } else if (model.getRequestDate() > 0) {
            holder.tvMainDate.setText("Completed on: " + sdf.format(new Date(model.getRequestDate())));
        } else {
            holder.tvMainDate.setText("Completed on: --");
        }

        holder.timelineContainer.removeAllViews();

        List<Map<String, Object>> banks = model.getAssignedBanks();
        if (banks != null && !banks.isEmpty()) {
            for (Map<String, Object> bank : banks) {
                if (shouldShowDeliveredBank(bank)) {
                    addTimelineRow(holder.timelineContainer, bank, model.getRequestDate());
                }
            }
        }
    }

    private boolean shouldShowDeliveredBank(Map<String, Object> bankData) {
        if (bankData == null) return false;

        String deliveryStatus = safeString(bankData.get("deliveryStatus"), "");
        if ("Delivered".equalsIgnoreCase(deliveryStatus)) {
            return true;
        }

        Long confirmedDate = getLongValue(bankData.get("confirmedDate"));
        return confirmedDate != null && confirmedDate > 0;
    }

    private void addTimelineRow(LinearLayout container, Map<String, Object> bankData, Long globalRequestDate) {
        if (container == null || bankData == null) {
            return;
        }

        View row = LayoutInflater.from(container.getContext())
                .inflate(R.layout.row_history_timeline, container, false);

        TextView tvBankName = row.findViewById(R.id.tvTimelineBankName);
        TextView tvOrdered = row.findViewById(R.id.tvStepOrdered);
        TextView tvDispatched = row.findViewById(R.id.tvStepDispatched);
        TextView tvDelivered = row.findViewById(R.id.tvStepDelivered);

        String name = safeString(bankData.get("bankName"), "Bank");
        String units = safeString(bankData.get("unitsProvided"), "0");
        tvBankName.setText(name + " | " + units + " Units");

        if (globalRequestDate != null && globalRequestDate > 0) {
            tvOrdered.setText("Request Placed: " + sdf.format(new Date(globalRequestDate)));
        } else {
            tvOrdered.setText("Request Placed: N/A");
        }

        Long dispatchedDate = getLongValue(bankData.get("dispatchedDate"));
        if (dispatchedDate != null && dispatchedDate > 0) {
            tvDispatched.setText("Dispatched: " + sdf.format(new Date(dispatchedDate)));
        } else {
            tvDispatched.setText("Dispatched: Pending");
        }

        Long confirmedDate = getLongValue(bankData.get("confirmedDate"));
        if (confirmedDate != null && confirmedDate > 0) {
            tvDelivered.setText("Delivered: " + sdf.format(new Date(confirmedDate)));
        } else {
            tvDelivered.setText("Delivered: Pending");
        }

        container.addView(row);
    }

    private Long getLongValue(Object value) {
        if (value == null) return null;

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String safeString(Object value, String fallback) {
        if (value == null) return fallback;
        String text = String.valueOf(value).trim();
        return text.isEmpty() || "null".equalsIgnoreCase(text) ? fallback : text;
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBloodTag, tvUnits, tvMainDate;
        LinearLayout timelineContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBloodTag = itemView.findViewById(R.id.tvHistoryBloodTag);
            tvUnits = itemView.findViewById(R.id.tvHistoryUnits);
            tvMainDate = itemView.findViewById(R.id.tvHistoryMainDate);
            timelineContainer = itemView.findViewById(R.id.llHistoryTimelineContainer);
        }
    }
}