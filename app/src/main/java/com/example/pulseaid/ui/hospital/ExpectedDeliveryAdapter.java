package com.example.pulseaid.ui.hospital;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import java.util.List;

public class ExpectedDeliveryAdapter extends RecyclerView.Adapter<ExpectedDeliveryAdapter.ViewHolder> {

    private List<PendingDeliveryModel> expectedList;

    public ExpectedDeliveryAdapter(List<PendingDeliveryModel> expectedList) {
        this.expectedList = expectedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expected_delivery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingDeliveryModel model = expectedList.get(position);

        holder.tvGroup.setText(model.getBloodGroup());
        holder.tvUnits.setText(model.getUnits() + " Units Expected");
        holder.tvSource.setText("From: " + model.getSourceBank());
        holder.tvETA.setText("ETA: " + model.getTime());
    }

    @Override
    public int getItemCount() {
        return expectedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroup, tvUnits, tvSource, tvETA;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroup = itemView.findViewById(R.id.tvExpectedBloodGroup);
            tvUnits = itemView.findViewById(R.id.tvExpectedUnits);
            tvSource = itemView.findViewById(R.id.tvExpectedSource);
            tvETA = itemView.findViewById(R.id.tvExpectedETA);
        }
    }
}