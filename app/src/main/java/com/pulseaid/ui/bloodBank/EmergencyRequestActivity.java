package com.pulseaid.ui.bloodBank;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pulseaid.R;
import com.pulseaid.viewmodel.bloodBank.EmergencyRequestViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class EmergencyRequestActivity extends AppCompatActivity {
    private AutoCompleteTextView dropdownBloodGroup;
    private TextInputEditText etUnits, etReason;
    private MaterialButton btnSend;
    private EmergencyRequestViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_request);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(EmergencyRequestViewModel.class);

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        dropdownBloodGroup = findViewById(R.id.dropdownBloodGroup);
        etUnits = findViewById(R.id.etUnits);
        etReason = findViewById(R.id.etReason);
        btnSend = findViewById(R.id.btnSendRequest);

        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bloodGroups);
        dropdownBloodGroup.setAdapter(adapter);

        // Observe ViewModel Data
        observeViewModel();

        btnSend.setOnClickListener(v -> {
            String type = dropdownBloodGroup.getText().toString();
            String qty = etUnits.getText().toString();
            String reason = etReason.getText().toString();

            viewModel.submitEmergencyRequest(type, qty, reason);
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                btnSend.setEnabled(false);
                btnSend.setText("SENDING...");
            } else {
                btnSend.setEnabled(true);
                btnSend.setText("SEND REQUEST");
            }
        });

        viewModel.getIsSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Emergency Request Sent!", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}