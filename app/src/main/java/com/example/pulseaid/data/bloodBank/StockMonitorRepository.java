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

    // Callback interface to send data back to ViewModel
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

        // Assuming you have a "BloodStock" collection or sub-collection for each Blood Bank
        db.collection("BloodStock").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, String> stockData = new HashMap<>();

                        if (document.exists()) {
                            // Extract values from Firestore (Ensure field names match exactly)
                            stockData.put("A+", getStockValue(document, "A_Pos"));
                            stockData.put("A-", getStockValue(document, "A_Neg"));
                            stockData.put("B+", getStockValue(document, "B_Pos"));
                            stockData.put("B-", getStockValue(document, "B_Neg"));
                            stockData.put("AB+", getStockValue(document, "AB_Pos"));
                            stockData.put("AB-", getStockValue(document, "AB_Neg"));
                            stockData.put("O+", getStockValue(document, "O_Pos"));
                            stockData.put("O-", getStockValue(document, "O_Neg"));
                        } else {
                            // If document doesn't exist, assume 0 for all
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

    // Helper method to safely convert objects to String counts
    private String getStockValue(DocumentSnapshot document, String field) {
        Object value = document.get(field);
        if (value != null) {
            return String.valueOf(value);
        }
        return "0";
    }
}