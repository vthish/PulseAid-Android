package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HospitalRequestRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public HospitalRequestRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public interface RequestCallback {
        void onSuccess(List<HospitalRequest> requests);
        void onFailure(String errorMessage);
    }

    public void fetchHospitalRequests(RequestCallback callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onFailure("User not logged in");
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        // Fetching real pending requests from Firestore
        db.collection("HospitalRequests")
                .whereEqualTo("bloodBankId", currentUserId)
                .whereEqualTo("status", "Pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HospitalRequest> requestList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            // Ensure these field names match your Firestore database exactly
                            String name = document.getString("hospitalName");
                            String type = document.getString("bloodGroup");
                            String unitsStr = document.getString("units");

                            String qty = (unitsStr != null ? unitsStr : "0") + " Units";
                            name = name != null ? name : "Unknown Hospital";
                            type = type != null ? type : "Unknown";

                            requestList.add(new HospitalRequest(id, name, type, qty));
                        }
                        callback.onSuccess(requestList);
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Error fetching requests");
                    }
                });
    }

    // Method to update the status in Firestore (Issue or Reject)
    public void updateRequestStatus(String requestId, String newStatus, RequestCallback callback) {
        db.collection("HospitalRequests").document(requestId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Refresh the list after successful update
                    fetchHospitalRequests(callback);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public static class HospitalRequest {
        public String id, name, type, qty;

        public HospitalRequest(String id, String name, String type, String qty) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.qty = qty;
        }
    }
}