package com.example.pulseaid.data.bloodBank;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        db.collection("appointments").document(appointmentId).get()
                .addOnSuccessListener(appointmentDoc -> {
                    if (appointmentDoc.exists()) {
                        String donorUid = appointmentDoc.getString("donorUid");
                        db.collection("Users").document(donorUid).get()
                                .addOnSuccessListener(donorDoc -> callback.onSuccess(appointmentDoc, donorDoc))
                                .addOnFailureListener(e -> callback.onFailure("Donor profile not found!"));
                    } else {
                        callback.onFailure("Appointment not found!");
                    }
                }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void completeDonationTransaction(String appId, String donorId, String centerId, String type, int units, TransactionCallback callback) {
        WriteBatch batch = db.batch();

        batch.update(db.collection("appointments").document(appId), "status", "COMPLETED");
        batch.update(db.collection("appointments").document(appId), "donateUnits", units);

        batch.update(db.collection("Users").document(donorId), "lastDonationDate", System.currentTimeMillis());
        batch.update(db.collection("Users").document(donorId), "donationCount", FieldValue.increment(1));

        long currentTime = System.currentTimeMillis();
        long expiryTime = currentTime + (35L * 24 * 60 * 60 * 1000);

        for (int i = 0; i < units; i++) {
            String packetId = "PKT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            Map<String, Object> packetData = new HashMap<>();
            packetData.put("packetId", packetId);
            packetData.put("bloodGroup", type);
            packetData.put("centerId", centerId);
            packetData.put("donorId", donorId);
            packetData.put("collectionTimestamp", currentTime);
            packetData.put("expiryTimestamp", expiryTime);
            packetData.put("status", "AVAILABLE");
            batch.set(db.collection("BloodPackets").document(packetId), packetData);
        }
        batch.commit().addOnSuccessListener(v -> callback.onSuccess()).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void rejectAppointmentTransaction(String appId, String reason, TransactionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "REJECTED");
        updates.put("rejectReason", reason);

        db.collection("appointments").document(appId)
                .update(updates)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}