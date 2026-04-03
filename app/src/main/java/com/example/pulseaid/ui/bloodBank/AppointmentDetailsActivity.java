package com.example.pulseaid.ui.bloodBank;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.bloodBank.DonorCheckinViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AppointmentDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#D32F2F"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(0);
            }
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        try {
            DonorCheckinViewModel vm = new ViewModelProvider(this).get(DonorCheckinViewModel.class);

            String appId = getIntent().getStringExtra("appId");
            String donorId = getIntent().getStringExtra("donorId");
            String bankId = getIntent().getStringExtra("bankId");
            String type = getIntent().getStringExtra("type");

            ((TextView)findViewById(R.id.tvDonorName)).setText(getIntent().getStringExtra("name"));
            ((TextView)findViewById(R.id.tvBloodType)).setText("Blood Group: " + type);
            ((TextView)findViewById(R.id.tvDate)).setText("Date: " + getIntent().getStringExtra("date"));
            ((TextView)findViewById(R.id.tvTimeSlot)).setText("Time: " + getIntent().getStringExtra("time"));
            ((TextView)findViewById(R.id.tvQueueNo)).setText("Queue No: " + getIntent().getStringExtra("queueNo"));
            ((TextView)findViewById(R.id.tvStatus)).setText("Status: " + getIntent().getStringExtra("status"));

            TextInputEditText etUnits = findViewById(R.id.etUnits);
            TextInputLayout tilUnits = findViewById(R.id.tilUnits);

            findViewById(R.id.btnComplete).setOnClickListener(v -> {
                String unitsText = etUnits.getText().toString().trim();
                if (unitsText.isEmpty()) {
                    tilUnits.setError("Required");
                    return;
                }
                vm.completeDonation(appId, donorId, bankId, type, Integer.parseInt(unitsText));
            });

            vm.getTransactionSuccess().observe(this, s -> { if(s != null && s) finish(); });
            vm.getErrorMessage().observe(this, error -> { if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show(); });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}