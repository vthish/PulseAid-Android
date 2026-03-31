package com.example.pulseaid.ui.bloodBank;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.DocumentSnapshot;
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
                    Toast.makeText(this, "Session Error: Re-login required.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please scan a QR code first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                btnVerify.setText("WAITING...");
                btnVerify.setEnabled(false);
            } else {
                btnVerify.setText("VERIFY APPOINTMENT");
                btnVerify.setEnabled(true);
            }
        });

        viewModel.getAppointmentData().observe(this, appointment -> {
            DocumentSnapshot donor = viewModel.getDonorData().getValue();
            if (appointment != null && donor != null) {
                showStatusUpdateDialog(appointment, donor);
            }
        });

        viewModel.getTransactionSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(this, "Donation Successfully Recorded!", Toast.LENGTH_SHORT).show();
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

    private void showStatusUpdateDialog(DocumentSnapshot appointment, DocumentSnapshot donor) {
        String donorName = donor.getString("fullName");
        String bloodType = donor.getString("bloodGroup");
        String appointmentId = appointment.getId();
        String donorUid = donor.getId();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Donor Identified!");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText etUnits = new EditText(this);
        etUnits.setHint("Blood Units (e.g., 1)");
        etUnits.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etUnits);

        builder.setMessage("Name: " + donorName + "\nBlood Type: " + bloodType);
        builder.setView(layout);

        builder.setPositiveButton("MARK AS COMPLETED", null);
        builder.setNegativeButton("CLOSE", (dialog, which) -> {
            scannedAppointmentId = null;
            barcodeScannerView.resume();
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String unitsStr = etUnits.getText().toString().trim();
                if (unitsStr.isEmpty()) {
                    etUnits.setError("Required");
                } else {
                    int units = Integer.parseInt(unitsStr);
                    viewModel.completeDonation(appointmentId, donorUid, currentBloodBankCenterId, bloodType, units);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void startScanner() {
        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    scannedAppointmentId = result.getText().trim();
                    Toast.makeText(DonerCheckInQueryActivity.this, "QR Captured", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_LONG).show();
            }
        }
    }
}