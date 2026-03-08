package com.example.pulseaid.ui.hospital;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import java.util.List;

public class DeliveryHistoryAdapter extends RecyclerView.Adapter<DeliveryHistoryAdapter.ViewHolder> {

    private List<DeliveryHistoryModel> list;

    public DeliveryHistoryAdapter(List<DeliveryHistoryModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeliveryHistoryModel model = list.get(position);
        holder.tvGroup.setText(model.getBloodGroup());
        holder.tvUnits.setText(model.getUnits() + " Units Received");
        holder.tvSource.setText("From: " + model.getSourceBank());
        holder.tvReqDate.setText("Requested: " + model.getRequestedDate());
        holder.tvRecDate.setText("Received: " + model.getReceivedDateTime());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroup, tvUnits, tvSource, tvReqDate, tvRecDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroup = itemView.findViewById(R.id.tvHistBloodGroup);
            tvUnits = itemView.findViewById(R.id.tvHistUnits);
            tvSource = itemView.findViewById(R.id.tvHistSource);
            tvReqDate = itemView.findViewById(R.id.tvHistReqDate);
            tvRecDate = itemView.findViewById(R.id.tvHistRecDate);
        }
    }
}