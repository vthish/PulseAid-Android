package com.pulseaid.ui.bloodBank;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pulseaid.R;
import com.pulseaid.viewmodel.bloodBank.HospitalRequestViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;

public class HospitleRequestHandlingActivity extends AppCompatActivity {
    private HospitalRequestViewModel viewModel;
    private RequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospitle_request_handling);

        viewModel = new ViewModelProvider(this).get(HospitalRequestViewModel.class);

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerHospitalRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestAdapter(new ArrayList<>(), req -> viewModel.confirmBlood(req));
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchView);

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setHintTextColor(Color.parseColor("#757575"));
            searchEditText.setTextColor(Color.BLACK);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });

        ChipGroup filterGroup = findViewById(R.id.filterChipGroup);
        filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = checkedIds.isEmpty() ? View.NO_ID : checkedIds.get(0);
            if (checkedId == R.id.chipUrgent) viewModel.setUrgencyFilter("Urgent");
            else if (checkedId == R.id.chipNormal) viewModel.setUrgencyFilter("Normal");
            else viewModel.setUrgencyFilter("All");
        });

        viewModel.getRequestList().observe(this, requests -> {
            if (requests != null) adapter.updateData(requests);
        });

        viewModel.getActionMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.loadRequests();
    }
}