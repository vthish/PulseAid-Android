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
import com.pulseaid.viewmodel.hospital.HospitalDeliveryViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.ViewHolder> {

    private List<HospitalActiveRequestModel> list = new ArrayList<>();
    private final HospitalDeliveryViewModel viewModel;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public DeliveryAdapter(HospitalDeliveryViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void setList(List<HospitalActiveRequestModel> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivery_confirmation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HospitalActiveRequestModel model = list.get(position);

        holder.tvBloodTag.setText(safeString(model.getRequestedBloodGroup(), "--"));
        holder.tvUnits.setText(model.getTotalUnits() + " Units Total");

        Long displayDate = getBestDispatchedDate(model);
        if (displayDate != null && displayDate > 0) {
            holder.tvDate.setText("Dispatched: " + sdf.format(new Date(displayDate)));
        } else if (model.getRequestDate() != null && model.getRequestDate() > 0) {
            holder.tvDate.setText("Dispatched: " + sdf.format(new Date(model.getRequestDate())));
        } else {
            holder.tvDate.setText("Dispatched: --");
        }

        holder.container.removeAllViews();

        List<Map<String, Object>> banks = model.getAssignedBanks();
        if (banks != null && !banks.isEmpty()) {
            for (Map<String, Object> bank : banks) {
                addDeliveryRow(holder.container, bank, model);
            }
        }
    }

    private void addDeliveryRow(LinearLayout container, Map<String, Object> bankData, HospitalActiveRequestModel model) {
        if (bankData == null) {
            return;
        }

        String name = safeString(bankData.get("bankName"), "Bank");
        String bStatus = safeString(bankData.get("deliveryStatus"), "Pending");
        String bUnits = safeString(bankData.get("unitsProvided"), "0");
        String bloodType = safeString(model.getRequestedBloodGroup(), "--");

        View rowView = LayoutInflater.from(container.getContext())
                .inflate(R.layout.row_bank_confirm, container, false);

        TextView tvInfo = rowView.findViewById(R.id.tvBankInfo);
        MaterialButton btnConfirm = rowView.findViewById(R.id.btnConfirmBank);

        tvInfo.setText(name + " | " + bloodType + " | " + bUnits + " Units");

        if ("Delivered".equalsIgnoreCase(bStatus)) {
            btnConfirm.setText("Confirmed");
            btnConfirm.setEnabled(false);
            btnConfirm.setAlpha(0.5f);
        } else {
            btnConfirm.setText("Confirm");
            btnConfirm.setEnabled(true);
            btnConfirm.setAlpha(1f);

            btnConfirm.setOnClickListener(v -> {
                bankData.put("deliveryStatus", "Delivered");
                bankData.put("confirmedDate", System.currentTimeMillis());

                btnConfirm.setText("Confirmed");
                btnConfirm.setEnabled(false);
                btnConfirm.setAlpha(0.5f);

                viewModel.updateBankDeliveryStatus(model.getRequestId(), model.getAssignedBanks());
                notifyDataSetChanged();
            });
        }

        container.addView(rowView);
    }

    private Long getBestDispatchedDate(HospitalActiveRequestModel model) {
        if (model == null || model.getAssignedBanks() == null) return null;

        Long earliest = null;

        for (Map<String, Object> bank : model.getAssignedBanks()) {
            if (bank == null) continue;

            Long dispatchedDate = getLongValue(bank.get("dispatchedDate"));
            if (dispatchedDate != null && dispatchedDate > 0) {
                if (earliest == null || dispatchedDate < earliest) {
                    earliest = dispatchedDate;
                }
            }
        }

        return earliest;
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
        TextView tvBloodTag, tvUnits, tvDate;
        LinearLayout container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBloodTag = itemView.findViewById(R.id.tvDeliveryBloodTag);
            tvUnits = itemView.findViewById(R.id.tvDeliveryUnits);
            tvDate = itemView.findViewById(R.id.tvDeliveryDate);
            container = itemView.findViewById(R.id.llDeliveryBanksContainer);
        }
    }
}