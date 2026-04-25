package com.pulseaid.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.pulseaid.data.admin.BloodRequest;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
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

    public LiveData<List<BloodRequest>> getPendingRequests() { loadPendingRequests(); return pendingRequests; }
    public LiveData<List<BloodRequest>> getHistoryRequests() { loadHistoryRequests(); return historyRequests; }

    private void loadPendingRequests() {
        db.collection("EmergencyRequests")
                .whereIn("status", Arrays.asList("Pending", "Broadcasted"))
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<BloodRequest> list = new ArrayList<>();
                        List<Task<DocumentSnapshot>> nameTasks = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            BloodRequest req = new BloodRequest();
                            req.setId(doc.getId());
                            String centerId = doc.getString("centerId");
                            req.setHospitalId(centerId);
                            Task<DocumentSnapshot> nameTask = db.collection("Users").document(centerId).get();
                            nameTasks.add(nameTask.addOnSuccessListener(userDoc -> {
                                String name = userDoc.getString("name");
                                if (name == null) name = userDoc.getString("institutionName");
                                req.setHospitalName(name != null ? name : "Unknown Blood Bank");
                            }));
                            String bg = doc.getString("bloodGroup");
                            if (bg == null) bg = doc.getString("bloodType");
                            req.setBloodGroup(bg != null ? bg : "N/A");
                            Long qty = doc.getLong("quantity");
                            if (qty == null) qty = doc.getLong("units");
                            req.setQuantity(qty != null ? qty.intValue() : 0);
                            req.setUrgency("Emergency");
                            req.setReason(doc.getString("reason"));
                            req.setStatus(doc.getString("status"));
                            req.setRequestDate(doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0);
                            list.add(req);
                        }
                        Tasks.whenAllComplete(nameTasks).addOnCompleteListener(t -> {
                            Collections.sort(list, (r1, r2) -> Long.compare(r2.getRequestDate(), r1.getRequestDate()));
                            pendingRequests.setValue(list);
                        });
                    }
                });
    }

    private void loadHistoryRequests() {
        db.collection("EmergencyRequests")
                .whereIn("status", Arrays.asList("Resolved", "Approved", "Rejected"))
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<BloodRequest> list = new ArrayList<>();
                        List<Task<DocumentSnapshot>> nameTasks = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            BloodRequest req = new BloodRequest();
                            req.setId(doc.getId());
                            String centerId = doc.getString("centerId");
                            req.setHospitalId(centerId);
                            Task<DocumentSnapshot> nameTask = db.collection("Users").document(centerId).get();
                            nameTasks.add(nameTask.addOnSuccessListener(userDoc -> {
                                String name = userDoc.getString("name");
                                if (name == null) name = userDoc.getString("institutionName");
                                req.setHospitalName(name != null ? name : "Unknown Blood Bank");
                            }));
                            String bg = doc.getString("bloodGroup");
                            if (bg == null) bg = doc.getString("bloodType");
                            req.setBloodGroup(bg != null ? bg : "N/A");
                            Long qty = doc.getLong("quantity");
                            if (qty == null) qty = doc.getLong("units");
                            req.setQuantity(qty != null ? qty.intValue() : 0);
                            req.setUrgency("Emergency");
                            req.setReason(doc.getString("reason"));
                            req.setStatus(doc.getString("status"));
                            req.setRequestDate(doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0);
                            list.add(req);
                        }
                        Tasks.whenAllComplete(nameTasks).addOnCompleteListener(t -> {
                            Collections.sort(list, (r1, r2) -> Long.compare(r2.getRequestDate(), r1.getRequestDate()));
                            historyRequests.setValue(list);
                        });
                    }
                });
    }

    public void resolveRequest(BloodRequest request) {
        db.collection("EmergencyRequests").document(request.getId()).update("status", "Resolved");
        db.collection("EmergencyAlerts").document(request.getId()).delete();
    }
}