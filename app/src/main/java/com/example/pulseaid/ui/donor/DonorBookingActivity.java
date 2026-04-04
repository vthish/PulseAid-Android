package com.example.pulseaid.ui.donor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.donor.DonorBookingViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DonorBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Spinner spinnerDistrict, spinnerCenter;
    private TextInputEditText etDate;
    private ChipGroup chipGroupTime;
    private MaterialButton btnCheckAvailability, btnConfirmBooking;
    private MaterialCardView cardSummary;
    private GoogleMap mMap;
    private DonorBookingViewModel viewModel;
    private TextView tvSumName, tvSumBlood, tvSumNic, tvSumCenter, tvSumAddress, tvSumPhone, tvSumDateTime, tvSumQueue, tvSumAppId;

    private String preselectedCenterId;
    private String preselectedDistrict;
    private boolean districtAutoSelected = false;
    private boolean centerAutoSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_booking);

        initViews();
        setupViewModel();
        readIntentExtras();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel.checkDonorEligibility(uid);
        viewModel.fetchDonorProfile(uid);

        setupDistrictSpinner();
        setupDatePicker();
        setupObservers();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnCheckAvailability.setOnClickListener(v -> validateAndRequestCheck());
        btnConfirmBooking.setOnClickListener(v -> processConfirmation());
    }

    private void readIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            preselectedCenterId = intent.getStringExtra("preselected_center_id");
            preselectedDistrict = intent.getStringExtra("preselected_district");
        }
    }

    private void setupObservers() {
        viewModel.getIsEligible().observe(this, eligible -> {
            if (!eligible) {
                String reason = viewModel.getLockReason().getValue();
                showLockDialog(reason);
            }
        });

        viewModel.getDonorProfile().observe(this, donor -> {
            if (donor != null && donor.exists()) {
                tvSumName.setText(donor.getString("name"));
                tvSumBlood.setText("Group: " + donor.getString("bloodGroup"));
                tvSumNic.setText("NIC: " + donor.getString("nic"));
            }
        });

        viewModel.getCenters().observe(this, centers -> {
            List<String> names = new ArrayList<>();
            names.add("Select Center");

            if (centers != null && !centers.isEmpty()) {
                for (DocumentSnapshot doc : centers) {
                    String name = doc.getString("name");
                    if (name != null) {
                        names.add(name);
                    }
                }
            } else if (mMap != null) {
                mMap.clear();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCenter.setAdapter(adapter);

            autoSelectCenterIfNeeded(centers);
        });

        viewModel.getSelectedCenter().observe(this, center -> {
            if (center != null && mMap != null) {
                Double lat = center.getDouble("locationLat");
                Double lng = center.getDouble("locationLng");

                if (lat != null && lng != null) {
                    LatLng loc = new LatLng(lat, lng);
                    mMap.clear();
                    Marker marker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(loc)
                                    .title(center.getString("name"))
                                    .snippet("Tel: " + center.getString("contactNo"))
                    );
                    if (marker != null) {
                        marker.showInfoWindow();
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f));
                }
            }
        });

        viewModel.getNextQueueNumber().observe(this, number -> {
            if (number != null && number != -1) {
                updateSummaryUI();
                tvSumQueue.setText(String.format("%02d", number));
                tvSumAppId.setText("#PENDING");
                cardSummary.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getBookingStatus().observe(this, status -> {
            if ("FULL".equals(status)) {
                cardSummary.setVisibility(View.GONE);
                Toast.makeText(this, "Sorry, this slot is full!", Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getConfirmedAppointmentId().observe(this, id -> {
            if (id != null && !id.trim().isEmpty()) {
                saveAppointmentLocally(id);
                tvSumAppId.setText("#" + id.substring(0, Math.min(8, id.length())).toUpperCase());

                Bitmap qr = generateQRCode(id);
                if (qr != null) {
                    showQRDialog(qr, id);
                } else {
                    Toast.makeText(this, "QR generation failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void autoSelectDistrictIfNeeded() {
        if (districtAutoSelected || TextUtils.isEmpty(preselectedDistrict) || spinnerDistrict.getAdapter() == null) {
            return;
        }

        for (int i = 0; i < spinnerDistrict.getAdapter().getCount(); i++) {
            Object item = spinnerDistrict.getAdapter().getItem(i);
            if (item != null && preselectedDistrict.equalsIgnoreCase(item.toString().trim())) {
                districtAutoSelected = true;
                spinnerDistrict.setSelection(i);
                break;
            }
        }
    }

    private void autoSelectCenterIfNeeded(List<DocumentSnapshot> centers) {
        if (centerAutoSelected || TextUtils.isEmpty(preselectedCenterId) || centers == null || centers.isEmpty()) {
            return;
        }

        for (int i = 0; i < centers.size(); i++) {
            DocumentSnapshot doc = centers.get(i);
            if (doc != null && preselectedCenterId.equals(doc.getId())) {
                centerAutoSelected = true;
                spinnerCenter.setSelection(i + 1);
                break;
            }
        }
    }

    private void showLockDialog(String reason) {
        String message = "You have recently donated blood. Please wait 90 days before your next donation.";

        if ("ACTIVE_EXISTS".equals(reason)) {
            message = "You already have a upcoming appointment. Please complete or cancel it before booking again.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Booking Locked")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private void saveAppointmentLocally(String id) {
        SharedPreferences prefs = getSharedPreferences("PulseAidPrefs", MODE_PRIVATE);
        prefs.edit().putString("last_appointment_id", id).apply();
    }

    private void initViews() {
        spinnerDistrict = findViewById(R.id.spinner_district);
        spinnerCenter = findViewById(R.id.spinner_center);
        etDate = findViewById(R.id.et_booking_date);
        chipGroupTime = findViewById(R.id.chip_group_time);
        btnCheckAvailability = findViewById(R.id.btn_check_availability);
        btnConfirmBooking = findViewById(R.id.btn_confirm_booking);
        cardSummary = findViewById(R.id.card_booking_summary);
        tvSumName = findViewById(R.id.tv_summary_name);
        tvSumBlood = findViewById(R.id.tv_summary_blood);
        tvSumNic = findViewById(R.id.tv_summary_nic);
        tvSumCenter = findViewById(R.id.tv_summary_center);
        tvSumAddress = findViewById(R.id.tv_summary_address);
        tvSumPhone = findViewById(R.id.tv_summary_phone);
        tvSumDateTime = findViewById(R.id.tv_summary_datetime);
        tvSumQueue = findViewById(R.id.tv_summary_queue);
        tvSumAppId = findViewById(R.id.tv_summary_app_id);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DonorBookingViewModel.class);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> etDate.setText(year + "-" + (month + 1) + "-" + day),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() + 86400000);
            datePicker.show();
        });
    }

    private void validateAndRequestCheck() {
        if (spinnerDistrict.getSelectedItemPosition() == 0 ||
                spinnerCenter.getSelectedItemPosition() == 0 ||
                etDate.getText() == null ||
                etDate.getText().toString().trim().isEmpty() ||
                chipGroupTime.getCheckedChipId() == View.NO_ID) {
            Toast.makeText(this, "Please fill all steps correctly!", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentSnapshot center = viewModel.getSelectedCenter().getValue();
        Chip selectedChip = findViewById(chipGroupTime.getCheckedChipId());

        if (center != null && selectedChip != null) {
            viewModel.checkAvailability(
                    center.getId(),
                    etDate.getText().toString().trim(),
                    selectedChip.getText().toString()
            );
        }
    }

    private void updateSummaryUI() {
        DocumentSnapshot center = viewModel.getSelectedCenter().getValue();
        Chip selectedChip = findViewById(chipGroupTime.getCheckedChipId());

        if (center != null && selectedChip != null) {
            tvSumCenter.setText(center.getString("name"));
            tvSumAddress.setText(center.getString("address"));
            tvSumPhone.setText("Phone: " + center.getString("contactNo"));
            tvSumDateTime.setText(etDate.getText().toString().trim() + " | " + selectedChip.getText().toString());
        }
    }

    private void processConfirmation() {
        DocumentSnapshot center = viewModel.getSelectedCenter().getValue();
        Chip selectedChip = findViewById(chipGroupTime.getCheckedChipId());
        Integer qNo = viewModel.getNextQueueNumber().getValue();

        if (center != null && selectedChip != null && qNo != null && qNo != -1) {
            viewModel.confirmBooking(
                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    center.getId(),
                    etDate.getText().toString().trim(),
                    selectedChip.getText().toString(),
                    qNo
            );
        }
    }

    private void setupDistrictSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sri_lanka_districts,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(adapter);

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.onDistrictSelected(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerCenter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    viewModel.onCenterSelected(position - 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        autoSelectDistrictIfNeeded();
    }

    private Bitmap generateQRCode(String appointmentId) {
        try {
            String qrContent = appointmentId.trim();
            return new BarcodeEncoder().encodeBitmap(qrContent, BarcodeFormat.QR_CODE, 500, 500);
        } catch (Exception e) {
            return null;
        }
    }

    private void showQRDialog(Bitmap bitmap, String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Appointment Pending");
        builder.setMessage("Your Booking ID: #" + id.substring(0, Math.min(8, id.length())).toUpperCase());
        builder.setCancelable(false);

        ImageView iv = new ImageView(this);
        iv.setPadding(40, 40, 40, 40);
        iv.setImageBitmap(bitmap);
        builder.setView(iv);

        builder.setPositiveButton("DONE", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });

        builder.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}