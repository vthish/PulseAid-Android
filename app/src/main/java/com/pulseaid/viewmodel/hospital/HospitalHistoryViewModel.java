package com.pulseaid.viewmodel.hospital;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pulseaid.data.hospital.HospitalActiveRequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HospitalHistoryViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public MutableLiveData<List<HospitalActiveRequestModel>> historyRequests =
            new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public void listenToHistoryRequests() {
        if (auth.getCurrentUser() == null) {
            isLoading.setValue(false);
            historyRequests.setValue(new ArrayList<>());
            return;
        }

        String currentHospitalId = auth.getCurrentUser().getUid();
        isLoading.setValue(true);

        db.collection("BloodRequests")
                .whereEqualTo("hospitalId", currentHospitalId)
                .whereEqualTo("status", "Completed")
                .addSnapshotListener((value, error) -> {
                    isLoading.setValue(false);

                    if (error != null || value == null) {
                        historyRequests.setValue(new ArrayList<>());
                        return;
                    }

                    List<HospitalActiveRequestModel> requests = new ArrayList<>();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        HospitalActiveRequestModel model =
                                doc.toObject(HospitalActiveRequestModel.class);

                        if (model != null) {
                            model.setRequestId(doc.getId());
                            requests.add(model);
                        }
                    }

                    Collections.sort(requests, (o1, o2) -> {
                        long d1 = o1 != null ? o1.getRequestDate() : 0L;
                        long d2 = o2 != null ? o2.getRequestDate() : 0L;
                        return Long.compare(d2, d1);
                    });

                    historyRequests.setValue(requests);
                });
    }
}