package com.example.pulseaid.ui.hospital;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import java.util.List;

public class ConfirmDeliveryAdapter extends RecyclerView.Adapter<ConfirmDeliveryAdapter.ViewHolder> {

    private List<PendingDeliveryModel> pendingList;

    public ConfirmDeliveryAdapter(List<PendingDeliveryModel> pendingList) {
        this.pendingList = pendingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_confirm_delivery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingDeliveryModel model = pendingList.get(position);

        holder.tvBloodGroup.setText(model.getBloodGroup());
        holder.tvUnits.setText(model.getUnits() + " Units Arrived");
        holder.tvSourceBank.setText("From: " + model.getSourceBank());
        holder.tvTime.setText(model.getTime());

        holder.btnConfirmReceipt.setOnClickListener(v -> {
            pendingList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, pendingList.size());

            Toast.makeText(v.getContext(), model.getBloodGroup() + " Delivery Confirmed!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return pendingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBloodGroup, tvUnits, tvSourceBank, tvTime;
        Button btnConfirmReceipt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvUnits = itemView.findViewById(R.id.tvUnits);
            tvSourceBank = itemView.findViewById(R.id.tvSourceBank);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnConfirmReceipt = itemView.findViewById(R.id.btnConfirmReceipt);
        }
    }
}