package com.pulseaid.ui.donor;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pulseaid.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DonorUpcomingAppointmentFragment extends Fragment {

    private ImageView ivQrCode;
    private TextView tvDate, tvTime, tvCenter, tvQueueNo, tvNoAppointment, tvStatusBadge;
    private MaterialButton btnCancel, btnDirections;
    private MaterialCardView cardMain;
    private LinearLayout layoutContent, layoutLoading;
    private FirebaseFirestore db;
    private String currentAppointmentId;
    private Double centerLat, centerLng;
    private ListenerRegistration appointmentListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donor_upcoming_appointment, container, false);

        db = FirebaseFirestore.getInstance();
        initViews(view);
        showLoadingState();
        listenToAppointmentChanges();

        btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());
        btnDirections.setOnClickListener(v -> openGoogleMaps());

        return view;
    }

    private void initViews(View view) {
        ivQrCode = view.findViewById(R.id.iv_upcoming_qr);
        tvDate = view.findViewById(R.id.tv_upcoming_date);
        tvTime = view.findViewById(R.id.tv_upcoming_time);
        tvCenter = view.findViewById(R.id.tv_upcoming_center);
        tvQueueNo = view.findViewById(R.id.tv_upcoming_queue_no);
        tvNoAppointment = view.findViewById(R.id.tv_no_upcoming_appointment);
        tvStatusBadge = view.findViewById(R.id.tv_upcoming_status_badge);
        btnCancel = view.findViewById(R.id.btn_cancel_appointment);
        btnDirections = view.findViewById(R.id.btn_get_directions);
        cardMain = view.findViewById(R.id.card_upcoming_main);
        layoutContent = view.findViewById(R.id.layout_content);
        layoutLoading = view.findViewById(R.id.layout_loading);
    }

    private void listenToAppointmentChanges() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            showNoAppointmentUI();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        appointmentListener = db.collection("appointments")
                .whereEqualTo("donorUid", uid)
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        showNoAppointmentUI();
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        List<DocumentSnapshot> docs = new ArrayList<>(value.getDocuments());

                        Collections.sort(docs, (doc1, doc2) -> {
                            Long t1 = doc1.getLong("timestamp");
                            Long t2 = doc2.getLong("timestamp");

                            if (t1 == null) t1 = 0L;
                            if (t2 == null) t2 = 0L;

                            return Long.compare(t2, t1);
                        });

                        DocumentSnapshot doc = docs.get(0);
                        currentAppointmentId = doc.getId();
                        saveAppointmentLocally(currentAppointmentId);
                        displayAppointmentDetails(doc);
                    } else {
                        showNoAppointmentUI();
                    }
                });
    }

    private void displayAppointmentDetails(DocumentSnapshot doc) {
        tvDate.setText(safeText(doc.getString("date")));
        tvTime.setText(safeText(doc.getString("timeSlot")));
        tvStatusBadge.setText("PENDING");

        Long queueNo = doc.getLong("queueNo");
        tvQueueNo.setText(String.format("%02d", queueNo != null ? queueNo : 0));

        String centerId = doc.getString("centerId");
        if (centerId != null && !centerId.trim().isEmpty()) {
            db.collection("Users").document(centerId).get().addOnSuccessListener(centerDoc -> {
                if (centerDoc.exists()) {
                    String centerName = centerDoc.getString("name");
                    tvCenter.setText(!TextUtils.isEmpty(centerName) ? centerName : "Blood Center");

                    centerLat = centerDoc.getDouble("locationLat");
                    centerLng = centerDoc.getDouble("locationLng");

                    if (centerLat != null && centerLng != null) {
                        btnDirections.setVisibility(View.VISIBLE);
                    } else {
                        btnDirections.setVisibility(View.GONE);
                    }
                } else {
                    tvCenter.setText("Blood Center");
                    btnDirections.setVisibility(View.GONE);
                }

                generateQrCode(doc.getId());
                showContentState();
            }).addOnFailureListener(e -> {
                tvCenter.setText("Blood Center");
                btnDirections.setVisibility(View.GONE);
                generateQrCode(doc.getId());
                showContentState();
            });
        } else {
            tvCenter.setText("Blood Center");
            btnDirections.setVisibility(View.GONE);
            generateQrCode(doc.getId());
            showContentState();
        }
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private void openGoogleMaps() {
        if (centerLat != null && centerLng != null) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + centerLat + "," + centerLng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (getActivity() != null && mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        }
    }

    private void saveAppointmentLocally(String id) {
        if (getActivity() == null) {
            return;
        }

        SharedPreferences prefs = getActivity().getSharedPreferences("PulseAidPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("last_appointment_id", id).apply();
    }

    private void clearSavedAppointmentLocally() {
        if (getActivity() == null) {
            return;
        }

        SharedPreferences prefs = getActivity().getSharedPreferences("PulseAidPrefs", Context.MODE_PRIVATE);
        prefs.edit().remove("last_appointment_id").apply();
    }

    private void generateQrCode(String appointmentId) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(appointmentId, BarcodeFormat.QR_CODE, 500, 500);
            ivQrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            ivQrCode.setImageDrawable(null);
        }
    }

    private void showCancelConfirmationDialog() {
        if (getContext() == null) {
            return;
        }

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cancel_appointment_confirm);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        MaterialButton btnNo = dialog.findViewById(R.id.btn_cancel_no);
        MaterialButton btnYes = dialog.findViewById(R.id.btn_cancel_yes);

        btnNo.setOnClickListener(v -> dialog.dismiss());

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            cancelAppointment();
        });

        dialog.show();
    }

    private void cancelAppointment() {
        if (currentAppointmentId != null) {
            db.collection("appointments").document(currentAppointmentId)
                    .update("status", "CANCELLED")
                    .addOnSuccessListener(aVoid -> {
                        clearSavedAppointmentLocally();
                        Toast.makeText(getContext(), "Appointment Cancelled", Toast.LENGTH_SHORT).show();
                        goHome();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Unable to cancel appointment", Toast.LENGTH_SHORT).show());
        }
    }

    private void goHome() {
        if (getActivity() instanceof DonorDashboardActivity) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.donor_fragment_container, new DonorHomeFragment())
                    .commit();

            if (((DonorDashboardActivity) requireActivity()).findViewById(R.id.donor_bottom_nav) != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                        requireActivity().findViewById(R.id.donor_bottom_nav);
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    private void showLoadingState() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);
        tvNoAppointment.setVisibility(View.GONE);
    }

    private void showContentState() {
        layoutLoading.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);
        tvNoAppointment.setVisibility(View.GONE);
    }

    private void showNoAppointmentUI() {
        layoutLoading.setVisibility(View.GONE);
        layoutContent.setVisibility(View.GONE);
        tvNoAppointment.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (appointmentListener != null) {
            appointmentListener.remove();
        }
    }
}