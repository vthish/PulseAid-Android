package com.example.pulseaid.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.data.User; // Imported User class

import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {

    private List<User> userList;
    private OnDonorClickListener listener;

    public interface OnDonorClickListener {
        void onDeleteClick(User user);
    }

    public DonorAdapter(List<User> userList, OnDonorClickListener listener) {
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
    public DonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_donor_card, parent, false);
        return new DonorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvDonorName.setText(user.getName());
        holder.tvDonorEmail.setText(user.getEmail());

        // Using getBloodType() instead of getBloodGroup() based on User.java
        String bloodType = user.getBloodGroup();
        if (bloodType != null && !bloodType.isEmpty()) {
            holder.tvBloodGroup.setText(bloodType);
        } else {
            holder.tvBloodGroup.setText("?");
        }

        holder.btnDeleteDonor.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class DonorViewHolder extends RecyclerView.ViewHolder {
        TextView tvDonorName, tvDonorEmail, tvBloodGroup;
        ImageView btnDeleteDonor;

        public DonorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDonorName = itemView.findViewById(R.id.tvDonorName);
            tvDonorEmail = itemView.findViewById(R.id.tvDonorEmail);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            btnDeleteDonor = itemView.findViewById(R.id.btnDeleteDonor);
        }
    }
}