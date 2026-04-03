package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Map;

public class BloodBankDashboardRepository {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public BloodBankDashboardRepository() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public interface DashboardStatsCallback {
        void onSuccess(int totalStock, int pendingOrders, int todayAppointments, int expireAlerts);
        void onFailure(String error);
    }

    public void fetchDashboardStats(DashboardStatsCallback callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onFailure("User not logged in");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        Calendar cal = Calendar.getInstance();
        String currentDate = cal.get(Calendar.YEAR) + "-" +
                (cal.get(Calendar.MONTH) + 1) + "-" +
                cal.get(Calendar.DAY_OF_MONTH);

        // Fetching from 'Users' collection now
        db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot doc = task.getResult();
                int totalStock = 0;

                // Getting the nested 'inventory' map
                if (doc.exists() && doc.contains("inventory")) {
                    Map<String, Object> inventory = (Map<String, Object>) doc.get("inventory");

                    totalStock += getIntValue(inventory, "A+");
                    totalStock += getIntValue(inventory, "A-");
                    totalStock += getIntValue(inventory, "B+");
                    totalStock += getIntValue(inventory, "B-");
                    totalStock += getIntValue(inventory, "AB+");
                    totalStock += getIntValue(inventory, "AB-");
                    totalStock += getIntValue(inventory, "O+");
                    totalStock += getIntValue(inventory, "O-");
                }

                final int finalTotalStock = totalStock;

                db.collection("appointments")
                        .whereEqualTo("centerId", userId)
                        .whereEqualTo("date", currentDate)
                        .whereEqualTo("status", "PENDING")
                        .get()
                        .addOnCompleteListener(apptTask -> {
                            int todayPendingAppointmentsCount = 0;
                            if (apptTask.isSuccessful() && apptTask.getResult() != null) {
                                todayPendingAppointmentsCount = apptTask.getResult().size();
                            }

                            int pendingOrders = 0;
                            int expireAlerts = 0;

                            callback.onSuccess(finalTotalStock, pendingOrders, todayPendingAppointmentsCount, expireAlerts);
                        });
            } else {
                callback.onFailure("Failed to load dashboard data");
            }
        });
    }

    private int getIntValue(Map<String, Object> inventory, String field) {
        if (inventory != null && inventory.containsKey(field)) {
            try {
                return Integer.parseInt(String.valueOf(inventory.get(field)));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}