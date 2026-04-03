package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ExpiringAlertsRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public ExpiringAlertsRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public interface AlertsCallback {
        void onSuccess(List<AlertItem> alerts);
        void onFailure(String error);
    }

    public void fetchExpiringAlerts(AlertsCallback callback) {
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
                        List<AlertItem> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String status = doc.getString("status");
                            Long exp = doc.getLong("expiryTimestamp");

                            if (exp != null && ("AVAILABLE".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status))) {
                                long diff = exp - now;

                                if (diff <= sevenDays) {
                                    String timeText;
                                    int days = (int) (diff / oneDay);

                                    if (diff <= 0) {
                                        timeText = "EXPIRED";
                                        days = -1;
                                    } else if (days >= 1) {
                                        timeText = days + " Days Left";
                                    } else {
                                        long hours = diff / (60 * 60 * 1000L);
                                        timeText = hours + " Hours Left";
                                    }

                                    list.add(new AlertItem(
                                            doc.getString("bloodGroup"),
                                            doc.getString("packetId"),
                                            timeText,
                                            days
                                    ));
                                }
                            }
                        }
                        callback.onSuccess(list);
                    } else callback.onFailure("Error loading alerts");
                });
    }

    public static class AlertItem {
        public String bloodGroup, packetId, daysLeftText;
        public int daysLeftValue;
        public AlertItem(String bloodGroup, String packetId, String daysLeftText, int daysLeftValue) {
            this.bloodGroup = bloodGroup;
            this.packetId = packetId;
            this.daysLeftText = daysLeftText;
            this.daysLeftValue = daysLeftValue;
        }
    }
}