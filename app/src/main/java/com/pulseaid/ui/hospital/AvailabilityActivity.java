package com.pulseaid.ui.hospital;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pulseaid.R;
import com.pulseaid.data.hospital.HospitalBankModel;
import com.pulseaid.viewmodel.hospital.BloodRequestViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AvailabilityActivity extends AppCompatActivity {

    private BloodRequestViewModel viewModel;
    private HospitalBankAdapter adapter;
    private String bloodType = "";
    private String urgency = "Normal";
    private int requiredUnits = 0;
    private SwitchCompat switchCompatible;
    private RecyclerView recyclerView;
    private MaterialButton btnConfirm;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_availability);

            initViews();
            setupToolbar();
            setupViewModel();
            readIntentData();
            setupRecyclerView();
            setupObservers();
            setupActions();
            loadSuggestions(false);

        } catch (Exception e) {
            Log.e("PulseAid_Error", "Activity Crash: " + e.getMessage(), e);
            Toast.makeText(this, "System Error occurred!", Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            switchCompatible = findViewById(R.id.switchCompatible);
            recyclerView = findViewById(R.id.rvBankSuggestions);
            btnConfirm = findViewById(R.id.btnConfirm);

            if (toolbar == null) {
                throw new IllegalStateException("Toolbar not found");
            }
            if (switchCompatible == null) {
                throw new IllegalStateException("Compatibility switch not found");
            }
            if (recyclerView == null) {
                throw new IllegalStateException("RecyclerView not found");
            }
            if (btnConfirm == null) {
                throw new IllegalStateException("Confirm button not found");
            }

        } catch (Exception e) {
            Toast.makeText(this, "UI Initialization Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Error", "initViews failed", e);
            throw e;
        }
    }

    private void setupToolbar() {
        try {
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Suggested Blood Banks");
            }

            Drawable upArrow = ContextCompat.getDrawable(
                    this,
                    androidx.appcompat.R.drawable.abc_ic_ab_back_material
            );

            if (upArrow != null) {
                upArrow.setTint(ContextCompat.getColor(this, android.R.color.black));
                toolbar.setNavigationIcon(upArrow);
            }

            toolbar.setNavigationOnClickListener(v -> {
                try {
                    onBackPressed();
                } catch (Exception e) {
                    Toast.makeText(this, "Back action failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("PulseAid_Error", "Toolbar back failed", e);
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Toolbar setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Error", "setupToolbar failed", e);
        }
    }

    private void setupViewModel() {
        try {
            viewModel = new ViewModelProvider(this).get(BloodRequestViewModel.class);
        } catch (Exception e) {
            Toast.makeText(this, "ViewModel load failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Error", "setupViewModel failed", e);
            throw e;
        }
    }

    private void readIntentData() {
        try {
            if (getIntent() != null) {
                String bt = getIntent().getStringExtra("BLOOD_TYPE");
                String ur = getIntent().getStringExtra("URGENCY");

                bloodType = bt != null ? bt : "";
                urgency = ur != null ? ur : "Normal";
                requiredUnits = getIntent().getIntExtra("REQUIRED_UNITS", 0);
            }

            if (bloodType.trim().isEmpty()) {
                Toast.makeText(this, "Blood type not found", Toast.LENGTH_LONG).show();
            }

            if (requiredUnits <= 0) {
                Toast.makeText(this, "Required units not valid", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Intent data error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Error", "readIntentData failed", e);
        }
    }

    private void setupRecyclerView() {
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new HospitalBankAdapter();
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, "List setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Error", "setupRecyclerView failed", e);
        }
    }

    private void setupObservers() {
        try {
            viewModel.statusMessage.observe(this, message -> {
                try {
                    if (message != null && !message.trim().isEmpty()) {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("PulseAid_Error", "statusMessage observe failed", e);
                }
            });

            viewModel.suggestedBanks.observe(this, banks -> {
                try {
                    List<HospitalBankModel> safeBanks = banks != null ? banks : new ArrayList<>();
                    adapter.setBanks(safeBanks);

                    int foundUnits = 0;
                    for (HospitalBankModel b : safeBanks) {
                        if (b != null) {
                            foundUnits += b.getUnitsToContribute();
                        }
                    }

                    if (!safeBanks.isEmpty() && foundUnits < requiredUnits) {
                        Toast.makeText(
                                this,
                                "Only " + foundUnits + " units available near you.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(this, "Display error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("PulseAid_Error", "suggestedBanks observe failed", e);
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Observer setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Error", "setupObservers failed", e);
        }
    }

    private void setupActions() {
        try {
            switchCompatible.setOnCheckedChangeListener((buttonView, isChecked) -> {
                try {
                    loadSuggestions(isChecked);
                } catch (Exception e) {
                    Toast.makeText(this, "Compatibility filter failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("PulseAid_Error", "switchCompatible failed", e);
                }
            });

            btnConfirm.setOnClickListener(v -> {
                try {
                    List<HospitalBankModel> banksToRequest = viewModel.suggestedBanks.getValue();

                    if (banksToRequest != null && !banksToRequest.isEmpty()) {
                        viewModel.placeBloodOrder(bloodType, requiredUnits, urgency, banksToRequest);
                        finish();
                    } else {
                        Toast.makeText(this, "No banks selected!", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(this, "Confirm failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("PulseAid_Error", "Confirm failed", e);
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Action setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Error", "setupActions failed", e);
        }
    }

    private void loadSuggestions(boolean includeCompatible) {
        try {
            if (bloodType.trim().isEmpty() || requiredUnits <= 0) {
                Toast.makeText(this, "Invalid request details", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.findSuggestedBanks(bloodType, requiredUnits, includeCompatible);

        } catch (Exception e) {
            Toast.makeText(this, "Suggestion load failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Error", "loadSuggestions failed", e);
        }
    }
}