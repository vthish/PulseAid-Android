package com.example.pulseaid.ui.hospital;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.pulseaid.R;
import com.example.pulseaid.ui.LoginActivity;
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
    private MaterialButton btnLogout;
    private TextInputEditText etHospitalName;
    private TextInputEditText etContactNumber;
    private TextInputEditText etLandLineNumber;
    private TextInputEditText etAddress;
    private ScrollView profileScrollView;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private LatLng currentLocationLatLng;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN |
                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            );

            setContentView(R.layout.activity_hospital_profile);

            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            initViews();
            setupKeyboardHandling();
            setupMap();
            setupActions();
            loadHospitalData();

        } catch (Exception e) {
            Toast.makeText(this, "Initialization Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        try {
            btnBack = findViewById(R.id.btnBack);
            btnSaveProfile = findViewById(R.id.btnSaveProfile);
            btnLogout = findViewById(R.id.btnLogout);
            etHospitalName = findViewById(R.id.etHospitalName);
            etContactNumber = findViewById(R.id.etContactNumber);
            etLandLineNumber = findViewById(R.id.etLandLineNumber);
            etAddress = findViewById(R.id.etAddress);
            profileScrollView = findViewById(R.id.profileScrollView);

            if (etHospitalName != null) {
                etHospitalName.setEnabled(false);
            }

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        } catch (Exception e) {
            Toast.makeText(this, "View initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupKeyboardHandling() {
        try {
            setupFocusScroll(etHospitalName);
            setupFocusScroll(etContactNumber);
            setupFocusScroll(etLandLineNumber);
            setupFocusScroll(etAddress);

            View content = findViewById(android.R.id.content);
            if (content != null) {
                content.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                    try {
                        View focused = getCurrentFocus();
                        if (focused != null && profileScrollView != null) {
                            profileScrollView.post(() ->
                                    profileScrollView.smoothScrollTo(0, focused.getBottom() + 200));
                        }
                    } catch (Exception ignored) {
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Keyboard handling setup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupFocusScroll(View targetView) {
        try {
            if (targetView == null) return;

            targetView.setOnFocusChangeListener((v, hasFocus) -> {
                try {
                    if (hasFocus && profileScrollView != null) {
                        profileScrollView.postDelayed(
                                () -> profileScrollView.smoothScrollTo(0, v.getBottom() + 200),
                                200
                        );
                    }
                } catch (Exception ignored) {
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Focus setup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMap() {
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapFragment);

            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            } else {
                Toast.makeText(this, "Map not available", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Map initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupActions() {
        try {
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            if (btnSaveProfile != null) {
                btnSaveProfile.setOnClickListener(v -> {
                    try {
                        String hospitalName = etHospitalName != null && etHospitalName.getText() != null
                                ? etHospitalName.getText().toString().trim() : "";
                        String contact = etContactNumber != null && etContactNumber.getText() != null
                                ? etContactNumber.getText().toString().trim() : "";
                        String landLine = etLandLineNumber != null && etLandLineNumber.getText() != null
                                ? etLandLineNumber.getText().toString().trim() : "";
                        String address = etAddress != null && etAddress.getText() != null
                                ? etAddress.getText().toString().trim() : "";

                        if (hospitalName.isEmpty() || contact.isEmpty() || address.isEmpty()) {
                            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (currentLocationLatLng == null) {
                            Toast.makeText(this, "Please wait for the location to load", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        saveProfileToFirestore(hospitalName, contact, landLine, address);

                    } catch (Exception e) {
                        Toast.makeText(this, "Save action failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> {
                    try {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(HospitalProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } catch (Exception e) {
                        Toast.makeText(this, "Logout failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

        } catch (Exception e) {
            Toast.makeText(this, "Action setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadHospitalData() {
        try {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();

            db.collection("Users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        try {
                            if (!documentSnapshot.exists()) {
                                Toast.makeText(this, "Profile data not found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String name = documentSnapshot.getString("name");
                            String contact = documentSnapshot.getString("contactNumber");
                            String landLine = documentSnapshot.getString("landLine");
                            String address = documentSnapshot.getString("address");

                            if (etHospitalName != null) {
                                etHospitalName.setText(name != null ? name : "");
                            }

                            if (etContactNumber != null) {
                                etContactNumber.setText(contact != null ? contact : "");
                            }

                            if (etLandLineNumber != null) {
                                etLandLineNumber.setText(landLine != null ? landLine : "");
                            }

                            if (etAddress != null) {
                                etAddress.setText(address != null ? address : "");
                            }

                            Double lat = documentSnapshot.getDouble("latitude");
                            Double lng = documentSnapshot.getDouble("longitude");

                            if (lat != null && lng != null) {
                                currentLocationLatLng = new LatLng(lat, lng);

                                if (mMap != null) {
                                    mMap.clear();
                                    mMap.addMarker(
                                            new MarkerOptions()
                                                    .position(currentLocationLatLng)
                                                    .title("Saved Location")
                                    );
                                    mMap.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 15f)
                                    );
                                }
                            }

                        } catch (Exception e) {
                            Toast.makeText(this, "Profile parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Profile load failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

        } catch (Exception e) {
            Toast.makeText(this, "Load profile error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveProfileToFirestore(String hospitalName, String contact, String landLine, String address) {
        try {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentLocationLatLng == null) {
                Toast.makeText(this, "Location not selected", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();

            Map<String, Object> hospitalData = new HashMap<>();
            hospitalData.put("name", hospitalName);
            hospitalData.put("contactNumber", contact);
            hospitalData.put("landLine", landLine);
            hospitalData.put("address", address);
            hospitalData.put("latitude", currentLocationLatLng.latitude);
            hospitalData.put("longitude", currentLocationLatLng.longitude);
            hospitalData.put("profileComplete", true);

            db.collection("Users")
                    .document(uid)
                    .update(hospitalData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(HospitalProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        loadHospitalData();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(HospitalProfileActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

        } catch (Exception e) {
            Toast.makeText(this, "Save profile error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        try {
            mMap = googleMap;

            if (currentLocationLatLng != null) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(currentLocationLatLng).title("Saved Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 15f));
            } else {
                getCurrentLocation();
            }

            mMap.setOnMapClickListener(latLng -> {
                try {
                    currentLocationLatLng = latLng;
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                } catch (Exception e) {
                    Toast.makeText(this, "Map selection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Map ready error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        LOCATION_PERMISSION_REQUEST_CODE
                );
                return;
            }

            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        try {
                            if (location != null && currentLocationLatLng == null && mMap != null) {
                                currentLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(currentLocationLatLng).title("Hospital Location"));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 15f));
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "Location display failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Location fetch failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

        } catch (Exception e) {
            Toast.makeText(this, "Get location error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Permission result error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}