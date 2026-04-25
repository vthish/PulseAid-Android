package com.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
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
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        long now = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000L;
        long sevenDays = 7 * oneDay;

        db.collection("BloodPackets")
                .whereEqualTo("centerId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        WriteBatch batch = db.batch();
                        boolean needsUpdate = false;
                        int validStock = 0;
                        int alerts = 0;

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String status = doc.getString("status");
                            Long exp = doc.getLong("expiryTimestamp");

                            if (exp != null && ("AVAILABLE".equals(status) || "EXPIRED".equals(status))) {
                                long diff = exp - now;

                                if (diff <= oneDay && "AVAILABLE".equals(status)) {
                                    batch.update(doc.getReference(), "status", "EXPIRED");
                                    needsUpdate = true;
                                }

                                if ("AVAILABLE".equals(status)) validStock++;
                                if (diff > 0 && diff <= sevenDays) alerts++;
                            }
                        }
                        if (needsUpdate) batch.commit();

                        final int finalStock = validStock;
                        final int finalAlerts = alerts;

                        Calendar cal = Calendar.getInstance();
                        String today = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);

                        db.collection("appointments")
                                .whereEqualTo("centerId", userId)
                                .whereEqualTo("date", today)
                                .whereEqualTo("status", "PENDING")
                                .get()
                                .addOnCompleteListener(apptTask -> {
                                    int appts = (apptTask.isSuccessful() && apptTask.getResult() != null) ? apptTask.getResult().size() : 0;
                                    db.collection("BloodRequests").get().addOnCompleteListener(reqTask -> {
                                        int pending = 0;
                                        int reserved = 0;
                                        if (reqTask.isSuccessful() && reqTask.getResult() != null) {
                                            for (DocumentSnapshot d : reqTask.getResult()) {
                                                List<Map<String, Object>> assigned = (List<Map<String, Object>>) d.get("assignedBanks");
                                                if (assigned != null) {
                                                    for (Map<String, Object> b : assigned) {
                                                        if (userId.equals(b.get("bankId")) && ("Pending".equals(b.get("deliveryStatus")) || "Reserved".equals(b.get("deliveryStatus")))) {
                                                            pending++;
                                                            reserved += ((Number) b.get("unitsProvided")).intValue();
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        callback.onSuccess(Math.max(0, finalStock - reserved), pending, appts, finalAlerts);
                                    });
                                });
                    } else callback.onFailure("Error");
                });
    }
}