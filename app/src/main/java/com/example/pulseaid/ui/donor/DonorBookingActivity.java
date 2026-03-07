package com.example.pulseaid.ui.donor;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
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

    private TextView tvSumName, tvSumBlood, tvSumNic, tvSumCenter, tvSumAddress, tvSumPhone, tvSumDateTime, tvSumQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_booking);

        initViews();
        setupViewModel();
        setupDistrictSpinner();
        setupDatePicker();
        setupObservers();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnCheckAvailability.setOnClickListener(v -> validateAndShowSummary());

        btnConfirmBooking.setOnClickListener(v -> {
            String appointmentId = "PA-" + System.currentTimeMillis();
            Bitmap qrBitmap = generateQRCode(appointmentId);
            if (qrBitmap != null) {
                showQRDialog(qrBitmap, appointmentId);
            }
        });
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
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DonorBookingViewModel.class);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {
                etDate.setText(year + "-" + (month + 1) + "-" + day);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() + 86400000);
            datePicker.show();
        });
    }

    private void setupObservers() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            viewModel.fetchDonorProfile(uid);
        }

        viewModel.getDonorProfile().observe(this, donor -> {
            if (donor != null && donor.exists()) {
                tvSumName.setText(donor.getString("name"));
                tvSumBlood.setText("Group: " + donor.getString("bloodGroup"));
                tvSumNic.setText("NIC: " + donor.getString("nic"));
            }
        });

        viewModel.getCenters().observe(this, centers -> {
            List<String> names = new ArrayList<>();
            for (DocumentSnapshot doc : centers) {
                names.add(doc.getString("name"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCenter.setAdapter(adapter);
        });

        viewModel.getSelectedCenter().observe(this, center -> {
            if (center != null && mMap != null) {
                Double lat = center.getDouble("latitude");
                Double lng = center.getDouble("longitude");
                if (lat != null && lng != null) {
                    LatLng loc = new LatLng(lat, lng);
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(loc).title(center.getString("name")));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f));
                }
            }
        });
    }

    private void validateAndShowSummary() {
        if (spinnerDistrict.getSelectedItemPosition() == 0 || spinnerCenter.getSelectedItem() == null ||
                etDate.getText().toString().isEmpty() || chipGroupTime.getCheckedChipId() == View.NO_ID) {
            Toast.makeText(this, "Please fill all steps!", Toast.LENGTH_SHORT).show();
            return;
        }

        updateSummaryUI();
        cardSummary.setVisibility(View.VISIBLE);
    }

    private void updateSummaryUI() {
        DocumentSnapshot center = viewModel.getSelectedCenter().getValue();
        if (center != null) {
            tvSumCenter.setText(center.getString("name"));
            tvSumAddress.setText(center.getString("address"));
            tvSumPhone.setText("Phone: " + center.get("phone"));
        }

        String dateStr = etDate.getText().toString();
        int checkedId = chipGroupTime.getCheckedChipId();
        Chip selectedChip = findViewById(checkedId);
        if (selectedChip != null) {
            tvSumDateTime.setText(dateStr + " | " + selectedChip.getText());
        }

        tvSumQueue.setText("05");
    }

    private void setupDistrictSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sri_lanka_districts, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(adapter);

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.onDistrictSelected(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCenter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.onCenterSelected(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private Bitmap generateQRCode(String text) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 500, 500);
        } catch (Exception e) {
            return null;
        }
    }

    private void showQRDialog(Bitmap bitmap, String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Appointment Confirmed");
        builder.setMessage("ID: " + id);
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        builder.setView(imageView);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (viewModel.getSelectedCenter().getValue() != null) {
            viewModel.onCenterSelected(spinnerCenter.getSelectedItemPosition());
        }
    }
}