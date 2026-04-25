package com.pulseaid.ui.hospital;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pulseaid.R;
import com.pulseaid.data.hospital.HospitalActiveRequestModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HospitalActiveRequestAdapter extends RecyclerView.Adapter<HospitalActiveRequestAdapter.RequestViewHolder> {

    private List<HospitalActiveRequestModel> requestList = new ArrayList<>();

    public void setList(List<HospitalActiveRequestModel> list) {
        this.requestList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        HospitalActiveRequestModel request = requestList.get(position);

        holder.tvUnits.setText(request.getTotalUnits() + " Units Requested");
        holder.tvBloodTag.setText(request.getRequestedBloodGroup());
        holder.tvStatus.setText(request.getStatus());

        if (request.getRequestDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            String dateStr = sdf.format(new java.util.Date(request.getRequestDate()));
            holder.tvDate.setText("Requested on: " + dateStr);
        }

        if ("Urgent".equalsIgnoreCase(request.getUrgency())) {
            holder.tvUrgency.setVisibility(View.VISIBLE);
        } else {
            holder.tvUrgency.setVisibility(View.GONE);
        }

        if ("Pending".equalsIgnoreCase(request.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#E53935"));
        } else if ("Accepted".equalsIgnoreCase(request.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#388E3C"));
        } else if ("Dispatched".equalsIgnoreCase(request.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#1976D2"));
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvUnits, tvDate, tvBloodTag, tvStatus, tvUrgency;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUnits = itemView.findViewById(R.id.tvActiveUnits);
            tvDate = itemView.findViewById(R.id.tvActiveDate);
            tvBloodTag = itemView.findViewById(R.id.tvActiveBloodTag);
            tvStatus = itemView.findViewById(R.id.tvActiveStatusValue);
            tvUrgency = itemView.findViewById(R.id.tvUrgencyTag);
        }
    }
}