package com.pulseaid.viewmodel.hospital;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.pulseaid.data.hospital.HospitalActiveRequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HospitalActiveRequestsViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public MutableLiveData<List<HospitalActiveRequestModel>> activeRequests = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public void listenToHospitalRequests() {
        if (auth.getCurrentUser() == null) {
            isLoading.setValue(false);
            activeRequests.setValue(new ArrayList<>());
            Log.e("HospitalRequests", "Current user is null");
            return;
        }

        String currentHospitalId = auth.getCurrentUser().getUid();
        Log.d("HospitalRequests", "Listening for hospitalId: " + currentHospitalId);
        isLoading.setValue(true);

        db.collection("BloodRequests")
                .whereEqualTo("hospitalId", currentHospitalId)
                .whereIn("status", Arrays.asList("Pending", "Accepted", "Dispatched"))
                .addSnapshotListener((value, error) -> {
                    isLoading.setValue(false);

                    if (error != null) {
                        Log.e("HospitalRequests", "Firestore error: ", error);
                        activeRequests.setValue(new ArrayList<>());
                        return;
                    }

                    if (value != null) {
                        List<HospitalActiveRequestModel> requests = new ArrayList<>();
                        for (var doc : value.getDocuments()) {
                            try {
                                HospitalActiveRequestModel model = doc.toObject(HospitalActiveRequestModel.class);
                                if (model != null) {
                                    requests.add(model);
                                }
                            } catch (Exception e) {
                                Log.e("HospitalRequests", "Error parsing doc: " + doc.getId(), e);
                            }
                        }
                        Log.d("HospitalRequests", "Documents found: " + requests.size());
                        activeRequests.setValue(requests);
                    } else {
                        Log.d("HospitalRequests", "Snapshot is null");
                        activeRequests.setValue(new ArrayList<>());
                    }
                });
    }
}