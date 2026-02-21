package com.example.pulseaid.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.admin.Hospital;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageHospitalsViewModel extends ViewModel {

    private MutableLiveData<List<Hospital>> hospitalList = new MutableLiveData<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Hospital>> getHospitals() {
        loadHospitals();
        return hospitalList;
    }

    private void loadHospitals() {
        // Fetching users where role is "Hospital"
        db.collection("Users")
                .whereEqualTo("role", "Hospital")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        List<Hospital> tempRef = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Hospital hospital = doc.toObject(Hospital.class);
                            hospital.setId(doc.getId());
                            tempRef.add(hospital);
                        }
                        hospitalList.setValue(tempRef);
                    }
                });
    }

    // Method to delete a Hospital
    public void deleteHospital(String id) {
        if (id != null) {
            db.collection("Users").document(id).delete();
        }
    }
}