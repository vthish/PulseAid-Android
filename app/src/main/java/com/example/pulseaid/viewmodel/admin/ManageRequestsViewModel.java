package com.example.pulseaid.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.admin.BloodRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
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
        db.collection("BloodRequests")
                .whereEqualTo("status", "Pending")
                .orderBy("requestDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<BloodRequest> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            BloodRequest req = doc.toObject(BloodRequest.class);
                            req.setId(doc.getId());
                            list.add(req);
                        }
                        pendingRequests.setValue(list);
                    }
                });
    }

    private void loadHistoryRequests() {

        db.collection("BloodRequests")
                .whereIn("status", Arrays.asList("Approved", "Rejected"))
                .orderBy("requestDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<BloodRequest> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            BloodRequest req = doc.toObject(BloodRequest.class);
                            req.setId(doc.getId());
                            list.add(req);
                        }
                        historyRequests.setValue(list);
                    }
                });
    }

    //Admin aprv or rejct karama stts ek updt krn methd ek
    public void updateRequestStatus(String requestId, String newStatus) {
        if (requestId != null) {
            db.collection("BloodRequests").document(requestId)
                    .update("status", newStatus);
        }
    }
}