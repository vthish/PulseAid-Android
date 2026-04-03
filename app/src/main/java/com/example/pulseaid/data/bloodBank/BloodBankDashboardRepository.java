package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

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
        String currentDate = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);

        db.collection("BloodPackets")
                .whereEqualTo("centerId", userId) // Filter by centerId
                .whereEqualTo("status", "AVAILABLE")
                .get()
                .addOnCompleteListener(stockTask -> {
                    if (stockTask.isSuccessful() && stockTask.getResult() != null) {
                        int totalStockCount = stockTask.getResult().size();
                        AtomicInteger expireAlertsCount = new AtomicInteger(0);
                        long currentTime = System.currentTimeMillis();
                        long oneDayMillis = 24 * 60 * 60 * 1000L;

                        for (QueryDocumentSnapshot doc : stockTask.getResult()) {
                            Long expiryTimestamp = doc.getLong("expiryTimestamp");
                            if (expiryTimestamp != null) {
                                long diff = expiryTimestamp - currentTime;
                                int daysLeft = (int) (diff / oneDayMillis);
                                if (daysLeft >= 0 && daysLeft <= 7) {
                                    expireAlertsCount.incrementAndGet();
                                }
                            }
                        }

                        db.collection("appointments")
                                .whereEqualTo("centerId", userId)
                                .whereEqualTo("date", currentDate)
                                .whereEqualTo("status", "PENDING")
                                .get()
                                .addOnCompleteListener(apptTask -> {
                                    int todayPending = (apptTask.isSuccessful() && apptTask.getResult() != null) ? apptTask.getResult().size() : 0;
                                    callback.onSuccess(totalStockCount, 0, todayPending, expireAlertsCount.get());
                                });
                    } else {
                        callback.onFailure("Failed to load dashboard data");
                    }
                });
    }
}