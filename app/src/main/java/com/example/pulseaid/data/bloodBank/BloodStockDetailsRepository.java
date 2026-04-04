package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BloodStockDetailsRepository {

    public interface DetailsCallback {
        void onSuccess(Map<String, String> compatibilityInfo, List<BloodPacket> packets);
        void onFailure(String error);
    }

    public void fetchDetails(String bloodGroup, DetailsCallback callback) {
        Map<String, String> info = getCompatibilityInfo(bloodGroup);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            callback.onFailure("User not authenticated");
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        long oneDay = 24 * 60 * 60 * 1000L;
        long now = System.currentTimeMillis();

        db.collection("BloodRequests").get().addOnCompleteListener(reqTask -> {
            if (!reqTask.isSuccessful()) {
                callback.onFailure("Error fetching pending requests");
                return;
            }

            int pendingUnitsToDeduct = 0;
            for (DocumentSnapshot doc : reqTask.getResult()) {
                List<Map<String, Object>> assigned = (List<Map<String, Object>>) doc.get("assignedBanks");
                if (assigned != null) {
                    for (Map<String, Object> bank : assigned) {
                        if (userId.equals(bank.get("bankId")) && "Pending".equals(bank.get("deliveryStatus"))) {
                            String bg = (String) bank.get("bloodTypeProvided");
                            if (bloodGroup.equals(bg)) {
                                Object q = bank.get("unitsProvided");
                                if (q instanceof Long) pendingUnitsToDeduct += ((Long) q).intValue();
                                else if (q instanceof Integer) pendingUnitsToDeduct += (Integer) q;
                            }
                        }
                    }
                }
            }

            final int finalPendingDeduction = pendingUnitsToDeduct;

            db.collection("BloodPackets")
                    .whereEqualTo("centerId", userId)
                    .whereEqualTo("bloodGroup", bloodGroup)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<BloodPacket> validPackets = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String status = doc.getString("status");
                            if (status != null && status.equalsIgnoreCase("Available")) {
                                Long expTime = doc.getLong("expiryTimestamp");
                                if (expTime != null) {
                                    long diff = expTime - now;

                                    if (diff > oneDay) {
                                        String packetId = doc.getString("packetId");
                                        String bg = doc.getString("bloodGroup");
                                        Long colTime = doc.getLong("collectionTimestamp");

                                        String colDate = colTime != null ? sdf.format(new Date(colTime)) : "N/A";
                                        String expDate = sdf.format(new Date(expTime));

                                        validPackets.add(new BloodPacket(packetId, bg, colDate, expDate, status, expTime));
                                    }
                                }
                            }
                        }

                        Collections.sort(validPackets, (p1, p2) -> Long.compare(p1.expTimestamp, p2.expTimestamp));

                        if (finalPendingDeduction > 0 && finalPendingDeduction <= validPackets.size()) {
                            validPackets.subList(0, finalPendingDeduction).clear();
                        } else if (finalPendingDeduction > validPackets.size()) {
                            validPackets.clear();
                        }

                        callback.onSuccess(info, validPackets);
                    })
                    .addOnFailureListener(e -> callback.onFailure("Failed to load details"));
        });
    }

    private Map<String, String> getCompatibilityInfo(String bg) {
        Map<String, String> info = new HashMap<>();
        String donateTo = "";
        String receiveFrom = "";

        switch (bg) {
            case "A+": donateTo = "A+, AB+"; receiveFrom = "A+, A-, O+, O-"; break;
            case "A-": donateTo = "A+, A-, AB+, AB-"; receiveFrom = "A-, O-"; break;
            case "B+": donateTo = "B+, AB+"; receiveFrom = "B+, B-, O+, O-"; break;
            case "B-": donateTo = "B+, B-, AB+, AB-"; receiveFrom = "B-, O-"; break;
            case "AB+": donateTo = "AB+"; receiveFrom = "Everyone (Universal Recipient)"; break;
            case "AB-": donateTo = "AB+, AB-"; receiveFrom = "AB-, A-, B-, O-"; break;
            case "O+": donateTo = "O+, A+, B+, AB+"; receiveFrom = "O+, O-"; break;
            case "O-": donateTo = "Everyone (Universal Donor)"; receiveFrom = "O-"; break;
            default: donateTo = "Unknown"; receiveFrom = "Unknown";
        }
        info.put("donateTo", donateTo);
        info.put("receiveFrom", receiveFrom);
        return info;
    }

    public static class BloodPacket {
        public String packetId, bloodGroup, collectionDate, expiryDate, status;
        public long expTimestamp;

        public BloodPacket(String packetId, String bloodGroup, String collectionDate, String expiryDate, String status, long expTimestamp) {
            this.packetId = packetId;
            this.bloodGroup = bloodGroup;
            this.collectionDate = collectionDate;
            this.expiryDate = expiryDate;
            this.status = status;
            this.expTimestamp = expTimestamp;
        }
    }
}