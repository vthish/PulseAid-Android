package com.example.pulseaid.ui.hospital;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.pulseaid.R;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HospitalProfileActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ImageView btnBack;
    private MaterialButton btnSaveProfile;
    private TextInputEditText etHospitalName, etContactNumber, etAddress;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private LatLng currentLocationLatLng;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        etHospitalName = findViewById(R.id.etHospitalName);
        etContactNumber = findViewById(R.id.etContactNumber);
        etAddress = findViewById(R.id.etAddress);

        etHospitalName.setEnabled(false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadHospitalData();

        btnBack.setOnClickListener(v -> finish());

        btnSaveProfile.setOnClickListener(v -> {
            String contact = etContactNumber.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (contact.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentLocationLatLng == null) {
                Toast.makeText(this, "Please wait for the location to load", Toast.LENGTH_SHORT).show();
                return;
            }

            saveProfileToFirestore(contact, address);
        });
    }

    private void loadHospitalData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                etHospitalName.setText(name);

                if (documentSnapshot.contains("contactNumber")) {
                    etContactNumber.setText(documentSnapshot.getString("contactNumber"));
                }
                if (documentSnapshot.contains("address")) {
                    etAddress.setText(documentSnapshot.getString("address"));
                }

                if (documentSnapshot.contains("latitude") && documentSnapshot.contains("longitude")) {
                    double lat = documentSnapshot.getDouble("latitude");
                    double lng = documentSnapshot.getDouble("longitude");
                    currentLocationLatLng = new LatLng(lat, lng);

                    if (mMap != null) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(currentLocationLatLng).title("Saved Location"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 15f));
                    }
                }
            }
        });
    }

    private void saveProfileToFirestore(String contact, String address) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> hospitalData = new HashMap<>();
        hospitalData.put("contactNumber", contact);
        hospitalData.put("address", address);
        hospitalData.put("latitude", currentLocationLatLng.latitude);
        hospitalData.put("longitude", currentLocationLatLng.longitude);
        hospitalData.put("profileComplete", true);

        db.collection("Users").document(uid)
                .update(hospitalData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HospitalProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(HospitalProfileActivity.this, HospitalDashboard.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HospitalProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (currentLocationLatLng != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(currentLocationLatLng).title("Saved Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 15f));
        } else {
            getCurrentLocation();
        }

        mMap.setOnMapClickListener(latLng -> {
            currentLocationLatLng = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && currentLocationLatLng == null) {
                currentLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(currentLocationLatLng).title("Hospital Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 15f));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }
}