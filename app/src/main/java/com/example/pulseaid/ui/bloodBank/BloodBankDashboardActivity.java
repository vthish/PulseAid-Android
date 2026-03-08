package com.example.pulseaid.ui.bloodBank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.pulseaid.R;
import com.example.pulseaid.ui.LoginActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class BloodBankDashboardActivity extends AppCompatActivity {

    private CardView cardInventory, cardRequests, cardAppointments, cardAlerts;
    private TextView txtTotalStock, txtPending, txtTodayApoinment, txtExpireAlert;
    private MaterialCardView btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_bank_dashboard);

        initializeViews();
        setupNavigation();
        setupLogout();
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

        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupNavigation() {
        cardInventory.setOnClickListener(v ->
                startActivity(new Intent(this, StockMonitosActivity.class))
        );

        cardRequests.setOnClickListener(v ->
                startActivity(new Intent(this, HospitleRequestHandlingActivity.class))
        );

        cardAppointments.setOnClickListener(v ->
                startActivity(new Intent(this, DonerCheckInQueryActivity.class))
        );

        cardAlerts.setOnClickListener(v ->
                startActivity(new Intent(this, StockMonitosActivity.class))
        );
    }

    private void setupLogout() {
        if (btnLogout == null) return;

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent i = new Intent(BloodBankDashboardActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }
}