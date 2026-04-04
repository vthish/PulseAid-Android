package com.example.pulseaid.ui.hospital;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.data.hospital.HospitalBankModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HospitalBankAdapter extends RecyclerView.Adapter<HospitalBankAdapter.BankViewHolder> {

    private static final String TAG = "PulseAid_Adapter";
    private List<HospitalBankModel> bankList = new ArrayList<>();

    public void setBanks(List<HospitalBankModel> banks) {
        try {
            this.bankList = banks != null ? banks : new ArrayList<>();
            notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error setting banks", e);
        }
    }

    @NonNull
    @Override
    public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hospital_bank_suggestion, parent, false);
            return new BankViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "onCreateViewHolder error", e);
            View fallbackView = new View(parent.getContext());
            fallbackView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return new BankViewHolder(fallbackView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BankViewHolder holder, int position) {
        try {
            if (bankList == null || position < 0 || position >= bankList.size()) {
                return;
            }

            HospitalBankModel bank = bankList.get(position);
            if (bank == null) {
                clearHolder(holder);
                return;
            }

            String bankName = safeText(bank.getName(), "Unknown Blood Bank");
            String address = safeText(bank.getAddress(), "Address not available");
            String contact = safeText(bank.getContactNo(), "Not available");
            String bloodType = safeText(bank.getProvidedBloodType(), "");
            int units = Math.max(bank.getUnitsToContribute(), 0);
            double distance = Math.max(bank.getDistanceFromHospital(), 0.0);

            holder.tvName.setText(bankName);
            holder.tvAddress.setText(address);
            holder.tvContact.setText("Contact: " + contact);
            holder.tvDistance.setText(String.format(Locale.getDefault(), "%.1f km away", distance));
            holder.tvUnits.setText(String.format(Locale.getDefault(), "Units: %d", units));

            if (!bloodType.isEmpty()) {
                holder.tvTypeTag.setText(bloodType);
                holder.tvTypeTag.setVisibility(View.VISIBLE);
            } else {
                holder.tvTypeTag.setText("");
                holder.tvTypeTag.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Binding Error at pos " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        try {
            return bankList != null ? bankList.size() : 0;
        } catch (Exception e) {
            Log.e(TAG, "getItemCount error", e);
            return 0;
        }
    }

    private String safeText(String value, String fallback) {
        try {
            if (value == null) {
                return fallback;
            }
            String text = value.trim();
            return text.isEmpty() ? fallback : text;
        } catch (Exception e) {
            Log.e(TAG, "safeText error", e);
            return fallback;
        }
    }

    private void clearHolder(BankViewHolder holder) {
        try {
            holder.tvName.setText("Unknown Blood Bank");
            holder.tvAddress.setText("Address not available");
            holder.tvContact.setText("Contact: Not available");
            holder.tvDistance.setText("0.0 km away");
            holder.tvUnits.setText("Units: 0");
            holder.tvTypeTag.setText("");
            holder.tvTypeTag.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "clearHolder error", e);
        }
    }

    static class BankViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAddress;
        TextView tvContact;
        TextView tvDistance;
        TextView tvUnits;
        TextView tvTypeTag;

        public BankViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvHospitalBankName);
            tvAddress = itemView.findViewById(R.id.tvHospitalBankAddress);
            tvContact = itemView.findViewById(R.id.tvHospitalBankContact);
            tvDistance = itemView.findViewById(R.id.tvHospitalBankDistance);
            tvUnits = itemView.findViewById(R.id.tvHospitalUnitsToProvide);
            tvTypeTag = itemView.findViewById(R.id.tvBloodTypeTag);
        }
    }
}