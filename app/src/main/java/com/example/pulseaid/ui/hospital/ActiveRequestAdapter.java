package com.example.pulseaid.ui.hospital;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import java.util.List;

public class ActiveRequestAdapter extends RecyclerView.Adapter<ActiveRequestAdapter.ViewHolder> {

    private List<ActiveRequestModel> list;

    public ActiveRequestAdapter(List<ActiveRequestModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActiveRequestModel model = list.get(position);

        holder.tvBloodGroup.setText(model.getBloodGroup());
        holder.tvUnits.setText(model.getUnits() + " Units Requested");
        holder.tvDate.setText("Req Date: " + model.getDateRequested());
        holder.tvStatusMessage.setText(model.getStatusMessage());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBloodGroup, tvUnits, tvDate, tvStatusMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBloodGroup = itemView.findViewById(R.id.tvReqBloodGroup);
            tvUnits = itemView.findViewById(R.id.tvReqUnits);
            tvDate = itemView.findViewById(R.id.tvReqDate);
            tvStatusMessage = itemView.findViewById(R.id.tvStatusMessage);
        }
    }
}