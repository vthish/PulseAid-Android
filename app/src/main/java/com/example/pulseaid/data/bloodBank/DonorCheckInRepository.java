package com.example.pulseaid.data.bloodBank;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class DonorCheckInRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface AppointmentDetailsCallback {
        void onSuccess(DocumentSnapshot appointmentDoc, DocumentSnapshot donorDoc);
        void onFailure(String error);
    }

    public interface TransactionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void getAppointmentWithDonor(String appointmentId, AppointmentDetailsCallback callback) {
        if (appointmentId == null || appointmentId.trim().isEmpty() || appointmentId.contains("/") || appointmentId.contains(".")) {
            callback.onFailure("Invalid QR Code Format!");
            return;
        }

        try {
            db.collection("appointments").document(appointmentId).get()
                    .addOnSuccessListener(appointmentDoc -> {
                        if (appointmentDoc.exists()) {
                            String donorUid = appointmentDoc.getString("donorUid");
                            if (donorUid != null) {
                                db.collection("Users").document(donorUid).get()
                                        .addOnSuccessListener(donorDoc -> callback.onSuccess(appointmentDoc, donorDoc))
                                        .addOnFailureListener(e -> callback.onFailure("Donor profile not found!"));
                            } else {
                                callback.onFailure("Invalid appointment data!");
                            }
                        } else {
                            callback.onFailure("Appointment not found in database!");
                        }
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } catch (Exception e) {
            callback.onFailure("Database Error: " + e.getMessage());
        }
    }

    public void completeDonationTransaction(String appId, String donorId, String bankId, String type, int units, TransactionCallback callback) {
        db.runTransaction(transaction -> {
                    transaction.update(db.collection("appointments").document(appId), "status", "COMPLETED");
                    transaction.update(db.collection("Users").document(donorId), "lastDonationDate", System.currentTimeMillis());
                    transaction.update(db.collection("Users").document(donorId), "donationCount", FieldValue.increment(1));
                    transaction.update(db.collection("Users").document(bankId), "inventory." + type, FieldValue.increment(units));
                    return null;
                }).addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}