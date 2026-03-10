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
import com.example.pulseaid.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doner_check_in_query);

        barcodeScannerView = findViewById(R.id.barcode_scanner);
        barcodeScannerView.getStatusView().setVisibility(View.GONE);

        btnVerify = findViewById(R.id.btnVerify90Days);
        btnBack = findViewById(R.id.btnBack);

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
                Toast.makeText(this, "Verifying Donor ID: " + scannedDonorId, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Please align a QR code in the frame first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startScanner() {
        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    scannedDonorId = result.getText();
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