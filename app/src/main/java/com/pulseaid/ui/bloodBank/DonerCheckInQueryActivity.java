package com.pulseaid.ui.bloodBank;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.pulseaid.R;
import com.pulseaid.viewmodel.bloodBank.DonorCheckinViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class DonerCheckInQueryActivity extends AppCompatActivity {
    private DecoratedBarcodeView barcodeScannerView;
    private String scannedId = null;
    private DonorCheckinViewModel viewModel;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doner_check_in_query);

        barcodeScannerView = findViewById(R.id.barcode_scanner);
        barcodeScannerView.setStatusText("");
        Button btnVerify = findViewById(R.id.btnVerify90Days);
        MaterialCardView btnBack = findViewById(R.id.btnBack);

        viewModel = new ViewModelProvider(this).get(DonorCheckinViewModel.class);

        viewModel.getAppointmentData().observe(this, data -> { if(data != null) checkAndNavigate(); });
        viewModel.getDonorData().observe(this, data -> { if(data != null) checkAndNavigate(); });

        viewModel.getErrorMessage().observe(this, err -> {
            if (err != null && !err.isEmpty()) {
                showErrorDialog(err, scannedId);
            }
        });

        btnBack.setOnClickListener(v -> finish());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else { startScanner(); }

        btnVerify.setOnClickListener(v -> Toast.makeText(this, "Point camera at QR code", Toast.LENGTH_SHORT).show());
    }

    private void checkAndNavigate() {
        try {
            DocumentSnapshot app = viewModel.getAppointmentData().getValue();
            DocumentSnapshot donor = viewModel.getDonorData().getValue();

            if (app != null && donor != null && isNavigating) {
                Intent intent = new Intent(this, AppointmentDetailsActivity.class);
                intent.putExtra("appId", app.getId());
                intent.putExtra("donorId", donor.getId());
                intent.putExtra("name", donor.getString("name") != null ? donor.getString("name") : "Unknown");
                intent.putExtra("type", donor.getString("bloodGroup") != null ? donor.getString("bloodGroup") : "N/A");
                intent.putExtra("date", app.getString("date") != null ? app.getString("date") : "N/A");
                intent.putExtra("time", app.getString("timeSlot") != null ? app.getString("timeSlot") : "N/A");
                intent.putExtra("status", app.getString("status") != null ? app.getString("status") : "N/A");

                String uid = FirebaseAuth.getInstance().getUid();
                if (uid != null) {
                    intent.putExtra("bankId", uid);
                }

                String qNo = app.contains("queueNo") && app.get("queueNo") != null ? String.valueOf(app.get("queueNo")) : "0";
                intent.putExtra("queueNo", qNo);

                viewModel.resetNavigationData();
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            showErrorDialog("Navigation Error: " + e.getMessage(), scannedId);
        }
    }

    private void startScanner() {
        barcodeScannerView.decodeContinuous(result -> {
            try {
                if (result.getText() != null && !isNavigating) {
                    isNavigating = true;
                    scannedId = result.getText().trim();
                    barcodeScannerView.pause();

                    if (scannedId.contains("/") || scannedId.contains("http") || scannedId.length() < 5) {
                        showErrorDialog("Invalid QR Code format! Looks like a link or is too short.", scannedId);
                        return;
                    }

                    // Get the logged-in center's UID
                    String uid = FirebaseAuth.getInstance().getUid();
                    if (uid == null) {
                        showErrorDialog("Authentication Error: Center not logged in.", scannedId);
                        return;
                    }

                    Toast.makeText(this, "Checking Data...", Toast.LENGTH_SHORT).show();
                    viewModel.verifyAppointmentQR(scannedId, uid);
                }
            } catch (Exception e) {
                showErrorDialog("Scan Error: " + e.getMessage(), scannedId);
            }
        });
    }

    private void showErrorDialog(String errorMsg, String id) {
        new AlertDialog.Builder(this)
                .setTitle("QR Verification Failed")
                .setMessage("Scanned QR Text:\n" + id + "\n\nReason:\n" + errorMsg)
                .setPositiveButton("Try Again", (dialog, which) -> {
                    viewModel.clearErrorMessage();
                    resetScanner();
                })
                .setCancelable(false)
                .show();
    }

    private void resetScanner() {
        scannedId = null;
        isNavigating = false;
        barcodeScannerView.resume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        }
    }

    @Override protected void onResume() { super.onResume(); resetScanner(); }
    @Override protected void onPause() { super.onPause(); barcodeScannerView.pause(); }
}