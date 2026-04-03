package com.example.pulseaid.data.bloodBank;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HospitalRequestRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public HospitalRequestRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public interface RequestCallback {
        void onSuccess(List<HospitalRequest> requests);
        void onFailure(String errorMessage);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void fetchHospitalRequests(RequestCallback callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onFailure("User not logged in");
            return;
        }
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("BloodRequests")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HospitalRequest> requestList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            List<Map<String, Object>> assignedBanks = (List<Map<String, Object>>) document.get("assignedBanks");
                            if (assignedBanks != null) {
                                for (Map<String, Object> bankMap : assignedBanks) {
                                    String bankId = (String) bankMap.get("bankId");
                                    String deliveryStatus = (String) bankMap.get("deliveryStatus");
                                    if (currentUserId.equals(bankId) && "Pending".equals(deliveryStatus)) {
                                        String id = document.getId();
                                        String hospitalName = document.getString("hospitalName");
                                        String bloodGroup = (String) bankMap.get("bloodTypeProvided");
                                        long units = 0;
                                        Object unitsObj = bankMap.get("unitsProvided");
                                        if (unitsObj instanceof Long) units = (Long) unitsObj;
                                        else if (unitsObj instanceof Integer) units = (Integer) unitsObj;
                                        requestList.add(new HospitalRequest(id, hospitalName, bloodGroup, (int)units));
                                    }
                                }
                            }
                        }
                        callback.onSuccess(requestList);
                    } else {
                        callback.onFailure("Error fetching requests");
                    }
                });
    }

    public void confirmBloodUnits(String requestId, String bloodGroup, int unitsToShift, ActionCallback callback) {
        if (mAuth.getCurrentUser() == null) return;
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("BloodPackets")
                .whereEqualTo("centerId", currentUserId)
                .whereEqualTo("bloodGroup", bloodGroup)
                .whereEqualTo("status", "AVAILABLE")
                .limit(unitsToShift)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() < unitsToShift) {
                        callback.onFailure("Not enough AVAILABLE stock!");
                        return;
                    }
                    WriteBatch batch = db.batch();
                    long currentTime = System.currentTimeMillis();
                    for (DocumentSnapshot packetDoc : queryDocumentSnapshots) {
                        batch.update(packetDoc.getReference(), "status", "Dispatched");
                        batch.update(packetDoc.getReference(), "dispatchedDate", currentTime);
                    }
                    DocumentReference requestRef = db.collection("BloodRequests").document(requestId);
                    requestRef.get().addOnSuccessListener(requestDoc -> {
                        List<Map<String, Object>> assignedBanks = (List<Map<String, Object>>) requestDoc.get("assignedBanks");
                        if (assignedBanks != null) {
                            for (Map<String, Object> bankMap : assignedBanks) {
                                if (currentUserId.equals(bankMap.get("bankId"))) {
                                    bankMap.put("deliveryStatus", "Dispatched");
                                    bankMap.put("dispatchedDate", currentTime);
                                }
                            }
                            batch.update(requestRef, "assignedBanks", assignedBanks);
                            batch.commit()
                                    .addOnSuccessListener(v -> callback.onSuccess())
                                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public static class HospitalRequest {
        public String id, name, type;
        public int qty;
        public HospitalRequest(String id, String name, String type, int qty) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.qty = qty;
        }
    }
}