package com.pulseaid.data.bloodBank;

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
        if (mAuth.getCurrentUser() == null) return;
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("BloodRequests").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<HospitalRequest> list = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    List<Map<String, Object>> assigned = (List<Map<String, Object>>) doc.get("assignedBanks");
                    if (assigned != null) {
                        for (Map<String, Object> bank : assigned) {
                            if (currentUserId.equals(bank.get("bankId")) && "Pending".equals(bank.get("deliveryStatus"))) {
                                String urgency = doc.getString("urgency");
                                if (urgency == null) urgency = "Normal";
                                long units = 0;
                                Object q = bank.get("unitsProvided");
                                if (q instanceof Long) units = (Long) q;
                                else if (q instanceof Integer) units = (Integer) q;
                                list.add(new HospitalRequest(doc.getId(), doc.getString("hospitalName"), (String)bank.get("bloodTypeProvided"), (int)units, urgency));
                            }
                        }
                    }
                }
                callback.onSuccess(list);
            } else callback.onFailure("Error");
        });
    }

    public void confirmBloodUnits(String requestId, String bloodGroup, int qty, ActionCallback callback) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("BloodPackets")
                .whereEqualTo("centerId", currentUserId)
                .whereEqualTo("bloodGroup", bloodGroup)
                .whereEqualTo("status", "AVAILABLE")
                .limit(qty)
                .get()
                .addOnSuccessListener(snaps -> {
                    if (snaps.size() < qty) {
                        callback.onFailure("Insufficient stock!");
                        return;
                    }
                    WriteBatch batch = db.batch();
                    long currentTime = System.currentTimeMillis();
                    for (DocumentSnapshot d : snaps) {
                        batch.update(d.getReference(), "status", "Dispatched");
                        batch.update(d.getReference(), "dispatchedDate", currentTime);
                    }
                    DocumentReference ref = db.collection("BloodRequests").document(requestId);
                    ref.get().addOnSuccessListener(doc -> {
                        List<Map<String, Object>> banks = (List<Map<String, Object>>) doc.get("assignedBanks");
                        if (banks != null) {
                            for (Map<String, Object> b : banks) {
                                if (currentUserId.equals(b.get("bankId"))) {
                                    b.put("deliveryStatus", "Dispatched");
                                    b.put("dispatchedDate", currentTime);
                                }
                            }
                            batch.update(ref, "assignedBanks", banks);
                            batch.commit().addOnSuccessListener(v -> callback.onSuccess());
                        }
                    });
                });
    }

    public static class HospitalRequest {
        public String id, name, type, urgency;
        public int qty;
        public HospitalRequest(String id, String name, String type, int qty, String urgency) {
            this.id = id; this.name = name; this.type = type; this.qty = qty; this.urgency = urgency;
        }
    }
}