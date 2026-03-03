package com.example.pulseaid.ui.bloodBank;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private List<com.example.pulseaid.ui.bloodBank.HospitleRequestHandlingActivity.HospitalRequest> requests;

    public RequestAdapter(List<com.example.pulseaid.ui.bloodBank.HospitleRequestHandlingActivity.HospitalRequest> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        com.example.pulseaid.ui.bloodBank.HospitleRequestHandlingActivity.HospitalRequest request = requests.get(position);
        holder.hospitalName.setText(request.name);
        holder.bloodDetails.setText("Type: " + request.type + " | Qty: " + request.qty);
    }

    @Override
    public int getItemCount() { return requests.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView hospitalName, bloodDetails;
        Button btnIssue, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            hospitalName = itemView.findViewById(R.id.txtHospitalName);
            bloodDetails = itemView.findViewById(R.id.txtBloodDetails);
            btnIssue = itemView.findViewById(R.id.btnIssue);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}