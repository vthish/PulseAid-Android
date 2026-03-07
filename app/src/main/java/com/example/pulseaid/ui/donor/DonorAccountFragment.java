package com.example.pulseaid.ui.donor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulseaid.R;
import com.example.pulseaid.ui.LoginActivity;
import com.example.pulseaid.viewmodel.donor.DonorAccountViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DonorAccountFragment extends Fragment {

    private TextView tvViewName, tvViewPhone, tvViewWeight, tvViewDob, tvStaticNic, tvStaticEmail, tvHeaderName, tvHeaderBlood;
    private TextInputLayout tilEditName, tilEditPhone, tilEditWeight, tilEditDob;
    private TextInputEditText etEditName, etEditPhone, etEditWeight, etEditDob;
    private MaterialButton btnEditToggle, btnLogout;

    private DonorAccountViewModel viewModel;
    private boolean isEditMode = false;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donor_account, container, false);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel = new ViewModelProvider(this).get(DonorAccountViewModel.class);

        initViews(view);
        setupObservers();

        viewModel.fetchProfile(userId);

        btnEditToggle.setOnClickListener(v -> {
            if (isEditMode) {
                saveData();
            } else {
                toggleEditMode(true);
            }
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            android.content.Intent intent = new android.content.Intent(getActivity(), LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        etEditDob.setOnClickListener(v -> showDatePicker());

        return view;
    }

    private void initViews(View v) {
        tvViewName = v.findViewById(R.id.tv_view_name);
        tvViewPhone = v.findViewById(R.id.tv_view_phone);
        tvViewWeight = v.findViewById(R.id.tv_view_weight);
        tvViewDob = v.findViewById(R.id.tv_view_dob);
        tvStaticNic = v.findViewById(R.id.tv_static_nic);
        tvStaticEmail = v.findViewById(R.id.tv_static_email);
        tvHeaderName = v.findViewById(R.id.tv_header_name);
        tvHeaderBlood = v.findViewById(R.id.tv_header_blood_group);

        tilEditName = v.findViewById(R.id.til_edit_name);
        tilEditPhone = v.findViewById(R.id.til_edit_phone);
        tilEditWeight = v.findViewById(R.id.til_edit_weight);
        tilEditDob = v.findViewById(R.id.til_edit_dob);

        etEditName = v.findViewById(R.id.et_edit_name);
        etEditPhone = v.findViewById(R.id.et_edit_phone);
        etEditWeight = v.findViewById(R.id.et_edit_weight);
        etEditDob = v.findViewById(R.id.et_edit_dob);

        btnEditToggle = v.findViewById(R.id.btn_edit_toggle);
        btnLogout = v.findViewById(R.id.btn_logout);
    }

    private void setupObservers() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), this::updateUI);

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                toggleEditMode(false);
                viewModel.resetUpdateStatus();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(DocumentSnapshot doc) {
        if (doc != null && doc.exists()) {
            String name = doc.getString("name");
            String phone = doc.getString("phone");
            String weight = String.valueOf(doc.get("weight"));
            String dob = doc.getString("dob");
            String blood = doc.getString("bloodGroup");
            String nic = doc.getString("nic");
            String email = doc.getString("email");

            tvViewName.setText(name);
            tvViewPhone.setText(phone);
            tvViewWeight.setText(weight + " kg");
            tvViewDob.setText(dob);
            tvHeaderName.setText(name);
            tvHeaderBlood.setText(blood);
            tvStaticNic.setText(nic);
            tvStaticEmail.setText(email);

            etEditName.setText(name);
            etEditPhone.setText(phone);
            etEditWeight.setText(weight);
            etEditDob.setText(dob);
        }
    }

    private void toggleEditMode(boolean enable) {
        isEditMode = enable;
        int readVis = enable ? View.GONE : View.VISIBLE;
        int editVis = enable ? View.VISIBLE : View.GONE;

        tvViewName.setVisibility(readVis);
        tilEditName.setVisibility(editVis);

        tvViewPhone.setVisibility(readVis);
        tilEditPhone.setVisibility(editVis);

        tvViewWeight.setVisibility(readVis);
        tilEditWeight.setVisibility(editVis);

        tvViewDob.setVisibility(readVis);
        tilEditDob.setVisibility(editVis);

        btnEditToggle.setText(enable ? "Save Changes" : "Edit Profile");
        btnEditToggle.setIconResource(enable ? android.R.drawable.ic_menu_save : android.R.drawable.ic_menu_edit);
    }

    private void saveData() {
        String name = etEditName.getText().toString().trim();
        String phone = etEditPhone.getText().toString().trim();
        String weightStr = etEditWeight.getText().toString().trim();
        String dob = etEditDob.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || weightStr.isEmpty() || dob.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all editable fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("weight", Integer.parseInt(weightStr));
        updates.put("dob", dob);

        viewModel.updateProfile(userId, updates);
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
            etEditDob.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }
}