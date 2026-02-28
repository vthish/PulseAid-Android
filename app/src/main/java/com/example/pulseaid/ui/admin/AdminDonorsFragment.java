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
import com.example.pulseaid.viewmodel.admin.ManageUsersViewModel;

import java.util.ArrayList;

public class AdminDonorsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private DonorAdapter adapter;
    private ManageUsersViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel = new ViewModelProvider(this).get(ManageUsersViewModel.class);

        adapter = new DonorAdapter(new ArrayList<>(), user -> {
            viewModel.deleteUser(user.getUid());
            Toast.makeText(getContext(), "Donor Deleted", Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        fetchData();

        return view;
    }

    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);
        viewModel.getUsersByRole("Donor").observe(getViewLifecycleOwner(), users -> {
            progressBar.setVisibility(View.GONE);
            if (users == null || users.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                adapter.setList(users);
            }
        });
    }
}