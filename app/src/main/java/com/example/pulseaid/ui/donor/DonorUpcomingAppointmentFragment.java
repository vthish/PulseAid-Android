package com.example.pulseaid.ui.donor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pulseaid.R;
import com.google.android.material.button.MaterialButton;

public class DonorUpcomingAppointmentFragment extends Fragment {

    private MaterialButton btnCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donor_upcoming_appointment, container, false);

        btnCancel = view.findViewById(R.id.btn_cancel_appointment);

        btnCancel.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Appointment Cancellation Requested", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}