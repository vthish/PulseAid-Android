package com.example.pulseaid.ui.hospital;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pulseaid.R;
import com.example.pulseaid.ui.LoginActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class HospitalDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hospital_dashboard);

        MaterialCardView cardRequestBlood = findViewById(R.id.cardHeroRequest);
        cardRequestBlood.setOnClickListener(v -> {
            Intent intent = new Intent(HospitalDashboard.this, RequestFormActivity.class);
            startActivity(intent);
        });



        MaterialCardView cardConfirmDelivery = findViewById(R.id.cardConfirmDelivery);

        cardConfirmDelivery.setOnClickListener(v -> {
            Intent intent = new Intent(HospitalDashboard.this, ConfirmDeliveriesActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardActiveStatus).setOnClickListener(v -> {
            startActivity(new Intent(HospitalDashboard.this, ActiveRequestsActivity.class));
        });

        findViewById(R.id.cardHistory).setOnClickListener(v -> {
            startActivity(new Intent(HospitalDashboard.this, HistoryActivity.class));
        });

        View btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HospitalDashboard.this, HospitalProfileActivity.class);
            startActivity(intent);
        });

        View btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            Toast.makeText(HospitalDashboard.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(HospitalDashboard.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
        });
    }
}