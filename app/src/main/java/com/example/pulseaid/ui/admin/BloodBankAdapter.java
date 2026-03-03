package com.example.pulseaid.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.data.User;

import java.util.List;

public class BloodBankAdapter extends RecyclerView.Adapter<BloodBankAdapter.BloodBankViewHolder> {

    private List<User> userList;
    private OnBankClickListener listener;

    public interface OnBankClickListener {
        void onDeleteClick(User user);
    }

    public BloodBankAdapter(List<User> userList, OnBankClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    public void setList(List<User> list) {
        this.userList = list;
        notifyDataSetChanged();
    }
    public void filterList(List<User> filteredList) {
        this.userList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BloodBankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_bank_card, parent, false);
        return new BloodBankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BloodBankViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvBankName.setText(user.getName());
        holder.tvBankEmail.setText(user.getEmail());

        holder.btnDeleteBank.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
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