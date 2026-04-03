package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExpiringAlertsRepository {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public ExpiringAlertsRepository() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public interface AlertsCallback {
        void onSuccess(List<AlertItem> alerts);
        void onFailure(String error);
    }

    public void fetchExpiringAlerts(AlertsCallback callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onFailure("User not logged in");
            return;
        }

        String bankId = mAuth.getCurrentUser().getUid();

        db.collection("BloodPackets")
                .whereEqualTo("center Id", bankId)
                .whereEqualTo("status", "AVAILABLE")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<AlertItem> alertList = new ArrayList<>();
                        long currentTime = System.currentTimeMillis();
                        long oneDayMillis = 24 * 60 * 60 * 1000L;

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Long expiryTimestamp = doc.getLong("expiryTimestamp");

                            if (expiryTimestamp != null) {
                                long diff = expiryTimestamp - currentTime;
                                int daysLeft = (int) (diff / oneDayMillis);

                                if (daysLeft >= 0 && daysLeft <= 7) {
                                    String packetId = doc.getString("packetId");
                                    if (packetId == null) {
                                        packetId = doc.getId();
                                    }
                                    String bloodGroup = doc.getString("bloodGroup");
                                    alertList.add(new AlertItem(packetId, bloodGroup, daysLeft));
                                }
                            }
                        }
                        callback.onSuccess(alertList);
                    } else {
                        callback.onFailure("Failed to fetch expiring alerts");
                    }
                });
    }

    public static class AlertItem {
        public String packetId, bloodGroup, daysLeftText;
        public int daysLeftValue;

        public AlertItem(String packetId, String bloodGroup, int daysLeftValue) {
            this.packetId = packetId;
            this.bloodGroup = bloodGroup;
            this.daysLeftValue = daysLeftValue;
            this.daysLeftText = daysLeftValue + " Days";
        }
    }
}