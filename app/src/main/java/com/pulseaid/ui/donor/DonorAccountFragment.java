package com.pulseaid.ui.donor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.pulseaid.R;
import com.pulseaid.ui.LoginActivity;
import com.pulseaid.viewmodel.donor.DonorAccountViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DonorAccountFragment extends Fragment {

    private TextView tvViewName, tvViewPhone, tvViewWeight, tvViewDob, tvStaticNic, tvStaticEmail, tvHeaderName, tvHeaderBlood, tvAccountError;
    private TextInputLayout tilEditName, tilEditPhone, tilEditWeight, tilEditDob;
    private TextInputEditText etEditName, etEditPhone, etEditWeight, etEditDob;
    private MaterialButton btnEditToggle, btnLogout;
    private LinearLayout layoutAccountLoading;
    private NestedScrollView nestedScrollView;

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

        btnEditToggle.setOnClickListener(v -> {
            if (isEditMode) {
                saveData();
            } else {
                toggleEditMode(true);
            }
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        etEditDob.setOnClickListener(v -> showDatePicker());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        showLoadingState();
        viewModel.startProfileListener(userId);
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.stopProfileListener();
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
        tvAccountError = v.findViewById(R.id.tv_account_error);

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

        layoutAccountLoading = v.findViewById(R.id.layout_account_loading);
        nestedScrollView = v.findViewById(R.id.nestedScrollView);
    }

    private void setupObservers() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), doc -> {
            if (doc != null && doc.exists()) {
                updateUI(doc);
                showContentState();
            }
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                toggleEditMode(false);
                viewModel.resetUpdateStatus();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.trim().isEmpty()) {
                if (nestedScrollView.getVisibility() != View.VISIBLE) {
                    showErrorState();
                }
            }
        });
    }

    private void updateUI(DocumentSnapshot doc) {
        String name = safeText(doc.getString("name"));
        String phone = safePhone(doc);
        String weight = safeWeight(doc.get("weight"));
        String dob = safeText(doc.getString("dob"));
        String blood = safeText(doc.getString("bloodGroup"));
        String nic = safeText(doc.getString("nic"));
        String email = safeText(doc.getString("email"));

        tvViewName.setText(name);
        tvViewPhone.setText(phone);
        tvViewWeight.setText(TextUtils.isEmpty(weight) ? "-" : weight + " kg");
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

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private String safePhone(DocumentSnapshot doc) {
        String phone = doc.getString("phone");
        if (phone != null && !phone.trim().isEmpty()) {
            return phone.trim();
        }

        String phoneNumber = doc.getString("phoneNumber");
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            return phoneNumber.trim();
        }

        return "-";
    }

    private String safeWeight(Object weightObj) {
        if (weightObj == null) {
            return "";
        }
        return String.valueOf(weightObj).trim();
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
    }

    private void saveData() {
        String name = etEditName.getText() != null ? etEditName.getText().toString().trim() : "";
        String phone = etEditPhone.getText() != null ? etEditPhone.getText().toString().trim() : "";
        String weightStr = etEditWeight.getText() != null ? etEditWeight.getText().toString().trim() : "";
        String dob = etEditDob.getText() != null ? etEditDob.getText().toString().trim() : "";

        if (name.isEmpty() || phone.isEmpty() || weightStr.isEmpty() || dob.isEmpty()) {
            return;
        }

        int weightValue;
        try {
            weightValue = Integer.parseInt(weightStr);
        } catch (Exception e) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("phoneNumber", phone);
        updates.put("weight", weightValue);
        updates.put("dob", dob);

        viewModel.updateProfile(userId, updates);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
            etEditDob.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void showLoadingState() {
        layoutAccountLoading.setVisibility(View.VISIBLE);
        nestedScrollView.setVisibility(View.GONE);
        tvAccountError.setVisibility(View.GONE);
    }

    private void showContentState() {
        layoutAccountLoading.setVisibility(View.GONE);
        nestedScrollView.setVisibility(View.VISIBLE);
        tvAccountError.setVisibility(View.GONE);
    }

    private void showErrorState() {
        layoutAccountLoading.setVisibility(View.GONE);
        nestedScrollView.setVisibility(View.GONE);
        tvAccountError.setVisibility(View.VISIBLE);
    }
}