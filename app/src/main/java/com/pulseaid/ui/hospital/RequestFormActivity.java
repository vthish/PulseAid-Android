package com.pulseaid.ui.hospital;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.pulseaid.R;
import com.google.android.material.button.MaterialButton;

public class RequestFormActivity extends AppCompatActivity {

    private Spinner spinBloodType;
    private EditText etUnits;
    private RadioGroup rgUrgency;
    private MaterialButton btnFindBanks;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_request_form);

            initViews();
            setupToolbar();
            setupBloodTypeSpinner();
            setupActions();

        } catch (Exception e) {
            Toast.makeText(this, "Initialization Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Log", "Error in onCreate", e);
        }
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            spinBloodType = findViewById(R.id.spinBloodType);
            etUnits = findViewById(R.id.etUnits);
            rgUrgency = findViewById(R.id.rgUrgency);
            btnFindBanks = findViewById(R.id.btnCheck);

            if (toolbar == null) {
                throw new IllegalStateException("Toolbar not found");
            }
            if (spinBloodType == null) {
                throw new IllegalStateException("Blood type spinner not found");
            }
            if (etUnits == null) {
                throw new IllegalStateException("Units input not found");
            }
            if (rgUrgency == null) {
                throw new IllegalStateException("Urgency selector not found");
            }
            if (btnFindBanks == null) {
                throw new IllegalStateException("Find button not found");
            }

        } catch (Exception e) {
            Toast.makeText(this, "View Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Log", "Error in initViews", e);
            throw e;
        }
    }

    private void setupToolbar() {
        try {
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("New Blood Request");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
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
                    Log.e("PulseAid_Log", "Toolbar back error", e);
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Toolbar Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Log", "Error in setupToolbar", e);
        }
    }

    private void setupBloodTypeSpinner() {
        try {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.blood_types_array,
                    android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinBloodType.setAdapter(adapter);

            spinBloodType.setClickable(true);
            spinBloodType.setFocusable(true);
            spinBloodType.setEnabled(true);

            spinBloodType.setOnTouchListener((v, event) -> {
                try {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        spinBloodType.performClick();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Blood type list open failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("PulseAid_Log", "Spinner touch error", e);
                }
                return false;
            });

        } catch (Exception e) {
            Toast.makeText(this, "Spinner Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Log", "Error in setupBloodTypeSpinner", e);
        }
    }

    private void setupActions() {
        try {
            btnFindBanks.setOnClickListener(v -> {
                try {
                    submitRequest();
                } catch (Exception e) {
                    Toast.makeText(this, "Submit action failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("PulseAid_Log", "Button click error", e);
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Action Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Log", "Error in setupActions", e);
        }
    }

    private void submitRequest() {
        try {
            if (spinBloodType.getSelectedItem() == null) {
                Toast.makeText(this, "Please select a valid blood type", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedType = String.valueOf(spinBloodType.getSelectedItem()).trim();
            String unitsStr = etUnits.getText() != null ? etUnits.getText().toString().trim() : "";

            if (selectedType.isEmpty()) {
                Toast.makeText(this, "Please select a valid blood type", Toast.LENGTH_SHORT).show();
                return;
            }

            if (unitsStr.isEmpty()) {
                etUnits.setError("Required");
                Toast.makeText(this, "Please enter the number of units", Toast.LENGTH_SHORT).show();
                return;
            }

            int units;
            try {
                units = Integer.parseInt(unitsStr);
            } catch (NumberFormatException e) {
                etUnits.setError("Invalid number");
                Toast.makeText(this, "Invalid unit count. Please enter a number", Toast.LENGTH_SHORT).show();
                Log.e("PulseAid_Log", "Unit parse error", e);
                return;
            }

            if (units <= 0) {
                etUnits.setError("Must be greater than zero");
                Toast.makeText(this, "Unit count must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }

            String urgency = "Normal";
            int selectedUrgencyId = rgUrgency.getCheckedRadioButtonId();
            if (selectedUrgencyId == R.id.rbUrgent) {
                urgency = "Urgent";
            }

            Intent intent = new Intent(RequestFormActivity.this, AvailabilityActivity.class);
            intent.putExtra("BLOOD_TYPE", selectedType);
            intent.putExtra("REQUIRED_UNITS", units);
            intent.putExtra("URGENCY", urgency);
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Request Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PulseAid_Log", "Error in submitRequest", e);
        }
    }
}