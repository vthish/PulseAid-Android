package com.example.pulseaid.ui.hospital;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pulseaid.R;
import com.google.android.material.card.MaterialCardView;

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

        MaterialCardView bannerAlert = findViewById(R.id.bannerAlert);
        bannerAlert.setOnClickListener(v -> {
            Intent intent2 = new Intent(HospitalDashboard.this, DelivaryStatusActivity.class);

            intent2.putExtra("BLOOD_GROUP", "O+");
            intent2.putExtra("UNITS", "04");
            intent2.putExtra("ETA", "15 Mins");

            startActivity(intent2);
        });

        MaterialCardView cardConfirmDelivery = findViewById(R.id.cardConfirmDelivery);

        cardConfirmDelivery.setOnClickListener(v -> {
            Intent intent = new Intent(HospitalDashboard.this, ConfirmDeliveriesActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardActiveStatus).setOnClickListener(v -> {
            startActivity(new Intent(HospitalDashboard.this, ActiveRequestsActivity.class));
        });

        findViewById(R.id.cardHistory).setOnClickListener(v -> { // ඔයාගේ History කාඩ් එකේ ID එක දෙන්න
            startActivity(new Intent(HospitalDashboard.this, HistoryActivity.class));
        });
    }
}