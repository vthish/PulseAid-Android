package com.example.pulseaid.ui.bloodBank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.bloodBank.StockMonitorViewModel;
import com.google.android.material.card.MaterialCardView;

public class StockMonitosActivity extends AppCompatActivity {

    private CardView cardAPos, cardANeg, cardBPos, cardBNeg, cardABPos, cardABNeg, cardOPos, cardONeg;
    private TextView txtCountAPos, txtCountANeg, txtCountBPos, txtCountBNeg, txtCountABPos, txtCountABNeg, txtCountOPos, txtCountONeg;
    private MaterialCardView btnBack;

    private StockMonitorViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_monitos);

        initializeViews();
        setupClickListeners();
        setupBack();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(StockMonitorViewModel.class);

        // Observe Data from ViewModel
        observeViewModel();
    }

    private void initializeViews() {
        // Initialize Cards
        cardAPos = findViewById(R.id.cardAPositive);
        cardANeg = findViewById(R.id.cardANegative);
        cardBPos = findViewById(R.id.cardBPositive);
        cardBNeg = findViewById(R.id.cardBNegative);
        cardABPos = findViewById(R.id.cardABPositive);
        cardABNeg = findViewById(R.id.cardABNegative);
        cardOPos = findViewById(R.id.cardOPositive);
        cardONeg = findViewById(R.id.cardONegative);

        // Initialize TextViews for counts
        txtCountAPos = findViewById(R.id.txtCountAPositive);
        txtCountANeg = findViewById(R.id.txtCountANegative);
        txtCountBPos = findViewById(R.id.txtCountBPositive);
        txtCountBNeg = findViewById(R.id.txtCountBNegative);
        txtCountABPos = findViewById(R.id.txtCountABPositive);
        txtCountABNeg = findViewById(R.id.txtCountABNegative);
        txtCountOPos = findViewById(R.id.txtCountOPositive);
        txtCountONeg = findViewById(R.id.txtCountONegative);

        btnBack = findViewById(R.id.btnBack);
    }

    private void observeViewModel() {
        // Show loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                String loadingText = "...";
                txtCountAPos.setText(loadingText);
                txtCountANeg.setText(loadingText);
                txtCountBPos.setText(loadingText);
                txtCountBNeg.setText(loadingText);
                txtCountABPos.setText(loadingText);
                txtCountABNeg.setText(loadingText);
                txtCountOPos.setText(loadingText);
                txtCountONeg.setText(loadingText);
            }
        });

        // Update UI with Firebase data
        viewModel.getStockData().observe(this, stockData -> {
            if (stockData != null) {
                txtCountAPos.setText(stockData.get("A+") + " Units");
                txtCountANeg.setText(stockData.get("A-") + " Units");
                txtCountBPos.setText(stockData.get("B+") + " Units");
                txtCountBNeg.setText(stockData.get("B-") + " Units");
                txtCountABPos.setText(stockData.get("AB+") + " Units");
                txtCountABNeg.setText(stockData.get("AB-") + " Units");
                txtCountOPos.setText(stockData.get("O+") + " Units");
                txtCountONeg.setText(stockData.get("O-") + " Units");
            }
        });

        // Show error message if fetching fails
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
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