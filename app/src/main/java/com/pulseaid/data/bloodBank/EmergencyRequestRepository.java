package com.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EmergencyRequestRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public EmergencyRequestRepository() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }


    public interface RequestCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void sendEmergencyRequest(String bloodType, int units, String reason, RequestCallback callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onFailure("User not authenticated!");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("centerId", userId);
        data.put("bloodGroup", bloodType);
        data.put("units", units);
        data.put("reason", reason);
        data.put("status", "Pending");
        data.put("timestamp", System.currentTimeMillis());

        db.collection("EmergencyRequests").add(data)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}