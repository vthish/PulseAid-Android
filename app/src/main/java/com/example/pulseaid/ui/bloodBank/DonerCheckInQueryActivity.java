package com.example.pulseaid.ui.bloodBank;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.bloodBank.DonorCheckInViewModel;
import com.google.android.material.card.MaterialCardView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class DonerCheckInQueryActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScannerView;
    private Button btnVerify;
    private MaterialCardView btnBack;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private String scannedDonorId = null;
    private DonorCheckInViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doner_check_in_query);

        barcodeScannerView = findViewById(R.id.barcode_scanner);
        barcodeScannerView.getStatusView().setVisibility(View.GONE);

        btnVerify = findViewById(R.id.btnVerify90Days);
        btnBack = findViewById(R.id.btnBack);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DonorCheckInViewModel.class);
        observeViewModel();

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(DonerCheckInQueryActivity.this, BloodBankDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startScanner();
        }

        btnVerify.setOnClickListener(v -> {
            if (scannedDonorId != null) {
                viewModel.verifyDonor(scannedDonorId);
            } else {
                Toast.makeText(this, "Please align a QR code in the frame first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                btnVerify.setText("VERIFYING...");
                btnVerify.setEnabled(false);
            } else {
                btnVerify.setText("VERIFY 90 DAYS ELIGIBILITY");
                btnVerify.setEnabled(true);
            }
        });

        viewModel.getVerificationMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startScanner() {
        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    scannedDonorId = result.getText();
                    // Optional: Visual feedback that QR was captured
                    Toast.makeText(DonerCheckInQueryActivity.this, "QR Captured! Tap Verify.", Toast.LENGTH_SHORT).show();
                    // Pause scanner to prevent multiple rapid scans
                    barcodeScannerView.pause();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            barcodeScannerView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
                barcodeScannerView.resume();
            } else {
                Toast.makeText(this, "Camera permission is permanently denied or required!", Toast.LENGTH_LONG).show();
            }
        }
    }
}