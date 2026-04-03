package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class StockMonitorRepository {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public StockMonitorRepository() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
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

                        Map<String, Integer> stockCounts = new HashMap<>();
                        stockCounts.put("A+", 0); stockCounts.put("A-", 0);
                        stockCounts.put("B+", 0); stockCounts.put("B-", 0);
                        stockCounts.put("AB+", 0); stockCounts.put("AB-", 0);
                        stockCounts.put("O+", 0); stockCounts.put("O-", 0);

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String bloodGroup = doc.getString("bloodGroup");
                            if (bloodGroup != null && stockCounts.containsKey(bloodGroup)) {
                                stockCounts.put(bloodGroup, stockCounts.get(bloodGroup) + 1);
                            }
                        }

                        Map<String, String> stockDataString = new HashMap<>();
                        for (Map.Entry<String, Integer> entry : stockCounts.entrySet()) {
                            stockDataString.put(entry.getKey(), String.valueOf(entry.getValue()));
                        }

                        callback.onSuccess(stockDataString);
                    } else {
                        callback.onFailure("Failed to fetch stock data");
                    }
                });
    }
}