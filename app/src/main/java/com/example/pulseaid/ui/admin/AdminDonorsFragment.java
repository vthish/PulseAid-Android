package com.example.pulseaid.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.pulseaid.data.admin.Donor;
import com.example.pulseaid.viewmodel.admin.ManageUsersViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminDonorsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TextInputEditText etSearch;

    private DonorAdapter adapter;
    private ManageUsersViewModel viewModel;

    private List<Donor> originalDonorList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        etSearch = view.findViewById(R.id.etSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel = new ViewModelProvider(this).get(ManageUsersViewModel.class);

        adapter = new DonorAdapter(new ArrayList<>(), donor -> {
            viewModel.deleteUser(donor.getUid());
            Toast.makeText(getContext(), "Donor Deleted", Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fetchData();

        return view;
    }

    private void filter(String text) {
        List<Donor> filteredList = new ArrayList<>();
        for (Donor donor : originalDonorList) {
            boolean nameMatches = donor.getName() != null && donor.getName().toLowerCase().contains(text.toLowerCase());
            boolean emailMatches = donor.getEmail() != null && donor.getEmail().toLowerCase().contains(text.toLowerCase());

            if (nameMatches || emailMatches) {
                filteredList.add(donor);
            }
        }
        adapter.filterList(filteredList);

        if (filteredList.isEmpty() && !text.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No results found for '" + text + "'");
        } else if (filteredList.isEmpty() && text.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No data found");
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);
        viewModel.getDonors().observe(getViewLifecycleOwner(), donors -> {
            progressBar.setVisibility(View.GONE);
            if (donors == null || donors.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("No data found");
                originalDonorList.clear();
            } else {
                tvEmptyState.setVisibility(View.GONE);
                originalDonorList = donors;
                adapter.setList(donors);
            }
        });
    }
}