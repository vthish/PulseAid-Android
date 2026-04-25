package com.pulseaid.ui.bloodBank;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.pulseaid.R;
import com.pulseaid.viewmodel.bloodBank.BloodBankProfileViewModel;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BloodBankProfileActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextInputEditText etContactNo, etAddress, etOpenTime, etCloseTime, etDailyLimit;
    private AutoCompleteTextView etDistrict;
    private MaterialButton btnSaveProfile, btnGetCurrentLocation;
    private ImageView btnBack;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private Double locationLat = null;
    private Double locationLng = null;

    private BloodBankProfileViewModel viewModel;

    private Map<String, LatLng> districtLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_bank_profile);

        viewModel = new ViewModelProvider(this).get(BloodBankProfileViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etContactNo = findViewById(R.id.etContactNo);
        etAddress = findViewById(R.id.etAddress);
        etOpenTime = findViewById(R.id.etOpenTime);
        etCloseTime = findViewById(R.id.etCloseTime);
        etDistrict = findViewById(R.id.etDistrict);
        etDailyLimit = findViewById(R.id.etDailyLimit);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnGetCurrentLocation = findViewById(R.id.btnGetCurrentLocation);
        btnBack = findViewById(R.id.btnBack);

        setupDistrictData();

        btnBack.setOnClickListener(v -> finish());
        etOpenTime.setOnClickListener(v -> showTimePicker(etOpenTime));
        etCloseTime.setOnClickListener(v -> showTimePicker(etCloseTime));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnGetCurrentLocation.setOnClickListener(v -> fetchCurrentLocation());
        btnSaveProfile.setOnClickListener(v -> saveProfileData());

        setupObservers();
    }

    private void setupDistrictData() {
        districtLocations = new HashMap<>();
        districtLocations.put("Ampara", new LatLng(7.2964, 81.6747));
        districtLocations.put("Anuradhapura", new LatLng(8.3114, 80.4037));
        districtLocations.put("Badulla", new LatLng(6.9934, 81.0550));
        districtLocations.put("Batticaloa", new LatLng(7.7102, 81.6924));
        districtLocations.put("Colombo", new LatLng(6.9271, 79.8612));
        districtLocations.put("Galle", new LatLng(6.0328, 80.2168));
        districtLocations.put("Gampaha", new LatLng(7.0873, 79.9985));
        districtLocations.put("Hambantota", new LatLng(6.1246, 81.1220));
        districtLocations.put("Jaffna", new LatLng(9.6615, 80.0255));
        districtLocations.put("Kalutara", new LatLng(6.5854, 79.9607));
        districtLocations.put("Kandy", new LatLng(7.2906, 80.6337));
        districtLocations.put("Kegalle", new LatLng(7.2513, 80.3464));
        districtLocations.put("Kilinochchi", new LatLng(9.3803, 80.3770));
        districtLocations.put("Kurunegala", new LatLng(7.4818, 80.3609));
        districtLocations.put("Mannar", new LatLng(8.9810, 79.9044));
        districtLocations.put("Matale", new LatLng(7.4675, 80.6234));
        districtLocations.put("Matara", new LatLng(5.9549, 80.5469));
        districtLocations.put("Moneragala", new LatLng(6.8728, 81.3471));
        districtLocations.put("Mullaitivu", new LatLng(9.2671, 80.8142));
        districtLocations.put("Nuwara Eliya", new LatLng(6.9497, 80.7828));
        districtLocations.put("Polonnaruwa", new LatLng(7.9403, 81.0188));
        districtLocations.put("Puttalam", new LatLng(8.0362, 79.8283));
        districtLocations.put("Ratnapura", new LatLng(6.7056, 80.3847));
        districtLocations.put("Trincomalee", new LatLng(8.5711, 81.2335));
        districtLocations.put("Vavuniya", new LatLng(8.7514, 80.4971));

        List<String> districtList = new ArrayList<>(districtLocations.keySet());
        Collections.sort(districtList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, districtList);
        etDistrict.setAdapter(adapter);

        etDistrict.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDistrict = adapter.getItem(position);
            if (mMap != null && selectedDistrict != null && districtLocations.containsKey(selectedDistrict)) {
                LatLng districtCenter = districtLocations.get(selectedDistrict);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(districtCenter, 10f));
            }
        });
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
        String district = etDistrict.getText().toString().trim();
        String dailyLimitStr = etDailyLimit.getText().toString().trim();

        if (contactNo.isEmpty() || address.isEmpty() || openTime.isEmpty() || closeTime.isEmpty() || district.isEmpty() || dailyLimitStr.isEmpty() || locationLat == null) {
            Toast.makeText(this, "Please fill all fields, select a district, and map location", Toast.LENGTH_SHORT).show();
            return;
        }

        int dailyLimit = Integer.parseInt(dailyLimitStr);

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("contactNo", contactNo);
        profileData.put("address", address);
        profileData.put("district", district);
        profileData.put("locationLat", locationLat);
        profileData.put("locationLng", locationLng);
        profileData.put("openTime", openTime);
        profileData.put("closeTime", closeTime);
        profileData.put("dailyLimit", dailyLimit);

        viewModel.updateProfile(profileData);
    }
}