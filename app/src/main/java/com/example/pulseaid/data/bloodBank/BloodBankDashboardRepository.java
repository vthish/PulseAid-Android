package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

        db.collection("BloodStock").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot doc = task.getResult();
                int totalStock = 0;

                if (doc.exists()) {
                    // Summing up all the available blood units
                    totalStock += getIntValue(doc, "A_Pos");
                    totalStock += getIntValue(doc, "A_Neg");
                    totalStock += getIntValue(doc, "B_Pos");
                    totalStock += getIntValue(doc, "B_Neg");
                    totalStock += getIntValue(doc, "AB_Pos");
                    totalStock += getIntValue(doc, "AB_Neg");
                    totalStock += getIntValue(doc, "O_Pos");
                    totalStock += getIntValue(doc, "O_Neg");
                }

                // TODO: Replace these with actual Firestore queries for respective collections
                // Currently set to 0 to remove mock data from the UI
                int pendingOrders = 0;
                int todayAppointments = 0;
                int expireAlerts = 0;

                callback.onSuccess(totalStock, pendingOrders, todayAppointments, expireAlerts);
            } else {
                callback.onFailure("Failed to load dashboard data");
            }
        });
    }

    // Helper method to safely parse Firestore objects to integer
    private int getIntValue(DocumentSnapshot doc, String field) {
        Object value = doc.get(field);
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}