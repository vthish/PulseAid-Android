package com.example.pulseaid.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.data.admin.Hospital;

import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    private List<Hospital> hospitalList;
    private OnHospitalClickListener listener;

    public interface OnHospitalClickListener {
        void onDeleteClick(Hospital hospital);
    }

    public HospitalAdapter(List<Hospital> hospitalList, OnHospitalClickListener listener) {
        this.hospitalList = hospitalList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital_card, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        Hospital hospital = hospitalList.get(position);
        holder.tvHospitalName.setText(hospital.getName());
        holder.tvHospitalEmail.setText(hospital.getEmail());

        holder.btnDeleteHospital.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(hospital);
        });
    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
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