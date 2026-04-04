package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.List;
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
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("BloodPackets")
                .whereEqualTo("centerId", userId)
                .whereEqualTo("status", "AVAILABLE")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Integer> availableCounts = new HashMap<>();
                        String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
                        for (String g : groups) availableCounts.put(g, 0);

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String grp = doc.getString("bloodGroup");
                            if (grp != null && availableCounts.containsKey(grp)) {
                                availableCounts.put(grp, availableCounts.get(grp) + 1);
                            }
                        }

                        // Pending request walata assign karapu units adu kirima
                        db.collection("BloodRequests").get().addOnCompleteListener(reqTask -> {
                            if (reqTask.isSuccessful() && reqTask.getResult() != null) {
                                for (DocumentSnapshot d : reqTask.getResult()) {
                                    List<Map<String, Object>> assigned = (List<Map<String, Object>>) d.get("assignedBanks");
                                    if (assigned != null) {
                                        for (Map<String, Object> b : assigned) {
                                            if (userId.equals(b.get("bankId")) &&
                                                    ("Pending".equals(b.get("deliveryStatus")) || "Reserved".equals(b.get("deliveryStatus")))) {

                                                String bg = (String) b.get("bloodTypeProvided");
                                                Object q = b.get("unitsProvided");
                                                int units = (q instanceof Number) ? ((Number) q).intValue() : 0;

                                                if (bg != null && availableCounts.containsKey(bg)) {
                                                    int current = availableCounts.get(bg);
                                                    availableCounts.put(bg, Math.max(0, current - units));
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Map<String, String> results = new HashMap<>();
                            for (Map.Entry<String, Integer> entry : availableCounts.entrySet()) {
                                results.put(entry.getKey(), String.valueOf(entry.getValue()));
                            }
                            callback.onSuccess(results);
                        });

                    } else {
                        callback.onFailure("Error fetching blood stock");
                    }
                });
    }
}