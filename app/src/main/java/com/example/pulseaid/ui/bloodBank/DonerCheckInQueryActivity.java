package com.example.pulseaid.ui.bloodBank;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.bloodBank.DonorCheckinViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class DonerCheckInQueryActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScannerView;
    private Button btnVerify;
    private MaterialCardView btnBack;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private String scannedAppointmentId = null;
    private String currentBloodBankCenterId;
    private DonorCheckinViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doner_check_in_query);

        barcodeScannerView = findViewById(R.id.barcode_scanner);
        barcodeScannerView.getStatusView().setVisibility(View.GONE);

        btnVerify = findViewById(R.id.btnVerify90Days);
        btnVerify.setText("VERIFY APPOINTMENT");
        btnBack = findViewById(R.id.btnBack);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentBloodBankCenterId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        viewModel = new ViewModelProvider(this).get(DonorCheckinViewModel.class);
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
            if (scannedAppointmentId != null) {
                if (currentBloodBankCenterId != null) {
                    viewModel.verifyAppointmentQR(scannedAppointmentId, currentBloodBankCenterId);
                } else {
                    Toast.makeText(this, "Error: Blood Bank not logged in properly.", Toast.LENGTH_SHORT).show();
                }
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
                btnVerify.setText("VERIFY APPOINTMENT");
                btnVerify.setEnabled(true);
            }
        });

        viewModel.getValidAppointment().observe(this, document -> {
            if (document != null) {
                showStatusUpdateDialog(document.getId());
            }
        });

        viewModel.getUpdateSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(this, "Appointment Status Updated Successfully!", Toast.LENGTH_SHORT).show();
                scannedAppointmentId = null;
                barcodeScannerView.resume();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                scannedAppointmentId = null;
                barcodeScannerView.resume();
            }
        });
    }

    private void showStatusUpdateDialog(String appointmentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Appointment Status");
        builder.setMessage("This QR is valid for your center. What is the status of this appointment?");

        builder.setPositiveButton("COMPLETED", (dialog, which) -> {
            viewModel.updateStatus(appointmentId, "COMPLETED");
        });

        builder.setNegativeButton("REJECTED", (dialog, which) -> {
            viewModel.updateStatus(appointmentId, "REJECTED");
        });

        builder.setNeutralButton("CANCEL", (dialog, which) -> {
            scannedAppointmentId = null;
            barcodeScannerView.resume();
            dialog.dismiss();
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void startScanner() {
        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    scannedAppointmentId = result.getText().trim();


                    Toast.makeText(DonerCheckInQueryActivity.this, "Scanned Data: " + scannedAppointmentId, Toast.LENGTH_LONG).show();

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
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_LONG).show();
            }
        }
    }
}