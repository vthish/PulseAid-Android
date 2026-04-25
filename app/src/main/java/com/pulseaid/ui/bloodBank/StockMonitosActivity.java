package com.pulseaid.ui.bloodBank;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.pulseaid.R;
import com.pulseaid.viewmodel.bloodBank.StockMonitorViewModel;
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

        viewModel = new ViewModelProvider(this).get(StockMonitorViewModel.class);
        observeViewModel();
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

        viewModel.getStockData().observe(this, stockData -> {
            if (stockData != null) {
                updateStockUI(txtCountAPos, stockData.get("A+"));
                updateStockUI(txtCountANeg, stockData.get("A-"));
                updateStockUI(txtCountBPos, stockData.get("B+"));
                updateStockUI(txtCountBNeg, stockData.get("B-"));
                updateStockUI(txtCountABPos, stockData.get("AB+"));
                updateStockUI(txtCountABNeg, stockData.get("AB-"));
                updateStockUI(txtCountOPos, stockData.get("O+"));
                updateStockUI(txtCountONeg, stockData.get("O-"));
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // අලුත් Color Logic එක මෙතන තියෙනවා
    private void updateStockUI(TextView textView, String countStr) {
        if (countStr == null) countStr = "0";

        textView.setText(countStr + " Units");

        try {
            int count = Integer.parseInt(countStr.trim());

            if (count <= 5) {
                // 5 හෝ ඊට අඩු නම්: රතු (Critical)
                textView.setTextColor(Color.parseColor("#D32F2F"));
            } else if (count <= 10) {
                // 6 ත් 10 ත් අතර නම්: තැඹිලි (Warning)
                textView.setTextColor(Color.parseColor("#F57C00"));
            } else {
                // 10 ට වැඩි නම්: කොළ (Good Status)
                textView.setTextColor(Color.parseColor("#388E3C"));
            }

        } catch (NumberFormatException e) {
            textView.setTextColor(Color.parseColor("#D32F2F")); // Error එකක් ආවොත් රතු පාටින් පෙන්වන්න
        }
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