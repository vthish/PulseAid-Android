package com.example.pulseaid.ui.bloodBank;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.pulseaid.R;
import com.google.android.material.card.MaterialCardView;

public class StockMonitosActivity extends AppCompatActivity {

    private CardView cardAPos, cardANeg, cardBPos, cardBNeg, cardABPos, cardABNeg, cardOPos, cardONeg;
    private MaterialCardView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_monitos);

        initializeViews();
        setupClickListeners();
        setupBack();
    }

    private void initializeViews() {
        cardAPos = findViewById(R.id.cardAPositive);
        cardANeg = findViewById(R.id.cardANegative);
        cardBPos = findViewById(R.id.cardBPositive);
        cardBNeg = findViewById(R.id.cardBNegative);
        cardABPos = findViewById(R.id.cardABPositive);
        cardABNeg = findViewById(R.id.cardABNegative);
        cardOPos = findViewById(R.id.cardOPositive);
        cardONeg = findViewById(R.id.cardONegative);

        btnBack = findViewById(R.id.btnBack);
    }

    private void setupBack() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupClickListeners() {
        cardAPos.setOnClickListener(v -> navigateToDetail("A+"));
        cardANeg.setOnClickListener(v -> navigateToDetail("A-"));
        cardBPos.setOnClickListener(v -> navigateToDetail("B+"));
        cardBNeg.setOnClickListener(v -> navigateToDetail("B-"));
        cardABPos.setOnClickListener(v -> navigateToDetail("AB+"));
        cardABNeg.setOnClickListener(v -> navigateToDetail("AB-"));
        cardOPos.setOnClickListener(v -> navigateToDetail("O+"));
        cardONeg.setOnClickListener(v -> navigateToDetail("O-"));
    }

    private void navigateToDetail(String bloodGroup) {
        Intent intent = new Intent(StockMonitosActivity.this, BloodStockDetailsActivity.class);
        intent.putExtra("BLOOD_GROUP", bloodGroup);
        startActivity(intent);
    }
}