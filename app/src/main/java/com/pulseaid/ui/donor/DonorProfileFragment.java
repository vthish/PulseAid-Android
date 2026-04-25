package com.pulseaid.ui.donor;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.pulseaid.R;
import com.pulseaid.viewmodel.donor.DonorProfileViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

public class DonorProfileFragment extends Fragment {

    private EditText inputName, inputNic, inputPhone, inputWeight, inputDob, inputAddress;
    private Spinner spinnerGender, spinnerBlood;
    private MaterialButton btnSave;
    private DonorProfileViewModel profileViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donor_profile, container, false);

        inputName = view.findViewById(R.id.donor_input_name);
        inputNic = view.findViewById(R.id.donor_input_nic);
        inputPhone = view.findViewById(R.id.donor_input_phone);
        inputWeight = view.findViewById(R.id.donor_input_weight);
        inputDob = view.findViewById(R.id.donor_input_dob);
        inputAddress = view.findViewById(R.id.donor_input_address);
        spinnerGender = view.findViewById(R.id.donor_spinner_gender);
        spinnerBlood = view.findViewById(R.id.donor_spinner_blood);
        btnSave = view.findViewById(R.id.btn_save_donor_profile);

        profileViewModel = new ViewModelProvider(this).get(DonorProfileViewModel.class);

        profileViewModel.getSaveStatus().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.donor_fragment_container, new DonorHomeFragment())
                        .commit();
            }
        });

        setupSelectors();

        btnSave.setOnClickListener(v -> validateAndSubmit());

        return view;
    }

    private void setupSelectors() {
        String[] genders = {"Gender", "Male", "Female"};
        String[] bloodGroups = {"Blood Group", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, genders) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(position == 0 ? Color.parseColor("#757575") : Color.BLACK);
                tv.setTextSize(15f);
                tv.setPadding(24, 24, 24, 24);
                tv.setSingleLine(true);
                tv.setEllipsize(TextUtils.TruncateAt.END);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setBackgroundColor(Color.WHITE);

                if (position == 0) {
                    tv.setTextColor(Color.TRANSPARENT);
                    tv.setHeight(1);
                    tv.setPadding(0, 0, 0, 0);
                } else {
                    tv.setTextColor(Color.BLACK);
                    tv.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                    tv.setPadding(32, 28, 32, 28);
                }

                return tv;
            }
        };
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, bloodGroups) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(position == 0 ? Color.parseColor("#757575") : Color.BLACK);
                tv.setTextSize(15f);
                tv.setPadding(24, 24, 24, 24);
                tv.setSingleLine(true);
                tv.setEllipsize(TextUtils.TruncateAt.END);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setBackgroundColor(Color.WHITE);

                if (position == 0) {
                    tv.setTextColor(Color.TRANSPARENT);
                    tv.setHeight(1);
                    tv.setPadding(0, 0, 0, 0);
                } else {
                    tv.setTextColor(Color.BLACK);
                    tv.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                    tv.setPadding(32, 28, 32, 28);
                }

                return tv;
            }
        };
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerGender.setAdapter(genderAdapter);
        spinnerBlood.setAdapter(bloodAdapter);
        spinnerGender.setSelection(0);
        spinnerBlood.setSelection(0);

        inputDob.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (dp, y, m, d) ->
                    inputDob.setText(d + "/" + (m + 1) + "/" + y),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void validateAndSubmit() {
        String name = inputName.getText().toString().trim();
        String nic = inputNic.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String weight = inputWeight.getText().toString().trim();
        String dob = inputDob.getText().toString().trim();
        String address = inputAddress.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String blood = spinnerBlood.getSelectedItem().toString();

        if (name.isEmpty() || nic.isEmpty() || phone.isEmpty() || weight.isEmpty() ||
                dob.isEmpty() || address.isEmpty() || gender.equals("Gender") || blood.equals("Blood Group")) {
            Toast.makeText(getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!(nic.matches("^[0-9]{9}[vVxX]$") || nic.matches("^[0-9]{12}$"))) {
            inputNic.setError("Invalid NIC Format");
            return;
        }

        if (!phone.matches("^\\d{10}$")) {
            inputPhone.setError("Enter a valid 10-digit number");
            return;
        }

        String[] dateParts = dob.split("/");
        if (dateParts.length != 3) {
            inputDob.setError("Invalid date");
            return;
        }

        int day;
        int month;
        int year;

        try {
            day = Integer.parseInt(dateParts[0]);
            month = Integer.parseInt(dateParts[1]) - 1;
            year = Integer.parseInt(dateParts[2]);
        } catch (Exception e) {
            inputDob.setError("Invalid date");
            return;
        }

        int calculatedAge = calculateAge(year, month, day);

        if (calculatedAge < 18) {
            Toast.makeText(getContext(), "Sorry you must be at least 18 years old donate blood.", Toast.LENGTH_LONG).show();
            return;
        }

        profileViewModel.updateDonorProfile(name, nic, phone, gender, blood, weight, dob, address, calculatedAge);
        Toast.makeText(getContext(), "Processing PulseAid Profile.", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocus = getActivity().getCurrentFocus();
            if (imm != null && currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    private int calculateAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
}