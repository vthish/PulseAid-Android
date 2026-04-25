package com.pulseaid.ui.admin;

import android.content.Intent;
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

import com.pulseaid.R;
import com.pulseaid.data.User;
import com.pulseaid.viewmodel.admin.ManageUsersViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminBloodBanksFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TextInputEditText etSearch;

    private BloodBankAdapter adapter;
    private ManageUsersViewModel viewModel;

    private List<User> originalUserList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        etSearch = view.findViewById(R.id.etSearch);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel = new ViewModelProvider(this).get(ManageUsersViewModel.class);

        adapter = new BloodBankAdapter(new ArrayList<>(), user -> {
            viewModel.deleteUser(user.getUid());
            Toast.makeText(getContext(), "Blood Bank Deleted", Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        fabAdd.setVisibility(View.VISIBLE);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddInstitutionActivity.class);
            intent.putExtra("INSTITUTION_TYPE", "Blood Bank");
            startActivity(intent);
        });

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
        List<User> filteredList = new ArrayList<>();
        for (User user : originalUserList) {
            if (user.getName().toLowerCase().contains(text.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(user);
            }
        }
        adapter.filterList(filteredList);
    }

    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);
        viewModel.getUsersByRole("Blood Bank").observe(getViewLifecycleOwner(), users -> {
            progressBar.setVisibility(View.GONE);
            if (users == null || users.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                originalUserList.clear();
            } else {
                tvEmptyState.setVisibility(View.GONE);
                originalUserList = users;
                adapter.setList(users);
            }
        });
    }
}