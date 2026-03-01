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

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    private List<User> userList;
    private OnHospitalClickListener listener;

    public interface OnHospitalClickListener {
        void onDeleteClick(User user);
    }

    public HospitalAdapter(List<User> userList, OnHospitalClickListener listener) {
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
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital_card, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvHospitalName.setText(user.getName());
        holder.tvHospitalEmail.setText(user.getEmail());

        holder.btnDeleteHospital.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class HospitalViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospitalName, tvHospitalEmail;
        ImageView btnDeleteHospital;

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvHospitalEmail = itemView.findViewById(R.id.tvHospitalEmail);
            btnDeleteHospital = itemView.findViewById(R.id.btnDeleteHospital);
        }
    }
}