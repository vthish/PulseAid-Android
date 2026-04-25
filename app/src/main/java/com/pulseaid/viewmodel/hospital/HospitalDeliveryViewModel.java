package com.pulseaid.viewmodel.hospital;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pulseaid.data.hospital.HospitalActiveRequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HospitalDeliveryViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public MutableLiveData<List<HospitalActiveRequestModel>> dispatchedRequests =
            new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public void listenToDispatchedRequests() {
        if (auth.getCurrentUser() == null) {
            isLoading.setValue(false);
            dispatchedRequests.setValue(new ArrayList<>());
            return;
        }

        String currentHospitalId = auth.getCurrentUser().getUid();
        isLoading.setValue(true);

        db.collection("BloodRequests")
                .whereEqualTo("hospitalId", currentHospitalId)
                .addSnapshotListener((value, error) -> {
                    isLoading.setValue(false);

                    if (error != null || value == null) {
                        dispatchedRequests.setValue(new ArrayList<>());
                        return;
                    }

                    List<HospitalActiveRequestModel> requests = new ArrayList<>();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        HospitalActiveRequestModel model = doc.toObject(HospitalActiveRequestModel.class);
                        if (model == null) {
                            continue;
                        }

                        model.setRequestId(doc.getId());

                        List<Map<String, Object>> banks = model.getAssignedBanks();
                        String computedStatus = computeOverallStatus(banks);
                        String currentStatus = safeString(model.getStatus());

                        Long currentCompletedDate = model.getCompletedDate();
                        boolean shouldHaveCompletedDate = "Completed".equalsIgnoreCase(computedStatus);
                        boolean hasCompletedDate = currentCompletedDate != null && currentCompletedDate > 0;

                        boolean needsStatusUpdate = !computedStatus.equalsIgnoreCase(currentStatus);
                        boolean needsCompletedDateAdd = shouldHaveCompletedDate && !hasCompletedDate;
                        boolean needsCompletedDateRemove = !shouldHaveCompletedDate && hasCompletedDate;

                        if (needsStatusUpdate || needsCompletedDateAdd || needsCompletedDateRemove) {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("status", computedStatus);

                            if (shouldHaveCompletedDate) {
                                updates.put("completedDate", System.currentTimeMillis());
                            } else {
                                updates.put("completedDate", null);
                            }

                            db.collection("BloodRequests")
                                    .document(doc.getId())
                                    .update(updates);
                        }

                        if ("Dispatched".equalsIgnoreCase(computedStatus)) {
                            model.setStatus(computedStatus);
                            requests.add(model);
                        }
                    }

                    dispatchedRequests.setValue(requests);
                });
    }

    public void updateBankDeliveryStatus(String requestId, List<Map<String, Object>> updatedBanks) {
        if (requestId == null || requestId.trim().isEmpty() || updatedBanks == null || updatedBanks.isEmpty()) {
            return;
        }

        String overallStatus = computeOverallStatus(updatedBanks);

        Map<String, Object> updates = new HashMap<>();
        updates.put("assignedBanks", updatedBanks);
        updates.put("status", overallStatus);

        if ("Completed".equalsIgnoreCase(overallStatus)) {
            updates.put("completedDate", System.currentTimeMillis());
        } else {
            updates.put("completedDate", null);
        }

        db.collection("BloodRequests")
                .document(requestId)
                .update(updates);
    }

    private String computeOverallStatus(List<Map<String, Object>> banks) {
        if (banks == null || banks.isEmpty()) {
            return "Pending";
        }

        boolean allDelivered = true;
        boolean allDispatchedOrDelivered = true;

        for (Map<String, Object> bank : banks) {
            if (bank == null) {
                allDelivered = false;
                allDispatchedOrDelivered = false;
                continue;
            }

            String deliveryStatus = safeString(bank.get("deliveryStatus"));

            if (!"Delivered".equalsIgnoreCase(deliveryStatus)) {
                allDelivered = false;
            }

            if (!"Dispatched".equalsIgnoreCase(deliveryStatus)
                    && !"Delivered".equalsIgnoreCase(deliveryStatus)) {
                allDispatchedOrDelivered = false;
            }
        }

        if (allDelivered) {
            return "Completed";
        }

        if (allDispatchedOrDelivered) {
            return "Dispatched";
        }

        return "Pending";
    }

    private String safeString(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value).trim();
        return "null".equalsIgnoreCase(text) ? "" : text;
    }
}