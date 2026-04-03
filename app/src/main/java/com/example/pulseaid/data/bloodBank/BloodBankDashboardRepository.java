package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class BloodBankDashboardRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public BloodBankDashboardRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
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
        String currentDate = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);

        db.collection("BloodPackets")
                .whereEqualTo("centerId", userId)
                .whereEqualTo("status", "AVAILABLE")
                .get()
                .addOnCompleteListener(stockTask -> {
                    if (stockTask.isSuccessful() && stockTask.getResult() != null) {
                        int totalStockCount = stockTask.getResult().size();
                        int expireAlertsCount = 0;
                        long currentTime = System.currentTimeMillis();
                        long oneDayMillis = 24 * 60 * 60 * 1000L;
                        for (QueryDocumentSnapshot doc : stockTask.getResult()) {
                            Long expiry = doc.getLong("expiryTimestamp");
                            if (expiry != null) {
                                long diff = expiry - currentTime;
                                int daysLeft = (int) (diff / oneDayMillis);
                                if (daysLeft >= 0 && daysLeft <= 7) expireAlertsCount++;
                            }
                        }
                        final int finalExpire = expireAlertsCount;
                        db.collection("appointments")
                                .whereEqualTo("centerId", userId)
                                .whereEqualTo("date", currentDate)
                                .whereEqualTo("status", "PENDING")
                                .get()
                                .addOnCompleteListener(apptTask -> {
                                    int appointments = (apptTask.isSuccessful() && apptTask.getResult() != null) ? apptTask.getResult().size() : 0;
                                    db.collection("BloodRequests")
                                            .get()
                                            .addOnCompleteListener(reqTask -> {
                                                int pending = 0;
                                                if (reqTask.isSuccessful() && reqTask.getResult() != null) {
                                                    for (DocumentSnapshot doc : reqTask.getResult()) {
                                                        List<Map<String, Object>> assigned = (List<Map<String, Object>>) doc.get("assignedBanks");
                                                        if (assigned != null) {
                                                            for (Map<String, Object> bank : assigned) {
                                                                if (userId.equals(bank.get("bankId")) && "Pending".equals(bank.get("deliveryStatus"))) {
                                                                    pending++; break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                callback.onSuccess(totalStockCount, pending, appointments, finalExpire);
                                            });
                                });
                    } else {
                        callback.onFailure("Error loading dashboard data");
                    }
                });
    }
}