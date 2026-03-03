package com.example.pulseaid.ui.bloodBank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.pulseaid.R;

public class BloodBankDashboardActivity extends AppCompatActivity {

    private CardView cardInventory, cardRequests, cardAppointments, cardAlerts;

    private TextView txtTotalStock, txtPending, txtTodayApoinment, txtExpireAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_bank_dashboard);

        initializeViews();

        setupNavigation();
    }

    private void initializeViews() {

        cardInventory = findViewById(R.id.cardInventory);
        cardRequests = findViewById(R.id.cardRequests);
        cardAppointments = findViewById(R.id.cardAppointments);
        cardAlerts = findViewById(R.id.cardAlerts);

        txtTotalStock = findViewById(R.id.txtTotalStock);
        txtPending = findViewById(R.id.txtPending);
        txtTodayApoinment = findViewById(R.id.txtTodayApoinment);
        txtExpireAlert = findViewById(R.id.txtExpireAlert);
    }

    private void setupNavigation() {
        cardInventory.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.pulseaid.ui.bloodBank.StockMonitosActivity.class));
        });

        cardRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.pulseaid.ui.bloodBank.HospitleRequestHandlingActivity.class));
        });

        cardAppointments.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.pulseaid.ui.bloodBank.DonerCheckInQueryActivity.class));
        });

        cardAlerts.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.pulseaid.ui.bloodBank.StockMonitosActivity.class));
        });
    }
}