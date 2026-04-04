package com.example.pulseaid.data.donor;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class DonorRepository {
    private final FirebaseFirestore db;

    public DonorRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getProfile(String userId, OnProfileCallback callback) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public ListenerRegistration listenToProfile(String userId, OnProfileListenerCallback callback) {
        return db.collection("Users").document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot);
                    }
                });
    }

    public Task<Void> updateProfile(String userId, Map<String, Object> updates) {
        return db.collection("Users").document(userId).update(updates);
    }

    public void getBloodCentersByDistrict(String district, OnCentersCallback callback) {
        db.collection("Users")
                .whereEqualTo("role", "Blood Bank")
                .whereEqualTo("district", district)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching centers: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    public void getAppointmentsForSlot(String centerId, String date, String timeSlot, OnAppointmentsCallback callback) {
        db.collection("appointments")
                .whereEqualTo("centerId", centerId)
                .whereEqualTo("date", date)
                .whereEqualTo("timeSlot", timeSlot)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public void getAppointmentsForDonor(String donorUid, OnAppointmentsCallback callback) {
        db.collection("appointments")
                .whereEqualTo("donorUid", donorUid)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public void getResolvedEmergencyRequests(OnEmergencyRequestsCallback callback) {
        db.collection("EmergencyRequests")
                .whereEqualTo("status", "Resolved")
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public Task<DocumentReference> saveAppointment(Map<String, Object> appointmentData) {
        return db.collection("appointments").add(appointmentData);
    }

    public interface OnProfileCallback {
        void onSuccess(DocumentSnapshot document);
        void onFailure(Exception e);
    }

    public interface OnProfileListenerCallback {
        void onSuccess(DocumentSnapshot document);
        void onFailure(Exception e);
    }

    public interface OnCentersCallback {
        void onSuccess(QuerySnapshot querySnapshot);
        void onFailure(Exception e);
    }

    public interface OnAppointmentsCallback {
        void onSuccess(QuerySnapshot querySnapshot);
        void onFailure(Exception e);
    }

    public interface OnEmergencyRequestsCallback {
        void onSuccess(QuerySnapshot querySnapshot);
        void onFailure(Exception e);
    }
}