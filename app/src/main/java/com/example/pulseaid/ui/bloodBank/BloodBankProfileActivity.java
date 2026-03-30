package com.example.pulseaid.ui.bloodBank;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.bloodBank.BloodBankProfileViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BloodBankProfileActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextInputEditText etContactNo, etAddress, etOpenTime, etCloseTime, etDailyLimit;
    private MaterialButton btnSaveProfile, btnGetCurrentLocation;
    private ImageView btnBack;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private Double locationLat = null;
    private Double locationLng = null;

    // ViewModel instance
    private BloodBankProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_bank_profile);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(BloodBankProfileViewModel.class);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etContactNo = findViewById(R.id.etContactNo);
        etAddress = findViewById(R.id.etAddress);
        etOpenTime = findViewById(R.id.etOpenTime);
        etCloseTime = findViewById(R.id.etCloseTime);
        etDailyLimit = findViewById(R.id.etDailyLimit);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnGetCurrentLocation = findViewById(R.id.btnGetCurrentLocation);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        etOpenTime.setOnClickListener(v -> showTimePicker(etOpenTime));
        etCloseTime.setOnClickListener(v -> showTimePicker(etCloseTime));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnGetCurrentLocation.setOnClickListener(v -> fetchCurrentLocation());

        btnSaveProfile.setOnClickListener(v -> saveProfileData());

        // Observe ViewModel LiveData
        setupObservers();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                btnSaveProfile.setText("SAVING...");
                btnSaveProfile.setEnabled(false);
            } else {
                btnSaveProfile.setText("SAVE / UPDATE PROFILE");
                btnSaveProfile.setEnabled(true);
            }
        });

        viewModel.getUpdateSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BloodBankProfileActivity.this, BloodBankDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getUpdateError().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showTimePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String amPm = hourOfDay >= 12 ? "PM" : "AM";
            int hr = hourOfDay % 12;
            hr = hr == 0 ? 12 : hr;
            String time = String.format(Locale.getDefault(), "%02d:%02d %s", hr, minuteOfHour, amPm);
            editText.setText(time);
        }, hour, minute, false);
        timePickerDialog.show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng defaultLocation = new LatLng(7.8731, 80.7718);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 7f));

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Blood Bank Location"));
            locationLat = latLng.latitude;
            locationLng = latLng.longitude;
        });
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && mMap != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                locationLat = location.getLatitude();
                locationLng = location.getLongitude();
            } else {
                Toast.makeText(BloodBankProfileActivity.this, "Unable to find location. Please ensure GPS is enabled.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfileData() {
        String contactNo = etContactNo.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String openTime = etOpenTime.getText().toString().trim();
        String closeTime = etCloseTime.getText().toString().trim();
        String dailyLimitStr = etDailyLimit.getText().toString().trim();

        if (contactNo.isEmpty() || address.isEmpty() || openTime.isEmpty() || closeTime.isEmpty() || dailyLimitStr.isEmpty() || locationLat == null) {
            Toast.makeText(this, "Please fill all fields and select a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        int dailyLimit = Integer.parseInt(dailyLimitStr);

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("contactNo", contactNo);
        profileData.put("address", address);
        profileData.put("locationLat", locationLat);
        profileData.put("locationLng", locationLng);
        profileData.put("openTime", openTime);
        profileData.put("closeTime", closeTime);
        profileData.put("dailyLimit", dailyLimit);

        // Pass data to ViewModel instead of accessing Firebase directly
        viewModel.updateProfile(profileData);
    }
}