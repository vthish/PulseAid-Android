package com.pulseaid.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pulseaid.R;
import com.pulseaid.data.admin.Donor;

import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {

    private List<Donor> donorList;
    private OnDonorClickListener listener;

    public interface OnDonorClickListener {
        void onDeleteClick(Donor donor);
    }

    public DonorAdapter(List<Donor> donorList, OnDonorClickListener listener) {
        this.donorList = donorList;
        this.listener = listener;
    }

    public void setList(List<Donor> list) {
        this.donorList = list;
        notifyDataSetChanged();
    }

    public void filterList(List<Donor> filteredList) {
        this.donorList = filteredList;
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
        Donor donor = donorList.get(position);
        holder.tvDonorName.setText(donor.getName());
        holder.tvDonorEmail.setText(donor.getEmail());

        String bloodGroup = donor.getBloodGroup();
        if (bloodGroup != null && !bloodGroup.isEmpty()) {
            holder.tvBloodGroup.setText(bloodGroup);
        } else {
            holder.tvBloodGroup.setText("?");
        }

        holder.btnDeleteDonor.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(donor);
        });
    }

    @Override
    public int getItemCount() {
        return donorList.size();
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