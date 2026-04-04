package com.example.pulseaid.ui.hospital;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pulseaid.R;
import com.google.android.material.card.MaterialCardView;

public class HospitalDashboard extends AppCompatActivity {

    private MaterialCardView cardRequestBlood;
    private MaterialCardView cardConfirmDelivery;
    private View cardActiveStatus;
    private View cardHistory;
    private View btnProfile;
    private View headerSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hospital_dashboard);

        setupStatusBar();
        initViews();
        setupInsets();
        setupClickListeners();
    }

    private void setupStatusBar() {
        Window window = getWindow();
        if (window != null) {
            window.setStatusBarColor(getColorCompat(R.color.pulse_red_dark));
            WindowCompat.getInsetsController(window, window.getDecorView())
                    .setAppearanceLightStatusBars(false);
        }
    }

    private void initViews() {
        headerSection = findViewById(R.id.headerSection);
        cardRequestBlood = findViewById(R.id.cardHeroRequest);
        cardConfirmDelivery = findViewById(R.id.cardConfirmDelivery);
        cardActiveStatus = findViewById(R.id.cardActiveStatus);
        cardHistory = findViewById(R.id.cardHistory);
        btnProfile = findViewById(R.id.btnProfile);
    }

    private void setupInsets() {
        if (headerSection != null) {
            final int paddingStart = headerSection.getPaddingStart();
            final int paddingTop = headerSection.getPaddingTop();
            final int paddingEnd = headerSection.getPaddingEnd();
            final int paddingBottom = headerSection.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(headerSection, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.setPadding(
                        paddingStart,
                        paddingTop + systemBars.top,
                        paddingEnd,
                        paddingBottom
                );
                return insets;
            });
        }
    }

    private void setupClickListeners() {
        if (cardRequestBlood != null) {
            cardRequestBlood.setOnClickListener(v ->
                    startActivity(new Intent(HospitalDashboard.this, RequestFormActivity.class)));
        }

        if (cardConfirmDelivery != null) {
            cardConfirmDelivery.setOnClickListener(v ->
                    startActivity(new Intent(HospitalDashboard.this, ConfirmDeliveriesActivity.class)));
        }

        if (cardActiveStatus != null) {
            cardActiveStatus.setOnClickListener(v ->
                    startActivity(new Intent(HospitalDashboard.this, ActiveRequestsActivity.class)));
        }

        if (cardHistory != null) {
            cardHistory.setOnClickListener(v ->
                    startActivity(new Intent(HospitalDashboard.this, HospitalHistoryActivity.class)));
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v ->
                    startActivity(new Intent(HospitalDashboard.this, HospitalProfileActivity.class)));
        }
    }

    private int getColorCompat(int colorResId) {
        return androidx.core.content.ContextCompat.getColor(this, colorResId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        clearClickListener(cardRequestBlood);
        clearClickListener(cardConfirmDelivery);
        clearClickListener(cardActiveStatus);
        clearClickListener(cardHistory);
        clearClickListener(btnProfile);

        headerSection = null;
        cardRequestBlood = null;
        cardConfirmDelivery = null;
        cardActiveStatus = null;
        cardHistory = null;
        btnProfile = null;
    }

    private void clearClickListener(View view) {
        if (view != null) {
            view.setOnClickListener(null);
        }
    }
}