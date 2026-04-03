package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Fetching from 'Users' collection
        db.collection("Users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, String> stockData = new HashMap<>();


                        if (document.exists() && document.contains("inventory")) {
                            Map<String, Object> inventory = (Map<String, Object>) document.get("inventory");

                            stockData.put("A+", getStockValue(inventory, "A+"));
                            stockData.put("A-", getStockValue(inventory, "A-"));
                            stockData.put("B+", getStockValue(inventory, "B+"));
                            stockData.put("B-", getStockValue(inventory, "B-"));
                            stockData.put("AB+", getStockValue(inventory, "AB+"));
                            stockData.put("AB-", getStockValue(inventory, "AB-"));
                            stockData.put("O+", getStockValue(inventory, "O+"));
                            stockData.put("O-", getStockValue(inventory, "O-"));
                        } else {
                            stockData.put("A+", "0"); stockData.put("A-", "0");
                            stockData.put("B+", "0"); stockData.put("B-", "0");
                            stockData.put("AB+", "0"); stockData.put("AB-", "0");
                            stockData.put("O+", "0"); stockData.put("O-", "0");
                        }
                        callback.onSuccess(stockData);
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Failed to fetch stock");
                    }
                });
    }

    private String getStockValue(Map<String, Object> inventory, String field) {
        if (inventory != null && inventory.containsKey(field)) {
            return String.valueOf(inventory.get(field));
        }
        return "0";
    }
}