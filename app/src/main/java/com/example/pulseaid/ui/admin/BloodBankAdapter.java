package com.example.pulseaid.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.data.admin.BloodBank;

import java.util.List;

public class BloodBankAdapter extends RecyclerView.Adapter<BloodBankAdapter.BloodBankViewHolder> {

    private List<BloodBank> bankList;
    private OnBankClickListener listener;

    public interface OnBankClickListener {
        void onDeleteClick(BloodBank bank);
    }

    public BloodBankAdapter(List<BloodBank> bankList, OnBankClickListener listener) {
        this.bankList = bankList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BloodBankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_bank_card, parent, false);
        return new BloodBankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BloodBankViewHolder holder, int position) {
        BloodBank bank = bankList.get(position);
        holder.tvBankName.setText(bank.getName());
        holder.tvBankEmail.setText(bank.getEmail());

        holder.btnDeleteBank.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(bank);
        });
    }

    @Override
    public int getItemCount() {
        return bankList.size();
    }

    public static class BloodBankViewHolder extends RecyclerView.ViewHolder {
        TextView tvBankName, tvBankEmail;
        ImageView btnDeleteBank;

        public BloodBankViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBankName = itemView.findViewById(R.id.tvBankName);
            tvBankEmail = itemView.findViewById(R.id.tvBankEmail);
            btnDeleteBank = itemView.findViewById(R.id.btnDeleteBank);
        }
    }
}