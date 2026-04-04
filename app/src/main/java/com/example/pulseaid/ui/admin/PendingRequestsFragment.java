package com.example.pulseaid.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.example.pulseaid.data.admin.BloodRequest;
import com.example.pulseaid.viewmodel.admin.ManageRequestsViewModel;

public class PendingRequestsFragment extends Fragment {

    private RecyclerView pendingRecyclerView;
    private BloodRequestAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoPending;
    private ManageRequestsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_requests, container, false);

        pendingRecyclerView = view.findViewById(R.id.pendingRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoPending = view.findViewById(R.id.tvNoPending);

        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel = new ViewModelProvider(requireActivity()).get(ManageRequestsViewModel.class);

        progressBar.setVisibility(View.VISIBLE);
        viewModel.getPendingRequests().observe(getViewLifecycleOwner(), requests -> {
            progressBar.setVisibility(View.GONE);
            if (requests == null || requests.isEmpty()) {
                tvNoPending.setVisibility(View.VISIBLE);
                pendingRecyclerView.setVisibility(View.GONE);
            } else {
                tvNoPending.setVisibility(View.GONE);
                pendingRecyclerView.setVisibility(View.VISIBLE);

                adapter = new BloodRequestAdapter(requests, true, new BloodRequestAdapter.OnRequestActionListener() {
                    @Override
                    public void onResolve(BloodRequest request) {
                        viewModel.resolveRequest(request);
                        Toast.makeText(getContext(), "Request Marked as Resolved!", Toast.LENGTH_SHORT).show();
                    }
                });
                pendingRecyclerView.setAdapter(adapter);
            }
        });

        return view;
    }
}