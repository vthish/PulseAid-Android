package com.pulseaid.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pulseaid.R;
import com.pulseaid.viewmodel.admin.ManageRequestsViewModel;

public class HistoryRequestsFragment extends Fragment {

    private RecyclerView historyRecyclerView;
    private BloodRequestAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoHistory;
    private ManageRequestsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_requests, container, false);

        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoHistory = view.findViewById(R.id.tvNoHistory);

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(requireActivity()).get(ManageRequestsViewModel.class);

        progressBar.setVisibility(View.VISIBLE);
        viewModel.getHistoryRequests().observe(getViewLifecycleOwner(), requests -> {
            progressBar.setVisibility(View.GONE);
            if (requests == null || requests.isEmpty()) {
                tvNoHistory.setVisibility(View.VISIBLE);
                historyRecyclerView.setVisibility(View.GONE);
            } else {
                tvNoHistory.setVisibility(View.GONE);
                historyRecyclerView.setVisibility(View.VISIBLE);

                adapter = new BloodRequestAdapter(requests, false, null);
                historyRecyclerView.setAdapter(adapter);
            }
        });

        return view;
    }
}