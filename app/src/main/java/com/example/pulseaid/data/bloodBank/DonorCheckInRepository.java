package com.example.pulseaid.data.bloodBank;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DonorCheckInRepository {

    private final FirebaseFirestore db;

    public DonorCheckInRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface AppointmentCallback {
        void onSuccess(DocumentSnapshot document);
        void onFailure(String error);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void getAppointment(String appointmentId, AppointmentCallback callback) {
        db.collection("appointments").document(appointmentId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        callback.onSuccess(task.getResult());
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Error fetching appointment.");
                    }
                });
    }

    public void updateAppointmentStatus(String appointmentId, String status, UpdateCallback callback) {
        db.collection("appointments").document(appointmentId).update("status", status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Error updating status.");
                    }
                });
    }
}