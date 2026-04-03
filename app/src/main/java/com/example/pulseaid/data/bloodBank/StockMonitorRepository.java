package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class StockMonitorRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public StockMonitorRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public interface StockCallback {
        void onSuccess(Map<String, String> stockData);
        void onFailure(String errorMessage);
    }

    public void fetchBloodStock(StockCallback callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onFailure("User not logged in");
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("BloodPackets")
                .whereEqualTo("centerId", userId)
                .whereEqualTo("status", "AVAILABLE")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Integer> counts = new HashMap<>();
                        counts.put("A+", 0); counts.put("A-", 0); counts.put("B+", 0); counts.put("B-", 0);
                        counts.put("AB+", 0); counts.put("AB-", 0); counts.put("O+", 0); counts.put("O-", 0);
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String group = doc.getString("bloodGroup");
                            if (group != null && counts.containsKey(group)) counts.put(group, counts.get(group) + 1);
                        }
                        Map<String, String> results = new HashMap<>();
                        for (Map.Entry<String, Integer> entry : counts.entrySet()) results.put(entry.getKey(), String.valueOf(entry.getValue()));
                        callback.onSuccess(results);
                    } else {
                        callback.onFailure("Failed to fetch stock");
                    }
                });
    }
}