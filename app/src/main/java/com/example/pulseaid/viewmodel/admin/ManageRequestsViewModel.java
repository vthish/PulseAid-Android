package com.example.pulseaid.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.admin.BloodRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ManageRequestsViewModel extends ViewModel {

    private MutableLiveData<List<BloodRequest>> pendingRequests = new MutableLiveData<>();
    private MutableLiveData<List<BloodRequest>> historyRequests = new MutableLiveData<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<BloodRequest>> getPendingRequests() {
        loadPendingRequests();
        return pendingRequests;
    }

    public LiveData<List<BloodRequest>> getHistoryRequests() {
        loadHistoryRequests();
        return historyRequests;
    }

    private void loadPendingRequests() {
        // Load both Pending and Broadcasted requests in the Pending Tab
        db.collection("BloodRequests")
                .whereIn("status", Arrays.asList("Pending", "Broadcasted"))
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<BloodRequest> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            BloodRequest req = doc.toObject(BloodRequest.class);
                            req.setId(doc.getId());
                            list.add(req);
                        }

                        Collections.sort(list, (r1, r2) -> Long.compare(r2.getRequestDate(), r1.getRequestDate()));

                        pendingRequests.setValue(list);
                    }
                });
    }

    private void loadHistoryRequests() {

        db.collection("BloodRequests")
                .whereIn("status", Arrays.asList("Resolved", "Approved", "Rejected"))
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<BloodRequest> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            BloodRequest req = doc.toObject(BloodRequest.class);
                            req.setId(doc.getId());
                            list.add(req);
                        }

                        Collections.sort(list, (r1, r2) -> Long.compare(r2.getRequestDate(), r1.getRequestDate()));

                        historyRequests.setValue(list);
                    }
                });
    }

    public void broadcastEmergencyAlert(BloodRequest request) {

        db.collection("BloodRequests").document(request.getId()).update("status", "Broadcasted");

        java.util.Map<String, Object> alert = new java.util.HashMap<>();
        alert.put("requestId", request.getId());
        alert.put("hospitalName", request.getHospitalName());
        alert.put("bloodGroup", request.getBloodGroup());
        alert.put("quantity", request.getQuantity());
        alert.put("urgency", request.getUrgency());
        alert.put("timestamp", System.currentTimeMillis());
        alert.put("expiresAt", System.currentTimeMillis() + (24 * 60 * 60 * 1000));

        db.collection("EmergencyAlerts").document(request.getId()).set(alert);
    }

    public void markAsResolved(String requestId) {
        db.collection("BloodRequests").document(requestId).update("status", "Resolved");
        db.collection("EmergencyAlerts").document(requestId).delete();
    }

    public void updateRequestStatus(String requestId, String newStatus) {
        if (requestId != null) {
            db.collection("BloodRequests").document(requestId)
                    .update("status", newStatus);
        }
    }
}