package com.example.pulseaid.ui.bloodBank;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import com.example.pulseaid.data.bloodBank.HospitalRequestRepository.HospitalRequest;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private List<HospitalRequest> requests;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onIssue(String requestId);
        void onReject(String requestId);
    }

    public RequestAdapter(List<HospitalRequest> requests, OnRequestActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HospitalRequest request = requests.get(position);
        holder.hospitalName.setText(request.name);
        holder.bloodDetails.setText("Type: " + request.type + " | Qty: " + request.qty);

        holder.btnIssue.setOnClickListener(v -> listener.onIssue(request.id));
        holder.btnReject.setOnClickListener(v -> listener.onReject(request.id));
    }

    @Override
    public int getItemCount() { return requests.size(); }

    public void updateData(List<HospitalRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

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