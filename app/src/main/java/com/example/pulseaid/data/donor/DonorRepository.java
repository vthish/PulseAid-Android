package com.example.pulseaid.data.donor;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

    public Task<Void> updateProfile(String userId, Map<String, Object> updates) {
        return db.collection("Users").document(userId).update(updates);
    }

    public void getBloodCentersByDistrict(String district, OnCentersCallback callback) {
        db.collection("Users")
                .whereEqualTo("role", "Blood Bank")
                .whereEqualTo("district", district)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public interface OnProfileCallback {
        void onSuccess(DocumentSnapshot document);
        void onFailure(Exception e);
    }

    public interface OnCentersCallback {
        void onSuccess(QuerySnapshot querySnapshot);
        void onFailure(Exception e);
    }
}