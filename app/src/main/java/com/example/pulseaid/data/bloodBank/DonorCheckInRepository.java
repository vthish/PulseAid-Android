package com.example.pulseaid.data.bloodBank;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

public class DonorCheckInRepository {

    private final FirebaseFirestore db;

    public DonorCheckInRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface AppointmentDetailsCallback {
        void onSuccess(DocumentSnapshot appointmentDoc, DocumentSnapshot donorDoc);
        void onFailure(String error);
    }

    public interface TransactionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void getAppointmentWithDonor(String appointmentId, AppointmentDetailsCallback callback) {
        db.collection("appointments").document(appointmentId).get()
                .addOnSuccessListener(appointmentDoc -> {
                    if (appointmentDoc.exists()) {
                        String donorUid = appointmentDoc.getString("donorUid");
                        if (donorUid != null) {
                            db.collection("Users").document(donorUid).get()
                                    .addOnSuccessListener(donorDoc -> {
                                        callback.onSuccess(appointmentDoc, donorDoc);
                                    })
                                    .addOnFailureListener(e -> callback.onFailure("Donor details not found"));
                        } else {
                            callback.onFailure("Invalid appointment data");
                        }
                    } else {
                        callback.onFailure("Appointment not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void completeDonationTransaction(
            String appointmentId,
            String donorUid,
            String bloodBankId,
            String bloodType,
            int units,
            TransactionCallback callback) {

        DocumentReference appointmentRef = db.collection("appointments").document(appointmentId);
        DocumentReference donorRef = db.collection("Users").document(donorUid);
        DocumentReference bankRef = db.collection("Users").document(bloodBankId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    DocumentSnapshot appointmentSnap = transaction.get(appointmentRef);
                    DocumentSnapshot donorSnap = transaction.get(donorRef);

                    String status = appointmentSnap.getString("status");
                    if (!"CONFIRMED".equals(status)) {
                        throw new FirebaseFirestoreException("Appointment is not in CONFIRMED status",
                                FirebaseFirestoreException.Code.ABORTED);
                    }

                    // 1. Update Appointment Status
                    transaction.update(appointmentRef, "status", "COMPLETED");

                    // 2. Update Donor Profile (Date and Count)
                    long currentCount = 0;
                    if (donorSnap.contains("donationCount") && donorSnap.getLong("donationCount") != null) {
                        currentCount = donorSnap.getLong("donationCount");
                    }
                    transaction.update(donorRef, "lastDonationDate", System.currentTimeMillis());
                    transaction.update(donorRef, "donationCount", currentCount + 1);

                    // 3. Update Blood Bank Inventory Map
                    String inventoryPath = "inventory." + bloodType;
                    transaction.update(bankRef, inventoryPath, FieldValue.increment(units));

                    return null;
                }).addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}